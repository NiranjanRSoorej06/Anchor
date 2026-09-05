package com.anchor.domain.routine

/**
 * Pure-Kotlin validator for [Routine] instances. Collects every failure
 * without short-circuiting so the caller can present the full list at once.
 *
 * This is plain Kotlin: no Android, no Compose.
 */
object RoutineValidator {

    /**
     * Validate [routine] and return [ValidationResult.Valid] if every rule
     * passes, or [ValidationResult.Invalid] with all reasons that failed.
     */
    fun validate(routine: Routine): ValidationResult {
        val reasons = mutableListOf<String>()

        // Routine-level rules
        if (routine.name.isBlank()) reasons += "Name must not be blank"
        if (routine.steps.isEmpty()) reasons += "Steps must not be empty"
        if (routine.steps.size > Routine.MAX_STEPS) {
            reasons += "Steps must not exceed ${Routine.MAX_STEPS}"
        }
        if (routine.totalDurationSec() > Routine.MAX_TOTAL_SEC) {
            reasons += "Total duration must not exceed ${Routine.MAX_TOTAL_SEC}s"
        }

        // Per-step rules
        routine.steps.forEachIndexed { index, step ->
            when (step) {
                is RoutineStep.Haptic -> {
                    if (step.patternId.isBlank()) {
                        reasons += "Step ${index + 1}: Haptic patternId must not be blank"
                    }
                    if (step.intensity < 0f || step.intensity > 1f) {
                        reasons += "Step ${index + 1}: Haptic intensity must be 0f..1f"
                    }
                    if (step.durationSec <= 0) {
                        reasons += "Step ${index + 1}: Haptic durationSec must be > 0"
                    }
                }
                is RoutineStep.Breathing -> {
                    if (step.durationSec <= 0 || step.durationSec > 120) {
                        reasons += "Step ${index + 1}: Breathing durationSec must be 1..120"
                    }
                }
                is RoutineStep.Pause -> {
                    if (step.durationSec <= 0 || step.durationSec > 120) {
                        reasons += "Step ${index + 1}: Pause durationSec must be 1..120"
                    }
                }
                is RoutineStep.SafetyPhrase -> {
                    if (step.clipId.isBlank()) {
                        reasons += "Step ${index + 1}: SafetyPhrase clipId must not be blank"
                    }
                }
                is RoutineStep.CustomAudio -> {
                    if (step.uri.isBlank()) {
                        reasons += "Step ${index + 1}: CustomAudio uri must not be blank"
                    }
                }
            }
        }

        return if (reasons.isEmpty()) ValidationResult.Valid
        else ValidationResult.Invalid(reasons)
    }
}

/**
 * The result of validating a [Routine]. [Valid] means the routine satisfies
 * every rule; [Invalid] carries the full list of failure reasons.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reasons: List<String>) : ValidationResult
}
