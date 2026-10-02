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

val LocalConnectoColors = staticCompositionLocalOf { DarkConnectoColors }
