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
    NOIR("noir", "Black Theme", "Pure pitch black OLED • Battery saver", "🌑"),
    PARCHMENT("parchment", "White Theme", "Clean crisp white • Daylight readability", "☀️"),
    LIQUID_GLASS("liquid_glass", "Liquid Glass", "Translucent glass with optical blur", "🪞"),
    EMERALD("emerald", "Emerald Matrix", "Obsidian black with emerald glow", "💎"),
    MIDNIGHT("midnight", "Midnight Navy", "Deep ocean blue with azure tint", "🌌"),
    CYBERPUNK("cyberpunk", "Cyberpunk Neon", "High-contrast purple & cyan neon", "⚡"),
    MOSS_EMERALD("moss_emerald", "Jet Black & Moss", "Jet Black • Muted Moss Green • Neon Emerald", "🌿"),
    MINT_RED("mint_red", "Mint & Vivid Red", "Mint Green • Pure Black • Vivid Red", "🍃"),
    FIERY_LIME("fiery_lime", "Fiery Orange & Lime", "Fiery Orange • Sunshine Yellow • Lime Green", "🔥"),
    MOCHA_ELECTRIC("mocha_electric", "Mocha & Electric Blue", "Mocha Brown • Vanilla Cream • Electric Blue", "☕"),
    BUTTER_INK("butter_ink", "Butter & Ink", "Butter Yellow • Ink Black • Lipstick Red", "🧈"),
    OLIVE_BUBBLEGUM("olive_bubblegum", "Olive & Bubblegum", "Olive Drab • Linen White • Bubblegum Pink", "🫒"),
    CHARCOAL_CRIMSON("charcoal_crimson", "Charcoal Crimson", "Charcoal Black • Forest Green • Crimson Red", "🎯"),
    LEMON_LAVENDER("lemon_lavender", "Lemon & Lavender", "Lemon • Charcoal Black • Electric Lavender", "🍋"),
    MOCHA_GOLD("mocha_gold", "Mocha & Gold", "Mocha Brown • Champagne Gold • Burnt Amber", "👑");

    val isDark: Boolean
        get() = this != PARCHMENT && this != MINT_RED && this != BUTTER_INK && this != LEMON_LAVENDER

    val isGlass: Boolean
        get() = this == LIQUID_GLASS || this == MIDNIGHT || this == EMERALD || this == CYBERPUNK

    val isLiquidGlass: Boolean
        get() = this == LIQUID_GLASS

    companion object {
        fun fromId(id: String?): ThemeMode {
            if (id.equals("parchment", ignoreCase = true) || id.equals("white", ignoreCase = true) || id.equals("light", ignoreCase = true)) {
                return PARCHMENT
            }
            return NOIR
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
    val clampedAlpha = glassAlpha.coerceIn(0.0f, 1.0f)

    val surfaceColor = if (clampedAlpha <= 0.005f) {
        Color.Transparent
    } else {
        Color.White.copy(alpha = (clampedAlpha * 0.55f).coerceIn(0.01f, 0.70f))
    }

    val surfaceVariantColor = if (clampedAlpha <= 0.005f) {
        Color.Transparent
    } else {
        Color.White.copy(alpha = (clampedAlpha * 0.35f).coerceIn(0.01f, 0.45f))
    }

    val primaryContainerColor = if (clampedAlpha <= 0.005f) {
        Color(0x08FFFFFF)
    } else {
        Color.White.copy(alpha = (clampedAlpha * 0.28f).coerceIn(0.02f, 0.35f))
    }

    // Specular border glare: crisp pure white specular rim, no color cast
    val rimAlpha = if (clampedAlpha <= 0.005f) 0.65f else (0.75f - (clampedAlpha * 0.20f)).coerceIn(0.35f, 0.85f)
    val borderCol = Color.White.copy(alpha = rimAlpha)
    val borderSubtleCol = Color.White.copy(alpha = (rimAlpha * 0.40f).coerceIn(0.12f, 0.40f))

    return LiquidGlassConnectoColors.copy(
        surface = surfaceColor,
        surfaceVariant = surfaceVariantColor,
        primaryContainer = primaryContainerColor,
        border = borderCol,
        borderSubtle = borderSubtleCol,
        glassAlpha = clampedAlpha
    )
}

val MossEmeraldConnectoColors = ConnectoColors(
    primary = Color(0xFF00FF88),          // 10% Neon Emerald Green
    onPrimary = Color(0xFF052E16),
    primaryContainer = Color(0x3300FF88),
    onPrimaryContainer = Color(0xFFECFDF5),
    background = Color(0xFF0A0D0A),       // 60% Jet Black
    onBackground = Color(0xFFF0FDF4),
    surface = Color(0xFF1E2B20),          // 30% Muted Moss Green
    onSurface = Color(0xFFF0FDF4),
    surfaceVariant = Color(0xFF283A2B),
    onSurfaceVariant = Color(0xFFA7F3D0),
    textPrimary = Color(0xFFF0FDF4),
    textSecondary = Color(0xFF86EFAC),
    textDisabled = Color(0xFF4B634E),
    border = Color(0xFF35543A),
    borderSubtle = Color(0xFF1E2B20),
    success = Color(0xFF00FF88),
    onSuccess = Color(0xFF052E16),
    warning = Color(0xFFFBBF24),
    onWarning = Color(0xFF451A03),
    error = Color(0xFFEF4444),
    onError = Color(0xFF450A0A),
    info = Color(0xFF34D399),
    onInfo = Color(0xFF064E3B),
    isDark = true,
    isGlass = false,
    isLiquidGlass = false
)

val MintRedConnectoColors = ConnectoColors(
    primary = Color(0xFFEF4444),          // 10% Vivid Red
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x26EF4444),
    onPrimaryContainer = Color(0xFF7F1D1D),
    background = Color(0xFFD1FAE5),       // 60% Mint Green
    onBackground = Color(0xFF064E3B),
    surface = Color(0xFF09090B),          // 30% Pure Black Cards
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF18181B),
    onSurfaceVariant = Color(0xFFA1A1AA),
    textPrimary = Color(0xFF064E3B),
    textSecondary = Color(0xFF047857),
    textDisabled = Color(0xFF6EE7B7),
    border = Color(0x33064E3B),
    borderSubtle = Color(0x1A064E3B),
    success = Color(0xFF10B981),
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFF59E0B),
    onWarning = Color(0xFFFFFFFF),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    info = Color(0xFF0284C7),
    onInfo = Color(0xFFFFFFFF),
    isDark = false,
    isGlass = false,
    isLiquidGlass = false
)

