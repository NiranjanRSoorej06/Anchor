package com.anchor.domain.goals

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalsTest {

    // Fixed timestamps — no System.currentTimeMillis.
    private companion object {
        const val NOW = 1_000_000_000_000L
        const val DAY = 86_400_000L        // 24 h in millis
        const val WEEK = 7L * DAY          // 604_800_000
    }

    // ── GoalProgress.countThisWeek ──────────────────────────────────────

    @Test
    fun `completions within rolling window are counted`() {
        val goal = Goal(
            id = "b1",
            text = "Practice breathing",
            targetPerWeek = 3,
            completionsMillis = listOf(NOW - DAY, NOW - 2 * DAY, NOW - 3 * DAY),
        )
        assertEquals(3, GoalProgress.countThisWeek(goal, NOW))
    }

    @Test
    fun `completion 8 days ago is excluded`() {
        val goal = Goal(
            id = "b2",
            text = "Use grounding",
            targetPerWeek = 1,
            completionsMillis = listOf(NOW - 8 * DAY),
        )
        assertEquals(0, GoalProgress.countThisWeek(goal, NOW))
    }

    @Test
    fun `completion exactly 7 days ago is excluded`() {
        val goal = Goal(
            id = "b3",
            text = "Reach out to a friend",
            targetPerWeek = 1,
            completionsMillis = listOf(NOW - WEEK),
        )
        assertEquals(0, GoalProgress.countThisWeek(goal, NOW))
    }

    @Test
    fun `completion just inside window is counted`() {
        // Exactly 7 days minus 1 ms ago → just inside the window.
        val goal = Goal(
            id = "b4",
            text = "Practice breathing",
            targetPerWeek = 1,
            completionsMillis = listOf(NOW - WEEK + 1),
        )
        assertEquals(1, GoalProgress.countThisWeek(goal, NOW))
    }

    @Test
    fun `empty completions gives count 0`() {
        val goal = Goal(id = "b5", text = "Practice breathing", targetPerWeek = 1)
        assertEquals(0, GoalProgress.countThisWeek(goal, NOW))
    }

    // ── GoalProgress.isMet ─────────────────────────────────────────────

    @Test
    fun `isMet is true when target reached`() {
        val goal = Goal(
            id = "c1",
            text = "Practice breathing",
            targetPerWeek = 2,
            completionsMillis = listOf(NOW - DAY, NOW - 2 * DAY),
        )
        assertTrue(GoalProgress.isMet(goal, NOW))
    }

    @Test
    fun `isMet is true when target exceeded`() {
        val goal = Goal(
            id = "c2",
            text = "Practice breathing",
            targetPerWeek = 1,
            completionsMillis = listOf(NOW - DAY, NOW - 2 * DAY),
        )
        assertTrue(GoalProgress.isMet(goal, NOW))
    }

    @Test
    fun `isMet is false when target not reached`() {
        val goal = Goal(
            id = "c3",
            text = "Practice breathing",
            targetPerWeek = 3,
            completionsMillis = listOf(NOW - DAY, NOW - 2 * DAY),
        )
        assertFalse(GoalProgress.isMet(goal, NOW))
    }

    @Test
    fun `isMet is false with empty completions`() {
        val goal = Goal(id = "c4", text = "Practice breathing", targetPerWeek = 1)
        assertFalse(GoalProgress.isMet(goal, NOW))
    }

    // ── GoalValidator ──────────────────────────────────────────────────

    private fun validGoal() = Goal(
        id = "g1",
        text = "Practice breathing",
        targetPerWeek = 3,
    )

    @Test
    fun `valid goal returns Valid`() {
        assertTrue(GoalValidator.validate(validGoal()) is ValidationResult.Valid)
    }

    @Test
    fun `blank id is invalid`() {
        val goal = validGoal().copy(id = "   ")
        val result = GoalValidator.validate(goal) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("id") })
    }

    @Test
    fun `blank text is invalid`() {
        val goal = validGoal().copy(text = "")
        val result = GoalValidator.validate(goal) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("text") })
    }

    @Test
    fun `121-char text is invalid`() {
        val goal = validGoal().copy(text = "a".repeat(121))
        val result = GoalValidator.validate(goal) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("text") })
    }

    @Test
    fun `120-char text is valid`() {
        val goal = validGoal().copy(text = "a".repeat(120))
        assertTrue(GoalValidator.validate(goal) is ValidationResult.Valid)
    }

    @Test
    fun `targetPerWeek 0 is invalid`() {
        val goal = validGoal().copy(targetPerWeek = 0)
        val result = GoalValidator.validate(goal) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("targetPerWeek") })
    }

    @Test
    fun `targetPerWeek 15 is invalid`() {
        val goal = validGoal().copy(targetPerWeek = 15)
        val result = GoalValidator.validate(goal) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("targetPerWeek") })
    }

    @Test
    fun `targetPerWeek 1 is valid boundary`() {
        val goal = validGoal().copy(targetPerWeek = 1)
        assertTrue(GoalValidator.validate(goal) is ValidationResult.Valid)
    }

    @Test
    fun `targetPerWeek 14 is valid boundary`() {
        val goal = validGoal().copy(targetPerWeek = 14)
        assertTrue(GoalValidator.validate(goal) is ValidationResult.Valid)
    }

    @Test
    fun `negative timestamp is invalid`() {
        val goal = validGoal().copy(completionsMillis = listOf(-1L))
        val result = GoalValidator.validate(goal) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("negative") })
    }

    @Test
    fun `multiple failures are all reported`() {
        val goal = Goal(id = "", text = "", targetPerWeek = 0)
        val result = GoalValidator.validate(goal) as ValidationResult.Invalid
        assertTrue("Expected at least 3 reasons", result.reasons.size >= 3)
    }
}
