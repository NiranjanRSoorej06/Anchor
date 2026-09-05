package com.anchor.domain.followup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FollowUpValidatorTest {

    // -- valid cases --

    @Test
    fun `fully-skipped check-in is valid`() {
        val checkIn = FollowUpCheckIn(episodeId = "ep-1")
        val result = FollowUpValidator.validate(checkIn)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `fully-skipped check-in with zero defaults is valid`() {
        // Default constructor values: empty triggers, null distress, null note
        val checkIn = FollowUpCheckIn(episodeId = "abc-123")
        assertTrue(FollowUpValidator.validate(checkIn) is ValidationResult.Valid)
    }

    @Test
    fun `complete valid check-in passes`() {
        val checkIn = FollowUpCheckIn(
            episodeId = "ep-42",
            triggerIds = setOf("crowded-place", "loud-noise"),
            distress = 3,
            note = "Feeling a bit better now.",
            completedAtMillis = 1700000000000L
        )
        assertTrue(FollowUpValidator.validate(checkIn) is ValidationResult.Valid)
    }

    @Test
    fun `distress lower boundary 1 is valid`() {
        val checkIn = FollowUpCheckIn(episodeId = "ep-1", distress = 1)
        assertTrue(FollowUpValidator.validate(checkIn) is ValidationResult.Valid)
    }

    @Test
    fun `distress upper boundary 5 is valid`() {
        val checkIn = FollowUpCheckIn(episodeId = "ep-1", distress = 5)
        assertTrue(FollowUpValidator.validate(checkIn) is ValidationResult.Valid)
    }

    @Test
    fun `note exactly MAX_NOTE_CHARS is valid`() {
        val note = "a".repeat(FollowUpCheckIn.MAX_NOTE_CHARS)
        val checkIn = FollowUpCheckIn(episodeId = "ep-1", note = note)
        assertTrue(FollowUpValidator.validate(checkIn) is ValidationResult.Valid)
    }

    // -- invalid cases --

    @Test
    fun `blank episodeId is invalid`() {
        val checkIn = FollowUpCheckIn(episodeId = "")
        val result = FollowUpValidator.validate(checkIn)
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("episodeId") })
    }

    @Test
    fun `distress 0 is invalid`() {
        val checkIn = FollowUpCheckIn(episodeId = "ep-1", distress = 0)
        val result = FollowUpValidator.validate(checkIn)
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("distress") })
    }

    @Test
    fun `distress 6 is invalid`() {
        val checkIn = FollowUpCheckIn(episodeId = "ep-1", distress = 6)
        val result = FollowUpValidator.validate(checkIn)
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("distress") })
    }

    @Test
    fun `note exceeding MAX_NOTE_CHARS is invalid`() {
        val note = "a".repeat(FollowUpCheckIn.MAX_NOTE_CHARS + 1)
        val checkIn = FollowUpCheckIn(episodeId = "ep-1", note = note)
        val result = FollowUpValidator.validate(checkIn)
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("note") })
    }

    @Test
    fun `blank trigger entry is invalid`() {
        val checkIn = FollowUpCheckIn(
            episodeId = "ep-1",
            triggerIds = setOf("valid-trigger", "", "another")
        )
        val result = FollowUpValidator.validate(checkIn)
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.reasons.any { it.contains("triggerIds") })
    }

    // -- aggregation --

    @Test
    fun `multiple failures are aggregated`() {
        val checkIn = FollowUpCheckIn(
            episodeId = "",
            distress = 0,
            note = "x".repeat(FollowUpCheckIn.MAX_NOTE_CHARS + 1),
            triggerIds = setOf("")
        )
        val result = FollowUpValidator.validate(checkIn)
        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        // At least 4 distinct reasons: episodeId, distress, note, triggerIds
        assertTrue("Expected at least 4 reasons, got ${invalid.reasons.size}", invalid.reasons.size >= 4)
    }

    // -- data preservation --

    @Test
    fun `triggerIds are preserved in the data class`() {
        val triggers = setOf("work-stress", "insomnia", "social-conflict")
        val checkIn = FollowUpCheckIn(episodeId = "ep-1", triggerIds = triggers)
        assertEquals(triggers, checkIn.triggerIds)
    }

    @Test
    fun `null distress is preserved and valid`() {
        val checkIn = FollowUpCheckIn(episodeId = "ep-1", distress = null)
        assertEquals(null, checkIn.distress)
        assertTrue(FollowUpValidator.validate(checkIn) is ValidationResult.Valid)
    }

    @Test
    fun `note is preserved in the data class`() {
        val text = "Today was tough but I managed."
        val checkIn = FollowUpCheckIn(episodeId = "ep-1", note = text)
        assertEquals(text, checkIn.note)
    }
}
