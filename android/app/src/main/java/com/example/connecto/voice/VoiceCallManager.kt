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
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
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
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.IdentityHashMap
import java.util.Locale
import java.util.TimeZone
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

enum class AudioRoute(val id: String, val displayName: String) {
    EARPIECE("earpiece", "Earpiece"),
    SPEAKER("speaker", "Speaker"),
    BLUETOOTH("bluetooth", "Bluetooth")
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

    private val _activeCallPartnerUsername = MutableStateFlow<String?>(null)
    val activeCallPartnerUsername: StateFlow<String?> = _activeCallPartnerUsername.asStateFlow()

    private val notifiedMessageIds = java.util.Collections.synchronizedSet(java.util.LinkedHashSet<String>())

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

    private val _currentAudioRoute = MutableStateFlow(AudioRoute.SPEAKER)
    val currentAudioRoute: StateFlow<AudioRoute> = _currentAudioRoute.asStateFlow()

    private val _isBluetoothConnected = MutableStateFlow(false)
    val isBluetoothConnected: StateFlow<Boolean> = _isBluetoothConnected.asStateFlow()

    private val _bluetoothDeviceName = MutableStateFlow<String?>("Bluetooth Audio")
    val bluetoothDeviceName: StateFlow<String?> = _bluetoothDeviceName.asStateFlow()

    private val _availableAudioRoutes = MutableStateFlow<List<AudioRoute>>(listOf(AudioRoute.SPEAKER, AudioRoute.EARPIECE))
    val availableAudioRoutes: StateFlow<List<AudioRoute>> = _availableAudioRoutes.asStateFlow()

    // Realtime Voice Isolation (Adaptive Noise Cancellation & Vocal EQ)
    private val _isVoiceIsolationEnabled = MutableStateFlow(true)
    val isVoiceIsolationEnabled: StateFlow<Boolean> = _isVoiceIsolationEnabled.asStateFlow()

    data class VoiceIsolationCapabilities(
        val hasHardwareNoiseSuppressor: Boolean = false,
        val hasHardwareEchoCanceler: Boolean = false,
        val hasHardwareGainControl: Boolean = false,
        val hasHardwareMicBeamforming: Boolean = false,
        val multiMicCount: Int = 1,
        val isHardwareSupported: Boolean = false,
        val statusSummary: String = "AI Studio Noise Filter"
    )

    private val _voiceIsolationCapabilities = MutableStateFlow(VoiceIsolationCapabilities())
    val voiceIsolationCapabilities: StateFlow<VoiceIsolationCapabilities> = _voiceIsolationCapabilities.asStateFlow()

    private var hardwareNoiseSuppressor: NoiseSuppressor? = null
    private var hardwareEchoCanceler: AcousticEchoCanceler? = null
    private var hardwareGainControl: AutomaticGainControl? = null

    private var audioDeviceCallback: android.media.AudioDeviceCallback? = null

    private val _liveAudioAmplitude = MutableStateFlow(0f)
    val liveAudioAmplitude: StateFlow<Float> = _liveAudioAmplitude.asStateFlow()

    @Volatile
    var activeChatRoomOrFriend: String? = null

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private var isCallCaller = false
    private val recordedCallSessions = java.util.Collections.synchronizedSet(mutableSetOf<String>())

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
    private val pendingWsMessages = java.util.concurrent.ConcurrentLinkedQueue<String>()

    // Real-time Channel Subscriptions & Incoming Message Bus
    private val subscribedChannels = java.util.concurrent.CopyOnWriteArraySet<String>()
    private val _incomingChannelMessages = MutableSharedFlow<MessageDto>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingChannelMessages: SharedFlow<MessageDto> = _incomingChannelMessages.asSharedFlow()

    fun handleIncomingPushMessage(msgDto: MessageDto) {
        val cleanSender = msgDto.authorId.trim().removePrefix("@").lowercase()
        val content = msgDto.content.trim()
        val dedupKey = if (msgDto.id.isNotBlank()) msgDto.id else "${cleanSender}_${content.hashCode()}"
        if (notifiedMessageIds.add(dedupKey)) {
            if (notifiedMessageIds.size > 500) {
                val first = notifiedMessageIds.firstOrNull()
                if (first != null) notifiedMessageIds.remove(first)
            }
        }
        _incomingChannelMessages.tryEmit(msgDto)
    }

    private val _incomingPollUpdates = MutableSharedFlow<PollDto>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingPollUpdates: SharedFlow<PollDto> = _incomingPollUpdates.asSharedFlow()

    private val _deletedMessageIds = MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val deletedMessageIds: SharedFlow<String> = _deletedMessageIds.asSharedFlow()

