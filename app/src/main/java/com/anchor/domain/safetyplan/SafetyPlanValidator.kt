package com.anchor.domain.safetyplan

/**
 * Pure-Kotlin validator for [SafetyPlan] instances. Collects every failure
 * without short-circuiting so the caller can present the full list at once.
 *
 * This is plain Kotlin: no Android, no Compose.
 */
object SafetyPlanValidator {

    /**
     * Validate [plan] and return [ValidationResult.Valid] if every rule
     * passes, or [ValidationResult.Invalid] with all reasons that failed.
     */
    fun validate(plan: SafetyPlan): ValidationResult {
        val reasons = mutableListOf<String>()

        validateRequiredSection(plan.warningSigns, "Warning signs", reasons)
        validateRequiredSection(plan.copingStrategies, "Coping strategies", reasons)
        validateRequiredSection(plan.socialDistraction, "Social distraction", reasons)
        validateRequiredSection(plan.helpContacts, "Help contacts", reasons)
        validateRequiredSection(plan.professionals, "Professionals", reasons)

        // meansRestriction is optional — an empty list is allowed — but if
        // present its entries must still be non-blank, within the size limit,
        // and free of duplicates.
        validateOptionalSection(plan.meansRestriction, "Means restriction", reasons)

        return if (reasons.isEmpty()) ValidationResult.Valid
        else ValidationResult.Invalid(reasons)
    }

    /** Validates a required (non-empty) section. */
    private fun validateRequiredSection(
        entries: List<String>,
        sectionName: String,
        reasons: MutableList<String>,
    ) {
        if (entries.isEmpty()) {
            reasons += "$sectionName must not be empty"
        }
        validateEntries(entries, sectionName, reasons)
    }

    /** Validates an optional (may be empty) section. */
    private fun validateOptionalSection(
        entries: List<String>,
        sectionName: String,
        reasons: MutableList<String>,
    ) {
        validateEntries(entries, sectionName, reasons)
    }

    /** Shared rules applied to any section's entries. */
    private fun validateEntries(
        entries: List<String>,
        sectionName: String,
        reasons: MutableList<String>,
    ) {
        if (entries.size > SafetyPlan.MAX_ENTRIES_PER_SECTION) {
            reasons += "$sectionName must not exceed ${SafetyPlan.MAX_ENTRIES_PER_SECTION} entries"
        }
        entries.forEachIndexed { index, entry ->
            if (entry.isBlank()) {
                reasons += "$sectionName must not contain blank entries"
            }
        }
        val duplicates = entries.filter { it.isNotBlank() }
            .groupBy { it }
            .filter { it.value.size > 1 }
            .keys
        if (duplicates.isNotEmpty()) {
            reasons += "$sectionName has duplicate entries"
        }
    }
}

/**
 * The result of validating a [SafetyPlan]. [Valid] means the plan satisfies
 * every rule; [Invalid] carries the full list of failure reasons.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reasons: List<String>) : ValidationResult
}
