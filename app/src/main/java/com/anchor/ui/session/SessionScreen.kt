package com.anchor.ui.session

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPatterns
import com.anchor.domain.session.CheckInResponse
import com.anchor.domain.session.SessionState
import com.anchor.domain.session.SessionStateMachine
import kotlinx.coroutines.delay

/** How long the EASING transition holds before moving on to CHECK_IN. Not
 * evidence-based — see plan.md's own note that this whole screen is a
 * placeholder UX layer, not a claim about how long anything should take. */
private const val EASING_DELAY_MS = 1500L

/**
 * The real session screen: reacts to [SessionStateMachine.state] and drives
 * [HapticEngine] the same way SessionStateTestScreen does, but with real
 * copy and automatic transitions where the state itself has nothing for a
 * user to decide (ACTIVATING, ROUTING, EASING) instead of dev-only buttons.
 *
 * Deliberately generic where a teammate owns the real content: the
 * pulsing circle during GROUNDING/INTERVENTION stands in for whatever
 * Dev C's real BreathingView becomes; SAFETY_STOP shows the same honest
 * placeholder as the dev screen, since the real safety screen (trusted
 * contact, SMS) is Dev B's build.
 */
@Composable
fun SessionScreen(
    machine: SessionStateMachine,
    hapticEngine: HapticEngine,
    onExitToHome: () -> Unit
) {
    val state by machine.state.collectAsState()

    // Haptic follows exactly the states where "an activity is running" —
    // stops immediately for everything else, including SAFETY_STOP.
    LaunchedEffect(state) {
        if (state == SessionState.GROUNDING || state == SessionState.INTERVENTION) {
            hapticEngine.play(HapticPatterns.DOUBLE_PULSE)
        } else {
            hapticEngine.stop()
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
            when (state) {
                SessionState.IDLE, SessionState.ACTIVATING -> {
                    // ACTIVATING auto-advances near-instantly; nothing to show.
                }

                SessionState.GROUNDING -> ActivityStage(
                    onContinue = { machine.finishGrounding() },
                    onCancel = { machine.cancel() }
                )

                SessionState.INTERVENTION -> ActivityStage(
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
private fun ActivityStage(onContinue: () -> Unit, onCancel: (() -> Unit)?) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(180.dp * scale)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )

        Button(modifier = Modifier.fillMaxWidth(), onClick = onContinue) {
            Text("Continue")
        }

        if (onCancel != null) {
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onCancel) {
                Text("Cancel")
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
        // Honest placeholder — see class doc. No contact/dial/SMS action exists yet.
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
