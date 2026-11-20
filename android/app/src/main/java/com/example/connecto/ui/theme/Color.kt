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

// Light Edition: Pure White (Optimized Clean White)
val ParchmentCanvas = LightConnectoColors.background       // Pure White Canvas (#F8FAFC)
val ParchmentSurface = LightConnectoColors.surface         // Crisp White (#FFFFFF)
val ParchmentSurfaceVariant = LightConnectoColors.surfaceVariant // Level 1 Crisp Surface (#F1F5F9)
val ParchmentElevated = LightConnectoColors.primaryContainer    // Level 2 Crisp Surface (#E2E8F0)
val ParchmentBorder = LightConnectoColors.border           // Clean Slate Border (#E2E8F0)
val ParchmentBorderSubtle = LightConnectoColors.borderSubtle // Subtle Border (#F1F5F9)
val ParchmentTextPrimary = LightConnectoColors.textPrimary  // Deep Charcoal Slate (#0F172A)
val ParchmentTextSecondary = LightConnectoColors.textSecondary // Slate 600 Secondary (#475569)
val ParchmentTextMuted = LightConnectoColors.textDisabled   // Slate 400 Muted (#94A3B8)
val ParchmentAccent = LightConnectoColors.primary           // Crisp High-Contrast Accent (#0F172A)
val ParchmentCyan = LightConnectoColors.info                // Sky Azure (#0284C7)
val ParchmentEmerald = LightConnectoColors.success          // Emerald (#10B981)
val ParchmentGold = LightConnectoColors.warning             // Amber Alert (#D97706)
val ParchmentDanger = LightConnectoColors.error             // Crimson (#DC2626)

// Standard Semantic Aliases
val BackgroundColor = StitchCanvas
val SurfaceColor = StitchSurface
val SurfaceElevatedColor = StitchElevated
val BorderColor = StitchRim

val CyberCyan = StitchCyan
val NeonPurple = StitchAccent
val NeonMagenta = Color(0xFFE4E4E7)
val OnlineGreen = StitchEmerald

val TextPrimaryColor = DarkConnectoColors.textPrimary
val TextSecondaryColor = DarkConnectoColors.textSecondary
val TextDisabledColor = DarkConnectoColors.textDisabled

// Accent Gradients (Pure OLED Monochrome & Parchment Slate - Flat Solid Fills, Zero Color Gradients)
val DarkAccentGradientColors = listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF))
val LightAccentGradientColors = listOf(Color(0xFF0F172A), Color(0xFF0F172A))

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

// Theme-Aware Dynamic Gradient Helper (Zero Gradient - Pure Solid White in Dark Theme)
@Composable
fun getDynamicAccentGradientColors(): List<Color> {
    return if (isAppInLightTheme()) {
        listOf(Color(0xFF0F172A), Color(0xFF0F172A))
    } else {
        listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF))
    }
}

// Bulletproof Light/Dark Theme Detector
@Composable
fun isAppInLightTheme(): Boolean {
    return !com.example.connecto.ui.designsystem.ConnectoTheme.colors.isDark
}

@Composable
fun getContentColorOnAccentGradient(): Color {
    return if (isAppInLightTheme()) Color.White else Color(0xFF000000)
}
