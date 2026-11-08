package com.connecto.store.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.connecto.store.installer.ApkInstaller
import com.connecto.store.model.StoreApp
import com.connecto.store.model.StoreRole
import com.connecto.store.model.StoreSessionManager
import com.connecto.store.network.StoreApiClient
import com.connecto.store.viewmodel.AppUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Resilient, battery-efficient AlarmManager BroadcastReceiver that runs autonomously
 * even when Our Store has been swiped from Recents / background killed.
 */
class StoreUpdateAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "StoreUpdateAlarmRcvr"
        const val ACTION_CHECK_UPDATES = "com.connecto.store.ACTION_CHECK_UPDATES"
        private const val REQUEST_CODE = 9002
        private const val INTERVAL_MS = 15 * 60 * 1000L // 15 minutes

        /**
         * Schedules the autonomous background alarm.
         * If delayMs is provided, schedules the first check after that delay.
         */
        fun scheduleNext(context: Context, delayMs: Long = INTERVAL_MS) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val intent = Intent(context, StoreUpdateAlarmReceiver::class.java).apply {
                    action = ACTION_CHECK_UPDATES
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val triggerAtMillis = System.currentTimeMillis() + delayMs
                val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }

                if (canExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Scheduled autonomous background update alarm in ${delayMs / 1000}s")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule StoreUpdateAlarmReceiver", e)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "StoreUpdateAlarmReceiver awakened from background! Action: ${intent?.action}")

        val pendingResult = goAsync()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ourstore:background_update_wake"
        )
        wakeLock?.acquire(30_000L) // 30s max wake

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val sessionManager = StoreSessionManager(context)
                val user = sessionManager.getUser()
                val role = user?.role ?: StoreRole.GUEST

                // 1. Fetch live apps from edge catalog with user authentication
                val apps = StoreApiClient.fetchApps(user?.token, user?.username)
                if (apps.isNotEmpty()) {
                    // 2. Strict RBAC: JARVIS AI is strictly visible only for Admin accounts
                    val roleFiltered = if (role == StoreRole.ADMIN) {
                        apps
                    } else {
                        apps.filter { app ->
                            app.id != "jarvis" && app.packageName != "com.omni.assistant"
                        }
                    }

                    // 3. Resolve install state
                    val uiApps = roleFiltered.map { resolveInstallState(context, it) }

                    // 4. Force notify any available updates so user sees it in Android system tray
                    withContext(Dispatchers.Main) {
                        StoreNotificationHelper.checkAndNotifyUpdates(
                            context = context,
                            apps = uiApps,
                            forceNotify = true
                        )
                    }

                    Log.d(TAG, "Autonomous background alarm check complete. Processed ${uiApps.size} apps.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Autonomous background alarm scan failed", e)
            } finally {
                // 5. Always schedule next recurring alarm
                scheduleNext(context, INTERVAL_MS)
                try {
                    if (wakeLock?.isHeld == true) wakeLock.release()
                } catch (_: Exception) {}
                pendingResult.finish()
            }
        }
    }

    private fun resolveInstallState(ctx: Context, app: StoreApp): AppUiState {
        val installedCode = ApkInstaller.getInstalledVersionCode(ctx, app.packageName)
        val (installState, installedVersionName) = when {
            installedCode < 0 -> StoreApp.InstallState.NOT_INSTALLED to null
            installedCode < app.versionCode -> StoreApp.InstallState.UPDATE_AVAILABLE to getVersionName(ctx, app.packageName)
            else -> StoreApp.InstallState.UP_TO_DATE to getVersionName(ctx, app.packageName)
        }
        return AppUiState(app, installState, installedVersionName)
    }

    private fun getVersionName(ctx: Context, packageName: String): String? {
        return try {
            ctx.packageManager.getPackageInfo(packageName, 0).versionName
        } catch (_: Exception) { null }
    }
}
