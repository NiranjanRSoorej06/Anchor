package com.anchor.core.companion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionMessageComposerTest {

    @Test
    fun `returns base message unchanged when location is null`() {
        val result = CompanionMessageComposer.withLocation("Anchor Alert: they hit SOS.", null)
        assertEquals("Anchor Alert: they hit SOS.", result)
    }

    @Test
    fun `appends plain coordinates, not a link, when location is present`() {
        val location = LocationSnapshot(latitude = 12.9716, longitude = 77.5946)
        val result = CompanionMessageComposer.withLocation("Anchor Alert: they hit SOS.", location)

        assertTrue(result.startsWith("Anchor Alert: they hit SOS.\n"))
        assertTrue(result.endsWith("12.9716, 77.5946 (paste into Maps)"))
        // Regression guard for the carrier-blocked-URL bug: verified on-device
        // that any message containing a maps.google.com link showed "Not
        // sent," while a link-free message sent successfully — never
        // reintroduce a URL/link scheme here.
        assertFalse(result.contains("http"))
        assertFalse(result.contains("maps.google.com"))
    }

    @Test
    fun `negative coordinates round-trip exactly`() {
        val location = LocationSnapshot(latitude = -33.8688, longitude = 151.2093)
        val result = CompanionMessageComposer.withLocation("base", location)
        assertEquals("base\nLocation: -33.8688, 151.2093 (paste into Maps)", result)
    }

    @Test
    fun `bare 10-digit number gets India country code prepended`() {
        assertEquals("+919495371370", CompanionMessageComposer.normalizePhoneNumber("9495371370"))
    }

    @Test
    fun `number with surrounding whitespace is trimmed before normalizing`() {
        assertEquals("+919495371370", CompanionMessageComposer.normalizePhoneNumber("  9495371370  "))
    }

    @Test
    fun `number already carrying a country code is left untouched`() {
        assertEquals("+919495371370", CompanionMessageComposer.normalizePhoneNumber("+919495371370"))
    }

    @Test
    fun `non-10-digit or non-numeric input is left untouched`() {
        assertEquals("12345", CompanionMessageComposer.normalizePhoneNumber("12345"))
        assertEquals("94953-71370", CompanionMessageComposer.normalizePhoneNumber("94953-71370"))
    }
}
