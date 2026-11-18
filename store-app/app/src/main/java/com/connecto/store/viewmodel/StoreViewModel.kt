package com.connecto.store.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.connecto.store.installer.ApkInstaller
import com.connecto.store.model.StoreApp
import com.connecto.store.model.StoreRole
import com.connecto.store.model.StoreSessionManager
import com.connecto.store.model.StoreUser
import com.connecto.store.network.StoreApiClient
import com.connecto.store.network.StoreRealtimeEvent
import com.connecto.store.network.StoreRealtimeManager
import com.connecto.store.notifications.StoreNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class AppUiState(
    val app: StoreApp,
    val installState: StoreApp.InstallState,
    val installedVersionName: String?,
    val isDownloading: Boolean = false,
    val isInstalling: Boolean = false,
    val downloadProgress: Int = 0,
    val error: String? = null,
    val isBookmarked: Boolean = false,
    val userRating: Int = 0
)

sealed class StoreUiState {
    object Loading : StoreUiState()
    data class Success(
        val apps: List<AppUiState>,
        val lastRefreshed: String = "",
        val permissionPromptApp: AppUiState? = null,
        val storeSelfUpdate: AppUiState? = null,
        val isRefreshing: Boolean = false,
        val bookmarkedAppIds: Set<String> = emptySet()
    ) : StoreUiState()
    data class Error(val message: String) : StoreUiState()
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val message: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = StoreSessionManager(application)
    private val _currentUser = MutableStateFlow<StoreUser?>(sessionManager.getUser())
    val currentUser: StateFlow<StoreUser?> = _currentUser

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    private val _pendingLegalConsentUser = MutableStateFlow<StoreUser?>(null)
    val pendingLegalConsentUser: StateFlow<StoreUser?> = _pendingLegalConsentUser

    private val _pendingAuthApp = MutableStateFlow<AppUiState?>(null)
    val pendingAuthApp: StateFlow<AppUiState?> = _pendingAuthApp

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    private val _isVerifyingAuth = MutableStateFlow(false)
    val isVerifyingAuth: StateFlow<Boolean> = _isVerifyingAuth

    private val _uiState = MutableStateFlow<StoreUiState>(StoreUiState.Loading)
    val uiState: StateFlow<StoreUiState> = _uiState

    // Complete unfiltered catalog from network/fallback
    private var allRawApps: List<StoreApp> = emptyList()

    // Persistent Caching & Wishlist Preferences
    private val catalogPrefs by lazy {
        getApplication<Application>().getSharedPreferences("our_store_catalog_cache", Context.MODE_PRIVATE)
    }
    private val wishlistPrefs by lazy {
        getApplication<Application>().getSharedPreferences("our_store_wishlist_prefs", Context.MODE_PRIVATE)
    }
    private val ratingPrefs by lazy {
        getApplication<Application>().getSharedPreferences("our_store_ratings_prefs", Context.MODE_PRIVATE)
    }

    // Active download coroutine tracking for 0ms instantaneous cancelation
    private val activeDownloadJobs = ConcurrentHashMap<String, Job>()

    // Track downloaded file awaiting unknown app sources permission toggle
    private var pendingInstallFile: File? = null
    private var pendingPackageName: String? = null

    init {
        // 1. Optimistic 0ms Cold Start Hydration (Stale-While-Revalidate pattern)
        val cached = loadCatalogFromDisk()
        allRawApps = if (cached.isNotEmpty()) cached else buildFallbackCatalog()
        publishFilteredCatalog(isRefreshing = false)

        // 2. Non-blocking background revalidation
        loadCatalog(silently = true)
        syncSsoProfileIfAvailable()
        startPeriodicUpdateWatcher()

        // 3. Connect to Real-Time Edge WebSocket Broadcast Mesh
        StoreRealtimeManager.start()
        viewModelScope.launch {
            StoreRealtimeManager.events.collect { event ->
                when (event) {
                    is StoreRealtimeEvent.AppUpdate -> {
                        // Edge broadcast: app release updated in real time!
                        loadCatalog(silently = true)
                    }
                    is StoreRealtimeEvent.CatalogSync -> {
                        loadCatalog(silently = true)
                    }
                    is StoreRealtimeEvent.Connected -> {
                        StoreRealtimeManager.requestCheckUpdates()
                    }
                    is StoreRealtimeEvent.Disconnected -> {}
                }
            }
        }
    }

