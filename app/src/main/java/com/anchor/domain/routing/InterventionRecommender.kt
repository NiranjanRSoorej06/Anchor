package com.anchor.domain.routing

import com.anchor.domain.content.Intervention
import com.anchor.domain.content.InterventionCatalog
import com.anchor.domain.content.toSafetyCandidate
import com.anchor.domain.personalization.SessionOutcome
import com.anchor.domain.safety.SafetyProfile
import com.anchor.domain.triage.IncidentKind
import com.anchor.domain.triage.IncidentTriage

/**
 * Pure-function entry point for intervention recommendation given an
 * [IncidentKind].
 *
 * Composes [IncidentTriage.starterIds] → [InterventionCatalog] lookup →
 * [InterventionRouter.rank] into a single callable:
 *
 * ```
 * starterIds(kind)           // 9 IncidentKinds → starter E-ids
 *   .map { catalog[it] }     // E-ids → Intervention objects
 *   .map { toSafetyCandidate } // → SafetyCandidate for the filter
 *   .let { rank(it, state, profile, outcomes) } // safety + personalization
 *   .map { catalogById[it.id] } // back to Intervention objects
 * ```
 *
 * Uses default profile and empty outcomes for the unparameterised form,
 * suitable for demo routing and initial recommendation before the user
 * has built up personalization data.
 *
 * **Never empty**: [InterventionRouter.rank] guarantees [SafetyFilter.SAFE_FALLBACK]
 * when all candidates are excluded.
 *
 * @param kind the user-reported incident kind from the triage picker.
 * @param profile the user's sensory profile; defaults to all-enabled.
 * @param outcomes historical session outcomes for reranking; defaults to empty.
 * @return a non-empty, safety-filtered, ranked list of [Intervention] objects.
 */
fun recommendInterventions(
    kind: IncidentKind,
    profile: SafetyProfile = SafetyProfile(),
    outcomes: List<SessionOutcome> = emptyList()
): List<Intervention> {
    val catalogById = InterventionCatalog.ALL.associateBy { it.id }

    // Step 1: get starter intervention ids for this incident kind
    val starterIds = IncidentTriage.starterIds(kind)

    // Step 2: map to catalog interventions, then to safety candidates
    val starters = starterIds.mapNotNull { catalogById[it] }
    val candidates = starters.map { it.toSafetyCandidate() }

    // Step 3: rank through the full safety + personalization pipeline
    val rankedCandidates = InterventionRouter.rank(
        candidates = candidates,
        state = IncidentTriage.toCurrentState(kind),
        profile = profile,
        outcomes = outcomes
    )

    // Step 4: map back to Intervention objects
    // SAFE_FALLBACK has id "SAFE_FALLBACK" which won't be in the catalog,
    // so it maps to null and is replaced by a synthetic fallback entry.
    return rankedCandidates.mapNotNull { candidate ->
        catalogById[candidate.id]
    }.ifEmpty {
        // Should never happen — InterventionRouter guarantees non-empty —
        // but defense in depth for callers.
        listOf(InterventionCatalog.ALL.first { it.id == "E007" })
    }
}
