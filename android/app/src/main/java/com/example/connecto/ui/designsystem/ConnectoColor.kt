package com.example.connecto.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Connecto Semantic Color System
 * Provides structured semantic colors for Light and Dark themes.
 * Clean, high-contrast, professional, and accessible.
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
    primary = Color(0xFF4F46E5),         // Refined Indigo 600
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEF2FF), // Indigo 50
    onPrimaryContainer = Color(0xFF312E81),
    background = Color(0xFFF8FAFC),       // Slate 50
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),          // Pure White
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),   // Slate 100
    onSurfaceVariant = Color(0xFF475569),
    textPrimary = Color(0xFF0F172A),      // Slate 900
    textSecondary = Color(0xFF64748B),    // Slate 500
    textDisabled = Color(0xFF94A3B8),     // Slate 400
    border = Color(0xFFE2E8F0),           // Slate 200
    borderSubtle = Color(0xFFF1F5F9),     // Slate 100
    success = Color(0xFF10B981),          // Emerald 500
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFF59E0B),          // Amber 500
    onWarning = Color(0xFFFFFFFF),
    error = Color(0xFFEF4444),            // Red 500
    onError = Color(0xFFFFFFFF),
    info = Color(0xFF3B82F6),             // Blue 500
    onInfo = Color(0xFFFFFFFF),
    isDark = false
)

val DarkConnectoColors = ConnectoColors(
    primary = Color(0xFF6366F1),          // Indigo 500 (Vibrant on dark)
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1E1B4B), // Deep Indigo 950
    onPrimaryContainer = Color(0xFFE0E7FF),
    background = Color(0xFF000000),       // Pure Black (OLED true 0-nit black)
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF08080A),          // Ultra-deep black surface (contrast against pure black canvas)
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF141418),   // Slightly elevated dark container
    onSurfaceVariant = Color(0xFFA1A1AA),
    textPrimary = Color(0xFFFFFFFF),      // Pure crisp white
    textSecondary = Color(0xFFA1A1AA),    // Slate/Zinc 400
    textDisabled = Color(0xFF52525B),     // Zinc 600
    border = Color(0xFF222228),           // Sleek 1dp OLED border
    borderSubtle = Color(0xFF16161C),     // Hairline OLED border
    success = Color(0xFF34D399),          // Emerald 400
    onSuccess = Color(0xFF064E3B),
    warning = Color(0xFFFBBF24),          // Amber 400
    onWarning = Color(0xFF78350F),
    error = Color(0xFFF87171),            // Red 400
    onError = Color(0xFF7F1D1D),
    info = Color(0xFF60A5FA),             // Blue 400
    onInfo = Color(0xFF1E3A8A),
    isDark = true
)

val LocalConnectoColors = staticCompositionLocalOf { LightConnectoColors }
