# Anchor — Implementation Plan

**Status:** authoritative execution plan · **Revision 3** · 36-hour hackathon
**Companions:** `evidence.md` (authoritative for every clinical claim) ·
`CLAUDE.md` (project constraints)

> **Standing rule.** Every clinical or evidence statement Anchor makes — in
> code, in UI copy, in the pitch — must trace to a record in `evidence.md`.
> If it is not in `evidence.md`, Anchor does not say it. Research lives in
> `evidence.md`. This file answers: what we build, how we build it, in what
> order, and what we cut.

---

## 1. Executive Summary

Revision 3 changes the product's centre of gravity twice over.

**Entry is no longer a lock-screen stunt.** The realistic flow is: distress →
unlock the phone → one tap on **ANCHOR NOW** → haptic grounding starts
instantly. Lock-screen doors (quick-settings tile, notification action, side
key) survive as cheap accelerants, demoted from MUST to SHOULD. The
accessibility volume trigger stays deferred.

**The product idea is now personalization with a paper trail.** Anchor learns
which evidence-informed stabilization techniques an individual reports helping,
and turns them into a one-tap personal recovery routine. Four forces, in strict
precedence:

```
SAFETY           vetoes anything unsafe for the current state   (absolute)
EVIDENCE         determines what may exist in the catalog at all
USER PREFERENCE  determines what the person wants available
RESPONSE HISTORY determines what Anchor offers first
```

Everything at runtime is deterministic Kotlin over local data. No network, no
LLM, no model inference in the acute path — ever.

**Honest budget position.** The MUST set is ~64 engineering-hours against
~65–70 effective hours for four devs, ~50 for three. It fits four devs with
almost no slack and does not fit three. §18.4 is the pre-agreed cut ladder;
agree it before hour 0, not at hour 30.

**Build this first (H0–H6):** skeleton → `HapticEngine` + heartbeat on a real
Samsung → frozen data models → ANCHOR NOW launching an immediate haptic. That
vertical slice is demoable on its own and everything else hangs off it.

---

## 2. Product Goal

> "An evidence-informed acute stabilization and grounding tool for people
> experiencing PTSD-related distress." (`evidence.md` §13, verbatim)

Anchor helps someone get through an acute episode with minimal cognitive load,
then remembers what they reported helping, so the next episode starts with the
thing most likely to help them specifically.

Anchor is **not**: a diagnosis, a trauma-type classifier, a treatment, a cure,
an AI therapist, an emergency service, or a replacement for professional care.
See §11.

---

## 3. Core User Journey

```
DISTRESS
  ↓  user unlocks the phone (or uses tile / side key / shortcut — SHOULD)
ONE TAP — "ANCHOR NOW"
  ↓  < 300 ms
IMMEDIATE HAPTIC GROUNDING          ← no question has been asked yet
  ↓
PERSONAL DEFAULT RECOVERY ROUTINE   ← their routine, or the safe default
  ↓
OPTIONAL STATE SELECTION            ← an overlay, skippable, never blocking
  ↓
SAFETY FILTER → EVIDENCE CATALOG → RANKING
  ↓
1–3 MINUTE INTERVENTION
  ↓
"HOW ARE YOU NOW?"  →  Better · Same · Worse
      Better → finish
      Same   → next eligible intervention (tried ones excluded)
      Worse  → stop everything → safety options
  ↓
SESSION COMPLETE → episode stored locally
  ↓
~10 MINUTE FOLLOW-UP  →  Calm · Better but not calm · Still distressed
  ↓
PERSONAL RESPONSE HISTORY UPDATED
  ↓
FUTURE ROUTINES AND RANKINGS IMPROVE
```

The load-bearing property: **grounding starts before any question.** The state
question is an overlay on top of an already-running haptic, and ignoring it
costs the user nothing.

---

## 4. Product Principles

1. **Relief before questions.** Haptic within 300 ms of the tap. Every question
   is optional and non-blocking.
2. **You never need to describe what happened to you.** (`evidence.md` §2.)
   Onboarding asks about situations, senses and preferences — never the event.
   No field in the data model can hold a trauma narrative.
3. **A profile is not a diagnosis.** (`evidence.md` §1, §3.)
4. **The catalog is the source of truth.** Interventions are pre-authored and
   evidence-tagged. Nothing clinical is generated at runtime. (`evidence.md` §14.)
5. **Safety overrides everything** — evidence, preference and history alike.
6. **Dissociation is not hyperarousal.** Separate pathway; worsening stops the
   session rather than escalating. (`evidence.md` §10.)
7. **No trauma imagery or exposure content anywhere in the acute path.**
   (`evidence.md` §10, §13.)
8. **Personalization is descriptive, never clinical.** "You reported feeling
   better 7 of 8 times" — never "this works for you."
9. **Offline or it doesn't ship.** Airplane mode is the test condition.
10. **Determinism.** Same inputs, same output, every time — for safety, for
    testability, and so the demo cannot surprise us.

---

## 5. Feature Scope and Priority

Scoring: Impact / Effort / Risk / Feasibility, 1–10 (higher effort and risk are
worse). Full inventory with reasons in §23.

### MUST — the story is not demonstrable without these

| Feature | Imp | Eff | Risk | Feas |
|---|---|---|---|---|
| ANCHOR NOW one-tap entry + `AnchorActivity` | 10 | 2 | 2 | 10 |
| Launcher shortcut | 6 | 1 | 1 | 10 |
| `HapticEngine` + capability detection | 9 | 3 | 4 | 8 |
| Heartbeat texture | 9 | 2 | 4 | 8 |
| Haptic breathing pacer + visual pacer | 8 | 3 | 3 | 8 |
| Session state machine (§15) | 10 | 4 | 3 | 8 |
| Session UI | 9 | 5 | 2 | 9 |
| Optional state-selection overlay | 8 | 1.5 | 1 | 9 |
| Evidence catalog + intervention catalog | 9 | 5 | 3 | 8 |
| Safety filter | 10 | 2.5 | 2 | 9 |
| Deterministic routing | 10 | 3 | 3 | 8 |
| Personalization scorer + insight copy | 10 | 2.5 | 2 | 9 |
| Reassessment (Better/Same/Worse) | 9 | 1.5 | 1 | 9 |
| Safety / emergency screen | 10 | 2 | 2 | 9 |
| Recovery routine model + seeded defaults | 9 | 2 | 2 | 9 |
| Recovery routine editor ("My Anchor") | 9 | 4 | 3 | 8 |
| Local persistence (profile, episodes, routines) | 8 | 3 | 2 | 9 |
| Episode + outcome recording | 9 | 2 | 2 | 9 |
| ~10-minute follow-up | 8 | 3 | 4 | 7 |
| Safety phrase record + playback | 8 | 3 | 3 | 8 |
| Minimal onboarding | 7 | 2.5 | 1 | 9 |
| Evidence traceability screen | 8 | 1.5 | 1 | 9 |

### SHOULD — after the core is green

Quick-settings tile · notification quick action · Samsung side-key mapping ·
`showWhenLocked` on `AnchorActivity` · entry-status screen · Purr texture ·
Marble texture · trusted contact (SMS composer) · recovery/refractory mode
(reduced scope) · haptic lab screen.

### IF TIME

Brown noise (E009) · learning/psychoeducation modules · full insights/pattern
view · per-step "why this?" cards.

### DEFERRED (designed, scoped, not built — §10, §17.9)

Volume triple-press (accessibility) · earbud/media-button trigger · rapid
power-press trigger · shake trigger · runtime RAG · build-time RAG tooling.

### DROP

Environmental sound classification · runtime AI/LLM in any path · backend,
auth, cloud sync · passive sensor EMA · ITQ-style symptom questionnaire in
onboarding.

---

## 6. The Acute Anchor Experience

### 6.1 Entry

The home screen is one enormous button — **ANCHOR NOW** — occupying the upper
two-thirds of the screen, reachable one-handed, with a secondary row for
routines/history/settings below. `AnchorActivity` is `singleTask`, and fires
the haptic in `onCreate()` before the first composition.

Additional doors (SHOULD, all funnelling into the same activity): launcher
shortcut, quick-settings tile, notification action, Samsung side-key
double-press. `showWhenLocked`/`turnScreenOn` remain as one-line flags so a
lock-screen tap still works, but nothing in the product depends on them.

### 6.2 First ten seconds

1. Haptic starts (< 300 ms) — the user's default routine's first step, or
   Heartbeat if no routine exists.
2. The screen shows the running step and one large **"How does it feel?"**
   affordance. Nothing must be read.
3. The state overlay is offered once, after ~4 s, as a low-emphasis card. Six
   options, ≤ 3 words each. Dismiss or ignore = safe default path continues.
4. A **STOP** control is always present and always one tap.

### 6.3 Current-state options

| Enum | User-facing wording | Pathway |
|---|---|---|
| `PANICKY` | "Racing / can't calm down" | Self-calming |
| `HYPER_ALERT` | "On edge / can't settle" | Self-calming |
| `FLASHBACK` | "Feels like it's happening again" | Present-time orientation |
| `DISSOCIATION` | "Far away / not real" | **External orientation (separate)** |
| `FROZEN` | "Stuck / can't move" | Low-demand default |
| `NOT_SURE` | "Not sure" | Low-demand default |

Structured user selection only — never inferred, never classified
(`evidence.md` §4). `FROZEN` and `NOT_SURE` have **no pathway evidence** in
`evidence.md`; they route to the lowest-demand option by product decision and
are tagged as such (§24, C7).

### 6.4 Reassessment

Three large targets after each intervention. `BETTER` → session complete.
`SAME` → route to the next eligible intervention, tried ones excluded, with
under-observed interventions preferred (§9.4). `WORSE` → safety filter rule SF3
fires: haptics and audio stop immediately and the safety screen appears.

---

## 7. Personal Recovery Routines ("My Anchor")

A routine is an **ordered list of catalog interventions with durations**, built
in a constrained editor. The user composes *delivery*, never clinical content.

### 7.1 Example

```
MY ANCHOR — HYPER-ALERT
1. Heartbeat haptic ........ 60 s
2. Paced breathing ......... 2 min
3. Personal safety phrase .. once
4. Marble texture .......... 60 s
```

### 7.2 What the user may and may not do

| Allowed | Not allowed |
|---|---|
| Pick steps from the catalog's eligible set | Add anything not in the catalog |
| Reorder steps | Add steps the safety filter vetoes for that state |
| Enable/disable steps | Type free-text instructions of any kind |
| Adjust duration inside `[minDurationSec, maxDurationSec]` | Exceed per-intervention safe bounds |
| Mark one routine default per state | Exceed 6 steps or 10 minutes total |
| Name a routine (label only, ≤ 40 chars) | Author or edit clinical text, claims or citations |

Interventions vetoed for a routine's state are shown **greyed with the reason**
("not offered for this state — see why"), which doubles as an evidence-screen
entry point. That transparency is a demo asset.

### 7.3 Validation rules (enforced in `RoutineValidator`, unit-tested)

- `V1` every `interventionId` resolves in the catalog
- `V2` every step passes `SafetyFilter` for the routine's `forState`
- `V3` `durationSec ∈ [min, max]` of its intervention
- `V4` `1 ≤ steps.size ≤ 6`, total duration ≤ 600 s
- `V5` at most one default routine per state
- `V6` name is a label: length-capped, no URLs, never rendered as clinical copy
- `V7` a routine failing validation on load (catalog changed) is repaired by
  dropping offending steps, never by silently substituting

