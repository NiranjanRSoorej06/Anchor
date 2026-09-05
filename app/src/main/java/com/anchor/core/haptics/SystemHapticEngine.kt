package com.anchor.core.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Real vibration hardware, reached through the platform APIs.
 *
 * Every Android call in here is wrapped defensively: a missing vibrator,
 * an unsupported feature, or a thrown exception from the platform must
 * never propagate out of this class. [play] and [stop] report failure by
 * returning false / doing nothing, never by throwing.
 */
class SystemHapticEngine(context: Context) : HapticEngine {

    private val appContext = context.applicationContext
    private val vibrator: Vibrator? = resolveVibrator(appContext)

    override val capabilities: HapticCapabilities = buildCapabilities(vibrator)

    override fun play(pattern: HapticPattern, intensity: Float): Boolean {
        val activeVibrator = vibrator
        if (activeVibrator == null || !capabilities.hasVibrator) {
            Log.d(TAG, "play(${pattern.id}) skipped: no vibrator on this device")
            return false
        }

        return try {
            val scaledAmplitudes = scaleAmplitudes(pattern.amplitudes, intensity)
            val effect = VibrationEffect.createWaveform(pattern.timings, scaledAmplitudes, pattern.repeatIndex)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Non-null by construction: ACCESSIBILITY_ATTRIBUTES is only null below API 33.
                activeVibrator.vibrate(effect, ACCESSIBILITY_ATTRIBUTES!!)
            } else {
                @Suppress("DEPRECATION")
                activeVibrator.vibrate(effect)
            }
            true
        } catch (e: Exception) {
            // Any platform failure (bad waveform, OEM quirk, permission issue) degrades
            // silently — a haptic that doesn't fire must never take the session down with it.
            Log.w(TAG, "play(${pattern.id}) failed: ${e.message}")
            false
        }
    }

    override fun stop() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "stop() failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SystemHapticEngine"

        /**
         * Vibration attributes marking these effects as accessibility-purposed
         * output, so Do Not Disturb doesn't silence them. Without this,
         * a phone in DND — exactly the state someone in distress might be
         * holding — never vibrates at all.
         */
        private val ACCESSIBILITY_ATTRIBUTES: VibrationAttributes? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_ACCESSIBILITY)
                    .build()
            } else {
                null
            }

        private fun resolveVibrator(context: Context): Vibrator? = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to resolve a Vibrator: ${e.message}")
            null
        }

        private fun buildCapabilities(vibrator: Vibrator?): HapticCapabilities {
            if (vibrator == null) return HapticCapabilities.none(Build.VERSION.SDK_INT)

            val hasVibrator = try {
                vibrator.hasVibrator()
            } catch (e: Exception) {
                false
            }

            val hasAmplitudeControl = hasVibrator && try {
                vibrator.hasAmplitudeControl()
            } catch (e: Exception) {
                false
            }

            val supportsPrimitives = hasVibrator &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                try {
                    vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)
                } catch (e: Exception) {
                    false
                }

            return HapticCapabilities(
                hasVibrator = hasVibrator,
                hasAmplitudeControl = hasAmplitudeControl,
                supportsPrimitives = supportsPrimitives,
                apiLevel = Build.VERSION.SDK_INT
            )
        }
    }
}

/**
 * Scales each amplitude by [intensity] (clamped 0f..1f), clamped back into
 * VibrationEffect's valid 0..255 range. Pure function — no Android
 * dependency — so it is unit-testable without a device or Robolectric.
 *
 * A null [amplitudes] array (default-amplitude waveform) passes through
 * unchanged: there is nothing to scale.
 */
internal fun scaleAmplitudes(amplitudes: IntArray?, intensity: Float): IntArray? {
    if (amplitudes == null) return null
    val clampedIntensity = intensity.coerceIn(0f, 1f)
    return IntArray(amplitudes.size) { i ->
        (amplitudes[i] * clampedIntensity).toInt().coerceIn(0, 255)
    }
}
