package com.example.connecto.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * LiquidGlassTransparencyBar - VisionOS Spatial Liquid Glass Concept
 *
 * Implements Concept 01: Spatial Liquid Glass Capsule featuring:
 * - Ultra-refractive floating glass capsule with curved ends
 * - Optical transparency checkerboard verifying genuine alpha density
 * - Smooth liquid gradient fill transitioning from 0% clear to solid obsidian/cyan glass
 * - Precision milestone tick marks at 25%, 50%, 75%
 * - Crystal Lens Orb thumb with dual specular caustics and 3D spherical refraction
 * - Floating live HUD percentage tooltip bubble with spatial glow during drag
 * - Milestone haptic feedback detents on snap points
 */
@Composable
fun LiquidGlassTransparencyBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    var isDragging by remember { mutableStateOf(false) }
    var lastHapticMilestone by remember { mutableIntStateOf(-1) }

    // Smooth spring animation when tapping presets; 0-latency instant response when actively dragging
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(0.0f, 1.0f),
        animationSpec = if (isDragging) snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "transparencyAnimation"
    )

    val displayValue = if (isDragging) value.coerceIn(0.0f, 1.0f) else animatedValue.coerceIn(0.0f, 1.0f)
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "thumbScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackShape = CircleShape // Pure capsule shape matching VisionOS standard

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp), // Height accommodates 44dp capsule + floating HUD pill
        contentAlignment = Alignment.BottomCenter
    ) {
        val density = LocalDensity.current
        val widthPx = constraints.maxWidth.toFloat()
        val trackHeightDp = 44.dp
        val thumbDiameterDp = 36.dp
        val trackHeightPx = with(density) { trackHeightDp.toPx() }
        val thumbDiameterPx = with(density) { thumbDiameterDp.toPx() }
        val thumbRadiusPx = thumbDiameterPx / 2f
        val sidePaddingPx = with(density) { 4.dp.toPx() }

        val travelPx = (widthPx - thumbDiameterPx - (sidePaddingPx * 2)).coerceAtLeast(1f)
        val thumbOffsetPx = sidePaddingPx + (displayValue * travelPx)

        // Haptic feedback check for milestones (0%, 25%, 50%, 75%, 100%)
        fun checkMilestoneHaptic(ratio: Float) {
            val milestoneIndex = when {
                ratio <= 0.02f -> 0
                kotlin.math.abs(ratio - 0.25f) <= 0.025f -> 1
                kotlin.math.abs(ratio - 0.50f) <= 0.025f -> 2
                kotlin.math.abs(ratio - 0.75f) <= 0.025f -> 3
                ratio >= 0.98f -> 4
                else -> -1
            }
            if (milestoneIndex != -1 && milestoneIndex != lastHapticMilestone) {
                lastHapticMilestone = milestoneIndex
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } else if (milestoneIndex == -1) {
                lastHapticMilestone = -1
            }
        }

        // Floating Spatial HUD Tooltip Pill during drag
        AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn(spring(stiffness = Spring.StiffnessHigh)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessHigh)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset {
                    IntOffset(
                        x = (thumbOffsetPx + thumbRadiusPx - with(density) { 26.dp.toPx() }).roundToInt()
                            .coerceIn(0, (widthPx - with(density) { 52.dp.toPx() }).roundToInt().coerceAtLeast(0)),
                        y = 0
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xEE1E293B),
                                Color(0xFA0F172A)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.60f), RoundedCornerShape(12.dp))
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFF06B6D4))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(displayValue * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE0F2FE)
                )
            }
        }

        // Main Track & Interactive Touch Target
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeightDp)
                .clip(trackShape)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isDragging = true
                        val tapRatio = ((down.position.x - sidePaddingPx - thumbRadiusPx) / travelPx).coerceIn(0.0f, 1.0f)
                        currentOnValueChange(tapRatio)
                        checkMilestoneHaptic(tapRatio)

                        val pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) break
                            change.consume()
                            val dragRatio = ((change.position.x - sidePaddingPx - thumbRadiusPx) / travelPx).coerceIn(0.0f, 1.0f)
                            currentOnValueChange(dragRatio)
                            checkMilestoneHaptic(dragRatio)
                        }
                        isDragging = false
                    }
                }
        ) {
            // Layer 1: Visual Optical Canvas (Checkerboard, Density Gradient, Caustics, Milestones)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(trackShape)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // 1A. Fine optical checkerboard grid
                val tileSize = 7.dp.toPx()
                val cols = (canvasWidth / tileSize).toInt() + 1
                val rows = (canvasHeight / tileSize).toInt() + 1

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val isLight = (r + c) % 2 == 0
                        val tileColor = if (isLight) Color(0x1AFFFFFF) else Color(0x07FFFFFF)
                        drawRect(
                            color = tileColor,
                            topLeft = Offset(c * tileSize, r * tileSize),
                            size = Size(tileSize, tileSize)
                        )
                    }
                }

                // 1B. Spatial Liquid Gradient Fill (0% Pure Clear -> 50% Translucent Cyan -> 100% Solid)
                val densityGradient = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x05FFFFFF),
                        Color(0x3306B6D4), // Cyan-500 refractive glow
                        Color(0xD906B6D4), // 85% Solid Cyan
                        Color(0xF00891B2)  // Solid Cyan/Deep Azure
                    ),
                    startX = 0f,
                    endX = canvasWidth
                )
                drawRect(
                    brush = densityGradient,
                    size = size
                )

                // 1C. Active Illuminated Glass Core (up to thumb)
                val activeEndPx = (thumbOffsetPx + thumbRadiusPx).coerceIn(0f, canvasWidth)
                val activeCoreGlow = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x10FFFFFF),
                        Color(0x5538BDF8)
                    ),
                    startX = 0f,
                    endX = activeEndPx
                )
                drawRect(
                    brush = activeCoreGlow,
                    size = Size(activeEndPx, canvasHeight)
                )

                // 1D. Precision Milestone Tick Marks (25%, 50%, 75%)
                val milestones = listOf(0.25f, 0.50f, 0.75f)
                val tickHeight = 12.dp.toPx()
                val tickTop = (canvasHeight - tickHeight) / 2f
                val tickStroke = 1.5.dp.toPx()

                milestones.forEach { ratio ->
                    val tickX = sidePaddingPx + thumbRadiusPx + (ratio * travelPx)
                    drawLine(
                        color = Color.White.copy(alpha = 0.40f),
                        start = Offset(tickX, tickTop),
                        end = Offset(tickX, tickTop + tickHeight),
                        strokeWidth = tickStroke,
                        cap = StrokeCap.Round
                    )
                }

                // 1E. Top Specular Glaze Reflection (Upper half glass sheen)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = canvasHeight * 0.50f
                    ),
                    size = Size(canvasWidth, canvasHeight * 0.50f)
                )

                // 1F. Bottom Recessed Shadow (adds glass chamber depth)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.30f)
                        ),
                        startY = canvasHeight * 0.75f,
                        endY = canvasHeight
                    ),
                    topLeft = Offset(0f, canvasHeight * 0.75f),
                    size = Size(canvasWidth, canvasHeight * 0.25f)
                )
            }

            // Outer Specular Glass Rim Border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.65f), // Top specular reflection
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.10f)  // Bottom edge
                            )
                        ),
                        shape = trackShape
                    )
            )

            // Layer 2: VisionOS Floating Crystal Lens Orb (Thumb)
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = thumbOffsetPx.roundToInt(), y = ((trackHeightPx - thumbDiameterPx) / 2f).roundToInt()) }
                    .size(thumbDiameterDp)
                    .scale(thumbScale)
                    .shadow(
                        elevation = if (isDragging) 12.dp else 6.dp,
                        shape = CircleShape,
                        spotColor = Color.Black.copy(alpha = 0.70f),
                        ambientColor = Color(0xFF06B6D4).copy(alpha = 0.50f)
                    )
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFBAE6FD),
                                Color(0xFF38BDF8).copy(alpha = 0.8f)
                            ),
                            start = Offset.Zero,
                            end = Offset(thumbDiameterPx, thumbDiameterPx)
                        ),
                        shape = CircleShape
                    )
            ) {
                // High-fidelity Crystal Sphere Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // 2A. Radial Refraction Gradient (VisionOS crystal orb)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFA5F3FC), // Soft cyan tint
                                Color(0xFF0891B2), // Deep cyan core
                                Color(0xFF0E7490)
                            ),
                            center = Offset(size.width * 0.35f, size.height * 0.35f),
                            radius = radius * 1.3f
                        ),
                        radius = radius,
                        center = center
                    )

                    // 2B. Primary Specular Pinpoint Caustic (top-left glint)
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(size.width * 0.35f, size.height * 0.35f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f),
                        radius = 4.5.dp.toPx(),
                        center = Offset(size.width * 0.35f, size.height * 0.35f)
                    )

                    // 2C. Secondary Lower-Right Internal Refraction Crescent
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x66E0F2FE),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.70f, size.height * 0.70f),
                            radius = radius * 0.6f
                        ),
                        radius = radius * 0.5f,
                        center = Offset(size.width * 0.70f, size.height * 0.70f)
                    )
                }
            }
        }
    }
}