### 7.4 Runtime selection

```
routineFor(state) =
    user default routine for state
        ?: user default routine for ANY
        ?: seeded default routine for state
        ?: SAFE_FALLBACK (Heartbeat 60 s → Orienting 60 s)
```

Every candidate is re-validated against the safety filter at run time, not just
at edit time — the profile may have changed since.

### 7.5 Seeded defaults (shipped, editable, derived from §8.3)

| State | Routine |
|---|---|
| PANICKY / HYPER_ALERT | Heartbeat 60 s → Paced breathing 120 s → Safety phrase |
| FLASHBACK | Sensory scan 90 s → Present-time statement + safety phrase → Heartbeat 60 s |
| DISSOCIATION | External orientation 90 s → Orienting to the room 60 s → Heartbeat 60 s *(no breath-focus — SF2)* |
| FROZEN / NOT_SURE | Heartbeat 60 s → Orienting to the room 90 s |

---

## 8. Evidence-Constrained Intervention Engine

### 8.1 Runtime pipeline (deterministic, offline, ~1 ms)

```
UserProfile + CurrentState + ResponseHistory
        ↓
SafetyFilter        ── hard vetoes, absolute precedence
        ↓
InterventionCatalog ── pre-authored, evidence-tagged
        ↓
Eligible set        ── allowedStates ∩ preferences ∩ not-already-tried
        ↓
Ranking             ── personalization score (§9)
        ↓
Pre-authored sequence (routine or single intervention)
        ↓
Session → Outcome → history update
```

### 8.2 Safety filter — SF1…SF8

| Rule | Behaviour | Basis |
|---|---|---|
| SF1 | Trauma imagery, exposure-style or narrative content is never eligible in the acute path | `evidence.md` §10, §13 |
| SF2 | `DISSOCIATION` excludes interoceptive/inward-focus interventions (breath-focus, body scan, inward relaxation); only external-orientation candidates remain | `evidence.md` §10 |
| SF3 | Any `WORSE` ends the session and routes to the safety screen. On the dissociation path one `WORSE` is enough | `evidence.md` §10 |
| SF4 | Sensory preferences are hard exclusions: no-audio, no-voice, reduced-haptics, touch-sensitive | Profile |
| SF5 | Emergency services are never contacted automatically — explicit user action only | `CLAUDE.md`, §11 |
| SF6 | An intervention whose `allowedStates` omits the current state is never eligible | Catalog integrity |
| SF7 | If the catalog fails to load or everything is filtered out, fall back to `SAFE_FALLBACK` — never an empty screen | Failure design |
| SF8 | An intervention the user marked "not for me" is never offered, at any rank | Preference |

Pure functions over data classes, no Android imports — the most test-worthy
code in the project.

### 8.3 Intervention catalog (nine pre-authored entries)

| id | Name | Target states | Technique | Evidence status | Source |
|---|---|---|---|---|---|
| E001 | Present-time external orientation | DISSOCIATION | External orientation | `CLINICAL_CAUTION_DERIVED` | NICE NG116; VA NCPTSD |
| E002 | Sensory scan grounding | FLASHBACK, FROZEN | Grounding | `RCT_COMPONENT` | Lewis 2017 |
| E003 | Present-time statement + safety phrase | FLASHBACK | Flashback management | `GUIDELINE_RECOMMENDED` + `DELIVERY_METHOD_UNEVALUATED` | NICE NG116 |
| E004 | Paced breathing with haptic pacer | PANICKY, HYPER_ALERT | Self-calming | `GUIDELINE_RECOMMENDED` + `DELIVERY_METHOD_UNEVALUATED` | NICE NG116 |
| E005 | Brief applied relaxation step | HYPER_ALERT, PANICKY | Relaxation | `RCT_STUDIED_WITH_LIMITATIONS` | Thorp 2019; Vera 2022; VA/DoD 2023 |
| E006 | Riding out the wave (emotion tolerance) | PANICKY, HYPER_ALERT | Emotion regulation | `ADJUNCT_EVIDENCE` | Bryant 2013 |
| E007 | Tactile anchor (Heartbeat / Purr / Marble) | all | Grounding — tactile delivery | `RCT_COMPONENT` (class) + `DELIVERY_METHOD_UNEVALUATED` (haptic) | Lewis 2017 |
| E008 | Orienting to the room | NOT_SURE, FROZEN, DISSOCIATION | Grounding / external orientation | `RCT_COMPONENT` | Lewis 2017 |
| E009 | Sound cover (brown noise) | adjunct only, never primary | Sensory comfort | `EVIDENCE_GAP` | — none — |

`evidenceStatus` is a closed enum, rendered verbatim in the UI:

`GUIDELINE_RECOMMENDED` · `RCT_COMPONENT` · `RCT_STUDIED_WITH_LIMITATIONS` ·
`ADJUNCT_EVIDENCE` · `CLINICAL_CAUTION_DERIVED` · `DELIVERY_METHOD_UNEVALUATED`
· `EVIDENCE_GAP` (no support in `evidence.md`; requires human review; **never
clinically routed as a primary intervention**) · `PRODUCT_DECISION`.

E005 must always render alongside its limitation string: *"The 2023 VA/DoD
guideline states evidence is insufficient to recommend relaxation training as a
standalone treatment."* (`evidence.md` §7.)

### 8.4 Ranking

```
candidates = catalog
    .filter { state in it.allowedStates }          // SF6
    .filter { safetyFilter.permits(it, profile, episode) }   // SF1–SF8
    .filter { it.id !in episode.alreadyTried }
    .sortedWith(rankComparator)                    // §9
```

---

## 9. Personalization / Personal Response History

Explainable arithmetic over local data. No ML, no inference, no randomness.

### 9.1 What is recorded

Per completed `InterventionOutcome`: `interventionId`, `state`, `startedAt`,
`durationSec`, `reassessment` (BETTER/SAME/WORSE), `wasFollowedByAnother`,
plus per-episode `followUpResult` and `timeToFirstBetterMs`.

### 9.2 Scoring

Outcome value: `BETTER = 1.0` · `SAME = 0.25` · `WORSE = 0.0`.

Recency weight for the *k*-th most recent observation (k = 0 newest):
`w_k = 0.9^k`, capped at the most recent 20 observations.

State-specific weighted mean, with a neutral prior so new options are never
buried:

```
rawScore(i, s) = ( Σ w_k · value_k  +  α · PRIOR ) / ( Σ w_k + α )
    PRIOR = 0.5     α = 2.0
```

Blend with the intervention's global (all-state) score while state evidence is
thin:

```
n      = observations for (i, s)
score  = ( n · rawScore(i,s) + β · rawScore(i, ALL) ) / ( n + β )     β = 2.0
```

Constants live in `PersonalizationConfig` — one place, tunable, documented.

### 9.3 Precedence when ordering candidates

```
1. Safety filter        — veto, not a weight (§8.2)
2. User "not for me"    — veto (SF8)
3. User pin / routine default for this state — sorts first
4. score, descending
5. tie: fewer observations first   (gives new options a real chance)
6. tie: higher evidence status     (catalog enum order)
7. tie: catalog order              (fully deterministic)
```

### 9.4 The `SAME` branch — exploration without randomness

On `SAME`, before re-ranking: if any eligible unfiltered intervention has
`n < MIN_OBS`, offer the highest-scoring of *those* first. Deterministic, so
the demo is reproducible, and no option can be permanently buried.

### 9.5 Surfacing it

`MIN_OBS = 3` observations for a given (intervention, state) before Anchor
says anything. Below that: *"Still learning what helps you."*

Approved copy at or above threshold — descriptive, never clinical:

> "During **hyper-alert** episodes you reported feeling better after
> **Heartbeat** 7 of 8 times."
> *(your own reports · not a clinical measure)*

Banned framings: "works for you", "effective", "recommended treatment",
"clinically", "proven", any prediction. (§24, C11.)

### 9.6 How it changes behaviour

- Ranking within a session (§9.3).
- A one-line suggestion on the routine editor: *"Add Heartbeat to this
  routine?"* — always a suggestion, never an automatic edit.
- The insight card on the home screen after `MIN_OBS` is reached.

Anchor **never** silently rewrites a user's routine.

---

## 10. RAG / Research Architecture

**Verdict: no RAG in the hackathon. Runtime RAG is DROPPED permanently;
build-time RAG is DEFERRED.**

**Why not at runtime.** A retrieval-and-generation layer choosing or wording a
clinical intervention is precisely what `evidence.md` §14 rules out, and what
`CLAUDE.md` forbids. It would also break offline-first, add keys to the APK,
and add latency to an acute path.

**Why not even at build time, this weekend.** The evidence corpus is nine
sources. Embedding, indexing and retrieving over nine documents costs more
hours than reading them, and the output still needs full human verification.
The curation pass a human does with an AI assistant *is* the retrieval layer,
and its artifact is `evidence.md` → `evidence_catalog.json`.

**Deferred path, post-hackathon, if the corpus grows past ~100 sources:**

1. Offline ingestion of a vetted source set (PDF/HTML → chunks + metadata).
2. Local embedding index built **on a workstation**, never on-device.
3. Retrieval assists a **human curator** drafting intervention metadata.
4. A human verifies every citation; the diff is reviewed like code.
5. The shipped artifact stays a static JSON catalog. **The runtime never
   retrieves, never generates, and never sees a model.**

That boundary — AI personalizes *presentation*, never invents the intervention
— is the pitch's strongest technical line (`evidence.md` §14). Say it.

---

## 11. Safety and Emergency Boundaries

### 11.1 Claims

Approved framing (verbatim, §2). Banned: "clinically proven", "AI treatment",
"cure", "diagnoses your PTSD type", "stops a flashback in 90 seconds",
"regulates your nervous system", "first-line treatment", "guaranteed to calm
you in 10 minutes".

Allowed statements, each shown beside its source:

| Statement | Source |
|---|---|
| "Grounding is an evidence-informed component used in evaluated PTSD interventions." | Lewis 2017 (`evidence.md` §5) |
| "Clinical guidance recommends self-calming techniques for managing arousal." | NICE NG116 (§6) |
| "Clinical guidance recognises flashback management as a treatment component." | NICE NG116 (§9) |
| "Relaxation techniques have been studied as part of PTSD treatment." — always with: "The 2023 VA/DoD guideline states evidence is insufficient to recommend it as a standalone treatment." | Thorp 2019; Vera 2022; VA/DoD 2023 (§7) |
| "Emotion-regulation skills have been studied as preparation for, or an adjunct to, trauma-focused treatment." | Bryant 2013 (§8) |
| "Symptom-state assessment is more appropriate than sorting people into a trauma type." | Cloitre 2018 (§1, §4) |
| "Clinical guidance advises caution with computerised trauma-focused approaches where dissociative symptoms are prominent, so Anchor routes dissociation separately." | NICE NG116; VA NCPTSD (§10) |

### 11.2 The safety screen — "I need help now"

Reached on `WORSE` (SF3), or from a persistent low-emphasis affordance in any
session. On entry: **stop all haptics and audio immediately.**

Three explicit choices, no inference, no automation:

