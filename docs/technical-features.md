# Anchor Technical Features

This document describes the technical features currently implemented in the
Anchor Android project, what each feature is used for, and whether it is part
of the usable runtime flow or is currently a domain foundation/developer tool.

## Technology Stack

| Technology | Used for |
| --- | --- |
| Kotlin | Application and domain implementation language. |
| Android SDK 35, minimum SDK 26 | Android application platform and runtime APIs. |
| Jetpack Compose | Declarative UI for the home, session, grounding, and developer screens. |
| Material 3 | Compose UI components, typography, colors, and theming. |
| AndroidX Activity and Lifecycle | Compose activity hosting, lifecycle-aware camera binding, and Android integration. |
| Kotlin Coroutines | Asynchronous camera capture and on-device labeling work. |
| Android Vibration APIs | Hardware haptic playback through `VibrationEffect` and `VibrationAttributes`. |
| Android TextToSpeech | Optional local spoken delivery when private audio output is connected. |
| Android AudioManager | Detects private audio output devices before spoken delivery. |
| CameraX | One-frame back-camera capture for visual grounding. |
| Google ML Kit Image Labeling | Offline object labels from the captured frame. |
| Jetpack Glance AppWidget | Home-screen grounding shortcut. |
| Android AccessibilityService | Optional volume-button long-press grounding trigger. |
| JUnit 4 | Unit tests for the platform-independent domain logic. |

## Runtime Features

### ANCHOR NOW session

The main user flow is exposed from `MainActivity` and `HomeScreen`:

1. The user taps **ANCHOR NOW**.
2. The app enters the session state machine.
3. Haptic grounding starts immediately.
4. The user can follow the breathing/grounding visual state.
5. The session can finish with a Better/Same/Worse outcome.

The UI is intentionally small and offline-first. The session state machine is
plain Kotlin, so it can be tested without an Android device and does not make
emergency calls, send messages, or perform network requests.

### Haptic grounding

Haptics are isolated behind the `HapticEngine` interface. This keeps Android
hardware APIs out of the domain layer and allows the same UI to run with a
debug fake on an emulator.

Implemented haptic patterns include:

- `DOUBLE_PULSE`: two gentle tactile taps for an immediate grounding cue.
- `SLOW_PULSE`: a single slow repeating pulse.
- `BREATHING_IN`: a four-second intensity ramp for inhalation.
- `BREATHING_OUT`: a six-second intensity ramp for exhalation.
- `TEST_PULSE`: a developer-only engine check.

`SystemHapticEngine` uses Android vibration hardware. It reports device
capabilities and scales intensity. `DebugHapticEngine` provides a visible test
stand-in when hardware is unavailable. Haptics are interaction mechanisms, not
biometric sensing or a clinical treatment claim.

