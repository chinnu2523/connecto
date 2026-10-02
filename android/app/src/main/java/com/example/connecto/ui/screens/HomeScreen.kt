package com.example.connecto.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.example.connecto.ui.theme.isAppInLightTheme
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import com.example.connecto.voice.VoiceCallManager
import com.example.connecto.voice.CallState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.ui.theme.CyanGlow
import com.example.connecto.ui.theme.DarkCardSurface
import com.example.connecto.ui.theme.DeepSpaceBackground
import com.example.connecto.ui.theme.ElectricViolet
import com.example.connecto.ui.theme.NeonPink
import com.example.connecto.ui.theme.TextPrimaryDark
import com.example.connecto.ui.theme.TextSecondaryDark
import com.example.connecto.ui.theme.glassmorphicCard
import com.example.connecto.ui.theme.pressScaleEffect
import com.example.connecto.ui.theme.PulsingOnlineDot
import com.example.connecto.ui.theme.StaggeredReveal
import com.example.connecto.ui.theme.TypingIndicatorDots

data class GameChannelItem(
    val id: String,
    val name: String,
    val description: String,
    val isReadOnly: Boolean = false
)

data class GameFriend(
    val id: String,
    val name: String,
    val username: String,
    val isOnline: Boolean,
    val isPending: Boolean = false
)

