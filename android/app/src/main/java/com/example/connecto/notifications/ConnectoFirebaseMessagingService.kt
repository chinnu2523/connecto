package com.example.connecto.notifications

import android.util.Log
import com.example.connecto.network.ConnectoApiClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles FCM push notifications for Connecto.
 *
 * Routes incoming FCM push messages to ConnectoNotificationManager to display
 * as native Android system notifications when the app is backgrounded/killed.
 *
 * The backend sends FCM pushes when the recipient is OFFLINE (no active WebSocket).
 * When ONLINE, the WebSocket path handles in-app delivery instead.
 */
class ConnectoFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "ConnectoFCMService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** Called when FCM generates a new registration token. */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: ${token.take(20)}...")
        getSharedPreferences("connecto_fcm", MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply()
        serviceScope.launch {
            try {
                ConnectoApiClient.registerFcmToken(token)
            } catch (e: Exception) {
                Log.w(TAG, "FCM token registration failed: ${e.message}")
            }
        }
    }

    /** Called when a push message arrives while the app is FOREGROUND. */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")
        val data = remoteMessage.data
        val type = data["type"] ?: "message"
        val title = data["title"] ?: remoteMessage.notification?.title ?: "Connecto"
        val body = data["body"] ?: remoteMessage.notification?.body ?: ""
        val sender = data["sender"] ?: data["sender_username"] ?: ""
        val referenceId = data["reference_id"] ?: data["room_id"] ?: ""
        ConnectoNotificationManager.init(applicationContext)
        when (type) {
            "call_invite", "call", "incoming_call" -> {
                val callerAvatar = data["caller_avatar"] ?: data["avatar"]
                val callerUser = data["caller_username"] ?: sender.ifBlank { title }
                ConnectoNotificationManager.showIncomingCallNotification(
                    callerName = sender.ifBlank { title },
                    roomId = referenceId,
                    callerUsername = callerUser,
                    callerAvatar = callerAvatar
                )
            }
            "dm", "message", "new_dm_alert" -> {
                val senderAvatar = data["sender_avatar"] ?: data["avatar"]
                ConnectoNotificationManager.showMessageNotification(
                    senderName = sender.ifBlank { title },
                    messageText = body.ifBlank { "New message" },
                    channelOrDmId = referenceId.ifBlank { sender },
                    senderAvatar = senderAvatar
                )
            }
            "friend_request" -> {
                val senderAvatar = data["sender_avatar"] ?: data["avatar"]
                ConnectoNotificationManager.showFriendRequestNotification(
                    senderName = sender.ifBlank { title },
                    requestId = referenceId,
                    senderAvatar = senderAvatar
                )
            }
            else -> {
                val senderAvatar = data["sender_avatar"] ?: data["avatar"]
                ConnectoNotificationManager.showMessageNotification(
                    senderName = title,
                    messageText = body.ifBlank { "You have a new notification" },
                    channelOrDmId = referenceId,
                    senderAvatar = senderAvatar
                )
            }
        }
    }
}
