package com.example.connecto.ui.screens

import android.content.Context
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import com.example.connecto.crypto.ConnectoE2EEncryption
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.fragment.app.FragmentActivity
import android.content.ContextWrapper
import com.example.connecto.security.BiometricAuthManager
import com.example.connecto.ui.components.PhotoLightboxDialog
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.connecto.data.ConnectoDatabaseHelper
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.collectAsState
import com.example.connecto.ui.components.AddFriendDialog
import com.example.connecto.ui.components.CreateGroupChatDialog
import com.example.connecto.ui.components.ConnectoEmojiPicker
import com.example.connecto.ui.components.ConnectoTopHeader
import com.example.connecto.ui.components.WebRtcCallOverlay
import com.example.connecto.voice.CallState
import com.example.connecto.voice.VoiceCallManager
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.TextDisabledColor
import com.example.connecto.ui.theme.ShimmerConversationItem
import com.example.connecto.ui.theme.ShimmerMessageFeed
import com.example.connecto.ui.theme.StaggeredReveal
import com.example.connecto.ui.theme.breathingPulse
import com.example.connecto.ui.theme.pressScaleEffect
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.ProfileDataDto
import java.util.UUID
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.designsystem.ConnectoAvatar
import com.example.connecto.ui.designsystem.ConnectoPresenceStatus
import com.example.connecto.ui.designsystem.ConnectoTheme
import com.example.connecto.ui.theme.isAppInLightTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun formatIsoTime(isoString: String): String {
    return try {
        val trimmed = isoString.trim()
        if (trimmed.isEmpty() || trimmed.equals("just now", ignoreCase = true)) {
            return "Just now"
        }
        if (trimmed.equals("active", ignoreCase = true)) {
            return "Active"
        }
        // Check if already in friendly format like "10:14 AM" or "01:02 PM"
        if (trimmed.matches(Regex("""\d{1,2}:\d{2}\s*(?:AM|PM|am|pm)?"""))) {
            return trimmed
        }
        // Extract time part from ISO or SQL timestamp (handles 'T' or ' ' delimiter)
        val timePart = when {
            trimmed.contains("T") -> trimmed.substringAfter("T").substringBefore(".").substringBefore("Z")
            trimmed.contains(" ") -> trimmed.substringAfter(" ").substringBefore(".")
            else -> trimmed
        }
        val parts = timePart.split(":")
        if (parts.size >= 2) {
            var hour = parts[0].trim().toIntOrNull() ?: 0
            val min = parts[1].trim()
            val ampm = if (hour >= 12) "PM" else "AM"
            if (hour > 12) hour -= 12
            if (hour == 0) hour = 12
            "$hour:$min $ampm"
        } else {
            "Just now"
        }
    } catch (e: Exception) {
        "Just now"
    }
}

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

data class ChatThemeOption(
    val id: String,
    val name: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val ownBubbleBgLight: Color,
    val ownBubbleBgDark: Color,
    val ownBubbleTextLight: Color,
    val ownBubbleTextDark: Color,
    val friendBubbleBgLight: Color,
    val friendBubbleBgDark: Color,
    val friendBubbleTextLight: Color,
    val friendBubbleTextDark: Color
)

val AVAILABLE_CHAT_THEMES = listOf(
    ChatThemeOption(
        id = "default",
        name = "Pure Monochrome",
        subtitle = "Editorial Noir / Parchment contrast",
        primaryColor = Color(0xFF6C5CE7),
        secondaryColor = Color(0xFFA29BFE),
        ownBubbleBgLight = Color(0xFF141312),
        ownBubbleBgDark = Color(0xFFFFFFFF),
        ownBubbleTextLight = Color(0xFFFFFFFF),
        ownBubbleTextDark = Color(0xFF000000),
        friendBubbleBgLight = Color(0xFFFFFFFF),
        friendBubbleBgDark = Color(0xFF18181B),
        friendBubbleTextLight = Color(0xFF141312),
        friendBubbleTextDark = Color(0xFFFFFFFF)
    ),
    ChatThemeOption(
        id = "cyberpunk",
        name = "Cyberpunk Neon",
        subtitle = "Electric violet & luminous cyan glow",
        primaryColor = Color(0xFFD946EF),
        secondaryColor = Color(0xFF06B6D4),
        ownBubbleBgLight = Color(0xFF9333EA),
        ownBubbleBgDark = Color(0xFF7E22CE),
        ownBubbleTextLight = Color(0xFFFFFFFF),
        ownBubbleTextDark = Color(0xFFFFFFFF),
        friendBubbleBgLight = Color(0xFFF3E8FF),
        friendBubbleBgDark = Color(0xFF1E112A),
        friendBubbleTextLight = Color(0xFF581C87),
        friendBubbleTextDark = Color(0xFFF3E8FF)
    ),
    ChatThemeOption(
        id = "midnight",
        name = "Midnight Azure",
        subtitle = "Deep oceanic navy & azure stream",
        primaryColor = Color(0xFF2563EB),
        secondaryColor = Color(0xFF38BDF8),
        ownBubbleBgLight = Color(0xFF1D4ED8),
        ownBubbleBgDark = Color(0xFF1E40AF),
        ownBubbleTextLight = Color(0xFFFFFFFF),
        ownBubbleTextDark = Color(0xFFFFFFFF),
        friendBubbleBgLight = Color(0xFFEFF6FF),
        friendBubbleBgDark = Color(0xFF0F172A),
        friendBubbleTextLight = Color(0xFF1E3A8A),
        friendBubbleTextDark = Color(0xFFE0F2FE)
    ),
    ChatThemeOption(
        id = "emerald",
        name = "Emerald Matrix",
        subtitle = "Obsidian deep with matrix emerald",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF059669),
        ownBubbleBgLight = Color(0xFF047857),
        ownBubbleBgDark = Color(0xFF065F46),
        ownBubbleTextLight = Color(0xFFFFFFFF),
        ownBubbleTextDark = Color(0xFFFFFFFF),
        friendBubbleBgLight = Color(0xFFECFDF5),
        friendBubbleBgDark = Color(0xFF062319),
        friendBubbleTextLight = Color(0xFF064E3B),
        friendBubbleTextDark = Color(0xFFD1FAE5)
    ),
    ChatThemeOption(
        id = "sunset",
        name = "Solar Sunset",
        subtitle = "Vibrant coral & warm amber horizon",
        primaryColor = Color(0xFFF97316),
        secondaryColor = Color(0xFFF43F5E),
        ownBubbleBgLight = Color(0xFFEA580C),
        ownBubbleBgDark = Color(0xFFC2410C),
        ownBubbleTextLight = Color(0xFFFFFFFF),
        ownBubbleTextDark = Color(0xFFFFFFFF),
        friendBubbleBgLight = Color(0xFFFFF7ED),
        friendBubbleBgDark = Color(0xFF29150B),
        friendBubbleTextLight = Color(0xFF9A3412),
        friendBubbleTextDark = Color(0xFFFFEDD5)
    )
)

data class ChatWallpaperOption(
    val id: String,
    val name: String,
    val subtitle: String,
    val lightBrush: Brush,
    val darkBrush: Brush
)

val AVAILABLE_CHAT_WALLPAPERS = listOf(
    ChatWallpaperOption(
        id = "default",
        name = "Default Clean",
        subtitle = "Subtle theme background",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFFBF8F2), Color(0xFFF5F1E8))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF0A0A0A)))
    ),
    ChatWallpaperOption(
        id = "oled",
        name = "True 0-Nit OLED",
        subtitle = "Pitch black battery-saver luxury",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFAFAFA))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF000000)))
    ),
    ChatWallpaperOption(
        id = "mesh_dark",
        name = "Cosmic Mesh",
        subtitle = "Deep nebula slate with violet aura",
        lightBrush = Brush.radialGradient(listOf(Color(0xFFEDE9FE), Color(0xFFF1F5F9))),
        darkBrush = Brush.radialGradient(listOf(Color(0xFF1E1035), Color(0xFF090614)))
    ),
    ChatWallpaperOption(
        id = "indigo_flow",
        name = "Indigo Gradient",
        subtitle = "Soft twilight sky gradient",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1E1B4B)))
    ),
    ChatWallpaperOption(
        id = "emerald_glow",
        name = "Emerald Matrix",
        subtitle = "Deep tactical aurora matrix",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF041C14), Color(0xFF020E0A)))
    ),
    ChatWallpaperOption(
        id = "parchment_warm",
        name = "Archival Paper",
        subtitle = "Warm vintage ivory grain",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFFBF8F2), Color(0xFFF3EDE2))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF1C1917), Color(0xFF0C0A09)))
    )
)

data class FriendItem(
    val id: String,
    val name: String,
    val handle: String,
    val initial: String,
    val status: String,
    val bio: String,
    val lastMessage: String,
    val timeAgo: String = "10:14 AM",
    var unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val avatarUrl: String? = null
)

data class CustomMessageItem(
    val id: String,
    val senderName: String,
    val initial: String,
    val content: String,
    val time: String,
    val isMe: Boolean,
    var isDelivered: Boolean = true,
    var isRead: Boolean = false,
    var reaction: String? = null,
    var showReactionPill: Boolean = false,
    val avatarUrl: String? = null,
    val replyToId: String? = null,
    val replyToContent: String? = null,
    val replyToAuthor: String? = null,
    val isEncrypted: Boolean = false
)

