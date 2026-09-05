package com.anchor.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.anchor.domain.profile.UserProfile
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device store for the single [UserProfile], backed by
 * [SharedPreferences] + JSON — same pattern as [EpisodeStorePersistent].
 * Deliberately never stores a trauma-narrative field — [UserProfile] has
 * none by design.
 */
class UserProfileStore(context: Context) {

    private companion object {
        const val PREF_NAME = "anchor_user_profile_pref"
        const val KEY_PROFILE_JSON = "user_profile_json"
        const val TAG = "UserProfileStore"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun get(): UserProfile? {
        val jsonStr = prefs.getString(KEY_PROFILE_JSON, null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            UserProfile(
                triggerSituations = json.stringSet("triggerSituations"),
                audioOk = json.optBoolean("audioOk", true),
                voiceOk = json.optBoolean("voiceOk", true),
                hapticIntensity = json.optDouble("hapticIntensity", 1.0).toFloat(),
                touchSensitive = json.optBoolean("touchSensitive", false),
                reducedVisual = json.optBoolean("reducedVisual", false),
                notForMe = json.stringSet("notForMe"),
                trustedContactNumber = json.optNullableString("trustedContactNumber"),
                emergencyNumber = json.optNullableString("emergencyNumber"),
                onboardingComplete = json.optBoolean("onboardingComplete", false),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse user profile: ${e.message}", e)
            null
        }
    }

    fun save(profile: UserProfile) {
        val json = JSONObject().apply {
            put("triggerSituations", JSONArray(profile.triggerSituations))
            put("audioOk", profile.audioOk)
            put("voiceOk", profile.voiceOk)
            put("hapticIntensity", profile.hapticIntensity.toDouble())
            put("touchSensitive", profile.touchSensitive)
            put("reducedVisual", profile.reducedVisual)
            put("notForMe", JSONArray(profile.notForMe))
            put("trustedContactNumber", profile.trustedContactNumber ?: JSONObject.NULL)
            put("emergencyNumber", profile.emergencyNumber ?: JSONObject.NULL)
            put("onboardingComplete", profile.onboardingComplete)
        }
        prefs.edit().putString(KEY_PROFILE_JSON, json.toString()).apply()
    }

    private fun JSONObject.stringSet(key: String): Set<String> {
        val array = optJSONArray(key) ?: return emptySet()
        return (0 until array.length()).map { array.getString(it) }.toSet()
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
}
