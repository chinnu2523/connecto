package com.example.connecto.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseInCubic
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.mutableFloatStateOf
import kotlin.math.roundToInt
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.example.connecto.network.PinnedMessageDto
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
import com.example.connecto.ui.theme.glassCanvasMesh
import com.example.connecto.ui.theme.liquidGlassCanvasMesh
import com.example.connecto.ui.theme.glassBubble
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
    onDrawerStateChanged: ((Boolean) -> Unit)? = null,
    scrollToTopTrigger: Int = 0,
    scrollToBottomTrigger: Int = 0,
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

    var isDrawerOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isDrawerOpen) {
        onDrawerStateChanged?.invoke(isDrawerOpen)
    }

    val savedChannelId = remember { sharedPrefs.getString("last_active_channel", initialChannelId) ?: initialChannelId }
    var selectedChannelId by rememberSaveable { mutableStateOf(savedChannelId) }

    // Sync active channel conversation with ConnectoNotificationManager for foreground suppression and tray cleanup
    androidx.compose.runtime.DisposableEffect(selectedChannelId) {
        val cleanChan = selectedChannelId.trim().lowercase().removePrefix("#")
        com.example.connecto.notifications.ConnectoNotificationManager.currentActiveConversationId = cleanChan
        com.example.connecto.notifications.ConnectoNotificationManager.cancelConversationNotification(cleanChan)
        com.example.connecto.notifications.ConnectoNotificationManager.cancelConversationNotification(selectedChannelId)
        onDispose {
            if (com.example.connecto.notifications.ConnectoNotificationManager.currentActiveConversationId == cleanChan) {
                com.example.connecto.notifications.ConnectoNotificationManager.currentActiveConversationId = null
            }
        }
    }

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

    val isGuest = remember(currentUsername) {
        currentUsername.trim().lowercase().removePrefix("@") == "guest" ||
        (ConnectoApiClient.currentUsername?.trim()?.lowercase()?.removePrefix("@") == "guest") ||
        ConnectoApiClient.sessionToken == "guest_token_sandbox"
    }

    val cleanSelectedChannel = remember(selectedChannelId) {
        selectedChannelId.trim().lowercase().removePrefix("#")
    }

    val deletedForMeSet = remember(currentUsername) {
        sharedPrefs.getStringSet("deleted_for_me_$currentUsername", emptySet())?.toSet() ?: emptySet()
    }

    // Seed cached messages synchronously so LazyColumn never starts at index 0 (welcome banner / oldest messages)
    val initialCached = remember(cleanSelectedChannel, isGuest) {
        if (isGuest) {
            getGuestChannelMockMessages(cleanSelectedChannel)
        } else {
            try {
                val cached = dbHelper.getMessagesForChannel(cleanSelectedChannel, limit = 100)
                android.util.Log.e("ChannelsDebug", "initialCached for '$cleanSelectedChannel': rawCached=${cached.size}")
                if (cached.isNotEmpty()) {
                    val res = cached.distinctBy { it.id }.filterNot { deletedForMeSet.contains(it.id) }
                    android.util.Log.e("ChannelsDebug", "initialCached for '$cleanSelectedChannel': distinctCount=${res.size}, first=${res.firstOrNull()?.id}, last=${res.lastOrNull()?.id}")
                    res
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("ChannelsDebug", "initialCached exception: ${e.message}", e)
                emptyList()
            }
        }
    }

    // Messages feed state initialized with cached messages from frame 1
    val messages = remember(cleanSelectedChannel) {
        mutableStateListOf<MessageDto>().apply { addAll(initialCached) }
    }
    var isLoadingMessages by remember(cleanSelectedChannel) {
        mutableStateOf(if (isGuest) false else initialCached.isEmpty())
    }
    var typedMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isInputFocused by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var replyingTo by remember { mutableStateOf<MessageDto?>(null) }

    // Seed pinned message from cached preferences synchronously
    val initialPinnedMessage = remember(cleanSelectedChannel, isGuest) {
        if (isGuest) {
            getGuestPinnedMessage(cleanSelectedChannel)
        } else {
            val cachedPinId = sharedPrefs.getString("pinned_msg_ch_$cleanSelectedChannel", null)
            val cachedPinContent = sharedPrefs.getString("pinned_content_ch_$cleanSelectedChannel", null)
            val cachedPinAuthor = sharedPrefs.getString("pinned_author_ch_$cleanSelectedChannel", null)
            if (!cachedPinId.isNullOrBlank() && !cachedPinContent.isNullOrBlank()) {
                PinnedMessageDto(
                    messageId = cachedPinId,
                    channelId = cleanSelectedChannel,
                    authorName = cachedPinAuthor ?: "Pinned",
                    content = cachedPinContent,
                    pinnedBy = "",
                    pinnedAt = ""
                )
            } else {
                null
            }
        }
    }
    var pinnedMessageForChannel by remember(cleanSelectedChannel) {
        mutableStateOf<PinnedMessageDto?>(initialPinnedMessage)
    }
    var messageForAction by remember { mutableStateOf<MessageDto?>(null) }
    var showMessageActionDialog by remember { mutableStateOf(false) }
    var selectedMessageForAction by remember { mutableStateOf<MessageDto?>(null) }
    var showMoreOptionsMenu by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<MessageDto?>(null) }

    var isChannelSearchOpen by rememberSaveable { mutableStateOf(false) }
    var channelSearchQuery by rememberSaveable { mutableStateOf("") }
    var showChannelOptionsMenu by remember { mutableStateOf(false) }
    var showChannelInfoDialog by remember { mutableStateOf(false) }
    var isChannelMuted by rememberSaveable(cleanSelectedChannel) {
        mutableStateOf(sharedPrefs.getBoolean("muted_ch_$cleanSelectedChannel", false))
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isDrawerOpen) 180f else 0f,
        animationSpec = tween(durationMillis = 280, easing = EaseInOutCubic),
        label = "drawerChevron"
    )

    val displayMessages = remember(messages.toList(), channelSearchQuery) {
        if (channelSearchQuery.isBlank()) {
            messages
        } else {
            val q = channelSearchQuery.trim().lowercase()
            messages.filter { msg ->
                msg.content.lowercase().contains(q) ||
                msg.authorName.lowercase().contains(q) ||
                msg.authorId.lowercase().contains(q)
            }
        }
    }

    BackHandler(enabled = isDrawerOpen) {
        isDrawerOpen = false
    }

    BackHandler(enabled = selectedMessageForAction != null) {
        selectedMessageForAction = null
    }

    // Friends list for detecting friend-only private DM reply in channels
    var friendsList by remember { mutableStateOf<List<FriendItem>>(emptyList()) }

    LaunchedEffect(currentUsername) {
        withContext(Dispatchers.IO) {
            val cached = dbHelper.getCachedFriends()
            withContext(Dispatchers.Main) {
                friendsList = cached
            }
            try {
                val fresh = ConnectoApiClient.getFriends(username = currentUsername).getOrNull()
                if (fresh != null) {
                    val converted = fresh.map { f ->
                        FriendItem(
                            id = f.friendId,
                            name = f.friendDisplayName.ifBlank { f.friendUsername },
                            handle = f.friendUsername,
                            initial = (f.friendDisplayName.ifBlank { f.friendUsername }).take(1).uppercase(),
                            status = f.status,
                            bio = "Connecto Friend",
                            lastMessage = "",
                            isOnline = f.isOnline,
                            avatarUrl = f.friendAvatarUrl
                        )
                    }
                    withContext(Dispatchers.Main) {
                        friendsList = converted
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Match replyingTo message author with accepted friends
    val matchedFriendTarget by remember(replyingTo, friendsList) {
        derivedStateOf {
            val target = replyingTo ?: return@derivedStateOf null
            val authorIdClean = target.authorId.trim().lowercase().removePrefix("@")
            val authorNameClean = target.authorName.trim().lowercase().removePrefix("@")

            friendsList.firstOrNull { f ->
                val fHandle = f.handle.trim().lowercase().removePrefix("@")
                val fId = f.id.trim().lowercase()
                val fName = f.name.trim().lowercase()

                (fHandle.isNotEmpty() && (authorIdClean == fHandle || authorNameClean == fHandle)) ||
                (fId.isNotEmpty() && (authorIdClean == fId || authorNameClean == fId)) ||
                (fName.isNotEmpty() && authorNameClean == fName)
            }
        }
    }

    // Check if user has typed @<userId> while replying to a friend's message
    val isPrivateDmReply by remember(typedMessage, matchedFriendTarget, replyingTo) {
        derivedStateOf {
            if (replyingTo == null || matchedFriendTarget == null) return@derivedStateOf false
            val textTrim = typedMessage.trim()
            val targetU = matchedFriendTarget?.handle?.trim()?.lowercase()?.removePrefix("@") ?: ""
            val targetI = matchedFriendTarget?.id?.trim()?.lowercase() ?: ""
            if (targetU.isNotEmpty()) {
                val handleRegex = Regex("^@${Regex.escape(targetU)}(\\b|\\s|:|$)", RegexOption.IGNORE_CASE)
                if (handleRegex.containsMatchIn(textTrim)) return@derivedStateOf true
            }
            if (targetI.isNotEmpty()) {
                val idRegex = Regex("^@${Regex.escape(targetI)}(\\b|\\s|:|$)", RegexOption.IGNORE_CASE)
                if (idRegex.containsMatchIn(textTrim)) return@derivedStateOf true
            }
            false
        }
    }

    // Local reactions storage: messageId -> map of emoji to list of userIds
    val channelReactions = remember { mutableStateMapOf<String, Map<String, List<String>>>() }
    var highlightedMessageId by remember { mutableStateOf<String?>(null) }
    val messageInputFocusRequester = remember { FocusRequester() }
    var showFullEmojiDialogForMessage by remember { mutableStateOf<MessageDto?>(null) }

    // Real-Time Reaction Observer via WebSocket
    LaunchedEffect(Unit) {
        VoiceCallManager.reactionEvents.collect { event ->
            val msgId = event.messageId
            val emoji = event.emoji
            val uId = event.userId
            val prevMap = channelReactions[msgId] ?: emptyMap()
            val currentMap = prevMap.toMutableMap()
            val users = (currentMap[emoji] ?: emptyList()).toMutableList()
            if (users.any { it.equals(uId, ignoreCase = true) }) {
                users.removeAll { it.equals(uId, ignoreCase = true) }
            } else {
                users.add(uId)
            }
            if (users.isEmpty()) {
                currentMap.remove(emoji)
            } else {
                currentMap[emoji] = users
            }
            channelReactions[msgId] = currentMap
        }
    }

    val initialScrollIndex = remember(cleanSelectedChannel) {
        if (initialCached.isNotEmpty()) initialCached.size else 0
    }
    val listState = remember(cleanSelectedChannel) {
        LazyListState(firstVisibleItemIndex = initialScrollIndex)
    }

    var hasScrolledToBottomOnEntry by remember(cleanSelectedChannel) { mutableStateOf(false) }
    LaunchedEffect(cleanSelectedChannel, messages.size) {
        if (messages.isNotEmpty() && !hasScrolledToBottomOnEntry) {
            hasScrolledToBottomOnEntry = true
            try { listState.scrollToItem(messages.size) } catch (_: Exception) {}
        }
    }

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0 && messages.isNotEmpty()) {
            try {
                val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                if (!isAtTop) {
                    if (listState.firstVisibleItemIndex > 15) {
                        listState.scrollToItem(15)
                    }
                    listState.animateScrollToItem(0)
                } else {
                    listState.animateScrollToItem(messages.size)
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(scrollToBottomTrigger) {
        if (scrollToBottomTrigger > 0 && messages.isNotEmpty()) {
            try {
                listState.animateScrollToItem(messages.size)
            } catch (_: Exception) {}
        }
    }

    // 1. Instant Cache-First Loading + Background Network Sync
    LaunchedEffect(selectedChannelId, isGuest) {
        val cleanCh = cleanSelectedChannel
        sharedPrefs.edit().putString("last_active_channel", cleanCh).apply()

        if (isGuest) {
            isLoadingMessages = false
            val mockMessages = getGuestChannelMockMessages(cleanCh)
            messages.clear()
            messages.addAll(mockMessages)
            pinnedMessageForChannel = getGuestPinnedMessage(cleanCh)
            try { listState.scrollToItem(messages.size) } catch (_: Exception) {}
            return@LaunchedEffect
        }

        VoiceCallManager.subscribeChannel(cleanCh)

        val currentDeletedSet = sharedPrefs.getStringSet("deleted_for_me_$currentUsername", emptySet())
            ?.toSet() ?: emptySet()

        // Fallback: If messages was empty at composition, try reading from local SQLite
        if (messages.isEmpty()) {
            withContext(Dispatchers.IO) {
                val cached = dbHelper.getMessagesForChannel(cleanCh, limit = 100)
                if (cached.isNotEmpty()) {
                    val distinctCached = cached.distinctBy { it.id }.filterNot { currentDeletedSet.contains(it.id) }
                    withContext(Dispatchers.Main) {
                        if (messages.isEmpty()) {
                            messages.addAll(distinctCached)
                            isLoadingMessages = false
                            try { listState.scrollToItem(messages.size) } catch (_: Exception) {}
                        }
                    }
                }
            }
        }

        // Parallel network refresh
        withContext(Dispatchers.IO) {
            try {
                val serverPinRes = ConnectoApiClient.getPinnedMessage(cleanCh)
                if (serverPinRes.isSuccess) {
                    val serverPin = serverPinRes.getOrNull()
                    withContext(Dispatchers.Main) {
                        if (serverPin != null) {
                            pinnedMessageForChannel = serverPin
                            sharedPrefs.edit()
                                .putString("pinned_msg_ch_$cleanCh", serverPin.messageId)
                                .putString("pinned_content_ch_$cleanCh", serverPin.content)
                                .putString("pinned_author_ch_$cleanCh", serverPin.authorName)
                                .apply()
                        } else {
                            pinnedMessageForChannel = null
                            sharedPrefs.edit()
                                .remove("pinned_msg_ch_$cleanCh")
                                .remove("pinned_content_ch_$cleanCh")
                                .remove("pinned_author_ch_$cleanCh")
                                .apply()
                        }
                    }
                }
            } catch (_: Exception) {}

            try {
                val res = ConnectoApiClient.getMessages(channelId = cleanCh, limit = 100)
                withContext(Dispatchers.Main) {
                    isLoadingMessages = false
                }
                if (res.isSuccess) {
                    val fresh = res.getOrThrow().distinctBy { it.id }.filterNot { currentDeletedSet.contains(it.id) }
                    withContext(Dispatchers.Main) {
                        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        val wasNearBottom = messages.isEmpty() || lastVisible >= (messages.size - 3).coerceAtLeast(0)

                        // Only reconcile if fresh messages were actually returned from the network.
                        // NEVER purge local SQLite cached messages if the network returns an empty list
                        // (e.g. D1 quota exhaustion, offline fallback, or transient network error)
                        if (fresh.isNotEmpty()) {
                            for (i in fresh.indices) {
                                if (i < messages.size) {
                                    if (messages[i] != fresh[i]) {
                                        messages[i] = fresh[i]
                                    }
                                } else {
                                    messages.add(fresh[i])
                                }
                            }
                            while (messages.size > fresh.size) {
                                messages.removeAt(messages.size - 1)
                            }
                            dbHelper.saveMessages(cleanCh, fresh)
                        }

                        if (wasNearBottom && fresh.isNotEmpty()) {
                            try { listState.scrollToItem(messages.size) } catch (_: Exception) {}
                        }
                    }
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

    // 2. Real-time WebSocket Message Bus Listener (Bypassed for Guest users)
    LaunchedEffect(selectedChannelId, isGuest) {
        if (isGuest) return@LaunchedEffect
        val cleanCh = selectedChannelId.trim().lowercase().removePrefix("#")
        VoiceCallManager.incomingChannelMessages.collect { incoming ->
            val deletedForMeSet = sharedPrefs.getStringSet("deleted_for_me_$currentUsername", emptySet())
                ?.toSet() ?: emptySet()
            if (deletedForMeSet.contains(incoming.id)) return@collect

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

    // Real-time listener for deleted messages broadcast over WebSocket (Bypassed for Guest users)
    LaunchedEffect(isGuest) {
        if (isGuest) return@LaunchedEffect
        VoiceCallManager.deletedMessageIds.collect { deletedId ->
            messages.removeAll { it.id == deletedId }
        }
    }

    // Real-time listener for edited messages broadcast over WebSocket (Bypassed for Guest users)
    LaunchedEffect(isGuest) {
        if (isGuest) return@LaunchedEffect
        VoiceCallManager.editedMessages.collect { evt ->
            val index = messages.indexOfFirst { it.id == evt.messageId }
            if (index != -1) {
                messages[index] = messages[index].copy(content = evt.newContent)
            }
        }
    }

    var channelTypingUser by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(activeChip.id, isGuest) {
        if (isGuest) return@LaunchedEffect
        channelTypingUser = null
        val cleanCh = activeChip.id.trim().lowercase().removePrefix("#")
        val cleanName = activeChip.name.trim().lowercase()
        VoiceCallManager.typingEvents.collect { ev ->
            val evClean = ev.channelId.trim().lowercase().removePrefix("#")
            if (evClean == cleanCh || evClean == cleanName) {
                if (ev.isTyping && !ev.username.equals(currentUsername, ignoreCase = true)) {
                    channelTypingUser = ev.displayName.ifEmpty { ev.username }
                } else if (!ev.isTyping) {
                    if (channelTypingUser.equals(ev.displayName, ignoreCase = true) || channelTypingUser.equals(ev.username, ignoreCase = true)) {
                        channelTypingUser = null
                    }
                }
            }
        }
    }

    LaunchedEffect(channelTypingUser) {
        if (channelTypingUser != null) {
            kotlinx.coroutines.delay(4000)
            channelTypingUser = null
        }
    }

    // Broadcast current user's typing activity in channel, suppressed if Stealth Mode is ON or Guest
    LaunchedEffect(typedMessage, activeChip.id, isGuest) {
        if (isGuest) return@LaunchedEffect
        val cleanCh = activeChip.id.trim().lowercase().removePrefix("#")
        if (cleanCh.isNotEmpty()) {
            val isStealth = ConnectoApiClient.isStealthMode || context.getSharedPreferences("connecto_profile", android.content.Context.MODE_PRIVATE).getBoolean("stealth_mode_${currentUsername}", false)
            if (!isStealth) {
                if (typedMessage.isNotBlank()) {
                    VoiceCallManager.sendTypingStart(cleanCh)
                    kotlinx.coroutines.delay(2500)
                    VoiceCallManager.sendTypingStop(cleanCh)
                } else {
                    VoiceCallManager.sendTypingStop(cleanCh)
                }
            }
        }
    }

    // Send Message Handler
    fun handleSendMessage() {
        val text = typedMessage.trim()
        if (text.isEmpty() || isSending) return

        if (isGuest) {
            val cleanCh = selectedChannelId.trim().lowercase().removePrefix("#")
            val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).format(Date())
            val activeReply = replyingTo
            val guestMsg = MessageDto(
                id = "guest_${UUID.randomUUID()}",
                channelId = cleanCh,
                authorId = "guest",
                authorName = "Guest Explorer",
                authorAvatar = "sparkles",
                content = text,
                createdAt = nowIso,
                type = "text",
                replyToId = activeReply?.id,
                replyToContent = activeReply?.content,
                replyToAuthor = activeReply?.authorName?.ifBlank { activeReply?.authorId }
            )
            typedMessage = ""
            replyingTo = null
            focusManager.clearFocus()
            showEmojiPicker = false
            messages.add(guestMsg)
            coroutineScope.launch {
                try { listState.animateScrollToItem(messages.size) } catch (_: Exception) {}
                delay(1200)
                val replies = listOf(
                    "Great message! Create a free account anytime to chat with real squads and join live voice rooms! 🚀",
                    "Welcome to Connecto! The guest preview lets you test all channels and visual themes.",
                    "Hey Guest Explorer! Join the community by signing up from the Sign In page! 🎮",
                    "Great to have you here! Check out the different tabs: Messages, Calls, and Profile."
                )
                val replyMsg = MessageDto(
                    id = "mock_${UUID.randomUUID()}",
                    channelId = cleanCh,
                    authorId = "ConnectoGuide",
                    authorName = "Connecto Guide",
                    authorAvatar = "robot",
                    content = replies.random(),
                    createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).format(Date()),
                    type = "text",
                    replyToId = guestMsg.id,
                    replyToContent = guestMsg.content,
                    replyToAuthor = guestMsg.authorName
                )
                messages.add(replyMsg)
                try { listState.animateScrollToItem(messages.size) } catch (_: Exception) {}
            }
            return
        }

        editingMessage?.let { editTarget ->
            val newText = text
            val index = messages.indexOfFirst { it.id == editTarget.id }
            if (index != -1) {
                messages[index] = messages[index].copy(content = newText)
            }
            val cleanCh = selectedChannelId.trim().lowercase().removePrefix("#")
            coroutineScope.launch(Dispatchers.IO) {
                dbHelper.updateMessageContent(editTarget.id, newText)
                ConnectoApiClient.editMessage(editTarget.id, newText, cleanCh)
            }
            editingMessage = null
            typedMessage = ""
            focusManager.clearFocus()
            return
        }

        val activeReply = replyingTo
        val targetFriend = matchedFriendTarget
        val isPrivate = isPrivateDmReply && targetFriend != null && activeReply != null

        val stopCh = activeChip.id.trim().lowercase().removePrefix("#")
        if (stopCh.isNotEmpty()) {
            VoiceCallManager.sendTypingStop(stopCh)
        }

        isSending = true
        typedMessage = ""
        replyingTo = null
        focusManager.clearFocus()
        showEmojiPicker = false

        val cleanCh = selectedChannelId.trim().lowercase().removePrefix("#")
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).format(Date())
        val displayName = ConnectoApiClient.currentUserDisplayName ?: currentUsername

        // If swiped friend's message and typed @<user_id>, deliver directly to friend's private chat
        if (isPrivate && targetFriend != null && activeReply != null) {
            val targetU = targetFriend.handle.trim().lowercase().removePrefix("@")
            val targetI = targetFriend.id.trim().lowercase()
            val cleanMe = currentUsername.trim().lowercase().removePrefix("@")
            val canonicalDmId = "dm-${minOf(cleanMe, targetU)}-${maxOf(cleanMe, targetU)}"

            val privateContent = text.replaceFirst(
                Regex("^@(${Regex.escape(targetU)}|${Regex.escape(targetI)})[:\\s]*", RegexOption.IGNORE_CASE),
                ""
            ).trim().ifBlank { text }

            val optDmMsg = MessageDto(
                id = "temp_${UUID.randomUUID()}",
                channelId = canonicalDmId,
                authorId = currentUsername,
                authorName = displayName,
                authorAvatar = ConnectoApiClient.currentUserAvatarState.value,
                content = privateContent,
                createdAt = nowIso,
                type = "text",
                replyToId = activeReply.id,
                replyToContent = "[#$cleanCh] ${activeReply.content}",
                replyToAuthor = activeReply.authorName.ifBlank { activeReply.authorId }
            )

            // Save in local SQLite for offline access and direct DM conversations screen
            coroutineScope.launch(Dispatchers.IO) {
                dbHelper.saveMessage(optDmMsg)
                dbHelper.saveMessages(canonicalDmId, listOf(optDmMsg))
            }

            // Immediately update readPrefs so Messages conversation list shows preview
            try {
                val readPrefs = context.getSharedPreferences("connecto_chat_read_prefs", Context.MODE_PRIVATE)
                val nowMillis = System.currentTimeMillis()
                readPrefs.edit()
                    .putString("last_msg_${targetFriend.id}", privateContent)
                    .putString("last_msg_$targetU", privateContent)
                    .putString("last_time_${targetFriend.id}", "Just now")
                    .putString("last_time_$targetU", "Just now")
                    .putLong("last_timestamp_${targetFriend.id}", nowMillis)
                    .putLong("last_timestamp_$targetU", nowMillis)
                    .apply()
            } catch (_: Exception) {}

            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val sendRes = ConnectoApiClient.sendMessage(
                        channelId = canonicalDmId,
                        content = privateContent,
                        replyToId = activeReply.id,
                        replyToContent = "[#$cleanCh] ${activeReply.content}",
                        replyToAuthor = activeReply.authorName.ifBlank { activeReply.authorId }
                    )
                    withContext(Dispatchers.Main) {
                        if (sendRes.isSuccess) {
                            val serverMsg = sendRes.getOrThrow()
                            withContext(Dispatchers.IO) {
                                dbHelper.saveMessage(serverMsg)
                                dbHelper.saveMessages(canonicalDmId, listOf(serverMsg))
                                if (!serverMsg.channelId.isNullOrBlank() && serverMsg.channelId != canonicalDmId) {
                                    dbHelper.saveMessages(serverMsg.channelId, listOf(serverMsg))
                                    dbHelper.saveDmChannel(canonicalDmId, serverMsg.channelId)
                                }
                            }
                            Toast.makeText(context, "Direct message sent to @$targetU's private chat 🔒", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Message saved to @$targetU's private chat", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error sending private message: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        isSending = false
                    }
                }
            }
            return
        }

        val optimisticMsg = MessageDto(
            id = "temp_${UUID.randomUUID()}",
            channelId = cleanCh,
            authorId = currentUsername,
            authorName = displayName,
            authorAvatar = ConnectoApiClient.currentUserAvatarState.value,
            content = text,
            createdAt = nowIso,
            type = "text",
            replyToId = activeReply?.id,
            replyToContent = activeReply?.content,
            replyToAuthor = activeReply?.authorName?.ifBlank { activeReply?.authorId }
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
                val sendRes = ConnectoApiClient.sendMessage(
                    channelId = cleanCh,
                    content = text,
                    replyToId = activeReply?.id,
                    replyToContent = activeReply?.content,
                    replyToAuthor = activeReply?.authorName?.ifBlank { activeReply?.authorId }
                )
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
        isOpen = isDrawerOpen,
        selectedChannelId = selectedChannelId,
        onChannelSelected = { newCh ->
            selectedChannelId = newCh
        },
        onCloseDrawer = {
            isDrawerOpen = false
        },
        currentUsername = currentUsername,
        onProfileClick = onOpenProfile
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(
                    if (colors.isLiquidGlass) Modifier.liquidGlassCanvasMesh()
                    else if (colors.isGlass) Modifier.glassCanvasMesh()
                    else Modifier.background(colors.background)
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ================= STANDARDIZED TOP HEADER BAR / SELECTION HEADER =================
                val activeSelMsg = selectedMessageForAction
                if (activeSelMsg != null) {
                    val selMsg = activeSelMsg
                    val isMyMsg = remember(selMsg, currentUsername) {
                        val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
                        val myDisplay = (ConnectoApiClient.currentUserDisplayName ?: "").trim().lowercase().removePrefix("@")
                        val myUserId = (ConnectoApiClient.currentUserId ?: "").trim().lowercase()
                        val authorClean = selMsg.authorId.trim().lowercase().removePrefix("@")
                        val authorNameClean = selMsg.authorName.trim().lowercase().removePrefix("@")
                        (myUser.isNotBlank() && (authorClean == myUser || authorNameClean == myUser)) ||
                        (myDisplay.isNotBlank() && (authorClean == myDisplay || authorNameClean == myDisplay)) ||
                        (myUserId.isNotBlank() && (authorClean == myUserId || authorNameClean == myUserId || selMsg.authorId.equals(myUserId, ignoreCase = true)))
                    }
                    val isPinned = pinnedMessageForChannel?.messageId == selMsg.id

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                        color = if (colors.isDark) Color(0xFF13151B) else colors.surface,
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { selectedMessageForAction = null }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Selection",
                                    tint = colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1 selected",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))

                            // Right side 3-dots button
                            Box {
                                IconButton(
                                    onClick = { showMoreOptionsMenu = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Message Options",
                                        tint = colors.textPrimary
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMoreOptionsMenu,
                                    onDismissRequest = { showMoreOptionsMenu = false },
                                    modifier = Modifier.background(if (colors.isDark) Color(0xFF1E2029) else Color.White)
                                ) {
                                    // 1. Pin / Unpin
                                    DropdownMenuItem(
                                        text = { Text(if (isPinned) "Unpin" else "Pin", color = colors.textPrimary) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = null,
                                                tint = colors.primary
                                            )
                                        },
                                        onClick = {
                                            showMoreOptionsMenu = false
                                            if (isGuest) {
                                                selectedMessageForAction = null
                                                Toast.makeText(context, "Sign in to pin messages for other users", Toast.LENGTH_SHORT).show()
                                                return@DropdownMenuItem
                                            }
                                            val cleanCh = activeChip.id.trim().lowercase().removePrefix("#")
                                            if (isPinned) {
                                                pinnedMessageForChannel = null
                                                sharedPrefs.edit()
                                                    .remove("pinned_msg_ch_$cleanCh")
                                                    .remove("pinned_content_ch_$cleanCh")
                                                    .remove("pinned_author_ch_$cleanCh")
                                                    .apply()
                                                coroutineScope.launch { ConnectoApiClient.unpinMessage(cleanCh) }
                                            } else {
                                                pinnedMessageForChannel = PinnedMessageDto(
                                                    messageId = selMsg.id,
                                                    channelId = cleanCh,
                                                    authorName = selMsg.authorName.ifBlank { selMsg.authorId },
                                                    content = selMsg.content,
                                                    pinnedBy = currentUsername,
                                                    pinnedAt = System.currentTimeMillis().toString()
                                                )
                                                sharedPrefs.edit()
                                                    .putString("pinned_msg_ch_$cleanCh", selMsg.id)
                                                    .putString("pinned_content_ch_$cleanCh", selMsg.content)
                                                    .putString("pinned_author_ch_$cleanCh", selMsg.authorName.ifBlank { selMsg.authorId })
                                                    .apply()
                                                coroutineScope.launch { ConnectoApiClient.pinMessage(cleanCh, selMsg.id, currentUsername) }
                                            }
                                            selectedMessageForAction = null
                                        }
                                    )

                                    // 2. Edit (only for own message)
                                    if (isMyMsg) {
                                        DropdownMenuItem(
                                            text = { Text("Edit", color = colors.textPrimary) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = colors.primary
                                                )
                                            },
                                            onClick = {
                                                showMoreOptionsMenu = false
                                                editingMessage = selMsg
                                                typedMessage = selMsg.content
                                                selectedMessageForAction = null
                                            }
                                        )
                                    }

                                    // 3. Delete (only for own message)
                                    if (isMyMsg) {
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = Color(0xFFEF4444)) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEF4444)
                                                )
                                            },
                                            onClick = {
                                                showMoreOptionsMenu = false
                                                val msgId = selMsg.id
                                                val cleanCh = activeChip.id.trim().lowercase().removePrefix("#")
                                                messages.removeAll { it.id == msgId }
                                                if (pinnedMessageForChannel?.messageId == msgId) {
                                                    pinnedMessageForChannel = null
                                                    sharedPrefs.edit()
                                                        .remove("pinned_msg_ch_$cleanCh")
                                                        .remove("pinned_content_ch_$cleanCh")
                                                        .remove("pinned_author_ch_$cleanCh")
                                                        .apply()
                                                }
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    dbHelper.deleteMessage(msgId)
                                                    ConnectoApiClient.deleteMessage(msgId, cleanCh)
                                                }
                                                selectedMessageForAction = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ================= DEDICATED CHANNEL WORKSPACE NAVIGATION BAR =================
                    Surface(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                        color = if (colors.isLiquidGlass) {
                            val alpha = colors.glassAlpha
                            if (alpha <= 0.005f) Color.Transparent
                            else colors.surface.copy(alpha = (alpha * 0.75f).coerceIn(0.15f, 0.90f))
                        } else if (colors.isGlass) {
                            colors.surface.copy(alpha = if (colors.isDark) 0.85f else 0.92f)
                        } else if (colors.isDark) {
                            Color(0xFF0D0F15)
                        } else {
                            colors.surface
                        },
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Row 1: Workspace Selector Capsule + Quick Actions
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Workspace Capsule (Interactive Drawer / Switcher trigger)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .pressScaleEffect(
                                            onClick = {
                                                isDrawerOpen = !isDrawerOpen
                                            },
                                            targetScale = 0.95f
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (activeChip.id == "voice-lounge") OnlineGreen.copy(alpha = 0.15f)
                                                else colors.primary.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = activeChip.icon,
                                            contentDescription = null,
                                            tint = if (activeChip.id == "voice-lounge") OnlineGreen else colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(verticalArrangement = Arrangement.Center) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "#${activeChip.name}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (isChannelMuted) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.NotificationsOff,
                                                    contentDescription = "Muted",
                                                    tint = colors.textSecondary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(OnlineGreen)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Workspace",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textSecondary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Open Workspace Drawer",
                                        tint = colors.textSecondary,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .graphicsLayer { rotationZ = chevronRotation }
                                    )
                                }

                                // Right Actions Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Voice Join button if in voice-lounge
                                    if (activeChip.id == "voice-lounge") {
                                        Box(
                                            modifier = Modifier
                                                .pressScaleEffect(onClick = { onNavigateToCalls?.invoke() })
                                                .clip(RoundedCornerShape(ConnectoRadius.sm))
                                                .background(Brush.linearGradient(gradientColors))
                                                .padding(horizontal = 9.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                VoiceLoungeWaveBars(
                                                    color = contentOnGradient,
                                                    barWidth = 2.dp,
                                                    spacing = 1.5.dp,
                                                    scale = 1.0f,
                                                    modifier = Modifier.height(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = "Join Lounge",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = contentOnGradient
                                                )
                                            }
                                        }
                                    }

                                    // Search In-Channel Toggle Button
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isChannelSearchOpen) colors.primary.copy(alpha = 0.15f)
                                                else if (colors.isDark) Color.White.copy(alpha = 0.05f)
                                                else Color.Black.copy(alpha = 0.04f)
                                            )
                                            .clickable {
                                                isChannelSearchOpen = !isChannelSearchOpen
                                                if (!isChannelSearchOpen) channelSearchQuery = ""
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search Messages",
                                            tint = if (isChannelSearchOpen) colors.primary else colors.textPrimary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }

                                    // 3-Dots Channel Options Menu
                                    Box {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (colors.isDark) Color.White.copy(alpha = 0.05f)
                                                    else Color.Black.copy(alpha = 0.04f)
                                                )
                                                .clickable { showChannelOptionsMenu = true },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Channel Options",
                                                tint = colors.textPrimary,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showChannelOptionsMenu,
                                            onDismissRequest = { showChannelOptionsMenu = false },
                                            shape = RoundedCornerShape(16.dp),
                                            containerColor = if (colors.isDark) Color(0xFF181C28) else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (colors.isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
                                            ),
                                            modifier = Modifier.padding(6.dp)
                                        ) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (isChannelMuted) "Unmute Channel" else "Mute Channel",
                                                        color = colors.textPrimary,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 13.5.sp
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = if (isChannelMuted) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                                        contentDescription = null,
                                                        tint = if (isChannelMuted) colors.success else colors.textSecondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                },
                                                onClick = {
                                                    showChannelOptionsMenu = false
                                                    isChannelMuted = !isChannelMuted
                                                    sharedPrefs.edit().putBoolean("muted_ch_$cleanSelectedChannel", isChannelMuted).apply()
                                                    Toast.makeText(context, if (isChannelMuted) "Muted #${activeChip.name}" else "Unmuted #${activeChip.name}", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                            )
                                        }
                                    }
                                }
                            }

                            // Row 2: In-Channel Expandable Search Bar
                            AnimatedVisibility(
                                visible = isChannelSearchOpen,
                                enter = expandVertically(tween(240, easing = EaseOutCubic)) + fadeIn(tween(220, easing = EaseOutCubic)) + slideInVertically(animationSpec = tween(240, easing = EaseOutCubic), initialOffsetY = { -it / 3 }),
                                exit = shrinkVertically(tween(180, easing = EaseInCubic)) + fadeOut(tween(160, easing = EaseInCubic)) + slideOutVertically(animationSpec = tween(180, easing = EaseInCubic), targetOffsetY = { -it / 3 })
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (colors.isDark) Color(0xFF161822) else Color(0xFFEEF0F6))
                                        .border(1.dp, colors.borderSubtle.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (channelSearchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search in #${activeChip.name}...",
                                                    fontSize = 13.sp,
                                                    color = colors.textSecondary.copy(alpha = 0.7f)
                                                )
                                            }
                                            BasicTextField(
                                                value = channelSearchQuery,
                                                onValueChange = { channelSearchQuery = it },
                                                textStyle = TextStyle(
                                                    fontSize = 13.sp,
                                                    color = colors.textPrimary
                                                ),
                                                cursorBrush = SolidColor(colors.primary),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        if (channelSearchQuery.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${displayMessages.size} found",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.primary,
                                                style = TextStyle(fontFeatureSettings = "tnum")
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .clickable { channelSearchQuery = "" },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear",
                                                    tint = colors.textSecondary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    isChannelSearchOpen = false
                                                    channelSearchQuery = ""
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close Search",
                                                tint = colors.textPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Microscopic Hairline Divider separating top header from messages
                            ConnectoDivider(
                                thickness = 0.5.dp,
                                color = colors.borderSubtle.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // ================= CHANNEL INFO DIALOG =================
                if (showChannelInfoDialog) {
                    AlertDialog(
                        onDismissRequest = { showChannelInfoDialog = false },
                        containerColor = if (colors.isDark) Color(0xFF161821) else Color.White,
                        shape = RoundedCornerShape(16.dp),
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = activeChip.icon,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "#${activeChip.name}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = activeChip.description.ifBlank { "Connecto official community channel." },
                                    fontSize = 13.5.sp,
                                    color = colors.textSecondary,
                                    lineHeight = 18.sp
                                )
                                ConnectoDivider(thickness = 0.5.dp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Channel Messages",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = "${messages.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        style = TextStyle(fontFeatureSettings = "tnum")
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Access Level",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                    Text(
                                        text = "Public Community",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primary
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showChannelInfoDialog = false }) {
                                Text(text = "Close", fontWeight = FontWeight.Bold, color = colors.primary)
                            }
                        }
                    )
                }



                // ================= GUEST PREVIEW BANNER =================
                if (isGuest) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 3.dp),
                        color = colors.surfaceVariant.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guest Explorer — Showing simulated community feed",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // ================= PINNED MESSAGE BANNER =================
                AnimatedVisibility(
                    visible = pinnedMessageForChannel != null,
                    enter = expandVertically(tween(240, easing = EaseOutCubic)) + fadeIn(tween(220, easing = EaseOutCubic)),
                    exit = shrinkVertically(tween(180, easing = EaseInCubic)) + fadeOut(tween(160, easing = EaseInCubic))
                ) {
                    val pinned = pinnedMessageForChannel
                    if (pinned != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    val targetIdx = messages.indexOfFirst { it.id == pinned.messageId }
                                    if (targetIdx != -1) {
                                        coroutineScope.launch {
                                            try { listState.animateScrollToItem(targetIdx) } catch (_: Exception) {}
                                        }
                                    }
                                },
                            color = colors.surfaceVariant.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(14.dp),
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Pinned",
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "PINNED MESSAGE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            color = colors.primary
                                        )
                                        if (pinned.authorName.isNotBlank()) {
                                            Text(
                                                text = " • ${pinned.authorName}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Text(
                                        text = cleanMessageContentForDisplay(pinned.content, "Pinned message"),
                                        fontSize = 12.5.sp,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            val cleanCh = activeChip.id.trim().lowercase().removePrefix("#")
                                            pinnedMessageForChannel = null
                                            sharedPrefs.edit()
                                                .remove("pinned_msg_ch_$cleanCh")
                                                .remove("pinned_content_ch_$cleanCh")
                                                .remove("pinned_author_ch_$cleanCh")
                                                .apply()
                                            coroutineScope.launch {
                                                ConnectoApiClient.unpinMessage(cleanCh)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Unpin",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ================= MESSAGES TIMELINE FEED =================
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val showChannelSkeleton = isLoadingMessages && messages.isEmpty()
                    androidx.compose.animation.Crossfade(
                        targetState = showChannelSkeleton,
                        animationSpec = tween(durationMillis = 160),
                        label = "channelMessagesCrossfade"
                    ) { isSkeleton ->
                        if (isSkeleton) {
                            ShimmerMessageFeed(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                isDark = colors.isDark
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
                    } else if (displayMessages.isEmpty() && channelSearchQuery.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(colors.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No results for \"$channelSearchQuery\"",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No messages match your search in #${activeChip.name}",
                                    fontSize = 12.5.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.primary.copy(alpha = 0.15f))
                                        .clickable { channelSearchQuery = "" }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Clear search",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }
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
                            // Top Welcome Banner inside the feed (visible when scrolled to top and not searching)
                            if (channelSearchQuery.isBlank()) {
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
                            }

                            itemsIndexed(displayMessages, key = { _, msg -> msg.id }) { idx, msg ->
                                val currentDate = formatChannelDate(msg.createdAt)
                                val prevDate = if (idx > 0) formatChannelDate(displayMessages[idx - 1].createdAt) else null
                                val showDateDivider = prevDate == null || currentDate != prevDate

                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
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

                                    val msgReactionsMap = channelReactions[msg.id] ?: emptyMap()

                                    var itemSwipeOffset by remember(msg.id) { mutableFloatStateOf(0f) }
                                    var hasTriggeredHaptic by remember(msg.id) { mutableStateOf(false) }
                                    val animatedItemSwipeOffset by animateFloatAsState(
                                        targetValue = itemSwipeOffset,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
                                        label = "msgSwipe"
                                    )
                                    val clipboardManager = LocalClipboardManager.current

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .pointerInput(msg.id) {
                                                detectHorizontalDragGestures(
                                                    onDragEnd = {
                                                        if (kotlin.math.abs(itemSwipeOffset) >= 40f) {
                                                            replyingTo = msg
                                                            try { messageInputFocusRequester.requestFocus() } catch (_: Exception) {}
                                                        }
                                                        itemSwipeOffset = 0f
                                                        hasTriggeredHaptic = false
                                                    },
                                                    onDragCancel = {
                                                        itemSwipeOffset = 0f
                                                        hasTriggeredHaptic = false
                                                    },
                                                    onHorizontalDrag = { change, dragAmount ->
                                                        if (isMe) {
                                                            itemSwipeOffset = (itemSwipeOffset + dragAmount * 0.5f).coerceIn(-60f, 60f)
                                                        } else {
                                                            if (dragAmount > 0 || itemSwipeOffset > 0f) {
                                                                itemSwipeOffset = (itemSwipeOffset + dragAmount * 0.5f).coerceIn(0f, 60f)
                                                            }
                                                        }
                                                        change.consume()
                                                        val dist = kotlin.math.abs(itemSwipeOffset)
                                                        if (dist >= 40f && !hasTriggeredHaptic) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            hasTriggeredHaptic = true
                                                        } else if (dist < 40f) {
                                                            hasTriggeredHaptic = false
                                                        }
                                                    }
                                                )
                                            }
                                    ) {
                                        val dragDist = kotlin.math.abs(animatedItemSwipeOffset)
                                        if (dragDist > 3f) {
                                            val isSwipeRight = animatedItemSwipeOffset > 0
                                            val progress = (dragDist / 40f).coerceIn(0f, 1f)
                                            val isActivated = dragDist >= 40f
                                            Box(
                                                modifier = Modifier
                                                    .align(if (isSwipeRight) Alignment.CenterStart else Alignment.CenterEnd)
                                                    .padding(if (isSwipeRight) PaddingValues(start = 8.dp) else PaddingValues(end = 8.dp))
                                                    .size(36.dp)
                                                    .graphicsLayer {
                                                        alpha = progress
                                                        scaleX = 0.4f + 0.6f * progress
                                                        scaleY = 0.4f + 0.6f * progress
                                                    }
                                                    .clip(CircleShape)
                                                    .background(if (isActivated) colors.primary else colors.surfaceVariant)
                                                    .border(1.dp, if (isActivated) colors.primary else colors.borderSubtle, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Reply,
                                                    contentDescription = "Reply",
                                                    tint = if (isActivated) Color.White else colors.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        val isRecentMsg = remember(msg.id) {
                                            try {
                                                val raw = msg.createdAt.substringBefore(".")
                                                val d = if (msg.createdAt.contains("T")) {
                                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(raw)
                                                } else {
                                                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(raw)
                                                }
                                                val diff = if (d != null) System.currentTimeMillis() - d.time else 999999L
                                                diff in 0..4000L
                                            } catch (_: Exception) {
                                                false
                                            }
                                        }
                                        val popInAnim = remember { Animatable(if (isRecentMsg) 0f else 1f) }
                                        LaunchedEffect(msg.id) {
                                            if (isRecentMsg && popInAnim.value < 1f) {
                                                popInAnim.animateTo(
                                                    targetValue = 1f,
                                                    animationSpec = tween(durationMillis = 180, easing = EaseOutCubic)
                                                )
                                            }
                                        }
                                        val msgBubbleScale = if (isRecentMsg) 0.94f + 0.06f * popInAnim.value else 1f
                                        val msgBubbleTranslateY = if (isRecentMsg) 14f * (1f - popInAnim.value) else 0f

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .offset { IntOffset(animatedItemSwipeOffset.roundToInt(), 0) }
                                                .graphicsLayer {
                                                    scaleX = msgBubbleScale
                                                    scaleY = msgBubbleScale
                                                    translationY = msgBubbleTranslateY.dp.toPx()
                                                    if (isRecentMsg) {
                                                        alpha = 0.4f + 0.6f * popInAnim.value
                                                    }
                                                }
                                        ) {
                                            ChannelMessageCard(
                                                message = msg,
                                                isMe = isMe,
                                                gradientColors = gradientColors,
                                                reactionsMap = msgReactionsMap,
                                                onToggleReaction = { emoji ->
                                                    val myUid = (ConnectoApiClient.currentUserId ?: currentUsername).trim().lowercase()
                                                    val myUname = currentUsername.trim().lowercase()
                                                    val curMap = (channelReactions[msg.id] ?: emptyMap()).toMutableMap()
                                                    val uList = (curMap[emoji] ?: emptyList()).toMutableList()
                                                    val hadReacted = uList.any {
                                                        val cu = it.trim().lowercase()
                                                        cu == myUname || (myUid.isNotBlank() && cu == myUid)
                                                    }
                                                    if (hadReacted) {
                                                        uList.removeAll {
                                                            val cu = it.trim().lowercase()
                                                            cu == myUname || (myUid.isNotBlank() && cu == myUid)
                                                        }
                                                    } else {
                                                        uList.add(if (myUid.isNotBlank()) myUid else myUname)
                                                    }
                                                    if (uList.isEmpty()) {
                                                        curMap.remove(emoji)
                                                    } else {
                                                        curMap[emoji] = uList
                                                    }
                                                    channelReactions[msg.id] = curMap
                                                    selectedMessageForAction = null

                                                    val cleanCh = cleanSelectedChannel
                                                    VoiceCallManager.sendReactionToggled(
                                                        messageId = msg.id,
                                                        emoji = emoji,
                                                        userId = if (myUid.isNotBlank()) myUid else myUname,
                                                        channelId = cleanCh
                                                    )
                                                    coroutineScope.launch {
                                                        try {
                                                            ConnectoApiClient.reactToMessage(
                                                                messageId = msg.id,
                                                                emoji = emoji,
                                                                channelId = cleanCh
                                                            )
                                                        } catch (_: Exception) {}
                                                    }
                                                },
                                                isSelected = selectedMessageForAction?.id == msg.id,
                                                isHighlighted = msg.id == highlightedMessageId,
                                                onReply = {
                                                    replyingTo = msg
                                                    selectedMessageForAction = null
                                                    try { messageInputFocusRequester.requestFocus() } catch (_: Exception) {}
                                                },
                                                onCopy = {
                                                    clipboardManager.setText(AnnotatedString(msg.content))
                                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                                    selectedMessageForAction = null
                                                },
                                                onUnsend = {
                                                    val msgId = msg.id
                                                    messages.removeAll { it.id == msgId }
                                                    selectedMessageForAction = null
                                                    val cleanCh = cleanSelectedChannel
                                                    coroutineScope.launch {
                                                        ConnectoApiClient.deleteMessage(msgId, cleanCh)
                                                    }
                                                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
                                                },
                                                onQuotedMessageClick = { quotedId ->
                                                    val targetIdx = displayMessages.indexOfFirst { it.id == quotedId }
                                                    if (targetIdx >= 0) {
                                                        coroutineScope.launch {
                                                            val offset = if (channelSearchQuery.isBlank()) 1 else 0
                                                            listState.animateScrollToItem(targetIdx + offset)
                                                        }
                                                        highlightedMessageId = quotedId
                                                        coroutineScope.launch {
                                                            delay(600)
                                                            if (highlightedMessageId == quotedId) {
                                                                highlightedMessageId = null
                                                            }
                                                        }
                                                    }
                                                },
                                                onOpenFullEmojiSheet = { targetMsg ->
                                                    showFullEmojiDialogForMessage = targetMsg
                                                },
                                                onMessageClick = {
                                                    if (selectedMessageForAction != null) {
                                                        selectedMessageForAction = if (selectedMessageForAction?.id == msg.id) null else msg
                                                    }
                                                },
                                                onMessageLongPress = { targetMsg ->
                                                    selectedMessageForAction = targetMsg
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (channelTypingUser != null) {
                                item(key = "channel_typing_indicator") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        ChannelTypingIndicator(
                                            typingUser = channelTypingUser ?: "",
                                            accentColor = colors.primary,
                                            bubbleBg = if (colors.isDark) Color(0xFF18181B) else Color(0xFFF4F4F5),
                                            borderColor = if (colors.isDark) Color(0xFF27272A) else Color(0xFFE4E4E7)
                                        )
                                    }
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
                            (totalCount > 0 && lastVisible < totalCount - 1) || listState.canScrollForward
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = canScrollDown && displayMessages.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(durationMillis = 160, easing = EaseOutCubic)) +
                                scaleIn(initialScale = 0f, animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 140, easing = EaseInCubic)) +
                               scaleOut(targetScale = 0f, animationSpec = tween(durationMillis = 140, easing = EaseInCubic)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 76.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .pressScaleEffect(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        coroutineScope.launch {
                                            if (displayMessages.isNotEmpty()) {
                                                try {
                                                    listState.animateScrollToItem((displayMessages.size - 1).coerceAtLeast(0))
                                                } catch (_: Exception) {
                                                    listState.scrollToItem((displayMessages.size - 1).coerceAtLeast(0))
                                                }
                                            }
                                        }
                                    },
                                    targetScale = 0.90f
                                )
                                .clip(CircleShape)
                                .background(if (colors.isDark) Color(0xFF181C28) else colors.surface)
                                .border(1.dp, if (colors.isDark) Color.White.copy(alpha = 0.15f) else colors.primary.copy(alpha = 0.4f), CircleShape)
                                .shadow(8.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Scroll to bottom",
                                tint = if (colors.isDark) Color.White else colors.primary,
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
                        targetValue = if (isInputFocused) (if (colors.isDark) Color(0x47FFFFFF) else Color(0xFF18181B))
                                      else if (colors.isDark) Color(0x14FFFFFF)
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
                                onClose = { showEmojiPicker = false },
                                onBackspace = {
                                    if (typedMessage.isNotEmpty()) {
                                        val lastCodePoint = typedMessage.codePointBefore(typedMessage.length)
                                        val charCount = Character.charCount(lastCodePoint)
                                        typedMessage = typedMessage.dropLast(charCount)
                                    }
                                }
                            )
                        }
                    }

                    // Active Editing Message Banner
                    AnimatedVisibility(
                        visible = editingMessage != null,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(180)) + expandVertically(),
                        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(tween(150)) + shrinkVertically()
                    ) {
                        editingMessage?.let { editTarget ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.primary.copy(alpha = 0.15f))
                                    .border(
                                        width = 1.dp,
                                        color = colors.primary.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editing",
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Editing message",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                    Text(
                                        text = editTarget.content,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            editingMessage = null
                                            typedMessage = ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel edit",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Active Reply Preview Banner (Compact 48px height)
                    AnimatedVisibility(
                        visible = replyingTo != null,
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(tween(180)) + expandVertically(),
                        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(tween(150)) + shrinkVertically()
                    ) {
                        replyingTo?.let { replyTarget ->
                            val targetFriend = matchedFriendTarget
                            val isPrivate = isPrivateDmReply && targetFriend != null
                            val targetU = targetFriend?.handle?.trim()?.lowercase()?.removePrefix("@") ?: ""

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(bottom = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isPrivate) (if (colors.isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF))
                                        else if (colors.isGlass) Color(0xD91E293B)
                                        else if (colors.isDark) Color(0xFF18181B)
                                        else colors.surfaceVariant.copy(alpha = 0.95f)
                                    )
                                    .border(
                                        width = if (isPrivate) 1.5.dp else 1.dp,
                                        color = if (isPrivate) Color(0xFF6366F1)
                                        else if (colors.isDark) Color(0x1FFFFFFF)
                                        else colors.primary.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isPrivate) Color(0xFF818CF8) else colors.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isPrivate) "🔒 Private Reply to @$targetU"
                                               else "Replying to ${replyTarget.authorName.ifBlank { replyTarget.authorId }}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPrivate) Color(0xFF818CF8) else if (colors.isDark) Color.White else colors.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isPrivate) "Will be sent directly to @$targetU's private chat"
                                               else replyTarget.content,
                                        fontSize = 13.sp,
                                        color = if (isPrivate) Color(0xFFA5B4FC) else colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .clickable { replyingTo = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel reply",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Main Aesthetic Unified Input Capsule
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = glowElevation,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = if (colors.isDark) Color.Black.copy(alpha = 0.5f) else Color(0x1F000000),
                                ambientColor = Color.Black.copy(alpha = 0.35f)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (colors.isGlass) Color(0xD9111827)
                                else (if (colors.isDark) Color(0xFF141416) else Color(0xFFFFFFFF))
                            )
                            .border(
                                width = if (isPrivateDmReply) 1.5.dp else if (isInputFocused) 1.5.dp else 1.dp,
                                brush = if (isPrivateDmReply) {
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF6366F1),
                                            Color(0xFF818CF8)
                                        )
                                    )
                                } else if (colors.isGlass) {
                                    Brush.linearGradient(
                                        listOf(
                                            if (isInputFocused) Color(0xEE38BDF8) else Color(0x8038BDF8),
                                            if (isInputFocused) Color(0x8038BDF8) else Color(0x20334A6E)
                                        )
                                    )
                                } else {
                                    SolidColor(focusBorderColor)
                                },
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
                                        if (showEmojiPicker) (if (colors.isDark) Color(0xFF27272A) else Color(0xFFE2E8F0))
                                        else Color.Transparent
                                    )
                                    .clickable(
                                        interactionSource = emojiInteractionSource,
                                        indication = null
                                    ) {
                                        if (!showEmojiPicker) {
                                            focusManager.clearFocus()
                                        }
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
                                        text = if (isPrivateDmReply && matchedFriendTarget != null) {
                                            "Message @${matchedFriendTarget?.handle?.trim()?.removePrefix("@")} privately 🔒..."
                                        } else {
                                            "Message #${activeChip.name}..."
                                        },
                                        color = if (isPrivateDmReply) Color(0xFF818CF8)
                                                else if (colors.isDark) Color(0xFF71717A) else Color(0xFF94A3B8),
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
                                        .focusRequester(messageInputFocusRequester)
                                        .onFocusChanged { isInputFocused = it.isFocused },
                                    textStyle = TextStyle(
                                        color = if (colors.isDark) Color.White else Color(0xFF0F172A),
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Default
                                    ),
                                    cursorBrush = SolidColor(if (colors.isDark) Color.White else Color.Black),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { handleSendMessage() })
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // 3. Circular Monochrome Send Button on Right (Tactile Spring Scale & Rotation)
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
                                        spotColor = Color.Black.copy(alpha = 0.35f)
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        if (hasText) (if (colors.isDark) Color.White else Color(0xFF18181B))
                                        else (if (colors.isDark) Color(0xFF1F1F23) else Color(0xFFF1F5F9))
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (hasText) (if (colors.isDark) Color.White else Color(0xFF18181B))
                                                else (if (colors.isDark) Color(0xFF2E2E33) else Color(0xFFE2E8F0)),
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
                                        color = if (colors.isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (isPrivateDmReply) Icons.Default.Lock else Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = if (isPrivateDmReply) "Send Private DM" else "Send",
                                        tint = if (isPrivateDmReply && hasText) (if (colors.isDark) Color.Black else Color.White)
                                               else if (hasText) (if (colors.isDark) Color.Black else Color.White)
                                               else (if (colors.isDark) Color(0xFF71717A) else Color(0xFF94A3B8)),
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

            // Full Emoji Sheet Dialog for Reaction Pill "+" Button
            val fullEmojiTarget = showFullEmojiDialogForMessage
            if (fullEmojiTarget != null) {
                val targetMsg = fullEmojiTarget
                AlertDialog(
                    onDismissRequest = { showFullEmojiDialogForMessage = null },
                    title = {
                        Text(
                            text = "React to message",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val extendedRows = listOf(
                                listOf("❤️", "🔥", "😂", "👍", "😮", "😢"),
                                listOf("🎉", "👏", "🚀", "✨", "💯", "🙏"),
                                listOf("🤩", "😍", "🥳", "🤔", "👀", "💡"),
                                listOf("🙌", "💪", "⚡", "🤝", "😎", "🫡")
                            )
                            extendedRows.forEach { rowEmojis ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowEmojis.forEach { em ->
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    val msgToReact = targetMsg
                                                    showFullEmojiDialogForMessage = null
                                                    val myUid = (ConnectoApiClient.currentUserId ?: currentUsername).trim().lowercase()
                                                    val myUname = currentUsername.trim().lowercase()
                                                    val curMap = (channelReactions[msgToReact.id] ?: emptyMap()).toMutableMap()
                                                    val uList = (curMap[em] ?: emptyList()).toMutableList()
                                                    val hadReacted = uList.any {
                                                        val cu = it.trim().lowercase()
                                                        cu == myUname || (myUid.isNotBlank() && cu == myUid)
                                                    }
                                                    if (hadReacted) {
                                                        uList.removeAll {
                                                            val cu = it.trim().lowercase()
                                                            cu == myUname || (myUid.isNotBlank() && cu == myUid)
                                                        }
                                                    } else {
                                                        uList.add(if (myUid.isNotBlank()) myUid else myUname)
                                                    }
                                                    if (uList.isEmpty()) {
                                                        curMap.remove(em)
                                                    } else {
                                                        curMap[em] = uList
                                                    }
                                                    channelReactions[msgToReact.id] = curMap
                                                    selectedMessageForAction = null

                                                    val cleanCh = cleanSelectedChannel
                                                    VoiceCallManager.sendReactionToggled(
                                                        messageId = msgToReact.id,
                                                        emoji = em,
                                                        userId = if (myUid.isNotBlank()) myUid else myUname,
                                                        channelId = cleanCh
                                                    )
                                                    coroutineScope.launch {
                                                        try {
                                                            ConnectoApiClient.reactToMessage(
                                                                messageId = msgToReact.id,
                                                                emoji = em,
                                                                channelId = cleanCh
                                                            )
                                                        } catch (_: Exception) {}
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = em, fontSize = 20.sp)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFullEmojiDialogForMessage = null }) {
                            Text("Close", color = MaterialTheme.colorScheme.primary)
                        }
                    }
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
    reactionsMap: Map<String, List<String>>,
    onToggleReaction: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isHighlighted: Boolean = false,
    onReply: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onUnsend: (() -> Unit)? = null,
    onQuotedMessageClick: ((String) -> Unit)? = null,
    onOpenFullEmojiSheet: ((MessageDto) -> Unit)? = null,
    onMessageClick: (() -> Unit)? = null,
    onMessageLongPress: (MessageDto) -> Unit = {}
) {
    val colors = LocalConnectoColors.current
    val haptic = LocalHapticFeedback.current
    val isLight = !colors.isDark

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

    val entryAnim = remember { Animatable(0f) }
    LaunchedEffect(message.id) {
        entryAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = 500f
            )
        )
    }

    // 600ms Subtle Background Pulse Animation for Scroll-to-Original
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.35f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "channelPulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                val progress = entryAnim.value
                translationY = (1f - progress) * 12.dp.toPx()
                scaleX = 0.96f + (0.04f * progress)
                scaleY = 0.96f + (0.04f * progress)
                alpha = progress.coerceIn(0f, 1f)
            },
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (isMe) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                        .pointerInput(message.id, isSelected) {
                            detectTapGestures(
                                onTap = {
                                    if (onMessageClick != null) {
                                        onMessageClick()
                                    }
                                },
                                onLongPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onMessageLongPress(message)
                                }
                            )
                        }
                )
            }

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
                // Header row (Author Name + Timestamp for other members)
                if (!isMe) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message.authorName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (colors.isDark) Color.White else colors.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = if (colors.isDark) Color(0xFFA1A1AA) else colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // ========== TOP FLOATING PILL (ABOVE BUBBLE) ==========
                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f), initialScale = 0.7f) + fadeIn(animationSpec = tween(140)),
                    exit = scaleOut(targetScale = 0.7f) + fadeOut(animationSpec = tween(120))
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .height(46.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp)),
                        color = if (isLight) Color(0xFFFFFFFF) else Color(0xFF181B24),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, if (isLight) Color(0x14000000) else Color(0x1FFFFFFF))
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
                                var visible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    delay(idx * 28L)
                                    visible = true
                                }
                                val scale by animateFloatAsState(
                                    targetValue = if (visible) 1f else 0.4f,
                                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 450f),
                                    label = "popScale"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onToggleReaction(emoji)
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
                                        onOpenFullEmojiSheet?.invoke(message)
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
                }

                val bubbleShape = if (isMe) {
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
                }

                val ownBubbleBg = if (colors.isDark) Color(0xFF3B4254) else Color(0xFF4F46E5)
                val ownBubbleText = Color(0xFFFFFFFF)
                val friendBubbleBg = if (colors.isDark) Color(0xFF18181B) else Color(0xFFFFFFFF)
                val friendBubbleText = if (colors.isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
                val friendBorder = if (colors.isDark) Color(0xFF27272A) else Color(0xFFE2E8F0)

                val bubbleModifier = if (colors.isLiquidGlass || colors.isGlass) {
                    Modifier.glassBubble(isMe = isMe, shape = bubbleShape, isLiquid = colors.isLiquidGlass)
                } else {
                    val bgBrush = if (isMe) {
                        Brush.linearGradient(
                            colors = listOf(
                                ownBubbleBg,
                                Color(
                                    red = (ownBubbleBg.red * 0.85f).coerceIn(0f, 1f),
                                    green = (ownBubbleBg.green * 0.85f).coerceIn(0f, 1f),
                                    blue = (ownBubbleBg.blue * 0.95f).coerceIn(0f, 1f),
                                    alpha = 1f
                                )
                            ),
                            start = Offset.Zero,
                            end = Offset(300f, 300f)
                        )
                    } else {
                        SolidColor(friendBubbleBg)
                    }

                    val borderBrush = if (isMe) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.45f),
                                ownBubbleBg.copy(alpha = 0.60f),
                                Color.Transparent
                            ),
                            start = Offset.Zero,
                            end = Offset(220f, 220f)
                        )
                    } else {
                        SolidColor(friendBorder)
                    }

                    Modifier
                        .clip(bubbleShape)
                        .background(bgBrush)
                        .border(
                            width = 0.8.dp,
                            brush = borderBrush,
                            shape = bubbleShape
                        )
                }

                val selectionBorder = if (isSelected) {
                    Modifier
                        .border(2.dp, colors.primary, bubbleShape)
                        .background(colors.primary.copy(alpha = 0.22f), bubbleShape)
                } else Modifier

                val pulseModifier = if (pulseAlpha > 0f) {
                    Modifier.background(colors.primary.copy(alpha = pulseAlpha), bubbleShape)
                } else Modifier

                Box(
                    modifier = bubbleModifier
                        .then(selectionBorder)
                        .then(pulseModifier)
                        .pointerInput(message.id, isSelected) {
                            detectTapGestures(
                                onTap = {
                                    if (onMessageClick != null) {
                                        onMessageClick()
                                    }
                                },
                                onLongPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onMessageLongPress(message)
                                }
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        // Quoted Reply Preview inside bubble
                        if (!message.replyToContent.isNullOrBlank()) {
                            val accentColor = colors.primary
                            val quoteBg = if (colors.isDark) Color(0x2E000000) else Color(0x0F0F172A)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(quoteBg)
                                    .clickable(enabled = !message.replyToId.isNullOrBlank()) {
                                        message.replyToId?.let { targetId ->
                                            onQuotedMessageClick?.invoke(targetId)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(accentColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    val authorName = message.replyToAuthor?.ifBlank { "Reply" } ?: "Reply"
                                    Text(
                                        text = authorName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = cleanMessageContentForDisplay(message.replyToContent, ""),
                                        fontSize = 12.sp,
                                        color = if (isMe) ownBubbleText.copy(alpha = 0.8f) else friendBubbleText.copy(alpha = 0.75f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

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
                                text = cleanMessageContentForDisplay(message.content, ""),
                                fontSize = 13.5.sp,
                                color = if (isMe) ownBubbleText else friendBubbleText,
                                lineHeight = 19.sp
                            )
                        }

                        // Outgoing message timestamp & delivery ticks pinned to bottom-right
                        if (isMe) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formattedTime,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                if (isPending) {
                                    ConnectoDeliverySendingRing()
                                } else {
                                    ConnectoDeliveryDeliveredDots()
                                }
                            }
                        }
                    }
                }

                // ========== REACTION BADGES ON BUBBLE (COMPACT PILLS) ==========
                if (reactionsMap.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .offset(y = (-4).dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentUid = (ConnectoApiClient.currentUserId ?: "").trim().lowercase()
                        val currentUname = (ConnectoApiClient.currentUsername ?: "").trim().removePrefix("@").lowercase()

                        reactionsMap.forEach { (emoji, userList) ->
                            val hasUserReacted = userList.any { u ->
                                val cu = u.trim().removePrefix("@").lowercase()
                                (currentUid.isNotBlank() && cu == currentUid) || (currentUname.isNotBlank() && cu == currentUname)
                            }
                            val count = userList.size

                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (hasUserReacted) colors.primary.copy(alpha = 0.20f)
                                        else if (isLight) Color(0xFFF1F5F9)
                                        else Color(0xFF161922)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (hasUserReacted) colors.primary.copy(alpha = 0.65f)
                                                else if (isLight) Color(0x14000000)
                                                else Color(0x1FFFFFFF),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleReaction(emoji)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = emoji, fontSize = 12.sp)
                                    if (count > 1) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = count.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasUserReacted) colors.primary
                                                    else if (isLight) Color(0xFF64748B)
                                                    else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ========== BOTTOM ACTION MENU (BELOW BUBBLE) ==========
                AnimatedVisibility(
                    visible = isSelected,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .shadow(8.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp)),
                        color = if (isLight) Color(0xFFFFFFFF) else Color(0xFF181B24),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isLight) Color(0x14000000) else Color(0x1FFFFFFF))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            ActionMenuItem(
                                icon = Icons.AutoMirrored.Filled.Reply,
                                label = "Reply",
                                color = if (isLight) Color(0xFF0F172A) else Color.White,
                                onClick = { onReply?.invoke() }
                            )
                            ActionMenuItem(
                                icon = Icons.Default.ContentCopy,
                                label = "Copy",
                                color = if (isLight) Color(0xFF0F172A) else Color.White,
                                onClick = { onCopy?.invoke() }
                            )
                            if (isMe) {
                                ActionMenuItem(
                                    icon = Icons.Default.Delete,
                                    label = "Unsend",
                                    color = Color(0xFFEF4444),
                                    onClick = { onUnsend?.invoke() }
                                )
                            }
                        }
                    }
                }
            }

            if (!isMe) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                        .pointerInput(message.id, isSelected) {
                            detectTapGestures(
                                onTap = {
                                    if (onMessageClick != null) {
                                        onMessageClick()
                                    }
                                },
                                onLongPress = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onMessageLongPress(message)
                                }
                            )
                        }
                )
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

