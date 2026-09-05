package com.anchor.domain.sleep

/**
 * One night's self-reported sleep diary entry.
 *
 * The field set mirrors a standard **CBT-I sleep diary** (Irish et al. 2015;
 * SF Gov CBT-I handout — see `docs/content-sources.md`): the user reports
 * when they went to bed, how long it took to fall asleep, how much of the
 * night they were awake, and when they finally got up. From those, the
 * derived metrics in [SleepMetrics] (total sleep time, sleep efficiency) are
 * the same summary numbers a paper diary produces.
 *
 * This is **self-tracking psychoeducation, not treatment**. Anchor never
 * computes a sleep-restriction prescription (that requires clinician
 * titration) and never diagnoses.
 *
 * This is plain Kotlin: no Android, no Compose.
 *
 * Clock times are stored as **minute-of-day** (`0..1439`, i.e. `13*60+30`
 * for 13:30) so the model stays timezone-free and deterministic. A sleep
 * period that crosses midnight is handled by [SleepMetrics.timeInBedMin],
 * which adds a day when the wake time is not after the bedtime.
 *
 * @property id Unique identifier for this entry.
 * @property nightOfMillis Unix epoch millis for the calendar date the sleep
 *   period *started* (the evening the user went to bed). Used only for
 *   ordering and the rolling-window summary.
 * @property bedtimeMinOfDay Minute-of-day the user got into bed.
 * @property sleepLatencyMin Minutes it took to fall asleep after lights out.
 * @property awakeningsCount How many times the user woke during the night.
 * @property awakeningsMin Total minutes spent awake during those awakenings.
 * @property finalWakeMinOfDay Minute-of-day the user woke for the last time.
 * @property outOfBedMinOfDay Minute-of-day the user actually got out of bed.
 * @property quality Subjective rating from 1 (very poor) to 5 (very good).
 * @property hadNightmare Whether the user recalls a nightmare this night.
 * @property note Optional short free-text note (≤ [MAX_NOTE_CHARS] chars).
 */
data class SleepEntry(
    val id: String,
    val nightOfMillis: Long,
    val bedtimeMinOfDay: Int,
    val sleepLatencyMin: Int,
    val awakeningsCount: Int,
    val awakeningsMin: Int,
    val finalWakeMinOfDay: Int,
    val outOfBedMinOfDay: Int,
    val quality: Int,
    val hadNightmare: Boolean = false,
    val note: String? = null,
) {
    companion object {
        const val MAX_NOTE_CHARS = 280
        const val MIN_QUALITY = 1
        const val MAX_QUALITY = 5
        const val MINUTES_PER_DAY = 24 * 60
    }
}

/**
 * A rolling-window summary over a set of [SleepEntry] rows — the same
 * numbers a CBT-I clinician reads off a week of diary pages.
 *
 * @property nightsLogged How many entries fell inside the window.
 * @property avgTotalSleepMin Mean total sleep time in minutes (0 when
 *   [nightsLogged] is 0).
 * @property avgEfficiencyPct Mean sleep efficiency percentage, 0..100.
 * @property nightmareNights How many nights in the window had a nightmare.
 */
data class SleepSummary(
    val nightsLogged: Int,
    val avgTotalSleepMin: Int,
    val avgEfficiencyPct: Int,
    val nightmareNights: Int,
)

/**
 * Pure-Kotlin derivation of the standard sleep-diary metrics.
 *
 * All functions are deterministic and take their "now" explicitly so they
 * are trivially testable (mirrors [com.anchor.domain.goals.GoalProgress]).
 */
object SleepMetrics {

    private const val MS_PER_DAY: Long = 24L * 60 * 60 * 1000

    /**
     * Minutes between getting into bed and getting out of bed. If
     * [SleepEntry.outOfBedMinOfDay] is not strictly after
     * [SleepEntry.bedtimeMinOfDay], the period is assumed to cross midnight
     * and a full day is added.
     */
    fun timeInBedMin(entry: SleepEntry): Int {
        val raw = entry.outOfBedMinOfDay - entry.bedtimeMinOfDay
        return if (raw > 0) raw else raw + SleepEntry.MINUTES_PER_DAY
    }

