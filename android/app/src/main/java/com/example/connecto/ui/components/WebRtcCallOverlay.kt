package com.example.connecto.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.voice.CallState
import com.example.connecto.voice.VoiceCallManager

@Composable
fun WebRtcCallOverlay(
    callTitle: String = "1:1 Voice Call",
    participantCount: Int = 2,
    roomCode: String? = null,
    allowVideo: Boolean = false,
    onInviteClick: (() -> Unit)? = null,
    onEndCall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gradientColors = getDynamicAccentGradientColors()

    // Bind real hardware state from VoiceCallManager
    val isMuted by VoiceCallManager.isMuted.collectAsState()
    val isSpeakerOn by VoiceCallManager.isSpeakerOn.collectAsState()
    val callDuration by VoiceCallManager.callDurationSeconds.collectAsState()
    val liveAmplitude by VoiceCallManager.liveAudioAmplitude.collectAsState()
    val callState by VoiceCallManager.callState.collectAsState()
    val managerParticipantCount by VoiceCallManager.participantCount.collectAsState()

    var isVideoOn by remember { mutableStateOf(false) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            VoiceCallManager.restartAudioHardwareIfConnected()
        }
    }

    LaunchedEffect(hasMicPermission) {
        if (hasMicPermission) {
            VoiceCallManager.restartAudioHardwareIfConnected()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val minutes = callDuration / 60
    val seconds = callDuration % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val effectiveParticipants = maxOf(participantCount, managerParticipantCount)

    val statusSubtitle = when (callState) {
        CallState.OUTGOING_RINGING -> "Ringing recipient..."
        CallState.INCOMING_RINGING -> "Incoming call..."
        CallState.CONNECTED -> if (!roomCode.isNullOrBlank()) {
            "Room: $roomCode • $effectiveParticipants active"
        } else {
            "Connected • Live Audio (16kHz PCM)"
        }
        CallState.IDLE -> "Call Ended"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.2.dp, Brush.linearGradient(gradientColors), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Status + ICE Connection State
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (allowVideo && isVideoOn) Icons.Default.Videocam else Icons.Default.Headset,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = callTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (callState == CallState.CONNECTED) OnlineGreen else Color(0xFFFFB300))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = statusSubtitle,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Live Equalizer Visualizer Bars (reacting to real mic amplitude)
                val safeAmplitude = if (liveAmplitude.isNaN() || liveAmplitude.isInfinite()) 0f else liveAmplitude.coerceIn(0f, 1f)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    val barHeights = listOf(
                        10.dp + (safeAmplitude * 20f).dp,
                        14.dp + (safeAmplitude * 32f).dp,
                        18.dp + (safeAmplitude * 42f).dp,
                        14.dp + (safeAmplitude * 28f).dp,
                        10.dp + (safeAmplitude * 18f).dp
                    )
                    barHeights.forEach { targetHeight ->
                        val safeTargetHeight = targetHeight.coerceIn(4.dp, 36.dp)
                        val animatedHeight by animateDpAsState(
                            targetValue = if (isMuted) 4.dp else safeTargetHeight,
                            animationSpec = tween(70),
                            label = "equalizerBar"
                        )
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(animatedHeight)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isMuted) Color.Gray else OnlineGreen)
                        )
                    }
                }

                // Duration Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // In-Call Action Control Buttons (Mute, Video, Speaker, End Call)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Mic Toggle Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) Color.Red.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (isMuted) Color.Red else MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { VoiceCallManager.toggleMute() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute Microphone",
                        tint = if (isMuted) Color.Red else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Camera Video Toggle Button (if enabled)
                if (allowVideo) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isVideoOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isVideoOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { isVideoOn = !isVideoOn },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Toggle Video Camera",
                            tint = if (isVideoOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Speaker Toggle Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isSpeakerOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (isSpeakerOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { VoiceCallManager.toggleSpeaker() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speaker",
                        tint = if (isSpeakerOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // End Call Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .clickable {
                            VoiceCallManager.endCall()
                            onEndCall()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenCallUI(
    callTitle: String = "Voice Call",
    participantCount: Int = 2,
    roomCode: String? = null,
    avatarUrl: String? = null,
    allowVideo: Boolean = false,
    onEndCall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val gradientColors = getDynamicAccentGradientColors()

    val isMuted by VoiceCallManager.isMuted.collectAsState()
    val isSpeakerOn by VoiceCallManager.isSpeakerOn.collectAsState()
    val callDuration by VoiceCallManager.callDurationSeconds.collectAsState()
    val liveAmplitude by VoiceCallManager.liveAudioAmplitude.collectAsState()
    val callState by VoiceCallManager.callState.collectAsState()
    val managerParticipantCount by VoiceCallManager.participantCount.collectAsState()
    val managerAvatar by VoiceCallManager.activeCallAvatar.collectAsState()
    val callPartner by VoiceCallManager.activeCallPartner.collectAsState()

    var resolvedAvatar by remember(avatarUrl, managerAvatar) {
        mutableStateOf(avatarUrl ?: managerAvatar)
    }

    val cleanDisplayName = (callPartner ?: callTitle)
        .removePrefix("1:1 Voice Call with ")
        .removePrefix("Voice Call with ")
        .removePrefix("Call with ")
        .trim()
        .ifEmpty { "Friend" }

    LaunchedEffect(cleanDisplayName, resolvedAvatar) {
        if (resolvedAvatar.isNullOrBlank() && cleanDisplayName.isNotBlank() && cleanDisplayName != "Friend") {
            try {
                val res = ConnectoApiClient.getProfile(cleanDisplayName)
                if (res.isSuccess) {
                    val p = res.getOrNull()
                    val pic = p?.avatarUrl
                    if (!pic.isNullOrBlank()) {
                        resolvedAvatar = pic
                    }
                }
            } catch (_: Exception) {}
        }
    }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) VoiceCallManager.restartAudioHardwareIfConnected()
    }
    LaunchedEffect(hasMicPermission) {
        if (hasMicPermission) VoiceCallManager.restartAudioHardwareIfConnected()
        else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val minutes = callDuration / 60
    val seconds = callDuration % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val effectiveParticipants = maxOf(participantCount, managerParticipantCount)
    val callerInitial = cleanDisplayName.trim().take(1).uppercase().ifEmpty { "C" }
    val hue = (Math.abs(cleanDisplayName.hashCode()) % 360).toFloat()
    val avatarAccent = Color.hsv(hue, 0.65f, 0.9f)

    val statusSubtitle = when (callState) {
        CallState.OUTGOING_RINGING -> "Ringing..."
        CallState.INCOMING_RINGING -> "Incoming Call..."
        CallState.CONNECTED -> if (!roomCode.isNullOrBlank()) "Room: $roomCode • HD Voice" else "Connected • Encrypted"
        CallState.IDLE -> "Call Ended"
    }

    // Pulsing ring animation around avatar
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.45f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "pulse1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.45f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "alpha1"
    )
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.75f,
        animationSpec = infiniteRepeatable(tween(2100), RepeatMode.Restart),
        label = "pulse2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.28f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2100), RepeatMode.Restart),
        label = "alpha2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF060911),
                        Color(0xFF0F172A),
                        Color(0xFF070B14)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background image watermark if avatar exists
        if (!resolvedAvatar.isNullOrBlank()) {
            AsyncImage(
                model = resolvedAvatar,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.14f }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp)
        ) {
            // TOP BAR: Call Mode & Encrypted Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (callState == CallState.CONNECTED) OnlineGreen else Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (callState == CallState.CONNECTED) "SECURE VOICE CALL" else "VOICE CALL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = cleanDisplayName,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = statusSubtitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )

                if (callState == CallState.CONNECTED) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.09f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            // CENTER: Large Profile Picture with Pulsing Rings & Equalizer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    // Outer pulse ring 2
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .graphicsLayer {
                                scaleX = pulseScale2
                                scaleY = pulseScale2
                                alpha = pulseAlpha2
                            }
                            .clip(CircleShape)
                            .background(avatarAccent.copy(alpha = 0.22f))
                    )
                    // Outer pulse ring 1
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .graphicsLayer {
                                scaleX = pulseScale1
                                scaleY = pulseScale1
                                alpha = pulseAlpha1
                            }
                            .clip(CircleShape)
                            .background(avatarAccent.copy(alpha = 0.35f))
                    )
                    // Main Avatar Profile Picture Circle (170.dp)
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF1E293B),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                            .border(3.5.dp, Brush.linearGradient(gradientColors), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!resolvedAvatar.isNullOrBlank()) {
                            AsyncImage(
                                model = resolvedAvatar,
                                contentDescription = cleanDisplayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = callerInitial,
                                fontSize = 68.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Live Audio Equalizer Waveform
                val safeAmplitude = if (liveAmplitude.isNaN() || liveAmplitude.isInfinite()) 0f else liveAmplitude.coerceIn(0f, 1f)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val barMultipliers = listOf(0.45f, 0.75f, 1.0f, 0.75f, 0.45f)
                    barMultipliers.forEach { multiplier ->
                        val targetHeight = (8.dp + (safeAmplitude * 36f * multiplier).dp).coerceIn(6.dp, 44.dp)
                        val animH by animateDpAsState(
                            targetValue = if (isMuted || callState != CallState.CONNECTED) 6.dp else targetHeight,
                            animationSpec = tween(70),
                            label = "eqBar"
                        )
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(animH)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isMuted) Color.Gray.copy(alpha = 0.5f) else avatarAccent)
                        )
                    }
                }
            }

            // BOTTOM CONTROLS DOCK
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute / Unmute Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(
                                if (isMuted) Color.Red.copy(alpha = 0.22f)
                                else Color.White.copy(alpha = 0.12f)
                            )
                            .border(1.5.dp, if (isMuted) Color.Red else Color.White.copy(alpha = 0.25f), CircleShape)
                            .clickable { VoiceCallManager.toggleMute() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (isMuted) Color.Red else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isMuted) "Unmute" else "Mute",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // End Call Button (Large Center)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .clickable {
                                VoiceCallManager.endCall()
                                onEndCall()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "End Call",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Speaker Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSpeakerOn) OnlineGreen.copy(alpha = 0.22f)
                                else Color.White.copy(alpha = 0.12f)
                            )
                            .border(1.5.dp, if (isSpeakerOn) OnlineGreen else Color.White.copy(alpha = 0.25f), CircleShape)
                            .clickable { VoiceCallManager.toggleSpeaker() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speaker",
                            tint = if (isSpeakerOn) OnlineGreen else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isSpeakerOn) "Speaker On" else "Speaker",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

