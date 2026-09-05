package com.anchor.domain.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionStateMachineTest {

    private lateinit var machine: SessionStateMachine

    @Before
    fun setUp() {
        machine = SessionStateMachine()
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(SessionState.IDLE, machine.currentState)
    }

    @Test
    fun `IDLE to ACTIVATING via start`() {
        val result = machine.start()
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.ACTIVATING, machine.currentState)
    }

    @Test
    fun `ACTIVATING to GROUNDING via beginGrounding`() {
        machine.start()
        val result = machine.beginGrounding()
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.GROUNDING, machine.currentState)
    }

    @Test
    fun `GROUNDING to EASING via finishGrounding`() {
        machine.start()
        machine.beginGrounding()
        val result = machine.finishGrounding()
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.EASING, machine.currentState)
    }

    @Test
    fun `EASING to CHECK_IN via completeEasing`() {
        machine.start()
        machine.beginGrounding()
        machine.finishGrounding()
        val result = machine.completeEasing()
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.CHECK_IN, machine.currentState)
    }

    @Test
    fun `CHECK_IN plus BETTER moves to RECOVERY`() {
        driveToCheckIn()
        val result = machine.submitCheckIn(CheckInResponse.BETTER)
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.RECOVERY, machine.currentState)
    }

    @Test
    fun `CHECK_IN plus SAME returns to GROUNDING`() {
        driveToCheckIn()
        val result = machine.submitCheckIn(CheckInResponse.SAME)
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.GROUNDING, machine.currentState)
    }

    @Test
    fun `CHECK_IN plus WORSE returns to GROUNDING and triggers nothing else`() {
        driveToCheckIn()
        val result = machine.submitCheckIn(CheckInResponse.WORSE)
        assertTrue(result is TransitionResult.Success)
        // WORSE behaves exactly like SAME at the state-machine level: a plain
        // transition back to GROUNDING. No emergency/contact/diagnosis side
        // effect exists to test, because none exists in this class.
        assertEquals(SessionState.GROUNDING, machine.currentState)
    }

    @Test
    fun `RECOVERY to IDLE via finishRecovery`() {
        driveToCheckIn()
        machine.submitCheckIn(CheckInResponse.BETTER)
        val result = machine.finishRecovery()
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.IDLE, machine.currentState)
    }

    @Test
    fun `GROUNDING cancellation returns to IDLE`() {
        machine.start()
        machine.beginGrounding()
        val result = machine.cancel()
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.IDLE, machine.currentState)
    }

    @Test
    fun `invalid IDLE to CHECK_IN is rejected`() {
        // completeEasing()'s target is CHECK_IN; calling it from IDLE is the
        // "IDLE -> CHECK_IN" case the M4 spec calls out as invalid.
        val result = machine.completeEasing()
        assertTrue(result is TransitionResult.Rejected)
        assertEquals("IDLE → CHECK_IN", (result as TransitionResult.Rejected).reason)
        assertEquals(SessionState.IDLE, machine.currentState)
    }

    @Test
    fun `invalid IDLE to RECOVERY is rejected`() {
        // submitCheckIn(BETTER)'s target is RECOVERY; calling it from IDLE is
        // the "IDLE -> RECOVERY" case the M4 spec calls out as invalid.
        val result = machine.submitCheckIn(CheckInResponse.BETTER)
        assertTrue(result is TransitionResult.Rejected)
        assertEquals("IDLE → RECOVERY", (result as TransitionResult.Rejected).reason)
        assertEquals(SessionState.IDLE, machine.currentState)
    }

    @Test
    fun `invalid GROUNDING to RECOVERY is rejected`() {
        machine.start()
        machine.beginGrounding()
        // Same target-method trick as above, this time attempted from GROUNDING.
        val result = machine.submitCheckIn(CheckInResponse.BETTER)
        assertTrue(result is TransitionResult.Rejected)
        assertEquals("GROUNDING → RECOVERY", (result as TransitionResult.Rejected).reason)
        assertEquals(SessionState.GROUNDING, machine.currentState)
    }

    @Test
    fun `a rejected transition leaves state completely unchanged`() {
        machine.start()
        val before = machine.currentState
        machine.cancel() // cancel is only valid from GROUNDING; we're in ACTIVATING
        assertEquals(before, machine.currentState)
    }

    @Test
    fun `repeated start does not create another session`() {
        val first = machine.start()
        val second = machine.start()
        assertTrue(first is TransitionResult.Success)
        assertTrue(second is TransitionResult.Rejected)
        assertEquals(SessionState.ACTIVATING, machine.currentState)
    }

    @Test
    fun `repeated check-in submission cannot produce an invalid state`() {
        driveToCheckIn()
        val first = machine.submitCheckIn(CheckInResponse.BETTER)
        val second = machine.submitCheckIn(CheckInResponse.BETTER) // already in RECOVERY now
        assertTrue(first is TransitionResult.Success)
        assertTrue(second is TransitionResult.Rejected)
        assertEquals(SessionState.RECOVERY, machine.currentState)
    }

    private fun driveToCheckIn() {
        machine.start()
        machine.beginGrounding()
        machine.finishGrounding()
        machine.completeEasing()
    }
}
