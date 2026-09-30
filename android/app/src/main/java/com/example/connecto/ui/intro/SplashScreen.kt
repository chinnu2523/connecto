package com.example.connecto.ui.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.BuildConfig
import com.example.connecto.R
import com.example.connecto.ui.designsystem.ConnectoRadius
import com.example.connecto.ui.designsystem.ConnectoSpacing
import com.example.connecto.ui.designsystem.LocalConnectoColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Connecto Professional Splash Screen.
 * Fast startup (under 1 second), clean brand mark, no distracting rotating halos or floating bubbles.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val colors = LocalConnectoColors.current
    val logoScale = remember { Animatable(0.85f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            contentAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
        // Swift, professional startup duration
        delay(950)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(contentAlpha.value)
        ) {
            // Clean Brand Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(logoScale.value)
                    .clip(CircleShape)
                    .background(colors.surface)
                    .border(1.dp, colors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.connecto_app_icon),
                    contentDescription = "Connecto Logo",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(ConnectoSpacing.lg))

            Text(
                text = "Connecto",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(ConnectoSpacing.xxs))

            Text(
                text = "Secure Communication Platform",
                fontSize = 13.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(ConnectoSpacing.xxl))

            // Subtle Version Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(ConnectoRadius.full))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = ConnectoSpacing.md, vertical = 4.dp)
            ) {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            }
        }
    }
}
