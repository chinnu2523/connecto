package com.example.connecto.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable Connecto Chip component adhering to the Centralized Design System.
 * Supports Filter, Action, and Emoji Reaction pills.
 */
@Composable
fun ConnectoFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badgeCount: Int? = null
) {
    val colors = LocalConnectoColors.current

    val bgColor = if (selected) colors.primaryContainer else colors.surface
    val textColor = if (selected) colors.onPrimaryContainer else colors.textSecondary
    val borderColor = if (selected) colors.primary else colors.border

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 34.dp)
            .clip(RoundedCornerShape(ConnectoRadius.full))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(ConnectoRadius.full))
            .clickable(onClick = onClick)
            .padding(horizontal = ConnectoSpacing.md, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
            if (badgeCount != null && badgeCount > 0) {
                Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                ConnectoBadge(count = badgeCount)
            }
        }
    }
}

/**
 * Chat Emoji Reaction Pill with active toggle state.
 */
@Composable
fun ConnectoReactionChip(
    emoji: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalConnectoColors.current

    val bgColor = if (isSelected) colors.primaryContainer else colors.surfaceVariant.copy(alpha = 0.6f)
    val borderColor = if (isSelected) colors.primary else colors.borderSubtle
    val textColor = if (isSelected) colors.primary else colors.textSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(ConnectoRadius.sm))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(ConnectoRadius.sm))
            .clickable(onClick = onClick)
            .padding(horizontal = ConnectoSpacing.xs, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 13.sp)
            if (count > 0) {
                Spacer(modifier = Modifier.width(ConnectoSpacing.xxs))
                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}