<!-- AUTO:CORE_HAPTICS:START -->
- `DebugHapticEngine` (class) in `app/src/main/java/com/anchor/core/haptics/DebugHapticEngine.kt`: A non-hardware [HapticEngine] for the emulator, where nothing physically vibrates. This engine never claims otherwise: [play] always returns false, and [capabilities] honestly reports no vibrator. What it *does* provide is [activePulse], a plain Kotlin StateFlow (no Compose dependency here) that a later UI layer can collect to render an on-screen visual stand-in for the haptic while developing off-device.
- `DebugPulse` (class) in `app/src/main/java/com/anchor/core/haptics/DebugHapticEngine.kt`: What debug pattern is "playing", for a future visual pulse to render.
- `HapticCapabilities` (class) in `app/src/main/java/com/anchor/core/haptics/HapticCapabilities.kt`: What the current device/engine can actually do. Callers (and a later Settings screen) use this to decide whether to show a haptic-unavailable fallback rather than assuming vibration always works.
- `HapticEngine` (interface) in `app/src/main/java/com/anchor/core/haptics/HapticEngine.kt`: The hardware boundary for vibration. Nothing outside this package should import android.os.Vibrator / VibratorManager / VibrationEffect directly — every other layer (session logic, UI) depends on this interface instead, so it can run against [DebugHapticEngine] on an emulator and [SystemHapticEngine] on a device without changing a single call site. Deliberately not Compose-aware: this is plain Kotlin so it stays testable and reusable from services/receivers that have no Compose context.
- `createHapticEngine` (fun) in `app/src/main/java/com/anchor/core/haptics/HapticEngineFactory.kt`: The single, canonical place that decides which [HapticEngine] to use. Previously duplicated privately inside two devtools screens; now a real product screen needs the exact same decision, so it lives here instead — one heuristic, not three copies of it. The heuristic itself only matters for development: on a real phone this will always resolve to [SystemHapticEngine]. It exists so the emulator (which cannot vibrate) gets [DebugHapticEngine] instead, without anyone needing to switch that by hand.
- `HapticPattern` (class) in `app/src/main/java/com/anchor/core/haptics/HapticPattern.kt`: A device-agnostic vibration waveform. Maps directly onto [android.os.VibrationEffect.createWaveform]: [timings] are millisecond durations, [amplitudes] (0-255, or null for default amplitude) are the strength for each timing entry, and [repeatIndex] is the index to loop back to (-1 for no repeat). This module defines the shape only. Named, tuned patterns (double pulse, slow pulse, breathing, and future textures) live in [HapticPatterns].
- `HapticPatterns` (object) in `app/src/main/java/com/anchor/core/haptics/HapticPatterns.kt`: The catalog of named, deterministic haptic patterns Anchor can play. This is the ONLY place raw timing/amplitude arrays should be written. Everywhere else in the app should call `hapticEngine.play(HapticPatterns.DOUBLE_PULSE)` rather than constructing a [HapticPattern] by hand — that keeps the "what does this feel like" decision in one file, and keeps callers ignorant of the underlying waveform shape. These are interaction mechanisms, not medical devices: nothing here (or anywhere referencing these patterns) may claim a specific vibration is clinically proven to treat PTSD. See evidence.md and plan.md §11.1 for the claims Anchor is and isn't allowed to make. A pattern is later attached to an evidence-informed intervention by a higher layer — this file only defines what things feel like. Anchor has no heart-rate, pulse, or other biometric sensor, and never will as part of this module. "Double pulse" is only the name of a vibration rhythm (two short taps, like a knock) — it does not measure, sense, or represent any physiological signal. Do not name or describe a pattern in a way that implies otherwise. Every pattern here is a fixed, hand-authored array — same input, same output, every time. There is no randomness and no device-specific branching; that belongs to [SystemHapticEngine] (whether the platform can honor amplitude at all) not to the pattern definition.
- `SystemHapticEngine` (class) in `app/src/main/java/com/anchor/core/haptics/SystemHapticEngine.kt`: Real vibration hardware, reached through the platform APIs. Every Android call in here is wrapped defensively: a missing vibrator, an unsupported feature, or a thrown exception from the platform must never propagate out of this class. [play] and [stop] report failure by returning false / doing nothing, never by throwing.
- `scaleAmplitudes` (fun) in `app/src/main/java/com/anchor/core/haptics/SystemHapticEngine.kt`: Scales each amplitude by [intensity] (clamped 0f..1f), clamped back into VibrationEffect's valid 0..255 range. Pure function — no Android dependency — so it is unit-testable without a device or Robolectric. A null [amplitudes] array (default-amplitude waveform) passes through unchanged: there is nothing to scale.
<!-- AUTO:CORE_HAPTICS:END -->

### Private audio delivery

The audio boundary uses Android `TextToSpeech` and checks for private output
devices before speaking. When Bluetooth, wired, USB, BLE, or hearing-aid audio
is unavailable, the delivery engine remains silent rather than sending speech
through the phone speaker. `DebugAudioEngine` and `FakeAudioOutputDetector`
support host tests and previews.

