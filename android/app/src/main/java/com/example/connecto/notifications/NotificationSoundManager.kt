package com.example.connecto.notifications

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.Ringtone
import android.media.RingtoneManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Sound model representing a trending notification sound.
 */
data class TrendingNotificationSound(
    val id: String,
    val name: String,
    val subtitle: String,
    val tag: String,
    val durationMs: Int
)

/**
 * Manages playback, preview, and generation of trending notification sounds.
 */
object NotificationSoundManager {
    private const val TAG = "NotifSoundManager"
    private const val SAMPLE_RATE = 44100

    val TRENDING_SOUNDS = listOf(
        TrendingNotificationSound(
            id = "aurora_chime",
            name = "Aurora Chime",
            subtitle = "Ethereal crystal chime with harmonic decay",
            tag = "Trending #1",
            durationMs = 550
        ),
        TrendingNotificationSound(
            id = "cosmic_pulse",
            name = "Cosmic Pulse",
            subtitle = "Deep analog synth bloom with bass warmth",
            tag = "Viral",
            durationMs = 480
        ),
        TrendingNotificationSound(
            id = "cyber_ripple",
            name = "Cyber Ripple",
            subtitle = "Rapid tri-tone high-tech digital sequence",
            tag = "Futuristic",
            durationMs = 420
        ),
        TrendingNotificationSound(
            id = "zen_crystal",
            name = "Zen Crystal",
            subtitle = "528Hz Solfeggio pure singing crystal bowl",
            tag = "Aesthetic",
            durationMs = 650
        ),
        TrendingNotificationSound(
            id = "neon_pop",
            name = "Neon Pop",
            subtitle = "Punchy retro-arcade alert pop",
            tag = "Popular",
            durationMs = 220
        ),
        TrendingNotificationSound(
            id = "vapor_wave",
            name = "Vapor Wave",
            subtitle = "Dreamy major 7th chord swell with tape warmth",
            tag = "Lofi Vibe",
            durationMs = 600
        ),
        TrendingNotificationSound(
            id = "echo_drop",
            name = "Echo Drop",
            subtitle = "Crisp water droplet with acoustic bounce",
            tag = "Minimal",
            durationMs = 480
        ),
        TrendingNotificationSound(
            id = "system_default",
            name = "System Default",
            subtitle = "Android device system default ringtone",
            tag = "System",
            durationMs = 500
        )
    )

    private val _currentlyPlayingSoundId = MutableStateFlow<String?>(null)
    val currentlyPlayingSoundId: StateFlow<String?> = _currentlyPlayingSoundId.asStateFlow()

    private var activeAudioTrack: AudioTrack? = null
    private var activeRingtone: Ringtone? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

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
                    playSynthesizedSound(soundId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play sound preview: ${e.message}", e)
            } finally {
                if (_currentlyPlayingSoundId.value == soundId) {
                    _currentlyPlayingSoundId.value = null
                }
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
                    playSynthesizedSound(soundId, isPreview = false)
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
            activeAudioTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            }
            activeAudioTrack = null
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

    private suspend fun playSynthesizedSound(soundId: String, isPreview: Boolean = true) {
        val samples = generateSamplesForSound(soundId)
        if (samples.isEmpty()) return

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(if (isPreview) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        val bufferSize = samples.size * 2 // 2 bytes per 16-bit short

        val track = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        if (isPreview) {
            activeAudioTrack = track
        }

        try {
            track.write(samples, 0, samples.size)
            track.play()

            val durationMs = (samples.size * 1000L) / SAMPLE_RATE
            delay(durationMs + 60L)
        } finally {
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            } catch (_: Exception) {}
            if (activeAudioTrack == track) {
                activeAudioTrack = null
            }
        }
    }

    private fun generateSamplesForSound(soundId: String): ShortArray {
        return when (soundId) {
            "aurora_chime" -> generateAuroraChime()
            "cosmic_pulse" -> generateCosmicPulse()
            "cyber_ripple" -> generateCyberRipple()
            "zen_crystal" -> generateZenCrystal()
            "neon_pop" -> generateNeonPop()
            "vapor_wave" -> generateVaporWave()
            "echo_drop" -> generateEchoDrop()
            else -> generateAuroraChime()
        }
    }

    /**
     * Aurora Chime: Ethereal shimmering crystalline bell (880Hz + 1320Hz + 1760Hz).
     */
    private fun generateAuroraChime(): ShortArray {
        val durationMs = 550
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        val freq1 = 880.0
        val freq2 = 1320.0
        val freq3 = 1760.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-5.5 * t) // smooth exponential bell decay

            // Fast attack ramp (first 10ms) to prevent initial pop
            val attack = if (t < 0.01) (t / 0.01) else 1.0

            val wave = 0.55 * sin(2.0 * PI * freq1 * t) +
                    0.30 * sin(2.0 * PI * freq2 * t) +
                    0.15 * sin(2.0 * PI * freq3 * t)

