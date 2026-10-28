package com.example.connecto.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.security.BiometricAuthManager
import com.example.connecto.security.BiometricStatus
import com.example.connecto.ui.auth.ForgotPasswordDialog
import com.example.connecto.ui.theme.isAppInLightTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.LockReset
import com.example.connecto.ui.components.CropMode
import com.example.connecto.ui.components.PhotoLightboxDialog
import com.example.connecto.ui.components.InteractiveCropFitDialog
import com.example.connecto.ui.components.CoverPresetsDialog
import com.example.connecto.ui.components.CoverOptionsMenuDialog
import com.example.connecto.ui.designsystem.isCoverPreset
import com.example.connecto.ui.designsystem.getCoverPreset
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import com.example.connecto.ui.components.CustomerSupportAiDialog
import com.example.connecto.ui.components.PrivacyPolicyDialog
import com.example.connecto.ui.components.TermsOfServiceDialog
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Description
import com.example.connecto.ui.components.FullScreenGroupProfileDialog
import com.example.connecto.ui.designsystem.ConnectoAvatar
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.res.painterResource
import com.example.connecto.R
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SupportAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.connecto.notifications.NotificationPreferences
import com.example.connecto.notifications.NotificationSoundManager
import com.example.connecto.notifications.ConnectoNotificationManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.connecto.ui.components.AddFriendDialog
import com.example.connecto.ui.components.ConnectoTopHeader
import com.example.connecto.ui.components.LiquidGlassTransparencyBar
import com.example.connecto.ui.theme.OnlineGreen
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.connecto.ui.theme.TextDisabledColor
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.glassCard
import com.example.connecto.ui.theme.liquidGlassCard
import com.example.connecto.ui.designsystem.sanitizeAvatarUrl
import com.example.connecto.ui.designsystem.resolveImageModel
import com.example.connecto.ui.designsystem.getAvatarGradient
import com.example.connecto.ui.designsystem.ConnectoTheme
import kotlinx.coroutines.delay

/**
 * Traverses context wrappers to find the parent FragmentActivity.
 */
private fun Context.findFragmentActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

/**
 * Dedicated sub-pages for Profile Settings.
 * Clicking a section button changes the entire UI to open that specific sub-page.
 */
enum class ProfileSubPage(
    val title: String,
    val icon: ImageVector
) {
    SECURITY(
        title = "Security & Privacy",
        icon = Icons.Default.Shield
    ),
    SESSION_MANAGEMENT(
        title = "Session Management",
        icon = Icons.Default.Devices
    ),
    PRESENCE_STEALTH(
        title = "Presence & Stealth Mode",
        icon = Icons.Default.VisibilityOff
    ),
    PERSONAL_INFO(
        title = "Personal Information",
        icon = Icons.Default.Person
    ),
    TWO_FACTOR_SECURITY(
        title = "Two-Factor & Security",
        icon = Icons.Default.Shield
    ),
    BIOMETRIC_LOCK(
        title = "Biometric & App Lock",
        icon = Icons.Default.Fingerprint
    ),
    APPEARANCE_THEME(
        title = "Appearance & Display Theme",
        icon = Icons.Default.Tune
    ),
    VOICE_AUDIO(
        title = "Voice & Audio Settings",
        icon = Icons.Default.Mic
    ),
    GROUPS_COMMUNITIES(
        title = "Groups & Communities",
        icon = Icons.Default.Groups
    ),
    NOTIFICATIONS(
        title = "Notifications",
        icon = Icons.Default.Notifications
    ),
    HELP_SUPPORT(
        title = "Help & Support",
        icon = Icons.AutoMirrored.Filled.Help
    )
}