| Action | Android implementation | Permission | Offline |
|---|---|---|---|
| Call emergency services | `Intent(ACTION_DIAL, Uri.parse("tel:$configuredNumber"))` — **`ACTION_DIAL`, never `ACTION_CALL`**: it opens the dialer pre-filled and the user presses call | **None** | Yes — a call is not internet |
| Contact trusted person | `Intent(ACTION_SENDTO, Uri.parse("smsto:$number"))` with `sms_body` pre-filled | **None** (no `SEND_SMS`, no auto-send) | Composes offline; sends when the network returns |
| Continue grounding | Return to the session with a low-arousal intervention | — | Yes |

- **The emergency number is user-configured** in onboarding/settings, with a
  short list of common defaults and a prompt to verify the correct local
  number. Anchor makes **no claim** about which number is correct in the
  user's jurisdiction, and no regulatory-compliance claim of any kind.
- **No dialer app** (rare, e.g. some tablets): catch
  `ActivityNotFoundException`, show the number as large selectable text.
- **Cancel** returns to the session unchanged; only `safetyScreenOpened = true`
  is logged — never who was contacted or whether a call was placed.
- "Anchor is not an emergency service" is visible on this screen and beside
  every contact action.

---

## 12. Privacy and Offline Architecture

Everything is local, in app-private storage, with no backend, no account and no
analytics.

| Data | Store | Why |
|---|---|---|
| `UserProfile`, preferences, settings | DataStore Preferences | Small, flat, atomic |
| Routines, episodes, outcomes, follow-ups | Single JSON file via `kotlinx.serialization` in `filesDir` | Append-and-rewrite; a few thousand records is far beyond need. **No Room** — KSP setup costs ~45 min for query power this app never uses |
| Safety phrase audio | `filesDir/safety_phrase.m4a`, app-private | Never leaves the device; deletable in Settings |
| Catalogs | `assets/*.json`, read-only | Shipped, never mutated |

Must work with **no network**: ANCHOR NOW, haptics, routines, catalog, state
selection, routing, ranking, episode logging, personalization, safety phrase,
follow-up scheduling, safety screen. Nothing else exists in the MVP, so the
whole app is offline by construction.

Permissions requested: `POST_NOTIFICATIONS` (API 33+, for the follow-up) and
optional `RECORD_AUDIO` (safety phrase). Both decline gracefully. `VIBRATE` is
normal and auto-granted. **No `SEND_SMS`, no `CALL_PHONE`, no location, no
`READ_CONTACTS`** (the trusted contact is typed, not picked, to avoid the
permission entirely).

---

## 13. Technical Architecture

Single Gradle module. **Kotlin + Jetpack Compose + Material 3**, `minSdk 26`,
`targetSdk 35`. Dependencies beyond the Compose BOM: `datastore-preferences`,
`kotlinx-serialization-json`. **No Hilt, no Room, no Retrofit, no WorkManager,
no TFLite, no network client.**

```
app/src/main/java/com/anchor/
  AnchorApp.kt                     // Application; builds the service graph by hand
  MainActivity.kt                  // home: ANCHOR NOW + routines/history/settings
  AnchorActivity.kt                // the session host; singleTask
  ServiceLocator.kt                // ~30 lines of manual wiring instead of DI

  core/haptics/    HapticEngine.kt (interface) · SystemHapticEngine · DebugHapticEngine
                   HapticPattern.kt · HapticPatterns.kt · HapticCapabilities.kt
  core/audio/      AudioEngine.kt (interface) · SafetyPhraseRecorder · SafetyPhrasePlayer
                   NoiseLoopPlayer.kt            (IF TIME)
  core/entry/      EntryPoint.kt (enum) · AnchorLauncher.kt
                   AnchorTileService.kt · AnchorNotification.kt · shortcuts.xml   (SHOULD)
  core/followup/   FollowUpScheduler.kt · FollowUpReceiver.kt · FollowUpNotifier.kt

  domain/model/    CurrentState.kt · UserProfile.kt · Intervention.kt · EvidenceRecord.kt
                   RecoveryRoutine.kt · RoutineStep.kt · Episode.kt
                   InterventionOutcome.kt · FollowUp.kt
  domain/safety/   SafetyFilter.kt · SafetyRule.kt
  domain/routing/  InterventionRouter.kt · RankComparator.kt
  domain/personalization/ PersonalizationScorer.kt · PersonalizationConfig.kt
                          ResponseHistory.kt · InsightBuilder.kt
  domain/session/  SessionState.kt · SessionEvent.kt · SessionViewModel.kt · SessionReducer.kt
  domain/routine/  RoutineValidator.kt · RoutineSelector.kt
  domain/recovery/ RecoveryState.kt · RecoveryConfig.kt          (SHOULD)

  content/         CatalogLoader.kt · InterventionCatalog.kt · EvidenceRepository.kt
                   LearningModules.kt                            (IF TIME)
  data/            AnchorStore.kt · ProfileStore.kt · EpisodeStore.kt · RoutineStore.kt
                   ActiveSessionStore.kt
  ui/home/ ui/session/ ui/state/ ui/routine/ ui/evidence/ ui/safety/
  ui/onboarding/ ui/history/ ui/settings/ ui/theme/

app/src/main/assets/interventions.json
app/src/main/assets/evidence_catalog.json
app/src/main/res/raw/brown_noise.ogg        (IF TIME)
```

**Interfaces only where they earn it:** `HapticEngine` and `AudioEngine`
(platform boundaries, and the only way emulator development works),
`SafetyFilter` / `InterventionRouter` / `PersonalizationScorer` (pure functions,
testable without Android), the four `*Store` classes (persistence seam).
Everything else is a plain class. `ServiceLocator` is a hand-written object — a
DI framework would cost more than it saves at this size.

---

## 14. Data Models

Representative Kotlin. All `@Serializable`. **No field anywhere can hold a
trauma narrative** — an acceptance criterion, not a convention.

```kotlin
enum class CurrentState { PANICKY, HYPER_ALERT, FLASHBACK, DISSOCIATION, FROZEN, NOT_SURE }

enum class Technique { GROUNDING, EXTERNAL_ORIENTATION, SELF_CALMING,
    FLASHBACK_MANAGEMENT, RELAXATION, EMOTION_REGULATION, SENSORY_COMFORT }

enum class Delivery { HAPTIC, AUDIO, VOICE, VISUAL, TEXT }

enum class EvidenceStatus { GUIDELINE_RECOMMENDED, RCT_COMPONENT,
    RCT_STUDIED_WITH_LIMITATIONS, ADJUNCT_EVIDENCE, CLINICAL_CAUTION_DERIVED,
    DELIVERY_METHOD_UNEVALUATED, EVIDENCE_GAP, PRODUCT_DECISION }

enum class Reassessment { BETTER, SAME, WORSE }
enum class FollowUpResult { CALM, BETTER_NOT_CALM, STILL_DISTRESSED, NO_RESPONSE, DISMISSED }

@Serializable
data class Intervention(
    val id: String,                       // "E004"
    val name: String,
    val shortLabel: String,               // <= 20 chars, used in the session UI
    val allowedStates: Set<CurrentState>,
    val technique: Technique,
    val delivery: Set<Delivery>,
    val defaultDurationSec: Int,
    val minDurationSec: Int,
    val maxDurationSec: Int,
    val steps: List<String>,              // pre-authored copy; never user-editable
    val evidenceIds: List<String>,        // -> EvidenceRecord.id
    val evidenceStatus: EvidenceStatus,
    val limitations: String?,             // e.g. the VA/DoD relaxation caveat
    val safetyNotes: String?,
    val interoceptive: Boolean,           // drives SF2
    val requiresPrefs: Set<String> = emptySet(),   // e.g. "voiceOk" for E003
    val userCustomizable: Set<String> = setOf("durationSec", "enabled", "order")
)

@Serializable
data class EvidenceRecord(
    val id: String,                       // "LEWIS2017"
    val citation: String, val year: Int, val url: String,
    val sourceType: String,               // "RCT" | "CLINICAL_GUIDELINE" | "MEASURE"
    val claim: String,                    // transcribed from evidence.md
    val limitations: String?,
    val status: EvidenceStatus
)

@Serializable
data class UserProfile(
    val triggerSituations: Set<String> = emptySet(),   // evidence.md §2 option list
    val audioOk: Boolean = true, val voiceOk: Boolean = true,
    val hapticIntensity: Float = 1.0f, val touchSensitive: Boolean = false,
    val reducedVisual: Boolean = false,
    val preferredDelivery: List<Delivery> = listOf(Delivery.HAPTIC),
    val helpfulTechniques: Set<Technique> = emptySet(),
    val notForMe: Set<String> = emptySet(),            // intervention ids -> SF8
    val trustedContactNumber: String? = null,
    val emergencyNumber: String? = null,               // user-verified, never assumed
    val onboardingComplete: Boolean = false
)   // note: no trauma narrative field exists, by design

@Serializable data class RoutineStep(
    val interventionId: String, val durationSec: Int,
    val enabled: Boolean = true, val order: Int
)

@Serializable data class RecoveryRoutine(
    val id: String, val name: String,                  // label only, <= 40 chars
    val forState: CurrentState?,                       // null = applies to any
    val steps: List<RoutineStep>,
    val isDefault: Boolean = false, val isSeeded: Boolean = false
)

@Serializable data class InterventionOutcome(
    val interventionId: String, val state: CurrentState?,
    val startedAt: Long, val durationSec: Int,
    val reassessment: Reassessment?, val wasFollowedByAnother: Boolean = false
)

@Serializable data class FollowUp(
    val dueAt: Long, val respondedAt: Long? = null,
    val result: FollowUpResult = FollowUpResult.NO_RESPONSE
)

@Serializable data class Episode(
    val id: String, val startedAt: Long, val endedAt: Long? = null,
    val entryPoint: EntryPoint,
    val selectedState: CurrentState? = null, val stateWasSkipped: Boolean = true,
    val initialDistress: Int? = null,                  // optional 1-5, skippable
    val routineId: String? = null,
    val outcomes: List<InterventionOutcome> = emptyList(),
    val endedBy: EndReason = EndReason.USER_ENDED,     // USER_ENDED|BETTER|WORSE_SAFETY|ABANDONED
    val safetyScreenOpened: Boolean = false,
    val followUp: FollowUp? = null,
    val timeToFirstBetterMs: Long? = null
)

@Serializable data class PersonalizationScore(
    val interventionId: String, val state: CurrentState?,
    val observations: Int, val score: Float, val betterCount: Int
)   // derived, cached; recomputed from outcomes on load
```

---

## 15. Runtime State Machine

### 15.1 States and allowed transitions

```
IDLE
 → ANCHOR_OPEN            (entry tapped; activity created)
 → ACTIVATING             (haptic fired; routine resolved)          [auto, <300ms]
 → GROUNDING(step)        (routine step running; state overlay offered at ~4 s)
      ⇄ MANUAL_OVERRIDE   (user picks a texture directly; always available)
 → OPTIONAL_STATE_SELECT  (overlay; skip/ignore returns to GROUNDING)
 → ROUTING                (transient, no UI, deterministic)
 → INTERVENTION(step)     (a routed intervention running)
 → REASSESSMENT
      ├ BETTER  → SESSION_COMPLETE
      ├ SAME    → ROUTING            (alreadyTried grows)
      └ WORSE   → SAFETY_STOP        (haptics + audio stop immediately)
 SAFETY_STOP → SESSION_COMPLETE
 SESSION_COMPLETE → FOLLOW_UP_PENDING → FOLLOW_UP_COMPLETE → IDLE
```

