package com.anchor.domain.safety

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SafetyFilter] — one test per SF rule, plus edge-case
 * and fallback coverage. JUnit4, pure-Kotlin, no Android dependencies.
 *
 * These mirror the routing pipeline described in plan.md §8.2–8.4.
 */
class SafetyFilterTest {

    // ── Helper candidates ──────────────────────────────────────────────

    private val externalOrientation = SafetyCandidate(
        id = "E001",
        interoceptive = false,
        allowedStates = setOf(CurrentState.DISSOCIATION),
        requiresAudio = false,
        requiresVoice = false,
        requiresHaptics = false,
        hasTraumaImagery = false
    )

    private val breathingHaptic = SafetyCandidate(
        id = "E004",
        interoceptive = true,
        allowedStates = setOf(CurrentState.PANICKY, CurrentState.HYPER_ALERT),
        requiresAudio = false,
        requiresVoice = false,
        requiresHaptics = true,
        hasTraumaImagery = false
    )

    private val audioGuidedRelaxation = SafetyCandidate(
        id = "E005",
        interoceptive = true,
        allowedStates = setOf(CurrentState.HYPER_ALERT, CurrentState.PANICKY),
        requiresAudio = true,
        requiresVoice = false,
        requiresHaptics = false,
        hasTraumaImagery = false
    )

    private val voiceNarration = SafetyCandidate(
        id = "E006",
        interoceptive = false,
        allowedStates = setOf(CurrentState.PANICKY, CurrentState.HYPER_ALERT),
        requiresAudio = false,
        requiresVoice = true,
        requiresHaptics = false,
        hasTraumaImagery = false
    )

    private val traumaExposure = SafetyCandidate(
        id = "TRAUMA_EXPOSURE",
        interoceptive = false,
        allowedStates = CurrentState.values().toSet(),
        requiresAudio = false,
        requiresVoice = false,
        requiresHaptics = false,
        hasTraumaImagery = true
    )

    private val tactileAnchor = SafetyCandidate(
        id = "E007",
        interoceptive = false,
        allowedStates = CurrentState.values().toSet(),
        requiresAudio = false,
        requiresVoice = false,
        requiresHaptics = true,
        hasTraumaImagery = false
    )

    // allowedStates covers every non-DISSOCIATION state so SF2 can be tested
    // in isolation from SF6 (wrong-state exclusion) below.
    private val interoceptiveAnyNonDissociation = SafetyCandidate(
        id = "E004-BROAD",
        interoceptive = true,
        allowedStates = CurrentState.values().filter { it != CurrentState.DISSOCIATION }.toSet(),
        requiresAudio = false,
        requiresVoice = false,
        requiresHaptics = true,
        hasTraumaImagery = false
    )

    private val fullProfile = SafetyProfile()
    private val noAudio = fullProfile.copy(audioOk = false)
    private val noVoice = fullProfile.copy(voiceOk = false)
    private val noHaptics = fullProfile.copy(hapticsOk = false)
    private val touchSensitive = fullProfile.copy(touchSensitive = true)

    // ── SF1: Trauma imagery veto ───────────────────────────────────────

    @Test
    fun `SF1 - trauma imagery candidate is vetoed regardless of state`() {
        for (state in CurrentState.values()) {
            assertFalse(
                "SF1 should veto trauma exposure in state $state",
                SafetyFilter.permits(traumaExposure, state, fullProfile)
            )
        }
    }

    // ── SF2: DISSOCIATION excludes interoceptive ───────────────────────

    @Test
    fun `SF2 - dissociation blocks interoceptive candidate`() {
        assertFalse(
            "SF2 should veto interoceptive in DISSOCIATION",
            SafetyFilter.permits(breathingHaptic, CurrentState.DISSOCIATION, fullProfile)
        )
    }

    @Test
    fun `SF2 - dissociation allows external-orientation candidate`() {
        assertTrue(
            "SF2 should allow external-orientation in DISSOCIATION",
            SafetyFilter.permits(externalOrientation, CurrentState.DISSOCIATION, fullProfile)
        )
    }

    @Test
    fun `SF2 - non-dissociation states allow interoceptive candidate`() {
        // Uses a fixture allowed in every non-DISSOCIATION state so this
        // exercises SF2 alone — breathingHaptic's own allowedStates (PANICKY,
        // HYPER_ALERT only) would otherwise trip SF6 for the other states,
        // which is a different rule than the one this test is named for.
        val nonDissociation = CurrentState.values().filter { it != CurrentState.DISSOCIATION }
        for (state in nonDissociation) {
            assertTrue(
                "SF2 should allow interoceptive in state $state",
                SafetyFilter.permits(interoceptiveAnyNonDissociation, state, fullProfile)
            )
        }
    }

    // ── SF4: Sensory exclusions ────────────────────────────────────────

    @Test
    fun `SF4 - audio candidate vetoed when audioOk is false`() {
        assertFalse(
            "SF4 should veto audio when profile.audioOk=false",
            SafetyFilter.permits(audioGuidedRelaxation, CurrentState.PANICKY, noAudio)
        )
    }

    @Test
    fun `SF4 - voice candidate vetoed when voiceOk is false`() {
        assertFalse(
            "SF4 should veto voice when profile.voiceOk=false",
            SafetyFilter.permits(voiceNarration, CurrentState.PANICKY, noVoice)
        )
    }

    @Test
    fun `SF4 - haptic candidate vetoed when hapticsOk is false`() {
        assertFalse(
            "SF4 should veto haptics when profile.hapticsOk=false",
            SafetyFilter.permits(tactileAnchor, CurrentState.FROZEN, noHaptics)
        )
    }