    data class MessageEditEvent(val messageId: String, val newContent: String)
    private val _editedMessages = MutableSharedFlow<MessageEditEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val editedMessages: SharedFlow<MessageEditEvent> = _editedMessages.asSharedFlow()

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
    fun sendTypingStart(channelId: String, recipientUsername: String? = null) {
        val clean = channelId.trim().lowercase().removePrefix("#")
        if (!isWsConnected || clean.isEmpty()) return
        // Stealth Mode check: Never broadcast typing if stealth mode is enabled
        if (ConnectoApiClient.isStealthMode) return
        val currentU = (ConnectoApiClient.currentUsername ?: "").trim().lowercase().removePrefix("@")
        appContext?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences("connecto_profile", android.content.Context.MODE_PRIVATE)
                if (currentU.isNotEmpty() && prefs.getBoolean("stealth_mode_$currentU", false)) {
                    return
                }
            } catch (_: Exception) {}
        }
        val targetU = recipientUsername?.trim()?.lowercase()?.removePrefix("@") ?: ""
        try {
            Log.d(TAG, "sendTypingStart: channel=$clean, recipient=$targetU, sender=$currentU")
            sendWsMessage(JSONObject().apply {
                put("type", "typing_start")
                put("action", "typing:start")
                put("channel_id", clean)
                put("channel", clean)
                if (currentU.isNotEmpty()) {
                    put("user", currentU)
                    put("username", currentU)
                }
                if (targetU.isNotEmpty()) {
                    put("target_user", targetU)
                    put("recipient", targetU)
                    put("target", targetU)
                }
            })
        } catch (_: Exception) {}
    }

    /** Send a typing_stop event to the server for the given channel. */
    fun sendTypingStop(channelId: String, recipientUsername: String? = null) {
        val clean = channelId.trim().lowercase().removePrefix("#")
        if (!isWsConnected || clean.isEmpty()) return
        val currentU = (ConnectoApiClient.currentUsername ?: "").trim().lowercase().removePrefix("@")
        val targetU = recipientUsername?.trim()?.lowercase()?.removePrefix("@") ?: ""
        try {
            Log.d(TAG, "sendTypingStop: channel=$clean, recipient=$targetU, sender=$currentU")
            sendWsMessage(JSONObject().apply {
                put("type", "typing_stop")
                put("action", "typing:stop")
                put("channel_id", clean)
                put("channel", clean)
                if (currentU.isNotEmpty()) {
                    put("user", currentU)
                    put("username", currentU)
                }
                if (targetU.isNotEmpty()) {
                    put("target_user", targetU)
                    put("recipient", targetU)
                    put("target", targetU)
                }
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
                            if (_isVoiceIsolationEnabled.value) {
                                applyVoiceIsolationDsp(data, data.size)
                            }
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
                mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation2", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl2", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression2", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googTypingNoiseDetection", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAudioMirroring", "false"))
                mandatory.add(MediaConstraints.KeyValuePair("echoCancellation", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("noiseSuppression", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("autoGainControl", "true"))
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
                    } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED ||
                        newState == PeerConnection.IceConnectionState.CLOSED ||
                        newState == PeerConnection.IceConnectionState.FAILED) {
                        val is1on1 = _activeCallPartner.value != null || _activeCallPartnerUsername.value != null || isCallCaller
                        if (is1on1 && _callState.value == CallState.CONNECTED) {
                            Log.d(TAG, "1:1 WebRTC ICE connection disconnected/closed ($newState). Terminating call session.")
                            endCallLocally()
                        }
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
        val savedVoiceIso = context.getSharedPreferences("connecto_audio_prefs", Context.MODE_PRIVATE)
            .getBoolean("connecto_voice_isolation_enabled", true)
        _isVoiceIsolationEnabled.value = savedVoiceIso
        detectVoiceIsolationCapabilities(context)
        applyHardwareVoiceIsolationParams(savedVoiceIso)
        initWebRtcFactory(context)
        initAudioDeviceCallbacks()
        connectWebSocket()
        startPeriodicPing()
    }

    fun updateCredentialsAndConnect(
        username: String?,
        userId: String?,
        token: String? = null,
        displayName: String? = null
    ) {
        if (!username.isNullOrBlank()) {
            ConnectoApiClient.currentUsername = username
        }
        if (!userId.isNullOrBlank()) {
            ConnectoApiClient.currentUserId = userId
        }
        if (!token.isNullOrBlank()) {
            ConnectoApiClient.sessionToken = token
        }
        if (!displayName.isNullOrBlank()) {
            ConnectoApiClient.currentUserDisplayName = displayName
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
    @Volatile
    private var isWsConnecting = false
    private val wsConnectLock = Any()

    private val recentlyTerminatedCalls = java.util.Collections.synchronizedMap(object : java.util.LinkedHashMap<String, Long>(50, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?): Boolean {
            return size > 100
        }
    })

    fun markCallTerminated(roomId: String?) {
        if (!roomId.isNullOrBlank()) {
            val clean = roomId.trim()
            recentlyTerminatedCalls[clean] = System.currentTimeMillis()
            Log.d(TAG, "Marked call as terminated: $clean")
        }
    }

    fun isCallTerminated(roomId: String?): Boolean {
        if (roomId.isNullOrBlank()) return false
        val clean = roomId.trim()
        val timestamp = recentlyTerminatedCalls[clean] ?: return false
        if (System.currentTimeMillis() - timestamp < 5 * 60 * 1000L) {
            return true
        }
        recentlyTerminatedCalls.remove(clean)
        return false
    }

    fun connectWebSocket(force: Boolean = false) {
        synchronized(wsConnectLock) {
            isExplicitlyDisconnected = false
            if (!force && (isWsConnected || isWsConnecting) && webSocket != null) return

            if (force) {
                try {
                    webSocket?.cancel()
                } catch (e: Exception) {
                    // ignore
                }
                webSocket = null
                isWsConnected = false
            }

            isWsConnecting = true

        val username = ConnectoApiClient.currentUsername?.ifEmpty { "" } ?: ""
        val cleanUser = username.trim().lowercase().removePrefix("@")
        val token = ConnectoApiClient.sessionToken ?: ""

        if (cleanUser.isBlank() || cleanUser == "user" || token.isBlank()) {
            Log.d(TAG, "connectWebSocket skipped: unauthenticated state (username='$cleanUser', hasToken=${token.isNotBlank()})")
            isWsConnecting = false
            return
        }

        val userId = ConnectoApiClient.currentUserId?.ifEmpty { cleanUser } ?: cleanUser

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
                    isWsConnecting = false
                    isWsConnected = true
                    _isWsConnectedFlow.value = true

                    val serverHdr = response.header("X-Connecto-Server")
                    val modeHdr = response.header("X-Connecto-Mode")
                    if (!serverHdr.isNullOrBlank()) {
                        ConnectoApiClient.updateServerType(serverHdr)
                    } else if (!modeHdr.isNullOrBlank()) {
                        val inferred = if (modeHdr.equals("local_server", ignoreCase = true)) "Tailscale" else "Cloudflare"
                        ConnectoApiClient.updateServerType(inferred)
                    }

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

                    // Drain and send all queued signaling messages immediately
                    while (true) {
                        val queued = pendingWsMessages.poll() ?: break
                        try {
                            val sent = ws.send(queued)
                            Log.d(TAG, "Flushed queued WS message on open (sent=$sent): $queued")
                        } catch (t: Throwable) {
                            Log.e(TAG, "Failed to send queued WS message: ${t.message}", t)
                        }
                    }
                }

                override fun onMessage(ws: WebSocket, text: String) {
                    handleWebSocketMessage(text)
                }

                override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "Voice WebSocket closing: $code / $reason")
                    isWsConnecting = false
                    isWsConnected = false
                    _isWsConnectedFlow.value = false
                }

                override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "Voice WebSocket closed: code=$code, reason=$reason")
                    isWsConnecting = false
                    isWsConnected = false
                    _isWsConnectedFlow.value = false
                    if ((code == 4001 || code == 1008 || reason.contains("Session terminated", ignoreCase = true) || reason.contains("Unauthorized", ignoreCase = true)) &&
                        !token.isNullOrBlank() && !ConnectoApiClient.sessionToken.isNullOrBlank()) {
                        Log.w(TAG, "Voice WebSocket permanently closed due to session revocation (code=$code, reason=$reason)")
                        ConnectoApiClient.triggerSessionExpired("Your session was logged out.")
                        return
                    }
                    if (code != 1000 && !isExplicitlyDisconnected) {
                        reconnectWebSocketAfterDelay()
                    }
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "Voice WebSocket failure: ${t.message}")
                    isWsConnecting = false
                    isWsConnected = false
                    _isWsConnectedFlow.value = false
                    val respCode = response?.code
                    if ((respCode == 401 || respCode == 4001 || respCode == 1008 || (response?.message?.contains("Session terminated", ignoreCase = true) == true)) &&
                        !token.isNullOrBlank() && !ConnectoApiClient.sessionToken.isNullOrBlank()) {
                        Log.w(TAG, "Voice WebSocket handshake rejected: HTTP $respCode Unauthorized")
                        ConnectoApiClient.triggerSessionExpired("Your session has expired or was terminated.")
                        return
                    }
                    if (!isExplicitlyDisconnected) {
                        reconnectWebSocketAfterDelay()
                    }
                }
            })
        }
    }

    fun disconnectWebSocket() {
        isExplicitlyDisconnected = true
        isWsConnecting = false
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

    fun sendWsMessage(json: JSONObject) {
        val msg = json.toString()
        if (isWsConnected && webSocket != null) {
            val sent = try { webSocket?.send(msg) ?: false } catch (_: Throwable) { false }
            if (sent) return
        }
        Log.w(TAG, "WS not ready or send failed; queueing message: $msg")
        if (pendingWsMessages.size < 100) {
            pendingWsMessages.offer(msg)
        }
        connectWebSocket()
    }

    fun inviteToVoiceGroup(roomId: String, groupTitle: String, targetUser: String) {
        val inviteMsg = JSONObject().apply {
            put("action", "call:initiate")
            put("type", "call:initiate")
            put("target", targetUser)
            put("target_user", targetUser)
            put("call_id", roomId)
            put("room_id", roomId)
            put("is_group", true)
            put("group_title", groupTitle)
            put("avatar", "🔊")
        }
        sendWsMessage(inviteMsg)
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
                "connected", "welcome", "init" -> {
                    val sType = json.optString("server_type").ifEmpty { json.optString("server").ifEmpty { json.optString("network") } }
                    val sMode = json.optString("mode")
                    val detected = if (sType.equals("tailscale", ignoreCase = true) || sMode.equals("local_server", ignoreCase = true)) "Tailscale" else "Cloudflare"
                    ConnectoApiClient.updateServerType(detected)
                }
                "session_terminated", "session_revoked", "force_logout" -> {
                    val initiatorHash = json.optString("initiator_token_hash").ifEmpty { json.optString("current_hash") }
                    val myHash = ConnectoApiClient.getSessionTokenHash()
                    val mySessionId = ConnectoApiClient.currentSessionId ?: ""
                    val targetSessionId = json.optString("session_id")
                    val termHashes = mutableListOf<String>()
                    val arr = json.optJSONArray("terminated_hashes")
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val h = arr.optString(i)
                            if (h.isNotBlank()) termHashes.add(h)
                        }
                    }
                    val singleHash = json.optString("session_token_hash")
                    if (singleHash.isNotBlank()) termHashes.add(singleHash)

                    val isTargetedByHash = myHash.isNotBlank() && termHashes.any { it.equals(myHash, ignoreCase = true) }
                    val isTargetedBySessionId = mySessionId.isNotBlank() && targetSessionId.isNotBlank() && mySessionId.equals(targetSessionId, ignoreCase = true)
                    val isTargetedByOtherSessionsRevocation = initiatorHash.isNotBlank() && myHash.isNotBlank() && !initiatorHash.equals(myHash, ignoreCase = true)
                    val isBroadcastLogout = initiatorHash.isBlank() && termHashes.isEmpty() && targetSessionId.isBlank()

                    val isTerminated = isTargetedByHash || isTargetedBySessionId || isTargetedByOtherSessionsRevocation || isBroadcastLogout

                    if (isTerminated) {
                        Log.w(TAG, "This session has been terminated by another device/session! Initiating sign out.")
                        disconnectWebSocket()
                        val reason = json.optString("message").ifEmpty { "Your session was logged out from another device." }
                        ConnectoApiClient.triggerSessionExpired(reason)
                    } else {
                        Log.d(TAG, "Received session_terminated event, but current session is the initiator or not targeted.")
                    }
                }
                "message_deleted", "delete_message" -> {
                    val data = json.optJSONObject("data") ?: json.optJSONObject("payload") ?: json
                    val msgId = data.optString("message_id").ifEmpty { json.optString("message_id").ifEmpty { data.optString("id") } }
                    if (msgId.isNotBlank()) {
                        appContext?.let { ctx ->
                            try {
                                ConnectoDatabaseHelper.getInstance(ctx).deleteMessage(msgId)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to delete message locally: ${e.message}")
                            }
                        }
                        _deletedMessageIds.tryEmit(msgId)
                        Log.d(TAG, "Real-time message deleted event received via WebSocket: $msgId")
                    }
                }
                "message_edited", "edit_message" -> {
                    val data = json.optJSONObject("data") ?: json.optJSONObject("payload") ?: json
                    val msgObj = data.optJSONObject("message") ?: data
                    val msgId = msgObj.optString("id").ifEmpty { json.optString("message_id") }
                    val newContent = msgObj.optString("content").ifEmpty { msgObj.optString("text") }
                    if (msgId.isNotBlank() && newContent.isNotBlank()) {
                        appContext?.let { ctx ->
                            try {
                                ConnectoDatabaseHelper.getInstance(ctx).updateMessageContent(msgId, newContent)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to update message locally: ${e.message}")
                            }
                        }
                        _editedMessages.tryEmit(MessageEditEvent(msgId, newContent))
                        Log.d(TAG, "Real-time message edited event received via WebSocket: $msgId -> $newContent")
                    }
                }
                "message_created", "new_message", "dm_message", "new_dm_alert", "channel_message", "chat_message", "message" -> {
                    val data = json.optJSONObject("data") ?: json.optJSONObject("message") ?: json.optJSONObject("payload") ?: json
                    val rawChId = json.optString("channel_id").ifEmpty { json.optString("channel").ifEmpty { data.optString("channel_id").ifEmpty { data.optString("channel") } } }
                    val rawChName = json.optString("channel_name").ifEmpty { data.optString("channel_name") }
                    if (rawChName.isNotBlank() && rawChId.isNotBlank()) {
                        ConnectoApiClient.channelCache[rawChName.lowercase()] = rawChId
                        ConnectoApiClient.channelCache[rawChId.lowercase()] = rawChName.lowercase()
                    }
                    val chId = if (rawChName.isNotBlank() && !rawChName.startsWith("dm-") && !rawChName.startsWith("dm_")) {
                        rawChName.lowercase()
                    } else if (rawChId.isNotBlank()) {
                        rawChId
                    } else {
                        rawChName.lowercase()
                    }
                    val senderUsername = data.optString("sender_username").ifEmpty { data.optString("user").ifEmpty { data.optString("sender").ifEmpty { json.optString("sender") } } }
                    val senderDisplay = data.optString("sender_display_name").ifEmpty { data.optString("nickname").ifEmpty { senderUsername } }
                    val content = data.optString("content").ifEmpty { data.optString("text") }
                    val msgId = data.optString("id").ifEmpty { json.optString("message_id") }
                    val createdAt = data.optString("created_at").ifEmpty { data.optString("timestamp") }
                    var authorAvatar = data.optString("avatar_url").ifEmpty { data.optString("sender_avatar_url").ifEmpty { data.optString("author_avatar").ifEmpty { data.optString("authorAvatar").ifEmpty { data.optString("avatar").ifEmpty { json.optString("sender_avatar").ifEmpty { null } } } } } }
                    val isAuthorAvatarEmoji = authorAvatar != null && !authorAvatar.contains("/") && !authorAvatar.contains(".") && !authorAvatar.startsWith("data:")
                    if (authorAvatar.isNullOrBlank() || isAuthorAvatarEmoji) {
                        val dbAvatar = appContext?.let { ctx ->
                            ConnectoDatabaseHelper.getInstance(ctx).getFriendByUsernameOrHandle(senderUsername)?.avatarUrl
                        }
                        if (!dbAvatar.isNullOrBlank() && (dbAvatar.contains("/") || dbAvatar.contains(".") || dbAvatar.startsWith("data:"))) {
                            authorAvatar = dbAvatar
                        }
                    }
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
                    val replyToId = if (data.has("reply_to_id") && !data.isNull("reply_to_id")) data.optString("reply_to_id") else null
                    val replyToContent = if (data.has("reply_to_content") && !data.isNull("reply_to_content")) data.optString("reply_to_content") else null
                    val replyToAuthor = if (data.has("reply_to_author") && !data.isNull("reply_to_author")) data.optString("reply_to_author") else null

                    if (chId.isNotBlank() && content.isNotBlank()) {
                        val msgDto = MessageDto(
                            id = msgId.ifEmpty { "ws_${System.currentTimeMillis()}" },
                            channelId = chId,
                            authorId = senderUsername,
                            authorName = senderDisplay,
                            content = content,
                            createdAt = createdAt.ifEmpty { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date()) },
                            authorAvatar = authorAvatar,
                            type = type,
                            pollId = pollId,
                            poll = poll,
                            timerSeconds = timerSeconds,
                            expiresAt = expiresAt,
                            replyToId = replyToId,
                            replyToContent = replyToContent,
                            replyToAuthor = replyToAuthor
                        )
                        appContext?.let { ctx ->
                            try {
                                ConnectoDatabaseHelper.getInstance(ctx).saveMessage(msgDto)
                                if (rawChId.isNotBlank() && rawChId != chId) {
                                    ConnectoDatabaseHelper.getInstance(ctx).saveMessage(msgDto.copy(channelId = rawChId))
                                }
                                if (rawChName.isNotBlank() && rawChName != chId && rawChName != rawChId) {
                                    ConnectoDatabaseHelper.getInstance(ctx).saveMessage(msgDto.copy(channelId = rawChName))
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to cache message locally: ${e.message}")
                            }
                        }
                        _incomingChannelMessages.tryEmit(msgDto)
                        Log.d(TAG, "Real-time message received via WebSocket for channel $chId: $content")
                    }

                    val myUser = (ConnectoApiClient.currentUsername ?: "").trim().removePrefix("@").lowercase()
                    val myDisplay = (ConnectoApiClient.currentUserDisplayName ?: "").trim().lowercase()
                    val myUserId = (ConnectoApiClient.currentUserId ?: "").trim().lowercase()

                    val cleanSender = senderUsername.trim().removePrefix("@").lowercase()
                    val cleanSenderId = (data.optString("sender_id").ifEmpty { data.optString("author_id").ifEmpty { json.optString("sender_id") } }).trim().lowercase()
                    val cleanSenderDisplay = senderDisplay.trim().removePrefix("@").lowercase()

                    val isFromMe = (myUser.isNotBlank() && (cleanSender == myUser || cleanSenderDisplay == myUser || cleanSenderId == myUser)) ||
                                   (myDisplay.isNotBlank() && (cleanSender == myDisplay || cleanSenderDisplay == myDisplay)) ||
                                   (myUserId.isNotBlank() && (cleanSender == myUserId || cleanSenderId == myUserId))

                    if (!isFromMe && cleanSender.isNotBlank() && content.isNotBlank()) {
                        val dedupKey = msgId.ifEmpty { "${cleanSender}_${chId}_${content.hashCode()}" }
                        if (notifiedMessageIds.add(dedupKey)) {
                            if (notifiedMessageIds.size > 500) {
                                val first = notifiedMessageIds.firstOrNull()
                                if (first != null) notifiedMessageIds.remove(first)
                            }
                            val displayTitle = senderDisplay.ifEmpty { senderUsername }
                            val navRef = if (chId.startsWith("dm-") || chId.startsWith("dm_") || rawChName.startsWith("dm-") || rawChName.startsWith("dm_")) {
                                senderUsername
                            } else {
                                chId.ifEmpty { senderUsername }
                            }

                            val activeTarget = activeChatRoomOrFriend?.lowercase()
                            val isCurrentlyViewingThisChat = activeTarget != null && (
                                activeTarget == cleanSender ||
                                (cleanSenderId.isNotBlank() && activeTarget == cleanSenderId) ||
                                (chId.isNotBlank() && activeTarget == chId.lowercase()) ||
                                (rawChName.isNotBlank() && activeTarget == rawChName.lowercase()) ||
                                (chId.startsWith("dm-") && chId.lowercase().contains(activeTarget)) ||
                                (rawChName.startsWith("dm-") && rawChName.lowercase().contains(activeTarget))
                            )

                            // Deliver Android system notification for ALL messages (even when app is not in the background)
                            ConnectoNotificationManager.showMessageNotification(
                                senderName = displayTitle,
                                messageText = content,
                                channelOrDmId = navRef,
                                senderAvatar = authorAvatar,
                                senderUsername = senderUsername
                            )

                            // In-app notification popup banner suppressed: only native Android system notification is posted
                            if (!isCurrentlyViewingThisChat) {
                                appContext?.let { ctx ->
                                    try {
                                        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                                        ConnectoDatabaseHelper.getInstance(ctx).incrementFriendUnreadCount(
                                            friendIdOrHandle = cleanSender,
                                            lastMessage = content,
                                            timeAgo = timeStr
                                        )
                                        val friendObj = ConnectoDatabaseHelper.getInstance(ctx).getFriendByUsernameOrHandle(cleanSender)
                                        val fid = friendObj?.id ?: cleanSender
                                        val fHandle = friendObj?.handle?.removePrefix("@")?.lowercase() ?: cleanSender
                                        for (prefName in listOf("connecto_chat_read_prefs", "connecto_chat_prefs")) {
                                            val rPrefs = ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                                            val curUnread = maxOf(rPrefs.getInt("unread_$fid", 0), rPrefs.getInt("unread_$cleanSender", 0)) + 1
                                            val editor = rPrefs.edit()
                                                .putString("last_msg_$fid", content)
                                                .putString("last_time_$fid", timeStr)
                                                .putInt("unread_$fid", curUnread)
                                                .putLong("last_timestamp_$fid", System.currentTimeMillis())
                                                .putString("last_msg_$cleanSender", content)
                                                .putString("last_time_$cleanSender", timeStr)
                                                .putInt("unread_$cleanSender", curUnread)
                                                .putLong("last_timestamp_$cleanSender", System.currentTimeMillis())
                                            if (fHandle != cleanSender && fHandle.isNotBlank()) {
                                                editor.putString("last_msg_$fHandle", content)
                                                    .putString("last_time_$fHandle", timeStr)
                                                    .putInt("unread_$fHandle", curUnread)
                                                    .putLong("last_timestamp_$fHandle", System.currentTimeMillis())
                                            }
                                            editor.apply()
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        }
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
                        val newBanner = json.optString("banner_url").ifEmpty {
                            json.optJSONObject("data")?.optString("banner_url") ?: ""
                        }
                        if (newBanner.isNotBlank()) {
                            ConnectoApiClient.updateBannerState(newBanner, appContext)
                        }
                        val newDisplay = json.optString("display_name").ifEmpty {
                            json.optJSONObject("data")?.optString("display_name") ?: ""
                        }
                        if (newDisplay.isNotBlank()) {
                            ConnectoApiClient.currentUserDisplayName = newDisplay
                        }
                        Log.d(TAG, "Real-time user avatar/banner/profile updated: avatar=$newAvatar, banner=$newBanner, display=$newDisplay")
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
                    val chId = json.optString("channel_id").ifEmpty { json.optString("channel").ifEmpty { "general" } }
                    val username = json.optString("username").ifEmpty { json.optString("user") }
                    val displayName = json.optString("display_name").ifEmpty { json.optString("displayName").ifEmpty { username } }
                    val isStealth = json.optBoolean("is_stealth", false)
                    if (username.isNotBlank() && !isStealth) {
                        _typingEvents.tryEmit(TypingEvent(username, displayName, chId, isTyping = true))
                        // Update legacy activeTypingUser for backward compat
                        ConnectoApiClient.activeTypingUser = displayName
                    }
                }

                "typing_stop", "typing:stop" -> {
                    val chId = json.optString("channel_id").ifEmpty { json.optString("channel").ifEmpty { "general" } }
                    val username = json.optString("username").ifEmpty { json.optString("user") }
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
                "message_read", "dm_read" -> {
                    val msgId = json.optString("message_id").ifEmpty { "" }
                    val chId = json.optString("channel_id").ifEmpty { json.optString("channel") }
                    val readerUsername = json.optString("reader_username").ifEmpty { json.optString("user").ifEmpty { json.optString("target") } }
                    _readReceiptEvents.tryEmit(ReadReceiptEvent(msgId, chId, readerUsername))
                    Log.d(TAG, "Read receipt received via WebSocket from $readerUsername for channel $chId")
                }


                "incoming_call", "call:incoming", "call_invite", "call:invite" -> {
                    val roomId = json.optString("room_id").ifEmpty { json.optString("call_id") }
                    if (isCallTerminated(roomId)) {
                        Log.d(TAG, "Ignoring incoming call for already terminated room: $roomId")
                        return
                    }
                    val callerId = json.optString("caller_id")
                    val callerUsername = json.optString("caller_username").ifEmpty { json.optString("caller") }
                    val currentU = (ConnectoApiClient.currentUsername ?: "").trim().lowercase().removePrefix("@")
                    if (callerUsername.trim().lowercase().removePrefix("@") == currentU && currentU.isNotBlank()) {
                        Log.d(TAG, "Ignoring incoming call echo from ourselves: $callerUsername")
                        return
                    }
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
                    com.example.connecto.ui.components.InAppNotificationController.show(
                        com.example.connecto.ui.components.InAppNotificationData(
                            title = callerName.ifEmpty { callerUsername },
                            message = "Incoming Voice Call • Tap to answer",
                            avatarUrl = callerAvatar,
                            username = callerUsername,
                            actionType = "call",
                            referenceId = roomId
                        )
                    )
                }

                "call_ringing", "call:ringing" -> {
                    val roomId = json.optString("room_id").ifEmpty { json.optString("call_id") }
                    if (isCallTerminated(roomId) || _callState.value == CallState.IDLE) {
                        Log.d(TAG, "Ignoring call_ringing for terminated or idle session: $roomId")
                        return
                    }
                    Log.d(TAG, "Call ringing on remote party")
                    _callState.value = CallState.OUTGOING_RINGING
                }

                "call_connected", "call:accepted", "call:connected" -> {
                    val roomId = json.optString("room_id").ifEmpty { json.optString("call_id") }
                    if (isCallTerminated(roomId) || _callState.value == CallState.IDLE) {
                        Log.d(TAG, "Ignoring call_connected for terminated or idle session: $roomId")
                        return
                    }
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
                                        _activeCallPartnerUsername.value ?: _activeCallPartner.value ?: ""
                                    }
                                }
                            }
                        }
                    }
                    if (isCallCaller && targetUser.isNotBlank()) {
                        initiateWebRtcOffer(targetUser)
                    }
                }

                "call_rejected", "call:declined", "call:rejected", "call_ended", "call:ended", "call_end", "call:end", "call:cancel", "call_canceled", "call_cancelled", "call:cancelled", "call_hung_up", "call:hung_up" -> {
                    val roomId = json.optString("room_id").ifEmpty { json.optString("call_id") }
                    Log.d(TAG, "Call terminated by peer or server signaling: ${json.optString("type")}, roomId=$roomId")
                    markCallTerminated(roomId)
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
                    if (isCallTerminated(roomId) || _callState.value == CallState.IDLE) {
                        Log.d(TAG, "Ignoring voice_room_joined for terminated or idle session: $roomId")
                        return
                    }
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
                    val peerId = json.optString("peer_id").ifEmpty { json.optString("username") }
                    val is1on1 = _activeCallPartner.value != null || _activeCallPartnerUsername.value != null || isCallCaller
                    Log.d(TAG, "WebRTC voice peer left: $peerId (is1on1=$is1on1, callState=${_callState.value})")
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

                    // In 1:1 direct calls, if the friend leaves or disconnects, end the call locally
                    if (is1on1 && _callState.value != CallState.IDLE) {
                        Log.d(TAG, "Friend left 1:1 voice room, ending call session locally")
                        endCallLocally()
                    }
                }

                "voice_offer", "call:offer" -> {
                    val senderId = json.optString("sender_user_id").ifEmpty {
                        json.optString("sender_username").ifEmpty {
                            json.optString("caller").ifEmpty {
                                _activeCallPartnerUsername.value ?: _activeCallPartner.value ?: ""
                            }
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
                    val senderUsername = json.optString("sender_username").ifEmpty {
                        _activeCallPartnerUsername.value ?: ""
                    }
                    val senderId = json.optString("sender_user_id").ifEmpty {
                        senderUsername.ifEmpty {
                            json.optString("caller").ifEmpty {
                                _activeCallPartner.value ?: ""
                            }
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
                    val senderUsername = json.optString("sender_username").ifEmpty {
                        _activeCallPartnerUsername.value ?: ""
                    }
                    val senderId = json.optString("sender_user_id").ifEmpty {
                        senderUsername.ifEmpty {
                            json.optString("caller").ifEmpty {
                                _activeCallPartner.value ?: ""
                            }
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
                                senderAvatar = friendAvatar,
                                senderUsername = friendUser
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

                "friend_removed", "unfriend_event" -> {
                    val myName = (ConnectoApiClient.currentUsername ?: "").trim().lowercase().removePrefix("@")
                    var friendUser = json.optString("friend_username")
                    if (friendUser.isEmpty()) {
                        val u1 = json.optString("user1")
                        val u2 = json.optString("user2")
                        friendUser = if (u1.equals(myName, ignoreCase = true)) u2 else u1
                    }
                    if (friendUser.isNotEmpty()) {
                        appContext?.let { ctx ->
                            val clean = friendUser.trim().lowercase().removePrefix("@")
                            ConnectoDatabaseHelper.getInstance(ctx).deleteMutualConversationData(myName, clean)
                            ConnectoApiClient.cachedCallLogs = ConnectoApiClient.cachedCallLogs.filter { log ->
                                val u = log.username.trim().lowercase().removePrefix("@")
                                val c = log.callerName.trim().lowercase().removePrefix("@")
                                u != clean && c != clean
                            }
                            val intent = Intent("com.connecto.app.FRIEND_REQUEST_UPDATED").apply {
                                putExtra("friend_username", clean)
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
                                    senderAvatar = nAvatar,
                                    senderUsername = nSender
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
                                val myUser = (ConnectoApiClient.currentUsername ?: "").trim().removePrefix("@").lowercase()
                                val myDisplay = (ConnectoApiClient.currentUserDisplayName ?: "").trim().lowercase()
                                val myUserId = (ConnectoApiClient.currentUserId ?: "").trim().lowercase()
                                val cleanSender = nSender.removePrefix("@").trim().lowercase()

                                val isFromMe = (myUser.isNotBlank() && cleanSender == myUser) ||
                                               (myDisplay.isNotBlank() && cleanSender == myDisplay) ||
                                               (myUserId.isNotBlank() && cleanSender == myUserId)

                                if (!isFromMe) {
                                    val extraMsgId: String = notifObj.optString("message_id").ifBlank {
                                        notifObj.optJSONObject("extra_data")?.optString("message_id") ?: ""
                                    }
                                    val dedupKey: String = if (extraMsgId.isNotBlank()) extraMsgId else if (nId.isNotBlank()) nId else "${cleanSender}_${nRefId}_${nContent.hashCode()}"
                                    if (notifiedMessageIds.add(dedupKey)) {
                                        if (notifiedMessageIds.size > 500) {
                                            val first = notifiedMessageIds.firstOrNull()
                                            if (first != null) notifiedMessageIds.remove(first)
                                        }
                                        val activeTarget = activeChatRoomOrFriend?.lowercase()
                                        val isCurrentlyViewingThisChat = activeTarget != null && (
                                            activeTarget == cleanSender ||
                                            (nRefId.isNotBlank() && (nRefId.lowercase() == activeTarget || nRefId.lowercase().contains(activeTarget)))
                                        )

                                        if (!isCurrentlyViewingThisChat) {
                                            ConnectoNotificationManager.showMessageNotification(
                                                senderName = nSender.ifEmpty { nTitle },
                                                messageText = nContent,
                                                channelOrDmId = nRefId.ifEmpty { nSender },
                                                senderAvatar = nAvatar,
                                                senderUsername = cleanSender.ifEmpty { nSender }
                                            )
                                        }
                                        // Ensure message is dispatched to real-time chat stream and local DB so it is visible in chat
                                        val canonicalName = if (cleanSender.isNotBlank() && myUser.isNotBlank()) {
                                            "dm-${minOf(myUser, cleanSender)}-${maxOf(myUser, cleanSender)}"
                                        } else nRefId
                                        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())
                                        val finalMsgId: String = if (extraMsgId.isNotBlank()) extraMsgId else if (nId.isNotBlank()) nId else "notif_msg_${System.currentTimeMillis()}"
                                        val msgDto = MessageDto(
                                            id = finalMsgId,
                                            channelId = canonicalName.ifEmpty { nRefId },
                                            authorId = cleanSender,
                                            authorName = cleanSender,
                                            content = nContent,
                                            createdAt = nowIso,
                                            authorAvatar = nAvatar
                                        )
                                        appContext?.let { ctx ->
                                            try {
                                                ConnectoDatabaseHelper.getInstance(ctx).saveMessage(msgDto)
                                                if (nRefId.isNotBlank() && nRefId != canonicalName) {
                                                    ConnectoDatabaseHelper.getInstance(ctx).saveMessage(msgDto.copy(channelId = nRefId))
                                                }
                                            } catch (_: Exception) {}
                                        }
                                        _incomingChannelMessages.tryEmit(msgDto)
                                        // In-app notification banner suppressed for personal chat / messages per user requirement
                                    }
                                }
                            }
                            "call", "call_invite" -> {
                                triggerIncomingCallFromPushOrBackground(
                                    roomId = nRefId,
                                    callerId = nSender,
                                    callerUsername = nSender.ifEmpty { "Gamer" },
                                    callerName = nSender.ifEmpty { "Gamer" },
                                    callerAvatar = nAvatar
                                )
                            }
                            else -> {
                                ConnectoNotificationManager.showMessageNotification(
                                    senderName = nTitle.ifEmpty { "Connecto" },
                                    messageText = nContent,
                                    channelOrDmId = nRefId,
                                    senderAvatar = nAvatar,
                                    senderUsername = nSender
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

    fun triggerIncomingCallFromPushOrBackground(
        roomId: String,
        callerId: String = "",
        callerUsername: String,
        callerName: String,
        callerAvatar: String? = null
    ) {
        if (roomId.isBlank()) return
        if (isCallTerminated(roomId)) {
            Log.d(TAG, "Ignoring incoming call trigger for already terminated room: $roomId")
            return
        }
        if (_callState.value != CallState.IDLE && _incomingCall.value?.roomId == roomId) {
            return
        }
        Log.d(TAG, "Triggering incoming call (background/push/poll) from $callerName ($roomId)")
        _incomingCall.value = IncomingCallData(
            roomId = roomId,
            callerId = callerId.ifEmpty { callerUsername },
            callerUsername = callerUsername,
            callerName = callerName.ifEmpty { callerUsername },
            callerAvatar = callerAvatar
        )
        _callState.value = CallState.INCOMING_RINGING
        ConnectoNotificationManager.showIncomingCallNotification(
            callerName = callerName.ifEmpty { callerUsername },
            roomId = roomId,
            callerUsername = callerUsername,
            callerAvatar = callerAvatar
        )
        com.example.connecto.ui.components.InAppNotificationController.show(
            com.example.connecto.ui.components.InAppNotificationData(
                title = callerName.ifEmpty { callerUsername },
                message = "Incoming Voice Call • Tap to answer",
                avatarUrl = callerAvatar,
                username = callerUsername,
                actionType = "call",
                referenceId = roomId
            )
        )
    }

    // ----------------------------------------------------
    // Public Call Controls
    // ----------------------------------------------------

    fun start1on1Call(targetUsername: String, targetDisplayName: String, avatarUrl: String? = null) {
        val callStartNano = System.nanoTime()
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

        // Instant fast-path avatar resolution from in-memory cache and local SQLite DB (<1ms)
        val resolvedAvatar = if (!avatarUrl.isNullOrBlank() && avatarUrl != "👤" && avatarUrl != "👾") {
            avatarUrl
        } else {
            val cachedFriend = ConnectoApiClient.cachedFriends.find {
                it.friendUsername.equals(cleanTarget, ignoreCase = true) ||
                it.friendDisplayName.equals(cleanTarget, ignoreCase = true) ||
                it.friendUsername.equals(cleanDisplayName, ignoreCase = true) ||
                it.friendDisplayName.equals(cleanDisplayName, ignoreCase = true)
            }
            cachedFriend?.friendAvatarUrl ?: appContext?.let { ctx ->
                try {
                    ConnectoDatabaseHelper.getInstance(ctx).getFriendByUsernameOrHandle(cleanTarget)?.avatarUrl
                } catch (_: Exception) { null }
            }
        }

        isCallCaller = true
        _activeRoomId.value = roomId
        _activeCallPartner.value = cleanDisplayName
        _activeCallPartnerUsername.value = cleanTarget
        _activeCallTitle.value = "1:1 Voice Call with $cleanDisplayName"
        _activeCallAvatar.value = resolvedAvatar
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
        val callerAvatar = ConnectoApiClient.currentUserAvatar ?: ""
        val callerDisplayName = ConnectoApiClient.currentUserDisplayName ?: currentUsername
        val inviteMsg = JSONObject().apply {
            put("action", "call:initiate")
            put("type", "call:initiate")
            put("target", cleanTarget)
            put("target_user", cleanTarget)
            put("call_id", roomId)
            put("room_id", roomId)
            put("caller", currentUsername)
            put("caller_username", currentUsername)
            put("caller_name", callerDisplayName)
            put("caller_avatar", callerAvatar)
            put("avatar", callerAvatar)
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

        val latencyMs = (System.nanoTime() - callStartNano) / 1_000_000.0
        Log.d(TAG, "[CallLatency] start1on1Call dispatched for $cleanTarget in ${String.format(java.util.Locale.US, "%.2f", latencyMs)}ms")

        if (resolvedAvatar.isNullOrBlank()) {
            scope.launch {
                try {
                    val fRes = ConnectoApiClient.getFriends()
                    if (fRes.isSuccess) {
                        val friend = fRes.getOrNull()?.find {
                            it.friendUsername.equals(cleanTarget, ignoreCase = true) ||
                            it.friendDisplayName.equals(cleanTarget, ignoreCase = true) ||
                            it.friendUsername.equals(cleanDisplayName, ignoreCase = true) ||
                            it.friendDisplayName.equals(cleanDisplayName, ignoreCase = true)
                        }
                        if (!friend?.friendAvatarUrl.isNullOrBlank()) {
                            _activeCallAvatar.value = friend?.friendAvatarUrl
                            return@launch
                        }
                    }

                    val res = ConnectoApiClient.getProfile(cleanTarget)
                    if (res.isSuccess) {
                        val p = res.getOrNull()
                        if (p != null && !p.username.equals(currentUsername, ignoreCase = true) && !p.avatarUrl.isNullOrBlank()) {
                            _activeCallAvatar.value = p.avatarUrl
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        // Fallback REST call in background
        scope.launch {
            try {
                ConnectoApiClient.inviteToVoiceCall(cleanTarget, roomId, callerAvatar)
            } catch (_: Throwable) {}
        }
    }

    fun acceptIncomingCall() {
        val call = _incomingCall.value ?: return
        com.example.connecto.ui.components.InAppNotificationController.dismiss()
        ConnectoNotificationManager.cancelCallNotification()
        isCallCaller = false
        _activeRoomId.value = call.roomId
        _activeCallPartner.value = call.callerName
        _activeCallPartnerUsername.value = call.callerUsername.ifEmpty { call.callerName }
        _activeCallTitle.value = "1:1 Voice Call with ${call.callerName}"
        _activeCallAvatar.value = call.callerAvatar
        _callState.value = CallState.CONNECTED
        _participantCount.value = 2

        if (call.callerAvatar.isNullOrBlank() && call.callerUsername.isNotBlank()) {
            scope.launch {
                try {
                    val res = ConnectoApiClient.getProfile(call.callerUsername)
                    if (res.isSuccess) {
                        val p = res.getOrNull()
                        if (!p?.avatarUrl.isNullOrBlank()) {
                            _activeCallAvatar.value = p?.avatarUrl
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        appContext?.let {
            VoiceCallForegroundService.startService(it, _activeCallTitle.value, call.callerName, _isMuted.value)
        }

        val acceptMsg = JSONObject().apply {
            put("action", "call:accept")
            put("type", "call:accept")   // FIX: was "call_accept" — production reads type field
            put("target", call.callerUsername)
            put("target_user", call.callerUsername)
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
            ConnectoApiClient.respondToVoiceCall(call.roomId, "accept", call.callerUsername)
        }

        startAudioHardware()
        startDurationTimer()
    }

    fun declineIncomingCall() {
        val call = _incomingCall.value ?: return
        markCallTerminated(call.roomId)
        com.example.connecto.ui.components.InAppNotificationController.dismiss()
        ConnectoNotificationManager.cancelCallNotification()
        val declineMsg = JSONObject().apply {
            put("action", "call:decline")
            put("type", "call:decline")   // FIX: was "call_decline"
            put("target", call.callerUsername)
            put("target_user", call.callerUsername)
            put("call_id", call.roomId)
            put("room_id", call.roomId)
            put("reason", "declined")
        }
        sendWsMessage(declineMsg)

        scope.launch {
            ConnectoApiClient.respondToVoiceCall(call.roomId, "decline", call.callerUsername)
        }

        // Record as a missed incoming call once
        val callSessionKey = "${call.roomId}_missed"
        if (!recordedCallSessions.contains(callSessionKey)) {
            recordedCallSessions.add(callSessionKey)
            scope.launch {
                ConnectoApiClient.recordCallLog(
                    callerName = call.callerUsername,
                    callType = "Voice Call",
                    durationSeconds = 0,
                    isMissed = true,
                    isOutgoing = false
                )
            }
        }

        _incomingCall.value = null
        _callState.value = CallState.IDLE
    }

    fun joinVoiceRoom(roomId: String, roomName: String, roomCode: String? = null) {
        _activeRoomId.value = roomId
        _activeCallTitle.value = roomName
        _activeCallPartner.value = null
        _activeCallPartnerUsername.value = null
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
        val roomId = _activeRoomId.value ?: _incomingCall.value?.roomId ?: _activeCallRoomCode.value
        markCallTerminated(roomId)
        val rawPartner = _activeCallPartnerUsername.value ?: _incomingCall.value?.callerUsername ?: _activeCallPartner.value ?: _incomingCall.value?.callerName ?: ""
        val cleanPartner = rawPartner.trim()
            .removePrefix("@")
            .removePrefix("1:1 Voice Call with ")
            .removePrefix("Voice Call with ")
            .removePrefix("Call with ")
            .trim()
        val partnerTitle = cleanPartner.ifBlank {
            (_activeCallPartner.value ?: _activeCallTitle.value)
                .removePrefix("1:1 Voice Call with ")
                .removePrefix("Voice Call with ")
                .removePrefix("Call with ")
                .removePrefix("@")
                .trim()
        }
        val duration = _callDurationSeconds.value
        val isCaller = isCallCaller
        val roomCode = _activeCallRoomCode.value ?: roomId ?: ""

        // Instantly switch call state to IDLE so compose overlays dismiss with zero latency
        _callState.value = CallState.IDLE

        if (!roomId.isNullOrBlank()) {
            val endMsg = JSONObject().apply {
                put("action", "call:end")
                put("type", "call:end")
                if (cleanPartner.isNotBlank()) {
                    put("target", cleanPartner)
                    put("target_user", cleanPartner)
                }
                put("call_id", roomId)
                put("room_id", roomId)
            }
            sendWsMessage(endMsg)

            val callEndMsg = JSONObject().apply {
                put("type", "call_end")
                put("action", "call:end")
                if (cleanPartner.isNotBlank()) {
                    put("target", cleanPartner)
                    put("target_user", cleanPartner)
                }
                put("room_id", roomId)
                put("call_id", roomId)
            }
            sendWsMessage(callEndMsg)

            val cancelMsg = JSONObject().apply {
                put("type", "call:cancel")
                put("action", "call:cancel")
                if (cleanPartner.isNotBlank()) {
                    put("target", cleanPartner)
                    put("target_user", cleanPartner)
                }
                put("room_id", roomId)
                put("call_id", roomId)
            }
            sendWsMessage(cancelMsg)

            // Also send voice_leave to remove us from server's voice_rooms map
            val leaveMsg = JSONObject().apply {
                put("type", "voice_leave")
                put("action", "voice:leave")
                put("channel_id", roomId)
                put("room_id", roomId)
            }
            sendWsMessage(leaveMsg)

            scope.launch {
                ConnectoApiClient.endVoiceCall(roomId, cleanPartner)
            }
            scope.launch {
                ConnectoApiClient.respondToVoiceCall(roomId, "decline", cleanPartner)
            }
        }

        // Record persistent call log exactly once per call session
        val callSessionKey = "${roomId}_${cleanPartner}_${isCaller}"
        if (!partnerTitle.isBlank() && !recordedCallSessions.contains(callSessionKey)) {
            recordedCallSessions.add(callSessionKey)
            scope.launch {
                ConnectoApiClient.recordCallLog(
                    callerName = partnerTitle,
                    callType = if (roomCode.startsWith("group_voice")) "Group Call" else "Voice Call",
                    durationSeconds = duration,
                    isMissed = false,
                    isOutgoing = isCaller,
                    roomName = partnerTitle,
                    roomCode = roomCode
                )
            }
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

    fun handleRemoteCallEnded(roomId: String) {
        Log.d(TAG, "handleRemoteCallEnded for room: $roomId")
        if (roomId.isNotBlank()) {
            markCallTerminated(roomId)
        }
        endCallLocally()
    }

    private fun endCallLocally() {
        synchronized(callTeardownLock) {
            val roomId = _activeRoomId.value ?: _incomingCall.value?.roomId ?: _activeCallRoomCode.value
            markCallTerminated(roomId)
            if (_callState.value == CallState.IDLE && _activeRoomId.value == null && _activeCallPartner.value == null && _incomingCall.value == null) {
                Log.d(TAG, "endCallLocally: Already idle, skipping redundant teardown")
                return
            }
            Log.d(TAG, "endCallLocally: Tearing down call session")
            com.example.connecto.ui.components.InAppNotificationController.dismiss()
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
            _activeCallPartnerUsername.value = null
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

    fun detectVoiceIsolationCapabilities(context: Context): VoiceIsolationCapabilities {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val hasNs = try { NoiseSuppressor.isAvailable() } catch (_: Throwable) { false }
        val hasAec = try { AcousticEchoCanceler.isAvailable() } catch (_: Throwable) { false }
        val hasAgc = try { AutomaticGainControl.isAvailable() } catch (_: Throwable) { false }

        var micCount = 1
        var hasBeamforming = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && am != null) {
            try {
                val mics = am.microphones
                micCount = mics.size
                hasBeamforming = micCount >= 2
            } catch (_: Throwable) {}
        }

        val isHwSupported = hasNs || hasAec || hasBeamforming
        val summary = when {
            hasBeamforming && hasNs -> "Hardware Dual-Mic Beamforming + AI Studio Isolation"
            hasNs -> "Hardware Noise Suppressor + AI Studio Isolation"
            hasAec -> "Hardware Echo Canceler + AI Studio Isolation"
            else -> "Advanced AI Studio Voice Isolation"
        }

        val caps = VoiceIsolationCapabilities(
            hasHardwareNoiseSuppressor = hasNs,
            hasHardwareEchoCanceler = hasAec,
            hasHardwareGainControl = hasAgc,
            hasHardwareMicBeamforming = hasBeamforming,
            multiMicCount = micCount,
            isHardwareSupported = isHwSupported,
            statusSummary = summary
        )
        _voiceIsolationCapabilities.value = caps
        Log.i(TAG, "Voice Isolation Capabilities detected on device: $caps")
        return caps
    }

    fun applyHardwareVoiceIsolationParams(enabled: Boolean) {
        try {
            val am = audioManager ?: return
            am.mode = AudioManager.MODE_IN_COMMUNICATION
            if (enabled) {
                am.setParameters("voice_isolation=true")
                am.setParameters("voice_isolation=on")
                am.setParameters("noise_suppression=on")
                am.setParameters("noise_suppression=true")
                am.setParameters("dualmic_enabled=true")
                am.setParameters("beamforming=true")
                am.setParameters("clear_voice=on")
                am.setParameters("call_noise_reduction=true")
                am.setParameters("ai_noise_reduction=true")
            } else {
                am.setParameters("voice_isolation=false")
                am.setParameters("voice_isolation=off")
                am.setParameters("noise_suppression=off")
            }
        } catch (_: Throwable) {}
    }

    /**
     * Toggles hardware and DSP voice isolation.
     * When enabled, background noise is dynamically suppressed and voice frequencies isolated.
     */
    fun toggleVoiceIsolation(): Boolean {
        val newState = !_isVoiceIsolationEnabled.value
        setVoiceIsolation(newState)
        Log.d(TAG, "Voice Isolation toggled to: $newState (${_voiceIsolationCapabilities.value.statusSummary})")
        return newState
    }

    fun setVoiceIsolation(enabled: Boolean) {
        _isVoiceIsolationEnabled.value = enabled
        try {
            hardwareNoiseSuppressor?.enabled = enabled
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to set hardware noise suppressor: ${t.message}")
        }
        try {
            hardwareEchoCanceler?.enabled = enabled
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to set hardware echo canceler: ${t.message}")
        }
        try {
            hardwareGainControl?.enabled = enabled
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to set hardware gain control: ${t.message}")
        }
        applyHardwareVoiceIsolationParams(enabled)
        try {
            appContext?.getSharedPreferences("connecto_audio_prefs", Context.MODE_PRIVATE)
                ?.edit()
                ?.putBoolean("connecto_voice_isolation_enabled", enabled)
                ?.apply()
        } catch (_: Throwable) {}
    }

    // Biquad filter state for 2nd-order Butterworth HPF (140Hz cutoff at 16kHz)
    private var hpX1 = 0f
    private var hpX2 = 0f
    private var hpY1 = 0f
    private var hpY2 = 0f

    // Biquad filter state for 2nd-order Butterworth LPF (3400Hz cutoff at 16kHz)
    private var lpX1 = 0f
    private var lpX2 = 0f
    private var lpY1 = 0f
    private var lpY2 = 0f

    // Adaptive noise floor and voice gate state
    private var dspNoiseFloor = 180.0
    private var dspSmoothedGain = 0.0f
    private var voiceHangoverFrames = 0

    // Filter coefficients precomputed for 16kHz audio:
    // HPF: fc = 140Hz, Q = 0.7071
    private val hpB0 = 0.961870f
    private val hpB1 = -1.923740f
    private val hpB2 = 0.961870f
    private val hpA1 = -1.922286f
    private val hpA2 = 0.925195f

    // LPF: fc = 3400Hz, Q = 0.7071
    private val lpB0 = 0.227117f
    private val lpB1 = 0.454234f
    private val lpB2 = 0.227117f
    private val lpA1 = -0.276664f
    private val lpA2 = 0.185132f

    /**
     * Multi-stage studio Voice Isolation DSP:
     * 1. 2nd-order Butterworth High-Pass Filter (140Hz) rejecting DC offset, handling noise, desk thumps, AC hum.
     * 2. 2nd-order Butterworth Low-Pass Filter (3400Hz) rejecting fan turbulence, coil whine, keyboard clicks.
     * 3. Statistical ambient noise floor estimation with stationary noise isolation.
     * 4. Downward expander / voice gate: complete black silence (0.00f) during pauses, 250ms hangover hold time.
     * 5. Vocal presence normalization with soft saturation limiter preventing clipping.
     */
    private fun applyVoiceIsolationDsp(buffer: ByteArray, length: Int) {
        if (length < 2) return

        val shortsCount = length / 2
        val filtered = FloatArray(shortsCount)
        var frameEnergy = 0.0

        for (i in 0 until shortsCount) {
            val low = buffer[i * 2].toInt() and 0xFF
            val high = buffer[i * 2 + 1].toInt()
            val raw = ((high shl 8) or low).toFloat()

            // 1. High-Pass Filter (140Hz)
            val hpY = hpB0 * raw + hpB1 * hpX1 + hpB2 * hpX2 - hpA1 * hpY1 - hpA2 * hpY2
            hpX2 = hpX1
            hpX1 = raw
            hpY2 = hpY1
            hpY1 = hpY

            // 2. Low-Pass Filter (3400Hz)
            val lpY = lpB0 * hpY + lpB1 * lpX1 + lpB2 * lpX2 - lpA1 * lpY1 - lpA2 * lpY2
            lpX2 = lpX1
            lpX1 = hpY
            lpY2 = lpY1
            lpY1 = lpY

            filtered[i] = lpY
            frameEnergy += lpY * lpY
        }

        val frameRms = if (shortsCount > 0) Math.sqrt(frameEnergy / shortsCount) else 0.0

        // Adaptive Noise Floor Profiler
        if (frameRms < dspNoiseFloor * 1.5) {
            dspNoiseFloor = dspNoiseFloor * 0.94 + frameRms * 0.06
        } else {
            dspNoiseFloor = dspNoiseFloor * 0.998 + frameRms * 0.002
        }

        val safeNoiseFloor = maxOf(dspNoiseFloor, 120.0)
        val speechThreshold = safeNoiseFloor * 2.3
        val silenceFloor = safeNoiseFloor * 1.2

        val targetGain = when {
            frameRms > speechThreshold -> {
                voiceHangoverFrames = 12 // ~240ms hold time prevents dropping word endings
                1.15f
            }
            voiceHangoverFrames > 0 -> {
                voiceHangoverFrames--
                1.0f
            }
            frameRms < silenceFloor -> {
                0.0f // Complete studio black silence when user is not speaking
            }
            else -> {
                val ratio = ((frameRms - silenceFloor) / (speechThreshold - silenceFloor)).coerceIn(0.0, 1.0).toFloat()
                ratio * ratio // Soft-knee expansion curve
            }
        }

        // Asymmetric attack / release smoothing
        if (targetGain > dspSmoothedGain) {
            dspSmoothedGain = dspSmoothedGain * 0.35f + targetGain * 0.65f // Instant attack (~10ms)
        } else {
            dspSmoothedGain = dspSmoothedGain * 0.86f + targetGain * 0.14f // Smooth decay (~150ms)
        }

        // Apply gain, noise floor reduction, and soft saturation limiter
        for (i in 0 until shortsCount) {
            val sample = filtered[i]
            var isolated = sample * dspSmoothedGain

            // Soft saturation limiter to preserve vocal warmth without digital clipping
            if (isolated > 30000f) {
                isolated = 30000f + (isolated - 30000f) * 0.2f
            } else if (isolated < -30000f) {
                isolated = -30000f + (isolated + 30000f) * 0.2f
            }

            val outShort = isolated.toInt().coerceIn(-32768, 32767)
            buffer[i * 2] = (outShort and 0xFF).toByte()
            buffer[i * 2 + 1] = ((outShort shr 8) and 0xFF).toByte()
        }
    }

    fun refreshAudioDevices() {
        try {
            val am = audioManager ?: return
            var btFound = false
            var btName = "Bluetooth Headset"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val devices = am.availableCommunicationDevices
                val btDevice = devices.firstOrNull { dev ->
                    dev.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    dev.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                    dev.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                    dev.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                }
                if (btDevice != null) {
                    btFound = true
                    btName = btDevice.productName?.toString()?.ifBlank { "Bluetooth Headset" } ?: "Bluetooth Headset"
                }
            } else {
                val outputs = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                val btOut = outputs.firstOrNull { dev ->
                    dev.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    dev.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                }
                if (btOut != null) {
                    btFound = true
                    btName = btOut.productName?.toString()?.ifBlank { "Bluetooth Headset" } ?: "Bluetooth Headset"
                }
            }

            _isBluetoothConnected.value = btFound
            _bluetoothDeviceName.value = if (btFound) btName else null
            val routes = mutableListOf(AudioRoute.SPEAKER, AudioRoute.EARPIECE)
            if (btFound) {
                routes.add(0, AudioRoute.BLUETOOTH)
            }
            _availableAudioRoutes.value = routes

            if (btFound && _callState.value == CallState.CONNECTED && _currentAudioRoute.value != AudioRoute.BLUETOOTH) {
                setAudioRoute(AudioRoute.BLUETOOTH)
            } else if (!btFound && _currentAudioRoute.value == AudioRoute.BLUETOOTH) {
                setAudioRoute(AudioRoute.SPEAKER)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing audio devices: ${e.message}")
        }
    }

    fun initAudioDeviceCallbacks() {
        try {
            val am = audioManager ?: return
            if (audioDeviceCallback == null) {
                val callback = object : android.media.AudioDeviceCallback() {
                    override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                        refreshAudioDevices()
                    }
                    override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                        refreshAudioDevices()
                    }
                }
                audioDeviceCallback = callback
                am.registerAudioDeviceCallback(callback, null)
            }
            refreshAudioDevices()
        } catch (e: Exception) {
            Log.e(TAG, "Error registering AudioDeviceCallback: ${e.message}")
        }
    }

    /**
     * Testing hook to simulate Bluetooth connection on Android emulators
     */
    fun setBluetoothConnectedForTesting(connected: Boolean, deviceName: String = "Galaxy Buds Pro") {
        _isBluetoothConnected.value = connected
        _bluetoothDeviceName.value = if (connected) deviceName else null
        val routes = mutableListOf(AudioRoute.SPEAKER, AudioRoute.EARPIECE)
        if (connected) {
            routes.add(0, AudioRoute.BLUETOOTH)
            setAudioRoute(AudioRoute.BLUETOOTH)
        } else {
            if (_currentAudioRoute.value == AudioRoute.BLUETOOTH) {
                setAudioRoute(AudioRoute.SPEAKER)
            }
        }
        _availableAudioRoutes.value = routes
        Log.d(TAG, "setBluetoothConnectedForTesting: connected=$connected, name=$deviceName, currentRoute=${_currentAudioRoute.value}")
    }

    fun setAudioRoute(route: AudioRoute) {
        _currentAudioRoute.value = route
        _isSpeakerOn.value = (route == AudioRoute.SPEAKER)
        try {
            val am = audioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val devices = am.availableCommunicationDevices
                when (route) {
                    AudioRoute.SPEAKER -> {
                        am.clearCommunicationDevice()
                        val spk = devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                        if (spk != null) {
                            am.setCommunicationDevice(spk)
                        } else {
                            @Suppress("DEPRECATION")
                            am.isSpeakerphoneOn = true
                        }
                        try {
                            am.stopBluetoothSco()
                            am.isBluetoothScoOn = false
                        } catch (_: Throwable) {}
                    }
                    AudioRoute.EARPIECE -> {
                        am.clearCommunicationDevice()
                        val earpiece = devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
                        if (earpiece != null) {
                            am.setCommunicationDevice(earpiece)
                        } else {
                            @Suppress("DEPRECATION")
                            am.isSpeakerphoneOn = false
                        }
                        try {
                            am.stopBluetoothSco()
                            am.isBluetoothScoOn = false
                        } catch (_: Throwable) {}
                    }
                    AudioRoute.BLUETOOTH -> {
                        val bt = devices.firstOrNull {
                            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                            it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                            it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        }
                        if (bt != null) {
                            am.setCommunicationDevice(bt)
                        }
                        try {
                            am.startBluetoothSco()
                            am.isBluetoothScoOn = true
                            @Suppress("DEPRECATION")
                            am.isSpeakerphoneOn = false
                        } catch (_: Throwable) {}
                    }
                }
            } else {
                when (route) {
                    AudioRoute.SPEAKER -> {
                        try {
                            am.stopBluetoothSco()
                            am.isBluetoothScoOn = false
                        } catch (_: Throwable) {}
                        @Suppress("DEPRECATION")
                        am.isSpeakerphoneOn = true
                    }
                    AudioRoute.EARPIECE -> {
                        try {
                            am.stopBluetoothSco()
                            am.isBluetoothScoOn = false
                        } catch (_: Throwable) {}
                        @Suppress("DEPRECATION")
                        am.isSpeakerphoneOn = false
                    }
                    AudioRoute.BLUETOOTH -> {
                        @Suppress("DEPRECATION")
                        am.isSpeakerphoneOn = false
                        try {
                            am.startBluetoothSco()
                            am.isBluetoothScoOn = true
                        } catch (_: Throwable) {}
                    }
                }
            }
            Log.d(TAG, "Switched audio route to: $route")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch audio route $route: ${e.message}")
        }
    }

    fun toggleSpeaker() {
        if (_isBluetoothConnected.value) {
            val nextRoute = when (_currentAudioRoute.value) {
                AudioRoute.BLUETOOTH -> AudioRoute.SPEAKER
                AudioRoute.SPEAKER -> AudioRoute.EARPIECE
                AudioRoute.EARPIECE -> AudioRoute.BLUETOOTH
            }
            setAudioRoute(nextRoute)
        } else {
            val nextRoute = if (_currentAudioRoute.value == AudioRoute.SPEAKER) AudioRoute.EARPIECE else AudioRoute.SPEAKER
            setAudioRoute(nextRoute)
        }
    }

    fun applyAudioSettingsForCall() {
        try {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            setAudioRoute(_currentAudioRoute.value)
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
                hardwareNoiseSuppressor?.release()
            } catch (_: Throwable) {}
            try {
                hardwareEchoCanceler?.release()
            } catch (_: Throwable) {}
            try {
                hardwareGainControl?.release()
            } catch (_: Throwable) {}
            hardwareNoiseSuppressor = null
            hardwareEchoCanceler = null
            hardwareGainControl = null

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    activeRecord.setPreferredMicrophoneDirection(AudioRecord.MIC_DIRECTION_TOWARDS_USER)
                    activeRecord.setPreferredMicrophoneFieldDimension(0.0f)
                } catch (t: Throwable) {
                    Log.w(TAG, "Microphone directionality not supported: ${t.message}")
                }
            }

            val sessionId = activeRecord.audioSessionId
            if (NoiseSuppressor.isAvailable()) {
                try {
                    hardwareNoiseSuppressor = NoiseSuppressor.create(sessionId)?.apply {
                        this.enabled = _isVoiceIsolationEnabled.value
                    }
                    Log.d(TAG, "Hardware NoiseSuppressor attached to AudioRecord (enabled=${hardwareNoiseSuppressor?.enabled})")
                } catch (t: Throwable) {
                    Log.w(TAG, "Could not attach hardware NoiseSuppressor: ${t.message}")
                }
            }
            if (AcousticEchoCanceler.isAvailable()) {
                try {
                    hardwareEchoCanceler = AcousticEchoCanceler.create(sessionId)?.apply {
                        this.enabled = _isVoiceIsolationEnabled.value
                    }
                    Log.d(TAG, "Hardware AcousticEchoCanceler attached to AudioRecord (enabled=${hardwareEchoCanceler?.enabled})")
                } catch (t: Throwable) {
                    Log.w(TAG, "Could not attach hardware AcousticEchoCanceler: ${t.message}")
                }
            }
            if (AutomaticGainControl.isAvailable()) {
                try {
                    hardwareGainControl = AutomaticGainControl.create(sessionId)?.apply {
                        this.enabled = _isVoiceIsolationEnabled.value
                    }
                    Log.d(TAG, "Hardware AutomaticGainControl attached to AudioRecord (enabled=${hardwareGainControl?.enabled})")
                } catch (t: Throwable) {
                    Log.w(TAG, "Could not attach hardware AutomaticGainControl: ${t.message}")
                }
            }
            applyHardwareVoiceIsolationParams(_isVoiceIsolationEnabled.value)

            recordingJob = scope.launch(Dispatchers.IO) {
                val buffer = ByteArray(CHUNK_SIZE_BYTES)
                try {
                    while (isActive && isRecordingActive && activeRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        val read = activeRecord.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            if (_isVoiceIsolationEnabled.value) {
                                applyVoiceIsolationDsp(buffer, read)
                            }
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
