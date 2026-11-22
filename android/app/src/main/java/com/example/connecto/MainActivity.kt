package com.example.connecto

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseInOutCubic
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
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
import com.example.connecto.ui.components.FullScreenGroupCallUI
import com.example.connecto.ui.components.MinimizedCallPill
import com.example.connecto.ui.components.MinimizedMultitaskingCallBar
import com.example.connecto.ui.components.IncomingGroupCallUI
import com.example.connecto.ui.components.ModeAGroupCallBanner
import com.example.connecto.voice.CallState
import com.example.connecto.voice.VoiceCallManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.zIndex
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
        try {
            ConnectoNetworkHelper.installSecurityProvider(this)
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "installSecurityProvider failed: ${t.message}")
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                )
            } else {
                window.statusBarColor = android.graphics.Color.parseColor("#000000")
                window.navigationBarColor = android.graphics.Color.parseColor("#000000")
            }
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "enableEdgeToEdge failed: ${t.message}")
        }
        // Unlock native high refresh rate (90Hz / 120Hz) on supported displays
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val currentDisplay = display
                val modes = currentDisplay?.supportedModes ?: emptyArray()
                val maxRefreshMode = modes.maxByOrNull { it.refreshRate }
                if (maxRefreshMode != null && maxRefreshMode.refreshRate > 60f) {
                    val lp = window.attributes
                    lp.preferredDisplayModeId = maxRefreshMode.modeId
                    window.attributes = lp
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val currentDisplay = windowManager.defaultDisplay
                val modes = currentDisplay?.supportedModes ?: emptyArray()
                val maxRefreshMode = modes.maxByOrNull { it.refreshRate }
                if (maxRefreshMode != null && maxRefreshMode.refreshRate > 60f) {
                    val lp = window.attributes
                    lp.preferredDisplayModeId = maxRefreshMode.modeId
                    window.attributes = lp
                }
            }
        } catch (_: Throwable) {}
        try {
            com.example.connecto.network.ConnectoApiClient.initSessionFromPrefs(this)
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "initSessionFromPrefs failed: ${t.message}")
        }
        try {
            com.example.connecto.security.ScreenSecurityManager.init(this)
            com.example.connecto.security.ScreenSecurityManager.applySecurityPolicy(this)
            com.example.connecto.security.ScreenSecurityManager.registerListeners(this)
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "ScreenSecurityManager failed: ${t.message}")
        }
        try {
            com.example.connecto.voice.ProximityScreenLockManager.updateActivity(this)
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "updateActivity failed: ${t.message}")
        }
        try {
            handleIncomingIntent(intent)
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "handleIncomingIntent failed: ${t.message}")
        }

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
                ConnectoNotificationManager.cancelConversationNotification(target)
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

    override fun onStart() {
        super.onStart()
        try {
            ConnectoNotificationManager.isAppInForeground = true
            com.example.connecto.voice.VoiceCallManager.onAppForegrounded()
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "onStart presence error: ${t.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            ConnectoNotificationManager.isAppInForeground = true
            com.example.connecto.security.ScreenSecurityManager.applySecurityPolicy(this)
            com.example.connecto.voice.ProximityScreenLockManager.updateActivity(this)
            if (com.example.connecto.voice.VoiceCallManager.callState.value != com.example.connecto.voice.CallState.CONNECTED) {
                com.example.connecto.voice.ProximityScreenLockManager.applyScreenState(near = false)
            }
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "onResume error: ${t.message}")
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        try {
            ConnectoNotificationManager.isAppInForeground = false
            if (com.example.connecto.voice.VoiceCallManager.callState.value != com.example.connecto.voice.CallState.CONNECTED) {
                com.example.connecto.voice.ProximityScreenLockManager.stopMonitoring()
            }
            com.example.connecto.voice.VoiceCallManager.onAppBackgrounded()
            com.example.connecto.security.SecretVaultManager.lockVault()
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "onStop presence error: ${t.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            com.example.connecto.security.ScreenSecurityManager.unregisterListeners(this)
            com.example.connecto.voice.ProximityScreenLockManager.stopMonitoring()
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "onDestroy error: ${t.message}")
        }
    }
}