    /**
     * Total sleep time: time in bed minus time spent falling asleep and
     * time spent awake during the night. Never negative.
     */
    fun totalSleepTimeMin(entry: SleepEntry): Int =
        (timeInBedMin(entry) - entry.sleepLatencyMin - entry.awakeningsMin).coerceAtLeast(0)

    /**
     * Sleep efficiency: total sleep time as a percentage of time in bed,
     * clamped to `0..100`. Returns 0 when time in bed is 0.
     */
    fun sleepEfficiencyPct(entry: SleepEntry): Int {
        val tib = timeInBedMin(entry)
        if (tib <= 0) return 0
        return ((totalSleepTimeMin(entry) * 100.0) / tib).toInt().coerceIn(0, 100)
    }

    /**
     * Summarise the entries whose [SleepEntry.nightOfMillis] falls inside
     * the window `(nowMillis - nights*24h, nowMillis]`.
     */
    fun rollingSummary(
        entries: List<SleepEntry>,
        nowMillis: Long,
        nights: Int = 7,
    ): SleepSummary {
        val lowerBound = nowMillis - nights * MS_PER_DAY
        val window = entries.filter { it.nightOfMillis in (lowerBound + 1)..nowMillis }
        if (window.isEmpty()) return SleepSummary(0, 0, 0, 0)
        return SleepSummary(
            nightsLogged = window.size,
            avgTotalSleepMin = window.sumOf { totalSleepTimeMin(it) } / window.size,
            avgEfficiencyPct = window.sumOf { sleepEfficiencyPct(it) } / window.size,
            nightmareNights = window.count { it.hadNightmare },
        )
    }
}

/**
 * Pure-Kotlin validator for [SleepEntry]. Collects every failure without
 * short-circuiting so the caller can present the full list at once — same
 * shape as [com.anchor.domain.goals.GoalValidator] and
 * [com.anchor.domain.journal.JournalValidator].
 */
object SleepValidator {

    private fun Int.isMinuteOfDay() = this in 0 until SleepEntry.MINUTES_PER_DAY

    fun validate(entry: SleepEntry): ValidationResult {
        val reasons = mutableListOf<String>()

        if (entry.id.isBlank()) reasons += "id must not be blank"
        if (entry.nightOfMillis < 0) reasons += "nightOfMillis must not be negative"
        if (!entry.bedtimeMinOfDay.isMinuteOfDay()) reasons += "bedtime must be a valid time of day"
        if (!entry.finalWakeMinOfDay.isMinuteOfDay()) reasons += "final wake must be a valid time of day"
        if (!entry.outOfBedMinOfDay.isMinuteOfDay()) reasons += "out-of-bed must be a valid time of day"
        if (entry.sleepLatencyMin < 0) reasons += "sleep latency must not be negative"
        if (entry.awakeningsCount < 0) reasons += "awakenings count must not be negative"
        if (entry.awakeningsMin < 0) reasons += "time awake must not be negative"
        if (entry.quality < SleepEntry.MIN_QUALITY || entry.quality > SleepEntry.MAX_QUALITY) {
            reasons += "quality must be ${SleepEntry.MIN_QUALITY}..${SleepEntry.MAX_QUALITY}"
        }

        val tib = SleepMetrics.timeInBedMin(entry)
        if (entry.sleepLatencyMin + entry.awakeningsMin > tib) {
            reasons += "time falling asleep plus time awake cannot exceed time in bed"
        }

        entry.note?.let {
            if (it.length > SleepEntry.MAX_NOTE_CHARS) {
                reasons += "note must not exceed ${SleepEntry.MAX_NOTE_CHARS} characters"
            }
        }

        return if (reasons.isEmpty()) ValidationResult.Valid
        else ValidationResult.Invalid(reasons)
    }
}

/**
 * The result of validating a [SleepEntry]. [Valid] means the entry
 * satisfies every rule; [Invalid] carries the full list of failure reasons.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reasons: List<String>) : ValidationResult
}
