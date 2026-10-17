package com.example.connecto.notifications

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
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
 * Background service that maintains the Connecto real-time WebSocket connection
 * and incoming call / notification listener while the app is active.
 * Runs silently without any ongoing notification banner.
 */
class ConnectoRealtimeService : Service() {

    companion object {
        private const val TAG = "ConnectoRealtimeSvc"
        const val NOTIFICATION_ID = 8809
        const val CHANNEL_ID = "connecto_realtime_background_sync"

        const val ACTION_START = "com.example.connecto.notifications.START_REALTIME_SERVICE"
        const val ACTION_STOP = "com.example.connecto.notifications.STOP_REALTIME_SERVICE"

        private val deliveredNotificationIds = java.util.Collections.synchronizedSet(java.util.LinkedHashSet<String>())
        private var initialNotificationsSeeded = false

        /**
         * Cancels any lingering "Connecto Active" foreground notification and deletes its notification channel.
         */
        fun dismissConnectoActiveNotification(context: Context) {
            try {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                nm?.cancel(NOTIFICATION_ID)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    nm?.deleteNotificationChannel(CHANNEL_ID)
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to dismiss notification or channel: ${t.message}")
            }
        }

        fun startService(context: Context) {
            try {
                dismissConnectoActiveNotification(context)
                val appCtx = context.applicationContext
                val intent = Intent(appCtx, ConnectoRealtimeService::class.java).apply {
                    action = ACTION_START
                }
                appCtx.startService(intent)
            } catch (t: Throwable) {
                Log.w(TAG, "startService: ${t.message}")
            }
        }

        fun stopService(context: Context) {
            try {
                dismissConnectoActiveNotification(context)
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
        dismissConnectoActiveNotification(this)
        serviceScope.launch {
            initializeRealtimeStack()
            startBackgroundMonitorLoop()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dismissConnectoActiveNotification(this)
        if (intent?.action == ACTION_STOP) {
            monitorJob?.cancel()
            stopSelf()
            return START_NOT_STICKY
        }

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
        Log.d(TAG, "onTaskRemoved: App swiped from Recents - maintaining background realtime listener")
        dismissConnectoActiveNotification(this)
        try {
            val appCtx = applicationContext
            val restartIntent = Intent(appCtx, ConnectoRealtimeService::class.java).apply {
                action = ACTION_START
            }
            appCtx.startService(restartIntent)
        } catch (t: Throwable) {
            Log.w(TAG, "onTaskRemoved restart: ${t.message}")
        }
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        dismissConnectoActiveNotification(this)
        super.onDestroy()
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
                                                        senderAvatar = n.senderAvatar,
                                                        senderUsername = n.senderUsername
                                                    )
                                                }
                                                else -> {
                                                    val sender = n.senderUsername ?: n.title.ifEmpty { "Connecto" }
                                                    if (!sender.equals(username, ignoreCase = true)) {
                                                        ConnectoNotificationManager.showMessageNotification(
                                                            senderName = sender,
                                                            messageText = n.content,
                                                            channelOrDmId = n.referenceId ?: sender,
                                                            senderAvatar = n.senderAvatar,
                                                            senderUsername = n.senderUsername ?: sender
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
}
