package com.anchor.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.anchor.domain.sleep.SleepEntry
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device store for [SleepEntry] rows, backed by [SharedPreferences] +
 * JSON — same pattern as [GoalsStore] / [EpisodeStorePersistent]. Everything
 * stays on-device; nothing is uploaded or synced.
 */
class SleepLogStore(context: Context) {

    private companion object {
        const val PREF_NAME = "anchor_sleep_log_pref"
        const val KEY_ENTRIES_JSON = "sleep_entries_json"
        const val TAG = "SleepLogStore"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** Newest night first. */
    fun all(): List<SleepEntry> {
        val jsonStr = prefs.getString(KEY_ENTRIES_JSON, null) ?: return emptyList()
        val result = mutableListOf<SleepEntry>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                result.add(fromJson(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse sleep entries: ${e.message}", e)
        }
        return result.sortedByDescending { it.nightOfMillis }
    }

    /** Upserts [entry] by [SleepEntry.id]. */
    fun save(entry: SleepEntry) {
        val entries = linkedMapOf<String, SleepEntry>()
        all().forEach { entries[it.id] = it }
        entries[entry.id] = entry
        saveAll(entries.values.toList())
    }

    fun delete(id: String) {
        saveAll(all().filter { it.id != id })
    }

    private fun saveAll(entries: List<SleepEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_ENTRIES_JSON, array.toString()).apply()
    }

    private fun toJson(e: SleepEntry): JSONObject = JSONObject().apply {
        put("id", e.id)
        put("nightOfMillis", e.nightOfMillis)
        put("bedtimeMinOfDay", e.bedtimeMinOfDay)
        put("sleepLatencyMin", e.sleepLatencyMin)
        put("awakeningsCount", e.awakeningsCount)
        put("awakeningsMin", e.awakeningsMin)
        put("finalWakeMinOfDay", e.finalWakeMinOfDay)
        put("outOfBedMinOfDay", e.outOfBedMinOfDay)
        put("quality", e.quality)
        put("hadNightmare", e.hadNightmare)
        put("note", e.note ?: JSONObject.NULL)
    }

    private fun fromJson(json: JSONObject): SleepEntry = SleepEntry(
        id = json.getString("id"),
        nightOfMillis = json.getLong("nightOfMillis"),
        bedtimeMinOfDay = json.getInt("bedtimeMinOfDay"),
        sleepLatencyMin = json.getInt("sleepLatencyMin"),
        awakeningsCount = json.getInt("awakeningsCount"),
        awakeningsMin = json.getInt("awakeningsMin"),
        finalWakeMinOfDay = json.getInt("finalWakeMinOfDay"),
        outOfBedMinOfDay = json.getInt("outOfBedMinOfDay"),
        quality = json.getInt("quality"),
        hadNightmare = json.optBoolean("hadNightmare", false),
        note = if (json.isNull("note")) null else json.optString("note", null),
    )
}
