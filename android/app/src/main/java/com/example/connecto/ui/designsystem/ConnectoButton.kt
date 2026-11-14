package com.example.connecto.ui.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ConnectoButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINED,
    DESTRUCTIVE,
    TEXT
}

enum class ConnectoButtonSize {
    COMPACT, // 36dp height
    REGULAR, // 44dp height
    LARGE    // 52dp height
}

/**
 * Universal Connecto Button adhering to the 5 Canonical States:
 * 1. Default  - Balanced resting aesthetics, theme-compliant colors and subtle elevation.
 * 2. Hover    - Subtle lift (translateY -1.5dp, scale 1.02f) and border specular highlight.
 * 3. Loading  - Fluid CircularProgressIndicator without layout jitter; interaction safely gated.
 * 4. Disabled - Muted opacity (0.45f), suppressed shadows, disabled interaction.
 * 5. Error    - Tactile horizontal shake animation, crimson tint, error alert icon and error text.
 */
@Composable
fun ConnectoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ConnectoButtonVariant = ConnectoButtonVariant.PRIMARY,
    size: ConnectoButtonSize = ConnectoButtonSize.REGULAR,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isError: Boolean = false,
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val colors = LocalConnectoColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Error shake animation
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    (-8f) at 80
                    8f at 160
                    (-5f) at 240
                    5f at 320
                    0f at 400
                }
            )
        }
    }

    // Interactive scale following Default, Hover, Pressed, and Disabled states
    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 1.0f
            isPressed -> 0.96f
            isHovered && !isLoading && !isError -> 1.02f
            else -> 1.0f
        },
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.8f),
        label = "buttonScale"
    )

    // Interactive translation Y for hover lift and press dip
    val translateY by animateDpAsState(
        targetValue = when {
            !enabled -> 0.dp
            isPressed -> 1.dp
            isHovered && !isLoading && !isError -> (-1.5).dp
            else -> 0.dp
        },
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.8f),
        label = "buttonTranslateY"
    )

    val minHeight = when (size) {
        ConnectoButtonSize.COMPACT -> 36.dp
        ConnectoButtonSize.REGULAR -> 44.dp
        ConnectoButtonSize.LARGE -> 52.dp
    }

    val shape = RoundedCornerShape(10.dp) // Refined 10px ergonomic radius

    // Base Content Color (State 1: Default)
    val baseContentColor = when {
        colors.isGlass -> when (variant) {
            ConnectoButtonVariant.PRIMARY -> Color(0xFF031627)
            ConnectoButtonVariant.SECONDARY -> Color(0xFFF8FAFC)
            ConnectoButtonVariant.OUTLINED -> colors.primary
            ConnectoButtonVariant.DESTRUCTIVE -> colors.onError
            ConnectoButtonVariant.TEXT -> colors.primary
        }
        else -> when (variant) {
            ConnectoButtonVariant.PRIMARY -> if (colors.isDark) Color(0xFF0A0A0F) else Color.White
            ConnectoButtonVariant.SECONDARY -> if (colors.isDark) Color(0xFFF4F4F6) else Color(0xFF18181B)
            ConnectoButtonVariant.OUTLINED -> colors.primary
            ConnectoButtonVariant.DESTRUCTIVE -> colors.onError
            ConnectoButtonVariant.TEXT -> colors.primary
        }
    }

    // Base Container Color (State 1: Default)
    val baseContainerColor = when {
        colors.isGlass -> when (variant) {
            ConnectoButtonVariant.PRIMARY -> colors.primary
            ConnectoButtonVariant.SECONDARY -> Color(0xFF1E293B)
            ConnectoButtonVariant.OUTLINED -> Color.Transparent
            ConnectoButtonVariant.DESTRUCTIVE -> colors.error
            ConnectoButtonVariant.TEXT -> Color.Transparent
        }
        else -> when (variant) {
            ConnectoButtonVariant.PRIMARY -> if (colors.isDark) Color(0xFFF5F5F7) else Color(0xFF111116)
            ConnectoButtonVariant.SECONDARY -> if (colors.isDark) Color(0xFF1F1F27) else Color(0xFFF4F4F6)
            ConnectoButtonVariant.OUTLINED -> Color.Transparent
            ConnectoButtonVariant.DESTRUCTIVE -> colors.error
            ConnectoButtonVariant.TEXT -> Color.Transparent
        }
    }

    // State 4: Disabled Colors
    val disabledContainerColor = when {
        colors.isGlass -> when (variant) {
            ConnectoButtonVariant.PRIMARY,
            ConnectoButtonVariant.DESTRUCTIVE -> Color(0xFF0D1B2A)
            ConnectoButtonVariant.SECONDARY -> Color(0xFF0F172A)
            ConnectoButtonVariant.OUTLINED,
            ConnectoButtonVariant.TEXT -> Color.Transparent
        }
        else -> when (variant) {
            ConnectoButtonVariant.PRIMARY,
            ConnectoButtonVariant.DESTRUCTIVE -> if (colors.isDark) Color(0xFF16161D) else Color(0xFFE2E8F0)
            ConnectoButtonVariant.SECONDARY -> if (colors.isDark) Color(0xFF14141A) else Color(0xFFF1F5F9)
            ConnectoButtonVariant.OUTLINED,
            ConnectoButtonVariant.TEXT -> Color.Transparent
        }
    }

    // State 5: Error Resolution
    val effectiveContainerColor = when {
        isError -> if (variant == ConnectoButtonVariant.PRIMARY || variant == ConnectoButtonVariant.DESTRUCTIVE) colors.error else colors.error.copy(alpha = 0.15f)
        else -> baseContainerColor
    }

    val effectiveContentColor = when {
        isError -> if (variant == ConnectoButtonVariant.PRIMARY || variant == ConnectoButtonVariant.DESTRUCTIVE) Color.White else colors.error
        else -> baseContentColor
    }

    LaunchedEffect(isPressed) {
        if (isPressed && enabled && !isLoading && !isError) {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
        }
    }

    val shadowElevation by animateDpAsState(
        targetValue = when {
            !enabled || isError -> 0.dp
            isPressed -> 2.dp
            isHovered -> when (variant) {
                ConnectoButtonVariant.PRIMARY -> 10.dp
                ConnectoButtonVariant.DESTRUCTIVE -> 8.dp
                ConnectoButtonVariant.SECONDARY -> 6.dp
                else -> 0.dp
            }
            variant == ConnectoButtonVariant.PRIMARY -> 8.dp
            variant == ConnectoButtonVariant.DESTRUCTIVE -> 6.dp
            variant == ConnectoButtonVariant.SECONDARY -> 4.dp
            else -> 0.dp
        },
        animationSpec = spring(stiffness = 450f, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonShadowElevation"
    )

    val shadowAmbientColor = when {
        !enabled || isError -> Color.Transparent
        colors.isDark -> Color.Black.copy(alpha = if (isPressed) 0.65f else (if (isHovered) 0.55f else 0.45f))
        else -> Color.Black.copy(alpha = if (isPressed) 0.25f else (if (isHovered) 0.18f else 0.12f))
    }

    val shadowSpotColor = when {
        !enabled || isError -> Color.Transparent
        variant == ConnectoButtonVariant.PRIMARY -> colors.primary.copy(alpha = if (isPressed) 0.50f else (if (isHovered) 0.48f else 0.38f))
        variant == ConnectoButtonVariant.DESTRUCTIVE -> colors.error.copy(alpha = if (isPressed) 0.50f else 0.38f)
        colors.isDark -> Color(0xFF38BDF8).copy(alpha = if (isPressed) 0.25f else 0.12f)
        else -> Color.Black.copy(alpha = 0.10f)
    }

    // Border Resolution (Default, Hover, and Error)
    val border: BorderStroke? = when {
        isError -> BorderStroke(1.2.dp, colors.error)
        isHovered && enabled && !isLoading -> when (variant) {
            ConnectoButtonVariant.OUTLINED -> BorderStroke(1.5.dp, colors.primary)
            ConnectoButtonVariant.PRIMARY -> BorderStroke(1.2.dp, colors.primary.copy(alpha = 0.85f))
            ConnectoButtonVariant.SECONDARY -> BorderStroke(1.2.dp, if (colors.isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
            else -> null
        }
        colors.isGlass -> when (variant) {
            ConnectoButtonVariant.PRIMARY -> BorderStroke(
                1.2.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFE0F2FE),
                        colors.primary.copy(alpha = 0.60f)
                    )
                )
            )
            ConnectoButtonVariant.SECONDARY -> BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.60f),
                        Color(0x6638BDF8),
                        Color(0x1A38BDF8)
                    )
                )
            )
            ConnectoButtonVariant.OUTLINED -> BorderStroke(1.2.dp, if (enabled) colors.primary.copy(alpha = 0.8f) else colors.border.copy(alpha = 0.3f))
            else -> null
        }
        else -> when (variant) {
            ConnectoButtonVariant.PRIMARY -> BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        if (colors.isDark) Color(0xFFFFFFFF) else Color(0xFF3F3F46),
                        if (colors.isDark) Color(0xFFD4D4D8) else Color(0xFF1E1E24),
                        if (colors.isDark) Color(0xFF71717A) else Color(0xFF09090B)
                    )
                )
            )
            ConnectoButtonVariant.SECONDARY -> BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        if (colors.isDark) Color(0xFF71717A) else Color(0xFFFFFFFF),
                        if (colors.isDark) Color(0xFF3F3F46) else Color(0xFFE4E4E7)
                    )
                )
            )
            ConnectoButtonVariant.OUTLINED -> BorderStroke(1.dp, if (enabled) colors.border else colors.border.copy(alpha = 0.5f))
            else -> null
        }
    }

    val buttonModifier = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .defaultMinSize(minHeight = minHeight)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.translationY = translateY.toPx()
            this.translationX = shakeOffset.value.dp.toPx()
            alpha = if (enabled) 1.0f else 0.45f
        }
        .shadow(
            elevation = shadowElevation,
            shape = shape,
            ambientColor = shadowAmbientColor,
            spotColor = shadowSpotColor,
            clip = false
        )

    Button(
        onClick = { if (!isLoading && enabled && !isError) onClick() },
        modifier = buttonModifier,
        enabled = enabled && !isLoading,
        shape = shape,
        border = border,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = effectiveContainerColor,
            contentColor = effectiveContentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = colors.textDisabled
        ),
        contentPadding = PaddingValues(
            horizontal = when (size) {
                ConnectoButtonSize.COMPACT -> 12.dp
                ConnectoButtonSize.REGULAR -> 16.dp
                ConnectoButtonSize.LARGE -> 20.dp
            },
            vertical = ConnectoSpacing.xs
        )
    ) {
        // State 3: Loading
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = effectiveContentColor
            )
        }
        // State 5: Error
        else if (isError) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    modifier = Modifier.size(17.dp),
                    tint = effectiveContentColor
                )
                Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                Text(
                    text = errorText ?: "Failed. Try again",
                    fontSize = when (size) {
                        ConnectoButtonSize.COMPACT -> 13.sp
                        ConnectoButtonSize.REGULAR -> 14.sp
                        ConnectoButtonSize.LARGE -> 15.sp
                    },
                    fontWeight = FontWeight.Bold,
                    color = effectiveContentColor
                )
            }
        }
        // States 1 (Default), 2 (Hover), and 4 (Disabled)
        else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = effectiveContentColor
                    )
                    Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                }
                Text(
                    text = text,
                    fontSize = when (size) {
                        ConnectoButtonSize.COMPACT -> 13.5.sp
                        ConnectoButtonSize.REGULAR -> 14.5.sp
                        ConnectoButtonSize.LARGE -> 15.5.sp
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) effectiveContentColor else colors.textDisabled
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = effectiveContentColor
                    )
                }
            }
        }
    }
}
