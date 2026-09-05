package com.anchor.domain.safetyplan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SafetyPlanValidatorTest {

    private lateinit var plan: SafetyPlan

    @Before
    fun setUp() {
        plan = SafetyPlan(
            warningSigns = listOf("Can't sleep", "Replaying events"),
            copingStrategies = listOf("Deep breathing", "Go for a walk"),
            socialDistraction = listOf("Call sister", "Visit coffee shop"),
            helpContacts = listOf("Mom", "Best friend"),
            professionals = listOf("Dr. Smith", "988 Lifeline"),
            meansRestriction = listOf("Lock medication cabinet"),
        )
    }

    // ── Happy path ──────────────────────────────────────────────────────

    @Test
    fun `fully valid plan returns Valid`() {
        assertTrue(SafetyPlanValidator.validate(plan) is ValidationResult.Valid)
    }

    // ── Required sections empty ─────────────────────────────────────────

    @Test
    fun `empty warningSigns is invalid`() {
        plan = plan.copy(warningSigns = emptyList())
        val result = SafetyPlanValidator.validate(plan) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Warning signs") && it.contains("empty") })
    }

    @Test
    fun `empty copingStrategies is invalid`() {
        plan = plan.copy(copingStrategies = emptyList())
        val result = SafetyPlanValidator.validate(plan) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Coping strategies") && it.contains("empty") })
    }

    @Test
    fun `empty socialDistraction is invalid`() {
        plan = plan.copy(socialDistraction = emptyList())
        val result = SafetyPlanValidator.validate(plan) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Social distraction") && it.contains("empty") })
    }

    @Test
    fun `empty helpContacts is invalid`() {
        plan = plan.copy(helpContacts = emptyList())
        val result = SafetyPlanValidator.validate(plan) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Help contacts") && it.contains("empty") })
    }

    @Test
    fun `empty professionals is invalid`() {
        plan = plan.copy(professionals = emptyList())
        val result = SafetyPlanValidator.validate(plan) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Professionals") && it.contains("empty") })
    }

    // ── Optional section ────────────────────────────────────────────────

    @Test
    fun `empty meansRestriction is valid`() {
        plan = plan.copy(meansRestriction = emptyList())
        assertTrue(SafetyPlanValidator.validate(plan) is ValidationResult.Valid)
    }

    // ── Blank entries ───────────────────────────────────────────────────

    @Test
    fun `blank entry in warningSigns is invalid`() {
        plan = plan.copy(warningSigns = listOf("Can't sleep", "   "))
        val result = SafetyPlanValidator.validate(plan) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Warning signs") && it.contains("blank") })
    }

    // ── Too many entries ────────────────────────────────────────────────

    @Test
    fun `eleven entries in warningSigns is invalid`() {
        plan = plan.copy(warningSigns = (1..11).map { "Entry $it" })
        val result = SafetyPlanValidator.validate(plan) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Warning signs") && it.contains("exceed") })
    }

    // ── Duplicates ──────────────────────────────────────────────────────

    @Test
    fun `duplicate entries in copingStrategies are reported`() {
        plan = plan.copy(copingStrategies = listOf("Deep breathing", "Go for a walk", "Deep breathing"))
        val result = SafetyPlanValidator.validate(plan) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Coping strategies") && it.contains("duplicate") })
    }

    // ── Multiple failures aggregated ────────────────────────────────────

    @Test
    fun `multiple failures are all reported`() {
        plan = SafetyPlan(
            warningSigns = emptyList(),
            copingStrategies = emptyList(),
            socialDistraction = emptyList(),
            helpContacts = emptyList(),
            professionals = emptyList(),
            meansRestriction = emptyList(),
        )
        val result = SafetyPlanValidator.validate(plan) as ValidationResult.Invalid
        assertEquals("Expected 5 required-section failures", 5, result.reasons.size)
    }

    // ── Boundary: exactly 10 entries ────────────────────────────────────

    @Test
    fun `exactly ten entries is valid`() {
        plan = plan.copy(warningSigns = (1..10).map { "Entry $it" })
        assertTrue(SafetyPlanValidator.validate(plan) is ValidationResult.Valid)
    }
}
