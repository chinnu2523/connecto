package com.connecto.store.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.connecto.store.BuildConfig

/**
 * Our Store Feedback & Suggestions Dialog.
 * Allows users to submit ratings, categorize feedback, and send insights with device diagnostics.
 */
@Composable
fun OurStoreFeedbackDialog(
    onDismiss: () -> Unit
) {
    val theme = LocalStoreTheme.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val categories = remember {
        listOf("Feature Request", "App Experience", "Bug Report", "Store Catalog", "Performance", "General")
    }
    var selectedCategory by remember { mutableStateOf("Feature Request") }
    var rating by remember { mutableIntStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    val quickPills = remember {
        listOf(
            "Ultra-clean OLED interface!",
            "Fastest downloads in class",
            "Smooth spring animations",
            "Love the drag-to-refresh",
            "Please add more open-source apps"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        color = (if (theme.isDark) Color.White else Color.Black).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = if (theme.isDark) Color(0xFF0D0F14) else Color(0xFFFFFFFF),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (theme.isDark) Color(0xFF161922) else Color(0xFFF1F5F9))
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RateReview,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Send Feedback",
                                    color = theme.textPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Help us shape the future of Our Store",
                                    color = theme.textMuted,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.cardBorder.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = theme.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable Feedback Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Rating Selector Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(theme.cardElevated)
                                .border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "HOW WOULD YOU RATE OUR STORE?",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textDarkMuted,
                                    letterSpacing = 0.8.sp
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (star in 1..5) {
                                        val isFilled = star <= rating
                                        IconButton(
                                            onClick = { rating = star },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isFilled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                                contentDescription = "$star stars",
                                                tint = if (isFilled) Color(0xFFF59E0B) else theme.textDarkMuted,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = when (rating) {
                                        5 -> "Outstanding Experience 🚀"
                                        4 -> "Great & Reliable 👍"
                                        3 -> "Good, has room for improvements 👌"
                                        2 -> "Needs attention & fixes ⚠️"
                                        else -> "Encountered major issues ❌"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.accentColor
                                )
                            }
                        }

                        // Category Selector
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "FEEDBACK TOPIC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textDarkMuted,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(categories) { cat ->
                                    val isSelected = cat == selectedCategory
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) theme.accentColor
                                                else theme.cardElevated
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) theme.accentColor
                                                else theme.cardBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedCategory = cat }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            color = if (isSelected) Color.White else theme.textSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Quick suggestion pills
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "QUICK HIGHLIGHTS (TAP TO ADD)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textDarkMuted,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(quickPills) { pill ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.cardElevated.copy(alpha = 0.6f))
                                            .border(0.8.dp, theme.cardBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                feedbackText = if (feedbackText.isBlank()) pill
                                                else "$feedbackText\n• $pill"
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = pill,
                                            fontSize = 11.sp,
                                            color = theme.textMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Detailed Message Text Box
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "YOUR COMMENTS & SUGGESTIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textDarkMuted,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            OutlinedTextField(
                                value = feedbackText,
                                onValueChange = { if (it.length <= 1000) feedbackText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                placeholder = {
                                    Text(
                                        text = "Tell us what's working well, what needs improvement, or features you'd like to see in Our Store...",
                                        color = theme.textDarkMuted,
                                        fontSize = 13.sp
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = theme.searchBarBg,
                                    unfocusedContainerColor = theme.searchBarBg,
                                    focusedBorderColor = theme.borderHighlight,
                                    unfocusedBorderColor = theme.cardBorder,
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textSecondary
                                )
                            )

                            Text(
                                text = "${feedbackText.length}/1000 characters",
                                color = theme.textDarkMuted,
                                fontSize = 10.5.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }

                        // Diagnostics Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.cardElevated.copy(alpha = 0.5f))
                                .border(0.8.dp, theme.cardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = theme.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Diagnostics will automatically include: Our Store v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) • Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                                    fontSize = 10.5.sp,
                                    color = theme.textMuted,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
                        ) {
                            Text(
                                text = "Cancel",
                                color = theme.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                val trimmed = feedbackText.trim()
                                val feedbackSummary = buildString {
                                    appendLine("--- Our Store User Feedback ---")
                                    appendLine("Topic: $selectedCategory")
                                    appendLine("Rating: $rating / 5 Stars")
                                    appendLine()
                                    appendLine("Comments:")
                                    appendLine(if (trimmed.isNotBlank()) trimmed else "[Quick Positive Feedback]")
                                    appendLine()
                                    appendLine("--- Device Telemetry ---")
                                    appendLine("App Version: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})")
                                    appendLine("Android OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                                    appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                                }

                                val emailSubject = "[Our Store Feedback - $selectedCategory] $rating Stars"
                                val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:feedback@connecto.fun?subject=${Uri.encode(emailSubject)}&body=${Uri.encode(feedbackSummary)}")
                                }

                                try {
                                    context.startActivity(mailIntent)
                                } catch (_: Exception) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Feedback", feedbackSummary))
                                    Toast.makeText(context, "Feedback copied to clipboard (feedback@connecto.fun)", Toast.LENGTH_LONG).show()
                                }

                                Toast.makeText(context, "Thank you for your feedback! 🚀", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.accentColor
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Submit Feedback",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
