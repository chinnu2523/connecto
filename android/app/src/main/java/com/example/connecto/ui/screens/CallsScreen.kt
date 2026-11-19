package com.example.connecto.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.mutableFloatStateOf
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.ui.focus.FocusRequester
import com.example.connecto.ui.components.ConnectoAnimatedSearchBar
import com.example.connecto.ui.components.SubstringHighlightedText
import com.example.connecto.ui.components.SearchEmptyState
import com.example.connecto.ui.designsystem.ConnectoTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.example.connecto.ui.designsystem.ConnectoSettingsSection
import com.example.connecto.ui.designsystem.ConnectoSettingsCard
import com.example.connecto.ui.designsystem.ConnectoSettingsDivider
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
import com.example.connecto.network.FriendshipDto
import com.example.connecto.ui.components.AddFriendDialog
import com.example.connecto.ui.components.ConnectoTopHeader
import com.example.connecto.ui.components.CreateVoiceGroupDialog
import com.example.connecto.ui.components.WebRtcCallOverlay
import org.json.JSONObject
import com.example.connecto.ui.components.FullScreenCallUI
import com.example.connecto.ui.theme.CyanGlow
import com.example.connecto.ui.theme.OnlineGreen
import com.example.connecto.ui.theme.StaggeredReveal
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.pressScaleEffect
import com.example.connecto.ui.theme.glassCanvasMesh
import com.example.connecto.ui.theme.glassCard
import com.example.connecto.ui.theme.liquidGlassCanvasMesh
import com.example.connecto.ui.theme.liquidGlassCard
import com.example.connecto.ui.designsystem.LocalConnectoColors
import com.example.connecto.voice.CallState
import com.example.connecto.voice.VoiceCallManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.connecto.network.CallLogDto
import com.example.connecto.ui.designsystem.resolveImageModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun parseCallLogTimestamp(raw: String?): Long {
    if (raw.isNullOrBlank() || raw.equals("Recent", ignoreCase = true) || raw.equals("Just now", ignoreCase = true)) {
        return System.currentTimeMillis()
    }
    val clean = raw.trim()
    val num = clean.toLongOrNull()
    if (num != null) {
        return if (num < 100_000_000_000L) num * 1000L else num
    }

    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss.SSSSSS",
        "yyyy-MM-dd HH:mm:ss.SSS",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (fmt in formats) {
        try {
            val sdf = java.text.SimpleDateFormat(fmt, java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val d = sdf.parse(clean)
            if (d != null) return d.time
        } catch (_: Exception) {}
    }
    return System.currentTimeMillis()
}

fun formatCallLogDateTime(rawCreatedAt: String?): String {
    val epochMs = parseCallLogTimestamp(rawCreatedAt)
    val callCal = java.util.Calendar.getInstance().apply { timeInMillis = epochMs }
    val nowCal = java.util.Calendar.getInstance()

    val timeFormatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    val timeStr = timeFormatter.format(callCal.time)

    val isSameDay = callCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
                    callCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)

    if (isSameDay) {
        return "Today, $timeStr"
    }

    val yesterdayCal = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.DAY_OF_YEAR, -1)
    }
    val isYesterday = callCal.get(java.util.Calendar.YEAR) == yesterdayCal.get(java.util.Calendar.YEAR) &&
                      callCal.get(java.util.Calendar.DAY_OF_YEAR) == yesterdayCal.get(java.util.Calendar.DAY_OF_YEAR)

    if (isYesterday) {
        return "Yesterday, $timeStr"
    }

    val isSameYear = callCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR)
    val dateFormatter = if (isSameYear) {
        java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
    } else {
        java.text.SimpleDateFormat("MMM d, yyyy, h:mm a", java.util.Locale.getDefault())
    }
    return dateFormatter.format(callCal.time)
}

fun formatCallDuration(seconds: Int): String {
    if (seconds <= 0) return ""
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> String.format(java.util.Locale.getDefault(), "%dh %02dm", hours, minutes)
        minutes > 0 -> String.format(java.util.Locale.getDefault(), "%dm %02ds", minutes, secs)
        else -> "${secs}s"
    }
}

