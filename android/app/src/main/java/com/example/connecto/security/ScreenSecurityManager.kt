package com.example.connecto.security

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * ScreenSecurityManager enforces bulletproof screen privacy across Connecto.
 *
 * When enabled (default: true):
 * 1. Sets WindowManager.LayoutParams.FLAG_SECURE on the Activity window.
 * 2. Blocks screenshots: Taking a screenshot renders a solid black screen or is blocked by system policy.
 * 3. Blacks out screen sharing: MediaProjection API (used by Discord, Zoom, Teams, Google Meet,
 *    and system screen recorders) captures a solid black screen (#000000) for remote viewers ("others").
 * 4. Blacks out screen casting: Wireless / external non-secure displays (Miracast, Google Cast)
 *    render a solid black screen for others.
 * 5. Blanks out recent app preview thumbnails in the overview/multitasking task switcher.
 * 6. Actively monitors live screen recording/screenshare sessions via Android 14+ WindowManager API
 *    and DisplayManager to warn and protect user privacy in real time.
 */
object ScreenSecurityManager {
    private const val TAG = "ScreenSecurityManager"
    private const val PREFS_NAME = "connecto_screen_security_prefs"
    private const val KEY_SCREEN_SECURITY_ENABLED = "screen_security_blackscreen_enabled"

    private val _isSecurityEnabledState = MutableStateFlow(true)
    val isSecurityEnabledState: StateFlow<Boolean> = _isSecurityEnabledState.asStateFlow()

    private val _isScreenRecordingActive = MutableStateFlow(false)
    val isScreenRecordingActive: StateFlow<Boolean> = _isScreenRecordingActive.asStateFlow()

    private val _isCastingActive = MutableStateFlow(false)
    val isCastingActive: StateFlow<Boolean> = _isCastingActive.asStateFlow()

    private var activeActivityRef: WeakReference<Activity>? = null
    private var screenRecordingCallback: Any? = null
    private var displayListener: DisplayManager.DisplayListener? = null

    fun init(context: Context) {
        val prefs = getPrefs(context)
        val enabled = prefs.getBoolean(KEY_SCREEN_SECURITY_ENABLED, true)
        _isSecurityEnabledState.value = enabled
        Log.d(TAG, "ScreenSecurityManager initialized. Blackscreen security enabled: $enabled")
    }

    fun isScreenSecurityEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SCREEN_SECURITY_ENABLED, true)
    }

    fun setScreenSecurityEnabled(context: Context, enabled: Boolean, activity: Activity? = null) {
        getPrefs(context).edit().putBoolean(KEY_SCREEN_SECURITY_ENABLED, enabled).apply()
        _isSecurityEnabledState.value = enabled
        val targetActivity = activity ?: activeActivityRef?.get() ?: (context as? Activity)
        applySecurityPolicy(targetActivity)
        Log.d(TAG, "Screen security preference updated: $enabled")
    }

    fun applySecurityPolicy(activity: Activity?) {
        if (activity == null) return
        activeActivityRef = WeakReference(activity)
        activity.runOnUiThread {
            try {
                val enabled = isScreenSecurityEnabled(activity)
                if (enabled) {
                    activity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                    Log.d(TAG, "FLAG_SECURE applied to window: blackscreen protection ACTIVE")
                } else {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    Log.d(TAG, "FLAG_SECURE cleared from window: blackscreen protection DISABLED")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to apply security policy to window: ${e.message}")
            }
        }
    }

    fun registerListeners(activity: Activity) {
        activeActivityRef = WeakReference(activity)

        // 1. Android 14+ Live Screen Recording / Screen Share Callback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                if (screenRecordingCallback == null) {
                    val consumer = java.util.function.Consumer<Int> { state ->
                        val isRecording = (state == WindowManager.SCREEN_RECORDING_STATE_VISIBLE)
                        _isScreenRecordingActive.value = isRecording
                        Log.d(TAG, "Screen recording/sharing state changed: $state (isRecording=$isRecording)")
                        if (isRecording && isScreenSecurityEnabled(activity)) {
                            activity.runOnUiThread {
                                applySecurityPolicy(activity)
                                Toast.makeText(
                                    activity,
                                    "🛡️ Screen Share/Recording Detected: Content is blacked out for others.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    screenRecordingCallback = consumer
                    activity.windowManager.addScreenRecordingCallback(
                        activity.mainExecutor,
                        consumer
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not register addScreenRecordingCallback: ${e.message}")
            }
        }

        // 2. DisplayManager listener for live screen casting / external presentation displays
        try {
            val displayManager = activity.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            if (displayManager != null && displayListener == null) {
                val listener = object : DisplayManager.DisplayListener {
                    override fun onDisplayAdded(displayId: Int) {
                        checkCastingDisplays(displayManager, activity)
                    }

                    override fun onDisplayRemoved(displayId: Int) {
                        checkCastingDisplays(displayManager, activity)
                    }

                    override fun onDisplayChanged(displayId: Int) {
                        checkCastingDisplays(displayManager, activity)
                    }
                }
                displayListener = listener
                displayManager.registerDisplayListener(listener, null)
                checkCastingDisplays(displayManager, activity)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not register DisplayListener: ${e.message}")
        }
    }

    private fun checkCastingDisplays(displayManager: DisplayManager, activity: Activity) {
        try {
            val displays = displayManager.displays
            var hasExternalOrVirtual = false
            for (d in displays) {
                if (d.displayId != Display.DEFAULT_DISPLAY) {
                    hasExternalOrVirtual = true
                    break
                }
            }
            _isCastingActive.value = hasExternalOrVirtual
            Log.d(TAG, "Casting/external display detected: $hasExternalOrVirtual")
            if (hasExternalOrVirtual && isScreenSecurityEnabled(activity)) {
                activity.runOnUiThread {
                    applySecurityPolicy(activity)
                    Toast.makeText(
                        activity,
                        "📺 Screen Cast Active: Content is blacked out for viewers.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun unregisterListeners(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && screenRecordingCallback != null) {
            try {
                @Suppress("UNCHECKED_CAST")
                activity.windowManager.removeScreenRecordingCallback(
                    screenRecordingCallback as java.util.function.Consumer<Int>
                )
                screenRecordingCallback = null
            } catch (e: Exception) {
                Log.w(TAG, "Could not unregister removeScreenRecordingCallback: ${e.message}")
            }
        }

        if (displayListener != null) {
            try {
                val displayManager = activity.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
                displayManager?.unregisterDisplayListener(displayListener)
                displayListener = null
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
