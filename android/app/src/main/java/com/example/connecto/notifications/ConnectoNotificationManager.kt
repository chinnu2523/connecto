package com.example.connecto.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.connecto.MainActivity
import com.example.connecto.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.connecto.voice.CallActionReceiver

object ConnectoNotificationManager {
    private const val TAG = "ConnectoNotifManager"

    // Notification Channel IDs
    const val CHANNEL_CALLS = "connecto_voice_calls_v2"
    const val CHANNEL_MESSAGES = "connecto_messages_v2"
    const val CHANNEL_SOCIAL = "connecto_social_v2"

    // Action Intents
    const val ACTION_ANSWER_CALL = "com.example.connecto.ACTION_ANSWER_CALL"
    const val ACTION_DECLINE_CALL = "com.example.connecto.ACTION_DECLINE_CALL"
    const val ACTION_OPEN_CHAT = "com.example.connecto.ACTION_OPEN_CHAT"
    const val ACTION_OPEN_NOTIFICATIONS = "com.example.connecto.ACTION_OPEN_NOTIFICATIONS"

    // Intent Extras
    const val EXTRA_ROOM_ID = "extra_room_id"
    const val EXTRA_CALLER_NAME = "extra_caller_name"
    const val EXTRA_CHANNEL_ID = "extra_channel_id"
    const val EXTRA_NOTIFICATION_ACTION = "extra_notification_action"

    const val NOTIFICATION_ID_CALL = 7001
    const val NOTIFICATION_ID_MESSAGE = 7002
    const val NOTIFICATION_ID_SOCIAL = 7003

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val context = appContext ?: return
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            // 1. Calls Channel (High importance, vibration, heads-up banner)
            val callsChannel = NotificationChannel(
                CHANNEL_CALLS,
                "Voice & Video Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming 1:1 voice calls and war room alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            // 2. Messages Channel
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Chat Messages & Mentions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Direct messages, channel mentions, and squad pings"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                setShowBadge(true)
            }

