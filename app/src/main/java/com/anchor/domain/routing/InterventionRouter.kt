package com.anchor.domain.routing

import com.anchor.domain.personalization.PersonalizationScorer
import com.anchor.domain.personalization.SessionOutcome
import com.anchor.domain.safety.CurrentState
import com.anchor.domain.safety.SafetyCandidate
import com.anchor.domain.safety.SafetyFilter
import com.anchor.domain.safety.SafetyProfile

/**
 * Deterministic, pure-Kotlin ranking pipeline for intervention candidates.
 *
 * No Android imports, no I/O, no randomness — the entire function is a
 * pipeline of deterministic transforms over immutable data. Safe to call
 * from any thread and trivially testable.
 *
 * ## Pipeline (plan.md lines 358–365 + SF7)
 *
 * ```
 * candidates
 *   .filter { it.id !in alreadyTried }              // plan.md:364
 *   .filter { SafetyFilter.permits(it, state, profile) }  // plan.md:363
 *   .sortedWith(rankComparator)                      // plan.md:365 / §9
 *   .ifEmpty { listOf(SafetyFilter.fallback(candidates, state, profile)) }  // SF7
 * ```
 */
object InterventionRouter {

    /**
     * Rank [candidates] for the user's current [state] and [profile],
     * excluding previously attempted interventions and applying all safety
     * vetoes. The result is never empty — when every candidate is excluded,
     * [SafetyFilter.fallback] supplies [SafetyFilter.SAFE_FALLBACK] (SF7).
     *
     * @param candidates     the full catalog slice to rank.
     * @param state          the user's current affective state.
     * @param profile        the user's sensory preferences and vetoes.
     * @param outcomes       historical session outcomes for success-rate scoring.
     * @param alreadyTried   intervention ids the user already attempted in this
     *                       episode; excluded per plan.md line 364.
     * @return a non-empty, safety-filtered, personalization-ranked list of
     *         candidates.
     */
    fun rank(
        candidates: List<SafetyCandidate>,
        state: CurrentState,
        profile: SafetyProfile,
        outcomes: List<SessionOutcome>,
        alreadyTried: Set<String> = emptySet()
    ): List<SafetyCandidate> {
        // Stage 1 — Drop candidates already tried this episode (plan.md:364)
        val notTried = candidates.filter { it.id !in alreadyTried }

        // Stage 2 — Keep only safety-permitted survivors (plan.md:363, SF1–SF8)
        val permitted = notTried.filter { SafetyFilter.permits(it, state, profile) }

        // Stage 3 — Sort by personalization success rate descending (plan.md:365, §9).
        //           Null rates (no outcome data) sort last; input order is
        //           preserved on ties for stable, deterministic ranking.
        val (withRate, withoutRate) = permitted.partition {
            PersonalizationScorer.successRate(outcomes, it.id) != null
        }
        val sortedWithRate = withRate.sortedByDescending {
            PersonalizationScorer.successRate(outcomes, it.id)!!
        }
        val ranked = sortedWithRate + withoutRate

        // Stage 4 — Fallback guarantees non-empty result (SF7)
        return ranked.ifEmpty {
            listOf(SafetyFilter.fallback(candidates, state, profile))
        }
    }
}
