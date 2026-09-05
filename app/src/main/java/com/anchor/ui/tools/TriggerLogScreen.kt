package com.anchor.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.anchor.data.TriggerLogStore
import com.anchor.domain.followup.FollowUpCheckIn
import com.anchor.ui.components.ThermometerSlider
import com.anchor.ui.session.INCIDENT_OPTIONS
import java.text.DateFormat
import java.util.Date
import java.util.UUID

/**
 * Standalone "log a trigger any time" tool (module-build-prompts.md
 * M4.2) — the same [FollowUpCheckIn] model and situation-language chips
 * used by the post-SOS follow-up, just reachable any time from Tools
 * rather than only right after a session. Entries are stored with a
 * synthetic `manual-<uuid>` episode id (see [TriggerLogStore]'s KDoc).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerLogScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { TriggerLogStore(context) }
    var entries by remember { mutableStateOf(store.all()) }

    var selectedTriggers by remember { mutableStateOf(setOf<String>()) }
    var distress by remember { mutableStateOf(5) }
    var note by remember { mutableStateOf("") }

    fun save() {
        store.save(
            FollowUpCheckIn(
                episodeId = "manual-${UUID.randomUUID()}",
                triggerIds = selectedTriggers,
                distress = distress,
                note = note.ifBlank { null },
                completedAtMillis = System.currentTimeMillis()
            )
        )
        entries = store.all()
        selectedTriggers = emptySet()
        distress = 5
        note = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trigger Log", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("What kind of thing happened?", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                INCIDENT_OPTIONS.chunked(2).forEach { row ->
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { option ->
                            FilterChip(
                                selected = option.label in selectedTriggers,
                                onClick = {
                                    selectedTriggers = if (option.label in selectedTriggers) {
                                        selectedTriggers - option.label
                                    } else {
                                        selectedTriggers + option.label
                                    }
                                },
                                label = { Text(option.label) }
                            )
                        }
                    }
                }
            }

            Text("Distress level", style = MaterialTheme.typography.titleMedium)
            ThermometerSlider(value = distress, range = 1..10, onValueChange = { distress = it })

            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= FollowUpCheckIn.MAX_NOTE_CHARS) note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Button(
                onClick = ::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedTriggers.isNotEmpty() || note.isNotBlank()
            ) {
                Text("Save entry")
            }

            HorizontalDivider()

            Text("Past entries (${entries.size})", style = MaterialTheme.typography.titleMedium)
            if (entries.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No trigger entries logged yet. Select a trigger option or type a note above, then tap 'Save entry'.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(entries, key = { it.episodeId }) { entry ->
                        TriggerLogEntryCard(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun TriggerLogEntryCard(entry: FollowUpCheckIn) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(Date(entry.completedAtMillis)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (entry.triggerIds.isNotEmpty()) {
                Text(entry.triggerIds.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
            }
            entry.distress?.let { Text("Distress: $it/10", style = MaterialTheme.typography.bodySmall) }
            entry.note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
