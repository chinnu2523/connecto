package com.example.connecto.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
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
import androidx.compose.material3.IconButton
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.LockReset
import com.example.connecto.ui.components.CropMode
import com.example.connecto.ui.components.PhotoLightboxDialog
import com.example.connecto.ui.components.InteractiveCropFitDialog
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import com.example.connecto.ui.components.FullScreenGroupProfileDialog
import com.example.connecto.ui.designsystem.ConnectoAvatar
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.connecto.ui.components.AddFriendDialog
import com.example.connecto.ui.components.ConnectoTopHeader
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.TextDisabledColor
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.pressScaleEffect
import com.example.connecto.ui.theme.rotatingGlowHalo
import com.example.connecto.ui.designsystem.sanitizeAvatarUrl
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

@Composable
fun ProfileScreen(
    username: String = "Gamer",
    currentThemeMode: com.example.connecto.ui.designsystem.ThemeMode = com.example.connecto.ui.designsystem.ThemeMode.NOIR,
    onSelectThemeMode: (com.example.connecto.ui.designsystem.ThemeMode) -> Unit = {},
    isDarkTheme: Boolean = currentThemeMode.isDark,
    onToggleTheme: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {},
    onLockApp: () -> Unit = {},
    onOpenNotifications: (() -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = remember(context) { context.findFragmentActivity() }
    val prefs = remember { context.getSharedPreferences("connecto_user_profile_prefs", Context.MODE_PRIVATE) }

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
    var bannerPhotoUri by remember(username) {
        mutableStateOf(prefs.getString("banner_uri_$username", null))
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
        mutableStateOf(!prefs.getBoolean("stealth_mode_$username", false))
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

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var show2FaSetupDialog by remember { mutableStateOf(false) }
    var twoFaSetupMaskedDest by remember { mutableStateOf<String?>(null) }
    var twoFaErrorMessage by remember { mutableStateOf<String?>(null) }

    // User Groups Management State in Profile Tab
    val profileGroupsList = remember { mutableStateListOf<FriendItem>() }
    var isLoadingProfileGroups by remember { mutableStateOf(false) }
    var selectedGroupForProfileDialog by remember { mutableStateOf<FriendItem?>(null) }
    var groupToConfirmDelete by remember { mutableStateOf<FriendItem?>(null) }
    var isDeletingProfileGroup by remember { mutableStateOf(false) }
    var groupToConfirmLeave by remember { mutableStateOf<FriendItem?>(null) }
    var isLeavingProfileGroup by remember { mutableStateOf(false) }

    // Function to reload groups for profile tab
    fun reloadProfileGroups() {
        coroutineScope.launch {
            isLoadingProfileGroups = true
            try {
                val groupsRes = ConnectoApiClient.getUserGroups()
                if (groupsRes.isSuccess) {
                    val serverGroups = groupsRes.getOrThrow()
                    val myUsernameLower = username.trim().lowercase().removePrefix("@")
                    val mappedGroups = serverGroups.map { g ->
                        val init = if (g.name.isNotBlank()) g.name.first().toString().uppercase() else "G"
                        val count = if (g.memberCount > 0) g.memberCount else (g.members.size.coerceAtLeast(1))
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
                            isOnline = true,
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
                    profileGroupsList.removeAll { it.id !in gIds }
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
                    bannerPhotoUri = prof.bannerUrl
                    prefs.edit().putString("banner_uri_${prof.username}", prof.bannerUrl).apply()
                    bannerUrlFieldInput = prof.bannerUrl
                }
            }
        } catch (e: Exception) {
            // Keep local fallback
        }
    }

    // Section Expand/Collapse State (Clean profile view: details show only on clicking dedicated button)
    var expandedSection by rememberSaveable { mutableStateOf<String?>(null) }
    val toggleSection: (String) -> Unit = { sectionKey ->
        expandedSection = if (expandedSection == sectionKey) null else sectionKey
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
                val res = ConnectoApiClient.updateProfile(
                    username = currentUsernameState,
                    displayName = displayNameInput,
                    fullName = fullNameInput,
                    bio = bioInput,
                    avatarUrl = profilePhotoUri,
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

    // Photo Picker Launcher -> opens interactive crop dialog
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
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
        contract = ActivityResultContracts.PickVisualMedia()
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ========== SHARED TOP HEADER ==========
            ConnectoTopHeader(
                title = "PROFILE SETTINGS",
                subtitle = if (stealthModeEnabled) "Stealth Mode Active" else "@$username",
                userInitial = userInitial,
                avatarUrl = profilePhotoUri,
                isStealthModeOn = stealthModeEnabled,
                isOnline = true,
                showProfileButton = false
            )

            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            val parallaxTranslationY by remember {
                androidx.compose.runtime.derivedStateOf {
                    if (listState.firstVisibleItemIndex <= 1) {
                        (listState.firstVisibleItemScrollOffset * 0.45f).coerceAtMost(100f)
                    } else 0f
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // ================= AUTO-SAVE CONFIRMATION BADGE =================
                item(key = "auto_save_badge") {
                    AnimatedVisibility(
                        visible = showSavedBadge,
                        enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + androidx.compose.animation.scaleIn(
                            initialScale = 0.85f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                        ),
                        exit = fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.85f)
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

                // ================= 1. AVATAR & USER SUMMARY CARD WITH COVER =================
                item(key = "avatar_summary_card") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    )
                                ),
                                RoundedCornerShape(24.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Centered Avatar with Glow
                            Box(
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(3.dp)
                                        .rotatingGlowHalo(
                                            glowColors = gradientColors,
                                            borderWidth = 3.dp,
                                            durationMillis = 4500
                                        )
                                        .pressScaleEffect(
                                            onClick = {
                                                lightboxTitle = "Profile Photo"
                                                lightboxImageUrl = profilePhotoUri
                                                isLightboxBanner = false
                                                showLightbox = true
                                            },
                                            targetScale = 0.92f
                                        )
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val sanitizedProfilePhoto = sanitizeAvatarUrl(profilePhotoUri)
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
                                            fontSize = 38.sp
                                        )
                                        if (sanitizedProfilePhoto != null) {
                                            AsyncImage(
                                                model = sanitizedProfilePhoto,
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
                                                modifier = Modifier.size(28.dp),
                                                color = Color.White,
                                                strokeWidth = 3.dp
                                            )
                                        }
                                    }
                                }

                                // Camera Badge Trigger
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                        .pressScaleEffect(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            targetScale = 0.88f
                                        )
                                        .background(Brush.linearGradient(gradientColors)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Upload Profile Photo",
                                        tint = contentOnGradient,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Name & Verified Badge
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

                                // Cloud Synced Pill
                                if (profilePhotoUri?.startsWith("http") == true && profilePhotoUri != "null") {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(OnlineGreen.copy(alpha = 0.12f))
                                            .border(1.dp, OnlineGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = OnlineGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "Synced",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = OnlineGreen
                                            )
                                        }
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
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
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

                // ================= PRESENCE & STEALTH MODE =================
                item(key = "presence_stealth_section") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionHeader(
                            title = "Presence & Stealth Mode",
                            subtitle = "Control how others see your online status",
                            icon = Icons.Default.VisibilityOff,
                            badgeText = if (stealthModeEnabled) "STEALTH" else "ONLINE",
                            badgeColor = if (stealthModeEnabled) ConnectoTheme.colors.info else OnlineGreen,
                            isExpanded = expandedSection == "presence_stealth",
                            onClick = { toggleSection("presence_stealth") }
                        )

                        AnimatedVisibility(
                            visible = expandedSection == "presence_stealth",
                            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                    expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
                            exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                                   shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (stealthModeEnabled) ConnectoTheme.colors.info.copy(alpha = 0.08f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        1.dp,
                                        if (stealthModeEnabled) ConnectoTheme.colors.info.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(18.dp)
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
                }

                // ================= PERSONAL DETAILS SECTION =================
                item(key = "personal_details_section") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionHeader(
                            title = "Personal Information",
                            subtitle = "Manage your identity, personal information, and contact details",
                            icon = Icons.Default.Person,
                            badgeText = if (expandedSection == "personal_details") "OPEN" else "DETAILS",
                            badgeColor = MaterialTheme.colorScheme.primary,
                            isExpanded = expandedSection == "personal_details",
                            onClick = { toggleSection("personal_details") }
                        )

                        AnimatedVisibility(
                            visible = expandedSection == "personal_details",
                            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                    expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
                            exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                                   shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
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
                                            .pressScaleEffect(
                                                onClick = { triggerAutoSave() },
                                                targetScale = 0.96f
                                            )
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
                }

                // ================= 7B. TWO-FACTOR AUTH (2FA) & PASSWORD RECOVERY =================
                item(key = "two_factor_auth_section") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionHeader(
                            title = "Two-Factor Auth & Password Security",
                            subtitle = "Email 2FA verification & instant inbox recovery",
                            icon = Icons.Default.Shield,
                            badgeText = if (twoFactorEnabled) "ENABLED" else "DISABLED",
                            badgeColor = if (twoFactorEnabled) OnlineGreen else MaterialTheme.colorScheme.error,
                            isExpanded = expandedSection == "two_factor_auth",
                            onClick = { toggleSection("two_factor_auth") }
                        )

                        AnimatedVisibility(
                            visible = expandedSection == "two_factor_auth",
                            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                    expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
                            exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                                   shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
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
                                                .pressScaleEffect(
                                                    onClick = { showForgotPasswordDialog = true },
                                                    targetScale = 0.93f
                                                )
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
                                            coroutineScope.launch {
                                                val res = ConnectoApiClient.toggle2Fa(false, token = activeSessionToken)
                                                if (res.isSuccess) {
                                                    twoFactorEnabled = false
                                                    prefs.edit().putBoolean("two_factor_enabled_$username", false).apply()
                                                    twoFaErrorMessage = null
                                                } else {
                                                    twoFaErrorMessage = res.exceptionOrNull()?.message ?: "Failed to disable 2FA."
                                                }
                                            }
                                        } else {
                                            if (emailInput.isBlank()) {
                                                twoFaErrorMessage = "Please ensure your email address is set in your profile above first."
                                            } else {
                                                twoFaErrorMessage = null
                                                coroutineScope.launch {
                                                    val res = ConnectoApiClient.request2FaSetupOtp(
                                                        method = "email",
                                                        phoneNumber = emailInput.trim(),
                                                        token = activeSessionToken
                                                    )
                                                    if (res.isSuccess) {
                                                        val data = res.getOrNull()
                                                        twoFaSetupMaskedDest = data?.maskedDestination
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
                }

                // ================= 7C. BIOMETRIC & APP LOCK =================
                item(key = "biometric_vault_section") {
                    val isDeviceSecure = remember(context) { BiometricAuthManager.isDeviceSecure(context) }
                    val isBioReady = remember(context) { BiometricAuthManager.checkBiometricAvailability(context) == BiometricStatus.READY }
                    val statusDescription = remember(context) { BiometricAuthManager.getStatusDescription(context) }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionHeader(
                            title = "Biometric & App Lock",
                            subtitle = "Biometric (Fingerprint/Face) & Device Screen Lock security",
                            icon = Icons.Default.Fingerprint,
                            badgeText = if (isAppLockEnabled) "PROTECTED" else "UNLOCKED",
                            badgeColor = if (isAppLockEnabled) OnlineGreen else TextDisabledColor,
                            isExpanded = expandedSection == "biometric_vault",
                            onClick = { toggleSection("biometric_vault") }
                        )

                        AnimatedVisibility(
                            visible = expandedSection == "biometric_vault",
                            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                    expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
                            exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                                   shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
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
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Biometric App Lock",
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
                                                    text = if (isAppLockEnabled) "Require biometric scan or device screen lock to open" else "Unlock app without biometric prompt",
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
                                                        "Biometric App Lock enabled. Unlock with Biometrics, Screen Lock, or Password.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                isAppLockEnabled = isChecked
                                                prefs.edit().putBoolean("biometric_lock_$username", isChecked).apply()
                                            },
                                            colors = getMonochromeSwitchColors()
                                        )
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
                                                    imageVector = if (isAppLockEnabled) Icons.Default.Shield else Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = if (isAppLockEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Device Hardware Security",
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

                                    // 3. Auto-Lock on App Exit / Background Toggle
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
                }

                // ================= 7D. APPEARANCE & DISPLAY THEME =================
                item(key = "appearance_theme_section") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionHeader(
                            title = "Appearance & Display Theme",
                            subtitle = "Personalize your visual experience across the entire app",
                            icon = Icons.Default.Tune,
                            badgeText = currentThemeMode.displayName.uppercase(),
                            badgeColor = MaterialTheme.colorScheme.primary,
                            isExpanded = expandedSection == "appearance_theme",
                            onClick = { toggleSection("appearance_theme") }
                        )

                        AnimatedVisibility(
                            visible = expandedSection == "appearance_theme",
                            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                    expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
                            exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                                   shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                                    .padding(16.dp)
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

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 1.dp)

                                    Text(
                                        text = "SELECT COLOR PALETTE & ATMOSPHERE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // 2. Theme Selection Cards Grid (All 5 themes)
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        com.example.connecto.ui.designsystem.ThemeMode.values().forEach { mode ->
                                            val isSelected = currentThemeMode == mode
                                            val modeColors = com.example.connecto.ui.designsystem.getColorsForTheme(mode)

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                                    )
                                                    .border(
                                                        width = if (isSelected) 2.dp else 1.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(14.dp)
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
                                                        // Live Palette Preview Pill
                                                        Box(
                                                            modifier = Modifier
                                                                .size(42.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(modeColors.background)
                                                                .border(1.dp, modeColors.border, RoundedCornerShape(10.dp)),
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

                                                    // Right: Color Swatches Preview
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Primary accent swatch
                                                        Box(
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clip(CircleShape)
                                                                .background(modeColors.primary)
                                                                .border(1.dp, modeColors.border, CircleShape)
                                                        )
                                                        // Info / Secondary swatch
                                                        Box(
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clip(CircleShape)
                                                                .background(modeColors.info)
                                                                .border(1.dp, modeColors.border, CircleShape)
                                                        )
                                                        // Surface swatch
                                                        Box(
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clip(CircleShape)
                                                                .background(modeColors.surface)
                                                                .border(1.dp, modeColors.border, CircleShape)
                                                        )
                                                        // Background swatch
                                                        Box(
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clip(CircleShape)
                                                                .background(modeColors.background)
                                                                .border(1.dp, modeColors.border, CircleShape)
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

                // ================= 7. GROUPS & COMMUNITIES MANAGEMENT (Delete for Admin / Exit for Member) =================
                item(key = "groups_management_section") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionHeader(
                            title = "Groups & Communities",
                            subtitle = "Manage created & joined groups",
                            icon = Icons.Default.Groups,
                            badgeText = if (isLoadingProfileGroups) "LOADING" else "${profileGroupsList.size} GROUPS",
                            badgeColor = MaterialTheme.colorScheme.primary,
                            isExpanded = expandedSection == "groups_management",
                            onClick = { toggleSection("groups_management") }
                        )

                        AnimatedVisibility(
                            visible = expandedSection == "groups_management",
                            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                    expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
                            exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                                   shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
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
                }

                // ================= 8. PRIMARY SIGN OUT BUTTON =================
                item(key = "sign_out_button") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .pressScaleEffect(
                                onClick = onSignOut,
                                targetScale = 0.95f
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
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
                            try {
                                val res = ConnectoApiClient.deleteGroup(targetGroup.id)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Group #${targetGroup.name} deleted", Toast.LENGTH_SHORT).show()
                                    profileGroupsList.removeAll { it.id == targetGroup.id }
                                    groupToConfirmDelete = null
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
                            try {
                                val res = ConnectoApiClient.leaveGroup(targetGroup.id)
                                if (res.isSuccess) {
                                    Toast.makeText(context, "You left #${targetGroup.name}", Toast.LENGTH_SHORT).show()
                                    profileGroupsList.removeAll { it.id == targetGroup.id }
                                    groupToConfirmLeave = null
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
                    bannerPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            },
            onDismiss = { showLightbox = false }
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
                        }
                        triggerAutoSave()
                    }
                } else {
                    isUploadingBanner = true
                    coroutineScope.launch {
                        try {
                            val result = ConnectoApiClient.uploadBannerBitmap(context, croppedBitmap)
                            if (result.isSuccess) {
                                val serverUrl = result.getOrThrow()
                                if (serverUrl.isNotBlank()) {
                                    bannerPhotoUri = serverUrl
                                    bannerUrlFieldInput = serverUrl
                                    prefs.edit().putString("banner_uri_$username", serverUrl).apply()
                                }
                            }
                        } finally {
                            isUploadingBanner = false
                        }
                        triggerAutoSave()
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

    if (show2FaSetupDialog) {
        TwoFactorSetupDialog(
            method = "email",
            destination = emailInput.trim(),
            maskedDestination = twoFaSetupMaskedDest,
            onDismiss = { show2FaSetupDialog = false },
            onSuccess = {
                show2FaSetupDialog = false
                twoFactorEnabled = true
                prefs.edit().putBoolean("two_factor_enabled_$username", true).apply()
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

}

// ================= HELPER COMPOSABLES =================

@Composable
private fun TwoFactorSetupDialog(
    method: String,
    destination: String,
    maskedDestination: String?,
    devOtp: String? = null,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val isLight = isAppInLightTheme()
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    // Real-time verification when 6 digits are typed
    LaunchedEffect(otpCode) {
        val clean = otpCode.trim()
        if (clean.length == 6 && !isVerifying) {
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
                    errorMessage = res.exceptionOrNull()?.message ?: "Invalid verification code."
                }
            }
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
                    text = "A 6-digit setup code was $destText.\nLook for the email titled '[Connecto 2FA Setup]'.\n(Note: Do not enter older password recovery codes).",
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
                        if (otpCode.length < 6) {
                            errorMessage = "Please enter the 6-digit code."
                            return@Button
                        }
                        isVerifying = true
                        errorMessage = null
                        coroutineScope.launch {
                            val res = ConnectoApiClient.verify2FaSetupOtp(
                                otpCode = otpCode.trim(),
                                method = method,
                                phoneNumber = destination
                            )
                            isVerifying = false
                            if (res.isSuccess && res.getOrNull()?.twoFactorEnabled == true) {
                                onSuccess()
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "2FA verification failed."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = !isVerifying && otpCode.length == 6
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Brush.horizontalGradient(gradientColors), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(color = contentOnGradient, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Text("CONFIRM & ACTIVATE 2FA", color = contentOnGradient, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
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
            .pressScaleEffect(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                targetScale = 0.94f
            )
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
            .pressScaleEffect(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                targetScale = 0.93f
            )
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
                        .pressScaleEffect(onClick = onChangeClick, targetScale = 0.94f)
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
    var devOtp by remember { mutableStateOf<String?>(null) }
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
                                    val obj = res.getOrThrow()
                                    val devCode = obj.optString("dev_otp").takeIf { it.isNotEmpty() }
                                    devOtp = devCode
                                    if (devCode != null) {
                                        otpCode = devCode
                                    }
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

                    if (devOtp != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ConnectoTheme.colors.success.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Dev OTP Auto-detected: $devOtp",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ConnectoTheme.colors.success
                            )
                        }
                    }

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

