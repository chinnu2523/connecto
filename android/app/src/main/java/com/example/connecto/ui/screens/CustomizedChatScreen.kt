package com.example.connecto.ui.screens

import android.content.Context
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.connecto.data.ConnectoDatabaseHelper
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.collectAsState
import com.example.connecto.ui.components.AddFriendDialog
import com.example.connecto.ui.components.ConnectoEmojiPicker
import com.example.connecto.ui.components.ConnectoTopHeader
import com.example.connecto.ui.components.WebRtcCallOverlay
import com.example.connecto.voice.CallState
import com.example.connecto.voice.VoiceCallManager
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.ShimmerConversationItem
import com.example.connecto.ui.theme.ShimmerMessageFeed
import com.example.connecto.ui.theme.StaggeredReveal
import com.example.connecto.ui.theme.breathingPulse
import com.example.connecto.ui.theme.pressScaleEffect
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.connecto.network.ConnectoApiClient
import java.util.UUID
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.designsystem.ConnectoAvatar
import com.example.connecto.ui.designsystem.ConnectoPresenceStatus
import com.example.connecto.ui.theme.isAppInLightTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun formatIsoTime(isoString: String): String {
    return try {
        val trimmed = isoString.trim()
        if (trimmed.isEmpty() || trimmed.equals("just now", ignoreCase = true)) {
            return "Just now"
        }
        if (trimmed.equals("active", ignoreCase = true)) {
            return "Active"
        }
        // Check if already in friendly format like "10:14 AM" or "01:02 PM"
        if (trimmed.matches(Regex("""\d{1,2}:\d{2}\s*(?:AM|PM|am|pm)?"""))) {
            return trimmed
        }
        // Extract time part from ISO or SQL timestamp (handles 'T' or ' ' delimiter)
        val timePart = when {
            trimmed.contains("T") -> trimmed.substringAfter("T").substringBefore(".").substringBefore("Z")
            trimmed.contains(" ") -> trimmed.substringAfter(" ").substringBefore(".")
            else -> trimmed
        }
        val parts = timePart.split(":")
        if (parts.size >= 2) {
            var hour = parts[0].trim().toIntOrNull() ?: 0
            val min = parts[1].trim()
            val ampm = if (hour >= 12) "PM" else "AM"
            if (hour > 12) hour -= 12
            if (hour == 0) hour = 12
            "$hour:$min $ampm"
        } else {
            "Just now"
        }
    } catch (e: Exception) {
        "Just now"
    }
}

data class FriendItem(
    val id: String,
    val name: String,
    val handle: String,
    val initial: String,
    val status: String,
    val bio: String,
    val lastMessage: String,
    val timeAgo: String = "10:14 AM",
    var unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val avatarUrl: String? = null
)

data class CustomMessageItem(
    val id: String,
    val senderName: String,
    val initial: String,
    val content: String,
    val time: String,
    val isMe: Boolean,
    var isDelivered: Boolean = true,
    var isRead: Boolean = false,
    var reaction: String? = null,
    var showReactionPill: Boolean = false,
    val avatarUrl: String? = null
)

