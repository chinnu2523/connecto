package com.example.connecto.ui.components

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.BuildConfig
import com.example.connecto.ui.designsystem.ConnectoModalBottomSheet
import com.example.connecto.ui.designsystem.LocalConnectoColors

/**
 * Connecto Feedback & Feature Suggestion Sheet.
 * Allows users to share feedback, select categories, choose 1-5 star ratings,
 * and submit with automatic system diagnostics.
 */
@Composable
fun ConnectoFeedbackDialog(
    username: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = LocalConnectoColors.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val categories = remember {
        listOf(
            "Feature Suggestion",
            "UI & Experience",
            "Voice & Video Calls",
            "Performance & Battery",
            "Notifications",
            "General"
        )
    }
    var selectedCategory by remember { mutableStateOf("Feature Suggestion") }
    var rating by remember { mutableIntStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    val quickPills = remember {
        listOf(
            "End-to-End Encryption feels instant",
            "Crystal-clear audio calls",
            "Love the pure OLED black theme",
            "Optimistic UI is lightning fast",
            "Smooth drag-to-refresh animation"
        )
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
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Send Feedback",
                            color = colors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Connected as @$username",
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
                // Rating Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.4f))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "RATE YOUR CONNECTO EXPERIENCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (star in 1..5) {
                                val isFilled = star <= rating
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        rating = star
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "$star stars",
                                        tint = if (isFilled) Color(0xFFF59E0B) else colors.textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = when (rating) {
                                5 -> "Exceptional & Encrypted 🚀"
                                4 -> "Great Messaging & Calls 👍"
                                3 -> "Good, has room for improvements 👌"
                                2 -> "Experienced hiccups / issues ⚠️"
                                else -> "Needs major fixes ❌"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary
                        )
                    }
                }

                // Category Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FEEDBACK CATEGORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
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
                                        if (isSelected) colors.primary
                                        else colors.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.primary
                                        else colors.borderSubtle,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedCategory = cat
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) colors.onPrimary else colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Quick Suggestion Pills
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "QUICK HIGHLIGHTS (TAP TO ADD)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
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
                                    .background(colors.surfaceVariant.copy(alpha = 0.35f))
                                    .border(0.8.dp, colors.borderSubtle, RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        feedbackText = if (feedbackText.isBlank()) pill
                                        else "$feedbackText\n• $pill"
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = pill,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Message Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "YOUR THOUGHTS & DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
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
                                text = "What do you love about Connecto? What can we improve, polish, or add?",
                                color = colors.textSecondary.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = colors.surfaceVariant.copy(alpha = 0.3f),
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.borderSubtle,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    Text(
                        text = "${feedbackText.length}/1000 characters",
                        color = colors.textSecondary,
                        fontSize = 10.5.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                // Device Diagnostics Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.3f))
                        .border(0.8.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Included: Connecto v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) • Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) • ${Build.MANUFACTURER} ${Build.MODEL}",
                            fontSize = 10.5.sp,
                            color = colors.textSecondary,
                            lineHeight = 14.sp
                        )
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
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Text(
                        text = "Cancel",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                    Button(
                        onClick = {
                            val trimmed = feedbackText.trim()
                            val feedbackSummary = buildString {
                                appendLine("--- Connecto User Feedback ---")
                                appendLine("User: @$username")
                                appendLine("Category: $selectedCategory")
                                appendLine("Experience Rating: $rating / 5 Stars")
                                appendLine()
                                appendLine("Feedback Description:")
                                appendLine(if (trimmed.isNotBlank()) trimmed else "[Quick Positive Feedback]")
                                appendLine()
                                appendLine("--- System Diagnostics ---")
                                appendLine("App Version: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})")
                                appendLine("Android OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                                appendLine("Device Model: ${Build.MANUFACTURER} ${Build.MODEL}")
                            }

                            val subject = "[Connecto Feedback - $selectedCategory] @$username ($rating Stars)"
                            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@connecto.fun?subject=${Uri.encode(subject)}&body=${Uri.encode(feedbackSummary)}")
                            }

                            try {
                                context.startActivity(mailIntent)
                            } catch (_: Exception) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Feedback", feedbackSummary))
                                Toast.makeText(context, "Feedback copied to clipboard (support@connecto.fun)", Toast.LENGTH_LONG).show()
                            }

                            Toast.makeText(context, "Thank you @$username! Feedback submitted 🚀", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
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
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = colors.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Send Feedback",
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
