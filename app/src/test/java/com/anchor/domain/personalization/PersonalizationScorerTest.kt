package com.anchor.domain.personalization

import com.anchor.domain.session.CheckInResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PersonalizationScorerTest {

    private lateinit var outcomes: MutableList<SessionOutcome>

    @Before
    fun setUp() {
        outcomes = mutableListOf()
    }

    // ── successRate ──────────────────────────────────────────────────────

    @Test
    fun `empty list returns null rate`() {
        assertNull(PersonalizationScorer.successRate(emptyList(), "breathing"))
    }

    @Test
    fun `rate of 2 BETTER out of 4 is 0_5`() {
        outcomes += make("s1", "breathing", CheckInResponse.BETTER)
        outcomes += make("s2", "breathing", CheckInResponse.BETTER)
        outcomes += make("s3", "breathing", CheckInResponse.SAME)
        outcomes += make("s4", "breathing", CheckInResponse.WORSE)
        assertEquals(0.5, PersonalizationScorer.successRate(outcomes, "breathing")!!, 0.001)
    }

    @Test
    fun `WORSE and SAME count as not-BETTER`() {
        outcomes += make("s1", "breathing", CheckInResponse.SAME)
        outcomes += make("s2", "breathing", CheckInResponse.WORSE)
        assertEquals(0.0, PersonalizationScorer.successRate(outcomes, "breathing")!!, 0.001)
    }

    @Test
    fun `unknown routineId returns null rate`() {
        outcomes += make("s1", "breathing", CheckInResponse.BETTER)
        assertNull(PersonalizationScorer.successRate(outcomes, "unknown"))
    }

    @Test
    fun `null-routineId outcomes ignored by per-routine rate`() {
        outcomes += make("s1", null, CheckInResponse.BETTER)
        outcomes += make("s2", "breathing", CheckInResponse.BETTER)
        outcomes += make("s3", "breathing", CheckInResponse.SAME)
        // 1 BETTER / 2 total for "breathing" — null-routineId entry is excluded
        assertEquals(0.5, PersonalizationScorer.successRate(outcomes, "breathing")!!, 0.001)
    }

    // ── bestRoutine ──────────────────────────────────────────────────────

    @Test
    fun `bestRoutine picks highest rate`() {
        outcomes += make("s1", "breathing", CheckInResponse.BETTER)   // 1/1 = 1.0
        outcomes += make("s2", "grounding", CheckInResponse.SAME)    // 0/1 = 0.0
        assertEquals("breathing", PersonalizationScorer.bestRoutine(outcomes, listOf("breathing", "grounding")))
    }

    @Test
    fun `bestRoutine tie-break goes to most sessions`() {
        outcomes += make("s1", "breathing", CheckInResponse.BETTER)   // 1/1 = 1.0, count=1
        outcomes += make("s2", "grounding", CheckInResponse.BETTER)   // 1/1 = 1.0
        outcomes += make("s3", "grounding", CheckInResponse.BETTER)   // 2/2 = 1.0, count=2
        assertEquals("grounding", PersonalizationScorer.bestRoutine(outcomes, listOf("breathing", "grounding")))
    }

    @Test
    fun `bestRoutine returns null when outcomes is empty`() {
        assertNull(PersonalizationScorer.bestRoutine(emptyList(), listOf("breathing", "grounding")))
    }

    // ── insight ──────────────────────────────────────────────────────────

    @Test
    fun `insight returns fallback when fewer than MIN_SESSIONS`() {
        outcomes += make("s1", "breathing", CheckInResponse.BETTER)
        outcomes += make("s2", "breathing", CheckInResponse.SAME)
        assertEquals(
            "Not enough sessions yet to say what helps",
            PersonalizationScorer.insight(outcomes, "breathing", "Breathing")
        )
    }

    @Test
    fun `insight returns count string when threshold met`() {
        outcomes += make("s1", "breathing", CheckInResponse.BETTER)
        outcomes += make("s2", "breathing", CheckInResponse.BETTER)
        outcomes += make("s3", "breathing", CheckInResponse.SAME)
        outcomes += make("s4", "breathing", CheckInResponse.BETTER)
        outcomes += make("s5", "breathing", CheckInResponse.WORSE)
        assertEquals(
            "Breathing helped 3 out of 5 times",
            PersonalizationScorer.insight(outcomes, "breathing", "Breathing")
        )
    }

    @Test
    fun `insight returns fallback for unknown routineId`() {
        assertEquals(
            "Not enough sessions yet to say what helps",
            PersonalizationScorer.insight(emptyList(), "unknown", "Unknown")
        )
    }

    @Test
    fun `null-routineId outcomes do not crash per-routine functions`() {
        outcomes += make("s1", null, CheckInResponse.BETTER)
        outcomes += make("s2", null, CheckInResponse.WORSE)
        // successRate for a real routine with no matching outcomes → null
        assertNull(PersonalizationScorer.successRate(outcomes, "breathing"))
        // insight for a real routine with no matching outcomes → fallback
        assertEquals(
            "Not enough sessions yet to say what helps",
            PersonalizationScorer.insight(outcomes, "breathing", "Breathing")
        )
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private fun make(
        sessionId: String,
        routineId: String?,
        response: CheckInResponse
    ) = SessionOutcome(
        sessionId = sessionId,
        routineId = routineId,
        response = response,
        timestampMillis = 0L
    )
}
