package com.example.connecto.ui.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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
}

/**
 * Master Connecto Theme provider.
 * Sets up both the custom Connecto tokens and Material 3 color schemes.
 */
@Composable
fun ConnectoDesignTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val connectoColors = if (darkTheme) DarkConnectoColors else LightConnectoColors
    val connectoTypography = ConnectoTypography()

    val m3ColorScheme = if (darkTheme) {
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
