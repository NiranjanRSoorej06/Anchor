package com.anchor.domain.support

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [PinRegions].
 *
 * Covers:
 * - Table integrity (non-empty, unique prefixes, all 2-digit format)
 * - Known pincodes route to the correct state/UT
 * - Invalid / unmapped inputs return `null`
 * - Whitespace trimming
 */
class PinRegionsTest {

    // ── Table integrity ──────────────────────────────────────────────

    @Test
    fun `ALL is non-empty`() {
        assertTrue("ALL should contain at least one entry", PinRegions.ALL.isNotEmpty())
    }

    @Test
    fun `all prefixes are unique`() {
        val prefixes = PinRegions.ALL.map { it.prefix2 }
        assertEquals(
            "Prefixes must be unique; found duplicates",
            prefixes.size,
            prefixes.toSet().size,
        )
    }

    @Test
    fun `all prefixes are exactly two digits`() {
        PinRegions.ALL.forEach { region ->
            assertEquals(
                "prefix2 must be exactly 2 characters, was: '${region.prefix2}'",
                2,
                region.prefix2.length,
            )
            assertTrue(
                "prefix2 must contain only digits, was: '${region.prefix2}'",
                region.prefix2.all { it in '0'..'9' },
            )
        }
    }

    // ── Known pincodes ──────────────────────────────────────────────

    @Test
    fun `110001 maps to Delhi`() {
        assertEquals("Delhi", PinRegions.stateForPincode("110001"))
    }

    @Test
    fun `560001 maps to Karnataka`() {
        assertEquals("Karnataka", PinRegions.stateForPincode("560001"))
    }

    @Test
    fun `600001 maps to Tamil Nadu`() {
        assertEquals("Tamil Nadu", PinRegions.stateForPincode("600001"))
    }

    @Test
    fun `781001 maps to Assam and Northeast States`() {
        assertEquals(
            "Assam and Northeast States",
            PinRegions.stateForPincode("781001"),
        )
    }

    @Test
    fun `194101 maps to Jammu and Kashmir`() {
        assertEquals(
            "Jammu and Kashmir",
            PinRegions.stateForPincode("194101"),
        )
    }

    @Test
    fun `900001 maps to Army Postal Service`() {
        assertEquals(
            "Army Postal Service",
            PinRegions.stateForPincode("900001"),
        )
    }

    // ── Invalid input → null ─────────────────────────────────────────

    @Test
    fun `5-digit pincode returns null`() {
        assertNull(PinRegions.stateForPincode("11001"))
    }

    @Test
    fun `letters return null`() {
        assertNull(PinRegions.stateForPincode("abcdef"))
    }

    @Test
    fun `blank string returns null`() {
        assertNull(PinRegions.stateForPincode(""))
    }

    @Test
    fun `null input returns null`() {
        assertNull(PinRegions.stateForPincode(null))
    }

    // ── Unmapped prefixes → null ─────────────────────────────────────

    @Test
    fun `unmapped prefix 00 returns null`() {
        assertNull(PinRegions.stateForPincode("000001"))
    }

    @Test
    fun `unmapped prefix 29 returns null`() {
        assertNull(PinRegions.stateForPincode("290001"))
    }

    @Test
    fun `unmapped prefix 55 returns null`() {
        assertNull(PinRegions.stateForPincode("550001"))
    }

    @Test
    fun `unmapped prefix 65 returns null`() {
        assertNull(PinRegions.stateForPincode("650001"))
    }

    @Test
    fun `unmapped prefix 86 returns null`() {
        assertNull(PinRegions.stateForPincode("860001"))
    }

    @Test
    fun `unmapped prefix 88 returns null`() {
        assertNull(PinRegions.stateForPincode("880001"))
    }

    // ── Whitespace trimming ──────────────────────────────────────────

    @Test
    fun `leading and trailing whitespace is trimmed`() {
        assertEquals("Delhi", PinRegions.stateForPincode("  110001  "))
        assertEquals("Karnataka", PinRegions.stateForPincode("\t560001\n"))
    }

    // ── Regression: PinRegions state → SupportDirectory region match ──

    @Test
    fun `Kashmir pincode resolves to SupportDirectory Kashmir Lifeline`() {
        val state = PinRegions.stateForPincode("194101")!!
        val entries = SupportDirectory.forRegion(state)
        val ids = entries.map { it.id }
        assertTrue(
            "forRegion(\"$state\") must contain kashmir_lifeline but was: $ids",
            ids.contains("kashmir_lifeline"),
        )
    }

    @Test
    fun `Karnataka pincode resolves to SupportDirectory NIMHANS`() {
        val state = PinRegions.stateForPincode("560001")!!
        val entries = SupportDirectory.forRegion(state)
        val ids = entries.map { it.id }
        assertTrue(
            "forRegion(\"$state\") must contain nimhans_helpline but was: $ids",
            ids.contains("nimhans_helpline"),
        )
    }
}