fun performCompleteSignOut(context: Context, userToSignOut: String?, isAccountDeletion: Boolean = false) {
    val normUser = userToSignOut?.trim()?.lowercase()?.removePrefix("@")
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            com.example.connecto.voice.VoiceCallManager.disconnectWebSocket()
            if (!normUser.isNullOrBlank()) {
                com.example.connecto.network.ConnectoApiClient.setPresenceOffline(normUser)
            }
        } catch (_: Exception) {}
    }
    // Stop background service and disconnect voice
    com.example.connecto.notifications.ConnectoRealtimeService.stopService(context)
    com.example.connecto.voice.VoiceCallManager.disconnectWebSocket()

    // Sign out session state while preserving persistent cache for instantaneous re-login
    val sessionPrefs = context.getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE)
    sessionPrefs.edit()
        .putBoolean("is_logged_in", false)
        .remove("token")
        .apply()

    try {
        val secPrefs = com.example.connecto.security.SecureStorageManager.getEncryptedPrefs(context)
        secPrefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("token")
            .apply()
    } catch (_: Exception) {}

    // Store last signed-out user so re-login by the same user retains local SQLite cache for 0ms startup
    val metaPrefs = context.getSharedPreferences("connecto_auth_meta", Context.MODE_PRIVATE)
    if (!normUser.isNullOrBlank()) {
        metaPrefs.edit().putString("last_signed_out_user", normUser).apply()
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

    // Clear local SQLite cache and user preferences ONLY on explicit account deletion
    if (isAccountDeletion) {
        val prefsList = listOf(
            "connecto_session_prefs",
            "connecto_secure_session_prefs",
            "connecto_user_profile_prefs",
            "connecto_chat_read_prefs",
            "connecto_friend_prefs",
            "connecto_notifications_prefs",
            "connecto_notification_prefs",
            "connecto_profile",
            "connecto_auth_meta"
        )
        for (name in prefsList) {
            context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().apply()
        }
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                com.example.connecto.data.ConnectoDatabaseHelper.getInstance(context).clearAllCache()
            } catch (_: Exception) {}
        }
    }
}

