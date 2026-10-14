package com.example.connecto.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.connecto.data.ConnectoDatabaseHelper
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.FriendshipDto
import com.example.connecto.network.GroupDto
import com.example.connecto.ui.designsystem.ConnectoRadius
import com.example.connecto.ui.designsystem.ConnectoSpacing
import com.example.connecto.ui.designsystem.LocalConnectoColors
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.liquidGlassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Dialog for selecting participants and starting a Group Voice Call.
 * Provides two selection modes:
 * 1. "Friends": Select individual friends from contacts to start a custom group call.
 * 2. "Existing Groups": Select an existing group chat to immediately launch a group call with all group members.
 */
@Composable
fun CreateVoiceGroupDialog(
    currentUsername: String,
    onDismissRequest: () -> Unit,
    onStartVoiceGroup: (groupTitle: String, selectedFriends: List<FriendshipDto>) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalConnectoColors.current
    val coroutineScope = rememberCoroutineScope()
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()
    val dbHelper = remember { ConnectoDatabaseHelper.getInstance(context) }

    var selectedMode by remember { mutableStateOf("FRIENDS") } // "FRIENDS" or "GROUPS"
    var groupTitle by remember { mutableStateOf("Voice Group Call") }
    var searchQuery by remember { mutableStateOf("") }
    var isLoadingFriends by remember { mutableStateOf(true) }
    var isLoadingGroups by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val friendsList = remember { mutableStateListOf<FriendshipDto>() }
    val selectedFriends = remember { mutableStateListOf<FriendshipDto>() }
    val groupsList = remember { mutableStateListOf<GroupDto>() }
    var selectedGroup by remember { mutableStateOf<GroupDto?>(null) }

    val cleanCurrent = currentUsername.trim().lowercase().removePrefix("@")

    // 0ms Cache-first loading of friends & groups, followed by network sync
    LaunchedEffect(currentUsername) {
        val cached = withContext(Dispatchers.IO) {
            dbHelper.getCachedFriends()
        }
        if (cached.isNotEmpty()) {
            val mappedCached = cached.filter {
                val u = it.handle.trim().lowercase().removePrefix("@")
                u.isNotBlank() && u != cleanCurrent
            }.map { f ->
                FriendshipDto(
                    id = f.id,
                    userId = currentUsername,
                    friendId = f.id,
                    friendUsername = f.handle.removePrefix("@"),
                    friendDisplayName = f.name,
                    friendAvatarUrl = f.avatarUrl,
                    status = "accepted",
                    isOnline = f.isOnline,
                    bio = f.bio
                )
            }
            if (mappedCached.isNotEmpty()) {
                friendsList.clear()
                friendsList.addAll(mappedCached)
                isLoadingFriends = false
            }
        }

        // Fetch fresh friends from network
        try {
            val res = ConnectoApiClient.getFriends(username = currentUsername)
            if (res.isSuccess) {
                val list = res.getOrThrow()
                val freshFiltered = list.filter { friend ->
                    val u = friend.friendUsername.trim().lowercase().removePrefix("@")
                    u.isNotBlank() && u != cleanCurrent
                }
                friendsList.clear()
                friendsList.addAll(freshFiltered)
                errorMessage = null
            } else if (friendsList.isEmpty()) {
                errorMessage = "Failed to load friends: ${res.exceptionOrNull()?.message}"
            }
        } catch (e: Exception) {
            if (friendsList.isEmpty()) {
                errorMessage = e.message ?: "Failed to load friends"
            }
        } finally {
            isLoadingFriends = false
        }

        // Fetch user groups for the Existing Groups tab
        isLoadingGroups = true
        try {
            val gRes = ConnectoApiClient.getUserGroups()
            if (gRes.isSuccess) {
                groupsList.clear()
                groupsList.addAll(gRes.getOrThrow())
            }
        } catch (_: Exception) {
        } finally {
            isLoadingGroups = false
        }
    }

    val filteredFriends = remember(friendsList, searchQuery) {
        if (searchQuery.isBlank()) {
            friendsList
        } else {
            val q = searchQuery.trim().lowercase()
            friendsList.filter {
                it.friendDisplayName.lowercase().contains(q) ||
                it.friendUsername.lowercase().contains(q)
            }
        }
    }

    val filteredGroups = remember(groupsList, searchQuery) {
        if (searchQuery.isBlank()) {
            groupsList
        } else {
            val q = searchQuery.trim().lowercase()
            groupsList.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.93f)
                .liquidGlassCard(shape = RoundedCornerShape(ConnectoRadius.lg))
        ) {
            Column(modifier = Modifier.padding(ConnectoSpacing.lg)) {

                // ── Header ──────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = contentOnGradient,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Group Voice Call",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (selectedMode == "FRIENDS") "Select friends to start a voice group" else "Select an existing group chat to call",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    IconButton(onClick = onDismissRequest, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(ConnectoSpacing.md))

                // ── Mode Selector Tabs (Friends vs Groups) ──────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.background)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Friends Tab
                    val isFriendsActive = selectedMode == "FRIENDS"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isFriendsActive) Brush.linearGradient(gradientColors)
                                else SolidColor(Color.Transparent)
                            )
                            .clickable {
                                selectedMode = "FRIENDS"
                                searchQuery = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isFriendsActive) contentOnGradient else colors.textSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Select Friends",
                                fontSize = 12.sp,
                                fontWeight = if (isFriendsActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isFriendsActive) contentOnGradient else colors.textSecondary
                            )
                        }
                    }

                    // Existing Groups Tab
                    val isGroupsActive = selectedMode == "GROUPS"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isGroupsActive) Brush.linearGradient(gradientColors)
                                else SolidColor(Color.Transparent)
                            )
                            .clickable {
                                selectedMode = "GROUPS"
                                searchQuery = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = if (isGroupsActive) contentOnGradient else colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (groupsList.isNotEmpty()) "Existing Groups (${groupsList.size})" else "Existing Groups",
                                fontSize = 12.sp,
                                fontWeight = if (isGroupsActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isGroupsActive) contentOnGradient else colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(ConnectoSpacing.md))

                if (selectedMode == "FRIENDS") {
                    // ── FRIENDS MODE: Group Call Name Input ──────────────────────
                    Text(
                        text = "VOICE GROUP NAME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(colors.background)
                            .border(
                                1.dp,
                                if (groupTitle.isNotEmpty()) colors.primary.copy(alpha = 0.6f) else colors.borderSubtle,
                                RoundedCornerShape(ConnectoRadius.sm)
                            )
                            .padding(horizontal = ConnectoSpacing.md, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = groupTitle,
                            onValueChange = { if (it.length <= 50) groupTitle = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = colors.textPrimary,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(colors.primary),
                            decorationBox = { inner ->
                                if (groupTitle.isEmpty()) {
                                    Text(
                                        text = "e.g. Squad Voice, Team Call...",
                                        fontSize = 13.sp,
                                        color = colors.textSecondary.copy(alpha = 0.6f)
                                    )
                                }
                                inner()
                            }
                        )
                        Text(
                            text = "${groupTitle.length}/50",
                            fontSize = 10.sp,
                            color = colors.textSecondary.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(ConnectoSpacing.sm))

                    // ── Filter Friends ──────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(colors.background)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.sm))
                            .padding(horizontal = ConnectoSpacing.md, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
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
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Filter friends by name...",
                                        fontSize = 13.sp,
                                        color = colors.textSecondary.copy(alpha = 0.6f)
                                    )
                                }
                                inner()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = colors.textSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ConnectoSpacing.sm))

                    // ── Friends Selection List Header ───────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECT FRIENDS (${selectedFriends.size} selected)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedFriends.isNotEmpty()) colors.primary else colors.textSecondary,
                            letterSpacing = 1.sp
                        )
                        if (friendsList.isNotEmpty()) {
                            Text(
                                text = if (selectedFriends.size == friendsList.size) "Deselect All" else "Select All",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary,
                                modifier = Modifier.clickable {
                                    if (selectedFriends.size == friendsList.size) {
                                        selectedFriends.clear()
                                    } else {
                                        selectedFriends.clear()
                                        selectedFriends.addAll(friendsList)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Selected Friends Chips ──────────────────────────────────
                    AnimatedVisibility(visible = selectedFriends.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            items(selectedFriends, key = { it.friendUsername }) { friend ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(colors.primary.copy(alpha = 0.14f))
                                        .border(1.dp, colors.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = friend.friendDisplayName.ifEmpty { friend.friendUsername },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = colors.primary,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable { selectedFriends.remove(friend) }
                                    )
                                }
                            }
                        }
                    }

                    // ── Friends Scroll List ─────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(colors.background)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.sm))
                    ) {
                        when {
                            isLoadingFriends && friendsList.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = colors.primary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Loading friends...",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            errorMessage != null && friendsList.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = errorMessage ?: "Failed to load",
                                        fontSize = 12.sp,
                                        color = colors.error,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            friendsList.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No friends found",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Add friends first to start a group call.",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            filteredFriends.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No friends match \"$searchQuery\"",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(filteredFriends, key = { it.friendUsername }) { friend ->
                                        val isSelected = selectedFriends.any { it.friendUsername == friend.friendUsername }
                                        val displayName = friend.friendDisplayName.ifEmpty { friend.friendUsername }
                                        val initial = displayName.firstOrNull()?.uppercase() ?: "F"

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (isSelected) {
                                                        selectedFriends.removeAll { it.friendUsername == friend.friendUsername }
                                                    } else {
                                                        selectedFriends.add(friend)
                                                    }
                                                }
                                                .background(
                                                    if (isSelected) colors.primary.copy(alpha = 0.08f)
                                                    else Color.Transparent
                                                )
                                                .padding(horizontal = ConnectoSpacing.md, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Avatar
                                            Box(modifier = Modifier.size(34.dp)) {
                                                if (!friend.friendAvatarUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = friend.friendAvatarUrl,
                                                        contentDescription = displayName,
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .clip(CircleShape)
                                                            .background(Brush.linearGradient(gradientColors)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = initial,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = contentOnGradient
                                                        )
                                                    }
                                                }
                                                if (friend.isOnline) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(9.dp)
                                                            .clip(CircleShape)
                                                            .background(OnlineGreen)
                                                            .border(1.5.dp, colors.surface, CircleShape)
                                                            .align(Alignment.BottomEnd)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(ConnectoSpacing.sm))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = displayName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = colors.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "@${friend.friendUsername} • ${if (friend.isOnline) "Online" else "Offline"}",
                                                    fontSize = 11.sp,
                                                    color = if (friend.isOnline) OnlineGreen else colors.textSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Checkbox
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) Brush.linearGradient(gradientColors)
                                                        else SolidColor(Color.Transparent)
                                                    )
                                                    .border(
                                                        1.5.dp,
                                                        if (isSelected) Color.Transparent else colors.borderSubtle,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = contentOnGradient,
                                                        modifier = Modifier.size(13.dp)
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
                    // ── GROUPS MODE: Select Existing Group Chat ──────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(colors.background)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.sm))
                            .padding(horizontal = ConnectoSpacing.md, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
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
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search existing group chats...",
                                        fontSize = 13.sp,
                                        color = colors.textSecondary.copy(alpha = 0.6f)
                                    )
                                }
                                inner()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = colors.textSecondary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ConnectoSpacing.sm))

                    Text(
                        text = "EXISTING GROUP CHATS (${filteredGroups.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 260.dp)
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(colors.background)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.sm))
                    ) {
                        when {
                            isLoadingGroups && groupsList.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = colors.primary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Loading groups...",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            groupsList.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No group chats found",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Create a group chat first or select friends above to start a voice group.",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            filteredGroups.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No groups match \"$searchQuery\"",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(filteredGroups, key = { it.id }) { group ->
                                        val isSelected = selectedGroup?.id == group.id
                                        val memberCount = if (group.memberCount > 0) group.memberCount else group.members.size.coerceAtLeast(1)
                                        val initial = group.name.firstOrNull()?.uppercase() ?: "G"

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedGroup = if (isSelected) null else group
                                                }
                                                .background(
                                                    if (isSelected) colors.primary.copy(alpha = 0.08f)
                                                    else Color.Transparent
                                                )
                                                .padding(horizontal = ConnectoSpacing.md, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Group Initial/Avatar
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(gradientColors)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = initial,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = contentOnGradient
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(ConnectoSpacing.sm))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "#${group.name}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "$memberCount members • ${if (group.isCreatedByMe) "Admin" else "Member"}",
                                                    fontSize = 11.sp,
                                                    color = colors.textSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Quick One-tap Start Call Button
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Brush.linearGradient(gradientColors))
                                                    .clickable {
                                                        val membersList = group.members.filter { !it.equals(currentUsername, ignoreCase = true) }.map { u ->
                                                            val clean = u.trim().removePrefix("@")
                                                            FriendshipDto(
                                                                id = clean,
                                                                userId = currentUsername,
                                                                friendId = clean,
                                                                friendUsername = clean,
                                                                friendDisplayName = clean,
                                                                friendAvatarUrl = null,
                                                                status = "accepted",
                                                                isOnline = true
                                                            )
                                                        }
                                                        onStartVoiceGroup(group.name, membersList)
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Call,
                                                        contentDescription = null,
                                                        tint = contentOnGradient,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Call",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = contentOnGradient
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(ConnectoSpacing.lg))

                // ── Action Buttons ───────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ConnectoSpacing.sm)
                ) {
                    // Cancel Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(colors.background)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.sm))
                            .clickable { onDismissRequest() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }

                    // Start Call Button
                    val isActionEnabled = if (selectedMode == "FRIENDS") {
                        groupTitle.isNotBlank() && selectedFriends.isNotEmpty()
                    } else {
                        selectedGroup != null
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(
                                if (isActionEnabled) Brush.linearGradient(gradientColors)
                                else SolidColor(colors.borderSubtle)
                            )
                            .clickable(enabled = isActionEnabled) {
                                if (selectedMode == "FRIENDS") {
                                    onStartVoiceGroup(groupTitle.trim(), selectedFriends.toList())
                                } else {
                                    selectedGroup?.let { g ->
                                        val membersList = g.members.filter { !it.equals(currentUsername, ignoreCase = true) }.map { u ->
                                            val clean = u.trim().removePrefix("@")
                                            FriendshipDto(
                                                id = clean,
                                                userId = currentUsername,
                                                friendId = clean,
                                                friendUsername = clean,
                                                friendDisplayName = clean,
                                                friendAvatarUrl = null,
                                                status = "accepted",
                                                isOnline = true
                                            )
                                        }
                                        onStartVoiceGroup(g.name, membersList)
                                    }
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = if (isActionEnabled) contentOnGradient else colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedMode == "FRIENDS") "START VOICE GROUP" else "CALL GROUP",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActionEnabled) contentOnGradient else colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
