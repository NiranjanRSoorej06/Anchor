package com.anchor.core.haptics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class HapticPatternTest {

    @Test
    fun `equal patterns with equal array contents are equal`() {
        val a = HapticPattern("p", longArrayOf(0, 100), intArrayOf(0, 255))
        val b = HapticPattern("p", longArrayOf(0, 100), intArrayOf(0, 255))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `patterns with different timings are not equal`() {
        val a = HapticPattern("p", longArrayOf(0, 100))
        val b = HapticPattern("p", longArrayOf(0, 200))
        assertFalse(a == b)
    }

    @Test
    fun `empty timings are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            HapticPattern("bad", longArrayOf())
        }
    }

    @Test
    fun `mismatched amplitudes size is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            HapticPattern("bad", longArrayOf(0, 100), intArrayOf(255))
        }
    }

    @Test
    fun `test pulse is a valid pattern`() {
        val pattern = HapticPattern.TEST_PULSE
        assertEquals(pattern.timings.size, pattern.amplitudes?.size)
    }
}