data class ChannelMessage(
    val id: Int,
    val senderName: String,
    val initial: String,
    val content: String,
    val time: String,
    val isMe: Boolean
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToRadar: () -> Unit = {}
) {
    var selectedChannelId by remember { mutableStateOf("general") }
    var selectedNavSection by remember { mutableStateOf("channels") } // "channels" or "friends"

    // Group Voice Call Creation State ("Gamers Call" as suggested default name)
    var showGroupCallModal by remember { mutableStateOf(false) }
    var groupCallTitleInput by remember { mutableStateOf("Gamers Call") }
    val selectedFriendIds = remember { mutableStateListOf<String>() }

    // Active Call State
    val managerCallState by VoiceCallManager.callState.collectAsState()
    val managerCallTitle by VoiceCallManager.activeCallTitle.collectAsState()
    val managerIsMuted by VoiceCallManager.isMuted.collectAsState()
    var isInActiveCall by remember { mutableStateOf(false) }
    var activeCallTitle by remember { mutableStateOf("") }
    var activeParticipantCount by remember { mutableStateOf(2) }

    val context = LocalContext.current
    var pendingFriendCall by remember { mutableStateOf<Pair<String, String>?>(null) }
    var pendingGroupCall by remember { mutableStateOf<String?>(null) }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            VoiceCallManager.restartAudioHardwareIfConnected()
            pendingFriendCall?.let { (user, name) ->
                isInActiveCall = true
                activeCallTitle = "1:1 Call with $name"
                activeParticipantCount = 2
                VoiceCallManager.start1on1Call(user, name)
            }
            pendingGroupCall?.let { title ->
                isInActiveCall = true
                activeCallTitle = title
                activeParticipantCount = selectedFriendIds.size + 1
                VoiceCallManager.joinVoiceRoom("group_${System.currentTimeMillis()}", title)
            }
        } else {
            Toast.makeText(context, "Microphone permission is required for voice calls", Toast.LENGTH_SHORT).show()
        }
        pendingFriendCall = null
        pendingGroupCall = null
    }

    // Friends List
    var friendUsernameInput by remember { mutableStateOf("") }
    val friendsList = remember {
        mutableStateListOf(
            GameFriend("1", "Elena Vance", "elena_v", true),
            GameFriend("2", "Marcus Chen", "marcus_c", true),
            GameFriend("3", "Zack Miller", "zack_m", true),
            GameFriend("4", "Sarah Connor", "sarah_c", false, isPending = true)
        )
    }

    // Official Channels List
    val channelsList = remember {
        listOf(
            GameChannelItem("general", "general", "Global public chat - talk with everyone globally"),
            GameChannelItem("announcements", "announcements", "App updates & feature details", isReadOnly = true),
            GameChannelItem("free-fire-tournaments", "free-fire-tournaments", "Free Fire gaming tournaments"),
            GameChannelItem("esports-lounge", "esports-lounge", "Community gaming lounge")
        )
    }

    var typedMessage by remember { mutableStateOf("") }

    // Separate Message feeds per channel
    val generalMessages = remember {
        mutableStateListOf(
            ChannelMessage(1, "Elena Vance", "E", "Welcome to #general global chat! Anyone up for Free Fire?", "10:12 AM", false),
            ChannelMessage(2, "Marcus Chen", "M", "Hey everyone! Glad to connect on Connecto.", "10:14 AM", false)
        )
    }

    val announcementMessages = remember {
        mutableStateListOf(
            ChannelMessage(1, "Connecto Team", "C", "🚀 Connecto v2.4 Release: WebRTC Voice Calls, Argon2id Vault, and AI CV Screening are now live!", "09:00 AM", false),
            ChannelMessage(2, "Connecto Team", "C", "📢 Announcement: Create group calls with 2+ friends (suggested default: Gamers Call)!", "09:30 AM", false)
        )
    }

    val activeMessages = when (selectedChannelId) {
        "announcements" -> announcementMessages
        else -> generalMessages
    }

    val activeChannel = channelsList.find { it.id == selectedChannelId } ?: channelsList[0]

    // Auto-scroll chat feed to bottom when new message arrives
    val chatListState = rememberLazyListState()
    LaunchedEffect(activeMessages.size) {
        if (activeMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(activeMessages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
            .statusBarsPadding()
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // CLEAN TOP HEADER BAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(shape = RoundedCornerShape(24.dp))
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1: Brand Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(ElectricViolet, CyanGlow))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "CONNECTO GAMERS HUB",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimaryDark,
                                letterSpacing = 0.5.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Radio,
                                    contentDescription = null,
                                    tint = CyanGlow,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Live Global Community",
                                    fontSize = 11.sp,
                                    color = CyanGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Row 2: Full-Width Segmented Tab Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardSurface)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .pressScaleEffect(onClick = { selectedNavSection = "channels" })
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedNavSection == "channels") Brush.horizontalGradient(listOf(ElectricViolet, CyanGlow))
                                    else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Channels",
                                color = if (selectedNavSection == "channels") Color.White else TextSecondaryDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .pressScaleEffect(onClick = { selectedNavSection = "friends" })
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selectedNavSection == "friends") Brush.horizontalGradient(listOf(ElectricViolet, CyanGlow))
                                    else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Friends & Group Calls",
                                color = if (selectedNavSection == "friends") Color.White else TextSecondaryDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // MAIN CONTENT BODY
            if (selectedNavSection == "channels") {
                Column(modifier = Modifier.fillMaxSize()) {
                    // CHANNEL REEL (#general, #announcements, #free-fire-tournaments, #esports-lounge)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(channelsList) { index, ch ->
                            val isSelected = selectedChannelId == ch.id
                            StaggeredReveal(index = index, staggerMs = 40L) {
                            Box(
                                modifier = Modifier
                                    .pressScaleEffect(onClick = { selectedChannelId = ch.id })
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) Brush.horizontalGradient(listOf(ElectricViolet, CyanGlow))
                                        else Brush.horizontalGradient(listOf(DarkCardSurface, DarkCardSurface))
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (ch.isReadOnly) {
                                        Icon(
                                            imageVector = Icons.Default.Campaign,
                                            contentDescription = null,
                                            tint = NeonPink,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "#",
                                            color = CyanGlow,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = ch.name,
                                        color = if (isSelected) Color.White else TextSecondaryDark,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Channel Description Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardSurface.copy(alpha = 0.6f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "📍 #${activeChannel.name}: ${activeChannel.description}",
                            fontSize = 11.sp,
                            color = CyanGlow,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ACTIVE VOICE CALL BAR (IF IN CALL)
                    val isCallActive = (managerCallState != CallState.IDLE) || isInActiveCall
                    AnimatedVisibility(visible = isCallActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphicCard(shape = RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Headset,
                                        contentDescription = null,
                                        tint = CyanGlow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (managerCallTitle.isNotBlank()) managerCallTitle else activeCallTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimaryDark
                                        )
                                        Text(
                                            text = if (managerCallState == CallState.CONNECTED) "Voice Call Connected • Live Audio" else "Voice Call Active • $activeParticipantCount participants",
                                            fontSize = 11.sp,
                                            color = CyanGlow
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { VoiceCallManager.toggleMute() },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (managerIsMuted) Color.Red.copy(alpha = 0.2f) else DarkCardSurface)
                                    ) {
                                        Icon(
                                            imageVector = if (managerIsMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                            contentDescription = "Mute",
                                            tint = if (managerIsMuted) Color.Red else CyanGlow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .pressScaleEffect(onClick = {
                                                isInActiveCall = false
                                                VoiceCallManager.leaveVoiceRoom()
                                                VoiceCallManager.endCall()
                                            })
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CallEnd,
                                                contentDescription = "Leave",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "LEAVE",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CHAT FEED AREA
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(activeMessages) { index, msg ->
                            StaggeredReveal(index = index) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (msg.isMe) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Row(
                                    horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start,
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    if (!msg.isMe) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(ElectricViolet, NeonPink))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = msg.initial,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Column(
                                        horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start
                                    ) {
                                        if (!msg.isMe) {
                                            Text(
                                                text = "${msg.senderName} • ${msg.time}",
                                                fontSize = 11.sp,
                                                color = TextSecondaryDark,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 18.dp,
                                                        topEnd = 18.dp,
                                                        bottomStart = if (msg.isMe) 18.dp else 4.dp,
                                                        bottomEnd = if (msg.isMe) 4.dp else 18.dp
                                                    )
                                                )
                                                .background(
                                                    if (msg.isMe) Brush.horizontalGradient(listOf(ElectricViolet, NeonPink))
                                                    else Brush.horizontalGradient(listOf(DarkCardSurface, DarkCardSurface))
                                                )
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = msg.content,
                                                color = TextPrimaryDark,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                            }
                        }

                        // Animated typing indicator (if not announcements)
                        if (!activeChannel.isReadOnly) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(DarkCardSurface)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Forum,
                                            contentDescription = null,
                                            tint = CyanGlow,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        androidx.compose.material3.Text(
                                            text = "Elena Vance ",
                                            color = TextSecondaryDark,
                                            fontSize = 11.sp
                                        )
                                        TypingIndicatorDots(color = CyanGlow)
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(70.dp)) }
                    }

                    // GLASSMORPHIC INPUT BAR
                    val sessionPrefs = remember { context.getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE) }
                    val loggedInUsername = remember { sessionPrefs.getString("username", "user") ?: "user" }
                    val isUserAdmin = loggedInUsername.equals("connecto_admin", ignoreCase = true) ||
                                      loggedInUsername.equals("admin", ignoreCase = true) ||
                                      loggedInUsername.equals("vance", ignoreCase = true) ||
                                      sessionPrefs.getBoolean("is_admin", false)
                    val canPostInChannel = !activeChannel.isReadOnly || isUserAdmin
                    if (canPostInChannel) {
                        val sendButtonScale by animateFloatAsState(
                            targetValue = if (typedMessage.isNotBlank()) 1f else 0.82f,
                            animationSpec = tween(200),
                            label = "sendButtonScale"
                        )
                        val sendButtonAlpha by animateFloatAsState(
                            targetValue = if (typedMessage.isNotBlank()) 1f else 0.45f,
                            animationSpec = tween(200),
                            label = "sendButtonAlpha"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphicCard(shape = RoundedCornerShape(20.dp))
                                .padding(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = typedMessage,
                                    onValueChange = { typedMessage = it },
                                    placeholder = { Text("Message #${activeChannel.name}...", color = TextSecondaryDark, fontSize = 13.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = TextPrimaryDark,
                                        unfocusedTextColor = TextPrimaryDark
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .graphicsLayer {
                                            scaleX = sendButtonScale
                                            scaleY = sendButtonScale
                                            alpha = sendButtonAlpha
                                        }
                                        .pressScaleEffect(
                                            onClick = {
                                                if (typedMessage.isNotBlank()) {
                                                    activeMessages.add(
                                                        ChannelMessage(
                                                            id = activeMessages.size + 1,
                                                            senderName = "You",
                                                            initial = "Y",
                                                            content = typedMessage,
                                                            time = "Just now",
                                                            isMe = true
                                                        )
                                                    )
                                                    typedMessage = ""
                                                }
                                            }
                                        )
                                        .clip(CircleShape)
                                        .background(Brush.horizontalGradient(listOf(ElectricViolet, CyanGlow))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Read-only Announcement Channel Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphicCard(shape = RoundedCornerShape(20.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔒 Only administrators can post in #announcements",
                                fontSize = 12.sp,
                                color = if (isAppInLightTheme()) Color(0xFF78716C) else TextSecondaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // FRIENDS LIST, 1:1 CALLS & GROUP CALL CREATION VIEW
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Create Group Call Banner Button
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pressScaleEffect(onClick = { showGroupCallModal = true })
                                .glassmorphicCard(
                                    shape = RoundedCornerShape(24.dp),
                                    borderColor = NeonPink
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(NeonPink, ElectricViolet))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GroupAdd,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "Create Group Voice Call",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = TextPrimaryDark
                                        )
                                        Text(
                                            text = "Call 2+ friends together • Suggested: Gamers Call",
                                            fontSize = 11.sp,
                                            color = NeonPink
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Group Call",
                                    tint = NeonPink,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Add Friend Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphicCard(shape = RoundedCornerShape(24.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "ADD FRIEND & CONNECTION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanGlow,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = friendUsernameInput,
                                        onValueChange = { friendUsernameInput = it },
                                        placeholder = { Text("Enter username (e.g. zack_m)...", color = TextSecondaryDark, fontSize = 12.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanGlow,
                                            unfocusedBorderColor = Color(0x40A29BFE),
                                            focusedTextColor = TextPrimaryDark,
                                            unfocusedTextColor = TextPrimaryDark
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .pressScaleEffect(
                                                onClick = {
                                                    if (friendUsernameInput.isNotBlank()) {
                                                        friendsList.add(
                                                            GameFriend(
                                                                (friendsList.size + 1).toString(),
                                                                friendUsernameInput,
                                                                friendUsernameInput,
                                                                isOnline = true,
                                                                isPending = true
                                                            )
                                                        )
                                                        friendUsernameInput = ""
                                                    }
                                                }
                                            )
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Brush.horizontalGradient(listOf(ElectricViolet, CyanGlow)))
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = "Add Friend",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "FRIENDS LIST & 1:1 CALLS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryDark,
                            letterSpacing = 1.sp
                        )
                    }

                    itemsIndexed(friendsList) { index, friend ->
                        StaggeredReveal(index = index, staggerMs = 60L) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphicCard(shape = RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(ElectricViolet, NeonPink))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = friend.name.first().toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                        if (friend.isOnline && !friend.isPending) {
                                            PulsingOnlineDot(
                                                color = CyanGlow,
                                                size = 10.dp,
                                                modifier = Modifier.padding(1.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = friend.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimaryDark
                                        )
                                        Text(
                                            text = if (friend.isPending) "Pending Request" else if (friend.isOnline) "Online" else "Offline",
                                            fontSize = 11.sp,
                                            color = if (friend.isPending) NeonPink else if (friend.isOnline) CyanGlow else TextSecondaryDark
                                        )
                                    }
                                }

                                if (!friend.isPending) {
                                    Box(
                                        modifier = Modifier
                                            .pressScaleEffect(
                                                onClick = {
                                                    val friendUser = friend.username.removePrefix("@").ifBlank { friend.name }
                                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                        isInActiveCall = true
                                                        activeCallTitle = "1:1 Call with ${friend.name}"
                                                        activeParticipantCount = 2
                                                        VoiceCallManager.start1on1Call(friendUser, friend.name)
                                                    } else {
                                                        pendingFriendCall = Pair(friendUser, friend.name)
                                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }
                                                }
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Brush.horizontalGradient(listOf(ElectricViolet, CyanGlow)))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "Voice Call",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "CALL",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(70.dp)) }
                }
            }
        }
    }

    // CREATE GROUP CALL MODAL (SAVES DEFAULT SUGGESTED NAME: "Gamers Call")
    if (showGroupCallModal) {
        AlertDialog(
            onDismissRequest = { showGroupCallModal = false },
            title = {
                Text(
                    text = "Create Group Voice Call",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Group calls require selecting 2 or more online friends (3+ participants).",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )

                    OutlinedTextField(
                        value = groupCallTitleInput,
                        onValueChange = { groupCallTitleInput = it },
                        label = { Text("Group Call Name (Suggested: Gamers Call)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = Color(0x40A29BFE),
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Select Online Friends to Call:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanGlow
                    )

                    friendsList.filter { it.isOnline && !it.isPending }.forEach { friend ->
                        val isSelected = selectedFriendIds.contains(friend.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedFriendIds.remove(friend.id)
                                    else selectedFriendIds.add(friend.id)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedFriendIds.add(friend.id)
                                    else selectedFriendIds.remove(friend.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CyanGlow,
                                    uncheckedColor = TextSecondaryDark
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = friend.name, color = TextPrimaryDark, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val callName = if (groupCallTitleInput.isNotBlank()) groupCallTitleInput.trim() else "Gamers Call"
                        showGroupCallModal = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            isInActiveCall = true
                            activeCallTitle = callName
                            activeParticipantCount = selectedFriendIds.size + 1
                            VoiceCallManager.joinVoiceRoom("group_${System.currentTimeMillis()}", callName)
                        } else {
                            pendingGroupCall = callName
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    enabled = selectedFriendIds.size >= 2 // Requires 2+ friends selected
                ) {
                    Text("Start Group Call", color = CyanGlow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGroupCallModal = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = Color(0xFF161B26)
        )
    }
}
