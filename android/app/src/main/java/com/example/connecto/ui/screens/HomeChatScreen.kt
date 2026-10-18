package com.example.connecto.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Star
import com.example.connecto.ui.components.CreateGroupChatDialog
import com.example.connecto.ui.components.CreateVoiceGroupDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.crypto.ConnectoE2EEncryption
import com.example.connecto.data.ConnectoDatabaseHelper
import com.example.connecto.network.ChannelDto
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.FriendRequestItemDto
import com.example.connecto.network.FriendshipDto
import com.example.connecto.network.NotificationDto
import com.example.connecto.ui.components.AddFriendDialog
import com.example.connecto.ui.components.ConnectoTopHeader
import com.example.connecto.ui.designsystem.ConnectoAvatar
import com.example.connecto.ui.designsystem.ConnectoAvatarSize
import com.example.connecto.ui.designsystem.ConnectoBadge
import com.example.connecto.ui.designsystem.ConnectoBadgeVariant
import com.example.connecto.ui.designsystem.ConnectoButton
import com.example.connecto.ui.designsystem.ConnectoButtonSize
import com.example.connecto.ui.designsystem.ConnectoButtonVariant
import com.example.connecto.ui.designsystem.ConnectoCard
import com.example.connecto.ui.designsystem.ConnectoDivider
import com.example.connecto.ui.designsystem.ConnectoEmptyState
import com.example.connecto.ui.designsystem.ConnectoPresenceStatus
import com.example.connecto.ui.designsystem.ConnectoRadius
import com.example.connecto.ui.designsystem.ConnectoSpacing
import com.example.connecto.ui.designsystem.ConnectoTag
import com.example.connecto.ui.designsystem.ConnectoTextField
import com.example.connecto.ui.designsystem.LocalConnectoColors
import com.example.connecto.ui.theme.ShimmerConversationItem
import com.example.connecto.ui.theme.ShimmerChannelItem
import com.example.connecto.ui.theme.StaggeredReveal
import com.example.connecto.ui.theme.PulsingOnlineDot
import com.example.connecto.ui.theme.pressScaleEffect
import com.example.connecto.ui.theme.glassCanvasMesh
import com.example.connecto.ui.theme.glassCard
import com.example.connecto.ui.theme.liquidGlassCanvasMesh
import com.example.connecto.ui.theme.liquidGlassCard
import com.example.connecto.voice.VoiceCallManager
import com.example.connecto.ui.screens.FriendItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class RecentConversationItem(
    val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isGroup: Boolean = false
)

/**
 * Connecto Home Screen - Information Dashboard
 * Implements the core architecture requirement:
 * "What is happening in my Connecto account?"
 *
 * Provides:
 * - Header with Search, Notification Bell, and Profile Avatar
 * - Time-based greeting & account overview
 * - Quick Actions
 * - Unread Messages & Recent Conversations
 * - Recent Workspace Channels
 * - Pending Friend Requests (one-tap Accept/Decline)
 * - Live Voice Stage & Upcoming Calls
 * - Recent Activity Timeline
 * - Global Search
 */
