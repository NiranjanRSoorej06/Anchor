# Anchor Expansion Plan — demo build (branch: prajwal)

Source: P1 research — explorer gap map (exp-18) + librarian evidence brief (lib-1).
Status: Gate 1 passed (proceed-with-remediation, all findings applied below). Nothing built yet.

## 1. Home vision (locked for this expansion)

- Hero: big blue ANCHOR NOW button (`ui/HomeScreen.kt` AnchorNowButton) + triple-volume-tap
  (`trigger/AnchorAccessibilityService.kt`) — the USP, active not passive.
- Below hero, 3 polished cards: **Manage Symptoms / Tools / Get Support**.
- Graduate `devtools/FindSupportScreen.kt` + `devtools/ToolsLibraryScreen.kt` to production UI
  (both already exist — graduation is nav wiring + polish, ~1–2h, not a new build).

## 2. Where we stand (exp-18 gap map, condensed)

- EXISTS: SOS button + triple-tap, 60s sprint w/ breathing + "I feel steady", haptics stack,
  TTS whisper audio, SprintLogStore (50 logs), companion contacts + SMS engine, helplines (8),
  communities list, `MapsQueries` geo: builder, `PinRegions` table (never called from UI),
  `IncidentTriage` (9 kinds) + `InterventionRouter` (domain-only, no UI), `domain/meds`,
  `domain/goals` (no UI, no persistence), PCL-5, safety plan, follow-up model.
- MISSING: comfort-tool selector, editable anchor in-flow, looping mp3 audio, journal + skip→reminder,
  timeout "feel better?" path UI, Manage Symptoms screen, pincode field, standalone trigger log,
  meds/goals/journal UI + persistence, trends, professional-help handoff.
- PREREQUISITE (Gate 1 M1 — lock violation, not optional): `CompanionNotificationEngine`
  auto-sends SMS via SmsManager, but locked vision SF5 + #7 mandate dignity-SMS via
  `ACTION_SENDTO` intent. P2 default: replace auto-send with intent (user taps send).
  SmsManager survives only via explicit team lock-override recorded in `vision.md`.
  `SessionScreen.kt:409` call-site changes with it.

## 3. Exercise evidence (lib-1, condensed — full sources in gate context)

- Hyperarousal/panic: paced breathing (4-in/6-8-out), box breathing, PMR (PMC8272667).
- Intrusion/flashback + dissociation: 5-4-3-2-1 sensory grounding, safe-place visualization,
  present-moment statements, cold-water reset (acute).
- Avoidance/mood: thought defusion / cognitive reframing (CPT lineage).
- Only clusters above get selector dropdowns; no invented exercises.
- PTSD Coach lessons: robust persistence (history-loss complaints) → versioned DataStore + migration
  test; Material 3; civilian-first language; 8–10 deep tools over 17 thin; difficulty ratings +
  trauma-informed warnings; local-language audio note (Hindi/Tamil = follow-up, not demo).

## 4. Phase P2 — Anchor SOS hero (demo-critical, all REAL, no dummies in acute path)

- Sprint duration: change `SPRINT_DURATION_SECONDS` 60 → 30 for demo (no configurability —
  post-demo, per Gate 1 N3).
- Comfort-tool selector (breathing default + 5-4-3-2-1 + PMR-lite + safe-place) from
  `InterventionCatalog`; lite Edit-Anchor (reorder/toggle/duration via `RoutineValidator`).
- Audio: new `CalmingAudioPlayer` in `core/audio/` (MediaPlayer + `setLooping(true)`) behind interface
  w/ Debug fake; bundled asset only for demo (SAF `audio/*` picker deferred to Tier1, per Gate 1 N2).
- "I'm steady" → journal (≤280 chars, skippable, in-memory for P2) → skip shows
  importance-of-logging note → incident-kind picker (9) → router-ranked exercises.
  (Reminder-to-log needs WorkManager, not yet a dependency — moved to P4, per Gate 1 M2.)
- Timeout → "feel better?" → same incident path.
- Companion: P2 prerequisite per M1 — `ACTION_SENDTO` intent by default; confirm-gate SmsManager
  only under explicit team lock-override.
- Fallbacks: TTS whisper if mp3 missing.

## 5. Phase P3 — Manage Symptoms (REAL where evidence exists)

- New `ui/symptoms/ManageSymptomsScreen.kt` + `Screen` enum entry; symptom selector for
  hyperarousal / intrusion / dissociation / sleep (+ avoidance if time).
- Wire `IncidentTriage.starterIds()` → `InterventionRouter.rank()` → `InterventionCatalog`
  filtered list → reuse breathing/grounding stage UI as runners.
- "Need professional help?" handoff → `Screen.SUPPORT`. Empty clusters hidden, never invented.

## 6. Phase P4 — Get Support + Tools + home assembly + demo hardening (REAL except noted)

- Support: pincode field → `PinRegions.stateForPincode()` → `forRegion()` → geo: intent;
  re-verify all helpline numbers pre-demo; contacts opt in/out inline; NIMHANS RAAH link.
- Deps first (Gate 1 M3): neither DataStore nor WorkManager is in `build.gradle.kts` yet —
  add DataStore (+ WorkManager only if reminders survive the cut) before any tool UI.
- Tools core (P4 must): standalone trigger log (reuse interview questionnaire), journal persistence.
- Tools stretch — cut in this order if time runs short (Gate 1 N1): goals UI+persistence →
  meds UI+reminders → history list → trigger log.
  Advanced graphs = honest "working on it" placeholder (only allowed dummy).
- Home assembly + designer polish (big blue anchor + 3 cards). Demo: 90-sec script, full
  airplane-mode run, per-feature fallback.

## 7. Demo honesty policy

Every placeholder says "we're working on it" and is clickable-to-explain. Acute path has zero
placeholders. Permissions asked in context (SMS, notifications), zero-permission story preserved
for core SOS.

## 8. Open team decisions (grill)

1. Companion auto-SMS vs SF5: lock-compliant intent (default, no decision needed) or formal
   lock-override with confirm-gate SmsManager (requires `vision.md` update)?
2. Loved-one mp3: bundled-only for demo (recommended per Gate 1) or SAF picker now?
3. Maps: generic pincode geo: query (recommended) vs curated-map embed?
4. Must-live symptom clusters for demo day if time runs short?
