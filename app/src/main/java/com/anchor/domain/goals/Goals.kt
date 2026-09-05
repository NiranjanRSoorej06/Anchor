package com.anchor.domain.goals

/**
 * A **behavioral** goal — something the user practices in daily life, not a
 * clinical treatment target. Examples:
 *   - "Practice breathing 3× this week"
 *   - "Use grounding technique when I notice tension"
 *   - "Reach out to a friend at least once this week"
 *
 * This is plain Kotlin: no Android, no Compose.
 *
 * @property id Unique identifier for this goal.
 * @property text Human-readable description (1–120 chars).
 * @property targetPerWeek How many times the user aims to complete this
 *   goal in a rolling 7-day window (1–14).
 * @property completionsMillis Timestamps (epoch millis) when the goal was
 *   completed. Negative values are rejected by [GoalValidator].
 */
data class Goal(
    val id: String,
    val text: String,
    val targetPerWeek: Int,
    val completionsMillis: List<Long> = emptyList(),
)

/**
 * Pure-Kotlin progress tracker for [Goal]s.
 *
 * The "week" is a **rolling 7×24 h window** ending at the supplied
 * [nowMillis] so the logic is deterministic and testable.
 */
object GoalProgress {

    private const val MILLIS_PER_WEEK: Long = 7L * 24 * 60 * 60 * 1000

    /**
     * Count how many completions fall inside the rolling window
     * `(nowMillis - MILLIS_PER_WEEK, nowMillis]`.
     */
    fun countThisWeek(goal: Goal, nowMillis: Long): Int {
        val lowerBound = nowMillis - MILLIS_PER_WEEK
        return goal.completionsMillis.count { it in (lowerBound + 1)..nowMillis }
    }

    /**
     * `true` when [countThisWeek] ≥ [Goal.targetPerWeek].
     */
    fun isMet(goal: Goal, nowMillis: Long): Boolean =
        countThisWeek(goal, nowMillis) >= goal.targetPerWeek
}

/**
 * Pure-Kotlin validator for [Goal] instances. Collects every failure
 * without short-circuiting so the caller can present the full list at once.
 *
 * This is plain Kotlin: no Android, no Compose.
 */
object GoalValidator {

    /** Maximum allowed length for [Goal.text]. */
    const val MAX_TEXT_LENGTH = 120

    /** Maximum allowed [Goal.targetPerWeek]. */
    const val MAX_TARGET = 14

    /** Minimum allowed [Goal.targetPerWeek]. */
    const val MIN_TARGET = 1

    /**
     * Validate [goal] and return [ValidationResult.Valid] if every rule
     * passes, or [ValidationResult.Invalid] with all reasons that failed.
     */
    fun validate(goal: Goal): ValidationResult {
        val reasons = mutableListOf<String>()

        if (goal.id.isBlank()) reasons += "id must not be blank"
        if (goal.text.isBlank()) reasons += "text must not be blank"
        if (goal.text.length > MAX_TEXT_LENGTH) {
            reasons += "text must not exceed $MAX_TEXT_LENGTH characters"
        }
        if (goal.targetPerWeek < MIN_TARGET || goal.targetPerWeek > MAX_TARGET) {
            reasons += "targetPerWeek must be $MIN_TARGET..$MAX_TARGET"
        }
        goal.completionsMillis.forEachIndexed { index, ts ->
            if (ts < 0) reasons += "completion ${index + 1}: timestamp must not be negative"
        }

        return if (reasons.isEmpty()) ValidationResult.Valid
        else ValidationResult.Invalid(reasons)
    }
}

/**
 * The result of validating a [Goal]. [Valid] means the goal satisfies
 * every rule; [Invalid] carries the full list of failure reasons.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reasons: List<String>) : ValidationResult
}
