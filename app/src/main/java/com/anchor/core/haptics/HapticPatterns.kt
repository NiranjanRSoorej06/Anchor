package com.anchor.core.haptics

/**
 * The catalog of named, deterministic haptic patterns Anchor can play.
 *
 * This is the ONLY place raw timing/amplitude arrays should be written.
 * Everywhere else in the app should call `hapticEngine.play(HapticPatterns.DOUBLE_PULSE)`
 * rather than constructing a [HapticPattern] by hand — that keeps the "what
 * does this feel like" decision in one file, and keeps callers ignorant of
 * the underlying waveform shape.
 *
 * These are interaction mechanisms, not medical devices: nothing here (or
 * anywhere referencing these patterns) may claim a specific vibration is
 * clinically proven to treat PTSD. See evidence.md and plan.md §11.1 for the
 * claims Anchor is and isn't allowed to make. A pattern is later attached to
 * an evidence-informed intervention by a higher layer — this file only
 * defines what things feel like.
 *
 * Anchor has no heart-rate, pulse, or other biometric sensor, and never will
 * as part of this module. "Double pulse" is only the name of a vibration
 * rhythm (two short taps, like a knock) — it does not measure, sense, or
 * represent any physiological signal. Do not name or describe a pattern in
 * a way that implies otherwise.
 *
 * Every pattern here is a fixed, hand-authored array — same input, same
 * output, every time. There is no randomness and no device-specific
 * branching; that belongs to [SystemHapticEngine] (whether the platform can
 * honor amplitude at all) not to the pattern definition.
 */
object HapticPatterns {

    /** Minimal single-pulse pattern used to exercise engines. Not a product pattern. */
    val TEST_PULSE = HapticPattern.TEST_PULSE

    /**
     * A soft double-tap rhythm: two short pulses close together, then a
     * longer pause before repeating. Purely a tactile rhythm — it does not
     * measure or represent a heartbeat or any other physiological signal.
     * Kept deliberately gentle (not full amplitude) so it reads as a
     * background presence rather than an alarm.
     */
    val DOUBLE_PULSE = HapticPattern(
        id = "double_pulse",
        //           gap  tap1  gap  tap2   pause
        timings = longArrayOf(0, 90, 100, 110, 700),
        amplitudes = intArrayOf(0, 180, 0, 140, 0),
        repeatIndex = 0
    )

    /**
     * A single clear pulse repeated slowly, with an obvious silent gap
     * between pulses — distinct from DOUBLE_PULSE's paired taps. Useful as
     * a plain "still here" grounding cue.
     */
    val SLOW_PULSE = HapticPattern(
        id = "slow_pulse",
        //           gap  pulse   pause
        timings = longArrayOf(0, 200, 900),
        amplitudes = intArrayOf(0, 200, 0),
        repeatIndex = 0
    )

    /**
     * A smooth ramp from silent to strong over ~4 seconds, in small steps.
     * Intended to be felt alongside a slow inhale. One-shot: it does not
     * repeat, so a higher layer plays BREATHING_IN then BREATHING_OUT in
     * sequence to build a breathing cycle.
     */
    val BREATHING_IN = HapticPattern(
        id = "breathing_in",
        timings = longArrayOf(0, 500, 500, 500, 500, 500, 500, 500, 500),
        amplitudes = intArrayOf(0, 32, 64, 96, 128, 160, 192, 224, 255),
        repeatIndex = -1
    )

    /**
     * The mirror of BREATHING_IN: a smooth ramp from strong down to silent
     * over ~4 seconds, for a slow exhale. Also one-shot.
     */
    val BREATHING_OUT = HapticPattern(
        id = "breathing_out",
        timings = longArrayOf(0, 500, 500, 500, 500, 500, 500, 500, 500),
        amplitudes = intArrayOf(255, 224, 192, 160, 128, 96, 64, 32, 0),
        repeatIndex = -1
    )

    /** Every pattern in the catalog, for the dev screen and for tests that must check "all of them". */
    val ALL: List<HapticPattern> = listOf(TEST_PULSE, DOUBLE_PULSE, SLOW_PULSE, BREATHING_IN, BREATHING_OUT)
}
