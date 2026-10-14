package com.example.connecto.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Connecto Semantic Color System
 * Strictly synchronized with the Web Editorial Dual-Theme System:
 * - Edition 1: Pure Black OLED (Noir - 0-nit black canvas & crisp white/cyan typography)
 * - Edition 2: Ivory Paper (Parchment - archival paper canvas & Sumi ink typography)
 */
@Immutable
data class ConnectoColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val border: Color,
    val borderSubtle: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val error: Color,
    val onError: Color,
    val info: Color,
    val onInfo: Color,
    val isDark: Boolean,
    val isGlass: Boolean = true,
    val isLiquidGlass: Boolean = true,
    val glassAlpha: Float = 0.20f
)

enum class ThemeMode(val id: String, val displayName: String, val subtitle: String, val iconEmoji: String) {
    NOIR("noir", "True Dark (OLED)", "Pure neutral zero-nit obsidian black with crisp neutral typography", "🌑"),
    LIQUID_GLASS("liquid_glass", "Transparent Liquid Glass", "Apple iOS & VisionOS optical liquid glass with dynamic tint & refraction", "🪞"),
    EMERALD("emerald", "Emerald Matrix", "Tactical deep obsidian with transparent liquid matrix emerald refraction", "💎"),
    MIDNIGHT("midnight", "Midnight Navy", "Deep oceanic navy with transparent liquid azure refraction", "🌌"),
    CYBERPUNK("cyberpunk", "Cyberpunk Neon", "High-voltage neon purple & cyan transparent liquid glass glow", "⚡"),
    PARCHMENT("parchment", "Ivory Paper", "Warm archival light parchment with translucent liquid glass sheen", "☀️");

    val isDark: Boolean
        get() = this != PARCHMENT

    val isGlass: Boolean
        get() = this == LIQUID_GLASS || this == MIDNIGHT || this == EMERALD || this == CYBERPUNK

    val isLiquidGlass: Boolean
        get() = this == LIQUID_GLASS

    companion object {
        fun fromId(id: String?): ThemeMode {
            if (id.equals("liquid_glass", ignoreCase = true)) return LIQUID_GLASS
            if (id.equals("glass", ignoreCase = true)) return LIQUID_GLASS
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NOIR
        }
    }
}

val LightConnectoColors = ConnectoColors(
    primary = Color(0xFFB45309),          // Warm Amber Ink Accent (#B45309)
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x26B45309), // Translucent Amber Glass Container (15% alpha)
    onPrimaryContainer = Color(0xFF141312),
    background = Color(0xFFFBF8F2),       // Warm Archival Ivory Paper Canvas (#FBF8F2)
    onBackground = Color(0xFF141312),
    surface = Color(0x33FFFFFF),          // Crystal Translucent White Frosted Glass (20% alpha)
    onSurface = Color(0xFF141312),
    surfaceVariant = Color(0x59EBE3D5),   // Elevated Archival Glass Layer (35% alpha)
    onSurfaceVariant = Color(0xFF78350F), // Rich Sienna Muted Text (#78350F)
    textPrimary = Color(0xFF141312),      // Deep Ink Header (#141312)
    textSecondary = Color(0xFF57534E),    // Normal Body Ink (#57534E)
    textDisabled = Color(0xFFA8A29E),     // Muted Archival Ink (#A8A29E)
    border = Color(0x4D141312),           // Specular Sumi Ink Rim (30% alpha)
    borderSubtle = Color(0x26141312),     // Subtle Translucent Rim (15% alpha)
    success = Color(0xFF15803D),          // Forest Green
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFB45309),          // Warm Amber 600
    onWarning = Color(0xFFFFFFFF),
    error = Color(0xFFB91C1C),            // Crimson 600
    onError = Color(0xFFFFFFFF),
    info = Color(0xFF0284C7),             // Azure Sky
    onInfo = Color(0xFFFFFFFF),
    isDark = false,
    isGlass = true,
    isLiquidGlass = false,
    glassAlpha = 0.20f
)

val DarkConnectoColors = ConnectoColors(
    primary = Color(0xFFFAFAFA),          // Pure Crisp Monochrome White / Platinum Accent
    onPrimary = Color(0xFF09090B),
    primaryContainer = Color(0xFF27272A), // Neutral Zinc 800 Container
    onPrimaryContainer = Color(0xFFFAFAFA),
    background = Color(0xFF000000),       // Pure 0-nit OLED Black Canvas (Zero Blue/Cyan Tint)
    onBackground = Color(0xFFFAFAFA),
    surface = Color(0xFF121214),          // Sleek Neutral Dark Graphite Surface
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF1E1E22),   // Elevated Neutral Graphite Layer
    onSurfaceVariant = Color(0xFFA1A1AA), // Zinc 400 Neutral Silver Text
    textPrimary = Color(0xFFFFFFFF),      // Pure Crisp White
    textSecondary = Color(0xFFA1A1AA),    // Neutral Zinc 400 Silver (Crisp & Clean, No Blue Cast)
    textDisabled = Color(0xFF52525B),     // Zinc 600 Muted
    border = Color(0xFF27272A),           // Subtle Neutral Hairline Rim
    borderSubtle = Color(0xFF18181B),     // Very Subtle Inner Rim
    success = Color(0xFF22C55E),          // Emerald Green
    onSuccess = Color(0xFF052E16),
    warning = Color(0xFFF59E0B),          // Solar Flare Amber
    onWarning = Color(0xFF451A03),
    error = Color(0xFFEF4444),            // Crimson Red
    onError = Color(0xFF450A0A),
    info = Color(0xFF38BDF8),             // Sky Azure
    onInfo = Color(0xFF082F49),
    isDark = true,
    isGlass = false,
    isLiquidGlass = false,
    glassAlpha = 1.0f
)

