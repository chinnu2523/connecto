package com.example.connecto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.connecto.ui.designsystem.DarkConnectoColors
import com.example.connecto.ui.designsystem.LightConnectoColors

// ========== CONNECTO REFINED PROFESSIONAL PALETTE ==========
// Classic, mature communication app theme (inspired by Slack, Teams, Discord standards)
val StitchCanvas = DarkConnectoColors.background         // OLED Pure Black Canvas (#000000)
val StitchSurface = DarkConnectoColors.surface           // Deep Surface Container (#08080A)
val StitchElevated = DarkConnectoColors.surfaceVariant   // Elevated Container (#141418)
val StitchHover = Color(0xFF1C1C24)                     // Interactive Hover Surface
val StitchAccent = DarkConnectoColors.primary           // Professional Indigo (#6366F1)
val StitchCyan = DarkConnectoColors.info                // Professional Blue Accent (#60A5FA)
val StitchEmerald = DarkConnectoColors.success          // Online Status Green (#34D399)
val StitchGold = DarkConnectoColors.warning             // Notification Amber (#FBBF24)
val StitchDanger = DarkConnectoColors.error             // Alert Red (#F87171)
val StitchRim = DarkConnectoColors.border               // Crisp 1dp Border (#222228)
val StitchRimSubtle = DarkConnectoColors.borderSubtle   // Subtle Border (#16161C)
val StitchRimHover = Color(0xFF2E2E38)                  // Hover Border

// Standard Semantic Aliases
val BackgroundColor = StitchCanvas
val SurfaceColor = StitchSurface
val SurfaceElevatedColor = StitchElevated
val BorderColor = StitchRim

val CyberCyan = StitchCyan
val NeonPurple = StitchAccent
val NeonMagenta = Color(0xFF818CF8)
val OnlineGreen = StitchEmerald

val TextPrimaryColor = DarkConnectoColors.textPrimary
val TextSecondaryColor = DarkConnectoColors.textSecondary
val TextDisabledColor = DarkConnectoColors.textDisabled

// Accent Gradients (Subtle & Restrained)
val DarkAccentGradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
val LightAccentGradientColors = listOf(Color(0xFF4F46E5), Color(0xFF4338CA))

val AccentGradientColors = DarkAccentGradientColors
val AccentGradient = Brush.linearGradient(AccentGradientColors)

// Backward Compatibility Aliases
val DeepSpaceBackground = BackgroundColor
val DarkCardSurface = SurfaceColor
val DarkSurfaceGlass = SurfaceElevatedColor
val ElectricViolet = NeonPurple
val CyanGlow = CyberCyan
val NeonPink = NeonMagenta
val TextPrimaryDark = TextPrimaryColor
val TextSecondaryDark = TextSecondaryColor
val TextMutedDark = TextDisabledColor

// Restrained Spring Spec
val FramerMotionSpring = androidx.compose.animation.core.spring<Float>(
    stiffness = 500f,
    dampingRatio = 0.85f
)

// Theme-Aware Dynamic Gradient Helper
@Composable
fun getDynamicAccentGradientColors(): List<Color> {
    return if (isAppInLightTheme()) LightAccentGradientColors else DarkAccentGradientColors
}

// Bulletproof Light/Dark Theme Detector
@Composable
fun isAppInLightTheme(): Boolean {
    val bg = MaterialTheme.colorScheme.background
    val luminance = 0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue
    return luminance > 0.5f
}

@Composable
fun getContentColorOnAccentGradient(): Color {
    return Color.White
}
