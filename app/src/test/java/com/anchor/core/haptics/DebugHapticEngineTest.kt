package com.anchor.core.haptics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class DebugHapticEngineTest {

    @Test
    fun `debug engine honestly reports no vibrator`() {
        val engine = DebugHapticEngine()
        assertFalse(engine.capabilities.hasVibrator)
        assertFalse(engine.capabilities.hasAmplitudeControl)
        assertFalse(engine.capabilities.supportsPrimitives)
    }

    @Test
    fun `play never claims a real vibration occurred`() {
        val engine = DebugHapticEngine()
        val played = engine.play(HapticPattern.TEST_PULSE)
        assertFalse(played)
    }

    @Test
    fun `play records the pattern as the active pulse for debug rendering`() {
        val engine = DebugHapticEngine()
        engine.play(HapticPattern.TEST_PULSE, intensity = 0.5f)

        val pulse = engine.activePulse.value
        assertNotNull(pulse)
        assertEquals(HapticPattern.TEST_PULSE.id, pulse?.patternId)
        assertEquals(0.5f, pulse?.intensity)
    }

    @Test
    fun `stop clears the active pulse`() {
        val engine = DebugHapticEngine()
        engine.play(HapticPattern.TEST_PULSE)
        engine.stop()
        assertNull(engine.activePulse.value)
    }

    @Test
    fun `intensity outside 0 to 1 is clamped in the recorded pulse`() {
        val engine = DebugHapticEngine()
        engine.play(HapticPattern.TEST_PULSE, intensity = 5f)
        assertEquals(1f, engine.activePulse.value?.intensity)
    }
}
