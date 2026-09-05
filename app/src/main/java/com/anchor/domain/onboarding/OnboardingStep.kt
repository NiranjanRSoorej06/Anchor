package com.anchor.domain.onboarding

/**
 * The stage of the onboarding flow. Plain Kotlin enum — no Android, no Compose.
 *
 * Maps to the S0–S12 sequence from `docs/onboarding-research.md` §5:
 *
 * | Step        | Spec   | Description                                              |
 * |-------------|--------|----------------------------------------------------------|
 * | [WELCOME]   | S0     | Privacy-first welcome screen (5 s).                      |
 * | [CONSENT]   | S1     | Consent / data-usage explanation.                        |
 * | [PCL5]      | S2–S6  | PCL-5 intro + four cluster screens (grouped as one step).|
 * | [SITUATION] | S7     | Situation picker (multi-select chips).                   |
 * | [SENSORY]   | S8     | Sensory preference toggles + defaults.                   |
 * | [PHRASE]    | S9     | Safety phrase recording or bundled fallback.              |
 * | [CONTACT]   | S10    | Trusted contact (phone + template preview).              |
 * | [DONE]      | S12    | Done reveal + ANCHOR NOW payoff.                         |
 *
 * **S11 (safety-plan intro) is deliberately absent** — it defers to Settings
 * for the demo and has no step in this machine.
 *
 * Linear order = enum declaration order. UI-level jumps (e.g. CONSENT → S7)
 * are a UI-layer concern; the state machine always walks forward one step
 * at a time.
 */
enum class OnboardingStep {

    /** S0 — Privacy-first welcome screen (≈5 s). */
    WELCOME,

    /** S1 — Consent / data-usage explanation. "Skip setup" jumps to S7 at the UI layer. */
    CONSENT,

    /** S2–S6 — PCL-5 intro + four cluster screens (treated as one logical step). */
    PCL5,

    /** S7 — Situation picker (multi-select chips). */
    SITUATION,

    /** S8 — Sensory preference toggles with sensible defaults. */
    SENSORY,

    /** S9 — Safety phrase: record your own or accept a bundled fallback. */
    PHRASE,

    /** S10 — Trusted contact: phone number + message template preview. Skippable. */
    CONTACT,

    /** S12 — Done reveal + ANCHOR NOW payoff. Terminal state. */
    DONE
}
