package com.example.connecto.data

import android.content.Context
import com.example.connecto.network.ConnectoApiClient
import org.json.JSONObject

/**
 * EmojiUsageRecord
 *
 * Tracks emoji usage count and last used timestamp for an individual user.
 */
data class EmojiUsageRecord(
    val emoji: String,
    val count: Int,
    val lastUsedTimestamp: Long
)

/**
 * EmojiUsageManager
 *
 * Manages per-user emoji usage frequency and recency.
 * Ensures each user's most used and recently used emojis are surfaced first,
 * persisted across app restarts in SharedPreferences.
 */
object EmojiUsageManager {
    private const val PREFS_NAME = "connecto_emoji_usage_prefs"
    private const val KEY_PREFIX = "emoji_usage_"

    /**
     * Resolves the effective user identifier for scoping emoji usage.
     */
    fun getEffectiveUserId(userId: String? = null): String {
        return userId?.takeIf { it.isNotBlank() }
            ?: ConnectoApiClient.currentUserId?.takeIf { it.isNotBlank() }
            ?: ConnectoApiClient.currentUsername?.takeIf { it.isNotBlank() }
            ?: "default_user"
    }

    /**
     * Records an emoji usage event for the specified user (or current logged-in user).
     * Increments the frequency counter and updates the lastUsed timestamp.
     */
    fun recordEmojiUsage(context: Context, emoji: String, userId: String? = null) {
        if (emoji.isBlank()) return
        val user = getEffectiveUserId(userId)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = "$KEY_PREFIX$user"
        val raw = prefs.getString(key, null)

        val records = mutableMapOf<String, EmojiUsageRecord>()

        if (!raw.isNullOrBlank()) {
            try {
                val json = JSONObject(raw)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val obj = json.optJSONObject(k)
                    if (obj != null) {
                        val count = obj.optInt("count", 1)
                        val time = obj.optLong("time", System.currentTimeMillis())
                        records[k] = EmojiUsageRecord(k, count, time)
                    } else {
                        val count = json.optInt(k, 1)
                        records[k] = EmojiUsageRecord(k, count, System.currentTimeMillis())
                    }
                }
            } catch (e: Exception) {
                // Ignore parse errors and rebuild
            }
        }

        val existing = records[emoji]
        val newCount = (existing?.count ?: 0) + 1
        records[emoji] = EmojiUsageRecord(emoji, newCount, System.currentTimeMillis())

        val outJson = JSONObject()
        for ((em, rec) in records) {
            val item = JSONObject()
            item.put("count", rec.count)
            item.put("time", rec.lastUsedTimestamp)
            outJson.put(em, item)
        }
        prefs.edit().putString(key, outJson.toString()).apply()
    }

    /**
     * Retrieves the list of emojis used by the individual user, ordered by:
     * 1. Frequency (count) descending - most used first
     * 2. Recency (lastUsedTimestamp) descending
     */
    fun getUsedEmojis(context: Context, userId: String? = null, maxCount: Int = 48): List<String> {
        val user = getEffectiveUserId(userId)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = "$KEY_PREFIX$user"
        val raw = prefs.getString(key, null) ?: return emptyList()

        val list = mutableListOf<EmojiUsageRecord>()
        try {
            val json = JSONObject(raw)
            val keys = json.keys()
            while (keys.hasNext()) {
                val emoji = keys.next()
                val obj = json.optJSONObject(emoji)
                if (obj != null) {
                    val count = obj.optInt("count", 1)
                    val time = obj.optLong("time", 0L)
                    list.add(EmojiUsageRecord(emoji, count, time))
                } else {
                    val count = json.optInt(emoji, 1)
                    list.add(EmojiUsageRecord(emoji, count, 0L))
                }
            }
        } catch (e: Exception) {
            return emptyList()
        }

        return list
            .sortedWith(compareByDescending<EmojiUsageRecord> { it.count }.thenByDescending { it.lastUsedTimestamp })
            .map { it.emoji }
            .take(maxCount)
    }

    /**
     * Returns true if the user has recorded any emoji usages.
     */
    fun hasUserUsedEmojis(context: Context, userId: String? = null): Boolean {
        return getUsedEmojis(context, userId, maxCount = 1).isNotEmpty()
    }

    /**
     * Returns the usage count of a specific emoji for the user.
     */
    fun getEmojiUsageCount(context: Context, emoji: String, userId: String? = null): Int {
        val user = getEffectiveUserId(userId)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = "$KEY_PREFIX$user"
        val raw = prefs.getString(key, null) ?: return 0
        return try {
            val json = JSONObject(raw)
            json.optJSONObject(emoji)?.optInt("count", 0) ?: json.optInt(emoji, 0)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Clears emoji history for the user.
     */
    fun clearUsage(context: Context, userId: String? = null) {
        val user = getEffectiveUserId(userId)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("$KEY_PREFIX$user").apply()
    }
}
