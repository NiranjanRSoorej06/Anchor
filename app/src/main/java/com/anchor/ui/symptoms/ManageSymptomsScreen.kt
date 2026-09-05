package com.anchor.ui.symptoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.data.SessionOutcomeStore
import com.anchor.domain.content.InterventionCatalog
import com.anchor.domain.content.InterventionScripts
import com.anchor.domain.content.toSafetyCandidate
import com.anchor.domain.personalization.SessionOutcome
import com.anchor.domain.routing.InterventionRouter
import com.anchor.domain.safety.CurrentState
import com.anchor.domain.safety.SafetyProfile
import com.anchor.domain.session.CheckInResponse
import com.anchor.ui.session.GuidedExercisePlayer
import com.anchor.ui.theme.AnchorColors
import java.util.UUID
import kotlin.math.roundToInt

private enum class Phase { PICKER, PRE_RATING, EXERCISE, POST_RATING, RESULT }

private data class SymptomCategory(
    val state: CurrentState,
    val title: String,
    val subtitle: String
)

private val CATEGORIES = listOf(
    SymptomCategory(CurrentState.PANICKY, "Panicky, heart racing", "Hyperarousal, on edge, chest tight"),
    SymptomCategory(CurrentState.FLASHBACK, "Reliving a memory", "A memory plays like it's happening again"),
    SymptomCategory(CurrentState.DISSOCIATION, "Feeling unreal or far away", "Disconnected from your body or surroundings"),
    SymptomCategory(CurrentState.FROZEN, "Frozen or overwhelmed", "Can't move, can't think, everything is too much"),
    SymptomCategory(CurrentState.NOT_SURE, "Low, heavy, hard to care", "Low mood, low energy, hard to feel motivated"),
)

/**
 * "Manage Symptoms" — pick what you're feeling, get a matched exercise.
 * Five evidence-backed categories only (per `anchor_Symptom_Exercise_Evidence_table.md`
 * and the approved plan) — nothing is offered here that the catalog can't
 * actually back, so "Do you need professional help?" is always visible
 * below the list rather than a silently-missing sixth category.
 *
 * Wraps the same [InterventionRouter] call Phase 1's Anchor flow uses,
 * plus a pre/post distress rating (PTSD Coach's own best-validated
 * mechanic per `docs/images/`, reimplemented in Anchor's own visual
 * language — no copied art or text) that now genuinely feeds
 * [SessionOutcomeStore] so ranking improves over time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSymptomsScreen(
    audioEngine: AudioDeliveryEngine,
    onBack: () -> Unit,
    onNeedProfessionalHelp: () -> Unit
) {
    val context = LocalContext.current
    val outcomeStore = remember { SessionOutcomeStore(context) }

    var phase by remember { mutableStateOf(Phase.PICKER) }
    var selectedCategory by remember { mutableStateOf<SymptomCategory?>(null) }
    var preRating by remember { mutableFloatStateOf(5f) }
    var postRating by remember { mutableFloatStateOf(5f) }
    var recommendedId by remember { mutableStateOf<String?>(null) }
    var alreadyTried by remember { mutableStateOf(setOf<String>()) }

    fun pickExercise(): String {
        val category = selectedCategory ?: return "SAFE_FALLBACK"
        val ranked = InterventionRouter.rank(
            candidates = InterventionCatalog.ALL.map { it.toSafetyCandidate() },
            state = category.state,
            profile = SafetyProfile(),
            outcomes = outcomeStore.all(),
            alreadyTried = alreadyTried,
        )
        return ranked.first().id
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Symptoms", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (phase) {
                Phase.PICKER -> SymptomPicker(
                    onSelect = { category ->
                        selectedCategory = category
                        alreadyTried = emptySet()
                        preRating = 5f
                        phase = Phase.PRE_RATING
                    },
                    onNeedProfessionalHelp = onNeedProfessionalHelp
                )

                Phase.PRE_RATING -> DistressRatingStage(
                    title = "How are you feeling right now?",
                    value = preRating,
                    onValueChange = { preRating = it },
                    buttonLabel = "Continue",
                    onContinue = {
                        val id = pickExercise()
                        recommendedId = id
                        alreadyTried = alreadyTried + id
                        phase = Phase.EXERCISE
                    }
                )

                Phase.EXERCISE -> {
                    val id = recommendedId ?: "SAFE_FALLBACK"
                    val name = InterventionCatalog.ALL.firstOrNull { it.id == id }?.name ?: "Grounding"
                    Column(modifier = Modifier.padding(20.dp)) {
                        GuidedExercisePlayer(
                            title = name,
                            steps = InterventionScripts.forId(id),
                            audioEngine = audioEngine,
                            onComplete = {
                                postRating = preRating
                                phase = Phase.POST_RATING
                            }
                        )
                    }
                }

                Phase.POST_RATING -> DistressRatingStage(
                    title = "How about now?",
                    value = postRating,
                    onValueChange = { postRating = it },
                    buttonLabel = "See result",
                    onContinue = {
                        val delta = preRating - postRating
                        val response = when {
                            delta >= 2f -> CheckInResponse.BETTER
                            delta <= -2f -> CheckInResponse.WORSE
                            else -> CheckInResponse.SAME
                        }
                        outcomeStore.append(
                            SessionOutcome(
                                sessionId = UUID.randomUUID().toString(),
                                routineId = recommendedId,
                                response = response,
                                timestampMillis = System.currentTimeMillis(),
                            )
                        )
                        phase = Phase.RESULT
                    }
                )

                Phase.RESULT -> ResultStage(
                    preRating = preRating,
                    postRating = postRating,
                    onTrySomethingElse = {
                        val id = pickExercise()
                        recommendedId = id
                        alreadyTried = alreadyTried + id
                        phase = Phase.EXERCISE
                    },
                    onDone = {
                        phase = Phase.PICKER
                        selectedCategory = null
                    }
                )
            }
        }
    }
}

@Composable
private fun SymptomPicker(
    onSelect: (SymptomCategory) -> Unit,
    onNeedProfessionalHelp: () -> Unit
) {
    val accents = listOf(
        MaterialTheme.colorScheme.error,
        AnchorColors.current.amber,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        AnchorColors.current.ok,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "What's closest to what you're feeling?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            "We'll suggest an exercise research says tends to help.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        CATEGORIES.forEachIndexed { index, category ->
            SymptomCard(
                title = category.title,
                subtitle = category.subtitle,
                accent = accents[index % accents.size],
                onClick = { onSelect(category) }
            )
        }

        TextButton(
            onClick = onNeedProfessionalHelp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Do you need professional help?")
        }
    }
}

@Composable
private fun SymptomCard(title: String, subtitle: String, accent: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DistressRatingStage(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    buttonLabel: String,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
        Text(
            "0 = totally calm, 10 = completely overwhelming",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        Text(
            value.roundToInt().toString(),
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = onContinue) {
            Text(buttonLabel)
        }
    }
}

@Composable
private fun ResultStage(
    preRating: Float,
    postRating: Float,
    onTrySomethingElse: () -> Unit,
    onDone: () -> Unit
) {
    val delta = preRating - postRating
    val message = when {
        delta >= 2f -> "That helped — down ${delta.roundToInt()} points."
        delta <= -2f -> "That didn't help. Want to try something else?"
        else -> "About the same. You can try something else, or stop here."
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            message,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onTrySomethingElse) {
            Text("Try something else")
        }
        Button(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), onClick = onDone) {
            Text("Done")
        }
    }
}