@Composable
private fun ChannelTypingIndicator(
    typingUser: String,
    accentColor: Color,
    bubbleBg: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chTyping")

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chDot1"
    )
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chDot1Alpha"
    )

    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chDot2"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chDot2Alpha"
    )

    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chDot3"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, delayMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chDot3Alpha"
    )

    Column(
        modifier = modifier.padding(start = 12.dp, top = 4.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(bubbleBg)
                .border(1.dp, borderColor, RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = dot1Offset.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = dot1Alpha))
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot2Offset.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = dot2Alpha))
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot3Offset.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = dot3Alpha))
                )
            }
        }
        // Trailing thought bubble tail dots
        Box(
            modifier = Modifier
                .offset(x = 6.dp, y = (-2).dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(bubbleBg)
                .border(1.dp, borderColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = 1.dp)
                .size(4.5.dp)
                .clip(CircleShape)
                .background(bubbleBg)
                .border(1.dp, borderColor, CircleShape)
        )
    }
}

// ================= GUEST RANDOM / MOCK MESSAGES GENERATOR =================

private fun getGuestChannelMockMessages(channelId: String): List<MessageDto> {
    val clean = channelId.trim().lowercase().removePrefix("#")
    val baseTime = System.currentTimeMillis()
    fun timeAgo(minutesAgo: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US)
        return sdf.format(Date(baseTime - minutesAgo * 60 * 1000))
    }

    return when (clean) {
        "general" -> listOf(
            MessageDto(
                id = "guest_mock_gen_1",
                channelId = "general",
                authorId = "ConnectoBot",
                authorName = "Connecto Bot",
                authorAvatar = "robot",
                content = "👋 Welcome to Connecto! You're currently exploring in Guest Mode. Experience the fast UI, themes, and community channels freely!",
                createdAt = timeAgo(45),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_gen_2",
                channelId = "general",
                authorId = "ApexStriker",
                authorName = "Apex Striker",
                authorAvatar = "gamer",
                content = "Anyone looking for a 4th squad member for Bermuda Rush tonight? Let's get some Booyahs! 🔥",
                createdAt = timeAgo(28),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_gen_3",
                channelId = "general",
                authorId = "PixelQueen",
                authorName = "Pixel Queen",
                authorAvatar = "sparkles",
                content = "The new UI animations and dark theme are super responsive! Love the search bar in the messages tab ✨",
                createdAt = timeAgo(15),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_gen_4",
                channelId = "general",
                authorId = "GhostSniper",
                authorName = "Ghost Sniper",
                authorAvatar = "ninja",
                content = "Tip for guests: You can sign up with a full account to unlock WebRTC voice calls and squad channels! 🚀",
                createdAt = timeAgo(6),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_gen_5",
                channelId = "general",
                authorId = "NovaGamer",
                authorName = "Nova Gamer",
                authorAvatar = "star",
                content = "GGs to everyone in the practice matches earlier! Let's run it back tomorrow 🎮",
                createdAt = timeAgo(2),
                type = "text"
            )
        )
        "announcements" -> listOf(
            MessageDto(
                id = "guest_mock_ann_1",
                channelId = "announcements",
                authorId = "ConnectoTeam",
                authorName = "Connecto Team",
                authorAvatar = "shield",
                content = "🚀 Connecto v5.2.9 is officially rolled out! Featuring ultra-fast channel rendering, guest privacy isolation, and enhanced UI animations.",
                createdAt = timeAgo(120),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_ann_2",
                channelId = "announcements",
                authorId = "SystemBot",
                authorName = "System Sentinel",
                authorAvatar = "robot",
                content = "⚡ Global edge servers reporting 100% operational status with <15ms average latency.",
                createdAt = timeAgo(60),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_ann_3",
                channelId = "announcements",
                authorId = "SecurityLead",
                authorName = "Security Team",
                authorAvatar = "lock",
                content = "🔒 End-to-end Keystore encryption & biometric Face/Fingerprint auth are active for all registered accounts.",
                createdAt = timeAgo(10),
                type = "text"
            )
        )
        "gaming" -> listOf(
            MessageDto(
                id = "guest_mock_gam_1",
                channelId = "gaming",
                authorId = "Valkyrie",
                authorName = "Valkyrie",
                authorAvatar = "fire",
                content = "What sensitivity settings is everyone running on Free Fire MAX this season? Testing out a high-DPI claw setup.",
                createdAt = timeAgo(50),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_gam_2",
                channelId = "gaming",
                authorId = "BulletMaster",
                authorName = "Bullet Master",
                authorAvatar = "target",
                content = "General: 96, Red Dot: 85, 2x: 80. Keeps the recoil drag headshots super consistent! 🎯",
                createdAt = timeAgo(32),
                type = "text",
                replyToId = "guest_mock_gam_1",
                replyToContent = "What sensitivity settings is everyone running on Free Fire MAX this season?",
                replyToAuthor = "Valkyrie"
            ),
            MessageDto(
                id = "guest_mock_gam_3",
                channelId = "gaming",
                authorId = "NeonClutch",
                authorName = "Neon Clutch",
                authorAvatar = "controller",
                content = "Clutched a 1v3 in Kalahari ruins! Check out the clips channel for the replay 🔥",
                createdAt = timeAgo(12),
                type = "text"
            )
        )
        "tournaments" -> listOf(
            MessageDto(
                id = "guest_mock_tourn_1",
                channelId = "tournaments",
                authorId = "ArenaMaster",
                authorName = "Arena Master",
                authorAvatar = "trophy",
                content = "🏆 Connecto Weekly Esports Showdown starts this Saturday! 32 squads, 6 rounds, 50,000 Diamonds prize pool.",
                createdAt = timeAgo(90),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_tourn_2",
                channelId = "tournaments",
                authorId = "CaptainBlitz",
                authorName = "Captain Blitz",
                authorAvatar = "lightning",
                content = "Squad 'Phantom Elite' is checked in and ready. Good luck to all teams in the qualifiers! ⚡",
                createdAt = timeAgo(40),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_tourn_3",
                channelId = "tournaments",
                authorId = "RefereeBot",
                authorName = "FairPlay Referee",
                authorAvatar = "robot",
                content = "📢 Reminder: Emulators & custom script overlays are prohibited. Clean matches only!",
                createdAt = timeAgo(15),
                type = "text"
            )
        )
        "clips" -> listOf(
            MessageDto(
                id = "guest_mock_clip_1",
                channelId = "clips",
                authorId = "ShadowClutch",
                authorName = "Shadow Clutch",
                authorAvatar = "skull",
                content = "🎯 Insane 200m AWM no-scope through smoke on Factory roof! Highlight clip of the week.",
                createdAt = timeAgo(75),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_clip_2",
                channelId = "clips",
                authorId = "GlowRider",
                authorName = "Glow Rider",
                authorAvatar = "car",
                content = "Full squad vehicle wipe with landmine setup at Bimasakti Strip 😂 Watch your roads!",
                createdAt = timeAgo(30),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_clip_3",
                channelId = "clips",
                authorId = "ClipReviewer",
                authorName = "Clip Reviewer",
                authorAvatar = "star",
                content = "Drop your clips here with timestamps to be featured in the weekly community montage! 🎬",
                createdAt = timeAgo(8),
                type = "text"
            )
        )
        "voice-lounge" -> listOf(
            MessageDto(
                id = "guest_mock_vl_1",
                channelId = "voice-lounge",
                authorId = "AudioHost",
                authorName = "Audio Host",
                authorAvatar = "headphones",
                content = "🎙️ Welcome to Voice Lounge! Low-latency WebRTC spatial rooms are open for all squads.",
                createdAt = timeAgo(60),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_vl_2",
                channelId = "voice-lounge",
                authorId = "ChillVibes",
                authorName = "Chill Vibes",
                authorAvatar = "music",
                content = "Hanging out in Lounge 1 listening to lo-fi beats between tournament scrims 🎧",
                createdAt = timeAgo(25),
                type = "text"
            ),
            MessageDto(
                id = "guest_mock_vl_3",
                channelId = "voice-lounge",
                authorId = "SoundTech",
                authorName = "Sound Tech",
                authorAvatar = "robot",
                content = "Noise suppression and mic gain calibration tests complete. Audio quality 10/10! 🔊",
                createdAt = timeAgo(5),
                type = "text"
            )
        )
        else -> listOf(
            MessageDto(
                id = "guest_mock_def_1",
                channelId = clean,
                authorId = "ConnectoBot",
                authorName = "Connecto Bot",
                authorAvatar = "robot",
                content = "Welcome to #$clean! You are previewing sample messages in Guest Mode.",
                createdAt = timeAgo(10),
                type = "text"
            )
        )
    }
}

