package com.anchor.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.anchor.domain.history.Episode
import com.anchor.domain.history.EpisodeStore
import com.anchor.domain.session.CheckInResponse
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device [EpisodeStore] backed by [SharedPreferences] + JSON, mirroring
 * the pattern already used by `core/companion/CompanionPreferences.kt` and
 * `core/logging/SprintLogStore.kt`. This is the production implementation
 * wired at the `MainActivity` call site; [com.anchor.domain.history.InMemoryEpisodeStore]
 * remains the fake used by tests and dev screens.
 */
class EpisodeStorePersistent(context: Context) : EpisodeStore {

    private companion object {
        const val PREF_NAME = "anchor_episodes_pref"
        const val KEY_EPISODES_JSON = "episodes_json"
        const val TAG = "EpisodeStorePersistent"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override fun record(episode: Episode) {
        require(episode.isValid()) {
            "Invalid episode: id='${episode.id}', " +
                "startedAtMillis=${episode.startedAtMillis}, " +
                "endedAtMillis=${episode.endedAtMillis}"
        }
        val episodes = linkedMapOf<String, Episode>()
        all().forEach { episodes[it.id] = it }
        episodes.remove(episode.id)
        episodes[episode.id] = episode
        save(episodes.values.toList())
    }

    override fun all(): List<Episode> {
        val jsonStr = prefs.getString(KEY_EPISODES_JSON, null) ?: return emptyList()
        val result = mutableListOf<Episode>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                result.add(fromJson(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse episodes: ${e.message}", e)
        }
        return result
    }

    override fun clear() {
        prefs.edit().remove(KEY_EPISODES_JSON).apply()
    }

    private fun save(episodes: List<Episode>) {
        val array = JSONArray()
        episodes.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_EPISODES_JSON, array.toString()).apply()
    }

    private fun toJson(episode: Episode): JSONObject = JSONObject().apply {
        put("id", episode.id)
        put("startedAtMillis", episode.startedAtMillis)
        put("endedAtMillis", episode.endedAtMillis ?: JSONObject.NULL)
        put("routineId", episode.routineId ?: JSONObject.NULL)
        put("finalResponse", episode.finalResponse?.name ?: JSONObject.NULL)
    }

    private fun fromJson(json: JSONObject): Episode = Episode(
        id = json.getString("id"),
        startedAtMillis = json.getLong("startedAtMillis"),
        endedAtMillis = if (json.isNull("endedAtMillis")) null else json.getLong("endedAtMillis"),
        routineId = if (json.isNull("routineId")) null else json.getString("routineId"),
        finalResponse = if (json.isNull("finalResponse")) null
        else CheckInResponse.valueOf(json.getString("finalResponse")),
    )
}
