package com.anchor.core.haptics

/**
 * What the current device/engine can actually do. Callers (and a later
 * Settings screen) use this to decide whether to show a haptic-unavailable
 * fallback rather than assuming vibration always works.
 */
data class HapticCapabilities(
    /** False on devices with no vibration hardware at all, or when the engine can't reach it. */
    val hasVibrator: Boolean,
    /** Whether the vibrator honors per-segment amplitude, or only on/off. */
    val hasAmplitudeControl: Boolean,
    /** Whether VibrationEffect.Composition primitives (API 31+) are usable. */
    val supportsPrimitives: Boolean,
    /** The device's SDK level, kept here so callers don't need their own Build.VERSION checks. */
    val apiLevel: Int
) {
    companion object {
        /** The honest baseline for a device/engine with no vibration capability. */
        fun none(apiLevel: Int) = HapticCapabilities(
            hasVibrator = false,
            hasAmplitudeControl = false,
            supportsPrimitives = false,
            apiLevel = apiLevel
        )
    }
}
