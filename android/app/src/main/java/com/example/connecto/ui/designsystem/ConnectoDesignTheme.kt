package com.example.connecto.ui.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.max

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
        get() = LocalConnectoColors.current.background == Color(0xFF000000) || (!LocalConnectoColors.current.isGlass && LocalConnectoColors.current.isDark)

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

    val animSpec = tween<Color>(durationMillis = 320, easing = FastOutSlowInEasing)

    val animatedPrimary by animateColorAsState(rawColors.primary, animSpec, label = "primaryColor")
    val animatedOnPrimary by animateColorAsState(rawColors.onPrimary, animSpec, label = "onPrimaryColor")
    val animatedPrimaryContainer by animateColorAsState(rawColors.primaryContainer, animSpec, label = "primaryContainerColor")
    val animatedOnPrimaryContainer by animateColorAsState(rawColors.onPrimaryContainer, animSpec, label = "onPrimaryContainerColor")
    val animatedBackground by animateColorAsState(rawColors.background, animSpec, label = "backgroundColor")
    val animatedOnBackground by animateColorAsState(rawColors.onBackground, animSpec, label = "onBackgroundColor")
    val animatedSurface by animateColorAsState(rawColors.surface, animSpec, label = "surfaceColor")
    val animatedOnSurface by animateColorAsState(rawColors.onSurface, animSpec, label = "onSurfaceColor")
    val animatedSurfaceVariant by animateColorAsState(rawColors.surfaceVariant, animSpec, label = "surfaceVariantColor")
    val animatedOnSurfaceVariant by animateColorAsState(rawColors.onSurfaceVariant, animSpec, label = "onSurfaceVariantColor")
    val animatedTextPrimary by animateColorAsState(rawColors.textPrimary, animSpec, label = "textPrimaryColor")
    val animatedTextSecondary by animateColorAsState(rawColors.textSecondary, animSpec, label = "textSecondaryColor")
    val animatedTextDisabled by animateColorAsState(rawColors.textDisabled, animSpec, label = "textDisabledColor")
    val animatedBorder by animateColorAsState(rawColors.border, animSpec, label = "borderColor")
    val animatedBorderSubtle by animateColorAsState(rawColors.borderSubtle, animSpec, label = "borderSubtleColor")
    val animatedSuccess by animateColorAsState(rawColors.success, animSpec, label = "successColor")
    val animatedWarning by animateColorAsState(rawColors.warning, animSpec, label = "warningColor")
    val animatedError by animateColorAsState(rawColors.error, animSpec, label = "errorColor")
    val animatedInfo by animateColorAsState(rawColors.info, animSpec, label = "infoColor")

    val connectoColors = rawColors.copy(
        primary = animatedPrimary,
        onPrimary = animatedOnPrimary,
        primaryContainer = animatedPrimaryContainer,
        onPrimaryContainer = animatedOnPrimaryContainer,
        background = animatedBackground,
        onBackground = animatedOnBackground,
        surface = animatedSurface,
        onSurface = animatedOnSurface,
        surfaceVariant = animatedSurfaceVariant,
        onSurfaceVariant = animatedOnSurfaceVariant,
        textPrimary = animatedTextPrimary,
        textSecondary = animatedTextSecondary,
        textDisabled = animatedTextDisabled,
        border = animatedBorder,
        borderSubtle = animatedBorderSubtle,
        success = animatedSuccess,
        warning = animatedWarning,
        error = animatedError,
        info = animatedInfo
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
            colorScheme = m3ColorScheme
        ) {
            ConnectoThemeTransitionContainer(isDark = connectoColors.isDark) {
                content()
            }
        }
    }
}

/**
 * Global coordinator for smooth circular iris theme transitions.
 * Captures the exact screen coordinates of the trigger toggle so the open/close
 * aperture animation radiates directly from the button.
 */
object ThemeTransitionManager {
    var toggleOrigin by mutableStateOf<Offset?>(null)

    fun recordOrigin(origin: Offset) {
        toggleOrigin = origin
    }
}

/**
 * Full-screen theme transition iris reveal container.
 * Animates a silky circular aperture open/close reveal originating directly
 * from the toggle's physical screen coordinates.
 */
@Composable
fun ConnectoThemeTransitionContainer(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var previousDarkState by remember { mutableStateOf<Boolean?>(null) }
    val transitionProgress = remember { Animatable(1f) }
    var animatingToDark by remember { mutableStateOf(isDark) }

    LaunchedEffect(isDark) {
        if (previousDarkState != null && previousDarkState != isDark) {
            animatingToDark = isDark
            transitionProgress.snapTo(0f)
            transitionProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 440,
                    easing = androidx.compose.animation.core.CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                )
            )
        }
        previousDarkState = isDark
    }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (transitionProgress.value < 1.0f) {
            val progress = transitionProgress.value
            val origin = ThemeTransitionManager.toggleOrigin

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                // Origin starts from the toggle button coordinates or falls back to top right
                val center = origin ?: Offset(size.width * 0.82f, size.height * 0.32f)

                // Calculate the exact distance from center to the furthest of the 4 screen corners
                val maxRadius = maxOf(
                    hypot(center.x, center.y),
                    hypot(size.width - center.x, center.y),
                    hypot(center.x, size.height - center.y),
                    hypot(size.width - center.x, size.height - center.y)
                ) * 1.05f

                val currentRadius = maxRadius * progress

                val accentColor = if (animatingToDark) Color(0xFF38BDF8) else Color(0xFFF59E0B)
                val targetBg = if (animatingToDark) Color(0xFF000000) else Color(0xFFFAF9F6)

                // 1. Soft radial aperture aura expanding from the toggle
                val fillAlpha = ((1f - progress) * 0.18f).coerceIn(0f, 1f)
                val radialBrush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = fillAlpha * 0.95f),
                        targetBg.copy(alpha = fillAlpha * 0.50f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius.coerceAtLeast(1f)
                )
                drawCircle(
                    brush = radialBrush,
                    radius = currentRadius,
                    center = center
                )

                // 2. Glowing Iris wavefront boundary ring (Aperture opening / closing)
                val ringAlpha = ((1f - progress * 0.75f) * 0.85f).coerceIn(0f, 1f)
                drawCircle(
                    color = accentColor.copy(alpha = ringAlpha),
                    radius = currentRadius,
                    center = center,
                    style = Stroke(width = 3.5.dp.toPx())
                )

                // 3. Ambient soft bloom aura around the wavefront
                val glowAlpha = ((1f - progress) * 0.40f).coerceIn(0f, 1f)
                drawCircle(
                    color = accentColor.copy(alpha = glowAlpha),
                    radius = (currentRadius + 4.dp.toPx()),
                    center = center,
                    style = Stroke(width = 8.dp.toPx())
                )
            }
        }
    }
}
