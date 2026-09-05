package com.anchor.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.anchor.domain.personalization.SessionOutcome
import com.anchor.domain.session.CheckInResponse
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device store for [SessionOutcome] records, backed by
 * [SharedPreferences] + JSON — same pattern as [EpisodeStorePersistent].
 *
 * This is what actually closes the loop [PersonalizationScorer][com.anchor.domain.personalization.PersonalizationScorer]
 * and [InterventionRouter][com.anchor.domain.routing.InterventionRouter]
 * were built for: every completed Anchor session or Manage Symptoms
 * exercise appends one record here, and every subsequent router call reads
 * the full history back so ranking can actually improve over time.
 */
class SessionOutcomeStore(context: Context) {

    private companion object {
        const val PREF_NAME = "anchor_session_outcomes_pref"
        const val KEY_OUTCOMES_JSON = "session_outcomes_json"
        const val TAG = "SessionOutcomeStore"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun all(): List<SessionOutcome> {
        val jsonStr = prefs.getString(KEY_OUTCOMES_JSON, null) ?: return emptyList()
        val result = mutableListOf<SessionOutcome>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    SessionOutcome(
                        sessionId = obj.getString("sessionId"),
                        routineId = if (obj.isNull("routineId")) null else obj.getString("routineId"),
                        response = CheckInResponse.valueOf(obj.getString("response")),
                        timestampMillis = obj.getLong("timestampMillis"),
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse session outcomes: ${e.message}", e)
        }
        return result
    }

    fun append(outcome: SessionOutcome) {
        val outcomes = all() + outcome
        val array = JSONArray()
        outcomes.forEach { o ->
            array.put(
                JSONObject().apply {
                    put("sessionId", o.sessionId)
                    put("routineId", o.routineId ?: JSONObject.NULL)
                    put("response", o.response.name)
                    put("timestampMillis", o.timestampMillis)
                }
            )
        }
        prefs.edit().putString(KEY_OUTCOMES_JSON, array.toString()).apply()
    }
}
