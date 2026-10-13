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
    val isLiquidGlass: Boolean = true
)

enum class ThemeMode(val id: String, val displayName: String, val subtitle: String, val iconEmoji: String) {
    NOIR("noir", "Pure Black OLED", "0-nit pitch black luxury with transparent liquid obsidian refraction", "🌙"),
    EMERALD("emerald", "Emerald Matrix", "Tactical deep obsidian with transparent liquid matrix emerald refraction", "💎"),
    MIDNIGHT("midnight", "Midnight Navy", "Deep oceanic navy with transparent liquid azure refraction", "🌌"),
    CYBERPUNK("cyberpunk", "Cyberpunk Neon", "High-voltage neon purple & cyan transparent liquid glass glow", "⚡"),
    PARCHMENT("parchment", "Ivory Paper", "Warm archival light parchment with translucent liquid glass sheen", "☀️");

    val isDark: Boolean
        get() = this != PARCHMENT

    val isGlass: Boolean
        get() = true

    val isLiquidGlass: Boolean
        get() = true

    companion object {
        fun fromId(id: String?): ThemeMode {
            if (id.equals("liquid_glass", ignoreCase = true)) return MIDNIGHT
            if (id.equals("glass", ignoreCase = true)) return MIDNIGHT
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
    isLiquidGlass = true
)

val DarkConnectoColors = ConnectoColors(
    primary = Color(0xFFFFFFFF),          // Pure White Crisp Accent
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0x26FFFFFF), // Translucent White Glass Highlight (15% alpha)
    onPrimaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF000000),       // OLED True 0-nit Pure Black (#000000)
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0x1F222228),          // Ultra-Translucent Obsidian Crystal Glass (12% alpha)
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0x382C2C34),   // Elevated Translucent Obsidian Layer (22% alpha)
    onSurfaceVariant = Color(0xFFD4D4D8), // Secondary Text (#D4D4D8)
    textPrimary = Color(0xFFFFFFFF),      // Pure Crisp White (#FFFFFF)
    textSecondary = Color(0xFFD4D4D8),    // Normal Body Text (#D4D4D8)
    textDisabled = Color(0xFF8E8E93),     // Muted Gray (#8E8E93)
    border = Color(0x4DFFFFFF),           // Specular White Rim Highlight (30% alpha)
    borderSubtle = Color(0x26FFFFFF),     // Subtle Translucent Rim (15% alpha)
    success = Color(0xFF34D399),          // Emerald 400
    onSuccess = Color(0xFF064E3B),
    warning = Color(0xFFFBBF24),          // Amber 400
    onWarning = Color(0xFF78350F),
    error = Color(0xFFF87171),            // Red 400
    onError = Color(0xFF7F1D1D),
    info = Color(0xFF38BDF8),             // Sky Cyan Accent
    onInfo = Color(0xFF083344),
    isDark = true,
    isGlass = true,
    isLiquidGlass = true
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

fun getColorsForTheme(themeMode: ThemeMode): ConnectoColors {
    return when (themeMode) {
        ThemeMode.NOIR -> DarkConnectoColors
        ThemeMode.PARCHMENT -> LightConnectoColors
        ThemeMode.CYBERPUNK -> CyberpunkConnectoColors
        ThemeMode.MIDNIGHT -> MidnightConnectoColors
        ThemeMode.EMERALD -> EmeraldConnectoColors
    }
}

val LocalConnectoColors = staticCompositionLocalOf { DarkConnectoColors }
