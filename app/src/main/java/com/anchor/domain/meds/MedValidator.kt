package com.anchor.domain.meds

/**
 * Pure-Kotlin validator for [MedReminder] instances. Collects every failure
 * without short-circuiting so the caller can present the full list at once.
 *
 * This is plain Kotlin: no Android, no Compose.
 *
 * **Reminder:** Anchor tracks medication reminders only. The app never
 * advises, adjusts, or interprets medication, and no dosage field exists
 * by design.
 */
object MedValidator {

    /**
     * Validate [reminder] and return [ValidationResult.Valid] if every rule
     * passes, or [ValidationResult.Invalid] with all reasons that failed.
     */
    fun validate(reminder: MedReminder): ValidationResult {
        val reasons = mutableListOf<String>()

        if (reminder.id.isBlank()) reasons += "Id must not be blank"
        if (reminder.name.isBlank()) reasons += "Name must not be blank"
        if (reminder.times.isEmpty()) reasons += "Times must not be empty"

        reminder.times.forEachIndexed { index, time ->
            if (time.hour !in 0..23) reasons += "Time ${index + 1}: hour must be 0..23"
            if (time.minute !in 0..59) reasons += "Time ${index + 1}: minute must be 0..59"
        }

        val duplicates = reminder.times
            .groupBy { it.hour to it.minute }
            .filter { it.value.size > 1 }
        if (duplicates.isNotEmpty()) reasons += "Duplicate times are not allowed"

        return if (reasons.isEmpty()) ValidationResult.Valid
        else ValidationResult.Invalid(reasons)
    }
}

/**
 * The result of validating a [MedReminder]. [Valid] means the reminder
 * satisfies every rule; [Invalid] carries the full list of failure reasons.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reasons: List<String>) : ValidationResult
}
