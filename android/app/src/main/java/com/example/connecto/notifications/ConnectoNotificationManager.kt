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
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import android.util.LruCache
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.connecto.voice.CallActionReceiver

object ConnectoNotificationManager {
    private const val TAG = "ConnectoNotifManager"
    private val avatarMemoryCache = LruCache<String, Bitmap>(64)

    // Notification Channel IDs
    const val CHANNEL_CALLS = "connecto_voice_calls_v2"
    const val CHANNEL_MESSAGES = "connecto_messages_v4"
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
    // SEC-08 FIX: Internal source marker — prevents external intents from injecting data into SQLite
    const val EXTRA_INTERNAL_SOURCE = "connecto_internal_notification_source"

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

            val callSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val callAudioAttributes = AudioAttributes.Builder()
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
                setSound(callSoundUri, callAudioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            // 2. Messages Channel (High importance, heads-up banner, vibration, sound)
            val notifSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notifAudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build()

            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Chat Messages & Mentions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Direct messages, channel mentions, and squad pings"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 200, 250)
                setSound(notifSoundUri, notifAudioAttributes)
                enableLights(true)
                lightColor = 0xFF6C5CE7.toInt()
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
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

            val channelsToRegister = mutableListOf(callsChannel, messagesChannel, socialChannel)

            // Register dedicated channels for custom trending notification sounds (especially Faah)
            NotificationSoundManager.TRENDING_SOUNDS.forEach { sound ->
                if (sound.id != "system_default") {
                    val sChannelId = "connecto_msg_channel_${sound.id}"
                    val sUri = NotificationSoundManager.getSoundUri(context, sound.id)
                    if (sUri != null) {
                        val sChannel = NotificationChannel(
                            sChannelId,
                            "Chat Messages (${sound.name})",
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = "Chat messages with ${sound.name} sound alert"
                            enableVibration(true)
                            vibrationPattern = longArrayOf(0, 250, 200, 250)
                            setSound(sUri, notifAudioAttributes)
                            enableLights(true)
                            lightColor = 0xFF6C5CE7.toInt()
                            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                            setShowBadge(true)
                        }
                        channelsToRegister.add(sChannel)
                    }
                }
            }

            notificationManager.createNotificationChannels(channelsToRegister)
            Log.d(TAG, "Notification channels registered successfully (including trending sound channels)")
        }
    }

    fun getOrCreateMessageChannelForSound(context: Context, soundId: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return CHANNEL_MESSAGES

            if (soundId == "system_default") {
                return CHANNEL_MESSAGES
            }

            val channelId = "connecto_msg_channel_$soundId"
            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel != null) {
                return channelId
            }

            val sound = NotificationSoundManager.TRENDING_SOUNDS.find { it.id == soundId }
            val soundName = sound?.name ?: "Faah"
            val soundUri = NotificationSoundManager.getSoundUri(context, soundId)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Chat Messages ($soundName)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat messages with $soundName sound alert"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 200, 250)
                setSound(soundUri, audioAttributes)
                enableLights(true)
                lightColor = 0xFF6C5CE7.toInt()
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(channel)
            return channelId
        }
        return CHANNEL_MESSAGES
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
                val avatarBitmap = withTimeoutOrNull(400L) {
                    fetchAvatarBitmap(callerAvatar, callerName, callerUsername)
                } ?: createBadgedAvatarBitmap(context, createMonogramBitmap(callerName))

                val callerPerson = Person.Builder()
                    .setName(callerName)
                    .setIcon(IconCompat.createWithBitmap(avatarBitmap))
                    .setImportant(true)
                    .build()

                val callStyle = NotificationCompat.CallStyle.forIncomingCall(
                    callerPerson,
                    declinePendingIntent,
                    answerPendingIntent
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_CALLS)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setStyle(callStyle)
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
                    .setColor(0xFF6C5CE7.toInt()) // Connecto Brand Accent Purple
                    .build()

                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CALL, notification)
                Log.d(TAG, "Incoming call notification displayed with avatar for: $callerName ($roomId)")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to post incoming call notification: ${t.message}", t)
            }
        }
    }

    fun createEmojiBitmap(emoji: String): Bitmap {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#1A1D24")
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 64f
            textAlign = Paint.Align.CENTER
        }
        val yOffset = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(emoji, size / 2f, yOffset, textPaint)
        return bitmap
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
        val origSize = minOf(bitmap.width, bitmap.height)
        val targetSize = minOf(origSize, 256)
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            color = -0x1
        }
        val rect = Rect(0, 0, targetSize, targetSize)
        val srcRect = Rect(
            (bitmap.width - origSize) / 2,
            (bitmap.height - origSize) / 2,
            (bitmap.width + origSize) / 2,
            (bitmap.height + origSize) / 2
        )
        canvas.drawCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, srcRect, rect, paint)
        return output
    }

    fun createBadgedAvatarBitmap(context: Context, baseAvatar: Bitmap): Bitmap {
        val size = 256
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Draw base circular profile picture scaled to size x size
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val avatarRect = Rect(0, 0, size, size)
        canvas.drawBitmap(baseAvatar, null, avatarRect, paint)

        // 2. Decode Connecto app icon
        val connectoBmp = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.connecto_app_icon)
                ?: BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round)
        } catch (_: Exception) {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        }

        if (connectoBmp != null) {
            val badgeSize = (size * 0.35f).toInt() // ~90px badge
            val badgeX = size - badgeSize - 4f
            val badgeY = size - badgeSize - 4f
            val badgeRadius = badgeSize / 2f
            val badgeCenterX = badgeX + badgeRadius
            val badgeCenterY = badgeY + badgeRadius

            // Outer dark circle border
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#0B0E14")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(badgeCenterX, badgeCenterY, badgeRadius + 4f, borderPaint)

            // Subtle glowing accent ring
            val accentBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#6C5CE7")
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(badgeCenterX, badgeCenterY, badgeRadius + 2f, accentBorderPaint)

            // Clip Connecto icon into circular badge
            val badgeCircleBmp = Bitmap.createBitmap(badgeSize, badgeSize, Bitmap.Config.ARGB_8888)
            val badgeCanvas = Canvas(badgeCircleBmp)
            val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = -0x1
            }
            badgeCanvas.drawCircle(badgeRadius, badgeRadius, badgeRadius, clipPaint)
            clipPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            badgeCanvas.drawBitmap(connectoBmp, null, Rect(0, 0, badgeSize, badgeSize), clipPaint)

            canvas.drawBitmap(badgeCircleBmp, badgeX, badgeY, paint)
        }

        return output
    }

    private suspend fun fetchAvatarBitmap(
        avatarUrl: String?,
        senderName: String? = null,
        senderUsername: String? = null
    ): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = (senderUsername ?: senderName ?: avatarUrl)?.trim()?.lowercase() ?: ""
        if (cacheKey.isNotBlank()) {
            val cached = avatarMemoryCache.get(cacheKey)
            if (cached != null) return@withContext cached
        }

        val context = appContext
        var rawUrl = avatarUrl?.trim()

        val isEmojiOnly = rawUrl != null && !rawUrl.contains("/") && !rawUrl.contains(".") && !rawUrl.startsWith("data:")
        // 1. If avatarUrl is null/blank or emoji, attempt to resolve actual photo from local SQLite cached friends or network profile
        if (rawUrl.isNullOrBlank() || isEmojiOnly) {
            val candidateKey = (senderUsername?.ifBlank { null } ?: senderName?.ifBlank { null })?.trim()?.removePrefix("@")
            if (context != null && !candidateKey.isNullOrBlank()) {
                val friend = com.example.connecto.data.ConnectoDatabaseHelper.getInstance(context)
                    .getFriendByUsernameOrHandle(candidateKey)
                val dbAvatar = friend?.avatarUrl?.trim()
                if (!dbAvatar.isNullOrBlank() && (dbAvatar.contains("/") || dbAvatar.contains(".") || dbAvatar.startsWith("data:"))) {
                    rawUrl = dbAvatar
                }
            }
            if ((rawUrl.isNullOrBlank() || isEmojiOnly) && !candidateKey.isNullOrBlank()) {
                try {
                    val profRes = com.example.connecto.network.ConnectoApiClient.getProfile(candidateKey)
                    if (profRes.isSuccess) {
                        val prof = profRes.getOrNull()
                        if (!prof?.avatarUrl.isNullOrBlank()) {
                            rawUrl = prof?.avatarUrl?.trim()
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        if (rawUrl.isNullOrBlank()) return@withContext null

        // 2. Check for base64 data URL (e.g. data:image/png;base64,...)
        if (rawUrl.startsWith("data:")) {
            try {
                val commaIdx = rawUrl.indexOf(",")
                if (commaIdx != -1) {
                    val base64Data = rawUrl.substring(commaIdx + 1)
                    val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                    if (bytes != null && bytes.isNotEmpty()) {
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null) {
                            val circ = getCircularBitmap(bmp)
                            val badged = if (context != null) createBadgedAvatarBitmap(context, circ) else circ
                            if (cacheKey.isNotBlank()) avatarMemoryCache.put(cacheKey, badged)
                            return@withContext badged
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to decode base64 avatar data: ${e.message}")
            }
        }

        // 3. Check for local file path (e.g. file:// or /data/user/0/...)
        val localFilePath = if (rawUrl.startsWith("file://")) rawUrl.removePrefix("file://") else rawUrl
        val localFile = java.io.File(localFilePath)
        if (localFile.exists() && localFile.isFile && localFile.length() > 0) {
            try {
                val bmp = BitmapFactory.decodeFile(localFile.absolutePath)
                if (bmp != null) {
                    val circ = getCircularBitmap(bmp)
                    val badged = if (context != null) createBadgedAvatarBitmap(context, circ) else circ
                    if (cacheKey.isNotBlank()) avatarMemoryCache.put(cacheKey, badged)
                    return@withContext badged
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to decode local file avatar: ${e.message}")
            }
        }

        // 4. Check for emoji avatar (e.g. 👾)
        if (!rawUrl.contains("/") && !rawUrl.contains(".") && rawUrl.length <= 4) {
            try {
                val emojiBmp = createEmojiBitmap(rawUrl)
                val badged = if (context != null) createBadgedAvatarBitmap(context, emojiBmp) else emojiBmp
                if (cacheKey.isNotBlank()) avatarMemoryCache.put(cacheKey, badged)
                return@withContext badged
            } catch (_: Exception) {}
        }

        // 5. Remote HTTP/HTTPS fetch with reliable 2500ms timeout
        val resolvedBitmap = kotlinx.coroutines.withTimeoutOrNull(2500L) {
            val candidateUrls = linkedSetOf<String>()
            val activeBase = com.example.connecto.network.ConnectoNetworkConfig.activeBaseUrl.trimEnd('/')

            if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                candidateUrls.add(rawUrl)
            } else {
                val cleanPath = rawUrl.trimStart('/')
                if (activeBase.startsWith("http")) {
                    candidateUrls.add("$activeBase/$cleanPath")
                }
                candidateUrls.add("https://connecto.fun/$cleanPath")
            }

            // Attempt 5A: Coil ImageLoader (fast disk/memory cache)
            if (context != null) {
                for (targetUrl in candidateUrls) {
                    try {
                        val request = coil.request.ImageRequest.Builder(context)
                            .data(targetUrl)
                            .allowHardware(false)
                            .size(256, 256)
                            .build()
                        val result = coil.Coil.imageLoader(context).execute(request)
                        if (result is coil.request.SuccessResult) {
                            val drawable = result.drawable
                            val bmp = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                            if (bmp != null) {
                                val circ = getCircularBitmap(bmp)
                                return@withTimeoutOrNull createBadgedAvatarBitmap(context, circ)
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
            null
        }

        if (resolvedBitmap != null && cacheKey.isNotBlank()) {
            avatarMemoryCache.put(cacheKey, resolvedBitmap)
        }
        resolvedBitmap
    }

    @SuppressLint("MissingPermission")
    fun showMessageNotification(
        senderName: String,
        messageText: String,
        channelOrDmId: String,
        senderAvatar: String? = null,
        senderUsername: String? = null
    ) {
        val context = appContext ?: return
        if (!hasNotificationPermission()) return
        if (!shouldShowNotification(channelOrDmId)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val avatarBitmap = fetchAvatarBitmap(senderAvatar, senderName, senderUsername)
                    ?: createBadgedAvatarBitmap(context, createMonogramBitmap(senderName))

                val intent = Intent(context, MainActivity::class.java).apply {
                    action = ACTION_OPEN_CHAT
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_OPEN_CHAT)
                    putExtra(EXTRA_CHANNEL_ID, channelOrDmId)
                    putExtra(EXTRA_INTERNAL_SOURCE, true) // SEC-08 FIX: Mark as trusted internal notification
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    201,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val isDm = channelOrDmId.startsWith("dm-") || channelOrDmId.startsWith("dm_") || !channelOrDmId.contains("#")
                val subText = if (isDm) "Connecto • DM" else "Connecto • #${channelOrDmId.removePrefix("#")}"

                // In Android MessagingStyle, user is the CURRENT (local) user; senderPerson is the remote sender.
                val myUsername = (com.example.connecto.network.ConnectoApiClient.currentUsername ?: "").trim().removePrefix("@")
                val myDisplayName = (com.example.connecto.network.ConnectoApiClient.currentUserDisplayName ?: "").ifBlank { myUsername.ifEmpty { "Me" } }

                val localUser = Person.Builder()
                    .setName(myDisplayName)
                    .setKey(myUsername.ifEmpty { "self" })
                    .build()

                val senderPersonKey = (senderUsername ?: senderName).trim().lowercase().removePrefix("@")
                val senderPerson = Person.Builder()
                    .setName(senderName)
                    .setKey(senderPersonKey.ifBlank { "sender" })
                    .setIcon(IconCompat.createWithBitmap(avatarBitmap))
                    .build()

                val dbHelper = com.example.connecto.data.ConnectoDatabaseHelper.getInstance(context)
                val recentMessages = try {
                    if (isDm) {
                        dbHelper.getDirectMessages(myUsername, senderPersonKey, limit = 5)
                    } else {
                        dbHelper.getMessagesForChannel(channelOrDmId, limit = 5)
                    }
                } catch (_: Exception) {
                    emptyList()
                }

                val messagingStyle = NotificationCompat.MessagingStyle(localUser)
                    .setConversationTitle(if (isDm) null else "#${channelOrDmId.removePrefix("#")}")
                    .setGroupConversation(!isDm)

                // Build history messages in chronological order (oldest to newest)
                val historyList = if (recentMessages.isNotEmpty()) {
                    recentMessages.sortedBy { it.createdAt }
                        .filter { it.content.trim() != messageText.trim() }
                        .takeLast(4)
                } else emptyList()

                historyList.forEach { m ->
                    val isMe = m.authorId.equals(myUsername, ignoreCase = true) || m.authorName.equals(myUsername, ignoreCase = true)
                    val authorPerson = if (isMe) localUser else {
                        Person.Builder()
                            .setName(m.authorName.ifBlank { senderName })
                            .setKey(m.authorId.lowercase().removePrefix("@"))
                            .setIcon(IconCompat.createWithBitmap(avatarBitmap))
                            .build()
                    }
                    val ts = try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                        sdf.parse(m.createdAt.substringBefore("."))?.time ?: System.currentTimeMillis()
                    } catch (_: Exception) {
                        System.currentTimeMillis()
                    }
                    messagingStyle.addMessage(m.content, ts, authorPerson)
                }

                // ALWAYS append the actual current incoming message as the latest message in messagingStyle
                messagingStyle.addMessage(messageText, System.currentTimeMillis(), senderPerson)

                val isContentHidden = NotificationPreferences.isHideContentEnabled(context)
                val displayTitle = if (isContentHidden) "Connecto" else senderName
                val displayMessage = if (isContentHidden) "New message received" else messageText
                val displaySubText = if (isContentHidden) "Connecto • Private" else subText

                val notifId = NOTIFICATION_ID_MESSAGE + (Math.abs(channelOrDmId.hashCode()) % 1000)

                // Inline Direct Reply RemoteInput (just like WhatsApp)
                val remoteInput = androidx.core.app.RemoteInput.Builder(DirectReplyReceiver.KEY_TEXT_REPLY)
                    .setLabel("Reply to $senderName...")
                    .build()

                val replyIntent = Intent(context, DirectReplyReceiver::class.java).apply {
                    setPackage(context.packageName)
                    action = DirectReplyReceiver.ACTION_DIRECT_REPLY
                    putExtra(EXTRA_CHANNEL_ID, channelOrDmId)
                    putExtra("extra_sender_name", senderName)
                    putExtra("extra_sender_username", senderUsername ?: senderName)
                    putExtra("extra_notification_id", notifId)
                }
                val replyPendingIntent = PendingIntent.getBroadcast(
                    context,
                    notifId + 10,
                    replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                val replyAction = NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_send,
                    "Reply",
                    replyPendingIntent
                ).addRemoteInput(remoteInput)
                    .setAllowGeneratedReplies(true)
                    .build()

                val markReadIntent = Intent(context, DirectReplyReceiver::class.java).apply {
                    action = DirectReplyReceiver.ACTION_MARK_AS_READ
                    putExtra(EXTRA_CHANNEL_ID, channelOrDmId)
                    putExtra("extra_sender_username", senderUsername ?: senderName)
                    putExtra("extra_notification_id", notifId)
                }
                val markReadPendingIntent = PendingIntent.getBroadcast(
                    context,
                    notifId + 20,
                    markReadIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val markReadAction = NotificationCompat.Action.Builder(
                    android.R.drawable.checkbox_on_background,
                    "Mark as read",
                    markReadPendingIntent
                ).build()

                val selectedSoundId = NotificationPreferences.getSelectedSoundId(context)
                val targetChannelId = getOrCreateMessageChannelForSound(context, selectedSoundId)
                val soundUri = NotificationSoundManager.getSoundUri(context, selectedSoundId)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val vibrationPattern = longArrayOf(0, 250, 200, 250)

                val builder = NotificationCompat.Builder(context, targetChannelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(avatarBitmap)
                    .setContentTitle(displayTitle)
                    .setSubText(displaySubText)
                    .setContentText(displayMessage)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setColor(0xFF6C5CE7.toInt()) // Connecto Brand Accent Purple
                    .setVisibility(if (isContentHidden) NotificationCompat.VISIBILITY_PRIVATE else NotificationCompat.VISIBILITY_PUBLIC)
                    .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                    .setSound(soundUri)
                    .setVibrate(vibrationPattern)
                    .addAction(replyAction)
                    .addAction(markReadAction)

                if (isDm && !isContentHidden) {
                    try {
                        val shortcutId = "dm_${senderPersonKey.ifBlank { "user" }}"
                        val shortcutIntent = Intent(Intent.ACTION_VIEW).apply {
                            setClass(context, MainActivity::class.java)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(EXTRA_NOTIFICATION_ACTION, ACTION_OPEN_CHAT)
                            putExtra(EXTRA_CHANNEL_ID, channelOrDmId)
                            putExtra(EXTRA_INTERNAL_SOURCE, true) // SEC-08 FIX
                        }
                        val shortcut = ShortcutInfoCompat.Builder(context, shortcutId)
                            .setShortLabel(senderName)
                            .setLongLabel(senderName)
                            .setIcon(IconCompat.createWithBitmap(avatarBitmap))
                            .setIntent(shortcutIntent)
                            .setLongLived(true)
                            .setPerson(senderPerson)
                            .build()
                        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
                        builder.setShortcutId(shortcutId)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to push conversation shortcut: ${e.message}", e)
                    }
                }

                if (!isContentHidden) {
                    builder.setStyle(messagingStyle)
                } else {
                    val publicNotif = NotificationCompat.Builder(context, targetChannelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Connecto")
                        .setContentText("New message received")
                        .build()
                    builder.setPublicVersion(publicNotif)
                }

                val notification = builder.build()
                NotificationManagerCompat.from(context).notify(notifId, notification)
                Log.d(TAG, "Message notification posted with heads-up banner & reply for: $senderName (notifId=$notifId)")

                // Play chosen trending notification sound
                NotificationSoundManager.playNotificationSound(context)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to post message notification: ${t.message}", t)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun showFriendRequestNotification(
        senderName: String,
        requestId: String,
        senderAvatar: String? = null,
        senderUsername: String? = null
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

                val avatarBitmap = fetchAvatarBitmap(senderAvatar, senderName, senderUsername)
                    ?: createBadgedAvatarBitmap(context, createMonogramBitmap(senderName))

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
        roomId: String? = null,
        callerUsername: String? = null
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

                val avatarBitmap = fetchAvatarBitmap(callerAvatar, callerName, callerUsername)
                    ?: createBadgedAvatarBitmap(context, createMonogramBitmap(callerName))

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
        avatarUrl: String? = null,
        username: String? = null
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

                val avatarBitmap = fetchAvatarBitmap(avatarUrl, title, username)
                    ?: createBadgedAvatarBitmap(context, createMonogramBitmap(title))

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
