package com.example.connecto

import android.app.Application
import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import java.io.PrintWriter
import java.io.StringWriter

/**
 * ConnectoApplication sets up global uncaught exception catching to capture cold-start
 * and runtime crashes before the process dies, and configures a high-performance
 * in-memory LRU cache and disk cache for contact avatars and images.
 */
class ConnectoApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        installGlobalCrashCatcher()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(60L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }

    private fun installGlobalCrashCatcher() {
        try {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    val sw = StringWriter()
                    val pw = PrintWriter(sw)
                    throwable.printStackTrace(pw)
                    val fullStackTrace = sw.toString()
                    val lines = fullStackTrace.lines()
                    val topLines = lines.take(16).joinToString("\n")
                    val crashLog = "${throwable.javaClass.name}: ${throwable.message ?: "No message"}\n$topLines"

                    val prefs = getSharedPreferences("connecto_crash_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("last_crash_log", crashLog).commit()
                    Log.e("ConnectoCrashCatcher", "Crash log captured:\n$crashLog")
                } catch (t: Throwable) {
                    Log.e("ConnectoCrashCatcher", "Failed to persist crash log: ${t.message}")
                }
                defaultHandler?.uncaughtException(thread, throwable)
            }
        } catch (t: Throwable) {
            Log.e("ConnectoCrashCatcher", "Failed to install uncaught exception handler: ${t.message}")
        }
    }
}
