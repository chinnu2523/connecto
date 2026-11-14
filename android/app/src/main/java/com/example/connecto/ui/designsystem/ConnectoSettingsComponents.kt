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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Connecto Centralized Inset-Grouped Settings Components
 * Delivers the calm, smooth, clear, and high-end grouped card experience
 * inspired by modern executive UI and iOS Settings design paradigms.
 */

@Composable
fun ConnectoSettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalConnectoColors.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section Header (Uppercase, Subtly Tracked, Crisp Muted Tone)
        Text(
            text = title.uppercase(),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (colors.isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
        )

        // Unified Inset Grouped Surface Card
        ConnectoSettingsCard {
            content()
        }
    }
}

@Composable
fun ConnectoSettingsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalConnectoColors.current
    val shape = RoundedCornerShape(18.dp)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.985f else 1.0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "settingsCardScale"
    )

    val backgroundColor = when {
        colors.isGlass -> colors.surface.copy(alpha = if (colors.isDark) 0.70f else 0.88f)
        colors.isDark -> colors.surface
        else -> Color.White
    }

    val borderColor = when {
        colors.isGlass -> Color.White.copy(alpha = if (colors.isDark) 0.18f else 0.50f)
        colors.isDark -> colors.borderSubtle.copy(alpha = 0.45f)
        else -> Color(0xFFE2E8F0)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (colors.isDark) 2.dp else 1.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.35f else 0.05f),
                spotColor = colors.primary.copy(alpha = if (colors.isDark) 0.10f else 0.02f)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(width = 0.8.dp, color = borderColor, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}
                        onClick()
                    }
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun ConnectoSettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    subtitle: String? = null,
    value: String? = null,
    badge: String? = null,
    isDestructive: Boolean = false,
    showChevron: Boolean = true
) {
    val colors = LocalConnectoColors.current
    val haptic = LocalHapticFeedback.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val rowScale by animateFloatAsState(
        targetValue = if (isPressed) 0.982f else 1.0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "settingsRowScale"
    )

    val rowBgColor = if (isPressed) {
        if (colors.isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .graphicsLayer {
                scaleX = rowScale
                scaleY = rowScale
            }
            .background(rowBgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {}
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon
        if (icon != null) {
            val resolvedTint = when {
                isDestructive -> colors.error
                iconTint != null -> iconTint
                colors.isDark -> colors.textPrimary
                else -> Color(0xFF1E293B)
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isDestructive) colors.error.copy(alpha = 0.12f)
                        else if (colors.isDark) Color.White.copy(alpha = 0.08f)
                        else Color(0xFFF1F5F9)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedTint,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
        }

        // Title and optional Subtitle
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) colors.error else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Optional Status Badge
        if (badge != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.primary.copy(alpha = 0.12f))
                    .border(0.6.dp, colors.primary.copy(alpha = 0.30f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
        }

        // Value Label (e.g. "English", "Light", "1 Active")
        if (!value.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Trailing Chevron
        if (showChevron) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (colors.isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
fun ConnectoSettingsDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = 58.dp
) {
    val colors = LocalConnectoColors.current
    val dividerColor = if (colors.isDark) {
        colors.borderSubtle.copy(alpha = 0.35f)
    } else {
        Color(0xFFF1F5F9)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startIndent)
            .height(0.8.dp)
            .background(dividerColor)
    )
}

@Composable
fun ConnectoSettingsActionCard(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = true,
    icon: ImageVector? = null
) {
    val colors = LocalConnectoColors.current
    val shape = RoundedCornerShape(18.dp)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1.0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "actionCardScale"
    )

    val backgroundColor = when {
        colors.isGlass -> colors.surface.copy(alpha = if (colors.isDark) 0.70f else 0.88f)
        colors.isDark -> colors.surface
        else -> Color.White
    }

    val borderColor = when {
        isDestructive -> colors.error.copy(alpha = 0.25f)
        colors.isDark -> colors.borderSubtle.copy(alpha = 0.45f)
        else -> Color(0xFFE2E8F0)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 1.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.35f else 0.05f)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(width = 0.8.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {}
                onClick()
            }
            .padding(vertical = 16.dp, horizontal = 20.dp),
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
                    tint = if (isDestructive) colors.error else colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (isDestructive) colors.error else colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ConnectoSettingsTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null
) {
    val colors = LocalConnectoColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(36.dp))
            }

            // Centered Screen Title
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Optional Actions or Optical Balance Spacer
            if (actions != null) {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    actions()
                }
            } else {
                Spacer(modifier = Modifier.size(36.dp))
            }
        }

        // Subtle Hairline Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.6.dp)
                .background(colors.borderSubtle.copy(alpha = if (colors.isDark) 0.40f else 0.50f))
        )
    }
}