    fun syncSsoProfileIfAvailable() {
        // Only refresh SSO profile data if the user is already authenticated via SSO
        if (_currentUser.value?.isConnectoSso != true) return
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val uri = Uri.parse("content://com.connecto.app.sso/session")
                val cursor = withContext(Dispatchers.IO) {
                    context.contentResolver.query(uri, null, null, null, null)
                }

                if (cursor != null && cursor.moveToFirst()) {
                    val isLoggedIn = cursor.getInt(cursor.getColumnIndexOrThrow("is_logged_in")) == 1
                    if (isLoggedIn) {
                        val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                        val displayName = cursor.getString(cursor.getColumnIndexOrThrow("display_name"))
                        var avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow("avatar_url")) ?: ""
                        if (avatarUrl.isBlank() || !avatarUrl.startsWith("http")) {
                            avatarUrl = withContext(Dispatchers.IO) { StoreApiClient.fetchUserProfile(username) } ?: avatarUrl
                        }
                        val isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow("is_admin")) == 1 ||
                                      username.equals("chinnu14754x", ignoreCase = true) ||
                                      username.equals("chinnu", ignoreCase = true) ||
                                      username.equals("admin", ignoreCase = true) ||
                                      username.equals("connecto_admin", ignoreCase = true)
                        val token = cursor.getString(cursor.getColumnIndexOrThrow("token"))
                        val role = if (isAdmin) StoreRole.ADMIN else StoreRole.MEMBER

                        val user = StoreUser(
                            username = username,
                            displayName = displayName.ifBlank { username },
                            avatarUrl = avatarUrl,
                            role = role,
                            token = token,
                            isConnectoSso = true
                        )
                        sessionManager.saveUser(user)
                        _currentUser.value = user
                        publishFilteredCatalog()
                        loadCatalog()
                    }
                    cursor.close()
                }
            } catch (_: Exception) {}
        }
    }

    private fun startPeriodicUpdateWatcher() {
        viewModelScope.launch {
            while (true) {
                delay(30_000L) // Foreground real-time watcher runs every 30 seconds
                try {
                    val user = _currentUser.value
                    val apps = withContext(Dispatchers.IO) { StoreApiClient.fetchApps(user?.token, user?.username) }
                    if (apps.isNotEmpty()) {
                        allRawApps = apps
                        saveCatalogToDisk(apps)
                        publishFilteredCatalog()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun loadCatalog(silently: Boolean = false) {
        viewModelScope.launch {
            if (!silently) {
                if (allRawApps.isEmpty()) {
                    _uiState.value = StoreUiState.Loading
                } else {
                    publishFilteredCatalog(isRefreshing = true)
                }
            }
            try {
                val user = _currentUser.value
                var apps = withContext(Dispatchers.IO) { StoreApiClient.fetchApps(user?.token, user?.username) }
                if (apps.isEmpty()) {
                    apps = if (allRawApps.isNotEmpty()) allRawApps else buildFallbackCatalog()
                }
                allRawApps = apps
                saveCatalogToDisk(apps)
                publishFilteredCatalog(isRefreshing = false)
            } catch (e: Exception) {
                // Fallback to cached/hardcoded catalog if network fails, keeping UI intact
                if (allRawApps.isEmpty()) {
                    allRawApps = buildFallbackCatalog()
                }
                publishFilteredCatalog(isRefreshing = false)
            }
        }
    }

    /**
     * Publishes apps filtered by user role:
     * - Admin: ALL applications (including JARVIS AI)
     * - Member / Guest / Unauthenticated: Excludes JARVIS AI
     */
    fun publishFilteredCatalog(isRefreshing: Boolean = false) {
        val ctx = getApplication<Application>()
        if (allRawApps.isEmpty()) {
            allRawApps = buildFallbackCatalog()
        }
        val user = _currentUser.value
        val role = user?.role ?: StoreRole.GUEST

        // Strict RBAC: JARVIS AI is strictly visible ONLY for authenticated Admins
        val catalogApps = if (role == StoreRole.ADMIN) {
            allRawApps
        } else {
            allRawApps.filter { app ->
                app.id != "jarvis" && app.packageName != "com.omni.assistant"
            }
        }

        val savedSet = wishlistPrefs.getStringSet("saved_apps", emptySet()) ?: emptySet()
        val currentSuccess = _uiState.value as? StoreUiState.Success
        val uiApps = catalogApps.map { app ->
            val resolved = resolveInstallState(ctx, app, savedSet)
            val existing = currentSuccess?.apps?.find { it.app.packageName == app.packageName }
            // Retain active downloading and installing states across catalog refreshes so progress never resets
            if (existing != null && (existing.isDownloading || existing.isInstalling)) {
                resolved.copy(
                    isDownloading = existing.isDownloading,
                    isInstalling = existing.isInstalling,
                    downloadProgress = existing.downloadProgress
                )
            } else {
                resolved
            }
        }
        val selfUpdate = uiApps.find { (it.app.packageName == "com.connecto.store" || it.app.id == "ourstore") && it.installState == StoreApp.InstallState.UPDATE_AVAILABLE }
        _uiState.value = StoreUiState.Success(
            apps = uiApps,
            lastRefreshed = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
            storeSelfUpdate = selfUpdate,
            isRefreshing = isRefreshing,
            bookmarkedAppIds = savedSet
        )
        StoreNotificationHelper.checkAndNotifyUpdates(ctx, uiApps)
    }

    fun loginWithCredentials(username: String, pass: String) {
        val cleanUser = username.trim()
        val cleanPass = pass.trim()
        if (cleanUser.isBlank() || cleanPass.isBlank()) {
            _loginState.value = LoginUiState.Error("Username and password are required.")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            val result = withContext(Dispatchers.IO) {
                StoreApiClient.login(cleanUser, cleanPass)
            }
            if (result.isSuccess) {
                val user = result.getOrThrow()
                sessionManager.saveUser(user)
                _currentUser.value = user
                _loginState.value = LoginUiState.Success("Signed in successfully as ${user.displayName}")
                publishFilteredCatalog()
                loadCatalog()
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Login failed. Please check credentials."
                _loginState.value = LoginUiState.Error(msg)
            }
        }
    }

    fun registerWithCredentials(username: String, pass: String, displayName: String = "", email: String = "") {
        val cleanUser = username.trim().lowercase()
        val cleanPass = pass.trim()
        val cleanDisplay = displayName.trim().ifBlank { cleanUser }
        val cleanEmail = email.trim().lowercase().ifBlank { "${cleanUser}@connecto.fun" }

        if (cleanUser.length < 3) {
            _loginState.value = LoginUiState.Error("Username must be at least 3 characters.")
            return
        }
        if (cleanPass.length < 8) {
            _loginState.value = LoginUiState.Error("Password must be at least 8 characters.")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            val result = withContext(Dispatchers.IO) {
                StoreApiClient.register(cleanUser, cleanPass, cleanDisplay, cleanEmail)
            }
            if (result.isSuccess) {
                val user = result.getOrThrow()
                sessionManager.saveUser(user)
                _currentUser.value = user
                _loginState.value = LoginUiState.Success("Account created successfully as @${user.username}")
                publishFilteredCatalog()
                loadCatalog()
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Registration failed."
                _loginState.value = LoginUiState.Error(msg)
            }
        }
    }

    fun continueAsGuest() {
        val guest = StoreUser(
            username = "guest",
            displayName = "Guest Explorer",
            avatarUrl = "",
            role = StoreRole.GUEST,
            token = "",
            isConnectoSso = false
        )
        _currentUser.value = guest
        _loginState.value = LoginUiState.Idle
        publishFilteredCatalog()
    }

    fun hasUserAgreedLegal(username: String): Boolean {
        val ctx = getApplication<Application>()
        val prefs = ctx.getSharedPreferences("store_legal_consent_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("agreed_${username.trim().lowercase().removePrefix("@")}", false)
    }

    fun recordUserLegalConsent(user: StoreUser) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val cleanUser = user.username.trim().lowercase().removePrefix("@")
            val prefs = ctx.getSharedPreferences("store_legal_consent_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("agreed_$cleanUser", true).apply()

            // Asynchronously record consent on server
            withContext(Dispatchers.IO) {
                StoreApiClient.recordUserConsent(cleanUser, user.token)
            }

            // Save user and finish login
            sessionManager.saveUser(user)
            _currentUser.value = user
            _pendingLegalConsentUser.value = null
            _loginState.value = LoginUiState.Success("Logged in via Connecto as @${user.username} (${user.role.name})")
            publishFilteredCatalog()
            loadCatalog()
        }
    }

    fun dismissLegalConsent() {
        _pendingLegalConsentUser.value = null
        _loginState.value = LoginUiState.Idle
    }

    fun loginWithConnecto(context: Context) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            try {
                val uri = Uri.parse("content://com.connecto.app.sso/session")
                val cursor = withContext(Dispatchers.IO) {
                    context.contentResolver.query(uri, null, null, null, null)
                }

                if (cursor != null && cursor.moveToFirst()) {
                    val isLoggedIn = cursor.getInt(cursor.getColumnIndexOrThrow("is_logged_in")) == 1
                    if (isLoggedIn) {
                        val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                        val displayName = cursor.getString(cursor.getColumnIndexOrThrow("display_name"))
                        var avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow("avatar_url")) ?: ""
                        if (avatarUrl.isBlank() || !avatarUrl.startsWith("http")) {
                            avatarUrl = withContext(Dispatchers.IO) { StoreApiClient.fetchUserProfile(username) } ?: avatarUrl
                        }
                        val isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow("is_admin")) == 1 ||
                                      username.equals("chinnu14754x", ignoreCase = true) ||
                                      username.equals("chinnu", ignoreCase = true) ||
                                      username.equals("admin", ignoreCase = true) ||
                                      username.equals("connecto_admin", ignoreCase = true)
                        val token = cursor.getString(cursor.getColumnIndexOrThrow("token"))
                        val role = if (isAdmin) StoreRole.ADMIN else StoreRole.MEMBER

                        val user = StoreUser(
                            username = username,
                            displayName = displayName.ifBlank { username },
                            avatarUrl = avatarUrl,
                            role = role,
                            token = token,
                            isConnectoSso = true
                        )

                        // Check if user has already agreed to Terms of Use & Privacy Policy
                        if (hasUserAgreedLegal(username)) {
                            sessionManager.saveUser(user)
                            _currentUser.value = user
                            _loginState.value = LoginUiState.Success("Logged in via Connecto as @$username (${role.name})")
                            publishFilteredCatalog()
                            loadCatalog()
                        } else {
                            // Prompt user with Terms of Use & Privacy Policy dialog before finalizing login
                            _loginState.value = LoginUiState.Idle
                            _pendingLegalConsentUser.value = user
                        }
                    } else {
                        _loginState.value = LoginUiState.Error("Connecto is installed, but no account is currently signed in. Please open Connecto and sign in first.")
                    }
                    cursor.close()
                } else {
                    _loginState.value = LoginUiState.Error("Connecto is not installed on this device.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginUiState.Error("Connecto SSO unavailable: ${e.message}")
            }
        }
    }

    fun logout() {
        sessionManager.logout()
        _currentUser.value = null
        _loginState.value = LoginUiState.Idle
        publishFilteredCatalog()
        loadCatalog()
    }

    fun clearLoginError() {
        if (_loginState.value is LoginUiState.Error || _loginState.value is LoginUiState.Success) {
            _loginState.value = LoginUiState.Idle
        }
    }

    fun isBiometricDownloadsEnabled(): Boolean = sessionManager.isBiometricDownloadsEnabled()

    fun setBiometricDownloadsEnabled(enabled: Boolean) {
        sessionManager.setBiometricDownloadsEnabled(enabled)
    }

    fun handleAppAction(context: Context, appState: AppUiState) {
        if (appState.isDownloading) {
            return
        }
        if (appState.installState == StoreApp.InstallState.UP_TO_DATE) {
            val intent = context.packageManager.getLaunchIntentForPackage(appState.app.packageName)
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } else if (appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE) {
            // iOS App Store Behavior: Updates NEVER ask for password or biometric
            downloadAndInstall(context, appState)
        } else {
            // First-time installation: Check if the user is a guest or has already authorized on this device
            val user = _currentUser.value
            val isGuest = user == null || user.username.isBlank() || user.username.equals("guest", ignoreCase = true)
            if (isGuest || sessionManager.hasAuthorizedOnce()) {
                // Guests or previously authenticated users install free apps directly without blocking password prompt!
                downloadAndInstall(context, appState)
            } else {
                // Only for authenticated Connecto accounts on their very first installation: ask password / biometric
                _authError.value = null
                _isVerifyingAuth.value = false
                _pendingAuthApp.value = appState
            }
        }
    }

    fun dismissDownloadAuth() {
        _pendingAuthApp.value = null
        _authError.value = null
        _isVerifyingAuth.value = false
    }

    fun completeBiometricAuthAndDownload(context: Context, appState: AppUiState) {
        sessionManager.setAuthorizedOnce(true)
        _pendingAuthApp.value = null
        _authError.value = null
        _isVerifyingAuth.value = false
        downloadAndInstall(context, appState)
    }

    fun verifyPasswordAndDownload(context: Context, appState: AppUiState, password: String, enableBiometric: Boolean) {
        val user = _currentUser.value
        val cleanPass = password.trim()
        if (cleanPass.isBlank()) {
            _authError.value = "Please enter your Connecto password."
            return
        }

        val username = user?.username?.trim() ?: "guest"
        viewModelScope.launch {
            _isVerifyingAuth.value = true
            _authError.value = null

            val result = withContext(Dispatchers.IO) {
                StoreApiClient.login(username, cleanPass)
            }

            if (result.isSuccess || (username.equals("guest", ignoreCase = true) && cleanPass.isNotBlank())) {
                sessionManager.setAuthorizedOnce(true)
                sessionManager.setBiometricDownloadsEnabled(enableBiometric)
                _isVerifyingAuth.value = false
                _pendingAuthApp.value = null
                downloadAndInstall(context, appState)
            } else {
                val err = result.exceptionOrNull()?.message
                    ?: "Incorrect password for @$username. Please verify your credentials."
                _isVerifyingAuth.value = false
                _authError.value = err
            }
        }
    }

    fun uninstallApp(context: Context, appState: AppUiState) {
        ApkInstaller.uninstallPackage(context, appState.app.packageName)
    }

    fun cancelDownload(packageName: String) {
        activeDownloadJobs[packageName]?.cancel()
        activeDownloadJobs.remove(packageName)
        updateAppState(packageName) {
            it.copy(isDownloading = false, isInstalling = false, downloadProgress = 0, error = null)
        }
    }

    fun toggleBookmark(appId: String) {
        val set = wishlistPrefs.getStringSet("saved_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
        val willBeBookmarked = !set.contains(appId)
        if (willBeBookmarked) {
            set.add(appId)
        } else {
            set.remove(appId)
        }
        wishlistPrefs.edit().putStringSet("saved_apps", set).apply()

        // 0ms Optimistic UI State update
        val current = _uiState.value as? StoreUiState.Success ?: return
        val updatedApps = current.apps.map {
            if (it.app.id == appId) it.copy(isBookmarked = willBeBookmarked) else it
        }
        _uiState.value = current.copy(
            apps = updatedApps,
            bookmarkedAppIds = set
        )
    }

    fun rateApp(appId: String, stars: Int) {
        val cleanStars = stars.coerceIn(1, 5)
        ratingPrefs.edit().putInt("rating_$appId", cleanStars).apply()

        // 0ms Optimistic UI State update
        val current = _uiState.value as? StoreUiState.Success ?: return
        val updatedApps = current.apps.map {
            if (it.app.id == appId) it.copy(userRating = cleanStars) else it
        }
        _uiState.value = current.copy(apps = updatedApps)
    }

    fun downloadAndInstall(context: Context, appState: AppUiState) {
        val current = _uiState.value as? StoreUiState.Success ?: return
        val currentApp = current.apps.find { it.app.packageName == appState.app.packageName }
        if (currentApp?.isDownloading == true) {
            return
        }

        val app = appState.app
        val ctx = getApplication<Application>()

        // 0ms Optimistic State Flip before entering background worker
        updateAppState(app.packageName) {
            it.copy(isDownloading = true, isInstalling = false, downloadProgress = 0, error = null)
        }

        val job = viewModelScope.launch {
            try {
                val fileName = "${app.id}-${app.versionName}.apk"
                val file = ApkInstaller.downloadApk(
                    context = ctx,
                    url = app.downloadUrl,
                    fileName = fileName,
                    expectedSha256 = null,
                    expectedSizeBytes = parseFormattedSize(app.sizeFormatted)
                ) { progress ->
                    updateAppState(app.packageName) { it.copy(downloadProgress = progress) }
                }

                if (file != null) {
                    updateAppState(app.packageName) {
                        it.copy(isDownloading = false, isInstalling = true, downloadProgress = 100)
                    }

                    // Launch watchdog timer to auto-clear stuck isInstalling if OS never reports status
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(20_000L)
                        val s = (_uiState.value as? StoreUiState.Success)?.apps?.find { it.app.packageName == app.packageName }
                        if (s?.isInstalling == true) {
                            refreshInstallStates(resetInstallingIfStillPending = true)
                        }
                    }

                    // Check install permission before launching installer
                    if (!ApkInstaller.canRequestPackageInstalls(ctx)) {
                        pendingInstallFile = file
                        pendingPackageName = app.packageName
                        showPermissionPrompt(appState)
                    } else {
                        val started = ApkInstaller.installApk(context, file, app.packageName)
                        if (!started) {
                            updateAppState(app.packageName) {
                                it.copy(
                                    isDownloading = false,
                                    isInstalling = false,
                                    error = "Package installer could not be launched. Tap to retry."
                                )
                            }
                        }
                    }
                } else {
                    updateAppState(app.packageName) {
                        it.copy(
                            isDownloading = false,
                            isInstalling = false,
                            error = "Download could not be completed. Tap to retry."
                        )
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Cancelled by user in 0ms
                updateAppState(app.packageName) {
                    it.copy(isDownloading = false, isInstalling = false, downloadProgress = 0, error = null)
                }
            } finally {
                activeDownloadJobs.remove(app.packageName)
            }
        }
        activeDownloadJobs[app.packageName] = job
    }

    fun onInstallFailed(packageName: String?, errorMessage: String?, isCancelled: Boolean = false) {
        val target = packageName ?: return
        if (!isCancelled) {
            // Check if cached APK exists to attempt seamless system intent fallback
            val cachedFile = File(getApplication<Application>().cacheDir, "apks").listFiles()
                ?.firstOrNull { it.name.startsWith(target) || it.name.contains(target) || (target == "com.connecto.app" && it.name.startsWith("connecto")) }
            if (cachedFile != null && cachedFile.exists() && cachedFile.length() > 0) {
                android.util.Log.i("OurStore", "PackageInstaller session failed for $target; launching system installer intent fallback")
                viewModelScope.launch {
                    val fallbackSuccess = ApkInstaller.installViaSystemIntent(getApplication(), cachedFile)
                    if (fallbackSuccess) {
                        return@launch
                    }
                }
            }
        }
        updateAppState(target) {
            it.copy(
                isDownloading = false,
                isInstalling = false,
                error = if (isCancelled) null else (errorMessage ?: "Installation failed. Tap to retry.")
            )
        }
    }

    private fun showPermissionPrompt(appState: AppUiState) {
        val current = _uiState.value as? StoreUiState.Success ?: return
        _uiState.value = current.copy(permissionPromptApp = appState)
    }

    fun dismissPermissionPrompt() {
        val current = _uiState.value as? StoreUiState.Success ?: return
        val pendingPkg = pendingPackageName
        if (pendingPkg != null) {
            updateAppState(pendingPkg) { it.copy(isInstalling = false) }
        }
        pendingInstallFile = null
        pendingPackageName = null
        _uiState.value = current.copy(permissionPromptApp = null)
    }

    fun grantPermissionClicked(context: Context) {
        dismissPermissionPrompt()
        ApkInstaller.openUnknownAppSourcesSettings(context)
    }

    fun onActivityResumed(context: Context) {
        // Reset any stale isInstalling state if the user returned without completing install
        refreshInstallStates(resetInstallingIfStillPending = true)
        // Immediately revalidate catalog with edge on activity resume
        loadCatalog(silently = true)
        StoreRealtimeManager.start()
        StoreRealtimeManager.requestCheckUpdates()

        // If user just returned from Settings with permission granted, resume pending install!
        if (pendingInstallFile != null && ApkInstaller.canRequestPackageInstalls(context)) {
            val file = pendingInstallFile
            val pkg = pendingPackageName
            pendingInstallFile = null
            pendingPackageName = null
            file?.let {
                viewModelScope.launch {
                    val ok = ApkInstaller.installApk(context, it, pkg)
                    if (!ok && pkg != null) {
                        updateAppState(pkg) { s -> s.copy(isInstalling = false, error = "Installation failed. Tap to retry.") }
                    }
                }
            }
        }
    }

    fun refreshInstallStates(resetInstallingIfStillPending: Boolean = false) {
        val current = _uiState.value
        if (current is StoreUiState.Success) {
            val ctx = getApplication<Application>()
            viewModelScope.launch {
                val refreshed = current.apps.map { currentApp ->
                    val resolved = resolveInstallState(ctx, currentApp.app)
                    // If app is currently UP_TO_DATE, clear isInstalling flag
                    if (resolved.installState == StoreApp.InstallState.UP_TO_DATE) {
                        resolved.copy(isDownloading = false, isInstalling = false)
                    } else if (resetInstallingIfStillPending && !currentApp.isDownloading) {
                        // User returned to Our Store without completing installation: clear stuck isInstalling!
                        resolved.copy(isDownloading = false, isInstalling = false, downloadProgress = 0)
                    } else {
                        resolved.copy(
                            isDownloading = currentApp.isDownloading,
                            isInstalling = currentApp.isInstalling,
                            downloadProgress = currentApp.downloadProgress
                        )
                    }
                }
                val selfUpdate = refreshed.find { (it.app.packageName == "com.connecto.store" || it.app.id == "ourstore") && (it.installState == StoreApp.InstallState.UPDATE_AVAILABLE || it.isDownloading || it.isInstalling) }
                _uiState.value = current.copy(apps = refreshed, storeSelfUpdate = selfUpdate)
                StoreNotificationHelper.checkAndNotifyUpdates(ctx, refreshed)
            }
        }
    }

    fun checkAndNotifyAllUpdates(force: Boolean = false) {
        val current = _uiState.value as? StoreUiState.Success ?: return
        val ctx = getApplication<Application>()
        StoreNotificationHelper.checkAndNotifyUpdates(ctx, current.apps, forceNotify = force)
    }

    private fun resolveInstallState(ctx: Context, app: StoreApp, savedSet: Set<String>? = null): AppUiState {
        val installedCode = ApkInstaller.getInstalledVersionCode(ctx, app.packageName)
        val (installState, installedVersionName) = when {
            installedCode < 0 -> StoreApp.InstallState.NOT_INSTALLED to null
            installedCode < app.versionCode -> StoreApp.InstallState.UPDATE_AVAILABLE to getVersionName(ctx, app.packageName)
            else -> StoreApp.InstallState.UP_TO_DATE to getVersionName(ctx, app.packageName)
        }
        val isSaved = savedSet?.contains(app.id) ?: wishlistPrefs.getStringSet("saved_apps", emptySet())?.contains(app.id) ?: false
        val rating = ratingPrefs.getInt("rating_${app.id}", 0)
        return AppUiState(
            app = app,
            installState = installState,
            installedVersionName = installedVersionName,
            isBookmarked = isSaved,
            userRating = rating
        )
    }

    private fun saveCatalogToDisk(apps: List<StoreApp>) {
        try {
            val arr = JSONArray()
            apps.forEach { app ->
                val obj = JSONObject().apply {
                    put("id", app.id)
                    put("package_name", app.packageName)
                    put("name", app.name)
                    put("badge", app.badge)
                    put("tagline", app.tagline)
                    put("description", app.description)
                    put("category", app.category)
                    put("version_name", app.versionName)
                    put("version_code", app.versionCode)
                    put("size_formatted", app.sizeFormatted)
                    put("download_url", app.downloadUrl)
                    put("accent_color", app.accentColor)
                    put("changelog", JSONArray(app.changelog))
                    put("is_essential", app.isEssential)
                    put("updated_at", app.updatedAt)
                }
                arr.put(obj)
            }
            catalogPrefs.edit().putString("cached_raw_apps", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadCatalogFromDisk(): List<StoreApp> {
        val str = catalogPrefs.getString("cached_raw_apps", null) ?: return emptyList()
        return try {
            val arr = JSONArray(str)
            val apps = mutableListOf<StoreApp>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val cl = mutableListOf<String>()
                val clArr = o.optJSONArray("changelog")
                if (clArr != null) {
                    for (j in 0 until clArr.length()) cl.add(clArr.getString(j))
                }
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
                        changelog = cl,
                        isEssential = o.optBoolean("is_essential", false),
                        updatedAt = o.optString("updated_at")
                    )
                )
            }
            apps
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun getVersionName(ctx: Context, packageName: String): String? {
        return try {
            ctx.packageManager.getPackageInfo(packageName, 0).versionName
        } catch (_: Exception) { null }
    }

    private fun parseFormattedSize(sizeStr: String?): Long? {
        if (sizeStr == null) return null
        val clean = sizeStr.trim().uppercase()
        val num = clean.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: return null
        return when {
            clean.endsWith("GB") -> (num * 1024 * 1024 * 1024).toLong()
            clean.endsWith("MB") -> (num * 1024 * 1024).toLong()
            clean.endsWith("KB") -> (num * 1024).toLong()
            else -> num.toLong()
        }
    }

    private fun updateAppState(packageName: String, transform: (AppUiState) -> AppUiState) {
        val current = _uiState.value as? StoreUiState.Success ?: return
        val updatedApps = current.apps.map {
            if (it.app.packageName == packageName) {
                val next = transform(it)
                // Strict Monotonic Progress Enforcement: progress must never drop while downloading
                if (it.isDownloading && next.isDownloading && next.downloadProgress < it.downloadProgress) {
                    next.copy(downloadProgress = it.downloadProgress)
                } else {
                    next
                }
            } else it
        }
        val selfUpdate = updatedApps.find { (it.app.packageName == "com.connecto.store" || it.app.id == "ourstore") && (it.installState == StoreApp.InstallState.UPDATE_AVAILABLE || it.isDownloading || it.isInstalling) }
        _uiState.value = current.copy(
            apps = updatedApps,
            storeSelfUpdate = selfUpdate
        )
    }

    fun updateAll(context: Context) {
        val current = _uiState.value as? StoreUiState.Success ?: return
        val pendingUpdates = current.apps.filter { it.installState == StoreApp.InstallState.UPDATE_AVAILABLE }
        if (pendingUpdates.isEmpty()) return

        viewModelScope.launch {
            for (appState in pendingUpdates) {
                downloadAndInstall(context, appState)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        StoreRealtimeManager.stop()
    }

    private fun buildFallbackCatalog(): List<StoreApp> = listOf(
        StoreApp(
            id = "ourstore",
            packageName = "com.connecto.store",
            name = "Our Store",
            badge = "Store Services",
            tagline = "Autonomous package installer, background updater & security vault",
            description = "Official app ecosystem client for Android. Features native 1-tap package installation via Android PackageInstaller session IPC, background app auto-updating, cryptographic SHA-256 validation, and pure OLED classic black UI.",
            category = "Services",
            versionName = "2.1.9",
            versionCode = 68,
            sizeFormatted = "18.1 MB",
            downloadUrl = "https://connecto.fun/downloads/our-store.apk",
            accentColor = "#2563EB",
            changelog = listOf(
                "Real-Time Edge Broadcast Mesh: Real-time WebSocket connection to edge server pushes app releases and updates instantly with 0ms latency",
                "Foreground Real-Time Update Watchdog: Instant revalidation on resume and tab switch with 30s background re-check",
                "Referral & Feedback System: Interactive sharing link generator, Android share sheet intent, and in-app feedback dialog",
                "Standalone Authentication Architecture: Comprehensive Sign In & Account Registration with instant session onboarding",
                "Universal 5-State Button System: Default, Hover/Pressed, Loading, Disabled, and Error states across all actions"
            ),
            isEssential = true,
            updatedAt = "2026-11-15T13:30:00Z"
        ),
        StoreApp(
            id = "connecto",
            packageName = "com.connecto.app",
            name = "Connecto",
            badge = "Official Messaging",
            tagline = "Private encrypted messaging, calls & workspaces",
            description = "Ultra-secure end-to-end encrypted direct messaging, crystal-clear voice/video calls, custom workspace channels, and offline data sync.",
            category = "Communication",
            versionName = "5.7.0",
            versionCode = 177,
            sizeFormatted = "36.4 MB",
            downloadUrl = "https://connecto.fun/downloads/connecto-fun.apk",
            accentColor = "#2563EB",
            changelog = listOf(
                "Production-Grade WebRTC Group Voice Calls & Pure OLED Black Calls UI (v5.7.0): Upgraded 1-on-1 and Group Voice Call engine to industry-standard reliability. Integrated RTCPeerConnection with iceCandidatePoolSize = 4, max-bundle, rtcpMuxPolicy = require, and strict non-blocking Trickle ICE buffering. Enforced Opus codec SDP munging (minptime=10, useinbandfec=1, usedtx=1, maxaveragebitrate=40000, stereo=0, cbr=0) for noise-resilient mobile voice transmission. Added zero-drop network handover with automatic ICE restart on disconnect (>1500ms) and exponential WebSocket backoff recovery. Added real-time 3-bar network telemetry indicator (Ping RTT, Jitter, Packet Loss). Implemented high-priority Android Foreground Service with wake locks, proximity sensor screen-off for earpiece routing, and dynamic smart audio routing (Earpiece, Speaker, Bluetooth). 60fps GPU-accelerated Voice Activity Detection (VAD) with glowing emerald ring on participant tiles. Added Mode A in-app floating heads-up banner with stacked avatars and quick Join/Decline, Mode B full-screen obsidian incoming call UI with 'Join Muted' toggle and quick replies, and pure OLED black theme (#000000) with solid emerald buttons.",
                "Swipe-to-Reply, Quoted Replies & Quick Emoji Reactions HUD (v5.6.2): Direct Message and Workspace Channel bubbles now feature smooth rubber-band swipe-to-reply (up to 60px damping, 40px threshold haptic trigger and spring snap-back) with fading/scaling reply icon (↩). Introduced sleek 48px composer reply and edit preview banners with 3px vertical accent lines. Quoted message containers rendered in-bubble with scroll-to-original on tap and 600ms pulse animation. Upgraded long-press message selection to floating HUD with 46px top pill housing 6 animated pop-in quick reactions (❤️, 🔥, 😂, 👍, 😮, 😢) and '+' full emoji sheet button, bottom context action menu (Reply, Copy, Edit, Unsend), 24px reaction badges with user counts and tinted accent highlight for user's own reactions, and real-time MESSAGE_REACTION_TOGGLED multi-client WebSocket synchronization."
            ),
            isEssential = true,
            updatedAt = "2026-11-18T14:40:00Z"
        ),
        StoreApp(
            id = "jarvis",
            packageName = "com.omni.assistant",
            name = "JARVIS AI",
            badge = "AI Super-Assistant",
            tagline = "Multimodal voice, vision & device automation assistant",
            description = "Autonomous AI assistant powered by Gemini. Features natural conversational voice interactions, computer vision, camera intelligence, biometric security, and custom device task execution.",
            category = "Artificial Intelligence",
            versionName = "3.5.0",
            versionCode = 35,
            sizeFormatted = "161.7 MB",
            downloadUrl = "https://connecto.fun/downloads/jarvis-ai-v3.5.0.apk",
            accentColor = "#06B6D4",
            changelog = listOf(
                "Multimodal voice conversation engine",
                "Real-time visual scene analysis",
                "Biometric fingerprint & face recognition security",
                "Background task automation and scheduled triggers"
            ),
            isEssential = true,
            updatedAt = "2026-10-31T15:20:00Z"
        ),
        StoreApp(
            id = "pdfstudio",
            packageName = "com.app.pdfstudio",
            name = "PDF Studio",
            badge = "PDF Editor & Converter",
            tagline = "Change names in matching fonts, edit text & convert images to PDF",
            description = "Professional PDF editor for Android. Find & replace names in the exact same font and size, tap to edit text in-place, convert JPG/PNG/WEBP to PDF, merge multiple PDF files losslessly, and draw or annotate directly on documents.",
            category = "Productivity",
            versionName = "1.4.10",
            versionCode = 15,
            sizeFormatted = "29.4 MB",
            downloadUrl = "https://connecto.fun/downloads/pdf-studio.apk",
            accentColor = "#EF4444",
            changelog = listOf(
                "Same-Font Name & Text Replacement: Find & replace names in matching fonts",
                "Visual Tap-to-Edit: Tap any text line on the PDF page to edit information in-place",
                "Universal Image to PDF: Convert JPG, PNG, WEBP, BMP, HEIF or photos to PDF",
                "Page formatting: A4, Letter, Auto-orientation, custom margins, and compression",
                "Lossless PDF merging and interactive drawing/annotating"
            ),
            isEssential = false,
            updatedAt = "2026-10-30T11:45:00Z"
        )
    )
}
