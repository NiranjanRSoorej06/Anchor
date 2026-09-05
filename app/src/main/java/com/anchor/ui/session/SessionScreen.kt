package com.anchor.ui.session

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.core.audio.DebugAudioEngine
import com.anchor.core.companion.CompanionNotificationEngine
import com.anchor.core.companion.CompanionPreferences
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPatterns
import com.anchor.core.reminders.ReminderScheduler
import com.anchor.data.AnchorRoutinePreferences
import com.anchor.data.EpisodeStorePersistent
import com.anchor.data.JournalStore
import com.anchor.data.SessionOutcomeStore
import com.anchor.domain.content.GuidedScripts
import com.anchor.domain.content.InterventionCatalog
import com.anchor.domain.content.InterventionScripts
import com.anchor.domain.content.toSafetyCandidate
import com.anchor.domain.history.Episode
import com.anchor.domain.journal.JournalEntry
import com.anchor.domain.personalization.SessionOutcome
import com.anchor.domain.routing.InterventionRouter
import com.anchor.domain.safety.SafetyProfile
import com.anchor.domain.session.CheckInResponse
import com.anchor.domain.session.SessionState
import com.anchor.domain.session.SessionStateMachine
import com.anchor.domain.triage.IncidentKind
import com.anchor.domain.triage.IncidentTriage
import com.anchor.ui.grounding.GroundingCaptureScreen
import com.anchor.ui.theme.AnchorColors
import kotlinx.coroutines.delay
import java.util.UUID

/** Demo-compressed duration for the default breathing comfort tool. Clinical protocols run longer. */
private const val BREATHING_DURATION_SECONDS = 30

/** Grace period before the automatic SOS-start companion alert fires —
 * gives a session the user resolves right away time to do so first. */
private const val SOS_NOTIFY_DELAY_MS = 10_000L

/** The steps of the rebuilt SOS flow, on top of [SessionStateMachine]'s own states. */
private enum class FlowPhase {
    COMFORT_TOOL, FEEL_BETTER_GATE, CONSENT_GATE, JOURNAL, REMINDER_PICKER,
    INCIDENT_PICKER, RECOMMENDED_EXERCISE, CLOSING, CRITICAL
}

/** Shared with `ui/tools/TriggerLogScreen.kt` — same situation-language
 * vocabulary for "what kind of incident" whether logged right after an
 * SOS session or any time later as a standalone tool. */
internal data class IncidentOption(val kind: IncidentKind, val label: String)

internal val INCIDENT_OPTIONS = listOf(
    IncidentOption(IncidentKind.PANIC, "Panicky, heart racing"),
    IncidentOption(IncidentKind.FLASHBACK, "Reliving a memory"),
    IncidentOption(IncidentKind.DISSOCIATION, "Feeling unreal or far away"),
    IncidentOption(IncidentKind.SENSORY_OVERLOAD, "Too much noise or light"),
    IncidentOption(IncidentKind.ANGER_SPIKE, "Sudden anger or tension"),
    IncidentOption(IncidentKind.LOW_MOOD, "Heavy, low, hard to care"),
    IncidentOption(IncidentKind.NIGHTMARE, "Woke from a nightmare"),
    IncidentOption(IncidentKind.AVOIDANCE_URGE, "Strong urge to avoid something"),
    IncidentOption(IncidentKind.NOT_SURE, "Not sure / something's off"),
)

/**
 * The real Anchor SOS flow: SOS → comfort tool (breathing by default, or
 * whatever Edit Anchor set) → steady-or-timeout → feel-better gate →
 * consent → journal (or skip + remind-me-later) → close; OR, if not
 * better, incident kind → router-recommended exercise → close.
 *
 * Wires the previously-unwired domain layer for the first time:
 * [IncidentTriage], [InterventionRouter], [InterventionCatalog],
 * [Episode] persistence. See `/home/ash/.claude/plans/jiggly-kindling-sky.md`
 * Phase 1.
 */
