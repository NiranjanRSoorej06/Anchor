package com.anchor.domain.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SafetyPhraseBank].
 *
 * Covers:
 * - default phrase list integrity (count, non-blank, word limit, uniqueness)
 * - [SafetyPhraseBank.isAcceptable] guardrails (blank, boundary, normal)
 */
class SafetyPhraseBankTest {

    // ── DEFAULTS list integrity ──────────────────────────────────────────

    @Test
    fun defaultsContainsExactly24Phrases() {
        assertEquals(24, SafetyPhraseBank.DEFAULTS.size)
    }

    @Test
    fun allDefaultsAreNonBlank() {
        SafetyPhraseBank.DEFAULTS.forEach { phrase ->
            assertTrue(
                "Default phrase should be non-blank: '$phrase'",
                phrase.isNotBlank()
            )
        }
    }

    @Test
    fun allDefaultsAreWithinWordLimit() {
        SafetyPhraseBank.DEFAULTS.forEach { phrase ->
            val wordCount = phrase.trim().split(Regex("\\s+")).size
            assertTrue(
                "Default phrase should have ≤ ${SafetyPhraseBank.MAX_WORDS} words " +
                    "but has $wordCount: '$phrase'",
                wordCount <= SafetyPhraseBank.MAX_WORDS
            )
        }
    }

    @Test
    fun allDefaultsAreUnique() {
        val unique = SafetyPhraseBank.DEFAULTS.toSet()
        assertEquals(
            "DEFAULTS should contain no duplicate phrases",
            SafetyPhraseBank.DEFAULTS.size,
            unique.size
        )
    }

    // ── isAcceptable: positive cases ─────────────────────────────────────

    @Test
    fun isAcceptableAcceptsNormalPhrase() {
        assertTrue(
            SafetyPhraseBank.isAcceptable("I am safe right now.")
        )
    }

    @Test
    fun isAcceptableAcceptsExactly25WordBoundaryPhrase() {
        val phrase = "One two three four five six seven eight nine ten " +
            "eleven twelve thirteen fourteen fifteen sixteen " +
            "seventeen eighteen nineteen twenty twentyone " +
            "twentytwo twentythree twentyfour twentyfive"
        assertEquals(25, phrase.trim().split(Regex("\\s+")).size)
        assertTrue(
            "Phrase with exactly 25 words should be accepted",
            SafetyPhraseBank.isAcceptable(phrase)
        )
    }

    // ── isAcceptable: negative cases ─────────────────────────────────────

    @Test
    fun isAcceptableRejectsBlankString() {
        assertFalse(SafetyPhraseBank.isAcceptable(""))
    }

    @Test
    fun isAcceptableRejectsWhitespaceOnlyString() {
        assertFalse(SafetyPhraseBank.isAcceptable("   \t\n  "))
    }

    @Test
    fun isAcceptableRejects26WordPhrase() {
        val phrase = "One two three four five six seven eight nine ten " +
            "eleven twelve thirteen fourteen fifteen sixteen " +
            "seventeen eighteen nineteen twenty twentyone " +
            "twentytwo twentythree twentyfour twentyfive twentysix"
        assertEquals(26, phrase.trim().split(Regex("\\s+")).size)
        assertFalse(
            "Phrase with 26 words should be rejected",
            SafetyPhraseBank.isAcceptable(phrase)
        )
    }

    @Test
    fun maxWordsConstantIs25() {
        assertEquals(25, SafetyPhraseBank.MAX_WORDS)
    }
}
