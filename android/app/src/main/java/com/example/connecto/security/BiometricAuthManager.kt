package com.example.connecto.security

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

    const val PREF_BIOMETRIC_MODALITY = "pref_biometric_modality_"

    enum class BiometricModality {
        FACE_ID,
        FINGERPRINT,
        AUTO,
        DEVICE_CREDENTIAL
    }

    /**
     * Checks whether the device hardware supports Face Recognition / Face ID.
     */
    fun hasFaceHardware(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            pm.hasSystemFeature(PackageManager.FEATURE_FACE) ||
            pm.hasSystemFeature("android.hardware.biometrics.face") ||
            pm.hasSystemFeature("android.hardware.face") ||
            pm.hasSystemFeature("com.samsung.android.bio.face") ||
            pm.hasSystemFeature("com.sec.android.hardware.biometrics.face") ||
            (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) && isWeakBiometricAvailable(context))
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Checks if weak biometrics (such as Class 2 Face Unlock) are available on device.
     */
    fun isWeakBiometricAvailable(context: Context): Boolean {
        return try {
            val bm = BiometricManager.from(context)
            val res = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            res == BiometricManager.BIOMETRIC_SUCCESS || res == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Checks whether the device hardware supports Fingerprint recognition.
     */
    fun hasFingerprintHardware(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) ||
            pm.hasSystemFeature("android.hardware.fingerprint")
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Checks whether the device hardware supports Iris recognition.
     */
    fun hasIrisHardware(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            pm.hasSystemFeature(PackageManager.FEATURE_IRIS) ||
            pm.hasSystemFeature("android.hardware.biometrics.iris")
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Returns the user's preferred biometric unlock modality.
     */
    fun getPreferredModality(context: Context, username: String?): BiometricModality {
        if (username.isNullOrBlank()) return BiometricModality.AUTO
        return try {
            val prefs = context.getSharedPreferences("connecto_user_profile_prefs", Context.MODE_PRIVATE)
            val saved = prefs.getString("$PREF_BIOMETRIC_MODALITY$username", BiometricModality.AUTO.name)
            BiometricModality.valueOf(saved ?: BiometricModality.AUTO.name)
        } catch (_: Throwable) {
            BiometricModality.AUTO
        }
    }

    /**
     * Saves the user's preferred biometric unlock modality.
     */
    fun setPreferredModality(context: Context, username: String, modality: BiometricModality) {
        val prefs = context.getSharedPreferences("connecto_user_profile_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("$PREF_BIOMETRIC_MODALITY$username", modality.name).apply()
    }

    /**
     * Returns true if Face ID is the primary/active biometric unlock method for this user/device.
     */
    fun isFaceIdPrimary(context: Context, username: String? = null): Boolean {
        val pref = getPreferredModality(context, username)
        if (pref == BiometricModality.FACE_ID) return true
        if (pref == BiometricModality.FINGERPRINT) return false

        // AUTO: If device has Face ID hardware, default to Face ID!
        return hasFaceHardware(context)
    }

    // Authenticators for Face ID (includes BIOMETRIC_WEAK so Class 2 Face Unlock is recognized by Android)
    const val AUTHENTICATORS_FACE_ID = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK

    // Authenticators for Fingerprint on Android 11+ (API 30+)
    const val ALLOWED_AUTHENTICATORS_API30 = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    // Authenticators for Device Screen Lock (PIN, Pattern, Password) directly
    const val AUTHENTICATORS_DEVICE_CREDENTIAL = BiometricManager.Authenticators.DEVICE_CREDENTIAL

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
    fun getStatusDescription(context: Context, username: String? = null): String {
        val hasFace = hasFaceHardware(context)
        val hasFp = hasFingerprintHardware(context)
        val hasBio = isBiometricEnrolled(context)
        val hasDeviceSecure = isDeviceSecure(context)
        val isFacePrimary = isFaceIdPrimary(context, username)

        return when {
            hasFace && hasFp && hasBio -> if (isFacePrimary) "Face ID (Primary) & Fingerprint active" else "Fingerprint (Primary) & Face ID active"
            hasFace && hasBio -> "Face ID enrolled & active"
            hasFp && hasBio -> "Fingerprint enrolled & active"
            hasBio && hasDeviceSecure -> if (isFacePrimary) "Face ID & Device Screen Lock active" else "Fingerprint & Device Screen Lock active"
            hasBio -> if (isFacePrimary) "Face ID biometric enrolled & active" else "Fingerprint biometric enrolled & active"
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
        isFaceId: Boolean = false,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {},
        onFallbackToDeviceCredential: (() -> Unit)? = null
    ) {
        try {
            val actualTitle = if (isFaceId && title == "Unlock Connecto") "Unlock Connecto with Face ID" else title
            val actualSubtitle = if (isFaceId && subtitle.contains("biometric")) "Glance at the camera or use device screen lock" else subtitle
            val actualDescription = if (isFaceId && description != null && description.contains("Fingerprint")) "Confirm your identity with Face ID or Device PIN" else description
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
                .setTitle(actualTitle)
                .setSubtitle(actualSubtitle)

            if (!actualDescription.isNullOrBlank()) {
                promptInfoBuilder.setDescription(actualDescription)
            }

            if (isFaceId) {
                // Face ID Mode:
                // 1. Must include BIOMETRIC_WEAK so Class 2 Face Unlock is recognized by Android
                // 2. setConfirmationRequired(false): Passive unlock! As soon as face is recognized by camera, it unlocks immediately without requiring touch
                // 3. setNegativeButtonText: Allows fallback to device screen lock (PIN/Password) on all Android versions
                promptInfoBuilder.setAllowedAuthenticators(AUTHENTICATORS_FACE_ID)
                promptInfoBuilder.setConfirmationRequired(false)
                promptInfoBuilder.setNegativeButtonText("Use PIN / Password")
            } else {
                // Fingerprint Mode:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    promptInfoBuilder.setAllowedAuthenticators(ALLOWED_AUTHENTICATORS_API30)
                } else {
                    promptInfoBuilder.setNegativeButtonText("Unlock with Password")
                }
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
