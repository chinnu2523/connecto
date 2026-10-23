package com.example.connecto.ui.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

/**
 * Accessor object for Connecto Design System tokens in Composable functions.
 */
object ConnectoTheme {
    val colors: ConnectoColors
        @Composable
        get() = LocalConnectoColors.current

    val typography: ConnectoTypography
        @Composable
        get() = LocalConnectoTypography.current

    val isGlass: Boolean
        @Composable
        get() = LocalConnectoColors.current.isGlass

    val isLiquidGlass: Boolean
        @Composable
        get() = LocalConnectoColors.current.isLiquidGlass

    val isPureBlack: Boolean
        @Composable
        get() = LocalConnectoColors.current.background == Color(0xFF000000)

    val glassAlpha: Float
        @Composable
        get() = LocalConnectoColors.current.glassAlpha
}

/**
 * Master Connecto Theme provider.
 * Sets up both the custom Connecto tokens, Material 3 color schemes,
 * and buttery animated theme transitions.
 */
@Composable
fun ConnectoDesignTheme(
    themeMode: ThemeMode = ThemeMode.NOIR,
    darkTheme: Boolean = themeMode.isDark,
    glassTransparency: Float = 0.0f,
    content: @Composable () -> Unit
) {
    val rawColors = getColorsForTheme(themeMode, glassTransparency)
    val connectoTypography = ConnectoTypography()

    val animSpec = tween<Color>(durationMillis = 260, easing = FastOutSlowInEasing)

    val animatedPrimary by animateColorAsState(rawColors.primary, animSpec, label = "primaryColor")
    val animatedBackground by animateColorAsState(rawColors.background, animSpec, label = "backgroundColor")
    val animatedSurface by animateColorAsState(rawColors.surface, animSpec, label = "surfaceColor")
    val animatedSurfaceVariant by animateColorAsState(rawColors.surfaceVariant, animSpec, label = "surfaceVariantColor")
    val animatedTextPrimary by animateColorAsState(rawColors.textPrimary, animSpec, label = "textPrimaryColor")
    val animatedTextSecondary by animateColorAsState(rawColors.textSecondary, animSpec, label = "textSecondaryColor")
    val animatedBorder by animateColorAsState(rawColors.border, animSpec, label = "borderColor")
    val animatedBorderSubtle by animateColorAsState(rawColors.borderSubtle, animSpec, label = "borderSubtleColor")

    val connectoColors = rawColors.copy(
        primary = animatedPrimary,
        background = animatedBackground,
        surface = animatedSurface,
        surfaceVariant = animatedSurfaceVariant,
        textPrimary = animatedTextPrimary,
        textSecondary = animatedTextSecondary,
        border = animatedBorder,
        borderSubtle = animatedBorderSubtle
    )

    val m3ColorScheme = if (connectoColors.isDark) {
        darkColorScheme(
            primary = connectoColors.primary,
            onPrimary = connectoColors.onPrimary,
            primaryContainer = connectoColors.primaryContainer,
            onPrimaryContainer = connectoColors.onPrimaryContainer,
            secondary = connectoColors.info,
            onSecondary = connectoColors.onInfo,
            tertiary = connectoColors.warning,
            onTertiary = connectoColors.onWarning,
            background = connectoColors.background,
            onBackground = connectoColors.onBackground,
            surface = connectoColors.surface,
            onSurface = connectoColors.onSurface,
            surfaceVariant = connectoColors.surfaceVariant,
            onSurfaceVariant = connectoColors.onSurfaceVariant,
            outline = connectoColors.border,
            error = connectoColors.error,
            onError = connectoColors.onError
        )
    } else {
        lightColorScheme(
            primary = connectoColors.primary,
            onPrimary = connectoColors.onPrimary,
            primaryContainer = connectoColors.primaryContainer,
            onPrimaryContainer = connectoColors.onPrimaryContainer,
            secondary = connectoColors.info,
            onSecondary = connectoColors.onInfo,
            tertiary = connectoColors.warning,
            onTertiary = connectoColors.onWarning,
            background = connectoColors.background,
            onBackground = connectoColors.onBackground,
            surface = connectoColors.surface,
            onSurface = connectoColors.onSurface,
            surfaceVariant = connectoColors.surfaceVariant,
            onSurfaceVariant = connectoColors.onSurfaceVariant,
            outline = connectoColors.border,
            error = connectoColors.error,
            onError = connectoColors.onError
        )
    }

    CompositionLocalProvider(
        LocalConnectoColors provides connectoColors,
        LocalConnectoTypography provides connectoTypography
    ) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            content = content
        )
    }
}
