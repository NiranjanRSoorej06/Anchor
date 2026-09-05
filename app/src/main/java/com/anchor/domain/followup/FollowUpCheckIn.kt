package com.anchor.domain.followup

/**
 * An optional gentle follow-up check-in surfaced ~10–15 min after an SOS
 * episode, scheduled by the SDK machine via AlarmManager.
 *
 * The 3-tap flow (trigger chips → distress rating → short note) is fully
 * skippable at every stage; an entirely skipped check-in still records
 * that follow-up happened so the system does not re-prompt.
 *
 * This is plain Kotlin: no Android, no Compose.
 *
 * @property episodeId    Links to [Episode.id] as an opaque string; identifies
 *                        which SOS episode this follow-up belongs to.
 * @property triggerIds   Set of opaque situation-label strings (e.g. the
 *                        chip the user tapped). Empty means none were selected
 *                        or the user skipped this stage.
 * @property distress     Subjective Units of Distress Scale (SUDS-lite), 1–5.
 *                        `null` when the user skipped the distress stage.
 * @property note         Optional free-text reflection (≤[MAX_NOTE_CHARS] chars)
 *                        for the user or their therapist. Never routed on and
 *                        never diagnosed from.
 * @property completedAtMillis Unix epoch millis when the user completed (or
 *                             skipped) the flow. `0L` means not yet completed.
 */
data class FollowUpCheckIn(
    val episodeId: String,
    val triggerIds: Set<String> = emptySet(),
    val distress: Int? = null,
    val note: String? = null,
    val completedAtMillis: Long = 0L
) {
    companion object {
        /** Maximum allowed characters for [note]. */
        const val MAX_NOTE_CHARS = 280
    }
}
