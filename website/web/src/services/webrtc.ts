import { wsClient } from './websocket';

/**
 * WebRTC Configuration using Cloudflare Anycast STUN and TURN (UDP prioritized first).
 */
export const RTC_CONFIGURATION: RTCConfiguration = {
  iceServers: [
    { urls: ['stun:stun.cloudflare.com:3478', 'stun:stun.l.google.com:19302', 'stun:stun1.l.google.com:19302'] },
    {
      urls: [
        'turn:turn.cloudflare.com:3478?transport=udp',
        'turn:turn.cloudflare.com:443?transport=tcp',
        'turns:turn.cloudflare.com:443?transport=tcp',
      ],
      username: 'cloudflare',
      credential: 'cloudflare',
    },
  ],
  iceCandidatePoolSize: 4,
  bundlePolicy: 'max-bundle',
  rtcpMuxPolicy: 'require',
};

/**
 * Mangle SDP to enforce low-latency Opus parameters with forward error correction.
 */
function mangleSdpForLowLatency(sdp: string): string {
  const lines = sdp.split('\r\n');
  let opusPayload: string | null = null;
  for (const line of lines) {
    const m = line.match(/^a=rtpmap:(\d+) opus\/48000/);
    if (m) {
      opusPayload = m[1];
      break;
    }
  }
  const targetPayload = opusPayload || '111';
  const fmtpPrefix = `a=fmtp:${targetPayload}`;
  const opusParams = 'minptime=10; ptime=20; useinbandfec=1; usedtx=1; maxaveragebitrate=40000; stereo=0; cbr=0';
  const hasFmtp = lines.some((l) => l.startsWith(fmtpPrefix));
  const result: string[] = [];
  for (const line of lines) {
    if (line.startsWith(fmtpPrefix)) {
      result.push(`${fmtpPrefix} ${opusParams}`);
    } else if (!hasFmtp && line.startsWith(`a=rtpmap:${targetPayload}`)) {
      result.push(line);
      result.push(`${fmtpPrefix} ${opusParams}`);
    } else {
      result.push(line);
    }
  }
  if (!hasFmtp && !lines.some((l) => l.startsWith(`a=rtpmap:${targetPayload}`))) {
    result.push('a=rtpmap:111 opus/48000/2');
    result.push(`a=fmtp:111 ${opusParams}`);
  }
  return result.join('\r\n') + '\r\n';
}

export class VoiceCallManager {
  private peerConnections: Map<string, RTCPeerConnection> = new Map();
  private localStream: MediaStream | null = null;
  private activeVoiceChannelId: string | null = null;
  private onRemoteStreamCallback: ((peerId: string, stream: MediaStream) => void) | null = null;
  private audioContext: AudioContext | null = null;

  public setOnRemoteStreamListener(callback: (peerId: string, stream: MediaStream) => void) {
    this.onRemoteStreamCallback = callback;
  }

  public getActiveVoiceChannelId(): string | null {
    return this.activeVoiceChannelId;
  }

  /**
   * Unlock Web AudioContext and prime DOM audio element during user gesture (click).
   */
  public unlockAudio() {
    try {
      if (!this.audioContext) {
        const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
        if (AudioCtx) {
          this.audioContext = new AudioCtx({ sampleRate: 48000 });
        }
      }
      if (this.audioContext && this.audioContext.state === 'suspended') {
        this.audioContext.resume();
      }
      const audioEl = document.getElementById('remote_call_audio') as HTMLAudioElement;
      if (audioEl) {
        audioEl.play().catch(() => {});
      }
    } catch (e) {
      // ignore
    }
  }