@Immutable
data class CallLogItem(
    val id: String,
    val callerName: String,
    val initial: String,
    val callType: String,
    val timeAgo: String,
    val durationSeconds: Int = 0,
    val timestampMillis: Long = 0L,
    val isMissed: Boolean = false,
    val isOutgoing: Boolean = false,
    val friendUsername: String = "",
    val avatarUrl: String? = null
)

@Immutable
data class GroupedCallLog(
    val primaryLog: CallLogItem,
    val count: Int
)

fun groupConsecutiveCallLogs(logs: List<CallLogItem>): List<GroupedCallLog> {
    if (logs.isEmpty()) return emptyList()
    val grouped = mutableListOf<GroupedCallLog>()
    var current = logs[0]
    var count = 1
    for (i in 1 until logs.size) {
        val next = logs[i]
        if (next.callerName.equals(current.callerName, ignoreCase = true) &&
            next.timeAgo == current.timeAgo &&
            next.callType == current.callType
        ) {
            count++
        } else {
            grouped.add(GroupedCallLog(current, count))
            current = next
            count = 1
        }
    }
    grouped.add(GroupedCallLog(current, count))
    return grouped
}

fun getCallAvatarColor(name: String): Color {
    val palette = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFF14B8A6), // Teal
        Color(0xFFF59E0B), // Amber
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981)  // Emerald
    )
    val hash = kotlin.math.abs(name.hashCode())
    return palette[hash % palette.size]
}

private fun mapCallLogDtoToItem(
    log: CallLogDto,
    currentUsername: String,
    friends: List<FriendshipDto>
): CallLogItem {
    val rawTarget = if (log.callerName.equals(currentUsername, true) || log.callerName.isBlank()) {
        log.username
    } else {
        log.callerName
    }
    val cleanPartner = rawTarget
        .removePrefix("1:1 Voice Call with ")
        .removePrefix("Voice Call with ")
        .removePrefix("Call with ")
        .removePrefix("@")
        .trim()

    val matchedFriend = friends.find { f ->
        f.friendUsername.equals(cleanPartner, ignoreCase = true) ||
        f.friendDisplayName.equals(cleanPartner, ignoreCase = true) ||
        f.friendId.equals(cleanPartner, ignoreCase = true)
    }

    val friendUsername = matchedFriend?.friendUsername?.takeIf { it.isNotBlank() } ?: cleanPartner
    val displayName = matchedFriend?.friendDisplayName?.takeIf { it.isNotBlank() } ?: cleanPartner
    val safeDisplayName = if (displayName.equals(currentUsername, ignoreCase = true) && !cleanPartner.equals(currentUsername, ignoreCase = true)) {
        cleanPartner
    } else {
        displayName
    }

    val avatar = matchedFriend?.friendAvatarUrl?.takeIf { it.isNotBlank() }
        ?: log.callerAvatar?.takeIf { it.isNotBlank() }

    val safeAvatar = if (safeDisplayName.equals(currentUsername, ignoreCase = true) || friendUsername.equals(currentUsername, ignoreCase = true)) {
        null
    } else {
        avatar
    }

    val init = if (safeDisplayName.isNotBlank()) {
        val clean = safeDisplayName.trimStart { c -> !c.isLetterOrDigit() }
        if (clean.isNotEmpty()) clean.first().toString().uppercase() else "P"
    } else "P"

    val epochMs = parseCallLogTimestamp(log.createdAt)
    val formattedTime = formatCallLogDateTime(log.createdAt)

    return CallLogItem(
        id = log.id,
        callerName = safeDisplayName,
        initial = init,
        callType = log.callType,
        timeAgo = formattedTime,
        durationSeconds = log.durationSeconds,
        timestampMillis = epochMs,
        isMissed = log.isMissed,
        isOutgoing = log.isOutgoing,
        friendUsername = friendUsername,
        avatarUrl = safeAvatar
    )
}

