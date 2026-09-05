package com.anchor.domain.profile

import com.anchor.domain.safety.SafetyProfile

/**
 * The user's sensory preferences, intervention vetoes, and contact
 * information. This is the single source of truth for personalisation
 * in the domain layer — UI and data layers map to/from this type.
 *
 * Mapped 1:1 to [SafetyProfile] by [toSafetyProfile] so that the
 * [SafetyFilter][com.anchor.domain.safety.SafetyFilter] remains a
 * pure function over data with no dependency on a full user profile
 * or Android layer.
 *
 * ## Design note
 *
 * There is deliberately **no trauma narrative field**. Anchor is not a
 * journaling or therapy-capture app — storing free-text trauma content
 * would introduce clinical obligations, storage risk, and cognitive load
 * that conflicts with the product philosophy (plan.md).
 *
 * ## Field semantics
 *
 * @property triggerSituations Set of user-identified trigger labels
 *   (e.g. crowds, loud noises). Kept as opaque strings — no
 *   Technique/Delivery enums exist yet at this stage of development.
 *
 * @property audioOk Whether audio-based interventions are acceptable.
 *   `true` by default; set `false` to exclude any intervention that
 *   produces sound.
 *
 * @property voiceOk Whether voice/narration interventions are acceptable.
 *   `true` by default; set `false` to exclude spoken-word content.
 *
 * @property hapticIntensity Intensity of haptic feedback on a linear
 *   0f–1f scale. Values outside this range are invalid per
 *   [UserProfileValidator]. A value of `0f` effectively disables haptics
 *   (mapped to `hapticsOk = false` in [SafetyProfile]).
 *
 * @property touchSensitive Whether the user's skin or touch sensitivity
 *   is heightened right now. When `true`, haptic interventions are
 *   excluded by [SafetyFilter][com.anchor.domain.safety.SafetyFilter]
 *   regardless of [hapticIntensity].
 *
 * @property reducedVisual Whether the user currently needs reduced
 *   visual stimulation (e.g. dark mode, minimal animations).
 *
 * @property notForMe Set of intervention identifiers (SF8 vetoes) that
 *   the user has explicitly excluded. The [SafetyFilter][com.anchor.domain.safety.SafetyFilter]
 *   checks candidate IDs against this set.
 *
 * @property trustedContactNumber Optional phone number of a person the
 *   user can reach out to discreetly. Must be non-blank when present
 *   and match the pattern `^\+?[0-9 ]{7,}$`.
 *
 * @property emergencyNumber Optional emergency services number. User-
 *   verified only — never auto-populated or assumed. Same validation
 *   rules as [trustedContactNumber].
 *
 * @property onboardingComplete Whether the user has finished the
 *   initial onboarding / personalisation flow.
 */
data class UserProfile(
    val triggerSituations: Set<String> = emptySet(),
    val audioOk: Boolean = true,
    val voiceOk: Boolean = true,
    val hapticIntensity: Float = 1.0f,
    val touchSensitive: Boolean = false,
    val reducedVisual: Boolean = false,
    val notForMe: Set<String> = emptySet(),
    val trustedContactNumber: String? = null,
    val emergencyNumber: String? = null,
    val onboardingComplete: Boolean = false
) {

    /**
     * Maps this [UserProfile] to a [SafetyProfile] snapshot for use by
     * the safety-filter pipeline.
     *
     * Mapping rules:
     * - [audioOk][UserProfile.audioOk] → [SafetyProfile.audioOk] (1:1)
     * - [voiceOk][UserProfile.voiceOk] → [SafetyProfile.voiceOk] (1:1)
     * - [touchSensitive][UserProfile.touchSensitive] → [SafetyProfile.touchSensitive] (1:1)
     * - [notForMe][UserProfile.notForMe] → [SafetyProfile.notForMe] (1:1)
     * - [hapticIntensity][UserProfile.hapticIntensity] → [SafetyProfile.hapticsOk]
     *   (`true` when intensity > `0f`)
     *
     * Fields that have no SafetyProfile counterpart ([triggerSituations],
     * [reducedVisual], [trustedContactNumber], [emergencyNumber],
     * [onboardingComplete]) are intentionally not carried over — they
     * are consumed by other subsystems.
     */
    fun toSafetyProfile(): SafetyProfile = SafetyProfile(
        audioOk = audioOk,
        voiceOk = voiceOk,
        hapticsOk = hapticIntensity > 0f,
        touchSensitive = touchSensitive,
        notForMe = notForMe
    )
}
