package com.anchor.domain.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyPhraseRecordingTest {

    private fun valid() = SafetyPhraseRecording(
        id = "bundled_0",
        phraseText = SafetyPhraseBank.DEFAULTS.first(),
        filePath = "/data/user/0/com.anchor/files/phrases/bundled_0.m4a",
        createdAtMillis = 1_000L,
    )

    @Test
    fun `valid recording passes`() {
        assertEquals(SafetyPhraseRecordingValidator.ValidationResult.Valid, SafetyPhraseRecordingValidator.validate(valid()))
    }

    @Test
    fun `blank id is rejected`() {
        val result = SafetyPhraseRecordingValidator.validate(valid().copy(id = ""))
        assertTrue(result is SafetyPhraseRecordingValidator.ValidationResult.Invalid)
    }

    @Test
    fun `blank filePath is rejected`() {
        val result = SafetyPhraseRecordingValidator.validate(valid().copy(filePath = ""))
        assertTrue(result is SafetyPhraseRecordingValidator.ValidationResult.Invalid)
    }

    @Test
    fun `phrase over the word limit is rejected`() {
        val tooLong = (1..30).joinToString(" ") { "word" }
        val result = SafetyPhraseRecordingValidator.validate(valid().copy(phraseText = tooLong))
        assertTrue(result is SafetyPhraseRecordingValidator.ValidationResult.Invalid)
    }

    @Test
    fun `negative timestamp is rejected`() {
        val result = SafetyPhraseRecordingValidator.validate(valid().copy(createdAtMillis = -1L))
        assertTrue(result is SafetyPhraseRecordingValidator.ValidationResult.Invalid)
    }

    @Test
    fun `custom phrase text is accepted same as a bundled one`() {
        val result = SafetyPhraseRecordingValidator.validate(valid().copy(id = "custom_abc", phraseText = "I choose calm."))
        assertEquals(SafetyPhraseRecordingValidator.ValidationResult.Valid, result)
    }
}
