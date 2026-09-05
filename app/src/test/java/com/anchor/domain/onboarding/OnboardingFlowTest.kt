package com.anchor.domain.onboarding

import com.anchor.domain.session.TransitionResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OnboardingFlowTest {

    private lateinit var flow: OnboardingFlow

    @Before
    fun setUp() {
        flow = OnboardingFlow()
    }

    // ── 1. Initial state ───────────────────────────────────────────────

    @Test
    fun `initial state is WELCOME`() {
        assertEquals(OnboardingStep.WELCOME, flow.currentStep)
        assertFalse(flow.isComplete)
        assertTrue(flow.completedSections.isEmpty())
    }

    // ── 2. Full complete() walk WELCOME → DONE ─────────────────────────

    @Test
    fun `full complete walk records all completable steps and reaches DONE`() {
        // Walk through every step via completeCurrent().
        // completeCurrent from WELCOME through PHRASE are the 6 completable steps;
        // finish() handles CONTACT → DONE and records CONTACT.
        assertTrue(flow.completeCurrent() is TransitionResult.Success) // WELCOME → CONSENT
        assertTrue(flow.completeCurrent() is TransitionResult.Success) // CONSENT → PCL5
        assertTrue(flow.completeCurrent() is TransitionResult.Success) // PCL5 → SITUATION
        assertTrue(flow.completeCurrent() is TransitionResult.Success) // SITUATION → SENSORY
        assertTrue(flow.completeCurrent() is TransitionResult.Success) // SENSORY → PHRASE
        assertTrue(flow.completeCurrent() is TransitionResult.Success) // PHRASE → CONTACT
        assertTrue(flow.finish() is TransitionResult.Success)          // CONTACT → DONE

        assertEquals(OnboardingStep.DONE, flow.currentStep)
        assertTrue(flow.isComplete)

        // All 7 steps (6 via completeCurrent + CONTACT via finish) recorded.
        val expected = setOf(
            OnboardingStep.WELCOME,
            OnboardingStep.CONSENT,
            OnboardingStep.PCL5,
            OnboardingStep.SITUATION,
            OnboardingStep.SENSORY,
            OnboardingStep.PHRASE,
            OnboardingStep.CONTACT
        )
        assertEquals(expected, flow.completedSections)
    }

    // ── 3. Skip path records nothing ──────────────────────────────────

    @Test
    fun `skip path advances without recording`() {
        assertTrue(flow.skipCurrent() is TransitionResult.Success) // WELCOME skipped → CONSENT
        assertTrue(flow.skipCurrent() is TransitionResult.Success) // CONSENT skipped → PCL5
        assertTrue(flow.skipCurrent() is TransitionResult.Success) // PCL5 skipped → SITUATION

        assertEquals(OnboardingStep.SITUATION, flow.currentStep)
        assertTrue(flow.completedSections.isEmpty())
    }

    // ── 4. skipCurrent from WELCOME rejected ──────────────────────────

    @Test
    fun `skipCurrent from WELCOME is rejected`() {
        val result = flow.skipCurrent()
        assertTrue(result is TransitionResult.Rejected)
        assertEquals(OnboardingStep.WELCOME, flow.currentStep)
    }

    // ── 5. completeCurrent from DONE rejected ──────────────────────────

    @Test
    fun `completeCurrent from DONE is rejected`() {
        driveToDone()
        val result = flow.completeCurrent()
        assertTrue(result is TransitionResult.Rejected)
        assertEquals(OnboardingStep.DONE, flow.currentStep)
    }

    // ── 6. finish only from CONTACT ───────────────────────────────────

    @Test
    fun `finish from WELCOME is rejected`() {
        val result = flow.finish()
        assertTrue(result is TransitionResult.Rejected)
        assertEquals(OnboardingStep.WELCOME, flow.currentStep)
    }

    @Test
    fun `finish from CONSENT is rejected`() {
        flow.skipCurrent() // WELCOME → CONSENT
        val result = flow.finish()
        assertTrue(result is TransitionResult.Rejected)
        assertEquals(OnboardingStep.CONSENT, flow.currentStep)
    }

    @Test
    fun `finish from DONE is rejected`() {
        driveToDone()
        val result = flow.finish()
        assertTrue(result is TransitionResult.Rejected)
        assertEquals(OnboardingStep.DONE, flow.currentStep)
    }

    @Test
    fun `finish from CONTACT succeeds`() {
        driveTo(OnboardingStep.CONTACT)
        val result = flow.finish()
        assertTrue(result is TransitionResult.Success)
        assertEquals(OnboardingStep.DONE, flow.currentStep)
        assertTrue(flow.completedSections.contains(OnboardingStep.CONTACT))
    }

    // ── 7. restart clears + returns WELCOME ───────────────────────────

    @Test
    fun `restart from DONE clears completedSections and returns WELCOME`() {
        // Record a few steps first.
        flow.completeCurrent() // WELCOME → CONSENT
        flow.completeCurrent() // CONSENT → PCL5
        assertEquals(setOf(OnboardingStep.WELCOME, OnboardingStep.CONSENT), flow.completedSections)

        driveToDone()
        assertTrue(flow.isComplete)

        val result = flow.restart()
        assertTrue(result is TransitionResult.Success)
        assertEquals(OnboardingStep.WELCOME, flow.currentStep)
        assertFalse(flow.isComplete)
        assertTrue(flow.completedSections.isEmpty())
    }

    @Test
    fun `restart from non-DONE is rejected`() {
        val result = flow.restart()
        assertTrue(result is TransitionResult.Rejected)
        assertEquals(OnboardingStep.WELCOME, flow.currentStep)
    }

    // ── 8. Double complete safety ─────────────────────────────────────

    @Test
    fun `two successive completeCurrent calls each advance one step`() {
        val first = flow.completeCurrent()  // WELCOME → CONSENT
        val second = flow.completeCurrent() // CONSENT → PCL5

        assertTrue(first is TransitionResult.Success)
        assertTrue(second is TransitionResult.Success)
        assertEquals(OnboardingStep.PCL5, flow.currentStep)
    }

    // ── 9. State flow emits ───────────────────────────────────────────

    @Test
    fun `state flow emits current step`() = runBlocking {
        // The StateFlow should immediately emit the initial value.
        val initial = flow.state.first()
        assertEquals(OnboardingStep.WELCOME, initial)

        // After a transition the flow emits the new value.
        flow.completeCurrent()
        val after = flow.state.first()
        assertEquals(OnboardingStep.CONSENT, after)
    }

    // ── 10. Rejected transition leaves state unchanged ────────────────

    @Test
    fun `rejected transition leaves state completely unchanged`() {
        val before = flow.currentStep
        val beforeSections = flow.completedSections
        flow.completeCurrent() // WELCOME → CONSENT (succeeds)
        flow.restart()         // CONSENT → WELCOME (rejected, not DONE)
        // State should still be CONSENT from the first call, not reset.
        assertEquals(OnboardingStep.CONSENT, flow.currentStep)
        assertEquals(setOf(OnboardingStep.WELCOME), flow.completedSections)
    }

    // ── Helpers ────────────────────────────────────────────────────────

    /** Drive the machine to [OnboardingStep.DONE] via the happy path. */
    private fun driveToDone() {
        flow.completeCurrent() // WELCOME → CONSENT
        flow.completeCurrent() // CONSENT → PCL5
        flow.completeCurrent() // PCL5 → SITUATION
        flow.completeCurrent() // SITUATION → SENSORY
        flow.completeCurrent() // SENSORY → PHRASE
        flow.completeCurrent() // PHRASE → CONTACT
        flow.finish()          // CONTACT → DONE
    }

    /**
     * Syntactic sugar for tests that need to be at a specific step
     * without caring how they got there.
     */
    private fun driveTo(step: OnboardingStep) {
        val path = listOf(
            OnboardingStep.WELCOME,
            OnboardingStep.CONSENT,
            OnboardingStep.PCL5,
            OnboardingStep.SITUATION,
            OnboardingStep.SENSORY,
            OnboardingStep.PHRASE,
            OnboardingStep.CONTACT,
            OnboardingStep.DONE
        )
        val targetIndex = path.indexOf(step)
        require(targetIndex >= 0) { "Unknown step: $step" }
        for (i in 0 until targetIndex) {
            flow.completeCurrent()
        }
    }
}