@Composable
fun SessionScreen(
    machine: SessionStateMachine,
    hapticEngine: HapticEngine,
    audioEngine: AudioDeliveryEngine = DebugAudioEngine(),
    onExitToHome: () -> Unit,
    onGoToGetSupport: () -> Unit = onExitToHome,
    onGoToTrackProgress: () -> Unit = onExitToHome
) {
    val context = LocalContext.current
    val state by machine.state.collectAsState()
    val isWhisper = audioEngine.isWhisperModeActive()

    val companionPrefs = remember { CompanionPreferences(context) }
    val companionEngine = remember { CompanionNotificationEngine(context) }
    val routinePrefs = remember { AnchorRoutinePreferences(context) }
    val episodeStore = remember { EpisodeStorePersistent(context) }
    val journalStore = remember { JournalStore(context) }
    val outcomeStore = remember { SessionOutcomeStore(context) }

    var phase by remember { mutableStateOf(FlowPhase.COMFORT_TOOL) }
    var episodeId by remember { mutableStateOf("") }
    var episodeStartMillis by remember { mutableStateOf(0L) }
    var routineIdUsed by remember { mutableStateOf(routinePrefs.comfortTool.id) }
    var reportedResponse by remember { mutableStateOf<CheckInResponse?>(null) }
    var showSafetyPlanDialog by remember { mutableStateOf(false) }
    var recommendedInterventionId by remember { mutableStateOf<String?>(null) }

    fun recordEpisode() {
        if (episodeId.isBlank()) return
        val now = System.currentTimeMillis()
        episodeStore.record(
            Episode(
                id = episodeId,
                startedAtMillis = episodeStartMillis,
                endedAtMillis = now,
                routineId = routineIdUsed,
                finalResponse = reportedResponse,
            )
        )
        reportedResponse?.let { response ->
            outcomeStore.append(
                SessionOutcome(
                    sessionId = episodeId,
                    routineId = routineIdUsed,
                    response = response,
                    timestampMillis = now,
                )
            )
        }
    }

    // Fresh session entry: create the episode, start the comfort tool.
    LaunchedEffect(state) {
        if (state == SessionState.ACTIVATING) {
            phase = FlowPhase.COMFORT_TOOL
            episodeId = UUID.randomUUID().toString()
            episodeStartMillis = System.currentTimeMillis()
            routineIdUsed = routinePrefs.comfortTool.id
            reportedResponse = null
            recommendedInterventionId = null
            machine.beginGrounding()
        }
    }

    // Companion notify-on-SOS-start, after a short grace timer rather than
    // instantly — kept as its own effect, keyed on the episode rather than
    // `state`: the effect above changes `state` immediately via
    // beginGrounding(), which would cancel a delay living in that same
    // LaunchedEffect before it ever completed.
    LaunchedEffect(episodeId) {
        if (episodeId.isNotBlank() && companionPrefs.notifyOnSosStart) {
            delay(SOS_NOTIFY_DELAY_MS)
            companionEngine.notifyCompanion(companionPrefs.getSosStartMessageText())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            hapticEngine.stop()
            audioEngine.stop()
        }
    }

    LaunchedEffect(state) {
        if (state == SessionState.IDLE) onExitToHome()
    }

    /** Advances the state machine to CHECK_IN and submits [response], recording it as reported. */
    fun submitFeelBetter(response: CheckInResponse) {
        reportedResponse = response
        machine.finishGrounding()
        machine.completeEasing()
        machine.submitCheckIn(response)
        phase = when (response) {
            CheckInResponse.BETTER -> FlowPhase.CONSENT_GATE
            CheckInResponse.SAME -> {
                machine.beginIntervention()
                FlowPhase.INCIDENT_PICKER
            }
            // Tap-gated, not automatic: this point is only reachable after
            // the comfort tool's own timeout (30s for breathing, or the
            // grounding technique otherwise running its course) followed by
            // the person explicitly confirming they're still distressed —
            // but sending itself always waits for their own tap on the
            // Critical screen's button, exactly once per press.
            CheckInResponse.WORSE -> FlowPhase.CRITICAL
        }
    }

    fun finishRecommendedExercise() {
        machine.finishIntervention()
        machine.completeEasing()
        machine.submitCheckIn(CheckInResponse.BETTER)
        recordEpisode()
        phase = FlowPhase.CLOSING
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
                    if (phase == FlowPhase.CRITICAL) criticalBackground
                    else Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface)
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
                if (phase != FlowPhase.CRITICAL && phase != FlowPhase.COMFORT_TOOL) {
                    Text(
                        text = if (isWhisper) "Whisper Mode" else "Speaker Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isWhisper) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                when (phase) {
                    FlowPhase.COMFORT_TOOL -> ComfortToolStage(
                        tool = routinePrefs.comfortTool,
                        calmingAudioUri = routinePrefs.calmingAudioUri,
                        ambientNoiseType = routinePrefs.ambientNoiseType,
                        safePlacePhotoPath = routinePrefs.safePlacePhotoPath,
                        safePlacePresetName = routinePrefs.safePlacePresetName,
                        audioEngine = audioEngine,
                        hapticEngine = hapticEngine,
                        onSteady = { submitFeelBetter(CheckInResponse.BETTER) },
                        onTimeout = { phase = FlowPhase.FEEL_BETTER_GATE }
                    )

                    FlowPhase.FEEL_BETTER_GATE -> FeelBetterGate(
                        onBetter = { submitFeelBetter(CheckInResponse.BETTER) },
                        onTrySomethingElse = { submitFeelBetter(CheckInResponse.SAME) },
                        onNeedHelpNow = { submitFeelBetter(CheckInResponse.WORSE) }
                    )

                    FlowPhase.CONSENT_GATE -> ConsentGate(
                        onYes = { phase = FlowPhase.JOURNAL },
                        onNotNow = { phase = FlowPhase.REMINDER_PICKER }
                    )

                    FlowPhase.JOURNAL -> JournalStage(
                        onSave = { whatHelped, reflection ->
                            journalStore.add(
                                JournalEntry(
                                    id = UUID.randomUUID().toString(),
                                    createdAtMillis = System.currentTimeMillis(),
                                    whatHelped = whatHelped.ifBlank { null },
                                    reflection = reflection.ifBlank { null },
                                    linkedEpisodeId = episodeId,
                                )
                            )
                            recordEpisode()
                            phase = FlowPhase.CLOSING
                        },
                        onSkip = { phase = FlowPhase.REMINDER_PICKER }
                    )

                    FlowPhase.REMINDER_PICKER -> ReminderPickerStage(
                        onPicked = {
                            recordEpisode()
                            phase = FlowPhase.CLOSING
                        }
                    )

                    FlowPhase.INCIDENT_PICKER -> IncidentPickerStage(
                        onSelect = { kind ->
                            val currentState = IncidentTriage.toCurrentState(kind)
                            val ranked = InterventionRouter.rank(
                                candidates = InterventionCatalog.ALL.map { it.toSafetyCandidate() },
                                state = currentState,
                                profile = SafetyProfile(),
                                outcomes = outcomeStore.all(),
                            )
                            val topId = ranked.first().id
                            recommendedInterventionId = topId
                            routineIdUsed = topId
                            phase = FlowPhase.RECOMMENDED_EXERCISE
                        }
                    )

                    FlowPhase.RECOMMENDED_EXERCISE -> {
                        val id = recommendedInterventionId ?: "SAFE_FALLBACK"
                        val name = InterventionCatalog.ALL.firstOrNull { it.id == id }?.name
                            ?: "Grounding"
                        GuidedExercisePlayer(
                            title = name,
                            steps = InterventionScripts.forId(id),
                            audioEngine = audioEngine,
                            onComplete = { finishRecommendedExercise() }
                        )
                    }

                    FlowPhase.CLOSING -> ClosingStage(
                        onGoToGetSupport = onGoToGetSupport,
                        onGoToTrackProgress = onGoToTrackProgress,
                        onDone = {
                            // Best-effort cleanup; navigate regardless of whether the
                            // machine was in the exact state this transition expects
                            // (e.g. a mid-flow back-navigation-and-retry can leave it
                            // elsewhere) — the UI must never get stuck on Done.
                            machine.finishRecovery()
                            onExitToHome()
                        }
                    )

                    FlowPhase.CRITICAL -> CriticalEmergencyStage(
                        hasTrustedContact = companionPrefs.isEnabled && companionPrefs.getContacts().isNotEmpty(),
                        onNotifyEmergencyContact = {
                            // Per-tap, not automatic — the user decides whether to send this,
                            // right here after the exercise. Location is attached automatically
                            // when Companion Mode's "share location" toggle is on.
                            companionEngine.notifyCompanion(
                                "Anchor Alert: They are still distressed after a grounding exercise and asked that you be notified. Please check in on them when you can."
                            )
                        },
                        onCallHelpline = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:14416")))
                        },
                        onCallEmergency = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
                        },
                        onOpenSafetyPlan = { showSafetyPlanDialog = true },
                        onRestartSprint = {
                            phase = FlowPhase.COMFORT_TOOL
                            machine.acknowledgeSafetyStop()
                            machine.start()
                        },
                        onComposedExit = {
                            recordEpisode()
                            machine.acknowledgeSafetyStop()
                            onExitToHome()
                        }
                    )
                }
            }

            if (showSafetyPlanDialog) {
                SafetyPlanDialog(onDismiss = { showSafetyPlanDialog = false })
            }
        }
    }
}

