package com.anchor.core.vision

import android.os.Build

/**
 * The single, canonical place that decides which [ObjectLabeler] to use.
 * Mirrors [com.anchor.core.haptics.createHapticEngine] and its emulator
 * heuristic exactly — same reasoning: on a real phone this always resolves
 * to [MlKitObjectLabeler]; it exists so the emulator (whose virtual camera
 * doesn't produce meaningful frames for a real model to label) gets
 * [DebugObjectLabeler] instead, without anyone needing to switch it by hand.
 */
fun createObjectLabeler(): ObjectLabeler {
    val looksLikeEmulator = Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu") ||
        Build.PRODUCT.contains("sdk")

    return if (looksLikeEmulator) DebugObjectLabeler() else MlKitObjectLabeler()
}
