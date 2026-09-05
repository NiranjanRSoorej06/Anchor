# Anchor — Locked Vision (v1)

> Single track for all agents. Codebase truth: repo HEAD (M1–M5 +
> `domain/routine` + `domain/personalization`, 77 unit tests).
> Planning truth: this file.

## One-liner
Offline grounding button that works in airplane mode. Haptic in <300ms, no quizzes in the acute path, no backend.

## Non-negotiables (locked)
1. **Haptics are the product.** Double Pulse + Breathing In/Out ship first (already in `core/haptics/`). Purr/Marble Tier1. No biometric claims ("89Hz cortisol" dropped).
2. **ANCHOR NOW is hero entry.** Big one-tap button. Volume triple-press DEFERRED (button-first, per team lock).
3. **Offline-first, deterministic.** No network, no LLM/STT/TTS at runtime. Same inputs → same output.
4. **Privacy.** No trauma narrative field, no ambient audio upload, no diagnosis. Ever.
5. **Safety filter overrides everything.** `domain/safety` built (18 tests): `permits()` enforces SF1/SF2/SF4/SF6/SF8 as hard vetoes, `fallback()` guarantees SF7 never-empty screen; SF3 owned by `SessionStateMachine`, SF5 by UI layer. Dissociation → separate pathway, WORSE → SAFETY_STOP, no auto-dial/SMS.
6. **Safety phrase ships.** User-recorded (MediaRecorder) + bundled fallback, <300ms playback.
7. **Dignity SMS via intent.** Pre-written templates ("rough moment, don't need you to do anything…") through `ACTION_SENDTO`. No Ed25519 for demo.
8. **Safety plan ships.** Stanley-Brown 6-step plan (`domain/safetyplan` model + validator built), user-entered text only, display-only — no auto-dial, no auto-SMS. UI later.
9. **Persistence: DataStore + JSON, on-device only.** Profile, routines, episodes, safety plan. Matches the `EpisodeStore` interface; SDK machine implements.

## Onboarding (locked: one-time quiz OK, never repeated)
One-time structured quiz at onboarding is FINE. Complaint was repeated quizzes, not onboarding. Rule: quiz once → tool becomes functional, never nags again.
Allowed once: PCL-5 (20 items, free, validated) as baseline + functional prefs below. No ITQ/DES at onboarding (labeling + length). No free-text trauma description + classification (diagnosis + re-traumatization + determinism risk).
Flow:
1. PCL-5 once (`domain/assessment`: `Pcl5Catalog` 20 items + `Pcl5Scorer`, cutoff 31, screening-only KDoc; skippable, on-device only, never re-asked unless user opens Settings > Progress)
2. What hits you most? (racing heart / shut down-numb / loud world / flashback-ish / nightmares)
3. What helps, even a little? (hold / breathe / own voice / quiet+dark / reach someone)
4. Sensory prefs (sound on/off, vibration strength from `HapticCapabilities`, work-safe default)
5. Safety phrase (record or bundled) + trusted contact (typed number, template preview) — both skippable
6. Done → ANCHOR NOW
Learning after: Better/Same/Worse only (1 tap) → `domain/personalization/PersonalizationScorer` → insight card ("X helped 4 out of 5 times", MIN_SESSIONS=3). No scales in acute path, no re-quizzing.
Full flow spec: S0–S12 in `docs/onboarding-research.md` §5 — every screen skippable, safety plan defers to Settings, paths from 15s bare to 4–5 min full. PCL-5 presented as 4 cluster screens with break + halfway safeguard, score internal only. Flow machine built: `domain/onboarding` (linear WELCOME→DONE, complete/skip record, restart; 14 tests). UI jumps stay in UI layer; onboarding UI itself NOT built.

## Post-SOS check-in (locked: model built, notification + UI on SDK machine)
One gentle notification ~10–15 min after SOS (AlarmManager; discreet text
"Anchor: how are you doing?" — never clinical wording on a lock screen).
Opens optional 3-tap flow: trigger chips + distress 1–5 + ≤280-char note
(`domain/followup`, 15 tests), each independently skippable, once per
episode, dismiss = never re-asked. Lands in Episode → therapist export.
Support directory (`domain/support`: 9 entries + `forRegion()`, 18 tests)
backs the Find Support screen. This episode-linked check-in is the ONLY
allowed re-prompt — scheduled/repeated assessments stay banned.

## Custom routines (locked: model built, UI later)
The USP: user builds calming routines (own audio/voice clips, haptics,
breathing, pauses) and binds one to the panic button. Spec (`domain/routine/`,
21 tests): `RoutineStep` = Haptic(patternId, intensity 0..1, durationSec) |
Breathing(1..120s) | SafetyPhrase(clipId) | Pause(1..120s); `Routine` max 8
steps, max 180s total; `RoutineValidator` collects all failures. Playback UI,
recording UI, and panic-button binding are NOT built yet — model only.

