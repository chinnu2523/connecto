package com.example.connecto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.connecto.ui.designsystem.DarkConnectoColors
import com.example.connecto.ui.designsystem.LightConnectoColors

// ========== CONNECTO REFINED PROFESSIONAL PALETTE ==========
// Dark Edition: Pure Black OLED (0-nit luxury noir)
val StitchCanvas = DarkConnectoColors.background         // OLED Pure Black Canvas (#000000)
val StitchSurface = DarkConnectoColors.surface           // Deep Base Container (#080808)
val StitchElevated = DarkConnectoColors.surfaceVariant   // Obsidian Card Surface (#0A0A0A)
val StitchHover = Color(0xFF181818)                     // Interactive Hover Surface (#181818)
val StitchAccent = DarkConnectoColors.primary           // Crisp White Accent (#FFFFFF)
val StitchCyan = DarkConnectoColors.info                // Cyber Cyan Accent (#22D3EE)
val StitchEmerald = DarkConnectoColors.success          // Online Status Green (#34D399)
val StitchGold = DarkConnectoColors.warning             // Notification Amber (#FBBF24)
val StitchDanger = DarkConnectoColors.error             // Alert Red (#F87171)
val StitchRim = DarkConnectoColors.border               // Crisp 1dp Border (#222228)
val StitchRimSubtle = DarkConnectoColors.borderSubtle   // Subtle Border (#16161C)
val StitchRimHover = Color(0xFF2E2E38)                  // Hover Border

// Light Edition: Ivory Paper (Parchment)
val ParchmentCanvas = LightConnectoColors.background       // Ivory Paper (#FBF8F2)
val ParchmentSurface = LightConnectoColors.surface         // Crisp White (#FFFFFF)
val ParchmentSurfaceVariant = LightConnectoColors.surfaceVariant // Level 1 Warm Surface (#F5F1E8)
val ParchmentElevated = LightConnectoColors.primaryContainer    // Level 2 Warm Surface (#EBE6DC)
val ParchmentBorder = LightConnectoColors.border           // Archival Border (#E7E2D8)
val ParchmentBorderSubtle = LightConnectoColors.borderSubtle // Subtle Border (#F0ECE4)
val ParchmentTextPrimary = LightConnectoColors.textPrimary  // Sumi Ink (#141312)
val ParchmentTextSecondary = LightConnectoColors.textSecondary // Warm Secondary (#44403C)
val ParchmentTextMuted = LightConnectoColors.textDisabled   // Archival Muted (#78716C)
val ParchmentAccent = LightConnectoColors.primary           // Sumi Ink Accent (#141312)
val ParchmentCyan = LightConnectoColors.info                // Cyan Accent (#0891B2)
val ParchmentEmerald = LightConnectoColors.success          // Emerald (#10B981)
val ParchmentGold = LightConnectoColors.warning             // Amber (#D97706)
val ParchmentDanger = LightConnectoColors.error             // Crimson (#DC2626)

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
val DarkAccentGradientColors = listOf(Color(0xFFFFFFFF), Color(0xFFA1A1AA))
val LightAccentGradientColors = listOf(Color(0xFF141312), Color(0xFF292524))

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
    return if (isAppInLightTheme()) Color.White else Color.Black
}
