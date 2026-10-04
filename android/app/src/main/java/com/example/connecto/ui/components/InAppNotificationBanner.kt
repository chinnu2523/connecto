package com.example.connecto.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

data class InAppNotificationData(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val avatarUrl: String? = null,
    val username: String? = null,
    val actionType: String = "chat", // "chat", "notifications", "call"
    val referenceId: String? = null
)

object InAppNotificationController {
    private val _currentNotification = MutableStateFlow<InAppNotificationData?>(null)
    val currentNotificationFlow = _currentNotification.asStateFlow()

    val currentNotification: InAppNotificationData?
        get() = _currentNotification.value

    fun show(data: InAppNotificationData) {
        _currentNotification.value = data
    }

    fun dismiss() {
        _currentNotification.value = null
    }
}

private const val DISMISS_DURATION_MS = 5000L
private const val PROGRESS_STEPS = 100

@Composable
fun InAppNotificationBanner(
    onNavigate: (actionType: String, referenceId: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val notification by InAppNotificationController.currentNotificationFlow.collectAsState()
    val current = notification

    // Progress (1f → 0f) for the countdown bar
    var progress by remember(current?.id) { mutableFloatStateOf(1f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 80),
        label = "notifProgress"
    )

    LaunchedEffect(current?.id) {
        if (current != null) {
            val stepDelayMs = DISMISS_DURATION_MS / PROGRESS_STEPS
            for (i in PROGRESS_STEPS downTo 0) {
                progress = i / PROGRESS_STEPS.toFloat()
                delay(stepDelayMs)
            }
            if (InAppNotificationController.currentNotification?.id == current.id) {
                InAppNotificationController.dismiss()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .zIndex(9999f),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = current != null,
            enter = slideInVertically(
                initialOffsetY = { -it - 60 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { -it - 60 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(animationSpec = tween(180))
        ) {
            if (current != null) {
                val resolvedAvatar = current.avatarUrl?.let { raw ->
                    if (raw.startsWith("http://") || raw.startsWith("https://")) raw
                    else "${com.example.connecto.network.ConnectoNetworkConfig.activeBaseUrl}/${raw.trimStart('/')}"
                }

                val borderGradient = Brush.linearGradient(
                    listOf(
                        Color(0xFF6C5CE7), // Brand Purple
                        Color(0xFF00CEC9)  // Chakra Cyan
                    )
                )

                val progressGradient = Brush.horizontalGradient(
                    listOf(Color(0xFF6C5CE7), Color(0xFF00CEC9))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0xFF6C5CE7).copy(alpha = 0.5f),
                            ambientColor = Color(0xFF00CEC9).copy(alpha = 0.2f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0D0E15).copy(alpha = 0.97f))
                        .border(1.2.dp, borderGradient, RoundedCornerShape(20.dp))
                        .clickable {
                            val action = current.actionType
                            val ref = current.referenceId
                            InAppNotificationController.dismiss()
                            onNavigate(action, ref)
                        }
                ) {
                    // Main content row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 10.dp, top = 14.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Profile Picture or Initial
                        if (!resolvedAvatar.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, borderGradient, CircleShape)
                            ) {
                                AsyncImage(
                                    model = resolvedAvatar,
                                    contentDescription = current.username ?: current.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            val initial = (current.username ?: current.title).trim().take(1).uppercase().ifEmpty { "C" }
                            val hue = (Math.abs(initial.hashCode()) % 360).toFloat()
                            val initialColor = Color.hsv(hue, 0.7f, 0.9f)
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(initialColor.copy(alpha = 0.22f))
                                    .border(1.5.dp, initialColor.copy(alpha = 0.65f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Notification Content
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = current.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF6C5CE7).copy(alpha = 0.28f))
                                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = "NOW",
                                        color = Color(0xFFC084FC),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = current.message,
                                color = Color(0xFFB0B7C3),
                                fontSize = 12.5.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Dismiss button
                        IconButton(
                            onClick = { InAppNotificationController.dismiss() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFF8B8FA8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Countdown progress bar at bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color.White.copy(alpha = 0.07f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = animatedProgress.coerceIn(0f, 1f))
                                .height(3.dp)
                                .background(progressGradient)
                        )
                    }
                }
            }
        }
    }
}
