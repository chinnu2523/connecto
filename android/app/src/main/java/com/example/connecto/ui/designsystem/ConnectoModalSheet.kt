package com.example.connecto.ui.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Connecto Centralized Modal Bottom Sheet
 * Implements modern, tactile modal sheet transition physics with top-docked
 * rounded corners, fluid spring gestures, and strict Pure Black / Clean White theme compliance.
 *
 * This version hides the experimental SheetState from the public API signature so
 * calling Composables don't need to propagate ExperimentalMaterial3Api opt-ins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectoModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showDragHandle: Boolean = true,
    topCornerRadius: Dp = 28.dp,
    dragHandle: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = LocalConnectoColors.current
    val sheetShape = RoundedCornerShape(topStart = topCornerRadius, topEnd = topCornerRadius)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = sheetShape,
        containerColor = if (colors.isDark) Color(0xFF0D0F14) else Color.White,
        contentColor = colors.textPrimary,
        scrimColor = Color.Black.copy(alpha = if (colors.isDark) 0.75f else 0.50f),
        tonalElevation = 0.dp,
        dragHandle = if (showDragHandle) {
            dragHandle ?: {
                ConnectoSheetDragHandle()
            }
        } else null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            content()
        }
    }
}

/**
 * Modern Ergonomic Pill Drag Handle
 */
@Composable
fun ConnectoSheetDragHandle(
    modifier: Modifier = Modifier
) {
    val colors = LocalConnectoColors.current
    val handleColor = if (colors.isDark) Color(0xFF334155) else Color(0xFFCBD5E1)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 4.5.dp)
                .clip(CircleShape)
                .background(handleColor)
        )
    }
}

/**
 * Connecto Alert / Action Modal Sheet (Modern alternative to centered popup dialogs)
 */
@Composable
fun ConnectoAlertModalSheet(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    icon: ImageVector? = null
) {
    val colors = LocalConnectoColors.current
    val haptic = LocalHapticFeedback.current

    ConnectoModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Optional Header Icon
            if (icon != null) {
                val iconBg = if (isDestructive) colors.error.copy(alpha = 0.12f)
                else if (colors.isDark) Color.White.copy(alpha = 0.08f)
                else Color(0xFFF1F5F9)

                val iconTint = if (isDestructive) colors.error else colors.primary

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Title
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body Message
            Text(
                text = message,
                fontSize = 14.sp,
                color = colors.textSecondary,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Action Buttons with Spring Feedback
            ConnectoButton(
                text = confirmText,
                onClick = {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (_: Exception) {}
                    onConfirm()
                },
                variant = if (isDestructive) ConnectoButtonVariant.DESTRUCTIVE else ConnectoButtonVariant.PRIMARY,
                size = ConnectoButtonSize.LARGE,
                fullWidth = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            ConnectoButton(
                text = dismissText,
                onClick = onDismiss,
                variant = ConnectoButtonVariant.SECONDARY,
                size = ConnectoButtonSize.REGULAR,
                fullWidth = true
            )
        }
    }
}