            // 3. Social Channel
            val socialChannel = NotificationChannel(
                CHANNEL_SOCIAL,
                "Friend Requests & Activity",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Friend requests, accepted invitations, and clan activity"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(callsChannel, messagesChannel, socialChannel))
            Log.d(TAG, "Notification channels registered successfully")
        }
    }

    private fun hasNotificationPermission(): Boolean {
        val context = appContext ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun shouldShowNotification(channelOrDmId: String? = null): Boolean {
        val context = appContext ?: return true
        val prefs = context.getSharedPreferences("connecto_notification_prefs", Context.MODE_PRIVATE)

        // 1. Check Do Not Disturb (DND)
        val dndEnabled = prefs.getBoolean("dnd_enabled", false)
        if (dndEnabled) {
            Log.d(TAG, "Notification suppressed: DND is enabled")
            return false
        }

        // 2. Check Quiet Hours
        val quietHoursEnabled = prefs.getBoolean("quiet_hours_enabled", false)
        if (quietHoursEnabled) {
            val startHour = prefs.getInt("quiet_hours_start_hour", 22)
            val startMin = prefs.getInt("quiet_hours_start_min", 0)
            val endHour = prefs.getInt("quiet_hours_end_hour", 7)
            val endMin = prefs.getInt("quiet_hours_end_min", 0)

            val now = java.util.Calendar.getInstance()
            val currentMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
            val startMinutes = startHour * 60 + startMin
            val endMinutes = endHour * 60 + endMin

            val inQuietHours = if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes..endMinutes
            } else {
                currentMinutes >= startMinutes || currentMinutes <= endMinutes
            }

            if (inQuietHours) {
                Log.d(TAG, "Notification suppressed: Within quiet hours")
                return false
            }
        }

        // 3. Check Muted Channels / DMs
        if (!channelOrDmId.isNullOrBlank()) {
            val cleanId = channelOrDmId.trim().lowercase().removePrefix("#").removePrefix("@")
            val muted = prefs.getStringSet("muted_channels", emptySet()) ?: emptySet()
            if (muted.any { it.trim().lowercase().removePrefix("#").removePrefix("@") == cleanId }) {
                Log.d(TAG, "Notification suppressed: Channel/DM '$channelOrDmId' is muted")
                return false
            }
        }

        return true
    }

    @SuppressLint("MissingPermission")
    fun showIncomingCallNotification(
        callerName: String,
        roomId: String,
        callerUsername: String = callerName,
        callerAvatar: String? = null
    ) {
        val context = appContext ?: return
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Cannot show incoming call notification: POST_NOTIFICATIONS permission not granted")
            return
        }

        // Suppress only if strict DND is explicitly enabled
        val prefs = context.getSharedPreferences("connecto_notification_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("dnd_enabled", false)) {
            Log.d(TAG, "Call notification suppressed: DND enabled")
            return
        }

        // Native dialer style Full-Screen Intent directly launching IncomingCallActivity
        val fullScreenIntent = Intent(context, com.example.connecto.voice.IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra("extra_caller_username", callerUsername)
            putExtra("extra_caller_avatar", callerAvatar)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            101,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Answer Action broadcast directly to CallActionReceiver
        val answerIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_ANSWER_CALL
            putExtra(EXTRA_ROOM_ID, roomId)
            putExtra(EXTRA_CALLER_NAME, callerName)
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decline Action broadcast directly to CallActionReceiver
        val declineIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_DECLINE_CALL
            putExtra(EXTRA_ROOM_ID, roomId)
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            103,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val avatarBitmap = fetchAvatarBitmap(callerAvatar) ?: createMonogramBitmap(callerName)

                val notification = NotificationCompat.Builder(context, CHANNEL_CALLS)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(avatarBitmap)
                    .setContentTitle("Incoming Voice Call")
                    .setSubText("Connecto • Voice")
                    .setContentText("$callerName is calling you on Connecto")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setSound(soundUri)
                    .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
                    .setFullScreenIntent(fullScreenPendingIntent, true)
                    .setContentIntent(fullScreenPendingIntent)
                    .addAction(android.R.drawable.ic_menu_call, "Answer", answerPendingIntent)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", declinePendingIntent)
                    .setColor(0xFF6C5CE7.toInt()) // Connecto Brand Accent Purple
                    .build()

                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CALL, notification)
                Log.d(TAG, "Incoming call notification displayed with avatar for: $callerName ($roomId)")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to post incoming call notification: ${t.message}", t)
            }
        }
    }

    fun createMonogramBitmap(name: String): Bitmap {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cleanName = name.ifBlank { "User" }
        val hue = (Math.abs(cleanName.hashCode()) % 360).toFloat()
        paint.color = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.75f, 0.85f))
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val initial = cleanName.trim().take(1).uppercase()
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 60f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val yOffset = (size / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(initial, size / 2f, yOffset, paint)

        return bitmap
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            color = -0x1
        }
        val rect = Rect(0, 0, size, size)
        val srcRect = Rect(
            (bitmap.width - size) / 2,
            (bitmap.height - size) / 2,
            (bitmap.width + size) / 2,
            (bitmap.height + size) / 2
        )
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, srcRect, rect, paint)
        return output
    }

    private suspend fun fetchAvatarBitmap(avatarUrl: String?): Bitmap? = withContext(Dispatchers.IO) {
        if (avatarUrl.isNullOrBlank()) return@withContext null
        try {
            val resolvedUrl = if (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")) {
                avatarUrl
            } else {
                val cleanPath = avatarUrl.trimStart('/')
                "${com.example.connecto.network.ConnectoNetworkConfig.activeBaseUrl}/$cleanPath"
            }
            val connection = java.net.URL(resolvedUrl).openConnection() as java.net.HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.connect()
            if (connection.responseCode == 200) {
                connection.inputStream.use { input ->
                    val rawBitmap = BitmapFactory.decodeStream(input)
                    if (rawBitmap != null) getCircularBitmap(rawBitmap) else null
                }
            } else null
        } catch (_: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun showMessageNotification(
        senderName: String,
        messageText: String,
        channelOrDmId: String,
        senderAvatar: String? = null
    ) {
        val context = appContext ?: return
        if (!hasNotificationPermission()) return
        if (!shouldShowNotification(channelOrDmId)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val avatarBitmap = fetchAvatarBitmap(senderAvatar) ?: createMonogramBitmap(senderName)

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_OPEN_CHAT)
                    putExtra(EXTRA_CHANNEL_ID, channelOrDmId)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    201,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val isDm = channelOrDmId.startsWith("dm-") || channelOrDmId.startsWith("dm_") || !channelOrDmId.startsWith("#")
                val subText = if (isDm) "Connecto • DM" else "Connecto • #${channelOrDmId.removePrefix("#")}"

                val personBuilder = Person.Builder()
                    .setName(senderName)
                    .setKey(channelOrDmId)
                    .setIcon(IconCompat.createWithBitmap(avatarBitmap))
                val person = personBuilder.build()

                val messagingStyle = NotificationCompat.MessagingStyle(person)
                    .setConversationTitle(if (isDm) null else "#${channelOrDmId.removePrefix("#")}")
                    .addMessage(messageText, System.currentTimeMillis(), person)

                val builder = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(avatarBitmap)
                    .setContentTitle(senderName)
                    .setSubText(subText)
                    .setContentText(messageText)
                    .setStyle(messagingStyle)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setColor(0xFF6C5CE7.toInt()) // Connecto Brand Accent Purple
                    .setDefaults(NotificationCompat.DEFAULT_ALL)

                val notification = builder.build()
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_MESSAGE + (channelOrDmId.hashCode() % 1000), notification)
                Log.d(TAG, "Message notification posted with sender avatar for: $senderName")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to post message notification: ${t.message}", t)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun showFriendRequestNotification(
        senderName: String,
        requestId: String,
        senderAvatar: String? = null
    ) {
        val context = appContext ?: return
        if (!hasNotificationPermission()) return
        if (!shouldShowNotification()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_OPEN_NOTIFICATIONS)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    301,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val avatarBitmap = fetchAvatarBitmap(senderAvatar) ?: createMonogramBitmap(senderName)

                val notification = NotificationCompat.Builder(context, CHANNEL_SOCIAL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(avatarBitmap)
                    .setContentTitle("New Friend Request")
                    .setContentText("$senderName sent you a friend request.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_SOCIAL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setColor(0xFF10B981.toInt())
                    .build()

                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SOCIAL + requestId.hashCode() % 1000, notification)
                Log.d(TAG, "Friend request notification posted with avatar for: $senderName")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to post friend request notification: ${t.message}", t)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun showMissedCallNotification(
        callerName: String,
        callerAvatar: String? = null,
        roomId: String? = null
    ) {
        val context = appContext ?: return
        if (!hasNotificationPermission()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("extra_navigate_tab", "CALLS")
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    401,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val avatarBitmap = fetchAvatarBitmap(callerAvatar) ?: createMonogramBitmap(callerName)

                val notification = NotificationCompat.Builder(context, CHANNEL_CALLS)
                    .setSmallIcon(android.R.drawable.stat_notify_missed_call)
                    .setLargeIcon(avatarBitmap)
                    .setContentTitle("Missed Call")
                    .setSubText("Connecto • Calls")
                    .setContentText("Missed voice call from $callerName")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setColor(0xFFEF4444.toInt())
                    .build()

                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CALL + (callerName.hashCode() % 1000), notification)
                Log.d(TAG, "Missed call notification displayed for: $callerName")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to post missed call notification: ${t.message}", t)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun showGeneralNotification(
        title: String,
        message: String,
        subText: String = "Connecto",
        avatarUrl: String? = null
    ) {
        val context = appContext ?: return
        if (!hasNotificationPermission()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    501,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val avatarBitmap = fetchAvatarBitmap(avatarUrl) ?: createMonogramBitmap(title)

                val notification = NotificationCompat.Builder(context, CHANNEL_SOCIAL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(avatarBitmap)
                    .setContentTitle(title)
                    .setSubText(subText)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_EVENT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setColor(0xFF6C5CE7.toInt())
                    .build()

                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SOCIAL + (title.hashCode() % 1000), notification)
                Log.d(TAG, "General notification displayed: $title")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to post general notification: ${t.message}", t)
            }
        }
    }

    fun cancelCallNotification() {
        val context = appContext ?: return
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_CALL)
            Log.d(TAG, "Call notification canceled")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to cancel call notification: ${t.message}", t)
        }
    }

    fun cancelAll() {
        val context = appContext ?: return
        try {
            NotificationManagerCompat.from(context).cancelAll()
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to cancel all notifications: ${t.message}", t)
        }
    }
}
