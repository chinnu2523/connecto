import { wsClient } from './websocket';

/**
 * WebRTC Configuration using public STUN servers.
 * Documented Note: A TURN server (e.g., coturn) is required in production for reliable
 * media relay across strict corporate firewalls and symmetric NATs.
 */
export const RTC_CONFIGURATION: RTCConfiguration = {
  iceServers: [
    { urls: ['stun:stun.l.google.com:19302', 'stun:stun1.l.google.com:19302'] },
  ],
};

export class VoiceCallManager {
  private peerConnections: Map<string, RTCPeerConnection> = new Map();
  private localStream: MediaStream | null = null;
  private activeVoiceChannelId: string | null = null;
  private onRemoteStreamCallback: ((peerId: string, stream: MediaStream) => void) | null = null;
  private audioContext: AudioContext | null = null;
  private scriptProcessor: ScriptProcessorNode | null = null;
  private audioSourceNode: MediaStreamAudioSourceNode | null = null;

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
          this.audioContext = new AudioCtx({ sampleRate: 16000 });
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

  public async joinVoiceChannel(channelId: string) {
    this.activeVoiceChannelId = channelId;
    this.unlockAudio();

    try {
      // Capture local microphone stream with echo cancellation & noise suppression
      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
        },
        video: false,
      });
    } catch (e) {
      console.warn('Microphone access not available or denied:', e);
      this.localStream = new MediaStream(); // Fallback empty stream
    }

    // Start WebSocket audio packet streaming fallback
    this.startWebSocketAudioFallback(channelId);

    // Join voice room over WebSocket (support both type and action for compatibility)
    wsClient.send({
      type: 'voice_join',
      action: 'voice:join',
      channel_id: channelId,
      room_id: channelId,
    });
  }

  public startWebSocketAudioFallback(roomId: string) {
    if (!this.localStream || !this.localStream.getAudioTracks().length) return;
    try {
      if (!this.audioContext) {
        const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
        if (AudioCtx) {
          this.audioContext = new AudioCtx({ sampleRate: 16000 });
        }
      }
      if (!this.audioContext) return;
      if (this.audioContext.state === 'suspended') {
        this.audioContext.resume();
      }
      if (this.scriptProcessor) return;

      this.audioSourceNode = this.audioContext.createMediaStreamSource(this.localStream);
      // Process in ~128ms chunks at 16kHz
      this.scriptProcessor = this.audioContext.createScriptProcessor(2048, 1, 1);
      this.scriptProcessor.onaudioprocess = (e) => {
        if (!this.activeVoiceChannelId) return;
        const inputData = e.inputBuffer.getChannelData(0);
        const int16Array = new Int16Array(inputData.length);
        for (let i = 0; i < inputData.length; i++) {
          const s = Math.max(-1, Math.min(1, inputData[i]));
          int16Array[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
        }
        const uint8Array = new Uint8Array(int16Array.buffer);
        let binary = '';
        for (let i = 0; i < uint8Array.byteLength; i++) {
          binary += String.fromCharCode(uint8Array[i]);
        }
        const base64 = btoa(binary);
        wsClient.send({
          type: 'voice_data',
          action: 'voice_data',
          room_id: roomId,
          channel_id: roomId,
          audio: base64,
        });
      };
      this.audioSourceNode.connect(this.scriptProcessor);
      this.scriptProcessor.connect(this.audioContext.destination);
    } catch (e) {
      console.warn('Fallback audio sender initialization failed:', e);
    }
  }

  public playVoiceChunk(base64Audio: string) {
    try {
      if (!this.audioContext) {
        const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
        if (AudioCtx) {
          this.audioContext = new AudioCtx({ sampleRate: 16000 });
        }
      }
      if (!this.audioContext) return;
      if (this.audioContext.state === 'suspended') {
        this.audioContext.resume();
      }

      const binaryStr = atob(base64Audio);
      const len = binaryStr.length;
      const bytes = new Uint8Array(len);
      for (let i = 0; i < len; i++) {
        bytes[i] = binaryStr.charCodeAt(i);
      }
      const int16Array = new Int16Array(bytes.buffer);
      const float32Array = new Float32Array(int16Array.length);
      for (let i = 0; i < int16Array.length; i++) {
        float32Array[i] = int16Array[i] / 32768.0;
      }

      const audioBuffer = this.audioContext.createBuffer(1, float32Array.length, 16000);
      audioBuffer.copyToChannel(float32Array, 0);

      const source = this.audioContext.createBufferSource();
      source.buffer = audioBuffer;
      source.connect(this.audioContext.destination);
      source.start();
    } catch {
      // ignore decode error
    }
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

    if (this.scriptProcessor) {
      try {
        this.scriptProcessor.disconnect();
      } catch {}
      this.scriptProcessor = null;
    }
    if (this.audioSourceNode) {
      try {
        this.audioSourceNode.disconnect();
      } catch {}
      this.audioSourceNode = null;
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

  public async initiateOffer(targetPeerId: string) {
    if (!this.activeVoiceChannelId) return;

    const pc = this.createPeerConnection(targetPeerId);

    const offer = await pc.createOffer({
      offerToReceiveAudio: true,
      offerToReceiveVideo: false,
    });
    await pc.setLocalDescription(offer);

    wsClient.send({
      type: 'voice_offer',
      channel_id: this.activeVoiceChannelId,
      room_id: this.activeVoiceChannelId,
      target_user_id: targetPeerId,
      sdp: offer.sdp,
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

      wsClient.send({
        type: 'voice_answer',
        channel_id: this.activeVoiceChannelId,
        room_id: this.activeVoiceChannelId,
        target_user_id: senderPeerId,
        sdp: answer.sdp,
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

    // Attach local audio tracks
    if (this.localStream) {
      this.localStream.getTracks().forEach((track) => {
        pc.addTrack(track, this.localStream!);
      });
    }

    // Handle ICE candidates
    pc.onicecandidate = (event) => {
      if (event.candidate && this.activeVoiceChannelId) {
        wsClient.send({
          type: 'voice_ice_candidate',
          channel_id: this.activeVoiceChannelId,
          room_id: this.activeVoiceChannelId,
          target_user_id: peerId,
          candidate: event.candidate.toJSON(),
        });
      }
    };

    // Handle remote audio stream and automatically mount & play in DOM audio player
    pc.ontrack = (event) => {
      console.log('WebRTC ontrack received from peer:', peerId, event.track.kind);
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
      audioEl.srcObject = stream;
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
