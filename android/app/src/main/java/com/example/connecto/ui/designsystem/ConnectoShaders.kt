package com.example.connecto.ui.designsystem

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * AGSL (Android Graphics Shading Language) Shader Source for Android 13+ (API 33+).
 * Generates an organic, fluid Dark Luxury & Liquid Glass volumetric aurora mesh.
 */
private const val AGSL_AURORA_SHADER = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform float4 color1;
    uniform float4 color2;
    uniform float4 color3;
    uniform float4 bg;

    float hash(float2 p) {
        return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
    }

    float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        float2 u = f * f * (3.0 - 2.0 * f);
        return mix(mix(hash(i + float2(0.0, 0.0)), hash(i + float2(1.0, 0.0)), u.x),
                   mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / iResolution;
        float t = iTime * 0.28;
        
        float n1 = noise(uv * 2.2 + float2(t * 0.35, t * 0.25));
        float n2 = noise(uv * 3.2 - float2(t * 0.28, t * 0.42) + n1 * 0.4);
        float n3 = noise(uv * 1.6 + float2(sin(t * 0.4) * 0.45, cos(t * 0.35) * 0.45));
        
        half4 finalCol = half4(bg);
        finalCol = mix(finalCol, half4(color1), half(smoothstep(0.18, 0.72, n1) * 0.55));
        finalCol = mix(finalCol, half4(color2), half(smoothstep(0.22, 0.80, n2) * 0.45));
        finalCol = mix(finalCol, half4(color3), half(smoothstep(0.28, 0.88, n3) * 0.38));
        
        // Specular ambient vignette
        float d = distance(uv, float2(0.5, 0.3));
        finalCol += half4(color1.rgb * (1.0 - smoothstep(0.0, 0.85, d)) * 0.12, 0.0);
        return finalCol;
    }
