package com.anchor.domain.support

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [MapsQueries].
 *
 * Covers:
 * - [MapsQueries.uriFor] returns a URI starting with [MapsQueries.BASE]
 * - spaces are encoded as `+` in the output
 * - all three presets produce distinct URIs
 * - empty query still produces a well-formed URI
 */
class MapsQueriesTest {

    @Test
    fun uriForStartsWithBase() {
        val uri = MapsQueries.uriFor("test query")
        assertTrue(
            "uriFor output should start with BASE, was: $uri",
            uri.startsWith(MapsQueries.BASE)
        )
    }

    @Test
    fun uriForEncodesSpaces() {
        val uri = MapsQueries.uriFor("hello world")
        assertFalse(
            "Spaces should be replaced with + in output: $uri",
            uri.contains(" ")
        )
        assertTrue(
            "Output should contain + for spaces: $uri",
            uri.contains("hello+world")
        )
    }

    @Test
    fun threePresetsProduceDistinctUris() {
        val clinicUri = MapsQueries.uriFor(MapsQueries.CLINIC)
        val psychUri = MapsQueries.uriFor(MapsQueries.PSYCHIATRIST)
        val counselUri = MapsQueries.uriFor(MapsQueries.COUNSELLING)

        assertNotEquals("CLINIC and PSYCHIATRIST URIs should differ", clinicUri, psychUri)
        assertNotEquals("PSYCHIATRIST and COUNSELLING URIs should differ", psychUri, counselUri)
        assertNotEquals("CLINIC and COUNSELLING URIs should differ", clinicUri, counselUri)
    }

    @Test
    fun emptyQueryProducesWellFormedUri() {
        val uri = MapsQueries.uriFor("")
        assertEquals("geo:0,0?q=", uri)
    }
}
