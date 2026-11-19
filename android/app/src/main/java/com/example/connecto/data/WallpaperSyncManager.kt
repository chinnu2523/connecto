package com.example.connecto.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.connecto.network.CloudWallpaperDto
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.ui.screens.FriendItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cloud-Backed Wallpaper Synchronization & Offline Caching Engine.
 *
 * Responsibilities:
 * 1. Compresses user gallery images to max width 1440px at 82% quality (WebP/JPEG).
 * 2. Uploads custom photos to Cloud Storage (POST /api/wallpapers/upload) and receives permanent URLs.
 * 3. Persists wallpaper configurations (presets & custom photos with full transforms) to Cloudflare D1.
 * 4. Stale-While-Revalidate: Caches metadata locally and pre-downloads remote custom photos for 0ms offline rendering.
 * 5. Automatic cleanup: Purges deleted/replaced custom images from cloud storage.
 */
object WallpaperSyncManager {

    private const val TAG = "WallpaperSyncManager"
    private const val MAX_WIDTH = 1440
    private const val QUALITY = 82

    private val _wallpaperUpdateTrigger = MutableStateFlow(0L)
    val wallpaperUpdateTrigger = _wallpaperUpdateTrigger.asStateFlow()

    private val syncScope = CoroutineScope(Dispatchers.IO)

