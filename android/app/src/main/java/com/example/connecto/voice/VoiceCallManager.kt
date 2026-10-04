package com.example.connecto.voice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.ConnectoNetworkConfig
import com.example.connecto.notifications.ConnectoNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import com.example.connecto.network.ConnectoNetworkHelper
import com.example.connecto.network.PollDto
import org.json.JSONArray
import java.net.URLEncoder
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import com.example.connecto.data.ConnectoDatabaseHelper
import com.example.connecto.network.MessageDto
import org.webrtc.AudioSource
import org.webrtc.AudioTrack as RtcAudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule
import org.webrtc.audio.AudioDeviceModule

enum class CallState {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTED
}

data class IncomingCallData(
    val roomId: String,
    val callerId: String,
    val callerUsername: String,
    val callerName: String,
    val callerAvatar: String? = null
)

data class CallStatsDto(
    val roundTripTimeMs: Double,
    val jitterMs: Double,
    val packetsLost: Int,
    val candidateType: String // "host", "srflx", "relay"
)

@SuppressLint("StaticFieldLeak")
object VoiceCallManager {
    private const val TAG = "VoiceCallManager"
    private const val SAMPLE_RATE = 16000
    private const val CHUNK_SIZE_BYTES = 2560 // ~80ms at 16kHz 16-bit mono

    private var appContext: Context? = null
    private var audioManager: AudioManager? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // Call States
    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _activeRoomId = MutableStateFlow<String?>(null)
    val activeRoomId: StateFlow<String?> = _activeRoomId.asStateFlow()

    private val _activeCallTitle = MutableStateFlow("Voice Call")
    val activeCallTitle: StateFlow<String> = _activeCallTitle.asStateFlow()

    private val _activeCallPartner = MutableStateFlow<String?>(null)
    val activeCallPartner: StateFlow<String?> = _activeCallPartner.asStateFlow()

    private val _activeCallAvatar = MutableStateFlow<String?>(null)
    val activeCallAvatar: StateFlow<String?> = _activeCallAvatar.asStateFlow()

    private val _activeCallRoomCode = MutableStateFlow<String?>(null)
    val activeCallRoomCode: StateFlow<String?> = _activeCallRoomCode.asStateFlow()

    private val _incomingCall = MutableStateFlow<IncomingCallData?>(null)
    val incomingCall: StateFlow<IncomingCallData?> = _incomingCall.asStateFlow()

    private val _participantCount = MutableStateFlow(1)
    val participantCount: StateFlow<Int> = _participantCount.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _liveAudioAmplitude = MutableStateFlow(0f)
    val liveAudioAmplitude: StateFlow<Float> = _liveAudioAmplitude.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private var isCallCaller = false

    // Audio Hardware Instances
    @Volatile
    private var isRecordingActive = false
    private val audioHardwareLock = Any()
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordingJob: Job? = null
    private var durationJob: Job? = null
    private var pingJob: Job? = null
    private var statsJob: Job? = null

    // WebRTC call stats (RTT, jitter, etc.) — updated every 2s during a connected call
    private val _callStats = MutableStateFlow<CallStatsDto?>(null)
    val callStats: StateFlow<CallStatsDto?> = _callStats.asStateFlow()

    // WebSocket
    private val okHttpClient by lazy {
        ConnectoNetworkHelper.getCompatibleOkHttpClient(isWebSocket = true)
    }
    private var webSocket: WebSocket? = null
    private var isWsConnected = false
    private val _isWsConnectedFlow = MutableStateFlow(false)
    val isWsConnectedFlow: StateFlow<Boolean> = _isWsConnectedFlow.asStateFlow()

    // Real-time Channel Subscriptions & Incoming Message Bus
    private val subscribedChannels = java.util.concurrent.CopyOnWriteArraySet<String>()
    private val _incomingChannelMessages = MutableSharedFlow<MessageDto>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingChannelMessages: SharedFlow<MessageDto> = _incomingChannelMessages.asSharedFlow()
    private val _incomingPollUpdates = MutableSharedFlow<PollDto>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingPollUpdates: SharedFlow<PollDto> = _incomingPollUpdates.asSharedFlow()

    // Typing indicator events: Pair(username, isTyping)
    data class TypingEvent(val username: String, val displayName: String, val channelId: String, val isTyping: Boolean)
    private val _typingEvents = MutableSharedFlow<TypingEvent>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val typingEvents: SharedFlow<TypingEvent> = _typingEvents.asSharedFlow()

    // Real-time user presence updates: Pair(username, isOnline)
    private val _userPresenceUpdates = MutableSharedFlow<Pair<String, Boolean>>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val userPresenceUpdates: SharedFlow<Pair<String, Boolean>> = _userPresenceUpdates.asSharedFlow()

    // @mention notification events
    data class MentionEvent(val fromUser: String, val channelId: String, val channelName: String, val messageId: String, val content: String)
    private val _mentionEvents = MutableSharedFlow<MentionEvent>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val mentionEvents: SharedFlow<MentionEvent> = _mentionEvents.asSharedFlow()

    // Message read receipt events
    data class ReadReceiptEvent(val messageId: String, val channelId: String, val readerUsername: String)
    private val _readReceiptEvents = MutableSharedFlow<ReadReceiptEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val readReceiptEvents: SharedFlow<ReadReceiptEvent> = _readReceiptEvents.asSharedFlow()

    fun subscribeChannel(channelNameOrId: String) {
        val clean = channelNameOrId.trim().lowercase().removePrefix("#")
        if (clean.isEmpty()) return
        subscribedChannels.add(clean)
        if (isWsConnected && webSocket != null) {
            try {
                // connecto.fun single-channel action format
                val subAction = JSONObject().apply {
                    put("action", "subscribe")
                    put("channel_id", clean)
                }
                sendWsMessage(subAction)

                // Standard channels array format
                val subMsg = JSONObject().apply {
                    put("type", "subscribe")
                    put("channels", JSONArray().put(clean))
                }
                sendWsMessage(subMsg)
                Log.d(TAG, "Subscribed to channel over WebSocket: $clean")
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribing to channel: ${e.message}")
            }
        }
    }

    /** Send a typing_start event to the server for the given channel. */
    fun sendTypingStart(channelId: String) {
        val clean = channelId.trim().lowercase().removePrefix("#")
        if (!isWsConnected || clean.isEmpty()) return
        try {
            sendWsMessage(JSONObject().apply {
                put("type", "typing_start")
                put("action", "typing:start")
                put("channel_id", clean)
            })
        } catch (_: Exception) {}
    }

    /** Send a typing_stop event to the server for the given channel. */
    fun sendTypingStop(channelId: String) {
        val clean = channelId.trim().lowercase().removePrefix("#")
        if (!isWsConnected || clean.isEmpty()) return
        try {
            sendWsMessage(JSONObject().apply {
                put("type", "typing_stop")
                put("action", "typing:stop")
                put("channel_id", clean)
            })
        } catch (_: Exception) {}
    }

    // WebRTC Engine & Concurrency Controls
    private val webRtcTeardownLock = Any()
    private val callTeardownLock = Any()
    private val disposedPcs: MutableSet<PeerConnection> = Collections.synchronizedSet(Collections.newSetFromMap(IdentityHashMap<PeerConnection, Boolean>()))
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: RtcAudioTrack? = null
    private val peerConnections = ConcurrentHashMap<String, PeerConnection>()

    // ICE servers: Only our own connecto.fun STUN/TURN + one Google STUN fallback.
    // Removed 4 third-party openrelay.metered.ca servers — each one added 200-800ms of
    // parallel ICE gathering delay before WebRTC could converge on the best candidate pair.
    private val iceServers by lazy {
        listOf(
            // Primary: connecto.fun STUN
            PeerConnection.IceServer.builder("stun:connecto.fun:3478").createIceServer(),
            // Primary: connecto.fun TURN over UDP — lowest latency relay path
            PeerConnection.IceServer.builder("turn:connecto.fun:3478?transport=udp")
                .setUsername("connecto").setPassword("ConnectoVoice2026!").createIceServer(),
            // Primary: connecto.fun TURN over TCP — firewall fallback
            PeerConnection.IceServer.builder("turn:connecto.fun:3478?transport=tcp")
                .setUsername("connecto").setPassword("ConnectoVoice2026!").createIceServer(),
            // Secondary: Google STUN — internet-side fallback
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
    }

    private fun getRtcConfig(): PeerConnection.RTCConfiguration {
        return PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            // GATHER_ONCE: stop after first complete set of candidates.
            // GATHER_CONTINUALLY was causing perpetual ICE restarts adding 100-300ms overhead.
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_ONCE
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            // Pre-gather 10 candidate slots before call needs them, hiding ICE latency
            // behind signaling exchange. Raised from 2 to 10.
            iceCandidatePoolSize = 10
            // Disable TCP candidate fallback — TCP adds ~50ms vs UDP; only fallback if UDP fails.
            // WebRTC already handles TCP fallback internally via TURN TCP server above.
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
        }
    }

    /** Mangle SDP to enforce low-latency Opus parameters. */
    private fun mangleSdpForLowLatency(sdp: String): String {
        val lines = sdp.lines().toMutableList()
        var opusPayload: String? = null
        for (line in lines) {
            val m = Regex("""a=rtpmap:(\d+) opus/48000""").find(line)
            if (m != null) { opusPayload = m.groupValues[1]; break }
        }
        if (opusPayload == null) return sdp
        val fmtpPrefix = "a=fmtp:$opusPayload"
        val opusParams = "minptime=10;ptime=10;maxaveragebitrate=32000;stereo=0;sprop-stereo=0;usedtx=0;cbr=1"
        val hasFmtp = lines.any { it.startsWith(fmtpPrefix) }
        val result = mutableListOf<String>()
        for (line in lines) {
            when {
                line.startsWith(fmtpPrefix) ->
                    result.add("$fmtpPrefix $opusParams")
                !hasFmtp && line.startsWith("a=rtpmap:$opusPayload") -> {
                    result.add(line)
                    result.add("$fmtpPrefix $opusParams")
                }
                else -> result.add(line)
            }
        }
        return result.joinToString("\n")
    }

