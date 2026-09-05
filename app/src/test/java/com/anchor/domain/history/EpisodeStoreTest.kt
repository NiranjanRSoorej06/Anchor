package com.anchor.domain.history

import com.anchor.domain.session.CheckInResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EpisodeStoreTest {

    private lateinit var store: InMemoryEpisodeStore

    @Before
    fun setUp() {
        store = InMemoryEpisodeStore()
    }

    @Test
    fun `record then all returns single episode`() {
        val episode = Episode("e1", startedAtMillis = 1000L)
        store.record(episode)
        assertEquals(listOf(episode), store.all())
    }

    @Test
    fun `record multiple episodes preserves insertion order`() {
        val e1 = Episode("e1", startedAtMillis = 1000L)
        val e2 = Episode("e2", startedAtMillis = 2000L)
        val e3 = Episode("e3", startedAtMillis = 3000L)
        store.record(e1)
        store.record(e2)
        store.record(e3)
        assertEquals(listOf(e1, e2, e3), store.all())
    }

    @Test
    fun `record with same id replaces existing episode`() {
        val original = Episode("e1", startedAtMillis = 1000L)
        val updated = Episode("e1", startedAtMillis = 1000L, endedAtMillis = 2000L)
        store.record(original)
        store.record(updated)
        assertEquals(listOf(updated), store.all())
    }

    @Test
    fun `upsert moves replaced episode to end`() {
        val e1 = Episode("e1", startedAtMillis = 1000L)
        val e2 = Episode("e2", startedAtMillis = 2000L)
        val e1Updated = Episode("e1", startedAtMillis = 1000L, endedAtMillis = 1500L)
        store.record(e1)
        store.record(e2)
        store.record(e1Updated)
        assertEquals(listOf(e2, e1Updated), store.all())
    }

    @Test
    fun `clear empties the store`() {
        store.record(Episode("e1", startedAtMillis = 1000L))
        store.record(Episode("e2", startedAtMillis = 2000L))
        store.clear()
        assertTrue(store.all().isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `record with blank id throws`() {
        store.record(Episode("", startedAtMillis = 1000L))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `record with negative start throws`() {
        store.record(Episode("e1", startedAtMillis = -1L))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `record with end before start throws`() {
        store.record(Episode("e1", startedAtMillis = 2000L, endedAtMillis = 1000L))
    }

    @Test
    fun `episode with null end is valid`() {
        val episode = Episode("e1", startedAtMillis = 1000L, endedAtMillis = null)
        assertTrue(episode.isValid())
        store.record(episode)
        assertEquals(listOf(episode), store.all())
    }

    @Test
    fun `valid completed episode with response is accepted`() {
        val episode = Episode(
            "e1",
            startedAtMillis = 1000L,
            endedAtMillis = 2000L,
            routineId = "routine-a",
            finalResponse = CheckInResponse.BETTER
        )
        assertTrue(episode.isValid())
        store.record(episode)
        assertEquals(1, store.all().size)
    }

    @Test
    fun `all returns empty list for new store`() {
        assertTrue(store.all().isEmpty())
    }

    @Test
    fun `record with end equal to start is valid`() {
        val episode = Episode("e1", startedAtMillis = 1000L, endedAtMillis = 1000L)
        assertTrue(episode.isValid())
        store.record(episode)
    }
}