val LiquidGlassConnectoColors = ConnectoColors(
    primary = Color(0xFFFFFFFF),          // Pure Crisp Specular White (Neutral glass)
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0x1FFFFFFF), // Translucent neutral glass sheen (12% alpha)
    onPrimaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF000000),       // Pure deep obsidian canvas (0-nit, no blue cast)
    onBackground = Color(0xFFFFFFFF),
    surface = Color.Transparent,          // Completely transparent (opacity 0)
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0x14FFFFFF),   // Translucent neutral glass layer (8% alpha)
    onSurfaceVariant = Color(0xFFE2E8F0), // Crisp Platinum / Frosted Silver Text
    textPrimary = Color(0xFFFFFFFF),      // Pure Pristine White
    textSecondary = Color(0xFFCBD5E1),    // Frosted Platinum
    textDisabled = Color(0xFF94A3B8),     // Muted Slate
    border = Color(0x66FFFFFF),           // Specular Pure White Rim (40% alpha)
    borderSubtle = Color(0x26FFFFFF),     // Subtle Glass Hairline (15% alpha)
    success = Color(0xFF10B981),          // Aqua Marine Green
    onSuccess = Color(0xFF022C22),
    warning = Color(0xFFF59E0B),          // Solar Flare Amber
    onWarning = Color(0xFF451A03),
    error = Color(0xFFEF4444),            // Coral Red
    onError = Color(0xFF4C0519),
    info = Color(0xFFFFFFFF),             // Pure Neutral Crystal Glass
    onInfo = Color(0xFF000000),
    isDark = true,
    isGlass = true,
    isLiquidGlass = true,
    glassAlpha = 0.0f
)

val CyberpunkConnectoColors = ConnectoColors(
    primary = Color(0xFFD946EF),          // Electric Neon Magenta Glow
    onPrimary = Color(0xFF2E0238),
    primaryContainer = Color(0x26D946EF), // Transparent Neon Magenta Container (15% alpha)
    onPrimaryContainer = Color(0xFFFDF4FF),
    background = Color(0xFF07040D),       // Abyssal Neon Night City Canvas (#07040D)
    onBackground = Color(0xFFFAF5FF),
    surface = Color(0x1FD946EF),          // Ultra-Translucent Neon Violet Hydro Glass (12% alpha)
    onSurface = Color(0xFFFAF5FF),
    surfaceVariant = Color(0x383B0764),   // Elevated Translucent Cyber Layer (22% alpha)
    onSurfaceVariant = Color(0xFFE879F9), // Soft Neon Fuchsia Text (#E879F9)
    textPrimary = Color(0xFFFAF5FF),      // Bright Neon White
    textSecondary = Color(0xFFC084FC),    // Lavender / Purple Accent
    textDisabled = Color(0xFF6B21A8),     // Indigo Muted
    border = Color(0x66D946EF),           // Specular Neon Magenta Rim (40% alpha)
    borderSubtle = Color(0x3306B6D4),     // Subtle Electric Cyan Hairline Rim (20% alpha)
    success = Color(0xFF22C55E),          // Cyber Green
    onSuccess = Color(0xFF052E16),
    warning = Color(0xFFFACC15),          // Electric Gold
    onWarning = Color(0xFF713F12),
    error = Color(0xFFFF2A85),            // Laser Pink Red
    onError = Color(0xFF4C0519),
    info = Color(0xFF06B6D4),             // Electric Neon Cyan (#06B6D4)
    onInfo = Color(0xFF083344),
    isDark = true,
    isGlass = true,
    isLiquidGlass = true
)

