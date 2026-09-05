package com.anchor.core.audio

import android.content.Context
import android.media.AudioAttributes
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
    fun speakWhisper(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH)

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
    private var pendingSpeechText: String? = null
    private var pendingQueueMode: Int = TextToSpeech.QUEUE_FLUSH

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                tts?.setLanguage(Locale.US)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    tts?.setAudioAttributes(audioAttributes)
                }

                // Calibrate TTS for soft, soothing female whisper tone (1.18f pitch for natural soft female voice)
                tts?.setPitch(1.18f)
                tts?.setSpeechRate(0.85f)
                isTtsReady = true
                Log.i(TAG, "Whisper TTS initialized instantly with soft female voice")

                // Immediately speak any phrase requested while TTS was starting
                val textToSpeak = pendingSpeechText
                if (textToSpeak != null && isWhisperModeActive()) {
                    speakInternal(textToSpeak, pendingQueueMode)
                    pendingSpeechText = null
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error configuring TTS: ${e.message}")
            }
        } else {
            Log.w(TAG, "TTS initialization failed with status $status")
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
            Log.d(TAG, "TTS initializing; queuing instant speech: $text")
            pendingSpeechText = text
            pendingQueueMode = queueMode
            return
        }

        speakInternal(text, queueMode)
    }

    private fun speakInternal(text: String, queueMode: Int) {
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
            pendingSpeechText = null
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
