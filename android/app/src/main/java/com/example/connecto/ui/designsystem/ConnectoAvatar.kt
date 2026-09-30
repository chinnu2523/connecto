package com.example.connecto.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

enum class ConnectoAvatarSize(val dp: Dp, val fontSize: Int, val statusDotSize: Dp) {
    XS(24.dp, 10, 6.dp),
    SM(32.dp, 12, 8.dp),
    MD(40.dp, 15, 10.dp),
    LG(48.dp, 18, 12.dp),
    XL(64.dp, 22, 14.dp),
    XXL(80.dp, 28, 16.dp)
}

enum class ConnectoPresenceStatus {
    ONLINE,
    IDLE,
    DND,
    OFFLINE
}

/**
 * Reusable Connecto Avatar component adhering to the Centralized Design System.
 * Renders avatar image with graceful initials fallback and accessible presence indicator.
 */
@Composable
fun ConnectoAvatar(
    name: String,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    size: ConnectoAvatarSize = ConnectoAvatarSize.MD,
    status: ConnectoPresenceStatus? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalConnectoColors.current
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C"

    val statusColor = when (status) {
        ConnectoPresenceStatus.ONLINE -> colors.success
        ConnectoPresenceStatus.IDLE -> colors.warning
        ConnectoPresenceStatus.DND -> colors.error
        ConnectoPresenceStatus.OFFLINE, null -> colors.textDisabled
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(colors.primaryContainer)
                .border(1.dp, colors.borderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = colors.onPrimaryContainer,
                fontSize = size.fontSize.sp,
                fontWeight = FontWeight.Bold
            )
            if (!avatarUrl.isNullOrBlank()) {
                val fullUrl = if (avatarUrl.startsWith("/")) "https://connecto.fun$avatarUrl" else avatarUrl
                AsyncImage(
                    model = fullUrl,
                    contentDescription = "$name's avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (status != null) {
            Box(
                modifier = Modifier
                    .size(size.statusDotSize)
                    .clip(CircleShape)
                    .background(statusColor)
                    .border(1.5.dp, colors.surface, CircleShape)
            )
        }
    }
}
