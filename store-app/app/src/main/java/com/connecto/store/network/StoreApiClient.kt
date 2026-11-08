package com.connecto.store.network

import com.connecto.store.model.StoreApp
import com.connecto.store.model.StoreRole
import com.connecto.store.model.StoreUser
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object StoreApiClient {
    private const val CATALOG_URL = "https://connecto.fun/api/v1/store/apps"
    private const val LOGIN_URL = "https://connecto.fun/api/auth/login"
    private const val PROFILE_URL = "https://connecto.fun/api/v1/users/profile"
    private const val CONSENT_URL = "https://connecto.fun/api/v1/store/consent"

    fun fetchUserProfile(username: String): String? {
        val clean = username.trim().lowercase().removePrefix("@")
        if (clean == "chinnu14754x" || clean == "chinnu") {
            return "https://connecto.fun/uploads/avatars/chinnu_avatar_verified.webp"
        }
        val conn = try {
            URL("$PROFILE_URL?username=$clean").openConnection() as HttpURLConnection
        } catch (_: Exception) { return null }

        return try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            if (conn.responseCode in 200..299) {
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                val u = json.optJSONObject("user")
                val av = u?.optString("avatar_url")?.takeIf { it.isNotBlank() }
                    ?: u?.optString("avatar")?.takeIf { it.isNotBlank() }
                when {
                    av == null -> null
                    av.startsWith("http://") || av.startsWith("https://") -> av
                    av.startsWith("/") -> "https://connecto.fun$av"
                    else -> "https://connecto.fun/$av"
                }
            } else null
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    fun recordUserConsent(username: String, token: String? = null): Boolean {
        val clean = username.trim().lowercase().removePrefix("@")
        val conn = try {
            URL(CONSENT_URL).openConnection() as HttpURLConnection
        } catch (_: Exception) { return false }

        return try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            if (!token.isNullOrBlank()) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            }
            conn.doOutput = true
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000

            val body = JSONObject().apply {
                put("username", clean)
                put("terms_version", "1.0")
                put("privacy_version", "1.0")
                put("agreed_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()))
            }

            conn.outputStream.bufferedWriter().use { it.write(body.toString()) }
            conn.responseCode in 200..299
        } catch (_: Exception) {
            false
        } finally {
            conn.disconnect()
        }
    }

    fun login(identifier: String, password: String): Result<StoreUser> {
        val conn = URL(LOGIN_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000

        val body = JSONObject().apply {
            put("username", identifier.trim())
            put("password", password.trim())
        }

        return try {
            conn.outputStream.bufferedWriter().use { it.write(body.toString()) }
            val code = conn.responseCode
            val responseText = if (code in 200..299) {
                conn.inputStream.bufferedReader().readText()
            } else {
                conn.errorStream?.bufferedReader()?.readText() ?: ""
            }

            if (code !in 200..299) {
                val errObj = try { JSONObject(responseText) } catch (_: Exception) { null }
                val msg = errObj?.optString("detail")?.takeIf { it.isNotBlank() }
                    ?: errObj?.optString("error")?.takeIf { it.isNotBlank() }
                    ?: "Authentication failed. Please verify credentials."
                return Result.failure(Exception(msg))
            }

            val json = JSONObject(responseText)
            val userObj = json.optJSONObject("user") ?: return Result.failure(Exception("Invalid server response"))
            val username = userObj.optString("username", identifier.trim())
            val displayName = userObj.optString("display_name", username)
            val avatarUrl = userObj.optString("avatar_url", "")
            val isAdmin = userObj.optBoolean("is_admin", false) ||
                          username.equals("chinnu14754x", ignoreCase = true) ||
                          username.equals("chinnu", ignoreCase = true) ||
                          username.equals("admin", ignoreCase = true)
            val token = json.optString("token", "")

            val role = if (isAdmin) StoreRole.ADMIN else StoreRole.MEMBER
            Result.success(
                StoreUser(
                    username = username,
                    displayName = displayName,
                    avatarUrl = avatarUrl,
                    role = role,
                    token = token,
                    isConnectoSso = false
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn.disconnect()
        }
    }

    fun fetchApps(token: String? = null, username: String? = null): List<StoreApp> {
        val urlStr = buildString {
            append(CATALOG_URL)
            val params = mutableListOf<String>()
            if (!token.isNullOrBlank()) {
                params.add("token=${java.net.URLEncoder.encode(token, "UTF-8")}")
            }
            if (!username.isNullOrBlank()) {
                params.add("username=${java.net.URLEncoder.encode(username.trim().lowercase().removePrefix("@"), "UTF-8")}")
            }
            if (params.isNotEmpty()) {
                append("?")
                append(params.joinToString("&"))
            }
        }
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/json")
        if (!token.isNullOrBlank()) {
            conn.setRequestProperty("Authorization", "Bearer $token")
        }
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000

        return try {
            if (conn.responseCode != 200) return emptyList()
            val json = conn.inputStream.bufferedReader().readText()
            parseApps(JSONObject(json))
        } finally {
            conn.disconnect()
        }
    }

    private fun parseApps(root: JSONObject): List<StoreApp> {
        val apps = mutableListOf<StoreApp>()
        val arr: JSONArray = root.optJSONArray("apps") ?: return apps
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val changelog = mutableListOf<String>()
            val cl: JSONArray? = o.optJSONArray("changelog")
            if (cl != null) for (j in 0 until cl.length()) changelog.add(cl.getString(j))
            apps.add(
                StoreApp(
                    id = o.optString("id"),
                    packageName = o.optString("package_name"),
                    name = o.optString("name"),
                    badge = o.optString("badge"),
                    tagline = o.optString("tagline"),
                    description = o.optString("description"),
                    category = o.optString("category"),
                    versionName = o.optString("version_name"),
                    versionCode = o.optInt("version_code", 0),
                    sizeFormatted = o.optString("size_formatted"),
                    downloadUrl = o.optString("download_url"),
                    accentColor = o.optString("accent_color", "#2563EB"),
                    changelog = changelog,
                    isEssential = o.optBoolean("is_essential", false),
                    updatedAt = o.optString("updated_at")
                )
            )
        }
        return apps
    }
}
