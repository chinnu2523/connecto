package com.connecto.store.installer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log

class InstallStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val packageName = intent.getStringExtra("package_name") ?: ""
        Log.d("OurStore", "PackageInstaller status received: $status for $packageName")

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                confirmIntent?.let {
                    // Security verification: Validate target component is system package installer across Android OEMs
                    val targetPkg = (it.component?.packageName ?: it.`package`)?.lowercase()
                    val isTrustedInstaller = targetPkg == null ||
                        targetPkg.contains("packageinstaller") ||
                        targetPkg.contains("android") ||
                        targetPkg.contains("oplus") ||
                        targetPkg.contains("coloros") ||
                        targetPkg.contains("miui") ||
                        targetPkg.contains("samsung") ||
                        targetPkg.contains("huawei") ||
                        targetPkg.contains("transsion")
                    if (!isTrustedInstaller) {
                        Log.w("OurStore", "Security warning: rejected untrusted confirmIntent targeting: $targetPkg")
                        return
                    }

                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
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
                        Log.e("OurStore", "Failed to start confirm intent with options, trying standard start", e)
                        try {
                            context.startActivity(it)
                        } catch (ex2: Exception) {
                            Log.e("OurStore", "Standard start also failed", ex2)
                        }
                    }
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Log.i("OurStore", "Package $packageName successfully installed via PackageInstaller!")
                val broadcast = Intent("com.connecto.store.PACKAGE_INSTALLED").apply {
                    putExtra("package_name", packageName)
                    setPackage(context.packageName)
                }
                context.sendBroadcast(broadcast)
            }
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Log.e("OurStore", "PackageInstaller failed with status: $status message: $message")
                val broadcast = Intent("com.connecto.store.PACKAGE_INSTALL_FAILED").apply {
                    putExtra("package_name", packageName)
                    putExtra("error_message", message)
                    setPackage(context.packageName)
                }
                context.sendBroadcast(broadcast)
            }
        }
    }
}