// ── Comfort tool stage ──────────────────────────────────────────────────

@Composable
private fun ComfortToolStage(
    tool: AnchorRoutinePreferences.ComfortTool,
    calmingAudioUri: String?,
    ambientNoiseType: AnchorRoutinePreferences.NoiseType,
    safePlacePhotoPath: String?,
    safePlacePresetName: String?,
    audioEngine: AudioDeliveryEngine,
    hapticEngine: HapticEngine,
    onSteady: () -> Unit,
    onTimeout: () -> Unit
) {
    CalmingAudioLoop(calmingAudioUri, ambientNoiseType, audioEngine)

    when (tool) {
        AnchorRoutinePreferences.ComfortTool.BREATHING -> BreathingStage(
            audioEngine = audioEngine,
            hapticEngine = hapticEngine,
            onSteadyClicked = onSteady,
            onTimeout = onTimeout
        )
        AnchorRoutinePreferences.ComfortTool.GROUNDING_54321 -> {
            val script = GuidedScripts.byId("grounding_54321")!!
            GuidedExercisePlayer(
                title = script.title,
                steps = script.steps,
                audioEngine = audioEngine,
                onComplete = onTimeout,
                showSteadyButton = true,
                onSteady = onSteady
            )
        }
        AnchorRoutinePreferences.ComfortTool.PMR -> {
            val script = GuidedScripts.byId("pmr_full")!!
            GuidedExercisePlayer(
                title = script.title,
                steps = script.steps,
                audioEngine = audioEngine,
                onComplete = onTimeout,
                showSteadyButton = true,
                onSteady = onSteady
            )
        }
        AnchorRoutinePreferences.ComfortTool.VISUALIZATION -> {
            val script = GuidedScripts.byId("visualization_monsoon")!!
            SafePlaceVisualizationStage(
                photoPath = safePlacePhotoPath,
                presetName = safePlacePresetName,
                script = script,
                audioEngine = audioEngine,
                onComplete = onTimeout,
                onSteady = onSteady
            )
        }
        AnchorRoutinePreferences.ComfortTool.CAMERA_GROUNDING -> GroundingCaptureScreen(
            audioEngine = audioEngine,
            onDone = onSteady
        )
    }
}

