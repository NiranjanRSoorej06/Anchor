package com.anchor.domain.content

import com.anchor.domain.safety.CurrentState

/**
 * The complete set of nine pre-authored interventions (plan.md §8.3).
 *
 * Entries are ordered by evidence strength — the router should iterate
 * [ALL] in sequence when building a ranked candidate list. Each entry
 * maps to a [SafetyCandidate][com.anchor.domain.safety.SafetyCandidate]
 * via [Intervention.toSafetyCandidate].
 *
 * ## E009 "Sound cover (brown noise)"
 *
 * This entry has [EvidenceStatus.EVIDENCE_GAP]: there is **no support**
 * in evidence.md. It exists as comfort-only. **The router must never
 * rank E009 first** — this is a product-level constraint not enforced
 * by the safety filter. Future ranking logic should ensure E009 is
 * only offered as a secondary or tertiary option alongside clinically
 * grounded interventions.
 *
 * @see Intervention
 * @see com.anchor.domain.safety.SafetyFilter
 */
object InterventionCatalog {

    /** All nine catalog interventions in evidence-ranked order. */
    val ALL: List<Intervention> = listOf(
        Intervention(
            id = "E001",
            name = "Present-time external orientation",
            allowedStates = setOf(CurrentState.DISSOCIATION),
            interoceptive = false,
            evidenceStatus = EvidenceStatus.CLINICAL_CAUTION_DERIVED
        ),
        Intervention(
            id = "E002",
            name = "Sensory scan grounding",
            allowedStates = setOf(CurrentState.FLASHBACK, CurrentState.FROZEN),
            interoceptive = false,
            evidenceStatus = EvidenceStatus.RCT_COMPONENT
        ),
        Intervention(
            id = "E003",
            name = "Present-time statement + safety phrase",
            allowedStates = setOf(CurrentState.FLASHBACK),
            interoceptive = false,
            requiresVoice = true,
            evidenceStatus = EvidenceStatus.GUIDELINE_RECOMMENDED,
            limitations = "Delivery method (recorded voice) unevaluated"
        ),
        Intervention(
            id = "E004",
            name = "Paced breathing with haptic pacer",
            allowedStates = setOf(CurrentState.PANICKY, CurrentState.HYPER_ALERT),
            interoceptive = true,
            requiresHaptics = true,
            evidenceStatus = EvidenceStatus.GUIDELINE_RECOMMENDED,
            limitations = "Delivery method (haptic pacer) unevaluated"
        ),
        Intervention(
            id = "E005",
            name = "Brief applied relaxation step",
            allowedStates = setOf(CurrentState.HYPER_ALERT, CurrentState.PANICKY),
            interoceptive = true,
            evidenceStatus = EvidenceStatus.RCT_STUDIED_WITH_LIMITATIONS,
            limitations = "The 2023 VA/DoD guideline states evidence is insufficient to recommend relaxation training as a standalone treatment."
        ),
        Intervention(
            id = "E006",
            name = "Riding out the wave",
            allowedStates = setOf(CurrentState.PANICKY, CurrentState.HYPER_ALERT),
            interoceptive = false,
            evidenceStatus = EvidenceStatus.ADJUNCT_EVIDENCE
        ),
        Intervention(
            id = "E007",
            name = "Tactile anchor",
            allowedStates = CurrentState.values().toSet(),
            interoceptive = false,
            requiresHaptics = true,
            evidenceStatus = EvidenceStatus.RCT_COMPONENT,
            limitations = "Haptic delivery unevaluated"
        ),
        Intervention(
            id = "E008",
            name = "Orienting to the room",
            allowedStates = setOf(CurrentState.NOT_SURE, CurrentState.FROZEN, CurrentState.DISSOCIATION),
            interoceptive = false,
            evidenceStatus = EvidenceStatus.RCT_COMPONENT
        ),
        Intervention(
            id = "E009",
            name = "Sound cover (brown noise)",
            allowedStates = CurrentState.values().toSet(),
            interoceptive = false,
            evidenceStatus = EvidenceStatus.EVIDENCE_GAP,
            limitations = "No support in evidence.md; comfort only, never routed as a primary intervention"
        )
    )
}
