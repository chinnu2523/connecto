package com.example.connecto.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.connecto.ui.designsystem.LocalConnectoColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Connecto Drag-To-Refresh Component
 *
 * Specifications:
 * 1. Strict Scroll-Boundary Gesture Gating:
 *    Only engages when list is strictly at the very top (!canScrollBackward()) and user performs
 *    a downward vertical drag (dy > 0 and abs(dy) > abs(dx)). Normal list scrolling is never hijacked.
 * 2. Rubber-Band Drag Physics & Haptic Threshold:
 *    Damping equation: pullDistance = rawDragY * 0.45, capped at max visual travel of 96px.
 *    Activation threshold at 68px. Crossing 68px triggers a crisp light haptic pulse and
 *    refresh icon scale bounce (1.0 -> 1.15 -> 1.0). Releasing before 68px springs back to 0px
 *    (stiffness: 380, dampingRatio: 0.75).
 * 3. Custom Floating Refresh Indicator (Overlay):
 *    Never shifts header or leaves black gap. 38px x 38px circular badge (#161A26 dark / #FFFFFF light,
 *    1px border rgba(255,255,255,0.1), soft shadow) sliding from behind header (-48px -> +24px).
 *    Drag-driven rotation (0deg -> 270deg) & opacity (0 -> 1).
 *    When active: resting at translateY 16px with continuous 360deg rotational loop (800ms linear).
 * 4. Guaranteed Completion & Timeout Guard:
 *    Smooth 200ms ease-in exit animation (scale 1.0 -> 0.5, fade out, retract to -48px).
 *    6000ms safety watchdog prevents UI freezing if network drops.
 */