/**
 * [GuidedExercisePlayer] running the safe-place script, with the user's
 * own photo (if set) shown as a proud, full-vividness hero image above it
 * — not a faded wash — with just a bottom gradient for the caption's
 * legibility. This is an optional, more speculative layer
 * (`CLINICAL_CAUTION_DERIVED`, extrapolated from attachment/safety-cue
 * research: Coan et al. 2006; Selcuk et al. 2018) on top of the
 * internally-guided visualization itself, which has the stronger
 * evidence basis. The technique works identically with no photo set.
 */
@Composable
private fun SafePlaceVisualizationStage(
    photoPath: String?,
    presetName: String?,
    script: com.anchor.domain.content.GuidedScript,
    audioEngine: AudioDeliveryEngine,
    onComplete: () -> Unit,
    onSteady: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val bitmap = remember(photoPath) {
            photoPath?.let { runCatching { android.graphics.BitmapFactory.decodeFile(it) }.getOrNull() }
        }
        val preset = remember(presetName) { com.anchor.ui.routine.SafePlacePreset.fromName(presetName) }

        if (bitmap != null || preset != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Your safe place",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                } else if (preset != null) {
                    com.anchor.ui.routine.SafePlacePresetBackdrop(preset, modifier = Modifier.matchParentSize())
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                                startY = 260f
                            )
                        )
                )
                Text(
                    text = "Your safe place",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }
        GuidedExercisePlayer(
            title = script.title,
            steps = script.steps,
            audioEngine = audioEngine,
            onComplete = onComplete,
            showSteadyButton = true,
            onSteady = onSteady
        )
    }
}

