package com.example.connecto.ui.screens

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.connecto.network.MessageDto
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.BackHandler
import androidx.compose.ui.zIndex
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.connecto.crypto.ConnectoE2EEncryption
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.material.icons.filled.MeetingRoom
import com.example.connecto.security.SecretVaultManager
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
import androidx.compose.material.icons.filled.Warning
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
import com.example.connecto.ui.components.SwipeableConversationRow
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.connecto.data.ConnectoDatabaseHelper
import com.example.connecto.data.WallpaperResolver
import com.example.connecto.data.WallpaperTransform
import com.example.connecto.data.WallpaperSyncManager
import com.example.connecto.ui.components.WallpaperCustomizerDialog
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import com.example.connecto.ui.components.ConnectoLongPressFocusOverlay
import androidx.compose.material.icons.filled.Tune
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import com.example.connecto.ui.components.ConnectoAnimatedSearchBar
import com.example.connecto.ui.components.ConnectoSearchPreferences
import com.example.connecto.ui.components.RecentSearchesAndFilterChips
import com.example.connecto.ui.components.SubstringHighlightedText
import com.example.connecto.ui.components.SearchEmptyState
import com.example.connecto.ui.components.InlineChatSearchBar
import com.example.connecto.network.CloudMessageSearchResult
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
        // Extract time part: convert UTC ISO/SQL timestamp to user's local timezone
        val millis = parseMessageTimestamp(trimmed)
        if (millis > 0L) {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }
            return sdf.format(Date(millis))
        }
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

fun cleanMessageContentForDisplay(rawText: String?, defaultPlaceholder: String = ""): String {
    if (rawText.isNullOrBlank() || rawText == "null" || rawText == "None") {
        return defaultPlaceholder
    }
    val trimmed = rawText.trim()

    // 1. Never leak raw ENC: or __E2E_AES_GCM__: ciphertext in UI
    if (ConnectoE2EEncryption.isEncrypted(trimmed) || trimmed.startsWith("__E2E_AES_GCM__:")) {
        return "🔒 Encrypted message"
    }

    // 2. Parse structured JSON payloads (attachments, images, audio, polls, calls)
    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
        try {
            val obj = org.json.JSONObject(trimmed)
            val type = obj.optString("type").lowercase()
            val fileUrl = obj.optString("file_url").ifEmpty { obj.optString("image_url").ifEmpty { obj.optString("url") } }
            val fileName = obj.optString("file_name").ifEmpty { obj.optString("filename").ifEmpty { obj.optString("name") } }
            val caption = obj.optString("caption").ifEmpty { obj.optString("text").ifEmpty { obj.optString("content") } }

            if (type == "image" || (!fileUrl.isNullOrBlank() && (fileUrl.endsWith(".jpg", true) || fileUrl.endsWith(".png", true) || fileUrl.endsWith(".jpeg", true) || fileUrl.endsWith(".webp", true)))) {
                return if (caption.isNotBlank()) "📷 Photo: $caption" else "📷 Photo"
            }
            if (type == "audio" || obj.has("audio_url")) {
                return "🎤 Voice message"
            }
            if (type == "poll" || obj.has("poll") || obj.has("question")) {
                val q = obj.optString("question").ifEmpty { obj.optJSONObject("poll")?.optString("question") ?: "Community Poll" }
                return "📊 Poll: $q"
            }
            if (type == "call" || obj.optString("action").startsWith("call:")) {
                return "📞 Voice call"
            }
            if (fileUrl.isNotBlank()) {
                val cleanName = if (fileName.isNotBlank()) fileName else "Attachment"
                return "📎 $cleanName"
            }
            if (caption.isNotBlank()) {
                return caption
            }
            if (obj.has("message") && obj.optString("message").isNotBlank()) {
                return obj.optString("message")
            }
            if (obj.has("title") && obj.optString("title").isNotBlank()) {
                return obj.optString("title")
            }
        } catch (_: Exception) {
            // Not valid JSON, continue with text sanitization
        }
    }

    // 3. Strip any raw HTML or server error page markup
    if (trimmed.startsWith("<!DOCTYPE", ignoreCase = true) || trimmed.contains("<html", ignoreCase = true)) {
        return "Server communication notice"
    }
    return trimmed
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
        subtitle = "Editorial Obsidian / Modern Indigo contrast",
        primaryColor = Color(0xFFFAFAFA),
        secondaryColor = Color(0xFFA1A1AA),
        ownBubbleBgLight = Color(0xFF4F46E5),
        ownBubbleBgDark = Color(0xFF3B4254),
        ownBubbleTextLight = Color(0xFFFFFFFF),
        ownBubbleTextDark = Color(0xFFFFFFFF),
        friendBubbleBgLight = Color(0xFFFFFFFF),
        friendBubbleBgDark = Color(0xFF18181B),
        friendBubbleTextLight = Color(0xFF0F172A),
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
        id = "royal_indigo",
        name = "Royal Indigo",
        subtitle = "Deep majestic indigo & sapphire stream",
        primaryColor = Color(0xFF4F46E5),
        secondaryColor = Color(0xFF6366F1),
        ownBubbleBgLight = Color(0xFF4338CA),
        ownBubbleBgDark = Color(0xFF3730A3),
        ownBubbleTextLight = Color(0xFFFFFFFF),
        ownBubbleTextDark = Color(0xFFFFFFFF),
        friendBubbleBgLight = Color(0xFFEEF2FF),
        friendBubbleBgDark = Color(0xFF1E1B4B),
        friendBubbleTextLight = Color(0xFF312E81),
        friendBubbleTextDark = Color(0xFFE0E7FF)
    ),
    ChatThemeOption(
        id = "nordic_slate",
        name = "Nordic Slate",
        subtitle = "Clean minimalist architectural slate",
        primaryColor = Color(0xFF334155),
        secondaryColor = Color(0xFF64748B),
        ownBubbleBgLight = Color(0xFF1E293B),
        ownBubbleBgDark = Color(0xFF334155),
        ownBubbleTextLight = Color(0xFFFFFFFF),
        ownBubbleTextDark = Color(0xFFFFFFFF),
        friendBubbleBgLight = Color(0xFFF1F5F9),
        friendBubbleBgDark = Color(0xFF0F172A),
        friendBubbleTextLight = Color(0xFF0F172A),
        friendBubbleTextDark = Color(0xFFF8FAFC)
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
        name = "Default",
        subtitle = "Clean theme canvas",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF0A0C10), Color(0xFF13161F)))
    ),
    ChatWallpaperOption(
        id = "glass_mesh",
        name = "Prism Glass",
        subtitle = "Aurora crystal glow",
        lightBrush = Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFFE0E7FF), Color(0xFFF1F5F9), Color(0xFFF8FAFC))),
        darkBrush = Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF1E3A8A), Color(0xFF0A0C10)))
    ),
    ChatWallpaperOption(
        id = "liquid_water",
        name = "Aqua Ripples",
        subtitle = "Oceanic fluid reflection",
        lightBrush = Brush.linearGradient(listOf(Color(0xFFE0F7FA), Color(0xFFE0F2FE), Color(0xFFF0FDF4), Color(0xFFF8FAFC))),
        darkBrush = Brush.linearGradient(listOf(Color(0xFF082F49), Color(0xFF0C4A6E), Color(0xFF0369A1), Color(0xFF0A101D)))
    ),
    ChatWallpaperOption(
        id = "oled",
        name = "True 0-Nit OLED",
        subtitle = "Pitch black luxury",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFAFAFA))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF000000)))
    ),
    ChatWallpaperOption(
        id = "mesh_dark",
        name = "Cosmic Mesh",
        subtitle = "Nebula violet slate",
        lightBrush = Brush.linearGradient(listOf(Color(0xFFEDE9FE), Color(0xFFF3E8FF), Color(0xFFF5F3FF), Color(0xFFF8FAFC))),
        darkBrush = Brush.linearGradient(listOf(Color(0xFF180828), Color(0xFF3B0764), Color(0xFF581C87), Color(0xFF1E1B4B), Color(0xFF0A0C10)))
    ),
    ChatWallpaperOption(
        id = "indigo_flow",
        name = "Indigo Gradient",
        subtitle = "Soft twilight stream",
        lightBrush = Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF), Color(0xFFC7D2FE), Color(0xFFEEF2FF))),
        darkBrush = Brush.linearGradient(listOf(Color(0xFF0F111A), Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF3730A3), Color(0xFF0A0C10)))
    ),
    ChatWallpaperOption(
        id = "emerald_glow",
        name = "Emerald Matrix",
        subtitle = "Tactical aurora matrix",
        lightBrush = Brush.linearGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5), Color(0xFFA7F3D0), Color(0xFFF0FDF4))),
        darkBrush = Brush.linearGradient(listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF051B14)))
    ),
    ChatWallpaperOption(
        id = "parchment_warm",
        name = "Pure White",
        subtitle = "Architectural white canvas",
        lightBrush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))),
        darkBrush = Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
    )
)

@Immutable
data class FriendItem(
    val id: String,
    val name: String,
    val handle: String,
    val initial: String,
    val status: String,
    val bio: String,
    val lastMessage: String,
    val timeAgo: String = "10:14 AM",
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val isGroup: Boolean = false,
    val isCreatedByMe: Boolean = false,
    val memberCount: Int = 0,
    val members: List<String> = emptyList(),
    val admin: String = "",
    val creator: String = "",
    val lastMessageTimestamp: Long = 0L,
    val lastSeen: String? = null,
    val seenBy: List<String> = emptyList(),
    val reactions: List<String> = emptyList()
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
    val timestampMillis: Long = System.currentTimeMillis(),
    val reactions: Map<String, List<String>> = emptyMap(),
    val seenBy: List<String> = emptyList(),
    val readAt: String? = null,
    val replyToMessageId: String? = null,
    val replyToSenderName: String? = null,
    val replyToText: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: String? = null,
    val isSilent: Boolean = false,
    val isWhisper: Boolean = false
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
private fun DirectChatOnlineBeacon(modifier: Modifier = Modifier) {
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
    Box(
        modifier = modifier
            .size(11.dp)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
                alpha = pulseAlpha
            }
            .clip(CircleShape)
            .background(OnlineGreen)
    )
    Box(
        modifier = modifier
            .size(11.dp)
            .clip(CircleShape)
            .background(OnlineGreen)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
    )
}

@Composable
fun ConnectoDeliverySendingRing(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sending_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Box(
        modifier = modifier
            .size(8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .border(1.2.dp, Color(0xFF64748B), CircleShape)
    )
}

@Composable
fun ConnectoDeliveryDeliveredDots(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(Color(0xFF38BDF8))
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(Color(0xFF38BDF8))
        )
    }
}

fun formatDynamicLastSeen(
    timestampMillis: Long?,
    lastSeenStr: String? = null,
    tickTrigger: Long = 0L
): String? {
    return try {
        val ts = timestampMillis ?: try {
            if (!lastSeenStr.isNullOrBlank()) {
                val s = lastSeenStr.trim()
                val cleanStr = if (s.contains(".")) s.substringBefore(".") else s.removeSuffix("Z")
                val format = if (cleanStr.contains("T")) {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                } else {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                }
                format.parse(cleanStr)?.time
            } else null
        } catch (_: Throwable) {
            null
        }

        if (ts == null || ts <= 0L) return null

        val now = System.currentTimeMillis()
        val diffMs = now - ts
        if (diffMs < 0L || diffMs < 60_000L) {
            return "Last seen just now"
        }
        val diffMin = diffMs / 60_000L
        if (diffMin < 60L) {
            return "Last seen ${diffMin}m ago"
        }

        val calNow = Calendar.getInstance()
        val calSeen = Calendar.getInstance().apply { timeInMillis = ts }

        val isSameDay = calNow.get(Calendar.YEAR) == calSeen.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calSeen.get(Calendar.DAY_OF_YEAR)

        calNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calNow.get(Calendar.YEAR) == calSeen.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calSeen.get(Calendar.DAY_OF_YEAR)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
        when {
            isSameDay -> "Last seen today at ${timeFormat.format(Date(ts))}"
            isYesterday -> "Last seen yesterday at ${timeFormat.format(Date(ts))}"
            else -> {
                val olderFormat = SimpleDateFormat("MMM d", Locale.US)
                "Last seen ${olderFormat.format(Date(ts))}"
            }
        }
    } catch (_: Throwable) {
        null
    }
}

fun formatLastSeenRelative(timestampMillis: Long?, lastSeenStr: String? = null): String {
    return try {
        formatDynamicLastSeen(timestampMillis, lastSeenStr) ?: "Direct Chat"
    } catch (_: Throwable) {
        "Direct Chat"
    }
}

fun evaluateIsUserOnline(
    username: String?,
    userId: String? = null,
    presenceMap: Map<String, Boolean> = emptyMap(),
    friends: List<FriendItem> = emptyList(),
    fallbackFriend: FriendItem? = null,
    myHideOnline: Boolean = false
): Boolean {
    // Reciprocity: if viewing user hid their online status, they cannot see other contacts' online status
    if (myHideOnline) return false

    val cleanUser = username?.trim()?.lowercase()?.removePrefix("@") ?: ""
    val cleanId = userId?.trim()?.lowercase() ?: ""

    // 1. Check live WebSocket presence state flow (highest precedence)
    if (cleanUser.isNotBlank() && presenceMap.containsKey(cleanUser)) {
        return presenceMap[cleanUser] == true
    }
    if (cleanId.isNotBlank() && presenceMap.containsKey(cleanId)) {
        return presenceMap[cleanId] == true
    }

    // 2. Check VoiceCallManager in-memory heartbeat / lastSeen within 45s
    if (cleanUser.isNotBlank() && VoiceCallManager.userPresenceMap.containsKey(cleanUser)) {
        return VoiceCallManager.isUserOnline(cleanUser)
    }
    if (cleanId.isNotBlank() && VoiceCallManager.userPresenceMap.containsKey(cleanId)) {
        return VoiceCallManager.isUserOnline(cleanId)
    }

    // 3. Fallback to friends list item from local cache
    val friendInList = friends.firstOrNull { f ->
        (cleanId.isNotBlank() && f.id.equals(cleanId, ignoreCase = true)) ||
        (cleanUser.isNotBlank() && f.handle.trim().lowercase().removePrefix("@") == cleanUser)
    }
    if (friendInList != null) return friendInList.isOnline

    // 4. Fallback friend snapshot if present
    if (fallbackFriend != null) return fallbackFriend.isOnline

    return false
}

