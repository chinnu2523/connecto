package com.example.connecto.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.connecto.R
import com.example.connecto.data.ConnectoDatabaseHelper
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.MessageDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Handles inline direct replies and "Mark as read" actions from Android system notifications,
 * providing a seamless notification experience just like WhatsApp.
 */
class DirectReplyReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DirectReplyReceiver"
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val ACTION_DIRECT_REPLY = "com.example.connecto.ACTION_DIRECT_REPLY"
        const val ACTION_MARK_AS_READ = "com.example.connecto.ACTION_MARK_AS_READ"
    }

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val channelOrDmId = intent.getStringExtra(ConnectoNotificationManager.EXTRA_CHANNEL_ID) ?: ""
        val senderUsername = intent.getStringExtra("extra_sender_username") ?: ""
        val conversationId = intent.getStringExtra("extra_conversation_id") ?: channelOrDmId
        val notificationId = intent.getIntExtra("extra_notification_id", ConnectoNotificationManager.NOTIFICATION_ID_MESSAGE)

        Log.d(TAG, "DirectReplyReceiver onReceive: action=$action channel=$channelOrDmId sender=$senderUsername notifId=$notificationId convId=$conversationId")

        when (action) {
            ACTION_MARK_AS_READ -> {
                // Cancel notification immediately from tray
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(notificationId)
                ConnectoNotificationManager.cancelConversationNotification(conversationId)
                if (channelOrDmId.isNotBlank() && channelOrDmId != conversationId) {
                    ConnectoNotificationManager.cancelConversationNotification(channelOrDmId)
                }

                receiverScope.launch {
                    try {
                        val dbHelper = ConnectoDatabaseHelper.getInstance(context)
                        val cleanSender = senderUsername.trim().lowercase().removePrefix("@")
                        if (cleanSender.isNotBlank()) {
                            dbHelper.updateFriendUnreadCount(cleanSender, 0)
                            for (prefName in listOf("connecto_chat_read_prefs", "connecto_chat_prefs")) {
                                context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                                    .edit()
                                    .putInt("unread_$cleanSender", 0)
                                    .putBoolean("read_$cleanSender", true)
                                    .apply()
                            }
                            try {
                                ConnectoApiClient.initSessionFromPrefs(context)
                                ConnectoApiClient.markDmRead(cleanSender)
                            } catch (_: Exception) {}
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to mark as read: ${e.message}")
                    }
                }
            }

            ACTION_DIRECT_REPLY -> {
                val remoteInput = RemoteInput.getResultsFromIntent(intent)
                val replyText = remoteInput?.getCharSequence(KEY_TEXT_REPLY)?.toString()?.trim()
                    ?: intent.getStringExtra(KEY_TEXT_REPLY)?.trim()

                if (replyText.isNullOrBlank()) {
                    Log.w(TAG, "Direct reply text is empty")
                    return
                }

                // SEC-A06 FIX: Suppress plaintext chat content in release logs
                if (com.example.connecto.BuildConfig.DEBUG) {
                    Log.d(TAG, "Processing direct reply: \"$replyText\" to $senderUsername ($channelOrDmId)")
                } else {
                    Log.d(TAG, "Processing direct reply to $senderUsername ($channelOrDmId)")
                }

                // Append the user's own reply to the active MessagingStyle notification briefly
                ConnectoNotificationManager.onInlineReplySubmitted(
                    conversationId = conversationId,
                    replyText = replyText,
                    notifId = notificationId
                )

                receiverScope.launch {
                    try {
                        ConnectoApiClient.initSessionFromPrefs(context)
                        val myUsername = (ConnectoApiClient.currentUsername ?: "").trim().removePrefix("@").lowercase()
                        val myUserId = ConnectoApiClient.currentUserId ?: myUsername

                        val cleanRecipient = senderUsername.trim().removePrefix("@").lowercase()
                        val cleanChannel = channelOrDmId.trim()
                        val targetChannel = cleanChannel.ifBlank { "dm-$cleanRecipient" }

                        // Send DM via background API
                        val sendResult = ConnectoApiClient.sendMessage(
                            channelId = targetChannel,
                            content = replyText
                        )

                        val messageId = sendResult.getOrNull()?.id ?: "reply_${System.currentTimeMillis()}"
                        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.format(Date())

                        // Save sent message locally to SQLite
                        val dbHelper = ConnectoDatabaseHelper.getInstance(context)
                        val msgDto = MessageDto(
                            id = messageId,
                            channelId = cleanChannel.ifBlank { "dm-$cleanRecipient" },
                            authorId = myUserId,
                            authorName = myUsername,
                            content = replyText,
                            createdAt = nowIso,
                            authorAvatar = null
                        )
                        dbHelper.saveMessage(msgDto)

                        // Update local last message snippet
                        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                        dbHelper.incrementFriendUnreadCount(
                            friendIdOrHandle = cleanRecipient,
                            lastMessage = "You: $replyText",
                            timeAgo = timeStr,
                            lastMessageTimestamp = System.currentTimeMillis()
                        )

                        // Dismiss cleanly once sent
                        kotlinx.coroutines.delay(1400L)
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                        notificationManager?.cancel(notificationId)
                        ConnectoNotificationManager.cancelConversationNotification(conversationId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send direct reply: ${e.message}", e)
                    }
                }
            }
        }
    }
}
