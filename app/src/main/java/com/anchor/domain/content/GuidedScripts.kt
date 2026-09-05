package com.anchor.domain.content

/**
 * A single timed step within a [GuidedScript].
 *
 * Each step carries the prompt text shown to the user and the duration
 * in seconds the step should remain active.  The UI layer reads
 * [durationSec] to drive a timer; the text is displayed as-is.
 *
 * @property text the user-facing prompt (non-blank by construction).
 * @property durationSec how long this step lasts, in the range 1–120.
 * @see GuidedScript
 */
data class ScriptStep(
    val text: String,
    val durationSec: Int
)

/**
 * A multi-step guided exercise the user can play through sequentially.
 *
 * Every script lives in [GuidedScripts.ALL] and is looked up by its
 * [id].  Scripts are pure data — no Android or Compose dependencies.
 *
 * Design constraints enforced by construction (and verified in
 * [GuidedScriptsTest]):
 * - [id] is unique across all scripts.
 * - [steps] is non-empty.
 * - Every step's text is non-blank.
 * - Every step's durationSec is in 1..120.
 * - The total duration ([totalDurationSec]) is ≤ 300 s.
 *
 * @property id a stable, lowercase-underscore identifier (e.g. `"breathing_box"`).
 * @property title a short, human-readable title shown in the UI.
 * @property steps the ordered list of [ScriptStep] entries.
 * @see GuidedScripts
 */
data class GuidedScript(
    val id: String,
    val title: String,
    val steps: List<ScriptStep>
) {
    /**
     * Returns the total duration of this script in seconds.
     *
     * Computed as the sum of every [ScriptStep.durationSec].
     *
     * @return total duration in seconds, guaranteed ≤ 300.
     */
    fun totalDurationSec(): Int = steps.sumOf { it.durationSec }
}

/**
 * Registry of all bundled guided scripts.
 *
 * Scripts cover grounding, breathing, and progressive muscle-relaxation
 * techniques.  Ratios and groupings follow public-domain clinical
 * protocols; all wording is original (see `docs/content-sources.md`).
 *
 * Lookup: [byId] returns `null` for unknown ids.
 */
object GuidedScripts {

    // ── Private script definitions ─────────────────────────────────────

    private val ScriptGrounding54321 = GuidedScript(
        id = "grounding_54321",
        title = "5-4-3-2-1 sensory scan",
        steps = listOf(
            ScriptStep("Look around and notice five things you can see. Name them one by one.", 15),
            ScriptStep("Look around and spot another four things you can see. Take your time.", 15),
            ScriptStep("Four more sights — let your eyes settle on each one for a moment.", 15),
            ScriptStep("Four things you see. Just observe, without judging.", 15),
            ScriptStep("Last four — let your gaze wander slowly and calmly.", 15),
            ScriptStep("Now touch four things near you. Feel the texture under your fingers.", 15),
            ScriptStep("Move your hand to something else — four surfaces to notice.", 15),
            ScriptStep("Four touches. You might feel fabric, wood, or cool air on your skin.", 15),
            ScriptStep("Skip any sense that feels wrong right now. Four things you can feel.", 15),
            ScriptStep("Listen for three sounds around you. They can be quiet or loud.", 15),
            ScriptStep("Three more sounds — hum of a device, traffic outside, your own breath.", 15),
            ScriptStep("Three sounds you hear right now. Let them come and go.", 15),
            ScriptStep("Notice two smells in the air. If you can't find any, that's fine too.", 15),
            ScriptStep("Two smells — or simply the clean air in front of you.", 15),
            ScriptStep("One taste. Take a slow breath through your mouth and notice what is there.", 15)
        )
    )

    private val ScriptGrounding3Step = GuidedScript(
        id = "grounding_3step",
        title = "3-step reset",
        steps = listOf(
            ScriptStep("Place both feet flat on the ground. Press down gently and feel the solid surface beneath you.", 20),
            ScriptStep("Look around and say your name for this place — room name or address — and today's date. Out loud or silently.", 20),
            ScriptStep("Take one slow, steady breath in through your nose, then let it all go through your mouth.", 20)
        )
    )

