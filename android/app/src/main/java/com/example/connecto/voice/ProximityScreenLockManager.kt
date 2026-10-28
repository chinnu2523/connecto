package com.example.connecto.voice

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Manages in-call proximity detection and native screen-off behavior.
 * When speaking in the EARPIECE, covering the top receiver / proximity sensor
 * (holding the phone against the ear) automatically turns off the screen and locks touch,
 * exactly like native Android phone calls.
 * Pulling the phone away immediately turns the screen back on.
 * If switched to SPEAKER or BLUETOOTH, proximity screen-off is deactivated.
 */
object ProximityScreenLockManager : SensorEventListener {

    private const val TAG = "ProximityScreenLock"
    private const val WAKE_LOCK_TAG = "Connecto:InCallProximityWakeLock"

    private var appContext: Context? = null
    private var currentActivityRef: WeakReference<Activity>? = null

    private var powerManager: PowerManager? = null
    private var proximityWakeLock: PowerManager.WakeLock? = null

    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var isSensorRegistered = false

    private val _isNear = MutableStateFlow(false)
    val isNear: StateFlow<Boolean> = _isNear.asStateFlow()

    private val _isScreenOffLocked = MutableStateFlow(false)
    val isScreenOffLocked: StateFlow<Boolean> = _isScreenOffLocked.asStateFlow()

    private var originalBrightness: Float = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    private var isMonitoringActive = false

    fun init(context: Context) {
        val app = context.applicationContext
        appContext = app
        try {
            powerManager = app.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager?.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK) == true) {
                @Suppress("DEPRECATION")
                proximityWakeLock = powerManager?.newWakeLock(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                    WAKE_LOCK_TAG
                )?.apply {
                    setReferenceCounted(false)
                }
                Log.d(TAG, "Hardware PROXIMITY_SCREEN_OFF_WAKE_LOCK supported and ready")
            } else {
                Log.d(TAG, "Hardware PROXIMITY_SCREEN_OFF_WAKE_LOCK not supported; software proximity lock active")
            }

            sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            Log.d(TAG, "Proximity sensor: ${proximitySensor?.name}, maxRange=${proximitySensor?.maximumRange}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ProximityScreenLockManager: ${e.message}", e)
        }
    }

    fun updateActivity(activity: Activity?) {
        if (activity != null) {
            currentActivityRef = WeakReference(activity)
            if (appContext == null) {
                init(activity)
            }
        }
    }

    /**
     * Starts monitoring the proximity sensor for an in-progress call on the EARPIECE.
     */
    fun startMonitoring(activity: Activity? = null) {
        activity?.let { updateActivity(it) }
        isMonitoringActive = true

        val sm = sensorManager ?: appContext?.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sensorManager = sm
        val sensor = proximitySensor ?: sm?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        proximitySensor = sensor

        if (!isSensorRegistered && sm != null && sensor != null) {
            val registered = sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            isSensorRegistered = registered
            Log.d(TAG, "Proximity sensor listener registered: $registered")
        }

        acquireWakeLock()
    }

    /**
     * Stops monitoring and restores the screen to normal brightness.
     */
    fun stopMonitoring() {
        isMonitoringActive = false
        if (isSensorRegistered) {
            sensorManager?.unregisterListener(this)
            isSensorRegistered = false
        }
        releaseWakeLock()
        applyScreenState(near = false)
        _isNear.value = false
        _isScreenOffLocked.value = false
    }

    private fun acquireWakeLock() {
        try {
            val wl = proximityWakeLock
            if (wl != null && !wl.isHeld) {
                wl.acquire()
                Log.d(TAG, "ProximityWakeLock acquired")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring ProximityWakeLock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            val wl = proximityWakeLock
            if (wl != null && wl.isHeld) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        wl.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
                    } catch (_: Throwable) {
                        wl.release()
                    }
                } else {
                    wl.release()
                }
                Log.d(TAG, "ProximityWakeLock released")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing ProximityWakeLock: ${e.message}")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_PROXIMITY) return
        val distance = event.values.getOrNull(0) ?: return
        val maxRange = proximitySensor?.maximumRange ?: 5.0f

        // When the top receiver is closed / covered, distance is near 0
        // (usually 0.0cm or less than maxRange / 4.0cm)
        val isNear = distance < maxRange && distance < 4.0f
        Log.d(TAG, "onSensorChanged: distance=$distance, maxRange=$maxRange -> isNear=$isNear, route=${VoiceCallManager.currentAudioRoute.value}")

        _isNear.value = isNear

        val isCallActive = (VoiceCallManager.callState.value == CallState.CONNECTED ||
                VoiceCallManager.callState.value == CallState.OUTGOING_RINGING)
        val isEarpiece = (VoiceCallManager.currentAudioRoute.value == AudioRoute.EARPIECE)

        if (isMonitoringActive && isCallActive && isEarpiece) {
            _isScreenOffLocked.value = isNear
            applyScreenState(isNear)
        } else {
            _isScreenOffLocked.value = false
            applyScreenState(false)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun applyScreenState(near: Boolean) {
        val activity = currentActivityRef?.get() ?: return
        activity.runOnUiThread {
            try {
                val window = activity.window ?: return@runOnUiThread
                val lp = window.attributes
                if (near) {
                    if (originalBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                        originalBrightness = lp.screenBrightness
                    }
                    // Blank out the display backlight (temporary lock)
                    lp.screenBrightness = 0.0f
                    window.attributes = lp
                } else {
                    // Restore original display brightness
                    lp.screenBrightness = originalBrightness
                    window.attributes = lp
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adjusting window brightness for proximity: ${e.message}")
            }
        }
    }

    /**
     * Testing hook to simulate proximity events (for emulator or test scripts)
     */
    fun simulateProximityForTesting(isNear: Boolean) {
        _isNear.value = isNear
        val isCallActive = (VoiceCallManager.callState.value == CallState.CONNECTED ||
                VoiceCallManager.callState.value == CallState.OUTGOING_RINGING)
        val isEarpiece = (VoiceCallManager.currentAudioRoute.value == AudioRoute.EARPIECE)

        if (isCallActive && isEarpiece) {
            _isScreenOffLocked.value = isNear
            applyScreenState(isNear)
        } else {
            _isScreenOffLocked.value = false
            applyScreenState(false)
        }
    }
}
