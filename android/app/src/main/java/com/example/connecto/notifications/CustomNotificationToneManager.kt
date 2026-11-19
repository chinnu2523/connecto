package com.example.connecto.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import com.example.connecto.security.SecretVaultManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Model representing a selectable notification tone.
 */
data class NotificationToneItem(
    val id: String,
    val title: String,
    val uriString: String,
    val type: String // "default", "custom", "system", "silent"
)

/**
 * Model representing a vibration pattern.
 */
data class VibrationPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val pattern: LongArray?
)

/**
 * Model representing a per-contact customized notification tone & vibration override.
 */
data class ContactNotificationOverride(
    val contactId: String,
    val contactName: String = "",
    val contactAvatar: String? = null,
    val toneType: String = CustomNotificationToneManager.TONE_TYPE_DEFAULT,
    val toneName: String = CustomNotificationToneManager.DEFAULT_TONE_NAME,
    val toneUri: String = "",
    val toneLocalPath: String = "",
    val vibrationPresetId: String = CustomNotificationToneManager.VIBRATION_DEFAULT,
    val customVibrationTimings: String = "" // comma-separated timings e.g. "0,80,70,80"
)

/**
 * Manager for custom message notification sounds, tactile vibrations, and Android 15 versioned channels.
 */
object CustomNotificationToneManager {
    private const val TAG = "CustomNotifToneManager"
    private const val PREFS_NAME = "connecto_notification_tone_prefs"

    const val TONE_TYPE_DEFAULT = "default"
    const val TONE_TYPE_CUSTOM = "custom"
    const val TONE_TYPE_SYSTEM = "system"
    const val TONE_TYPE_SILENT = "silent"

    const val DEFAULT_TONE_NAME = "Connecto Chime"

    const val VIBRATION_DEFAULT = "default"
    const val VIBRATION_DOUBLE_TAP = "double_tap"
    const val VIBRATION_HEARTBEAT = "heartbeat"
    const val VIBRATION_TRIPLE_BUZZ = "triple_buzz"
    const val VIBRATION_LONG_PULSE = "long_pulse"
    const val VIBRATION_OFF = "off"
    const val VIBRATION_CUSTOM = "custom"

    // Canonical Curated Presets
    val VIBRATION_PRESETS = listOf(
        VibrationPreset(
            id = VIBRATION_DEFAULT,
            title = "Default",
            subtitle = "Standard single pulse alert",
            pattern = longArrayOf(0, 250)
        ),
        VibrationPreset(
            id = VIBRATION_DOUBLE_TAP,
            title = "Double Tap",
            subtitle = "Crisp rhythmic double-tap",
            pattern = longArrayOf(0, 80, 70, 80)
        ),
        VibrationPreset(
            id = VIBRATION_HEARTBEAT,
            title = "Heartbeat",
            subtitle = "Subtle organic pulse pair",
            pattern = longArrayOf(0, 90, 90, 180)
        ),
        VibrationPreset(
            id = VIBRATION_TRIPLE_BUZZ,
            title = "Triple Buzz",
            subtitle = "Quick three-burst alert",
            pattern = longArrayOf(0, 60, 60, 60, 60, 60)
        ),
        VibrationPreset(
            id = VIBRATION_LONG_PULSE,
            title = "Long Pulse",
            subtitle = "Solid extended 350ms buzz",
            pattern = longArrayOf(0, 350)
        ),
        VibrationPreset(
            id = VIBRATION_OFF,
            title = "Off",
            subtitle = "Zero vibration (silent tactile)",
            pattern = null
        )
    )

    private const val KEY_GLOBAL_TONE_TYPE = "key_global_tone_type"
    private const val KEY_GLOBAL_TONE_NAME = "key_global_tone_name"
    private const val KEY_GLOBAL_TONE_URI = "key_global_tone_uri"
    private const val KEY_GLOBAL_TONE_PATH = "key_global_tone_path"

    private const val KEY_GLOBAL_VIBRATION_ID = "key_global_vibration_id"
    private const val KEY_GLOBAL_CUSTOM_VIBRATION = "key_global_custom_vibration"

