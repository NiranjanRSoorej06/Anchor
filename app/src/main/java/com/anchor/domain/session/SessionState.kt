package com.anchor.domain.session

/**
 * The stage of a single Anchor session. This is deliberately just an enum —
 * no state carries its own data, so a sealed class hierarchy would be
 * unnecessary ceremony for M4.
 *
 * This is NOT a clinical or diagnostic model. It only tracks which screen
 * of the session flow is active. Anchor does not measure heart rate, pulse,
 * or any other physiological signal anywhere in this state machine.
 */
enum class SessionState {
    /** No session in progress. The starting and ending state. */
    IDLE,

    /** Session just started; about to begin grounding. */
    ACTIVATING,

    /** The grounding activity (e.g. a haptic pattern) is running. */
    GROUNDING,

    /** Grounding finished; a brief transition before asking how it went. */
    EASING,

    /** Waiting for the user to report Better / Same / Worse. */
    CHECK_IN,

    /** Session concluded well; a calmer post-session stage. */
    RECOVERY,

    /**
     * Transient: choosing what to try next after a SAME response. Carries
     * no UI of its own — a caller advances out of it immediately via
     * [SessionStateMachine.beginIntervention]. There is no selection logic
     * behind this yet; it exists only so a future module has a state to
     * attach real routing to.
     */
    ROUTING,

    /**
     * A retry activity is running, chosen by ROUTING. Functionally the same
     * kind of stage as GROUNDING (an activity plays, then winds down through
     * EASING) — the separate name only distinguishes "the first attempt"
     * from "a routed retry" for whatever later reads the session history.
     */
    INTERVENTION,

    /**
     * Reached only from a WORSE check-in response. Stops here and goes no
     * further on its own — see [SessionStateMachine.acknowledgeSafetyStop].
     * This state carries NO automatic behavior: no contact, no dialing, no
     * emergency action of any kind. It exists only to mark "stop, and show
     * a safety-oriented screen" as a place in the flow; a future module
     * attaches the actual screen and any user-initiated actions on it.
     */
    SAFETY_STOP
}

/**
 * The user's self-reported response at check-in. This is a plain report,
 * not a clinical assessment — see [SessionStateMachine.submitCheckIn].
 */
enum class CheckInResponse {
    BETTER,
    SAME,
    WORSE
}
