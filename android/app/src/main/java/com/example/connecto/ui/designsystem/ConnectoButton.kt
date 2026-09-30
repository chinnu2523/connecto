package com.example.connecto.ui.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer

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
 * Reusable Connecto Button adhering to the Centralized Design System.
 * Consistent 8px radius, 14–15px font, 600 weight, smooth transitions with slight scale/translate on press.
 * Primary: dark (near-black) background, white text, subtle shadow, smooth press states.
 * Secondary: light background, dark text, thin border, subtle press states.
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
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    fullWidth: Boolean = false
) {
    val colors = LocalConnectoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.8f),
        label = "buttonScale"
    )
    val translateY by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 0.dp,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.8f),
        label = "buttonTranslateY"
    )

    val minHeight = when (size) {
        ConnectoButtonSize.COMPACT -> 36.dp
        ConnectoButtonSize.REGULAR -> 44.dp
        ConnectoButtonSize.LARGE -> 52.dp
    }

    val shape = RoundedCornerShape(8.dp) // Consistent 8px radius

    val contentColor = when (variant) {
        ConnectoButtonVariant.PRIMARY -> Color.White
        ConnectoButtonVariant.SECONDARY -> if (colors.isDark) Color(0xFFF4F4F6) else Color(0xFF18181B)
        ConnectoButtonVariant.OUTLINED -> colors.primary
        ConnectoButtonVariant.DESTRUCTIVE -> colors.onError
        ConnectoButtonVariant.TEXT -> colors.primary
    }

    val containerColor = when (variant) {
        ConnectoButtonVariant.PRIMARY -> if (colors.isDark) Color(0xFF141418) else Color(0xFF111116) // Near-black background
        ConnectoButtonVariant.SECONDARY -> if (colors.isDark) Color(0xFF1A1A22) else Color(0xFFF4F4F6) // Light background
        ConnectoButtonVariant.OUTLINED -> Color.Transparent
        ConnectoButtonVariant.DESTRUCTIVE -> colors.error
        ConnectoButtonVariant.TEXT -> Color.Transparent
    }

    val disabledContainerColor = when (variant) {
        ConnectoButtonVariant.PRIMARY,
        ConnectoButtonVariant.DESTRUCTIVE -> if (colors.isDark) Color(0xFF16161D) else Color(0xFFE2E8F0)
        ConnectoButtonVariant.SECONDARY -> if (colors.isDark) Color(0xFF14141A) else Color(0xFFF1F5F9)
        ConnectoButtonVariant.OUTLINED,
        ConnectoButtonVariant.TEXT -> Color.Transparent
    }

    val border = when (variant) {
        ConnectoButtonVariant.PRIMARY -> BorderStroke(1.dp, if (colors.isDark) Color(0xFF262630) else Color(0xFF1E1E24))
        ConnectoButtonVariant.SECONDARY -> BorderStroke(1.dp, if (colors.isDark) Color(0xFF2E2E38) else Color(0xFFE4E4E7))
        ConnectoButtonVariant.OUTLINED -> BorderStroke(1.dp, if (enabled) colors.border else colors.border.copy(alpha = 0.5f))
        else -> null
    }

    val elevation = if (variant == ConnectoButtonVariant.PRIMARY && enabled) {
        ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp)
    } else {
        ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    }

    val buttonModifier = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .defaultMinSize(minHeight = minHeight)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.translationY = translateY.toPx()
        }

    Button(
        onClick = { if (!isLoading && enabled) onClick() },
        modifier = buttonModifier,
        enabled = enabled && !isLoading,
        shape = shape,
        border = border,
        elevation = elevation,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
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
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = contentColor
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
                    color = if (enabled) contentColor else colors.textDisabled
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = contentColor
                    )
                }
            }
        }
    }
}
