package com.example.connecto.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.connecto.ui.designsystem.ConnectoTheme

@Composable
fun Modifier.glassmorphicCard(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = ConnectoTheme.colors.surface,
    borderColor: Color = ConnectoTheme.colors.primary,
    borderWidth: Dp = 1.dp
): Modifier {
    val colors = ConnectoTheme.colors
    val isDark = colors.isDark
    val isPureBlack = colors.background == Color(0xFF000000)

    val glareColor = when {
        isPureBlack -> Color.White.copy(alpha = 0.55f)
        isDark -> Color.White.copy(alpha = 0.70f)
        else -> Color.White.copy(alpha = 0.90f)
    }
    val topTint = when {
        isPureBlack -> Color.White.copy(alpha = 0.08f)
        isDark -> Color.White.copy(alpha = 0.12f)
        else -> Color.White.copy(alpha = 0.70f)
    }
    val bottomTint = when {
        isPureBlack -> Color.White.copy(alpha = 0.03f)
        isDark -> backgroundColor.copy(alpha = 0.07f)
        else -> Color.White.copy(alpha = 0.35f)
    }

    val borderColors = when {
        isPureBlack -> listOf(
            glareColor,
            Color.White.copy(alpha = 0.22f),
            Color.White.copy(alpha = 0.06f)
        )
        else -> listOf(
            glareColor,
            borderColor.copy(alpha = if (isDark) 0.45f else 0.60f),
            borderColor.copy(alpha = 0.08f)
        )
    }

    return this
        .shadow(
            elevation = if (isPureBlack) 0.dp else 6.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = if (isDark) 0.40f else 0.10f),
            spotColor = if (isPureBlack) Color.Transparent else borderColor.copy(alpha = if (isDark) 0.22f else 0.08f)
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    topTint,
                    bottomTint
                )
            )
        )
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = borderColors,
                start = Offset.Zero,
                end = Offset(300f, 300f)
            ),
            shape = shape
        )
}

@Composable
fun Modifier.pressScaleEffect(
    onClick: () -> Unit,
    targetScale: Float = 0.94f
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 420f),
        label = "pressScale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}

@Composable
fun Modifier.breathingPulse(
    minScale: Float = 0.96f,
    maxScale: Float = 1.04f,
    durationMillis: Int = 1800
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "breathingPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun Modifier.animatedGlowBorder(
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.5.dp
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "glowTransition")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderGlow"
    )

    val brush = Brush.linearGradient(
        colors = listOf(ElectricViolet, CyanGlow, NeonPink, ElectricViolet),
        start = Offset(animOffset, 0f),
        end = Offset(animOffset + 400f, 400f)
    )

    return this.border(
        width = borderWidth,
        brush = brush,
        shape = shape
    )
}

@Composable
fun Modifier.rotatingGlowHalo(
    shape: Shape = CircleShape,
    borderWidth: Dp = 2.dp,
    glowColors: List<Color> = listOf(ElectricViolet, CyanGlow, NeonPink, ElectricViolet),
    durationMillis: Int = 3000
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "rotatingGlowHalo")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloAngle"
    )

    // Only the border gradient rotates — content stays upright
    val brush = Brush.sweepGradient(colors = glowColors)

    return this.border(
        width = borderWidth,
        brush = brush,
        shape = shape
    )
}

@Composable
fun AnimatedEqualizerBars(
    color: Color = CyanGlow,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    maxHeight: Dp = 16.dp,
    barWidth: Dp = 3.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizerBars")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq1"
    )

    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(360, delayMillis = 80, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq2"
    )

    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, delayMillis = 160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq3"
    )

    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(390, delayMillis = 240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq4"
    )

    val heights = listOf(bar1, bar2, bar3, bar4)

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val scale = heights[i % heights.size]
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(maxHeight * scale)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

/**
 * Animated moving specular reflection sweep across glass surfaces.
 * Creates a fluid, diagonal prismatic gleam that tracks across cards and containers.
 */
