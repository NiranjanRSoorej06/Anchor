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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.core.audio.DebugAudioEngine
import com.anchor.core.companion.CompanionNotificationEngine
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPatterns
import com.anchor.core.logging.SprintExperienceLog
import com.anchor.core.logging.SprintLogStore
import com.anchor.domain.session.CheckInResponse
import com.anchor.domain.session.SessionState
import com.anchor.domain.session.SessionStateMachine
import com.anchor.ui.theme.AnchorColors
import kotlinx.coroutines.delay

/** Total duration for the Anchor Sprint in seconds (1 minute). */
private const val SPRINT_DURATION_SECONDS = 60

data class GroundingCondition(
    val id: String,
    val name: String,
    val instruction: String
)

val GROUNDING_CONDITIONS = listOf(
    GroundingCondition(
        id = "dissociation",
        name = "Dissociation",
        instruction = "Name 5 things you see, 4 things you feel, and press both feet firmly into the floor."
    ),
    GroundingCondition(
        id = "flashback",
        name = "Flashback",
        instruction = "Look around and say aloud: \"I am here, I am safe, that was then, this is now,\" while slowly breathing out."
    ),
    GroundingCondition(
        id = "panic",
        name = "Panic",
        instruction = "Inhale gently for 4 seconds, exhale slowly for 6 seconds; repeat 3 times."
    ),
    GroundingCondition(
        id = "flooding",
        name = "Emotional Flooding",
        instruction = "Put one hand on your chest and one on your abdomen; take 3 slow breaths and name the emotion you feel."
    ),
    GroundingCondition(
        id = "freeze",
        name = "Freeze Response",
        instruction = "Wiggle your fingers and toes, press your feet into the floor, then slowly move your shoulders."
    ),
    GroundingCondition(
        id = "shutdown",
        name = "Shutdown",
        instruction = "Take one slow breath, look at one nearby object, and describe its color, shape, and texture aloud."
    ),
    GroundingCondition(
        id = "loss_awareness",
        name = "Loss of Awareness",
        instruction = "Stop what you're doing, sit or stand somewhere safe, press your feet into the floor, and identify your name, location, and today's date."
    )
)

enum class SprintPhase {
    BREATHING,             // 0s - 20s
    CONDITION_SELECTION,   // 20s - 30s
    SPECIALIZED_EXERCISE,  // 30s - 60s
    INTERVIEW              // Questionnaire outside timer
}

/**
 * The real session screen: reacts to [SessionStateMachine.state] and drives
 * [HapticEngine] and [AudioDeliveryEngine].
 *
 * Supports 1-Minute Anchor Sprint structured into:
 * - 0-20s: Resonant Breathing
 * - 20-30s: Condition Selection (Dissociation, Flashback, Panic, Flooding, Freeze, Shutdown, Loss of Awareness)
 * - 30-60s: Condition-Specific Grounding Exercise
 * - Post-Sprint: 5-question reflection questionnaire (outside the timer).
 */
