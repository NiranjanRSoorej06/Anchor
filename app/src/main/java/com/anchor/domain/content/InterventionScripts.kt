package com.anchor.domain.content

/**
 * Spoken-script content for each [InterventionCatalog] entry, used by the
 * shared guided-exercise player. This is the "what do we actually say"
 * layer behind [InterventionRouter][com.anchor.domain.routing.InterventionRouter]'s
 * id-based ranking — every id the router can return resolves to a short,
 * own-wording script here, own tone and pacing matching the rest of
 * `domain/content` (own words, never copied from a clinical worksheet).
 *
 * Kept intentionally short (≈30s total per script) to fit the demo-
 * compressed exercise window; own wording, no diagnosis, no trauma
 * imagery — consistent with every other entry in this package.
 */
object InterventionScripts {

    private val scripts: Map<String, List<ScriptStep>> = mapOf(
        // E001 — Present-time external orientation (dissociation)
        "E001" to listOf(
            ScriptStep("Take a slow breath. Look around your space and name five things you can see.", 8),
            ScriptStep("Now touch and name four things you can feel around you.", 8),
            ScriptStep("Press both of your feet firmly down into the floor below you.", 7),
            ScriptStep("Feel the stability of the floor beneath you. You are here and safe.", 7),
        ),
        // E002 — Sensory scan grounding (flashback / frozen)
        "E002" to listOf(
            ScriptStep("Slowly scan the room. Let your eyes land on one object and study its color.", 8),
            ScriptStep("Notice its shape and texture. Just observe, without judging.", 8),
            ScriptStep("Listen for one sound nearby — let it come and go.", 7),
            ScriptStep("Bring your attention back to this room, right now.", 7),
        ),
        // E003 — Present-time statement + safety phrase (flashback)
        "E003" to listOf(
            ScriptStep("Look slowly around the room. Notice where you are right now.", 8),
            ScriptStep("Say aloud with me. I am here. I am safe.", 8),
            ScriptStep("That was then. This is now.", 7),
            ScriptStep("Exhale very slowly through your mouth. Feel this present moment.", 7),
        ),
        // E004 — Paced breathing with haptic pacer (panicky / hyper-alert)
        "E004" to listOf(
            ScriptStep("Inhale gently for four seconds. Exhale slowly for six seconds.", 10),
            ScriptStep("Second breath. Inhale softly. Exhale completely and let your shoulders drop.", 10),
            ScriptStep("Third breath. Inhale peace. Exhale all tension.", 10),
        ),
        // E005 — Brief applied relaxation (hyper-alert / panicky)
        "E005" to listOf(
            ScriptStep("Place one hand softly on your chest, and one on your abdomen.", 8),
            ScriptStep("Take three slow, soothing breaths into your hands.", 10),
            ScriptStep("Acknowledge the emotion you are feeling, without judgment.", 6),
            ScriptStep("You are safe to let this feeling pass.", 6),
        ),
        // E006 — Riding out the wave (panicky / hyper-alert)
        "E006" to listOf(
            ScriptStep("This feeling is a wave. It rises, it peaks, and it always falls again.", 8),
            ScriptStep("You don't have to fight it — just stay steady while it moves through you.", 8),
            ScriptStep("Notice it start to ease, even a little.", 7),
            ScriptStep("You're still here. The wave is passing.", 7),
        ),
        // E007 — Tactile anchor (any state)
        "E007" to listOf(
            ScriptStep("Gently wiggle your fingers and your toes.", 8),
            ScriptStep("Press your feet firmly down into the floor below.", 7),
            ScriptStep("Slowly roll your shoulders up, and down.", 7),
            ScriptStep("Feel movement returning gently to your body.", 8),
        ),
        // E008 — Orienting to the room (not sure / frozen / dissociation)
        "E008" to listOf(
            ScriptStep("Pause. Sit or stand somewhere that feels steady.", 8),
            ScriptStep("Press your feet firmly into the floor.", 7),
            ScriptStep("Say your name out loud, and where you are right now.", 8),
            ScriptStep("Identify today's date. You are grounded in this moment.", 7),
        ),
        // E009 — Sound cover / comfort only (evidence gap; rarely surfaces first)
        "E009" to listOf(
            ScriptStep("Let a steady, even sound settle around you for a moment.", 10),
            ScriptStep("You don't need to do anything else right now.", 10),
            ScriptStep("Just let this pass at its own pace.", 10),
        ),
    )

    /**
     * Returns the script for [interventionId], or a short generic fallback
     * (mirroring [com.anchor.domain.safety.SafetyFilter.SAFE_FALLBACK]'s
     * intent) when the id has no dedicated content — this keeps the player
     * non-empty for any id the router could ever return.
     */
    fun forId(interventionId: String): List<ScriptStep> =
        scripts[interventionId] ?: listOf(
            ScriptStep("Take a slow breath. Notice you are in a safe place, right now.", 10),
            ScriptStep("Let your shoulders soften and your breathing settle.", 10),
        )
}
