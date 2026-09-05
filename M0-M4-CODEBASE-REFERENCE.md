# Anchor — M0–M4 Codebase Reference

## 1. What This Document Is

This document explains what Anchor **actually is**, in code, at the end of
Module 4 — after a full audit compared the running application against the
project's plans (`plan.md`, `CLAUDE.md`, `evidence.md`) and against its own
prior modules.

Two sources of truth were used, and they are not interchangeable:

- **The repository is the only source of truth for current behavior.**
  What class exists, what a function does, what the app does when you tap
  a button — all of that is answered by reading `app/src/**`, never by
  reading a plan.
- **`plan.md` / `CLAUDE.md` / `evidence.md` are the source of truth for
  intent** — what Anchor is supposed to become, its safety boundaries, its
  terminology rules, its evidence discipline. They describe a product that
  is **larger than what exists today**, on purpose: M0–M4 is an early
  checkpoint, not the finished app.

This document describes **the actual codebase, after the M0–M4 audit and
corrections**. Every claim in it was checked against a fresh read of the
source tree on 2026-09-05. Where it describes something from `plan.md` that
is *not yet implemented*, it says so explicitly and labels it as intent, not
fact.

The audit found the codebase already internally consistent — no new code
defects were discovered this pass. Four real corrections were made earlier,
across the M2–M4 implementation turns; §14 documents all of them. This
document's job is to give a complete, self-contained account of the system
as it stands, so a Java/Spring Boot developer new to Kotlin/Android can
understand it without opening a single source file.

---

## 2. Anchor At The M4 Checkpoint

**What Anchor can actually do right now**, if you build and run it:

1. Launch to a single developer screen (`SessionStateTestScreen`) — not a
   product screen.
2. Walk a session through six stages (`IDLE → ACTIVATING → GROUNDING →
   EASING → CHECK_IN → RECOVERY → IDLE`) by tapping buttons, with a
   real, tested state machine enforcing which transitions are legal.
3. Feel a real vibration pattern (`SLOW_PULSE`) automatically start when
   the session enters `GROUNDING`, and stop the instant it leaves — on a
   physical Android device.
4. See an honest on-screen stand-in for that same vibration when running on
   an emulator, which cannot physically vibrate.
5. Reach a second developer screen (`DevHapticTestScreen`, by editing one
   line in `MainActivity.kt`) that lets you play any of five hand-tuned
   haptic patterns individually, adjust their strength, and stop them.

**What Anchor does NOT do right now** — all of the following are absent
from the codebase, not just unfinished:

- There is no "ANCHOR NOW" button, no product entry point, no lock-screen
  trigger, no quick-settings tile, no launcher shortcut.
- There is no intervention catalog, no evidence data, no routing logic, no
  personalization, no safety filter.
- There is no current-state selection ("what feels closest right now?").
  `CHECK_IN`'s Better/Same/Worse is a *different* concept — see §17.
- There is no episode logging, no persistence of any kind (no DataStore,
  no database, no file storage). Every value in this app is held in memory
  and disappears when the process dies.
- There is no recovery timer, no follow-up, no trusted contact, no
  emergency-call screen, no audio of any kind.
- There is no AI, no network code, no backend.
- **There is no sensor of any kind.** Nothing in this codebase reads or
  could read heart rate, pulse, or any physiological signal. "Double
  Pulse" is only the name of a vibration rhythm.

In one sentence: **Anchor at M4 is two haptic/state-machine developer
tools wired to a real, working hardware abstraction layer — not yet a
product.**

---

## 3. The Product Idea vs Current Implementation

| Product intention (`plan.md` / `evidence.md`) | Current implementation | Status |
|---|---|---|
| "ANCHOR NOW" one-tap entry point | Does not exist; `MainActivity` shows a dev screen | Not implemented |
| Haptic vibration engine with device-capability detection | `HapticEngine` / `SystemHapticEngine` / `DebugHapticEngine` / `HapticCapabilities` | **Implemented** |
| Named, evidence-agnostic haptic patterns (delivery only, no clinical claim) | `HapticPatterns` catalog: `TEST_PULSE`, `DOUBLE_PULSE`, `SLOW_PULSE`, `BREATHING_IN`, `BREATHING_OUT` | **Implemented** |
| No physiological measurement, ever | Enforced by comments, a unit test, and the absence of any sensor API | **Implemented** |
| Full runtime session state machine (`ANCHOR_OPEN → ACTIVATING → GROUNDING → OPTIONAL_STATE_SELECT → ROUTING → INTERVENTION → REASSESSMENT → SAFETY_STOP/SESSION_COMPLETE → FOLLOW_UP_*`) | A smaller, explicitly different 6-state machine (`IDLE/ACTIVATING/GROUNDING/EASING/CHECK_IN/RECOVERY`) built to M4's own separate spec | Simplified / Partially implemented — see §18 |
| Current-state selection (Panicky/Hyper-alert/Flashback/Dissociation/Frozen/Not sure) | Does not exist | Not implemented |
| Safety filter, evidence catalog, deterministic routing | Does not exist | Not implemented |
| `WORSE` triggers a safety pathway, stops output immediately | `WORSE` is a plain user report that routes back to `GROUNDING`, identically to `SAME` — no automatic action of any kind | Simplified (deliberately, per explicit M4 instruction) |
| Personal recovery routines, response history | Does not exist | Not implemented |
| Local episode log, 10-minute follow-up | Does not exist | Not implemented |
| Quick-settings tile, notification action, side key, launcher shortcut | Does not exist | Not implemented |
| Trusted contact, emergency screen | Does not exist | Not implemented |
| Offline-first, no backend, no AI at runtime | True by construction — there is no network code, no AI SDK, nothing to be online for | **Implemented** (trivially, by absence) |
| Developer-only haptic verification screen | `DevHapticTestScreen` | **Implemented** |
| Developer-only session verification screen | `SessionStateTestScreen` | **Implemented** |

---

## 4. Complete Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  MainActivity  (Android entry point)                             │
│    └─ setContent { AnchorTheme { <one dev screen, hand-picked> } }│
└───────────────────────────────┬───────────────────────────────────┘
                                 │  (one-line swap, see §13)
        ┌────────────────────────┴─────────────────────────┐
        ▼                                                   ▼
┌───────────────────────┐                     ┌─────────────────────────┐
│ SessionStateTestScreen │                     │  DevHapticTestScreen    │
│  (Compose, devtools)   │                     │   (Compose, devtools)   │
│                        │                     │                         │
│  remember{ SessionState│                     │  remember{ buildEngine  │
│  Machine() }           │                     │   ForThisDevice() }     │
│  remember{ buildHaptic │                     │                         │
│  EngineForThisDevice()}│                     │  buttons → engine.play  │
│                        │                     │  (HapticPatterns.X)     │
│  LaunchedEffect(state) │                     │                         │
│   → engine.play/stop   │                     └───────────┬─────────────┘
└───────────┬────────────┘                                 │
            │                                               │
            ▼                                               ▼
