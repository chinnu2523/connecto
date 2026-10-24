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
}