val FieryLimeConnectoColors = ConnectoColors(
    primary = Color(0xFFA3E635),          // 10% Neon Lime Green
    onPrimary = Color(0xFF09090B),        // Jet Black
    primaryContainer = Color(0xFF09090B), // Jet Black container
    onPrimaryContainer = Color(0xFFA3E635),
    background = Color(0xFF9A3412),       // 60% Fiery Orange
    onBackground = Color(0xFFFFF7ED),
    surface = Color(0xFFFEF08A),          // 30% Sunshine Yellow
    onSurface = Color(0xFF451A03),
    surfaceVariant = Color(0xFFFDE047),
    onSurfaceVariant = Color(0xFF78350F),
    textPrimary = Color(0xFFFFF7ED),
    textSecondary = Color(0xFFFED7AA),
    textDisabled = Color(0xFFEA580C),
    border = Color(0x66A3E635),
    borderSubtle = Color(0x4009090B),
    success = Color(0xFFA3E635),
    onSuccess = Color(0xFF1A2E05),
    warning = Color(0xFFFDE047),
    onWarning = Color(0xFF451A03),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    info = Color(0xFF38BDF8),
    onInfo = Color(0xFF082F49),
    isDark = true,
    isGlass = false,
    isLiquidGlass = false
)

val MochaElectricConnectoColors = ConnectoColors(
    primary = Color(0xFF00D2FF),          // 10% Electric Blue
    onPrimary = Color(0xFF002233),
    primaryContainer = Color(0x3300D2FF),
    onPrimaryContainer = Color(0xFFE0F7FA),
    background = Color(0xFF26150D),       // 60% Mocha Brown
    onBackground = Color(0xFFFDFBF7),
    surface = Color(0xFFFDFBF7),          // 30% Vanilla Cream
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFF5EFE6),
    onSurfaceVariant = Color(0xFF57534E),
    textPrimary = Color(0xFFFDFBF7),
    textSecondary = Color(0xFFD6C7B8),
    textDisabled = Color(0xFF786253),
    border = Color(0x6600D2FF),
    borderSubtle = Color(0x33D6C7B8),
    success = Color(0xFF10B981),
    onSuccess = Color(0xFF022C22),
    warning = Color(0xFFF59E0B),
    onWarning = Color(0xFF451A03),
    error = Color(0xFFEF4444),
    onError = Color(0xFF450A0A),
    info = Color(0xFF00D2FF),
    onInfo = Color(0xFF002233),
    isDark = true,
    isGlass = false,
    isLiquidGlass = false
)

