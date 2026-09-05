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
    fun `CHECK_IN plus SAME moves to ROUTING`() {
        driveToCheckIn()
        val result = machine.submitCheckIn(CheckInResponse.SAME)
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.ROUTING, machine.currentState)
    }

    @Test
    fun `CHECK_IN plus WORSE moves to SAFETY_STOP and triggers nothing else`() {
        driveToCheckIn()
        val result = machine.submitCheckIn(CheckInResponse.WORSE)
        assertTrue(result is TransitionResult.Success)
        // Reaching SAFETY_STOP is the only effect. No emergency/contact/
        // diagnosis side effect exists to test, because none exists in this
        // class — see acknowledgeSafetyStop's own tests below for proof
        // nothing carries the session further without an explicit call.
        assertEquals(SessionState.SAFETY_STOP, machine.currentState)
    }

    @Test
    fun `ROUTING to INTERVENTION via beginIntervention`() {
        driveToCheckIn()
        machine.submitCheckIn(CheckInResponse.SAME) // -> ROUTING
        val result = machine.beginIntervention()
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.INTERVENTION, machine.currentState)
    }

    @Test
    fun `INTERVENTION to EASING via finishIntervention`() {
        driveToCheckIn()
        machine.submitCheckIn(CheckInResponse.SAME) // -> ROUTING
        machine.beginIntervention() // -> INTERVENTION
        val result = machine.finishIntervention()
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.EASING, machine.currentState)
    }

    @Test
    fun `full SAME loop returns to CHECK_IN via the retry path`() {
        driveToCheckIn()
        assertTrue(machine.submitCheckIn(CheckInResponse.SAME) is TransitionResult.Success)
        assertTrue(machine.beginIntervention() is TransitionResult.Success)
        assertTrue(machine.finishIntervention() is TransitionResult.Success)
        assertTrue(machine.completeEasing() is TransitionResult.Success)
        assertEquals(SessionState.CHECK_IN, machine.currentState)
    }

    @Test
    fun `SAFETY_STOP to IDLE via acknowledgeSafetyStop, never through RECOVERY`() {
        driveToCheckIn()
        machine.submitCheckIn(CheckInResponse.WORSE) // -> SAFETY_STOP
        assertEquals(SessionState.SAFETY_STOP, machine.currentState)
        val result = machine.acknowledgeSafetyStop()
        assertTrue(result is TransitionResult.Success)
        assertEquals(SessionState.IDLE, machine.currentState)
    }

    @Test
    fun `SAFETY_STOP never advances on its own`() {
        driveToCheckIn()
        machine.submitCheckIn(CheckInResponse.WORSE) // -> SAFETY_STOP
        // No method other than acknowledgeSafetyStop can move out of SAFETY_STOP.
        assertTrue(machine.start() is TransitionResult.Rejected)
        assertTrue(machine.beginGrounding() is TransitionResult.Rejected)
        assertTrue(machine.finishRecovery() is TransitionResult.Rejected)
        assertEquals(SessionState.SAFETY_STOP, machine.currentState)
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
    fun `invalid IDLE to INTERVENTION is rejected`() {
        val result = machine.beginIntervention()
        assertTrue(result is TransitionResult.Rejected)
        assertEquals("IDLE → INTERVENTION", (result as TransitionResult.Rejected).reason)
        assertEquals(SessionState.IDLE, machine.currentState)
    }

    @Test
    fun `acknowledgeSafetyStop is rejected from any state other than SAFETY_STOP`() {
        // acknowledgeSafetyStop's own target is IDLE, so testing rejection
        // from IDLE would read as the confusing "IDLE -> IDLE" (the same
        // trap fixed for the debug screen in M4 — see the reference doc).
        // ACTIVATING makes the guard's behavior legible instead.
        machine.start() // -> ACTIVATING
        val result = machine.acknowledgeSafetyStop()
        assertTrue(result is TransitionResult.Rejected)
        assertEquals("ACTIVATING → IDLE", (result as TransitionResult.Rejected).reason)
        assertEquals(SessionState.ACTIVATING, machine.currentState)
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