`GROUNDING` and `INTERVENTION` are the same rendering path with different
provenance (routine step vs routed selection); they stay distinct in the enum
because the episode log and the personalization scorer treat them differently.

**Reconciliation with revision 2:** `ACTIVATING`, `MODE_SWITCH` (now
`MANUAL_OVERRIDE`), `REASSESS`, `RECOVERY` are preserved. `STATE_SELECT` moved
from *before* grounding to an optional overlay *during* it — the single most
important change in this revision. `EASING`/`CHECK_IN` are folded into
`SESSION_COMPLETE`. `FOLLOW_UP_*` are new.

### 15.2 Data held per state

| State | Holds |
|---|---|
| `ANCHOR_OPEN` | `episodeId`, `entryPoint`, `startedAt` |
| `ACTIVATING` | + resolved `routineId`, step 0 |
| `GROUNDING` / `INTERVENTION` | + `currentStep`, `stepStartedAt`, `stepDeadline` |
| `OPTIONAL_STATE_SELECT` | + the underlying step keeps running |
| `ROUTING` | + `alreadyTried`, `selectedState` |
| `REASSESSMENT` | + the outcome awaiting a verdict |
| `SESSION_COMPLETE` | full `Episode` |
| `FOLLOW_UP_PENDING` | `episodeId`, `dueAt` |

### 15.3 Lifecycle and durability

- **Process death.** `ActiveSessionStore` persists `{episodeId, state, stepIndex,
  startedAt, alreadyTried, selectedState}` on **every transition** (a small
  atomic write). On relaunch: age < 30 min → offer *"Continue where you left
  off?"*; older → close as `ABANDONED`, keep partial outcomes, **no follow-up
  scheduled**.
- **Backgrounded.** `onStop` stops haptics and audio deliberately — a phone
  buzzing forever in a pocket is a real failure mode — and pauses the step
  timer. Returning resumes the same step. A session backgrounded > 30 min is
  closed as `ABANDONED`.
- **User exits.** Any partial episode is written with `endedBy = USER_ENDED`;
  outcomes without a reassessment are stored with `reassessment = null` and are
  **excluded from personalization** (no verdict, no data point).
- **Haptics fail mid-session.** `HapticEngine` reports failure once; the session
  continues on the visual pacer and text, with a one-line non-blocking notice.
- **Audio fails.** The step continues silently; E003 falls back to on-screen
  text.
- **Follow-up notification never delivered.** The pending follow-up is shown
  in-app on next launch; after 60 min it is recorded `NO_RESPONSE`.

---

## 16. Android Implementation Details

### 16.1 Haptics

```kotlin
interface HapticEngine {
    val capabilities: HapticCapabilities
    fun play(pattern: HapticPattern, intensity: Float = 1f): Boolean  // false = unavailable
    fun stop()
}
data class HapticCapabilities(
    val hasVibrator: Boolean, val hasAmplitudeControl: Boolean,
    val supportsPrimitives: Boolean
)
```

- **Getting the vibrator.** API 31+: `getSystemService(VibratorManager::class.java).defaultVibrator`.
  Below: `getSystemService(Vibrator::class.java)`. Wrap once in
  `SystemHapticEngine`; nothing else in the app touches these classes.
- **Patterns** via `VibrationEffect.createWaveform(timings, amplitudes, repeatIndex)`
  (API 26+), looping with `repeatIndex = 0`:
  - **Heartbeat** — `timings [0,70,120,70,900]`, `amplitudes [0,200,0,150,0]`, repeat.
  - **Purr** — ~40 ms segments with amplitudes oscillating `90…140`, repeat.
    Needs amplitude control; without it, short on/off ticks at ~45 ms.
  - **Marble** — a 1.2 s ramp up then down across ~24 segments, repeat.
  - **Breath (E004)** — one cycle as a single waveform: inhale ramp up 4 s,
    hold 4 s (silent or faint), exhale ramp down 6 s; loop. Timing constants in
    `BreathConfig`, default 4-4-6.
- **API-level differences.** `hasAmplitudeControl()` false (common on budget
  devices) → binary on/off variants, chosen inside `HapticPatterns`, never at
  call sites. `VibrationEffect.Composition` primitives (API 31+) are used
  **only** when `areAllPrimitivesSupported(...)` is true — several Samsung
  models return false; the waveform path is the default, not the fallback.
- **Do Not Disturb.** API 33+: `VibrationAttributes.Builder().setUsage(USAGE_ACCESSIBILITY)`.
  Below: `AudioAttributes` with `USAGE_ASSISTANCE_ACCESSIBILITY`. **Without
  this, DND silences the haptics** — the most common way a haptic demo dies.
- **Intensity.** `profile.hapticIntensity` scales amplitudes at build time in
  `HapticPatterns`, clamped to `[0.3, 1.0]`. One UI's own vibration-intensity
  setting also applies and cannot be overridden — note it in the demo checklist.
- **Emulator.** No vibration, ever. `DebugHapticEngine` logs and drives an
  on-screen pulse so UI work proceeds. **Tuning is meaningless off-device**:
  every pattern is signed off on the Samsung.

### 16.2 Entry points

- `MainActivity` — ANCHOR NOW occupies the top two-thirds. Tapping starts
  `AnchorActivity` and calls `hapticEngine.play(...)` **before** `startActivity`.
- `AnchorActivity` — `launchMode="singleTask"`, `showWhenLocked`,
  `turnScreenOn`, `excludeFromRecents="false"`. Haptic fires in `onCreate`
  before `setContent`.
- Shortcut — static `res/xml/shortcuts.xml`, `android:targetClass` =
  `AnchorActivity`, extra `entry_point=SHORTCUT`.
- Tile (SHOULD) — `TileService`, `BIND_QUICK_SETTINGS_TILE`; `onClick()`
  vibrates first, then `startActivityAndCollapse(PendingIntent)` on API 34+.
  Prompt with `StatusBarManager.requestAddTileService()` on API 33+.
- Notification (SHOULD) — ongoing `IMPORTANCE_LOW`, action's `PendingIntent`
  must be `getActivity()` **directly**: since Android 12 a receiver or service
  launched from a notification may not call `startActivity`.
- Side key (SHOULD) — Samsung Settings → Advanced features → Side key → Double
  press → Open app. Zero code; a device-setup step.

### 16.3 Follow-up scheduling

- `AlarmManager.set(RTC_WAKEUP, dueAt, pendingIntent)` — **inexact on purpose**.
  Exact alarms need `SCHEDULE_EXACT_ALARM` on API 31+ and are unjustifiable for
  a ten-minute check-in. **No WorkManager dependency.**
- `FollowUpReceiver` (a manifest-registered `BroadcastReceiver`) posts a
  notification on a dedicated low-importance channel. Text is neutral —
  **"Anchor · checking in"** — never distress or PTSD wording, because it may
  appear on a lock screen someone else can see.
- Tapping opens `AnchorActivity` in follow-up mode: three targets — Calm /
  Better but not calm / Still distressed — plus **Dismiss**.
- Delivery is best-effort: Doze and One UI may delay it. Therefore the pending
  follow-up is **also** rendered in-app on next launch, and expires to
  `NO_RESPONSE` after 60 minutes. Never nag twice.
- `followUpDelayMs` is config: 10 min default, **45 s in debug** for the demo.
- `POST_NOTIFICATIONS` denial: no notification, in-app prompt only. Nothing
  breaks.

### 16.4 Audio

`MediaRecorder` (AAC/m4a, app-private `filesDir`) for the safety phrase;
`MediaPlayer` for playback. `RECORD_AUDIO` requested only at the recording
screen, skippable, with bundled fallback clips. Emulator microphone capture is
unreliable — verify on the device early.

---

## 17. Feature-by-Feature Implementation Notes

Condensed; per-task detail lives in §18.

**17.1 ANCHOR NOW.** One composable, one activity, one call into
`AnchorLauncher.fire(entryPoint)`. The whole feature is the *ordering*: haptic,
then navigation, then persistence. Never the reverse.

**17.2 Session UI.** A single screen driven by `SessionState`. Big step label,
progress ring, STOP always visible, and one contextual affordance (state
overlay → reassessment → next). No navigation stack inside a session.

**17.3 Routine editor.** `LazyColumn` over `RoutineStep`s with drag-to-reorder,
a toggle, and a duration stepper bounded by the intervention. An "add step"
sheet lists the catalog split into *eligible* and *not offered for this state*
(greyed, with reason + evidence link). Save runs `RoutineValidator`; failures
are shown inline, never silently corrected.

**17.4 Catalog loading.** `CatalogLoader` reads both JSONs from assets on first
use, joins `evidenceIds`, and **fails loudly in debug / falls back to
`SAFE_FALLBACK` in release**. A unit test fails the build if any `evidenceId`
is dangling or any `evidenceStatus` is outside the enum.

**17.5 Safety filter.** Pure functions, one file, one test file. The two tests
that matter: a `DISSOCIATION` state can never receive an `interoceptive = true`
intervention; a `WORSE` always terminates.

**17.6 Personalization.** `ResponseHistory` folds `Episode.outcomes` into
per-(intervention, state) aggregates on load, cached in memory. `InsightBuilder`
turns aggregates into approved copy or the "still learning" string.

**17.7 Follow-up.** See §16.3. The subtle part is that the notification is an
optimisation, not the mechanism — the in-app pending prompt is.

**17.8 Recovery/refractory mode (SHOULD, reduced scope).** A persisted
`recoveryUntil` timestamp flips a dimmer theme and a gentle prompt. Duration is
config (12 h default, 2 min debug). UI calls it a **setting**, never a
physiological claim (§24, C3).

**17.9 Deferred trigger modules.** Each is a package under `core/entry/`
implementing the same `AnchorLauncher` call: **volume triple-press** via
`AccessibilityService` + `FLAG_REQUEST_FILTER_KEY_EVENTS` (blockers: Android
13+ restricted-settings block on sideloaded apps — `adb install -i
com.android.vending`; unverified key delivery with the display fully off;
silent disabling by system updates); **earbud triple-click** via `MediaSession`
+ `MediaButtonReceiver`; **rapid power-press** via `ACTION_SCREEN_ON/OFF`
counting; **shake** via accelerometer with a cancel window. ~2–4 h each, none
touching the core.

---

## 18. Task Breakdown

Complexity: **S** ≤ 1.5 h · **M** 2–3 h · **L** 4–5 h · **XL** 6 h+.
Model tier: **FAST** boilerplate/UI · **MEDIUM** ordinary logic · **STRONG**
hardware / safety-critical / API judgement. `‖` = parallelisable.

### PHASE 0 — Setup (H0–H4)

**A1 · Project skeleton** — *Goal:* buildable Compose app on emulator and
Samsung. *Files:* `build.gradle.kts` ×2, `AndroidManifest.xml`,
`MainActivity.kt`, `ui/theme/*`. *APIs:* none. *Accept:* `./gradlew
assembleDebug` passes; launches on both; dark theme. *Test:* install both.
*Complexity:* S (**hard timebox 1.5 h**) · MEDIUM · deps none.
*Fallback:* stock Empty Compose Activity template.