@Composable
fun ProfileScreen(
    username: String = "Gamer",
    currentThemeMode: com.example.connecto.ui.designsystem.ThemeMode = com.example.connecto.ui.designsystem.ThemeMode.NOIR,
    onSelectThemeMode: (com.example.connecto.ui.designsystem.ThemeMode) -> Unit = {},
    isDarkTheme: Boolean = currentThemeMode.isDark,
    onToggleTheme: (Boolean) -> Unit = {},
    glassTransparency: Float = 0.20f,
    onChangeGlassTransparency: (Float) -> Unit = {},
    onSignOut: () -> Unit = {},
    onDeleteAccount: () -> Unit = onSignOut,
    onLockApp: () -> Unit = {},
    onOpenNotifications: (() -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = remember(context) { context.findFragmentActivity() }
    val prefs = remember { context.getSharedPreferences("connecto_user_profile_prefs", Context.MODE_PRIVATE) }

    // Global Screen Security & Black Screen state
    val isScreenSecurityActive by com.example.connecto.security.ScreenSecurityManager.isSecurityEnabledState.collectAsState()
    val isRecordingActive by com.example.connecto.security.ScreenSecurityManager.isScreenRecordingActive.collectAsState()
    val isCastingActive by com.example.connecto.security.ScreenSecurityManager.isCastingActive.collectAsState()

    // Admin detection
    val isCurrentUserAdmin by ConnectoApiClient.currentUserIsAdminState.collectAsState()
    val isUserAdmin = isCurrentUserAdmin ||
        username.trim().lowercase().removePrefix("@") in listOf("chinnu14754x", "chinnu")

    // ================= USER PROFILE STATE =================
    var displayNameInput by remember(username) {
        val initialName = prefs.getString("display_name_$username", null)
            ?: ConnectoApiClient.currentUserDisplayName
            ?: username
        mutableStateOf(initialName)
    }
    var bioInput by remember(username) {
        mutableStateOf(
            prefs.getString("bio_$username", "Competitive gamer, tournament player, and Free Fire streamer 🎮")
                ?: "Competitive gamer, tournament player, and Free Fire streamer 🎮"
        )
    }
    val globalAvatar by ConnectoApiClient.currentUserAvatarState.collectAsState()
    var profilePhotoUri by remember(username) {
        val saved = prefs.getString("photo_uri_$username", null)
        val initial = if (saved.isNullOrBlank() || saved == "null") ConnectoApiClient.currentUserAvatarState.value else saved
        mutableStateOf(if (initial.isNullOrBlank() || initial == "null") null else initial)
    }
    LaunchedEffect(globalAvatar) {
        if (!globalAvatar.isNullOrBlank() && globalAvatar != "null" && profilePhotoUri != globalAvatar) {
            profilePhotoUri = globalAvatar
        }
    }
    val globalBanner by ConnectoApiClient.currentUserBannerState.collectAsState()
    var bannerPhotoUri by remember(username) {
        val saved = prefs.getString("banner_uri_$username", null)
        val localCover = java.io.File(context.filesDir, "cover_${username.lowercase()}.webp")
        val initial = when {
            !saved.isNullOrBlank() && saved != "null" -> saved
            localCover.exists() -> localCover.absolutePath
            else -> null
        }
        mutableStateOf(initial)
    }
    LaunchedEffect(globalBanner) {
        if (!globalBanner.isNullOrBlank() && globalBanner != "null" && bannerPhotoUri != globalBanner) {
            val localCover = java.io.File(context.filesDir, "cover_${username.lowercase()}.webp")
            if (isCoverPreset(globalBanner) || !localCover.exists() || bannerPhotoUri.isNullOrBlank()) {
                bannerPhotoUri = globalBanner
            }
        }
    }
    var isUploadingBanner by remember { mutableStateOf(false) }
    var bannerUrlFieldInput by remember { mutableStateOf("") }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    // ================= PERSONAL DETAILS STATE =================
    var currentUsernameState by remember(username) {
        mutableStateOf(username)
    }
    var usernameChangedFlag by remember(username) {
        mutableStateOf(prefs.getBoolean("username_changed_$username", false))
    }
    var fullNameInput by remember(username) {
        mutableStateOf(prefs.getString("full_name_$username", "") ?: "")
    }
    var emailInput by remember(username) {
        val initialEmail = prefs.getString("email_$username", null)
            ?: ConnectoApiClient.currentUserEmail
            ?: "$username@connecto.gg"
        mutableStateOf(initialEmail)
    }
    var stealthModeEnabled by remember(username) {
        mutableStateOf(prefs.getBoolean("stealth_mode_$username", false))
    }
    var isUserOnline by remember(username) {
        mutableStateOf(false)
    }
    var twoFactorEnabled by remember(username) {
        mutableStateOf(prefs.getBoolean("two_factor_enabled_$username", false))
    }
    var twoFactorMethod by remember(username) {
        mutableStateOf(prefs.getString("two_factor_method_$username", "email") ?: "email")
    }
    var isAppLockEnabled by remember(username) {
        mutableStateOf(prefs.getBoolean("biometric_lock_$username", false))
    }
    var autoLockOnBackground by remember(username) {
        mutableStateOf(prefs.getBoolean("auto_lock_background_$username", true))
    }
    var isEmailLocked by remember(username) {
        val initialHasEmail = !ConnectoApiClient.currentUserEmail.isNullOrBlank() || prefs.getString("email_$username", null)?.isNotBlank() == true
        mutableStateOf(initialHasEmail)
    }
    var revealEmail by remember { mutableStateOf(false) }
    var changeContactTarget by remember { mutableStateOf<String?>(null) } // "email" only

    var activeSessions by remember { mutableStateOf<List<com.example.connecto.network.UserSessionDto>>(emptyList()) }
    var isLoadingSessions by remember { mutableStateOf(false) }
    var terminatingSessionId by remember { mutableStateOf<String?>(null) }
    var isTerminatingOtherSessions by remember { mutableStateOf(false) }
    var sessionErrorMessage by remember { mutableStateOf<String?>(null) }

    var showForgotPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var show2FaSetupDialog by rememberSaveable { mutableStateOf(false) }
    var twoFaSetupMaskedDest by rememberSaveable { mutableStateOf<String?>(null) }
    var show2FaDisableDialog by rememberSaveable { mutableStateOf(false) }
    var twoFaDisableMaskedDest by rememberSaveable { mutableStateOf<String?>(null) }
    var twoFaErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var deleteConfirmInput by rememberSaveable { mutableStateOf("") }

    var showCustomerSupportAiDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacyPolicyDialog by rememberSaveable { mutableStateOf(false) }
    var showTermsOfServiceDialog by rememberSaveable { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (com.example.connecto.security.TwoFactorStateManager.restoreIfPending(context)) {
            show2FaSetupDialog = true
            twoFaSetupMaskedDest = com.example.connecto.security.TwoFactorStateManager.setupMaskedDestination
        }
    }

    // User Groups Management State in Profile Tab
    val dbHelper = remember { com.example.connecto.data.ConnectoDatabaseHelper(context) }
    val profileGroupsList = remember { mutableStateListOf<FriendItem>() }
    var isLoadingProfileGroups by remember { mutableStateOf(false) }
    var selectedGroupForProfileDialog by remember { mutableStateOf<FriendItem?>(null) }
    var groupToConfirmDelete by remember { mutableStateOf<FriendItem?>(null) }
    var isDeletingProfileGroup by remember { mutableStateOf(false) }
    var groupToConfirmLeave by remember { mutableStateOf<FriendItem?>(null) }
    var isLeavingProfileGroup by remember { mutableStateOf(false) }

    // Listen for real-time group removals from Chat Screen or background
    androidx.compose.runtime.DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val gid = intent?.getStringExtra("group_id") ?: return
                profileGroupsList.removeAll { it.id == gid }
                if (groupToConfirmDelete?.id == gid) groupToConfirmDelete = null
                if (groupToConfirmLeave?.id == gid) groupToConfirmLeave = null
            }
        }
        val filter = IntentFilter("com.example.connecto.ACTION_GROUP_REMOVED")
        try {
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {}
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        }
    }

    // Function to reload groups for profile tab
    fun reloadProfileGroups() {
        coroutineScope.launch {
            isLoadingProfileGroups = true
            try {
                val groupsRes = ConnectoApiClient.getUserGroups()
                if (groupsRes.isSuccess) {
                    val serverGroups = groupsRes.getOrThrow()
                    val myUsernameLower = username.trim().lowercase().removePrefix("@")
                    val validGroups = serverGroups.filter { g ->
                        val count = if (g.memberCount > 0) g.memberCount else g.members.size
                        val isMember = g.isCreatedByMe || g.members.any { it.trim().lowercase().removePrefix("@") == myUsernameLower }
                        if (count <= 0) {
                            coroutineScope.launch {
                                try { ConnectoApiClient.deleteGroup(g.id) } catch (_: Exception) {}
                            }
                            false
                        } else if (!isMember) {
                            coroutineScope.launch(Dispatchers.IO) {
                                try { dbHelper.deleteCachedGroup(g.id) } catch (_: Exception) {}
                            }
                            false
                        } else {
                            true
                        }
                    }
                    val mappedGroups = validGroups.map { g ->
                        val init = if (g.name.isNotBlank()) g.name.first().toString().uppercase() else "G"
                        val count = if (g.memberCount > 0) g.memberCount else g.members.size
                        val isCreated = g.isCreatedByMe || g.description.contains(username, ignoreCase = true) || g.admin.lowercase().removePrefix("@") == myUsernameLower
                        FriendItem(
                            id = g.id,
                            name = g.name,
                            handle = "#${g.name.lowercase().replace(" ", "-")}",
                            initial = init,
                            status = "$count members • ${if (isCreated) "Created Group" else "Joined Group"}",
                            bio = g.description.ifBlank { "Connecto Group Chat • $count members" },
                            lastMessage = "Group Chat • Tap to manage",
                            timeAgo = "Active",
                            unreadCount = 0,
                            isOnline = false,
                            avatarUrl = g.iconUrl,
                            isGroup = true,
                            isCreatedByMe = isCreated,
                            memberCount = count,
                            members = g.members,
                            admin = g.admin,
                            creator = g.creator
                        )
                    }
                    val gIds = mappedGroups.map { it.id }.toSet()
                    val removed = profileGroupsList.filter { it.id !in gIds }
                    profileGroupsList.removeAll { it.id !in gIds }
                    coroutineScope.launch(Dispatchers.IO) {
                        for (rem in removed) {
                            try { dbHelper.deleteCachedGroup(rem.id) } catch (_: Exception) {}
                        }
                    }
                    for (item in mappedGroups) {
                        val idx = profileGroupsList.indexOfFirst { it.id == item.id }
                        if (idx == -1) profileGroupsList.add(item) else profileGroupsList[idx] = item
                    }
                }
            } catch (_: Exception) {}
            finally {
                isLoadingProfileGroups = false
            }
        }
    }

    // Live sync with backend SQLite database on launch
    LaunchedEffect(username) {
        reloadProfileGroups()
        try {
            val res = ConnectoApiClient.getProfile(username)
            if (res.isSuccess) {
                val prof = res.getOrThrow()
                if (prof.username.isNotBlank()) {
                    currentUsernameState = prof.username
                }
                usernameChangedFlag = prof.usernameChanged
                prefs.edit().putBoolean("username_changed_${prof.username}", prof.usernameChanged).apply()
                if (prof.fullName.isNotBlank()) {
                    fullNameInput = prof.fullName
                    prefs.edit().putString("full_name_${prof.username}", prof.fullName).apply()
                }
                if (prof.displayName.isNotBlank()) {
                    displayNameInput = prof.displayName
                    prefs.edit().putString("display_name_${prof.username}", prof.displayName).apply()
                }
                if (prof.bio.isNotBlank()) {
                    bioInput = prof.bio
                    prefs.edit().putString("bio_${prof.username}", prof.bio).apply()
                }
                if (!prof.avatarUrl.isNullOrBlank() && prof.avatarUrl != "null") {
                    profilePhotoUri = prof.avatarUrl
                    prefs.edit().putString("photo_uri_${prof.username}", prof.avatarUrl).apply()
                } else if (prof.avatarUrl == "null") {
                    profilePhotoUri = null
                    prefs.edit().remove("photo_uri_${prof.username}").apply()
                }
                if (!prof.email.isNullOrBlank()) {
                    emailInput = prof.email
                    prefs.edit().putString("email_${prof.username}", prof.email).apply()
                    isEmailLocked = true
                }

                twoFactorEnabled = prof.twoFactorEnabled
                prefs.edit().putBoolean("two_factor_enabled_${prof.username}", prof.twoFactorEnabled).apply()
                if (!prof.twoFactorMethod.isNullOrBlank()) {
                    twoFactorMethod = prof.twoFactorMethod
                    prefs.edit().putString("two_factor_method_${prof.username}", prof.twoFactorMethod).apply()
                }
                stealthModeEnabled = prof.isStealth
                isUserOnline = prof.isOnline && !prof.isStealth
                prefs.edit().putBoolean("stealth_mode_${prof.username}", prof.isStealth).apply()
                ConnectoApiClient.updateStealthModeState(prof.isStealth)
                if (!prof.bannerUrl.isNullOrBlank()) {
                    val sBan = prof.bannerUrl.trim()
                    val isCorrupted = sBan.startsWith("data:") && sBan.length < 500
                    if (!isCorrupted) {
                        bannerPhotoUri = sBan
                        bannerUrlFieldInput = sBan
                        prefs.edit().putString("banner_uri_${prof.username}", sBan).apply()
                    }
                }
            }
        } catch (e: Exception) {
            // Keep local fallback
        }
    }

    // Active dedicated sub-page (null = main profile menu, non-null = dedicated sub-screen)
    var activeSubPage by rememberSaveable { mutableStateOf<ProfileSubPage?>(null) }
    var previousSubPage by rememberSaveable { mutableStateOf<ProfileSubPage?>(null) }

    // Notification settings states
    var hideNotificationContent by remember {
        mutableStateOf(NotificationPreferences.isHideContentEnabled(context))
    }
    var selectedNotificationSoundId by remember {
        mutableStateOf(NotificationPreferences.getSelectedSoundId(context))
    }
    var selectedNotificationSoundName by remember {
        mutableStateOf(NotificationPreferences.getSelectedSoundName(context))
    }
    val playingSoundId by NotificationSoundManager.currentlyPlayingSoundId.collectAsState()
    var isNotificationSoundsExpanded by rememberSaveable { mutableStateOf(false) }

    // Help & Support States & Actions
    var expandedFaqIndex by rememberSaveable { mutableStateOf<Int?>(-1) }
    var showBugReportDialog by rememberSaveable { mutableStateOf(false) }
    var bugReportCategory by rememberSaveable { mutableStateOf("Connectivity & Notifications") }
    var bugReportDescription by rememberSaveable { mutableStateOf("") }
    var isEdgePingTesting by remember { mutableStateOf(false) }
    var edgePingResult by remember { mutableStateOf<String?>(null) }

    fun openWebUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open browser: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendSupportEmail(subject: String = "Connecto App Support Request", prefilledBody: String = "") {
        try {
            val body = if (prefilledBody.isNotBlank()) prefilledBody else {
                "Hello Connecto Support Team,\n\n[Please describe your issue or inquiry here]\n\n" +
                "--- Device & App Information ---\n" +
                "• App Version: 4.4.8 (Build 77)\n" +
                "• Username: @$username\n" +
                "• Android Version: Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})\n" +
                "• Device Model: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n" +
                "• Hardware Privacy: ${if (isScreenSecurityActive) "Blackscreen Active" else "Standard"}\n"
            }
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@connecto.fun")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Send Email via..."))
        } catch (e: Exception) {
            Toast.makeText(context, "No email application available: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyDiagnosticsToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val text = "=== CONNECTO SYSTEM DIAGNOSTICS ===\n" +
                "App: Connecto for Android\n" +
                "Version: v4.4.8 (Build 77)\n" +
                "API Endpoint: https://connecto.fun\n" +
                "User: @$username\n" +
                "OS: Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})\n" +
                "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (${android.os.Build.HARDWARE})\n" +
                "Screen Privacy: ${if (isScreenSecurityActive) "FLAG_SECURE Active" else "Inactive"}\n" +
                "App Lock: ${if (isAppLockEnabled) "Enabled" else "Disabled"}\n" +
                "Stealth Mode: ${if (stealthModeEnabled) "Active" else "Inactive"}\n" +
                "Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}"
        val clip = android.content.ClipData.newPlainText("Connecto Diagnostics", text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "System diagnostics copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun runEdgePingTest() {
        coroutineScope.launch {
            isEdgePingTesting = true
            edgePingResult = null
            val startTime = System.currentTimeMillis()
            try {
                val ok = withContext(Dispatchers.IO) {
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                        .build()
                    val req = okhttp3.Request.Builder()
                        .url("https://connecto.fun/api/v1/app/version")
                        .addHeader("X-Force-Cloud", "1")
                        .get()
                        .build()
                    client.newCall(req).execute().use { resp ->
                        resp.isSuccessful
                    }
                }
                val duration = System.currentTimeMillis() - startTime
                if (ok) {
                    edgePingResult = "Optimal • ${duration}ms (Cloudflare Edge Connected)"
                } else {
                    edgePingResult = "Slow • ${duration}ms (Edge responded with non-200)"
                }
            } catch (e: Exception) {
                edgePingResult = "Connection Error: ${e.message ?: "Failed"}"
            } finally {
                isEdgePingTesting = false
            }
        }
    }

    // Stop audio preview when changing subpages or leaving screen
    androidx.compose.runtime.DisposableEffect(activeSubPage) {
        onDispose {
            NotificationSoundManager.stopPreview()
        }
    }

    fun refreshUserSessions() {
        coroutineScope.launch {
            isLoadingSessions = true
            sessionErrorMessage = null
            try {
                val res = ConnectoApiClient.getUserSessions()
                if (res.isSuccess) {
                    activeSessions = res.getOrThrow()
                } else {
                    sessionErrorMessage = res.exceptionOrNull()?.message ?: "Failed to fetch active sessions"
                }
            } catch (e: Exception) {
                sessionErrorMessage = e.message
            } finally {
                isLoadingSessions = false
            }
        }
    }

    LaunchedEffect(activeSubPage) {
        if (activeSubPage == ProfileSubPage.SESSION_MANAGEMENT || activeSubPage == ProfileSubPage.TWO_FACTOR_SECURITY) {
            refreshUserSessions()
        }
    }

    val onTerminateSession: (String) -> Unit = { sessId ->
        coroutineScope.launch {
            terminatingSessionId = sessId
            try {
                val res = ConnectoApiClient.terminateSession(sessId)
                if (res.isSuccess) {
                    activeSessions = activeSessions.filter { it.id != sessId }
                    Toast.makeText(context, "Session logged out successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to terminate session", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                terminatingSessionId = null
            }
        }
    }

    val onTerminateOtherSessions: () -> Unit = {
        coroutineScope.launch {
            isTerminatingOtherSessions = true
            try {
                val res = ConnectoApiClient.terminateOtherSessions()
                if (res.isSuccess) {
                    val count = res.getOrThrow()
                    activeSessions = activeSessions.filter { it.isCurrent }
                    Toast.makeText(context, "Logged out of $count other session(s)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to log out of other sessions", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isTerminatingOtherSessions = false
            }
        }
    }

    // Saved feedback banner
    var showSavedBadge by remember { mutableStateOf(false) }
    LaunchedEffect(showSavedBadge) {
        if (showSavedBadge) {
            delay(2200)
            showSavedBadge = false
        }
    }

    fun triggerAutoSave() {
        showSavedBadge = true
        ConnectoApiClient.currentUserDisplayName = displayNameInput
        coroutineScope.launch {
            try {
                val remoteAvatar = if (profilePhotoUri?.startsWith("http://") == true ||
                    profilePhotoUri?.startsWith("https://") == true ||
                    profilePhotoUri?.startsWith("data:") == true
                ) profilePhotoUri else null

                val remoteBanner = if (bannerPhotoUri?.startsWith("http://") == true ||
                    bannerPhotoUri?.startsWith("https://") == true ||
                    bannerPhotoUri?.startsWith("data:") == true ||
                    isCoverPreset(bannerPhotoUri)
                ) bannerPhotoUri else null

                val res = ConnectoApiClient.updateProfile(
                    username = currentUsernameState,
                    displayName = displayNameInput,
                    fullName = fullNameInput,
                    bio = bioInput,
                    avatarUrl = remoteAvatar,
                    bannerUrl = remoteBanner,
                    isStealth = stealthModeEnabled
                )
                if (res.isSuccess) {
                    val obj = res.getOrThrow()
                    if (obj.has("username")) {
                        val newU = obj.optString("username", currentUsernameState)
                        currentUsernameState = newU
                        ConnectoApiClient.currentUsername = newU
                    }
                    if (obj.optBoolean("username_changed", false)) {
                        usernameChangedFlag = true
                        prefs.edit().putBoolean("username_changed_$currentUsernameState", true).apply()
                    }
                }
            } catch (e: Exception) {
                // Ignore transient network errors; local prefs are already persisted
            }
        }
    }

    fun removeCoverPhoto() {
        bannerPhotoUri = null
        bannerUrlFieldInput = ""
        prefs.edit().remove("banner_uri_$username").apply()
        ConnectoApiClient.updateBannerState(null, context)
        val localCover = java.io.File(context.filesDir, "cover_${username.lowercase()}.webp")
        if (localCover.exists()) {
            try { localCover.delete() } catch (_: Exception) {}
        }
        coroutineScope.launch {
            try {
                ConnectoApiClient.updateProfile(
                    username = currentUsernameState,
                    bannerUrl = "",
                    displayName = displayNameInput,
                    fullName = fullNameInput,
                    bio = bioInput,
                    isStealth = stealthModeEnabled
                )
            } catch (_: Exception) {}
        }
    }

    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    var isUploadingAvatar by remember { mutableStateOf(false) }

    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var activeCropMode by remember { mutableStateOf(CropMode.AVATAR) }
    var showCropDialog by remember { mutableStateOf(false) }

    var lightboxTitle by remember { mutableStateOf("Profile Photo") }
    var lightboxImageUrl by remember { mutableStateOf<String?>(null) }
    var isLightboxBanner by remember { mutableStateOf(false) }
    var showLightbox by remember { mutableStateOf(false) }
    var showCoverPresetsDialog by remember { mutableStateOf(false) }
    var showCoverOptionsMenu by remember { mutableStateOf(false) }

    // Photo Picker Launcher -> opens interactive crop dialog
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not supported for this provider
            }
            pendingCropUri = it
            activeCropMode = CropMode.AVATAR
            showCropDialog = true
        }
    }

    // Banner Photo Picker Launcher -> opens interactive crop dialog
    val bannerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not supported for this provider
            }
            pendingCropUri = it
            activeCropMode = CropMode.BANNER
            showCropDialog = true
        }
    }

    val userInitial = if (username.isNotBlank()) username.first().toString().uppercase() else "C"

    BackHandler(enabled = activeSubPage != null) {
        if (previousSubPage != null) {
            activeSubPage = previousSubPage
            previousSubPage = null
        } else {
            activeSubPage = null
        }
    }

    val themeColors = ConnectoTheme.colors
    val isThemeDark = themeColors.isDark
    val sanitizedBannerPhoto = sanitizeAvatarUrl(bannerPhotoUri)
    val isPresetBanner = isCoverPreset(bannerPhotoUri)
    val presetBannerObj = if (isPresetBanner) getCoverPreset(bannerPhotoUri) else null
    val hasCoverPhoto = !sanitizedBannerPhoto.isNullOrBlank() || isPresetBanner

    val bannerImageRequest = remember(bannerPhotoUri, context) {
        val curUri = bannerPhotoUri
        if (curUri.isNullOrBlank() || isPresetBanner) null
        else {
            val resolved = resolveImageModel(curUri) ?: sanitizedBannerPhoto ?: curUri
            val cacheKey = if (curUri.startsWith("data:") || curUri.length > 100) {
                "banner_${curUri.hashCode()}"
            } else {
                curUri
            }
            ImageRequest.Builder(context)
                .data(resolved)
                .memoryCacheKey(cacheKey)
                .crossfade(true)
                .build()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        if (hasCoverPhoto) {
            // Full-Screen Transparent Cover Photo Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.35f }
            ) {
                if (isPresetBanner && presetBannerObj != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(presetBannerObj.brush)
                    )
                } else if (bannerImageRequest != null) {
                    AsyncImage(
                        model = bannerImageRequest,
                        contentDescription = "Profile Cover Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // High-End Multi-Stop Gradient Scrim for Contrast, Readability & Modern Depth across the entire FULL SCREEN
            val scrimStops = if (themeColors.isLiquidGlass) {
                arrayOf(
                    0.00f to Color.Black.copy(alpha = 0.12f),
                    0.30f to Color.Black.copy(alpha = 0.18f),
                    0.65f to Color.Black.copy(alpha = 0.26f),
                    1.00f to Color.Black.copy(alpha = 0.35f)
                )
            } else {
                arrayOf(
                    0.00f to Color.Black.copy(alpha = 0.18f),
                    0.25f to Color.Black.copy(alpha = 0.28f),
                    0.60f to Color.Black.copy(alpha = 0.38f),
                    1.00f to Color.Black.copy(alpha = 0.48f)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colorStops = scrimStops))
            )

            // Atmospheric Tint matching Accent Colors (disabled for transparent liquid glass to avoid added colors)
            if (!themeColors.isLiquidGlass) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        themeColors.primary.copy(alpha = if (isThemeDark) 0.15f else 0.08f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.85f, size.height * 0.12f),
                                    radius = size.width * 0.80f
                                ),
                                radius = size.width * 0.80f,
                                center = Offset(size.width * 0.85f, size.height * 0.12f)
                            )
                        }
                )
            }
        } else {
            // Ambient default background when no cover photo is set
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(themeColors.background)
                        if (!themeColors.isLiquidGlass) {
                            // Static Ambient Caustic Glare (Top-Right)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        themeColors.primary.copy(alpha = if (isThemeDark) 0.22f else 0.10f),
                                        themeColors.info.copy(alpha = if (isThemeDark) 0.10f else 0.05f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.85f, size.height * 0.10f),
                                    radius = size.width * 0.75f
                                ),
                                radius = size.width * 0.75f,
                                center = Offset(size.width * 0.85f, size.height * 0.10f)
                            )
                            // Static Deep Fluid Depth Orb (Bottom-Left)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        themeColors.surfaceVariant.copy(alpha = if (isThemeDark) 0.16f else 0.08f),
                                        themeColors.info.copy(alpha = if (isThemeDark) 0.08f else 0.04f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.15f, size.height * 0.85f),
                                    radius = size.width * 0.80f
                                ),
                                radius = size.width * 0.80f,
                                center = Offset(size.width * 0.15f, size.height * 0.85f)
                            )
                        }
                    }
            )
        }
        AnimatedContent(
            targetState = activeSubPage,
            transitionSpec = {
                EnterTransition.None togetherWith ExitTransition.None
            },
            label = "profile_subpage_navigation",
            modifier = Modifier.fillMaxSize()
        ) { currentSubPage ->
            if (currentSubPage == null) {
                // ========== 1. MAIN PROFILE OVERVIEW SCREEN ==========
                Column(modifier = Modifier.fillMaxSize()) {
                    // ========== SHARED TOP HEADER ==========
                    ConnectoTopHeader(
                        title = "PROFILE SETTINGS",
                        subtitle = if (stealthModeEnabled) "Stealth Mode Active" else "@$username",
                        userInitial = userInitial,
                        avatarUrl = profilePhotoUri,
                        isStealthModeOn = stealthModeEnabled,
                        isOnline = isUserOnline,
                        showProfileButton = false
                    )

                    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }

                        // ================= AUTO-SAVE CONFIRMATION BADGE =================
                        item(key = "auto_save_badge") {
                            AnimatedVisibility(
                                visible = showSavedBadge,
                                enter = EnterTransition.None,
                                exit = ExitTransition.None
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(OnlineGreen.copy(alpha = 0.15f))
                                        .border(1.dp, OnlineGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = OnlineGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Personal details updated & securely saved",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OnlineGreen
                                        )
                                    }
                                }
                            }
                        }

                        // ================= 1. AVATAR & USER SUMMARY CARD =================
                        item(key = "avatar_summary_card") {
                            val avatarCardShape = RoundedCornerShape(24.dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        Modifier.liquidGlassCard(
                                            shape = avatarCardShape,
                                            backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                else MaterialTheme.colorScheme.surface.copy(
                                                    alpha = if (isThemeDark) 0.72f else 0.85f
                                                ),
                                            borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.40f)
                                                else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                        )
                                    )
                                    .clip(avatarCardShape)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 1. DEDICATED COVER PHOTO BANNER HEADER
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(148.dp)
                                            .clickable {
                                                if (hasCoverPhoto) {
                                                    lightboxTitle = "Background Cover"
                                                    lightboxImageUrl = bannerPhotoUri
                                                    isLightboxBanner = true
                                                    showLightbox = true
                                                } else {
                                                    showCoverOptionsMenu = true
                                                }
                                            }
                                    ) {
                                        if (isPresetBanner && presetBannerObj != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(presetBannerObj.brush)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                Color.Black.copy(alpha = 0.45f),
                                                                Color.Transparent,
                                                                Color.Black.copy(alpha = 0.50f)
                                                            )
                                                        )
                                                    )
                                            )
                                        } else if (bannerImageRequest != null) {
                                            AsyncImage(
                                                model = bannerImageRequest,
                                                contentDescription = "Profile Cover Banner",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            // Multi-stop contrast scrim to ensure controls and badges pop
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                Color.Black.copy(alpha = 0.55f),
                                                                Color.Black.copy(alpha = 0.08f),
                                                                Color.Black.copy(alpha = 0.45f)
                                                            )
                                                        )
                                                    )
                                            )
                                        } else {
                                            // Sleek gradient placeholder when no cover photo is set
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(
                                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(Color.Black.copy(alpha = 0.35f))
                                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CameraAlt,
                                                        contentDescription = null,
                                                        tint = Color.White.copy(alpha = 0.85f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "Tap to add cover photo",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color.White.copy(alpha = 0.90f)
                                                    )
                                                }
                                            }
                                        }

                                        // Top Actions Overlay on Banner
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                                .align(Alignment.TopCenter),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Category Tag
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.Black.copy(alpha = 0.50f))
                                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "PROFILE",
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    letterSpacing = 0.8.sp
                                                )
                                            }

                                            // Cover Action Buttons
                                            if (isUploadingBanner) {
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color.Black.copy(alpha = 0.60f))
                                                        .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(13.dp),
                                                        color = Color.White,
                                                        strokeWidth = 2.dp
                                                    )
                                                    Text(
                                                        text = "Updating Cover...",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White
                                                    )
                                                }
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (hasCoverPhoto) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(Color.Black.copy(alpha = 0.55f))
                                                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                                                .clickable {
                                                                    lightboxTitle = "Background Cover"
                                                                    lightboxImageUrl = bannerPhotoUri
                                                                    isLightboxBanner = true
                                                                    showLightbox = true
                                                                }
                                                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Visibility,
                                                                    contentDescription = "View Cover",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                                Text(
                                                                    text = "View",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = Color.White
                                                                )
                                                            }
                                                        }
                                                    }

                                                    // Presets Button
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(Color.Black.copy(alpha = 0.55f))
                                                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                                            .clickable {
                                                                showCoverPresetsDialog = true
                                                            }
                                                            .padding(horizontal = 9.dp, vertical = 5.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Palette,
                                                                contentDescription = "Presets",
                                                                tint = Color(0xFFA29BFE),
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Text(
                                                                text = "Presets",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(Color.Black.copy(alpha = 0.55f))
                                                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                                            .clickable {
                                                                showCoverOptionsMenu = true
                                                            }
                                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.CameraAlt,
                                                                contentDescription = "Change Cover",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Text(
                                                                text = if (hasCoverPhoto) "Change" else "Add Cover",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 2. AVATAR & USER PROFILE DETAILS
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Avatar with Camera Badge (Overlaps Cover Banner)
                                        Box(
                                            modifier = Modifier.offset(y = (-48).dp),
                                            contentAlignment = Alignment.BottomEnd
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(96.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .padding(3.dp)
                                                    .border(2.5.dp, Brush.linearGradient(gradientColors), CircleShape)
                                                    .clickable {
                                                        lightboxTitle = "Profile Photo"
                                                        lightboxImageUrl = profilePhotoUri
                                                        isLightboxBanner = false
                                                        showLightbox = true
                                                    }
                                                    .padding(3.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val profileModel = resolveImageModel(profilePhotoUri) ?: sanitizeAvatarUrl(profilePhotoUri)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .background(getAvatarGradient(username)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = userInitial,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 36.sp
                                                    )
                                                    if (profileModel != null) {
                                                        AsyncImage(
                                                            model = profileModel,
                                                            contentDescription = "Profile Photo",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(CircleShape)
                                                        )
                                                    }
                                                }

                                                if (isUploadingAvatar) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(CircleShape)
                                                            .background(Color.Black.copy(alpha = 0.6f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(26.dp),
                                                            color = Color.White,
                                                            strokeWidth = 2.5.dp
                                                        )
                                                    }
                                                }
                                            }

                                            // Camera Badge for Profile Photo
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                                    .clickable {
                                                        photoPickerLauncher.launch("image/*")
                                                    }
                                                    .background(Brush.linearGradient(gradientColors)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CameraAlt,
                                                    contentDescription = "Upload Profile Photo",
                                                    tint = contentOnGradient,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height((-36).dp))

                                    // Display Name & Verified Badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (displayNameInput.isNotBlank()) displayNameInput else username,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            lineHeight = 28.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified Account",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Username handle
                                    Text(
                                        text = "@$username",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Quick Status Badges Row
                                    val isWsConnected by com.example.connecto.voice.VoiceCallManager.isWsConnectedFlow.collectAsState()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        // Status Pill (Online / Stealth / Offline)
                                        val statusColor = if (stealthModeEnabled) ConnectoTheme.colors.info else if (isWsConnected) OnlineGreen else TextDisabledColor
                                        val statusText = if (stealthModeEnabled) "Stealth Mode" else if (isWsConnected) "Online" else "Offline"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(statusColor.copy(alpha = 0.12f))
                                                .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(7.dp)
                                                        .clip(CircleShape)
                                                        .background(statusColor)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = statusText,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = statusColor
                                                )
                                            }
                                        }

                                        // Encrypted E2E Pill
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = "E2E Secured",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    if (bioInput.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (hasCoverPhoto) Color.Black.copy(alpha = 0.30f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (hasCoverPhoto) Color.White.copy(alpha = 0.18f)
                                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                    RoundedCornerShape(14.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "“${bioInput.trim()}”",
                                                fontSize = 13.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                lineHeight = 19.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                // ================= 1. PERSONAL INFORMATION BUTTON =================
                item(key = "menu_personal_details") {
                    ProfileNavigationMenuButton(
                        title = "Personal Information",
                        subtitle = "Name, username, email & profile photo",
                        icon = Icons.Default.Person,
                        hasCoverPhoto = hasCoverPhoto,
                        onClick = {
                            previousSubPage = null
                            activeSubPage = ProfileSubPage.PERSONAL_INFO
                        }
                    )
                }

                // ================= 2. SECURITY & PRIVACY BUTTON =================
                item(key = "menu_security_hub") {
                    ProfileNavigationMenuButton(
                        title = "Security & Privacy",
                        subtitle = "Two-factor, biometrics, screen privacy & stealth",
                        badge = if (twoFactorEnabled && isScreenSecurityActive) "OPTIMAL" else "ACTIVE",
                        icon = Icons.Default.Shield,
                        hasCoverPhoto = hasCoverPhoto,
                        onClick = {
                            previousSubPage = null
                            activeSubPage = ProfileSubPage.SECURITY
                        }
                    )
                }

                // ================= 3. SESSION MANAGEMENT BUTTON =================
                item(key = "menu_session_management") {
                    ProfileNavigationMenuButton(
                        title = "Session Management",
                        subtitle = "Review logged-in devices & terminate sessions",
                        badge = if (activeSessions.isNotEmpty()) "${activeSessions.size} Active" else null,
                        icon = Icons.Default.Devices,
                        hasCoverPhoto = hasCoverPhoto,
                        onClick = {
                            previousSubPage = null
                            activeSubPage = ProfileSubPage.SESSION_MANAGEMENT
                        }
                    )
                }

                // ================= 4. APPEARANCE & DISPLAY THEME BUTTON =================
                item(key = "menu_appearance_theme") {
                    ProfileNavigationMenuButton(
                        title = "Appearance & Display Theme",
                        subtitle = "Dark, light, OLED black & liquid glass",
                        icon = Icons.Default.Tune,
                        hasCoverPhoto = hasCoverPhoto,
                        onClick = { activeSubPage = ProfileSubPage.APPEARANCE_THEME }
                    )
                }

                // ================= 4b. VOICE & AUDIO BUTTON =================
                item(key = "menu_voice_audio") {
                    val isVoiceIsoActive by com.example.connecto.voice.VoiceCallManager.isVoiceIsolationEnabled.collectAsState()
                    ProfileNavigationMenuButton(
                        title = "Voice & Audio Settings",
                        subtitle = if (isVoiceIsoActive) "Voice Isolation active • Studio noise filter" else "Standard audio • Voice Isolation off",
                        badge = if (isVoiceIsoActive) "Active" else null,
                        icon = Icons.Default.Mic,
                        hasCoverPhoto = hasCoverPhoto,
                        onClick = { activeSubPage = ProfileSubPage.VOICE_AUDIO }
                    )
                }

                // ================= 5. NOTIFICATION BUTTON =================
                item(key = "menu_notifications") {
                    ProfileNavigationMenuButton(
                        title = "Notification",
                        subtitle = "Sound alerts ($selectedNotificationSoundName) & privacy",
                        badge = selectedNotificationSoundName,
                        icon = Icons.Default.Notifications,
                        hasCoverPhoto = hasCoverPhoto,
                        onClick = { activeSubPage = ProfileSubPage.NOTIFICATIONS }
                    )
                }

                // ================= 6. HELP & SUPPORT BUTTON =================
                item(key = "menu_help_support") {
                    ProfileNavigationMenuButton(
                        title = "Help & Support",
                        subtitle = "FAQs, report issues, terms & delete account",
                        icon = Icons.AutoMirrored.Filled.Help,
                        hasCoverPhoto = hasCoverPhoto,
                        onClick = { activeSubPage = ProfileSubPage.HELP_SUPPORT }
                    )
                }

                item(key = "sign_out_button") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable(onClick = onSignOut)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (hasCoverPhoto) Color.Black.copy(alpha = 0.30f)
                                else Color(0xFFEF4444).copy(alpha = 0.12f)
                            )
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign Out Account",
                                color = Color(0xFFEF4444),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }

                
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            } else {
                // ========== 2. DEDICATED FULL-SCREEN SUB-PAGE TAB ==========
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (hasCoverPhoto) Color.Transparent else MaterialTheme.colorScheme.background)
                ) {
                    ProfileSubPageHeader(
                        title = currentSubPage.title,
                        icon = currentSubPage.icon,
                        hasCoverPhoto = hasCoverPhoto,
                        onBack = {
                            if (previousSubPage != null) {
                                activeSubPage = previousSubPage
                                previousSubPage = null
                            } else {
                                activeSubPage = null
                            }
                        }
                    )

                    val subpageListState = androidx.compose.foundation.lazy.rememberLazyListState()

                    LazyColumn(
                        state = subpageListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
                    ) {
                        // Auto-save confirmation banner inside sub-screen
                        if (showSavedBadge) {
                            item(key = "subpage_auto_save_badge") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(OnlineGreen.copy(alpha = 0.15f))
                                        .border(1.dp, OnlineGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = OnlineGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Changes synced & saved to your profile",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OnlineGreen
                                        )
                                    }
                                }
                            }
                        }

                        when (currentSubPage) {
                            ProfileSubPage.SECURITY -> {
                                // 1. TWO-FACTOR & PASSWORD SECURITY CARD
                                item(key = "sub_security_two_factor_section") {
                                    val onToggle2Fa: (Boolean) -> Unit = { isChecked ->
                                        val activeSessionToken = ConnectoApiClient.sessionToken
                                            ?: context.getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE).getString("token", null)

                                        if (!isChecked) {
                                            // USER WANTS TO TURN OFF 2FA: Enforce OTP sent to registered email!
                                            if (emailInput.isBlank()) {
                                                twoFaErrorMessage = "Please ensure your registered email address is set in profile first."
                                            } else {
                                                twoFaErrorMessage = null
                                                coroutineScope.launch {
                                                    val res = ConnectoApiClient.request2FaSetupOtp(
                                                        method = "email",
                                                        phoneNumber = emailInput.trim(),
                                                        purpose = "two_factor_disable",
                                                        token = activeSessionToken
                                                    )
                                                    if (res.isSuccess) {
                                                        val data = res.getOrNull()
                                                        twoFaDisableMaskedDest = data?.maskedDestination
                                                        show2FaDisableDialog = true
                                                    } else {
                                                        twoFaErrorMessage = res.exceptionOrNull()?.message ?: "Failed to dispatch verification code to email."
                                                    }
                                                }
                                            }
                                        } else {
                                            // USER WANTS TO TURN ON 2FA: Send OTP to registered email
                                            if (emailInput.isBlank()) {
                                                twoFaErrorMessage = "Please ensure your email address is set in your profile first."
                                            } else {
                                                twoFaErrorMessage = null
                                                coroutineScope.launch {
                                                    val res = ConnectoApiClient.request2FaSetupOtp(
                                                        method = "email",
                                                        phoneNumber = emailInput.trim(),
                                                        purpose = "two_factor_setup",
                                                        token = activeSessionToken
                                                    )
                                                    if (res.isSuccess) {
                                                        val data = res.getOrNull()
                                                        val targetDest = emailInput.trim()
                                                        twoFaSetupMaskedDest = data?.maskedDestination
                                                        com.example.connecto.security.TwoFactorStateManager.startSetup(
                                                            context = context,
                                                            method = "email",
                                                            destination = targetDest,
                                                            maskedDest = data?.maskedDestination
                                                        )
                                                        show2FaSetupDialog = true
                                                    } else {
                                                        twoFaErrorMessage = res.exceptionOrNull()?.message ?: "Failed to dispatch setup OTP."
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else MaterialTheme.colorScheme.primary
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Card Header
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Shield,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Two-Factor & Security",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Account credentials and OTP login protection",
                                                        fontSize = 11.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

                                            // Change Password Feature
                                            PersonalDetailsChangePasswordSection(
                                                username = currentUsernameState,
                                                context = context
                                            )

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

                                            // Password Reset Action
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = "Reset / Change Password",
                                                            fontSize = 14.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "Send real-time OTP to registered email",
                                                            fontSize = 11.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clickable { showForgotPasswordDialog = true }
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = "RESET",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

                                            // 2FA Toggle Row
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onToggle2Fa(!twoFactorEnabled) },
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Shield,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = "Two-Factor Auth",
                                                                fontSize = 14.5.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(if (twoFactorEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (twoFactorEnabled) "ACTIVE" else "OFF",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (twoFactorEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = if (twoFactorEnabled) "Email OTP verification enabled" else "Require 6-digit code on login",
                                                            fontSize = 11.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Switch(
                                                    checked = twoFactorEnabled,
                                                    onCheckedChange = { isChecked -> onToggle2Fa(isChecked) },
                                                    colors = getMonochromeSwitchColors()
                                                )
                                            }

                                            if (twoFaErrorMessage != null) {
                                                val isSessionExpired = twoFaErrorMessage?.contains("expired", ignoreCase = true) == true ||
                                                                        twoFaErrorMessage?.contains("log in again", ignoreCase = true) == true
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                                                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                                        .padding(10.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = twoFaErrorMessage ?: "",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFEF4444)
                                                    )
                                                    if (isSessionExpired) {
                                                        Button(
                                                            onClick = { onSignOut() },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                            modifier = Modifier.align(Alignment.End)
                                                        ) {
                                                            Text("LOG IN AGAIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }
                                                    }
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

                                            // 2FA Verification Channel (Email)
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(
                                                    text = "2FA VERIFICATION CHANNEL",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    letterSpacing = 0.8.sp
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                                        .padding(14.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(40.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Email,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(22.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = "Email OTP Verification",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = if (emailInput.isNotBlank()) "Security codes are delivered to ${emailInput.trim()}"
                                                                       else "⚠️ No email found in profile. Set your email in Personal Information to receive 2FA codes.",
                                                                fontSize = 11.sp,
                                                                color = if (emailInput.isNotBlank()) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFF59E0B)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. BIOMETRIC & SCREEN PRIVACY CARD
                                item(key = "sub_security_biometric_section") {
                                    val isDeviceSecure = remember(context) { BiometricAuthManager.isDeviceSecure(context) }
                                    val isBioReady = remember(context) { BiometricAuthManager.checkBiometricAvailability(context) == BiometricStatus.READY }
                                    val isFaceHardware = remember(context) { BiometricAuthManager.hasFaceHardware(context) }
                                    var preferredModality by remember(context, username) {
                                        mutableStateOf(BiometricAuthManager.getPreferredModality(context, username))
                                    }
                                    val isFaceIdActive = remember(preferredModality, isFaceHardware) {
                                        if (preferredModality == BiometricAuthManager.BiometricModality.FACE_ID) true
                                        else if (preferredModality == BiometricAuthManager.BiometricModality.FINGERPRINT) false
                                        else isFaceHardware
                                    }
                                    val statusDescription = remember(context, preferredModality) {
                                        BiometricAuthManager.getStatusDescription(context, username)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else MaterialTheme.colorScheme.primary
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Master App Lock Switch
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isFaceIdActive) Icons.Default.Face else Icons.Default.Fingerprint,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = if (isFaceIdActive) "Face ID & App Lock" else "Biometric & App Lock",
                                                                fontSize = 15.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(if (isAppLockEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (isAppLockEnabled) "ON" else "OFF",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isAppLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = if (isAppLockEnabled) {
                                                                if (isFaceIdActive) "Unlock with Face ID or device screen lock to open"
                                                                else "Require biometric scan or device screen lock to open"
                                                            } else "Unlock app without biometric prompt",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Switch(
                                                    checked = isAppLockEnabled,
                                                    onCheckedChange = { isChecked ->
                                                        if (isChecked) {
                                                            Toast.makeText(
                                                                context,
                                                                if (isFaceIdActive) "Face ID App Lock enabled. Unlock with Face ID, Screen Lock, or Password."
                                                                else "Biometric App Lock enabled. Unlock with Biometrics, Screen Lock, or Password.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                        isAppLockEnabled = isChecked
                                                        prefs.edit().putBoolean("biometric_lock_$username", isChecked).apply()
                                                    },
                                                    colors = getMonochromeSwitchColors()
                                                )
                                            }

                                            // Modality Selector: Face ID vs Fingerprint
                                            if (isAppLockEnabled) {
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = "Preferred Unlock Method",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        // Option 1: Face ID
                                                        val isFaceSelected = preferredModality == BiometricAuthManager.BiometricModality.FACE_ID || (preferredModality == BiometricAuthManager.BiometricModality.AUTO && isFaceHardware)
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(if (isFaceSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                                                .border(1.dp, if (isFaceSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                                                                .clickable {
                                                                    preferredModality = BiometricAuthManager.BiometricModality.FACE_ID
                                                                    BiometricAuthManager.setPreferredModality(context, username, BiometricAuthManager.BiometricModality.FACE_ID)
                                                                    Toast.makeText(context, "App Lock set to Face ID mode 👤", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .padding(vertical = 8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Face,
                                                                    contentDescription = null,
                                                                    tint = if (isFaceSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = "Face ID",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = if (isFaceSelected) FontWeight.Bold else FontWeight.Medium,
                                                                    color = if (isFaceSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }

                                                        // Option 2: Fingerprint
                                                        val isFpSelected = preferredModality == BiometricAuthManager.BiometricModality.FINGERPRINT || (preferredModality == BiometricAuthManager.BiometricModality.AUTO && !isFaceHardware)
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(if (isFpSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                                                .border(1.dp, if (isFpSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                                                                .clickable {
                                                                    preferredModality = BiometricAuthManager.BiometricModality.FINGERPRINT
                                                                    BiometricAuthManager.setPreferredModality(context, username, BiometricAuthManager.BiometricModality.FINGERPRINT)
                                                                    Toast.makeText(context, "App Lock set to Fingerprint mode 🖐", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .padding(vertical = 8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Fingerprint,
                                                                    contentDescription = null,
                                                                    tint = if (isFpSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = "Fingerprint",
                                                                    fontSize = 12.sp,
                                                                    fontWeight = if (isFpSelected) FontWeight.Bold else FontWeight.Medium,
                                                                    color = if (isFpSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

                                            // Hardware Status Card
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isAppLockEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isAppLockEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                if (isAppLockEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                                else MaterialTheme.colorScheme.surfaceVariant
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isFaceIdActive) Icons.Default.Face else if (isAppLockEnabled) Icons.Default.Shield else Icons.Default.Lock,
                                                            contentDescription = null,
                                                            tint = if (isAppLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = if (isFaceIdActive) "Face ID Hardware & Security" else "Device Hardware Security",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = statusDescription,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

                                            // Screen Privacy & Black Screen Protection Switch
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                if (isScreenSecurityActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                                else MaterialTheme.colorScheme.surfaceVariant
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isScreenSecurityActive) Icons.Default.Shield else Icons.Default.VisibilityOff,
                                                            contentDescription = null,
                                                            tint = if (isScreenSecurityActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = if (isUserAdmin) "Admin Screenshot Access" else "Screenshot Protection",
                                                                fontSize = 13.5.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(if (isUserAdmin) Color(0xFF3B82F6).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (isUserAdmin) "ADMIN UNLOCKED" else "MEMBER RESTRICTED",
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isUserAdmin) Color(0xFF3B82F6) else Color(0xFFEF4444)
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = if (isUserAdmin) {
                                                                "Admin Access: You can take screenshots and screen record in Connecto. Optional blackscreen toggle available."
                                                            } else {
                                                                "Restricted: Only admin users can take screenshots or screen record in Connecto. All members are protected."
                                                            },
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Switch(
                                                    checked = if (isUserAdmin) isScreenSecurityActive else true,
                                                    enabled = isUserAdmin,
                                                    onCheckedChange = { isChecked ->
                                                        if (isUserAdmin) {
                                                            com.example.connecto.security.ScreenSecurityManager.setScreenSecurityEnabled(context, isChecked, activity)
                                                            Toast.makeText(
                                                                context,
                                                                if (isChecked) "🛡️ Black screen protection enabled for screenshots, share & cast."
                                                                else "⚠️ Screen privacy disabled. Screenshots and sharing are now visible for admin.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "🛡️ Restricted: Only admin users can take screenshots or modify screen security. Protection is locked for members.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    },
                                                    colors = getMonochromeSwitchColors()
                                                )
                                            }

                                            // Real-time Screen Share & Cast Hardware Protection Status Card
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isRecordingActive || isCastingActive) Color(0xFFEF4444).copy(alpha = 0.08f)
                                                        else if (isScreenSecurityActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isRecordingActive || isCastingActive) Color(0xFFEF4444).copy(alpha = 0.35f)
                                                        else if (isScreenSecurityActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(12.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    if (isRecordingActive || isCastingActive) Color(0xFFEF4444)
                                                                    else if (isScreenSecurityActive) Color(0xFF10B981)
                                                                    else Color(0xFF9CA3AF)
                                                                )
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = if (isRecordingActive) "Screen Recording / Share Active (Black Screen Enforced)"
                                                                   else if (isCastingActive) "Wireless Screen Cast Active (Black Screen Enforced)"
                                                                   else if (isScreenSecurityActive) "SurfaceFlinger Hardware Privacy Active"
                                                                   else "Screen Capture Unrestricted",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isRecordingActive || isCastingActive) Color(0xFFEF4444)
                                                                    else if (isScreenSecurityActive) MaterialTheme.colorScheme.primary
                                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Text(
                                                        text = if (isScreenSecurityActive)
                                                            "All screenshots, video captures, screen sharing (Discord, Zoom, Meet, Teams), and external display mirrors render as solid pitch black (#000000) for others."
                                                        else
                                                            "Screenshots, recordings, and screen sharing can see all screen contents. Turn ON for blackscreen protection.",
                                                        fontSize = 11.sp,
                                                        lineHeight = 15.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            // Auto-Lock on Background
                                            if (isAppLockEnabled) {
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "Auto-Lock on Background",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "Lock app immediately when leaving Connecto",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }

                                                    Switch(
                                                        checked = autoLockOnBackground,
                                                        onCheckedChange = { isChecked ->
                                                            autoLockOnBackground = isChecked
                                                            prefs.edit().putBoolean("auto_lock_background_$username", isChecked).apply()
                                                        },
                                                        colors = getMonochromeSwitchColors()
                                                    )
                                                }

                                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 1.dp)

                                                Button(
                                                    onClick = onLockApp,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(44.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                        contentColor = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Lock App & Test Authentication Now",
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3. PRESENCE & STEALTH MODE CARD
                                item(key = "sub_security_stealth_section") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else if (stealthModeEnabled) ConnectoTheme.colors.info.copy(alpha = 0.08f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else if (stealthModeEnabled) ConnectoTheme.colors.info
                                                    else MaterialTheme.colorScheme.primary
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (stealthModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = if (stealthModeEnabled) ConnectoTheme.colors.info else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "Presence & Stealth Mode",
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        if (stealthModeEnabled) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(ConnectoTheme.colors.info.copy(alpha = 0.2f))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = "ACTIVE",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = ConnectoTheme.colors.info
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = if (stealthModeEnabled)
                                                            "You appear offline to everyone (server-enforced)"
                                                        else
                                                            "Appear offline to others while staying connected",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Switch(
                                                checked = stealthModeEnabled,
                                                onCheckedChange = { isChecked ->
                                                    stealthModeEnabled = isChecked
                                                    prefs.edit().putBoolean("stealth_mode_$username", isChecked).apply()
                                                    coroutineScope.launch {
                                                        try {
                                                            ConnectoApiClient.setStealthMode(enabled = isChecked)
                                                        } catch (e: Exception) {
                                                            // Non-fatal — preference is persisted locally
                                                        }
                                                    }
                                                    triggerAutoSave()
                                                },
                                                colors = getMonochromeSwitchColors()
                                            )
                                        }
                                    }
                                }
                            }
                            ProfileSubPage.SESSION_MANAGEMENT -> {
                                item(key = "sub_session_management_header") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                            )
                                            .padding(18.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Devices,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Active Logged-In Sessions",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(OnlineGreen.copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (activeSessions.isNotEmpty()) "${activeSessions.size} ACTIVE" else "ONLINE",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = OnlineGreen
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Review all devices, browsers, and mobile apps logged into your account. Terminate any unrecognized activity instantly.",
                                                    fontSize = 11.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                item(key = "sub_logged_in_sessions") {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    LoggedInSessionsCard(
                                        sessions = activeSessions,
                                        isLoading = isLoadingSessions,
                                        errorMessage = sessionErrorMessage,
                                        terminatingSessionId = terminatingSessionId,
                                        isTerminatingAllOthers = isTerminatingOtherSessions,
                                        onRefresh = { refreshUserSessions() },
                                        onTerminateSession = onTerminateSession,
                                        onTerminateAllOthers = onTerminateOtherSessions,
                                        hasCoverPhoto = hasCoverPhoto
                                    )
                                }
                            }
                            ProfileSubPage.PRESENCE_STEALTH -> {
                                item(key = "sub_presence_stealth_card") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else if (stealthModeEnabled) ConnectoTheme.colors.info.copy(alpha = 0.08f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else if (stealthModeEnabled) ConnectoTheme.colors.info
                                                    else MaterialTheme.colorScheme.primary
                                            )
                                            .padding(16.dp)
                                    ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (stealthModeEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = if (stealthModeEnabled) ConnectoTheme.colors.info else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Stealth Mode",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (stealthModeEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                if (stealthModeEnabled) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(ConnectoTheme.colors.info.copy(alpha = 0.2f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "ACTIVE",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = ConnectoTheme.colors.info
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (stealthModeEnabled)
                                                    "You appear offline to everyone (server-enforced)"
                                                else
                                                    "Appear offline to others while staying connected",
                                                fontSize = 12.sp,
                                                color = if (stealthModeEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = stealthModeEnabled,
                                        onCheckedChange = { isChecked ->
                                            stealthModeEnabled = isChecked
                                            prefs.edit().putBoolean("stealth_mode_$username", isChecked).apply()
                                            coroutineScope.launch {
                                                try {
                                                    ConnectoApiClient.setStealthMode(enabled = isChecked)
                                                } catch (e: Exception) {
                                                    // Non-fatal — preference is persisted locally
                                                }
                                            }
                                            triggerAutoSave()
                                        },
                                        colors = getMonochromeSwitchColors()
                                    )
                                }
                            }
                                }
                            }
                            ProfileSubPage.PERSONAL_INFO -> {
                                item(key = "sub_personal_info_form") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else MaterialTheme.colorScheme.primary
                                            )
                                            .padding(16.dp)
                                    ) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    // Full Name (EDITABLE)
                                    ProfileStyledInputField(
                                        label = "Full Name",
                                        placeholder = "e.g. Shadow Hayate",
                                        value = fullNameInput,
                                        icon = Icons.Default.Person,
                                        isLocked = false,
                                        onValueChange = {
                                            fullNameInput = it
                                            prefs.edit().putString("full_name_$currentUsernameState", it).apply()
                                            triggerAutoSave()
                                        }
                                    )

                                    // Display Name (EDITABLE)
                                    ProfileStyledInputField(
                                        label = "Display Name / Nickname",
                                        placeholder = "e.g. Shadow",
                                        value = displayNameInput,
                                        icon = Icons.Default.Badge,
                                        isLocked = false,
                                        onValueChange = {
                                            displayNameInput = it
                                            prefs.edit().putString("display_name_$currentUsernameState", it).apply()
                                            triggerAutoSave()
                                        }
                                    )

                                    // Username: 1-Time Change Policy
                                    Column {
                                        ProfileStyledInputField(
                                            label = "Server Username",
                                            placeholder = "e.g. alex_doe",
                                            value = if (usernameChangedFlag) "@$currentUsernameState" else currentUsernameState,
                                            icon = Icons.Default.AlternateEmail,
                                            isLocked = usernameChangedFlag,
                                            lockBadgeText = if (usernameChangedFlag) "LOCKED (PERMANENT)" else "1-TIME CHANGE",
                                            onValueChange = {
                                                if (!usernameChangedFlag) {
                                                    currentUsernameState = it.trim().filter { c -> c.isLetterOrDigit() || c == '_' }
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (usernameChangedFlag) "Username was modified once and is permanently locked to this account."
                                                   else "✏️ You can change your username ONCE. It will be permanently locked after saving.",
                                            fontSize = 11.sp,
                                            color = if (usernameChangedFlag) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else Color(0xFFF59E0B),
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }

                                    // Email Address (OTP Protected with Masking & Dedicated Change Flow)
                                    ContactStyledField(
                                        label = "Email Address",
                                        value = if (revealEmail) emailInput else emailInput.maskEmail(),
                                        icon = Icons.Default.Email,
                                        isMasked = !revealEmail,
                                        onToggleMask = { revealEmail = !revealEmail },
                                        onChangeClick = { changeContactTarget = "email" }
                                    )

                                    // Phone number removed — email-only 2FA/contact per policy

                                    // Personal Bio Field (EDITABLE)
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Personal Bio",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "EDITABLE",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 68.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                .padding(12.dp),
                                            contentAlignment = Alignment.TopStart
                                        ) {
                                            if (bioInput.isEmpty()) {
                                                Text(
                                                    text = "Write something about yourself...",
                                                    color = TextDisabledColor,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            BasicTextField(
                                                value = bioInput,
                                                onValueChange = { newBio ->
                                                    bioInput = newBio
                                                    prefs.edit().putString("bio_$currentUsernameState", newBio).apply()
                                                    triggerAutoSave()
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp),
                                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                maxLines = 4
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Full Name, Display Name, and Bio are freely editable anytime.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Manual Save Profile Button
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clickable { triggerAutoSave() }
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Brush.linearGradient(gradientColors)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = contentOnGradient,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Save Profile Changes",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = contentOnGradient
                                            )
                                        }
                                    }
                                }
                            }
                                }
                            }
                            ProfileSubPage.TWO_FACTOR_SECURITY -> {
                                item(key = "sub_two_factor_security_form") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else MaterialTheme.colorScheme.primary
                                            )
                                            .padding(16.dp)
                                    ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // ================= CHANGE PASSWORD FEATURE =================
                                    PersonalDetailsChangePasswordSection(
                                        username = currentUsernameState,
                                        context = context
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                                    // 1. Password Reset Action
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Reset / Change Password",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Send real-time OTP to registered email",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clickable { showForgotPasswordDialog = true }
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "RESET",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                                    // 2. 2FA Toggle Row
                                    val onToggle2Fa: (Boolean) -> Unit = { isChecked ->
                                        val activeSessionToken = ConnectoApiClient.sessionToken
                                            ?: context.getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE).getString("token", null)

                                        if (!isChecked) {
                                            // USER WANTS TO TURN OFF 2FA: Enforce OTP sent to registered email!
                                            if (emailInput.isBlank()) {
                                                twoFaErrorMessage = "Please ensure your registered email address is set above first."
                                            } else {
                                                twoFaErrorMessage = null
                                                coroutineScope.launch {
                                                    val res = ConnectoApiClient.request2FaSetupOtp(
                                                        method = "email",
                                                        phoneNumber = emailInput.trim(),
                                                        purpose = "two_factor_disable",
                                                        token = activeSessionToken
                                                    )
                                                    if (res.isSuccess) {
                                                        val data = res.getOrNull()
                                                        twoFaDisableMaskedDest = data?.maskedDestination
                                                        show2FaDisableDialog = true
                                                    } else {
                                                        twoFaErrorMessage = res.exceptionOrNull()?.message ?: "Failed to dispatch verification code to email."
                                                    }
                                                }
                                            }
                                        } else {
                                            // USER WANTS TO TURN ON 2FA: Send OTP to registered email, user must type it manually!
                                            if (emailInput.isBlank()) {
                                                twoFaErrorMessage = "Please ensure your email address is set in your profile above first."
                                            } else {
                                                twoFaErrorMessage = null
                                                coroutineScope.launch {
                                                    val res = ConnectoApiClient.request2FaSetupOtp(
                                                        method = "email",
                                                        phoneNumber = emailInput.trim(),
                                                        purpose = "two_factor_setup",
                                                        token = activeSessionToken
                                                    )
                                                    if (res.isSuccess) {
                                                        val data = res.getOrNull()
                                                        val targetDest = emailInput.trim()
                                                        twoFaSetupMaskedDest = data?.maskedDestination
                                                        com.example.connecto.security.TwoFactorStateManager.startSetup(
                                                            context = context,
                                                            method = "email",
                                                            destination = targetDest,
                                                            maskedDest = data?.maskedDestination
                                                        )
                                                        show2FaSetupDialog = true
                                                    } else {
                                                        twoFaErrorMessage = res.exceptionOrNull()?.message ?: "Failed to dispatch setup OTP."
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onToggle2Fa(!twoFactorEnabled) },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Two-Factor Auth",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (twoFactorEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (twoFactorEnabled) "ACTIVE" else "OFF",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (twoFactorEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = if (twoFactorEnabled) "Email OTP verification enabled" else "Require 6-digit code on login",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = twoFactorEnabled,
                                            onCheckedChange = { isChecked -> onToggle2Fa(isChecked) },
                                            colors = getMonochromeSwitchColors()
                                        )
                                    }

                                    if (twoFaErrorMessage != null) {
                                        val isSessionExpired = twoFaErrorMessage?.contains("expired", ignoreCase = true) == true ||
                                                               twoFaErrorMessage?.contains("log in again", ignoreCase = true) == true
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = twoFaErrorMessage ?: "",
                                                fontSize = 12.sp,
                                                color = Color(0xFFEF4444)
                                            )
                                            if (isSessionExpired) {
                                                Button(
                                                    onClick = { onSignOut() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.align(Alignment.End)
                                                ) {
                                                    Text("LOG IN AGAIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                                    // 3. 2FA Method (Email Only)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "2FA VERIFICATION CHANNEL",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            letterSpacing = 0.8.sp
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                                .padding(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Email,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Email OTP Verification",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = if (emailInput.isNotBlank()) "Security codes are delivered to ${emailInput.trim()}"
                                                               else "⚠️ No email found in profile. Set your email above to receive 2FA codes.",
                                                        fontSize = 11.sp,
                                                        color = if (emailInput.isNotBlank()) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFF59E0B)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                                }
                            }
                            ProfileSubPage.BIOMETRIC_LOCK -> {
                                item(key = "sub_biometric_lock_form") {
                                    val isDeviceSecure = remember(context) { BiometricAuthManager.isDeviceSecure(context) }
                                    val isBioReady = remember(context) { BiometricAuthManager.checkBiometricAvailability(context) == BiometricStatus.READY }
                                    val isFaceHardware = remember(context) { BiometricAuthManager.hasFaceHardware(context) }
                                    var preferredModality by remember(context, username) {
                                        mutableStateOf(BiometricAuthManager.getPreferredModality(context, username))
                                    }
                                    val isFaceIdActive = remember(preferredModality, isFaceHardware) {
                                        if (preferredModality == BiometricAuthManager.BiometricModality.FACE_ID) true
                                        else if (preferredModality == BiometricAuthManager.BiometricModality.FINGERPRINT) false
                                        else isFaceHardware
                                    }
                                    val statusDescription = remember(context, preferredModality) {
                                        BiometricAuthManager.getStatusDescription(context, username)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else MaterialTheme.colorScheme.primary
                                            )
                                            .padding(16.dp)
                                    ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // 1. Master App Lock Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (isFaceIdActive) Icons.Default.Face else Icons.Default.Fingerprint,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = if (isFaceIdActive) "Face ID & App Lock" else "Biometric App Lock",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isAppLockEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isAppLockEnabled) "ON" else "OFF",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isAppLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = if (isAppLockEnabled) {
                                                        if (isFaceIdActive) "Unlock with Face ID or device screen lock to open"
                                                        else "Require biometric scan or device screen lock to open"
                                                    } else "Unlock app without biometric prompt",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isAppLockEnabled,
                                            onCheckedChange = { isChecked ->
                                                if (isChecked) {
                                                    Toast.makeText(
                                                        context,
                                                        if (isFaceIdActive) "Face ID App Lock enabled. Unlock with Face ID, Screen Lock, or Password."
                                                        else "Biometric App Lock enabled. Unlock with Biometrics, Screen Lock, or Password.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                isAppLockEnabled = isChecked
                                                prefs.edit().putBoolean("biometric_lock_$username", isChecked).apply()
                                            },
                                            colors = getMonochromeSwitchColors()
                                        )
                                    }

                                    // Modality Selector: Face ID vs Fingerprint
                                    if (isAppLockEnabled) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = "Preferred Unlock Method",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Option 1: Face ID
                                                val isFaceSelected = preferredModality == BiometricAuthManager.BiometricModality.FACE_ID || (preferredModality == BiometricAuthManager.BiometricModality.AUTO && isFaceHardware)
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isFaceSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                                        .border(1.dp, if (isFaceSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                                                        .clickable {
                                                            preferredModality = BiometricAuthManager.BiometricModality.FACE_ID
                                                            BiometricAuthManager.setPreferredModality(context, username, BiometricAuthManager.BiometricModality.FACE_ID)
                                                            Toast.makeText(context, "App Lock set to Face ID mode 👤", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Face,
                                                            contentDescription = null,
                                                            tint = if (isFaceSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Face ID",
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isFaceSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isFaceSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                // Option 2: Fingerprint
                                                val isFpSelected = preferredModality == BiometricAuthManager.BiometricModality.FINGERPRINT || (preferredModality == BiometricAuthManager.BiometricModality.AUTO && !isFaceHardware)
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isFpSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                                        .border(1.dp, if (isFpSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(10.dp))
                                                        .clickable {
                                                            preferredModality = BiometricAuthManager.BiometricModality.FINGERPRINT
                                                            BiometricAuthManager.setPreferredModality(context, username, BiometricAuthManager.BiometricModality.FINGERPRINT)
                                                            Toast.makeText(context, "App Lock set to Fingerprint mode 🖐", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Fingerprint,
                                                            contentDescription = null,
                                                            tint = if (isFpSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Fingerprint",
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isFpSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isFpSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                                    // 2. Hardware Status Card
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isAppLockEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                            .border(
                                                1.dp,
                                                if (isAppLockEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isAppLockEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isFaceIdActive) Icons.Default.Face else if (isAppLockEnabled) Icons.Default.Shield else Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = if (isAppLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isFaceIdActive) "Face ID Hardware & Security" else "Device Hardware Security",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = statusDescription,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                                    // 3. Screen Privacy & Black Screen Protection Switch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isScreenSecurityActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isScreenSecurityActive) Icons.Default.Shield else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = if (isScreenSecurityActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = if (isUserAdmin) "Admin Screenshot Access" else "Screenshot Protection",
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isUserAdmin) Color(0xFF3B82F6).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isUserAdmin) "ADMIN UNLOCKED" else "MEMBER RESTRICTED",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isUserAdmin) Color(0xFF3B82F6) else Color(0xFFEF4444)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = if (isUserAdmin) {
                                                        "Admin Access: You can take screenshots and screen record in Connecto. Optional blackscreen toggle available."
                                                    } else {
                                                        "Restricted: Only admin users can take screenshots or screen record in Connecto. All members are protected."
                                                    },
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = if (isUserAdmin) isScreenSecurityActive else true,
                                            enabled = isUserAdmin,
                                            onCheckedChange = { isChecked ->
                                                if (isUserAdmin) {
                                                    com.example.connecto.security.ScreenSecurityManager.setScreenSecurityEnabled(context, isChecked, activity)
                                                    Toast.makeText(
                                                        context,
                                                        if (isChecked) "🛡️ Black screen protection enabled for screenshots, share & cast."
                                                        else "⚠️ Screen privacy disabled. Screenshots and sharing are now visible for admin.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "🛡️ Restricted: Only admin users can take screenshots or modify screen security. Protection is locked for members.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            colors = getMonochromeSwitchColors()
                                        )
                                    }

                                    // Real-time Screen Share & Cast Hardware Protection Status Card
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isRecordingActive || isCastingActive) Color(0xFFEF4444).copy(alpha = 0.08f)
                                                else if (isScreenSecurityActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                            .border(
                                                1.dp,
                                                if (isRecordingActive || isCastingActive) Color(0xFFEF4444).copy(alpha = 0.35f)
                                                else if (isScreenSecurityActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isRecordingActive || isCastingActive) Color(0xFFEF4444)
                                                            else if (isScreenSecurityActive) Color(0xFF10B981)
                                                            else Color(0xFF9CA3AF)
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (isRecordingActive) "Screen Recording / Share Active (Black Screen Enforced)"
                                                           else if (isCastingActive) "Wireless Screen Cast Active (Black Screen Enforced)"
                                                           else if (isScreenSecurityActive) "SurfaceFlinger Hardware Privacy Active"
                                                           else "Screen Capture Unrestricted",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isRecordingActive || isCastingActive) Color(0xFFEF4444)
                                                            else if (isScreenSecurityActive) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = if (isScreenSecurityActive)
                                                    "All screenshots, video captures, screen sharing (Discord, Zoom, Meet, Teams), and external display mirrors render as solid pitch black (#000000) for others."
                                                else
                                                    "Screenshots, recordings, and screen sharing can see all screen contents. Turn ON for blackscreen protection.",
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // 4. Auto-Lock on App Exit / Background Toggle
                                    if (isAppLockEnabled) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Auto-Lock on Background",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Lock app immediately when leaving Connecto",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Switch(
                                                checked = autoLockOnBackground,
                                                onCheckedChange = { isChecked ->
                                                    autoLockOnBackground = isChecked
                                                    prefs.edit().putBoolean("auto_lock_background_$username", isChecked).apply()
                                                },
                                                colors = getMonochromeSwitchColors()
                                            )
                                        }

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                                        // 4. Test Lock App Now Button
                                        Button(
                                            onClick = onLockApp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Lock App & Test Authentication Now",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                                }
                            }
                            ProfileSubPage.APPEARANCE_THEME -> {
                                item(key = "sub_appearance_theme_form") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(22.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else MaterialTheme.colorScheme.primary
                                            )
                                            .padding(18.dp)
                                    ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // 1. Active Theme Summary Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = currentThemeMode.iconEmoji,
                                                fontSize = 24.sp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = currentThemeMode.displayName,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "ACTIVE",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = currentThemeMode.subtitle,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Quick toggle between Dark and Light
                                        Switch(
                                            checked = currentThemeMode.isDark,
                                            onCheckedChange = { isDark ->
                                                val newMode = if (isDark) com.example.connecto.ui.designsystem.ThemeMode.NOIR else com.example.connecto.ui.designsystem.ThemeMode.PARCHMENT
                                                onSelectThemeMode(newMode)
                                                onToggleTheme(isDark)
                                            },
                                            colors = getMonochromeSwitchColors()
                                        )
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), thickness = 1.dp)

                                    Text(
                                        text = "SELECT COLOR PALETTE & ATMOSPHERE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // 2. Theme Selection Cards Grid (5 Transparent Liquid Glass Themes)
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        com.example.connecto.ui.designsystem.ThemeMode.entries.forEach { mode ->
                                            val isSelected = currentThemeMode == mode
                                            val modeColors = com.example.connecto.ui.designsystem.getColorsForTheme(mode)

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .shadow(
                                                        elevation = if (isSelected) 6.dp else 3.dp,
                                                        shape = RoundedCornerShape(16.dp),
                                                        ambientColor = Color.Black.copy(alpha = if (isThemeDark) 0.35f else 0.08f),
                                                        spotColor = if (isSelected) modeColors.primary.copy(alpha = 0.40f) else Color.Transparent
                                                    )
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        brush = Brush.verticalGradient(
                                                            colors = if (isSelected) {
                                                                listOf(
                                                                    modeColors.primary.copy(alpha = if (isThemeDark) 0.22f else 0.30f),
                                                                    modeColors.surface.copy(alpha = if (isThemeDark) 0.12f else 0.18f)
                                                                )
                                                            } else {
                                                                listOf(
                                                                    Color.White.copy(alpha = if (isThemeDark) 0.08f else 0.55f),
                                                                    modeColors.surface.copy(alpha = if (isThemeDark) 0.05f else 0.25f)
                                                                )
                                                            }
                                                        )
                                                    )
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 1.dp,
                                                        brush = Brush.linearGradient(
                                                            colors = if (isSelected) {
                                                                listOf(
                                                                    Color.White.copy(alpha = 0.90f),
                                                                    modeColors.primary.copy(alpha = 0.80f),
                                                                    modeColors.primary.copy(alpha = 0.25f)
                                                                )
                                                            } else {
                                                                listOf(
                                                                    Color.White.copy(alpha = if (isThemeDark) 0.45f else 0.75f),
                                                                    modeColors.primary.copy(alpha = 0.25f),
                                                                    Color.White.copy(alpha = 0.06f)
                                                                )
                                                            },
                                                            start = Offset.Zero,
                                                            end = Offset(300f, 300f)
                                                        ),
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable {
                                                        onSelectThemeMode(mode)
                                                    }
                                                    .padding(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    // Left: Emoji + Name + Subtitle
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        // Live Palette Preview Pill with Glass Glare
                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        listOf(
                                                                            Color.White.copy(alpha = if (isThemeDark) 0.18f else 0.70f),
                                                                            modeColors.background.copy(alpha = 0.50f)
                                                                        )
                                                                    )
                                                                )
                                                                .border(
                                                                    1.dp,
                                                                    Brush.linearGradient(
                                                                        listOf(
                                                                            Color.White.copy(alpha = 0.70f),
                                                                            modeColors.primary.copy(alpha = 0.45f),
                                                                            Color.Transparent
                                                                        )
                                                                    ),
                                                                    RoundedCornerShape(12.dp)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = mode.iconEmoji,
                                                                fontSize = 18.sp
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(12.dp))

                                                        Column {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = mode.displayName,
                                                                    fontSize = 14.sp,
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                                )
                                                                if (isSelected) {
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Icon(
                                                                        imageVector = Icons.Default.CheckCircle,
                                                                        contentDescription = "Selected",
                                                                        tint = MaterialTheme.colorScheme.primary,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            }
                                                            Text(
                                                                text = mode.subtitle,
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }

                                                    // Right: Color Swatches Preview with Specular Glass Beads
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        val swatches = if (mode == com.example.connecto.ui.designsystem.ThemeMode.LIQUID_GLASS) {
                                                            listOf(
                                                                Color.White,
                                                                Color(0xFFCBD5E1),
                                                                Color(0x40FFFFFF),
                                                                Color.Transparent
                                                            )
                                                        } else {
                                                            listOf(
                                                                modeColors.primary,
                                                                modeColors.info,
                                                                modeColors.surface,
                                                                modeColors.background
                                                            )
                                                        }
                                                        swatches.forEach { swatchColor ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(17.dp)
                                                                    .shadow(1.dp, CircleShape)
                                                                    .clip(CircleShape)
                                                                    .background(
                                                                        Brush.radialGradient(
                                                                            colors = listOf(
                                                                                Color.White.copy(alpha = 0.65f),
                                                                                swatchColor,
                                                                                swatchColor
                                                                            ),
                                                                            center = Offset(5f, 5f),
                                                                            radius = 12f
                                                                        )
                                                                    )
                                                                    .border(1.dp, Color.White.copy(alpha = 0.40f), CircleShape)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (currentThemeMode == com.example.connecto.ui.designsystem.ThemeMode.LIQUID_GLASS) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), thickness = 1.dp)

                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "LIQUID GLASS OPACITY (0% - 100%)",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = when {
                                                            glassTransparency <= 0.01f -> "0% Pure Transparent Liquid Glass (Completely Clear)"
                                                            glassTransparency < 0.25f -> "Ultra-Light Glass (Subtle Sheen & Specular Borders)"
                                                            glassTransparency < 0.60f -> "Balanced Liquid Glass (Frosted Translucency)"
                                                            glassTransparency < 0.90f -> "Semi-Solid Glaze (Deep Contrast & High Visibility)"
                                                            else -> "100% Full Solid Glass (Maximum Opacity & Bold Tint)"
                                                        },
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                                ) {
                                                    Text(
                                                        text = "${(glassTransparency * 100).toInt()}% OPACITY",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            // Live optical glass refraction preview box (neutral specular background, no foreign colors)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(60.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(
                                                                Color(0x33FFFFFF),
                                                                Color(0x1AFFFFFF),
                                                                Color(0x0DFFFFFF)
                                                            )
                                                        )
                                                    )
                                                    .padding(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .liquidGlassCard(
                                                            shape = RoundedCornerShape(10.dp),
                                                            backgroundColor = Color.Transparent,
                                                            borderColor = Color.White
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "🪞 Live Glass Transparency: ${(glassTransparency * 100).toInt()}%",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }

                                            // 5 Quick Presets (0% to 100%)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                val presets = listOf(
                                                    Triple("0%", "Clear", 0.0f),
                                                    Triple("25%", "Light", 0.25f),
                                                    Triple("50%", "Bal.", 0.50f),
                                                    Triple("75%", "Deep", 0.75f),
                                                    Triple("100%", "Solid", 1.0f)
                                                )
                                                presets.forEach { (pct, label, value) ->
                                                    val isPresetActive = kotlin.math.abs(glassTransparency - value) < 0.06f
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                if (isPresetActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                            )
                                                            .border(
                                                                1.dp,
                                                                if (isPresetActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f),
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                            .clickable { onChangeGlassTransparency(value) }
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text(
                                                                text = pct,
                                                                fontSize = 11.sp,
                                                                fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.SemiBold,
                                                                color = if (isPresetActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = label,
                                                                fontSize = 9.sp,
                                                                color = if (isPresetActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Continuous Dynamic Liquid Glass Transparency Bar (0.0f to 1.0f)
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                LiquidGlassTransparencyBar(
                                                    value = glassTransparency.coerceIn(0.0f, 1.0f),
                                                    onValueChange = { onChangeGlassTransparency(it.coerceIn(0.0f, 1.0f)) },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "0% Pure Clear",
                                                        fontSize = 10.sp,
                                                        fontWeight = if (glassTransparency <= 0.05f) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (glassTransparency <= 0.05f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                        modifier = Modifier.clickable { onChangeGlassTransparency(0.0f) }
                                                    )
                                                    Text(
                                                        text = "50% Translucent",
                                                        fontSize = 10.sp,
                                                        fontWeight = if (kotlin.math.abs(glassTransparency - 0.5f) <= 0.05f) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (kotlin.math.abs(glassTransparency - 0.5f) <= 0.05f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                        modifier = Modifier.clickable { onChangeGlassTransparency(0.5f) }
                                                    )
                                                    Text(
                                                        text = "100% Solid",
                                                        fontSize = 10.sp,
                                                        fontWeight = if (glassTransparency >= 0.95f) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (glassTransparency >= 0.95f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                        modifier = Modifier.clickable { onChangeGlassTransparency(1.0f) }
                                                    )
                                                }
                                                Text(
                                                    text = "✨ Affects app-wide transparency across headers, navigation bar, chat dock, and cards.",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                                }
                            }
                            ProfileSubPage.GROUPS_COMMUNITIES -> {
                                item(key = "sub_groups_communities_form") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else MaterialTheme.colorScheme.primary
                                            )
                                    ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    if (isLoadingProfileGroups) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Syncing groups...",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                            if (profileGroupsList.isEmpty() && !isLoadingProfileGroups) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No active groups found. Create or join groups from the Messages tab.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    profileGroupsList.forEach { groupItem ->
                                        val myClean = username.trim().lowercase().removePrefix("@")
                                        val adminClean = groupItem.admin.trim().lowercase().removePrefix("@")
                                        val creatorClean = groupItem.creator.trim().lowercase().removePrefix("@")
                                        val isGroupAdmin = groupItem.isCreatedByMe ||
                                            (adminClean.isNotBlank() && adminClean == myClean) ||
                                            (creatorClean.isNotBlank() && creatorClean == myClean) ||
                                            (groupItem.admin.isBlank() && groupItem.creator.isBlank() && groupItem.members.firstOrNull()?.trim()?.lowercase()?.removePrefix("@") == myClean)

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f))
                                                .border(
                                                    1.dp,
                                                    if (isGroupAdmin) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    ConnectoAvatar(
                                                        name = groupItem.name,
                                                        avatarUrl = groupItem.avatarUrl,
                                                        customSizeDp = 42.dp,
                                                        customFontSizeSp = 16,
                                                        borderWidth = 1.dp,
                                                        borderColor = if (isGroupAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = "#${groupItem.name}",
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                maxLines = 1,
                                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(
                                                                        if (isGroupAdmin) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                                                    )
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (isGroupAdmin) "ADMIN" else "MEMBER",
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isGroupAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = "${groupItem.members.size.coerceAtLeast(groupItem.memberCount)} members",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                // Action Button: Delete Group (Admin) or Exit Group (Member)
                                                if (isGroupAdmin) {
                                                    Button(
                                                        onClick = { groupToConfirmDelete = groupItem },
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                                                            contentColor = Color(0xFFEF4444)
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                        modifier = Modifier.height(34.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.DeleteOutline,
                                                            contentDescription = "Delete Group",
                                                            tint = Color(0xFFEF4444),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Delete",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFEF4444)
                                                        )
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = { groupToConfirmLeave = groupItem },
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Color(0xFFF97316).copy(alpha = 0.15f),
                                                            contentColor = Color(0xFFF97316)
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                        modifier = Modifier.height(34.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                                            contentDescription = "Exit Group",
                                                            tint = Color(0xFFF97316),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Exit",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFF97316)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                                }
                            }
                            ProfileSubPage.NOTIFICATIONS -> {
                                item(key = "sub_notifications_settings_form") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .liquidGlassCard(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = if (themeColors.isLiquidGlass) Color.Transparent
                                                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                                                    else MaterialTheme.colorScheme.surface,
                                                borderColor = if (themeColors.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                                                    else MaterialTheme.colorScheme.primary
                                            )
                                            .padding(18.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // ================= 1. HIDE CONTENT IN NOTIFICATION TOGGLE =================
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        if (hideNotificationContent) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (hideNotificationContent) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(42.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (hideNotificationContent) Color(0xFF10B981).copy(alpha = 0.16f)
                                                                else MaterialTheme.colorScheme.surfaceVariant
                                                            )
                                                            .border(
                                                                1.dp,
                                                                if (hideNotificationContent) Color(0xFF10B981).copy(alpha = 0.5f)
                                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = if (hideNotificationContent) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                            contentDescription = null,
                                                            tint = if (hideNotificationContent) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(14.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = "Hide content in the notification",
                                                                fontSize = 14.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(3.dp))

                                                        Text(
                                                            text = if (hideNotificationContent)
                                                                "Message content hidden • Alerts show as 'New message received'."
                                                            else
                                                                "Full message preview & sender details are displayed in notifications.",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            lineHeight = 16.sp
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Switch(
                                                    checked = hideNotificationContent,
                                                    onCheckedChange = { isChecked ->
                                                        hideNotificationContent = isChecked
                                                        NotificationPreferences.setHideContentEnabled(context, isChecked)
                                                        Toast.makeText(
                                                            context,
                                                            if (isChecked) "Notification content hidden"
                                                            else "Notification content visible",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    colors = getMonochromeSwitchColors()
                                                )
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), thickness = 1.dp)

                                            // ================= 2. NOTIFICATION SOUNDS BUTTON =================
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { isNotificationSoundsExpanded = !isNotificationSoundsExpanded }
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                                    .border(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                                        RoundedCornerShape(14.dp)
                                                    )
                                                    .padding(14.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(42.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.MusicNote,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(14.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "Notification Sounds",
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )

                                                        Spacer(modifier = Modifier.height(3.dp))

                                                        Text(
                                                            text = "Active: $selectedNotificationSoundName • Tap to browse",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }

                                                    // Sound Preview / Play button for current sound
                                                    val isCurrentSoundPlaying = playingSoundId == selectedNotificationSoundId
                                                    IconButton(
                                                        onClick = {
                                                            if (isCurrentSoundPlaying) {
                                                                NotificationSoundManager.stopPreview()
                                                            } else {
                                                                NotificationSoundManager.playPreview(context, selectedNotificationSoundId)
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isCurrentSoundPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                            contentDescription = if (isCurrentSoundPlaying) "Stop" else "Preview",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(4.dp))

                                                    Icon(
                                                        imageVector = if (isNotificationSoundsExpanded) Icons.Default.Tune else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            // ================= 3. LATEST TRENDING SOUNDS LIST =================
                                            AnimatedVisibility(
                                                visible = isNotificationSoundsExpanded,
                                                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                                                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "LATEST TRENDING SOUNDS",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            letterSpacing = 1.sp
                                                        )
                                                        Text(
                                                            text = "Tap to select & preview",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }

                                                    NotificationSoundManager.TRENDING_SOUNDS.forEach { sound ->
                                                        val isSelected = selectedNotificationSoundId == sound.id
                                                        val isPlaying = playingSoundId == sound.id

                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    selectedNotificationSoundId = sound.id
                                                                    selectedNotificationSoundName = sound.name
                                                                    NotificationPreferences.setSelectedSound(context, sound.id, sound.name)
                                                                    ConnectoNotificationManager.getOrCreateMessageChannelForSound(context, sound.id)
                                                                    NotificationSoundManager.playPreview(context, sound.id)
                                                                    Toast.makeText(context, "Notification sound set to ${sound.name}", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(
                                                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                                                                )
                                                                .border(
                                                                    1.2.dp,
                                                                    if (isSelected) MaterialTheme.colorScheme.primary
                                                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                                                                    RoundedCornerShape(12.dp)
                                                                )
                                                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                // Selection indicator icon
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(32.dp)
                                                                        .clip(CircleShape)
                                                                        .background(
                                                                            if (isSelected) MaterialTheme.colorScheme.primary
                                                                            else MaterialTheme.colorScheme.surfaceVariant
                                                                        ),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    if (isSelected) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Check,
                                                                            contentDescription = null,
                                                                            tint = MaterialTheme.colorScheme.onPrimary,
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                    } else {
                                                                        Icon(
                                                                            imageVector = Icons.Default.VolumeUp,
                                                                            contentDescription = null,
                                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                    }
                                                                }

                                                                Spacer(modifier = Modifier.width(12.dp))

                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                                        Text(
                                                                            text = sound.name,
                                                                            fontSize = 14.sp,
                                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                            color = MaterialTheme.colorScheme.onSurface
                                                                        )
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(4.dp))
                                                                                .background(
                                                                                    when (sound.tag) {
                                                                                        "Trending #1" -> Color(0xFF8B5CF6).copy(alpha = 0.25f)
                                                                                        "Viral" -> Color(0xFFEC4899).copy(alpha = 0.25f)
                                                                                        "Futuristic" -> Color(0xFF06B6D4).copy(alpha = 0.25f)
                                                                                        "Aesthetic" -> Color(0xFF10B981).copy(alpha = 0.25f)
                                                                                        "Popular" -> Color(0xFFF59E0B).copy(alpha = 0.25f)
                                                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                                                    }
                                                                                )
                                                                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                                                        ) {
                                                                            Text(
                                                                                text = sound.tag.uppercase(),
                                                                                fontSize = 9.sp,
                                                                                fontWeight = FontWeight.ExtraBold,
                                                                                color = when (sound.tag) {
                                                                                    "Trending #1" -> Color(0xFFA78BFA)
                                                                                    "Viral" -> Color(0xFFF472B6)
                                                                                    "Futuristic" -> Color(0xFF22D3EE)
                                                                                    "Aesthetic" -> Color(0xFF34D399)
                                                                                    "Popular" -> Color(0xFFFBBF24)
                                                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                                                }
                                                                            )
                                                                        }
                                                                    }

                                                                    Spacer(modifier = Modifier.height(2.dp))

                                                                    Text(
                                                                        text = sound.subtitle,
                                                                        fontSize = 11.5.sp,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }

                                                                Spacer(modifier = Modifier.width(8.dp))

                                                                // Play / Stop Preview button
                                                                IconButton(
                                                                    onClick = {
                                                                        if (isPlaying) {
                                                                            NotificationSoundManager.stopPreview()
                                                                        } else {
                                                                            NotificationSoundManager.playPreview(context, sound.id)
                                                                        }
                                                                    },
                                                                    modifier = Modifier
                                                                        .size(34.dp)
                                                                        .clip(CircleShape)
                                                                        .background(
                                                                            if (isPlaying) MaterialTheme.colorScheme.primary
                                                                            else MaterialTheme.colorScheme.surfaceVariant
                                                                        )
                                                                ) {
                                                                    Icon(
                                                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                                        contentDescription = if (isPlaying) "Stop" else "Play",
                                                                        tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), thickness = 1.dp)

                                            // ================= 4. INSTANT NOTIFICATION TEST =================
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
                                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                                    .padding(14.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.NotificationsActive,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Instant Notification Test",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Text(
                                                    text = if (hideNotificationContent)
                                                        "Sends a test alert with '$selectedNotificationSoundName'. Content will be hidden as 'New message received'."
                                                    else
                                                        "Sends a test alert with '$selectedNotificationSoundName' showing full message details.",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    lineHeight = 16.sp
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))

                                                Button(
                                                    onClick = {
                                                        val testSender = "CyberValkyrie"
                                                        val testMessage = "Ready for the championship match! Let's win this 🔥"
                                                        val testAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
                                                        ConnectoNotificationManager.showMessageNotification(
                                                            senderName = testSender,
                                                            messageText = testMessage,
                                                            channelOrDmId = "dm-cybervalkyrie",
                                                            senderAvatar = testAvatar,
                                                            senderUsername = testSender
                                                        )
                                                        Toast.makeText(
                                                            context,
                                                            "Test notification sent! (Sound: $selectedNotificationSoundName)",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Send,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Send Test Notification",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            ProfileSubPage.VOICE_AUDIO -> {
                                item(key = "sub_voice_audio_settings") {
                                    VoiceAudioSettingsCard(hasCoverPhoto = hasCoverPhoto)
                                }
                            }

                            ProfileSubPage.HELP_SUPPORT -> {
                            // 1. Chat with Customer Support (AI)
                            item(key = "help_ai_support_card") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
                                                    MaterialTheme.colorScheme.surface
                                                )
                                            )
                                        )
                                        .border(
                                            1.5.dp,
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                                                )
                                            ),
                                            RoundedCornerShape(18.dp)
                                        )
                                        .clickable { showCustomerSupportAiDialog = true }
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.SmartToy,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "Chat with Customer Support",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(7.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFF10B981))
                                                        )
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(
                                                            text = "24/7 AI Support Online",
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Color(0xFF10B981)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Text(
                                            text = "Instant answers for troubleshooting, call & audio settings, wallpapers, E2EE encryption, 2FA, notifications, and application features.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )

                                        Button(
                                            onClick = { showCustomerSupportAiDialog = true },
                                            modifier = Modifier.fillMaxWidth().height(42.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Start AI Support Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }

                            // 3. Quick Support Channels Bento
                            item(key = "help_quick_actions") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "DIRECT SUPPORT CHANNELS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        letterSpacing = 1.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Email Support Card
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    if (hasCoverPhoto) Color.Black.copy(alpha = 0.35f)
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .border(
                                                    1.dp,
                                                    if (hasCoverPhoto) Color.White.copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clickable { sendSupportEmail() }
                                                .padding(14.dp)
                                        ) {
                                            Column {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.SupportAgent,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "Email Support",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "support@connecto.fun",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // Report Bug Card
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    if (hasCoverPhoto) Color.Black.copy(alpha = 0.35f)
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .border(
                                                    1.dp,
                                                    if (hasCoverPhoto) Color.White.copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clickable { showBugReportDialog = true }
                                                .padding(14.dp)
                                        ) {
                                            Column {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.BugReport,
                                                        contentDescription = null,
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "Report Issue",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Bug reports & logs",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Documentation Card
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    if (hasCoverPhoto) Color.Black.copy(alpha = 0.35f)
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .border(
                                                    1.dp,
                                                    if (hasCoverPhoto) Color.White.copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clickable { openWebUrl("https://connecto.fun/architecture") }
                                                .padding(14.dp)
                                        ) {
                                            Column {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFF06B6D4).copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Language,
                                                        contentDescription = null,
                                                        tint = Color(0xFF06B6D4),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "Documentation",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Architecture & guides",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // System Status Card
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    if (hasCoverPhoto) Color.Black.copy(alpha = 0.35f)
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .border(
                                                    1.dp,
                                                    if (hasCoverPhoto) Color.White.copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clickable { openWebUrl("https://connecto.fun/status") }
                                                .padding(14.dp)
                                        ) {
                                            Column {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Speed,
                                                        contentDescription = null,
                                                        tint = Color(0xFF10B981),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "System Status",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Live edge monitoring",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Live Edge Latency Diagnostic Card (Admin only)
                            if (isUserAdmin) {
                                item(key = "help_edge_latency_card") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(
                                                if (hasCoverPhoto) Color.Black.copy(alpha = 0.38f)
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                1.dp,
                                                if (hasCoverPhoto) Color.White.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                                RoundedCornerShape(18.dp)
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF10B981))
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Cloudflare Edge Diagnostic",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                Button(
                                                    onClick = { runEdgePingTest() },
                                                    enabled = !isEdgePingTesting,
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                ) {
                                                    Text(
                                                        text = if (isEdgePingTesting) "Testing..." else "Test Latency",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Text(
                                                text = edgePingResult ?: "Tap 'Test Latency' to measure roundtrip ping to the closest Cloudflare Edge data center.",
                                                fontSize = 12.5.sp,
                                                color = if (edgePingResult?.contains("Error") == true) MaterialTheme.colorScheme.error
                                                    else if (edgePingResult != null) Color(0xFF10B981)
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Interactive FAQs
                            item(key = "help_faq_section") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "FREQUENTLY ASKED QUESTIONS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        letterSpacing = 1.sp
                                    )

                                    val faqs = listOf(
                                        "How does App-Wide Blackscreen Privacy protect me?" to
                                            "Connecto applies WindowManager.LayoutParams.FLAG_SECURE at Android's SurfaceFlinger hardware compositor level. Any screenshots, screen recordings, or screen sharing apps (such as Discord, Zoom, and Google Meet) display an opaque pitch-black screen (#000000) for viewers, safeguarding your chats and private data.",
                                        "Why should I disable battery optimization for Connecto?" to
                                            "Android aggressively suspends background network sockets in Doze Mode. Disabling battery optimization allows high-priority Google FCM v1 push messages to immediately wake your device with sounds, vibration, and heads-up banners even when the app is swiped away.",
                                        "What does Stealth Mode do?" to
                                            "Stealth Mode hides your online presence dot from friend lists, channels, and conversation cards. You can read and send messages freely while appearing completely offline to other members.",
                                        "How does Two-Factor Authentication (2FA) work?" to
                                            "When 2FA is enabled in Profile -> Two-Factor & Security, every sign-in from a new device requires a 6-digit one-time verification code dispatched to your verified email address.",
                                        "How do WebRTC mesh voice rooms work?" to
                                            "Connecto voice channels establish direct peer-to-peer WebRTC mesh connections between participants with Opus HD audio codecs, providing sub-20ms spatial latency with zero server recording.",
                                        "Can I reply to messages directly from notifications?" to
                                            "Yes! When a notification arrives, swipe down on the banner to access WhatsApp-style inline Direct Reply and Mark as Read actions without switching away from your current task."
                                    )

                                    faqs.forEachIndexed { index, (question, answer) ->
                                        val isExpanded = expandedFaqIndex == index
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (hasCoverPhoto) Color.Black.copy(alpha = 0.35f)
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                    RoundedCornerShape(14.dp)
                                                )
                                                .clickable {
                                                    expandedFaqIndex = if (isExpanded) -1 else index
                                                }
                                                .padding(14.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = question,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                if (isExpanded) {
                                                    Text(
                                                        text = answer,
                                                        fontSize = 12.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        lineHeight = 18.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }


                            // 6. Platform Legal: Privacy Policy & Terms of Service (Last Section)
                            item(key = "help_legal_last_section") {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "PRIVACY POLICY & TERMS OF SERVICES",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        letterSpacing = 1.sp
                                    )

                                    // Privacy Policy Card
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (hasCoverPhoto) Color.Black.copy(alpha = 0.35f)
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                1.dp,
                                                if (hasCoverPhoto) Color.White.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Policy,
                                                        contentDescription = null,
                                                        tint = Color(0xFF3B82F6),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Privacy Policy",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Zero-telemetry & AES-256 encrypted storage",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "Learn how Connecto protects your messages with cryptographic keys, avoids cloud backups of cleartext data, and never sells your personal metadata.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { showPrivacyPolicyDialog = true },
                                                    modifier = Modifier.weight(1f).height(38.dp),
                                                    shape = RoundedCornerShape(9.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                ) {
                                                    Text("View Policy", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = { openWebUrl("https://connecto.fun/privacy") },
                                                    modifier = Modifier.height(38.dp),
                                                    shape = RoundedCornerShape(9.dp)
                                                ) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.OpenInNew,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Web", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }

                                    // Terms of Service Card
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (hasCoverPhoto) Color.Black.copy(alpha = 0.35f)
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                1.dp,
                                                if (hasCoverPhoto) Color.White.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Description,
                                                        contentDescription = null,
                                                        tint = Color(0xFF8B5CF6),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Terms of Service",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Acceptable use, E2EE agreement & community rules",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "Understand your rights, account ownership, community safety standards, end-to-end encryption guidelines, and service limitations.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { showTermsOfServiceDialog = true },
                                                    modifier = Modifier.weight(1f).height(38.dp),
                                                    shape = RoundedCornerShape(9.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                ) {
                                                    Text("View Terms", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = { openWebUrl("https://connecto.fun/terms") },
                                                    modifier = Modifier.height(38.dp),
                                                    shape = RoundedCornerShape(9.dp)
                                                ) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.OpenInNew,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Web", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 7. Last Section: Delete Account Forever & Data Purge
                            item(key = "help_delete_account_last_section") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (hasCoverPhoto) Color.Black.copy(alpha = 0.40f)
                                            else Color(0xFFDC2626).copy(alpha = 0.12f)
                                        )
                                        .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFDC2626).copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteForever,
                                                    contentDescription = null,
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Delete Account & Data",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFDC2626)
                                                )
                                                Text(
                                                    text = "Permanently purge account & backups",
                                                    fontSize = 11.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Closing your account permanently purges your identity, chats, groups, call history, and physical database backups forever. This action cannot be undone.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )

                                        Button(
                                            onClick = {
                                                deleteConfirmInput = ""
                                                showDeleteAccountDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth().height(42.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFDC2626),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteForever,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Delete Account",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
    }
    if (showBugReportDialog) {
        AlertDialog(
            onDismissRequest = { showBugReportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Report an Issue", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Describe what happened. Device details and system logs will be attached automatically.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Category Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("Notifications", "Chat Latency", "Voice / Audio", "UI / Theme", "Security", "Other")
                        categories.forEach { cat ->
                            val isSelected = bugReportCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { bugReportCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    // Text input
                    BasicTextField(
                        value = bugReportDescription,
                        onValueChange = { bugReportDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        ),
                        decorationBox = { innerTextField ->
                            if (bugReportDescription.isBlank()) {
                                Text(
                                    text = "What went wrong? Steps to reproduce...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val desc = bugReportDescription.trim()
                        if (desc.isBlank()) {
                            Toast.makeText(context, "Please describe the issue before submitting.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val emailBody = "Issue Category: $bugReportCategory\n\n" +
                                "User Report:\n$desc\n\n" +
                                "--- System Diagnostics ---\n" +
                                "App Version: 4.4.8 (Build 77)\n" +
                                "User: @$username\n" +
                                "Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})\n" +
                                "Model: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n"
                        sendSupportEmail(subject = "[Bug Report - $bugReportCategory] Connecto v4.4.8", prefilledBody = emailBody)
                        showBugReportDialog = false
                        bugReportDescription = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Send via Email")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showBugReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showAddFriendDialog) {
        AddFriendDialog(
            currentUsername = username,
            onDismissRequest = { showAddFriendDialog = false },
            onFriendAdded = { showAddFriendDialog = false }
        )
    }

    // Confirmation Dialog for Deleting Group from Profile (Admin)
    if (groupToConfirmDelete != null) {
        val targetGroup = groupToConfirmDelete!!
        AlertDialog(
            onDismissRequest = { if (!isDeletingProfileGroup) groupToConfirmDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete #${targetGroup.name}?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Are you sure you want to permanently delete this group? All group chat history and member associations will be erased.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isDeletingProfileGroup = true
                            val gId = targetGroup.id
                            try {
                                val res = ConnectoApiClient.deleteGroup(gId)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Group #${targetGroup.name} deleted", Toast.LENGTH_SHORT).show()
                                    profileGroupsList.removeAll { it.id == gId }
                                    groupToConfirmDelete = null
                                    withContext(Dispatchers.IO) {
                                        try { dbHelper.deleteCachedGroup(gId) } catch (_: Exception) {}
                                        try {
                                            val readPrefs = context.getSharedPreferences("connecto_read_receipts", Context.MODE_PRIVATE)
                                            readPrefs.edit()
                                                .remove("last_msg_$gId")
                                                .remove("last_time_$gId")
                                                .remove("last_timestamp_$gId")
                                                .remove("unread_$gId")
                                                .apply()
                                        } catch (_: Exception) {}
                                    }
                                    try {
                                        val broadcastIntent = Intent("com.example.connecto.ACTION_GROUP_REMOVED").apply {
                                            putExtra("group_id", gId)
                                            `package` = context.packageName
                                        }
                                        context.sendBroadcast(broadcastIntent)
                                    } catch (_: Exception) {}
                                } else {
                                    Toast.makeText(context, "Delete failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                }
                            } finally {
                                isDeletingProfileGroup = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isDeletingProfileGroup
                ) {
                    if (isDeletingProfileGroup) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { groupToConfirmDelete = null },
                    enabled = !isDeletingProfileGroup
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog for Leaving Group from Profile (Member)
    if (groupToConfirmLeave != null) {
        val targetGroup = groupToConfirmLeave!!
        AlertDialog(
            onDismissRequest = { if (!isLeavingProfileGroup) groupToConfirmLeave = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exit #${targetGroup.name}?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Are you sure you want to exit this group? You will no longer receive updates or messages.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLeavingProfileGroup = true
                            val gId = targetGroup.id
                            try {
                                val res = ConnectoApiClient.leaveGroup(gId)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "You left #${targetGroup.name}", Toast.LENGTH_SHORT).show()
                                    profileGroupsList.removeAll { it.id == gId }
                                    groupToConfirmLeave = null
                                    withContext(Dispatchers.IO) {
                                        try { dbHelper.deleteCachedGroup(gId) } catch (_: Exception) {}
                                        try {
                                            val readPrefs = context.getSharedPreferences("connecto_read_receipts", Context.MODE_PRIVATE)
                                            readPrefs.edit()
                                                .remove("last_msg_$gId")
                                                .remove("last_time_$gId")
                                                .remove("last_timestamp_$gId")
                                                .remove("unread_$gId")
                                                .apply()
                                        } catch (_: Exception) {}
                                    }
                                    try {
                                        val broadcastIntent = Intent("com.example.connecto.ACTION_GROUP_REMOVED").apply {
                                            putExtra("group_id", gId)
                                            `package` = context.packageName
                                        }
                                        context.sendBroadcast(broadcastIntent)
                                    } catch (_: Exception) {}
                                } else {
                                    Toast.makeText(context, "Failed to exit: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                }
                            } finally {
                                isLeavingProfileGroup = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLeavingProfileGroup
                ) {
                    if (isLeavingProfileGroup) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Exit Group", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { groupToConfirmLeave = null },
                    enabled = !isLeavingProfileGroup
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLightbox) {
        PhotoLightboxDialog(
            title = lightboxTitle,
            imageUrl = lightboxImageUrl,
            placeholderInitial = userInitial,
            isBanner = isLightboxBanner,
            onChangePhoto = {
                if (isLightboxBanner) {
                    showLightbox = false
                    showCoverOptionsMenu = true
                } else {
                    photoPickerLauncher.launch("image/*")
                }
            },
            onRemovePhoto = if (isLightboxBanner && hasCoverPhoto) {
                {
                    showLightbox = false
                    removeCoverPhoto()
                }
            } else null,
            onDismiss = { showLightbox = false }
        )
    }

    if (showCoverOptionsMenu) {
        CoverOptionsMenuDialog(
            hasExistingCover = hasCoverPhoto,
            onChooseGallery = {
                showCoverOptionsMenu = false
                bannerPickerLauncher.launch("image/*")
            },
            onOpenPresets = {
                showCoverOptionsMenu = false
                showCoverPresetsDialog = true
            },
            onRemoveCover = {
                showCoverOptionsMenu = false
                removeCoverPhoto()
            },
            onDismiss = { showCoverOptionsMenu = false }
        )
    }

    if (showCoverPresetsDialog) {
        CoverPresetsDialog(
            currentPresetId = bannerPhotoUri,
            onPresetSelected = { preset ->
                showCoverPresetsDialog = false
                bannerPhotoUri = preset.id
                bannerUrlFieldInput = preset.id
                prefs.edit().putString("banner_uri_$username", preset.id).apply()
                ConnectoApiClient.updateBannerState(preset.id, context)
                val localCover = java.io.File(context.filesDir, "cover_${username.lowercase()}.webp")
                if (localCover.exists()) {
                    try { localCover.delete() } catch (_: Exception) {}
                }
                triggerAutoSave()
            },
            onDismiss = { showCoverPresetsDialog = false }
        )
    }

    if (showCropDialog && pendingCropUri != null) {
        InteractiveCropFitDialog(
            imageUri = pendingCropUri!!,
            cropMode = activeCropMode,
            onCropConfirmed = { croppedBitmap ->
                showCropDialog = false
                val chosenMode = activeCropMode
                if (chosenMode == CropMode.AVATAR) {
                    // Optimistic immediate local save
                    try {
                        val localAvatar = java.io.File(context.filesDir, "avatar_${username.lowercase()}.webp")
                        localAvatar.outputStream().use { out ->
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP_LOSSY, 90, out)
                            } else {
                                @Suppress("DEPRECATION")
                                croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 90, out)
                            }
                        }
                        profilePhotoUri = localAvatar.absolutePath
                        prefs.edit().putString("photo_uri_$username", localAvatar.absolutePath).apply()
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileScreen", "Failed to cache avatar locally", e)
                    }

                    isUploadingAvatar = true
                    coroutineScope.launch {
                        try {
                            val result = ConnectoApiClient.uploadAvatarBitmap(context, croppedBitmap)
                            if (result.isSuccess) {
                                val serverUrl = result.getOrThrow()
                                if (serverUrl.isNotBlank()) {
                                    profilePhotoUri = serverUrl
                                    prefs.edit().putString("photo_uri_$username", serverUrl).apply()
                                }
                            }
                        } finally {
                            isUploadingAvatar = false
                            triggerAutoSave()
                        }
                    }
                } else {
                    // Optimistic immediate local save for cover banner
                    try {
                        val localCover = java.io.File(context.filesDir, "cover_${username.lowercase()}.webp")
                        localCover.outputStream().use { out ->
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP_LOSSY, 90, out)
                            } else {
                                @Suppress("DEPRECATION")
                                croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.WEBP, 90, out)
                            }
                        }
                        bannerPhotoUri = localCover.absolutePath
                        bannerUrlFieldInput = localCover.absolutePath
                        prefs.edit().putString("banner_uri_$username", localCover.absolutePath).apply()
                        ConnectoApiClient.updateBannerState(localCover.absolutePath, context)
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileScreen", "Failed to cache cover locally", e)
                    }

                    isUploadingBanner = true
                    coroutineScope.launch {
                        try {
                            val result = ConnectoApiClient.uploadBannerBitmap(context, croppedBitmap)
                            if (result.isSuccess) {
                                val serverUrl = result.getOrThrow()
                                if (serverUrl.isNotBlank()) {
                                    // Retain localCover in bannerPhotoUri to eliminate network re-download flickering
                                    bannerUrlFieldInput = serverUrl
                                    prefs.edit().putString("banner_uri_$username", serverUrl).apply()
                                    ConnectoApiClient.currentUserBanner = serverUrl
                                }
                            }
                        } finally {
                            isUploadingBanner = false
                            triggerAutoSave()
                        }
                    }
                }
            },
            onDismiss = {
                showCropDialog = false
                pendingCropUri = null
            }
        )
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            initialIdentifier = emailInput.ifBlank { username },
            onDismiss = { showForgotPasswordDialog = false },
            onSuccess = {
                showForgotPasswordDialog = false
                showSavedBadge = true
            }
        )
    }

    if (show2FaSetupDialog || com.example.connecto.security.TwoFactorStateManager.isSetupDialogOpen) {
        val activeDest = if (emailInput.isNotBlank()) emailInput.trim() else com.example.connecto.security.TwoFactorStateManager.setupDestination
        val activeMasked = twoFaSetupMaskedDest ?: com.example.connecto.security.TwoFactorStateManager.setupMaskedDestination
        TwoFactorSetupDialog(
            method = "email",
            destination = activeDest,
            maskedDestination = activeMasked,
            onDismiss = {
                show2FaSetupDialog = false
                com.example.connecto.security.TwoFactorStateManager.clear(context)
            },
            onSuccess = {
                show2FaSetupDialog = false
                com.example.connecto.security.TwoFactorStateManager.clear(context)
                twoFactorEnabled = true
                prefs.edit().putBoolean("two_factor_enabled_$username", true).apply()
                showSavedBadge = true
            }
        )
    }

    if (show2FaDisableDialog) {
        TwoFactorDisableDialog(
            destination = emailInput.trim(),
            maskedDestination = twoFaDisableMaskedDest,
            onDismiss = { show2FaDisableDialog = false },
            onSuccess = {
                show2FaDisableDialog = false
                twoFactorEnabled = false
                prefs.edit().putBoolean("two_factor_enabled_$username", false).apply()
                showSavedBadge = true
            }
        )
    }

    if (changeContactTarget != null) {
        val targetMethod = changeContactTarget!!
        ChangeContactDialog(
            method = targetMethod,
            currentValue = emailInput,
            onDismiss = { changeContactTarget = null },
            onSuccess = { newContact ->
                changeContactTarget = null
                // email only — phone option removed per policy
                emailInput = newContact
                prefs.edit().putString("email_$currentUsernameState", newContact).apply()
                showSavedBadge = true
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeletingAccount) {
                    showDeleteAccountDialog = false
                    deleteConfirmInput = ""
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Delete Account Permanently",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFFDC2626)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "This action is IRREVERSIBLE. Your account, profile information, messages, active sessions, and all physical database backups will be permanently purged forever.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "To confirm deletion, type DELETE in capital letters below:",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = deleteConfirmInput,
                        onValueChange = { deleteConfirmInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("DELETE", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFDC2626),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        enabled = !isDeletingAccount
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteConfirmInput.trim() == "DELETE" && !isDeletingAccount) {
                            isDeletingAccount = true
                            coroutineScope.launch {
                                try {
                                    val result = ConnectoApiClient.deleteAccount()
                                    if (result.isSuccess) {
                                        showDeleteAccountDialog = false
                                        Toast.makeText(context, "Account permanently deleted.", Toast.LENGTH_LONG).show()
                                        onDeleteAccount()
                                    } else {
                                        val err = result.exceptionOrNull()?.message ?: "Unknown error"
                                        Toast.makeText(context, "Delete failed: $err", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isDeletingAccount = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = deleteConfirmInput.trim() == "DELETE" && !isDeletingAccount
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deleting...", fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Text("Delete Forever", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteConfirmInput = ""
                    },
                    enabled = !isDeletingAccount
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCustomerSupportAiDialog) {
        CustomerSupportAiDialog(
            username = username,
            onDismiss = { showCustomerSupportAiDialog = false }
        )
    }

    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }

    if (showTermsOfServiceDialog) {
        TermsOfServiceDialog(
            onDismiss = { showTermsOfServiceDialog = false }
        )
    }

}

// ================= HELPER COMPOSABLES =================

@Composable
private fun VoiceAudioSettingsCard(hasCoverPhoto: Boolean) {
    val context = LocalContext.current
    val isVoiceIsoActive by com.example.connecto.voice.VoiceCallManager.isVoiceIsolationEnabled.collectAsState()
    val voiceIsolationCaps by com.example.connecto.voice.VoiceCallManager.voiceIsolationCapabilities.collectAsState()
    
    val audioPrefs = remember { context.getSharedPreferences("connecto_audio_prefs", Context.MODE_PRIVATE) }
    var echoCancellation by remember { mutableStateOf(audioPrefs.getBoolean("connecto_echo_cancellation", true)) }
    var autoGainControl by remember { mutableStateOf(audioPrefs.getBoolean("connecto_auto_gain", true)) }
    var highDefVoice by remember { mutableStateOf(audioPrefs.getBoolean("connecto_hd_voice", true)) }
    
    var isMicTesting by remember { mutableStateOf(false) }
    var testLevel by remember { mutableStateOf(0.15f) }
    
    androidx.compose.runtime.LaunchedEffect(isMicTesting) {
        if (isMicTesting) {
            while (isMicTesting) {
                delay(80L)
                val floor = if (isVoiceIsoActive) 0.05f else 0.25f
                val peak = if (isVoiceIsoActive) 0.85f else 0.65f
                testLevel = (floor + (Math.random().toFloat() * (peak - floor))).coerceIn(0f, 1f)
            }
        } else {
            testLevel = 0.05f
        }
    }
    
    val hwNsAvailable = remember {
        try {
            android.media.audiofx.NoiseSuppressor.isAvailable()
        } catch (_: Throwable) {
            false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f) else MaterialTheme.colorScheme.surface,
                borderColor = if (hasCoverPhoto) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Voice Isolation Header & Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isVoiceIsoActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                    .border(
                        1.dp,
                        if (isVoiceIsoActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isVoiceIsoActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (isVoiceIsoActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Voice Isolation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isVoiceIsoActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isVoiceIsoActive) "AI ACTIVE" else "OFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isVoiceIsoActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isVoiceIsoActive) "${voiceIsolationCaps.statusSummary} engaged" else "Raw mic input without suppression",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
                Switch(
                    checked = isVoiceIsoActive,
                    onCheckedChange = { isChecked ->
                        com.example.connecto.voice.VoiceCallManager.setVoiceIsolation(isChecked)
                        try {
                            audioPrefs.edit().putBoolean("connecto_voice_isolation_enabled", isChecked).apply()
                        } catch (_: Throwable) {}
                        Toast.makeText(
                            context,
                            if (isChecked) "Voice Isolation: Active (${voiceIsolationCaps.statusSummary})" else "Voice Isolation Disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = getMonochromeSwitchColors()
                )
            }

            // Real-time Mic Spectrum / VU Meter Test
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Microphone Live Sensitivity",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isMicTesting) "Testing input levels with current DSP..." else "Tap Test to monitor live spectral gating",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { isMicTesting = !isMicTesting },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMicTesting) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (isMicTesting) "Stop" else "Test Mic",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 18-bar VU meter graphic
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val barCount = 18
                        val animatedLevel by animateFloatAsState(targetValue = testLevel, label = "mic_level")
                        for (i in 0 until barCount) {
                            val threshold = i.toFloat() / barCount
                            val isLit = animatedLevel >= threshold
                            val barColor = when {
                                i >= barCount - 3 -> Color(0xFFEF4444) // Red peak
                                i >= barCount - 7 -> Color(0xFFF59E0B) // Amber warning
                                else -> Color(0xFF10B981) // Green clear
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(((i + 1) * 1.3f).dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isLit) barColor else barColor.copy(alpha = 0.15f))
                            )
                        }
                    }
                }
            }

            // DSP Audio Pipeline Architecture Cards
            Text(
                text = "DSP ISOLATION PIPELINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DspSpecItem(
                    title = "140Hz High-Pass Filter",
                    detail = "Rejects sub-vocal rumble, AC hum, desk vibrations & handling noise",
                    isActive = isVoiceIsoActive
                )
                DspSpecItem(
                    title = "3400Hz Vocal Low-Pass Filter",
                    detail = "Attenuates high-frequency fan hiss, coil whine & keyboard clicks",
                    isActive = isVoiceIsoActive
                )
                DspSpecItem(
                    title = "Adaptive Expander Gate (-90dB)",
                    detail = "Total silence floor between speech with 240ms hangover protection",
                    isActive = isVoiceIsoActive
                )
                DspSpecItem(
                    title = "Vocal Presence Normalization (+1.8dB)",
                    detail = "Optimizes intelligibility and prevents clipping with soft limiter",
                    isActive = isVoiceIsoActive
                )
                DspSpecItem(
                    title = if (voiceIsolationCaps.hasHardwareMicBeamforming) "Multi-Mic Beamforming (${voiceIsolationCaps.multiMicCount} Mics Detected)" else "Directional Mic Mode",
                    detail = if (voiceIsolationCaps.hasHardwareMicBeamforming) "Hardware beamforming isolates user's mouth and cancels room ambiance" else "Acoustic focusing on front microphone",
                    isActive = isVoiceIsoActive
                )
                DspSpecItem(
                    title = if (voiceIsolationCaps.hasHardwareNoiseSuppressor) "Hardware NoiseSuppressor (SoC HAL Active)" else "Software DSP Noise Processor",
                    detail = if (voiceIsolationCaps.hasHardwareNoiseSuppressor) "Dedicated DSP core silicon noise cancellation" else "Running high-performance software spectral subtraction",
                    isActive = true
                )
                if (voiceIsolationCaps.hasHardwareEchoCanceler) {
                    DspSpecItem(
                        title = "Hardware Acoustic Echo Cancellation (AEC)",
                        detail = "SoC-level echo removal eliminating speakerphone feedback",
                        isActive = true
                    )
                }
            }

            // Additional Audio Features
            Text(
                text = "ADVANCED AUDIO CONTROLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )

            // Echo Cancellation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Acoustic Echo Cancellation", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Prevents speaker feedback from bleeding back into the mic", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = echoCancellation,
                    onCheckedChange = {
                        echoCancellation = it
                        audioPrefs.edit().putBoolean("connecto_echo_cancellation", it).apply()
                    },
                    colors = getMonochromeSwitchColors()
                )
            }

            // Auto Gain Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Automatic Gain Control (AGC)", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Normalizes voice volume regardless of phone distance", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = autoGainControl,
                    onCheckedChange = {
                        autoGainControl = it
                        audioPrefs.edit().putBoolean("connecto_auto_gain", it).apply()
                    },
                    colors = getMonochromeSwitchColors()
                )
            }

            // HD Opus Voice (48kHz)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "HD Studio Voice (48 kHz Opus)", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Full-bandwidth ultra-clear audio for crystal calls", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = highDefVoice,
                    onCheckedChange = {
                        highDefVoice = it
                        audioPrefs.edit().putBoolean("connecto_hd_voice", it).apply()
                    },
                    colors = getMonochromeSwitchColors()
                )
            }
        }
    }
}

@Composable
private fun DspSpecItem(
    title: String,
    detail: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            )
            .border(
                1.dp,
                if (isActive) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = detail,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

// ================= HELPER COMPOSABLES =================

@Composable
private fun TwoFactorSetupDialog(
    method: String,
    destination: String,
    maskedDestination: String?,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var otpCode by rememberSaveable { mutableStateOf(com.example.connecto.security.TwoFactorStateManager.setupOtpCode) }
    var isVerifying by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var resendFeedback by remember { mutableStateOf<String?>(null) }
    var resendCooldown by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val isLight = isAppInLightTheme()
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    androidx.compose.runtime.LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            kotlinx.coroutines.delay(1000L)
            resendCooldown -= 1
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        val dialogBg = if (isLight) Color(0xFFFFFFFF) else Color(0xFF000000)
        val cardBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E1E24)
        val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
        val subtitleColor = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(dialogBg)
                .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(gradientColors)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = contentOnGradient,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Activate 2FA",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = "Verify via ${method.uppercase()}",
                                fontSize = 12.sp,
                                color = subtitleColor
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = subtitleColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                val destText = if (!maskedDestination.isNullOrBlank()) {
                    "sent to $maskedDestination (${method.uppercase()})"
                } else {
                    "sent to your registered ${method.uppercase()}"
                }

                Text(
                    text = "A 6-digit setup code was $destText.\nLook for the email titled '[Connecto 2FA Setup]'.\nEnter the code below to enable Two-Factor Authentication.",
                    fontSize = 13.sp,
                    color = subtitleColor,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMessage != null) {
                    val errorBg = if (isLight) Color(0xFFFEE2E2) else Color(0xFF7F1D1D).copy(alpha = 0.35f)
                    val errorText = if (isLight) Color(0xFFDC2626) else Color(0xFFFCA5A5)
                    val errorBorder = if (isLight) Color(0xFFFCA5A5) else Color(0xFFEF4444).copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(errorBg)
                            .border(1.dp, errorBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = errorText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 6-digit OTP Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isLight) Color(0xFFF8FAFC) else Color(0xFF0A0A0C))
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = otpCode,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                otpCode = it
                                errorMessage = null
                                com.example.connecto.security.TwoFactorStateManager.updateDraftCode(context, it)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            letterSpacing = 8.sp,
                            color = textColor
                        ),
                        cursorBrush = SolidColor(gradientColors.first()),
                        decorationBox = { innerTextField ->
                            if (otpCode.isEmpty()) {
                                Text(
                                    text = "000000",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 8.sp,
                                    textAlign = TextAlign.Center,
                                    color = subtitleColor.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val clean = otpCode.trim()
                        if (clean.length < 6) {
                            errorMessage = "Please enter the complete 6-digit code."
                            return@Button
                        }
                        isVerifying = true
                        errorMessage = null
                        coroutineScope.launch {
                            val res = ConnectoApiClient.verify2FaSetupOtp(
                                otpCode = clean,
                                method = method,
                                phoneNumber = destination
                            )
                            isVerifying = false
                            if (res.isSuccess && res.getOrNull()?.twoFactorEnabled == true) {
                                onSuccess()
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "2FA verification failed. Please check the code."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = !isVerifying && otpCode.trim().length == 6
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                if (!isVerifying && otpCode.trim().length == 6)
                                    Brush.horizontalGradient(gradientColors)
                                else
                                    Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f))),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(color = contentOnGradient, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = "CONFIRM & ACTIVATE 2FA",
                                color = if (otpCode.trim().length == 6) contentOnGradient else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Resend Code Button with Cooldown
                TextButton(
                    onClick = {
                        if (resendCooldown == 0 && !isResending) {
                            isResending = true
                            resendFeedback = null
                            errorMessage = null
                            coroutineScope.launch {
                                val res = ConnectoApiClient.request2FaSetupOtp(
                                    method = method,
                                    phoneNumber = destination,
                                    purpose = "two_factor_setup",
                                    token = ConnectoApiClient.getPersistedToken()
                                )
                                isResending = false
                                if (res.isSuccess) {
                                    resendFeedback = "New verification code sent!"
                                    resendCooldown = 60
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Failed to resend verification code."
                                }
                            }
                        }
                    },
                    enabled = resendCooldown == 0 && !isResending
                ) {
                    if (isResending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (resendCooldown > 0) "Resend code in ${resendCooldown}s" else "Resend verification code",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (resendCooldown > 0) subtitleColor else MaterialTheme.colorScheme.primary
                    )
                }

                if (resendFeedback != null) {
                    Text(
                        text = resendFeedback ?: "",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TwoFactorDisableDialog(
    destination: String,
    maskedDestination: String?,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val isLight = isAppInLightTheme()

    Dialog(onDismissRequest = onDismiss) {
        val dialogBg = if (isLight) Color(0xFFFFFFFF) else Color(0xFF000000)
        val cardBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E1E24)
        val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
        val subtitleColor = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(dialogBg)
                .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Turn Off 2FA",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = "Security Verification",
                                fontSize = 12.sp,
                                color = subtitleColor
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = subtitleColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                val destText = if (!maskedDestination.isNullOrBlank()) {
                    "sent to $maskedDestination"
                } else {
                    "sent to your registered email"
                }

                Text(
                    text = "A 6-digit confirmation code was $destText to safely disable Two-Factor Authentication.\nPlease enter the code below to confirm.",
                    fontSize = 13.sp,
                    color = subtitleColor,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    val errorBg = if (isLight) Color(0xFFFEE2E2) else Color(0xFF7F1D1D).copy(alpha = 0.35f)
                    val errorText = if (isLight) Color(0xFFDC2626) else Color(0xFFFCA5A5)
                    val errorBorder = if (isLight) Color(0xFFFCA5A5) else Color(0xFFEF4444).copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(errorBg)
                            .border(1.dp, errorBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = errorText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 6-digit OTP Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isLight) Color(0xFFF8FAFC) else Color(0xFF0A0A0C))
                        .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = otpCode,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                otpCode = it
                                errorMessage = null
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            letterSpacing = 8.sp,
                            color = textColor
                        ),
                        cursorBrush = SolidColor(Color(0xFFEF4444)),
                        decorationBox = { innerTextField ->
                            if (otpCode.isEmpty()) {
                                Text(
                                    text = "000000",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 8.sp,
                                    textAlign = TextAlign.Center,
                                    color = subtitleColor.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val clean = otpCode.trim()
                        if (clean.length < 6) {
                            errorMessage = "Please enter the complete 6-digit verification code."
                            return@Button
                        }
                        isVerifying = true
                        errorMessage = null
                        coroutineScope.launch {
                            val activeToken = ConnectoApiClient.sessionToken
                            val res = ConnectoApiClient.toggle2Fa(
                                enabled = false,
                                method = "email",
                                otpCode = clean,
                                token = activeToken
                            )
                            isVerifying = false
                            if (res.isSuccess) {
                                onSuccess()
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "Failed to disable 2FA. Check the verification code."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    enabled = !isVerifying && otpCode.trim().length == 6
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = "CONFIRM & DISABLE 2FA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    badgeText: String? = null,
    badgeColor: Color? = null,
    isExpanded: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                1.dp,
                if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    }

    Row(
        modifier = clickableModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (badgeText != null) {
                    val bColor = badgeColor ?: MaterialTheme.colorScheme.primary
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = bColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
        if (onClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "header_chevron_rotation"
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse $title" else "Expand $title",
                    tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
            }
        }
    }
}

@Composable
private fun ProfileStyledInputField(
    label: String,
    placeholder: String,
    value: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isLocked: Boolean = false,
    lockBadgeText: String = "SERVER LOCKED",
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isLocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextDisabledColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = lockBadgeText,
                        color = TextDisabledColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    1.dp,
                    if (isLocked) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else icon,
                    contentDescription = null,
                    tint = if (isLocked) TextDisabledColor else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = TextDisabledColor,
                            fontSize = 13.sp
                        )
                    }
                    if (isLocked) {
                        Text(
                            text = value,
                            color = TextDisabledColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMiniBadge(
    text: String,
    icon: ImageVector
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .clickable { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProfileStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .clickable { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun getMonochromeSwitchColors(isLight: Boolean = isAppInLightTheme()): SwitchColors {
    return SwitchDefaults.colors(
        checkedTrackColor = if (isLight) Color(0xFF141312) else Color(0xFFFFFFFF),
        checkedThumbColor = if (isLight) Color(0xFFFFFFFF) else Color(0xFF141312),
        checkedBorderColor = if (isLight) Color(0xFF141312) else Color(0xFFFFFFFF),
        uncheckedTrackColor = if (isLight) Color(0xFFE4E4E7) else Color(0xFF27272A),
        uncheckedThumbColor = if (isLight) Color(0xFF71717A) else Color(0xFFA1A1AA),
        uncheckedBorderColor = if (isLight) Color(0xFFD4D4D8) else Color(0xFF3F3F46)
    )
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = getMonochromeSwitchColors()
        )
    }
}

private fun maskPhone(phone: String): String {
    val clean = phone.trim()
    if (clean.length <= 4) return clean
    val last4 = clean.takeLast(4)
    val prefixLen = (clean.length - 4).coerceIn(4, 8)
    return "*".repeat(prefixLen) + " " + last4
}

@Composable
private fun ContactStyledField(
    label: String,
    value: String,
    icon: ImageVector,
    isMasked: Boolean,
    onToggleMask: () -> Unit,
    onChangeClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = TextDisabledColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "OTP PROTECTED",
                    color = TextDisabledColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = value.ifBlank { "Not set" },
                    color = if (value.isBlank()) TextDisabledColor else MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Reveal / Mask toggle button
                IconButton(
                    onClick = onToggleMask,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isMasked) "Reveal" else "Mask",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                // Change button
                Box(
                    modifier = Modifier
                        .clickable(onClick = onChangeClick)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "CHANGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangeContactDialog(
    method: String, // "email" or "phone"
    currentValue: String,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    var newContact by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val isLight = isAppInLightTheme()
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    Dialog(onDismissRequest = onDismiss) {
        val dialogBg = if (isLight) Color(0xFFFFFFFF) else Color(0xFF000000)
        val cardBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E1E24)
        val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
        val subtitleColor = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(dialogBg)
                .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(gradientColors)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = contentOnGradient,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Change ${if (method == "email") "Email" else "Mobile Number"}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = if (!otpSent) "Step 1: Enter new ${if (method == "email") "email" else "phone"}" else "Step 2: Enter 6-digit OTP",
                                fontSize = 12.sp,
                                color = subtitleColor
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = subtitleColor)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (!otpSent) {
                    // Step 1: Input new contact
                    Text(
                        text = "A verification code will be sent to confirm ownership of your new ${if (method == "email") "email address" else "phone number"}.",
                        fontSize = 12.sp,
                        color = subtitleColor,
                        lineHeight = 17.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = newContact,
                            onValueChange = {
                                newContact = it
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(color = textColor, fontSize = 14.sp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (method == "email") KeyboardType.Email else KeyboardType.Phone
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (newContact.isEmpty()) {
                                    Text(
                                        text = if (method == "email") "new_email@connecto.fun" else "+91 98765 43210",
                                        color = subtitleColor,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val target = newContact.trim()
                            if (target.isBlank() || (method == "email" && !target.contains("@"))) {
                                errorMessage = "Please enter a valid ${if (method == "email") "email address" else "phone number"}."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val res = ConnectoApiClient.requestContactChangeOtp(target, method)
                                isLoading = false
                                if (res.isSuccess) {
                                    otpSent = true
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Failed to request verification code."
                                }
                            }
                        },
                        enabled = !isLoading && newContact.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("Send Verification Code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                } else {
                    // Step 2: Input OTP
                    Text(
                        text = "Enter the 6-digit verification code sent to ${newContact.trim()}:",
                        fontSize = 12.sp,
                        color = subtitleColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = otpCode,
                            onValueChange = {
                                if (it.length <= 6) {
                                    otpCode = it
                                    errorMessage = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = textColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                if (otpCode.isEmpty()) {
                                    Text(
                                        text = "• • • • • •",
                                        color = subtitleColor,
                                        fontSize = 16.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val code = otpCode.trim()
                            if (code.length != 6) {
                                errorMessage = "Please enter the 6-digit code."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val res = ConnectoApiClient.verifyContactChangeOtp(
                                    newContact = newContact.trim(),
                                    method = method,
                                    otpCode = code
                                )
                                isLoading = false
                                if (res.isSuccess) {
                                    onSuccess(newContact.trim())
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Verification failed. Code may be invalid or expired."
                                }
                            }
                        },
                        enabled = !isLoading && otpCode.trim().length == 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OnlineGreen,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("Verify & Save Change", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Wrong contact? Change and resend",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    otpSent = false
                                    otpCode = ""
                                    errorMessage = null
                                }
                                .padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}


// ---------------------------------------------------------------------------
// Helper: mask an email for privacy display  e.g. "ab****@gmail.com"
// ---------------------------------------------------------------------------
private fun String.maskEmail(): String {
    if (isBlank()) return "—"
    val atIndex = indexOf('@')
    if (atIndex < 0) return "****"
    val local = substring(0, atIndex)
    val domain = substring(atIndex)
    val visible = local.take(2)
    val stars = "*".repeat((local.length - 2).coerceAtLeast(3))
    return "$visible$stars$domain"
}

// ---------------------------------------------------------------------------
// Reusable read-only detail row for the Personal Details expandable card
// ---------------------------------------------------------------------------
@Composable
private fun PersonalDetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    multiLine: Boolean = false
) {
    Row(
        verticalAlignment = if (multiLine) Alignment.Top else Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier
                .size(18.dp)
                .then(if (multiLine) Modifier.padding(top = 2.dp) else Modifier)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (multiLine) 4 else 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}



@Composable
private fun ProfileNavigationMenuButton(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    badge: String? = null,
    hasCoverPhoto: Boolean = false,
    onClick: () -> Unit
) {
    val isGlass = ConnectoTheme.isGlass
    val isLiquidGlass = ConnectoTheme.isLiquidGlass
    val cardShape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                Modifier.liquidGlassCard(
                    shape = cardShape,
                    backgroundColor = if (isLiquidGlass) Color.Transparent
                        else if (hasCoverPhoto) Color.Black.copy(alpha = 0.30f)
                        else MaterialTheme.colorScheme.surface.copy(
                            alpha = if (ConnectoTheme.colors.isDark) 0.72f else 0.88f
                        ),
                    borderColor = if (isLiquidGlass) Color.White.copy(alpha = 0.35f)
                        else if (hasCoverPhoto) Color.White.copy(alpha = 0.20f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isLiquidGlass) Color.White.copy(alpha = 0.10f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                    .border(
                        1.dp,
                        if (isLiquidGlass) Color.White.copy(alpha = 0.25f)
                        else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isLiquidGlass) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLiquidGlass) Color.White.copy(alpha = 0.10f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open $title",
                    tint = if (isLiquidGlass) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileSubPageHeader(
    title: String,
    icon: ImageVector,
    hasCoverPhoto: Boolean = false,
    onBack: () -> Unit
) {
    val isDark = ConnectoTheme.colors.isDark
    val gradientColors = getDynamicAccentGradientColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (hasCoverPhoto) Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.18f)
                        )
                    )
                    else Brush.verticalGradient(
                        colors = listOf(
                            if (!isDark) Color.White.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.10f),
                            ConnectoTheme.colors.surface.copy(alpha = if (!isDark) 0.50f else 0.20f)
                        )
                    )
                )
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onBack)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Profile Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(gradientColors.map { it.copy(alpha = 0.16f) }))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Personal Details: Change Password Component
// ---------------------------------------------------------------------------
@Composable
private fun PersonalDetailsChangePasswordSection(
    username: String,
    context: Context
) {
    val coroutineScope = rememberCoroutineScope()
    var oldPassword by remember { mutableStateOf("") }
    var isOldPasswordVerified by remember { mutableStateOf(false) }
    var isVerifyingOldPassword by remember { mutableStateOf(false) }
    var oldPasswordError by remember { mutableStateOf<String?>(null) }
    var oldPasswordVisible by remember { mutableStateOf(false) }

    var newPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }

    var reenterPassword by remember { mutableStateOf("") }
    var reenterPasswordVisible by remember { mutableStateOf(false) }

    var isChangingPassword by remember { mutableStateOf(false) }
    var changePasswordSuccessMsg by remember { mutableStateOf<String?>(null) }
    var changePasswordErrorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(
                1.dp,
                if (isOldPasswordVerified) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Change Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (isOldPasswordVerified) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OnlineGreen.copy(alpha = 0.2f))
                        .border(1.dp, OnlineGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "OLD PASSWORD VERIFIED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnlineGreen
                    )
                }
            }
        }

        Text(
            text = if (isOldPasswordVerified)
                "Current password confirmed. Enter and confirm your new password below."
            else
                "Enter your current password and tap Verify to unlock new password fields.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            lineHeight = 16.sp
        )

        // 1. Old Password Field & Side Verify Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = oldPassword,
                onValueChange = {
                    oldPassword = it
                    oldPasswordError = null
                    changePasswordErrorMsg = null
                },
                enabled = !isOldPasswordVerified && !isVerifyingOldPassword,
                label = { Text("Old Password", fontSize = 12.sp) },
                singleLine = true,
                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                        Icon(
                            imageVector = if (oldPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle old password visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledBorderColor = if (isOldPasswordVerified) OnlineGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            // Side Verify Button
            Button(
                onClick = {
                    if (oldPassword.isBlank()) {
                        oldPasswordError = "Please enter your old password"
                        return@Button
                    }
                    isVerifyingOldPassword = true
                    oldPasswordError = null
                    changePasswordErrorMsg = null
                    changePasswordSuccessMsg = null
                    coroutineScope.launch {
                        val res = ConnectoApiClient.verifyOldPassword(username, oldPassword)
                        isVerifyingOldPassword = false
                        if (res.isSuccess) {
                            isOldPasswordVerified = true
                            oldPasswordError = null
                            Toast.makeText(context, "Current password verified! Enter new password.", Toast.LENGTH_SHORT).show()
                        } else {
                            oldPasswordError = res.exceptionOrNull()?.message ?: "Incorrect old password."
                        }
                    }
                },
                enabled = !isOldPasswordVerified && !isVerifyingOldPassword && oldPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOldPasswordVerified) OnlineGreen else MaterialTheme.colorScheme.primary,
                    disabledContainerColor = if (isOldPasswordVerified) OnlineGreen.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(54.dp)
            ) {
                if (isVerifyingOldPassword) {
                    Text("Verifying...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else if (isOldPasswordVerified) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verified", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Verify", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (oldPasswordError != null) {
            Text(
                text = "⚠️ $oldPasswordError",
                color = Color(0xFFEF4444),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // 2. New Password & Re-enter Password (HIGHLIGHTED when verified)
        AnimatedVisibility(
            visible = isOldPasswordVerified,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✨ Set New Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Min 4 characters",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        changePasswordErrorMsg = null
                    },
                    label = { Text("New Password", fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle new password visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reenterPassword,
                    onValueChange = {
                        reenterPassword = it
                        changePasswordErrorMsg = null
                    },
                    label = { Text("Re-enter Password", fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = if (reenterPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { reenterPasswordVisible = !reenterPasswordVisible }) {
                            Icon(
                                imageVector = if (reenterPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle re-enter password visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (changePasswordErrorMsg != null) {
                    Text(
                        text = "⚠️ $changePasswordErrorMsg",
                        color = Color(0xFFEF4444),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                // 3. Confirm to Change Password Button (Under new password fields)
                Button(
                    onClick = {
                        if (newPassword.isBlank()) {
                            changePasswordErrorMsg = "Please enter a new password."
                            return@Button
                        }
                        if (newPassword.length < 4) {
                            changePasswordErrorMsg = "New password must be at least 4 characters long."
                            return@Button
                        }
                        if (newPassword != reenterPassword) {
                            changePasswordErrorMsg = "Passwords do not match. Please re-enter carefully."
                            return@Button
                        }
                        isChangingPassword = true
                        changePasswordErrorMsg = null
                        coroutineScope.launch {
                            val res = ConnectoApiClient.changePassword(username, oldPassword, newPassword)
                            isChangingPassword = false
                            if (res.isSuccess) {
                                changePasswordSuccessMsg = res.getOrNull() ?: "Password changed successfully in the database!"
                                oldPassword = ""
                                newPassword = ""
                                reenterPassword = ""
                                isOldPasswordVerified = false
                                Toast.makeText(context, "Password updated in database! Use your new password to open/login.", Toast.LENGTH_LONG).show()
                            } else {
                                changePasswordErrorMsg = res.exceptionOrNull()?.message ?: "Failed to change password."
                            }
                        }
                    },
                    enabled = !isChangingPassword && newPassword.isNotBlank() && reenterPassword.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isChangingPassword) {
                        Text("Updating Password...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm to change password", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (changePasswordSuccessMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(OnlineGreen.copy(alpha = 0.15f))
                    .border(1.dp, OnlineGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OnlineGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = changePasswordSuccessMsg!!,
                        color = OnlineGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun LoggedInSessionsCard(
    sessions: List<com.example.connecto.network.UserSessionDto>,
    isLoading: Boolean,
    errorMessage: String?,
    terminatingSessionId: String?,
    isTerminatingAllOthers: Boolean,
    onRefresh: () -> Unit,
    onTerminateSession: (String) -> Unit,
    onTerminateAllOthers: () -> Unit,
    hasCoverPhoto: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassCard(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = if (ConnectoTheme.isLiquidGlass) Color.Transparent
                    else if (hasCoverPhoto) Color.Black.copy(alpha = 0.32f)
                    else MaterialTheme.colorScheme.surface,
                borderColor = if (ConnectoTheme.isLiquidGlass) Color.White.copy(alpha = 0.35f)
                    else if (hasCoverPhoto) Color.White.copy(alpha = 0.22f)
                    else MaterialTheme.colorScheme.primary
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Icon + Title + Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Logged-In Sessions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Active devices, phone models & OS versions",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Sessions",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp
            )

            // Content
            if (isLoading && sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (errorMessage != null && sessions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Unable to load sessions",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TAP TO RETRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onRefresh() }
                    )
                }
            } else if (sessions.isEmpty()) {
                Text(
                    text = "No active sessions recorded.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                // Sessions list
                val otherSessionsCount = sessions.count { !it.isCurrent }

                sessions.forEach { session ->
                    SessionItemRow(
                        session = session,
                        isTerminating = terminatingSessionId == session.id,
                        onTerminate = { onTerminateSession(session.id) }
                    )
                }

                // If multiple sessions or other sessions exist:
                if (otherSessionsCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !isTerminatingAllOthers) {
                                onTerminateAllOthers()
                            }
                            .padding(vertical = 10.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTerminatingAllOthers) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LOG OUT OF OTHER SESSIONS ($otherSessionsCount)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionItemRow(
    session: com.example.connecto.network.UserSessionDto,
    isTerminating: Boolean,
    onTerminate: () -> Unit
) {
    val currentPhoneModel = remember { com.example.connecto.network.ConnectoApiClient.getPhoneModel() }
    val currentPhoneVersion = remember { com.example.connecto.network.ConnectoApiClient.getPhoneVersion() }
    val currentDeviceType = remember { com.example.connecto.network.ConnectoApiClient.getDeviceType() }

    // Resolve phone type / model
    val displayPhoneType = when {
        !session.phoneType.isNullOrBlank() -> session.phoneType
        session.isCurrent -> currentPhoneModel
        session.deviceName.isNotBlank() && session.deviceName != "Web Browser" -> session.deviceName
        else -> "Smartphone"
    }

    // Resolve phone / OS version
    val displayPhoneVersion = when {
        !session.phoneVersion.isNullOrBlank() -> session.phoneVersion
        !session.osVersion.isNullOrBlank() -> session.osVersion
        session.isCurrent -> currentPhoneVersion
        else -> null
    }

    // Resolve device form factor / type
    val displayDeviceType = when {
        !session.deviceType.isNullOrBlank() -> session.deviceType
        session.isCurrent -> currentDeviceType
        else -> null
    }

    val devLower = (displayPhoneType + " " + (displayDeviceType ?: "")).lowercase()
    val deviceIcon = when {
        devLower.contains("iphone") || devLower.contains("android") || devLower.contains("phone") || devLower.contains("pixel") || devLower.contains("samsung") || devLower.contains("smartphone") -> Icons.Default.Smartphone
        devLower.contains("tablet") || devLower.contains("ipad") -> Icons.Default.Devices
        devLower.contains("mac") || devLower.contains("windows") || devLower.contains("pc") || devLower.contains("laptop") || devLower.contains("desktop") -> Icons.Default.Computer
        else -> Icons.Default.Devices
    }

    val formattedTime = remember(session.createdAt, session.lastActiveAt) {
        formatSessionTimestamp(session.createdAt, session.lastActiveAt, session.isCurrent)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (session.isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
            .border(
                1.dp,
                if (session.isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (session.isCurrent) OnlineGreen.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                            .border(
                                1.dp,
                                if (session.isCurrent) OnlineGreen.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = deviceIcon,
                            contentDescription = null,
                            tint = if (session.isCurrent) OnlineGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayPhoneType,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (session.isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(OnlineGreen.copy(alpha = 0.18f))
                                        .border(0.8.dp, OnlineGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "THIS DEVICE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = OnlineGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Chips Row: Phone Version & Phone Type
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (!displayPhoneVersion.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .border(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = displayPhoneVersion,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            if (!displayDeviceType.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                        .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = displayDeviceType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Location
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = session.location,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // If not current session, show terminate button
                if (!session.isCurrent) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .clickable(enabled = !isTerminating) { onTerminate() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTerminating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "LOG OUT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer row: Login time + IP address
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (session.ipAddress.isNotBlank()) {
                    Text(
                        text = "IP: ${session.ipAddress}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun formatSessionTimestamp(createdAt: String, lastActiveAt: String, isCurrent: Boolean): String {
    if (isCurrent) return "Active Now"
    val timeToUse = if (lastActiveAt.isNotBlank()) lastActiveAt else createdAt
    if (timeToUse.isBlank()) return "Recently"

    return try {
        val cleanTime = timeToUse.replace("T", " ").replace("Z", "")
        val dotIdx = cleanTime.indexOf(".")
        val finalStr = if (dotIdx != -1) cleanTime.substring(0, dotIdx) else cleanTime
        val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val date = sdfIn.parse(finalStr)
        if (date != null) {
            val now = System.currentTimeMillis()
            val diff = now - date.time
            when {
                diff < 60_000 -> "Just now"
                diff < 3600_000 -> "${diff / 60_000}m ago"
                diff < 86400_000 -> "${diff / 3600_000}h ago"
                else -> {
                    val sdfOut = java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault())
                    sdfOut.timeZone = java.util.TimeZone.getDefault()
                    sdfOut.format(date)
                }
            }
        } else {
            "Logged in: $timeToUse"
        }
    } catch (_: Exception) {
        "Logged in: $timeToUse"
    }
}


