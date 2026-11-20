package com.example.connecto.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

enum class CropMode {
    AVATAR,
    BANNER
}

enum class BannerAspectRatio(val label: String, val ratio: Float) {
    STANDARD("16:9", 16f / 9f),
    ULTRAWIDE("2.4:1", 2.4f),
    PANORAMIC("3:1", 3.0f)
}

/**
 * Immersive Minimal Lightbox to inspect Cover Photos or Avatars.
 * Features deep #05060A backdrop, edge-to-edge transparent top bar,
 * frameless media preview, native pinch/pan/double-tap gestures, swipe-down dismiss,
 * floating zoom pill, and spacious bottom action dock.
 */
@Composable
fun PhotoLightboxDialog(
    title: String,
    imageUrl: String?,
    placeholderInitial: String = "C",
    isBanner: Boolean = false,
    onChangePhoto: (() -> Unit)? = null,
    onRemovePhoto: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            securePolicy = com.example.connecto.security.ScreenSecurityManager.getSecureFlagPolicy()
        )
    ) {
        var zoomScale by remember { mutableFloatStateOf(1.0f) }
        var panOffset by remember { mutableStateOf(Offset.Zero) }
        var dismissDragY by remember { mutableFloatStateOf(0f) }
        var showRemoveConfirmDialog by remember { mutableStateOf(false) }

        var lastZoomInteractionTime by remember { mutableStateOf(0L) }
        var showZoomIndicator by remember { mutableStateOf(false) }

        LaunchedEffect(lastZoomInteractionTime) {
            if (lastZoomInteractionTime > 0L) {
                showZoomIndicator = true
                kotlinx.coroutines.delay(800L)
                showZoomIndicator = false
            }
        }

        val context = androidx.compose.ui.platform.LocalContext.current
        val lightboxImageRequest = remember(imageUrl, context) {
            if (imageUrl.isNullOrBlank() || com.example.connecto.ui.designsystem.isCoverPreset(imageUrl)) null
            else {
                val model = com.example.connecto.ui.designsystem.resolveImageModel(imageUrl) ?: imageUrl
                ImageRequest.Builder(context)
                    .data(model)
                    .memoryCacheKey(imageUrl)
                    .diskCacheKey(imageUrl)
                    .crossfade(false)
                    .build()
            }
        }

        val backdropAlpha = (1f - (dismissDragY / 500f)).coerceIn(0.25f, 1f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF05060A).copy(alpha = backdropAlpha))
        ) {
            // Ambient subtle glow behind media preview
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.12f * backdropAlpha),
                                Color(0xFF0F172A).copy(alpha = 0.06f * backdropAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Top Bar: Clean, edge-to-edge transparent top bar over #05060A
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // 40px x 40px circular translucent close button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Center Viewport: Native Mobile Gestures
            // Double-tap (1.0x <-> 2.5x), pinch-to-zoom (1.0x to 4.0x), drag-panning while zoomed,
            // swipe-down-to-dismiss (proportional background fade)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 72.dp, bottom = 96.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (zoomScale > 1.2f) {
                                    zoomScale = 1.0f
                                    panOffset = Offset.Zero
                                } else {
                                    zoomScale = 2.5f
                                }
                                lastZoomInteractionTime = System.currentTimeMillis()
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newZoom = (zoomScale * zoom).coerceIn(1.0f, 4.0f)
                            if (newZoom != zoomScale) {
                                zoomScale = newZoom
                                lastZoomInteractionTime = System.currentTimeMillis()
                            }
                            if (zoomScale > 1.05f) {
                                panOffset += pan
                            } else {
                                panOffset = Offset.Zero
                            }
                        }
                    }
                    .pointerInput(zoomScale) {
                        if (zoomScale <= 1.05f) {
                            detectDragGestures(
                                onDragEnd = {
                                    if (dismissDragY > 120f) {
                                        onDismiss()
                                    } else {
                                        dismissDragY = 0f
                                    }
                                },
                                onDragCancel = {
                                    dismissDragY = 0f
                                },
                                onDrag = { change: PointerInputChange, dragAmount: Offset ->
                                    change.consume()
                                    if (dragAmount.y > 0 || dismissDragY > 0) {
                                        dismissDragY = (dismissDragY + dragAmount.y).coerceAtLeast(0f)
                                    }
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val isPreset = com.example.connecto.ui.designsystem.isCoverPreset(imageUrl)
                val presetObj = com.example.connecto.ui.designsystem.getCoverPreset(imageUrl)

                val mediaTransformModifier = Modifier.graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                    translationX = panOffset.x
                    translationY = if (zoomScale > 1.05f) panOffset.y else dismissDragY
                }

                if (isPreset && presetObj != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .aspectRatio(16f / 9f)
                            .then(mediaTransformModifier)
                            .shadow(20.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(presetObj.brush)
                            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.50f))
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = presetObj.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = presetObj.subtitle,
                                color = Color.White.copy(alpha = 0.80f),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else if (lightboxImageRequest != null) {
                    if (isBanner) {
                        // Background Cover: 16px horizontal margin, 16:9 aspect ratio, 16px radius, ContentScale.Crop filling 100%
                        AsyncImage(
                            model = lightboxImageRequest,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .aspectRatio(16f / 9f)
                                .then(mediaTransformModifier)
                                .shadow(20.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                        )
                    } else {
                        // Profile Photo: centered at 82% screen width as a crisp 1:1 circle with 1px rgba(255,255,255,0.14) hairline
                        AsyncImage(
                            model = lightboxImageRequest,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth(0.82f)
                                .aspectRatio(1f)
                                .then(mediaTransformModifier)
                                .shadow(24.dp, CircleShape, spotColor = Color(0x336366F1), ambientColor = Color(0x22000000))
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                        )
                    }
                } else {
                    // Fallback placeholder circle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .aspectRatio(1f)
                            .then(mediaTransformModifier)
                            .shadow(24.dp, CircleShape, spotColor = Color(0x336366F1))
                            .clip(CircleShape)
                            .background(Color(0xFF1E212B))
                            .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = placeholderInitial,
                            color = Color.White,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Minimal Floating Zoom Indicator Pill (e.g. 180%) near bottom, fading out 800ms after release
            AnimatedVisibility(
                visible = showZoomIndicator,
                enter = fadeIn(androidx.compose.animation.core.tween(150)),
                exit = fadeOut(androidx.compose.animation.core.tween(300)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF12141F).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "${(zoomScale * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Spacious Bottom Action Bar: Clean floating pill dock (height: 56px, 12px gaps, nowrap full labels)
            if (onChangePhoto != null || onRemovePhoto != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Secondary Destructive Button: "Remove" (whenever onRemovePhoto is provided)
                        if (onRemovePhoto != null) {
                            Button(
                                onClick = { showRemoveConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x29EF4444),
                                    contentColor = Color(0xFFF87171)
                                ),
                                border = BorderStroke(1.dp, Color(0x40EF4444)),
                                shape = RoundedCornerShape(28.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Remove",
                                    color = Color(0xFFF87171),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        // Primary Button: "Change Photo" (Solid White Button with Black Icon/Text)
                        if (onChangePhoto != null) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    onChangePhoto()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(28.dp),
                                modifier = Modifier
                                    .weight(if (onRemovePhoto != null) 1.25f else 1f)
                                    .height(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBanner) "Change Cover" else "Change Photo",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Quick Confirmation Dialog before deleting
            if (showRemoveConfirmDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showRemoveConfirmDialog = false },
                    title = {
                        Text(
                            text = if (isBanner) "Remove Cover Photo?" else "Remove Profile Photo?",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = if (isBanner) {
                                "Your cover photo will be reverted to the default banner."
                            } else {
                                "Your profile photo will be removed and revert to your initial."
                            },
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    },
                    containerColor = Color(0xFF141724),
                    confirmButton = {
                        Button(
                            onClick = {
                                showRemoveConfirmDialog = false
                                onDismiss()
                                onRemovePhoto?.invoke()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Remove", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showRemoveConfirmDialog = false }
                        ) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                )
            }
        }
    }
}

/**
 * Ultra-aesthetic Cover Presets Dialog: Choose from curated presets matching the web app.
 */
@Composable
fun CoverPresetsDialog(
    currentPresetId: String?,
    onPresetSelected: (com.example.connecto.ui.designsystem.CoverPreset) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            securePolicy = com.example.connecto.security.ScreenSecurityManager.getSecureFlagPolicy()
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0F111A),
            tonalElevation = 8.dp,
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .border(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.35f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6C5CE7).copy(alpha = 0.20f))
                                .border(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.40f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFFA29BFE),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Cover Presets",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Select an aesthetic gradient banner",
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.60f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable presets list
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(com.example.connecto.ui.designsystem.COVER_PRESETS.size) { idx ->
                        val preset = com.example.connecto.ui.designsystem.COVER_PRESETS[idx]
                        val isSelected = currentPresetId == preset.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(preset.brush)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFA29BFE) else Color.White.copy(alpha = 0.20f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    onPresetSelected(preset)
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.45f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = preset.name,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = preset.subtitle,
                                        fontSize = 10.5.sp,
                                        color = Color.White.copy(alpha = 0.80f)
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF6C5CE7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom / Center Modal Dialog for Cover Photo Options:
 * - Choose from Gallery (launches photo picker)
 * - Cover Presets (opens presets browser)
 * - Remove Cover Photo (resets to default gradient)
 */
@Composable
fun CoverOptionsMenuDialog(
    hasExistingCover: Boolean,
    onChooseGallery: () -> Unit,
    onOpenPresets: () -> Unit,
    onRemoveCover: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            securePolicy = com.example.connecto.security.ScreenSecurityManager.getSecureFlagPolicy()
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0F111A),
            tonalElevation = 8.dp,
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .border(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.35f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6C5CE7).copy(alpha = 0.20f))
                                .border(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.40f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFFA29BFE),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Cover Photo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Personalize your profile header banner",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.70f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.10f))

                // Option 1: Gallery Upload
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onChooseGallery()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF3B82F6).copy(alpha = 0.20f))
                                .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.40f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Choose from Gallery",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Interactive zoom, pan, and custom aspect crop",
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.60f)
                            )
                        }
                    }
                }

                // Option 2: Presets
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onOpenPresets()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFA855F7).copy(alpha = 0.20f))
                                .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.40f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cover Presets",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Aesthetic themes: Aurora, Space, Cyberpunk...",
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.60f)
                            )
                        }
                    }
                }

                // Option 3: Remove Cover Photo (if has existing cover)
                if (hasExistingCover) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.30f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onRemoveCover()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.20f))
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.40f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Remove Cover Photo",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFCA5A5)
                                )
                                Text(
                                    text = "Reset to default gradient profile header",
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.50f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Crop & Fit Dialog with high customization:
 * - Zoom In and Zoom OUT (down to 0.35f to fit full photos without forced cropping)
 * - Fine-tune zoom slider with live percentage
 * - Quick "Fit All" and "Fill Frame" presets
 * - Rotate 90° button to orient phone photos
 * - Aspect ratio selection for covers (16:9, 2.4:1, 3:1)
 * - Canvas Matrix bitmap rendering matching exact screen frame
 */
@Composable
fun InteractiveCropFitDialog(
    imageUri: Uri,
    cropMode: CropMode,
    onCropConfirmed: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingBitmap by remember { mutableStateOf(true) }
    var isProcessingCrop by remember { mutableStateOf(false) }

    // User transformation states: zoom out down to 0.35f!
    var userScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var selectedAspectRatio by remember { mutableStateOf(BannerAspectRatio.STANDARD) }

    // Load Bitmap safely from Uri
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                val bmp = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = if (imageUri.scheme == "file") {
                            ImageDecoder.createSource(java.io.File(imageUri.path ?: ""))
                        } else {
                            ImageDecoder.createSource(context.contentResolver, imageUri)
                        }
                        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.isMutableRequired = true
                        }
                    } else {
                        context.contentResolver.openInputStream(imageUri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                } catch (e: Exception) {
                    try {
                        context.contentResolver.openInputStream(imageUri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    } catch (e2: Exception) {
                        if (imageUri.path != null) {
                            BitmapFactory.decodeFile(imageUri.path)
                        } else {
                            null
                        }
                    }
                }
                originalBitmap = bmp
            } catch (e: Exception) {
                originalBitmap = null
            } finally {
                isLoadingBitmap = false
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isProcessingCrop) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isProcessingCrop,
            dismissOnClickOutside = !isProcessingCrop,
            securePolicy = com.example.connecto.security.ScreenSecurityManager.getSecureFlagPolicy()
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0C0D12)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header with title and controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (cropMode == CropMode.AVATAR) "Customize Avatar" else "Customize Cover Photo",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (cropMode == CropMode.AVATAR) "Pinch, zoom out, or drag to position" else "Adjust frame, zoom out, or rotate",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotate 90° button
                        IconButton(
                            onClick = {
                                rotationAngle = (rotationAngle + 90f) % 360f
                            },
                            enabled = !isProcessingCrop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "Rotate 90 degrees",
                                tint = Color(0xFFA29BFE),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isProcessingCrop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Interactive Crop Area
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val canvasW = constraints.maxWidth.toFloat()
                    val canvasH = constraints.maxHeight.toFloat()

                    // Calculate Frame Rect based on mode and aspect ratio
                    val frameRect = remember(canvasW, canvasH, cropMode, selectedAspectRatio) {
                        if (cropMode == CropMode.AVATAR) {
                            val frameSize = min(canvasW, canvasH) * 0.78f
                            val left = (canvasW - frameSize) / 2f
                            val top = (canvasH - frameSize) / 2f
                            Rect(left, top, left + frameSize, top + frameSize)
                        } else {
                            val frameW = canvasW * 0.92f
                            val targetRatio = selectedAspectRatio.ratio
                            val frameH = (frameW / targetRatio).coerceAtMost(canvasH * 0.65f)
                            val left = (canvasW - frameW) / 2f
                            val top = (canvasH - frameH) / 2f
                            Rect(left, top, left + frameW, top + frameH)
                        }
                    }

                    val bmp = originalBitmap
                    if (bmp != null) {
                        val isQuarterTurn = (rotationAngle.toInt() % 180 != 0)
                        val effectiveSrcW = if (isQuarterTurn) bmp.height.toFloat() else bmp.width.toFloat()
                        val effectiveSrcH = if (isQuarterTurn) bmp.width.toFloat() else bmp.height.toFloat()

                        // Base scale to cover the frame
                        val baseCoverScale = max(frameRect.width / effectiveSrcW, frameRect.height / effectiveSrcH)
                        val currentTotalScale = baseCoverScale * userScale

                        // Layer 1: Transformed Image with pinch, pan & rotation
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        // Allow zooming out down to 0.35f!
                                        userScale = (userScale * zoom).coerceIn(0.35f, 4.5f)
                                        panOffset += pan
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Editable Photo",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .size(
                                        width = (bmp.width.toFloat() * baseCoverScale / LocalContext.current.resources.displayMetrics.density).dp,
                                        height = (bmp.height.toFloat() * baseCoverScale / LocalContext.current.resources.displayMetrics.density).dp
                                    )
                                    .graphicsLayer {
                                        scaleX = userScale
                                        scaleY = userScale
                                        rotationZ = rotationAngle
                                        translationX = panOffset.x
                                        translationY = panOffset.y
                                    }
                            )
                        }

                        // Layer 2: Mask & Guidelines
                        ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                            val cutoutPath = Path().apply {
                                fillType = PathFillType.EvenOdd
                                addRect(Rect(0f, 0f, size.width, size.height))
                                if (cropMode == CropMode.AVATAR) {
                                    addOval(frameRect)
                                } else {
                                    addRoundRect(RoundRect(frameRect, CornerRadius(16f, 16f)))
                                }
                            }
                            drawPath(cutoutPath, color = Color.Black.copy(alpha = 0.75f))

                            // Highlight border
                            if (cropMode == CropMode.AVATAR) {
                                drawCircle(
                                    color = Color(0xFF6C5CE7),
                                    radius = frameRect.width / 2f,
                                    center = frameRect.center,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            } else {
                                drawRoundRect(
                                    color = Color(0xFF6C5CE7),
                                    topLeft = frameRect.topLeft,
                                    size = frameRect.size,
                                    cornerRadius = CornerRadius(16f, 16f),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }

                            // 3x3 Rule-of-Thirds Grid
                            val stepX = frameRect.width / 3f
                            val stepY = frameRect.height / 3f
                            val gridColor = Color.White.copy(alpha = 0.22f)
                            val gridStroke = Stroke(width = 1.dp.toPx())

                            drawLine(
                                color = gridColor,
                                start = Offset(frameRect.left + stepX, frameRect.top),
                                end = Offset(frameRect.left + stepX, frameRect.bottom),
                                strokeWidth = gridStroke.width
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(frameRect.left + stepX * 2, frameRect.top),
                                end = Offset(frameRect.left + stepX * 2, frameRect.bottom),
                                strokeWidth = gridStroke.width
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(frameRect.left, frameRect.top + stepY),
                                end = Offset(frameRect.right, frameRect.top + stepY),
                                strokeWidth = gridStroke.width
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(frameRect.left, frameRect.top + stepY * 2),
                                end = Offset(frameRect.right, frameRect.top + stepY * 2),
                                strokeWidth = gridStroke.width
                            )
                        }
                    } else if (isLoadingBitmap) {
                        CircularProgressIndicator(
                            color = Color(0xFF6C5CE7),
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Text(
                            text = "Failed to load selected photo",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }

                    if (isProcessingCrop) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color(0xFF6C5CE7),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Rendering high-res photo...",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Aspect Ratio Selector Chips (for BANNER mode)
                if (cropMode == CropMode.BANNER) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF11131C))
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ratio:",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        BannerAspectRatio.values().forEach { aspect ->
                            val isSelected = selectedAspectRatio == aspect
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0xFF6C5CE7) else Color.White.copy(alpha = 0.08f)
                                    )
                                    .clickable { selectedAspectRatio = aspect }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = aspect.label,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Bottom Control & Zoom Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF141620))
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Action Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fit Whole Photo (Fit All)
                        TextButton(
                            onClick = {
                                val bmp = originalBitmap ?: return@TextButton
                                val isQuarterTurn = (rotationAngle.toInt() % 180 != 0)
                                val effW = if (isQuarterTurn) bmp.height.toFloat() else bmp.width.toFloat()
                                val effH = if (isQuarterTurn) bmp.width.toFloat() else bmp.height.toFloat()
                                // Fit inside frame
                                val frameW = 800f
                                val frameH = if (cropMode == CropMode.AVATAR) 800f else (800f / selectedAspectRatio.ratio)
                                val coverScale = max(frameW / effW, frameH / effH)
                                val fitScale = min(frameW / effW, frameH / effH)
                                userScale = (fitScale / coverScale).coerceIn(0.35f, 4.0f)
                                panOffset = Offset.Zero
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitScreen,
                                contentDescription = "Fit All",
                                tint = Color(0xFFA29BFE),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fit Whole Photo", color = Color(0xFFA29BFE), fontSize = 12.sp)
                        }

                        // Fill Frame (Cover)
                        TextButton(
                            onClick = {
                                userScale = 1.0f
                                panOffset = Offset.Zero
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = "Fill",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fill Frame", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }

                        // Live Zoom Percentage Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${(userScale * 100).toInt()}%",
                                color = Color(0xFFA29BFE),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Zoom Slider Row with - / + buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = { userScale = (userScale - 0.15f).coerceAtLeast(0.35f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Slider(
                            value = userScale,
                            onValueChange = { userScale = it },
                            valueRange = 0.35f..4.0f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6C5CE7),
                                activeTrackColor = Color(0xFF6C5CE7),
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )

                        IconButton(
                            onClick = { userScale = (userScale + 0.15f).coerceAtMost(4.0f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Confirm / Cancel Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            enabled = !isProcessingCrop,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.08f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                val bmp = originalBitmap ?: return@Button
                                isProcessingCrop = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        // Target output dimensions
                                        val targetW = if (cropMode == CropMode.AVATAR) 512 else 1200
                                        val targetH = if (cropMode == CropMode.AVATAR) {
                                            512
                                        } else {
                                            (1200f / selectedAspectRatio.ratio).toInt()
                                        }

                                        val outputBmp = Bitmap.createBitmap(
                                            targetW,
                                            targetH,
                                            Bitmap.Config.ARGB_8888
                                        )
                                        val canvas = Canvas(outputBmp)

                                        // Dark clean background fill (for zoomed out letterbox)
                                        canvas.drawColor(android.graphics.Color.rgb(15, 16, 21))

                                        val isQuarterTurn = (rotationAngle.toInt() % 180 != 0)
                                        val effSrcW = if (isQuarterTurn) bmp.height.toFloat() else bmp.width.toFloat()
                                        val effSrcH = if (isQuarterTurn) bmp.width.toFloat() else bmp.height.toFloat()

                                        // Calculate exact scale factor for target canvas
                                        val targetBaseCover = max(targetW.toFloat() / effSrcW, targetH.toFloat() / effSrcH)
                                        val finalScale = targetBaseCover * userScale

                                        // Map user's screen pan translation to target bitmap coordinates
                                        // On phone screen, frameRect was width W_f
                                        val panRatioX = panOffset.x / 400f
                                        val panRatioY = panOffset.y / 400f

                                        val targetCenterX = (targetW / 2f) + (panRatioX * targetW * 0.5f)
                                        val targetCenterY = (targetH / 2f) + (panRatioY * targetH * 0.5f)

                                        val matrix = Matrix().apply {
                                            // 1. Center image at origin
                                            postTranslate(-bmp.width / 2f, -bmp.height / 2f)
                                            // 2. Rotate
                                            postRotate(rotationAngle)
                                            // 3. Scale
                                            postScale(finalScale, finalScale)
                                            // 4. Translate to target center
                                            postTranslate(targetCenterX, targetCenterY)
                                        }

                                        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
                                        canvas.drawBitmap(bmp, matrix, paint)

                                        withContext(Dispatchers.Main) {
                                            isProcessingCrop = false
                                            onCropConfirmed(outputBmp)
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            isProcessingCrop = false
                                            onDismiss()
                                        }
                                    }
                                }
                            },
                            enabled = originalBitmap != null && !isProcessingCrop,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6C5CE7),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Save & Apply",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
