package com.anchor.core.haptics

import android.content.Context
import android.os.Build

/**
 * The single, canonical place that decides which [HapticEngine] to use.
 *
 * Previously duplicated privately inside two devtools screens; now a real
 * product screen needs the exact same decision, so it lives here instead —
 * one heuristic, not three copies of it.
 *
 * The heuristic itself only matters for development: on a real phone this
 * will always resolve to [SystemHapticEngine]. It exists so the emulator
 * (which cannot vibrate) gets [DebugHapticEngine] instead, without anyone
 * needing to switch that by hand.
 */
fun createHapticEngine(context: Context): HapticEngine {
    val looksLikeEmulator = Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu") ||
        Build.PRODUCT.contains("sdk")

    return if (looksLikeEmulator) DebugHapticEngine() else SystemHapticEngine(context)
}