@Composable
fun CrashLogBanner() {
    val context = LocalContext.current
    val crashPrefs = remember {
        try {
            context.getSharedPreferences("connecto_crash_prefs", Context.MODE_PRIVATE)
        } catch (_: Throwable) {
            null
        }
    }
    var crashLog by remember {
        mutableStateOf(
            try {
                crashPrefs?.getString("last_crash_log", null)
            } catch (_: Throwable) {
                null
            }
        )
    }

    if (!crashLog.isNullOrBlank()) {
        val nonNullLog = crashLog!!
        var showFullDetails by remember { mutableStateOf(false) }

        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            color = Color(0xFF2A1010),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.6f)),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Text(
                        text = "⚠️ App Crash Recovered",
                        color = Color(0xFFFF8A80),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.material3.TextButton(
                        onClick = { showFullDetails = !showFullDetails },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        androidx.compose.material3.Text(
                            text = if (showFullDetails) "Hide" else "Details",
                            color = Color(0xFFFFCCBC),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val firstLine = nonNullLog.lines().firstOrNull() ?: nonNullLog
                androidx.compose.material3.Text(
                    text = firstLine,
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (showFullDetails) 12 else 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (showFullDetails) {
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.material3.Surface(
                        color = Color(0xFF140808),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Text(
                            text = nonNullLog,
                            color = Color(0xFFFFCDD2),
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            try {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Connecto Crash Log", nonNullLog)
                                clipboard?.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Crash log copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            } catch (t: Throwable) {
                                android.widget.Toast.makeText(context, "Failed to copy: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        androidx.compose.material3.Text("Copy Crash Log", fontSize = 11.sp, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Button(
                        onClick = {
                            try {
                                crashPrefs?.edit()?.remove("last_crash_log")?.apply()
                            } catch (_: Throwable) {}
                            crashLog = null
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        androidx.compose.material3.Text("Dismiss", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
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
    var glassTransparency by remember(authenticatedUsername) {
        val saved = profilePrefs.getFloat("glass_transparency_$authenticatedUsername", 0.0f).coerceIn(0.0f, 1.0f)
        mutableStateOf(saved)
    }

    ConnectoTheme(themeMode = currentThemeMode, glassTransparency = glassTransparency) {
        Column(modifier = Modifier.fillMaxSize()) {
            CrashLogBanner()
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                        if (currentThemeMode.isDark != isDark) {
                            val newMode = if (isDark) com.example.connecto.ui.designsystem.ThemeMode.NOIR else com.example.connecto.ui.designsystem.ThemeMode.PARCHMENT
                            currentThemeMode = newMode
                            profilePrefs.edit()
                                .putString("theme_mode_$authenticatedUsername", newMode.id)
                                .putBoolean("dark_theme_$authenticatedUsername", isDark)
                                .apply()
                        }
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
    val coroutineScope = rememberCoroutineScope()

    // Lazy-Mount + Keep-Alive: mount only initialTab on cold launch; subsequent tabs mount upon first tap
    var mountedTabNames by rememberSaveable { mutableStateOf(setOf(initialTab.name)) }

    val updateCurrentTab: (ConnectoTab) -> Unit = { tab ->
        if (tab != currentTab) {
            // Instant Visual Response: update active tab immediately before any background work starts
            currentTab = tab
            if (!mountedTabNames.contains(tab.name)) {
                mountedTabNames = mountedTabNames + tab.name
            }
            // Defer background persistence to IO thread past frame 1
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                prefs.edit().putString("last_active_tab", tab.name).apply()
            }
        }
    }
    var hasUnreadChats by remember { mutableStateOf(false) }
    var totalUnreadMessagesCount by remember { mutableStateOf(0) }
    var isDirectChatOpen by remember { mutableStateOf(false) }
    var isNotificationCenterOpen by remember { mutableStateOf(false) }
    var isChannelDrawerOpen by remember { mutableStateOf(false) }
    var homeScrollToTopTrigger by remember { mutableStateOf(0) }
    var messagesScrollToTopTrigger by remember { mutableStateOf(0) }
    var channelScrollToTopTrigger by remember { mutableStateOf(0) }
    var callsScrollToTopTrigger by remember { mutableStateOf(0) }
    var profileScrollToTopTrigger by remember { mutableStateOf(0) }
    var channelScrollToBottomTrigger by remember { mutableStateOf(0) }

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
            if (authenticatedUsername != "guest" && com.example.connecto.network.ConnectoApiClient.currentUsername != "guest") {
                (context as? android.app.Activity)?.runOnUiThread {
                    if (flowState != AppFlowState.AUTH_HOME) {
                        android.widget.Toast.makeText(context, reason, android.widget.Toast.LENGTH_LONG).show()
                        performCompleteSignOut(context, authenticatedUsername)
                        flowState = AppFlowState.AUTH_HOME
                    }
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
        try {
            val isMaint = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.example.connecto.network.ConnectoApiClient.isServerInMaintenance()
            }
            if (isMaint) {
                MainActivity.openMaintenanceScreen()
            }
        } catch (_: Throwable) {}

        try {
            VoiceCallManager.init(context)
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "VoiceCallManager.init failed: ${t.message}")
        }
        try {
            ConnectoNotificationManager.init(context)
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "ConnectoNotificationManager.init failed: ${t.message}")
        }

        // Defer background service cleanup & database purge off-startup
        try {
            com.example.connecto.notifications.ConnectoRealtimeService.dismissConnectoActiveNotification(context)
        } catch (_: Throwable) {}
        try {
            com.example.connecto.data.ConnectoDatabaseHelper.getInstance(context).schedulePurgeAsync()
        } catch (_: Throwable) {}

        // Defer ProcessLifecycleOwner observer registration until after UI mount
        try {
            androidx.lifecycle.ProcessLifecycleOwner.get().lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
                override fun onStart(owner: androidx.lifecycle.LifecycleOwner) {
                    try {
                        com.example.connecto.voice.VoiceCallManager.onAppForegrounded()
                    } catch (t: Throwable) {
                        android.util.Log.e("MainActivity", "ProcessLifecycle onStart error: ${t.message}")
                    }
                }
                override fun onStop(owner: androidx.lifecycle.LifecycleOwner) {
                    try {
                        com.example.connecto.voice.VoiceCallManager.onAppBackgrounded()
                        com.example.connecto.security.SecretVaultManager.lockVault()
                    } catch (t: Throwable) {
                        android.util.Log.e("MainActivity", "ProcessLifecycle onStop error: ${t.message}")
                    }
                }
            })
        } catch (t: Throwable) {
            android.util.Log.e("MainActivity", "ProcessLifecycleObserver registration failed: ${t.message}")
        }

        // Configure Coil image caching with memory & disk cache
        try {
            val imageLoader = coil.ImageLoader.Builder(context)
                .memoryCache {
                    coil.memory.MemoryCache.Builder(context)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    coil.disk.DiskCache.Builder()
                        .directory(context.cacheDir.resolve("coil_image_cache"))
                        .maxSizeBytes(50L * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build()
            coil.Coil.setImageLoader(imageLoader)
        } catch (_: Throwable) {}

        // Only prompt notification permission for a new user on their very first download/launch
        try {
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
        } catch (_: Throwable) {}
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
                    ?: com.example.connecto.network.ConnectoApiClient.getPersistedToken()
                if (!savedToken.isNullOrBlank()) {
                    com.example.connecto.network.ConnectoApiClient.sessionToken = savedToken
                }
                if (com.example.connecto.network.ConnectoApiClient.currentUsername.isNullOrBlank()) {
                    com.example.connecto.network.ConnectoApiClient.currentUsername = authenticatedUsername
                }
                if (com.example.connecto.network.ConnectoApiClient.currentUserId.isNullOrBlank()) {
                    com.example.connecto.network.ConnectoApiClient.currentUserId = userId
                }
                VoiceCallManager.updateCredentialsAndConnect(
                    username = authenticatedUsername,
                    userId = userId,
                    token = savedToken,
                    displayName = displayName
                )
                com.example.connecto.notifications.ConnectoRealtimeService.startService(context)
                // Run non-critical initial profile & logs in background without blocking startup
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        com.example.connecto.network.ConnectoApiClient.getProfile(authenticatedUsername)
                    } catch (_: Exception) {}
                    try {
                        com.example.connecto.network.ConnectoApiClient.getCallLogs(authenticatedUsername)
                    } catch (_: Exception) {}
                    try {
                        com.example.connecto.data.WallpaperSyncManager.syncWallpapersFromCloud(context)
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                // Ignore network error on startup
            }
        }
    }

    // Register FCM token and sync cloud wallpapers on login/entry
    androidx.compose.runtime.LaunchedEffect(flowState, authenticatedUsername) {
        if (flowState == AppFlowState.CHAT_APP && authenticatedUsername.isNotBlank()) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    com.example.connecto.data.WallpaperSyncManager.syncWallpapersFromCloud(context)
                } catch (_: Exception) {}
            }
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
                    val db = com.example.connecto.data.ConnectoDatabaseHelper.getInstance(context)
                    val friends = db.getCachedFriends()
                    val groups = db.getCachedGroups()
                    val totalUnread = friends.sumOf { it.unreadCount } + groups.sumOf { it.unreadCount }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        totalUnreadMessagesCount = totalUnread
                        hasUnreadChats = totalUnread > 0
                    }
                } catch (_: Exception) {}
            }

            // Listen to real-time incoming messages across tabs
            VoiceCallManager.incomingChannelMessages.collect { incoming ->
                val myU = authenticatedUsername.trim().removePrefix("@").lowercase()
                if (myU == "guest") return@collect
                val authorU = incoming.authorId.trim().removePrefix("@").lowercase()
                if (authorU.isNotBlank() && authorU != myU) {
                    // If user is actively inside direct chat in MESSAGES tab, CustomizedChatScreen handles real-time read state
                    if (currentTab == ConnectoTab.MESSAGES && isDirectChatOpen) {
                        return@collect
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val db = com.example.connecto.data.ConnectoDatabaseHelper.getInstance(context)
                            val friends = db.getCachedFriends()
                            val groups = db.getCachedGroups()
                            val totalUnread = friends.sumOf { it.unreadCount } + groups.sumOf { it.unreadCount }
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                totalUnreadMessagesCount = maxOf(totalUnreadMessagesCount + 1, totalUnread)
                                hasUnreadChats = totalUnreadMessagesCount > 0
                            }
                        } catch (_: Exception) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                totalUnreadMessagesCount++
                                hasUnreadChats = true
                            }
                        }
                    }
                }
            }
        }
    }

    // Automated Server Maintenance Detection: Keep connections running in the background without interrupting the user
    val isWsConnectedGlobal by VoiceCallManager.isWsConnectedFlow.collectAsState()

    androidx.compose.runtime.LaunchedEffect(isWsConnectedGlobal, flowState) {
        if (isWsConnectedGlobal && MainActivity.isServerMaintenanceOpen) {
            MainActivity.closeMaintenanceScreen()
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

    // Keep real-time WebSocket & background service alive when active; auto-lock on return if enabled
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, authenticatedUsername) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
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
                                val savedUserId = prefs.getString("user_id", null) ?: if (savedUsername == "guest") "guest_sandbox_user" else savedUsername
                                val savedEmail = prefs.getString("email", null) ?: if (savedUsername == "guest") "guest@connecto.fun" else null
                                val savedDisplayName = prefs.getString("display_name", null) ?: if (savedUsername == "guest") "Guest Explorer" else savedUsername
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
                            // Check if same or different user is signing in
                            val metaPrefs = context.getSharedPreferences("connecto_auth_meta", Context.MODE_PRIVATE)
                            val lastUser = metaPrefs.getString("last_signed_out_user", null)
                            val currentNormUser = username.trim().lowercase().removePrefix("@")

                            // Privacy guard: Only purge local SQLite cache if a DIFFERENT user logs in
                            if (lastUser != null && lastUser.isNotBlank() && lastUser != currentNormUser) {
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    try {
                                        com.example.connecto.data.ConnectoDatabaseHelper.getInstance(context).clearAllCache()
                                    } catch (_: Exception) {}
                                }
                                val userPrefsToPurge = listOf(
                                    "connecto_user_profile_prefs",
                                    "connecto_chat_read_prefs",
                                    "connecto_friend_prefs",
                                    "connecto_notifications_prefs",
                                    "connecto_notification_prefs",
                                    "connecto_profile"
                                )
                                for (pName in userPrefsToPurge) {
                                    context.getSharedPreferences(pName, Context.MODE_PRIVATE).edit().clear().apply()
                                }
                            }
                            metaPrefs.edit().putString("last_signed_out_user", currentNormUser).apply()

                            // Persist user session across both SharedPreferences and SecureStorageManager
                            val tokenVal = com.example.connecto.network.ConnectoApiClient.sessionToken
                                ?: if (username == "guest") "guest_token_sandbox" else null
                            val userIdVal = com.example.connecto.network.ConnectoApiClient.currentUserId
                                ?: if (username == "guest") "guest_sandbox_user" else username
                            val emailVal = com.example.connecto.network.ConnectoApiClient.currentUserEmail
                                ?: if (username == "guest") "guest@connecto.fun" else null
                            val dispNameVal = com.example.connecto.network.ConnectoApiClient.currentUserDisplayName
                                ?: if (username == "guest") "Guest Explorer" else username
                            val isAdminVal = com.example.connecto.network.ConnectoApiClient.currentUserIsAdmin

                            prefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putBoolean("is_admin", isAdminVal)
                                .putString("username", username)
                                .putString("token", tokenVal)
                                .putString("user_id", userIdVal)
                                .putString("email", emailVal)
                                .putString("display_name", dispNameVal)
                                .apply()

                            try {
                                val secPrefs = com.example.connecto.security.SecureStorageManager.getEncryptedPrefs(context)
                                secPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putBoolean("is_admin", isAdminVal)
                                    .putString("username", username)
                                    .putString("token", tokenVal)
                                    .putString("user_id", userIdVal)
                                    .putString("email", emailVal)
                                    .putString("display_name", dispNameVal)
                                    .apply()
                            } catch (_: Exception) {}

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
                    val savedChannel = remember { prefs.getString("last_active_channel", "general") ?: "general" }
                    var selectedChannelForWorkspace by rememberSaveable { mutableStateOf(savedChannel) }
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val isImeVisible = androidx.compose.foundation.layout.WindowInsets.ime.getBottom(density) > 100

                    val emailVerification by com.example.connecto.network.ConnectoApiClient.emailVerificationState.collectAsState()
                    var showManualVerifyModal by rememberSaveable { mutableStateOf(false) }
                    var isProfileSubScreenOpen by rememberSaveable { mutableStateOf(false) }

                    val showBottomBar = !(currentTab == ConnectoTab.MESSAGES && isDirectChatOpen) && !(currentTab == ConnectoTab.PROFILE && isProfileSubScreenOpen) && !isImeVisible

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
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                            ) {
                                // Minimized Multitasking Call Bar: Always visible during active minimized call across all screens
                                val isCallOngoing = (callState == CallState.CONNECTED || callState == CallState.OUTGOING_RINGING)
                                if (isCallMinimized && isCallOngoing) {
                                    MinimizedMultitaskingCallBar(
                                        callTitle = if (managerCallTitle.isNotBlank()) managerCallTitle else "Voice Call",
                                        onExpand = { isCallMinimized = false },
                                        onEndCall = {
                                            isCallMinimized = false
                                            VoiceCallManager.endCall()
                                        }
                                    )
                                }

                                if (showBottomBar) {
                                    ConnectoBottomBar(
                                        currentTab = currentTab,
                                        onTabSelected = { selectedTab ->
                                            if (selectedTab == ConnectoTab.HOME && currentTab == ConnectoTab.HOME) {
                                                homeScrollToTopTrigger++
                                            }
                                            if (selectedTab == ConnectoTab.MESSAGES && currentTab == ConnectoTab.MESSAGES) {
                                                messagesScrollToTopTrigger++
                                            }
                                            if (selectedTab == ConnectoTab.CHANNELS && currentTab == ConnectoTab.CHANNELS) {
                                                channelScrollToTopTrigger++
                                            }
                                            if (selectedTab == ConnectoTab.CALLS && currentTab == ConnectoTab.CALLS) {
                                                callsScrollToTopTrigger++
                                            }
                                            if (selectedTab == ConnectoTab.PROFILE && currentTab == ConnectoTab.PROFILE) {
                                                profileScrollToTopTrigger++
                                            }
                                            updateCurrentTab(selectedTab)
                                        },
                                        hasUnreadChats = hasUnreadChats,
                                        hasUnreadCalls = false,
                                        isCallActive = isCallOngoing,
                                        unreadMessagesCount = totalUnreadMessagesCount,
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
                                ConnectoTab.entries.forEach { tab ->
                                    if (mountedTabNames.contains(tab.name)) {
                                        val isVisible = (currentTab == tab)
                                        val tabAlpha by androidx.compose.animation.core.animateFloatAsState(
                                            targetValue = if (isVisible) 1.0f else 0.0f,
                                            animationSpec = tween(durationMillis = 80, easing = androidx.compose.animation.core.LinearEasing),
                                            label = "tabVisibility_${tab.name}"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .zIndex(if (isVisible) 1f else 0f)
                                                .graphicsLayer {
                                                    alpha = tabAlpha
                                                }
                                                .layout { measurable, constraints ->
                                                    val placeable = measurable.measure(constraints)
                                                    layout(placeable.width, placeable.height) {
                                                        if (tabAlpha > 0.01f) {
                                                            placeable.place(0, 0)
                                                        }
                                                    }
                                                }
                                        ) {
                                            when (tab) {
                                                ConnectoTab.HOME -> {
                                                    HomeChatScreen(
                                                        currentUsername = authenticatedUsername,
                                                        isTabActive = isVisible,
                                                        onNavigateToMessages = { updateCurrentTab(ConnectoTab.MESSAGES) },
                                                        onNavigateToProfile = { updateCurrentTab(ConnectoTab.PROFILE) },
                                                        onNavigateToCalls = { updateCurrentTab(ConnectoTab.CALLS) },
                                                        onOpenNotifications = { isNotificationCenterOpen = true },
                                                        unreadNotificationsCount = unreadNotifCount,
                                                        onNavigateToChannel = { channelName ->
                                                            selectedChannelForWorkspace = channelName
                                                            prefs.edit().putString("last_active_channel", channelName).apply()
                                                            updateCurrentTab(ConnectoTab.CHANNELS)
                                                        },
                                                        scrollToTopTrigger = homeScrollToTopTrigger
                                                    )
                                                }
                                                ConnectoTab.MESSAGES -> {
                                                    CustomizedChatScreen(
                                                        currentUsername = authenticatedUsername,
                                                        isTabActive = isVisible,
                                                        onOpenProfile = { updateCurrentTab(ConnectoTab.PROFILE) },
                                                        onUnreadStatusChanged = { hasRemainingUnread ->
                                                            hasUnreadChats = hasRemainingUnread
                                                        },
                                                        onUnreadCountChanged = { count ->
                                                            totalUnreadMessagesCount = count
                                                            hasUnreadChats = count > 0
                                                        },
                                                        onDirectChatStateChanged = { isOpen ->
                                                            isDirectChatOpen = isOpen
                                                        },
                                                        onOpenNotifications = { isNotificationCenterOpen = true },
                                                        unreadNotificationsCount = unreadNotifCount,
                                                        targetChatUserOrChannel = MainActivity.pendingOpenChatTarget,
                                                        onTargetChatHandled = {
                                                            val prevTarget = MainActivity.pendingOpenChatTarget
                                                            if (!prevTarget.isNullOrBlank()) {
                                                                ConnectoNotificationManager.cancelConversationNotification(prevTarget)
                                                            }
                                                            MainActivity.pendingOpenChatTarget = null
                                                        },
                                                        scrollToTopTrigger = messagesScrollToTopTrigger
                                                    )
                                                }
                                                ConnectoTab.CHANNELS -> {
                                                    ChannelsWorkspaceScreen(
                                                        currentUsername = authenticatedUsername,
                                                        isTabActive = isVisible,
                                                        initialChannelId = selectedChannelForWorkspace,
                                                        onOpenProfile = { updateCurrentTab(ConnectoTab.PROFILE) },
                                                        onOpenNotifications = { isNotificationCenterOpen = true },
                                                        unreadNotificationsCount = unreadNotifCount,
                                                        onNavigateToCalls = { updateCurrentTab(ConnectoTab.CALLS) },
                                                        onDrawerStateChanged = { isOpen -> isChannelDrawerOpen = isOpen },
                                                        scrollToTopTrigger = channelScrollToTopTrigger,
                                                        scrollToBottomTrigger = channelScrollToBottomTrigger
                                                    )
                                                }
                                                ConnectoTab.CALLS -> {
                                                    CallsScreen(
                                                        currentUsername = authenticatedUsername,
                                                        isTabActive = isVisible,
                                                        onOpenProfile = { updateCurrentTab(ConnectoTab.PROFILE) },
                                                        onOpenNotifications = { isNotificationCenterOpen = true },
                                                        unreadNotificationsCount = unreadNotifCount,
                                                        scrollToTopTrigger = callsScrollToTopTrigger
                                                    )
                                                }
                                                ConnectoTab.PROFILE -> {
                                                    ProfileScreen(
                                                        username = authenticatedUsername,
                                                        isTabActive = isVisible,
                                                        currentThemeMode = currentThemeMode,
                                                        onSelectThemeMode = onSelectThemeMode,
                                                        isDarkTheme = isDarkTheme,
                                                        onToggleTheme = onToggleTheme,
                                                        glassTransparency = glassTransparency,
                                                        onChangeGlassTransparency = onChangeGlassTransparency,
                                                        onLockApp = { isBiometricOverlayActive = true },
                                                        onOpenNotifications = { isNotificationCenterOpen = true },
                                                        unreadNotificationsCount = unreadNotifCount,
                                                        scrollToTopTrigger = profileScrollToTopTrigger,
                                                        onSubScreenChanged = { isProfileSubScreenOpen = it },
                                                        onSignOut = {
                                                            performCompleteSignOut(context, authenticatedUsername)
                                                            flowState = AppFlowState.AUTH_HOME
                                                        },
                                                        onDeleteAccount = {
                                                            performCompleteSignOut(context, authenticatedUsername, isAccountDeletion = true)
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

        val isGroupCall by VoiceCallManager.isGroupCall.collectAsState()
        val incomingGroupCall by VoiceCallManager.incomingGroupCall.collectAsState()
        var isGroupCallModeBOpen by remember { mutableStateOf(false) }

        LaunchedEffect(incomingGroupCall) {
            if (incomingGroupCall == null) {
                isGroupCallModeBOpen = false
            }
        }

        // Safe Mode A / Mode B Group Voice Call Handling (No !! forced unwrap)
        incomingGroupCall?.let { groupCallData ->
            if (!isGroupCallModeBOpen) {
                ModeAGroupCallBanner(
                    incomingData = groupCallData,
                    onExpandModeB = { isGroupCallModeBOpen = true },
                    onJoin = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        VoiceCallManager.acceptGroupCall(initialMuteState = false)
                    },
                    onDecline = {
                        VoiceCallManager.declineGroupCall()
                    }
                )
            } else {
                IncomingGroupCallUI(
                    incomingData = groupCallData,
                    onAccept = { initialMute ->
                        isGroupCallModeBOpen = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        VoiceCallManager.acceptGroupCall(initialMuteState = initialMute)
                    },
                    onDecline = { replyMsg ->
                        isGroupCallModeBOpen = false
                        VoiceCallManager.declineGroupCall(sendReplyMessage = replyMsg)
                    }
                )
            }
        }

        // 1:1 Incoming Voice Call In-App Overlay
        val currentIncoming1on1 = incomingCallData
        if (currentIncoming1on1 != null && callState == CallState.INCOMING_RINGING) {
            com.example.connecto.voice.FullScreenIncomingCallUI(
                callerName = currentIncoming1on1.callerName.ifEmpty { currentIncoming1on1.callerUsername },
                callerUsername = currentIncoming1on1.callerUsername,
                callerAvatar = currentIncoming1on1.callerAvatar,
                onAccept = {
                    com.example.connecto.voice.CallRingtoneManager.stopRingtoneAndVibration(context)
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    VoiceCallManager.acceptIncomingCall()
                    com.example.connecto.notifications.ConnectoNotificationManager.cancelCallNotification()
                },
                onDecline = {
                    com.example.connecto.voice.CallRingtoneManager.stopRingtoneAndVibration(context)
                    VoiceCallManager.declineIncomingCall()
                    com.example.connecto.notifications.ConnectoNotificationManager.cancelCallNotification()
                }
            )
        }

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
            if (isGroupCall) {
                FullScreenGroupCallUI(
                    callTitle = if (managerCallTitle.isNotBlank()) managerCallTitle else "Group Voice Call",
                    onMinimize = {
                        isCallMinimized = true
                    },
                    onEndCall = {
                        isCallMinimized = false
                        VoiceCallManager.endCall()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
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
        // Active strictly during CONNECTED call state only (never during ringing/dialing or idle).
        val isCallActive = (callState == CallState.CONNECTED)
        val shouldBlackoutForEar = isProximityScreenLocked &&
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

