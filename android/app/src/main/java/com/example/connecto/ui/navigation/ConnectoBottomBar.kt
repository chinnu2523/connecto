package com.example.connecto.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.ui.designsystem.LocalConnectoColors
import kotlin.math.absoluteValue

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

    val isPureBlack = colors.background == Color(0xFF000000)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        if (!colors.isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF11141C).copy(alpha = 0.88f),
                        if (!colors.isDark) Color.White.copy(alpha = 0.92f) else Color(0xFF0B0D13).copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        // Specular 1px Hairline Top Border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            (if (isPureBlack) Color.White else colors.primary).copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.18f),
                            (if (isPureBlack) Color.White else colors.info).copy(alpha = 0.30f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                )
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            val totalWidth = maxWidth
            val tabCount = ConnectoTab.entries.size
            val tabWidth = totalWidth / tabCount
            val selectedIndex = ConnectoTab.entries.indexOf(currentTab).coerceAtLeast(0)

            // Dynamic tracking center of the active tab with fluid spring physics
            val targetCenterX = tabWidth * selectedIndex + tabWidth / 2
            val animatedCenterX by animateDpAsState(
                targetValue = targetCenterX,
                animationSpec = spring(
                    stiffness = 380f,
                    dampingRatio = 0.78f // Clean, professional settle with subtle fluidity
                ),
                label = "navPillCenterX"
            )

            // Fluid squish & stretch during movement
            val distance = (targetCenterX - animatedCenterX).value.dp
            val stretchFactor = 1f + (distance.value.absoluteValue / 75f).coerceAtMost(0.32f)
            val squishY = 1f - (distance.value.absoluteValue / 160f).coerceAtMost(0.15f)

            // Ripple glow burst on tab selection
            val rippleProgress = remember { Animatable(0f) }
            LaunchedEffect(currentTab) {
                rippleProgress.snapTo(0f)
                rippleProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
                )
            }

            // LAYER 1: Multi-Tier Diffuse Ambient Shadow & Ripple
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pillCenter = Offset(x = animatedCenterX.toPx(), y = 20.dp.toPx())
                val primaryColor = if (isPureBlack) Color.White else colors.primary

                // Soft ambient colored glow under active pill
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.24f),
                            primaryColor.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        radius = 28.dp.toPx(),
                        center = pillCenter
                    ),
                    radius = 28.dp.toPx(),
                    center = pillCenter
                )

                // Soft subtle ripple
                val progress = rippleProgress.value
                if (progress > 0f && progress < 1f) {
                    val rippleRadius = (8.dp.toPx() + 24.dp.toPx() * progress)
                    val rippleAlpha = ((1f - progress) * 0.35f).coerceIn(0f, 1f)
                    drawCircle(
                        color = primaryColor.copy(alpha = rippleAlpha),
                        radius = rippleRadius,
                        center = pillCenter
                    )
                }
            }

            // LAYER 2: Professional Liquid Glass Pill (Bubble) Capsule
            val pillWidth = 54.dp * stretchFactor
            val pillHeight = 34.dp * squishY

            Box(
                modifier = Modifier
                    .offset(x = animatedCenterX - pillWidth / 2, y = 3.dp)
                    .width(pillWidth)
                    .height(pillHeight)
                    .clip(RoundedCornerShape(17.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                (if (isPureBlack) Color.White else colors.primary).copy(alpha = 0.18f),
                                (if (isPureBlack) Color.White else colors.primary).copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.45f),
                                (if (isPureBlack) Color.White else colors.primary).copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(17.dp)
                    )
            ) {
                // Subtle top specular highlight glint
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 1.5.dp)
                        .width((pillWidth * 0.55f).coerceAtMost(32.dp))
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // LAYER 3: Ergonomic Interactive Custom Nav Buttons Row
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectoTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    // Tactile micro-compression on tap (0.92x spring)
                    val buttonPressScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.92f else 1.0f,
                        animationSpec = spring(stiffness = 550f, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "buttonPressScale"
                    )

                    // Smooth icon elevation & bounce on selection
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1.0f,
                        animationSpec = spring(
                            stiffness = 360f,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        label = "tabIconScale"
                    )

                    val iconOffsetY by animateDpAsState(
                        targetValue = if (isSelected) (-2.dp) else 0.dp,
                        animationSpec = spring(
                            stiffness = 380f,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        label = "tabIconOffsetY"
                    )

                    val iconPrimary = if (isPureBlack) Color.White else colors.primary
                    val iconInactive = colors.textSecondary.copy(alpha = 0.70f)

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) iconPrimary else colors.textSecondary,
                        animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
                        label = "tabTextColor"
                    )

                    // Custom Ergonomic Button Container
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .graphicsLayer {
                                scaleX = buttonPressScale
                                scaleY = buttonPressScale
                            }
                            .clip(RoundedCornerShape(16.dp))
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
                                    .width(52.dp)
                                    .height(34.dp),
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
                                    // Bespoke Classic & Aesthetic Vector Icon
                                    ClassicAestheticTabIcon(
                                        tab = tab,
                                        isSelected = isSelected,
                                        primaryColor = iconPrimary,
                                        inactiveColor = iconInactive,
                                        avatarUrl = userAvatar,
                                        userInitial = userInitial,
                                        modifier = Modifier.size(23.dp)
                                    )

                                    // Subtle Unread / Active Badges
                                    if (tab == ConnectoTab.CALLS && isCallActive) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(colors.success)
                                                .border(1.5.dp, colors.surface, CircleShape)
                                        )
                                    } else if (tab == ConnectoTab.MESSAGES && hasUnreadChats) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(colors.primary)
                                                .border(1.5.dp, colors.surface, CircleShape)
                                        )
                                    } else if (tab == ConnectoTab.CALLS && hasUnreadCalls) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
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
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                letterSpacing = 0.2.sp,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        }
    }
}
