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

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK")
                || Build.BOARD == "QC_Reference_Phone"
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HOST.startsWith("Build")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }

    /**
     * Evaluates whether the currently authenticated/active user is an Admin.
     */
    fun isAdminUser(context: Context? = null): Boolean {
        if (com.example.connecto.network.ConnectoApiClient.currentUserIsAdmin) return true
        if (com.example.connecto.network.ConnectoApiClient.currentUserIsAdminState.value) return true
        if (com.example.connecto.network.ConnectoNetworkConfig.isUserAdmin) return true
        val uname = (com.example.connecto.network.ConnectoApiClient.currentUsername ?: "").trim().lowercase().removePrefix("@")
        if (uname.isNotEmpty()) {
            if (uname in listOf("chinnu14754x", "chinnu")) {
                return true
            }
        }
        val ctx = context ?: activeActivityRef?.get()
        if (ctx != null) {
            try {
                for (prefsName in listOf("connecto_session_prefs", "connecto_secure_session_prefs", "connecto_auth_prefs")) {
                    val p = ctx.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                    val savedUname = p.getString("username", null)?.trim()?.lowercase()?.removePrefix("@") ?: ""
                    if (savedUname in listOf("chinnu14754x", "chinnu") && p.getBoolean("is_admin", false)) {
                        return true
                    }
                }
            } catch (_: Exception) {}
        }
        return false
    }

    /**
     * Core Security Policy:
     * Only the admin user can take screenshots and screen record in the application.
     * All other members are strictly prohibited: FLAG_SECURE is enforced to block screenshots and recording.
     */
    fun isScreenSecurityEnforced(context: Context? = null): Boolean {
        val isAdmin = isAdminUser(context)
        if (isAdmin) {
            // Admin user: Can take screenshots and screen record!
            val ctx = context ?: activeActivityRef?.get()
            return if (ctx != null) {
                getPrefs(ctx).getBoolean(KEY_SCREEN_SECURITY_ENABLED, false)
            } else false
        } else {
            // All other members: CANNOT take screenshots or screen record under any circumstances.
            return true
        }
    }

    fun init(context: Context) {
        val secureRequired = isScreenSecurityEnforced(context)
        _isSecurityEnabledState.value = secureRequired
        Log.d(TAG, "ScreenSecurityManager initialized. isSecureEnforced=$secureRequired (isAdmin=${isAdminUser(context)})")
    }

    fun isScreenSecurityEnabled(context: Context): Boolean {
        return isScreenSecurityEnforced(context)
    }

    fun getSecureFlagPolicy(context: Context? = null): androidx.compose.ui.window.SecureFlagPolicy {
        return if (isAdminUser(context)) {
            androidx.compose.ui.window.SecureFlagPolicy.SecureOff
        } else {
            androidx.compose.ui.window.SecureFlagPolicy.SecureOn
        }
    }

    fun setScreenSecurityEnabled(context: Context, enabled: Boolean, activity: Activity? = null): Boolean {
        val isAdmin = isAdminUser(context)
        if (!isAdmin) {
            Toast.makeText(
                context,
                "🛡️ Restricted: Only administrator users can take screenshots or modify screen security. Members are protected.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        getPrefs(context).edit().putBoolean(KEY_SCREEN_SECURITY_ENABLED, enabled).apply()
        _isSecurityEnabledState.value = enabled
        val targetActivity = activity ?: activeActivityRef?.get() ?: (context as? Activity)
        applySecurityPolicy(targetActivity)
        Log.d(TAG, "Screen security preference updated by admin: $enabled")
        return true
    }

    fun applySecurityPolicy(activity: Activity? = null) {
        val targetActivity = activity ?: activeActivityRef?.get() ?: return
        activeActivityRef = WeakReference(targetActivity)
        targetActivity.runOnUiThread {
            try {
                val secureRequired = isScreenSecurityEnforced(targetActivity)
                _isSecurityEnabledState.value = secureRequired
                if (secureRequired) {
                    targetActivity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                    Log.d(TAG, "FLAG_SECURE applied to window: non-admin member screenshot/recording BLOCKED")
                } else {
                    targetActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    Log.d(TAG, "FLAG_SECURE cleared from window: admin user screenshot/recording ALLOWED")
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
