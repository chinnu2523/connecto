package com.connecto.store.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Tactile Drag-To-Refresh container for Our Store.
 * Supports smooth gesture overscroll damping, haptic feedback on threshold cross,
 * and high-contrast OLED pill indicator with spring physics.
 */
@Composable
fun StoreDragToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    canScrollBackward: () -> Boolean = { false },
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val refreshThresholdPx = with(density) { 70.dp.toPx() }
    val maxDragDistancePx = with(density) { 115.dp.toPx() }

    val pullOffset = remember { Animatable(0f) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    // Track active user gesture lifecycle and eligibility to prevent auto-refresh during normal scrolling
    var isUserDragging by remember { mutableStateOf(false) }
    var isEligibleForPull by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            hasTriggeredHaptic = false
            isEligibleForPull = false
            isUserDragging = false
        } else {
            pullOffset.animateTo(
                targetValue = refreshThresholdPx,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If user is dragging backwards up while refresh pill is pulled down
                if (available.y < 0 && pullOffset.value > 0f) {
                    val newOffset = (pullOffset.value + available.y).coerceAtLeast(0f)
                    val consumed = newOffset - pullOffset.value
                    scope.launch { pullOffset.snapTo(newOffset) }
                    return Offset(0f, consumed)
                }

                // Initialize gesture eligibility on the very first touch movement of a gesture
                if (source == NestedScrollSource.UserInput) {
                    if (!isUserDragging && pullOffset.value <= 0f) {
                        isUserDragging = true
                        // Only eligible for pull-to-refresh if the user is ALREADY settled at the top!
                        isEligibleForPull = !canScrollBackward()
                    }
                } else if (source == NestedScrollSource.SideEffect) {
                    // Flings are NEVER eligible for pull-to-refresh
                    isEligibleForPull = false
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (isRefreshing) return Offset.Zero

                // Flings and momentum animations must NEVER pull down or trigger refresh
                if (source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }

                // If content consumed any upward scroll during this stroke, the user was scrolling
                // from below - mark as ineligible for pull-to-refresh for the remainder of this gesture!
                if (consumed.y > 0) {
                    isEligibleForPull = false
                    return Offset.Zero
                }

                // Only pull down if user was already at top and actively dragging downwards
                if (available.y > 0 && isEligibleForPull) {
                    val dampedDelta = available.y * 0.42f
                    val newOffset = (pullOffset.value + dampedDelta).coerceAtMost(maxDragDistancePx)
                    scope.launch { pullOffset.snapTo(newOffset) }

                    if (newOffset >= refreshThresholdPx && !hasTriggeredHaptic) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        hasTriggeredHaptic = true
                    } else if (newOffset < refreshThresholdPx) {
                        hasTriggeredHaptic = false
                    }

                    return Offset(0f, available.y)
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                isUserDragging = false

                if (isRefreshing) return Velocity.Zero

                if (pullOffset.value >= refreshThresholdPx && isEligibleForPull) {
                    isEligibleForPull = false
                    onRefresh()
                } else {
                    isEligibleForPull = false
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
                isUserDragging = false
                isEligibleForPull = false
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, pullOffset.value.roundToInt()) }
        ) {
            content()
        }

        if (pullOffset.value > 6f || isRefreshing) {
            val progress = (pullOffset.value / refreshThresholdPx).coerceIn(0f, 1f)
            val isThresholdReached = pullOffset.value >= refreshThresholdPx

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .offset {
                        val indicatorOffset = (pullOffset.value * 0.75f - with(density) { 34.dp.toPx() })
                            .coerceAtLeast(0f)
                        IntOffset(0, indicatorOffset.roundToInt())
                    }
                    .alpha(progress.coerceAtLeast(0.2f))
                    .background(
                        color = Color(0xFF09090B),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isThresholdReached) Color(0xFF38BDF8) else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(13.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Syncing Catalog...",
                            color = Color(0xFFF1F5F9),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        val rotation = progress * 180f
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = "Drag to refresh",
                            tint = if (isThresholdReached) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            modifier = Modifier
                                .size(13.dp)
                                .rotate(if (isThresholdReached) 180f else rotation)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isThresholdReached) "Release to refresh" else "Drag to refresh",
                            color = if (isThresholdReached) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
