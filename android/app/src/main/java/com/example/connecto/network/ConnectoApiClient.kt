package com.example.connecto.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class AuthResult(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val token: String?,
    val avatarUrl: String?,
    val phoneNumber: String? = null,
    val twoFactorEnabled: Boolean = false,
    val twoFactorMethod: String? = "sms",
    val requires2Fa: Boolean = false,
    val maskedDestination: String? = null,
    val devOtp: String? = null
)

data class OtpRequestResult(
    val status: String,
    val message: String,
    val method: String,
    val maskedDestination: String?,
    val identifier: String?,
    val devOtp: String? = null
)

data class OtpVerifyResult(
    val status: String,
    val verified: Boolean,
    val identifier: String?,
    val message: String
)

data class TwoFactorToggleResult(
    val status: String,
    val twoFactorEnabled: Boolean,
    val twoFactorMethod: String?,
    val phoneNumber: String?,
    val message: String
)

data class UserDto(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?
)

data class ChannelDto(
    val id: String,
    val name: String,
    val type: String
)

data class PollOptionDto(
    val text: String,
    val votes: List<String> = emptyList()
)

data class PollDto(
    val id: String,
    val question: String,
    val options: List<PollOptionDto> = emptyList(),
    val author: String = "",
    val authorName: String = "",
    val channelId: String = "",
    val multiple: Boolean = false,
    val totalVotes: Int = 0,
    val closed: Boolean = false,
    val createdAt: String = "",
    val timestamp: String = ""
)

data class MessageDto(
    val id: String,
    val channelId: String?,
    val authorId: String,
    val authorName: String,
    val content: String,
    val createdAt: String,
    val authorAvatar: String? = null,
    val type: String = "text",
    val pollId: String? = null,
    val poll: PollDto? = null,
    val timerSeconds: Int? = null,
    val expiresAt: String? = null
)

data class UserSearchResultDto(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val relationStatus: String
)

data class FriendRequestItemDto(
    val id: String,
    val senderId: String,
    val senderUsername: String,
    val senderDisplayName: String,
    val senderAvatarUrl: String?,
    val createdAt: String
)

data class FriendshipDto(
    val id: String,
    val userId: String,
    val friendId: String,
    val friendUsername: String,
    val friendDisplayName: String,
    val friendAvatarUrl: String?,
    val status: String,
    val isOnline: Boolean = false,
    val isStealth: Boolean = false,
    val bio: String? = null
)

data class NotificationDto(
    val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val content: String,
    val senderUsername: String? = null,
    val senderAvatar: String? = null,
    val referenceId: String? = null,
    val isRead: Boolean = false,
    val createdAt: String = ""
)

data class LeaderboardUserDto(
    val username: String,
    val nickname: String,
    val avatar: String?,
    val rank: String,
    val xp: Int,
    val streak: Int,
    val badge: String
)

data class LeaderboardResponseDto(
    val totalShinobi: Int,
    val leaderboard: List<LeaderboardUserDto>
)

data class PlatformStatsDto(
    val activeShinobi: Int,
    val clansFormed: Int,
    val messagesSent: Int,
    val onlineUsers: Int
)

data class VoiceParticipantDto(
    val username: String,
    val nickname: String,
    val avatar: String?,
    val rank: String?,
    val muted: Boolean = false,
    val speaking: Boolean = false
)

data class VoiceRoomDto(
    val id: String,
    val name: String,
    val topic: String,
    val icon: String,
    val creator: String,
    val participantCount: Int,
    val participants: List<VoiceParticipantDto>,
    val code: String = ""
)

data class CallLogDto(
    val id: String,
    val username: String,
    val callerName: String,
    val callType: String,
    val roomName: String = "",
    val roomCode: String = "",
    val durationSeconds: Int = 0,
    val isMissed: Boolean = false,
    val isOutgoing: Boolean = true,
    val createdAt: String = ""
)

data class UsernameCheckResult(
    val available: Boolean = false,
    val valid: Boolean = false,
    val message: String = "",
    val username: String = ""
)

data class EmailCheckResult(
    val valid: Boolean = false,
    val available: Boolean = false,
    val message: String = "",
    val email: String = ""
)

data class ProfileDataDto(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val fullName: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val location: String? = null,
    val usernameChanged: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val twoFactorMethod: String? = "sms",
    val isStealth: Boolean = false,
    val isOnline: Boolean = true
)

object ConnectoApiClient {

    @Volatile
    var sessionToken: String? = null

    @Volatile
    var sessionCookie: String? = null

    @Volatile
    var currentUserId: String? = null

    @Volatile
    var currentUsername: String? = null

    @Volatile
    var currentUserEmail: String? = null

    @Volatile
    var currentUserDisplayName: String? = null

    // Set by WebSocket typing events; null means no one is typing in the community channel
    @Volatile
    var activeTypingUser: String? = null

    @Volatile
    var currentUserAvatar: String? = null

    private val _currentUserAvatarState = MutableStateFlow<String?>(null)
    val currentUserAvatarState: StateFlow<String?> = _currentUserAvatarState.asStateFlow()

    val currentUserAvatarUrl: String?
        get() = currentUserAvatar ?: _currentUserAvatarState.value

    @Volatile
    var cachedVoiceRooms: List<VoiceRoomDto> = emptyList()

    @Volatile
    var cachedCallLogs: List<CallLogDto> = emptyList()

    @Volatile
    var isStealthMode: Boolean = false

    private val _isStealthModeState = MutableStateFlow<Boolean>(false)
    val isStealthModeState: StateFlow<Boolean> = _isStealthModeState.asStateFlow()

    fun updateStealthModeState(stealth: Boolean) {
        isStealthMode = stealth
        _isStealthModeState.value = stealth
    }