val MidnightConnectoColors = ConnectoColors(
    primary = Color(0xFF00E5FF),          // Fluid Electric Aqua Cyan (#00E5FF)
    onPrimary = Color(0xFF002233),
    primaryContainer = Color(0x2600E5FF), // Transparent Azure Liquid Container (15% alpha)
    onPrimaryContainer = Color(0xFFE0F7FA),
    background = Color(0xFF040A14),       // Deep Oceanic Midnight Canvas (#040A14)
    onBackground = Color(0xFFF0FDF4),
    surface = Color(0x1F00E5FF),          // Ultra-Translucent Liquid Cyan Acrylic Base (12% alpha)
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0x380C4A6E),   // Elevated Hydro-Glass Layer (22% alpha)
    onSurfaceVariant = Color(0xFF7DD3FC), // Water Ripple Text (#7DD3FC)
    textPrimary = Color(0xFFFFFFFF),      // Pure Water Drop White (#FFFFFF)
    textSecondary = Color(0xFFBAE6FD),    // Soft Cyan Aquatic Silver (#BAE6FD)
    textDisabled = Color(0xFF5B7A99),     // Deep Trench Slate (#5B7A99)
    border = Color(0x6600E5FF),           // Specular Aqua Rim (40% alpha)
    borderSubtle = Color(0x3300E5FF),     // Subtle Translucent Rim (20% alpha)
    success = Color(0xFF10B981),          // Aqua Marine Green (#10B981)
    onSuccess = Color(0xFF022C22),
    warning = Color(0xFFF59E0B),          // Solar Flare Amber (#F59E0B)
    onWarning = Color(0xFF451A03),
    error = Color(0xFFF43F5E),            // Coral Red (#F43F5E)
    onError = Color(0xFF4C0519),
    info = Color(0xFF38BDF8),             // Sky Liquid Azure (#38BDF8)
    onInfo = Color(0xFF083344),
    isDark = true,
    isGlass = true,
    isLiquidGlass = true
)

val EmeraldConnectoColors = ConnectoColors(
    primary = Color(0xFF10B981),          // Luminous Matrix Emerald 500
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0x2610B981), // Transparent Emerald Glass Container (15% alpha)
    onPrimaryContainer = Color(0xFFD1FAE5),
    background = Color(0xFF040D09),       // Deep Matrix Black-Green (#040D09)
    onBackground = Color(0xFFECFDF5),
    surface = Color(0x1F10B981),          // Ultra-Translucent Jade Hydro Glass Base (12% alpha)
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = Color(0x38064E3B),   // Elevated Translucent Jade Hydro Layer (22% alpha)
    onSurfaceVariant = Color(0xFF6EE7B7), // Mint Green Text (#6EE7B7)
    textPrimary = Color(0xFFECFDF5),      // Terminal Green White
    textSecondary = Color(0xFFA7F3D0),    // Soft Mint
    textDisabled = Color(0xFF4B6E5F),     // Camo Olive Muted
    border = Color(0x6610B981),           // Specular Emerald Rim (40% alpha)
    borderSubtle = Color(0x3310B981),     // Subtle Translucent Emerald Rim (20% alpha)
    success = Color(0xFF34D399),          // Emerald 400
    onSuccess = Color(0xFF064E3B),
    warning = Color(0xFFFBBF24),          // Warning Gold
    onWarning = Color(0xFF78350F),
    error = Color(0xFFF87171),            // Alert Red
    onError = Color(0xFF7F1D1D),
    info = Color(0xFF2DD4BF),             // Teal 400 (#2DD4BF)
    onInfo = Color(0xFF134E4A),
    isDark = true,
    isGlass = true,
    isLiquidGlass = true
)

/**
 * Dynamically constructs the Liquid Glass color system with continuous
 * interpolation from Ultra-Clear Transparent Liquid Glass (0% opacity, completely transparent)
 * to Frosted Specular Glass (neutral white refraction, zero unwanted colors).
 */
fun getLiquidGlassColors(glassAlpha: Float = 0.0f): ConnectoColors {
    val clampedAlpha = glassAlpha.coerceIn(0.0f, 0.90f)

    val surfaceColor = if (clampedAlpha <= 0.01f) {
        Color.Transparent
    } else {
        Color.White.copy(alpha = (clampedAlpha * 0.35f).coerceIn(0.02f, 0.40f))
    }

    val surfaceVariantColor = if (clampedAlpha <= 0.01f) {
        Color.Transparent
    } else {
        Color.White.copy(alpha = (clampedAlpha * 0.18f).coerceIn(0.01f, 0.22f))
    }

    // Specular border glare: crisp pure white specular rim, no color cast
    val rimAlpha = if (clampedAlpha <= 0.01f) 0.65f else (0.75f - (clampedAlpha * 0.30f)).coerceIn(0.35f, 0.85f)
    val borderCol = Color.White.copy(alpha = rimAlpha)
    val borderSubtleCol = Color.White.copy(alpha = (rimAlpha * 0.40f).coerceIn(0.12f, 0.40f))

    return LiquidGlassConnectoColors.copy(
        surface = surfaceColor,
        surfaceVariant = surfaceVariantColor,
        border = borderCol,
        borderSubtle = borderSubtleCol,
        glassAlpha = clampedAlpha
    )
}

fun getColorsForTheme(themeMode: ThemeMode, glassAlpha: Float = 0.20f): ConnectoColors {
    return when (themeMode) {
        ThemeMode.NOIR -> DarkConnectoColors
        ThemeMode.LIQUID_GLASS -> getLiquidGlassColors(glassAlpha)
        ThemeMode.PARCHMENT -> LightConnectoColors
        ThemeMode.CYBERPUNK -> CyberpunkConnectoColors
        ThemeMode.MIDNIGHT -> MidnightConnectoColors
        ThemeMode.EMERALD -> EmeraldConnectoColors
    }
}

val LocalConnectoColors = staticCompositionLocalOf { DarkConnectoColors }
