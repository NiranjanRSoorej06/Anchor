package com.anchor.core.haptics

import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A non-hardware [HapticEngine] for the emulator, where nothing physically
 * vibrates. This engine never claims otherwise: [play] always returns
 * false, and [capabilities] honestly reports no vibrator.
 *
 * What it *does* provide is [activePulse], a plain Kotlin StateFlow (no
 * Compose dependency here) that a later UI layer can collect to render an
 * on-screen visual stand-in for the haptic while developing off-device.
 */
class DebugHapticEngine : HapticEngine {

    override val capabilities: HapticCapabilities = HapticCapabilities.none(Build.VERSION.SDK_INT)

    private val _activePulse = MutableStateFlow<DebugPulse?>(null)

    /** The most recently requested pattern, or null once stopped. For UI debug rendering only. */
    val activePulse: StateFlow<DebugPulse?> = _activePulse.asStateFlow()

    override fun play(pattern: HapticPattern, intensity: Float): Boolean {
        val clampedIntensity = intensity.coerceIn(0f, 1f)
        Log.d(TAG, "would play '${pattern.id}' at intensity=$clampedIntensity (emulator does not vibrate)")
        _activePulse.value = DebugPulse(
            patternId = pattern.id,
            startedAtMillis = System.currentTimeMillis(),
            intensity = clampedIntensity
        )
        // Honest: no real vibration was produced.
        return false
    }

    override fun stop() {
        Log.d(TAG, "stop()")
        _activePulse.value = null
    }

    companion object {
        private const val TAG = "DebugHapticEngine"
    }
}

/** What debug pattern is "playing", for a future visual pulse to render. */
data class DebugPulse(
    val patternId: String,
    val startedAtMillis: Long,
    val intensity: Float
)
