package com.example.connecto.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.connecto.security.SecretVaultManager
import com.example.connecto.voice.CallRingtoneManager
import com.example.connecto.voice.ContactRingtoneSetting
import com.example.connecto.voice.RingtoneOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val CanvasDark = Color(0xFF0A0C10)
private val CardDark = Color(0xFF13161F)
private val AccentIndigo = Color(0xFF6366F1)
private val TextWhite = Color(0xFFF8FAFC)
private val TextMuted = Color(0xFF94A3B8)
private val BorderSubtle = Color(255, 255, 255, 20)

@Composable
fun CallRingtonePickerScreen(
    targetContactId: String? = null,
    targetContactName: String? = null,
    targetContactAvatar: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isContactMode = !targetContactId.isNullOrBlank()

    // Global state
    var selectedType by remember { mutableStateOf(CallRingtoneManager.getSelectedRingtoneType(context)) }
    var selectedName by remember { mutableStateOf(CallRingtoneManager.getSelectedRingtoneName(context)) }
    var selectedUri by remember { mutableStateOf(CallRingtoneManager.getSelectedRingtoneUri(context)) }
    var isProcessingFile by remember { mutableStateOf(false) }

    // Contact override state (if in contact mode)
    var contactSetting by remember(targetContactId) {
        mutableStateOf(if (isContactMode) CallRingtoneManager.getContactRingtoneSetting(context, targetContactId!!) else null)
    }

    // Refresh trigger for global overrides list
    var overridesRefreshKey by remember { mutableStateOf(0) }
    val contactOverrides = remember(overridesRefreshKey) {
        if (!isContactMode) {
            CallRingtoneManager.getAllContactRingtoneSettings(context).filter { setting ->
                !SecretVaultManager.isContactHiddenInVault(context, setting.contactId)
            }
        } else {
            emptyList()
        }
    }

    var systemRingtones by remember { mutableStateOf<List<RingtoneOption>>(emptyList()) }
    val currentlyPlayingId by CallRingtoneManager.currentlyPlayingPreviewId.collectAsState()

    // Query system ringtones asynchronously
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val list = CallRingtoneManager.getSystemRingtones(context)
            withContext(Dispatchers.Main) {
                systemRingtones = list
            }
        }
    }

    // Stop audio previews immediately on navigating back
    DisposableEffect(Unit) {
        onDispose {
            CallRingtoneManager.stopPreview()
        }
    }

    // Storage Access Framework audio file picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingFile = true
            coroutineScope.launch {
                if (targetContactId != null) {
                    val result = CallRingtoneManager.saveContactCustomRingtone(
                        context = context,
                        contactId = targetContactId,
                        contactName = targetContactName ?: targetContactId,
                        contactAvatar = targetContactAvatar,
                        sourceUri = uri
                    )
                    isProcessingFile = false
                    result.fold(
                        onSuccess = { fileName ->
                            contactSetting = CallRingtoneManager.getContactRingtoneSetting(context, targetContactId)
                            Toast.makeText(context, "Custom ringtone saved for ${targetContactName ?: targetContactId}: $fileName 🎵", Toast.LENGTH_SHORT).show()
                            contactSetting?.let { setting ->
                                CallRingtoneManager.playPreview(
                                    context,
                                    RingtoneOption(
                                        id = "contact_custom_preview",
                                        title = setting.displayName,
                                        uriString = setting.localPath,
                                        type = CallRingtoneManager.RINGTONE_TYPE_CUSTOM
                                    )
                                )
                            }
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                error.message ?: "Failed to set custom ringtone",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                } else {
                    val result = CallRingtoneManager.saveCustomRingtone(context, uri)
                    isProcessingFile = false
                    result.fold(
                        onSuccess = { fileName ->
                            selectedType = CallRingtoneManager.RINGTONE_TYPE_CUSTOM
                            selectedName = fileName
                            selectedUri = CallRingtoneManager.getSelectedRingtoneUri(context)
                            Toast.makeText(context, "Custom ringtone saved: $fileName 🎵", Toast.LENGTH_SHORT).show()
                            CallRingtoneManager.playPreview(
                                context,
                                RingtoneOption(
                                    id = "custom_ringtone",
                                    title = fileName,
                                    uriString = selectedUri,
                                    type = CallRingtoneManager.RINGTONE_TYPE_CUSTOM
                                )
                            )
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                error.message ?: "Failed to set custom ringtone",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasDark)
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    CallRingtoneManager.stopPreview()
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
                    text = if (isContactMode) "Custom Ringtone" else "Call Ringtone",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = if (isContactMode) {
                        "Personalized alert for ${targetContactName ?: "@$targetContactId"}"
                    } else {
                        "Incoming voice call sound alert"
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
                Spacer(modifier = Modifier.height(6.dp))
            }

            // =================================================================
            // CONTACT MODE: OPTION 1 - Use Global Default (Removes Override)
            // =================================================================
            if (isContactMode) {
                item(key = "contact_global_default_card") {
                    val isUsingGlobal = contactSetting == null
                    val isPlaying = currentlyPlayingId == "contact_default_preview"

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
                                    CallRingtoneManager.removeContactRingtoneOverride(context, cid)
                                    contactSetting = null
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
                                        text = "Standard call ringtone: ${CallRingtoneManager.getSelectedRingtoneName(context)}",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Preview global ringtone
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        CallRingtoneManager.stopPreview()
                                    } else {
                                        val gType = CallRingtoneManager.getSelectedRingtoneType(context)
                                        val gUri = CallRingtoneManager.getSelectedRingtoneUri(context)
                                        CallRingtoneManager.playPreview(
                                            context,
                                            RingtoneOption(
                                                id = "contact_default_preview",
                                                title = CallRingtoneManager.getSelectedRingtoneName(context),
                                                uriString = gUri,
                                                type = gType
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
                        }
                    }
                }
            }

            // =================================================================
            // CHOOSE FROM PHONE STORAGE (Both Modes)
            // =================================================================
            item(key = "phone_storage_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardDark)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
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
                        .padding(18.dp)
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
                                    .size(46.dp)
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
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = AccentIndigo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Choose from Phone Storage",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isContactMode) "Assign custom audio file to this friend" else "Pick any MP3, M4A, OGG, WAV, AAC, FLAC",
                                    fontSize = 12.sp,
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

            // =================================================================
            // CONTACT MODE: ACTIVE CUSTOM OVERRIDE CARD
            // =================================================================
            if (isContactMode && contactSetting?.sourceType == CallRingtoneManager.RINGTONE_TYPE_CUSTOM) {
                item(key = "active_contact_custom_card") {
                    val setting = contactSetting!!
                    val isPlaying = currentlyPlayingId == "contact_custom_preview"

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
                                            text = setting.displayName,
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
                                        text = "Active Contact Audio • App Storage",
                                        fontSize = 11.5.sp,
                                        color = AccentIndigo
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            CallRingtoneManager.stopPreview()
                                        } else {
                                            CallRingtoneManager.playPreview(
                                                context,
                                                RingtoneOption(
                                                    id = "contact_custom_preview",
                                                    title = setting.displayName,
                                                    uriString = setting.localPath,
                                                    type = CallRingtoneManager.RINGTONE_TYPE_CUSTOM
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
                                        targetContactId?.let { cid ->
                                            CallRingtoneManager.removeContactRingtoneOverride(context, cid)
                                            contactSetting = null
                                            Toast.makeText(context, "Reverted to Global Default", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red.copy(alpha = 0.12f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove Override",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // GLOBAL MODE: ACTIVE GLOBAL CUSTOM RINGTONE CARD
            // =================================================================
            if (!isContactMode && selectedType == CallRingtoneManager.RINGTONE_TYPE_CUSTOM) {
                item(key = "active_custom_ringtone_card") {
                    val isPlaying = currentlyPlayingId == "custom_ringtone"
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
                                            text = selectedName,
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
                                        text = "Active Custom Ringtone • App Storage",
                                        fontSize = 11.5.sp,
                                        color = AccentIndigo
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            CallRingtoneManager.stopPreview()
                                        } else {
                                            CallRingtoneManager.playPreview(
                                                context,
                                                RingtoneOption(
                                                    id = "custom_ringtone",
                                                    title = selectedName,
                                                    uriString = selectedUri,
                                                    type = CallRingtoneManager.RINGTONE_TYPE_CUSTOM
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
                                        CallRingtoneManager.resetToDefault(context)
                                        selectedType = CallRingtoneManager.RINGTONE_TYPE_DEFAULT
                                        selectedName = CallRingtoneManager.DEFAULT_RINGTONE_NAME
                                        selectedUri = ""
                                        Toast.makeText(context, "Reverted to Connecto Default ringtone", Toast.LENGTH_SHORT).show()
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

            // =================================================================
            // GLOBAL MODE: CONNECTO DEFAULT
            // =================================================================
            if (!isContactMode) {
                item(key = "default_ringtone_card") {
                    val isSelected = selectedType == CallRingtoneManager.RINGTONE_TYPE_DEFAULT
                    val isPlaying = currentlyPlayingId == "default_ringtone"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardDark)
                            .border(
                                1.dp,
                                if (isSelected) AccentIndigo.copy(alpha = 0.6f) else BorderSubtle,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                CallRingtoneManager.resetToDefault(context)
                                selectedType = CallRingtoneManager.RINGTONE_TYPE_DEFAULT
                                selectedName = CallRingtoneManager.DEFAULT_RINGTONE_NAME
                                selectedUri = ""
                                CallRingtoneManager.playPreview(
                                    context,
                                    RingtoneOption(
                                        id = "default_ringtone",
                                        title = CallRingtoneManager.DEFAULT_RINGTONE_NAME,
                                        uriString = "",
                                        type = CallRingtoneManager.RINGTONE_TYPE_DEFAULT
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
                                        .size(38.dp)
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Connecto Default",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        if (isSelected) {
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
                                        text = "Built-in incoming call alert",
                                        fontSize = 11.5.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        CallRingtoneManager.stopPreview()
                                    } else {
                                        CallRingtoneManager.playPreview(
                                            context,
                                            RingtoneOption(
                                                id = "default_ringtone",
                                                title = CallRingtoneManager.DEFAULT_RINGTONE_NAME,
                                                uriString = "",
                                                type = CallRingtoneManager.RINGTONE_TYPE_DEFAULT
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
                        }
                    }
                }
            }

            // =================================================================
            // SYSTEM RINGTONES SECTION (Both Modes)
            // =================================================================
            item(key = "system_section_header") {
                Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                    Text(
                        text = "SYSTEM RINGTONES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Phone manufacturer ringtones installed on your device",
                        fontSize = 11.sp,
                        color = TextMuted.copy(alpha = 0.7f)
                    )
                }
            }

            itemsIndexed(systemRingtones, key = { _, item -> item.id }) { _, ringtone ->
                val isSelected = if (isContactMode) {
                    contactSetting?.sourceType == CallRingtoneManager.RINGTONE_TYPE_SYSTEM &&
                            contactSetting?.systemUri == ringtone.uriString
                } else {
                    selectedType == CallRingtoneManager.RINGTONE_TYPE_SYSTEM && selectedUri == ringtone.uriString
                }
                val isPlaying = currentlyPlayingId == ringtone.id

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
                            if (targetContactId != null) {
                                CallRingtoneManager.saveContactSystemRingtone(
                                    context = context,
                                    contactId = targetContactId,
                                    contactName = targetContactName ?: targetContactId,
                                    contactAvatar = targetContactAvatar,
                                    systemTitle = ringtone.title,
                                    systemUri = ringtone.uriString
                                )
                                contactSetting = CallRingtoneManager.getContactRingtoneSetting(context, targetContactId)
                                Toast.makeText(context, "Set ${ringtone.title} for ${targetContactName ?: targetContactId}", Toast.LENGTH_SHORT).show()
                            } else {
                                CallRingtoneManager.setRingtoneSetting(
                                    context = context,
                                    type = CallRingtoneManager.RINGTONE_TYPE_SYSTEM,
                                    name = ringtone.title,
                                    uri = ringtone.uriString
                                )
                                selectedType = CallRingtoneManager.RINGTONE_TYPE_SYSTEM
                                selectedName = ringtone.title
                                selectedUri = ringtone.uriString
                            }
                            CallRingtoneManager.playPreview(context, ringtone)
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
                                        text = ringtone.title,
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
                                    text = "System Ringtone",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    CallRingtoneManager.stopPreview()
                                } else {
                                    CallRingtoneManager.playPreview(context, ringtone)
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
            // GLOBAL MODE: CENTRALIZED CUSTOM CONTACT RINGTONES SECTION
            // =================================================================
            if (!isContactMode && contactOverrides.isNotEmpty()) {
                item(key = "contact_overrides_header") {
                    Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                        Text(
                            text = "CUSTOM CONTACT RINGTONES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Individual ringtone overrides configured for specific friends",
                            fontSize = 11.sp,
                            color = TextMuted.copy(alpha = 0.7f)
                        )
                    }
                }

                itemsIndexed(contactOverrides, key = { _, item -> "override_${item.contactId}" }) { _, item ->
                    val previewKey = "override_preview_${item.contactId}"
                    val isPlaying = currentlyPlayingId == previewKey

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
                                // Contact Avatar or fallback
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
                                    Text(
                                        text = "${item.displayName} • ${if (item.sourceType == CallRingtoneManager.RINGTONE_TYPE_CUSTOM) "Custom File" else "System"}",
                                        fontSize = 11.5.sp,
                                        color = AccentIndigo,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Preview Button
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            CallRingtoneManager.stopPreview()
                                        } else {
                                            CallRingtoneManager.playPreview(
                                                context,
                                                RingtoneOption(
                                                    id = previewKey,
                                                    title = item.displayName,
                                                    uriString = if (item.sourceType == CallRingtoneManager.RINGTONE_TYPE_CUSTOM) item.localPath else item.systemUri,
                                                    type = item.sourceType
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

                                Spacer(modifier = Modifier.width(6.dp))

                                // Reset to Global Default in One Tap
                                IconButton(
                                    onClick = {
                                        CallRingtoneManager.removeContactRingtoneOverride(context, item.contactId)
                                        overridesRefreshKey++
                                        Toast.makeText(
                                            context,
                                            "Reverted ${item.contactName.ifBlank { item.contactId }} to global ringtone",
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
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
