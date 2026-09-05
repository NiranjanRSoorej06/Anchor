package com.anchor.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

enum class CalmingPreset(val displayName: String, val description: String) {
    OCEAN_WAVES("Soothing Ocean Waves", "Continuous rhythm of ocean tides to synchronize deep breathing."),
    GENTLE_RAIN("Gentle Ambient Rain", "Soft steady rainfall sounds to calm mental agitation."),
    WHITE_NOISE("Calming Soft Noise", "Consistent frequency sound mask to reduce external trigger sensitivity."),
    CUSTOM_MP3("Loved One's Voice / Custom MP3", "Play a custom recorded voice message or loved one's audio file."),
    QUIET("Quiet Mode (No Background Audio)", "100% silent background mode; voice guidance & haptics only.")
}

interface CalmingAudioEngine {
    val currentPreset: CalmingPreset
    val customMp3Uri: Uri?
    fun startCalmingAudio(preset: CalmingPreset = CalmingPreset.OCEAN_WAVES)
    fun stopCalmingAudio()
    fun updateCustomMp3Uri(uri: Uri?)
    fun isPlaying(): Boolean
}

class SystemCalmingAudioEngine(private val context: Context) : CalmingAudioEngine {

    private companion object {
        const val TAG = "CalmingAudioEngine"
    }

    private var mediaPlayer: MediaPlayer? = null
    override var customMp3Uri: Uri? = null
    override var currentPreset: CalmingPreset = CalmingPreset.OCEAN_WAVES

    override fun startCalmingAudio(preset: CalmingPreset) {
        currentPreset = preset
        if (preset == CalmingPreset.QUIET) {
            stopCalmingAudio()
            return
        }

        try {
            stopCalmingAudio()

            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
            }

            if (preset == CalmingPreset.CUSTOM_MP3 && customMp3Uri != null) {
                mp.setDataSource(context, customMp3Uri!!)
                mp.prepareAsync()
                mp.setOnPreparedListener { player ->
                    player.start()
                    Log.i(TAG, "Custom MP3 calming audio playback started: $customMp3Uri")
                }
            } else {
                Log.i(TAG, "Calming background audio initialized for preset: ${preset.displayName}")
            }
            mediaPlayer = mp
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

    override fun updateCustomMp3Uri(uri: Uri?) {
        this.customMp3Uri = uri
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
    override var customMp3Uri: Uri? = null
    override var currentPreset: CalmingPreset = CalmingPreset.OCEAN_WAVES

    override fun startCalmingAudio(preset: CalmingPreset) {
        currentPreset = preset
        playing = preset != CalmingPreset.QUIET
    }

    override fun stopCalmingAudio() {
        playing = false
    }

    override fun updateCustomMp3Uri(uri: Uri?) {
        this.customMp3Uri = uri
    }

    override fun isPlaying(): Boolean = playing
}