@Composable
fun CustomizedChatScreen(
    currentUsername: String,
    onOpenProfile: () -> Unit,
    onUnreadStatusChanged: (Boolean) -> Unit = {},
    onDirectChatStateChanged: (Boolean) -> Unit = {},
    onOpenNotifications: (() -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    targetChatUserOrChannel: String? = null,
    onTargetChatHandled: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFriend by remember { mutableStateOf<FriendItem?>(null) }
    var selectedFriendProfileData by remember { mutableStateOf<ProfileDataDto?>(null) }
    var friendProfileSelectedTab by remember { mutableStateOf("settings") } // "settings", "customization", "actions"
    var showFriendProfileDialog by remember { mutableStateOf(false) }
    var showUnfriendConfirmDialog by remember { mutableStateOf(false) }
    var isUnfriending by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showCreateGroupChatDialog by remember { mutableStateOf(false) }
    var pendingRequestsCount by remember { mutableIntStateOf(0) }
    var friendSearchQuery by remember { mutableStateOf("") }
    var typedMessage by remember { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }
    var isFriendTyping by remember { mutableStateOf(false) }
    var isLoadingMessages by remember { mutableStateOf(false) }
    val messageDrafts = remember { mutableStateMapOf<String, String>() }
    var replyingTo by remember { mutableStateOf<CustomMessageItem?>(null) }

    // Active Call State
    val callState by VoiceCallManager.callState.collectAsState()
    val activeCallTitle by VoiceCallManager.activeCallTitle.collectAsState()
    val activeCallPartner by VoiceCallManager.activeCallPartner.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Send Button Animation State
    var isSendPressed by remember { mutableStateOf(false) }
    val sendButtonScale by animateFloatAsState(
        targetValue = if (isSendPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "sendScale"
    )

    val context = LocalContext.current
    val readPrefs = remember { context.getSharedPreferences("connecto_chat_read_prefs", Context.MODE_PRIVATE) }
    val friendPrefs = remember { context.getSharedPreferences("connecto_friend_prefs", Context.MODE_PRIVATE) }
    val notifPrefs = remember { context.getSharedPreferences("connecto_notifications_prefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { ConnectoDatabaseHelper.getInstance(context) }

    // Friend Profile Preview Lightbox & Per-Chat Preferences
    var showPhotoLightbox by remember { mutableStateOf(false) }
    var isChatMuted by remember { mutableStateOf(false) }
    var isChatLocked by remember { mutableStateOf(false) }
    var chatThemeId by remember { mutableStateOf("default") }
    var chatWallpaperId by remember { mutableStateOf("default") }
    var customWallpaperUri by remember { mutableStateOf<String?>(null) }

    // Custom Wallpaper Photo Picker Launcher
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val uriStr = uri.toString()
            customWallpaperUri = uriStr
            chatWallpaperId = "custom"
            val fId = selectedFriend?.id
            if (fId != null) {
                friendPrefs.edit()
                    .putString("chat_wallpaper_custom_$fId", uriStr)
                    .putString("chat_wallpaper_$fId", "custom")
                    .apply()
            }
        }
    }

    // Session cache of unlocked friends during current app session
    val unlockedLockedFriends = remember { mutableStateListOf<String>() }
    var pendingLockedFriendToOpen by remember { mutableStateOf<FriendItem?>(null) }
    var isBiometricAuthPromptActive by remember { mutableStateOf(false) }

    // Synchronize mute, lock, theme, wallpaper when selectedFriend changes
    LaunchedEffect(selectedFriend?.id) {
        val friend = selectedFriend
        if (friend != null) {
            val friendId = friend.id
            val cleanHandle = friend.handle.trim().lowercase().removePrefix("@")
            val mutedSet = notifPrefs.getStringSet("muted_channels", emptySet()) ?: emptySet()
            isChatMuted = mutedSet.any { it.trim().lowercase().removePrefix("#").removePrefix("@") == cleanHandle || it.trim().lowercase() == friendId.lowercase() }
            isChatLocked = friendPrefs.getBoolean("chat_locked_$friendId", false) || friendPrefs.getBoolean("chat_locked_$cleanHandle", false)
            chatThemeId = friendPrefs.getString("chat_theme_$friendId", "default") ?: "default"
            chatWallpaperId = friendPrefs.getString("chat_wallpaper_$friendId", "default") ?: "default"
            customWallpaperUri = friendPrefs.getString("chat_wallpaper_custom_$friendId", null)
        }
    }

    // Active Chat Theme & Wallpaper Resolution
    val activeChatTheme = remember(chatThemeId) {
        AVAILABLE_CHAT_THEMES.firstOrNull { it.id == chatThemeId } ?: AVAILABLE_CHAT_THEMES.first()
    }
    val activeChatWallpaper = remember(chatWallpaperId) {
        AVAILABLE_CHAT_WALLPAPERS.firstOrNull { it.id == chatWallpaperId } ?: AVAILABLE_CHAT_WALLPAPERS.first()
    }

    // Dynamic Accent Colors & Light/Dark Theme Detector
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()
    val isLight = isAppInLightTheme()
    val userInitial = if (currentUsername.isNotBlank()) currentUsername.first().toString().uppercase() else "C"

    // Theme Aware Message Bubble Colors - Dynamically configured per friend theme
    val ownBubbleBg = if (isLight) activeChatTheme.ownBubbleBgLight else activeChatTheme.ownBubbleBgDark
    val ownBubbleText = if (isLight) activeChatTheme.ownBubbleTextLight else activeChatTheme.ownBubbleTextDark
    val friendBubbleBg = if (isLight) activeChatTheme.friendBubbleBgLight else activeChatTheme.friendBubbleBgDark
    val friendBubbleBorder = if (chatThemeId == "default") {
        if (isLight) Color(0xFFE2E8F0) else Color(0xFF27272A)
    } else {
        activeChatTheme.primaryColor.copy(alpha = 0.35f)
    }
    val friendBubbleText = if (isLight) activeChatTheme.friendBubbleTextLight else activeChatTheme.friendBubbleTextDark

    // Friends List State - start empty, populated by IO LaunchedEffect below for 0ms Main-thread blocking
    val friendsList = remember { mutableStateListOf<FriendItem>() }
    var isFriendsLoadedOnce by remember { mutableStateOf(false) }

    // Auto-select chat when opened from push notification intent
    LaunchedEffect(targetChatUserOrChannel, friendsList.size) {
        val target = targetChatUserOrChannel?.trim()
        if (!target.isNullOrBlank()) {
            val clean = target.lowercase().removePrefix("@").removePrefix("dm-").removePrefix("dm_")
            val matchingFriend = friendsList.firstOrNull {
                it.id.equals(clean, ignoreCase = true) ||
                it.handle.removePrefix("@").equals(clean, ignoreCase = true) ||
                it.name.equals(clean, ignoreCase = true) ||
                (target.startsWith("dm-") && target.contains(it.handle.removePrefix("@").lowercase()))
            }
            if (matchingFriend != null) {
                selectedFriend = matchingFriend
            } else {
                val displayName = clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                selectedFriend = FriendItem(
                    id = clean,
                    name = displayName,
                    handle = "@$clean",
                    initial = clean.take(1).uppercase(),
                    status = "Online",
                    bio = "Connecto Member",
                    lastMessage = "",
                    isOnline = true,
                    avatarUrl = null
                )
            }
            onTargetChatHandled?.invoke()
        }
    }

    // Load cached friends from SQLite on IO thread on first composition
    LaunchedEffect(Unit) {
        val cached = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dbHelper.getCachedFriends()
        }
        if (cached.isNotEmpty() && friendsList.isEmpty()) {
            friendsList.addAll(cached)
            isFriendsLoadedOnce = true
        }
    }

    // Safe Audio Permission Launcher for 1:1 and Group Voice Calls
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            VoiceCallManager.restartAudioHardwareIfConnected()
            val target = selectedFriend
            if (target != null) {
                val friendUser = target.handle.removePrefix("@").ifBlank { target.name }
                val friendName = target.name.ifBlank { friendUser }
                VoiceCallManager.start1on1Call(friendUser, friendName)
            }
        } else {
            Toast.makeText(context, "Microphone permission is required for voice calls", Toast.LENGTH_SHORT).show()
        }
    }

    fun initiateVoiceCallWithFriend(friend: FriendItem) {
        val friendUser = friend.handle.removePrefix("@").ifBlank { friend.name }
        val friendName = friend.name.ifBlank { friendUser }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            VoiceCallManager.start1on1Call(friendUser, friendName)
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Function to sync accepted friends and pending friend requests from backend
    suspend fun syncAcceptedFriends() {
        try {
            val effUsername = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
            if (effUsername.isNotBlank()) {
                ConnectoApiClient.currentUsername = effUsername
            }
            val reqRes = ConnectoApiClient.getReceivedFriendRequests(username = effUsername)
            if (reqRes.isSuccess) {
                pendingRequestsCount = reqRes.getOrThrow().size
            }

            val friendsRes = ConnectoApiClient.getFriends(username = effUsername)
            if (friendsRes.isSuccess) {
                val serverFriends = friendsRes.getOrThrow()
                val mapped = serverFriends
                    .distinctBy { it.friendUsername.trim().lowercase().removePrefix("@") }
                    .map { f ->
                    val cleanUsername = f.friendUsername.trim().removePrefix("@")
                    val displayName = f.friendDisplayName.ifBlank { cleanUsername }
                    val init = if (displayName.isNotBlank()) displayName.first().toString().uppercase()
                               else if (cleanUsername.isNotBlank()) cleanUsername.first().toString().uppercase()
                               else "G"
                    val unread = readPrefs.getInt("unread_${f.friendId}", 0)
                    val lastMsg = readPrefs.getString("last_msg_${f.friendId}", "Direct chat active • Tap to message") ?: "Direct chat active • Tap to message"
                    val time = formatIsoTime(readPrefs.getString("last_time_${f.friendId}", "Active") ?: "Active")
                    val isFriendOnline = f.isOnline && !f.isStealth
                    FriendItem(
                        id = f.friendId.ifBlank { f.id },
                        name = displayName,
                        handle = "@$cleanUsername",
                        initial = init,
                        status = if (isFriendOnline) "Accepted Friend • Online" else "Accepted Friend • Offline",
                        bio = f.bio?.ifBlank { "Connecto Member • Friend" } ?: "Connecto Member • Friend",
                        lastMessage = lastMsg,
                        timeAgo = time,
                        unreadCount = unread,
                        isOnline = isFriendOnline,
                        avatarUrl = f.friendAvatarUrl
                    )
                }

                // Cache to persistent local SQLite database for zero-latency launches
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    dbHelper.saveFriends(mapped)
                }
                // Diff-update: never clear the entire list — avoids empty-list flash in LazyColumn
                val newIds = mapped.map { it.id }.toSet()
                friendsList.removeAll { it.id !in newIds }
                for (item in mapped) {
                    val idx = friendsList.indexOfFirst { it.id == item.id }
                    if (idx == -1) friendsList.add(item) else friendsList[idx] = item
                }
                isFriendsLoadedOnce = true

                // Sync real unread counts from server (authoritative — excludes own sent messages)
                try {
                    val unreadRes = ConnectoApiClient.getDmUnreadCounts()
                    if (unreadRes.isSuccess) {
                        val serverUnread = unreadRes.getOrThrow() // Map<String, Int> partner→count
                        for (i in friendsList.indices) {
                            val friendHandle = friendsList[i].handle.removePrefix("@").trim().lowercase()
                        val serverCount = serverUnread[friendHandle] ?: serverUnread.entries
                            .firstOrNull { it.key.trim().removePrefix("@").equals(friendHandle, ignoreCase = true) }?.value ?: 0
                        if (serverCount != friendsList[i].unreadCount) {
                            friendsList[i] = friendsList[i].copy(unreadCount = serverCount)
                            readPrefs.edit().putInt("unread_${friendsList[i].id}", serverCount).apply()
                        }
                        }
                        val hasUnread = friendsList.any { it.unreadCount > 0 }
                        onUnreadStatusChanged(hasUnread)
                    }
                } catch (e: Exception) {
                    // Non-fatal — local SharedPrefs count is fallback
                }
            } else {
                isFriendsLoadedOnce = true
            }
        } catch (e: Exception) {
            isFriendsLoadedOnce = true
            // Ignore transient network errors
        }
    }

    val screenCtx = LocalContext.current

    // Real-time friend requests & accepted friends sync listener
    DisposableEffect(screenCtx) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                coroutineScope.launch {
                    syncAcceptedFriends()
                }
            }
        }
        val filter = IntentFilter("com.connecto.app.FRIEND_REQUEST_UPDATED")
        try {
            ContextCompat.registerReceiver(
                screenCtx,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {}
        onDispose {
            try {
                screenCtx.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    // Dynamic friend requests & accepted friends sync loop from backend
    LaunchedEffect(Unit) {
        while (true) {
            syncAcceptedFriends()
            delay(3500)
        }
    }

    val filteredFriends by remember {
        derivedStateOf {
            if (friendSearchQuery.isBlank()) friendsList.toList()
            else friendsList.filter {
                it.name.contains(friendSearchQuery, ignoreCase = true) || it.handle.contains(friendSearchQuery, ignoreCase = true)
            }
        }
    }

    // Direct Messages Feed
    var activeDmChannelId by remember { mutableStateOf<String?>(null) }
    var friendMessages by remember {
        mutableStateOf<List<CustomMessageItem>>(emptyList())
    }

    // Handle System Back button when in 1:1 direct chat
    BackHandler(enabled = selectedFriend != null) {
        selectedFriend?.let { friend ->
            if (typedMessage.isNotBlank()) {
                messageDrafts[friend.id] = typedMessage
            } else {
                messageDrafts.remove(friend.id)
            }
        }
        typedMessage = ""
        selectedFriend = null
        activeDmChannelId = null
        isLoadingMessages = false
        onDirectChatStateChanged(false)
    }

    // Real-time WebSocket User Presence Listener
    LaunchedEffect(Unit) {
        VoiceCallManager.userPresenceUpdates.collect { (user, isOnline) ->
            val cleanUser = user.lowercase().removePrefix("@")
            val currentSelected = selectedFriend
            if (currentSelected != null) {
                val currentHandle = currentSelected.handle.lowercase().removePrefix("@")
                val currentId = currentSelected.id.lowercase()
                val currentName = currentSelected.name.lowercase()
                if (currentHandle == cleanUser || currentId == cleanUser || currentName == cleanUser) {
                    selectedFriend = currentSelected.copy(
                        isOnline = isOnline,
                        status = if (isOnline) "Accepted Friend • Online" else "Accepted Friend • Offline"
                    )
                }
            }
            val idx = friendsList.indexOfFirst {
                it.handle.lowercase().removePrefix("@") == cleanUser ||
                it.id.lowercase() == cleanUser ||
                it.name.lowercase() == cleanUser
            }
            if (idx != -1) {
                friendsList[idx] = friendsList[idx].copy(
                    isOnline = isOnline,
                    status = if (isOnline) "Accepted Friend • Online" else "Accepted Friend • Offline"
                )
            }
        }
    }

    // Real-time WebSocket Incoming DM Bus Listener
    LaunchedEffect(Unit) {
        VoiceCallManager.incomingChannelMessages.collect { dto ->
            val myUsername = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@").lowercase()
            val myDisplayName = (ConnectoApiClient.currentUserDisplayName ?: "").trim().lowercase()
            val myUserId = ConnectoApiClient.currentUserId?.trim()

            val authorClean = dto.authorId.trim().removePrefix("@").lowercase()
            val authorNameClean = dto.authorName.trim().removePrefix("@").lowercase()

            val isMe = (
                authorClean == myUsername ||
                authorNameClean == myUsername ||
                (myDisplayName.isNotBlank() && (authorNameClean == myDisplayName || authorClean == myDisplayName)) ||
                (myUserId != null && (dto.authorId == myUserId || dto.authorName == myUserId))
            )

            val currentFriend = selectedFriend
            val currentDmId = activeDmChannelId
            val currentCanonical = if (currentFriend != null) {
                val clean = currentFriend.handle.removePrefix("@").lowercase()
                val me = myUsername
                "dm-${minOf(me, clean)}-${maxOf(me, clean)}"
            } else null

            val isForCurrentChat = currentFriend != null && (
                (currentDmId != null && dto.channelId.equals(currentDmId, ignoreCase = true)) ||
                (currentCanonical != null && dto.channelId.equals(currentCanonical, ignoreCase = true)) ||
                authorClean == currentFriend.handle.removePrefix("@").trim().lowercase() ||
                (dto.channelId != null && dto.channelId.contains(currentFriend.handle.removePrefix("@"), ignoreCase = true)) ||
                (isMe && (dto.channelId.equals(currentDmId, ignoreCase = true) || dto.channelId.equals(currentCanonical, ignoreCase = true) || (dto.channelId != null && dto.channelId.contains(currentFriend.handle.removePrefix("@"), ignoreCase = true))))
            )

            if (isForCurrentChat) {
                val init = if (dto.authorName.isNotBlank()) dto.authorName.first().toString().uppercase() else "U"
                val msgAvatar = if (isMe) {
                    ConnectoApiClient.currentUserAvatarUrl
                } else {
                    dto.authorAvatar ?: currentFriend?.avatarUrl
                }
                val newItem = CustomMessageItem(
                    id = dto.id,
                    senderName = dto.authorName,
                    initial = init,
                    content = dto.content,
                    time = formatIsoTime(dto.createdAt),
                    isMe = isMe,
                    isDelivered = true,
                    isRead = true,
                    avatarUrl = msgAvatar
                )
                // Reconcile and deduplicate:
                val existingIdx = friendMessages.indexOfFirst {
                    it.id == newItem.id || (
                        isMe && it.isMe &&
                        (it.id.startsWith("opt-") || it.id.length > 30 || it.time == "Just now") &&
                        it.content.trim() == newItem.content.trim()
                    )
                }

                if (existingIdx != -1) {
                    val updated = friendMessages.toMutableList()
                    val existingItem = updated[existingIdx]
                    updated[existingIdx] = newItem.copy(reaction = existingItem.reaction ?: newItem.reaction)
                    friendMessages = updated
                } else if (friendMessages.none { it.id == newItem.id }) {
                    friendMessages = friendMessages + newItem
                }

                // Save to local SQLite cache instantly
                if (currentCanonical != null) {
                    val incomingList = listOf(dto)
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            dbHelper.saveMessages(currentCanonical, incomingList)
                            if (currentDmId != null && currentDmId != currentCanonical) {
                                dbHelper.saveMessages(currentDmId, incomingList)
                            }
                        } catch (_: Exception) {}
                    }
                }

                // Update friends list entry — current chat always has 0 unread
                val fIdx = friendsList.indexOfFirst {
                    it.id == currentFriend!!.id || it.handle.removePrefix("@").equals(currentFriend!!.handle.removePrefix("@"), ignoreCase = true)
                }
                if (fIdx != -1) {
                    friendsList[fIdx] = friendsList[fIdx].copy(
                        lastMessage = dto.content,
                        timeAgo = formatIsoTime(dto.createdAt),
                        unreadCount = 0
                    )
                }
            } else if (!isMe) {
                // Incoming message for another friend — ONLY increment if NOT sent by myself
                val fIdx = friendsList.indexOfFirst {
                    val h = it.handle.removePrefix("@").trim().lowercase()
                    h == authorClean || it.name.trim().lowercase() == authorNameClean || h == authorNameClean
                }
                if (fIdx != -1) {
                    val targetFriend = friendsList[fIdx]
                    val newUnread = targetFriend.unreadCount + 1
                    friendsList[fIdx] = targetFriend.copy(
                        lastMessage = dto.content,
                        timeAgo = formatIsoTime(dto.createdAt),
                        unreadCount = newUnread
                    )
                    readPrefs.edit()
                        .putString("last_msg_${targetFriend.id}", dto.content)
                        .putString("last_time_${targetFriend.id}", formatIsoTime(dto.createdAt))
                        .putInt("unread_${targetFriend.id}", newUnread)
                        .apply()
                    onUnreadStatusChanged(true)
                }
            }
        }
    }

    // DM Channel Setup and Live Polling Loop
    LaunchedEffect(selectedFriend) {
        val friend = selectedFriend
        onDirectChatStateChanged(friend != null)

        if (friend != null) {
            typedMessage = messageDrafts[friend.id] ?: ""
            activeDmChannelId = null

            val friendId = friend.id
            val idx = friendsList.indexOfFirst { it.id == friendId }
            if (idx != -1) {
                friendsList[idx] = friendsList[idx].copy(unreadCount = 0)
            }
            readPrefs.edit()
                .putInt("unread_$friendId", 0)
                .putBoolean("read_$friendId", true)
                .apply()
            val hasRemainingUnread = friendsList.any { it.unreadCount > 0 }
            onUnreadStatusChanged(hasRemainingUnread)

            val cleanTarget = friend.handle.removePrefix("@")
            val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
            val canonicalName = "dm-${minOf(myUser, cleanTarget.lowercase())}-${maxOf(myUser, cleanTarget.lowercase())}"

            // 1. Instant SQLite Cache Load (0ms UI latency across all channel aliases)
            val cachedMessages = withContext(Dispatchers.IO) {
                dbHelper.getDirectMessages(
                    myUsername = myUser,
                    friendUsername = cleanTarget,
                    channelId = activeDmChannelId ?: ConnectoApiClient.channelCache[canonicalName.lowercase()],
                    limit = 50
                )
            }

            if (cachedMessages.isNotEmpty()) {
                val myUsername = ConnectoApiClient.currentUsername ?: currentUsername
                val myDisplayName = ConnectoApiClient.currentUserDisplayName ?: ""
                val myUserId = ConnectoApiClient.currentUserId
                friendMessages = cachedMessages.map { dto ->
                    val isMe = (
                        dto.authorId.equals(myUsername, ignoreCase = true) ||
                        dto.authorName.equals(myUsername, ignoreCase = true) ||
                        (myDisplayName.isNotBlank() && dto.authorName.equals(myDisplayName, ignoreCase = true)) ||
                        (myUserId != null && (dto.authorId == myUserId || dto.authorName == myUserId)) ||
                        dto.authorId.equals(currentUsername, ignoreCase = true) ||
                        dto.authorName.equals(currentUsername, ignoreCase = true)
                    )
                    val init = if (dto.authorName.isNotBlank()) dto.authorName.first().toString().uppercase() else "U"
                    val msgAvatar = if (isMe) {
                        ConnectoApiClient.currentUserAvatarUrl
                    } else {
                        dto.authorAvatar ?: friend.avatarUrl
                    }
                    CustomMessageItem(
                        id = dto.id,
                        senderName = dto.authorName,
                        initial = init,
                        content = dto.content,
                        time = formatIsoTime(dto.createdAt),
                        isMe = isMe,
                        isDelivered = true,
                        isRead = true,
                        avatarUrl = msgAvatar
                    )
                }
                isLoadingMessages = false
            } else {
                friendMessages = emptyList()
                isLoadingMessages = true
            }

            // Immediately subscribe to canonical DM channel over WebSocket
            VoiceCallManager.subscribeChannel(canonicalName)

            // Resolve DM channel on backend and subscribe in background
            coroutineScope.launch {
                try {
                    val startRes = ConnectoApiClient.startDM(cleanTarget)
                    if (startRes.isSuccess) {
                        val ch = startRes.getOrThrow()
                        activeDmChannelId = ch.id
                        ConnectoApiClient.channelCache[canonicalName.lowercase()] = ch.id
                        ConnectoApiClient.channelCache[ch.id.lowercase()] = canonicalName.lowercase()
                        VoiceCallManager.subscribeChannel(ch.id)
                        VoiceCallManager.subscribeChannel(ch.name)
                    }
                } catch (_: Exception) {}
            }

            // Mark this DM as read on backend asynchronously
            coroutineScope.launch {
                try {
                    ConnectoApiClient.markDmRead(cleanTarget)
                } catch (_: Exception) {}
            }

            // Re-fetch fresh profile data on view — avoids indefinite caching of avatar or display name
            coroutineScope.launch {
                try {
                    val profRes = ConnectoApiClient.getProfile(cleanTarget)
                    if (profRes.isSuccess) {
                        val prof = profRes.getOrThrow()
                        val freshAvatar = prof.avatarUrl
                        val freshName = prof.displayName.ifBlank { prof.username }
                        val freshBio = prof.bio ?: ""
                        val freshOnline = prof.isOnline
                        selectedFriend = selectedFriend?.copy(
                            avatarUrl = freshAvatar ?: selectedFriend?.avatarUrl,
                            name = if (freshName.isNotBlank()) freshName else (selectedFriend?.name ?: cleanTarget),
                            bio = freshBio,
                            isOnline = freshOnline,
                            status = if (freshOnline) "Accepted Friend • Online" else "Accepted Friend • Offline"
                        )
                        val fIdx = friendsList.indexOfFirst { it.id == friendId }
                        if (fIdx != -1) {
                            friendsList[fIdx] = friendsList[fIdx].copy(
                                avatarUrl = freshAvatar ?: friendsList[fIdx].avatarUrl,
                                name = if (freshName.isNotBlank()) freshName else friendsList[fIdx].name,
                                bio = freshBio,
                                isOnline = freshOnline,
                                status = if (freshOnline) "Accepted Friend • Online" else "Accepted Friend • Offline"
                            )
                        }
                    }
                } catch (_: Exception) {}
            }

            // Continuous live polling every 1.5s
            while (true) {
                try {
                    val dmId = activeDmChannelId ?: ConnectoApiClient.channelCache[canonicalName.lowercase()] ?: canonicalName

                    if (dmId.isNotBlank()) {
                        val msgRes = ConnectoApiClient.getMessages(channelId = dmId, friendUsername = cleanTarget)
                        if (msgRes.isSuccess) {
                            val dtoList = msgRes.getOrThrow()
                            val unexpiredList = dtoList.distinctBy { it.id }
                            isLoadingMessages = false
                            if (unexpiredList.isNotEmpty()) {
                                // Learn channel UUID if returned in message DTOs
                                val serverChId = unexpiredList.firstOrNull {
                                    !it.channelId.isNullOrBlank() && !it.channelId.startsWith("dm-") && !it.channelId.startsWith("dm_")
                                }?.channelId
                                if (serverChId != null) {
                                    if (activeDmChannelId == null) activeDmChannelId = serverChId
                                    ConnectoApiClient.channelCache[canonicalName.lowercase()] = serverChId
                                    ConnectoApiClient.channelCache[serverChId.lowercase()] = canonicalName.lowercase()
                                }

                                withContext(Dispatchers.IO) {
                                    dbHelper.saveMessages(canonicalName, unexpiredList)
                                    if (dmId != canonicalName) {
                                        dbHelper.saveMessages(dmId, unexpiredList)
                                    }
                                    if (serverChId != null && serverChId != dmId && serverChId != canonicalName) {
                                        dbHelper.saveMessages(serverChId, unexpiredList)
                                    }
                                }
                                val myUsername = ConnectoApiClient.currentUsername ?: currentUsername
                                val myDisplayName = ConnectoApiClient.currentUserDisplayName ?: ""
                                val myUserId = ConnectoApiClient.currentUserId
                                val existingReactions = friendMessages.associate { it.id to it.reaction }
                                val mapped = unexpiredList.map { dto ->
                                    val isMe = (
                                        dto.authorId.equals(myUsername, ignoreCase = true) ||
                                        dto.authorName.equals(myUsername, ignoreCase = true) ||
                                        (myDisplayName.isNotBlank() && dto.authorName.equals(myDisplayName, ignoreCase = true)) ||
                                        (myUserId != null && (dto.authorId == myUserId || dto.authorName == myUserId)) ||
                                        dto.authorId.equals(currentUsername, ignoreCase = true) ||
                                        dto.authorName.equals(currentUsername, ignoreCase = true)
                                    )
                                    val init = if (dto.authorName.isNotBlank()) dto.authorName.first().toString().uppercase() else "U"
                                    val msgAvatar = if (isMe) {
                                        ConnectoApiClient.currentUserAvatarUrl
                                    } else {
                                        dto.authorAvatar ?: selectedFriend?.avatarUrl
                                    }
                                    val otherUser = friend.handle.removePrefix("@")
                                    val conversationKey = ConnectoE2EEncryption.deriveConversationKey(myUsername, otherUser)
                                    val isEnc = ConnectoE2EEncryption.isEncrypted(dto.content)
                                    val decryptedContent = if (isEnc) ConnectoE2EEncryption.decryptMessage(dto.content, conversationKey) else dto.content
                                    val decryptedReplyContent = if (dto.replyToContent != null && ConnectoE2EEncryption.isEncrypted(dto.replyToContent)) {
                                        ConnectoE2EEncryption.decryptMessage(dto.replyToContent, conversationKey)
                                    } else dto.replyToContent

                                    CustomMessageItem(
                                        id = dto.id,
                                        senderName = dto.authorName,
                                        initial = init,
                                        content = decryptedContent,
                                        time = formatIsoTime(dto.createdAt),
                                        isMe = isMe,
                                        isDelivered = true,
                                        isRead = true,
                                        reaction = existingReactions[dto.id],
                                        avatarUrl = msgAvatar,
                                        replyToId = dto.replyToId,
                                        replyToContent = decryptedReplyContent,
                                        replyToAuthor = dto.replyToAuthor,
                                        isEncrypted = isEnc
                                    )
                                }

                                // Merge with any in-flight optimistic message that has not yet reached the server
                                val inFlight = friendMessages.filter {
                                    it.isMe && (it.id.startsWith("opt-") || it.time == "Just now") &&
                                    mapped.none { m -> m.content.trim() == it.content.trim() }
                                }
                                val merged = mapped + inFlight
                                if (friendMessages != merged) {
                                    friendMessages = merged
                                }

                                // Update last message in friends list and persist
                                val last = merged.last()
                                val fIdx = friendsList.indexOfFirst { it.id == friend.id || it.handle.removePrefix("@").equals(friend.handle.removePrefix("@"), ignoreCase = true) }
                                if (fIdx != -1) {
                                    friendsList[fIdx] = friendsList[fIdx].copy(
                                        lastMessage = last.content,
                                        timeAgo = last.time,
                                        unreadCount = 0
                                    )
                                    readPrefs.edit()
                                        .putString("last_msg_${friend.id}", last.content)
                                        .putString("last_time_${friend.id}", last.time)
                                        .putInt("unread_${friend.id}", 0)
                                        .apply()
                                }
                            } else {
                                // Server returned empty: NEVER wipe existing local cached or user messages.
                                isLoadingMessages = false
                            }
                        } else {
                            isLoadingMessages = false
                        }
                    }
                } catch (e: Exception) {
                    // Ignore transient network errors and ensure loading shimmer is dismissed
                    isLoadingMessages = false
                }
                delay(1500)
            }
        } else {
            friendMessages = emptyList()
            activeDmChannelId = null
            isLoadingMessages = false
        }
    }

    var hasInitiallyScrolledForFriend by remember(selectedFriend?.id) { mutableStateOf(false) }

    // Auto-scroll on new messages or input focus (instantly on initial open, animated on new messages)
    LaunchedEffect(friendMessages.size, isInputFocused) {
        if (selectedFriend != null && friendMessages.isNotEmpty()) {
            // +1 accounts for the "encryption_chip" header item at index 0
            val targetIdx = friendMessages.size  // messages occupy indices 1..friendMessages.size
            if (!hasInitiallyScrolledForFriend) {
                try { listState.scrollToItem(targetIdx) } catch (_: Exception) {}
                hasInitiallyScrolledForFriend = true
            } else {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val isNearBottom = lastVisible >= (targetIdx - 2).coerceAtLeast(0)
                if (isNearBottom || isInputFocused) {
                    try { listState.animateScrollToItem(targetIdx) } catch (_: Exception) {}
                }
            }
        }
    }

    LaunchedEffect(showFriendProfileDialog) {
        if (showFriendProfileDialog && selectedFriend != null) {
            val clean = selectedFriend!!.handle.removePrefix("@")
            try {
                val res = ConnectoApiClient.getProfile(clean)
                if (res.isSuccess) {
                    val prof = res.getOrThrow()
                    selectedFriendProfileData = prof
                    selectedFriend = selectedFriend?.copy(
                        avatarUrl = prof.avatarUrl ?: selectedFriend?.avatarUrl,
                        name = prof.displayName.ifBlank { prof.fullName.ifBlank { prof.username } },
                        bio = prof.bio.ifBlank { selectedFriend?.bio ?: "" },
                        isOnline = prof.isOnline
                    )
                }
            } catch (e: Exception) {
                // Non-fatal
            }
        }
    }

    // Friend Profile Info Dialog - Enhanced with Tabs matching Profile tab
    if (showFriendProfileDialog && selectedFriend != null) {
        val friendObj = selectedFriend!!
        val profData = selectedFriendProfileData
        val friendHandleClean = friendObj.handle.removePrefix("@")
        val effectiveFullName = profData?.fullName?.ifBlank { friendObj.name } ?: friendObj.name
        val effectiveDisplayName = profData?.displayName?.ifBlank { friendObj.name } ?: friendObj.name
        val effectiveBio = profData?.bio?.ifBlank { friendObj.bio } ?: friendObj.bio
        val effectiveEmail = profData?.email?.ifBlank { null }
        val effectivePhone = profData?.phoneNumber?.ifBlank { null }
        val isTwoFactorActive = profData?.twoFactorEnabled ?: false
        val twoFactorMethod = profData?.twoFactorMethod ?: "email"

        Dialog(
            onDismissRequest = { showFriendProfileDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                securePolicy = SecureFlagPolicy.Inherit
            )
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .heightIn(max = 700.dp)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // STABLE / FIXED HEADER (Profile pic, name, badges, tab buttons)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                    // Header close button bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FRIEND PROFILE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                .clickable { showFriendProfileDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Avatar Circle with Dynamic Accent Glow & Full-Screen Photo Preview
                    val friendAvatarResolved = friendObj.avatarUrl ?: profData?.avatarUrl
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable { showPhotoLightbox = true }
                    ) {
                        ConnectoAvatar(
                            name = friendObj.name,
                            avatarUrl = friendAvatarResolved,
                            customSizeDp = 88.dp,
                            customFontSizeSp = 34,
                            borderWidth = 3.dp,
                            borderBrush = Brush.sweepGradient(gradientColors),
                            onClick = { showPhotoLightbox = true }
                        )
                        // Zoom inspect pill badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "View Profile Picture Fullscreen",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Display Name & Verified Badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = effectiveDisplayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Member",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Username handle
                        Text(
                            text = "@$friendHandleClean",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Status Badges Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Online / Offline Status Pill
                        val statusColor = if (friendObj.isOnline) OnlineGreen else TextDisabledColor
                        val statusText = if (friendObj.isOnline) "Online" else "Offline"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
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
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }

                        // E2E Secured Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
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
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Synced Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(OnlineGreen.copy(alpha = 0.12f))
                                .border(1.dp, OnlineGreen.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = OnlineGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Synced",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnlineGreen
                                )
                            }
                        }
                    }

                    // Interactive Tab Selector Buttons (Personal, Chat & Lock, Theme & Wallpaper, Actions)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            Triple("settings", "Chat & Lock", Icons.Default.Lock),
                            Triple("customization", "Theme & Wall", Icons.Default.Palette),
                            Triple("actions", "Actions", Icons.Default.Call)
                        ).forEach { (tabKey, tabTitle, tabIcon) ->
                            val isSelected = friendProfileSelectedTab == tabKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) Brush.linearGradient(gradientColors)
                                        else SolidColor(Color.Transparent)
                                    )
                                    .clickable { friendProfileSelectedTab = tabKey }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = tabIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) contentOnGradient else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = tabTitle,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) contentOnGradient else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    } // end Fixed Header

                    // SCROLLABLE TAB CONTENT BODY
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    when (friendProfileSelectedTab) {
                        "settings" -> {
                            // Chat Privacy, Notifications & Security Controls
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 1. MUTE NOTIFICATIONS TOGGLE
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
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
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isChatMuted) Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isChatMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                                                    contentDescription = "Mute Notifications",
                                                    tint = if (isChatMuted) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Mute Notifications",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (isChatMuted) "Notifications silenced for this friend" else "Alerts & incoming vibrations active",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isChatMuted,
                                            onCheckedChange = { checked ->
                                                isChatMuted = checked
                                                val currentMuted = notifPrefs.getStringSet("muted_channels", emptySet())?.toMutableSet() ?: mutableSetOf()
                                                val clean = friendObj.handle.trim().lowercase().removePrefix("@")
                                                val fId = friendObj.id.lowercase()
                                                if (checked) {
                                                    currentMuted.add(clean)
                                                    currentMuted.add(fId)
                                                    Toast.makeText(context, "Muted notifications from @$clean", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    currentMuted.removeAll { it.lowercase() == clean || it.lowercase() == fId }
                                                    Toast.makeText(context, "Unmuted notifications from @$clean", Toast.LENGTH_SHORT).show()
                                                }
                                                notifPrefs.edit().putStringSet("muted_channels", currentMuted).apply()
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                    }
                                }

                                // 2. LOCK THE CHAT TOGGLE (Biometric / PIN)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
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
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isChatLocked) Color(0xFFEF4444).copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isChatLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                                    contentDescription = "Lock Chat",
                                                    tint = if (isChatLocked) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Lock Chat Session",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (isChatLocked) "Requires fingerprint/PIN to open chat" else "Unlocked • Tap to enable biometric lock",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isChatLocked,
                                            onCheckedChange = { checked ->
                                                val activity = context.findFragmentActivity()
                                                val fId = friendObj.id
                                                val clean = friendObj.handle.trim().lowercase().removePrefix("@")
                                                if (checked) {
                                                    // Verify biometric/credentials before enabling lock
                                                    if (activity != null && (BiometricAuthManager.isBiometricEnrolled(activity) || BiometricAuthManager.isDeviceSecure(activity))) {
                                                        BiometricAuthManager.authenticate(
                                                            activity = activity,
                                                            title = "Lock Chat with @$clean",
                                                            subtitle = "Verify biometric or device credential to enable chat lock",
                                                            onSuccess = {
                                                                isChatLocked = true
                                                                friendPrefs.edit()
                                                                    .putBoolean("chat_locked_$fId", true)
                                                                    .putBoolean("chat_locked_$clean", true)
                                                                    .apply()
                                                                if (!unlockedLockedFriends.contains(fId)) {
                                                                    unlockedLockedFriends.add(fId)
                                                                }
                                                                Toast.makeText(context, "Chat locked for @$clean", Toast.LENGTH_SHORT).show()
                                                            },
                                                            onError = { _, err ->
                                                                Toast.makeText(context, "Authentication required: $err", Toast.LENGTH_SHORT).show()
                                                            }
                                                        )
                                                    } else {
                                                        // Fallback direct set
                                                        isChatLocked = true
                                                        friendPrefs.edit()
                                                            .putBoolean("chat_locked_$fId", true)
                                                            .putBoolean("chat_locked_$clean", true)
                                                            .apply()
                                                        Toast.makeText(context, "Chat locked for @$clean", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    // Verify biometric/credential before removing lock
                                                    if (activity != null && (BiometricAuthManager.isBiometricEnrolled(activity) || BiometricAuthManager.isDeviceSecure(activity))) {
                                                        BiometricAuthManager.authenticate(
                                                            activity = activity,
                                                            title = "Unlock Chat with @$clean",
                                                            subtitle = "Verify biometric or device credential to disable chat lock",
                                                            onSuccess = {
                                                                isChatLocked = false
                                                                friendPrefs.edit()
                                                                    .remove("chat_locked_$fId")
                                                                    .remove("chat_locked_$clean")
                                                                    .apply()
                                                                unlockedLockedFriends.remove(fId)
                                                                Toast.makeText(context, "Chat lock disabled for @$clean", Toast.LENGTH_SHORT).show()
                                                            },
                                                            onError = { _, err ->
                                                                Toast.makeText(context, "Authentication required: $err", Toast.LENGTH_SHORT).show()
                                                            }
                                                        )
                                                    } else {
                                                        isChatLocked = false
                                                        friendPrefs.edit()
                                                            .remove("chat_locked_$fId")
                                                            .remove("chat_locked_$clean")
                                                            .apply()
                                                        unlockedLockedFriends.remove(fId)
                                                        Toast.makeText(context, "Chat lock disabled for @$clean", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFFEF4444),
                                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                    }
                                }

                                // Direct Channel Cryptography Info
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Direct Channel Security",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Messages and voice calls are protected with client-side AES-256-GCM cryptographic keys.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "customization" -> {
                            // Chat Theme and Chat Background Wallpaper Customization
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Section Header: Chat Themes
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CHAT THEME COLOR",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                }

                                // Themes Horizontal Scroll List
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AVAILABLE_CHAT_THEMES.forEach { theme ->
                                        val isSelected = chatThemeId == theme.id
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                                )
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    chatThemeId = theme.id
                                                    val fId = friendObj.id
                                                    friendPrefs.edit().putString("chat_theme_$fId", theme.id).apply()
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    // Color Swatch Circles
                                                    Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(CircleShape)
                                                                .background(theme.primaryColor)
                                                                .border(1.dp, Color.White, CircleShape)
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(CircleShape)
                                                                .background(theme.secondaryColor)
                                                                .border(1.dp, Color.White, CircleShape)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = theme.name,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = theme.subtitle,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(22.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Active Theme",
                                                            tint = contentOnGradient,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Section Header: Chat Wallpaper
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wallpaper,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CHAT BACKGROUND WALLPAPER",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                }

                                    // CUSTOM PHOTO WALLPAPER CARD (Pick from Gallery)
                                    val isCustomSelected = chatWallpaperId == "custom" && !customWallpaperUri.isNullOrBlank()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isCustomSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                            )
                                            .border(
                                                width = if (isCustomSelected) 2.dp else 1.dp,
                                                color = if (isCustomSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                if (!customWallpaperUri.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = customWallpaperUri,
                                                        contentDescription = "Custom Wallpaper",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(width = 46.dp, height = 34.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 46.dp, height = 34.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.AddPhotoAlternate,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "Custom Photo Wallpaper",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = if (!customWallpaperUri.isNullOrBlank()) "Photo active for this chat" else "Pick custom photo from gallery",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.primary)
                                                        .clickable {
                                                            wallpaperPickerLauncher.launch(
                                                                androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                            )
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = if (customWallpaperUri.isNullOrBlank()) "Choose" else "Change",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = contentOnGradient
                                                    )
                                                }

                                                if (!customWallpaperUri.isNullOrBlank()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(30.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFDC2626).copy(alpha = 0.15f))
                                                            .clickable {
                                                                customWallpaperUri = null
                                                                chatWallpaperId = "default"
                                                                val fId = friendObj.id
                                                                friendPrefs.edit()
                                                                    .remove("chat_wallpaper_custom_" + fId)
                                                                    .putString("chat_wallpaper_" + fId, "default")
                                                                    .apply()
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Remove Custom Wallpaper",
                                                            tint = Color(0xFFEF4444),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                // Wallpaper Preview Grid / Cards
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AVAILABLE_CHAT_WALLPAPERS.forEach { wallpaper ->
                                        val isSelected = chatWallpaperId == wallpaper.id
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                                )
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    chatWallpaperId = wallpaper.id
                                                    val fId = friendObj.id
                                                    friendPrefs.edit().putString("chat_wallpaper_$fId", wallpaper.id).apply()
                                                }
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    // Wallpaper Preview Thumbnail
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 46.dp, height = 34.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isLight) wallpaper.lightBrush else wallpaper.darkBrush)
                                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = wallpaper.name,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = wallpaper.subtitle,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(22.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Active Wallpaper",
                                                            tint = contentOnGradient,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "actions" -> {
                            // Quick Action Buttons
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // START VOICE CALL BUTTON
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .pressScaleEffect(
                                            onClick = {
                                                val friend = selectedFriend
                                                showFriendProfileDialog = false
                                                if (friend != null) {
                                                    initiateVoiceCallWithFriend(friend)
                                                }
                                            },
                                            targetScale = 0.96f
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.linearGradient(gradientColors)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = null,
                                            tint = contentOnGradient,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "START ENCRYPTED VOICE CALL",
                                            color = contentOnGradient,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                // UNFRIEND BUTTON
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .pressScaleEffect(
                                            onClick = {
                                                showUnfriendConfirmDialog = true
                                            },
                                            targetScale = 0.96f
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFDC2626).copy(alpha = 0.12f))
                                        .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PersonRemove,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "REMOVE FROM FRIENDS",
                                            color = Color(0xFFEF4444),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    } // end Scrollable Tab Body

                    // PERSISTENT FOOTER (Always fixed at bottom of dialog)
                    if (friendProfileSelectedTab != "actions") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // START VOICE CALL BUTTON
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .pressScaleEffect(
                                        onClick = {
                                            val friend = selectedFriend
                                            showFriendProfileDialog = false
                                            if (friend != null) {
                                                initiateVoiceCallWithFriend(friend)
                                            }
                                        },
                                        targetScale = 0.96f
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(gradientColors)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Call, contentDescription = null, tint = contentOnGradient, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("VOICE CALL", color = contentOnGradient, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // UNFRIEND BUTTON
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .pressScaleEffect(
                                        onClick = {
                                            showUnfriendConfirmDialog = true
                                        },
                                        targetScale = 0.96f
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFDC2626).copy(alpha = 0.12f))
                                    .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("UNFRIEND", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Friend Profile Photo Fullscreen Lightbox Dialog
    if (showPhotoLightbox && selectedFriend != null) {
        val friendObj = selectedFriend!!
        val resolvedAvatar = friendObj.avatarUrl ?: selectedFriendProfileData?.avatarUrl
        PhotoLightboxDialog(
            title = friendObj.name,
            imageUrl = resolvedAvatar,
            placeholderInitial = friendObj.initial,
            isBanner = false,
            onDismiss = { showPhotoLightbox = false }
        )
    }

    // Unfriend Confirmation Modal Dialog
    if (showUnfriendConfirmDialog && selectedFriend != null) {
        val friendToRemove = selectedFriend!!
        val friendUser = friendToRemove.handle.removePrefix("@").ifBlank { friendToRemove.name }
        val friendName = friendToRemove.name.ifBlank { friendUser }

        AlertDialog(
            onDismissRequest = { if (!isUnfriending) showUnfriendConfirmDialog = false },
            title = {
                Text(
                    text = "Unfriend $friendName?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove @$friendUser from your friends? Direct private messaging and voice calls will be disconnected.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            isUnfriending = true
                            try {
                                val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                                ConnectoApiClient.unfriendUser(targetUsername = friendUser, token = null)
                                dbHelper.deleteFriend(friendToRemove.id)
                                dbHelper.deleteFriend(friendUser)
                                friendsList.removeAll {
                                    it.id == friendToRemove.id ||
                                    it.handle.removePrefix("@").equals(friendUser, ignoreCase = true) ||
                                    it.name.equals(friendName, ignoreCase = true)
                                }
                                showUnfriendConfirmDialog = false
                                showFriendProfileDialog = false
                                selectedFriend = null
                                Toast.makeText(context, "Removed @$friendUser from friends", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isUnfriending = false
                            }
                        }
                    },
                    enabled = !isUnfriending
                ) {
                    if (isUnfriending) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFFEF4444))
                    } else {
                        Text("UNFRIEND", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnfriendConfirmDialog = false },
                    enabled = !isUnfriending
                ) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Add Friends & Friend Requests Search Dialog
    if (showAddFriendDialog) {
        val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
        AddFriendDialog(
            currentUsername = effUser,
            onDismissRequest = {
                showAddFriendDialog = false
                coroutineScope.launch { syncAcceptedFriends() }
            },
            onFriendAdded = {
                coroutineScope.launch { syncAcceptedFriends() }
            },
            onOpenChatWithFriend = { searchUser ->
                showAddFriendDialog = false
                val cleanHandle = searchUser.username.trim().removePrefix("@")
                val existing = friendsList.firstOrNull {
                    it.handle.equals("@$cleanHandle", ignoreCase = true) || it.id == searchUser.id
                }
                val friendItem = existing ?: FriendItem(
                    id = searchUser.id,
                    name = searchUser.displayName.ifBlank { cleanHandle },
                    handle = "@$cleanHandle",
                    initial = (searchUser.displayName.ifBlank { cleanHandle }).firstOrNull()?.toString()?.uppercase() ?: "F",
                    status = "Direct Chat Ready",
                    bio = "Connecto Member",
                    lastMessage = "Direct chat active • Tap to message",
                    timeAgo = "Just now",
                    unreadCount = 0,
                    isOnline = false
                )
                friendMessages = emptyList()
                activeDmChannelId = null
                isLoadingMessages = true
                typedMessage = messageDrafts[friendItem.id] ?: ""
                selectedFriend = friendItem
                onDirectChatStateChanged(true)
            }
        )
    }

    if (showCreateGroupChatDialog) {
        val effUser = if (currentUsername.isNotBlank()) currentUsername else (com.example.connecto.network.ConnectoApiClient.currentUsername ?: "")
        CreateGroupChatDialog(
            currentUsername = effUser,
            onDismissRequest = { showCreateGroupChatDialog = false },
            onGroupCreated = { _, _, _ -> showCreateGroupChatDialog = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========== TOP HEADER ==========
            if (selectedFriend != null) {
                // 1:1 Direct Chat Header with Call Buttons & Friend Profile Info Trigger
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        .statusBarsPadding()
                        .heightIn(min = 68.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
                                    selectedFriend?.let { friend ->
                                        if (typedMessage.isNotBlank()) {
                                            messageDrafts[friend.id] = typedMessage
                                        } else {
                                            messageDrafts.remove(friend.id)
                                        }
                                    }
                                    typedMessage = ""
                                    selectedFriend = null
                                    activeDmChannelId = null
                                    isLoadingMessages = false
                                    onDirectChatStateChanged(false)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Friends List",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Friend Name & Avatar Row (Triggers Friend Profile Dialog)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showFriendProfileDialog = true }
                        ) {
                            ConnectoAvatar(
                                name = selectedFriend?.name ?: "Friend",
                                avatarUrl = selectedFriend?.avatarUrl,
                                customSizeDp = 42.dp,
                                customFontSizeSp = 16,
                                borderWidth = 1.5.dp,
                                borderColor = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = selectedFriend!!.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val isSelectedOnline = selectedFriend?.isOnline == true
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelectedOnline) OnlineGreen else Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isFriendTyping) "typing..." else if (isSelectedOnline) "Online • Direct Chat" else "Offline • Direct Chat",
                                        color = if (isFriendTyping) MaterialTheme.colorScheme.primary else if (isSelectedOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = if (isFriendTyping) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Right Action Controls: Friend Info, Voice & Video Call Buttons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                                    .clickable { showFriendProfileDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Friend Profile Info",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    .clickable {
                                        val friend = selectedFriend
                                        if (friend != null) {
                                            initiateVoiceCallWithFriend(friend)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Voice Call",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                ConnectoTopHeader(
                    title = "DIRECT MESSAGES",
                    subtitle = "Chat with friends & members",
                    userInitial = userInitial,
                    onAddFriendClick = { showAddFriendDialog = true },
                    pendingFriendRequestsCount = pendingRequestsCount,
                    onAvatarClick = onOpenProfile,
                    customAction = {
                        androidx.compose.material3.IconButton(
                            onClick = { showCreateGroupChatDialog = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = "Create Group Chat",
                                tint = if (isAppInLightTheme()) androidx.compose.ui.graphics.Color(0xFF141312) else androidx.compose.ui.graphics.Color(0xFFD4D4D8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }

            // ========== ACTIVE CALL OVERLAY ==========
            val isCallVisible = (callState != CallState.IDLE)
            AnimatedVisibility(visible = isCallVisible) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    WebRtcCallOverlay(
                        callTitle = if (activeCallTitle.isNotBlank()) activeCallTitle else (selectedFriend?.name?.let { "Call with $it" } ?: "Voice Call"),
                        participantCount = 2,
                        allowVideo = false,
                        onEndCall = { VoiceCallManager.endCall() }
                    )
                }
            }

            AnimatedContent(
                targetState = selectedFriend != null,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(tween(280)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut(tween(280)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), initialOffsetX = { fullWidth -> -fullWidth }) + fadeIn(tween(280)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), targetOffsetX = { fullWidth -> fullWidth }) + fadeOut(tween(280)))
                    }
                },
                label = "friendChatViewTransition",
                modifier = Modifier.weight(1f)
            ) { isFriendSelected ->
                if (!isFriendSelected) {
                    // ================= 1. FRIENDS LIST SCREEN =================
                    DisableSelection {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        // Search Bar Item
                        item(key = "friends_search_bar") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Friends",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = friendSearchQuery,
                                    onValueChange = { friendSearchQuery = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    decorationBox = { inner ->
                                        if (friendSearchQuery.isEmpty()) {
                                            Text(
                                                text = "Search friends by name or username...",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }
                        }

                        item(key = "friends_section_title") {
                            Text(
                                text = "ACTIVE DIRECT CONVERSATIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (filteredFriends.isEmpty()) {
                            if (isFriendsLoadedOnce) {
                                item(key = "friends_empty_state") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .breathingPulse(minScale = 0.94f, maxScale = 1.06f)
                                                    .size(60.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(gradientColors)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PersonAdd,
                                                    contentDescription = null,
                                                    tint = contentOnGradient,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }

                                            Text(
                                                text = if (friendSearchQuery.isNotBlank()) "No Matching Friends" else "No Accepted Friends Yet",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )

                                            Text(
                                                text = if (friendSearchQuery.isNotBlank())
                                                    "No accepted friend matching '$friendSearchQuery' found."
                                                else
                                                    "Direct chat is private between accepted friends. Send a friend request and once they accept, you can chat with them here!",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 18.sp
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(Brush.linearGradient(gradientColors))
                                                    .clickable { showAddFriendDialog = true }
                                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PersonAdd,
                                                        contentDescription = null,
                                                        tint = contentOnGradient,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = "Find & Add Friends",
                                                        color = contentOnGradient,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                item(key = "friends_initial_loading") {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                    ) {
                                        repeat(4) {
                                            ShimmerConversationItem()
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(filteredFriends, key = { _, friend -> friend.id }) { index, friend ->
                            val isFriendLocked = friendPrefs.getBoolean("chat_locked_${friend.id}", false)
                            val isUnlocked = unlockedLockedFriends.contains(friend.id)

                            val openFriendChat = {
                                selectedFriend = friend
                                typedMessage = messageDrafts[friend.id] ?: ""
                                friendMessages = emptyList()
                                activeDmChannelId = null
                                isLoadingMessages = true
                                onDirectChatStateChanged(true)
                                val idx = friendsList.indexOfFirst { it.id == friend.id }
                                if (idx != -1) {
                                    friendsList[idx] = friendsList[idx].copy(unreadCount = 0)
                                }
                                readPrefs.edit()
                                    .putInt("unread_${friend.id}", 0)
                                    .putBoolean("read_${friend.id}", true)
                                    .apply()
                                onUnreadStatusChanged(friendsList.any { it.unreadCount > 0 })
                            }

                            StaggeredReveal(index = index) {
                                Card(
                                onClick = {
                                    if (isFriendLocked && !isUnlocked) {
                                        val activity = context.findFragmentActivity()
                                        if (activity != null) {
                                            BiometricAuthManager.authenticate(
                                                activity = activity,
                                                title = "Unlock Chat with ${friend.name}",
                                                subtitle = "Authenticate to view private chat history",
                                                onSuccess = {
                                                    unlockedLockedFriends.add(friend.id)
                                                    openFriendChat()
                                                },
                                                onError = { _, err ->
                                                    Toast.makeText(context, "Authentication failed: $err", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            openFriendChat()
                                        }
                                    } else {
                                        openFriendChat()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar with online status ring
                                    ConnectoAvatar(
                                        name = friend.name,
                                        avatarUrl = friend.avatarUrl,
                                        customSizeDp = 48.dp,
                                        customFontSizeSp = 18,
                                        status = if (friend.isOnline) ConnectoPresenceStatus.ONLINE else ConnectoPresenceStatus.OFFLINE,
                                        borderWidth = 1.5.dp,
                                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Center Info: Name, Handle, and Last Message
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = friend.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (isFriendLocked) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Chat Locked",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = friend.handle,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Text(
                                            text = friend.lastMessage,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Right column: formatted timestamp & unread badge
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = formatIsoTime(friend.timeAgo),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (friend.unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )

                                        if (friend.unreadCount > 0) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${friend.unreadCount}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    } // end DisableSelection
                }
                } else {
                    // ================= 2. DIRECT CHAT STREAM WITH IME RESIZE =================
                    val canScrollDown by remember {
                        derivedStateOf {
                            listState.canScrollForward
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .navigationBarsPadding()
                    ) {
                        // Wallpaper Background Layer: Custom Photo or Preset Gradient Brush
                        if (chatWallpaperId == "custom" && !customWallpaperUri.isNullOrBlank()) {
                            AsyncImage(
                                model = customWallpaperUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Legibility Scrim Overlay so chat bubbles and text remain crisp
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isLight) Color.White.copy(alpha = 0.40f)
                                        else Color.Black.copy(alpha = 0.55f)
                                    )
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = if (isLight) activeChatWallpaper.lightBrush else activeChatWallpaper.darkBrush
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                            if (isLoadingMessages) {
                                ShimmerMessageFeed(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp, vertical = 8.dp)
                                )
                            } else if (friendMessages.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(gradientColors)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SportsEsports,
                                                contentDescription = null,
                                                tint = contentOnGradient,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Text(
                                            text = "Say hello to " + (selectedFriend?.name ?: "Friend") + "!",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "You are connected! Send a message or pick a quick gaming invite below to start chatting.",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                }
                            } else {
                                DisableSelection {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                                ) {
                                    // Security Vault Encryption Chip
                                    item(key = "encryption_chip") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "🔒 End-to-End AES-256-GCM Encrypted Chat Session",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    itemsIndexed(
                                        friendMessages.distinctBy { it.id },
                                        key = { _, msg -> msg.id }
                                    ) { _, msg ->
                                        var dragOffsetX by remember { mutableStateOf(0f) }
                                        val animatedDragOffset by animateFloatAsState(
                                            targetValue = dragOffsetX,
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                                            label = "dragOffset"
                                        )
                                        val hapticFeedback = LocalHapticFeedback.current

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .pointerInput(msg.id) {
                                                    detectHorizontalDragGestures(
                                                        onDragEnd = {
                                                            if (dragOffsetX > 80f) {
                                                                replyingTo = msg
                                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            }
                                                            dragOffsetX = 0f
                                                        },
                                                        onDragCancel = {
                                                            dragOffsetX = 0f
                                                        },
                                                        onHorizontalDrag = { _, dragAmount ->
                                                            // Only allow swiping right (positive dragAmount)
                                                            if (dragAmount > 0 || dragOffsetX > 0) {
                                                                dragOffsetX = (dragOffsetX + dragAmount).coerceIn(0f, 130f)
                                                            }
                                                        }
                                                    )
                                                }
                                        ) {
                                            // Reply indicator icon on the left when swiping right
                                            if (animatedDragOffset > 10f) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.CenterStart)
                                                        .padding(start = 12.dp)
                                                        .size(32.dp)
                                                        .graphicsLayer {
                                                            alpha = (animatedDragOffset / 80f).coerceIn(0f, 1f)
                                                            scaleX = (animatedDragOffset / 80f).coerceIn(0.5f, 1.1f)
                                                            scaleY = (animatedDragOffset / 80f).coerceIn(0.5f, 1.1f)
                                                        }
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Reply",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier.offset(x = animatedDragOffset.dp)
                                            ) {
                                                CustomChatBubble(
                                                    message = msg,
                                                    ownBg = ownBubbleBg,
                                                    ownText = ownBubbleText,
                                                    friendBg = friendBubbleBg,
                                                    friendBorder = friendBubbleBorder,
                                                    friendText = friendBubbleText
                                                )
                                            }
                                        }
                                    }

                                    if (isFriendTyping) {
                                        item(key = "typing_indicator") {
                                            AnimatedTypingIndicator(
                                                friendName = selectedFriend?.name ?: "Friend"
                                            )
                                        }
                                    }
                                }
                                } // end DisableSelection
                            }

                            // Floating "Scroll to Bottom" FAB
                            if (friendMessages.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 16.dp, bottom = 8.dp)
                                ) {
                                    AnimatedVisibility(
                                        visible = canScrollDown,
                                        enter = fadeIn() + scaleIn(),
                                        exit = fadeOut() + scaleOut()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                .shadow(4.dp, CircleShape)
                                                .clickable {
                                                    coroutineScope.launch {
                                                        if (friendMessages.isNotEmpty()) {
                                                            listState.animateScrollToItem(friendMessages.size)
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Scroll to bottom",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ================= QUICK ACTION CHIPS (COLLAPSIBLE) =================
                        AnimatedVisibility(
                            visible = !isInputFocused && typedMessage.isEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            DisableSelection {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val quickChips = listOf(
                                        "👋 Hey, how are you doing?",
                                        "📅 Free for a quick call?",
                                        "🚀 Just sent you an update",
                                        "👍 Sounds great!"
                                    )
                                    items(quickChips) { chipText ->
                                        val chipInteractionSource = remember { MutableInteractionSource() }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                                .clickable(
                                                    interactionSource = chipInteractionSource,
                                                    indication = null
                                                ) {
                                                    typedMessage = chipText
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = chipText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ================= RICH EMOJI PICKER TRAY (100+ CATEGORIZED EMOJIS) =================
                        AnimatedVisibility(
                            visible = showEmojiPicker,
                            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(initialScale = 0.94f),
                            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleOut(targetScale = 0.94f)
                        ) {
                            DisableSelection {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                                ConnectoEmojiPicker(
                                    onEmojiSelected = { emoji ->
                                        typedMessage += emoji
                                    },
                                    onClose = { showEmojiPicker = false }
                                )
                            }
                            } // end DisableSelection
                        }

                        // ================= 3. ADVANCED CHAT INPUT BAR & SEND BUTTON =================
                        val isAcceptedFriend = selectedFriend == null || friendsList.any {
                            it.id == selectedFriend!!.id ||
                            it.handle.removePrefix("@").equals(selectedFriend!!.handle.removePrefix("@"), ignoreCase = true)
                        }

                        if (selectedFriend != null && !isAcceptedFriend) {
                            val targetHandle = selectedFriend!!.handle.removePrefix("@")
                            var isSendingReq by remember { mutableStateOf(false) }
                            var reqSentLocally by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color(0xFFF87171),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Direct Messaging Restricted",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF87171)
                                            )
                                            Text(
                                                text = if (reqSentLocally) "Friend request sent. Waiting for @$targetHandle to accept."
                                                       else "You must be accepted friends to send messages.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (reqSentLocally) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Pending", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                if (!isSendingReq) {
                                                    isSendingReq = true
                                                    coroutineScope.launch {
                                                        try {
                                                            val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                                                            val res = ConnectoApiClient.sendFriendRequest(targetHandle, myUsernameOverride = effUser)
                                                            if (res.isSuccess) {
                                                                reqSentLocally = true
                                                                Toast.makeText(context, "Friend request sent to @$targetHandle", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, "Could not send request: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                        } finally {
                                                            isSendingReq = false
                                                        }
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Add Friend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Swipe-to-Reply active preview bar
                            AnimatedVisibility(
                                visible = replyingTo != null,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                val replyTarget = replyingTo
                                if (replyTarget != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Replying to ${if (replyTarget.isMe) "yourself" else replyTarget.senderName}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = replyTarget.content,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable { replyingTo = null },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cancel reply",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                            // Text Field Box (Multi-line Expandable)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 46.dp, max = 120.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = 1.dp,
                                        color = if (isInputFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(22.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = typedMessage,
                                    onValueChange = { typedMessage = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { isInputFocused = it.isFocused },
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    maxLines = 4,
                                    decorationBox = { inner ->
                                        if (typedMessage.isEmpty()) {
                                            Text(
                                                text = "Type message for " + (selectedFriend?.name ?: "friend") + "...",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp
                                            )
                                        }
                                        inner()
                                    }
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Emoji Toggle Button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable { showEmojiPicker = !showEmojiPicker },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                                        contentDescription = "Emoji picker",
                                        tint = if (showEmojiPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Dynamic Send Action Button
                            val onSendMessage = {
                                if (selectedFriend != null && !isAcceptedFriend) {
                                    Toast.makeText(context, "Direct messaging is restricted to accepted friends.", Toast.LENGTH_SHORT).show()
                                } else if (typedMessage.isNotBlank()) {
                                    val userMsg = typedMessage.trim()
                                    typedMessage = ""
                                    selectedFriend?.let { messageDrafts.remove(it.id) }
                                    showEmojiPicker = false

                                    val myName = ConnectoApiClient.currentUsername ?: currentUsername
                                    val tempId = "opt-${UUID.randomUUID()}"
                                    val currentReply = replyingTo
                                    replyingTo = null // Clear reply banner immediately

                                    val localItem = CustomMessageItem(
                                        id = tempId,
                                        senderName = myName,
                                        initial = userInitial,
                                        content = userMsg,
                                        time = "Just now",
                                        isMe = true,
                                        isDelivered = true,
                                        isRead = true,
                                        avatarUrl = ConnectoApiClient.currentUserAvatarUrl,
                                        replyToId = currentReply?.id,
                                        replyToContent = currentReply?.content,
                                        replyToAuthor = currentReply?.senderName,
                                        isEncrypted = true
                                    )
                                    val updatedList = friendMessages + localItem
                                    friendMessages = updatedList

                                    coroutineScope.launch {
                                        try {
                                            val friend = selectedFriend
                                            val dmId = activeDmChannelId ?: if (friend != null) {
                                                val r = ConnectoApiClient.startDM(friend.handle.removePrefix("@"))
                                                if (r.isSuccess) {
                                                    val id = r.getOrThrow().id
                                                    activeDmChannelId = id
                                                    id
                                                } else null
                                            } else null

                                            if (dmId != null) {
                                                // Encrypt outgoing content with conversation key (AES-256-GCM)
                                                val otherUser = friend?.handle?.removePrefix("@") ?: ""
                                                val conversationKey = ConnectoE2EEncryption.deriveConversationKey(myName, otherUser)
                                                val encryptedContent = ConnectoE2EEncryption.encryptMessage(userMsg, conversationKey)

                                                val sendRes = ConnectoApiClient.sendMessage(
                                                    channelId = dmId,
                                                    content = encryptedContent,
                                                    replyToId = currentReply?.id,
                                                    replyToContent = currentReply?.content,
                                                    replyToAuthor = currentReply?.senderName
                                                )
                                                if (sendRes.isSuccess) {
                                                    val sentDto = sendRes.getOrThrow()
                                                    withContext(Dispatchers.IO) {
                                                        dbHelper.saveMessage(sentDto)
                                                        val cleanTarget = friend?.handle?.removePrefix("@") ?: ""
                                                        val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
                                                        val canonicalName = "dm-${minOf(myUser, cleanTarget.lowercase())}-${maxOf(myUser, cleanTarget.lowercase())}"
                                                        dbHelper.saveMessages(canonicalName, listOf(sentDto))
                                                        if (dmId.isNotBlank() && dmId != canonicalName) {
                                                            dbHelper.saveMessages(dmId, listOf(sentDto))
                                                        }
                                                    }
                                                    val realItem = CustomMessageItem(
                                                        id = sentDto.id,
                                                        senderName = sentDto.authorName,
                                                        initial = userInitial,
                                                        content = userMsg, // Keep decrypted plaintext for local display
                                                        time = formatIsoTime(sentDto.createdAt),
                                                        isMe = true,
                                                        isDelivered = true,
                                                        isRead = true,
                                                        avatarUrl = ConnectoApiClient.currentUserAvatarUrl,
                                                        replyToId = sentDto.replyToId ?: currentReply?.id,
                                                        replyToContent = sentDto.replyToContent ?: currentReply?.content,
                                                        replyToAuthor = sentDto.replyToAuthor ?: currentReply?.senderName,
                                                        isEncrypted = true
                                                    )
                                                    val curList = friendMessages.toMutableList()
                                                    val optIdx = curList.indexOfFirst {
                                                        it.id == tempId || (it.isMe && (it.id.startsWith("opt-") || it.time == "Just now") && it.content.trim() == userMsg)
                                                    }
                                                    if (optIdx != -1) {
                                                        curList[optIdx] = realItem.copy(reaction = curList[optIdx].reaction ?: realItem.reaction)
                                                        friendMessages = curList
                                                    } else if (curList.none { it.id == realItem.id }) {
                                                        curList.add(realItem)
                                                        friendMessages = curList
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Local message remains visible
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .graphicsLayer {
                                        scaleX = sendButtonScale
                                        scaleY = sendButtonScale
                                    }
                                    .clip(CircleShape)
                                    .background(
                                        if (typedMessage.isNotBlank()) Brush.linearGradient(gradientColors)
                                        else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (typedMessage.isNotBlank()) Color.Transparent else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (typedMessage.isNotBlank()) {
                                            onSendMessage()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Hold to record end-to-end encrypted voice note",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (typedMessage.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = contentOnGradient,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Note",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    } // end Wallpaper Box
                }
            }
        }
    }
}
}

@Composable
fun CustomChatBubble(
    message: CustomMessageItem,
    ownBg: Color,
    ownText: Color,
    friendBg: Color,
    friendBorder: Color,
    friendText: Color,
    modifier: Modifier = Modifier
) {
    var showReactionPill by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    var isBubblePressed by remember { mutableStateOf(false) }
    val bubbleScale by animateFloatAsState(
        targetValue = if (isBubblePressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "bubbleScale"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isMe) {
            ConnectoAvatar(
                name = message.senderName,
                avatarUrl = message.avatarUrl,
                customSizeDp = 32.dp,
                customFontSizeSp = 13,
                borderWidth = 1.dp,
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(min = 60.dp, max = 290.dp)
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = bubbleScale
                        scaleY = bubbleScale
                    }
                    .clip(
                        if (message.isMe) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                        else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                    )
                    .background(
                        if (message.isMe) {
                            SolidColor(ownBg)
                        } else {
                            SolidColor(friendBg)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (message.isMe) ownText.copy(alpha = 0.15f) else friendBorder,
                        shape = if (message.isMe) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                        else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                    )
                    .pointerInput(message.id) {
                        detectTapGestures(
                            onPress = {
                                isBubblePressed = true
                                tryAwaitRelease()
                                isBubblePressed = false
                            },
                            onTap = {
                                if (showReactionPill) {
                                    showReactionPill = false
                                }
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showReactionPill = true
                            }
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    // Quoted replied-to message preview
                    if (!message.replyToContent.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (message.isMe) ownText.copy(alpha = 0.12f)
                                    else friendText.copy(alpha = 0.08f)
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (message.isMe) ownText.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                if (!message.replyToAuthor.isNullOrBlank()) {
                                    Text(
                                        text = message.replyToAuthor,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (message.isMe) ownText.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = message.replyToContent,
                                    fontSize = 11.sp,
                                    color = if (message.isMe) ownText.copy(alpha = 0.75f) else friendText.copy(alpha = 0.75f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Text(
                        text = message.content,
                        color = if (message.isMe) ownText else friendText,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatIsoTime(message.time),
                            fontSize = 10.sp,
                            color = if (message.isMe) ownText.copy(alpha = 0.70f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (message.isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val isPending = message.id.startsWith("opt-") || message.time.equals("Just now", ignoreCase = true)
                            Icon(
                                imageVector = if (isPending) Icons.Default.Schedule else Icons.Default.DoneAll,
                                contentDescription = if (isPending) "Sending" else if (message.isRead) "Read" else "Delivered",
                                tint = if (isPending) ownText.copy(alpha = 0.45f) else ownText.copy(alpha = 0.85f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Attached Reaction Badge with Bouncy Spring Entry
            AnimatedVisibility(
                visible = message.reaction != null,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                if (message.reaction != null) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = message.reaction!!, fontSize = 12.sp)
                    }
                }
            }

            // Interactive Emoji Reaction Pill Bar with Spring Overshoot
            AnimatedVisibility(
                visible = showReactionPill,
                enter = scaleIn(
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
                    initialScale = 0.7f
                ) + fadeIn(animationSpec = tween(140)),
                exit = scaleOut(targetScale = 0.7f) + fadeOut(animationSpec = tween(120))
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("👍", "❤️", "😂", "🎉", "🚀", "💡", "👀").forEach { emoji ->
                        var isEmojiPressed by remember { mutableStateOf(false) }
                        val emojiScale by animateFloatAsState(
                            targetValue = if (isEmojiPressed) 1.5f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
                            label = "reactEmojiScale"
                        )

                        Text(
                            text = emoji,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = emojiScale
                                    scaleY = emojiScale
                                }
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    message.reaction = if (message.reaction == emoji) null else emoji
                                    showReactionPill = false
                                }
                                .padding(3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedTypingIndicator(
    friendName: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typingTransition")

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 130, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 260, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(start = 40.dp, top = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = dot1Offset.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot2Offset.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot3Offset.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$friendName is typing...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
