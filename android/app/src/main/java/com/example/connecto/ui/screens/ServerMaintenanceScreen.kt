package com.example.connecto.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.voice.VoiceCallManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun ServerMaintenanceScreen(
    onDismissToOffline: (() -> Unit)? = null,
    onServerRestored: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCheckingStatus by remember { mutableStateOf(false) }
    var checkFeedbackText by remember { mutableStateOf<String?>(null) }
    var checkFeedbackColor by remember { mutableStateOf(Color(0xFFF59E0B)) }

    // Pulsing animations
    val infiniteTransition = rememberInfiniteTransition(label = "maintenanceAnimations")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val clockRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "clockRotate"
    )

    // Background automatic health poller (checks every 12 seconds)
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(12000L)
            val isHealthy = ConnectoApiClient.checkHealth()
            if (isHealthy) {
                VoiceCallManager.connectWebSocket()
                onServerRestored()
                break
            }
        }
    }

    val performManualCheck = {
        scope.launch {
            if (isCheckingStatus) return@launch
            isCheckingStatus = true
            checkFeedbackText = "Checking maintenance progress..."
            checkFeedbackColor = Color(0xFF60A5FA)

            val healthy = ConnectoApiClient.checkHealth()
            if (healthy) {
                checkFeedbackText = "✓ Upgrades complete! Reconnecting session..."
                checkFeedbackColor = Color(0xFF34D399)
                VoiceCallManager.connectWebSocket()
                delay(800L)
                onServerRestored()
            } else {
                checkFeedbackText = "⚙️ Scheduled maintenance is currently underway. We'll be back shortly."
                checkFeedbackColor = Color(0xFFFBBF24)
            }
            isCheckingStatus = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D13))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x264F46E5), Color(0x101E1B4B), Color.Transparent),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Brand symbol and status badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Brand Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161922))
                        .border(1.dp, Color(0x2AFFFFFF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONNECTO INFRASTRUCTURE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Animated emblem / Mechanical Clock stage
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF131722))
                        .border(1.dp, Color(0xFF272D3D), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer rotating decorative gear tick
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .rotate(clockRotate)
                            .border(1.5.dp, Color(0x336366F1), CircleShape)
                    )
                    // Core Icon
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Maintenance & Upgrades",
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Glowing Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1A818CF8))
                        .border(1.dp, Color(0x4D818CF8), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF818CF8).copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROUTINE SERVICE UPGRADE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = Color(0xFFA5B4FC)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Main Heading
                Text(
                    text = "Server is in Maintenance",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Explanatory Body Text
                Text(
                    text = "We are currently performing routine scheduled maintenance to upgrade our backend infrastructure and deploy service enhancements. Connecto will be back online shortly with improved speed and reliability.",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(26.dp))

                // System Upgrades Highlights Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF12151E))
                        .border(1.dp, Color(0xFF1E2433), RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF818CF8))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM UPGRADES IN PROGRESS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFF818CF8)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    UpgradeFeatureRow(
                        icon = Icons.Default.Speed,
                        iconTint = Color(0xFF60A5FA),
                        title = "Speed & Performance",
                        description = "Optimizing real-time message routing and low-latency networking"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    UpgradeFeatureRow(
                        icon = Icons.Default.Security,
                        iconTint = Color(0xFF34D399),
                        title = "Security & Encryption",
                        description = "Deploying enhanced end-to-end cryptographic protocols and vault protection"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    UpgradeFeatureRow(
                        icon = Icons.Default.CloudSync,
                        iconTint = Color(0xFFA78BFA),
                        title = "Platform Resilience",
                        description = "Expanding server capacity and database infrastructure"
                    )
                }

                if (checkFeedbackText != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = checkFeedbackText ?: "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = checkFeedbackColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Actions Group
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                // Button 1: Check Status & Reconnect
                Button(
                    onClick = { performManualCheck() },
                    enabled = !isCheckingStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0B0D13)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isCheckingStatus) {
                        CircularProgressIndicator(
                            color = Color(0xFF0B0D13),
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Checking Connectivity...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Check Server Status & Reconnect",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Button 2: Open Web Maintenance Page
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://connecto-web.pages.dev/maintenance")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not launch browser: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE2E8F0)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF475569)))
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "View Web Maintenance Page",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 16.sp
            )
        }
    }
}
