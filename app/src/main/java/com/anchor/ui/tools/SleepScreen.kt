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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.data.SleepLogStore
import com.anchor.domain.content.GuidedScripts
import com.anchor.domain.content.SleepChecklist
import com.anchor.domain.content.SleepEducation
import com.anchor.domain.sleep.SleepEntry
import com.anchor.domain.sleep.SleepMetrics
import com.anchor.domain.sleep.SleepValidator
import com.anchor.domain.sleep.ValidationResult
import com.anchor.ui.session.GuidedExercisePlayer
import java.text.DateFormat
import java.util.Date
import java.util.UUID

/**
 * Sleep tool (Tools hub). Four sections:
 *  - **Log** — a CBT-I-style morning sleep diary ([SleepEntry]).
 *  - **Trends** — 7-night rolling averages ([SleepMetrics.rollingSummary]).
 *  - **Wind down** — [SleepChecklist] habits + the guided
 *    `breathing_478_sleep` script played through [GuidedExercisePlayer].
 *  - **Learn** — [SleepEducation] psychoeducation, routing nightmares to
 *    professional care.
 *
 * Self-tracking psychoeducation only — no sleep-restriction prescription,
 * no diagnosis, no reminders. See `docs/exercise-evidence.md`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    audioEngine: AudioDeliveryEngine,
    onBack: () -> Unit,
    onNeedProfessionalHelp: () -> Unit,
) {
    val context = LocalContext.current
    val store = remember { SleepLogStore(context) }
    var entries by remember { mutableStateOf(store.all()) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var playBreathing by remember { mutableStateOf(false) }
    val tabs = listOf("Log", "Trends", "Wind down", "Learn")

    if (playBreathing) {
        val script = GuidedScripts.byId("breathing_478_sleep")!!
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            GuidedExercisePlayer(
                title = script.title,
                steps = script.steps,
                audioEngine = audioEngine,
                onComplete = {
                    audioEngine.stop()
                    playBreathing = false
                },
            )
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { audioEngine.stop(); playBreathing = false }) { Text("Stop") }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sleep", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title, fontSize = 12.sp) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            when (selectedTab) {
                0 -> LogTab(
                    entries = entries,
                    onAdd = { showAddDialog = true },
                    onDelete = { id -> store.delete(id); entries = store.all() },
                )
                1 -> TrendsTab(entries)
                2 -> WindDownTab(onStartBreathing = { playBreathing = true })
                3 -> LearnTab(
                    hasNightmares = entries.any { it.hadNightmare },
                    onNeedProfessionalHelp = onNeedProfessionalHelp,
                )
            }
        }
    }

    if (showAddDialog) {
        AddSleepEntryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { entry ->
                store.save(entry)
                entries = store.all()
                showAddDialog = false
            },
        )
    }
}

// ── Log ───────────────────────────────────────────────────────────────

@Composable
private fun LogTab(entries: List<SleepEntry>, onAdd: () -> Unit, onDelete: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Log last night") }

        if (entries.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    "No nights logged yet. Fill this in each morning — after a week " +
                        "or two the pattern in Trends becomes useful to you and a clinician.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            entries.forEach { e -> SleepEntryCard(e, onDelete = { onDelete(e.id) }) }
        }
    }
}

@Composable
private fun SleepEntryCard(e: SleepEntry, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(e.nightOfMillis)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Asleep about ${formatDuration(SleepMetrics.totalSleepTimeMin(e))} " +
                    "· efficiency ${SleepMetrics.sleepEfficiencyPct(e)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Quality ${e.quality}/5 · ${e.awakeningsCount} awakening(s) · " +
                    "${e.sleepLatencyMin} min to fall asleep",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (e.hadNightmare) {
                Text("Nightmare noted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
            e.note?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

// ── Trends ────────────────────────────────────────────────────────────

@Composable
private fun TrendsTab(entries: List<SleepEntry>) {
    val summary = remember(entries) {
        SleepMetrics.rollingSummary(entries, System.currentTimeMillis(), nights = 7)
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Last 7 nights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        if (summary.nightsLogged == 0) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    "Log 3–4 nights to start seeing your pattern here.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TrendRow("Nights logged", "${summary.nightsLogged}")
                    TrendRow("Average time asleep", formatDuration(summary.avgTotalSleepMin))
                    TrendRow("Average sleep efficiency", "${summary.avgEfficiencyPct}%")
                    TrendRow("Nights with a nightmare", "${summary.nightmareNights}")
                }
            }
            Text(
                "Sleep efficiency is how much of your time in bed you spent asleep. " +
                    "Healthy sleepers are often around 85% or higher, but a single " +
                    "number on its own doesn't diagnose anything — share the trend " +
                    "with a clinician.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TrendRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

// ── Wind down ─────────────────────────────────────────────────────────

@Composable
private fun WindDownTab(onStartBreathing: () -> Unit) {
    val checked = remember { mutableStateListOf<Int>() }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Wind-down habits", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(
            "Supportive habits, not a treatment. Tick what you managed tonight — " +
                "the list resets each visit.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SleepChecklist.ITEMS.forEachIndexed { i, item ->
            val isChecked = i in checked
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { if (isChecked) checked.remove(i) else checked.add(i) },
                    )
                    Text(item, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = onStartBreathing, modifier = Modifier.fillMaxWidth()) {
            Text("Start 4-7-8 breathing")
        }
        Text(
            "A slow paced-breathing pattern often used before sleep. If holding " +
                "your breath feels uncomfortable, let the holds go and just breathe " +
                "out slowly.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Learn ─────────────────────────────────────────────────────────────

@Composable
private fun LearnTab(hasNightmares: Boolean, onNeedProfessionalHelp: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(SleepEducation.INTRO, style = MaterialTheme.typography.bodyMedium)
        SleepEducation.CBT_I_POINTS.forEach { point ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(point, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
        if (hasNightmares) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(
                    SleepEducation.NIGHTMARE_NOTE,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        OutlinedButton(onClick = onNeedProfessionalHelp, modifier = Modifier.fillMaxWidth()) {
            Text("Find professional care")
        }
        Text(
            SleepEducation.DISCLAIMER,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Add-entry dialog ──────────────────────────────────────────────────

@Composable
private fun AddSleepEntryDialog(onDismiss: () -> Unit, onConfirm: (SleepEntry) -> Unit) {
    var bedtime by remember { mutableStateOf("23:00") }
    var latency by remember { mutableStateOf("15") }
    var awakeningsCount by remember { mutableStateOf("1") }
    var awakeningsMin by remember { mutableStateOf("10") }
    var finalWake by remember { mutableStateOf("07:00") }
    var outOfBed by remember { mutableStateOf("07:15") }
    var quality by remember { mutableIntStateOf(3) }
    var nightmare by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Last night") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TimeField("Got into bed", bedtime) { bedtime = it }
                NumberField("Minutes to fall asleep", latency) { latency = it }
                NumberField("Times woke up in the night", awakeningsCount) { awakeningsCount = it }
                NumberField("Total minutes awake in the night", awakeningsMin) { awakeningsMin = it }
                TimeField("Woke for the last time", finalWake) { finalWake = it }
                TimeField("Got out of bed", outOfBed) { outOfBed = it }

                Text("Sleep quality", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { q ->
                        OutlinedButton(
                            onClick = { quality = q },
                            colors = if (quality == q)
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                )
                            else androidx.compose.material3.ButtonDefaults.outlinedButtonColors(),
                        ) { Text("$q") }
                    }
                }

                AssistChip(
                    onClick = { nightmare = !nightmare },
                    label = { Text(if (nightmare) "Nightmare: yes" else "Nightmare: no") },
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= SleepEntry.MAX_NOTE_CHARS) note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val bt = parseHhMm(bedtime)
                val fw = parseHhMm(finalWake)
                val ob = parseHhMm(outOfBed)
                if (bt == null || fw == null || ob == null) {
                    error = "Enter times as HH:MM, e.g. 23:30"
                    return@TextButton
                }
                val entry = SleepEntry(
                    id = UUID.randomUUID().toString(),
                    nightOfMillis = System.currentTimeMillis(),
                    bedtimeMinOfDay = bt,
                    sleepLatencyMin = latency.toIntOrNull() ?: 0,
                    awakeningsCount = awakeningsCount.toIntOrNull() ?: 0,
                    awakeningsMin = awakeningsMin.toIntOrNull() ?: 0,
                    finalWakeMinOfDay = fw,
                    outOfBedMinOfDay = ob,
                    quality = quality,
                    hadNightmare = nightmare,
                    note = note.trim().ifBlank { null },
                )
                when (val v = SleepValidator.validate(entry)) {
                    is ValidationResult.Valid -> onConfirm(entry)
                    is ValidationResult.Invalid -> error = v.reasons.first()
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun TimeField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = { Text("HH:MM") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { s -> onChange(s.filter { it.isDigit() }.take(4)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

// ── helpers ───────────────────────────────────────────────────────────

/** Parses "H:MM" / "HH:MM" (24-hour) to minute-of-day, or null. */
private fun parseHhMm(raw: String): Int? {
    val parts = raw.trim().split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}
