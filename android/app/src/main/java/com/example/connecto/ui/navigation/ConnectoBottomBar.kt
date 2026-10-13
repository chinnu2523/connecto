package com.example.connecto.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
 * Connecto Premium Navigation Bottom Bar.
 * Enhanced with fluid water drop animation, liquid elongation physics across tab changes,
 * concentric aquatic ripple splash, specular refraction glint, and bounce transitions.
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

    val gleamBrush = if (colors.isGlass) {
        val infiniteTransition = rememberInfiniteTransition(label = "glassNavGleam")
        val gleamOffset by infiniteTransition.animateFloat(
            initialValue = -400f,
            targetValue = 1400f,
            animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
            label = "navGleam"
        )
        Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                (if (colors.isLiquidGlass) Color(0xFF00E5FF) else colors.primary).copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.85f),
                (if (colors.isLiquidGlass) Color(0xFF00E5FF) else colors.primary).copy(alpha = 0.35f),
                Color.Transparent
            ),
            start = Offset(gleamOffset, 0f),
            end = Offset(gleamOffset + 350f, 0f)
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                colors.primary.copy(alpha = 0.20f),
                colors.primary.copy(alpha = 0.75f),
                colors.primary.copy(alpha = 0.20f),
                Color.Transparent
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (colors.isLiquidGlass) Color(0x66061120)
                else if (colors.isGlass) Color(0xF2090D16)
                else colors.surface
            )
    ) {
        // Glowing gradient top border line with animated specular gleam in glass / liquid theme
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(gleamBrush)
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = ConnectoSpacing.xs, vertical = 2.dp)
        ) {
            val totalWidth = maxWidth
            val tabCount = ConnectoTab.entries.size
            val tabWidth = totalWidth / tabCount
            val selectedIndex = ConnectoTab.entries.indexOf(currentTab).coerceAtLeast(0)

            // Droplet center tracking across tabs with fluid spring physics
            val targetCenterX = tabWidth * selectedIndex + tabWidth / 2
            val animatedCenterX by animateDpAsState(
                targetValue = targetCenterX,
                animationSpec = spring(
                    stiffness = 340f,
                    dampingRatio = 0.72f // Fluid liquid rebound with subtle overshoot
                ),
                label = "waterDropCenterX"
            )

            // Dynamic fluid elongation & squish based on movement speed/distance
            val distance = (targetCenterX - animatedCenterX).value.dp
            val stretchFactor = 1f + (distance.value.absoluteValue / 65f).coerceAtMost(0.48f)
            val squishY = 1f - (distance.value.absoluteValue / 130f).coerceAtMost(0.24f)

            // Water droplet splash / ripple animation triggered on tab change
            val rippleProgress = remember { Animatable(0f) }
            LaunchedEffect(currentTab) {
                rippleProgress.snapTo(0f)
                rippleProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
                )
            }

            // Layer 1: Water drop expanding aquatic ripples behind the tabs
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val rippleCenter = Offset(x = animatedCenterX.toPx(), y = 20.dp.toPx())
                val progress = rippleProgress.value
                if (progress > 0f && progress < 1f) {
                    val outerRadius = (5.dp.toPx() + 34.dp.toPx() * progress)
                    val outerAlpha = ((1f - progress) * 0.55f).coerceIn(0f, 1f)
                    drawCircle(
                        color = (if (colors.isLiquidGlass) Color(0xFF00E5FF) else colors.primary).copy(alpha = outerAlpha),
                        radius = outerRadius,
                        center = rippleCenter,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    val innerRadius = (2.dp.toPx() + 20.dp.toPx() * progress)
                    val innerAlpha = ((1f - progress) * 0.40f).coerceIn(0f, 1f)
                    drawCircle(
                        color = Color.White.copy(alpha = innerAlpha),
                        radius = innerRadius,
                        center = rippleCenter,
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                }
            }

            // Layer 2: Moving Liquid Water Droplet Capsule
            val dropletWidth = 52.dp * stretchFactor
            val dropletHeight = 33.dp * squishY

            Box(
                modifier = Modifier
                    .offset(x = animatedCenterX - dropletWidth / 2, y = 3.dp)
                    .width(dropletWidth)
                    .height(dropletHeight)
                    .clip(RoundedCornerShape(17.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (colors.isLiquidGlass) Color(0x6600E5FF) else colors.primary.copy(alpha = 0.38f)),
                                (if (colors.isLiquidGlass) Color(0x280284C7) else colors.primary.copy(alpha = 0.16f)),
                                Color.Transparent
                            ),
                            radius = 65f
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                (if (colors.isLiquidGlass) Color(0x4D00E5FF) else colors.primary.copy(alpha = 0.30f)),
                                (if (colors.isLiquidGlass) Color(0x1A00E5FF) else colors.primary.copy(alpha = 0.08f))
                            )
                        )
                    )
                    .border(
                        width = 1.3.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.80f),
                                (if (colors.isLiquidGlass) Color(0x9900E5FF) else colors.primary.copy(alpha = 0.65f)),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(17.dp)
                    )
            ) {
                // Water droplet specular light reflection (glint on top rim)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp)
                        .width((dropletWidth * 0.62f).coerceAtMost(38.dp))
                        .height(2.5.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.75f),
                                    Color.White.copy(alpha = 0.95f),
                                    Color.White.copy(alpha = 0.75f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Layer 3: Moving Bottom Liquid Water Bead Indicator
            val beadWidth = (20.dp * stretchFactor).coerceAtMost(32.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = animatedCenterX - beadWidth / 2, y = (-2).dp)
                    .width(beadWidth)
                    .height(3.5.dp)
            ) {
                // Ambient aquatic glow beneath bead
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = 0.5f }
                        .clip(CircleShape)
                        .background(if (colors.isLiquidGlass) Color(0xFF00E5FF) else colors.primary)
                )
                // Crisp fluid bead
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (colors.isLiquidGlass) Color(0xFF00E5FF) else colors.primary,
                                    Color.White.copy(alpha = 0.95f),
                                    if (colors.isLiquidGlass) Color(0xFF00E5FF) else colors.primary
                                )
                            )
                        )
                )
            }

            // Layer 4: Foreground Interactive Navigation Tabs Row
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectoTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    // Tactile compression on touch
                    val buttonPressScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.88f else 1.0f,
                        animationSpec = spring(stiffness = 550f, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "buttonPressScale"
                    )

                    // Playful icon bounce with overshoot when water droplet arrives
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.22f else 1.0f,
                        animationSpec = spring(
                            stiffness = 320f,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        label = "tabIconBounceScale"
                    )

                    // Smooth vertical floating translation
                    val iconOffsetY by animateDpAsState(
                        targetValue = if (isSelected) (-3.dp) else 0.dp,
                        animationSpec = spring(
                            stiffness = 340f,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        label = "tabIconOffsetY"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            if (colors.isLiquidGlass) Color(0xFF00E5FF) else colors.primary
                        } else colors.textSecondary,
                        animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
                        label = "tabIconColor"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            if (colors.isLiquidGlass) Color(0xFF00E5FF) else colors.primary
                        } else colors.textSecondary,
                        animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
                        label = "tabTextColor"
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
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(33.dp),
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
                                                        color = if (isSelected) {
                                                            if (colors.isLiquidGlass) Color(0xFF00E5FF) else colors.primary
                                                        } else colors.borderSubtle,
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
                                                                if (colors.isLiquidGlass) Color(0xFF00E5FF) else colors.primary,
                                                                if (colors.isLiquidGlass) Color(0xFF0284C7) else Color(0xFF8B5CF6)
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

                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}