@Composable
fun CustomizedChatScreen(
    currentUsername: String,
    onOpenProfile: () -> Unit,
    onUnreadStatusChanged: (Boolean) -> Unit = {},
    onUnreadCountChanged: (Int) -> Unit = {},
    onDirectChatStateChanged: (Boolean) -> Unit = {},
    onOpenNotifications: (() -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    targetChatUserOrChannel: String? = null,
    onTargetChatHandled: (() -> Unit)? = null,
    scrollToTopTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedFriend by remember { mutableStateOf<FriendItem?>(null) }
    var selectedFriendProfileData by remember { mutableStateOf<ProfileDataDto?>(null) }
    var isLoadingFriendProfile by remember { mutableStateOf(false) }
    val friendProfilesCache = remember { mutableStateMapOf<String, ProfileDataDto>() }
    var friendProfileSelectedTab by remember { mutableStateOf("settings") } // "settings", "customization", "actions"
    var isFriendProfileOpen by remember { mutableStateOf(false) }
    var friendProfileSubPage by remember { mutableStateOf<String?>(null) }
    var openedProfileFromChatList by remember { mutableStateOf(false) }
    var showFriendProfileDialog by remember { mutableStateOf(false) }
    var showGroupProfileDialog by remember { mutableStateOf(false) }
    var isRefreshingFriends by remember { mutableStateOf(false) }
    var showAddGroupMembersDialog by remember { mutableStateOf(false) }
    var showUnfriendConfirmDialog by remember { mutableStateOf(false) }
    var isUnfriending by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showCreateGroupChatDialog by remember { mutableStateOf(false) }
    var showCreateVoiceGroupDialog by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var pendingRequestsCount by remember { mutableIntStateOf(0) }
    var friendSearchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    var isDmSearchFocused by remember { mutableStateOf(false) }
    var selectedDmSearchFilter by remember { mutableStateOf("All") }
    var cloudMessageResults by remember { mutableStateOf<List<CloudMessageSearchResult>>(emptyList()) }
    var isCloudSearching by remember { mutableStateOf(false) }

    // In-Chat Search state
    var isChatSearchOpen by remember { mutableStateOf(false) }
    var chatSearchQuery by remember { mutableStateOf("") }
    var currentChatMatchIdx by remember { mutableIntStateOf(0) }
    var pulsingGlowMessageId by remember { mutableStateOf<String?>(null) }
    var typedMessage by remember { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }
    var isFriendTyping by remember { mutableStateOf(false) }
    var isLoadingMessages by remember { mutableStateOf(false) }
    val messageDrafts = remember { mutableStateMapOf<String, String>() }
    var replyingTo by remember { mutableStateOf<CustomMessageItem?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    // Active Call State & Presence
    val callState by VoiceCallManager.callState.collectAsState()
    val activeCallTitle by VoiceCallManager.activeCallTitle.collectAsState()
    val activeCallPartner by VoiceCallManager.activeCallPartner.collectAsState()
    val livePresenceMap by VoiceCallManager.presenceState.collectAsState()
    val apiHideOnline by ConnectoApiClient.isHideOnlineState.collectAsState()
    val apiHideLastSeen by ConnectoApiClient.isHideLastSeenState.collectAsState()
    val apiStealth by ConnectoApiClient.isStealthModeState.collectAsState()
    val profilePrefs = remember(currentUsername) { context.getSharedPreferences("connecto_profile", Context.MODE_PRIVATE) }
    val myCleanUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
    val myHideOnline = apiHideOnline || apiStealth || profilePrefs.getBoolean("hide_online_$myCleanUser", false) || profilePrefs.getBoolean("stealth_mode_$myCleanUser", false)
    val myHideLastSeen = apiHideLastSeen || apiStealth || profilePrefs.getBoolean("hide_last_seen_$myCleanUser", false) || profilePrefs.getBoolean("stealth_mode_$myCleanUser", false)

    var lastSeenTicker by remember { mutableStateOf(0L) }
    LaunchedEffect(selectedFriend?.id) {
        while (isActive) {
            kotlinx.coroutines.delay(30_000L)
            lastSeenTicker = System.currentTimeMillis()
        }
    }

    // Send Button Animation State
    var isSendPressed by remember { mutableStateOf(false) }
    val sendButtonScale by animateFloatAsState(
        targetValue = if (isSendPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "sendScale"
    )

    val readPrefs = remember { context.getSharedPreferences("connecto_chat_read_prefs", Context.MODE_PRIVATE) }
    val friendPrefs = remember { context.getSharedPreferences("connecto_friend_prefs", Context.MODE_PRIVATE) }
    val notifPrefs = remember { context.getSharedPreferences("connecto_notifications_prefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { ConnectoDatabaseHelper.getInstance(context) }
    val myUsername = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@").lowercase()
    val chatActivity = remember(context) { context.findFragmentActivity() }
    val userPrefix = myUsername.lowercase()
    var pinnedChatIds by remember(myUsername) {
        mutableStateOf(friendPrefs.getStringSet("${userPrefix}_pinned_chat_ids", emptySet())?.toSet() ?: emptySet())
    }
    var deletedChatIds by remember(myUsername) {
        mutableStateOf(friendPrefs.getStringSet("${userPrefix}_deleted_chat_ids", emptySet())?.toSet() ?: emptySet())
    }
    var pendingDeleteChatIds by remember { mutableStateOf(emptySet<String>()) }
    var hasSeenSwipePeekHint by remember(myUsername) {
        mutableStateOf(friendPrefs.getBoolean("${userPrefix}_has_seen_swipe_peek_hint", false))
    }
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

    // Sync active conversation with ConnectoNotificationManager for foreground suppression and tray cleanup
    DisposableEffect(selectedFriend) {
        val friend = selectedFriend
        if (friend != null) {
            val convId = getCanonicalChatId(friend)
            val cleanHandle = friend.handle.trim().lowercase().removePrefix("@")
            com.example.connecto.notifications.ConnectoNotificationManager.currentActiveConversationId = convId
            com.example.connecto.notifications.ConnectoNotificationManager.cancelConversationNotification(convId)
            com.example.connecto.notifications.ConnectoNotificationManager.cancelConversationNotification(cleanHandle)
            com.example.connecto.notifications.ConnectoNotificationManager.cancelConversationNotification(friend.id)
        } else {
            com.example.connecto.notifications.ConnectoNotificationManager.currentActiveConversationId = null
        }
        onDispose {
            com.example.connecto.notifications.ConnectoNotificationManager.currentActiveConversationId = null
        }
    }

    // Direct Messages Feed & State
    var activeDmChannelId by remember { mutableStateOf<String?>(null) }
    var friendMessages by remember {
        mutableStateOf<List<CustomMessageItem>>(emptyList())
    }
    // High-performance chat pre-warming in-memory cache: maps chat canonical ID & item ID to preloaded messages
    val preloadedChatMessages = remember { mutableStateMapOf<String, List<CustomMessageItem>>() }
    var pinnedMessageForChat by remember { mutableStateOf<PinnedMessageDto?>(null) }
    var messageForAction by remember { mutableStateOf<CustomMessageItem?>(null) }
    var showMessageActionDialog by remember { mutableStateOf(false) }

    fun deduplicateCustomMessages(messages: List<CustomMessageItem>): List<CustomMessageItem> {
        val result = mutableListOf<CustomMessageItem>()
        for (msg in messages) {
            val bareMsgId = msg.id.removePrefix("notif_ws_").removePrefix("notif_")
            val existingIndex = result.indexOfFirst { existing ->
                if (existing.id == msg.id) return@indexOfFirst true
                val bareExistingId = existing.id.removePrefix("notif_ws_").removePrefix("notif_")
                if (bareExistingId.isNotBlank() && bareExistingId == bareMsgId) return@indexOfFirst true
                val existingIsTemp = existing.id.startsWith("opt-") || existing.id.startsWith("ws_") || existing.id.startsWith("notif_")
                val newIsTemp = msg.id.startsWith("opt-") || msg.id.startsWith("ws_") || msg.id.startsWith("notif_")
                // Merge by content and timestamp if at least one is temporary/synthetic or if sender & content match within 30s
                val sameContent = existing.content.trim() == msg.content.trim() && msg.content.isNotBlank()
                if (sameContent && existing.isMe == msg.isMe) {
                    val timeDiff = kotlin.math.abs(existing.timestampMillis - msg.timestampMillis)
                    if (timeDiff <= 30_000L || existing.time == msg.time || existingIsTemp || newIsTemp) {
                        return@indexOfFirst true
                    }
                }
                false
            }
            if (existingIndex == -1) {
                result.add(msg)
            } else {
                val existing = result[existingIndex]
                val existingIsTemp = existing.id.startsWith("opt-") || existing.id.startsWith("ws_") || existing.id.startsWith("notif_")
                val newIsTemp = msg.id.startsWith("opt-") || msg.id.startsWith("ws_") || msg.id.startsWith("notif_")
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
                timestampMillis = msgMillis,
                isSilent = dto.isSilent,
                isWhisper = dto.isWhisper
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
    val friendsListState = rememberLazyListState()

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            try {
                if (selectedFriend != null) {
                    if (listState.firstVisibleItemIndex > 15) {
                        listState.scrollToItem(15)
                    }
                    listState.animateScrollToItem(0)
                } else {
                    if (friendsListState.firstVisibleItemIndex > 15) {
                        friendsListState.scrollToItem(15)
                    }
                    friendsListState.animateScrollToItem(0)
                }
            } catch (_: Exception) {}
        }
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
    val wallpaperSyncTrigger by WallpaperSyncManager.wallpaperUpdateTrigger.collectAsState()

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
            WallpaperSyncManager.applyAndSyncWallpaper(
                context = context,
                myUsername = myUsername,
                friend = sf,
                conversationId = null,
                wallpaperId = "custom",
                customUri = fileUriStr,
                transform = wallpaperTransform,
                isGlobal = false
            )
        }
    }

    // Session cache of unlocked friends during current app session
    val unlockedLockedFriends = remember { mutableStateListOf<String>() }
    var pendingLockedFriendToOpen by remember { mutableStateOf<FriendItem?>(null) }
    var isBiometricAuthPromptActive by remember { mutableStateOf(false) }

    // Synchronize mute, lock, theme, wallpaper when selectedFriend changes or cloud wallpapers update
    LaunchedEffect(selectedFriend?.id, myUsername, wallpaperSyncTrigger) {
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
            val (resolvedWp, resolvedUri) = WallpaperResolver.resolveForConversation("global", null, myUsername, friendPrefs, context.filesDir)
            chatWallpaperId = resolvedWp
            customWallpaperUri = resolvedUri
            wallpaperTransform = WallpaperResolver.resolveTransform(null, myUsername, friendPrefs)
        }
    }

    // Active Chat Theme & Wallpaper Resolution (Auto-adapts to Liquid Glass when app is in liquid glass theme)
    val isLight = isAppInLightTheme()
    val isGlassTheme = ConnectoTheme.isGlass
    val isLiquidGlassTheme = ConnectoTheme.isLiquidGlass
    val isPureBlack = !isLight || ConnectoTheme.colors.background == Color(0xFF000000) || ConnectoTheme.colors.background == Color(0xFF0A0B0E)
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

    // User Groups State (Groups)
    val userGroupsList = remember { mutableStateListOf<FriendItem>() }
    var messageTabFilter by rememberSaveable { mutableStateOf("ALL") } // "ALL", "DIRECT", "GROUPS"

    val isVaultUnlocked by SecretVaultManager.isVaultUnlocked.collectAsState()

    val notifyUnreadChanged: () -> Unit = {
        val userPrefix = myUsername.lowercase()
        val prefix = "${userPrefix}_chat_locked_"
        val lockedIds = if (SecretVaultManager.isHideLockedChatsEnabled(context) && !isVaultUnlocked) {
            friendPrefs.all.filter { it.key.startsWith(prefix) && it.value == true }
                .map { it.key.removePrefix(prefix).lowercase() }
                .toSet()
        } else emptySet()

        val total = friendsList.filter {
            val clean = it.handle.removePrefix("@").trim().lowercase()
            it.id.lowercase() !in lockedIds && clean !in lockedIds
        }.sumOf { it.unreadCount } + userGroupsList.sumOf { it.unreadCount }

        onUnreadStatusChanged(total > 0)
        onUnreadCountChanged(total)
    }

    val markFriendChatAsReadLocallyAndRemotely: (FriendItem) -> Unit = { targetFriend ->
        val cleanTarget = targetFriend.handle.removePrefix("@").removePrefix("#").trim()
        val friendId = targetFriend.id

        // 1. Update in-memory friends list
        val idx = friendsList.indexOfFirst {
            it.id == friendId || it.handle.removePrefix("@").equals(cleanTarget, ignoreCase = true)
        }
        if (idx != -1) {
            friendsList[idx] = friendsList[idx].copy(unreadCount = 0)
        }

        // 1b. Update in-memory userGroupsList if target is a group
        val gIdx = userGroupsList.indexOfFirst {
            it.id == friendId || it.handle.removePrefix("#").equals(cleanTarget, ignoreCase = true) || it.name.equals(cleanTarget, ignoreCase = true)
        }
        if (gIdx != -1) {
            userGroupsList[gIdx] = userGroupsList[gIdx].copy(unreadCount = 0)
        }

        // 2. Persist to SharedPreferences for both ID and clean username keys
        readPrefs.edit()
            .putInt("unread_$friendId", 0)
            .putBoolean("read_$friendId", true)
            .putInt("unread_${cleanTarget.lowercase()}", 0)
            .putBoolean("read_${cleanTarget.lowercase()}", true)
            .apply()

        // 3. Persist to local SQLite database (TABLE_FRIENDS & TABLE_GROUPS) so cold starts don't resurrect unread badge
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                dbHelper.updateFriendUnreadCount(friendId, 0)
                if (cleanTarget.isNotBlank() && cleanTarget != friendId) {
                    dbHelper.updateFriendUnreadCount(cleanTarget, 0)
                }
                if (targetFriend.isGroup) {
                    dbHelper.updateGroupUnreadCount(friendId, 0)
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

        // 5. Update global unread indicator and unseen message count
        notifyUnreadChanged()
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
            val isGuestUser = effUser.trim().lowercase().removePrefix("@") == "guest"

            if (isGuestUser) {
                // Privacy protection: Guest session strictly shows non-identifying guest assistance contacts
                val guestFriends = listOf(
                    FriendItem(
                        id = "guest_assistant_1",
                        name = "Connecto Assistant",
                        handle = "@ai_assistant",
                        initial = "A",
                        status = "AI Intelligence Agent",
                        bio = "Ask me anything about workspace navigation, private messaging & calls",
                        lastMessage = "Hi Guest! Welcome to Connecto. How can I assist you today?",
                        timeAgo = "Just now",
                        unreadCount = 0,
                        isOnline = true,
                        avatarUrl = null,
                        lastMessageTimestamp = System.currentTimeMillis()
                    ),
                    FriendItem(
                        id = "guest_support_2",
                        name = "Connecto Support",
                        handle = "@support",
                        initial = "S",
                        status = "Official Platform Guide",
                        bio = "24/7 Official Platform Help Center for Guest Members",
                        lastMessage = "Welcome! Explore channels or tap any friend to test direct chat.",
                        timeAgo = "10m ago",
                        unreadCount = 0,
                        isOnline = true,
                        avatarUrl = null,
                        lastMessageTimestamp = System.currentTimeMillis() - 600000L
                    ),
                    FriendItem(
                        id = "guest_vault_3",
                        name = "Security Vault",
                        handle = "@vault_bot",
                        initial = "V",
                        status = "System Verified",
                        bio = "End-to-End Encryption & Zero-Telemetry Hardware Vault",
                        lastMessage = "Guest sandbox session active • Zero cloud biometric or message leakage",
                        timeAgo = "45m ago",
                        unreadCount = 0,
                        isOnline = false,
                        avatarUrl = null,
                        lastMessageTimestamp = System.currentTimeMillis() - 2700000L
                    )
                )
                val guestGroups = listOf(
                    FriendItem(
                        id = "grp_guest_lounge",
                        name = "Guest Community",
                        handle = "#guest-lounge",
                        initial = "G",
                        status = "Public Community",
                        bio = "Public guest space for team exploration",
                        lastMessage = "Welcome to Connecto! Feel free to say hello.",
                        timeAgo = "15m ago",
                        unreadCount = 0,
                        isOnline = true,
                        avatarUrl = null,
                        isGroup = true,
                        isCreatedByMe = false,
                        memberCount = 8,
                        members = listOf("guest", "support", "assistant"),
                        admin = "support",
                        creator = "support",
                        lastMessageTimestamp = System.currentTimeMillis() - 900000L
                    )
                )
                Pair(guestFriends, guestGroups)
            } else {
                if (effUser.isNotBlank()) {
                    dbHelper.sanitizeFriendsLastMessages(effUser)
                }
                val list = dbHelper.getCachedFriends()
                val mappedFriends = list.map { f ->
                    val cleanHandle = f.handle.removePrefix("@").trim().lowercase()
                    val latest = if (effUser.isNotBlank()) dbHelper.getLatestMessageInfoForFriend(effUser, cleanHandle) else null
                    val prefMsg = readPrefs.getString("last_msg_${f.id}", null) ?: readPrefs.getString("last_msg_${cleanHandle}", null)
                    val prefTime = readPrefs.getString("last_time_${f.id}", null) ?: readPrefs.getString("last_time_${cleanHandle}", null)
                    val prefTs = maxOf(readPrefs.getLong("last_timestamp_${f.id}", 0L), readPrefs.getLong("last_timestamp_${cleanHandle}", 0L))

                    val cleanMsg = if (latest != null && latest.first.isNotBlank() && latest.first != "null" && latest.first != "None") {
                        latest.first
                    } else if (!prefMsg.isNullOrBlank() && prefMsg != "null" && prefMsg != "None" && prefMsg != "Direct chat active • Tap to message") {
                        prefMsg
                    } else if (f.lastMessage.isNotBlank() && f.lastMessage != "null" && f.lastMessage != "None" && f.lastMessage != "Direct chat active • Tap to message") {
                        f.lastMessage
                    } else {
                        "Tap to start chatting"
                    }

                    val ts = if (latest != null && latest.second > 0L) latest.second else if (prefTs > 0L) prefTs else f.lastMessageTimestamp
                    val time = if (ts > 0L) {
                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
                    } else if (!prefTime.isNullOrBlank() && prefTime != "Active" && prefTime != "null") {
                        formatIsoTime(prefTime)
                    } else if (f.timeAgo.isNotBlank() && f.timeAgo != "Active" && f.timeAgo != "null" && f.timeAgo != "None") {
                        formatIsoTime(f.timeAgo)
                    } else {
                        ""
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
        }
        if (cachedFriends.isNotEmpty() && friendsList.isEmpty()) {
            friendsList.addAll(cachedFriends)
            isFriendsLoadedOnce = true
        }
        if (cachedGroups.isNotEmpty() && userGroupsList.isEmpty()) {
            userGroupsList.addAll(cachedGroups.filter { it.id != "grp-design-team-joined" })
        }
        notifyUnreadChanged()
    }

    // Chat Pre-Warming Engine (Step 1): Preload local SQLite messages for all conversations
    // into memory as soon as friends and groups are available, guaranteeing 0ms chat loading
    LaunchedEffect(friendsList.size, userGroupsList.size) {
        if (friendsList.isNotEmpty() || userGroupsList.isNotEmpty()) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val targets = (userGroupsList + friendsList).take(25)
                for (item in targets) {
                    val cKey = getCanonicalChatId(item)
                    if (!preloadedChatMessages.containsKey(cKey)) {
                        val local = loadLocalMessagesForFriend(item)
                        if (local.isNotEmpty()) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                preloadedChatMessages[cKey] = local
                                preloadedChatMessages[item.id] = local
                            }
                        }
                    }
                }
            }
        }
    }

    // Chat Pre-Warming Engine (Step 2): Pre-fetch latest messages from backend in background
    // for active chats so cache is fresh before user even opens any conversation
    LaunchedEffect(friendsList.size, userGroupsList.size) {
        val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
        val isGuestUser = effUser.trim().lowercase().removePrefix("@") == "guest"
        if (effUser.isNotBlank() && !isGuestUser && (friendsList.isNotEmpty() || userGroupsList.isNotEmpty())) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val myClean = effUser.trim().lowercase().removePrefix("@")
                val activeChats = (userGroupsList + friendsList)
                    .filter { it.lastMessageTimestamp > 0L || it.unreadCount > 0 }
                    .take(6)
                for (chat in activeChats) {
                    try {
                        val cleanTarget = chat.handle.removePrefix("@")
                        val canonicalName = if (chat.isGroup) chat.id else "dm-${minOf(myClean, cleanTarget.lowercase())}-${maxOf(myClean, cleanTarget.lowercase())}"
                        val storedDmId = if (!chat.isGroup) dbHelper.getDmChannelId(canonicalName) else null
                        val targetChannelId = if (chat.isGroup) chat.id else (storedDmId ?: canonicalName)
                        val res = if (chat.isGroup) {
                            ConnectoApiClient.getMessages(channelId = chat.id)
                        } else {
                            ConnectoApiClient.getMessages(channelId = targetChannelId, friendUsername = cleanTarget)
                        }
                        if (res.isSuccess) {
                            val dtoList = res.getOrThrow()
                            if (dtoList.isNotEmpty()) {
                                dbHelper.saveMessages(if (chat.isGroup) chat.id else canonicalName, dtoList)
                                val freshMapped = loadLocalMessagesForFriend(chat)
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    val cKey = getCanonicalChatId(chat)
                                    preloadedChatMessages[cKey] = freshMapped
                                    preloadedChatMessages[chat.id] = freshMapped
                                    if (selectedFriend?.id == chat.id) {
                                        friendMessages = freshMapped
                                        isLoadingMessages = false
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
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
        if (VoiceCallManager.isDialLockedFlow.value || VoiceCallManager.callState.value != com.example.connecto.voice.CallState.IDLE) {
            return
        }
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
            val isGuestUser = effUsername.trim().lowercase().removePrefix("@") == "guest"
            if (!isGuestUser) {
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
                    } else if (!sanitizedPrefMsg.isNullOrBlank() && sanitizedPrefMsg != "Direct chat active • Tap to message") {
                        val rawPrefTime = readPrefs.getString("last_time_${f.friendId}", null) ?: readPrefs.getString("last_time_${cleanUsername.lowercase()}", null)
                        val prefTs = maxOf(readPrefs.getLong("last_timestamp_${f.friendId}", 0L), readPrefs.getLong("last_timestamp_${cleanUsername.lowercase()}", 0L))
                        val formattedTime = if (prefTs > 0L) {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(prefTs))
                        } else if (!rawPrefTime.isNullOrBlank() && rawPrefTime != "Active") {
                            formatIsoTime(rawPrefTime)
                        } else "Just now"
                        Triple(sanitizedPrefMsg, formattedTime, prefTs)
                    } else if (cachedFriend != null && cachedFriend.lastMessage.isNotBlank() && cachedFriend.lastMessage != "null" && cachedFriend.lastMessage != "None" && cachedFriend.lastMessage != "Direct chat active • Tap to message") {
                        val ts = cachedFriend.lastMessageTimestamp
                        val formattedTime = if (ts > 0L) {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
                        } else formatIsoTime(cachedFriend.timeAgo)
                        Triple(cachedFriend.lastMessage, formattedTime, ts)
                    } else {
                        Triple("Tap to start chatting", "", 0L)
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

                // Synchronize live online presence with VoiceCallManager
                for (item in mapped) {
                    if (item.isOnline) {
                        VoiceCallManager.setUserPresence(item.handle, true)
                        VoiceCallManager.setUserPresence(item.id, true)
                    }
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
                notifyUnreadChanged()

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
                        notifyUnreadChanged()
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
                        val isMember = (g.members.isNotEmpty() && g.members.any { it.trim().lowercase().removePrefix("@") == myUsernameLower }) || (g.members.isEmpty() && g.isCreatedByMe)
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
                    val removed = userGroupsList.filter { it.id !in gIds }
                    userGroupsList.removeAll { it.id !in gIds }
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
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            for (rem in removed) {
                                dbHelper.deleteCachedGroup(rem.id)
                            }
                            dbHelper.saveGroups(userGroupsList.toList())
                        } catch (_: Exception) {}
                    }
                    val selF = selectedFriend
                    if (selF != null && selF.isGroup && userGroupsList.none { it.id == selF.id }) {
                        selectedFriend = null
                        chatWallpaperId = "default"
                        customWallpaperUri = null
                    }
                    notifyUnreadChanged()
                }
            } catch (_: Exception) {}
            } else {
                isFriendsLoadedOnce = true
            }
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
                "GROUPS" -> userGroupsList.toList()
                else -> friendsList.toList() + userGroupsList.toList()
            }
            // Filter out deleted chats unless currently in undo strip state
            val activeList = baseList.filter { it.id !in deletedChatIds || it.id in pendingDeleteChatIds }

            // Filter out locked chats if hide locked chats is enabled and vault is locked
            val userPrefix = myUsername.lowercase()
            val prefix = "${userPrefix}_chat_locked_"
            val lockedIds = if (SecretVaultManager.isHideLockedChatsEnabled(context) && !isVaultUnlocked) {
                friendPrefs.all.filter { it.key.startsWith(prefix) && it.value == true }
                    .map { it.key.removePrefix(prefix).lowercase() }
                    .toSet()
            } else emptySet()

            val visibleList = if (lockedIds.isNotEmpty()) {
                activeList.filter { friend ->
                    val clean = friend.handle.removePrefix("@").trim().lowercase()
                    friend.id.lowercase() !in lockedIds && clean !in lockedIds
                }
            } else activeList

            val sortedList = visibleList.sortedWith(
                compareByDescending<FriendItem> { pinnedChatIds.contains(it.id) }
                    .thenByDescending { it.lastMessageTimestamp }
                    .thenBy { it.name.lowercase() }
            )
            if (friendSearchQuery.isBlank()) sortedList
            else {
                val q = friendSearchQuery.trim()
                if (SecretVaultManager.isSecretCodeMatch(context, q)) {
                    emptyList()
                } else {
                    sortedList.filter {
                        it.name.contains(q, ignoreCase = true) ||
                        it.handle.contains(q, ignoreCase = true) ||
                        it.lastMessage.contains(q, ignoreCase = true) ||
                        it.bio.contains(q, ignoreCase = true)
                    }
                }
            }
        }
    }



    // Message Context Action States (Pin, Edit, Delete via Contextual Header 3-Dots Menu)
    var selectedMessageForAction by remember { mutableStateOf<CustomMessageItem?>(null) }
    val messageBoundsMap = remember { mutableStateMapOf<String, Rect>() }
    var showMoreOptionsMenu by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<CustomMessageItem?>(null) }
    var highlightedMessageId by remember { mutableStateOf<String?>(null) }
    val messageInputFocusRequester = remember { FocusRequester() }
    var showFullEmojiDialogForMessage by remember { mutableStateOf<CustomMessageItem?>(null) }

    // Real-Time Reaction Observer via WebSocket
    LaunchedEffect(Unit) {
        VoiceCallManager.reactionEvents.collect { event ->
            val msgId = event.messageId
            val emoji = event.emoji
            val uId = event.userId
            friendMessages = friendMessages.map { m ->
                if (m.id == msgId) {
                    val currentMap = m.reactions.toMutableMap()
                    val users = (currentMap[emoji] ?: emptyList()).toMutableList()
                    if (users.any { it.equals(uId, ignoreCase = true) }) {
                        users.removeAll { it.equals(uId, ignoreCase = true) }
                    } else {
                        users.add(uId)
                    }
                    if (users.isEmpty()) {
                        currentMap.remove(emoji)
                    } else {
                        currentMap[emoji] = users
                    }
                    val primaryReaction = currentMap.keys.firstOrNull()
                    m.copy(reactions = currentMap, reaction = primaryReaction)
                } else m
            }
        }
    }

    // Real-Time Message Edit Observer via WebSocket
    LaunchedEffect(Unit) {
        VoiceCallManager.editedMessages.collect { editEv ->
            val idx = friendMessages.indexOfFirst { it.id == editEv.messageId }
            if (idx != -1) {
                friendMessages = friendMessages.toMutableList().also { list ->
                    list[idx] = list[idx].copy(content = editEv.newContent)
                }
                selectedFriend?.let { sf ->
                    val cKey = getCanonicalChatId(sf)
                    preloadedChatMessages[cKey] = friendMessages
                    preloadedChatMessages[sf.id] = friendMessages
                }
            }
        }
    }

    // Intercept back button when message is selected
    BackHandler(enabled = selectedMessageForAction != null) {
        selectedMessageForAction = null
        showMoreOptionsMenu = false
    }

    // Multi-Select Chat Messages State
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedMultiMessageIds = remember { mutableStateListOf<String>() }
    var showForwardSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Centered Obsidian Unsend Confirmation Modal State
    var showUnsendConfirmDialogForMsg by remember { mutableStateOf<CustomMessageItem?>(null) }
    BackHandler(enabled = showUnsendConfirmDialogForMsg != null) {
        showUnsendConfirmDialogForMsg = null
    }

    // Deferred Local Delete & Floating Undo Toast State
    var pendingDeleteMessage by remember { mutableStateOf<CustomMessageItem?>(null) }
    var pendingDeleteJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var pendingDeleteProgress by remember { mutableStateOf(1f) }
    var previousDmPreview by remember { mutableStateOf<String?>(null) }
    var chatInputBarHeightPx by remember { mutableIntStateOf(0) }

    val hapticFeedback = LocalHapticFeedback.current

    val flushPendingDelete: () -> Unit = {
        val msgToCommit = pendingDeleteMessage
        if (msgToCommit != null) {
            pendingDeleteJob?.cancel()
            pendingDeleteJob = null
            pendingDeleteMessage = null

            val msgId = msgToCommit.id
            val deletedSet = friendPrefs.getStringSet("deleted_for_me_$myUsername", emptySet<String>())?.toMutableSet() ?: mutableSetOf()
            deletedSet.add(msgId)
            friendPrefs.edit().putStringSet("deleted_for_me_$myUsername", deletedSet).apply()

            coroutineScope.launch(Dispatchers.IO) {
                try { dbHelper.deleteMessage(msgId) } catch (_: Exception) {}
            }
            coroutineScope.launch {
                try {
                    ConnectoApiClient.deleteMessageForMe(msgId, myUsername)
                } catch (_: Exception) {}
            }
        }
    }

    val startPendingDelete: (CustomMessageItem) -> Unit = { msg ->
        if (pendingDeleteMessage != null) {
            flushPendingDelete()
        }
        try {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {}

        // Capture previous DM preview
        selectedFriend?.let { friend ->
            val fIdx = friendsList.indexOfFirst { it.id == friend.id }
            if (fIdx != -1) {
                previousDmPreview = friendsList[fIdx].lastMessage
            }
        }

        pendingDeleteMessage = msg
        pendingDeleteProgress = 1f

        // Optimistically remove from visible timeline
        friendMessages = friendMessages.filterNot { it.id == msg.id }

        // Optimistically update DM preview with new latest message
        selectedFriend?.let { friend ->
            val fIdx = friendsList.indexOfFirst { it.id == friend.id }
            if (fIdx != -1) {
                val newLastMsg = friendMessages.lastOrNull()?.content ?: ""
                val updated = friendsList[fIdx].copy(lastMessage = newLastMsg)
                friendsList[fIdx] = updated
            }
        }

        // Launch 3-second countdown timer
        pendingDeleteJob?.cancel()
        pendingDeleteJob = coroutineScope.launch {
            val totalDurationMs = 3000L
            val stepMs = 30L
            val totalSteps = (totalDurationMs / stepMs).toInt()
            for (step in totalSteps downTo 0) {
                pendingDeleteProgress = step.toFloat() / totalSteps.toFloat()
                delay(stepMs)
            }
            flushPendingDelete()
        }
    }

    val undoPendingDelete: () -> Unit = {
        pendingDeleteJob?.cancel()
        pendingDeleteJob = null
        val msgToRestore = pendingDeleteMessage
        pendingDeleteMessage = null
        if (msgToRestore != null) {
            try {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
            // Smoothly insert back in original chronological order
            friendMessages = (friendMessages + msgToRestore).sortedBy { it.timestampMillis }
            // Restore previous DM preview
            previousDmPreview?.let { prevPreview ->
                selectedFriend?.let { friend ->
                    val fIdx = friendsList.indexOfFirst { it.id == friend.id }
                    if (fIdx != -1) {
                        val updated = friendsList[fIdx].copy(lastMessage = prevPreview)
                        friendsList[fIdx] = updated
                    }
                }
            }
            previousDmPreview = null
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
                flushPendingDelete()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            flushPendingDelete()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Intercept back button when Multi-Select is active (exits Multi-Select without leaving chat)
    BackHandler(enabled = isMultiSelectMode) {
        isMultiSelectMode = false
        selectedMultiMessageIds.clear()
    }

    // Intercept back button when in-chat search is active
    BackHandler(enabled = isChatSearchOpen) {
        isChatSearchOpen = false
        chatSearchQuery = ""
        pulsingGlowMessageId = null
    }

    // Intercept back button when search query is entered in friends list or search is focused
    BackHandler(enabled = (isDmSearchFocused || friendSearchQuery.isNotEmpty()) && selectedFriend == null && !isFriendProfileOpen) {
        friendSearchQuery = ""
        isDmSearchFocused = false
    }

    // Secret Vault Search Trigger with Haptic Tick
    val isSecretCodeMatched = remember(friendSearchQuery) {
        SecretVaultManager.isSecretCodeMatch(context, friendSearchQuery)
    }
    LaunchedEffect(isSecretCodeMatched) {
        if (isSecretCodeMatched) {
            try {
                // Haptic feedback tick on secret code entry
                android.os.Vibrator::class.java
            } catch (_: Exception) {}
        }
    }

    // Two-Tier Search Engine: 300ms Debounced Cloud Message Search with automatic cancellation
    LaunchedEffect(friendSearchQuery) {
        val q = friendSearchQuery.trim()
        if (SecretVaultManager.isSecretCodeMatch(context, q)) {
            // Secret code matched: suppress cloud search and recent search saving completely
            cloudMessageResults = emptyList()
            isCloudSearching = false
            return@LaunchedEffect
        }
        if (q.length >= 2) {
            isCloudSearching = true
            kotlinx.coroutines.delay(300L) // 300ms debounce
            try {
                val res = ConnectoApiClient.searchMessagesCloud(q, null)
                if (res.isSuccess) {
                    cloudMessageResults = res.getOrDefault(emptyList())
                }
                ConnectoSearchPreferences.saveRecentSearch(context, q)
            } catch (_: Exception) {
                cloudMessageResults = emptyList()
            } finally {
                isCloudSearching = false
            }
        } else {
            cloudMessageResults = emptyList()
            isCloudSearching = false
        }
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

    DisposableEffect(Unit) {
        onDispose {
            isFriendTyping = false
        }
    }

    // Broadcast current user's typing activity in DM (always broadcast active typing even in Stealth Mode)
    LaunchedEffect(typedMessage, selectedFriend?.id, activeDmChannelId) {
        val recipientUser = selectedFriend?.handle?.removePrefix("@") ?: selectedFriend?.name ?: selectedFriend?.id
        val target = activeDmChannelId ?: recipientUser
        if (!target.isNullOrBlank()) {
            if (typedMessage.isNotBlank()) {
                VoiceCallManager.sendTypingStart(target, recipientUser)
                kotlinx.coroutines.delay(2500)
                VoiceCallManager.sendTypingStop(target, recipientUser)
            } else {
                VoiceCallManager.sendTypingStop(target, recipientUser)
            }
        }
    }

    // Handle System Back button when in friend profile or 1:1 direct chat
    BackHandler(enabled = selectedFriend != null || isFriendProfileOpen) {
        flushPendingDelete()
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
            isMultiSelectMode = false
            selectedMultiMessageIds.clear()
            showForwardSheet = false
            showDeleteConfirmDialog = false
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
                    if (showUnsendConfirmDialogForMsg != null) {
                        showUnsendConfirmDialogForMsg = null
                        Toast.makeText(context, "Message was just seen — you can only delete it for yourself", Toast.LENGTH_LONG).show()
                    }
                    val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                    friendMessages = friendMessages.map { msg ->
                        if (msg.isMe && (!msg.isDelivered || !msg.isRead || msg.readAt.isNullOrBlank())) {
                            msg.copy(isDelivered = true, isRead = true, readAt = msg.readAt ?: nowIso)
                        } else {
                            msg
                        }
                    }
                    if (selectedMessageForAction?.isMe == true) {
                        selectedMessageForAction = selectedMessageForAction?.copy(
                            isDelivered = true,
                            isRead = true,
                            readAt = selectedMessageForAction?.readAt ?: nowIso
                        )
                    }
                }
            }
        }
    }

    // Real-time Shared Wallpaper sync observer
    LaunchedEffect(selectedFriend?.id, myUsername) {
        VoiceCallManager.sharedWallpaperEvents.collect { event ->
            val friend = selectedFriend
            val userPrefix = myUsername.trim().removePrefix("@").lowercase()
            val curChatKey = if (friend != null) WallpaperResolver.getCanonicalChatId(friend, userPrefix) else ""
            val fHandle = friend?.handle?.trim()?.removePrefix("@")?.lowercase() ?: ""
            val isTargetChat = event.conversationId.isNotBlank() && (
                event.conversationId.equals(curChatKey, ignoreCase = true) ||
                (fHandle.isNotBlank() && event.conversationId.contains(fHandle, ignoreCase = true)) ||
                (friend != null && event.conversationId.contains(friend.id, ignoreCase = true))
            )

            if (isTargetChat) {
                // If system message present, ensure it appears in timeline
                val sysMsg = event.systemMessage
                if (sysMsg != null && sysMsg.content.isNotBlank()) {
                    val sysItem = CustomMessageItem(
                        id = sysMsg.id,
                        senderName = event.updatedByUsername,
                        initial = event.updatedByUsername.take(1).uppercase(),
                        content = sysMsg.content,
                        time = "Just now",
                        isMe = event.updatedByUsername.equals(myUsername, ignoreCase = true),
                        timestampMillis = System.currentTimeMillis()
                    )
                    if (!friendMessages.any { it.id == sysItem.id }) {
                        friendMessages = (friendMessages + sysItem).sortedBy { it.timestampMillis }
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
                    timestampMillis = parseMessageTimestamp(dto.createdAt),
                    isSilent = dto.isSilent,
                    isWhisper = dto.isWhisper
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
                selectedFriend?.let { sf ->
                    val cKey = getCanonicalChatId(sf)
                    preloadedChatMessages[cKey] = friendMessages
                    preloadedChatMessages[sf.id] = friendMessages
                }
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
                val safeInChatPreview = cleanMessageContentForDisplay(decryptedContent, "Direct chat active • Tap to message")
                val activeCurrFriend = currentFriend
                val fIdx = if (activeCurrFriend != null) {
                    friendsList.indexOfFirst {
                        it.id == activeCurrFriend.id || it.handle.removePrefix("@").equals(activeCurrFriend.handle.removePrefix("@"), ignoreCase = true)
                    }
                } else -1
                if (fIdx != -1) {
                    val target = friendsList[fIdx]
                    val cleanHandle = target.handle.removePrefix("@").trim().lowercase()
                    val updated = target.copy(
                        lastMessage = safeInChatPreview,
                        timeAgo = formatIsoTime(dto.createdAt),
                        unreadCount = 0,
                        lastMessageTimestamp = currentTs
                    )
                    friendsList.removeAt(fIdx)
                    friendsList.add(0, updated)
                    readPrefs.edit()
                        .putString("last_msg_${updated.id}", safeInChatPreview)
                        .putString("last_msg_${cleanHandle}", safeInChatPreview)
                        .putString("last_time_${updated.id}", formatIsoTime(dto.createdAt))
                        .putString("last_time_${cleanHandle}", formatIsoTime(dto.createdAt))
                        .putLong("last_timestamp_${updated.id}", currentTs)
                        .putLong("last_timestamp_${cleanHandle}", currentTs)
                        .apply()
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            dbHelper.updateFriendLastMessageAndUnread(updated.id, safeInChatPreview, formatIsoTime(dto.createdAt), 0, currentTs)
                        } catch (_: Exception) {}
                    }
                    markFriendChatAsReadLocallyAndRemotely(updated)
                } else if (activeCurrFriend != null) {
                    val gIdx = userGroupsList.indexOfFirst { it.id == activeCurrFriend.id }
                    if (gIdx != -1) {
                        val updated = userGroupsList[gIdx].copy(
                            lastMessage = safeInChatPreview,
                            timeAgo = formatIsoTime(dto.createdAt),
                            unreadCount = 0,
                            lastMessageTimestamp = currentTs
                        )
                        userGroupsList.removeAt(gIdx)
                        userGroupsList.add(0, updated)
                        readPrefs.edit()
                            .putString("last_msg_${updated.id}", safeInChatPreview)
                            .putString("last_time_${updated.id}", formatIsoTime(dto.createdAt))
                            .putInt("unread_${updated.id}", 0)
                            .putLong("last_timestamp_${updated.id}", currentTs)
                            .apply()
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                dbHelper.updateGroupLastMessageAndUnread(updated.id, safeInChatPreview, formatIsoTime(dto.createdAt), 0, currentTs)
                            } catch (_: Exception) {}
                        }
                        markFriendChatAsReadLocallyAndRemotely(updated)
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

                val safeBgPreview = cleanMessageContentForDisplay(decContent, "New message")
                if (fIdx != -1) {
                    val targetFriend = friendsList[fIdx]
                    val cleanHandle = targetFriend.handle.removePrefix("@").trim().lowercase()
                    val newUnread = targetFriend.unreadCount + 1
                    val updated = targetFriend.copy(
                        lastMessage = safeBgPreview,
                        timeAgo = formatIsoTime(dto.createdAt),
                        unreadCount = newUnread,
                        lastMessageTimestamp = currentTs
                    )
                    friendsList.removeAt(fIdx)
                    friendsList.add(0, updated)
                    readPrefs.edit()
                        .putString("last_msg_${targetFriend.id}", safeBgPreview)
                        .putString("last_msg_${cleanHandle}", safeBgPreview)
                        .putString("last_time_${targetFriend.id}", formatIsoTime(dto.createdAt))
                        .putString("last_time_${cleanHandle}", formatIsoTime(dto.createdAt))
                        .putInt("unread_${targetFriend.id}", newUnread)
                        .putInt("unread_${cleanHandle}", newUnread)
                        .putLong("last_timestamp_${targetFriend.id}", currentTs)
                        .putLong("last_timestamp_${cleanHandle}", currentTs)
                        .apply()
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            dbHelper.updateFriendLastMessageAndUnread(targetFriend.id, safeBgPreview, formatIsoTime(dto.createdAt), newUnread, currentTs)
                        } catch (_: Exception) {}
                    }
                    notifyUnreadChanged()
                } else {
                    val gIdx = userGroupsList.indexOfFirst {
                        it.id.equals(dtoChannelClean, ignoreCase = true) || it.name.trim().lowercase() == dtoChannelClean
                    }
                    if (gIdx != -1) {
                        val targetGroup = userGroupsList[gIdx]
                        val newUnread = targetGroup.unreadCount + 1
                        val updated = targetGroup.copy(
                            lastMessage = safeBgPreview,
                            timeAgo = formatIsoTime(dto.createdAt),
                            unreadCount = newUnread,
                            lastMessageTimestamp = currentTs
                        )
                        userGroupsList.removeAt(gIdx)
                        userGroupsList.add(0, updated)
                        readPrefs.edit()
                            .putString("last_msg_${targetGroup.id}", safeBgPreview)
                            .putString("last_time_${targetGroup.id}", formatIsoTime(dto.createdAt))
                            .putInt("unread_${targetGroup.id}", newUnread)
                            .putLong("last_timestamp_${targetGroup.id}", currentTs)
                            .apply()
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                dbHelper.updateGroupLastMessageAndUnread(targetGroup.id, safeBgPreview, formatIsoTime(dto.createdAt), newUnread, currentTs)
                            } catch (_: Exception) {}
                        }
                        notifyUnreadChanged()
                    } else {
                        // Friend not yet in memory list — check local SQLite database
                        coroutineScope.launch(Dispatchers.IO) {
                            val cachedFriend = dbHelper.getFriendByUsernameOrHandle(authorClean)
                            if (cachedFriend != null) {
                                val cleanH = cachedFriend.handle.removePrefix("@").trim().lowercase()
                                val newUnread = cachedFriend.unreadCount + 1
                                val updated = cachedFriend.copy(
                                    lastMessage = safeBgPreview,
                                    timeAgo = formatIsoTime(dto.createdAt),
                                    unreadCount = newUnread,
                                    lastMessageTimestamp = currentTs
                                )
                                readPrefs.edit()
                                    .putString("last_msg_${cachedFriend.id}", safeBgPreview)
                                    .putString("last_msg_${cleanH}", safeBgPreview)
                                    .putString("last_time_${cachedFriend.id}", formatIsoTime(dto.createdAt))
                                    .putString("last_time_${cleanH}", formatIsoTime(dto.createdAt))
                                    .putInt("unread_${cachedFriend.id}", newUnread)
                                    .putInt("unread_${cleanH}", newUnread)
                                    .putLong("last_timestamp_${cachedFriend.id}", currentTs)
                                    .putLong("last_timestamp_${cleanH}", currentTs)
                                    .apply()
                                try {
                                    dbHelper.updateFriendLastMessageAndUnread(cachedFriend.id, safeBgPreview, formatIsoTime(dto.createdAt), newUnread, currentTs)
                                } catch (_: Exception) {}
                                withContext(Dispatchers.Main) {
                                    val existingIdx = friendsList.indexOfFirst { it.id == updated.id }
                                    if (existingIdx != -1) {
                                        friendsList.removeAt(existingIdx)
                                    }
                                    friendsList.add(0, updated)
                                    notifyUnreadChanged()
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
            selectedFriend?.let { sf ->
                val cKey = getCanonicalChatId(sf)
                preloadedChatMessages[cKey] = friendMessages
                preloadedChatMessages[sf.id] = friendMessages
            }
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

            // 0ms INSTANT LOAD: Check preloaded memory cache first
            val cKey = getCanonicalChatId(friend)
            val prewarmed = preloadedChatMessages[cKey] ?: preloadedChatMessages[friend.id]
            if (!prewarmed.isNullOrEmpty()) {
                friendMessages = prewarmed
                isLoadingMessages = false
                try { listState.scrollToItem(prewarmed.size) } catch (_: Exception) {}
            } else {
                val resolvedCached = withContext(Dispatchers.IO) {
                    loadLocalMessagesForFriend(friend)
                }
                friendMessages = resolvedCached
                if (resolvedCached.isNotEmpty()) {
                    preloadedChatMessages[cKey] = resolvedCached
                    preloadedChatMessages[friend.id] = resolvedCached
                }
                isLoadingMessages = false
                if (resolvedCached.isNotEmpty()) {
                    try { listState.scrollToItem(resolvedCached.size) } catch (_: Exception) {}
                }
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

            // Ensure persistent WebSocket connection is active
            VoiceCallManager.connectWebSocket()

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
                            val wasAlreadyOnline = evaluateIsUserOnline(
                                username = cleanLower,
                                userId = currentOpeningFriendId,
                                presenceMap = livePresenceMap,
                                friends = friendsList,
                                fallbackFriend = selectedFriend
                            )
                            val freshOnline = if (wasAlreadyOnline) true else prof.isOnline

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
                                        timestampMillis = msgMillis,
                                        isSilent = dto.isSilent,
                                        isWhisper = dto.isWhisper
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
                                    val chk = getCanonicalChatId(friend)
                                    preloadedChatMessages[chk] = merged
                                    preloadedChatMessages[friend.id] = merged
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
                                    val safeLastPreview = cleanMessageContentForDisplay(last.content, "Direct chat active • Tap to message")
                                    val fIdx = friendsList.indexOfFirst { it.id == friend.id || it.handle.removePrefix("@").equals(friend.handle.removePrefix("@"), ignoreCase = true) }
                                    if (fIdx != -1) {
                                        val updated = friendsList[fIdx].copy(
                                            lastMessage = safeLastPreview,
                                            timeAgo = last.time,
                                            lastMessageTimestamp = last.timestampMillis
                                        )
                                        friendsList[fIdx] = updated
                                        val cleanH = updated.handle.removePrefix("@").trim().lowercase()
                                        readPrefs.edit()
                                            .putString("last_msg_${updated.id}", safeLastPreview)
                                            .putString("last_msg_${cleanH}", safeLastPreview)
                                            .putString("last_time_${updated.id}", last.time)
                                            .putString("last_time_${cleanH}", last.time)
                                            .putLong("last_timestamp_${updated.id}", last.timestampMillis)
                                            .putLong("last_timestamp_${cleanH}", last.timestampMillis)
                                            .apply()
                                        coroutineScope.launch(Dispatchers.IO) {
                                            try {
                                                dbHelper.updateFriendLastMessageAndUnread(updated.id, safeLastPreview, last.time, updated.unreadCount, last.timestampMillis)
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
                isLoadingFriendProfile = false
            } else {
                if (selectedFriendProfileData != null && !selectedFriendProfileData?.username.equals(cleanLower, ignoreCase = true)) {
                    // Instantly purge stale profile from previous friend so friend1 data is NEVER shown for friend2
                    selectedFriendProfileData = null
                }
                isLoadingFriendProfile = true
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
            } finally {
                isLoadingFriendProfile = false
            }
        } else if (currentFriend == null) {
            selectedFriendProfileData = null
            isLoadingFriendProfile = false
        }
    }

    // Friend Profile Info Dialog - Disabled in favor of full screen Friend Profile tab
    val curSelFriend = selectedFriend
    if (false && showFriendProfileDialog && curSelFriend != null) {
        val friendObj = curSelFriend
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
                                            Text(
                                                text = "Custom Photo",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
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
                                                            wallpaperTransform = WallpaperTransform()
                                                            WallpaperSyncManager.resetAndSyncWallpaper(
                                                                context = context,
                                                                myUsername = myUsername,
                                                                friend = friendObj,
                                                                conversationId = null,
                                                                isGlobal = false
                                                            )
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
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    chatWallpaperId = wallpaper.id
                                                    customWallpaperUri = null
                                                    WallpaperSyncManager.applyAndSyncWallpaper(
                                                        context = context,
                                                        myUsername = myUsername,
                                                        friend = friendObj,
                                                        conversationId = null,
                                                        wallpaperId = wallpaper.id,
                                                        customUri = null,
                                                        transform = WallpaperTransform(),
                                                        isGlobal = false
                                                    )
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
                                                    Text(
                                                        text = wallpaper.name,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(22.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Active Wallpaper",
                                                            tint = Color(0xFF0A0B0E),
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
                }
            }
        }
    }

    // Friend Profile Photo Fullscreen Lightbox Dialog
    val lightboxFriend = selectedFriend
    if (showPhotoLightbox && lightboxFriend != null) {
        val friendObj = lightboxFriend
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

    // Chat Wallpaper Size, Position, Presets & Legibility Customizer Dialog
    if (showWallpaperCustomizerDialog) {
        val targetFriendForDialog = selectedFriend
        val friendDisplayName = targetFriendForDialog?.name?.ifBlank { null } ?: targetFriendForDialog?.handle?.removePrefix("@")
        val isDirectChatForDialog = targetFriendForDialog != null && !targetFriendForDialog.isGroup

        WallpaperCustomizerDialog(
            wallpaperId = chatWallpaperId,
            customWallpaperUri = customWallpaperUri,
            initialTransform = wallpaperTransform,
            isDarkTheme = !isLight,
            friendName = friendDisplayName,
            isDirectChat = isDirectChatForDialog,
            onDismissRequest = { showWallpaperCustomizerDialog = false },
            onPickCustomPhoto = {
                wallpaperPickerLauncher.launch("image/*")
            },
            onApply = { newWpId, newCustomUri, newTransform, isGlobal, isShared ->
                val friend = selectedFriend
                if (friend != null && friend.isGroup) {
                    val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                    if (!friend.isUserAdmin(effUser)) {
                        Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                        showWallpaperCustomizerDialog = false
                        return@WallpaperCustomizerDialog
                    }
                }
                chatWallpaperId = newWpId
                customWallpaperUri = newCustomUri
                wallpaperTransform = newTransform

                val targetFriend = if (isGlobal) null else friend
                WallpaperSyncManager.applyAndSyncWallpaper(
                    context = context,
                    myUsername = myUsername,
                    friend = targetFriend,
                    conversationId = null,
                    wallpaperId = newWpId,
                    customUri = newCustomUri,
                    transform = newTransform,
                    isGlobal = isGlobal,
                    isShared = isShared
                )
                val msg = if (isGlobal) "Wallpaper applied to all chats!"
                          else if (isShared) "Wallpaper shared with ${friendDisplayName ?: "friend"}!"
                          else "Wallpaper updated for this chat!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                showWallpaperCustomizerDialog = false
            },
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
                WallpaperSyncManager.applyAndSyncWallpaper(
                    context = context,
                    myUsername = myUsername,
                    friend = friend,
                    conversationId = null,
                    wallpaperId = chatWallpaperId,
                    customUri = customWallpaperUri,
                    transform = newTransform,
                    isGlobal = false
                )
                Toast.makeText(context, "Wallpaper layout updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Unfriend Confirmation Modal Dialog with Data Wipe Warning Tab
    val unfriendTarget = selectedFriend
    if (showUnfriendConfirmDialog && unfriendTarget != null) {
        val friendToRemove = unfriendTarget
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
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Irreversible Action",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Unfriending @$friendUser will permanently delete all mutual history for both users.",
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "The following data will be permanently removed:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Direct Messages: Mutual chat history & media",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Call Logs: Voice & video call logs between users",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Notifications: Pending alerts and badges",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm & Delete", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                        }
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

    // Full Emoji Sheet Dialog for Reaction Pill "+" Button
    val fullEmojiSheetTarget = showFullEmojiDialogForMessage
    if (fullEmojiSheetTarget != null) {
        val targetMsg = fullEmojiSheetTarget
        AlertDialog(
            onDismissRequest = { showFullEmojiDialogForMessage = null },
            title = {
                Text(
                    text = "React to message",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val extendedRows = listOf(
                        listOf("❤️", "🔥", "😂", "👍", "😮", "😢"),
                        listOf("👏", "🙏", "🎉", "🚀", "✨", "💯"),
                        listOf("🤩", "😍", "🥳", "🤔", "👀", "💡"),
                        listOf("🙌", "💪", "⚡", "🤝", "😎", "🫡")
                    )
                    extendedRows.forEach { rowEmojis ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowEmojis.forEach { em ->
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            val msgToReact = targetMsg
                                            showFullEmojiDialogForMessage = null
                                            val currentUid = (ConnectoApiClient.currentUserId ?: currentUsername).trim().lowercase()
                                            val currentUname = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@").lowercase()
                                            val prevMap = msgToReact.reactions
                                            val newMap = prevMap.toMutableMap()
                                            val userList = (newMap[em] ?: emptyList()).toMutableList()
                                            val hasReacted = userList.any {
                                                val cu = it.trim().removePrefix("@").lowercase()
                                                cu == currentUname || (currentUid.isNotBlank() && cu == currentUid)
                                            }
                                            if (hasReacted) {
                                                userList.removeAll {
                                                    val cu = it.trim().removePrefix("@").lowercase()
                                                    cu == currentUname || (currentUid.isNotBlank() && cu == currentUid)
                                                }
                                            } else {
                                                userList.add(if (currentUid.isNotBlank()) currentUid else currentUname)
                                            }
                                            if (userList.isEmpty()) {
                                                newMap.remove(em)
                                            } else {
                                                newMap[em] = userList
                                            }
                                            val primaryReaction = newMap.keys.firstOrNull()

                                            // OPTIMISTIC UPDATE: Instant 0ms Compose state update
                                            friendMessages = friendMessages.map { m ->
                                                if (m.id == msgToReact.id) m.copy(reactions = newMap, reaction = primaryReaction) else m
                                            }
                                            selectedMessageForAction = null

                                            val targetChan = activeDmChannelId ?: (if (selectedFriend?.isGroup == true) (selectedFriend?.id ?: "") else getCanonicalChatId(selectedFriend))
                                            val recipientU = selectedFriend?.handle?.removePrefix("@") ?: selectedFriend?.name?.removePrefix("@")
                                            VoiceCallManager.sendReactionToggled(
                                                messageId = msgToReact.id,
                                                emoji = em,
                                                userId = if (currentUid.isNotBlank()) currentUid else currentUname,
                                                channelId = targetChan.ifBlank { null },
                                                recipientUsername = recipientU
                                            )

                                            coroutineScope.launch(Dispatchers.IO) {
                                                try {
                                                    dbHelper.updateMessageReaction(msgToReact.id, primaryReaction)
                                                } catch (_: Exception) {}
                                            }
                                            coroutineScope.launch {
                                                try {
                                                    ConnectoApiClient.reactToMessage(
                                                        messageId = msgToReact.id,
                                                        emoji = em,
                                                        channelId = targetChan.ifBlank { null }
                                                    )
                                                } catch (_: Exception) {}
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = em, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFullEmojiDialogForMessage = null }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
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

    val currentGrpTarget = selectedFriend
    if (showGroupProfileDialog && currentGrpTarget != null && currentGrpTarget.isGroup) {
        val groupItem = currentGrpTarget
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
                WallpaperSyncManager.applyAndSyncWallpaper(
                    context = context,
                    myUsername = effUser,
                    friend = groupItem,
                    conversationId = null,
                    wallpaperId = wId,
                    customUri = customUri,
                    transform = WallpaperTransform(),
                    isGlobal = false
                )
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
            .background(if (!isLight) Color(0xFF0A0B0E) else MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========== TOP HEADER ==========
            if (isFriendProfileOpen && selectedFriend?.isGroup == false) {
                // Friend Profile screen renders its own full-screen custom top header
            } else if (selectedFriend != null) {
                // 1:1 Direct Chat Header with Call Buttons & Friend Profile Info Trigger
                val headerBg = if (isLiquidGlassTheme) {
                    val alpha = ConnectoTheme.glassAlpha
                    if (alpha <= 0.005f) Color.Transparent
                    else Color.White.copy(alpha = (alpha * 0.35f).coerceIn(0.02f, 0.45f))
                } else if (isPureBlack) {
                    Color(0xCC000000)
                } else if (isLight) {
                    Color(0xE0FFFFFF)
                } else {
                    Color(0xE00E1016)
                }

                val headerBorderColor = if (isLight) Color(0xFFE2E8F0) else Color(0x0FFFFFFF)
                val actionBtnBg = if (isLight) Color(0xFFF1F5F9) else Color(0xFF1A1D26)
                val actionBtnBorder = if (isLight) Color(0xFFE2E8F0) else Color(0x0FFFFFFF)
                val actionIconTint = if (isLight) Color(0xFF1E293B) else Color(0xFFE4E4E7)
                val headerTitleColor = if (isLight) Color(0xFF0F172A) else Color.White
                val headerSubColor = if (isLight) Color(0xFF64748B) else Color(0xFF8E929F)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .border(width = 1.dp, color = headerBorderColor, shape = RectangleShape)
                        .statusBarsPadding()
                        .heightIn(min = 68.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = isChatSearchOpen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220, easing = EaseOutCubic)) togetherWith
                            fadeOut(animationSpec = tween(180, easing = EaseInCubic))
                        },
                        label = "inChatHeaderSearchCrossfade"
                    ) { searchOpen ->
                        if (searchOpen) {
                            val chatSearchResults = remember(chatSearchQuery, friendMessages) {
                                if (chatSearchQuery.isBlank()) emptyList<Pair<Int, CustomMessageItem>>()
                                else {
                                    val q = chatSearchQuery.trim()
                                    friendMessages.mapIndexedNotNull { index, msg ->
                                        if (msg.content.contains(q, ignoreCase = true)) Pair(index, msg) else null
                                    }
                                }
                            }
                            InlineChatSearchBar(
                                query = chatSearchQuery,
                                onQueryChange = {
                                    chatSearchQuery = it
                                    currentChatMatchIdx = 0
                                },
                                placeholder = if (selectedFriend?.isGroup == true) "Search in #${selectedFriend?.name}..." else "Search in chat...",
                                currentMatchIndex = if (chatSearchResults.isNotEmpty()) currentChatMatchIdx.coerceIn(0, chatSearchResults.size - 1) else 0,
                                totalMatches = chatSearchResults.size,
                                onPreviousMatch = {
                                    if (chatSearchResults.isNotEmpty()) {
                                        currentChatMatchIdx = if (currentChatMatchIdx > 0) currentChatMatchIdx - 1 else chatSearchResults.size - 1
                                        val match = chatSearchResults[currentChatMatchIdx]
                                        coroutineScope.launch {
                                            listState.animateScrollToItem((match.first + 1).coerceAtLeast(0))
                                        }
                                        pulsingGlowMessageId = match.second.id
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(1200)
                                            if (pulsingGlowMessageId == match.second.id) {
                                                pulsingGlowMessageId = null
                                            }
                                        }
                                    }
                                },
                                onNextMatch = {
                                    if (chatSearchResults.isNotEmpty()) {
                                        currentChatMatchIdx = (currentChatMatchIdx + 1) % chatSearchResults.size
                                        val match = chatSearchResults[currentChatMatchIdx]
                                        coroutineScope.launch {
                                            listState.animateScrollToItem((match.first + 1).coerceAtLeast(0))
                                        }
                                        pulsingGlowMessageId = match.second.id
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(1200)
                                            if (pulsingGlowMessageId == match.second.id) {
                                                pulsingGlowMessageId = null
                                            }
                                        }
                                    }
                                },
                                onClose = {
                                    isChatSearchOpen = false
                                    chatSearchQuery = ""
                                    pulsingGlowMessageId = null
                                },
                                isDark = !isLight
                            )
                        } else if (isMultiSelectMode) {
                            // Morphing Multi-Select Top Header: ✕ (Exit), live count (3 selected), and Select All / Clear on the right
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(actionBtnBg)
                                            .border(1.dp, actionBtnBorder, CircleShape)
                                            .clickable {
                                                isMultiSelectMode = false
                                                selectedMultiMessageIds.clear()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Exit Multi-Select",
                                            tint = actionIconTint,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = "${selectedMultiMessageIds.size} selected",
                                        color = headerTitleColor,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                val isAllSelected = selectedMultiMessageIds.size >= friendMessages.size && friendMessages.isNotEmpty()

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1F2433))
                                        .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (isAllSelected) {
                                                selectedMultiMessageIds.clear()
                                            } else {
                                                selectedMultiMessageIds.clear()
                                                selectedMultiMessageIds.addAll(friendMessages.map { it.id })
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isAllSelected) "Clear" else "Select All",
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
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
                                .background(actionBtnBg)
                                .border(1.dp, actionBtnBorder, CircleShape)
                                .pressScaleEffect(
                                    onClick = {
                                        flushPendingDelete()
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
                                        isMultiSelectMode = false
                                        selectedMultiMessageIds.clear()
                                        isLoadingMessages = false
                                        onDirectChatStateChanged(false)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Friends List",
                                tint = actionIconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Friend / Group Name & Avatar Row
                        val isGroupChat = selectedFriend?.isGroup == true
                        val headerEntranceAlpha by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 240, easing = EaseOutCubic),
                            label = "headerEntranceAlpha"
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer { alpha = headerEntranceAlpha }
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
                            val isSelectedOnline = if (isGroupChat) false else evaluateIsUserOnline(
                                username = selectedFriend?.handle,
                                userId = selectedFriend?.id,
                                presenceMap = livePresenceMap,
                                friends = friendsList,
                                fallbackFriend = selectedFriend,
                                myHideOnline = myHideOnline
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
                                    val showOnlineDot = isSelectedOnline && !myHideOnline
                                    ConnectoAvatar(
                                        name = selectedFriend?.name ?: "Friend",
                                        avatarUrl = selectedFriend?.avatarUrl,
                                        customSizeDp = 42.dp,
                                        customFontSizeSp = 16,
                                        borderWidth = 1.5.dp,
                                        borderColor = if (showOnlineDot) OnlineGreen.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                                    )
                                    if (showOnlineDot) {
                                        DirectChatOnlineBeacon(modifier = Modifier.align(Alignment.BottomEnd))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = if (isGroupChat) "#${selectedFriend?.name.orEmpty()}" else selectedFriend?.name.orEmpty(),
                                    color = headerTitleColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isOnlineVisible = !isGroupChat && isSelectedOnline && !myHideOnline
                                    if (!isGroupChat && isOnlineVisible) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    val subText = if (isGroupChat) {
                                        val count = selectedFriend?.memberCount ?: selectedFriend?.members?.size ?: 0
                                        "$count members • Group Chat"
                                    } else {
                                        if (isFriendTyping) {
                                            "typing..."
                                        } else if (isOnlineVisible) {
                                            "Online"
                                        } else if (!myHideLastSeen) {
                                            val lastSeenTs = VoiceCallManager.getUserLastSeen(selectedFriend?.handle)
                                                ?: VoiceCallManager.getUserLastSeen(selectedFriend?.id)
                                            val formatted = formatDynamicLastSeen(lastSeenTs, selectedFriend?.lastSeen, lastSeenTicker)
                                            formatted ?: "Direct Chat"
                                        } else {
                                            "Direct Chat"
                                        }
                                    }
                                    Text(
                                        text = subText,
                                        color = if (!isGroupChat && isFriendTyping) MaterialTheme.colorScheme.primary else if (isOnlineVisible) Color(0xFF10B981) else headerSubColor,
                                        fontSize = 12.sp,
                                        fontWeight = if (!isGroupChat && isFriendTyping) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Right Action Controls: Voice Call & Search in Chat
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isGroupChat) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(actionBtnBg)
                                        .border(1.dp, actionBtnBorder, CircleShape)
                                        .pressScaleEffect(
                                            onClick = {
                                                val friend = selectedFriend
                                                if (friend != null) {
                                                    initiateVoiceCallWithFriend(friend)
                                                }
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Voice Call",
                                        tint = actionIconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(actionBtnBg)
                                        .border(1.dp, actionBtnBorder, CircleShape)
                                        .pressScaleEffect(
                                            onClick = {
                                                isChatSearchOpen = true
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search in chat",
                                        tint = actionIconTint,
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
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                            .clickable { isChatSearchOpen = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search in group",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

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

                // Group Voice Active Lobby Banner
                val currentSelectedFriend = selectedFriend
                val isGroupVoiceActive = if (currentSelectedFriend != null && currentSelectedFriend.isGroup) {
                    VoiceCallManager.isGroupVoiceActive(currentSelectedFriend.id) || VoiceCallManager.isGroupVoiceActive(currentSelectedFriend.name)
                } else false

                if (isGroupVoiceActive && currentSelectedFriend != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF11221C))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
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
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Voice Active • Tap to Join",
                                    color = Color(0xFF34D399),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981))
                                    .clickable {
                                        initiateGroupVoiceCall(currentSelectedFriend)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Join",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                val isSearchActive = isDmSearchFocused || friendSearchQuery.isNotEmpty()
                val dmHeaderCollapseRatio by animateFloatAsState(
                    targetValue = if (isSearchActive) 0f else 1f,
                    animationSpec = tween(durationMillis = 220, easing = EaseOutCubic),
                    label = "dmHeaderCollapseRatio"
                )

                // Collapsible Header: Animates upward (opacity: 1 -> 0, height -> 0, 220ms ease-out)
                if (dmHeaderCollapseRatio > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = dmHeaderCollapseRatio
                                translationY = -35f * (1f - dmHeaderCollapseRatio)
                            }
                            .then(
                                if (dmHeaderCollapseRatio < 0.99f) Modifier.height((64.dp * dmHeaderCollapseRatio))
                                else Modifier
                            )
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                coroutineScope.launch {
                                    try {
                                        if (friendsListState.firstVisibleItemIndex > 15) {
                                            friendsListState.scrollToItem(15)
                                        }
                                        friendsListState.animateScrollToItem(0)
                                    } catch (_: Exception) {}
                                }
                            }
                    ) {
                        ConnectoTopHeader(
                            title = "DIRECT MESSAGES",
                            subtitle = if (friendSearchQuery.isNotBlank()) {
                                "${filteredFriends.size} conversation${if (filteredFriends.size != 1) "s" else ""} found"
                            } else {
                                "Chat with friends & members"
                            },
                            userInitial = userInitial,
                            onVoiceGroupClick = null,
                            onAddFriendClick = null,
                            pendingFriendRequestsCount = 0,
                            showProfileButton = false,
                            onAvatarClick = {},
                            customAction = null
                        )
                    }
                }

                // 60fps Micro-Animated Search Bar with sliding cancel, active glow border & inline spinner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    ConnectoAnimatedSearchBar(
                        query = friendSearchQuery,
                        onQueryChange = { friendSearchQuery = it },
                        placeholder = "Search messages, groups, or friends...",
                        isFocused = isDmSearchFocused,
                        onFocusChanged = { isDmSearchFocused = it },
                        isLoading = isCloudSearching,
                        onCancel = {
                            friendSearchQuery = ""
                            isDmSearchFocused = false
                        },
                        focusRequester = searchFocusRequester,
                        isDark = !isLight
                    )
                }

                // Recent Searches & Quick Category Filter Chips (All, People, Messages, Channels)
                if (isDmSearchFocused && friendSearchQuery.isEmpty()) {
                    val recentList = remember(friendSearchQuery, isDmSearchFocused) {
                        ConnectoSearchPreferences.getRecentSearches(context)
                    }
                    RecentSearchesAndFilterChips(
                        recentSearches = recentList,
                        selectedFilter = selectedDmSearchFilter,
                        onSelectFilter = { selectedDmSearchFilter = it },
                        onSelectRecentSearch = { q ->
                            friendSearchQuery = q
                        },
                        onClearRecentSearches = {
                            ConnectoSearchPreferences.clearRecentSearches(context)
                        },
                        isDark = !isLight
                    )
                } else if (friendSearchQuery.isNotEmpty()) {
                    RecentSearchesAndFilterChips(
                        recentSearches = emptyList(),
                        selectedFilter = selectedDmSearchFilter,
                        onSelectFilter = { selectedDmSearchFilter = it },
                        onSelectRecentSearch = {},
                        onClearRecentSearches = {},
                        isDark = !isLight
                    )
                }

                // Inset 1px divider under Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(if (!isLight) Color(0x0DFFFFFF) else Color(0x0F000000))
                )

                // Secret Vault Card Triggered by Secret Search Code
                if (isSecretCodeMatched) {
                    val haptic = LocalHapticFeedback.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.22f),
                                        Color(0xFF8B5CF6).copy(alpha = 0.22f)
                                    )
                                )
                            )
                            .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable {
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } catch (_: Exception) {}
                                val activity = context.findFragmentActivity()
                                if (activity != null) {
                                    BiometricAuthManager.authenticate(
                                        activity = activity,
                                        title = "Unlock Secret Vault",
                                        subtitle = "Authenticate to access hidden chats",
                                        onSuccess = {
                                            SecretVaultManager.unlockVault()
                                            friendSearchQuery = ""
                                            isDmSearchFocused = false
                                            Toast.makeText(context, "Secret Vault unlocked 🔓", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { _, err ->
                                            Toast.makeText(context, "Authentication failed: $err", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    SecretVaultManager.unlockVault()
                                    friendSearchQuery = ""
                                    isDmSearchFocused = false
                                }
                            }
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6366F1).copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Secret Vault",
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔒 Secret Vault • Tap to open",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLight) Color(0xFF0F172A) else Color.White
                                )
                                Text(
                                    text = "Authenticate with biometric credentials to view hidden locked chats",
                                    fontSize = 11.5.sp,
                                    color = if (isLight) Color(0xFF64748B) else Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            val currentScreenState = when {
                isFriendProfileOpen && selectedFriend?.isGroup == false -> "FRIEND_PROFILE"
                selectedFriend != null -> "DIRECT_CHAT"
                else -> "FRIENDS_LIST"
            }

            AnimatedContent(
                targetState = currentScreenState,
                transitionSpec = {
                    if (targetState == "FRIEND_PROFILE" || (initialState == "FRIENDS_LIST" && targetState == "DIRECT_CHAT")) {
                        // Opening Direct Chat or Friend Profile: fluid horizontal slide-in from right
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(320, easing = EaseOutCubic)
                        ) + fadeIn(animationSpec = tween(220, easing = EaseOutCubic)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -fullWidth / 4 },
                                    animationSpec = tween(280, easing = EaseInCubic)
                                ) + fadeOut(animationSpec = tween(180, easing = EaseInCubic))
                            )
                    } else if (targetState == "FRIENDS_LIST") {
                        // Closing Direct Chat / Friend Profile: fluid horizontal slide-out to right
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 4 },
                            animationSpec = tween(300, easing = EaseOutCubic)
                        ) + fadeIn(animationSpec = tween(220, easing = EaseOutCubic)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> fullWidth },
                                    animationSpec = tween(280, easing = EaseInCubic)
                                ) + fadeOut(animationSpec = tween(180, easing = EaseInCubic))
                            )
                    } else {
                        // Direct Chat <-> Friend Profile switching
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth / 2 },
                            animationSpec = tween(280, easing = EaseOutCubic)
                        ) + fadeIn(animationSpec = tween(200, easing = EaseOutCubic)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { fullWidth -> -fullWidth / 2 },
                                    animationSpec = tween(260, easing = EaseInCubic)
                                ) + fadeOut(animationSpec = tween(180, easing = EaseInCubic))
                            )
                    }
                },
                label = "friendChatViewTransition",
                modifier = Modifier.weight(1f)
            ) { screenState ->
                when (screenState) {
                    "FRIENDS_LIST" -> {
                    // ================= 1. FRIENDS LIST SCREEN =================
                    Box(modifier = Modifier.fillMaxSize()) {
                    com.example.connecto.ui.components.ConnectoDragToRefresh(
                        isRefreshing = isRefreshingFriends,
                        onRefresh = {
                            isRefreshingFriends = true
                            coroutineScope.launch {
                                val startTime = System.currentTimeMillis()
                                try {
                                    kotlinx.coroutines.withTimeoutOrNull(2000L) {
                                        kotlinx.coroutines.coroutineScope {
                                            val d1 = async { syncAcceptedFriends() }
                                            d1.await()
                                        }
                                    }
                                } catch (_: Exception) {
                                } finally {
                                    val elapsed = System.currentTimeMillis() - startTime
                                    if (elapsed < 350L) {
                                        kotlinx.coroutines.delay(350L - elapsed)
                                    }
                                    isRefreshingFriends = false
                                }
                            }
                        },
                        refreshText = "Syncing conversations & friends...",
                        canScrollBackward = {
                            friendsListState.canScrollBackward ||
                            friendsListState.firstVisibleItemIndex > 0 ||
                            friendsListState.firstVisibleItemScrollOffset > 0
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                    DisableSelection {
                    val decryptedPreviewCache = remember { java.util.concurrent.ConcurrentHashMap<String, String>() }
                    val userPrefix = myUsername.lowercase()
                    val lockedFriendIds = remember(userPrefix, unlockedLockedFriends.size) {
                        val prefix = "${userPrefix}_chat_locked_"
                        friendPrefs.all.filter { it.key.startsWith(prefix) && it.value == true }
                            .map { it.key.removePrefix(prefix) }
                            .toSet()
                    }
                    LazyColumn(
                        state = friendsListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        if (isVaultUnlocked) {
                            item(key = "secret_vault_active_banner") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF6366F1).copy(alpha = 0.15f))
                                        .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            SecretVaultManager.lockVault()
                                            Toast.makeText(context, "Secret Vault locked 🔒", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = "🔓", fontSize = 14.sp)
                                        Text(
                                            text = "Secret Vault Active • Tap to lock",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFA5B4FC)
                                        )
                                    }
                                    Text(
                                        text = "Lock",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF818CF8)
                                    )
                                }
                            }
                        }

                        // Message Tab Filter Button Group (Segmented Controls)
                        item(key = "friends_filter_button_group") {
                            val filterOptions = listOf(
                                "ALL" to "All",
                                "DIRECT" to "Direct",
                                "GROUPS" to "Groups"
                            )
                            val isDark = !isLight
                            val trackBg = if (isDark) Color(0xFF12141A) else Color(0xFFF1F5F9)
                            val activeTabBg = if (isDark) Color(0xFF242836) else Color(0xFF0F172A)
                            val activeTextColor = Color(0xFFFFFFFF)
                            val inactiveTextColor = if (isDark) Color(0xFF7A7F8F) else Color(0xFF64748B)

                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(trackBg)
                                    .padding(4.dp)
                            ) {
                                val haptic = LocalHapticFeedback.current
                                val totalWidth = maxWidth
                                val optionCount = filterOptions.size
                                val tabWidth = totalWidth / optionCount
                                val selectedIndex = filterOptions.indexOfFirst { it.first == messageTabFilter }.coerceAtLeast(0)

                                val animatedPillOffset by animateDpAsState(
                                    targetValue = tabWidth * selectedIndex,
                                    animationSpec = spring(
                                        dampingRatio = 0.8f,
                                        stiffness = 380f
                                    ),
                                    label = "slidingPillOffset"
                                )

                                // Sliding active background pill behind text (smooth 250ms spring curve)
                                Box(
                                    modifier = Modifier
                                        .offset(x = animatedPillOffset)
                                        .width(tabWidth)
                                        .height(36.dp)
                                        .padding(horizontal = 2.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(activeTabBg)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    filterOptions.forEach { (key, label) ->
                                        val isSelected = (messageTabFilter == key)
                                        val textColor by animateColorAsState(
                                            targetValue = if (isSelected) activeTextColor else inactiveTextColor,
                                            animationSpec = tween(200),
                                            label = "segmentedTextColor"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(36.dp)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    try {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    } catch (_: Exception) {}
                                                    messageTabFilter = key
                                                }
                                                .padding(horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                color = textColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item(key = "friends_section_title") {
                            val titleText = if (friendSearchQuery.isNotBlank()) {
                                "CONTACTS (${filteredFriends.size})"
                            } else when (messageTabFilter) {
                                "DIRECT" -> "Direct Conversations"
                                "GROUPS" -> "Group Conversations"
                                else -> "All Conversations"
                            }
                            Text(
                                text = titleText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
                            )
                        }

                        if (filteredFriends.isEmpty() && (cloudMessageResults.isEmpty() || selectedDmSearchFilter == "People")) {
                            if (friendSearchQuery.isNotBlank()) {
                                item(key = "friends_search_empty_state") {
                                    SearchEmptyState(query = friendSearchQuery, isDark = !isLight)
                                }
                            } else if (isFriendsLoadedOnce) {
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
                                                "GROUPS" -> Icons.Default.Groups
                                                else -> Icons.Default.PersonAdd
                                            }
                                            val emptyTitle = if (friendSearchQuery.isNotBlank()) "No Matching Conversations"
                                            else when (messageTabFilter) {
                                                "GROUPS" -> "No Groups Yet"
                                                "DIRECT" -> "No Direct Messages Yet"
                                                else -> "No Active Conversations"
                                            }
                                            val emptySubtitle = if (friendSearchQuery.isNotBlank())
                                                "No conversations or groups matching '$friendSearchQuery' found."
                                            else when (messageTabFilter) {
                                                "GROUPS" -> "You haven't joined or created any groups yet. Create a group to talk with multiple friends together!"
                                                "DIRECT" -> "Direct chat is private between accepted friends. Send a friend request to start messaging!"
                                                else -> "Start chatting with friends or create a group to talk with multiple members together!"
                                            }

                                            Box(
                                                modifier = Modifier
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

                                            val isGroupFilter = messageTabFilter == "GROUPS"
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
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        repeat(6) {
                                            ShimmerConversationItem(isDark = !isLight)
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(
                            filteredFriends,
                            key = { _, friend -> friend.id },
                            contentType = { _, _ -> "conversation_row" }
                        ) { index, friend ->
                            val cleanFriendHandle = remember(friend.handle) { friend.handle.trim().lowercase().removePrefix("@") }
                            val isFriendLocked = remember(friend.id, cleanFriendHandle, lockedFriendIds) {
                                lockedFriendIds.contains(friend.id) || lockedFriendIds.contains(cleanFriendHandle)
                            }
                            val isUnlocked = unlockedLockedFriends.contains(friend.id)
                            val isPinned = pinnedChatIds.contains(friend.id)
                            val isPendingDelete = pendingDeleteChatIds.contains(friend.id)

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
                                val chKey = getCanonicalChatId(friend)
                                val prewarmed = preloadedChatMessages[chKey] ?: preloadedChatMessages[friend.id]
                                if (!prewarmed.isNullOrEmpty()) {
                                    friendMessages = prewarmed
                                    isLoadingMessages = false
                                } else {
                                    friendMessages = emptyList()
                                    isLoadingMessages = true
                                }
                                typedMessage = messageDrafts[friend.id] ?: ""
                                selectedFriend = friend
                                onDirectChatStateChanged(true)
                                markFriendChatAsReadLocallyAndRemotely(friend)

                                coroutineScope.launch(Dispatchers.IO) {
                                    val initialMsgs = if (prewarmed.isNullOrEmpty()) loadLocalMessagesForFriend(friend) else prewarmed
                                    withContext(Dispatchers.Main) {
                                        friendMessages = initialMsgs
                                        preloadedChatMessages[chKey] = initialMsgs
                                        preloadedChatMessages[friend.id] = initialMsgs
                                        isLoadingMessages = false
                                        try { listState.scrollToItem(initialMsgs.size) } catch (_: Exception) {}
                                    }
                                }
                            }

                            SwipeableConversationRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                chatId = friend.id,
                                chatName = friend.name,
                                isPinned = isPinned,
                                isPendingDelete = isPendingDelete,
                                triggerInitialPeekHint = false,
                                onTogglePin = {
                                    val isCurrentlyPinned = pinnedChatIds.contains(friend.id)
                                    pinnedChatIds = if (isCurrentlyPinned) {
                                        pinnedChatIds - friend.id
                                    } else {
                                        pinnedChatIds + friend.id
                                    }
                                    friendPrefs.edit().putStringSet("${userPrefix}_pinned_chat_ids", pinnedChatIds).apply()
                                },
                                onDeleteInitiated = {
                                    pendingDeleteChatIds = pendingDeleteChatIds + friend.id
                                },
                                onDeleteFinalized = {
                                    pendingDeleteChatIds = pendingDeleteChatIds - friend.id
                                    deletedChatIds = deletedChatIds + friend.id
                                    friendPrefs.edit().putStringSet("${userPrefix}_deleted_chat_ids", deletedChatIds).apply()
                                },
                                onUndoDelete = {
                                    pendingDeleteChatIds = pendingDeleteChatIds - friend.id
                                    deletedChatIds = deletedChatIds - friend.id
                                    friendPrefs.edit().putStringSet("${userPrefix}_deleted_chat_ids", deletedChatIds).apply()
                                },
                                onPeekHintDone = {
                                    hasSeenSwipePeekHint = true
                                    friendPrefs.edit().putBoolean("${userPrefix}_has_seen_swipe_peek_hint", true).apply()
                                }
                            ) {
                                val onRowClick: () -> Unit = {
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
                                }

                                val displayTime = remember(friend.lastMessageTimestamp, friend.timeAgo) {
                                    if (friend.lastMessageTimestamp > 0L) {
                                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(friend.lastMessageTimestamp))
                                    } else if (friend.timeAgo.isNotBlank() && friend.timeAgo != "null" && friend.timeAgo != "None" && friend.timeAgo != "Active") {
                                        formatIsoTime(friend.timeAgo)
                                    } else {
                                        ""
                                    }
                                }

                                val displayMsg = remember(friend.id, friend.lastMessage) {
                                    if (friend.lastMessage.isBlank() || friend.lastMessage == "null" || friend.lastMessage == "None" || friend.lastMessage == "Direct chat active • Tap to message") {
                                        "Tap to start chatting"
                                    } else if (ConnectoE2EEncryption.isEncrypted(friend.lastMessage)) {
                                        val cacheKey = "${friend.id}_${friend.lastMessage.hashCode()}"
                                        decryptedPreviewCache.getOrPut(cacheKey) {
                                            try {
                                                val cleanTarget = friend.handle.removePrefix("@").trim().lowercase()
                                                val e2eSecret = ConnectoApiClient.getE2ESecret()
                                                val key = ConnectoE2EEncryption.deriveConversationKey(myUsername, cleanTarget, e2eSecret)
                                                val fallbackKey = ConnectoE2EEncryption.deriveConversationKey(myUsername, cleanTarget, null)
                                                val dec = ConnectoE2EEncryption.decryptMessage(friend.lastMessage, key, fallbackKey)
                                                cleanMessageContentForDisplay(dec, "Tap to start chatting")
                                            } catch (_: Exception) {
                                                cleanMessageContentForDisplay(friend.lastMessage, "Tap to start chatting")
                                            }
                                        }
                                    } else {
                                        cleanMessageContentForDisplay(friend.lastMessage, "Tap to start chatting")
                                    }
                                }

                                val rowInteraction = remember { MutableInteractionSource() }
                                val isRowPressed by rowInteraction.collectIsPressedAsState()
                                val rowScale by animateFloatAsState(
                                    targetValue = if (isRowPressed) 0.97f else 1.0f,
                                    animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
                                    label = "chatRowScale"
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isLight) Color.White else Color(0xFF0A0B0E))
                                        .graphicsLayer {
                                            scaleX = rowScale
                                            scaleY = rowScale
                                        }
                                        .clickable(
                                            interactionSource = rowInteraction,
                                            indication = null,
                                            onClick = onRowClick
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 48px avatar with 12px status dot cutout
                                        Box(
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            if (friend.isGroup) {
                                                if (!friend.avatarUrl.isNullOrBlank()) {
                                                    ConnectoAvatar(
                                                        name = friend.name,
                                                        avatarUrl = friend.avatarUrl,
                                                        customSizeDp = 48.dp,
                                                        customFontSizeSp = 18
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isLight) Color(0xFFE2E8F0) else Color(0xFF1A1D26)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Groups,
                                                            contentDescription = "Group",
                                                            tint = if (isLight) Color(0xFF0F172A) else Color.White,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                            } else {
                                                ConnectoAvatar(
                                                    name = friend.name,
                                                    avatarUrl = friend.avatarUrl,
                                                    customSizeDp = 48.dp,
                                                    customFontSizeSp = 18
                                                )
                                                val isFriendOnline = evaluateIsUserOnline(
                                                    username = friend.handle,
                                                    userId = friend.id,
                                                    presenceMap = livePresenceMap,
                                                    friends = friendsList,
                                                    fallbackFriend = friend,
                                                    myHideOnline = myHideOnline
                                                )
                                                val presenceDotColor = if (isFriendOnline) Color(0xFF10B981) else Color(0xFF475569)
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isLight) Color(0xFFFFFFFF) else Color(0xFF0A0B0E))
                                                        .padding(2.dp)
                                                        .clip(CircleShape)
                                                        .background(presenceDotColor)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        // Center Column: Display Name + Message Preview
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val displayName = if (friend.name.isNotBlank()) friend.name else friend.handle
                                                if (friendSearchQuery.isNotBlank() && displayName.contains(friendSearchQuery, ignoreCase = true)) {
                                                    SubstringHighlightedText(
                                                        text = displayName,
                                                        query = friendSearchQuery,
                                                        style = TextStyle(
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (isLight) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                } else {
                                                    Text(
                                                        text = displayName,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isLight) Color(0xFF0F172A) else Color(0xFFFFFFFF),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                if (isPinned) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Filled.PushPin,
                                                        contentDescription = "Pinned",
                                                        tint = Color(0xFFF59E0B),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                                if (isFriendLocked) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = "Chat Locked",
                                                        tint = if (isLight) Color(0xFF0F172A) else Color(0xFF8E929F),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                                if (displayTime.isNotBlank()) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    // Top-aligned timestamp (12px, #686C78)
                                                    Text(
                                                        text = displayTime,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (friend.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                                        color = if (friend.unreadCount > 0) (if (isLight) Color(0xFF0F172A) else Color.White) else (if (isLight) Color(0xFF64748B) else Color(0xFF686C78)),
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(3.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val previewText = if (friend.isGroup) {
                                                    "${friend.memberCount} members"
                                                } else {
                                                    displayMsg
                                                }

                                                if (friendSearchQuery.isNotBlank() && previewText.contains(friendSearchQuery, ignoreCase = true)) {
                                                    SubstringHighlightedText(
                                                        text = previewText,
                                                        query = friendSearchQuery,
                                                        style = TextStyle(
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Normal,
                                                            color = if (friend.unreadCount > 0) (if (isLight) Color(0xFF0F172A) else Color(0xFFE4E4E7)) else (if (isLight) Color(0xFF64748B) else Color(0xFF8E929F))
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                } else {
                                                    Text(
                                                        text = previewText,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = if (friend.unreadCount > 0) (if (isLight) Color(0xFF0F172A) else Color(0xFFE4E4E7)) else (if (isLight) Color(0xFF64748B) else Color(0xFF8E929F)),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }

                                                if (friend.unreadCount > 0) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                                                            .clip(RoundedCornerShape(9.dp))
                                                            .background(if (isLight) Color(0xFF0F172A) else Color.White)
                                                            .padding(horizontal = 5.dp, vertical = 1.5.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = if (friend.unreadCount > 99) "99+" else "${friend.unreadCount}",
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isLight) Color.White else Color.Black
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Subtle 1px divider aligned with text
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 78.dp)
                                            .height(1.dp)
                                            .background(if (isLight) Color(0x0F000000) else Color(0x0DFFFFFF))
                                    )
                                }
                            }
                        }

                        // Cloud Message Search Results section (Two-tier deep history)
                        if (friendSearchQuery.isNotBlank() && cloudMessageResults.isNotEmpty() && selectedDmSearchFilter != "People") {
                            item(key = "cloud_messages_section_header") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp, top = 16.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "MESSAGES (${cloudMessageResults.size})",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = if (!isLight) Color(0xFF818CF8) else Color(0xFF4F46E5)
                                    )
                                }
                            }

                            items(cloudMessageResults, key = { "cloud_msg_${it.id}" }) { msgResult ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (!isLight) Color(0xFF141722) else Color(0xFFF8FAFC))
                                        .border(1.dp, if (!isLight) Color(0x14FFFFFF) else Color(0x1A000000), RoundedCornerShape(14.dp))
                                        .clickable {
                                            val foundFriend = friendsList.firstOrNull { it.id == msgResult.channelId || it.handle.removePrefix("@").equals(msgResult.authorUsername.removePrefix("@"), ignoreCase = true) }
                                            if (foundFriend != null) {
                                                selectedFriend = foundFriend
                                                onDirectChatStateChanged(true)
                                            } else {
                                                val tempFriend = FriendItem(
                                                    id = msgResult.channelId.ifBlank { msgResult.senderId },
                                                    name = msgResult.authorName,
                                                    handle = "@${msgResult.authorUsername}",
                                                    initial = (msgResult.authorName.take(1).ifBlank { "U" }).uppercase(),
                                                    status = "online",
                                                    bio = "",
                                                    lastMessage = msgResult.content,
                                                    isGroup = msgResult.channelId.startsWith("grp-") || msgResult.channelId.startsWith("grp_") || msgResult.channelId.startsWith("group-")
                                                )
                                                selectedFriend = tempFriend
                                                onDirectChatStateChanged(true)
                                            }
                                        }
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                ConnectoAvatar(
                                                    name = msgResult.authorName,
                                                    avatarUrl = null,
                                                    customSizeDp = 26.dp,
                                                    customFontSizeSp = 11
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                SubstringHighlightedText(
                                                    text = msgResult.authorName,
                                                    query = friendSearchQuery,
                                                    style = TextStyle(
                                                        fontSize = 13.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (!isLight) Color.White else Color(0xFF0F172A)
                                                    )
                                                )
                                            }
                                            if (msgResult.createdAt.isNotBlank()) {
                                                Text(
                                                    text = formatIsoTime(msgResult.createdAt),
                                                    fontSize = 10.sp,
                                                    color = if (!isLight) Color(0xFF64748B) else Color(0xFF94A3B8)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        SubstringHighlightedText(
                                            text = msgResult.content,
                                            query = friendSearchQuery,
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                color = if (!isLight) Color(0xFFCBD5E1) else Color(0xFF334155),
                                                lineHeight = 18.sp
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    } // end DisableSelection
                    }

                    // ================= MESSAGES TAB FAB & SPEED-DIAL =================
                    // 1. Scrim overlay when speed-dial is expanded (rgba(0, 0, 0, 0.60), zIndex below FAB layer)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isFabExpanded,
                        enter = fadeIn(animationSpec = tween(200, easing = EaseOutCubic)),
                        exit = fadeOut(animationSpec = tween(180, easing = EaseInCubic)),
                        modifier = Modifier.zIndex(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.60f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isFabExpanded = false
                                }
                        )
                    }

                    // 2. Expandable Speed-Dial Action Pills (Staggered upward with 35ms delay between each item)
                    val r1Alpha by animateFloatAsState(
                        targetValue = if (isFabExpanded) 1f else 0f,
                        animationSpec = if (isFabExpanded) tween(200, delayMillis = 0, easing = EaseOutCubic) else tween(120),
                        label = "fabR1Alpha"
                    )
                    val r1Y by animateFloatAsState(
                        targetValue = if (isFabExpanded) 0f else 16f,
                        animationSpec = if (isFabExpanded) spring(stiffness = 450f, dampingRatio = Spring.DampingRatioMediumBouncy) else tween(120),
                        label = "fabR1Y"
                    )
                    val r1Scale by animateFloatAsState(
                        targetValue = if (isFabExpanded) 1f else 0.85f,
                        animationSpec = if (isFabExpanded) spring(stiffness = 450f, dampingRatio = Spring.DampingRatioMediumBouncy) else tween(120),
                        label = "fabR1Scale"
                    )

                    val r2Alpha by animateFloatAsState(
                        targetValue = if (isFabExpanded) 1f else 0f,
                        animationSpec = if (isFabExpanded) tween(200, delayMillis = 35, easing = EaseOutCubic) else tween(120),
                        label = "fabR2Alpha"
                    )
                    val r2Y by animateFloatAsState(
                        targetValue = if (isFabExpanded) 0f else 16f,
                        animationSpec = if (isFabExpanded) spring(stiffness = 450f, dampingRatio = Spring.DampingRatioMediumBouncy) else tween(120),
                        label = "fabR2Y"
                    )
                    val r2Scale by animateFloatAsState(
                        targetValue = if (isFabExpanded) 1f else 0.85f,
                        animationSpec = if (isFabExpanded) spring(stiffness = 450f, dampingRatio = Spring.DampingRatioMediumBouncy) else tween(120),
                        label = "fabR2Scale"
                    )

                    val r3Alpha by animateFloatAsState(
                        targetValue = if (isFabExpanded) 1f else 0f,
                        animationSpec = if (isFabExpanded) tween(200, delayMillis = 70, easing = EaseOutCubic) else tween(120),
                        label = "fabR3Alpha"
                    )
                    val r3Y by animateFloatAsState(
                        targetValue = if (isFabExpanded) 0f else 16f,
                        animationSpec = if (isFabExpanded) spring(stiffness = 450f, dampingRatio = Spring.DampingRatioMediumBouncy) else tween(120),
                        label = "fabR3Y"
                    )
                    val r3Scale by animateFloatAsState(
                        targetValue = if (isFabExpanded) 1f else 0.85f,
                        animationSpec = if (isFabExpanded) spring(stiffness = 450f, dampingRatio = Spring.DampingRatioMediumBouncy) else tween(120),
                        label = "fabR3Scale"
                    )

                    val r4Alpha by animateFloatAsState(
                        targetValue = if (isFabExpanded) 1f else 0f,
                        animationSpec = if (isFabExpanded) tween(200, delayMillis = 105, easing = EaseOutCubic) else tween(120),
                        label = "fabR4Alpha"
                    )
                    val r4Y by animateFloatAsState(
                        targetValue = if (isFabExpanded) 0f else 16f,
                        animationSpec = if (isFabExpanded) spring(stiffness = 450f, dampingRatio = Spring.DampingRatioMediumBouncy) else tween(120),
                        label = "fabR4Y"
                    )
                    val r4Scale by animateFloatAsState(
                        targetValue = if (isFabExpanded) 1f else 0.85f,
                        animationSpec = if (isFabExpanded) spring(stiffness = 450f, dampingRatio = Spring.DampingRatioMediumBouncy) else tween(120),
                        label = "fabR4Scale"
                    )

                    if (isFabExpanded || r4Alpha > 0.02f || r3Alpha > 0.02f) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .zIndex(2f)
                                .padding(end = 20.dp, bottom = 86.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val haptic = LocalHapticFeedback.current
                            val isDark = !isLight

                            // Option D: Ephemeral Zero-Install Guest Room (Top row, delay 105ms)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = r4Alpha
                                        translationY = r4Y.dp.toPx()
                                        scaleX = r4Scale
                                        scaleY = r4Scale
                                    }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        } catch (_: Exception) {}
                                        isFabExpanded = false
                                        coroutineScope.launch {
                                            val res = ConnectoApiClient.createGuestRoom()
                                            if (res.isSuccess) {
                                                val room = res.getOrNull()
                                                val shareUrl = room?.shareUrl ?: "https://connecto.fun/room/${room?.roomCode ?: ""}"
                                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("Connecto Guest Room", shareUrl)
                                                clipboard?.setPrimaryClip(clip)
                                                Toast.makeText(context, "Guest Room created! Link copied: $shareUrl", Toast.LENGTH_SHORT).show()
                                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Join my Connecto Guest Room")
                                                    putExtra(android.content.Intent.EXTRA_TEXT, "Join my real-time voice and chat guest room on Connecto (no install required): $shareUrl")
                                                }
                                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Guest Room Link"))
                                            } else {
                                                Toast.makeText(context, "Failed to create guest room: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDark) Color(0xFF181C28) else Color.White,
                                    shadowElevation = 6.dp,
                                    border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0x0F000000))
                                ) {
                                    Text(
                                        text = "Guest Room",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6366F1))
                                        .shadow(6.dp, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MeetingRoom,
                                        contentDescription = "New Guest Room",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Option A: Voice Group / Audio Room (Delay 70ms)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = r3Alpha
                                        translationY = r3Y.dp.toPx()
                                        scaleX = r3Scale
                                        scaleY = r3Scale
                                    }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isFabExpanded = false
                                        showCreateVoiceGroupDialog = true
                                    }
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDark) Color(0xFF181C28) else Color.White,
                                    shadowElevation = 6.dp,
                                    border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0x0F000000))
                                ) {
                                    Text(
                                        text = "Voice Group",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                        .shadow(6.dp, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = "New Voice Group",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Option B: New Group Chat (Middle row, delay 35ms)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = r2Alpha
                                        translationY = r2Y.dp.toPx()
                                        scaleX = r2Scale
                                        scaleY = r2Scale
                                    }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isFabExpanded = false
                                        showCreateGroupChatDialog = true
                                    }
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDark) Color(0xFF181C28) else Color.White,
                                    shadowElevation = 6.dp,
                                    border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0x0F000000))
                                ) {
                                    Text(
                                        text = "New Group",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6366F1))
                                        .shadow(6.dp, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GroupAdd,
                                        contentDescription = "New Group Chat",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Option C: New Direct Chat (Bottom row, delay 0ms)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = r1Alpha
                                        translationY = r1Y.dp.toPx()
                                        scaleX = r1Scale
                                        scaleY = r1Scale
                                    }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isFabExpanded = false
                                        showAddFriendDialog = true
                                    }
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isDark) Color(0xFF181C28) else Color.White,
                                    shadowElevation = 6.dp,
                                    border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0x0F000000))
                                ) {
                                    Text(
                                        text = "New Chat",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0284C7))
                                        .shadow(6.dp, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = "New Direct Chat",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Primary Floating Action Button (Rotates 45deg from + to x with 200ms spring)
                    val haptic = LocalHapticFeedback.current
                    val fabInteraction = remember { MutableInteractionSource() }
                    val isFabPressed by fabInteraction.collectIsPressedAsState()
                    val fabScale by animateFloatAsState(
                        targetValue = if (isFabPressed) 0.97f else 1.0f,
                        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
                        label = "fabScale"
                    )
                    val fabRotation by animateFloatAsState(
                        targetValue = if (isFabExpanded) 45f else 0f,
                        animationSpec = spring(stiffness = 500f, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "fabRotation"
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .zIndex(3f)
                            .padding(end = 20.dp, bottom = 20.dp)
                            .graphicsLayer {
                                scaleX = fabScale
                                scaleY = fabScale
                            }
                            .size(56.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = CircleShape,
                                ambientColor = Color(0x592563EB),
                                spotColor = Color(0x592563EB)
                            )
                            .clip(CircleShape)
                            .background(Color(0xFF4F46E5))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable(
                                interactionSource = fabInteraction,
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isFabExpanded = !isFabExpanded
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (isFabExpanded) "Close options" else "Start conversation",
                            tint = Color.White,
                            modifier = Modifier
                                .size(26.dp)
                                .graphicsLayer {
                                    rotationZ = fabRotation
                                }
                        )
                    }
                    }
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
                                isLoading = isLoadingFriendProfile && profData == null,
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
                                    WallpaperSyncManager.applyAndSyncWallpaper(
                                        context = context,
                                        myUsername = myUser,
                                        friend = friendObj,
                                        conversationId = null,
                                        wallpaperId = wpId,
                                        customUri = null,
                                        transform = WallpaperTransform(),
                                        isGlobal = false
                                    )
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
                                    wallpaperTransform = WallpaperTransform()
                                    WallpaperSyncManager.resetAndSyncWallpaper(
                                        context = context,
                                        myUsername = myUser,
                                        friend = friendObj,
                                        conversationId = null,
                                        isGlobal = false
                                    )
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
                        derivedStateOf { listState.canScrollForward }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .navigationBarsPadding()
                    ) {
                        // Wallpaper Background Layer: Custom Photo or Preset Gradient Brush with 300ms Crossfade
                        Crossfade(
                            targetState = Triple(chatWallpaperId, customWallpaperUri, wallpaperTransform),
                            animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
                            label = "chatWallpaperCrossfade"
                        ) { (currentId, currentUri, currentTransform) ->
                            if (currentId == "custom" && !currentUri.isNullOrBlank()) {
                                val resolvedContentScale = when (currentTransform.contentScaleMode) {
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
                                    val customWallpaperModel = remember(currentUri) {
                                        if (currentUri.startsWith("file://")) {
                                            java.io.File(currentUri.removePrefix("file://"))
                                        } else if (currentUri.startsWith("/")) {
                                            java.io.File(currentUri)
                                        } else {
                                            android.net.Uri.parse(currentUri)
                                        }
                                    }

                                    AsyncImage(
                                        model = customWallpaperModel,
                                        contentDescription = null,
                                        contentScale = resolvedContentScale,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                scaleX = currentTransform.scale
                                                scaleY = currentTransform.scale
                                                translationX = currentTransform.offsetX
                                                translationY = currentTransform.offsetY
                                            }
                                            .then(
                                                if (currentTransform.blur > 0f) Modifier.blur(currentTransform.blur.dp) else Modifier
                                            )
                                    )
                                    // Legibility Scrim Overlay so chat bubbles and text remain crisp
                                    val scrimAlpha = (currentTransform.dim * 0.5f).coerceIn(0f, 0.45f)
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
                                    val activeWp = remember(currentId) {
                                        if (currentId != "default") {
                                            AVAILABLE_CHAT_WALLPAPERS.firstOrNull { it.id == currentId } ?: AVAILABLE_CHAT_WALLPAPERS.first()
                                        } else if (isLiquidGlassTheme || isGlassTheme) {
                                            AVAILABLE_CHAT_WALLPAPERS.firstOrNull { it.id == "liquid_water" } ?: AVAILABLE_CHAT_WALLPAPERS.first()
                                        } else {
                                            AVAILABLE_CHAT_WALLPAPERS.first()
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                scaleX = currentTransform.scale
                                                scaleY = currentTransform.scale
                                                translationX = currentTransform.offsetX
                                                translationY = currentTransform.offsetY
                                            }
                                            .then(
                                                if (currentTransform.blur > 0f) Modifier.blur(currentTransform.blur.dp) else Modifier
                                            )
                                            .then(
                                                if (currentId != "default") {
                                                    if (currentId == "oled") {
                                                        Modifier.background(Color(0xFF000000))
                                                    } else {
                                                        Modifier.background(
                                                            brush = if (isLight) activeWp.lightBrush else activeWp.darkBrush
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
                                                            brush = if (isLight) activeWp.lightBrush else activeWp.darkBrush
                                                        )
                                                    }
                                                }
                                            )
                                    )
                                    if (currentId != "default" && currentId != "oled") {
                                        val scrimAlpha = (currentTransform.dim * 0.35f).coerceIn(0f, 0.4f)
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
                                                    text = cleanMessageContentForDisplay(pinned.content, "Pinned message"),
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
                                val showMessagesSkeleton = isLoadingMessages && friendMessages.isEmpty()
                                androidx.compose.animation.Crossfade(
                                    targetState = showMessagesSkeleton,
                                    animationSpec = tween(durationMillis = 160),
                                    label = "directChatMessagesCrossfade"
                                ) { isSkeleton ->
                                    if (isSkeleton) {
                                        ShimmerMessageFeed(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 4.dp, vertical = 8.dp),
                                            isDark = !isLight
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
                                val deduplicatedMessages = remember(friendMessages) {
                                    friendMessages.distinctBy { it.id.removePrefix("notif_ws_").removePrefix("notif_") }
                                }
                                val latestReadMessageId = remember(deduplicatedMessages) {
                                    deduplicatedMessages.filter { it.isMe && it.isRead }.lastOrNull()?.id
                                }
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
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
                                                    text = "🔒 End-to-End Encrypted",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    itemsIndexed(
                                        deduplicatedMessages,
                                        key = { _, msg -> msg.id },
                                        contentType = { _, msg -> if (msg.content.startsWith("🎨 ") && msg.content.contains("wallpaper")) "system_pill" else if (msg.isMe) "own" else "friend" }
                                    ) { index, msg ->
                                        // Shared Wallpaper System Pill
                                        if (msg.content.startsWith("🎨 ") && msg.content.contains("wallpaper")) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = if (isLight) Color(0xE6F1F5F9) else Color(0xCC1E2433),
                                                    tonalElevation = 2.dp,
                                                    shadowElevation = 1.dp,
                                                    border = BorderStroke(1.dp, if (isLight) Color(0x20000000) else Color(0x30FFFFFF))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Text(
                                                            text = msg.content,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = if (isLight) Color(0xFF334155) else Color(0xFFCBD5E1)
                                                        )
                                                        Text(
                                                            text = "Reset",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .clickable {
                                                                    coroutineScope.launch {
                                                                        try {
                                                                            val convId = currentChatKey.ifBlank {
                                                                                selectedFriend?.let {
                                                                                    if (it.isGroup) "group-${it.id}" else "dm-${listOf(myUsername.lowercase(), it.name.lowercase()).sorted().joinToString("-")}"
                                                                                } ?: "global"
                                                                            }

                                                                            WallpaperSyncManager.resetAndSyncWallpaper(
                                                                                context = context,
                                                                                myUsername = myUsername,
                                                                                friend = selectedFriend,
                                                                                conversationId = convId,
                                                                                isGlobal = false,
                                                                                isShared = true
                                                                            )
                                                                            WallpaperResolver.clearSharedWallpaper(convId, friendPrefs)
                                                                            val (defId, defUri) = WallpaperResolver.resolveForConversation(convId, selectedFriend, myUsername, friendPrefs, context.filesDir)
                                                                            chatWallpaperId = defId
                                                                            customWallpaperUri = defUri
                                                                            wallpaperTransform = WallpaperResolver.resolveTransformForConversation(convId, selectedFriend, myUsername, friendPrefs)
                                                                            Toast.makeText(context, "Wallpaper reset to default", Toast.LENGTH_SHORT).show()
                                                                        } catch (e: Exception) {
                                                                            e.printStackTrace()
                                                                        }
                                                                    }
                                                                }
                                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            return@itemsIndexed
                                        }

                                        val prevMsg = deduplicatedMessages.getOrNull(index - 1)
                                        val nextMsg = deduplicatedMessages.getOrNull(index + 1)
                                        val isSameUserAsPrev = prevMsg != null && prevMsg.isMe == msg.isMe &&
                                            (if (msg.isMe) true else prevMsg.senderName.equals(msg.senderName, ignoreCase = true)) &&
                                            kotlin.math.abs(msg.timestampMillis - prevMsg.timestampMillis) < 3 * 60 * 1000L
                                        val isSameUserAsNext = nextMsg != null && nextMsg.isMe == msg.isMe &&
                                            (if (msg.isMe) true else nextMsg.senderName.equals(msg.senderName, ignoreCase = true)) &&
                                            kotlin.math.abs(nextMsg.timestampMillis - msg.timestampMillis) < 3 * 60 * 1000L

                                        val isDirectChat = selectedFriend?.isGroup != true
                                        val showSenderName = !isDirectChat && !isSameUserAsPrev
                                        val showAvatar = !isSameUserAsNext

                                        var dragOffsetX by remember(msg.id) { mutableStateOf(0f) }
                                        var hasTriggeredHaptic by remember(msg.id) { mutableStateOf(false) }
                                        val animatedDragOffset by animateFloatAsState(
                                            targetValue = dragOffsetX,
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                                            label = "dragOffset"
                                        )
                                        val hapticFeedback = LocalHapticFeedback.current
                                        val clipboardManager = LocalClipboardManager.current
                                        val isSelectedMsg = selectedMessageForAction?.id == msg.id

                                        val isRecentMsg = remember(msg.id) {
                                            val diff = System.currentTimeMillis() - msg.timestampMillis
                                            diff in 0..4000L
                                        }
                                        val popInAnim = remember { Animatable(if (isRecentMsg) 0f else 1f) }
                                        LaunchedEffect(msg.id) {
                                            if (isRecentMsg && popInAnim.value < 1f) {
                                                popInAnim.animateTo(
                                                    targetValue = 1f,
                                                    animationSpec = tween(durationMillis = 180, easing = EaseOutCubic)
                                                )
                                            }
                                        }
                                        val msgBubbleScale = if (isRecentMsg) 0.94f + 0.06f * popInAnim.value else 1f
                                        val msgBubbleTranslateY = if (isRecentMsg) 14f * (1f - popInAnim.value) else 0f

                                        val isMsgSelected = isMultiSelectMode && selectedMultiMessageIds.contains(msg.id)
                                        var totalDeltaX by remember(msg.id) { mutableStateOf(0f) }
                                        var totalDeltaY by remember(msg.id) { mutableStateOf(0f) }
                                        var isSwipingRight by remember(msg.id) { mutableStateOf(false) }

                                        val dragModifier = if (!isMultiSelectMode) {
                                            Modifier.pointerInput(msg.id) {
                                                detectDragGestures(
                                                    onDragStart = {
                                                        totalDeltaX = 0f
                                                        totalDeltaY = 0f
                                                        isSwipingRight = false
                                                    },
                                                    onDragEnd = {
                                                        if (dragOffsetX >= 52f) {
                                                            editingMessage = null
                                                            replyingTo = msg
                                                            try { messageInputFocusRequester.requestFocus() } catch (_: Exception) {}
                                                        }
                                                        dragOffsetX = 0f
                                                        hasTriggeredHaptic = false
                                                        isSwipingRight = false
                                                    },
                                                    onDragCancel = {
                                                        dragOffsetX = 0f
                                                        hasTriggeredHaptic = false
                                                        isSwipingRight = false
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        if (!isSwipingRight) {
                                                            totalDeltaX += dragAmount.x
                                                            totalDeltaY += dragAmount.y
                                                            // Only lock into horizontal swipe when deltaX > 12px AND deltaX > Math.abs(deltaY) * 1.5
                                                            if (totalDeltaX > 12f && totalDeltaX > kotlin.math.abs(totalDeltaY) * 1.5f) {
                                                                isSwipingRight = true
                                                                change.consume()
                                                            }
                                                        } else {
                                                            change.consume()
                                                            // Strict Single-Direction Swipe (Swipe Right --> ONLY) for ALL messages
                                                            if (dragAmount.x > 0f || dragOffsetX > 0f) {
                                                                val resistance = (1f - (dragOffsetX / 90f).coerceIn(0f, 0.85f))
                                                                val nextOffset = dragOffsetX + dragAmount.x * 0.45f * resistance
                                                                dragOffsetX = nextOffset.coerceIn(0f, 68f)
                                                            }
                                                            if (dragOffsetX >= 52f && !hasTriggeredHaptic) {
                                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                hasTriggeredHaptic = true
                                                            } else if (dragOffsetX < 52f) {
                                                                hasTriggeredHaptic = false
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        } else {
                                            Modifier.clickable {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                if (selectedMultiMessageIds.contains(msg.id)) {
                                                    selectedMultiMessageIds.remove(msg.id)
                                                    if (selectedMultiMessageIds.isEmpty()) {
                                                        isMultiSelectMode = false
                                                    }
                                                } else {
                                                    selectedMultiMessageIds.add(msg.id)
                                                }
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .animateItem()
                                                .background(if (isMsgSelected) Color(0x246366F1) else Color.Transparent)
                                                .zIndex(if (isSelectedMsg) 10f else 0f)
                                                .graphicsLayer {
                                                    scaleX = msgBubbleScale
                                                    scaleY = msgBubbleScale
                                                    translationY = msgBubbleTranslateY.dp.toPx()
                                                    if (isRecentMsg) {
                                                        alpha = 0.4f + 0.6f * popInAnim.value
                                                    }
                                                }
                                                .padding(top = if (isSameUserAsPrev) 3.dp else 10.dp)
                                                .then(dragModifier)
                                        ) {
                                            // High-contrast 32px x 32px dark circular badge (#1E2333, 1px solid rgba(255,255,255,0.14)) with white reply icon (↩)
                                            val dragDist = animatedDragOffset.coerceAtLeast(0f)
                                            if (dragDist > 3f) {
                                                val isActivated = dragDist >= 52f
                                                val progress = (dragDist / 52f).coerceIn(0f, 1f)
                                                val badgeScale = if (isActivated) 1.12f else (0.55f + 0.45f * progress)
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.CenterStart)
                                                        .padding(start = 12.dp)
                                                        .size(32.dp)
                                                        .graphicsLayer {
                                                            alpha = progress
                                                            scaleX = badgeScale
                                                            scaleY = badgeScale
                                                        }
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isActivated) Color(0xFF6366F1)
                                                            else Color(0xFF1E2333)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (isActivated) Color(0xFF6366F1)
                                                            else Color(0x24FFFFFF),
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Reply,
                                                        contentDescription = "Reply",
                                                        tint = Color(0xFFFFFFFF),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .offset { IntOffset(animatedDragOffset.roundToInt(), 0) },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Animated 22px Checkbox in Left Gutter for Multi-Select
                                                AnimatedVisibility(
                                                    visible = isMultiSelectMode,
                                                    enter = expandHorizontally() + fadeIn(),
                                                    exit = shrinkHorizontally() + fadeOut()
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(start = 12.dp, end = 4.dp)
                                                            .size(22.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isMsgSelected) Color(0xFF6366F1) else Color.Transparent)
                                                            .border(
                                                                width = if (isMsgSelected) 0.dp else 1.5.dp,
                                                                color = if (isMsgSelected) Color.Transparent else Color(0xFF64748B),
                                                                shape = CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isMsgSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Selected",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Box(modifier = Modifier.weight(1f)) {
                                                    CustomChatBubble(
                                                        message = msg,
                                                        ownBg = ownBubbleBg,
                                                        ownText = ownBubbleText,
                                                        friendBg = friendBubbleBg,
                                                        friendBorder = friendBubbleBorder,
                                                        friendText = friendBubbleText,
                                                        isSelected = selectedMessageForAction?.id == msg.id,
                                                        showAvatar = showAvatar,
                                                        showSenderName = showSenderName,
                                                        isLatestRead = (msg.id == latestReadMessageId) && (selectedFriend?.isGroup != true),
                                                        isHighlighted = msg.id == highlightedMessageId,
                                                        isSearchGlowPulse = msg.id == pulsingGlowMessageId,
                                                        searchHighlightQuery = if (isChatSearchOpen) chatSearchQuery else "",
                                                        recipientAvatarUrl = selectedFriend?.avatarUrl,
                                                        recipientName = selectedFriend?.name ?: "Friend",
                                                        onBubblePositioned = { rect ->
                                                            messageBoundsMap[msg.id] = rect
                                                        },
                                                        onMessageClick = {
                                                            if (isMultiSelectMode) {
                                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                if (selectedMultiMessageIds.contains(msg.id)) {
                                                                    selectedMultiMessageIds.remove(msg.id)
                                                                    if (selectedMultiMessageIds.isEmpty()) {
                                                                        isMultiSelectMode = false
                                                                    }
                                                                } else {
                                                                    selectedMultiMessageIds.add(msg.id)
                                                                }
                                                            } else if (selectedMessageForAction != null) {
                                                                selectedMessageForAction = if (selectedMessageForAction?.id == msg.id) null else msg
                                                            }
                                                        },
                                                    onMessageLongPress = { targetMsg ->
                                                        selectedMessageForAction = targetMsg
                                                    },
                                                    onReplyClick = {
                                                        editingMessage = null
                                                        replyingTo = msg
                                                        selectedMessageForAction = null
                                                        try { messageInputFocusRequester.requestFocus() } catch (_: Exception) {}
                                                    },
                                                    onCopyClick = {
                                                        clipboardManager.setText(AnnotatedString(msg.content))
                                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                                        selectedMessageForAction = null
                                                    },
                                                    onEditClick = {
                                                        replyingTo = null
                                                        editingMessage = msg
                                                        typedMessage = msg.content
                                                        selectedMessageForAction = null
                                                        try { messageInputFocusRequester.requestFocus() } catch (_: Exception) {}
                                                    },
                                                    onUnsendClick = {
                                                        selectedMessageForAction = null
                                                        showUnsendConfirmDialogForMsg = msg
                                                    },
                                                    onQuotedMessageClick = { quotedId ->
                                                        val targetIdx = deduplicatedMessages.indexOfFirst { it.id == quotedId }
                                                        if (targetIdx >= 0) {
                                                            coroutineScope.launch {
                                                                listState.animateScrollToItem(targetIdx + 1)
                                                            }
                                                            highlightedMessageId = quotedId
                                                            coroutineScope.launch {
                                                                delay(600)
                                                                if (highlightedMessageId == quotedId) {
                                                                    highlightedMessageId = null
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onOpenFullEmojiSheet = { targetMsg ->
                                                        showFullEmojiDialogForMessage = targetMsg
                                                    },
                                                    onReact = { targetMsg, emoji ->
                                                        val currentUid = (ConnectoApiClient.currentUserId ?: currentUsername).trim().lowercase()
                                                        val currentUname = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@").lowercase()
                                                        val prevMap = targetMsg.reactions
                                                        val newMap = prevMap.toMutableMap()
                                                        val userList = (newMap[emoji] ?: emptyList()).toMutableList()
                                                        val hasReacted = userList.any {
                                                            val cu = it.trim().removePrefix("@").lowercase()
                                                            cu == currentUname || (currentUid.isNotBlank() && cu == currentUid)
                                                        }
                                                        if (hasReacted) {
                                                            userList.removeAll {
                                                                val cu = it.trim().removePrefix("@").lowercase()
                                                                cu == currentUname || (currentUid.isNotBlank() && cu == currentUid)
                                                            }
                                                        } else {
                                                            userList.add(if (currentUid.isNotBlank()) currentUid else currentUname)
                                                        }
                                                        if (userList.isEmpty()) {
                                                            newMap.remove(emoji)
                                                        } else {
                                                            newMap[emoji] = userList
                                                        }
                                                        val primaryReaction = newMap.keys.firstOrNull()

                                                        // OPTIMISTIC UPDATE: Instant 0ms Compose state update
                                                        friendMessages = friendMessages.map { m ->
                                                            if (m.id == targetMsg.id) m.copy(reactions = newMap, reaction = primaryReaction) else m
                                                        }
                                                        selectedMessageForAction = null

                                                        val targetChan = activeDmChannelId ?: (if (selectedFriend?.isGroup == true) (selectedFriend?.id ?: "") else getCanonicalChatId(selectedFriend))
                                                        val recipientU = selectedFriend?.handle?.removePrefix("@") ?: selectedFriend?.name?.removePrefix("@")
                                                        VoiceCallManager.sendReactionToggled(
                                                            messageId = targetMsg.id,
                                                            emoji = emoji,
                                                            userId = if (currentUid.isNotBlank()) currentUid else currentUname,
                                                            channelId = targetChan.ifBlank { null },
                                                            recipientUsername = recipientU
                                                        )

                                                        coroutineScope.launch(Dispatchers.IO) {
                                                            try {
                                                                dbHelper.updateMessageReaction(targetMsg.id, primaryReaction)
                                                            } catch (_: Exception) {}
                                                        }
                                                        coroutineScope.launch {
                                                            try {
                                                                ConnectoApiClient.reactToMessage(
                                                                    messageId = targetMsg.id,
                                                                    emoji = emoji,
                                                                    channelId = targetChan.ifBlank { null }
                                                                )
                                                            } catch (_: Exception) {}
                                                        }
                                                    }
                                                )
                                            }
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
                            }

                            // Floating "Scroll to Bottom" FAB (Bottom-Right corner: right 16px, 12px above chips/input dock)
                            if (friendMessages.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 16.dp, bottom = 12.dp)
                                        .zIndex(6f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    AnimatedVisibility(
                                        visible = canScrollDown,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 240, easing = EaseOutCubic)) +
                                                scaleIn(initialScale = 0.65f, animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)) +
                                                slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)),
                                        exit = fadeOut(animationSpec = tween(durationMillis = 180, easing = EaseInCubic)) +
                                               scaleOut(targetScale = 0.65f, animationSpec = tween(durationMillis = 180, easing = EaseInCubic)) +
                                               slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(durationMillis = 180, easing = EaseInCubic))
                                    ) {
                                        val haptic = LocalHapticFeedback.current
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .pressScaleEffect(
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        coroutineScope.launch {
                                                            val count = friendMessages.size
                                                            if (count > 0) {
                                                                try {
                                                                    listState.animateScrollToItem(count - 1)
                                                                } catch (_: Exception) {
                                                                    listState.scrollToItem(count - 1)
                                                                }
                                                            }
                                                        }
                                                    },
                                                    targetScale = 0.90f
                                                )
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(
                                                    width = 1.dp,
                                                    color = Color.White.copy(alpha = 0.12f),
                                                    shape = CircleShape
                                                )
                                                .shadow(elevation = 6.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.35f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Scroll to latest message",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(20.dp)
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
                                        val chipBg = if (isLight) Color(0xFFF1F5F9) else Color(0xFF161922)
                                        val chipBorder = if (isLight) Color(0xFFE2E8F0) else Color(0x0FFFFFFF)
                                        val chipTextColor = if (isLight) Color(0xFF0F172A) else Color(0xFFE4E4E7)

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(chipBg)
                                                .border(1.dp, chipBorder, RoundedCornerShape(14.dp))
                                                .clickable(
                                                    interactionSource = chipInteractionSource,
                                                    indication = null
                                                ) {
                                                    typedMessage = chipText
                                                }
                                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = chipText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = chipTextColor
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
                        val selFriendForInput = selectedFriend
                        val isAcceptedFriend = selFriendForInput == null || selFriendForInput.isGroup || friendsList.any {
                            it.id == selFriendForInput.id ||
                            it.handle.removePrefix("@").equals(selFriendForInput.handle.removePrefix("@"), ignoreCase = true)
                        }

                        if (selFriendForInput != null && !isAcceptedFriend) {
                            val targetHandle = selFriendForInput.handle.removePrefix("@")
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
                        } else if (isMultiSelectMode) {
                            // Bottom Action Dock: Replace the message input bar smoothly with a clean #12151E bulk action dock containing evenly spaced icon + label buttons
                            val clipboard = LocalClipboardManager.current
                            val selectedMsgs = remember(selectedMultiMessageIds.size, friendMessages) {
                                friendMessages.filter { selectedMultiMessageIds.contains(it.id) }.sortedBy { it.timestampMillis }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF12151E))
                                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(18.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Copy Button
                                    val isCopyEnabled = selectedMsgs.isNotEmpty()
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable(enabled = isCopyEnabled) {
                                                val distinctSenders = selectedMsgs.map { it.senderName }.distinct()
                                                val formattedText = if (distinctSenders.size > 1) {
                                                    selectedMsgs.joinToString("\n") { "[${it.senderName}]: ${it.content}" }
                                                } else {
                                                    selectedMsgs.joinToString("\n") { it.content }
                                                }
                                                clipboard.setText(AnnotatedString(formattedText))
                                                Toast.makeText(context, "Copied ${selectedMsgs.size} messages", Toast.LENGTH_SHORT).show()
                                                isMultiSelectMode = false
                                                selectedMultiMessageIds.clear()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = if (isCopyEnabled) Color.White else Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Copy",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isCopyEnabled) Color.White else Color(0xFF64748B)
                                        )
                                    }

                                    // 2. Forward Button
                                    val isForwardEnabled = selectedMsgs.isNotEmpty()
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable(enabled = isForwardEnabled) {
                                                showForwardSheet = true
                                            }
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Reply,
                                            contentDescription = "Forward",
                                            tint = if (isForwardEnabled) Color.White else Color(0xFF64748B),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .graphicsLayer { scaleX = -1f }
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Forward",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isForwardEnabled) Color.White else Color(0xFF64748B)
                                        )
                                    }

                                    // 3. Delete Button
                                    val isDeleteEnabled = selectedMsgs.isNotEmpty()
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable(enabled = isDeleteEnabled) {
                                                showDeleteConfirmDialog = true
                                            }
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = if (isDeleteEnabled) Color(0xFFEF4444) else Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Delete",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDeleteEnabled) Color(0xFFEF4444) else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coords ->
                                        chatInputBarHeightPx = coords.size.height
                                    }
                            ) {
                                // Compact Swipe-to-Reply active preview bar (48px height)
                            AnimatedVisibility(
                                visible = replyingTo != null,
                                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(180)) + expandVertically(),
                                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(tween(150)) + shrinkVertically()
                            ) {
                                val replyTarget = replyingTo
                                if (replyTarget != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .padding(horizontal = 16.dp, vertical = 2.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isLiquidGlassTheme) {
                                                    val alpha = ConnectoTheme.glassAlpha
                                                    if (alpha <= 0.005f) Color.Transparent
                                                    else Color.White.copy(alpha = (alpha * 0.25f).coerceIn(0.01f, 0.35f))
                                                }
                                                else if (isPureBlack) Color(0x1AFFFFFF)
                                                else if (isLight) Color(0xFFF1F5F9)
                                                else Color(0xFF161922)
                                            )
                                            .border(
                                                1.dp,
                                                if (isLight) Color(0x14000000) else Color(0x1FFFFFFF),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(28.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = if (replyTarget.isMe) "Replying to yourself" else "Replying to ${replyTarget.senderName}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = cleanMessageContentForDisplay(replyTarget.content, "Replying to message"),
                                                fontSize = 13.sp,
                                                color = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8),
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
                                                tint = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Active Editing Message Banner (48px height)
                            AnimatedVisibility(
                                visible = editingMessage != null,
                                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(180)) + expandVertically(),
                                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(tween(150)) + shrinkVertically()
                            ) {
                                editingMessage?.let { editTarget ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .padding(horizontal = 16.dp, vertical = 2.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isLight) Color(0xFFF1F5F9) else Color(0xFF161922)
                                            )
                                            .border(
                                                1.dp,
                                                if (isLight) Color(0x14000000) else Color(0x1FFFFFFF),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(28.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "Editing message",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = cleanMessageContentForDisplay(editTarget.content, "Editing message"),
                                                fontSize = 13.sp,
                                                color = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8),
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
                                                tint = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8),
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
                            val inputContainerBg = if (isLiquidGlassTheme) {
                                val alpha = ConnectoTheme.glassAlpha
                                if (alpha <= 0.005f) Color.Transparent
                                else Color.White.copy(alpha = (alpha * 0.28f).coerceIn(0.01f, 0.40f))
                            } else if (isPureBlack) {
                                Color(0x1AFFFFFF)
                            } else if (isLight) {
                                Color(0xFFFFFFFF)
                            } else {
                                Color(0xFF161922)
                            }

                            val inputContainerBorder = if (isLight) {
                                if (isInputFocused) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)
                            } else {
                                if (isInputFocused) Color(0x33FFFFFF) else Color(0x0FFFFFFF)
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 46.dp, max = 120.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(inputContainerBg)
                                    .border(
                                        width = 1.dp,
                                        color = inputContainerBorder,
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
                                        .focusRequester(messageInputFocusRequester)
                                        .onFocusChanged { isInputFocused = it.isFocused },
                                    textStyle = TextStyle(
                                        color = if (isLight) Color(0xFF0F172A) else Color.White,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    ),
                                    cursorBrush = SolidColor(if (isLight) Color(0xFF0F172A) else MaterialTheme.colorScheme.primary),
                                    maxLines = 4,
                                    decorationBox = { inner ->
                                        if (typedMessage.isEmpty()) {
                                            Text(
                                                text = "Type message for " + (selectedFriend?.name ?: "friend") + "...",
                                                color = if (isLight) Color(0xFF94A3B8) else Color(0xFF717686),
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
                                        tint = if (showEmojiPicker) MaterialTheme.colorScheme.primary else (if (isLight) Color(0xFF64748B) else Color(0xFF8E929F)),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Dynamic Send Action Button with Hold-to-Send Silent & Whisper support
                            var showHoldToSendMenu by remember { mutableStateOf(false) }
                            val onSendMessage: (Boolean, Boolean) -> Unit = { isSilent, isWhisper ->
                                val targetToEdit = editingMessage
                                if (targetToEdit != null) {
                                    val editTarget = targetToEdit
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
                                        timestampMillis = nowMillis,
                                        isSilent = isSilent,
                                        isWhisper = isWhisper
                                    )
                                    val updatedList = (friendMessages + localItem).sortedBy { it.timestampMillis }
                                    friendMessages = updatedList
                                    isLoadingMessages = false

                                    // Display preview text: for whispers, show 'Sent a whisper' in DM list
                                    val previewText = if (isWhisper) "🫧 Sent a whisper" else userMsg

                                    // Move current friend/group to index 0 of conversations list with latest message and timestamp
                                    selectedFriend?.let { friend ->
                                        val fIdx = friendsList.indexOfFirst {
                                            it.id == friend.id || it.handle.removePrefix("@").equals(friend.handle.removePrefix("@"), ignoreCase = true)
                                        }
                                        if (fIdx != -1) {
                                            val updated = friendsList[fIdx].copy(
                                                lastMessage = previewText,
                                                timeAgo = "Just now",
                                                unreadCount = 0,
                                                lastMessageTimestamp = nowMillis
                                            )
                                            friendsList.removeAt(fIdx)
                                            friendsList.add(0, updated)
                                            val cleanHandle = updated.handle.removePrefix("@").trim().lowercase()
                                            readPrefs.edit()
                                                .putString("last_msg_${updated.id}", previewText)
                                                .putString("last_msg_${cleanHandle}", previewText)
                                                .putString("last_time_${updated.id}", "Just now")
                                                .putString("last_time_${cleanHandle}", "Just now")
                                                .putLong("last_timestamp_${updated.id}", nowMillis)
                                                .putLong("last_timestamp_${cleanHandle}", nowMillis)
                                                .apply()
                                            coroutineScope.launch(Dispatchers.IO) {
                                                try {
                                                    dbHelper.updateFriendLastMessageAndUnread(updated.id, previewText, "Just now", 0, nowMillis)
                                                } catch (_: Exception) {}
                                            }
                                            notifyUnreadChanged()
                                        } else {
                                            val gIdx = userGroupsList.indexOfFirst { it.id == friend.id }
                                            if (gIdx != -1) {
                                                val updated = userGroupsList[gIdx].copy(
                                                    lastMessage = previewText,
                                                    timeAgo = "Just now",
                                                    unreadCount = 0,
                                                    lastMessageTimestamp = nowMillis
                                                )
                                                userGroupsList.removeAt(gIdx)
                                                userGroupsList.add(0, updated)
                                                readPrefs.edit()
                                                    .putString("last_msg_${updated.id}", previewText)
                                                    .putString("last_time_${updated.id}", "Just now")
                                                    .putInt("unread_${updated.id}", 0)
                                                    .putLong("last_timestamp_${updated.id}", nowMillis)
                                                    .apply()
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    try {
                                                        dbHelper.updateGroupLastMessageAndUnread(updated.id, previewText, "Just now", 0, nowMillis)
                                                    } catch (_: Exception) {}
                                                }
                                                notifyUnreadChanged()
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
                                                    replyToAuthor = currentReply?.senderName,
                                                    isSilent = isSilent,
                                                    isWhisper = isWhisper
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
                                                replyToAuthor = currentReply?.senderName,
                                                isSilent = isSilent,
                                                isWhisper = isWhisper
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
                                                    timestampMillis = if (sentMillis > 0) sentMillis else nowMillis,
                                                    isSilent = sentDto.isSilent,
                                                    isWhisper = sentDto.isWhisper
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
                                modifier = Modifier.wrapContentSize(),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                val haptic = LocalHapticFeedback.current
                                if (showHoldToSendMenu) {
                                    val pillScale by animateFloatAsState(
                                        targetValue = if (showHoldToSendMenu) 1f else 0.85f,
                                        animationSpec = spring(stiffness = 380f, dampingRatio = 0.75f),
                                        label = "holdPillScale"
                                    )
                                    val pillAlpha by animateFloatAsState(
                                        targetValue = if (showHoldToSendMenu) 1f else 0f,
                                        animationSpec = tween(160, easing = EaseOutCubic),
                                        label = "holdPillAlpha"
                                    )

                                    androidx.compose.ui.window.Popup(
                                        alignment = Alignment.TopEnd,
                                        offset = androidx.compose.ui.unit.IntOffset(0, -220),
                                        onDismissRequest = { showHoldToSendMenu = false }
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(18.dp),
                                            color = Color(0xFF181C28),
                                            shadowElevation = 10.dp,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    scaleX = pillScale
                                                    scaleY = pillScale
                                                    alpha = pillAlpha
                                                }
                                                .padding(bottom = 8.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                // Option 1: Send Silently
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .clickable {
                                                            try {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            } catch (_: Exception) {}
                                                            showHoldToSendMenu = false
                                                            onSendMessage(true, false)
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Text(text = "🔕", fontSize = 16.sp)
                                                    Text(
                                                        text = "Send Silently",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White
                                                    )
                                                }

                                                // Option 2: Send as Whisper
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .clickable {
                                                            try {
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            } catch (_: Exception) {}
                                                            showHoldToSendMenu = false
                                                            onSendMessage(false, true)
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Text(text = "🫧", fontSize = 16.sp)
                                                    Text(
                                                        text = "Send as Whisper",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White
                                                    )
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
                                        .pointerInput(typedMessage) {
                                            detectTapGestures(
                                                onTap = {
                                                    if (typedMessage.isNotBlank()) {
                                                        onSendMessage(false, false)
                                                    }
                                                },
                                                onLongPress = {
                                                    if (typedMessage.isNotBlank()) {
                                                        try {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        } catch (_: Exception) {}
                                                        showHoldToSendMenu = true
                                                    }
                                                }
                                            )
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
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        } // end Bottom Chat Input Area Column
                    } // end Main Messages Column

                    // Sleek, compact floating Undo toast pill docked cleanly 12px above the message input bar
                    val density = LocalDensity.current
                    val bottomToastOffsetDp = remember(chatInputBarHeightPx) {
                        if (chatInputBarHeightPx > 0) {
                            with(density) { chatInputBarHeightPx.toDp() } + 12.dp
                        } else {
                            76.dp
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = pendingDeleteMessage != null,
                            enter = slideInVertically(
                                initialOffsetY = { fullHeight -> fullHeight },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)
                            ) + fadeIn(tween(180)),
                            exit = slideOutVertically(
                                targetOffsetY = { fullHeight -> fullHeight },
                                animationSpec = tween(150, easing = EaseInCubic)
                            ) + fadeOut(tween(150)),
                            modifier = Modifier
                                .padding(bottom = bottomToastOffsetDp)
                                .padding(horizontal = 20.dp)
                                .zIndex(95f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF181C28),
                                border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures { _, dragAmount ->
                                            if (dragAmount > 8f) {
                                                flushPendingDelete()
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 3.0s Subtle Countdown Progress Ring
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .drawBehind {
                                                drawArc(
                                                    color = Color(0x33818CF8),
                                                    startAngle = -90f,
                                                    sweepAngle = 360f,
                                                    useCenter = false,
                                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                                )
                                                drawArc(
                                                    color = Color(0xFF818CF8),
                                                    startAngle = -90f,
                                                    sweepAngle = 360f * pendingDeleteProgress.coerceIn(0f, 1f),
                                                    useCenter = false,
                                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                                )
                                            }
                                    )
                                    Text(
                                        text = "Message deleted for you",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Undo",
                                        color = Color(0xFF818CF8),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                undoPendingDelete()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    } // end Wallpaper Box
                }
                } // end when (screenState)
            }
        }

        // Full-Screen Root Portal Modal Layer (zIndex: 999) for Long-Press Message Focus
        val activeSelectedMsg = selectedMessageForAction
        ConnectoLongPressFocusOverlay(
            visible = activeSelectedMsg != null,
            bubbleBounds = activeSelectedMsg?.let { messageBoundsMap[it.id] },
            isMe = activeSelectedMsg?.isMe == true,
            isLight = isLight,
            onDismiss = { selectedMessageForAction = null },
            onReact = { emoji ->
                activeSelectedMsg?.let { targetMsg ->
                    val currentUid = (ConnectoApiClient.currentUserId ?: currentUsername).trim().lowercase()
                    val currentUname = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@").lowercase()
                    val prevMap = targetMsg.reactions
                    val newMap = prevMap.toMutableMap()
                    val userList = (newMap[emoji] ?: emptyList()).toMutableList()
                    val hasReacted = userList.any {
                        val cu = it.trim().removePrefix("@").lowercase()
                        cu == currentUname || (currentUid.isNotBlank() && cu == currentUid)
                    }
                    if (hasReacted) {
                        userList.removeAll {
                            val cu = it.trim().removePrefix("@").lowercase()
                            cu == currentUname || (currentUid.isNotBlank() && cu == currentUid)
                        }
                    } else {
                        userList.add(if (currentUid.isNotBlank()) currentUid else currentUname)
                    }
                    if (userList.isEmpty()) {
                        newMap.remove(emoji)
                    } else {
                        newMap[emoji] = userList
                    }
                    val primaryReaction = newMap.keys.firstOrNull()

                    // OPTIMISTIC UPDATE: Instant 0ms Compose state update
                    friendMessages = friendMessages.map { m ->
                        if (m.id == targetMsg.id) m.copy(reactions = newMap, reaction = primaryReaction) else m
                    }
                    selectedMessageForAction = null

                    val targetChan = activeDmChannelId ?: (if (selectedFriend?.isGroup == true) (selectedFriend?.id ?: "") else getCanonicalChatId(selectedFriend))
                    val recipientU = selectedFriend?.handle?.removePrefix("@") ?: selectedFriend?.name?.removePrefix("@")
                    VoiceCallManager.sendReactionToggled(
                        messageId = targetMsg.id,
                        emoji = emoji,
                        userId = if (currentUid.isNotBlank()) currentUid else currentUname,
                        channelId = targetChan.ifBlank { null },
                        recipientUsername = recipientU
                    )

                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            dbHelper.updateMessageReaction(targetMsg.id, primaryReaction)
                        } catch (_: Exception) {}
                    }
                    coroutineScope.launch {
                        try {
                            ConnectoApiClient.reactToMessage(
                                messageId = targetMsg.id,
                                emoji = emoji,
                                channelId = targetChan.ifBlank { null }
                            )
                        } catch (_: Exception) {}
                    }
                }
            },
            onOpenFullEmojiSheet = {
                showFullEmojiDialogForMessage = activeSelectedMsg
                selectedMessageForAction = null
            },
            actions = {
                val clipboard = LocalClipboardManager.current
                if (activeSelectedMsg != null) {
                    ActionMenuItem(
                        icon = Icons.AutoMirrored.Filled.Reply,
                        label = "Reply",
                        color = if (isLight) Color(0xFF0F172A) else Color.White,
                        onClick = {
                            editingMessage = null
                            replyingTo = activeSelectedMsg
                            selectedMessageForAction = null
                            try { messageInputFocusRequester.requestFocus() } catch (_: Exception) {}
                        }
                    )
                    ActionMenuItem(
                        icon = Icons.Default.ContentCopy,
                        label = "Copy",
                        color = if (isLight) Color(0xFF0F172A) else Color.White,
                        onClick = {
                            clipboard.setText(AnnotatedString(activeSelectedMsg.content))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            selectedMessageForAction = null
                        }
                    )
                    ActionMenuItem(
                        icon = Icons.Default.CheckCircle,
                        label = "Select",
                        color = if (isLight) Color(0xFF0F172A) else Color.White,
                        onClick = {
                            selectedMultiMessageIds.clear()
                            selectedMultiMessageIds.add(activeSelectedMsg.id)
                            isMultiSelectMode = true
                            selectedMessageForAction = null
                        }
                    )
                    val isSeen = activeSelectedMsg.isRead || !activeSelectedMsg.readAt.isNullOrBlank() || activeSelectedMsg.seenBy.isNotEmpty()

                    // Edit Feature Alignment: Lock Edit once seen, leaving Reply, Copy, Select, and Delete
                    if (activeSelectedMsg.isMe && !isSeen) {
                        ActionMenuItem(
                            icon = Icons.Default.Edit,
                            label = "Edit",
                            color = if (isLight) Color(0xFF0F172A) else Color.White,
                            onClick = {
                                replyingTo = null
                                editingMessage = activeSelectedMsg
                                typedMessage = activeSelectedMsg.content
                                selectedMessageForAction = null
                                try { messageInputFocusRequester.requestFocus() } catch (_: Exception) {}
                            }
                        )
                    }

                    if (activeSelectedMsg.isMe) {
                        if (!isSeen) {
                            // Unseen sent message: display "Unsend" (red trash icon)
                            ActionMenuItem(
                                icon = Icons.Default.Delete,
                                label = "Unsend",
                                color = Color(0xFFEF4444),
                                onClick = {
                                    val targetMsg = activeSelectedMsg
                                    selectedMessageForAction = null
                                    showUnsendConfirmDialogForMsg = targetMsg
                                }
                            )
                        } else {
                            // Seen sent message: automatically replaces Unsend with "Delete" (Delete for you only)
                            ActionMenuItem(
                                icon = Icons.Default.Delete,
                                label = "Delete",
                                color = Color(0xFFEF4444),
                                onClick = {
                                    val targetMsg = activeSelectedMsg
                                    selectedMessageForAction = null
                                    startPendingDelete(targetMsg)
                                }
                            )
                        }
                    } else {
                        // Received messages (sent by others): display "Delete" (delete for me only)
                        ActionMenuItem(
                            icon = Icons.Default.Delete,
                            label = "Delete",
                            color = Color(0xFFEF4444),
                            onClick = {
                                val targetMsg = activeSelectedMsg
                                selectedMessageForAction = null
                                startPendingDelete(targetMsg)
                            }
                        )
                    }
                }
            },
            bubbleContent = {
                if (activeSelectedMsg != null) {
                    CustomChatBubble(
                        message = activeSelectedMsg,
                        ownBg = ownBubbleBg,
                        ownText = ownBubbleText,
                        friendBg = friendBubbleBg,
                        friendBorder = friendBubbleBorder,
                        friendText = friendBubbleText,
                        isSelected = false,
                        showAvatar = false,
                        showSenderName = false,
                        isLatestRead = false,
                        isHighlighted = false,
                        recipientAvatarUrl = selectedFriend?.avatarUrl,
                        recipientName = selectedFriend?.name ?: "Friend",
                        onMessageClick = { selectedMessageForAction = null },
                        onMessageLongPress = {}
                    )
                }
            }
        )

        // Multi-Select Forward Sheet
        if (showForwardSheet) {
            val forwardTargets = remember(friendsList.size, userGroupsList.size) {
                (friendsList + userGroupsList).distinctBy { it.id }
            }
            val selectedMsgs = remember(selectedMultiMessageIds.size, friendMessages) {
                friendMessages.filter { selectedMultiMessageIds.contains(it.id) }.sortedBy { it.timestampMillis }
            }

            Dialog(
                onDismissRequest = { showForwardSheet = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000))
                        .clickable { showForwardSheet = false },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = false) {}
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color(0xFF13161F))
                            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Forward to...",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                IconButton(onClick = { showForwardSheet = false }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(forwardTargets) { target ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1C202D))
                                            .clickable {
                                                val myName = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@")
                                                val targetChan = if (target.isGroup) target.id else "dm-${minOf(myName.lowercase(), target.handle.removePrefix("@").trim().lowercase())}-${maxOf(myName.lowercase(), target.handle.removePrefix("@").trim().lowercase())}"
                                                coroutineScope.launch {
                                                    for (msgToFwd in selectedMsgs) {
                                                        try {
                                                            ConnectoApiClient.sendMessage(
                                                                channelId = targetChan,
                                                                content = msgToFwd.content,
                                                                replyToId = null
                                                            )
                                                        } catch (_: Exception) {}
                                                    }
                                                }
                                                Toast.makeText(context, "Forwarded ${selectedMsgs.size} messages to ${target.name}", Toast.LENGTH_SHORT).show()
                                                showForwardSheet = false
                                                isMultiSelectMode = false
                                                selectedMultiMessageIds.clear()
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ConnectoAvatar(
                                            name = target.name,
                                            avatarUrl = target.avatarUrl,
                                            customSizeDp = 36.dp,
                                            customFontSizeSp = 14
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = target.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = if (target.isGroup) "Group" else target.handle,
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF94A3B8)
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

        // Multi-Select Smart Delete Confirmation Dialog
        if (showDeleteConfirmDialog) {
            val selectedMsgs = remember(selectedMultiMessageIds.size, friendMessages) {
                friendMessages.filter { selectedMultiMessageIds.contains(it.id) }.sortedBy { it.timestampMillis }
            }
            val allMine = remember(selectedMsgs) {
                selectedMsgs.isNotEmpty() && selectedMsgs.all { it.isMe }
            }
            val anySeen = remember(selectedMsgs) {
                selectedMsgs.any { it.isRead || !it.readAt.isNullOrBlank() || it.seenBy.isNotEmpty() }
            }

            Dialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000))
                        .clickable { showDeleteConfirmDialog = false },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = false) {}
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color(0xFF13161F))
                            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .padding(horizontal = 18.dp, vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Delete ${selectedMsgs.size} message${if (selectedMsgs.size > 1) "s" else ""}?",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (allMine && !anySeen) "You can unsend these messages for everyone in this chat or delete them for yourself only."
                                else if (allMine) "Some messages have already been seen and can only be deleted for yourself."
                                else "Selected messages include incoming messages and will be deleted for you only.",
                                fontSize = 13.sp,
                                color = Color(0xFF8E95A8)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            if (allMine && !anySeen) {
                                // Unsend for Everyone (Only for unseen messages)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFEF4444))
                                        .clickable {
                                            val targetChan = activeDmChannelId ?: (if (selectedFriend?.isGroup == true) (selectedFriend?.id ?: "") else getCanonicalChatId(selectedFriend))
                                            val ids = selectedMsgs.map { it.id }.toSet()
                                            friendMessages = friendMessages.filterNot { ids.contains(it.id) }
                                            val deletedSet = friendPrefs.getStringSet("deleted_for_me_$myUsername", emptySet())?.toMutableSet() ?: mutableSetOf()
                                            deletedSet.addAll(ids)
                                            friendPrefs.edit().putStringSet("deleted_for_me_$myUsername", deletedSet).apply()

                                            coroutineScope.launch(Dispatchers.IO) {
                                                for (msgId in ids) {
                                                    try { dbHelper.deleteMessage(msgId) } catch (_: Exception) {}
                                                }
                                            }
                                            coroutineScope.launch {
                                                for (msgId in ids) {
                                                    try {
                                                        ConnectoApiClient.unsendMessage(msgId, targetChan.ifBlank { null })
                                                    } catch (_: Exception) {}
                                                }
                                            }
                                            Toast.makeText(context, "Unsent ${ids.size} messages", Toast.LENGTH_SHORT).show()
                                            showDeleteConfirmDialog = false
                                            isMultiSelectMode = false
                                            selectedMultiMessageIds.clear()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Unsend for Everyone",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Delete for Me
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1C202D))
                                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                                    .clickable {
                                        val ids = selectedMsgs.map { it.id }.toSet()
                                        friendMessages = friendMessages.filterNot { ids.contains(it.id) }
                                        val deletedSet = friendPrefs.getStringSet("deleted_for_me_$myUsername", emptySet())?.toMutableSet() ?: mutableSetOf()
                                        deletedSet.addAll(ids)
                                        friendPrefs.edit().putStringSet("deleted_for_me_$myUsername", deletedSet).apply()

                                        coroutineScope.launch(Dispatchers.IO) {
                                            for (msgId in ids) {
                                                try { dbHelper.deleteMessage(msgId) } catch (_: Exception) {}
                                            }
                                        }
                                        coroutineScope.launch {
                                            for (msgId in ids) {
                                                try {
                                                    ConnectoApiClient.deleteMessageForMe(msgId, myUsername)
                                                } catch (_: Exception) {}
                                            }
                                        }
                                        Toast.makeText(context, "Deleted ${ids.size} messages for me", Toast.LENGTH_SHORT).show()
                                        showDeleteConfirmDialog = false
                                        isMultiSelectMode = false
                                        selectedMultiMessageIds.clear()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Delete for Me",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (allMine) Color.White else Color(0xFFEF4444)
                                )
                            }

                            // Cancel
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showDeleteConfirmDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF8E95A8)
                                )
                            }
                        }
                    }
                }
            }
        }
        // Centered Obsidian Confirmation Modal for Irreversible Unsend
        if (showUnsendConfirmDialogForMsg != null) {
            val msgToUnsend = showUnsendConfirmDialogForMsg
            Dialog(
                onDismissRequest = { showUnsendConfirmDialogForMsg = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000))
                        .clickable { showUnsendConfirmDialogForMsg = null },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clickable(enabled = false) {}
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF161922))
                            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(20.dp))
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Unsend message?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This will permanently remove the message for everyone in this chat.",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 19.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Cancel (neutral pill)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1C202D))
                                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                                        .clickable {
                                            showUnsendConfirmDialogForMsg = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Cancel",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                // Unsend (solid red #EF4444 pill)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFEF4444))
                                        .clickable {
                                            showUnsendConfirmDialogForMsg = null
                                            if (msgToUnsend != null) {
                                                val msgId = msgToUnsend.id
                                                val targetChan = activeDmChannelId ?: (if (selectedFriend?.isGroup == true) (selectedFriend?.id ?: "") else getCanonicalChatId(selectedFriend))
                                                coroutineScope.launch {
                                                    val res = ConnectoApiClient.unsendMessage(msgId, targetChan.ifBlank { null })
                                                    if (res.isSuccess) {
                                                        friendMessages = friendMessages.filterNot { it.id == msgId }
                                                        withContext(Dispatchers.IO) {
                                                            try { dbHelper.deleteMessage(msgId) } catch (_: Exception) {}
                                                        }
                                                        Toast.makeText(context, "Message unsent", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        val errMsg = res.exceptionOrNull()?.message ?: ""
                                                        if (errMsg.contains("ALREADY_SEEN", ignoreCase = true)) {
                                                            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                                                            friendMessages = friendMessages.map { m ->
                                                                if (m.id == msgId) m.copy(isDelivered = true, isRead = true, readAt = nowIso)
                                                                else m
                                                            }
                                                            Toast.makeText(context, "Message was just seen — you can only delete it for yourself", Toast.LENGTH_LONG).show()
                                                        } else {
                                                            Toast.makeText(context, "Failed to unsend: $errMsg", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Unsend",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
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

@Composable
fun ActionMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
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
    showAvatar: Boolean = true,
    showSenderName: Boolean = true,
    isLatestRead: Boolean = false,
    isHighlighted: Boolean = false,
    isSearchGlowPulse: Boolean = false,
    searchHighlightQuery: String = "",
    recipientAvatarUrl: String? = null,
    recipientName: String = "Friend",
    onMessageClick: (() -> Unit)? = null,
    onMessageLongPress: (CustomMessageItem) -> Unit = {},
    onReact: ((CustomMessageItem, String) -> Unit)? = null,
    onReplyClick: (() -> Unit)? = null,
    onCopyClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onUnsendClick: (() -> Unit)? = null,
    onQuotedMessageClick: ((String) -> Unit)? = null,
    onOpenFullEmojiSheet: ((CustomMessageItem) -> Unit)? = null,
    onBubblePositioned: ((Rect) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isLight = !ConnectoTheme.colors.isDark

    // 600ms Subtle Background Pulse Animation for Scroll-to-Original
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.35f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "pulseAlpha"
    )

    // 1.2s Soft Accent Glow Pulse for In-Chat Search Navigation
    val searchGlowAlpha by animateFloatAsState(
        targetValue = if (isSearchGlowPulse) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "searchGlowAlpha"
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
            val effectiveAvatar = message.avatarUrl?.takeIf { it.isNotBlank() } ?: recipientAvatarUrl?.takeIf { it.isNotBlank() }
            val effectiveName = if (message.senderName.isNotBlank() && !message.senderName.equals("gamer", ignoreCase = true)) message.senderName else recipientName
            if (showAvatar) {
                ConnectoAvatar(
                    name = effectiveName,
                    avatarUrl = effectiveAvatar,
                    customSizeDp = 28.dp,
                    customFontSizeSp = 12,
                    borderWidth = 1.dp,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            } else {
                Spacer(modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(min = 60.dp, max = 300.dp)
        ) {
            if (!message.isMe && showSenderName && message.senderName.isNotBlank() && !message.senderName.equals("gamer", ignoreCase = true)) {
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
            val bubbleShape = if (message.isMe) {
                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
            } else {
                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
            }

            val isWhiteBubble = ownBg == Color.White || ownBg == Color(0xFFFAFAFA)
            val effectiveOwnBg = ownBg
            val effectiveOwnText = ownText

            val bubbleModifier = if (isGlassTheme || isLiquidGlassTheme) {
                Modifier.glassBubble(
                    isMe = message.isMe,
                    shape = bubbleShape,
                    isLiquid = isLiquidGlassTheme
                )
            } else {
                val bgBrush = remember(message.isMe, isWhiteBubble, effectiveOwnBg, friendBg) {
                    if (message.isMe) {
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
                }

                val borderBrush = remember(message.isMe, isWhiteBubble, effectiveOwnText, friendBorder) {
                    if (message.isMe) {
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
                }

                Modifier
                    .clip(bubbleShape)
                    .background(bgBrush)
                    .border(
                        width = 0.8.dp,
                        brush = borderBrush,
                        shape = bubbleShape
                    )
            }

            val pulseModifier = if (pulseAlpha > 0f) {
                Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha), bubbleShape)
            } else Modifier

            val searchGlowModifier = if (searchGlowAlpha > 0.01f) {
                Modifier.border(
                    width = 1.8.dp,
                    color = Color(0xFF818CF8).copy(alpha = searchGlowAlpha),
                    shape = bubbleShape
                )
            } else Modifier

            Box(
                modifier = bubbleModifier
                    .then(pulseModifier)
                    .then(searchGlowModifier)
                    .graphicsLayer {
                        if (isSelected) alpha = 0f
                    }
                    .onGloballyPositioned { coords ->
                        onBubblePositioned?.invoke(coords.boundsInRoot())
                    }
                    .pointerInput(message.id) {
                        detectTapGestures(
                            onTap = {
                                onMessageClick?.invoke()
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
                    // Quoted replied-to message preview inside bubble
                    if (!message.replyToContent.isNullOrBlank()) {
                        val accentColor = MaterialTheme.colorScheme.primary
                        val quoteBg = if (isLight) Color(0x0F0F172A) else Color(0x2E000000)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(quoteBg)
                                .clickable(enabled = !message.replyToId.isNullOrBlank()) {
                                    message.replyToId?.let { targetId ->
                                        onQuotedMessageClick?.invoke(targetId)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val authorName = message.replyToAuthor?.ifBlank { "Reply" } ?: "Reply"
                                Text(
                                    text = authorName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val isRepliedMsgDeletedForMe = remember(message.replyToId) {
                                    if (message.replyToId.isNullOrBlank()) false
                                    else {
                                        val sp = context.getSharedPreferences("ConnectoPrefs", android.content.Context.MODE_PRIVATE)
                                        val uName = (ConnectoApiClient.currentUsername ?: "").trim().lowercase().removePrefix("@")
                                        val deletedSet = sp.getStringSet("deleted_for_me_$uName", emptySet<String>()) ?: emptySet<String>()
                                        deletedSet.contains(message.replyToId)
                                    }
                                }
                                Text(
                                    text = if (isRepliedMsgDeletedForMe) "Deleted message" else cleanMessageContentForDisplay(message.replyToContent, ""),
                                    fontSize = 12.sp,
                                    fontStyle = if (isRepliedMsgDeletedForMe) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                    color = if (isRepliedMsgDeletedForMe) (if (message.isMe) effectiveOwnText.copy(alpha = 0.5f) else friendText.copy(alpha = 0.5f)) else (if (message.isMe) effectiveOwnText.copy(alpha = 0.8f) else friendText.copy(alpha = 0.75f)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    val isWhisper = message.isWhisper
                    var isWhisperRevealed by remember(message.id) { mutableStateOf(false) }
                    val whisperBlurRadius by animateDpAsState(
                        targetValue = if (!isWhisper || isWhisperRevealed) 0.dp else 12.dp,
                        animationSpec = tween(180, easing = EaseOutCubic),
                        label = "whisperBlur"
                    )

                    Box(
                        modifier = Modifier.then(
                            if (isWhisper) {
                                Modifier.pointerInput(message.id) {
                                    detectTapGestures(
                                        onPress = {
                                            try {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            } catch (_: Exception) {}
                                            isWhisperRevealed = true
                                            try {
                                                tryAwaitRelease()
                                            } finally {
                                                isWhisperRevealed = false
                                            }
                                        }
                                    )
                                }
                            } else Modifier
                        )
                    ) {
                        Box(
                            modifier = if (isWhisper && whisperBlurRadius > 0.dp) Modifier.blur(whisperBlurRadius) else Modifier
                        ) {
                            val trimmedContent = message.content.trim()
                            val isDirectImageUrl = trimmedContent.startsWith("http", ignoreCase = true) &&
                                (trimmedContent.endsWith(".jpg", true) || trimmedContent.endsWith(".png", true) || trimmedContent.endsWith(".jpeg", true) || trimmedContent.endsWith(".webp", true))

                            if (isDirectImageUrl) {
                                AsyncImage(
                                    model = trimmedContent,
                                    contentDescription = "Photo",
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            } else {
                                val safeBubbleContent = cleanMessageContentForDisplay(message.content, "")
                                if (searchHighlightQuery.isNotBlank() && safeBubbleContent.contains(searchHighlightQuery, ignoreCase = true)) {
                                    SubstringHighlightedText(
                                        text = safeBubbleContent,
                                        query = searchHighlightQuery,
                                        style = TextStyle(
                                            color = if (message.isMe) effectiveOwnText else friendText,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp
                                        )
                                    )
                                } else {
                                    Text(
                                        text = safeBubbleContent,
                                        color = if (message.isMe) effectiveOwnText else friendText,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        if (isWhisper && !isWhisperRevealed) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xB3181C28))
                                        .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "🫧 Hold to reveal",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatIsoTime(message.time),
                            fontSize = 10.sp,
                            color = if (message.isMe) {
                                if (isWhiteBubble) Color(0x99000000) else effectiveOwnText.copy(alpha = 0.70f)
                            } else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (message.isSilent) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "🔕",
                                fontSize = 10.sp
                            )
                        }
                        if (message.isMe) {
                            Spacer(modifier = Modifier.width(5.dp))
                            val isPending = message.id.startsWith("opt-") || message.id.startsWith("temp_")
                            if (isPending) {
                                // SENDING: subtle pulsing hollow ring (8px diameter, #64748B)
                                ConnectoDeliverySendingRing()
                            } else if (message.isRead) {
                                // READ / SEEN: hide delivery dots on read messages (recipient mini avatar shown below)
                            } else if (message.isDelivered) {
                                // DELIVERED: glowing double-dot indicator (#38BDF8)
                                ConnectoDeliveryDeliveredDots()
                            } else {
                                // SENT: single crisp solid dot (6px diameter, #94A3B8)
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF94A3B8))
                                )
                            }
                        }
                    }
                }
            }

            // ========== REACTION BADGES ON BUBBLE (COMPACT PILLS) ==========
            val activeReactionsMap = remember(message.reactions, message.reaction) {
                if (message.reactions.isNotEmpty()) {
                    message.reactions
                } else if (!message.reaction.isNullOrBlank()) {
                    val r = message.reaction ?: ""
                    if (r.isNotBlank()) mapOf(r to listOf("user")) else emptyMap()
                } else {
                    emptyMap()
                }
            }

            if (activeReactionsMap.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .offset(y = (-4).dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentUid = (ConnectoApiClient.currentUserId ?: "").trim().lowercase()
                    val currentUname = (ConnectoApiClient.currentUsername ?: "").trim().removePrefix("@").lowercase()

                    activeReactionsMap.forEach { (emoji, userList) ->
                        val hasUserReacted = userList.any { u ->
                            val cu = u.trim().removePrefix("@").lowercase()
                            (currentUid.isNotBlank() && cu == currentUid) || (currentUname.isNotBlank() && cu == currentUname)
                        }
                        val count = userList.size

                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (hasUserReacted) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                                    else if (isLight) Color(0xFFF1F5F9)
                                    else Color(0xFF161922)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (hasUserReacted) MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                                            else if (isLight) Color(0x14000000)
                                            else Color(0x1FFFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onReact?.invoke(message, emoji)
                                }
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emoji, fontSize = 12.sp)
                                if (count > 1) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = count.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasUserReacted) MaterialTheme.colorScheme.primary
                                                else if (isLight) Color(0xFF64748B)
                                                else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Signature Seen Mini-Avatar (placed under the most recent read message)
            if (message.isMe && isLatestRead) {
                AnimatedVisibility(
                    visible = isLatestRead,
                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp, end = 2.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!recipientAvatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = recipientAvatarUrl,
                                contentDescription = "Seen by $recipientName",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = recipientName.take(1).uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
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
