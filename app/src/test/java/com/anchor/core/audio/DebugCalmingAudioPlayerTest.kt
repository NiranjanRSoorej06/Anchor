package com.anchor.core.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugCalmingAudioPlayerTest {

    @Test
    fun `starts not playing`() {
        val player = DebugCalmingAudioPlayer()
        assertFalse(player.isPlaying)
    }

    @Test
    fun `play sets isPlaying true and records asset name`() {
        val player = DebugCalmingAudioPlayer()
        player.play("calm_rain.ogg")

        assertTrue(player.isPlaying)
        assertEquals(1, player.playedAssets.size)
        assertEquals("calm_rain.ogg", player.playedAssets[0])
    }

    @Test
    fun `stop sets isPlaying false`() {
        val player = DebugCalmingAudioPlayer()
        player.play("calm_rain.ogg")
        player.stop()

        assertFalse(player.isPlaying)
    }

    @Test
    fun `stop without play is safe`() {
        val player = DebugCalmingAudioPlayer()
        player.stop()
        assertFalse(player.isPlaying)
    }

    @Test
    fun `multiple play calls record all assets in order`() {
        val player = DebugCalmingAudioPlayer()
        player.play("first.ogg")
        player.play("second.ogg")
        player.play("third.ogg")

        assertEquals(listOf("first.ogg", "second.ogg", "third.ogg"), player.playedAssets)
        assertTrue(player.isPlaying)
    }

    @Test
    fun `playedAssets list persists after stop for test assertions`() {
        val player = DebugCalmingAudioPlayer()
        player.play("sound_a.ogg")
        player.play("sound_b.ogg")
        player.stop()

        // List survives stop so tests can inspect what was requested
        assertEquals(2, player.playedAssets.size)
        assertEquals("sound_a.ogg", player.playedAssets[0])
        assertEquals("sound_b.ogg", player.playedAssets[1])
    }

    @Test
    fun `stop then play resumes playing state`() {
        val player = DebugCalmingAudioPlayer()
        player.play("a.ogg")
        player.stop()
        assertFalse(player.isPlaying)

        player.play("b.ogg")
        assertTrue(player.isPlaying)
    }
}