<!-- AUTO:CORE_AUDIO:START -->
- `AudioDeliveryEngine` (interface) in `app/src/main/java/com/anchor/core/audio/AudioDeliveryEngine.kt`: Dual-mode audio delivery engine boundary. If Bluetooth earbuds or headphones are connected: - Speaks guided scripts in a soft, private female whisper into the user's ear. If NO earbuds are connected: - Mutes audio output completely (0% speaker sound) so the app operates in 100% silent, haptic-only mode (useful in meetings, classrooms, or public spaces).
- `AudioEngineProvider` (object) in `app/src/main/java/com/anchor/core/audio/AudioDeliveryEngine.kt`: Singleton provider to keep TTS warm in memory 24/7 across the accessibility service and app activities for zero-latency speech playback.
- `WhisperAudioEngine` (class) in `app/src/main/java/com/anchor/core/audio/AudioDeliveryEngine.kt`: Production Android [AudioDeliveryEngine] implementation backed by [TextToSpeech] and [AudioOutputDetector].
- `DebugAudioEngine` (class) in `app/src/main/java/com/anchor/core/audio/AudioDeliveryEngine.kt`: Debug/fake engine for host unit testing and Compose previews.
- `createAudioEngine` (fun) in `app/src/main/java/com/anchor/core/audio/AudioDeliveryEngine.kt`: Factory function to instantiate the system audio delivery engine (pre-warmed singleton).
- `AudioOutputDetector` (interface) in `app/src/main/java/com/anchor/core/audio/AudioOutputDetector.kt`: Detects whether private listening devices (Bluetooth earbuds, A2DP, SCO, BLE headset, or wired headphones) are connected to the system.
- `SystemAudioOutputDetector` (class) in `app/src/main/java/com/anchor/core/audio/AudioOutputDetector.kt`: System implementation backed by Android [AudioManager].
- `FakeAudioOutputDetector` (class) in `app/src/main/java/com/anchor/core/audio/AudioOutputDetector.kt`: Debug/fake detector for host unit testing and Compose previews.
<!-- AUTO:CORE_AUDIO:END -->

### Vision labeler boundary

Visual grounding uses an interface-based labeler boundary. Physical devices
use the bundled offline ML Kit implementation; emulator and preview builds use
a deterministic debug labeler. The capture screen and grounding script builder
do not depend directly on ML Kit.

