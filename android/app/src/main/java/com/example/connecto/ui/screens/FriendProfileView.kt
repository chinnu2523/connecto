package com.example.connecto.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wallpaper
import com.example.connecto.voice.CallRingtoneManager
import com.example.connecto.notifications.CustomNotificationToneManager
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.material3.LinearProgressIndicator
import com.example.connecto.data.WallpaperTransform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.connecto.network.ProfileDataDto
import com.example.connecto.ui.designsystem.ConnectoAvatar
import com.example.connecto.ui.designsystem.ConnectoPresenceStatus
import com.example.connecto.ui.designsystem.getCoverPreset
import com.example.connecto.ui.designsystem.isCoverPreset
import com.example.connecto.ui.designsystem.resolveImageModel
import com.example.connecto.ui.designsystem.sanitizeAvatarUrl
import com.example.connecto.ui.designsystem.ConnectoTheme
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.liquidGlassCard

@Composable
fun FriendProfileNavButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String? = null,
    badgeColor: Color? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLiquidGlass = ConnectoTheme.isLiquidGlass
    val isDark = ConnectoTheme.colors.isDark
    val cardShape = RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
        label = "friendNavBtnScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(cardShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(if (isDark) Color(0xFF131620) else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (isDark) Color(0x0DFFFFFF) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), cardShape)
            .padding(horizontal = 18.dp, vertical = 16.dp)
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
                        if (isLiquidGlass) Color.White.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    )
                    .border(
                        1.dp,
                        if (isLiquidGlass) Color.White.copy(alpha = 0.28f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (badgeText != null) {
                        val bColor = if (isLiquidGlass) Color.White else (badgeColor ?: MaterialTheme.colorScheme.primary)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(bColor.copy(alpha = 0.16f))
                                .border(0.8.dp, bColor.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = bColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLiquidGlass) Color.White.copy(alpha = 0.10f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
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
fun FriendProfileScreen(
    friend: FriendItem,
    profData: ProfileDataDto?,
    isChatLocked: Boolean,
    isChatMuted: Boolean,
    chatWallpaperId: String,
    customWallpaperUri: String?,
    subPage: String?,
    onNavigateSubPage: (String?) -> Unit,
    onBack: () -> Unit,
    onToggleChatLock: (Boolean) -> Unit,
    onToggleChatMute: (Boolean) -> Unit,
    onSelectWallpaper: (String) -> Unit,
    onPickCustomWallpaper: () -> Unit,
    onRemoveCustomWallpaper: () -> Unit,
    wallpaperTransform: WallpaperTransform = WallpaperTransform(),
    onOpenWallpaperCustomizer: (() -> Unit)? = null,
    onUnfriend: () -> Unit,
    onInitiateVoiceCall: () -> Unit,
    onInitiateVideoCall: (() -> Unit)? = null,
    onOpenDirectChat: () -> Unit,
    onViewPhoto: () -> Unit,
    isAdmin: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apiHideOnline by com.example.connecto.network.ConnectoApiClient.isHideOnlineState.collectAsState()
    val apiHideLastSeen by com.example.connecto.network.ConnectoApiClient.isHideLastSeenState.collectAsState()
    val apiStealth by com.example.connecto.network.ConnectoApiClient.isStealthModeState.collectAsState()
    val profilePrefs = remember { context.getSharedPreferences("connecto_profile", android.content.Context.MODE_PRIVATE) }
    val myCleanUser = (com.example.connecto.network.ConnectoApiClient.currentUsername ?: "").trim().lowercase().removePrefix("@")
    val myHideOnline = apiHideOnline || apiStealth || profilePrefs.getBoolean("hide_online_$myCleanUser", false) || profilePrefs.getBoolean("stealth_mode_$myCleanUser", false)
    val myHideLastSeen = apiHideLastSeen || apiStealth || profilePrefs.getBoolean("hide_last_seen_$myCleanUser", false) || profilePrefs.getBoolean("stealth_mode_$myCleanUser", false)

    var lastSeenTicker by remember { mutableStateOf(0L) }
    LaunchedEffect(friend.id) {
        while (true) {
            kotlinx.coroutines.delay(30_000L)
            lastSeenTicker = System.currentTimeMillis()
        }
    }
    val friendHandleClean = friend.handle.removePrefix("@").trim()
    val isMatchingProfile = profData != null && (
        profData.username.equals(friendHandleClean, ignoreCase = true) ||
        (profData.id.isNotBlank() && profData.id.equals(friend.id, ignoreCase = true))
    )
    val validatedProf = if (isMatchingProfile) profData else null
    val effectiveName = validatedProf?.displayName?.ifBlank { validatedProf.fullName.ifBlank { friend.name } } ?: friend.name
    val effectiveBio = validatedProf?.bio?.ifBlank { friend.bio } ?: friend.bio
    val isLight = !ConnectoTheme.colors.isDark
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val haptic = LocalHapticFeedback.current

    // 0ms Optimistic UI state for Mute toggle (syncs immediately with prop)
    var optimisticMuted by remember(friend.id, isChatMuted) { mutableStateOf(isChatMuted) }
    var handleCopied by remember(friend.id) { mutableStateOf(false) }
    var confirmUnfriend by remember(friend.id) { mutableStateOf(false) }

    LaunchedEffect(handleCopied) {
        if (handleCopied) {
            kotlinx.coroutines.delay(1600L)
            handleCopied = false
        }
    }
    LaunchedEffect(confirmUnfriend) {
        if (confirmUnfriend) {
            kotlinx.coroutines.delay(4000L)
            confirmUnfriend = false
        }
    }

    AnimatedContent(
        targetState = subPage,
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally(tween(260)) { it } + fadeIn(tween(260)))
                    .togetherWith(slideOutHorizontally(tween(260)) { -it } + fadeOut(tween(260)))
            } else {
                (slideInHorizontally(tween(260)) { -it } + fadeIn(tween(260)))
                    .togetherWith(slideOutHorizontally(tween(260)) { it } + fadeOut(tween(260)))
            }
        },
        label = "friendProfileSubPageTransition",
        modifier = modifier.fillMaxSize()
    ) { currentSub ->
        when (currentSub) {
            "chat_lock" -> {
                // ================= SUBPAGE: CHAT & LOCK =================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                ) {
                    // Subpage Top Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { onNavigateSubPage(null) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Friend Profile",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Chat & Lock",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "@$friendHandleClean • Security & Lock",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = navBarBottom + 32.dp)
                    ) {
                        // 1. BIOMETRIC CHAT LOCK
                        item(key = "setting_lock_chat") {
                            val isDark = ConnectoTheme.colors.isDark
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isDark) Color(0xFF131620) else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isDark) Color(0x0DFFFFFF) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 18.dp, vertical = 16.dp)
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
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isChatLocked) Color(0xFFEF4444).copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isChatLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                                contentDescription = null,
                                                tint = if (isChatLocked) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = "Lock Chat Session",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isChatLocked) "Protected by Biometric / PIN" else "Require fingerprint/PIN to open this chat",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isChatLocked,
                                        onCheckedChange = { onToggleChatLock(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        // 2. MUTE NOTIFICATIONS
                        item(key = "setting_mute_notifs") {
                            val isDark = ConnectoTheme.colors.isDark
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isDark) Color(0xFF131620) else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isDark) Color(0x0DFFFFFF) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 18.dp, vertical = 16.dp)
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
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isChatMuted) Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isChatMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                                                contentDescription = null,
                                                tint = if (isChatMuted) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = "Mute Notifications",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isChatMuted) "Notifications silenced for @$friendHandleClean" else "Alert sounds and vibrations active",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isChatMuted,
                                        onCheckedChange = { onToggleChatMute(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        // 3. E2E ENCRYPTION INFO (Soft sunken fill & subtle emerald border)
                        item(key = "setting_encryption_card") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0x0F10B981))
                                    .border(1.dp, Color(0x2E10B981), RoundedCornerShape(20.dp))
                                    .padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(OnlineGreen.copy(alpha = 0.15f))
                                                .border(1.dp, OnlineGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = OnlineGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = "256-bit AES End-to-End Encryption",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Verified cryptographic channel",
                                                fontSize = 11.5.sp,
                                                color = OnlineGreen,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Messages and calls in this conversation are secured end-to-end between your device and @$friendHandleClean's device. No third parties or servers can read or intercept them.",
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                    }
                }
            }

            "theme_wallpaper" -> {
                // ================= SUBPAGE: THEME & WALLPAPER =================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                ) {
                    // Subpage Top Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { onNavigateSubPage(null) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Friend Profile",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Theme & Wallpaper",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "@$friendHandleClean • Background Customization",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = navBarBottom + 32.dp)
                    ) {
                        // 0. ADMIN RESTRICTION NOTICE (if group and non-admin)
                        if (friend.isGroup && !isAdmin) {
                            item(key = "wp_admin_only_banner") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.12f))
                                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Admin Access Required",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF59E0B)
                                            )
                                            Text(
                                                text = "Only group admins can change the group chat wallpaper.",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 1. LIVE WALLPAPER PREVIEW CARD
                        item(key = "wp_preview_card") {
                            val activeWp = AVAILABLE_CHAT_WALLPAPERS.find { it.id == chatWallpaperId }
                                ?: AVAILABLE_CHAT_WALLPAPERS.first()

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "LIVE CHAT PREVIEW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.8.sp
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                                        .then(
                                            if (onOpenWallpaperCustomizer != null) Modifier.clickable {
                                                if (friend.isGroup && !isAdmin) {
                                                    Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    onOpenWallpaperCustomizer()
                                                }
                                            }
                                            else Modifier
                                        )
                                ) {
                                    if (chatWallpaperId == "custom" && !customWallpaperUri.isNullOrBlank()) {
                                        val contentScale = when (wallpaperTransform.contentScaleMode) {
                                            "fit" -> ContentScale.Fit
                                            "fill_bounds" -> ContentScale.FillBounds
                                            "inside" -> ContentScale.Inside
                                            else -> ContentScale.Crop
                                        }
                                        AsyncImage(
                                            model = Uri.parse(customWallpaperUri),
                                            contentDescription = "Custom Wallpaper",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer {
                                                    scaleX = wallpaperTransform.scale
                                                    scaleY = wallpaperTransform.scale
                                                    translationX = wallpaperTransform.offsetX * 0.4f
                                                    translationY = wallpaperTransform.offsetY * 0.4f
                                                }
                                                .then(
                                                    if (wallpaperTransform.blur > 0f) Modifier.blur(wallpaperTransform.blur.dp) else Modifier
                                                ),
                                            contentScale = contentScale
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer {
                                                    scaleX = wallpaperTransform.scale
                                                    scaleY = wallpaperTransform.scale
                                                    translationX = wallpaperTransform.offsetX * 0.4f
                                                    translationY = wallpaperTransform.offsetY * 0.4f
                                                }
                                                .background(if (isLight) activeWp.lightBrush else activeWp.darkBrush)
                                        )
                                    }

                                    // Scrim Overlay
                                    val scrimAlpha = wallpaperTransform.dim
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                if (isLight) Color.White.copy(alpha = scrimAlpha * 0.75f)
                                                else Color.Black.copy(alpha = scrimAlpha)
                                            )
                                    )

                                    // Mock Chat Bubbles
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Friend incoming bubble
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.90f))
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "Hey! How does this wallpaper look? ✨",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Outgoing bubble
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.End)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFF47526B))
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "Looks clean and responsive! 🚀",
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. CUSTOM PHOTO WALLPAPER ACTIONS
                        item(key = "wp_custom_photo_card") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "CUSTOM PHOTO WALLPAPER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.8.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (friend.isGroup && !isAdmin) {
                                                Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                                            } else {
                                                onPickCustomWallpaper()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (customWallpaperUri != null) "Change Photo" else "Pick From Gallery",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }

                                    if (onOpenWallpaperCustomizer != null && (customWallpaperUri != null || chatWallpaperId != "default")) {
                                        Button(
                                            onClick = {
                                                if (friend.isGroup && !isAdmin) {
                                                    Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    onOpenWallpaperCustomizer()
                                                }
                                            },
                                            modifier = Modifier.height(46.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = "Adjust Size & Position",
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Layout",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (customWallpaperUri != null) {
                                        Button(
                                            onClick = {
                                                if (friend.isGroup && !isAdmin) {
                                                    Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    onRemoveCustomWallpaper()
                                                }
                                            },
                                            modifier = Modifier.height(46.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove Custom Photo",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. WALLPAPER PRESET OPTIONS
                        item(key = "wp_presets_header") {
                            Text(
                                text = "GRADIENT & THEME PRESETS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                        }

                        items(AVAILABLE_CHAT_WALLPAPERS, key = { it.id }) { wp ->
                            val isSelected = chatWallpaperId == wp.id && customWallpaperUri == null
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        if (friend.isGroup && !isAdmin) {
                                            Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                                        } else {
                                            onSelectWallpaper(wp.id)
                                        }
                                    }
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
                                        // Wallpaper Thumbnail Swatch
                                        Box(
                                            modifier = Modifier
                                                .size(width = 46.dp, height = 36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isLight) wp.lightBrush else wp.darkBrush)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                        )

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = wp.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = wp.subtitle,
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
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
                                                contentDescription = "Active",
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

            "call_ringtone" -> {
                CallRingtonePickerScreen(
                    targetContactId = friend.id.ifBlank { friend.handle },
                    targetContactName = effectiveName,
                    targetContactAvatar = friend.avatarUrl ?: profData?.avatarUrl,
                    onBack = { onNavigateSubPage(null) }
                )
            }

            "notif_tone_vibration" -> {
                MessageNotificationSettingsScreen(
                    targetContactId = friend.id.ifBlank { friend.handle },
                    targetContactName = effectiveName,
                    targetContactAvatar = friend.avatarUrl ?: profData?.avatarUrl,
                    onBack = { onNavigateSubPage(null) }
                )
            }

            else -> {
                // ================= MAIN FRIEND PROFILE OVERVIEW TAB =================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                ) {
                    // Top App Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    .clickable(onClick = onBack),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Chat",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Friend Profile",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@$friendHandleClean",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Non-blocking 0ms Optimistic UI: render immediately with local data and show subtle sync bar while refreshing
                    if (isLoading && validatedProf == null) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = navBarBottom + 32.dp)
                    ) {
                        // 1. HERO COVER BANNER & AVATAR
                        item(key = "friend_profile_hero") {
                            val bannerUrl = validatedProf?.bannerUrl ?: friend.bannerUrl
                            val isPreset = isCoverPreset(bannerUrl)
                            val presetObj = if (isPreset) getCoverPreset(bannerUrl) else null
                            val avatarRingBg = if (isLight) Color(0xFFFFFFFF) else Color(0xFF0A0B0E)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(172.dp)
                            ) {
                                // Cover Banner
                                val isDark = ConnectoTheme.colors.isDark
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(128.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .border(1.dp, if (isDark) Color(0x14FFFFFF) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
                                ) {
                                    if (isPreset && presetObj != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(presetObj.brush)
                                        )
                                    } else if (!bannerUrl.isNullOrBlank()) {
                                        val resolvedModel = resolveImageModel(bannerUrl) ?: sanitizeAvatarUrl(bannerUrl) ?: bannerUrl
                                        AsyncImage(
                                            model = resolvedModel,
                                            contentDescription = "Cover Banner",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF0F172A))
                                                    )
                                                )
                                        )
                                    }
                                    // Subtle bottom depth scrim for crisp avatar separation
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                                                )
                                            )
                                    )
                                }

                                // Avatar overlapping bottom-left
                                Box(
                                    modifier = Modifier
                                        .offset(x = 16.dp, y = 86.dp)
                                        .clickable(onClick = onViewPhoto)
                                ) {
                                    ConnectoAvatar(
                                        name = effectiveName,
                                        avatarUrl = friend.avatarUrl ?: validatedProf?.avatarUrl,
                                        customSizeDp = 82.dp,
                                        customFontSizeSp = 28,
                                        status = if (friend.isOnline && !myHideOnline) ConnectoPresenceStatus.ONLINE else ConnectoPresenceStatus.OFFLINE,
                                        borderWidth = 4.dp,
                                        borderColor = avatarRingBg,
                                        statusBorderWidth = 3.dp,
                                        statusBorderColor = avatarRingBg
                                    )
                                }
                            }
                        }

                        // 2. FRIEND DETAILS & BIO
                        item(key = "friend_info_bio") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = effectiveName,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // 1-tap Copy @handle pill with optimistic "Copied" feedback
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                            cm?.setPrimaryClip(android.content.ClipData.newPlainText("Connecto Handle", "@$friendHandleClean"))
                                            handleCopied = true
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    AnimatedContent(
                                        targetState = handleCopied,
                                        transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(140)) },
                                        label = "copyHandleBadge"
                                    ) { copied ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (copied) "Copied @$friendHandleClean" else "@$friendHandleClean",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (copied) OnlineGreen else MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Icon(
                                                imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                                contentDescription = "Copy username",
                                                tint = if (copied) OnlineGreen else MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }

                                if (effectiveBio.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = effectiveBio,
                                        fontSize = 13.5.sp,
                                        lineHeight = 19.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Status Badges
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val isFriendOnlineEffective = friend.isOnline && !myHideOnline
                                    val statusColor = if (isFriendOnlineEffective) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                    val statusText = if (isFriendOnlineEffective) {
                                        "Online"
                                    } else if (!myHideLastSeen) {
                                        val lastSeenTs = com.example.connecto.voice.VoiceCallManager.getUserLastSeen(friend.handle)
                                            ?: com.example.connecto.voice.VoiceCallManager.getUserLastSeen(friend.id)
                                        val formatted = formatDynamicLastSeen(lastSeenTs, friend.lastSeen, lastSeenTicker)
                                        formatted ?: "Offline"
                                    } else {
                                        "Offline"
                                    }

                                    val pulseTransition = rememberInfiniteTransition(label = "onlinePulse")
                                    val pulseScale by pulseTransition.animateFloat(
                                        initialValue = 1f,
                                        targetValue = if (isFriendOnlineEffective) 1.35f else 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "pulseScale"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(statusColor.copy(alpha = 0.14f))
                                            .border(1.dp, statusColor.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .graphicsLayer {
                                                        scaleX = pulseScale
                                                        scaleY = pulseScale
                                                    }
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

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
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
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. QUICK ACTION BUTTONS ROW (Chat, Voice Call & Optimistic Mute)
                        item(key = "friend_quick_actions") {
                            val isDark = ConnectoTheme.colors.isDark
                            val chatInteraction = remember { MutableInteractionSource() }
                            val isChatPressed by chatInteraction.collectIsPressedAsState()
                            val chatScale by animateFloatAsState(
                                targetValue = if (isChatPressed) 0.95f else 1.0f,
                                animationSpec = spring(stiffness = 450f, dampingRatio = 0.75f),
                                label = "quickChatScale"
                            )

                            val callInteraction = remember { MutableInteractionSource() }
                            val isCallPressed by callInteraction.collectIsPressedAsState()
                            val callScale by animateFloatAsState(
                                targetValue = if (isCallPressed) 0.95f else 1.0f,
                                animationSpec = spring(stiffness = 450f, dampingRatio = 0.75f),
                                label = "quickCallScale"
                            )

                            val muteInteraction = remember { MutableInteractionSource() }
                            val isMutePressed by muteInteraction.collectIsPressedAsState()
                            val muteScale by animateFloatAsState(
                                targetValue = if (isMutePressed) 0.95f else 1.0f,
                                animationSpec = spring(stiffness = 450f, dampingRatio = 0.75f),
                                label = "quickMuteScale"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Message
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .graphicsLayer {
                                            scaleX = chatScale
                                            scaleY = chatScale
                                        }
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isDark) Color(0xFF131620) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                                        .clickable(
                                            interactionSource = chatInteraction,
                                            indication = null,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onOpenDirectChat()
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Chat",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Voice Call
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .graphicsLayer {
                                            scaleX = callScale
                                            scaleY = callScale
                                        }
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isDark) Color(0xFF131620) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, OnlineGreen.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                        .clickable(
                                            interactionSource = callInteraction,
                                            indication = null,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onInitiateVoiceCall()
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = null,
                                            tint = OnlineGreen,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Call",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Optimistic Mute Toggle
                                val muteColor = if (optimisticMuted) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .graphicsLayer {
                                            scaleX = muteScale
                                            scaleY = muteScale
                                        }
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (optimisticMuted) Color(0xFFF59E0B).copy(alpha = 0.14f)
                                            else if (isDark) Color(0xFF131620)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            1.dp,
                                            if (optimisticMuted) Color(0xFFF59E0B).copy(alpha = 0.45f)
                                            else if (isDark) Color(0x0DFFFFFF)
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable(
                                            interactionSource = muteInteraction,
                                            indication = null,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                val next = !optimisticMuted
                                                optimisticMuted = next
                                                onToggleChatMute(next)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = optimisticMuted,
                                        transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                                        label = "optimisticMuteBtn"
                                    ) { muted ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (muted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                                                contentDescription = null,
                                                tint = if (muted) Color(0xFFF59E0B) else muteColor,
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (muted) "Muted" else "Mute",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (muted) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. SECTION HEADER: CHAT PREFERENCES & SETTINGS
                        item(key = "section_preferences_title") {
                            Text(
                                text = "CHAT PREFERENCES & SETTINGS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                letterSpacing = 1.sp
                            )
                        }

                        // 5. BUTTON 1: CHAT & LOCK
                        item(key = "nav_chat_lock") {
                            FriendProfileNavButton(
                                title = "Chat & Lock",
                                subtitle = "Lock chat with biometrics, mute notifications & security",
                                icon = Icons.Default.Lock,
                                badgeText = if (isChatLocked) "LOCKED" else if (optimisticMuted) "MUTED" else "SECURED",
                                badgeColor = if (isChatLocked) Color(0xFFEF4444) else if (optimisticMuted) Color(0xFFF59E0B) else OnlineGreen,
                                onClick = { onNavigateSubPage("chat_lock") }
                            )
                        }

                        // 6. BUTTON 2: THEME & WALLPAPER
                        item(key = "nav_theme_wallpaper") {
                            val activeWpName = if (chatWallpaperId == "custom" && !customWallpaperUri.isNullOrBlank()) "CUSTOM PHOTO"
                            else AVAILABLE_CHAT_WALLPAPERS.find { it.id == chatWallpaperId }?.name?.uppercase() ?: "DEFAULT"

                            val isWallpaperRestricted = friend.isGroup && !isAdmin

                            FriendProfileNavButton(
                                title = "Theme & Wallpaper",
                                subtitle = if (isWallpaperRestricted) "Only group admins can change wallpaper" else "Custom photo wallpaper, gradient presets & chat theme",
                                icon = Icons.Default.Wallpaper,
                                badgeText = if (isWallpaperRestricted) "ADMIN ONLY" else activeWpName,
                                badgeColor = if (isWallpaperRestricted) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                                onClick = {
                                    if (isWallpaperRestricted) {
                                        Toast.makeText(context, "Only group admins can change the group wallpaper", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onNavigateSubPage("theme_wallpaper")
                                    }
                                }
                            )
                        }

                        // 6b. BUTTON 3: CUSTOM RINGTONE (For direct 1:1 chats)
                        if (!friend.isGroup) {
                            item(key = "nav_call_ringtone") {
                                val contactId = friend.id.ifBlank { friend.handle }
                                val ringtoneSetting = remember(contactId, subPage) {
                                    CallRingtoneManager.getContactRingtoneSetting(context, contactId)
                                }
                                val badgeText = ringtoneSetting?.displayName?.uppercase() ?: "DEFAULT (GLOBAL)"
                                val badgeColor = if (ringtoneSetting != null) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                                FriendProfileNavButton(
                                    title = "Custom Ringtone",
                                    subtitle = if (ringtoneSetting != null) "Assigned: ${ringtoneSetting.displayName}" else "Personalized incoming call audio for this contact",
                                    icon = Icons.Default.MusicNote,
                                    badgeText = badgeText,
                                    badgeColor = badgeColor,
                                    onClick = { onNavigateSubPage("call_ringtone") }
                                )
                            }

                            // 6c. BUTTON 4: NOTIFICATION TONE & VIBRATION (For direct 1:1 chats)
                            item(key = "nav_notif_tone_vibration") {
                                val contactId = friend.id.ifBlank { friend.handle }
                                val notifOverride = remember(contactId, subPage) {
                                    CustomNotificationToneManager.getContactOverride(context, contactId)
                                }
                                val vibTitle = CustomNotificationToneManager.VIBRATION_PRESETS.find { it.id == notifOverride?.vibrationPresetId }?.title ?: "Custom"
                                val badgeText = if (notifOverride != null) {
                                    "${notifOverride.toneName.uppercase()} · ${vibTitle.uppercase()}"
                                } else {
                                    "DEFAULT (GLOBAL)"
                                }
                                val badgeColor = if (notifOverride != null) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                                FriendProfileNavButton(
                                    title = "Notification Tone & Vibration",
                                    subtitle = if (notifOverride != null) {
                                        "Assigned: ${notifOverride.toneName} · $vibTitle"
                                    } else {
                                        "Default (Global) alert sound & haptics"
                                    },
                                    icon = Icons.Default.Vibration,
                                    badgeText = badgeText,
                                    badgeColor = badgeColor,
                                    onClick = { onNavigateSubPage("notif_tone_vibration") }
                                )
                            }
                        }

                        // 7. DANGER ZONE: INLINE 2-STEP OPTIMISTIC UNFRIEND
                        item(key = "danger_unfriend") {
                            AnimatedContent(
                                targetState = confirmUnfriend,
                                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                                label = "unfriendConfirmState"
                            ) { confirming ->
                                if (!confirming) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFDC2626).copy(alpha = 0.10f))
                                            .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                confirmUnfriend = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PersonRemove,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "UNFRIEND @$friendHandleClean",
                                                color = Color(0xFFEF4444),
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { confirmUnfriend = false },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Cancel",
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1.4f)
                                                .height(48.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFFDC2626))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    confirmUnfriend = false
                                                    onUnfriend()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Confirm Unfriend",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
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
