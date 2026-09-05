package com.anchor.core.data

import com.anchor.domain.goals.Goal
import com.anchor.domain.meds.DoseLog
import com.anchor.domain.meds.MedReminder
import com.anchor.domain.meds.MedTime
import org.json.JSONArray
import org.json.JSONObject

/**
 * JSON serialization helpers for store models.
 *
 * Uses [org.json] (available in Android SDK and as a JVM Maven artifact
 * for unit tests). Each serializer is a pure function — no side effects,
 * no Android dependencies beyond the JSON library.
 */
object StoreJson {

    // ── MedReminder ─────────────────────────────────────────────────

    fun medReminderToJson(r: MedReminder): String {
        val timesArray = JSONArray()
        r.times.forEach { t ->
            timesArray.put(JSONObject().apply {
                put("hour", t.hour)
                put("minute", t.minute)
            })
        }
        return JSONObject().apply {
            put("id", r.id)
            put("name", r.name)
            put("times", timesArray)
            put("enabled", r.enabled)
        }.toString()
    }

    fun jsonToMedReminder(json: String): MedReminder {
        val obj = JSONObject(json)
        val timesArray = obj.getJSONArray("times")
        val times = (0 until timesArray.length()).map { i ->
            val t = timesArray.getJSONObject(i)
            MedTime(hour = t.getInt("hour"), minute = t.getInt("minute"))
        }
        return MedReminder(
            id = obj.getString("id"),
            name = obj.getString("name"),
            times = times,
            enabled = obj.optBoolean("enabled", true),
        )
    }

    // ── DoseLog ─────────────────────────────────────────────────────

    fun doseLogToJson(d: DoseLog): String {
        return JSONObject().apply {
            put("reminderId", d.reminderId)
            put("timestampMillis", d.timestampMillis)
            put("taken", d.taken)
        }.toString()
    }

    fun jsonToDoseLog(json: String): DoseLog {
        val obj = JSONObject(json)
        return DoseLog(
            reminderId = obj.getString("reminderId"),
            timestampMillis = obj.getLong("timestampMillis"),
            taken = obj.getBoolean("taken"),
        )
    }

    // ── Goal ────────────────────────────────────────────────────────

    fun goalToJson(g: Goal): String {
        val completionsArray = JSONArray()
        g.completionsMillis.forEach { completionsArray.put(it) }
        return JSONObject().apply {
            put("id", g.id)
            put("text", g.text)
            put("targetPerWeek", g.targetPerWeek)
            put("completionsMillis", completionsArray)
        }.toString()
    }

    fun jsonToGoal(json: String): Goal {
        val obj = JSONObject(json)
        val compArray = obj.getJSONArray("completionsMillis")
        val completions = (0 until compArray.length()).map { compArray.getLong(it) }
        return Goal(
            id = obj.getString("id"),
            text = obj.getString("text"),
            targetPerWeek = obj.getInt("targetPerWeek"),
            completionsMillis = completions,
        )
    }

    // ── JournalEntry ────────────────────────────────────────────────

    fun journalEntryToJson(e: JournalEntry): String {
        return JSONObject().apply {
            put("id", e.id)
            put("millis", e.millis)
            put("text", e.text)
        }.toString()
    }

    fun jsonToJournalEntry(json: String): JournalEntry {
        val obj = JSONObject(json)
        return JournalEntry(
            id = obj.getString("id"),
            millis = obj.getLong("millis"),
            text = obj.getString("text"),
        )
    }

    // ── List helpers ────────────────────────────────────────────────

    fun <T> listToJson(items: List<T>, itemToJson: (T) -> String): String {
        val array = JSONArray()
        items.forEach { array.put(JSONObject(itemToJson(it))) }
        return array.toString()
    }

    fun <T> jsonToList(json: String, jsonToItem: (String) -> T): List<T> {
        if (json.isBlank() || json == "[]") return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            jsonToItem(array.getJSONObject(i).toString())
        }
    }
}
