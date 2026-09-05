package com.anchor.domain.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [CopingStatements] and [SleepChecklist].
 *
 * Covers:
 * - per-category statement counts (5–7 each)
 * - all statements ≤ 140 characters and non-blank
 * - no duplicates across [CopingStatements.ALL]
 * - [SleepChecklist.ITEMS] count (8–10) and non-blank
 */
class StaticContentTest {

    private val maxLen = 140

    // ── CopingStatements ──────────────────────────────────────────────

    @Test
    fun safetyCountBetween5And7() {
        val count = CopingStatements.SAFETY.size
        assertTrue("SAFETY should have 5–7 items, has $count", count in 5..7)
    }

    @Test
    fun temporalCountBetween5And7() {
        val count = CopingStatements.TEMPORAL.size
        assertTrue("TEMPORAL should have 5–7 items, has $count", count in 5..7)
    }

    @Test
    fun capabilityCountBetween5And7() {
        val count = CopingStatements.CAPABILITY.size
        assertTrue("CAPABILITY should have 5–7 items, has $count", count in 5..7)
    }

    @Test
    fun allStatementsAreNonBlankAndWithin140Chars() {
        CopingStatements.ALL.forEach { stmt ->
            assertTrue("Statement should be non-blank: '$stmt'", stmt.isNotBlank())
            assertTrue(
                "Statement should be ≤ $maxLen chars (${stmt.length}): '$stmt'",
                stmt.length <= maxLen
            )
        }
    }

    @Test
    fun noDuplicatesAcrossAll() {
        val unique = CopingStatements.ALL.toSet()
        assertEquals(
            "ALL should contain no duplicates",
            CopingStatements.ALL.size,
            unique.size
        )
    }

    @Test
    fun allCategoriesAreCombinedIntoAll() {
        val expected = CopingStatements.SAFETY.size +
            CopingStatements.TEMPORAL.size +
            CopingStatements.CAPABILITY.size
        assertEquals("ALL should be the union of three categories", expected, CopingStatements.ALL.size)
    }

    // ── SleepChecklist ────────────────────────────────────────────────

    @Test
    fun sleepItemCountBetween8And10() {
        val count = SleepChecklist.ITEMS.size
        assertTrue("ITEMS should have 8–10 items, has $count", count in 8..10)
    }

    @Test
    fun sleepItemsAreNonBlank() {
        SleepChecklist.ITEMS.forEach { item ->
            assertTrue("Sleep item should be non-blank: '$item'", item.isNotBlank())
        }
    }

    @Test
    fun sleepItemsAreWithin140Chars() {
        SleepChecklist.ITEMS.forEach { item ->
            assertTrue(
                "Sleep item should be ≤ $maxLen chars (${item.length}): '$item'",
                item.length <= maxLen
            )
        }
    }
}
