package com.example.connecto.voice

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.connecto.MainActivity
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.notifications.ConnectoNotificationManager
import com.example.connecto.ui.designsystem.resolveImageModel
import com.example.connecto.ui.theme.ConnectoTheme
import com.example.connecto.ui.theme.OnlineGreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect

class IncomingCallActivity : ComponentActivity() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLockscreenFlags()
        acquireWakeLock()
        startRingtoneAndVibration()

        // Auto-dismiss if caller hangs up, cancels, declines, or call ends
        lifecycleScope.launch {
            VoiceCallManager.callState.collect { state ->
                if (state != CallState.INCOMING_RINGING) {
                    ConnectoNotificationManager.cancelCallNotification()
                    finish()
                }
            }
        }

        val roomId = intent.getStringExtra(ConnectoNotificationManager.EXTRA_ROOM_ID) ?: ""
        val callerName = intent.getStringExtra(ConnectoNotificationManager.EXTRA_CALLER_NAME) ?: "Connecto Gamer"
        val callerUsername = intent.getStringExtra("extra_caller_username") ?: callerName
        val callerAvatar = intent.getStringExtra("extra_caller_avatar")

        setContent {
            ConnectoTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF020617)
                ) {
                    FullScreenIncomingCallUI(
                        callerName = callerName,
                        callerUsername = callerUsername,
                        callerAvatar = callerAvatar,
                        onAccept = {
                            stopRingtoneAndVibration()
                            VoiceCallManager.acceptIncomingCall()
                            ConnectoNotificationManager.cancelCallNotification()
                            val mainIntent = Intent(this@IncomingCallActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra(ConnectoNotificationManager.EXTRA_NOTIFICATION_ACTION, ConnectoNotificationManager.ACTION_ANSWER_CALL)
                                putExtra(ConnectoNotificationManager.EXTRA_ROOM_ID, roomId)
                            }
                            startActivity(mainIntent)
                            finish()
                        },
                        onDecline = {
                            stopRingtoneAndVibration()
                            VoiceCallManager.declineIncomingCall()
                            ConnectoNotificationManager.cancelCallNotification()
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun startRingtoneAndVibration() {
        try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, soundUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()
        } catch (_: Throwable) {}

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            val pattern = longArrayOf(0, 800, 800, 800, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Throwable) {}
    }

    private fun stopRingtoneAndVibration() {
        try {
            if (ringtone?.isPlaying == true) {
                ringtone?.stop()
            }
            ringtone = null
        } catch (_: Throwable) {}

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (_: Throwable) {}
    }

    private fun setupLockscreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            @Suppress("DEPRECATION")
            wakeLock = pm?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "connecto:incoming_call_wake_lock"
            )?.apply {
                acquire(30000L)
            }
        } catch (_: Throwable) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtoneAndVibration()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }
}

@Composable
fun FullScreenIncomingCallUI(
    callerName: String,
    callerUsername: String,
    callerAvatar: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var resolvedAvatar by remember(callerAvatar) { mutableStateOf(callerAvatar) }

    LaunchedEffect(callerUsername) {
        if (resolvedAvatar.isNullOrBlank() || resolvedAvatar == "👤" || resolvedAvatar == "👾") {
            try {
                val cleanUser = callerUsername.removePrefix("@").trim()
                if (cleanUser.isNotBlank()) {
                    val res = ConnectoApiClient.getProfile(cleanUser)
                    if (res.isSuccess) {
                        val pic = res.getOrNull()?.avatarUrl
                        if (!pic.isNullOrBlank()) {
                            resolvedAvatar = pic
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020617),
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = OnlineGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "End-to-End Encrypted Voice Call",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "INCOMING CALL",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(170.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(OnlineGreen.copy(alpha = ringAlpha))
                    )
                    Box(
                        modifier = Modifier
                            .size(135.dp)
                            .clip(CircleShape)
                            .border(2.dp, OnlineGreen.copy(alpha = 0.6f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(2.5.dp, Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val imgModel = resolveImageModel(resolvedAvatar)
                        val isEmoji = resolvedAvatar != null && !resolvedAvatar!!.contains("/") && !resolvedAvatar!!.contains(".") && resolvedAvatar!!.length <= 4
                        if (!resolvedAvatar.isNullOrBlank() && !isEmoji && imgModel != null) {
                            AsyncImage(
                                model = imgModel,
                                contentDescription = callerName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else if (isEmoji && resolvedAvatar != null) {
                            Text(
                                text = resolvedAvatar!!,
                                fontSize = 42.sp
                            )
                        } else {
                            Text(
                                text = if (callerName.isNotBlank()) callerName.first().toString().uppercase() else "C",
                                color = Color.White,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = callerName,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "@${callerUsername.removePrefix("@")}",
                    color = Color(0xFF94A3B8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "48kHz Opus • Sub-5ms Mesh Audio",
                    color = OnlineGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable(onClick = onDecline),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Decline Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Decline",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable(onClick = onAccept),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Accept Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Accept",
                        color = Color(0xFF10B981),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
