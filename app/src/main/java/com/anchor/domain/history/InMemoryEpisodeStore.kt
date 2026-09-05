package com.anchor.domain.history

/**
 * An in-memory [EpisodeStore] backed by a [LinkedHashMap].
 *
 * Insertion order is preserved: new episodes append to the end. When an
 * existing episode is upserted (same [Episode.id]) the old entry is removed
 * first, so the updated episode moves to the end — this is the simplest
 * choice that avoids hidden internal reordering and keeps the iteration
 * order obvious to callers.
 *
 * This store is the HAL-pattern debug/test fake (mirroring
 * [com.anchor.core.haptics.DebugHapticEngine]) so a future
 * DataStore-backed implementation can slot in for the real SDK machine
 * without changing any call site.
 */
class InMemoryEpisodeStore : EpisodeStore {

    private val episodes = linkedMapOf<String, Episode>()

    override fun record(episode: Episode) {
        require(episode.isValid()) {
            "Invalid episode: id='${episode.id}', " +
                "startedAtMillis=${episode.startedAtMillis}, " +
                "endedAtMillis=${episode.endedAtMillis}"
        }
        // Remove-then-put moves the entry to the end of insertion order.
        episodes.remove(episode.id)
        episodes[episode.id] = episode
    }

    override fun all(): List<Episode> = episodes.values.toList()

    override fun clear() {
        episodes.clear()
    }
}
