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
    RECOVERY
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
