package com.anchor.domain.triage

import com.anchor.domain.safety.CurrentState

/**
 * What the user feels is happening right now — situation language only.
 *
 * Each value describes the user's *current experience*, never the cause,
 * diagnosis, or origin. These labels are selected by the user after
 * SOS + breathing + check-in, when they are ready to pick "what kind
 * of thing hit me" so the app can recommend starter interventions.
 *
 * This is NOT a clinical instrument. It is a deterministic lookup key
 * that feeds [IncidentTriage.starterIds] → [InterventionRouter][com.anchor.domain.content.InterventionCatalog].
 */
enum class IncidentKind {
    /** Heart racing, short breath, body feels like danger — the panic wave. */
    PANIC,

    /** A memory plays like a movie, as if it is happening again. */
    FLASHBACK,

    /** Waking from a bad dream, body still shaking. */
    NIGHTMARE,

    /** Feeling unreal, foggy, or far away from yourself. */
    DISSOCIATION,

    /** Sudden heat, tension, or urge to lash out. */
    ANGER_SPIKE,

    /** Strong pull to avoid a place, person, or activity. */
    AVOIDANCE_URGE,

    /** Lights, sounds, or textures feel painfully intense. */
    SENSORY_OVERLOAD,

    /** Heavy sadness, low energy, hard to care about anything. */
    LOW_MOOD,

    /** Cannot name it — something is wrong but the label is unclear. */
    NOT_SURE
}

/**
 * Deterministic mapping from [IncidentKind] to starter intervention
 * ids and a [CurrentState] for downstream routing.
 *
 * This object is a **pure function table** — no side effects, no
 * randomness, no Android imports. It feeds the intervention router:
 * given a user-reported incident kind, it produces the set of catalog
 * ids that should be offered first, and the [CurrentState] the safety
 * filter should evaluate against.
 *
 * This is NOT a diagnosis. It never implies cause, etiology, or
 * clinical category. It is a lookup table over user-selected
 * situation-language labels.
 */
object IncidentTriage {

    /**
     * Maps [kind] to the [CurrentState] that the safety filter should
     * evaluate starter interventions against.
     */
    fun toCurrentState(kind: IncidentKind): CurrentState = when (kind) {
        IncidentKind.PANIC           -> CurrentState.PANICKY
        IncidentKind.FLASHBACK       -> CurrentState.FLASHBACK
        IncidentKind.NIGHTMARE       -> CurrentState.FLASHBACK
        IncidentKind.DISSOCIATION    -> CurrentState.DISSOCIATION
        IncidentKind.ANGER_SPIKE     -> CurrentState.HYPER_ALERT
        IncidentKind.AVOIDANCE_URGE  -> CurrentState.NOT_SURE
        IncidentKind.SENSORY_OVERLOAD -> CurrentState.HYPER_ALERT
        IncidentKind.LOW_MOOD        -> CurrentState.NOT_SURE
        IncidentKind.NOT_SURE        -> CurrentState.NOT_SURE
    }

    /**
     * Returns the ordered list of starter intervention catalog ids
     * for [kind]. Every id maps to an entry in [InterventionCatalog.ALL][com.anchor.domain.content.InterventionCatalog.ALL].
     *
     * The list is deterministic and non-empty for every [IncidentKind].
     */
    fun starterIds(kind: IncidentKind): List<String> = when (kind) {
        IncidentKind.PANIC           -> listOf("E004", "E006", "E007")
        IncidentKind.FLASHBACK       -> listOf("E002", "E003", "E007")
        IncidentKind.NIGHTMARE       -> listOf("E002", "E008")
        IncidentKind.DISSOCIATION    -> listOf("E001", "E008", "E007")
        IncidentKind.ANGER_SPIKE     -> listOf("E004", "E005")
        IncidentKind.AVOIDANCE_URGE  -> listOf("E006", "E003")
        IncidentKind.SENSORY_OVERLOAD -> listOf("E008", "E007")
        IncidentKind.LOW_MOOD        -> listOf("E006", "E003")
        IncidentKind.NOT_SURE        -> listOf("E007", "E008")
    }
}
