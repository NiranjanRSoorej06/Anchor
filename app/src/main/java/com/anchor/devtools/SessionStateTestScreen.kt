package com.anchor.devtools

import android.content.Context
import android.os.Build
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchor.core.haptics.DebugHapticEngine
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPatterns
import com.anchor.core.haptics.SystemHapticEngine
import com.anchor.domain.session.CheckInResponse
import com.anchor.domain.session.SessionState
import com.anchor.domain.session.SessionStateMachine
import com.anchor.domain.session.TransitionResult

/**
 * SESSION STATE TEST (dev) — a developer-only screen to manually exercise
 * [SessionStateMachine]. It is NOT the final Anchor session UI.
 *
 * Only shows the button(s) valid for the current state, plus a small
 * "force an invalid transition" section at the bottom so a developer can
 * verify rejection behavior without needing to hack the state by hand.
 *
 * Haptic integration: this screen (not the state machine) reacts to state
 * changes and calls [HapticEngine.play] when entering GROUNDING or
 * INTERVENTION (both are "an activity is running" stages), and
 * [HapticEngine.stop] otherwise. This is the "simpler option" the M4
 * brief allows: [SessionStateMachine] stays pure Kotlin with zero knowledge
 * of haptics, and the coordination happens in a `LaunchedEffect` here —
 * no ViewModel or event bus needed for a dev screen this small.
 *
 * A second `LaunchedEffect` advances ROUTING to INTERVENTION automatically,
 * the instant ROUTING is observed — ROUTING has no UI of its own (see
 * [SessionState.ROUTING]) and no selection logic exists yet, so there is
 * nothing for a user to choose here.
 *
 * SAFETY_STOP is shown with an explicit acknowledgement button and on-screen
 * text stating plainly that no contact or emergency action happens here —
 * see [SessionState.SAFETY_STOP].
 *
 * Delete this file and its call site in MainActivity once the real Anchor
 * session screen (a later module) replaces it.
 */
@Composable
fun SessionStateTestScreen() {
    val context = LocalContext.current
    val hapticEngine = remember { buildHapticEngineForThisDevice(context) }
    val machine = remember { SessionStateMachine() }
    val state by machine.state.collectAsState()
    var lastRejection by remember { mutableStateOf<String?>(null) }

    fun attempt(result: TransitionResult) {
        lastRejection = when (result) {
            is TransitionResult.Success -> null
            is TransitionResult.Rejected -> result.reason
        }
    }

    // The one place this screen touches haptics: play a pattern for as long
    // as an activity (GROUNDING or its routed retry, INTERVENTION) is
    // running, stop otherwise — including immediately on SAFETY_STOP.
    // HapticEngine only — never android.os.Vibrator directly.
    LaunchedEffect(state) {
        if (state == SessionState.GROUNDING || state == SessionState.INTERVENTION) {
            hapticEngine.play(HapticPatterns.SLOW_PULSE)
        } else {
            hapticEngine.stop()
        }
    }

    // ROUTING is transient and has no UI (see SessionState.ROUTING) — advance
    // out of it the instant it's observed. No selection logic exists yet.
    LaunchedEffect(state) {
        if (state == SessionState.ROUTING) {
            attempt(machine.beginIntervention())
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Session State Test (dev)", style = MaterialTheme.typography.headlineSmall)

            Text("Current state:", style = MaterialTheme.typography.titleSmall)
            Text(state.name, style = MaterialTheme.typography.headlineMedium)

            lastRejection?.let { reason ->
                Text(
                    text = "Rejected:\n$reason",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            // Only the action(s) valid for the current state are shown here.
            when (state) {
                SessionState.IDLE -> {
                    DevButton("START SESSION") { attempt(machine.start()) }
                }
                SessionState.ACTIVATING -> {
                    DevButton("BEGIN GROUNDING") { attempt(machine.beginGrounding()) }
                }
                SessionState.GROUNDING -> {
                    DevButton("FINISH GROUNDING") { attempt(machine.finishGrounding()) }
                    DevButton("CANCEL") { attempt(machine.cancel()) }
                }
                SessionState.EASING -> {
                    DevButton("COMPLETE EASING") { attempt(machine.completeEasing()) }
                }
                SessionState.CHECK_IN -> {
                    DevButton("BETTER") { attempt(machine.submitCheckIn(CheckInResponse.BETTER)) }
                    DevButton("SAME") { attempt(machine.submitCheckIn(CheckInResponse.SAME)) }
                    DevButton("WORSE") { attempt(machine.submitCheckIn(CheckInResponse.WORSE)) }
                }
                SessionState.RECOVERY -> {
                    DevButton("FINISH RECOVERY") { attempt(machine.finishRecovery()) }
                }
                SessionState.ROUTING -> {
                    // No button: this LaunchedEffect above advances it automatically.
                    Text(
                        "Routing… (no selection logic yet — advances immediately)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                SessionState.INTERVENTION -> {
                    DevButton("FINISH INTERVENTION") { attempt(machine.finishIntervention()) }
                }
                SessionState.SAFETY_STOP -> {
                    Text(
                        "Placeholder only: no contact, dialing, or emergency\n" +
                            "action happens here. A future module attaches the\n" +
                            "real safety screen.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    DevButton("ACKNOWLEDGE (return to IDLE)") { attempt(machine.acknowledgeSafetyStop()) }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            Text(
                "Debug: force a transition regardless of state\n(used to test rejection)",
                style = MaterialTheme.typography.labelSmall
            )
            // completeEasing()'s target is CHECK_IN, so calling it from any
            // state other than EASING demonstrates an "X → CHECK_IN" rejection.
            DevButton("Force completeEasing (→ CHECK_IN)") { attempt(machine.completeEasing()) }
            // submitCheckIn(BETTER)'s target is RECOVERY, so calling it from
            // any state other than CHECK_IN demonstrates an "X → RECOVERY" rejection.
            DevButton("Force submitCheckIn BETTER (→ RECOVERY)") {
                attempt(machine.submitCheckIn(CheckInResponse.BETTER))
            }
            // acknowledgeSafetyStop()'s target is IDLE, but its *source* is the
            // interesting thing to test: it should reject from anywhere except
            // SAFETY_STOP, which is what keeps it from ever firing automatically.
            DevButton("Force acknowledgeSafetyStop (→ IDLE)") {
                attempt(machine.acknowledgeSafetyStop())
            }
        }
    }
}

@Composable
private fun DevButton(label: String, onClick: () -> Unit) {
    Button(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Text(label)
    }
}

/**
 * Duplicated from DevHapticTestScreen.kt on purpose: M4 preserves M1-M3
 * files untouched rather than extracting a shared helper. See the M4
 * report for why.
 */
private fun buildHapticEngineForThisDevice(context: Context): HapticEngine {
    val looksLikeEmulator = Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu") ||
        Build.PRODUCT.contains("sdk")

    return if (looksLikeEmulator) DebugHapticEngine() else SystemHapticEngine(context)
}
