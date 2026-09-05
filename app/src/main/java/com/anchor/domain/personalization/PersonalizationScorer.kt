package com.anchor.domain.personalization

import com.anchor.domain.session.CheckInResponse

/**
 * Deterministic, pure-Kotlin scorer for session outcomes.
 *
 * All functions are stateless and free of I/O. They operate solely on
 * the provided [SessionOutcome] list and return aggregate statistics.
 * No randomness, no side effects — safe to call from any thread and
 * trivially testable.
 */
object PersonalizationScorer {

    /** Minimum number of sessions for a routine before an insight is emitted. */
    const val MIN_SESSIONS = 3

    /**
     * Fraction of sessions for [routineId] where the user reported BETTER.
     *
     * @return A value in `0.0..1.0`, or `null` when there are zero outcomes
     *         matching [routineId].
     */
    fun successRate(outcomes: List<SessionOutcome>, routineId: String): Double? {
        val filtered = outcomes.filter { it.routineId == routineId }
        if (filtered.isEmpty()) return null
        val better = filtered.count { it.response == CheckInResponse.BETTER }
        return better.toDouble() / filtered.size.toDouble()
    }

    /**
     * The routine with the highest [successRate] among [candidateIds].
     *
     * Tie-break: the routine with the most recorded sessions wins.
     * Returns `null` when [outcomes] is empty or no candidate has any
     * recorded session.
     */
    fun bestRoutine(outcomes: List<SessionOutcome>, candidateIds: List<String>): String? {
        if (outcomes.isEmpty()) return null

        data class Candidate(val id: String, val rate: Double, val count: Int)

        val candidates = candidateIds.mapNotNull { id ->
            val filtered = outcomes.filter { it.routineId == id }
            if (filtered.isEmpty()) return@mapNotNull null
            val better = filtered.count { it.response == CheckInResponse.BETTER }
            val rate = better.toDouble() / filtered.size.toDouble()
            Candidate(id, rate, filtered.size)
        }

        if (candidates.isEmpty()) return null

        return candidates
            .sortedWith(compareByDescending<Candidate> { it.rate }.thenByDescending { it.count })
            .first()
            .id
    }

    /**
     * A human-readable string summarizing how often [routineId] helped.
     *
     * When the total number of sessions for [routineId] is at least
     * [MIN_SESSIONS], returns e.g. `"Breathing helped 4 out of 5 times"`
     * using [displayName]. Otherwise returns a fallback indicating
     * insufficient data.
     */
    fun insight(outcomes: List<SessionOutcome>, routineId: String, displayName: String): String {
        val filtered = outcomes.filter { it.routineId == routineId }
        val count = filtered.size
        if (count < MIN_SESSIONS) {
            return "Not enough sessions yet to say what helps"
        }
        val better = filtered.count { it.response == CheckInResponse.BETTER }
        return "$displayName helped $better out of $count times"
    }
}
