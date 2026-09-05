package com.anchor.core.data

import com.anchor.domain.meds.MedReminder
import com.anchor.domain.meds.MedTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InMemoryMedStoreTest {

    private lateinit var store: InMemoryMedStore

    private val sampleReminder = MedReminder(
        id = "rem_1",
        name = "Sertraline",
        times = listOf(MedTime(hour = 8, minute = 0), MedTime(hour = 20, minute = 0)),
    )

    @Before
    fun setUp() {
        store = InMemoryMedStore()
    }

    // ── list ────────────────────────────────────────────────────────

    @Test
    fun `list returns empty when no reminders saved`() = runBlocking {
        assertEquals(emptyList<MedReminder>(), store.list())
    }

    @Test
    fun `list returns saved reminders in insertion order`() = runBlocking {
        val r1 = sampleReminder
        val r2 = sampleReminder.copy(id = "rem_2", name = "Ibuprofen")
        store.save(r1)
        store.save(r2)
        val result = store.list()
        assertEquals(2, result.size)
        assertEquals("rem_1", result[0].id)
        assertEquals("rem_2", result[1].id)
    }

    // ── save ────────────────────────────────────────────────────────

    @Test
    fun `save adds a new reminder`() = runBlocking {
        store.save(sampleReminder)
        assertEquals(1, store.list().size)
        assertEquals("Sertraline", store.list().first().name)
    }

    @Test
    fun `save replaces existing reminder with same id`() = runBlocking {
        store.save(sampleReminder)
        val updated = sampleReminder.copy(name = "Updated")
        store.save(updated)
        assertEquals(1, store.list().size)
        assertEquals("Updated", store.list().first().name)
    }

    // ── logDose / doses ─────────────────────────────────────────────

    @Test
    fun `logDose records a dose entry`() = runBlocking {
        store.save(sampleReminder)
        store.logDose("rem_1", taken = true, atMillis = 1000L)
        val doses = store.doses("rem_1")
        assertEquals(1, doses.size)
        assertTrue(doses.first().taken)
        assertEquals(1000L, doses.first().timestampMillis)
    }

    @Test
    fun `doses returns empty list for unknown reminder`() = runBlocking {
        assertEquals(emptyList<Any>(), store.doses("nonexistent"))
    }

    @Test
    fun `doses returns entries sorted by timestamp`() = runBlocking {
        store.save(sampleReminder)
        store.logDose("rem_1", taken = true, atMillis = 3000L)
        store.logDose("rem_1", taken = false, atMillis = 1000L)
        store.logDose("rem_1", taken = true, atMillis = 2000L)
        val doses = store.doses("rem_1")
        assertEquals(3, doses.size)
        assertEquals(1000L, doses[0].timestampMillis)
        assertEquals(2000L, doses[1].timestampMillis)
        assertEquals(3000L, doses[2].timestampMillis)
    }

    @Test
    fun `logDose throws for unknown reminder`() = runBlocking {
        try {
            store.logDose("nonexistent", taken = true, atMillis = 1000L)
            assertTrue("Expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("nonexistent") == true)
        }
    }

    @Test
    fun `doses only returns entries for the requested reminder`() = runBlocking {
        store.save(sampleReminder)
        store.save(sampleReminder.copy(id = "rem_2", name = "Other"))
        store.logDose("rem_1", taken = true, atMillis = 1000L)
        store.logDose("rem_2", taken = false, atMillis = 2000L)
        val doses = store.doses("rem_1")
        assertEquals(1, doses.size)
        assertEquals("rem_1", doses.first().reminderId)
    }
}
