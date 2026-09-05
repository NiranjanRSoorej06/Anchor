package com.anchor.ui.routine

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anchor.data.AnchorRoutinePreferences
import java.io.File
import java.io.FileOutputStream

/**
 * "Edit Anchor" — choose the comfort tool that plays on SOS (default:
 * paced breathing) and a calming audio track that loops underneath it
 * (bundled tone, or a user-picked local file — the loved-one's-voice /
 * custom mp3 case).
 *
 * Picking a custom file uses Storage Access Framework
 * ([ActivityResultContracts.OpenDocument]) with a persisted URI
 * permission, so no `READ_MEDIA_AUDIO` runtime permission is needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAnchorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { AnchorRoutinePreferences(context) }

    var selectedTool by remember { mutableStateOf(prefs.comfortTool) }
    var audioLabel by remember { mutableStateOf(prefs.calmingAudioLabel) }
    var noiseType by remember { mutableStateOf(prefs.ambientNoiseType) }
    var safePlacePhotoPath by remember { mutableStateOf(prefs.safePlacePhotoPath) }
    var selectedPreset by remember { mutableStateOf(SafePlacePreset.fromName(prefs.safePlacePresetName)) }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Photo Picker grants only a temporary read — copy into
            // app-private storage so the photo survives past this session,
            // same "copy on pick" convention as every other user-supplied
            // media in this app.
            try {
                val dest = File(context.filesDir, "safe_place_photo.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(dest).use { output -> input.copyTo(output) }
                }
                prefs.safePlacePhotoPath = dest.absolutePath
                safePlacePhotoPath = dest.absolutePath
                selectedPreset = null
            } catch (_: Exception) {
                // Copy failed — leave the previous photo (if any) in place
                // rather than silently clearing it.
            }
        }
    }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers don't support persistable permission; the
                // clip still plays for this session even if it can't survive
                // a restart.
            }
            val label = queryDisplayName(context, uri) ?: "Custom audio"
            prefs.calmingAudioUri = uri.toString()
            prefs.calmingAudioLabel = label
            audioLabel = label
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Anchor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Comfort tool",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "This plays first when you hit the Anchor button.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnchorRoutinePreferences.ComfortTool.entries.forEach { tool ->
                ToolOptionCard(
                    label = tool.label,
                    selected = tool == selectedTool,
                    onClick = {
                        selectedTool = tool
                        prefs.comfortTool = tool
                    }
                )
            }

            Text(
                "Calming audio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Loops quietly underneath your comfort tool. Pick your own file — " +
                    "a loved one's voice, or anything that helps.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = audioLabel ?: "No custom audio selected",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { pickAudioLauncher.launch(arrayOf("audio/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose audio file")
                }
                if (audioLabel != null) {
                    TextButton(
                        onClick = {
                            prefs.clearCalmingAudio()
                            audioLabel = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove custom audio", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Text(
                "Or, ambient sound",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Only used if no custom audio file is set above. Generated on your " +
                    "phone — makes background sound less noticeable, doesn't cancel it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    AnchorRoutinePreferences.NoiseType.NONE to "None",
                    AnchorRoutinePreferences.NoiseType.WHITE to "White noise",
                    AnchorRoutinePreferences.NoiseType.PINK to "Pink noise",
                    AnchorRoutinePreferences.NoiseType.RAIN to "Rain-like",
                    AnchorRoutinePreferences.NoiseType.OCEAN to "Wave-like"
                ).forEach { (type, label) ->
                    FilterChip(
                        selected = noiseType == type,
                        onClick = {
                            noiseType = type
                            prefs.ambientNoiseType = type
                        },
                        label = { Text(label) }
                    )
                }
            }

            Text(
                "Safe-place photo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Shown during Safe-place visualization. Optional — the exercise " +
                    "works the same without one.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            safePlacePhotoPath?.let { path ->
                val bitmap = remember(path) {
                    runCatching { android.graphics.BitmapFactory.decodeFile(path) }.getOrNull()
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Your safe-place photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        pickPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (safePlacePhotoPath != null) "Change photo" else "Choose a photo")
                }
                if (safePlacePhotoPath != null) {
                    TextButton(
                        onClick = {
                            prefs.clearSafePlacePhoto()
                            safePlacePhotoPath = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove photo", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Text(
                "Or, a built-in backdrop",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "No photo of your own? Pick one of these instead. Used only when " +
                    "no photo is set above.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SafePlacePreset.entries.forEach { preset ->
                    Column(
                        modifier = Modifier.clickable {
                            prefs.safePlacePresetName = preset.name
                            selectedPreset = preset
                            safePlacePhotoPath = null
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (selectedPreset == preset) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            SafePlacePresetBackdrop(preset)
                        }
                        Text(preset.label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            if (selectedPreset != null) {
                TextButton(
                    onClick = {
                        prefs.clearSafePlacePreset()
                        selectedPreset = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Remove backdrop", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ToolOptionCard(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        },
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun queryDisplayName(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    } catch (_: Exception) {
        null
    }
}
