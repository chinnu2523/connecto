package com.example.connecto.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * SecureStorageManager provides Hardware Android KeyStore-backed EncryptedSharedPreferences (AES-256-GCM)
 * for sensitive authentication tokens and cryptographic salts, with automatic legacy migration and safe fallback.
 */
object SecureStorageManager {
    private const val TAG = "SecureStorageManager"
    private const val SECURE_PREFS_NAME = "connecto_secure_session_prefs"
    private const val LEGACY_PREFS_NAME = "connecto_session_prefs"

    @Volatile
    private var encryptedPrefs: SharedPreferences? = null

    fun getEncryptedPrefs(context: Context): SharedPreferences {
        return encryptedPrefs ?: synchronized(this) {
            encryptedPrefs ?: try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val secure = EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

                // Seamless migration: If legacy plaintext prefs exist, migrate sensitive keys to encrypted storage
                val legacy = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
                val legacyToken = legacy.getString("token", null)
                val legacyE2ESecret = legacy.getString("e2e_secret", null)
                val legacySessionId = legacy.getString("session_id", null)

                if (!legacyToken.isNullOrBlank() && !secure.contains("token")) {
                    secure.edit().putString("token", legacyToken).apply()
                    legacy.edit().remove("token").apply()
                }
                if (!legacyE2ESecret.isNullOrBlank() && !secure.contains("e2e_secret")) {
                    secure.edit().putString("e2e_secret", legacyE2ESecret).apply()
                    legacy.edit().remove("e2e_secret").apply()
                }
                if (!legacySessionId.isNullOrBlank() && !secure.contains("session_id")) {
                    secure.edit().putString("session_id", legacySessionId).apply()
                    legacy.edit().remove("session_id").apply()
                }

                encryptedPrefs = secure
                secure
            } catch (e: Exception) {
                Log.w(TAG, "Hardware KeyStore / EncryptedSharedPreferences unavailable; falling back to private prefs", e)
                context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    private const val KEY_CLIENT_DEVICE_ID = "client_device_id"

    /**
     * Returns a stable, persistent device ID unique to this physical installation.
     * Stored in encrypted storage and remains identical across app restarts, token refreshes,
     * and network/IP changes.
     */
    fun getOrCreateDeviceId(context: Context): String {
        val prefs = getEncryptedPrefs(context)
        val existing = prefs.getString(KEY_CLIENT_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) {
            return existing
        }
        synchronized(this) {
            val doubleCheck = prefs.getString(KEY_CLIENT_DEVICE_ID, null)
            if (!doubleCheck.isNullOrBlank()) {
                return doubleCheck
            }
            val androidId = try {
                android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                )
            } catch (_: Exception) {
                null
            }
            val newDeviceId = if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                "dev_" + java.util.UUID.nameUUIDFromBytes("${context.packageName}:$androidId".toByteArray()).toString()
            } else {
                "dev_" + java.util.UUID.randomUUID().toString()
            }
            prefs.edit().putString(KEY_CLIENT_DEVICE_ID, newDeviceId).apply()
            return newDeviceId
        }
    }

    fun clearDeviceId(context: Context) {
        try {
            getEncryptedPrefs(context).edit().remove(KEY_CLIENT_DEVICE_ID).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear client deviceId", e)
        }
    }
}
