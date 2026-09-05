package com.anchor.domain.routing

import com.anchor.domain.personalization.SessionOutcome
import com.anchor.domain.session.CheckInResponse
import com.anchor.domain.safety.CurrentState
import com.anchor.domain.safety.SafetyCandidate
import com.anchor.domain.safety.SafetyFilter
import com.anchor.domain.safety.SafetyProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InterventionRouterTest {

    // ── Reusable test fixtures ──────────────────────────────────────

    private val allStates = CurrentState.values().toSet()

    private val defaultProfile = SafetyProfile()

    private fun candidate(
        id: String,
        states: Set<CurrentState> = allStates,
        interoceptive: Boolean = false,
        hasTraumaImagery: Boolean = false,
        requiresAudio: Boolean = false,
        requiresVoice: Boolean = false,
        requiresHaptics: Boolean = false
    ) = SafetyCandidate(
        id = id,
        interoceptive = interoceptive,
        allowedStates = states,
        requiresAudio = requiresAudio,
        requiresVoice = requiresVoice,
        requiresHaptics = requiresHaptics,
        hasTraumaImagery = hasTraumaImagery
    )

    private fun outcome(routineId: String, response: CheckInResponse = CheckInResponse.BETTER) =
        SessionOutcome(
            sessionId = "s-${routineId}",
            routineId = routineId,
            response = response,
            timestampMillis = 0L
        )

    // ── 1. Vetoed: trauma imagery (SF1) ────────────────────────────

    @Test
    fun `trauma imagery candidates are excluded`() {
        val imagery = candidate("imagery", hasTraumaImagery = true)
        val safe = candidate("safe")

        val result = InterventionRouter.rank(
            candidates = listOf(imagery, safe),
            state = CurrentState.FROZEN,
            profile = defaultProfile,
            outcomes = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals("safe", result[0].id)
    }

    // ── 2. Vetoed: interoceptive during dissociation (SF2) ─────────

    @Test
    fun `interoceptive candidates are excluded during DISSOCIATION`() {
        val intero = candidate("intero", interoceptive = true)
        val external = candidate("external")

        val result = InterventionRouter.rank(
            candidates = listOf(intero, external),
            state = CurrentState.DISSOCIATION,
            profile = defaultProfile,
            outcomes = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals("external", result[0].id)
    }

    // ── 3. Vetoed: wrong state (SF6) ───────────────────────────────

    @Test
    fun `candidates not allowing the current state are excluded`() {
        val panickyOnly = candidate("panicky-only", states = setOf(CurrentState.PANICKY))
        val allOk = candidate("all-ok")

        val result = InterventionRouter.rank(
            candidates = listOf(panickyOnly, allOk),
            state = CurrentState.FROZEN,
            profile = defaultProfile,
            outcomes = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals("all-ok", result[0].id)
    }

    // ── 4. Vetoed: notForMe (SF8) ──────────────────────────────────

    @Test
    fun `candidates in notForMe set are excluded`() {
        val rejected = candidate("rejected")
        val accepted = candidate("accepted")
        val profile = SafetyProfile(notForMe = setOf("rejected"))

        val result = InterventionRouter.rank(
            candidates = listOf(rejected, accepted),
            state = CurrentState.FROZEN,
            profile = profile,
            outcomes = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals("accepted", result[0].id)
    }

    // ── 5. Vetoed: sensory mismatch (SF4) ──────────────────────────

    @Test
    fun `candidates requiring audio are excluded when audioOk is false`() {
        val needsAudio = candidate("needs-audio", requiresAudio = true)
        val noAudio = candidate("no-audio")
        val profile = SafetyProfile(audioOk = false)

        val result = InterventionRouter.rank(
            candidates = listOf(needsAudio, noAudio),
            state = CurrentState.FROZEN,
            profile = profile,
            outcomes = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals("no-audio", result[0].id)
    }

    @Test
    fun `candidates requiring haptics are excluded when touchSensitive is true`() {
        val needsHaptics = candidate("needs-haptics", requiresHaptics = true)
        val noHaptics = candidate("no-haptics")
        val profile = SafetyProfile(touchSensitive = true)

        val result = InterventionRouter.rank(
            candidates = listOf(needsHaptics, noHaptics),
            state = CurrentState.FROZEN,
            profile = profile,
            outcomes = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals("no-haptics", result[0].id)
    }

    // ── 6. alreadyTried excluded (plan.md:364) ──────────────────────

    @Test
    fun `candidates in alreadyTried are excluded`() {
        val tried = candidate("tried")
        val fresh = candidate("fresh")

        val result = InterventionRouter.rank(
            candidates = listOf(tried, fresh),
            state = CurrentState.FROZEN,
            profile = defaultProfile,
            outcomes = emptyList(),
            alreadyTried = setOf("tried")
        )

        assertEquals(1, result.size)
        assertEquals("fresh", result[0].id)
    }

    // ── 7. Ordering: highest success rate first ─────────────────────

    @Test
    fun `candidates are sorted by success rate descending`() {
        val low = candidate("low")
        val high = candidate("high")
        val mid = candidate("mid")

        val outcomes = listOf(
            outcome("low", CheckInResponse.SAME),
            outcome("low", CheckInResponse.SAME),
            outcome("high", CheckInResponse.BETTER),
            outcome("high", CheckInResponse.BETTER),
            outcome("mid", CheckInResponse.BETTER),
            outcome("mid", CheckInResponse.SAME)
        )

        val result = InterventionRouter.rank(
            candidates = listOf(low, mid, high),
            state = CurrentState.FROZEN,
            profile = defaultProfile,
            outcomes = outcomes
        )

        assertEquals(3, result.size)
        assertEquals("high", result[0].id)
        assertEquals("mid", result[1].id)
        assertEquals("low", result[2].id)
    }

    // ── 8. Null-rate candidates last, preserving input order ────────

    @Test
    fun `candidates with no outcome data sort last in input order`() {
        val withRate1 = candidate("rate-a")
        val withoutRate1 = candidate("no-data-1")
        val withRate2 = candidate("rate-b")
        val withoutRate2 = candidate("no-data-2")

        val outcomes = listOf(
            outcome("rate-a", CheckInResponse.BETTER),
            outcome("rate-a", CheckInResponse.BETTER),
            outcome("rate-b", CheckInResponse.SAME)
        )

        val result = InterventionRouter.rank(
            candidates = listOf(withRate1, withoutRate1, withRate2, withoutRate2),
            state = CurrentState.FROZEN,
            profile = defaultProfile,
            outcomes = outcomes
        )

        assertEquals(4, result.size)
        assertEquals("rate-a", result[0].id)       // 100% rate
        assertEquals("rate-b", result[1].id)       // 50% rate
        assertEquals("no-data-1", result[2].id)    // null, input order preserved
        assertEquals("no-data-2", result[3].id)    // null, input order preserved
    }

    // ── 9. Empty input → SAFE_FALLBACK (SF7) ───────────────────────

    @Test
    fun `empty candidates list returns SAFE_FALLBACK`() {
        val result = InterventionRouter.rank(
            candidates = emptyList(),
            state = CurrentState.FROZEN,
            profile = defaultProfile,
            outcomes = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals(SafetyFilter.SAFE_FALLBACK, result[0])
    }

    // ── 10. All vetoed → SAFE_FALLBACK (SF7) ───────────────────────

    @Test
    fun `all-vetoed candidates yield SAFE_FALLBACK`() {
        val imagery = candidate("imagery", hasTraumaImagery = true)
        val intero = candidate("intero", interoceptive = true)
        val wrongState = candidate("wrong", states = setOf(CurrentState.PANICKY))

        val result = InterventionRouter.rank(
            candidates = listOf(imagery, intero, wrongState),
            state = CurrentState.DISSOCIATION,
            profile = defaultProfile,
            outcomes = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals(SafetyFilter.SAFE_FALLBACK, result[0])
    }

    // ── 11. Single eligible candidate returned as-is ────────────────

    @Test
    fun `single eligible candidate is returned as-is`() {
        val only = candidate("only")

        val result = InterventionRouter.rank(
            candidates = listOf(only),
            state = CurrentState.FROZEN,
            profile = defaultProfile,
            outcomes = emptyList()
        )

        assertEquals(1, result.size)
        assertEquals("only", result[0].id)
    }

    // ── 12. Result never empty across states/profiles matrix ────────

    @Test
    fun `result is never empty across a matrix of states and profiles`() {
        val panickyOnly = candidate("panicky-only", states = setOf(CurrentState.PANICKY))
        val frozenOnly = candidate("frozen-only", states = setOf(CurrentState.FROZEN))
        val audioOnly = candidate("audio-only", requiresAudio = true)
        val universal = candidate("universal")

        val states = CurrentState.values()
        val profiles = listOf(
            defaultProfile,
            SafetyProfile(audioOk = false),
            SafetyProfile(hapticsOk = false),
            SafetyProfile(notForMe = setOf("universal")),
            SafetyProfile(audioOk = false, notForMe = setOf("universal"))
        )

        for (state in states) {
            for (profile in profiles) {
                val result = InterventionRouter.rank(
                    candidates = listOf(panickyOnly, frozenOnly, audioOnly, universal),
                    state = state,
                    profile = profile,
                    outcomes = emptyList()
                )
                assertTrue(
                    "Result must never be empty for state=$state, profile=$profile",
                    result.isNotEmpty()
                )
            }
        }
    }

    // ── 13. alreadyTried + veto combined still yields fallback ──────

    @Test
    fun `alreadyTried combined with veto still yields fallback`() {
        val only = candidate("only")

        val result = InterventionRouter.rank(
            candidates = listOf(only),
            state = CurrentState.FROZEN,
            profile = defaultProfile,
            outcomes = emptyList(),
            alreadyTried = setOf("only")
        )

        assertEquals(1, result.size)
        assertEquals(SafetyFilter.SAFE_FALLBACK, result[0])
    }

    // ── 14. Tie on rate preserves input order (stable sort) ─────────

    @Test
    fun `candidates with equal success rate preserve input order`() {
        val a = candidate("a")
        val b = candidate("b")
        val c = candidate("c")

        // All have the same outcome data — identical success rates
        val outcomes = listOf(
            outcome("a", CheckInResponse.BETTER),
            outcome("b", CheckInResponse.BETTER),
            outcome("c", CheckInResponse.BETTER)
        )

        val result = InterventionRouter.rank(
            candidates = listOf(a, b, c),
            state = CurrentState.FROZEN,
            profile = defaultProfile,
            outcomes = outcomes
        )

        assertEquals(3, result.size)
        assertEquals("a", result[0].id)
        assertEquals("b", result[1].id)
        assertEquals("c", result[2].id)
    }
}
