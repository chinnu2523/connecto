package com.example.connecto

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.connecto.network.ConnectoNetworkHelper
import com.example.connecto.security.BiometricAuthManager
import com.example.connecto.security.BiometricStatus
import com.example.connecto.ui.auth.AuthHomeScreen
import com.example.connecto.ui.components.EmailGracePeriodBanner
import com.example.connecto.ui.components.EmailVerificationModal
import com.example.connecto.ui.intro.SplashScreen
import com.example.connecto.ui.navigation.ConnectoBottomBar
import com.example.connecto.ui.navigation.ConnectoTab
import com.example.connecto.ui.screens.CallsScreen
import com.example.connecto.ui.screens.ChannelsWorkspaceScreen
import com.example.connecto.ui.screens.CustomizedChatScreen
import com.example.connecto.ui.screens.HomeChatScreen
import com.example.connecto.ui.screens.ProfileScreen
import com.example.connecto.ui.security.BiometricLockScreen
import com.example.connecto.ui.theme.ConnectoTheme
import com.example.connecto.ui.theme.glassCanvasMesh
import com.example.connecto.ui.theme.liquidGlassCanvasMesh
import com.example.connecto.ui.designsystem.ThemeMode
import com.example.connecto.ui.designsystem.connectoAmbientShader
import com.example.connecto.ui.components.FullScreenCallUI
import com.example.connecto.ui.components.MinimizedCallPill
import com.example.connecto.voice.CallState
import com.example.connecto.voice.VoiceCallManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.connecto.ui.designsystem.isCoverPreset
import com.example.connecto.ui.designsystem.getCoverPreset
import com.example.connecto.ui.designsystem.sanitizeAvatarUrl

import androidx.activity.SystemBarStyle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.connecto.notifications.ConnectoNotificationManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

enum class AppFlowState {
    SPLASH,
    BIOMETRIC_LOCK, // Biometric Fingerprint & Face ID Vault Screen
    AUTH_HOME,      // Homepage with Sign Up / Sign In ONLY
    CHAT_APP        // Gamers Community Chat UI Experience
}

