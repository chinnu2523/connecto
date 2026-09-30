package com.example.connecto.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Reusable Connecto Confirmation / Alert Dialog.
 */
@Composable
fun ConnectoAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "Cancel",
    isDestructive: Boolean = false
) {
    val colors = LocalConnectoColors.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(ConnectoRadius.lg))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.lg))
                .padding(ConnectoSpacing.xl)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(ConnectoSpacing.sm))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(ConnectoSpacing.xl))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ConnectoButton(
                        text = dismissText,
                        onClick = onDismiss,
                        variant = ConnectoButtonVariant.TEXT,
                        size = ConnectoButtonSize.COMPACT
                    )

                    Spacer(modifier = Modifier.width(ConnectoSpacing.sm))

                    ConnectoButton(
                        text = confirmText,
                        onClick = onConfirm,
                        variant = if (isDestructive) ConnectoButtonVariant.DESTRUCTIVE else ConnectoButtonVariant.PRIMARY,
                        size = ConnectoButtonSize.COMPACT
                    )
                }
            }
        }
    }
}
