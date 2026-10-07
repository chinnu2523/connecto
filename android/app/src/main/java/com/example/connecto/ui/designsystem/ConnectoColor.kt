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
    val isDark: Boolean
)

enum class ThemeMode(val id: String, val displayName: String, val subtitle: String, val iconEmoji: String) {
    NOIR("noir", "Pure Black OLED", "0-nit pitch black luxury with crisp white accents", "🌙"),
    PARCHMENT("parchment", "Ivory Paper", "Warm archival light parchment with sumi ink", "☀️"),
    CYBERPUNK("cyberpunk", "Cyberpunk Neon", "High-voltage neon purple & cyan night city glow", "⚡"),
    MIDNIGHT("midnight", "Midnight Navy", "Deep oceanic navy with electric azure & cobalt", "🌌"),
    EMERALD("emerald", "Emerald Matrix", "Tactical deep obsidian with luminous matrix emerald", "💎");

    val isDark: Boolean
        get() = this != PARCHMENT

    companion object {
        fun fromId(id: String?): ThemeMode {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NOIR
        }
    }
}

val LightConnectoColors = ConnectoColors(
    primary = Color(0xFF141312),          // Sumi Ink Accent
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEBE6DC), // Level 2 Warm Archival Surface
    onPrimaryContainer = Color(0xFF141312),
    background = Color(0xFFFBF8F2),       // Ivory Paper Canvas (#FBF8F2)
    onBackground = Color(0xFF141312),
    surface = Color(0xFFFFFFFF),          // Pure White Card Surface (#FFFFFF)
    onSurface = Color(0xFF141312),
    surfaceVariant = Color(0xFFF5F1E8),   // Level 1 Warm Surface (#F5F1E8)
    onSurfaceVariant = Color(0xFF44403C), // Warm Secondary Ink (#44403C)
    textPrimary = Color(0xFF141312),      // Deep Ink Header (#141312)
    textSecondary = Color(0xFF44403C),    // Normal Body Ink (#44403C)
    textDisabled = Color(0xFF78716C),     // Muted Archival Ink (#78716C)
    border = Color(0xFFE7E2D8),           // Archival Border (#E7E2D8)
    borderSubtle = Color(0xFFF0ECE4),     // Subtle Archival Border (#F0ECE4)
    success = Color(0xFF10B981),          // Emerald 500
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFD97706),          // Warm Amber 600
    onWarning = Color(0xFFFFFFFF),
    error = Color(0xFFDC2626),            // Crimson 600
    onError = Color(0xFFFFFFFF),
    info = Color(0xFF0891B2),             // Cyan 600
    onInfo = Color(0xFFFFFFFF),
    isDark = false
)

val DarkConnectoColors = ConnectoColors(
    primary = Color(0xFFFFFFFF),          // Crisp White Noir Accent
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF141418), // Deep Elevated Dark Container
    onPrimaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF000000),       // OLED True 0-nit Pure Black (#000000)
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF080808),          // Deep Modal & Nav Base (#080808)
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF0A0A0A),   // Obsidian Card Surface (#0A0A0A)
    onSurfaceVariant = Color(0xFFD4D4D8), // Secondary Text (#D4D4D8)
    textPrimary = Color(0xFFFFFFFF),      // Pure Crisp White (#FFFFFF)
    textSecondary = Color(0xFFD4D4D8),    // Normal Body Text (#D4D4D8)
    textDisabled = Color(0xFF8E8E93),     // Muted Gray (#8E8E93)
    border = Color(0xFF222228),           // Sleek 1dp OLED Border (#222228)
    borderSubtle = Color(0xFF16161C),     // Hairline OLED Border (#16161C)
    success = Color(0xFF34D399),          // Emerald 400
    onSuccess = Color(0xFF064E3B),
    warning = Color(0xFFFBBF24),          // Amber 400
    onWarning = Color(0xFF78350F),
    error = Color(0xFFF87171),            // Red 400
    onError = Color(0xFF7F1D1D),
    info = Color(0xFF22D3EE),             // Cyber Cyan 400
    onInfo = Color(0xFF083344),
    isDark = true
)