## Panic button (LOCKED: medical default, user-editable)
Ships a medically-based default sequence — E007 tactile anchor →
E004 paced breathing → check-in — that the user can edit (reorder, toggle,
duration via `RoutineValidator`; voice-recording builder is Tier1, not
demo). Answers "allow edits to it": yes, lite editor, no audio recording
for the demo.

## Routing + catalog (locked: models built, wiring later)
`domain/content`: 9 pre-authored interventions E001–E009 per plan.md §8.3
with `EvidenceStatus` + `toSafetyCandidate()` bridge (10 tests).
`domain/routing`: `rank()` pipeline — drop alreadyTried → safety permits →
successRate sort → SF7 fallback, never empty (14 tests). E009 brown noise
flagged EVIDENCE_GAP, never primary. Wiring into session flow + UI NOT built yet.

## Profile + history (locked: models built, persistence/UI later)
`domain/profile`: `UserProfile` per plan.md (sensory prefs, vetoes,
contacts, no narrative field by design) + `toSafetyProfile()` bridge +
validator (17 tests). `domain/history`: `Episode` record + `EpisodeStore`
interface + `InMemoryEpisodeStore` (HAL pattern; DataStore impl is
SDK-machine work) (12 tests). Onboarding/profile UI and DataStore wiring
NOT built yet.

## Content library + You-are-not-alone (locked: specs, UI later)
Exercises: adapt public-domain/clinical protocols (5-4-3-2-1, breathing
ratios, Jacobson PMR, sleep hygiene, coping statements, DBT paraphrase
only — Guilford enforces), own wording always, never copy worksheets.
BUILT as data: `GuidedScripts` (6 scripts: 54321, 3-step, box, coherent,
exhale, PMR; 17 tests), `CopingStatements` (19) + `SleepChecklist`
(10 items; 9 tests), `MapsQueries` (3 presets; 4 tests). UI readers
NOT built.
24 bundled safety phrases (`domain/content/SafetyPhraseBank`, 10 tests) + custom recordings (≤15s, present tense, no
narrative clause, preview + confirm; `isAcceptable` guards text form). You-are-not-alone order: crisis
helplines (offline) → Maps button (`geo:0,0?q=`, no permission needed,
online-badged) → therapist directories → peer communities last with
caveats ("browse when stable"). Spec: `docs/content-sources.md`.

## Session loop (M5 extended — HOLD on replace vs extend)
`IDLE → ACTIVATING → GROUNDING → EASING → CHECK_IN → RECOVERY`, plus `ROUTING → INTERVENTION → SAFETY_STOP` (M5).
OPEN: extend M5 incrementally vs rebuild full plan.md §15. No new states until team unblocks.

## Demo story (LOCKED: reliability)
Tap ANCHOR NOW → haptic grounds you → check-in → SMS template → privacy
close. All other scripts killed. UI build priority follows this order.

## Demo lock (MUST genuinely work on the day)
MUST (reliability path): ANCHOR NOW ✅ built · haptic grounding ✅ built ·
check-in ✅ built · SMS template ❌ (~2–3h: pre-written templates via
ACTION_SENDTO, no auto-send) · privacy close ❌ (~1h: end card + data
statement, no deletion theater).
PLUS (differentiators, cheap): Find Support India directory + regional
routing ❌ (~8h per docs/near-you-research.md; manual-picker-only lite
~2h fallback) · safety plan screen ❌ (model built; form UI ~3–4h) ·
insight card ❌ (scorer built; ~1–2h).
SEED, DON'T BUILD for demo: onboarding profile (spec S0–S12 exists;
ship seeded profile; build flow only if MUST+PLUS done).
CUT for demo: volume clutch, routine builder UI, voice recording,
brown noise, Learn module, graphs.
BORROW from PTSD Coach (content patterns only, own words + evidence
labels — techniques like paced breathing, 5-4-3-2-1 grounding, PMR,
sleep hygiene are standard clinical procedures; our E001–E009 already
follows this pattern): exercise content shapes, Stanley-Brown structure
(published protocol), SUDS concept (adapted to Better/Same/Worse).
NEVER borrow: forced assessments, repeat reminders, crisis-line spam,
veteran framing.
Novelty order (pitch-safe): offline zero-permission ★5, dissociation
routing ★5, outcome rerank ★4 (mechanism only), custom voice ★4
(neuroscience-informed), regional routing ★4. Never claim as clinical
fact: haptics proven, vagal regulation, BLS app, personalization
improves outcomes, voice reduces anxiety. Full brief:
`docs/exercise-evidence.md`.