  public async preFetchTurnCredentials() {
    try {
      const res = await fetch('/api/turn-credentials', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: '{}',
      });
      if (res.ok) {
        const data = await res.json();
        const servers = data.iceServers || data.ice_servers;
        if (Array.isArray(servers) && servers.length > 0) {
          RTC_CONFIGURATION.iceServers = servers.filter((s: any) => {
            const urls = Array.isArray(s.urls) ? s.urls : [s.urls || s.url];
            return !urls.some((u: string) => typeof u === 'string' && u.includes('openrelay.metered.ca'));
          });
        }
      }
    } catch {
      // Fallback to static Cloudflare TURN
    }
  }

  public async joinVoiceChannel(channelId: string) {
    this.activeVoiceChannelId = channelId;
    this.unlockAudio();
    this.preFetchTurnCredentials().catch(() => {});

    try {
      // Capture local microphone stream with echo cancellation & noise suppression
      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
          channelCount: 1,
          sampleRate: 48000,
        },
        video: false,
      });
    } catch (e) {
      console.warn('Microphone access not available or denied:', e);
      this.localStream = new MediaStream(); // Fallback empty stream
    }

    // Join voice room over WebSocket for signaling (100% media is pure WebRTC)
    wsClient.send({
      type: 'voice_join',
      action: 'voice:join',
      channel_id: channelId,
      room_id: channelId,
    });
  }

  public leaveVoiceChannel() {
    if (this.activeVoiceChannelId) {
      wsClient.send({
        type: 'voice_leave',
        action: 'voice:leave',
        channel_id: this.activeVoiceChannelId,
        room_id: this.activeVoiceChannelId,
      });
      this.activeVoiceChannelId = null;
    }

    // Stop and release local audio tracks
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) => track.stop());
      this.localStream = null;
    }

    // Close all peer connections
    this.peerConnections.forEach((pc) => {
      try {
        pc.close();
      } catch (e) {
        console.warn('Error closing PeerConnection:', e);
      }
    });
    this.peerConnections.clear();

    // Clean up all DOM audio elements
    try {
      const audioElements = document.querySelectorAll('[id^="remote_audio_"]');
      audioElements.forEach((el) => el.remove());
      const mainAudio = document.getElementById('remote_call_audio') as HTMLAudioElement;
      if (mainAudio) {
        mainAudio.srcObject = null;
      }
    } catch (e) {
      console.warn('Error cleaning up audio elements:', e);
    }
  }

  public setMuted(muted: boolean) {
    if (this.localStream) {
      this.localStream.getAudioTracks().forEach((track) => {
        track.enabled = !muted;
      });
    }
  }

  public playVoiceChunk(_base64Audio: string) {
    // Suppressed: 100% of media travels over RTCPeerConnection (Opus over RTP/UDP)
  }

  public async initiateOffer(targetPeerId: string) {
    if (!this.activeVoiceChannelId) return;

    const pc = this.createPeerConnection(targetPeerId);

    const offer = await pc.createOffer({
      offerToReceiveAudio: true,
      offerToReceiveVideo: false,
    });
    await pc.setLocalDescription(offer);

    const mangledSdp = mangleSdpForLowLatency(offer.sdp || '');
    wsClient.send({
      type: 'voice_offer',
      action: 'call:offer',
      channel_id: this.activeVoiceChannelId,
      room_id: this.activeVoiceChannelId,
      target_user_id: targetPeerId,
      target: targetPeerId,
      target_user: targetPeerId,
      sdp: mangledSdp,
    });
  }

  public async handleOffer(senderPeerId: string, sdp: string) {
    if (!this.activeVoiceChannelId) return;

    const pc = this.createPeerConnection(senderPeerId);

    // If glare occurs (we already created an offer), rollback local offer
    if (pc.signalingState === 'have-local-offer') {
      try {
        await pc.setLocalDescription({ type: 'rollback' } as any);
      } catch (e) {
        console.warn('Rollback failed on glare:', e);
      }
    }

    try {
      await pc.setRemoteDescription(new RTCSessionDescription({ type: 'offer', sdp }));
      const answer = await pc.createAnswer({
        offerToReceiveAudio: true,
        offerToReceiveVideo: false,
      });
      await pc.setLocalDescription(answer);

      const mangledSdp = mangleSdpForLowLatency(answer.sdp || '');
      wsClient.send({
        type: 'voice_answer',
        action: 'call:answer',
        channel_id: this.activeVoiceChannelId,
        room_id: this.activeVoiceChannelId,
        target_user_id: senderPeerId,
        target: senderPeerId,
        target_user: senderPeerId,
        sdp: mangledSdp,
      });
    } catch (err) {
      console.error('Failed to handle offer:', err);
    }
  }

  public async handleAnswer(senderPeerId: string, sdp: string) {
    const pc = this.peerConnections.get(senderPeerId);
    if (pc && pc.signalingState === 'have-local-offer') {
      try {
        await pc.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp }));
      } catch (err) {
        console.error('Failed to set remote answer:', err);
      }
    }
  }

  public async handleIceCandidate(senderPeerId: string, candidate: any) {
    const pc = this.peerConnections.get(senderPeerId);
    if (pc && candidate) {
      try {
        await pc.addIceCandidate(new RTCIceCandidate(candidate));
      } catch (e) {
        console.warn('Error adding ICE candidate:', e);
      }
    }
  }

  private createPeerConnection(peerId: string): RTCPeerConnection {
    if (this.peerConnections.has(peerId)) {
      return this.peerConnections.get(peerId)!;
    }

    const pc = new RTCPeerConnection(RTC_CONFIGURATION);

    // Attach local audio tracks (Never attached to local destination or speakers)
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) => {
        pc.addTrack(track, this.localStream!);
      });
    }

    // Handle ICE candidates immediately (True Trickle ICE)
    pc.onicecandidate = (event) => {
      if (event.candidate && this.activeVoiceChannelId) {
        wsClient.send({
          type: 'voice_ice_candidate',
          action: 'call:ice_candidate',
          channel_id: this.activeVoiceChannelId,
          room_id: this.activeVoiceChannelId,
          target_user_id: peerId,
          target: peerId,
          target_user: peerId,
          candidate: event.candidate.toJSON(),
        });
      }
    };

    // Handle remote audio stream and play in persistent DOM audio player
    pc.ontrack = (event) => {
      console.log('WebRTC ontrack received from peer:', peerId, event.track.kind);
      // Tight playout delay hints on receiver
      try {
        if ('playoutDelayHint' in event.receiver) {
          (event.receiver as any).playoutDelayHint = 0.05;
        }
        if ('jitterBufferTarget' in event.receiver) {
          (event.receiver as any).jitterBufferTarget = 50;
        }
      } catch {}

      const stream = event.streams && event.streams[0] ? event.streams[0] : new MediaStream([event.track]);

      let audioEl = (document.getElementById('remote_call_audio') ||
                     document.getElementById(`remote_audio_${peerId}`)) as HTMLAudioElement;
      if (!audioEl) {
        audioEl = document.createElement('audio');
        audioEl.id = `remote_audio_${peerId}`;
        audioEl.autoplay = true;
        audioEl.setAttribute('playsinline', 'true');
        audioEl.style.display = 'none';
        document.body.appendChild(audioEl);
      }
      if (audioEl.srcObject !== stream) {
        audioEl.srcObject = stream;
      }
      audioEl.volume = 1.0;
      audioEl.play().catch((err) => {
        console.warn('Auto-play blocked by browser policy, awaiting user gesture:', err);
      });

      if (this.onRemoteStreamCallback) {
        this.onRemoteStreamCallback(peerId, stream);
      }
    };

    this.peerConnections.set(peerId, pc);
    return pc;
  }
}

export const voiceManager = new VoiceCallManager();