@Composable
fun SessionScreen(
    machine: SessionStateMachine,
    hapticEngine: HapticEngine,
    audioEngine: AudioDeliveryEngine = DebugAudioEngine(),
    onExitToHome: () -> Unit
) {
    val context = LocalContext.current
    val state by machine.state.collectAsState()
    val isWhisper = audioEngine.isWhisperModeActive()
    val companionEngine = remember { CompanionNotificationEngine(context) }
    val logStore = remember { SprintLogStore(context) }

    var secondsRemaining by remember { mutableIntStateOf(SPRINT_DURATION_SECONDS) }
    var currentPhase by remember { mutableStateOf(SprintPhase.BREATHING) }
    var isUntimedMode by remember { mutableStateOf(false) }
    var selectedCondition by remember { mutableStateOf<GroundingCondition?>(null) }

    var isCriticalMode by remember { mutableStateOf(false) }
    var alertSent by remember { mutableStateOf(false) }
    var showSafetyPlanDialog by remember { mutableStateOf(false) }

    // Interview state variables
    var interviewStep by remember { mutableIntStateOf(0) }
    var q1Distress by remember { mutableStateOf<String?>(null) }
    var q2Trigger by remember { mutableStateOf<String?>(null) }
    var q3Haptics by remember { mutableStateOf<String?>(null) }
    var q4Audio by remember { mutableStateOf<String?>(null) }
    var q5State by remember { mutableStateOf<String?>(null) }

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
            isUntimedMode = false
            selectedCondition = null
            isCriticalMode = false
            alertSent = false
            interviewStep = 0
            q1Distress = null
            q2Trigger = null
            q3Haptics = null
            q4Audio = null
            q5State = null
            machine.beginGrounding()
        }
    }

    // 60-Second Sprint Timer & Phase Transitions
    LaunchedEffect(state, isCriticalMode, isUntimedMode, currentPhase) {
        if (!isCriticalMode && !isUntimedMode && currentPhase != SprintPhase.INTERVIEW &&
            (state == SessionState.GROUNDING || state == SessionState.INTERVENTION || state == SessionState.CHECK_IN)) {
            while (secondsRemaining > 0 && !isCriticalMode && !isUntimedMode && currentPhase != SprintPhase.INTERVIEW) {
                delay(1000L)
                secondsRemaining--

                // Phase 1 -> Phase 2 (at 20s mark, i.e., 40s remaining)
                if (secondsRemaining == 40 && currentPhase == SprintPhase.BREATHING) {
                    currentPhase = SprintPhase.CONDITION_SELECTION
                }

                // Phase 2 -> Phase 3 (at 30s mark, i.e., 30s remaining)
                if (secondsRemaining == 30 && currentPhase == SprintPhase.CONDITION_SELECTION) {
                    if (selectedCondition == null) {
                        selectedCondition = GROUNDING_CONDITIONS[0] // Default 1st choice (Dissociation)
                    }
                    currentPhase = SprintPhase.SPECIALIZED_EXERCISE
                }
            }

            // 60-second sprint timer complete! Move to questionnaire outside the timer
            if (secondsRemaining <= 0 && currentPhase == SprintPhase.SPECIALIZED_EXERCISE) {
                currentPhase = SprintPhase.INTERVIEW
            }
        }
    }

    // Haptic breathing loop during BREATHING phase
    LaunchedEffect(currentPhase, isCriticalMode) {
        if (!isCriticalMode && currentPhase == SprintPhase.BREATHING) {
            while (true) {
                hapticEngine.play(HapticPatterns.BREATHING_IN)
                delay(4000)
                hapticEngine.play(HapticPatterns.BREATHING_OUT)
                delay(6000)
            }
        } else {
            hapticEngine.stop()
            audioEngine.stop()
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
                if (!isCriticalMode && currentPhase != SprintPhase.INTERVIEW) {
                    // Sprint Timer Badge & Mode Badge
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
                                text = if (isUntimedMode) "UNTIMED SPRINT" else "1-MIN SPRINT: 00:${secondsRemaining.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Text(
                            text = if (isWhisper) "Whisper Mode" else "Speaker Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isWhisper) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (!isUntimedMode) {
                        LinearProgressIndicator(
                            progress = { secondsRemaining / SPRINT_DURATION_SECONDS.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .padding(bottom = 16.dp),
                            color = if (secondsRemaining < 15) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                if (isCriticalMode) {
                    CriticalEmergencyStage(
                        onCallHelpline = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:14416"))
                            context.startActivity(intent)
                        },
                        onCallEmergency = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                            context.startActivity(intent)
                        },
                        onOpenSafetyPlan = { showSafetyPlanDialog = true },
                        onRestartSprint = {
                            isCriticalMode = false
                            secondsRemaining = SPRINT_DURATION_SECONDS
                            currentPhase = SprintPhase.BREATHING
                            isUntimedMode = false
                            selectedCondition = null
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
                } else {
                    when (currentPhase) {
                        SprintPhase.BREATHING -> BreathingStage(
                            audioEngine = audioEngine,
                            onSteadyClicked = {
                                // Early "I feel steady" tap during 1st 20s: takes user to condition choice without timer
                                isUntimedMode = true
                                currentPhase = SprintPhase.CONDITION_SELECTION
                            }
                        )

                        SprintPhase.CONDITION_SELECTION -> ConditionSelectionStage(
                            conditions = GROUNDING_CONDITIONS,
                            selectedId = selectedCondition?.id,
                            onSelect = { cond ->
                                selectedCondition = cond
                                currentPhase = SprintPhase.SPECIALIZED_EXERCISE
                            }
                        )

                        SprintPhase.SPECIALIZED_EXERCISE -> SpecializedExerciseStage(
                            condition = selectedCondition ?: GROUNDING_CONDITIONS[0],
                            audioEngine = audioEngine,
                            isUntimed = isUntimedMode,
                            onComplete = {
                                currentPhase = SprintPhase.INTERVIEW
                            }
                        )

                        SprintPhase.INTERVIEW -> ExperienceInterviewStage(
                            step = interviewStep,
                            onStepChange = { interviewStep = it },
                            onQ1Selected = { q1Distress = it },
                            onQ2Selected = { q2Trigger = it },
                            onQ3Selected = { q3Haptics = it },
                            onQ4Selected = { q4Audio = it },
                            onDeclineInterview = {
                                logStore.saveLog(
                                    SprintExperienceLog(
                                        comfortableToTalk = false,
                                        finalState = "User Declined Interview"
                                    )
                                )
                                machine.submitCheckIn(CheckInResponse.BETTER)
                                onExitToHome()
                            },
                            onInterviewCompleteComposed = { finalState ->
                                q5State = finalState
                                logStore.saveLog(
                                    SprintExperienceLog(
                                        comfortableToTalk = true,
                                        distressLevel = q1Distress,
                                        primaryTrigger = q2Trigger,
                                        hapticsHelpful = q3Haptics,
                                        audioComfort = q4Audio,
                                        finalState = finalState,
                                        alertSentToTrustedContacts = false
                                    )
                                )
                                machine.submitCheckIn(CheckInResponse.BETTER)
                                onExitToHome()
                            },
                            onInterviewCompleteDistressed = { finalState ->
                                q5State = finalState
                                if (!alertSent) {
                                    alertSent = true
                                    companionEngine.notifyCompanion(
                                        "Anchor Alert: User completed a 1-minute grounding sprint but remains distressed and needs immediate support."
                                    )
                                }
                                logStore.saveLog(
                                    SprintExperienceLog(
                                        comfortableToTalk = true,
                                        distressLevel = q1Distress,
                                        primaryTrigger = q2Trigger,
                                        hapticsHelpful = q3Haptics,
                                        audioComfort = q4Audio,
                                        finalState = finalState,
                                        alertSentToTrustedContacts = true
                                    )
                                )
                                isCriticalMode = true
                                machine.submitCheckIn(CheckInResponse.WORSE)
                            }
                        )
                    }
                }
            }

            if (showSafetyPlanDialog) {
                SafetyPlanDialog(onDismiss = { showSafetyPlanDialog = false })
            }
        }
    }
}

@Composable
private fun BreathingStage(
    audioEngine: AudioDeliveryEngine,
    onSteadyClicked: () -> Unit
) {
    var phaseText by remember { mutableStateOf("Breathe In…") }
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

    LaunchedEffect(Unit) {
        while (true) {
            phaseText = "Breathe In…"
            phaseSubtext = "Inhale slowly (4s)"
            audioEngine.speakWhisper("Breathe In")
            delay(4000)
            phaseText = "Breathe Out…"
            phaseSubtext = "Exhale completely (6s)"
            audioEngine.speakWhisper("Breathe Out")
            delay(6000)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Text(
            text = phaseText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(230.dp)
        ) {
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
            onClick = onSteadyClicked
        ) {
            Text("I feel steady")
        }
    }
}

@Composable
private fun ConditionSelectionStage(
    conditions: List<GroundingCondition>,
    selectedId: String?,
    onSelect: (GroundingCondition) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Identify Your Condition",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Select what best describes what you are experiencing (or default will be selected):",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        conditions.forEach { cond ->
            val isSelected = cond.id == selectedId
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelect(cond) },
                colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) else ButtonDefaults.outlinedButtonColors()
            ) {
                Text(
                    text = cond.name,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun SpecializedExerciseStage(
    condition: GroundingCondition,
    audioEngine: AudioDeliveryEngine,
    isUntimed: Boolean,
    onComplete: () -> Unit
) {
    LaunchedEffect(condition) {
        audioEngine.speakWhisper(condition.name + ". " + condition.instruction)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = condition.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Specialized Grounding Exercise",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = condition.instruction,
                    style = MaterialTheme.typography.titleLarge.copy(lineHeight = 30.sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (isUntimed) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onComplete
            ) {
                Text("Continue to Reflection")
            }
        } else {
            Text(
                text = "Focus on the exercise while timer completes...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExperienceInterviewStage(
    step: Int,
    onStepChange: (Int) -> Unit,
    onQ1Selected: (String) -> Unit,
    onQ2Selected: (String) -> Unit,
    onQ3Selected: (String) -> Unit,
    onQ4Selected: (String) -> Unit,
    onDeclineInterview: () -> Unit,
    onInterviewCompleteComposed: (String) -> Unit,
    onInterviewCompleteDistressed: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (step) {
            0 -> {
                Text(
                    text = "Anchor Sprint Complete",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Are you comfortable to talk about your experience right now?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onStepChange(1) }
                ) {
                    Text("Yes, I'm ready to reflect")
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDeclineInterview
                ) {
                    Text("Not right now")
                }
            }

            1 -> {
                Text(
                    text = "Question 1 of 5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "How intense was your emotional distress during this sprint?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                val options = listOf(
                    "1 - Mild / Barely noticeable",
                    "2 - Moderate / Uncomfortable",
                    "3 - Strong / Intense",
                    "4 - Severe / Overwhelming"
                )
                options.forEach { opt ->
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onQ1Selected(opt)
                            onStepChange(2)
                        }
                    ) {
                        Text(opt)
                    }
                }
            }

            2 -> {
                Text(
                    text = "Question 2 of 5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "What was the main trigger or feeling before starting Anchor?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                val options = listOf(
                    "Anxiety / Panic Attack",
                    "Sensory Overload / Loud Noise",
                    "Intrusive Memory / Flashback",
                    "High Stress / Overwhelm",
                    "Other / Prefer not to say"
                )
                options.forEach { opt ->
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onQ2Selected(opt)
                            onStepChange(3)
                        }
                    ) {
                        Text(opt)
                    }
                }
            }

            3 -> {
                Text(
                    text = "Question 3 of 5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Did the haptic vibration rhythm help bring your focus back?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                val options = listOf(
                    "Very Helpful - Grounded me quickly",
                    "Somewhat Helpful - Slowed my breathing",
                    "Neutral - Didn't notice much",
                    "Not Helpful - Distracting"
                )
                options.forEach { opt ->
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onQ3Selected(opt)
                            onStepChange(4)
                        }
                    ) {
                        Text(opt)
                    }
                }
            }

            4 -> {
                Text(
                    text = "Question 4 of 5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "How comfortable was the voice/whisper guidance?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                val options = listOf(
                    "Soothing & Private",
                    "Clear & Reassuring",
                    "Muted / Didn't use audio",
                    "Distracting / Too loud"
                )
                options.forEach { opt ->
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onQ4Selected(opt)
                            onStepChange(5)
                        }
                    ) {
                        Text(opt)
                    }
                }
            }

            5 -> {
                Text(
                    text = "Question 5 of 5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "How do you feel right now overall?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onInterviewCompleteComposed("Fully Grounded & Calm") }
                ) {
                    Text("Fully Grounded & Calm")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onInterviewCompleteComposed("Slightly Better, Still Shaky") }
                ) {
                    Text("Slightly Better, Still Shaky")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onInterviewCompleteDistressed("Still Distressed / Need Support") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("Still Distressed / Need Immediate Help")
                }
            }
        }
    }
}

@Composable
private fun CriticalEmergencyStage(
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
            text = "1-Minute Anchor Sprint Ended",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Automated Trusted Contacts Alert Sent",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You remained distressed after the 1-minute grounding sprint. An SMS alert has been dispatched to your trusted contacts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRestartSprint,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Try Sprint Again", fontSize = 13.sp)
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
