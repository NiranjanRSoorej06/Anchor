package com.anchor.domain.routine

/**
 * An ordered collection of [RoutineStep]s that together form one grounding
 * or support routine. This is plain Kotlin — no Android dependencies.
 *
 * @property id Unique identifier for this routine.
 * @property name Human-readable name (must be non-blank).
 * @property steps Ordered list of [RoutineStep]s. Must not exceed [MAX_STEPS].
 */
data class Routine(
    val id: String,
    val name: String,
    val steps: List<RoutineStep>,
) {
    /** Returns the total wall-clock duration in seconds, summing [RoutineStep.Haptic], [RoutineStep.Breathing], and [RoutineStep.Pause] durations. [RoutineStep.SafetyPhrase] counts as 0. */
    fun totalDurationSec(): Int =
        steps.sumOf { step ->
            when (step) {
                is RoutineStep.Haptic -> step.durationSec
                is RoutineStep.Breathing -> step.durationSec
                is RoutineStep.SafetyPhrase -> 0
                is RoutineStep.Pause -> step.durationSec
            }
        }

    companion object {
        /** Maximum number of steps allowed in a single routine. */
        const val MAX_STEPS = 8

        /** Maximum total duration in seconds across all timed steps. */
        const val MAX_TOTAL_SEC = 180
    }
}
