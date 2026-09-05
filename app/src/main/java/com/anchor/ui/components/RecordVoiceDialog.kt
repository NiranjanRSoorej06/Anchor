package com.anchor.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.anchor.core.audio.PhraseRecorder
import kotlinx.coroutines.delay
import java.io.File

/**
 * Shared record-or-import flow — used by Safety Phrases and by exercise-
 * narration recording, so the "record → preview → save (overwrites)"
 * mechanic and its bug fixes live in exactly one place.
 *
 * @param promptLabel what to show the user as the thing they're recording
 *   (a safety phrase, or a guided-exercise step's line).
 * @param outputFile where a successful recording is written — callers
 *   pass a fresh temp path; [onSaved] receives this same file once a
 *   take is confirmed good, and is responsible for moving/renaming it
 *   into its permanent home and overwriting any previous take there.
 */
@Composable
fun RecordVoiceDialog(
    promptLabel: String,
    outputFile: File,
    onSaved: (File) -> Unit,
    onImportInstead: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val recorder = remember { PhraseRecorder(context) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasMicPermission = granted }

    var isRecording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var recordingTooShort by remember { mutableStateOf(false) }
    var elapsedSec by remember { mutableIntStateOf(0) }

    // MediaRecorder throws if stop() is called within roughly the first
    // second of start() — the output file is left empty/corrupt when that
    // happens. This was shipping as a silent bug: the dialog used to treat
    // every stop() as successful and let the user "Save" a broken file
    // that would never play back. Now stop()'s real result gates whether
    // a take is offered at all.
    fun stopAndCapture() {
        val stoppedCleanly = recorder.stop()
        isRecording = false
        if (stoppedCleanly && outputFile.length() > 0) {
            recordedFile = outputFile
            recordingTooShort = false
        } else {
            outputFile.delete()
            recordedFile = null
            recordingTooShort = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { recorder.cancel() }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            elapsedSec = 0
            while (isRecording && elapsedSec < (PhraseRecorder.MAX_DURATION_MS / 1000)) {
                delay(1000)
                elapsedSec++
            }
            if (isRecording) stopAndCapture()
        }
    }

    AlertDialog(
        onDismissRequest = { recorder.cancel(); onCancel() },
        title = { Text("Record") },
        text = {
            Column {
                Text("\"$promptLabel\"", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = when {
                        !hasMicPermission -> "Microphone permission needed to record."
                        recordingTooShort -> "That was too short to save — try holding a bit longer."
                        recordedFile != null -> "Recorded (${elapsedSec}s). Save, or record again."
                        isRecording -> "Recording… ${elapsedSec}s / ${PhraseRecorder.MAX_DURATION_MS / 1000}s"
                        else -> "Up to ${PhraseRecorder.MAX_DURATION_MS / 1000} seconds."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (recordingTooShort || isRecording) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                )
            }
        },
        confirmButton = {
            when {
                !hasMicPermission -> TextButton(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) {
                    Text("Grant microphone access")
                }
                recordedFile != null -> TextButton(onClick = { recordedFile?.let(onSaved) }) { Text("Save") }
                isRecording -> TextButton(onClick = { stopAndCapture() }) { Text("Stop") }
                else -> TextButton(onClick = {
                    recordingTooShort = false
                    if (recorder.start(outputFile) != null) {
                        isRecording = true
                    }
                }) { Text("Start recording") }
            }
        },
        dismissButton = {
            Row {
                if (recordedFile != null) {
                    TextButton(onClick = {
                        recordedFile?.delete()
                        recordedFile = null
                    }) { Text("Re-record") }
                }
                TextButton(onClick = onImportInstead) { Text("Import instead") }
                TextButton(onClick = { recorder.cancel(); onCancel() }) { Text("Cancel") }
            }
        }
    )
}
