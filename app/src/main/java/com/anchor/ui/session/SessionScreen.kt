package com.anchor.ui.session

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.core.audio.DebugAudioEngine
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPatterns
import com.anchor.domain.session.CheckInResponse
import com.anchor.domain.session.SessionState
import com.anchor.domain.session.SessionStateMachine
import kotlinx.coroutines.delay

/** How long the EASING transition holds before moving on to CHECK_IN. */
private const val EASING_DELAY_MS = 1500L

/**
 * The real session screen: reacts to [SessionStateMachine.state] and drives
 * [HapticEngine] and [AudioDeliveryEngine].
 *
 * Supports Dual-Mode Audio & Haptic Delivery:
 * - If earbuds are attached: speaks private whisper prompts directly into the earbud.
 * - If no earbuds are attached: mutes 100% of speaker sound for silent haptic-only mode.
 */
@Composable
fun SessionScreen(
    machine: SessionStateMachine,
    hapticEngine: HapticEngine,
    audioEngine: AudioDeliveryEngine = DebugAudioEngine(),
    onExitToHome: () -> Unit
) {
    val state by machine.state.collectAsState()
    val isWhisper = audioEngine.isWhisperModeActive()

    // Haptic breathing loop synchronized with the visualizer:
    // 4 seconds continuous ascending vibration (Inhale) + 6 seconds continuous descending vibration (Exhale).
    LaunchedEffect(state) {
        if (state == SessionState.GROUNDING || state == SessionState.INTERVENTION) {
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

    // Auto-advance the states that have nothing for a user to decide.
    LaunchedEffect(state) {
        when (state) {
            SessionState.ACTIVATING -> machine.beginGrounding()
            SessionState.ROUTING -> machine.beginIntervention()
            SessionState.EASING -> {
                delay(EASING_DELAY_MS)
                machine.completeEasing()
            }
            else -> Unit
        }
    }

    // Leaving the session entirely is always "we're back at IDLE."
    LaunchedEffect(state) {
        if (state == SessionState.IDLE) onExitToHome()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isWhisper) "🎧 Whisper Mode · Private Earbuds" else "🔊 Speaker Mode · Soothing Voice",
                style = MaterialTheme.typography.labelSmall,
                color = if (isWhisper) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (state) {
                SessionState.IDLE, SessionState.ACTIVATING -> {
                    // ACTIVATING auto-advances near-instantly; nothing to show.
                }

                SessionState.GROUNDING -> ActivityStage(
                    audioEngine = audioEngine,
                    onContinue = { machine.finishGrounding() },
                    onCancel = { machine.cancel() }
                )

                SessionState.INTERVENTION -> ActivityStage(
                    audioEngine = audioEngine,
                    onContinue = { machine.finishIntervention() },
                    onCancel = null
                )

                SessionState.ROUTING -> Text(
                    "One moment…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SessionState.EASING -> Text(
                    "Take a moment.",
                    style = MaterialTheme.typography.titleMedium
                )

                SessionState.CHECK_IN -> CheckInStage(
                    onBetter = { machine.submitCheckIn(CheckInResponse.BETTER) },
                    onSame = { machine.submitCheckIn(CheckInResponse.SAME) },
                    onWorse = { machine.submitCheckIn(CheckInResponse.WORSE) }
                )

                SessionState.RECOVERY -> RecoveryStage(
                    onDone = { machine.finishRecovery() }
                )

                SessionState.SAFETY_STOP -> SafetyStopStage(
                    onAcknowledge = { machine.acknowledgeSafetyStop() }
                )
            }
        }
    }
}

@Composable
private fun ActivityStage(
    audioEngine: AudioDeliveryEngine,
    onContinue: () -> Unit,
    onCancel: (() -> Unit)?
) {
    var phaseText by remember { mutableStateOf("Breathe In…") }
    var phaseSubtext by remember { mutableStateOf("Inhale slowly through your nose (4s)") }

    // Scientific Standard Resonant Paced Breathing:
    // 4s Inhale + 6s Exhale = 10s total cycle (6 breaths/minute).
    val infiniteTransition = rememberInfiniteTransition(label = "resonantBreathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 10000 // 10s per cycle = 6 breaths/min
                0.75f at 0 with LinearOutSlowInEasing
                1.25f at 4000 with FastOutSlowInEasing
                0.75f at 10000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "visualizerScale"
    )

    LaunchedEffect(Unit) {
        audioEngine.speakWhisper("You are in a safe place.")
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
            // Outer translucent pulse aura
            Box(
                modifier = Modifier
                    .size(190.dp * scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
            // Inner solid anchor circle
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

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(modifier = Modifier.fillMaxWidth(), onClick = onContinue) {
                Text("I feel steady")
            }

            if (onCancel != null) {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onCancel) {
                    Text("Pause")
                }
            }
        }
    }
}

@Composable
private fun CheckInStage(onBetter: () -> Unit, onSame: () -> Unit, onWorse: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "How does it feel now?",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = onBetter) { Text("Better") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onSame) { Text("About the same") }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onWorse) { Text("Worse") }
    }
}

@Composable
private fun RecoveryStage(onDone: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "You're okay. Take your time.",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = onDone) { Text("Done") }
    }
}

@Composable
private fun SafetyStopStage(onAcknowledge: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Let's pause here.",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            "This is a placeholder screen. No contact, dialing, or emergency " +
                "action happens here yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = onAcknowledge) { Text("Acknowledge") }
    }
}
