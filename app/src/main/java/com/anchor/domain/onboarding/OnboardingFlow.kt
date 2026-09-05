package com.anchor.domain.onboarding

import com.anchor.domain.session.TransitionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A deterministic, linear state machine for the Anchor onboarding flow.
 *
 * Models the S0–S12 sequence from `docs/onboarding-research.md` §5. Pure
 * Kotlin — no Android, no Compose, no side effects. It answers "what step
 * are we in and is this action allowed right now?".
 *
 * ## Transition semantics
 *
 * Every public method attempts exactly **one** transition and returns a
 * [TransitionResult]. An action that doesn't make sense from the current
 * step is rejected and the state is left completely unchanged — this
 * protects against accidental double-taps and out-of-order calls.
 *
 * | Method            | From (valid)         | To         | Records? |
 * |-------------------|----------------------|------------|----------|
 * | [completeCurrent] | WELCOME … PHRASE     | next step  | Yes      |
 * | [skipCurrent]     | CONSENT … CONTACT    | next step  | No       |
 * | [finish]          | CONTACT              | DONE       | Yes      |
 * | [restart]         | DONE                 | WELCOME    | Clears   |
 *
 * ## Skip semantics & S1-spec deviation
 *
 * The onboarding-research spec (§5, S1) says "Skip setup" on the consent
 * screen jumps directly to S7 (SITUATION). **This machine does not model
 * that jump.** [skipCurrent] always advances to the *linear* next step
 * (e.g. CONSENT → PCL5), never to an arbitrary later step. UI-layer
 * routing that maps "skip setup" to SITUATION is a presentation concern
 * and lives outside this class.
 *
 * In short: the machine is strictly linear; the UI may be non-linear.
 */
class OnboardingFlow {

    private val _state = MutableStateFlow(OnboardingStep.WELCOME)

    /** Observable current step. Collect from Compose via `collectAsState()`. */
    val state: StateFlow<OnboardingStep> = _state.asStateFlow()

    /** The current step, without the [StateFlow] wrapper. */
    val currentStep: OnboardingStep
        get() = _state.value

    /** `true` once [DONE] has been reached. */
    val isComplete: Boolean
        get() = currentStep == OnboardingStep.DONE

    private val _completedSections = mutableSetOf<OnboardingStep>()

    /**
     * Steps the user has explicitly completed (via [completeCurrent] or
     * [finish]). Skipped steps are **not** recorded here.
     */
    val completedSections: Set<OnboardingStep>
        get() = _completedSections.toSet()

    // ── Public transitions ─────────────────────────────────────────────

    /**
     * Mark the current step as completed, then advance to the next step.
     *
     * Valid from every step except [OnboardingStep.DONE]. Calling from
     * [OnboardingStep.CONTACT] records CONTACT and moves to DONE — this is
     * equivalent to [finish] but uses the generic advance path. If you
     * prefer the semantic signal, call [finish] instead.
     *
     * Double-call safety: calling twice in quick succession from adjacent
     * steps succeeds both times (the first advances, the second advances
     * again from the new step).
     */
    fun completeCurrent(): TransitionResult {
        val current = _state.value
        if (current == OnboardingStep.DONE) {
            return TransitionResult.Rejected(reason = "$current → next")
        }
        _completedSections.add(current)
        _state.value = current.next()
        return TransitionResult.Success
    }

    /**
     * Advance to the next step **without** recording the current step in
     * [completedSections].
     *
     * Valid from every step except [OnboardingStep.WELCOME] and
     * [OnboardingStep.DONE]. The user must at least see the welcome screen;
     * once they reach DONE there is nowhere to skip to.
     *
     * **UI-level jumps** (e.g. CONSENT → SITUATION per the S1 "Skip setup"
     * option) are *not* modelled here — see class KDoc. This method always
     * moves forward by exactly one linear step.
     */
    fun skipCurrent(): TransitionResult {
        val current = _state.value
        if (current == OnboardingStep.WELCOME || current == OnboardingStep.DONE) {
            return TransitionResult.Rejected(reason = "$current → next")
        }
        _state.value = current.next()
        return TransitionResult.Success
    }

    /**
     * Complete onboarding: move from [OnboardingStep.CONTACT] to
     * [OnboardingStep.DONE], recording CONTACT in [completedSections].
     *
     * Valid **only** from [OnboardingStep.CONTACT]. Use this instead of
     * [completeCurrent] when you want the semantic signal that the user
     * tapped through the final screen.
     */
    fun finish(): TransitionResult {
        val current = _state.value
        if (current != OnboardingStep.CONTACT) {
            return TransitionResult.Rejected(reason = "$current → ${OnboardingStep.DONE}")
        }
        _completedSections.add(current)
        _state.value = OnboardingStep.DONE
        return TransitionResult.Success
    }

    /**
     * Reset the machine to [OnboardingStep.WELCOME] and clear all records
     * from [completedSections]. Intended for the "re-run onboarding"
     * action in Settings.
     *
     * Valid only from [OnboardingStep.DONE].
     */
    fun restart(): TransitionResult {
        val result = transition(from = OnboardingStep.DONE, to = OnboardingStep.WELCOME)
        if (result is TransitionResult.Success) {
            _completedSections.clear()
        }
        return result
    }

    // ── Internal helpers ───────────────────────────────────────────────

    /**
     * Single-guard transition: only fires when the machine is in [from],
     * otherwise rejects and leaves state untouched. Mirrors the pattern
     * in `SessionStateMachine`.
     */
    private fun transition(from: OnboardingStep, to: OnboardingStep): TransitionResult {
        val current = _state.value
        if (current != from) {
            return TransitionResult.Rejected(reason = "$current → $to")
        }
        _state.value = to
        return TransitionResult.Success
    }

    /** Return the next step in the linear onboarding sequence. */
    private fun OnboardingStep.next(): OnboardingStep = when (this) {
        OnboardingStep.WELCOME   -> OnboardingStep.CONSENT
        OnboardingStep.CONSENT   -> OnboardingStep.PCL5
        OnboardingStep.PCL5      -> OnboardingStep.SITUATION
        OnboardingStep.SITUATION -> OnboardingStep.SENSORY
        OnboardingStep.SENSORY   -> OnboardingStep.PHRASE
        OnboardingStep.PHRASE    -> OnboardingStep.CONTACT
        OnboardingStep.CONTACT   -> OnboardingStep.DONE
        OnboardingStep.DONE      -> error("DONE has no next step")
    }
}
