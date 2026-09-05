package com.anchor.domain.personalization

import com.anchor.domain.session.CheckInResponse

/**
 * A plain record of a completed session and its user-reported outcome.
 *
 * This is NOT a clinical or diagnostic model — it mirrors the boundary
 * drawn by `domain/session/`: a session is a session, and the check-in
 * response is the user's own report. [PersonalizationScorer] uses these
 * records to surface statistical patterns; nothing here implies efficacy
 * or treatment.
 *
 * @property sessionId    Unique identifier for the session.
 * @property routineId    The routine used during the session, or `null` for
 *                        sessions recorded before routines existed.
 * @property response     The user's self-reported check-in response.
 * @property timestampMillis Unix epoch millis when the session concluded.
 */
data class SessionOutcome(
    val sessionId: String,
    val routineId: String?,
    val response: CheckInResponse,
    val timestampMillis: Long
)