private fun getGuestPinnedMessage(channelId: String): PinnedMessageDto {
    val clean = channelId.trim().lowercase().removePrefix("#")
    return when (clean) {
        "general" -> PinnedMessageDto(
            messageId = "pin_guest_gen",
            channelId = "general",
            authorName = "Connecto Team",
            content = "📌 Welcome to Connecto! Keep discussions respectful & fun. Sign up for a full account to unlock voice calls & real-time chat.",
            pinnedBy = "admin",
            pinnedAt = "Recent"
        )
        "announcements" -> PinnedMessageDto(
            messageId = "pin_guest_ann",
            channelId = "announcements",
            authorName = "Connecto Team",
            content = "📌 Connecto Platform v5.2.9: 100% cloud uptime, instant edge updates, and verified guest privacy.",
            pinnedBy = "admin",
            pinnedAt = "Recent"
        )
        "tournaments" -> PinnedMessageDto(
            messageId = "pin_guest_tourn",
            channelId = "tournaments",
            authorName = "Arena Master",
            content = "📌 Official Tournament Rules: Fair play, registered squads, no third-party modifiers.",
            pinnedBy = "admin",
            pinnedAt = "Recent"
        )
        else -> PinnedMessageDto(
            messageId = "pin_guest_def",
            channelId = clean,
            authorName = "Connecto Guide",
            content = "📌 Guest Preview Mode: Sample channel feed for #$clean. Sign in to chat live with players.",
            pinnedBy = "admin",
            pinnedAt = "Recent"
        )
    }
}

@Composable
private fun VoiceLoungeWaveBars(
    color: Color,
    modifier: Modifier = Modifier,
    barWidth: Dp = 2.dp,
    spacing: Dp = 1.5.dp,
    scale: Float = 1.0f
) {
    val waveTransition = rememberInfiniteTransition(label = "voiceLoungeWave")
    val waveBar1 by waveTransition.animateFloat(
        initialValue = 4f * scale,
        targetValue = 13f * scale,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wb1"
    )
    val waveBar2 by waveTransition.animateFloat(
        initialValue = 12f * scale,
        targetValue = 5f * scale,
        animationSpec = infiniteRepeatable(tween(560, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wb2"
    )
    val waveBar3 by waveTransition.animateFloat(
        initialValue = 6f * scale,
        targetValue = 15f * scale,
        animationSpec = infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wb3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier
    ) {
        Box(modifier = Modifier.width(barWidth).height(waveBar1.dp).clip(CircleShape).background(color))
        Box(modifier = Modifier.width(barWidth).height(waveBar2.dp).clip(CircleShape).background(color))
        Box(modifier = Modifier.width(barWidth).height(waveBar3.dp).clip(CircleShape).background(color))
    }
}
