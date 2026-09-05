package com.anchor.domain.grounding

/**
 * A short, ordered set of calm grounding sentences, generated from objects
 * detected in a single on-demand photo (or, when no usable objects are
 * available, a fixed fallback). This is plain Kotlin — no Android, no
 * network, no persistence of the source image.
 *
 * @property sentences Ordered list of short sentences to display, one per
 *   grounded object. Never empty — [GroundingScriptBuilder] always returns
 *   at least the fallback script.
 * @property isFallback True when [sentences] came from the fixed fallback
 *   script rather than real detected objects (no camera permission, no
 *   objects detected, or every detected label was filtered out).
 */
data class GroundingScript(
    val sentences: List<String>,
    val isFallback: Boolean
)
