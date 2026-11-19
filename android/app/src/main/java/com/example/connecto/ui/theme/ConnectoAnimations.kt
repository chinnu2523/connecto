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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.connecto.ui.designsystem.ConnectoTheme
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// 1. GPU-ACCELERATED SHIMMER ENGINE & SKELETON PRIMITIVES
//    Diagonal sweep (left-to-right, 1200ms linear loop, locked 60fps)
//    Dark mode: baseColor = #131620, highlightColor = #1E2433
//    Light mode: baseColor = #E2E8F0, highlightColor = #F1F5F9
// ---------------------------------------------------------------------------

@Composable
fun rememberConnectoShimmerBrush(
    isDark: Boolean = ConnectoTheme.colors.isDark
): Brush {
    val baseColor = if (isDark) Color(0xFF131620) else Color(0xFFE2E8F0)
    val highlightColor = if (isDark) Color(0xFF1E2433) else Color(0xFFF1F5F9)
    val shimmerColors = listOf(
        baseColor,
        highlightColor,
        baseColor
    )

    val infiniteTransition = rememberInfiniteTransition(label = "connectoShimmerEngine")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -800f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 480f, 320f)
    )
}

/**
 * Core reusable primitive: SkeletonBox.
 * GPU-safe: animates only background gradient offset (zero layout invalidation).
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    val brush = rememberConnectoShimmerBrush(isDark = isDark)
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * Reusable ShimmerPlaceholder primitive.
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    SkeletonBox(modifier = modifier, shape = shape, isDark = isDark)
}

/**
 * Reusable SkeletonCircle primitive.
 */
@Composable
fun SkeletonCircle(
    size: Dp,
    modifier: Modifier = Modifier,
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    SkeletonBox(
        modifier = modifier.size(size),
        shape = CircleShape,
        isDark = isDark
    )
}

/**
 * Modifier extension to apply shimmer placeholder background to any composable.
 */
fun Modifier.shimmerPlaceholder(
    shape: Shape = RoundedCornerShape(8.dp),
    isDark: Boolean = true
): Modifier = composed {
    val brush = rememberConnectoShimmerBrush(isDark = isDark)
    this.clip(shape).background(brush)
}

