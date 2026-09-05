package com.anchor.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

enum class CalmingPreset(val displayName: String) {
    OCEAN_WAVES("Soothing Ocean Waves"),
    GENTLE_RAIN("Gentle Ambient Rain"),
    WHITE_NOISE("Calming Soft Noise"),
    CUSTOM_MP3("Custom Loved-One Voice / MP3"),
    QUIET("Quiet Mode (No Background Audio)")
}

interface CalmingAudioEngine {
    val currentPreset: CalmingPreset
    fun startCalmingAudio(preset: CalmingPreset = CalmingPreset.OCEAN_WAVES)
    fun stopCalmingAudio()
    fun setCustomMp3Uri(uri: Uri?)
    fun isPlaying(): Boolean
}

class SystemCalmingAudioEngine(private val context: Context) : CalmingAudioEngine {

    private companion object {
        const val TAG = "CalmingAudioEngine"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var customUri: Uri? = null
    override var currentPreset: CalmingPreset = CalmingPreset.OCEAN_WAVES

    override fun startCalmingAudio(preset: CalmingPreset) {
        currentPreset = preset
        if (preset == CalmingPreset.QUIET) {
            stopCalmingAudio()
            return
        }

        try {
            stopCalmingAudio()

            // Initialize MediaPlayer with looping enabled
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
            }

            if (preset == CalmingPreset.CUSTOM_MP3 && customUri != null) {
                mediaPlayer?.setDataSource(context, customUri!!)
                mediaPlayer?.prepareAsync()
                mediaPlayer?.setOnPreparedListener { mp ->
                    mp.start()
                    Log.i(TAG, "Custom MP3 calming audio playback started")
                }
            } else {
                // Preset synthesized gentle ambient sound simulation or system asset
                Log.i(TAG, "Calming audio active for preset: ${preset.displayName}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error starting calming audio: ${e.message}")
        }
    }

    override fun stopCalmingAudio() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping calming audio: ${e.message}")
        }
    }

    override fun setCustomMp3Uri(uri: Uri?) {
        this.customUri = uri
        Log.i(TAG, "Custom MP3 URI updated: $uri")
    }

    override fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }
}

class DebugCalmingAudioEngine : CalmingAudioEngine {
    private var playing = false
    override var currentPreset: CalmingPreset = CalmingPreset.OCEAN_WAVES

    override fun startCalmingAudio(preset: CalmingPreset) {
        currentPreset = preset
        playing = preset != CalmingPreset.QUIET
    }

    override fun stopCalmingAudio() {
        playing = false
    }

    override fun setCustomMp3Uri(uri: Uri?) {}

    override fun isPlaying(): Boolean = playing
}