    suspend fun setPresenceOffline(username: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = (username ?: currentUsername)?.trim()?.lowercase()?.removePrefix("@") ?: return@withContext Result.success(false)
            val jsonBody = org.json.JSONObject().apply {
                put("username", user)
            }
            val res = executeRequest(
                endpoint = "/api/presence/offline",
                method = "POST",
                body = jsonBody.toString(),
                token = sessionToken
            )
            Result.success(res.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private var applicationContext: android.content.Context? = null

    fun updateAvatarState(avatarUrl: String?, context: android.content.Context? = null) {
        currentUserAvatar = avatarUrl
        _currentUserAvatarState.value = avatarUrl
        val ctx = context ?: applicationContext
        if (ctx != null && !avatarUrl.isNullOrBlank()) {
            try {
                val prefs = ctx.getSharedPreferences("connecto_session_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("avatar_url", avatarUrl).apply()
                val user = currentUsername
                if (!user.isNullOrBlank()) {
                    val pPrefs = ctx.getSharedPreferences("connecto_user_profile_prefs", android.content.Context.MODE_PRIVATE)
                    pPrefs.edit().putString("photo_uri_$user", avatarUrl).apply()
                }
            } catch (_: Exception) {}
        }
    }

    fun initSessionFromPrefs(context: android.content.Context) {
        applicationContext = context.applicationContext
        try {
            val prefs = context.getSharedPreferences("connecto_session_prefs", android.content.Context.MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("is_logged_in", false)
            val savedUsername = prefs.getString("username", null)
            val savedToken = prefs.getString("token", null)
            if (isLoggedIn && !savedUsername.isNullOrEmpty()) {
                sessionToken = savedToken
                currentUserId = prefs.getString("user_id", null)
                currentUsername = savedUsername
                currentUserEmail = prefs.getString("email", null)
                currentUserDisplayName = prefs.getString("display_name", null)
                val savedAvatar = prefs.getString("avatar_url", null)
                updateAvatarState(savedAvatar)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    fun getPersistedToken(): String? {
        val inMemory = sessionToken
        if (!inMemory.isNullOrBlank()) return inMemory
        val ctx = applicationContext ?: return null
        return try {
            val prefs = ctx.getSharedPreferences("connecto_session_prefs", android.content.Context.MODE_PRIVATE)
            val t = prefs.getString("token", null)?.takeIf { it.isNotBlank() }
            if (t != null) {
                sessionToken = t
            }
            t
        } catch (_: Exception) {
            null
        }
    }

    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 Connecto/2.5"

    suspend fun checkHealth(): Boolean = withContext(Dispatchers.IO) {
        val candidateUrls = if (ConnectoNetworkConfig.activeBaseUrl.contains("localhost") || ConnectoNetworkConfig.activeBaseUrl.contains("10.0.2.2")) {
            listOf(ConnectoNetworkConfig.activeBaseUrl, "http://localhost:8000", "http://10.0.2.2:8000").distinct()
        } else {
            listOf(ConnectoNetworkConfig.activeBaseUrl)
        }
        val healthEndpoints = listOf("/api/health", "/api/status", "/api/v1/health", "/api/members")
        for (base in candidateUrls) {
            for (path in healthEndpoints) {
                try {
                    android.util.Log.d("ConnectoSync", "checkHealth: connecting to $base$path")
                    val url = URL("$base$path")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 8000
                        readTimeout = 8000
                        setRequestProperty("User-Agent", USER_AGENT)
                        setRequestProperty("Accept", "application/json")
                    }
                    val code = conn.responseCode
                    android.util.Log.d("ConnectoSync", "checkHealth: $base$path returned $code")
                    if (code in 200..299) {
                        ConnectoNetworkConfig.activeBaseUrl = base
                        return@withContext true
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ConnectoSync", "checkHealth failed for $base$path: ${e.javaClass.name}: ${e.message}", e)
                    // try next path
                }
            }
        }
        false
    }

    suspend fun signup(
        email: String,
        username: String,
        displayName: String,
        password: String
    ): Result<AuthResult> = withContext(Dispatchers.IO) {
        try {
            val cleanUser = username.trim().lowercase()
            val cleanEmail = email.trim().lowercase()
            val cleanName = displayName.trim().ifEmpty { username.trim() }

            // Dual-compatible attempt — matches both production RegisterRequest and local UserSignup schemas
            val body = JSONObject().apply {
                put("username", cleanUser)
                put("nickname", cleanName)
                put("display_name", cleanName)
                put("email", cleanEmail)
                put("password", password)
            }

            var response = executeRequest(
                endpoint = "/api/auth/register",
                method = "POST",
                body = body.toString(),
                token = null
            )
            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/register",
                    method = "POST",
                    body = body.toString(),
                    token = null
                )
            }
            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/v1/auth/signup",
                    method = "POST",
                    body = body.toString(),
                    token = null
                )
            }

            if (response.isSuccess) {
                val jsonObj = JSONObject(response.getOrThrow())
                val userObj = jsonObj.optJSONObject("user") ?: jsonObj
                val tokenStr = jsonObj.optString("token").takeIf { it.isNotEmpty() }
                    ?: userObj.optString("token").takeIf { it.isNotEmpty() }

                val authResult = AuthResult(
                    id = userObj.optString("id").ifEmpty { userObj.optString("uid", UUID.randomUUID().toString()) },
                    username = userObj.optString("username", cleanUser),
                    displayName = userObj.optString("nickname").ifEmpty { userObj.optString("display_name", cleanName) },
                    email = userObj.optString("email", cleanEmail),
                    token = tokenStr,
                    avatarUrl = userObj.optString("avatar").ifEmpty { userObj.optString("avatar_url").takeIf { it.isNotEmpty() } }
                )

                sessionToken = authResult.token
                currentUserId = authResult.id
                currentUsername = authResult.username
                currentUserEmail = authResult.email
                currentUserDisplayName = authResult.displayName
                Result.success(authResult)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Sign up failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkUsernameAvailability(username: String): Result<UsernameCheckResult> = withContext(Dispatchers.IO) {
        try {
            val clean = username.trim()
            val encoded = java.net.URLEncoder.encode(clean, "UTF-8")
            var resp = executeRequest(
                endpoint = "/api/auth/check-username?username=$encoded",
                method = "GET",
                body = null,
                token = null
            )
            if (!resp.isSuccess) {
                resp = executeRequest(
                    endpoint = "/api/users/check-username?username=$encoded",
                    method = "GET",
                    body = null,
                    token = null
                )
            }
            if (resp.isSuccess) {
                val json = JSONObject(resp.getOrThrow())
                val available = json.optBoolean("available", false)
                val valid = json.optBoolean("valid", false)
                val msg = json.optString("message", if (available) "Username available ✓" else "Username is already taken")
                val u = json.optString("username", clean)
                Result.success(UsernameCheckResult(available = available, valid = valid, message = msg, username = u))
            } else {
                Result.failure(resp.exceptionOrNull() ?: Exception("Failed to check username"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkEmailAvailability(email: String): Result<EmailCheckResult> = withContext(Dispatchers.IO) {
        try {
            val clean = email.trim()
            val encoded = java.net.URLEncoder.encode(clean, "UTF-8")
            val resp = executeRequest(
                endpoint = "/api/auth/check-email?email=$encoded",
                method = "GET",
                body = null,
                token = null
            )
            if (resp.isSuccess) {
                val json = JSONObject(resp.getOrThrow())
                val valid = json.optBoolean("valid", false)
                val available = json.optBoolean("available", false)
                val msg = json.optString("message", if (valid && available) "Email is valid & verified ✓" else "Invalid email format")
                val e = json.optString("email", clean)
                Result.success(EmailCheckResult(valid = valid, available = available, message = msg, email = e))
            } else {
                Result.failure(resp.exceptionOrNull() ?: Exception("Failed to check email"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        loginIdentifier: String,
        password: String
    ): Result<AuthResult> = withContext(Dispatchers.IO) {
        try {
            val cleanIdentifier = loginIdentifier.trim()

            // Dual-compatible attempt — supports both "username" (production) and "login" (local)
            val body = JSONObject().apply {
                put("username", cleanIdentifier)
                put("login", cleanIdentifier)
                put("password", password)
            }

            var response = executeRequest(
                endpoint = "/api/auth/login",
                method = "POST",
                body = body.toString(),
                token = null
            )
            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/login",
                    method = "POST",
                    body = body.toString(),
                    token = null
                )
            }
            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/v1/auth/login",
                    method = "POST",
                    body = body.toString(),
                    token = null
                )
            }

            if (response.isSuccess) {
                val jsonObj = JSONObject(response.getOrThrow())
                val userObj = jsonObj.optJSONObject("user") ?: jsonObj
                val tokenStr = jsonObj.optString("token").takeIf { it.isNotEmpty() }
                    ?: userObj.optString("token").takeIf { it.isNotEmpty() }

                val rawAvatar = userObj.optString("avatar").ifEmpty {
                    userObj.optString("picture").ifEmpty {
                        userObj.optString("avatar_url")
                    }
                }.takeIf { it.isNotEmpty() }
                val parsedAvatar = if (rawAvatar != null && rawAvatar.startsWith("/")) {
                    "${ConnectoNetworkConfig.activeBaseUrl}$rawAvatar"
                } else {
                    rawAvatar
                }

                val req2Fa = jsonObj.optBoolean("requires_2fa", false) || userObj.optBoolean("requires_2fa", false)
                val twoFaEnabled = jsonObj.optBoolean("two_factor_enabled", false) || userObj.optBoolean("two_factor_enabled", false)
                val twoFaMethod = userObj.optString("two_factor_method", "sms")
                val maskedDest = userObj.optString("masked_destination").takeIf { it.isNotEmpty() }
                    ?: jsonObj.optString("masked_destination").takeIf { it.isNotEmpty() }
                val phone = userObj.optString("phone_number").takeIf { it.isNotEmpty() }
                val devOtpStr = jsonObj.optString("dev_otp").takeIf { it.isNotEmpty() }
                    ?: userObj.optString("dev_otp").takeIf { it.isNotEmpty() }
                val authResult = AuthResult(
                    id = userObj.optString("id").ifEmpty { userObj.optString("uid", UUID.randomUUID().toString()) },
                    username = userObj.optString("username", cleanIdentifier),
                    displayName = userObj.optString("nickname").ifEmpty { userObj.optString("display_name", cleanIdentifier) },
                    email = userObj.optString("email", "$cleanIdentifier@connecto.fun"),
                    token = tokenStr,
                    avatarUrl = parsedAvatar,
                    phoneNumber = phone,
                    twoFactorEnabled = twoFaEnabled,
                    twoFactorMethod = twoFaMethod,
                    requires2Fa = req2Fa,
                    maskedDestination = maskedDest,
                    devOtp = devOtpStr
                )

                if (!req2Fa) {
                    sessionToken = authResult.token
                    currentUserId = authResult.id
                    currentUsername = authResult.username
                    currentUserEmail = authResult.email
                    currentUserDisplayName = authResult.displayName
                    updateAvatarState(parsedAvatar)
                }
                Result.success(authResult)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyLogin2Fa(
        loginIdentifier: String,
        otpCode: String
    ): Result<AuthResult> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("login", loginIdentifier.trim())
                put("username", loginIdentifier.trim())
                put("otp_code", otpCode.trim())
                put("otp", otpCode.trim())
            }
            val response = executeRequest(
                endpoint = "/api/v1/auth/login/2fa-verify",
                method = "POST",
                body = body.toString(),
                token = null
            )
            if (response.isSuccess) {
                val jsonObj = JSONObject(response.getOrThrow())
                val userObj = jsonObj.optJSONObject("user") ?: jsonObj
                val tokenStr = jsonObj.optString("token").takeIf { it.isNotEmpty() }
                    ?: userObj.optString("token").takeIf { it.isNotEmpty() }

                val rawAvatar = userObj.optString("avatar_url").takeIf { it.isNotEmpty() }
                val parsedAvatar = if (rawAvatar != null && rawAvatar.startsWith("/")) {
                    "${ConnectoNetworkConfig.activeBaseUrl}$rawAvatar"
                } else {
                    rawAvatar
                }

                val authResult = AuthResult(
                    id = userObj.optString("id"),
                    username = userObj.optString("username"),
                    displayName = userObj.optString("display_name"),
                    email = userObj.optString("email"),
                    token = tokenStr,
                    avatarUrl = parsedAvatar,
                    phoneNumber = userObj.optString("phone_number").takeIf { it.isNotEmpty() },
                    twoFactorEnabled = userObj.optBoolean("two_factor_enabled", true),
                    twoFactorMethod = userObj.optString("two_factor_method", "sms"),
                    requires2Fa = false
                )
                sessionToken = authResult.token
                currentUserId = authResult.id
                currentUsername = authResult.username
                currentUserEmail = authResult.email
                currentUserDisplayName = authResult.displayName
                updateAvatarState(parsedAvatar)
                Result.success(authResult)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("2FA Verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestForgotPasswordOtp(
        identifier: String,
        method: String // "sms" or "email"
    ): Result<OtpRequestResult> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("identifier", identifier.trim())
                // production server determines method automatically
            }
            val response = executeRequest(
                endpoint = "/api/auth/forgot-password",
                method = "POST",
                body = body.toString(),
                token = null
            )
            if (response.isSuccess) {
                val jsonObj = JSONObject(response.getOrThrow())
                Result.success(
                    OtpRequestResult(
                        status = jsonObj.optString("status", "ok"),
                        message = jsonObj.optString("message", "Verification code sent"),
                        method = jsonObj.optString("method", method),
                        maskedDestination = jsonObj.optString("masked_destination").takeIf { it.isNotEmpty() },
                        identifier = jsonObj.optString("identifier", identifier),
                        devOtp = null
                    )
                )
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to request OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyForgotPasswordOtp(
        identifier: String,
        otpCode: String
    ): Result<OtpVerifyResult> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("username", identifier.trim())
                put("otp", otpCode.trim())
            }
            val response = executeRequest(
                endpoint = "/api/verify-otp",
                method = "POST",
                body = body.toString(),
                token = null
            )
            if (response.isSuccess) {
                val jsonObj = JSONObject(response.getOrThrow())
                Result.success(
                    OtpVerifyResult(
                        status = jsonObj.optString("status", "ok"),
                        verified = jsonObj.optBoolean("verified", true),
                        identifier = jsonObj.optString("identifier", identifier),
                        message = jsonObj.optString("message", "Verified successfully")
                    )
                )
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Invalid verification code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPasswordWithOtp(
        identifier: String,
        otpCode: String,
        newPassword: String
    ): Result<AuthResult> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("username", identifier.trim())
                put("otp", otpCode.trim())
                put("new_password", newPassword)
            }
            val response = executeRequest(
                endpoint = "/api/auth/reset-password-with-otp",
                method = "POST",
                body = body.toString(),
                token = null
            )
            if (response.isSuccess) {
                val jsonObj = JSONObject(response.getOrThrow())
                val userObj = jsonObj.optJSONObject("user") ?: jsonObj
                val tokenStr = jsonObj.optString("token").takeIf { it.isNotEmpty() }
                    ?: userObj.optString("token").takeIf { it.isNotEmpty() }

                val rawAvatar = userObj.optString("avatar_url").takeIf { it.isNotEmpty() }
                val parsedAvatar = if (rawAvatar != null && rawAvatar.startsWith("/")) {
                    "${ConnectoNetworkConfig.activeBaseUrl}$rawAvatar"
                } else {
                    rawAvatar
                }

                val authResult = AuthResult(
                    id = userObj.optString("id"),
                    username = userObj.optString("username"),
                    displayName = userObj.optString("display_name"),
                    email = userObj.optString("email"),
                    token = tokenStr,
                    avatarUrl = parsedAvatar,
                    phoneNumber = userObj.optString("phone_number").takeIf { it.isNotEmpty() },
                    twoFactorEnabled = userObj.optBoolean("two_factor_enabled", false),
                    twoFactorMethod = userObj.optString("two_factor_method", "sms"),
                    requires2Fa = false
                )
                sessionToken = authResult.token
                currentUserId = authResult.id
                currentUsername = authResult.username
                currentUserEmail = authResult.email
                currentUserDisplayName = authResult.displayName
                updateAvatarState(parsedAvatar)
                Result.success(authResult)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to reset password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun request2FaSetupOtp(
        method: String,
        phoneNumber: String? = null,
        token: String? = null
    ): Result<OtpRequestResult> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("method", method.lowercase())
                if (!phoneNumber.isNullOrBlank()) {
                    put("phone_number", phoneNumber.trim())
                }
            }
            val activeToken = token ?: getPersistedToken()
            val response = executeRequest(
                endpoint = "/api/v1/auth/2fa/request-otp",
                method = "POST",
                body = body.toString(),
                token = activeToken
            )
            if (response.isSuccess) {
                val jsonObj = JSONObject(response.getOrThrow())
                Result.success(
                    OtpRequestResult(
                        status = jsonObj.optString("status", "ok"),
                        message = jsonObj.optString("message", "2FA setup code sent"),
                        method = jsonObj.optString("method", method),
                        maskedDestination = jsonObj.optString("masked_destination").takeIf { it.isNotEmpty() },
                        identifier = jsonObj.optString("identifier").takeIf { it.isNotEmpty() } ?: phoneNumber,
                        devOtp = null
                    )
                )
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to request 2FA OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verify2FaSetupOtp(
        otpCode: String,
        method: String,
        phoneNumber: String? = null,
        token: String? = null
    ): Result<TwoFactorToggleResult> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("otp_code", otpCode.trim())
                put("method", method.lowercase())
                if (!phoneNumber.isNullOrBlank()) {
                    put("phone_number", phoneNumber.trim())
                }
            }
            val activeToken = token ?: getPersistedToken()
            val response = executeRequest(
                endpoint = "/api/v1/auth/2fa/verify",
                method = "POST",
                body = body.toString(),
                token = activeToken
            )
            if (response.isSuccess) {
                val jsonObj = JSONObject(response.getOrThrow())
                Result.success(
                    TwoFactorToggleResult(
                        status = jsonObj.optString("status", "ok"),
                        twoFactorEnabled = jsonObj.optBoolean("two_factor_enabled", true),
                        twoFactorMethod = jsonObj.optString("two_factor_method", method),
                        phoneNumber = jsonObj.optString("phone_number").takeIf { it.isNotEmpty() } ?: phoneNumber,
                        message = jsonObj.optString("message", "2FA enabled successfully")
                    )
                )
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("2FA verification failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggle2Fa(
        enabled: Boolean,
        method: String? = null,
        phoneNumber: String? = null,
        token: String? = null
    ): Result<TwoFactorToggleResult> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("enabled", enabled)
                if (!method.isNullOrBlank()) {
                    put("method", method.lowercase())
                }
                if (!phoneNumber.isNullOrBlank()) {
                    put("phone_number", phoneNumber.trim())
                }
            }
            val activeToken = token ?: getPersistedToken()
            val response = executeRequest(
                endpoint = "/api/v1/auth/2fa/toggle",
                method = "POST",
                body = body.toString(),
                token = activeToken
            )
            if (response.isSuccess) {
                val jsonObj = JSONObject(response.getOrThrow())
                Result.success(
                    TwoFactorToggleResult(
                        status = jsonObj.optString("status", "ok"),
                        twoFactorEnabled = jsonObj.optBoolean("two_factor_enabled", enabled),
                        twoFactorMethod = jsonObj.optString("two_factor_method", method ?: "sms"),
                        phoneNumber = jsonObj.optString("phone_number").takeIf { it.isNotEmpty() } ?: phoneNumber,
                        message = jsonObj.optString("message", if (enabled) "Two-Factor Authentication enabled." else "Two-Factor Authentication disabled.")
                    )
                )
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to update 2FA state"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Volatile
    var channelCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    suspend fun resolveChannelId(channelName: String, token: String? = null): String? {
        val clean = channelName.trim().lowercase().removePrefix("#")
        val cached = channelCache[clean]
        if (cached != null) return cached

        val fetchRes = getChannels(token)
        if (fetchRes.isSuccess) {
            return channelCache[clean] ?: clean
        }
        return clean
    }

    suspend fun getChannels(token: String? = null): Result<List<ChannelDto>> = withContext(Dispatchers.IO) {
        try {
            var response = executeRequest(
                endpoint = "/api/channels",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )

            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/v1/chat/channels",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
            }

            if (response.isSuccess) {
                val respText = response.getOrThrow()
                val list = mutableListOf<ChannelDto>()

                if (respText.trim().startsWith("[")) {
                    val jsonArr = JSONArray(respText)
                    for (i in 0 until jsonArr.length()) {
                        val item = jsonArr.get(i)
                        if (item is JSONObject) {
                            val chId = item.optString("id").ifEmpty { item.optString("channel_id") }
                            val chName = item.optString("name").ifEmpty { chId }
                            val ch = ChannelDto(
                                id = chId,
                                name = chName,
                                type = item.optString("type", "text")
                            )
                            list.add(ch)
                            channelCache[ch.name.lowercase()] = ch.id
                        } else if (item is String) {
                            list.add(ChannelDto(id = item, name = item, type = "text"))
                            channelCache[item.lowercase()] = item
                        }
                    }
                } else if (respText.trim().startsWith("{")) {
                    val obj = JSONObject(respText)
                    val channelsArr = obj.optJSONArray("channels") ?: JSONArray()
                    for (i in 0 until channelsArr.length()) {
                        val chObj = channelsArr.getJSONObject(i)
                        val ch = ChannelDto(
                            id = chObj.optString("id"),
                            name = chObj.optString("name"),
                            type = chObj.optString("type", "text")
                        )
                        list.add(ch)
                        channelCache[ch.name.lowercase()] = ch.id
                    }
                }

                if (list.isEmpty()) {
                    list.add(ChannelDto("general", "general", "text"))
                    list.add(ChannelDto("announcements", "announcements", "text"))
                    list.add(ChannelDto("tournaments", "tournaments", "text"))
                }

                Result.success(list)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch channels"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(
        token: String? = null,
        channelId: String,
        limit: Int = 50,
        before: String? = null,
        friendUsername: String? = null
    ): Result<List<MessageDto>> = withContext(Dispatchers.IO) {
        try {
            val cleanChannel = channelId.trim().lowercase().removePrefix("#")
            val myUser = (currentUsername ?: "").trim().lowercase().removePrefix("@")
            val params = StringBuilder("limit=$limit")
            if (myUser.isNotEmpty()) {
                params.append("&username=${java.net.URLEncoder.encode(myUser, "UTF-8")}")
            }
            if (!before.isNullOrBlank()) {
                val encBefore = java.net.URLEncoder.encode(before, "UTF-8")
                params.append("&before=$encBefore")
            }

            // Prioritize canonical FastAPI v1 chat endpoint (used by Web & Mobile)
            var response = executeRequest(
                endpoint = "/api/v1/chat/channels/$channelId/messages?$params",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )

            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/channels/$cleanChannel/messages?$params",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
            }

            // Direct fallback for 1:1 friend chat to /api/dms/{friendUsername}/messages
            if (!response.isSuccess && !friendUsername.isNullOrBlank()) {
                val encFriend = java.net.URLEncoder.encode(friendUsername.trim().lowercase().removePrefix("@"), "UTF-8")
                response = executeRequest(
                    endpoint = "/api/dms/$encFriend/messages?$params",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
            }

            if (!response.isSuccess) {
                val err = response.exceptionOrNull()?.message ?: ""
                if (err.contains("404") || err.contains("not found", ignoreCase = true) || err.contains("code 404")) {
                    return@withContext Result.success(emptyList<MessageDto>())
                }
                return@withContext Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch messages"))
            }

            if (response.isSuccess) {
                val respText = response.getOrThrow()
                val list = mutableListOf<MessageDto>()

                val jsonArr = if (respText.trim().startsWith("[")) {
                    JSONArray(respText)
                } else {
                    val obj = JSONObject(respText)
                    obj.optJSONArray("messages") ?: JSONArray()
                }

                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    val authorId = when {
                        obj.has("user") && obj.optString("user").isNotBlank() -> obj.optString("user")
                        obj.has("sender_username") && obj.optString("sender_username").isNotBlank() -> obj.optString("sender_username")
                        obj.has("sender_id") && obj.optString("sender_id").isNotBlank() -> obj.optString("sender_id")
                        obj.has("author_id") && obj.optString("author_id").isNotBlank() -> obj.optString("author_id")
                        else -> "gamer"
                    }

                    val authorName = when {
                        obj.has("nickname") && obj.optString("nickname").isNotBlank() -> obj.optString("nickname")
                        obj.has("sender_display_name") && obj.optString("sender_display_name").isNotBlank() -> obj.optString("sender_display_name")
                        obj.has("user") && obj.optString("user").isNotBlank() -> obj.optString("user")
                        obj.has("sender_username") && obj.optString("sender_username").isNotBlank() -> obj.optString("sender_username")
                        obj.has("author_name") && obj.optString("author_name").isNotBlank() -> obj.optString("author_name")
                        else -> authorId
                    }

                    val timeStr = when {
                        obj.has("timestamp") && obj.optString("timestamp").isNotBlank() -> obj.optString("timestamp")
                        obj.has("created_at") && obj.optString("created_at").isNotBlank() -> obj.optString("created_at")
                        else -> "Just now"
                    }

                    val authorAvatar = when {
                        obj.has("avatar_url") && obj.optString("avatar_url").isNotBlank() -> obj.optString("avatar_url")
                        obj.has("sender_avatar_url") && obj.optString("sender_avatar_url").isNotBlank() -> obj.optString("sender_avatar_url")
                        obj.has("avatar") && obj.optString("avatar").isNotBlank() && obj.optString("avatar").length > 3 -> obj.optString("avatar")
                        else -> null
                    }

                    val resolvedAvatar = if (authorAvatar.isNullOrBlank()) null
                        else if (authorAvatar.startsWith("http://") || authorAvatar.startsWith("https://") || authorAvatar.startsWith("data:")) authorAvatar
                        else "${ConnectoNetworkConfig.activeBaseUrl.trimEnd('/')}/${authorAvatar.trimStart('/')}"

                    var msgType = obj.optString("type").ifEmpty { "text" }
                    var pollId = if (obj.has("poll_id") && !obj.isNull("poll_id")) obj.optString("poll_id") else null
                    var pollDto = if (obj.has("poll") && !obj.isNull("poll")) parsePollDto(obj.optJSONObject("poll")) else null

                    // If poll not populated at root, inspect nested attachments
                    if (pollDto == null || msgType == "text") {
                        val attObj: JSONObject? = when {
                            obj.has("attachments") && !obj.isNull("attachments") -> {
                                val raw = obj.get("attachments")
                                when (raw) {
                                    is JSONObject -> raw
                                    is String -> try { if (raw.trim().startsWith("{")) JSONObject(raw) else null } catch (_: Exception) { null }
                                    else -> null
                                }
                            }
                            else -> null
                        }
                        if (attObj != null) {
                            if (attObj.optString("type") == "poll") {
                                msgType = "poll"
                            }
                            if (pollId.isNullOrBlank() && attObj.has("poll_id")) {
                                pollId = attObj.optString("poll_id")
                            }
                            if (pollDto == null && attObj.has("poll")) {
                                pollDto = parsePollDto(attObj.optJSONObject("poll"))
                            }
                        }
                    }

                    val timerSec = if (obj.has("timer_seconds") && !obj.isNull("timer_seconds")) obj.optInt("timer_seconds") else null
                    val expiresAt = if (obj.has("expires_at") && !obj.isNull("expires_at")) obj.optString("expires_at") else null

                    list.add(
                        MessageDto(
                            id = obj.optString("id").ifEmpty { UUID.randomUUID().toString() },
                            channelId = cleanChannel,
                            authorId = authorId,
                            authorName = authorName,
                            content = obj.optString("content").ifEmpty { obj.optString("text") },
                            createdAt = timeStr,
                            authorAvatar = resolvedAvatar,
                            type = msgType,
                            pollId = pollId,
                            poll = pollDto,
                            timerSeconds = timerSec,
                            expiresAt = expiresAt
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parsePollDto(json: JSONObject?): PollDto? {
        if (json == null) return null
        val id = json.optString("id")
        val question = json.optString("question")
        val optionsArr = json.optJSONArray("options")
        val options = mutableListOf<PollOptionDto>()
        if (optionsArr != null) {
            for (i in 0 until optionsArr.length()) {
                val optObj = optionsArr.optJSONObject(i)
                if (optObj != null) {
                    val text = optObj.optString("text")
                    val votesArr = optObj.optJSONArray("votes")
                    val votes = mutableListOf<String>()
                    if (votesArr != null) {
                        for (v in 0 until votesArr.length()) {
                            votes.add(votesArr.optString(v))
                        }
                    }
                    options.add(PollOptionDto(text = text, votes = votes))
                }
            }
        }
        return PollDto(
            id = id,
            question = question,
            options = options,
            author = json.optString("author"),
            authorName = json.optString("author_name"),
            channelId = json.optString("channel_id"),
            multiple = json.optBoolean("multiple", false),
            totalVotes = json.optInt("total_votes", 0),
            closed = json.optBoolean("closed", false),
            createdAt = json.optString("created_at"),
            timestamp = json.optString("timestamp")
        )
    }

    suspend fun sendMessage(
        token: String? = null,
        channelId: String,
        content: String,
        timerSeconds: Int? = null
    ): Result<MessageDto> = withContext(Dispatchers.IO) {
        try {
            val cleanChannel = channelId.trim().lowercase().removePrefix("#")
            val myUsername = currentUsername ?: "guest"
            val myDisplayName = currentUserDisplayName ?: myUsername

            // 1. Prioritize canonical FastAPI v1 chat endpoint
            val v1Body = JSONObject().apply {
                put("content", content.trim())
                put("nonce", UUID.randomUUID().toString())
                if (timerSeconds != null && timerSeconds > 0) {
                    put("timer_seconds", timerSeconds)
                }
            }
            var response = executeRequest(
                endpoint = "/api/v1/chat/channels/$channelId/messages",
                method = "POST",
                body = v1Body.toString(),
                token = token ?: sessionToken
            )

            // 2. Fallbacks for server endpoints
            if (!response.isSuccess) {
                val prodBody = JSONObject().apply {
                    put("channel_id", cleanChannel)
                    put("content", content.trim())
                    put("username", myUsername)
                    put("author", myUsername)
                    put("avatar", "🎮")
                    if (timerSeconds != null && timerSeconds > 0) {
                        put("timer_seconds", timerSeconds)
                    }
                }
                response = executeRequest(
                    endpoint = "/api/channels/$cleanChannel/messages",
                    method = "POST",
                    body = prodBody.toString(),
                    token = token ?: sessionToken
                )
                if (!response.isSuccess) {
                    response = executeRequest(
                        endpoint = "/api/messages",
                        method = "POST",
                        body = prodBody.toString(),
                        token = token ?: sessionToken
                    )
                }
            }

            if (response.isSuccess) {
                val obj = JSONObject(response.getOrThrow())
                val timeStr = obj.optString("timestamp").ifEmpty { obj.optString("created_at", "Just now") }
                val respAvatar = obj.optString("avatar_url").ifEmpty { obj.optString("sender_avatar_url").ifEmpty { currentUserAvatarUrl } }
                val resolvedAvatar = if (respAvatar.isNullOrBlank()) null
                    else if (respAvatar.startsWith("http://") || respAvatar.startsWith("https://") || respAvatar.startsWith("data:")) respAvatar
                    else "${ConnectoNetworkConfig.activeBaseUrl.trimEnd('/')}/${respAvatar.trimStart('/')}"

                val msgType = obj.optString("type").ifEmpty { "text" }
                val pollId = if (obj.has("poll_id") && !obj.isNull("poll_id")) obj.optString("poll_id") else null
                val pollDto = if (obj.has("poll") && !obj.isNull("poll")) parsePollDto(obj.optJSONObject("poll")) else null
                val timerSec = if (obj.has("timer_seconds") && !obj.isNull("timer_seconds")) obj.optInt("timer_seconds") else timerSeconds
                val expAt = if (obj.has("expires_at") && !obj.isNull("expires_at")) obj.optString("expires_at") else null

                Result.success(
                    MessageDto(
                        id = obj.optString("id").ifEmpty { UUID.randomUUID().toString() },
                        channelId = cleanChannel,
                        authorId = myUsername,
                        authorName = myDisplayName,
                        content = content.trim(),
                        createdAt = timeStr,
                        authorAvatar = resolvedAvatar,
                        type = msgType,
                        pollId = pollId,
                        poll = pollDto,
                        timerSeconds = timerSec,
                        expiresAt = expAt
                    )
                )
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPoll(
        channelId: String,
        question: String,
        options: List<String>,
        multiple: Boolean = false,
        token: String? = null
    ): Result<PollDto> = withContext(Dispatchers.IO) {
        try {
            val cleanChannel = channelId.trim().lowercase().removePrefix("#")
            val myUsername = currentUsername ?: "guest"
            val optionsJson = org.json.JSONArray().apply {
                options.forEach { opt ->
                    if (opt.isNotBlank()) put(opt.trim())
                }
            }
            val body = JSONObject().apply {
                put("question", question.trim())
                put("options", optionsJson)
                put("multiple", multiple)
                put("author", myUsername)
            }
            val res = executeRequest(
                endpoint = "/api/channels/$cleanChannel/polls",
                method = "POST",
                body = body.toString(),
                token = token ?: sessionToken
            )
            if (res.isSuccess) {
                val obj = JSONObject(res.getOrThrow())
                val pollObj = obj.optJSONObject("poll")
                val poll = parsePollDto(pollObj)
                if (poll != null) {
                    Result.success(poll)
                } else {
                    Result.failure(Exception("Failed to parse created poll"))
                }
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("Failed to create poll"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun votePoll(
        pollId: String,
        optionIndex: Int,
        voter: String? = null,
        token: String? = null
    ): Result<PollDto> = withContext(Dispatchers.IO) {
        try {
            val myUsername = voter ?: currentUsername ?: "guest"
            val body = JSONObject().apply {
                put("option_index", optionIndex)
                put("voter", myUsername)
            }
            val res = executeRequest(
                endpoint = "/api/polls/$pollId/vote",
                method = "POST",
                body = body.toString(),
                token = token ?: sessionToken
            )
            if (res.isSuccess) {
                val obj = JSONObject(res.getOrThrow())
                val pollObj = obj.optJSONObject("poll")
                val poll = parsePollDto(pollObj)
                if (poll != null) {
                    Result.success(poll)
                } else {
                    Result.failure(Exception("Failed to parse voted poll"))
                }
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("Failed to cast vote"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closePoll(
        pollId: String,
        token: String? = null
    ): Result<PollDto> = withContext(Dispatchers.IO) {
        try {
            val myUsername = currentUsername ?: "guest"
            val body = JSONObject().apply {
                put("closed_by", myUsername)
            }
            val res = executeRequest(
                endpoint = "/api/polls/$pollId/close",
                method = "POST",
                body = body.toString(),
                token = token ?: sessionToken
            )
            if (res.isSuccess) {
                val obj = JSONObject(res.getOrThrow())
                val pollObj = obj.optJSONObject("poll")
                val poll = parsePollDto(pollObj)
                if (poll != null) {
                    Result.success(poll)
                } else {
                    Result.failure(Exception("Failed to parse closed poll"))
                }
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("Failed to close poll"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(token: String? = null): Result<List<UserDto>> = withContext(Dispatchers.IO) {
        try {
            var response = executeRequest(
                endpoint = "/api/members",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )

            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/v1/users",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
            }

            if (response.isSuccess) {
                val respText = response.getOrThrow()
                val list = mutableListOf<UserDto>()
                val jsonArr = if (respText.trim().startsWith("[")) {
                    JSONArray(respText)
                } else {
                    JSONObject(respText).optJSONArray("members") ?: JSONArray()
                }

                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    list.add(
                        UserDto(
                            id = obj.optString("id").ifEmpty { obj.optString("uid", UUID.randomUUID().toString()) },
                            username = obj.optString("username"),
                            displayName = obj.optString("nickname").ifEmpty { obj.optString("display_name", obj.optString("username")) },
                            email = obj.optString("email"),
                            avatarUrl = obj.optString("avatar").ifEmpty { obj.optString("avatar_url").takeIf { it.isNotEmpty() } }
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String, token: String? = null, username: String? = null): Result<List<UserSearchResultDto>> = withContext(Dispatchers.IO) {
        try {
            val cleanQ = query.trim().removePrefix("@")
            if (cleanQ.isBlank()) {
                return@withContext Result.success(emptyList())
            }

            val encodedQuery = java.net.URLEncoder.encode(cleanQ, "UTF-8")
            val myUsername = (username ?: currentUsername ?: "").trim().lowercase().removePrefix("@")
            val resultMap = linkedMapOf<String, UserSearchResultDto>()

            // 1. Fetch friend relationship sets so search results show accurate friend status
            val friendsSet = mutableSetOf<String>()
            val outgoingSet = mutableSetOf<String>()
            val incomingSet = mutableSetOf<String>()

            try {
                val friendsResp = executeRequest(
                    endpoint = "/api/friends?username=${myUsername.ifEmpty { "guest" }}",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
                if (friendsResp.isSuccess) {
                    val fText = friendsResp.getOrThrow().trim()
                    if (fText.startsWith("{")) {
                        val fObj = JSONObject(fText)
                        val fArr = fObj.optJSONArray("friends") ?: JSONArray()
                        for (i in 0 until fArr.length()) {
                            val item = fArr.optJSONObject(i)
                            val u = (item?.optString("username")?.ifEmpty { item.optString("friend_username") } ?: fArr.optString(i)).trim().removePrefix("@")
                            val d = item?.optString("nickname")?.ifEmpty { item.optString("display_name") }?.ifEmpty { u } ?: u
                            val av = item?.optString("avatar")?.ifEmpty { item.optString("avatar_url") }
                            val id = item?.optString("id")?.ifEmpty { item.optString("friend_id") }?.ifEmpty { u } ?: u

                            if (u.isNotBlank()) {
                                friendsSet.add(u.lowercase())
                                if (d.isNotBlank()) friendsSet.add(d.lowercase())

                                // Immediately match friend against search query so friend is guaranteed to appear
                                val matches = u.contains(cleanQ, ignoreCase = true) ||
                                              d.contains(cleanQ, ignoreCase = true) ||
                                              cleanQ.contains(u, ignoreCase = true)
                                if (matches && !resultMap.containsKey(u.lowercase())) {
                                    resultMap[u.lowercase()] = UserSearchResultDto(
                                        id = id,
                                        username = u,
                                        displayName = d,
                                        avatarUrl = av,
                                        relationStatus = "friends"
                                    )
                                }
                            }
                        }
                        val outArr = fObj.optJSONArray("outgoing") ?: JSONArray()
                        for (i in 0 until outArr.length()) {
                            val item = outArr.optJSONObject(i)
                            val r = (item?.optString("recipient")?.ifEmpty { item.optString("username") } ?: outArr.optString(i)).trim().removePrefix("@")
                            val d = item?.optString("nickname")?.ifEmpty { item.optString("display_name") }?.ifEmpty { r } ?: r
                            val av = item?.optString("avatar")?.ifEmpty { item.optString("avatar_url") }
                            val id = item?.optString("id")?.ifEmpty { r } ?: r
                            if (r.isNotBlank()) {
                                outgoingSet.add(r.lowercase())
                                if (d.isNotBlank()) outgoingSet.add(d.lowercase())
                                val matches = r.contains(cleanQ, ignoreCase = true) || d.contains(cleanQ, ignoreCase = true)
                                if (matches && !resultMap.containsKey(r.lowercase())) {
                                    resultMap[r.lowercase()] = UserSearchResultDto(
                                        id = id,
                                        username = r,
                                        displayName = d,
                                        avatarUrl = av,
                                        relationStatus = "pending_sent"
                                    )
                                }
                            }
                        }
                        val inArr = fObj.optJSONArray("incoming") ?: JSONArray()
                        for (i in 0 until inArr.length()) {
                            val item = inArr.optJSONObject(i)
                            val s = (item?.optString("sender")?.ifEmpty { item.optString("sender_username") }?.ifEmpty { item.optString("username") } ?: inArr.optString(i)).trim().removePrefix("@")
                            val d = item?.optString("nickname")?.ifEmpty { item.optString("display_name") }?.ifEmpty { s } ?: s
                            val av = item?.optString("avatar")?.ifEmpty { item.optString("avatar_url") }
                            val id = item?.optString("id")?.ifEmpty { s } ?: s
                            if (s.isNotBlank()) {
                                incomingSet.add(s.lowercase())
                                if (d.isNotBlank()) incomingSet.add(d.lowercase())
                                val matches = s.contains(cleanQ, ignoreCase = true) || d.contains(cleanQ, ignoreCase = true)
                                if (matches && !resultMap.containsKey(s.lowercase())) {
                                    resultMap[s.lowercase()] = UserSearchResultDto(
                                        id = id,
                                        username = s,
                                        displayName = d,
                                        avatarUrl = av,
                                        relationStatus = "pending_received"
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore relation prefetch failure
            }

            fun resolveRelation(uname: String, dName: String = "", uid: String = ""): String {
                val lower = uname.trim().lowercase().removePrefix("@")
                val lowerD = dName.trim().lowercase().removePrefix("@")
                val lowerUid = uid.trim().lowercase()

                fun checkMatches(set: Set<String>): Boolean {
                    if (set.contains(lower) || (lowerD.isNotBlank() && set.contains(lowerD)) || (lowerUid.isNotBlank() && set.contains(lowerUid))) {
                        return true
                    }
                    val stripped = lower.replace("_", "")
                    return set.any { s ->
                        s == lower || s.replace("_", "") == stripped || (lowerD.isNotBlank() && s == lowerD)
                    }
                }

                if (checkMatches(friendsSet)) return "friends"
                if (checkMatches(outgoingSet)) return "pending_sent"
                if (checkMatches(incomingSet)) return "pending_received"

                return "none"
            }

            // 2. Query /api/members (All registered members on connecto.fun server)
            try {
                val membersResp = executeRequest(
                    endpoint = "/api/members",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
                if (membersResp.isSuccess) {
                    val mText = membersResp.getOrThrow().trim()
                    if (mText.startsWith("{")) {
                        val mObj = JSONObject(mText)
                        val membersArr = mObj.optJSONArray("members") ?: JSONArray()
                        for (i in 0 until membersArr.length()) {
                            val member = membersArr.getJSONObject(i)
                            val uName = member.optString("username").trim().removePrefix("@")
                            val dName = member.optString("nickname").ifEmpty { uName }
                            val bio = member.optString("bio")
                            val avatar = member.optString("avatar").ifEmpty { member.optString("avatar_url").takeIf { it.isNotEmpty() } }
                            val id = member.optString("id").ifEmpty { uName }

                            if (uName.isBlank()) continue
                            if (myUsername.isNotBlank() && uName.equals(myUsername, ignoreCase = true)) continue

                            // Match query against username, nickname, or bio
                            val matches = uName.contains(cleanQ, ignoreCase = true) ||
                                          dName.contains(cleanQ, ignoreCase = true) ||
                                          bio.contains(cleanQ, ignoreCase = true) ||
                                          cleanQ.contains(uName, ignoreCase = true)

                            if (matches && !resultMap.containsKey(uName.lowercase())) {
                                resultMap[uName.lowercase()] = UserSearchResultDto(
                                    id = id,
                                    username = uName,
                                    displayName = dName,
                                    avatarUrl = avatar,
                                    relationStatus = resolveRelation(uName, dName, id)
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Continue to next strategy
            }

            // 3. Check exact username in server database via /api/auth/check-username
            try {
                val checkResp = executeRequest(
                    endpoint = "/api/auth/check-username?username=$encodedQuery",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
                if (checkResp.isSuccess) {
                    val cText = checkResp.getOrThrow().trim()
                    if (cText.startsWith("{")) {
                        val cObj = JSONObject(cText)
                        val isAvailable = cObj.optBoolean("available", true)
                        // If available == false, the user definitely EXISTS in the connecto database!
                        if (!isAvailable) {
                            val uName = cObj.optString("username", cleanQ).trim().removePrefix("@")
                            if (uName.isNotBlank() && (myUsername.isBlank() || !uName.equals(myUsername, ignoreCase = true)) && !resultMap.containsKey(uName.lowercase())) {
                                resultMap[uName.lowercase()] = UserSearchResultDto(
                                    id = UUID.randomUUID().toString(),
                                    username = uName,
                                    displayName = uName,
                                    avatarUrl = null,
                                    relationStatus = resolveRelation(uName)
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Continue
            }

            // 4. Fallback search endpoint
            for (searchEndpoint in listOf("/api/v1/users/search?q=$encodedQuery", "/api/users/search?q=$encodedQuery")) {
                try {
                    val searchResp = executeRequest(
                        endpoint = searchEndpoint,
                        method = "GET",
                        body = null,
                        token = token ?: sessionToken
                    )
                    if (searchResp.isSuccess) {
                        val sText = searchResp.getOrThrow().trim()
                        if (sText.startsWith("[")) {
                            val v1Arr = JSONArray(sText)
                            for (i in 0 until v1Arr.length()) {
                                val obj = v1Arr.getJSONObject(i)
                                val uName = obj.optString("username").trim().removePrefix("@")
                                if (uName.isBlank() || (myUsername.isNotBlank() && uName.equals(myUsername, ignoreCase = true))) continue
                                val dName = obj.optString("display_name").ifEmpty { obj.optString("nickname", uName) }
                                val avatar = obj.optString("avatar_url").ifEmpty { obj.optString("avatar").takeIf { it.isNotEmpty() } }
                                val id = obj.optString("id").ifEmpty { uName }

                                if (!resultMap.containsKey(uName.lowercase())) {
                                    resultMap[uName.lowercase()] = UserSearchResultDto(
                                        id = id,
                                        username = uName,
                                        displayName = dName,
                                        avatarUrl = avatar,
                                        relationStatus = resolveRelation(uName, dName, id)
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Continue
                }
            }

            // Sort results: Existing friends appear first, then incoming requests, then outgoing, then others
            val sortedList = resultMap.values.sortedWith(
                compareBy<UserSearchResultDto> { u ->
                    when (u.relationStatus) {
                        "friends" -> 0
                        "pending_received" -> 1
                        "pending_sent" -> 2
                        else -> 3
                    }
                }.thenBy { it.displayName.lowercase() }
            )

            Result.success(sortedList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(
        targetUsername: String,
        myUsernameOverride: String? = null,
        token: String? = null
    ): Result<FriendshipDto> = withContext(Dispatchers.IO) {
        try {
            val cleanUser = targetUsername.trim().lowercase().removePrefix("@")
            val myUsername = (myUsernameOverride ?: currentUsername ?: "").trim().lowercase().removePrefix("@").ifEmpty { "guest" }

            // 1. Try connecto.fun production friend request
            val prodBody = JSONObject().apply {
                put("sender", myUsername)
                put("recipient", cleanUser)
            }

            var response = executeRequest(
                endpoint = "/api/friends/request",
                method = "POST",
                body = prodBody.toString(),
                token = token ?: sessionToken
            )

            if (!response.isSuccess) {
                // Fallback to v1 schema
                val v1Body = JSONObject().apply {
                    put("friend_username", cleanUser)
                }
                response = executeRequest(
                    endpoint = "/api/v1/chat/friends/request",
                    method = "POST",
                    body = v1Body.toString(),
                    token = token ?: sessionToken
                )
            }

            if (response.isSuccess) {
                Result.success(
                    FriendshipDto(
                        id = UUID.randomUUID().toString(),
                        userId = myUsername,
                        friendId = cleanUser,
                        friendUsername = cleanUser,
                        friendDisplayName = cleanUser.replaceFirstChar { it.uppercase() },
                        friendAvatarUrl = null,
                        status = "pending"
                    )
                )
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to send friend request"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReceivedFriendRequests(token: String? = null, username: String? = null): Result<List<FriendRequestItemDto>> = withContext(Dispatchers.IO) {
        try {
            val myUsername = (username ?: currentUsername ?: "guest").trim().lowercase().removePrefix("@")

            var response = executeRequest(
                endpoint = "/api/friends?username=$myUsername",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )

            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/v1/chat/friends/requests/received",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
            }

            if (response.isSuccess) {
                val respText = response.getOrThrow()
                val list = mutableListOf<FriendRequestItemDto>()

                if (respText.trim().startsWith("{")) {
                    val obj = JSONObject(respText)
                    val incoming = obj.optJSONArray("incoming") ?: JSONArray()
                    for (i in 0 until incoming.length()) {
                        val item = incoming.getJSONObject(i)
                        val sUser = item.optString("sender").ifEmpty {
                            item.optString("sender_username").ifEmpty { item.optString("username") }
                        }
                        if (sUser.isBlank()) continue

                        val dName = item.optString("nickname").ifEmpty {
                            item.optString("display_name").ifEmpty {
                                item.optString("sender_nickname", sUser)
                            }
                        }
                        val reqId = item.optString("id").ifEmpty {
                            item.optString("request_id", sUser)
                        }
                        val avatar = item.optString("avatar").ifEmpty {
                            item.optString("avatar_url").takeIf { it.isNotEmpty() }
                        }
                        val time = item.optString("timestamp").ifEmpty {
                            item.optString("created_at", "Just now")
                        }

                        list.add(
                            FriendRequestItemDto(
                                id = reqId,
                                senderId = sUser,
                                senderUsername = sUser,
                                senderDisplayName = dName,
                                senderAvatarUrl = avatar,
                                createdAt = time
                            )
                        )
                    }
                } else if (respText.trim().startsWith("[")) {
                    val arr = JSONArray(respText)
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        list.add(
                            FriendRequestItemDto(
                                id = item.optString("id"),
                                senderId = item.optString("sender_id"),
                                senderUsername = item.optString("sender_username"),
                                senderDisplayName = item.optString("sender_display_name"),
                                senderAvatarUrl = item.optString("sender_avatar_url").takeIf { it.isNotEmpty() },
                                createdAt = item.optString("created_at")
                            )
                        )
                    }
                }
                Result.success(list)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch friend requests"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(
        senderUsername: String,
        requestId: String? = null,
        myUsernameOverride: String? = null,
        token: String? = null
    ): Result<FriendshipDto> = withContext(Dispatchers.IO) {
        try {
            val myUsername = (myUsernameOverride ?: currentUsername ?: "").trim().lowercase().removePrefix("@").ifEmpty { "guest" }
            val cleanSender = senderUsername.trim().lowercase().removePrefix("@")

            val prodBody = JSONObject().apply {
                put("sender", cleanSender)
                put("recipient", myUsername)
                if (!requestId.isNullOrBlank()) {
                    put("request_id", requestId)
                }
            }

            var response = executeRequest(
                endpoint = "/api/friends/accept",
                method = "POST",
                body = prodBody.toString(),
                token = token ?: sessionToken
            )

            if (!response.isSuccess) {
                val targetId = requestId ?: cleanSender
                response = executeRequest(
                    endpoint = "/api/v1/chat/friends/requests/$targetId/accept",
                    method = "POST",
                    body = "",
                    token = token ?: sessionToken
                )
            }

            if (response.isSuccess) {
                Result.success(
                    FriendshipDto(
                        id = requestId ?: UUID.randomUUID().toString(),
                        userId = myUsername,
                        friendId = cleanSender,
                        friendUsername = cleanSender,
                        friendDisplayName = cleanSender.replaceFirstChar { it.uppercase() },
                        friendAvatarUrl = null,
                        status = "friends"
                    )
                )
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to accept friend request"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineFriendRequest(
        senderUsername: String,
        requestId: String? = null,
        myUsernameOverride: String? = null,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val myUsername = (myUsernameOverride ?: currentUsername ?: "").trim().lowercase().removePrefix("@").ifEmpty { "guest" }
            val cleanSender = senderUsername.trim().lowercase().removePrefix("@")

            val prodBody = JSONObject().apply {
                put("sender", cleanSender)
                put("recipient", myUsername)
                if (!requestId.isNullOrBlank()) {
                    put("request_id", requestId)
                }
            }

            var response = executeRequest(
                endpoint = "/api/friends/decline",
                method = "POST",
                body = prodBody.toString(),
                token = token ?: sessionToken
            )

            if (!response.isSuccess) {
                val targetId = requestId ?: cleanSender
                response = executeRequest(
                    endpoint = "/api/v1/chat/friends/requests/$targetId/decline",
                    method = "POST",
                    body = "",
                    token = token ?: sessionToken
                )
            }

            if (response.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to decline friend request"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFriends(token: String? = null, username: String? = null): Result<List<FriendshipDto>> = withContext(Dispatchers.IO) {
        try {
            val myUsername = (username ?: currentUsername ?: "guest").trim().lowercase().removePrefix("@")

            var response = executeRequest(
                endpoint = "/api/friends?username=$myUsername",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )
            android.util.Log.i("ConnectoFriends", "getFriends(/api/friends) for $myUsername -> success=${response.isSuccess}, body=${response.getOrNull()}")

            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/v1/chat/friends",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
                android.util.Log.i("ConnectoFriends", "getFriends(/api/v1/chat/friends) -> success=${response.isSuccess}, body=${response.getOrNull()}")
            }

            if (response.isSuccess) {
                val respText = response.getOrThrow()
                val list = mutableListOf<FriendshipDto>()

                if (respText.trim().startsWith("{")) {
                    val obj = JSONObject(respText)
                    val friendsArr = obj.optJSONArray("friends") ?: JSONArray()
                    for (i in 0 until friendsArr.length()) {
                        val item = friendsArr.getJSONObject(i)
                        val uName = item.optString("username").ifEmpty { item.optString("friend_username") }
                        val dName = item.optString("nickname").ifEmpty { item.optString("display_name", uName) }
                        val isStealth = item.optBoolean("is_stealth", false)
                        val statusStr = item.optString("status", "offline").lowercase()
                        val isOnline = !isStealth && (item.optBoolean("is_online", false) || statusStr == "online")
                        val rawAvatar = item.optString("avatar_url").ifEmpty { item.optString("friend_avatar_url").ifEmpty { item.optString("avatar") } }.takeIf { it.isNotBlank() && it.length > 3 }
                        val resolvedAvatar = if (rawAvatar.isNullOrBlank()) null
                            else if (rawAvatar.startsWith("http://") || rawAvatar.startsWith("https://") || rawAvatar.startsWith("data:")) rawAvatar
                            else "${ConnectoNetworkConfig.activeBaseUrl.trimEnd('/')}/${rawAvatar.trimStart('/')}"
                        val itemBio = item.optString("bio").takeIf { it.isNotBlank() }
                        list.add(
                            FriendshipDto(
                                id = item.optString("id").ifEmpty { uName },
                                userId = myUsername,
                                friendId = uName,
                                friendUsername = uName,
                                friendDisplayName = dName,
                                friendAvatarUrl = resolvedAvatar,
                                status = if (isOnline) "online" else "offline",
                                isOnline = isOnline,
                                isStealth = isStealth,
                                bio = itemBio
                            )
                        )
                    }
                } else if (respText.trim().startsWith("[")) {
                    val arr = JSONArray(respText)
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        val isStealth = item.optBoolean("is_stealth", false)
                        val statusStr = item.optString("status", "offline").lowercase()
                        val isOnline = !isStealth && (item.optBoolean("is_online", false) || statusStr == "online")
                        val rawAvatar = item.optString("avatar_url").ifEmpty { item.optString("friend_avatar_url").ifEmpty { item.optString("avatar") } }.takeIf { it.isNotBlank() && it.length > 3 }
                        val resolvedAvatar = if (rawAvatar.isNullOrBlank()) null
                            else if (rawAvatar.startsWith("http://") || rawAvatar.startsWith("https://") || rawAvatar.startsWith("data:")) rawAvatar
                            else "${ConnectoNetworkConfig.activeBaseUrl.trimEnd('/')}/${rawAvatar.trimStart('/')}"
                        val itemBio = item.optString("bio").takeIf { it.isNotBlank() }
                        list.add(
                            FriendshipDto(
                                id = item.optString("id"),
                                userId = item.optString("user_id"),
                                friendId = item.optString("friend_id"),
                                friendUsername = item.optString("friend_username"),
                                friendDisplayName = item.optString("friend_display_name"),
                                friendAvatarUrl = resolvedAvatar,
                                status = if (isOnline) "online" else "offline",
                                isOnline = isOnline,
                                isStealth = isStealth,
                                bio = itemBio
                            )
                        )
                    }
                }
                Result.success(list)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch friends"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfriendUser(
        targetUsername: String,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val myUsername = (currentUsername ?: "guest").trim().lowercase().removePrefix("@")
            val cleanTarget = targetUsername.trim().lowercase().removePrefix("@")
            val payload = JSONObject().apply {
                put("sender", myUsername)
                put("recipient", cleanTarget)
                put("username", myUsername)
                put("friend_username", cleanTarget)
            }
            val response = executeRequest(
                endpoint = "/api/friends/remove",
                method = "POST",
                body = payload.toString(),
                token = token ?: sessionToken
            )
            if (response.isSuccess) {
                Result.success(true)
            } else {
                val fallback = executeRequest(
                    endpoint = "/api/friends/unfriend",
                    method = "POST",
                    body = payload.toString(),
                    token = token ?: sessionToken
                )
                Result.success(fallback.isSuccess)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startDM(targetUsername: String, token: String? = null): Result<ChannelDto> = withContext(Dispatchers.IO) {
        try {
            val cleanTarget = targetUsername.trim().lowercase().removePrefix("@")
            val myUser = (currentUsername ?: "guest").trim().lowercase().removePrefix("@")
            val encodedTarget = java.net.URLEncoder.encode(cleanTarget, "UTF-8")
            val encodedMy = java.net.URLEncoder.encode(myUser, "UTF-8")
            val bodyJson = JSONObject().apply {
                put("target_username", cleanTarget)
                put("my_username", myUser)
            }

            // 1. Try FastAPI v1 direct DM start endpoint
            var response = executeRequest(
                endpoint = "/api/v1/chat/dm/start?target_username=$encodedTarget&my_username=$encodedMy",
                method = "POST",
                body = bodyJson.toString(),
                token = token ?: sessionToken
            )

            // 2. Fallback to /api/dm/start or /api/chat/dm/start
            if (!response.isSuccess) {
                val bodyJson = JSONObject().apply {
                    put("target_username", cleanTarget)
                    put("my_username", myUser)
                }
                response = executeRequest(
                    endpoint = "/api/dm/start",
                    method = "POST",
                    body = bodyJson.toString(),
                    token = token ?: sessionToken
                )
            }

            if (response.isSuccess) {
                val respText = response.getOrThrow()
                val obj = JSONObject(respText)
                val chId = obj.optString("id").ifEmpty {
                    if (myUser.isNotEmpty() && myUser != cleanTarget && myUser != "guest") {
                        "dm-${minOf(myUser, cleanTarget)}-${maxOf(myUser, cleanTarget)}"
                    } else {
                        cleanTarget
                    }
                }
                val chName = obj.optString("name", cleanTarget)
                Result.success(ChannelDto(id = chId, name = chName, type = "dm"))
            } else {
                val canonicalName = if (myUser.isNotEmpty() && myUser != cleanTarget && myUser != "guest") {
                    "dm-${minOf(myUser, cleanTarget)}-${maxOf(myUser, cleanTarget)}"
                } else {
                    cleanTarget
                }
                Result.success(ChannelDto(id = canonicalName, name = cleanTarget, type = "dm"))
            }
        } catch (e: Exception) {
            val cleanTarget = targetUsername.trim().lowercase().removePrefix("@")
            val myUser = (currentUsername ?: "guest").trim().lowercase().removePrefix("@")
            val canonicalName = if (myUser.isNotEmpty() && myUser != cleanTarget && myUser != "guest") {
                "dm-${minOf(myUser, cleanTarget)}-${maxOf(myUser, cleanTarget)}"
            } else {
                cleanTarget
            }
            Result.success(ChannelDto(id = canonicalName, name = cleanTarget, type = "dm"))
        }
    }

    suspend fun getLeaderboard(token: String? = null): Result<LeaderboardResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = executeRequest(
                endpoint = "/api/leaderboard",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )
            if (response.isSuccess) {
                val obj = JSONObject(response.getOrThrow())
                val arr = obj.optJSONArray("leaderboard") ?: JSONArray()
                val total = obj.optInt("total_shinobi", arr.length())
                val list = mutableListOf<LeaderboardUserDto>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    list.add(
                        LeaderboardUserDto(
                            username = item.optString("username"),
                            nickname = item.optString("nickname").ifEmpty { item.optString("username") },
                            avatar = item.optString("avatar").takeIf { it.isNotEmpty() },
                            rank = item.optString("rank", "Member"),
                            xp = item.optInt("xp", 100),
                            streak = item.optInt("streak", 1),
                            badge = item.optString("badge", "🌱 Member")
                        )
                    )
                }
                Result.success(LeaderboardResponseDto(totalShinobi = total, leaderboard = list))
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch leaderboard"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStats(token: String? = null): Result<PlatformStatsDto> = withContext(Dispatchers.IO) {
        try {
            val response = executeRequest(
                endpoint = "/api/stats",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )
            if (response.isSuccess) {
                val obj = JSONObject(response.getOrThrow())
                Result.success(
                    PlatformStatsDto(
                        activeShinobi = obj.optInt("active_shinobi", 10),
                        clansFormed = obj.optInt("clans_formed", 5),
                        messagesSent = obj.optInt("messages_sent", 42),
                        onlineUsers = obj.optInt("online_users", 1)
                    )
                )
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVoiceRooms(token: String? = null): Result<List<VoiceRoomDto>> = withContext(Dispatchers.IO) {
        try {
            val response = executeRequest(
                endpoint = "/api/voice/rooms",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )
            if (response.isSuccess) {
                val obj = JSONObject(response.getOrThrow())
                val arr = obj.optJSONArray("rooms") ?: JSONArray()
                val list = mutableListOf<VoiceRoomDto>()
                for (i in 0 until arr.length()) {
                    val r = arr.getJSONObject(i)
                    val pArr = r.optJSONArray("participants") ?: JSONArray()
                    val pList = mutableListOf<VoiceParticipantDto>()
                    for (j in 0 until pArr.length()) {
                        val p = pArr.getJSONObject(j)
                        pList.add(
                            VoiceParticipantDto(
                                username = p.optString("username"),
                                nickname = p.optString("nickname").ifEmpty { p.optString("username") },
                                avatar = p.optString("avatar").takeIf { it.isNotEmpty() },
                                rank = p.optString("rank", "Member"),
                                muted = p.optBoolean("muted", false),
                                speaking = p.optBoolean("speaking", false)
                            )
                        )
                    }
                    val code = r.optString("code").ifEmpty { "VOX-${r.optString("id").takeLast(4).uppercase()}" }
                    list.add(
                        VoiceRoomDto(
                            id = r.optString("id"),
                            name = r.optString("name"),
                            topic = r.optString("topic"),
                            icon = r.optString("icon", "🛡️"),
                            creator = r.optString("creator"),
                            participantCount = r.optInt("participant_count", pList.size),
                            participants = pList,
                            code = code
                        )
                    )
                }
                cachedVoiceRooms = list
                Result.success(list)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch voice rooms"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createVoiceRoom(
        name: String,
        topic: String,
        icon: String = "⚔️",
        maxParticipants: Int = 8,
        token: String? = null
    ): Result<VoiceRoomDto> = withContext(Dispatchers.IO) {
        try {
            val myUsername = currentUsername ?: "Gamer"
            val payload = JSONObject().apply {
                put("name", name)
                put("topic", topic)
                put("icon", icon)
                put("creator", myUsername)
                put("max_participants", maxParticipants)
            }
            val response = executeRequest(
                endpoint = "/api/voice/rooms/create",
                method = "POST",
                body = payload.toString(),
                token = token ?: sessionToken
            )
            if (response.isSuccess) {
                val obj = JSONObject(response.getOrThrow())
                val r = obj.optJSONObject("room") ?: obj
                val pArr = r.optJSONArray("participants") ?: JSONArray()
                val pList = mutableListOf<VoiceParticipantDto>()
                for (j in 0 until pArr.length()) {
                    val p = pArr.getJSONObject(j)
                    pList.add(
                        VoiceParticipantDto(
                            username = p.optString("username"),
                            nickname = p.optString("nickname").ifEmpty { p.optString("username") },
                            avatar = p.optString("avatar").takeIf { it.isNotEmpty() },
                            rank = p.optString("rank", "Host"),
                            muted = p.optBoolean("muted", false),
                            speaking = p.optBoolean("speaking", true)
                        )
                    )
                }
                val code = r.optString("code").ifEmpty { obj.optString("code", "VOX-8492") }
                val created = VoiceRoomDto(
                    id = r.optString("id"),
                    name = r.optString("name", name),
                    topic = r.optString("topic", topic),
                    icon = r.optString("icon", icon),
                    creator = r.optString("creator", myUsername),
                    participantCount = r.optInt("participant_count", 1),
                    participants = pList,
                    code = code
                )
                Result.success(created)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to create voice room"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVoiceRoomByCode(code: String, token: String? = null): Result<VoiceRoomDto> = withContext(Dispatchers.IO) {
        try {
            val cleanCode = code.trim().uppercase()
            val response = executeRequest(
                endpoint = "/api/voice/rooms/code/$cleanCode",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )
            if (response.isSuccess) {
                val obj = JSONObject(response.getOrThrow())
                if (obj.optString("status") == "error") {
                    return@withContext Result.failure(Exception(obj.optString("message", "Voice room not found")))
                }
                val r = obj.optJSONObject("room") ?: obj
                val pArr = r.optJSONArray("participants") ?: JSONArray()
                val pList = mutableListOf<VoiceParticipantDto>()
                for (j in 0 until pArr.length()) {
                    val p = pArr.getJSONObject(j)
                    pList.add(
                        VoiceParticipantDto(
                            username = p.optString("username"),
                            nickname = p.optString("nickname").ifEmpty { p.optString("username") },
                            avatar = p.optString("avatar").takeIf { it.isNotEmpty() },
                            rank = p.optString("rank", "Member"),
                            muted = p.optBoolean("muted", false),
                            speaking = p.optBoolean("speaking", false)
                        )
                    )
                }
                val room = VoiceRoomDto(
                    id = r.optString("id"),
                    name = r.optString("name"),
                    topic = r.optString("topic"),
                    icon = r.optString("icon", "🛡️"),
                    creator = r.optString("creator"),
                    participantCount = r.optInt("participant_count", pList.size),
                    participants = pList,
                    code = r.optString("code", cleanCode)
                )
                Result.success(room)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to find voice room with code $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendVoiceRoomInvite(
        roomCode: String,
        roomName: String,
        recipientUsername: String,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val myUsername = currentUsername ?: "Host"
            val payload = JSONObject().apply {
                put("room_code", roomCode)
                put("room_name", roomName)
                put("sender_username", myUsername)
                put("recipient_username", recipientUsername)
            }
            val response = executeRequest(
                endpoint = "/api/voice/rooms/invite",
                method = "POST",
                body = payload.toString(),
                token = token ?: sessionToken
            )
            if (response.isSuccess) {
                Result.success(true)
            } else {
                // Fallback: direct DM
                val dmRes = startDM(recipientUsername, token)
                if (dmRes.isSuccess) {
                    val dmId = dmRes.getOrThrow().id
                    val inviteMsg = "🎙️ [VOICE ROOM INVITE]\nJoin my voice room '$roomName'!\n🔑 Room Code: $roomCode\nOpen Voice Lobby & enter the code to join now!"
                    val msgRes = sendMessage(token, dmId, inviteMsg)
                    if (msgRes.isSuccess) {
                        return@withContext Result.success(true)
                    }
                }
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to send invite"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCallLogs(username: String? = null, token: String? = null): Result<List<CallLogDto>> = withContext(Dispatchers.IO) {
        try {
            val user = username ?: currentUsername
            val query = if (!user.isNullOrBlank()) "?username=${java.net.URLEncoder.encode(user, "UTF-8")}" else ""
            val response = executeRequest(
                endpoint = "/api/voice/calls/logs$query",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )
            if (response.isSuccess) {
                val obj = JSONObject(response.getOrThrow())
                val arr = obj.optJSONArray("logs") ?: JSONArray()
                val list = mutableListOf<CallLogDto>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    val dispName = item.optString("display_name", "")
                    val callerName = if (dispName.isNotBlank()) dispName else item.optString("caller_name")
                    list.add(
                        CallLogDto(
                            id = item.optString("id"),
                            username = item.optString("username"),
                            callerName = callerName,
                            callType = item.optString("call_type", "Voice Call"),
                            roomName = item.optString("room_name"),
                            roomCode = item.optString("room_code"),
                            durationSeconds = item.optInt("duration_seconds", 0),
                            isMissed = item.optBoolean("is_missed", false),
                            isOutgoing = item.optBoolean("is_outgoing", true),
                            createdAt = item.optString("created_at", "Recent")
                        )
                    )
                }
                cachedCallLogs = list
                Result.success(list)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch call logs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordCallLog(
        callerName: String,
        callType: String = "Voice Call",
        roomName: String = "",
        roomCode: String = "",
        durationSeconds: Int = 0,
        isMissed: Boolean = false,
        isOutgoing: Boolean = true,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val myUsername = currentUsername ?: "Gamer"
            val payload = JSONObject().apply {
                put("username", myUsername)
                put("caller_name", callerName)
                put("call_type", callType)
                put("room_name", roomName)
                put("room_code", roomCode)
                put("duration_seconds", durationSeconds)
                put("is_missed", isMissed)
                put("is_outgoing", isOutgoing)
            }
            val response = executeRequest(
                endpoint = "/api/voice/calls/log",
                method = "POST",
                body = payload.toString(),
                token = token ?: sessionToken
            )
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCallLog(logId: String, token: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = executeRequest(
                endpoint = "/api/voice/calls/logs/$logId",
                method = "DELETE",
                body = null,
                token = token ?: sessionToken
            )
            cachedCallLogs = cachedCallLogs.filter { it.id != logId }
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            cachedCallLogs = cachedCallLogs.filter { it.id != logId }
            Result.failure(e)
        }
    }

    suspend fun clearCallLogs(username: String? = null, token: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val user = username ?: currentUsername
            val query = if (!user.isNullOrBlank()) "?username=${java.net.URLEncoder.encode(user, "UTF-8")}" else ""
            val response = executeRequest(
                endpoint = "/api/voice/calls/logs$query",
                method = "DELETE",
                body = null,
                token = token ?: sessionToken
            )
            cachedCallLogs = emptyList()
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            cachedCallLogs = emptyList()
            Result.failure(e)
        }
    }

    suspend fun joinVoiceRoom(
        roomId: String,
        username: String = currentUsername ?: "Gamer",
        nickname: String? = null,
        avatar: String? = null,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("username", username)
                put("nickname", nickname ?: username)
                if (avatar != null) put("avatar", avatar)
            }
            val response = executeRequest(
                endpoint = "/api/voice/rooms/$roomId/join",
                method = "POST",
                body = payload.toString(),
                token = token ?: sessionToken
            )
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveVoiceRoom(
        roomId: String,
        username: String = currentUsername ?: "Gamer",
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("username", username)
            }
            val response = executeRequest(
                endpoint = "/api/voice/rooms/$roomId/leave",
                method = "POST",
                body = payload.toString(),
                token = token ?: sessionToken
            )
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun inviteToVoiceCall(
        targetUser: String,
        roomId: String,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("caller_id", currentUserId ?: "")
                put("caller_username", currentUsername ?: "")
                put("caller_name", currentUserDisplayName ?: currentUsername ?: "")
                put("target_user", targetUser)
                put("room_id", roomId)
            }
            val response = executeRequest(
                endpoint = "/api/voice/call/invite",
                method = "POST",
                body = payload.toString(),
                token = token ?: sessionToken
            )
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun respondToVoiceCall(
        roomId: String,
        action: String,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("room_id", roomId)
                put("user_id", currentUserId ?: currentUsername ?: "")
                put("action", action)
            }
            val response = executeRequest(
                endpoint = "/api/voice/call/respond",
                method = "POST",
                body = payload.toString(),
                token = token ?: sessionToken
            )
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun endVoiceCall(
        roomId: String,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("room_id", roomId)
                put("user_id", currentUserId ?: currentUsername ?: "")
            }
            val response = executeRequest(
                endpoint = "/api/voice/call/end",
                method = "POST",
                body = payload.toString(),
                token = token ?: sessionToken
            )
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads an avatar image file to the backend via multipart/form-data.
     * Uses production endpoint POST /api/upload and automatically binds the avatar
     * to the user account via POST /api/account/settings.
     * Returns the full avatar URL on success.
     */
    suspend fun uploadAvatar(
        context: android.content.Context,
        imageUri: android.net.Uri,
        token: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val activeToken = token ?: sessionToken
            val boundary = "ConnectoAvatarBoundary_${System.currentTimeMillis()}"

            // Read image bytes from content:// URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Result.failure(Exception("Cannot read image from device"))
            val imageBytes = inputStream.use { it.readBytes() }

            // Determine MIME type
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val extension = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                else -> "jpg"
            }
            val fileName = "avatar.$extension"

            val candidateBases = listOf(ConnectoNetworkConfig.activeBaseUrl)
            var lastException: Exception? = null

            for (base in candidateBases) {
                var conn: java.net.HttpURLConnection? = null
                try {
                    // Production upload endpoint is /api/upload
                    val uploadUrl = "$base/api/upload"
                    val url = java.net.URL(uploadUrl)
                    conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 15000
                        readTimeout = 20000
                        doOutput = true
                        setRequestProperty("User-Agent", USER_AGENT)
                        setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                        if (!activeToken.isNullOrEmpty()) {
                            setRequestProperty("Authorization", "Bearer $activeToken")
                        }
                        if (!sessionCookie.isNullOrEmpty()) {
                            setRequestProperty("Cookie", sessionCookie)
                        }
                    }

                    // Write multipart body (field name must be "file")
                    conn.outputStream.use { output ->
                        val writer = java.io.PrintWriter(java.io.OutputStreamWriter(output, "UTF-8"), true)
                        writer.append("--$boundary\r\n")
                        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
                        writer.append("Content-Type: $mimeType\r\n")
                        writer.append("\r\n")
                        writer.flush()
                        output.write(imageBytes)
                        output.flush()
                        writer.append("\r\n--$boundary--\r\n")
                        writer.flush()
                    }

                    val responseCode = conn.responseCode
                    val isSuccess = responseCode in 200..299

                    // Extract cookies
                    val cookieHeaders = conn.headerFields["Set-Cookie"]
                    if (!cookieHeaders.isNullOrEmpty()) {
                        sessionCookie = cookieHeaders.first().substringBefore(";")
                    }

                    val stream = if (isSuccess) conn.inputStream else conn.errorStream
                    val responseText = stream?.bufferedReader(Charsets.UTF_8)?.readText() ?: ""

                    if (isSuccess) {
                        ConnectoNetworkConfig.activeBaseUrl = base
                        val json = JSONObject(responseText)
                        val returnedPath = json.optString("url").ifEmpty {
                            json.optString("avatar_url").ifEmpty {
                                json.optJSONObject("user")?.optString("avatar_url") ?: ""
                            }
                        }
                        val fullAvatarUrl = if (returnedPath.startsWith("http://") || returnedPath.startsWith("https://")) {
                            returnedPath
                        } else {
                            "$base$returnedPath"
                        }

                        // Persist to user's account settings on the server
                        val u = currentUsername
                        if (!u.isNullOrBlank()) {
                            try {
                                updateProfile(
                                    username = u,
                                    avatarUrl = fullAvatarUrl,
                                    token = activeToken
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("ConnectoSync", "Error persisting avatar to settings", e)
                            }
                        }

                        // Update local reactive avatar state and prefs
                        updateAvatarState(fullAvatarUrl)
                        try {
                            context.getSharedPreferences("connecto_session_prefs", android.content.Context.MODE_PRIVATE)
                                .edit()
                                .putString("avatar_url", fullAvatarUrl)
                                .apply()
                            if (!u.isNullOrBlank()) {
                                context.getSharedPreferences("connecto_user_profile_prefs", android.content.Context.MODE_PRIVATE)
                                    .edit()
                                    .putString("photo_uri_$u", fullAvatarUrl)
                                    .apply()
                            }
                        } catch (e: Exception) {
                            // ignore
                        }

                        return@withContext Result.success(fullAvatarUrl)
                    } else {
                        lastException = Exception("Avatar upload failed ($responseCode): $responseText")
                    }
                } catch (e: Exception) {
                    lastException = e
                } finally {
                    conn?.disconnect()
                }
            }
            Result.failure(lastException ?: Exception("Avatar upload failed — no reachable server"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads a cover banner image file to the backend via multipart/form-data.
     * Compresses/resizes to max 1200x400 on the device first,
     * uses endpoint POST /api/upload/banner (with fallback to /api/upload),
     * and automatically binds the banner to the user account via updateProfile.
     * Returns the full banner URL on success.
     */
    suspend fun uploadBanner(
        context: android.content.Context,
        imageUri: android.net.Uri,
        token: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val activeToken = token ?: sessionToken
            val boundary = "ConnectoBannerBoundary_${System.currentTimeMillis()}"

            // 1. Client-side Resize & Compression to 1200x400 WebP
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Result.failure(Exception("Cannot read image from device"))

            val originalBitmap = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, imageUri)
                    android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    android.graphics.BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                null
            } ?: return@withContext Result.failure(Exception("Failed to decode image file"))

            // Target aspect ratio is 3:1 (1200 x 400)
            val targetW = 1200
            val targetH = 400
            val srcW = originalBitmap.width
            val srcH = originalBitmap.height
            val targetRatio = targetW.toFloat() / targetH.toFloat()
            val srcRatio = srcW.toFloat() / srcH.toFloat()

            val croppedBitmap = if (srcRatio > targetRatio) {
                val newW = (srcH * targetRatio).toInt().coerceAtMost(srcW)
                val left = (srcW - newW) / 2
                android.graphics.Bitmap.createBitmap(originalBitmap, left, 0, newW, srcH)
            } else {
                val newH = (srcW / targetRatio).toInt().coerceAtMost(srcH)
                val top = (srcH - newH) / 2
                android.graphics.Bitmap.createBitmap(originalBitmap, 0, top, srcW, newH)
            }

            val finalBitmap = if (croppedBitmap.width > targetW || croppedBitmap.height > targetH) {
                android.graphics.Bitmap.createScaledBitmap(croppedBitmap, targetW, targetH, true)
            } else {
                croppedBitmap
            }

            val byteStream = java.io.ByteArrayOutputStream()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                finalBitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP_LOSSY, 85, byteStream)
            } else {
                @Suppress("DEPRECATION")
                finalBitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 85, byteStream)
            }
            val imageBytes = byteStream.toByteArray()
            val fileName = "banner.webp"
            val mimeType = "image/webp"

            val candidateBases = listOf(ConnectoNetworkConfig.activeBaseUrl)
            var lastException: Exception? = null

            for (base in candidateBases) {
                var conn: java.net.HttpURLConnection? = null
                try {
                    val uploadUrl = "$base/api/upload/banner"
                    val url = java.net.URL(uploadUrl)
                    conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 15000
                        readTimeout = 20000
                        doOutput = true
                        setRequestProperty("User-Agent", USER_AGENT)
                        setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                        if (!activeToken.isNullOrEmpty()) {
                            setRequestProperty("Authorization", "Bearer $activeToken")
                        }
                        if (!sessionCookie.isNullOrEmpty()) {
                            setRequestProperty("Cookie", sessionCookie)
                        }
                    }

                    // Write multipart body (field name "file")
                    conn.outputStream.use { output ->
                        val writer = java.io.PrintWriter(java.io.OutputStreamWriter(output, "UTF-8"), true)
                        writer.append("--$boundary\r\n")
                        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
                        writer.append("Content-Type: $mimeType\r\n")
                        writer.append("\r\n")
                        writer.flush()
                        output.write(imageBytes)
                        output.flush()
                        writer.append("\r\n--$boundary--\r\n")
                        writer.flush()
                    }

                    val responseCode = conn.responseCode
                    val isSuccess = responseCode in 200..299

                    val cookieHeaders = conn.headerFields["Set-Cookie"]
                    if (!cookieHeaders.isNullOrEmpty()) {
                        sessionCookie = cookieHeaders.first().substringBefore(";")
                    }

                    val stream = if (isSuccess) conn.inputStream else conn.errorStream
                    val responseText = stream?.bufferedReader(Charsets.UTF_8)?.readText() ?: ""

                    if (isSuccess) {
                        ConnectoNetworkConfig.activeBaseUrl = base
                        val json = JSONObject(responseText)
                        val returnedPath = json.optString("url").ifEmpty {
                            json.optString("banner_url").ifEmpty {
                                json.optJSONObject("user")?.optString("banner_url") ?: ""
                            }
                        }
                        val fullBannerUrl = if (returnedPath.startsWith("http://") || returnedPath.startsWith("https://")) {
                            returnedPath
                        } else {
                            "$base$returnedPath"
                        }

                        // Persist to user's account settings on the server
                        val u = currentUsername
                        if (!u.isNullOrBlank()) {
                            try {
                                updateProfile(
                                    username = u,
                                    bannerUrl = fullBannerUrl,
                                    token = activeToken
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("ConnectoSync", "Error persisting banner to settings", e)
                            }
                        }

                        try {
                            if (!u.isNullOrBlank()) {
                                context.getSharedPreferences("connecto_user_profile_prefs", android.content.Context.MODE_PRIVATE)
                                    .edit()
                                    .putString("banner_uri_$u", fullBannerUrl)
                                    .apply()
                            }
                        } catch (e: Exception) {
                            // ignore
                        }

                        return@withContext Result.success(fullBannerUrl)
                    } else {
                        lastException = Exception("Banner upload failed ($responseCode): $responseText")
                    }
                } catch (e: Exception) {
                    lastException = e
                } finally {
                    conn?.disconnect()
                }
            }
            Result.failure(lastException ?: Exception("Banner upload failed — no reachable server"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads an already cropped/adjusted avatar bitmap directly.
     */
    suspend fun uploadAvatarBitmap(
        context: android.content.Context,
        bitmap: android.graphics.Bitmap,
        token: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cacheFile = java.io.File(context.cacheDir, "avatar_${System.currentTimeMillis()}.webp")
            cacheFile.outputStream().use { out ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP_LOSSY, 90, out)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 90, out)
                }
            }
            uploadAvatar(context, android.net.Uri.fromFile(cacheFile), token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uploads an already cropped/adjusted cover banner bitmap directly.
     */
    suspend fun uploadBannerBitmap(
        context: android.content.Context,
        bitmap: android.graphics.Bitmap,
        token: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cacheFile = java.io.File(context.cacheDir, "banner_${System.currentTimeMillis()}.webp")
            cacheFile.outputStream().use { out ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP_LOSSY, 88, out)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 88, out)
                }
            }
            uploadBanner(context, android.net.Uri.fromFile(cacheFile), token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun executeRequest(
        endpoint: String,
        method: String,
        body: String?,
        token: String?
    ): Result<String> {
        val candidateBases = if (ConnectoNetworkConfig.activeBaseUrl.contains("localhost") || ConnectoNetworkConfig.activeBaseUrl.contains("10.0.2.2")) {
            listOf(ConnectoNetworkConfig.activeBaseUrl, "http://localhost:8000", "http://10.0.2.2:8000").distinct()
        } else {
            listOf(ConnectoNetworkConfig.activeBaseUrl)
        }

        val activeToken = token ?: getPersistedToken()
        var lastException: Exception? = null

        for (base in candidateBases) {
            val cleanEndpoint = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
            val fullUrl = "$base$cleanEndpoint"
            var conn: HttpURLConnection? = null

            try {
                android.util.Log.d("ConnectoSync", "executeRequest: $method $fullUrl")
                val url = URL(fullUrl)
                conn = (url.openConnection() as HttpURLConnection).apply {
                    if (this is javax.net.ssl.HttpsURLConnection) {
                        ConnectoNetworkHelper.patchedSslSocketFactory?.let { factory ->
                            sslSocketFactory = factory
                        }
                    }
                    requestMethod = method
                    connectTimeout = 12000
                    readTimeout = 15000
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", "application/json")
                    if (!activeToken.isNullOrEmpty()) {
                        setRequestProperty("Authorization", "Bearer $activeToken")
                    } else if (!sessionCookie.isNullOrEmpty()) {
                        setRequestProperty("Cookie", sessionCookie)
                    }
                    val u = currentUsername
                    if (!u.isNullOrBlank()) {
                        setRequestProperty("X-User-Username", u.trim().lowercase().removePrefix("@"))
                    }
                }

                if (body != null && (method == "POST" || method == "PUT" || method == "PATCH")) {
                    conn.doOutput = true
                    OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                        writer.write(body)
                        writer.flush()
                    }
                }

                val responseCode = conn.responseCode
                val isSuccess = responseCode in 200..299
                android.util.Log.d("ConnectoSync", "executeRequest: $method $fullUrl returned $responseCode (success=$isSuccess)")

                // Detect Cloudflare server mode
                val serverMode = conn.getHeaderField("X-Connecto-Mode")
                if (!serverMode.isNullOrBlank()) {
                    ConnectoNetworkConfig.activeServerMode = serverMode
                    android.util.Log.d("ConnectoSync", "Connecto active server mode: $serverMode")
                }

                // Extract any Set-Cookie headers
                val cookieHeaders = conn.headerFields["Set-Cookie"]
                if (!cookieHeaders.isNullOrEmpty()) {
                    val candidate = cookieHeaders.first().substringBefore(";")
                    if (candidate.startsWith("connecto_session=") && candidate.length > "connecto_session=".length) {
                        sessionCookie = candidate
                    }
                }

                val stream = if (isSuccess) conn.inputStream else conn.errorStream
                val responseText = if (stream != null) {
                    BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                        reader.readText()
                    }
                } else {
                    ""
                }

                if (isSuccess) {
                    ConnectoNetworkConfig.activeBaseUrl = base
                    return Result.success(responseText)
                } else if (responseCode in 400..499 && responseCode != 404) {
                    ConnectoNetworkConfig.activeBaseUrl = base
                    val errorDetail = parseErrorMessage(responseText, responseCode)
                    return Result.failure(Exception(errorDetail))
                } else {
                    // 404 or 5xx: fall through to check next server base
                    lastException = Exception("Server returned code $responseCode: $responseText")
                }
            } catch (e: Exception) {
                android.util.Log.e("ConnectoSync", "executeRequest: FAILED $method $fullUrl: ${e.javaClass.name}: ${e.message}", e)
                lastException = e
            } finally {
                conn?.disconnect()
            }
        }

        return Result.failure(lastException ?: Exception("Network connection failed. Please ensure your internet or server is connected."))
    }

    private fun parseErrorMessage(responseText: String, responseCode: Int): String {
        if (responseText.isBlank()) return "Server error ($responseCode)"
        return try {
            val errorJson = JSONObject(responseText)
            when {
                errorJson.has("detail") -> {
                    val detailObj = errorJson.get("detail")
                    when (detailObj) {
                        is String -> detailObj
                        is JSONArray -> {
                            val messages = mutableListOf<String>()
                            for (i in 0 until detailObj.length()) {
                                val item = detailObj.optJSONObject(i)
                                if (item != null) {
                                    val msg = item.optString("msg", "")
                                    val loc = item.optJSONArray("loc")
                                    val field = if (loc != null && loc.length() > 1) {
                                        loc.optString(loc.length() - 1)
                                    } else null
                                    if (!field.isNullOrBlank() && msg.isNotBlank()) {
                                        val cleanField = field.replace("_", " ").replaceFirstChar { it.uppercase() }
                                        messages.add("$cleanField: $msg")
                                    } else if (msg.isNotBlank()) {
                                        messages.add(msg)
                                    }
                                } else {
                                    val strVal = detailObj.optString(i)
                                    if (strVal.isNotBlank()) messages.add(strVal)
                                }
                            }
                            messages.joinToString("\n").ifBlank { responseText }
                        }
                        is JSONObject -> detailObj.toString()
                        else -> detailObj.toString()
                    }
                }
                errorJson.has("message") -> errorJson.optString("message", responseText)
                errorJson.has("error") -> errorJson.optString("error", responseText)
                else -> responseText
            }
        } catch (e: Exception) {
            responseText.ifBlank { "HTTP Error $responseCode" }
        }
    }

    suspend fun getProfile(username: String? = null, token: String? = null): Result<ProfileDataDto> = withContext(Dispatchers.IO) {
        try {
            val u = username ?: currentUsername ?: "user_99"
            val encoded = java.net.URLEncoder.encode(u, "UTF-8")
            var response = executeRequest(
                endpoint = "/api/user/profile?username=$encoded",
                method = "GET",
                body = null,
                token = token ?: sessionToken
            )
            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/users/profile?username=$encoded",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
            }
            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/users/me",
                    method = "GET",
                    body = null,
                    token = token ?: sessionToken
                )
            }

            if (response.isSuccess) {
                val obj = JSONObject(response.getOrThrow())
                val userObj = if (obj.has("user")) obj.optJSONObject("user") ?: obj else obj
                val isStealth = userObj.optBoolean("is_stealth", false)
                val isOnline = !isStealth && userObj.optBoolean("is_online", true)

                var av = userObj.optString("avatar").ifEmpty {
                    userObj.optString("picture").ifEmpty {
                        userObj.optString("avatar_url")
                    }
                }.takeIf { it.isNotEmpty() }

                if (av != null && av.startsWith("/")) {
                    av = "${ConnectoNetworkConfig.activeBaseUrl}$av"
                }

                var bUrl = userObj.optString("banner_url").takeIf { it.isNotEmpty() }
                if (bUrl != null && bUrl.startsWith("/")) {
                    bUrl = "${ConnectoNetworkConfig.activeBaseUrl}$bUrl"
                }

                val prof = ProfileDataDto(
                    id = userObj.optString("id"),
                    username = userObj.optString("username", u),
                    displayName = userObj.optString("display_name").ifEmpty { userObj.optString("nickname", u) },
                    fullName = userObj.optString("full_name", ""),
                    bio = userObj.optString("bio", ""),
                    avatarUrl = av,
                    bannerUrl = bUrl,
                    email = userObj.optString("email").takeIf { it.isNotEmpty() },
                    phoneNumber = userObj.optString("phone_number").takeIf { it.isNotEmpty() },
                    dateOfBirth = userObj.optString("date_of_birth").takeIf { it.isNotEmpty() },
                    gender = userObj.optString("gender").takeIf { it.isNotEmpty() },
                    location = userObj.optString("location").takeIf { it.isNotEmpty() },
                    usernameChanged = userObj.optBoolean("username_changed", false),
                    twoFactorEnabled = userObj.optBoolean("two_factor_enabled", false),
                    twoFactorMethod = userObj.optString("two_factor_method", "sms"),
                    isStealth = isStealth,
                    isOnline = isOnline
                )
                val isSelf = u.equals(currentUsername, ignoreCase = true)
                if (isSelf) {
                    if (prof.displayName.isNotBlank()) {
                        currentUserDisplayName = prof.displayName
                    }
                    if (!prof.avatarUrl.isNullOrBlank()) {
                        updateAvatarState(prof.avatarUrl)
                    }
                    updateStealthModeState(prof.isStealth)
                }
                Result.success(prof)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setStealthMode(username: String? = null, enabled: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val u = username ?: currentUsername ?: "user_99"
            val body = JSONObject().apply {
                put("username", u)
                put("enabled", enabled)
                put("is_stealth", enabled)
            }
            val response = executeRequest(
                endpoint = "/api/users/stealth",
                method = "POST",
                body = body.toString(),
                token = sessionToken
            )
            if (response.isSuccess) {
                updateStealthModeState(enabled)
            }
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        username: String? = null,
        displayName: String? = null,
        fullName: String? = null,
        bio: String? = null,
        gender: String? = null,
        location: String? = null,
        dateOfBirth: String? = null,
        avatarUrl: String? = null,
        bannerUrl: String? = null,
        phoneNumber: String? = null,
        isStealth: Boolean? = null,
        token: String? = null
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val u = username ?: currentUsername ?: "user_99"
            val body = JSONObject().apply {
                put("username", u)
                displayName?.let {
                    put("nickname", it)
                    put("display_name", it)
                }
                fullName?.let { put("full_name", it) }
                bio?.let { put("bio", it) }
                gender?.let { put("gender", it) }
                location?.let { put("location", it) }
                dateOfBirth?.let { put("date_of_birth", it) }
                phoneNumber?.let { put("phone_number", it) }
                avatarUrl?.let {
                    put("avatar", it)
                    put("picture", it)
                    put("avatar_url", it)
                }
                bannerUrl?.let {
                    put("banner_url", it)
                }
                isStealth?.let { put("is_stealth", it) }
            }

            var response = executeRequest(
                endpoint = "/api/v1/users/me",
                method = "PATCH",
                body = body.toString(),
                token = token ?: sessionToken
            )
            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/account/settings",
                    method = "POST",
                    body = body.toString(),
                    token = token ?: sessionToken
                )
            }
            if (!response.isSuccess) {
                response = executeRequest(
                    endpoint = "/api/users/profile",
                    method = "POST",
                    body = body.toString(),
                    token = token ?: sessionToken
                )
            }

            if (response.isSuccess) {
                if (!displayName.isNullOrBlank()) {
                    currentUserDisplayName = displayName
                }
                if (!avatarUrl.isNullOrBlank()) {
                    updateAvatarState(avatarUrl)
                }
                Result.success(JSONObject(response.getOrThrow()))
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestContactChangeOtp(
        newContact: String,
        method: String = "email",
        token: String? = null
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("new_contact", newContact.trim())
                put("method", method.trim().lowercase())
            }
            val response = executeRequest(
                endpoint = "/api/v1/users/me/change-contact/request-otp",
                method = "POST",
                body = body.toString(),
                token = token ?: sessionToken
            )
            if (response.isSuccess) {
                Result.success(JSONObject(response.getOrThrow()))
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to request verification code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyContactChangeOtp(
        newContact: String,
        method: String = "email",
        otpCode: String,
        token: String? = null
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("new_contact", newContact.trim())
                put("method", method.trim().lowercase())
                put("otp_code", otpCode.trim())
            }
            val response = executeRequest(
                endpoint = "/api/v1/users/me/change-contact/verify",
                method = "POST",
                body = body.toString(),
                token = token ?: sessionToken
            )
            if (response.isSuccess) {
                val obj = JSONObject(response.getOrThrow())
                if (method.equals("email", ignoreCase = true)) {
                    currentUserEmail = newContact.trim()
                }
                Result.success(obj)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Verification code is invalid or expired"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    suspend fun getNotifications(
        unreadOnly: Boolean = false,
        limit: Int = 50,
        username: String? = currentUsername,
        token: String? = null
    ): Result<List<NotificationDto>> = withContext(Dispatchers.IO) {
        try {
            val u = username ?: currentUsername ?: "user_99"
            val endpoint = "/api/v1/notifications?username=$u&unread_only=$unreadOnly&limit=$limit"
            val response = executeRequest(endpoint, "GET", null, token ?: sessionToken)
            if (response.isSuccess) {
                val array = JSONArray(response.getOrThrow())
                val list = mutableListOf<NotificationDto>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        NotificationDto(
                            id = obj.optString("id"),
                            userId = obj.optString("user_id"),
                            type = obj.optString("type"),
                            title = obj.optString("title"),
                            content = obj.optString("content"),
                            senderUsername = obj.optString("sender_username").takeIf { it.isNotBlank() },
                            senderAvatar = obj.optString("sender_avatar").takeIf { it.isNotBlank() },
                            referenceId = obj.optString("reference_id").takeIf { it.isNotBlank() },
                            isRead = obj.optBoolean("is_read", false),
                            createdAt = obj.optString("created_at")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to get notifications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnreadNotificationCount(
        username: String? = currentUsername,
        token: String? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val u = username ?: currentUsername ?: "user_99"
            val endpoint = "/api/v1/notifications/count?username=$u"
            val response = executeRequest(endpoint, "GET", null, token ?: sessionToken)
            if (response.isSuccess) {
                val obj = JSONObject(response.getOrThrow())
                val count = obj.optInt("unread_count", 0)
                _unreadNotificationCount.value = count
                Result.success(count)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to get notification count"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(
        notificationId: String,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "/api/v1/notifications/$notificationId/read"
            val response = executeRequest(endpoint, "POST", "{}", token ?: sessionToken)
            if (response.isSuccess) {
                if (_unreadNotificationCount.value > 0) {
                    _unreadNotificationCount.value -= 1
                }
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to mark read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsRead(
        username: String? = currentUsername,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val u = username ?: currentUsername ?: "user_99"
            val endpoint = "/api/v1/notifications/read-all?username=$u"
            val response = executeRequest(endpoint, "POST", "{}", token ?: sessionToken)
            if (response.isSuccess) {
                _unreadNotificationCount.value = 0
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to mark all read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearNotifications(
        username: String? = currentUsername,
        token: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val u = username ?: currentUsername ?: "user_99"
            val endpoint = "/api/v1/notifications?username=$u"
            val response = executeRequest(endpoint, "DELETE", null, token ?: sessionToken)
            if (response.isSuccess) {
                _unreadNotificationCount.value = 0
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to clear notifications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches real server-side unread DM counts for the current user.
     * Returns Map<partnerUsername, unreadCount>. Own sent messages are excluded server-side.
     */
    suspend fun getDmUnreadCounts(): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val response = executeRequest("/api/dm/unread-counts", "GET", null, sessionToken)
            if (response.isSuccess) {
                val body = response.getOrThrow()
                val obj = JSONObject(body)
                val map = mutableMapOf<String, Int>()
                obj.keys().forEach { key -> map[key] = obj.optInt(key, 0) }
                Result.success(map)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Failed to fetch DM unread counts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registers (or refreshes) the FCM device token with the backend.
     * Call after login and whenever FirebaseMessagingService.onNewToken() fires.
     */
    suspend fun registerFcmToken(fcmToken: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (fcmToken.isBlank() || sessionToken.isNullOrBlank()) return@withContext Result.success(false)
            val body = JSONObject().apply { put("fcm_token", fcmToken) }.toString()
            val response = executeRequest("/api/push/register-token", "POST", body, sessionToken)
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marks all messages in a DM conversation as read for the current user.
     * Call when the user opens a DM chat window.
     */
    suspend fun markDmRead(targetUsername: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (targetUsername.isBlank()) return@withContext Result.success(false)
            val encoded = java.net.URLEncoder.encode(targetUsername.trim(), "UTF-8")
            val response = executeRequest("/api/dms/$encoded/read", "POST", "{}", sessionToken)
            Result.success(response.isSuccess)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
