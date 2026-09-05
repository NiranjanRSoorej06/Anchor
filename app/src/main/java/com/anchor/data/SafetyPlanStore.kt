package com.anchor.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.anchor.domain.safetyplan.SafetyPlan
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device store for the single [SafetyPlan] a user maintains, backed by
 * [SharedPreferences] + JSON — same pattern as [EpisodeStorePersistent].
 * Display-only per the domain model's own contract: this store never
 * triggers a call, SMS, or any other action on its own.
 */
class SafetyPlanStore(context: Context) {

    private companion object {
        const val PREF_NAME = "anchor_safety_plan_pref"
        const val KEY_PLAN_JSON = "safety_plan_json"
        const val TAG = "SafetyPlanStore"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun get(): SafetyPlan? {
        val jsonStr = prefs.getString(KEY_PLAN_JSON, null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            SafetyPlan(
                warningSigns = json.stringList("warningSigns"),
                copingStrategies = json.stringList("copingStrategies"),
                socialDistraction = json.stringList("socialDistraction"),
                helpContacts = json.stringList("helpContacts"),
                professionals = json.stringList("professionals"),
                meansRestriction = json.stringList("meansRestriction"),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse safety plan: ${e.message}", e)
            null
        }
    }

    fun save(plan: SafetyPlan) {
        val json = JSONObject().apply {
            put("warningSigns", JSONArray(plan.warningSigns))
            put("copingStrategies", JSONArray(plan.copingStrategies))
            put("socialDistraction", JSONArray(plan.socialDistraction))
            put("helpContacts", JSONArray(plan.helpContacts))
            put("professionals", JSONArray(plan.professionals))
            put("meansRestriction", JSONArray(plan.meansRestriction))
        }
        prefs.edit().putString(KEY_PLAN_JSON, json.toString()).apply()
    }

    private fun JSONObject.stringList(key: String): List<String> {
        val array = optJSONArray(key) ?: return emptyList()
        return (0 until array.length()).map { array.getString(it) }
    }
}
