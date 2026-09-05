package com.anchor.ui.symptoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anchor.domain.content.Intervention
import com.anchor.domain.routing.recommendInterventions
import com.anchor.domain.triage.IncidentKind
import com.anchor.ui.theme.AnchorColors

// ──────────────────────────────────────────────────────────────────────────────
// Symptom clusters — civilian-first groupings of IncidentKind
// ──────────────────────────────────────────────────────────────────────────────

private enum class SymptomCluster(
    val label: String,
    val description: String,
    val kinds: List<IncidentKind>
) {
    HYPERAROUSAL(
        label = "On Edge",
        description = "Body feels wound tight, reactive, hard to settle.",
        kinds = listOf(IncidentKind.PANIC, IncidentKind.ANGER_SPIKE, IncidentKind.SENSORY_OVERLOAD)
    ),
    INTRUSION(
        label = "Memories Breaking Through",
        description = "A scene replays as if it\u2019s happening right now.",
        kinds = listOf(IncidentKind.FLASHBACK)
    ),
    DISSOCIATION(
        label = "Feeling Unreal",
        description = "Foggy, distant, or disconnected from yourself.",
        kinds = listOf(IncidentKind.DISSOCIATION)
    ),
    SLEEP(
        label = "Sleep Disruption",
        description = "Waking from a distressing dream, body still shaking.",
        kinds = listOf(IncidentKind.NIGHTMARE)
    ),
    AVOIDANCE_MOOD(
        label = "Avoidance & Low Mood",
        description = "Pulling away from things, or carrying a heavy weight.",
        kinds = listOf(IncidentKind.AVOIDANCE_URGE, IncidentKind.LOW_MOOD, IncidentKind.NOT_SURE)
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Difficulty / warning — mirrors SessionScreen helpers (private there)
// ──────────────────────────────────────────────────────────────────────────────

private enum class DifficultyLevel(val label: String) {
    GENTLE("Gentle"),
    MODERATE("Moderate")
}

private fun difficultyFor(intervention: Intervention): DifficultyLevel = when {
    intervention.interoceptive && intervention.requiresHaptics -> DifficultyLevel.MODERATE
    else -> DifficultyLevel.GENTLE
}

private fun warningFor(intervention: Intervention): String = when {
    intervention.interoceptive ->
        "Body-focused \u2014 start gently, stop anytime."
    intervention.requiresVoice ->
        "You\u2019ll speak a few simple words \u2014 no pressure."
    intervention.requiresHaptics ->
        "Uses physical sensation \u2014 adjust as needed."
    else ->
        "Go at your own pace \u2014 there\u2019s no wrong way to do this."
}

private val IncidentKind.civilianLabel: String
    get() = when (this) {
        IncidentKind.PANIC           -> "Heart racing, can\u2019t breathe"
        IncidentKind.FLASHBACK       -> "A memory playing like it\u2019s happening now"
        IncidentKind.NIGHTMARE       -> "Woke from a bad dream"
        IncidentKind.DISSOCIATION    -> "Feeling foggy or unreal"
        IncidentKind.ANGER_SPIKE     -> "Sudden anger or tension"
        IncidentKind.AVOIDANCE_URGE  -> "Wanting to avoid something"
        IncidentKind.SENSORY_OVERLOAD -> "Lights, sounds, or textures feel like too much"
        IncidentKind.LOW_MOOD        -> "Heavy sadness, low energy"
        IncidentKind.NOT_SURE        -> "Something\u2019s off, can\u2019t name it"
    }

// ──────────────────────────────────────────────────────────────────────────────
// Main screen
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageSymptomsScreen(
    onNavigateToSession: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onBack: () -> Unit
) {
    var selectedCluster by remember { mutableStateOf<SymptomCluster?>(null) }
    var selectedKind by remember { mutableStateOf<IncidentKind?>(null) }
    val recommendations = remember(selectedKind) {
        selectedKind?.let { recommendInterventions(it) } ?: emptyList()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
                Text(
                    text = "What you\u2019re feeling",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pick what best describes what\u2019s happening for you right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
            )

            // ── Cluster chips ─────────────────────────────────────────────
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SymptomCluster.entries.forEach { cluster ->
                    FilterChip(
                        selected = cluster == selectedCluster,
                        onClick = {
                            selectedCluster = if (cluster == selectedCluster) null else cluster
                            selectedKind = null
                        },
                        label = {
                            Text(
                                text = cluster.label,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Cluster description + symptom cards ────────────────────────
            AnimatedVisibility(
                visible = selectedCluster != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    selectedCluster?.let { cluster ->
                        Text(
                            text = cluster.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        cluster.kinds.forEach { kind ->
                            Card(
                                onClick = {
                                    selectedKind = if (kind == selectedKind) null else kind
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (kind == selectedKind)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (kind == selectedKind)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                            ) {
                                Text(
                                    text = kind.civilianLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Recommended exercises ──────────────────────────────────────
            AnimatedVisibility(
                visible = recommendations.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Exercises that may help",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Try whichever feels right \u2014 you can stop at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    recommendations.forEach { intervention ->
                        InterventionCard(
                            intervention = intervention,
                            onStart = onNavigateToSession
                        )
                    }
                }
            }

            // ── Professional help button ───────────────────────────────────
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onNavigateToSupport,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "Need professional help?",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Not a replacement for professional care \u00B7 works offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Intervention card — mirrors SessionScreen's RecommendedStage card pattern
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun InterventionCard(
    intervention: Intervention,
    onStart: () -> Unit
) {
    val difficulty = difficultyFor(intervention)
    val warning = warningFor(intervention)

    Card(
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
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title + difficulty badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = intervention.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (difficulty) {
                        DifficultyLevel.GENTLE -> AnchorColors.current.ok.copy(alpha = 0.15f)
                        DifficultyLevel.MODERATE -> AnchorColors.current.amber.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = difficulty.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (difficulty) {
                            DifficultyLevel.GENTLE -> AnchorColors.current.ok
                            DifficultyLevel.MODERATE -> AnchorColors.current.amber
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // One-line trauma-informed warning
            Text(
                text = warning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            // Limitations (if any)
            intervention.limitations?.let { lim ->
                Text(
                    text = lim,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }

            // Start button
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text("Start")
            }
        }
    }
}
