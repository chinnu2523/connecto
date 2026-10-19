package com.example.connecto.notifications

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import com.example.connecto.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Sound model representing a trending notification sound.
 */
data class TrendingNotificationSound(
    val id: String,
    val name: String,
    val subtitle: String,
    val tag: String,
    val durationMs: Int,
    val rawResId: Int = 0
)

/**
 * Manages playback, preview, and selection of latest trending notification sounds:
 * Faah, Yeh kya hai, Aayein, Moye Moye, Chin Tapak Dum Dum, Wah Kya Scene Hai.
 */
object NotificationSoundManager {
    private const val TAG = "NotifSoundManager"

    val TRENDING_SOUNDS = listOf(
        TrendingNotificationSound(
            id = "sound_faah",
            name = "Faah",
            subtitle = "Telugu viral meme punch sound",
            tag = "Viral #1",
            durationMs = 2600,
            rawResId = R.raw.sound_faah
        ),
        TrendingNotificationSound(
            id = "sound_yeh_kya_hai",
            name = "Yeh kya hai",
            subtitle = "Iconic hilarious confused meme dialogue",
            tag = "Trending",
            durationMs = 3000,
            rawResId = R.raw.sound_yeh_kya_hai
        ),
        TrendingNotificationSound(
            id = "sound_aayein",
            name = "Aayein",
            subtitle = "Aditya Kumar viral 'Aayein? Baingan!' reaction",
            tag = "Viral",
            durationMs = 650,
            rawResId = R.raw.sound_aayein
        ),
        TrendingNotificationSound(
            id = "sound_moye_moye",
            name = "Moye Moye",
            subtitle = "Catchy melancholic reel trend hook",
            tag = "Global Trend",
            durationMs = 3500,
            rawResId = R.raw.sound_moye_moye
        ),
        TrendingNotificationSound(
            id = "sound_chin_tapak",
            name = "Chin Tapak Dum Dum",
            subtitle = "Viral dialogue soundbite trend",
            tag = "Viral",
            durationMs = 2300,
            rawResId = R.raw.sound_chin_tapak
        ),
        TrendingNotificationSound(
            id = "sound_wah_kya_scene",
            name = "Wah Kya Scene Hai",
            subtitle = "Classic hilarious Bollywood meme alert",
            tag = "Legendary",
            durationMs = 2100,
            rawResId = R.raw.sound_wah_kya_scene
        ),
        TrendingNotificationSound(
            id = "system_default",
            name = "System Default",
            subtitle = "Android device system default ringtone",
            tag = "System",
            durationMs = 500,
            rawResId = 0
        )
    )

    private val _currentlyPlayingSoundId = MutableStateFlow<String?>(null)
    val currentlyPlayingSoundId: StateFlow<String?> = _currentlyPlayingSoundId.asStateFlow()

    private var activeMediaPlayer: MediaPlayer? = null
    private var activeRingtone: Ringtone? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Resolves the raw resource ID for a sound ID.
     */
    fun getRawResourceForSound(soundId: String): Int {
        return when (soundId) {
            "sound_faah", "faah" -> R.raw.sound_faah
            "sound_yeh_kya_hai", "yeh_kya_hai", "ye_kya_hai" -> R.raw.sound_yeh_kya_hai
            "sound_aayein", "aayein" -> R.raw.sound_aayein
            "sound_moye_moye", "moye_moye" -> R.raw.sound_moye_moye
            "sound_chin_tapak", "chin_tapak", "chin_tapak_dum_dum" -> R.raw.sound_chin_tapak
            "sound_wah_kya_scene", "wah_kya_scene", "waah_kya_scene_hai" -> R.raw.sound_wah_kya_scene
            else -> R.raw.sound_faah
        }
    }

    /**
     * Returns an Android resource Uri for a given sound ID.
     */
    fun getSoundUri(context: Context, soundId: String): Uri? {
        if (soundId == "system_default") {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        val resId = getRawResourceForSound(soundId)
        return Uri.parse("android.resource://${context.packageName}/$resId")
    }

    /**
     * Previews a selected sound. Automatically stops previous audio.
     */
    fun playPreview(context: Context, soundId: String) {
        stopPreview()

        _currentlyPlayingSoundId.value = soundId

        playbackJob = scope.launch {
            try {
                if (soundId == "system_default") {
                    playSystemRingtone(context)
                } else {
                    val resId = getRawResourceForSound(soundId)
                    playRawSound(context, resId, isPreview = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play sound preview: ${e.message}", e)
            }
        }
    }

    /**
     * Plays the user's currently selected notification sound.
     */
    fun playNotificationSound(context: Context, customSoundId: String? = null) {
        val soundId = customSoundId ?: NotificationPreferences.getSelectedSoundId(context)
        scope.launch {
            try {
                if (soundId == "system_default") {
                    val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context.applicationContext, notificationUri)
                    ringtone?.play()
                } else {
                    val resId = getRawResourceForSound(soundId)
                    playRawSound(context, resId, isPreview = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play notification sound: ${e.message}", e)
            }
        }
    }

    /**
     * Immediately stops active audio playback.
     */
    fun stopPreview() {
        playbackJob?.cancel()
        playbackJob = null

        try {
            activeRingtone?.stop()
            activeRingtone = null
        } catch (_: Exception) {}

        try {
            activeMediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
                player.release()
            }
            activeMediaPlayer = null
        } catch (_: Exception) {}

        _currentlyPlayingSoundId.value = null
    }

    private suspend fun playSystemRingtone(context: Context) {
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context.applicationContext, notificationUri)
        activeRingtone = ringtone
        ringtone?.play()
        delay(1200L)
        ringtone?.stop()
        activeRingtone = null
    }

    private fun playRawSound(context: Context, resId: Int, isPreview: Boolean) {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(if (isPreview) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val player = MediaPlayer.create(context.applicationContext, resId, audioAttributes, 0) ?: return
            player.setVolume(1.0f, 1.0f)
            activeMediaPlayer = player
            player.setOnCompletionListener { mp ->
                try {
                    mp.reset()
                    mp.release()
                } catch (_: Exception) {}
                if (activeMediaPlayer == mp) {
                    activeMediaPlayer = null
                }
                _currentlyPlayingSoundId.value = null
            }
            player.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing raw sound: ${e.message}", e)
        }
    }
}
