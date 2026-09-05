package com.anchor.domain.safetyplan

/**
 * A local data model for the Stanley & Brown Safety Planning Intervention.
 *
 * This is plain Kotlin: no Android, no Compose, no network, no auto-dialing.
 * It only **holds** user-entered information for display — it does not act on it.
 *
 * The six sections correspond to the six steps of the safety planning
 * intervention protocol.
 */
data class SafetyPlan(
    /**
     * Step 1 — Warning signs.
     * Thoughts, feelings, behaviors, or situations that signal a crisis may be
     * developing (e.g. "I keep replaying the event", "I can't sleep").
     */
    val warningSigns: List<String>,

    /**
     * Step 2 — Internal coping strategies.
     * Things the person can do on their own to manage distress without
     * contacting another person (e.g. "Go for a walk", "Deep breathing").
     */
    val copingStrategies: List<String>,

    /**
     * Step 3 — Social distraction / people and places that provide distraction.
     * Contacts or environments that can take the person's mind off the crisis
     * without necessarily disclosing the distress (e.g. "Call my sister",
     * "Visit the coffee shop on Main St.").
     */
    val socialDistraction: List<String>,

    /**
     * Step 4 — Family and friends I can ask for help.
     * People the person can turn to when they need support. This is explicitly
     * about asking for help, not just distraction.
     */
    val helpContacts: List<String>,

    /**
     * Step 5 — Professionals and agencies to contact.
     * Clinicians, crisis lines, or agencies the person can reach out to.
     * Stores crisis numbers and professional contacts as plain user-entered
     * text (e.g. "988 Suicide & Crisis Lifeline", "Dr. Smith — 555-0123").
     */
    val professionals: List<String>,

    /**
     * Step 6 — Making my environment safe.
     * Steps to reduce access to means of self-harm (e.g. "Remove pills from
     * bathroom cabinet"). This section is optional — some users may not have
     * means to restrict — so an empty list is valid.
     */
    val meansRestriction: List<String>,
) {
    companion object {
        /** Maximum number of entries allowed in any single section. */
        const val MAX_ENTRIES_PER_SECTION = 10
    }
}
