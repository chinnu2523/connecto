package com.example.connecto.notifications

import android.content.Context
import android.util.Log
import com.example.connecto.data.ConnectoDatabaseHelper
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.MessageDto
import com.example.connecto.voice.VoiceCallManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Handles FCM push notifications for Connecto.
 *
 * Routes incoming FCM push messages to ConnectoNotificationManager to display
 * as native Android system notifications when the app is backgrounded/killed,
 * and persists messages directly to local SQLite cache so real-time and background
 * delivery is fully synchronized.
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
                ConnectoApiClient.initSessionFromPrefs(applicationContext)
                ConnectoApiClient.registerFcmToken(token)
            } catch (e: Exception) {
                Log.w(TAG, "FCM token registration failed: ${e.message}")
            }
        }
    }

    /** Called when a push message arrives while the app is in foreground or background/killed. */
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
                VoiceCallManager.init(applicationContext)
                VoiceCallManager.triggerIncomingCallFromPushOrBackground(
                    roomId = referenceId,
                    callerId = callerUser,
                    callerUsername = callerUser,
                    callerName = sender.ifBlank { title },
                    callerAvatar = callerAvatar
                )
            }
            "dm", "message", "new_dm_alert" -> {
                val senderAvatar = data["sender_avatar"] ?: data["avatar"]
                val senderUser = data["sender_username"] ?: sender
                val cleanSender = senderUser.trim().removePrefix("@").lowercase()
                val messageId = data["message_id"]?.ifBlank { null } ?: data["id"]?.ifBlank { null } ?: "fcm_${System.currentTimeMillis()}"
                val channelId = data["channel_id"]?.ifBlank { null } ?: referenceId.ifBlank { "dm-$cleanSender" }
                val createdAt = data["created_at"]?.ifBlank { null }
                    ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())

                // 1. Build MessageDto and persist to local SQLite database so history is cached when opened
                val msgDto = MessageDto(
                    id = messageId,
                    channelId = channelId,
                    authorId = cleanSender,
                    authorName = sender.ifBlank { cleanSender },
                    content = body,
                    createdAt = createdAt,
                    authorAvatar = senderAvatar
                )

                try {
                    val dbHelper = ConnectoDatabaseHelper.getInstance(applicationContext)
                    dbHelper.saveMessage(msgDto)

                    // Derive canonical DM name to ensure message is found under both UUID and canonical name
                    val myPrefs = getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE)
                    val myUsername = (myPrefs.getString("username", null) ?: ConnectoApiClient.currentUsername ?: "").trim().removePrefix("@").lowercase()
                    val canonicalName = if (myUsername.isNotBlank() && cleanSender.isNotBlank()) {
                        "dm-${minOf(myUsername, cleanSender)}-${maxOf(myUsername, cleanSender)}"
                    } else channelId
                    if (canonicalName.isNotBlank() && canonicalName != channelId) {
                        dbHelper.saveMessage(msgDto.copy(channelId = canonicalName))
                    }

                    // 2. Increment unread count & last message in SQLite
                    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                    val currentTs = System.currentTimeMillis()
                    dbHelper.incrementFriendUnreadCount(
                        friendIdOrHandle = cleanSender,
                        lastMessage = body,
                        timeAgo = timeStr,
                        lastMessageTimestamp = currentTs
                    )

                    // 3. Update persistent SharedPreferences so conversation list & unread badges reflect immediately
                    val friendObj = dbHelper.getFriendByUsernameOrHandle(cleanSender)
                    val fid = friendObj?.id ?: cleanSender
                    val fHandle = friendObj?.handle?.removePrefix("@")?.lowercase() ?: cleanSender
                    for (prefName in listOf("connecto_chat_read_prefs", "connecto_chat_prefs")) {
                        val rPrefs = getSharedPreferences(prefName, Context.MODE_PRIVATE)
                        val curUnread = maxOf(rPrefs.getInt("unread_$fid", 0), rPrefs.getInt("unread_$cleanSender", 0)) + 1
                        val editor = rPrefs.edit()
                            .putString("last_msg_$fid", body)
                            .putString("last_time_$fid", timeStr)
                            .putInt("unread_$fid", curUnread)
                            .putLong("last_timestamp_$fid", currentTs)
                            .putString("last_msg_$cleanSender", body)
                            .putString("last_time_$cleanSender", timeStr)
                            .putInt("unread_$cleanSender", curUnread)
                            .putLong("last_timestamp_$cleanSender", currentTs)
                        if (fHandle != cleanSender && fHandle.isNotBlank()) {
                            editor.putString("last_msg_$fHandle", body)
                                .putString("last_time_$fHandle", timeStr)
                                .putInt("unread_$fHandle", curUnread)
                                .putLong("last_timestamp_$fHandle", currentTs)
                        }
                        editor.apply()
                    }

                    // 4. Forward to in-memory VoiceCallManager bus if process is active
                    VoiceCallManager.handleIncomingPushMessage(msgDto)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to persist FCM message: ${e.message}")
                }

                // 5. Display Android system notification
                ConnectoNotificationManager.showMessageNotification(
                    senderName = sender.ifBlank { title },
                    messageText = body.ifBlank { "New message" },
                    channelOrDmId = referenceId.ifBlank { senderUser },
                    senderAvatar = senderAvatar,
                    senderUsername = senderUser
                )
            }
            "friend_request" -> {
                val senderAvatar = data["sender_avatar"] ?: data["avatar"]
                val senderUser = data["sender_username"] ?: sender
                ConnectoNotificationManager.showFriendRequestNotification(
                    senderName = sender.ifBlank { title },
                    requestId = referenceId,
                    senderAvatar = senderAvatar,
                    senderUsername = senderUser
                )
            }
            else -> {
                val senderAvatar = data["sender_avatar"] ?: data["avatar"]
                val senderUser = data["sender_username"] ?: sender
                ConnectoNotificationManager.showMessageNotification(
                    senderName = title,
                    messageText = body.ifBlank { "You have a new notification" },
                    channelOrDmId = referenceId,
                    senderAvatar = senderAvatar,
                    senderUsername = senderUser
                )
            }
        }
    }
}
