package com.anchor.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anchor.ui.components.SectionCard

/**
 * Top-level Tools hub (module-build-prompts.md M4.1) — trigger log,
 * medicine tracker, journal, goals, track progress. Distinct from Manage
 * Symptoms' internal "Exercise Library" tab, which is a different "Tools"
 * concept from the PTSD Coach reference — see the doc's naming
 * disambiguation section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    onBack: () -> Unit,
    onTriggerLog: () -> Unit,
    onMedTracker: () -> Unit,
    onJournal: () -> Unit,
    onGoals: () -> Unit,
    onTrackProgress: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tools", fontWeight = FontWeight.Bold) },
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
            SectionCard(
                label = "Trigger Log",
                subtitle = "Log a trigger any time, not just after an SOS session",
                icon = Icons.Filled.Edit,
                onClick = onTriggerLog
            )
            SectionCard(
                label = "Medicine Tracker",
                subtitle = "Reminders and a taken/skipped log — tracking only, never advice",
                icon = Icons.Filled.CheckCircle,
                onClick = onMedTracker
            )
            SectionCard(
                label = "Journal",
                subtitle = "What helped, and anything else on your mind",
                icon = Icons.Filled.Favorite,
                onClick = onJournal
            )
            SectionCard(
                label = "Goals",
                subtitle = "Small weekly practices, tracked over time",
                icon = Icons.Filled.DateRange,
                onClick = onGoals
            )
            SectionCard(
                label = "Track Progress",
                subtitle = "What's helped, and how often",
                icon = Icons.Filled.Info,
                onClick = onTrackProgress
            )
        }
    }
}