val ButterInkConnectoColors = ConnectoColors(
    primary = Color(0xFFE11D48),          // 10% Lipstick Red
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x26E11D48),
    onPrimaryContainer = Color(0xFF881337),
    background = Color(0xFFFEF9C3),       // 60% Butter Yellow
    onBackground = Color(0xFF09090B),
    surface = Color(0xFF09090B),          // 30% Ink Black
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF18181B),
    onSurfaceVariant = Color(0xFFA1A1AA),
    textPrimary = Color(0xFF1C1917),
    textSecondary = Color(0xFF713F12),
    textDisabled = Color(0xFFA16207),
    border = Color(0x66E11D48),
    borderSubtle = Color(0x2609090B),
    success = Color(0xFF16A34A),
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFD97706),
    onWarning = Color(0xFFFFFFFF),
    error = Color(0xFFE11D48),
    onError = Color(0xFFFFFFFF),
    info = Color(0xFF0284C7),
    onInfo = Color(0xFFFFFFFF),
    isDark = false,
    isGlass = false,
    isLiquidGlass = false
)

val OliveBubblegumConnectoColors = ConnectoColors(
    primary = Color(0xFFFF69B4),          // 10% Bubblegum Pink
    onPrimary = Color(0xFF4A042B),
    primaryContainer = Color(0x33FF69B4),
    onPrimaryContainer = Color(0xFFFDF2F8),
    background = Color(0xFF353C19),       // 60% Olive Drab
    onBackground = Color(0xFFFAF0E6),
    surface = Color(0xFFFAF0E6),          // 30% Linen White
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFF0E5D8),
    onSurfaceVariant = Color(0xFF57534E),
    textPrimary = Color(0xFFFAF0E6),
    textSecondary = Color(0xFFD5D2B8),
    textDisabled = Color(0xFF737A4B),
    border = Color(0x66FF69B4),
    borderSubtle = Color(0x33FAF0E6),
    success = Color(0xFF22C55E),
    onSuccess = Color(0xFF052E16),
    warning = Color(0xFFFBBF24),
    onWarning = Color(0xFF451A03),
    error = Color(0xFFF43F5E),
    onError = Color(0xFF4C0519),
    info = Color(0xFFFF69B4),
    onInfo = Color(0xFF4A042B),
    isDark = true,
    isGlass = false,
    isLiquidGlass = false
)

val CharcoalCrimsonConnectoColors = ConnectoColors(
    primary = Color(0xFFDC2626),          // 10% Crimson Red
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0x33DC2626),
    onPrimaryContainer = Color(0xFFFEE2E2),
    background = Color(0xFF111315),       // 60% Charcoal Black
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF144D29),          // 30% Forest Green
    onSurface = Color(0xFFF0FDF4),
    surfaceVariant = Color(0xFF1B5E33),
    onSurfaceVariant = Color(0xFF86EFAC),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textDisabled = Color(0xFF64748B),
    border = Color(0x66DC2626),
    borderSubtle = Color(0x33144D29),
    success = Color(0xFF22C55E),
    onSuccess = Color(0xFF052E16),
    warning = Color(0xFFF59E0B),
    onWarning = Color(0xFF451A03),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    info = Color(0xFF38BDF8),
    onInfo = Color(0xFF082F49),
    isDark = true,
    isGlass = false,
    isLiquidGlass = false
)

