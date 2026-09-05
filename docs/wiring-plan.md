# Wiring Plan — connecting domain models to live UI (2026-09-05)

For the SDK machine (has the compiler). Each job: file, anchor, exact
call, holders, imports. Jobs 1–2 first (demo-critical routing + outcome
capture); 3–5 next; 6–7 placement only. Full line refs in exp-16 map.

## Job 1 — ROUTING branch ranks a NAMED intervention
File `ui/session/SessionScreen.kt` (~line 87, `SessionState.ROUTING -> machine.beginIntervention()`).
Before `beginIntervention()`, insert:
`val ranked = InterventionRouter.rank(InterventionCatalog.ALL.map { it.toSafetyCandidate() }, state = CurrentState.NOT_SURE, profile = SafetyProfile(), outcomes = <from Job 2 store>, alreadyTried = <per-episode set>)`; store `ranked.first().id` in `remember` for the INTERVENTION render (fixes "same screen twice"). Imports: routing.InterventionRouter, content.InterventionCatalog, content.toSafetyCandidate, safety.CurrentState, safety.SafetyProfile, personalization.SessionOutcome. Proper fix later: thread real CurrentState/profile/outcomes instead of defaults.

## Job 2 — CHECK_IN records outcome + episode
Same file (~lines 145–149, three `submitCheckIn` callbacks). Hoist `val episodeStore = remember { InMemoryEpisodeStore() }` (+ start-time + episode-id holders); wrap each callback to `store.record(Episode(id, start, now, routineId = chosenInterventionId, finalResponse = response))` BEFORE `submitCheckIn`. Guard: only map episodes with non-null finalResponse into SessionOutcome. Imports: history.InMemoryEpisodeStore, history.Episode.

## Job 3 — Onboarding entry (first-run gate)
`MainActivity`: add ONBOARDING to Screen enum + branch rendering new `ui/onboarding/OnboardingScreen.kt` (observe `OnboardingFlow.state`, per-step UI; substantial new file) + default launch to ONBOARDING. Domain ready; UI missing.

## Job 4 — Find Support entry (DONE from our side pattern)
Add FIND_SUPPORT branch rendering `FindSupportScreen()` (zero params) + Home link. Same pattern just used for SUPPORT/TOOLS nav.

## Job 5 — GroundingCaptureScreen uses GuidedScripts
Replace `GroundingScriptBuilder.build(emptyList())` with `GuidedScripts.byId("grounding_54321")!!`; iterate `steps` (text + durationSec) for TTS + display; change `ScriptStage` param `GroundingScript` → `GuidedScript`. Do NOT delete GroundingScriptBuilder (widget path may need it).

## Job 6 — Placement (no code yet)
PCL-5 → onboarding PCL5 step. SafetyPlan → Settings screen (deferred). FollowUpCheckIn → post-session trigger (AlarmManager deferred; demo: next-app-open check).

## Job 7 — MUST NOT touch
SessionStateMachine transitions, SessionState/CheckInResponse enums, SafetyFilter veto chain + SAFE_FALLBACK, WORSE→SAFETY_STOP ownership, catalog ordering, OnboardingFlow linear sequence, demo path HOME→SESSION→RECOVERY→HOME.

## Mismatches to resolve while wiring
M1-M3: SessionScreen lacks CurrentState/SafetyProfile/outcomes params — defaults now, thread later. M4: null-guard Episode→SessionOutcome mapping. M5: ScriptStage shape change (above).
