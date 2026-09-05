package com.anchor.domain.followup

/**
 * Pure-Kotlin validator for [FollowUpCheckIn] instances. Collects every
 * failure without short-circuiting so the caller can present the full
 * list at once.
 *
 * This is plain Kotlin: no Android, no Compose.
 */
object FollowUpValidator {

    /**
     * Validate [checkIn] and return [ValidationResult.Valid] if every rule
     * passes, or [ValidationResult.Invalid] with all reasons that failed.
     *
     * A fully-skipped check-in (empty [FollowUpCheckIn.triggerIds], null
     * distress, null note) is **valid** — it still records that follow-up
     * happened so the system does not re-prompt.
     */
    fun validate(checkIn: FollowUpCheckIn): ValidationResult {
        val reasons = mutableListOf<String>()

        if (checkIn.episodeId.isBlank()) {
            reasons += "episodeId must not be blank"
        }

        checkIn.distress?.let { d ->
            if (d !in 1..5) {
                reasons += "distress must be 1..5, was $d"
            }
        }

        checkIn.note?.let { n ->
            if (n.length > FollowUpCheckIn.MAX_NOTE_CHARS) {
                reasons += "note must not exceed ${FollowUpCheckIn.MAX_NOTE_CHARS} chars, was ${n.length}"
            }
        }

        checkIn.triggerIds.forEachIndexed { index, id ->
            if (id.isBlank()) {
                reasons += "triggerIds must not contain blank entries (index $index)"
            }
        }

        return if (reasons.isEmpty()) ValidationResult.Valid
        else ValidationResult.Invalid(reasons)
    }
}

/**
 * The result of validating a [FollowUpCheckIn]. [Valid] means the check-in
 * satisfies every rule; [Invalid] carries the full list of failure reasons.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reasons: List<String>) : ValidationResult
}
