package com.anchor.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anchor.data.TriggerLogStore
import com.anchor.domain.followup.FollowUpCheckIn
import com.anchor.ui.components.ThermometerSlider
import com.anchor.ui.session.INCIDENT_OPTIONS
import java.text.DateFormat
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggerLogScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { TriggerLogStore(context) }
    var entries by remember { mutableStateOf(store.all()) }

    var selectedTriggers by remember { mutableStateOf(setOf<String>()) }
    var customTriggerText by remember { mutableStateOf("") }
    var distress by remember { mutableStateOf(5) }
    var note by remember { mutableStateOf("") }

    fun save() {
        val finalTriggers = selectedTriggers.toMutableSet()
        if (customTriggerText.isNotBlank()) {
            finalTriggers.add(customTriggerText.trim())
        }

        store.save(
            FollowUpCheckIn(
                episodeId = "manual-${UUID.randomUUID()}",
                triggerIds = finalTriggers,
                distress = distress,
                note = note.ifBlank { null },
                completedAtMillis = System.currentTimeMillis()
            )
        )
        entries = store.all()
        selectedTriggers = emptySet()
        customTriggerText = ""
        distress = 5
        note = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trigger Log & History", fontWeight = FontWeight.Bold) },
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
            Text("What triggered your distress?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Filter Chips
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                INCIDENT_OPTIONS.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            // Custom Trigger Input
            OutlinedTextField(
                value = customTriggerText,
                onValueChange = { customTriggerText = it },
                label = { Text("Or describe custom trigger...") },
                placeholder = { Text("e.g. Loud car horn, crowded elevator") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Distress level (1 = Mild, 10 = Severe)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ThermometerSlider(value = distress, range = 1..10, onValueChange = { distress = it })

            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= FollowUpCheckIn.MAX_NOTE_CHARS) note = it },
                label = { Text("Reflection Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Button(
                onClick = ::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedTriggers.isNotEmpty() || customTriggerText.isNotBlank() || note.isNotBlank()
            ) {
                Text("Save Trigger Entry", fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // PAST SUBMISSIONS LIST
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Logged Submissions (${entries.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (entries.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Saved On-Device",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (entries.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No trigger entries logged yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select a trigger option or type your experience above, then tap 'Save Trigger Entry'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                entries.forEach { entry ->
                    TriggerLogEntryCard(
                        entry = entry,
                        onDelete = {
                            store.delete(entry.episodeId)
                            entries = store.all()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TriggerLogEntryCard(
    entry: FollowUpCheckIn,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(entry.completedAtMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (entry.triggerIds.isNotEmpty()) {
                Text(
                    text = entry.triggerIds.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            entry.distress?.let {
                Text(
                    text = "Distress Rating: $it / 10",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            entry.note?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
