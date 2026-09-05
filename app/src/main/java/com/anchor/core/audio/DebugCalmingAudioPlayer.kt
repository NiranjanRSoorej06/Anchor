package com.anchor.core.audio

import android.util.Log

/**
 * A non-hardware [CalmingAudioPlayer] for the emulator and unit tests.
 *
 * Follows the [DebugHapticEngine][com.anchor.core.haptics.DebugHapticEngine] pattern:
 * every method is a safe no-op that records what *would* have happened so
 * tests and previews can assert against [playedAssets].
 *
 * [isPlaying] tracks the logical state: true between [play] and [stop].
 */
class DebugCalmingAudioPlayer : CalmingAudioPlayer {

    /** Assets requested via [play], in order. For test assertions and preview rendering. */
    val playedAssets = mutableListOf<String>()

    @Volatile
    override var isPlaying: Boolean = false
        private set

    override fun play(assetFileName: String) {
        Log.d(TAG, "play('$assetFileName') — emulator, no audio output")
        playedAssets.add(assetFileName)
        isPlaying = true
    }

    override fun stop() {
        Log.d(TAG, "stop()")
        isPlaying = false
    }

    companion object {
        private const val TAG = "DebugCalmingAudioPlayer"
    }
}
