package com.anchor.core.data

import com.anchor.domain.goals.Goal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InMemoryGoalStoreTest {

    private lateinit var store: InMemoryGoalStore

    private val sampleGoal = Goal(
        id = "goal_1",
        text = "Practice breathing",
        targetPerWeek = 3,
    )

    @Before
    fun setUp() {
        store = InMemoryGoalStore()
    }

    // ── list ────────────────────────────────────────────────────────

    @Test
    fun `list returns empty when no goals saved`() = runBlocking {
        assertEquals(emptyList<Goal>(), store.list())
    }

    @Test
    fun `list returns saved goals in insertion order`() = runBlocking {
        val g1 = sampleGoal
        val g2 = sampleGoal.copy(id = "goal_2", text = "Grounding exercise")
        store.save(g1)
        store.save(g2)
        val result = store.list()
        assertEquals(2, result.size)
        assertEquals("goal_1", result[0].id)
        assertEquals("goal_2", result[1].id)
    }

    // ── save ────────────────────────────────────────────────────────

    @Test
    fun `save adds a new goal`() = runBlocking {
        store.save(sampleGoal)
        assertEquals(1, store.list().size)
    }

    @Test
    fun `save replaces existing goal with same id`() = runBlocking {
        store.save(sampleGoal)
        val updated = sampleGoal.copy(text = "Updated text")
        store.save(updated)
        assertEquals(1, store.list().size)
        assertEquals("Updated text", store.list().first().text)
    }

    @Test
    fun `save rejects blank id`() = runBlocking {
        try {
            store.save(sampleGoal.copy(id = "  "))
            assertTrue("Expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("id") == true)
        }
    }

    @Test
    fun `save rejects blank text`() = runBlocking {
        try {
            store.save(sampleGoal.copy(text = ""))
            assertTrue("Expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("text") == true)
        }
    }

    @Test
    fun `save rejects text exceeding 120 chars`() = runBlocking {
        try {
            store.save(sampleGoal.copy(text = "a".repeat(121)))
            assertTrue("Expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("text") == true)
        }
    }

    @Test
    fun `save rejects targetPerWeek out of range`() = runBlocking {
        try {
            store.save(sampleGoal.copy(targetPerWeek = 0))
            assertTrue("Expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("targetPerWeek") == true)
        }
    }

    // ── complete ────────────────────────────────────────────────────

    @Test
    fun `complete adds a timestamp to the goal`() = runBlocking {
        store.save(sampleGoal)
        store.complete("goal_1", atMillis = 5000L)
        val goal = store.list().first()
        assertEquals(listOf(5000L), goal.completionsMillis)
    }

    @Test
    fun `complete accumulates multiple timestamps`() = runBlocking {
        store.save(sampleGoal)
        store.complete("goal_1", atMillis = 1000L)
        store.complete("goal_1", atMillis = 2000L)
        val goal = store.list().first()
        assertEquals(listOf(1000L, 2000L), goal.completionsMillis)
    }

    @Test
    fun `complete throws for unknown id`() = runBlocking {
        try {
            store.complete("nonexistent", atMillis = 1000L)
            assertTrue("Expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("nonexistent") == true)
        }
    }
}
