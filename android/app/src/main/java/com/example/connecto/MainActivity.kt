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
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.connecto.network.ConnectoNetworkHelper
import com.example.connecto.security.BiometricAuthManager
import com.example.connecto.security.BiometricStatus
import com.example.connecto.ui.auth.AuthHomeScreen
import com.example.connecto.ui.components.IncomingCallDialog
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
import com.example.connecto.ui.components.FullScreenCallUI
import com.example.connecto.ui.components.MinimizedCallPill
import com.example.connecto.voice.CallState
import com.example.connecto.voice.VoiceCallManager

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
        val action = intent.getStringExtra(ConnectoNotificationManager.EXTRA_NOTIFICATION_ACTION)
        val type = intent.getStringExtra("type")
        val target = intent.getStringExtra(ConnectoNotificationManager.EXTRA_CHANNEL_ID)
            ?: intent.getStringExtra("reference_id")
            ?: intent.getStringExtra("channel_id")
            ?: intent.getStringExtra("room_id")
            ?: intent.getStringExtra("sender_username")
            ?: intent.getStringExtra("sender")

        if (!action.isNullOrBlank()) {
            pendingIntentAction = action
        }
        if (action == ConnectoNotificationManager.ACTION_OPEN_CHAT || type in listOf("dm", "message", "new_dm_alert")) {
            pendingIntentAction = ConnectoNotificationManager.ACTION_OPEN_CHAT
            if (!target.isNullOrBlank()) {
                pendingOpenChatTarget = target
            }
        }
        if (intent.getBooleanExtra("test_in_app_banner", false)) {
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
                channelOrDmId = "general",
                senderAvatar = avatar
            )
        }
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

    ConnectoTheme(themeMode = currentThemeMode) {
        ConnectoApp(
            authenticatedUsername = authenticatedUsername,
            onUsernameChange = { newUsername -> authenticatedUsername = newUsername },
            currentThemeMode = currentThemeMode,
            onSelectThemeMode = { newMode ->
                currentThemeMode = newMode
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
    onToggleTheme: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE) }

    val isLoggedInCached = remember { prefs.getBoolean("is_logged_in", false) }
    var flowState by rememberSaveable {
        mutableStateOf(if (isLoggedInCached) AppFlowState.CHAT_APP else AppFlowState.SPLASH)
    }
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

    val unreadNotifCount by com.example.connecto.network.ConnectoApiClient.unreadNotificationCount.collectAsState()
    val callState by VoiceCallManager.callState.collectAsState()
    val incomingCallData by VoiceCallManager.incomingCall.collectAsState()
    val managerCallTitle by VoiceCallManager.activeCallTitle.collectAsState()
    val managerRoomCode by VoiceCallManager.activeCallRoomCode.collectAsState()
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
        val isHealthy = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.example.connecto.network.ConnectoApiClient.checkHealth()
        }
        if (!isHealthy) {
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

    // Fetch initial profile & avatar from production backend
    androidx.compose.runtime.LaunchedEffect(flowState, authenticatedUsername) {
        if (flowState == AppFlowState.CHAT_APP && authenticatedUsername.isNotBlank()) {
            try {
                com.example.connecto.network.ConnectoApiClient.getProfile(authenticatedUsername)
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
                } else {
                    // Fetch token from Firebase asynchronously, then register with backend
                    suspendCancellableCoroutine<Unit> { cont ->
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                            .addOnSuccessListener { token ->
                                if (token.isNotBlank()) {
                                    fcmPrefs.edit().putString("fcm_token", token).apply()
                                    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                                        com.example.connecto.network.ConnectoApiClient.registerFcmToken(token)
                                    }
                                }
                                if (cont.isActive) cont.resume(Unit) {}
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(Unit) {}
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

    // Automated Server Maintenance Detection
    val isWsConnectedGlobal by VoiceCallManager.isWsConnectedFlow.collectAsState()

    androidx.compose.runtime.LaunchedEffect(isWsConnectedGlobal, flowState) {
        if (!isWsConnectedGlobal) {
            delay(2500L)
            if (!isWsConnectedGlobal) {
                val healthy = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.connecto.network.ConnectoApiClient.checkHealth()
                }
                if (!healthy) {
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

    // Auto-lock when returning from background if app lock is enabled
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, authenticatedUsername) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val user = authenticatedUsername
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        com.example.connecto.voice.VoiceCallManager.disconnectWebSocket()
                        if (user.isNotBlank()) {
                            com.example.connecto.network.ConnectoApiClient.setPresenceOffline(user)
                        }
                    } catch (_: Exception) {}
                }
            } else if (event == Lifecycle.Event.ON_START) {
                val isLoggedIn = prefs.getBoolean("is_logged_in", false)
                if (isLoggedIn && flowState == AppFlowState.CHAT_APP) {
                    com.example.connecto.voice.VoiceCallManager.connectWebSocket()
                    val profilePrefs = context.getSharedPreferences("connecto_user_profile_prefs", Context.MODE_PRIVATE)
                    val isLockEnabled = profilePrefs.getBoolean("biometric_lock_$authenticatedUsername", false)
                    val autoLock = profilePrefs.getBoolean("auto_lock_background_$authenticatedUsername", true)
                    if (isLockEnabled && autoLock) {
                        flowState = AppFlowState.BIOMETRIC_LOCK
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
            .background(MaterialTheme.colorScheme.background)
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
                                if (isBiometricEnabled && autoLock) {
                                    flowState = AppFlowState.BIOMETRIC_LOCK
                                } else {
                                    flowState = AppFlowState.CHAT_APP
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
                            flowState = AppFlowState.CHAT_APP
                        },
                        onPasswordUnlock = {
                            flowState = AppFlowState.CHAT_APP
                        },
                        onSwitchAccount = {
                            // Explicitly clear session when user chooses to switch account or sign out
                            prefs.edit().clear().apply()
                            com.example.connecto.network.ConnectoApiClient.sessionToken = null
                            com.example.connecto.network.ConnectoApiClient.currentUsername = null
                            com.example.connecto.network.ConnectoApiClient.currentUserId = null
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
                                .putString("username", username)
                                .putString("token", com.example.connecto.network.ConnectoApiClient.sessionToken)
                                .putString("user_id", com.example.connecto.network.ConnectoApiClient.currentUserId)
                                .putString("email", com.example.connecto.network.ConnectoApiClient.currentUserEmail)
                                .putString("display_name", com.example.connecto.network.ConnectoApiClient.currentUserDisplayName)
                                .apply()

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
                        // Move task to background without killing activity so it continues where left off
                        activity?.moveTaskToBack(true)
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
                                    if (isCallMinimized && callState != CallState.IDLE) {
                                        MinimizedCallPill(
                                            callTitle = if (managerCallTitle.isNotBlank()) managerCallTitle else "Voice Call",
                                            avatarUrl = null,
                                            onExpand = { isCallMinimized = false },
                                            onEndCall = {
                                                isCallMinimized = false
                                                VoiceCallManager.leaveVoiceRoom()
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
                                        isCallActive = callState != CallState.IDLE,
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
                            // Top Grace Period Banner if email is not verified and account is not blocked
                            if (!emailVerification.isEmailVerified && !emailVerification.isTemporarilyBlocked) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    EmailGracePeriodBanner(
                                        daysLeft = emailVerification.verificationDaysLeft,
                                        onVerifyClick = { showManualVerifyModal = true }
                                    )
                                }
                            }

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
                                            onLockApp = { flowState = AppFlowState.BIOMETRIC_LOCK },
                                            onOpenNotifications = { isNotificationCenterOpen = true },
                                            unreadNotificationsCount = unreadNotifCount,
                                            onSignOut = {
                                                val userToSignOut = authenticatedUsername
                                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                                    try {
                                                        com.example.connecto.voice.VoiceCallManager.disconnectWebSocket()
                                                        if (userToSignOut.isNotBlank()) {
                                                            com.example.connecto.network.ConnectoApiClient.setPresenceOffline(userToSignOut)
                                                        }
                                                    } catch (_: Exception) {}
                                                }
                                                // Clear ALL persistent data — no leftover state from previous account
                                                context.getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE)
                                                    .edit().clear().apply()
                                                context.getSharedPreferences("connecto_user_profile_prefs", Context.MODE_PRIVATE)
                                                    .edit().clear().apply()
                                                context.getSharedPreferences("connecto_chat_read_prefs", Context.MODE_PRIVATE)
                                                    .edit().clear().apply()
                                                // Reset all in-memory ApiClient state
                                                com.example.connecto.network.ConnectoApiClient.sessionToken = null
                                                com.example.connecto.network.ConnectoApiClient.sessionCookie = null
                                                com.example.connecto.network.ConnectoApiClient.currentUsername = null
                                                com.example.connecto.network.ConnectoApiClient.currentUserId = null
                                                com.example.connecto.network.ConnectoApiClient.currentUserEmail = null
                                                com.example.connecto.network.ConnectoApiClient.currentUserDisplayName = null
                                                com.example.connecto.network.ConnectoApiClient.activeTypingUser = null
                                                flowState = AppFlowState.AUTH_HOME
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Modal for manual verification or account blocked
                        if (emailVerification.isTemporarilyBlocked || showManualVerifyModal) {
                            EmailVerificationModal(
                                isBlocked = emailVerification.isTemporarilyBlocked,
                                daysLeft = emailVerification.verificationDaysLeft,
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

        // Global In-App Notification Banner displaying user's circular profile picture
        com.example.connecto.ui.components.InAppNotificationBanner(
            onNavigate = { actionType, refId ->
                if (actionType == "chat") {
                    updateCurrentTab(ConnectoTab.MESSAGES)
                }
            }
        )

        // Global incoming call alert dialog
        if (callState == CallState.INCOMING_RINGING && incomingCallData != null) {
            IncomingCallDialog(
                callerName = incomingCallData!!.callerName,
                callerUsername = incomingCallData!!.callerUsername,
                callerAvatar = incomingCallData!!.callerAvatar,
                onAccept = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    VoiceCallManager.acceptIncomingCall()
                    if (currentTab != ConnectoTab.MESSAGES && currentTab != ConnectoTab.CALLS) {
                        updateCurrentTab(ConnectoTab.MESSAGES)
                    }
                },
                onDecline = {
                    VoiceCallManager.declineIncomingCall()
                }
            )
        }

        // Global Full-Screen Audio Call Overlay
        val isCallActiveGlobal = callState != CallState.IDLE
        androidx.compose.animation.AnimatedVisibility(
            visible = isCallActiveGlobal && !isCallMinimized,
            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                    androidx.compose.animation.slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight / 4 },
                        animationSpec = androidx.compose.animation.core.tween(300)
                    ),
            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(250)) +
                    androidx.compose.animation.slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight / 4 },
                        animationSpec = androidx.compose.animation.core.tween(250)
                    ),
            modifier = Modifier.fillMaxSize()
        ) {
            FullScreenCallUI(
                callTitle = if (managerCallTitle.isNotBlank()) managerCallTitle else "Voice Call",
                participantCount = 2,
                roomCode = managerRoomCode,
                onMinimize = {
                    isCallMinimized = true
                },
                onEndCall = {
                    isCallMinimized = false
                    VoiceCallManager.leaveVoiceRoom()
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

    }
}
}

