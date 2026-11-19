package com.example.connecto.security

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

/**
 * SecretVaultManager manages the Secret Vault for locked chats hidden behind the DM search bar.
 *
 * Stores salted SHA-256 hashes of custom secret codes in Android KeyStore-backed EncryptedSharedPreferences
 * and provides runtime state flow for vault unlock status, with auto-lock on app minimize.
 */
object SecretVaultManager {
    private const val TAG = "SecretVaultManager"
    private const val PREF_KEY_VAULT_CODE_HASH = "secret_vault_code_hash"
    private const val PREF_KEY_HIDE_LOCKED_CHATS = "hide_locked_chats"
    private const val VAULT_SALT = "connecto_secret_vault_salt_v1_2026"

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    fun hashSecretCode(code: String): String {
        val clean = code.trim()
        if (clean.isBlank()) return ""
        val input = "$VAULT_SALT:$clean:$VAULT_SALT"
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hasSecretCode(context: Context): Boolean {
        val hash = getSecretCodeHash(context)
        return !hash.isNullOrBlank()
    }

    fun getSecretCodeHash(context: Context): String? {
        val prefs = SecureStorageManager.getEncryptedPrefs(context)
        return prefs.getString(PREF_KEY_VAULT_CODE_HASH, null)?.takeIf { it.isNotBlank() }
    }

    fun setSecretCode(context: Context, code: String) {
        val hash = hashSecretCode(code)
        val prefs = SecureStorageManager.getEncryptedPrefs(context)
        prefs.edit().putString(PREF_KEY_VAULT_CODE_HASH, hash).apply()
        Log.d(TAG, "Secret vault code hash updated.")
    }

    fun verifySecretCode(context: Context, input: String): Boolean {
        val savedHash = getSecretCodeHash(context) ?: return false
        val inputHash = hashSecretCode(input)
        return savedHash.equals(inputHash, ignoreCase = true)
    }

    fun isSecretCodeMatch(context: Context, query: String): Boolean {
        val q = query.trim()
        if (q.isBlank()) return false
        val savedHash = getSecretCodeHash(context) ?: return false
        val candidateHash = hashSecretCode(q)
        return savedHash.equals(candidateHash, ignoreCase = true)
    }

    fun isHideLockedChatsEnabled(context: Context): Boolean {
        val prefs = SecureStorageManager.getEncryptedPrefs(context)
        return prefs.getBoolean(PREF_KEY_HIDE_LOCKED_CHATS, false)
    }

    fun isContactHiddenInVault(context: Context, contactId: String?, contactUsername: String? = null): Boolean {
        if (contactId.isNullOrBlank() && contactUsername.isNullOrBlank()) return false
        if (isVaultUnlocked.value) return false
        if (!isHideLockedChatsEnabled(context)) return false

        val friendPrefs = context.getSharedPreferences("connecto_friend_prefs", Context.MODE_PRIVATE)
        val myUsername = (com.example.connecto.network.ConnectoApiClient.currentUsername ?: "").trim().lowercase().removePrefix("@")
        val cleanId = contactId?.trim()?.lowercase()?.removePrefix("#")?.removePrefix("@")?.removePrefix("dm-") ?: ""
        val cleanUser = contactUsername?.trim()?.lowercase()?.removePrefix("@") ?: ""

        val keysToCheck = mutableListOf<String>()
        if (cleanId.isNotBlank()) {
            keysToCheck.add("chat_locked_$cleanId")
            if (myUsername.isNotBlank()) keysToCheck.add("${myUsername}_chat_locked_$cleanId")
        }
        if (cleanUser.isNotBlank()) {
            keysToCheck.add("chat_locked_$cleanUser")
            if (myUsername.isNotBlank()) keysToCheck.add("${myUsername}_chat_locked_$cleanUser")
        }
        return keysToCheck.any { friendPrefs.getBoolean(it, false) }
    }

    fun setHideLockedChats(context: Context, enabled: Boolean) {
        val prefs = SecureStorageManager.getEncryptedPrefs(context)
        prefs.edit().putBoolean(PREF_KEY_HIDE_LOCKED_CHATS, enabled).apply()
        Log.d(TAG, "Hide locked chats from list preference updated to: $enabled")
    }

    fun resetSecretCode(context: Context) {
        val prefs = SecureStorageManager.getEncryptedPrefs(context)
        prefs.edit()
            .remove(PREF_KEY_VAULT_CODE_HASH)
            .putBoolean(PREF_KEY_HIDE_LOCKED_CHATS, false)
            .apply()
        _isVaultUnlocked.value = false
        Log.d(TAG, "Secret vault code reset.")
    }

    fun unlockVault() {
        _isVaultUnlocked.value = true
        Log.d(TAG, "Secret Vault unlocked.")
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
        Log.d(TAG, "Secret Vault locked.")
    }

    fun syncSettingsFromBackend(context: Context, codeHash: String?, hideLocked: Boolean) {
        val prefs = SecureStorageManager.getEncryptedPrefs(context)
        val editor = prefs.edit()
        if (!codeHash.isNullOrBlank()) {
            editor.putString(PREF_KEY_VAULT_CODE_HASH, codeHash)
        } else {
            editor.remove(PREF_KEY_VAULT_CODE_HASH)
        }
        editor.putBoolean(PREF_KEY_HIDE_LOCKED_CHATS, hideLocked)
        editor.apply()
        Log.d(TAG, "Synced secret vault settings from cloud backend.")
    }
}
