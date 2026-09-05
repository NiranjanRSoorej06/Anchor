package com.anchor.core.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InMemoryJournalStoreTest {

    private lateinit var store: InMemoryJournalStore

    @Before
    fun setUp() {
        store = InMemoryJournalStore()
    }

    // ── list ────────────────────────────────────────────────────────

    @Test
    fun `list returns empty when no entries exist`() = runBlocking {
        assertEquals(emptyList<JournalEntry>(), store.list())
    }

    @Test
    fun `list returns entries sorted most recent first`() = runBlocking {
        store.add("First entry")
        store.add("Second entry")
        val result = store.list()
        assertEquals(2, result.size)
        assertEquals("Second entry", result[0].text)
        assertEquals("First entry", result[1].text)
    }

    // ── add ─────────────────────────────────────────────────────────

    @Test
    fun `add creates an entry with generated id`() = runBlocking {
        val entry = store.add("Hello world")
        assertTrue(entry.id.startsWith("journal_"))
        assertEquals("Hello world", entry.text)
        assertTrue(entry.millis > 0)
    }

    @Test
    fun `add assigns sequential ids`() = runBlocking {
        val e1 = store.add("First")
        val e2 = store.add("Second")
        assertTrue(e1.id < e2.id)
    }

    @Test
    fun `add rejects blank text`() = runBlocking {
        try {
            store.add("  ")
            assertTrue("Expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("blank") == true)
        }
    }

    @Test
    fun `add rejects empty text`() = runBlocking {
        try {
            store.add("")
            assertTrue("Expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("blank") == true)
        }
    }

    @Test
    fun `add rejects text exceeding 500 chars`() = runBlocking {
        try {
            store.add("a".repeat(501))
            assertTrue("Expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("500") == true)
        }
    }

    @Test
    fun `add accepts text at exactly 500 chars`() = runBlocking {
        val entry = store.add("a".repeat(500))
        assertEquals(500, entry.text.length)
    }
}
