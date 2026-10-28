package com.example.connecto.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SupportChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun CustomerSupportAiDialog(
    username: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            SupportChatMessage(
                sender = "Connecto AI Support",
                text = "Hello @$username! 👋 I am Connecto's 24/7 AI Customer Support Assistant.\n\nI can help you with anything related to Connecto — including End-to-End Encryption, Voice & Video Calls, Background Notifications, Themes & Chat Wallpapers, Two-Factor Authentication, and Account Privacy.\n\nHow can I help you today? You can select a quick topic below or type your question!",
                isUser = false
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isAiTyping by remember { mutableStateOf(false) }

    fun generateAiResponse(userQuery: String): String {
        val q = userQuery.lowercase().trim()
        return when {
            q.contains("delete") && (q.contains("account") || q.contains("data") || q.contains("purge")) -> {
                "⚠️ **Deleting Your Connecto Account:**\n\n" +
                "You can permanently delete your account directly inside the app:\n" +
                "1. Go to **Profile Tab**.\n" +
                "2. Tap **Help & Support**.\n" +
                "3. At the **very top** of the screen, you will see the **Delete Account & Data** card.\n" +
                "4. Tap **Delete My Account**, review the prompt, and type `DELETE` in capital letters to confirm.\n\n" +
                "🔒 *Note: Closing your account permanently purges your identity, chats, groups, call history, and physical database backups forever. This action cannot be undone.*"
            }
            q.contains("wallpaper") || q.contains("background") -> {
                "🎨 **Chat Wallpaper & Background Customization:**\n\n" +
                "• **Setting a Wallpaper:** Open any 1-on-1 direct chat or group, tap the friend's profile info (or 3-dots), select **Theme & Wallpaper**, and choose from vibrant gradients (like Liquid Aqua Droplets, Prism Glass Aurora, Cosmic Mesh) or tap **Pick From Gallery** for a custom photo.\n" +
                "• **Adjusting Scale & Position:** Inside the chat, tap the top Wallpaper icon to scale, offset, dim, and blur your wallpaper for maximum legibility.\n" +
                "• **Persistence Healing:** Connecto v4.6.4 includes multi-key synchronization across scoped IDs and usernames so your wallpapers will never disappear!"
            }
            q.contains("mic") || q.contains("audio") || q.contains("voice") || q.contains("call") || q.contains("sound") -> {
                "📞 **Voice & Audio Settings / Troubleshooting:**\n\n" +
                "• **Voice Isolation:** Connecto features a studio-grade DSP pipeline (140Hz high-pass filter, 3400Hz vocal filter, expander gate, and hardware noise suppressor) to cancel background noise.\n" +
                "• **Themed Audio Settings:** In Profile -> **Voice & Audio Settings**, all switches (Voice Isolation, Echo Cancellation, AGC, and HD Studio Voice 48 kHz Opus) dynamically follow your active theme.\n" +
                "• **Microphone Sensitivity Test:** You can tap 'Test Mic' inside Voice & Audio Settings to monitor your real-time 18-bar VU spectral levels.\n" +
                "• **Calls Tab:** The top bar now cleanly features the **Group Call** launcher for instant multi-peer voice lounges!"
            }
            q.contains("notif") || q.contains("delay") || q.contains("banner") || q.contains("background") || q.contains("push") -> {
                "🔔 **Background Notifications & WhatsApp-Style Delivery:**\n\n" +
                "Connecto uses Google FCM v1 high-priority data payloads that wake your device even when the app is completely swiped away.\n\n" +
                "• **If notifications are delayed or not ringing:**\n" +
                "  1. Go to Phone Settings -> Apps -> Connecto -> **Battery** -> Set to **Unrestricted** (Disable battery optimization).\n" +
                "  2. On Xiaomi, POCO, or Vivo/Oppo devices, enable **Auto-start** and **Lock in Recent Apps**.\n" +
                "  3. Check Profile -> **Notifications** to ensure notification alerts and the **Faah** alert tone are active.\n" +
                "• **Direct Reply:** Swipe down on any notification banner to reply directly without leaving your current app!"
            }
            q.contains("encrypt") || q.contains("e2ee") || q.contains("security") || q.contains("blackscreen") || q.contains("privacy") || q.contains("flag_secure") -> {
                "🔒 **End-to-End Encryption & Blackscreen Privacy:**\n\n" +
                "• **End-to-End Encryption (E2EE):** All messages, photos, and voice data are encrypted client-side using **AES-256-GCM** with ephemeral salt derivation. Only you and your chat partner hold the decryption keys.\n" +
                "• **Admin Screenshot & Anti-Capture Protection:** Connecto applies Android `FLAG_SECURE` at the hardware compositor level for members. Only administrator users can take screenshots or screen record in the application; for all other members, screenshots and screen recordings are strictly blocked to protect privacy.\n" +
                "• **Stealth Mode:** Toggle Stealth Mode in Profile -> Security & Privacy to hide your green online status dot while reading and chatting freely!"
            }
            q.contains("2fa") || q.contains("two-factor") || q.contains("otp") || q.contains("login") || q.contains("password") -> {
                "🛡️ **Two-Factor Authentication (2FA) & Account Security:**\n\n" +
                "• **Email-Only OTP Delivery:** When 2FA is enabled in Profile -> Security & Privacy, logging in on any new device requires a 6-digit one-time code sent strictly to your verified email address.\n" +
                "• **Zero SMS Leaks:** Connecto never relies on insecure SMS routing. Your code is protected end-to-end.\n" +
                "• **Session Management:** View all currently logged-in devices in Profile -> **Session Management** and terminate unrecognized sessions with one tap!"
            }
            q.contains("theme") || q.contains("color") || q.contains("dark") || q.contains("light") || q.contains("oled") -> {
                "🎨 **Display Themes & Liquid Glass:**\n\n" +
                "Connecto features five custom themes built with 120fps hardware acceleration:\n" +
                "• **Noir:** Pure 0-Nit OLED Black for ultimate battery savings and sleek luxury.\n" +
                "• **Liquid Glass:** Refractive glass borders with subtle floating luminescence.\n" +
                "• **Midnight:** Deep navy slate twilight tones.\n" +
                "• **Emerald:** Tactical dark matrix styling.\n" +
                "• **Parchment:** Warm archival ivory grain for comfortable daylight reading.\n\n" +
                "Switch anytime in Profile -> **Appearance & Display Theme**!"
            }
            q.contains("terms") || q.contains("policy") || q.contains("legal") -> {
                "📜 **Privacy Policy & Terms of Service:**\n\n" +
                "You can view both full documents at the **very bottom of Help & Support** or directly on the web:\n" +
                "• **Privacy Policy:** Zero-knowledge encryption, zero third-party tracking, full data ownership.\n" +
                "• **Terms of Service:** Acceptable use, community safety, and peer-to-peer communication standards.\n" +
                "• Links: [connecto.fun/privacy](https://connecto.fun/privacy) and [connecto.fun/terms](https://connecto.fun/terms)"
            }
            q.contains("contact") || q.contains("email") || q.contains("human") || q.contains("agent") || q.contains("report") -> {
                "📬 **Contacting Human Support & Bug Reports:**\n\n" +
                "If you need personalized assistance or have spotted a bug:\n" +
                "• **Email Support:** `support@connecto.fun` (Response within 24 hours)\n" +
                "• **Report Issue:** Tap 'Report Issue' in Help & Support to submit detailed logs directly to our engineering team.\n" +
                "• **Live System Status:** Visit [connecto.fun/status](https://connecto.fun/status) for real-time edge network health."
            }
            else -> {
                "Thanks for reaching out! Regarding **\"$userQuery\"**:\n\n" +
                "Connecto is built around end-to-end encryption, ultra low-latency WebSockets, WebRTC spatial mesh voice lounges, and personalized luxury themes.\n\n" +
                "Here are a few quick tips:\n" +
                "• **For Audio or Call questions:** Check Profile -> Voice & Audio Settings.\n" +
                "• **For Chat Wallpaper issues:** Visit friend options -> Theme & Wallpaper.\n" +
                "• **For Privacy & Security:** Check Profile -> Security & Privacy (2FA, Stealth Mode, Biometric Vault).\n" +
                "• **For Account Deletion:** Tap 'Delete Account & Data' at the top of Help & Support.\n\n" +
                "Would you like me to elaborate on any of these topics, or would you like to email human support at `support@connecto.fun`?"
            }
        }
    }

    fun handleSend(textToSend: String) {
        val query = textToSend.trim()
        if (query.isBlank()) return

        messages.add(
            SupportChatMessage(
                sender = username,
                text = query,
                isUser = true
            )
        )
        inputText = ""
        isAiTyping = true

        coroutineScope.launch {
            // Natural typing delay for conversational AI feel
            delay(600)
            val aiResponse = generateAiResponse(query)
            messages.add(
                SupportChatMessage(
                    sender = "Connecto AI Support",
                    text = aiResponse,
                    isUser = false
                )
            )
            isAiTyping = false
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Connecto AI Support",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "24/7 ONLINE",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                            Text(
                                text = "Instant assistance for issues & features",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            messages.clear()
                            messages.add(
                                SupportChatMessage(
                                    sender = "Connecto AI Support",
                                    text = "Chat cleared! How can I help you next, @$username?",
                                    isUser = false
                                )
                            )
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chat Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.isUser
                        val timeStr = remember(msg.timestamp) {
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column(
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 16.dp
                                            )
                                        )
                                        .background(
                                            if (isUser) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                        )
                                        .border(
                                            1.dp,
                                            if (isUser) Color.Transparent
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 16.dp
                                            )
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.5.sp,
                                        lineHeight = 19.sp,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
                                ) {
                                    Text(
                                        text = timeStr,
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    if (!isUser) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy message",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .size(13.dp)
                                                .clickable {
                                                    clipboardManager.setText(AnnotatedString(msg.text))
                                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isAiTyping) {
                        item(key = "typing_indicator") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 36.dp, top = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connecto AI is typing...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Quick Suggestion Chips
                val quickChips = listOf(
                    "🔒 How does Encryption & Privacy work?",
                    "🎨 How do I set chat wallpapers?",
                    "📞 Audio & call issues",
                    "🔔 Background notifications",
                    "🛡️ How does 2FA work?",
                    "❌ How do I delete my account?"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickChips.forEach { chipText ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                .clickable { handleSend(chipText) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipText,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Bottom Input Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp, max = 120.dp),
                        placeholder = {
                            Text(
                                "Ask about any Connecto feature or issue...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { handleSend(inputText) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { handleSend(inputText) },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Privacy Policy",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://connecto.fun/privacy")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Web Version", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Connecto Platform Privacy Policy",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Last Updated: October 2026 • Version 4.6.4",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LegalSectionItem(
                        number = "1",
                        title = "Zero-Knowledge End-to-End Encryption",
                        body = "Connecto is engineered on the principle that your private conversations are yours alone. All direct messages, group chats, images, and voice payloads are encrypted on your device using AES-256-GCM before transmission. Encryption keys are generated client-side and never touch our servers or edge workers. We cannot read, analyze, or decrypt your private communications under any circumstance."
                    )

                    LegalSectionItem(
                        number = "2",
                        title = "Hardware Screen Protection & Admin Screenshot Policy (FLAG_SECURE)",
                        body = "Connecto enforces WindowManager.LayoutParams.FLAG_SECURE at Android's SurfaceFlinger hardware level for all members. Only authorized administrator users are permitted to take screenshots or screen record in the application. For all other members, screenshot tools, screen recorders, and screen-sharing apps (Zoom, Discord, Meet) receive a solid black screen (#000000)."
                    )

                    LegalSectionItem(
                        number = "3",
                        title = "Minimal Data Collection Policy",
                        body = "We collect only the bare minimum required to maintain your account: your chosen username, hashed credentials, and optional verified email address for Two-Factor Authentication. We do NOT track your GPS location, sell personal data to advertisers, or utilize invasive third-party analytics SDKs."
                    )

                    LegalSectionItem(
                        number = "4",
                        title = "Voice Channels & WebRTC Spatial Mesh",
                        body = "Voice lounges establish direct peer-to-peer WebRTC mesh connections between participants. Audio packets are encoded with the Opus 48 kHz high-definition codec and streamed directly between devices with zero server recording or storage."
                    )

                    LegalSectionItem(
                        number = "5",
                        title = "Right to Total Data Purge (Account Deletion)",
                        body = "You have the unconditional right to delete your account at any time. When you trigger 'Delete Account & Data', our database immediately purges your user profile, active sessions, message relations, and backups forever. No ghost records remain."
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun TermsOfServiceDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Terms of Service",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://connecto.fun/terms")).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Web Version", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Connecto Platform Terms of Service",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Last Updated: October 2026 • Version 4.6.4",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LegalSectionItem(
                        number = "1",
                        title = "Acceptance of Terms",
                        body = "By downloading, installing, or accessing Connecto (including the Android native application, Web platform, and Cloudflare Edge services), you agree to be bound by these Terms of Service. If you do not agree to these terms, you may discontinue use and delete your account."
                    )

                    LegalSectionItem(
                        number = "2",
                        title = "Permitted & Lawful Use",
                        body = "Connecto is provided as an open, encrypted communication platform for friends, colleagues, and developer communities. You agree not to use Connecto to distribute malware, orchestrate denial-of-service attacks, engage in targeted harassment or abuse, or violate local laws."
                    )

                    LegalSectionItem(
                        number = "3",
                        title = "Account Responsibility & Security",
                        body = "You are responsible for safeguarding your login credentials and maintaining the confidentiality of your device encryption keys. We strongly recommend enabling Two-Factor Authentication (2FA) and Biometric Vault locking to safeguard your chats against unauthorized physical access."
                    )

                    LegalSectionItem(
                        number = "4",
                        title = "Voice Channels & Community Etiquette",
                        body = "Participants in WebRTC spatial voice lounges and public channels are expected to maintain respectful behavior. Channel administrators retain the authority to mute, kick, or ban members who disrupt voice conversations."
                    )

                    LegalSectionItem(
                        number = "5",
                        title = "Service Availability & Termination",
                        body = "We strive for 99.99% edge network uptime across our global Cloudflare and WebSocket infrastructure. We reserve the right to suspend or terminate accounts that actively abuse our network sockets. You may terminate your account at any time via Profile -> Help & Support -> Delete Account & Data."
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LegalSectionItem(
    number: String,
    title: String,
    body: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = body,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
