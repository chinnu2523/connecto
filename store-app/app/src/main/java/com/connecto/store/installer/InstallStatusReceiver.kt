package com.connecto.store.installer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import com.connecto.store.notifications.StoreNotificationHelper

class InstallStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)
        val packageName = intent.getStringExtra("package_name") ?: ""
        if (!ApkInstaller.isKnownSession(sessionId)) {
            Log.w("OurStore", "Ignored unverified install status broadcast (sessionId=$sessionId, pkg=$packageName)")
            return
        }
        Log.d("OurStore", "PackageInstaller status received: $status for $packageName (session=$sessionId)")

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                confirmIntent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

                    // 1. Mandatory dispatch to MainActivity: Foreground Activity ALWAYS has permission to show system dialog
                    val notifyIntent = Intent("com.connecto.store.PACKAGE_INSTALL_PENDING_ACTION").apply {
                        putExtra("package_name", packageName)
                        putExtra(Intent.EXTRA_INTENT, it)
                        setPackage(context.packageName)
                    }
                    context.sendBroadcast(notifyIntent)

                    // 2. Direct activity launch attempt with BAL allowance (best effort for OEM systems)
                    try {
                        val options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            android.app.ActivityOptions.makeBasic().apply {
                                setPendingIntentBackgroundActivityStartMode(
                                    android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                                )
                            }.toBundle()
                        } else {
                            null
                        }
                        context.startActivity(it, options)
                    } catch (e: Exception) {
                        Log.d("OurStore", "Direct activity start from receiver not permitted: ${e.message}")
                    }

                    // 3. High-priority notification so user can tap to install even if Our Store was minimized
                    StoreNotificationHelper.notifyInstallPending(context, packageName, it)
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                ApkInstaller.markSessionFinished(sessionId)
                Log.i("OurStore", "Package $packageName successfully installed via PackageInstaller!")
                StoreDownloadCoordinator.markInstalled(packageName)
                StoreNotificationHelper.cancelInstallPending(context, packageName)
                val broadcast = Intent("com.connecto.store.PACKAGE_INSTALLED").apply {
                    putExtra("package_name", packageName)
                    setPackage(context.packageName)
                }
                context.sendBroadcast(broadcast)
            }
            else -> {
                ApkInstaller.markSessionFinished(sessionId)
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Log.e("OurStore", "PackageInstaller failed with status: $status message: $message")
                StoreNotificationHelper.cancelInstallPending(context, packageName)
                val isCancelled = status == PackageInstaller.STATUS_FAILURE_ABORTED ||
                    status == PackageInstaller.STATUS_FAILURE_INVALID ||
                    message?.contains("abort", ignoreCase = true) == true ||
                    message?.contains("cancel", ignoreCase = true) == true

                val broadcast = Intent("com.connecto.store.PACKAGE_INSTALL_FAILED").apply {
                    putExtra("package_name", packageName)
                    putExtra("error_message", if (isCancelled) null else message)
                    putExtra("is_cancelled", isCancelled)
                    setPackage(context.packageName)
                }
                context.sendBroadcast(broadcast)
            }
        }
    }
}
