package com.example.connecto.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

enum class ConnectoAvatarSize(val dp: Dp, val fontSize: Int, val statusDotSize: Dp) {
    XS(24.dp, 10, 6.dp),
    SM(32.dp, 12, 8.dp),
    MD(40.dp, 15, 10.dp),
    LG(48.dp, 18, 12.dp),
    XL(64.dp, 22, 14.dp),
    XXL(80.dp, 28, 16.dp)
}

enum class ConnectoPresenceStatus {
    ONLINE,
    IDLE,
    DND,
    OFFLINE
}

data class CoverPreset(
    val id: String,
    val name: String,
    val subtitle: String,
    val brush: Brush,
    val previewColors: List<Color>
)

val COVER_PRESETS = listOf(
    CoverPreset(
        id = "preset_aurora",
        name = "Aurora Borealis",
        subtitle = "Emerald Deep Glow",
        brush = Brush.linearGradient(listOf(Color(0xFF090D16), Color(0xFF064E3B), Color(0xFF083344))),
        previewColors = listOf(Color(0xFF090D16), Color(0xFF064E3B), Color(0xFF083344))
    ),
    CoverPreset(
        id = "preset_space",
        name = "Cosmic Nebula",
        subtitle = "Starlight & Violet",
        brush = Brush.linearGradient(listOf(Color(0xFF03001E), Color(0xFF7303C0), Color(0xFFEC38BC))),
        previewColors = listOf(Color(0xFF03001E), Color(0xFF7303C0), Color(0xFFEC38BC))
    ),
    CoverPreset(
        id = "preset_sunset",
        name = "Golden Sunset",
        subtitle = "Amber Warm Twilight",
        brush = Brush.linearGradient(listOf(Color(0xFF31103F), Color(0xFF831843), Color(0xFFF59E0B))),
        previewColors = listOf(Color(0xFF31103F), Color(0xFF831843), Color(0xFFF59E0B))
    ),
    CoverPreset(
        id = "preset_cyberpunk",
        name = "Cyber Neon",
        subtitle = "Matrix Teal & Synthwave",
        brush = Brush.linearGradient(listOf(Color(0xFF0D0A1A), Color(0xFF00CEC9), Color(0xFF6C5CE7))),
        previewColors = listOf(Color(0xFF0D0A1A), Color(0xFF00CEC9), Color(0xFF6C5CE7))
    ),
    CoverPreset(
        id = "preset_matrix",
        name = "Matrix Emerald",
        subtitle = "Digital Cyber Rain",
        brush = Brush.linearGradient(listOf(Color(0xFF022C22), Color(0xFF065F46), Color(0xFF0F172A))),
        previewColors = listOf(Color(0xFF022C22), Color(0xFF065F46), Color(0xFF0F172A))
    ),
    CoverPreset(
        id = "preset_carbon",
        name = "Obsidian Carbon",
        subtitle = "Minimalist Stealth",
        brush = Brush.linearGradient(listOf(Color(0xFF18181B), Color(0xFF27272A), Color(0xFF09090B))),
        previewColors = listOf(Color(0xFF18181B), Color(0xFF27272A), Color(0xFF09090B))
    ),
    CoverPreset(
        id = "preset_gold",
        name = "Royal Gold",
        subtitle = "Hokage Luxury",
        brush = Brush.linearGradient(listOf(Color(0xFF1C1917), Color(0xFF78350F), Color(0xFFCA8A04))),
        previewColors = listOf(Color(0xFF1C1917), Color(0xFF78350F), Color(0xFFCA8A04))
    )
)

fun isCoverPreset(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val clean = url.trim().lowercase()
    return COVER_PRESETS.any { it.id == clean } || clean.startsWith("preset_")
}

fun getCoverPreset(id: String?): CoverPreset? {
    if (id.isNullOrBlank()) return null
    val clean = id.trim().lowercase()
    return COVER_PRESETS.firstOrNull { it.id == clean }
}

/**
 * Sanitizes avatar URL by rejecting null strings, emoji fallbacks, and prepending base URL for relative paths.
 */
fun sanitizeAvatarUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    val trimmed = url.trim()
    val lower = trimmed.lowercase()
    if (lower in listOf("null", "none", "undefined", "false", "true", "")) return null
    if (isCoverPreset(trimmed)) return trimmed
    // Reject plain emoji strings or non-path strings (unless data: URL)
    if (!trimmed.contains("/") && !trimmed.contains(".") && !trimmed.startsWith("data:")) return null
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    if (trimmed.startsWith("file://") || trimmed.startsWith("content://") || trimmed.startsWith("data:")) return trimmed
    // Local device file path
    if (trimmed.startsWith("/data/") || trimmed.startsWith("/storage/") || java.io.File(trimmed).exists()) return trimmed
    if (trimmed.startsWith("/")) return "${com.example.connecto.network.ConnectoNetworkConfig.activeBaseUrl}$trimmed"
    if (trimmed.startsWith("uploads/")) return "${com.example.connecto.network.ConnectoNetworkConfig.activeBaseUrl}/$trimmed"
    return trimmed
}

