package com.example.connecto.ui.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable Connecto Empty State component.
 * Provides clear explanation and a helpful next action.
 */
@Composable
fun ConnectoEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = LocalConnectoColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ConnectoSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
                    .padding(ConnectoSpacing.md),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(ConnectoSpacing.md))
        }

        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ConnectoSpacing.xs))

        Text(
            text = description,
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(ConnectoSpacing.lg))
            ConnectoButton(
                text = actionText,
                onClick = onActionClick,
                variant = ConnectoButtonVariant.PRIMARY,
                size = ConnectoButtonSize.REGULAR
            )
        }
    }
}

/**
 * Reusable Connecto Error State component.
 * Contextual message with mandatory retry capability.
 */
@Composable
fun ConnectoErrorState(
    title: String = "Something went wrong",
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryText: String = "Try Again"
) {
    val colors = LocalConnectoColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ConnectoSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(colors.error.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = colors.error,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(ConnectoSpacing.md))

        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ConnectoSpacing.xs))

        Text(
            text = message,
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(ConnectoSpacing.lg))

        ConnectoButton(
            text = retryText,
            onClick = onRetry,
            variant = ConnectoButtonVariant.OUTLINED,
            size = ConnectoButtonSize.REGULAR,
            leadingIcon = Icons.Default.Refresh
        )
    }
}

/**
 * Reusable Connecto Loading Spinner.
 */
@Composable
fun ConnectoLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    val colors = LocalConnectoColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ConnectoSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            strokeWidth = 3.dp,
            color = colors.primary
        )
        if (!message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(ConnectoSpacing.md))
            Text(
                text = message,
                fontSize = 13.sp,
                color = colors.textSecondary
            )
        }
    }
}

/**
 * Connecto Shimmer Modifier for Skeleton Loading.
 */
@Composable
fun connectoShimmerBrush(): Brush {
    val colors = LocalConnectoColors.current
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val baseColor = if (colors.isDark) Color(0xFF1F2937) else Color(0xFFE2E8F0)
    val highlightColor = if (colors.isDark) Color(0xFF374151) else Color(0xFFF1F5F9)

    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

/**
 * Skeleton Loader Block.
 */
@Composable
fun ConnectoSkeleton(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(ConnectoRadius.xs)
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(shape)
            .background(connectoShimmerBrush())
    )
}

/**
 * Offline & Reconnecting Status Banner.
 */
@Composable
fun ConnectoOfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val colors = LocalConnectoColors.current

    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(colors.warning.copy(alpha = 0.15f))
                .border(1.dp, colors.warning.copy(alpha = 0.3f))
                .padding(horizontal = ConnectoSpacing.md, vertical = ConnectoSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
            Text(
                text = "Connecting to server...",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (onRetry != null) {
                Text(
                    text = "Retry",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    modifier = Modifier.padding(horizontal = ConnectoSpacing.xs)
                )
            }
        }
    }
}
