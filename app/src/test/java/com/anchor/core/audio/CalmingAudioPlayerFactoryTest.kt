package com.anchor.core.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalmingAudioPlayerFactoryTest {

    @Test
    fun `debug calming player implements CalmingAudioPlayer interface`() {
        // Verify the factory return type contract: createCalmingPlayer returns CalmingAudioPlayer.
        // We test against the debug variant since the production variant requires a real Context.
        val player: CalmingAudioPlayer = DebugCalmingAudioPlayer()
        assertFalse(player.isPlaying)
        player.play("test.ogg")
        assertTrue(player.isPlaying)
        player.stop()
        assertFalse(player.isPlaying)
    }

    @Test
    fun `debug calming player can swap for any CalmingAudioPlayer`() {
        // Prove the interface is the abstraction boundary:
        // code written against CalmingAudioPlayer works with the debug fake
        // and will work with the production MediaPlayerCalmingAudioPlayer.
        val player: CalmingAudioPlayer = DebugCalmingAudioPlayer()
        val assets = mutableListOf<String>()

        // Simulate the pattern callers will use with createCalmingPlayer
        fun playAndTrack(p: CalmingAudioPlayer, file: String) {
            p.play(file)
            if (p.isPlaying) assets.add(file)
        }

        playAndTrack(player, "calm_rain.ogg")
        playAndTrack(player, "brown_noise.ogg")

        assertEquals(listOf("calm_rain.ogg", "brown_noise.ogg"), assets)
        assertTrue(player.isPlaying)
    }
}
