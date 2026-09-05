package com.anchor.core.haptics

/**
 * The catalog of named, deterministic haptic patterns Anchor can play.
 *
 * Smooth breathing wave patterns calibrated to match the visualizer UI.
 */
object HapticPatterns {

    /** Minimal single-pulse pattern used to exercise engines. Not a product pattern. */
    val TEST_PULSE = HapticPattern.TEST_PULSE

    /**
     * Smooth breathing pulse pattern replacing heartbeat taps with a gentle breathing wave.
     */
    val DOUBLE_PULSE = HapticPattern(
        id = "double_pulse",
        timings = longArrayOf(0, 100, 100, 100, 100, 300),
        amplitudes = intArrayOf(0, 100, 180, 255, 120, 0),
        repeatIndex = -1
    )

    /**
     * A single clear pulse repeated slowly, with an obvious silent gap
     * between pulses. Useful as a plain "still here" grounding cue.
     */
    val SLOW_PULSE = HapticPattern(
        id = "slow_pulse",
        timings = longArrayOf(0, 200, 900),
        amplitudes = intArrayOf(0, 200, 0),
        repeatIndex = 0
    )

    /**
     * A continuous smooth ramp from silent to peak intensity over exactly 4 seconds (4000ms).
     * 10 steps of 400ms. Synchronized with the 4s Inhale phase of the breathing visualizer.
     */
    val BREATHING_IN = HapticPattern(
        id = "breathing_in",
        timings = longArrayOf(0, 400, 400, 400, 400, 400, 400, 400, 400, 400),
        amplitudes = intArrayOf(0, 25, 50, 75, 100, 130, 160, 190, 220, 255),
        repeatIndex = -1
    )

    /**
     * A continuous smooth ramp from peak intensity down to silent over exactly 6 seconds (6000ms).
     * 10 steps of 600ms. Synchronized with the 6s Exhale phase of the breathing visualizer.
     */
    val BREATHING_OUT = HapticPattern(
        id = "breathing_out",
        timings = longArrayOf(0, 600, 600, 600, 600, 600, 600, 600, 600, 600),
        amplitudes = intArrayOf(255, 220, 190, 160, 130, 100, 75, 50, 25, 0),
        repeatIndex = -1
    )

    /** Every pattern in the catalog. */
    val ALL: List<HapticPattern> = listOf(TEST_PULSE, DOUBLE_PULSE, SLOW_PULSE, BREATHING_IN, BREATHING_OUT)
}
