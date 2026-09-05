package com.anchor.core.haptics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HapticPatternsTest {

    private val requiredIds = setOf(
        "test_pulse",
        "double_pulse",
        "slow_pulse",
        "breathing_in",
        "breathing_out"
    )

    @Test
    fun `every required pattern exists in the catalog`() {
        val ids = HapticPatterns.ALL.map { it.id }.toSet()
        assertEquals(requiredIds, ids)
    }

    @Test
    fun `named catalog properties match the ALL list`() {
        assertTrue(HapticPatterns.TEST_PULSE in HapticPatterns.ALL)
        assertTrue(HapticPatterns.DOUBLE_PULSE in HapticPatterns.ALL)
        assertTrue(HapticPatterns.SLOW_PULSE in HapticPatterns.ALL)
        assertTrue(HapticPatterns.BREATHING_IN in HapticPatterns.ALL)
        assertTrue(HapticPatterns.BREATHING_OUT in HapticPatterns.ALL)
    }

    @Test
    fun `every pattern id is unique`() {
        val ids = HapticPatterns.ALL.map { it.id }
        assertEquals(
            "duplicate pattern id found: $ids",
            ids.size,
            ids.toSet().size
        )
    }

    @Test
    fun `every pattern has non-empty non-negative timings`() {
        for (pattern in HapticPatterns.ALL) {
            assertTrue("${pattern.id} must have at least one timing", pattern.timings.isNotEmpty())
            for (timing in pattern.timings) {
                assertTrue("${pattern.id} has a negative timing: $timing", timing >= 0)
            }
        }
    }

    @Test
    fun `every pattern's amplitudes array matches its timings length`() {
        for (pattern in HapticPatterns.ALL) {
            val amplitudes = pattern.amplitudes
            assertTrue("${pattern.id} should declare explicit amplitudes", amplitudes != null)
            assertEquals(
                "${pattern.id} timings/amplitudes length mismatch",
                pattern.timings.size,
                amplitudes!!.size
            )
        }
    }

    @Test
    fun `every amplitude is within the valid 0 to 255 range`() {
        for (pattern in HapticPatterns.ALL) {
            for (amplitude in pattern.amplitudes ?: IntArray(0)) {
                assertTrue(
                    "${pattern.id} has an out-of-range amplitude: $amplitude",
                    amplitude in 0..255
                )
            }
        }
    }

    @Test
    fun `repeat index is either disabled or a valid index into timings`() {
        for (pattern in HapticPatterns.ALL) {
            val repeat = pattern.repeatIndex
            assertTrue(
                "${pattern.id} has an invalid repeatIndex: $repeat",
                repeat == -1 || repeat in pattern.timings.indices
            )
        }
    }

    @Test
    fun `TEST_PULSE is unchanged from Module 1`() {
        val pattern = HapticPatterns.TEST_PULSE
        assertEquals("test_pulse", pattern.id)
        assertEquals(-1, pattern.repeatIndex)
        assertEquals(longArrayOf(0, 100).toList(), pattern.timings.toList())
        assertEquals(intArrayOf(0, 255).toList(), pattern.amplitudes?.toList())
    }

    @Test
    fun `double pulse and slow pulse are distinguishable patterns`() {
        assertTrue(HapticPatterns.DOUBLE_PULSE != HapticPatterns.SLOW_PULSE)
        // Double pulse has two taps per cycle: more segments than a single-pulse pattern.
        assertTrue(HapticPatterns.DOUBLE_PULSE.timings.size > HapticPatterns.SLOW_PULSE.timings.size)
    }

    @Test
    fun `breathing in ramps up and breathing out ramps down`() {
        val inAmps = HapticPatterns.BREATHING_IN.amplitudes!!
        val outAmps = HapticPatterns.BREATHING_OUT.amplitudes!!

        assertTrue("BREATHING_IN should start weaker than it ends", inAmps.first() < inAmps.last())
        assertTrue("BREATHING_OUT should start stronger than it ends", outAmps.first() > outAmps.last())

        // Mirrors: reversing one should match the shape of the other.
        assertEquals(inAmps.toList(), outAmps.reversed())
    }

    @Test
    fun `accessing a pattern repeatedly is deterministic`() {
        val first = HapticPatterns.DOUBLE_PULSE
        val second = HapticPatterns.DOUBLE_PULSE
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `no pattern id references heartbeat or biometric terms`() {
        // "pulse" alone is fine (TEST_PULSE, DOUBLE_PULSE, SLOW_PULSE are tactile
        // rhythms) — this guards specifically against physiological framing.
        val forbidden = listOf("heart", "biometric", "physiolog")
        for (pattern in HapticPatterns.ALL) {
            val lowerId = pattern.id.lowercase()
            for (term in forbidden) {
                assertTrue("${pattern.id} id must not reference '$term'", !lowerId.contains(term))
            }
        }
    }
}