@Composable
fun CustomizedChatScreen(
    currentUsername: String,
    onOpenProfile: () -> Unit,
    onUnreadStatusChanged: (Boolean) -> Unit = {},
    onDirectChatStateChanged: (Boolean) -> Unit = {},
    onOpenNotifications: (() -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    targetChatUserOrChannel: String? = null,
    onTargetChatHandled: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFriend by remember { mutableStateOf<FriendItem?>(null) }
    var showFriendProfileDialog by remember { mutableStateOf(false) }
    var showUnfriendConfirmDialog by remember { mutableStateOf(false) }
    var isUnfriending by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var pendingRequestsCount by remember { mutableIntStateOf(0) }
    var friendSearchQuery by remember { mutableStateOf("") }
    var typedMessage by remember { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }
    var isFriendTyping by remember { mutableStateOf(false) }
    var isLoadingMessages by remember { mutableStateOf(false) }
    val messageDrafts = remember { mutableStateMapOf<String, String>() }

    // Active Call State
    val callState by VoiceCallManager.callState.collectAsState()
    val activeCallTitle by VoiceCallManager.activeCallTitle.collectAsState()
    val activeCallPartner by VoiceCallManager.activeCallPartner.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Send Button Animation State
    var isSendPressed by remember { mutableStateOf(false) }
    val sendButtonScale by animateFloatAsState(
        targetValue = if (isSendPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "sendScale"
    )

    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()
    val isLight = isAppInLightTheme()
    val userInitial = if (currentUsername.isNotBlank()) currentUsername.first().toString().uppercase() else "C"

    // Theme Aware Bubble Colors
    val ownBubbleBg = if (isLight) Color(0xFF0052FF) else Color(0xFF3B82F6)
    val ownBubbleText = Color.White
    val friendBubbleBg = if (isLight) Color(0xFFF1F5F9) else Color(0xFF101014)
    val friendBubbleBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E1E24)
    val friendBubbleText = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    val context = LocalContext.current
    val readPrefs = remember { context.getSharedPreferences("connecto_chat_read_prefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { ConnectoDatabaseHelper.getInstance(context) }

    // Friends List State - start empty, populated by IO LaunchedEffect below for 0ms Main-thread blocking
    val friendsList = remember { mutableStateListOf<FriendItem>() }
    var isFriendsLoadedOnce by remember { mutableStateOf(false) }

    // Auto-select chat when opened from push notification intent
    LaunchedEffect(targetChatUserOrChannel, friendsList.size) {
        val target = targetChatUserOrChannel?.trim()
        if (!target.isNullOrBlank()) {
            val clean = target.lowercase().removePrefix("@").removePrefix("dm-").removePrefix("dm_")
            val matchingFriend = friendsList.firstOrNull {
                it.id.equals(clean, ignoreCase = true) ||
                it.handle.removePrefix("@").equals(clean, ignoreCase = true) ||
                it.name.equals(clean, ignoreCase = true) ||
                (target.startsWith("dm-") && target.contains(it.handle.removePrefix("@").lowercase()))
            }
            if (matchingFriend != null) {
                selectedFriend = matchingFriend
            } else {
                val displayName = clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                selectedFriend = FriendItem(
                    id = clean,
                    name = displayName,
                    handle = "@$clean",
                    initial = clean.take(1).uppercase(),
                    status = "Online",
                    bio = "Connecto Member",
                    lastMessage = "",
                    isOnline = true,
                    avatarUrl = null
                )
            }
            onTargetChatHandled?.invoke()
        }
    }

    // Load cached friends from SQLite on IO thread on first composition
    LaunchedEffect(Unit) {
        val cached = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dbHelper.getCachedFriends()
        }
        if (cached.isNotEmpty() && friendsList.isEmpty()) {
            friendsList.addAll(cached)
            isFriendsLoadedOnce = true
        }
    }

    // Safe Audio Permission Launcher for 1:1 and Group Voice Calls
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            VoiceCallManager.restartAudioHardwareIfConnected()
            val target = selectedFriend
            if (target != null) {
                val friendUser = target.handle.removePrefix("@").ifBlank { target.name }
                val friendName = target.name.ifBlank { friendUser }
                VoiceCallManager.start1on1Call(friendUser, friendName)
            }
        } else {
            Toast.makeText(context, "Microphone permission is required for voice calls", Toast.LENGTH_SHORT).show()
        }
    }

    fun initiateVoiceCallWithFriend(friend: FriendItem) {
        val friendUser = friend.handle.removePrefix("@").ifBlank { friend.name }
        val friendName = friend.name.ifBlank { friendUser }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            VoiceCallManager.start1on1Call(friendUser, friendName)
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Function to sync accepted friends and pending friend requests from backend
    suspend fun syncAcceptedFriends() {
        try {
            val effUsername = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
            if (effUsername.isNotBlank()) {
                ConnectoApiClient.currentUsername = effUsername
            }
            val reqRes = ConnectoApiClient.getReceivedFriendRequests(username = effUsername)
            if (reqRes.isSuccess) {
                pendingRequestsCount = reqRes.getOrThrow().size
            }

            val friendsRes = ConnectoApiClient.getFriends(username = effUsername)
            if (friendsRes.isSuccess) {
                val serverFriends = friendsRes.getOrThrow()
                val mapped = serverFriends
                    .distinctBy { it.friendUsername.trim().lowercase().removePrefix("@") }
                    .map { f ->
                    val cleanUsername = f.friendUsername.trim().removePrefix("@")
                    val displayName = f.friendDisplayName.ifBlank { cleanUsername }
                    val init = if (displayName.isNotBlank()) displayName.first().toString().uppercase()
                               else if (cleanUsername.isNotBlank()) cleanUsername.first().toString().uppercase()
                               else "G"
                    val unread = readPrefs.getInt("unread_${f.friendId}", 0)
                    val lastMsg = readPrefs.getString("last_msg_${f.friendId}", "Direct chat active • Tap to message") ?: "Direct chat active • Tap to message"
                    val time = formatIsoTime(readPrefs.getString("last_time_${f.friendId}", "Active") ?: "Active")
                    val isFriendOnline = f.isOnline && !f.isStealth
                    FriendItem(
                        id = f.friendId.ifBlank { f.id },
                        name = displayName,
                        handle = "@$cleanUsername",
                        initial = init,
                        status = if (isFriendOnline) "Accepted Friend • Online" else "Accepted Friend • Offline",
                        bio = f.bio?.ifBlank { "Connecto Member • Friend" } ?: "Connecto Member • Friend",
                        lastMessage = lastMsg,
                        timeAgo = time,
                        unreadCount = unread,
                        isOnline = isFriendOnline,
                        avatarUrl = f.friendAvatarUrl
                    )
                }

                // Cache to persistent local SQLite database for zero-latency launches
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    dbHelper.saveFriends(mapped)
                }
                // Diff-update: never clear the entire list — avoids empty-list flash in LazyColumn
                val newIds = mapped.map { it.id }.toSet()
                friendsList.removeAll { it.id !in newIds }
                for (item in mapped) {
                    val idx = friendsList.indexOfFirst { it.id == item.id }
                    if (idx == -1) friendsList.add(item) else friendsList[idx] = item
                }
                isFriendsLoadedOnce = true

                // Sync real unread counts from server (authoritative — excludes own sent messages)
                try {
                    val unreadRes = ConnectoApiClient.getDmUnreadCounts()
                    if (unreadRes.isSuccess) {
                        val serverUnread = unreadRes.getOrThrow() // Map<String, Int> partner→count
                        for (i in friendsList.indices) {
                            val friendHandle = friendsList[i].handle.removePrefix("@").trim().lowercase()
                        val serverCount = serverUnread[friendHandle] ?: serverUnread.entries
                            .firstOrNull { it.key.trim().removePrefix("@").equals(friendHandle, ignoreCase = true) }?.value ?: 0
                        if (serverCount != friendsList[i].unreadCount) {
                            friendsList[i] = friendsList[i].copy(unreadCount = serverCount)
                            readPrefs.edit().putInt("unread_${friendsList[i].id}", serverCount).apply()
                        }
                        }
                        val hasUnread = friendsList.any { it.unreadCount > 0 }
                        onUnreadStatusChanged(hasUnread)
                    }
                } catch (e: Exception) {
                    // Non-fatal — local SharedPrefs count is fallback
                }
            } else {
                isFriendsLoadedOnce = true
            }
        } catch (e: Exception) {
            isFriendsLoadedOnce = true
            // Ignore transient network errors
        }
    }

    val screenCtx = LocalContext.current

    // Real-time friend requests & accepted friends sync listener
    DisposableEffect(screenCtx) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                coroutineScope.launch {
                    syncAcceptedFriends()
                }
            }
        }
        val filter = IntentFilter("com.connecto.app.FRIEND_REQUEST_UPDATED")
        try {
            ContextCompat.registerReceiver(
                screenCtx,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {}
        onDispose {
            try {
                screenCtx.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    // Dynamic friend requests & accepted friends sync loop from backend
    LaunchedEffect(Unit) {
        while (true) {
            syncAcceptedFriends()
            delay(3500)
        }
    }

    val filteredFriends by remember {
        derivedStateOf {
            if (friendSearchQuery.isBlank()) friendsList.toList()
            else friendsList.filter {
                it.name.contains(friendSearchQuery, ignoreCase = true) || it.handle.contains(friendSearchQuery, ignoreCase = true)
            }
        }
    }

    // Direct Messages Feed
    var activeDmChannelId by remember { mutableStateOf<String?>(null) }
    var friendMessages by remember {
        mutableStateOf<List<CustomMessageItem>>(emptyList())
    }

    // Handle System Back button when in 1:1 direct chat
    BackHandler(enabled = selectedFriend != null) {
        selectedFriend?.let { friend ->
            if (typedMessage.isNotBlank()) {
                messageDrafts[friend.id] = typedMessage
            } else {
                messageDrafts.remove(friend.id)
            }
        }
        typedMessage = ""
        selectedFriend = null
        activeDmChannelId = null
        isLoadingMessages = false
        onDirectChatStateChanged(false)
    }

    // Real-time WebSocket User Presence Listener
    LaunchedEffect(Unit) {
        VoiceCallManager.userPresenceUpdates.collect { (user, isOnline) ->
            val cleanUser = user.lowercase().removePrefix("@")
            val currentSelected = selectedFriend
            if (currentSelected != null) {
                val currentHandle = currentSelected.handle.lowercase().removePrefix("@")
                val currentId = currentSelected.id.lowercase()
                val currentName = currentSelected.name.lowercase()
                if (currentHandle == cleanUser || currentId == cleanUser || currentName == cleanUser) {
                    selectedFriend = currentSelected.copy(
                        isOnline = isOnline,
                        status = if (isOnline) "Accepted Friend • Online" else "Accepted Friend • Offline"
                    )
                }
            }
            val idx = friendsList.indexOfFirst {
                it.handle.lowercase().removePrefix("@") == cleanUser ||
                it.id.lowercase() == cleanUser ||
                it.name.lowercase() == cleanUser
            }
            if (idx != -1) {
                friendsList[idx] = friendsList[idx].copy(
                    isOnline = isOnline,
                    status = if (isOnline) "Accepted Friend • Online" else "Accepted Friend • Offline"
                )
            }
        }
    }

    // Real-time WebSocket Incoming DM Bus Listener
    LaunchedEffect(Unit) {
        VoiceCallManager.incomingChannelMessages.collect { dto ->
            val myUsername = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@").lowercase()
            val myDisplayName = (ConnectoApiClient.currentUserDisplayName ?: "").trim().lowercase()
            val myUserId = ConnectoApiClient.currentUserId?.trim()

            val authorClean = dto.authorId.trim().removePrefix("@").lowercase()
            val authorNameClean = dto.authorName.trim().removePrefix("@").lowercase()

            val isMe = (
                authorClean == myUsername ||
                authorNameClean == myUsername ||
                (myDisplayName.isNotBlank() && (authorNameClean == myDisplayName || authorClean == myDisplayName)) ||
                (myUserId != null && (dto.authorId == myUserId || dto.authorName == myUserId))
            )

            val currentFriend = selectedFriend
            val currentDmId = activeDmChannelId
            val currentCanonical = if (currentFriend != null) {
                val clean = currentFriend.handle.removePrefix("@").lowercase()
                val me = myUsername
                "dm-${minOf(me, clean)}-${maxOf(me, clean)}"
            } else null

            val isForCurrentChat = currentFriend != null && (
                (currentDmId != null && dto.channelId.equals(currentDmId, ignoreCase = true)) ||
                (currentCanonical != null && dto.channelId.equals(currentCanonical, ignoreCase = true)) ||
                authorClean == currentFriend.handle.removePrefix("@").trim().lowercase() ||
                (dto.channelId != null && dto.channelId.contains(currentFriend.handle.removePrefix("@"), ignoreCase = true)) ||
                (isMe && (dto.channelId.equals(currentDmId, ignoreCase = true) || dto.channelId.equals(currentCanonical, ignoreCase = true) || (dto.channelId != null && dto.channelId.contains(currentFriend.handle.removePrefix("@"), ignoreCase = true))))
            )

            if (isForCurrentChat) {
                val init = if (dto.authorName.isNotBlank()) dto.authorName.first().toString().uppercase() else "U"
                val msgAvatar = if (isMe) {
                    ConnectoApiClient.currentUserAvatarUrl
                } else {
                    dto.authorAvatar ?: currentFriend?.avatarUrl
                }
                val newItem = CustomMessageItem(
                    id = dto.id,
                    senderName = dto.authorName,
                    initial = init,
                    content = dto.content,
                    time = formatIsoTime(dto.createdAt),
                    isMe = isMe,
                    isDelivered = true,
                    isRead = true,
                    avatarUrl = msgAvatar
                )
                // Reconcile and deduplicate:
                val existingIdx = friendMessages.indexOfFirst {
                    it.id == newItem.id || (
                        isMe && it.isMe &&
                        (it.id.startsWith("opt-") || it.id.length > 30 || it.time == "Just now") &&
                        it.content.trim() == newItem.content.trim()
                    )
                }

                if (existingIdx != -1) {
                    val updated = friendMessages.toMutableList()
                    val existingItem = updated[existingIdx]
                    updated[existingIdx] = newItem.copy(reaction = existingItem.reaction ?: newItem.reaction)
                    friendMessages = updated
                } else if (friendMessages.none { it.id == newItem.id }) {
                    friendMessages = friendMessages + newItem
                }

                // Save to local SQLite cache instantly
                if (currentCanonical != null) {
                    val incomingList = listOf(dto)
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            dbHelper.saveMessages(currentCanonical, incomingList)
                            if (currentDmId != null && currentDmId != currentCanonical) {
                                dbHelper.saveMessages(currentDmId, incomingList)
                            }
                        } catch (_: Exception) {}
                    }
                }

                // Update friends list entry — current chat always has 0 unread
                val fIdx = friendsList.indexOfFirst {
                    it.id == currentFriend!!.id || it.handle.removePrefix("@").equals(currentFriend!!.handle.removePrefix("@"), ignoreCase = true)
                }
                if (fIdx != -1) {
                    friendsList[fIdx] = friendsList[fIdx].copy(
                        lastMessage = dto.content,
                        timeAgo = formatIsoTime(dto.createdAt),
                        unreadCount = 0
                    )
                }
            } else if (!isMe) {
                // Incoming message for another friend — ONLY increment if NOT sent by myself
                val fIdx = friendsList.indexOfFirst {
                    val h = it.handle.removePrefix("@").trim().lowercase()
                    h == authorClean || it.name.trim().lowercase() == authorNameClean || h == authorNameClean
                }
                if (fIdx != -1) {
                    val targetFriend = friendsList[fIdx]
                    val newUnread = targetFriend.unreadCount + 1
                    friendsList[fIdx] = targetFriend.copy(
                        lastMessage = dto.content,
                        timeAgo = formatIsoTime(dto.createdAt),
                        unreadCount = newUnread
                    )
                    readPrefs.edit()
                        .putString("last_msg_${targetFriend.id}", dto.content)
                        .putString("last_time_${targetFriend.id}", formatIsoTime(dto.createdAt))
                        .putInt("unread_${targetFriend.id}", newUnread)
                        .apply()
                    onUnreadStatusChanged(true)
                }
            }
        }
    }

    // DM Channel Setup and Live Polling Loop
    LaunchedEffect(selectedFriend) {
        val friend = selectedFriend
        onDirectChatStateChanged(friend != null)

        if (friend != null) {
            typedMessage = messageDrafts[friend.id] ?: ""
            activeDmChannelId = null

            val friendId = friend.id
            val idx = friendsList.indexOfFirst { it.id == friendId }
            if (idx != -1) {
                friendsList[idx] = friendsList[idx].copy(unreadCount = 0)
            }
            readPrefs.edit()
                .putInt("unread_$friendId", 0)
                .putBoolean("read_$friendId", true)
                .apply()
            val hasRemainingUnread = friendsList.any { it.unreadCount > 0 }
            onUnreadStatusChanged(hasRemainingUnread)

            val cleanTarget = friend.handle.removePrefix("@")
            val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
            val canonicalName = "dm-${minOf(myUser, cleanTarget.lowercase())}-${maxOf(myUser, cleanTarget.lowercase())}"

            // 1. Instant SQLite Cache Load (0ms UI latency across all channel aliases)
            val cachedMessages = withContext(Dispatchers.IO) {
                dbHelper.getDirectMessages(
                    myUsername = myUser,
                    friendUsername = cleanTarget,
                    channelId = activeDmChannelId ?: ConnectoApiClient.channelCache[canonicalName.lowercase()],
                    limit = 50
                )
            }

            if (cachedMessages.isNotEmpty()) {
                val myUsername = ConnectoApiClient.currentUsername ?: currentUsername
                val myDisplayName = ConnectoApiClient.currentUserDisplayName ?: ""
                val myUserId = ConnectoApiClient.currentUserId
                friendMessages = cachedMessages.map { dto ->
                    val isMe = (
                        dto.authorId.equals(myUsername, ignoreCase = true) ||
                        dto.authorName.equals(myUsername, ignoreCase = true) ||
                        (myDisplayName.isNotBlank() && dto.authorName.equals(myDisplayName, ignoreCase = true)) ||
                        (myUserId != null && (dto.authorId == myUserId || dto.authorName == myUserId)) ||
                        dto.authorId.equals(currentUsername, ignoreCase = true) ||
                        dto.authorName.equals(currentUsername, ignoreCase = true)
                    )
                    val init = if (dto.authorName.isNotBlank()) dto.authorName.first().toString().uppercase() else "U"
                    val msgAvatar = if (isMe) {
                        ConnectoApiClient.currentUserAvatarUrl
                    } else {
                        dto.authorAvatar ?: friend.avatarUrl
                    }
                    CustomMessageItem(
                        id = dto.id,
                        senderName = dto.authorName,
                        initial = init,
                        content = dto.content,
                        time = formatIsoTime(dto.createdAt),
                        isMe = isMe,
                        isDelivered = true,
                        isRead = true,
                        avatarUrl = msgAvatar
                    )
                }
                isLoadingMessages = false
            } else {
                friendMessages = emptyList()
                isLoadingMessages = true
            }

            // Immediately subscribe to canonical DM channel over WebSocket
            VoiceCallManager.subscribeChannel(canonicalName)

            // Resolve DM channel on backend and subscribe in background
            coroutineScope.launch {
                try {
                    val startRes = ConnectoApiClient.startDM(cleanTarget)
                    if (startRes.isSuccess) {
                        val ch = startRes.getOrThrow()
                        activeDmChannelId = ch.id
                        ConnectoApiClient.channelCache[canonicalName.lowercase()] = ch.id
                        ConnectoApiClient.channelCache[ch.id.lowercase()] = canonicalName.lowercase()
                        VoiceCallManager.subscribeChannel(ch.id)
                        VoiceCallManager.subscribeChannel(ch.name)
                    }
                } catch (_: Exception) {}
            }

            // Mark this DM as read on backend asynchronously
            coroutineScope.launch {
                try {
                    ConnectoApiClient.markDmRead(cleanTarget)
                } catch (_: Exception) {}
            }

            // Re-fetch fresh profile data on view — avoids indefinite caching of avatar or display name
            coroutineScope.launch {
                try {
                    val profRes = ConnectoApiClient.getProfile(cleanTarget)
                    if (profRes.isSuccess) {
                        val prof = profRes.getOrThrow()
                        val freshAvatar = prof.avatarUrl
                        val freshName = prof.displayName.ifBlank { prof.username }
                        val freshBio = prof.bio ?: ""
                        val freshOnline = prof.isOnline
                        selectedFriend = selectedFriend?.copy(
                            avatarUrl = freshAvatar ?: selectedFriend?.avatarUrl,
                            name = if (freshName.isNotBlank()) freshName else (selectedFriend?.name ?: cleanTarget),
                            bio = freshBio,
                            isOnline = freshOnline,
                            status = if (freshOnline) "Accepted Friend • Online" else "Accepted Friend • Offline"
                        )
                        val fIdx = friendsList.indexOfFirst { it.id == friendId }
                        if (fIdx != -1) {
                            friendsList[fIdx] = friendsList[fIdx].copy(
                                avatarUrl = freshAvatar ?: friendsList[fIdx].avatarUrl,
                                name = if (freshName.isNotBlank()) freshName else friendsList[fIdx].name,
                                bio = freshBio,
                                isOnline = freshOnline,
                                status = if (freshOnline) "Accepted Friend • Online" else "Accepted Friend • Offline"
                            )
                        }
                    }
                } catch (_: Exception) {}
            }

            // Continuous live polling every 1.5s
            while (true) {
                try {
                    val dmId = activeDmChannelId ?: ConnectoApiClient.channelCache[canonicalName.lowercase()] ?: canonicalName

                    if (dmId.isNotBlank()) {
                        val msgRes = ConnectoApiClient.getMessages(channelId = dmId, friendUsername = cleanTarget)
                        if (msgRes.isSuccess) {
                            val dtoList = msgRes.getOrThrow()
                            val unexpiredList = dtoList.distinctBy { it.id }
                            isLoadingMessages = false
                            if (unexpiredList.isNotEmpty()) {
                                // Learn channel UUID if returned in message DTOs
                                val serverChId = unexpiredList.firstOrNull {
                                    !it.channelId.isNullOrBlank() && !it.channelId.startsWith("dm-") && !it.channelId.startsWith("dm_")
                                }?.channelId
                                if (serverChId != null) {
                                    if (activeDmChannelId == null) activeDmChannelId = serverChId
                                    ConnectoApiClient.channelCache[canonicalName.lowercase()] = serverChId
                                    ConnectoApiClient.channelCache[serverChId.lowercase()] = canonicalName.lowercase()
                                }

                                withContext(Dispatchers.IO) {
                                    dbHelper.saveMessages(canonicalName, unexpiredList)
                                    if (dmId != canonicalName) {
                                        dbHelper.saveMessages(dmId, unexpiredList)
                                    }
                                    if (serverChId != null && serverChId != dmId && serverChId != canonicalName) {
                                        dbHelper.saveMessages(serverChId, unexpiredList)
                                    }
                                }
                                val myUsername = ConnectoApiClient.currentUsername ?: currentUsername
                                val myDisplayName = ConnectoApiClient.currentUserDisplayName ?: ""
                                val myUserId = ConnectoApiClient.currentUserId
                                val existingReactions = friendMessages.associate { it.id to it.reaction }
                                val mapped = unexpiredList.map { dto ->
                                    val isMe = (
                                        dto.authorId.equals(myUsername, ignoreCase = true) ||
                                        dto.authorName.equals(myUsername, ignoreCase = true) ||
                                        (myDisplayName.isNotBlank() && dto.authorName.equals(myDisplayName, ignoreCase = true)) ||
                                        (myUserId != null && (dto.authorId == myUserId || dto.authorName == myUserId)) ||
                                        dto.authorId.equals(currentUsername, ignoreCase = true) ||
                                        dto.authorName.equals(currentUsername, ignoreCase = true)
                                    )
                                    val init = if (dto.authorName.isNotBlank()) dto.authorName.first().toString().uppercase() else "U"
                                    val msgAvatar = if (isMe) {
                                        ConnectoApiClient.currentUserAvatarUrl
                                    } else {
                                        dto.authorAvatar ?: selectedFriend?.avatarUrl
                                    }
                                    CustomMessageItem(
                                        id = dto.id,
                                        senderName = dto.authorName,
                                        initial = init,
                                        content = dto.content,
                                        time = formatIsoTime(dto.createdAt),
                                        isMe = isMe,
                                        isDelivered = true,
                                        isRead = true,
                                        reaction = existingReactions[dto.id],
                                        avatarUrl = msgAvatar
                                    )
                                }

                                // Merge with any in-flight optimistic message that has not yet reached the server
                                val inFlight = friendMessages.filter {
                                    it.isMe && (it.id.startsWith("opt-") || it.time == "Just now") &&
                                    mapped.none { m -> m.content.trim() == it.content.trim() }
                                }
                                val merged = mapped + inFlight
                                if (friendMessages != merged) {
                                    friendMessages = merged
                                }

                                // Update last message in friends list and persist
                                val last = merged.last()
                                val fIdx = friendsList.indexOfFirst { it.id == friend.id || it.handle.removePrefix("@").equals(friend.handle.removePrefix("@"), ignoreCase = true) }
                                if (fIdx != -1) {
                                    friendsList[fIdx] = friendsList[fIdx].copy(
                                        lastMessage = last.content,
                                        timeAgo = last.time,
                                        unreadCount = 0
                                    )
                                    readPrefs.edit()
                                        .putString("last_msg_${friend.id}", last.content)
                                        .putString("last_time_${friend.id}", last.time)
                                        .putInt("unread_${friend.id}", 0)
                                        .apply()
                                }
                            } else {
                                // Server returned empty: NEVER wipe existing local cached or user messages.
                                isLoadingMessages = false
                            }
                        } else {
                            isLoadingMessages = false
                        }
                    }
                } catch (e: Exception) {
                    // Ignore transient network errors and ensure loading shimmer is dismissed
                    isLoadingMessages = false
                }
                delay(1500)
            }
        } else {
            friendMessages = emptyList()
            activeDmChannelId = null
            isLoadingMessages = false
        }
    }

    var hasInitiallyScrolledForFriend by remember(selectedFriend?.id) { mutableStateOf(false) }

    // Auto-scroll on new messages or input focus (instantly on initial open, animated on new messages)
    LaunchedEffect(friendMessages.size, isInputFocused) {
        if (selectedFriend != null && friendMessages.isNotEmpty()) {
            // +1 accounts for the "encryption_chip" header item at index 0
            val targetIdx = friendMessages.size  // messages occupy indices 1..friendMessages.size
            if (!hasInitiallyScrolledForFriend) {
                try { listState.scrollToItem(targetIdx) } catch (_: Exception) {}
                hasInitiallyScrolledForFriend = true
            } else {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val isNearBottom = lastVisible >= (targetIdx - 2).coerceAtLeast(0)
                if (isNearBottom || isInputFocused) {
                    try { listState.animateScrollToItem(targetIdx) } catch (_: Exception) {}
                }
            }
        }
    }

    LaunchedEffect(showFriendProfileDialog) {
        if (showFriendProfileDialog && selectedFriend != null) {
            val clean = selectedFriend!!.handle.removePrefix("@")
            try {
                val res = ConnectoApiClient.getProfile(clean)
                if (res.isSuccess) {
                    val prof = res.getOrThrow()
                    selectedFriend = selectedFriend?.copy(
                        avatarUrl = prof.avatarUrl ?: selectedFriend?.avatarUrl,
                        name = prof.displayName.ifBlank { prof.username },
                        bio = prof.bio ?: selectedFriend?.bio ?: "",
                        isOnline = prof.isOnline
                    )
                }
            } catch (e: Exception) {
                // Non-fatal
            }
        }
    }

    // Friend Profile Info Dialog
    if (showFriendProfileDialog && selectedFriend != null) {
        Dialog(onDismissRequest = { showFriendProfileDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showFriendProfileDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Avatar Circle
                    ConnectoAvatar(
                        name = selectedFriend?.name ?: "Friend",
                        avatarUrl = selectedFriend?.avatarUrl,
                        customSizeDp = 80.dp,
                        customFontSizeSp = 30,
                        borderWidth = 2.5.dp,
                        borderBrush = Brush.sweepGradient(gradientColors)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedFriend!!.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = selectedFriend!!.handle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = selectedFriend!!.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ABOUT ME",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = selectedFriend!!.bio.ifBlank { "Connecto Member" },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // START VOICE CALL BUTTON
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(gradientColors))
                                .clickable {
                                    val friend = selectedFriend
                                    showFriendProfileDialog = false
                                    if (friend != null) {
                                        initiateVoiceCallWithFriend(friend)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = contentOnGradient, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("VOICE CALL", color = contentOnGradient, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // UNFRIEND BUTTON
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDC2626).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable {
                                    showUnfriendConfirmDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("UNFRIEND", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Unfriend Confirmation Modal Dialog
    if (showUnfriendConfirmDialog && selectedFriend != null) {
        val friendToRemove = selectedFriend!!
        val friendUser = friendToRemove.handle.removePrefix("@").ifBlank { friendToRemove.name }
        val friendName = friendToRemove.name.ifBlank { friendUser }

        AlertDialog(
            onDismissRequest = { if (!isUnfriending) showUnfriendConfirmDialog = false },
            title = {
                Text(
                    text = "Unfriend $friendName?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove @$friendUser from your friends? Direct private messaging and voice calls will be disconnected.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            isUnfriending = true
                            try {
                                val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                                ConnectoApiClient.unfriendUser(targetUsername = friendUser, token = null)
                                dbHelper.deleteFriend(friendToRemove.id)
                                dbHelper.deleteFriend(friendUser)
                                friendsList.removeAll {
                                    it.id == friendToRemove.id ||
                                    it.handle.removePrefix("@").equals(friendUser, ignoreCase = true) ||
                                    it.name.equals(friendName, ignoreCase = true)
                                }
                                showUnfriendConfirmDialog = false
                                showFriendProfileDialog = false
                                selectedFriend = null
                                Toast.makeText(context, "Removed @$friendUser from friends", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isUnfriending = false
                            }
                        }
                    },
                    enabled = !isUnfriending
                ) {
                    if (isUnfriending) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFFEF4444))
                    } else {
                        Text("UNFRIEND", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnfriendConfirmDialog = false },
                    enabled = !isUnfriending
                ) {
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Add Friends & Friend Requests Search Dialog
    if (showAddFriendDialog) {
        val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
        AddFriendDialog(
            currentUsername = effUser,
            onDismissRequest = {
                showAddFriendDialog = false
                coroutineScope.launch { syncAcceptedFriends() }
            },
            onFriendAdded = {
                coroutineScope.launch { syncAcceptedFriends() }
            },
            onOpenChatWithFriend = { searchUser ->
                showAddFriendDialog = false
                val cleanHandle = searchUser.username.trim().removePrefix("@")
                val existing = friendsList.firstOrNull {
                    it.handle.equals("@$cleanHandle", ignoreCase = true) || it.id == searchUser.id
                }
                val friendItem = existing ?: FriendItem(
                    id = searchUser.id,
                    name = searchUser.displayName.ifBlank { cleanHandle },
                    handle = "@$cleanHandle",
                    initial = (searchUser.displayName.ifBlank { cleanHandle }).firstOrNull()?.toString()?.uppercase() ?: "F",
                    status = "Direct Chat Ready",
                    bio = "Connecto Member",
                    lastMessage = "Direct chat active • Tap to message",
                    timeAgo = "Just now",
                    unreadCount = 0,
                    isOnline = false
                )
                friendMessages = emptyList()
                activeDmChannelId = null
                isLoadingMessages = true
                typedMessage = messageDrafts[friendItem.id] ?: ""
                selectedFriend = friendItem
                onDirectChatStateChanged(true)
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ========== TOP HEADER ==========
            if (selectedFriend != null) {
                // 1:1 Direct Chat Header with Call Buttons & Friend Profile Info Trigger
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        .statusBarsPadding()
                        .heightIn(min = 68.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
                                    selectedFriend?.let { friend ->
                                        if (typedMessage.isNotBlank()) {
                                            messageDrafts[friend.id] = typedMessage
                                        } else {
                                            messageDrafts.remove(friend.id)
                                        }
                                    }
                                    typedMessage = ""
                                    selectedFriend = null
                                    activeDmChannelId = null
                                    isLoadingMessages = false
                                    onDirectChatStateChanged(false)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Friends List",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Friend Name & Avatar Row (Triggers Friend Profile Dialog)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showFriendProfileDialog = true }
                        ) {
                            ConnectoAvatar(
                                name = selectedFriend?.name ?: "Friend",
                                avatarUrl = selectedFriend?.avatarUrl,
                                customSizeDp = 42.dp,
                                customFontSizeSp = 16,
                                borderWidth = 1.5.dp,
                                borderColor = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = selectedFriend!!.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val isSelectedOnline = selectedFriend?.isOnline == true
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelectedOnline) OnlineGreen else Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isFriendTyping) "typing..." else if (isSelectedOnline) "Online • Direct Chat" else "Offline • Direct Chat",
                                        color = if (isFriendTyping) MaterialTheme.colorScheme.primary else if (isSelectedOnline) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = if (isFriendTyping) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Right Action Controls: Friend Info, Voice & Video Call Buttons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                                    .clickable { showFriendProfileDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Friend Profile Info",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    .clickable {
                                        val friend = selectedFriend
                                        if (friend != null) {
                                            initiateVoiceCallWithFriend(friend)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Voice Call",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                ConnectoTopHeader(
                    title = "DIRECT MESSAGES",
                    subtitle = "Chat with friends & members",
                    userInitial = userInitial,
                    onAddFriendClick = { showAddFriendDialog = true },
                    pendingFriendRequestsCount = pendingRequestsCount,
                    onAvatarClick = onOpenProfile
                )
            }

            // ========== ACTIVE CALL OVERLAY ==========
            val isCallVisible = (callState != CallState.IDLE)
            AnimatedVisibility(visible = isCallVisible) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    WebRtcCallOverlay(
                        callTitle = if (activeCallTitle.isNotBlank()) activeCallTitle else (selectedFriend?.name?.let { "Call with $it" } ?: "Voice Call"),
                        participantCount = 2,
                        allowVideo = false,
                        onEndCall = { VoiceCallManager.endCall() }
                    )
                }
            }

            AnimatedContent(
                targetState = selectedFriend != null,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), initialOffsetX = { fullWidth -> fullWidth }) + fadeIn(tween(280)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut(tween(280)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), initialOffsetX = { fullWidth -> -fullWidth }) + fadeIn(tween(280)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing), targetOffsetX = { fullWidth -> fullWidth }) + fadeOut(tween(280)))
                    }
                },
                label = "friendChatViewTransition",
                modifier = Modifier.weight(1f)
            ) { isFriendSelected ->
                if (!isFriendSelected) {
                    // ================= 1. FRIENDS LIST SCREEN =================
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        // Search Bar Item
                        item(key = "friends_search_bar") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Friends",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = friendSearchQuery,
                                    onValueChange = { friendSearchQuery = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    decorationBox = { inner ->
                                        if (friendSearchQuery.isEmpty()) {
                                            Text(
                                                text = "Search friends by name or username...",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }
                        }

                        item(key = "friends_section_title") {
                            Text(
                                text = "ACTIVE DIRECT CONVERSATIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (filteredFriends.isEmpty()) {
                            if (isFriendsLoadedOnce) {
                                item(key = "friends_empty_state") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .breathingPulse(minScale = 0.94f, maxScale = 1.06f)
                                                    .size(60.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(gradientColors)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PersonAdd,
                                                    contentDescription = null,
                                                    tint = contentOnGradient,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }

                                            Text(
                                                text = if (friendSearchQuery.isNotBlank()) "No Matching Friends" else "No Accepted Friends Yet",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )

                                            Text(
                                                text = if (friendSearchQuery.isNotBlank())
                                                    "No accepted friend matching '$friendSearchQuery' found."
                                                else
                                                    "Direct chat is private between accepted friends. Send a friend request and once they accept, you can chat with them here!",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 18.sp
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(Brush.linearGradient(gradientColors))
                                                    .clickable { showAddFriendDialog = true }
                                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PersonAdd,
                                                        contentDescription = null,
                                                        tint = contentOnGradient,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = "Find & Add Friends",
                                                        color = contentOnGradient,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                item(key = "friends_initial_loading") {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                    ) {
                                        repeat(4) {
                                            ShimmerConversationItem()
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(filteredFriends, key = { _, friend -> friend.id }) { index, friend ->
                            StaggeredReveal(index = index) {
                                Card(
                                onClick = {
                                    selectedFriend = friend
                                    typedMessage = messageDrafts[friend.id] ?: ""
                                    friendMessages = emptyList()
                                    activeDmChannelId = null
                                    isLoadingMessages = true
                                    onDirectChatStateChanged(true)
                                    val idx = friendsList.indexOfFirst { it.id == friend.id }
                                    if (idx != -1) {
                                        friendsList[idx] = friendsList[idx].copy(unreadCount = 0)
                                    }
                                    readPrefs.edit()
                                        .putInt("unread_${friend.id}", 0)
                                        .putBoolean("read_${friend.id}", true)
                                        .apply()
                                    onUnreadStatusChanged(friendsList.any { it.unreadCount > 0 })
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar with online status ring
                                    ConnectoAvatar(
                                        name = friend.name,
                                        avatarUrl = friend.avatarUrl,
                                        customSizeDp = 48.dp,
                                        customFontSizeSp = 18,
                                        status = if (friend.isOnline) ConnectoPresenceStatus.ONLINE else ConnectoPresenceStatus.OFFLINE,
                                        borderWidth = 1.5.dp,
                                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Center Info: Name, Handle, and Last Message
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = friend.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = friend.handle,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Text(
                                            text = friend.lastMessage,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Right column: formatted timestamp & unread badge
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = formatIsoTime(friend.timeAgo),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (friend.unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )

                                        if (friend.unreadCount > 0) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${friend.unreadCount}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                } else {
                    // ================= 2. DIRECT CHAT STREAM WITH IME RESIZE =================
                    val canScrollDown by remember {
                        derivedStateOf {
                            listState.canScrollForward
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (isLoadingMessages) {
                                ShimmerMessageFeed(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp, vertical = 8.dp)
                                )
                            } else if (friendMessages.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(gradientColors)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SportsEsports,
                                                contentDescription = null,
                                                tint = contentOnGradient,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Text(
                                            text = "Say hello to " + (selectedFriend?.name ?: "Friend") + "!",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "You are connected! Send a message or pick a quick gaming invite below to start chatting.",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                                ) {
                                    // Security Vault Encryption Chip
                                    item(key = "encryption_chip") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "🔒 End-to-End Argon2id Encrypted Chat Session",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    itemsIndexed(
                                        friendMessages.distinctBy { it.id },
                                        key = { _, msg -> msg.id }
                                    ) { _, msg ->
                                        CustomChatBubble(
                                            message = msg,
                                            ownBg = ownBubbleBg,
                                            ownText = ownBubbleText,
                                            friendBg = friendBubbleBg,
                                            friendBorder = friendBubbleBorder,
                                            friendText = friendBubbleText
                                        )
                                    }

                                    if (isFriendTyping) {
                                        item(key = "typing_indicator") {
                                            AnimatedTypingIndicator(
                                                friendName = selectedFriend?.name ?: "Friend"
                                            )
                                        }
                                    }
                                }
                            }

                            // Floating "Scroll to Bottom" FAB
                            if (friendMessages.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(end = 16.dp, bottom = 8.dp)
                                ) {
                                    AnimatedVisibility(
                                        visible = canScrollDown,
                                        enter = fadeIn() + scaleIn(),
                                        exit = fadeOut() + scaleOut()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                .shadow(4.dp, CircleShape)
                                                .clickable {
                                                    coroutineScope.launch {
                                                        if (friendMessages.isNotEmpty()) {
                                                            listState.animateScrollToItem(friendMessages.size)
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Scroll to bottom",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ================= QUICK GAMER ACTION CHIPS (COLLAPSIBLE) =================
                        AnimatedVisibility(
                            visible = !isInputFocused && typedMessage.isEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val quickChips = listOf(
                                    "👋 Hey, how are you doing?",
                                    "📅 Free for a quick call?",
                                    "🚀 Just sent you an update",
                                    "👍 Sounds great!"
                                )
                                items(quickChips) { chipText ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                            .clickable {
                                                typedMessage = chipText
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = chipText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // ================= RICH EMOJI PICKER TRAY (100+ CATEGORIZED EMOJIS) =================
                        AnimatedVisibility(
                            visible = showEmojiPicker,
                            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(initialScale = 0.94f),
                            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleOut(targetScale = 0.94f)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                                ConnectoEmojiPicker(
                                    onEmojiSelected = { emoji ->
                                        typedMessage += emoji
                                    },
                                    onClose = { showEmojiPicker = false }
                                )
                            }
                        }

                        // ================= 3. ADVANCED CHAT INPUT BAR & SEND BUTTON =================
                        val isAcceptedFriend = selectedFriend == null || friendsList.any {
                            it.id == selectedFriend!!.id ||
                            it.handle.removePrefix("@").equals(selectedFriend!!.handle.removePrefix("@"), ignoreCase = true)
                        }

                        if (selectedFriend != null && !isAcceptedFriend) {
                            val targetHandle = selectedFriend!!.handle.removePrefix("@")
                            var isSendingReq by remember { mutableStateOf(false) }
                            var reqSentLocally by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color(0xFFF87171),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Direct Messaging Restricted",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFF87171)
                                            )
                                            Text(
                                                text = if (reqSentLocally) "Friend request sent. Waiting for @$targetHandle to accept."
                                                       else "You must be accepted friends to send messages.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (reqSentLocally) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Pending", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                if (!isSendingReq) {
                                                    isSendingReq = true
                                                    coroutineScope.launch {
                                                        try {
                                                            val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                                                            val res = ConnectoApiClient.sendFriendRequest(targetHandle, myUsernameOverride = effUser)
                                                            if (res.isSuccess) {
                                                                reqSentLocally = true
                                                                Toast.makeText(context, "Friend request sent to @$targetHandle", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, "Could not send request: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                        } finally {
                                                            isSendingReq = false
                                                        }
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Add Friend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                            // Text Field Box (Multi-line Expandable)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 46.dp, max = 120.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = 1.dp,
                                        color = if (isInputFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(22.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = typedMessage,
                                    onValueChange = { typedMessage = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { isInputFocused = it.isFocused },
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    maxLines = 4,
                                    decorationBox = { inner ->
                                        if (typedMessage.isEmpty()) {
                                            Text(
                                                text = "Type message for " + (selectedFriend?.name ?: "friend") + "...",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp
                                            )
                                        }
                                        inner()
                                    }
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Emoji Toggle Button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable { showEmojiPicker = !showEmojiPicker },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                                        contentDescription = "Emoji picker",
                                        tint = if (showEmojiPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Dynamic Send Action Button
                            val onSendMessage = {
                                if (selectedFriend != null && !isAcceptedFriend) {
                                    Toast.makeText(context, "Direct messaging is restricted to accepted friends.", Toast.LENGTH_SHORT).show()
                                } else if (typedMessage.isNotBlank()) {
                                    val userMsg = typedMessage.trim()
                                    typedMessage = ""
                                    selectedFriend?.let { messageDrafts.remove(it.id) }
                                    showEmojiPicker = false

                                    val myName = ConnectoApiClient.currentUsername ?: currentUsername
                                    val tempId = "opt-${UUID.randomUUID()}"
                                    val localItem = CustomMessageItem(
                                        id = tempId,
                                        senderName = myName,
                                        initial = userInitial,
                                        content = userMsg,
                                        time = "Just now",
                                        isMe = true,
                                        isDelivered = true,
                                        isRead = true,
                                        avatarUrl = ConnectoApiClient.currentUserAvatarUrl
                                    )
                                    val updatedList = friendMessages + localItem
                                    friendMessages = updatedList

                                    coroutineScope.launch {
                                        try {
                                            val friend = selectedFriend
                                            val dmId = activeDmChannelId ?: if (friend != null) {
                                                val r = ConnectoApiClient.startDM(friend.handle.removePrefix("@"))
                                                if (r.isSuccess) {
                                                    val id = r.getOrThrow().id
                                                    activeDmChannelId = id
                                                    id
                                                } else null
                                            } else null

                                            if (dmId != null) {
                                                val sendRes = ConnectoApiClient.sendMessage(channelId = dmId, content = userMsg)
                                                if (sendRes.isSuccess) {
                                                    val sentDto = sendRes.getOrThrow()
                                                    withContext(Dispatchers.IO) {
                                                        dbHelper.saveMessage(sentDto)
                                                        val cleanTarget = friend?.handle?.removePrefix("@") ?: ""
                                                        val myUser = (ConnectoApiClient.currentUsername ?: currentUsername).trim().lowercase().removePrefix("@")
                                                        val canonicalName = "dm-${minOf(myUser, cleanTarget.lowercase())}-${maxOf(myUser, cleanTarget.lowercase())}"
                                                        dbHelper.saveMessages(canonicalName, listOf(sentDto))
                                                        if (dmId.isNotBlank() && dmId != canonicalName) {
                                                            dbHelper.saveMessages(dmId, listOf(sentDto))
                                                        }
                                                    }
                                                    val realItem = CustomMessageItem(
                                                        id = sentDto.id,
                                                        senderName = sentDto.authorName,
                                                        initial = userInitial,
                                                        content = sentDto.content,
                                                        time = formatIsoTime(sentDto.createdAt),
                                                        isMe = true,
                                                        isDelivered = true,
                                                        isRead = true,
                                                        avatarUrl = ConnectoApiClient.currentUserAvatarUrl
                                                    )
                                                    val curList = friendMessages.toMutableList()
                                                    val optIdx = curList.indexOfFirst {
                                                        it.id == tempId || (it.isMe && (it.id.startsWith("opt-") || it.time == "Just now") && it.content.trim() == userMsg)
                                                    }
                                                    if (optIdx != -1) {
                                                        curList[optIdx] = realItem.copy(reaction = curList[optIdx].reaction ?: realItem.reaction)
                                                        friendMessages = curList
                                                    } else if (curList.none { it.id == realItem.id }) {
                                                        curList.add(realItem)
                                                        friendMessages = curList
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Local message remains visible
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .graphicsLayer {
                                        scaleX = sendButtonScale
                                        scaleY = sendButtonScale
                                    }
                                    .clip(CircleShape)
                                    .background(
                                        if (typedMessage.isNotBlank()) Brush.linearGradient(gradientColors)
                                        else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (typedMessage.isNotBlank()) Color.Transparent else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (typedMessage.isNotBlank()) {
                                            onSendMessage()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Hold to record end-to-end encrypted voice note",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (typedMessage.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = contentOnGradient,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Note",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomChatBubble(
    message: CustomMessageItem,
    ownBg: Color,
    ownText: Color,
    friendBg: Color,
    friendBorder: Color,
    friendText: Color,
    modifier: Modifier = Modifier
) {
    var showReactionPill by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    var isBubblePressed by remember { mutableStateOf(false) }
    val bubbleScale by animateFloatAsState(
        targetValue = if (isBubblePressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "bubbleScale"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isMe) {
            ConnectoAvatar(
                name = message.senderName,
                avatarUrl = message.avatarUrl,
                customSizeDp = 32.dp,
                customFontSizeSp = 13,
                borderWidth = 1.dp,
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(min = 60.dp, max = 290.dp)
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = bubbleScale
                        scaleY = bubbleScale
                    }
                    .clip(
                        if (message.isMe) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                        else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                    )
                    .background(
                        if (message.isMe) {
                            SolidColor(MaterialTheme.colorScheme.primary)
                        } else {
                            SolidColor(friendBg)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (message.isMe) Color.White.copy(alpha = 0.15f) else friendBorder,
                        shape = if (message.isMe) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                        else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                    )
                    .pointerInput(message.id) {
                        detectTapGestures(
                            onPress = {
                                isBubblePressed = true
                                tryAwaitRelease()
                                isBubblePressed = false
                            },
                            onTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showReactionPill = !showReactionPill
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showReactionPill = true
                            }
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.content,
                        color = if (message.isMe) ownText else friendText,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatIsoTime(message.time),
                            fontSize = 10.sp,
                            color = if (message.isMe) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (message.isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val isPending = message.id.startsWith("opt-") || message.time.equals("Just now", ignoreCase = true)
                            Icon(
                                imageVector = if (isPending) Icons.Default.Schedule else Icons.Default.DoneAll,
                                contentDescription = if (isPending) "Sending" else if (message.isRead) "Read" else "Delivered",
                                tint = if (isPending) Color.White.copy(alpha = 0.5f) else if (message.isRead) Color(0xFF67E8F9) else Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Attached Reaction Badge with Bouncy Spring Entry
            AnimatedVisibility(
                visible = message.reaction != null,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                if (message.reaction != null) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = message.reaction!!, fontSize = 12.sp)
                    }
                }
            }

            // Interactive Emoji Reaction Pill Bar with Spring Overshoot
            AnimatedVisibility(
                visible = showReactionPill,
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
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("👍", "❤️", "😂", "🎉", "🚀", "💡", "👀").forEach { emoji ->
                        var isEmojiPressed by remember { mutableStateOf(false) }
                        val emojiScale by animateFloatAsState(
                            targetValue = if (isEmojiPressed) 1.5f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 500f),
                            label = "reactEmojiScale"
                        )

                        Text(
                            text = emoji,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = emojiScale
                                    scaleY = emojiScale
                                }
                                .clip(CircleShape)
                                .pointerInput(emoji) {
                                    detectTapGestures(
                                        onPress = {
                                            isEmojiPressed = true
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            tryAwaitRelease()
                                            isEmojiPressed = false
                                            message.reaction = if (message.reaction == emoji) null else emoji
                                            showReactionPill = false
                                        }
                                    )
                                }
                                .padding(3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedTypingIndicator(
    friendName: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typingTransition")

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 130, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 260, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(start = 40.dp, top = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = dot1Offset.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot2Offset.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot3Offset.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$friendName is typing...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
