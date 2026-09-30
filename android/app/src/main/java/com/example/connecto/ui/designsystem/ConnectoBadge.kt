package com.example.connecto.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ConnectoBadgeVariant {
    PRIMARY,
    ERROR,
    SUCCESS,
    WARNING,
    NEUTRAL
}

/**
 * Reusable Connecto Badge component adhering to the Centralized Design System.
 */
@Composable
fun ConnectoBadge(
    count: Int,
    modifier: Modifier = Modifier,
    variant: ConnectoBadgeVariant = ConnectoBadgeVariant.PRIMARY
) {
    if (count <= 0) return
    val colors = LocalConnectoColors.current

    val containerColor = when (variant) {
        ConnectoBadgeVariant.PRIMARY -> colors.primary
        ConnectoBadgeVariant.ERROR -> colors.error
        ConnectoBadgeVariant.SUCCESS -> colors.success
        ConnectoBadgeVariant.WARNING -> colors.warning
        ConnectoBadgeVariant.NEUTRAL -> colors.surfaceVariant
    }

    val contentColor = when (variant) {
        ConnectoBadgeVariant.PRIMARY -> colors.onPrimary
        ConnectoBadgeVariant.ERROR -> colors.onError
        ConnectoBadgeVariant.SUCCESS -> colors.onSuccess
        ConnectoBadgeVariant.WARNING -> colors.onWarning
        ConnectoBadgeVariant.NEUTRAL -> colors.textPrimary
    }

    val displayText = if (count > 99) "99+" else count.toString()

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 5.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 12.sp
        )
    }
}

/**
 * Reusable Status Tag / Pill Badge (e.g., "Admin", "Voice Live", "Verified").
 */
@Composable
fun ConnectoTag(
    text: String,
    modifier: Modifier = Modifier,
    variant: ConnectoBadgeVariant = ConnectoBadgeVariant.NEUTRAL
) {
    val colors = LocalConnectoColors.current

    val (bgColor, textColor, borderColor) = when (variant) {
        ConnectoBadgeVariant.PRIMARY -> Triple(colors.primaryContainer, colors.onPrimaryContainer, colors.primary.copy(alpha = 0.3f))
        ConnectoBadgeVariant.SUCCESS -> Triple(colors.success.copy(alpha = 0.12f), colors.success, colors.success.copy(alpha = 0.3f))
        ConnectoBadgeVariant.ERROR -> Triple(colors.error.copy(alpha = 0.12f), colors.error, colors.error.copy(alpha = 0.3f))
        ConnectoBadgeVariant.WARNING -> Triple(colors.warning.copy(alpha = 0.12f), colors.warning, colors.warning.copy(alpha = 0.3f))
        ConnectoBadgeVariant.NEUTRAL -> Triple(colors.surfaceVariant, colors.textSecondary, colors.border)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ConnectoRadius.xs))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(ConnectoRadius.xs))
            .padding(horizontal = ConnectoSpacing.xs, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 14.sp
        )
    }
}
