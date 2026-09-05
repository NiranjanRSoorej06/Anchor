package com.anchor.domain.grounding

/**
 * Turns raw object-detection labels into a short, calm grounding script —
 * the "5 things you can see" technique, without asking the user to think
 * of the objects themselves.
 *
 * Deterministic and testable: no LLM, no network call — every sentence
 * comes from one fixed template. This is plain Kotlin, safe to unit test
 * on any host.
 */
object GroundingScriptBuilder {

    /** Matches the classic "5 things you can see" grounding technique. */
    const val MAX_SENTENCES = 5

    /**
     * Labels naming a person are excluded before building sentences.
     * Narrating "I see a person" about someone else who happens to be in
     * frame, without their consent, is a real dignity/privacy problem —
     * this filter runs before anything else in this file.
     */
    /**
     * Labels naming a person, generic abstract categories, shapes, colors, or room tags
     * are excluded before building sentences. Only concrete physical objects survive.
     */
    private val IGNORED_LABELS = setOf(
        // Persons
        "person", "people", "human", "human face", "face", "man", "woman",
        "boy", "girl", "child", "baby", "selfie", "portrait", "crowd",
        // Generic abstractions & shapes
        "font", "pattern", "design", "product", "material", "line", "parallel",
        "rectangle", "circle", "square", "shape", "brand", "logo", "text",
        "graphics", "art", "illustration", "component", "multimedia", "symbol",
        // Abstract room/environment/color terms
        "room", "floor", "ceiling", "wall", "lighting", "space", "indoor",
        "outdoor", "sky", "black", "white", "blue", "red", "green", "yellow",
        "shadow", "darkness", "light", "surface", "wood", "metal", "plastic",
        // Broad category catch-alls
        "technology", "electronic device", "display device", "accessory",
        "furniture", "object", "thing", "equipment", "gadget", "device"
    )

    /** Used when no real, usable labels are available — never references the camera. */
    private val FALLBACK_SENTENCES = listOf(
        "Notice something you can see right now.",
        "Notice something you can hear right now.",
        "Notice something you can touch right now.",
        "Notice your feet on the ground.",
        "Notice your breath, in and out."
    )

    /**
     * Builds a [GroundingScript] from raw detector [labels]. Filters out
     * blank entries and person-related labels, deduplicates
     * (case-insensitive), caps at [MAX_SENTENCES], and falls back to the
     * fixed camera-independent script when nothing usable survives.
     */
    fun build(labels: List<String>): GroundingScript {
        val usable = labels
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filter { label -> IGNORED_LABELS.none { it.equals(label, ignoreCase = true) } }
            .distinctBy { it.lowercase() }
            .take(MAX_SENTENCES)

        if (usable.isEmpty()) {
            return GroundingScript(sentences = FALLBACK_SENTENCES, isFallback = true)
        }

        return GroundingScript(
            sentences = usable.map(::sentenceFor),
            isFallback = false
        )
    }

    private fun sentenceFor(label: String): String =
        "Notice the ${label.lowercase()} near you."
}
