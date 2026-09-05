package com.anchor.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.anchor.domain.goals.Goal
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device store for [Goal]s, backed by [SharedPreferences] + JSON —
 * same pattern as [EpisodeStorePersistent] / `CompanionPreferences`.
 */
class GoalsStore(context: Context) {

    private companion object {
        const val PREF_NAME = "anchor_goals_pref"
        const val KEY_GOALS_JSON = "goals_json"
        const val TAG = "GoalsStore"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun all(): List<Goal> {
        val jsonStr = prefs.getString(KEY_GOALS_JSON, null) ?: return emptyList()
        val result = mutableListOf<Goal>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                result.add(fromJson(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse goals: ${e.message}", e)
        }
        return result
    }

    /** Upserts [goal] by [Goal.id]. */
    fun save(goal: Goal) {
        val goals = linkedMapOf<String, Goal>()
        all().forEach { goals[it.id] = it }
        goals[goal.id] = goal
        saveAll(goals.values.toList())
    }

    fun delete(id: String) {
        saveAll(all().filter { it.id != id })
    }

    private fun saveAll(goals: List<Goal>) {
        val array = JSONArray()
        goals.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_GOALS_JSON, array.toString()).apply()
    }

    private fun toJson(goal: Goal): JSONObject = JSONObject().apply {
        put("id", goal.id)
        put("text", goal.text)
        put("targetPerWeek", goal.targetPerWeek)
        put("completionsMillis", JSONArray(goal.completionsMillis))
    }

    private fun fromJson(json: JSONObject): Goal {
        val completions = mutableListOf<Long>()
        val array = json.optJSONArray("completionsMillis")
        if (array != null) {
            for (i in 0 until array.length()) completions.add(array.getLong(i))
        }
        return Goal(
            id = json.getString("id"),
            text = json.getString("text"),
            targetPerWeek = json.getInt("targetPerWeek"),
            completionsMillis = completions,
        )
    }
}
