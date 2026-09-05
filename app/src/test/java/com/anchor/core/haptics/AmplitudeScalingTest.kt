package com.anchor.core.haptics

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmplitudeScalingTest {

    @Test
    fun `full intensity leaves amplitudes unchanged`() {
        val result = scaleAmplitudes(intArrayOf(0, 128, 255), 1f)
        assertArrayEquals(intArrayOf(0, 128, 255), result)
    }

    @Test
    fun `zero intensity silences every segment`() {
        val result = scaleAmplitudes(intArrayOf(0, 128, 255), 0f)
        assertArrayEquals(intArrayOf(0, 0, 0), result)
    }

    @Test
    fun `half intensity roughly halves amplitude`() {
        val result = scaleAmplitudes(intArrayOf(200), 0.5f)
        assertArrayEquals(intArrayOf(100), result)
    }

    @Test
    fun `intensity above one is clamped to full strength`() {
        val result = scaleAmplitudes(intArrayOf(200), 2.5f)
        assertArrayEquals(intArrayOf(200), result)
    }

    @Test
    fun `negative intensity is clamped to silence`() {
        val result = scaleAmplitudes(intArrayOf(200), -1f)
        assertArrayEquals(intArrayOf(0), result)
    }

    @Test
    fun `null amplitudes pass through unchanged`() {
        assertNull(scaleAmplitudes(null, 0.7f))
    }
}
