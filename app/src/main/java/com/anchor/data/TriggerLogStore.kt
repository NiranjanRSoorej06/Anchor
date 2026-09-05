package com.anchor.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.anchor.domain.followup.FollowUpCheckIn
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device store for [FollowUpCheckIn] entries, backed by
 * [SharedPreferences] + JSON — same pattern as [EpisodeStorePersistent].
 *
 * Serves two callers with the same underlying model: the post-SOS
 * follow-up check-in (real [FollowUpCheckIn.episodeId]) and the standalone
 * "log a trigger any time" tool (synthetic `"manual-<uuid>"` episode id,
 * so every entry still has a unique key without changing the domain model).
 */
class TriggerLogStore(context: Context) {

    private companion object {
        const val PREF_NAME = "anchor_trigger_log_pref"
        const val KEY_ENTRIES_JSON = "trigger_log_entries_json"
        const val TAG = "TriggerLogStore"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** Newest first. */
    fun all(): List<FollowUpCheckIn> {
        val jsonStr = prefs.getString(KEY_ENTRIES_JSON, null) ?: return emptyList()
        val result = mutableListOf<FollowUpCheckIn>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                result.add(fromJson(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse trigger log entries: ${e.message}", e)
        }
        return result.sortedByDescending { it.completedAtMillis }
    }

    /** Upserts by [FollowUpCheckIn.episodeId]. */
    fun save(entry: FollowUpCheckIn) {
        val entries = linkedMapOf<String, FollowUpCheckIn>()
        all().forEach { entries[it.episodeId] = it }
        entries[entry.episodeId] = entry
        val array = JSONArray()
        entries.values.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_ENTRIES_JSON, array.toString()).apply()
    }

    private fun toJson(entry: FollowUpCheckIn): JSONObject = JSONObject().apply {
        put("episodeId", entry.episodeId)
        put("triggerIds", JSONArray(entry.triggerIds.toList()))
        put("distress", entry.distress ?: JSONObject.NULL)
        put("note", entry.note ?: JSONObject.NULL)
        put("completedAtMillis", entry.completedAtMillis)
    }

    private fun fromJson(json: JSONObject): FollowUpCheckIn {
        val triggerIds = mutableSetOf<String>()
        val array = json.optJSONArray("triggerIds")
        if (array != null) {
            for (i in 0 until array.length()) triggerIds.add(array.getString(i))
        }
        return FollowUpCheckIn(
            episodeId = json.getString("episodeId"),
            triggerIds = triggerIds,
            distress = if (json.isNull("distress")) null else json.getInt("distress"),
            note = if (json.isNull("note")) null else json.getString("note"),
            completedAtMillis = json.optLong("completedAtMillis", 0L),
        )
    }
}
