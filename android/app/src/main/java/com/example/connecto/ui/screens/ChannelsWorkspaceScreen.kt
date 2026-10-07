package com.example.connecto.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.connecto.data.ConnectoDatabaseHelper
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.MessageDto
import com.example.connecto.ui.components.AddFriendDialog
import com.example.connecto.ui.components.ChannelNavigationDrawer
import com.example.connecto.ui.components.ConnectoEmojiPicker
import com.example.connecto.ui.components.ConnectoTopHeader
import com.example.connecto.ui.designsystem.ConnectoDivider
import com.example.connecto.ui.designsystem.ConnectoRadius
import com.example.connecto.ui.designsystem.ConnectoSpacing
import com.example.connecto.ui.designsystem.LocalConnectoColors
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.ShimmerMessageFeed
import com.example.connecto.ui.theme.StaggeredReveal
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.pressScaleEffect
import com.example.connecto.voice.VoiceCallManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChannelQuickChip(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val description: String = ""
)

/**
 * Aesthetic & Professional Channels Workspace Screen.
 * Provides real-time channel chats, instant cache-first message feeds,
 * auto-scroll to latest messages, modern chat bubbles & reaction support,
 * and high-end input composer styling.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ChannelsWorkspaceScreen(
    currentUsername: String = ConnectoApiClient.currentUsername ?: "test1",
    initialChannelId: String = "general",
    onOpenProfile: () -> Unit = {},
    onOpenNotifications: (() -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    onNavigateToCalls: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val colors = LocalConnectoColors.current
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()
    val dbHelper = remember { ConnectoDatabaseHelper.getInstance(context) }
    val sharedPrefs = remember { context.getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val savedChannelId = remember { sharedPrefs.getString("last_active_channel", initialChannelId) ?: initialChannelId }
    var selectedChannelId by rememberSaveable { mutableStateOf(savedChannelId) }

    // Channel metadata list
    val defaultChannels = remember {
        listOf(
            ChannelQuickChip("general", "general", Icons.Rounded.Forum, "Global community chat & team discussion"),
            ChannelQuickChip("announcements", "announcements", Icons.Rounded.Campaign, "Official updates & platform release notes"),
            ChannelQuickChip("gaming", "gaming", Icons.Rounded.SportsEsports, "Casual discussion, lounge & off-topic"),
            ChannelQuickChip("tournaments", "tournaments", Icons.Rounded.EmojiEvents, "Free Fire esports tournaments & matches"),
            ChannelQuickChip("clips", "clips", Icons.Rounded.PlayCircle, "Community gaming highlights & clutches"),
            ChannelQuickChip("voice-lounge", "voice-lounge", Icons.AutoMirrored.Rounded.VolumeUp, "Voice & screen sharing lounge")
        )
    }

    val activeChip = defaultChannels.find { it.id.equals(selectedChannelId, ignoreCase = true) || it.name.equals(selectedChannelId, ignoreCase = true) }
        ?: defaultChannels[0]

    // Messages feed state
    val messages = remember { mutableStateListOf<MessageDto>() }
    var isLoadingMessages by remember { mutableStateOf(true) }
    var typedMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var isInputFocused by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    // Local reactions storage: messageId -> list of emoji strings
    val channelReactions = remember { mutableStateMapOf<String, List<String>>() }

    val listState = rememberLazyListState()

    // 1. Instant Cache-First Loading + Background Network Sync
    LaunchedEffect(selectedChannelId) {
        val cleanCh = selectedChannelId.trim().lowercase().removePrefix("#")
        sharedPrefs.edit().putString("last_active_channel", cleanCh).apply()
        VoiceCallManager.subscribeChannel(cleanCh)

        // Clear previous channel's messages to avoid showing stale content
        messages.clear()
        isLoadingMessages = true

        // Instant local cache display (0ms delay)
        withContext(Dispatchers.IO) {
            val cached = dbHelper.getMessagesForChannel(cleanCh, limit = 50)
            if (cached.isNotEmpty()) {
                val distinctCached = cached.reversed().distinctBy { it.id }
                withContext(Dispatchers.Main) {
                    messages.clear()
                    messages.addAll(distinctCached)
                    isLoadingMessages = false
                    delay(50)
                    try { listState.scrollToItem(messages.size) } catch (_: Exception) {}
                }
            }
        }

        // Parallel network refresh
        withContext(Dispatchers.IO) {
            try {
                val res = ConnectoApiClient.getMessages(channelId = cleanCh, limit = 50)
                if (res.isSuccess) {
                    val fresh = res.getOrThrow().distinctBy { it.id }
                    withContext(Dispatchers.Main) {
                        messages.clear()
                        messages.addAll(fresh)
                        isLoadingMessages = false
                        delay(60)
                        try { listState.scrollToItem(messages.size) } catch (_: Exception) {}
                    }
                    dbHelper.saveMessages(cleanCh, fresh)
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingMessages = false
                }
            }
        }
    }

    // Auto-scroll on new message received while inside channel
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val targetIdx = messages.size
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val isNearBottom = lastVisible >= (targetIdx - 3).coerceAtLeast(0)
            if (isNearBottom) {
                try { listState.animateScrollToItem(targetIdx) } catch (_: Exception) {}
            }
        }
    }

    // 2. Real-time WebSocket Message Bus Listener
    LaunchedEffect(selectedChannelId) {
        val cleanCh = selectedChannelId.trim().lowercase().removePrefix("#")
        VoiceCallManager.incomingChannelMessages.collect { incoming ->
            val incCh = incoming.channelId?.trim()?.lowercase()?.removePrefix("#") ?: ""
            if (incCh.equals(cleanCh, ignoreCase = true) || incCh.equals(activeChip.id, ignoreCase = true) || incCh.equals(activeChip.name, ignoreCase = true)) {
                val tempIdx = messages.indexOfFirst { it.id.startsWith("temp_") && it.content == incoming.content && it.authorId == incoming.authorId }
                if (tempIdx >= 0) {
                    messages[tempIdx] = incoming
                } else if (messages.none { it.id == incoming.id }) {
                    messages.add(incoming)
                }
                dbHelper.saveMessages(cleanCh, listOf(incoming))
                if (messages.size > 0) {
                    try { listState.animateScrollToItem(messages.size) } catch (_: Exception) {}
                }
            }
        }
    }

    // Send Message Handler
    fun handleSendMessage() {
        val text = typedMessage.trim()
        if (text.isEmpty() || isSending) return

        isSending = true
        typedMessage = ""
        focusManager.clearFocus()
        showEmojiPicker = false

        val cleanCh = selectedChannelId.trim().lowercase().removePrefix("#")
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).format(Date())
        val displayName = ConnectoApiClient.currentUserDisplayName ?: currentUsername

        val optimisticMsg = MessageDto(
            id = "temp_${UUID.randomUUID()}",
            channelId = cleanCh,
            authorId = currentUsername,
            authorName = displayName,
            authorAvatar = ConnectoApiClient.currentUserAvatarState.value,
            content = text,
            createdAt = nowIso,
            type = "text"
        )

        // Optimistic UI append
        messages.add(optimisticMsg)
        coroutineScope.launch {
            if (messages.size > 0) {
                try { listState.animateScrollToItem(messages.size) } catch (_: Exception) {}
            }
        }

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val sendRes = ConnectoApiClient.sendMessage(channelId = cleanCh, content = text)
                if (sendRes.isSuccess) {
                    val serverMsg = sendRes.getOrThrow()
                    withContext(Dispatchers.Main) {
                        val idx = messages.indexOfFirst { it.id == optimisticMsg.id }
                        if (idx >= 0) {
                            messages[idx] = serverMsg
                        }
                    }
                    dbHelper.saveMessages(cleanCh, listOf(serverMsg))
                } else {
                    dbHelper.saveMessages(cleanCh, listOf(optimisticMsg))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error sending: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isSending = false
                }
            }
        }
    }

    ChannelNavigationDrawer(
        drawerState = drawerState,
        selectedChannelId = selectedChannelId,
        onChannelSelected = { newCh ->
            selectedChannelId = newCh
        },
        onCloseDrawer = {
            coroutineScope.launch { drawerState.close() }
        },
        currentUsername = currentUsername,
        onProfileClick = onOpenProfile
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ================= STANDARDIZED TOP HEADER BAR =================
                ConnectoTopHeader(
                    title = "#${activeChip.name}",
                    subtitle = activeChip.description,
                    userInitial = currentUsername.firstOrNull()?.uppercase() ?: "C",
                    onMenuClick = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onAddFriendClick = { showAddFriendDialog = true },
                    onAvatarClick = onOpenProfile,
                    customAction = if (activeChip.id == "voice-lounge") {
                        {
                            Box(
                                modifier = Modifier
                                    .pressScaleEffect(onClick = { onNavigateToCalls?.invoke() })
                                    .clip(RoundedCornerShape(ConnectoRadius.sm))
                                    .background(Brush.linearGradient(gradientColors))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = null,
                                        tint = contentOnGradient,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Join Voice",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = contentOnGradient
                                    )
                                }
                            }
                        }
                    } else null
                )



                // ================= MESSAGES TIMELINE FEED =================
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (isLoadingMessages && messages.isEmpty()) {
                        ShimmerMessageFeed(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                        )
                    } else if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tag,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Welcome to #${activeChip.name}!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "This is the start of the #${activeChip.name} channel. Send a message to start the conversation!",
                                    fontSize = 13.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = ConnectoSpacing.md),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Welcome Banner inside the feed (visible when scrolled to top)
                            item(key = "feed_top_welcome_header") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(colors.surfaceVariant)
                                                .border(1.dp, colors.borderSubtle, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = activeChip.icon,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Welcome to #${activeChip.name}!",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (activeChip.description.isNotBlank()) activeChip.description
                                                   else "This is the beginning of #${activeChip.name}. Start chatting!",
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        ConnectoDivider(thickness = 0.5.dp)
                                    }
                                }
                            }

                            itemsIndexed(messages, key = { idx, msg -> "${msg.id}_$idx" }) { idx, msg ->
                                val currentDate = formatChannelDate(msg.createdAt)
                                val prevDate = if (idx > 0) formatChannelDate(messages[idx - 1].createdAt) else null
                                val showDateDivider = prevDate == null || currentDate != prevDate

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    if (showDateDivider) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(colors.surfaceVariant.copy(alpha = 0.85f))
                                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = currentDate,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    }

                                    val isMe = msg.authorId.equals(currentUsername, ignoreCase = true) ||
                                               msg.authorName.equals(currentUsername, ignoreCase = true)

                                    val msgReactions = channelReactions[msg.id] ?: run {
                                        val hash = kotlin.math.abs(msg.id.hashCode())
                                        when (hash % 3) {
                                            0 -> listOf("🔥", "🚀", "⚡", "❤️")
                                            1 -> listOf("🎮", "👾", "🏆")
                                            else -> listOf("👍", "💯", "🎉")
                                        }
                                    }

                                    StaggeredReveal(index = idx.coerceAtMost(6)) {
                                        ChannelMessageCard(
                                            message = msg,
                                            isMe = isMe,
                                            gradientColors = gradientColors,
                                            reactions = msgReactions,
                                            onToggleReaction = { emoji ->
                                                val currentList = msgReactions.toMutableList()
                                                if (currentList.contains(emoji)) {
                                                    currentList.remove(emoji)
                                                } else {
                                                    currentList.add(emoji)
                                                }
                                                channelReactions[msg.id] = currentList
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Floating "Scroll to Bottom" FAB
                    val canScrollDown by remember {
                        derivedStateOf {
                            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                            val totalCount = listState.layoutInfo.totalItemsCount
                            totalCount > 0 && lastVisible < totalCount - 1
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = canScrollDown && messages.isNotEmpty(),
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 0.8f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, colors.primary, CircleShape)
                                .shadow(6.dp, CircleShape)
                                .pressScaleEffect(
                                    onClick = {
                                        coroutineScope.launch {
                                            if (messages.isNotEmpty()) {
                                                try {
                                                    listState.animateScrollToItem(messages.size)
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Scroll to bottom",
                                tint = colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // ================= DOCKED CHAT INPUT COMPOSER =================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .background(colors.surface)
                        .padding(horizontal = ConnectoSpacing.md, vertical = 8.dp)
                ) {
                    val hasText = typedMessage.trim().isNotEmpty()

                    val focusBorderColor by animateColorAsState(
                        targetValue = if (isInputFocused) Color(0xFF6366F1)
                                      else if (colors.isDark) Color(0xFF26262E)
                                      else Color(0xFFE2E8F0),
                        animationSpec = tween(220),
                        label = "inputBorder"
                    )
                    val glowElevation by animateDpAsState(
                        targetValue = if (isInputFocused) 6.dp else 1.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "inputGlow"
                    )

                    // Spring Press Micro-Interactions for Send Button
                    val sendInteractionSource = remember { MutableInteractionSource() }
                    val isSendPressed by sendInteractionSource.collectIsPressedAsState()
                    val sendScale by animateFloatAsState(
                        targetValue = if (isSendPressed) 0.92f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "sendScale"
                    )
                    val sendTranslateY by animateDpAsState(
                        targetValue = if (isSendPressed) 1.5.dp else 0.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "sendTranslateY"
                    )

                    // Spring Press Micro-Interactions for Emoji Button
                    val emojiInteractionSource = remember { MutableInteractionSource() }
                    val isEmojiPressed by emojiInteractionSource.collectIsPressedAsState()
                    val emojiScale by animateFloatAsState(
                        targetValue = if (isEmojiPressed) 0.90f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "emojiScale"
                    )
                    val emojiTranslateY by animateDpAsState(
                        targetValue = if (isEmojiPressed) 1.5.dp else 0.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "emojiTranslateY"
                    )

                    // Attachment Action Interaction
                    val attachInteraction = remember { MutableInteractionSource() }
                    val isAttachPressed by attachInteraction.collectIsPressedAsState()
                    val attachScale by animateFloatAsState(
                        targetValue = if (isAttachPressed) 0.88f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "attachScale"
                    )

                    // Stack of the Emoji Section (Expandable Drawer Sheet stacked above bottom input bar)
                    AnimatedVisibility(
                        visible = showEmojiPicker,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(180)) + expandVertically(),
                        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(tween(150)) + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth()
                        ) {
                            ConnectoEmojiPicker(
                                onEmojiSelected = { emoji ->
                                    typedMessage += emoji
                                },
                                onClose = { showEmojiPicker = false }
                            )
                        }
                    }

                    // Main Aesthetic Unified Input Capsule (matches Screenshot 3)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = glowElevation,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = Color(0xFF6366F1).copy(alpha = 0.30f),
                                ambientColor = Color.Black.copy(alpha = 0.35f)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (colors.isDark) Color(0xFF0E1017) else Color(0xFFFFFFFF))
                            .border(
                                width = if (isInputFocused) 1.5.dp else 1.dp,
                                color = focusBorderColor,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Emoji / Reaction Trigger on Left (Tactile Spring Scale)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .graphicsLayer {
                                        scaleX = emojiScale
                                        scaleY = emojiScale
                                        this.translationY = emojiTranslateY.toPx()
                                    }
                                    .clip(CircleShape)
                                    .background(
                                        if (showEmojiPicker) Color(0xFF6366F1).copy(alpha = 0.20f)
                                        else Color.Transparent
                                    )
                                    .clickable(
                                        interactionSource = emojiInteractionSource,
                                        indication = null
                                    ) {
                                        showEmojiPicker = !showEmojiPicker
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "😊",
                                    fontSize = 19.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // 2. Central Text Field with Dynamic "Message #<channel>..." placeholder
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (typedMessage.isEmpty()) {
                                    Text(
                                        text = "Message #${activeChip.name}...",
                                        color = if (colors.isDark) Color(0xFF71717A) else Color(0xFF94A3B8),
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Default,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = typedMessage,
                                    onValueChange = { typedMessage = it },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { isInputFocused = it.isFocused },
                                    textStyle = TextStyle(
                                        color = if (colors.isDark) Color.White else Color(0xFF0F172A),
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Default
                                    ),
                                    cursorBrush = SolidColor(Color(0xFF6366F1)),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { handleSendMessage() })
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // 3. Circular Dark Send Button on Right (Tactile Spring Scale & Rotation)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .graphicsLayer {
                                        scaleX = sendScale
                                        scaleY = sendScale
                                        this.translationY = sendTranslateY.toPx()
                                    }
                                    .shadow(
                                        elevation = if (hasText) 3.dp else 0.dp,
                                        shape = CircleShape,
                                        spotColor = Color(0xFF6366F1).copy(alpha = 0.4f)
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        if (hasText) Color(0xFF1E2130)
                                        else (if (colors.isDark) Color(0xFF141722) else Color(0xFFF1F5F9))
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (hasText) Color(0xFF6366F1).copy(alpha = 0.5f)
                                                else (if (colors.isDark) Color(0xFF1E2232) else Color(0xFFE2E8F0)),
                                        shape = CircleShape
                                    )
                                    .clickable(
                                        interactionSource = sendInteractionSource,
                                        indication = null,
                                        enabled = hasText && !isSending
                                    ) {
                                        handleSendMessage()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSending) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = "Send",
                                        tint = if (hasText) Color.White else (if (colors.isDark) Color(0xFF52525B) else Color(0xFF94A3B8)),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (showAddFriendDialog) {
                AddFriendDialog(
                    currentUsername = currentUsername,
                    onDismissRequest = { showAddFriendDialog = false },
                    onFriendAdded = { showAddFriendDialog = false }
                )
            }
        }
    }
}

/**
 * Modern Chat Bubble & Message Card Component in Channel Feed.
 * Distinguishes user messages, supports media preview, and interactive emoji reactions.
 */
