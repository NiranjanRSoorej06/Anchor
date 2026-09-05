package com.anchor.core.data

import com.anchor.domain.goals.Goal
import com.anchor.domain.meds.DoseLog
import com.anchor.domain.meds.MedReminder
import com.anchor.domain.meds.MedTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreJsonTest {

    // ── MedReminder round-trip ──────────────────────────────────────

    @Test
    fun `MedReminder round-trips through JSON`() {
        val original = MedReminder(
            id = "rem_1",
            name = "Sertraline",
            times = listOf(MedTime(hour = 8, minute = 30), MedTime(hour = 20, minute = 0)),
            enabled = false,
        )
        val json = StoreJson.medReminderToJson(original)
        val restored = StoreJson.jsonToMedReminder(json)
        assertEquals(original, restored)
    }

    @Test
    fun `MedReminder with empty times round-trips`() {
        val original = MedReminder(id = "x", name = "X", times = emptyList())
        val json = StoreJson.medReminderToJson(original)
        val restored = StoreJson.jsonToMedReminder(json)
        assertEquals(original, restored)
    }

    @Test
    fun `MedReminder JSON contains expected keys`() {
        val json = StoreJson.medReminderToJson(
            MedReminder(id = "r1", name = "Test", times = listOf(MedTime(9, 0)))
        )
        assertTrue(json.contains("\"id\""))
        assertTrue(json.contains("\"name\""))
        assertTrue(json.contains("\"times\""))
        assertTrue(json.contains("\"enabled\""))
    }

    // ── DoseLog round-trip ──────────────────────────────────────────

    @Test
    fun `DoseLog round-trips through JSON`() {
        val original = DoseLog(reminderId = "rem_1", timestampMillis = 1234567890L, taken = true)
        val json = StoreJson.doseLogToJson(original)
        val restored = StoreJson.jsonToDoseLog(json)
        assertEquals(original, restored)
    }

    @Test
    fun `DoseLog with taken=false round-trips`() {
        val original = DoseLog(reminderId = "rem_2", timestampMillis = 999L, taken = false)
        val json = StoreJson.doseLogToJson(original)
        val restored = StoreJson.jsonToDoseLog(json)
        assertEquals(original, restored)
        assertFalse(restored.taken)
    }

    // ── Goal round-trip ─────────────────────────────────────────────

    @Test
    fun `Goal round-trips through JSON`() {
        val original = Goal(
            id = "goal_1",
            text = "Practice breathing",
            targetPerWeek = 3,
            completionsMillis = listOf(1000L, 2000L, 3000L),
        )
        val json = StoreJson.goalToJson(original)
        val restored = StoreJson.jsonToGoal(json)
        assertEquals(original, restored)
    }

    @Test
    fun `Goal with empty completions round-trips`() {
        val original = Goal(id = "g2", text = "Short text", targetPerWeek = 1)
        val json = StoreJson.goalToJson(original)
        val restored = StoreJson.jsonToGoal(json)
        assertEquals(original, restored)
        assertTrue(restored.completionsMillis.isEmpty())
    }

    // ── JournalEntry round-trip ─────────────────────────────────────

    @Test
    fun `JournalEntry round-trips through JSON`() {
        val original = JournalEntry(id = "j1", millis = 9876543210L, text = "Today was good")
        val json = StoreJson.journalEntryToJson(original)
        val restored = StoreJson.jsonToJournalEntry(json)
        assertEquals(original, restored)
    }

    @Test
    fun `JournalEntry with special characters round-trips`() {
        val original = JournalEntry(
            id = "j2",
            millis = 42L,
            text = "Quotes \"and\" backslash \\ and newlines\n oh my",
        )
        val json = StoreJson.journalEntryToJson(original)
        val restored = StoreJson.jsonToJournalEntry(json)
        assertEquals(original, restored)
    }

    // ── List helpers ────────────────────────────────────────────────

    @Test
    fun `listToJson produces valid JSON array`() {
        val items = listOf("a", "b", "c")
        val json = StoreJson.listToJson(items) { "\"$it\"" }
        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))
        assertTrue(json.contains("\"a\""))
        assertTrue(json.contains("\"b\""))
        assertTrue(json.contains("\"c\""))
    }

    @Test
    fun `jsonToList parses empty array`() {
        val result = StoreJson.jsonToList("[]") { it }
        assertTrue(result.isEmpty())
    }

    @Test
    fun `jsonToList parses blank string as empty`() {
        val result = StoreJson.jsonToList("") { it }
        assertTrue(result.isEmpty())
    }

    @Test
    fun `MedReminder list round-trips`() {
        val reminders = listOf(
            MedReminder(id = "r1", name = "A", times = listOf(MedTime(8, 0))),
            MedReminder(id = "r2", name = "B", times = listOf(MedTime(12, 30)), enabled = false),
        )
        val json = StoreJson.listToJson(reminders) { StoreJson.medReminderToJson(it) }
        val restored = StoreJson.jsonToList(json) { StoreJson.jsonToMedReminder(it) }
        assertEquals(reminders, restored)
    }

    @Test
    fun `Goal list round-trips`() {
        val goals = listOf(
            Goal(id = "g1", text = "First", targetPerWeek = 2, completionsMillis = listOf(100L)),
            Goal(id = "g2", text = "Second", targetPerWeek = 5),
        )
        val json = StoreJson.listToJson(goals) { StoreJson.goalToJson(it) }
        val restored = StoreJson.jsonToList(json) { StoreJson.jsonToGoal(it) }
        assertEquals(goals, restored)
    }

    @Test
    fun `DoseLog list round-trips`() {
        val logs = listOf(
            DoseLog(reminderId = "r1", timestampMillis = 100L, taken = true),
            DoseLog(reminderId = "r1", timestampMillis = 200L, taken = false),
        )
        val json = StoreJson.listToJson(logs) { StoreJson.doseLogToJson(it) }
        val restored = StoreJson.jsonToList(json) { StoreJson.jsonToDoseLog(it) }
        assertEquals(logs, restored)
    }

    @Test
    fun `JournalEntry list round-trips`() {
        val entries = listOf(
            JournalEntry(id = "j1", millis = 1000L, text = "First"),
            JournalEntry(id = "j2", millis = 2000L, text = "Second"),
        )
        val json = StoreJson.listToJson(entries) { StoreJson.journalEntryToJson(it) }
        val restored = StoreJson.jsonToList(json) { StoreJson.jsonToJournalEntry(it) }
        assertEquals(entries, restored)
    }
}