@Composable
fun Modifier.glassSpecularSweep(
    shape: Shape = RoundedCornerShape(16.dp),
    durationMillis: Int = 3600,
    sweepColor: Color = Color(0x3838BDF8)
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "glassSpecularSweep")
    val sweepOffset by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "specularSweepOffset"
    )

    return this.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                sweepColor.copy(alpha = 0.04f),
                sweepColor.copy(alpha = 0.28f),
                sweepColor.copy(alpha = 0.04f),
                Color.Transparent
            ),
            start = Offset(sweepOffset, 0f),
            end = Offset(sweepOffset + 320f, size.height)
        )
        drawRect(brush = brush)
    }.clip(shape)
}

/**
 * Ambient background mesh with floating luminous orbs (Ice Cyan + Deep Indigo).
 * Provides optical depth and background luminance behind translucent glass surfaces.
 */
@Composable
fun Modifier.glassCanvasMesh(
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this
    val colors = ConnectoTheme.colors
    if (colors.background == Color(0xFF000000)) {
        return this.background(Color(0xFF000000))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glassMeshAmbient")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glassMeshPulse"
    )

    return this.drawBehind {
        // Deep obsidian crystal backdrop
        drawRect(Color(0xFF090D16))

        // Ambient Cyan Glass Orb (Top Right)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x3838BDF8),
                    Color(0x150284C7),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.82f, size.height * 0.12f),
                radius = size.width * 0.65f * pulse
            ),
            radius = size.width * 0.65f * pulse,
            center = Offset(size.width * 0.82f, size.height * 0.12f)
        )

        // Ambient Indigo Glass Orb (Bottom Left)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x2D6366F1),
                    Color(0x12312E81),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.15f, size.height * 0.82f),
                radius = size.width * 0.70f / pulse
            ),
            radius = size.width * 0.70f / pulse,
            center = Offset(size.width * 0.15f, size.height * 0.82f)
        )

        // Subtle Aurora Prism Center Accent
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x1E22D3EE),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.45f),
                radius = size.width * 0.50f
            ),
            radius = size.width * 0.50f,
            center = Offset(size.width * 0.5f, size.height * 0.45f)
        )
    }
}

/**
 * High-End Frosted Glass Container Card.
 * Multi-layer translucent acrylic with dual-tone specular border and optional reflection sweep.
 */
@Composable
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = ConnectoTheme.colors.surface,
    borderColor: Color = ConnectoTheme.colors.primary,
    borderWidth: Dp = 1.dp,
    hasShimmer: Boolean = false
): Modifier {
    val colors = ConnectoTheme.colors
    val isDark = colors.isDark
    val isPureBlack = colors.background == Color(0xFF000000)

    val glareColor = when {
        isPureBlack -> Color.White.copy(alpha = 0.55f)
        isDark -> Color.White.copy(alpha = 0.70f)
        else -> Color.White.copy(alpha = 0.90f)
    }
    val topTint = when {
        isPureBlack -> Color.White.copy(alpha = 0.08f)
        isDark -> Color.White.copy(alpha = 0.12f)
        else -> Color.White.copy(alpha = 0.70f)
    }
    val bottomTint = when {
        isPureBlack -> Color.White.copy(alpha = 0.03f)
        isDark -> backgroundColor.copy(alpha = 0.07f)
        else -> Color.White.copy(alpha = 0.35f)
    }

    val borderColors = when {
        isPureBlack -> listOf(
            glareColor,
            Color.White.copy(alpha = 0.22f),
            Color.White.copy(alpha = 0.06f)
        )
        else -> listOf(
            glareColor,
            borderColor.copy(alpha = if (isDark) 0.45f else 0.60f),
            borderColor.copy(alpha = 0.08f)
        )
    }

    val base = this
        .shadow(
            elevation = if (isPureBlack) 0.dp else 6.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = if (isDark) 0.40f else 0.10f),
            spotColor = if (isPureBlack) Color.Transparent else borderColor.copy(alpha = if (isDark) 0.22f else 0.08f)
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    topTint,
                    bottomTint
                )
            )
        )
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = borderColors,
                start = Offset.Zero,
                end = Offset(300f, 300f)
            ),
            shape = shape
        )

    return if (hasShimmer) {
        base.glassSpecularSweep(shape = shape, sweepColor = if (isPureBlack) Color.White.copy(alpha = 0.15f) else borderColor)
    } else {
        base
    }
}

