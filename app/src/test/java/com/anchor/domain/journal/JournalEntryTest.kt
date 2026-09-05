package com.anchor.domain.journal

import org.junit.Assert.assertTrue
import org.junit.Test

class JournalEntryTest {

    private fun validEntry() = JournalEntry(
        id = "j1",
        createdAtMillis = 1_000L,
        whatHelped = "Breathing slowly",
    )

    @Test
    fun `valid entry returns Valid`() {
        assertTrue(JournalValidator.validate(validEntry()) is ValidationResult.Valid)
    }

    @Test
    fun `entry with only reflection is valid`() {
        val entry = validEntry().copy(whatHelped = null, reflection = "Felt calmer after")
        assertTrue(JournalValidator.validate(entry) is ValidationResult.Valid)
    }

    @Test
    fun `blank id is invalid`() {
        val entry = validEntry().copy(id = "   ")
        val result = JournalValidator.validate(entry) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("id") })
    }

    @Test
    fun `negative createdAtMillis is invalid`() {
        val entry = validEntry().copy(createdAtMillis = -1L)
        val result = JournalValidator.validate(entry) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("createdAtMillis") })
    }

    @Test
    fun `both fields blank is invalid`() {
        val entry = validEntry().copy(whatHelped = null, reflection = null)
        val result = JournalValidator.validate(entry) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("whatHelped or reflection") })
    }

    @Test
    fun `both fields blank strings are invalid`() {
        val entry = validEntry().copy(whatHelped = "  ", reflection = "")
        val result = JournalValidator.validate(entry) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("whatHelped or reflection") })
    }

    @Test
    fun `281-char whatHelped is invalid`() {
        val entry = validEntry().copy(whatHelped = "a".repeat(281))
        val result = JournalValidator.validate(entry) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("whatHelped") })
    }

    @Test
    fun `280-char whatHelped is valid boundary`() {
        val entry = validEntry().copy(whatHelped = "a".repeat(280))
        assertTrue(JournalValidator.validate(entry) is ValidationResult.Valid)
    }

    @Test
    fun `281-char reflection is invalid`() {
        val entry = validEntry().copy(reflection = "a".repeat(281))
        val result = JournalValidator.validate(entry) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("reflection") })
    }

    @Test
    fun `linkedEpisodeId does not affect validity`() {
        val entry = validEntry().copy(linkedEpisodeId = "ep1")
        assertTrue(JournalValidator.validate(entry) is ValidationResult.Valid)
    }

    @Test
    fun `multiple failures are all reported`() {
        val entry = JournalEntry(id = "", createdAtMillis = -1L, whatHelped = null, reflection = null)
        val result = JournalValidator.validate(entry) as ValidationResult.Invalid
        assertTrue("Expected at least 3 reasons", result.reasons.size >= 3)
    }
}