    private val ScriptBreathingBox = GuidedScript(
        id = "breathing_box",
        title = "Box breathing",
        steps = listOf(
            // Cycle 1
            ScriptStep("Inhale slowly through your nose for a count of four.", 4),
            ScriptStep("Hold your breath gently for four.", 4),
            ScriptStep("Exhale slowly through your mouth for a count of four.", 4),
            ScriptStep("Hold with lungs empty for four.", 4),
            // Cycle 2
            ScriptStep("Inhale again — one, two, three, four.", 4),
            ScriptStep("Hold — two, three, four.", 4),
            ScriptStep("Exhale — two, three, four.", 4),
            ScriptStep("Hold — two, three, four.", 4),
            // Cycle 3
            ScriptStep("Breathe in for four.", 4),
            ScriptStep("Hold for four.", 4),
            ScriptStep("Breathe out for four.", 4),
            ScriptStep("Hold for four.", 4),
            // Cycle 4
            ScriptStep("Last cycle — inhale, four counts.", 4),
            ScriptStep("Hold, four counts.", 4),
            ScriptStep("Exhale, four counts.", 4),
            ScriptStep("Hold empty, four counts. Well done.", 4)
        )
    )

    private val ScriptBreathingCoherent = GuidedScript(
        id = "breathing_coherent",
        title = "Slow steady breathing",
        steps = listOf(
            // Cycle 1
            ScriptStep("Breathe in slowly for a count of five.", 5),
            ScriptStep("Breathe out slowly for a count of five.", 5),
            // Cycle 2
            ScriptStep("In — two, three, four, five.", 5),
            ScriptStep("Out — two, three, four, five.", 5),
            // Cycle 3
            ScriptStep("In — let your belly rise as you fill up.", 5),
            ScriptStep("Out — let your belly fall as you empty.", 5),
            // Cycle 4
            ScriptStep("In — steady and unhurried.", 5),
            ScriptStep("Out — just as slow as the inhale.", 5),
            // Cycle 5
            ScriptStep("In — five counts, smooth and even.", 5),
            ScriptStep("Out — five counts, no rush.", 5),
            // Cycle 6
            ScriptStep("In — final round, five counts.", 5),
            ScriptStep("Out — five counts. You're doing well.", 5)
        )
    )

    private val ScriptBreathingExhale = GuidedScript(
        id = "breathing_exhale",
        title = "Long exhale",
        steps = listOf(
            ScriptStep("If holding feels bad, skip the pauses. Inhale for a count of four.", 4),
            ScriptStep("Exhale slowly for a count of eight — let the air slide out.", 8),
            ScriptStep("Inhale, four counts.", 4),
            ScriptStep("Exhale, eight counts — feel your shoulders drop.", 8),
            ScriptStep("In for four.", 4),
            ScriptStep("Out for eight — long and smooth.", 8),
            ScriptStep("In for four — last round.", 4),
            ScriptStep("Out for eight — you're almost there. Well done.", 8)
        )
    )

    private val ScriptPmrFull = GuidedScript(
        id = "pmr_full",
        title = "Muscle release",
        steps = listOf(
            // Feet
            ScriptStep("Curl your toes tightly and tense the arches of your feet. Hold for five seconds.", 5),
            ScriptStep("Release your feet. Notice the difference between tension and ease.", 10),
            // Calves
            ScriptStep("Point your toes toward your shins to tighten your calves. Hold for five seconds.", 5),
            ScriptStep("Let your calves go soft. Feel the warmth of release.", 10),
            // Thighs
            ScriptStep("Press your knees together and squeeze your thigh muscles. Hold for five seconds.", 5),
            ScriptStep("Relax your thighs and let your legs settle wherever they are comfortable.", 10),
            // Buttocks
            ScriptStep("Squeeze your buttocks firmly. Hold for five seconds.", 5),
            ScriptStep("Release and feel the tension drain away.", 10),
            // Abdomen
            ScriptStep("Tighten your stomach muscles — pull your navel in toward your spine. Hold for five seconds.", 5),
            ScriptStep("Let your belly soften completely. Breathe naturally.", 10),
            // Chest
            ScriptStep("Take a deep breath in and hold it to tighten your chest. Hold for five seconds.", 5),
            ScriptStep("Exhale and let your chest relax. Let your breathing return to normal.", 10),
            // Hands
            ScriptStep("Clench both fists tightly. Hold for five seconds.", 5),
            ScriptStep("Open your hands and let your fingers uncurl. Notice the tingling.", 10),
            // Forearms
            ScriptStep("Press your palms down on a surface or straighten your wrists to tighten your forearms. Hold for five seconds.", 5),
            ScriptStep("Relax your forearms. Feel them become heavy and loose.", 10),
            // Biceps
            ScriptStep("Make a fist and curl your arms to flex your biceps. Hold for five seconds.", 5),
            ScriptStep("Lower your arms and let your biceps go slack.", 10),
            // Shoulders
            ScriptStep("Lift both shoulders up toward your ears and hold the tension. Hold for five seconds.", 5),
            ScriptStep("Drop your shoulders down and away from your ears. Feel the release.", 10),
            // Neck
            ScriptStep("Gently tilt your head back or press your head against a support to tense your neck. Hold for five seconds.", 5),
            ScriptStep("Return your head to a neutral position and let your neck muscles soften.", 10),
            // Jaw
            ScriptStep("Clench your jaw and press your teeth together. Hold for five seconds.", 5),
            ScriptStep("Let your jaw drop slightly open. Allow your tongue to rest loosely.", 10),
            // Eyes
            ScriptStep("Squeeze your eyes shut and tighten the muscles around them. Hold for five seconds.", 5),
            ScriptStep("Open your eyes gently and let them relax. Notice how calm feels.", 10),
            // Forehead
            ScriptStep("Raise your eyebrows as high as you can to tighten your forehead. Hold for five seconds.", 5),
            ScriptStep("Let your forehead smooth out. You're done — notice how your whole body feels now.", 10)
        )
    )

