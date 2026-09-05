package com.anchor.domain.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [GuidedScripts] and the script data model.
 *
 * Pure-Kotlin — no Android dependencies.
 *
 * Verifies:
 * - Script count and unique ids
 * - [GuidedScripts.byId] hit/miss
 * - Per-script step counts match spec
 * - Total duration spot-checks
 * - All texts non-blank
 * - All durations in 1..120
 * - Every script total ≤ 300 s
 * - Skip-cue present in 54321
 * - Exhale opening caveat present
 */
class GuidedScriptsTest {

    // ── Structural invariants ──────────────────────────────────────────

    @Test
    fun allContainsExactlyNineScripts() {
        assertEquals(9, GuidedScripts.ALL.size)
    }

    @Test
    fun allIdsAreUnique() {
        val ids = GuidedScripts.ALL.map { it.id }
        assertEquals(
            "Ids must be unique, but duplicates found: ${ids.groupingBy { it }.eachCount().filter { it.value > 1 }}",
            ids.toSet().size,
            ids.size
        )
    }

    // ── byId lookups ───────────────────────────────────────────────────

    @Test
    fun byIdReturnsKnownScript() {
        assertNotNull(GuidedScripts.byId("breathing_box"))
        assertEquals("breathing_box", GuidedScripts.byId("breathing_box")?.id)
    }

    @Test
    fun byIdReturnsNullForUnknownId() {
        assertNull(GuidedScripts.byId("nonexistent_script"))
    }

    // ── Per-script step counts ─────────────────────────────────────────

    @Test
    fun grounding54321HasFifteenSteps() {
        val script = GuidedScripts.byId("grounding_54321")!!
        assertEquals(15, script.steps.size)
    }

    @Test
    fun grounding3StepHasThreeSteps() {
        val script = GuidedScripts.byId("grounding_3step")!!
        assertEquals(3, script.steps.size)
    }

    @Test
    fun breathingBoxHasSixteenSteps() {
        val script = GuidedScripts.byId("breathing_box")!!
        assertEquals(16, script.steps.size)
    }

    @Test
    fun breathingCoherentHasTwelveSteps() {
        val script = GuidedScripts.byId("breathing_coherent")!!
        assertEquals(12, script.steps.size)
    }

    @Test
    fun breathingExhaleHasEightSteps() {
        val script = GuidedScripts.byId("breathing_exhale")!!
        assertEquals(8, script.steps.size)
    }

    @Test
    fun breathing478SleepHasThirteenSteps() {
        val script = GuidedScripts.byId("breathing_478_sleep")!!
        assertEquals(13, script.steps.size)
    }

    @Test
    fun pmrFullHasTwentyEightSteps() {
        val script = GuidedScripts.byId("pmr_full")!!
        assertEquals(28, script.steps.size)
    }

    @Test
    fun visualizationMonsoonHasTenSteps() {
        val script = GuidedScripts.byId("visualization_monsoon")!!
        assertEquals(10, script.steps.size)
    }

    @Test
    fun visualizationTempleHasTenSteps() {
        val script = GuidedScripts.byId("visualization_temple")!!
        assertEquals(10, script.steps.size)
    }

    // ── Duration spot-checks ───────────────────────────────────────────

    @Test
    fun breathingBoxTotalDurationIs64Seconds() {
        val script = GuidedScripts.byId("breathing_box")!!
        assertEquals(64, script.totalDurationSec())
    }

    @Test
    fun breathingCoherentTotalDurationIs60Seconds() {
        val script = GuidedScripts.byId("breathing_coherent")!!
        assertEquals(60, script.totalDurationSec())
    }

    @Test
    fun visualizationMonsoonTotalDurationIs190Seconds() {
        val script = GuidedScripts.byId("visualization_monsoon")!!
        assertEquals(190, script.totalDurationSec())
    }

    @Test
    fun visualizationTempleTotalDurationIs190Seconds() {
        val script = GuidedScripts.byId("visualization_temple")!!
        assertEquals(190, script.totalDurationSec())
    }

    // ── All texts non-blank ────────────────────────────────────────────

    @Test
    fun allStepTextsAreNonBlank() {
        GuidedScripts.ALL.forEach { script ->
            script.steps.forEach { step ->
                assertTrue(
                    "${script.id}: step text must be non-blank, was '${step.text}'",
                    step.text.isNotBlank()
                )
            }
        }
    }

    // ── All durations in range 1..120 ─────────────────────────────────

    @Test
    fun allStepDurationsAreInRange() {
        GuidedScripts.ALL.forEach { script ->
            script.steps.forEach { step ->
                assertTrue(
                    "${script.id}: durationSec must be ≥ 1, was ${step.durationSec}",
                    step.durationSec >= 1
                )
                assertTrue(
                    "${script.id}: durationSec must be ≤ 120, was ${step.durationSec}",
                    step.durationSec <= 120
                )
            }
        }
    }

    // ── Every script total ≤ 300 s ─────────────────────────────────────

    @Test
    fun everyScriptTotalDurationIsAtMost300Seconds() {
        GuidedScripts.ALL.forEach { script ->
            assertTrue(
                "${script.id}: total duration ${script.totalDurationSec()}s must be ≤ 300s",
                script.totalDurationSec() <= 300
            )
        }
    }

    // ── Skip-cue in 5-4-3-2-1 ─────────────────────────────────────────

    @Test
    fun grounding54321ContainsSkipCue() {
        val script = GuidedScripts.byId("grounding_54321")!!
        val hasSkipCue = script.steps.any { step ->
            step.text.contains("skip", ignoreCase = true)
        }
        assertTrue(
            "5-4-3-2-1 script must contain a skip-cue mentioning 'skip' (case-insensitive)",
            hasSkipCue
        )
    }

    // ── Exhale opening caveat ───────────────────────────────────────────

    @Test
    fun breathingExhaleContainsOpeningCaveat() {
        val script = GuidedScripts.byId("breathing_exhale")!!
        val hasCaveat = script.steps.any { step ->
            step.text.contains("skip the pauses", ignoreCase = true)
        }
        assertTrue(
            "Long exhale script must contain the caveat 'skip the pauses' (case-insensitive)",
            hasCaveat
        )
    }

    @Test
    fun breathing478SleepContainsOpeningCaveat() {
        val script = GuidedScripts.byId("breathing_478_sleep")!!
        val hasCaveat = script.steps.any { step ->
            step.text.contains("skip the pauses", ignoreCase = true)
        }
        assertTrue(
            "4-7-8 sleep script must contain the caveat 'skip the pauses' (case-insensitive)",
            hasCaveat
        )
    }
}
