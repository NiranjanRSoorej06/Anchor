package com.anchor.domain.history

import com.anchor.domain.session.CheckInResponse

/**
 * A plain local record of one session run.
 *
 * An [Episode] captures the lifecycle of a single grounding session — from
 * start through completion (or abandonment). This is NOT a clinical or
 * diagnostic record; it mirrors the boundary drawn by `domain/session/`:
 * a session is a session, and the check-in response is the user's own report.
 *
 * @property id             Unique identifier for this episode.
 * @property startedAtMillis Unix epoch millis when the session began.
 * @property endedAtMillis  Unix epoch millis when the session concluded, or
 *                          `null` if still open / abandoned.
 * @property routineId      The routine used during the session, or `null` for
 *                          sessions recorded before routines existed.
 * @property finalResponse  The user's self-reported check-in response, or
 *                          `null` if the session has not concluded yet.
 */
data class Episode(
    val id: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null,
    val routineId: String? = null,
    val finalResponse: CheckInResponse? = null
) {
    /**
     * Whether this episode passes basic sanity checks.
     *
     * - [id] must be non-blank.
     * - [startedAtMillis] must be >= 0.
     * - If [endedAtMillis] is non-null it must be >= [startedAtMillis].
     */
    fun isValid(): Boolean {
        if (id.isBlank()) return false
        if (startedAtMillis < 0) return false
        if (endedAtMillis != null && endedAtMillis < startedAtMillis) return false
        return true
    }
}
