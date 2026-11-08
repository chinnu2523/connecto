package com.example.connecto.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * SwipeableConversationRow
 *
 * Implements:
 * 1. Interactive horizontal swipe gestures with Peek Hint.
 * 2. Right Swipe -> Pin/Unpin chat with amber background, haptics, and spring bounce.
 * 3. Left Swipe -> Delete chat with Liquid Deletion Animation (fluid curve, alpha dissolve, slide offscreen).
 * 4. Inline Undo Strip -> Swiping to delete leaves an inline undo row with live 5s countdown timer.
 *    Tapping "Undo" smoothly slides the conversation row back into place with liquid spring physics.
 */
@Composable
fun SwipeableConversationRow(
    chatId: String,
    chatName: String,
    isPinned: Boolean,
    isPendingDelete: Boolean,
    modifier: Modifier = Modifier,
    triggerInitialPeekHint: Boolean = false,
    onTogglePin: () -> Unit,
    onDeleteInitiated: () -> Unit,
    onUndoDelete: () -> Unit,
    onDeleteFinalized: () -> Unit,
    onPeekHintDone: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    val pinThresholdPx = with(density) { 85.dp.toPx() }
    val maxRightDragPx = with(density) { 130.dp.toPx() }
    val deleteThresholdPx = with(density) { 95.dp.toPx() }
    val maxLeftDragPx = with(density) { 160.dp.toPx() }

    val offsetX = remember(chatId) { Animatable(0f) }
    var hasHapticFiredForPin by remember(chatId) { mutableStateOf(false) }
    var hasHapticFiredForDelete by remember(chatId) { mutableStateOf(false) }

    // Liquid deletion visual collapse progress (0f = normal, 1f = fully collapsed)
    val liquidCollapseAlpha = remember(chatId) { Animatable(1f) }

    // Initial Peek Hint Nudge (shows first-time subtle peek to educate user)
    LaunchedEffect(triggerInitialPeekHint) {
        if (triggerInitialPeekHint && !isPendingDelete) {
            delay(400)
            // Nudge right slightly to hint Pin
            offsetX.animateTo(
                targetValue = with(density) { 36.dp.toPx() },
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )
            delay(120)
            // Nudge left slightly to hint Delete
            offsetX.animateTo(
                targetValue = with(density) { -36.dp.toPx() },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
            delay(120)
            // Slide back to resting position with gentle spring
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            onPeekHintDone()
        }
    }

    if (isPendingDelete) {
        // ================= INLINE UNDO STRIP =================
        InlineUndoStrip(
            modifier = modifier,
            chatName = chatName,
            onUndo = {
                coroutineScope.launch {
                    liquidCollapseAlpha.snapTo(1f)
                    // Reset offset slightly to the left, then slide back into place smoothly
                    offsetX.snapTo(-with(density) { 200.dp.toPx() })
                    onUndoDelete()
                    offsetX.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = 0.75f,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            },
            onTimeout = onDeleteFinalized
        )
    } else {
        // ================= SWIPEABLE CHAT ROW =================
        Box(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = liquidCollapseAlpha.value
                }
        ) {
            // Background Action Layer (Revealed during swipe)
            val currentOffset = offsetX.value

            if (currentOffset > 10f) {
                // RIGHT SWIPE: Pin / Unpin Action (Amber Gradient)
                val dragRatio = (currentOffset / pinThresholdPx).coerceIn(0f, 1.5f)
                val pinBgColor = if (isPinned) Color(0xFF3B82F6) else Color(0xFFF59E0B)
                val pinBgEnd = if (isPinned) Color(0xFF1D4ED8) else Color(0xFFD97706)

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(pinBgColor, pinBgEnd)))
                        .padding(start = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Micro-scaling icon peek hint
                        val iconScale = (0.7f + dragRatio * 0.45f).coerceIn(0.7f, 1.25f)
                        Icon(
                            imageVector = if (isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin,
                            contentDescription = if (isPinned) "Unpin" else "Pin",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(iconScale)
                        )
                        Text(
                            text = if (isPinned) "Unpin" else "Pin",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            } else if (currentOffset < -10f) {
                // LEFT SWIPE: Delete Action (Crimson Gradient)
                val dragRatio = (abs(currentOffset) / deleteThresholdPx).coerceIn(0f, 1.5f)
                val deleteBgStart = Color(0xFFEF4444)
                val deleteBgEnd = Color(0xFFDC2626)

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(deleteBgStart, deleteBgEnd)))
                        .padding(end = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Delete",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        // Micro-scaling trash icon peek hint
                        val iconScale = (0.7f + dragRatio * 0.45f).coerceIn(0.7f, 1.25f)
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(iconScale)
                        )
                    }
                }
            }

            // Foreground Chat Card (Draggable surface)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput(chatId) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                hasHapticFiredForPin = false
                                hasHapticFiredForDelete = false
                            },
                            onDragEnd = {
                                val finalOffset = offsetX.targetValue
                                coroutineScope.launch {
                                    if (finalOffset >= pinThresholdPx) {
                                        // Pinned / Unpinned: Fire haptic & execute onTogglePin immediately!
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onTogglePin()
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    } else if (finalOffset <= -deleteThresholdPx) {
                                        // Liquid Deletion Animation Triggered!
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        // 1. Rapidly slide offscreen to the left with liquid stretch
                                        launch {
                                            offsetX.animateTo(
                                                targetValue = -screenWidthPx * 1.15f,
                                                animationSpec = tween(
                                                    durationMillis = 280,
                                                    easing = FastOutSlowInEasing
                                                )
                                            )
                                        }
                                        // 2. Liquid dissolve alpha
                                        liquidCollapseAlpha.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(
                                                durationMillis = 260,
                                                easing = LinearEasing
                                            )
                                        )
                                        // 3. Switch row into Inline Undo Strip
                                        onDeleteInitiated()
                                    } else {
                                        // Release back to 0
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val currentVal = offsetX.targetValue
                                val newVal = if (dragAmount > 0) {
                                    // Swiping Right (Pin)
                                    if (currentVal >= pinThresholdPx) {
                                        currentVal + dragAmount * 0.35f // Resistance dampening
                                    } else {
                                        currentVal + dragAmount
                                    }
                                } else {
                                    // Swiping Left (Delete)
                                    if (currentVal <= -deleteThresholdPx) {
                                        currentVal + dragAmount * 0.35f // Resistance dampening
                                    } else {
                                        currentVal + dragAmount
                                    }
                                }

                                val clamped = newVal.coerceIn(-maxLeftDragPx, maxRightDragPx)
                                coroutineScope.launch {
                                    offsetX.snapTo(clamped)
                                }

                                // Haptic feedback when threshold reached during continuous drag
                                if (clamped >= pinThresholdPx && !hasHapticFiredForPin) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    hasHapticFiredForPin = true
                                } else if (clamped < pinThresholdPx) {
                                    hasHapticFiredForPin = false
                                }

                                if (clamped <= -deleteThresholdPx && !hasHapticFiredForDelete) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    hasHapticFiredForDelete = true
                                } else if (clamped > -deleteThresholdPx) {
                                    hasHapticFiredForDelete = false
                                }
                            }
                        )
                    }
            ) {
                content()
            }
        }
    }
}

/**
 * InlineUndoStrip
 *
 * Appears inline where the chat row was deleted.
 * Features:
 * - Real-time countdown timer (5 seconds).
 * - Animated linear progress bar.
 * - Bold Mint "UNDO" button with liquid slide-back restoration.
 * - Auto-finalizes deletion if timer expires.
 */
@Composable
fun InlineUndoStrip(
    chatName: String,
    onUndo: () -> Unit,
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = 5
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Animate countdown progress bar smoothly over 5 seconds
        launch {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = totalSeconds * 1000, easing = LinearEasing)
            )
        }

        // Decrement remaining seconds tick
        for (i in totalSeconds downTo 1) {
            remainingSeconds = i
            delay(1000)
        }
        remainingSeconds = 0
        onTimeout()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trash icon badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deleted",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Deleted chat label & countdown
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chat deleted",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "Disappearing in ${remainingSeconds}s • Tap Undo to restore",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Undo Button (Mint / Emerald)
                Button(
                    onClick = onUndo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                        contentColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.7f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "UNDO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Bottom Progress Bar
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Color(0xFF10B981),
                trackColor = Color(0xFFEF4444).copy(alpha = 0.2f)
            )
        }
    }
}
