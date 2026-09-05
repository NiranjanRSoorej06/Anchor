package com.anchor.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.anchor.domain.journal.JournalEntry
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device store for [JournalEntry] entries, backed by [SharedPreferences]
 * + JSON — same pattern as [EpisodeStorePersistent]. Everything here stays
 * on-device only; nothing is uploaded or synced.
 */
class JournalStore(context: Context) {

    private companion object {
        const val PREF_NAME = "anchor_journal_pref"
        const val KEY_ENTRIES_JSON = "journal_entries_json"
        const val TAG = "JournalStore"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** Newest first. */
    fun all(): List<JournalEntry> {
        val jsonStr = prefs.getString(KEY_ENTRIES_JSON, null) ?: return emptyList()
        val result = mutableListOf<JournalEntry>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                result.add(fromJson(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse journal entries: ${e.message}", e)
        }
        return result.sortedByDescending { it.createdAtMillis }
    }

    fun add(entry: JournalEntry) {
        val entries = all() + entry
        val array = JSONArray()
        entries.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_ENTRIES_JSON, array.toString()).apply()
    }

    private fun toJson(entry: JournalEntry): JSONObject = JSONObject().apply {
        put("id", entry.id)
        put("createdAtMillis", entry.createdAtMillis)
        put("whatHelped", entry.whatHelped ?: JSONObject.NULL)
        put("reflection", entry.reflection ?: JSONObject.NULL)
        put("linkedEpisodeId", entry.linkedEpisodeId ?: JSONObject.NULL)
    }

    private fun fromJson(json: JSONObject): JournalEntry = JournalEntry(
        id = json.getString("id"),
        createdAtMillis = json.getLong("createdAtMillis"),
        whatHelped = if (json.isNull("whatHelped")) null else json.getString("whatHelped"),
        reflection = if (json.isNull("reflection")) null else json.getString("reflection"),
        linkedEpisodeId = if (json.isNull("linkedEpisodeId")) null else json.getString("linkedEpisodeId"),
    )
}
