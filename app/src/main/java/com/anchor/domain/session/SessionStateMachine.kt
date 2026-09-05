package com.anchor.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The result of attempting a transition. Deliberately just two cases — this
 * is a hackathon MVP, not a full error-handling framework.
 */
sealed interface TransitionResult {
    data object Success : TransitionResult
    data class Rejected(val reason: String) : TransitionResult
}

/**
 * A small, deterministic state machine for a single Anchor session.
 *
 * This is plain Kotlin: no Android, no Compose, no HapticEngine. It only
 * answers "what stage are we in, and is this action allowed right now?".
 * A caller (a coordinator, or in M4's case the dev screen itself) reacts to
 * state changes to do anything else, such as playing a haptic pattern.
 *
 * Every public method attempts exactly one transition and returns a
 * [TransitionResult]. An action that doesn't make sense from the current
 * state is rejected and the state is left completely unchanged — this is
 * also what protects against accidental double-taps (e.g. calling
 * [start] twice just rejects the second call, it never starts a second
 * session).
 *
 * BETTER / SAME / WORSE in [submitCheckIn] are plain user-reported
 * responses. This class does not interpret them, does not diagnose
 * anything, and does not trigger any emergency or contact behavior.
 * BETTER moves toward RECOVERY, SAME moves toward another attempt via
 * ROUTING, and WORSE moves to SAFETY_STOP — which is itself inert: reaching
 * it does not, by itself, contact anyone or do anything beyond marking the
 * spot in the flow where a future module attaches real safety behavior.
 */
class SessionStateMachine {

    private val _state = MutableStateFlow(SessionState.IDLE)

    /** Observable current state. Collect this from Compose via collectAsState(). */
    val state: StateFlow<SessionState> = _state.asStateFlow()

    val currentState: SessionState
        get() = _state.value

    fun start(): TransitionResult =
        transition(from = SessionState.IDLE, to = SessionState.ACTIVATING)

    fun beginGrounding(): TransitionResult =
        transition(from = SessionState.ACTIVATING, to = SessionState.GROUNDING)

    fun finishGrounding(): TransitionResult =
        transition(from = SessionState.GROUNDING, to = SessionState.EASING)

    fun completeEasing(): TransitionResult =
        transition(from = SessionState.EASING, to = SessionState.CHECK_IN)

    /**
     * BETTER moves on to RECOVERY. SAME moves to ROUTING to try something
     * else. WORSE moves to SAFETY_STOP — see the class doc for why that
     * carries no automatic behavior of its own.
     */
    fun submitCheckIn(response: CheckInResponse): TransitionResult {
        val target = when (response) {
            CheckInResponse.BETTER -> SessionState.RECOVERY
            CheckInResponse.SAME -> SessionState.ROUTING
            CheckInResponse.WORSE -> SessionState.SAFETY_STOP
        }
        return transition(from = SessionState.CHECK_IN, to = target)
    }

    fun finishRecovery(): TransitionResult =
        transition(from = SessionState.RECOVERY, to = SessionState.IDLE)

    /** Only valid while GROUNDING, per the M4 spec. Any other state rejects. */
    fun cancel(): TransitionResult =
        transition(from = SessionState.GROUNDING, to = SessionState.IDLE)

    /**
     * ROUTING is transient and has no UI of its own (see [SessionState.ROUTING]);
     * a caller advances out of it immediately after observing it. There is no
     * selection logic behind this call — it always succeeds when currently
     * ROUTING and always lands on INTERVENTION.
     */
    fun beginIntervention(): TransitionResult =
        transition(from = SessionState.ROUTING, to = SessionState.INTERVENTION)

    /** Mirrors [finishGrounding] exactly: an activity finished, wind down through EASING. */
    fun finishIntervention(): TransitionResult =
        transition(from = SessionState.INTERVENTION, to = SessionState.EASING)

    /**
     * The only way out of SAFETY_STOP. Deliberately requires an explicit
     * call — nothing in this class transitions out of SAFETY_STOP on its
     * own, by design (see [SessionState.SAFETY_STOP]).
     */
    fun acknowledgeSafetyStop(): TransitionResult =
        transition(from = SessionState.SAFETY_STOP, to = SessionState.IDLE)

    private fun transition(from: SessionState, to: SessionState): TransitionResult {
        val current = _state.value
        if (current != from) {
            return TransitionResult.Rejected(reason = "$current → $to")
        }
        _state.value = to
        return TransitionResult.Success
    }
}
