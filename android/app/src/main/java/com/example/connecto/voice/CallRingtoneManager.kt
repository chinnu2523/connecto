package com.example.connecto.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Thread-safe centralized manager for incoming call ringtone and vibration.
 * Guarantees a single coordinated audio source and instantaneous, deterministic
 * teardown across all call response surfaces (fullscreen activity, notification actions,
 * in-app banners, and auto-dismiss collectors).
 */
object CallRingtoneManager {
    private const val TAG = "CallRingtoneManager"
    private var activeRingtone: Ringtone? = null
    private var activeVibrator: Vibrator? = null
    private val lock = Any()

    /**
     * Starts continuous looping ringtone and vibration for an incoming call.
     */
    fun startRingtoneAndVibration(context: Context) {
        synchronized(lock) {
            stopRingtoneAndVibration(context)

            try {
                val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
                if (ringtone != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ringtone.isLooping = true
                    }
                    ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()

                    ringtone.play()
                    activeRingtone = ringtone
                    Log.d(TAG, "startRingtoneAndVibration: Incoming call ringtone started playing")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "startRingtoneAndVibration: Failed to play ringtone: ${t.message}", t)
            }

            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                val pattern = longArrayOf(0, 800, 800, 800, 800)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
                activeVibrator = vibrator
                Log.d(TAG, "startRingtoneAndVibration: Incoming call vibration started")
            } catch (t: Throwable) {
                Log.e(TAG, "startRingtoneAndVibration: Failed to start vibrator: ${t.message}", t)
            }
        }
    }

    /**
     * Unconditionally and synchronously silences the ringtone and cancels vibration.
     * Guaranteed safe to call repeatedly from any thread.
     */
    fun stopRingtoneAndVibration(context: Context? = null) {
        synchronized(lock) {
            val r = activeRingtone
            activeRingtone = null
            if (r != null) {
                try {
                    // Unconditionally stop without checking isPlaying
                    r.stop()
                    Log.d(TAG, "stopRingtoneAndVibration: Active ringtone stopped unconditionally")
                } catch (t: Throwable) {
                    Log.e(TAG, "stopRingtoneAndVibration: Error stopping ringtone: ${t.message}")
                }
            }

            val v = activeVibrator
            activeVibrator = null
            if (v != null) {
                try {
                    v.cancel()
                    Log.d(TAG, "stopRingtoneAndVibration: Active vibrator cancelled")
                } catch (t: Throwable) {
                    Log.e(TAG, "stopRingtoneAndVibration: Error cancelling vibrator: ${t.message}")
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
}
