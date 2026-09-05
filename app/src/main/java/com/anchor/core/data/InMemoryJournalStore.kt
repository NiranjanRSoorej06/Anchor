package com.anchor.core.data

/**
 * In-memory implementation of [JournalStore] for testing and previews.
 *
 * Not thread-safe — safe for single-coroutine test usage.
 */
class InMemoryJournalStore : JournalStore {

    companion object {
        /** Maximum allowed characters for a journal entry. */
        const val MAX_TEXT_LENGTH = 500
    }

    private val entries = mutableListOf<JournalEntry>()
    private var nextId = 1L

    override suspend fun list(): List<JournalEntry> =
        entries.sortedByDescending { it.millis }

    override suspend fun add(text: String): JournalEntry {
        require(text.isNotBlank()) { "Journal text must not be blank" }
        require(text.length <= MAX_TEXT_LENGTH) {
            "Journal text must not exceed $MAX_TEXT_LENGTH characters (got ${text.length})"
        }
        val entry = JournalEntry(
            id = "journal_${nextId++}",
            millis = System.currentTimeMillis(),
            text = text,
        )
        entries.add(entry)
        return entry
    }
}
