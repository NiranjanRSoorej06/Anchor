package com.anchor.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.random.Random

/**
 * On-device ambient-noise synthesis for the optional masking layer during
 * a comfort tool. Synthesized in real time — no bundled or downloaded
 * audio asset, so there's nothing to source, license, or ship in the APK.
 *
 * [Type.RAIN] and [Type.OCEAN] are honest signal-processing
 * approximations built on the same pink-noise basis as [Type.PINK] — a
 * random-walk amplitude envelope for rain's patter, a slow sine swell for
 * ocean's waves — not recordings, and not claimed to be. Framed in the UI
 * as "rain-like"/"wave-like" ambience, never as authentic nature audio.
 *
 * **Framing, deliberately:** this is comfort/masking, not noise
 * cancellation — a phone app cannot control headset hardware ANC, and
 * evidence for white/pink noise reducing anxiety is real but mixed and
 * mostly studied outside PTSD (sleep, procedural anxiety). See
 * `docs/exercise-evidence.md`. Never claim this "cancels" surrounding
 * sound; it makes it less noticeable by playing over it in your ear.
 *
 * Callers are responsible for the same earbuds-only gate every other
 * audio path in this app respects (`AudioDeliveryEngine.isWhisperModeActive`)
 * — this class has no opinion on headset state, it just plays if started.
 */
class NoiseGenerator {

    enum class Type { WHITE, PINK, RAIN, OCEAN }

    private companion object {
        const val TAG = "NoiseGenerator"
        const val SAMPLE_RATE = 44100
        const val PINK_ROWS = 16
    }

    @Volatile
    private var running = false
    private var thread: Thread? = null

    fun start(type: Type, volume: Float = 0.35f) {
        if (running) return
        running = true
        thread = Thread {
            var track: AudioTrack? = null
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                track.setVolume(volume.coerceIn(0f, 1f))
                track.play()

                val chunk = ShortArray(bufferSize / 2)
                // Voss-McCartney pink noise: a small set of white-noise rows,
                // each re-rolled at a halving rate and summed — cheap,
                // no FFT/filtering needed for a real-time loop like this.
                // RAIN and OCEAN both start from this same pink basis and
                // apply a simple amplitude envelope on top of it.
                val pinkRows = FloatArray(PINK_ROWS)
                var pinkSum = 0f
                var sampleCount = 0
                // RAIN: a slowly random-walking envelope gives an uneven,
                // patter-like texture instead of pink noise's flat level.
                var rainEnvelope = 0.7f
                // OCEAN: a slow sine LFO (~0.12 Hz) swells the level up and
                // down like waves arriving and receding.
                var oceanPhase = 0f
                val oceanPhaseStep = (2.0 * Math.PI * 0.12 / SAMPLE_RATE).toFloat()

                while (running) {
                    for (i in chunk.indices) {
                        val pink = run {
                            sampleCount++
                            var idx = sampleCount
                            var row = 0
                            while (idx and 1 == 0 && row < PINK_ROWS - 1) {
                                idx = idx shr 1
                                row++
                            }
                            val newValue = Random.nextFloat() * 2f - 1f
                            pinkSum += newValue - pinkRows[row]
                            pinkRows[row] = newValue
                            (pinkSum / PINK_ROWS).coerceIn(-1f, 1f)
                        }
                        val sample = when (type) {
                            Type.WHITE -> Random.nextFloat() * 2f - 1f
                            Type.PINK -> pink
                            Type.RAIN -> {
                                rainEnvelope = (rainEnvelope + (Random.nextFloat() - 0.5f) * 0.02f).coerceIn(0.3f, 1f)
                                (pink * rainEnvelope).coerceIn(-1f, 1f)
                            }
                            Type.OCEAN -> {
                                oceanPhase += oceanPhaseStep
                                val swell = 0.55f + 0.45f * kotlin.math.sin(oceanPhase)
                                (pink * swell).coerceIn(-1f, 1f)
                            }
                        }
                        chunk[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                    track.write(chunk, 0, chunk.size)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Noise generation stopped: ${e.message}")
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (e: Exception) {
                    // Already released — harmless.
                }
            }
        }.apply {
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    fun stop() {
        running = false
        thread = null
    }
}