    private const val KEY_GLOBAL_CHANNEL_VERSION = "key_global_channel_version"
    private const val PREFIX_CONTACT_CHANNEL_VERSION = "key_contact_chan_ver_"
    private const val PREFIX_CONTACT_OVERRIDE = "key_contact_override_"

    // Preview state
    private var previewMediaPlayer: MediaPlayer? = null
    private var previewJob: Job? = null
    private val _currentlyPlayingToneId = MutableStateFlow<String?>(null)
    val currentlyPlayingToneId: StateFlow<String?> = _currentlyPlayingToneId.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun cleanContactId(id: String): String {
        return id.trim().lowercase().removePrefix("@").removePrefix("dm-").replace(Regex("[^a-z0-9_-]"), "_")
    }

    // =========================================================================
    // Global Settings Accessors
    // =========================================================================

    fun getGlobalToneType(context: Context): String {
        return getPrefs(context).getString(KEY_GLOBAL_TONE_TYPE, TONE_TYPE_DEFAULT) ?: TONE_TYPE_DEFAULT
    }

    fun getGlobalToneName(context: Context): String {
        return getPrefs(context).getString(KEY_GLOBAL_TONE_NAME, DEFAULT_TONE_NAME) ?: DEFAULT_TONE_NAME
    }

    fun getGlobalToneUri(context: Context): String {
        return getPrefs(context).getString(KEY_GLOBAL_TONE_URI, "") ?: ""
    }

    fun getGlobalTonePath(context: Context): String {
        return getPrefs(context).getString(KEY_GLOBAL_TONE_PATH, "") ?: ""
    }

    fun setGlobalTone(context: Context, type: String, name: String, uri: String, localPath: String = "") {
        getPrefs(context).edit()
            .putString(KEY_GLOBAL_TONE_TYPE, type)
            .putString(KEY_GLOBAL_TONE_NAME, name)
            .putString(KEY_GLOBAL_TONE_URI, uri)
            .putString(KEY_GLOBAL_TONE_PATH, localPath)
            .apply()
        // Recreate versioned global notification channel
        recreateGlobalMessageChannel(context)
        Log.d(TAG, "Global tone updated: type=$type, name=$name, uri=$uri, path=$localPath")
    }

    fun getGlobalVibrationId(context: Context): String {
        return getPrefs(context).getString(KEY_GLOBAL_VIBRATION_ID, VIBRATION_DEFAULT) ?: VIBRATION_DEFAULT
    }

    fun getGlobalCustomVibrationTimings(context: Context): String {
        return getPrefs(context).getString(KEY_GLOBAL_CUSTOM_VIBRATION, "") ?: ""
    }

    fun setGlobalVibration(context: Context, presetId: String, customTimings: String = "") {
        getPrefs(context).edit()
            .putString(KEY_GLOBAL_VIBRATION_ID, presetId)
            .putString(KEY_GLOBAL_CUSTOM_VIBRATION, customTimings)
            .apply()
        recreateGlobalMessageChannel(context)
        Log.d(TAG, "Global vibration updated: presetId=$presetId, customTimings=$customTimings")
    }

    fun getGlobalVibrationPattern(context: Context): LongArray? {
        val presetId = getGlobalVibrationId(context)
        if (presetId == VIBRATION_CUSTOM) {
            val timingsStr = getGlobalCustomVibrationTimings(context)
            return parseTimingsString(timingsStr)
        }
        return VIBRATION_PRESETS.find { it.id == presetId }?.pattern
    }

    fun getGlobalVibrationDisplayName(context: Context): String {
        val presetId = getGlobalVibrationId(context)
        if (presetId == VIBRATION_CUSTOM) return "Custom Pattern"
        return VIBRATION_PRESETS.find { it.id == presetId }?.title ?: "Default"
    }

    // =========================================================================
    // Contact Override Accessors & Clean File Hygiene
    // =========================================================================

