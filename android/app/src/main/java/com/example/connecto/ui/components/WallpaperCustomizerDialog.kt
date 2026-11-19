package com.example.connecto.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.connecto.data.WallpaperTransform
import com.example.connecto.ui.screens.AVAILABLE_CHAT_WALLPAPERS
import com.example.connecto.ui.screens.ChatWallpaperOption

/**
 * High-performance, clean, self-evident Chat Wallpaper Customizer Sheet/Dialog.
 * Optimized with deferred draw passes (120 FPS gesture tracking) and zero UI clutter.
 */
@Composable
fun WallpaperCustomizerDialog(
    wallpaperId: String,
    customWallpaperUri: String?,
    initialTransform: WallpaperTransform,
    isDarkTheme: Boolean = true,
    friendName: String? = null,
    isDirectChat: Boolean = false,
    onDismissRequest: () -> Unit,
    onPickCustomPhoto: () -> Unit = {},
    onApply: ((newWallpaperId: String, newCustomUri: String?, newTransform: WallpaperTransform, isGlobal: Boolean, isShared: Boolean) -> Unit)? = null,
    onSave: ((WallpaperTransform) -> Unit)? = null
) {
    var selectedWallpaperId by remember { mutableStateOf(wallpaperId) }
    var selectedCustomUri by remember(customWallpaperUri) { mutableStateOf(customWallpaperUri) }

    var scale by remember { mutableFloatStateOf(initialTransform.scale) }
    var offsetX by remember { mutableFloatStateOf(initialTransform.offsetX) }
    var offsetY by remember { mutableFloatStateOf(initialTransform.offsetY) }
    var mode by remember { mutableStateOf(initialTransform.contentScaleMode) }
    var dim by remember { mutableFloatStateOf(initialTransform.dim) }
    var blur by remember { mutableFloatStateOf(initialTransform.blur) }
    var applyToAllChats by remember { mutableStateOf(false) }
    var shareWithFriend by remember { mutableStateOf(false) }

    var activeTab by remember {
        mutableIntStateOf(if (wallpaperId == "custom" && !customWallpaperUri.isNullOrBlank()) 1 else 0)
    }

    val presetWallpaper = remember(selectedWallpaperId) {
        AVAILABLE_CHAT_WALLPAPERS.find { it.id == selectedWallpaperId } ?: AVAILABLE_CHAT_WALLPAPERS.first()
    }

    val contentScale = remember(mode) {
        when (mode) {
            "fit" -> ContentScale.Fit
            "fill_bounds" -> ContentScale.FillBounds
            "inside" -> ContentScale.Inside
            else -> ContentScale.Crop
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDarkTheme) Color(0xFF0A0C10) else MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // ================= TOP BAR =================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkTheme) Color(0xFF13161F) else MaterialTheme.colorScheme.surface)
                        .border(1.dp, if (isDarkTheme) Color(0x14FFFFFF) else MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismissRequest) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Wallpaper",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(
                            onClick = {
                                selectedWallpaperId = wallpaperId
                                selectedCustomUri = customWallpaperUri
                                scale = 1.0f
                                offsetX = 0f
                                offsetY = 0f
                                mode = "crop"
                                dim = if (isDarkTheme) 0.50f else 0.35f
                                blur = 0f
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                modifier = Modifier.size(15.dp),
                                tint = Color(0xFF6366F1)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reset",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6366F1)
                            )
                        }
                    }
                }

                // ================= SCROLLABLE BODY =================
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ================= 1. LIVE PREVIEW VIEWPORT =================
                    WallpaperLivePreview(
                        selectedWallpaperId = selectedWallpaperId,
                        selectedCustomUri = selectedCustomUri,
                        presetWallpaper = presetWallpaper,
                        contentScale = contentScale,
                        scaleProvider = { scale },
                        offsetXProvider = { offsetX },
                        offsetYProvider = { offsetY },
                        dimProvider = { dim },
                        blurProvider = { blur },
                        isDarkTheme = isDarkTheme,
                        onGestureTransform = { pan, zoom ->
                            scale = (scale * zoom).coerceIn(0.5f, 3.0f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    )

                    // ================= 2. SEGMENTED TABS =================
                    val tabs = listOf(
                        Triple(0, "Presets", Icons.Default.Wallpaper),
                        Triple(1, "Photos", Icons.Default.AddPhotoAlternate),
                        Triple(2, "Adjust", Icons.Default.Tune)
                    )

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF11141D))
                            .border(1.dp, Color(0x0FFFFFFF), RoundedCornerShape(12.dp))
                            .padding(3.dp)
                    ) {
                        val tabWidth = maxWidth / tabs.size
                        val animatedPillOffset by animateDpAsState(
                            targetValue = tabWidth * activeTab,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = 400f
                            ),
                            label = "wallpaperTabSlidingPill"
                        )

                        // Sliding background pill indicator
                        Box(
                            modifier = Modifier
                                .offset(x = animatedPillOffset)
                                .width(tabWidth)
                                .height(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(Color(0xFF1F2433))
                                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(9.dp))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tabs.forEach { (index, label, icon) ->
                                val isSelected = activeTab == index
                                val textColor by animateColorAsState(
                                    targetValue = if (isSelected) Color.White else Color(0xFF8E95A8),
                                    animationSpec = tween(200),
                                    label = "wpTabTextColor"
                                )
                                val iconColor by animateColorAsState(
                                    targetValue = if (isSelected) Color.White else Color(0xFF8E95A8),
                                    animationSpec = tween(200),
                                    label = "wpTabIconColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { activeTab = index },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = iconColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ================= TAB 0: PRESET WALLPAPERS =================
                    if (activeTab == 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            AVAILABLE_CHAT_WALLPAPERS.chunked(2).forEach { rowPair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowPair.forEach { wallpaper ->
                                        val isSelected = selectedWallpaperId == wallpaper.id
                                        val cardInteraction = remember { MutableInteractionSource() }
                                        val isCardPressed by cardInteraction.collectIsPressedAsState()
                                        val cardScale by animateFloatAsState(
                                            targetValue = if (isCardPressed) 0.97f else 1.0f,
                                            animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
                                            label = "wpPresetCardScale"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .graphicsLayer {
                                                    scaleX = cardScale
                                                    scaleY = cardScale
                                                }
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(Color(0xFF12151E))
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFF6366F1) else Color(0x0FFFFFFF),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                                .clickable(
                                                    interactionSource = cardInteraction,
                                                    indication = null
                                                ) {
                                                    selectedWallpaperId = wallpaper.id
                                                    selectedCustomUri = null
                                                }
                                                .padding(8.dp)
                                        ) {
                                            Column {
                                                // Concentric swatch: R_outer (18) - Padding (8) = R_inner (10)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(68.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (!isDarkTheme) wallpaper.lightBrush else wallpaper.darkBrush)
                                                )

                                                Text(
                                                    text = wallpaper.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(top = 10.dp, start = 6.dp, bottom = 4.dp, end = 6.dp)
                                                )
                                            }

                                            // 20px checkmark badge anchored at top: 6px, right: 6px
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(top = 6.dp, end = 6.dp)
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF6366F1)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (rowPair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // ================= TAB 1: CUSTOM GALLERY PHOTO =================
                    if (activeTab == 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Select Photo Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                    .clickable { onPickCustomPhoto() }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Choose Photo",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Select",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }

                            // Active Photo Preview Card
                            if (!selectedCustomUri.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            AsyncImage(
                                                model = Uri.parse(selectedCustomUri),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(width = 46.dp, height = 36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Active Photo",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFDC2626).copy(alpha = 0.15f))
                                                .clickable {
                                                    selectedCustomUri = null
                                                    selectedWallpaperId = "default"
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Photo",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ================= TAB 2: ADJUST & BLUR =================
                    if (activeTab == 2) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Fit Mode Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "crop" to "Fill",
                                    "fit" to "Fit",
                                    "fill_bounds" to "Stretch",
                                    "inside" to "Center"
                                ).forEach { (mKey, mLabel) ->
                                    val isSelected = mode == mKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
                                            )
                                            .clickable { mode = mKey }
                                            .padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Dimming Scrim Slider
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Opacity,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Dim", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Text(text = "${(dim * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = dim,
                                    onValueChange = { dim = it },
                                    valueRange = 0.0f..0.85f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = Color(0xFF262936)
                                    )
                                )
                            }

                            // Frosted Glass Blur Slider
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Blur", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Text(text = "${blur.toInt()} dp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = blur,
                                    onValueChange = { blur = it },
                                    valueRange = 0f..20f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = Color(0xFF262936)
                                    )
                                )
                            }

                            // Zoom Slider
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Zoom", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Text(text = "${(scale * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Slider(
                                    value = scale,
                                    onValueChange = { scale = it },
                                    valueRange = 0.5f..3.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = Color(0xFF262936)
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    listOf(0.75f to "75%", 1.0f to "100%", 1.5f to "150%", 2.0f to "200%").forEach { (sVal, sLabel) ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (kotlin.math.abs(scale - sVal) < 0.05f) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
                                                )
                                                .clickable { scale = sVal }
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = sLabel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (kotlin.math.abs(scale - sVal) < 0.05f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Pan Alignment Quick Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    Triple(0f, 0f, "Center"),
                                    Triple(0f, -150f, "Top"),
                                    Triple(0f, 150f, "Bottom"),
                                    Triple(-120f, 0f, "Left"),
                                    Triple(120f, 0f, "Right")
                                ).forEach { (px, py, pLabel) ->
                                    val isMatch = kotlin.math.abs(offsetX - px) < 5f && kotlin.math.abs(offsetY - py) < 5f
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isMatch) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
                                            )
                                            .clickable {
                                                offsetX = px
                                                offsetY = py
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pLabel,
                                            fontSize = 10.5.sp,
                                            fontWeight = if (isMatch) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isMatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ================= BOTTOM ACTIONS FOOTER =================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkTheme) Color(0xFF0A0C10) else MaterialTheme.colorScheme.surface)
                        .border(1.dp, if (isDarkTheme) Color(0x14FFFFFF) else MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Grouped Obsidian Surface Card for Toggles
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF12151E))
                                .border(1.dp, Color(0x0FFFFFFF), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                // Set for all chats toggle switch row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val next = !applyToAllChats
                                            applyToAllChats = next
                                            if (next) shareWithFriend = false
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Set for all chats",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Use this wallpaper across all your direct chats",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8E95A8)
                                        )
                                    }
                                    Switch(
                                        checked = applyToAllChats,
                                        onCheckedChange = { checked ->
                                            applyToAllChats = checked
                                            if (checked) shareWithFriend = false
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF6366F1),
                                            uncheckedThumbColor = Color(0xFF94A3B8),
                                            uncheckedTrackColor = Color(0xFF1E2333),
                                            uncheckedBorderColor = Color.Transparent
                                        ),
                                        modifier = Modifier.scale(0.82f)
                                    )
                                }

                                // Share with [Contact Name] toggle switch row (Direct chats only)
                                if (isDirectChat && !friendName.isNullOrBlank()) {
                                    // Vertical divider
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0x0FFFFFFF))
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val next = !shareWithFriend
                                                shareWithFriend = next
                                                if (next) applyToAllChats = false
                                            }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Share with $friendName",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Apply this background for both of you",
                                                fontSize = 11.sp,
                                                color = Color(0xFF8E95A8)
                                            )
                                        }
                                        Switch(
                                            checked = shareWithFriend,
                                            onCheckedChange = { checked ->
                                                shareWithFriend = checked
                                                if (checked) applyToAllChats = false
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF6366F1),
                                                uncheckedThumbColor = Color(0xFF94A3B8),
                                                uncheckedTrackColor = Color(0xFF1E2333),
                                                uncheckedBorderColor = Color.Transparent
                                            ),
                                            modifier = Modifier.scale(0.82f)
                                        )
                                    }
                                }
                            }
                        }

                        // Footer Buttons: Sleek #1A1E2B Cancel pill + #6366F1 Indigo primary button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1A1E2B))
                                    .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                                    .clickable { onDismissRequest() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            val applyInteraction = remember { MutableInteractionSource() }
                            val isApplyPressed by applyInteraction.collectIsPressedAsState()
                            val applyScale by animateFloatAsState(
                                targetValue = if (isApplyPressed) 0.96f else 1.0f,
                                animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
                                label = "wpApplyScale"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(44.dp)
                                    .graphicsLayer {
                                        scaleX = applyScale
                                        scaleY = applyScale
                                    }
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF6366F1))
                                    .clickable(
                                        interactionSource = applyInteraction,
                                        indication = null
                                    ) {
                                        val updated = WallpaperTransform(
                                            scale = scale,
                                            offsetX = offsetX,
                                            offsetY = offsetY,
                                            contentScaleMode = mode,
                                            dim = dim,
                                            blur = blur
                                        )
                                        onApply?.invoke(selectedWallpaperId, selectedCustomUri, updated, applyToAllChats, shareWithFriend)
                                        onSave?.invoke(updated)
                                        onDismissRequest()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Apply",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
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

/**
 * Isolated live wallpaper preview viewport.
 * Uses deferred lambdas to avoid recomposing the parent dialog during gesture pan/zoom.
 * Frameless rounded preview card (border-radius: 20px, height: ~180px, subtle 1px solid rgba(255, 255, 255, 0.08) hairline)
 * showing live incoming/outgoing sample bubbles over the selected wallpaper with smooth 220ms crossfade transitions.
 */
@Composable
private fun WallpaperLivePreview(
    selectedWallpaperId: String,
    selectedCustomUri: String?,
    presetWallpaper: ChatWallpaperOption,
    contentScale: ContentScale,
    scaleProvider: () -> Float,
    offsetXProvider: () -> Float,
    offsetYProvider: () -> Float,
    dimProvider: () -> Float,
    blurProvider: () -> Float,
    isDarkTheme: Boolean,
    onGestureTransform: (pan: Offset, zoom: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp))
            .background(Color(0xFF0A0C10))
    ) {
        // Wallpaper Layer with 220ms crossfade transitions
        Crossfade(
            targetState = Pair(selectedWallpaperId, selectedCustomUri),
            animationSpec = tween(durationMillis = 220),
            label = "wallpaperLiveCrossfade"
        ) { (wpId, customUri) ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            onGestureTransform(pan, zoom)
                        }
                    }
            ) {
                if (wpId == "custom" && !customUri.isNullOrBlank()) {
                    val currentBlur = blurProvider()
                    AsyncImage(
                        model = Uri.parse(customUri),
                        contentDescription = null,
                        contentScale = contentScale,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scaleProvider()
                                scaleY = scaleProvider()
                                translationX = offsetXProvider()
                                translationY = offsetYProvider()
                            }
                            .then(
                                if (currentBlur > 0f) Modifier.blur(currentBlur.dp) else Modifier
                            )
                    )
                } else {
                    val activePreset = remember(wpId) {
                        AVAILABLE_CHAT_WALLPAPERS.find { it.id == wpId } ?: presetWallpaper
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scaleProvider()
                                scaleY = scaleProvider()
                                translationX = offsetXProvider()
                                translationY = offsetYProvider()
                            }
                            .background(
                                if (!isDarkTheme) activePreset.lightBrush else activePreset.darkBrush
                            )
                    )
                }

                // Scrim Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            val currentDim = dimProvider()
                            val scrimColor = if (!isDarkTheme) Color.White.copy(alpha = currentDim * 0.85f)
                            else Color.Black.copy(alpha = currentDim)
                            drawRect(scrimColor)
                        }
                )
            }
        }

        // Realistic Mock Chat Bubbles Overlay (Incoming + Outgoing)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Sample Incoming Bubble
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(end = 36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155))
                        .border(1.dp, Color(0x1FFFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 4.dp))
                        .background(Color(0xFF1E2333).copy(alpha = 0.94f))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 4.dp))
                        .padding(horizontal = 11.dp, vertical = 7.dp)
                ) {
                    Column {
                        Text(
                            text = "Hey! Check out this wallpaper ✨",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = "10:42 AM",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 2.dp)
                        )
                    }
                }
            }

            // Sample Outgoing Bubble
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(start = 36.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 4.dp))
                    .background(Color(0xFF6366F1).copy(alpha = 0.95f))
                    .padding(horizontal = 11.dp, vertical = 7.dp)
            ) {
                Column {
                    Text(
                        text = "Looks super clean and high craft! 🚀",
                        fontSize = 11.5.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "10:43 AM",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
