package com.example.connecto.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.foundation.layout.widthIn
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
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        val sheetBg = if (colors.isDark) Color(0xFF12141C) else Color(0xFFFFFFFF)
        val searchBg = if (colors.isDark) Color(0xFF1A1D27) else Color(0xFFF1F5F9)
        val borderColor = if (colors.isDark) Color(0x1FFFFFFF) else Color(0x14000000)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.60f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismissRequest() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                    .background(sheetBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent click through to scrim */ }
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (colors.isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sheet Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start Voice Call",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Select participants for the call",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) Color(0xFF1A1D27) else Color(0xFFF1F5F9))
                            .clickable { onDismissRequest() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Input ("Search contacts to add...")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(searchBg)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF10B981)),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search contacts to add...",
                                    fontSize = 14.sp,
                                    color = colors.textSecondary.copy(alpha = 0.7f)
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

                // Horizontal Row of Selected Avatar Chips (tap to remove)
                AnimatedVisibility(visible = selectedFriends.isNotEmpty()) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(selectedFriends, key = { it.friendUsername }) { friend ->
                                val disp = friend.friendDisplayName.ifEmpty { friend.friendUsername }
                                val initial = disp.firstOrNull()?.uppercase() ?: "F"
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { selectedFriends.remove(friend) }
                                        .padding(horizontal = 2.dp)
                                ) {
                                    Box(modifier = Modifier.size(46.dp)) {
                                        if (!friend.friendAvatarUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = friend.friendAvatarUrl,
                                                contentDescription = disp,
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .align(Alignment.Center)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .align(Alignment.Center)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = initial,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        // Remove x badge on top-right
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .align(Alignment.TopEnd)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = disp,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 60.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contact List with Checkboxes & Online Dots
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 280.dp)
                ) {
                    when {
                        isLoadingFriends && friendsList.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF10B981),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                        friendsList.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No contacts available",
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        filteredFriends.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No contacts match \"$searchQuery\"",
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
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
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (isSelected) {
                                                    selectedFriends.removeAll { it.friendUsername == friend.friendUsername }
                                                } else {
                                                    selectedFriends.add(friend)
                                                }
                                            }
                                            .padding(horizontal = 6.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Avatar with Online dot
                                        Box(modifier = Modifier.size(42.dp)) {
                                            if (!friend.friendAvatarUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = friend.friendAvatarUrl,
                                                    contentDescription = displayName,
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(if (colors.isDark) Color(0xFF1E222D) else Color(0xFFE2E8F0)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = initial,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (colors.isDark) Color.White else Color(0xFF0F172A)
                                                    )
                                                }
                                            }

                                            // Online Green Dot
                                            if (friend.isOnline) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(11.dp)
                                                        .align(Alignment.BottomEnd)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF10B981))
                                                        .border(2.dp, sheetBg, CircleShape)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = displayName,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "@${friend.friendUsername}",
                                                fontSize = 12.sp,
                                                color = colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Circular Checkbox Indicator
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) Color(0xFF10B981)
                                                    else Color.Transparent
                                                )
                                                .border(
                                                    width = if (isSelected) 0.dp else 1.8.dp,
                                                    color = if (isSelected) Color.Transparent else if (colors.isDark) Color(0x40FFFFFF) else Color(0x30000000),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sticky Bottom CTA: "Start Voice Call (N)" (Solid Emerald #10B981, Zero Gradients)
                val isActionEnabled = selectedFriends.isNotEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isActionEnabled) Color(0xFF10B981)
                            else if (colors.isDark) Color(0xFF1E222D) else Color(0xFFE2E8F0)
                        )
                        .clickable(enabled = isActionEnabled) {
                            val title = if (selectedFriends.size == 1) {
                                "Voice Call with ${selectedFriends.first().friendDisplayName.ifEmpty { selectedFriends.first().friendUsername }}"
                            } else {
                                "Group Voice Call (${selectedFriends.size + 1})"
                            }
                            onStartVoiceGroup(title, selectedFriends.toList())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = if (isActionEnabled) Color.White else colors.textDisabled,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedFriends.isEmpty()) "Start Voice Call" else "Start Voice Call (${selectedFriends.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActionEnabled) Color.White else colors.textDisabled
                        )
                    }
                }
            }
        }
    }
}