/** Loops [uri] (if any) quietly underneath a comfort tool, released when the composable leaves. */
@Composable
private fun CalmingAudioLoop(
    uri: String?,
    noiseType: AnchorRoutinePreferences.NoiseType,
    audioEngine: AudioDeliveryEngine
) {
    val context = LocalContext.current
    // Same contract as WhisperAudioEngine.speakWhisper: no private headset
    // connected means silent, haptic-only mode — never play ambient audio
    // out loud on the speaker. A custom file takes priority over generated
    // noise when both are somehow set.
    DisposableEffect(uri, noiseType) {
        var player: MediaPlayer? = null
        var noiseGenerator: com.anchor.core.audio.NoiseGenerator? = null

        if (audioEngine.isWhisperModeActive()) {
            if (!uri.isNullOrBlank()) {
                try {
                    player = MediaPlayer().apply {
                        setDataSource(context, Uri.parse(uri))
                        isLooping = true
                        setVolume(0.35f, 0.35f)
                        prepare()
                        start()
                    }
                } catch (_: Exception) {
                    player = null
                }
            } else if (noiseType != AnchorRoutinePreferences.NoiseType.NONE) {
                val type = when (noiseType) {
                    AnchorRoutinePreferences.NoiseType.WHITE -> com.anchor.core.audio.NoiseGenerator.Type.WHITE
                    AnchorRoutinePreferences.NoiseType.PINK -> com.anchor.core.audio.NoiseGenerator.Type.PINK
                    AnchorRoutinePreferences.NoiseType.RAIN -> com.anchor.core.audio.NoiseGenerator.Type.RAIN
                    AnchorRoutinePreferences.NoiseType.OCEAN -> com.anchor.core.audio.NoiseGenerator.Type.OCEAN
                    AnchorRoutinePreferences.NoiseType.NONE -> null
                }
                if (type != null) {
                    noiseGenerator = com.anchor.core.audio.NoiseGenerator().apply { start(type) }
                }
            }
        }

        onDispose {
            try {
                player?.stop()
                player?.release()
            } catch (_: Exception) {
                // Already released or never started — nothing to clean up.
            }
            noiseGenerator?.stop()
        }
    }
}

@Composable
private fun BreathingStage(
    audioEngine: AudioDeliveryEngine,
    hapticEngine: HapticEngine,
    onSteadyClicked: () -> Unit,
    onTimeout: () -> Unit
) {
    var phaseText by remember { mutableStateOf("Breathe In…") }
    var phaseSubtext by remember { mutableStateOf("Inhale slowly through your nose (4s)") }
    var secondsRemaining by remember { mutableIntStateOf(BREATHING_DURATION_SECONDS) }
    var steadyTapped by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        while (true) {
            phaseText = "Breathe In…"
            phaseSubtext = "Inhale slowly (4s)"
            audioEngine.speakWhisper("Breathe In")
            hapticEngine.play(HapticPatterns.BREATHING_IN)
            delay(4000)
            phaseText = "Breathe Out…"
            phaseSubtext = "Exhale completely (6s)"
            audioEngine.speakWhisper("Breathe Out")
            hapticEngine.play(HapticPatterns.BREATHING_OUT)
            delay(6000)
        }
    }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0 && !steadyTapped) {
            delay(1000)
            secondsRemaining--
        }
        if (!steadyTapped) {
            hapticEngine.stop()
            onTimeout()
        }
    }

    DisposableEffect(Unit) {
        onDispose { hapticEngine.stop() }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Text(
                text = "00:${secondsRemaining.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Text(
            text = phaseText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(230.dp)) {
            Box(
                modifier = Modifier
                    .size(190.dp * scale)
                    .clip(CircleShape)
                    .background(AnchorColors.current.tint)
            )
            Box(
                modifier = Modifier
                    .size(140.dp * scale)
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
            onClick = {
                steadyTapped = true
                onSteadyClicked()
            }
        ) {
            Text("I'm steady")
        }
    }
}

// ── Feel-better gate ─────────────────────────────────────────────────────

@Composable
private fun FeelBetterGate(
    onBetter: () -> Unit,
    onTrySomethingElse: () -> Unit,
    onNeedHelpNow: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Do you feel better?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = onBetter) {
            Text("Yes, better")
        }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onTrySomethingElse) {
            Text("Not really, try something else")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNeedHelpNow,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Text("No, I need help now")
        }
    }
}

// ── Consent + journal + reminder ─────────────────────────────────────────

@Composable
private fun ConsentGate(onYes: () -> Unit, onNotNow: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Good. Are you comfortable talking about it for a moment?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = onYes) {
            Text("Yes")
        }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onNotNow) {
            Text("Not right now")
        }
    }
}

