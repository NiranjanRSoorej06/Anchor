package com.anchor.domain.content

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SleepEducation].
 *
 * Pure-Kotlin — no Android dependencies. Guards the framing constraints
 * from `docs/exercise-evidence.md`: sleep content is psychoeducation, and
 * imagery rehearsal therapy is routed to a professional, never delivered.
 */
class SleepEducationTest {

    @Test
    fun introAndDisclaimerAreNonBlank() {
        assertTrue(SleepEducation.INTRO.isNotBlank())
        assertTrue(SleepEducation.DISCLAIMER.isNotBlank())
        assertTrue(SleepEducation.NIGHTMARE_NOTE.isNotBlank())
    }

    @Test
    fun cbtIPointsAreNonBlankAndPresent() {
        assertTrue("expected several points", SleepEducation.CBT_I_POINTS.size >= 3)
        SleepEducation.CBT_I_POINTS.forEach {
            assertTrue("point should be non-blank: '$it'", it.isNotBlank())
        }
    }

    @Test
    fun namesCbtIAsTheFirstLineTreatment() {
        val hay = (SleepEducation.INTRO + SleepEducation.CBT_I_POINTS.joinToString(" ")).lowercase()
        assertTrue("should mention CBT-I", hay.contains("cbt-i"))
        assertTrue("should mention 'first-line'", hay.contains("first-line"))
    }

    @Test
    fun nightmareNoteRoutesToAProfessionalRatherThanDeliveringIrt() {
        val note = SleepEducation.NIGHTMARE_NOTE.lowercase()
        assertTrue("should mention imagery rehearsal", note.contains("imagery rehearsal"))
        assertTrue("should route to support", note.contains("support") || note.contains("clinician") || note.contains("professional"))
        assertTrue("should say Anchor can't provide it", note.contains("can't") || note.contains("cannot"))
    }

    @Test
    fun disclaimerDisclaimsDiagnosisAndTreatment() {
        val d = SleepEducation.DISCLAIMER.lowercase()
        assertTrue(d.contains("not medical advice") || d.contains("educational"))
        assertTrue(d.contains("diagnose") || d.contains("treat"))
    }
}
