package com.example.connecto.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
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
import com.example.connecto.ui.designsystem.resolveImageModel
import com.example.connecto.voice.AudioRoute
import com.example.connecto.voice.CallState
import com.example.connecto.voice.VoiceCallManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import com.example.connecto.ui.designsystem.LocalConnectoColors

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
    onMinimize: (() -> Unit)? = null,
    onEndCall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val designColors = LocalConnectoColors.current
    val gradientColors = getDynamicAccentGradientColors()

    val isMuted by VoiceCallManager.isMuted.collectAsState()
    val isSpeakerOn by VoiceCallManager.isSpeakerOn.collectAsState()
    val currentAudioRoute by VoiceCallManager.currentAudioRoute.collectAsState()
    val isBluetoothConnected by VoiceCallManager.isBluetoothConnected.collectAsState()
    val bluetoothDeviceName by VoiceCallManager.bluetoothDeviceName.collectAsState()
    var showAudioRouteDialog by remember { mutableStateOf(false) }
    var showKeypadDialog by remember { mutableStateOf(false) }

    val callDuration by VoiceCallManager.callDurationSeconds.collectAsState()
    val liveAmplitude by VoiceCallManager.liveAudioAmplitude.collectAsState()
    val callState by VoiceCallManager.callState.collectAsState()
    val managerParticipantCount by VoiceCallManager.participantCount.collectAsState()
    val managerAvatar by VoiceCallManager.activeCallAvatar.collectAsState()
    val callPartner by VoiceCallManager.activeCallPartner.collectAsState()
    val activePartnerUsername by VoiceCallManager.activeCallPartnerUsername.collectAsState()

    var resolvedAvatar by remember(avatarUrl, managerAvatar) {
        mutableStateOf(avatarUrl ?: managerAvatar)
    }

    LaunchedEffect(managerAvatar) {
        if (!managerAvatar.isNullOrBlank()) {
            resolvedAvatar = managerAvatar
        }
    }

    val cleanDisplayName = (callPartner ?: callTitle)
        .removePrefix("1:1 Voice Call with ")
        .removePrefix("Voice Call with ")
        .removePrefix("Call with ")
        .trim()
        .ifEmpty { "Friend" }

    LaunchedEffect(cleanDisplayName, activePartnerUsername, resolvedAvatar) {
        if (resolvedAvatar.isNullOrBlank() || resolvedAvatar == "👤" || resolvedAvatar == "👾") {
            val queryUser = activePartnerUsername?.trim()?.removePrefix("@")?.ifBlank { null }
                ?: cleanDisplayName.takeIf { !it.contains(" ") && it != "Friend" }
            if (!queryUser.isNullOrBlank()) {
                try {
                    val res = ConnectoApiClient.getProfile(queryUser)
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

    val callerInitial = cleanDisplayName.trim().take(1).uppercase().ifEmpty { "C" }
    val hue = (Math.abs(cleanDisplayName.hashCode()) % 360).toFloat()
    val avatarAccent = Color.hsv(hue, 0.65f, 0.9f)
    val themeAccent = designColors.primary

    val statusSubtitle = when (callState) {
        CallState.OUTGOING_RINGING -> "Calling..."
        CallState.INCOMING_RINGING -> "Incoming Audio Call..."
        CallState.CONNECTED -> if (!roomCode.isNullOrBlank()) "Active Room #$roomCode • Lossless HD Audio" else "Connected • End-to-End Encrypted"
        CallState.IDLE -> "Call Disconnected"
    }

    // Dynamic Pulsing Ring Animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.38f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "pulse1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.40f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "alpha1"
    )
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.70f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "pulse2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "alpha2"
    )

    // Continuous ambient glow breathing
    val ambientGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14)),
        contentAlignment = Alignment.Center
    ) {
        val bgModel = resolveImageModel(resolvedAvatar)
        val isBgEmoji = resolvedAvatar != null && !resolvedAvatar!!.contains("/") && !resolvedAvatar!!.contains(".") && resolvedAvatar!!.length <= 4
        if (!resolvedAvatar.isNullOrBlank() && !isBgEmoji && bgModel != null) {
            // Whole-screen friend profile photo covering entire display with cinematic blur
            AsyncImage(
                model = bgModel,
                contentDescription = cleanDisplayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(16.dp)
            )

            // Cinematic multi-stop gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xD9000000), // ~85% black at top status bar
                                Color(0x99000000), // ~60% black behind name & timer
                                Color(0x33000000), // ~20% black in upper center
                                Color(0x12000000), // ~7% black in middle (photo is fully visible & crisp)
                                Color(0x40000000), // ~25% black in lower center
                                Color(0xB3000000), // ~70% black
                                Color(0xF2000000)  // ~95% black at bottom behind controls
                            )
                        )
                    )
            )
        } else {
            // High-end ambient obsidian backdrop when no photo is set
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF070B14),
                                Color(0xFF0F172A),
                                Color(0xFF03060C)
                            )
                        )
                    )
            )

            // Subtle ambient radial halo
            Box(
                modifier = Modifier
                    .size(360.dp)
                    .graphicsLayer {
                        scaleX = ambientGlowScale
                        scaleY = ambientGlowScale
                        alpha = 0.15f
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                avatarAccent.copy(alpha = 0.35f),
                                themeAccent.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // TOP HEADER: Minimize Button, Security Status Pill, Audio Route
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minimize Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                        .clickable { onMinimize?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimize Call",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Security & Status Capsule Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = if (callState == CallState.CONNECTED) OnlineGreen else Color(0xFFFBBF24),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (callState == CallState.CONNECTED) OnlineGreen else Color(0xFFF59E0B))
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = if (callState == CallState.CONNECTED) "E2E ENCRYPTED" else "CONNECTING",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }

                // High Definition Audio Route Indicator (Speaker / Bluetooth / Earpiece)
                val isBtActive = isBluetoothConnected || currentAudioRoute == AudioRoute.BLUETOOTH
                val topAudioIcon = when {
                    isBtActive -> Icons.Default.Bluetooth
                    currentAudioRoute == AudioRoute.SPEAKER -> Icons.AutoMirrored.Filled.VolumeUp
                    else -> Icons.Default.Phone
                }
                val topAudioTint = when {
                    isBtActive -> Color(0xFF38BDF8)
                    currentAudioRoute == AudioRoute.SPEAKER -> OnlineGreen
                    else -> Color.White
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isBtActive) Color(0xFF38BDF8).copy(alpha = 0.20f)
                            else if (currentAudioRoute == AudioRoute.SPEAKER) OnlineGreen.copy(alpha = 0.20f)
                            else Color.White.copy(alpha = 0.08f)
                        )
                        .border(
                            1.dp,
                            if (isBtActive) Color(0xFF38BDF8).copy(alpha = 0.50f)
                            else if (currentAudioRoute == AudioRoute.SPEAKER) OnlineGreen.copy(alpha = 0.50f)
                            else Color.White.copy(alpha = 0.16f),
                            CircleShape
                        )
                        .clickable { showAudioRouteDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = topAudioIcon,
                        contentDescription = "Audio Route",
                        tint = topAudioTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // USER INFO & TIMER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = cleanDisplayName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.3).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (callState == CallState.CONNECTED) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedTime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFFF1F5F9)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (!roomCode.isNullOrBlank()) "Room #$roomCode • Lossless Audio" else "Connected • High Definition Audio",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                } else {
                    Text(
                        text = statusSubtitle,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // CENTER: Portrait (fallback monogram) & Dynamic Audio Equalizer Waveform
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val safeAmplitude = if (liveAmplitude.isNaN() || liveAmplitude.isInfinite()) 0f else liveAmplitude.coerceIn(0f, 1f)

                val voiceAuraScale by animateFloatAsState(
                    targetValue = if (callState == CallState.CONNECTED && !isMuted) 1f + (safeAmplitude * 0.22f) else 1f,
                    animationSpec = tween(100),
                    label = "voiceAura"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(190.dp)
                ) {
                    // Subtle voice aura ring expanding dynamically with live audio
                    Box(
                        modifier = Modifier
                            .size(174.dp)
                            .graphicsLayer {
                                scaleX = voiceAuraScale
                                scaleY = voiceAuraScale
                                alpha = if (callState == CallState.CONNECTED && !isMuted) (0.25f + safeAmplitude * 0.45f).coerceIn(0f, 0.70f) else 0.15f
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        OnlineGreen.copy(alpha = 0.55f),
                                        Color(0xFF06B6D4).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Master Portrait Circle with Connecto badge
                    Box(
                        modifier = Modifier.size(154.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFF1E293B),
                                            Color(0xFF0F172A)
                                        )
                                    )
                                )
                                .border(
                                    2.5.dp,
                                    Brush.linearGradient(
                                        if (callState == CallState.CONNECTED) listOf(OnlineGreen, Color(0xFF06B6D4))
                                        else listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.15f))
                                    ),
                                    CircleShape
                                )
                                .shadow(20.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val imgModel = resolveImageModel(resolvedAvatar)
                            val isEmoji = resolvedAvatar != null && !resolvedAvatar!!.contains("/") && !resolvedAvatar!!.contains(".") && resolvedAvatar!!.length <= 4
                            if (!resolvedAvatar.isNullOrBlank() && !isEmoji && imgModel != null) {
                                AsyncImage(
                                    model = imgModel,
                                    contentDescription = cleanDisplayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else if (isEmoji && resolvedAvatar != null) {
                                Text(
                                    text = resolvedAvatar!!,
                                    fontSize = 64.sp
                                )
                            } else {
                                Text(
                                    text = callerInitial,
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Connecto app icon badge on bottom-right of profile pic
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF0D0E15))
                                .border(2.dp, if (callState == CallState.CONNECTED) OnlineGreen else Color(0xFF6C5CE7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.connecto.R.drawable.connecto_app_icon),
                                contentDescription = "Connecto",
                                modifier = Modifier.size(34.dp).clip(CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Frosted Live Audio Waveform & Status Capsule
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Refined 7-Bar Dynamic Equalizer Waveform
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val barMultipliers = listOf(0.40f, 0.70f, 1.0f, 0.85f, 1.0f, 0.70f, 0.40f)
                            barMultipliers.forEach { multiplier ->
                                val targetHeight = (6.dp + (safeAmplitude * 36f * multiplier).dp).coerceIn(6.dp, 38.dp)
                                val animH by animateDpAsState(
                                    targetValue = if (isMuted || callState != CallState.CONNECTED) 6.dp else targetHeight,
                                    animationSpec = tween(65),
                                    label = "eqBar"
                                )
                                Box(
                                    modifier = Modifier
                                        .width(4.5.dp)
                                        .height(animH)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            brush = if (isMuted) {
                                                Brush.verticalGradient(listOf(Color(0xFFEF4444).copy(alpha = 0.7f), Color(0xFFEF4444).copy(alpha = 0.7f)))
                                            } else {
                                                Brush.verticalGradient(listOf(OnlineGreen, Color(0xFF06B6D4)))
                                            }
                                        )
                                )
                            }
                        }

                        Text(
                            text = if (isMuted) "Microphone Muted" else if (callState == CallState.CONNECTED) "Connected • Encrypted Audio" else "Establishing secure connection...",
                            fontSize = 12.sp,
                            color = if (isMuted) Color(0xFFEF4444) else Color.White.copy(alpha = 0.90f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // BOTTOM CONTROLS DOCK: Professional Console
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Row 1: 4 Control Buttons (Mute, Bluetooth/Speaker Route, Keypad, Chat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Mute / Unmute
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { VoiceCallManager.toggleMute() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isMuted) Color(0xFFEF4444).copy(alpha = 0.22f)
                                        else Color.White.copy(alpha = 0.10f)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isMuted) Color(0xFFEF4444) else Color.White.copy(alpha = 0.20f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute",
                                    tint = if (isMuted) Color(0xFFEF4444) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isMuted) "Unmute" else "Mute",
                                color = if (isMuted) Color(0xFFEF4444) else Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 2. Audio Route Selector (Bluetooth / Speaker / Earpiece)
                        val isBt = isBluetoothConnected || currentAudioRoute == AudioRoute.BLUETOOTH
                        val routeIcon = when {
                            isBt -> Icons.Default.Bluetooth
                            currentAudioRoute == AudioRoute.SPEAKER -> Icons.AutoMirrored.Filled.VolumeUp
                            else -> Icons.Default.Phone
                        }
                        val routeTint = when {
                            isBt -> Color(0xFF38BDF8)
                            currentAudioRoute == AudioRoute.SPEAKER -> OnlineGreen
                            else -> Color.White
                        }
                        val routeLabel = when {
                            isBt -> "Bluetooth"
                            currentAudioRoute == AudioRoute.SPEAKER -> "Speaker"
                            else -> "Earpiece"
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showAudioRouteDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isBt) Color(0xFF38BDF8).copy(alpha = 0.22f)
                                        else if (currentAudioRoute == AudioRoute.SPEAKER) OnlineGreen.copy(alpha = 0.22f)
                                        else Color.White.copy(alpha = 0.10f)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isBt) Color(0xFF38BDF8).copy(alpha = 0.60f)
                                        else if (currentAudioRoute == AudioRoute.SPEAKER) OnlineGreen.copy(alpha = 0.60f)
                                        else Color.White.copy(alpha = 0.20f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = routeIcon,
                                    contentDescription = "Audio Output",
                                    tint = routeTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = routeLabel,
                                color = routeTint,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 3. Dialpad / Keypad
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showKeypadDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.10f))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.20f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dialpad,
                                    contentDescription = "Keypad",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Keypad",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 4. Chat Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onMinimize?.invoke() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.10f))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.20f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "Chat",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Chat",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Row 2: Heroic End Call Action
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFEF4444),
                                        Color(0xFFDC2626)
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFF87171).copy(alpha = 0.50f), RoundedCornerShape(28.dp))
                            .clickable {
                                VoiceCallManager.endCall()
                                onEndCall()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "End Call",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Audio Route Selection Dialog
        if (showAudioRouteDialog) {
            AudioRouteSelectionDialog(onDismiss = { showAudioRouteDialog = false })
        }

        // In-Call Keypad Dialog
        if (showKeypadDialog) {
            InCallKeypadDialog(onDismiss = { showKeypadDialog = false })
        }
    }
}

/**
 * Audio Route Selection Dialog
 * Displays options: Bluetooth, Speaker, Earpiece
 */
@Composable
fun AudioRouteSelectionDialog(
    onDismiss: () -> Unit
) {
    val currentRoute by VoiceCallManager.currentAudioRoute.collectAsState()
    val isBluetoothConnected by VoiceCallManager.isBluetoothConnected.collectAsState()
    val bluetoothDeviceName by VoiceCallManager.bluetoothDeviceName.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Audio Output",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select preferred device for call audio",
                    fontSize = 12.5.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(18.dp))

                // 1. Bluetooth option
                val btSub = if (isBluetoothConnected) (bluetoothDeviceName ?: "Connected Audio Device") else "No device connected"
                AudioRouteSelectionItem(
                    icon = Icons.Default.Bluetooth,
                    title = "Bluetooth",
                    subtitle = btSub,
                    isSelected = currentRoute == AudioRoute.BLUETOOTH,
                    isEnabled = isBluetoothConnected,
                    onClick = {
                        VoiceCallManager.setAudioRoute(AudioRoute.BLUETOOTH)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Speaker option
                AudioRouteSelectionItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Speaker",
                    subtitle = "Device loudspeaker",
                    isSelected = currentRoute == AudioRoute.SPEAKER,
                    isEnabled = true,
                    onClick = {
                        VoiceCallManager.setAudioRoute(AudioRoute.SPEAKER)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Earpiece option
                AudioRouteSelectionItem(
                    icon = Icons.Default.Phone,
                    title = "Earpiece",
                    subtitle = "Internal phone receiver",
                    isSelected = currentRoute == AudioRoute.EARPIECE,
                    isEnabled = true,
                    onClick = {
                        VoiceCallManager.setAudioRoute(AudioRoute.EARPIECE)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Dismiss / Close
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Close",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioRouteSelectionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val itemBg = if (isSelected) OnlineGreen.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f)
    val itemBorder = if (isSelected) OnlineGreen.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.08f)
    val alpha = if (isEnabled) 1f else 0.40f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(itemBg)
            .border(1.dp, itemBorder, RoundedCornerShape(14.dp))
            .clickable(enabled = isEnabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isSelected) OnlineGreen.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) OnlineGreen else Color.White.copy(alpha = alpha),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) OnlineGreen else Color.White.copy(alpha = alpha)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8).copy(alpha = alpha)
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = OnlineGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * In-Call DTMF Keypad Dialog
 */
@Composable
fun InCallKeypadDialog(
    onDismiss: () -> Unit
) {
    var keypadInput by remember { mutableStateOf("") }
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("*", "0", "#")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Keypad",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = keypadInput.ifEmpty { " " },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnlineGreen,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                    .clickable {
                                        keypadInput += key
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onDismiss() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hide Keypad",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Minimized Call Capsule Dock
 * Aesthetic floating pill shown above the bottom bar when the user minimizes an active voice call
 * to multitask through Home, Messages, Channels, or Profile.
 */
@Composable
fun MinimizedCallPill(
    callTitle: String = "Voice Call",
    avatarUrl: String? = null,
    onExpand: () -> Unit = {},
    onEndCall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val callDuration by VoiceCallManager.callDurationSeconds.collectAsState()
    val callState by VoiceCallManager.callState.collectAsState()
    val isMuted by VoiceCallManager.isMuted.collectAsState()
    val callPartner by VoiceCallManager.activeCallPartner.collectAsState()
    val managerAvatar by VoiceCallManager.activeCallAvatar.collectAsState()
    val designColors = LocalConnectoColors.current

    val cleanName = (callPartner ?: callTitle)
        .removePrefix("1:1 Voice Call with ")
        .removePrefix("Voice Call with ")
        .removePrefix("Call with ")
        .trim()
        .ifEmpty { "Call" }

    val resolvedPic = avatarUrl ?: managerAvatar
    val minutes = callDuration / 60
    val seconds = callDuration % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val infiniteTransition = rememberInfiniteTransition(label = "pillPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pillAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
            .border(1.2.dp, designColors.primary.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
            .shadow(10.dp, RoundedCornerShape(28.dp))
            .clickable { onExpand() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Pulsing Green Indicator Dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .graphicsLayer { alpha = pulseAlpha }
                        .clip(CircleShape)
                        .background(if (callState == CallState.CONNECTED) OnlineGreen else Color(0xFFF59E0B))
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Small Avatar or Icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(designColors.primary.copy(alpha = 0.20f))
                        .border(1.dp, designColors.primary.copy(alpha = 0.50f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!resolvedPic.isNullOrBlank()) {
                        AsyncImage(
                            model = resolvedPic,
                            contentDescription = cleanName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = cleanName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = cleanName,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = if (callState == CallState.CONNECTED) formattedTime else "Calling...",
                        color = if (isMuted) Color(0xFFF87171) else Color(0xFF94A3B8),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Quick Actions: Mute Toggle + End Call
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) Color(0xFFEF4444).copy(alpha = 0.20f) else Color.White.copy(alpha = 0.10f))
                        .clickable { VoiceCallManager.toggleMute() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (isMuted) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626))
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
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

