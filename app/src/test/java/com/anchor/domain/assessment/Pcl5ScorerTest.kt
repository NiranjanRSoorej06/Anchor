package com.anchor.domain.assessment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Pcl5ScorerTest {

    // ------------------------------------------------------------------
    // Catalog integrity
    // ------------------------------------------------------------------

    @Test
    fun `catalog contains exactly 20 items`() {
        assertEquals(20, Pcl5Catalog.ITEMS.size)
    }

    @Test
    fun `item numbers are 1 through 20 with no duplicates`() {
        val numbers = Pcl5Catalog.ITEMS.map { it.number }
        assertEquals((1..20).toList(), numbers)
    }

    @Test
    fun `cluster ranges are correct`() {
        val intrusion = Pcl5Catalog.ITEMS.filter { it.cluster == Pcl5Cluster.INTRUSION }.map { it.number }
        assertEquals(listOf(1, 2, 3, 4, 5), intrusion)

        val avoidance = Pcl5Catalog.ITEMS.filter { it.cluster == Pcl5Cluster.AVOIDANCE }.map { it.number }
        assertEquals(listOf(6, 7), avoidance)

        val negative = Pcl5Catalog.ITEMS.filter { it.cluster == Pcl5Cluster.NEGATIVE_ALTERATIONS }.map { it.number }
        assertEquals(listOf(8, 9, 10, 11, 12, 13, 14), negative)

        val arousal = Pcl5Catalog.ITEMS.filter { it.cluster == Pcl5Cluster.AROUSAL }.map { it.number }
        assertEquals(listOf(15, 16, 17, 18, 19, 20), arousal)
    }

    // ------------------------------------------------------------------
    // Scoring: boundary cases
    // ------------------------------------------------------------------

    @Test
    fun `all zeros yields total 0 and no cutoff`() {
        val responses = (1..20).map { Pcl5Response(itemNumber = it, score = 0) }
        val result = Pcl5Scorer.score(responses)
        assertTrue(result is Pcl5Result.Complete)
        result as Pcl5Result.Complete
        assertEquals(0, result.total)
        assertFalse(result.meetsProvisionalCutoff)
    }

    @Test
    fun `all fours yields total 80 and meets cutoff`() {
        val responses = (1..20).map { Pcl5Response(itemNumber = it, score = 4) }
        val result = Pcl5Scorer.score(responses)
        assertTrue(result is Pcl5Result.Complete)
        result as Pcl5Result.Complete
        assertEquals(80, result.total)
        assertTrue(result.meetsProvisionalCutoff)
    }

    @Test
    fun `total 30 does not meet cutoff`() {
        // 15 items at 2, 5 items at 0 -> total 30
        val responses = (1..20).map { Pcl5Response(itemNumber = it, score = if (it <= 15) 2 else 0) }
        val result = Pcl5Scorer.score(responses)
        assertTrue(result is Pcl5Result.Complete)
        result as Pcl5Result.Complete
        assertEquals(30, result.total)
        assertFalse(result.meetsProvisionalCutoff)
    }

    @Test
    fun `total 31 meets cutoff`() {
        // 15 items at 2, item 16 at 1 -> total 31
        val responses = (1..20).map { Pcl5Response(itemNumber = it, score = if (it <= 15) 2 else if (it == 16) 1 else 0) }
        val result = Pcl5Scorer.score(responses)
        assertTrue(result is Pcl5Result.Complete)
        result as Pcl5Result.Complete
        assertEquals(31, result.total)
        assertTrue(result.meetsProvisionalCutoff)
    }

    // ------------------------------------------------------------------
    // Scoring: incomplete
    // ------------------------------------------------------------------

    @Test
    fun `19 items yields incomplete with answeredCount 19`() {
        val responses = (1..19).map { Pcl5Response(itemNumber = it, score = 1) }
        val result = Pcl5Scorer.score(responses)
        assertTrue(result is Pcl5Result.Incomplete)
        result as Pcl5Result.Incomplete
        assertEquals(19, result.answeredCount)
    }

    @Test
    fun `empty list yields incomplete with answeredCount 0`() {
        val result = Pcl5Scorer.score(emptyList())
        assertTrue(result is Pcl5Result.Incomplete)
        result as Pcl5Result.Incomplete
        assertEquals(0, result.answeredCount)
    }

    // ------------------------------------------------------------------
    // Scoring: invalid/out-of-range values
    // ------------------------------------------------------------------

    @Test
    fun `out-of-range scores are dropped`() {
        // 20 valid responses, but one has score 5 and one has score -1
        val responses = (1..20).map { Pcl5Response(itemNumber = it, score = 1) }.toMutableList()
        responses[0] = Pcl5Response(itemNumber = 1, score = 5)  // out of range
        responses[1] = Pcl5Response(itemNumber = 2, score = -1) // out of range
        val result = Pcl5Scorer.score(responses)
        assertTrue(result is Pcl5Result.Incomplete)
        result as Pcl5Result.Incomplete
        assertEquals(18, result.answeredCount)
    }

    @Test
    fun `out-of-range item numbers are dropped`() {
        val responses = (1..20).map { Pcl5Response(itemNumber = it, score = 2) }.toMutableList()
        responses.add(Pcl5Response(itemNumber = 0, score = 3))   // invalid item
        responses.add(Pcl5Response(itemNumber = 21, score = 3))  // invalid item
        val result = Pcl5Scorer.score(responses)
        assertTrue(result is Pcl5Result.Complete)
        result as Pcl5Result.Complete
        assertEquals(40, result.total) // 20 items * 2
    }

    // ------------------------------------------------------------------
    // Scoring: duplicates
    // ------------------------------------------------------------------

    @Test
    fun `duplicate item numbers keep first occurrence`() {
        val responses = (1..20).map { Pcl5Response(itemNumber = it, score = 2) }.toMutableList()
        // Add a duplicate of item 1 with a different score
        responses.add(Pcl5Response(itemNumber = 1, score = 4))
        val result = Pcl5Scorer.score(responses)
        assertTrue(result is Pcl5Result.Complete)
        result as Pcl5Result.Complete
        // Item 1 keeps its first score of 2, not the duplicate 4.
        assertEquals(40, result.total) // 20 items * 2
    }

    // ------------------------------------------------------------------
    // Cluster totals
    // ------------------------------------------------------------------

    @Test
    fun `cluster totals are computed correctly`() {
        // Intrusion (1-5): all 1s -> 5
        // Avoidance (6-7): all 2s -> 4
        // Negative (8-14): all 3s -> 21
        // Arousal (15-20): all 0s -> 0
        // Total = 5 + 4 + 21 + 0 = 30
        val responses = (1..20).map { item ->
            val score = when {
                item <= 5 -> 1
                item <= 7 -> 2
                item <= 14 -> 3
                else -> 0
            }
            Pcl5Response(itemNumber = item, score = score)
        }
        val result = Pcl5Scorer.score(responses)
        assertTrue(result is Pcl5Result.Complete)
        result as Pcl5Result.Complete
        assertEquals(30, result.total)
        assertEquals(5, result.clusterTotals[Pcl5Cluster.INTRUSION])
        assertEquals(4, result.clusterTotals[Pcl5Cluster.AVOIDANCE])
        assertEquals(21, result.clusterTotals[Pcl5Cluster.NEGATIVE_ALTERATIONS])
        assertEquals(0, result.clusterTotals[Pcl5Cluster.AROUSAL])
    }
}
