package com.anchor.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.anchor.domain.meds.DoseLog
import com.anchor.domain.meds.MedReminder
import com.anchor.domain.meds.MedTime
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device store for [MedReminder]s and [DoseLog]s, backed by
 * [SharedPreferences] + JSON — same pattern as [EpisodeStorePersistent].
 *
 * Never stores a dosage field — [MedReminder] has none by design, and this
 * store does not add one.
 */
class MedsStore(context: Context) {

    private companion object {
        const val PREF_NAME = "anchor_meds_pref"
        const val KEY_REMINDERS_JSON = "med_reminders_json"
        const val KEY_DOSE_LOGS_JSON = "dose_logs_json"
        const val TAG = "MedsStore"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ── Reminders ────────────────────────────────────────────────────

    fun allReminders(): List<MedReminder> {
        val jsonStr = prefs.getString(KEY_REMINDERS_JSON, null) ?: return emptyList()
        val result = mutableListOf<MedReminder>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                result.add(reminderFromJson(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse med reminders: ${e.message}", e)
        }
        return result
    }

    /** Upserts [reminder] by [MedReminder.id]. */
    fun saveReminder(reminder: MedReminder) {
        val reminders = linkedMapOf<String, MedReminder>()
        allReminders().forEach { reminders[it.id] = it }
        reminders[reminder.id] = reminder
        val array = JSONArray()
        reminders.values.forEach { array.put(reminderToJson(it)) }
        prefs.edit().putString(KEY_REMINDERS_JSON, array.toString()).apply()
    }

    fun deleteReminder(id: String) {
        val array = JSONArray()
        allReminders().filter { it.id != id }.forEach { array.put(reminderToJson(it)) }
        prefs.edit().putString(KEY_REMINDERS_JSON, array.toString()).apply()
    }

    private fun reminderToJson(reminder: MedReminder): JSONObject = JSONObject().apply {
        put("id", reminder.id)
        put("name", reminder.name)
        put("enabled", reminder.enabled)
        val times = JSONArray()
        reminder.times.forEach { t ->
            times.put(JSONObject().apply { put("hour", t.hour); put("minute", t.minute) })
        }
        put("times", times)
    }

    private fun reminderFromJson(json: JSONObject): MedReminder {
        val times = mutableListOf<MedTime>()
        val timesArray = json.optJSONArray("times")
        if (timesArray != null) {
            for (i in 0 until timesArray.length()) {
                val t = timesArray.getJSONObject(i)
                times.add(MedTime(t.getInt("hour"), t.getInt("minute")))
            }
        }
        return MedReminder(
            id = json.getString("id"),
            name = json.getString("name"),
            times = times,
            enabled = json.optBoolean("enabled", true),
        )
    }

    // ── Dose logs ────────────────────────────────────────────────────

    fun allDoseLogs(): List<DoseLog> {
        val jsonStr = prefs.getString(KEY_DOSE_LOGS_JSON, null) ?: return emptyList()
        val result = mutableListOf<DoseLog>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    DoseLog(
                        reminderId = obj.getString("reminderId"),
                        timestampMillis = obj.getLong("timestampMillis"),
                        taken = obj.getBoolean("taken"),
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse dose logs: ${e.message}", e)
        }
        return result
    }

    fun appendDoseLog(log: DoseLog) {
        val logs = allDoseLogs() + log
        val array = JSONArray()
        logs.forEach { l ->
            array.put(
                JSONObject().apply {
                    put("reminderId", l.reminderId)
                    put("timestampMillis", l.timestampMillis)
                    put("taken", l.taken)
                }
            )
        }
        prefs.edit().putString(KEY_DOSE_LOGS_JSON, array.toString()).apply()
    }
}
