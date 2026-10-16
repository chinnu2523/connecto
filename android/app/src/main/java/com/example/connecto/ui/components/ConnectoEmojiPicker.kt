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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.data.EmojiUsageManager
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import kotlinx.coroutines.launch

/**
 * Emoji Categories:
 * 1. USED: Frequently and recently used emojis for the current user, ordered strictly by usage.
 * 2. FACES: Primary smiley and emotion library shown first when no usage exists.
 * 3. GESTURES: Hands, gestures, and body symbols.
 * 4. HEARTS: Romantic, expressive, and colored hearts.
 * 5. GAMING: Arcade, controller, trophy, and battle icons.
 * 6. ACTIVITIES: Celebrations, party, and treats.
 * 7. NATURE: Animals, flowers, and greenery.
 * 8. SYMBOLS: Glows, fire, stars, and notification badges.
 */
enum class EmojiCategory(val label: String, val icon: String) {
    USED("Used", "🕒"),
    FACES("Faces", "😀"),
    GESTURES("Gestures", "👍"),
    HEARTS("Hearts", "❤️"),
    GAMING("Gaming", "🎮"),
    ACTIVITIES("Party", "🎉"),
    NATURE("Nature", "🌿"),
    SYMBOLS("Symbols", "🔥")
}

@Composable
fun ConnectoEmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    userId: String? = null,
    onBackspace: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val effectiveUserId = remember(userId) { EmojiUsageManager.getEffectiveUserId(userId) }
    val gradientColors = getDynamicAccentGradientColors()

    val currentOnEmojiSelected by rememberUpdatedState(onEmojiSelected)
    val currentOnClose by rememberUpdatedState(onClose)
    val currentOnBackspace by rememberUpdatedState(onBackspace)

    // Retrieve per-user used emojis, sorted by frequency (count) and recency
    var usedEmojis by remember(effectiveUserId) {
        mutableStateOf(EmojiUsageManager.getUsedEmojis(context, effectiveUserId))
    }

    // Requirement:
    // If user has used emojis, show their USED emojis first;
    // Otherwise show FACES emojis first.
    var selectedCategory by remember(effectiveUserId) {
        mutableStateOf(if (usedEmojis.isNotEmpty()) EmojiCategory.USED else EmojiCategory.FACES)
    }

    val faceEmojis = remember {
        listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹",
            "☺️", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘",
            "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐",
            "🤓", "😎", "🥸", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟",
            "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭",
            "😮‍💨", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱",
            "😨", "😰", "😥", "😓", "🫣", "🤫", "🫡", "🤥", "😶", "😐",
            "😑", "😬", "🫨", "🫠", "🙄", "😯", "😦", "😧", "😮", "😲",
            "🥱", "😴", "🤤", "😪", "😵", "😵‍💫", "🤐", "🥴", "🤢", "🤮",
            "🤧", "😷", "🤒", "🤕", "🤑", "🤠", "😈", "👿", "👹", "👺",
            "🤡", "💩", "👻", "💀", "☠️", "👽", "👾", "🤖"
        )
    }

    val gestureEmojis = remember {
        listOf(
            "👍", "👎", "👊", "✊", "🤛", "🤜", "🤞", "✌️", "🫰", "🤟",
            "🤘", "👌", "🤌", "🤏", "👈", "👉", "👆", "👇", "☝️", "✋",
            "🤚", "🖐️", "🖖", "👋", "🤙", "🫲", "🫱", "🫳", "🫴", "🫵",
            "✍️", "🙏", "🤝", "👏", "🙌", "🫶", "👐", "🤲", "🫂", "💪"
        )
    }

    val heartEmojis = remember {
        listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
            "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
            "💟", "💌", "🫀", "💋", "💐", "🌹", "🥀", "🌺", "🌸", "🌷"
        )
    }

    val gamingEmojis = remember {
        listOf(
            "🎮", "🕹️", "👾", "🏆", "🥇", "🥈", "🥉", "🏅", "🎯", "⚔️",
            "🛡️", "🏹", "🔫", "💣", "💥", "🚀", "⚡", "🔥", "👑", "💎",
            "🎲", "🎧", "🪄", "🎪", "🦾", "🏁", "🏎️", "🥊", "🥋", "🃏",
            "🎴", "🎳", "🔮", "💻", "🖥️", "📱"
        )
    }

    val partyEmojis = remember {
        listOf(
            "🎉", "🎊", "🎈", "🎁", "🍾", "🥂", "🍻", "🍺", "🍸", "🍹",
            "🍿", "🍕", "🍔", "🍟", "🌭", "🥪", "🌮", "🌯", "🍩", "🍪",
            "🎂", "🍰", "🧁", "🍫", "🍬", "🍭", "🍦", "🍨", "🍧", "🍓"
        )
    }

    val natureEmojis = remember {
        listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
            "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🦆", "🦅",
            "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🦋", "🐞", "🐢",
            "🐙", "🐬", "🐳", "🦈", "🌸", "🌺", "🌻", "🌹", "🍀", "🌿"
        )
    }

    val symbolEmojis = remember {
        listOf(
            "🔥", "⚡", "✨", "💫", "⭐", "🌟", "💥", "💯", "💢", "💨",
            "💦", "💤", "💬", "👁️‍🗨️", "🌐", "🔮", "🧿", "💡", "🔔", "📢",
            "⚠️", "⛔", "🛑", "🎵", "🎶", "🔊", "🔒", "🔑", "⏳", "⏰"
        )
    }

    // Handles emoji selection: emits event & records usage frequency for the individual user
    val onSelectEmoji: (String) -> Unit = { emoji ->
        currentOnEmojiSelected(emoji)
        EmojiUsageManager.recordEmojiUsage(context, emoji, effectiveUserId)
        usedEmojis = EmojiUsageManager.getUsedEmojis(context, effectiveUserId)
    }

    val currentEmojis = when (selectedCategory) {
        EmojiCategory.USED -> usedEmojis
        EmojiCategory.FACES -> faceEmojis
        EmojiCategory.GESTURES -> gestureEmojis
        EmojiCategory.HEARTS -> heartEmojis
        EmojiCategory.GAMING -> gamingEmojis
        EmojiCategory.ACTIVITIES -> partyEmojis
        EmojiCategory.NATURE -> natureEmojis
        EmojiCategory.SYMBOLS -> symbolEmojis
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
                            // Don't show the USED tab if the user has never used an emoji yet
                            if (category == EmojiCategory.USED && usedEmojis.isEmpty()) {
                                return@items
                            }

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

                    Spacer(modifier = Modifier.width(6.dp))

                    // Optional Backspace Action Button
                    if (onBackspace != null) {
                        val bsSource = remember { MutableInteractionSource() }
                        val isBsPressed by bsSource.collectIsPressedAsState()
                        val bsScale by animateFloatAsState(
                            targetValue = if (isBsPressed) 0.85f else 1.0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "bsScale"
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .scale(bsScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(
                                    interactionSource = bsSource,
                                    indication = null
                                ) {
                                    currentOnBackspace?.invoke()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                contentDescription = "Backspace",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Close Button with spring hover & rotate
                    val closeInteractionSource = remember { MutableInteractionSource() }
                    val isClosePressed by closeInteractionSource.collectIsPressedAsState()
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
                            .clickable(
                                interactionSource = closeInteractionSource,
                                indication = null
                            ) {
                                currentOnClose()
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

                Spacer(modifier = Modifier.height(8.dp))

                // Category Subtitle or Shelf
                when (selectedCategory) {
                    EmojiCategory.USED -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "✨ Frequently Used by You",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ranked by your usage",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "Clear",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        EmojiUsageManager.clearUsage(context, effectiveUserId)
                                        usedEmojis = emptyList()
                                        selectedCategory = EmojiCategory.FACES
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    EmojiCategory.FACES -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Smileys & Expressions",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${faceEmojis.size} emojis",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCategory.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${currentEmojis.size} emojis",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // If on FACES and user has used emojis, show a sleek quick-access row of their top emojis
                if (selectedCategory == EmojiCategory.FACES && usedEmojis.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🕒",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 2.dp, end = 2.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(usedEmojis.take(8)) { quickEmoji ->
                                Text(
                                    text = quickEmoji,
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onSelectEmoji(quickEmoji) }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

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
                ) { (category, emojis) ->
                    if (category == EmojiCategory.USED && emojis.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(135.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "😀 No emojis used yet",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap any face emoji to start your personal collection!",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(8),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(145.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                items = emojis,
                                key = { index, emoji -> "${category.name}_${index}_$emoji" }
                            ) { _, emoji ->
                                AnimatedEmojiItem(
                                    emoji = emoji,
                                    onSelect = { onSelectEmoji(emoji) }
                                )
                            }
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isJustSelected by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Bouncy scale physics
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 1.25f
            isJustSelected -> 1.15f
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
        targetValue = if (isPressed) -6f else if (isJustSelected) 6f else 0f,
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
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isJustSelected = true
                onSelect()
                coroutineScope.launch {
                    burstRadius.snapTo(0.2f)
                    burstAlpha.snapTo(0.6f)
                    burstRadius.animateTo(1.5f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                    burstAlpha.animateTo(0f, animationSpec = tween(220))
                    isJustSelected = false
                }
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