"""

/**
 * Master Ambient Shader Modifier.
 * Provides GPU-accelerated liquid glass & dark luxury volumetric lighting.
 * Uses AGSL on Android 13+ and hardware-accelerated animated gradient mesh on Android 7-12.
 */
fun Modifier.connectoAmbientShader(
    themeMode: ThemeMode = ThemeMode.LIQUID_GLASS,
    intensity: Float = 1.0f
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "ambientShaderClock")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f * 8f, // 8 full oscillation cycles
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shaderTime"
    )

    val colors = LocalConnectoColors.current
    val isDark = colors.isDark

    // Curated volumetric palette tuned for Dark Luxury & Liquid Glass
    val (color1, color2, color3, bgColor) = remember(themeMode, isDark) {
        when (themeMode) {
            ThemeMode.NOIR -> listOf(
                Color(0xFF27272A).copy(alpha = 0.40f), // Platinum Zinc
                Color(0xFF18181B).copy(alpha = 0.60f), // Obsidian
                Color(0xFF3F3F46).copy(alpha = 0.25f), // Sub-specular silver
                Color(0xFF000000)                      // Pure 0-nit black
            )
            ThemeMode.LIQUID_GLASS -> listOf(
                Color(0xFF00E5FF).copy(alpha = 0.38f), // Liquid Electric Cyan
                Color(0xFF3B82F6).copy(alpha = 0.32f), // Royal Blue Refraction
                Color(0xFF8B5CF6).copy(alpha = 0.28f), // Velvet Violet Specular
                Color(0xFF02040A)                      // Deep Obsidian Abyss
            )
            ThemeMode.MIDNIGHT -> listOf(
                Color(0xFF00E5FF).copy(alpha = 0.45f), // Electric Cyan
                Color(0xFF0284C7).copy(alpha = 0.40f), // Azure Sea
                Color(0xFF0369A1).copy(alpha = 0.35f), // Oceanic Deep
                Color(0xFF040A14)                      // Midnight Dark
            )
            ThemeMode.EMERALD -> listOf(
                Color(0xFF10B981).copy(alpha = 0.42f), // Emerald Glow
                Color(0xFF059669).copy(alpha = 0.38f), // Forest Matrix
                Color(0xFF047857).copy(alpha = 0.30f), // Deep Jade
                Color(0xFF020E08)                      // Obsidian Green
            )
            ThemeMode.CYBERPUNK -> listOf(
                Color(0xFFD946EF).copy(alpha = 0.45f), // Neon Magenta
                Color(0xFF06B6D4).copy(alpha = 0.40f), // Electric Cyan
                Color(0xFF8B5CF6).copy(alpha = 0.35f), // Neon Purple
                Color(0xFF07040D)                      // Cyber Void
            )
            ThemeMode.PARCHMENT -> listOf(
                Color(0xFFEBDCC9).copy(alpha = 0.65f), // Warm Amber Light
                Color(0xFFE5D5BC).copy(alpha = 0.50f), // Cream Sheen
                Color(0xFFF2EAD9).copy(alpha = 0.70f), // Soft Ivory
                Color(0xFFFBF8F2)                      // Archival Canvas
            )
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val runtimeShader = remember {
            try {
                android.graphics.RuntimeShader(AGSL_AURORA_SHADER)
            } catch (_: Exception) {
                null
            }
        }

        if (runtimeShader != null) {
            this.graphicsLayer {
                val w = size.width
                val h = size.height
                if (w > 0 && h > 0) {
                    runtimeShader.setFloatUniform("iResolution", w, h)
                    runtimeShader.setFloatUniform("iTime", time)
                    val a1 = (color1.alpha * intensity).coerceIn(0f, 1f)
                    val a2 = (color2.alpha * intensity).coerceIn(0f, 1f)
                    val a3 = (color3.alpha * intensity).coerceIn(0f, 1f)
                    runtimeShader.setFloatUniform("color1", color1.red, color1.green, color1.blue, a1)
                    runtimeShader.setFloatUniform("color2", color2.red, color2.green, color2.blue, a2)
                    runtimeShader.setFloatUniform("color3", color3.red, color3.green, color3.blue, a3)
                    runtimeShader.setFloatUniform("bg", bgColor.red, bgColor.green, bgColor.blue, bgColor.alpha)
                    
                    val renderEffect = android.graphics.RenderEffect.createShaderEffect(runtimeShader)
                    this.renderEffect = renderEffect.asComposeRenderEffect()
                }
            }.background(bgColor)
        } else {
            // Smooth Hardware-Accelerated Fallback
            this.drawBehind {
                drawVolumetricMeshFallback(time, color1, color2, color3, bgColor, intensity)
            }
        }
    } else {
        // High-performance Canvas Fallback for Android 7.0 - 12 (API 24 - 32)
        this.drawBehind {
            drawVolumetricMeshFallback(time, color1, color2, color3, bgColor, intensity)
        }
    }
}

/**
 * Universal volumetric mesh renderer using oscillating radial gradient waves.
 * Zero-lag 60/120fps hardware compositing on all Android GPU drivers.
 */
private fun DrawScope.drawVolumetricMeshFallback(
    time: Float,
    c1: Color,
    c2: Color,
    c3: Color,
    bg: Color,
    intensity: Float
) {
    // 1. Solid Deep Canvas base
    drawRect(color = bg)

    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return

    // 2. Node 1: Dynamic Top-Left / Center Orbital Pulse
    val x1 = w * (0.35f + 0.22f * cos(time * 0.45f))
    val y1 = h * (0.28f + 0.18f * sin(time * 0.38f))
    val r1 = (w.coerceAtLeast(h)) * (0.65f + 0.12f * sin(time * 0.5f))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(c1.copy(alpha = c1.alpha * intensity), Color.Transparent),
            center = Offset(x1, y1),
            radius = r1
        ),
        radius = r1,
        center = Offset(x1, y1)
    )

    // 3. Node 2: Dynamic Right / Lower-Center Counter-Orbital Glow
    val x2 = w * (0.70f + 0.20f * sin(time * 0.35f))
    val y2 = h * (0.55f + 0.22f * cos(time * 0.40f))
    val r2 = (w.coerceAtLeast(h)) * (0.60f + 0.15f * cos(time * 0.42f))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(c2.copy(alpha = c2.alpha * intensity), Color.Transparent),
            center = Offset(x2, y2),
            radius = r2
        ),
        radius = r2,
        center = Offset(x2, y2)
    )

    // 4. Node 3: Deep Volumetric Bottom Floor Highlight
    val x3 = w * (0.45f - 0.25f * cos(time * 0.30f))
    val y3 = h * (0.80f + 0.12f * sin(time * 0.48f))
    val r3 = (w.coerceAtLeast(h)) * (0.70f + 0.10f * sin(time * 0.28f))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(c3.copy(alpha = c3.alpha * intensity), Color.Transparent),
            center = Offset(x3, y3),
            radius = r3
        ),
        radius = r3,
        center = Offset(x3, y3)
    )
}

/**
 * Specular Edge Glow & Inset Lighting Modifier.
 * Renders a crisp 1px specular top highlight and subtle ambient edge refraction.
 */
fun Modifier.specularBorderGlow(
    shape: Shape,
    strokeWidth: Dp = 1.dp,
    glowColor: Color = Color.White.copy(alpha = 0.45f),
    rimColor: Color = Color.White.copy(alpha = 0.12f)
): Modifier = this
    .border(
        width = strokeWidth,
        brush = Brush.verticalGradient(
            colors = listOf(glowColor, rimColor, Color.Transparent)
        ),
        shape = shape
    )

/**
 * Optical Liquid Glass Material.
 * Combines translucent surface tint, multi-stop specular gradient, and specular rim.
 */
fun Modifier.liquidGlassSurface(
    shape: Shape,
    tint: Color = Color(0x1AFFFFFF),
    borderGlow: Color = Color(0x66FFFFFF),
    elevation: Dp = 4.dp
): Modifier = this
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                tint.copy(alpha = (tint.alpha * 1.35f).coerceAtMost(0.95f)),
                tint.copy(alpha = (tint.alpha * 0.75f))
            )
        ),
        shape = shape
    )
    .specularBorderGlow(
        shape = shape,
        strokeWidth = 1.dp,
        glowColor = borderGlow,
        rimColor = borderGlow.copy(alpha = 0.15f)
    )
