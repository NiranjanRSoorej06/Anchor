package com.anchor.domain.support

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportDirectoryTest {

    // ── Catalogue integrity ─────────────────────────────────────────────

    @Test
    fun `ALL contains exactly 9 entries`() {
        assertEquals(9, SupportDirectory.ALL.size)
    }

    @Test
    fun `every entry has a unique id`() {
        val ids = SupportDirectory.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `Tele MANAS entry has short code 14416`() {
        val teleManas = SupportDirectory.ALL.first { it.id == "tele_manas" }
        assertEquals("14416", teleManas.phoneNumber)
        assertEquals("Tele MANAS", teleManas.name)
        assertEquals(setOf(SupportDirectory.NATIONWIDE), teleManas.regions)
        assertEquals("24/7", teleManas.hours)
        assertTrue(teleManas.languages.size >= 10)
    }

    // ── Field constraints ───────────────────────────────────────────────

    @Test
    fun `all phone entries have non-blank phoneNumber`() {
        SupportDirectory.ALL
            .filter { it.category != SupportCategory.THERAPIST_DIRECTORY }
            .forEach { entry ->
                assertNotNull("Phone entry ${entry.id} must have phoneNumber", entry.phoneNumber)
                assertTrue("Phone entry ${entry.id} phoneNumber must not be blank", entry.phoneNumber!!.isNotBlank())
            }
    }

    @Test
    fun `all entries have non-empty regions`() {
        SupportDirectory.ALL.forEach { entry ->
            assertTrue("Entry ${entry.id} must have non-empty regions", entry.regions.isNotEmpty())
        }
    }

    @Test
    fun `therapist directory entry is onlineOnly with null phone`() {
        val therapist = SupportDirectory.ALL.first { it.id == "therapist_directory" }
        assertTrue(therapist.onlineOnly)
        assertNull(therapist.phoneNumber)
        assertNotNull(therapist.url)
        assertTrue(therapist.url!!.startsWith("http"))
        assertEquals(setOf(SupportDirectory.NATIONWIDE), therapist.regions)
        assertEquals(SupportCategory.THERAPIST_DIRECTORY, therapist.category)
    }

    @Test
    fun `emergency 112 has EMERGENCY category`() {
        val emergency = SupportDirectory.ALL.first { it.id == "emergency_112" }
        assertEquals(SupportCategory.EMERGENCY, emergency.category)
        assertEquals("112", emergency.phoneNumber)
    }

    // ── forRegion: null / blank / unknown queries ───────────────────────

    @Test
    fun `null query returns ALL in default order`() {
        val result = SupportDirectory.forRegion(null)
        assertEquals(SupportDirectory.ALL, result)
    }

    @Test
    fun `blank query returns ALL in default order`() {
        assertEquals(SupportDirectory.ALL, SupportDirectory.forRegion("   "))
        assertEquals(SupportDirectory.ALL, SupportDirectory.forRegion(""))
    }

    @Test
    fun `unknown state returns ALL in default order`() {
        assertEquals(SupportDirectory.ALL, SupportDirectory.forRegion("Atlantis"))
    }

    // ── forRegion: regional matches ─────────────────────────────────────

    @Test
    fun `Tamil Nadu query puts Sneha first`() {
        val result = SupportDirectory.forRegion("Tamil Nadu")
        assertEquals("sneha_chennai", result.first().id)

        // Remaining should be nationwide entries then directory, no duplicates
        assertEquals(SupportDirectory.ALL.size, result.size)
    }

    @Test
    fun `Jammu and Kashmir query puts Kashmir Lifeline first`() {
        val result = SupportDirectory.forRegion("Jammu and Kashmir")
        assertEquals("kashmir_lifeline", result.first().id)
        assertEquals(SupportDirectory.ALL.size, result.size)
    }

    @Test
    fun `Karnataka query puts NIMHANS first`() {
        val result = SupportDirectory.forRegion("Karnataka")
        assertEquals("nimhans_helpline", result.first().id)
        assertEquals(SupportDirectory.ALL.size, result.size)
    }

    @Test
    fun `regional query never drops nationwide entries`() {
        val result = SupportDirectory.forRegion("Tamil Nadu")
        val nationwideIds = SupportDirectory.ALL
            .filter { it.regions.contains(SupportDirectory.NATIONWIDE) }
            .map { it.id }
        assertTrue(result.map { it.id }.containsAll(nationwideIds))
    }

    @Test
    fun `therapist directory always appears last`() {
        val queries = listOf(null, "Tamil Nadu", "Karnataka", "Jammu and Kashmir", "Unknown")
        for (query in queries) {
            val result = SupportDirectory.forRegion(query)
            assertEquals(
                "therapist_directory must be last for query=$query",
                "therapist_directory",
                result.last().id,
            )
        }
    }

    @Test
    fun `NIMHANS regions contain Karnataka`() {
        val nimhans = SupportDirectory.ALL.first { it.id == "nimhans_helpline" }
        assertTrue(nimhans.regions.contains("Karnataka"))
    }

    @Test
    fun `Sneha regions contain Tamil Nadu`() {
        val sneha = SupportDirectory.ALL.first { it.id == "sneha_chennai" }
        assertTrue(sneha.regions.contains("Tamil Nadu"))
    }

    @Test
    fun `Kashmir Lifeline regions contain Jammu and Kashmir`() {
        val kll = SupportDirectory.ALL.first { it.id == "kashmir_lifeline" }
        assertTrue(kll.regions.contains("Jammu and Kashmir"))
    }
}
