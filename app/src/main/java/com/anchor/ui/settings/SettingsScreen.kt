package com.anchor.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.anchor.core.logging.SprintLogStore
import com.anchor.devtools.ThemeSwitcher
import com.anchor.ui.SprintLogsViewerDialogPublic
import com.anchor.ui.theme.ThemeVariant

/**
 * Real Settings destination — absorbs the dev-only [ThemeSwitcher] chip
 * (previously pinned above every screen in `MainActivity`) and the raw
 * "Reflection Logs" dump (previously on Home). Both explicitly said
 * "delete/move once a real Settings screen exists"; this is that screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeVariant: ThemeVariant,
    onThemeSelect: (ThemeVariant) -> Unit,
    onCompanionMode: () -> Unit,
    onSafetyPhrases: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showLogsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ThemeSwitcher(selected = themeVariant, onSelect = onThemeSelect)

            Text("Reflection Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SettingsRow(
                label = "View past sprint reflections",
                onClick = { showLogsDialog = true }
            )

            Text("Trusted Contacts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SettingsRow(
                label = "Companion mode & trusted contacts (moving to Get Support soon)",
                onClick = onCompanionMode
            )

            Text("Safety Phrases", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SettingsRow(
                label = "Record or import your own grounding phrases",
                onClick = onSafetyPhrases
            )
        }
    }

    if (showLogsDialog) {
        SprintLogsViewerDialogPublic(
            logStore = SprintLogStore(context),
            onDismiss = { showLogsDialog = false }
        )
    }
}

@Composable
private fun SettingsRow(label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
    }
}
