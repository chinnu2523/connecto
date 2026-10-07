package com.example.connecto.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.ui.text.input.ImeAction
import com.example.connecto.ui.designsystem.ConnectoTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.ui.components.AddFriendDialog
import com.example.connecto.ui.components.ConnectoTopHeader
import com.example.connecto.ui.components.WebRtcCallOverlay
import com.example.connecto.ui.components.FullScreenCallUI
import com.example.connecto.ui.theme.CyanGlow
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.StaggeredReveal
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.pressScaleEffect
import com.example.connecto.voice.CallState
import com.example.connecto.voice.VoiceCallManager
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CallLogItem(
    val id: String,
    val callerName: String,
    val initial: String,
    val callType: String,
    val timeAgo: String,
    val isMissed: Boolean = false,
    val isOutgoing: Boolean = false
)

@Composable
fun CallsScreen(
    currentUsername: String = "Member",
    onOpenProfile: () -> Unit = {},
    onOpenNotifications: (() -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var isInActiveCall by remember { mutableStateOf(false) }
    var activeCallTitle by remember { mutableStateOf("1:1 Voice Call") }
    var activeCallParticipants by remember { mutableIntStateOf(2) }
    var activeCallRoomCode by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Incoming", "Outgoing", "Missed"
    var callSearchQuery by remember { mutableStateOf("") }

    // Dialog state for clearing all or deleting
    var showClearAllDialog by remember { mutableStateOf(false) }
    var logPendingDelete by remember { mutableStateOf<CallLogItem?>(null) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }


    val liveCallLogs = remember {
        mutableStateListOf<CallLogItem>().apply {
            val cached = ConnectoApiClient.cachedCallLogs
            if (cached.isNotEmpty()) {
                val cleanLogs = cached.filter { log ->
                    !log.callerName.contains("Tactical", ignoreCase = true) &&
                    !log.callerName.contains("War Room", ignoreCase = true) &&
                    !log.callerName.contains("Cyber Defense", ignoreCase = true) &&
                    !log.callerName.contains("Lounge", ignoreCase = true) &&
                    !log.callerName.startsWith("#")
                }
                addAll(cleanLogs.map {
                    val pName = it.callerName
                        .removePrefix("1:1 Voice Call with ")
                        .removePrefix("Voice Call with ")
                        .removePrefix("Call with ")
                        .removePrefix("@")
                        .trim()
                    val init = if (pName.isNotBlank()) {
                        val clean = pName.trimStart { c -> !c.isLetterOrDigit() }
                        if (clean.isNotEmpty()) clean.first().toString().uppercase() else "P"
                    } else "P"
                    CallLogItem(
                        id = it.id,
                        callerName = pName,
                        initial = init,
                        callType = it.callType,
                        timeAgo = it.createdAt,
                        isMissed = it.isMissed,
                        isOutgoing = it.isOutgoing
                    )
                })
            }
        }
    }

    fun refreshCallLogs() {
        coroutineScope.launch {
            try {
                val res = ConnectoApiClient.getCallLogs(username = currentUsername)
                if (res.isSuccess) {
                    val logs = res.getOrThrow()
                    if (logs.isNotEmpty()) {
                        val cleanLogs = logs.filter { log ->
                            !log.callerName.contains("Tactical", ignoreCase = true) &&
                            !log.callerName.contains("War Room", ignoreCase = true) &&
                            !log.callerName.contains("Cyber Defense", ignoreCase = true) &&
                            !log.callerName.contains("Lounge", ignoreCase = true) &&
                            !log.callerName.startsWith("#")
                        }
                        val mapped = cleanLogs.map {
                            val pName = it.callerName
                                .removePrefix("1:1 Voice Call with ")
                                .removePrefix("Voice Call with ")
                                .removePrefix("Call with ")
                                .removePrefix("@")
                                .trim()
                            val init = if (pName.isNotBlank()) {
                                val clean = pName.trimStart { c -> !c.isLetterOrDigit() }
                                if (clean.isNotEmpty()) clean.first().toString().uppercase() else "P"
                            } else "P"
                            CallLogItem(
                                id = it.id,
                                callerName = pName,
                                initial = init,
                                callType = it.callType,
                                timeAgo = it.createdAt,
                                isMissed = it.isMissed,
                                isOutgoing = it.isOutgoing
                            )
                        }
                        if (liveCallLogs != mapped) {
                            liveCallLogs.clear()
                            liveCallLogs.addAll(mapped)
                        }
                    } else {
                        liveCallLogs.clear()
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun performDirectCall(sanitized: String) {
        activeCallTitle = "Voice Call with $sanitized"
        activeCallParticipants = 2
        activeCallRoomCode = null
        isInActiveCall = true
        VoiceCallManager.start1on1Call(sanitized, sanitized)
        coroutineScope.launch {
            ConnectoApiClient.recordCallLog(
                callerName = sanitized,
                callType = "Voice Call",
                isMissed = false,
                isOutgoing = true
            )
            refreshCallLogs()
        }
    }

    var pendingCallTarget by remember { mutableStateOf<String?>(null) }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            VoiceCallManager.restartAudioHardwareIfConnected()
            pendingCallTarget?.let { target ->
                performDirectCall(target)
            }
        } else {
            showToast("Microphone permission is required for voice calls")
        }
        pendingCallTarget = null
    }

    fun initiateDirectCall(target: String) {
        val sanitized = target.trim()
            .removePrefix("@")
            .removePrefix("1:1 Voice Call with ")
            .removePrefix("Voice Call with ")
            .removePrefix("Call with ")
            .trim()
        if (sanitized.isBlank()) {
            showToast("Please enter a username to dial")
            return
        }
        if (sanitized.equals(currentUsername, ignoreCase = true)) {
            showToast("You cannot call yourself")
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            performDirectCall(sanitized)
        } else {
            pendingCallTarget = sanitized
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) {
        refreshCallLogs()
        while (isActive) {
            delay(5000)
            try {
                val cRes = ConnectoApiClient.getCallLogs(username = currentUsername)
                if (cRes.isSuccess) {
                    val logs = cRes.getOrThrow()
                    if (logs.isNotEmpty()) {
                        val cleanLogs = logs.filter { log ->
                            !log.callerName.contains("Tactical", ignoreCase = true) &&
                            !log.callerName.contains("War Room", ignoreCase = true) &&
                            !log.callerName.contains("Cyber Defense", ignoreCase = true) &&
                            !log.callerName.contains("Lounge", ignoreCase = true) &&
                            !log.callerName.startsWith("#")
                        }
                        val mapped = cleanLogs.map {
                            val pName = it.callerName
                                .removePrefix("1:1 Voice Call with ")
                                .removePrefix("Voice Call with ")
                                .removePrefix("Call with ")
                                .removePrefix("@")
                                .trim()
                            val init = if (pName.isNotBlank()) {
                                val clean = pName.trimStart { c -> !c.isLetterOrDigit() }
                                if (clean.isNotEmpty()) clean.first().toString().uppercase() else "P"
                            } else "P"
                            CallLogItem(
                                id = it.id,
                                callerName = pName,
                                initial = init,
                                callType = it.callType,
                                timeAgo = it.createdAt,
                                isMissed = it.isMissed,
                                isOutgoing = it.isOutgoing
                            )
                        }
                        if (liveCallLogs != mapped) {
                            liveCallLogs.clear()
                            liveCallLogs.addAll(mapped)
                        }
                    } else {
                        if (liveCallLogs.isNotEmpty()) {
                            liveCallLogs.clear()
                        }
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    val callState by VoiceCallManager.callState.collectAsState()
    val managerCallTitle by VoiceCallManager.activeCallTitle.collectAsState()
    val managerRoomCode by VoiceCallManager.activeCallRoomCode.collectAsState()

    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()
    val userInitial = if (currentUsername.isNotBlank()) currentUsername.first().toString().uppercase() else "C"

    val tabFilteredLogs = when (selectedFilter) {
        "Incoming" -> liveCallLogs.filter { !it.isOutgoing }
        "Outgoing" -> liveCallLogs.filter { it.isOutgoing }
        "Missed" -> liveCallLogs.filter { it.isMissed }
        else -> liveCallLogs
    }

    val filteredLogs = remember(tabFilteredLogs, callSearchQuery) {
        if (callSearchQuery.isBlank()) {
            tabFilteredLogs
        } else {
            val q = callSearchQuery.trim()
            tabFilteredLogs.filter {
                it.callerName.contains(q, ignoreCase = true) ||
                it.callType.contains(q, ignoreCase = true) ||
                it.timeAgo.contains(q, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header (Notification bell removed)
            ConnectoTopHeader(
                title = "CALLS",
                subtitle = "Call History & Voice Hub",
                userInitial = userInitial,
                onAddFriendClick = { showAddFriendDialog = true },
                onAvatarClick = onOpenProfile,
                onNotificationClick = null,
                unreadNotificationsCount = 0
            )

            // Call History Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                ConnectoTextField(
                    value = callSearchQuery,
                    onValueChange = { callSearchQuery = it },
                    placeholder = "Search call history by name or date...",
                    leadingIcon = Icons.Default.Search,
                    showClearButton = true,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
            }

            // Quick Tab Filter Bar (All, Incoming, Outgoing, Missed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val quickTabs = listOf(
                        "All" to null,
                        "Incoming" to Icons.AutoMirrored.Filled.CallReceived,
                        "Outgoing" to Icons.AutoMirrored.Filled.CallMade,
                        "Missed" to Icons.AutoMirrored.Filled.CallMissed
                    )

                    quickTabs.forEach { (filter, icon) ->
                        val isSelected = selectedFilter == filter
                        val isMissedTab = filter == "Missed"
                        val unselectedChipBg = MaterialTheme.colorScheme.surfaceVariant
                        val selectedTextColor = if (isMissedTab) Color.White else contentOnGradient
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (isSelected && isMissedTab) {
                                        Modifier.background(MaterialTheme.colorScheme.error)
                                    } else if (isSelected) {
                                        Modifier.background(Brush.linearGradient(gradientColors))
                                    } else {
                                        Modifier.background(unselectedChipBg)
                                    }
                                )
                                .pressScaleEffect(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedFilter = filter
                                    },
                                    targetScale = 0.95f
                                )
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (icon != null) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) selectedTextColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = filter,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) selectedTextColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Call Logs List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CALL HISTORY (${filteredLogs.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (liveCallLogs.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .pressScaleEffect(
                                        onClick = { showClearAllDialog = true },
                                        targetScale = 0.94f
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Clear All",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Clear All",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                val emptyTitle = if (callSearchQuery.isNotBlank()) {
                                    "No calls found"
                                } else when (selectedFilter) {
                                    "Incoming" -> "No incoming calls"
                                    "Outgoing" -> "No outgoing calls yet"
                                    "Missed" -> "No missed calls"
                                    else -> "No call history yet"
                                }
                                val emptySubtitle = if (callSearchQuery.isNotBlank()) {
                                    "No call logs match \"$callSearchQuery\""
                                } else when (selectedFilter) {
                                    "Incoming" -> "Incoming calls from others will appear here"
                                    "Outgoing" -> "Calls you place will appear here"
                                    "Missed" -> "Missed calls will appear here"
                                    else -> "Outgoing and incoming voice calls will appear here"
                                }
                                Text(
                                    text = emptyTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = emptySubtitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                itemsIndexed(filteredLogs, key = { _, log -> "call_${log.id}" }) { idx, log ->
                    StaggeredReveal(index = idx) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Caller Initial Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (log.isMissed) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = log.initial,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (log.isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.callerName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (log.isMissed) Icons.AutoMirrored.Filled.CallMissed else if (log.isOutgoing) Icons.AutoMirrored.Filled.CallMade else Icons.AutoMirrored.Filled.CallReceived,
                                                contentDescription = null,
                                                tint = if (log.isMissed) MaterialTheme.colorScheme.error else OnlineGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            val statusText = if (log.isMissed) "Missed" else if (log.isOutgoing) "Outgoing" else "Incoming"
                                            Text(
                                                text = "$statusText • ${log.timeAgo}",
                                                fontSize = 12.sp,
                                                color = if (log.isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Delete Single Call Log Action
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                            .pressScaleEffect(
                                                onClick = {
                                                    logPendingDelete = log
                                                },
                                                targetScale = 0.88f
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Delete Call Log",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Initiate 1:1 Voice Call Action Button
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(gradientColors))
                                            .pressScaleEffect(
                                                onClick = {
                                                    initiateDirectCall(log.callerName)
                                                },
                                                targetScale = 0.88f
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Voice Call",
                                            tint = contentOnGradient,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog: Confirm Delete Single Call Log
        logPendingDelete?.let { log ->
            AlertDialog(
                onDismissRequest = { logPendingDelete = null },
                title = { Text("Delete Call Log", fontWeight = FontWeight.Bold) },
                text = { Text("Remove call record with ${log.callerName} from history?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val idToDelete = log.id
                            liveCallLogs.removeAll { it.id == idToDelete }
                            logPendingDelete = null
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                ConnectoApiClient.deleteCallLog(idToDelete)
                                refreshCallLogs()
                            }
                            showToast("Call record deleted")
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { logPendingDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog: Confirm Clear All History
        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                title = { Text("Clear Call History?", fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently remove all call logs from your history.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            liveCallLogs.clear()
                            showClearAllDialog = false
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                ConnectoApiClient.clearCallLogs(username = currentUsername)
                                refreshCallLogs()
                            }
                            showToast("Call history cleared")
                        }
                    ) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearAllDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Active call state handled globally by MainActivity FullScreenCallUI overlay
        LaunchedEffect(callState) {
            if (callState == CallState.IDLE && isInActiveCall) {
                isInActiveCall = false
                activeCallRoomCode = null
                refreshCallLogs()
            }
        }

        if (showAddFriendDialog) {
            AddFriendDialog(
                currentUsername = currentUsername,
                onDismissRequest = { showAddFriendDialog = false },
                onFriendAdded = { showAddFriendDialog = false }
            )
        }
    }
}
