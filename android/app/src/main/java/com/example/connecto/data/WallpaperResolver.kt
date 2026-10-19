package com.example.connecto.data

import android.content.SharedPreferences
import com.example.connecto.ui.screens.FriendItem
import java.io.File

/**
 * Robust wallpaper resolution and migration engine for Connecto.
 * Resolves active chat wallpaper across modern scoped keys, pre-update legacy keys,
 * handle vs ID representations, and on-disk cached custom wallpaper files.
 */
object WallpaperResolver {

    fun getCanonicalChatId(friend: FriendItem?, myUsername: String): String {
        if (friend == null) return ""
        if (friend.isGroup) return friend.id
        val clean = friend.handle.trim().removePrefix("@").lowercase()
        val u = myUsername.trim().removePrefix("@").lowercase()
        return if (u.isNotBlank() && clean.isNotBlank()) {
            "dm-${minOf(u, clean)}-${maxOf(u, clean)}"
        } else {
            clean
        }
    }

    /**
     * Resolves the active wallpaper ID and custom wallpaper URI for a friend or group.
     * Backwards-compatible with:
     * 1. Modern userPrefix-scoped keys (${userPrefix}_chat_wallpaper_$key)
     * 2. Pre-update unscoped legacy keys (chat_wallpaper_$key)
     * 3. Handle vs UUID/usr_ ID mismatches
     * 4. Pre-update image files stored on disk in chat_wallpapers directory
     * 5. Automatically heals/migrates legacy entries forward to canonical keys
     */
    fun resolve(
        friend: FriendItem?,
        myUsername: String,
        prefs: SharedPreferences,
        filesDir: File? = null
    ): Pair<String, String?> {
        if (friend == null) return Pair("default", null)
        val userPrefix = myUsername.trim().removePrefix("@").lowercase()
        val chatKey = getCanonicalChatId(friend, userPrefix)
        val friendId = friend.id.trim()
        val cleanHandle = friend.handle.trim().removePrefix("@").lowercase()
        val friendName = friend.name.trim().lowercase()

        // 1. Build prioritized list of candidate identifiers
        val candidateIds = linkedSetOf<String>()
        if (chatKey.isNotBlank()) candidateIds.add(chatKey)
        if (friendId.isNotBlank()) candidateIds.add(friendId)
        if (cleanHandle.isNotBlank()) candidateIds.add(cleanHandle)
        if (friendId.isNotBlank() && friendId.startsWith("usr_")) candidateIds.add(friendId.removePrefix("usr_"))
        if (cleanHandle.isNotBlank() && !cleanHandle.startsWith("usr_")) candidateIds.add("usr_$cleanHandle")
        if (friendName.isNotBlank() && friendName != cleanHandle) candidateIds.add(friendName)

        var resolvedWp: String? = null
        var resolvedUri: String? = null
        var isFromLegacyOrFallback = false

        // 2. Check modern userPrefix-scoped keys
        if (userPrefix.isNotBlank()) {
            for (id in candidateIds) {
                val scopedWp = prefs.getString("${userPrefix}_chat_wallpaper_$id", null)
                val scopedCustom = prefs.getString("${userPrefix}_chat_wallpaper_custom_$id", null)

                if (!scopedWp.isNullOrBlank() && scopedWp != "default") {
                    resolvedWp = scopedWp
                    if (scopedWp == "custom") {
                        resolvedUri = scopedCustom
                    }
                    break
                } else if (!scopedCustom.isNullOrBlank()) {
                    resolvedWp = "custom"
                    resolvedUri = scopedCustom
                    break
                }
            }
        }

        // 3. Check legacy unscoped keys (saved before the multi-account scoping update)
        if (resolvedWp == null || resolvedWp == "default") {
            for (id in candidateIds) {
                val legacyWp = prefs.getString("chat_wallpaper_$id", null)
                val legacyCustom = prefs.getString("chat_wallpaper_custom_$id", null)

                if (!legacyWp.isNullOrBlank() && legacyWp != "default") {
                    resolvedWp = legacyWp
                    if (legacyWp == "custom") {
                        resolvedUri = legacyCustom
                    }
                    isFromLegacyOrFallback = true
                    break
                } else if (!legacyCustom.isNullOrBlank()) {
                    resolvedWp = "custom"
                    resolvedUri = legacyCustom
                    isFromLegacyOrFallback = true
                    break
                }
            }
        }

        // 4. Check on-disk local files in chat_wallpapers directory
        // Only trigger if no wallpaper was found, or if wallpaper is custom but URI is missing/broken
        val needsDiskFallback = (resolvedWp == null || resolvedWp == "default") ||
                (resolvedWp == "custom" && (resolvedUri.isNullOrBlank() || (resolvedUri.startsWith("/") && !File(resolvedUri).exists())))

        if (needsDiskFallback && filesDir != null) {
            val wallpapersDir = File(filesDir, "chat_wallpapers")
            if (wallpapersDir.exists() && wallpapersDir.isDirectory) {
                for (id in candidateIds) {
                    val candidateFiles = listOfNotNull(
                        if (userPrefix.isNotBlank()) File(wallpapersDir, "wp_${userPrefix}_${id}.jpg") else null,
                        File(wallpapersDir, "wp_${id}.jpg"),
                        if (userPrefix.isNotBlank()) File(wallpapersDir, "custom_wp_${userPrefix}_${id}.jpg") else null,
                        File(wallpapersDir, "custom_wp_${id}.jpg")
                    )
                    for (file in candidateFiles) {
                        if (file.exists() && file.length() > 0) {
                            val diskUri = "file://" + file.absolutePath
                            resolvedUri = diskUri
                            resolvedWp = "custom"
                            isFromLegacyOrFallback = true
                            break
                        }
                    }
                    if (resolvedWp == "custom") break
                }
            }
        }

        // 5. Sanitize URI formatting (ensure valid file:// URI if pointing to local file path)
        if (resolvedWp == "custom" && !resolvedUri.isNullOrBlank()) {
            if (resolvedUri.startsWith("/")) {
                resolvedUri = "file://$resolvedUri"
            } else if (resolvedUri.startsWith("file:/") && !resolvedUri.startsWith("file:///")) {
                val path = resolvedUri.removePrefix("file:")
                val cleanPath = "/" + path.trimStart('/')
                resolvedUri = "file://$cleanPath"
            }
        }

        val finalWp = resolvedWp ?: "default"
        val finalUri = if (finalWp == "custom") resolvedUri else null

        // 6. Seamless Auto-Heal: Migrate forward into canonical scoped keys for 0ms future resolution
        if (isFromLegacyOrFallback && finalWp != "default" && userPrefix.isNotBlank()) {
            try {
                val editor = prefs.edit()
                if (chatKey.isNotBlank()) {
                    editor.putString("${userPrefix}_chat_wallpaper_$chatKey", finalWp)
                    if (finalUri != null) {
                        editor.putString("${userPrefix}_chat_wallpaper_custom_$chatKey", finalUri)
                    } else {
                        editor.remove("${userPrefix}_chat_wallpaper_custom_$chatKey")
                    }
                }
                if (friendId.isNotBlank()) {
                    editor.putString("${userPrefix}_chat_wallpaper_$friendId", finalWp)
                    if (finalUri != null) {
                        editor.putString("${userPrefix}_chat_wallpaper_custom_$friendId", finalUri)
                    } else {
                        editor.remove("${userPrefix}_chat_wallpaper_custom_$friendId")
                    }
                }
                if (cleanHandle.isNotBlank()) {
                    editor.putString("${userPrefix}_chat_wallpaper_$cleanHandle", finalWp)
                    if (finalUri != null) {
                        editor.putString("${userPrefix}_chat_wallpaper_custom_$cleanHandle", finalUri)
                    } else {
                        editor.remove("${userPrefix}_chat_wallpaper_custom_$cleanHandle")
                    }
                }
                editor.apply()
            } catch (_: Exception) {}
        }

        return Pair(finalWp, finalUri)
    }

