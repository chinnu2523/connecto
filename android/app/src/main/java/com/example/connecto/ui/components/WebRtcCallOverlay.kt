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