class MainActivity : FragmentActivity() {
    companion object {
        var pendingIntentAction by mutableStateOf<String?>(null)
        var pendingOpenChatTarget by mutableStateOf<String?>(null)
        var isServerMaintenanceOpen by mutableStateOf(false)

        fun openMaintenanceScreen() {
            isServerMaintenanceOpen = true
        }

        fun closeMaintenanceScreen() {
            isServerMaintenanceOpen = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConnectoNetworkHelper.installSecurityProvider(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            )
        } else {
            window.statusBarColor = android.graphics.Color.parseColor("#000000")
            window.navigationBarColor = android.graphics.Color.parseColor("#000000")
        }
        com.example.connecto.network.ConnectoApiClient.initSessionFromPrefs(this)
        com.example.connecto.security.ScreenSecurityManager.init(this)
        com.example.connecto.security.ScreenSecurityManager.applySecurityPolicy(this)
        com.example.connecto.security.ScreenSecurityManager.registerListeners(this)
        com.example.connecto.voice.ProximityScreenLockManager.updateActivity(this)
        // Ensure legacy Connecto Active foreground notification is removed and its channel deleted
        com.example.connecto.notifications.ConnectoRealtimeService.dismissConnectoActiveNotification(this)
        // Schedule message purge on background IO thread (not in onOpen which blocks Main thread)
        com.example.connecto.data.ConnectoDatabaseHelper.getInstance(this).schedulePurgeAsync()

        // Configure Coil image caching with memory & disk cache
        try {
            val imageLoader = coil.ImageLoader.Builder(this)
                .memoryCache {
                    coil.memory.MemoryCache.Builder(this)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    coil.disk.DiskCache.Builder()
                        .directory(cacheDir.resolve("coil_image_cache"))
                        .maxSizeBytes(50L * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build()
            coil.Coil.setImageLoader(imageLoader)
        } catch (e: Exception) {
            // ignore
        }

        handleIncomingIntent(intent)
        setContent {
            ConnectoAppWrapper()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val isInternal = intent.getBooleanExtra(ConnectoNotificationManager.EXTRA_INTERNAL_SOURCE, false)
        val action = intent.getStringExtra(ConnectoNotificationManager.EXTRA_NOTIFICATION_ACTION)
        val type = intent.getStringExtra("type")
        val target = intent.getStringExtra(ConnectoNotificationManager.EXTRA_CHANNEL_ID)
            ?: intent.getStringExtra("reference_id")
            ?: intent.getStringExtra("channel_id")
            ?: intent.getStringExtra("room_id")
            ?: intent.getStringExtra("sender_username")
            ?: intent.getStringExtra("sender")

        // SEC-A5-03 FIX: Call control actions (answering/declining calls) strictly require trusted internal notification origin
        if (action == ConnectoNotificationManager.ACTION_ANSWER_CALL || action == ConnectoNotificationManager.ACTION_DECLINE_CALL) {
            if (!isInternal) {
                return
            }
        }

        if (!action.isNullOrBlank()) {
            pendingIntentAction = action
        }
        if (action == ConnectoNotificationManager.ACTION_OPEN_CHAT || type in listOf("dm", "message", "new_dm_alert")) {
            // SEC-A6-01 FIX: Enforce trusted internal notification origin for chat navigation
            if (!isInternal) {
                return
            }
            pendingIntentAction = ConnectoNotificationManager.ACTION_OPEN_CHAT
            if (!target.isNullOrBlank()) {
                pendingOpenChatTarget = target
            }

            // SEC-A4-04 FIX: Never parse or persist message payloads into SQLite from untrusted Intent extras.
            // When opening chat, navigate directly to target channel; authenticated chat screens sync authoritative data.
        }
        // SEC-A05 FIX: Restrict test in-app banners to debug builds and verified internal notification intents
        if (BuildConfig.DEBUG && intent.getBooleanExtra(ConnectoNotificationManager.EXTRA_INTERNAL_SOURCE, false) && intent.getBooleanExtra("test_in_app_banner", false)) {
            val sender = intent.getStringExtra("sender") ?: "CyberValkyrie"
            val avatar = intent.getStringExtra("avatar") ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
            val msg = intent.getStringExtra("message") ?: "Ready for the championship match!"
            com.example.connecto.ui.components.InAppNotificationController.show(
                com.example.connecto.ui.components.InAppNotificationData(
                    title = sender,
                    message = msg,
                    avatarUrl = avatar,
                    username = sender,
                    actionType = "chat"
                )
            )
            ConnectoNotificationManager.showMessageNotification(
                senderName = sender,
                messageText = msg,
                channelOrDmId = if (sender.isNotBlank()) "dm-$sender" else "general",
                senderAvatar = avatar,
                senderUsername = sender
            )
        }
    }

    override fun onResume() {
        super.onResume()
        com.example.connecto.security.ScreenSecurityManager.applySecurityPolicy(this)
        com.example.connecto.voice.ProximityScreenLockManager.updateActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        com.example.connecto.security.ScreenSecurityManager.unregisterListeners(this)
        com.example.connecto.voice.ProximityScreenLockManager.stopMonitoring()
    }
}

fun performCompleteSignOut(context: Context, userToSignOut: String?) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            com.example.connecto.voice.VoiceCallManager.disconnectWebSocket()
            if (!userToSignOut.isNullOrBlank()) {
                com.example.connecto.network.ConnectoApiClient.setPresenceOffline(userToSignOut)
            }
        } catch (_: Exception) {}
    }
    // Clear ALL persistent data — no leftover state from previous account
    com.example.connecto.notifications.ConnectoRealtimeService.stopService(context)
    com.example.connecto.voice.VoiceCallManager.disconnectWebSocket()
    val prefsList = listOf(
        "connecto_session_prefs",
        "connecto_secure_session_prefs",
        "connecto_user_profile_prefs",
        "connecto_chat_read_prefs",
        "connecto_friend_prefs",
        "connecto_notifications_prefs",
        "connecto_notification_prefs",
        "connecto_profile"
    )
    for (name in prefsList) {
        context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().apply()
    }
    // Reset all in-memory ApiClient state
    com.example.connecto.network.ConnectoApiClient.sessionToken = null
    com.example.connecto.network.ConnectoApiClient.sessionCookie = null
    com.example.connecto.network.ConnectoApiClient.currentUsername = null
    com.example.connecto.network.ConnectoApiClient.currentUserId = null
    com.example.connecto.network.ConnectoApiClient.currentUserEmail = null
    com.example.connecto.network.ConnectoApiClient.currentUserDisplayName = null
    com.example.connecto.network.ConnectoApiClient.activeTypingUser = null
    com.example.connecto.network.ConnectoApiClient.channelCache.clear()
    // Clear local SQLite cache on sign out to guarantee zero cross-account message/channel collision
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            com.example.connecto.data.ConnectoDatabaseHelper.getInstance(context).clearAllCache()
        } catch (_: Exception) {}
    }
}

