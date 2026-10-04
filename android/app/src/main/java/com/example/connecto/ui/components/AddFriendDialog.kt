package com.example.connecto.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.FriendRequestItemDto
import com.example.connecto.network.UserSearchResultDto
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.pressScaleEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddFriendDialog(
    currentUsername: String = "",
    onDismissRequest: () -> Unit,
    onFriendAdded: () -> Unit = {},
    onOpenChatWithFriend: ((UserSearchResultDto) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Search & Add, 1: Received Requests

    // Search Tab States
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val searchResults = remember { mutableStateListOf<UserSearchResultDto>() }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val sentRequestUsernames = remember { mutableStateListOf<String>() }

    // Received Requests Tab States
    val receivedRequests = remember { mutableStateListOf<FriendRequestItemDto>() }
    var isLoadingRequests by remember { mutableStateOf(false) }
    var statusFeedbackMessage by remember { mutableStateOf<String?>(null) }

    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    // Fetch received friend requests function
    fun fetchReceivedRequests() {
        isLoadingRequests = true
        coroutineScope.launch {
            try {
                val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                val res = ConnectoApiClient.getReceivedFriendRequests(username = effUser)
                if (res.isSuccess) {
                    receivedRequests.clear()
                    receivedRequests.addAll(res.getOrThrow())
                }
            } catch (e: Exception) {
                // Ignore transient errors
            } finally {
                isLoadingRequests = false
            }
        }
    }

    // Live search function
    fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            isSearching = true
            if (query.isNotBlank()) {
                delay(250) // Debounce typing
            }
            try {
                val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                val res = ConnectoApiClient.searchUsers(query.trim(), username = effUser)
                if (res.isSuccess) {
                    searchResults.clear()
                    searchResults.addAll(res.getOrThrow())
                } else {
                    searchResults.clear()
                }
            } catch (e: Exception) {
                searchResults.clear()
            } finally {
                isSearching = false
            }
        }
    }


    val context = LocalContext.current

    // Live sync listener for real-time WebSocket friend updates
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                fetchReceivedRequests()
            }
        }
        val filter = IntentFilter("com.connecto.app.FRIEND_REQUEST_UPDATED")
        try {
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (_: Exception) {}
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    // Refresh when switching to Received Requests tab
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            fetchReceivedRequests()
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        fetchReceivedRequests()
        performSearch("")
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(26.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(26.dp)
                ),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // ================= 1. HEADER ROW =================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(gradientColors)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = contentOnGradient,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "ADD FRIENDS & REQUESTS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text = "Search directory & manage friend requests",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .pressScaleEffect(
                                onClick = onDismissRequest,
                                targetScale = 0.88f
                            )
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
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

                Spacer(modifier = Modifier.height(16.dp))

                // ================= 2. TAB SWITCHER =================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 0: Find Gamers
                    val isSearchTab = selectedTab == 0
                    val tab0Bg by animateColorAsState(
                        if (isSearchTab) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "tab0Bg"
                    )
                    val tab0Text by animateColorAsState(
                        if (isSearchTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "tab0Text"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .pressScaleEffect(
                                onClick = { selectedTab = 0 },
                                targetScale = 0.96f
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(tab0Bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                tint = tab0Text,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Find People",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = tab0Text
                            )
                        }
                    }

                    // Tab 1: Requests
                    val isReqTab = selectedTab == 1
                    val tab1Bg by animateColorAsState(
                        if (isReqTab) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "tab1Bg"
                    )
                    val tab1Text by animateColorAsState(
                        if (isReqTab) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "tab1Text"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .pressScaleEffect(
                                onClick = {
                                    selectedTab = 1
                                    fetchReceivedRequests()
                                },
                                targetScale = 0.96f
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(tab1Bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mail,
                                contentDescription = null,
                                tint = tab1Text,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Requests",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = tab1Text
                            )

                            if (receivedRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (isReqTab) Color.White else Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${receivedRequests.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isReqTab) MaterialTheme.colorScheme.primary else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Temporary feedback message toast banner
                AnimatedVisibility(visible = statusFeedbackMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(OnlineGreen.copy(alpha = 0.2f))
                            .border(1.dp, OnlineGreen.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusFeedbackMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ================= 3. TAB CONTENT =================
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(initialOffsetX = { it }) + fadeIn())
                                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
                        } else {
                            (slideInHorizontally(initialOffsetX = { -it }) + fadeIn())
                                .togetherWith(slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
                        }
                    },
                    label = "addFriendTabs",
                    modifier = Modifier.weight(1f)
                ) { tab ->
                    if (tab == 0) {
                        // =============== TAB 0: SEARCH USERS ===============
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Search Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        performSearch(it)
                                    },
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    singleLine = true,
                                    decorationBox = { inner ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search by username (e.g. marcus)...",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp
                                            )
                                        }
                                        inner()
                                    }
                                )

                                if (isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (searchQuery.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                searchQuery = ""
                                                performSearch("")
                                            }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Results List
                            if (isSearching && searchResults.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            } else if (searchResults.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonSearch,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = if (searchQuery.isBlank()) "No registered members found in directory." else "No users found matching '$searchQuery'",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 12.dp)
                                ) {
                                    if (searchQuery.isBlank()) {
                                        item(key = "discover_header") {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "COMMUNITY DIRECTORY (${searchResults.size})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    letterSpacing = 0.8.sp
                                                )
                                            }
                                        }
                                    }
                                    items(searchResults, key = { it.id }) { user ->
                                        val isSentLocally = sentRequestUsernames.contains(user.username)
                                        val effectiveRelation = if (isSentLocally) "pending_sent" else user.relationStatus
                                        val userInit = if (user.displayName.isNotBlank()) user.displayName.first().toString().uppercase()
                                                       else if (user.username.isNotBlank()) user.username.first().toString().uppercase()
                                                       else "G"

                                        Card(
                                            modifier = Modifier
                                                .animateItem()
                                                .fillMaxWidth()
                                                .then(
                                                    if (effectiveRelation == "friends" && onOpenChatWithFriend != null) {
                                                        Modifier.clickable { onOpenChatWithFriend(user) }
                                                    } else Modifier
                                                ),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(42.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = userInit,
                                                            fontSize = 16.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(10.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = user.displayName.ifBlank { user.username },
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "@${user.username}",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                // Action Button
                                                when (effectiveRelation) {
                                                    "friends" -> {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(10.dp))
                                                                    .background(OnlineGreen.copy(alpha = 0.15f))
                                                                    .border(1.dp, OnlineGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Icon(Icons.Default.Check, contentDescription = null, tint = OnlineGreen, modifier = Modifier.size(13.dp))
                                                                    Spacer(modifier = Modifier.width(3.dp))
                                                                    Text("Friends", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnlineGreen)
                                                                }
                                                            }

                                                            if (onOpenChatWithFriend != null) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(10.dp))
                                                                        .background(Brush.linearGradient(gradientColors))
                                                                        .clickable { onOpenChatWithFriend(user) }
                                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = contentOnGradient)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    "pending_sent" -> {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Pending", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                            }
                                                        }
                                                    }
                                                    "pending_received" -> {
                                                        Box(
                                                            modifier = Modifier
                                                                .pressScaleEffect(
                                                                    onClick = {
                                                                        selectedTab = 1
                                                                        fetchReceivedRequests()
                                                                    },
                                                                    targetScale = 0.92f
                                                                )
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(Brush.linearGradient(gradientColors))
                                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                        ) {
                                                            Text("Review Req", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = contentOnGradient)
                                                        }
                                                    }
                                                    else -> {
                                                        Box(
                                                            modifier = Modifier
                                                                .pressScaleEffect(
                                                                    onClick = {
                                                                         coroutineScope.launch {
                                                                            try {
                                                                                val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                                                                                val sendRes = ConnectoApiClient.sendFriendRequest(user.username, myUsernameOverride = effUser)
                                                                                if (sendRes.isSuccess) {
                                                                                    sentRequestUsernames.add(user.username)
                                                                                    statusFeedbackMessage = "Friend request sent to @${user.username}!"
                                                                                    delay(3000)
                                                                                    statusFeedbackMessage = null
                                                                                } else {
                                                                                    statusFeedbackMessage = sendRes.exceptionOrNull()?.message ?: "Failed to send request"
                                                                                    delay(3000)
                                                                                    statusFeedbackMessage = null
                                                                                }
                                                                            } catch (e: Exception) {
                                                                                // ignore
                                                                            }
                                                                        }
                                                                    },
                                                                    targetScale = 0.92f
                                                                )
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(Brush.linearGradient(gradientColors))
                                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = contentOnGradient, modifier = Modifier.size(13.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = contentOnGradient)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // =============== TAB 1: RECEIVED REQUESTS ===============
                        if (isLoadingRequests) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else if (receivedRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mail,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "No Pending Friend Requests",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "When other players send you a friend request, they will appear right here.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                items(receivedRequests, key = { it.id }) { req ->
                                    val reqInit = if (req.senderDisplayName.isNotBlank()) req.senderDisplayName.first().toString().uppercase()
                                                  else if (req.senderUsername.isNotBlank()) req.senderUsername.first().toString().uppercase()
                                                  else "G"

                                    Card(
                                        modifier = Modifier
                                            .animateItem()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                        .border(1.2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = reqInit,
                                                        fontSize = 17.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = req.senderDisplayName.ifBlank { req.senderUsername },
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "@${req.senderUsername}",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Accept & Decline Action Row
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Accept Button
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(36.dp)
                                                        .pressScaleEffect(
                                                            onClick = {
                                                                coroutineScope.launch {
                                                                    try {
                                                                        val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                                                                        val acceptRes = ConnectoApiClient.acceptFriendRequest(
                                                                            senderUsername = req.senderUsername,
                                                                            requestId = req.id,
                                                                            myUsernameOverride = effUser
                                                                        )
                                                                        if (acceptRes.isSuccess) {
                                                                            receivedRequests.remove(req)
                                                                            statusFeedbackMessage = "Accepted friend request from @${req.senderUsername}!"
                                                                            onFriendAdded()
                                                                            delay(3000)
                                                                            statusFeedbackMessage = null
                                                                        } else {
                                                                            statusFeedbackMessage = acceptRes.exceptionOrNull()?.message ?: "Failed to accept"
                                                                            delay(3000)
                                                                            statusFeedbackMessage = null
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        // ignore
                                                                    }
                                                                }
                                                            },
                                                            targetScale = 0.92f
                                                        )
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Brush.linearGradient(gradientColors)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = contentOnGradient, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = contentOnGradient)
                                                    }
                                                }

                                                // Decline Button
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(36.dp)
                                                        .pressScaleEffect(
                                                            onClick = {
                                                                coroutineScope.launch {
                                                                    try {
                                                                        val effUser = if (currentUsername.isNotBlank()) currentUsername else (ConnectoApiClient.currentUsername ?: "")
                                                                        val decRes = ConnectoApiClient.declineFriendRequest(
                                                                            senderUsername = req.senderUsername,
                                                                            requestId = req.id,
                                                                            myUsernameOverride = effUser
                                                                        )
                                                                        if (decRes.isSuccess) {
                                                                            receivedRequests.remove(req)
                                                                            statusFeedbackMessage = "Declined request from @${req.senderUsername}"
                                                                            delay(3000)
                                                                            statusFeedbackMessage = null
                                                                        } else {
                                                                            statusFeedbackMessage = decRes.exceptionOrNull()?.message ?: "Failed to decline"
                                                                            delay(3000)
                                                                            statusFeedbackMessage = null
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        // ignore
                                                                    }
                                                                }
                                                            },
                                                            targetScale = 0.92f
                                                        )
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Decline", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