@Composable
fun CallsScreen(
    currentUsername: String = "Member",
    onOpenProfile: () -> Unit = {},
    onOpenNotifications: (() -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    scrollToTopTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val callListState = androidx.compose.foundation.lazy.rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            try {
                if (callListState.firstVisibleItemIndex > 15) {
                    callListState.scrollToItem(15)
                }
                callListState.animateScrollToItem(0)
            } catch (_: Exception) {}
        }
    }

    var isInActiveCall by remember { mutableStateOf(false) }
    var activeCallTitle by remember { mutableStateOf("1:1 Voice Call") }
    var activeCallParticipants by remember { mutableIntStateOf(2) }
    var activeCallRoomCode by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Incoming", "Outgoing", "Missed"
    var callSearchQuery by remember { mutableStateOf("") }
    var isCallSearchFocused by remember { mutableStateOf(false) }
    val callSearchFocusRequester = remember { FocusRequester() }
    val isSearchActive = isCallSearchFocused || callSearchQuery.isNotEmpty()

    val callHeaderCollapseRatio by animateFloatAsState(
        targetValue = if (isSearchActive) 0f else 1f,
        animationSpec = tween(durationMillis = 220, easing = EaseOutCubic),
        label = "callHeaderCollapseRatio"
    )

    BackHandler(enabled = isCallSearchFocused || callSearchQuery.isNotEmpty()) {
        callSearchQuery = ""
        isCallSearchFocused = false
    }

    // Dialog state for clearing all or deleting
    var showClearAllDialog by remember { mutableStateOf(false) }
    var logPendingDelete by remember { mutableStateOf<CallLogItem?>(null) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showCreateVoiceGroupDialog by remember { mutableStateOf(false) }
    var pendingVoiceGroupTitle by remember { mutableStateOf<String?>(null) }
    var pendingVoiceGroupFriends by remember { mutableStateOf<List<FriendshipDto>>(emptyList()) }

    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }


    val liveCallLogs = remember {
        mutableStateListOf<CallLogItem>().apply {
            val cached = if (ConnectoApiClient.cachedCallLogs.isNotEmpty()) {
                ConnectoApiClient.cachedCallLogs
            } else {
                val diskLogs = ConnectoApiClient.loadCallLogsCache(context)
                if (diskLogs.isNotEmpty()) {
                    ConnectoApiClient.cachedCallLogs = diskLogs
                }
                diskLogs
            }
            if (cached.isNotEmpty()) {
                val cleanLogs = cached.filter { log ->
                    !log.callerName.contains("Tactical", ignoreCase = true) &&
                    !log.callerName.contains("War Room", ignoreCase = true) &&
                    !log.callerName.contains("Cyber Defense", ignoreCase = true) &&
                    !log.callerName.contains("Lounge", ignoreCase = true) &&
                    !log.callerName.startsWith("#")
                }
                addAll(cleanLogs.map { mapCallLogDtoToItem(it, currentUsername, ConnectoApiClient.cachedFriends) })
            }
        }
    }

    var isInitialLoading by remember { mutableStateOf(liveCallLogs.isEmpty()) }
    var isRefreshingCalls by remember { mutableStateOf(false) }

    fun refreshCallLogs() {
        coroutineScope.launch {
            try {
                // Fetch call logs and friends concurrently in parallel to eliminate waterfall latency
                val friendsDeferred = async {
                    if (ConnectoApiClient.cachedFriends.isNotEmpty()) {
                        ConnectoApiClient.cachedFriends
                    } else {
                        ConnectoApiClient.getFriends(username = currentUsername).getOrDefault(emptyList())
                    }
                }
                val logsDeferred = async {
                    ConnectoApiClient.getCallLogs(username = currentUsername)
                }
                val friends = friendsDeferred.await()
                val res = logsDeferred.await()
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
                        // Smart Deduplication: Merge multiple entries for same caller within 75 seconds
                        val dedupedLogs = mutableListOf<CallLogDto>()
                        for (log in cleanLogs) {
                            val logPartner = (if (log.callerName.equals(currentUsername, true) || log.callerName.isBlank()) log.username else log.callerName)
                                .removePrefix("1:1 Voice Call with ")
                                .removePrefix("Voice Call with ")
                                .removePrefix("Call with ")
                                .removePrefix("@")
                                .trim().lowercase()
                            val logEpoch = parseCallLogTimestamp(log.createdAt)
                            val existingIdx = dedupedLogs.indexOfFirst { existing ->
                                val existingPartner = (if (existing.callerName.equals(currentUsername, true) || existing.callerName.isBlank()) existing.username else existing.callerName)
                                    .removePrefix("1:1 Voice Call with ")
                                    .removePrefix("Voice Call with ")
                                    .removePrefix("Call with ")
                                    .removePrefix("@")
                                    .trim().lowercase()
                                val existingEpoch = parseCallLogTimestamp(existing.createdAt)
                                existingPartner == logPartner && Math.abs(existingEpoch - logEpoch) < 75_000L
                            }
                            if (existingIdx >= 0) {
                                val existing = dedupedLogs[existingIdx]
                                if (log.durationSeconds > existing.durationSeconds || (!log.isMissed && existing.isMissed && log.durationSeconds > 0)) {
                                    dedupedLogs[existingIdx] = log
                                }
                            } else {
                                dedupedLogs.add(log)
                            }
                        }

                        val mapped = dedupedLogs.map { mapCallLogDtoToItem(it, currentUsername, friends) }
                        if (liveCallLogs.toList() != mapped) {
                            val mappedIds = mapped.map { it.id }.toSet()
                            liveCallLogs.removeAll { it.id !in mappedIds }
                            mapped.forEachIndexed { index, newItem ->
                                val existingIndex = liveCallLogs.indexOfFirst { it.id == newItem.id }
                                if (existingIndex >= 0) {
                                    if (liveCallLogs[existingIndex] != newItem) {
                                        liveCallLogs[existingIndex] = newItem
                                    }
                                } else {
                                    if (index <= liveCallLogs.size) {
                                        liveCallLogs.add(index, newItem)
                                    } else {
                                        liveCallLogs.add(newItem)
                                    }
                                }
                            }
                        }
                    } else {
                        liveCallLogs.clear()
                    }
                }
            } catch (e: Exception) {
                // ignore
            } finally {
                isInitialLoading = false
            }
        }
    }

    fun performDirectCall(sanitized: String, displayName: String = sanitized, avatarUrl: String? = null) {
        val targetUser = sanitized.trim().removePrefix("@")
        val finalDisplayName = if (displayName.isNotBlank() && !displayName.equals(targetUser, ignoreCase = true)) {
            displayName
        } else {
            targetUser
        }
        activeCallTitle = "Voice Call with $finalDisplayName"
        activeCallParticipants = 2
        activeCallRoomCode = null
        isInActiveCall = true
        VoiceCallManager.start1on1Call(
            targetUsername = targetUser,
            targetDisplayName = finalDisplayName,
            avatarUrl = avatarUrl
        )
    }

    fun performVoiceGroupCall(groupTitle: String, selectedFriends: List<FriendshipDto>) {
        val currentU = ConnectoApiClient.currentUsername?.ifEmpty { currentUsername } ?: currentUsername
        val roomId = "group_voice_${currentU}_${System.currentTimeMillis()}"
        activeCallTitle = groupTitle.ifEmpty { "Voice Group Call" }
        activeCallParticipants = selectedFriends.size + 1
        activeCallRoomCode = roomId
        isInActiveCall = true

        VoiceCallManager.joinVoiceRoom(roomId = roomId, roomName = activeCallTitle, roomCode = roomId)

        coroutineScope.launch {
            for (friend in selectedFriends) {
                try {
                    VoiceCallManager.inviteToVoiceGroup(
                        roomId = roomId,
                        groupTitle = activeCallTitle,
                        targetUser = friend.friendUsername
                    )
                    ConnectoApiClient.inviteToVoiceCall(friend.friendUsername, roomId)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    var pendingCallTarget by remember { mutableStateOf<String?>(null) }
    var pendingCallDisplayName by remember { mutableStateOf<String?>(null) }
    var pendingCallAvatarUrl by remember { mutableStateOf<String?>(null) }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            VoiceCallManager.restartAudioHardwareIfConnected()
            pendingCallTarget?.let { target ->
                performDirectCall(
                    sanitized = target,
                    displayName = pendingCallDisplayName ?: target,
                    avatarUrl = pendingCallAvatarUrl
                )
            }
            pendingVoiceGroupTitle?.let { title ->
                performVoiceGroupCall(title, pendingVoiceGroupFriends)
            }
        } else {
            showToast("Microphone permission is required for voice calls")
        }
        pendingCallTarget = null
        pendingCallDisplayName = null
        pendingCallAvatarUrl = null
        pendingVoiceGroupTitle = null
        pendingVoiceGroupFriends = emptyList()
    }

    fun initiateVoiceGroupCall(groupTitle: String, selectedFriends: List<FriendshipDto>) {
        if (selectedFriends.isEmpty()) {
            showToast("Please select at least 1 friend to call")
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            performVoiceGroupCall(groupTitle, selectedFriends)
        } else {
            pendingVoiceGroupTitle = groupTitle
            pendingVoiceGroupFriends = selectedFriends
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun initiateDirectCall(target: String, displayName: String = target, avatarUrl: String? = null) {
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

        val resolvedAvatar = avatarUrl ?: ConnectoApiClient.cachedFriends.find {
            it.friendUsername.equals(sanitized, ignoreCase = true) ||
            it.friendDisplayName.equals(sanitized, ignoreCase = true)
        }?.friendAvatarUrl

        val resolvedName = if (displayName.isNotBlank() && !displayName.equals(sanitized, ignoreCase = true)) {
            displayName
        } else {
            ConnectoApiClient.cachedFriends.find {
                it.friendUsername.equals(sanitized, ignoreCase = true)
            }?.friendDisplayName ?: sanitized
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            performDirectCall(sanitized, resolvedName, resolvedAvatar)
        } else {
            pendingCallTarget = sanitized
            pendingCallDisplayName = resolvedName
            pendingCallAvatarUrl = resolvedAvatar
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val callState by VoiceCallManager.callState.collectAsState()

    LaunchedEffect(Unit) {
        refreshCallLogs()
        while (isActive) {
            delay(12000L)
            // Never re-poll and mutate call history while the user is actively calling
            if (VoiceCallManager.callState.value == CallState.IDLE) {
                refreshCallLogs()
            }
        }
    }

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

    val groupedLogs = remember(filteredLogs) { groupConsecutiveCallLogs(filteredLogs) }

    val colors = LocalConnectoColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (colors.isLiquidGlass) Modifier.liquidGlassCanvasMesh()
                else if (colors.isGlass) Modifier.glassCanvasMesh()
                else Modifier.background(if (colors.isDark) Color(0xFF000000) else MaterialTheme.colorScheme.background)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            val isCallSearchActive = isCallSearchFocused || callSearchQuery.isNotEmpty()
            val callSearchTopPadding by animateDpAsState(
                targetValue = if (isCallSearchActive) 10.dp else 6.dp,
                animationSpec = tween(durationMillis = 220, easing = EaseOutCubic),
                label = "callSearchTopPad"
            )

            // Collapsible Top Header: Animates upward (opacity: 1 -> 0, height -> 0, 220ms ease-out)
            if (callHeaderCollapseRatio > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = callHeaderCollapseRatio
                            translationY = -35f * (1f - callHeaderCollapseRatio)
                        }
                        .then(
                            if (callHeaderCollapseRatio < 0.99f) Modifier.height((64.dp * callHeaderCollapseRatio))
                            else Modifier
                        )
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                try {
                                    if (callListState.firstVisibleItemIndex > 15) {
                                        callListState.scrollToItem(15)
                                    }
                                    callListState.animateScrollToItem(0)
                                } catch (_: Exception) {}
                            }
                        }
                ) {
                    ConnectoTopHeader(
                        title = "CALLS",
                        subtitle = "Call History & Voice Hub",
                        userInitial = userInitial,
                        onAddFriendClick = null,
                        onVoiceGroupClick = null,
                        showProfileButton = false,
                        onAvatarClick = {},
                        onNotificationClick = null,
                        unreadNotificationsCount = 0,
                        customAction = null,
                        applyStatusBarsPadding = false
                    )
                }
            }

            // Call History Animated Search Bar with sliding cancel button, active glow border & haptics
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = callSearchTopPadding, bottom = 6.dp)
            ) {
                ConnectoAnimatedSearchBar(
                    query = callSearchQuery,
                    onQueryChange = { callSearchQuery = it },
                    placeholder = "Search call history by name or date...",
                    isFocused = isCallSearchFocused,
                    onFocusChanged = { isCallSearchFocused = it },
                    onCancel = {
                        callSearchQuery = ""
                        isCallSearchFocused = false
                    },
                    focusRequester = callSearchFocusRequester,
                    isDark = colors.isDark
                )
            }

            // Quick Tab Filter Bar (All, Incoming, Outgoing, Missed) - Hidden during active search
            if (!isCallSearchActive) {
                val filterShape = RoundedCornerShape(12.dp)
            val trackBg = if (colors.isDark) Color(0xFF0D0E12) else Color(0xFFF1F5F9)
            val activeTabBg = if (colors.isDark) Color(0xFF1E222D) else Color(0xFF0F172A)
            val quickTabs = listOf(
                "All" to null,
                "Incoming" to Icons.AutoMirrored.Filled.CallReceived,
                "Outgoing" to Icons.AutoMirrored.Filled.CallMade,
                "Missed" to Icons.AutoMirrored.Filled.CallMissed
            )
            val selectedIndex = quickTabs.indexOfFirst { it.first == selectedFilter }.coerceAtLeast(0)

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(filterShape)
                    .background(trackBg)
                    .then(if (colors.isDark) Modifier.border(1.dp, Color(0x18FFFFFF), filterShape) else Modifier)
                    .padding(4.dp)
            ) {
                val totalWidth = maxWidth
                val tabCount = quickTabs.size
                val tabWidth = totalWidth / tabCount

                val animatedPillOffset by animateDpAsState(
                    targetValue = tabWidth * selectedIndex,
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = 380f
                    ),
                    label = "callsSlidingPill"
                )

                val isMissedSelected = selectedFilter == "Missed"
                val slidingPillBg = if (isMissedSelected) MaterialTheme.colorScheme.error else activeTabBg

                // Sliding active pill behind text (smooth 250ms spring curve)
                Box(
                    modifier = Modifier
                        .offset(x = animatedPillOffset)
                        .width(tabWidth)
                        .height(34.dp)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(slidingPillBg)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    quickTabs.forEach { (filter, icon) ->
                        val isSelected = selectedFilter == filter
                        val selectedTextColor = Color.White
                        val unselectedTextColor = if (colors.isDark) Color(0xFF7A7F8F) else Color(0xFF64748B)

                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) selectedTextColor else unselectedTextColor,
                            animationSpec = tween(200),
                            label = "callTabTextColor"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedFilter = filter
                                }
                                .padding(vertical = 4.dp),
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
                                        tint = textColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = filter,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
            }

            // Call Logs List
            com.example.connecto.ui.components.ConnectoDragToRefresh(
                isRefreshing = isRefreshingCalls,
                onRefresh = {
                    isRefreshingCalls = true
                    coroutineScope.launch {
                        try {
                            kotlinx.coroutines.withTimeoutOrNull(6000L) {
                                refreshCallLogs()
                            }
                        } catch (_: Exception) {
                        } finally {
                            isRefreshingCalls = false
                        }
                    }
                },
                refreshText = "Syncing call history...",
                canScrollBackward = {
                    callListState.canScrollBackward ||
                    callListState.firstVisibleItemIndex > 0 ||
                    callListState.firstVisibleItemScrollOffset > 0
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
            LazyColumn(
                state = callListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
            ) {
                if (!isCallSearchActive) {
                    item(key = "calls_header_row") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 8.dp, bottom = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CALL HISTORY (${filteredLogs.size})",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = if (colors.isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                            if (liveCallLogs.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .pressScaleEffect(
                                            onClick = { showClearAllDialog = true },
                                            targetScale = 0.94f
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
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
                }

                if (filteredLogs.isEmpty()) {
                    item(key = "calls_empty_state") {
                        val showCallsSkeleton = isInitialLoading && liveCallLogs.isEmpty()
                        androidx.compose.animation.Crossfade(
                            targetState = showCallsSkeleton,
                            animationSpec = tween(durationMillis = 160),
                            label = "callsEmptyStateCrossfade"
                        ) { isSkeleton ->
                            if (isSkeleton) {
                                com.example.connecto.ui.theme.ShimmerCallLogsList(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 8.dp),
                                    count = 6,
                                    isDark = colors.isDark
                                )
                            } else if (callSearchQuery.isNotBlank()) {
                                SearchEmptyState(query = callSearchQuery, isDark = colors.isDark)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val emptyPulseTransition = rememberInfiniteTransition(label = "emptyPhonePulse")
                                    val emptyPhoneScale by emptyPulseTransition.animateFloat(
                                        initialValue = 1.0f,
                                        targetValue = 1.06f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(2000, easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "pulseScale"
                                    )
                                    var emptyEntranceVisible by remember { mutableStateOf(false) }
                                    LaunchedEffect(Unit) {
                                        emptyEntranceVisible = true
                                    }
                                    val emptyAlpha by animateFloatAsState(
                                        targetValue = if (emptyEntranceVisible) 1f else 0f,
                                        animationSpec = tween(400, easing = FastOutSlowInEasing),
                                        label = "emptyFadeIn"
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.graphicsLayer { alpha = emptyAlpha }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .graphicsLayer {
                                                    scaleX = emptyPhoneScale
                                                    scaleY = emptyPhoneScale
                                                }
                                                .clip(CircleShape)
                                                .background(if (colors.isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = null,
                                                tint = colors.textSecondary,
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
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = emptySubtitle,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item(key = "calls_grouped_card") {
                        ConnectoSettingsCard {
                            groupedLogs.forEachIndexed { idx, grouped ->
                                StaggeredReveal(index = idx, staggerMs = 25L, initialOffsetY = 8f) {
                                val log = grouped.primaryLog
                                val displayName = if (grouped.count > 1) "${log.callerName} (${grouped.count})" else log.callerName
                                var swipeOffsetX by remember(log.id) { mutableFloatStateOf(0f) }
                                val animatedSwipeOffset by animateFloatAsState(
                                    targetValue = swipeOffsetX,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                                    label = "callRowSwipe"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pointerInput(log.id) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    if (swipeOffsetX < -80f) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        logPendingDelete = log
                                                    }
                                                    swipeOffsetX = 0f
                                                },
                                                onDragCancel = {
                                                    swipeOffsetX = 0f
                                                },
                                                onHorizontalDrag = { _, dragAmount ->
                                                    if (dragAmount < 0 || swipeOffsetX < 0) {
                                                        swipeOffsetX = (swipeOffsetX + dragAmount).coerceIn(-120f, 0f)
                                                    }
                                                }
                                            )
                                        }
                                ) {
                                    if (animatedSwipeOffset < -10f) {
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                                .padding(end = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset { IntOffset(animatedSwipeOffset.roundToInt(), 0) }
                                            .background(if (colors.isDark) Color(0xFF0A0B10) else Color.White)
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Caller Initial or Profile Avatar with guaranteed bold initial fallback
                                            val hasRemotePhoto = !log.avatarUrl.isNullOrBlank() &&
                                                log.avatarUrl != "null" &&
                                                log.avatarUrl != "default" &&
                                                (log.avatarUrl.startsWith("http://") || log.avatarUrl.startsWith("https://") || log.avatarUrl.startsWith("content://"))

                                            val cleanInitial = log.initial.ifBlank {
                                                log.callerName.trimStart { !it.isLetterOrDigit() }.take(1).uppercase().ifBlank { "C" }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (log.isMissed) colors.error.copy(alpha = 0.15f)
                                                        else getCallAvatarColor(log.callerName)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = cleanInitial,
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (log.isMissed) colors.error else Color.White
                                                )
                                                if (hasRemotePhoto) {
                                                    val imgModel = resolveImageModel(log.avatarUrl)
                                                    AsyncImage(
                                                        model = imgModel,
                                                        contentDescription = log.callerName,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                if (callSearchQuery.isNotBlank() && displayName.contains(callSearchQuery, ignoreCase = true)) {
                                                    SubstringHighlightedText(
                                                        text = displayName,
                                                        query = callSearchQuery,
                                                        style = TextStyle(
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = colors.textPrimary
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                } else {
                                                    Text(
                                                        text = displayName,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = colors.textPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    val isCancelled = log.isOutgoing && log.durationSeconds == 0 && !log.isMissed
                                                    Icon(
                                                        imageVector = if (log.isMissed) Icons.AutoMirrored.Filled.CallMissed else if (log.isOutgoing) Icons.AutoMirrored.Filled.CallMade else Icons.AutoMirrored.Filled.CallReceived,
                                                        contentDescription = null,
                                                        tint = if (log.isMissed) colors.error else if (isCancelled) colors.textDisabled else OnlineGreen,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    val statusText = when {
                                                        log.isMissed -> "Missed"
                                                        log.isOutgoing && log.durationSeconds > 0 -> "Outgoing"
                                                        log.isOutgoing -> "Cancelled"
                                                        else -> "Incoming"
                                                    }
                                                    val durStr = formatCallDuration(log.durationSeconds)
                                                    val displaySubtext = if (durStr.isNotBlank()) {
                                                        "$statusText • ${log.timeAgo} • $durStr"
                                                    } else {
                                                        "$statusText • ${log.timeAgo}"
                                                    }
                                                    if (callSearchQuery.isNotBlank() && displaySubtext.contains(callSearchQuery, ignoreCase = true)) {
                                                        SubstringHighlightedText(
                                                            text = displaySubtext,
                                                            query = callSearchQuery,
                                                            style = TextStyle(
                                                                fontSize = 12.sp,
                                                                color = if (log.isMissed) colors.error else colors.textSecondary
                                                            ),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    } else {
                                                        Text(
                                                            text = displaySubtext,
                                                            fontSize = 12.sp,
                                                            color = if (log.isMissed) colors.error else colors.textSecondary,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Single clean phone icon button on the right (#161822 circular fill in dark mode)
                                        val phoneButtonBg = if (colors.isDark) Color(0xFF161822) else Color(0xFFF1F5F9)
                                        val phoneIconTint = if (colors.isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(phoneButtonBg)
                                                .pressScaleEffect(
                                                    onClick = {
                                                        val dialTarget = if (log.friendUsername.isNotBlank()) log.friendUsername else log.callerName
                                                        initiateDirectCall(
                                                            target = dialTarget,
                                                            displayName = log.callerName,
                                                            avatarUrl = log.avatarUrl
                                                        )
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "Voice Call",
                                                tint = phoneIconTint,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                    if (idx < groupedLogs.size - 1) {
                                        ConnectoSettingsDivider(startIndent = 74.dp)
                                    }
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
            if (callState == CallState.IDLE) {
                if (isInActiveCall) {
                    isInActiveCall = false
                    activeCallRoomCode = null
                }
                delay(400L)
                refreshCallLogs()
            }
        }

        // Floating Action Buttons for Guest Rooms & Group Voice Calls
        if (!isSearchActive) {
            Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Option 1: Ephemeral Zero-Install Guest Room (Indigo #6366F1)
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    coroutineScope.launch {
                        try {
                            val res = com.example.connecto.network.ConnectoApiClient.createGuestRoom()
                            if (res.isSuccess) {
                                val room = res.getOrThrow()
                                val shareUrl = room.roomUrl.ifBlank { "https://connecto.fun/room/${room.roomCode}" }
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Connecto Guest Room", shareUrl)
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "Guest Room created! Link copied: $shareUrl", Toast.LENGTH_SHORT).show()

                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Join my Connecto Guest Room")
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Join my instant zero-install room on Connecto: $shareUrl")
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Guest Room Link"))
                            } else {
                                Toast.makeText(context, "Could not create guest room. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.MeetingRoom,
                        contentDescription = "Create Guest Room",
                        tint = Color.White
                    )
                },
                text = {
                    Text(
                        text = "Guest Room",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                },
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )

            // Option 2: Group Voice Call (Emerald #10B981)
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showCreateVoiceGroupDialog = true
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = "New Group Call",
                        tint = Color.White
                    )
                },
                text = {
                    Text(
                        text = "Group Voice Call",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color.White
                    )
                },
                containerColor = Color(0xFF10B981),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
        }

        if (showAddFriendDialog) {
            AddFriendDialog(
                currentUsername = currentUsername,
                onDismissRequest = { showAddFriendDialog = false },
                onFriendAdded = { showAddFriendDialog = false }
            )
        }

        if (showCreateVoiceGroupDialog) {
            CreateVoiceGroupDialog(
                currentUsername = currentUsername,
                onDismissRequest = { showCreateVoiceGroupDialog = false },
                onStartVoiceGroup = { title, friends ->
                    showCreateVoiceGroupDialog = false
                    initiateVoiceGroupCall(title, friends)
                }
            )
        }
    }
}