**A2 · Device provisioning** ‖ — *Goal:* the Samsung is demo-ready on day one.
*Files:* `docs/device-setup.md`. *Steps:* battery **Unrestricted**; not in
Sleeping apps; notifications allowed; side-key double-press mapped; vibration
intensity noted. *Accept:* written down and reproducible in 5 minutes.
*Complexity:* S · FAST · deps A1.

**A3 · Domain models + catalog schema** — *Goal:* freeze the contracts so four
devs work in parallel. *Files:* all of `domain/model/`. *Accept:* every model in
§14 compiles as `@Serializable`; **no field can hold a trauma narrative**; both
JSON schemas agreed and committed. *Test:* round-trip serialization test.
*Complexity:* M · STRONG · deps A1. **Review with the whole team — this is the
single hard sync point.**

### PHASE 1 — Haptic core (H2–H10) ‖ Dev A

**B1 · HapticEngine + capabilities** — *Files:* `core/haptics/HapticEngine.kt`,
`SystemHapticEngine.kt`, `DebugHapticEngine.kt`, `HapticCapabilities.kt`.
*APIs:* `VibratorManager`/`Vibrator`, `VibrationEffect`, `VibrationAttributes`.
*Methods:* `play`, `stop`, `capabilities`. *Accept:* correct capability
reporting on device and emulator; **`USAGE_ACCESSIBILITY` on every effect**;
`play` returns `false` and never throws with no vibrator. *Test:* device — feel
it; emulator — logcat + on-screen pulse; toggle DND and confirm haptics
survive. *Emulator limit:* **no vibration at all.** *Complexity:* M · STRONG ·
deps A1.

**B2 · Heartbeat + Purr patterns** — *Files:* `HapticPattern.kt`,
`HapticPatterns.kt`. *Accept:* loop smoothly, distinguishable **by touch** on
the Samsung, start < 200 ms, stop instantly, respect intensity, degrade to
on/off without amplitude control. *Test:* haptic lab, eyes closed, two people.
*Complexity:* L (**tuned on hardware only**) · STRONG · deps B1.
*Fallback:* Heartbeat alone if Purr is indistinct.

**B3 · Breath waveform** — *Files:* `HapticPatterns.kt` (+`BreathConfig`).
*Accept:* 4-4-6 configurable; stays in sync with the visual pacer over 2 min.
*Complexity:* M · MEDIUM · deps B2.

**B4 · Haptic lab screen** (SHOULD) ‖ — *Files:* `ui/settings/HapticLabScreen.kt`.
*Accept:* one button per pattern, intensity slider, capability readout.
*Complexity:* S · FAST · deps B2.

**B5 · Marble texture** (SHOULD) ‖ — *Complexity:* S · FAST · deps B2.

### PHASE 2 — Anchor Now (H4–H8) ‖ Dev A

**C1 · AnchorActivity + AnchorLauncher** — *Files:* `AnchorActivity.kt`,
`core/entry/AnchorLauncher.kt`, `EntryPoint.kt`, manifest. *Accept:* haptic
< 300 ms of tap; `singleTask`; `entryPoint` recorded; `showWhenLocked` verified
once. *Test:* `adb shell am start -n com.anchor/.AnchorActivity`; stopwatch the
cold start (target < 1.5 s to first vibration). *Complexity:* M · STRONG ·
deps B1, A3.

**C2 · Home screen + ANCHOR NOW** — *Files:* `ui/home/HomeScreen.kt`.
*Accept:* button reachable one-handed, top two-thirds; secondary row for
Routines / History / Settings; insight card slot. *Complexity:* M · FAST ·
deps C1.

**C3 · Launcher shortcut** ‖ — *Files:* `res/xml/shortcuts.xml`. *Complexity:*
S · FAST · deps C1.

**C4 · Quick-settings tile** (SHOULD) ‖ — *Accept:* haptic in `onClick()`
before unlock; verified from the **lock-screen shade**; add-tile prompt on API
33+. *Complexity:* M · STRONG · deps C1.

**C5 · Notification quick action** (SHOULD) ‖ — *Accept:* ongoing
`IMPORTANCE_LOW`; action uses `PendingIntent.getActivity()` directly;
`POST_NOTIFICATIONS` denial degrades cleanly. *Complexity:* M · STRONG · deps C1.

### PHASE 3 — Catalog, safety, routing (H4–H14) ‖ Dev D then Dev B

**D1 · Evidence catalog authoring** ‖ — *Files:* `assets/evidence_catalog.json`.
*Accept:* one record per source in `evidence.md`'s reference list, with claim,
limitations, status; **every field traceable to a line in `evidence.md`; zero
invented citations**; reviewed by a second person. *Complexity:* M · STRONG.

**D2 · Intervention catalog authoring** — *Files:* `assets/interventions.json`.
*Accept:* the nine entries of §8.3 with all §14 metadata; E005 carries the
VA/DoD limitation string; E009 marked `EVIDENCE_GAP`; **no copy makes a claim
absent from `evidence.md`**. *Complexity:* M–L · STRONG · deps A3, D1.
**Copy review before merge.**

**D3 · CatalogLoader + integrity test** — *Files:* `content/CatalogLoader.kt`,
`InterventionCatalog.kt`, `EvidenceRepository.kt`, test. *Accept:* loads from
assets; joins evidence; test fails on a dangling `evidenceId` or an
out-of-enum status; malformed JSON → `SAFE_FALLBACK`, never a crash.
*Complexity:* M · MEDIUM · deps D2.

**E1 · SafetyFilter** — *Files:* `domain/safety/SafetyFilter.kt`,
`SafetyRule.kt`, tests. *Accept:* SF1–SF8; **tests prove dissociation never
receives an interoceptive intervention and `WORSE` always terminates**;
empty result → `SAFE_FALLBACK`. *Complexity:* M · STRONG · deps A3, D3.
**Safety-critical — review before merge.**

**E2 · Router + rank comparator** — *Files:* `domain/routing/*`, tests.
*Accept:* §8.4 pipeline; §9.3 precedence; deterministic and reproducible;
`alreadyTried` exclusion across `SAME` loops. *Complexity:* M · STRONG · deps E1.

**E3 · Personalization scorer** — *Files:* `domain/personalization/*`, tests.
*Accept:* §9.2 formulas exactly; constants in `PersonalizationConfig`;
`MIN_OBS` gate on surfacing; §9.4 exploration is deterministic; tests cover a
new intervention not being buried and a user pin overriding score.
*Complexity:* M · STRONG · deps A3.

### PHASE 4 — Session (H8–H20) ‖ Dev B

**F1 · Session state machine** — *Files:* `domain/session/SessionState.kt`,
`SessionEvent.kt`, `SessionReducer.kt`, `SessionViewModel.kt`, tests.
*Accept:* §15.1 transitions; unit tests cover all three reassessment branches
and the `SAME` loop; survives configuration change; runs with a no-op
`HapticEngine` and an empty catalog. *Complexity:* L · STRONG · deps A3, E2.

**F2 · ActiveSessionStore + lifecycle** — *Files:* `data/ActiveSessionStore.kt`.
*Accept:* §15.3 exactly — persisted on every transition; resume offered under
30 min; `onStop` stops haptics; `ABANDONED` closure schedules no follow-up.
*Test:* `adb shell am kill com.anchor` mid-session, relaunch. *Complexity:* M ·
STRONG · deps F1.

**F3 · Session UI** — *Files:* `ui/session/*`. *Accept:* step label, progress,
STOP always visible; manual texture override one tap; no navigation stack
inside a session; readable at arm's length; one-handed. *Complexity:* XL ·
MEDIUM · deps F1, B2.

**F4 · Optional state overlay** — *Files:* `ui/state/StateOverlay.kt`.
*Accept:* appears ~4 s in, six options ≤ 3 words, **the running haptic never
stops**, dismiss/ignore keeps the default path, `stateWasSkipped` recorded.
*Complexity:* S · MEDIUM · deps F3.

**F5 · Reassessment** — *Files:* `ui/session/ReassessScreen.kt`. *Accept:*
three large targets; wired to the three branches; outcome recorded before
transition. *Complexity:* S · MEDIUM · deps F1.

**F6 · Safety / emergency screen** — *Files:* `ui/safety/*`. *Accept:* haptics
and audio stop **immediately** on entry; three explicit actions per §11.2;
`ACTION_DIAL` not `ACTION_CALL`; no `SEND_SMS`; `ActivityNotFoundException`
handled; "not an emergency service" visible; cancel returns unchanged.
*Test:* airplane mode; a device with no SIM. *Complexity:* M · STRONG · deps F1.

### PHASE 5 — Routines (H10–H20) ‖ Dev C

**G1 · Routine model, seeds, selector, validator** — *Files:*
`domain/routine/*`, `data/RoutineStore.kt`, tests. *Accept:* §7.5 seeds ship;
`RoutineSelector` implements §7.4; `RoutineValidator` enforces V1–V7 with
tests; routines re-validated at run time, not only at save. *Complexity:* M ·
STRONG · deps A3, E1.

**G2 · Routine editor UI ("My Anchor")** — *Files:* `ui/routine/*`. *Accept:*
reorder, toggle, bounded duration stepper, add-step sheet splitting eligible
from **not-offered-for-this-state (greyed, with reason + evidence link)**;
**no free-text field anywhere except the routine name**; validation errors
inline. *Complexity:* L · MEDIUM · deps G1, K1.
*Fallback:* toggle + reorder only; fixed durations.

### PHASE 6 — Persistence, episodes, personalization surfaces (H10–H22) ‖ Dev C

**H1 · Storage layer** — *Files:* `data/AnchorStore.kt`, `ProfileStore.kt`,
`EpisodeStore.kt`. *Accept:* DataStore for profile; JSON file for
episodes/routines; survives restart; a write failure never crashes or blocks a
session; nothing leaves the device. *Complexity:* M · MEDIUM · deps A3.

**H2 · Episode + outcome recording** — *Accept:* every session writes an
`Episode` with entry point, state (or skipped), outcomes, `endedBy`,
`timeToFirstBetterMs`; outcomes without a reassessment are excluded from
personalization. *Complexity:* M · MEDIUM · deps F1, H1.

**K1 · Evidence traceability screen** — *Files:* `ui/evidence/*`. *Accept:*
two taps from any running intervention to source, year, claim, status,
limitations, **and the ranking reasons that selected it**; status rendered
verbatim; **no free-text clinical copy in the composable**. *Complexity:* S–M ·
MEDIUM · deps D3. *This wins the judge Q&A — do not cut.*

**K2 · Insight card + history screen** — *Files:* `ui/history/*`,
`InsightBuilder`. *Accept:* §9.5 copy exactly, gated at `MIN_OBS = 3`, with
"your own reports · not a clinical measure"; empty state handled; per-technique
counts visible. *Complexity:* M · MEDIUM · deps E3, H2.

### PHASE 7 — Follow-up (H16–H24) ‖ Dev A after Phase 2

**I1 · Follow-up scheduler + receiver** — *Files:* `core/followup/*`, manifest
receiver, channel. *APIs:* `AlarmManager.set(RTC_WAKEUP)`, `NotificationManager`.
*Accept:* scheduled on `SESSION_COMPLETE` (never on `ABANDONED`); neutral
notification text; debug delay 45 s; cancels if answered in-app first.
*Test:* on device with the debug delay; then with the screen off.
*Complexity:* M · STRONG · deps F1, H2.

