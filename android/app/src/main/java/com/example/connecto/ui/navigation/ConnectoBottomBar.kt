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

    val isPureBlack = colors.background == Color(0xFF000000) || (!colors.isGlass && colors.isDark)
    val isLiquidGlass = colors.isLiquidGlass
    val glassAlpha = colors.glassAlpha

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val totalWidth = maxWidth
        val tabCount = ConnectoTab.entries.size
        val tabWidth = totalWidth / tabCount
        val selectedIndex = ConnectoTab.entries.indexOf(currentTab).coerceAtLeast(0)

        val density = androidx.compose.ui.platform.LocalDensity.current
        // Baseline top of the bottom bar (leaving room above for the prominent liquid bulge curve)
        val baseTopY = with(density) { 24.dp.toPx() }

        // Dynamic tracking center of the active tab with fluid ease-in-out curve
        val targetCenterX = tabWidth * selectedIndex + tabWidth / 2
        val animatedCenterX by animateDpAsState(
            targetValue = targetCenterX,
            animationSpec = tween(
                durationMillis = 280,
                easing = EaseInOutCubic
            ),
            label = "navBulgeCenterX"
        )

        // Fluid squish & stretch during movement
        val distance = (targetCenterX - animatedCenterX).value.dp
        val stretchFactor = 1f + (distance.value.absoluteValue / 60f).coerceAtMost(0.25f)
        val squishY = 1f - (distance.value.absoluteValue / 140f).coerceAtMost(0.15f)

        // Liquid Jelly Spring Rebound on Arrival
        val liquidJiggle = remember { Animatable(0f) }
        LaunchedEffect(currentTab) {
            liquidJiggle.snapTo(1f)
            liquidJiggle.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.60f,
                    stiffness = 450f
                )
            )
        }
        val jiggleBounce = 1f + (liquidJiggle.value * 0.18f * sin(liquidJiggle.value * 3f * PI.toFloat()))
        // 02 LIQUID BULGE: Prominent organic dome matching blueprint (h 30, R 30)
        val bulgeHeightPx = with(density) { 22.dp.toPx() } * squishY * jiggleBounce
        val bulgeHalfWidthPx = with(density) { (44.dp * stretchFactor).toPx() }

        // Particle Gather Animation & Ambient Physics State
        var previousTabIndex by remember { mutableStateOf(selectedIndex) }
        val particles = remember(currentTab) {
            val dir = (previousTabIndex - selectedIndex).coerceIn(-2, 2)
            val baseAngle = if (dir > 0) 0f else if (dir < 0) PI.toFloat() else 0f
            val hasPrev = dir != 0

            (0 until 20).map { i ->
                val angle = if (hasPrev && i < 12) {
                    val spread = (Random.nextFloat() - 0.5f) * 1.6f
                    baseAngle + spread
                } else {
                    Random.nextFloat() * 2f * PI.toFloat()
                }
                val distanceDp = 24f + Random.nextFloat() * 40f
                val sizeDp = 1.6f + Random.nextFloat() * 1.8f
                val delay = (Random.nextFloat() * 0.15f)
                val curvature = (Random.nextFloat() - 0.5f) * 1.5f
                val tone = when (i % 4) {
                    0 -> 2 // white / highlight
                    1 -> 1 // cyan / accent
                    else -> 0 // primary brand
                }
                val streak = 0.8f + Random.nextFloat() * 0.6f

                TabGatherParticle(
                    initialAngleRad = angle,
                    initialDistanceDp = distanceDp,
                    sizeDp = sizeDp,
                    delay = delay,
                    curvature = curvature,
                    colorTone = tone,
                    streakFactor = streak
                )
            }
        }

        val gatherProgress = remember { Animatable(1f) }
        LaunchedEffect(currentTab) {
            gatherProgress.snapTo(0f)
            gatherProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 240,
                    easing = EaseInOutCubic
                )
            )
            previousTabIndex = selectedIndex
        }

        // LAYER 1: 02 LIQUID BULGE (Gooey contour + threshold blueprint h 30, R 30)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val curX = animatedCenterX.toPx()
            val peakY = (baseTopY - bulgeHeightPx).coerceAtLeast(1.5.dp.toPx())
            val leftBulgeX = curX - bulgeHalfWidthPx
            val rightBulgeX = curX + bulgeHalfWidthPx

            // Liquid Bulge Silhouette Path
            val bulgePath = Path().apply {
                moveTo(0f, baseTopY)
                lineTo(leftBulgeX.coerceAtLeast(0f), baseTopY)
                // Left smooth cubic curve into peak (R 30 organic transition)
                cubicTo(
                    leftBulgeX + 18.dp.toPx() * stretchFactor, baseTopY,
                    curX - 20.dp.toPx() * stretchFactor, peakY,
                    curX, peakY
                )
                // Right smooth cubic curve down from peak
                cubicTo(
                    curX + 20.dp.toPx() * stretchFactor, peakY,
                    rightBulgeX - 18.dp.toPx() * stretchFactor, baseTopY,
                    rightBulgeX.coerceAtMost(size.width), baseTopY
                )
                lineTo(size.width, baseTopY)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            // Fill Liquid Bulge Canvas with ambient glass / background brush
            val fillBrush = if (isLiquidGlass) {
                val alpha = if (glassAlpha <= 0.005f) 0.12f else (glassAlpha * 0.88f).coerceIn(0.15f, 0.94f)
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = (glassAlpha * 0.28f).coerceIn(0.04f, 0.38f)),
                        Color(0xFF080A10).copy(alpha = alpha)
                    ),
                    startY = peakY,
                    endY = size.height
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        if (!colors.isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF141722).copy(alpha = 0.92f),
                        if (!colors.isDark) Color(0xFFF1F5F9).copy(alpha = 0.96f) else Color(0xFF0A0C12).copy(alpha = 0.98f)
                    ),
                    startY = peakY,
                    endY = size.height
                )
            }
            drawPath(path = bulgePath, brush = fillBrush)

            // Specular Liquid Rim Stroke along the entire top contour including the Liquid Bulge
            val rimPath = Path().apply {
                moveTo(0f, baseTopY)
                lineTo(leftBulgeX.coerceAtLeast(0f), baseTopY)
                cubicTo(
                    leftBulgeX + 18.dp.toPx() * stretchFactor, baseTopY,
                    curX - 20.dp.toPx() * stretchFactor, peakY,
                    curX, peakY
                )
                cubicTo(
                    curX + 20.dp.toPx() * stretchFactor, peakY,
                    rightBulgeX - 18.dp.toPx() * stretchFactor, baseTopY,
                    rightBulgeX.coerceAtMost(size.width), baseTopY
                )
                lineTo(size.width, baseTopY)
            }

            val primaryColor = if (isPureBlack) Color.White else colors.primary
            val rimBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    primaryColor.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.85f), // High specular brilliance at the liquid bulge peak
                    primaryColor.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.08f)
                ),
                startX = (curX - 60.dp.toPx()).coerceAtLeast(0f),
                endX = (curX + 60.dp.toPx()).coerceAtMost(size.width)
            )

            drawPath(
                path = rimPath,
                brush = rimBrush,
                style = Stroke(
                    width = 1.35.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Multi-Tier Ambient Glow, Particle Gather & Idle Orbit
            val pillCenter = Offset(x = curX, y = peakY + 16.dp.toPx())
            val accentCyan = if (isPureBlack) Color(0xFFE0E0E0) else colors.info
            val glowWhite = Color.White

            // Soft ambient colored glow under active bulge
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.28f),
                        primaryColor.copy(alpha = 0.07f),
                        Color.Transparent
                    ),
                    radius = 32.dp.toPx() * stretchFactor,
                    center = pillCenter
                ),
                radius = 32.dp.toPx() * stretchFactor,
                center = pillCenter
            )

            // Particle Gather Inward Physics
            val gProg = gatherProgress.value
            if (gProg < 1f) {
                particles.forEach { p ->
                    if (gProg >= p.delay) {
                        val localT = ((gProg - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
                        val easeT = localT.pow(1.85f)
                        val currentDistPx = p.initialDistanceDp.dp.toPx() * (1f - easeT)
                        val currentAngle = p.initialAngleRad + p.curvature * (1f - easeT)

                        val curPartX = pillCenter.x + currentDistPx * cos(currentAngle)
                        val curPartY = pillCenter.y + currentDistPx * sin(currentAngle) * 0.62f

                        val particleAlpha = (if (localT < 0.15f) localT / 0.15f else if (localT > 0.88f) (1f - localT) / 0.12f else 1f).coerceIn(0f, 1f)

                        val pColor = when (p.colorTone) {
                            1 -> accentCyan
                            2 -> glowWhite
                            else -> primaryColor
                        }

                        if (localT > 0.04f && currentDistPx > 4.dp.toPx()) {
                            val trailT = (localT - 0.08f * p.streakFactor).coerceAtLeast(0f)
                            val trailEase = trailT.pow(1.85f)
                            val trailDistPx = p.initialDistanceDp.dp.toPx() * (1f - trailEase)
                            val trailAngle = p.initialAngleRad + p.curvature * (1f - trailEase)
                            val trailX = pillCenter.x + trailDistPx * cos(trailAngle)
                            val trailY = pillCenter.y + trailDistPx * sin(trailAngle) * 0.62f

                            drawLine(
                                color = pColor.copy(alpha = particleAlpha * 0.45f),
                                start = Offset(trailX, trailY),
                                end = Offset(curPartX, curPartY),
                                strokeWidth = (p.sizeDp.dp.toPx() * 0.85f).coerceAtLeast(1f),
                                cap = StrokeCap.Round
                            )
                        }

                        val radiusPx = p.sizeDp.dp.toPx()
                        drawCircle(
                            color = pColor.copy(alpha = particleAlpha * 0.35f),
                            radius = radiusPx * 2.2f,
                            center = Offset(curPartX, curPartY)
                        )
                        drawCircle(
                            color = if (p.colorTone == 2) Color.White.copy(alpha = particleAlpha) else pColor.copy(alpha = particleAlpha * 0.95f),
                            radius = radiusPx,
                            center = Offset(curPartX, curPartY)
                        )
                    }
                }

                if (gProg >= 0.55f) {
                    val flareP = ((gProg - 0.55f) / 0.45f).coerceIn(0f, 1f)
                    val flareRadius = 10.dp.toPx() + 22.dp.toPx() * flareP
                    val flareAlpha = (sin(flareP * PI.toFloat()) * 0.42f).coerceIn(0f, 1f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowWhite.copy(alpha = flareAlpha * 0.85f),
                                primaryColor.copy(alpha = flareAlpha * 0.45f),
                                Color.Transparent
                            ),
                            radius = flareRadius,
                            center = pillCenter
                        ),
                        radius = flareRadius,
                        center = pillCenter
                    )
                }
            } else {
                // Calm, highly optimized static ambient accent when idle — zero frame drops & zero GPU burn!
                drawCircle(
                    color = primaryColor.copy(alpha = 0.12f),
                    radius = 18.dp.toPx() * stretchFactor,
                    center = pillCenter
                )
            }
        }

        // LAYER 2: Interactive Ergonomic Nav Buttons Row with Elevated Bulge Icons
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConnectoTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                // Tactile micro-compression on tap with ease-in / ease-out curves
                val buttonPressScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1.0f,
                    animationSpec = tween(
                        durationMillis = 180,
                        easing = if (isPressed) EaseInCubic else EaseOutCubic
                    ),
                    label = "buttonPressScale"
                )

                // Smooth icon elevation & ease-in-out curve on selection into Liquid Bulge
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.20f else 1.0f,
                    animationSpec = tween(
                        durationMillis = 240,
                        easing = if (isSelected) EaseOutCubic else EaseInCubic
                    ),
                    label = "tabIconScale"
                )

                // Elevates directly into the apex of the Liquid Bulge dome (Blueprint 02)
                val iconOffsetY by animateDpAsState(
                    targetValue = if (isSelected) (-12.dp) else 0.dp,
                    animationSpec = tween(
                        durationMillis = 240,
                        easing = if (isSelected) EaseOutCubic else EaseInCubic
                    ),
                    label = "tabIconOffsetY"
                )

                val iconPrimary = if (isPureBlack) Color.White else colors.primary
                val iconInactive = colors.textSecondary.copy(alpha = 0.70f)

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) iconPrimary else colors.textSecondary,
                    animationSpec = tween(durationMillis = 200, easing = EaseInOutCubic),
                    label = "tabTextColor"
                )

                // Custom Ergonomic Button Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(32.dp),
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

                                    // Dynamic Unread & Active Status Badges
                                    if (tab == ConnectoTab.CALLS && isCallActive) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(colors.success)
                                                .border(1.5.dp, colors.surface, CircleShape)
                                        )
                                    } else if (tab == ConnectoTab.MESSAGES && unreadMessagesCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 8.dp, y = (-4).dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(iconPrimary)
                                                .border(1.2.dp, colors.surface, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 4.dp, vertical = 0.5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (unreadMessagesCount > 99) "99+" else unreadMessagesCount.toString(),
                                                color = if (isPureBlack) Color.Black else Color.White,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                lineHeight = 10.sp
                                            )
                                        }
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
                                fontSize = if (isSelected) 10.5.sp else 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = if (isSelected) 0.3.sp else 0.15.sp,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }
