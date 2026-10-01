package com.example.connecto.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import kotlinx.coroutines.launch

enum class EmojiCategory(val label: String, val icon: String) {
    GAMING("Gaming", "🎮"),
    FACES("Faces", "😀"),
    GESTURES("Gestures", "👍"),
    HEARTS("Hearts", "❤️"),
    ACTIVITIES("Party", "🎉")
}

@Composable
fun ConnectoEmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(EmojiCategory.GAMING) }
    val gradientColors = getDynamicAccentGradientColors()

    val gamingEmojis = remember {
        listOf(
            "🎮", "🕹️", "👾", "🏆", "🥇", "🎯", "⚔️", "🛡️",
            "🔫", "💣", "💥", "🚀", "⚡", "🔥", "👑", "💎",
            "🎲", "🎧", "🥈", "🥉", "🪄", "🎪", "🦾", "🏅",
            "🏁", "🏎️", "🥊", "🥋", "🃏", "🎴", "🎳", "🔮"
        )
    }

    val faceEmojis = remember {
        listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣",
            "😎", "🤩", "🥳", "😍", "🥰", "😘", "😋", "😜",
            "🫡", "💀", "☠️", "👻", "🤖", "👽", "🤔", "🤫",
            "😏", "🥺", "🥵", "🥶", "🤯", "😳", "😱", "🫣",
            "😴", "🤤", "🫠", "😇", "🙂", "🙃", "😉", "😌"
        )
    }

    val gestureEmojis = remember {
        listOf(
            "👍", "👎", "👊", "✊", "🤛", "🤜", "🤞", "✌️",
            "🫰", "🤟", "🤘", "👌", "🤌", "🤏", "👈", "👉",
            "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋",
            "🤙", "💪", "✍️", "🙏", "🤝", "👏", "🙌", "🫶"
        )
    }

    val heartEmojis = remember {
        listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
            "🤎", "💔", "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓",
            "💗", "💖", "💘", "💝", "💟", "🔥", "⚡", "🌟",
            "✨", "💯", "👑", "💎", "🚀", "🎯", "🛡️", "⚔️"
        )
    }

    val partyEmojis = remember {
        listOf(
            "🎉", "🎊", "🎈", "🎁", "🍾", "🥂", "🍻", "🍺",
            "🍿", "🍕", "🍔", "🍟", "🌭", "🍩", "🍪", "🎂",
            "🍰", "🧁", "🍫", "🍬", "🍭", "⚡", "✨", "💫"
        )
    }

    val currentEmojis = when (selectedCategory) {
        EmojiCategory.GAMING -> gamingEmojis
        EmojiCategory.FACES -> faceEmojis
        EmojiCategory.GESTURES -> gestureEmojis
        EmojiCategory.HEARTS -> heartEmojis
        EmojiCategory.ACTIVITIES -> partyEmojis
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(12.dp)
    ) {
        DisableSelection {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Category Tabs Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(EmojiCategory.entries) { category ->
                        val isSelected = selectedCategory == category

                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.04f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "tabScale"
                        )

                        val bgColor by animateColorAsState(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "catBg"
                        )
                        val textColor by animateColorAsState(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "catText"
                        )

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    brush = if (isSelected) Brush.linearGradient(gradientColors) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 11.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = category.icon,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = category.label,
                                    color = textColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Close Button with spring hover & rotate
                var isClosePressed by remember { mutableStateOf(false) }
                val closeScale by animateFloatAsState(
                    targetValue = if (isClosePressed) 0.85f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "closeScale"
                )

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .scale(closeScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isClosePressed = true
                                    tryAwaitRelease()
                                    isClosePressed = false
                                    onClose()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Emoji Picker",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Emoji Grid with High-End Fluid Animation
            AnimatedContent(
                targetState = selectedCategory to currentEmojis,
                transitionSpec = {
                    (slideInVertically(
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                        initialOffsetY = { it / 4 }
                    ) + fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.95f))
                        .togetherWith(
                            slideOutVertically(
                                animationSpec = tween(160, easing = FastOutSlowInEasing),
                                targetOffsetY = { -it / 6 }
                            ) + fadeOut(animationSpec = tween(160)) + scaleOut(targetScale = 0.95f)
                        )
                },
                label = "emojiGridTransition"
            ) { (_, emojis) ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = emojis,
                        key = { index, emoji -> "${selectedCategory.name}_${index}_$emoji" }
                    ) { _, emoji ->
                        AnimatedEmojiItem(
                            emoji = emoji,
                            onSelect = { onEmojiSelected(emoji) }
                        )
                    }
                }
            }
        }
        } // end DisableSelection
    }
}

@Composable
private fun AnimatedEmojiItem(
    emoji: String,
    onSelect: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isJustSelected by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Bouncy scale physics
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 1.45f
            isJustSelected -> 1.25f
            else -> 1.0f
        },
        animationSpec = spring(
            stiffness = if (isPressed) 600f else 350f,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "emojiScale"
    )

    // Subtle playful rotation on press
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) -10f else if (isJustSelected) 8f else 0f,
        animationSpec = spring(
            stiffness = 400f,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "emojiRotation"
    )

    // Pop burst wave animation
    val burstRadius = remember { Animatable(0f) }
    val burstAlpha = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .size(38.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .clip(CircleShape)
            .pointerInput(emoji) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        coroutineScope.launch {
                            burstRadius.snapTo(0.2f)
                            burstAlpha.snapTo(0.6f)
                        }
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        isJustSelected = true
                        onSelect()
                        coroutineScope.launch {
                            burstRadius.animateTo(1.5f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                            burstAlpha.animateTo(0f, animationSpec = tween(220))
                            isJustSelected = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Radiant Glow Burst on Tap
        if (burstAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .size((38 * burstRadius.value).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = burstAlpha.value))
            )
        }

        Text(
            text = emoji,
            fontSize = 21.sp
        )
    }
}