private val base64DecodedCache = java.util.concurrent.ConcurrentHashMap<String, android.graphics.Bitmap>()

/**
 * Resolves a model suitable for Coil's AsyncImage from a remote URL, local file path, file URI, or data:base64 URL.
 */
fun resolveImageModel(url: String?): Any? {
    if (url.isNullOrBlank()) return null
    val sanitized = sanitizeAvatarUrl(url) ?: return null
    if (sanitized.startsWith("data:")) {
        val cached = base64DecodedCache[sanitized]
        if (cached != null) return cached
        try {
            val commaIdx = sanitized.indexOf(",")
            if (commaIdx != -1) {
                val base64Data = sanitized.substring(commaIdx + 1)
                val decoded = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                if (decoded != null && decoded.isNotEmpty()) {
                    val bmp = android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                    if (bmp != null) {
                        base64DecodedCache[sanitized] = bmp
                        return bmp
                    }
                }
            }
        } catch (e: Exception) {
            // fallback to sanitized string
        }
    }
    if (sanitized.startsWith("file://")) {
        return java.io.File(sanitized.removePrefix("file://"))
    }
    if (sanitized.startsWith("/data/") || sanitized.startsWith("/storage/") || (sanitized.startsWith("/") && java.io.File(sanitized).exists())) {
        return java.io.File(sanitized)
    }
    return sanitized
}

/**
 * Generates deterministic vibrant 2-color gradient from username/name hash.
 */
fun getAvatarGradient(name: String): Brush {
    val palette = listOf(
        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)), // Indigo to Purple
        listOf(Color(0xFF3B82F6), Color(0xFF06B6D4)), // Blue to Cyan
        listOf(Color(0xFF10B981), Color(0xFF059669)), // Emerald to Teal
        listOf(Color(0xFFF59E0B), Color(0xFFEF4444)), // Amber to Red
        listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)), // Pink to Purple
        listOf(Color(0xFF8B5CF6), Color(0xFFD946EF)), // Purple to Fuchsia
        listOf(Color(0xFF14B8A6), Color(0xFF0284C7)), // Teal to Sky
        listOf(Color(0xFFF97316), Color(0xFFE11D48)), // Orange to Rose
        listOf(Color(0xFF0EA5E9), Color(0xFF6366F1))  // Sky to Indigo
    )
    val hash = kotlin.math.abs(name.trim().lowercase().hashCode())
    val colors = palette[hash % palette.size]
    return Brush.linearGradient(colors)
}

/**
 * Extracts 1-2 letter uppercase monogram from name.
 */
fun extractAvatarInitials(name: String): String {
    val clean = name.trim().removePrefix("@")
    if (clean.isBlank()) return "C"
    val parts = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
    return if (parts.size >= 2) {
        val firstChar = parts[0].firstOrNull()?.uppercaseChar()?.toString() ?: ""
        val secondChar = parts[1].firstOrNull()?.uppercaseChar()?.toString() ?: ""
        if (firstChar.isNotEmpty() && secondChar.isNotEmpty()) "$firstChar$secondChar" else clean.take(1).uppercase()
    } else {
        clean.take(1).uppercase()
    }
}

/**
 * Reusable Connecto Avatar component adhering to the Centralized Design System.
 * Renders avatar image with graceful initials monogram fallback and accessible presence indicator.
 */
@Composable
fun ConnectoAvatar(
    name: String,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    size: ConnectoAvatarSize = ConnectoAvatarSize.MD,
    customSizeDp: Dp? = null,
    customFontSizeSp: Int? = null,
    status: ConnectoPresenceStatus? = null,
    borderWidth: Dp = 1.dp,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalConnectoColors.current
    val effectiveSizeDp = customSizeDp ?: size.dp
    val effectiveFontSize = (customFontSizeSp ?: size.fontSize).sp
    val initial = extractAvatarInitials(name)
    val sanitizedUrl = sanitizeAvatarUrl(avatarUrl)

    val statusColor = when (status) {
        ConnectoPresenceStatus.ONLINE -> colors.success
        ConnectoPresenceStatus.IDLE -> colors.warning
        ConnectoPresenceStatus.DND -> colors.error
        ConnectoPresenceStatus.OFFLINE, null -> colors.textDisabled
    }

    Box(
        modifier = modifier
            .size(effectiveSizeDp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        val borderModifier = when {
            borderBrush != null -> Modifier.border(borderWidth, borderBrush, CircleShape)
            borderColor != null -> Modifier.border(borderWidth, borderColor, CircleShape)
            else -> Modifier.border(borderWidth, colors.borderSubtle, CircleShape)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(getAvatarGradient(name))
                .then(borderModifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = effectiveFontSize,
                fontWeight = FontWeight.Bold
            )
            val imageModel = resolveImageModel(avatarUrl) ?: sanitizedUrl
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "$name's avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }

        if (status != null) {
            val dotSize = if (customSizeDp != null) (customSizeDp * 0.25f).coerceIn(6.dp, 16.dp) else size.statusDotSize
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(statusColor)
                    .border(1.5.dp, colors.surface, CircleShape)
            )
        }
    }
}

