package com.anchor.domain.meds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedValidatorTest {

    // ── helpers ──────────────────────────────────────────────────────────

    private fun validReminder() = MedReminder(
        id = "rem-1",
        name = "Morning meds",
        times = listOf(MedTime(8, 0), MedTime(20, 30)),
    )

    // ── fully valid ──────────────────────────────────────────────────────

    @Test
    fun `fully valid reminder returns Valid`() {
        val result = MedValidator.validate(validReminder())
        assertTrue(result is ValidationResult.Valid)
    }

    // ── blank id ─────────────────────────────────────────────────────────

    @Test
    fun `blank id returns Invalid`() {
        val result = MedValidator.validate(validReminder().copy(id = ""))
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("Id") })
    }

    // ── blank name ───────────────────────────────────────────────────────

    @Test
    fun `blank name returns Invalid`() {
        val result = MedValidator.validate(validReminder().copy(name = ""))
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("Name") })
    }

    // ── empty times ──────────────────────────────────────────────────────

    @Test
    fun `empty times list returns Invalid`() {
        val result = MedValidator.validate(validReminder().copy(times = emptyList()))
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("Times") })
    }

    // ── hour boundary: 24 is invalid ────────────────────────────────────

    @Test
    fun `hour 24 is invalid`() {
        val result = MedValidator.validate(validReminder().copy(times = listOf(MedTime(24, 0))))
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("hour") })
    }

    // ── minute boundary: 60 is invalid ───────────────────────────────────

    @Test
    fun `minute 60 is invalid`() {
        val result = MedValidator.validate(validReminder().copy(times = listOf(MedTime(0, 60))))
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("minute") })
    }

    // ── boundary 0:00 is valid ───────────────────────────────────────────

    @Test
    fun `0_00 is valid`() {
        val result = MedValidator.validate(validReminder().copy(times = listOf(MedTime(0, 0))))
        assertTrue(result is ValidationResult.Valid)
    }

    // ── boundary 23:59 is valid ──────────────────────────────────────────

    @Test
    fun `23_59 is valid`() {
        val result = MedValidator.validate(validReminder().copy(times = listOf(MedTime(23, 59))))
        assertTrue(result is ValidationResult.Valid)
    }

    // ── duplicate times ──────────────────────────────────────────────────

    @Test
    fun `duplicate times returns Invalid`() {
        val dupes = listOf(MedTime(8, 0), MedTime(8, 0))
        val result = MedValidator.validate(validReminder().copy(times = dupes))
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("Duplicate") })
    }

    // ── multiple failures aggregated ─────────────────────────────────────

    @Test
    fun `multiple failures are all reported`() {
        val result = MedValidator.validate(
            MedReminder(id = "", name = "", times = emptyList())
        )
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.size >= 3) // id, name, times
    }

    // ── DoseLog: taken record ────────────────────────────────────────────

    @Test
    fun `DoseLog taken records correctly`() {
        val log = DoseLog(
            reminderId = "rem-1",
            timestampMillis = 1_000_000L,
            taken = true,
        )
        assertEquals("rem-1", log.reminderId)
        assertEquals(1_000_000L, log.timestampMillis)
        assertTrue(log.taken)
    }

    // ── DoseLog: skipped record ──────────────────────────────────────────

    @Test
    fun `DoseLog skipped records correctly`() {
        val log = DoseLog(
            reminderId = "rem-2",
            timestampMillis = 2_000_000L,
            taken = false,
        )
        assertEquals("rem-2", log.reminderId)
        assertEquals(2_000_000L, log.timestampMillis)
        assertTrue(!log.taken)
    }
}
