package com.example.connecto.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.connecto.ui.theme.ElectricViolet
import com.example.connecto.ui.theme.NeonPink
import com.example.connecto.ui.theme.PulsingOnlineDot
import com.example.connecto.ui.theme.ShimmerConversationItem
import com.example.connecto.ui.theme.StaggeredReveal
import com.example.connecto.ui.theme.TextPrimaryDark
import com.example.connecto.ui.theme.TextSecondaryDark
import com.example.connecto.ui.theme.TypingIndicatorDots
import com.example.connecto.ui.theme.glassmorphicCard
import com.example.connecto.ui.theme.pressScaleEffect

data class ConversationItem(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val isOnline: Boolean
)

data class ChatMessage(
    val sender: String,
    val text: String,
    val isMe: Boolean
)

@Composable
fun MessagesScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeChat by remember { mutableStateOf<ConversationItem?>(null) }
    // Simulate loading for shimmer demo
    var isLoading by remember { mutableStateOf(true) }

    val conversations = remember {
        listOf(
            ConversationItem(1, "Elena Vance", "Awesome! The neural pipeline is live 🚀", "10:42 AM", 2, true),
            ConversationItem(2, "Marcus Chen", "Did you test the new glassmorphic components?", "Yesterday", 0, true),
            ConversationItem(3, "Sophia Ross", "Let's connect at the Spatial AI summit next week.", "2 days ago", 1, false),
            ConversationItem(4, "Zack Miller", "Shared the repo link in the group.", "Oct 24", 0, true)
        )
    }

    // Dismiss shimmer after short delay
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(900)
        isLoading = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Text(
                text = "Conversations",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimaryDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Glassmorphic Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphicCard(shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search messages or signals...", color = TextSecondaryDark, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Friends Reel
            Text(
                text = "ONLINE NOW",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Online friends reel with pulsing dot
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val onlineFriends = conversations.filter { it.isOnline }
                itemsIndexed(onlineFriends) { index, chat ->
                    StaggeredReveal(index = index, staggerMs = 60L) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.pressScaleEffect(onClick = { activeChat = chat })
                        ) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(ElectricViolet, CyanGlow))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = chat.name.first().toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                                // Pulsing Online Indicator Dot
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                ) {
                                    PulsingOnlineDot(
                                        color = CyanGlow,
                                        size = 11.dp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = chat.name.split(" ").first(),
                                fontSize = 12.sp,
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Conversations List — with shimmer skeleton or real data
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    // Show shimmer skeletons while loading
                    items(3) {
                        ShimmerConversationItem()
                    }
                } else {
                    itemsIndexed(conversations) { index, chat ->
                        StaggeredReveal(index = index) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pressScaleEffect(onClick = { activeChat = chat })
                                    .glassmorphicCard(shape = RoundedCornerShape(22.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(ElectricViolet, NeonPink))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = chat.name.first().toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                        // Online status dot on avatar
                                        if (chat.isOnline) {
                                            Box(
                                                modifier = Modifier.align(Alignment.BottomEnd)
                                            ) {
                                                PulsingOnlineDot(
                                                    color = CyanGlow,
                                                    size = 9.dp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = chat.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = TextPrimaryDark
                                            )

                                            Text(
                                                text = chat.time,
                                                fontSize = 12.sp,
                                                color = TextSecondaryDark
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = chat.lastMessage,
                                            fontSize = 13.sp,
                                            color = TextSecondaryDark,
                                            maxLines = 1
                                        )
                                    }

                                    if (chat.unreadCount > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(ElectricViolet),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = chat.unreadCount.toString(),
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

                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }

        // Live Active Chat Sheet / Modal
        AnimatedVisibility(
            visible = activeChat != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(380, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
        ) {
            activeChat?.let { chat ->
                ChatConversationDetail(
                    chat = chat,
                    onClose = { activeChat = null }
                )
            }
        }
    }
}

@Composable
private fun ChatConversationDetail(
    chat: ConversationItem,
    onClose: () -> Unit
) {
    var typedMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val messages = remember {
        mutableStateListOf(
            ChatMessage(chat.name, "Hey! I saw your post on Connecto Radar.", false),
            ChatMessage("Me", "Thanks! Super excited to build on this network.", true),
            ChatMessage(chat.name, chat.lastMessage, false)
        )
    }

    // Animated send button scale based on whether there's text
    val sendButtonScale by animateFloatAsState(
        targetValue = if (typedMessage.isNotBlank()) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "sendBtnScale"
    )
    val sendButtonAlpha by animateFloatAsState(
        targetValue = if (typedMessage.isNotBlank()) 1f else 0.45f,
        animationSpec = tween(durationMillis = 180),
        label = "sendBtnAlpha"
    )

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .glassmorphicCard(
                shape = RoundedCornerShape(32.dp),
                backgroundColor = Color(0xF20B0E14),
                borderColor = ElectricViolet.copy(alpha = 0.4f),
                borderWidth = 1.5.dp
            )
            .padding(16.dp)
    ) {
        var showOptionsMenu by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Chat Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Online dot next to name in header
                    if (chat.isOnline) {
                        PulsingOnlineDot(color = CyanGlow, size = 9.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Text(
                            text = chat.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = if (chat.isOnline) "Active Signal Online" else "Offline",
                            fontSize = 12.sp,
                            color = if (chat.isOnline) CyanGlow else TextSecondaryDark
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondaryDark
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false },
                        modifier = Modifier.background(DarkCardSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear Chat", color = TextPrimaryDark) },
                            onClick = {
                                showOptionsMenu = false
                                messages.clear()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mute Notifications", color = TextPrimaryDark) },
                            onClick = {
                                showOptionsMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chat Messages List with animated new message entries
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(messages) { index, msg ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(220))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (msg.isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
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
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = TextPrimaryDark,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Typing indicator above input
            if (chat.isOnline) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypingIndicatorDots(color = CyanGlow)
                    Text(
                        text = "${chat.name.split(" ").first()} is typing",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chat Text Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typedMessage,
                    onValueChange = { typedMessage = it },
                    placeholder = { Text("Write a message...", color = TextSecondaryDark, fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricViolet,
                        unfocusedBorderColor = DarkCardSurface,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Animated Send Button — scales up when text present
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            scaleX = sendButtonScale
                            scaleY = sendButtonScale
                            alpha = sendButtonAlpha
                        }
                        .pressScaleEffect(
                            onClick = {
                                if (typedMessage.isNotBlank()) {
                                    messages.add(ChatMessage("Me", typedMessage, true))
                                    typedMessage = ""
                                }
                            }
                        )
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ElectricViolet, CyanGlow))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