val LemonLavenderConnectoColors = ConnectoColors(
    primary = Color(0xFFC084FC),          // 10% Electric Lavender
    onPrimary = Color(0xFF2E0238),
    primaryContainer = Color(0x26C084FC),
    onPrimaryContainer = Color(0xFF581C87),
    background = Color(0xFFFEF08A),       // 60% Lemon
    onBackground = Color(0xFF18181B),
    surface = Color(0xFF18181B),          // 30% Charcoal Black
    onSurface = Color(0xFFFAF5FF),
    surfaceVariant = Color(0xFF27272A),
    onSurfaceVariant = Color(0xFFD8B4FE),
    textPrimary = Color(0xFF18181B),
    textSecondary = Color(0xFF713F12),
    textDisabled = Color(0xFFA16207),
    border = Color(0x66C084FC),
    borderSubtle = Color(0x2618181B),
    success = Color(0xFF16A34A),
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFEAB308),
    onWarning = Color(0xFF422006),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    info = Color(0xFFC084FC),
    onInfo = Color(0xFF2E0238),
    isDark = false,
    isGlass = false,
    isLiquidGlass = false
)

val MochaGoldConnectoColors = ConnectoColors(
    primary = Color(0xFFD97706),          // 10% Burnt Amber
    onPrimary = Color(0xFF451A03),
    primaryContainer = Color(0x33D97706),
    onPrimaryContainer = Color(0xFFFEF3C7),
    background = Color(0xFF22120B),       // 60% Mocha Brown
    onBackground = Color(0xFFFFFBEB),
    surface = Color(0xFFD4AF37),          // 30% Champagne Gold
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFC59F2E),
    onSurfaceVariant = Color(0xFF451A03),
    textPrimary = Color(0xFFFFFBEB),
    textSecondary = Color(0xFFD6C3B3),
    textDisabled = Color(0xFF786253),
    border = Color(0x66D97706),
    borderSubtle = Color(0x33D4AF37),
    success = Color(0xFF10B981),
    onSuccess = Color(0xFF022C22),
    warning = Color(0xFFF59E0B),
    onWarning = Color(0xFF451A03),
    error = Color(0xFFEF4444),
    onError = Color(0xFF450A0A),
    info = Color(0xFFFBBF24),
    onInfo = Color(0xFF451A03),
    isDark = true,
    isGlass = false,
    isLiquidGlass = false
)

fun getColorsForTheme(themeMode: ThemeMode, glassAlpha: Float = 0.20f): ConnectoColors {
    return when (themeMode) {
        ThemeMode.NOIR -> DarkConnectoColors
        ThemeMode.LIQUID_GLASS -> getLiquidGlassColors(glassAlpha)
        ThemeMode.PARCHMENT -> LightConnectoColors
        ThemeMode.CYBERPUNK -> CyberpunkConnectoColors
        ThemeMode.MIDNIGHT -> MidnightConnectoColors
        ThemeMode.EMERALD -> EmeraldConnectoColors
        ThemeMode.MOSS_EMERALD -> MossEmeraldConnectoColors
        ThemeMode.MINT_RED -> MintRedConnectoColors
        ThemeMode.FIERY_LIME -> FieryLimeConnectoColors
        ThemeMode.MOCHA_ELECTRIC -> MochaElectricConnectoColors
        ThemeMode.BUTTER_INK -> ButterInkConnectoColors
        ThemeMode.OLIVE_BUBBLEGUM -> OliveBubblegumConnectoColors
        ThemeMode.CHARCOAL_CRIMSON -> CharcoalCrimsonConnectoColors
        ThemeMode.LEMON_LAVENDER -> LemonLavenderConnectoColors
        ThemeMode.MOCHA_GOLD -> MochaGoldConnectoColors
    }
}

val LocalConnectoColors = staticCompositionLocalOf { DarkConnectoColors }
