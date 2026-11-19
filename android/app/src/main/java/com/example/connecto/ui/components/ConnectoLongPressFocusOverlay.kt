package com.example.connecto.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Full-screen root portal / modal layer (zIndex: 999) for Long-Press Message Focus.
 *
 * Stacking Order:
 * - Full-screen container at zIndex: 999 covers chat header, messages, quick chips, and input dock.
 * - Layer 1 (zIndex: 1): Translucent blurred backdrop (rgba(0, 0, 0, 0.72)).
 * - Layer 2 (zIndex: 2): Crisp, un-dimmed elevated layer of the selected message bubble (scale 1.0 -> 1.03,
 *   soft drop shadow 0 12px 32px rgba(0,0,0,0.6), NO harsh border outline), top emoji reaction pill,
 *   and bottom action menu (#1C202E Dark / #FFFFFF Light, 1px solid rgba(255,255,255,0.1) border, 100% opacity).
 */
@Composable
fun ConnectoLongPressFocusOverlay(
    visible: Boolean,
    bubbleBounds: Rect?,
    isMe: Boolean,
    isLight: Boolean,
    onDismiss: () -> Unit,
    onReact: (String) -> Unit,
    onOpenFullEmojiSheet: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    bubbleContent: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Spring scale animation (scale: 1.0 -> 1.03 over 180ms when held, reverses in 150ms on dismiss)
    val scaleAnim by animateFloatAsState(
        targetValue = if (visible) 1.03f else 1.0f,
        animationSpec = if (visible) {
            spring(dampingRatio = 0.72f, stiffness = 380f)
        } else {
            tween(durationMillis = 150, easing = FastOutLinearInEasing)
        },
        label = "elevatedBubbleScale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 180, easing = LinearEasing)),
        exit = fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // ================= LAYER 1 (zIndex: 1): TRANSLUCENT BLURRED BACKDROP =================
            // Background: rgba(0, 0, 0, 0.72). Tapping anywhere on the scrim smoothly dismisses (150ms).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .background(Color(0xB8000000)) // rgba(0, 0, 0, 0.72)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onDismiss()
                    }
            )

            // ================= LAYER 2 (zIndex: 2): CRISP UN-DIMMED ELEVATED LAYER =================
            val menuWidthPx = with(density) { 276.dp.toPx() }
            val horizontalPaddingPx = with(density) { 16.dp.toPx() }

            val bubbleWidth = bubbleBounds?.width ?: with(density) { 240.dp.toPx() }
            val bubbleHeight = bubbleBounds?.height ?: with(density) { 60.dp.toPx() }

            val containerWidthPx = maxOf(bubbleWidth, menuWidthPx)
                .coerceAtMost(screenWidthPx - (horizontalPaddingPx * 2))

            val containerX = if (bubbleBounds != null) {
                if (isMe) {
                    (bubbleBounds.right - containerWidthPx)
                        .coerceIn(horizontalPaddingPx, screenWidthPx - containerWidthPx - horizontalPaddingPx)
                } else {
                    bubbleBounds.left
                        .coerceIn(horizontalPaddingPx, screenWidthPx - containerWidthPx - horizontalPaddingPx)
                }
            } else {
                if (isMe) screenWidthPx - containerWidthPx - horizontalPaddingPx else horizontalPaddingPx
            }

            val topPillHeightPx = with(density) { 54.dp.toPx() }
            val bottomMenuHeightPx = with(density) { 56.dp.toPx() }
            val minTopPx = with(density) { 64.dp.toPx() }
            val maxBottomPx = screenHeightPx - with(density) { 48.dp.toPx() }

            val rawContainerY = if (bubbleBounds != null) {
                bubbleBounds.top - topPillHeightPx
            } else {
                (screenHeightPx - bubbleHeight - topPillHeightPx - bottomMenuHeightPx) / 2f
            }

            val totalEstimatedHeightPx = topPillHeightPx + bubbleHeight + bottomMenuHeightPx
            val containerY = rawContainerY.coerceIn(
                minTopPx,
                (maxBottomPx - totalEstimatedHeightPx).coerceAtLeast(minTopPx)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onDismiss()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .offset { IntOffset(containerX.roundToInt(), containerY.roundToInt()) }
                        .width(with(density) { containerWidthPx.toDp() })
                        .graphicsLayer {
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                            transformOrigin = if (isMe) TransformOrigin(1f, 0.5f) else TransformOrigin(0f, 0.5f)
                        },
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    // 1. TOP EMOJI REACTION PILL
                    // Crisp surface (#1C202E Dark / #FFFFFF Light), 1px solid rgba(255,255,255,0.1) border, 100% opacity
                    Surface(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .height(46.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp)),
                        color = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1C202E),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, if (isLight) Color(0x14000000) else Color(0x1AFFFFFF))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val quickEmojis = listOf("❤️", "🔥", "😂", "👍", "😮", "😢")
                            quickEmojis.forEachIndexed { idx, emoji ->
                                var visibleEmoji by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    delay(idx * 24L)
                                    visibleEmoji = true
                                }
                                val popScale by animateFloatAsState(
                                    targetValue = if (visibleEmoji) 1f else 0.4f,
                                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 450f),
                                    label = "emojiPopScale"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .graphicsLayer {
                                            scaleX = popScale
                                            scaleY = popScale
                                        }
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onReact(emoji)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 17.sp)
                                }
                            }

                            // Trailing '+' icon button to open full emoji sheet
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isLight) Color(0x0D000000) else Color(0x1FFFFFFF))
                                    .clickable {
                                        onOpenFullEmojiSheet()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "All reactions",
                                    tint = if (isLight) Color(0xFF475569) else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // 2. FOCUSED MESSAGE BUBBLE (ELEVATED & CRISP)
                    // Drop shadow: 0 12px 32px rgba(0,0,0,0.6). NO harsh border outline.
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = 16.dp,
                                shape = if (isMe) {
                                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
                                } else {
                                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
                                },
                                spotColor = Color.Black.copy(alpha = 0.60f),
                                ambientColor = Color.Black.copy(alpha = 0.40f)
                            )
                    ) {
                        bubbleContent()
                    }

                    // 3. BOTTOM ACTION MENU (REPLY / COPY / UNSEND)
                    // Crisp surface (#1C202E Dark / #FFFFFF Light), 1px solid rgba(255,255,255,0.1) border, 100% opacity
                    Surface(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp)),
                        color = if (isLight) Color(0xFFFFFFFF) else Color(0xFF1C202E),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isLight) Color(0x14000000) else Color(0x1AFFFFFF))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            actions()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