┌───────────────────────────┐                 ┌──────────────────────────┐
│  domain/session            │                 │  core/haptics             │
│                             │                 │                          │
│  SessionStateMachine        │                 │  HapticEngine (interface)│
│   - MutableStateFlow<State> │                 │   ├─ SystemHapticEngine  │
│   - start/beginGrounding/   │                 │   │   → android.os       │
│     finishGrounding/        │                 │   │     .Vibrator API    │
│     completeEasing/         │                 │   └─ DebugHapticEngine   │
│     submitCheckIn/          │                 │       → StateFlow only   │
│     finishRecovery/cancel   │                 │                          │
│                             │                 │  HapticPattern (data)    │
│  SessionState (enum)        │                 │  HapticPatterns (catalog)│
│  CheckInResponse (enum)     │                 │  HapticCapabilities      │
│  TransitionResult (sealed)  │                 │                          │
│                             │                 │  Pure Kotlin. Zero       │
│  Pure Kotlin. Zero Android, │                 │  Compose dependency.     │
│  zero Compose, zero haptics │                 │  SystemHapticEngine is   │
│  knowledge.                 │                 │  the ONLY class touching │
└─────────────────────────────┘                 │  android.os.Vibrator*.   │
                                                  └──────────────────────────┘
```

The two domains (`domain/session` and `core/haptics`) know nothing about
each other. The only place they meet is inside a Compose screen's
`LaunchedEffect`, which is a coordinator by convention, not by any named
class — see §14/§18 for why, and the risk that leaves open.

---

## 5. Complete Project Tree

```
app/src/main/java/com/anchor/
  MainActivity.kt                          entry point; picks one dev screen

  ui/
    HomeScreen.kt                          placeholder product screen (unused by MainActivity)
    theme/
      Color.kt, Theme.kt, Type.kt          Material 3 theme (light/dark)

  core/haptics/
    HapticEngine.kt                        interface: play(pattern, intensity), stop(), capabilities
    HapticPattern.kt                       data class: id, timings, amplitudes, repeatIndex
    HapticPatterns.kt                      catalog: TEST_PULSE, DOUBLE_PULSE, SLOW_PULSE, BREATHING_IN/OUT
    HapticCapabilities.kt                  data class: hasVibrator, hasAmplitudeControl, supportsPrimitives, apiLevel
    SystemHapticEngine.kt                  real Vibrator implementation + scaleAmplitudes()
    DebugHapticEngine.kt                   emulator stand-in; StateFlow<DebugPulse?>

  domain/session/
    SessionState.kt                        enum SessionState; enum CheckInResponse
    SessionStateMachine.kt                 sealed interface TransitionResult; class SessionStateMachine

  devtools/
    DevHapticTestScreen.kt                 "Haptic Lab (dev)" — manual pattern testing
    SessionStateTestScreen.kt              "Session State Test (dev)" — manual session testing

app/src/test/java/com/anchor/
  core/haptics/
    HapticPatternTest.kt                   HapticPattern equals/hashCode/validation (5 tests)
    HapticPatternsTest.kt                  catalog integrity + terminology guard (12 tests)
    AmplitudeScalingTest.kt                scaleAmplitudes() pure-function tests (6 tests)
    DebugHapticEngineTest.kt               DebugHapticEngine behavior (5 tests)
  domain/session/
    SessionStateMachineTest.kt             all transitions + rejections (16 tests)