**I2 · Follow-up UI + no-response handling** — *Accept:* three options plus
Dismiss; pending follow-up also shown in-app on next launch; expires to
`NO_RESPONSE` after 60 min; feeds personalization; never nags twice.
*Complexity:* S–M · MEDIUM · deps I1.

### PHASE 8 — Profile, onboarding, phrase (H14–H24) ‖ Dev C

**J1 · Onboarding** — *Accept:* disclaimer → **"You never need to describe what
happened to you"** verbatim → trigger situations (multi-select incl. prefer-not-
to-say) → sensory/delivery preferences → optional safety phrase → optional
emergency/trusted numbers; **every screen skippable**; ≤ 60 s; re-runnable.
*Complexity:* M · FAST · deps H1.

**J2 · Safety phrase record + playback** — *APIs:* `MediaRecorder`,
`MediaPlayer`, `RECORD_AUDIO`. *Accept:* app-private file; plays < 300 ms in a
session; stops instantly; denial leaves a fully usable app and E003 falls back
to text. *Emulator limit:* mic capture unreliable — verify on device.
*Complexity:* M · STRONG · deps H1.

**J3 · Settings** ‖ — profile, haptic intensity, recovery duration, numbers,
data deletion, disclaimer, entry-point status. *Complexity:* M · FAST.

### PHASE 9 — SHOULD / IF TIME (H22–H28)

**L1 · Recovery/refractory mode** (SHOULD, reduced) — dim theme + prompt +
config duration. *S–M · MEDIUM.*
**L2 · Trusted contact** (SHOULD) — typed number, pre-filled composer, no
permissions. *S–M · MEDIUM.*
**L3 · Brown noise E009** (IF TIME) — looping raw asset, safe volume, instant
stop, never primary, labelled `EVIDENCE_GAP`. *S · FAST.*
**L4 · Learning modules** (IF TIME) — 3–4 modules referencing evidence ids
only, no new claims. *M–L · FAST/STRONG copy.*
**L5 · Full insights view** (IF TIME) — descriptive statistics only, no
interpretation, no clinical vocabulary; debug seeding labelled sample data.
*M–L · MEDIUM.*

### PHASE 10 — Hardening (H28–H36)

**M1 · Evidence and copy audit** — every user-facing clinical sentence traced
to `evidence.md`; banned phrasings (§11.1) grepped and absent; `EVIDENCE_GAP`
items visibly labelled. *S–M · STRONG.* **Do not skip.**
**M2 · Device matrix pass** — entry points, DND, silent, battery saver,
reboot, reinstall, process death mid-session, airplane mode. *S · FAST.*
**M3 · Demo hardening** — signed APK, seeded labelled history, two dry runs,
documented recovery path per feature, backup screen recording. *M · FAST.*

### 18.4 Budget and the cut ladder

MUST ≈ **64 h**. Four devs ≈ 65–70 effective hours; three devs ≈ 50.
**It fits four with no slack, and does not fit three.** Cut in this order,
without debate, when a gate slips:

1. Marble (B5) and Purr (B2 second pattern) → Heartbeat only
2. Haptic lab (B4) → capability text in Settings
3. Tile + notification (C4, C5) → shortcut and app icon only
4. Full insights (L5), brown noise (L3), learning modules (L4)
5. Recovery mode (L1) → the follow-up already covers "after"
6. Trusted contact (L2) → safety screen keeps dial + static text
7. Routine editor (G2) → reduced scope: toggle and reorder only
8. Safety-phrase **recording** (J2) → bundled clips, playback retained
9. Catalog from nine interventions to five (E001, E003, E004, E007, E008)

**Never cut:** safety filter, dissociation pathway, reassessment, safety
screen, evidence traceability screen, personalization scorer. Those four hours
are the entire differentiator.

---

## 19. Team Parallelization

| Dev | Track | Tasks |
|---|---|---|
| **A** — strongest Android, owns the device | Hardware + entry + follow-up | A1 → A2 → B1 → B2 → B3 → C1 → C2 → C3 → (C4, C5) → I1 → I2 → M2 |
| **B** — core logic | Session + routing | A3 (with team) → E1 → E2 → F1 → F2 → F3 → F4 → F5 → F6 |
| **C** — data + experience | Persistence, routines, onboarding | H1 → G1 → G2 → H2 → J1 → J2 → J3 |
| **D** — evidence + surfaces | Catalog + personalization UI | D1 → D2 → D3 → E3 → K1 → K2 → M1 → M3 |

**Critical path:** `A1 → A3 → E1 → E2 → F1 → F3 → F5 → H2 → K2`. Everything
else can slip a little; this cannot.

**Shared files (coordinate, don't co-edit):** `AndroidManifest.xml` (A, and
whoever adds a receiver — announce before touching), `domain/model/*` (frozen
after A3; changes need a shout), `ServiceLocator.kt` (append-only),
`ui/theme/*` (A only).

**Integration points:** H6 models frozen · H10 catalog + filter usable by the
session · H14 session drives real routing · H20 episodes feed personalization ·
H24 follow-up closes the loop. Integrate on a shared branch at every gate.

**Attempt early because they are risky:** B1/B2 (device-only verification),
I1 (One UI alarm/notification delivery), F2 (process death), C4 (tile
lock-screen behaviour).

**Timeboxed:** C4+C5 together 4 h — abandon to the shortcut if they fight back.
G2 5 h — fall back to reduced scope. I1 3 h — fall back to in-app-only
follow-up (still a complete feature; the notification is the optimisation).

---

## 20. Testing and Failure Handling

**Unit tests — the five worth writing:** `SafetyFilter` (SF1–SF8, with the two
named cases), `InterventionRouter` (determinism, exclusion, precedence),
`PersonalizationScorer` (formulas, `MIN_OBS`, new-option protection, pin
override), `SessionReducer` (every transition, all three branches),
`RoutineValidator` (V1–V7). Plus the catalog integrity test (D3). Everything
else is manual — broad suites are not a good use of hackathon hours.

**Manual passes:** emulator install-and-walk after every task; **device pass
required** for B1, B2, B3, C1, C4, C5, I1, J2, L3 — these are not done until
confirmed on the Samsung; routing walk-through at H20 (all six states × three
reassessment branches); offline pass at H28 and H32; cold-start timing at H32
(< 1.5 s to first vibration); fresh-install pass at H32 (empty profile, empty
log, every skip path).

**Failure matrix**

| Dependency | Failure | Behaviour |
|---|---|---|
| Haptics | Absent / no amplitude control / fails mid-session | On-off waveform → visual pulse + text; one non-blocking notice; session continues |
| Catalog JSON | Missing or malformed | `SAFE_FALLBACK` routine; loud in debug, silent-safe in release; never an empty screen |
| State overlay | Ignored | Default pathway; `stateWasSkipped = true` |
| Mic / `RECORD_AUDIO` | Denied or fails | Bundled clips; E003 falls back to text |
| Audio | Playback error | Silent session; haptics unaffected |
| Storage | Read/write error | Session runs; log write dropped with a notice |
| `POST_NOTIFICATIONS` | Denied | In-app follow-up only |
| Alarm delayed/dropped | Doze / One UI | In-app pending prompt on next launch; `NO_RESPONSE` after 60 min |
| Process death | Mid-session | Resume offer < 30 min; else `ABANDONED`, no follow-up |
| Dialer / SMS app absent | `ActivityNotFoundException` | Show the number as large selectable text |
| Network | Always assumed absent | Nothing depends on it |

---

## 21. Demo Plan (3 minutes, deterministic, airplane mode)

Pre-seeded state: a profile, a customised HYPER-ALERT routine, and ~8 labelled
sample episodes so personalization has something to say. Debug build: follow-up
at 45 s, recovery at 2 min.

1. **Before.** Ten seconds on **My Anchor** — their routine: Heartbeat 60 s →
   Paced breathing 2 min → safety phrase. Say the line: *"the user never
   describes what happened to them."*
2. **Distress.** Unlock, one tap on **ANCHOR NOW**. The heartbeat starts before
   the screen settles. *(Backup: the tile, or the app icon.)*
3. **Their routine runs.** No question was asked. Hand the phone to a judge so
   they feel it.
4. **The overlay appears.** Tap **"Far away / not real."**
5. **Watch the route change.** Anchor drops breathing and switches to external
   orientation. Say why: clinical guidance advises caution with computerised
   trauma-focused approaches where dissociative symptoms are prominent, so the
   safety filter excludes inward-focused techniques for this state
   (`evidence.md` §10).
6. **"Where did this come from?"** Two taps: source, year, claim, evidence
   status, limitations, and the ranking reasons. **The Q&A moment.**
7. **Reassess → Same.** Anchor offers the next eligible intervention, excluding
   what was tried. Then **Better**.
8. **Session complete**, episode written locally.
9. **Follow-up fires** 45 s later on the notification shade → "Better, but not
   fully calm."
10. **Insight card:** *"During hyper-alert episodes you reported feeling better
    after Heartbeat 7 of 8 times — your own reports, not a clinical measure."*
11. **Edit the routine live** — drag Heartbeat to the top. Show a greyed
    intervention with its "not offered for this state" reason.
12. **Close.** Airplane mode the whole time. No backend, no account, no runtime
    AI. Evidence-informed stabilization — not a diagnosis, not a treatment, not
    an emergency service. Volume-button triggers: designed, scoped,
    deliberately deferred (§17.9).

---

## 22. 36-Hour Milestones

| Gate | Definition |
|---|---|
| **H4** | Builds and installs; **models frozen (A3)**; device provisioned |
| **H6** | **Vertical slice:** ANCHOR NOW → immediate heartbeat on the Samsung |
| **H12** | Catalog + safety filter + router pass their tests; entry points work |
| **H20** | Full acute loop: tap → routine → overlay → routing → intervention → reassess → episode written |
| **H24** | Follow-up closes the loop; personalization surfaces an insight; routine editor usable |
| **H28** | **Feature freeze.** SHOULD/IF-TIME in or cut per §18.4 |
| **H32** | Signed demo build, seeded data, evidence audit done, two dry runs |
| **H36** | Present |

---

## 23. Complete Feature Inventory

Every feature proposed anywhere in `plan.md`, `evidence.md` or `CLAUDE.md`,
including cut ones. Complexity S/M/L/XL as §18. Demo value 1–5.

### Entry / triggers

