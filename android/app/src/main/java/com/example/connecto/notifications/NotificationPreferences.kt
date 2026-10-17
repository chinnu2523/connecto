package com.example.connecto.notifications

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages notification privacy settings and sound preferences for Connecto.
 */
object NotificationPreferences {
    private const val PREFS_NAME = "connecto_notification_prefs"

    const val KEY_HIDE_CONTENT = "hide_notification_content"
    const val KEY_SELECTED_SOUND = "selected_notification_sound"
    const val KEY_SELECTED_SOUND_NAME = "selected_notification_sound_name"

    const val DEFAULT_SOUND_ID = "sound_faah"
    const val DEFAULT_SOUND_NAME = "Faah"

    private val VALID_SOUND_IDS = setOf(
        "sound_faah",
        "sound_yeh_kya_hai",
        "sound_aayein",
        "sound_moye_moye",
        "sound_chin_tapak",
        "sound_wah_kya_scene",
        "system_default"
    )

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if notification message previews & sender details should be hidden.
     */
    fun isHideContentEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HIDE_CONTENT, false)
    }

    /**
     * Toggles whether notification message content is redacted to "New message received".
     */
    fun setHideContentEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HIDE_CONTENT, enabled).apply()
    }

    /**
     * Gets the currently selected notification sound ID (e.g. "sound_faah").
     */
    fun getSelectedSoundId(context: Context): String {
        val saved = getPrefs(context).getString(KEY_SELECTED_SOUND, DEFAULT_SOUND_ID) ?: DEFAULT_SOUND_ID
        return if (saved in VALID_SOUND_IDS) saved else DEFAULT_SOUND_ID
    }

    /**
     * Gets the display name of the selected notification sound (e.g. "Faah").
     */
    fun getSelectedSoundName(context: Context): String {
        val savedId = getPrefs(context).getString(KEY_SELECTED_SOUND, DEFAULT_SOUND_ID) ?: DEFAULT_SOUND_ID
        if (savedId !in VALID_SOUND_IDS) return DEFAULT_SOUND_NAME
        return getPrefs(context).getString(KEY_SELECTED_SOUND_NAME, DEFAULT_SOUND_NAME) ?: DEFAULT_SOUND_NAME
    }

    /**
     * Saves the chosen notification sound ID and human-readable name.
     */
    fun setSelectedSound(context: Context, soundId: String, soundName: String) {
        getPrefs(context).edit()
            .putString(KEY_SELECTED_SOUND, soundId)
            .putString(KEY_SELECTED_SOUND_NAME, soundName)
            .apply()
    }
}
