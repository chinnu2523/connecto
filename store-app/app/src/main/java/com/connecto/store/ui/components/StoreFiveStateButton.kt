package com.connecto.store.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 5 Standard Interactive Button States:
 * 1. DEFAULT
 * 2. HOVER (Pressed / Hover state with tactile spring compression)
 * 3. LOADING (Smooth indeterminate circular spinner)
 * 4. DISABLED (Muted alpha, non-interactive)
 * 5. ERROR (Crimson alert state with horizontal shake animation and warning indicator)
 */
enum class StoreButtonState {
    DEFAULT,
    HOVER,
    LOADING,
    DISABLED,
    ERROR
}

@Composable
fun StoreFiveStateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: StoreButtonState = StoreButtonState.DEFAULT,
    icon: ImageVector? = null,
    backgroundColor: Color = Color(0xFF2563EB),
    contentColor: Color = Color.White,
    errorColor: Color = Color(0xFFDC2626),
    shape: Shape = RoundedCornerShape(12.dp),
    border: BorderStroke? = null,
    fontSize: TextUnit = 13.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    horizontalPadding: Dp = 18.dp,
    verticalPadding: Dp = 14.dp,
    pressedScale: Float = 0.95f
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Determine effective state (hover/pressed takes precedence if default)
    val effectiveState = when {
        state == StoreButtonState.LOADING -> StoreButtonState.LOADING
        state == StoreButtonState.DISABLED -> StoreButtonState.DISABLED
        state == StoreButtonState.ERROR -> StoreButtonState.ERROR
        isPressed -> StoreButtonState.HOVER
        else -> state
    }

    // Ease-in and ease-out curves animation for tactile press / hover
    val scale by animateFloatAsState(
        targetValue = if (effectiveState == StoreButtonState.HOVER) pressedScale else 1f,
        animationSpec = tween(
            durationMillis = 180,
            easing = if (effectiveState == StoreButtonState.HOVER) EaseInCubic else EaseOutCubic
        ),
        label = "btnTactileScale"
    )

    // Error shake animation
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(effectiveState) {
        if (effectiveState == StoreButtonState.ERROR) {
            shakeAnim.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -6f at 60
                    6f at 140
                    -4f at 220
                    4f at 300
                    0f at 400
                }
            )
        }
    }

    val isClickable = effectiveState != StoreButtonState.LOADING && effectiveState != StoreButtonState.DISABLED

    val bg = when (effectiveState) {
        StoreButtonState.LOADING -> backgroundColor.copy(alpha = 0.85f)
        StoreButtonState.DISABLED -> Color(0xFF27272A).copy(alpha = 0.45f)
        StoreButtonState.ERROR -> errorColor
        StoreButtonState.HOVER -> backgroundColor
        StoreButtonState.DEFAULT -> backgroundColor
    }

    val fg = when (effectiveState) {
        StoreButtonState.DISABLED -> Color(0xFF71717A)
        StoreButtonState.ERROR -> Color.White
        else -> contentColor
    }

    val animatedBg by androidx.compose.animation.animateColorAsState(
        targetValue = bg,
        animationSpec = tween(durationMillis = 200, easing = EaseInOutCubic),
        label = "btnBgColor"
    )

    val strokeBorder = when (effectiveState) {
        StoreButtonState.ERROR -> BorderStroke(1.5.dp, Color(0xFFEF4444))
        StoreButtonState.DISABLED -> BorderStroke(1.dp, Color(0xFF3F3F46).copy(alpha = 0.35f))
        else -> border
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = shakeAnim.value
            }
            .clip(shape)
            .then(if (strokeBorder != null) Modifier.border(strokeBorder, shape) else Modifier)
            .background(animatedBg)
            .then(
                if (isClickable) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        when (effectiveState) {
            StoreButtonState.LOADING -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = fg,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Please wait…",
                        color = fg,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        letterSpacing = 0.2.sp
                    )
                }
            }
            StoreButtonState.ERROR -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = "Error",
                        tint = fg,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = text,
                        color = fg,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = fg,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = text,
                        color = fg,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
