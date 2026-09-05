package com.anchor.core.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioDeliveryEngineTest {

    @Test
    fun `when earbuds disconnected whisper mode is inactive`() {
        val fakeDetector = FakeAudioOutputDetector(headsetConnected = false)
        assertFalse(fakeDetector.isPrivateHeadsetConnected())
    }

    @Test
    fun `when earbuds connected whisper mode is active`() {
        val fakeDetector = FakeAudioOutputDetector(headsetConnected = true)
        assertTrue(fakeDetector.isPrivateHeadsetConnected())
    }

    @Test
    fun `debug engine mutes speech when earbuds disconnected`() {
        val engine = DebugAudioEngine(earbudConnected = false)
        assertFalse(engine.isWhisperModeActive())

        engine.speakWhisper("Breathe In")
        assertTrue(engine.spokenPhrases.isEmpty())
    }

    @Test
    fun `debug engine records whisper speech when earbuds connected`() {
        val engine = DebugAudioEngine(earbudConnected = true)
        assertTrue(engine.isWhisperModeActive())

        engine.speakWhisper("Breathe In")
        engine.speakWhisper("Breathe Out")

        assertEquals(2, engine.spokenPhrases.size)
        assertEquals("Breathe In", engine.spokenPhrases[0])
        assertEquals("Breathe Out", engine.spokenPhrases[1])
    }

    @Test
    fun `stop clears spoken phrases`() {
        val engine = DebugAudioEngine(earbudConnected = true)
        engine.speakWhisper("Breathe In")
        assertEquals(1, engine.spokenPhrases.size)

        engine.stop()
        assertTrue(engine.spokenPhrases.isEmpty())
    }
}