<!-- AUTO:CORE_VISION:START -->
- `DebugObjectLabeler` (class) in `app/src/main/java/com/anchor/core/vision/DebugObjectLabeler.kt`: Emulator/preview-safe [ObjectLabeler]. The Android emulator's virtual camera and ML Kit's on-device model do not reliably combine to produce meaningful labels, so this returns a fixed, plausible-looking result instead — enough to exercise the whole capture -> label -> script flow without a real camera or a real model. Mirrors [com.anchor.core.haptics.DebugHapticEngine].
- `MlKitObjectLabeler` (class) in `app/src/main/java/com/anchor/core/vision/MlKitObjectLabeler.kt`: Real, on-device [ObjectLabeler] backed by ML Kit's **bundled** Image Labeling model (statically linked into the APK — see the `image-labeling` dependency in app/build.gradle.kts, chosen over the Play-Services/ unbundled variant specifically so this works immediately offline with no first-run model download). Runs entirely on-device, no network call.
- `ObjectLabeler` (interface) in `app/src/main/java/com/anchor/core/vision/ObjectLabeler.kt`: The hardware/ML boundary for turning a photo into object labels. Nothing outside this package should import ML Kit directly — every other layer (the capture screen, the grounding-script builder) depends on this interface instead, so it can run against [DebugObjectLabeler] on an emulator and [MlKitObjectLabeler] on a device without changing a single call site. Mirrors the [com.anchor.core.haptics.HapticEngine] pattern.
- `createObjectLabeler` (fun) in `app/src/main/java/com/anchor/core/vision/ObjectLabelerFactory.kt`: The single, canonical place that decides which [ObjectLabeler] to use. Mirrors [com.anchor.core.haptics.createHapticEngine] and its emulator heuristic exactly — same reasoning: on a real phone this always resolves to [MlKitObjectLabeler]; it exists so the emulator (whose virtual camera doesn't produce meaningful frames for a real model to label) gets [DebugObjectLabeler] instead, without anyone needing to switch it by hand.
<!-- AUTO:CORE_VISION:END -->

### Home-screen widget trigger

The Glance widget provides a low-friction shortcut from the Android home
screen. Tapping it launches `MainActivity` with
`EXTRA_LAUNCH_GROUNDING`, opening the visual grounding flow directly instead
of requiring navigation through the home screen.

### Volume-button trigger

`AnchorAccessibilityService` implements the optional volume-button long-press
trigger. It launches the same visual grounding flow as the widget, so the
trigger paths do not duplicate grounding logic.

The current implementation is scoped to screen-on or locked-but-awake use.
Deep-sleep interception and a guaranteed real-device volume-key behavior are
not claimed as supported until verified on hardware. The service must be
enabled by the user in Android Accessibility settings.

### Camera-assisted visual grounding

`GroundingCaptureScreen` performs one private camera capture and turns the
result into a short "things you can see" grounding script:

1. CameraX binds a back-camera preview and image-capture use case.
2. One frame is captured into memory.
3. ML Kit labels objects locally on the device.
4. `GroundingScriptBuilder` converts labels into deterministic grounding text.
5. Camera resources are unbound and the in-memory image is discarded.

The capture is not saved to the device, not added to the MediaStore, not
uploaded, and not shown as a photo. If camera permission is missing or capture
or labeling fails, the app uses a fixed fallback script instead of showing an
error or requesting permission from a trigger-only entry point.

### Theme and accessibility-oriented presentation

The Compose theme supports Anchor color palettes and a developer theme switcher
for testing. The UI uses large controls, short text, and a direct path to the
primary action to reduce cognitive and motor demands during distress.

## Implemented Domain Features

These modules are implemented as platform-independent Kotlin models,
validators, catalogs, or state machines. Some still need production UI or
persistence wiring.

| Feature | Technical implementation | Purpose | Current status |
| --- | --- | --- | --- |
| Safety filtering | `SafetyFilter`, `SafetyProfile`, safety candidates, fallback intervention | Removes interventions that conflict with sensory preferences or safety rules and guarantees a non-empty fallback | Domain logic implemented; session/UI wiring is partial |
| Intervention routing | `InterventionRouter` and `InterventionCatalog` | Excludes already-tried items, applies safety permissions, ranks by success rate, and falls back safely | Domain logic implemented |
| Session lifecycle | `SessionState`, `SessionStateMachine` | Models `IDLE`, activation, grounding, intervention, check-in, recovery, and safety-stop transitions | Runtime session path implemented; broader M5 flow remains incremental |
| User profile | `UserProfile`, `UserProfileValidator` | Stores sensory preferences, haptic intensity, intervention vetoes, and contact values without a trauma narrative | Model and validation implemented; persistent storage/UI pending |
| Episode history | `Episode`, `SessionOutcome`, `EpisodeStore`, `InMemoryEpisodeStore` | Records what helped and session outcomes without recording a trauma story | In-memory implementation available; DataStore implementation pending |
| Onboarding flow | `OnboardingFlow`, `OnboardingStep` | Models a one-time, skippable onboarding sequence and completion state | State machine implemented; onboarding screens pending |
| PCL-5 assessment | `Pcl5Catalog`, `Pcl5Scorer` | Provides a one-time, on-device screening score for baseline personalization | Scoring logic implemented; screening only, not diagnosis |
| Follow-up check-in | `FollowUpCheckIn`, `FollowUpValidator` | Models a short optional post-session check-in with trigger, distress, and note fields | Domain logic implemented; notification/UI wiring pending |
| Personalization | `PersonalizationScorer`, `SessionOutcome` | Calculates simple evidence from Better/Same/Worse outcomes and avoids acute-path scales | Scoring logic implemented |
| Guided content | `GuidedScripts`, `CopingStatements`, `SleepChecklist` | Bundles 8 grounding, breathing, progressive muscle relaxation, visualization, coping, and sleep items for offline use | Content data implemented; reader UI pending |
| Safety phrases | `SafetyPhraseBank` and phrase validation | Supplies bundled grounding phrases and validates short user-recorded phrase text | Bundled content and validation implemented; recording/playback UI pending |
| Safety plan | `SafetyPlan`, `SafetyPlanValidator` | Models a Stanley-Brown-style six-step personal safety plan | Model and validation implemented; form UI pending |
| Custom routines | `Routine`, `RoutineStep`, `RoutineValidator` | Represents ordered haptic, breathing, safety-phrase, and pause steps with duration limits | Model and validation implemented; builder/playback UI pending |
| Support directory | `SupportDirectory`, `SupportEntry`, `PinRegions`, `MapsQueries` | Provides offline-first India support entries, PIN-prefix regional lookup, and map intents | Directory data and `FindSupportScreen` implemented; live map/browser destinations require external apps/network |
| Visual grounding scripts | `GroundingScript`, `GroundingScriptBuilder` | Converts object labels into short deterministic sensory prompts | Runtime implementation active in the camera flow |

## Privacy and Safety Controls

- No backend, cloud database, LLM, or speech-to-text service is required at
  runtime. Optional spoken delivery uses the device's local Android
  `TextToSpeech` service.
- No trauma narrative field is present in the profile or episode model.
- Camera grounding uses an in-memory frame and discards it after labeling.
- The app does not automatically call emergency services or contact a trusted
  person.
- Safety filtering is applied before intervention selection.
- Dissociation and worsening outcomes have dedicated safety-stop behavior in
  the domain state model.
- Missing camera permission and camera failures degrade to a local fallback
  instead of blocking the grounding experience.

## Android Permissions and Components

The manifest currently declares:

- `CAMERA`, optional because visual grounding has a text fallback.
- `VIBRATE`, for hardware haptic patterns.
- `BIND_ACCESSIBILITY_SERVICE`, required by Android to bind the optional
  volume-button trigger service.

Registered Android components are:

- `MainActivity`, the launcher and Compose host.
- `GroundingWidgetReceiver`, the home-screen widget receiver.
- `AnchorAccessibilityService`, the optional hardware trigger service.

## Developer and Test Features

The project includes developer-only screens for:

- Haptic pattern and engine testing.
- Session state-machine transitions.
- Intervention routing and sensory-profile combinations.
- Support directory inspection.
- Theme switching.

`FindSupportScreen` is an application support screen despite living in the
`devtools` package; the other screens in that package are developer tools.

<!-- AUTO:DEVTOOLS:START -->
- `DevHapticTestScreen` (fun) in `app/src/main/java/com/anchor/devtools/DevHapticTestScreen.kt`: Top-level function: `DevHapticTestScreen`.
- `FindSupportScreen` (fun) in `app/src/main/java/com/anchor/devtools/FindSupportScreen.kt`: Top-level function: `FindSupportScreen`.
- `RoutingLabScreen` (fun) in `app/src/main/java/com/anchor/devtools/RoutingLabScreen.kt`: Top-level function: `RoutingLabScreen`.
- `SessionStateTestScreen` (fun) in `app/src/main/java/com/anchor/devtools/SessionStateTestScreen.kt`: Top-level function: `SessionStateTestScreen`.
- `ThemeSwitcher` (fun) in `app/src/main/java/com/anchor/devtools/ThemeSwitcher.kt`: Top-level function: `ThemeSwitcher`.
- `ToolsLibraryScreen` (fun) in `app/src/main/java/com/anchor/devtools/ToolsLibraryScreen.kt`: Top-level function: `ToolsLibraryScreen`.
<!-- AUTO:DEVTOOLS:END -->

The domain layer is deliberately Android-independent where possible. This
supports fast JUnit testing of validators, routing, scoring, onboarding,
content, safety rules, routines, and state transitions without an emulator.

## Not Yet Production Features

The following are represented by specifications or domain models but should
not be described as fully shipped runtime features yet:

- DataStore and JSON persistence for profile, routines, episodes, and safety
  plans.
- Onboarding, profile, safety-plan, routine-builder, and follow-up screens.
- AlarmManager notification scheduling for the post-session check-in.
- Trusted-contact SMS intent and privacy-close end card.
- Full support-directory user screen and regional selection UI.
- User voice recording and playback.
- Brown noise, ambient audio processing, passive EMA, and background capture.
- Deep-sleep volume-button interception.

These boundaries keep the MVP offline, deterministic, private, and small enough
to verify reliably on Android hardware.
