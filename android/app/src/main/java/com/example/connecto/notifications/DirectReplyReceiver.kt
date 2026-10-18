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
        val notificationId = intent.getIntExtra("extra_notification_id", ConnectoNotificationManager.NOTIFICATION_ID_MESSAGE)

        Log.d(TAG, "DirectReplyReceiver onReceive: action=$action channel=$channelOrDmId sender=$senderUsername notifId=$notificationId")

        when (action) {
            ACTION_MARK_AS_READ -> {
                // Cancel notification and clear unread counts in local database
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(notificationId)

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
                                    .apply()
                            }
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

                Log.d(TAG, "Processing direct reply: \"$replyText\" to $senderUsername ($channelOrDmId)")

                // Update notification immediately to show "Sending..." feedback like WhatsApp
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                val feedbackNotification = NotificationCompat.Builder(context, ConnectoNotificationManager.CHANNEL_MESSAGES)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(senderUsername.ifBlank { "Connecto" })
                    .setContentText("Replied: $replyText")
                    .setAutoCancel(true)
                    .setColor(0xFF6C5CE7.toInt())
                    .build()
                notificationManager?.notify(notificationId, feedbackNotification)

                receiverScope.launch {
                    try {
                        ConnectoApiClient.initSessionFromPrefs(context)
                        val myUsername = (ConnectoApiClient.currentUsername ?: "").trim().removePrefix("@").lowercase()
                        val myUserId = ConnectoApiClient.currentUserId ?: myUsername

                        val cleanRecipient = senderUsername.trim().removePrefix("@").lowercase()
                        val cleanChannel = channelOrDmId.trim()
                        val targetChannel = cleanChannel.ifBlank { "dm-$cleanRecipient" }

                        // Send DM via API
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

                        // Clear notification after small delay so user sees feedback
                        kotlinx.coroutines.delay(1200L)
                        notificationManager?.cancel(notificationId)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send direct reply: ${e.message}", e)
                    }
                }
            }
        }
    }
}
