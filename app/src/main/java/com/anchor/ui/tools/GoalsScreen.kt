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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anchor.data.GoalsStore
import com.anchor.domain.goals.Goal
import com.anchor.domain.goals.GoalProgress
import java.util.UUID

/**
 * Weekly behavioral goals (module-build-prompts.md M4.5) — small practices
 * like "practice breathing 3x this week," tracked over a rolling 7-day
 * window via [GoalProgress]. No manifestation-prompt bank yet (the doc's
 * own fallback: tracker alone already satisfies "set goals").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { GoalsStore(context) }
    var goals by remember { mutableStateOf(store.all()) }
    var showAddDialog by remember { mutableStateOf(false) }
    val now = System.currentTimeMillis()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals", fontWeight = FontWeight.Bold) },
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
                Text("Add a goal")
            }

            if (goals.isEmpty()) {
                Text(
                    "No goals yet. Small, repeatable practices work best — \"breathe 3x this week,\" not \"stop having panic attacks.\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(goals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            now = now,
                            onMarkDone = {
                                val updated = goal.copy(completionsMillis = goal.completionsMillis + now)
                                store.save(updated)
                                goals = store.all()
                            },
                            onDelete = {
                                store.delete(goal.id)
                                goals = store.all()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { text, targetPerWeek ->
                store.save(Goal(id = UUID.randomUUID().toString(), text = text, targetPerWeek = targetPerWeek))
                goals = store.all()
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun GoalCard(goal: Goal, now: Long, onMarkDone: () -> Unit, onDelete: () -> Unit) {
    val completions = GoalProgress.countThisWeek(goal, now)
    val met = GoalProgress.isMet(goal, now)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(goal.text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            LinearProgressIndicator(
                progress = { (completions.toFloat() / goal.targetPerWeek).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (met) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
            Text(
                "$completions of ${goal.targetPerWeek} this week" + if (met) " — met!" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMarkDone, modifier = Modifier.weight(1f)) { Text("Mark done today") }
                OutlinedButton(onClick = onDelete) { Text("Remove") }
            }
        }
    }
}

@Composable
private fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (text: String, targetPerWeek: Int) -> Unit) {
    var text by remember { mutableStateOf("") }
    var target by remember { mutableStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("e.g. Practice breathing") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Times per week:")
                    OutlinedButton(onClick = { if (target > 1) target-- }) { Text("−") }
                    Text(target.toString(), style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(onClick = { if (target < 14) target++ }) { Text("+") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim(), target) }, enabled = text.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