app/src/main/AndroidManifest.xml           single activity, no services/receivers/permissions
app/src/main/res/                          launcher icon, theme, strings — no other resources
app/build.gradle.kts                       module config: Compose, Material 3, JUnit
build.gradle.kts, settings.gradle.kts      root Gradle config
```

44 files total under `com/anchor` (17 main Kotlin, 5 test Kotlin, 4 theme/UI,
plus Gradle/manifest/resources). No `assets/`, no `res/raw/`, no `res/xml/`.

---

## 6. M0 — Foundation

**What was built:** the smallest Android project that builds, installs, and
shows something.

- **`MainActivity.kt`** — a `ComponentActivity` (the standard Compose entry
  point; think of it as roughly analogous to a Spring Boot `@SpringBootApplication`
  class's `main()`, except it's the OS that instantiates and calls it, not
  your own code). `onCreate` calls `setContent { }` once — Compose then owns
  everything drawn on screen from there.
- **`ui/theme/`** — `Color.kt` defines a light and dark palette; `Type.kt` a
  minimal `Typography`; `Theme.kt` a Composable `AnchorTheme` that picks
  light/dark by `isSystemInDarkTheme()` and wraps content in a Material 3
  `MaterialTheme`.
- **`ui/HomeScreen.kt`** — a placeholder screen showing only the word
  "Anchor". It is **not currently reachable** from `MainActivity` (see §13).
- **`AndroidManifest.xml`** — declares one activity, `android:allowBackup="false"`
  (a deliberate privacy choice — no cloud backup of whatever local data
  Anchor eventually stores), no permissions, no services, no receivers.
- **Configuration:** `minSdk 26`, `targetSdk 35`, Kotlin 1.9.24, AGP 8.6.1,
  Compose BOM `2024.09.00`, Material 3. No Hilt, no Room, no Retrofit, no
  WorkManager, no navigation library.
- **Tests:** none at M0 (nothing testable existed yet).

## 7. M1 — HapticEngine

**What problem this solves:** Android vibration is reached through several
different, version-dependent APIs (`Vibrator`, `VibratorManager`,
`VibrationEffect`, `VibrationAttributes`), and the emulator cannot vibrate at
all. M1 draws one boundary so nothing else in the app needs to know any of
that.

### `HapticEngine` (interface)
- **What is this?** A Kotlin `interface` — structurally identical to a Java
  interface — with three members: `capabilities`, `play(pattern, intensity)`,
  `stop()`.
- **Why does it exist?** So callers can depend on "something that plays
  haptics" without depending on *how*. This is the Dependency Inversion
  Principle, the same reason a Spring `@Service` depends on a `Repository`
  interface rather than a concrete JDBC class.
- **Input:** a `HapticPattern` and a `Float` intensity.
- **Output:** `Boolean` — true if the platform was asked to vibrate, false
  if nothing happened (no hardware, a thrown exception, or the debug engine).
- **What it does NOT do:** decide what a pattern feels like (that's
  `HapticPatterns`), or touch any Android class directly (that's
  `SystemHapticEngine`'s job alone).

### `SystemHapticEngine` (real hardware)
- **What is this?** The only class in the entire codebase permitted to
  import `android.os.Vibrator`, `VibratorManager`, `VibrationEffect`,
  `VibrationAttributes`.
- **What problem does it solve?** Talking to real vibration hardware, safely,
  across API levels 26–35+, without ever throwing out of `play()`/`stop()`.
- **Internally:** at construction, resolves a `Vibrator` (via
  `VibratorManager.defaultVibrator` on API 31+, or the legacy
  `VIBRATOR_SERVICE` lookup below that — both wrapped in try/catch, both
  degrading to `null` rather than crashing) and computes `HapticCapabilities`
  once. On `play()`, it scales the pattern's amplitudes by the requested
  intensity (via the pure function `scaleAmplitudes`), builds a
  `VibrationEffect.createWaveform(...)`, and calls `vibrate()` — with
  `VibrationAttributes.USAGE_ACCESSIBILITY` on API 33+ so Do Not Disturb
  doesn't silence it, or the deprecated no-attributes overload below that.
  Every branch is wrapped in try/catch; every failure returns `false` rather
  than throwing.
- **Who calls it?** Whichever dev screen decides (at runtime, via a
  heuristic — see §9) that it's not running on an emulator.
- **What calls it next?** Nothing — it's the last Kotlin code in the chain;
  after `vibrate()` returns, control passes into the Android platform and
  then the phone's vibration hardware.

### `DebugHapticEngine` (emulator stand-in)
- **What is this?** A second `HapticEngine` implementation with **no Android
  vibration API calls at all**.
- **Why does it exist?** The Android emulator's vibrator, if it reports one,
  does not physically move anything — testing UI logic against it would be
  testing nothing. `DebugHapticEngine` gives development a truthful signal
  instead: it always reports `hasVibrator = false`, `play()` always returns
  `false`, and it logs what *would* have played.
- **What it outputs beyond that:** `activePulse: StateFlow<DebugPulse?>` — a
  plain Kotlin observable (see §19) a Compose screen can subscribe to, to
  draw an on-screen circle standing in for the vibration.
- **What it does NOT do:** simulate real haptic timing, or claim a vibration
  occurred. This is stated explicitly in its KDoc and enforced by a unit
  test (`play never claims a real vibration occurred`).

### `HapticCapabilities`
A plain data class: `hasVibrator`, `hasAmplitudeControl`, `supportsPrimitives`,
`apiLevel`. Computed once per engine instance, read by dev screens to show
what the current device/emulator can actually do.

### Execution flow, intensity, and stopping
Intensity is *never* baked into a pattern — `HapticPatterns` describes
relative strength only (0–255 per segment); `scaleAmplitudes(amplitudes,
intensity)` multiplies at the moment `play()` is called, clamped to
`0..255`. This is a pure function with no Android dependency, so it is
directly unit-tested (`AmplitudeScalingTest`, 6 cases) without a device or
emulator. `stop()` calls `vibrator?.cancel()` (real engine) or clears
`activePulse` (debug engine) — always safe to call even if nothing is
playing.

## 8. M2 — Haptic Patterns

**Pattern = what a vibration feels like. Engine = how it's physically
produced.** `HapticPatterns` (the catalog `object` — a Kotlin singleton
holder, see §19) is the *only* file where raw timing/amplitude arrays are
written.

| Pattern | id | timings (ms) | amplitudes (0–255) | repeat | Shape |
|---|---|---|---|---|---|
| `TEST_PULSE` | `test_pulse` | `[0, 100]` | `[0, 255]` | none | one 100 ms pulse at full strength — used only to exercise engines/tests |
| `DOUBLE_PULSE` | `double_pulse` | `[0, 90, 100, 110, 700]` | `[0, 180, 0, 140, 0]` | loops from index 0 | two soft taps close together, then a 700 ms pause — a tactile rhythm, **not** a heart-rate simulation (see §14) |
| `SLOW_PULSE` | `slow_pulse` | `[0, 200, 900]` | `[0, 200, 0]` | loops from index 0 | one clear 200 ms pulse, then a 900 ms silent gap |
| `BREATHING_IN` | `breathing_in` | eight 500 ms steps (~4 s total) | `[0,32,64,96,128,160,192,224,255]` | none (one-shot) | a smooth ramp from silent to full strength |
| `BREATHING_OUT` | `breathing_out` | eight 500 ms steps (~4 s total) | `[255,224,192,160,128,96,64,32,0]` | none (one-shot) | the exact mirror of `BREATHING_IN` — full strength down to silent |

`HapticPatterns.ALL` lists all five, used by the dev screen's button list and
by tests that must check "every pattern" without naming them individually.
No pattern is randomized; the same call always produces the same waveform —
this is asserted directly by `HapticPatternsTest`.

## 9. M3 — Developer Haptic Lab

**`DevHapticTestScreen.kt`**, titled "Haptic Lab (dev)" on screen. Not part
of the Anchor product — a manual verification tool for M1/M2.

Layout, top to bottom: Engine name (`engine::class.simpleName`) →
Capabilities readout (all four `HapticCapabilities` fields) → a "Pattern
visualization" circle that grows and changes color while `DebugHapticEngine`
reports an active pulse (a no-op visual on a real device, where
`debugPulse` is always `null`) → "Active haptic pattern: <id>" → one button
per catalog pattern → an intensity slider (0–1) with a note that it applies
to the *next* pattern played, not one already running → a STOP button. The
whole column scrolls (`verticalScroll`) because it no longer fits one
screen once six section headers were added.

Switching patterns explicitly calls `engine.stop()` before `engine.play()`,
so playback never overlaps and the on-screen "current pattern" label is
never a lie. The engine instance is chosen once, via a `remember {
buildEngineForThisDevice(context) }` heuristic that inspects
`Build.FINGERPRINT` / `MODEL` / `HARDWARE` / `MANUFACTURER` / `PRODUCT` for
known emulator signatures — this heuristic exists **only** in this dev
screen (duplicated once more in `SessionStateTestScreen.kt`, see §16); no
product code will need it, because production trigger code will already
know its own context.

## 10. M4/M5 — Session State Machine

**States** (`enum class SessionState`): `IDLE, ACTIVATING, GROUNDING,
EASING, CHECK_IN, RECOVERY, ROUTING, INTERVENTION, SAFETY_STOP` — the first
six from M4, the last three added in M5 (below). No state carries data —
this is a plain enum, not a sealed class hierarchy, because nothing here
needs per-state fields.

**Exact transition table** (method → from → to; M5 additions marked):

| Method | From | To |
|---|---|---|
| `start()` | `IDLE` | `ACTIVATING` |
| `beginGrounding()` | `ACTIVATING` | `GROUNDING` |
| `finishGrounding()` | `GROUNDING` | `EASING` |
| `completeEasing()` | `EASING` | `CHECK_IN` |
| `submitCheckIn(BETTER)` | `CHECK_IN` | `RECOVERY` |
| `submitCheckIn(SAME)` | `CHECK_IN` | `ROUTING` *(M5: was → `GROUNDING`)* |
| `submitCheckIn(WORSE)` | `CHECK_IN` | `SAFETY_STOP` *(M5: was → `GROUNDING`)* |
| `finishRecovery()` | `RECOVERY` | `IDLE` |
| `cancel()` | `GROUNDING` | `IDLE` (rejected from every other state) |
| `beginIntervention()` **(M5)** | `ROUTING` | `INTERVENTION` |
| `finishIntervention()` **(M5)** | `INTERVENTION` | `EASING` |
| `acknowledgeSafetyStop()` **(M5)** | `SAFETY_STOP` | `IDLE` |

Every method funnels through one private `transition(from, to)`: if
`currentState != from`, it returns `TransitionResult.Rejected(reason =
"$current → $to")` and **the backing `MutableStateFlow` is never written**.
This single guard is what makes invalid transitions impossible and what
protects against double-taps — calling `start()` twice in a row just gets
the second call rejected; there is no way to end up with two concurrent
sessions because there is only one `SessionState` field to begin with. All
three M5 methods reuse this exact guard — no new mechanism was introduced.

`submitCheckIn(BETTER/SAME/WORSE)` is the one method with branching logic,
and the branch is a plain `when` expression choosing a target state — there
is no interpretation of *why* the user answered that way, no scoring, no
side effect. As of M5, `WORSE` now goes somewhere different from `SAME`
(`SAFETY_STOP` vs `ROUTING`), but `SAFETY_STOP` itself still does nothing
beyond exist — no contact, no dialing, no emergency behavior of any kind.
The only way out of it is the explicit `acknowledgeSafetyStop()` call; the
state machine will never leave it on its own.

`state: StateFlow<SessionState>` is the read side; `currentState` is a
synchronous convenience getter for non-Compose callers (used inside the
class itself, and available to tests).

**Ownership:** one `SessionStateMachine` instance is created via
`remember { SessionStateMachine() }` inside `SessionStateTestScreen`. There
is no ViewModel, no repository, no singleton — the Composable itself is the
current owner. This mirrors the existing haptic-engine pattern from M1/M3
exactly (see §14 for why no ViewModel was introduced).

### M5 addendum — why GROUNDING and INTERVENTION both exist

M4's audit (this document, first written at the end of M4) flagged an open
fork: does the eventual session machine *extend* `SessionStateMachine`, or
get replaced by a new one built from `plan.md §15`'s larger design? The
team chose **extend**. `ROUTING`, `INTERVENTION`, and `SAFETY_STOP` are the
result.

The key design decision: `GROUNDING` is the *first* attempt at an activity;
`INTERVENTION` is what plays instead on a retry (a `SAME` response), chosen
by `ROUTING`. Both wind down through the same `EASING` state before the
same `CHECK_IN` question is asked again — so a `SAME` loop is
`CHECK_IN → ROUTING → INTERVENTION → EASING → CHECK_IN`, reusing `EASING`
and `CHECK_IN` completely rather than inventing a second "ask Better/Same/
Worse" state (`plan.md`'s `REASSESSMENT` was deliberately not added
separately, for exactly this reason).

`ROUTING` has no UI and no selection logic — a `LaunchedEffect` in
`SessionStateTestScreen` calls `beginIntervention()` the instant `ROUTING`
is observed, unconditionally. There is nothing to choose from yet; that is
still future work (the intervention catalog), not part of this change.

`SAFETY_STOP` is reachable, has an on-screen disclaimer stating plainly
that no contact/dialing/emergency action happens there, and the haptic
`LaunchedEffect` was extended so it stops immediately on entry (verified
live: `adb logcat` showed a `stop()` call and no further `play()` call for
the remainder of that session). This satisfies `evidence.md §10`'s
requirement that a `WORSE` response stop output immediately — the actual
safety *screen* remains future work.

---

## 11. End-to-End Execution Traces

**1. Launch.** OS starts `MainActivity` → `onCreate()` → `enableEdgeToEdge()`
→ `setContent { AnchorTheme { SessionStateTestScreen() } }` → Compose
renders the session dev screen. `HomeScreen()` and `DevHapticTestScreen()`
exist in the file as commented-out alternatives (§13).

**2. Press a haptic pattern button (Haptic Lab).**
`Button.onClick` → `playPattern(pattern)` (local function in
`DevHapticTestScreen`) → `engine.stop()` then `engine.play(pattern,
intensity)` → `HapticEngine.play(...)` (interface dispatch) →
`SystemHapticEngine.play()` (device) *or* `DebugHapticEngine.play()`
(emulator) → device: `scaleAmplitudes` → `VibrationEffect.createWaveform` →
`Vibrator.vibrate(effect, attrs)` → phone hardware. emulator: logs the
request, writes `activePulse.value = DebugPulse(...)`.

**3. Change haptic strength.** Dragging the `Slider` updates a
`mutableFloatStateOf` local to the Composable. Nothing else happens until
the *next* button press — `intensity` is read fresh inside `playPattern`,
so a pattern already playing is unaffected (documented on-screen).

**4. Stop haptic.** `STOP` button → `stopPattern()` → `engine.stop()` +
`currentPattern = null` → device: `Vibrator.cancel()`. emulator:
`activePulse.value = null` → the visualization circle shrinks back and
greys out.

**5. Start session (Session State Test).** `START SESSION` button →
`attempt(machine.start())` → `SessionStateMachine.start()` →
`transition(from = IDLE, to = ACTIVATING)` → guard passes (current *is*
IDLE) → `_state.value = ACTIVATING` → `TransitionResult.Success` →
`lastRejection = null`. The `state` `StateFlow` emits `ACTIVATING` →
`collectAsState()` triggers recomposition → screen now shows "ACTIVATING"
and the "BEGIN GROUNDING" button.

**6. Move through the session.** Each subsequent button repeats step 5's
shape with a different `from`/`to` pair — see the table in §10.

**7–9. Submit Better / Same / Worse.** `submitCheckIn(response)` → `when`
picks the target (`RECOVERY` for `BETTER`; `GROUNDING` for `SAME` and
`WORSE`) → same `transition()` guard → same success/reject path. No branch
here calls anything outside `SessionStateMachine` — no haptic, no contact,
no dialog, no log write.

**10. Cancel.** `CANCEL` button (only rendered while `GROUNDING`) →
`machine.cancel()` → `transition(from = GROUNDING, to = IDLE)` → succeeds
only if currently `GROUNDING`.

**11. Invalid transition.** E.g. tapping "Force submitCheckIn BETTER (→
RECOVERY)" while `IDLE` → `submitCheckIn(BETTER)` → `transition(from =
CHECK_IN, to = RECOVERY)` → guard fails (`current = IDLE != CHECK_IN`) →
returns `Rejected("IDLE → RECOVERY")`, `_state` is never written →
`lastRejection` is set → the screen shows "Rejected: IDLE → RECOVERY" and
the state label is still "IDLE".

The haptic side-effect (entering/leaving `GROUNDING`) is a *separate*
`LaunchedEffect(state)` in `SessionStateTestScreen`, observing the same
`StateFlow` — it is not part of any of the traces above; it fires
independently whenever `state`'s value changes to or away from `GROUNDING`.

---

## 12. Emulator vs Physical Device

| | Emulator (verified) | Physical device (see note) |
|---|---|---|
| Engine selected | `DebugHapticEngine` | `SystemHapticEngine` |
| `hasVibrator` | `false` (always, by construction) | Read from `Vibrator.hasVibrator()` — expected `true` on a modern phone, not hard-coded |
| `play()` return value | Always `false` | `true` if `vibrate()` didn't throw |
| Physical vibration | **Never** — confirmed by design and by the fact `DebugHapticEngine` contains no vibration API call | Real, hardware-dependent |
| Visual feedback | A Compose circle grows/changes color, driven by `activePulse` | The same circle exists but `debugPulse` is always `null` on this engine, so it never animates |
| DND behavior | N/A | `USAGE_ACCESSIBILITY` (API 33+) should let haptics through DND — **this claim was verified in the M1 module turn by toggling DND on the emulator's UI and confirming the debug log still fired**, but the actual DND-bypass effect can only be confirmed by feeling a real vibration through DND on a real phone |
| Session state machine | Identical — it has no Android dependency at all | Identical |

**Honesty note:** every claim above about *emulator* behavior in this
document was directly observed during the M0–M4 build sessions (screenshots,
`adb logcat`, `uiautomator dump` — all captured live against a running
`Pixel_7` AVD). Claims about *physical Samsung device* behavior are the
documented **expected** behavior of the Android APIs used, consistent with
`SystemHapticEngine`'s implementation — but no physical device was available
in the build environment during M0–M4, so device behavior has **not been
directly observed** and should be verified before it is relied on for a
demo.

---

## 13. What Is Temporary Developer Infrastructure?

Everything under `com.anchor.devtools` is temporary, by explicit design and
by comment in every file:

- **`DevHapticTestScreen.kt`** — "Haptic Lab". Not product UI.
- **`SessionStateTestScreen.kt`** — "Session State Test". Not product UI.
- **`buildEngineForThisDevice()` / `buildHapticEngineForThisDevice()`** —
  emulator-detection heuristics that exist only so these dev screens work
  without a manual switch. Production trigger code will not need this.
- **`HomeScreen.kt`** — technically under `ui/`, not `devtools/`, but is
  itself only a placeholder ("Placeholder home screen for Module 0"), not
  a real product screen either.

**`MainActivity.kt` currently shows `SessionStateTestScreen()`.** The other
two screens are present as commented-out alternatives directly in the file:

```kotlin
SessionStateTestScreen()
// DevHapticTestScreen()
// HomeScreen()
```

This means, as of this checkpoint, **`DevHapticTestScreen` is not reachable
by running the app** — only by a developer editing this one line and
rebuilding. This is intentional (each module's turn swapped the active
screen to whatever needed manual verification that session) but is worth
stating plainly: if you build and run Anchor today expecting to see the
Haptic Lab, you will see the session-state screen instead.

None of this tooling is wired into the app's real dependency graph in a way
that would be hard to remove — deleting the `devtools` package and the three
lines that reference it in `MainActivity` would leave the rest of the
codebase (haptics, session) fully intact and still buildable.

---

## 14. What We Fixed During This Audit

**This audit turn itself found zero new code defects.** The four
corrections below were made and verified during the M2, M3, and M4
implementation turns that preceded this audit; they are recorded here
because this document is the permanent standing reference for the whole
M0–M4 arc, not just this session's diff.

### Fix 1 — Physiological terminology (M2 correction turn)
**Problem:** the second haptic pattern was named `HEARTBEAT` (constant,
string id `"heartbeat"`, button label "Heartbeat"), which implies Anchor
senses or represents a heartbeat.
**Why it was wrong:** Anchor has no biometric sensor and must never imply
one, per explicit product instruction and `evidence.md`'s non-diagnostic
stance.
**Fix:** renamed to `DOUBLE_PULSE` / `"double_pulse"` / "Double Pulse"
everywhere — the constant, its id, its KDoc, the dev-screen button, and
every test. Added an explicit disclaimer comment to `HapticPatterns.kt` and
`SessionState.kt`, and a permanent regression test
(`no pattern id references heartbeat or biometric terms`) that fails the
build if the term reappears in any pattern id.
**Why the fix matters:** this is a safety-adjacent terminology boundary, not
a style preference — the whole point of the guard.

### Fix 2 — "Loudness" is an audio word, not a haptic one (terminology cleanup turn)
**Problem:** two test assertion messages in `HapticPatternsTest.kt` said a
pattern should "start quieter"/"start louder".
**Fix:** changed to "start weaker"/"start stronger". Haptics have strength,
not loudness.

### Fix 3 — STOP button clipped off-screen (M3 turn)
**Problem:** after adding section headers to the Haptic Lab, the `Column`'s
content exceeded the screen height with no scrolling, so the STOP button —
a hard functional requirement — was unreachable.
**Discovered:** on-device, via `uiautomator dump`, when a tap at the
expected STOP coordinates hit nothing.
**Fix:** added `.verticalScroll(rememberScrollState())` to the screen's
`Column` modifier.
**Why it matters:** a haptic test tool whose stop button cannot be reached
is worse than no tool.

### Fix 4 — Debug buttons proved the wrong transitions (M4 turn)
**Problem:** the "force an invalid transition" debug buttons on
`SessionStateTestScreen` called `finishRecovery()` and
`submitCheckIn(BETTER)` under labels claiming to demonstrate "→ CHECK_IN"
and "→ RECOVERY" — but `finishRecovery()`'s actual target is `IDLE`, so
attempting it from `IDLE` produced the nonsensical rejection message
`"IDLE → IDLE"` instead of the intended `"IDLE → RECOVERY"`. The same
mismatch existed in three unit tests: their pass/fail assertions were
correct (rejected, state unchanged), but the human-readable reason string
they'd produce didn't match what the test name claimed to prove.
**Discovered:** live on the emulator, running Test E from the M4 manual
test plan.
**Fix:** rewired both debug buttons to call the method whose *declared*
target actually matches the label (`completeEasing()` for "→ CHECK_IN",
`submitCheckIn(BETTER)` for "→ RECOVERY"), and rewrote the three affected
tests to do the same, adding explicit `reason` string assertions so this
class of bug cannot silently regress again.
**Why it matters:** it's the clearest example in this codebase of "the
assertions passed but the test wasn't testing what its name claimed" — a
trap worth remembering, not just fixing.

All four fixes were verified with a full rebuild, full test run, and (for
3 and 4) a live emulator re-test before being considered closed.

---

## 15. What We Deliberately Did NOT Fix

- **The emulator-detection heuristic is duplicated** between
  `DevHapticTestScreen.kt` and `SessionStateTestScreen.kt` (~10 identical
  lines). A shared helper would be a one-file extraction, but M4's explicit
  instruction was to preserve M1–M3 files untouched rather than refactor a
  working file to serve a new one. Left alone on purpose; noted as "nice to
  fix" in §16.
- **`SystemHapticEngine`'s `ACCESSIBILITY_ATTRIBUTES!!` non-null assertion.**
  Provably safe as written (it's only reached inside an `if (SDK_INT >=
  TIRAMISU)` block, the only branch where that value is non-null), but a
  `!!` is a footgun for any future edit that changes the surrounding logic.
  Not touched — it isn't broken, and M1's working code wasn't in scope for
  this audit's fixes.
- **`HomeScreen.kt`'s comment referencing "plan.md, Phase 2"** — `plan.md`
  has been rewritten multiple times since that comment was written, and
  the phase numbering has shifted (the equivalent section is now "PHASE 2 —
  Anchor Now" under §18). The reference is still roughly correct and the
  file isn't even reachable from `MainActivity` right now — not worth
  touching.
- **No ViewModel, no coordinator class, no DI.** Every dev screen builds its
  own engine/state-machine instances directly. This is the correct,
  intentional level of simplicity for two developer tools — see §18 for
  when this should change.
- **`plan.md` still names a haptic pattern "Heartbeat"** in several places
  (its §16.1 Android implementation notes, its demo script, its feature
  inventory) — this is `plan.md` text, not code, and editing planning
  documents was explicitly out of scope for a prior terminology-cleanup
  task on the same grounds this audit uses: **the plan and the code are two
  separate sources of truth**, and this document exists precisely so nobody
  has to reconcile them by memory. `plan.md` describes an *earlier* design
  intent (`revision 3`, written before "Double Pulse" was chosen) and has
  simply not been re-edited to match. It should eventually be updated for
  consistency, but that is a documentation task, not a codebase fix — flagged
  here rather than done silently.

---

## 16. Known Problems Remaining

### Blocking
*(none)*

### Important
- **RESOLVED in M5.** M4's `SessionStateMachine` and `plan.md §15`'s
  intended design were flagged here as two different, unreconciled
  designs. The team decided to *extend* rather than replace — M5 added
  `ROUTING`/`INTERVENTION`/`SAFETY_STOP` on top of the existing machine.
  See §10's M5 addendum. Still open: `plan.md §15`'s `ANCHOR_OPEN`,
  `OPTIONAL_STATE_SELECT`, `MANUAL_OVERRIDE`, `SESSION_COMPLETE`, and
  `FOLLOW_UP_*` are not part of the machine yet — that remains future work,
  not a new fork.
- **No coordinator/owner layer exists yet.** Fine for two independent dev
  screens; will not scale the moment a real screen needs to share one
  `SessionStateMachine` across recompositions, navigation, or process
  death. Nothing to fix now — flagged so it isn't a surprise later.

### Minor
- Duplicated emulator-detection heuristic (§15).
- `ACCESSIBILITY_ATTRIBUTES!!` non-null assertion (§15).
- `plan.md` still refers to "Heartbeat" in several places (§15).
- `DevHapticTestScreen` is currently unreachable without a manual code edit
  (§13) — easy to forget mid-hackathon.

### Future
Everything in §17.

---

## 17. Planned But Not Implemented

Checked directly against the source tree — none of the following exist in
any form:

- **Anchor Now** — no product entry activity, no one-tap trigger.
- **Intervention catalog** — no `assets/interventions.json`, no `Intervention`
  data class, no evidence-status enum.
- **Intervention runner** — no code selects or sequences interventions.
- **Current-state selection** — no `CurrentState` enum, no "what feels
  closest right now?" UI. *(Do not confuse this with `CheckInResponse`
  (`BETTER`/`SAME`/`WORSE`), which is a different, already-implemented
  concept — a post-session report, not a pre-session state selector.)*
- **Evidence routing / safety filter** — no `SafetyFilter`, no
  `InterventionRouter`, no `evidence_catalog.json`.
- **Personalization / response history** — no scoring, no `PersonalizationScorer`.
- **Episode logging** — no `Episode` model, no persistence of any kind.
- **Recovery duration / timer** — `RECOVERY` is a state you can sit in
  indefinitely; nothing times it out.
- **10-minute follow-up** — no `AlarmManager` usage, no follow-up notification.
- **Quick Settings Tile** — no `TileService`, confirmed absent from the manifest.
- **Launcher shortcut** — no `res/xml/shortcuts.xml`, confirmed absent.
- **Trusted contact** — no SMS composer intent anywhere.
- **Emergency call** — no dialer intent, no `ACTION_DIAL` anywhere.
- **Evidence UI** — no traceability screen.
- **Insights / pattern view** — does not exist.
- **Brown noise** — no `res/raw/`, confirmed absent; no `AudioEngine` at all.
- **Build-time RAG** — not part of the runtime app by design; would be
  tooling outside this repository if ever built.
- **AI (runtime)** — no AI SDK dependency anywhere in `build.gradle.kts`.
- **Backend** — no network client dependency of any kind.
- **Sensors / biometric measurement** — no sensor API import anywhere in
  the codebase; confirmed by the same grep that backs §14's terminology fix.

---

## 18. How The Current Architecture Should Evolve

`plan.md` (§15, revision 3) describes the eventual full session flow as:

```
ANCHOR_OPEN → ACTIVATING → GROUNDING(step) ⇄ MANUAL_OVERRIDE
  → OPTIONAL_STATE_SELECT → ROUTING → INTERVENTION(step)
  → REASSESSMENT ─┬─ BETTER → SESSION_COMPLETE
                  ├─ SAME   → ROUTING (tried set grows)
                  └─ WORSE  → SAFETY_STOP → SESSION_COMPLETE
  → FOLLOW_UP_PENDING → FOLLOW_UP_COMPLETE → IDLE