/**
 * Backwards-compatible ShimmerBox mapping to SkeletonBox with the unified 60fps GPU brush.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    SkeletonBox(
        modifier = modifier,
        shape = shape
    )
}

// ---------------------------------------------------------------------------
// 2. SHIMMER CONVERSATION CARD SKELETON (Exact Geometry Match)
//    48px circle, 110x14px name, 190x12px preview, 38x10px top-right bar
// ---------------------------------------------------------------------------

@Composable
fun ShimmerConversationItem(
    modifier: Modifier = Modifier,
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    val isLight = !isDark
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isLight) Color.White else Color(0xFF0A0B0E))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 48px circle on the left
            SkeletonBox(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                isDark = isDark
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 110px x 14px rounded bar for the name
                    SkeletonBox(
                        modifier = Modifier.size(width = 110.dp, height = 14.dp),
                        shape = RoundedCornerShape(4.dp),
                        isDark = isDark
                    )
                    // 38px x 10px bar on the top-right
                    SkeletonBox(
                        modifier = Modifier.size(width = 38.dp, height = 10.dp),
                        shape = RoundedCornerShape(4.dp),
                        isDark = isDark
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // 190px x 12px rounded bar for the message preview
                SkeletonBox(
                    modifier = Modifier.size(width = 190.dp, height = 12.dp),
                    shape = RoundedCornerShape(4.dp),
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
fun ShimmerConversationsList(
    modifier: Modifier = Modifier,
    count: Int = 6,
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(count) {
            ShimmerConversationItem(isDark = isDark)
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
        Column(
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
// 3b. SHIMMER CHAT MESSAGE BUBBLE & FEED SKELETON (Exact Geometry Match)
//     6 alternating bubbles, border-radius: 18px, widths: 140-240px, heights: 42-68px
// ---------------------------------------------------------------------------

@Composable
fun ShimmerChatMessageItem(
    isOutgoing: Boolean = false,
    widthDp: Dp = if (isOutgoing) 210.dp else 190.dp,
    heightDp: Dp = if (isOutgoing) 54.dp else 48.dp,
    modifier: Modifier = Modifier,
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    val bubbleShape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (isOutgoing) {
            SkeletonBox(
                modifier = Modifier
                    .width(widthDp)
                    .height(heightDp),
                shape = bubbleShape,
                isDark = isDark
            )
        } else {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    isDark = isDark
                )
                SkeletonBox(
                    modifier = Modifier
                        .width(widthDp)
                        .height(heightDp),
                    shape = bubbleShape,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
fun ShimmerMessageFeed(
    modifier: Modifier = Modifier,
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    // 6 alternating incoming (left-aligned) and outgoing (right-aligned) rounded bubble placeholders
    // border-radius: 18px, varying widths between 140px and 240px, heights between 42px and 68px
    val bubbleSpecs = listOf(
        Pair(false, Pair(190.dp, 48.dp)), // 1. incoming
        Pair(true, Pair(230.dp, 58.dp)),  // 2. outgoing
        Pair(false, Pair(150.dp, 42.dp)), // 3. incoming
        Pair(true, Pair(200.dp, 64.dp)),  // 4. outgoing
        Pair(false, Pair(240.dp, 68.dp)), // 5. incoming
        Pair(true, Pair(170.dp, 44.dp))   // 6. outgoing
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        bubbleSpecs.forEach { (isOut, size) ->
            ShimmerChatMessageItem(
                isOutgoing = isOut,
                widthDp = size.first,
                heightDp = size.second,
                isDark = isDark
            )
        }
    }
}

@Composable
fun ShimmerChannelItem(
    modifier: Modifier = Modifier,
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkeletonBox(
            modifier = Modifier.size(20.dp),
            shape = RoundedCornerShape(6.dp),
            isDark = isDark
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(12.dp),
                shape = RoundedCornerShape(6.dp),
                isDark = isDark
            )
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(10.dp),
                shape = RoundedCornerShape(5.dp),
                isDark = isDark
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 3c. SHIMMER CALL ITEM & LIST SKELETON
// ---------------------------------------------------------------------------

@Composable
fun ShimmerCallItem(
    modifier: Modifier = Modifier,
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    val callCardShape = RoundedCornerShape(16.dp)
    val cardBg = if (isDark) Color(0xFF131620) else Color.White
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(callCardShape)
            .background(cardBg)
            .border(1.dp, if (isDark) Color(0x0DFFFFFF) else Color(0x0F000000), callCardShape)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Avatar circle (44dp)
                SkeletonBox(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    isDark = isDark
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Contact name bar
                    SkeletonBox(
                        modifier = Modifier
                            .width(130.dp)
                            .height(14.dp),
                        shape = RoundedCornerShape(4.dp),
                        isDark = isDark
                    )
                    // Call type/timestamp bar
                    SkeletonBox(
                        modifier = Modifier
                            .width(85.dp)
                            .height(11.dp),
                        shape = RoundedCornerShape(4.dp),
                        isDark = isDark
                    )
                }
            }
            // Action button circle (36dp)
            SkeletonBox(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                isDark = isDark
            )
        }
    }
}

@Composable
fun ShimmerCallLogsList(
    modifier: Modifier = Modifier,
    count: Int = 6,
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) {
            ShimmerCallItem(isDark = isDark)
        }
    }
}

// ---------------------------------------------------------------------------
// 3d. SHIMMER USER & FRIEND PROFILE SKELETON (Exact Geometry Match)
//     Cover banner (125dp), avatar cutout (80dp), action buttons & nav items
// ---------------------------------------------------------------------------

@Composable
fun ShimmerProfileSkeleton(
    modifier: Modifier = Modifier,
    isDark: Boolean = ConnectoTheme.colors.isDark
) {
    val cardShape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Cover Banner with Avatar cutout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            // Cover banner placeholder (125dp)
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp),
                shape = cardShape,
                isDark = isDark
            )
            // Circular Avatar Cutout (80dp) overlapping banner cleanly by 50%
            Box(
                modifier = Modifier
                    .offset(x = 16.dp, y = 85.dp)
                    .size(80.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    isDark = isDark
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Name & Handle bar placeholders
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 160px x 20px rounded bar for display name
            SkeletonBox(
                modifier = Modifier.size(width = 160.dp, height = 20.dp),
                shape = RoundedCornerShape(6.dp),
                isDark = isDark
            )
            // 90px x 14px rounded bar for @username
            SkeletonBox(
                modifier = Modifier.size(width = 90.dp, height = 14.dp),
                shape = RoundedCornerShape(4.dp),
                isDark = isDark
            )
            // 220px x 12px rounded bar for bio preview
            SkeletonBox(
                modifier = Modifier.size(width = 220.dp, height = 12.dp),
                shape = RoundedCornerShape(4.dp),
                isDark = isDark
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Quick Action Buttons Row (4 action buttons matching Call/Video/Chat/Lock)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    isDark = isDark
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Settings / Navigation List Items (matching FriendProfileNavButtons)
        repeat(3) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = cardShape,
                isDark = isDark
            )
        }
    }
}

@Composable
fun ShimmerNotificationItem(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF161922))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBox(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(20.dp)
            )
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier.width(150.dp).height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                ShimmerBox(
                    modifier = Modifier.fillMaxWidth(0.85f).height(12.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                ShimmerBox(
                    modifier = Modifier.width(70.dp).height(10.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
    }
}

@Composable
fun ShimmerNotificationsList(
    modifier: Modifier = Modifier,
    count: Int = 4
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(count) {
            ShimmerNotificationItem()
        }
    }
}

@Composable
fun ShimmerFriendRequestItem(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF161922))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBox(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(22.dp)
            )
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier.width(120.dp).height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                ShimmerBox(
                    modifier = Modifier.width(80.dp).height(11.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBox(
                    modifier = Modifier.width(64.dp).height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                ShimmerBox(
                    modifier = Modifier.width(32.dp).height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
fun ShimmerFriendRequestsList(
    modifier: Modifier = Modifier,
    count: Int = 3
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) {
            ShimmerFriendRequestItem()
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
    durationMs: Int = 300,
    staggerMs: Long = 25L,
    initialOffsetY: Float = 16f,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!visible) {
            val safeDelay = (index.coerceAtLeast(0) * staggerMs).coerceAtMost(320L)
            if (safeDelay > 0) delay(safeDelay)
            visible = true
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
        label = "staggerAlpha_$index"
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else initialOffsetY,
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
