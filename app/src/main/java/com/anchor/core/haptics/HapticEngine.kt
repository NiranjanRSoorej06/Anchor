package com.anchor.core.haptics

/**
 * The hardware boundary for vibration. Nothing outside this package should
 * import android.os.Vibrator / VibratorManager / VibrationEffect directly —
 * every other layer (session logic, UI) depends on this interface instead,
 * so it can run against [DebugHapticEngine] on an emulator and
 * [SystemHapticEngine] on a device without changing a single call site.
 *
 * Deliberately not Compose-aware: this is plain Kotlin so it stays testable
 * and reusable from services/receivers that have no Compose context.
 */
interface HapticEngine {

    /** Snapshot of what this engine/device can do. Computed once at construction. */
    val capabilities: HapticCapabilities

    /**
     * Play [pattern] at [intensity] (0f..1f, clamped).
     *
     * Returns true if a real vibration was requested from the platform,
     * false if nothing happened (no vibrator, playback failed, or this is
     * a non-hardware engine). Callers must treat false as "silently
     * degrade" — never as a reason to crash or block the session.
     */
    fun play(pattern: HapticPattern, intensity: Float = 1f): Boolean

    /** Stop any vibration in progress. Always safe to call, even if nothing is playing. */
    fun stop()
}
