package com.anchor.domain.sleep

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SleepMetrics] and [SleepValidator].
 *
 * Pure-Kotlin — no Android dependencies. Fixed timestamps only, no
 * `System.currentTimeMillis` (mirrors `GoalsTest`).
 */
class SleepMetricsTest {

    private companion object {
        const val NOW = 1_000_000_000_000L
        const val DAY = 86_400_000L

        /** Bed 23:00, 20 min to sleep, 2 awakenings totalling 15 min, up at 07:00. */
        fun sample() = SleepEntry(
            id = "n1",
            nightOfMillis = NOW - DAY,
            bedtimeMinOfDay = 23 * 60,
            sleepLatencyMin = 20,
            awakeningsCount = 2,
            awakeningsMin = 15,
            finalWakeMinOfDay = 6 * 60 + 45,
            outOfBedMinOfDay = 7 * 60,
            quality = 3,
        )
    }

    // ── timeInBedMin ───────────────────────────────────────────────────

    @Test
    fun `time in bed handles the past-midnight wrap`() {
        assertEquals(480, SleepMetrics.timeInBedMin(sample()))
    }

    @Test
    fun `time in bed without a wrap`() {
        // Bed 01:00, up at 08:00 → 7 h, no day added.
        val e = sample().copy(bedtimeMinOfDay = 60, outOfBedMinOfDay = 8 * 60)
        assertEquals(420, SleepMetrics.timeInBedMin(e))
    }

    // ── totalSleepTimeMin / sleepEfficiencyPct ─────────────────────────

    @Test
    fun `total sleep time subtracts latency and time awake`() {
        assertEquals(480 - 20 - 15, SleepMetrics.totalSleepTimeMin(sample()))
    }

    @Test
    fun `total sleep time is never negative`() {
        val e = sample().copy(sleepLatencyMin = 600, awakeningsMin = 600)
        assertEquals(0, SleepMetrics.totalSleepTimeMin(e))
    }

    @Test
    fun `sleep efficiency is total sleep over time in bed`() {
        // 445 / 480 = 92.7% → 92 after truncation.
        assertEquals(92, SleepMetrics.sleepEfficiencyPct(sample()))
    }

    @Test
    fun `sleep efficiency is zero when no sleep was had`() {
        // Awake the entire 8 h in bed.
        val e = sample().copy(sleepLatencyMin = 480, awakeningsMin = 0)
        assertEquals(0, SleepMetrics.sleepEfficiencyPct(e))
    }

    @Test
    fun `sleep efficiency is always within 0 to 100`() {
        val e = sample().copy(sleepLatencyMin = 0, awakeningsMin = 0)
        assertTrue(SleepMetrics.sleepEfficiencyPct(e) in 0..100)
    }

    // ── rollingSummary ─────────────────────────────────────────────────

    @Test
    fun `rolling summary averages nights inside the window`() {
        val a = sample().copy(id = "a", nightOfMillis = NOW - DAY, hadNightmare = true)
        val b = sample().copy(id = "b", nightOfMillis = NOW - 2 * DAY, quality = 5)
        val summary = SleepMetrics.rollingSummary(listOf(a, b), NOW, nights = 7)
        assertEquals(2, summary.nightsLogged)
        assertEquals(445, summary.avgTotalSleepMin)
        assertEquals(92, summary.avgEfficiencyPct)
        assertEquals(1, summary.nightmareNights)
    }

    @Test
    fun `rolling summary excludes a night 8 days ago`() {
        val old = sample().copy(id = "old", nightOfMillis = NOW - 8 * DAY)
        val summary = SleepMetrics.rollingSummary(listOf(old), NOW, nights = 7)
        assertEquals(0, summary.nightsLogged)
        assertEquals(0, summary.avgTotalSleepMin)
    }

    @Test
    fun `rolling summary on no entries is all zeros`() {
        val summary = SleepMetrics.rollingSummary(emptyList(), NOW)
        assertEquals(SleepSummary(0, 0, 0, 0), summary)
    }

    // ── SleepValidator ─────────────────────────────────────────────────

    @Test
    fun `valid entry returns Valid`() {
        assertTrue(SleepValidator.validate(sample()) is ValidationResult.Valid)
    }

    @Test
    fun `blank id is invalid`() {
        val result = SleepValidator.validate(sample().copy(id = " ")) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("id") })
    }

    @Test
    fun `quality out of range is invalid`() {
        val result = SleepValidator.validate(sample().copy(quality = 6)) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("quality") })
    }

    @Test
    fun `bedtime beyond a day is invalid`() {
        val result = SleepValidator.validate(sample().copy(bedtimeMinOfDay = 1500)) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("bedtime") })
    }

    @Test
    fun `latency plus time awake exceeding time in bed is invalid`() {
        val e = sample().copy(sleepLatencyMin = 400, awakeningsMin = 200)
        val result = SleepValidator.validate(e) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("time in bed") })
    }

    @Test
    fun `over-long note is invalid`() {
        val e = sample().copy(note = "z".repeat(SleepEntry.MAX_NOTE_CHARS + 1))
        val result = SleepValidator.validate(e) as ValidationResult.Invalid
        assertTrue(result.reasons.any { it.contains("note") })
    }

    @Test
    fun `multiple failures are all reported`() {
        val e = sample().copy(id = "", quality = 0, sleepLatencyMin = -5)
        val result = SleepValidator.validate(e) as ValidationResult.Invalid
        assertTrue("Expected at least 3 reasons", result.reasons.size >= 3)
    }
}
