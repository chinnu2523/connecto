package com.connecto.store.notifications

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Native Android JobService scheduled to run autonomously in the background (even when
 * Our Store is closed/swiped away from Recents) to poll the catalog and dispatch notifications
 * if any installed app has a new update.
 */
class StoreUpdateJobService : JobService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "StoreUpdateJobService"
        const val JOB_ID = 9001
        // Periodic interval: 15 minutes (minimum periodic interval supported by JobScheduler)
        private const val PERIODIC_INTERVAL_MS = 15 * 60 * 1000L

        /**
         * Schedules the recurring background JobScheduler task.
         */
        fun schedule(context: Context) {
            try {
                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
                val componentName = ComponentName(context, StoreUpdateJobService::class.java)

                val builder = JobInfo.Builder(JOB_ID, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Requires internet connection
                    .setPersisted(true) // Survives device reboots
                    .setPeriodic(PERIODIC_INTERVAL_MS)

                val result = jobScheduler.schedule(builder.build())
                Log.d(TAG, "Scheduled background update check job #$JOB_ID with result=$result")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule StoreUpdateJobService", e)
            }
        }

        /**
         * Checks if the job is already scheduled.
         */
        fun isScheduled(context: Context): Boolean {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return false
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                jobScheduler.getPendingJob(JOB_ID) != null
            } else {
                jobScheduler.allPendingJobs.any { it.id == JOB_ID }
            }
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Autonomous background update scan starting...")

        serviceScope.launch {
            try {
                // 1. Fetch live catalog from edge API
                val apps = StoreApiClient.fetchApps()
                if (apps.isNotEmpty()) {
                    val sessionManager = StoreSessionManager(applicationContext)
                    val user = sessionManager.getUser()
                    val role = user?.role ?: StoreRole.MEMBER

                    // 2. Filter apps according to user role
                    val roleFiltered = if (role == StoreRole.ADMIN) {
                        apps
                    } else {
                        apps.filter { app ->
                            app.packageName == "com.connecto.store" ||
                            app.packageName == "com.app.pdfstudio" ||
                            app.packageName == "com.connecto.app" ||
                            app.id == "ourstore" ||
                            app.id == "connecto" ||
                            app.id == "pdfstudio"
                        }
                    }

                    // 3. Resolve install state for each app
                    val uiApps = roleFiltered.map { resolveInstallState(applicationContext, it) }

                    // 4. Dispatch Android system notifications for any updates
                    withContext(Dispatchers.Main) {
                        StoreNotificationHelper.checkAndNotifyUpdates(applicationContext, uiApps, forceNotify = true)
                    }

                    Log.d(TAG, "Autonomous background check complete. Scanned ${uiApps.size} apps.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Autonomous background update check failed", e)
            } finally {
                jobFinished(params, false)
            }
        }

        // Return true because work is continuing asynchronously on serviceScope
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Autonomous background update scan stopped prematurely")
        serviceScope.cancel()
        // Reschedule if job was interrupted
        return true
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
