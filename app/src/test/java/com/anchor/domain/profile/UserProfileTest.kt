package com.anchor.domain.profile

import com.anchor.domain.safety.SafetyProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [UserProfile] and [UserProfileValidator]. JUnit4,
 * pure-Kotlin, no Android dependencies.
 */
class UserProfileTest {

    // ── Helper ─────────────────────────────────────────────────────────

    private val defaults = UserProfile()

    // ── 1. Defaults are valid ──────────────────────────────────────────

    @Test
    fun `default UserProfile is valid`() {
        val result = UserProfileValidator.validate(defaults)
        assertTrue("default profile should be Valid", result is ValidationResult.Valid)
    }

    // ── 2–3. hapticIntensity boundary values ───────────────────────────

    @Test
    fun `hapticIntensity 0f is valid`() {
        val profile = defaults.copy(hapticIntensity = 0f)
        assertTrue(
            "0f is the lower bound and should be valid",
            UserProfileValidator.validate(profile) is ValidationResult.Valid
        )
    }

    @Test
    fun `hapticIntensity 1f is valid`() {
        val profile = defaults.copy(hapticIntensity = 1f)
        assertTrue(
            "1f is the upper bound and should be valid",
            UserProfileValidator.validate(profile) is ValidationResult.Valid
        )
    }

