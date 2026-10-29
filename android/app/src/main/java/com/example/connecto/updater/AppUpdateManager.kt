package com.example.connecto.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.connecto.BuildConfig
import com.example.connecto.network.ConnectoNetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateDetails(
    val isAvailable: Boolean,
    val latestVersionName: String,
    val latestVersionCode: Int,
    val changelog: List<String>,
    val sizeFormatted: String,
    val downloadUrl: String
)

object AppUpdateManager {
    private const val STORE_PACKAGE = "com.connecto.store"
    private const val STORE_WEB_URL = "https://connecto.fun/store"

    private val _updateState = MutableStateFlow<AppUpdateDetails?>(null)
    val updateState: StateFlow<AppUpdateDetails?> = _updateState.asStateFlow()

    fun isOurStoreInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getLaunchIntentForPackage(STORE_PACKAGE) != null
        } catch (_: Exception) {
            false
        }
    }

    suspend fun checkForUpdates(): AppUpdateDetails? = withContext(Dispatchers.IO) {
        val endpoint = "${ConnectoNetworkConfig.CLOUD_SERVER_URL}/api/v1/store/apps"
        return@withContext try {
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val apps = json.optJSONArray("apps")
                var foundUpdate: AppUpdateDetails? = null

                if (apps != null) {
                    for (i in 0 until apps.length()) {
                        val appObj = apps.getJSONObject(i)
                        val pkg = appObj.optString("package_name")
                        val id = appObj.optString("id")

                        if (pkg == "com.connecto.app" || id == "connecto") {
                            val serverCode = appObj.optInt("version_code", 0)
                            val serverName = appObj.optString("version_name", "Latest")
                            val sizeFormatted = appObj.optString("size_formatted", "")
                            val downloadUrl = appObj.optString("download_url", "https://connecto.fun/downloads/connecto-fun.apk")
                            val changelogList = mutableListOf<String>()
                            val clArr = appObj.optJSONArray("changelog")
                            if (clArr != null) {
                                for (c in 0 until clArr.length()) {
                                    changelogList.add(clArr.getString(c))
                                }
                            }

                            // Compare server version with current running APK version
                            val isNewer = serverCode > BuildConfig.VERSION_CODE
                            val details = AppUpdateDetails(
                                isAvailable = isNewer,
                                latestVersionName = serverName,
                                latestVersionCode = serverCode,
                                changelog = changelogList,
                                sizeFormatted = sizeFormatted,
                                downloadUrl = downloadUrl
                            )

                            if (isNewer) {
                                foundUpdate = details
                                _updateState.value = details
                            }
                            break
                        }
                    }
                }
                foundUpdate
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun openUpdateDestination(context: Context) {
        if (isOurStoreInstalled(context)) {
            val storeIntent = context.packageManager.getLaunchIntentForPackage(STORE_PACKAGE)
            if (storeIntent != null) {
                storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(storeIntent)
                return
            }
        }

        // Fallback: If Our Store is not installed, open Connecto website where they can download/update
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(STORE_WEB_URL)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
    }
}
