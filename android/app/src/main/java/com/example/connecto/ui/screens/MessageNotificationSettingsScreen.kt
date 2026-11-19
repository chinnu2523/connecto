package com.example.connecto.ui.screens

import android.net.Uri
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.connecto.notifications.ContactNotificationOverride
import com.example.connecto.notifications.CustomNotificationToneManager
import com.example.connecto.notifications.NotificationToneItem
import com.example.connecto.security.SecretVaultManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val CanvasDark = Color(0xFF0A0C10)
private val CardDark = Color(0xFF13161F)
private val PillDark = Color(0xFF1C202D)
private val AccentIndigo = Color(0xFF6366F1)
private val TextWhite = Color(0xFFF8FAFC)
private val TextMuted = Color(0xFF94A3B8)
private val BorderSubtle = Color(255, 255, 255, 20)

@Composable
fun MessageNotificationSettingsScreen(
    targetContactId: String? = null,
    targetContactName: String? = null,
    targetContactAvatar: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isContactMode = !targetContactId.isNullOrBlank()

    // Contact Override State
    var contactOverride by remember(targetContactId) {
        mutableStateOf(if (isContactMode) CustomNotificationToneManager.getContactOverride(context, targetContactId!!) else null)
    }

    // Global Tone & Vibration State
    var globalToneType by remember { mutableStateOf(CustomNotificationToneManager.getGlobalToneType(context)) }
    var globalToneName by remember { mutableStateOf(CustomNotificationToneManager.getGlobalToneName(context)) }
    var globalToneUri by remember { mutableStateOf(CustomNotificationToneManager.getGlobalToneUri(context)) }
    var globalTonePath by remember { mutableStateOf(CustomNotificationToneManager.getGlobalTonePath(context)) }
    var globalVibrationId by remember { mutableStateOf(CustomNotificationToneManager.getGlobalVibrationId(context)) }

    // Overrides list for global mode
    var overridesRefreshKey by remember { mutableStateOf(0) }
    val contactOverridesList = remember(overridesRefreshKey) {
        if (!isContactMode) {
            CustomNotificationToneManager.getAllContactOverrides(context).filter {
                !SecretVaultManager.isContactHiddenInVault(context, it.contactId)
            }
        } else {
            emptyList()
        }
    }

    // System tones list
    var systemTones by remember { mutableStateOf<List<NotificationToneItem>>(emptyList()) }
    val currentlyPlayingToneId by CustomNotificationToneManager.currentlyPlayingToneId.collectAsState()

    // File processing spinner
    var isProcessingFile by remember { mutableStateOf(false) }

    // Tap-to-Record Modal State
    var showRecordModal by remember { mutableStateOf(false) }

    // Query system notification tones
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val list = CustomNotificationToneManager.getSystemNotificationTones(context)
            withContext(Dispatchers.Main) {
                systemTones = list
            }
        }
    }

    // Clean up preview audio on exit
    DisposableEffect(Unit) {
        onDispose {
            CustomNotificationToneManager.stopPreview()
        }
    }

    // Storage Access Framework audio file picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingFile = true
            coroutineScope.launch {
                val result = CustomNotificationToneManager.saveCustomToneFile(
                    context = context,
                    sourceUri = uri,
                    contactId = targetContactId
                )
                isProcessingFile = false
                result.fold(
                    onSuccess = { saveRes ->
                        if (saveRes.isLongAudio) {
                            Toast.makeText(
                                context,
                                "Tip: Audio is longer than 10 seconds. Short notification tones sound best! 🎵",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(context, "Notification tone saved: ${saveRes.displayName} 🎵", Toast.LENGTH_SHORT).show()
                        }

                        if (isContactMode && targetContactId != null) {
                            val existing = contactOverride ?: ContactNotificationOverride(
                                contactId = targetContactId,
                                contactName = targetContactName ?: targetContactId,
                                contactAvatar = targetContactAvatar
                            )
                            val updated = existing.copy(
                                toneType = CustomNotificationToneManager.TONE_TYPE_CUSTOM,
                                toneName = saveRes.displayName,
                                toneUri = "",
                                toneLocalPath = saveRes.localPath
                            )
                            CustomNotificationToneManager.saveContactOverride(context, updated)
                            contactOverride = updated
                        } else {
                            CustomNotificationToneManager.setGlobalTone(
                                context = context,
                                type = CustomNotificationToneManager.TONE_TYPE_CUSTOM,
                                name = saveRes.displayName,
                                uri = "",
                                localPath = saveRes.localPath
                            )
                            globalToneType = CustomNotificationToneManager.TONE_TYPE_CUSTOM
                            globalToneName = saveRes.displayName
                            globalTonePath = saveRes.localPath
                        }

                        // Preview newly selected tone
                        CustomNotificationToneManager.playPreview(
                            context,
                            NotificationToneItem(
                                id = "custom_preview",
                                title = saveRes.displayName,
                                uriString = saveRes.localPath,
                                type = CustomNotificationToneManager.TONE_TYPE_CUSTOM
                            )
                        )
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            context,
                            error.message ?: "Failed to set custom tone",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasDark)
    ) {
        // --- Top App Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    CustomNotificationToneManager.stopPreview()
                    onBack()
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = if (isContactMode) "Tone & Vibration" else "Messages & Sounds",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = if (isContactMode) {
                        "Personalized alerts for ${targetContactName ?: "@$targetContactId"}"
                    } else {
                        "Message notifications & haptic patterns"
                    },
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        HorizontalDivider(color = BorderSubtle, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "header_spacer") {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // =================================================================
            // CONTACT MODE: OPTION 1 - Use Global Default (Removes Override)
            // =================================================================
            if (isContactMode) {
                item(key = "contact_use_global_default") {
                    val isUsingGlobal = contactOverride == null

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardDark)
                            .border(
                                1.dp,
                                if (isUsingGlobal) AccentIndigo.copy(alpha = 0.6f) else BorderSubtle,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                targetContactId?.let { cid ->
                                    CustomNotificationToneManager.removeContactOverride(context, cid)
                                    contactOverride = null
                                    Toast.makeText(context, "Using Global Default for ${targetContactName ?: cid}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(16.dp)
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isUsingGlobal) AccentIndigo.copy(alpha = 0.2f)
                                            else Color.White.copy(alpha = 0.05f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = if (isUsingGlobal) AccentIndigo else TextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Use Global Default",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        if (isUsingGlobal) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = AccentIndigo,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Global: ${CustomNotificationToneManager.getGlobalToneName(context)} · ${CustomNotificationToneManager.getGlobalVibrationDisplayName(context)}",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // SECTION HEADER: NOTIFICATION SOUND
            // =================================================================
            item(key = "section_sound_header") {
                Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
                    Text(
                        text = "NOTIFICATION SOUND",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Auditory alert played when receiving incoming messages",
                        fontSize = 11.sp,
                        color = TextMuted.copy(alpha = 0.7f)
                    )
                }
            }

            // 1. Choose from Phone Storage Card
            item(key = "phone_storage_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardDark)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                        .clickable(enabled = !isProcessingFile) {
                            audioPickerLauncher.launch(
                                arrayOf(
                                    "audio/*",
                                    "audio/mpeg",
                                    "audio/mp4",
                                    "audio/ogg",
                                    "audio/wav",
                                    "audio/aac",
                                    "audio/flac"
                                )
                            )
                        }
                        .padding(16.dp)
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(AccentIndigo.copy(alpha = 0.35f), AccentIndigo.copy(alpha = 0.15f))
                                        )
                                    )
                                    .border(1.dp, AccentIndigo.copy(alpha = 0.45f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isProcessingFile) {
                                    CircularProgressIndicator(
                                        color = AccentIndigo,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = AccentIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Choose from Phone Storage",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Max 5 MB • Short notification sound clips",
                                    fontSize = 11.5.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentIndigo.copy(alpha = 0.15f))
                                .border(1.dp, AccentIndigo.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Browse",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentIndigo
                            )
                        }
                    }
                }
            }

            // 2. Active Custom Audio File Card (if custom sound is selected)
            val currentCustomName = if (isContactMode) {
                if (contactOverride?.toneType == CustomNotificationToneManager.TONE_TYPE_CUSTOM) contactOverride?.toneName else null
            } else {
                if (globalToneType == CustomNotificationToneManager.TONE_TYPE_CUSTOM) globalToneName else null
            }
            val currentCustomPath = if (isContactMode) {
                contactOverride?.toneLocalPath ?: ""
            } else {
                globalTonePath
            }

            if (!currentCustomName.isNullOrBlank()) {
                item(key = "active_custom_tone_card") {
                    val isPlaying = currentlyPlayingToneId == "custom_preview"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardDark)
                            .border(1.dp, AccentIndigo.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .padding(16.dp)
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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(AccentIndigo.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AudioFile,
                                        contentDescription = null,
                                        tint = AccentIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = currentCustomName,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = AccentIndigo,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Custom Audio • App Storage",
                                        fontSize = 11.5.sp,
                                        color = AccentIndigo
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            CustomNotificationToneManager.stopPreview()
                                        } else {
                                            CustomNotificationToneManager.playPreview(
                                                context,
                                                NotificationToneItem(
                                                    id = "custom_preview",
                                                    title = currentCustomName,
                                                    uriString = currentCustomPath,
                                                    type = CustomNotificationToneManager.TONE_TYPE_CUSTOM
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) AccentIndigo else Color.White.copy(alpha = 0.08f))
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Stop" else "Preview",
                                        tint = if (isPlaying) Color.White else TextWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        if (isContactMode && targetContactId != null) {
                                            val updated = contactOverride?.copy(
                                                toneType = CustomNotificationToneManager.TONE_TYPE_DEFAULT,
                                                toneName = CustomNotificationToneManager.DEFAULT_TONE_NAME,
                                                toneLocalPath = "",
                                                toneUri = ""
                                            )
                                            if (updated != null) {
                                                CustomNotificationToneManager.saveContactOverride(context, updated)
                                                contactOverride = updated
                                            }
                                        } else {
                                            CustomNotificationToneManager.setGlobalTone(
                                                context = context,
                                                type = CustomNotificationToneManager.TONE_TYPE_DEFAULT,
                                                name = CustomNotificationToneManager.DEFAULT_TONE_NAME,
                                                uri = "",
                                                localPath = ""
                                            )
                                            globalToneType = CustomNotificationToneManager.TONE_TYPE_DEFAULT
                                            globalToneName = CustomNotificationToneManager.DEFAULT_TONE_NAME
                                            globalTonePath = ""
                                        }
                                        Toast.makeText(context, "Reverted to Connecto Default tone", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red.copy(alpha = 0.12f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Connecto Default Card
            item(key = "connecto_default_card") {
                val isSelected = if (isContactMode) {
                    contactOverride != null && contactOverride?.toneType == CustomNotificationToneManager.TONE_TYPE_DEFAULT
                } else {
                    globalToneType == CustomNotificationToneManager.TONE_TYPE_DEFAULT
                }
                val isPlaying = currentlyPlayingToneId == "default_chime"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardDark)
                        .border(
                            1.dp,
                            if (isSelected) AccentIndigo.copy(alpha = 0.6f) else BorderSubtle,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            if (isContactMode && targetContactId != null) {
                                val existing = contactOverride ?: ContactNotificationOverride(
                                    contactId = targetContactId,
                                    contactName = targetContactName ?: targetContactId,
                                    contactAvatar = targetContactAvatar
                                )
                                val updated = existing.copy(
                                    toneType = CustomNotificationToneManager.TONE_TYPE_DEFAULT,
                                    toneName = CustomNotificationToneManager.DEFAULT_TONE_NAME,
                                    toneUri = "",
                                    toneLocalPath = ""
                                )
                                CustomNotificationToneManager.saveContactOverride(context, updated)
                                contactOverride = updated
                            } else {
                                CustomNotificationToneManager.setGlobalTone(
                                    context = context,
                                    type = CustomNotificationToneManager.TONE_TYPE_DEFAULT,
                                    name = CustomNotificationToneManager.DEFAULT_TONE_NAME,
                                    uri = "",
                                    localPath = ""
                                )
                                globalToneType = CustomNotificationToneManager.TONE_TYPE_DEFAULT
                                globalToneName = CustomNotificationToneManager.DEFAULT_TONE_NAME
                            }
                            CustomNotificationToneManager.playPreview(
                                context,
                                NotificationToneItem(
                                    id = "default_chime",
                                    title = CustomNotificationToneManager.DEFAULT_TONE_NAME,
                                    uriString = "",
                                    type = CustomNotificationToneManager.TONE_TYPE_DEFAULT
                                )
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) AccentIndigo.copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.05f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isSelected) AccentIndigo else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Connecto Chime",
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = TextWhite
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = AccentIndigo,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Standard Connecto incoming chime",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    CustomNotificationToneManager.stopPreview()
                                } else {
                                    CustomNotificationToneManager.playPreview(
                                        context,
                                        NotificationToneItem(
                                            id = "default_chime",
                                            title = CustomNotificationToneManager.DEFAULT_TONE_NAME,
                                            uriString = "",
                                            type = CustomNotificationToneManager.TONE_TYPE_DEFAULT
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) AccentIndigo else Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Stop" else "Preview",
                                tint = if (isPlaying) Color.White else TextWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 4. Silent (No Sound) Card
            item(key = "silent_tone_card") {
                val isSelected = if (isContactMode) {
                    contactOverride?.toneType == CustomNotificationToneManager.TONE_TYPE_SILENT
                } else {
                    globalToneType == CustomNotificationToneManager.TONE_TYPE_SILENT
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardDark)
                        .border(
                            1.dp,
                            if (isSelected) AccentIndigo.copy(alpha = 0.6f) else BorderSubtle,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            CustomNotificationToneManager.stopPreview()
                            if (isContactMode && targetContactId != null) {
                                val existing = contactOverride ?: ContactNotificationOverride(
                                    contactId = targetContactId,
                                    contactName = targetContactName ?: targetContactId,
                                    contactAvatar = targetContactAvatar
                                )
                                val updated = existing.copy(
                                    toneType = CustomNotificationToneManager.TONE_TYPE_SILENT,
                                    toneName = "Silent",
                                    toneUri = "",
                                    toneLocalPath = ""
                                )
                                CustomNotificationToneManager.saveContactOverride(context, updated)
                                contactOverride = updated
                            } else {
                                CustomNotificationToneManager.setGlobalTone(
                                    context = context,
                                    type = CustomNotificationToneManager.TONE_TYPE_SILENT,
                                    name = "Silent",
                                    uri = "",
                                    localPath = ""
                                )
                                globalToneType = CustomNotificationToneManager.TONE_TYPE_SILENT
                                globalToneName = "Silent"
                            }
                            Toast.makeText(context, "Notification sound muted (silent)", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) AccentIndigo.copy(alpha = 0.2f)
                                    else Color.White.copy(alpha = 0.05f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (isSelected) AccentIndigo else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Silent (No Sound)",
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = TextWhite
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = AccentIndigo,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Keep visual heads-up alerts & vibration active without audio",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // 5. System Notification Tones List
            item(key = "system_tones_header") {
                Text(
                    text = "SYSTEM NOTIFICATION TONES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            itemsIndexed(systemTones, key = { _, item -> "sys_${item.id}" }) { _, item ->
                val isSelected = if (isContactMode) {
                    contactOverride?.toneType == CustomNotificationToneManager.TONE_TYPE_SYSTEM &&
                            contactOverride?.toneUri == item.uriString
                } else {
                    globalToneType == CustomNotificationToneManager.TONE_TYPE_SYSTEM && globalToneUri == item.uriString
                }
                val isPlaying = currentlyPlayingToneId == item.id

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardDark)
                        .border(
                            1.dp,
                            if (isSelected) AccentIndigo.copy(alpha = 0.6f) else BorderSubtle,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            if (isContactMode && targetContactId != null) {
                                val existing = contactOverride ?: ContactNotificationOverride(
                                    contactId = targetContactId,
                                    contactName = targetContactName ?: targetContactId,
                                    contactAvatar = targetContactAvatar
                                )
                                val updated = existing.copy(
                                    toneType = CustomNotificationToneManager.TONE_TYPE_SYSTEM,
                                    toneName = item.title,
                                    toneUri = item.uriString,
                                    toneLocalPath = ""
                                )
                                CustomNotificationToneManager.saveContactOverride(context, updated)
                                contactOverride = updated
                            } else {
                                CustomNotificationToneManager.setGlobalTone(
                                    context = context,
                                    type = CustomNotificationToneManager.TONE_TYPE_SYSTEM,
                                    name = item.title,
                                    uri = item.uriString,
                                    localPath = ""
                                )
                                globalToneType = CustomNotificationToneManager.TONE_TYPE_SYSTEM
                                globalToneName = item.title
                                globalToneUri = item.uriString
                            }
                            CustomNotificationToneManager.playPreview(context, item)
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) AccentIndigo.copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.05f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = if (isSelected) AccentIndigo else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = TextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = AccentIndigo,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "System Tone",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    CustomNotificationToneManager.stopPreview()
                                } else {
                                    CustomNotificationToneManager.playPreview(context, item)
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) AccentIndigo else Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Stop" else "Preview",
                                tint = if (isPlaying) Color.White else TextWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // =================================================================
            // SECTION HEADER: TACTILE VIBRATION PATTERNS
            // =================================================================
            item(key = "section_vibration_header") {
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 2.dp)) {
                    Text(
                        text = "TACTILE VIBRATION PATTERN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Haptic feedback pattern for incoming messages (tap to feel live)",
                        fontSize = 11.sp,
                        color = TextMuted.copy(alpha = 0.7f)
                    )
                }
            }

            // Curated Presets List
            itemsIndexed(CustomNotificationToneManager.VIBRATION_PRESETS, key = { _, item -> "vib_${item.id}" }) { _, preset ->
                val isSelected = if (isContactMode) {
                    contactOverride?.vibrationPresetId == preset.id
                } else {
                    globalVibrationId == preset.id
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardDark)
                        .border(
                            1.dp,
                            if (isSelected) AccentIndigo.copy(alpha = 0.6f) else BorderSubtle,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            // Trigger live tactile haptic feedback immediately
                            CustomNotificationToneManager.triggerVibration(context, preset.pattern)

                            if (isContactMode && targetContactId != null) {
                                val existing = contactOverride ?: ContactNotificationOverride(
                                    contactId = targetContactId,
                                    contactName = targetContactName ?: targetContactId,
                                    contactAvatar = targetContactAvatar
                                )
                                val updated = existing.copy(
                                    vibrationPresetId = preset.id,
                                    customVibrationTimings = ""
                                )
                                CustomNotificationToneManager.saveContactOverride(context, updated)
                                contactOverride = updated
                            } else {
                                CustomNotificationToneManager.setGlobalVibration(context, preset.id, "")
                                globalVibrationId = preset.id
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) AccentIndigo.copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.05f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = if (isSelected) AccentIndigo else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = preset.title,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = TextWhite
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = AccentIndigo,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = preset.subtitle,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        // Test Buzz Button
                        if (preset.pattern != null) {
                            IconButton(
                                onClick = {
                                    CustomNotificationToneManager.triggerVibration(context, preset.pattern)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.06f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Feel",
                                    tint = AccentIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Create Custom Pattern (Tap-to-Record Pad Entry)
            item(key = "create_custom_vibration_card") {
                val isCustomSelected = if (isContactMode) {
                    contactOverride?.vibrationPresetId == CustomNotificationToneManager.VIBRATION_CUSTOM
                } else {
                    globalVibrationId == CustomNotificationToneManager.VIBRATION_CUSTOM
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardDark)
                        .border(
                            1.dp,
                            if (isCustomSelected) AccentIndigo.copy(alpha = 0.6f) else BorderSubtle,
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { showRecordModal = true }
                        .padding(16.dp)
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AccentIndigo.copy(alpha = 0.18f))
                                    .border(1.dp, AccentIndigo.copy(alpha = 0.45f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = AccentIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Tap Custom Pattern",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    if (isCustomSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = AccentIndigo,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isCustomSelected) "Custom rhythm active • Tap to record new" else "Record your own tactile rhythm on a glowing pulse pad",
                                    fontSize = 11.5.sp,
                                    color = if (isCustomSelected) AccentIndigo else TextMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentIndigo.copy(alpha = 0.15f))
                                .border(1.dp, AccentIndigo.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Record",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentIndigo
                            )
                        }
                    }
                }
            }

            // =================================================================
            // GLOBAL MODE: CENTRALIZED CUSTOM CONTACT NOTIFICATIONS LIST
            // =================================================================
            if (!isContactMode && contactOverridesList.isNotEmpty()) {
                item(key = "global_contact_overrides_header") {
                    Column(modifier = Modifier.padding(top = 16.dp, bottom = 2.dp)) {
                        Text(
                            text = "CUSTOM CONTACT NOTIFICATIONS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Per-friend notification tone and vibration overrides",
                            fontSize = 11.sp,
                            color = TextMuted.copy(alpha = 0.7f)
                        )
                    }
                }

                itemsIndexed(contactOverridesList, key = { _, item -> "override_${item.contactId}" }) { _, item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardDark)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                if (!item.contactAvatar.isNullOrBlank()) {
                                    AsyncImage(
                                        model = item.contactAvatar,
                                        contentDescription = item.contactName,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(AccentIndigo.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.contactName.ifBlank { item.contactId }.take(1).uppercase(),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentIndigo
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.contactName.ifBlank { "@${item.contactId}" },
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    val vibTitle = CustomNotificationToneManager.VIBRATION_PRESETS.find { it.id == item.vibrationPresetId }?.title ?: "Custom"
                                    Text(
                                        text = "${item.toneName} · $vibTitle",
                                        fontSize = 11.5.sp,
                                        color = AccentIndigo,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // One-tap ✕ Reset Button
                            IconButton(
                                onClick = {
                                    CustomNotificationToneManager.removeContactOverride(context, item.contactId)
                                    overridesRefreshKey++
                                    Toast.makeText(
                                        context,
                                        "Reverted ${item.contactName.ifBlank { item.contactId }} to global defaults",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Reset Override",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // =========================================================================
    // Tap-to-Record Modal (Glowing #6366F1 Circular Pulse Pad)
    // =========================================================================
    if (showRecordModal) {
        TapToRecordVibrationDialog(
            initialTimings = if (isContactMode) {
                contactOverride?.customVibrationTimings ?: ""
            } else {
                CustomNotificationToneManager.getGlobalCustomVibrationTimings(context)
            },
            onSave = { timings ->
                if (isContactMode && targetContactId != null) {
                    val existing = contactOverride ?: ContactNotificationOverride(
                        contactId = targetContactId,
                        contactName = targetContactName ?: targetContactId,
                        contactAvatar = targetContactAvatar
                    )
                    val updated = existing.copy(
                        vibrationPresetId = CustomNotificationToneManager.VIBRATION_CUSTOM,
                        customVibrationTimings = timings
                    )
                    CustomNotificationToneManager.saveContactOverride(context, updated)
                    contactOverride = updated
                } else {
                    CustomNotificationToneManager.setGlobalVibration(
                        context,
                        CustomNotificationToneManager.VIBRATION_CUSTOM,
                        timings
                    )
                    globalVibrationId = CustomNotificationToneManager.VIBRATION_CUSTOM
                }
                showRecordModal = false
                Toast.makeText(context, "Custom vibration pattern saved! 📳", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showRecordModal = false }
        )
    }
}

/**
 * Compact modal with a glowing #6366F1 circular pulse pad to record tactile rhythms up to 4 seconds.
 */
@Composable
fun TapToRecordVibrationDialog(
    initialTimings: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var recordingStartUptime by remember { mutableStateOf(0L) }
    var lastEventUptime by remember { mutableStateOf(0L) }
    var isPadPressed by remember { mutableStateOf(false) }

    // Recorded intervals: [delay0, duration0, delay1, duration1, ...]
    val recordedIntervals = remember { mutableStateListOf<Long>() }

    // Populate initial if available
    LaunchedEffect(initialTimings) {
        val parsed = CustomNotificationToneManager.parseTimingsString(initialTimings)
        if (parsed != null && parsed.isNotEmpty()) {
            recordedIntervals.clear()
            parsed.forEach { recordedIntervals.add(it) }
        }
    }

    val padScale by animateFloatAsState(
        targetValue = if (isPadPressed) 0.92f else 1.0f,
        label = "padScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardDark)
                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Record Vibration",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isRecording) "Recording... Tap and hold your rhythm (up to 4s)" else "Tap and hold the pad below to record your beat",
                    fontSize = 12.sp,
                    color = if (isRecording) AccentIndigo else TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Glowing #6366F1 Circular Pulse Pad
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(padScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isPadPressed) {
                                    listOf(AccentIndigo, AccentIndigo.copy(alpha = 0.4f), Color.Transparent)
                                } else {
                                    listOf(AccentIndigo.copy(alpha = 0.35f), AccentIndigo.copy(alpha = 0.12f), Color.Transparent)
                                }
                            )
                        )
                        .border(
                            width = if (isPadPressed) 3.dp else 2.dp,
                            color = if (isPadPressed) AccentIndigo else AccentIndigo.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    val now = SystemClock.uptimeMillis()
                                    isPadPressed = true

                                    // Haptic tick on touch
                                    CustomNotificationToneManager.triggerVibration(context, longArrayOf(0, 50))

                                    if (!isRecording) {
                                        isRecording = true
                                        recordingStartUptime = now
                                        lastEventUptime = now
                                        recordedIntervals.clear()
                                        recordedIntervals.add(0L) // initial delay 0
                                    } else {
                                        val gap = now - lastEventUptime
                                        recordedIntervals.add(gap)
                                    }

                                    val downTime = now
                                    tryAwaitRelease()

                                    val upNow = SystemClock.uptimeMillis()
                                    isPadPressed = false
                                    val duration = upNow - downTime
                                    recordedIntervals.add(duration)
                                    lastEventUptime = upNow

                                    // Cap at 4,000 ms total recording
                                    if (upNow - recordingStartUptime >= 4000L) {
                                        isRecording = false
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Pad",
                            tint = if (isPadPressed) Color.White else AccentIndigo,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPadPressed) "HOLDING" else "TAP HERE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPadPressed) Color.White else AccentIndigo,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Live Animated Pulse Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(PillDark)
                ) {
                    val progress = if (recordedIntervals.isNotEmpty()) {
                        val total = recordedIntervals.sum().coerceAtMost(4000L)
                        (total / 4000f).coerceIn(0f, 1f)
                    } else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = if (progress > 0f) progress else 0.02f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(AccentIndigo)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (recordedIntervals.isNotEmpty()) "${recordedIntervals.size / 2} beat(s) recorded" else "0 beats",
                    fontSize = 11.5.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Clear / Test / Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            recordedIntervals.clear()
                            isRecording = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PillDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset", color = TextMuted, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (recordedIntervals.isNotEmpty()) {
                                CustomNotificationToneManager.triggerVibration(context, recordedIntervals.toLongArray())
                            }
                        },
                        enabled = recordedIntervals.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test", color = AccentIndigo, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (recordedIntervals.isNotEmpty()) {
                                val timingsStr = recordedIntervals.joinToString(",")
                                onSave(timingsStr)
                            }
                        },
                        enabled = recordedIntervals.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Save", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