@Composable
fun ConnectoAppWrapper() {
    val context = LocalContext.current
    val sessionPrefs = remember { context.getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("connecto_user_profile_prefs", Context.MODE_PRIVATE) }

    var authenticatedUsername by remember {
        mutableStateOf(sessionPrefs.getString("username", "Alex") ?: "Alex")
    }
    val defaultThemeId = if (profilePrefs.getBoolean("dark_theme_$authenticatedUsername", true)) "noir" else "parchment"
    var currentThemeMode by remember(authenticatedUsername) {
        val savedThemeId = profilePrefs.getString("theme_mode_$authenticatedUsername", defaultThemeId) ?: defaultThemeId
        mutableStateOf(com.example.connecto.ui.designsystem.ThemeMode.fromId(savedThemeId))
    }
    var glassTransparency by remember(authenticatedUsername) {
        val saved = profilePrefs.getFloat("glass_transparency_$authenticatedUsername", 0.0f).coerceIn(0.0f, 1.0f)
        mutableStateOf(saved)
    }

    ConnectoTheme(themeMode = currentThemeMode, glassTransparency = glassTransparency) {
        ConnectoApp(
            authenticatedUsername = authenticatedUsername,
            onUsernameChange = { newUsername -> authenticatedUsername = newUsername },
            currentThemeMode = currentThemeMode,
            onSelectThemeMode = { newMode ->
                currentThemeMode = newMode
                if (newMode == com.example.connecto.ui.designsystem.ThemeMode.LIQUID_GLASS) {
                    val saved = profilePrefs.getFloat("glass_transparency_$authenticatedUsername", -1.0f)
                    if (saved in 0.0f..1.0f) {
                        glassTransparency = saved
                    } else {
                        glassTransparency = 0.0f
                        profilePrefs.edit()
                            .putFloat("glass_transparency_$authenticatedUsername", 0.0f)
                            .apply()
                    }
                }
                profilePrefs.edit()
                    .putString("theme_mode_$authenticatedUsername", newMode.id)
                    .putBoolean("dark_theme_$authenticatedUsername", newMode.isDark)
                    .apply()
            },
            isDarkTheme = currentThemeMode.isDark,
            onToggleTheme = { isDark ->
                val newMode = if (isDark) com.example.connecto.ui.designsystem.ThemeMode.NOIR else com.example.connecto.ui.designsystem.ThemeMode.PARCHMENT
                currentThemeMode = newMode
                profilePrefs.edit()
                    .putString("theme_mode_$authenticatedUsername", newMode.id)
                    .putBoolean("dark_theme_$authenticatedUsername", isDark)
                    .apply()
            },
            glassTransparency = glassTransparency,
            onChangeGlassTransparency = { newLevel ->
                val clamped = newLevel.coerceIn(0.0f, 1.0f)
                glassTransparency = clamped
                profilePrefs.edit()
                    .putFloat("glass_transparency_$authenticatedUsername", clamped)
                    .apply()
            }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ConnectoApp(
    authenticatedUsername: String,
    onUsernameChange: (String) -> Unit,
    currentThemeMode: com.example.connecto.ui.designsystem.ThemeMode = com.example.connecto.ui.designsystem.ThemeMode.NOIR,
    onSelectThemeMode: (com.example.connecto.ui.designsystem.ThemeMode) -> Unit = {},
    isDarkTheme: Boolean = true,
    onToggleTheme: (Boolean) -> Unit = {},
    glassTransparency: Float = 0.20f,
    onChangeGlassTransparency: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE) }

    val isLoggedInCached = remember { prefs.getBoolean("is_logged_in", false) }
    var flowState by rememberSaveable {
        mutableStateOf(if (isLoggedInCached) AppFlowState.CHAT_APP else AppFlowState.SPLASH)
    }
    var isBiometricOverlayActive by rememberSaveable { mutableStateOf(false) }
    val savedTabStr = remember { prefs.getString("last_active_tab", ConnectoTab.HOME.name) }
    val initialTab = remember {
        try { ConnectoTab.valueOf(savedTabStr ?: ConnectoTab.HOME.name) } catch (_: Exception) { ConnectoTab.HOME }
    }
    var currentTab by rememberSaveable { mutableStateOf(initialTab) }

    val updateCurrentTab: (ConnectoTab) -> Unit = { tab ->
        currentTab = tab
        prefs.edit().putString("last_active_tab", tab.name).apply()
    }
    var hasUnreadChats by remember { mutableStateOf(false) }
    var isNotificationCenterOpen by remember { mutableStateOf(false) }

    // App-wide screen privacy & blackscreen protection for screenshot, screenshare, and screencast
    val isScreenSecurityActive by com.example.connecto.security.ScreenSecurityManager.isSecurityEnabledState.collectAsState()
    val isRecordingActive by com.example.connecto.security.ScreenSecurityManager.isScreenRecordingActive.collectAsState()
    val isCastingActive by com.example.connecto.security.ScreenSecurityManager.isCastingActive.collectAsState()
    val isUserAdmin by com.example.connecto.network.ConnectoApiClient.currentUserIsAdminState.collectAsState()
    val mainActivityRef = context as? android.app.Activity
    androidx.compose.runtime.LaunchedEffect(isScreenSecurityActive, isUserAdmin, authenticatedUsername, currentTab) {
        com.example.connecto.security.ScreenSecurityManager.applySecurityPolicy(mainActivityRef)
    }

    // Global session revocation & 401 expiration listener
    androidx.compose.runtime.DisposableEffect(authenticatedUsername) {
        com.example.connecto.network.ConnectoApiClient.onSessionExpired = { reason ->
            (context as? android.app.Activity)?.runOnUiThread {
                if (flowState != AppFlowState.AUTH_HOME) {
                    android.widget.Toast.makeText(context, reason, android.widget.Toast.LENGTH_LONG).show()
                    performCompleteSignOut(context, authenticatedUsername)
                    flowState = AppFlowState.AUTH_HOME
                }
            }
        }
        onDispose {
            com.example.connecto.network.ConnectoApiClient.onSessionExpired = null
        }
    }

    val unreadNotifCount by com.example.connecto.network.ConnectoApiClient.unreadNotificationCount.collectAsState()
    val callState by VoiceCallManager.callState.collectAsState()
    val currentAudioRoute by VoiceCallManager.currentAudioRoute.collectAsState()
    val isProximityScreenLocked by com.example.connecto.voice.ProximityScreenLockManager.isScreenOffLocked.collectAsState()
    val isProximityNear by com.example.connecto.voice.ProximityScreenLockManager.isNear.collectAsState()
    val incomingCallData by VoiceCallManager.incomingCall.collectAsState()
    val managerCallTitle by VoiceCallManager.activeCallTitle.collectAsState()
    val managerRoomCode by VoiceCallManager.activeCallRoomCode.collectAsState()
    val managerAvatar by VoiceCallManager.activeCallAvatar.collectAsState()
    var isCallMinimized by remember { mutableStateOf(false) }

    // Auto-reset minimization when call finishes
    androidx.compose.runtime.LaunchedEffect(callState) {
        if (callState == CallState.IDLE) {
            isCallMinimized = false
        }
    }

    // Only ask Notification permission when a new user downloaded the app for the first time
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // On-demand Mic permission launcher when answering incoming call
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            VoiceCallManager.restartAudioHardwareIfConnected()
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val isMaint = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.example.connecto.network.ConnectoApiClient.isServerInMaintenance()
        }
        if (isMaint) {
            MainActivity.openMaintenanceScreen()
        }
        VoiceCallManager.init(context)
        ConnectoNotificationManager.init(context)

        // Only prompt notification permission for a new user on their very first download/launch
        val permissionPrefs = context.getSharedPreferences("connecto_permissions", Context.MODE_PRIVATE)
        val hasPromptedFirstDownloadNotif = permissionPrefs.getBoolean("has_prompted_first_download_notification", false)
        if (!hasPromptedFirstDownloadNotif) {
            permissionPrefs.edit().putBoolean("has_prompted_first_download_notification", true).apply()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    // Fetch initial profile & avatar from production backend and ensure real-time WS + service are active
    androidx.compose.runtime.LaunchedEffect(flowState, authenticatedUsername) {
        if (flowState == AppFlowState.CHAT_APP && authenticatedUsername.isNotBlank()) {
            try {
                val userId = com.example.connecto.network.ConnectoApiClient.currentUserId
                    ?: prefs.getString("user_id", authenticatedUsername)
                    ?: authenticatedUsername
                val displayName = com.example.connecto.network.ConnectoApiClient.currentUserDisplayName
                    ?: prefs.getString("display_name", authenticatedUsername)
                    ?: authenticatedUsername
                val savedToken = com.example.connecto.network.ConnectoApiClient.sessionToken
                    ?: prefs.getString("token", null)
                VoiceCallManager.updateCredentialsAndConnect(
                    username = authenticatedUsername,
                    userId = userId,
                    token = savedToken,
                    displayName = displayName
                )
                com.example.connecto.notifications.ConnectoRealtimeService.startService(context)
                com.example.connecto.network.ConnectoApiClient.getProfile(authenticatedUsername)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    com.example.connecto.network.ConnectoApiClient.getCallLogs(authenticatedUsername)
                }
            } catch (e: Exception) {
                // Ignore network error on startup
            }
        }
    }

    // Register FCM token with backend on login (token was saved by ConnectoFirebaseMessagingService.onNewToken)
    androidx.compose.runtime.LaunchedEffect(flowState, authenticatedUsername) {
        if (flowState == AppFlowState.CHAT_APP && authenticatedUsername.isNotBlank()) {
            try {
                val fcmPrefs = context.getSharedPreferences("connecto_fcm", Context.MODE_PRIVATE)
                val savedToken = fcmPrefs.getString("fcm_token", null)
                if (!savedToken.isNullOrBlank()) {
                    // Token already cached — register directly (already in a coroutine context)
                    com.example.connecto.network.ConnectoApiClient.registerFcmToken(savedToken)
                }
                // Also verify/refresh with Firebase to guarantee backend holds the latest valid token
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        if (token.isNotBlank() && token != savedToken) {
                            fcmPrefs.edit().putString("fcm_token", token).apply()
                            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                                com.example.connecto.network.ConnectoApiClient.registerFcmToken(token)
                            }
                        }
                    }
            } catch (e: Exception) {
                // FCM not configured (no google-services.json) — push skipped gracefully
            }
        }
    }

    // Periodic unread notification sync
    androidx.compose.runtime.LaunchedEffect(flowState, authenticatedUsername) {
        if (flowState == AppFlowState.CHAT_APP) {
            while (isActive) {
                com.example.connecto.network.ConnectoApiClient.getUnreadNotificationCount(authenticatedUsername)
                delay(20000L)
            }
        }
    }

    // Global real-time incoming message bus listener & badge synchronizer
    androidx.compose.runtime.LaunchedEffect(flowState, authenticatedUsername) {
        if (flowState == AppFlowState.CHAT_APP) {
            // Check local SQLite cache on startup for any unread chats
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val friends = com.example.connecto.data.ConnectoDatabaseHelper.getInstance(context).getCachedFriends()
                    val hasUnread = friends.any { it.unreadCount > 0 }
                    if (hasUnread) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            hasUnreadChats = true
                        }
                    }
                } catch (_: Exception) {}
            }

            // Listen to real-time incoming messages across tabs
            VoiceCallManager.incomingChannelMessages.collect { incoming ->
                val myU = authenticatedUsername.trim().removePrefix("@").lowercase()
                val authorU = incoming.authorId.trim().removePrefix("@").lowercase()
                if (authorU.isNotBlank() && authorU != myU) {
                    hasUnreadChats = true
                }
            }
        }
    }

    // Automated Server Maintenance Detection
    val isWsConnectedGlobal by VoiceCallManager.isWsConnectedFlow.collectAsState()

    androidx.compose.runtime.LaunchedEffect(isWsConnectedGlobal, flowState) {
        if (!isWsConnectedGlobal) {
            delay(3500L)
            if (!isWsConnectedGlobal) {
                val isMaint = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.connecto.network.ConnectoApiClient.isServerInMaintenance()
                }
                if (isMaint) {
                    MainActivity.openMaintenanceScreen()
                }
            }
        } else {
            if (MainActivity.isServerMaintenanceOpen) {
                MainActivity.closeMaintenanceScreen()
            }
        }
    }

    // Handle deep link / system notification action triggers
    androidx.compose.runtime.LaunchedEffect(MainActivity.pendingIntentAction) {
        val action = MainActivity.pendingIntentAction ?: return@LaunchedEffect
        when (action) {
            ConnectoNotificationManager.ACTION_ANSWER_CALL -> {
                VoiceCallManager.acceptIncomingCall()
                if (currentTab != ConnectoTab.CALLS && currentTab != ConnectoTab.MESSAGES) {
                    updateCurrentTab(ConnectoTab.MESSAGES)
                }
            }
            ConnectoNotificationManager.ACTION_DECLINE_CALL -> {
                VoiceCallManager.declineIncomingCall()
            }
            ConnectoNotificationManager.ACTION_OPEN_CHAT -> {
                updateCurrentTab(ConnectoTab.MESSAGES)
            }
            ConnectoNotificationManager.ACTION_OPEN_NOTIFICATIONS -> {
                isNotificationCenterOpen = true
            }
        }
        MainActivity.pendingIntentAction = null
    }

    // Keep real-time WebSocket & background service alive when backgrounded; auto-lock on return if enabled
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, authenticatedUsername) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val isLoggedIn = prefs.getBoolean("is_logged_in", false)
                if (isLoggedIn) {
                    com.example.connecto.voice.VoiceCallManager.connectWebSocket()
                }
            } else if (event == Lifecycle.Event.ON_START) {
                val isLoggedIn = prefs.getBoolean("is_logged_in", false)
                if (isLoggedIn && flowState == AppFlowState.CHAT_APP) {
                    com.example.connecto.notifications.ConnectoRealtimeService.startService(context)
                    com.example.connecto.voice.VoiceCallManager.connectWebSocket()
                    val profilePrefs = context.getSharedPreferences("connecto_user_profile_prefs", Context.MODE_PRIVATE)
                    val isLockEnabled = profilePrefs.getBoolean("biometric_lock_$authenticatedUsername", false)
                    val autoLock = profilePrefs.getBoolean("auto_lock_background_$authenticatedUsername", true)
                    if (isLockEnabled && autoLock) {
                        isBiometricOverlayActive = true
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (currentThemeMode.isLiquidGlass || currentThemeMode.isGlass) {
                    Modifier.connectoAmbientShader(themeMode = currentThemeMode)
                } else if (currentThemeMode == ThemeMode.NOIR) {
                    Modifier.connectoAmbientShader(themeMode = currentThemeMode, intensity = 0.45f)
                } else {
                    Modifier.background(MaterialTheme.colorScheme.background)
                }
            )
    ) {

        Crossfade(
            targetState = flowState,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            label = "flowCrossfade"
        ) { state ->
            when (state) {
                AppFlowState.SPLASH -> {
                    SplashScreen(
                        onSplashFinished = {
                            val isLoggedIn = prefs.getBoolean("is_logged_in", false)
                            val savedUsername = prefs.getString("username", null)
                            val savedToken = prefs.getString("token", null)

                            if (isLoggedIn && !savedUsername.isNullOrEmpty()) {
                                val savedUserId = prefs.getString("user_id", null)
                                val savedEmail = prefs.getString("email", null)
                                val savedDisplayName = prefs.getString("display_name", null)
                                com.example.connecto.network.ConnectoApiClient.sessionToken = savedToken
                                com.example.connecto.network.ConnectoApiClient.currentUserId = savedUserId
                                com.example.connecto.network.ConnectoApiClient.currentUsername = savedUsername
                                com.example.connecto.network.ConnectoApiClient.currentUserEmail = savedEmail
                                com.example.connecto.network.ConnectoApiClient.currentUserDisplayName = savedDisplayName
                                onUsernameChange(savedUsername)
                                VoiceCallManager.updateCredentialsAndConnect(savedUsername, savedUserId, savedToken)
                                val profilePrefs = context.getSharedPreferences("connecto_user_profile_prefs", Context.MODE_PRIVATE)
                                val isBiometricEnabled = profilePrefs.getBoolean("biometric_lock_$savedUsername", false)
                                val autoLock = profilePrefs.getBoolean("auto_lock_background_$savedUsername", true)

                                // Enforce lock screen on launch only when biometric lock is enabled AND auto-lock in background is enabled
                                flowState = AppFlowState.CHAT_APP
                                if (isBiometricEnabled && autoLock) {
                                    isBiometricOverlayActive = true
                                }
                            } else {
                                flowState = AppFlowState.AUTH_HOME
                            }
                        }
                    )
                }

                AppFlowState.BIOMETRIC_LOCK -> {
                    // BIOMETRIC FINGERPRINT & FACE ID VAULT LOCK SCREEN
                    BiometricLockScreen(
                        username = authenticatedUsername,
                        onUnlockSuccess = {
                            isBiometricOverlayActive = false
                            flowState = AppFlowState.CHAT_APP
                        },
                        onPasswordUnlock = {
                            isBiometricOverlayActive = false
                            flowState = AppFlowState.CHAT_APP
                        },
                        onSwitchAccount = {
                            isBiometricOverlayActive = false
                            performCompleteSignOut(context, authenticatedUsername)
                            flowState = AppFlowState.AUTH_HOME
                        }
                    )
                }

                AppFlowState.AUTH_HOME -> {
                    // HOMEPAGE WITH SIGN UP & SIGN IN ONLY (NO OTHER TABS SHOWN)
                    AuthHomeScreen(
                        onAuthSuccess = { username ->
                            // Persist user session
                            prefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putBoolean("is_admin", com.example.connecto.network.ConnectoApiClient.currentUserIsAdmin)
                                .putString("username", username)
                                .putString("token", com.example.connecto.network.ConnectoApiClient.sessionToken)
                                .putString("user_id", com.example.connecto.network.ConnectoApiClient.currentUserId)
                                .putString("email", com.example.connecto.network.ConnectoApiClient.currentUserEmail)
                                .putString("display_name", com.example.connecto.network.ConnectoApiClient.currentUserDisplayName)
                                .apply()

                            com.example.connecto.network.ConnectoApiClient.channelCache.clear()
                            onUsernameChange(username)
                            VoiceCallManager.updateCredentialsAndConnect(
                                username,
                                com.example.connecto.network.ConnectoApiClient.currentUserId,
                                com.example.connecto.network.ConnectoApiClient.sessionToken
                            )

                            // Direct entry to community chat platform
                            flowState = AppFlowState.CHAT_APP
                        }
                    )
                }

                AppFlowState.CHAT_APP -> {
                    // GAMERS COMMUNITY CHAT UI EXPERIENCE WITH PERSISTENT NON-OVERLAPPING TABS
                    var isDirectChatOpen by remember { mutableStateOf(false) }
                    val savedChannel = remember { prefs.getString("last_active_channel", "general") ?: "general" }
                    var selectedChannelForWorkspace by rememberSaveable { mutableStateOf(savedChannel) }
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val isImeVisible = androidx.compose.foundation.layout.WindowInsets.ime.getBottom(density) > 100

                    val emailVerification by com.example.connecto.network.ConnectoApiClient.emailVerificationState.collectAsState()
                    var showManualVerifyModal by rememberSaveable { mutableStateOf(false) }

                    val showBottomBar = !(currentTab == ConnectoTab.MESSAGES && isDirectChatOpen) && !isImeVisible

                    val activity = context as? android.app.Activity
                    androidx.activity.compose.BackHandler(enabled = true) {
                        if (currentTab == ConnectoTab.CHANNELS) {
                            // When the user in the channel tab and uses the back button -> directly get to the home tab
                            updateCurrentTab(ConnectoTab.HOME)
                        } else if (currentTab == ConnectoTab.HOME) {
                            // When user is in the home tab and presses back button again -> app closes
                            activity?.finish()
                        } else {
                            // From other top-level tabs, navigate back to Home tab
                            updateCurrentTab(ConnectoTab.HOME)
                        }
                    }

                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            if (showBottomBar) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                ) {
                                    // Minimized Call Floating Capsule Dock
                                    val isCallOngoing = (callState == CallState.CONNECTED || callState == CallState.OUTGOING_RINGING)
                                    if (isCallMinimized && isCallOngoing) {
                                        MinimizedCallPill(
                                            callTitle = if (managerCallTitle.isNotBlank()) managerCallTitle else "Voice Call",
                                            avatarUrl = null,
                                            onExpand = { isCallMinimized = false },
                                            onEndCall = {
                                                isCallMinimized = false
                                                VoiceCallManager.endCall()
                                            }
                                        )
                                    }

                                    ConnectoBottomBar(
                                        currentTab = currentTab,
                                        onTabSelected = { selectedTab ->
                                            updateCurrentTab(selectedTab)
                                        },
                                        hasUnreadChats = hasUnreadChats,
                                        hasUnreadCalls = false,
                                        isCallActive = isCallOngoing,
                                        unreadMessagesCount = unreadNotifCount,
                                        userInitial = authenticatedUsername.trim().firstOrNull()?.uppercase() ?: "U"
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = {
                                    val isForward = targetState.ordinal > initialState.ordinal
                                    val slideSpring = spring<androidx.compose.ui.unit.IntOffset>(
                                        stiffness = 380f,
                                        dampingRatio = 0.82f
                                    )
                                    val scaleSpring = spring<Float>(
                                        stiffness = 380f,
                                        dampingRatio = 0.82f
                                    )
                                    (slideInHorizontally(
                                        animationSpec = slideSpring,
                                        initialOffsetX = { fullWidth -> if (isForward) (fullWidth * 0.22f).toInt() else (-fullWidth * 0.22f).toInt() }
                                    ) + scaleIn(
                                        initialScale = 0.94f,
                                        animationSpec = scaleSpring
                                    ) + fadeIn(
                                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                                    )).togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = slideSpring,
                                            targetOffsetX = { fullWidth -> if (isForward) (-fullWidth * 0.22f).toInt() else (fullWidth * 0.22f).toInt() }
                                        ) + scaleOut(
                                            targetScale = 0.96f,
                                            animationSpec = scaleSpring
                                        ) + fadeOut(
                                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                                        )
                                    )
                                },
                                label = "tabAnimatedContent",
                                modifier = Modifier.fillMaxSize()
                            ) { tab ->
                                when (tab) {
                                    ConnectoTab.HOME -> {
                                        HomeChatScreen(
                                            currentUsername = authenticatedUsername,
                                            onNavigateToMessages = { updateCurrentTab(ConnectoTab.MESSAGES) },
                                            onNavigateToProfile = { updateCurrentTab(ConnectoTab.PROFILE) },
                                            onNavigateToCalls = { updateCurrentTab(ConnectoTab.CALLS) },
                                            onOpenNotifications = { isNotificationCenterOpen = true },
                                            unreadNotificationsCount = unreadNotifCount,
                                            onNavigateToChannel = { channelName ->
                                                selectedChannelForWorkspace = channelName
                                                prefs.edit().putString("last_active_channel", channelName).apply()
                                                updateCurrentTab(ConnectoTab.CHANNELS)
                                            }
                                        )
                                    }
                                    ConnectoTab.MESSAGES -> {
                                        CustomizedChatScreen(
                                            currentUsername = authenticatedUsername,
                                            onOpenProfile = { updateCurrentTab(ConnectoTab.PROFILE) },
                                            onUnreadStatusChanged = { hasRemainingUnread ->
                                                hasUnreadChats = hasRemainingUnread
                                            },
                                            onDirectChatStateChanged = { isOpen ->
                                                isDirectChatOpen = isOpen
                                            },
                                            onOpenNotifications = { isNotificationCenterOpen = true },
                                            unreadNotificationsCount = unreadNotifCount,
                                            targetChatUserOrChannel = MainActivity.pendingOpenChatTarget,
                                            onTargetChatHandled = {
                                                MainActivity.pendingOpenChatTarget = null
                                            }
                                        )
                                    }
                                    ConnectoTab.CHANNELS -> {
                                        ChannelsWorkspaceScreen(
                                            currentUsername = authenticatedUsername,
                                            initialChannelId = selectedChannelForWorkspace,
                                            onOpenProfile = { updateCurrentTab(ConnectoTab.PROFILE) },
                                            onOpenNotifications = { isNotificationCenterOpen = true },
                                            unreadNotificationsCount = unreadNotifCount,
                                            onNavigateToCalls = { updateCurrentTab(ConnectoTab.CALLS) }
                                        )
                                    }
                                    ConnectoTab.CALLS -> {
                                        CallsScreen(
                                            currentUsername = authenticatedUsername,
                                            onOpenProfile = { updateCurrentTab(ConnectoTab.PROFILE) },
                                            onOpenNotifications = { isNotificationCenterOpen = true },
                                            unreadNotificationsCount = unreadNotifCount
                                        )
                                    }
                                    ConnectoTab.PROFILE -> {
                                        ProfileScreen(
                                            username = authenticatedUsername,
                                            currentThemeMode = currentThemeMode,
                                            onSelectThemeMode = onSelectThemeMode,
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = onToggleTheme,
                                            glassTransparency = glassTransparency,
                                            onChangeGlassTransparency = onChangeGlassTransparency,
                                            onLockApp = { isBiometricOverlayActive = true },
                                            onOpenNotifications = { isNotificationCenterOpen = true },
                                            unreadNotificationsCount = unreadNotifCount,
                                            onSignOut = {
                                                performCompleteSignOut(context, authenticatedUsername)
                                                flowState = AppFlowState.AUTH_HOME
                                            },
                                            onDeleteAccount = {
                                                performCompleteSignOut(context, authenticatedUsername)
                                                flowState = AppFlowState.AUTH_HOME
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Your account and all associated data have been permanently erased from live database and all backups.",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Modal for manual verification
                        if (showManualVerifyModal) {
                            EmailVerificationModal(
                                isBlocked = false,
                                daysLeft = null,
                                onDismiss = { showManualVerifyModal = false }
                            )
                        }
                    }
                }
            }
        }

        // Notification Center Bottom Sheet displaying profile avatars
        if (isNotificationCenterOpen) {
            com.example.connecto.ui.components.NotificationCenterSheet(
                isOpen = isNotificationCenterOpen,
                onDismiss = { isNotificationCenterOpen = false },
                onNavigateToChat = { channelOrDmId ->
                    isNotificationCenterOpen = false
                    updateCurrentTab(ConnectoTab.MESSAGES)
                }
            )
        }

        // Global In-App Notification Banner displaying user's circular profile picture and incoming call heads-up popup
        com.example.connecto.ui.components.InAppNotificationBanner(
            onNavigate = { actionType, refId ->
                if (actionType == "chat") {
                    updateCurrentTab(ConnectoTab.MESSAGES)
                } else if (actionType == "call") {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    VoiceCallManager.acceptIncomingCall()
                }
            }
        )

        // Global Full-Screen Audio Call Overlay (Only when CONNECTED or OUTGOING_RINGING, never during INCOMING_RINGING)
        val isCallActiveGlobal = (callState == CallState.CONNECTED || callState == CallState.OUTGOING_RINGING)
        androidx.compose.animation.AnimatedVisibility(
            visible = isCallActiveGlobal && !isCallMinimized,
            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(150)) +
                    androidx.compose.animation.slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight / 6 },
                        animationSpec = androidx.compose.animation.core.tween(150)
                    ),
            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) +
                    androidx.compose.animation.slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight / 6 },
                        animationSpec = androidx.compose.animation.core.tween(150)
                    ),
            modifier = Modifier.fillMaxSize()
        ) {
            FullScreenCallUI(
                callTitle = if (managerCallTitle.isNotBlank()) managerCallTitle else "Voice Call",
                avatarUrl = managerAvatar,
                participantCount = 2,
                roomCode = managerRoomCode,
                onMinimize = {
                    isCallMinimized = true
                },
                onEndCall = {
                    isCallMinimized = false
                    VoiceCallManager.endCall()
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Global Server Maintenance Screen Overlay
        androidx.compose.animation.AnimatedVisibility(
            visible = MainActivity.isServerMaintenanceOpen,
            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                    androidx.compose.animation.scaleIn(initialScale = 0.95f, animationSpec = androidx.compose.animation.core.tween(300)),
            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250)) +
                    androidx.compose.animation.scaleOut(targetScale = 0.95f, animationSpec = androidx.compose.animation.core.tween(250)),
            modifier = Modifier.fillMaxSize()
        ) {
            com.example.connecto.ui.screens.ServerMaintenanceScreen(
                onServerRestored = {
                    MainActivity.closeMaintenanceScreen()
                }
            )
        }

        // Global Biometric & App Lock Overlay: Keeps all chat screens, active calls, and 2FA dialogs intact underneath
        if (isBiometricOverlayActive && flowState == AppFlowState.CHAT_APP) {
            BiometricLockScreen(
                username = authenticatedUsername,
                onUnlockSuccess = {
                    isBiometricOverlayActive = false
                },
                onPasswordUnlock = {
                    isBiometricOverlayActive = false
                },
                onSwitchAccount = {
                    isBiometricOverlayActive = false
                    performCompleteSignOut(context, authenticatedUsername)
                    flowState = AppFlowState.AUTH_HOME
                }
            )
        }

        // In-Call Proximity Screen-Off Lock:
        // When audio is on the earpiece and the top receiver / proximity sensor is covered (phone held to ear),
        // the screen is completely turned off (pitch black) and touch input is locked just like native phone calls.
        val isCallActive = (callState == CallState.CONNECTED || callState == CallState.OUTGOING_RINGING)
        val shouldBlackoutForEar = (isProximityScreenLocked || isProximityNear) &&
                currentAudioRoute == com.example.connecto.voice.AudioRoute.EARPIECE &&
                isCallActive

        if (shouldBlackoutForEar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    }
            )
        }

    }
}
}