| Feature | Purpose | User-facing? | Priority | Status | Evidence-backed? | Main dependency | Complexity | Demo | Reason |
|---|---|---|---|---|---|---|---|---|---|
| ANCHOR NOW one-tap | Primary entry; relief in one action | Yes | MUST | Planned | N/A | B1 | M | 5 | The new product's front door |
| `AnchorActivity` | Session host; `showWhenLocked` retained | Partly | MUST | Planned | N/A | B1 | M | 4 | Every door lands here |
| Launcher shortcut | Long-press icon → ground me now | Yes | MUST | Planned | N/A | C1 | S | 2 | 15 minutes, never breaks |
| Quick-settings tile | Lock-screen shade access | Yes | SHOULD | Planned | N/A | C1 | M | 4 | Demoted: lock screen is no longer the primary story |
| Notification quick action | Always one pull-down away | Yes | SHOULD | Planned | N/A | C1 | M | 3 | Same demotion; cheap accelerant |
| Samsung side-key double-press | Screen-off activation, zero code | Yes | SHOULD | Planned | N/A | Device setup | S | 4 | Free; best screen-off answer |
| `showWhenLocked` / `turnScreenOn` | Lock-screen display without unlock | Yes | SHOULD | Planned | N/A | C1 | S | 3 | One flag; kept, not load-bearing |
| Entry-status screen | Honest per-door availability | Yes | SHOULD | Planned | N/A | C4/C5 | S | 2 | Only meaningful once tile/notification exist |
| Volume triple-press (accessibility) | Zero-look hardware trigger | Yes | DEFERRED | Deferred §17.9 | N/A | Accessibility service | M–L | 4 | Restricted-settings block, unverified screen-off delivery, volume-button side effects |
| Earbud/media-button trigger | Pocket activation | Yes | DEFERRED | Deferred §17.9 | N/A | MediaSession | M | 3 | Needs a live session; steals media controls |
| Rapid power-press trigger | Hardware trigger without accessibility | Yes | DEFERRED | Deferred §17.9 | N/A | Screen on/off receiver | M | 3 | Needs a surviving process; collides with Emergency SOS |
| Shake trigger | Motion activation | Yes | DEFERRED | Deferred §17.9 | N/A | Accelerometer | M | 2 | False positives; needs a live process |

### Acute support

| Feature | Purpose | User-facing? | Priority | Status | Evidence-backed? | Main dependency | Complexity | Demo | Reason |
|---|---|---|---|---|---|---|---|---|---|
| `HapticEngine` + capability detection | One vibration boundary; honest degradation | Partly | MUST | Planned | N/A | A1 | M | 3 | Emulator work is impossible without it |
| Heartbeat texture | Signature tactile anchor | Yes | MUST | Planned | Delivery only — grounding class, Lewis 2017 | B1 | M | 5 | The thing judges feel |
| Purr texture | Second texture | Yes | SHOULD | Cut ladder #1 | Delivery only | B1 | S | 3 | Nice, not necessary |
| Marble texture | Third texture | Yes | SHOULD | Cut ladder #1 | Delivery only | B2 | S | 3 | First cut if haptics run long |
| Vibrotactile breathing | Haptic pacing for E004 | Yes | MUST | Planned | Technique NICE NG116; haptic delivery unevaluated | B2 | M | 4 | Core self-calming delivery |
| Visual breathing pacer | Fallback and companion | Yes | MUST | Planned | Technique NICE NG116 | F3 | S | 3 | Required haptic fallback |
| Session state machine | Explicit spine, testable | No | MUST | Planned | N/A | A3 | L | 2 | Everything plugs into it |
| Session UI | The acute screen | Yes | MUST | Planned | N/A | F1 | XL | 5 | Where the demo lives |
| Optional state selection | Route without diagnosing | Yes | MUST | Planned | Yes — Cloitre 2018 framing | F3 | S | 5 | Now optional, never blocking |
| Manual texture override | "Just give me the heartbeat" | Yes | MUST | Planned | N/A | F3 | S | 3 | Preserves user control |
| Evidence catalog | Machine-readable `evidence.md` | Partly | MUST | Planned | Yes — by construction | A3 | M | 4 | Source of truth |
| Intervention catalog | Pre-authored interventions | Partly | MUST | Planned | Yes — per entry | D1 | M–L | 4 | Nothing clinical is generated |
| Safety filter (SF1–SF8) | Hard vetoes before any selection | No | MUST | Planned | Yes — NICE NG116, VA NCPTSD | D3 | M | 4 | Safety requirement |
| Deterministic routing | Eligible → ranked → chosen | No | MUST | Planned | Yes — `evidence.md` §11, §14 | E1 | M | 4 | The engine |
| Dissociation pathway | Separate external-orientation route | Yes | MUST | Planned | Yes — `evidence.md` §10 | E1 | S | 5 | Safety requirement and the best demo beat |
| Flashback pathway | Present-time orientation + phrase | Yes | MUST | Planned | Yes — NICE NG116 §9 | E2 | S | 4 | Guideline-recognised |
| Self-calming pathway | Breathing, relaxation, emotion tolerance | Yes | MUST | Planned | Yes — NICE NG116; Thorp; Vera; Bryant | E2 | S | 4 | Best-supported cluster |
| Low-demand default (`FROZEN`, `NOT_SURE`) | Safe route where evidence is silent | Yes | MUST | Planned | **No — product decision (C7)** | E2 | S | 2 | Must never block a session |
| Reassessment | Adapt or stop | Yes | MUST | Planned | Yes — `evidence.md` §11 | F1 | S | 5 | Drives everything downstream |
| Safety / emergency screen | Stop and offer explicit options | Yes | MUST | Planned | Yes — `evidence.md` §10 stop rule | F1 | M | 4 | Safety requirement |
| Recovery / refractory mode | Gentler UI after an episode | Yes | SHOULD | Cut ladder #5 | **No — product hypothesis (C3)** | H1 | S–M | 2 | Demoted: the follow-up now owns "after" |
| Haptic lab screen | Verify capability; demo prop | Yes | SHOULD | Cut ladder #2 | N/A | B2 | S | 2 | Useful, not core |
| Environmental sound reality-check | Classify ambient sound | Yes | DROP | Dropped | No — would overclaim | — | XL | 3 | A day's work; risks telling a distressed person they're safe |

### Routines and personalization

| Feature | Purpose | User-facing? | Priority | Status | Evidence-backed? | Main dependency | Complexity | Demo | Reason |
|---|---|---|---|---|---|---|---|---|---|
| Recovery routine model + seeds | Personal one-tap routine | Partly | MUST | Planned | Composition is a preference, not a claim (C15) | A3 | M | 4 | The "My Anchor" idea |
| Routine editor UI | Constrained composition while calm | Yes | MUST | Planned | Constrained to catalog | G1 | L | 5 | Demo step 11 |
| `RoutineValidator` (V1–V7) | Prevent unsafe or arbitrary routines | No | MUST | Planned | Yes — enforces SF rules | G1 | S | 2 | Keeps the editor constrained |
| Runtime routine selection | Default → seeded → safe fallback | No | MUST | Planned | N/A | G1 | S | 3 | Never blocks a session |
| Personalization scorer | Explainable ranking from outcomes | No | MUST | Planned | Descriptive only (C11) | A3 | M | 4 | The learning claim |
| Personal response history | Per-(intervention, state) aggregates | Yes | MUST | Planned | Descriptive only | E3 | S | 4 | Backs the insight copy |
| Insight card | One approved sentence | Yes | MUST | Planned | Descriptive only | E3 | M | 5 | The payoff |
| Routine suggestion from history | "Add Heartbeat?" — never automatic | Yes | SHOULD | Planned | Descriptive only | K2 | S | 3 | Nice; must stay a suggestion |
| Full insights / pattern view | Descriptive statistics | Yes | IF TIME | Cut ladder #4 | No — descriptive (C9) | H2 | M–L | 3 | Insight card carries the story alone |

### Profile / onboarding

| Feature | Purpose | User-facing? | Priority | Status | Evidence-backed? | Main dependency | Complexity | Demo | Reason |
|---|---|---|---|---|---|---|---|---|---|
| Minimal onboarding | Ready in under 60 s, all skippable | Yes | MUST | Planned | Design — `evidence.md` §2 | H1 | M | 3 | Sets up the personalization story |
| Trigger situations | Context without disclosure | Yes | MUST | Planned | Yes — `evidence.md` §2 list | A3 | S | 3 | Verbatim option list |
| Sensory preferences | Hard inputs to SF4 | Yes | MUST | Planned | Design | A3 | S | 2 | Safety-relevant |
| Helpful / not-for-me techniques | Preference vetoes and boosts | Yes | MUST | Planned | Design | A3 | S | 3 | User preference layer |
| Preferred delivery method | Haptic/audio/visual/voice | Yes | MUST | Planned | Design | A3 | S | 2 | Ranking input |
| Optional context note | Short, optional, never analysed | Yes | SHOULD | Planned | Design — `evidence.md` §3 | A3 | S | 1 | Must never become a narrative field |
| "You never need to describe what happened to you" | Stated verbatim | Yes | MUST | Planned | Yes — `evidence.md` §2 | J1 | S | 5 | The ethical centre |
| Personal safety phrase (record) | Their own words, recorded calm | Yes | MUST | Cut ladder #8 | Delivery unevaluated | H1 | M | 4 | Signature feature |
| Safety phrase playback | Delivers E003 | Yes | MUST | Planned | Technique NICE NG116 | J2 | S | 4 | Works with bundled clips if recording is cut |
| Trauma-type classifier | — | — | DROP | Dropped | **Contradicted by `evidence.md` §1, §3** | — | — | — | Would be a diagnosis |
| ITQ-style questionnaire | — | — | DROP | Dropped | Crosses the diagnosis line | — | — | — | A profile is not an assessment instrument |

### Evidence surfaces

| Feature | Purpose | User-facing? | Priority | Status | Evidence-backed? | Main dependency | Complexity | Demo | Reason |
|---|---|---|---|---|---|---|---|---|---|
| Evidence traceability screen | "Where did this come from?" | Yes | MUST | Planned | Yes | D3 | S–M | 5 | Wins the Q&A |
| Evidence status + limitations display | Verbatim enum + caveats | Yes | MUST | Planned | Yes — incl. VA/DoD caveat | K1 | S | 4 | Credibility through honesty |
| Technique → source traceability | `evidenceIds` on every intervention | Yes | MUST | Planned | Yes | D2 | S | 4 | Structural |
| Ranking-reason display | Why this was offered | Yes | SHOULD | Planned | N/A — transparency | K1 | S | 4 | Makes personalization explainable |
| `EVIDENCE_GAP` labelling | Honest marking of unsupported items | Yes | MUST | Planned | Yes — by definition | D2 | S | 3 | Required by §5 of the brief |
| Learning / psychoeducation modules | Education between episodes | Yes | IF TIME | Cut ladder #4 | Yes — references only | K1 | M–L | 2 | Core survives without them |
| Build-time research RAG | Curation assistance | No | DEFERRED | Deferred §10 | N/A | — | L | 1 | Nine sources; reading beats indexing |
| Runtime RAG / LLM | — | — | DROP | Dropped | **Forbidden — `evidence.md` §14, `CLAUDE.md`** | — | — | — | Would make a model the clinical decider |

### Post-episode

| Feature | Purpose | User-facing? | Priority | Status | Evidence-backed? | Main dependency | Complexity | Demo | Reason |
|---|---|---|---|---|---|---|---|---|---|
| Local episode log | Private structured record | Partly | MUST | Planned | N/A — privacy design | A3 | M | 3 | Feeds everything |
| Intervention outcome recording | Better/Same/Worse per step | Partly | MUST | Planned | N/A | F1 | S | 4 | The learning signal |
| ~10-minute follow-up | Measurement, not a deadline | Yes | MUST | Planned | **No — interval is a product decision (C10)** | I1 | M | 5 | Closes the loop |
| Follow-up no-response handling | In-app fallback; expires cleanly | Yes | MUST | Planned | N/A | I1 | S | 2 | The notification is best-effort |
| Initial distress rating | Optional 1–5 before/after | Yes | SHOULD | Planned | N/A | F3 | S | 2 | Optional; never blocks |
| 2-tap check-in | Before/after without a diary | Yes | MUST | Planned | N/A | F5 | S | 3 | Folded into reassessment |
| Passive sensor EMA | — | — | DROP | Dropped | Loses to Doze/One UI | — | L | 2 | Check-in yields the same data at 5% of the cost |

