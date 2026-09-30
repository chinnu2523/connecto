package com.example.connecto.ui.graphics

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.connecto.ui.theme.CyanGlow
import com.example.connecto.ui.theme.ElectricViolet
import com.example.connecto.ui.theme.NeonPink
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Node Network Canvas Graphic for Onboarding Slide 1.
 * Renders interconnected glowing nodes with moving pulses along connection lines.
 */
@Composable
fun NodeNetworkGraphic(
    modifier: Modifier = Modifier,
    nodeColor: Color = ElectricViolet,
    lineColor: Color = CyanGlow,
    pulseColor: Color = NeonPink
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nodeAnimation")
    
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseProgress"
    )

    val nodeGlowProgress by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nodeGlow"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Define node relative positions
        val nodes = listOf(
            Offset(width * 0.5f, height * 0.5f),   // Center main
            Offset(width * 0.22f, height * 0.28f), // Top left
            Offset(width * 0.78f, height * 0.25f), // Top right
            Offset(width * 0.18f, height * 0.72f), // Bottom left
            Offset(width * 0.82f, height * 0.75f), // Bottom right
            Offset(width * 0.5f, height * 0.15f)   // Top center
        )

        // Connections between nodes (index pairs)
        val connections = listOf(
            Pair(0, 1), Pair(0, 2), Pair(0, 3), Pair(0, 4), Pair(0, 5),
            Pair(1, 5), Pair(2, 5), Pair(1, 3), Pair(2, 4)
        )

        // Draw connection lines
        connections.forEach { (startIdx, endIdx) ->
            val start = nodes[startIdx]
            val end = nodes[endIdx]

            drawLine(
                color = lineColor.copy(alpha = 0.35f),
                start = start,
                end = end,
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            )

            // Draw traveling signal pulse along lines
            val currentPulsePos = Offset(
                x = start.x + (end.x - start.x) * ((pulseProgress + (startIdx + endIdx) * 0.15f) % 1f),
                y = start.y + (end.y - start.y) * ((pulseProgress + (startIdx + endIdx) * 0.15f) % 1f)
            )

            drawCircle(
                color = pulseColor,
                radius = 6f,
                center = currentPulsePos
            )
            drawCircle(
                color = pulseColor.copy(alpha = 0.3f),
                radius = 12f,
                center = currentPulsePos
            )
        }

        // Draw Nodes
        nodes.forEachIndexed { index, pos ->
            val radiusScale = if (index == 0) 24f * nodeGlowProgress else 14f
            
            // Outer aura glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(nodeColor.copy(alpha = 0.6f), Color.Transparent),
                    center = pos,
                    radius = radiusScale * 2.5f
                ),
                radius = radiusScale * 2.5f,
                center = pos
            )

            // Core node
            drawCircle(
                color = if (index == 0) NeonPink else nodeColor,
                radius = radiusScale,
                center = pos
            )
            drawCircle(
                color = Color.White,
                radius = radiusScale * 0.4f,
                center = pos
            )
        }
    }
}

/**
 * Animated Scanning Radar Graphic for Onboarding Slide 2.
 * Renders rotating laser sweep and expanding signal rings.
 */
@Composable
fun RadarSweepGraphic(
    modifier: Modifier = Modifier,
    radarColor: Color = CyanGlow
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarAnimation")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val ringExpand by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringExpand"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.width.coerceAtMost(size.height) / 2f * 0.85f

        // Draw concentric radar background rings
        for (i in 1..3) {
            val r = maxRadius * (i / 3f)
            drawCircle(
                color = radarColor.copy(alpha = 0.2f),
                radius = r,
                center = center,
                style = Stroke(width = 2f)
            )
        }

        // Crosshairs
        drawLine(
            color = radarColor.copy(alpha = 0.15f),
            start = Offset(center.x - maxRadius, center.y),
            end = Offset(center.x + maxRadius, center.y),
            strokeWidth = 2f
        )
        drawLine(
            color = radarColor.copy(alpha = 0.15f),
            start = Offset(center.x, center.y - maxRadius),
            end = Offset(center.x, center.y + maxRadius),
            strokeWidth = 2f
        )

        // Expanding signal wave
        val expandR = maxRadius * ringExpand
        val ringAlpha = (1f - ringExpand).coerceIn(0f, 1f)
        drawCircle(
            color = radarColor.copy(alpha = ringAlpha * 0.5f),
            radius = expandR,
            center = center,
            style = Stroke(width = 4f)
        )

        // Rotating sweep cone
        rotate(degrees = rotationAngle, pivot = center) {
            val sweepBrush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    radarColor.copy(alpha = 0.05f),
                    radarColor.copy(alpha = 0.45f)
                ),
                center = center
            )
            drawCircle(
                brush = sweepBrush,
                radius = maxRadius,
                center = center
            )
            drawLine(
                color = radarColor,
                start = center,
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 4f
            )
        }

        // Radar target dots
        val targets = listOf(
            Offset(center.x + maxRadius * 0.4f, center.y - maxRadius * 0.3f),
            Offset(center.x - maxRadius * 0.5f, center.y + maxRadius * 0.2f),
            Offset(center.x + maxRadius * 0.2f, center.y + maxRadius * 0.5f)
        )

        targets.forEach { target ->
            drawCircle(
                color = NeonPink,
                radius = 7f,
                center = target
            )
            drawCircle(
                color = NeonPink.copy(alpha = 0.3f),
                radius = 16f,
                center = target
            )
        }

        // Center dot
        drawCircle(
            color = Color.White,
            radius = 8f,
            center = center
        )
    }
}

/**
 * Ambient Drifting Bubbles Particle Canvas for backgrounds.
 */
@Composable
fun FloatingBubblesGraphic(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubblesAnimation")

    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floatAnim"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val bubbleColors = listOf(
            ElectricViolet.copy(alpha = 0.18f),
            CyanGlow.copy(alpha = 0.15f),
            NeonPink.copy(alpha = 0.12f)
        )

        for (i in 0..5) {
            val offsetX = (width * (0.2f + (i * 0.15f))) + (sin((floatAnim + i).toDouble()).toFloat() * 30f)
            val offsetY = (height * (0.2f + (i * 0.12f))) + (cos((floatAnim + i * 0.5f).toDouble()).toFloat() * 40f)
            val radius = 80f + (i * 25f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(bubbleColors[i % bubbleColors.size], Color.Transparent),
                    center = Offset(offsetX, offsetY),
                    radius = radius
                ),
                radius = radius,
                center = Offset(offsetX, offsetY)
            )
        }
    }
}