## Journaling (locked: structured built, photo Tier1, narrative banned)
Built: Episode + SessionOutcome + insight records WHAT HELPED, never what
happened. Tier1: voluntary trigger-photo log — user taps, CameraX captures
to app-private storage, timestamped + linked to episode + optional short
note; blurred thumbnails, no auto-review/slideshow, export-to-therapist
framing (photos can re-trigger on re-view). Banned unless the team
explicitly overrides: free-text trauma narrative fields (plan.md acceptance
criterion), passive/automatic capture of any kind (photo or audio),
anything captured leaving the device.

## Killed / deferred
- KILL for demo: Django/Channels backend, Deepgram+Groq+ElevenLabs chain, ambient upload, passive EMA, trauma-type diagnosis cards, Ed25519.
- STRETCH order: volume clutch first (docs/clutch-research.md verdict — Nugon real but niche, Dictate claim debunked, needs pre-configured Pixel/Samsung, 5.5–9.5d; Tier1 spike, never demo-default) → on-device loudness whisper → brown noise (comfort-labeled) → Purr/Marble.
  **Update (2026-09-05): the volume-clutch prototype is now built**, not
  just researched — `trigger/AnchorAccessibilityService.kt` (ported from
  Nugon's verified long-press pattern, screen-on/locked-awake only, no
  MediaSession deep-sleep bypass) plus a second, simpler home-screen
  widget trigger (`widget/GroundingWidget.kt`), both converging on
  `ui/grounding/GroundingCaptureScreen.kt`: one photo, on-device ML Kit
  object labeling, a template-built "things you can see" script, photo
  never persisted. Kept fully off the demo-critical path (`HomeScreen`'s
  `ANCHOR NOW` flow is untouched) — available as the stretch item this
  section already calls for, not a replacement for it. Verified: full
  build/tests green, and the shared capture flow confirmed end-to-end on
  an emulator (real CameraX capture, on-device labeling, script render,
  no file ever written to disk/MediaStore, camera-permission-denied
  fallback). NOT yet verified: the volume long-press's key interception
  on real hardware — the accessibility service registers and binds
  correctly, but the emulator's synthetic volume-key input never reached
  `onKeyEvent()`, so this specific mechanism still needs a real-device
  check before being demoed, exactly per this doc's own caveats above.

## India-first support (locked: directory spec, UI later)
PTSD Coach ships US-only numbers; India gets nothing. Anchor bundles an
India-ready Find Support directory (`docs/india-resources.md`): Tele MANAS
**14416** default, 112 emergency, Vandrevala 9999666555 (incl. WhatsApp),
NIMHANS, iCall, Sneha, Kashmir Lifeline, AASRA + therapist URL (badged
offline-unavailable). Phone-first (all work offline by call).
Directory only — we provide and integrate nothing: no booking, no accounts,
no backend, no live clinic lookup. "Free-first" means free-to-call
third-party helplines listed first, not free therapy from us. "Near you"
for demo = curated static entries + directory URLs; live location lookup
is stretch (permission-gated, optional). Pointing alone beats PTSD Coach's
nothing. Numbers must be re-verified before release.
Near-you verdict (locked): no live street-level clinic lookup — needs
online Places billing or GBs of offline data, both kill offline-first.
Ship region-mapped static directory instead: bundled ~25KB state table,
optional one-shot COARSE fix (never stored, never sent), manual 36-state
picker fallback. Spec: `docs/near-you-research.md`.
Pincode beats GPS: user types 6-digit PIN → first-2-digit state table
(~28 rows, <1KB), zero permissions, fully offline. Institution map
(apex/DMHP/NGO/private, 20 seed entries): `docs/ptsd-care-india-map.md`.

## Reading list (all agents read these, in order)
- `docs/vision.md` (this file) — product + locks
- `CLAUDE.md` — project rules, docs convention, cross-device workflow
- `plan.md`, `evidence.md`, `M0-M4-CODEBASE-REFERENCE.md` (repo root) — execution plan, clinical base, codebase truth
- `docs/teardown.md`, `docs/complaints.md`, `docs/synthesis.md` — why PTSD Coach loses
- `docs/onboarding-research.md`, `docs/india-resources.md` — onboarding flow spec + India support directory
- `docs/near-you-research.md` — near-you verdict (region-mapped static, no live POIs)
- `docs/ptsd-care-india-map.md` — PIN routing table + 20 institutions + card taxonomy
- `docs/content-sources.md` — exercise protocols, phrase bank, communities, Maps intent
- `docs/demo-plan.md` — THE demo: locked feature list, fake cards, 90-sec script (share with SDK machine)
- `docs/ptsd-coach-evidence.md` — trials, attrition, flaws (nice ≠ retained; our wedge)
- `docs/app-literature-suggestions.md` — authors' own recommendations per paper + top-10 synthesis + our gaps
- `docs/tech-validation.md`, `docs/trigger-research.md`, `docs/oracle-review.md`, `docs/oracle-rereview.md` — feasibility + kill/defer rationale
