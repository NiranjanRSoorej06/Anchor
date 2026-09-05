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
 * Singleton provider to keep TTS warm in memory 24/7 across the accessibility service
 * and app activities for zero-latency speech playback.
 */
object AudioEngineProvider {
    @Volatile
    private var instance: AudioDeliveryEngine? = null

    fun get(context: Context): AudioDeliveryEngine {
        return instance ?: synchronized(this) {
            instance ?: WhisperAudioEngine(context.applicationContext).also { instance = it }
        }
    }
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
        Log.i(TAG, "TTS onInit status=$status")
        if (status == TextToSpeech.SUCCESS) {
            try {
                tts?.setLanguage(Locale.US)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    tts?.setAudioAttributes(audioAttributes)
                }

                selectSoftFemaleVoice()
                // Calibrate TTS for warm, natural, high-clarity soothing female tone
                tts?.setPitch(1.05f)
                tts?.setSpeechRate(0.82f)
                isTtsReady = true
                Log.i(TAG, "Whisper TTS ready! Soothing female voice active")

                // Immediately speak any phrase requested while TTS was starting
                val textToSpeak = pendingSpeechText
                if (textToSpeak != null) {
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

    private fun selectSoftFemaleVoice() {
        val ttsEngine = tts ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val voices = ttsEngine.voices
                if (!voices.isNullOrEmpty()) {
                    val femaleVoice = voices
                        .filter { voice ->
                            val lang = voice.locale.language
                            (lang == Locale.ENGLISH.language || lang == "en") &&
                                !voice.isNetworkConnectionRequired &&
                                voice.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) != true
                        }
                        .sortedWith(
                            compareByDescending<android.speech.tts.Voice> { voice ->
                                var score = 0
                                val name = voice.name.lowercase()
                                if (name.contains("female") || name.contains("fem") ||
                                    name.contains("sfg") || name.contains("wmn") ||
                                    name.contains("woman") || name.contains("en-us-x-sfg")) {
                                    score += 100
                                }
                                if (voice.quality == android.speech.tts.Voice.QUALITY_VERY_HIGH) score += 50
                                else if (voice.quality == android.speech.tts.Voice.QUALITY_HIGH) score += 30
                                else if (voice.quality == android.speech.tts.Voice.QUALITY_NORMAL) score += 10
                                score
                            }
                        )
                        .firstOrNull() ?: voices.find { voice ->
                        voice.locale.language == Locale.ENGLISH.language && !voice.isNetworkConnectionRequired
                    }

                    if (femaleVoice != null) {
                        ttsEngine.voice = femaleVoice
                        Log.i(TAG, "Selected soothing female voice: ${femaleVoice.name}")
                    }
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
        val earbudConnected = isWhisperModeActive()
        Log.i(TAG, "speakWhisper requested: '$text' (earbudsConnected=$earbudConnected, ttsReady=$isTtsReady)")

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
                val params = android.os.Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                }
                tts?.speak(text, queueMode, params, "WhisperAudio_${System.currentTimeMillis()}")
            } else {
                @Suppress("DEPRECATION")
                val params = java.util.HashMap<String, String>().apply {
                    put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1.0")
                }
                @Suppress("DEPRECATION")
                tts?.speak(text, queueMode, params)
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
 * Factory function to instantiate the system audio delivery engine (pre-warmed singleton).
 */
fun createAudioEngine(context: Context): AudioDeliveryEngine {
    return AudioEngineProvider.get(context)
}
