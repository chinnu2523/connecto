package com.example.connecto.crypto

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * End-to-End Encryption helper for 1:1 Direct Messages using AES-256-GCM.
 *
 * Keys are derived deterministically per-conversation between two users:
 *   Key = SHA-256(canonical_dm_name)
 *
 * Encrypted payload format:
 *   "ENC:" + Base64(12-byte IV + ciphertext + 16-byte GCM tag)
 *
 * If a message does not start with "ENC:", it is treated as plaintext (backward compatible).
 */
object ConnectoE2EEncryption {
    private const val PREFIX = "ENC:"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    fun deriveConversationKey(userA: String, userB: String): ByteArray {
        val u1 = userA.trim().lowercase().removePrefix("@")
        val u2 = userB.trim().lowercase().removePrefix("@")
        val canonical = if (u1 < u2) "e2e-$u1-$u2" else "e2e-$u2-$u1"
        return MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
    }

    fun encryptMessage(plaintext: String, keyBytes: ByteArray): String {
        return try {
            val iv = ByteArray(GCM_IV_LENGTH)
            java.security.SecureRandom().nextBytes(iv)
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
            val cipherText = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

            PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback to original text if encryption fails
            plaintext
        }
    }

    fun decryptMessage(content: String, keyBytes: ByteArray): String {
        if (!isEncrypted(content)) return content
        return try {
            val raw = content.removePrefix(PREFIX)
            val combined = Base64.decode(raw, Base64.DEFAULT)
            if (combined.size < GCM_IV_LENGTH) return content

            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            val cipherTextSize = combined.size - GCM_IV_LENGTH
            val cipherText = ByteArray(cipherTextSize)
            System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherTextSize)

            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decrypted = cipher.doFinal(cipherText)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            // If decryption fails (e.g. wrong key, corrupted), show original or placeholder
            content
        }
    }

    fun isEncrypted(content: String): Boolean = content.startsWith(PREFIX)
}