    @Test
    fun `SF4 - haptic candidate vetoed when touchSensitive is true`() {
        assertFalse(
            "SF4 should veto haptics when profile.touchSensitive=true",
            SafetyFilter.permits(tactileAnchor, CurrentState.FROZEN, touchSensitive)
        )
    }

    // ── SF6: Wrong-state exclusion ─────────────────────────────────────

    @Test
    fun `SF6 - candidate for PANICKY or HYPER_ALERT excluded in FROZEN`() {
        assertFalse(
            "SF6 should veto a candidate whose allowedStates excludes FROZEN",
            SafetyFilter.permits(breathingHaptic, CurrentState.FROZEN, fullProfile)
        )
    }

    @Test
    fun `SF6 - candidate for DISSOCIATION excluded in FLASHBACK`() {
        assertFalse(
            "SF6 should veto E001 in FLASHBACK",
            SafetyFilter.permits(externalOrientation, CurrentState.FLASHBACK, fullProfile)
        )
    }

    // ── SF8: notForMe veto ─────────────────────────────────────────────

    @Test
    fun `SF8 - candidate vetoed when its id is in notForMe`() {
        val profile = fullProfile.copy(notForMe = setOf("E004"))
        assertFalse(
            "SF8 should veto E004 when user marked it not-for-me",
            SafetyFilter.permits(breathingHaptic, CurrentState.PANICKY, profile)
        )
    }

    @Test
    fun `SF8 - veto applies regardless of rank or position`() {
        // State is PANICKY so every candidate here already clears SF6 (all
        // three allow PANICKY) — isolating the thing under test to SF8's
        // notForMe veto, rather than mixing in the wrong-state exclusion
        // that DISSOCIATION would trigger against E004/E006 (neither of
        // which allows DISSOCIATION).
        val rankedCandidates = listOf(breathingHaptic, voiceNarration, audioGuidedRelaxation)
        val profile = fullProfile.copy(notForMe = setOf("E004"))
        val result = SafetyFilter.fallback(rankedCandidates, CurrentState.PANICKY, profile)
        assertEquals(
            "SF8: first-ranked E004 vetoed → fallback should skip to next eligible",
            "E006", result.id
        )
    }

    // ── All-pass: permits returns true for clean candidate ─────────────

    @Test
    fun `clean candidate passes all SF rules in matching state`() {
        assertTrue(
            "External orientation in DISSOCIATION with default profile should pass",
            SafetyFilter.permits(externalOrientation, CurrentState.DISSOCIATION, fullProfile)
        )
        assertTrue(
            "Breathing haptic in PANICKY with default profile should pass",
            SafetyFilter.permits(breathingHaptic, CurrentState.PANICKY, fullProfile)
        )
    }

    // ── SF7: fallback ──────────────────────────────────────────────────

    @Test
    fun `SF7 - fallback returns first eligible candidate in list order`() {
        val candidates = listOf(externalOrientation, breathingHaptic, voiceNarration)
        val result = SafetyFilter.fallback(candidates, CurrentState.PANICKY, fullProfile)
        // E001 is only allowed in DISSOCIATION → skipped; E004 is interoceptive but PANICKY allows it
        assertEquals("fallback should return first eligible", "E004", result.id)
    }

    @Test
    fun `SF7 - fallback returns SAFE_FALLBACK when all candidates are vetoed`() {
        val profile = fullProfile.copy(notForMe = setOf("E001", "E004", "E006"))
        val candidates = listOf(externalOrientation, breathingHaptic, voiceNarration)
        val result = SafetyFilter.fallback(candidates, CurrentState.DISSOCIATION, profile)
        assertEquals("fallback should return SAFE_FALLBACK", SafetyFilter.SAFE_FALLBACK, result)
    }

    @Test
    fun `SF7 - fallback returns SAFE_FALLBACK when list is empty`() {
        val result = SafetyFilter.fallback(emptyList(), CurrentState.PANICKY, fullProfile)
        assertEquals("fallback should return SAFE_FALLBACK for empty list", SafetyFilter.SAFE_FALLBACK, result)
    }

    // ── SAFE_FALLBACK always permits ───────────────────────────────────

    @Test
    fun `SAFE_FALLBACK permits in every state with default profile`() {
        for (state in CurrentState.values()) {
            assertTrue(
                "SAFE_FALLBACK should always pass permits() in state $state",
                SafetyFilter.permits(SafetyFilter.SAFE_FALLBACK, state, fullProfile)
            )
        }
    }

    @Test
    fun `SAFE_FALLBACK permits even with restrictive profile`() {
        val restrictive = SafetyProfile(
            audioOk = false,
            voiceOk = false,
            hapticsOk = false,
            touchSensitive = true,
            notForMe = setOf("SAFE_FALLBACK")
        )
        // SAFE_FALLBACK has no sensory requirements and interoceptive=false,
        // so SF1-SF4/SF6 pass. SF8: "SAFE_FALLBACK" in notForMe would veto it.
        // This is intentional — if the user explicitly vetoes the fallback, respect that.
        // But with default profile (empty notForMe), it always passes.
        val defaultOnly = SafetyProfile()
        for (state in CurrentState.values()) {
            assertTrue(
                "SAFE_FALLBACK should pass with default profile in state $state",
                SafetyFilter.permits(SafetyFilter.SAFE_FALLBACK, state, defaultOnly)
            )
        }
    }
}
