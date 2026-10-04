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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.ui.designsystem.ConnectoRadius
import com.example.connecto.ui.designsystem.ConnectoSpacing
import com.example.connecto.ui.designsystem.LocalConnectoColors
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dialog for creating a new group chat.
 * Lets users name the group and add members by searching existing connections.
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
    var isSearching by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    val searchResults = remember { mutableStateListOf<String>() }
    val selectedMembers = remember { mutableStateListOf<String>() }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Debounce search
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
                            .filter { it != currentUsername && it !in selectedMembers }
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
                            text = "Add members and name your group",
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

                // ── Member Search ───────────────────────────────────────────────
                Text(
                    text = "ADD MEMBERS",
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
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.sm))
                        .padding(horizontal = ConnectoSpacing.md, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colors.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search by username...",
                                    fontSize = 14.sp,
                                    color = colors.textSecondary.copy(alpha = 0.6f)
                                )
                            }
                            inner()
                        }
                    )
                }

                // Search results
                AnimatedVisibility(
                    visible = searchResults.isNotEmpty(),
                    enter = fadeIn() + scaleIn(initialScale = 0.97f),
                    exit = fadeOut() + scaleOut(targetScale = 0.97f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(colors.background)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(ConnectoRadius.sm)),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(searchResults) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMembers.add(user)
                                        searchResults.remove(user)
                                        searchQuery = ""
                                    }
                                    .padding(horizontal = ConnectoSpacing.md, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.firstOrNull()?.uppercase() ?: "?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(ConnectoSpacing.sm))
                                Text(
                                    text = user,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = colors.textSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // ── Selected Members Chips ───────────────────────────────────────
                AnimatedVisibility(visible = selectedMembers.isNotEmpty()) {
                    Column {
                        Spacer(modifier = Modifier.height(ConnectoSpacing.sm))
                        Text(
                            text = "MEMBERS (${selectedMembers.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(selectedMembers) { member ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(colors.primary.copy(alpha = 0.12f))
                                        .border(
                                            1.dp,
                                            colors.primary.copy(alpha = 0.3f),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = member,
                                        fontSize = 12.sp,
                                        color = colors.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove $member",
                                        tint = colors.primary.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { selectedMembers.remove(member) }
                                    )
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(ConnectoRadius.sm))
                            .background(
                                if (groupName.isNotBlank() && selectedMembers.isNotEmpty())
                                    Brush.linearGradient(gradientColors)
                                else
                                    Brush.linearGradient(listOf(colors.borderSubtle, colors.borderSubtle))
                            )
                            .clickable(
                                enabled = groupName.isNotBlank() && selectedMembers.isNotEmpty() && !isCreating
                            ) {
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
                                    tint = if (groupName.isNotBlank() && selectedMembers.isNotEmpty())
                                        contentOnGradient else colors.textSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Create",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (groupName.isNotBlank() && selectedMembers.isNotEmpty())
                                        contentOnGradient else colors.textSecondary,
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
