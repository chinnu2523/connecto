package com.example.connecto.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.R
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.ui.designsystem.LocalConnectoColors
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.pressScaleEffect
import kotlin.math.roundToInt

@Immutable
data class DrawerChannelOption(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val badgeTag: String? = null,
    val category: String = "COMMUNITY"
)

@Composable
fun ChannelNavigationDrawer(
    drawerState: DrawerState? = null,
    isOpen: Boolean = drawerState?.let { it.isOpen || it.targetValue == DrawerValue.Open } ?: false,
    selectedChannelId: String,
    onChannelSelected: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    currentUsername: String = ConnectoApiClient.currentUsername ?: "Member",
    onProfileClick: (() -> Unit)? = null,
    gesturesEnabled: Boolean = false,
    content: @Composable () -> Unit
) {
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    val officialChannels = remember {
        listOf(
            DrawerChannelOption("general", "general", "Global community chat & team discussion", Icons.Rounded.Forum, category = "OFFICIAL"),
            DrawerChannelOption("announcements", "announcements", "Official updates & platform release notes", Icons.Rounded.Campaign, badgeTag = "UPDATES", category = "OFFICIAL"),
            DrawerChannelOption("gaming", "gaming", "Casual discussion, lounge & off-topic", Icons.Rounded.SportsEsports, badgeTag = "LOUNGE", category = "OFFICIAL")
        )
    }

    val esportsChannels = remember {
        listOf(
            DrawerChannelOption("tournaments", "tournaments", "Community events, challenges & activities", Icons.Rounded.EmojiEvents, badgeTag = "EVENTS", category = "COLLABORATION"),
            DrawerChannelOption("clips", "clips", "Shared demos, media & highlights", Icons.Rounded.PlayCircle, badgeTag = "MEDIA", category = "COLLABORATION"),
            DrawerChannelOption("voice-lounge", "voice-lounge", "Voice room & screen sharing lounge", Icons.AutoMirrored.Rounded.VolumeUp, badgeTag = "VOICE", category = "COLLABORATION")
        )
    }

    val colors = LocalConnectoColors.current

    // Fluid 260ms cubic-bezier decelerate transition curve for the navigation drawer
    val drawerProgress by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = tween(
            durationMillis = 260,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "channelDrawerProgress"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Content underneath (smooth full-size chat, no squeezing or text clipping)
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }

        // 2. Full-screen backdrop / Scrim covering the chat behind it (rgba(0, 0, 0, 0.6))
        if (drawerProgress > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.60f * drawerProgress))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCloseDrawer
                    )
            )

            // 3. Navigation Drawer Sheet sliding from the left
            val density = LocalDensity.current
            val sheetWidthDp = 310.dp
            val sheetWidthPx = with(density) { sheetWidthDp.toPx() }
            val offsetX = -sheetWidthPx * (1f - drawerProgress)

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(sheetWidthDp)
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                    .background(
                        if (colors.isLiquidGlass) {
                            if (colors.glassAlpha <= 0.005f) Color(0x0A000000)
                            else Color(0xFF0F1117).copy(alpha = (0.15f + colors.glassAlpha * 0.80f).coerceIn(0.15f, 0.95f))
                        } else if (colors.isGlass) Color(0xF210121A)
                        else if (colors.isDark) Color(0xF513151F)
                        else Color(0xF8FFFFFF)
                    )
                    .border(
                        width = 1.dp,
                        color = if (colors.isLiquidGlass) Color.White.copy(alpha = if (colors.glassAlpha <= 0.005f) 0.45f else 0.25f)
                                else if (colors.isGlass) Color.White.copy(alpha = 0.22f)
                                else colors.borderSubtle,
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Consume clicks inside sheet so they do not dismiss
                    }
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Drawer Header (Workspace Title & Active Members across from ✕ Close Button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Connecto Workspace",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "24 members online",
                                    fontSize = 11.5.sp,
                                    color = colors.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .pressScaleEffect(onClick = onCloseDrawer)
                                .clip(CircleShape)
                                .background(if (colors.isDark) Color(0xFF1E2230) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Menu",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), thickness = 1.dp)

                    // Category 1: Official Communications
                    Text(
                        text = "WORKSPACE CHANNELS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        officialChannels.forEachIndexed { index, channel ->
                            val itemProgress by animateFloatAsState(
                                targetValue = if (drawerProgress > 0.35f) 1f else 0f,
                                animationSpec = tween(
                                    durationMillis = 240,
                                    delayMillis = index * 30,
                                    easing = EaseOutCubic
                                ),
                                label = "chItemAnim_$index"
                            )
                            Box(
                                modifier = Modifier.graphicsLayer {
                                    translationX = -30f * (1f - itemProgress)
                                    alpha = itemProgress
                                }
                            ) {
                                DrawerChannelItem(
                                    channel = channel,
                                    isSelected = channel.id == selectedChannelId,
                                    gradientColors = gradientColors,
                                    onSelect = {
                                        onChannelSelected(channel.id)
                                        onCloseDrawer()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Category 2: Collaboration & Voice
                    Text(
                        text = "COLLABORATION & VOICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        esportsChannels.forEachIndexed { index, channel ->
                            val itemProgress by animateFloatAsState(
                                targetValue = if (drawerProgress > 0.35f) 1f else 0f,
                                animationSpec = tween(
                                    durationMillis = 240,
                                    delayMillis = (index + 3) * 30,
                                    easing = EaseOutCubic
                                ),
                                label = "collabItemAnim_$index"
                            )
                            Box(
                                modifier = Modifier.graphicsLayer {
                                    translationX = -30f * (1f - itemProgress)
                                    alpha = itemProgress
                                }
                            ) {
                                DrawerChannelItem(
                                    channel = channel,
                                    isSelected = channel.id == selectedChannelId,
                                    gradientColors = gradientColors,
                                    onSelect = {
                                        onChannelSelected(channel.id)
                                        onCloseDrawer()
                                    }
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
private fun DrawerChannelItem(
    channel: DrawerChannelOption,
    isSelected: Boolean,
    gradientColors: List<Color>,
    onSelect: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val colors = LocalConnectoColors.current
    val isDark = colors.isDark

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "drawerItemScale"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.10f else 1.0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "iconScale"
    )

    val itemBg by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) Color(0xFF1C202E) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "itemBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) Color.White else MaterialTheme.colorScheme.primary
        } else {
            colors.textPrimary
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "textColor"
    )

    val subtitleColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            colors.textSecondary
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "subColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(itemBg)
            .clickable {
                onSelect()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
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
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .scale(iconScale)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                if (isDark) Color(0xFF2E354B) else MaterialTheme.colorScheme.primary
                            } else {
                                if (isDark) Color(0xFF1E2230) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = channel.icon,
                        contentDescription = null,
                        tint = if (isSelected) {
                            if (isDark) Color.White else MaterialTheme.colorScheme.onPrimary
                        } else {
                            colors.textSecondary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${channel.name}",
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = channel.description,
                        fontSize = 11.sp,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (channel.badgeTag != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) {
                                    if (isDark) Color(0xFF38BDF8).copy(alpha = 0.20f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                } else {
                                    if (isDark) Color(0xFF262B3D)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = channel.badgeTag,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected && isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + expandHorizontally(),
                    exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + shrinkHorizontally()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Selected",
                            tint = if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
