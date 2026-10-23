package com.example.connecto.ui.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ConnectoCardVariant {
    SURFACE,
    ELEVATED,
    OUTLINED
}

/**
 * Reusable Connecto Card component adhering to the Centralized Design System.
 * In Frosted Glass theme, renders with high-end translucent acrylic, dual-tone specular border,
 * and tactile spring physics.
 */
@Composable
fun ConnectoCard(
    modifier: Modifier = Modifier,
    variant: ConnectoCardVariant = ConnectoCardVariant.SURFACE,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ConnectoRadius.md),
    contentPadding: Dp = ConnectoSpacing.md,
    border: BorderStroke? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalConnectoColors.current

    if (colors.isGlass) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val haptic = LocalHapticFeedback.current

        val scale by animateFloatAsState(
            targetValue = if (onClick != null && isPressed) 0.965f else 1.0f,
            animationSpec = spring(stiffness = 500f, dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "glassCardScale"
        )

        val topTint = if (colors.isDark) Color.White.copy(alpha = if (variant == ConnectoCardVariant.ELEVATED) 0.15f else 0.11f) else Color.White.copy(alpha = 0.70f)
        val bottomTint = if (colors.isDark) colors.surface.copy(alpha = if (variant == ConnectoCardVariant.ELEVATED) 0.10f else 0.06f) else Color.White.copy(alpha = 0.35f)

        val cardBgBrush = Brush.verticalGradient(
            colors = listOf(topTint, bottomTint)
        )

        val cardBorderBrush = border?.brush ?: Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isPressed) 0.85f else (if (colors.isDark) 0.70f else 0.85f)),
                colors.primary.copy(alpha = if (isPressed) 0.70f else 0.45f),
                colors.borderSubtle.copy(alpha = 0.08f)
            ),
            start = androidx.compose.ui.geometry.Offset.Zero,
            end = androidx.compose.ui.geometry.Offset(350f, 350f)
        )

        Box(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = if (variant == ConnectoCardVariant.ELEVATED) 8.dp else 5.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.40f else 0.10f),
                    spotColor = colors.primary.copy(alpha = if (colors.isDark) 0.22f else 0.08f)
                )
                .clip(shape)
                .background(cardBgBrush)
                .border(
                    width = border?.width ?: 1.dp,
                    brush = cardBorderBrush,
                    shape = shape
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                            onClick()
                        }
                    } else Modifier
                )
                .padding(contentPadding)
        ) {
            content()
        }
        return
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    val containerColor = when (variant) {
        ConnectoCardVariant.SURFACE -> colors.surface
        ConnectoCardVariant.ELEVATED -> colors.surfaceVariant
        ConnectoCardVariant.OUTLINED -> colors.surface
    }

    val shadowElevation = when {
        isPressed -> 2.dp
        variant == ConnectoCardVariant.ELEVATED -> 8.dp
        variant == ConnectoCardVariant.SURFACE -> 4.dp
        else -> 0.dp
    }

    val effectiveBorderBrush = border?.brush ?: Brush.verticalGradient(
        colors = listOf(
            if (colors.isDark) Color.White.copy(alpha = if (variant == ConnectoCardVariant.ELEVATED) 0.22f else 0.14f) else Color.White.copy(alpha = 0.85f),
            if (colors.isDark) colors.border.copy(alpha = 0.60f) else colors.borderSubtle,
            if (colors.isDark) colors.borderSubtle.copy(alpha = 0.20f) else colors.borderSubtle.copy(alpha = 0.05f)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.50f else 0.15f),
                spotColor = colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.06f),
                clip = false
            )
            .clip(shape)
            .background(containerColor)
            .border(
                width = border?.width ?: 1.dp,
                brush = effectiveBorderBrush,
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        try { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                        onClick()
                    }
                } else Modifier
            )
            .padding(contentPadding)
    ) {
        content()
    }
}
