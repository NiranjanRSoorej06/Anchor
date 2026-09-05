package com.anchor.ui.tools

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anchor.core.reminders.ReminderScheduler
import com.anchor.data.MedsStore
import com.anchor.domain.meds.DoseLog
import com.anchor.domain.meds.MedReminder
import com.anchor.domain.meds.MedTime
import java.util.UUID

/**
 * Medicine reminders + taken/skipped log (module-build-prompts.md M4.3).
 * Tracking only — no dosage field exists on [MedReminder] by design, and
 * this screen never advises, adjusts, or interprets medication.
 *
 * Reminder scheduling here is a daily-repeating convenience built from
 * [ReminderScheduler]'s one-shot primitive: each saved time schedules the
 * next occurrence only (re-armed manually via "Mark taken/skipped" or on
 * next app open) rather than a true recurring alarm — simplest correct
 * behavior without adding `AlarmManager.setRepeating` drift handling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedTrackerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { MedsStore(context) }
    var reminders by remember { mutableStateOf(store.allReminders()) }
    var doseLogs by remember { mutableStateOf(store.allDoseLogs()) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Tracker", fontWeight = FontWeight.Bold) },
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
            Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Add reminder")
            }

            if (reminders.isEmpty()) {
                Text(
                    "No reminders yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reminders, key = { it.id }) { reminder ->
                        val lastLog = doseLogs.filter { it.reminderId == reminder.id }.maxByOrNull { it.timestampMillis }
                        MedReminderCard(
                            reminder = reminder,
                            lastLog = lastLog,
                            onMarkTaken = {
                                store.appendDoseLog(DoseLog(reminder.id, System.currentTimeMillis(), taken = true))
                                doseLogs = store.allDoseLogs()
                            },
                            onMarkSkipped = {
                                store.appendDoseLog(DoseLog(reminder.id, System.currentTimeMillis(), taken = false))
                                doseLogs = store.allDoseLogs()
                            },
                            onDelete = {
                                store.deleteReminder(reminder.id)
                                reminders = store.allReminders()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMedDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, hour, minute ->
                val reminder = MedReminder(id = UUID.randomUUID().toString(), name = name, times = listOf(MedTime(hour, minute)))
                store.saveReminder(reminder)
                reminders = store.allReminders()
                ReminderScheduler.ensureChannel(context)
                scheduleNextOccurrence(context, hour, minute)
                showAddDialog = false
            }
        )
    }
}

/** Schedules a one-shot "time for your medicine" reminder for the next
 * occurrence of [hour]:[minute] (today if still ahead, else tomorrow). */
private fun scheduleNextOccurrence(context: android.content.Context, hour: Int, minute: Int) {
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
        set(java.util.Calendar.SECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
    }
    val delayMillis = calendar.timeInMillis - System.currentTimeMillis()
    ReminderScheduler.scheduleLogReminder(context, delayMillis)
}

@Composable
private fun MedReminderCard(
    reminder: MedReminder,
    lastLog: DoseLog?,
    onMarkTaken: () -> Unit,
    onMarkSkipped: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(reminder.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                reminder.times.joinToString(", ") { "%02d:%02d".format(it.hour, it.minute) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (lastLog != null) {
                Text(
                    if (lastLog.taken) "Last marked taken" else "Last marked skipped",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMarkTaken, modifier = Modifier.weight(1f)) { Text("Taken") }
                OutlinedButton(onClick = onMarkSkipped, modifier = Modifier.weight(1f)) { Text("Skipped") }
                OutlinedButton(onClick = onDelete) { Text("Remove") }
            }
        }
    }
}

@Composable
private fun AddMedDialog(onDismiss: () -> Unit, onConfirm: (name: String, hour: Int, minute: Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf(9) }
    var minute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { hour = (hour + 23) % 24 }) { Text("−") }
                    Text("%02d".format(hour), style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(onClick = { hour = (hour + 1) % 24 }) { Text("+") }
                    OutlinedButton(onClick = { minute = (minute + 45) % 60 }) { Text("−") }
                    Text("%02d".format(minute), style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(onClick = { minute = (minute + 15) % 60 }) { Text("+") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim(), hour, minute) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
