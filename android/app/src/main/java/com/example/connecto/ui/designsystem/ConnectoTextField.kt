package com.example.connecto.ui.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable Connecto Text Field.
 * Consistent 8dp rounded corner, crisp outline, accessible labels, clear & password toggles.
 */
@Composable
fun ConnectoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    showClearButton: Boolean = false
) {
    val colors = LocalConnectoColors.current
    var passwordVisible by remember { mutableStateOf(!isPassword) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = when {
        isError -> colors.error
        isFocused -> colors.primary
        else -> colors.border
    }

    val borderWidth = if (isFocused || isError) 1.5.dp else 1.dp

    val shadowElevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isFocused) 4.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.spring(stiffness = 500f, dampingRatio = 0.8f),
        label = "inputElevation"
    )

    val inputBorderBrush = if (isError) {
        SolidColor(colors.error)
    } else if (isFocused) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.80f),
                colors.primary,
                colors.primary.copy(alpha = 0.60f)
            )
        )
    } else {
        SolidColor(colors.border)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isError) colors.error else colors.textPrimary,
                modifier = Modifier.padding(bottom = ConnectoSpacing.xxs)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = shadowElevation,
                    shape = RoundedCornerShape(ConnectoRadius.sm),
                    ambientColor = Color.Black.copy(alpha = 0.40f),
                    spotColor = colors.primary.copy(alpha = if (isFocused) 0.35f else 0.0f),
                    clip = false
                )
                .clip(RoundedCornerShape(ConnectoRadius.sm))
                .background(if (enabled) colors.surface else colors.surfaceVariant)
                .border(
                    width = borderWidth,
                    brush = inputBorderBrush,
                    shape = RoundedCornerShape(ConnectoRadius.sm)
                )
                .padding(
                    horizontal = ConnectoSpacing.md,
                    vertical = if (singleLine) 12.dp else ConnectoSpacing.sm
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = ConnectoSpacing.xs),
                        tint = if (isFocused) colors.primary else colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = colors.textDisabled
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        singleLine = singleLine,
                        maxLines = maxLines,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = if (enabled) colors.textPrimary else colors.textDisabled,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        visualTransformation = if (isPassword && !passwordVisible) {
                            PasswordVisualTransformation()
                        } else {
                            VisualTransformation.None
                        },
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        interactionSource = interactionSource
                    )
                }

                if (isPassword) {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else if (showClearButton && value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear input",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else if (trailingIcon != null) {
                    IconButton(
                        onClick = { onTrailingIconClick?.invoke() },
                        enabled = onTrailingIconClick != null,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isError && !errorMessage.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = errorMessage.orEmpty(),
                fontSize = 12.sp,
                color = colors.error,
                modifier = Modifier.padding(top = ConnectoSpacing.xxs, start = ConnectoSpacing.xxs)
            )
        }

        if (!isError && !helperText.isNullOrBlank()) {
            Text(
                text = helperText,
                fontSize = 12.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = ConnectoSpacing.xxs, start = ConnectoSpacing.xxs)
            )
        }
    }
}
