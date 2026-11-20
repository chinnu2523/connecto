package com.example.connecto.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.ui.designsystem.LocalConnectoColors
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Individual particle definition for the Tab Bar Particle Gather Animation.
 * Represents an energetic light mote that accelerates and gathers inward into the active tab capsule.
 */
private data class TabGatherParticle(
    val initialAngleRad: Float,
    val initialDistanceDp: Float,
    val sizeDp: Float,
    val delay: Float,
    val curvature: Float,
    val colorTone: Int, // 0 = primary, 1 = cyan/accent, 2 = white/highlight
    val streakFactor: Float
)

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
 * Connecto Professional & Aesthetic Navigation Bottom Bar.
 * Built for clarity, ergonomics, and understated luxury:
 * - Fluid Liquid Pill / Bubble Indicator with buttery spring physics.
 * - Multi-tier diffuse ambient shadow & soft specular top hairline.
 * - Custom tactile buttons with generous 48dp+ touch targets and spring micro-press.
 * - Bespoke, classic vector icons for Home, Messages, Channels, Calls, Profile.
 * - High-contrast, clean typography with zero clutter.
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

    val isDark = colors.isDark
    val barBg = if (isDark) Color(0xF20A0B10) else Color(0xE6FFFFFF)
    val topBorderColor = if (isDark) Color(0x1AFFFFFF) else Color(0x14000000)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(barBg)
            .drawBehind {
                drawLine(
                    color = topBorderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        val totalWidth = maxWidth
        val tabCount = ConnectoTab.entries.size
        val tabWidth = totalWidth / tabCount
        val selectedIndex = ConnectoTab.entries.indexOf(currentTab).coerceAtLeast(0)

        val targetCenterX = tabWidth * selectedIndex + tabWidth / 2
        val animatedCenterX by animateDpAsState(
            targetValue = targetCenterX,
            animationSpec = spring(
                dampingRatio = 0.85f,
                stiffness = 900f
            ),
            label = "navPillCenterX"
        )

        val pillWidth = 20.dp
        val pillHeight = 3.dp

        // Soft radial ambient glow directly under the top indicator pill with subtle Electric Indigo tint
        Box(
            modifier = Modifier
                .offset(x = animatedCenterX - 24.dp, y = 0.dp)
                .size(width = 48.dp, height = 20.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            (if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5)).copy(alpha = if (isDark) 0.28f else 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(48f, 0f),
                        radius = 48f
                    )
                )
        )

        // Sliding Top Pill Indicator (22px wide, 3px tall, rounded) with spring physics and electric indigo sheen
        Box(
            modifier = Modifier
                .offset(x = animatedCenterX - (pillWidth / 2), y = 0.dp)
                .size(width = pillWidth, height = pillHeight)
                .clip(RoundedCornerShape(bottomStart = 1.5.dp, bottomEnd = 1.5.dp))
                .background(
                    if (isDark) Brush.horizontalGradient(listOf(Color(0xFF818CF8), Color(0xFF6366F1)))
                    else Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConnectoTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                // Tap feedback: scale(0.92) for 100ms
                val buttonPressScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1.0f,
                    animationSpec = tween(100, easing = EaseOutCubic),
                    label = "tabPressScale"
                )

                // Active icon bounce & spring scale (1.0 -> 1.18 -> 1.08) on selection
                val iconBounce = remember { Animatable(1f) }
                LaunchedEffect(isSelected) {
                    if (isSelected) {
                        iconBounce.snapTo(1.0f)
                        iconBounce.animateTo(1.18f, tween(80, easing = EaseOutCubic))
                        iconBounce.animateTo(1.08f, spring(dampingRatio = 0.7f, stiffness = 600f))
                    } else {
                        iconBounce.snapTo(1.0f)
                    }
                }

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (isDark) Color.White else Color(0xFF0F172A)
                    } else {
                        if (isDark) Color(0xFF7A7F8F) else Color(0xFF64748B)
                    },
                    animationSpec = tween(80, easing = EaseOutCubic),
                    label = "tabIconColor"
                )

                val labelColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (isDark) Color.White else Color(0xFF0F172A)
                    } else {
                        if (isDark) Color(0xFF7A7F8F) else Color(0xFF64748B)
                    },
                    animationSpec = tween(80, easing = EaseOutCubic),
                    label = "tabTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = buttonPressScale
                            scaleY = buttonPressScale
                        }
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
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .graphicsLayer {
                                    scaleX = iconBounce.value
                                    scaleY = iconBounce.value
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            ClassicAestheticTabIcon(
                                tab = tab,
                                isSelected = isSelected,
                                primaryColor = iconColor,
                                inactiveColor = iconColor,
                                avatarUrl = userAvatar,
                                userInitial = userInitial,
                                modifier = Modifier.size(22.dp)
                            )

                            if (tab == ConnectoTab.CALLS && isCallActive) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(colors.success)
                                        .border(1.dp, barBg, CircleShape)
                                )
                            } else if (tab == ConnectoTab.MESSAGES && unreadMessagesCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-2).dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color.White else Color(0xFF0F172A))
                                        .padding(horizontal = 4.dp, vertical = 0.5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadMessagesCount > 99) "99+" else unreadMessagesCount.toString(),
                                        color = if (isDark) Color.Black else Color.White,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 10.sp
                                    )
                                }
                            } else if (tab == ConnectoTab.CALLS && hasUnreadCalls) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.warning)
                                        .border(1.dp, barBg, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = tab.title,
                            color = labelColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.2.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
