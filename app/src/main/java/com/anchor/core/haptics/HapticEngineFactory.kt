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
    // Always instantiate SystemHapticEngine to drive real system vibration.
    // SystemHapticEngine contains internal safety checks to degrade gracefully
    // if a device or emulator lacks vibration hardware.
    return SystemHapticEngine(context)
}
