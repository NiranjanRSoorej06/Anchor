package com.anchor.core.audio

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Dual-mode audio delivery engine boundary.
 *
 * If Bluetooth earbuds or headphones are connected:
 * - Speaks guided scripts in a soft, private female whisper into the user's ear.
 *
 * If NO earbuds are connected:
 * - Mutes audio output completely (0% speaker sound) so the app operates in
 *   100% silent, haptic-only mode (useful in meetings, classrooms, or public spaces).
 */
interface AudioDeliveryEngine {
    /** Returns true if private earbuds/headphones are connected and whisper delivery is active. */
    fun isWhisperModeActive(): Boolean

    /** Speaks [text] in a soft female whisper if earbuds are connected; mutes completely if not. */
    fun speakWhisper(text: String, queueMode: Int = TextToSpeech.QUEUE_ADD)

    /** Stops any ongoing speech immediately. */
    fun stop()

    /** Releases resources. */
    fun shutdown()
}

/**
 * Production Android [AudioDeliveryEngine] implementation backed by [TextToSpeech]
 * and [AudioOutputDetector].
 */
class WhisperAudioEngine(
    private val context: Context,
    private val detector: AudioOutputDetector = SystemAudioOutputDetector(context)
) : AudioDeliveryEngine, TextToSpeech.OnInitListener {

    private companion object {
        const val TAG = "WhisperAudioEngine"
    }

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isTtsReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "US English TTS language not supported")
            } else {
                selectSoftFemaleVoice()
                // Calibrate TTS for soft, soothing female whisper tone
                tts?.setPitch(1.05f)
                tts?.setSpeechRate(0.80f)
                isTtsReady = true
                Log.i(TAG, "Whisper TTS initialized cleanly with soft female voice")
            }
        } else {
            Log.w(TAG, "TTS initialization failed with status $status")
        }
    }

    private fun selectSoftFemaleVoice() {
        val ttsEngine = tts ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val femaleVoice = ttsEngine.voices?.find { voice ->
                    val name = voice.name.lowercase()
                    voice.locale.language == Locale.ENGLISH.language &&
                        (name.contains("female") || name.contains("fem") || name.contains("sfg") || name.contains("wmn") || name.contains("woman") || name.contains("en-us-x-sfg"))
                } ?: ttsEngine.voices?.find { voice ->
                    voice.locale.language == Locale.ENGLISH.language && !voice.isNetworkConnectionRequired
                }

                if (femaleVoice != null) {
                    ttsEngine.voice = femaleVoice
                    Log.i(TAG, "Selected soft female voice: ${femaleVoice.name}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error selecting soft female voice: ${e.message}")
        }
    }

    override fun isWhisperModeActive(): Boolean {
        return detector.isPrivateHeadsetConnected()
    }

    override fun speakWhisper(text: String, queueMode: Int) {
        // Essential Safety & Privacy rule: Mute 100% if no private earbuds are attached!
        if (!isWhisperModeActive()) {
            Log.d(TAG, "No earbuds connected — suppressing speaker audio (Silent Haptic Mode)")
            stop()
            return
        }

        if (!isTtsReady || tts == null) {
            Log.w(TAG, "TTS not ready yet; skipping whisper playback")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts?.speak(text, queueMode, null, "WhisperAudio_${System.currentTimeMillis()}")
            } else {
                @Suppress("DEPRECATION")
                tts?.speak(text, queueMode, null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to speak whisper: ${e.message}")
        }
    }

    override fun stop() {
        try {
            if (isTtsReady) {
                tts?.stop()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping TTS: ${e.message}")
        }
    }

    override fun shutdown() {
        try {
            stop()
            tts?.shutdown()
            tts = null
            isTtsReady = false
        } catch (e: Exception) {
            Log.w(TAG, "Error shutting down TTS: ${e.message}")
        }
    }
}

/**
 * Debug/fake engine for host unit testing and Compose previews.
 */
class DebugAudioEngine(
    var earbudConnected: Boolean = false
) : AudioDeliveryEngine {
    val spokenPhrases = mutableListOf<String>()

    override fun isWhisperModeActive(): Boolean = earbudConnected

    override fun speakWhisper(text: String, queueMode: Int) {
        if (earbudConnected) {
            spokenPhrases.add(text)
        }
    }

    override fun stop() {
        spokenPhrases.clear()
    }

    override fun shutdown() {
        spokenPhrases.clear()
    }
}

/**
 * Factory function to instantiate the system audio delivery engine.
 */
fun createAudioEngine(context: Context): AudioDeliveryEngine {
    return WhisperAudioEngine(context)
}
