package com.anchor.core.data

import com.anchor.domain.meds.DoseLog
import com.anchor.domain.meds.MedReminder

/**
 * In-memory implementation of [MedStore] for testing and previews.
 *
 * Not thread-safe — safe for single-coroutine test usage.
 */
class InMemoryMedStore : MedStore {

    private val reminders = mutableListOf<MedReminder>()
    private val doseLogs = mutableListOf<DoseLog>()

    override suspend fun list(): List<MedReminder> = reminders.toList()

    override suspend fun save(r: MedReminder) {
        reminders.removeAll { it.id == r.id }
        reminders.add(r)
    }

    override suspend fun logDose(reminderId: String, taken: Boolean, atMillis: Long) {
        require(reminders.any { it.id == reminderId }) {
            "No reminder with id=$reminderId"
        }
        doseLogs.add(
            DoseLog(
                reminderId = reminderId,
                timestampMillis = atMillis,
                taken = taken,
            )
        )
    }

    override suspend fun doses(reminderId: String): List<DoseLog> =
        doseLogs.filter { it.reminderId == reminderId }
            .sortedBy { it.timestampMillis }
}
