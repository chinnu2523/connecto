package com.connecto.store.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.connecto.store.BuildConfig

/**
 * Our Store Help & Support Center Dialog.
 * Provides interactive FAQs, direct contact channels, and device diagnostics.
 */
@Composable
fun OurStoreHelpSupportDialog(
    onDismiss: () -> Unit
) {
    val theme = LocalStoreTheme.current
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = FAQ, 1 = Contact, 2 = Diagnostics
    val scrollState = rememberScrollState()

    LaunchedEffect(selectedSection) {
        scrollState.scrollTo(0)
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
                                    .border(1.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.HeadsetMic,
                                    contentDescription = null,
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Help & Support",
                                    color = theme.textPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = "Our Store Ecosystem Guidance",
                                    color = theme.textMuted,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Close Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(theme.cardElevated)
                                .border(1.dp, theme.cardBorder, CircleShape)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Segmented Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(theme.cardElevated)
                            .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("FAQ", "Contact Us", "Diagnostics").forEachIndexed { index, label ->
                            val isSelected = selectedSection == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) {
                                            if (theme.isDark) Color(0xFF2563EB) else Color(0xFF2563EB)
                                        } else Color.Transparent
                                    )
                                    .clickable { selectedSection = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else theme.textMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content Scroll Area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when (selectedSection) {
                            0 -> {
                                // FAQ Section
                                HelpFaqItem(
                                    question = "How do updates work in Our Store?",
                                    answer = "Our Store continuously queries high-speed Cloudflare edge nodes for new signed builds. When a newer versionCode is available, a single tap downloads the APK and triggers silent package verification and installation.",
                                    theme = theme
                                )
                                HelpFaqItem(
                                    question = "Is app distribution secure?",
                                    answer = "Yes. Every application in Our Store is checked against SHA-256 cryptographic signatures and protected with local security gating to ensure complete code authenticity.",
                                    theme = theme
                                )
                                HelpFaqItem(
                                    question = "How do I toggle the Dark / White theme?",
                                    answer = "Go to the Account tab and use the Theme Mode Toggle switch. You can switch anytime between Pure OLED Pitch Black (#000000) and Pristine Studio White (#FFFFFF) with fluid animated transitions.",
                                    theme = theme
                                )
                                HelpFaqItem(
                                    question = "What is Connecto Single Sign-On (SSO)?",
                                    answer = "Connecto SSO allows 1-tap hardware authentication directly between the installed Connecto app and Our Store without entering credentials manually.",
                                    theme = theme
                                )
                                HelpFaqItem(
                                    question = "Can I use Our Store as a Guest?",
                                    answer = "Yes. Guest Explorer mode allows full browsing, app detail inspection, and catalog downloads without creating an account.",
                                    theme = theme
                                )
                            }
                            1 -> {
                                // Contact Us Section
                                ContactCard(
                                    icon = Icons.Rounded.Share,
                                    title = "Refer & Share Our Store",
                                    subtitle = "https://connecto.fun/downloads/our-store.apk",
                                    actionLabel = "Share Link",
                                    theme = theme,
                                    onAction = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Download Our Store - the independent, verified Android App Store with zero trackers: https://connecto.fun/downloads/our-store.apk"
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Our Store via"))
                                    }
                                )

                                ContactCard(
                                    icon = Icons.Rounded.RateReview,
                                    title = "Send Product Feedback",
                                    subtitle = "feedback@connecto.fun",
                                    actionLabel = "Send Feedback",
                                    theme = theme,
                                    onAction = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:feedback@connecto.fun?subject=Our%20Store%20Product%20Feedback")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Feedback Email", "feedback@connecto.fun"))
                                            Toast.makeText(context, "Copied feedback@connecto.fun", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )

                                ContactCard(
                                    icon = Icons.Rounded.Email,
                                    title = "Official Support Email",
                                    subtitle = "support@connecto.fun",
                                    actionLabel = "Copy Email",
                                    theme = theme,
                                    onAction = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Support Email", "support@connecto.fun"))
                                        Toast.makeText(context, "Email copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                ContactCard(
                                    icon = Icons.Rounded.Security,
                                    title = "Security & Vulnerability Team",
                                    subtitle = "security@connecto.fun",
                                    actionLabel = "Report Issue",
                                    theme = theme,
                                    onAction = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:security@connecto.fun?subject=Our%20Store%20Security%20Inquiry")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Security Email", "security@connecto.fun"))
                                            Toast.makeText(context, "Copied security@connecto.fun", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )

                                ContactCard(
                                    icon = Icons.Rounded.Language,
                                    title = "Web Support & Documentation",
                                    subtitle = "https://connecto.fun/support",
                                    actionLabel = "Open Web",
                                    theme = theme,
                                    onAction = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://connecto.fun"))
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Visit https://connecto.fun", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                            2 -> {
                                // Diagnostics Section
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(theme.cardElevated)
                                        .border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "CLIENT & SYSTEM DIAGNOSTICS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp,
                                            color = theme.textMuted
                                        )

                                        DiagnosticRow("Store Version", "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", theme)
                                        DiagnosticRow("Application ID", BuildConfig.APPLICATION_ID, theme)
                                        DiagnosticRow("Device Model", "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}", theme)
                                        DiagnosticRow("Android Version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", theme)
                                        DiagnosticRow("Edge Gateway", "Cloudflare Global CDN (HTTP/3)", theme)
                                        DiagnosticRow("Security Engine", "Verified SHA-256 Gatekeeper", theme)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Done / Close Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.accentColor)
                            .clickable { onDismiss() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLOSE HELP CENTER",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpFaqItem(
    question: String,
    answer: String,
    theme: StoreThemeColors
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(theme.cardElevated)
            .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    color = theme.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = theme.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Text(
                    text = answer,
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ContactCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    theme: StoreThemeColors,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(theme.cardElevated)
            .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(theme.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        color = theme.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = theme.textMuted,
                        fontSize = 11.5.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.accentColor.copy(alpha = 0.15f))
                    .border(0.8.dp, theme.accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionLabel,
                    color = theme.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    theme: StoreThemeColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = theme.textMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = theme.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