### Social / safety

| Feature | Purpose | User-facing? | Priority | Status | Evidence-backed? | Main dependency | Complexity | Demo | Reason |
|---|---|---|---|---|---|---|---|---|---|
| "I need help now" screen | Explicit user-chosen options | Yes | MUST | Planned | Yes — `evidence.md` §10 | F1 | M | 4 | Safety requirement |
| Emergency dialer action | `ACTION_DIAL`, user presses call | Yes | MUST | Planned | N/A | F6 | S | 3 | No permission, no auto-dial |
| Trusted contact SMS composer | Signal someone without explaining | Yes | SHOULD | Cut ladder #6 | N/A | H1 | S–M | 3 | No `SEND_SMS`, no auto-send |
| Static offline support text | Works with zero configuration | Yes | MUST | Planned | N/A | F6 | S | 2 | The zero-setup floor |
| Non-emergency disclaimer | Persistent, adjacent to contact actions | Yes | MUST | Planned | Yes — `evidence.md` §13 | A1 | S | 3 | Required boundary |
| Automatic emergency escalation | — | — | DROP | Dropped | **Forbidden** | — | — | — | Anchor cannot detect danger and must not claim to |

### Audio / platform / privacy

| Feature | Purpose | User-facing? | Priority | Status | Evidence-backed? | Main dependency | Complexity | Demo | Reason |
|---|---|---|---|---|---|---|---|---|---|
| `AudioEngine` boundary | One interface, defined failure | No | MUST | Planned | N/A | A1 | S | 1 | Keeps failures explicit |
| Brown noise (E009) | Optional sensory comfort | Yes | IF TIME | Cut ladder #4 | **No — `EVIDENCE_GAP` (C4)** | F3 | S | 2 | Never a primary intervention |
| Audio fallback behaviour | Failure → silent session | No | MUST | Planned | N/A | B1 | S | 1 | Haptics must be unaffected |
| Offline-first operation | Airplane mode is the test | Yes | MUST | Planned | N/A — `CLAUDE.md` | All | — | 5 | Core constraint |
| On-device storage only | DataStore + JSON, no database | No | MUST | Planned | N/A | A3 | M | 3 | Privacy story |
| No backend / auth / network client | Nothing to breach | No | MUST | Planned | N/A | — | — | 3 | Deliberate absence |
| No runtime AI in any path | Determinism and safety | No | MUST | Planned | Yes — `evidence.md` §14 | — | — | 4 | The differentiating boundary |
| No trauma-narrative storage | No field can hold one | No | MUST | Planned | Yes — `evidence.md` §2 | A3 | S | 4 | Enforced in the model |
| Process-death resume | Sessions survive a kill | No | MUST | Planned | N/A | F1 | M | 2 | Real-world reliability |
| Haptic capability detection | Honest degradation | Yes | MUST | Planned | N/A | B1 | S | 2 | Emulator + budget devices |
| Emulator debug haptics | Unblocks development | No | MUST | Planned | N/A | B1 | S | 1 | The emulator cannot vibrate |
| Catalog integrity test | Build fails on a dangling id | No | MUST | Planned | N/A | D2 | S | 2 | Cheapest guard against drift |
| Evidence / copy audit | Every claim traced or deleted | No | MUST | Planned | Yes | All UI | S–M | 3 | Reputational insurance |
| Minimum-permission posture | Only notifications + optional mic | Yes | MUST | Planned | N/A | — | — | 4 | Strong pitch line |
| Data deletion in Settings | User can erase everything | Yes | SHOULD | Planned | N/A | H1 | S | 3 | Expected of a privacy claim |

---

## 24. Contradictions, Risks and Open Questions

### 24.1 Contradictions, resolved explicitly

| # | Contradiction | Resolution |
|---|---|---|
| C1 | **Revisions 1–2 built the product around lock-screen entry; the new direction says unlock-then-tap** | ANCHOR NOW is MUST; tile, notification, side key and `showWhenLocked` demoted to SHOULD but **not removed**. The demo leads with the tap, mentions the shade as an accelerant |
| C2 | **Revision 2 made state selection a required step before routing; the new flow makes it optional** | State selection is an overlay *during* grounding, offered at ~4 s, ignorable. `stateWasSkipped` is recorded so the data stays honest |
| C3 | **The 12-hour recovery window has no support in `evidence.md`** | Kept as a labelled product hypothesis: config-driven, user-adjustable, described in UI as a setting. "PTSD Hangover" is retired from user-facing copy. Demoted to SHOULD because the follow-up now owns "after" |
| C4 | **Brown noise has no evidence in `evidence.md`** | `EVIDENCE_GAP`, adjunct only, never primary, IF TIME, labelled in-app |
| C5 | **Haptic textures have no clinical evidence**, yet they are the signature feature | Tagged `DELIVERY_METHOD_UNEVALUATED`. E007 cites Lewis 2017 for grounding *as a class* and states plainly that the haptic delivery is unevaluated. UI never implies the vibration is evidenced |
| C6 | **"Anchor learns what works for you" edges toward an efficacy claim** | All copy is reported-outcome phrasing, gated at `MIN_OBS = 3`, always carrying "your own reports · not a clinical measure" (§9.5) |
| C7 | **`FROZEN` and `NOT_SURE` have no pathway evidence** | Routed to the lowest-demand default and tagged `PRODUCT_DECISION`. Stated openly rather than dressed up |
| C8 | **`CLAUDE.md` says minimal dependencies; a follow-up wants scheduling** | `AlarmManager` + a receiver, no WorkManager. Inexact by design |
| C9 | **`CLAUDE.md` allows AI in the product; `evidence.md` §14 forbids AI inventing clinical content** | Reconciled as: AI is a build-time tool only. Runtime is deterministic. RAG deferred (§10) |
| C10 | **"10-minute follow-up" implies a recovery deadline** | It is a measurement checkpoint. Copy never promises improvement by any time. The interval is a product decision, not evidence |
| C11 | **Insights could read as clinical inference** | Descriptive statistics only; no interpretation, no clinical vocabulary; IF TIME |
| C12 | **The routine editor could become a clinical-instruction authoring tool** | Constrained editor: catalog-only steps, bounded durations, safety-filtered, **no free text except a routine name**. V1–V7 enforce it |
| C13 | **`CLAUDE.md` says use Plan Mode for substantial features, but this document is the plan** | This plan is the approved artifact. A1–M3 may be implemented directly. Re-plan only if an assumption here proves wrong — then stop and say so rather than redesigning silently |
| C14 | **Revision 2's effort estimates were already tight; revision 3 adds routines, follow-up and personalization** | MUST rose to ~64 h. §18.4 cut ladder exists precisely for this, and the three-dev case starts cutting at H22 |
| C15 | **A user-composed routine is not itself an evidence-backed protocol**, even when every step is | Routines are explicitly framed as **preference-ordered delivery of allowed techniques**, never as a treatment plan. The editor says so; the evidence screen attaches to individual steps, never to the routine |
| C16 | **Emergency functionality vs "we cannot detect danger"** | Everything is explicit user action: `ACTION_DIAL` never `ACTION_CALL`, typed numbers, no inference, no compliance claim, no auto-escalation |

### 24.2 Risk register

| # | Risk | Likelihood | Mitigation |
|---|---|---|---|
| R1 | Emulator cannot vibrate — haptics unverified until late | Certain | Dev A owns the Samsung from hour 0; `DebugHapticEngine`; on-device check every few hours |
| R2 | **MUST (~64 h) does not fit three devs** | High | §18.4 cut ladder agreed before hour 0; A3 frozen early so streams truly parallelise |
| R3 | Routine editor overruns | Medium | Timeboxed to 5 h with a reduced-scope fallback (toggle + reorder only) |
| R4 | Follow-up alarm delayed or dropped by One UI | Medium-High | In-app pending prompt is the mechanism; the notification is the optimisation |
| R5 | Catalog copy drifts into unsupported claims | Medium | D2 copy review, M1 audit, banned-phrase grep, integrity test |
| R6 | Safety filter has a hole | Medium | SF2 and SF3 explicitly unit-tested; E1 reviewed before merge |
| R7 | Process-death handling is fiddly and gets skipped | Medium | F2 is its own task with an `adb shell am kill` acceptance test |
| R8 | Gradle/Compose ramp-up burns hours | Medium | A1 hard-timeboxed to 90 min by the most experienced dev |
| R9 | Demo phone in DND/silent → no haptics | Medium | `USAGE_ACCESSIBILITY` on every effect; pre-flight checklist |
| R10 | Personalization has nothing to show on a fresh install | High | Seeded, clearly labelled sample history in the debug build |
| R11 | Over-architecting | Medium | Single module, `ServiceLocator`, no DI/Room/WorkManager |
| R12 | Clinical overclaiming in the pitch itself | Low but severe | §11.1 table read aloud in the dry run; the VA/DoD relaxation caveat stated explicitly |

### 24.3 Open questions requiring human review

1. **`EVIDENCE_GAP` items** — brown noise (E009) has no support in
   `evidence.md`. Ship labelled, or drop entirely? *Recommendation: ship
   labelled, IF TIME, never primary.*
2. **`FROZEN` / `NOT_SURE` routing** — a product decision with no evidence
   behind it. A clinician should sanity-check the default before the pitch.
3. **Haptic delivery of guideline techniques** — E003/E004 pair a
   guideline-recommended technique with an unevaluated delivery. The split
   tagging is honest, but confirm the wording reads that way to a clinician.
4. **The ~10-minute interval** — arbitrary. Documented as a product decision.
5. **The 12-hour recovery window** — unchanged hypothesis from revision 1.
6. **Routine composition** (C15) — needs a human sign-off that the framing
   never reads as a treatment plan.
7. **Emergency number defaults** — Anchor must not assert which number is
   correct in a user's jurisdiction. Confirm the settings copy asks the user to
   verify.
8. **Sample data in the demo** — confirm every seeded episode is visibly
   labelled so no judge mistakes it for real usage.

---

## 25. Definition of Done

The hackathon build is done when all of the following hold on the **physical
Samsung, in airplane mode**:

1. Cold tap on ANCHOR NOW produces a haptic in under 1.5 s.
2. A session runs the user's default routine without any question being asked.
3. The state overlay can be answered or ignored, and ignoring it changes
   nothing about the session's reliability.
4. Selecting "Far away / not real" produces a **different, non-interoceptive**
   intervention than selecting "Racing / can't calm down" — demonstrably, on
   stage.
5. Better / Same / Worse each behave per §15.1, and `WORSE` stops all output
   and opens the safety screen.
6. Every intervention reaches its source, status and limitations in two taps.
7. The episode is written locally and survives a force-kill.
8. The follow-up arrives (or its in-app fallback does) and records a result.
9. After three observations, the insight card renders approved copy with the
   "your own reports" qualifier.
10. The routine editor can reorder and toggle steps, and refuses an
    unsafe-for-state step with a visible reason.
11. `SafetyFilter`, `InterventionRouter`, `PersonalizationScorer`,
    `SessionReducer`, `RoutineValidator` and the catalog integrity test are
    green.
12. The evidence/copy audit (M1) has been run and no banned phrasing remains.
13. Two full dry runs have completed without improvisation.