    @Test
    fun `hapticIntensity -0f_1f is invalid`() {
        val profile = defaults.copy(hapticIntensity = -0.1f)
        val result = UserProfileValidator.validate(profile)
        assertTrue("below 0f should be Invalid", result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(
            "reasons should mention hapticIntensity",
            invalid.reasons.any { it.contains("hapticIntensity") }
        )
    }

    @Test
    fun `hapticIntensity 1f_1f is invalid`() {
        val profile = defaults.copy(hapticIntensity = 1.1f)
        val result = UserProfileValidator.validate(profile)
        assertTrue("above 1f should be Invalid", result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(
            "reasons should mention hapticIntensity",
            invalid.reasons.any { it.contains("hapticIntensity") }
        )
    }

    // ── 4–5. Blank set entries ─────────────────────────────────────────

    @Test
    fun `blank entry in triggerSituations is invalid`() {
        val profile = defaults.copy(triggerSituations = setOf("crowds", "  "))
        val result = UserProfileValidator.validate(profile)
        assertTrue("blank entry should fail", result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(
            "reasons should mention triggerSituations",
            invalid.reasons.any { it.contains("triggerSituations") }
        )
    }

    @Test
    fun `blank entry in notForMe is invalid`() {
        val profile = defaults.copy(notForMe = setOf("E001", ""))
        val result = UserProfileValidator.validate(profile)
        assertTrue("blank entry should fail", result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(
            "reasons should mention notForMe",
            invalid.reasons.any { it.contains("notForMe") }
        )
    }

    // ── 6–8. Phone number formats ──────────────────────────────────────

    @Test
    fun `phone with letters is invalid`() {
        val profile = defaults.copy(trustedContactNumber = "abc12345")
        val result = UserProfileValidator.validate(profile)
        assertTrue("letters should fail", result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(
            "reasons should mention trustedContactNumber",
            invalid.reasons.any { it.contains("trustedContactNumber") }
        )
    }

    @Test
    fun `phone too short is invalid`() {
        val profile = defaults.copy(emergencyNumber = "+1 234")
        val result = UserProfileValidator.validate(profile)
        assertTrue("too short should fail", result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(
            "reasons should mention emergencyNumber",
            invalid.reasons.any { it.contains("emergencyNumber") }
        )
    }

    @Test
    fun `blank phone string is invalid`() {
        val profile = defaults.copy(trustedContactNumber = "   ")
        val result = UserProfileValidator.validate(profile)
        assertTrue("blank string should fail", result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(
            "reasons should mention trustedContactNumber must not be blank",
            invalid.reasons.any { it.contains("must not be blank") }
        )
    }

    // ── 9. Null phones are valid ───────────────────────────────────────

    @Test
    fun `null phone numbers are valid`() {
        val profile = defaults.copy(
            trustedContactNumber = null,
            emergencyNumber = null
        )
        assertTrue(
            "null phones should be Valid",
            UserProfileValidator.validate(profile) is ValidationResult.Valid
        )
    }

    // ── 10. Valid phone formats ─────────────────────────────────────────

    @Test
    fun `valid phone formats are accepted`() {
        // emergencyNumber follows the same rules as trustedContactNumber
        // (see UserProfile's field doc and the "phone too short" test below) —
        // a bare "911" is 3 chars and would fail the shared 7-char minimum,
        // so use a realistic emergency-line number that satisfies PHONE_REGEX.
        val profile = defaults.copy(
            trustedContactNumber = "+1 555 123 4567",
            emergencyNumber = "9110000"
        )
        assertTrue(
            "standard formats should be valid",
            UserProfileValidator.validate(profile) is ValidationResult.Valid
        )
    }

    // ── 11. onboardingComplete flag ────────────────────────────────────

    @Test
    fun `onboardingComplete can be toggled`() {
        val incomplete = defaults.copy(onboardingComplete = false)
        val complete = defaults.copy(onboardingComplete = true)
        assertFalse("defaults to false", incomplete.onboardingComplete)
        assertTrue("can be set to true", complete.onboardingComplete)
        // Both should still be valid
        assertTrue(
            UserProfileValidator.validate(incomplete) is ValidationResult.Valid
        )
        assertTrue(
            UserProfileValidator.validate(complete) is ValidationResult.Valid
        )
    }

    // ── 12. toSafetyProfile mapping ────────────────────────────────────

    @Test
    fun `toSafetyProfile maps fields correctly`() {
        val profile = UserProfile(
            audioOk = false,
            voiceOk = true,
            hapticIntensity = 0.5f,
            touchSensitive = true,
            notForMe = setOf("E004")
        )
        val safety = profile.toSafetyProfile()
        assertEquals(false, safety.audioOk)
        assertEquals(true, safety.voiceOk)
        assertEquals(true, safety.hapticsOk)    // 0.5f > 0f
        assertEquals(true, safety.touchSensitive)
        assertEquals(setOf("E004"), safety.notForMe)
    }

    // ── 13. toSafetyProfile: hapticIntensity 0f → hapticsOk false ──────

    @Test
    fun `toSafetyProfile with hapticIntensity 0f produces hapticsOk false`() {
        val profile = defaults.copy(hapticIntensity = 0f)
        val safety = profile.toSafetyProfile()
        assertFalse(
            "hapticIntensity 0f should map to hapticsOk = false",
            safety.hapticsOk
        )
    }

    // ── 14. toSafetyProfile: default profile maps to default SafetyProfile

    @Test
    fun `toSafetyProfile from default UserProfile equals default SafetyProfile`() {
        assertEquals(SafetyProfile(), defaults.toSafetyProfile())
    }

    // ── 15. Multiple failures are aggregated ───────────────────────────

    @Test
    fun `multiple validation failures are all reported`() {
        val profile = UserProfile(
            hapticIntensity = -1f,
            triggerSituations = setOf("  "),
            notForMe = setOf(""),
            trustedContactNumber = "not-a-number!!!",
            emergencyNumber = "12"
        )
        val result = UserProfileValidator.validate(profile)
        assertTrue("should be Invalid", result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(
            "should have at least 4 distinct reasons",
            invalid.reasons.size >= 4
        )
        assertTrue(invalid.reasons.any { it.contains("hapticIntensity") })
        assertTrue(invalid.reasons.any { it.contains("triggerSituations") })
        assertTrue(invalid.reasons.any { it.contains("notForMe") })
        assertTrue(invalid.reasons.any { it.contains("trustedContactNumber") })
        assertTrue(invalid.reasons.any { it.contains("emergencyNumber") })
    }
}
