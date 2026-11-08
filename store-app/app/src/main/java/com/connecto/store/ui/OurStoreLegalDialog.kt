package com.connecto.store.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Our Store Legal Center: Terms of Use & Privacy Policy Dialog.
 * Designed with pure OLED obsidian aesthetics, segmented tabs, and comprehensive legal sections.
 */
@Composable
fun OurStoreLegalDialog(
    initialTab: Int = 0, // 0 = Terms of Use, 1 = Privacy Policy
    onAgree: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val theme = LocalStoreTheme.current
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    val scrollState = rememberScrollState()

    LaunchedEffect(selectedTab) {
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
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                (if (theme.isDark) Color.White else Color.Black).copy(alpha = 0.22f),
                                (if (theme.isDark) Color.White else Color.Black).copy(alpha = 0.05f)
                            )
                        ),
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
                    // ── Header Row ──────────────────────────────────────────
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
                                    .background(
                                        if (theme.isDark) Color(0xFF161922) else Color(0xFFF1F5F9)
                                    )
                                    .border(
                                        1.dp,
                                        if (selectedTab == 0) Color(0xFF3B82F6).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Rounded.Description else Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) Color(0xFF60A5FA) else Color(0xFF34D399),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Our Store Legal Center",
                                    color = theme.textPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = if (selectedTab == 0) "Terms of Service & Licensing" else "Zero-Telemetry Privacy Policy",
                                    color = theme.textMuted,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (theme.isDark) Color(0xFF1A1D27) else Color(0xFFE2E8F0))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Segmented Tab Switcher ──────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (theme.isDark) Color(0xFF141720) else Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LegalTabButton(
                            title = "Terms of Use",
                            icon = Icons.Rounded.Description,
                            isSelected = selectedTab == 0,
                            activeColor = Color(0xFF3B82F6),
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 0 }
                        )

                        LegalTabButton(
                            title = "Privacy Policy",
                            icon = Icons.Rounded.Security,
                            isSelected = selectedTab == 1,
                            activeColor = Color(0xFF10B981),
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 1 }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Scrollable Legal Body ───────────────────────────────
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (theme.isDark) Color(0xFF090A0E) else Color(0xFFF8FAFC))
                            .border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "legalContentSwitch"
                        ) { tab ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                if (tab == 0) {
                                    TermsOfUseContent()
                                } else {
                                    PrivacyPolicyContent()
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Bottom Action Button(s) ──────────────────────────────
                    if (onAgree != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "By agreeing, your acknowledgment is permanently recorded on the Connecto server and won't be requested again.",
                                color = theme.textMuted,
                                fontSize = 10.5.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
                            )
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
                                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.textMuted)
                                ) {
                                    Text(
                                        text = "DECLINE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Button(
                                    onClick = onAgree,
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "AGREE & CONTINUE",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == 0) Color(0xFF2563EB) else Color(0xFF059669)
                            )
                        ) {
                            Text(
                                text = "CLOSE & RETURN",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegalTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalStoreTheme.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(
                if (isSelected) activeColor.copy(alpha = 0.18f) else Color.Transparent
            )
            .border(
                1.dp,
                if (isSelected) activeColor.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(9.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeColor else theme.textMuted,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = title,
                color = if (isSelected) (if (theme.isDark) Color.White else Color(0xFF0F172A)) else theme.textMuted,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TermsOfUseContent() {
    val theme = LocalStoreTheme.current

    LegalSectionCard(
        number = "1",
        title = "Acceptance of Terms & Ecosystem Agreement",
        content = "By downloading, installing, launching, or accessing Our Store (\"the Application\") or installing any software distributed through it, you explicitly agree to be bound by these Terms of Use. If you do not agree to these terms, you must discontinue use and uninstall the Application immediately."
    )

    LegalSectionCard(
        number = "2",
        title = "Autonomous 1-Tap Package Installations",
        content = "Our Store operates via standard Android OS PackageInstaller session IPC. When you choose to install or update an application, package sessions are committed directly into your local operating system without third-party redirection. You acknowledge that installation of software packages is performed at your instruction."
    )

    LegalSectionCard(
        number = "3",
        title = "Cryptographic SHA-256 Integrity Validation",
        content = "Every binary package distributed via Our Store is cryptographically checked against authoritative SHA-256 hashes published on Cloudflare Anycast Edge servers prior to installation. Any package failing integrity verification is immediately aborted and purged to protect your device from tampered binaries."
    )

    LegalSectionCard(
        number = "4",
        title = "Authorized Use & Anti-Tampering Rules",
        content = "You agree not to modify, decompile, reverse engineer, or inject malicious code into Our Store or any applications distributed through it. You may not use automated bots to scrape or disrupt the distribution CDN, nor may you spoof developer signatures or SSO authentication tokens."
    )

    LegalSectionCard(
        number = "5",
        title = "Device Permissions & System Boundaries",
        content = "Our Store requests the standard Android REQUEST_INSTALL_PACKAGES permission solely to execute verified installer sessions on your local device. The Application does not inspect, read, or catalog non-ecosystem applications installed on your system."
    )

    LegalSectionCard(
        number = "6",
        title = "Warranty Disclaimer & Limitation of Liability",
        content = "The Application and all ecosystem packages are provided on an \"AS-IS\" and \"AS-AVAILABLE\" basis without warranties of any kind. To the maximum extent permitted by applicable law, the developers and contributors shall not be liable for any indirect, incidental, or consequential damages resulting from device incompatibility, OS restrictions, or network interruptions."
    )

    LegalSectionCard(
        number = "7",
        title = "Updates & Security Revocations",
        content = "To protect the ecosystem, we reserve the right to flag, block, or deprecate compromised package versions or prompt automatic client updates to ensure system stability and security."
    )
}

@Composable
private fun PrivacyPolicyContent() {
    LegalSectionCard(
        number = "1",
        title = "Strict Privacy-First Commitment",
        content = "Our Store is built with zero commercial trackers, zero advertising SDKs, and zero behavioral telemetry. We believe software package management should respect user autonomy and privacy."
    )

    LegalSectionCard(
        number = "2",
        title = "Hardware KeyStore AES-256-GCM Vault",
        content = "When you authenticate via Connecto SSO or account credentials, your authentication tokens are encrypted with AES-256-GCM in Master Key-backed EncryptedSharedPreferences on your device's hardware security enclave. Insecure backups are permanently disabled (allowBackup=false) to prevent credential extraction."
    )

    LegalSectionCard(
        number = "3",
        title = "Local Package Querying & Zero Data Collection",
        content = "To determine if an application has an update available, Our Store queries the version code of ecosystem packages (Connecto, JARVIS AI, PDF Studio, Our Store) locally on your device against the public catalog. Your personal device inventory, contacts, location, and photos are never collected or uploaded."
    )

    LegalSectionCard(
        number = "4",
        title = "Cloudflare Anycast CDN & Encrypted Edge",
        content = "All catalog metadata and binary APK downloads are delivered over TLS 1.3 encrypted connections via Cloudflare's Anycast Edge Network. We do not sell, rent, or monetize network request logs."
    )

    LegalSectionCard(
        number = "5",
        title = "Data Retention & In-App Uninstallation",
        content = "All downloaded APK caches are ephemeral and purged immediately following installation or uninstallation. You can clear all cached data or uninstall Our Store at any time via Android Settings with instant, complete removal."
    )

    LegalSectionCard(
        number = "6",
        title = "Your Rights & Privacy Inquiries",
        content = "You retain full control over your software. If you have questions regarding security, cryptographic validation, or data governance, you may contact the core developer ecosystem at security@connecto.fun."
    )
}

@Composable
private fun LegalSectionCard(
    number: String,
    title: String,
    content: String
) {
    val theme = LocalStoreTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (theme.isDark) Color(0xFF10131B) else Color(0xFFFFFFFF))
            .border(1.dp, theme.borderHighlight, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (theme.isDark) Color(0xFF1E2433) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number,
                        color = theme.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = title,
                    color = theme.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = content,
                color = theme.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