    /**
     * Resolves the size, position, scale mode, dim, and blur customizations for a chat wallpaper.
     */
    fun resolveTransform(
        friend: FriendItem?,
        myUsername: String,
        prefs: SharedPreferences
    ): WallpaperTransform {
        val userPrefix = myUsername.trim().removePrefix("@").lowercase()
        val chatKey = if (friend != null) getCanonicalChatId(friend, userPrefix) else "global"
        val friendId = friend?.id?.trim() ?: ""
        val cleanHandle = friend?.handle?.trim()?.removePrefix("@")?.lowercase() ?: ""
        val friendName = friend?.name?.trim()?.lowercase() ?: ""

        val candidateIds = linkedSetOf<String>()
        if (chatKey.isNotBlank()) candidateIds.add(chatKey)
        if (friendId.isNotBlank()) candidateIds.add(friendId)
        if (cleanHandle.isNotBlank()) candidateIds.add(cleanHandle)
        if (friendId.isNotBlank() && friendId.startsWith("usr_")) candidateIds.add(friendId.removePrefix("usr_"))
        if (cleanHandle.isNotBlank() && !cleanHandle.startsWith("usr_")) candidateIds.add("usr_$cleanHandle")
        if (friendName.isNotBlank() && friendName != cleanHandle) candidateIds.add(friendName)
        candidateIds.add("global")

        var scale: Float? = null
        var offsetX: Float? = null
        var offsetY: Float? = null
        var mode: String? = null
        var dim: Float? = null
        var blur: Float? = null

        // 1. Check userPrefix scoped keys
        if (userPrefix.isNotBlank()) {
            for (id in candidateIds) {
                if (prefs.contains("${userPrefix}_chat_wallpaper_scale_$id") && scale == null) {
                    scale = prefs.getFloat("${userPrefix}_chat_wallpaper_scale_$id", 1.0f)
                }
                if (prefs.contains("${userPrefix}_chat_wallpaper_offset_x_$id") && offsetX == null) {
                    offsetX = prefs.getFloat("${userPrefix}_chat_wallpaper_offset_x_$id", 0f)
                }
                if (prefs.contains("${userPrefix}_chat_wallpaper_offset_y_$id") && offsetY == null) {
                    offsetY = prefs.getFloat("${userPrefix}_chat_wallpaper_offset_y_$id", 0f)
                }
                if (prefs.contains("${userPrefix}_chat_wallpaper_mode_$id") && mode == null) {
                    mode = prefs.getString("${userPrefix}_chat_wallpaper_mode_$id", "crop")
                }
                if (prefs.contains("${userPrefix}_chat_wallpaper_dim_$id") && dim == null) {
                    dim = prefs.getFloat("${userPrefix}_chat_wallpaper_dim_$id", 0.50f)
                }
                if (prefs.contains("${userPrefix}_chat_wallpaper_blur_$id") && blur == null) {
                    blur = prefs.getFloat("${userPrefix}_chat_wallpaper_blur_$id", 0f)
                }
                if (scale != null && mode != null) break
            }
        }

        // 2. Fallback to legacy unscoped keys
        for (id in candidateIds) {
            if (scale == null && prefs.contains("chat_wallpaper_scale_$id")) {
                scale = prefs.getFloat("chat_wallpaper_scale_$id", 1.0f)
            }
            if (offsetX == null && prefs.contains("chat_wallpaper_offset_x_$id")) {
                offsetX = prefs.getFloat("chat_wallpaper_offset_x_$id", 0f)
            }
            if (offsetY == null && prefs.contains("chat_wallpaper_offset_y_$id")) {
                offsetY = prefs.getFloat("chat_wallpaper_offset_y_$id", 0f)
            }
            if (mode == null && prefs.contains("chat_wallpaper_mode_$id")) {
                mode = prefs.getString("chat_wallpaper_mode_$id", "crop")
            }
            if (dim == null && prefs.contains("chat_wallpaper_dim_$id")) {
                dim = prefs.getFloat("chat_wallpaper_dim_$id", 0.50f)
            }
            if (blur == null && prefs.contains("chat_wallpaper_blur_$id")) {
                blur = prefs.getFloat("chat_wallpaper_blur_$id", 0f)
            }
        }

        return WallpaperTransform(
            scale = (scale ?: 1.0f).coerceIn(0.5f, 3.0f),
            offsetX = offsetX ?: 0f,
            offsetY = offsetY ?: 0f,
            contentScaleMode = mode ?: "crop",
            dim = (dim ?: 0.50f).coerceIn(0.0f, 0.90f),
            blur = (blur ?: 0f).coerceIn(0f, 25f)
        )
    }

