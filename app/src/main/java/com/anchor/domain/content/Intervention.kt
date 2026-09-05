package com.anchor.domain.content

import com.anchor.domain.safety.CurrentState
import com.anchor.domain.safety.SafetyCandidate

/**
 * Evidence classification for an [Intervention] entry in the catalog.
 *
 * Rendered verbatim in the UI (plan.md §8.3). The ordering is intentional:
 * guideline-level support is strongest, while [EVIDENCE_GAP] and
 * [PRODUCT_DECISION] represent the weakest or non-clinical categories.
 *
 * @see InterventionCatalog
 */
enum class EvidenceStatus {
    /** Supported by clinical guideline recommendations (e.g. NICE NG116). */
    GUIDELINE_RECOMMENDED,

    /** Component of a randomised controlled trial, but not studied in isolation. */
    RCT_COMPONENT,

    /** Studied in an RCT but with significant methodological limitations. */
    RCT_STUDIED_WITH_LIMITATIONS,

    /** Supported only as an adjunct / complementary technique. */
    ADJUNCT_EVIDENCE,

    /** Derived from clinical caution notes, not direct trial evidence. */
    CLINICAL_CAUTION_DERIVED,

    /** Guideline-recommended technique whose specific delivery method is unevaluated. */
    DELIVERY_METHOD_UNEVALUATED,

    /** No support in evidence.md; requires human review; never routed as a primary intervention. */
    EVIDENCE_GAP,

    /** Chosen for product / UX reasons rather than clinical evidence. */
    PRODUCT_DECISION
}

/**
 * A pre-authored intervention entry in the catalog (plan.md §8.3).
 *
 * Every field maps 1:1 to a safety-filter rule enforced by
 * [SafetyFilter][com.anchor.domain.safety.SafetyFilter]:
 *
 * - **[id]** — SF8: checked against [SafetyProfile.notForMe][com.anchor.domain.safety.SafetyProfile.notForMe].
 * - **[allowedStates]** — SF6: the set of [CurrentState] values this
 *   intervention may serve; any state not in this set is excluded.
 * - **[interoceptive]** — SF2: when `true`, vetoed during [CurrentState.DISSOCIATION].
 * - **[requiresAudio]** — SF4: excluded when audio is disabled.
 * - **[requiresVoice]** — SF4: excluded when voice is disabled.
 * - **[requiresHaptics]** — SF4: excluded when haptics are disabled or touch is sensitive.
 *
 * [evidenceStatus] and [limitations] are informational; they are not
 * enforced by the safety filter but drive UI rendering and future
 * ranking logic.
 *
 * @see InterventionCatalog
 * @see toSafetyCandidate
 */
data class Intervention(
    /** Unique catalog identifier (E001–E009). */
    val id: String,

    /** Human-readable name shown in the UI. */
    val name: String,

    /** The set of [CurrentState] values in which this intervention may be offered (SF6). */
    val allowedStates: Set<CurrentState>,

    /** Whether this is an inward-focus technique; vetoed during DISSOCIATION (SF2). */
    val interoceptive: Boolean,

    /** Requires audio output; excluded when [SafetyProfile.audioOk][com.anchor.domain.safety.SafetyProfile.audioOk] is false (SF4). */
    val requiresAudio: Boolean = false,

    /** Requires recorded / live voice; excluded when [SafetyProfile.voiceOk][com.anchor.domain.safety.SafetyProfile.voiceOk] is false (SF4). */
    val requiresVoice: Boolean = false,

    /** Requires haptic output; excluded when haptics are off or touch-sensitive (SF4). */
    val requiresHaptics: Boolean = false,

    /** Evidence classification for this intervention. */
    val evidenceStatus: EvidenceStatus,

    /** Optional limitations text displayed alongside the intervention in the UI. */
    val limitations: String? = null
)

/**
 * Maps this [Intervention] to a [SafetyCandidate] for the safety-filter
 * pipeline (plan.md §8.3 → [SafetyFilter][com.anchor.domain.safety.SafetyFilter]).
 *
 * All routing-relevant fields are carried across 1:1.
 * [SafetyCandidate.hasTraumaImagery][com.anchor.domain.safety.SafetyCandidate.hasTraumaImagery]
 * is **always** `false` because SF1 bans trauma-imagery content from
 * the catalog entirely — no catalog entry may contain exposure-style material.
 *
 * @return a [SafetyCandidate] suitable for [SafetyFilter.permits][com.anchor.domain.safety.SafetyFilter.permits].
 */
fun Intervention.toSafetyCandidate(): SafetyCandidate = SafetyCandidate(
    id = id,
    interoceptive = interoceptive,
    allowedStates = allowedStates,
    requiresAudio = requiresAudio,
    requiresVoice = requiresVoice,
    requiresHaptics = requiresHaptics,
    hasTraumaImagery = false
)
