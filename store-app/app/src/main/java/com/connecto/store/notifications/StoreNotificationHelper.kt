package com.connecto.store.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.connecto.store.MainActivity
import com.connecto.store.model.StoreApp
import com.connecto.store.viewmodel.AppUiState

object StoreNotificationHelper {
    private const val TAG = "StoreNotification"
    const val CHANNEL_ID = "our_store_updates"
    private const val PREFS_NAME = "our_store_notifications"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Application Updates"
            val descriptionText = "Alerts when updates are available for installed ecosystem applications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Inspects current app states and fires notifications for any installed app that has an update available.
     * Cancels notifications for apps that are already up-to-date.
     */
    fun checkAndNotifyUpdates(context: Context, apps: List<AppUiState>, forceNotify: Boolean = false) {
        initNotificationChannel(context)

        // Permission check on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted; skipping notification dispatch")
                return
            }
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val pendingUpdates = apps.filter { it.installState == StoreApp.InstallState.UPDATE_AVAILABLE }

        // Cancel notifications for apps that have been updated to UP_TO_DATE
        val upToDateApps = apps.filter { it.installState == StoreApp.InstallState.UP_TO_DATE }
        val notificationManager = NotificationManagerCompat.from(context)
        for (installed in upToDateApps) {
            notificationManager.cancel(installed.app.packageName.hashCode())
        }

        val now = System.currentTimeMillis()
        for (appState in pendingUpdates) {
            val app = appState.app
            val oldVer = appState.installedVersionName ?: "Installed"
            val prefKey = "notified_${app.packageName}"
            val timeKey = "notified_time_${app.packageName}"
            val lastNotifiedVer = prefs.getString(prefKey, null)
            val lastNotifiedTime = prefs.getLong(timeKey, 0L)
            val timeSinceLastMs = now - lastNotifiedTime

            // Notify if forced, or version changed, or if running as periodic reminder (> 10 minutes since last)
            if (forceNotify || lastNotifiedVer != app.versionName || timeSinceLastMs > 10 * 60 * 1000L) {
                sendUpdateNotification(context, app, oldVer)
                prefs.edit()
                    .putString(prefKey, app.versionName)
                    .putLong(timeKey, now)
                    .apply()
            }
        }
    }

    /**
     * Dispatches an individual Android system notification for an updated app with deep linking.
     */
    fun sendUpdateNotification(context: Context, app: StoreApp, oldVersion: String) {
        initNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("OPEN_PACKAGE", app.packageName)
            putExtra("OPEN_SECTION", if (app.packageName == "com.connecto.store" || app.id == "ourstore") "LOGIN" else "UPDATES")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            app.packageName.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifTitle = if (app.packageName == "com.connecto.store" || app.id == "ourstore") {
            "Our Store Update Available"
        } else {
            "${app.name} Update Available"
        }
        val notifContent = if (app.packageName == "com.connecto.store" || app.id == "ourstore") {
            "Version ${app.versionName} is ready to install in the Account tab."
        } else {
            "${app.name} v${app.versionName} is available for update."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(notifTitle)
            .setContentText(notifContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.stat_sys_download,
                "UPDATE",
                pendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(app.packageName.hashCode(), notification)
            Log.d(TAG, "Successfully posted update notification for ${app.name} v${app.versionName}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification for ${app.name}", e)
        }
    }

    fun clearNotifiedVersion(context: Context, packageName: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove("notified_$packageName")
            .apply()
    }
}
