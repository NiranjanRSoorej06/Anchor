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
import com.anchor.data.JournalStore
import com.anchor.domain.journal.JournalEntry
import java.text.DateFormat
import java.util.Date
import java.util.UUID

/**
 * Standalone journal (module-build-prompts.md M4.4). Same reflection-
 * journal framing as `SessionScreen`'s post-session `JournalStage` —
 * "what helped" prompts, not a free-text trauma narrative field, per
 * `docs/vision.md`'s journaling guidance — just reachable any time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { JournalStore(context) }
    var entries by remember { mutableStateOf(store.all()) }

    var whatHelped by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }
    val canSave = whatHelped.isNotBlank() || reflection.isNotBlank()

    fun save() {
        store.add(
            JournalEntry(
                id = UUID.randomUUID().toString(),
                createdAtMillis = System.currentTimeMillis(),
                whatHelped = whatHelped.ifBlank { null },
                reflection = reflection.ifBlank { null }
            )
        )
        entries = store.all()
        whatHelped = ""
        reflection = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal", fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(
                value = whatHelped,
                onValueChange = { if (it.length <= JournalEntry.MAX_FIELD_CHARS) whatHelped = it },
                label = { Text("What's helped lately?") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = reflection,
                onValueChange = { if (it.length <= JournalEntry.MAX_FIELD_CHARS) reflection = it },
                label = { Text("Anything else on your mind?") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            Button(onClick = ::save, modifier = Modifier.fillMaxWidth(), enabled = canSave) {
                Text("Save entry")
            }

            HorizontalDivider()

            Text("Past entries (${entries.size})", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries, key = { it.id }) { entry ->
                    JournalEntryCard(entry)
                }
            }
        }
    }
}

@Composable
private fun JournalEntryCard(entry: JournalEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(Date(entry.createdAtMillis)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            entry.whatHelped?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            entry.reflection?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
