package com.connecto.store.model

/**
 * Represents a single app in the Connecto Store catalog returned by the backend API.
 */
data class StoreApp(
    val id: String,
    val packageName: String,
    val name: String,
    val badge: String,
    val tagline: String,
    val description: String,
    val category: String,
    val versionName: String,
    val versionCode: Int,
    val sizeFormatted: String,
    val downloadUrl: String,
    val accentColor: String,
    val changelog: List<String>,
    val isEssential: Boolean,
    val updatedAt: String
) {
    /** Live install state, computed at runtime by comparing against PackageManager */
    enum class InstallState {
        NOT_INSTALLED,
        UPDATE_AVAILABLE,
        UP_TO_DATE
    }
}
