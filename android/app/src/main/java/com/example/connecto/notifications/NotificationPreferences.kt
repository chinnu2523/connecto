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

    const val DEFAULT_SOUND_ID = "aurora_chime"
    const val DEFAULT_SOUND_NAME = "Aurora Chime"

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
     * Gets the currently selected notification sound ID (e.g. "aurora_chime").
     */
    fun getSelectedSoundId(context: Context): String {
        return getPrefs(context).getString(KEY_SELECTED_SOUND, DEFAULT_SOUND_ID) ?: DEFAULT_SOUND_ID
    }

    /**
     * Gets the display name of the selected notification sound (e.g. "Aurora Chime").
     */
    fun getSelectedSoundName(context: Context): String {
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
