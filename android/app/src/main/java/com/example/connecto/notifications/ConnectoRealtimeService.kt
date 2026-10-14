package com.example.connecto.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.connecto.MainActivity
import com.example.connecto.R
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.voice.CallState
import com.example.connecto.voice.VoiceCallManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Persistent background/foreground service that keeps the Connecto real-time WebSocket
 * and incoming call / notification listener alive even when the app is backgrounded
 * or swiped closed from Recents.
 */
class ConnectoRealtimeService : Service() {

    companion object {
        private const val TAG = "ConnectoRealtimeSvc"
        const val NOTIFICATION_ID = 8809
        const val CHANNEL_ID = "connecto_realtime_background_sync"
        const val CHANNEL_NAME = "Connecto Real-Time Service"

        const val ACTION_START = "com.example.connecto.notifications.START_REALTIME_SERVICE"
        const val ACTION_STOP = "com.example.connecto.notifications.STOP_REALTIME_SERVICE"

        private val deliveredNotificationIds = java.util.Collections.synchronizedSet(java.util.LinkedHashSet<String>())
        private var initialNotificationsSeeded = false

        fun startService(context: Context) {
            try {
                val appCtx = context.applicationContext
                val intent = Intent(appCtx, ConnectoRealtimeService::class.java).apply {
                    action = ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(appCtx, intent)
                } else {
                    appCtx.startService(intent)
                }
            } catch (t: Throwable) {
                Log.w(TAG, "startForegroundService fallback: ${t.message}")
                try {
                    val appCtx = context.applicationContext
                    appCtx.startService(Intent(appCtx, ConnectoRealtimeService::class.java).apply {
                        action = ACTION_START
                    })
                } catch (e: Throwable) {
                    Log.e(TAG, "Failed to start ConnectoRealtimeService: ${e.message}")
                }
            }
        }

        fun stopService(context: Context) {
            try {
                val appCtx = context.applicationContext
                val intent = Intent(appCtx, ConnectoRealtimeService::class.java).apply {
                    action = ACTION_STOP
                }
                appCtx.stopService(intent)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to stop ConnectoRealtimeService: ${t.message}")
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitorJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        promoteToForeground()
        serviceScope.launch {
            initializeRealtimeStack()
            startBackgroundMonitorLoop()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            monitorJob?.cancel()
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
            } catch (_: Throwable) {}
            stopSelf()
            return START_NOT_STICKY
        }

        promoteToForeground()
        serviceScope.launch {
            initializeRealtimeStack()
            if (monitorJob?.isActive != true) {
                startBackgroundMonitorLoop()
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved: App swiped from Recents — keeping WebSocket & scheduling auto-restart")
        try {
            VoiceCallManager.connectWebSocket()
        } catch (_: Throwable) {}
        scheduleImmediateRestart()
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        super.onDestroy()
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        super.onTimeout(startId, fgsType)
        Log.w(TAG, "ConnectoRealtimeService onTimeout fired by system for fgsType $fgsType")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Throwable) {}
        stopSelf()
    }

    private fun initializeRealtimeStack() {
        try {
            ConnectoNotificationManager.init(applicationContext)
            ConnectoApiClient.initSessionFromPrefs(applicationContext)
            VoiceCallManager.init(applicationContext)

            val username = ConnectoApiClient.currentUsername
            val userId = ConnectoApiClient.currentUserId ?: username
            val displayName = ConnectoApiClient.currentUserDisplayName ?: username
            if (!username.isNullOrBlank() && !userId.isNullOrBlank()) {
                VoiceCallManager.updateCredentialsAndConnect(
                    userId = userId,
                    username = username,
                    displayName = displayName ?: username
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error initializing realtime stack in service: ${t.message}")
        }
    }

    private fun startBackgroundMonitorLoop() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            var tick = 0
            while (isActive) {
                try {
                    val username = ConnectoApiClient.currentUsername
                    if (!username.isNullOrBlank()) {
                        // 1. Ensure WebSocket connection stays alive
                        VoiceCallManager.connectWebSocket()

                        // 2. Check for pending incoming voice call (catches calls during WS reconnect or background state)
                        if (VoiceCallManager.callState.value == CallState.IDLE) {
                            val pendingCallRes = ConnectoApiClient.checkPendingVoiceCall(username)
                            val pendingObj = pendingCallRes.getOrNull()
                            if (pendingObj != null) {
                                val roomId = pendingObj.optString("room_id").ifEmpty { pendingObj.optString("call_id") }
                                val callerUsername = pendingObj.optString("caller_username").ifEmpty { pendingObj.optString("caller") }
                                val callerName = pendingObj.optString("caller_name").ifEmpty { callerUsername }
                                val callerAvatar = pendingObj.optString("caller_avatar").ifEmpty { pendingObj.optString("avatar") }.ifEmpty { null }
                                if (roomId.isNotBlank() && callerUsername.isNotBlank()) {
                                    VoiceCallManager.triggerIncomingCallFromPushOrBackground(
                                        roomId = roomId,
                                        callerId = pendingObj.optString("caller_id", callerUsername),
                                        callerUsername = callerUsername,
                                        callerName = callerName,
                                        callerAvatar = callerAvatar
                                    )
                                }
                            }
                        }

                        // 3. Poll unread notifications every ~6 seconds as a reliable fallback when app is closed
                        if (tick % 2 == 0) {
                            val notifsRes = ConnectoApiClient.getNotifications(unreadOnly = true, limit = 10, username = username)
                            val notifs = notifsRes.getOrNull()
                            if (notifs != null) {
                                if (!initialNotificationsSeeded) {
                                    // Seed existing unread IDs on initial service boot so we only alert on new events
                                    notifs.forEach { if (it.id.isNotBlank()) deliveredNotificationIds.add(it.id) }
                                    initialNotificationsSeeded = true
                                } else {
                                    for (n in notifs.asReversed()) {
                                        if (n.id.isNotBlank() && deliveredNotificationIds.add(n.id)) {
                                            when (n.type) {
                                                "call", "call_invite", "incoming_call" -> {
                                                    val callId = n.referenceId ?: ""
                                                    if (callId.isNotBlank() && VoiceCallManager.isCallTerminated(callId)) {
                                                        Log.d(TAG, "Ignoring notification for already terminated call: $callId")
                                                        serviceScope.launch {
                                                            ConnectoApiClient.markNotificationRead(n.id)
                                                        }
                                                    } else {
                                                        VoiceCallManager.triggerIncomingCallFromPushOrBackground(
                                                            roomId = callId,
                                                            callerId = n.senderUsername ?: "",
                                                            callerUsername = n.senderUsername ?: "Gamer",
                                                            callerName = n.senderUsername ?: n.title.ifEmpty { "Gamer" },
                                                            callerAvatar = n.senderAvatar
                                                        )
                                                    }
                                                }
                                                "friend_request" -> {
                                                    ConnectoNotificationManager.showFriendRequestNotification(
                                                        senderName = n.senderUsername ?: n.title.ifEmpty { "Gamer" },
                                                        requestId = n.id,
                                                        senderAvatar = n.senderAvatar
                                                    )
                                                }
                                                else -> {
                                                    val sender = n.senderUsername ?: n.title.ifEmpty { "Connecto" }
                                                    if (!sender.equals(username, ignoreCase = true)) {
                                                        ConnectoNotificationManager.showMessageNotification(
                                                            senderName = sender,
                                                            messageText = n.content,
                                                            channelOrDmId = n.referenceId ?: sender,
                                                            senderAvatar = n.senderAvatar
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "Monitor loop error: ${t.message}")
                }
                tick++
                delay(3500L)
            }
        }
    }

    private fun scheduleImmediateRestart() {
        try {
            val restartIntent = Intent(applicationContext, ConnectoBootReceiver::class.java).apply {
                action = ConnectoBootReceiver.ACTION_RESTART_SERVICE
                setPackage(packageName)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                9901,
                restartIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000L,
                pendingIntent
            )
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to schedule service restart alarm: ${t.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Maintains real-time calls and instant message notifications in background"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_SECRET
            }
            nm.createNotificationChannel(channel)
        }
    }

    private fun promoteToForeground() {
        val notification = buildSilentForegroundNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (t: Throwable) {
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (inner: Throwable) {
                Log.w(TAG, "Could not promote to foreground: ${inner.message}")
            }
        }
    }

    private fun buildSilentForegroundNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            8809,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Connecto Active")
            .setContentText("Listening for real-time calls & messages")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()
    }
}
