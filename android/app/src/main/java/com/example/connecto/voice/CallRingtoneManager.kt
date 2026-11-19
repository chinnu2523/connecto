package com.example.connecto.voice

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.OpenableColumns
import android.util.Log
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
 * Model representing a selectable call ringtone.
 */
data class RingtoneOption(
    val id: String,
    val title: String,
    val uriString: String,
    val type: String // "default", "system", "custom"
)

/**
 * Model representing a per-contact customized call ringtone override.
 */
data class ContactRingtoneSetting(
    val contactId: String,
    val contactName: String = "",
    val contactAvatar: String? = null,
    val sourceType: String, // "custom", "system"
    val displayName: String,
    val localPath: String = "",
    val systemUri: String = ""
)

/**
 * Strict singleton controller for incoming call ringtones, custom audio storage, and vibration.
 * Guarantees zero overlapping ringtones and instantaneous, deterministic teardown across all surfaces.
 */
object CallRingtoneManager {
    private const val TAG = "CallRingtoneManager"
    private const val PREFS_NAME = "connecto_call_ringtone_prefs"

    const val RINGTONE_TYPE_DEFAULT = "default"
    const val RINGTONE_TYPE_SYSTEM = "system"
    const val RINGTONE_TYPE_CUSTOM = "custom"

    private const val KEY_TYPE = "key_ringtone_type"
    private const val KEY_NAME = "key_ringtone_name"
    private const val KEY_URI = "key_ringtone_uri"

    const val DEFAULT_RINGTONE_NAME = "Default (Connecto)"

    private val lock = Any()
    private var appContext: Context? = null
    private var activeMediaPlayer: MediaPlayer? = null
    private var activeVibrator: Vibrator? = null

    @Volatile
    var isRinging: Boolean = false
        private set

    // Preview player state for Settings UI
    private var previewMediaPlayer: MediaPlayer? = null
    private var previewJob: Job? = null
    private val _currentlyPlayingPreviewId = MutableStateFlow<String?>(null)
    val currentlyPlayingPreviewId: StateFlow<String?> = _currentlyPlayingPreviewId.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun cleanContactId(id: String): String {
        return id.trim().lowercase().removePrefix("@").removePrefix("dm-").replace(Regex("[^a-z0-9_-]"), "_")
    }

    // =========================================================================
    // Contact Override Settings & Hygiene
    // =========================================================================

