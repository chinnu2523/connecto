package com.example.connecto.voice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.connecto.MainActivity
import com.example.connecto.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VoiceCallForegroundService : Service() {

    companion object {
        private const val TAG = "VoiceCallService"
        const val NOTIFICATION_ID = 8801
        const val CHANNEL_ID = "connecto_active_call_channel"
        const val CHANNEL_NAME = "Active Voice Call"

        const val ACTION_START = "com.example.connecto.voice.START_CALL_SERVICE"
        const val ACTION_STOP = "com.example.connecto.voice.STOP_CALL_SERVICE"
        const val ACTION_UPDATE_MUTE = "com.example.connecto.voice.UPDATE_MUTE"
        const val ACTION_UPDATE_DURATION = "com.example.connecto.voice.UPDATE_DURATION"

        const val EXTRA_CALL_TITLE = "extra_call_title"
        const val EXTRA_CALL_PARTNER = "extra_call_partner"
        const val EXTRA_IS_MUTED = "extra_is_muted"
        const val EXTRA_DURATION_SEC = "extra_duration_sec"

        fun startService(context: Context, title: String, partner: String? = null, isMuted: Boolean = false) {
            try {
                val intent = Intent(context, VoiceCallForegroundService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_CALL_TITLE, title)
                    putExtra(EXTRA_CALL_PARTNER, partner)
                    putExtra(EXTRA_IS_MUTED, isMuted)
                }
                ContextCompat.startForegroundService(context, intent)
                Log.d(TAG, "VoiceCallForegroundService start request sent for: $title")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to start VoiceCallForegroundService: ${t.message}", t)
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, VoiceCallForegroundService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
                Log.d(TAG, "VoiceCallForegroundService stop request sent")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to stop VoiceCallForegroundService: ${t.message}", t)
            }
        }

        fun updateMute(context: Context, isMuted: Boolean) {
            try {
                val intent = Intent(context, VoiceCallForegroundService::class.java).apply {
                    action = ACTION_UPDATE_MUTE
                    putExtra(EXTRA_IS_MUTED, isMuted)
                }
                context.startService(intent)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to update mute in VoiceCallForegroundService: ${t.message}", t)
            }
        }
    }

    private var currentTitle: String = "Voice Call"
    private var currentPartner: String? = null
    private var isMuted: Boolean = false
    private var currentDurationSec: Long = 0L

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var durationMonitorJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VoiceCallForegroundService created")
        createNotificationChannel()
        acquireLocks()
        startDurationCollector()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        Log.d(TAG, "onStartCommand action: $action")

        when (action) {
            ACTION_STOP -> {
                stopCallService()
                return START_NOT_STICKY
            }

            ACTION_UPDATE_MUTE -> {
                isMuted = intent?.getBooleanExtra(EXTRA_IS_MUTED, isMuted) ?: isMuted
                updateNotification()
            }

            ACTION_UPDATE_DURATION -> {
                currentDurationSec = intent?.getLongExtra(EXTRA_DURATION_SEC, currentDurationSec) ?: currentDurationSec
                updateNotification()
            }

            ACTION_START -> {
                val title = intent?.getStringExtra(EXTRA_CALL_TITLE)
                if (!title.isNullOrBlank()) currentTitle = title
                currentPartner = intent?.getStringExtra(EXTRA_CALL_PARTNER)
                isMuted = intent?.getBooleanExtra(EXTRA_IS_MUTED, false) ?: false

                startForegroundWithNotification()
            }
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved: app swiped away from recents. Call state: ${VoiceCallManager.callState.value}")

        // If the call is still active or connected, KEEP the service alive in the background!
        if (VoiceCallManager.callState.value == CallState.CONNECTED ||
            VoiceCallManager.callState.value == CallState.OUTGOING_RINGING) {
            Log.d(TAG, "Call is active: preserving VoiceCallForegroundService in background despite app closure")
        } else {
            stopCallService()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "VoiceCallForegroundService onDestroy")
        durationMonitorJob?.cancel()
        releaseLocks()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing Connecto voice calls and live room audio"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireLocks() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Connecto:VoiceCallWakeLock")?.apply {
                setReferenceCounted(false)
                acquire(120 * 60 * 1000L) // 2 hours max safeguard
            }
            Log.d(TAG, "WakeLock acquired")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to acquire WakeLock: ${t.message}", t)
        }

        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiLock = wifiManager?.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Connecto:VoiceCallWifiLock")?.apply {
                setReferenceCounted(false)
                acquire()
            }
            Log.d(TAG, "WifiLock acquired")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to acquire WifiLock: ${t.message}", t)
        }
    }

    private fun releaseLocks() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                Log.d(TAG, "WakeLock released")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error releasing WakeLock: ${t.message}", t)
        }
        wakeLock = null

        try {
            if (wifiLock?.isHeld == true) {
                wifiLock?.release()
                Log.d(TAG, "WifiLock released")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error releasing WifiLock: ${t.message}", t)
        }
        wifiLock = null
    }

    private fun startDurationCollector() {
        durationMonitorJob?.cancel()
        durationMonitorJob = serviceScope.launch {
            VoiceCallManager.callDurationSeconds.collect { sec ->
                currentDurationSec = sec.toLong()
                updateNotification()
            }
        }
    }

    private fun startForegroundWithNotification() {
        val notification = buildCallNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            Log.d(TAG, "startForeground initialized successfully")
        } catch (t: Throwable) {
            Log.e(TAG, "startForeground fallback execution: ${t.message}", t)
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (inner: Throwable) {
                Log.e(TAG, "startForeground failed: ${inner.message}", inner)
            }
        }
    }

    private fun updateNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            notificationManager.notify(NOTIFICATION_ID, buildCallNotification())
        } catch (t: Throwable) {
            // Ignore transient notification updates
        }
    }

    private fun buildCallNotification(): Notification {
        val formattedTime = formatDuration(currentDurationSec)
        val subtitle = "$formattedTime • ${if (isMuted) "Muted 🔇" else "Microphone Active 🎙️"} • Tap to return"

        // Open App / Return to Call PendingIntent
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            501,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Toggle Mute Action
        val muteIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_TOGGLE_MUTE
        }
        val mutePendingIntent = PendingIntent.getBroadcast(
            this,
            502,
            muteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val muteTitle = if (isMuted) "Unmute 🎙️" else "Mute 🔇"

        // End Call Action
        val endCallIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_END_CALL
        }
        val endCallPendingIntent = PendingIntent.getBroadcast(
            this,
            503,
            endCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📞 $currentTitle")
            .setContentText(subtitle)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF2563EB.toInt())
            .addAction(android.R.drawable.ic_lock_silent_mode, muteTitle, mutePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "End Call 🔴", endCallPendingIntent)

        return builder.build()
    }

    private fun stopCallService() {
        Log.d(TAG, "Stopping VoiceCallForegroundService")
        durationMonitorJob?.cancel()
        releaseLocks()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error stopping foreground: ${t.message}", t)
        }
        stopSelf()
    }

    private fun formatDuration(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
