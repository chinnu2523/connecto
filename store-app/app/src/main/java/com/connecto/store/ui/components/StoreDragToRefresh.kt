package com.connecto.store.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Authentic Elastic Pull-To-Refresh container for Our Store.
 * Features:
 * - Dynamic asymptotic rubber-band elasticity (resistance increases smoothly as you pull)
 * - Strict top-anchor qualification & hysteresis deadband to prevent accidental auto-refresh during normal scrolling
 * - Elastic spring physics on release and return (Spring.DampingRatioMediumBouncy)
 * - Tactile morphing OLED pill with spring-loaded scale overshoot, arrow rotation, and haptic snap
 * - Protected in-flight refresh state preventing gesture disruption or stuck visual states
 */
@Composable
fun StoreDragToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshText: String = "Syncing Catalog...",
    canScrollBackward: () -> Boolean = { false },
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Thresholds: Intentional pull threshold (86dp) & maximum elastic overscroll ceiling (148dp)
    val refreshThresholdPx = with(density) { 86.dp.toPx() }
    val maxDragDistancePx = with(density) { 148.dp.toPx() }
    val hysteresisPx = with(density) { 12.dp.toPx() }

    val pullOffset = remember { Animatable(0f) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    // Strict two-phase gesture qualification to eliminate accidental refresh during normal scrolling
    var isTouchActive by remember { mutableStateOf(false) }
    var gestureQualifiedForPull by remember { mutableStateOf(false) }
    var accumulatedPullDistance by remember { mutableFloatStateOf(0f) }
    var userInitiatedPull by remember { mutableStateOf(false) }

    // Guard against multiple rapid re-entrant refresh triggers
    var isRefreshDispatched by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            isRefreshDispatched = false
            hasTriggeredHaptic = false
            gestureQualifiedForPull = false
            isTouchActive = false
            accumulatedPullDistance = 0f
            userInitiatedPull = false

            // Bouncy elastic spring return when refresh finishes
            pullOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        } else {
            // ONLY hold at resting indicator position if the user actually pulled down!
            // When silent background sync or automatic revalidation occurs, pullOffset remains 0f
            if (userInitiatedPull || pullOffset.value > 10f) {
                pullOffset.animateTo(
                    targetValue = refreshThresholdPx,
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // When actively refreshing, lock the container in place - do not allow touch disruption
                if (isRefreshing) {
                    if (available.y < 0 && pullOffset.value > 0f) {
                        return Offset.Zero
                    }
                    return Offset.Zero
                }

                // If user pulls down, then changes direction and pushes back up while indicator is visible
                if (available.y < 0 && pullOffset.value > 0f) {
                    val newOffset = (pullOffset.value + available.y).coerceAtLeast(0f)
                    val consumed = newOffset - pullOffset.value
                    accumulatedPullDistance = (accumulatedPullDistance + available.y).coerceAtLeast(0f)
                    scope.launch { pullOffset.snapTo(newOffset) }
                    return Offset(0f, consumed)
                }

                // Initialize gesture eligibility on the VERY FIRST touch down of a gesture stroke
                if (source == NestedScrollSource.UserInput) {
                    if (!isTouchActive) {
                        isTouchActive = true
                        // CRITICAL ROOT-CAUSE FIX:
                        // A gesture is ONLY qualified if the list was ALREADY completely settled at the absolute top
                        // when the finger touched down. If the user was scrolling up from below, canScrollBackward is true,
                        // which PERMANENTLY disqualifies this entire gesture from accidentally triggering refresh!
                        gestureQualifiedForPull = !canScrollBackward()
                        accumulatedPullDistance = 0f
                        userInitiatedPull = false
                    }
                } else if (source == NestedScrollSource.SideEffect) {
                    // Flings and momentum animations are strictly disqualified
                    gestureQualifiedForPull = false
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (isRefreshing) return Offset.Zero

                // Momentum flings must NEVER pull down or trigger refresh
                if (source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }

                // If content consumed any upward scroll during this stroke, the user was scrolling
                // from below - mark as permanently disqualified for this entire gesture
                if (consumed.y > 0) {
                    gestureQualifiedForPull = false
                    return Offset.Zero
                }

                // Only perform elastic pull down if the gesture started stationary at the top
                if (available.y > 0 && gestureQualifiedForPull) {
                    accumulatedPullDistance += available.y

                    // Hysteresis deadband: first 12dp of drag is consumed as gesture intent confirmation
                    if (accumulatedPullDistance > hysteresisPx) {
                        userInitiatedPull = true
                        val activeDelta = available.y

                        // Dynamic asymptotic rubber-band elasticity equation:
                        // Resistance increases smoothly as offset approaches maxDragDistancePx
                        val currentFraction = (pullOffset.value / maxDragDistancePx).coerceIn(0f, 1f)
                        val elasticTension = (1f - currentFraction.pow(0.65f)).coerceIn(0.12f, 0.65f)
                        val dampedDelta = activeDelta * elasticTension

                        val newOffset = (pullOffset.value + dampedDelta).coerceAtMost(maxDragDistancePx)
                        scope.launch { pullOffset.snapTo(newOffset) }

                        // Tactile haptic feedback on threshold cross
                        if (newOffset >= refreshThresholdPx && !hasTriggeredHaptic) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            hasTriggeredHaptic = true
                        } else if (newOffset < refreshThresholdPx && hasTriggeredHaptic) {
                            hasTriggeredHaptic = false
                        }

                        return Offset(0f, available.y)
                    }
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                isTouchActive = false

                if (isRefreshing) return Velocity.Zero

                val reachedThreshold = pullOffset.value >= refreshThresholdPx
                val isEligible = gestureQualifiedForPull && userInitiatedPull && !isRefreshDispatched

                gestureQualifiedForPull = false

                if (reachedThreshold && isEligible) {
                    isRefreshDispatched = true
                    // Tactile confirmation feedback
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRefresh()
                } else {
                    userInitiatedPull = false
                    // Elastic rebound bounce back to 0
                    pullOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                isTouchActive = false
                gestureQualifiedForPull = false
                if (pullOffset.value > 0f && !isRefreshing) {
                    pullOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
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
        // Content container with fluid elastic rubber-band transform
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = pullOffset.value
                    // Subtle elastic scale stretch on the content container
                    val stretchFactor = (pullOffset.value / maxDragDistancePx) * 0.02f
                    scaleY = 1f + stretchFactor
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
        ) {
            content()

            // CRITICAL TOUCH SHIELD: When actively pulling (pullOffset > 8dp) or refreshing,
            // intercept pointer input to completely prevent accidental clicks on the cards underneath
            if (pullOffset.value > 8f || (isRefreshing && userInitiatedPull)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { /* absorb touch events during pull gesture */ }
                        }
                )
            }
        }

        // Tactile Elastic Indicator Pill (Strictly only shown if user pulled or during user-initiated refresh)
        if (pullOffset.value > 8f || (isRefreshing && userInitiatedPull)) {
            val progress = (pullOffset.value / refreshThresholdPx).coerceIn(0f, 1f)
            val isThresholdReached = pullOffset.value >= refreshThresholdPx

            // Dynamic scale with springy overshoot as threshold is crossed
            val pillScale = if (isRefreshing) {
                1.0f
            } else {
                (0.72f + progress * 0.34f).coerceIn(0.72f, 1.06f)
            }

            val pillAlpha = if (isRefreshing) 1f else (pullOffset.value / (refreshThresholdPx * 0.4f)).coerceIn(0f, 1f)

            val pillBg = Color(0xFF09090B)
            val accentColor = Color(0xFF38BDF8)
            val pillBorder = if (isRefreshing || isThresholdReached) accentColor else Color(0x33FFFFFF)
            val textColor = Color(0xFFF1F5F9)

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .offset {
                        // Elastic indicator positioning with smooth physics
                        val indicatorOffset = (pullOffset.value * 0.76f - with(density) { 32.dp.toPx() })
                            .coerceAtLeast(0f)
                        IntOffset(0, indicatorOffset.roundToInt())
                    }
                    .scale(pillScale)
                    .alpha(pillAlpha)
                    .shadow(
                        elevation = if (isThresholdReached || isRefreshing) 12.dp else 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = accentColor.copy(alpha = 0.25f),
                        spotColor = accentColor.copy(alpha = 0.45f)
                    )
                    .background(
                        color = pillBg,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .border(
                        width = if (isThresholdReached || isRefreshing) 1.5.dp else 1.dp,
                        color = pillBorder,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = refreshText,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        // Smoothly rotating arrow based on elastic pull progress
                        val rotation = progress * 180f
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = "Pull to refresh",
                            tint = if (isThresholdReached) accentColor else Color(0xFF94A3B8),
                            modifier = Modifier
                                .size(14.dp)
                                .rotate(if (isThresholdReached) 180f else rotation)
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = if (isThresholdReached) "Release to refresh" else "Pull down to refresh",
                            color = if (isThresholdReached) accentColor else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
