package com.anchor.ui.settings

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anchor.data.AnchorRoutinePreferences
import com.anchor.data.SafetyPhraseRecordingStore
import com.anchor.domain.content.SafetyPhraseBank
import com.anchor.domain.content.SafetyPhraseRecording
import com.anchor.ui.components.RecordVoiceDialog
import com.anchor.ui.components.VoiceNoteRow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * "Safety Phrases" (Settings → Safety Phrases) — record your own voice
 * reading a grounding phrase, or import an existing recording, per
 * `docs/vision.md` non-negotiable #6 ("24 bundled safety phrases + custom
 * recordings"). Re-recording always writes to the same file path per
 * phrase id, so a new take simply replaces the old one — no separate
 * delete step, and no orphaned files left behind.
 *
 * Whichever recording is marked active loops during the Anchor comfort
 * tool: this screen writes into the same
 * [AnchorRoutinePreferences.calmingAudioUri]/`calmingAudioLabel` slot
 * Edit Anchor's "Calming audio" section already reads, so the existing,
 * already-tested ambient-loop playback needs no changes to support it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyPhrasesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { SafetyPhraseRecordingStore(context) }
    val routinePrefs = remember { AnchorRoutinePreferences(context) }

    var recordings by remember { mutableStateOf(store.all()) }
    var activeUri by remember { mutableStateOf(routinePrefs.calmingAudioUri) }
    var recordingTargetId by remember { mutableStateOf<String?>(null) } // id -> phraseText resolved from allPhrases
    // Holds a brand-new custom phrase's text between "Add a custom phrase"
    // and its first successful save — until then it has no recording yet,
    // so it can't be found in `allPhrases` (built from bundled phrases +
    // already-recorded custom ones).
    var pendingCustomPhraseText by remember { mutableStateOf<String?>(null) }
    var showAddCustom by remember { mutableStateOf(false) }
    var playingId by remember { mutableStateOf<String?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose { player?.release() }
    }

    fun stopPlayback() {
        player?.release()
        player = null
        playingId = null
    }

    fun togglePlay(recording: SafetyPhraseRecording) {
        if (playingId == recording.id) {
            stopPlayback()
            return
        }
        player?.release()
        playingId = recording.id
        player = try {
            MediaPlayer().apply {
                setDataSource(recording.filePath)
                setOnCompletionListener { playingId = null }
                prepare()
                start()
            }
        } catch (_: Exception) {
            playingId = null
            null
        }
    }

    fun recordingFor(id: String) = recordings.firstOrNull { it.id == id }
    fun isActive(recording: SafetyPhraseRecording) = activeUri == Uri.fromFile(File(recording.filePath)).toString()

    fun setActive(recording: SafetyPhraseRecording) {
        val uri = Uri.fromFile(File(recording.filePath)).toString()
        routinePrefs.calmingAudioUri = uri
        routinePrefs.calmingAudioLabel = recording.phraseText
        activeUri = uri
    }

    fun deleteRecording(recording: SafetyPhraseRecording) {
        if (playingId == recording.id) stopPlayback()
        File(recording.filePath).delete()
        store.delete(recording.id)
        recordings = store.all()
        if (isActive(recording)) {
            routinePrefs.clearCalmingAudio()
            activeUri = null
        }
    }

    fun phraseFileFor(id: String) = File(context.filesDir, "phrases/$id.m4a").apply { parentFile?.mkdirs() }

    val allPhrases: List<Pair<String, String>> =
        SafetyPhraseBank.DEFAULTS.mapIndexed { index, text -> "bundled_$index" to text } +
            recordings.filter { it.id.startsWith("custom_") }.map { it.id to it.phraseText }

    fun resolvePhraseText(id: String): String? =
        allPhrases.firstOrNull { it.first == id }?.second
            ?: pendingCustomPhraseText.takeIf { id.startsWith("custom_") }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val id = recordingTargetId
        val phraseText = id?.let(::resolvePhraseText)
        if (uri != null && id != null && phraseText != null) {
            try {
                val dest = phraseFileFor(id)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(dest).use { output -> input.copyTo(output) }
                }
                store.save(SafetyPhraseRecording(id, phraseText, dest.absolutePath, System.currentTimeMillis()))
                recordings = store.all()
            } catch (_: Exception) {
                // Copy failed — leave any previous recording for this phrase in place.
            }
        }
        recordingTargetId = null
        pendingCustomPhraseText = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Phrases", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Record yourself saying a phrase, or import a recording — your own " +
                    "voice, or a loved one's. Recording again replaces the old take. " +
                    "Whichever one you set as active plays on loop during the Anchor exercise.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = { showAddCustom = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Add a custom phrase")
            }
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allPhrases, key = { it.first }) { (id, text) ->
                    val recording = recordingFor(id)
                    val durationLabel = remember(recording?.filePath) {
                        recording?.filePath?.let { audioDurationLabel(it) } ?: "0:00"
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (recording != null && isActive(recording))
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text, style = MaterialTheme.typography.bodyLarge)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VoiceNoteRow(
                                    hasRecording = recording != null,
                                    isPlaying = recording != null && playingId == recording.id,
                                    durationLabel = durationLabel,
                                    onPlayToggle = { recording?.let(::togglePlay) },
                                    onRecordOrReRecord = { recordingTargetId = id }
                                )
                                if (recording != null) {
                                    if (isActive(recording)) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                                        Text("Active", style = MaterialTheme.typography.labelMedium)
                                    } else {
                                        TextButton(onClick = { setActive(recording) }) { Text("Set active") }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        IconButton(onClick = { deleteRecording(recording) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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

    recordingTargetId?.let { id ->
        val phraseText = resolvePhraseText(id) ?: ""
        RecordVoiceDialog(
            promptLabel = phraseText,
            outputFile = phraseFileFor(id),
            onSaved = { file ->
                store.save(SafetyPhraseRecording(id, phraseText, file.absolutePath, System.currentTimeMillis()))
                recordings = store.all()
                recordingTargetId = null
                pendingCustomPhraseText = null
            },
            onImportInstead = { importLauncher.launch(arrayOf("audio/*")) },
            onCancel = { recordingTargetId = null; pendingCustomPhraseText = null }
        )
    }

    if (showAddCustom) {
        AddCustomPhraseDialog(
            onDismiss = { showAddCustom = false },
            onConfirm = { text ->
                showAddCustom = false
                // Reserve the slot immediately so the record dialog can
                // resolve its text even though it has no recording (and so
                // isn't in `allPhrases`) yet.
                pendingCustomPhraseText = text
                recordingTargetId = "custom_${UUID.randomUUID()}"
            }
        )
    }
}

@Composable
private fun AddCustomPhraseDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val acceptable = SafetyPhraseBank.isAcceptable(text)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New phrase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Your phrase") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Present tense, ${SafetyPhraseBank.MAX_WORDS} words or fewer. Not what happened — what helps.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }, enabled = acceptable) {
                Text("Next: record or import")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Best-effort clip duration as "m:ss", or "0:00" if it can't be read. */
private fun audioDurationLabel(filePath: String): String {
    return try {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(filePath)
            val ms = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val totalSec = ms / 1000
            "${totalSec / 60}:${(totalSec % 60).toString().padStart(2, '0')}"
        }
    } catch (_: Exception) {
        "0:00"
    }
}
