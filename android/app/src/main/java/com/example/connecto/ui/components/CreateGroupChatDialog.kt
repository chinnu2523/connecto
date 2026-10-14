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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
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
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.FriendshipDto
import com.example.connecto.ui.designsystem.ConnectoRadius
import com.example.connecto.ui.designsystem.ConnectoSpacing
import com.example.connecto.ui.designsystem.LocalConnectoColors
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import kotlinx.coroutines.launch

/**
 * Dialog for creating a new group chat.
 * Shows the user's friends list for selecting members to add to the group chat.
 */
@Composable
fun CreateGroupChatDialog(
    currentUsername: String,
    onDismissRequest: () -> Unit,
    onGroupCreated: (groupName: String, groupId: String, members: List<String>) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val colors = LocalConnectoColors.current
    val coroutineScope = rememberCoroutineScope()
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    var groupName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isLoadingFriends by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }

    val friendsList = remember { mutableStateListOf<FriendshipDto>() }
    val selectedMembers = remember { mutableStateListOf<String>() }

    // Fetch ONLY the user's accepted friends on dialog launch
    LaunchedEffect(currentUsername) {
        isLoadingFriends = true
        errorMessage = null
        try {
            val res = ConnectoApiClient.getFriends(username = currentUsername)
            if (res.isSuccess) {
                val list = res.getOrThrow()
                friendsList.clear()
                // Filter out invalid/empty handles and self
                friendsList.addAll(
                    list.filter { friend ->
                        friend.friendUsername.isNotBlank() &&
                        !friend.friendUsername.equals(currentUsername, ignoreCase = true)
                    }
                )
            } else {
                errorMessage = "Failed to load friends: ${res.exceptionOrNull()?.message}"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load friends"
        } finally {
            isLoadingFriends = false
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

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.93f)
                .clip(RoundedCornerShape(ConnectoRadius.lg))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.lg))
        ) {
            Column(modifier = Modifier.padding(ConnectoSpacing.lg)) {

                // ── Header ──────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = null,
                            tint = contentOnGradient,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Create Group Chat",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Select friends to add to your group",
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

                // ── Group Name Field ────────────────────────────────────────────
                Text(
                    text = "GROUP NAME",
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
                            if (groupName.isNotEmpty()) colors.primary.copy(alpha = 0.6f) else colors.borderSubtle,
                            RoundedCornerShape(ConnectoRadius.sm)
                        )
                        .padding(horizontal = ConnectoSpacing.md, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = groupName,
                        onValueChange = { if (it.length <= 50) groupName = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        decorationBox = { inner ->
                            if (groupName.isEmpty()) {
                                Text(
                                    text = "e.g. Squad, Study Group...",
                                    fontSize = 14.sp,
                                    color = colors.textSecondary.copy(alpha = 0.6f)
                                )
                            }
                            inner()
                        }
                    )
                    Text(
                        text = "${groupName.length}/50",
                        fontSize = 10.sp,
                        color = colors.textSecondary.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(ConnectoSpacing.md))

                // ── Filter Friends Bar ──────────────────────────────────────────
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

                // ── Friends Header (Selection Counts & Quick Toggle) ─────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT FRIENDS (${selectedMembers.size} selected)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedMembers.isNotEmpty()) colors.primary else colors.textSecondary,
                        letterSpacing = 1.sp
                    )
                    if (friendsList.isNotEmpty()) {
                        Text(
                            text = if (selectedMembers.size == friendsList.size) "Deselect All" else "Select All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary,
                            modifier = Modifier.clickable {
                                if (selectedMembers.size == friendsList.size) {
                                    selectedMembers.clear()
                                } else {
                                    selectedMembers.clear()
                                    selectedMembers.addAll(friendsList.map { it.friendUsername })
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ── Selected Members Chips ───────────────────────────────────────
                AnimatedVisibility(visible = selectedMembers.isNotEmpty()) {
                    Column {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(selectedMembers) { memberUsername ->
                                val matchedFriend = friendsList.firstOrNull { it.friendUsername == memberUsername }
                                val label = matchedFriend?.friendDisplayName?.ifEmpty { memberUsername } ?: memberUsername

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(colors.primary.copy(alpha = 0.14f))
                                        .border(
                                            1.dp,
                                            colors.primary.copy(alpha = 0.4f),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove $label",
                                        tint = colors.primary.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { selectedMembers.remove(memberUsername) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Friends List Body ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 220.dp)
                        .clip(RoundedCornerShape(ConnectoRadius.sm))
                        .background(colors.background)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.sm))
                ) {
                    when {
                        isLoadingFriends -> {
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
                        errorMessage != null -> {
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
                                    text = "Add friends first to start a group chat.",
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
                                items(filteredFriends, key = { it.id.ifEmpty { it.friendUsername } }) { friend ->
                                    val isSelected = friend.friendUsername in selectedMembers
                                    val displayName = friend.friendDisplayName.ifEmpty { friend.friendUsername }
                                    val initial = displayName.firstOrNull()?.uppercase() ?: "F"

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isSelected) {
                                                    selectedMembers.remove(friend.friendUsername)
                                                } else {
                                                    selectedMembers.add(friend.friendUsername)
                                                }
                                            }
                                            .background(
                                                if (isSelected) colors.primary.copy(alpha = 0.08f)
                                                else Color.Transparent
                                            )
                                            .padding(horizontal = ConnectoSpacing.md, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Avatar with Online dot
                                        Box(modifier = Modifier.size(36.dp)) {
                                            if (!friend.friendAvatarUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = friend.friendAvatarUrl,
                                                    contentDescription = displayName,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .border(1.dp, colors.borderSubtle, CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(colors.primary.copy(alpha = 0.16f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = initial,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.primary
                                                    )
                                                }
                                            }

                                            if (friend.isOnline && !friend.isStealth) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(OnlineGreen)
                                                        .border(1.5.dp, colors.surface, CircleShape)
                                                        .align(Alignment.BottomEnd)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(ConnectoSpacing.md))

                                        // Name & Username
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
                                                text = "@${friend.friendUsername}",
                                                fontSize = 11.sp,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Checkbox Selection Circle
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
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
                                                    modifier = Modifier.size(14.dp)
                                                )
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
                    // Cancel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(colors.background)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.sm))
                            .clickable(enabled = !isCreating) { onDismissRequest() }
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

                    // Create
                    val isActionEnabled = groupName.isNotBlank() && selectedMembers.isNotEmpty() && !isCreating
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(
                                if (isActionEnabled) Brush.linearGradient(gradientColors)
                                else SolidColor(colors.borderSubtle)
                            )
                            .clickable(enabled = isActionEnabled) {
                                coroutineScope.launch {
                                    isCreating = true
                                    try {
                                        val result = ConnectoApiClient.createGroup(
                                            name = groupName.trim(),
                                            members = selectedMembers.toList()
                                        )
                                        if (result.isSuccess) {
                                            val responseJson = result.getOrNull()
                                            val groupId = responseJson
                                                ?.optJSONObject("group")
                                                ?.optString("id", "") ?: ""
                                            Toast.makeText(
                                                context,
                                                "Group \"${groupName.trim()}\" created!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onGroupCreated(groupName.trim(), groupId, selectedMembers.toList())
                                        } else {
                                            val errMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                                            Toast.makeText(context, "Failed to create group: $errMsg", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isCreating = false
                                    }
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = contentOnGradient,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isActionEnabled) contentOnGradient else colors.textSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Create",
                                    fontSize = 14.sp,
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
}