    /**
     * Persists customized wallpaper layout (scale, offsets, fit mode, dim, blur) across candidate keys.
     */
    fun saveTransform(
        friend: FriendItem?,
        myUsername: String,
        prefs: SharedPreferences,
        transform: WallpaperTransform
    ) {
        val userPrefix = myUsername.trim().removePrefix("@").lowercase()
        val chatKey = if (friend != null) getCanonicalChatId(friend, userPrefix) else "global"
        val friendId = friend?.id?.trim() ?: ""
        val cleanHandle = friend?.handle?.trim()?.removePrefix("@")?.lowercase() ?: ""

        val targets = linkedSetOf<String>()
        if (chatKey.isNotBlank()) targets.add(chatKey)
        if (friendId.isNotBlank()) targets.add(friendId)
        if (cleanHandle.isNotBlank()) targets.add(cleanHandle)
        if (targets.isEmpty()) targets.add("global")

        try {
            val editor = prefs.edit()
            for (id in targets) {
                if (userPrefix.isNotBlank()) {
                    editor.putFloat("${userPrefix}_chat_wallpaper_scale_$id", transform.scale)
                    editor.putFloat("${userPrefix}_chat_wallpaper_offset_x_$id", transform.offsetX)
                    editor.putFloat("${userPrefix}_chat_wallpaper_offset_y_$id", transform.offsetY)
                    editor.putString("${userPrefix}_chat_wallpaper_mode_$id", transform.contentScaleMode)
                    editor.putFloat("${userPrefix}_chat_wallpaper_dim_$id", transform.dim)
                    editor.putFloat("${userPrefix}_chat_wallpaper_blur_$id", transform.blur)
                }
            }
            editor.apply()
        } catch (_: Exception) {}
    }
}

/**
 * Data structure representing wallpaper size, position, fit mode, and visual tuning.
 */
data class WallpaperTransform(
    val scale: Float = 1.0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val contentScaleMode: String = "crop", // "crop" (Fill), "fit" (Fit Aspect), "fill_bounds" (Stretch), "inside" (Original)
    val dim: Float = 0.50f, // 0.0f to 0.90f
    val blur: Float = 0f // 0f to 25f
)
