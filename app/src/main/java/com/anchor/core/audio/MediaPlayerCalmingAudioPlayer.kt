package com.anchor.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

/**
 * Production [CalmingAudioPlayer] backed by Android [MediaPlayer].
 *
 * The player:
 * - Loads from the bundled `assets/` directory via [android.content.res.AssetFileDescriptor].
 * - Sets looping to `true` so calming audio plays continuously until [stop].
 * - Catches all platform failures defensively — a MediaPlayer error must
 *   never propagate out of this class.
 */
class MediaPlayerCalmingAudioPlayer(context: Context) : CalmingAudioPlayer {

    private val appContext = context.applicationContext

    private var player: MediaPlayer? = null

    @Volatile
    override var isPlaying: Boolean = false
        private set

    override fun play(assetFileName: String) {
        stop()
        try {
            val afd = appContext.assets.openFd(assetFileName)
            val mp = MediaPlayer()
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.isLooping = true
            mp.prepare()
            mp.start()
            player = mp
            isPlaying = true
            Log.i(TAG, "Playing calming audio: $assetFileName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play calming audio '$assetFileName': ${e.message}", e)
            isPlaying = false
        }
    }

    override fun stop() {
        try {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping calming audio: ${e.message}")
        } finally {
            player = null
            isPlaying = false
        }
    }

    companion object {
        private const val TAG = "MediaPlayerCalmingAudio"
    }
}
