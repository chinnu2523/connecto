package com.example.connecto.security

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Manages the state of the Two-Factor Authentication (2FA) verification dialog.
 * Persists the verification flow across app backgrounding, app-lock / biometric re-entry,
 * and Android activity lifecycle restarts.
 */
object TwoFactorStateManager {
    private const val PREFS_NAME = "connecto_2fa_pending_prefs"
    private const val KEY_IS_PENDING = "is_setup_pending"
    private const val KEY_METHOD = "setup_method"
    private const val KEY_DESTINATION = "setup_destination"
    private const val KEY_MASKED_DEST = "setup_masked_destination"
    private const val KEY_DEV_OTP = "setup_dev_otp"
    private const val KEY_OTP_DRAFT = "setup_otp_draft"
    private const val KEY_TIMESTAMP = "setup_timestamp"

    // 10 minutes OTP lifetime
    private const val OTP_LIFETIME_MS = 10 * 60 * 1000L

    var isSetupDialogOpen by mutableStateOf(false)
    var setupMethod by mutableStateOf("email")
    var setupDestination by mutableStateOf("")
    var setupMaskedDestination by mutableStateOf<String?>(null)
    var setupDevOtp by mutableStateOf<String?>(null)
    var setupOtpCode by mutableStateOf("")
    var setupErrorMessage by mutableStateOf<String?>(null)
    var isVerifying by mutableStateOf(false)
    var isResending by mutableStateOf(false)
    var resendSuccessMessage by mutableStateOf<String?>(null)
    var resendCountdown by mutableIntStateOf(0)
    var setupTimestamp by mutableLongStateOf(0L)

    fun startSetup(
        context: Context,
        method: String,
        destination: String,
        maskedDest: String?,
        devOtp: String?
    ) {
        val now = System.currentTimeMillis()
        setupMethod = method
        setupDestination = destination
        setupMaskedDestination = maskedDest
        setupDevOtp = devOtp
        setupOtpCode = ""
        setupErrorMessage = null
        isVerifying = false
        isResending = false
        resendSuccessMessage = null
        resendCountdown = 0
        setupTimestamp = now
        isSetupDialogOpen = true

        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_IS_PENDING, true)
                .putString(KEY_METHOD, method)
                .putString(KEY_DESTINATION, destination)
                .putString(KEY_MASKED_DEST, maskedDest)
                .putString(KEY_DEV_OTP, devOtp)
                .putString(KEY_OTP_DRAFT, "")
                .putLong(KEY_TIMESTAMP, now)
                .apply()
        } catch (_: Exception) {}
    }

    fun updateDraftCode(context: Context, code: String) {
        setupOtpCode = code
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_OTP_DRAFT, code).apply()
        } catch (_: Exception) {}
    }

    fun updateDevOtp(context: Context, devOtp: String?) {
        setupDevOtp = devOtp
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_DEV_OTP, devOtp).apply()
        } catch (_: Exception) {}
    }

    fun restoreIfPending(context: Context): Boolean {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isPending = prefs.getBoolean(KEY_IS_PENDING, false)
            if (!isPending) return false

            val ts = prefs.getLong(KEY_TIMESTAMP, 0L)
            val now = System.currentTimeMillis()
            if (now - ts > OTP_LIFETIME_MS) {
                clear(context)
                return false
            }

            setupMethod = prefs.getString(KEY_METHOD, "email") ?: "email"
            setupDestination = prefs.getString(KEY_DESTINATION, "") ?: ""
            setupMaskedDestination = prefs.getString(KEY_MASKED_DEST, null)
            setupDevOtp = prefs.getString(KEY_DEV_OTP, null)
            setupOtpCode = prefs.getString(KEY_OTP_DRAFT, "") ?: ""
            setupTimestamp = ts
            isSetupDialogOpen = true
            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun clear(context: Context) {
        isSetupDialogOpen = false
        setupMethod = "email"
        setupDestination = ""
        setupMaskedDestination = null
        setupDevOtp = null
        setupOtpCode = ""
        setupErrorMessage = null
        isVerifying = false
        isResending = false
        resendSuccessMessage = null
        resendCountdown = 0
        setupTimestamp = 0L

        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        } catch (_: Exception) {}
    }
}