@Composable
private fun ChannelMessageCard(
    message: MessageDto,
    isMe: Boolean,
    gradientColors: List<Color>,
    reactions: List<String>,
    onToggleReaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalConnectoColors.current
    val contentOnGradient = getContentColorOnAccentGradient()
    val haptic = LocalHapticFeedback.current
    var showReactionPicker by remember { mutableStateOf(false) }

    // Formatted time — handles ISO-T and space-separated timestamps
    val formattedTime = remember(message.createdAt) {
        try {
            val raw = message.createdAt.substringBefore(".")
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            val d = if (message.createdAt.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(raw)
            } else {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(raw)
            }
            if (d != null) formatter.format(d) else "Just now"
        } catch (_: Exception) {
            "Just now"
        }
    }

    val isPending = message.id.startsWith("temp_")
    val isImageContent = remember(message.content) {
        val trimmed = message.content.trim().lowercase()
        trimmed.startsWith("http") && (trimmed.endsWith(".jpg") || trimmed.endsWith(".png") || trimmed.endsWith(".jpeg") || trimmed.endsWith(".webp") || trimmed.endsWith(".gif"))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left Avatar for other members
            if (!isMe) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.borderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = if (message.authorName.isNotBlank()) message.authorName.first().toString().uppercase() else "C"
                    Text(
                        text = initial,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (!message.authorAvatar.isNullOrBlank()) {
                        val fullAvatar = if (message.authorAvatar.startsWith("/")) "${com.example.connecto.network.ConnectoNetworkConfig.activeBaseUrl}${message.authorAvatar}" else message.authorAvatar
                        AsyncImage(
                            model = fullAvatar,
                            contentDescription = message.authorName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Bubble Body
            Column(
                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                modifier = Modifier.widthIn(min = 80.dp, max = 310.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(
                            if (isMe) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                            else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                        )
                        .background(
                            if (isMe) colors.primary.copy(alpha = 0.12f)
                            else colors.surfaceVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (isMe) colors.primary.copy(alpha = 0.35f) else colors.borderSubtle,
                            shape = if (isMe) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                            else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showReactionPicker = !showReactionPicker
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Column {
                        // Header row (Author Name + Timestamp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isMe) "You" else message.authorName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) colors.primary else colors.textPrimary
                                )
                                if (isMe) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.primary.copy(alpha = 0.18f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "YOU",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formattedTime,
                                    fontSize = 10.sp,
                                    color = colors.textSecondary
                                )
                                if (isMe) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isPending) Icons.Rounded.Schedule else Icons.Rounded.DoneAll,
                                        contentDescription = null,
                                        tint = if (isPending) colors.textDisabled else Color(0xFF67E8F9),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Message Text or Image Preview
                        if (isImageContent) {
                            AsyncImage(
                                model = message.content.trim(),
                                contentDescription = "Attached image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Text(
                                text = message.content,
                                fontSize = 13.5.sp,
                                color = colors.textPrimary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                // Interactive Quick Emoji Reaction Bar (pops up on tap/long press)
                AnimatedVisibility(
                    visible = showReactionPicker,
                    enter = scaleIn(
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
                        initialScale = 0.7f
                    ) + fadeIn(animationSpec = tween(140)),
                    exit = scaleOut(targetScale = 0.7f) + fadeOut(animationSpec = tween(120))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val quickEmojis = listOf("👍", "❤️", "😂", "🔥", "🚀", "🎉")
                        quickEmojis.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleReaction(emoji)
                                        showReactionPicker = false
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                // Stack of the Emoji Section below Bubble
                if (reactions.isNotEmpty()) {
                    val uniqueEmojis = reactions.distinct()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // 1. Stacked Overlapping Emoji Discs Pill
                        val topStack = uniqueEmojis.take(4)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primary.copy(alpha = 0.12f))
                                .border(1.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showReactionPicker = !showReactionPicker }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val stackWidth = ((topStack.size - 1) * 13 + 20).dp
                                Box(
                                    modifier = Modifier
                                        .height(20.dp)
                                        .width(stackWidth)
                                ) {
                                    topStack.forEachIndexed { index, em ->
                                        Box(
                                            modifier = Modifier
                                                .offset(x = (index * 13).dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(if (colors.isDark) Color(0xFF141418) else Color.White)
                                                .border(1.dp, colors.primary.copy(alpha = 0.3f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = em, fontSize = 10.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${reactions.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }

                        // 2. Interactive Individual Reaction Badges
                        uniqueEmojis.forEach { emoji ->
                            val count = reactions.count { it == emoji }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surfaceVariant)
                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onToggleReaction(emoji)
                                    }
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = emoji, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = count.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }

                        // 3. Quick '+' Add Reaction Pill Button
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.surfaceVariant.copy(alpha = 0.7f))
                                .border(1.dp, colors.borderSubtle, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    showReactionPicker = !showReactionPicker
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Right Avatar for current user
            if (isMe) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = if (message.authorName.isNotBlank()) message.authorName.first().toString().uppercase() else "U"
                    Text(
                        text = initial,
                        color = contentOnGradient,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (!message.authorAvatar.isNullOrBlank()) {
                        val fullAvatar = if (message.authorAvatar.startsWith("/")) "${com.example.connecto.network.ConnectoNetworkConfig.activeBaseUrl}${message.authorAvatar}" else message.authorAvatar
                        AsyncImage(
                            model = fullAvatar,
                            contentDescription = message.authorName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formats a message ISO timestamp into a human-friendly date divider.
 */
private fun formatChannelDate(isoDate: String): String {
    return try {
        val raw = isoDate.substringBefore(".")
        val date = if (isoDate.contains("T")) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(raw)
        } else {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(raw)
        } ?: return "Today"

        val nowCal = java.util.Calendar.getInstance()
        val msgCal = java.util.Calendar.getInstance().apply { time = date }

        if (nowCal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
            nowCal.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            "Today"
        } else if (nowCal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
            nowCal.get(java.util.Calendar.DAY_OF_YEAR) - msgCal.get(java.util.Calendar.DAY_OF_YEAR) == 1) {
            "Yesterday"
        } else {
            SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date)
        }
    } catch (_: Exception) {
        "Today"
    }
}
