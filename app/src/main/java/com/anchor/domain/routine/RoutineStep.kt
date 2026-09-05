package com.anchor.domain.routine

/**
 * A single step inside a [Routine]. Each variant describes one discrete
 * sensory or pacing element — haptic pattern, breathing pause, safety-phrase
 * playback, or silent delay.
 *
 * This is plain Kotlin: no Android, no Compose, no HapticEngine. The
 * haptic [patternId] is an opaque string that a higher layer resolves to a
 * real vibration pattern at playback time.
 */
sealed interface RoutineStep {

    /**
     * A haptic vibration step.
     *
     * @property patternId Opaque identifier resolved to a vibration pattern
     *   by the haptics layer — intentionally decoupled from `core/haptics`.
     * @property intensity Normalized strength in the range `0f..1f`.
     * @property durationSec How long the step lasts, in whole seconds (> 0).
     */
    data class Haptic(
        val patternId: String,
        val intensity: Float,
        val durationSec: Int,
    ) : RoutineStep

    /**
     * A guided-breathing step (e.g. inhale / exhale pacing).
     *
     * @property durationSec Duration in whole seconds (> 0, ≤ 120).
     */
    data class Breathing(val durationSec: Int) : RoutineStep

    /**
     * Playback of the user's recorded safety phrase.
     *
     * @property clipId Identifies the audio clip to play (blank is invalid).
     */
    data class SafetyPhrase(val clipId: String) : RoutineStep

    /**
     * A silent pause between other steps.
     *
     * @property durationSec Duration in whole seconds (> 0, ≤ 120).
     */
    data class Pause(val durationSec: Int) : RoutineStep
}
