package com.anchor.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anchor.data.EpisodeStorePersistent
import com.anchor.data.GoalsStore
import com.anchor.data.SessionOutcomeStore
import com.anchor.domain.goals.GoalProgress
import com.anchor.domain.personalization.PersonalizationScorer
import com.anchor.domain.session.CheckInResponse

/**
 * Track Progress (module-build-prompts.md M4.6) — the doc's own fallback:
 * a numeric summary rather than drawn charts, since Canvas chart code adds
 * real risk this close to a demo for the same information. Reads
 * [EpisodeStorePersistent], [SessionOutcomeStore], and [GoalsStore] — all
 * already the production-persisted stores wired at [com.anchor.MainActivity]'s
 * call sites, so this is real data, not a mock.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackProgressScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val episodeStore = remember { EpisodeStorePersistent(context) }
    val outcomeStore = remember { SessionOutcomeStore(context) }
    val goalsStore = remember { GoalsStore(context) }

    val episodes = remember { episodeStore.all() }
    val outcomes = remember { outcomeStore.all() }
    val goals = remember { goalsStore.all() }
    val now = remember { System.currentTimeMillis() }

    val betterCount = outcomes.count { it.response == CheckInResponse.BETTER }
    val routineIds = outcomes.mapNotNull { it.routineId }.distinct()
    val goalsMetThisWeek = goals.count { GoalProgress.isMet(it, now) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Progress", fontWeight = FontWeight.Bold) },
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
            SummaryCard(
                title = "Episodes logged",
                value = episodes.size.toString(),
                detail = if (outcomes.isNotEmpty()) "$betterCount of ${outcomes.size} sessions ended better" else "No sessions recorded yet"
            )
            SummaryCard(
                title = "Goals on track",
                value = "$goalsMetThisWeek of ${goals.size}",
                detail = "Met their weekly target so far"
            )
            if (routineIds.isNotEmpty()) {
                Text("What's helped", style = MaterialTheme.typography.titleMedium)
                routineIds.forEach { routineId ->
                    SummaryCard(
                        title = routineId,
                        value = "",
                        detail = PersonalizationScorer.insight(outcomes, routineId, routineId)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            if (value.isNotBlank()) {
                Text(value, style = MaterialTheme.typography.headlineSmall)
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
