package com.anchor.core.data

import com.anchor.domain.meds.DoseLog
import com.anchor.domain.meds.MedReminder

/**
 * Persistence contract for medication reminders and dose logs.
 *
 * Pure Kotlin: no Android, no Compose. Implementations can back this
 * with DataStore+JSON, Room, or in-memory maps.
 */
interface MedStore {

    /** Return all saved reminders in insertion order. */
    suspend fun list(): List<MedReminder>

    /** Insert or replace a reminder by its [MedReminder.id]. */
    suspend fun save(r: MedReminder)

    /**
     * Append a dose-log entry for [reminderId].
     *
     * @param reminderId Must reference an existing [MedReminder.id].
     * @param taken `true` if taken, `false` if skipped.
     * @param atMillis Epoch millis when the event occurred.
     */
    suspend fun logDose(reminderId: String, taken: Boolean, atMillis: Long)

    /** Return all dose-log entries for [reminderId], oldest first. */
    suspend fun doses(reminderId: String): List<DoseLog>
}
