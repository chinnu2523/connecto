package com.connecto.store.installer

import android.content.Context
import com.connecto.store.model.StoreApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class DownloadTaskState(
    val isDownloading: Boolean = false,
    val isInstalling: Boolean = false,
    val downloadProgress: Int = 0,
    val error: String? = null
)

/**
 * Application-scoped download & install coordinator.
 * Ensures APK downloads continue uninterrupted in the background when the user navigates back
 * or leaves MainActivity, and preserves live progress across UI re-mounts.
 */
object StoreDownloadCoordinator {
    private val coordinatorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val _states = MutableStateFlow<Map<String, DownloadTaskState>>(emptyMap())
    val states: StateFlow<Map<String, DownloadTaskState>> = _states.asStateFlow()

    @Volatile
    var pendingInstallFile: File? = null
    @Volatile
    var pendingPackageName: String? = null

    fun getState(packageName: String): DownloadTaskState? = _states.value[packageName]

    private fun updateState(packageName: String, transform: (DownloadTaskState) -> DownloadTaskState) {
        _states.update { map ->
            val current = map[packageName] ?: DownloadTaskState()
            map + (packageName to transform(current))
        }
    }

    fun clearState(packageName: String) {
        _states.update { map -> map - packageName }
    }

    fun startDownloadAndInstall(
        appContext: Context,
        app: StoreApp,
        expectedSizeBytes: Long?,
        onPermissionRequired: (StoreApp, File) -> Unit
    ) {
        val current = _states.value[app.packageName]
        if (current?.isDownloading == true || activeJobs[app.packageName]?.isActive == true) {
            return
        }

        updateState(app.packageName) {
            DownloadTaskState(isDownloading = true, isInstalling = false, downloadProgress = 0, error = null)
        }

        val job = coordinatorScope.launch {
            try {
                val fileName = "${app.id}-${app.versionName}.apk"
                val file = ApkInstaller.downloadApk(
                    context = appContext,
                    url = app.downloadUrl,
                    fileName = fileName,
                    expectedSha256 = app.sha256,
                    expectedSizeBytes = expectedSizeBytes
                ) { progress ->
                    updateState(app.packageName) { state ->
                        val nextProgress = if (state.isDownloading && progress < state.downloadProgress) {
                            state.downloadProgress
                        } else {
                            progress
                        }
                        state.copy(isDownloading = true, downloadProgress = nextProgress)
                    }
                }

                if (file != null) {
                    updateState(app.packageName) {
                        it.copy(isDownloading = false, isInstalling = true, downloadProgress = 100, error = null)
                    }

                    // Watchdog to clear stale isInstalling if OS never reports completion
                    coordinatorScope.launch {
                        delay(25_000L)
                        val s = _states.value[app.packageName]
                        if (s?.isInstalling == true) {
                            updateState(app.packageName) { it.copy(isInstalling = false) }
                        }
                    }

                    if (!ApkInstaller.canRequestPackageInstalls(appContext)) {
                        pendingInstallFile = file
                        pendingPackageName = app.packageName
                        onPermissionRequired(app, file)
                    } else {
                        val started = ApkInstaller.installApk(appContext, file, app.packageName)
                        if (!started) {
                            updateState(app.packageName) {
                                it.copy(
                                    isDownloading = false,
                                    isInstalling = false,
                                    error = "Package installer could not be launched. Tap to retry."
                                )
                            }
                        }
                    }
                } else {
                    updateState(app.packageName) {
                        it.copy(
                            isDownloading = false,
                            isInstalling = false,
                            downloadProgress = 0,
                            error = "Download could not be completed. Tap to retry."
                        )
                    }
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                updateState(app.packageName) {
                    DownloadTaskState(isDownloading = false, isInstalling = false, downloadProgress = 0, error = null)
                }
            } finally {
                activeJobs.remove(app.packageName)
            }
        }
        activeJobs[app.packageName] = job
    }

    fun cancelDownload(packageName: String) {
        activeJobs.remove(packageName)?.cancel()
        updateState(packageName) {
            DownloadTaskState(isDownloading = false, isInstalling = false, downloadProgress = 0, error = null)
        }
    }

    fun markInstallFailed(packageName: String, errorMessage: String?, isCancelled: Boolean) {
        updateState(packageName) {
            DownloadTaskState(
                isDownloading = false,
                isInstalling = false,
                downloadProgress = 0,
                error = if (isCancelled) null else (errorMessage ?: "Installation failed. Tap to retry.")
            )
        }
    }

    fun markInstalled(packageName: String) {
        clearState(packageName)
    }
}
