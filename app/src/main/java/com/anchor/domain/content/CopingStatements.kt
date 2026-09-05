package com.anchor.domain.content

/**
 * Pre-authored coping statements organised by CPT/SIT category.
 *
 * Phrases are present-tense, trauma-narrative-free, and kept short
 * (≤ 140 characters) for quick display during acute distress.
 * Adapted from Beck/CPT/SIT clinical frameworks and Mood Juice NHS
 * guidance — all wording is original (see `docs/content-sources.md`).
 *
 * - **[SAFETY]** — grounding statements anchored in present-moment safety.
 * - **[TEMPORAL]** — "this will pass" statements reinforcing impermanence.
 * - **[CAPABILITY]** — "survived before" statements recalling past resilience.
 */
object CopingStatements {

    /** Present-tense safety affirmations grounded in the here-and-now. */
    val SAFETY: List<String> = listOf(
        "You are safe right now.",
        "You are here, in this moment.",
        "Your feet are on the ground.",
        "You are not in danger right now.",
        "Your body is here, and it is safe.",
        "This is a feeling, not a fact.",
        "You are allowed to take up space."
    )

    /** Temporal statements reinforcing that distress is impermanent. */
    val TEMPORAL: List<String> = listOf(
        "This feeling will pass.",
        "This moment will not last forever.",
        "The worst part is over.",
        "Right now, you are okay.",
        "One breath at a time.",
        "You are doing the best you can."
    )

    /** Capability statements recalling past survival and inner strength. */
    val CAPABILITY: List<String> = listOf(
        "You have survived this before.",
        "You are stronger than you think.",
        "You have gotten through hard things before.",
        "You are not alone in this.",
        "It takes courage to ask for help.",
        "You are more than what happened to you."
    )

    /** Normalize statements explaining that trauma reactions are expected. */
    val NORMALIZE: List<String> = listOf(
        "Stress reactions after frightening events are normal.",
        "It's normal to feel on guard long after a stressful event.",
        "Feeling numb or disconnected is a common response to trauma.",
        "Difficulty sleeping after a stressful event is expected.",
        "It's normal for your body to stay on alert after danger.",
        "Having trouble concentrating after trauma is very common."
    )

    /** Combined list of all coping statements across all categories. */
    val ALL: List<String> = SAFETY + TEMPORAL + CAPABILITY + NORMALIZE
}
