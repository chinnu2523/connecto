package com.example.connecto.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.network.NotificationDto
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onNavigateToChat: ((channelOrDmId: String) -> Unit)? = null,
    onNavigateToVoice: ((roomId: String) -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isOpen) return

    val coroutineScope = rememberCoroutineScope()
    val gradientColors = getDynamicAccentGradientColors()

    var notifications by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("All") }

    fun refresh() {
        coroutineScope.launch {
            isLoading = true
            val res = ConnectoApiClient.getNotifications(unreadOnly = false)
            if (res.isSuccess) {
                notifications = res.getOrDefault(emptyList())
            }
            ConnectoApiClient.getUnreadNotificationCount()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "NOTIFICATIONS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )

                    val unreadCount = notifications.count { !it.isRead }
                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$unreadCount new",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (notifications.any { !it.isRead }) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    ConnectoApiClient.markAllNotificationsRead()
                                    notifications = notifications.map { it.copy(isRead = true) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
                                contentDescription = "Mark all read",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    ConnectoApiClient.clearNotifications()
                                    notifications = emptyList()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Clear all",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips Row
            val filters = listOf("All", "Calls", "Requests", "Messages")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) Brush.linearGradient(gradientColors)
                                else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notification List
            val filteredNotifications = remember(notifications, selectedFilter) {
                when (selectedFilter) {
                    "Calls" -> notifications.filter { it.type.contains("call", ignoreCase = true) }
                    "Requests" -> notifications.filter { it.type.contains("friend", ignoreCase = true) }
                    "Messages" -> notifications.filter { it.type.contains("message", ignoreCase = true) || it.type.contains("chat", ignoreCase = true) }
                    else -> notifications
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "All Caught Up!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "No pending alerts or incoming call notifications",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { item ->
                        NotificationItemRow(
                            item = item,
                            gradientColors = gradientColors,
                            onItemClick = {
                                if (!item.isRead) {
                                    coroutineScope.launch {
                                        ConnectoApiClient.markNotificationRead(item.id)
                                        notifications = notifications.map {
                                            if (it.id == item.id) it.copy(isRead = true) else it
                                        }
                                    }
                                }
                                if (item.type.contains("call", ignoreCase = true)) {
                                    onDismiss()
                                    onNavigateToVoice?.invoke(item.referenceId ?: "")
                                } else if (item.type.contains("message", ignoreCase = true)) {
                                    onDismiss()
                                    onNavigateToChat?.invoke(item.referenceId ?: "")
                                }
                            },
                            onAcceptFriend = {
                                coroutineScope.launch {
                                    val sender = item.senderUsername ?: ""
                                    ConnectoApiClient.acceptFriendRequest(sender, item.referenceId ?: item.id)
                                    ConnectoApiClient.markNotificationRead(item.id)
                                    refresh()
                                }
                            },
                            onDeclineFriend = {
                                coroutineScope.launch {
                                    val sender = item.senderUsername ?: ""
                                    ConnectoApiClient.declineFriendRequest(sender, item.referenceId ?: item.id)
                                    ConnectoApiClient.markNotificationRead(item.id)
                                    refresh()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemRow(
    item: NotificationDto,
    gradientColors: List<Color>,
    onItemClick: () -> Unit,
    onAcceptFriend: () -> Unit,
    onDeclineFriend: () -> Unit
) {
    val isFriendRequest = item.type.contains("friend", ignoreCase = true) && !item.type.contains("accepted", ignoreCase = true)
    val isCall = item.type.contains("call", ignoreCase = true)

    val icon: ImageVector = when {
        isCall -> Icons.Rounded.PhoneInTalk
        isFriendRequest -> Icons.Rounded.PersonAdd
        item.type.contains("message", ignoreCase = true) -> Icons.Rounded.ChatBubble
        else -> Icons.Rounded.Notifications
    }

    val iconTint = when {
        isCall -> Color(0xFF3B82F6)
        isFriendRequest -> Color(0xFF10B981)
        item.type.contains("message", ignoreCase = true) -> Color(0xFF8B5CF6)
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (!item.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            )
            .border(
                width = if (!item.isRead) 1.2.dp else 0.8.dp,
                brush = if (!item.isRead) Brush.linearGradient(gradientColors)
                else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.04f))),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onItemClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Left Sender Avatar or Icon Badge
            val resolvedAvatar = item.senderAvatar?.let { raw ->
                if (raw.startsWith("http://") || raw.startsWith("https://")) raw
                else "https://connecto.fun/${raw.trimStart('/')}"
            }

            if (!resolvedAvatar.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Brush.linearGradient(gradientColors), CircleShape)
                ) {
                    AsyncImage(
                        model = resolvedAvatar,
                        contentDescription = item.senderUsername ?: item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else if (!item.senderUsername.isNullOrBlank()) {
                val initial = item.senderUsername.trim().take(1).uppercase()
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(iconTint.copy(alpha = 0.35f), iconTint.copy(alpha = 0.15f))
                            )
                        )
                        .border(1.2.dp, iconTint.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f))
                        .border(1.dp, iconTint.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.content,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
