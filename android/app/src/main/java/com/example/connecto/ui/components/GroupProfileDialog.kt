package com.example.connecto.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.connecto.ui.designsystem.ConnectoAvatar
import com.example.connecto.ui.screens.AVAILABLE_CHAT_WALLPAPERS
import com.example.connecto.ui.screens.ChatWallpaperOption
import com.example.connecto.ui.screens.FriendItem
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.TextDisabledColor
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen group profile details tab & management screen.
 * Displays group details, allows group admin (creator) to:
 * 1. Edit the group name
 * 2. Add / change group profile avatar photo
 * 3. Set custom or preset chat wallpaper for the group
 * 4. View all members with default Admin badge for creator
 * 5. Add new members via integrated member picker
 */
@Composable
fun FullScreenGroupProfileDialog(
    group: FriendItem,
    currentUsername: String,
    chatWallpaperId: String,
    customWallpaperUri: String?,
    onWallpaperSelected: (wallpaperId: String, customUri: String?) -> Unit,
    onGroupUpdated: (updatedGroup: FriendItem) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()
    val isLight = MaterialTheme.colorScheme.surface.red > 0.5f

    val myCleanUsername = currentUsername.trim().lowercase().removePrefix("@")
    val adminClean = group.admin.trim().lowercase().removePrefix("@")
    val creatorClean = group.creator.trim().lowercase().removePrefix("@")
    val isAdmin = group.isCreatedByMe ||
        (adminClean.isNotBlank() && adminClean == myCleanUsername) ||
        (creatorClean.isNotBlank() && creatorClean == myCleanUsername) ||
        (group.admin.isBlank() && group.creator.isBlank() && group.members.firstOrNull()?.trim()?.lowercase()?.removePrefix("@") == myCleanUsername)

    val effectiveAdminUsername = when {
        group.admin.isNotBlank() -> group.admin.trim().removePrefix("@")
        group.creator.isNotBlank() -> group.creator.trim().removePrefix("@")
        group.isCreatedByMe -> currentUsername.trim().removePrefix("@")
        group.members.isNotEmpty() -> group.members.first().trim().removePrefix("@")
        else -> "Admin"
    }

    var groupName by remember(group.name) { mutableStateOf(group.name) }
    var isEditingName by remember { mutableStateOf(false) }
    var editedNameInput by remember(group.name) { mutableStateOf(group.name) }
    var groupAvatarUrl by remember(group.avatarUrl) { mutableStateOf(group.avatarUrl) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var isSavingName by remember { mutableStateOf(false) }

    val membersList = remember { mutableStateListOf<String>().apply { addAll(group.members) } }
    var showAddMembersModal by remember { mutableStateOf(false) }

    // Group Avatar Picker Launcher
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isUploadingAvatar = true
                try {
                    val uploadRes = ConnectoApiClient.uploadGroupAvatar(context, uri)
                    if (uploadRes.isSuccess) {
                        val newAvatarUrl = uploadRes.getOrThrow()
                        groupAvatarUrl = newAvatarUrl
                        // Persist to group on server
                        val updateRes = ConnectoApiClient.updateGroup(group.id, iconUrl = newAvatarUrl)
                        if (updateRes.isSuccess) {
                            Toast.makeText(context, "Group profile picture updated!", Toast.LENGTH_SHORT).show()
                            val updated = group.copy(avatarUrl = newAvatarUrl)
                            onGroupUpdated(updated)
                        } else {
                            Toast.makeText(context, "Photo uploaded but group update failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Failed to upload group image: ${uploadRes.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploadingAvatar = false
                }
            }
        }
    }

    // Wallpaper Photo Picker Launcher
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            val uriStr = uri.toString()
            onWallpaperSelected("custom", uriStr)
            Toast.makeText(context, "Chat wallpaper applied to this group!", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top App Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Group Details",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${membersList.size} members • ${if (isAdmin) "You are Admin" else "Created by @$effectiveAdminUsername"}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Close text action
                        TextButton(onClick = onDismissRequest) {
                            Text(
                                text = "Done",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // ================= 1. GROUP AVATAR & HEADER CARD =================
                    item(key = "group_header_card") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Group Profile Picture / Avatar
                                Box(
                                    contentAlignment = Alignment.BottomEnd,
                                    modifier = Modifier.size(96.dp)
                                ) {
                                    if (!groupAvatarUrl.isNullOrBlank()) {
                                        ConnectoAvatar(
                                            name = groupName,
                                            avatarUrl = groupAvatarUrl,
                                            customSizeDp = 96.dp,
                                            customFontSizeSp = 36,
                                            borderWidth = 2.5.dp,
                                            borderColor = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(96.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                                                .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Groups,
                                                contentDescription = "Group",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(46.dp)
                                            )
                                        }
                                    }

                                    // If Admin, show camera badge button to change avatar
                                    if (isAdmin) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                                .clickable(enabled = !isUploadingAvatar) {
                                                    avatarPickerLauncher.launch(
                                                        androidx.activity.result.PickVisualMediaRequest(
                                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                                        )
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isUploadingAvatar) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = contentOnGradient
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.CameraAlt,
                                                    contentDescription = "Change Group Picture",
                                                    tint = contentOnGradient,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Group Name & Inline Edit
                                if (isEditingName && isAdmin) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            BasicTextField(
                                                value = editedNameInput,
                                                onValueChange = { editedNameInput = it },
                                                textStyle = TextStyle(
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = {
                                                    val trimmed = editedNameInput.trim()
                                                    if (trimmed.isNotBlank()) {
                                                        coroutineScope.launch {
                                                            isSavingName = true
                                                            try {
                                                                val res = ConnectoApiClient.updateGroup(group.id, name = trimmed)
                                                                if (res.isSuccess) {
                                                                    groupName = trimmed
                                                                    isEditingName = false
                                                                    Toast.makeText(context, "Group name saved!", Toast.LENGTH_SHORT).show()
                                                                    val updated = group.copy(name = trimmed)
                                                                    onGroupUpdated(updated)
                                                                } else {
                                                                    Toast.makeText(context, "Failed to save: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                                                }
                                                            } finally {
                                                                isSavingName = false
                                                            }
                                                        }
                                                    }
                                                },
                                                enabled = !isSavingName && editedNameInput.isNotBlank(),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                if (isSavingName) {
                                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = contentOnGradient)
                                                } else {
                                                    Text("Save Name", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                            }

                                            TextButton(onClick = {
                                                isEditingName = false
                                                editedNameInput = groupName
                                            }) {
                                                Text("Cancel", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "#$groupName",
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isAdmin) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = { isEditingName = true },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Group Name",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Badges Row: Admin Tag & Member Count
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isAdmin) "GROUP ADMIN" else "MEMBER",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(OnlineGreen.copy(alpha = 0.15f))
                                            .border(1.dp, OnlineGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${membersList.size} PARTICIPANTS",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnlineGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ================= 2. GROUP CHAT WALLPAPER OPTION =================
                    item(key = "group_wallpaper_section") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallpaper,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "GROUP CHAT WALLPAPER",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Custom photo wallpaper trigger card
                            val isCustomActive = chatWallpaperId == "custom" && !customWallpaperUri.isNullOrBlank()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isCustomActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        if (isCustomActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (isCustomActive) {
                                            AsyncImage(
                                                model = customWallpaperUri,
                                                contentDescription = "Custom Wallpaper",
                                                modifier = Modifier
                                                    .size(46.dp, 34.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddPhotoAlternate,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Custom Photo Wallpaper",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isCustomActive) "Custom photo set for this group" else "Pick custom photo from gallery",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = {
                                                wallpaperPickerLauncher.launch(
                                                    androidx.activity.result.PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text(if (isCustomActive) "Change" else "Pick", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (isCustomActive) {
                                            IconButton(
                                                onClick = {
                                                    onWallpaperSelected("default", null)
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove Custom Wallpaper",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Preset Wallpapers List
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                AVAILABLE_CHAT_WALLPAPERS.forEach { wallpaper ->
                                    val isSelected = chatWallpaperId == wallpaper.id
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                onWallpaperSelected(wallpaper.id, null)
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp, 32.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isLight) wallpaper.lightBrush else wallpaper.darkBrush)
                                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = wallpaper.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = wallpaper.subtitle,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ================= 3. MEMBERS ROSTER SECTION =================
                    item(key = "group_members_section_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "MEMBERS (${membersList.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Add Members Action Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable { showAddMembersModal = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = "Add Members",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Add Members",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Members List Items
                    items(membersList) { member ->
                        val memberClean = member.trim().lowercase().removePrefix("@")
                        val isMemberAdmin = (memberClean == effectiveAdminUsername.lowercase()) ||
                            (group.isCreatedByMe && memberClean == myCleanUsername)
                        val isCurrentUser = (memberClean == myCleanUsername)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ConnectoAvatar(
                                        name = member,
                                        avatarUrl = null,
                                        customSizeDp = 40.dp,
                                        customFontSizeSp = 15,
                                        borderWidth = 1.dp,
                                        borderColor = if (isMemberAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isCurrentUser) "$member (You)" else member,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = "@${member.trim().removePrefix("@")}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isMemberAdmin) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "ADMIN",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }

    // Modal to search and add new members to this group
    if (showAddMembersModal) {
        AddGroupMembersModal(
            groupId = group.id,
            existingMembers = membersList.toList(),
            currentUsername = currentUsername,
            onDismissRequest = { showAddMembersModal = false },
            onMembersAdded = { newMembers ->
                membersList.addAll(newMembers.filter { it !in membersList })
                val updated = group.copy(
                    members = membersList.toList(),
                    memberCount = membersList.size
                )
                onGroupUpdated(updated)
                showAddMembersModal = false
            }
        )
    }
}

/**
 * Sub-dialog for searching and adding new members to an existing group chat.
 */
@Composable
private fun AddGroupMembersModal(
    groupId: String,
    existingMembers: List<String>,
    currentUsername: String,
    onDismissRequest: () -> Unit,
    onMembersAdded: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var isAdding by remember { mutableStateOf(false) }
    val searchResults = remember { mutableStateListOf<String>() }
    val selectedMembers = remember { mutableStateListOf<String>() }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val existingSet = remember(existingMembers) {
        existingMembers.map { it.trim().lowercase().removePrefix("@") }.toSet()
    }

    LaunchedEffect(searchQuery) {
        searchJob?.cancel()
        if (searchQuery.length < 2) {
            searchResults.clear()
            isSearching = false
            return@LaunchedEffect
        }
        searchJob = coroutineScope.launch {
            delay(300)
            isSearching = true
            try {
                val res = ConnectoApiClient.searchUsers(searchQuery, username = currentUsername)
                if (res.isSuccess) {
                    val users = res.getOrThrow()
                    searchResults.clear()
                    searchResults.addAll(
                        users.map { it.username }
                            .filter {
                                val uClean = it.trim().lowercase().removePrefix("@")
                                uClean !in existingSet && it !in selectedMembers
                            }
                            .take(20)
                    )
                } else {
                    searchResults.clear()
                }
            } catch (_: Exception) {
                searchResults.clear()
            } finally {
                isSearching = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Add Members",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Search friends to add to this group",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                // Search Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search username...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Selected Chips
                if (selectedMembers.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Selected (${selectedMembers.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            selectedMembers.forEach { m ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "@$m", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clickable { selectedMembers.remove(m) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Search Results List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    if (isSearching) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    } else if (searchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.length < 2) "Type at least 2 characters to search" else "No users found",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(searchResults) { u ->
                                val isSelected = u in selectedMembers
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable {
                                            if (isSelected) selectedMembers.remove(u) else selectedMembers.add(u)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ConnectoAvatar(name = u, avatarUrl = null, customSizeDp = 28.dp, customFontSizeSp = 11)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "@$u", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        enabled = !isAdding
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            if (selectedMembers.isNotEmpty()) {
                                coroutineScope.launch {
                                    isAdding = true
                                    try {
                                        val res = ConnectoApiClient.addGroupMembers(groupId, selectedMembers.toList())
                                        if (res.isSuccess) {
                                            Toast.makeText(context, "Added ${selectedMembers.size} member(s)!", Toast.LENGTH_SHORT).show()
                                            onMembersAdded(selectedMembers.toList())
                                        } else {
                                            Toast.makeText(context, "Failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } finally {
                                        isAdding = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedMembers.isNotEmpty() && !isAdding,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = contentOnGradient)
                        } else {
                            Text("Add Selected", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
