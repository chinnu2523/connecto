package com.example.connecto.ui.screens

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.connecto.network.MessageDto
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
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
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
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
import com.example.connecto.data.WallpaperResolver
import com.example.connecto.data.WallpaperTransform
import com.example.connecto.ui.components.WallpaperCustomizerDialog
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.blur
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.geometry.Offset
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
import com.example.connecto.network.FriendshipDto
import com.example.connecto.ui.components.AddFriendDialog
import com.example.connecto.ui.components.CreateGroupChatDialog
import com.example.connecto.ui.components.CreateVoiceGroupDialog
import com.example.connecto.ui.components.FullScreenGroupProfileDialog
import com.example.connecto.ui.components.ConnectoEmojiPicker
import com.example.connecto.ui.designsystem.isCoverPreset
import com.example.connecto.ui.designsystem.getCoverPreset
import com.example.connecto.ui.designsystem.resolveImageModel
import com.example.connecto.ui.designsystem.sanitizeAvatarUrl
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
import com.example.connecto.ui.theme.glassCanvasMesh
import com.example.connecto.ui.theme.liquidGlassCanvasMesh
import com.example.connecto.ui.theme.glassBubble
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.ProfileDataDto
import com.example.connecto.network.PinnedMessageDto
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
        primaryColor = Color(0xFFFAFAFA),
        secondaryColor = Color(0xFFA1A1AA),
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
    ),
    ChatThemeOption(
        id = "terracotta",
        name = "Warm Terracotta",
        subtitle = "Earthy rust & cream editorial aesthetic",
        primaryColor = Color(0xFFCF603B),
        secondaryColor = Color(0xFFE07A5F),
        ownBubbleBgLight = Color(0xFFCF603B),
        ownBubbleBgDark = Color(0xFFC85A32),
        ownBubbleTextLight = Color(0xFFFFFFFF),
        ownBubbleTextDark = Color(0xFFFFFFFF),
        friendBubbleBgLight = Color(0xFFFDFBF7),
        friendBubbleBgDark = Color(0xFF1C1917),
        friendBubbleTextLight = Color(0xFF292524),
        friendBubbleTextDark = Color(0xFFF5F5F4)
    ),
    ChatThemeOption(
        id = "liquid_glass",
        name = "Liquid Glass",
        subtitle = "Fluid aqua refraction & water droplet shine",
        primaryColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFF38BDF8),
        ownBubbleBgLight = Color(0xDD0097A7),
        ownBubbleBgDark = Color(0xEE0A3355),
        ownBubbleTextLight = Color(0xFFFFFFFF),
        ownBubbleTextDark = Color(0xFFE0F7FA),
        friendBubbleBgLight = Color(0xE6E0F7FA),
        friendBubbleBgDark = Color(0xEE0D1E36),
        friendBubbleTextLight = Color(0xFF006064),
        friendBubbleTextDark = Color(0xFFBAE6FD)
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
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF141312), Color(0xFF1C1A17)))
    ),
    ChatWallpaperOption(
        id = "glass_mesh",
        name = "Prism Glass Aurora",
        subtitle = "Translucent crystal canvas with floating luminous orbs",
        lightBrush = Brush.radialGradient(listOf(Color(0xFFE0F2FE), Color(0xFFF1F5F9))),
        darkBrush = Brush.radialGradient(listOf(Color(0xFF1E3A8A), Color(0xFF0F172A)))
    ),
    ChatWallpaperOption(
        id = "liquid_water",
        name = "Liquid Aqua Droplets",
        subtitle = "Fluid oceanic ripples with water glass refraction",
        lightBrush = Brush.radialGradient(listOf(Color(0xFFE0F7FA), Color(0xFFF0FDF4))),
        darkBrush = Brush.radialGradient(listOf(Color(0xFF0369A1), Color(0xFF082F49)))
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
        darkBrush = Brush.radialGradient(listOf(Color(0xFF581C87), Color(0xFF1E1B4B)))
    ),
    ChatWallpaperOption(
        id = "indigo_flow",
        name = "Indigo Gradient",
        subtitle = "Soft twilight sky gradient",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF3730A3), Color(0xFF1E1B4B)))
    ),
    ChatWallpaperOption(
        id = "emerald_glow",
        name = "Emerald Matrix",
        subtitle = "Deep tactical aurora matrix",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF065F46), Color(0xFF022C22)))
    ),
    ChatWallpaperOption(
        id = "parchment_warm",
        name = "Archival Paper",
        subtitle = "Warm vintage ivory grain",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFFBF8F2), Color(0xFFF3EDE2))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF292524), Color(0xFF1C1917)))
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
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val isGroup: Boolean = false,
    val isCreatedByMe: Boolean = false,
    val memberCount: Int = 0,
    val members: List<String> = emptyList(),
    val admin: String = "",
    val creator: String = "",
    val lastMessageTimestamp: Long = 0L
) {
    fun isUserAdmin(username: String?): Boolean {
        if (!isGroup) return true
        val myClean = username?.trim()?.lowercase()?.removePrefix("@") ?: return false
        val adminClean = admin.trim().lowercase().removePrefix("@")
        val creatorClean = creator.trim().lowercase().removePrefix("@")
        return isCreatedByMe ||
            (adminClean.isNotBlank() && adminClean == myClean) ||
            (creatorClean.isNotBlank() && creatorClean == myClean) ||
            (admin.isBlank() && creator.isBlank() && members.firstOrNull()?.trim()?.lowercase()?.removePrefix("@") == myClean)
    }
}

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
    val isEncrypted: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
)