```

layered conceptually as:

```
Anchor Now
    ↓
Personal routine
    ↓
Intervention selection (safety filter → evidence catalog → ranking)
    ↓
Haptic / audio intervention
    ↓
Reassessment (Better / Same / Worse)
    ↓
Episode record (local, private)
    ↓
Personal response history (feeds future ranking)
```

**The extend-vs-replace decision this section used to flag as open was made
in M5: extend.** `SessionStateMachine` now has `ROUTING`, `INTERVENTION`,
and `SAFETY_STOP` (§10), closing the two most important gaps between M4 and
this diagram — a `SAME` response now retries through a routed
`INTERVENTION` rather than looping raw `GROUNDING`, and a `WORSE` response
reaches a dedicated (still-inert) `SAFETY_STOP` instead of behaving
identically to `SAME`.

**What is still a real gap**, now purely additive (no more forks to
resolve, just features to build):

- **`ROUTING` has no selection logic.** It advances to `INTERVENTION`
  unconditionally. Building the intervention catalog and a real routing
  rule is what would give this state something to actually choose between
  — see `plan.md §8`.
- **No `OPTIONAL_STATE_SELECT`.** There is still no current-state concept
  ("what feels closest right now?") anywhere in the codebase (§17).
- **`SAFETY_STOP` has no real screen.** It stops haptic/audio output (this
  is implemented and verified) and requires an explicit acknowledgement to
  leave, but has no contact option, no crisis resources, nothing beyond a
  placeholder disclaimer. This is the next safety-adjacent feature to plan.
- **No `MANUAL_OVERRIDE`, `SESSION_COMPLETE`, or `FOLLOW_UP_*`.** No
  persistence, no `ActiveSessionStore`, no process-death handling exists —
  a force-killed app loses all session state, same as at the end of M4.
- **`cancel()` was deliberately not extended** to `ROUTING`/`INTERVENTION`
  in M5, to keep that change reviewable as "add three states" rather than
  "also revisit cancellation." Worth doing before this is user-facing.

None of the above requires reopening the architecture question — they are
now feature work on a settled foundation, each substantial enough to plan
on its own rather than being decided implicitly by whichever module reaches
it first.

---

## 19. Kotlin / Android Concepts Used

Only concepts actually present in this codebase.

- **`data class`** — like a Java `record`, but older and more capable
  (supports inheritance-free `copy()`, structural `equals`/`hashCode`
  auto-generated from constructor properties). Used for `HapticPattern`,
  `HapticCapabilities`, `DebugPulse`. **Gotcha:** Kotlin's auto-generated
  `equals`/`hashCode` use *referential* equality for array properties (like
  Java), so `HapticPattern` manually overrides both to use
  `contentEquals`/`contentHashCode` — remember this if you ever add another
  array field to a data class.
- **`interface`** — same as Java. `HapticEngine`.
- **`object`** — a Kotlin keyword for a compiler-managed singleton; no
  `getInstance()` boilerplate needed. Used for `HapticPatterns` (think:
  a Java class with a private constructor and only static fields, but the
  language does the plumbing).
- **`enum class`** — same as Java enums. `SessionState`, `CheckInResponse`.
- **`sealed interface`** — a closed set of implementing types, all known at
  compile time (roughly like Java 17's `sealed` interfaces). Used for
  `TransitionResult`, with two implementations: `data object Success` (a
  singleton case with no data — `data object` is new in Kotlin 1.9) and
  `data class Rejected(val reason: String)`.
- **`StateFlow` / `MutableStateFlow`** — from `kotlinx.coroutines.flow`.
  The closest Spring analogy is a hot `Flux` that also always has a current
  value you can read synchronously (`.value`). `MutableStateFlow` is the
  private writable side; `StateFlow` (via `.asStateFlow()`) is the public
  read-only view — the same private-mutable/public-immutable pattern you'd
  use exposing a `List` field in Java.
- **`Composable` functions** — Kotlin functions annotated `@Composable`
  that *describe* UI declaratively; Compose re-runs ("recomposes") them
  when observed state changes, similar in spirit to how a React component
  re-renders off changed props/state — not like imperative Android
  `View.findViewById` code.
- **`remember { }`** — tells Compose "compute this once and keep it across
  recompositions" (otherwise a new `SessionStateMachine()` would be created
  every time the screen redraws). Roughly like a `@Bean` with a narrow,
  UI-lifecycle-tied scope rather than application scope.
- **`collectAsState()`** — subscribes a Composable to a `StateFlow` and
  triggers recomposition on every new value; the Compose equivalent of a
  reactive subscriber.
- **`LaunchedEffect(key)`** — runs a coroutine block when `key` changes,
  cancelling any previous run. Used to react to `state` changes and drive
  haptics as a side effect, outside the render path itself.
- **`by` property delegation** — `var intensity by remember {
  mutableFloatStateOf(1f) }` reads/writes through Compose's state system
  using ordinary `intensity = x` syntax instead of `.value`. Unlike
  anything in standard Java; think of it as syntactic sugar over a getter/setter
  pair Compose manages for you.
- **`::functionName`** — a callable reference, passing a function as a
  value (`PatternButton(..., ::playPattern)`), similar to a Java method
  reference (`Class::method`).
- **`internal` visibility** — narrower than `public`, wider than `private`;
  visible anywhere in the same Gradle module (main and test source sets
  included, since AGP compiles them with access to each other's `internal`
  declarations). Used for `scaleAmplitudes` so it's directly unit-testable
  without becoming part of the public API.
- **`ComponentActivity` / `Context`** — Android's activity base class and
  the interface for "a handle to system services and app resources."
  `Context` is threaded into `SystemHapticEngine`'s constructor specifically
  as `context.applicationContext` (never the `Activity` context) to avoid
  leaking the activity's memory beyond its lifecycle.
- **`Build.VERSION.SDK_INT` / `Build.VERSION_CODES`** — the running device's
  API level and named constants for it (e.g. `TIRAMISU` = 33), used
  throughout `SystemHapticEngine` to gate API-level-specific code paths.
- **`VibratorManager` / `Vibrator` / `VibrationEffect` / `VibrationAttributes`**
  — the actual Android vibration APIs, confined entirely to
  `SystemHapticEngine.kt`.
- **Gradle Kotlin DSL (`.kts`)** — this project's `build.gradle.kts` files
  are Kotlin, not Groovy; syntactically closer to a typed configuration
  script than Maven's XML, but conceptually the same job (declare plugins,
  dependencies, build variants).

---

## 20. Debugging Guide

### "Haptics don't work"
1. Check the on-screen "Engine" line. If it says `DebugHapticEngine`, you're
   on an emulator (or the heuristic in `buildEngineForThisDevice()`
   misidentified your device) — **no physical vibration is possible or
   expected**; that's not a bug.
2. If it says `SystemHapticEngine`, check the Capabilities readout —
   `hasVibrator = false` means the device genuinely reports no vibrator.
3. Check `adb logcat | grep SystemHapticEngine` for a `"play(...) failed"`
   warning — every failure path logs before returning `false`.
4. Check system Do Not Disturb / vibration-intensity settings on the device
   — these sit *outside* the app and are not overridden by
   `USAGE_ACCESSIBILITY` in every OEM's implementation.

### "Emulator visualization doesn't work"
1. Confirm the engine really is `DebugHapticEngine` (see above).
2. In `DevHapticTestScreen`/`SessionStateTestScreen`, confirm
   `(engine as? DebugHapticEngine)?.activePulse?.collectAsState()` — if
   `engine` isn't actually a `DebugHapticEngine` at that call site, this
   silently evaluates to `null` and the circle never animates.
3. `adb logcat | grep DebugHapticEngine` should show a `"would play..."`
   line on every button press — if it doesn't, the button's `onClick` isn't
   reaching `engine.play()`.

### "Stop doesn't work"
1. Confirm the STOP button is actually visible — `DevHapticTestScreen`'s
   `Column` scrolls (§14, Fix 3); on a small screen or a nested composable
   that lost that modifier, it can be clipped off-screen again.
2. Confirm `stopPattern()`/`stopPattern` calls `engine.stop()`, not just
   clearing local UI state.

### "Intensity doesn't behave correctly"
1. Remember intensity is read **at the moment `play()` is called** — moving
   the slider mid-vibration has no visible effect until the next tap. This
   is documented on-screen and is not a bug.
2. To verify the math directly, read `AmplitudeScalingTest.kt` — it proves
   `scaleAmplitudes` in isolation, independent of any device.

### "Session state doesn't change"
1. Check the on-screen "Rejected: X → Y" message — if a transition looks
   like it "did nothing," it was very likely rejected because
   `currentState` wasn't the expected `from` value. This is intentional
   protective behavior, not a bug.
2. Read `SessionStateMachine.transition()` directly — it is nine lines and
   the entire mechanism lives there.

### "Invalid transition behaves incorrectly"
1. Compare against the transition table in §10 — every method's `from`/`to`
   pair is fixed and explicit; there's no dynamic table to misconfigure.
2. Check `SessionStateMachineTest.kt` — the three "invalid X to Y" tests
   assert both `Rejected` **and** the exact `reason` string; if you add a
   new method, add a matching test rather than trusting the label on a
   debug button (§14, Fix 4, is the cautionary tale).

### "Compose doesn't update"
1. Confirm you're reading state via `collectAsState()` (or `by remember`),
   not capturing a plain `val` snapshot at composition time.
2. Confirm the `StateFlow` you're collecting is actually the one being
   written — e.g. a stray second `SessionStateMachine()` instance (not
   wrapped in `remember`) would silently create a second, disconnected
   state flow.

### "Build/test fails"
1. Run `./gradlew testDebugUnitTest assembleDebug --console=plain` and read
   the first `FAILED` task, not the last line.
2. Unit tests run against a stub `android.jar`; any call into
   un-stubbed Android code (e.g. `android.util.Log`) throws unless
   `testOptions.unitTests.isReturnDefaultValues = true` is set in
   `app/build.gradle.kts` (it already is — if a fresh test file suddenly
   fails with a "not mocked" error, check that setting wasn't reverted).

---

## 21. File-by-File Reference

| File | Module | Responsibility | Important symbols |
|---|---|---|---|
| `MainActivity.kt` | M0 | App entry point; picks the active dev screen | `MainActivity` |
| `ui/HomeScreen.kt` | M0 | Placeholder product screen (currently unreachable) | `HomeScreen` |
| `ui/theme/Color.kt` | M0 | Light/dark palette | `AnchorPrimary`, `AnchorBackgroundDark`, ... |
| `ui/theme/Theme.kt` | M0 | Material 3 theme wrapper | `AnchorTheme` |
| `ui/theme/Type.kt` | M0 | Typography | `AnchorTypography` |
| `core/haptics/HapticEngine.kt` | M1 | Hardware-agnostic vibration contract | `HapticEngine` |
| `core/haptics/HapticPattern.kt` | M1 | Raw waveform data shape | `HapticPattern`, `TEST_PULSE` |
| `core/haptics/HapticCapabilities.kt` | M1 | Device/engine capability snapshot | `HapticCapabilities` |
| `core/haptics/SystemHapticEngine.kt` | M1 | Real `Vibrator` implementation | `SystemHapticEngine`, `scaleAmplitudes` |
| `core/haptics/DebugHapticEngine.kt` | M1 | Emulator stand-in, no real vibration | `DebugHapticEngine`, `DebugPulse` |
| `core/haptics/HapticPatterns.kt` | M2 | Named pattern catalog | `HapticPatterns` (`TEST_PULSE`…`BREATHING_OUT`, `ALL`) |
| `devtools/DevHapticTestScreen.kt` | M3 | Manual haptic verification UI | `DevHapticTestScreen`, `buildEngineForThisDevice` |
| `domain/session/SessionState.kt` | M4 | Session stage + check-in response enums | `SessionState`, `CheckInResponse` |
| `domain/session/SessionStateMachine.kt` | M4 | Session transition logic | `SessionStateMachine`, `TransitionResult` |
| `devtools/SessionStateTestScreen.kt` | M4 | Manual session verification UI + haptic coordination | `SessionStateTestScreen`, `buildHapticEngineForThisDevice` |
| `test/.../HapticPatternTest.kt` | M1 | `HapticPattern` validation/equality | 5 tests |
| `test/.../AmplitudeScalingTest.kt` | M1 | `scaleAmplitudes` pure-function behavior | 6 tests |
| `test/.../DebugHapticEngineTest.kt` | M1 | `DebugHapticEngine` honesty guarantees | 5 tests |
| `test/.../HapticPatternsTest.kt` | M2 | Catalog integrity + terminology guard | 12 tests |
| `test/.../SessionStateMachineTest.kt` | M4 | All transitions, rejections, repeated actions | 16 tests |
| `AndroidManifest.xml` | M0 | One activity, no permissions/services/receivers | — |
| `app/build.gradle.kts` | M0 | Module dependencies/config | Compose BOM, Material 3, JUnit |

---

## 22. Complete Mental Model

```
HapticPattern
    = describes a vibration (data only — timings, amplitudes, repeat)