    fun getContactOverride(context: Context, contactId: String): ContactNotificationOverride? {
        val clean = cleanContactId(contactId)
        if (clean.isBlank()) return null
        val rawJson = getPrefs(context).getString("$PREFIX_CONTACT_OVERRIDE$clean", null) ?: return null
        return try {
            val obj = JSONObject(rawJson)
            ContactNotificationOverride(
                contactId = obj.optString("contactId", clean),
                contactName = obj.optString("contactName", ""),
                contactAvatar = obj.optString("contactAvatar").takeIf { it.isNotBlank() },
                toneType = obj.optString("toneType", TONE_TYPE_DEFAULT),
                toneName = obj.optString("toneName", DEFAULT_TONE_NAME),
                toneUri = obj.optString("toneUri", ""),
                toneLocalPath = obj.optString("toneLocalPath", ""),
                vibrationPresetId = obj.optString("vibrationPresetId", VIBRATION_DEFAULT),
                customVibrationTimings = obj.optString("customVibrationTimings", "")
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Error parsing contact override for $contactId: ${t.message}")
            null
        }
    }

    fun getAllContactOverrides(context: Context): List<ContactNotificationOverride> {
        val list = mutableListOf<ContactNotificationOverride>()
        try {
            val all = getPrefs(context).all
            for ((key, value) in all) {
                if (key.startsWith(PREFIX_CONTACT_OVERRIDE) && value is String) {
                    try {
                        val obj = JSONObject(value)
                        list.add(
                            ContactNotificationOverride(
                                contactId = obj.optString("contactId"),
                                contactName = obj.optString("contactName"),
                                contactAvatar = obj.optString("contactAvatar").takeIf { it.isNotBlank() },
                                toneType = obj.optString("toneType", TONE_TYPE_DEFAULT),
                                toneName = obj.optString("toneName", DEFAULT_TONE_NAME),
                                toneUri = obj.optString("toneUri", ""),
                                toneLocalPath = obj.optString("toneLocalPath", ""),
                                vibrationPresetId = obj.optString("vibrationPresetId", VIBRATION_DEFAULT),
                                customVibrationTimings = obj.optString("customVibrationTimings", "")
                            )
                        )
                    } catch (_: Throwable) {}
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error querying all contact overrides: ${t.message}")
        }
        return list.sortedBy { it.contactName.ifBlank { it.contactId }.lowercase() }
    }

    fun saveContactOverride(context: Context, override: ContactNotificationOverride) {
        val clean = cleanContactId(override.contactId)
        if (clean.isBlank()) return
        try {
            val obj = JSONObject().apply {
                put("contactId", override.contactId)
                put("contactName", override.contactName)
                put("contactAvatar", override.contactAvatar ?: "")
                put("toneType", override.toneType)
                put("toneName", override.toneName)
                put("toneUri", override.toneUri)
                put("toneLocalPath", override.toneLocalPath)
                put("vibrationPresetId", override.vibrationPresetId)
                put("customVibrationTimings", override.customVibrationTimings)
            }
            getPrefs(context).edit().putString("$PREFIX_CONTACT_OVERRIDE$clean", obj.toString()).apply()
            recreateContactMessageChannel(context, override.contactId, override.contactName)
            Log.d(TAG, "Saved contact override for ${override.contactId}: ${override.toneName} · ${override.vibrationPresetId}")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed saving contact override for ${override.contactId}: ${t.message}")
        }
    }

    fun removeContactOverride(context: Context, contactId: String) {
        stopPreview()
        val clean = cleanContactId(contactId)
        if (clean.isBlank()) return
        // Delete audio files
        deleteContactToneFiles(context, clean)
        // Delete dedicated versioned channel
        deleteContactMessageChannel(context, contactId)
        // Remove from preferences
        getPrefs(context).edit().remove("$PREFIX_CONTACT_OVERRIDE$clean").apply()
        Log.d(TAG, "Removed contact notification override for $contactId (reverted to global)")
    }

    private fun deleteContactToneFiles(context: Context, cleanContactId: String) {
        try {
            val dir = File(context.filesDir, "notification_tones")
            if (dir.exists()) {
                val prefix = "contact_${cleanContactId}."
                dir.listFiles { f -> f.name.startsWith(prefix) }?.forEach { f ->
                    val deleted = f.delete()
                    Log.d(TAG, "Deleted prior contact tone file: ${f.name} (success=$deleted)")
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error deleting contact tone files: ${t.message}")
        }
    }

    // =========================================================================
    // Custom Audio File Import & Android 15 FileProvider SystemUI Grant
    // =========================================================================

    data class ToneSaveResult(
        val displayName: String,
        val localPath: String,
        val isLongAudio: Boolean // > 10s hint
    )

    suspend fun saveCustomToneFile(
        context: Context,
        sourceUri: Uri,
        contactId: String? = null
    ): Result<ToneSaveResult> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val rawName = queryDisplayName(context, sourceUri) ?: "custom_tone.mp3"
            val ext = rawName.substringAfterLast('.', "mp3")

            val dir = File(context.filesDir, "notification_tones").apply {
                if (!exists()) mkdirs()
            }

            val destFile: File
            if (contactId != null) {
                val clean = cleanContactId(contactId)
                deleteContactToneFiles(context, clean)
                destFile = File(dir, "contact_${clean}.$ext")
            } else {
                dir.listFiles { f -> f.name.startsWith("global.") }?.forEach { it.delete() }
                destFile = File(dir, "global.$ext")
            }

            // Copy stream with 5 MB max limit
            resolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(16384)
                    var totalBytes = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        totalBytes += read
                        if (totalBytes > 5 * 1024 * 1024L) { // 5 MB max limit
                            destFile.delete()
                            return@withContext Result.failure(IllegalStateException("Audio file exceeds maximum 5 MB limit."))
                        }
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            } ?: return@withContext Result.failure(IllegalStateException("Could not open audio file stream."))

            // Validate audio can prepare
            var testMp: MediaPlayer? = null
            try {
                testMp = MediaPlayer().apply {
                    setDataSource(destFile.absolutePath)
                    prepare()
                }
            } catch (t: Throwable) {
                destFile.delete()
                return@withContext Result.failure(IllegalStateException("Corrupt or unsupported audio format."))
            } finally {
                try { testMp?.release() } catch (_: Throwable) {}
            }

            // Check duration (> 10s hint)
            var isLong = false
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(destFile.absolutePath)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 0L
                isLong = durationMs > 10_000L
                retriever.release()
            } catch (_: Throwable) {}

            // Android 15 FileProvider SystemUI Grant
            try {
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destFile
                )
                context.grantUriPermission(
                    "com.android.systemui",
                    contentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Log.d(TAG, "Granted SystemUI read permission for contentUri: $contentUri")
            } catch (t: Throwable) {
                Log.w(TAG, "Failed granting SystemUI permission on contentUri: ${t.message}")
            }

            Result.success(
                ToneSaveResult(
                    displayName = rawName,
                    localPath = destFile.absolutePath,
                    isLongAudio = isLong
                )
            )
        } catch (t: Throwable) {
            Log.e(TAG, "saveCustomToneFile failed: ${t.message}", t)
            Result.failure(t)
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) return name
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
        return uri.lastPathSegment
    }

    // =========================================================================
    // System Notification Tones Querying
    // =========================================================================

    fun getSystemNotificationTones(context: Context): List<NotificationToneItem> {
        val list = mutableListOf<NotificationToneItem>()
        try {
            val manager = RingtoneManager(context).apply {
                setType(RingtoneManager.TYPE_NOTIFICATION)
            }
            val cursor: Cursor? = manager.cursor
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                    val uri = manager.getRingtoneUri(cursor.position)
                    if (uri != null) {
                        list.add(
                            NotificationToneItem(
                                id = uri.toString(),
                                title = title ?: "System Tone",
                                uriString = uri.toString(),
                                type = TONE_TYPE_SYSTEM
                            )
                        )
                    }
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error querying system notification tones: ${t.message}")
        }
        return list
    }

    // =========================================================================
    // Audio Preview & Tactile Vibration Trigger
    // =========================================================================

    fun playPreview(context: Context, toneItem: NotificationToneItem) {
        stopPreview()
        if (toneItem.type == TONE_TYPE_SILENT) return

        _currentlyPlayingToneId.value = toneItem.id

        previewJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    when (toneItem.type) {
                        TONE_TYPE_CUSTOM -> {
                            setDataSource(toneItem.uriString)
                        }
                        TONE_TYPE_SYSTEM -> {
                            setDataSource(context, Uri.parse(toneItem.uriString))
                        }
                        else -> {
                            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            setDataSource(context, defaultUri)
                        }
                    }
                    prepare()
                    start()
                }
                previewMediaPlayer = mp

                // Auto-stop preview after 5 seconds
                delay(5_000L)
                stopPreview()
            } catch (t: Throwable) {
                Log.e(TAG, "Error playing tone preview for ${toneItem.title}: ${t.message}")
                stopPreview()
            }
        }
    }

    fun stopPreview() {
        previewJob?.cancel()
        previewJob = null
        previewMediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) mp.stop()
                mp.reset()
                mp.release()
            } catch (_: Throwable) {}
        }
        previewMediaPlayer = null
        _currentlyPlayingToneId.value = null
    }

    /**
     * Triggers tactile vibration on-device so user can immediately feel the pattern.
     */
    fun triggerVibration(context: Context, pattern: LongArray?) {
        if (pattern == null || pattern.isEmpty()) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
            Log.d(TAG, "Triggered tactile vibration pattern of size ${pattern.size}")
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to trigger vibration: ${t.message}")
        }
    }

    fun parseTimingsString(timings: String): LongArray? {
        val clean = timings.trim()
        if (clean.isBlank()) return null
        return try {
            clean.split(",").map { it.trim().toLong() }.toLongArray()
        } catch (_: Throwable) {
            null
        }
    }

    fun formatTimingsArray(pattern: LongArray?): String {
        return pattern?.joinToString(",") ?: ""
    }

    // =========================================================================
    // Android 15 Versioned NotificationChannel Manager
    // =========================================================================

    private fun getNotificationManager(context: Context): NotificationManager? {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    fun getGlobalChannelVersion(context: Context): Int {
        return getPrefs(context).getInt(KEY_GLOBAL_CHANNEL_VERSION, 1)
    }

    fun getContactChannelVersion(context: Context, contactId: String): Int {
        val clean = cleanContactId(contactId)
        return getPrefs(context).getInt("$PREFIX_CONTACT_CHANNEL_VERSION$clean", 1)
    }

    fun getGlobalChannelId(context: Context): String {
        return "msg_global_v${getGlobalChannelVersion(context)}"
    }

    fun getContactChannelId(context: Context, contactId: String): String {
        val clean = cleanContactId(contactId)
        return "msg_contact_${clean}_v${getContactChannelVersion(context, contactId)}"
    }

    fun recreateGlobalMessageChannel(context: Context): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "msg_global_v1"
        val nm = getNotificationManager(context) ?: return "msg_global_v1"

        val oldVer = getGlobalChannelVersion(context)
        val oldChannelId = "msg_global_v$oldVer"

        // Increment version
        val newVer = oldVer + 1
        getPrefs(context).edit().putInt(KEY_GLOBAL_CHANNEL_VERSION, newVer).apply()
        val newChannelId = "msg_global_v$newVer"

        // Delete old channel
        try {
            nm.deleteNotificationChannel(oldChannelId)
            Log.d(TAG, "Deleted old global notification channel: $oldChannelId")
        } catch (_: Throwable) {}

        // Resolve Sound & AudioAttributes
        val soundUri = resolveGlobalSoundUri(context)
        val pattern = getGlobalVibrationPattern(context)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channel = NotificationChannel(
            newChannelId,
            "Direct Messages",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Direct 1-on-1 private messages and chat alerts"
            if (pattern != null && pattern.isNotEmpty()) {
                enableVibration(true)
                vibrationPattern = pattern
            } else {
                enableVibration(false)
                vibrationPattern = null
            }
            setSound(soundUri, audioAttributes)
            enableLights(true)
            lightColor = 0xFF6366F1.toInt()
            setShowBadge(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        try {
            nm.createNotificationChannel(channel)
            Log.d(TAG, "Created versioned global channel: $newChannelId (sound=$soundUri, vib=${pattern?.contentToString()})")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed creating channel $newChannelId: ${t.message}", t)
        }
        return newChannelId
    }

    fun recreateContactMessageChannel(context: Context, contactId: String, contactName: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "msg_contact_${cleanContactId(contactId)}_v1"
        val nm = getNotificationManager(context) ?: return "msg_contact_${cleanContactId(contactId)}_v1"

        val clean = cleanContactId(contactId)
        val oldVer = getContactChannelVersion(context, contactId)
        val oldChannelId = "msg_contact_${clean}_v$oldVer"

        // Increment version
        val newVer = oldVer + 1
        getPrefs(context).edit().putInt("$PREFIX_CONTACT_CHANNEL_VERSION$clean", newVer).apply()
        val newChannelId = "msg_contact_${clean}_v$newVer"

        // Delete old channel
        try {
            nm.deleteNotificationChannel(oldChannelId)
            Log.d(TAG, "Deleted old contact notification channel: $oldChannelId")
        } catch (_: Throwable) {}

        val override = getContactOverride(context, contactId)
        val soundUri = resolveContactSoundUri(context, override)
        val pattern = resolveContactVibrationPattern(context, override)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val displayName = contactName.ifBlank { "@$contactId" }
        val channel = NotificationChannel(
            newChannelId,
            "Messages from $displayName",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Custom notifications for $displayName"
            if (pattern != null && pattern.isNotEmpty()) {
                enableVibration(true)
                vibrationPattern = pattern
            } else {
                enableVibration(false)
                vibrationPattern = null
            }
            setSound(soundUri, audioAttributes)
            enableLights(true)
            lightColor = 0xFF6366F1.toInt()
            setShowBadge(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        try {
            nm.createNotificationChannel(channel)
            Log.d(TAG, "Created versioned contact channel: $newChannelId for $displayName")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed creating contact channel $newChannelId: ${t.message}", t)
        }
        return newChannelId
    }

    fun deleteContactMessageChannel(context: Context, contactId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getNotificationManager(context) ?: return
        val clean = cleanContactId(contactId)
        val ver = getContactChannelVersion(context, contactId)
        val channelId = "msg_contact_${clean}_v$ver"
        try {
            nm.deleteNotificationChannel(channelId)
            Log.d(TAG, "Deleted contact notification channel: $channelId")
        } catch (t: Throwable) {
            Log.w(TAG, "Error deleting contact channel: ${t.message}")
        }
    }

    private fun resolveGlobalSoundUri(context: Context): Uri? {
        val type = getGlobalToneType(context)
        if (type == TONE_TYPE_SILENT) return null

        if (type == TONE_TYPE_CUSTOM) {
            val path = getGlobalTonePath(context)
            if (path.isNotBlank()) {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    return try {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        context.grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        uri
                    } catch (_: Throwable) {
                        Uri.fromFile(file)
                    }
                }
            }
        } else if (type == TONE_TYPE_SYSTEM) {
            val uriStr = getGlobalToneUri(context)
            if (uriStr.isNotBlank()) return Uri.parse(uriStr)
        }

        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    private fun resolveContactSoundUri(context: Context, override: ContactNotificationOverride?): Uri? {
        if (override == null) return resolveGlobalSoundUri(context)
        if (override.toneType == TONE_TYPE_SILENT) return null

        if (override.toneType == TONE_TYPE_CUSTOM && override.toneLocalPath.isNotBlank()) {
            val file = File(override.toneLocalPath)
            if (file.exists() && file.canRead()) {
                return try {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    context.grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    uri
                } catch (_: Throwable) {
                    Uri.fromFile(file)
                }
            }
        } else if (override.toneType == TONE_TYPE_SYSTEM && override.toneUri.isNotBlank()) {
            return Uri.parse(override.toneUri)
        }

        return resolveGlobalSoundUri(context)
    }

    private fun resolveContactVibrationPattern(context: Context, override: ContactNotificationOverride?): LongArray? {
        if (override == null) return getGlobalVibrationPattern(context)
        if (override.vibrationPresetId == VIBRATION_OFF) return null
        if (override.vibrationPresetId == VIBRATION_CUSTOM) {
            return parseTimingsString(override.customVibrationTimings) ?: getGlobalVibrationPattern(context)
        }
        val preset = VIBRATION_PRESETS.find { it.id == override.vibrationPresetId }
        return preset?.pattern ?: getGlobalVibrationPattern(context)
    }

    // =========================================================================
    // Safe Routing Precedence on Incoming Message
    // =========================================================================

    data class ResolvedMessageRoute(
        val channelId: String,
        val soundUri: Uri?,
        val vibrationPattern: LongArray?,
        val isMutedOrSilent: Boolean,
        val isVaultPrivate: Boolean
    )

    fun resolveRouting(
        context: Context,
        conversationId: String?,
        senderUsername: String?,
        isDm: Boolean,
        isSilent: Boolean,
        isLocked: Boolean
    ): ResolvedMessageRoute {
        // 1. Silent message (hold-to-send isSilent: true)
        if (isSilent) {
            return ResolvedMessageRoute(
                channelId = ConnectoNotificationManager.CHANNEL_SILENT_MESSAGES,
                soundUri = null,
                vibrationPattern = null,
                isMutedOrSilent = true,
                isVaultPrivate = false
            )
        }

        // 2. Secret Vault / Locked Chat: NEVER create a named contact channel
        val isHiddenInVault = SecretVaultManager.isContactHiddenInVault(context, conversationId, senderUsername)
        if (isLocked || isHiddenInVault) {
            return ResolvedMessageRoute(
                channelId = "msg_vault_private",
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                vibrationPattern = longArrayOf(0, 250),
                isMutedOrSilent = false,
                isVaultPrivate = true
            )
        }

        // 3. Contact Override (if configured)
        val candidateContactId = (if (isDm) senderUsername ?: conversationId else null)?.trim()?.lowercase()?.removePrefix("@")
        if (!candidateContactId.isNullOrBlank()) {
            val override = getContactOverride(context, candidateContactId)
            if (override != null) {
                val soundUri = resolveContactSoundUri(context, override)
                val pattern = resolveContactVibrationPattern(context, override)
                val channelId = ensureContactChannelExists(context, candidateContactId, override.contactName, soundUri, pattern)
                return ResolvedMessageRoute(
                    channelId = channelId,
                    soundUri = soundUri,
                    vibrationPattern = pattern,
                    isMutedOrSilent = override.toneType == TONE_TYPE_SILENT && override.vibrationPresetId == VIBRATION_OFF,
                    isVaultPrivate = false
                )
            }
        }

        // 4. Global Message Channel
        val globalSoundUri = resolveGlobalSoundUri(context)
        val globalPattern = getGlobalVibrationPattern(context)
        val globalChannelId = ensureGlobalChannelExists(context, globalSoundUri, globalPattern)

        return ResolvedMessageRoute(
            channelId = globalChannelId,
            soundUri = globalSoundUri,
            vibrationPattern = globalPattern,
            isMutedOrSilent = getGlobalToneType(context) == TONE_TYPE_SILENT && getGlobalVibrationId(context) == VIBRATION_OFF,
            isVaultPrivate = false
        )
    }

    private fun ensureGlobalChannelExists(context: Context, soundUri: Uri?, pattern: LongArray?): String {
        val channelId = getGlobalChannelId(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getNotificationManager(context) ?: return channelId
            if (nm.getNotificationChannel(channelId) == null) {
                return recreateGlobalMessageChannel(context)
            }
        }
        return channelId
    }

    private fun ensureContactChannelExists(
        context: Context,
        contactId: String,
        contactName: String,
        soundUri: Uri?,
        pattern: LongArray?
    ): String {
        val channelId = getContactChannelId(context, contactId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getNotificationManager(context) ?: return channelId
            if (nm.getNotificationChannel(channelId) == null) {
                return recreateContactMessageChannel(context, contactId, contactName)
            }
        }
        return channelId
    }
}
