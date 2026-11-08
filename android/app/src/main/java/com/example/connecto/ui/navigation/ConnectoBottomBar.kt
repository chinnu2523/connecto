package com.example.connecto.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.graphics.StrokeCap
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

    val isPureBlack = colors.background == Color(0xFF000000)
    val isLiquidGlass = colors.isLiquidGlass
    val glassAlpha = colors.glassAlpha

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isLiquidGlass) {
                    if (glassAlpha <= 0.005f) {
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = (glassAlpha * 0.25f).coerceIn(0.01f, 0.35f)),
                                Color(0xFF080A10).copy(alpha = (glassAlpha * 0.85f).coerceIn(0.04f, 0.92f))
                            )
                        )
                    }
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            if (!colors.isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF11141C).copy(alpha = 0.88f),
                            if (!colors.isDark) Color.White.copy(alpha = 0.92f) else Color(0xFF0B0D13).copy(alpha = 0.95f)
                        )
                    )
                }
            )
    ) {
        // Specular 1px Hairline Top Border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    if (isLiquidGlass) {
                        val rimAlpha = if (glassAlpha <= 0.005f) 0.55f else (0.65f - glassAlpha * 0.20f).coerceIn(0.30f, 0.75f)
                        Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.White.copy(alpha = rimAlpha),
                                Color.White.copy(alpha = rimAlpha * 0.6f),
                                Color.White.copy(alpha = rimAlpha),
                                Color.White.copy(alpha = 0.10f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                (if (isPureBlack) Color.White else colors.primary).copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.18f),
                                (if (isPureBlack) Color.White else colors.info).copy(alpha = 0.30f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        )
                    }
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

            // Particle Gather Animation & Ambient Physics State
            var previousTabIndex by remember { mutableStateOf(selectedIndex) }
            val particles = remember(currentTab) {
                val dir = (previousTabIndex - selectedIndex).coerceIn(-2, 2)
                val baseAngle = if (dir > 0) 0f else if (dir < 0) PI.toFloat() else 0f
                val hasPrev = dir != 0

                (0 until 32).map { i ->
                    val angle = if (hasPrev && i < 18) {
                        val spread = (Random.nextFloat() - 0.5f) * 1.6f
                        baseAngle + spread
                    } else {
                        Random.nextFloat() * 2f * PI.toFloat()
                    }
                    val distanceDp = 30f + Random.nextFloat() * 54f
                    val sizeDp = 1.6f + Random.nextFloat() * 2.2f
                    val delay = (Random.nextFloat() * 0.20f)
                    val curvature = (Random.nextFloat() - 0.5f) * 1.8f
                    val tone = when (i % 5) {
                        0 -> 2 // white / highlight
                        1, 2 -> 1 // cyan / accent
                        else -> 0 // primary brand
                    }
                    val streak = 0.8f + Random.nextFloat() * 0.8f

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

            val infiniteTransition = rememberInfiniteTransition(label = "bottomBarIdle")
            val idleOrbitPhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2f * PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "idleOrbitPhase"
            )
            val idleGlowPulse by infiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "idleGlowPulse"
            )

            val gatherProgress = remember { Animatable(1f) }
            LaunchedEffect(currentTab) {
                gatherProgress.snapTo(0f)
                gatherProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 520,
                        easing = CubicBezierEasing(0.16f, 0.90f, 0.22f, 1.0f)
                    )
                )
                previousTabIndex = selectedIndex
            }

            // LAYER 1: Multi-Tier Ambient Glow, Particle Gather & Idle Orbit
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pillCenter = Offset(x = animatedCenterX.toPx(), y = 20.dp.toPx())
                val primaryColor = if (isPureBlack) Color.White else colors.primary
                val accentCyan = if (isPureBlack) Color(0xFFE0E0E0) else colors.info
                val glowWhite = Color.White

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

                // Particle Gather Inward Physics
                val gProg = gatherProgress.value
                if (gProg < 1f) {
                    particles.forEach { p ->
                        if (gProg >= p.delay) {
                            val localT = ((gProg - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)

                            // Gravitational inward suction curve
                            val easeT = localT.pow(1.85f)
                            val currentDistPx = p.initialDistanceDp.dp.toPx() * (1f - easeT)
                            val currentAngle = p.initialAngleRad + p.curvature * (1f - easeT)

                            val curX = pillCenter.x + currentDistPx * cos(currentAngle)
                            val curY = pillCenter.y + currentDistPx * sin(currentAngle) * 0.62f

                            // Fade in on spawn, fade out when reaching center
                            val particleAlpha = (if (localT < 0.15f) localT / 0.15f else if (localT > 0.88f) (1f - localT) / 0.12f else 1f).coerceIn(0f, 1f)

                            val pColor = when (p.colorTone) {
                                1 -> accentCyan
                                2 -> glowWhite
                                else -> primaryColor
                            }

                            // Dynamic motion trailing streak
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
                                    end = Offset(curX, curY),
                                    strokeWidth = (p.sizeDp.dp.toPx() * 0.85f).coerceAtLeast(1f),
                                    cap = StrokeCap.Round
                                )
                            }

                            // Glowing particle head (soft aura + sharp core)
                            val radiusPx = p.sizeDp.dp.toPx()
                            drawCircle(
                                color = pColor.copy(alpha = particleAlpha * 0.35f),
                                radius = radiusPx * 2.2f,
                                center = Offset(curX, curY)
                            )
                            drawCircle(
                                color = if (p.colorTone == 2) Color.White.copy(alpha = particleAlpha) else pColor.copy(alpha = particleAlpha * 0.95f),
                                radius = radiusPx,
                                center = Offset(curX, curY)
                            )
                        }
                    }

                    // Coalescence absorption flare at pill center
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
                    // Ambient Idle Motes hovering around active capsule when settled
                    val idleMoteCount = 5
                    for (i in 0 until idleMoteCount) {
                        val phaseOffset = i * (2f * PI.toFloat() / idleMoteCount)
                        val angle = idleOrbitPhase + phaseOffset
                        val baseRadiusX = 28.dp.toPx() + (i % 3) * 4.dp.toPx()
                        val baseRadiusY = 16.dp.toPx() + (i % 2) * 3.dp.toPx()

                        val moteX = pillCenter.x + cos(angle) * baseRadiusX
                        val moteY = pillCenter.y + sin(angle) * baseRadiusY

                        val moteAlpha = (0.22f + 0.18f * sin(angle * 2f + i.toFloat())).coerceIn(0.08f, 0.45f)
                        val moteColor = if (i % 2 == 0) primaryColor else accentCyan
                        val moteRadius = (1.4f + (i % 2) * 0.8f).dp.toPx() * idleGlowPulse

                        drawCircle(
                            color = moteColor.copy(alpha = moteAlpha),
                            radius = moteRadius,
                            center = Offset(moteX, moteY)
                        )
                    }
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