    fun getContactRingtoneSetting(context: Context, contactId: String): ContactRingtoneSetting? {
        val clean = cleanContactId(contactId)
        if (clean.isBlank()) return null
        val rawJson = getPrefs(context).getString("key_contact_override_$clean", null) ?: return null
        return try {
            val obj = JSONObject(rawJson)
            ContactRingtoneSetting(
                contactId = obj.optString("contactId", clean),
                contactName = obj.optString("contactName", ""),
                contactAvatar = obj.optString("contactAvatar").takeIf { it.isNotBlank() },
                sourceType = obj.optString("sourceType", RINGTONE_TYPE_DEFAULT),
                displayName = obj.optString("displayName", ""),
                localPath = obj.optString("localPath", ""),
                systemUri = obj.optString("systemUri", "")
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Error parsing contact ringtone setting for $contactId: ${e.message}")
            null
        }
    }

    fun getAllContactRingtoneSettings(context: Context): List<ContactRingtoneSetting> {
        val list = mutableListOf<ContactRingtoneSetting>()
        try {
            val allEntries = getPrefs(context).all
            for ((key, value) in allEntries) {
                if (key.startsWith("key_contact_override_") && value is String) {
                    try {
                        val obj = JSONObject(value)
                        list.add(
                            ContactRingtoneSetting(
                                contactId = obj.optString("contactId"),
                                contactName = obj.optString("contactName"),
                                contactAvatar = obj.optString("contactAvatar").takeIf { it.isNotBlank() },
                                sourceType = obj.optString("sourceType"),
                                displayName = obj.optString("displayName"),
                                localPath = obj.optString("localPath"),
                                systemUri = obj.optString("systemUri")
                            )
                        )
                    } catch (_: Throwable) {}
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error querying all contact ringtone settings: ${e.message}")
        }
        return list.sortedBy { it.contactName.ifBlank { it.contactId }.lowercase() }
    }

    suspend fun saveContactCustomRingtone(
        context: Context,
        contactId: String,
        contactName: String,
        contactAvatar: String?,
        sourceUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        val clean = cleanContactId(contactId)
        if (clean.isBlank()) return@withContext Result.failure(IllegalArgumentException("Invalid contact ID"))

        try {
            val resolver = context.contentResolver
            val displayName = queryDisplayName(context, sourceUri) ?: "contact_${clean}_ringtone.mp3"

            val ringtonesDir = File(context.filesDir, "ringtones").apply {
                if (!exists()) mkdirs()
            }
            // Clean up any prior custom ringtones for this contact
            deleteContactRingtoneFiles(context, clean)

            val ext = displayName.substringAfterLast('.', "mp3")
            val destFile = File(ringtonesDir, "contact_${clean}.$ext")

            resolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(16384)
                    var totalBytes = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        totalBytes += read
                        if (totalBytes > 20 * 1024 * 1024L) { // 20 MB max limit
                            destFile.delete()
                            return@withContext Result.failure(IllegalStateException("Audio file exceeds maximum 20 MB limit."))
                        }
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            } ?: return@withContext Result.failure(IllegalStateException("Could not open audio file stream."))

            // Validate that MediaPlayer can prepare and play it
            var testMp: MediaPlayer? = null
            try {
                testMp = MediaPlayer().apply {
                    setDataSource(destFile.absolutePath)
                    prepare()
                }
            } catch (t: Throwable) {
                destFile.delete()
                return@withContext Result.failure(IllegalStateException("Corrupt or unsupported audio file format."))
            } finally {
                try { testMp?.release() } catch (_: Throwable) {}
            }

            // Save JSON setting to prefs
            val settingObj = JSONObject().apply {
                put("contactId", contactId)
                put("contactName", contactName)
                put("contactAvatar", contactAvatar ?: "")
                put("sourceType", RINGTONE_TYPE_CUSTOM)
                put("displayName", displayName)
                put("localPath", destFile.absolutePath)
                put("systemUri", "")
            }
            getPrefs(context).edit().putString("key_contact_override_$clean", settingObj.toString()).apply()

            Log.d(TAG, "Saved contact custom ringtone for $contactId: $displayName -> ${destFile.absolutePath}")
            Result.success(displayName)
        } catch (t: Throwable) {
            Log.e(TAG, "saveContactCustomRingtone failed for $contactId: ${t.message}", t)
            Result.failure(t)
        }
    }

    fun saveContactSystemRingtone(
        context: Context,
        contactId: String,
        contactName: String,
        contactAvatar: String?,
        systemTitle: String,
        systemUri: String
    ) {
        val clean = cleanContactId(contactId)
        if (clean.isBlank()) return
        deleteContactRingtoneFiles(context, clean)

        val settingObj = JSONObject().apply {
            put("contactId", contactId)
            put("contactName", contactName)
            put("contactAvatar", contactAvatar ?: "")
            put("sourceType", RINGTONE_TYPE_SYSTEM)
            put("displayName", systemTitle)
            put("localPath", "")
            put("systemUri", systemUri)
        }
        getPrefs(context).edit().putString("key_contact_override_$clean", settingObj.toString()).apply()
        Log.d(TAG, "Saved contact system ringtone for $contactId: $systemTitle -> $systemUri")
    }

    fun removeContactRingtoneOverride(context: Context, contactId: String) {
        stopPreview()
        val clean = cleanContactId(contactId)
        if (clean.isBlank()) return
        deleteContactRingtoneFiles(context, clean)
        getPrefs(context).edit().remove("key_contact_override_$clean").apply()
        Log.d(TAG, "Removed contact ringtone override for $contactId (reverted to global)")
    }

    private fun deleteContactRingtoneFiles(context: Context, cleanContactId: String) {
        try {
            val ringtonesDir = File(context.filesDir, "ringtones")
            if (ringtonesDir.exists()) {
                val prefix = "contact_${cleanContactId}."
                ringtonesDir.listFiles { f -> f.name.startsWith(prefix) }?.forEach { f ->
                    val deleted = f.delete()
                    Log.d(TAG, "Deleted prior contact ringtone file: ${f.name} (success=$deleted)")
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error cleaning up contact ringtone files: ${t.message}")
        }
    }

    // =========================================================================
    // Global Settings
    // =========================================================================

    fun getSelectedRingtoneType(context: Context): String {
        return getPrefs(context).getString(KEY_TYPE, RINGTONE_TYPE_DEFAULT) ?: RINGTONE_TYPE_DEFAULT
    }

    fun getSelectedRingtoneName(context: Context): String {
        return getPrefs(context).getString(KEY_NAME, DEFAULT_RINGTONE_NAME) ?: DEFAULT_RINGTONE_NAME
    }

    fun getSelectedRingtoneUri(context: Context): String {
        return getPrefs(context).getString(KEY_URI, "") ?: ""
    }

    fun setRingtoneSetting(context: Context, type: String, name: String, uri: String) {
        getPrefs(context).edit()
            .putString(KEY_TYPE, type)
            .putString(KEY_NAME, name)
            .putString(KEY_URI, uri)
            .apply()
        Log.d(TAG, "Ringtone setting saved: type=$type, name=$name, uri=$uri")
    }

    fun resetToDefault(context: Context) {
        stopPreview()
        setRingtoneSetting(context, RINGTONE_TYPE_DEFAULT, DEFAULT_RINGTONE_NAME, "")
    }

    fun getCustomRingtoneFile(context: Context): File? {
        val path = getSelectedRingtoneUri(context)
        if (path.isNotBlank()) {
            val file = File(path)
            if (file.exists() && file.canRead()) return file
        }
        val dir = File(context.filesDir, "ringtones")
        if (dir.exists()) {
            val files = dir.listFiles { f -> f.name.startsWith("active_custom_ringtone") }
            if (!files.isNullOrEmpty()) {
                return files[0]
            }
        }
        return null
    }

    /**
     * Starts continuous looping ringtone and vibration for an incoming call.
     * Guaranteed thread-safe and stops any prior audio/vibration before starting.
     *
     * 3-Tier Priority Resolution:
     * Tier 1: Per-contact custom override (if callerUserId provided & not hidden in Secret Vault)
     * Tier 2: Global custom ringtone (if configured)
     * Tier 3: Default system ringtone fallback
     */
    fun startRingtoneAndVibration(context: Context, callerUserId: String? = null) {
        appContext = context.applicationContext
        synchronized(lock) {
            stopPreview()
            stopRingtoneAndVibration(context)

            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val ringerMode = am?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL

            // Respect Android Device Ringer Modes
            if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                Log.d(TAG, "startRingtoneAndVibration: Device is in SILENT/DND mode (ringerMode=$ringerMode) — 0 sound & 0 vibration")
                return
            }

            val shouldVibrate = ringerMode == AudioManager.RINGER_MODE_NORMAL || ringerMode == AudioManager.RINGER_MODE_VIBRATE
            val shouldPlaySound = ringerMode == AudioManager.RINGER_MODE_NORMAL

            if (shouldVibrate) {
                startVibrationInternal(context)
            }

            if (shouldPlaySound) {
                startSoundInternal(context, callerUserId)
            }
        }
    }

    fun startRinging(callId: String?, callerUserId: String? = null) {
        appContext?.let { startRingtoneAndVibration(it, callerUserId) }
    }

    private fun startSoundInternal(context: Context, callerUserId: String? = null) {
        var started = false

        // Secret Vault Privacy Guard:
        // If caller is hidden inside Secret Vault and privacy redaction is active,
        // play only the neutral default ringtone so secret contacts are not exposed by custom audio.
        val isHiddenInVault = !callerUserId.isNullOrBlank() &&
                SecretVaultManager.isContactHiddenInVault(context, callerUserId)

        if (isHiddenInVault) {
            Log.d(TAG, "Privacy Guard: Caller $callerUserId is hidden in Secret Vault. Falling back to neutral default ringtone.")
            playFallbackDefault(context)
            return
        }

        // TIER 1: Contact Override
        if (!callerUserId.isNullOrBlank()) {
            val contactSetting = getContactRingtoneSetting(context, callerUserId)
            if (contactSetting != null) {
                Log.d(TAG, "Tier 1: Found contact ringtone override for $callerUserId: ${contactSetting.displayName} (${contactSetting.sourceType})")
                if (contactSetting.sourceType == RINGTONE_TYPE_CUSTOM && contactSetting.localPath.isNotBlank()) {
                    val file = File(contactSetting.localPath)
                    if (file.exists() && file.canRead()) {
                        started = tryPlayMediaPlayer(context, Uri.fromFile(file))
                    } else {
                        Log.w(TAG, "Contact custom ringtone file not readable: ${contactSetting.localPath}")
                    }
                } else if (contactSetting.sourceType == RINGTONE_TYPE_SYSTEM && contactSetting.systemUri.isNotBlank()) {
                    started = tryPlayMediaPlayer(context, Uri.parse(contactSetting.systemUri))
                }
            }
        }

        // TIER 2: Global Custom / System Ringtone
        if (!started) {
            val globalType = getSelectedRingtoneType(context)
            val globalUriStr = getSelectedRingtoneUri(context)
            Log.d(TAG, "Tier 2: Checking global ringtone: type=$globalType")
            if (globalType == RINGTONE_TYPE_CUSTOM) {
                val customFile = getCustomRingtoneFile(context)
                if (customFile != null && customFile.exists() && customFile.canRead()) {
                    started = tryPlayMediaPlayer(context, Uri.fromFile(customFile))
                }
            } else if (globalType == RINGTONE_TYPE_SYSTEM && globalUriStr.isNotBlank()) {
                started = tryPlayMediaPlayer(context, Uri.parse(globalUriStr))
            }
        }

        // TIER 3: Default Fallback (Connecto Default / Android System Ringtone)
        if (!started) {
            Log.d(TAG, "Tier 3: Falling back to default system ringtone")
            playFallbackDefault(context)
        }
    }

    private fun tryPlayMediaPlayer(context: Context, uri: Uri): Boolean {
        return try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = true
                prepare()
                start()
            }
            activeMediaPlayer = mp
            isRinging = true
            Log.d(TAG, "MediaPlayer ringtone started playing uri: $uri")
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Failed playing MediaPlayer with uri $uri: ${t.message}", t)
            false
        }
    }

    private fun playFallbackDefault(context: Context) {
        try {
            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, defaultUri)
                isLooping = true
                prepare()
                start()
            }
            activeMediaPlayer = mp
            isRinging = true
            Log.d(TAG, "Fallback system default ringtone started playing")
        } catch (t: Throwable) {
            Log.e(TAG, "Critical: Even system default ringtone failed to start: ${t.message}", t)
        }
    }

    private fun startVibrationInternal(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            val pattern = longArrayOf(0, 1000, 800, 1000, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
            activeVibrator = vibrator
            Log.d(TAG, "Incoming call vibration started")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to start vibrator: ${t.message}", t)
        }
    }

    /**
     * Unconditionally and synchronously silences the ringtone, releases MediaPlayer, and cancels vibration.
     * Guaranteed safe to call repeatedly from any thread.
     */
    fun stopRingtoneAndVibration(context: Context? = null) {
        synchronized(lock) {
            val mp = activeMediaPlayer
            activeMediaPlayer = null
            isRinging = false
            if (mp != null) {
                try {
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                    mp.reset()
                    mp.release()
                    Log.d(TAG, "Active ringtone MediaPlayer released successfully")
                } catch (t: Throwable) {
                    Log.w(TAG, "Error releasing active MediaPlayer: ${t.message}")
                }
            }

            val v = activeVibrator
            activeVibrator = null
            if (v != null) {
                try {
                    v.cancel()
                    Log.d(TAG, "Active vibrator cancelled")
                } catch (t: Throwable) {
                    Log.w(TAG, "Error cancelling vibrator: ${t.message}")
                }
            }

            // Ensure AudioManager releases ring stream focus
            try {
                val am = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                if (am != null && am.mode == AudioManager.MODE_RINGTONE) {
                    am.mode = AudioManager.MODE_NORMAL
                }
            } catch (_: Throwable) {}
        }
    }

    // =========================================================================
    // Custom Ringtone SAF File Persistence (Android 15 safe - zero revoked URIs)
    // =========================================================================

    suspend fun saveCustomRingtone(context: Context, sourceUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val displayName = queryDisplayName(context, sourceUri) ?: "custom_ringtone.mp3"

            val ringtonesDir = File(context.filesDir, "ringtones").apply {
                if (!exists()) mkdirs()
            }
            // Clean up old global custom ringtones without affecting contact overrides
            ringtonesDir.listFiles { f -> f.name.startsWith("active_custom_ringtone.") }?.forEach { it.delete() }

            val ext = displayName.substringAfterLast('.', "mp3")
            val destFile = File(ringtonesDir, "active_custom_ringtone.$ext")

            resolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(16384)
                    var totalBytes = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        totalBytes += read
                        if (totalBytes > 20 * 1024 * 1024L) { // 20 MB max limit
                            destFile.delete()
                            return@withContext Result.failure(IllegalStateException("Audio file exceeds maximum 20 MB limit."))
                        }
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            } ?: return@withContext Result.failure(IllegalStateException("Could not open audio file stream."))

            // Validate that MediaPlayer can prepare and play it
            var testMp: MediaPlayer? = null
            try {
                testMp = MediaPlayer().apply {
                    setDataSource(destFile.absolutePath)
                    prepare()
                }
            } catch (t: Throwable) {
                destFile.delete()
                return@withContext Result.failure(IllegalStateException("Corrupt or unsupported audio file format."))
            } finally {
                try { testMp?.release() } catch (_: Throwable) {}
            }

            setRingtoneSetting(
                context = context,
                type = RINGTONE_TYPE_CUSTOM,
                name = displayName,
                uri = destFile.absolutePath
            )
            Result.success(displayName)
        } catch (t: Throwable) {
            Log.e(TAG, "saveCustomRingtone failed: ${t.message}", t)
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
    // System Ringtones Querying
    // =========================================================================

    fun getSystemRingtones(context: Context): List<RingtoneOption> {
        val list = mutableListOf<RingtoneOption>()
        try {
            val manager = RingtoneManager(context).apply {
                setType(RingtoneManager.TYPE_RINGTONE)
            }
            val cursor: Cursor? = manager.cursor
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                    val uri = manager.getRingtoneUri(cursor.position)
                    if (uri != null) {
                        list.add(
                            RingtoneOption(
                                id = uri.toString(),
                                title = title ?: "System Ringtone",
                                uriString = uri.toString(),
                                type = RINGTONE_TYPE_SYSTEM
                            )
                        )
                    }
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error querying system ringtones: ${t.message}")
        }
        return list
    }

    // =========================================================================
    // Ringtone Preview Handling (Auto-stop after 15s)
    // =========================================================================

    fun playPreview(context: Context, option: RingtoneOption) {
        synchronized(lock) {
            stopPreview()
            previewJob?.cancel()

            try {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    if (option.type == RINGTONE_TYPE_CUSTOM) {
                        setDataSource(option.uriString)
                    } else if (option.type == RINGTONE_TYPE_SYSTEM && option.uriString.isNotBlank()) {
                        setDataSource(context, Uri.parse(option.uriString))
                    } else {
                        setDataSource(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                    }
                    isLooping = true
                    prepare()
                    start()
                }
                previewMediaPlayer = mp
                _currentlyPlayingPreviewId.value = option.id

                // Auto-stop preview after 15 seconds
                previewJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(15_000L)
                    stopPreview()
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed playing preview for ${option.title}: ${t.message}", t)
                stopPreview()
            }
        }
    }

    fun stopPreview() {
        synchronized(lock) {
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
            _currentlyPlayingPreviewId.value = null
        }
    }
}

/**
 * Controller alias matching specification requirements.
 */
typealias CallRingtoneController = CallRingtoneManager
