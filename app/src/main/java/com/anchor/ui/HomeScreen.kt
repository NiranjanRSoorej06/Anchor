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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.core.audio.DebugAudioEngine
import com.anchor.core.haptics.DebugHapticEngine
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPatterns
import com.anchor.core.logging.SprintLogStore
import com.anchor.domain.session.SessionStateMachine
import com.anchor.ui.theme.AnchorTheme

/**
 * The Anchor home screen. Per docs/vision.md's Non-negotiable #2:
 * "ANCHOR NOW is hero entry. Big one-tap button."
 *
 * Layout: audio-mode indicator → hero button → 3 cards (Manage / Tools / Support)
 * → discreet footer. Civilian-first copy throughout.
 */
@Composable
fun HomeScreen(
    machine: SessionStateMachine,
    hapticEngine: HapticEngine,
    audioEngine: AudioDeliveryEngine = DebugAudioEngine(),
    onEnterSession: () -> Unit,
    onFindSupport: () -> Unit,
    onTools: () -> Unit,
    onCompanionMode: () -> Unit = {}
) {
    val context = LocalContext.current
    val isWhisper = audioEngine.isWhisperModeActive()
    var showLogsDialog by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Audio-mode indicator ──────────────────────────────────────
            Text(
                text = if (isWhisper) "Whisper Mode · Private Earbuds" else "Speaker Mode · Soothing Voice",
                style = MaterialTheme.typography.labelSmall,
                color = if (isWhisper) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // ── Hero: ANCHOR NOW button ───────────────────────────────────
            AnchorNowButton {
                hapticEngine.play(HapticPatterns.DOUBLE_PULSE)
                machine.start()
                onEnterSession()
            }

            Spacer(modifier = Modifier.height(44.dp))

            // ── Three navigation cards ────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1 — Manage (→ SESSION)
                Card(
                    onClick = {
                        hapticEngine.play(HapticPatterns.DOUBLE_PULSE)
                        machine.start()
                        onEnterSession()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    )
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "Manage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A few quiet minutes of guided breathing and grounding.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Not a diagnosis or cure \u2014 just a moment to breathe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }
                }

                // Card 2 — Tools (→ TOOLS)
                Card(
                    onClick = onTools,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "Tools",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Guided exercises and coping techniques you can use anytime.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Card 3 — Support (→ SUPPORT)
                Card(
                    onClick = onFindSupport,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "Support",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Find professional help nearby \u2014 you don\u2019t have to do this alone.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Reflection logs + companion links (compact row) ───────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showLogsDialog = true }) {
                    Text(
                        text = "Reflection Logs",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    text = "\u00B7",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                TextButton(onClick = onCompanionMode) {
                    Text(
                        text = "People Around Me",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // ── Footer disclaimer ─────────────────────────────────────────
            Text(
                text = "Not a replacement for professional care \u00B7 works offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
            )
        }

        if (showLogsDialog) {
            SprintLogsViewerDialog(
                logStore = SprintLogStore(context),
                onDismiss = { showLogsDialog = false }
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Hero button
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnchorNowButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(236.dp)
            .shadow(
                elevation = 24.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ANCHOR",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "NOW",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Sprint logs dialog (carried forward from original)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun SprintLogsViewerDialog(
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
            onFindSupport = {},
            onTools = {}
        )
    }
}