@Composable
private fun JournalStage(onSave: (whatHelped: String, reflection: String) -> Unit, onSkip: () -> Unit) {
    var whatHelped by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }
    val canSave = whatHelped.isNotBlank() || reflection.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Log this moment",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Text(
            "Optional — what helped, or anything on your mind. Never what happened, just what helps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = whatHelped,
            onValueChange = { if (it.length <= JournalEntry.MAX_FIELD_CHARS) whatHelped = it },
            label = { Text("What helped just now?") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        OutlinedTextField(
            value = reflection,
            onValueChange = { if (it.length <= JournalEntry.MAX_FIELD_CHARS) reflection = it },
            label = { Text("Anything else on your mind?") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = canSave,
            onClick = { onSave(whatHelped, reflection) }
        ) {
            Text("Save note")
        }
        TextButton(modifier = Modifier.fillMaxWidth(), onClick = onSkip) {
            Text("Skip for now")
        }
    }
}

@Composable
private fun ReminderPickerStage(onPicked: () -> Unit) {
    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* proceed regardless — logging still works without the notification */ }

    fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun schedule(delayMillis: Long) {
        ReminderScheduler.ensureChannel(context)
        ensureNotificationPermission()
        ReminderScheduler.scheduleLogReminder(context, delayMillis)
        onPicked()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "When should we remind you to log it?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Text(
            "Logging helps you and Anchor understand your triggers over time — even a few words later helps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = { schedule(3_600_000L) }) {
            Text("In 1 hour")
        }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { schedule(4 * 3_600_000L) }) {
            Text("Tonight")
        }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { schedule(16 * 3_600_000L) }) {
            Text("Tomorrow morning")
        }
        TextButton(modifier = Modifier.fillMaxWidth(), onClick = onPicked) {
            Text("No reminder")
        }
    }
}

// ── Incident routing ─────────────────────────────────────────────────────

@Composable
private fun IncidentPickerStage(onSelect: (IncidentKind) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "What's closest to what you're feeling?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Text(
            "We'll suggest something that tends to help.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        INCIDENT_OPTIONS.forEach { option ->
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelect(option.kind) }
            ) {
                Text(option.label)
            }
        }
    }
}

// ── Closing ──────────────────────────────────────────────────────────────

@Composable
private fun ClosingStage(
    onGoToGetSupport: () -> Unit,
    onGoToTrackProgress: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "That's it for now.",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Text(
            "Everything you just did stayed on this phone.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onGoToGetSupport) {
            Text("Get Support")
        }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onGoToTrackProgress) {
            Text("Track Progress")
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onDone) {
            Text("Done")
        }
    }
}

// ── Critical / safety-stop ────────────────────────────────────────────────

@Composable
private fun CriticalEmergencyStage(
    hasTrustedContact: Boolean,
    onNotifyEmergencyContact: () -> Unit,
    onCallHelpline: () -> Unit,
    onCallEmergency: () -> Unit,
    onOpenSafetyPlan: () -> Unit,
    onRestartSprint: () -> Unit,
    onComposedExit: () -> Unit
) {
    // Nothing sends until the user taps the button below — this just
    // tracks whether that tap has already happened, so the button can't
    // fire the alert a second time.
    var resent by remember { mutableStateOf(false) }
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
            text = "Still distressed",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "You said you're still distressed.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!hasTrustedContact) {
                    Text(
                        text = "No trusted contact set up yet. Add one from Get Support → My Personal Support Contacts to use this.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (resent) {
                    // Once sent, the button is gone — nothing left to tap
                    // means nothing left that could fire a second message.
                    Text(
                        text = "Emergency contact notified — the message includes your current location, if available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Button(
                        onClick = {
                            // Checked and flipped together so a double-tap
                            // landing before recomposition hides the button
                            // still can't fire this twice.
                            if (!resent) {
                                resent = true
                                onNotifyEmergencyContact()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Notify Emergency Contact", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Sends your current location and a distress alert — only if you tap this, and only once.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Button(
            onClick = onCallHelpline,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Call Tele MANAS Helpline (14416)", fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onCallEmergency,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onRestartSprint, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Try Again", fontSize = 13.sp)
            }
            Button(
                onClick = onComposedExit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("I'm Composed", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SafetyPlanDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Immediate Safety Plan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
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
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
