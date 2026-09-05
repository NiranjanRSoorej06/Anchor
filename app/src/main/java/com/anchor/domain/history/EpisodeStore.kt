package com.anchor.domain.history

/**
 * Persistence boundary for [Episode] records. Nothing outside this package
 * should talk to a concrete store directly — every other layer depends on
 * this interface instead, so it can run against [InMemoryEpisodeStore]
 * during tests and a DataStore-backed implementation on device without
 * changing a single call site.
 *
 * Deliberately not Android-aware: this is plain Kotlin so it stays
 * testable and reusable from services or receivers that have no
 * Android context.
 *
 * A DataStore-backed implementation is future work for the SDK machine.
 */
interface EpisodeStore {

    /**
     * Record an [episode], upserted by [Episode.id].
     *
     * If an episode with the same [Episode.id] already exists it is
     * replaced; the replacement moves to the end of insertion order
     * (see [InMemoryEpisodeStore] for the rationale).
     *
     * @throws IllegalArgumentException if [Episode.isValid] is false.
     */
    fun record(episode: Episode)

    /**
     * All recorded episodes in insertion order.
     */
    fun all(): List<Episode>

    /**
     * Discard all recorded episodes.
     */
    fun clear()
}
