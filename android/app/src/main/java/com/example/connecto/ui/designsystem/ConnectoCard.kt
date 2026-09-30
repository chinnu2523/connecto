package com.example.connecto.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ConnectoCardVariant {
    SURFACE,
    ELEVATED,
    OUTLINED
}

/**
 * Reusable Connecto Card component adhering to the Centralized Design System.
 * Controlled corner radius (12dp), no over-the-top glassmorphism or giant rounded corners.
 */
@Composable
fun ConnectoCard(
    modifier: Modifier = Modifier,
    variant: ConnectoCardVariant = ConnectoCardVariant.SURFACE,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ConnectoRadius.md),
    contentPadding: Dp = ConnectoSpacing.md,
    border: BorderStroke? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalConnectoColors.current

    val containerColor = when (variant) {
        ConnectoCardVariant.SURFACE -> colors.surface
        ConnectoCardVariant.ELEVATED -> colors.surface
        ConnectoCardVariant.OUTLINED -> colors.surface
    }

    val elevation = when (variant) {
        ConnectoCardVariant.ELEVATED -> ConnectoElevation.low
        ConnectoCardVariant.SURFACE,
        ConnectoCardVariant.OUTLINED -> ConnectoElevation.none
    }

    val effectiveBorder = border ?: when (variant) {
        ConnectoCardVariant.OUTLINED -> BorderStroke(1.dp, colors.border)
        ConnectoCardVariant.SURFACE,
        ConnectoCardVariant.ELEVATED -> BorderStroke(1.dp, colors.borderSubtle)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = colors.textPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = effectiveBorder
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}
