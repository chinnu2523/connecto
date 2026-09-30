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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.glassmorphicCard(
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0x2B1E2538),
    borderColor: Color = Color(0x33A29BFE),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.35f),
                backgroundColor.copy(alpha = 0.15f)
            )
        )
    )
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                borderColor,
                borderColor.copy(alpha = 0.05f)
            )
        ),
        shape = shape
    )

@Composable
fun Modifier.pressScaleEffect(
    onClick: () -> Unit,
    targetScale: Float = 0.94f
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
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
        .pointerInput(onClick) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    tryAwaitRelease()
                    isPressed = false
                    onClick()
                }
            )
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

