package com.anchor.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchor.domain.content.CopingStatements
import com.anchor.domain.content.GuidedScripts
import com.anchor.domain.content.SleepChecklist

/**
 * TOOLS LIBRARY (dev) — a developer-only browser over the bundled content
 * in `domain/content`: guided scripts, coping statements, and the sleep
 * checklist. It is NOT user-facing product UI.
 *
 * Scripts mode lists all [GuidedScripts.ALL] entries (title + total
 * duration); tapping one shows its steps with per-step durations and a
 * Back button. Phrases mode cycles the three [CopingStatements]
 * categories and shows the [SleepChecklist] below. No timers, no audio,
 * no persistence — reading aid only.
 *
 * Delete this file and its call site in MainActivity once the real Anchor
 * tools/exercise screens (a later module) replace the devtools package.
 */
@Composable
fun ToolsLibraryScreen() {
    var showScripts by remember { mutableStateOf(true) }
    var selectedScriptId by remember { mutableStateOf<String?>(null) }
    var phraseCategory by remember { mutableStateOf(0) }

    val selected = selectedScriptId?.let { GuidedScripts.byId(it) }
    val categories = remember {
        listOf(
            "Safety" to CopingStatements.SAFETY,
            "Passing" to CopingStatements.TEMPORAL,
            "Strength" to CopingStatements.CAPABILITY
        )
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tools Library (dev)", style = MaterialTheme.typography.headlineSmall)

            ToolButton(if (showScripts) "Mode: Scripts (tap for Phrases)" else "Mode: Phrases (tap for Scripts)") {
                showScripts = !showScripts
                selectedScriptId = null
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            if (showScripts) {
                if (selected == null) {
                    Text(
                        "${GuidedScripts.ALL.size} scripts (dev preview)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    GuidedScripts.ALL.forEach { script ->
                        val minutes = script.totalDurationSec() / 60
                        val seconds = script.totalDurationSec() % 60
                        ToolButton("${script.title} — ${minutes}m ${seconds}s") {
                            selectedScriptId = script.id
                        }
                    }
                } else {
                    Text(selected.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${selected.steps.size} steps, ${selected.totalDurationSec()}s total",
                        style = MaterialTheme.typography.bodySmall
                    )
                    selected.steps.forEachIndexed { index, step ->
                        Text(
                            "${index + 1}. ${step.text} (${step.durationSec}s)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    ToolButton("Back to list") { selectedScriptId = null }
                }
            } else {
                val (label, phrases) = categories[phraseCategory % categories.size]
                ToolButton("Phrases: $label (tap to cycle)") {
                    phraseCategory = (phraseCategory + 1) % categories.size
                }
                phrases.forEach { phrase ->
                    Text("• $phrase", style = MaterialTheme.typography.bodyMedium)
                }

                HorizontalDivider(Modifier.padding(vertical = 4.dp))

                Text("Sleep checklist (info only)", style = MaterialTheme.typography.titleSmall)
                SleepChecklist.ITEMS.forEachIndexed { index, item ->
                    Text(
                        "${index + 1}. $item",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolButton(label: String, onClick: () -> Unit) {
    Button(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Text(label)
    }
}
