package com.example.connecto.ui.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Connecto Centralized Design System Tokens
 * Defines standard spacing, corner radiuses, elevations, and animation timings.
 * Consistent across all screens — eliminates random spacing and arbitrary styling.
 */
object ConnectoSpacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 40.dp
    val huge: Dp = 48.dp
}

object ConnectoRadius {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val full: Dp = 999.dp
}

object ConnectoElevation {
    val none: Dp = 0.dp
    val low: Dp = 2.dp
    val medium: Dp = 4.dp
    val high: Dp = 8.dp
    val overlay: Dp = 16.dp
}

object ConnectoDuration {
    const val FAST = 150
    const val NORMAL = 250
    const val SLOW = 350
}