HapticEngine
    = the contract for "something that can play a HapticPattern"

SystemHapticEngine
    = connects HapticEngine to real Android vibration hardware
    = the ONLY class allowed to import android.os.Vibrator*

DebugHapticEngine
    = simulates haptic execution for emulator testing
    = never claims a physical vibration occurred

HapticPatterns
    = the named catalog (TEST_PULSE, DOUBLE_PULSE, SLOW_PULSE,
      BREATHING_IN, BREATHING_OUT) — the only place raw arrays live

SessionState / CheckInResponse
    = describes which stage a session is in, and how a check-in was answered
    = carries no other data

SessionStateMachine
    = the ONLY thing allowed to change SessionState
    = knows nothing about Compose, Android, or HapticEngine

DevHapticTestScreen / SessionStateTestScreen
    = display state, accept button taps, call into the two domains above
    = the ONLY place haptics and session state currently meet
      (via one LaunchedEffect, not a shared class)

MainActivity
    = picks exactly one Composable to show; currently SessionStateTestScreen
```

Each layer knows strictly less than the layer above it. `HapticPattern`
doesn't know Android exists. `SessionStateMachine` doesn't know haptics
exist. Only the two dev screens know about both, and even they only touch
each domain through its own public interface (`HapticEngine`,
`SessionStateMachine`'s methods) — never around it.

---

## 23. M0–M4 Checkpoint Summary

### Implemented
A working hardware abstraction for vibration (`HapticEngine` +
`SystemHapticEngine` + `DebugHapticEngine` + `HapticCapabilities`), a
five-pattern catalog (`HapticPatterns`), a tested six-state session state
machine (`SessionStateMachine`) with hard guarantees against invalid and
repeated transitions, and two developer screens that exercise both, one of
which wires them together live via a `LaunchedEffect`. 44 unit tests, all
passing. `assembleDebug` succeeds. Both dev screens have been manually
verified end-to-end on a running Android emulator (button-precise, via
`uiautomator dump`, not guessed coordinates).

### Corrected
Four issues, found and fixed during the M2–M4 build turns rather than this
audit turn (which found none new): physiological terminology
(`HEARTBEAT`→`DOUBLE_PULSE`), audio-framed wording ("loudness"→"strength"),
an unreachable STOP button (missing scroll), and mislabeled debug buttons /
tests whose rejection-reason strings didn't match their own names.

### Intentionally deferred
Everything in §17 — no Anchor Now, no intervention catalog (content/data —
`INTERVENTION` the *state* now exists, `INTERVENTION` the *catalog entry
concept* does not), no personalization, no persistence, no audio, no
triggers beyond dev buttons, no real safety screen or contact/emergency
behavior, no AI, no backend, no sensors. `SAFETY_STOP` exists and is
reached on `WORSE`, but it is inert by design — no automatic action of any
kind happens there yet.

### Remaining risks
**Resolved in M5:** the extend-vs-replace fork on `SessionStateMachine` — the
team chose extend; `ROUTING`/`INTERVENTION`/`SAFETY_STOP` were added
(§10). Still open: a duplicated ~10-line emulator heuristic across two dev
files, one defensively-safe-but-stylistically-risky `!!` in
`SystemHapticEngine`, and `DevHapticTestScreen` currently being unreachable
without a manual code edit. New from M5: `cancel()` still only works from
`GROUNDING` — it was deliberately not extended to `ROUTING`/`INTERVENTION`,
so a session stuck retrying currently has no way back to `IDLE` except
finishing the loop or reaching `RECOVERY`/`SAFETY_STOP` — worth revisiting
before this becomes a real user-facing screen.

### Current demo capability
You can hand someone a phone, tap through a full session including a retry
loop (`IDLE→ACTIVATING→GROUNDING→EASING→CHECK_IN→SAME→ROUTING→
INTERVENTION→EASING→CHECK_IN→BETTER→RECOVERY→IDLE`), feel a real vibration
turn on and off exactly during each activity stage, trigger the `WORSE`
path and show haptics stopping immediately with zero contact/emergency
behavior firing, and show that mashing buttons or attempting an impossible
transition cannot break any of it. That is a legitimate, honest
hardware+state-machine demo, now including the safety-relevant branch. It
is not yet a demo of "Anchor helps someone in distress" — there is still no
distress-facing UI, no real intervention content, and no real safety
screen.

### Next architectural decision
With the state-machine shape settled, the next real decision is what
`ROUTING` should actually route *between* — i.e., starting the intervention
catalog (`plan.md §8`) that gives `INTERVENTION` something other than a
placeholder haptic to play, and what the real `SAFETY_STOP` screen should
contain. Both are substantial, safety-adjacent features and should get the
same plan-first treatment this module did.

**This document describes the codebase as of the end of the M5 extension.
Module 6 has not been started.**
