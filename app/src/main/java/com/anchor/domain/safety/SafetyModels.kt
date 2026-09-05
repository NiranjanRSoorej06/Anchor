package com.anchor.domain.safety

/**
 * The user's current affective state, reported at check-in time.
 *
 * Mirrors the catalog target states in [plan.md §8.3][plan.md] and the
 * intervention routing table. Every [SafetyCandidate] declares which of
 * these states it may serve; the filter uses membership to enforce SF6.
 *
 * @see SafetyFilter.permits
 */
enum class CurrentState {
    DISSOCIATION,
    FLASHBACK,
    FROZEN,
    PANICKY,
    HYPER_ALERT,
    NOT_SURE
}

/**
 * A candidate intervention drawn from the intervention catalog
 * (plan.md §8.3, nine pre-authored entries).
 *
 * Each field maps directly to an SF rule enforced by [SafetyFilter]:
 *
 * - **[id]** — SF8: checked against [SafetyProfile.notForMe]; also the
 *   unique identifier for every catalog entry (E001–E009).
 * - **[interoceptive]** — SF2: when `true` the candidate is an
 *   inward-focus technique (breath-focus, body scan, inward relaxation)
 *   and is vetoed during [CurrentState.DISSOCIATION].
 * - **[allowedStates]** — SF6: the set of [CurrentState] values this
 *   candidate may serve; any state not in this set is excluded.
 * - **[requiresAudio]** — SF4: hard-excluded when
 *   [SafetyProfile.audioOk] is `false`.
 * - **[requiresVoice]** — SF4: hard-excluded when
 *   [SafetyProfile.voiceOk] is `false`.
 * - **[requiresHaptics]** — SF4: hard-excluded when
 *   [SafetyProfile.hapticsOk] is `false` **or**
 *   [SafetyProfile.touchSensitive] is `true`.
 * - **[hasTraumaImagery]** — SF1: when `true` the candidate contains
 *   trauma imagery or exposure-style content and is **never** eligible
 *   in the acute path.
 */
data class SafetyCandidate(
    val id: String,
    val interoceptive: Boolean,
    val allowedStates: Set<CurrentState>,
    val requiresAudio: Boolean = false,
    val requiresVoice: Boolean = false,
    val requiresHaptics: Boolean = false,
    val hasTraumaImagery: Boolean = false
)

/**
 * A snapshot of the user's sensory preferences and intervention vetoes,
 * mapped 1:1 from the future `UserProfile` (plan.md lines 656–667):
 *
 * - **[audioOk]** ↔ `UserProfile.audioOk`
 * - **[voiceOk]** ↔ `UserProfile.voiceOk`
 * - **[hapticsOk]** ↔ derived from `UserProfile.hapticIntensity > 0`
 * - **[touchSensitive]** ↔ `UserProfile.touchSensitive`
 * - **[notForMe]** ↔ `UserProfile.notForMe` (set of intervention ids,
 *   plan.md line 663)
 *
 * This class exists so [SafetyFilter] is a pure function over data with
 * no dependency on a full `UserProfile` or Android layer.
 */
data class SafetyProfile(
    val audioOk: Boolean = true,
    val voiceOk: Boolean = true,
    val hapticsOk: Boolean = true,
    val touchSensitive: Boolean = false,
    val notForMe: Set<String> = emptySet()
)
