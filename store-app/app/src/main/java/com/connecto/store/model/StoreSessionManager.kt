package com.connecto.store.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages active user authentication session and security tokens backed by
 * hardware Android KeyStore EncryptedSharedPreferences (AES-256-GCM) with
 * automatic legacy migration and fallback.
 */
class StoreSessionManager(context: Context) {

    private val prefs: SharedPreferences = getOrCreateEncryptedPrefs(context)

    companion object {
        private const val TAG = "StoreSessionManager"
        private const val SECURE_PREFS_NAME = "our_store_secure_session_prefs"
        private const val LEGACY_PREFS_NAME = "our_store_session_prefs"

        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USERNAME = "key_username"
        private const val KEY_DISPLAY_NAME = "key_display_name"
        private const val KEY_AVATAR_URL = "key_avatar_url"
        private const val KEY_ROLE = "key_role"
        private const val KEY_TOKEN = "key_token"
        private const val KEY_IS_SSO = "key_is_sso"
        private const val KEY_BIOMETRIC_DOWNLOADS = "key_biometric_downloads_enabled"
        private const val KEY_HAS_AUTHORIZED_ONCE = "key_has_authorized_once"

        private fun getOrCreateEncryptedPrefs(context: Context): SharedPreferences {
            return try {
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

                // Seamless automatic migration from legacy plaintext preferences
                val legacy = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
                if (legacy.all.isNotEmpty() && !secure.contains(KEY_IS_LOGGED_IN)) {
                    val editor = secure.edit()
                    legacy.all.forEach { (key, value) ->
                        when (value) {
                            is Boolean -> editor.putBoolean(key, value)
                            is String -> editor.putString(key, value)
                            is Int -> editor.putInt(key, value)
                            is Long -> editor.putLong(key, value)
                            is Float -> editor.putFloat(key, value)
                        }
                    }
                    editor.apply()
                    legacy.edit().clear().apply()
                    Log.i(TAG, "Successfully migrated legacy session preferences to KeyStore AES-256 encrypted storage")
                }

                secure
            } catch (e: Exception) {
                Log.w(TAG, "Hardware KeyStore / EncryptedSharedPreferences unavailable; falling back to private prefs", e)
                context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    fun saveUser(user: StoreUser) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_AVATAR_URL, user.avatarUrl)
            .putString(KEY_ROLE, user.role.name)
            .putString(KEY_TOKEN, user.token)
            .putBoolean(KEY_IS_SSO, user.isConnectoSso)
            .apply()
    }

    fun getUser(): StoreUser? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val displayName = prefs.getString(KEY_DISPLAY_NAME, username) ?: username
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, "") ?: ""
        val roleStr = prefs.getString(KEY_ROLE, StoreRole.MEMBER.name) ?: StoreRole.MEMBER.name
        val role = try { StoreRole.valueOf(roleStr) } catch (_: Exception) { StoreRole.MEMBER }
        val token = prefs.getString(KEY_TOKEN, "") ?: ""
        val isSso = prefs.getBoolean(KEY_IS_SSO, false)

        return StoreUser(
            username = username,
            displayName = displayName,
            avatarUrl = avatarUrl,
            role = role,
            token = token,
            isConnectoSso = isSso
        )
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isBiometricDownloadsEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_DOWNLOADS, true)

    fun setBiometricDownloadsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_DOWNLOADS, enabled).apply()
    }

    fun hasAuthorizedOnce(): Boolean = prefs.getBoolean(KEY_HAS_AUTHORIZED_ONCE, false)

    fun setAuthorizedOnce(authorized: Boolean = true) {
        prefs.edit().putBoolean(KEY_HAS_AUTHORIZED_ONCE, authorized).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getRole(): StoreRole {
        val user = getUser()
        return user?.role ?: StoreRole.GUEST
    }
}
