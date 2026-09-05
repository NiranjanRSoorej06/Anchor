package com.anchor.domain.content

import com.anchor.domain.safety.CurrentState
import com.anchor.domain.safety.SafetyFilter
import com.anchor.domain.safety.SafetyProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [InterventionCatalog] and [Intervention.toSafetyCandidate].
 *
 * Pure-Kotlin — no Android dependencies.
 */
class InterventionCatalogTest {

    private val defaultProfile = SafetyProfile()

    // ---- Structural invariants ------------------------------------------------

    @Test
    fun catalogContainsExactlyNineEntries() {
        assertEquals(9, InterventionCatalog.ALL.size)
    }

    @Test
    fun allIdsAreUniqueE001ThroughE009() {
        val ids = InterventionCatalog.ALL.map { it.id }
        assertEquals(ids.toSet().size, ids.size)
        assertEquals(
            listOf("E001", "E002", "E003", "E004", "E005", "E006", "E007", "E008", "E009"),
            ids
        )
    }

    // ---- E005 limitation string (verbatim) ------------------------------------

    @Test
    fun e005LimitationStringIsVerbatim() {
        val e005 = InterventionCatalog.ALL.first { it.id == "E005" }
        assertEquals(
            "The 2023 VA/DoD guideline states evidence is insufficient to recommend relaxation training as a standalone treatment.",
            e005.limitations
        )
    }

    // ---- E007 covers all CurrentState values -----------------------------------

    @Test
    fun e007AllowedStatesCoversAllCurrentStateValues() {
        val e007 = InterventionCatalog.ALL.first { it.id == "E007" }
        assertEquals(CurrentState.values().toSet(), e007.allowedStates)
    }

    // ---- E009 evidence status -------------------------------------------------

    @Test
    fun e009IsEvidenceGap() {
        val e009 = InterventionCatalog.ALL.first { it.id == "E009" }
        assertEquals(EvidenceStatus.EVIDENCE_GAP, e009.evidenceStatus)
    }

    // ---- toSafetyCandidate spot-checks ----------------------------------------

    @Test
    fun toSafetyCandidateMapsInteroceptiveCorrectly() {
        // E002 is non-interoceptive
        val e002 = InterventionCatalog.ALL.first { it.id == "E002" }
        assertFalse(e002.toSafetyCandidate().interoceptive)

        // E004 is interoceptive
        val e004 = InterventionCatalog.ALL.first { it.id == "E004" }
        assertTrue(e004.toSafetyCandidate().interoceptive)
    }

    @Test
    fun toSafetyCandidateMapsRequiresVoiceCorrectly() {
        // E002 does not require voice
        val e002 = InterventionCatalog.ALL.first { it.id == "E002" }
        assertFalse(e002.toSafetyCandidate().requiresVoice)

        // E003 requires voice
        val e003 = InterventionCatalog.ALL.first { it.id == "E003" }
        assertTrue(e003.toSafetyCandidate().requiresVoice)
    }

    @Test
    fun toSafetyCandidateMapsRequiresHapticsCorrectly() {
        // E002 does not require haptics
        val e002 = InterventionCatalog.ALL.first { it.id == "E002" }
        assertFalse(e002.toSafetyCandidate().requiresHaptics)

        // E004 requires haptics
        val e004 = InterventionCatalog.ALL.first { it.id == "E004" }
        assertTrue(e004.toSafetyCandidate().requiresHaptics)
    }

    // ---- hasTraumaImagery is always false (SF1) -------------------------------

    @Test
    fun noEntryHasTraumaImagery() {
        InterventionCatalog.ALL.forEach { intervention ->
            assertFalse(
                "${intervention.id} must not have trauma imagery",
                intervention.toSafetyCandidate().hasTraumaImagery
            )
        }
    }

    // ---- Every entry is permitted in at least one state ------------------------

    @Test
    fun everyEntryIsPermittedInAtLeastOneState() {
        InterventionCatalog.ALL.forEach { intervention ->
            val candidate = intervention.toSafetyCandidate()
            val permittedInSomeState = CurrentState.values().any { state ->
                SafetyFilter.permits(candidate, state, defaultProfile)
            }
            assertTrue(
                "${intervention.id} '${intervention.name}' must be permitted in at least one state with default profile",
                permittedInSomeState
            )
        }
    }
}
