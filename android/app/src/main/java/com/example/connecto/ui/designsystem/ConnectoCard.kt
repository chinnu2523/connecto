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

        val cardBgBrush = Brush.verticalGradient(
            colors = listOf(
                colors.surface.copy(alpha = if (variant == ConnectoCardVariant.ELEVATED) 0.88f else 0.78f),
                colors.surfaceVariant.copy(alpha = if (variant == ConnectoCardVariant.ELEVATED) 0.72f else 0.52f)
            )
        )

        val cardBorderBrush = border?.brush ?: Brush.linearGradient(
            colors = listOf(
                colors.primary.copy(alpha = if (isPressed) 0.85f else 0.50f),
                colors.border.copy(alpha = 0.25f),
                colors.borderSubtle.copy(alpha = 0.08f)
            )
        )

        Box(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
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

    val containerColor = when (variant) {
        ConnectoCardVariant.SURFACE -> colors.surface
        ConnectoCardVariant.ELEVATED -> colors.surface
        ConnectoCardVariant.OUTLINED -> colors.surface
    }

    val elevation = when (variant) {
        ConnectoCardVariant.ELEVATED -> ConnectoElevation.low
        ConnectoCardVariant.SURFACE,
        ConnectoCardVariant.OUTLINED -> ConnectoElevation.none
    }

    val effectiveBorder = border ?: when (variant) {
        ConnectoCardVariant.OUTLINED -> BorderStroke(1.dp, colors.border)
        ConnectoCardVariant.SURFACE,
        ConnectoCardVariant.ELEVATED -> BorderStroke(1.dp, colors.borderSubtle)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = colors.textPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = effectiveBorder
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}
