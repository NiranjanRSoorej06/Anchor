package com.anchor.core.haptics

/**
 * A device-agnostic vibration waveform.
 *
 * Maps directly onto [android.os.VibrationEffect.createWaveform]: [timings] are
 * millisecond durations, [amplitudes] (0-255, or null for default amplitude)
 * are the strength for each timing entry, and [repeatIndex] is the index to
 * loop back to (-1 for no repeat).
 *
 * This module defines the shape only. Named, tuned patterns (double pulse,
 * slow pulse, breathing, and future textures) live in [HapticPatterns].
 */
data class HapticPattern(
    val id: String,
    val timings: LongArray,
    val amplitudes: IntArray? = null,
    val repeatIndex: Int = -1
) {
    init {
        require(timings.isNotEmpty()) { "HapticPattern '$id' must have at least one timing entry" }
        require(amplitudes == null || amplitudes.size == timings.size) {
            "HapticPattern '$id' amplitudes size (${amplitudes?.size}) must match timings size (${timings.size})"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HapticPattern) return false
        return id == other.id &&
            timings.contentEquals(other.timings) &&
            (amplitudes?.contentEquals(other.amplitudes ?: IntArray(0)) ?: (other.amplitudes == null)) &&
            repeatIndex == other.repeatIndex
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + timings.contentHashCode()
        result = 31 * result + (amplitudes?.contentHashCode() ?: 0)
        result = 31 * result + repeatIndex
        return result
    }

    companion object {
        /**
         * A minimal single-pulse waveform used to exercise [HapticEngine]
         * implementations. Not a tuned product pattern.
         */
        val TEST_PULSE = HapticPattern(
            id = "test_pulse",
            timings = longArrayOf(0, 100),
            amplitudes = intArrayOf(0, 255),
            repeatIndex = -1
        )
    }
}