    private val ScriptVisualizationMonsoon = GuidedScript(
        id = "visualization_monsoon",
        title = "Monsoon garden",
        steps = listOf(
            ScriptStep("It's normal to notice thoughts drifting or feelings of tension. You can let them come and go.", 15),
            ScriptStep("Picture yourself standing at the edge of an open garden. A soft monsoon rain begins to fall, cool and steady.", 20),
            ScriptStep("See the green leaves trembling under gentle raindrops. Droplets catch the light like tiny mirrors.", 20),
            ScriptStep("Listen to the steady patter of rain on leaves and earth. Each drop a soft, even rhythm.", 20),
            ScriptStep("Breathe in the scent of wet soil and fresh green growth. If scent feels wrong, skip it.", 20),
            ScriptStep("Feel the cool mist settling lightly on your skin. A gentle freshness, neither warm nor cold.", 20),
            ScriptStep("Let the soft breeze brush your face. It carries a quiet, constant coolness.", 20),
            ScriptStep("Stay here as long as you like. You can wander the garden or simply stand where you are.", 25),
            ScriptStep("When you're ready, let the garden slowly fade. Bring your attention back to the room around you.", 15),
            ScriptStep("Well done. You gave yourself a moment of calm. You can return here anytime.", 15)
        )
    )

    private val ScriptVisualizationTemple = GuidedScript(
        id = "visualization_temple",
        title = "Quiet courtyard",
        steps = listOf(
            ScriptStep("It's normal to feel on edge even in a quiet moment. Your mind is doing its best to keep you safe.", 15),
            ScriptStep("Imagine a quiet courtyard in the early morning. Stone walls are warm from the first light.", 20),
            ScriptStep("See a shallow pool of water at the center, perfectly still, reflecting the pale sky.", 20),
            ScriptStep("Listen for a bell in the distance, a single soft note that fades into silence.", 20),
            ScriptStep("Notice the faint scent of incense drifting across the courtyard. If scent feels wrong, skip it.", 20),
            ScriptStep("Feel the warmth of the sun on your arms. It is steady and gentle.", 20),
            ScriptStep("Place your hand on the cool stone wall. Feel its rough texture under your palm.", 20),
            ScriptStep("Stay here as long as you like. You can sit by the water or explore the courtyard.", 25),
            ScriptStep("When you're ready, let the courtyard fade. Bring your attention back to where you are now.", 15),
            ScriptStep("Well done. You gave yourself a quiet moment. You can return here whenever you need.", 15)
        )
    )

    // ── Public API ────────────────────────────────────────────────────

    /**
     * The complete list of bundled guided scripts.
     *
     * Order is intentional — grounding exercises come first (acute use),
     * followed by breathing variants, then PMR (longer, lower-urgency).
     */
    val ALL: List<GuidedScript> = listOf(
        ScriptGrounding54321,
        ScriptGrounding3Step,
        ScriptBreathingBox,
        ScriptBreathingCoherent,
        ScriptBreathingExhale,
        ScriptPmrFull,
        ScriptVisualizationMonsoon,
        ScriptVisualizationTemple
    )

    /**
     * Returns the script with the given [id], or `null` if no script
     * carries that identifier.
     *
     * @param id a lowercase-underscore identifier (e.g. `"breathing_box"`).
     * @return the matching [GuidedScript], or `null`.
     */
    fun byId(id: String): GuidedScript? = ALL.firstOrNull { it.id == id }
}
