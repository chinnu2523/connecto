package com.example.connecto.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.ui.designsystem.ConnectoRadius
import com.example.connecto.ui.designsystem.ConnectoSpacing
import com.example.connecto.ui.designsystem.LocalConnectoColors

enum class ConnectoTab(
    val title: String,
    val outlinedIcon: ImageVector,
    val activeIcon: ImageVector
) {
    HOME("Home", Icons.Outlined.Home, Icons.Filled.Home),
    MESSAGES("Messages", Icons.AutoMirrored.Outlined.Chat, Icons.AutoMirrored.Filled.Chat),
    CHANNELS("Channels", Icons.Outlined.Tag, Icons.Filled.Tag),
    CALLS("Calls", Icons.Outlined.Call, Icons.Filled.Call),
    PROFILE("Profile", Icons.Outlined.Person, Icons.Filled.Person)
}

/**
 * Connecto Premium Navigation Bottom Bar.
 * Enhanced with playful spring physics, dynamic icon bounce micro-interactions,
 * specular capsule lighting, tactile haptic feedback, and fluid indicator transitions.
 */
@Composable
fun ConnectoBottomBar(
    currentTab: ConnectoTab,
    onTabSelected: (ConnectoTab) -> Unit,
    modifier: Modifier = Modifier,
    hasUnreadChats: Boolean = false,
    hasUnreadCalls: Boolean = false,
    isCallActive: Boolean = false,
    unreadMessagesCount: Int = 0,
    userInitial: String = "U"
) {
    val colors = LocalConnectoColors.current
    val haptic = LocalHapticFeedback.current
    val rawUserAvatar by ConnectoApiClient.currentUserAvatarState.collectAsState()
    val userAvatar = remember(rawUserAvatar) {
        if (rawUserAvatar == "null" || rawUserAvatar.isNullOrBlank()) null else rawUserAvatar
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
    ) {
        // High-end glowing gradient top border line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            colors.primary.copy(alpha = 0.20f),
                            colors.primary.copy(alpha = 0.75f),
                            colors.primary.copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = ConnectoSpacing.xs, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConnectoTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                // Tactile button compression on touch
                val buttonPressScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.88f else 1.0f,
                    animationSpec = spring(stiffness = 650f, dampingRatio = 0.70f),
                    label = "buttonPressScale"
                )

                // Playful icon bounce when selected
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.20f else 1.0f,
                    animationSpec = spring(
                        stiffness = 380f,
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    ),
                    label = "tabIconBounceScale"
                )

                // Smooth vertical floating translation
                val iconOffsetY by animateDpAsState(
                    targetValue = if (isSelected) (-2.5).dp else 0.dp,
                    animationSpec = spring(
                        stiffness = 380f,
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    ),
                    label = "tabIconOffsetY"
                )

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) colors.primary else colors.textSecondary,
                    animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
                    label = "tabIconColor"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) colors.primary else colors.textSecondary,
                    animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
                    label = "tabTextColor"
                )

                val pillWidth by animateDpAsState(
                    targetValue = if (isSelected) 54.dp else 36.dp,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    ),
                    label = "tabPillWidth"
                )

                val indicatorWidth by animateDpAsState(
                    targetValue = if (isSelected) 18.dp else 0.dp,
                    animationSpec = spring(
                        stiffness = 420f,
                        dampingRatio = 0.70f
                    ),
                    label = "indicatorWidth"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .graphicsLayer {
                            scaleX = buttonPressScale
                            scaleY = buttonPressScale
                        }
                        .clip(RoundedCornerShape(ConnectoRadius.md))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            } catch (_: Exception) {}
                            onTabSelected(tab)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Capsule Pill Backdrop with Ambient Specular Lighting
                        Box(
                            modifier = Modifier
                                .width(pillWidth)
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.linearGradient(
                                            listOf(
                                                colors.primary.copy(alpha = 0.24f),
                                                colors.primary.copy(alpha = 0.08f)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(Color.Transparent, Color.Transparent)
                                        )
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) colors.primary.copy(alpha = 0.50f) else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(y = iconOffsetY)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    },
                                contentAlignment = Alignment.TopEnd
                            ) {
                                var avatarLoadFailed by remember(userAvatar) { mutableStateOf(false) }

                                if (tab == ConnectoTab.PROFILE) {
                                    // Profile Button: Avatar or Initial Circle
                                    if (!userAvatar.isNullOrBlank() && !avatarLoadFailed) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .border(
                                                width = if (isSelected) 1.8.dp else 1.dp,
                                                color = if (isSelected) colors.primary else colors.borderSubtle,
                                                shape = CircleShape
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = userAvatar,
                                                contentDescription = tab.title,
                                                contentScale = ContentScale.Crop,
                                                onError = { avatarLoadFailed = true },
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        }
                                    } else {
                                        // Stylized letter initial avatar fallback
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(
                                                            colors.primary,
                                                            Color(0xFF8B5CF6)
                                                        )
                                                    )
                                                )
                                                .border(
                                                    width = if (isSelected) 1.8.dp else 1.dp,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color.Transparent,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (userInitial.isNotBlank()) userInitial.take(1).uppercase() else "U",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    // Standard Navigation Icons (Home, Messages, Channels, Calls)
                                    Icon(
                                        imageVector = if (isSelected) tab.activeIcon else tab.outlinedIcon,
                                        contentDescription = tab.title,
                                        tint = iconColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Badges with subtle bounce & Active Call Pulse
                                if (tab == ConnectoTab.CALLS && isCallActive) {
                                    val pulseTransition = rememberInfiniteTransition(label = "bottomBarCallPulse")
                                    val pulseAlpha by pulseTransition.animateFloat(
                                        initialValue = 0.35f,
                                        targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
                                        label = "callDotAlpha"
                                    )
                                    val pulseScale by pulseTransition.animateFloat(
                                        initialValue = 0.9f,
                                        targetValue = 1.25f,
                                        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
                                        label = "callDotScale"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .graphicsLayer {
                                                alpha = pulseAlpha
                                                scaleX = pulseScale
                                                scaleY = pulseScale
                                            }
                                            .clip(CircleShape)
                                            .background(colors.success)
                                            .border(1.5.dp, colors.surface, CircleShape)
                                    )
                                } else if (tab == ConnectoTab.MESSAGES && hasUnreadChats) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary)
                                            .border(1.5.dp, colors.surface, CircleShape)
                                    )
                                } else if (tab == ConnectoTab.CALLS && hasUnreadCalls) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(colors.warning)
                                            .border(1.5.dp, colors.surface, CircleShape)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tab.title,
                            color = textColor,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Modern Accent Dash Indicator with Soft Radiant Glow
                        Box(
                            modifier = Modifier
                                .width(indicatorWidth)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            colors.primary,
                                            Color(0xFF818CF8)
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