val CyberpunkConnectoColors = ConnectoColors(
    primary = Color(0xFFEC4899),          // Neon Fuchsia / Pink 500
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF2A123D), // Deep Violet Glow Container
    onPrimaryContainer = Color(0xFFFDF2F8),
    background = Color(0xFF0D0A1A),       // Deep Cyberpunk Void (#0D0A1A)
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF15102A),          // Dark Synthwave Surface (#15102A)
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E173B),   // Electric Violet Elevation (#1E173B)
    onSurfaceVariant = Color(0xFFC084FC), // Lavender / Purple Accent
    textPrimary = Color(0xFFF8FAFC),      // Bright Neon White
    textSecondary = Color(0xFFE2E8F0),    // Crisp Subtitle
    textDisabled = Color(0xFF818CF8),     // Indigo Muted
    border = Color(0xFF3B2768),           // Violet Border (#3B2768)
    borderSubtle = Color(0xFF271A46),     // Subtle Border
    success = Color(0xFF06B6D4),          // Cyber Cyan (#06B6D4)
    onSuccess = Color(0xFF083344),
    warning = Color(0xFFF59E0B),          // Electric Amber
    onWarning = Color(0xFF78350F),
    error = Color(0xFFEF4444),            // Laser Red
    onError = Color(0xFFFFFFFF),
    info = Color(0xFF38BDF8),             // Electric Cyan Accent (#38BDF8)
    onInfo = Color(0xFF0C4A6E),
    isDark = true
)

val MidnightConnectoColors = ConnectoColors(
    primary = Color(0xFF38BDF8),          // Electric Sky Blue 400
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF0C2442), // Deep Ocean Trench Container
    onPrimaryContainer = Color(0xFFE0F2FE),
    background = Color(0xFF050B14),       // Deep Abyssal Navy (#050B14)
    onBackground = Color(0xFFF0F9FF),
    surface = Color(0xFF0A1526),          // Midnight Hull Surface (#0A1526)
    onSurface = Color(0xFFF0F9FF),
    surfaceVariant = Color(0xFF0E1E36),   // Deep Cobalt Surface (#0E1E36)
    onSurfaceVariant = Color(0xFF93C5FD), // Soft Azure Text (#93C5FD)
    textPrimary = Color(0xFFF0F9FF),      // Crisp Starlight White
    textSecondary = Color(0xFFBAE6FD),    // Pale Sky Blue
    textDisabled = Color(0xFF64748B),     // Slate Gray
    border = Color(0xFF1E3A5F),           // Deep Oceanic Border (#1E3A5F)
    borderSubtle = Color(0xFF132842),     // Subtle Abyss Border
    success = Color(0xFF10B981),          // Seafoam Emerald
    onSuccess = Color(0xFF022C22),
    warning = Color(0xFFF59E0B),          // Solar Flare Gold
    onWarning = Color(0xFF78350F),
    error = Color(0xFFFB7185),            // Rose Red
    onError = Color(0xFF4C0519),
    info = Color(0xFF60A5FA),             // Azure 400 (#60A5FA)
    onInfo = Color(0xFF172554),
    isDark = true
)

val EmeraldConnectoColors = ConnectoColors(
    primary = Color(0xFF10B981),          // Luminous Matrix Emerald 500
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF063327), // Jade Terminal Container
    onPrimaryContainer = Color(0xFFD1FAE5),
    background = Color(0xFF040D09),       // Deep Matrix Black-Green (#040D09)
    onBackground = Color(0xFFECFDF5),
    surface = Color(0xFF081812),          // Obsidian Jade Surface (#081812)
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = Color(0xFF0D251C),   // Elevated Terminal Base (#0D251C)
    onSurfaceVariant = Color(0xFF6EE7B7), // Mint Green Text (#6EE7B7)
    textPrimary = Color(0xFFECFDF5),      // Terminal Green White
    textSecondary = Color(0xFFA7F3D0),    // Soft Mint
    textDisabled = Color(0xFF4B6E5F),     // Camo Olive Muted
    border = Color(0xFF144D39),           // Emerald Glow Border (#144D39)
    borderSubtle = Color(0xFF0D3325),     // Subtle Foliage Border
    success = Color(0xFF34D399),          // Emerald 400
    onSuccess = Color(0xFF064E3B),
    warning = Color(0xFFFBBF24),          // Warning Gold
    onWarning = Color(0xFF78350F),
    error = Color(0xFFF87171),            // Alert Red
    onError = Color(0xFF7F1D1D),
    info = Color(0xFF2DD4BF),             // Teal 400 (#2DD4BF)
    onInfo = Color(0xFF134E4A),
    isDark = true
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