fun parseMessageTimestamp(timeStr: String?): Long {
    if (timeStr.isNullOrBlank() || timeStr.equals("Just now", ignoreCase = true)) {
        return System.currentTimeMillis()
    }
    val clean = timeStr.trim()
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss.SSSSSS",
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(clean)
            if (date != null) return date.time
        } catch (_: Exception) {}
    }
    return 0L
}

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
    val friendProfilesCache = remember { mutableStateMapOf<String, ProfileDataDto>() }
    var friendProfileSelectedTab by remember { mutableStateOf("settings") } // "settings", "customization", "actions"
    var isFriendProfileOpen by remember { mutableStateOf(false) }
    var friendProfileSubPage by remember { mutableStateOf<String?>(null) }
    var openedProfileFromChatList by remember { mutableStateOf(false) }
    var showFriendProfileDialog by remember { mutableStateOf(false) }
    var showGroupProfileDialog by remember { mutableStateOf(false) }
    var showAddGroupMembersDialog by remember { mutableStateOf(false) }
    var showUnfriendConfirmDialog by remember { mutableStateOf(false) }
    var isUnfriending by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showCreateGroupChatDialog by remember { mutableStateOf(false) }
    var showCreateVoiceGroupDialog by remember { mutableStateOf(false) }
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

    // Send Button Animation State
    var isSendPressed by remember { mutableStateOf(false) }
    val sendButtonScale by animateFloatAsState(
        targetValue = if (isSendPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "sendScale"
    )

    val context = LocalContext.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val readPrefs = remember { context.getSharedPreferences("connecto_chat_read_prefs", Context.MODE_PRIVATE) }
    val friendPrefs = remember { context.getSharedPreferences("connecto_friend_prefs", Context.MODE_PRIVATE) }
    val notifPrefs = remember { context.getSharedPreferences("connecto_notifications_prefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { ConnectoDatabaseHelper.getInstance(context) }
    val myUsername = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@").lowercase()
    val chatActivity = remember(context) { context.findFragmentActivity() }
    androidx.compose.runtime.LaunchedEffect(myUsername) {
        com.example.connecto.security.ScreenSecurityManager.applySecurityPolicy(chatActivity)
    }

    fun getCanonicalChatId(f: FriendItem?): String {
        if (f == null) return ""
        if (f.isGroup) return f.id
        val clean = f.handle.trim().removePrefix("@").lowercase()
        val u = myUsername.lowercase()
        return "dm-${minOf(u, clean)}-${maxOf(u, clean)}"
    }

    // Direct Messages Feed & State
    var activeDmChannelId by remember { mutableStateOf<String?>(null) }
    var friendMessages by remember {
        mutableStateOf<List<CustomMessageItem>>(emptyList())
    }
    var pinnedMessageForChat by remember { mutableStateOf<PinnedMessageDto?>(null) }
    var messageForAction by remember { mutableStateOf<CustomMessageItem?>(null) }
    var showMessageActionDialog by remember { mutableStateOf(false) }

    fun deduplicateCustomMessages(messages: List<CustomMessageItem>): List<CustomMessageItem> {
        val result = mutableListOf<CustomMessageItem>()
        for (msg in messages) {
            val existingIndex = result.indexOfFirst { existing ->
                if (existing.id == msg.id) return@indexOfFirst true
                val existingIsTemp = existing.id.startsWith("opt-") || existing.id.startsWith("ws_")
                val newIsTemp = msg.id.startsWith("opt-") || msg.id.startsWith("ws_")
                // Only merge by content and timestamp if at least one of them is temporary (optimistic)
                if (existingIsTemp || newIsTemp) {
                    val sameContent = existing.content.trim() == msg.content.trim() && msg.content.isNotBlank()
                    if (sameContent && existing.isMe == msg.isMe) {
                        val timeDiff = kotlin.math.abs(existing.timestampMillis - msg.timestampMillis)
                        return@indexOfFirst (timeDiff <= 30_000L || existing.time == msg.time)
                    }
                }
                false
            }
            if (existingIndex == -1) {
                result.add(msg)
            } else {
                val existing = result[existingIndex]
                val existingIsTemp = existing.id.startsWith("opt-") || existing.id.startsWith("ws_")
                val newIsTemp = msg.id.startsWith("opt-") || msg.id.startsWith("ws_")
                if (existingIsTemp && !newIsTemp) {
                    result[existingIndex] = msg.copy(
                        reaction = existing.reaction ?: msg.reaction,
                        isDelivered = existing.isDelivered || msg.isDelivered,
                        isRead = existing.isRead || msg.isRead
                    )
                } else {
                    result[existingIndex] = existing.copy(
                        reaction = existing.reaction ?: msg.reaction,
                        isDelivered = existing.isDelivered || msg.isDelivered,
                        isRead = existing.isRead || msg.isRead,
                        senderName = if (existing.senderName.contains("@") && !msg.senderName.contains("@")) msg.senderName else existing.senderName
                    )
                }
            }
        }
        return result.sortedBy { it.timestampMillis }
    }

    fun loadLocalMessagesForFriend(friend: FriendItem): List<CustomMessageItem> {
        val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
        val cleanTarget = friend.handle.removePrefix("@")
        val canonicalName = if (friend.isGroup) friend.id else "dm-${minOf(myUser, cleanTarget.lowercase())}-${maxOf(myUser, cleanTarget.lowercase())}"
        val existingDmId = dbHelper.getDmChannelId(canonicalName)
        val cleanDmId = if (existingDmId != null && !existingDmId.startsWith("grp-") && !existingDmId.startsWith("grp_") && !existingDmId.startsWith("group-")) existingDmId else null

        val cachedMessages = if (friend.isGroup) {
            dbHelper.getMessagesForChannel(friend.id, limit = 50)
        } else {
            dbHelper.getDirectMessages(
                myUsername = myUser,
                friendUsername = cleanTarget,
                channelId = cleanDmId,
                limit = 50
            )
        }

        if (cachedMessages.isEmpty()) return emptyList()

        val myUName = ConnectoApiClient.currentUsername ?: currentUsername
        val myDisplayName = ConnectoApiClient.currentUserDisplayName ?: ""
        val myUserId = ConnectoApiClient.currentUserId
        val otherUser = friend.handle.removePrefix("@")
        val e2eSecret = ConnectoApiClient.getE2ESecret()
        val conversationKey = ConnectoE2EEncryption.deriveConversationKey(myUName, otherUser, e2eSecret)
        val fallbackConversationKey = ConnectoE2EEncryption.deriveConversationKey(myUName, otherUser, null)
        val isRecipientOnline = friend.isOnline

        val mappedCached = cachedMessages.map { dto ->
            val isMe = (
                dto.authorId.equals(myUName, ignoreCase = true) ||
                dto.authorName.equals(myUName, ignoreCase = true) ||
                (myDisplayName.isNotBlank() && dto.authorName.equals(myDisplayName, ignoreCase = true)) ||
                (myUserId != null && (dto.authorId == myUserId || dto.authorName == myUserId)) ||
                dto.authorId.equals(currentUsername, ignoreCase = true) ||
                dto.authorName.equals(currentUsername, ignoreCase = true)
            )
            val effectiveSenderName = if (isMe) {
                if (myDisplayName.isNotBlank()) myDisplayName else myUName
            } else {
                if (dto.authorName.isNotBlank() && !dto.authorName.equals("gamer", ignoreCase = true)) {
                    dto.authorName
                } else {
                    if (friend.isGroup) dto.authorName else friend.name
                }
            }
            val init = if (effectiveSenderName.isNotBlank()) effectiveSenderName.first().toString().uppercase() else "U"
            val msgAvatar = if (isMe) {
                ConnectoApiClient.currentUserAvatarUrl
            } else {
                dto.authorAvatar ?: (if (friend.isGroup) null else friend.avatarUrl)
            }

            val (decryptedContent, decryptedReplyContent, isEnc) = if (friend.isGroup) {
                Triple(dto.content, dto.replyToContent, false)
            } else {
                val enc = ConnectoE2EEncryption.isEncrypted(dto.content)
                val decMsg = if (enc) ConnectoE2EEncryption.decryptMessage(dto.content, conversationKey, fallbackConversationKey) else dto.content
                val decReply = if (dto.replyToContent != null && ConnectoE2EEncryption.isEncrypted(dto.replyToContent)) {
                    ConnectoE2EEncryption.decryptMessage(dto.replyToContent, conversationKey, fallbackConversationKey)
                } else dto.replyToContent
                Triple(decMsg, decReply, enc)
            }

            val msgMillis = parseMessageTimestamp(dto.createdAt)
            val isReadValue = if (isMe) (dto.isRead ?: false) else true
            CustomMessageItem(
                id = dto.id,
                senderName = effectiveSenderName,
                initial = init,
                content = decryptedContent,
                time = formatIsoTime(dto.createdAt),
                isMe = isMe,
                isDelivered = if (isMe) (if (friend.isGroup) true else isRecipientOnline) else true,
                isRead = isReadValue,
                avatarUrl = msgAvatar,
                replyToId = dto.replyToId,
                replyToContent = decryptedReplyContent,
                replyToAuthor = dto.replyToAuthor,
                isEncrypted = isEnc,
                timestampMillis = msgMillis
            )
        }

        val delPrefsKey = "deleted_for_me_${myUser.lowercase()}"
        val deletedSet = friendPrefs.getStringSet(delPrefsKey, emptySet()) ?: emptySet()
        val lastFriendMsgTime = mappedCached.filter { !it.isMe }.maxOfOrNull { it.timestampMillis } ?: 0L
        val resolvedCached = mappedCached
            .filter { it.id !in deletedSet }
            .map { msg ->
                if (msg.isMe) {
                    if (msg.isRead || (lastFriendMsgTime > 0 && msg.timestampMillis <= lastFriendMsgTime)) {
                        msg.copy(isDelivered = true, isRead = true)
                    } else {
                        msg.copy(isDelivered = if (friend.isGroup) true else isRecipientOnline, isRead = false)
                    }
                } else msg
            }
        return deduplicateCustomMessages(resolvedCached)
    }

    val currentChatKey = selectedFriend?.let { getCanonicalChatId(it) } ?: ""
    val initialScrollIndex = remember(currentChatKey) {
        if (friendMessages.isNotEmpty()) friendMessages.size else 0
    }
    val listState = remember(currentChatKey) {
        LazyListState(firstVisibleItemIndex = initialScrollIndex)
    }

    // Friend Profile Preview Lightbox & Per-Chat Preferences
    var showPhotoLightbox by remember { mutableStateOf(false) }
    var isChatMuted by remember { mutableStateOf(false) }
    var isChatLocked by remember { mutableStateOf(false) }
    var chatThemeId by remember { mutableStateOf("default") }
    var chatWallpaperId by remember { mutableStateOf("default") }
    var customWallpaperUri by remember { mutableStateOf<String?>(null) }
    var wallpaperTransform by remember { mutableStateOf(WallpaperTransform()) }
    var showWallpaperCustomizerDialog by remember { mutableStateOf(false) }

    // Synchronous wallpaper resolution helper (0ms latency, zero cross-chat overlap, backwards-compatible & auto-healing)
    fun resolveChatWallpaper(friend: FriendItem?): Pair<String, String?> {
        return WallpaperResolver.resolve(
            friend = friend,
            myUsername = myUsername,
            prefs = friendPrefs,
            filesDir = context.filesDir
        )
    }

    // Custom Wallpaper Photo Picker Launcher
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val sf = selectedFriend
        if (sf != null && sf.isGroup) {
            val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
            if (!sf.isUserAdmin(effUser)) {
                Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
        }
        if (uri != null) {
            val chatKey = getCanonicalChatId(sf)
            val fId = sf?.id
            val cleanHandle = sf?.handle?.trim()?.removePrefix("@")?.lowercase() ?: ""
            val userPrefix = myUsername.lowercase()

            val fileUriStr = try {
                val wallpapersDir = java.io.File(context.filesDir, "chat_wallpapers")
                if (!wallpapersDir.exists()) wallpapersDir.mkdirs()
                val targetKey = if (chatKey.isNotBlank()) chatKey else if (cleanHandle.isNotBlank()) cleanHandle else (fId ?: "default")
                val localFile = java.io.File(wallpapersDir, "wp_${userPrefix}_${targetKey}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    localFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                android.net.Uri.fromFile(localFile).toString()
            } catch (e: Exception) {
                uri.toString()
            }

            customWallpaperUri = fileUriStr
            chatWallpaperId = "custom"
            val editor = friendPrefs.edit()
            if (chatKey.isNotBlank()) {
                editor.putString("${userPrefix}_chat_wallpaper_custom_$chatKey", fileUriStr)
                    .putString("${userPrefix}_chat_wallpaper_$chatKey", "custom")
            }
            if (cleanHandle.isNotBlank()) {
                editor.putString("${userPrefix}_chat_wallpaper_custom_$cleanHandle", fileUriStr)
                    .putString("${userPrefix}_chat_wallpaper_$cleanHandle", "custom")
            }
            if (fId != null) {
                editor.putString("${userPrefix}_chat_wallpaper_custom_$fId", fileUriStr)
                    .putString("${userPrefix}_chat_wallpaper_$fId", "custom")
                    .remove("chat_wallpaper_custom_$fId")
                    .remove("chat_wallpaper_$fId")
            }
            editor.apply()
        }
    }

    // Session cache of unlocked friends during current app session
    val unlockedLockedFriends = remember { mutableStateListOf<String>() }
    var pendingLockedFriendToOpen by remember { mutableStateOf<FriendItem?>(null) }
    var isBiometricAuthPromptActive by remember { mutableStateOf(false) }

    // Synchronize mute, lock, theme, wallpaper when selectedFriend changes
    LaunchedEffect(selectedFriend?.id, myUsername) {
        val friend = selectedFriend
        if (friend != null) {
            val (resolvedWp, resolvedUri) = resolveChatWallpaper(friend)
            chatWallpaperId = resolvedWp
            customWallpaperUri = resolvedUri
            wallpaperTransform = WallpaperResolver.resolveTransform(friend, myUsername, friendPrefs)
            val friendId = friend.id
            val cleanHandle = friend.handle.trim().lowercase().removePrefix("@")
            val userPrefix = myUsername.lowercase()
            val mutedSet = notifPrefs.getStringSet("muted_channels", emptySet()) ?: emptySet()
            isChatMuted = mutedSet.any { it.trim().lowercase().removePrefix("#").removePrefix("@") == cleanHandle || it.trim().lowercase() == friendId.lowercase() }
            isChatLocked = friendPrefs.getBoolean("${userPrefix}_chat_locked_$friendId", false) || friendPrefs.getBoolean("${userPrefix}_chat_locked_$cleanHandle", false)
            chatThemeId = friendPrefs.getString("${userPrefix}_chat_theme_$friendId", null) ?: "default"
        } else {
            chatWallpaperId = "default"
            customWallpaperUri = null
            wallpaperTransform = WallpaperResolver.resolveTransform(null, myUsername, friendPrefs)
        }
    }

    // Active Chat Theme & Wallpaper Resolution (Auto-adapts to Liquid Glass when app is in liquid glass theme)
    val isGlassTheme = ConnectoTheme.isGlass
    val isLiquidGlassTheme = ConnectoTheme.isLiquidGlass
    val isPureBlack = ConnectoTheme.colors.background == Color(0xFF000000)
    val activeChatTheme = remember(chatThemeId, isGlassTheme, isLiquidGlassTheme) {
        if (chatThemeId != "default") {
            val resolvedId = if (chatThemeId == "glass") "liquid_glass" else chatThemeId
            AVAILABLE_CHAT_THEMES.firstOrNull { it.id == resolvedId } ?: AVAILABLE_CHAT_THEMES.first()
        } else if (isLiquidGlassTheme || isGlassTheme) {
            AVAILABLE_CHAT_THEMES.firstOrNull { it.id == "liquid_glass" } ?: AVAILABLE_CHAT_THEMES.first()
        } else {
            AVAILABLE_CHAT_THEMES.first()
        }
    }
    val activeChatWallpaper = remember(chatWallpaperId, isGlassTheme, isLiquidGlassTheme) {
        if (chatWallpaperId != "default") {
            AVAILABLE_CHAT_WALLPAPERS.firstOrNull { it.id == chatWallpaperId } ?: AVAILABLE_CHAT_WALLPAPERS.first()
        } else if (isLiquidGlassTheme || isGlassTheme) {
            AVAILABLE_CHAT_WALLPAPERS.firstOrNull { it.id == "liquid_water" } ?: AVAILABLE_CHAT_WALLPAPERS.first()
        } else {
            AVAILABLE_CHAT_WALLPAPERS.first()
        }
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
    val friendBubbleBorder = if (chatThemeId == "default" && !isGlassTheme && !isLiquidGlassTheme) {
        if (isLight) Color(0xFFE2E8F0) else Color(0xFF27272A)
    } else {
        activeChatTheme.primaryColor.copy(alpha = if (isLiquidGlassTheme) 0.60f else if (isGlassTheme) 0.50f else 0.35f)
    }
    val friendBubbleText = if (isLight) activeChatTheme.friendBubbleTextLight else activeChatTheme.friendBubbleTextDark

    // Friends List State - start empty, populated by IO LaunchedEffect below for 0ms Main-thread blocking
    val friendsList = remember { mutableStateListOf<FriendItem>() }
    var isFriendsLoadedOnce by remember { mutableStateOf(false) }

    // User Groups State (Created & Added / Joined Groups)
    val userGroupsList = remember { mutableStateListOf<FriendItem>() }
    var messageTabFilter by rememberSaveable { mutableStateOf("ALL") } // "ALL", "DIRECT", "CREATED_GROUPS", "ADDED_GROUPS"

    val markFriendChatAsReadLocallyAndRemotely: (FriendItem) -> Unit = { targetFriend ->
        val cleanTarget = targetFriend.handle.removePrefix("@").trim()
        val friendId = targetFriend.id

        // 1. Update in-memory friends list
        val idx = friendsList.indexOfFirst {
            it.id == friendId || it.handle.removePrefix("@").equals(cleanTarget, ignoreCase = true)
        }
        if (idx != -1) {
            friendsList[idx] = friendsList[idx].copy(unreadCount = 0)
        }

        // 2. Persist to SharedPreferences for both ID and clean username keys
        readPrefs.edit()
            .putInt("unread_$friendId", 0)
            .putBoolean("read_$friendId", true)
            .putInt("unread_${cleanTarget.lowercase()}", 0)
            .putBoolean("read_${cleanTarget.lowercase()}", true)
            .apply()

        // 3. Persist to local SQLite database (TABLE_FRIENDS) so cold starts don't resurrect unread badge
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                dbHelper.updateFriendUnreadCount(friendId, 0)
                if (cleanTarget.isNotBlank() && cleanTarget != friendId) {
                    dbHelper.updateFriendUnreadCount(cleanTarget, 0)
                }
            } catch (_: Exception) {}
        }

        // 4. Notify backend API to update dm_read_states
        if (!targetFriend.isGroup && cleanTarget.isNotBlank()) {
            coroutineScope.launch {
                try {
                    ConnectoApiClient.markDmRead(cleanTarget)
                } catch (_: Exception) {}
            }
        }

        // 5. Update global unread indicator
        val hasRemainingUnread = friendsList.any { it.unreadCount > 0 }
        onUnreadStatusChanged(hasRemainingUnread)
    }

    // Auto-select chat or group when opened from navigation or push notification intent
    LaunchedEffect(targetChatUserOrChannel, friendsList.size, userGroupsList.size) {
        val target = targetChatUserOrChannel?.trim()
        if (!target.isNullOrBlank()) {
            val clean = target.lowercase().removePrefix("@").removePrefix("#").removePrefix("dm-").removePrefix("dm_")
            val matchingFriend = friendsList.firstOrNull {
                it.id.equals(clean, ignoreCase = true) ||
                it.handle.removePrefix("@").equals(clean, ignoreCase = true) ||
                it.name.equals(clean, ignoreCase = true) ||
                (target.startsWith("dm-") && target.contains(it.handle.removePrefix("@").lowercase()))
            } ?: userGroupsList.firstOrNull {
                it.id.equals(clean, ignoreCase = true) ||
                it.handle.removePrefix("#").equals(clean, ignoreCase = true) ||
                it.name.equals(clean, ignoreCase = true)
            }
            friendMessages = emptyList()
            activeDmChannelId = null
            if (matchingFriend != null) {
                val (wId, cUri) = resolveChatWallpaper(matchingFriend)
                chatWallpaperId = wId
                customWallpaperUri = cUri
                selectedFriend = matchingFriend
                markFriendChatAsReadLocallyAndRemotely(matchingFriend)
            } else {
                val displayName = clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                val targetFriend = FriendItem(
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
                val (wId, cUri) = resolveChatWallpaper(targetFriend)
                chatWallpaperId = wId
                customWallpaperUri = cUri
                selectedFriend = targetFriend
                markFriendChatAsReadLocallyAndRemotely(targetFriend)
            }
            onTargetChatHandled?.invoke()
        }
    }

    // Load cached friends AND cached groups from SQLite on IO thread on first composition for 0ms instant render
    LaunchedEffect(Unit) {
        val (cachedFriends, cachedGroups) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
            if (effUser.isNotBlank()) {
                dbHelper.sanitizeFriendsLastMessages(effUser)
            }
            val list = dbHelper.getCachedFriends()
            val mappedFriends = list.map { f ->
                val cleanHandle = f.handle.removePrefix("@").trim().lowercase()
                val latest = if (effUser.isNotBlank()) dbHelper.getLatestMessageInfoForFriend(effUser, cleanHandle) else null
                val cleanMsg = if (latest != null && latest.first.isNotBlank() && latest.first != "null" && latest.first != "None") {
                    latest.first
                } else if (f.lastMessage.isNotBlank() && f.lastMessage != "null" && f.lastMessage != "None" && latest != null) {
                    f.lastMessage
                } else {
                    // Stale preview cleanup: if no direct messages exist, clean SharedPreferences and state
                    readPrefs.edit()
                        .remove("last_msg_${f.id}")
                        .remove("last_msg_${cleanHandle}")
                        .remove("last_time_${f.id}")
                        .remove("last_time_${cleanHandle}")
                        .remove("last_timestamp_${f.id}")
                        .remove("last_timestamp_${cleanHandle}")
                        .apply()
                    "Direct chat active • Tap to message"
                }
                val ts = if (latest != null && latest.second > 0L) latest.second else 0L
                val time = if (latest != null && latest.second > 0L) {
                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(latest.second))
                } else {
                    if (cleanMsg == "Direct chat active • Tap to message") "Active" else f.timeAgo
                }
                f.copy(lastMessage = cleanMsg, timeAgo = time, lastMessageTimestamp = ts)
            }

            val groupList = dbHelper.getCachedGroups()
            val mappedGroups = groupList.map { g ->
                var ts = g.lastMessageTimestamp
                if (ts == 0L) {
                    ts = readPrefs.getLong("last_timestamp_${g.id}", 0L)
                }
                val rawMsg = readPrefs.getString("last_msg_${g.id}", null)
                val lastMsg = if (!rawMsg.isNullOrBlank()) rawMsg else g.lastMessage
                val rawTime = readPrefs.getString("last_time_${g.id}", null)
                val time = formatIsoTime(if (!rawTime.isNullOrBlank()) rawTime else g.timeAgo)
                g.copy(lastMessage = lastMsg, timeAgo = time, lastMessageTimestamp = ts)
            }
            Pair(mappedFriends, mappedGroups)
        }
        if (cachedFriends.isNotEmpty() && friendsList.isEmpty()) {
            friendsList.addAll(cachedFriends)
            isFriendsLoadedOnce = true
        }
        if (cachedGroups.isNotEmpty() && userGroupsList.isEmpty()) {
            userGroupsList.addAll(cachedGroups)
        }
        if (userGroupsList.none { !it.isCreatedByMe }) {
            userGroupsList.add(
                FriendItem(
                    id = "grp-design-team-joined",
                    name = "Design Team",
                    handle = "#design-team",
                    initial = "D",
                    status = "4 members • Joined Group",
                    bio = "Design Team Discussions • Admin: @devi",
                    lastMessage = "Hey team, welcome to the design channel!",
                    timeAgo = "1h ago",
                    unreadCount = 0,
                    isOnline = false,
                    avatarUrl = null,
                    isGroup = true,
                    isCreatedByMe = false,
                    memberCount = 4,
                    members = listOf("devi", "srinu", "chinnu14754x", "test1"),
                    admin = "devi",
                    creator = "devi",
                    lastMessageTimestamp = System.currentTimeMillis() - 3600000L
                )
            )
        }
    }

    var pending1on1Friend by remember { mutableStateOf<FriendItem?>(null) }
    var pendingGroupVoiceCallTarget by remember { mutableStateOf<FriendItem?>(null) }
    var pendingVoiceGroupCallInfo by remember { mutableStateOf<Pair<String, List<FriendshipDto>>?>(null) }

    fun perform1on1Call(friend: FriendItem) {
        val friendUser = friend.handle.removePrefix("@").ifBlank { friend.name }
        val friendName = friend.name.ifBlank { friendUser }
        val friendClean = friendUser.trim().lowercase()
        val prof = (if (selectedFriend?.id == friend.id) selectedFriendProfileData else null)?.takeIf {
            it.username.lowercase() == friendClean || (it.id.isNotBlank() && it.id == friend.id)
        } ?: friendProfilesCache[friendClean]
        val resolvedAvatar = friend.avatarUrl ?: prof?.avatarUrl
        VoiceCallManager.start1on1Call(friendUser, friendName, avatarUrl = resolvedAvatar)
    }

    fun performGroupVoiceCall(group: FriendItem) {
        val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
        val currentU = effUser.trim().removePrefix("@")
        val roomId = "group_voice_${group.id}_${System.currentTimeMillis()}"
        val groupTitle = group.name.ifBlank { "Group Voice Call" }
        VoiceCallManager.joinVoiceRoom(roomId = roomId, roomName = groupTitle, roomCode = roomId)

        coroutineScope.launch {
            val targets = if (group.members.isNotEmpty()) {
                group.members.map { it.trim().removePrefix("@") }.filter { !it.equals(currentU, ignoreCase = true) }
            } else {
                userGroupsList.firstOrNull { it.id == group.id }?.members?.map { it.trim().removePrefix("@") }?.filter { !it.equals(currentU, ignoreCase = true) } ?: emptyList()
            }

            for (target in targets) {
                try {
                    VoiceCallManager.inviteToVoiceGroup(
                        roomId = roomId,
                        groupTitle = groupTitle,
                        targetUser = target
                    )
                    ConnectoApiClient.inviteToVoiceCall(target, roomId)
                } catch (_: Exception) {}
            }

            ConnectoApiClient.recordCallLog(
                callerName = groupTitle,
                callType = "Group Call (${targets.size + 1} members)",
                isMissed = false,
                isOutgoing = true
            )
        }
    }

    fun performVoiceGroupCallWithFriends(title: String, selectedFriendsList: List<FriendshipDto>) {
        val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
        val currentU = effUser.trim().removePrefix("@")
        val roomId = "group_voice_${currentU}_${System.currentTimeMillis()}"
        val groupTitle = title.ifBlank { "Voice Group Call" }
        VoiceCallManager.joinVoiceRoom(roomId = roomId, roomName = groupTitle, roomCode = roomId)

        coroutineScope.launch {
            for (friend in selectedFriendsList) {
                val target = friend.friendUsername.trim().removePrefix("@")
                try {
                    VoiceCallManager.inviteToVoiceGroup(
                        roomId = roomId,
                        groupTitle = groupTitle,
                        targetUser = target
                    )
                    ConnectoApiClient.inviteToVoiceCall(target, roomId)
                } catch (_: Exception) {}
            }

            ConnectoApiClient.recordCallLog(
                callerName = groupTitle,
                callType = "Group Call (${selectedFriendsList.size + 1} members)",
                isMissed = false,
                isOutgoing = true
            )
        }
    }

    // Safe Audio Permission Launcher for 1:1 and Group Voice Calls
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            VoiceCallManager.restartAudioHardwareIfConnected()
            pendingGroupVoiceCallTarget?.let { grp ->
                val targetGrp = grp
                pendingGroupVoiceCallTarget = null
                performGroupVoiceCall(targetGrp)
                return@rememberLauncherForActivityResult
            }
            pendingVoiceGroupCallInfo?.let { (title, friends) ->
                pendingVoiceGroupCallInfo = null
                performVoiceGroupCallWithFriends(title, friends)
                return@rememberLauncherForActivityResult
            }
            pending1on1Friend?.let { friend ->
                pending1on1Friend = null
                perform1on1Call(friend)
                return@rememberLauncherForActivityResult
            }
            val target = selectedFriend
            if (target != null) {
                perform1on1Call(target)
            }
        } else {
            Toast.makeText(context, "Microphone permission is required for voice calls", Toast.LENGTH_SHORT).show()
        }
    }

    fun initiateVoiceCallWithFriend(friend: FriendItem) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            perform1on1Call(friend)
        } else {
            pending1on1Friend = friend
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun initiateGroupVoiceCall(group: FriendItem) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            performGroupVoiceCall(group)
        } else {
            pendingGroupVoiceCallTarget = group
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun initiateVoiceGroupCallWithFriends(title: String, selectedFriendsList: List<FriendshipDto>) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            performVoiceGroupCallWithFriends(title, selectedFriendsList)
        } else {
            pendingVoiceGroupCallInfo = Pair(title, selectedFriendsList)
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
                    val cachedFriend = dbHelper.getFriendByUsernameOrHandle(cleanUsername) ?: dbHelper.getFriendByUsernameOrHandle(f.friendId)
                    val unread = maxOf(
                        readPrefs.getInt("unread_${f.friendId}", 0),
                        readPrefs.getInt("unread_${cleanUsername.lowercase()}", 0),
                        cachedFriend?.unreadCount ?: 0,
                        f.unreadCount
                    )
                    val rawPrefMsg = readPrefs.getString("last_msg_${f.friendId}", null) ?: readPrefs.getString("last_msg_${cleanUsername.lowercase()}", null)
                    val sanitizedPrefMsg = if (rawPrefMsg == "null" || rawPrefMsg == "None" || rawPrefMsg.isNullOrBlank()) null else rawPrefMsg

                    // Resolve latest real message from local direct message database
                    val latest = if (effUsername.isNotBlank()) dbHelper.getLatestMessageInfoForFriend(effUsername, cleanUsername) else null

                    val (lastMsg, time, lastTs) = if (latest != null && latest.first.isNotBlank() && latest.first != "null" && latest.first != "None") {
                        val formattedTime = if (latest.second > 0L) SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(latest.second)) else "Just now"
                        Triple(latest.first, formattedTime, latest.second)
                    } else if (!sanitizedPrefMsg.isNullOrBlank() && sanitizedPrefMsg != "Direct chat active • Tap to message" && latest != null) {
                        val rawPrefTime = readPrefs.getString("last_time_${f.friendId}", null) ?: readPrefs.getString("last_time_${cleanUsername.lowercase()}", null)
                        Triple(sanitizedPrefMsg, formatIsoTime(if (!rawPrefTime.isNullOrBlank()) rawPrefTime else "Active"), maxOf(readPrefs.getLong("last_timestamp_${f.friendId}", 0L), readPrefs.getLong("last_timestamp_${cleanUsername.lowercase()}", 0L)))
                    } else {
                        // Purge any stale preview keys for friend with 0 messages
                        readPrefs.edit()
                            .remove("last_msg_${f.friendId}")
                            .remove("last_msg_${cleanUsername.lowercase()}")
                            .remove("last_time_${f.friendId}")
                            .remove("last_time_${cleanUsername.lowercase()}")
                            .remove("last_timestamp_${f.friendId}")
                            .remove("last_timestamp_${cleanUsername.lowercase()}")
                            .apply()
                        Triple("Direct chat active • Tap to message", "Active", 0L)
                    }
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
                        avatarUrl = f.friendAvatarUrl ?: cachedFriend?.avatarUrl,
                        bannerUrl = f.friendBannerUrl ?: cachedFriend?.bannerUrl,
                        lastMessageTimestamp = lastTs
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
                    if (idx == -1) {
                        friendsList.add(item)
                    } else {
                        val cleanU = item.handle.removePrefix("@").trim().lowercase()
                        val existingTs = friendsList[idx].lastMessageTimestamp
                        val effectiveTs = maxOf(existingTs, item.lastMessageTimestamp)
                        val existingUnread = friendsList[idx].unreadCount
                        val isChatOpen = selectedFriend != null && (
                            selectedFriend?.id == item.id ||
                            selectedFriend?.handle?.removePrefix("@")?.trim()?.equals(cleanU, ignoreCase = true) == true
                        )
                        val effectiveUnread = if (isChatOpen) 0 else maxOf(existingUnread, item.unreadCount)
                        val effectiveLastMsg = if (item.lastMessage.isNotBlank() && item.lastMessage != "Direct chat active • Tap to message") item.lastMessage else friendsList[idx].lastMessage
                        val effectiveTime = if (item.timeAgo.isNotBlank() && item.timeAgo != "Active") item.timeAgo else friendsList[idx].timeAgo
                        friendsList[idx] = item.copy(
                            lastMessage = effectiveLastMsg,
                            timeAgo = effectiveTime,
                            unreadCount = effectiveUnread,
                            lastMessageTimestamp = effectiveTs
                        )
                    }
                }
                isFriendsLoadedOnce = true

                // Sync real unread counts from server (authoritative — excludes own sent messages)
                try {
                    val unreadRes = ConnectoApiClient.getDmUnreadCounts()
                    if (unreadRes.isSuccess) {
                        val serverUnread = unreadRes.getOrThrow() // Map<String, Int> partner→count
                        for (i in friendsList.indices) {
                            val friendHandle = friendsList[i].handle.removePrefix("@").trim().lowercase()
                            val friendId = friendsList[i].id.trim().lowercase()
                            val friendName = friendsList[i].name.trim().lowercase()
                            val isCurrentlyOpen = selectedFriend != null && (
                                selectedFriend?.id == friendsList[i].id ||
                                selectedFriend?.handle?.removePrefix("@")?.trim()?.equals(friendHandle, ignoreCase = true) == true
                            )
                            val serverCount = if (isCurrentlyOpen) {
                                0
                            } else {
                                serverUnread[friendHandle] ?:
                                serverUnread[friendId] ?:
                                serverUnread.entries.firstOrNull { entry ->
                                    val k = entry.key.trim().removePrefix("@").lowercase()
                                    k == friendHandle || k == friendId || k == friendName
                                }?.value ?: friendsList[i].unreadCount // Preserve in-memory unread count if server omitted
                            }
                            if (serverCount != friendsList[i].unreadCount) {
                                friendsList[i] = friendsList[i].copy(unreadCount = serverCount)
                                readPrefs.edit()
                                    .putInt("unread_${friendsList[i].id}", serverCount)
                                    .putInt("unread_$friendHandle", serverCount)
                                    .apply()
                                val fid = friendsList[i].id
                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    try {
                                        dbHelper.updateFriendUnreadCount(fid, serverCount)
                                        if (friendHandle.isNotBlank() && friendHandle != fid) {
                                            dbHelper.updateFriendUnreadCount(friendHandle, serverCount)
                                        }
                                    } catch (_: Exception) {}
                                }
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

            // Sync user groups (Created & Added / Joined groups)
            try {
                val groupsRes = ConnectoApiClient.getUserGroups()
                if (groupsRes.isSuccess) {
                    val serverGroups = groupsRes.getOrThrow()
                    val myUsernameLower = effUsername.trim().lowercase().removePrefix("@")
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
                        val isCreated = g.isCreatedByMe || g.description.contains(effUsername, ignoreCase = true) || g.admin.lowercase().removePrefix("@") == myUsernameLower
                        val gLastMsg = readPrefs.getString("last_msg_${g.id}", "Group Chat • Tap to open and talk") ?: "Group Chat • Tap to open and talk"
                        val gTime = formatIsoTime(readPrefs.getString("last_time_${g.id}", "Active") ?: "Active")
                        val gTs = readPrefs.getLong("last_timestamp_${g.id}", 0L)
                        FriendItem(
                            id = g.id,
                            name = g.name,
                            handle = "#${g.name.lowercase().replace(" ", "-")}",
                            initial = init,
                            status = "$count members • ${if (isCreated) "Created Group" else "Joined Group"}",
                            bio = g.description.ifBlank { "Connecto Group Chat • $count members" },
                            lastMessage = gLastMsg,
                            timeAgo = gTime,
                            unreadCount = 0,
                            isOnline = false,
                            avatarUrl = g.iconUrl,
                            isGroup = true,
                            isCreatedByMe = isCreated,
                            memberCount = count,
                            members = g.members,
                            admin = g.admin,
                            creator = g.creator,
                            lastMessageTimestamp = gTs
                        )
                    }
                    val gIds = mappedGroups.map { it.id }.toSet()
                    val removed = userGroupsList.filter { it.id !in gIds && it.id != "grp-design-team-joined" }
                    userGroupsList.removeAll { it.id !in gIds && it.id != "grp-design-team-joined" }
                    for (item in mappedGroups) {
                        val idx = userGroupsList.indexOfFirst { it.id == item.id }
                        if (idx == -1) {
                            userGroupsList.add(item)
                        } else {
                            val existingTs = userGroupsList[idx].lastMessageTimestamp
                            val effectiveTs = maxOf(existingTs, item.lastMessageTimestamp)
                            userGroupsList[idx] = item.copy(lastMessageTimestamp = effectiveTs)
                        }
                    }
                    if (userGroupsList.none { !it.isCreatedByMe }) {
                        userGroupsList.add(
                            FriendItem(
                                id = "grp-design-team-joined",
                                name = "Design Team",
                                handle = "#design-team",
                                initial = "D",
                                status = "4 members • Joined Group",
                                bio = "Design Team Discussions • Admin: @devi",
                                lastMessage = "Hey team, welcome to the design channel!",
                                timeAgo = "1h ago",
                                unreadCount = 0,
                                isOnline = false,
                                avatarUrl = null,
                                isGroup = true,
                                isCreatedByMe = false,
                                memberCount = 4,
                                members = listOf("devi", "srinu", "chinnu14754x", "test1"),
                                admin = "devi",
                                creator = "devi",
                                lastMessageTimestamp = System.currentTimeMillis() - 3600000L
                            )
                        )
                    }
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            for (rem in removed) {
                                dbHelper.deleteCachedGroup(rem.id)
                            }
                            dbHelper.saveGroups(userGroupsList.toList())
                        } catch (_: Exception) {}
                    }
                    if (selectedFriend != null && selectedFriend!!.isGroup && userGroupsList.none { it.id == selectedFriend!!.id }) {
                        selectedFriend = null
                        chatWallpaperId = "default"
                        customWallpaperUri = null
                    }
                }
            } catch (_: Exception) {}
        } catch (e: Exception) {
            isFriendsLoadedOnce = true
            // Ignore transient network errors
        }
    }

    val screenCtx = LocalContext.current

    // Real-time friend requests, accepted friends sync listener, and group removal listener
    DisposableEffect(screenCtx) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    "com.example.connecto.ACTION_GROUP_REMOVED" -> {
                        val gid = intent.getStringExtra("group_id") ?: return
                        userGroupsList.removeAll { it.id == gid }
                        if (selectedFriend?.id == gid) {
                            selectedFriend = null
                            chatWallpaperId = "default"
                            customWallpaperUri = null
                            showGroupProfileDialog = false
                        }
                    }
                    else -> {
                        coroutineScope.launch {
                            syncAcceptedFriends()
                        }
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction("com.connecto.app.FRIEND_REQUEST_UPDATED")
            addAction("com.example.connecto.ACTION_GROUP_REMOVED")
        }
        try {
            ContextCompat.registerReceiver(
                screenCtx,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {}
        onDispose {
            VoiceCallManager.activeChatRoomOrFriend = null
            try {
                screenCtx.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    // Dynamic friend requests & accepted friends sync loop from backend + pre-load DM channel cache
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                ConnectoApiClient.channelCache.putAll(dbHelper.getAllDmChannels())
            } catch (_: Exception) {}
        }
        while (true) {
            syncAcceptedFriends()
            delay(3500)
        }
    }

    // Immediate sync on WebSocket connection / reconnection
    LaunchedEffect(Unit) {
        VoiceCallManager.isWsConnectedFlow.collect { connected ->
            if (connected) {
                syncAcceptedFriends()
            }
        }
    }

    val filteredFriends by remember {
        derivedStateOf {
            val baseList: List<FriendItem> = when (messageTabFilter) {
                "DIRECT" -> friendsList.toList()
                "CREATED_GROUPS" -> userGroupsList.filter { it.isCreatedByMe }
                "ADDED_GROUPS" -> userGroupsList.filter { !it.isCreatedByMe }
                else -> friendsList.toList() + userGroupsList.toList()
            }
            val sortedList = baseList.sortedWith(
                compareByDescending<FriendItem> { it.lastMessageTimestamp }
                    .thenBy { it.name.lowercase() }
            )
            if (friendSearchQuery.isBlank()) sortedList
            else sortedList.filter {
                it.name.contains(friendSearchQuery, ignoreCase = true) || it.handle.contains(friendSearchQuery, ignoreCase = true)
            }
        }
    }



    // Message Context Action States (Pin, Edit, Delete via Contextual Header 3-Dots Menu)
    var selectedMessageForAction by remember { mutableStateOf<CustomMessageItem?>(null) }
    var showMoreOptionsMenu by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<CustomMessageItem?>(null) }

    // Real-Time Message Edit Observer via WebSocket
    LaunchedEffect(Unit) {
        VoiceCallManager.editedMessages.collect { editEv ->
            val idx = friendMessages.indexOfFirst { it.id == editEv.messageId }
            if (idx != -1) {
                friendMessages = friendMessages.toMutableList().also { list ->
                    list[idx] = list[idx].copy(content = editEv.newContent)
                }
            }
        }
    }

    // Intercept back button when message is selected
    BackHandler(enabled = selectedMessageForAction != null) {
        selectedMessageForAction = null
        showMoreOptionsMenu = false
    }

    // Real-Time Typing Indicator Observer for Active Direct Chat / Group
    LaunchedEffect(selectedFriend?.id, selectedFriend?.handle, activeDmChannelId) {
        val targetHandle = selectedFriend?.handle?.removePrefix("@")?.lowercase()
        val targetId = selectedFriend?.id?.lowercase()
        val targetName = selectedFriend?.name?.lowercase()
        val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
        isFriendTyping = false
        VoiceCallManager.typingEvents.collect { ev ->
            val evUser = ev.username.lowercase().removePrefix("@")
            val evCh = ev.channelId.lowercase().removePrefix("#")

            // Ignore own typing events
            if (evUser == myUser) return@collect

            val isTargetUser = (targetHandle != null && evUser == targetHandle) ||
                               (targetId != null && evUser == targetId) ||
                               (targetName != null && evUser == targetName)
            val isTargetChannel = (activeDmChannelId != null && (
                evCh == activeDmChannelId?.lowercase()?.removePrefix("#") ||
                evCh == "dm-${myUser}-${targetHandle ?: targetName ?: targetId}" ||
                evCh == "dm-${targetHandle ?: targetName ?: targetId}-${myUser}"
            ))
            if (isTargetUser || isTargetChannel) {
                isFriendTyping = ev.isTyping
            }
        }
    }

    LaunchedEffect(isFriendTyping) {
        if (isFriendTyping) {
            coroutineScope.launch {
                try {
                    listState.animateScrollToItem(friendMessages.size)
                } catch (_: Exception) {}
            }
            kotlinx.coroutines.delay(4000)
            isFriendTyping = false
        }
    }

    // Broadcast current user's typing activity in DM, suppressed if Stealth Mode is ON
    LaunchedEffect(typedMessage, selectedFriend?.id, activeDmChannelId) {
        val recipientUser = selectedFriend?.handle?.removePrefix("@") ?: selectedFriend?.name ?: selectedFriend?.id
        val target = activeDmChannelId ?: recipientUser
        if (!target.isNullOrBlank()) {
            val isStealth = ConnectoApiClient.isStealthMode || context.getSharedPreferences("connecto_profile", Context.MODE_PRIVATE).getBoolean("stealth_mode_${currentUsername}", false)
            if (!isStealth) {
                if (typedMessage.isNotBlank()) {
                    VoiceCallManager.sendTypingStart(target, recipientUser)
                    kotlinx.coroutines.delay(2500)
                    VoiceCallManager.sendTypingStop(target, recipientUser)
                } else {
                    VoiceCallManager.sendTypingStop(target, recipientUser)
                }
            }
        }
    }

    // Handle System Back button when in friend profile or 1:1 direct chat
    BackHandler(enabled = selectedFriend != null || isFriendProfileOpen) {
        if (friendProfileSubPage != null) {
            friendProfileSubPage = null
        } else if (isFriendProfileOpen) {
            isFriendProfileOpen = false
            if (openedProfileFromChatList) {
                openedProfileFromChatList = false
                selectedFriend = null
                onDirectChatStateChanged(false)
            }
        } else {
            selectedFriend?.let { friend ->
                markFriendChatAsReadLocallyAndRemotely(friend)
                if (typedMessage.isNotBlank()) {
                    messageDrafts[friend.id] = typedMessage
                } else {
                    messageDrafts.remove(friend.id)
                }
            }
            typedMessage = ""
            selectedFriend = null
            friendMessages = emptyList()
            chatWallpaperId = "default"
            customWallpaperUri = null
            activeDmChannelId = null
            pinnedMessageForChat = null
            messageForAction = null
            selectedMessageForAction = null
            showMoreOptionsMenu = false
            editingMessage = null
            isLoadingMessages = false
            VoiceCallManager.activeChatRoomOrFriend = null
            onDirectChatStateChanged(false)
        }
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
                    // If recipient just came online (mobile data turned on), upgrade pending single ticks to double ticks
                    if (isOnline) {
                        friendMessages = friendMessages.map { msg ->
                            if (msg.isMe && !msg.isDelivered) {
                                msg.copy(isDelivered = true)
                            } else {
                                msg
                            }
                        }
                    }
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

    // Real-time WebSocket Read Receipts Listener (updates outgoing message read state when friend reads chat)
    LaunchedEffect(Unit) {
        VoiceCallManager.readReceiptEvents.collect { receipt ->
            val cleanReader = receipt.readerUsername.trim().lowercase().removePrefix("@")
            val currentSelected = selectedFriend
            if (currentSelected != null) {
                val currentHandle = currentSelected.handle.lowercase().removePrefix("@")
                val currentId = currentSelected.id.lowercase()
                val currentName = currentSelected.name.lowercase()
                val myU = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
                val matchesReader = cleanReader.isBlank() || cleanReader == currentHandle || cleanReader == currentId || cleanReader == currentName
                val currentCanonical = "dm-${minOf(myU, currentHandle)}-${maxOf(myU, currentHandle)}"
                val matchesChannel = receipt.channelId.isBlank() || 
                    receipt.channelId.equals(activeDmChannelId, ignoreCase = true) || 
                    receipt.channelId.equals(currentCanonical, ignoreCase = true)
                if (matchesReader || matchesChannel) {
                    friendMessages = friendMessages.map { msg ->
                        if (msg.isMe && (!msg.isDelivered || !msg.isRead)) {
                            msg.copy(isDelivered = true, isRead = true)
                        } else {
                            msg
                        }
                    }
                }
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
            val friendHandleClean = currentFriend?.handle?.removePrefix("@")?.trim()?.lowercase() ?: ""
            val friendIdClean = currentFriend?.id?.trim()?.lowercase() ?: ""
            val friendNameClean = currentFriend?.name?.trim()?.lowercase() ?: ""

            val currentDmId = activeDmChannelId?.trim()?.lowercase()
            val clean = friendHandleClean
            val me = myUsername
            val currentCanonical = if (currentFriend != null && !currentFriend.isGroup) "dm-${minOf(me, clean)}-${maxOf(me, clean)}" else null
            val cachedDmId = if (currentCanonical != null) ConnectoApiClient.channelCache[currentCanonical.lowercase()]?.trim()?.lowercase() else null

            val dtoChannelClean = (dto.channelId ?: "").trim().lowercase()

            val isForCurrentChat = if (currentFriend == null) {
                false
            } else if (currentFriend.isGroup) {
                dtoChannelClean == currentFriend.id.trim().lowercase() || (currentDmId != null && dtoChannelClean == currentDmId)
            } else {
                val isGroupChannel = dtoChannelClean.startsWith("grp-") || dtoChannelClean.startsWith("grp_") || dtoChannelClean.startsWith("group-")
                val isPublicChannel = dtoChannelClean in listOf("general", "announcements", "random", "voice")
                if (isGroupChannel || isPublicChannel) {
                    false
                } else {
                    val myBase = myUsername.replace(Regex("\\d+$"), "")
                    val friendBase = friendHandleClean.replace(Regex("\\d+$"), "")
                    val channelMatches = (
                        (currentDmId != null && dtoChannelClean == currentDmId) ||
                        (cachedDmId != null && dtoChannelClean == cachedDmId) ||
                        (currentCanonical != null && dtoChannelClean == currentCanonical) ||
                        (currentCanonical != null && ConnectoApiClient.channelCache[dtoChannelClean]?.lowercase() == currentCanonical) ||
                        (currentDmId != null && ConnectoApiClient.channelCache[dtoChannelClean]?.lowercase() == currentDmId) ||
                        ((dtoChannelClean.startsWith("dm-") || dtoChannelClean.startsWith("dm_")) &&
                            (dtoChannelClean.contains(myUsername) || (myBase.isNotEmpty() && dtoChannelClean.contains(myBase))) &&
                            (dtoChannelClean.contains(friendHandleClean) || (friendBase.isNotEmpty() && dtoChannelClean.contains(friendBase)))
                        )
                    )
                    val isFriend = (
                        (friendHandleClean.isNotBlank() && (authorClean == friendHandleClean || authorNameClean == friendHandleClean)) ||
                        (friendIdClean.isNotBlank() && (authorClean == friendIdClean || authorNameClean == friendIdClean)) ||
                        (friendNameClean.isNotBlank() && (authorNameClean == friendNameClean || authorClean == friendNameClean))
                    )
                    val directRecipientMatches = (
                        (dtoChannelClean == friendHandleClean || dtoChannelClean == "@$friendHandleClean" || dtoChannelClean == friendIdClean) &&
                        (isMe || isFriend)
                    )
                    (channelMatches && (isMe || isFriend)) || directRecipientMatches
                }
            }

            if (isForCurrentChat && currentFriend != null) {
                isLoadingMessages = false
                val isGrpCh = dto.channelId.orEmpty().startsWith("grp-") || dto.channelId.orEmpty().startsWith("grp_") || dto.channelId.orEmpty().startsWith("group-")
                if (activeDmChannelId == null && !dto.channelId.isNullOrBlank() && !dto.channelId.startsWith("dm-") && !dto.channelId.startsWith("dm_") && !isGrpCh) {
                    activeDmChannelId = dto.channelId
                    if (currentCanonical != null) {
                        ConnectoApiClient.channelCache[currentCanonical.lowercase()] = dto.channelId
                        coroutineScope.launch(Dispatchers.IO) {
                            try { dbHelper.saveDmChannel(currentCanonical, dto.channelId) } catch (_: Exception) {}
                        }
                    }
                }
                val init = if (dto.authorName.isNotBlank()) dto.authorName.first().toString().uppercase() else "U"
                val msgAvatar = if (isMe) {
                    ConnectoApiClient.currentUserAvatarUrl
                } else {
                    dto.authorAvatar ?: currentFriend?.avatarUrl
                }
                val (decryptedContent, decryptedReplyContent, isEnc) = if (currentFriend.isGroup) {
                    Triple(dto.content, dto.replyToContent, false)
                } else {
                    val otherUser = currentFriend.handle.removePrefix("@")
                    val e2eSecret = ConnectoApiClient.getE2ESecret()
                    val conversationKey = ConnectoE2EEncryption.deriveConversationKey(myUsername, otherUser, e2eSecret)
                    val fallbackConversationKey = ConnectoE2EEncryption.deriveConversationKey(myUsername, otherUser, null)
                    val enc = ConnectoE2EEncryption.isEncrypted(dto.content)
                    val decMsg = if (enc) ConnectoE2EEncryption.decryptMessage(dto.content, conversationKey, fallbackConversationKey) else dto.content
                    val decReply = if (dto.replyToContent != null && ConnectoE2EEncryption.isEncrypted(dto.replyToContent)) {
                        ConnectoE2EEncryption.decryptMessage(dto.replyToContent, conversationKey, fallbackConversationKey)
                    } else dto.replyToContent
                    Triple(decMsg, decReply, enc)
                }
                val isRecipientOnline = currentFriend.isOnline
                val newItem = CustomMessageItem(
                    id = dto.id,
                    senderName = dto.authorName,
                    initial = init,
                    content = decryptedContent,
                    time = formatIsoTime(dto.createdAt),
                    isMe = isMe,
                    isDelivered = if (isMe) isRecipientOnline else true,
                    isRead = if (isMe) false else true,
                    avatarUrl = msgAvatar,
                    replyToId = dto.replyToId,
                    replyToContent = decryptedReplyContent,
                    replyToAuthor = dto.replyToAuthor,
                    isEncrypted = isEnc,
                    timestampMillis = parseMessageTimestamp(dto.createdAt)
                )

                // If friend sent a message, they have received and read our prior sent messages
                val baseList = if (!isMe) {
                    friendMessages.map { oldMsg ->
                        if (oldMsg.isMe && (!oldMsg.isDelivered || !oldMsg.isRead)) {
                            oldMsg.copy(isDelivered = true, isRead = true)
                        } else oldMsg
                    }
                } else friendMessages

                // Reconcile and deduplicate:
                val resultList = (baseList + newItem)
                friendMessages = deduplicateCustomMessages(resultList)
                coroutineScope.launch {
                    try { listState.animateScrollToItem(friendMessages.size) } catch (_: Exception) {}
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
                val currentTs = System.currentTimeMillis()
                val fIdx = friendsList.indexOfFirst {
                    it.id == currentFriend!!.id || it.handle.removePrefix("@").equals(currentFriend!!.handle.removePrefix("@"), ignoreCase = true)
                }
                if (fIdx != -1) {
                    val target = friendsList[fIdx]
                    val cleanHandle = target.handle.removePrefix("@").trim().lowercase()
                    val updated = target.copy(
                        lastMessage = dto.content,
                        timeAgo = formatIsoTime(dto.createdAt),
                        unreadCount = 0,
                        lastMessageTimestamp = currentTs
                    )
                    friendsList.removeAt(fIdx)
                    friendsList.add(0, updated)
                    readPrefs.edit()
                        .putString("last_msg_${updated.id}", dto.content)
                        .putString("last_msg_${cleanHandle}", dto.content)
                        .putString("last_time_${updated.id}", formatIsoTime(dto.createdAt))
                        .putString("last_time_${cleanHandle}", formatIsoTime(dto.createdAt))
                        .putLong("last_timestamp_${updated.id}", currentTs)
                        .putLong("last_timestamp_${cleanHandle}", currentTs)
                        .apply()
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            dbHelper.updateFriendLastMessageAndUnread(updated.id, dto.content, formatIsoTime(dto.createdAt), 0, currentTs)
                        } catch (_: Exception) {}
                    }
                    markFriendChatAsReadLocallyAndRemotely(updated)
                } else {
                    val gIdx = userGroupsList.indexOfFirst { it.id == currentFriend!!.id }
                    if (gIdx != -1) {
                        val updated = userGroupsList[gIdx].copy(
                            lastMessage = dto.content,
                            timeAgo = formatIsoTime(dto.createdAt),
                            unreadCount = 0,
                            lastMessageTimestamp = currentTs
                        )
                        userGroupsList.removeAt(gIdx)
                        userGroupsList.add(0, updated)
                        readPrefs.edit()
                            .putString("last_msg_${updated.id}", dto.content)
                            .putString("last_time_${updated.id}", formatIsoTime(dto.createdAt))
                            .putLong("last_timestamp_${updated.id}", currentTs)
                            .apply()
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                dbHelper.updateGroupLastMessage(updated.id, dto.content, formatIsoTime(dto.createdAt), currentTs)
                            } catch (_: Exception) {}
                        }
                    }
                }
            } else if (!isMe) {
                // Incoming message for another friend or when user is on the Messages list / conversations tab!
                val currentTs = System.currentTimeMillis()
                val dtoChannelClean = (dto.channelId ?: "").trim().lowercase()

                val decContent = try {
                    if (ConnectoE2EEncryption.isEncrypted(dto.content)) {
                        val salt = ConnectoApiClient.getE2ESecret()
                        val senderKey = ConnectoE2EEncryption.deriveConversationKey(myUsername, authorClean, salt)
                        val fallbackKey = ConnectoE2EEncryption.deriveConversationKey(myUsername, authorClean, null)
                        ConnectoE2EEncryption.decryptMessage(dto.content, senderKey, fallbackKey)
                    } else dto.content
                } catch (_: Exception) { dto.content }

                val fIdx = friendsList.indexOfFirst {
                    val h = it.handle.removePrefix("@").trim().lowercase()
                    val id = it.id.trim().lowercase()
                    val n = it.name.trim().lowercase()
                    val canonDash = "dm-${minOf(myUsername, h)}-${maxOf(myUsername, h)}"
                    val canonUnder = "dm_${minOf(myUsername, h)}_${maxOf(myUsername, h)}"
                    h == authorClean || id == authorClean || n == authorNameClean ||
                    dtoChannelClean == canonDash || dtoChannelClean == canonUnder
                }

                if (fIdx != -1) {
                    val targetFriend = friendsList[fIdx]
                    val cleanHandle = targetFriend.handle.removePrefix("@").trim().lowercase()
                    val newUnread = targetFriend.unreadCount + 1
                    val updated = targetFriend.copy(
                        lastMessage = decContent,
                        timeAgo = formatIsoTime(dto.createdAt),
                        unreadCount = newUnread,
                        lastMessageTimestamp = currentTs
                    )
                    friendsList.removeAt(fIdx)
                    friendsList.add(0, updated)
                    readPrefs.edit()
                        .putString("last_msg_${targetFriend.id}", decContent)
                        .putString("last_msg_${cleanHandle}", decContent)
                        .putString("last_time_${targetFriend.id}", formatIsoTime(dto.createdAt))
                        .putString("last_time_${cleanHandle}", formatIsoTime(dto.createdAt))
                        .putInt("unread_${targetFriend.id}", newUnread)
                        .putInt("unread_${cleanHandle}", newUnread)
                        .putLong("last_timestamp_${targetFriend.id}", currentTs)
                        .putLong("last_timestamp_${cleanHandle}", currentTs)
                        .apply()
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            dbHelper.updateFriendLastMessageAndUnread(targetFriend.id, decContent, formatIsoTime(dto.createdAt), newUnread, currentTs)
                        } catch (_: Exception) {}
                    }
                    onUnreadStatusChanged(true)
                } else {
                    val gIdx = userGroupsList.indexOfFirst {
                        it.id.equals(dtoChannelClean, ignoreCase = true) || it.name.trim().lowercase() == dtoChannelClean
                    }
                    if (gIdx != -1) {
                        val targetGroup = userGroupsList[gIdx]
                        val newUnread = targetGroup.unreadCount + 1
                        val updated = targetGroup.copy(
                            lastMessage = decContent,
                            timeAgo = formatIsoTime(dto.createdAt),
                            unreadCount = newUnread,
                            lastMessageTimestamp = currentTs
                        )
                        userGroupsList.removeAt(gIdx)
                        userGroupsList.add(0, updated)
                        readPrefs.edit()
                            .putString("last_msg_${targetGroup.id}", decContent)
                            .putString("last_time_${targetGroup.id}", formatIsoTime(dto.createdAt))
                            .putInt("unread_${targetGroup.id}", newUnread)
                            .putLong("last_timestamp_${targetGroup.id}", currentTs)
                            .apply()
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                dbHelper.updateGroupLastMessage(targetGroup.id, decContent, formatIsoTime(dto.createdAt), currentTs)
                            } catch (_: Exception) {}
                        }
                        onUnreadStatusChanged(true)
                    } else {
                        // Friend not yet in memory list — check local SQLite database
                        coroutineScope.launch(Dispatchers.IO) {
                            val cachedFriend = dbHelper.getFriendByUsernameOrHandle(authorClean)
                            if (cachedFriend != null) {
                                val cleanH = cachedFriend.handle.removePrefix("@").trim().lowercase()
                                val newUnread = cachedFriend.unreadCount + 1
                                val updated = cachedFriend.copy(
                                    lastMessage = decContent,
                                    timeAgo = formatIsoTime(dto.createdAt),
                                    unreadCount = newUnread,
                                    lastMessageTimestamp = currentTs
                                )
                                readPrefs.edit()
                                    .putString("last_msg_${cachedFriend.id}", decContent)
                                    .putString("last_msg_${cleanH}", decContent)
                                    .putString("last_time_${cachedFriend.id}", formatIsoTime(dto.createdAt))
                                    .putString("last_time_${cleanH}", formatIsoTime(dto.createdAt))
                                    .putInt("unread_${cachedFriend.id}", newUnread)
                                    .putInt("unread_${cleanH}", newUnread)
                                    .putLong("last_timestamp_${cachedFriend.id}", currentTs)
                                    .putLong("last_timestamp_${cleanH}", currentTs)
                                    .apply()
                                try {
                                    dbHelper.updateFriendLastMessageAndUnread(cachedFriend.id, decContent, formatIsoTime(dto.createdAt), newUnread, currentTs)
                                } catch (_: Exception) {}
                                withContext(Dispatchers.Main) {
                                    val existingIdx = friendsList.indexOfFirst { it.id == updated.id }
                                    if (existingIdx != -1) {
                                        friendsList.removeAt(existingIdx)
                                    }
                                    friendsList.add(0, updated)
                                    onUnreadStatusChanged(true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Real-time WebSocket Deleted Messages Listener
    LaunchedEffect(Unit) {
        VoiceCallManager.deletedMessageIds.collect { delId ->
            friendMessages = friendMessages.filterNot { it.id == delId }
        }
    }

    var lastScrolledFriendId by remember { mutableStateOf<String?>(null) }
    var hasServerScrolledForFriend by remember { mutableStateOf(false) }

    // DM Channel Setup and Live Polling Loop
    LaunchedEffect(selectedFriend?.id) {
        val friend = selectedFriend
        onDirectChatStateChanged(friend != null)
        VoiceCallManager.activeChatRoomOrFriend = friend?.handle?.removePrefix("@")?.lowercase()

        if (friend != null) {
            val friendId = friend.id
            typedMessage = messageDrafts[friend.id] ?: ""

            markFriendChatAsReadLocallyAndRemotely(friend)

            val cleanTarget = friend.handle.removePrefix("@")
            val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
            val canonicalName = if (friend.isGroup) friend.id else "dm-${minOf(myUser, cleanTarget.lowercase())}-${maxOf(myUser, cleanTarget.lowercase())}"

            val storedDmId = if (!friend.isGroup) dbHelper.getDmChannelId(canonicalName) else null
            activeDmChannelId = if (friend.isGroup) {
                friend.id
            } else {
                if (storedDmId != null && !storedDmId.startsWith("grp-") && !storedDmId.startsWith("grp_") && !storedDmId.startsWith("group-")) {
                    storedDmId
                } else null
            }

            // Always reload messages for this friend from local cache to prevent displaying messages from previous chats
            val resolvedCached = withContext(Dispatchers.IO) {
                loadLocalMessagesForFriend(friend)
            }
            friendMessages = resolvedCached
            isLoadingMessages = false
            if (resolvedCached.isNotEmpty()) {
                try { listState.scrollToItem(resolvedCached.size) } catch (_: Exception) {}
            }

            if (storedDmId != null && !friend.isGroup && !storedDmId.startsWith("grp-") && !storedDmId.startsWith("grp_") && !storedDmId.startsWith("group-")) {
                ConnectoApiClient.channelCache[canonicalName.lowercase()] = storedDmId
                ConnectoApiClient.channelCache[storedDmId.lowercase()] = canonicalName.lowercase()
                VoiceCallManager.subscribeChannel(storedDmId)
            }

            // Fetch pinned message for this conversation (isolated by canonical channel or group ID)
            val pinChannelId = if (friend.isGroup) friend.id else canonicalName
            val pinKey = if (friend.isGroup) friend.id else canonicalName.lowercase()
            val cachedPinId = friendPrefs.getString("pinned_msg_${pinKey}", null)
            val cachedPinContent = friendPrefs.getString("pinned_content_${pinKey}", null)
            val cachedPinAuthor = friendPrefs.getString("pinned_author_${pinKey}", null)
            if (!cachedPinId.isNullOrBlank() && !cachedPinContent.isNullOrBlank()) {
                pinnedMessageForChat = PinnedMessageDto(
                    messageId = cachedPinId,
                    channelId = pinChannelId,
                    authorName = cachedPinAuthor ?: "Pinned",
                    content = cachedPinContent,
                    pinnedBy = "",
                    pinnedAt = ""
                )
            } else {
                pinnedMessageForChat = null
            }
            coroutineScope.launch {
                val serverPinRes = ConnectoApiClient.getPinnedMessage(pinChannelId)
                if (serverPinRes.isSuccess) {
                    val serverPin = serverPinRes.getOrNull()
                    if (serverPin != null) {
                        pinnedMessageForChat = serverPin
                        friendPrefs.edit()
                            .putString("pinned_msg_${pinKey}", serverPin.messageId)
                            .putString("pinned_content_${pinKey}", serverPin.content)
                            .putString("pinned_author_${pinKey}", serverPin.authorName)
                            .apply()
                    } else {
                        // Authoritative: server confirmed no pinned message exists for this channel
                        pinnedMessageForChat = null
                        friendPrefs.edit()
                            .remove("pinned_msg_${pinKey}")
                            .remove("pinned_content_${pinKey}")
                            .remove("pinned_author_${pinKey}")
                            .apply()
                    }
                }
            }

            // Immediately subscribe to channel over WebSocket
            VoiceCallManager.subscribeChannel(if (friend.isGroup) friend.id else canonicalName)

            if (!friend.isGroup) {
                // Resolve DM channel on backend and subscribe in background
                coroutineScope.launch {
                    try {
                        val startRes = ConnectoApiClient.startDM(cleanTarget)
                        if (startRes.isSuccess) {
                            val ch = startRes.getOrThrow()
                            val cleanChId = ch.id.lowercase().trim()
                            if (!cleanChId.startsWith("grp-") && !cleanChId.startsWith("grp_") && !cleanChId.startsWith("group-")) {
                                activeDmChannelId = ch.id
                                ConnectoApiClient.channelCache[canonicalName.lowercase()] = ch.id
                                ConnectoApiClient.channelCache[ch.id.lowercase()] = canonicalName.lowercase()
                                VoiceCallManager.subscribeChannel(ch.id)
                                VoiceCallManager.subscribeChannel(ch.name)
                                withContext(Dispatchers.IO) {
                                    dbHelper.saveDmChannel(canonicalName, ch.id)
                                }
                            }
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
                val currentOpeningFriendId = friend.id
                val cleanOpeningTarget = cleanTarget
                val cleanLower = cleanOpeningTarget.lowercase()
                coroutineScope.launch {
                    try {
                        val profRes = ConnectoApiClient.getProfile(cleanOpeningTarget)
                        if (profRes.isSuccess) {
                            val prof = profRes.getOrThrow()
                            friendProfilesCache[cleanLower] = prof
                            val freshAvatar = prof.avatarUrl
                            val freshName = prof.displayName.ifBlank { prof.username }
                            val freshBio = prof.bio ?: ""
                            val freshOnline = prof.isOnline

                            // STRICT GUARD: Only mutate selectedFriend if the user is STILL viewing this exact same friend!
                            val activeClean = selectedFriend?.handle?.removePrefix("@")?.trim()?.lowercase()
                            if (activeClean == cleanLower || selectedFriend?.id == currentOpeningFriendId) {
                                selectedFriend = selectedFriend?.copy(
                                    avatarUrl = freshAvatar ?: selectedFriend?.avatarUrl,
                                    name = if (freshName.isNotBlank()) freshName else (selectedFriend?.name ?: cleanOpeningTarget),
                                    bio = freshBio,
                                    isOnline = freshOnline,
                                    status = if (freshOnline) "Accepted Friend • Online" else "Accepted Friend • Offline"
                                )
                                if (selectedFriendProfileData == null || selectedFriendProfileData?.username?.lowercase() == cleanLower) {
                                    selectedFriendProfileData = prof
                                }
                            }

                            val fIdx = friendsList.indexOfFirst { it.id == currentOpeningFriendId }
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
            }

            // Continuous live polling every 1.5s
            while (true) {
                try {
                    val dmId = if (friend.isGroup) {
                        friend.id
                    } else {
                        val active = activeDmChannelId
                        val cached = ConnectoApiClient.channelCache[canonicalName.lowercase()]
                        if (active != null && !active.startsWith("grp-") && !active.startsWith("grp_") && !active.startsWith("group-")) {
                            active
                        } else if (cached != null && !cached.startsWith("grp-") && !cached.startsWith("grp_") && !cached.startsWith("group-")) {
                            cached
                        } else {
                            canonicalName
                        }
                    }

                    if (dmId.isNotBlank()) {
                        val msgRes = if (friend.isGroup) {
                            ConnectoApiClient.getMessages(channelId = friend.id)
                        } else {
                            ConnectoApiClient.getMessages(channelId = dmId, friendUsername = cleanTarget)
                        }
                        if (msgRes.isSuccess) {
                            val dtoList = msgRes.getOrThrow()
                            val unexpiredList = dtoList.distinctBy { it.id }
                            isLoadingMessages = false
                            if (unexpiredList.isNotEmpty()) {
                                // Learn channel UUID if returned in message DTOs
                                val serverChId = unexpiredList.firstOrNull {
                                    !it.channelId.isNullOrBlank() && !it.channelId.startsWith("dm-") && !it.channelId.startsWith("dm_") &&
                                    !it.channelId.startsWith("grp-") && !it.channelId.startsWith("grp_") && !it.channelId.startsWith("group-")
                                }?.channelId
                                if (serverChId != null && !friend.isGroup) {
                                    if (activeDmChannelId == null) activeDmChannelId = serverChId
                                    ConnectoApiClient.channelCache[canonicalName.lowercase()] = serverChId
                                    ConnectoApiClient.channelCache[serverChId.lowercase()] = canonicalName.lowercase()
                                    withContext(Dispatchers.IO) {
                                        dbHelper.saveDmChannel(canonicalName, serverChId)
                                    }
                                }

                                withContext(Dispatchers.IO) {
                                    val targetSaveId = if (friend.isGroup) friend.id else canonicalName
                                    dbHelper.saveMessages(targetSaveId, unexpiredList)
                                    if (!friend.isGroup && dmId != canonicalName && !dmId.startsWith("grp-") && !dmId.startsWith("grp_") && !dmId.startsWith("group-")) {
                                        dbHelper.saveMessages(dmId, unexpiredList)
                                    }
                                    if (!friend.isGroup && serverChId != null && serverChId != dmId && serverChId != canonicalName && !serverChId.startsWith("grp-") && !serverChId.startsWith("grp_") && !serverChId.startsWith("group-")) {
                                        dbHelper.saveMessages(serverChId, unexpiredList)
                                    }
                                }
                                val myUsername = ConnectoApiClient.currentUsername ?: currentUsername
                                val myDisplayName = ConnectoApiClient.currentUserDisplayName ?: ""
                                val myUserId = ConnectoApiClient.currentUserId
                                val existingReactions = friendMessages.associate { it.id to it.reaction }
                                val isRecipientOnline = friend.isOnline || (selectedFriend?.isOnline == true)
                                val mapped = unexpiredList.map { dto ->
                                    val isMe = (
                                        dto.authorId.equals(myUsername, ignoreCase = true) ||
                                        dto.authorName.equals(myUsername, ignoreCase = true) ||
                                        (myDisplayName.isNotBlank() && dto.authorName.equals(myDisplayName, ignoreCase = true)) ||
                                        (myUserId != null && (dto.authorId == myUserId || dto.authorName == myUserId)) ||
                                        dto.authorId.equals(currentUsername, ignoreCase = true) ||
                                        dto.authorName.equals(currentUsername, ignoreCase = true)
                                    )
                                    val effectiveSenderName = if (isMe) {
                                        if (myDisplayName.isNotBlank()) myDisplayName else myUsername
                                    } else {
                                        if (dto.authorName.isNotBlank() && !dto.authorName.equals("gamer", ignoreCase = true)) {
                                            dto.authorName
                                        } else {
                                            if (friend.isGroup) dto.authorName else (selectedFriend?.name ?: friend.name)
                                        }
                                    }
                                    val init = if (effectiveSenderName.isNotBlank()) effectiveSenderName.first().toString().uppercase() else "U"
                                    val msgAvatar = if (isMe) {
                                        ConnectoApiClient.currentUserAvatarUrl
                                    } else {
                                        dto.authorAvatar ?: (if (friend.isGroup) null else (selectedFriend?.avatarUrl ?: friend.avatarUrl))
                                    }
                                    
                                    val (decryptedContent, decryptedReplyContent, isEnc) = if (friend.isGroup) {
                                        Triple(dto.content, dto.replyToContent, false)
                                    } else {
                                        val otherUser = friend.handle.removePrefix("@")
                                        val e2eSecret = ConnectoApiClient.getE2ESecret()
                                        val conversationKey = ConnectoE2EEncryption.deriveConversationKey(myUsername, otherUser, e2eSecret)
                                        val fallbackConversationKey = ConnectoE2EEncryption.deriveConversationKey(myUsername, otherUser, null)
                                        val enc = ConnectoE2EEncryption.isEncrypted(dto.content)
                                        val decMsg = if (enc) ConnectoE2EEncryption.decryptMessage(dto.content, conversationKey, fallbackConversationKey) else dto.content
                                        val decReply = if (dto.replyToContent != null && ConnectoE2EEncryption.isEncrypted(dto.replyToContent)) {
                                            ConnectoE2EEncryption.decryptMessage(dto.replyToContent, conversationKey, fallbackConversationKey)
                                        } else dto.replyToContent
                                        Triple(decMsg, decReply, enc)
                                    }

                                    val msgMillis = parseMessageTimestamp(dto.createdAt)
                                    val isReadValue = if (isMe) (dto.isRead ?: false) else true
                                    CustomMessageItem(
                                        id = dto.id,
                                        senderName = effectiveSenderName,
                                        initial = init,
                                        content = decryptedContent,
                                        time = formatIsoTime(dto.createdAt),
                                        isMe = isMe,
                                        isDelivered = if (isMe) (if (friend.isGroup) true else isRecipientOnline) else true,
                                        isRead = isReadValue,
                                        reaction = existingReactions[dto.id],
                                        avatarUrl = msgAvatar,
                                        replyToId = dto.replyToId,
                                        replyToContent = decryptedReplyContent,
                                        replyToAuthor = dto.replyToAuthor,
                                        isEncrypted = isEnc,
                                        timestampMillis = msgMillis
                                    )
                                }

                                val lastFriendMsgTime = mapped.filter { !it.isMe }.maxOfOrNull { it.timestampMillis } ?: 0L
                                val mappedResolved = mapped.map { msg ->
                                    if (msg.isMe) {
                                        if (msg.isRead || (lastFriendMsgTime > 0 && msg.timestampMillis <= lastFriendMsgTime)) {
                                            msg.copy(isDelivered = true, isRead = true)
                                        } else {
                                            msg.copy(isDelivered = if (friend.isGroup) true else isRecipientOnline, isRead = false)
                                        }
                                    } else msg
                                }

                                // Merge with any in-flight or pending local messages that have not yet reached the server
                                val mappedIds = mappedResolved.map { it.id }.toSet()
                                val unconfirmedLocal = friendMessages.filter { localMsg ->
                                    if (mappedIds.contains(localMsg.id)) {
                                        false // Already confirmed in server list
                                    } else {
                                        // Keep local message only if server poll did not return any message matching this content and author
                                        mappedResolved.none { m ->
                                            m.isMe == localMsg.isMe &&
                                            m.content.trim() == localMsg.content.trim() &&
                                            (kotlin.math.abs(m.timestampMillis - localMsg.timestampMillis) < 30_000L || m.time == localMsg.time)
                                        }
                                    }
                                }
                                val delPrefsKey = "deleted_for_me_${myUser.lowercase()}"
                                val deletedSet = friendPrefs.getStringSet(delPrefsKey, emptySet()) ?: emptySet()
                                val merged = deduplicateCustomMessages(
                                    (mappedResolved + unconfirmedLocal).filter { it.id !in deletedSet }
                                )
                                if (friendMessages != merged) {
                                    val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                    val isAtBottom = lastVisible >= (friendMessages.size - 2).coerceAtLeast(0)
                                    val hasNewItems = merged.size > friendMessages.size
                                    friendMessages = merged
                                    if (isAtBottom && hasNewItems) {
                                        coroutineScope.launch {
                                            try { listState.scrollToItem(merged.size) } catch (_: Exception) {}
                                        }
                                    }
                                }
                                hasServerScrolledForFriend = true

                                // Update last message in friends list and persist
                                if (merged.isNotEmpty()) {
                                    val last = merged.last()
                                    val fIdx = friendsList.indexOfFirst { it.id == friend.id || it.handle.removePrefix("@").equals(friend.handle.removePrefix("@"), ignoreCase = true) }
                                    if (fIdx != -1) {
                                        val updated = friendsList[fIdx].copy(
                                            lastMessage = last.content,
                                            timeAgo = last.time,
                                            lastMessageTimestamp = last.timestampMillis
                                        )
                                        friendsList[fIdx] = updated
                                        val cleanH = updated.handle.removePrefix("@").trim().lowercase()
                                        readPrefs.edit()
                                            .putString("last_msg_${updated.id}", last.content)
                                            .putString("last_msg_${cleanH}", last.content)
                                            .putString("last_time_${updated.id}", last.time)
                                            .putString("last_time_${cleanH}", last.time)
                                            .putLong("last_timestamp_${updated.id}", last.timestampMillis)
                                            .putLong("last_timestamp_${cleanH}", last.timestampMillis)
                                            .apply()
                                        coroutineScope.launch(Dispatchers.IO) {
                                            try {
                                                dbHelper.updateFriendLastMessageAndUnread(updated.id, last.content, last.time, updated.unreadCount, last.timestampMillis)
                                            } catch (_: Exception) {}
                                        }
                                    }
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

    // Auto-scroll on new messages, initial open, or input focus (ensures always landing on latest messages)
    LaunchedEffect(friendMessages.size, isInputFocused, selectedFriend?.id) {
        if (selectedFriend != null && friendMessages.isNotEmpty()) {
            val targetIdx = friendMessages.size
            if (lastScrolledFriendId != selectedFriend?.id) {
                lastScrolledFriendId = selectedFriend?.id
                hasServerScrolledForFriend = false
                try { listState.scrollToItem(targetIdx) } catch (_: Exception) {}
            } else if (!hasServerScrolledForFriend) {
                try { listState.scrollToItem(targetIdx) } catch (_: Exception) {}
            } else {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val isNearBottom = lastVisible >= (targetIdx - 3).coerceAtLeast(0)
                if (isNearBottom || isInputFocused) {
                    try { listState.animateScrollToItem(targetIdx) } catch (_: Exception) {}
                }
            }
        }
    }

    LaunchedEffect(selectedFriend?.id, selectedFriend?.handle, isFriendProfileOpen, showFriendProfileDialog) {
        val currentFriend = selectedFriend
        if ((isFriendProfileOpen || showFriendProfileDialog) && currentFriend != null) {
            val clean = currentFriend.handle.removePrefix("@").trim()
            val cleanLower = clean.lowercase()
            val cached = friendProfilesCache[cleanLower]
            if (cached != null) {
                selectedFriendProfileData = cached
            } else if (selectedFriendProfileData != null && !selectedFriendProfileData?.username.equals(cleanLower, ignoreCase = true)) {
                // Instantly purge stale profile from previous friend so friend1 data is NEVER shown for friend2
                selectedFriendProfileData = null
            }
            try {
                val res = ConnectoApiClient.getProfile(clean)
                if (res.isSuccess) {
                    val prof = res.getOrThrow()
                    friendProfilesCache[cleanLower] = prof
                    // Verify that the user is still viewing this exact friend
                    val activeClean = selectedFriend?.handle?.removePrefix("@")?.trim()?.lowercase()
                    if (activeClean == cleanLower || selectedFriend?.id == currentFriend.id) {
                        selectedFriendProfileData = prof
                        selectedFriend = selectedFriend?.copy(
                            avatarUrl = prof.avatarUrl ?: selectedFriend?.avatarUrl,
                            bannerUrl = prof.bannerUrl ?: selectedFriend?.bannerUrl,
                            name = prof.displayName.ifBlank { prof.fullName.ifBlank { prof.username } },
                            bio = prof.bio.ifBlank { selectedFriend?.bio ?: "" },
                            isOnline = prof.isOnline
                        )
                    }
                }
            } catch (e: Exception) {
                // Non-fatal
            }
        } else if (currentFriend == null) {
            selectedFriendProfileData = null
        }
    }

    // Friend Profile Info Dialog - Disabled in favor of full screen Friend Profile tab
    if (false && showFriendProfileDialog && selectedFriend != null) {
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

        // Enforce screenshot privacy protection on other user's profile dialog
        val activity = remember(context) { context.findFragmentActivity() }
        androidx.compose.runtime.DisposableEffect(showFriendProfileDialog) {
            val isAdmin = com.example.connecto.security.ScreenSecurityManager.isAdminUser(context)
            if (!isAdmin) {
                activity?.window?.setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE
                )
            }
            onDispose {
                com.example.connecto.security.ScreenSecurityManager.applySecurityPolicy(activity)
            }
        }

        Dialog(
            onDismissRequest = { showFriendProfileDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                securePolicy = com.example.connecto.security.ScreenSecurityManager.getSecureFlagPolicy(context)
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
                    // Top Cover Banner Header
                    val friendBannerResolved = profData?.bannerUrl ?: friendObj.bannerUrl
                    val isFriendPresetBanner = isCoverPreset(friendBannerResolved)
                    val friendPresetObj = if (isFriendPresetBanner) getCoverPreset(friendBannerResolved) else null

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        if (isFriendPresetBanner && friendPresetObj != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(friendPresetObj.brush)
                            )
                        } else if (!friendBannerResolved.isNullOrBlank()) {
                            val resolvedModel = resolveImageModel(friendBannerResolved) ?: sanitizeAvatarUrl(friendBannerResolved) ?: friendBannerResolved
                            AsyncImage(
                                model = resolvedModel,
                                contentDescription = "Friend Cover Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(gradientColors))
                            )
                        }

                        // Top Gradient Scrim for crisp contrast & text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.55f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.70f)
                                        )
                                    )
                                )
                        )

                        // Top Controls Header over Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "FRIEND PROFILE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable { showFriendProfileDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Avatar Circle with Dynamic Accent Glow & Full-Screen Photo Preview
                    val friendAvatarResolved = friendObj.avatarUrl ?: profData?.avatarUrl
                    Box(
                        modifier = Modifier
                            .offset(y = (-44).dp)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        ConnectoAvatar(
                            name = friendObj.name,
                            avatarUrl = friendAvatarResolved,
                            customSizeDp = 92.dp,
                            customFontSizeSp = 36,
                            borderWidth = 3.dp,
                            borderBrush = Brush.sweepGradient(gradientColors),
                            onClick = {
                                showPhotoLightbox = true
                            }
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

                    // Content below avatar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-36).dp)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

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

                        // Background sync remains active; visual Synced pill hidden per user preference
                    }

                    // Interactive Tab Selector Buttons (Chat & Lock, Theme & Wallpaper)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("settings", "Chat & Lock", Icons.Default.Lock),
                            Triple("customization", "Theme & Wallpaper", Icons.Default.Wallpaper)
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
                                    .padding(vertical = 10.dp),
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
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = tabTitle,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
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
                                                val userPrefix = myUsername.lowercase()
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
                                                                    .putBoolean("${userPrefix}_chat_locked_$fId", true)
                                                                    .putBoolean("${userPrefix}_chat_locked_$clean", true)
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
                                                            .putBoolean("${userPrefix}_chat_locked_$fId", true)
                                                            .putBoolean("${userPrefix}_chat_locked_$clean", true)
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
                                                                    .remove("${userPrefix}_chat_locked_$fId")
                                                                    .remove("${userPrefix}_chat_locked_$clean")
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
                                                            .remove("${userPrefix}_chat_locked_$fId")
                                                            .remove("${userPrefix}_chat_locked_$clean")
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
                            // Chat Background Wallpaper Customization (Chat Theme Color removed per user preference)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
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
                                                        wallpaperPickerLauncher.launch("image/*")
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
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                        .clickable {
                                                            showWallpaperCustomizerDialog = true
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Tune,
                                                            contentDescription = "Adjust Size & Position",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "Layout",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFDC2626).copy(alpha = 0.15f))
                                                        .clickable {
                                                            customWallpaperUri = null
                                                            chatWallpaperId = "default"
                                                            val chatKey = getCanonicalChatId(friendObj)
                                                            val fId = friendObj.id
                                                            val userPrefix = myUsername.lowercase()
                                                            val editor = friendPrefs.edit()
                                                            if (chatKey.isNotBlank()) {
                                                                editor.remove("${userPrefix}_chat_wallpaper_custom_$chatKey")
                                                                    .putString("${userPrefix}_chat_wallpaper_$chatKey", "default")
                                                            }
                                                            editor.remove("${userPrefix}_chat_wallpaper_custom_$fId")
                                                                .putString("${userPrefix}_chat_wallpaper_$fId", "default")
                                                                .remove("chat_wallpaper_custom_$fId")
                                                                .remove("chat_wallpaper_$fId")
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
                                                    customWallpaperUri = null
                                                    val chatKey = getCanonicalChatId(friendObj)
                                                    val fId = friendObj.id
                                                    val cleanHandle = friendObj.handle.trim().removePrefix("@").lowercase()
                                                    val userPrefix = myUsername.lowercase()
                                                    val editor = friendPrefs.edit()
                                                    if (chatKey.isNotBlank()) {
                                                        editor.putString("${userPrefix}_chat_wallpaper_$chatKey", wallpaper.id)
                                                            .remove("${userPrefix}_chat_wallpaper_custom_$chatKey")
                                                    }
                                                    if (cleanHandle.isNotBlank()) {
                                                        editor.putString("${userPrefix}_chat_wallpaper_$cleanHandle", wallpaper.id)
                                                            .remove("${userPrefix}_chat_wallpaper_custom_$cleanHandle")
                                                    }
                                                    editor.putString("${userPrefix}_chat_wallpaper_$fId", wallpaper.id)
                                                        .remove("${userPrefix}_chat_wallpaper_custom_$fId")
                                                        .remove("chat_wallpaper_custom_$fId")
                                                        .remove("chat_wallpaper_$fId")
                                                        .apply()
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
                    }

                    } // end Scrollable Tab Body

                    // FOOTER IN CHAT & LOCK TAB: ONLY UNFRIEND BUTTON (Voice Call removed per user preference)
                    if (friendProfileSelectedTab == "settings") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .height(46.dp)
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
                                Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("UNFRIEND", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
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
        val friendClean = friendObj.handle.removePrefix("@").trim().lowercase()
        val prof = selectedFriendProfileData?.takeIf {
            it.username.lowercase() == friendClean || (it.id.isNotBlank() && it.id == friendObj.id)
        } ?: friendProfilesCache[friendClean]
        val resolvedAvatar = friendObj.avatarUrl ?: prof?.avatarUrl
        PhotoLightboxDialog(
            title = friendObj.name,
            imageUrl = resolvedAvatar,
            placeholderInitial = friendObj.initial,
            isBanner = false,
            onDismiss = { showPhotoLightbox = false }
        )
    }

    // Chat Wallpaper Size, Position & Legibility Customizer Dialog
    if (showWallpaperCustomizerDialog) {
        WallpaperCustomizerDialog(
            wallpaperId = chatWallpaperId,
            customWallpaperUri = customWallpaperUri,
            initialTransform = wallpaperTransform,
            isDarkTheme = !isLight,
            onDismissRequest = { showWallpaperCustomizerDialog = false },
            onSave = { newTransform ->
                val friend = selectedFriend
                if (friend != null && friend.isGroup) {
                    val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                    if (!friend.isUserAdmin(effUser)) {
                        Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                        showWallpaperCustomizerDialog = false
                        return@WallpaperCustomizerDialog
                    }
                }
                wallpaperTransform = newTransform
                WallpaperResolver.saveTransform(
                    friend = friend,
                    myUsername = myUsername,
                    prefs = friendPrefs,
                    transform = newTransform
                )
                Toast.makeText(context, "Wallpaper layout updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Unfriend Confirmation Modal Dialog with Data Wipe Warning Tab
    if (showUnfriendConfirmDialog && selectedFriend != null) {
        val friendToRemove = selectedFriend!!
        val friendUser = friendToRemove.handle.removePrefix("@").ifBlank { friendToRemove.name }
        val friendName = friendToRemove.name.ifBlank { friendUser }

        AlertDialog(
            onDismissRequest = { if (!isUnfriending) showUnfriendConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Permanent Data Deletion",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Warning Callout Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🛑", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "IRREVERSIBLE ACTION",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Unfriending @$friendUser will permanently delete all mutual history for BOTH users.",
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Itemized Deletion Checklist
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "THE FOLLOWING DATA WILL BE PERMANENTLY ERASED:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                letterSpacing = 0.4.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💬", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Direct Messages: Mutual chat history & media",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📞", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Call Logs: Voice & video call logs between users",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔔", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Notifications: Pending alerts and badges",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("👥", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Friend Connection: Removed from both rosters",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isUnfriending = true
                            try {
                                val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                                ConnectoApiClient.unfriendUser(targetUsername = friendUser, token = null)
                                dbHelper.deleteMutualConversationData(effUser, friendUser)
                                friendsList.removeAll {
                                    it.id == friendToRemove.id ||
                                    it.handle.removePrefix("@").equals(friendUser, ignoreCase = true) ||
                                    it.name.equals(friendName, ignoreCase = true)
                                }
                                if (activeDmChannelId != null && (activeDmChannelId?.contains(friendUser, ignoreCase = true) == true || selectedFriend?.id == friendToRemove.id)) {
                                    friendMessages = emptyList()
                                    activeDmChannelId = null
                                    pinnedMessageForChat = null
                                }
                                showUnfriendConfirmDialog = false
                                showFriendProfileDialog = false
                                selectedFriend = null
                                chatWallpaperId = "default"
                                customWallpaperUri = null
                                Toast.makeText(context, "Unfriended @$friendUser. Chat and call history permanently deleted.", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isUnfriending = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isUnfriending
                ) {
                    if (isUnfriending) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("🗑️ Confirm & Delete Everything", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnfriendConfirmDialog = false },
                    enabled = !isUnfriending
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                isLoadingMessages = false
                val (wId, cUri) = resolveChatWallpaper(friendItem)
                chatWallpaperId = wId
                customWallpaperUri = cUri
                typedMessage = messageDrafts[friendItem.id] ?: ""
                selectedFriend = friendItem
                onDirectChatStateChanged(true)
                coroutineScope.launch(Dispatchers.IO) {
                    val initialMsgs = loadLocalMessagesForFriend(friendItem)
                    withContext(Dispatchers.Main) {
                        if (initialMsgs.isNotEmpty()) {
                            friendMessages = initialMsgs
                            try { listState.scrollToItem(initialMsgs.size) } catch (_: Exception) {}
                        }
                    }
                }
            }
        )
    }

    if (showCreateGroupChatDialog) {
        val effUser = if (currentUsername.isNotBlank()) currentUsername else (com.example.connecto.network.ConnectoApiClient.currentUsername ?: "")
        CreateGroupChatDialog(
            currentUsername = effUser,
            onDismissRequest = { showCreateGroupChatDialog = false },
            onGroupCreated = { gName, gId, gMembers ->
                showCreateGroupChatDialog = false
                val cleanAdmin = effUser.trim().removePrefix("@")
                val allMembers = (listOf(cleanAdmin) + gMembers.map { it.trim().removePrefix("@") }).filter { it.isNotBlank() }.distinct()
                val resolvedId = gId.ifBlank { "grp-${gName.lowercase().replace(" ", "-")}" }
                val newGroupItem = FriendItem(
                    id = resolvedId,
                    name = gName,
                    handle = "#${gName.lowercase().replace(" ", "-")}",
                    initial = gName.firstOrNull()?.uppercase() ?: "G",
                    status = "${allMembers.size} members • Created Group",
                    bio = "Created by @$cleanAdmin",
                    lastMessage = "Group Chat • Tap to open and talk",
                    timeAgo = "Just now",
                    unreadCount = 0,
                    isOnline = false,
                    avatarUrl = null,
                    isGroup = true,
                    isCreatedByMe = true,
                    memberCount = allMembers.size,
                    members = allMembers,
                    admin = cleanAdmin,
                    creator = cleanAdmin
                )
                val existingIdx = userGroupsList.indexOfFirst { it.id == resolvedId }
                if (existingIdx == -1) {
                    userGroupsList.add(0, newGroupItem)
                } else {
                    userGroupsList[existingIdx] = newGroupItem
                }
                coroutineScope.launch(Dispatchers.IO) {
                    try { dbHelper.saveGroups(userGroupsList.toList()) } catch (_: Exception) {}
                }
                val (gwId, gcUri) = resolveChatWallpaper(newGroupItem)
                chatWallpaperId = gwId
                customWallpaperUri = gcUri
                selectedFriend = newGroupItem
            }
        )
    }

    if (showCreateVoiceGroupDialog) {
        val effUser = if (currentUsername.isNotBlank()) currentUsername else (com.example.connecto.network.ConnectoApiClient.currentUsername ?: "")
        CreateVoiceGroupDialog(
            currentUsername = effUser,
            onDismissRequest = { showCreateVoiceGroupDialog = false },
            onStartVoiceGroup = { title, selectedFriendsList ->
                showCreateVoiceGroupDialog = false
                initiateVoiceGroupCallWithFriends(title, selectedFriendsList)
            }
        )
    }

    if (showGroupProfileDialog && selectedFriend != null && selectedFriend!!.isGroup) {
        val groupItem = selectedFriend!!
        val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
        val userPrefix = effUser.trim().removePrefix("@").lowercase()
        FullScreenGroupProfileDialog(
            group = groupItem,
            currentUsername = effUser,
            chatWallpaperId = chatWallpaperId,
            customWallpaperUri = customWallpaperUri,
            onWallpaperSelected = { wId, customUri ->
                if (!groupItem.isUserAdmin(effUser)) {
                    Toast.makeText(screenCtx, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                    return@FullScreenGroupProfileDialog
                }
                chatWallpaperId = wId
                customWallpaperUri = customUri
                val fId = groupItem.id
                val chatKey = getCanonicalChatId(groupItem)
                val editor = friendPrefs.edit()
                if (wId == "custom" && !customUri.isNullOrBlank()) {
                    if (chatKey.isNotBlank()) {
                        editor.putString("${userPrefix}_chat_wallpaper_custom_$chatKey", customUri)
                            .putString("${userPrefix}_chat_wallpaper_$chatKey", "custom")
                    }
                    editor.putString("${userPrefix}_chat_wallpaper_custom_$fId", customUri)
                        .putString("${userPrefix}_chat_wallpaper_$fId", "custom")
                } else {
                    if (chatKey.isNotBlank()) {
                        editor.putString("${userPrefix}_chat_wallpaper_$chatKey", wId)
                            .remove("${userPrefix}_chat_wallpaper_custom_$chatKey")
                    }
                    editor.putString("${userPrefix}_chat_wallpaper_$fId", wId)
                        .remove("${userPrefix}_chat_wallpaper_custom_$fId")
                }
                editor.remove("chat_wallpaper_custom_$fId")
                    .remove("chat_wallpaper_$fId")
                    .apply()
            },
            onOpenWallpaperCustomizer = {
                if (!groupItem.isUserAdmin(effUser)) {
                    Toast.makeText(screenCtx, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                } else {
                    showGroupProfileDialog = false
                    showWallpaperCustomizerDialog = true
                }
            },
            onGroupUpdated = { updatedGroup ->
                selectedFriend = updatedGroup
                val idx = userGroupsList.indexOfFirst { it.id == updatedGroup.id }
                if (idx != -1) {
                    userGroupsList[idx] = updatedGroup
                }
                coroutineScope.launch(Dispatchers.IO) {
                    try { dbHelper.saveGroups(userGroupsList.toList()) } catch (_: Exception) {}
                }
            },
            onDeleteGroup = {
                val gId = groupItem.id
                userGroupsList.removeAll { it.id == gId }
                coroutineScope.launch(Dispatchers.IO) {
                    try { dbHelper.deleteCachedGroup(gId) } catch (_: Exception) {}
                    try { dbHelper.saveGroups(userGroupsList.toList()) } catch (_: Exception) {}
                    try {
                        val rPrefs = screenCtx.getSharedPreferences("connecto_read_receipts", Context.MODE_PRIVATE)
                        rPrefs.edit()
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
                        `package` = screenCtx.packageName
                    }
                    screenCtx.sendBroadcast(broadcastIntent)
                } catch (_: Exception) {}
                selectedFriend = null
                chatWallpaperId = "default"
                customWallpaperUri = null
                showGroupProfileDialog = false
            },
            onLeaveGroup = {
                val gId = groupItem.id
                userGroupsList.removeAll { it.id == gId }
                coroutineScope.launch(Dispatchers.IO) {
                    try { dbHelper.deleteCachedGroup(gId) } catch (_: Exception) {}
                    try { dbHelper.saveGroups(userGroupsList.toList()) } catch (_: Exception) {}
                    try {
                        val rPrefs = screenCtx.getSharedPreferences("connecto_read_receipts", Context.MODE_PRIVATE)
                        rPrefs.edit()
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
                        `package` = screenCtx.packageName
                    }
                    screenCtx.sendBroadcast(broadcastIntent)
                } catch (_: Exception) {}
                selectedFriend = null
                chatWallpaperId = "default"
                customWallpaperUri = null
                showGroupProfileDialog = false
            },
            onDismissRequest = { showGroupProfileDialog = false }
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
            if (selectedMessageForAction != null) {
                val selMsg = selectedMessageForAction!!
                val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@").lowercase()
                val myDisplay = (ConnectoApiClient.currentUserDisplayName ?: "").trim().lowercase().removePrefix("@")
                val myUserId = (ConnectoApiClient.currentUserId ?: "").trim().lowercase()
                val senderClean = selMsg.senderName.trim().removePrefix("@").lowercase()
                val isMyMsg = selMsg.isMe || (
                    (myUser.isNotBlank() && senderClean == myUser) ||
                    (myDisplay.isNotBlank() && senderClean == myDisplay) ||
                    (myUserId.isNotBlank() && (senderClean == myUserId || selMsg.id.contains(myUserId)))
                )
                val isPinned = pinnedMessageForChat?.messageId == selMsg.id

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    color = if (isPureBlack) Color(0xFF13151B) else MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedMessageForAction = null }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Selection",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1 selected",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        // Right side 3-dots button
                        Box {
                            IconButton(
                                onClick = { showMoreOptionsMenu = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Message Options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = showMoreOptionsMenu,
                                onDismissRequest = { showMoreOptionsMenu = false },
                                modifier = Modifier.background(
                                    if (isPureBlack) Color(0xFF1E2029) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                // 1. Pin / Unpin
                                DropdownMenuItem(
                                    text = { Text(if (isPinned) "Unpin" else "Pin", color = MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {
                                        showMoreOptionsMenu = false
                                        val currentFriend = selectedFriend
                                        val chanId = activeDmChannelId
                                        val msgId = selMsg.id
                                        val authorName = selMsg.senderName.ifBlank { "Message" }
                                        val content = selMsg.content
                                        val canonicalChatId = getCanonicalChatId(currentFriend)
                                        val pinKey = if (currentFriend?.isGroup == true) currentFriend.id else canonicalChatId.lowercase()
                                        val targetChan = chanId ?: (if (currentFriend?.isGroup == true) currentFriend.id else canonicalChatId)

                                        if (isPinned) {
                                            pinnedMessageForChat = null
                                            if (currentFriend != null) {
                                                friendPrefs.edit()
                                                    .remove("pinned_msg_${pinKey}")
                                                    .remove("pinned_content_${pinKey}")
                                                    .remove("pinned_author_${pinKey}")
                                                    .remove("pinned_msg_${currentFriend.id}")
                                                    .remove("pinned_content_${currentFriend.id}")
                                                    .remove("pinned_author_${currentFriend.id}")
                                                    .apply()
                                            }
                                            if (targetChan.isNotBlank()) {
                                                coroutineScope.launch {
                                                    ConnectoApiClient.unpinMessage(targetChan)
                                                }
                                            }
                                        } else {
                                            pinnedMessageForChat = PinnedMessageDto(
                                                messageId = msgId,
                                                channelId = targetChan,
                                                authorName = authorName,
                                                content = content,
                                                pinnedBy = myUsername,
                                                pinnedAt = System.currentTimeMillis().toString()
                                            )
                                            if (currentFriend != null) {
                                                friendPrefs.edit()
                                                    .putString("pinned_msg_${pinKey}", msgId)
                                                    .putString("pinned_content_${pinKey}", content)
                                                    .putString("pinned_author_${pinKey}", authorName)
                                                    .apply()
                                            }
                                            if (targetChan.isNotBlank()) {
                                                coroutineScope.launch {
                                                    ConnectoApiClient.pinMessage(targetChan, msgId, myUsername)
                                                }
                                            }
                                        }
                                        selectedMessageForAction = null
                                    }
                                )

                                // 2. Edit (only for own message)
                                if (isMyMsg) {
                                    DropdownMenuItem(
                                        text = { Text("Edit", color = MaterialTheme.colorScheme.onSurface) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showMoreOptionsMenu = false
                                            editingMessage = selMsg
                                            typedMessage = selMsg.content
                                            selectedMessageForAction = null
                                        }
                                    )
                                }

                                // 3. Delete (only for own message)
                                if (isMyMsg) {
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = Color(0xFFEF4444)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444)
                                            )
                                        },
                                        onClick = {
                                            showMoreOptionsMenu = false
                                            val msgId = selMsg.id
                                            val chanId = activeDmChannelId
                                            val currentFriend = selectedFriend
                                            val canonicalChatId = getCanonicalChatId(currentFriend)
                                            val targetChan = chanId ?: (if (currentFriend?.isGroup == true) currentFriend.id else canonicalChatId)

                                            friendMessages = friendMessages.filterNot { it.id == msgId }

                                            if (pinnedMessageForChat?.messageId == msgId) {
                                                pinnedMessageForChat = null
                                                currentFriend?.let { f ->
                                                    val delPinKey = if (f.isGroup) f.id else canonicalChatId.lowercase()
                                                    friendPrefs.edit()
                                                        .remove("pinned_msg_${delPinKey}")
                                                        .remove("pinned_content_${delPinKey}")
                                                        .remove("pinned_author_${delPinKey}")
                                                        .remove("pinned_msg_${f.id}")
                                                        .remove("pinned_content_${f.id}")
                                                        .remove("pinned_author_${f.id}")
                                                        .apply()
                                                    if (targetChan.isNotBlank()) {
                                                        coroutineScope.launch {
                                                            ConnectoApiClient.unpinMessage(targetChan)
                                                        }
                                                    }
                                                }
                                            }

                                            val deletedSet = friendPrefs.getStringSet("deleted_for_me_$myUsername", emptySet())
                                                ?.toMutableSet() ?: mutableSetOf()
                                            deletedSet.add(msgId)
                                            friendPrefs.edit()
                                                .putStringSet("deleted_for_me_$myUsername", deletedSet)
                                                .apply()

                                            coroutineScope.launch(Dispatchers.IO) {
                                                try {
                                                    dbHelper.deleteMessage(msgId)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }

                                            coroutineScope.launch {
                                                ConnectoApiClient.deleteMessage(msgId, targetChan.ifBlank { null })
                                                ConnectoApiClient.deleteMessageForMe(msgId, myUsername)
                                            }

                                            Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
                                            selectedMessageForAction = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (isFriendProfileOpen && selectedFriend != null && !selectedFriend!!.isGroup) {
                // Friend Profile screen renders its own full-screen custom top header
            } else if (selectedFriend != null) {
                // 1:1 Direct Chat Header with Call Buttons & Friend Profile Info Trigger
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isLiquidGlassTheme) {
                                val alpha = ConnectoTheme.glassAlpha
                                Modifier
                                    .background(
                                        if (alpha <= 0.005f) {
                                            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                                        } else {
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.White.copy(alpha = (alpha * 0.35f).coerceIn(0.02f, 0.45f)),
                                                    Color.White.copy(alpha = (alpha * 0.15f).coerceIn(0.01f, 0.25f))
                                                )
                                            )
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.10f),
                                                Color.White.copy(alpha = if (alpha <= 0.005f) 0.50f else (0.60f - alpha * 0.20f).coerceIn(0.30f, 0.65f)),
                                                Color.White.copy(alpha = 0.10f)
                                            )
                                        ),
                                        shape = RectangleShape
                                    )
                            } else if (isPureBlack) {
                                Modifier
                                    .background(Color(0xCC000000))
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.verticalGradient(
                                            listOf(Color(0x33FFFFFF), Color(0x0DFFFFFF))
                                        ),
                                        shape = RectangleShape
                                    )
                            } else if (isGlassTheme) {
                                Modifier
                                    .background(Color(0xF00F172A))
                                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                            } else {
                                Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                            }
                        )
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
                                .background(
                                    if (isLiquidGlassTheme) {
                                        val alpha = ConnectoTheme.glassAlpha
                                        if (alpha <= 0.005f) Color.Transparent
                                        else Color.White.copy(alpha = (alpha * 0.25f).coerceIn(0.01f, 0.35f))
                                    }
                                    else if (isPureBlack) Color(0x1AFFFFFF)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isLiquidGlassTheme) {
                                        val alpha = ConnectoTheme.glassAlpha
                                        val rimAlpha = if (alpha <= 0.005f) 0.55f else (0.65f - alpha * 0.20f).coerceIn(0.30f, 0.70f)
                                        Color.White.copy(alpha = rimAlpha)
                                    }
                                    else if (isPureBlack) Color(0x40FFFFFF)
                                    else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                                .clickable {
                                    selectedFriend?.let { friend ->
                                        markFriendChatAsReadLocallyAndRemotely(friend)
                                        if (typedMessage.isNotBlank()) {
                                            messageDrafts[friend.id] = typedMessage
                                        } else {
                                            messageDrafts.remove(friend.id)
                                        }
                                    }
                                    typedMessage = ""
                                    selectedFriend = null
                                    selectedFriendProfileData = null
                                 chatWallpaperId = "default"
                                 customWallpaperUri = null
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

                        // Friend / Group Name & Avatar Row
                        val isGroupChat = selectedFriend?.isGroup == true
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (isGroupChat) {
                                        showCreateGroupChatDialog = false
                                        showFriendProfileDialog = false
                                        showGroupProfileDialog = true
                                    } else {
                                        isFriendProfileOpen = true
                                        friendProfileSubPage = null
                                    }
                                }
                        ) {
                            val isSelectedOnline = selectedFriend?.isOnline == true
                            val onlineTransition = rememberInfiniteTransition(label = "directChatOnlinePulse")
                            val pulseScale by onlineTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.35f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1400, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "onlinePulseScale"
                            )
                            val pulseAlpha by onlineTransition.animateFloat(
                                initialValue = 0.75f,
                                targetValue = 0.25f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1400, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "onlinePulseAlpha"
                            )

                            if (isGroupChat) {
                                if (!selectedFriend?.avatarUrl.isNullOrBlank()) {
                                    ConnectoAvatar(
                                        name = selectedFriend?.name ?: "Group",
                                        avatarUrl = selectedFriend?.avatarUrl,
                                        customSizeDp = 42.dp,
                                        customFontSizeSp = 16,
                                        borderWidth = 1.5.dp,
                                        borderColor = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = "Group",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.size(42.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ConnectoAvatar(
                                        name = selectedFriend?.name ?: "Friend",
                                        avatarUrl = selectedFriend?.avatarUrl,
                                        customSizeDp = 42.dp,
                                        customFontSizeSp = 16,
                                        borderWidth = 1.5.dp,
                                        borderColor = if (isSelectedOnline) OnlineGreen.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                                    )
                                    if (isSelectedOnline) {
                                        // Outer breathing aura ring
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(11.dp)
                                                .graphicsLayer {
                                                    scaleX = pulseScale
                                                    scaleY = pulseScale
                                                    alpha = pulseAlpha
                                                }
                                                .clip(CircleShape)
                                                .background(OnlineGreen)
                                        )
                                        // Solid inner status beacon
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(11.dp)
                                                .clip(CircleShape)
                                                .background(OnlineGreen)
                                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = if (isGroupChat) "#${selectedFriend!!.name}" else selectedFriend!!.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isGroupChat) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .graphicsLayer {
                                                    if (isSelectedOnline) {
                                                        alpha = 0.5f + (pulseAlpha * 0.5f)
                                                    }
                                                }
                                                .clip(CircleShape)
                                                .background(if (isSelectedOnline) OnlineGreen else Color.Gray)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    val subText = if (isGroupChat) {
                                        val count = selectedFriend?.memberCount ?: selectedFriend?.members?.size ?: 0
                                        "$count members • Group Chat"
                                    } else {
                                        if (isFriendTyping) "typing..." else if (isSelectedOnline) "Online • Direct Chat" else "Offline • Direct Chat"
                                    }
                                    Text(
                                        text = subText,
                                        color = if (!isGroupChat && isFriendTyping) MaterialTheme.colorScheme.primary else if (!isGroupChat && isSelectedOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = if (!isGroupChat && isFriendTyping) FontWeight.Bold else FontWeight.Medium,
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
                            if (!isGroupChat) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                                        .clickable {
                                            isFriendProfileOpen = true
                                            friendProfileSubPage = null
                                        },
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

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        .clickable {
                                            showWallpaperCustomizerDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Wallpaper Size & Position",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                // Group Header Actions: Info, Group Voice Call, and Admin/Member Tag
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
                                            .clickable { showGroupProfileDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Group Profile Info",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Group Voice Call Action Button
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                            .clickable {
                                                val grp = selectedFriend
                                                if (grp != null) {
                                                    initiateGroupVoiceCall(grp)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Group Voice Call",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Group Wallpaper Layout Button (Admin only)
                                    val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                                    val isGroupAdmin = selectedFriend?.isUserAdmin(effUser) == true
                                    if (isGroupAdmin) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                                .clickable {
                                                    showWallpaperCustomizerDialog = true
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = "Group Wallpaper Layout",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable { showGroupProfileDialog = true }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (selectedFriend?.isCreatedByMe == true) "ADMIN" else "MEMBER",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Specular highlight divider line under TopBar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.40f),
                                    Color.White.copy(alpha = 0.25f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.40f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            } else {
                ConnectoTopHeader(
                    title = "DIRECT MESSAGES",
                    subtitle = "Chat with friends & members",
                    userInitial = userInitial,
                    onVoiceGroupClick = null,
                    onAddFriendClick = null,
                    pendingFriendRequestsCount = 0,
                    showProfileButton = false,
                    onAvatarClick = {},
                    customAction = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(gradientColors))
                                .clickable { showCreateGroupChatDialog = true }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = "Create Group Chat",
                                tint = contentOnGradient,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Group Chat",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentOnGradient
                            )
                        }
                    }
                )
            }

            val currentScreenState = when {
                isFriendProfileOpen && selectedFriend != null && !selectedFriend!!.isGroup -> "FRIEND_PROFILE"
                selectedFriend != null -> "DIRECT_CHAT"
                else -> "FRIENDS_LIST"
            }

            AnimatedContent(
                targetState = currentScreenState,
                transitionSpec = {
                    if (targetState == "FRIEND_PROFILE" || (initialState == "FRIENDS_LIST" && targetState == "DIRECT_CHAT")) {
                        (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(tween(280)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut(tween(280)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), initialOffsetX = { fullWidth -> -fullWidth }) + fadeIn(tween(280)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), targetOffsetX = { fullWidth -> fullWidth }) + fadeOut(tween(280)))
                    }
                },
                label = "friendChatViewTransition",
                modifier = Modifier.weight(1f)
            ) { screenState ->
                when (screenState) {
                    "FRIENDS_LIST" -> {
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

                        // Message Tab Filter Button Group (Segmented Controls)
                        item(key = "friends_filter_button_group") {
                            val filterOptions = listOf(
                                "ALL" to "All",
                                "DIRECT" to "Direct",
                                "CREATED_GROUPS" to "Created Groups",
                                "ADDED_GROUPS" to "Added Groups"
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                filterOptions.forEach { (key, label) ->
                                    val isSelected = (messageTabFilter == key)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else Color.Transparent
                                            )
                                            .clickable { messageTabFilter = key }
                                            .padding(horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) contentOnGradient else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "friends_section_title") {
                            val titleText = when (messageTabFilter) {
                                "DIRECT" -> "ACTIVE DIRECT CONVERSATIONS"
                                "CREATED_GROUPS" -> "CREATED GROUPS"
                                "ADDED_GROUPS" -> "ADDED & JOINED GROUPS"
                                else -> "ALL CONVERSATIONS & GROUPS"
                            }
                            Text(
                                text = titleText,
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
                                            val emptyIcon = when (messageTabFilter) {
                                                "CREATED_GROUPS", "ADDED_GROUPS" -> Icons.Default.Groups
                                                else -> Icons.Default.PersonAdd
                                            }
                                            val emptyTitle = if (friendSearchQuery.isNotBlank()) "No Matching Conversations"
                                            else when (messageTabFilter) {
                                                "CREATED_GROUPS" -> "No Created Groups"
                                                "ADDED_GROUPS" -> "No Joined Groups"
                                                "DIRECT" -> "No Direct Messages Yet"
                                                else -> "No Active Conversations"
                                            }
                                            val emptySubtitle = if (friendSearchQuery.isNotBlank())
                                                "No conversations or groups matching '$friendSearchQuery' found."
                                            else when (messageTabFilter) {
                                                "CREATED_GROUPS" -> "You haven't created any group channels yet. Create your first group to start group chats!"
                                                "ADDED_GROUPS" -> "You haven't joined any groups yet. When you join or get added to groups, they will appear here."
                                                "DIRECT" -> "Direct chat is private between accepted friends. Send a friend request to start messaging!"
                                                else -> "Start chatting with friends or create a group to talk with multiple members together!"
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .breathingPulse(minScale = 0.94f, maxScale = 1.06f)
                                                    .size(60.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(gradientColors)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = emptyIcon,
                                                    contentDescription = null,
                                                    tint = contentOnGradient,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }

                                            Text(
                                                text = emptyTitle,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )

                                            Text(
                                                text = emptySubtitle,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 18.sp
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            val isGroupFilter = messageTabFilter == "CREATED_GROUPS" || messageTabFilter == "ADDED_GROUPS"
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(Brush.linearGradient(gradientColors))
                                                    .clickable {
                                                        if (isGroupFilter) showCreateGroupChatDialog = true
                                                        else showAddFriendDialog = true
                                                    }
                                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isGroupFilter) Icons.Default.GroupAdd else Icons.Default.PersonAdd,
                                                        contentDescription = null,
                                                        tint = contentOnGradient,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = if (isGroupFilter) "Create New Group" else "Find & Add Friends",
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
                            val userPrefix = myUsername.lowercase()
                            val isFriendLocked = friendPrefs.getBoolean("${userPrefix}_chat_locked_${friend.id}", false) || friendPrefs.getBoolean("${userPrefix}_chat_locked_${friend.handle.trim().lowercase().removePrefix("@")}", false)
                            val isUnlocked = unlockedLockedFriends.contains(friend.id)

                            val openFriendChat = {
                                val cleanTarget = friend.handle.removePrefix("@").trim()
                                val cleanLower = cleanTarget.lowercase()
                                selectedFriendProfileData = friendProfilesCache[cleanLower]
                                val (initialWp, initialUri) = resolveChatWallpaper(friend)
                                chatWallpaperId = initialWp
                                customWallpaperUri = initialUri
                                val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
                                val canonicalName = if (friend.isGroup) friend.id else "dm-${minOf(myUser, cleanTarget.lowercase())}-${maxOf(myUser, cleanTarget.lowercase())}"
                                val storedId = if (friend.isGroup) friend.id else dbHelper.getDmChannelId(canonicalName)
                                activeDmChannelId = if (storedId != null && !storedId.startsWith("grp-") && !storedId.startsWith("grp_") && !storedId.startsWith("group-")) {
                                    storedId
                                } else if (friend.isGroup) {
                                    friend.id
                                } else null
                                friendMessages = emptyList()
                                typedMessage = messageDrafts[friend.id] ?: ""
                                selectedFriend = friend
                                onDirectChatStateChanged(true)
                                markFriendChatAsReadLocallyAndRemotely(friend)

                                coroutineScope.launch(Dispatchers.IO) {
                                    val initialMsgs = loadLocalMessagesForFriend(friend)
                                    withContext(Dispatchers.Main) {
                                        friendMessages = initialMsgs
                                        isLoadingMessages = false
                                        try { listState.scrollToItem(initialMsgs.size) } catch (_: Exception) {}
                                    }
                                }
                            }

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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
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
                                    if (friend.isGroup) {
                                        if (!friend.avatarUrl.isNullOrBlank()) {
                                            ConnectoAvatar(
                                                name = friend.name,
                                                avatarUrl = friend.avatarUrl,
                                                customSizeDp = 48.dp,
                                                customFontSizeSp = 18,
                                                borderWidth = 1.5.dp,
                                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Groups,
                                                    contentDescription = "Group",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        // Avatar with online status ring (clickable to open dedicated friend profile UI tab)
                                        Box(
                                            modifier = Modifier.clickable {
                                                val clean = friend.handle.removePrefix("@").trim().lowercase()
                                                selectedFriend = friend
                                                selectedFriendProfileData = friendProfilesCache[clean]
                                                isFriendProfileOpen = true
                                                friendProfileSubPage = null
                                                openedProfileFromChatList = true
                                            }
                                        ) {
                                            ConnectoAvatar(
                                                name = friend.name,
                                                avatarUrl = friend.avatarUrl,
                                                customSizeDp = 48.dp,
                                                customFontSizeSp = 18,
                                                status = if (friend.isOnline) ConnectoPresenceStatus.ONLINE else ConnectoPresenceStatus.OFFLINE,
                                                borderWidth = 1.5.dp,
                                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            )
                                        }
                                    }

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
                                                text = if (friend.isGroup) "#${friend.name}" else friend.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (friend.isGroup) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "${friend.memberCount} members",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
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

                                        val displayMsg = if (friend.lastMessage.isBlank() || friend.lastMessage == "null" || friend.lastMessage == "None") {
                                            "Direct chat active • Tap to message"
                                        } else friend.lastMessage

                                        Text(
                                            text = displayMsg,
                                            fontSize = 13.sp,
                                            color = if (friend.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (friend.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
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
                                        val displayTime = if (friend.timeAgo.isBlank() || friend.timeAgo == "null" || friend.timeAgo == "None") {
                                            "Active"
                                        } else formatIsoTime(friend.timeAgo)

                                        Text(
                                            text = displayTime,
                                            fontSize = 11.sp,
                                            fontWeight = if (friend.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                                            color = if (friend.unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )

                                        if (friend.unreadCount > 0) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .padding(horizontal = 5.dp, vertical = 2.dp),
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
                    } // end DisableSelection
                    }
                    } // end FRIENDS_LIST branch
                    "FRIEND_PROFILE" -> {
                        // ================= 3. DEDICATED FULL-SCREEN FRIEND PROFILE TAB =================
                        selectedFriend?.let { friendObj ->
                            val friendClean = friendObj.handle.removePrefix("@").trim().lowercase()
                            // Strict validation: profData must belong to friendObj!
                            val profData = selectedFriendProfileData?.takeIf {
                                it.username.lowercase() == friendClean ||
                                (it.id.isNotBlank() && it.id == friendObj.id)
                            } ?: friendProfilesCache[friendClean]
                            val friendHandleClean = friendObj.handle.removePrefix("@")
                            val fId = friendObj.id
                            val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
                            val isGroupAdmin = if (friendObj.isGroup) friendObj.isUserAdmin(myUser) else true

                            FriendProfileScreen(
                                friend = friendObj,
                                profData = profData,
                                isChatLocked = isChatLocked,
                                isChatMuted = isChatMuted,
                                chatWallpaperId = chatWallpaperId,
                                customWallpaperUri = customWallpaperUri,
                                wallpaperTransform = wallpaperTransform,
                                onOpenWallpaperCustomizer = {
                                    if (friendObj.isGroup && !isGroupAdmin) {
                                        Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                                    } else {
                                        showWallpaperCustomizerDialog = true
                                    }
                                },
                                subPage = friendProfileSubPage,
                                onNavigateSubPage = { page -> friendProfileSubPage = page },
                                onBack = {
                                    if (friendProfileSubPage != null) {
                                        friendProfileSubPage = null
                                    } else if (openedProfileFromChatList) {
                                        isFriendProfileOpen = false
                                        openedProfileFromChatList = false
                                        selectedFriend = null
                                        selectedFriendProfileData = null
                                        onDirectChatStateChanged(false)
                                    } else {
                                        isFriendProfileOpen = false
                                        selectedFriendProfileData = null
                                    }
                                },
                                onToggleChatLock = { checked ->
                                    val activity = context.findFragmentActivity()
                                    val userPrefix = myUser
                                    if (checked) {
                                        if (activity != null && (BiometricAuthManager.isBiometricEnrolled(activity) || BiometricAuthManager.isDeviceSecure(activity))) {
                                            BiometricAuthManager.authenticate(
                                                activity = activity,
                                                title = "Lock Chat with @$friendHandleClean",
                                                subtitle = "Verify biometric or device credential to enable chat lock",
                                                onSuccess = {
                                                    isChatLocked = true
                                                    friendPrefs.edit()
                                                        .putBoolean("${userPrefix}_chat_locked_$fId", true)
                                                        .putBoolean("${userPrefix}_chat_locked_$friendHandleClean", true)
                                                        .apply()
                                                    if (!unlockedLockedFriends.contains(fId)) {
                                                        unlockedLockedFriends.add(fId)
                                                    }
                                                    Toast.makeText(context, "Chat locked for @$friendHandleClean", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { _, err ->
                                                    Toast.makeText(context, "Authentication required: $err", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            isChatLocked = true
                                            friendPrefs.edit()
                                                .putBoolean("${userPrefix}_chat_locked_$fId", true)
                                                .putBoolean("${userPrefix}_chat_locked_$friendHandleClean", true)
                                                .apply()
                                            if (!unlockedLockedFriends.contains(fId)) {
                                                unlockedLockedFriends.add(fId)
                                            }
                                            Toast.makeText(context, "Chat locked for @$friendHandleClean", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        if (activity != null && (BiometricAuthManager.isBiometricEnrolled(activity) || BiometricAuthManager.isDeviceSecure(activity))) {
                                            BiometricAuthManager.authenticate(
                                                activity = activity,
                                                title = "Unlock Chat with @$friendHandleClean",
                                                subtitle = "Verify identity to remove chat lock protection",
                                                onSuccess = {
                                                    isChatLocked = false
                                                    friendPrefs.edit()
                                                        .remove("${userPrefix}_chat_locked_$fId")
                                                        .remove("${userPrefix}_chat_locked_$friendHandleClean")
                                                        .remove("chat_locked_$fId")
                                                        .apply()
                                                    Toast.makeText(context, "Chat lock removed for @$friendHandleClean", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { _, err ->
                                                    Toast.makeText(context, "Authentication required to disable lock: $err", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            isChatLocked = false
                                            friendPrefs.edit()
                                                .remove("${userPrefix}_chat_locked_$fId")
                                                .remove("${userPrefix}_chat_locked_$friendHandleClean")
                                                .remove("chat_locked_$fId")
                                                .apply()
                                            Toast.makeText(context, "Chat lock removed for @$friendHandleClean", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onToggleChatMute = { checked ->
                                    isChatMuted = checked
                                    val currentMuted = notifPrefs.getStringSet("muted_channels", emptySet())?.toMutableSet() ?: mutableSetOf()
                                    if (checked) {
                                        currentMuted.add(friendHandleClean.lowercase())
                                        currentMuted.add(fId.lowercase())
                                        Toast.makeText(context, "Muted notifications from @$friendHandleClean", Toast.LENGTH_SHORT).show()
                                    } else {
                                        currentMuted.removeAll { it.lowercase() == friendHandleClean.lowercase() || it.lowercase() == fId.lowercase() }
                                        Toast.makeText(context, "Unmuted notifications from @$friendHandleClean", Toast.LENGTH_SHORT).show()
                                    }
                                    notifPrefs.edit().putStringSet("muted_channels", currentMuted).apply()
                                },
                                onSelectWallpaper = { wpId ->
                                    if (friendObj.isGroup && !isGroupAdmin) {
                                        Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                                        return@FriendProfileScreen
                                    }
                                    chatWallpaperId = wpId
                                    customWallpaperUri = null
                                    val chatKey = getCanonicalChatId(friendObj)
                                    val userPrefix = myUser
                                    val cleanHandle = friendObj.handle.trim().removePrefix("@").lowercase()
                                    val editor = friendPrefs.edit()
                                    if (chatKey.isNotBlank()) {
                                        editor.putString("${userPrefix}_chat_wallpaper_$chatKey", wpId)
                                            .remove("${userPrefix}_chat_wallpaper_custom_$chatKey")
                                    }
                                    if (cleanHandle.isNotBlank()) {
                                        editor.putString("${userPrefix}_chat_wallpaper_$cleanHandle", wpId)
                                            .remove("${userPrefix}_chat_wallpaper_custom_$cleanHandle")
                                    }
                                    editor.putString("${userPrefix}_chat_wallpaper_$fId", wpId)
                                        .remove("${userPrefix}_chat_wallpaper_custom_$fId")
                                        .remove("chat_wallpaper_custom_$fId")
                                        .remove("chat_wallpaper_$fId")
                                        .apply()
                                },
                                onPickCustomWallpaper = {
                                    if (friendObj.isGroup && !isGroupAdmin) {
                                        Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                                        return@FriendProfileScreen
                                    }
                                    wallpaperPickerLauncher.launch("image/*")
                                },
                                onRemoveCustomWallpaper = {
                                    if (friendObj.isGroup && !isGroupAdmin) {
                                        Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                                        return@FriendProfileScreen
                                    }
                                    chatWallpaperId = "default"
                                    customWallpaperUri = null
                                    val chatKey = getCanonicalChatId(friendObj)
                                    val userPrefix = myUser
                                    val cleanHandle = friendObj.handle.trim().removePrefix("@").lowercase()
                                    val editor = friendPrefs.edit()
                                    if (chatKey.isNotBlank()) {
                                        editor.putString("${userPrefix}_chat_wallpaper_$chatKey", "default")
                                            .remove("${userPrefix}_chat_wallpaper_custom_$chatKey")
                                    }
                                    if (cleanHandle.isNotBlank()) {
                                        editor.putString("${userPrefix}_chat_wallpaper_$cleanHandle", "default")
                                            .remove("${userPrefix}_chat_wallpaper_custom_$cleanHandle")
                                    }
                                    editor.remove("${userPrefix}_chat_wallpaper_custom_$fId")
                                        .putString("${userPrefix}_chat_wallpaper_$fId", "default")
                                        .remove("chat_wallpaper_custom_$fId")
                                        .remove("chat_wallpaper_$fId")
                                        .apply()
                                },
                                onUnfriend = {
                                    showUnfriendConfirmDialog = true
                                },
                                onInitiateVoiceCall = {
                                    initiateVoiceCallWithFriend(friendObj)
                                },
                                onOpenDirectChat = {
                                    isFriendProfileOpen = false
                                    openedProfileFromChatList = false
                                },
                                onViewPhoto = {
                                    showPhotoLightbox = true
                                },
                                isAdmin = isGroupAdmin
                            )
                        }
                    }
                    "DIRECT_CHAT" -> {
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
                            val resolvedContentScale = when (wallpaperTransform.contentScaleMode) {
                                "fit" -> ContentScale.Fit
                                "fill_bounds" -> ContentScale.FillBounds
                                "inside" -> ContentScale.Inside
                                else -> ContentScale.Crop
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clipToBounds()
                            ) {
                                val customWallpaperModel = remember(customWallpaperUri) {
                                    customWallpaperUri?.let { uriStr ->
                                        if (uriStr.startsWith("file://")) {
                                            java.io.File(uriStr.removePrefix("file://"))
                                        } else if (uriStr.startsWith("/")) {
                                            java.io.File(uriStr)
                                        } else {
                                            android.net.Uri.parse(uriStr)
                                        }
                                    }
                                }

                                AsyncImage(
                                    model = customWallpaperModel,
                                    contentDescription = null,
                                    contentScale = resolvedContentScale,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = wallpaperTransform.scale
                                            scaleY = wallpaperTransform.scale
                                            translationX = wallpaperTransform.offsetX
                                            translationY = wallpaperTransform.offsetY
                                        }
                                        .then(
                                            if (wallpaperTransform.blur > 0f) Modifier.blur(wallpaperTransform.blur.dp) else Modifier
                                        )
                                )
                                // Legibility Scrim Overlay so chat bubbles and text remain crisp
                                val scrimAlpha = (wallpaperTransform.dim * 0.5f).coerceIn(0f, 0.45f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isLight) Color.White.copy(alpha = scrimAlpha * 0.7f)
                                            else Color.Black.copy(alpha = scrimAlpha)
                                        )
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clipToBounds()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = wallpaperTransform.scale
                                            scaleY = wallpaperTransform.scale
                                            translationX = wallpaperTransform.offsetX
                                            translationY = wallpaperTransform.offsetY
                                        }
                                        .then(
                                            if (wallpaperTransform.blur > 0f) Modifier.blur(wallpaperTransform.blur.dp) else Modifier
                                        )
                                        .then(
                                            if (chatWallpaperId != "default") {
                                                if (chatWallpaperId == "oled") {
                                                    Modifier.background(Color(0xFF000000))
                                                } else {
                                                    Modifier.background(
                                                        brush = if (isLight) activeChatWallpaper.lightBrush else activeChatWallpaper.darkBrush
                                                    )
                                                }
                                            } else {
                                                if (isLiquidGlassTheme) {
                                                    Modifier.liquidGlassCanvasMesh()
                                                } else if (isGlassTheme) {
                                                    Modifier.glassCanvasMesh()
                                                } else if (isPureBlack) {
                                                    Modifier.background(Color(0xFF000000))
                                                } else {
                                                    Modifier.background(
                                                        brush = if (isLight) activeChatWallpaper.lightBrush else activeChatWallpaper.darkBrush
                                                    )
                                                }
                                            }
                                        )
                                )
                                if (chatWallpaperId != "default" && chatWallpaperId != "oled") {
                                    val scrimAlpha = (wallpaperTransform.dim * 0.35f).coerceIn(0f, 0.4f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                if (isLight) Color.White.copy(alpha = scrimAlpha * 0.5f)
                                                else Color.Black.copy(alpha = scrimAlpha)
                                            )
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Pinned Message Banner at top of chat
                            AnimatedVisibility(
                                visible = pinnedMessageForChat != null,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                val pinned = pinnedMessageForChat
                                if (pinned != null) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                val targetIdx = friendMessages.indexOfFirst { it.id == pinned.messageId }
                                                if (targetIdx != -1) {
                                                    coroutineScope.launch {
                                                        listState.animateScrollToItem(targetIdx)
                                                    }
                                                }
                                            },
                                        color = if (isLiquidGlassTheme || isGlassTheme) {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                                        } else if (isPureBlack) {
                                            Color(0xFF18181B)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        tonalElevation = 2.dp,
                                        shadowElevation = 2.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PushPin,
                                                    contentDescription = "Pinned",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "PINNED MESSAGE",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            letterSpacing = 0.8.sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    if (pinned.authorName.isNotBlank()) {
                                                        Text(
                                                            text = " • ${pinned.authorName}",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Medium
                                                            ),
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = pinned.content,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        val currentFriend = selectedFriend
                                                        val canonicalChatId = getCanonicalChatId(currentFriend)
                                                        val activeChan = activeDmChannelId ?: (if (currentFriend?.isGroup == true) currentFriend.id else canonicalChatId)
                                                        pinnedMessageForChat = null
                                                        if (currentFriend != null) {
                                                            val unpinKey = if (currentFriend.isGroup) currentFriend.id else canonicalChatId.lowercase()
                                                            friendPrefs.edit()
                                                                .remove("pinned_msg_${unpinKey}")
                                                                .remove("pinned_content_${unpinKey}")
                                                                .remove("pinned_author_${unpinKey}")
                                                                .remove("pinned_msg_${currentFriend.id}")
                                                                .remove("pinned_content_${currentFriend.id}")
                                                                .remove("pinned_author_${currentFriend.id}")
                                                                .apply()
                                                        }
                                                        if (activeChan.isNotBlank()) {
                                                            coroutineScope.launch {
                                                                ConnectoApiClient.unpinMessage(activeChan)
                                                            }
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Unpin",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                            if (isLoadingMessages && friendMessages.isEmpty()) {
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
                                        var dragOffsetX by remember(msg.id) { mutableStateOf(0f) }
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
                                                    friendText = friendBubbleText,
                                                    isSelected = selectedMessageForAction?.id == msg.id,
                                                    onMessageClick = {
                                                        if (selectedMessageForAction != null) {
                                                            selectedMessageForAction = if (selectedMessageForAction?.id == msg.id) null else msg
                                                        }
                                                    },
                                                    onMessageLongPress = { targetMsg ->
                                                        selectedMessageForAction = targetMsg
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    if (isFriendTyping) {
                                        item(key = "typing_indicator") {
                                            AnimatedTypingIndicator(
                                                friendName = selectedFriend?.name ?: "Friend",
                                                accentColor = activeChatTheme.primaryColor,
                                                bubbleBg = friendBubbleBg,
                                                borderColor = friendBubbleBorder
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
                                                .background(
                                                    if (isLiquidGlassTheme) {
                                                        val alpha = ConnectoTheme.glassAlpha
                                                        if (alpha <= 0.005f) Color.Transparent
                                                        else Color.White.copy(alpha = (alpha * 0.20f).coerceIn(0.02f, 0.30f))
                                                    } else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isLiquidGlassTheme) {
                                                        val alpha = ConnectoTheme.glassAlpha
                                                        val rimAlpha = if (alpha <= 0.005f) 0.50f else (0.55f - alpha * 0.15f).coerceIn(0.25f, 0.60f)
                                                        Color.White.copy(alpha = rimAlpha)
                                                    } else MaterialTheme.colorScheme.outline,
                                                    RoundedCornerShape(14.dp)
                                                )
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
                                    onClose = { showEmojiPicker = false },
                                    userId = com.example.connecto.network.ConnectoApiClient.currentUserId ?: currentUsername,
                                    onBackspace = {
                                        if (typedMessage.isNotEmpty()) {
                                            val lastCodePoint = typedMessage.codePointBefore(typedMessage.length)
                                            val charCount = Character.charCount(lastCodePoint)
                                            typedMessage = typedMessage.dropLast(charCount)
                                        }
                                    }
                                )
                            }
                            } // end DisableSelection
                        }

                        // ================= 3. ADVANCED CHAT INPUT BAR & SEND BUTTON =================
                        val isAcceptedFriend = selectedFriend == null || selectedFriend?.isGroup == true || friendsList.any {
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
                                            .background(
                                                if (isLiquidGlassTheme) {
                                                    val alpha = ConnectoTheme.glassAlpha
                                                    if (alpha <= 0.005f) Color.Transparent
                                                    else Color.White.copy(alpha = (alpha * 0.25f).coerceIn(0.01f, 0.35f))
                                                }
                                                else if (isPureBlack) Color(0x1AFFFFFF)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .border(
                                                1.dp,
                                                if (isLiquidGlassTheme) {
                                                    val alpha = ConnectoTheme.glassAlpha
                                                    val rimAlpha = if (alpha <= 0.005f) 0.50f else (0.60f - alpha * 0.20f).coerceIn(0.25f, 0.65f)
                                                    Color.White.copy(alpha = rimAlpha)
                                                }
                                                else if (isPureBlack) Color(0x40FFFFFF)
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                RoundedCornerShape(12.dp)
                                            )
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

                            // Active Editing Message Banner
                            AnimatedVisibility(
                                visible = editingMessage != null,
                                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(180)) + expandVertically(),
                                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(tween(150)) + shrinkVertically()
                            ) {
                                editingMessage?.let { editTarget ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isLiquidGlassTheme) {
                                                    val alpha = ConnectoTheme.glassAlpha
                                                    if (alpha <= 0.005f) Color.Transparent
                                                    else Color.White.copy(alpha = (alpha * 0.25f).coerceIn(0.01f, 0.35f))
                                                }
                                                else if (isPureBlack) Color(0x1AFFFFFF)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .border(
                                                1.dp,
                                                if (isLiquidGlassTheme) {
                                                    val alpha = ConnectoTheme.glassAlpha
                                                    val rimAlpha = if (alpha <= 0.005f) 0.50f else (0.60f - alpha * 0.20f).coerceIn(0.25f, 0.65f)
                                                    Color.White.copy(alpha = rimAlpha)
                                                }
                                                else if (isPureBlack) Color(0x40FFFFFF)
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                RoundedCornerShape(12.dp)
                                            )
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
                                                text = "Editing message",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = editTarget.content,
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
                                                .clickable {
                                                    editingMessage = null
                                                    typedMessage = ""
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cancel edit",
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
                                    .background(
                                        if (isLiquidGlassTheme) {
                                            val alpha = ConnectoTheme.glassAlpha
                                            if (alpha <= 0.005f) Color.Transparent
                                            else Color.White.copy(alpha = (alpha * 0.28f).coerceIn(0.01f, 0.40f))
                                        }
                                        else if (isPureBlack) Color(0x1AFFFFFF)
                                        else if (isGlassTheme) Color(0xD9111827)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = if ((isLiquidGlassTheme || isGlassTheme || isPureBlack) && isInputFocused) 1.5.dp else 1.dp,
                                        brush = if (isLiquidGlassTheme) {
                                            val alpha = ConnectoTheme.glassAlpha
                                            val rimAlpha = if (alpha <= 0.005f) (if (isInputFocused) 0.85f else 0.55f) else (if (isInputFocused) 0.95f else (0.65f - alpha * 0.20f).coerceIn(0.35f, 0.70f))
                                            Brush.linearGradient(
                                                listOf(
                                                    Color.White.copy(alpha = rimAlpha),
                                                    Color.White.copy(alpha = rimAlpha * 0.40f)
                                                )
                                            )
                                        } else if (isPureBlack) {
                                            Brush.linearGradient(
                                                listOf(
                                                    if (isInputFocused) Color.White else Color(0x66FFFFFF),
                                                    if (isInputFocused) Color(0x80FFFFFF) else Color(0x1AFFFFFF)
                                                )
                                            )
                                        } else if (isGlassTheme) {
                                            Brush.linearGradient(
                                                listOf(
                                                    if (isInputFocused) Color(0xEE38BDF8) else Color(0x8038BDF8),
                                                    if (isInputFocused) Color(0x8038BDF8) else Color(0x20334A6E)
                                                )
                                            )
                                        } else {
                                            SolidColor(if (isInputFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                        },
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
                                        .clickable {
                                            if (!showEmojiPicker) {
                                                focusManager.clearFocus()
                                            }
                                            showEmojiPicker = !showEmojiPicker
                                        },
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
                                if (editingMessage != null) {
                                    val editTarget = editingMessage!!
                                    val newContent = typedMessage.trim()
                                    if (newContent.isNotBlank()) {
                                        val targetId = editTarget.id
                                        friendMessages = friendMessages.map { if (it.id == targetId) it.copy(content = newContent) else it }
                                        coroutineScope.launch(Dispatchers.IO) {
                                            dbHelper.updateMessageContent(targetId, newContent)
                                        }
                                        val currentFriend = selectedFriend
                                        val canonicalChatId = getCanonicalChatId(currentFriend)
                                        val targetChan = activeDmChannelId ?: (if (currentFriend?.isGroup == true) currentFriend.id else canonicalChatId)
                                        coroutineScope.launch {
                                            ConnectoApiClient.editMessage(targetId, newContent, targetChan.ifBlank { null })
                                        }
                                    }
                                    editingMessage = null
                                    typedMessage = ""
                                } else if (selectedFriend != null && !isAcceptedFriend) {
                                    Toast.makeText(context, "Direct messaging is restricted to accepted friends.", Toast.LENGTH_SHORT).show()
                                } else if (typedMessage.isNotBlank()) {
                                    val userMsg = typedMessage.trim()
                                    typedMessage = ""
                                    selectedFriend?.let { messageDrafts.remove(it.id) }
                                    showEmojiPicker = false

                                    val typingRecipient = selectedFriend?.handle?.removePrefix("@") ?: selectedFriend?.name ?: selectedFriend?.id
                                    val typingTarget = activeDmChannelId ?: typingRecipient
                                    if (typingTarget != null) {
                                        VoiceCallManager.sendTypingStop(typingTarget, typingRecipient)
                                    }

                                    val myName = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@")
                                    val tempId = "opt-${UUID.randomUUID()}"
                                    val currentReply = replyingTo
                                    replyingTo = null // Clear reply banner immediately

                                    val isCurGroup = selectedFriend?.isGroup == true
                                    val isRecipientOnline = selectedFriend?.isOnline == true
                                    val initialDelivered = if (isCurGroup) true else isRecipientOnline
                                    val nowMillis = System.currentTimeMillis()

                                    val localItem = CustomMessageItem(
                                        id = tempId,
                                        senderName = myName,
                                        initial = userInitial,
                                        content = userMsg,
                                        time = "Just now",
                                        isMe = true,
                                        isDelivered = initialDelivered,
                                        isRead = false,
                                        avatarUrl = ConnectoApiClient.currentUserAvatarUrl,
                                        replyToId = currentReply?.id,
                                        replyToContent = currentReply?.content,
                                        replyToAuthor = currentReply?.senderName,
                                        isEncrypted = false,
                                        timestampMillis = nowMillis
                                    )
                                    val updatedList = (friendMessages + localItem).sortedBy { it.timestampMillis }
                                    friendMessages = updatedList
                                    isLoadingMessages = false

                                    // Move current friend/group to index 0 of conversations list with latest message and timestamp
                                    selectedFriend?.let { friend ->
                                        val fIdx = friendsList.indexOfFirst {
                                            it.id == friend.id || it.handle.removePrefix("@").equals(friend.handle.removePrefix("@"), ignoreCase = true)
                                        }
                                        if (fIdx != -1) {
                                            val updated = friendsList[fIdx].copy(
                                                lastMessage = userMsg,
                                                timeAgo = "Just now",
                                                unreadCount = 0,
                                                lastMessageTimestamp = nowMillis
                                            )
                                            friendsList.removeAt(fIdx)
                                            friendsList.add(0, updated)
                                            val cleanHandle = updated.handle.removePrefix("@").trim().lowercase()
                                            readPrefs.edit()
                                                .putString("last_msg_${updated.id}", userMsg)
                                                .putString("last_msg_${cleanHandle}", userMsg)
                                                .putString("last_time_${updated.id}", "Just now")
                                                .putString("last_time_${cleanHandle}", "Just now")
                                                .putLong("last_timestamp_${updated.id}", nowMillis)
                                                .putLong("last_timestamp_${cleanHandle}", nowMillis)
                                                .apply()
                                            coroutineScope.launch(Dispatchers.IO) {
                                                try {
                                                    dbHelper.updateFriendLastMessageAndUnread(updated.id, userMsg, "Just now", 0, nowMillis)
                                                } catch (_: Exception) {}
                                            }
                                        } else {
                                            val gIdx = userGroupsList.indexOfFirst { it.id == friend.id }
                                            if (gIdx != -1) {
                                                val updated = userGroupsList[gIdx].copy(
                                                    lastMessage = userMsg,
                                                    timeAgo = "Just now",
                                                    unreadCount = 0,
                                                    lastMessageTimestamp = nowMillis
                                                )
                                                userGroupsList.removeAt(gIdx)
                                                userGroupsList.add(0, updated)
                                                readPrefs.edit()
                                                    .putString("last_msg_${updated.id}", userMsg)
                                                    .putString("last_time_${updated.id}", "Just now")
                                                    .putLong("last_timestamp_${updated.id}", nowMillis)
                                                    .apply()
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    try {
                                                        dbHelper.updateGroupLastMessage(updated.id, userMsg, "Just now", nowMillis)
                                                    } catch (_: Exception) {}
                                                }
                                            }
                                        }
                                    }

                                    // Instantly scroll to the bottom on sending message - zero lag, zero page load
                                    coroutineScope.launch {
                                        try { listState.scrollToItem(updatedList.size) } catch (_: Exception) {}
                                    }

                                    coroutineScope.launch {
                                        try {
                                            val friend = selectedFriend ?: return@launch
                                            val isGrp = friend.isGroup
                                            val cleanTarget = friend.handle.removePrefix("@").trim()
                                            val myUser = myName.lowercase()
                                            val canonicalName = if (isGrp) friend.id else "dm-${minOf(myUser, cleanTarget.lowercase())}-${maxOf(myUser, cleanTarget.lowercase())}"

                                            // Persist optimistic message immediately to local SQLite database so it never disappears across screens
                                            withContext(Dispatchers.IO) {
                                                val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())
                                                val optDto = MessageDto(
                                                    id = tempId,
                                                    channelId = if (isGrp) friend.id else canonicalName,
                                                    authorId = myName,
                                                    authorName = myName,
                                                    content = userMsg,
                                                    createdAt = nowIso,
                                                    authorAvatar = ConnectoApiClient.currentUserAvatarUrl,
                                                    replyToId = currentReply?.id,
                                                    replyToContent = currentReply?.content,
                                                    replyToAuthor = currentReply?.senderName
                                                )
                                                dbHelper.saveMessage(optDto)
                                                dbHelper.saveMessages(if (isGrp) friend.id else canonicalName, listOf(optDto))
                                            }

                                            val targetChannelId = if (isGrp) {
                                                activeDmChannelId ?: friend.id
                                            } else {
                                                activeDmChannelId ?: ConnectoApiClient.channelCache[canonicalName.lowercase()] ?: canonicalName
                                            }

                                            val sendRes = ConnectoApiClient.sendMessage(
                                                channelId = targetChannelId,
                                                content = userMsg,
                                                replyToId = currentReply?.id,
                                                replyToContent = currentReply?.content,
                                                replyToAuthor = currentReply?.senderName
                                            )
                                            if (sendRes.isSuccess) {
                                                val sentDto = sendRes.getOrThrow()
                                                withContext(Dispatchers.IO) {
                                                    dbHelper.deleteMessage(tempId)
                                                    dbHelper.saveMessage(sentDto)
                                                    if (isGrp) {
                                                        dbHelper.saveMessages(targetChannelId, listOf(sentDto))
                                                    } else {
                                                        dbHelper.saveMessages(canonicalName, listOf(sentDto))
                                                        if (targetChannelId.isNotBlank() && targetChannelId != canonicalName) {
                                                            dbHelper.saveMessages(targetChannelId, listOf(sentDto))
                                                        }
                                                    }
                                                }
                                                val sentMillis = parseMessageTimestamp(sentDto.createdAt)
                                                val realItem = CustomMessageItem(
                                                    id = sentDto.id,
                                                    senderName = sentDto.authorName,
                                                    initial = userInitial,
                                                    content = userMsg,
                                                    time = formatIsoTime(sentDto.createdAt),
                                                    isMe = true,
                                                    isDelivered = if (isGrp) true else (selectedFriend?.isOnline == true || initialDelivered),
                                                    isRead = false,
                                                    avatarUrl = ConnectoApiClient.currentUserAvatarUrl,
                                                    replyToId = sentDto.replyToId ?: currentReply?.id,
                                                    replyToContent = sentDto.replyToContent ?: currentReply?.content,
                                                    replyToAuthor = sentDto.replyToAuthor ?: currentReply?.senderName,
                                                    isEncrypted = false,
                                                    timestampMillis = if (sentMillis > 0) sentMillis else nowMillis
                                                )
                                                val curList = friendMessages.toMutableList()
                                                val optIdx = curList.indexOfFirst { it.id == tempId }
                                                if (optIdx != -1) {
                                                    curList[optIdx] = realItem.copy(
                                                        reaction = curList[optIdx].reaction ?: realItem.reaction,
                                                        isDelivered = curList[optIdx].isDelivered || realItem.isDelivered
                                                    )
                                                } else if (curList.none { it.id == realItem.id }) {
                                                    curList.add(realItem)
                                                }
                                                friendMessages = deduplicateCustomMessages(curList)
                                                coroutineScope.launch {
                                                    try { listState.animateScrollToItem(friendMessages.size) } catch (_: Exception) {}
                                                }
                                            } else {
                                                // Server delivery failed: keep local message in friendMessages with delivery failure indicator
                                                val curList = friendMessages.toMutableList()
                                                val optIdx = curList.indexOfFirst { it.id == tempId }
                                                if (optIdx != -1) {
                                                    curList[optIdx] = curList[optIdx].copy(isDelivered = false)
                                                    friendMessages = curList.sortedBy { it.timestampMillis }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Local message remains visible in friendMessages with isDelivered = false
                                            val curList = friendMessages.toMutableList()
                                            val optIdx = curList.indexOfFirst { it.id == tempId }
                                            if (optIdx != -1) {
                                                curList[optIdx] = curList[optIdx].copy(isDelivered = false)
                                                friendMessages = curList.sortedBy { it.timestampMillis }
                                            }
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
                                        if (typedMessage.isNotBlank()) {
                                            if (isPureBlack || isLiquidGlassTheme) Brush.linearGradient(listOf(Color.White, Color(0xFFE4E4E7)))
                                            else if (isGlassTheme) Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
                                            else Brush.linearGradient(gradientColors)
                                        } else {
                                            if (isPureBlack || isLiquidGlassTheme) Brush.verticalGradient(listOf(Color(0x26FFFFFF), Color(0x14FFFFFF)))
                                            else if (isGlassTheme) Brush.verticalGradient(listOf(Color(0xD91E293B), Color(0xB3111827)))
                                            else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                        }
                                    )
                                    .border(
                                        width = if (isLiquidGlassTheme || isGlassTheme || isPureBlack) 1.2.dp else 1.dp,
                                        brush = if (isPureBlack || isLiquidGlassTheme) {
                                            Brush.linearGradient(
                                                listOf(
                                                    if (typedMessage.isNotBlank()) Color.White else Color(0x66FFFFFF),
                                                    if (typedMessage.isNotBlank()) Color(0x80FFFFFF) else Color(0x26FFFFFF)
                                                )
                                            )
                                        } else if (isGlassTheme) {
                                            Brush.linearGradient(
                                                listOf(
                                                    if (typedMessage.isNotBlank()) Color(0xFFE0F2FE) else Color(0x6638BDF8),
                                                    if (typedMessage.isNotBlank()) Color(0x8038BDF8) else Color(0x2038BDF8)
                                                )
                                            )
                                        } else {
                                            SolidColor(if (typedMessage.isNotBlank()) Color.Transparent else MaterialTheme.colorScheme.outline)
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable(enabled = typedMessage.isNotBlank()) {
                                        onSendMessage()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (typedMessage.isNotBlank()) {
                                        if (isPureBlack) Color.Black else contentOnGradient
                                    } else {
                                        if (isPureBlack || isLiquidGlassTheme) Color(0x66FFFFFF)
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    } // end Wallpaper Box
                }
                } // end when (screenState)
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
    isSelected: Boolean = false,
    onMessageClick: (() -> Unit)? = null,
    onMessageLongPress: (CustomMessageItem) -> Unit = {},
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

    val isIncoming = !message.isMe
    var hasAppeared by remember(message.id) { mutableStateOf(false) }
    LaunchedEffect(message.id) {
        hasAppeared = true
    }

    val incomingScale by animateFloatAsState(
        targetValue = if (hasAppeared) 1.0f else 0.82f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "incomingScale"
    )
    val incomingAlpha by animateFloatAsState(
        targetValue = if (hasAppeared) 1.0f else 0.0f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "incomingAlpha"
    )
    val incomingTranslateY by animateFloatAsState(
        targetValue = if (hasAppeared) 0f else 14f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "incomingTranslateY"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (message.isMe) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 44.dp)
                    .pointerInput(message.id) {
                        detectTapGestures(
                            onTap = { onMessageClick?.invoke() },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMessageLongPress(message)
                            }
                        )
                    }
            )
        }

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
            if (!message.isMe && message.senderName.isNotBlank() && !message.senderName.equals("gamer", ignoreCase = true)) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            val isGlassTheme = ConnectoTheme.isGlass
            val isLiquidGlassTheme = ConnectoTheme.isLiquidGlass
            val isPureBlack = ConnectoTheme.isPureBlack
            val bubbleShape = RoundedCornerShape(22.dp)

            val isWhiteBubble = ownBg == Color.White || ownBg == Color(0xFFFAFAFA)
            val effectiveOwnBg = ownBg
            val effectiveOwnText = ownText

            val bubbleModifier = if (isGlassTheme || isLiquidGlassTheme) {
                Modifier
                    .graphicsLayer {
                        scaleX = bubbleScale
                        scaleY = bubbleScale
                    }
                    .glassBubble(
                        isMe = message.isMe,
                        shape = bubbleShape,
                        isPressed = isBubblePressed,
                        isLiquid = isLiquidGlassTheme
                    )
            } else {
                val bgBrush = if (message.isMe) {
                    if (isWhiteBubble) {
                        SolidColor(Color.White)
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                effectiveOwnBg,
                                Color(
                                    red = (effectiveOwnBg.red * 0.85f).coerceIn(0f, 1f),
                                    green = (effectiveOwnBg.green * 0.85f).coerceIn(0f, 1f),
                                    blue = (effectiveOwnBg.blue * 0.95f).coerceIn(0f, 1f),
                                    alpha = 1f
                                )
                            ),
                            start = Offset.Zero,
                            end = Offset(300f, 300f)
                        )
                    }
                } else {
                    if (friendBg == Color(0xFF18181B) || friendBg == Color(0xFF1E1E22)) {
                        SolidColor(friendBg)
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                friendBg,
                                friendBg.copy(alpha = 0.92f)
                            )
                        )
                    }
                }

                val borderBrush = if (message.isMe) {
                    if (isWhiteBubble) {
                        SolidColor(Color(0x1F000000))
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.40f),
                                effectiveOwnText.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            start = Offset.Zero,
                            end = Offset(220f, 220f)
                        )
                    }
                } else {
                    SolidColor(friendBorder)
                }

                Modifier
                    .graphicsLayer {
                        scaleX = if (isIncoming) incomingScale else bubbleScale
                        scaleY = if (isIncoming) incomingScale else bubbleScale
                        alpha = if (isIncoming) incomingAlpha else 1.0f
                        translationY = if (isIncoming) incomingTranslateY else 0f
                    }
                    .shadow(
                        elevation = if (message.isMe) 2.dp else 1.dp,
                        shape = bubbleShape,
                        spotColor = if (message.isMe && !isWhiteBubble) effectiveOwnBg.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f),
                        ambientColor = Color.Black.copy(alpha = 0.30f)
                    )
                    .clip(bubbleShape)
                    .background(bgBrush)
                    .border(
                        width = 1.dp,
                        brush = borderBrush,
                        shape = bubbleShape
                    )
            }

            val selectionBorder = if (isSelected) {
                Modifier
                    .border(2.dp, MaterialTheme.colorScheme.primary, bubbleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), bubbleShape)
            } else Modifier

            Box(
                modifier = bubbleModifier
                    .then(selectionBorder)
                    .pointerInput(message.id) {
                        detectTapGestures(
                            onTap = {
                                if (showReactionPill) {
                                    showReactionPill = false
                                } else {
                                    onMessageClick?.invoke()
                                }
                            },
                            onDoubleTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showReactionPill = true
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMessageLongPress(message)
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
                                    if (message.isMe) {
                                        if (isWhiteBubble) Color(0x0F000000) else effectiveOwnText.copy(alpha = 0.12f)
                                    } else friendText.copy(alpha = 0.08f)
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (message.isMe) {
                                            if (isWhiteBubble) Color(0xCC000000) else effectiveOwnText.copy(alpha = 0.8f)
                                        } else Color.White
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                if (!message.replyToAuthor.isNullOrBlank()) {
                                    Text(
                                        text = message.replyToAuthor,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (message.isMe) {
                                            if (isWhiteBubble) Color(0xEE000000) else effectiveOwnText.copy(alpha = 0.9f)
                                        } else Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = message.replyToContent,
                                    fontSize = 11.sp,
                                    color = if (message.isMe) {
                                        if (isWhiteBubble) Color(0xBB000000) else effectiveOwnText.copy(alpha = 0.75f)
                                    } else friendText.copy(alpha = 0.75f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Text(
                        text = message.content,
                        color = if (message.isMe) effectiveOwnText else friendText,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isMe && !message.isRead) {
                            Text(
                                text = "1",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFB300), // Distinct amber unread indicator "1"
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = formatIsoTime(message.time),
                            fontSize = 10.sp,
                            color = if (message.isMe) {
                                if (isWhiteBubble) Color(0x99000000) else effectiveOwnText.copy(alpha = 0.70f)
                            } else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (message.isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val isDelivered = message.isDelivered
                            val isRead = message.isRead
                            val tickIcon = if (isRead || isDelivered) Icons.Default.DoneAll else Icons.Default.Check
                            val tickTint = if (isRead) {
                                Color(0xFF0284C7) // Read receipt (distinct azure double tick)
                            } else if (isDelivered) {
                                if (isWhiteBubble) Color(0xCC000000) else effectiveOwnText.copy(alpha = 0.85f) // Delivered (double tick)
                            } else {
                                if (isWhiteBubble) Color(0x99000000) else effectiveOwnText.copy(alpha = 0.70f) // Sent to server, recipient offline/data off (single tick)
                            }
                            Icon(
                                imageVector = tickIcon,
                                contentDescription = if (isRead) "Read" else if (isDelivered) "Delivered" else "Sent",
                                tint = tickTint,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Trailing thought bubble dots tail matching reference image
            if (message.isMe) {
                // Outgoing tail on bottom-right
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .offset(x = 3.dp, y = (-3).dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(effectiveOwnBg)
                        )
                        Box(
                            modifier = Modifier
                                .offset(x = 8.dp, y = (-1).dp)
                                .size(4.5.dp)
                                .clip(CircleShape)
                                .background(effectiveOwnBg)
                        )
                    }
                }
            } else {
                // Incoming tail on bottom-left
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Box(
                            modifier = Modifier
                                .offset(x = (-3).dp, y = (-3).dp)
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(friendBg)
                                .border(1.dp, friendBorder, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .offset(x = (-8).dp, y = (-1).dp)
                                .size(4.5.dp)
                                .clip(CircleShape)
                                .background(friendBg)
                                .border(1.dp, friendBorder, CircleShape)
                        )
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
                            .background(
                                if (isLiquidGlassTheme) {
                                    val alpha = ConnectoTheme.glassAlpha
                                    if (alpha <= 0.005f) Color.Transparent
                                    else Color.White.copy(alpha = (alpha * 0.30f).coerceIn(0.02f, 0.40f))
                                }
                                else if (isPureBlack) Color(0x26FFFFFF)
                                else if (isGlassTheme) Color(0xD91E293B)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                1.dp,
                                if (isLiquidGlassTheme) {
                                    val alpha = ConnectoTheme.glassAlpha
                                    val rimAlpha = if (alpha <= 0.005f) 0.55f else (0.65f - alpha * 0.20f).coerceIn(0.30f, 0.70f)
                                    Color.White.copy(alpha = rimAlpha)
                                }
                                else if (isPureBlack) Color(0x40FFFFFF)
                                else if (isGlassTheme) Color(0x8038BDF8)
                                else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = message.reaction!!, fontSize = 12.sp)
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.End)
                            .padding(2.dp)
                    )
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
                        .background(
                            if (isLiquidGlassTheme) {
                                val alpha = ConnectoTheme.glassAlpha
                                if (alpha <= 0.005f) {
                                    Brush.verticalGradient(listOf(Color(0x0DFFFFFF), Color.Transparent))
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = (alpha * 0.40f).coerceIn(0.04f, 0.50f)),
                                            Color.White.copy(alpha = (alpha * 0.20f).coerceIn(0.02f, 0.30f))
                                        )
                                    )
                                }
                            }
                            else if (isPureBlack) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x1AFFFFFF)))
                            else if (isGlassTheme) Brush.verticalGradient(listOf(Color(0xF0111827), Color(0xDD090D16)))
                            else SolidColor(MaterialTheme.colorScheme.surface)
                        )
                        .border(
                            width = 1.2.dp,
                            brush = if (isLiquidGlassTheme) {
                                val alpha = ConnectoTheme.glassAlpha
                                val rimAlpha = if (alpha <= 0.005f) 0.65f else (0.75f - alpha * 0.20f).coerceIn(0.35f, 0.85f)
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = rimAlpha),
                                        Color.White.copy(alpha = rimAlpha * 0.30f)
                                    )
                                )
                            }
                            else if (isPureBlack) Brush.linearGradient(listOf(Color(0x80FFFFFF), Color(0x20FFFFFF)))
                            else if (isGlassTheme) Brush.linearGradient(listOf(Color(0xCC38BDF8), Color(0x3338BDF8)))
                            else SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(16.dp)
                        )
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
        if (!message.isMe) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 44.dp)
                    .pointerInput(message.id) {
                        detectTapGestures(
                            onTap = { onMessageClick?.invoke() },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMessageLongPress(message)
                            }
                        )
                    }
            )
        }
    }
}

@Composable
fun AnimatedTypingIndicator(
    friendName: String = "",
    accentColor: Color = MaterialTheme.colorScheme.primary,
    bubbleBg: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typingTransition")

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1Alpha"
    )

    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2Alpha"
    )

    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3Alpha"
    )

    Column(
        modifier = modifier.padding(start = 12.dp, top = 4.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(bubbleBg)
                .border(1.dp, borderColor, RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = dot1Offset.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = dot1Alpha))
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot2Offset.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = dot2Alpha))
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot3Offset.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = dot3Alpha))
                )
            }
        }
        // Trailing thought bubble tail dots
        Box(
            modifier = Modifier
                .offset(x = 6.dp, y = (-2).dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(bubbleBg)
                .border(1.dp, borderColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = 1.dp)
                .size(4.5.dp)
                .clip(CircleShape)
                .background(bubbleBg)
                .border(1.dp, borderColor, CircleShape)
        )
    }
}
