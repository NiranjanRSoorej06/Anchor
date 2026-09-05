package com.anchor.core.vision

import android.graphics.Bitmap

/**
 * Emulator/preview-safe [ObjectLabeler]. The Android emulator's virtual
 * camera and ML Kit's on-device model do not reliably combine to produce
 * meaningful labels, so this returns a fixed, plausible-looking result
 * instead — enough to exercise the whole capture -> label -> script flow
 * without a real camera or a real model. Mirrors [com.anchor.core.haptics.DebugHapticEngine].
 */
class DebugObjectLabeler : ObjectLabeler {
    override suspend fun label(bitmap: Bitmap): List<String> =
        listOf("Chair", "Window", "Lamp")
}
