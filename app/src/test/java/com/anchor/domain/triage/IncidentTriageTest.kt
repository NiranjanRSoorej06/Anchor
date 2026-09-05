package com.anchor.domain.triage

import com.anchor.domain.content.InterventionCatalog
import com.anchor.domain.safety.CurrentState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentTriageTest {

    private val catalogById = InterventionCatalog.ALL.associateBy { it.id }

    // ── Every kind produces a non-empty starter list ───────────────

    @Test fun panic_starterIds_notEmpty() =
        assertTrue(IncidentTriage.starterIds(IncidentKind.PANIC).isNotEmpty())

    @Test fun flashback_starterIds_notEmpty() =
        assertTrue(IncidentTriage.starterIds(IncidentKind.FLASHBACK).isNotEmpty())

    @Test fun nightmare_starterIds_notEmpty() =
        assertTrue(IncidentTriage.starterIds(IncidentKind.NIGHTMARE).isNotEmpty())

    @Test fun dissociation_starterIds_notEmpty() =
        assertTrue(IncidentTriage.starterIds(IncidentKind.DISSOCIATION).isNotEmpty())

    @Test fun angerSpike_starterIds_notEmpty() =
        assertTrue(IncidentTriage.starterIds(IncidentKind.ANGER_SPIKE).isNotEmpty())

    @Test fun avoidanceUrge_starterIds_notEmpty() =
        assertTrue(IncidentTriage.starterIds(IncidentKind.AVOIDANCE_URGE).isNotEmpty())

    @Test fun sensoryOverload_starterIds_notEmpty() =
        assertTrue(IncidentTriage.starterIds(IncidentKind.SENSORY_OVERLOAD).isNotEmpty())

    @Test fun lowMood_starterIds_notEmpty() =
        assertTrue(IncidentTriage.starterIds(IncidentKind.LOW_MOOD).isNotEmpty())

    @Test fun notSure_starterIds_notEmpty() =
        assertTrue(IncidentTriage.starterIds(IncidentKind.NOT_SURE).isNotEmpty())

    // ── Every starter id exists in InterventionCatalog.ALL ─────────

    @Test fun all_starterIds_existInCatalog() {
        for (kind in IncidentKind.values()) {
            for (id in IncidentTriage.starterIds(kind)) {
                assertTrue(
                    "$id from $kind not found in InterventionCatalog.ALL",
                    catalogById.containsKey(id)
                )
            }
        }
    }

    // ── DISSOCIATION starters are ALL non-interoceptive ───────────

    @Test fun dissociation_starters_areNonInteroceptive() {
        for (id in IncidentTriage.starterIds(IncidentKind.DISSOCIATION)) {
            val intervention = catalogById.getValue(id)
            assertFalse(
                "DISSOCIATION starter $id (${intervention.name}) must not be interoceptive",
                intervention.interoceptive
            )
        }
    }

    // ── E009 never appears in any starter list ────────────────────

    @Test fun e009_neverAppearsInAnyStarterList() {
        for (kind in IncidentKind.values()) {
            val ids = IncidentTriage.starterIds(kind)
            assertFalse(
                "E009 should not appear in starters for $kind",
                ids.contains("E009")
            )
        }
    }

    // ── No duplicates within any starter list ─────────────────────

    @Test fun starterIds_haveNoDuplicates() {
        for (kind in IncidentKind.values()) {
            val ids = IncidentTriage.starterIds(kind)
            assertEquals(
                "Duplicate ids in starters for $kind",
                ids.size,
                ids.toSet().size
            )
        }
    }

    // ── toCurrentState spot-checks ────────────────────────────────

    @Test fun toCurrentState_panic_mapsToPanicky() =
        assertEquals(CurrentState.PANICKY, IncidentTriage.toCurrentState(IncidentKind.PANIC))

    @Test fun toCurrentState_dissociation_mapsToDissociation() =
        assertEquals(CurrentState.DISSOCIATION, IncidentTriage.toCurrentState(IncidentKind.DISSOCIATION))

    @Test fun toCurrentState_notSure_mapsToNotSure() =
        assertEquals(CurrentState.NOT_SURE, IncidentTriage.toCurrentState(IncidentKind.NOT_SURE))

    @Test fun toCurrentState_flashback_mapsToFlashback() =
        assertEquals(CurrentState.FLASHBACK, IncidentTriage.toCurrentState(IncidentKind.FLASHBACK))

    @Test fun toCurrentState_nightmare_mapsToFlashback() =
        assertEquals(CurrentState.FLASHBACK, IncidentTriage.toCurrentState(IncidentKind.NIGHTMARE))

    @Test fun toCurrentState_angerSpike_mapsToHyperAlert() =
        assertEquals(CurrentState.HYPER_ALERT, IncidentTriage.toCurrentState(IncidentKind.ANGER_SPIKE))

    @Test fun toCurrentState_sensoryOverload_mapsToHyperAlert() =
        assertEquals(CurrentState.HYPER_ALERT, IncidentTriage.toCurrentState(IncidentKind.SENSORY_OVERLOAD))

    @Test fun toCurrentState_avoidanceUrge_mapsToNotSure() =
        assertEquals(CurrentState.NOT_SURE, IncidentTriage.toCurrentState(IncidentKind.AVOIDANCE_URGE))

    @Test fun toCurrentState_lowMood_mapsToNotSure() =
        assertEquals(CurrentState.NOT_SURE, IncidentTriage.toCurrentState(IncidentKind.LOW_MOOD))
}
