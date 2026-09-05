package com.anchor.domain.journal

/**
 * A local, on-device-only journal entry.
 *
 * Deliberately structured around "what helped" rather than "what happened" —
 * per `docs/vision.md`'s journaling guidance, Anchor avoids collecting a
 * free-text trauma narrative. [whatHelped] and [reflection] are still plain
 * text fields the user controls, but the prompts steer toward coping and
 * recovery rather than re-describing the triggering event.
 *
 * This is plain Kotlin: no Android, no Compose.
 *
 * @property id Unique identifier for this entry.
 * @property createdAtMillis Unix epoch millis when the entry was written.
 * @property whatHelped Optional short note on what helped this time
 *   (≤ [MAX_FIELD_CHARS] chars).
 * @property reflection Optional freeform reflection note
 *   (≤ [MAX_FIELD_CHARS] chars).
 * @property linkedEpisodeId Optional [com.anchor.domain.history.Episode.id]
 *   this entry relates to, or `null` for a standalone entry.
 */
data class JournalEntry(
    val id: String,
    val createdAtMillis: Long,
    val whatHelped: String? = null,
    val reflection: String? = null,
    val linkedEpisodeId: String? = null,
) {
    companion object {
        /** Maximum allowed characters for [whatHelped] and [reflection]. */
        const val MAX_FIELD_CHARS = 280
    }
}

/**
 * Pure-Kotlin validator for [JournalEntry] instances. Collects every
 * failure without short-circuiting so the caller can present the full
 * list at once.
 */
object JournalValidator {

    /**
     * Validate [entry] and return [ValidationResult.Valid] if every rule
     * passes, or [ValidationResult.Invalid] with all reasons that failed.
     */
    fun validate(entry: JournalEntry): ValidationResult {
        val reasons = mutableListOf<String>()

        if (entry.id.isBlank()) reasons += "id must not be blank"
        if (entry.createdAtMillis < 0) reasons += "createdAtMillis must not be negative"
        if (entry.whatHelped.isNullOrBlank() && entry.reflection.isNullOrBlank()) {
            reasons += "at least one of whatHelped or reflection must be non-blank"
        }
        entry.whatHelped?.let {
            if (it.length > JournalEntry.MAX_FIELD_CHARS) {
                reasons += "whatHelped must not exceed ${JournalEntry.MAX_FIELD_CHARS} characters"
            }
        }
        entry.reflection?.let {
            if (it.length > JournalEntry.MAX_FIELD_CHARS) {
                reasons += "reflection must not exceed ${JournalEntry.MAX_FIELD_CHARS} characters"
            }
        }

        return if (reasons.isEmpty()) ValidationResult.Valid
        else ValidationResult.Invalid(reasons)
    }
}

/**
 * The result of validating a [JournalEntry]. [Valid] means the entry
 * satisfies every rule; [Invalid] carries the full list of failure reasons.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reasons: List<String>) : ValidationResult
}