    private fun initWebRtcFactory(context: Context) {
        if (peerConnectionFactory != null) return
        try {
            val initOptions = PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initOptions)

            val hasAec = try { JavaAudioDeviceModule.isBuiltInAcousticEchoCancelerSupported() } catch (_: Throwable) { false }
            val hasNs = try { JavaAudioDeviceModule.isBuiltInNoiseSuppressorSupported() } catch (_: Throwable) { false }

            val admBuilder = JavaAudioDeviceModule.builder(context.applicationContext)
                .setUseHardwareAcousticEchoCanceler(hasAec)
                .setUseHardwareNoiseSuppressor(hasNs)

            admBuilder.setAudioRecordErrorCallback(object : JavaAudioDeviceModule.AudioRecordErrorCallback {
                override fun onWebRtcAudioRecordInitError(errorMessage: String?) {
                    Log.e(TAG, "WebRTC AudioRecord Init Error: $errorMessage")
                }
                override fun onWebRtcAudioRecordStartError(errorCode: JavaAudioDeviceModule.AudioRecordStartErrorCode?, errorMessage: String?) {
                    Log.e(TAG, "WebRTC AudioRecord Start Error: $errorCode - $errorMessage")
                }
                override fun onWebRtcAudioRecordError(errorMessage: String?) {
                    Log.e(TAG, "WebRTC AudioRecord Error: $errorMessage")
                }
            })

            admBuilder.setAudioTrackErrorCallback(object : JavaAudioDeviceModule.AudioTrackErrorCallback {
                override fun onWebRtcAudioTrackInitError(errorMessage: String?) {
                    Log.e(TAG, "WebRTC AudioTrack Init Error: $errorMessage")
                }
                override fun onWebRtcAudioTrackStartError(errorCode: JavaAudioDeviceModule.AudioTrackStartErrorCode?, errorMessage: String?) {
                    Log.e(TAG, "WebRTC AudioTrack Start Error: $errorCode - $errorMessage")
                }
                override fun onWebRtcAudioTrackError(errorMessage: String?) {
                    Log.e(TAG, "WebRTC AudioTrack Error: $errorMessage")
                }
            })

            try {
                admBuilder.setSamplesReadyCallback { samples ->
                    try {
                        val data = samples.data
                        if (data.isNotEmpty() && !_isMuted.value) {
                            var sum = 0.0
                            val shortsCount = data.size / 2
                            for (i in 0 until shortsCount) {
                                val low = data[i * 2].toInt() and 0xFF
                                val high = data[i * 2 + 1].toInt()
                                val sample = (high shl 8) or low
                                sum += sample * sample
                            }
                            val rms = if (shortsCount > 0) Math.sqrt(sum / shortsCount) else 0.0
                            val rawAmp = (rms / 16000.0).toFloat()
                            val normalized = if (rawAmp.isNaN() || rawAmp.isInfinite()) 0f else rawAmp.coerceIn(0f, 1f)
                            _liveAudioAmplitude.value = normalized

                            // Play back locally if echo test
                            if (_activeCallTitle.value.contains("Echo", ignoreCase = true)) {
                                audioTrack?.write(data, 0, data.size, AudioTrack.WRITE_NON_BLOCKING)
                            }

                            // Relay voice_data over WebSocket if P2P peer connections are not established
                            val roomId = _activeRoomId.value
                            if (_callState.value == CallState.CONNECTED && !roomId.isNullOrBlank() && peerConnections.isEmpty()) {
                                val base64Str = Base64.encodeToString(data, 0, data.size, Base64.NO_WRAP)
                                val packet = JSONObject().apply {
                                    put("action", "voice_data")
                                    put("type", "voice_data")
                                    put("room_id", roomId)
                                    put("channel_id", roomId)
                                    put("audio", base64Str)
                                }
                                sendWsMessage(packet)
                            }
                        } else {
                            _liveAudioAmplitude.value = 0f
                        }
                    } catch (_: Throwable) {}
                }
            } catch (t: Throwable) {
                Log.w(TAG, "SamplesReadyCallback not supported: ${t.message}")
            }

            val adm = admBuilder.createAudioDeviceModule()
            val options = PeerConnectionFactory.Options()
            peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(adm)
                .createPeerConnectionFactory()
            Log.d(TAG, "PeerConnectionFactory initialized successfully with JavaAudioDeviceModule (AEC=$hasAec, NS=$hasNs)")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize PeerConnectionFactory: ${t.message}", t)
        }
    }

    private fun startLocalAudio() {
        val factory = peerConnectionFactory ?: return
        if (localAudioTrack != null) return

        val context = appContext ?: return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot start local WebRTC audio: RECORD_AUDIO permission missing")
            return
        }

        try {
            val audioConstraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            }
            val source = factory.createAudioSource(audioConstraints)
            localAudioSource = source
            val track = factory.createAudioTrack("ARDAMSa0", source)
            track.setEnabled(!_isMuted.value)
            localAudioTrack = track
            Log.d(TAG, "WebRTC local audio track created (enabled=${!_isMuted.value})")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to create local WebRTC audio track: ${t.message}", t)
        }
    }

    private fun getOrCreatePeerConnection(peerId: String): PeerConnection? {
        synchronized(webRtcTeardownLock) {
            val existing = peerConnections[peerId]
            if (existing != null && !disposedPcs.contains(existing)) {
                return existing
            }
            val factory = peerConnectionFactory ?: return null
            startLocalAudio()

            val rtcConfig = getRtcConfig()
            val observer = object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    val roomId = _activeRoomId.value ?: return
                    Log.d(TAG, "WebRTC ICE candidate generated for peer: $peerId (${candidate.sdpMid})")
                    try {
                        val candObj = JSONObject().apply {
                            put("candidate", candidate.sdp)
                            put("sdpMid", candidate.sdpMid)
                            put("sdpMLineIndex", candidate.sdpMLineIndex)
                        }
                        val msg = JSONObject().apply {
                            put("type", "voice_ice_candidate")
                            put("action", "call:ice-candidate")
                            put("channel_id", roomId)
                            put("room_id", roomId)
                            put("call_id", roomId)
                            put("target_user_id", peerId)
                            put("target", peerId)
                            put("target_user", peerId)
                            put("candidate", candObj)
                        }
                        sendWsMessage(msg)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed sending ICE candidate: ${e.message}")
                    }
                }

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}

                override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
                    Log.d(TAG, "WebRTC ICE connection state for peer $peerId: $newState")
                    if (newState == PeerConnection.IceConnectionState.CONNECTED ||
                        newState == PeerConnection.IceConnectionState.COMPLETED) {
                        _callState.value = CallState.CONNECTED
                        startStatsPolling(peerId)
                    }
                }

                override fun onIceConnectionReceivingChange(receiving: Boolean) {}

                override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {
                    Log.d(TAG, "WebRTC ICE gathering state for peer $peerId: $newState")
                }

                override fun onSignalingChange(newState: PeerConnection.SignalingState) {
                    Log.d(TAG, "WebRTC signaling state for peer $peerId: $newState")
                }

                override fun onAddStream(stream: MediaStream) {
                    Log.d(TAG, "WebRTC onAddStream from peer $peerId: ${stream.audioTracks.size} audio tracks")
                    for (audioTrack in stream.audioTracks) {
                        audioTrack.setEnabled(true)
                        audioTrack.setVolume(1.0)
                    }
                }

                override fun onRemoveStream(stream: MediaStream) {
                    Log.d(TAG, "WebRTC onRemoveStream from peer $peerId")
                }

                override fun onDataChannel(dataChannel: DataChannel) {}

                override fun onRenegotiationNeeded() {
                    Log.d(TAG, "WebRTC renegotiation needed for peer $peerId")
                }

                override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<out MediaStream>) {
                    val track = receiver.track()
                    Log.d(TAG, "WebRTC onAddTrack from peer $peerId, kind=${track?.kind()}")
                    if (track is RtcAudioTrack) {
                        track.setEnabled(true)
                        track.setVolume(1.0)
                    }
                    for (stream in mediaStreams) {
                        for (audioTrack in stream.audioTracks) {
                            audioTrack.setEnabled(true)
                            audioTrack.setVolume(1.0)
                        }
                    }
                }
            }

            val pc = factory.createPeerConnection(rtcConfig, observer) ?: return null
            localAudioTrack?.let { track ->
                pc.addTrack(track, listOf("ARDAMS"))
            }
            peerConnections[peerId] = pc
            Log.d(TAG, "WebRTC PeerConnection registered for peer: $peerId")
            return pc
        }
    }

    fun initiateWebRtcOffer(peerId: String) {
        scope.launch(Dispatchers.IO) {
            val pc = getOrCreatePeerConnection(peerId) ?: return@launch
            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
            }
            pc.createOffer(object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    pc.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            val roomId = _activeRoomId.value ?: return
                            Log.d(TAG, "WebRTC Offer created and local description set for peer: $peerId")
                            val offerMsg = JSONObject().apply {
                                put("type", "voice_offer")
                                put("action", "call:offer")
                                put("channel_id", roomId)
                                put("room_id", roomId)
                                put("call_id", roomId)
                                put("target_user_id", peerId)
                                put("target", peerId)
                                put("target_user", peerId)
                                put("sdp", mangleSdpForLowLatency(sessionDescription.description))
                            }
                            sendWsMessage(offerMsg)
                        }

                        override fun onSetFailure(s: String?) {
                            Log.e(TAG, "WebRTC setLocalDescription failure: $s")
                        }

                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, sessionDescription)
                }

                override fun onSetSuccess() {}

                override fun onCreateFailure(s: String?) {
                    Log.e(TAG, "WebRTC createOffer failure: $s")
                }

                override fun onSetFailure(p0: String?) {}
            }, constraints)
        }
    }

    fun handleWebRtcOffer(senderPeerId: String, sdpDescription: String) {
        scope.launch(Dispatchers.IO) {
            val pc = getOrCreatePeerConnection(senderPeerId) ?: return@launch
            val remoteDesc = SessionDescription(SessionDescription.Type.OFFER, sdpDescription)
            pc.setRemoteDescription(object : SdpObserver {
                override fun onSetSuccess() {
                    val constraints = MediaConstraints().apply {
                        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
                    }
                    pc.createAnswer(object : SdpObserver {
                        override fun onCreateSuccess(sessionDescription: SessionDescription) {
                            pc.setLocalDescription(object : SdpObserver {
                                override fun onSetSuccess() {
                                    val roomId = _activeRoomId.value ?: return
                                    Log.d(TAG, "WebRTC Answer created and local description set for peer: $senderPeerId")
                                    val answerMsg = JSONObject().apply {
                                        put("type", "voice_answer")
                                        put("action", "call:answer")
                                        put("channel_id", roomId)
                                        put("room_id", roomId)
                                        put("call_id", roomId)
                                        put("target_user_id", senderPeerId)
                                        put("target", senderPeerId)
                                        put("target_user", senderPeerId)
                                        put("sdp", mangleSdpForLowLatency(sessionDescription.description))
                                    }
                                    sendWsMessage(answerMsg)
                                }

                                override fun onSetFailure(s: String?) {
                                    Log.e(TAG, "WebRTC setLocalDescription for answer failure: $s")
                                }

                                override fun onCreateSuccess(p0: SessionDescription?) {}
                                override fun onCreateFailure(p0: String?) {}
                            }, sessionDescription)
                        }

                        override fun onCreateFailure(s: String?) {
                            Log.e(TAG, "WebRTC createAnswer failure: $s")
                        }

                        override fun onSetSuccess() {}
                        override fun onSetFailure(p0: String?) {}
                    }, constraints)
                }

                override fun onSetFailure(s: String?) {
                    Log.e(TAG, "WebRTC setRemoteDescription for offer failure: $s")
                }

                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onCreateFailure(p0: String?) {}
            }, remoteDesc)
        }
    }

    fun getPeerConnectionForPeer(peerId: String, senderUsername: String? = null): PeerConnection? {
        // 1. Direct exact key match
        peerConnections[peerId]?.let { if (!disposedPcs.contains(it)) return it }

        // 2. Exact match on senderUsername if provided
        if (!senderUsername.isNullOrBlank()) {
            peerConnections[senderUsername]?.let { if (!disposedPcs.contains(it)) return it }
            val cleanU = senderUsername.trim().lowercase().removePrefix("@")
            peerConnections[cleanU]?.let { if (!disposedPcs.contains(it)) return it }
        }

        // 3. Clean handle / case-insensitive search
        val cleanId = peerId.trim().lowercase().removePrefix("@")
        for ((key, pc) in peerConnections) {
            if (disposedPcs.contains(pc)) continue
            val cleanKey = key.trim().lowercase().removePrefix("@")
            if (cleanKey == cleanId || cleanKey.contains(cleanId) || cleanId.contains(cleanKey)) {
                return pc
            }
        }

        // 4. If 1:1 call partner matches
        val partner = _activeCallPartner.value?.trim()?.lowercase()?.removePrefix("@")
        if (partner != null && (partner == cleanId || (senderUsername != null && partner == senderUsername.trim().lowercase().removePrefix("@")))) {
            peerConnections[partner]?.let { if (!disposedPcs.contains(it)) return it }
            for ((key, pc) in peerConnections) {
                if (disposedPcs.contains(pc)) continue
                val cleanKey = key.trim().lowercase().removePrefix("@")
                if (cleanKey == partner) return pc
            }
        }

        // 5. In any 1:1 call, if there is exactly 1 valid non-disposed PeerConnection, it MUST be the remote party!
        val activePcs = peerConnections.values.filter { !disposedPcs.contains(it) }.distinct()
        if (activePcs.size == 1) {
            val singlePc = activePcs.firstOrNull()
            if (singlePc != null) {
                // Register alias for future instant O(1) lookups
                peerConnections[peerId] = singlePc
                if (!senderUsername.isNullOrBlank()) {
                    peerConnections[senderUsername] = singlePc
                }
                return singlePc
            }
        }

        return null
    }

    fun handleWebRtcAnswer(senderPeerId: String, sdpDescription: String, senderUsername: String? = null) {
        scope.launch(Dispatchers.IO) {
            val pc = getPeerConnectionForPeer(senderPeerId, senderUsername) ?: run {
                Log.w(TAG, "handleWebRtcAnswer: No PeerConnection found for $senderPeerId / $senderUsername (existing: ${peerConnections.keys()})")
                return@launch
            }
            val remoteDesc = SessionDescription(SessionDescription.Type.ANSWER, sdpDescription)
            pc.setRemoteDescription(object : SdpObserver {
                override fun onSetSuccess() {
                    Log.d(TAG, "WebRTC setRemoteDescription for answer succeeded from: $senderPeerId ($senderUsername)")
                }

                override fun onSetFailure(s: String?) {
                    Log.e(TAG, "WebRTC setRemoteDescription for answer failed: $s")
                }

                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onCreateFailure(p0: String?) {}
            }, remoteDesc)
        }
    }

    fun handleWebRtcIceCandidate(senderPeerId: String, candObj: JSONObject, senderUsername: String? = null) {
        scope.launch(Dispatchers.IO) {
            val pc = getPeerConnectionForPeer(senderPeerId, senderUsername) ?: run {
                Log.w(TAG, "handleWebRtcIceCandidate: No PeerConnection found for $senderPeerId / $senderUsername")
                return@launch
            }
            try {
                val sdpMid = candObj.optString("sdpMid", "0")
                val sdpMLineIndex = candObj.optInt("sdpMLineIndex", 0)
                val candidateSdp = candObj.optString("candidate")
                if (candidateSdp.isNotEmpty()) {
                    val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidateSdp)
                    pc.addIceCandidate(iceCandidate)
                    Log.d(TAG, "WebRTC added ICE candidate from: $senderPeerId ($senderUsername)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "WebRTC error adding ICE candidate: ${e.message}")
            }
        }
    }

    private fun startStatsPolling(peerId: String) {
        statsJob?.cancel()
        statsJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2000)
                val pc = peerConnections[peerId] ?: break
                if (disposedPcs.contains(pc)) break
                try {
                    pc.getStats { report ->
                        if (!isActive || disposedPcs.contains(pc)) return@getStats
                        try {
                            var rttMs = 0.0
                            var jitterMs = 0.0
                            var packetsLost = 0
                            var candidateType = "unknown"
                            for (stats in report.statsMap.values) {
                                when (stats.type) {
                                    "remote-inbound-rtp" -> {
                                        (stats.members["roundTripTime"] as? Double)?.let { rttMs = it * 1000 }
                                        (stats.members["jitter"] as? Double)?.let { jitterMs = it * 1000 }
                                        (stats.members["packetsLost"] as? Int)?.let { packetsLost = it }
                                    }
                                    "candidate-pair" -> {
                                        val nominated = stats.members["nominated"] as? Boolean ?: false
                                        if (nominated) {
                                            val localId = stats.members["localCandidateId"] as? String
                                            if (localId != null) {
                                                val localStats = report.statsMap[localId]
                                                candidateType = (localStats?.members?.get("candidateType") as? String) ?: "unknown"
                                            }
                                        }
                                    }
                                }
                            }
                            val dto = CallStatsDto(rttMs, jitterMs, packetsLost, candidateType)
                            _callStats.value = dto
                            Log.d(TAG, "WebRTC stats: RTT=${rttMs.toInt()}ms jitter=${jitterMs.toInt()}ms loss=$packetsLost candidate=$candidateType")
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing WebRTC stats: ${e.message}")
                        }
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "Failed to getStats: ${t.message}")
                    break
                }
            }
        }
    }

    private fun stopStatsPolling() {
        statsJob?.cancel()
        statsJob = null
        _callStats.value = null
    }

    private fun stopWebRtc() {
        synchronized(webRtcTeardownLock) {
            stopStatsPolling()
            try {
                // 1. Immediately disable local audio track to prevent audio processing threads
                // from pushing frames into closing peer connections.
                try {
                    localAudioTrack?.setEnabled(false)
                } catch (t: Throwable) {
                    Log.w(TAG, "Error disabling localAudioTrack: ${t.message}")
                }

                // 2. Snapshot unique PeerConnection instances (identity set) and clear lookup map
                val uniquePcs = peerConnections.values.filterNotNull().toSet()
                peerConnections.clear()

                // 3. Gracefully close and dispose each unique PeerConnection once and only once
                for (pc in uniquePcs) {
                    if (!disposedPcs.contains(pc)) {
                        disposedPcs.add(pc)
                        try {
                            // Detach senders if possible
                            try {
                                for (sender in pc.senders) {
                                    try {
                                        pc.removeTrack(sender)
                                    } catch (_: Throwable) {}
                                }
                            } catch (_: Throwable) {}

                            pc.close()
                            pc.dispose()
                            Log.d(TAG, "PeerConnection successfully closed and disposed")
                        } catch (t: Throwable) {
                            Log.e(TAG, "Error disposing PeerConnection: ${t.message}")
                        }
                    }
                }

                // 4. Dispose local audio track & source after PeerConnection teardown
                try {
                    localAudioTrack?.dispose()
                } catch (t: Throwable) {
                    Log.e(TAG, "Error disposing localAudioTrack: ${t.message}")
                } finally {
                    localAudioTrack = null
                }

                try {
                    localAudioSource?.dispose()
                } catch (t: Throwable) {
                    Log.e(TAG, "Error disposing localAudioSource: ${t.message}")
                } finally {
                    localAudioSource = null
                }

                Log.d(TAG, "WebRTC engine successfully stopped and disposed")
            } catch (t: Throwable) {
                Log.e(TAG, "Error stopping WebRTC: ${t.message}")
            }
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        audioManager = appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        initWebRtcFactory(context)
        connectWebSocket()
        startPeriodicPing()
    }

    fun updateCredentialsAndConnect(username: String?, userId: String?, token: String?) {
        if (!username.isNullOrBlank()) {
            ConnectoApiClient.currentUsername = username
        }
        if (!userId.isNullOrBlank()) {
            ConnectoApiClient.currentUserId = userId
        }
        if (!token.isNullOrBlank()) {
            ConnectoApiClient.sessionToken = token
        }
        Log.d(TAG, "Updating credentials and reconnecting voice signaling WebSocket: $username ($userId)")
        try {
            webSocket?.close(1000, "Credential update")
        } catch (e: Exception) {
            // ignore
        }
        webSocket = null
        isWsConnected = false
        connectWebSocket(force = true)
    }

    // ----------------------------------------------------
    // WebSocket Lifecycle & Messaging
    // ----------------------------------------------------

    @Volatile
    private var isExplicitlyDisconnected = false

    fun connectWebSocket(force: Boolean = false) {
        isExplicitlyDisconnected = false
        if (!force && isWsConnected && webSocket != null) return

        if (force) {
            try {
                webSocket?.cancel()
            } catch (e: Exception) {
                // ignore
            }
            webSocket = null
            isWsConnected = false
        }

        val username = ConnectoApiClient.currentUsername?.ifEmpty { "User" } ?: "User"
        val cleanUser = username.trim().lowercase().removePrefix("@")
        val userId = ConnectoApiClient.currentUserId?.ifEmpty { cleanUser } ?: cleanUser
        val token = ConnectoApiClient.sessionToken ?: ""

        val encodedUser = try { URLEncoder.encode(cleanUser, "UTF-8") } catch (_: Exception) { cleanUser }
        val encodedId = try { URLEncoder.encode(userId, "UTF-8") } catch (_: Exception) { userId }
        val encodedToken = try { URLEncoder.encode(token, "UTF-8") } catch (_: Exception) { token }

        val wsPath = "/api/v1/ws?user_id=$encodedId&username=$encodedUser&token=$encodedToken"
        val url = ConnectoNetworkConfig.getWsUrl(wsPath)

        Log.d(TAG, "Connecting voice signaling WebSocket to: $url")
        val request = Request.Builder().url(url).build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "Voice WebSocket connected successfully to $url")
                isWsConnected = true
                _isWsConnectedFlow.value = true

                // Auto-resubscribe to all active channels
                if (subscribedChannels.isNotEmpty()) {
                    try {
                        for (ch in subscribedChannels) {
                            val subAction = JSONObject().apply {
                                put("action", "subscribe")
                                put("channel_id", ch)
                            }
                            ws.send(subAction.toString())
                        }
                        val arr = JSONArray()
                        subscribedChannels.forEach { arr.put(it) }
                        val subMsg = JSONObject().apply {
                            put("type", "subscribe")
                            put("channels", arr)
                        }
                        ws.send(subMsg.toString())
                        Log.d(TAG, "Resubscribed to channels on WebSocket open: $subscribedChannels")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error resubscribing to channels: ${e.message}")
                    }
                }

                // Ensure continuous keepalive pinging
                startPeriodicPing()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                handleWebSocketMessage(text)
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Voice WebSocket closing: $code / $reason")
                isWsConnected = false
                _isWsConnectedFlow.value = false
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Voice WebSocket closed: code=$code, reason=$reason")
                isWsConnected = false
                _isWsConnectedFlow.value = false
                if (code != 1000 && !isExplicitlyDisconnected) {
                    reconnectWebSocketAfterDelay()
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Voice WebSocket failure: ${t.message}")
                isWsConnected = false
                _isWsConnectedFlow.value = false
                if (!isExplicitlyDisconnected) {
                    reconnectWebSocketAfterDelay()
                }
            }
        })
    }

    fun disconnectWebSocket() {
        isExplicitlyDisconnected = true
        pingJob?.cancel()
        try {
            webSocket?.close(1000, "App paused or disconnected")
        } catch (_: Exception) {}
        webSocket = null
        isWsConnected = false
        _isWsConnectedFlow.value = false
    }

    private fun reconnectWebSocketAfterDelay() {
        if (isExplicitlyDisconnected) return
        scope.launch {
            delay(3000L)
            if (!isWsConnected && !isExplicitlyDisconnected) {
                connectWebSocket()
            }
        }
    }

    private fun startPeriodicPing() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive) {
                delay(15000L)
                if (isWsConnected && webSocket != null) {
                    try {
                        val ping = JSONObject().apply { put("type", "ping") }
                        webSocket?.send(ping.toString())
                    } catch (e: Exception) {
                        Log.e(TAG, "Ping error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun sendWsMessage(json: JSONObject) {
        val msg = json.toString()
        val sent = webSocket?.send(msg) ?: false
        if (!sent) {
            Log.w(TAG, "Failed to send WS message, attempting reconnect: $msg")
            connectWebSocket()
        }
    }

    private fun handleWebSocketMessage(rawText: String) {
        try {
            val json = JSONObject(rawText)
            val msgType = json.optString("type").ifEmpty { json.optString("action") }

            when (msgType) {
                "ping" -> {
                    try {
                        val pong = JSONObject().apply { put("type", "pong") }
                        webSocket?.send(pong.toString())
                    } catch (_: Exception) {}
                }
                "pong" -> {
                    // Keepalive pong received
                }
                "message_created", "new_message" -> {
                    val data = json.optJSONObject("data") ?: json.optJSONObject("message")
                    if (data != null) {
                        val rawChId = json.optString("channel_id").ifEmpty { data.optString("channel_id") }
                        val rawChName = json.optString("channel_name").ifEmpty { data.optString("channel_name") }
                        val chId = if (rawChName.isNotBlank() && !rawChName.startsWith("dm-") && !rawChName.startsWith("dm_")) {
                            ConnectoApiClient.channelCache[rawChName.lowercase()] = rawChId
                            ConnectoApiClient.channelCache[rawChId.lowercase()] = rawChName.lowercase()
                            rawChName.lowercase()
                        } else {
                            rawChId
                        }
                        val senderUsername = data.optString("sender_username").ifEmpty { data.optString("user").ifEmpty { data.optString("sender") } }
                        val senderDisplay = data.optString("sender_display_name").ifEmpty { data.optString("nickname").ifEmpty { senderUsername } }
                        val content = data.optString("content").ifEmpty { data.optString("text") }
                        val msgId = data.optString("id")
                        val createdAt = data.optString("created_at").ifEmpty { data.optString("timestamp") }
                        val authorAvatar = data.optString("avatar_url").ifEmpty { data.optString("author_avatar").ifEmpty { data.optString("avatar").ifEmpty { null } } }
                        var poll = if (data.has("poll") && !data.isNull("poll")) ConnectoApiClient.parsePollDto(data.optJSONObject("poll")) else null
                        var pollId = data.optString("poll_id").ifEmpty { poll?.id }
                        var type = data.optString("type").ifEmpty { if (poll != null) "poll" else "text" }

                        if (poll == null) {
                            val att = data.opt("attachments")
                            val attObj = when (att) {
                                is org.json.JSONObject -> att
                                is String -> try { if (att.trim().startsWith("{")) org.json.JSONObject(att) else null } catch (_: Exception) { null }
                                else -> null
                            }
                            if (attObj != null) {
                                if (attObj.optString("type") == "poll") type = "poll"
                                if (pollId.isNullOrBlank() && attObj.has("poll_id")) pollId = attObj.optString("poll_id")
                                if (attObj.has("poll")) poll = ConnectoApiClient.parsePollDto(attObj.optJSONObject("poll"))
                            }
                        }

                        val timerSeconds = if (data.has("timer_seconds")) data.optInt("timer_seconds", 0).let { if (it > 0) it else null } else null
                        val expiresAt = data.optString("expires_at").ifEmpty { null }

                        val msgDto = MessageDto(
                            id = msgId,
                            channelId = chId,
                            authorId = senderUsername,
                            authorName = senderDisplay,
                            content = content,
                            createdAt = createdAt,
                            authorAvatar = authorAvatar,
                            type = type,
                            pollId = pollId,
                            poll = poll,
                            timerSeconds = timerSeconds,
                            expiresAt = expiresAt
                        )
                        appContext?.let { ctx ->
                            try {
                                ConnectoDatabaseHelper.getInstance(ctx).saveMessage(msgDto)
                                if (rawChName.isNotBlank() && rawChName != chId) {
                                    ConnectoDatabaseHelper.getInstance(ctx).saveMessage(msgDto.copy(channelId = rawChName))
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to cache message locally: ${e.message}")
                            }
                        }
                        _incomingChannelMessages.tryEmit(msgDto)
                        Log.d(TAG, "Real-time message received via WebSocket for channel $chId: $content")
                    }
                }

                "poll:voted", "poll_updated", "poll:updated" -> {
                    val pollObj = json.optJSONObject("poll")
                    val poll = ConnectoApiClient.parsePollDto(pollObj)
                    if (poll != null) {
                        _incomingPollUpdates.tryEmit(poll)
                        appContext?.let { ctx ->
                            try {
                                val dbHelper = ConnectoDatabaseHelper.getInstance(ctx)
                                val messages = dbHelper.getMessagesForChannel(poll.channelId)
                                for (msg in messages) {
                                    if (msg.poll?.id == poll.id || msg.pollId == poll.id) {
                                        dbHelper.saveMessage(msg.copy(poll = poll))
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to update cached poll: ${e.message}")
                            }
                        }
                        Log.d(TAG, "Real-time poll update received via WebSocket for poll ${poll.id}")
                    }
                }
                "user_updated", "profile_updated", "user:updated" -> {
                    val u = json.optString("username").ifEmpty { json.optJSONObject("data")?.optString("username") ?: "" }
                    val myUser = ConnectoApiClient.currentUsername ?: ""
                    if (u.isNotBlank() && u.equals(myUser, ignoreCase = true)) {
                        val newAvatar = json.optString("avatar_url").ifEmpty {
                            json.optString("avatar").ifEmpty {
                                json.optJSONObject("data")?.optString("avatar_url") ?: json.optJSONObject("data")?.optString("avatar") ?: ""
                            }
                        }
                        if (newAvatar.isNotBlank()) {
                            ConnectoApiClient.updateAvatarState(newAvatar, appContext)
                        }
                        val newDisplay = json.optString("display_name").ifEmpty {
                            json.optJSONObject("data")?.optString("display_name") ?: ""
                        }
                        if (newDisplay.isNotBlank()) {
                            ConnectoApiClient.currentUserDisplayName = newDisplay
                        }
                        Log.d(TAG, "Real-time user avatar/profile updated: avatar=$newAvatar, display=$newDisplay")
                    }
                }

                "presence_update" -> {
                    val pUser = json.optString("username").ifEmpty { json.optString("user_id") }
                    val isOnline = json.optBoolean("is_online", json.optString("status").equals("online", ignoreCase = true))
                    Log.d(TAG, "Presence update received: user=$pUser, is_online=$isOnline")
                    if (pUser.isNotBlank()) {
                        val clean = pUser.lowercase().removePrefix("@")
                        _userPresenceUpdates.tryEmit(clean to isOnline)
                        appContext?.let { ctx ->
                            try {
                                ConnectoDatabaseHelper.getInstance(ctx).updateFriendOnlineStatus(clean, isOnline)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating friend online status in db: ${e.message}", e)
                            }
                        }
                    }
                }

                // --- TYPING INDICATORS ---
                "typing_start", "typing:start" -> {
                    val chId = json.optString("channel_id").ifEmpty { "general" }
                    val username = json.optString("username")
                    val displayName = json.optString("display_name").ifEmpty { username }
                    if (username.isNotBlank()) {
                        _typingEvents.tryEmit(TypingEvent(username, displayName, chId, isTyping = true))
                        // Update legacy activeTypingUser for backward compat
                        ConnectoApiClient.activeTypingUser = displayName
                    }
                }

                "typing_stop", "typing:stop" -> {
                    val chId = json.optString("channel_id").ifEmpty { "general" }
                    val username = json.optString("username")
                    if (username.isNotBlank()) {
                        _typingEvents.tryEmit(TypingEvent(username, username, chId, isTyping = false))
                        if (ConnectoApiClient.activeTypingUser == username) {
                            ConnectoApiClient.activeTypingUser = null
                        }
                    }
                }

                // --- @MENTION NOTIFICATIONS ---
                "mention" -> {
                    val chId = json.optString("channel_id").ifEmpty { "" }
                    val chName = json.optString("channel_name").ifEmpty { chId }
                    val fromUser = json.optString("from_user").ifEmpty { json.optString("from_display_name") }
                    val msgId = json.optString("message_id").ifEmpty { "" }
                    val content = json.optString("content").ifEmpty { "" }
                    if (fromUser.isNotBlank()) {
                        _mentionEvents.tryEmit(MentionEvent(fromUser, chId, chName, msgId, content))
                        Log.d(TAG, "Mention received from $fromUser in #$chName")
                    }
                }

                // --- READ RECEIPTS ---
                "message_read" -> {
                    val msgId = json.optString("message_id").ifEmpty { "" }
                    val chId = json.optString("channel_id").ifEmpty { "" }
                    val readerUsername = json.optString("reader_username").ifEmpty { "" }
                    if (msgId.isNotBlank()) {
                        _readReceiptEvents.tryEmit(ReadReceiptEvent(msgId, chId, readerUsername))
                    }
                }


                "incoming_call", "call:incoming" -> {
                    val roomId = json.optString("room_id").ifEmpty { json.optString("call_id") }
                    val callerId = json.optString("caller_id")
                    val callerUsername = json.optString("caller_username").ifEmpty { json.optString("caller") }
                    val callerName = json.optString("caller_name").ifEmpty { json.optString("caller").ifEmpty { callerUsername } }
                    val callerAvatar = json.optString("caller_avatar").ifEmpty { json.optString("avatar") }

                    Log.d(TAG, "Incoming call received from $callerName ($roomId)")
                    _incomingCall.value = IncomingCallData(
                        roomId = roomId,
                        callerId = callerId,
                        callerUsername = callerUsername,
                        callerName = callerName,
                        callerAvatar = callerAvatar
                    )
                    _callState.value = CallState.INCOMING_RINGING
                    ConnectoNotificationManager.showIncomingCallNotification(
                        callerName = callerName,
                        roomId = roomId,
                        callerUsername = callerUsername,
                        callerAvatar = callerAvatar
                    )
                }

                "call_ringing", "call:ringing" -> {
                    Log.d(TAG, "Call ringing on remote party")
                    _callState.value = CallState.OUTGOING_RINGING
                }

                "call_connected", "call:accepted", "call:connected" -> {
                    val roomId = json.optString("room_id").ifEmpty { json.optString("call_id") }
                    Log.d(TAG, "Call connected! Room: $roomId")
                    _callState.value = CallState.CONNECTED
                    _incomingCall.value = null
                    _participantCount.value = 2
                    appContext?.let {
                        VoiceCallForegroundService.startService(it, _activeCallTitle.value, _activeCallPartner.value, _isMuted.value)
                    }
                    // Ensure this peer is in the server's voice_rooms map so audio relay works.
                    // Both the caller (who gets call_connected after callee accepts) and the
                    // callee (who may also get this event) must be in voice_rooms[roomId].
                    if (roomId.isNotBlank()) {
                        val vrJoin = JSONObject().apply {
                            put("type", "voice_join")
                            put("action", "voice:join")
                            put("channel_id", roomId)
                            put("room_id", roomId)
                        }
                        sendWsMessage(vrJoin)
                        _activeRoomId.value = roomId
                    }
                    startAudioHardware()
                    startDurationTimer()

                    // Initiate WebRTC offer to callee if caller
                    val targetUser = json.optString("target").ifEmpty {
                        json.optString("target_user").ifEmpty {
                            json.optString("callee").ifEmpty {
                                json.optString("callee_username").ifEmpty {
                                    json.optString("username").ifEmpty {
                                        _activeCallPartner.value ?: ""
                                    }
                                }
                            }
                        }
                    }
                    if (isCallCaller && targetUser.isNotBlank()) {
                        initiateWebRtcOffer(targetUser)
                    }
                }

                "call_rejected", "call:declined", "call:rejected" -> {
                    Log.d(TAG, "Call rejected or declined by remote party")
                    endCallLocally()
                }

                "call_ended", "call:ended" -> {
                    Log.d(TAG, "Call ended by peer")
                    endCallLocally()
                }

                "call:error" -> {
                    val errMsg = json.optString("message", "Call error")
                    Log.e(TAG, "Call error from server: $errMsg")
                    endCallLocally()
                }

                "voice_data" -> {
                    val base64Audio = json.optString("audio")
                    if (base64Audio.isNotEmpty() && _callState.value == CallState.CONNECTED) {
                        playAudioChunk(base64Audio)
                    }
                }

                "voice_room_joined", "voice:user_joined" -> {
                    val roomId = json.optString("room_id").ifEmpty { json.optString("channel_id") }
                    val existingPeers = json.optJSONArray("existing_peer_ids") ?: json.optJSONArray("participants")
                    val count = maxOf((existingPeers?.length() ?: 0) + 1, _participantCount.value + 1)
                    _participantCount.value = count
                    Log.d(TAG, "Joined voice room $roomId with $count peers")

                    val is1on1 = _activeCallPartner.value != null
                    // If we are caller in 1:1 call and still waiting for callee to answer,
                    // stay in OUTGOING_RINGING until callee accepts.
                    if (is1on1 && isCallCaller && _callState.value == CallState.OUTGOING_RINGING) {
                        Log.d(TAG, "Caller joined voice room for 1:1 call; staying in OUTGOING_RINGING until callee accepts")
                    } else {
                        _callState.value = CallState.CONNECTED
                        startAudioHardware()
                        startDurationTimer()

                        // WebRTC: Initiate offer to each existing peer in room (only if caller in 1:1 to prevent glare)
                        if (existingPeers != null && (!is1on1 || isCallCaller)) {
                            for (i in 0 until existingPeers.length()) {
                                val peerId = existingPeers.optString(i)
                                if (peerId.isNotBlank()) {
                                    initiateWebRtcOffer(peerId)
                                }
                            }
                        }
                    }
                }

                "voice_peer_joined" -> {
                    val peerId = json.optString("peer_id")
                    _participantCount.value += 1
                    Log.d(TAG, "WebRTC voice peer joined: $peerId")
                    if (peerId.isNotBlank()) {
                        getOrCreatePeerConnection(peerId)
                    }
                }

                "voice_peer_left", "voice:user_left" -> {
                    val peerId = json.optString("peer_id")
                    if (peerId.isNotBlank()) {
                        synchronized(webRtcTeardownLock) {
                            val pc = peerConnections.remove(peerId)
                            if (pc != null) {
                                // Remove any alias keys pointing to the same PeerConnection instance
                                val aliasKeys = peerConnections.entries.filter { it.value === pc }.map { it.key }
                                aliasKeys.forEach { peerConnections.remove(it) }

                                if (!disposedPcs.contains(pc)) {
                                    disposedPcs.add(pc)
                                    try {
                                        for (sender in pc.senders) {
                                            try { pc.removeTrack(sender) } catch (_: Throwable) {}
                                        }
                                        pc.close()
                                        pc.dispose()
                                        Log.d(TAG, "Disposed PeerConnection for left peer: $peerId")
                                    } catch (e: Throwable) {
                                        Log.e(TAG, "Error disposing left peer PC: ${e.message}")
                                    }
                                }
                            }
                        }
                    }
                    if (_participantCount.value > 1) {
                        _participantCount.value -= 1
                    }
                }

                "voice_offer", "call:offer" -> {
                    val senderId = json.optString("sender_user_id").ifEmpty {
                        json.optString("sender_username").ifEmpty {
                            json.optString("caller")
                        }
                    }
                    val sdp = if (json.optJSONObject("sdp") != null) {
                        json.getJSONObject("sdp").optString("sdp")
                    } else {
                        json.optString("sdp").ifEmpty { json.optString("sdp_str") }
                    }
                    if (senderId.isNotBlank() && sdp.isNotBlank()) {
                        Log.d(TAG, "Received WebRTC voice_offer/call:offer from $senderId")
                        handleWebRtcOffer(senderId, sdp)
                    }
                }

                "voice_answer", "call:answer" -> {
                    val senderUsername = json.optString("sender_username")
                    val senderId = json.optString("sender_user_id").ifEmpty {
                        senderUsername.ifEmpty {
                            json.optString("caller")
                        }
                    }
                    val sdp = if (json.optJSONObject("sdp") != null) {
                        json.getJSONObject("sdp").optString("sdp")
                    } else {
                        json.optString("sdp").ifEmpty { json.optString("sdp_str") }
                    }
                    if (senderId.isNotBlank() && sdp.isNotBlank()) {
                        Log.d(TAG, "Received WebRTC voice_answer/call:answer from $senderId (username=$senderUsername)")
                        handleWebRtcAnswer(senderId, sdp, senderUsername.ifEmpty { null })
                    }
                }

                "voice_ice_candidate", "call:ice-candidate" -> {
                    val senderUsername = json.optString("sender_username")
                    val senderId = json.optString("sender_user_id").ifEmpty {
                        senderUsername.ifEmpty {
                            json.optString("caller")
                        }
                    }
                    val candidate = json.optJSONObject("candidate") ?: run {
                        val candStr = json.optString("candidate")
                        if (candStr.isNotBlank()) {
                            try { JSONObject(candStr) } catch (_: Exception) { null }
                        } else null
                    }
                    if (senderId.isNotBlank() && candidate != null) {
                        Log.d(TAG, "Received WebRTC ICE candidate from $senderId (username=$senderUsername)")
                        handleWebRtcIceCandidate(senderId, candidate, senderUsername.ifEmpty { null })
                    }
                }

                "friend_request_received" -> {
                    val dataObj = json.optJSONObject("data")
                    val senderName = dataObj?.optString("sender_username")?.ifEmpty { dataObj.optString("sender") } ?: "Someone"
                    val reqId = dataObj?.optString("id") ?: ""
                    val senderAvatar = dataObj?.optString("sender_avatar")?.ifEmpty { dataObj.optString("avatar_url") }?.ifEmpty { dataObj.optString("avatar") }
                    Log.i(TAG, "[WS_FRIEND] friend_request_received from $senderName (reqId=$reqId, avatar=$senderAvatar)")
                    ConnectoNotificationManager.showFriendRequestNotification(
                        senderName = senderName,
                        requestId = reqId,
                        senderAvatar = senderAvatar
                    )
                    com.example.connecto.ui.components.InAppNotificationController.show(
                        com.example.connecto.ui.components.InAppNotificationData(
                            title = "New Friend Request",
                            message = "$senderName sent you a friend request",
                            avatarUrl = senderAvatar,
                            username = senderName,
                            actionType = "notifications"
                        )
                    )
                    appContext?.let { ctx ->
                        val intent = Intent("com.connecto.app.FRIEND_REQUEST_UPDATED").apply {
                            putExtra("sender", senderName)
                            putExtra("request_id", reqId)
                            setPackage(ctx.packageName)
                        }
                        ctx.sendBroadcast(intent)
                    }
                }

                "friend_accepted", "friend_request_accepted", "friend_updated" -> {
                    val dataObj = json.optJSONObject("data")
                    val friendUser = dataObj?.optString("friend_username")?.ifEmpty { dataObj.optString("sender") } ?: ""
                    val friendDisplay = dataObj?.optString("friend_display_name")?.ifEmpty { friendUser } ?: ""
                    val friendAvatar = dataObj?.optString("friend_avatar")?.ifEmpty { dataObj.optString("avatar_url") }?.ifEmpty { dataObj.optString("avatar") }
                    Log.i(TAG, "[WS_FRIEND] friend_accepted with $friendUser")
                    if (friendUser.isNotEmpty()) {
                        appContext?.let { ctx ->
                            ConnectoNotificationManager.showMessageNotification(
                                senderName = "🤝 Alliance Forged",
                                messageText = "You and @$friendDisplay are now friends!",
                                channelOrDmId = "dm",
                                senderAvatar = friendAvatar
                            )
                            com.example.connecto.ui.components.InAppNotificationController.show(
                                com.example.connecto.ui.components.InAppNotificationData(
                                    title = "🤝 Alliance Forged",
                                    message = "You and @$friendDisplay are now friends!",
                                    avatarUrl = friendAvatar,
                                    username = friendUser,
                                    actionType = "chat",
                                    referenceId = friendUser
                                )
                            )
                            val intent = Intent("com.connecto.app.FRIEND_REQUEST_UPDATED").apply {
                                putExtra("friend_username", friendUser)
                                putExtra("action", "accepted")
                                setPackage(ctx.packageName)
                            }
                            ctx.sendBroadcast(intent)
                        }
                    }
                }

                "friend_removed" -> {
                    val friendUser = json.optString("friend_username")
                    if (friendUser.isNotEmpty()) {
                        appContext?.let { ctx ->
                            ConnectoDatabaseHelper.getInstance(ctx).deleteFriend(friendUser)
                            val intent = Intent("com.connecto.app.FRIEND_REQUEST_UPDATED").apply {
                                putExtra("friend_username", friendUser)
                                putExtra("action", "removed")
                                setPackage(ctx.packageName)
                            }
                            ctx.sendBroadcast(intent)
                        }
                    }
                }

                "notification_received" -> {
                    val notifObj = json.optJSONObject("notification")
                    if (notifObj != null) {
                        val nType = notifObj.optString("type")
                        val nTitle = notifObj.optString("title")
                        val nContent = notifObj.optString("content")
                        val nSender = notifObj.optString("sender_username")
                        val nAvatar = notifObj.optString("sender_avatar").ifEmpty { notifObj.optString("avatar_url") }.ifEmpty { notifObj.optString("avatar") }
                        val nRefId = notifObj.optString("reference_id")
                        val nId = notifObj.optString("id")

                        // Update in-memory unread count
                        scope.launch {
                            ConnectoApiClient.getUnreadNotificationCount()
                        }

                        // Display appropriate Android system notification & in-app banner with profile avatar
                        when (nType) {
                            "friend_request" -> {
                                ConnectoNotificationManager.showFriendRequestNotification(
                                    senderName = nSender.ifEmpty { "Gamer" },
                                    requestId = nId.ifEmpty { nRefId },
                                    senderAvatar = nAvatar
                                )
                                com.example.connecto.ui.components.InAppNotificationController.show(
                                    com.example.connecto.ui.components.InAppNotificationData(
                                        title = "New Friend Request",
                                        message = "${nSender.ifEmpty { "A gamer" }} sent you a friend request.",
                                        avatarUrl = nAvatar,
                                        username = nSender,
                                        actionType = "notifications"
                                    )
                                )
                                appContext?.let { ctx ->
                                    val intent = Intent("com.connecto.app.FRIEND_REQUEST_UPDATED").apply {
                                        putExtra("sender", nSender)
                                        putExtra("type", "friend_request")
                                        setPackage(ctx.packageName)
                                    }
                                    ctx.sendBroadcast(intent)
                                }
                            }
                            "friend_accepted" -> {
                                ConnectoNotificationManager.showMessageNotification(
                                    senderName = nTitle.ifEmpty { "🤝 Alliance Forged" },
                                    messageText = nContent,
                                    channelOrDmId = "dm",
                                    senderAvatar = nAvatar
                                )
                                com.example.connecto.ui.components.InAppNotificationController.show(
                                    com.example.connecto.ui.components.InAppNotificationData(
                                        title = nTitle.ifEmpty { "🤝 Alliance Forged" },
                                        message = nContent,
                                        avatarUrl = nAvatar,
                                        username = nSender,
                                        actionType = "chat",
                                        referenceId = nRefId
                                    )
                                )
                                appContext?.let { ctx ->
                                    val intent = Intent("com.connecto.app.FRIEND_REQUEST_UPDATED").apply {
                                        putExtra("sender", nSender)
                                        putExtra("type", "friend_accepted")
                                        setPackage(ctx.packageName)
                                    }
                                    ctx.sendBroadcast(intent)
                                }
                            }
                            "message" -> {
                                ConnectoNotificationManager.showMessageNotification(
                                    senderName = nSender.ifEmpty { nTitle },
                                    messageText = nContent,
                                    channelOrDmId = nRefId,
                                    senderAvatar = nAvatar
                                )
                                com.example.connecto.ui.components.InAppNotificationController.show(
                                    com.example.connecto.ui.components.InAppNotificationData(
                                        title = nSender.ifEmpty { nTitle },
                                        message = nContent,
                                        avatarUrl = nAvatar,
                                        username = nSender,
                                        actionType = "chat",
                                        referenceId = nRefId
                                    )
                                )
                            }
                            "call", "call_invite" -> {
                                ConnectoNotificationManager.showIncomingCallNotification(
                                    callerName = nSender.ifEmpty { "Gamer" },
                                    roomId = nRefId,
                                    callerUsername = nSender.ifEmpty { "Gamer" },
                                    callerAvatar = nAvatar
                                )
                            }
                            else -> {
                                ConnectoNotificationManager.showMessageNotification(
                                    senderName = nTitle.ifEmpty { "Connecto" },
                                    messageText = nContent,
                                    channelOrDmId = nRefId,
                                    senderAvatar = nAvatar
                                )
                                com.example.connecto.ui.components.InAppNotificationController.show(
                                    com.example.connecto.ui.components.InAppNotificationData(
                                        title = nTitle.ifEmpty { "Connecto" },
                                        message = nContent,
                                        avatarUrl = nAvatar,
                                        username = nSender,
                                        actionType = "chat",
                                        referenceId = nRefId
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling WS message: ${e.message}", e)
        }
    }

    // ----------------------------------------------------
    // Public Call Controls
    // ----------------------------------------------------

    fun start1on1Call(targetUsername: String, targetDisplayName: String, avatarUrl: String? = null) {
        val cleanTarget = targetUsername.trim()
            .removePrefix("@")
            .removePrefix("1:1 Voice Call with ")
            .removePrefix("Voice Call with ")
            .removePrefix("Call with ")
            .trim()
        val cleanDisplayName = targetDisplayName.trim()
            .removePrefix("1:1 Voice Call with ")
            .removePrefix("Voice Call with ")
            .removePrefix("Call with ")
            .trim()
            .ifEmpty { cleanTarget }

        val currentUsername = ConnectoApiClient.currentUsername?.ifEmpty { "User" } ?: "User"
        val roomId = "call_${currentUsername}_${System.currentTimeMillis()}"

        isCallCaller = true
        _activeRoomId.value = roomId
        _activeCallPartner.value = cleanDisplayName
        _activeCallTitle.value = "1:1 Voice Call with $cleanDisplayName"
        _activeCallAvatar.value = avatarUrl
        _participantCount.value = 2

        // If user is calling themselves (Echo / Loopback audio test call)
        if (cleanTarget.equals(currentUsername, ignoreCase = true) ||
            cleanTarget.equals("echo", ignoreCase = true) ||
            cleanTarget.equals("self", ignoreCase = true)) {
            Log.d(TAG, "Initiating Echo / Loopback audio test call for user: $currentUsername")
            _activeCallTitle.value = "Echo Audio Test (Self)"
            _callState.value = CallState.CONNECTED
            startAudioHardware()
            startDurationTimer()
            return
        }

        _callState.value = CallState.OUTGOING_RINGING
        appContext?.let {
            VoiceCallForegroundService.startService(it, _activeCallTitle.value, cleanDisplayName, _isMuted.value)
        }

        // Send call invite — production WS reads "type" field, must be "call:initiate"
        val inviteMsg = JSONObject().apply {
            put("action", "call:initiate")
            put("type", "call:initiate")   // FIX: was "call_invite" — production ignores that
            put("target", cleanTarget)
            put("target_user", cleanTarget)
            put("call_id", roomId)
            put("room_id", roomId)
            put("avatar", "👤")
        }
        sendWsMessage(inviteMsg)

        // Caller must also join the voice room so the server's in-memory voice_rooms map
        // includes the caller — otherwise voice_data broadcast won't reach them.
        val callerJoinMsg = JSONObject().apply {
            put("type", "voice_join")
            put("action", "voice:join")
            put("channel_id", roomId)
            put("room_id", roomId)
        }
        sendWsMessage(callerJoinMsg)

        // Fallback REST call
        scope.launch {
            try {
                ConnectoApiClient.inviteToVoiceCall(cleanTarget, roomId)
            } catch (_: Throwable) {}
        }
    }

    fun acceptIncomingCall() {
        val call = _incomingCall.value ?: return
        ConnectoNotificationManager.cancelCallNotification()
        isCallCaller = false
        _activeRoomId.value = call.roomId
        _activeCallPartner.value = call.callerName
        _activeCallTitle.value = "1:1 Voice Call with ${call.callerName}"
        _activeCallAvatar.value = call.callerAvatar
        _callState.value = CallState.CONNECTED
        _participantCount.value = 2
        appContext?.let {
            VoiceCallForegroundService.startService(it, _activeCallTitle.value, call.callerName, _isMuted.value)
        }

        val acceptMsg = JSONObject().apply {
            put("action", "call:accept")
            put("type", "call:accept")   // FIX: was "call_accept" — production reads type field
            put("target", call.callerUsername)
            put("call_id", call.roomId)
            put("room_id", call.roomId)
        }
        sendWsMessage(acceptMsg)

        // Callee must also explicitly join the voice room on the server so that
        // voice_data packets broadcast to voice_rooms[roomId] will include this user.
        val calleeJoinMsg = JSONObject().apply {
            put("type", "voice_join")
            put("action", "voice:join")
            put("channel_id", call.roomId)
            put("room_id", call.roomId)
        }
        sendWsMessage(calleeJoinMsg)

        scope.launch {
            ConnectoApiClient.respondToVoiceCall(call.roomId, "accept")
        }

        startAudioHardware()
        startDurationTimer()
    }

    fun declineIncomingCall() {
        val call = _incomingCall.value ?: return
        ConnectoNotificationManager.cancelCallNotification()
        val declineMsg = JSONObject().apply {
            put("action", "call:decline")
            put("type", "call:decline")   // FIX: was "call_decline"
            put("target", call.callerUsername)
            put("call_id", call.roomId)
            put("room_id", call.roomId)
            put("reason", "declined")
        }
        sendWsMessage(declineMsg)

        scope.launch {
            ConnectoApiClient.respondToVoiceCall(call.roomId, "decline")
        }

        _incomingCall.value = null
        _callState.value = CallState.IDLE
    }

    fun joinVoiceRoom(roomId: String, roomName: String, roomCode: String? = null) {
        _activeRoomId.value = roomId
        _activeCallTitle.value = roomName
        _activeCallPartner.value = null
        _activeCallRoomCode.value = roomCode
        _callState.value = CallState.CONNECTED
        appContext?.let {
            VoiceCallForegroundService.startService(it, roomName, null, _isMuted.value)
        }

        // Subscribe to voice room text channel
        val subAction = JSONObject().apply {
            put("action", "subscribe")
            put("channel_id", "voice_room:$roomId")
        }
        sendWsMessage(subAction)

        // Send voice_join BEFORE starting audio so the server adds us to voice_rooms[roomId]
        // first. Audio relay (broadcast_to_voice_room) requires both users to be in that set.
        val joinMsg = JSONObject().apply {
            put("action", "voice:join")
            put("type", "voice_join")
            put("channel_id", roomId)
            put("room_id", roomId)
        }
        sendWsMessage(joinMsg)

        // Fallback REST join — some production servers track voice room participants via REST
        scope.launch {
            ConnectoApiClient.joinVoiceRoom(roomId, ConnectoApiClient.currentUsername ?: "Gamer")
        }

        // Start audio now — voice_room_joined WS event will also trigger startAudioHardware()
        // if the server responds; if it doesn't (production may be silent), we still start here.
        startAudioHardware()
        startDurationTimer()
    }

    fun leaveVoiceRoom() {
        val roomId = _activeRoomId.value
        if (!roomId.isNullOrBlank()) {
            val leaveMsg = JSONObject().apply {
                put("action", "voice:leave")
                put("type", "voice_leave")
                put("channel_id", roomId)
                put("room_id", roomId)
            }
            sendWsMessage(leaveMsg)

            scope.launch {
                ConnectoApiClient.leaveVoiceRoom(roomId, ConnectoApiClient.currentUsername ?: "Gamer")
            }
        }
        endCallLocally()
    }

    fun endCall() {
        val roomId = _activeRoomId.value
        val partner = _activeCallPartner.value
        if (!roomId.isNullOrBlank()) {
            val endMsg = JSONObject().apply {
                put("action", "call:end")
                put("type", "call:end")   // FIX: was "call_end"
                if (!partner.isNullOrBlank()) {
                    put("target", partner.trim().removePrefix("@"))
                }
                put("call_id", roomId)
                put("room_id", roomId)
            }
            sendWsMessage(endMsg)

            // Also send voice_leave to remove us from server's voice_rooms map
            val leaveMsg = JSONObject().apply {
                put("type", "voice_leave")
                put("action", "voice:leave")
                put("channel_id", roomId)
                put("room_id", roomId)
            }
            sendWsMessage(leaveMsg)

            scope.launch {
                ConnectoApiClient.endVoiceCall(roomId)
            }
        }

        // Record persistent call log
        val partnerTitle = _activeCallPartner.value ?: _activeCallTitle.value
        val duration = _callDurationSeconds.value
        scope.launch {
            ConnectoApiClient.recordCallLog(
                callerName = partnerTitle,
                callType = "Voice Call",
                durationSeconds = duration,
                isMissed = false,
                isOutgoing = true,
                roomName = _activeCallTitle.value,
                roomCode = _activeCallRoomCode.value ?: ""
            )
        }

        endCallLocally()
    }

    fun restartAudioHardwareIfConnected() {
        if (_callState.value == CallState.CONNECTED) {
            val context = appContext ?: return
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "RECORD_AUDIO permission active, ensuring WebRTC and audio hardware are running")
                startLocalAudio()
                startAudioHardware()
            }
        }
    }

    private fun endCallLocally() {
        synchronized(callTeardownLock) {
            if (_callState.value == CallState.IDLE && _activeRoomId.value == null && _activeCallPartner.value == null && _incomingCall.value == null) {
                Log.d(TAG, "endCallLocally: Already idle, skipping redundant teardown")
                return
            }
            Log.d(TAG, "endCallLocally: Tearing down call session")
            ConnectoNotificationManager.cancelCallNotification()
            appContext?.let {
                VoiceCallForegroundService.stopService(it)
            }
            stopWebRtc()
            stopAudioHardware()
            durationJob?.cancel()
            durationJob = null
            _callDurationSeconds.value = 0
            _callState.value = CallState.IDLE
            isCallCaller = false
            _activeRoomId.value = null
            _activeCallPartner.value = null
            _activeCallAvatar.value = null
            _activeCallRoomCode.value = null
            _incomingCall.value = null
            _participantCount.value = 1
            _liveAudioAmplitude.value = 0f
        }
    }

    fun toggleMute() {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        localAudioTrack?.setEnabled(!newMuted)
        Log.d(TAG, "Mute toggled: isMuted=$newMuted")
        appContext?.let {
            VoiceCallForegroundService.updateMute(it, newMuted)
        }
    }

    fun toggleSpeaker() {
        val newSpeaker = !_isSpeakerOn.value
        _isSpeakerOn.value = newSpeaker
        applySpeakerphoneSetting(newSpeaker)
    }

    private fun applySpeakerphoneSetting(isSpeaker: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val devices = audioManager?.availableCommunicationDevices ?: emptyList()
                val targetType = if (isSpeaker) AudioDeviceInfo.TYPE_BUILTIN_SPEAKER else AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                val targetDevice = devices.firstOrNull { it.type == targetType }
                if (targetDevice != null) {
                    audioManager?.setCommunicationDevice(targetDevice)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager?.isSpeakerphoneOn = isSpeaker
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.isSpeakerphoneOn = isSpeaker
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed setting speakerphone: ${e.message}")
        }
    }

    fun applyAudioSettingsForCall() {
        try {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            applySpeakerphoneSetting(_isSpeakerOn.value)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting AudioManager mode: ${e.message}")
        }
    }

    // ----------------------------------------------------
    // Audio Hardware Engine (AudioRecord & AudioTrack)
    // ----------------------------------------------------

    private fun startAudioHardware() {
        applyAudioSettingsForCall()
        val context = appContext ?: return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot start audio capture: RECORD_AUDIO permission not granted")
            return
        }

        // Ensure WebRTC local audio track is created
        try {
            startLocalAudio()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to start local WebRTC audio: ${t.message}", t)
        }

        synchronized(audioHardwareLock) {
            // Always initialize AudioTrack so incoming voice_data packets can play immediately
            initAudioTrack()

            // CRITICAL BUG FIX FOR OLDER ANDROID (API 24-29):
            // When WebRTC's localAudioTrack is active, WebRTC's JavaAudioDeviceModule has
            // exclusive ownership of the microphone. Opening a second concurrent AudioRecord
            // in startAudioRecordLoop() will fail or cause the HAL to cancel the recording,
            // which in turn triggers a fatal WebRTC AssertionError (assertTrue(!list.isEmpty()))
            // that bypasses standard Exception catches and force quits the application.
            if (localAudioTrack != null) {
                Log.d(TAG, "WebRTC audio track is active; exclusive mic ownership delegated to WebRTC.")
                return
            }

            if (isRecordingActive && audioRecord != null && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                Log.d(TAG, "Audio hardware is already actively running, ignoring duplicate start")
                return
            }

            startAudioRecordLoop()
        }
    }

    private fun stopAudioHardware() {
        val jobToCancel: Job?
        val recordToRelease: AudioRecord?
        val trackToRelease: AudioTrack?

        synchronized(audioHardwareLock) {
            isRecordingActive = false
            jobToCancel = recordingJob
            recordingJob = null
            recordToRelease = audioRecord
            audioRecord = null
            trackToRelease = audioTrack
            audioTrack = null
        }

        scope.launch(Dispatchers.IO) {
            try {
                if (recordToRelease?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    recordToRelease.stop()
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error stopping AudioRecord: ${t.message}")
            }

            try {
                jobToCancel?.cancelAndJoin()
            } catch (t: Throwable) {
                Log.w(TAG, "Error joining recording job: ${t.message}")
            }

            try {
                recordToRelease?.release()
            } catch (t: Throwable) {
                Log.e(TAG, "Error releasing AudioRecord: ${t.message}")
            }

            try {
                if (trackToRelease?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    trackToRelease.stop()
                }
                trackToRelease?.release()
            } catch (t: Throwable) {
                Log.e(TAG, "Error releasing AudioTrack: ${t.message}")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                audioManager?.clearCommunicationDevice()
            } catch (t: Throwable) {
                Log.e(TAG, "Error clearing communication device: ${t.message}")
            }
        }

        try {
            audioManager?.mode = AudioManager.MODE_NORMAL
        } catch (t: Throwable) {
            Log.e(TAG, "Error resetting AudioManager mode: ${t.message}")
        }
    }

    private fun initAudioTrack() {
        try {
            if (audioTrack != null && audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                if (audioTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack?.play()
                }
                return
            }
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufSize = maxOf(minBuf * 2, CHUNK_SIZE_BYTES * 4)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            Log.d(TAG, "AudioTrack initialized and playing")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed initializing AudioTrack: ${t.message}", t)
        }
    }

    private fun playAudioChunk(base64Audio: String) {
        try {
            val bytes = Base64.decode(base64Audio, Base64.NO_WRAP)
            if (bytes.isNotEmpty() && audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack?.write(bytes, 0, bytes.size, AudioTrack.WRITE_NON_BLOCKING)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error playing audio chunk: ${t.message}")
        }
    }

    private fun startAudioRecordLoop() {
        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) {
            Log.e(TAG, "Invalid AudioRecord minBufferSize: $minBuf")
            return
        }
        val bufSize = maxOf(minBuf * 2, CHUNK_SIZE_BYTES * 4)

        try {
            val record = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize
            )

            val activeRecord = if (record.state == AudioRecord.STATE_INITIALIZED) {
                record
            } else {
                Log.w(TAG, "VOICE_COMMUNICATION failed, falling back to MIC audio source")
                try { record.release() } catch (_: Throwable) {}
                val fallbackRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufSize
                )
                if (fallbackRecord.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord initialization failed on all audio sources")
                    try { fallbackRecord.release() } catch (_: Throwable) {}
                    return
                }
                fallbackRecord
            }

            activeRecord.startRecording()
            audioRecord = activeRecord
            isRecordingActive = true
            Log.d(TAG, "AudioRecord started recording")

            recordingJob = scope.launch(Dispatchers.IO) {
                val buffer = ByteArray(CHUNK_SIZE_BYTES)
                try {
                    while (isActive && isRecordingActive && activeRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        val read = activeRecord.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            // Calculate RMS amplitude for UI audio visualizer
                            var sum = 0.0
                            val shortsCount = read / 2
                            for (i in 0 until shortsCount) {
                                val low = buffer[i * 2].toInt() and 0xFF
                                val high = buffer[i * 2 + 1].toInt()
                                val sample = (high shl 8) or low
                                sum += sample * sample
                            }
                            val rms = if (shortsCount > 0) Math.sqrt(sum / shortsCount) else 0.0
                            val rawAmp = (rms / 16000.0).toFloat()
                            val normalized = if (rawAmp.isNaN() || rawAmp.isInfinite()) 0f else rawAmp.coerceIn(0f, 1f)
                            _liveAudioAmplitude.value = normalized

                            // Transmit audio packet if not muted and in active call
                            val roomId = _activeRoomId.value
                            if (!_isMuted.value && _callState.value == CallState.CONNECTED) {
                                if (_activeCallTitle.value.contains("Echo", ignoreCase = true)) {
                                    audioTrack?.write(buffer, 0, read, AudioTrack.WRITE_NON_BLOCKING)
                                }
                                if (!roomId.isNullOrBlank()) {
                                    val base64Str = Base64.encodeToString(buffer, 0, read, Base64.NO_WRAP)
                                    val packet = JSONObject().apply {
                                        put("action", "voice_data")
                                        put("type", "voice_data")
                                        put("room_id", roomId)
                                        put("channel_id", roomId)
                                        put("audio", base64Str)
                                    }
                                    sendWsMessage(packet)
                                }
                            }
                        } else if (read == AudioRecord.ERROR_INVALID_OPERATION || read == AudioRecord.ERROR_BAD_VALUE) {
                            Log.w(TAG, "AudioRecord read returned error code ($read), terminating loop")
                            break
                        } else {
                            delay(20)
                        }
                    }
                } catch (t: Throwable) {
                    Log.e(TAG, "Exception in AudioRecord loop: ${t.message}")
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to start AudioRecord loop: ${t.message}", t)
        }
    }

    private fun startDurationTimer() {
        if (durationJob?.isActive == true) {
            return
        }
        durationJob?.cancel()
        _callDurationSeconds.value = 0
        durationJob = scope.launch {
            while (isActive && _callState.value == CallState.CONNECTED) {
                delay(1000L)
                _callDurationSeconds.value += 1
            }
        }
    }
}
