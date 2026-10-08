package com.example.connecto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.ui.designsystem.ConnectoAvatar
import com.example.connecto.ui.designsystem.ConnectoAvatarSize
import com.example.connecto.ui.designsystem.ConnectoBadge
import com.example.connecto.ui.designsystem.ConnectoBadgeVariant
import com.example.connecto.ui.designsystem.ConnectoPresenceStatus
import com.example.connecto.ui.designsystem.ConnectoSpacing
import com.example.connecto.ui.designsystem.LocalConnectoColors
import com.example.connecto.ui.theme.PulsingOnlineDot

/**
 * Connecto Refined Top Header.
 * Classic, clean, professional header with channel drawer trigger, status, notifications, and profile.
 */
@Composable
fun ConnectoTopHeader(
    title: String = "Connecto",
    subtitle: String = "Live Community",
    userInitial: String = "C",
    avatarUrl: String? = null,
    isStealthModeOn: Boolean = false,
    isOnline: Boolean = true,
    onMenuClick: (() -> Unit)? = null,
    onAddFriendClick: (() -> Unit)? = null,
    pendingFriendRequestsCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    showProfileButton: Boolean = true,
    onAvatarClick: () -> Unit = {},
    customAction: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalConnectoColors.current
    val globalAvatar by ConnectoApiClient.currentUserAvatarState.collectAsState()
    val resolvedAvatarUrl = if (!avatarUrl.isNullOrBlank()) avatarUrl else globalAvatar
    val isWsConnected by com.example.connecto.voice.VoiceCallManager.isWsConnectedFlow.collectAsState()
    val globalStealth by ConnectoApiClient.isStealthModeState.collectAsState()
    val effectiveStealth = isStealthModeOn || globalStealth

    val isActuallyOnline = !effectiveStealth && (isWsConnected && isOnline)

    // Bug #2 fix: timeout after 12s so "Connecting..." becomes "Server Offline"
    var connectTimedOut by remember { mutableStateOf(false) }
    LaunchedEffect(isWsConnected) {
        if (!isWsConnected) {
            kotlinx.coroutines.delay(12_000L)
            if (!isWsConnected) connectTimedOut = true
        } else {
            connectTimedOut = false
        }
    }

    val statusText = when {
        effectiveStealth -> "Stealth Mode"
        isActuallyOnline -> "Online"
        connectTimedOut -> "Server in Maintenance"
        else -> "Connecting..."
    }

    val presence = when {
        effectiveStealth -> ConnectoPresenceStatus.IDLE
        isActuallyOnline -> ConnectoPresenceStatus.ONLINE
        else -> ConnectoPresenceStatus.OFFLINE
    }

    // On first load, animate header sliding down + fading in (≈0.4–0.6s)
    var isHeaderLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isHeaderLoaded = true
    }
    val headerAlpha by animateFloatAsState(
        targetValue = if (isHeaderLoaded) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )
    val headerOffsetY by animateDpAsState(
        targetValue = if (isHeaderLoaded) 0.dp else (-26).dp,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "headerOffsetY"
    )

    // Very subtle moving shimmer/gradient line across the header as a continuous loop
    val infiniteTransition = rememberInfiniteTransition(label = "headerShimmerLoop")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerPhase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .statusBarsPadding()
            .offset(y = headerOffsetY)
            .graphicsLayer { alpha = headerAlpha }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = ConnectoSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Menu / Navigation button and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (onMenuClick != null) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Channels",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (title == "CONNECTO GAMERS HUB") "Connecto" else title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = connectTimedOut || !isActuallyOnline) {
                                com.example.connecto.MainActivity.openMaintenanceScreen()
                            }
                            .padding(vertical = 1.dp, horizontal = 2.dp)
                    ) {
                        if (presence == ConnectoPresenceStatus.ONLINE) {
                            PulsingOnlineDot(
                                color = colors.success,
                                size = 6.dp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (presence) {
                                            ConnectoPresenceStatus.ONLINE -> colors.success
                                            ConnectoPresenceStatus.IDLE -> colors.info
                                            ConnectoPresenceStatus.DND -> colors.error
                                            ConnectoPresenceStatus.OFFLINE -> colors.textDisabled
                                        }
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(ConnectoSpacing.xxs))
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = if (connectTimedOut) colors.warning else colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Right: Actions (Add Friend, Notification Bell, Avatar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (onAddFriendClick != null) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = onAddFriendClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (pendingFriendRequestsCount > 0) Icons.Filled.PersonAdd else Icons.Outlined.PersonAdd,
                                contentDescription = "Add Friend / Requests",
                                tint = if (pendingFriendRequestsCount > 0) colors.primary else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (pendingFriendRequestsCount > 0) {
                            ConnectoBadge(
                                count = pendingFriendRequestsCount,
                                variant = ConnectoBadgeVariant.PRIMARY,
                                modifier = Modifier.padding(top = 4.dp, end = 4.dp)
                            )
                        }
                    }
                }



                if (customAction != null) {
                    customAction()
                    Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                }

                if (showProfileButton) {
                    Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                    ConnectoAvatar(
                        name = userInitial,
                        avatarUrl = resolvedAvatarUrl,
                        size = ConnectoAvatarSize.SM,
                        status = presence,
                        onClick = onAvatarClick
                    )
                }
            }
        }

        // Clean 1dp Bottom Border with continuous moving subtle shimmer/gradient line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.borderSubtle)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val w = size.width
                        val startX = (shimmerPhase - 0.20f) * w
                        val endX = (shimmerPhase + 0.20f) * w
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colors.primary.copy(alpha = 0.15f),
                                    colors.primary.copy(alpha = 0.85f),
                                    colors.info.copy(alpha = 0.95f),
                                    colors.primary.copy(alpha = 0.85f),
                                    colors.primary.copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                startX = startX,
                                endX = endX
                            )
                        )
                    }
            )
        }
    }
}
