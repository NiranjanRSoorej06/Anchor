package com.anchor.domain.content

/**
 * Psychoeducation about sleep and PTSD.
 *
 * Wording is original. Concepts are drawn from CBT-I patient education
 * (SF Gov CBT-I handout; Sleep Foundation) and the AASM/VA position that
 * CBT-I is the first-line treatment for chronic insomnia, including when it
 * co-occurs with PTSD — see `docs/content-sources.md` and
 * `docs/exercise-evidence.md`.
 *
 * This is **educational content only**. It is not a diagnosis, not a
 * treatment plan, and not a substitute for care from a clinician. Anchor's
 * sleep tool records a diary and shows the user their own numbers; it never
 * prescribes a sleep schedule (sleep-restriction therapy requires clinician
 * titration) and never delivers imagery rehearsal therapy for nightmares
 * (that is a multi-session clinical treatment — see
 * `docs/anchor_Symptom_Exercise_Evidence_table.md`).
 */
object SleepEducation {

    /** Short framing shown at the top of the "Learn" section. */
    const val INTRO: String =
        "Trouble sleeping is one of the most common parts of living with PTSD. " +
            "Keeping a simple sleep diary for a week or two helps you and a " +
            "clinician see the real pattern, rather than guessing."

    /** Brief points about what the evidence-based approach looks like. */
    val CBT_I_POINTS: List<String> = listOf(
        "The first-line treatment for ongoing insomnia is CBT-I — cognitive " +
            "behavioural therapy for insomnia. It works even when insomnia " +
            "comes alongside PTSD, and a trained therapist delivers it.",
        "Keeping regular wake-up times, getting out of bed when you can't " +
            "sleep, and reserving the bed for sleep are the core habits CBT-I " +
            "builds on.",
        "A wind-down routine and slow breathing before bed can make it easier " +
            "to fall asleep. They are supportive habits, not a cure.",
        "Sleep changes slowly. A few rough nights in a row are normal and " +
            "don't mean the approach isn't working."
    )

    /** Note shown when the user has been logging nightmares. */
    const val NIGHTMARE_NOTE: String =
        "If nightmares are a regular problem, there are treatments that target " +
            "them directly, such as imagery rehearsal therapy. These are " +
            "delivered by a clinician over several sessions — Anchor can't " +
            "provide them, but a professional can. See Get Support."

    /** Standard disclaimer, same tone as [SleepChecklist]. */
    const val DISCLAIMER: String =
        "Educational information only — not medical advice. Anchor does not " +
            "diagnose or treat sleep disorders."
}
