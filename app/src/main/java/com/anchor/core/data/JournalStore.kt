package com.anchor.core.data

/**
 * A single journal entry — free-form text captured by the user.
 *
 * @property id Unique identifier.
 * @property millis Epoch millis when the entry was created.
 * @property text User-written text (max 500 characters, must not be blank).
 */
data class JournalEntry(
    val id: String,
    val millis: Long,
    val text: String,
)

/**
 * Persistence contract for the private journal.
 *
 * Pure Kotlin: no Android, no Compose. Implementations can back this
 * with DataStore+JSON, Room, or in-memory maps.
 */
interface JournalStore {

    /** Return all journal entries, most recent first. */
    suspend fun list(): List<JournalEntry>

    /**
     * Create and persist a new journal entry.
     *
     * @param text Entry text. Must not be blank and must not exceed 500
     *   characters.
     * @return The newly created [JournalEntry].
     * @throws IllegalArgumentException if [text] is blank or exceeds 500
     *   characters.
     */
    suspend fun add(text: String): JournalEntry
}
