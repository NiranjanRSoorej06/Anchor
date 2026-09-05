package com.anchor.domain.safety

/**
 * Pure-function safety gate for the intervention routing pipeline.
 *
 * Placement in the pipeline (plan.md lines 300–315):
 *
 * ```
 * UserProfile + CurrentState + ResponseHistory
 *         ↓
 * SafetyFilter        ── hard vetoes, absolute precedence
 *         ↓
 * InterventionCatalog ── pre-authored, evidence-tagged
 *         ↓
 * Eligible set        ── allowedStates ∩ preferences ∩ not-already-tried
 *         ↓
 * Ranking             ── personalization score (§9)
 * ```
 *
 * ## Out-of-scope rules (by design)
 *
 * - **SF3 (WORSE → safety screen):** Owned by
 *   [SessionStateMachine.submitCheckIn][com.anchor.domain.session.SessionStateMachine].
 *   That method routes `WORSE` to `SAFETY_STOP` — an inert state with no
 *   automatic behavior. SafetyFilter does not and must not duplicate that
 *   routing logic.
 * - **SF5 (no auto-contact):** A UI / action-layer rule. Emergency
 *   services are never contacted automatically; explicit user action is
 *   required. This is enforced at the call-site level, not here.
 *
 * Every rule that *is* implemented is documented with its SF number.
 * No Android imports — the most test-worthy code in the project.
 */
object SafetyFilter {

    /**
     * The last-resort candidate returned by [fallback] when every
     * catalog entry is vetoed or the list is empty (SF7).
     *
     * Interprets as: "The user is safe; settle into the present moment."
     * - `id = "SAFE_FALLBACK"` — the only guaranteed constant id.
     * - `interoceptive = false` — safe during dissociation (SF2).
     * - `allowedStates = all` — permitted in every state (SF6).
     * - No sensory requirements — works for every profile (SF4).
     * - `hasTraumaImagery = false` — never vetoed by SF1.
     */
    val SAFE_FALLBACK = SafetyCandidate(
        id = "SAFE_FALLBACK",
        interoceptive = false,
        allowedStates = CurrentState.values().toSet(),
        requiresAudio = false,
        requiresVoice = false,
        requiresHaptics = false,
        hasTraumaImagery = false
    )

    /**
     * Returns `true` if [candidate] is permitted for the given [state]
     * and [profile]. All SF rules are evaluated as hard vetoes in a
     * short-circuit chain — the first failing rule returns `false`.
     *
     * @param candidate the catalog entry to evaluate.
     * @param state     the user's current affective state.
     * @param profile   the user's sensory preferences and vetoes.
     * @return `true` if the candidate survives every applicable filter rule.
     */
    fun permits(candidate: SafetyCandidate, state: CurrentState, profile: SafetyProfile): Boolean {
        // SF1 — Trauma imagery / exposure-style content is never eligible
        //        in the acute path.
        if (candidate.hasTraumaImagery) return false

        // SF2 — DISSOCIATION excludes interoceptive / inward-focus
        //        interventions; only external-orientation candidates remain.
        if (state == CurrentState.DISSOCIATION && candidate.interoceptive) return false

        // SF4 — Sensory preferences are hard exclusions.
        if (candidate.requiresAudio && !profile.audioOk) return false
        if (candidate.requiresVoice && !profile.voiceOk) return false
        if (candidate.requiresHaptics && (!profile.hapticsOk || profile.touchSensitive)) return false

        // SF6 — An intervention whose allowedStates omits the current
        //        state is never eligible.
        if (state !in candidate.allowedStates) return false

        // SF8 — An intervention the user marked "not for me" is never
        //        offered, at any rank.
        if (candidate.id in profile.notForMe) return false

        return true
    }

    /**
     * Returns the first [SafetyCandidate] in [candidates] that passes
     * [permits] (SF7), preserving list order so the catalog's
     * evidence-ranked sequence is respected.
     *
     * If **no** candidate passes — or [candidates] is empty —
     * [SAFE_FALLBACK] is returned. This guarantees the UI never renders
     * an empty screen.
     *
     * @param candidates the ranked catalog slice to search.
     * @param state      the user's current affective state.
     * @param profile    the user's sensory preferences and vetoes.
     * @return a non-null, non-empty candidate — always.
     */
    fun fallback(
        candidates: List<SafetyCandidate>,
        state: CurrentState,
        profile: SafetyProfile
    ): SafetyCandidate =
        candidates.firstOrNull { permits(it, state, profile) } ?: SAFE_FALLBACK
}
