package com.anchor.ui.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anchor.domain.session.IncidentKind
import com.anchor.ui.theme.AnchorColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Civilian labels (matching ManageSymptomsScreen) ──────────────────────

private val IncidentKind.civilianLabel: String
    get() = when (this) {
        IncidentKind.PANIC           -> "Heart racing, can't breathe"
        IncidentKind.FLASHBACK       -> "A memory playing like it's happening now"
        IncidentKind.NIGHTMARE       -> "Woke from a bad dream"
        IncidentKind.DISSOCIATION    -> "Feeling foggy or unreal"
        IncidentKind.ANGER_SPIKE     -> "Sudden anger or tension"
        IncidentKind.AVOIDANCE_URGE  -> "Wanting to avoid something"
        IncidentKind.SENSORY_OVERLOAD -> "Lights, sounds, or textures feel like too much"
        IncidentKind.LOW_MOOD        -> "Heavy sadness, low energy"
        IncidentKind.NOT_SURE        -> "Something's off, can't name it"
    }

// ── In-memory data model ─────────────────────────────────────────────────

private data class TriggerEntry(
    val id: Long,
    val trigger: String?,
    val distress: Int?,       // 1-5 or null if skipped
    val note: String,
    val timestampMillis: Long
)

// ── Distress badge color ─────────────────────────────────────────────────

@Composable
private fun distressColor(level: Int) = when {
    level <= 2 -> AnchorColors.current.ok
    level <= 3 -> AnchorColors.current.amber
    else -> MaterialTheme.colorScheme.error
}

// ── Screen ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TriggerLogScreen(onBack: () -> Unit) {
    // Step state
    var step by remember { mutableIntStateOf(1) } // 1=trigger, 2=distress, 3=note
    var selectedTrigger by remember { mutableStateOf<String?>(null) }
    var distress by remember { mutableIntStateOf(0) } // 0 = not selected
    var note by remember { mutableStateOf("") }

    // History
    val history = remember { mutableStateListOf<TriggerEntry>() }
    var nextId by remember { mutableStateOf(1L) }

    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    fun saveAndReset() {
        history.add(
            TriggerEntry(
                id = nextId++,
                trigger = selectedTrigger,
                distress = if (distress > 0) distress else null,
                note = note.trim(),
                timestampMillis = System.currentTimeMillis()
            )
        )
        selectedTrigger = null
        distress = 0
        note = ""
        step = 1
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
                Text(
                    text = "Trigger Log",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stepLabel(step),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
            )

            // ── Step content ───────────────────────────────────────────────
            when (step) {
                1 -> TriggerStep(
                    selected = selectedTrigger,
                    onSelect = { selectedTrigger = it },
                    onSkip = { step = 2 },
                    onNext = { step = 2 }
                )
                2 -> DistressStep(
                    selected = distress,
                    onSelect = { distress = it },
                    onSkip = { step = 3 },
                    onNext = { step = 3 }
                )
                3 -> NoteStep(
                    note = note,
                    onNoteChange = { if (it.length <= 280) note = it },
                    onSkip = {
                        saveAndReset()
                    },
                    onSave = {
                        saveAndReset()
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── History list ───────────────────────────────────────────────
            if (history.isNotEmpty()) {
                Text(
                    text = "Past entries",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history.reversed(), key = { it.id }) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateFormat.format(Date(entry.timestampMillis)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    entry.distress?.let { level ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = distressColor(level).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Distress $level/5",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = distressColor(level),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                entry.trigger?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (entry.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = entry.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Empty state fills remaining space
                if (step == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "No entries yet. Log how you're doing — it only takes a moment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            }
        }
    }
}

// ── Step helpers ──────────────────────────────────────────────────────────

private fun stepLabel(step: Int) = when (step) {
    1 -> "Step 1 of 3 — What happened?"
    2 -> "Step 2 of 3 — How are you feeling?"
    3 -> "Step 3 of 3 — Anything to add?"
    else -> ""
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TriggerStep(
    selected: String?,
    onSelect: (String) -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        IncidentKind.entries.forEach { kind ->
            FilterChip(
                selected = kind.civilianLabel == selected,
                onClick = {
                    onSelect(if (kind.civilianLabel == selected) "" else kind.civilianLabel)
                },
                label = {
                    Text(
                        text = kind.civilianLabel,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onSkip) { Text("Skip") }
        OutlinedButton(onClick = onNext) { Text("Next") }
    }
}

@Composable
private fun DistressStep(
    selected: Int,
    onSelect: (Int) -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit
) {
    Text(
        text = "Rate your distress (1 = mild, 5 = severe)",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        (1..5).forEach { level ->
            val isSelected = level == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(if (isSelected) 0 else level) },
                label = {
                    Text(
                        text = "$level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = distressColor(level).copy(alpha = 0.2f),
                    selectedLabelColor = distressColor(level)
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onSkip) { Text("Skip") }
        OutlinedButton(onClick = onNext) { Text("Next") }
    }
}

@Composable
private fun NoteStep(
    note: String,
    onNoteChange: (String) -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text("Any notes? (optional)") },
        supportingText = { Text("${note.length}/280") },
        maxLines = 4,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onSkip) { Text("Skip") }
        Button(onClick = onSave) { Text("Save") }
    }
}
