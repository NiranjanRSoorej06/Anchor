package com.anchor.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.anchor.domain.content.SafetyPhraseRecording
import org.json.JSONArray
import org.json.JSONObject

/**
 * On-device store for [SafetyPhraseRecording]s, backed by
 * [SharedPreferences] + JSON — same pattern as every other store in this
 * package. Also owns which single recording is "active" (the one bound
 * to loop during the Anchor comfort tool) — mirrored into
 * [AnchorRoutinePreferences.calmingAudioUri]/`calmingAudioLabel` when set,
 * so the existing, already-tested ambient-audio-loop playback path needs
 * no changes at all to support this feature.
 */
class SafetyPhraseRecordingStore(context: Context) {

    private companion object {
        const val PREF_NAME = "anchor_safety_phrase_recordings_pref"
        const val KEY_RECORDINGS_JSON = "safety_phrase_recordings_json"
        const val TAG = "SafetyPhraseRecordingStore"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun all(): List<SafetyPhraseRecording> {
        val jsonStr = prefs.getString(KEY_RECORDINGS_JSON, null) ?: return emptyList()
        val result = mutableListOf<SafetyPhraseRecording>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                result.add(fromJson(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse safety phrase recordings: ${e.message}", e)
        }
        return result
    }

    /** Upserts [recording] by [SafetyPhraseRecording.id]. */
    fun save(recording: SafetyPhraseRecording) {
        val recordings = linkedMapOf<String, SafetyPhraseRecording>()
        all().forEach { recordings[it.id] = it }
        recordings[recording.id] = recording
        saveAll(recordings.values.toList())
    }

    fun delete(id: String) {
        saveAll(all().filter { it.id != id })
    }

    fun byId(id: String): SafetyPhraseRecording? = all().firstOrNull { it.id == id }

    private fun saveAll(recordings: List<SafetyPhraseRecording>) {
        val array = JSONArray()
        recordings.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_RECORDINGS_JSON, array.toString()).apply()
    }

    private fun toJson(recording: SafetyPhraseRecording): JSONObject = JSONObject().apply {
        put("id", recording.id)
        put("phraseText", recording.phraseText)
        put("filePath", recording.filePath)
        put("createdAtMillis", recording.createdAtMillis)
    }

    private fun fromJson(json: JSONObject): SafetyPhraseRecording = SafetyPhraseRecording(
        id = json.getString("id"),
        phraseText = json.getString("phraseText"),
        filePath = json.getString("filePath"),
        createdAtMillis = json.optLong("createdAtMillis", 0L),
    )
}
