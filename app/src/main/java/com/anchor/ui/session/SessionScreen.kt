package com.anchor.ui.session

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.core.audio.CalmingAudioPlayer
import com.anchor.core.audio.DebugAudioEngine
import com.anchor.core.companion.CompanionNotificationEngine
import com.anchor.core.companion.CompanionPreferences
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPatterns
import com.anchor.core.logging.SprintExperienceLog
import com.anchor.core.logging.SprintLogStore
import com.anchor.domain.content.Intervention
import com.anchor.domain.routing.recommendInterventions
import com.anchor.domain.session.CheckInResponse
import com.anchor.domain.session.SessionState
import com.anchor.domain.session.SessionStateMachine
import com.anchor.domain.triage.IncidentKind
import com.anchor.ui.theme.AnchorColors
import kotlinx.coroutines.delay

/** Total duration for the Anchor Sprint in seconds — 30s breathing window. */
private const val SPRINT_DURATION_SECONDS = 30

// ──────────────────────────────────────────────────────────────────────────────
// Comfort tools available during the breathing stage
// ──────────────────────────────────────────────────────────────────────────────

enum class ComfortTool(
    val id: String,
    val label: String,
    val description: String,
    val spokenPrompt: String
) {
    BREATHING(
        id = "breathing",
        label = "Breathing",
        description = "Breathe in for 4 seconds, out for 6 seconds.",
        spokenPrompt = "Breathe in slowly through your nose… and breathe out completely."
    ),
    GROUNDING_54321(
        id = "grounding",
        label = "5-4-3-2-1",
        description = "Name 5 things you see, 4 you hear, 3 you feel, 2 you smell, 1 you taste.",
        spokenPrompt = "Look around you. Name five things you can see. Then four you can hear."
    ),
    PMR_LITE(
        id = "pmr",
        label = "PMR-lite",
        description = "Tense each muscle group gently, then let it go.",
        spokenPrompt = "Tense your shoulders gently… hold for a moment… and let them drop."
    ),
    SAFE_PLACE(
        id = "safe_place",
        label = "Safe Place",
        description = "Picture a place where you feel completely safe and calm.",
        spokenPrompt = "Close your eyes for a moment. Picture a place where you feel completely safe."
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// UI phases for the SOS flow (driven by user actions, not the state machine)
// ──────────────────────────────────────────────────────────────────────────────

private enum class SprintPhase {
    BREATHING,       // 0-30 s  — breathing with comfort-tool chips
    FEEL_BETTER,     // timer expired → "How are you feeling?"
    JOURNAL,         // optional in-memory journal, ≤ 280 chars
    INCIDENT_PICKER, // pick what happened from IncidentKind
    RECOMMENDED      // recommended interventions from catalog
}

// ──────────────────────────────────────────────────────────────────────────────
// Difficulty / warning helpers for recommended interventions
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
        "Body-focused — start gently, stop anytime."
    intervention.requiresVoice ->
        "You\u2019ll speak a few simple words — no pressure."
    intervention.requiresHaptics ->
        "Uses physical sensation — adjust as needed."
    else ->
        "Go at your own pace — there\u2019s no wrong way to do this."
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
// Main session screen
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionScreen(
    machine: SessionStateMachine,
    hapticEngine: HapticEngine,
    audioEngine: AudioDeliveryEngine = DebugAudioEngine(),
    calmingPlayer: CalmingAudioPlayer,
    onExitToHome: () -> Unit
) {
    val context = LocalContext.current
    val state by machine.state.collectAsState()
    val isWhisper = audioEngine.isWhisperModeActive()
    val companionEngine = remember { CompanionNotificationEngine(context) }
    val companionPrefs = remember { CompanionPreferences(context) }
    val logStore = remember { SprintLogStore(context) }

    // ── Sprint state ──────────────────────────────────────────────────────
    var secondsRemaining by remember { mutableIntStateOf(SPRINT_DURATION_SECONDS) }
    var currentPhase by remember { mutableStateOf(SprintPhase.BREATHING) }
    var selectedTool by remember { mutableStateOf(ComfortTool.BREATHING) }

    // ── Journal state ─────────────────────────────────────────────────────
    var journalText by remember { mutableStateOf("") }
    var showSkipNote by remember { mutableStateOf(false) }

    // ── Incident state ────────────────────────────────────────────────────
    var selectedIncident by remember { mutableStateOf<IncidentKind?>(null) }

    // ── Recommended state ─────────────────────────────────────────────────
    var recommendedList by remember { mutableStateOf<List<Intervention>>(emptyList()) }

    // ── Alert / critical state ────────────────────────────────────────────
    var isCriticalMode by remember { mutableStateOf(false) }
    var alertSent by remember { mutableStateOf(false) }
    var showConfirmAlert by remember { mutableStateOf(false) }
    var showSafetyPlanDialog by remember { mutableStateOf(false) }

    // Synchronize critical mode if state hits SAFETY_STOP
    LaunchedEffect(state) {
        if (state == SessionState.SAFETY_STOP) {
            isCriticalMode = true
        }
    }

    // Reset state when entering session fresh
    LaunchedEffect(state) {
        if (state == SessionState.ACTIVATING) {
            secondsRemaining = SPRINT_DURATION_SECONDS
            currentPhase = SprintPhase.BREATHING
            selectedTool = ComfortTool.BREATHING
            journalText = ""
            showSkipNote = false
            selectedIncident = null
            recommendedList = emptyList()
            isCriticalMode = false
            alertSent = false
            showConfirmAlert = false
            machine.beginGrounding()
        }
    }

    // Transition machine from GROUNDING → EASING → CHECK_IN when breathing ends
    LaunchedEffect(currentPhase) {
        if (currentPhase != SprintPhase.BREATHING &&
            machine.currentState == SessionState.GROUNDING
        ) {
            machine.finishGrounding()
            machine.completeEasing()
        }
    }

    // ── 30-Second Sprint Timer & Phase Transitions ────────────────────────
    LaunchedEffect(state, isCriticalMode, currentPhase) {
        if (!isCriticalMode && currentPhase == SprintPhase.BREATHING &&
            (state == SessionState.GROUNDING || state == SessionState.INTERVENTION || state == SessionState.CHECK_IN)
        ) {
            while (secondsRemaining > 0 && !isCriticalMode && currentPhase == SprintPhase.BREATHING) {
                delay(1000L)
                secondsRemaining--
            }
            // Timer complete → ask how they feel
            if (secondsRemaining <= 0 && currentPhase == SprintPhase.BREATHING) {
                currentPhase = SprintPhase.FEEL_BETTER
            }
        }
    }

    // ── Haptic breathing loop + calming audio during BREATHING phase ──────
    LaunchedEffect(currentPhase, isCriticalMode) {
        if (!isCriticalMode && currentPhase == SprintPhase.BREATHING) {
            calmingPlayer.play("calm_loop.mp3")
            while (currentPhase == SprintPhase.BREATHING && !isCriticalMode) {
                hapticEngine.play(HapticPatterns.BREATHING_IN)
                delay(4000)
                hapticEngine.play(HapticPatterns.BREATHING_OUT)
                delay(6000)
            }
        } else {
            hapticEngine.stop()
            audioEngine.stop()
            calmingPlayer.stop()
        }
    }

    // ── Spoken prompts for selected comfort tool ──────────────────────────
    LaunchedEffect(selectedTool) {
        if (currentPhase == SprintPhase.BREATHING && !isCriticalMode) {
            when (selectedTool) {
                ComfortTool.BREATHING -> {
                    // Continuous breathing prompts handled by haptic loop context
                    // Just speak the initial cue
                    audioEngine.speakWhisper("Breathe in slowly through your nose…")
                    delay(4000)
                    audioEngine.speakWhisper("Breathe out completely…")
                }
                else -> {
                    audioEngine.speakWhisper(selectedTool.spokenPrompt)
                }
            }
        }
    }

    // Leaving session on IDLE
    LaunchedEffect(state) {
        if (state == SessionState.IDLE && !isCriticalMode) onExitToHome()
    }

    val criticalBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            Color(0xFF3E1A1D).copy(alpha = 0.35f),
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isCriticalMode) criticalBackground
                    else Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isCriticalMode) {
                    // ── Sprint timer badge & mode indicator ────────────────
                    if (currentPhase == SprintPhase.BREATHING) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "30s GROUNDING: 00:${secondsRemaining.toString().padStart(2, '0')}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Text(
                                text = if (isWhisper) "Whisper" else "Speaker",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isWhisper) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { secondsRemaining / SPRINT_DURATION_SECONDS.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .padding(bottom = 16.dp),
                            color = if (secondsRemaining < 10) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    // ── Phase content ─────────────────────────────────────
                    when (currentPhase) {
                        SprintPhase.BREATHING -> BreathingStage(
                            selectedTool = selectedTool,
                            onToolSelected = { selectedTool = it },
                            audioEngine = audioEngine,
                            onSteadyClicked = {
                                currentPhase = SprintPhase.JOURNAL
                            }
                        )

                        SprintPhase.FEEL_BETTER -> FeelBetterStage(
                            onFeelingBetter = { currentPhase = SprintPhase.JOURNAL },
                            onStillFeelingIt = { currentPhase = SprintPhase.JOURNAL }
                        )

                        SprintPhase.JOURNAL -> JournalStage(
                            text = journalText,
                            onTextChange = { journalText = it.take(280) },
                            showSkipNote = showSkipNote,
                            onSave = { currentPhase = SprintPhase.INCIDENT_PICKER },
                            onSkip = {
                                if (journalText.isBlank()) {
                                    showSkipNote = true
                                } else {
                                    currentPhase = SprintPhase.INCIDENT_PICKER
                                }
                            },
                            onContinueFromNote = { currentPhase = SprintPhase.INCIDENT_PICKER }
                        )

                        SprintPhase.INCIDENT_PICKER -> IncidentPickerStage(
                            onSelect = { kind ->
                                selectedIncident = kind
                                recommendedList = recommendInterventions(kind)
                                currentPhase = SprintPhase.RECOMMENDED
                            }
                        )

                        SprintPhase.RECOMMENDED -> RecommendedStage(
                            recommendations = recommendedList,
                            onFeelingBetter = {
                                logStore.saveLog(
                                    SprintExperienceLog(
                                        comfortableToTalk = false,
                                        finalState = "Feeling Better"
                                    )
                                )
                                machine.submitCheckIn(CheckInResponse.BETTER)
                            },
                            onStillNeedHelp = { showConfirmAlert = true }
                        )
                    }
                } else {
                    // ── Critical emergency stage ───────────────────────────
                    CriticalEmergencyStage(
                        alertSent = alertSent,
                        onCallHelpline = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:14416")))
                        },
                        onCallEmergency = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
                        },
                        onOpenSafetyPlan = { showSafetyPlanDialog = true },
                        onRestartSprint = {
                            isCriticalMode = false
                            secondsRemaining = SPRINT_DURATION_SECONDS
                            currentPhase = SprintPhase.BREATHING
                            selectedTool = ComfortTool.BREATHING
                            journalText = ""
                            showSkipNote = false
                            selectedIncident = null
                            recommendedList = emptyList()
                            alertSent = false
                            machine.start()
                        },
                        onComposedExit = {
                            isCriticalMode = false
                            if (state == SessionState.SAFETY_STOP) {
                                machine.acknowledgeSafetyStop()
                            } else {
                                machine.finishRecovery()
                            }
                            onExitToHome()
                        }
                    )
                }
            }

            // ── Dialogs ───────────────────────────────────────────────────
            if (showConfirmAlert) {
                ConfirmAlertDialog(
                    onConfirm = {
                        showConfirmAlert = false
                        alertSent = true
                        val phones = companionPrefs.getContacts().map { it.phoneNumber }
                        companionEngine.sendAfterUserConfirm(
                            contactPhones = phones,
                            message = "Anchor: Your contact could use some quiet support right now."
                        )
                        logStore.saveLog(
                            SprintExperienceLog(
                                comfortableToTalk = true,
                                finalState = "Still Distressed — Alert Sent",
                                alertSentToTrustedContacts = true
                            )
                        )
                        isCriticalMode = true
                        machine.submitCheckIn(CheckInResponse.WORSE)
                    },
                    onDismiss = { showConfirmAlert = false }
                )
            }

            if (showSafetyPlanDialog) {
                SafetyPlanDialog(onDismiss = { showSafetyPlanDialog = false })
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// BREATHING stage — breathing circle + comfort-tool selector chips
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BreathingStage(
    selectedTool: ComfortTool,
    onToolSelected: (ComfortTool) -> Unit,
    audioEngine: AudioDeliveryEngine,
    onSteadyClicked: () -> Unit
) {
    var phaseText by remember { mutableStateOf("Breathe In\u2026") }
    var phaseSubtext by remember { mutableStateOf("Inhale slowly through your nose (4s)") }

    val infiniteTransition = rememberInfiniteTransition(label = "resonantBreathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 10000
                0.75f at 0 with LinearOutSlowInEasing
                1.25f at 4000 with FastOutSlowInEasing
                0.75f at 10000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "visualizerScale"
    )

    // Continuous breathing text cycle (visual only; audio handled by parent)
    LaunchedEffect(selectedTool) {
        if (selectedTool == ComfortTool.BREATHING) {
            while (true) {
                phaseText = "Breathe In\u2026"
                phaseSubtext = "Inhale slowly (4s)"
                delay(4000)
                phaseText = "Breathe Out\u2026"
                phaseSubtext = "Exhale completely (6s)"
                delay(6000)
            }
        } else {
            phaseText = selectedTool.label
            phaseSubtext = selectedTool.description
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // ── Comfort tool selector chips ───────────────────────────────────
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ComfortTool.entries.forEach { tool ->
                FilterChip(
                    selected = tool == selectedTool,
                    onClick = { onToolSelected(tool) },
                    label = {
                        Text(
                            text = tool.label,
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

        // ── Breathing visualizer ──────────────────────────────────────────
        Text(
            text = phaseText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(170.dp * scale)
                    .clip(CircleShape)
                    .background(AnchorColors.current.tint)
            )
            Box(
                modifier = Modifier
                    .size(120.dp * scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Text(
            text = phaseSubtext,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSteadyClicked
        ) {
            Text("I feel steady")
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// FEEL BETTER stage — shown when the 30s timer expires
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeelBetterStage(
    onFeelingBetter: () -> Unit,
    onStillFeelingIt: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "How are you feeling now?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Text(
            text = "There\u2019s no wrong answer.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onFeelingBetter
        ) {
            Text("Better \u2014 I\u2019m okay")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onStillFeelingIt
        ) {
            Text("Still feeling it")
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// JOURNAL stage — optional ≤280-char in-memory note
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun JournalStage(
    text: String,
    onTextChange: (String) -> Unit,
    showSkipNote: Boolean,
    onSave: () -> Unit,
    onSkip: () -> Unit,
    onContinueFromNote: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (showSkipNote) {
            // ── Skip note: importance of logging ──────────────────────────
            Text(
                text = "Logging helps you notice patterns over time \u2014 but it\u2019s entirely optional.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onContinueFromNote
            ) {
                Text("Continue")
            }
        } else {
            // ── Journal entry ─────────────────────────────────────────────
            Text(
                text = "Want to note what happened?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Even a few words can help you spot patterns later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = {
                    Text(
                        "What\u2019s on your mind?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                supportingText = {
                    Text(
                        text = "${text.length} / 280",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (text.length > 260) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = MaterialTheme.shapes.medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onSkip
                ) {
                    Text("Skip")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onSave
                ) {
                    Text("Save")
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// INCIDENT PICKER stage — select what happened from IncidentKind
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IncidentPickerStage(
    onSelect: (IncidentKind) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "What best describes what you experienced?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Pick the closest match \u2014 this helps us suggest what might help.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        IncidentKind.entries.forEach { kind ->
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelect(kind) },
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = kind.civilianLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// RECOMMENDED stage — intervention list with difficulty + warnings
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecommendedStage(
    recommendations: List<Intervention>,
    onFeelingBetter: () -> Unit,
    onStillNeedHelp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Here are some things that might help",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Try whichever feels right — you can stop at any time.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        recommendations.forEach { intervention ->
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Exit / help buttons ───────────────────────────────────────────
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onFeelingBetter
        ) {
            Text("I\u2019m feeling better")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onStillNeedHelp,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Text("Still need help")
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// CONFIRM ALERT dialog — user-authorized companion notification
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConfirmAlertDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Alert your contacts?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                "A trusted person will get a message that you could use some support. They won\u2019t know any details.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// CRITICAL EMERGENCY stage — modified: shows alert-sent status, no auto-send
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun CriticalEmergencyStage(
    alertSent: Boolean,
    onCallHelpline: () -> Unit,
    onCallEmergency: () -> Unit,
    onOpenSafetyPlan: () -> Unit,
    onRestartSprint: () -> Unit,
    onComposedExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "CRITICAL EMERGENCY MODE",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Text(
            text = "Grounding session ended",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        if (alertSent) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alert sent to your contacts",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A trusted person has been notified that you could use some support.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Button(
            onClick = onCallHelpline,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Call Tele MANAS Helpline (14416)", fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onCallEmergency,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Dial Emergency (112)", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onOpenSafetyPlan,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("View Emergency Safety Plan", fontWeight = FontWeight.SemiBold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRestartSprint,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Try Again", fontSize = 13.sp)
            }

            Button(
                onClick = onComposedExit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("I\u2019m Composed", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Safety plan dialog
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun SafetyPlanDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Immediate Safety Plan",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("1. Resonant Breathing: Inhale slowly for 4 seconds, exhale for 6 seconds.")
                Text("2. Cold Water Reset: Splash cold water on face or hold an ice cube.")
                Text("3. 5-4-3-2-1 Technique: Spot 5 red objects, feel 4 textures around you.")
                Text("4. Contact Support: Reach out to your trusted companion or call 14416.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
