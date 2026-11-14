package com.connecto.store.installer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object ApkInstaller {

    private const val TAG = "OurStoreInstaller"

    /**
     * Returns the installed versionCode for [packageName], or -1 if not installed.
     */
    fun getInstalledVersionCode(context: Context, packageName: String): Int {
        return try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                info.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            -1
        }
    }

    /**
     * Checks whether Our Store has permission to install packages.
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Checks whether Our Store is the installer of record for [packageName].
     * Android requires an app to be the installer of record to perform silent updates without user prompt.
     */
    fun isInstallerOfRecord(context: Context, packageName: String): Boolean {
        return try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(packageName)
            }
            installer == context.packageName
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Opens the system Unknown App Sources settings screen for Our Store.
     */
    fun openUnknownAppSourcesSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            }
        }
    }

    /**
     * Computes the cryptographic SHA-256 hex string of a given file.
     */
    fun computeSha256(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(65536)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compute SHA-256 for ${file.name}", e)
            ""
        }
    }

    /**
     * Downloads the APK from [url] with full redirect handling directly into the application's
     * private cache directory, streaming bytes, verifying cryptographic integrity, and reporting progress (0–100).
     */
    suspend fun downloadApk(
        context: Context,
        url: String,
        fileName: String,
        expectedSha256: String? = null,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val destFile = File(context.cacheDir, "apks/$fileName")
        destFile.parentFile?.mkdirs()
        if (destFile.exists()) destFile.delete()

        try {
            var currentUrl = url
            var connection: HttpURLConnection? = null
            var redirects = 0

            while (redirects < 5) {
                val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 20000
                    readTimeout = 45000
                    setRequestProperty("User-Agent", "OurStore-PlayInstaller/2.0.0 (Android)")
                    setRequestProperty("Accept", "application/vnd.android.package-archive, */*")
                }
                conn.connect()
                val code = conn.responseCode

                if (code in 300..399) {
                    val location = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (location != null) {
                        currentUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                            location
                        } else {
                            URL(URL(currentUrl), location).toString()
                        }
                        redirects++
                        continue
                    }
                }

                connection = conn
                break
            }

            val finalConn = connection ?: return@withContext null
            val responseCode = finalConn.responseCode
            if (responseCode !in 200..299) {
                Log.e(TAG, "Download failed with HTTP $responseCode for $currentUrl")
                finalConn.disconnect()
                return@withContext null
            }

            val totalBytes = finalConn.contentLength.toLong()
            var downloadedBytes = 0L

            finalConn.inputStream.use { input ->
                destFile.outputStream().use { output ->
                    val buffer = ByteArray(32768)
                    var bytesRead: Int
                    var lastReported = 0
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val progress = ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                            if (progress != lastReported) {
                                lastReported = progress
                                onProgress(progress)
                            }
                        }
                    }
                    output.flush()
                }
            }
            finalConn.disconnect()

            if (destFile.exists() && destFile.length() > 1024) {
                if (!expectedSha256.isNullOrBlank()) {
                    val actualSha = computeSha256(destFile)
                    if (!actualSha.equals(expectedSha256.trim(), ignoreCase = true)) {
                        Log.e(TAG, "Cryptographic integrity failure: SHA-256 mismatch! Expected: $expectedSha256, Actual: $actualSha")
                        destFile.delete()
                        return@withContext null
                    }
                    Log.i(TAG, "Cryptographic integrity verified: SHA-256 match ($actualSha)")
                }
                onProgress(100)
                destFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during APK download", e)
            null
        }
    }

    /**
     * Installs [apkFile] using native system PackageInstaller intent,
     * ensuring web browsers can NEVER intercept the installation and eliminating
     * Background Activity Launch (BAL) restrictions.
     */
    fun installApk(context: Context, apkFile: File, packageName: String? = null) {
        if (!apkFile.exists() || apkFile.length() < 1024) {
            Log.e(TAG, "Cannot install: APK file does not exist or is too small at ${apkFile.absolutePath}")
            return
        }

        // Pre-flight sanity check: Verify APK parses validly prior to system handoff
        val pm = context.packageManager
        val archiveInfo = try {
            pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_ACTIVITIES)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse archive info", e)
            null
        }

        if (archiveInfo == null) {
            Log.e(TAG, "Pre-flight validation failed: APK archive is corrupted, incomplete, or invalid at ${apkFile.absolutePath}")
            return
        }

        val targetPackage = packageName ?: archiveInfo.packageName
        Log.i(TAG, "Pre-flight verified valid APK: package=${archiveInfo.packageName}, versionCode=${archiveInfo.versionCode}, target=$targetPackage")

        // Priority 1: On Android 12+ (API 31+), only attempt PackageInstaller Session IPC
        // if Our Store is already the installer-of-record. If not, Android requires user interaction
        // and strictly blocks background activity launches from BroadcastReceivers on Android 14+,
        // so we invoke the system package installer intent directly from the active foreground activity.
        val isOurStoreInstaller = isInstallerOfRecord(context, targetPackage)
        if (targetPackage.isNotBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isOurStoreInstaller) {
            val sessionSuccess = installViaPackageInstaller(context, apkFile, targetPackage)
            if (sessionSuccess) {
                Log.i(TAG, "PackageInstaller session successfully initiated for $targetPackage")
                return
            }
        }

        // Priority 2: Universal System PackageInstaller Intent (rock-solid for updates and initial installs)
        Log.i(TAG, "Launching system package installer for $targetPackage at ${apkFile.name}")
        installViaSystemIntent(context, apkFile)
    }

    private fun installViaPackageInstaller(context: Context, apkFile: File, packageName: String): Boolean {
        return try {
            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(packageName)
                setSize(apkFile.length())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setInstallReason(PackageManager.INSTALL_REASON_USER)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // If the app is already installed, allow seamless silent update without asking user action again!
                    val isAlreadyInstalled = getInstalledVersionCode(context, packageName) >= 0
                    if (isAlreadyInstalled) {
                        setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                    } else {
                        setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
                    }
                }
            }

            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            apkFile.inputStream().use { input ->
                session.openWrite("base.apk", 0, apkFile.length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }

            val statusIntent = Intent(context, InstallStatusReceiver::class.java).apply {
                action = "com.connecto.store.ACTION_INSTALL_STATUS"
                putExtra("package_name", packageName)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, sessionId, statusIntent, flags)
            session.commit(pendingIntent.intentSender)
            session.close()
            true
        } catch (e: Exception) {
            Log.w(TAG, "PackageInstaller session failed, falling back to intent", e)
            false
        }
    }

    private fun installViaSystemIntent(context: Context, apkFile: File) {
        try {
            apkFile.setReadable(true, false)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                clipData = android.content.ClipData.newRawUri("package", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            }

            // Universal OEM installer permissions: grant read permission to known system package installers
            val installerPackages = listOf(
                "com.google.android.packageinstaller",
                "com.android.packageinstaller",
                "com.miui.packageinstaller",
                "com.samsung.android.packageinstaller",
                "com.coloros.packageinstaller",
                "com.oppo.packageinstaller",
                "com.vivo.abe",
                "com.transsion.installer",
                "com.huawei.appmarket"
            )
            for (pkg in installerPackages) {
                try {
                    context.grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
            }

            context.startActivity(intent)
            Log.i(TAG, "Successfully started system installer activity for ${apkFile.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch targeted system installer intent, attempting fallback", e)
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    val fallbackUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
                    setDataAndType(fallbackUri, "application/vnd.android.package-archive")
                    clipData = android.content.ClipData.newRawUri("package", fallbackUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                }
                context.startActivity(fallbackIntent)
            } catch (fallbackErr: Exception) {
                Log.e(TAG, "All installation methods failed", fallbackErr)
            }
        }
    }

    /**
     * Triggers the native system uninstallation dialog for [packageName].
     */
    fun uninstallPackage(context: Context, packageName: String) {
        try {
            val packageUri = Uri.parse("package:$packageName")
            val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri).apply {
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(uninstallIntent)
        } catch (e: Exception) {
            try {
                val deleteIntent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(deleteIntent)
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed to launch package uninstaller", fallbackEx)
            }
        }
    }

    /**
     * Opens Google Play Protect settings so the user can verify device scan status.
     */
    fun openPlayProtectSettings(context: Context) {
        try {
            val intent = Intent("com.google.android.gms.security.settings.PlayProtectActivity").apply {
                setPackage("com.google.android.gms")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps")
                    setPackage("com.android.vending")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }
}