    /**
     * Compresses and resizes a user-selected gallery image to max width 1440px with 82% quality.
     */
    fun compressImageForWallpaper(context: Context, uri: Uri): ByteArray? {
        return try {
            val contentResolver = context.contentResolver

            // 1. Decode bounds only to determine dimensions and optimal inSampleSize
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            if (origWidth <= 0 || origHeight <= 0) {
                Log.w(TAG, "Invalid image dimensions: $origWidth x $origHeight")
                return null
            }

            // 2. Compute inSampleSize for fast memory-efficient decoding
            var inSampleSize = 1
            if (origWidth > MAX_WIDTH) {
                while ((origWidth / (inSampleSize * 2)) >= MAX_WIDTH) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decodedBitmap = contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // 3. Exact aspect-ratio scaling down to max width 1440px if still larger
            val finalBitmap = if (decodedBitmap.width > MAX_WIDTH) {
                val targetHeight = ((decodedBitmap.height.toFloat() / decodedBitmap.width.toFloat()) * MAX_WIDTH).toInt()
                val scaled = Bitmap.createScaledBitmap(decodedBitmap, MAX_WIDTH, targetHeight.coerceAtLeast(1), true)
                if (scaled !== decodedBitmap) {
                    decodedBitmap.recycle()
                }
                scaled
            } else {
                decodedBitmap
            }

            // 4. Compress to WebP (lossy at 82%) with fallback to JPEG at 82%
            val byteStream = ByteArrayOutputStream()
            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }

            val compressed = finalBitmap.compress(format, QUALITY, byteStream)
            if (!compressed) {
                byteStream.reset()
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, byteStream)
            }
            finalBitmap.recycle()

            byteStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing wallpaper image", e)
            null
        }
    }

    /**
     * Resolves the target key string for a chat or global wallpaper.
     */
    fun getTargetKey(friend: FriendItem?, conversationId: String?, isGlobal: Boolean, myUsername: String): String {
        if (isGlobal) return "global"
        if (!conversationId.isNullOrBlank()) return conversationId
        val userPrefix = myUsername.trim().removePrefix("@").lowercase()
        return if (friend != null) WallpaperResolver.getCanonicalChatId(friend, userPrefix) else "global"
    }

    /**
     * Applies wallpaper locally (0ms) and dispatches background synchronization to Cloudflare.
     */
    fun applyAndSyncWallpaper(
        context: Context,
        myUsername: String,
        friend: FriendItem?,
        conversationId: String?,
        wallpaperId: String,
        customUri: String?,
        transform: WallpaperTransform,
        isGlobal: Boolean
    ) {
        val prefs = context.getSharedPreferences("friend_prefs", Context.MODE_PRIVATE)
        val userPrefix = myUsername.trim().removePrefix("@").lowercase()
        val targetFriend = if (isGlobal) null else friend
        val targetChatId = if (isGlobal) null else (conversationId ?: (friend?.let { WallpaperResolver.getCanonicalChatId(it, userPrefix) } ?: "global"))

        // 1. Instant local persistence for 0ms lag-free UI update
        WallpaperResolver.saveWallpaper(
            friend = targetFriend,
            myUsername = myUsername,
            prefs = prefs,
            wallpaperId = wallpaperId,
            customUri = customUri
        )
        WallpaperResolver.saveTransform(
            friend = targetFriend,
            myUsername = myUsername,
            prefs = prefs,
            transform = transform
        )

        _wallpaperUpdateTrigger.value = System.currentTimeMillis()

        // 2. Asynchronous cloud persistence and upload
        syncScope.launch {
            try {
                var finalImageUrl: String? = null

                if (wallpaperId == "custom" && !customUri.isNullOrBlank()) {
                    if (customUri.startsWith("http://") || customUri.startsWith("https://")) {
                        finalImageUrl = customUri
                    } else {
                        // Compress photo and upload to Cloudflare storage
                        val uri = Uri.parse(customUri)
                        val compressedBytes = compressImageForWallpaper(context, uri)
                        if (compressedBytes != null) {
                            val uploadResult = ConnectoApiClient.uploadWallpaperBytes(
                                context = context,
                                imageBytes = compressedBytes,
                                mimeType = "image/webp"
                            )
                            if (uploadResult.isSuccess) {
                                finalImageUrl = uploadResult.getOrNull()
                                // Cache compressed bytes locally on disk for instant offline playout
                                cacheWallpaperBytesOnDisk(context, userPrefix, targetChatId ?: "global", compressedBytes)
                                if (!finalImageUrl.isNullOrBlank()) {
                                    val editor = prefs.edit()
                                    val key = targetChatId ?: "global"
                                    editor.putString("${userPrefix}_chat_wallpaper_cloud_url_$key", finalImageUrl)
                                    editor.apply()
                                }
                            } else {
                                Log.w(TAG, "Wallpaper upload failed: ${uploadResult.exceptionOrNull()?.message}")
                            }
                        }
                    }
                }

                val dto = CloudWallpaperDto(
                    conversationId = targetChatId,
                    wallpaperType = if (wallpaperId == "custom") "custom_image" else "preset",
                    presetId = if (wallpaperId != "custom") wallpaperId else null,
                    imageUrl = finalImageUrl,
                    dim = transform.dim,
                    blur = transform.blur,
                    zoom = transform.scale,
                    offsetX = transform.offsetX,
                    offsetY = transform.offsetY,
                    fitMode = transform.contentScaleMode
                )

                val saveResult = ConnectoApiClient.saveWallpaperConfig(dto)
                if (saveResult.isSuccess) {
                    Log.i(TAG, "Successfully synced wallpaper config to cloud for $targetChatId")
                } else {
                    Log.w(TAG, "Cloud wallpaper save failed: ${saveResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in background wallpaper cloud sync", e)
            }
        }
    }

    /**
     * Resets wallpaper locally and removes cloud configuration (deleting orphaned cloud photos).
     */
    fun resetAndSyncWallpaper(
        context: Context,
        myUsername: String,
        friend: FriendItem?,
        conversationId: String?,
        isGlobal: Boolean
    ) {
        val prefs = context.getSharedPreferences("friend_prefs", Context.MODE_PRIVATE)
        val userPrefix = myUsername.trim().removePrefix("@").lowercase()
        val targetFriend = if (isGlobal) null else friend
        val targetChatId = if (isGlobal) null else (conversationId ?: (friend?.let { WallpaperResolver.getCanonicalChatId(it, userPrefix) } ?: "global"))

        // Reset local preferences
        WallpaperResolver.saveWallpaper(
            friend = targetFriend,
            myUsername = myUsername,
            prefs = prefs,
            wallpaperId = "default",
            customUri = null
        )
        WallpaperResolver.saveTransform(
            friend = targetFriend,
            myUsername = myUsername,
            prefs = prefs,
            transform = WallpaperTransform()
        )

        _wallpaperUpdateTrigger.value = System.currentTimeMillis()

        // Sync deletion to cloud
        syncScope.launch {
            try {
                ConnectoApiClient.deleteWallpaperConfig(conversationId = targetChatId, resetAll = isGlobal)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting cloud wallpaper", e)
            }
        }
    }

    /**
     * Synchronizes user's cloud-backed wallpaper preferences down to local device on Login / App Launch.
     * Implements Stale-While-Revalidate and downloads remote custom images for 0ms offline rendering.
     */
    suspend fun syncWallpapersFromCloud(context: Context) = withContext(Dispatchers.IO) {
        val myUsername = ConnectoApiClient.currentUsername ?: return@withContext
        val userPrefix = myUsername.trim().removePrefix("@").lowercase()
        if (userPrefix.isBlank() || userPrefix == "guest") return@withContext

        try {
            val result = ConnectoApiClient.fetchWallpapers()
            if (result.isFailure) {
                Log.d(TAG, "Could not fetch cloud wallpapers: ${result.exceptionOrNull()?.message}")
                return@withContext
            }

            val list = result.getOrNull() ?: return@withContext
            val prefs = context.getSharedPreferences("friend_prefs", Context.MODE_PRIVATE)
            val editor = prefs.edit()

            for (dto in list) {
                val targetKey = dto.conversationId ?: "global"
                val wpId = if (dto.wallpaperType == "custom_image") "custom" else (dto.presetId ?: "default")

                editor.putString("${userPrefix}_chat_wallpaper_$targetKey", wpId)
                editor.putFloat("${userPrefix}_chat_wallpaper_scale_$targetKey", dto.zoom)
                editor.putFloat("${userPrefix}_chat_wallpaper_offset_x_$targetKey", dto.offsetX)
                editor.putFloat("${userPrefix}_chat_wallpaper_offset_y_$targetKey", dto.offsetY)
                editor.putString("${userPrefix}_chat_wallpaper_mode_$targetKey", dto.fitMode)
                editor.putFloat("${userPrefix}_chat_wallpaper_dim_$targetKey", dto.dim)
                editor.putFloat("${userPrefix}_chat_wallpaper_blur_$targetKey", dto.blur)

                if (dto.wallpaperType == "custom_image" && !dto.imageUrl.isNullOrBlank()) {
                    editor.putString("${userPrefix}_chat_wallpaper_cloud_url_$targetKey", dto.imageUrl)

                    // Download custom image to local disk cache if not already present
                    val localFile = downloadAndCacheCustomImage(context, userPrefix, targetKey, dto.imageUrl)
                    if (localFile != null && localFile.exists() && localFile.length() > 0) {
                        editor.putString("${userPrefix}_chat_wallpaper_custom_$targetKey", "file://${localFile.absolutePath}")
                    } else {
                        editor.putString("${userPrefix}_chat_wallpaper_custom_$targetKey", dto.imageUrl)
                    }
                } else if (wpId != "custom") {
                    editor.remove("${userPrefix}_chat_wallpaper_custom_$targetKey")
                }
            }

            editor.apply()
            _wallpaperUpdateTrigger.value = System.currentTimeMillis()
            Log.i(TAG, "Successfully synchronized ${list.size} wallpapers from cloud")
        } catch (e: Exception) {
            Log.e(TAG, "Exception during cloud wallpaper synchronization", e)
        }
    }

    /**
     * Caches compressed wallpaper bytes locally in app storage.
     */
    private fun cacheWallpaperBytesOnDisk(context: Context, userPrefix: String, targetKey: String, bytes: ByteArray): File? {
        return try {
            val dir = File(context.filesDir, "chat_wallpapers")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "wp_${userPrefix}_${targetKey}.webp")
            file.outputStream().use { it.write(bytes) }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache wallpaper locally", e)
            null
        }
    }

    /**
     * Downloads remote wallpaper photo into local cache for 0ms offline rendering.
     */
    private fun downloadAndCacheCustomImage(context: Context, userPrefix: String, targetKey: String, remoteUrl: String): File? {
        return try {
            val dir = File(context.filesDir, "chat_wallpapers")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "wp_${userPrefix}_${targetKey}.webp")

            // If already downloaded and valid, return immediately
            if (file.exists() && file.length() > 0) {
                return file
            }

            val url = URL(remoteUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 12000
            conn.instanceFollowRedirects = true

            if (conn.responseCode in 200..299) {
                conn.inputStream.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                file
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to download remote wallpaper $remoteUrl", e)
            null
        }
    }
}
