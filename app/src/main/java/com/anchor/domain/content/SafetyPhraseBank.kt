package com.anchor.domain.content

/**
 * Bundled safety phrases and guardrails for custom user-recorded phrases.
 *
 * This object owns the 24 default phrases sourced from clinical grounding
 * protocols (Najavits, Mood Juice NHS, VA guidance) and the validation
 * rules applied to any user-supplied custom phrase.
 *
 * ## Design notes
 *
 * - **Word limit:** each phrase is capped at [MAX_WORDS] (25) words to
 *   ensure short, low-cognitive-load audio playback.
 * - **Audio length (~15 s):** a UI-layer responsibility — the recording
 *   UI must enforce the ~15-second maximum duration and discard
 *   recordings that exceed it.
 * - **Narrative-content review:** phrases that contain trauma narrative
 *   clauses (e.g. "I was attacked but…") are a UI-layer review concern;
 *   this bank only validates word count and blankness.
 *
 * @see Intervention
 */
object SafetyPhraseBank {

    /** Maximum allowed word count for any safety phrase (bundled or custom). */
    const val MAX_WORDS: Int = 25

    /**
     * The 24 bundled safety phrases shipped with the application.
     *
     * Present-tense, ≤25 words each, no trauma narrative, no clinical claims.
     * Sourced from Najavits grounding, Mood Juice NHS, and VA guidance
     * (see `docs/content-sources.md`).
     *
     * Order is intentional — the UI may display them in this sequence.
     */
    val DEFAULTS: List<String> = listOf(
        "You are safe right now.",
        "This feeling will pass.",
        "You are here, in this moment.",
        "Your feet are on the ground.",
        "You have survived this before.",
        "You are not in danger right now.",
        "Breathe. You are in control.",
        "Name where you are and what day it is.",
        "This is a feeling, not a fact.",
        "You are allowed to take up space.",
        "You are doing the best you can.",
        "It's okay to feel what you're feeling.",
        "The worst part is over.",
        "You are stronger than you think.",
        "Right now, you are okay.",
        "You are not alone in this.",
        "This moment will not last forever.",
        "You deserve kindness, especially from yourself.",
        "You have gotten through hard things before.",
        "You are allowed to rest.",
        "Your body is here, and it is safe.",
        "One breath at a time.",
        "You are more than what happened to you.",
        "It takes courage to ask for help."
    )

    /**
     * Returns `true` when a custom user phrase passes the safety-bank
     * guardrails.
     *
     * Rules enforced:
     * 1. **Non-blank** — the trimmed phrase must contain at least one
     *    non-whitespace character.
     * 2. **Word count ≤ [MAX_WORDS]** — words are split on any whitespace
     *    (space, tab, newline); consecutive whitespace is collapsed.
     *
     * Rules **not** enforced here (UI-layer responsibilities):
     * - Audio-length guard (~15 s maximum recording duration).
     * - Narrative-content review (presence of trauma-narrative clauses).
     *
     * @param phrase the candidate phrase to validate.
     * @return `true` if the phrase is non-blank and ≤ [MAX_WORDS] words.
     */
    fun isAcceptable(phrase: String): Boolean {
        val trimmed = phrase.trim()
        if (trimmed.isEmpty()) return false
        val wordCount = trimmed.split(Regex("\\s+")).size
        return wordCount <= MAX_WORDS
    }
}
