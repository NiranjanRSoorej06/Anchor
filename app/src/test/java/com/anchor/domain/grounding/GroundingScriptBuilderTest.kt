package com.anchor.domain.grounding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroundingScriptBuilderTest {

    @Test
    fun `empty labels returns fallback script`() {
        val result = GroundingScriptBuilder.build(emptyList())
        assertTrue(result.isFallback)
        assertTrue(result.sentences.isNotEmpty())
    }

    @Test
    fun `labels that are all person-related return fallback script`() {
        val result = GroundingScriptBuilder.build(listOf("Person", "Face", "Selfie"))
        assertTrue("all-person-label input should fall back", result.isFallback)
    }

    @Test
    fun `duplicate labels are deduplicated case-insensitively`() {
        val result = GroundingScriptBuilder.build(listOf("Chair", "chair", "CHAIR"))
        assertEquals(1, result.sentences.size)
        assertTrue(!result.isFallback)
    }

    @Test
    fun `more than MAX_SENTENCES labels are capped`() {
        val labels = listOf("Chair", "Window", "Lamp", "Table", "Door", "Plant", "Rug")
        val result = GroundingScriptBuilder.build(labels)
        assertEquals(GroundingScriptBuilder.MAX_SENTENCES, result.sentences.size)
        assertTrue(!result.isFallback)
    }

    @Test
    fun `normal labels produce calm sentences mentioning each object`() {
        val result = GroundingScriptBuilder.build(listOf("Chair", "Window"))
        assertEquals(2, result.sentences.size)
        assertTrue(result.sentences[0].contains("chair", ignoreCase = true))
        assertTrue(result.sentences[1].contains("window", ignoreCase = true))
        assertTrue(!result.isFallback)
    }

    @Test
    fun `person labels are filtered out but other labels in the same list survive`() {
        val result = GroundingScriptBuilder.build(listOf("Person", "Chair"))
        assertEquals(1, result.sentences.size)
        assertTrue(result.sentences[0].contains("chair", ignoreCase = true))
        assertTrue(!result.isFallback)
    }

    @Test
    fun `blank and whitespace-only labels are ignored`() {
        val result = GroundingScriptBuilder.build(listOf("", "   ", "Chair"))
        assertEquals(1, result.sentences.size)
        assertTrue(!result.isFallback)
    }
}
