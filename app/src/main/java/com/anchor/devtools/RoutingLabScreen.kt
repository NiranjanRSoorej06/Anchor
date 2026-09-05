package com.anchor.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.anchor.domain.content.InterventionCatalog
import com.anchor.domain.content.toSafetyCandidate
import com.anchor.domain.personalization.PersonalizationScorer
import com.anchor.domain.personalization.SessionOutcome
import com.anchor.domain.routing.InterventionRouter
import com.anchor.domain.safety.CurrentState
import com.anchor.domain.safety.SafetyFilter
import com.anchor.domain.safety.SafetyProfile
import com.anchor.domain.session.CheckInResponse

/**
 * ROUTING LAB (dev) — a developer-only screen that exercises the
 * catalog → safety filter → personalization ranking pipeline live.
 * It is NOT a user-facing screen.
 *
 * Controls drive [InterventionRouter.rank] directly: the current state
 * cycles on tap, four buttons flip the sensory profile, one button vetoes
 * E007 (to demo SF8), outcomes can be seeded ("E004 helped 4 out of 5"),
 * and the top result can be marked tried (to demo already-tried exclusion).
 * The ranked list below always reflects the current inputs; when everything
 * is vetoed the list shows SAFE_FALLBACK (SF7) instead of going empty.
 *
 * Delete this file and its call site in MainActivity once the real Anchor
 * session screen (a later module) replaces the devtools package.
 */
@Composable
fun RoutingLabScreen() {
    var selectedState by remember { mutableStateOf(CurrentState.PANICKY) }
    var audioOk by remember { mutableStateOf(true) }
    var voiceOk by remember { mutableStateOf(true) }
    var hapticsOk by remember { mutableStateOf(true) }
    var touchSensitive by remember { mutableStateOf(false) }
    var vetoE007 by remember { mutableStateOf(false) }
    var outcomes by remember { mutableStateOf(emptyList<SessionOutcome>()) }
    var tried by remember { mutableStateOf(emptySet<String>()) }
    var seedBatch by remember { mutableStateOf(0) }

    val profile = SafetyProfile(
        audioOk = audioOk,
        voiceOk = voiceOk,
        hapticsOk = hapticsOk,
        touchSensitive = touchSensitive,
        notForMe = if (vetoE007) setOf("E007") else emptySet()
    )
    val candidates = remember { InterventionCatalog.ALL.map { it.toSafetyCandidate() } }
    val names = remember { InterventionCatalog.ALL.associate { it.id to it.name } }
    val ranked = remember(selectedState, profile, outcomes, tried) {
        InterventionRouter.rank(candidates, selectedState, profile, outcomes, tried)
    }
    val vetoedCount = remember(selectedState, profile) {
        candidates.count { !SafetyFilter.permits(it, selectedState, profile) }
    }

    fun rateLabel(id: String): String {
        val rate = PersonalizationScorer.successRate(outcomes, id) ?: return "new"
        return "${(rate * 100).toInt()}%"
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Routing Lab (dev)", style = MaterialTheme.typography.headlineSmall)

            LabButton("State: ${selectedState.name} (tap to cycle)") {
                val values = CurrentState.values()
                selectedState = values[(values.indexOf(selectedState) + 1) % values.size]
            }

            LabButton("Audio: ${if (audioOk) "ON" else "OFF"}") { audioOk = !audioOk }
            LabButton("Voice: ${if (voiceOk) "ON" else "OFF"}") { voiceOk = !voiceOk }
            LabButton("Haptics: ${if (hapticsOk) "ON" else "OFF"}") { hapticsOk = !hapticsOk }
            LabButton("Touch-sensitive: ${if (touchSensitive) "ON" else "OFF"}") {
                touchSensitive = !touchSensitive
            }
            LabButton("Veto E007 (SF8 demo): ${if (vetoE007) "ON" else "OFF"}") {
                vetoE007 = !vetoE007
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            Text(
                "Outcomes: ${outcomes.size} recorded",
                style = MaterialTheme.typography.bodyMedium
            )
            LabButton("Seed: E004 helped 4 of 5") {
                seedBatch += 1
                val base = "seed-$seedBatch"
                outcomes = outcomes + listOf(
                    SessionOutcome("$base-1", "E004", CheckInResponse.BETTER, 1L),
                    SessionOutcome("$base-2", "E004", CheckInResponse.BETTER, 2L),
                    SessionOutcome("$base-3", "E004", CheckInResponse.BETTER, 3L),
                    SessionOutcome("$base-4", "E004", CheckInResponse.BETTER, 4L),
                    SessionOutcome("$base-5", "E004", CheckInResponse.SAME, 5L)
                )
            }
            LabButton("Clear outcomes") { outcomes = emptyList() }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            Text(
                "Ranked (${ranked.size}, vetoed $vetoedCount, tried ${tried.size})",
                style = MaterialTheme.typography.titleSmall
            )
            ranked.forEachIndexed { index, candidate ->
                val fallbackMark = if (candidate.id == SafetyFilter.SAFE_FALLBACK.id) " (SAFE_FALLBACK)" else ""
                Text(
                    "${index + 1}. ${candidate.id} ${names[candidate.id] ?: ""} — ${rateLabel(candidate.id)}$fallbackMark",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            LabButton("Mark top result tried") {
                ranked.firstOrNull()?.let { tried = tried + it.id }
            }
            LabButton("Reset tried") { tried = emptySet() }
        }
    }
}

@Composable
private fun LabButton(label: String, onClick: () -> Unit) {
    Button(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Text(label)
    }
}
