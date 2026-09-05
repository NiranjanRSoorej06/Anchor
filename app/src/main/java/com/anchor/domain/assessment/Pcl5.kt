package com.anchor.domain.assessment

/**
 * PCL-5 (PTSD Checklist for DSM-5) assessment domain model.
 *
 * The PCL-5 is a **self-report screening instrument** for PTSD symptoms.
 * It is **not** a diagnostic tool and does not replace a clinical assessment.
 * A score at or above the provisional cutoff suggests further evaluation may
 * be warranted, but it does **not** constitute a diagnosis.
 *
 * Scoring: each of the 20 items is rated 0 (Not at all) through 4 (Extremely)
 * for the past month. The total score ranges from 0 to 80.
 *
 * Reference: Weathers, F.W., et al. (2013). The PTSD Checklist for DSM-5
 * (PCL-5). National Center for PTSD. https://www.ptsd.va.gov/
 */

/**
 * The four symptom clusters of the PCL-5, mirroring DSM-5 PTSD criteria.
 *
 * @property label Human-readable cluster name for display / logging.
 */
enum class Pcl5Cluster(val label: String) {
    /** Items 1-5: Intrusion symptoms. */
    INTRUSION("Intrusion"),

    /** Items 6-7: Avoidance. */
    AVOIDANCE("Avoidance"),

    /** Items 8-14: Negative alterations in cognitions and mood. */
    NEGATIVE_ALTERATIONS("Negative Alterations in Cognitions and Mood"),

    /** Items 15-20: Marked alterations in arousal and reactivity. */
    AROUSAL("Marked Alterations in Arousal and Reactivity")
}

/**
 * A single PCL-5 item.
 *
 * @property number The item number on the PCL-5 form (1-20).
 * @property cluster The symptom cluster this item belongs to.
 * @property text The item wording as it appears on the form.
 */
data class Pcl5Item(
    val number: Int,
    val cluster: Pcl5Cluster,
    val text: String
)

/**
 * Catalog of all 20 PCL-5 items.
 *
 * This is a plain-Kotlin object with no Android dependencies, suitable for
 * unit testing and sharing across platform layers.
 *
 * **Screening-only note:** The PCL-5 is a screening instrument, not a
 * diagnostic instrument. A high total score indicates that further
 * clinical evaluation is advisable; it does not confirm or deny PTSD.
 */
object Pcl5Catalog {

    /**
     * The provisional PTSD cutoff score from the PCL-5 manual.
     *
     * A total score of [CUTOFF] or above is considered a positive screen
     * for provisional PTSD. This value is widely used in research and
     * clinical practice but should always be interpreted alongside
     * clinical judgement.
     */
    const val CUTOFF = 31

    /**
     * The complete set of 20 PCL-5 items in form order.
     */
    val ITEMS: List<Pcl5Item> = listOf(
        // Cluster B: Intrusion (items 1-5)
        Pcl5Item(
            number = 1,
            cluster = Pcl5Cluster.INTRUSION,
            text = "Repeated, disturbing, and unwanted memories of the stressful experience"
        ),
        Pcl5Item(
            number = 2,
            cluster = Pcl5Cluster.INTRUSION,
            text = "Repeated, disturbing dreams"
        ),
        Pcl5Item(
            number = 3,
            cluster = Pcl5Cluster.INTRUSION,
            text = "Suddenly feeling or acting as if the stressful experience were actually happening again (flashbacks)"
        ),
        Pcl5Item(
            number = 4,
            cluster = Pcl5Cluster.INTRUSION,
            text = "Feeling very upset when something reminded you of the stressful experience"
        ),
        Pcl5Item(
            number = 5,
            cluster = Pcl5Cluster.INTRUSION,
            text = "Having strong physical reactions when something reminded you (heart pounding, trouble breathing, sweating)"
        ),

        // Cluster C: Avoidance (items 6-7)
        Pcl5Item(
            number = 6,
            cluster = Pcl5Cluster.AVOIDANCE,
            text = "Avoiding memories, thoughts, or feelings related to the stressful experience"
        ),
        Pcl5Item(
            number = 7,
            cluster = Pcl5Cluster.AVOIDANCE,
            text = "Avoiding external reminders (people, places, conversations, activities, objects, situations)"
        ),

        // Cluster D: Negative alterations in cognitions and mood (items 8-14)
        Pcl5Item(
            number = 8,
            cluster = Pcl5Cluster.NEGATIVE_ALTERATIONS,
            text = "Trouble remembering important parts"
        ),
        Pcl5Item(
            number = 9,
            cluster = Pcl5Cluster.NEGATIVE_ALTERATIONS,
            text = "Having strong negative beliefs about yourself, other people, or the world"
        ),
        Pcl5Item(
            number = 10,
            cluster = Pcl5Cluster.NEGATIVE_ALTERATIONS,
            text = "Blaming yourself or someone else for the stressful experience or what happened after it"
        ),
        Pcl5Item(
            number = 11,
            cluster = Pcl5Cluster.NEGATIVE_ALTERATIONS,
            text = "Having strong negative feelings such as fear, horror, anger, guilt, or shame"
        ),
        Pcl5Item(
            number = 12,
            cluster = Pcl5Cluster.NEGATIVE_ALTERATIONS,
            text = "Loss of interest in activities that you used to enjoy"
        ),
        Pcl5Item(
            number = 13,
            cluster = Pcl5Cluster.NEGATIVE_ALTERATIONS,
            text = "Feeling distant or cut off from other people"
        ),
        Pcl5Item(
            number = 14,
            cluster = Pcl5Cluster.NEGATIVE_ALTERATIONS,
            text = "Trouble experiencing positive feelings"
        ),

        // Cluster E: Marked alterations in arousal and reactivity (items 15-20)
        Pcl5Item(
            number = 15,
            cluster = Pcl5Cluster.AROUSAL,
            text = "Irritable behavior, angry outbursts, or acting aggressively"
        ),
        Pcl5Item(
            number = 16,
            cluster = Pcl5Cluster.AROUSAL,
            text = "Taking too many risks or doing things that could cause you harm"
        ),
        Pcl5Item(
            number = 17,
            cluster = Pcl5Cluster.AROUSAL,
            text = "Being superalert or watchful or on guard"
        ),
        Pcl5Item(
            number = 18,
            cluster = Pcl5Cluster.AROUSAL,
            text = "Feeling jumpy or easily startled"
        ),
        Pcl5Item(
            number = 19,
            cluster = Pcl5Cluster.AROUSAL,
            text = "Having difficulty concentrating"
        ),
        Pcl5Item(
            number = 20,
            cluster = Pcl5Cluster.AROUSAL,
            text = "Trouble falling or staying asleep"
        )
    )
}
