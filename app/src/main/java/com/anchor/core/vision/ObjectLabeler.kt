package com.anchor.core.vision

import android.graphics.Bitmap

/**
 * The hardware/ML boundary for turning a photo into object labels. Nothing
 * outside this package should import ML Kit directly — every other layer
 * (the capture screen, the grounding-script builder) depends on this
 * interface instead, so it can run against [DebugObjectLabeler] on an
 * emulator and [MlKitObjectLabeler] on a device without changing a single
 * call site. Mirrors the [com.anchor.core.haptics.HapticEngine] pattern.
 */
interface ObjectLabeler {

    /**
     * Returns short object-name labels found in [bitmap] (e.g. "Chair",
     * "Window"), most-confident first. Returns an empty list if nothing is
     * detected or labeling fails — callers must treat that as "fall back
     * to the fixed grounding script," never as a reason to crash or block.
     */
    suspend fun label(bitmap: Bitmap): List<String>
}
