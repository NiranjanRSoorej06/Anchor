package com.anchor.domain.routine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoutineValidatorTest {

    private lateinit var routine: Routine

    @Before
    fun setUp() {
        routine = Routine(
            id = "test-routine",
            name = "Morning Calm",
            steps = listOf(
                RoutineStep.Haptic(patternId = "heartbeat", intensity = 0.5f, durationSec = 10),
                RoutineStep.Breathing(durationSec = 30),
                RoutineStep.Pause(durationSec = 5),
                RoutineStep.SafetyPhrase(clipId = "safe-clip"),
            ),
        )
    }

    // ── Happy path ──────────────────────────────────────────────────────

    @Test
    fun `valid routine returns Valid`() {
        assertTrue(RoutineValidator.validate(routine) is ValidationResult.Valid)
    }

    // ── Routine-level rules ─────────────────────────────────────────────

    @Test
    fun `blank name is invalid`() {
        routine = routine.copy(name = "   ")
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("blank") })
    }

    @Test
    fun `empty steps is invalid`() {
        routine = routine.copy(steps = emptyList())
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("empty") })
    }

    @Test
    fun `exactly MAX_STEPS steps is valid`() {
        val steps = (1..Routine.MAX_STEPS).map {
            RoutineStep.Pause(durationSec = 1)
        }
        routine = routine.copy(steps = steps)
        assertTrue(RoutineValidator.validate(routine) is ValidationResult.Valid)
    }

    @Test
    fun `more than MAX_STEPS steps is invalid`() {
        val steps = (1..Routine.MAX_STEPS + 1).map {
            RoutineStep.Pause(durationSec = 1)
        }
        routine = routine.copy(steps = steps)
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("exceed") })
    }

    @Test
    fun `total duration exactly MAX_TOTAL_SEC is valid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Breathing(durationSec = Routine.MAX_TOTAL_SEC)),
        )
        assertTrue(RoutineValidator.validate(routine) is ValidationResult.Valid)
    }

    @Test
    fun `total duration exceeding MAX_TOTAL_SEC is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Breathing(durationSec = Routine.MAX_TOTAL_SEC + 1)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("duration") })
    }

    // ── Haptic step rules ───────────────────────────────────────────────

    @Test
    fun `blank Haptic patternId is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Haptic(patternId = "", intensity = 0.5f, durationSec = 10)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("patternId") })
    }

    @Test
    fun `Haptic intensity below 0f is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Haptic(patternId = "p", intensity = -0.1f, durationSec = 10)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("intensity") })
    }

    @Test
    fun `Haptic intensity above 1f is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Haptic(patternId = "p", intensity = 1.1f, durationSec = 10)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("intensity") })
    }

    @Test
    fun `Haptic intensity exactly 0f is valid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Haptic(patternId = "p", intensity = 0f, durationSec = 10)),
        )
        assertTrue(RoutineValidator.validate(routine) is ValidationResult.Valid)
    }

    @Test
    fun `Haptic intensity exactly 1f is valid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Haptic(patternId = "p", intensity = 1f, durationSec = 10)),
        )
        assertTrue(RoutineValidator.validate(routine) is ValidationResult.Valid)
    }

    @Test
    fun `Haptic durationSec 0 is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Haptic(patternId = "p", intensity = 0.5f, durationSec = 0)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("durationSec") })
    }

    @Test
    fun `Haptic negative durationSec is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Haptic(patternId = "p", intensity = 0.5f, durationSec = -5)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("durationSec") })
    }

    // ── Breathing / Pause rules ─────────────────────────────────────────

    @Test
    fun `Breathing durationSec 0 is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Breathing(durationSec = 0)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Breathing") })
    }

    @Test
    fun `Breathing durationSec 121 is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Breathing(durationSec = 121)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Breathing") })
    }

    @Test
    fun `Breathing durationSec 120 is valid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Breathing(durationSec = 120)),
        )
        assertTrue(RoutineValidator.validate(routine) is ValidationResult.Valid)
    }

    @Test
    fun `Pause durationSec 0 is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Pause(durationSec = 0)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Pause") })
    }

    @Test
    fun `Pause durationSec 121 is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.Pause(durationSec = 121)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("Pause") })
    }

    // ── SafetyPhrase rules ──────────────────────────────────────────────

    @Test
    fun `blank SafetyPhrase clipId is invalid`() {
        routine = routine.copy(
            steps = listOf(RoutineStep.SafetyPhrase(clipId = "")),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("clipId") })
    }

    // ── Collects ALL failures ───────────────────────────────────────────

    @Test
    fun `multiple failures are all reported`() {
        routine = Routine(
            id = "",
            name = "",
            steps = listOf(RoutineStep.Haptic(patternId = "", intensity = 2f, durationSec = -1)),
        )
        val result = RoutineValidator.validate(routine) as ValidationResult.Invalid
        assertTrue("Expected at least 4 reasons", result.reasons.size >= 4)
    }
}