@Composable
fun ConnectoDragToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshText: String = "Syncing...",
    canScrollBackward: () -> Boolean = { false },
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val colors = LocalConnectoColors.current
    val scope = rememberCoroutineScope()

    // Key Physical Metrics
    val thresholdDp = 68.dp
    val maxTravelDp = 96.dp
    val thresholdPx = with(density) { thresholdDp.toPx() }
    val maxTravelPx = with(density) { maxTravelDp.toPx() }
    val restingPx = with(density) { 16.dp.toPx() }
    val hiddenPx = with(density) { (-48).dp.toPx() }

    val pullDistance = remember { Animatable(0f) }
    var isPullEligible by remember { mutableStateOf(false) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    var isUserInitiatedRefresh by remember { mutableStateOf(false) }

    // Icon scale bounce on crossing 68px threshold
    val iconScaleAnim = remember { Animatable(1f) }

    // Exit animation states
    val badgeExitScale = remember { Animatable(1f) }
    val badgeExitAlpha = remember { Animatable(1f) }
    var isRetracting by remember { mutableStateOf(false) }

    // Continuous 360deg rotation loop during active refresh (800ms linear)
    val infiniteTransition = rememberInfiniteTransition(label = "refreshSpinner")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAngle"
    )

    // Handle isRefreshing transitions & guaranteed timeout guard
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            if (pullDistance.value > 0f || isUserInitiatedRefresh) {
                isRetracting = true
                // Retract badge: scale 1.0 -> 0.5, fade out, retract behind header over 200ms ease-in
                scope.launch {
                    badgeExitScale.animateTo(0.5f, animationSpec = tween(200, easing = FastOutLinearInEasing))
                }
                scope.launch {
                    badgeExitAlpha.animateTo(0f, animationSpec = tween(200, easing = FastOutLinearInEasing))
                }
                pullDistance.animateTo(
                    targetValue = hiddenPx,
                    animationSpec = tween(200, easing = FastOutLinearInEasing)
                )
                pullDistance.snapTo(0f)
                badgeExitScale.snapTo(1f)
                badgeExitAlpha.snapTo(1f)
                isRetracting = false
                isUserInitiatedRefresh = false
                hasTriggeredHaptic = false
                isPullEligible = false
            }
        } else {
            // When refresh begins
            badgeExitScale.snapTo(1f)
            badgeExitAlpha.snapTo(1f)
            isRetracting = false
            if (isUserInitiatedRefresh) {
                pullDistance.animateTo(
                    targetValue = restingPx,
                    animationSpec = spring(stiffness = 380f, dampingRatio = 0.8f)
                )
            }
        }
    }

    // Safety Watchdog: Guard against stuck refreshing state if network drops or hangs
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(6000L)
            if (isRefreshing && isUserInitiatedRefresh) {
                isRetracting = true
                scope.launch {
                    badgeExitScale.animateTo(0.5f, animationSpec = tween(200, easing = FastOutLinearInEasing))
                }
                scope.launch {
                    badgeExitAlpha.animateTo(0f, animationSpec = tween(200, easing = FastOutLinearInEasing))
                }
                pullDistance.animateTo(0f, tween(200, easing = FastOutLinearInEasing))
                isRetracting = false
                isUserInitiatedRefresh = false
            }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isRefreshing) return Offset.Zero

                // Push back up: If indicator was pulled down, consume upward drag to retract it
                if (available.y < 0 && pullDistance.value > 0f) {
                    val newPull = (pullDistance.value + available.y * 0.45f).coerceAtLeast(0f)
                    val consumedY = (newPull - pullDistance.value) / 0.45f
                    scope.launch { pullDistance.snapTo(newPull) }
                    return Offset(0f, consumedY)
                }

                // Check eligibility on first touch
                if (source == NestedScrollSource.UserInput && pullDistance.value == 0f) {
                    isPullEligible = !canScrollBackward()
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (isRefreshing) return Offset.Zero
                if (source != NestedScrollSource.UserInput) return Offset.Zero

                // If content consumed any upward scroll, user was scrolling from below
                if (consumed.y > 0) {
                    isPullEligible = false
                    return Offset.Zero
                }

                // Strictly engage pull gesture only when at top AND dragging downward vertically
                val isAtTop = !canScrollBackward()
                val isVertical = abs(available.y) > abs(available.x)

                if (available.y > 0 && isAtTop && isVertical && isPullEligible) {
                    // Progressive rubber-band damping: rawDragY * 0.45 capped at 96px
                    val dampedDelta = available.y * 0.45f
                    val newPull = (pullDistance.value + dampedDelta).coerceAtMost(maxTravelPx)
                    scope.launch { pullDistance.snapTo(newPull) }

                    // Exact 68px threshold crossing: crisp light haptic pulse + icon scale bounce
                    if (newPull >= thresholdPx && !hasTriggeredHaptic) {
                        hasTriggeredHaptic = true
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}
                        scope.launch {
                            iconScaleAnim.animateTo(1.15f, animationSpec = tween(110, easing = FastOutSlowInEasing))
                            iconScaleAnim.animateTo(1.0f, animationSpec = spring(stiffness = 500f, dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    } else if (newPull < thresholdPx && hasTriggeredHaptic) {
                        hasTriggeredHaptic = false
                    }

                    return Offset(0f, available.y)
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (isRefreshing) return Velocity.Zero

                val reachedThreshold = pullDistance.value >= thresholdPx
                val isEligible = isPullEligible

                isPullEligible = false

                if (reachedThreshold && isEligible) {
                    isUserInitiatedRefresh = true
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {}

                    // Snap indicator to resting position (translateY: 16px)
                    pullDistance.animateTo(
                        targetValue = restingPx,
                        animationSpec = spring(stiffness = 380f, dampingRatio = 0.8f)
                    )
                    onRefresh()
                } else {
                    isUserInitiatedRefresh = false
                    // Release before reaching 68px: spring back to 0px (stiffness: 380, damping: 28)
                    pullDistance.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(stiffness = 380f, dampingRatio = 0.75f)
                    )
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                isPullEligible = false
                if (pullDistance.value > 0f && !isRefreshing) {
                    isUserInitiatedRefresh = false
                    pullDistance.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(stiffness = 380f, dampingRatio = 0.75f)
                    )
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // 1. Content layer: Strictly sits at natural position with NO layout shift or black gap
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        // 2. Custom Floating Refresh Indicator Overlay
        val showBadge = pullDistance.value > 1f || isRefreshing || isRetracting
        if (showBadge) {
            val progress = (pullDistance.value / thresholdPx).coerceIn(0f, 1f)

            // Slides out from behind top header: translateY from -48px to +24px (resting at 16px when active)
            val badgeTranslateY = if (isRefreshing) {
                16.dp
            } else {
                (-48).dp + 72.dp * progress
            }

            // Drag-driven rotation (0deg -> 270deg) or continuous 360deg loop during active refresh
            val iconRotation = if (isRefreshing) spinRotation else (progress * 270f)
            val badgeAlpha = if (isRefreshing) 1f else progress

            val badgeBg = if (colors.isDark) Color(0xFF161A26) else Color(0xFFFFFFFF)
            val badgeBorder = if (colors.isDark) Color(0x1AFFFFFF) else Color(0x1F000000)

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, with(density) { badgeTranslateY.toPx().roundToInt() }) }
                    .zIndex(20f)
                    .scale(if (isRetracting) badgeExitScale.value else 1f)
                    .alpha(if (isRetracting) badgeExitAlpha.value else badgeAlpha)
                    .size(38.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    )
                    .background(color = badgeBg, shape = CircleShape)
                    .border(width = 1.dp, color = badgeBorder, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh",
                    tint = if (colors.isDark) Color.White else Color(0xFF0F172A),
                    modifier = Modifier
                        .size(20.dp)
                        .scale(iconScaleAnim.value)
                        .rotate(iconRotation)
                )
            }
        }
    }
}
