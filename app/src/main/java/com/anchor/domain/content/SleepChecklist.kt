package com.anchor.domain.content

/**
 * Psychoeducation sleep-hygiene checklist.
 *
 * Items are drawn from Irish et al. 2015, SF Gov CBT-I handouts, and
 * Sleep Foundation guidance — all wording is original (see
 * `docs/content-sources.md`).
 *
 * This is **educational content only**, never a clinical treatment
 * recommendation. Displayed as reference material, not personalised advice.
 */
object SleepChecklist {

    /** Sleep hygiene items presented as psychoeducation. */
    val ITEMS: List<String> = listOf(
        "Go to bed and wake up at the same time every day, even on weekends.",
        "Keep your bedroom dark, cool, and quiet for better sleep quality.",
        "Avoid screens — phone, TV, laptop — for at least 30 minutes before bed.",
        "Stop caffeine by early afternoon; it can stay in your system for hours.",
        "If you can't fall asleep within about 20 minutes, get up and do something calm, then try again.",
        "Get bright sunlight exposure soon after waking to set your body clock.",
        "Exercise regularly during the day, but not too close to bedtime.",
        "Limit naps to 20–30 minutes and avoid napping late in the afternoon.",
        "Use your bed only for sleep and intimacy — keep work and screens elsewhere.",
        "Avoid heavy meals and alcohol in the hours before sleep."
    )
}
