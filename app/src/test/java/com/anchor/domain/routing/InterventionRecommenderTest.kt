package com.anchor.domain.routing

import com.anchor.domain.content.InterventionCatalog
import com.anchor.domain.triage.IncidentKind
import com.anchor.domain.triage.IncidentTriage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterventionRecommenderTest {

    private val catalogById = InterventionCatalog.ALL.associateBy { it.id }

    // ── Result is never empty for all 9 kinds ───────────────────────

    @Test
    fun panic_recommendInterventions_notEmpty() {
        val result = recommendInterventions(IncidentKind.PANIC)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun flashback_recommendInterventions_notEmpty() {
        val result = recommendInterventions(IncidentKind.FLASHBACK)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun nightmare_recommendInterventions_notEmpty() {
        val result = recommendInterventions(IncidentKind.NIGHTMARE)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun dissociation_recommendInterventions_notEmpty() {
        val result = recommendInterventions(IncidentKind.DISSOCIATION)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun angerSpike_recommendInterventions_notEmpty() {
        val result = recommendInterventions(IncidentKind.ANGER_SPIKE)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun avoidanceUrge_recommendInterventions_notEmpty() {
        val result = recommendInterventions(IncidentKind.AVOIDANCE_URGE)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun sensoryOverload_recommendInterventions_notEmpty() {
        val result = recommendInterventions(IncidentKind.SENSORY_OVERLOAD)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun lowMood_recommendInterventions_notEmpty() {
        val result = recommendInterventions(IncidentKind.LOW_MOOD)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun notSure_recommendInterventions_notEmpty() {
        val result = recommendInterventions(IncidentKind.NOT_SURE)
        assertTrue(result.isNotEmpty())
    }

    // ── All returned ids are valid catalog entries ───────────────────

    @Test
    fun allReturnedIdsExistInCatalog() {
        for (kind in IncidentKind.values()) {
            val result = recommendInterventions(kind)
            for (intervention in result) {
                assertTrue(
                    "${intervention.id} from $kind not in InterventionCatalog.ALL",
                    catalogById.containsKey(intervention.id)
                )
            }
        }
    }

    // ── Results only contain starter ids + SAFE_FALLBACK fallback ───

    @Test
    fun resultsAreSubsetOfStarterIdsOrFallback() {
        for (kind in IncidentKind.values()) {
            val starterIds = IncidentTriage.starterIds(kind).toSet()
            val result = recommendInterventions(kind)
            for (intervention in result) {
                assertTrue(
                    "${intervention.id} from $kind is not a starter for that kind",
                    starterIds.contains(intervention.id)
                )
            }
        }
    }

    // ── No duplicates in any result ─────────────────────────────────

    @Test
    fun noDuplicatesInAnyResult() {
        for (kind in IncidentKind.values()) {
            val result = recommendInterventions(kind)
            val ids = result.map { it.id }
            assertEquals(
                "Duplicate ids in result for $kind",
                ids.size,
                ids.toSet().size
            )
        }
    }

    // ── DISSOCIATION results are never interoceptive ────────────────

    @Test
    fun dissociation_resultsAreNeverInteroceptive() {
        val result = recommendInterventions(IncidentKind.DISSOCIATION)
        for (intervention in result) {
            assertFalse(
                "DISSOCIATION result ${intervention.id} must not be interoceptive",
                intervention.interoceptive
            )
        }
    }

    // ── No starter for any kind contains E009 ───────────────────────

    @Test
    fun e009NeverAppearsInResult() {
        for (kind in IncidentKind.values()) {
            val result = recommendInterventions(kind)
            assertFalse(
                "E009 should not appear in result for $kind",
                result.any { it.id == "E009" }
            )
        }
    }

    // ── Default profile: every kind returns result with default profile ─

    @Test
    fun defaultProfile_producesResultsForAllKinds() {
        for (kind in IncidentKind.values()) {
            val result = recommendInterventions(kind)
            assertTrue(
                "Default profile must produce results for $kind",
                result.isNotEmpty()
            )
        }
    }

    // ── Spot-check: each kind returns correct state mapping ──────────

    @Test
    fun panic_resultContainsPanickyStateInterventions() {
        val result = recommendInterventions(IncidentKind.PANIC)
        // E004 (paced breathing) is in PANIC starters and allows PANICKY
        assertTrue(
            "PANIC result should contain E004 (paced breathing)",
            result.any { it.id == "E004" }
        )
    }

    @Test
    fun dissociation_resultContainsExternalOrientation() {
        val result = recommendInterventions(IncidentKind.DISSOCIATION)
        // E001 (present-time external orientation) is in DISSOCIATION starters
        assertTrue(
            "DISSOCIATION result should contain E001 (external orientation)",
            result.any { it.id == "E001" }
        )
    }
}
