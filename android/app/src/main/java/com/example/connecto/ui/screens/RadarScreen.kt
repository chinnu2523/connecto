package com.example.connecto.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.ui.graphics.RadarSweepGraphic
import com.example.connecto.ui.theme.CyanGlow
import com.example.connecto.ui.theme.ElectricViolet
import com.example.connecto.ui.theme.NeonPink
import com.example.connecto.ui.theme.TextPrimaryDark
import com.example.connecto.ui.theme.TextSecondaryDark
import com.example.connecto.ui.theme.glassmorphicCard
import com.example.connecto.ui.theme.pressScaleEffect

data class RadarUser(
    val name: String,
    val role: String,
    val distance: String,
    val match: Int,
    val bio: String
)

@Composable
fun RadarScreen(
    modifier: Modifier = Modifier
) {
    val filters = listOf("All Active", "Engineers", "Designers", "Founders", "AI Creators")
    var selectedFilter by remember { mutableStateOf("All Active") }

    val sampleUsers = remember {
        listOf(
            RadarUser("Elena Vance", "AI Architect", "85m away", 98, "Building autonomous multi-agent systems and real-time spatial graph engines."),
            RadarUser("Marcus Chen", "UX Designer", "140m away", 94, "Exploring glassmorphism, adaptive fluid UI design systems and micro-interactions."),
            RadarUser("Sophia Ross", "Startup Founder", "210m away", 91, "Scaling next-generation social discovery and spatial network protocols.")
        )
    }

    var selectedUser by remember { mutableStateOf<RadarUser?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE RADAR SCANNER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanGlow
                        )
                    }
                    Text(
                        text = "Active Node Network",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryDark
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CyanGlow.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "12 NODES",
                        color = CyanGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .pressScaleEffect(onClick = { selectedFilter = filter })
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(listOf(ElectricViolet, CyanGlow))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0x3B161B26), Color(0x3B161B26)))
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else TextSecondaryDark,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Radar Visual Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .glassmorphicCard(shape = RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                RadarSweepGraphic(modifier = Modifier.fillMaxSize())

                // Clickable Radar Target Overlay Points
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(32.dp)
                        .pressScaleEffect(onClick = { selectedUser = sampleUsers[0] })
                        .clip(CircleShape)
                        .background(NeonPink.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(NeonPink)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(48.dp)
                        .pressScaleEffect(onClick = { selectedUser = sampleUsers[1] })
                        .clip(CircleShape)
                        .background(CyanGlow.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(CyanGlow)
                    )
                }
            }
        }

        // Bottom User Detail Modal Sheet
        AnimatedVisibility(
            visible = selectedUser != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        ) {
            selectedUser?.let { user ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphicCard(
                            shape = RoundedCornerShape(32.dp),
                            backgroundColor = Color(0xEC161B26),
                            borderColor = ElectricViolet,
                            borderWidth = 1.5.dp
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(ElectricViolet, NeonPink))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.first().toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = TextPrimaryDark
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = CyanGlow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = user.role,
                                        fontSize = 13.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                            }

                            IconButton(onClick = { selectedUser = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextSecondaryDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = CyanGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = user.distance,
                                    color = CyanGlow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "${user.match}% Compatibility",
                                color = NeonPink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user.bio,
                            fontSize = 13.sp,
                            color = TextSecondaryDark,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pressScaleEffect(onClick = { selectedUser = null })
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.horizontalGradient(listOf(ElectricViolet, CyanGlow)))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SEND PING SIGNAL",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
