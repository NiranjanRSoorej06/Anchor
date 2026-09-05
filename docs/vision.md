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
Full flow spec: S0–S12 in `docs/onboarding-research.md` §5 — every screen skippable, safety plan defers to Settings, paths from 15s bare to 4–5 min full. PCL-5 presented as 4 cluster screens with break + halfway safeguard, score internal only.

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

## Session loop (M5 extended — HOLD on replace vs extend)
`IDLE → ACTIVATING → GROUNDING → EASING → CHECK_IN → RECOVERY`, plus `ROUTING → INTERVENTION → SAFETY_STOP` (M5).
OPEN: extend M5 incrementally vs rebuild full plan.md §15. No new states until team unblocks.

## Demo story (LOCKED: reliability)
Tap ANCHOR NOW → haptic grounds you → check-in → SMS template → privacy
close. All other scripts killed. UI build priority follows this order.

## Killed / deferred
- KILL for demo: Django/Channels backend, Deepgram+Groq+ElevenLabs chain, ambient upload, passive EMA, trauma-type diagnosis cards, Ed25519.
- STRETCH only: on-device loudness → hardcoded whisper, brown-noise asset (labeled comfort), Purr/Marble, volume trigger.

## India-first support (locked: directory spec, UI later)
PTSD Coach ships US-only numbers; India gets nothing. Anchor bundles an
India-ready Find Support directory (`docs/india-resources.md`): Tele MANAS
**14416** default, 112 emergency, Vandrevala 9999666555 (incl. WhatsApp),
NIMHANS, iCall, Sneha, Kashmir Lifeline, AASRA + therapist URL (badged
offline-unavailable). Phone-first (all work offline by call), free-first
ordering, language tags, regional priority. Numbers must be re-verified
before release.

## Reading list (all agents read these, in order)
- `docs/vision.md` (this file) — product + locks
- `CLAUDE.md` — project rules, docs convention, cross-device workflow
- `plan.md`, `evidence.md`, `M0-M4-CODEBASE-REFERENCE.md` (repo root) — execution plan, clinical base, codebase truth
- `docs/teardown.md`, `docs/complaints.md`, `docs/synthesis.md` — why PTSD Coach loses
- `docs/onboarding-research.md`, `docs/india-resources.md` — onboarding flow spec + India support directory
- `docs/tech-validation.md`, `docs/trigger-research.md`, `docs/oracle-review.md`, `docs/oracle-rereview.md` — feasibility + kill/defer rationale