/**
 * Ultra-Clear Liquid Glass Canvas Mesh.
 * Fluid oceanic gradients, caustic water orb refractions, and theme-adaptive liquid reflections.
 */
@Composable
fun Modifier.liquidGlassCanvasMesh(
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this
    val colors = ConnectoTheme.colors
    val isDark = colors.isDark

    // Pure Black OLED theme: STRICTLY 100% 0-nit pitch black (#000000). Zero orbs, zero gradients.
    if (colors.background == Color(0xFF000000)) {
        return this.background(Color(0xFF000000))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "liquidMeshAmbient")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liquidMeshPulse"
    )

    return this.drawBehind {
        // Base canvas color according to the active theme palette
        drawRect(colors.background)

        val primaryGlow = colors.primary.copy(alpha = if (isDark) 0.26f else 0.14f)
        val infoGlow = colors.info.copy(alpha = if (isDark) 0.16f else 0.09f)
        val surfaceGlow = colors.surfaceVariant.copy(alpha = if (isDark) 0.20f else 0.11f)

        // Top right fluid caustic orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryGlow,
                    infoGlow,
                    Color.Transparent
                ),
                center = Offset(size.width * 0.82f, size.height * 0.12f),
                radius = size.width * 0.68f * pulse
            ),
            radius = size.width * 0.68f * pulse,
            center = Offset(size.width * 0.82f, size.height * 0.12f)
        )

        // Bottom left deep fluid orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    surfaceGlow,
                    infoGlow,
                    Color.Transparent
                ),
                center = Offset(size.width * 0.15f, size.height * 0.82f),
                radius = size.width * 0.72f / pulse
            ),
            radius = size.width * 0.72f / pulse,
            center = Offset(size.width * 0.15f, size.height * 0.82f)
        )

        // Central caustic glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryGlow.copy(alpha = if (isDark) 0.15f else 0.08f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.45f),
                radius = size.width * 0.52f
            ),
            radius = size.width * 0.52f,
            center = Offset(size.width * 0.5f, size.height * 0.45f)
        )
    }
}

/**
 * Transparent Liquid Glass Card (iPhone Ultra-Thin Material).
 * Crystal-clear hydro-acrylic body with fluid transparency, hairline specular reflection, and floating depth.
 */
@Composable
fun Modifier.liquidGlassCard(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = ConnectoTheme.colors.surface,
    borderColor: Color = ConnectoTheme.colors.primary,
    borderWidth: Dp = 1.dp
): Modifier {
    val colors = ConnectoTheme.colors
    val isDark = colors.isDark
    val isPureBlack = colors.background == Color(0xFF000000)

    val glareColor = when {
        isPureBlack -> Color.White.copy(alpha = 0.55f)
        isDark -> Color.White.copy(alpha = 0.70f)
        else -> Color.White.copy(alpha = 0.90f)
    }
    val topTint = when {
        isPureBlack -> Color.White.copy(alpha = 0.08f)
        isDark -> Color.White.copy(alpha = 0.12f)
        else -> Color.White.copy(alpha = 0.70f)
    }
    val bottomTint = when {
        isPureBlack -> Color.White.copy(alpha = 0.03f)
        isDark -> backgroundColor.copy(alpha = 0.07f)
        else -> Color.White.copy(alpha = 0.35f)
    }

    val borderColors = when {
        isPureBlack -> listOf(
            glareColor,                      // Specular glare reflection on top rim
            Color.White.copy(alpha = 0.22f), // Crystal translucent rim
            Color.White.copy(alpha = 0.06f)  // Subtle translucent bottom rim
        )
        else -> listOf(
            glareColor,                      // Specular glare reflection on top rim
            borderColor.copy(alpha = if (isDark) 0.45f else 0.60f), // Themed rim glow
            borderColor.copy(alpha = 0.08f)  // Translucent subtle bottom edge
        )
    }

    return this
        .shadow(
            elevation = if (isPureBlack) 0.dp else 6.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = if (isDark) 0.40f else 0.10f),
            spotColor = if (isPureBlack) Color.Transparent else borderColor.copy(alpha = if (isDark) 0.22f else 0.08f)
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    topTint,
                    bottomTint
                )
            )
        )
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = borderColors,
                start = Offset.Zero,
                end = Offset(300f, 300f)
            ),
            shape = shape
        )
}

