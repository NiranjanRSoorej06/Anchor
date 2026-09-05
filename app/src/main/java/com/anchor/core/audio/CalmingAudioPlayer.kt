package com.anchor.core.audio

/**
 * Calming audio playback boundary for bundled asset files (e.g. brown noise,
 * nature loops, safety-phrase recordings).
 *
 * Follows the same hardware-boundary pattern as [com.anchor.core.haptics.HapticEngine]:
 * nothing above this layer imports MediaPlayer directly — production code uses
 * [MediaPlayerCalmingAudioPlayer], test / preview code uses [DebugCalmingAudioPlayer].
 *
 * Deliberately not Compose-aware: plain Kotlin so it stays testable and
 * reusable from services/receivers that have no Compose context.
 */
interface CalmingAudioPlayer {

    /**
     * Play the bundled asset [assetFileName] in a loop.
     *
     * If audio is already playing, the caller must [stop] before calling play
     * again — double-play is undefined.
     *
     * @param assetFileName the asset filename relative to `assets/`, e.g. `"calm_rain.ogg"`.
     */
    fun play(assetFileName: String)

    /** Stop any audio currently playing. Always safe to call, even if nothing is playing. */
    fun stop()

    /** Whether audio is currently playing. */
    val isPlaying: Boolean
}
