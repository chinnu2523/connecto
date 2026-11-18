package com.example.connecto.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.ui.designsystem.ConnectoModalBottomSheet
import com.example.connecto.ui.designsystem.LocalConnectoColors

/**
 * Connecto Refer & Invite Friends Sheet.
 * Enables users to invite friends and coworkers to Connecto via share sheet or copied referral link.
 */
@Composable
fun ConnectoReferDialog(
    username: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalConnectoColors.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val cleanUser = remember(username) { username.trim().removePrefix("@") }
    val referralLink = remember(cleanUser) { "https://connecto.fun/download?ref=$cleanUser" }
    val inviteMessage = remember(cleanUser, referralLink) {
        "Hey! Connect with me on Connecto — the private, ultra-fast end-to-end encrypted messaging and voice/video calling app! 🔒🚀\n\nDownload the app: $referralLink\nAdd me via username: @$cleanUser"
    }

    fun copyToClipboard(text: String, label: String = "Invite Link") {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
    }

    fun launchShareChooser() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, inviteMessage)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Invite friends to Connecto via...")
        try {
            context.startActivity(shareIntent)
        } catch (_: Exception) {
            copyToClipboard(referralLink, "Invite Link")
        }
    }

    ConnectoModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary.copy(alpha = 0.12f))
                            .border(1.dp, colors.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Invite Friends & Refer",
                            color = colors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Grow your secure private circle",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Referral Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surfaceVariant.copy(alpha = if (colors.isDark) 0.35f else 0.5f))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.12f))
                                .border(1.dp, colors.primary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Invite Your Friends to Connecto",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Bring friends and family to an ad-free, encrypted space with instant messaging, crystal-clear calls, and total data privacy.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Personal Referral Link Box
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "YOUR PERSONAL INVITE LINK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surfaceVariant.copy(alpha = 0.4f))
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = referralLink,
                                    color = colors.primary,
                                    fontSize = 12.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Your handle: @$cleanUser",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.15f))
                                    .clickable { copyToClipboard(referralLink, "Invite Link") }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "COPY",
                                    color = colors.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Connecto Privacy & Feature Guarantees
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "WHAT YOUR FRIENDS GET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    val perks = listOf(
                        Triple(Icons.Default.Lock, "End-to-End Encryption", "Signal-grade cryptographic protocols keep messages and files confidential"),
                        Triple(Icons.Default.PhoneInTalk, "Studio Quality Voice & Video", "Low-latency WebRTC media relays for ultra-clear voice and video calls"),
                        Triple(Icons.Default.Shield, "Zero Telemetry & Zero Ads", "We never track, profile, or sell user communications to anyone")
                    )

                    perks.forEach { (icon, title, desc) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant.copy(alpha = 0.35f))
                                .border(0.8.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = title,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { copyToClipboard(inviteMessage, "Invite Message") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Copy Message",
                            color = colors.textSecondary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = { launchShareChooser() },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = colors.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Share via Apps",
                            color = colors.onPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