/**
 * Tactile Glass Button with spring scale compression, vertical translation, and specular edge bloom.
 */
@Composable
fun Modifier.glassInteractiveButton(
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(12.dp),
    containerColor: Color = Color(0xFF1E293B),
    borderColor: Color = Color(0xFF38BDF8),
    targetScale: Float = 0.94f
): Modifier {
    val colors = ConnectoTheme.colors
    val isPureBlack = colors.background == Color(0xFF000000)
    val effectiveContainer = if (isPureBlack) Color(0x1AFFFFFF) else containerColor
    val effectiveBorder = if (isPureBlack) Color(0x55FFFFFF) else borderColor

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 520f),
        label = "glassBtnScale"
    )
    val translateY by animateFloatAsState(
        targetValue = if (isPressed) 2f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 520f),
        label = "glassBtnTranslate"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 0.45f,
        animationSpec = tween(150),
        label = "glassBtnGlow"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.translationY = translateY
        }
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                listOf(
                    effectiveContainer.copy(alpha = if (isPressed) 0.94f else 0.78f),
                    effectiveContainer.copy(alpha = if (isPressed) 0.78f else 0.52f)
                )
            )
        )
        .border(
            width = if (isPressed) 1.5.dp else 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    effectiveBorder.copy(alpha = glowAlpha),
                    effectiveBorder.copy(alpha = glowAlpha * 0.35f)
                )
            ),
            shape = shape
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
            onClick()
        }
}

/**
 * Translucent Glass Chat Bubble styling for direct messages and channels.
 */
@Composable
fun Modifier.glassBubble(
    isMe: Boolean,
    shape: Shape,
    isPressed: Boolean = false,
    isLiquid: Boolean = true
): Modifier {
    val colors = ConnectoTheme.colors
    val isDark = colors.isDark
    val isPureBlack = colors.background == Color(0xFF000000)

    val bgBrush = when {
        isPureBlack -> {
            if (isMe) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.09f)
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            }
        }
        isLiquid -> {
            if (isMe) {
                Brush.verticalGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = if (isDark) 0.30f else 0.42f),
                        colors.primary.copy(alpha = if (isDark) 0.14f else 0.22f)
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.14f else 0.65f),
                        colors.surface.copy(alpha = if (isDark) 0.08f else 0.32f)
                    )
                )
            }
        }
        else -> {
            if (isMe) {
                Brush.verticalGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.85f),
                        colors.primary.copy(alpha = 0.65f)
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        colors.surfaceVariant.copy(alpha = 0.85f),
                        colors.surface.copy(alpha = 0.65f)
                    )
                )
            }
        }
    }

    val borderBrush = when {
        isPureBlack -> {
            if (isMe) {
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.70f),
                        Color.White.copy(alpha = 0.30f),
                        Color.White.copy(alpha = 0.08f)
                    ),
                    start = Offset.Zero,
                    end = Offset(240f, 240f)
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.05f)
                    ),
                    start = Offset.Zero,
                    end = Offset(240f, 240f)
                )
            }
        }
        isMe -> {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.85f),
                    colors.primary.copy(alpha = 0.60f),
                    colors.primary.copy(alpha = 0.15f)
                ),
                start = Offset.Zero,
                end = Offset(240f, 240f)
            )
        }
        else -> {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isDark) 0.55f else 0.75f),
                    colors.border.copy(alpha = 0.40f),
                    colors.borderSubtle.copy(alpha = 0.10f)
                ),
                start = Offset.Zero,
                end = Offset(240f, 240f)
            )
        }
    }

    return this
        .shadow(
            elevation = if (isPureBlack) 0.dp else 3.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = if (isDark) 0.35f else 0.08f),
            spotColor = if (isPureBlack) Color.Transparent else if (isMe) colors.primary.copy(alpha = 0.20f) else Color.Transparent
        )
        .clip(shape)
        .background(bgBrush)
        .border(
            width = if (isPressed) 1.5.dp else 1.dp,
            brush = borderBrush,
            shape = shape
        )
}

