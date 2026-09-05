package com.anchor.domain.profile

/**
 * Pure-Kotlin validator for [UserProfile]. Collects **all** failures
 * rather than short-circuiting, so the UI can display every issue at once.
 *
 * No Android or coroutine dependencies — safe for unit testing on any
 * host.
 */
object UserProfileValidator {

    private val PHONE_REGEX = Regex("^\\+?[0-9 ]{7,}$")

    /**
     * Validates [profile] against the full set of domain rules.
     *
     * Rules:
     * 1. [UserProfile.hapticIntensity] must be in the closed range `0f..1f`.
     * 2. No blank (empty or whitespace-only) entries in
     *    [UserProfile.triggerSituations] or [UserProfile.notForMe].
     * 3. [UserProfile.trustedContactNumber], when non-null, must be
     *    non-blank and match [PHONE_REGEX] (`^\+?[0-9 ]{7,}$`).
     * 4. [UserProfile.emergencyNumber], when non-null, follows the same
     *    rules as [UserProfile.trustedContactNumber].
     *
     * @return [ValidationResult.Valid] when no rules are violated,
     *   [ValidationResult.Invalid] with a list of human-readable reasons
     *   otherwise.
     */
    fun validate(profile: UserProfile): ValidationResult {
        val reasons = mutableListOf<String>()

        // Rule 1 — hapticIntensity bounds
        if (profile.hapticIntensity !in 0f..1f) {
            reasons.add(
                "hapticIntensity must be between 0f and 1f inclusive, " +
                    "was ${profile.hapticIntensity}"
            )
        }

        // Rule 2 — blank entries in triggerSituations
        for (entry in profile.triggerSituations) {
            if (entry.isBlank()) {
                reasons.add("triggerSituations must not contain blank entries")
                break // one message is enough for the whole set
            }
        }

        // Rule 2 — blank entries in notForMe
        for (entry in profile.notForMe) {
            if (entry.isBlank()) {
                reasons.add("notForMe must not contain blank entries")
                break
            }
        }

        // Rule 3 — trustedContactNumber format
        profile.trustedContactNumber?.let { number ->
            if (number.isBlank()) {
                reasons.add("trustedContactNumber must not be blank when provided")
            } else if (!PHONE_REGEX.matches(number)) {
                reasons.add(
                    "trustedContactNumber format is invalid: '$number' " +
                        "must match ^\\+?[0-9 ]{7,}\$ (digits/spaces, " +
                        "optional leading +, minimum 7 characters)"
                )
            }
        }

        // Rule 4 — emergencyNumber format
        profile.emergencyNumber?.let { number ->
            if (number.isBlank()) {
                reasons.add("emergencyNumber must not be blank when provided")
            } else if (!PHONE_REGEX.matches(number)) {
                reasons.add(
                    "emergencyNumber format is invalid: '$number' " +
                        "must match ^\\+?[0-9 ]{7,}\$ (digits/spaces, " +
                        "optional leading +, minimum 7 characters)"
                )
            }
        }

        return if (reasons.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(reasons)
        }
    }
}

/**
 * The outcome of [UserProfileValidator.validate].
 *
 * - [Valid] — the profile satisfies every domain rule.
 * - [Invalid] — one or more rules were violated; [reasons] contains a
 *   human-readable description of each failure.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reasons: List<String>) : ValidationResult
}
