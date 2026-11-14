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
import androidx.compose.foundation.clickable
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

/**
 * Modern In-Place Error Banner (Errors in Place - UI Fix #01)
 * Replaces disruptive alert dialogs with inline contextual error notifications.
 */
@Composable
fun ConnectoInPlaceError(
    message: String?,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val colors = LocalConnectoColors.current

    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        if (!message.isNullOrBlank()) {
            val bg = if (colors.isDark) Color(0xFF3B1219).copy(alpha = 0.65f) else Color(0xFFFFECEC)
            val borderCol = if (colors.isDark) Color(0xFFEF4444).copy(alpha = 0.35f) else Color(0xFFFCA5A5)
            val textCol = if (colors.isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626)

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = textCol,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (onDismiss != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                color = textCol.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Multi-item Skeleton Card for List Previews (Fixes "Screens open empty" & "Blank loading screens")
 */
@Composable
fun ConnectoSkeletonList(
    itemCount: Int = 4,
    modifier: Modifier = Modifier
) {
    val colors = LocalConnectoColors.current
    val shimmer = connectoShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (colors.isDark) colors.surface else Color.White)
                    .border(0.8.dp, colors.borderSubtle.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(shimmer)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmer)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(11.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmer)
                    )
                }
            }
        }
    }
}