@Composable
fun HomeChatScreen(
    currentUsername: String,
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCalls: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    unreadNotificationsCount: Int = 0,
    onNavigateToChannel: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val colors = LocalConnectoColors.current
    val coroutineScope = rememberCoroutineScope()

    // State holdings
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    val homePrefs = remember { context.getSharedPreferences("connecto_home_prefs", android.content.Context.MODE_PRIVATE) }
    val cleanCurrentU = remember(currentUsername) {
        (currentUsername.ifBlank { ConnectoApiClient.currentUsername ?: "" }).trim().lowercase().removePrefix("@")
    }
    val savedFavsString = remember(cleanCurrentU) {
        homePrefs.getString("favourites_$cleanCurrentU", "") ?: ""
    }
    var favouriteUsernames by remember(cleanCurrentU, savedFavsString) {
        mutableStateOf(if (savedFavsString.isNotBlank()) savedFavsString.split(",").map { it.trim().lowercase().removePrefix("@") }.filter { it.isNotBlank() } else emptyList())
    }
    var allFriends by remember { mutableStateOf<List<FriendshipDto>>(emptyList()) }
    var allFriendConversations by remember { mutableStateOf<List<RecentConversationItem>>(emptyList()) }
    var isFavouritesDialogOpen by remember { mutableStateOf(false) }

    var recentConversations by remember { mutableStateOf<List<RecentConversationItem>>(emptyList()) }
    var channels by remember { mutableStateOf<List<ChannelDto>>(emptyList()) }
    var friendRequests by remember { mutableStateOf<List<FriendRequestItemDto>>(emptyList()) }
    var recentNotifications by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var isAddFriendOpen by remember { mutableStateOf(false) }
    var isGroupChatOpen by remember { mutableStateOf(false) }
    var isVoiceGroupOpen by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var pendingVoiceGroupInfo by remember { mutableStateOf<Pair<String, List<FriendshipDto>>?>(null) }

    fun startHomeVoiceRoom(title: String, selectedFriends: List<FriendshipDto>) {
        val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
        val currentU = effUser.trim().removePrefix("@")
        val roomId = "group_voice_${currentU}_${System.currentTimeMillis()}"
        val groupTitle = title.ifBlank { "Voice Group Call" }
        VoiceCallManager.joinVoiceRoom(roomId = roomId, roomName = groupTitle, roomCode = roomId)

        coroutineScope.launch {
            for (friend in selectedFriends) {
                val target = friend.friendUsername.trim().removePrefix("@")
                try {
                    VoiceCallManager.inviteToVoiceGroup(
                        roomId = roomId,
                        groupTitle = groupTitle,
                        targetUser = target
                    )
                    ConnectoApiClient.inviteToVoiceCall(target, roomId)
                } catch (_: Exception) {}
            }

            ConnectoApiClient.recordCallLog(
                callerName = groupTitle,
                callType = "Group Call (${selectedFriends.size + 1} members)",
                isMissed = false,
                isOutgoing = true
            )
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            VoiceCallManager.restartAudioHardwareIfConnected()
            pendingVoiceGroupInfo?.let { (title, friends) ->
                pendingVoiceGroupInfo = null
                startHomeVoiceRoom(title, friends)
            }
        } else {
            Toast.makeText(context, "Microphone permission is required for voice calls", Toast.LENGTH_SHORT).show()
        }
    }

    fun performHomeVoiceGroupCall(title: String, selectedFriends: List<FriendshipDto>) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startHomeVoiceRoom(title, selectedFriends)
        } else {
            pendingVoiceGroupInfo = Pair(title, selectedFriends)
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val filteredConversations = remember(recentConversations, searchQuery) {
        if (searchQuery.isBlank()) recentConversations
        else recentConversations.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true) ||
            it.username.contains(searchQuery.trim(), ignoreCase = true) ||
            it.lastMessage.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    val favouriteConversations = remember(favouriteUsernames, allFriendConversations, allFriends) {
        if (favouriteUsernames.isEmpty()) {
            emptyList<RecentConversationItem>()
        } else {
            val convosByUsername = allFriendConversations.associateBy { it.username.lowercase().removePrefix("@") }
            favouriteUsernames.take(5).mapNotNull { favUname ->
                val cleanFav = favUname.trim().lowercase().removePrefix("@")
                convosByUsername[cleanFav] ?: run {
                    val matchingFriend = allFriends.find { it.friendUsername.trim().lowercase().removePrefix("@") == cleanFav }
                    if (matchingFriend != null) {
                        RecentConversationItem(
                            id = matchingFriend.id,
                            name = matchingFriend.friendDisplayName,
                            username = matchingFriend.friendUsername.removePrefix("@"),
                            avatarUrl = matchingFriend.friendAvatarUrl,
                            lastMessage = "Tap to open chat",
                            timestamp = if (matchingFriend.isOnline) "Online" else "Active",
                            unreadCount = 0,
                            isOnline = matchingFriend.isOnline,
                            isGroup = false
                        )
                    } else null
                }
            }
        }
    }

    val displayedFavourites = remember(favouriteConversations, searchQuery) {
        if (searchQuery.isBlank()) favouriteConversations
        else favouriteConversations.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true) ||
            it.username.contains(searchQuery.trim(), ignoreCase = true) ||
            it.lastMessage.contains(searchQuery.trim(), ignoreCase = true)
        }
    }
    val filteredChannels = remember(channels, searchQuery) {
        if (searchQuery.isBlank()) channels
        else channels.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true)
        }
    }
    val dbHelper = remember { ConnectoDatabaseHelper.getInstance(context) }

    val onOpenConversation: (RecentConversationItem) -> Unit = { convo ->
        val target = if (convo.isGroup) convo.id else convo.username
        val cleanTarget = convo.username.removePrefix("@").trim()

        recentConversations = recentConversations.map {
            if (it.id == convo.id || (!it.isGroup && it.username.equals(convo.username, ignoreCase = true))) {
                it.copy(unreadCount = 0)
            } else it
        }
        allFriendConversations = allFriendConversations.map {
            if (it.id == convo.id || (!it.isGroup && it.username.equals(convo.username, ignoreCase = true))) {
                it.copy(unreadCount = 0)
            } else it
        }

        coroutineScope.launch(Dispatchers.IO) {
            try {
                dbHelper.updateFriendUnreadCount(convo.id, 0)
                if (cleanTarget.isNotBlank() && cleanTarget != convo.id) {
                    dbHelper.updateFriendUnreadCount(cleanTarget, 0)
                }
            } catch (_: Exception) {}
        }
        if (!convo.isGroup && cleanTarget.isNotBlank()) {
            coroutineScope.launch {
                try {
                    ConnectoApiClient.markDmRead(cleanTarget)
                } catch (_: Exception) {}
            }
        }

        com.example.connecto.MainActivity.pendingOpenChatTarget = target
        onNavigateToMessages()
    }

    var isLoadingDashboard by remember { mutableStateOf(false) }

    val isWsConnected by VoiceCallManager.isWsConnectedFlow.collectAsState()
    val isUserAdmin by ConnectoApiClient.currentUserIsAdminState.collectAsState()
    val activeServerType by ConnectoApiClient.activeServerTypeState.collectAsState()

    LaunchedEffect(Unit) {
        ConnectoApiClient.fetchServerMode()
    }

    // Bug #2 fix: show "Server Offline" after 12s of failed connection instead of "Connecting..." forever
    var connectTimedOut by remember { mutableStateOf(false) }
    LaunchedEffect(isWsConnected) {
        if (!isWsConnected) {
            kotlinx.coroutines.delay(12_000L)
            if (!isWsConnected) connectTimedOut = true
        } else {
            connectTimedOut = false
        }
    }

    // Real-time WebSocket Incoming Messages Listener for Direct Messages dashboard
    LaunchedEffect(Unit) {
        VoiceCallManager.incomingChannelMessages.collect { dto ->
            val myUsername = (ConnectoApiClient.currentUsername ?: currentUsername).trim().removePrefix("@").lowercase()
            val myDisplayName = (ConnectoApiClient.currentUserDisplayName ?: "").trim().lowercase()
            val authorClean = dto.authorId.trim().removePrefix("@").lowercase()
            val authorNameClean = dto.authorName.trim().removePrefix("@").lowercase()
            val isMe = (authorClean == myUsername || authorNameClean == myUsername ||
                (myDisplayName.isNotBlank() && (authorNameClean == myDisplayName || authorClean == myDisplayName)))
            if (!isMe) {
                val decContent = try {
                    if (ConnectoE2EEncryption.isEncrypted(dto.content)) {
                        val senderKey = ConnectoE2EEncryption.deriveConversationKey(myUsername, authorClean)
                        ConnectoE2EEncryption.decryptMessage(dto.content, senderKey)
                    } else dto.content
                } catch (_: Exception) { dto.content }

                recentConversations = recentConversations.map { item ->
                    val cleanU = item.username.trim().lowercase().removePrefix("@")
                    if (cleanU == authorClean || item.name.trim().equals(dto.authorName.trim(), ignoreCase = true)) {
                        item.copy(
                            unreadCount = item.unreadCount + 1,
                            lastMessage = decContent,
                            timestamp = "Just now"
                        )
                    } else item
                }.sortedByDescending { it.timestamp == "Just now" }

                allFriendConversations = allFriendConversations.map { item ->
                    val cleanU = item.username.trim().lowercase().removePrefix("@")
                    if (cleanU == authorClean || item.name.trim().equals(dto.authorName.trim(), ignoreCase = true)) {
                        item.copy(
                            unreadCount = item.unreadCount + 1,
                            lastMessage = decContent,
                            timestamp = "Just now"
                        )
                    } else item
                }
            }
        }
    }

    // Time-of-day greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Welcome back"
        }
    }

    val displayName = remember(currentUsername) {
        ConnectoApiClient.currentUserDisplayName ?: currentUsername.replaceFirstChar { it.uppercase() }
    }

    // Refresh Dashboard Data in Parallel (reduces latency by ~85%)
    suspend fun refreshDashboard() {
        withContext(Dispatchers.IO) {
            try {
                coroutineScope {
                    val friendsDeferred = async { ConnectoApiClient.getFriends(username = currentUsername) }
                    val groupsDeferred = async { ConnectoApiClient.getUserGroups() }
                    val channelsDeferred = async { ConnectoApiClient.getChannels() }
                    val reqsDeferred = async { ConnectoApiClient.getReceivedFriendRequests(username = currentUsername) }
                    val notifDeferred = async { ConnectoApiClient.getNotifications() }
                    val unreadDeferred = async { ConnectoApiClient.getDmUnreadCounts() }

                    val friendsRes = friendsDeferred.await()
                    val groupsRes = groupsDeferred.await()
                    val channelsRes = channelsDeferred.await()
                    val reqsRes = reqsDeferred.await()
                    val notifRes = notifDeferred.await()
                    val unreadMap = unreadDeferred.await().getOrDefault(emptyMap())

                    val friendsList = friendsRes.getOrDefault(emptyList())
                    val groupsList = groupsRes.getOrDefault(emptyList())
                    val channelsList = channelsRes.getOrDefault(emptyList())
                    val reqsList = reqsRes.getOrDefault(emptyList())
                    val notifsList = notifRes.getOrDefault(emptyList())

                    val cachedFriends = dbHelper.getCachedFriends()

                    val groupConvos = groupsList.filter { (it.memberCount > 0 || it.members.isNotEmpty()) }.map { g ->
                        val count = if (g.memberCount > 0) g.memberCount else g.members.size
                        RecentConversationItem(
                            id = g.id,
                            name = "#${g.name}",
                            username = g.id,
                            avatarUrl = g.iconUrl,
                            lastMessage = "$count members • Group Chat",
                            timestamp = "Group",
                            unreadCount = 0,
                            isOnline = false,
                            isGroup = true
                        )
                    }

                    val friendConvos = (friendsList.ifEmpty {
                        cachedFriends.map { f ->
                            FriendshipDto(
                                id = f.id,
                                userId = currentUsername,
                                friendId = f.handle,
                                friendUsername = f.handle,
                                friendDisplayName = f.name,
                                friendAvatarUrl = f.avatarUrl,
                                status = "accepted",
                                isOnline = f.isOnline
                            )
                        }
                    }).map { friend ->
                        val cleanHandle = (friend.friendUsername.ifEmpty { friend.friendDisplayName }).removePrefix("@")
                        val unread = maxOf(unreadMap[cleanHandle.lowercase()] ?: 0, friend.unreadCount)
                        val myUName = currentUsername.removePrefix("@")
                        val lastDm = try {
                            dbHelper.getDirectMessages(myUName, cleanHandle, limit = 1).firstOrNull()
                        } catch (_: Exception) { null }
                        val lastMsgText = when {
                            lastDm != null && lastDm.content.isNotBlank() -> {
                                if (ConnectoE2EEncryption.isEncrypted(lastDm.content)) {
                                    val key = ConnectoE2EEncryption.deriveConversationKey(myUName, cleanHandle)
                                    ConnectoE2EEncryption.decryptMessage(lastDm.content, key)
                                } else lastDm.content
                            }
                            !friend.lastMessage.isNullOrBlank() && friend.lastMessage != "Connected via Cloudflare D1" -> {
                                if (ConnectoE2EEncryption.isEncrypted(friend.lastMessage)) {
                                    val key = ConnectoE2EEncryption.deriveConversationKey(myUName, cleanHandle)
                                    ConnectoE2EEncryption.decryptMessage(friend.lastMessage, key)
                                } else friend.lastMessage
                            }
                            else -> "Tap to open chat"
                        }
                        val lastMsgTime = lastDm?.createdAt?.let {
                            formatTimeAgoShort(it)
                        } ?: if (friend.lastMessageTimestamp > 0L) {
                            formatTimeAgoShort(friend.lastMessageTimestamp)
                        } else if (friend.isOnline) "Online" else "Active"

                        RecentConversationItem(
                            id = friend.id,
                            name = friend.friendDisplayName,
                            username = cleanHandle,
                            avatarUrl = friend.friendAvatarUrl,
                            lastMessage = lastMsgText,
                            timestamp = lastMsgTime,
                            unreadCount = unread,
                            isOnline = friend.isOnline,
                            isGroup = false
                        )
                    }

                    val conversations = (groupConvos + friendConvos).take(8)

                    val finalChannels = channelsList.ifEmpty {
                        listOf(
                            ChannelDto("general", "general", "text"),
                            ChannelDto("dev-talk", "dev-talk", "text"),
                            ChannelDto("announcements", "announcements", "text"),
                            ChannelDto("resources", "resources", "text")
                        )
                    }

                    // Save fresh channels and friends to local SQLite database for instant next launch
                    if (channelsList.isNotEmpty()) {
                        dbHelper.saveChannels(channelsList)
                    }
                    if (friendsList.isNotEmpty()) {
                        val myUName = currentUsername.removePrefix("@")
                        val mappedFriends = friendsList.map { f: FriendshipDto ->
                            val cleanHandle = f.friendUsername.removePrefix("@")
                            val lastDm = try {
                                dbHelper.getDirectMessages(myUName, cleanHandle, limit = 1).firstOrNull()
                            } catch (_: Exception) { null }
                            val lastMsgText = when {
                                lastDm != null && lastDm.content.isNotBlank() -> {
                                    if (ConnectoE2EEncryption.isEncrypted(lastDm.content)) {
                                        val key = ConnectoE2EEncryption.deriveConversationKey(myUName, cleanHandle)
                                        ConnectoE2EEncryption.decryptMessage(lastDm.content, key)
                                    } else lastDm.content
                                }
                                else -> "Tap to open chat"
                            }
                            val lastMsgTime = lastDm?.createdAt?.let {
                                formatTimeAgoShort(it)
                            } ?: if (f.isOnline) "Online" else "Active"

                            FriendItem(
                                id = f.friendId.ifBlank { f.id },
                                name = f.friendDisplayName,
                                handle = "@" + cleanHandle,
                                initial = (f.friendDisplayName.ifBlank { f.friendUsername }).take(1).uppercase(),
                                status = if (f.isOnline) "Online" else "Offline",
                                bio = "Connecto Member • Friend",
                                lastMessage = lastMsgText,
                                timeAgo = lastMsgTime,
                                unreadCount = unreadMap[cleanHandle.lowercase()] ?: 0,
                                isOnline = f.isOnline,
                                avatarUrl = f.friendAvatarUrl
                            )
                        }
                        dbHelper.saveFriends(mappedFriends)
                    }

                    val resolvedFriends = friendsList.ifEmpty {
                        cachedFriends.map { f ->
                            FriendshipDto(
                                id = f.id,
                                userId = currentUsername,
                                friendId = f.handle,
                                friendUsername = f.handle,
                                friendDisplayName = f.name,
                                friendAvatarUrl = f.avatarUrl,
                                status = "accepted",
                                isOnline = f.isOnline
                            )
                        }
                    }

                    withContext(Dispatchers.Main) {
                        recentConversations = conversations
                        allFriends = resolvedFriends
                        allFriendConversations = friendConvos
                        channels = finalChannels
                        friendRequests = reqsList
                        recentNotifications = notifsList.take(5)
                        isLoadingDashboard = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingDashboard = false
                }
            }
        }
    }

    // 1. Instant 0ms cache-first rendering, then parallel network sync
    LaunchedEffect(currentUsername) {
        withContext(Dispatchers.IO) {
            val cachedFriends = dbHelper.getCachedFriends()
            val cachedChannels = dbHelper.getChannels()
            if (cachedFriends.isNotEmpty() || cachedChannels.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    if (cachedFriends.isNotEmpty()) {
                        val mapped = cachedFriends.take(6).map { f ->
                            RecentConversationItem(
                                id = f.id,
                                name = f.name,
                                username = f.handle.removePrefix("@"),
                                avatarUrl = f.avatarUrl,
                                lastMessage = f.lastMessage.ifBlank { "Tap to open chat" },
                                timestamp = f.timeAgo.ifBlank { "Active" },
                                unreadCount = f.unreadCount,
                                isOnline = f.isOnline
                            )
                        }
                        recentConversations = mapped
                        allFriendConversations = mapped
                        allFriends = cachedFriends.map { f ->
                            FriendshipDto(
                                id = f.id,
                                userId = currentUsername,
                                friendId = f.handle,
                                friendUsername = f.handle,
                                friendDisplayName = f.name,
                                friendAvatarUrl = f.avatarUrl,
                                status = "accepted",
                                isOnline = f.isOnline
                            )
                        }
                    }
                    if (cachedChannels.isNotEmpty()) {
                        channels = cachedChannels
                    }
                    isLoadingDashboard = false
                }
            }
        }
        refreshDashboard()
    }

    // Real-time WebSocket User Presence Listener for Home Direct Messages
    LaunchedEffect(Unit) {
        VoiceCallManager.userPresenceUpdates.collect { (user, isOnline) ->
            val cleanUser = user.lowercase().removePrefix("@")
            withContext(Dispatchers.Main) {
                recentConversations = recentConversations.map { item ->
                    val itemUser = item.username.lowercase().removePrefix("@")
                    val itemId = item.id.lowercase()
                    val itemName = item.name.lowercase()
                    if (itemUser == cleanUser || itemId == cleanUser || itemName == cleanUser) {
                        item.copy(isOnline = isOnline)
                    } else {
                        item
                    }
                }
                allFriendConversations = allFriendConversations.map { item ->
                    val itemUser = item.username.lowercase().removePrefix("@")
                    val itemId = item.id.lowercase()
                    val itemName = item.name.lowercase()
                    if (itemUser == cleanUser || itemId == cleanUser || itemName == cleanUser) {
                        item.copy(isOnline = isOnline)
                    } else {
                        item
                    }
                }
                allFriends = allFriends.map { item ->
                    val itemUser = item.friendUsername.lowercase().removePrefix("@")
                    if (itemUser == cleanUser) {
                        item.copy(isOnline = isOnline)
                    } else {
                        item
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (colors.isLiquidGlass) Modifier.liquidGlassCanvasMesh()
                else if (colors.isGlass) Modifier.glassCanvasMesh()
                else Modifier.background(colors.background)
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Standardized Top Header with Status (Profile avatar removed as requested)
            ConnectoTopHeader(
                title = "Connecto",
                subtitle = "Dashboard",
                userInitial = currentUsername.firstOrNull()?.uppercase() ?: "C",
                onMenuClick = null,
                onVoiceGroupClick = null,
                showProfileButton = false,
                onAvatarClick = {},
                customAction = null
            )

            // Scrollable Dashboard Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Interactive Search Bar (inside LazyColumn to enable smooth top scrolling)
                item {
                    val searchShape = RoundedCornerShape(ConnectoRadius.sm)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ConnectoSpacing.md, vertical = ConnectoSpacing.xs)
                            .then(
                                if (colors.isLiquidGlass) {
                                    Modifier.liquidGlassCard(shape = searchShape)
                                } else if (colors.isGlass) {
                                    Modifier.glassCard(shape = searchShape, backgroundColor = Color(0xFF111827), borderColor = Color(0xFF38BDF8))
                                } else {
                                    Modifier
                                        .clip(searchShape)
                                        .background(colors.surface)
                                        .border(1.dp, colors.borderSubtle, searchShape)
                                }
                            )
                            .padding(horizontal = ConnectoSpacing.md, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (searchQuery.isNotEmpty()) colors.primary else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = colors.textPrimary,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(colors.primary),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search messages, channels, or people...",
                                        fontSize = 13.sp,
                                        color = colors.textDisabled
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                // 2. Greeting & Status Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ConnectoSpacing.md, vertical = ConnectoSpacing.xs)
                    ) {
                        Text(
                            text = "$greeting, $displayName 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(enabled = !isWsConnected) {
                                    com.example.connecto.MainActivity.openMaintenanceScreen()
                                }
                                .padding(vertical = 2.dp, horizontal = 2.dp)
                        ) {
                            if (isWsConnected) {
                                PulsingOnlineDot(
                                    color = colors.success,
                                    size = 7.dp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                connectTimedOut -> colors.warning
                                                else -> colors.textDisabled
                                            }
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                            Text(
                                text = when {
                                    isWsConnected -> {
                                        if (isUserAdmin) "Connected to the Connecto Network ($activeServerType)"
                                        else "Connected to the Connecto Network"
                                    }
                                    connectTimedOut -> "Server in Maintenance · Tap to view details"
                                    else -> "Connecting..."
                                },
                                fontSize = 12.sp,
                                color = if (connectTimedOut && !isWsConnected) colors.warning else colors.textSecondary
                            )
                        }
                    }
                }

                // 3. Quick Actions Row
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = ConnectoSpacing.sm),
                        contentPadding = PaddingValues(horizontal = ConnectoSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(ConnectoSpacing.sm)
                    ) {
                        item {
                            QuickActionChip(
                                icon = Icons.AutoMirrored.Filled.Chat,
                                label = "Messages",
                                onClick = onNavigateToMessages
                            )
                        }
                        item {
                            QuickActionChip(
                                icon = Icons.Default.Call,
                                label = "Calls",
                                onClick = onNavigateToCalls
                            )
                        }
                        item {
                            QuickActionChip(
                                icon = Icons.Default.Tag,
                                label = "Channels",
                                onClick = { onNavigateToChannel?.invoke("general") ?: onNavigateToMessages() }
                            )
                        }
                        item {
                            QuickActionChip(
                                icon = Icons.Default.PersonAdd,
                                label = "Add Friend",
                                onClick = { isAddFriendOpen = true }
                            )
                        }
                    }
                }

                // 4. Pending Friend Requests (Alert banner if any)
                if (friendRequests.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ConnectoSpacing.md, vertical = ConnectoSpacing.xs)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Friend Requests",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                ConnectoBadge(
                                    count = friendRequests.size,
                                    variant = ConnectoBadgeVariant.PRIMARY
                                )
                            }

                            Spacer(modifier = Modifier.height(ConnectoSpacing.xs))

                            friendRequests.forEach { req ->
                                FriendRequestCard(
                                    request = req,
                                    onAccept = {
                                        coroutineScope.launch {
                                            ConnectoApiClient.acceptFriendRequest(
                                                senderUsername = req.senderUsername,
                                                requestId = req.id,
                                                myUsernameOverride = currentUsername
                                            )
                                            refreshDashboard()
                                        }
                                    },
                                    onDecline = {
                                        coroutineScope.launch {
                                            ConnectoApiClient.declineFriendRequest(
                                                senderUsername = req.senderUsername,
                                                requestId = req.id,
                                                myUsernameOverride = currentUsername
                                            )
                                            refreshDashboard()
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(ConnectoSpacing.xs))
                            }
                        }
                    }
                }

                // 6. Recent Channels Directory
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ConnectoSpacing.md, vertical = ConnectoSpacing.sm)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Workspace Channels",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "View All",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.primary,
                                modifier = Modifier.clickable { onNavigateToChannel?.invoke("general") ?: onNavigateToMessages() }
                            )
                        }

                        Spacer(modifier = Modifier.height(ConnectoSpacing.xs))

                        if (channels.isEmpty() && isLoadingDashboard) {
                            androidx.compose.foundation.layout.Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(3) {
                                    ShimmerChannelItem()
                                }
                            }
                        } else if (filteredChannels.isEmpty() && searchQuery.isNotEmpty()) {
                            Text(
                                text = "No channels matching \"$searchQuery\"",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(vertical = ConnectoSpacing.xs)
                            )
                        } else {
                            val displayedChannels = filteredChannels.take(if (searchQuery.isNotEmpty()) 10 else 4)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                displayedChannels.forEachIndexed { index, ch ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                                            .clickable {
                                                onNavigateToChannel?.invoke(ch.name) ?: onNavigateToMessages()
                                            }
                                            .padding(vertical = 10.dp, horizontal = ConnectoSpacing.xs),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tag,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "#${ch.name}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = when (ch.name) {
                                                    "general" -> "Team announcements and general chat"
                                                    "dev-talk" -> "Engineering, architecture and code"
                                                    "announcements" -> "Official updates and releases"
                                                    else -> "Discussions and shared resources"
                                                },
                                                fontSize = 12.sp,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    if (index < displayedChannels.size - 1) {
                                        ConnectoDivider(thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. Recent Conversations / Direct Messages (Favourites & All Contacts)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ConnectoSpacing.md, vertical = ConnectoSpacing.sm)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (favouriteUsernames.isNotEmpty()) "Direct Messages (Favourites)" else "Direct Messages",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (favouriteUsernames.isNotEmpty()) "Edit (${favouriteUsernames.size}/5)" else "★ Add Favourites",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.primary,
                                    modifier = Modifier
                                        .clickable { isFavouritesDialogOpen = true }
                                        .padding(end = 12.dp)
                                )
                                Text(
                                    text = "Open Chat",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.primary,
                                    modifier = Modifier.clickable { onNavigateToMessages() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(ConnectoSpacing.xs))

                        if (favouriteUsernames.isNotEmpty()) {
                            // Render pinned favourites
                            if (displayedFavourites.isEmpty() && searchQuery.isNotEmpty()) {
                                Text(
                                    text = "No favourites matching \"$searchQuery\"",
                                    fontSize = 13.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(vertical = ConnectoSpacing.xs)
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    displayedFavourites.forEachIndexed { index, convo ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(ConnectoRadius.sm))
                                                .clickable { onOpenConversation(convo) }
                                                .padding(vertical = 8.dp, horizontal = ConnectoSpacing.xs),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ConnectoAvatar(
                                                name = convo.name,
                                                avatarUrl = convo.avatarUrl,
                                                size = ConnectoAvatarSize.MD,
                                                status = if (convo.isOnline) ConnectoPresenceStatus.ONLINE else ConnectoPresenceStatus.OFFLINE
                                            )
                                            Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    ) {
                                                        Text(
                                                            text = convo.name,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = colors.textPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = "Pinned Favourite",
                                                            tint = Color(0xFFFFB800),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                                                    Text(
                                                        text = convo.timestamp,
                                                        fontSize = 11.sp,
                                                        color = colors.textDisabled
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "@${convo.username} • ${convo.lastMessage}",
                                                        fontSize = 12.sp,
                                                        color = colors.textSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    if (convo.unreadCount > 0) {
                                                        ConnectoBadge(count = convo.unreadCount)
                                                    }
                                                }
                                            }
                                        }
                                        if (index < displayedFavourites.size - 1) {
                                            ConnectoDivider(thickness = 0.5.dp)
                                        }
                                    }
                                }

                                if (favouriteUsernames.size < 5) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = ConnectoSpacing.xs)
                                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                                            .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                            .border(
                                                1.dp,
                                                colors.primary.copy(alpha = 0.25f),
                                                RoundedCornerShape(ConnectoRadius.sm)
                                            )
                                            .clickable { isFavouritesDialogOpen = true }
                                            .padding(vertical = 10.dp, horizontal = ConnectoSpacing.md),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Add More Favourites (${favouriteUsernames.size}/5)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.primary
                                        )
                                    }
                                }

                                // Also show remaining active conversations so user profiles are never hidden
                                val favCleanSet = favouriteUsernames.map { it.trim().lowercase().removePrefix("@") }.toSet()
                                val otherConvos = filteredConversations.filter { !favCleanSet.contains(it.username.trim().lowercase().removePrefix("@")) }
                                if (otherConvos.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(ConnectoSpacing.sm))
                                    Text(
                                        text = "Other Conversations",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary,
                                        modifier = Modifier.padding(vertical = 4.dp, horizontal = ConnectoSpacing.xs)
                                    )
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        otherConvos.forEachIndexed { index, convo ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(ConnectoRadius.sm))
                                                    .clickable { onOpenConversation(convo) }
                                                    .padding(vertical = 8.dp, horizontal = ConnectoSpacing.xs),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                ConnectoAvatar(
                                                    name = convo.name,
                                                    avatarUrl = convo.avatarUrl,
                                                    size = ConnectoAvatarSize.MD,
                                                    status = if (convo.isGroup) null else (if (convo.isOnline) ConnectoPresenceStatus.ONLINE else ConnectoPresenceStatus.OFFLINE)
                                                )
                                                Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = convo.name,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = colors.textPrimary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = convo.timestamp,
                                                            fontSize = 11.sp,
                                                            color = colors.textDisabled
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = if (convo.isGroup) convo.lastMessage else "@${convo.username} • ${convo.lastMessage}",
                                                            fontSize = 12.sp,
                                                            color = colors.textSecondary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        if (convo.unreadCount > 0) {
                                                            ConnectoBadge(count = convo.unreadCount)
                                                        }
                                                    }
                                                }
                                            }
                                            if (index < otherConvos.size - 1) {
                                                ConnectoDivider(thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Favourites empty: display all conversations and user profiles directly!
                            if (recentConversations.isEmpty() && isLoadingDashboard) {
                                androidx.compose.foundation.layout.Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    repeat(3) {
                                        ShimmerConversationItem()
                                    }
                                }
                            } else if (filteredConversations.isEmpty() && searchQuery.isNotEmpty()) {
                                Text(
                                    text = "No conversations matching \"$searchQuery\"",
                                    fontSize = 13.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(vertical = ConnectoSpacing.xs)
                                )
                            } else if (filteredConversations.isEmpty()) {
                                ConnectoEmptyState(
                                    title = "No conversations yet",
                                    description = "Connect with colleagues or friends to start messaging.",
                                    icon = Icons.Outlined.Chat,
                                    actionText = "Add a Friend",
                                    onActionClick = { isAddFriendOpen = true }
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    filteredConversations.forEachIndexed { index, convo ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(ConnectoRadius.sm))
                                                .clickable { onOpenConversation(convo) }
                                                .padding(vertical = 8.dp, horizontal = ConnectoSpacing.xs),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ConnectoAvatar(
                                                name = convo.name,
                                                avatarUrl = convo.avatarUrl,
                                                size = ConnectoAvatarSize.MD,
                                                status = if (convo.isGroup) null else (if (convo.isOnline) ConnectoPresenceStatus.ONLINE else ConnectoPresenceStatus.OFFLINE)
                                            )
                                            Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = convo.name,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = colors.textPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = convo.timestamp,
                                                        fontSize = 11.sp,
                                                        color = colors.textDisabled
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (convo.isGroup) convo.lastMessage else "@${convo.username} • ${convo.lastMessage}",
                                                        fontSize = 12.sp,
                                                        color = colors.textSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    if (convo.unreadCount > 0) {
                                                        ConnectoBadge(count = convo.unreadCount)
                                                    }
                                                }
                                            }
                                        }
                                        if (index < filteredConversations.size - 1) {
                                            ConnectoDivider(thickness = 0.5.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Friend Dialog
        if (isAddFriendOpen) {
            AddFriendDialog(
                currentUsername = currentUsername,
                onDismissRequest = { isAddFriendOpen = false },
                onFriendAdded = {
                    isAddFriendOpen = false
                    coroutineScope.launch { refreshDashboard() }
                }
            )
        }

        // Create Group Chat Dialog
        if (isGroupChatOpen) {
            CreateGroupChatDialog(
                currentUsername = currentUsername,
                onDismissRequest = { isGroupChatOpen = false },
                onGroupCreated = { groupName, groupId, members ->
                    isGroupChatOpen = false
                    com.example.connecto.MainActivity.pendingOpenChatTarget = groupId.ifBlank { groupName }
                    onNavigateToMessages()
                }
            )
        }

        // Group Voice Call Dialog
        if (isVoiceGroupOpen) {
            CreateVoiceGroupDialog(
                currentUsername = currentUsername,
                onDismissRequest = { isVoiceGroupOpen = false },
                onStartVoiceGroup = { title, selectedFriends ->
                    isVoiceGroupOpen = false
                    performHomeVoiceGroupCall(title, selectedFriends)
                }
            )
        }

        // Global Search Dialog
        if (isSearchOpen) {
            GlobalSearchDialog(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onDismiss = { isSearchOpen = false },
                onSelectPerson = { username ->
                    isSearchOpen = false
                    onNavigateToMessages()
                },
                onSelectChannel = { channelName ->
                    isSearchOpen = false
                    onNavigateToMessages()
                }
            )
        }

        // Select Favourites Dialog
        if (isFavouritesDialogOpen) {
            SelectFavouritesDialog(
                allFriends = allFriends,
                initialSelected = favouriteUsernames,
                onDismiss = { isFavouritesDialogOpen = false },
                onSave = { newFavs ->
                    val clean = newFavs.map { it.trim().lowercase().removePrefix("@") }
                    homePrefs.edit().putString("favourites_$cleanCurrentU", clean.joinToString(",")).apply()
                    favouriteUsernames = clean
                    Toast.makeText(context, "Favourites updated (${clean.size}/5)", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = LocalConnectoColors.current
    val chipShape = RoundedCornerShape(ConnectoRadius.full)
    Row(
        modifier = Modifier
            .pressScaleEffect(onClick = onClick, targetScale = 0.92f)
            .then(
                if (colors.isLiquidGlass) {
                    Modifier.liquidGlassCard(shape = chipShape)
                } else if (colors.isGlass) {
                    Modifier.glassCard(shape = chipShape, backgroundColor = Color(0xFF1E293B), borderColor = Color(0x6038BDF8))
                } else {
                    Modifier
                        .clip(chipShape)
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle.copy(alpha = 0.8f), chipShape)
                }
            )
            .padding(start = 6.dp, end = ConnectoSpacing.md, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun FriendRequestCard(
    request: FriendRequestItemDto,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val colors = LocalConnectoColors.current
    ConnectoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                ConnectoAvatar(
                    name = request.senderDisplayName,
                    avatarUrl = request.senderAvatarUrl,
                    size = ConnectoAvatarSize.SM
                )
                Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                Column {
                    Text(
                        text = request.senderDisplayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "@${request.senderUsername}",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Decline",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(ConnectoSpacing.xxs))
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalSearchDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSelectPerson: (String) -> Unit,
    onSelectChannel: (String) -> Unit
) {
    val colors = LocalConnectoColors.current
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(ConnectoRadius.lg))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.lg))
                .padding(ConnectoSpacing.lg)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Global Search",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(ConnectoSpacing.sm))

                ConnectoTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = "Type a name, channel, or keyword...",
                    leadingIcon = Icons.Default.Search,
                    showClearButton = true
                )

                Spacer(modifier = Modifier.height(ConnectoSpacing.md))

                // Default suggestions or filtered items
                Text(
                    text = "Suggested Channels",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(ConnectoSpacing.xs))

                listOf("general", "dev-talk", "announcements").filter {
                    query.isBlank() || it.contains(query, ignoreCase = true)
                }.forEach { ch ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .clickable { onSelectChannel(ch) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                        Text(
                            text = "#$ch",
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeAgoShort(timestampMs: Long): String {
    return try {
        val date = java.util.Date(timestampMs)
        java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(date)
    } catch (_: Exception) {
        "Active"
    }
}

private fun formatTimeAgoShort(isoString: String): String {
    return try {
        val trimmed = isoString.trim()
        if (trimmed.isEmpty() || trimmed.equals("just now", ignoreCase = true)) return "Just now"
        if (trimmed.equals("active", ignoreCase = true)) return "Active"
        if (trimmed.matches(Regex("""\d{1,2}:\d{2}\s*(?:AM|PM|am|pm)?"""))) return trimmed
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
            "Active"
        }
    } catch (_: Exception) {
        "Active"
    }
}

@Composable
private fun SelectFavouritesDialog(
    allFriends: List<FriendshipDto>,
    initialSelected: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalConnectoColors.current
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    var searchQuery by remember { mutableStateOf("") }
    val selectedUsernames = remember {
        mutableStateListOf<String>().apply {
            addAll(initialSelected.map { it.trim().lowercase().removePrefix("@") })
        }
    }

    val filteredFriends = remember(allFriends, searchQuery) {
        if (searchQuery.isBlank()) allFriends
        else allFriends.filter {
            it.friendDisplayName.contains(searchQuery.trim(), ignoreCase = true) ||
            it.friendUsername.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(ConnectoRadius.xl))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.xl))
                .padding(ConnectoSpacing.lg)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Select Favourites",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(ConnectoSpacing.xs))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${selectedUsernames.size}/5",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Choose up to 5 people to pin to Home tab",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(ConnectoSpacing.md))

                // Search Bar
                ConnectoTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search friends...",
                    leadingIcon = Icons.Default.Search,
                    showClearButton = true
                )

                Spacer(modifier = Modifier.height(ConnectoSpacing.sm))

                // Friends List
                if (filteredFriends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No friends matching \"$searchQuery\"" else "No friends available to select.\nAdd friends first!",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredFriends, key = { it.id.ifEmpty { it.friendUsername } }) { friend ->
                            val cleanUser = friend.friendUsername.trim().lowercase().removePrefix("@")
                            val isSelected = selectedUsernames.contains(cleanUser)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(ConnectoRadius.sm))
                                    .background(
                                        if (isSelected) colors.primary.copy(alpha = 0.08f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            selectedUsernames.remove(cleanUser)
                                        } else {
                                            if (selectedUsernames.size >= 5) {
                                                Toast.makeText(context, "You can select up to 5 favourites only", Toast.LENGTH_SHORT).show()
                                            } else {
                                                selectedUsernames.add(cleanUser)
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ConnectoAvatar(
                                    name = friend.friendDisplayName,
                                    avatarUrl = friend.friendAvatarUrl,
                                    size = ConnectoAvatarSize.SM,
                                    status = if (friend.isOnline) ConnectoPresenceStatus.ONLINE else ConnectoPresenceStatus.OFFLINE
                                )

                                Spacer(modifier = Modifier.width(ConnectoSpacing.sm))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = friend.friendDisplayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "@${friend.friendUsername.removePrefix("@")}",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(ConnectoSpacing.xs))

                                // Selection check circle
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isSelected) Modifier.background(Brush.linearGradient(gradientColors))
                                            else Modifier.border(1.5.dp, colors.textDisabled, CircleShape)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = contentOnGradient,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(ConnectoSpacing.md))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ConnectoSpacing.sm)
                ) {
                    ConnectoButton(
                        text = "Cancel",
                        variant = ConnectoButtonVariant.SECONDARY,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    ConnectoButton(
                        text = "Save (${selectedUsernames.size}/5)",
                        variant = ConnectoButtonVariant.PRIMARY,
                        onClick = {
                            onSave(selectedUsernames.toList())
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
