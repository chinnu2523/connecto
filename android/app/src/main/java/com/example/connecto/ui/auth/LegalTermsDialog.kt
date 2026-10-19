package com.example.connecto.ui.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors

/**
 * Modern in-app legal dialog rendering Terms of Use and Privacy Policy
 * with segmented tab switching, full policy sections, and external browser link.
 */
@Composable
fun LegalTermsDialog(
    initialTab: Int = 0, // 0 = Terms of Use, 1 = Privacy Policy
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    val context = LocalContext.current
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()
    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    val scrollState = rememberScrollState()

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
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF090A0F)) // Pure OLED jet black base
                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(gradientColors)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Outlined.Security else Icons.Outlined.Lock,
                                contentDescription = "Legal Policy",
                                tint = contentOnGradient,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (selectedTab == 0) "Terms of Use" else "Privacy Policy",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Connecto Network Platform Agreement",
                                fontSize = 11.5.sp,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF18181B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFA1A1AA),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Tab Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF18181B))
                        .padding(4.dp)
                ) {
                    // Terms Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedTab == 0) Brush.linearGradient(gradientColors)
                                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { selectedTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Terms of Use",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) contentOnGradient else Color(0xFFA1A1AA)
                        )
                    }

                    // Privacy Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selectedTab == 1) Brush.linearGradient(gradientColors)
                                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { selectedTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Privacy Policy",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) contentOnGradient else Color(0xFFA1A1AA)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Document Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF12131A))
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    ) {
                        if (selectedTab == 0) {
                            // TERMS OF USE CONTENT
                            LegalBadge(text = "Official Legal Agreement • Public Beta Testing • 2026")
                            Spacer(modifier = Modifier.height(8.dp))

                            LegalSection(
                                number = "01",
                                title = "Agreement to Terms & Public Beta Testing Stage",
                                content = "These Terms of Use constitute a legally binding agreement between you and Connecto Network (\"Connecto\"). Connecto is currently in an active Public Beta Testing Stage. By accessing connecto.fun, creating an account, or downloading the Connecto Android client, you agree to comply with all terms herein and acknowledge that features, edge sync mechanisms, and audio mesh protocols are actively tested and optimized."
                            )

                            LegalSection(
                                number = "02",
                                title = "Age Requirement (18+ Strictly Enforced)",
                                content = "Connecto is engineered exclusively for adults. You certify that you are at least 18 years old upon creating an account. Any account discovered to belong to or operated by an individual under the age of 18 will be permanently deleted without notice."
                            )

                            LegalSection(
                                number = "03",
                                title = "Beta Testing Disclaimer & Service Availability",
                                content = "Connecto is provided on an \"AS-IS\" and \"AS-AVAILABLE\" basis during the Public Beta Testing phase without commercial advertisements or paywalls. While we maintain 99.9% uptime, edge database quotas, background updates, or protocol iterations may occur. Connecto disclaims liability for indirect, incidental, or consequential damages resulting from beta service updates."
                            )

                            LegalSection(
                                number = "04",
                                title = "User Conduct & Community Safety",
                                content = "You agree not to deploy automated denial-of-service bots, transmit malware or exploit payloads, harass or intimidate other members, harvest personal data, or attempt unauthorized access to other users' private rooms, messages, or vaults. Ethical security testing is only permitted through authorized channels."
                            )

                            LegalSection(
                                number = "05",
                                title = "Account Security & Authentication",
                                content = "You are solely responsible for maintaining the confidentiality of your account credentials, sessions, and hardware biometric keys. Multi-factor authentication via Resend email OTP and Connecto Vault device biometrics are strongly encouraged to safeguard access."
                            )

                            LegalSection(
                                number = "06",
                                title = "Ownership & Intellectual Property",
                                content = "You retain 100% intellectual property rights to all messages, code, artifacts, and media you share on Connecto. By posting in public channels, you grant Connecto only the technical license needed to distribute and render your content to participants."
                            )

                            LegalSection(
                                number = "07",
                                title = "Account Deletion & Data Purge",
                                content = "You may delete your account and all associated messages at any time directly through your profile settings or by contacting legal@connecto.fun. Account deletion permanently and irrecoverably wipes your database records."
                            )
                        } else {
                            // PRIVACY POLICY CONTENT
                            LegalBadge(text = "GDPR & CCPA Compliant • Public Beta Privacy Standard")
                            Spacer(modifier = Modifier.height(8.dp))

                            LegalSection(
                                number = "01",
                                title = "Zero Data Selling Guarantee & Beta Privacy",
                                content = "Connecto Network does not sell, rent, monetize, or trade your personal data, messages, voice audio, or browsing habits to advertisers, data brokers, or third parties. We have zero advertising trackers or behavioral pixels. In this Public Beta Testing Stage, data processing is strictly limited to essential core service delivery and crash debugging."
                            )

                            LegalSection(
                                number = "02",
                                title = "Data Minimization & What We Collect",
                                content = "We practice strict data minimization. We only collect the minimal credentials necessary to operate your account: chosen username, display name, public bio, email address for 2FA / OTP verification, and hashed passwords salted via modern cryptographic algorithms (Argon2id)."
                            )

                            LegalSection(
                                number = "03",
                                title = "Ephemeral WebRTC Voice & Calls",
                                content = "All spatial audio voice lounges and one-to-one voice calls are powered by WebRTC mesh connections and Opus 96kHz audio relays. Voice packets are completely ephemeral; we do not record, transcribe, store, or train machine learning models on your conversations."
                            )

                            LegalSection(
                                number = "04",
                                title = "Hardware Vault & Local Biometrics",
                                content = "Connecto Vault utilizes native Android BiometricPrompt and hardware keystores (Fingerprint / Face Unlock / PIN). Your biometric templates never leave your local device and are never transmitted to or stored on any server."
                            )

                            LegalSection(
                                number = "05",
                                title = "Cryptographic Security in Transit & at Rest",
                                content = "All client-to-server and client-to-edge traffic is encrypted via TLS 1.3 with modern cipher suites (ChaCha20-Poly1305 and AES-256-GCM). Data at rest in Cloudflare D1 and NVMe storage engines is cryptographically secured."
                            )

                            LegalSection(
                                number = "06",
                                title = "Your Rights (GDPR & CCPA)",
                                content = "Under European GDPR and California CCPA regulations, you have full ownership over your information: Right to Access your data, Right to Rectification of inaccurate details, and Right to Erasure (\"Right to be Forgotten\")."
                            )

                            LegalSection(
                                number = "07",
                                title = "Data Protection & Beta Feedback Inquiries",
                                content = "For privacy requests, audit reports, beta feedback, or data deletion inquiries, contact our data protection team directly at legal@connecto.fun, privacy@connecto.fun, or support@connecto.fun."
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Open in Browser Link
                    OutlinedButton(
                        onClick = {
                            val url = if (selectedTab == 0) "https://connecto.fun/terms" else "https://connecto.fun/privacy"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFA1A1AA)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "Open Web Version",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Web Version",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // I Accept Button
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gradientColors.first(),
                            contentColor = contentOnGradient
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Agree and Accept",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Agree & Accept",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegalBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x1A10B981))
            .border(1.dp, Color(0x4010B981), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF10B981)
        )
    }
}

@Composable
private fun LegalSection(
    number: String,
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x2600E5FF))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = number,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
            color = Color(0xFFD4D4D8)
        )
    }
}
