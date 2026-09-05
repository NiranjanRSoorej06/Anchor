package com.anchor.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ToolsSuiteScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Trigger Log", "Journal", "Meds", "Goals", "Analytics")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("Back to Home", color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "Tools Suite",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(1.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Tab Bar
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 11.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> StandaloneTriggerLogTab()
                1 -> JournalingTab()
                2 -> MedicationTrackerTab()
                3 -> MicroGoalsTab()
                4 -> AnalyticsTab()
            }
        }
    }
}

@Composable
private fun StandaloneTriggerLogTab() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { com.anchor.data.TriggerLogStore(context) }
    var persistentLogs by remember { mutableStateOf(store.all()) }

    var triggerText by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf("Moderate") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Standalone Trigger Logger", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text("Log triggers experienced at any time to understand pattern frequencies:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = triggerText,
            onValueChange = { triggerText = it },
            label = { Text("What triggered your distress?") },
            placeholder = { Text("e.g. Sudden loud noise, crowded room, stressful argument") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Low", "Moderate", "High").forEach { level ->
                OutlinedButton(
                    onClick = { intensity = level },
                    colors = if (intensity == level) androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(level)
                }
            }
        }

        Button(
            onClick = {
                if (triggerText.isNotBlank()) {
                    val distressValue = when (intensity) {
                        "Low" -> 3
                        "Moderate" -> 6
                        else -> 9
                    }
                    store.save(
                        com.anchor.domain.followup.FollowUpCheckIn(
                            episodeId = "manual-${java.util.UUID.randomUUID()}",
                            triggerIds = setOf(triggerText.trim()),
                            distress = distressValue,
                            note = "Intensity: $intensity",
                            completedAtMillis = System.currentTimeMillis()
                        )
                    )
                    persistentLogs = store.all()
                    triggerText = ""
                }
            },
            enabled = triggerText.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Trigger Entry")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Recent Trigger Entries (${persistentLogs.size}):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        if (persistentLogs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No trigger entries logged yet. Type a trigger above and tap 'Save Trigger Entry'.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            persistentLogs.forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT)
                                .format(java.util.Date(entry.completedAtMillis)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (entry.triggerIds.isNotEmpty()) {
                            Text(
                                text = entry.triggerIds.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        entry.note?.let {
                            Text(text = it, style = MaterialTheme.typography.bodySmall)
                        }
                        entry.distress?.let {
                            Text(text = "Distress Rating: $it/10", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalingTab() {
    var noteText by remember { mutableStateOf("") }
    val journalEntries = remember { mutableStateListOf("Felt grounded after 30s breathing exercise.", "Recognized sensory grounding helped focus my vision.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Personal Journal & Reflections", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text("Expressing thoughts and reflections down on paper is clinically proven to reduce distress:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Write your reflection...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        Button(
            onClick = {
                if (noteText.isNotBlank()) {
                    journalEntries.add(0, noteText)
                    noteText = ""
                }
            },
            enabled = noteText.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Reflection Note")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Journal Entries (${journalEntries.size}):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        journalEntries.forEach { entry ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(entry, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MedicationTrackerTab() {
    var medName by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("09:00 AM") }
    val meds = remember { mutableStateListOf("Prazosin (1mg) — 09:00 PM (Daily)", "Sertraline (50mg) — 08:00 AM (Daily)") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Medication Tracker & Reminders", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text("Track prescribed medications and setup daily reminder alerts:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(value = medName, onValueChange = { medName = it }, label = { Text("Medication Name & Dosage") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Reminder Time") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (medName.isNotBlank()) {
                    meds.add("$medName — $time (Scheduled)")
                    medName = ""
                }
            },
            enabled = medName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Medication Reminder")
        }

        Spacer(modifier = Modifier.height(8.dp))
        meds.forEach { med ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(med, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MicroGoalsTab() {
    var newGoal by remember { mutableStateOf("") }
    val goals = remember { mutableStateListOf("Complete 1-min grounding sprint daily", "Take 5 deep breaths during stressful moments", "Spend 10 minutes outdoors") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Micro-Goals & Manifestation Anchors", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text("Set small, achievable daily recovery milestones:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(value = newGoal, onValueChange = { newGoal = it }, label = { Text("New Micro-Goal") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (newGoal.isNotBlank()) {
                    goals.add(newGoal)
                    newGoal = ""
                }
            },
            enabled = newGoal.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Micro-Goal")
        }

        Spacer(modifier = Modifier.height(8.dp))
        goals.forEach { goal ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text("✓ $goal", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun AnalyticsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Visual Analytics & Recovery Trends", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total SOS Sprints Completed: 14", fontWeight = FontWeight.Bold)
                Text("Average Distress Reduction: 78%", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Most Used Exercise: Resonant Breathing (4s/6s)", style = MaterialTheme.typography.bodySmall)
                Text("Top Trigger: Sudden Loud Noises", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
