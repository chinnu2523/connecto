package com.example.connecto.security

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricStatus {
    READY,
    NOT_ENROLLED,
    NO_HARDWARE,
    HW_UNAVAILABLE,
    SECURITY_UPDATE_REQUIRED,
    UNKNOWN
}

object BiometricAuthManager {

    private const val TAG = "BiometricAuthManager"

    // Combination for modern Android 11+ (API 30+)
    // NOTE: BIOMETRIC_WEAK cannot be combined with DEVICE_CREDENTIAL on API 30+ — causes IllegalArgumentException
    const val ALLOWED_AUTHENTICATORS_API30 = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    // Authenticators for API < 30 check
    const val BIOMETRIC_CHECK_LEGACY = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK

    /**
     * Checks if the device has a secure lock screen (PIN, Pattern, or Password) configured.
     */
    fun isDeviceSecure(context: Context): Boolean {
        return try {
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.isDeviceSecure == true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if device is secure: ${e.message}")
            false
        }
    }

    /**
     * Checks if biometric sensors (Fingerprint, Face) are available and user has enrolled biometric credentials.
     * Backwards-compatible across Android 7.0 - 15+ using BiometricManager.
     */
    fun isBiometricEnrolled(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val authTypes = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            biometricManager.canAuthenticate(authTypes) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Throwable) {
            Log.e(TAG, "Error checking if biometric is enrolled: ${e.message}")
            false
        }
    }

    /**
     * Creates the native intent to prompt the device screen lock (PIN, pattern, password).
     */
    fun createConfirmDeviceCredentialIntent(
        context: Context,
        title: String = "Unlock Connecto",
        description: String = "Enter your device PIN, pattern, or password to access Connecto"
    ): Intent? {
        // On API 30+ this API is fully deprecated; callers should use BiometricPrompt with
        // ALLOWED_AUTHENTICATORS_API30 instead. Return null so callers fall through to BiometricPrompt.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return null
        return try {
            @Suppress("DEPRECATION")
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            @Suppress("DEPRECATION")
            keyguardManager?.createConfirmDeviceCredentialIntent(title, description)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating confirm device credential intent: ${e.message}")
            null
        }
    }

    /**
     * Checks if biometric hardware is present and enrolled or screen lock is available.
     * Guaranteed backward-compatible across Android 7.0 - 15+ without throwing unsupported exceptions.
     */
    fun checkBiometricAvailability(context: Context): BiometricStatus {
        try {
            val biometricManager = BiometricManager.from(context)
            val authTypes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ALLOWED_AUTHENTICATORS_API30
            } else {
                BIOMETRIC_CHECK_LEGACY
            }

            when (biometricManager.canAuthenticate(authTypes)) {
                BiometricManager.BIOMETRIC_SUCCESS -> return BiometricStatus.READY
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    return if (isDeviceSecure(context)) BiometricStatus.READY else BiometricStatus.NOT_ENROLLED
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    return if (isDeviceSecure(context)) BiometricStatus.READY else BiometricStatus.NO_HARDWARE
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    return if (isDeviceSecure(context)) BiometricStatus.READY else BiometricStatus.HW_UNAVAILABLE
                }
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> return BiometricStatus.SECURITY_UPDATE_REQUIRED
                else -> {
                    return if (isDeviceSecure(context)) BiometricStatus.READY else BiometricStatus.UNKNOWN
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Biometric check encountered fallback condition: ${t.message}")
            return if (isDeviceSecure(context)) BiometricStatus.READY else BiometricStatus.UNKNOWN
        }
    }

    /**
     * Returns a human-friendly description of the security & biometric status.
     */
    fun getStatusDescription(context: Context): String {
        val hasBio = isBiometricEnrolled(context)
        val hasDeviceSecure = isDeviceSecure(context)

        return when {
            hasBio && hasDeviceSecure -> "Fingerprint, Face ID & Device Screen Lock active"
            hasBio -> "Biometric scan (Fingerprint / Face ID) enrolled & active"
            hasDeviceSecure -> "Device Screen Lock (PIN / Pattern / Password) active"
            else -> "Connecto account password lock active"
        }
    }

    /**
     * Legacy overload for backwards compatibility.
     */
    fun getStatusDescription(status: BiometricStatus): String {
        return when (status) {
            BiometricStatus.READY -> "Biometric & Screen Lock active and enrolled"
            BiometricStatus.NOT_ENROLLED -> "No fingerprint enrolled in device settings"
            BiometricStatus.NO_HARDWARE -> "Biometric hardware is not available on this device"
            BiometricStatus.HW_UNAVAILABLE -> "Biometric hardware is currently busy or unavailable"
            BiometricStatus.SECURITY_UPDATE_REQUIRED -> "Security update required for biometric hardware"
            BiometricStatus.UNKNOWN -> "Connecto account password lock active"
        }
    }

    /**
     * Shows the system BiometricPrompt modal with complete API level compatibility.
     * Prevents force close / crash on Android 7.0 - 10 (API 24-29).
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Connecto",
        subtitle: String = "Verify your biometric or device screen lock to continue",
        description: String? = "Confirm your identity with Fingerprint, Face ID, or Device PIN",
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {},
        onFallbackToDeviceCredential: (() -> Unit)? = null
    ) {
        try {
            // On API < 30 (Android 10 and below), if biometric is not enrolled but device lock is set,
            // BiometricPrompt cannot handle screen lock without biometrics. Route directly to device credential.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !isBiometricEnrolled(activity)) {
                if (isDeviceSecure(activity)) {
                    if (onFallbackToDeviceCredential != null) {
                        onFallbackToDeviceCredential()
                    } else {
                        val intent = createConfirmDeviceCredentialIntent(activity, title, description ?: subtitle)
                        if (intent != null) {
                            activity.startActivity(intent)
                        } else {
                            onError(BiometricPrompt.ERROR_NO_BIOMETRICS, "Screen lock unavailable")
                        }
                    }
                    return
                }
            }

            val executor = ContextCompat.getMainExecutor(activity)

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user clicked the negative button (e.g. "Unlock with Password / Screen Lock"), trigger fallback
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        if (onFallbackToDeviceCredential != null) {
                            onFallbackToDeviceCredential()
                            return
                        }
                    }
                    onError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }

            val biometricPrompt = BiometricPrompt(activity, executor, callback)

            val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)

            if (!description.isNullOrBlank()) {
                promptInfoBuilder.setDescription(description)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+): Allows DEVICE_CREDENTIAL in allowed authenticators without negative button
                promptInfoBuilder.setAllowedAuthenticators(ALLOWED_AUTHENTICATORS_API30)
            } else {
                // Android 7.0 - 10 (API 24-29):
                // Do NOT call setAllowedAuthenticators(BIOMETRIC_STRONG) - causes IllegalArgumentException on weak sensors!
                // Omit setAllowedAuthenticators and setNegativeButtonText so all enrolled fingerprints work reliably.
                promptInfoBuilder.setNegativeButtonText("Unlock with Password")
            }

            val promptInfo = promptInfoBuilder.build()
            biometricPrompt.authenticate(promptInfo)
        } catch (t: Throwable) {
            Log.e(TAG, "Biometric authentication exception caught safely: ${t.message}", t)
            onError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, t.localizedMessage ?: "Biometric prompt error")
            onFallbackToDeviceCredential?.invoke()
        }
    }
}
