package com.anchor.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.core.audio.DebugAudioEngine
import com.anchor.core.haptics.DebugHapticEngine
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.logging.SprintLogStore
import com.anchor.domain.session.SessionStateMachine
import com.anchor.ui.theme.AnchorTheme

/**
 * The real Anchor entry point. Per `docs/vision.md`'s Non-negotiable #2:
 * "ANCHOR NOW is hero entry. Big one-tap button."
 *
 * Home layout (per the approved home-redesign plan): the Anchor button up
 * top, an "Edit Anchor" link beneath it, then three section cards — Manage
 * Symptoms, Tools, Get Support — with Settings tucked into a corner icon
 * rather than sitting in the main flow.
 */
@Composable
fun HomeScreen(
    machine: SessionStateMachine,
    hapticEngine: HapticEngine,
    audioEngine: AudioDeliveryEngine = DebugAudioEngine(),
    onEnterSession: () -> Unit,
    onEditAnchor: () -> Unit = {},
    onManageSymptoms: () -> Unit,
    onTools: () -> Unit,
    onFindSupport: () -> Unit,
    onSettings: () -> Unit = {}
) {
    val isWhisper = audioEngine.isWhisperModeActive()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isWhisper) "Whisper Mode · Private Earbuds" else "Speaker Mode · Soothing Voice",
                style = MaterialTheme.typography.labelSmall,
                color = if (isWhisper) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            AnchorNowButton {
                machine.start()
                onEnterSession()
            }

            TextButton(onClick = onEditAnchor, modifier = Modifier.padding(top = 12.dp)) {
                Text("Edit Anchor")
            }

            Spacer(modifier = Modifier.height(28.dp))

            SectionCard(
                title = "Manage Symptoms",
                subtitle = "Pick what you're feeling, get a matched exercise",
                onClick = onManageSymptoms
            )
            SectionCard(
                title = "Tools",
                subtitle = "Trigger log, meds, journal, goals, progress",
                onClick = onTools
            )
            SectionCard(
                title = "Get Support",
                subtitle = "Helplines, nearby care, your trusted contacts",
                onClick = onFindSupport
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Not a replacement for professional care · works offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun AnchorNowButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(236.dp)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ANCHOR\nNOW",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Reflection-logs viewer, kept here (non-private) so [com.anchor.ui.settings.SettingsScreen]
 * can reuse it without duplicating the dialog. Superseded by the real Track
 * Progress screen in a later phase.
 */
@Composable
fun SprintLogsViewerDialogPublic(
    logStore: SprintLogStore,
    onDismiss: () -> Unit
) {
    var logs by remember { mutableStateOf(logStore.getLogs()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sprint Experience Logs",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "No sprint experience logs recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    logs.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.alertSentToTrustedContacts)
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (item.alertSentToTrustedContacts)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.timestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (item.alertSentToTrustedContacts)
                                            MaterialTheme.colorScheme.errorContainer
                                        else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = item.finalState ?: "Completed",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (item.alertSentToTrustedContacts)
                                                MaterialTheme.colorScheme.onErrorContainer
                                            else MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (item.comfortableToTalk) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    item.distressLevel?.let { Text("Distress: $it", style = MaterialTheme.typography.bodySmall) }
                                    item.primaryTrigger?.let { Text("Trigger: $it", style = MaterialTheme.typography.bodySmall) }
                                    item.hapticsHelpful?.let { Text("Haptics: $it", style = MaterialTheme.typography.bodySmall) }
                                    item.audioComfort?.let { Text("Audio: $it", style = MaterialTheme.typography.bodySmall) }
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Reflection declined by user", style = MaterialTheme.typography.bodySmall)
                                }

                                if (item.alertSentToTrustedContacts) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "SMS Alert dispatched to Trusted Contacts",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            if (logs.isNotEmpty()) {
                TextButton(
                    onClick = {
                        logStore.clearLogs()
                        logs = emptyList()
                    }
                ) {
                    Text("Clear Logs", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AnchorTheme {
        HomeScreen(
            machine = SessionStateMachine(),
            hapticEngine = DebugHapticEngine(),
            audioEngine = DebugAudioEngine(),
            onEnterSession = {},
            onManageSymptoms = {},
            onTools = {},
            onFindSupport = {}
        )
    }
}
