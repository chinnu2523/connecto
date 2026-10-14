package com.example.connecto.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Classic & Aesthetic Bespoke Icon System for Connecto.
 * Distinct, handcrafted vector geometry representing:
 * - Home: Modernist Architectural Sanctuary with Portico & Hearth
 * - Messages: Fluid Ergonomic Dialogue Squircle with Dual Discourse Bars
 * - Channels: Interlocking Frequency Lattice with Diamond Intersections
 * - Calls: Vintage-Luxury Acoustic Handset with Resonance Wave Arcs
 * - Profile: Timeless Cameo Signet Medallion with Sculpted Silhouette
 */
@Composable
fun ClassicAestheticTabIcon(
    tab: ConnectoTab,
    isSelected: Boolean,
    primaryColor: Color,
    inactiveColor: Color,
    avatarUrl: String? = null,
    userInitial: String = "U",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "classicAestheticAura")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (tab) {
            ConnectoTab.HOME -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val strokeWidth = 1.75.dp.toPx()
                    val tint = if (isSelected) primaryColor else inactiveColor

                    // Roof & Sanctuary Pavilion Structure
                    val roofPath = Path().apply {
                        moveTo(w * 0.50f, h * 0.15f) // Apex
                        lineTo(w * 0.88f, h * 0.44f) // Right eave cantilever
                        lineTo(w * 0.77f, h * 0.44f) // Right wall join
                        lineTo(w * 0.77f, h * 0.81f) // Right wall base
                        lineTo(w * 0.23f, h * 0.81f) // Left wall base
                        lineTo(w * 0.23f, h * 0.44f) // Left wall join
                        lineTo(w * 0.12f, h * 0.44f) // Left eave cantilever
                        close()
                    }

                    if (isSelected) {
                        // Ambient luminous gradient fill
                        drawPath(
                            path = roofPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.32f),
                                    primaryColor.copy(alpha = 0.06f)
                                )
                            )
                        )
                    }

                    // Pavilion Contour
                    drawPath(
                        path = roofPath,
                        color = tint,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Architectural Floating Base Plinth
                    drawLine(
                        color = tint,
                        start = Offset(w * 0.14f, h * 0.88f),
                        end = Offset(w * 0.86f, h * 0.88f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )

                    // Arched Portal Threshold
                    val archPath = Path().apply {
                        moveTo(w * 0.39f, h * 0.81f)
                        lineTo(w * 0.39f, h * 0.60f)
                        arcTo(
                            rect = Rect(
                                left = w * 0.39f,
                                top = h * 0.49f,
                                right = w * 0.61f,
                                bottom = h * 0.71f
                            ),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 180f,
                            forceMoveTo = false
                        )
                        lineTo(w * 0.61f, h * 0.81f)
                    }

                    drawPath(
                        path = archPath,
                        color = tint,
                        style = Stroke(
                            width = strokeWidth * 0.88f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    if (isSelected) {
                        // Radiant Hearth Pip
                        drawCircle(
                            color = Color.White,
                            radius = 1.6.dp.toPx(),
                            center = Offset(w * 0.50f, h * 0.64f)
                        )
                    }
                }
            }

            ConnectoTab.MESSAGES -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val strokeWidth = 1.75.dp.toPx()
                    val tint = if (isSelected) primaryColor else inactiveColor

                    // Aesthetic dialogue squircle with fluid curved tail
                    val bubblePath = Path().apply {
                        moveTo(w * 0.30f, h * 0.15f)
                        lineTo(w * 0.72f, h * 0.15f)
                        arcTo(
                            rect = Rect(w * 0.60f, h * 0.15f, w * 0.88f, h * 0.42f),
                            startAngleDegrees = 270f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(w * 0.88f, h * 0.50f)
                        arcTo(
                            rect = Rect(w * 0.60f, h * 0.45f, w * 0.88f, h * 0.72f),
                            startAngleDegrees = 0f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(w * 0.38f, h * 0.72f)
                        // Elegant curved tail
                        quadraticTo(
                            w * 0.24f, h * 0.76f,
                            w * 0.14f, h * 0.88f
                        )
                        quadraticTo(
                            w * 0.19f, h * 0.75f,
                            w * 0.16f, h * 0.62f
                        )
                        lineTo(w * 0.16f, h * 0.32f)
                        arcTo(
                            rect = Rect(w * 0.16f, h * 0.15f, w * 0.42f, h * 0.42f),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        close()
                    }

                    if (isSelected) {
                        drawPath(
                            path = bubblePath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    primaryColor.copy(alpha = 0.08f)
                                )
                            )
                        )
                    }

                    drawPath(
                        path = bubblePath,
                        color = tint,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Dual discourse horizontal bars
                    val dashColor = if (isSelected) Color.White.copy(alpha = 0.95f) else inactiveColor.copy(alpha = 0.75f)
                    drawLine(
                        color = dashColor,
                        start = Offset(w * 0.32f, h * 0.37f),
                        end = Offset(w * 0.72f, h * 0.37f),
                        strokeWidth = strokeWidth * 0.85f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = dashColor,
                        start = Offset(w * 0.32f, h * 0.51f),
                        end = Offset(w * 0.56f, h * 0.51f),
                        strokeWidth = strokeWidth * 0.85f,
                        cap = StrokeCap.Round
                    )
                }
            }

            ConnectoTab.CHANNELS -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val strokeWidth = 1.75.dp.toPx()
                    val tint = if (isSelected) primaryColor else inactiveColor

                    // Slanted vertical bars (sleek 12-degree incline)
                    val leftVStart = Offset(w * 0.42f, h * 0.15f)
                    val leftVEnd = Offset(w * 0.32f, h * 0.85f)
                    val rightVStart = Offset(w * 0.68f, h * 0.15f)
                    val rightVEnd = Offset(w * 0.58f, h * 0.85f)

                    // Horizontal sweeping bars
                    val topHStart = Offset(w * 0.16f, h * 0.37f)
                    val topHEnd = Offset(w * 0.84f, h * 0.37f)
                    val botHStart = Offset(w * 0.12f, h * 0.63f)
                    val botHEnd = Offset(w * 0.80f, h * 0.63f)

                    drawLine(color = tint, start = leftVStart, end = leftVEnd, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(color = tint, start = rightVStart, end = rightVEnd, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(color = tint, start = topHStart, end = topHEnd, strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(color = tint, start = botHStart, end = botHEnd, strokeWidth = strokeWidth, cap = StrokeCap.Round)

                    if (isSelected) {
                        // Refined diamond/jewel pips at the 4 lattice intersection points
                        val nodes = listOf(
                            Offset(w * 0.385f, h * 0.37f),
                            Offset(w * 0.645f, h * 0.37f),
                            Offset(w * 0.355f, h * 0.63f),
                            Offset(w * 0.615f, h * 0.63f)
                        )
                        for (node in nodes) {
                            drawCircle(
                                color = Color.White,
                                radius = 1.6.dp.toPx(),
                                center = node
                            )
                        }
                    }
                }
            }

            ConnectoTab.CALLS -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val strokeWidth = 1.75.dp.toPx()
                    val tint = if (isSelected) primaryColor else inactiveColor

                    // Vintage-luxury acoustic telephone handset
                    val handsetPath = Path().apply {
                        moveTo(w * 0.28f, h * 0.18f)
                        lineTo(w * 0.42f, h * 0.32f)
                        quadraticTo(w * 0.36f, h * 0.48f, w * 0.52f, h * 0.64f)
                        lineTo(w * 0.66f, h * 0.78f)
                        lineTo(w * 0.80f, h * 0.64f)
                        quadraticTo(w * 0.72f, h * 0.46f, w * 0.46f, h * 0.20f)
                        close()
                    }

                    if (isSelected) {
                        drawPath(
                            path = handsetPath,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    primaryColor.copy(alpha = 0.10f)
                                )
                            )
                        )
                    }

                    drawPath(
                        path = handsetPath,
                        color = tint,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Concentric acoustic wave arcs
                    val waveColor = if (isSelected) Color.White.copy(alpha = pulseAlpha) else inactiveColor.copy(alpha = 0.65f)
                    drawArc(
                        color = waveColor,
                        startAngle = -72f,
                        sweepAngle = 65f,
                        useCenter = false,
                        topLeft = Offset(w * 0.36f, h * 0.08f),
                        size = Size(w * 0.46f, h * 0.46f),
                        style = Stroke(width = strokeWidth * 0.88f, cap = StrokeCap.Round)
                    )

                    if (isSelected) {
                        drawArc(
                            color = primaryColor.copy(alpha = 0.80f),
                            startAngle = -72f,
                            sweepAngle = 65f,
                            useCenter = false,
                            topLeft = Offset(w * 0.46f, h * 0.01f),
                            size = Size(w * 0.58f, h * 0.58f),
                            style = Stroke(width = strokeWidth * 0.88f, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            ConnectoTab.PROFILE -> {
                var avatarFailed by remember(avatarUrl) { mutableStateOf(false) }

                if (!avatarUrl.isNullOrBlank() && !avatarFailed) {
                    // Profile with loaded avatar in aesthetic cameo bezel
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                color = if (isSelected) primaryColor else inactiveColor.copy(alpha = 0.40f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            onError = { avatarFailed = true },
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                } else {
                    // Handcrafted Classic Cameo Signet Medallion
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val strokeWidth = 1.75.dp.toPx()
                        val tint = if (isSelected) primaryColor else inactiveColor
                        val center = Offset(w * 0.50f, h * 0.50f)
                        val ringRadius = w * 0.43f

                        if (isSelected) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.28f),
                                        Color.Transparent
                                    ),
                                    radius = ringRadius
                                ),
                                radius = ringRadius,
                                center = center
                            )
                        }

                        // Cameo outer signet ring
                        drawCircle(
                            color = tint,
                            radius = ringRadius,
                            center = center,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )

                        // Cameo sculpted head oval
                        drawOval(
                            color = if (isSelected) Color.White else tint,
                            topLeft = Offset(w * 0.38f, h * 0.25f),
                            size = Size(w * 0.24f, h * 0.28f),
                            style = if (isSelected) Fill else Stroke(width = strokeWidth * 0.9f)
                        )

                        // Cameo draped shoulder silhouette
                        val shouldersPath = Path().apply {
                            moveTo(w * 0.24f, h * 0.80f)
                            quadraticTo(
                                w * 0.30f, h * 0.58f,
                                w * 0.50f, h * 0.58f
                            )
                            quadraticTo(
                                w * 0.70f, h * 0.58f,
                                w * 0.76f, h * 0.80f
                            )
                        }
                        drawPath(
                            path = shouldersPath,
                            color = if (isSelected) Color.White else tint,
                            style = Stroke(
                                width = strokeWidth * 0.9f,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }
        }
    }
}
