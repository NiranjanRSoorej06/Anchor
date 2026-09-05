package com.anchor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * A WhatsApp-voice-note-style control: empty state shows a "Record"
 * button; once a take exists, shows a compact play pill with a duration
 * label and a small re-record affordance. Tapping play replays from the
 * start every time — "hear it again and again" — and tapping re-record
 * always *replaces* the existing take (callers overwrite, they never
 * require a separate delete step first).
 *
 * Uses text, not an icon, for record/re-record and stop — this codebase
 * only has the curated core Material icon set (`material-icons-core`),
 * not `-extended`, and it doesn't include a mic or pause glyph; pulling
 * in the extended library just for two icons isn't worth the APK size.
 */
@Composable
fun VoiceNoteRow(
    hasRecording: Boolean,
    isPlaying: Boolean,
    durationLabel: String,
    onPlayToggle: () -> Unit,
    onRecordOrReRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hasRecording) {
        OutlinedButton(onClick = onRecordOrReRecord, modifier = modifier) {
            Text("Record")
        }
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isPlaying) {
                    TextButton(onClick = onPlayToggle, modifier = Modifier.size(width = 56.dp, height = 36.dp)) {
                        Text("Stop", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    IconButton(onClick = onPlayToggle, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )
                Text(
                    text = durationLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onRecordOrReRecord, modifier = Modifier.size(width = 84.dp, height = 36.dp)) {
                    Text("Re-record", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
