package com.example.connecto.ui.intro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.ui.graphics.FloatingBubblesGraphic
import com.example.connecto.ui.graphics.NodeNetworkGraphic
import com.example.connecto.ui.graphics.RadarSweepGraphic
import com.example.connecto.ui.theme.CyanGlow
import com.example.connecto.ui.theme.DeepSpaceBackground
import com.example.connecto.ui.theme.ElectricViolet
import com.example.connecto.ui.theme.NeonPink
import com.example.connecto.ui.theme.TextPrimaryDark
import com.example.connecto.ui.theme.TextSecondaryDark
import com.example.connecto.ui.theme.glassmorphicCard
import com.example.connecto.ui.theme.pressScaleEffect
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val pageType: Int // 0 = Nodes, 1 = Radar, 2 = Messaging
)

@Composable
fun OnboardingScreen(
    onOnboardingCompleted: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            title = "Instant Connections",
            subtitle = "Find like-minded people around you in real-time with zero friction.",
            pageType = 0
        ),
        OnboardingPageData(
            title = "AI Radar Discovery",
            subtitle = "Smart spatial scanning highlights active network nodes & high-compatibility matches.",
            pageType = 1
        ),
        OnboardingPageData(
            title = "Seamless Messaging",
            subtitle = "End-to-end encrypted messaging with live audio waveforms and instant pinging.",
            pageType = 2
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
    ) {
        // Floating gradient atmosphere
        FloatingBubblesGraphic(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp)
        ) {
            // Top Bar with Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONNECTO-FUN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp,
                    color = CyanGlow
                )

                TextButton(
                    onClick = onOnboardingCompleted
                ) {
                    Text(
                        text = "SKIP",
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            // Horizontal Pager Carousel
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val pageData = pages[page]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Graphic Card Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .glassmorphicCard(shape = RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (pageData.pageType) {
                            0 -> NodeNetworkGraphic(modifier = Modifier.fillMaxSize())
                            1 -> RadarSweepGraphic(modifier = Modifier.fillMaxSize())
                            2 -> MessagingPreviewGraphic()
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Title
                    Text(
                        text = pageData.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtitle
                    Text(
                        text = pageData.subtitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // Bottom Navigation Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expanding Page Indicator Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 28.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) CyanGlow else TextSecondaryDark.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Next / Get Started Gradient Button
                val isLastPage = pagerState.currentPage == pages.size - 1

                Box(
                    modifier = Modifier
                        .pressScaleEffect(
                            onClick = {
                                if (isLastPage) {
                                    onOnboardingCompleted()
                                } else {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            }
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isLastPage) listOf(ElectricViolet, NeonPink) else listOf(ElectricViolet, CyanGlow)
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLastPage) "GET STARTED" else "NEXT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isLastPage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Graphic layout preview for Onboarding Slide 3 (Messaging).
 */
@Composable
private fun MessagingPreviewGraphic() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Chat bubble left
        Box(
            modifier = Modifier
                .align(Alignment.Start)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                .background(ElectricViolet.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Hey! Connected on Radar nearby 👋",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chat bubble right
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                .background(CyanGlow.copy(alpha = 0.85f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Awesome! Let's collaborate 🚀",
                color = DeepSpaceBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Animated typing card
        Box(
            modifier = Modifier
                .align(Alignment.Start)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = null,
                    tint = CyanGlow,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Alex is typing...",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }
        }
    }
}
