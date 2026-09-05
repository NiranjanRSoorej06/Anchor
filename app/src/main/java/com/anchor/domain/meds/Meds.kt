package com.anchor.domain.meds

/**
 * A time of day on the 24-hour clock.
 *
 * @property hour 0–23.
 * @property minute 0–59.
 */
data class MedTime(val hour: Int, val minute: Int)

/**
 * A user-created medication reminder.
 *
 * This model tracks **what** the user entered and whether they marked each
 * scheduled time as taken. It stores NO dosage, gives NO dosage guidance,
 * and suggests NOTHING. The app never advises, adjusts, or interprets
 * medication — this is strictly a tracking convenience.
 *
 * @property id Unique identifier (must be non-blank).
 * @property name Display name entered by the user (must be non-blank).
 * @property times Scheduled times for this reminder (must be non-empty,
 *   no duplicates).
 * @property enabled Whether the reminder is active.
 */
data class MedReminder(
    val id: String,
    val name: String,
    val times: List<MedTime>,
    val enabled: Boolean = true,
)

/**
 * A single dose log entry recording whether the user marked a scheduled
 * dose as taken or explicitly skipped/dismissed it.
 *
 * @property reminderId The [MedReminder.id] this entry relates to.
 * @property timestampMillis Epoch millis when the user responded.
 * @property taken `true` = taken, `false` = explicitly skipped / dismissed.
 */
data class DoseLog(
    val reminderId: String,
    val timestampMillis: Long,
    val taken: Boolean,
)
