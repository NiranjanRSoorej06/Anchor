package com.anchor.core.logging

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SprintExperienceLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val comfortableToTalk: Boolean,
    val distressLevel: String? = null,
    val primaryTrigger: String? = null,
    val hapticsHelpful: String? = null,
    val audioComfort: String? = null,
    val finalState: String? = null,
    val alertSentToTrustedContacts: Boolean = false
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("timestamp", timestamp)
            put("comfortableToTalk", comfortableToTalk)
            put("distressLevel", distressLevel ?: "")
            put("primaryTrigger", primaryTrigger ?: "")
            put("hapticsHelpful", hapticsHelpful ?: "")
            put("audioComfort", audioComfort ?: "")
            put("finalState", finalState ?: "")
            put("alertSentToTrustedContacts", alertSentToTrustedContacts)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): SprintExperienceLog {
            return SprintExperienceLog(
                id = json.optString("id", UUID.randomUUID().toString()),
                timestamp = json.optString("timestamp", ""),
                comfortableToTalk = json.optBoolean("comfortableToTalk", false),
                distressLevel = json.optString("distressLevel").takeIf { it.isNotBlank() },
                primaryTrigger = json.optString("primaryTrigger").takeIf { it.isNotBlank() },
                hapticsHelpful = json.optString("hapticsHelpful").takeIf { it.isNotBlank() },
                audioComfort = json.optString("audioComfort").takeIf { it.isNotBlank() },
                finalState = json.optString("finalState").takeIf { it.isNotBlank() },
                alertSentToTrustedContacts = json.optBoolean("alertSentToTrustedContacts", false)
            )
        }
    }
}

class SprintLogStore(context: Context) {

    private companion object {
        const val PREF_NAME = "anchor_sprint_logs_pref"
        const val KEY_LOGS_ARRAY = "sprint_experience_logs"
        const val TAG = "SprintLogStore"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveLog(log: SprintExperienceLog) {
        try {
            val logs = getLogs().toMutableList()
            logs.add(0, log) // prepend latest
            val jsonArray = JSONArray()
            logs.take(50).forEach { item ->
                jsonArray.put(item.toJsonObject())
            }
            prefs.edit().putString(KEY_LOGS_ARRAY, jsonArray.toString()).apply()
            Log.i(TAG, "Successfully saved sprint experience log: id=${log.id}, finalState=${log.finalState}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save sprint log: ${e.message}", e)
        }
    }

    fun getLogs(): List<SprintExperienceLog> {
        val jsonStr = prefs.getString(KEY_LOGS_ARRAY, null) ?: return emptyList()
        val result = mutableListOf<SprintExperienceLog>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(SprintExperienceLog.fromJsonObject(obj))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse sprint logs: ${e.message}", e)
        }
        return result
    }

    fun clearLogs() {
        prefs.edit().remove(KEY_LOGS_ARRAY).apply()
    }
}