            val sample = (wave * envelope * attack * 28000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Cosmic Pulse: Warm analog synth bloom (330Hz -> 587Hz glide with sub-bass 165Hz).
     */
    private fun generateCosmicPulse(): ShortArray {
        val durationMs = 480
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / (durationMs / 1000.0)

            // Warm upward pitch glide
            val currentFreq = 330.0 + (257.0 * progress * (1.0 - progress * 0.5))
            val subFreq = currentFreq * 0.5

            // ADSR style curve: rise then smooth warm fall
            val envelope = if (progress < 0.15) {
                progress / 0.15
            } else {
                exp(-4.2 * (progress - 0.15))
            }

            val wave = 0.65 * sin(2.0 * PI * currentFreq * t) +
                    0.35 * sin(2.0 * PI * subFreq * t)

            val sample = (wave * envelope * 29000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Cyber Ripple: Tri-tone rapid arpeggiated digital ripple (587Hz -> 740Hz -> 880Hz).
     */
    private fun generateCyberRipple(): ShortArray {
        val durationMs = 420
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        val notes = doubleArrayOf(587.33, 739.99, 880.00) // D5, F#5, A5
        val noteDuration = 0.14

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noteIndex = (t / noteDuration).toInt().coerceIn(0, 2)
            val noteTime = t - (noteIndex * noteDuration)
            val freq = notes[noteIndex]

            val noteAttack = if (noteTime < 0.008) (noteTime / 0.008) else 1.0
            val noteDecay = exp(-7.0 * noteTime)
            val wave = 0.70 * sin(2.0 * PI * freq * t) + 0.30 * sin(2.0 * PI * (freq * 2.0) * t)

            val sample = (wave * noteAttack * noteDecay * 27000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Zen Crystal: 528Hz Solfeggio frequency with pure harmonic singing bowl resonance.
     */
    private fun generateZenCrystal(): ShortArray {
        val durationMs = 650
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        val freq = 528.0
        val harmonic = 1056.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-3.8 * t)
            val attack = if (t < 0.02) (t / 0.02) else 1.0

            val wave = 0.82 * sin(2.0 * PI * freq * t) + 0.18 * sin(2.0 * PI * harmonic * t)
            val sample = (wave * envelope * attack * 28000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Neon Pop: Snappy retro-arcade pop alert (chirps 1400Hz -> 480Hz).
     */
    private fun generateNeonPop(): ShortArray {
        val durationMs = 220
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / (durationMs / 1000.0)

            // Exponential downward chirp
            val freq = 1400.0 * exp(-1.8 * progress)
            val attack = if (progress < 0.06) (progress / 0.06) else 1.0
            val decay = exp(-7.5 * progress)

            val wave = sin(2.0 * PI * freq * t)
            val sample = (wave * attack * decay * 31000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Vapor Wave: Dreamy lush major 7th chord (F4, A4, C5, E5).
     */
    private fun generateVaporWave(): ShortArray {
        val durationMs = 600
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        val f1 = 349.23 // F4
        val f2 = 440.00 // A4
        val f3 = 523.25 // C5
        val f4 = 659.25 // E5

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val attack = if (t < 0.04) (t / 0.04) else 1.0
            val decay = exp(-4.0 * t)

            val wave = 0.30 * sin(2.0 * PI * f1 * t) +
                    0.25 * sin(2.0 * PI * f2 * t) +
                    0.25 * sin(2.0 * PI * f3 * t) +
                    0.20 * sin(2.0 * PI * f4 * t)

            val sample = (wave * attack * decay * 28000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }

    /**
     * Echo Drop: Clean water droplet with two subtle acoustic bounces.
     */
    private fun generateEchoDrop(): ShortArray {
        val durationMs = 480
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE

            // Primary drop (t: 0..0.18s)
            val drop1 = if (t in 0.0..0.18) {
                val pt = t
                val freq = 1250.0 - (400.0 * (pt / 0.18))
                val env = exp(-12.0 * pt)
                0.75 * sin(2.0 * PI * freq * pt) * env
            } else 0.0

            // First echo bounce at t = 0.16s
            val drop2 = if (t in 0.16..0.32) {
                val pt = t - 0.16
                val freq = 1100.0 - (300.0 * (pt / 0.16))
                val env = exp(-14.0 * pt)
                0.35 * sin(2.0 * PI * freq * pt) * env
            } else 0.0

            // Second micro-echo at t = 0.30s
            val drop3 = if (t in 0.30..0.45) {
                val pt = t - 0.30
                val freq = 1000.0 - (200.0 * (pt / 0.15))
                val env = exp(-16.0 * pt)
                0.18 * sin(2.0 * PI * freq * pt) * env
            } else 0.0

            val wave = drop1 + drop2 + drop3
            val sample = (wave * 29000.0).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        return buffer
    }
}
