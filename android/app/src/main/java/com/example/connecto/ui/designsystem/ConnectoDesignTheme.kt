package com.example.connecto.ui.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.geometry.Size
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
 * Full-screen theme transition portal reveal container.
 * Animates an immersive dimensional portal opening and expanding directly
 * from the toggle's physical screen coordinates in pure Black & White.
 * Features a swirling vortex core, event horizon rings, spinning portal blades,
 * and outer gravitational shockwave ripples.
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
                    durationMillis = 520,
                    easing = CubicBezierEasing(0.12f, 0.8f, 0.15f, 1.0f)
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
                // Portal origin starts directly from the toggle switch or falls back
                val center = origin ?: Offset(size.width * 0.82f, size.height * 0.32f)

                // Calculate distance to the furthest screen corner to fully engulf the viewport
                val maxRadius = maxOf(
                    hypot(center.x, center.y),
                    hypot(size.width - center.x, center.y),
                    hypot(center.x, size.height - center.y),
                    hypot(size.width - center.x, size.height - center.y)
                ) * 1.12f

                val currentRadius = maxRadius * progress

                // Strict Black & White dimensional portal palette
                val horizonColor = if (animatingToDark) Color.White else Color.Black
                val voidColor = if (animatingToDark) Color.Black else Color.White

                // 1. Vortex Core Singularity (Radial Dimensional Gradient)
                val fillAlpha = ((1f - progress) * 0.28f).coerceIn(0f, 1f)
                val coreAlpha = ((1f - progress * 0.65f) * 0.40f).coerceIn(0f, 1f)
                val portalBrush = Brush.radialGradient(
                    0.0f to horizonColor.copy(alpha = coreAlpha),
                    0.55f to voidColor.copy(alpha = fillAlpha * 0.70f),
                    0.92f to horizonColor.copy(alpha = fillAlpha * 0.95f),
                    1.0f to Color.Transparent,
                    center = center,
                    radius = currentRadius.coerceAtLeast(1f)
                )
                drawCircle(
                    brush = portalBrush,
                    radius = currentRadius,
                    center = center
                )

                // 2. Primary Event Horizon Ring
                val ringAlpha = ((1f - progress * 0.70f) * 0.90f).coerceIn(0f, 1f)
                drawCircle(
                    color = horizonColor.copy(alpha = ringAlpha),
                    radius = currentRadius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )

                // 3. Inner Swirling Vortex Depth Rings
                val innerRing1Alpha = ((1f - progress) * 0.55f).coerceIn(0f, 1f)
                val innerRadius1 = currentRadius * 0.76f
                if (innerRadius1 > 1f) {
                    drawCircle(
                        color = horizonColor.copy(alpha = innerRing1Alpha),
                        radius = innerRadius1,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                val innerRing2Alpha = ((1f - progress) * 0.35f).coerceIn(0f, 1f)
                val innerRadius2 = currentRadius * 0.46f
                if (innerRadius2 > 1f) {
                    drawCircle(
                        color = horizonColor.copy(alpha = innerRing2Alpha),
                        radius = innerRadius2,
                        center = center,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }

                // 4. Outer Gravitational Shockwave Ripple
                val shockwaveAlpha = ((1f - progress) * 0.38f).coerceIn(0f, 1f)
                drawCircle(
                    color = horizonColor.copy(alpha = shockwaveAlpha),
                    radius = currentRadius * 1.08f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // 5. Spinning Portal Horizon Blades / Rotating Vortex Arcs
                val spinAngle = progress * 240f
                val bladeAlpha = ((1f - progress * 0.80f) * 0.80f).coerceIn(0f, 1f)
                val bladeRadius = currentRadius * 0.92f
                if (bladeRadius > 2f) {
                    val bladeDiameter = bladeRadius * 2f
                    val bladeTopLeft = Offset(center.x - bladeRadius, center.y - bladeRadius)
                    val bladeSize = Size(bladeDiameter, bladeDiameter)

                    for (i in 0 until 6) {
                        val arcStart = (i * 60f + spinAngle) % 360f
                        drawArc(
                            color = horizonColor.copy(alpha = bladeAlpha),
                            startAngle = arcStart,
                            sweepAngle = 32f,
                            useCenter = false,
                            topLeft = bladeTopLeft,
                            size = bladeSize,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
