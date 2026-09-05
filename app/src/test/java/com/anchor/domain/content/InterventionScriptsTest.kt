package com.anchor.domain.content

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterventionScriptsTest {

    @Test
    fun `every catalog id resolves to a non-empty script`() {
        InterventionCatalog.ALL.forEach { intervention ->
            val steps = InterventionScripts.forId(intervention.id)
            assertTrue("Expected steps for ${intervention.id}", steps.isNotEmpty())
        }
    }

    @Test
    fun `every step has non-blank text and positive duration`() {
        InterventionCatalog.ALL.forEach { intervention ->
            InterventionScripts.forId(intervention.id).forEach { step ->
                assertFalse("${intervention.id}: blank step text", step.text.isBlank())
                assertTrue("${intervention.id}: non-positive duration", step.durationSec > 0)
            }
        }
    }

    @Test
    fun `unknown id returns a non-empty fallback script`() {
        val steps = InterventionScripts.forId("NOT_A_REAL_ID")
        assertTrue(steps.isNotEmpty())
        steps.forEach { assertFalse(it.text.isBlank()) }
    }
}
