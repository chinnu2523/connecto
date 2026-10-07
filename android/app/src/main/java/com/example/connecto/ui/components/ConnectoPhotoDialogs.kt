package com.example.connecto.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

enum class CropMode {
    AVATAR,
    BANNER
}

/**
 * Fullscreen Lightbox Modal to view Avatar or Cover Photo in high resolution.
 */
@Composable
fun PhotoLightboxDialog(
    title: String,
    imageUrl: String?,
    placeholderInitial: String = "C",
    isBanner: Boolean = false,
    onChangePhoto: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            securePolicy = androidx.compose.ui.window.SecureFlagPolicy.SecureOn
        )
    ) {
        var zoomScale by remember { mutableFloatStateOf(1.0f) }
        var panOffset by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBanner) "Cover Banner Preview" else "Profile Avatar Preview",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            // Center Image Viewer with Pinch & Pan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 80.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.8f, 4.5f)
                            panOffset += pan
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = if (isBanner) ContentScale.FillWidth else ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(if (isBanner) 0.95f else 0.85f)
                            .then(
                                if (isBanner) Modifier.height(200.dp) else Modifier.size(320.dp)
                            )
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                                clip = !isBanner
                                if (!isBanner) {
                                    shape = CircleShape
                                }
                            }
                            .then(
                                if (!isBanner) Modifier.clip(CircleShape) else Modifier.clip(RoundedCornerShape(16.dp))
                            )
                            .border(
                                width = 2.dp,
                                color = Color(0xFF6C5CE7).copy(alpha = 0.6f),
                                shape = if (!isBanner) CircleShape else RoundedCornerShape(16.dp)
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22252F)),
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

            // Bottom Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        zoomScale = 1.0f
                        panOffset = Offset.Zero
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Zoom",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Zoom", fontSize = 13.sp)
                }

                if (onChangePhoto != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onChangePhoto()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C5CE7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Photo", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Interactive Crop & Fit Dialog with pan/zoom gestures, interactive slider,
 * cutout guidelines, and high-fidelity bitmap rendering.
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

    var userScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Load Bitmap safely from Uri
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    context.contentResolver.openInputStream(imageUri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
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
            dismissOnClickOutside = !isProcessingCrop
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F1015)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (cropMode == CropMode.AVATAR) "Crop Profile Photo" else "Crop Cover Photo",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (cropMode == CropMode.AVATAR) "Pinch or drag to fit your avatar" else "Adjust frame for 3:1 cover banner",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }

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
                            tint = Color.White
                        )
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

                    // Calculate Frame Rect
                    val frameRect = remember(canvasW, canvasH, cropMode) {
                        if (cropMode == CropMode.AVATAR) {
                            val frameSize = min(canvasW, canvasH) * 0.78f
                            val left = (canvasW - frameSize) / 2f
                            val top = (canvasH - frameSize) / 2f
                            Rect(left, top, left + frameSize, top + frameSize)
                        } else {
                            val frameW = canvasW * 0.92f
                            val frameH = frameW / 3f
                            val left = (canvasW - frameW) / 2f
                            val top = (canvasH - frameH) / 2f
                            Rect(left, top, left + frameW, top + frameH)
                        }
                    }

                    val bmp = originalBitmap
                    if (bmp != null) {
                        val srcW = bmp.width.toFloat()
                        val srcH = bmp.height.toFloat()
                        val baseScale = max(frameRect.width / srcW, frameRect.height / srcH)
                        val currentTotalScale = baseScale * userScale

                        // Layer 1: The Transformed Image
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        userScale = (userScale * zoom).coerceIn(1.0f, 4.5f)
                                        panOffset += pan
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Source Image",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .size(
                                        width = (srcW * currentTotalScale / (LocalContext.current.resources.displayMetrics.density)).dp,
                                        height = (srcH * currentTotalScale / (LocalContext.current.resources.displayMetrics.density)).dp
                                    )
                                    .graphicsLayer {
                                        translationX = panOffset.x
                                        translationY = panOffset.y
                                    }
                            )
                        }

                        // Layer 2: Cutout Mask Overlay & Grid Guidelines
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cutoutPath = Path().apply {
                                fillType = PathFillType.EvenOdd
                                addRect(Rect(0f, 0f, size.width, size.height))
                                if (cropMode == CropMode.AVATAR) {
                                    addOval(frameRect)
                                } else {
                                    addRoundRect(RoundRect(frameRect, CornerRadius(16f, 16f)))
                                }
                            }
                            drawPath(cutoutPath, color = Color.Black.copy(alpha = 0.72f))

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

                            // Subtle 3x3 Grid Lines inside crop area
                            val stepX = frameRect.width / 3f
                            val stepY = frameRect.height / 3f
                            val gridColor = Color.White.copy(alpha = 0.25f)
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
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color(0xFF6C5CE7),
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Processing & cropping photo...",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Bottom Control & Zoom Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF14161F))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Zoom Slider Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { userScale = (userScale - 0.2f).coerceAtLeast(1.0f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Slider(
                            value = userScale,
                            onValueChange = { userScale = it },
                            valueRange = 1.0f..4.0f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6C5CE7),
                                activeTrackColor = Color(0xFF6C5CE7),
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )

                        IconButton(
                            onClick = { userScale = (userScale + 0.2f).coerceAtMost(4.0f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Reset / Center Button
                        TextButton(
                            onClick = {
                                userScale = 1.0f
                                panOffset = Offset.Zero
                            }
                        ) {
                            Text(
                                text = "Fit",
                                color = Color(0xFF6C5CE7),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
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
                                        val srcW = bmp.width.toFloat()
                                        val srcH = bmp.height.toFloat()

                                        // Frame Rect calculations
                                        val canvasW = bmp.width.coerceAtLeast(800).toFloat()
                                        val canvasH = bmp.height.coerceAtLeast(800).toFloat()

                                        // Target dimensions
                                        val targetW = if (cropMode == CropMode.AVATAR) 512 else 1200
                                        val targetH = if (cropMode == CropMode.AVATAR) 512 else 400

                                        // Calculate source crop window
                                        val effectiveScale = max(1.0f, userScale)
                                        val cropW = (srcW / effectiveScale).coerceIn(10f, srcW)
                                        val cropH = (if (cropMode == CropMode.AVATAR) cropW else cropW / 3f).coerceIn(10f, srcH)

                                        // Apply pan translation inverted
                                        val panRatioX = (panOffset.x / (canvasW * 0.5f)).coerceIn(-0.5f, 0.5f)
                                        val panRatioY = (panOffset.y / (canvasH * 0.5f)).coerceIn(-0.5f, 0.5f)

                                        val centerX = (srcW / 2f) - (panRatioX * srcW)
                                        val centerY = (srcH / 2f) - (panRatioY * srcH)

                                        val left = (centerX - cropW / 2f).coerceIn(0f, srcW - cropW)
                                        val top = (centerY - cropH / 2f).coerceIn(0f, srcH - cropH)

                                        val croppedRaw = Bitmap.createBitmap(
                                            bmp,
                                            left.toInt(),
                                            top.toInt(),
                                            cropW.toInt().coerceAtMost(bmp.width - left.toInt()),
                                            cropH.toInt().coerceAtMost(bmp.height - top.toInt())
                                        )

                                        val scaledCropped = Bitmap.createScaledBitmap(
                                            croppedRaw,
                                            targetW,
                                            targetH,
                                            true
                                        )

                                        withContext(Dispatchers.Main) {
                                            isProcessingCrop = false
                                            onCropConfirmed(scaledCropped)
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
                                    imageVector = Icons.Default.Crop,
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
