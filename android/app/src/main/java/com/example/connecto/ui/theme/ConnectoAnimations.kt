package com.example.connecto.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// 1. SHIMMER LOADING BOX
//    GPU-safe: animates only background position (no layout props)
// ---------------------------------------------------------------------------

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val shimmerColors = listOf(
        Color(0xFF1C1E2A),
        Color(0xFF2A2D3E),
        Color(0xFF1C1E2A)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffsetX by infiniteTransition.animateFloat(
        initialValue = -800f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(shimmerOffsetX, 0f),
                    end = Offset(shimmerOffsetX + 600f, 400f)
                )
            )
    )
}

// ---------------------------------------------------------------------------
// 2. SHIMMER CONVERSATION CARD SKELETON
//    Placeholder shown while conversations are loading
// ---------------------------------------------------------------------------

@Composable
fun ShimmerConversationItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF12141A))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar circle
            ShimmerBox(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp)
            )
            // Text lines
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(12.dp),
                    shape = RoundedCornerShape(6.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(10.dp),
                    shape = RoundedCornerShape(5.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. SHIMMER VOICE ROOM CARD SKELETON
// ---------------------------------------------------------------------------

@Composable
fun ShimmerVoiceRoomCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF12141A))
            .padding(20.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(14.dp),
                shape = RoundedCornerShape(7.dp)
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(10.dp),
                shape = RoundedCornerShape(5.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    ShimmerBox(
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3b. SHIMMER CHAT MESSAGE BUBBLE & FEED SKELETON
// ---------------------------------------------------------------------------

@Composable
fun ShimmerChatMessageItem(
    isOwn: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = if (isOwn) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (isOwn) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(52.dp),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(18.dp)
                )
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(10.dp),
                        shape = RoundedCornerShape(5.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(48.dp),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerMessageFeed(
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ShimmerChatMessageItem(isOwn = false)
        ShimmerChatMessageItem(isOwn = true)
        ShimmerChatMessageItem(isOwn = false)
        ShimmerChatMessageItem(isOwn = true)
        ShimmerChatMessageItem(isOwn = false)
    }
}

@Composable
fun ShimmerChannelItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(
            modifier = Modifier.size(20.dp),
            shape = RoundedCornerShape(6.dp)
        )
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(12.dp),
                shape = RoundedCornerShape(6.dp)
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(10.dp),
                shape = RoundedCornerShape(5.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4. PULSING ONLINE DOT
//    Scale 1.0 → 1.4 → 1.0, uses only graphicsLayer (GPU-safe)
// ---------------------------------------------------------------------------

@Composable
fun PulsingOnlineDot(
    color: Color = Color(0xFF34D399),
    size: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "onlinePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(modifier = modifier.size(size + 4.dp), contentAlignment = Alignment.Center) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    alpha = pulseAlpha * 0.35f
                }
                .clip(CircleShape)
                .background(color)
        )
        // Solid inner dot
        Box(
            modifier = Modifier
                .size(size * 0.65f)
                .clip(CircleShape)
                .background(color)
        )
    }
}

// ---------------------------------------------------------------------------
// 5. TYPING INDICATOR — 3 dots with staggered bounce animation
// ---------------------------------------------------------------------------

@Composable
fun TypingIndicatorDots(
    color: Color = Color(0xFF9CA3AF),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    val dot1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    val offsets = listOf(dot1Y, dot2Y, dot3Y)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        offsets.forEach { offsetY ->
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .graphicsLayer { translationY = offsetY }
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 6. STAGGERED REVEAL WRAPPER
//    Wraps content with index-based fade + slide-up entry animation.
//    Uses graphicsLayer only (GPU-safe).
// ---------------------------------------------------------------------------

@Composable
fun StaggeredReveal(
    index: Int,
    durationMs: Int = 350,
    staggerMs: Long = 55L,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(index) {
        delay(index * staggerMs)
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "staggerAlpha_$index"
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 32f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "staggerY_$index"
    )

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translationY
        }
    ) {
        content()
    }
}
