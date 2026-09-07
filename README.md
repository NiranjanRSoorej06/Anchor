# Anchor

An offline-first Android app for grounding and stabilization during acute distress (panic, flashbacks, dissociation, PTSD-related crises). Built with Kotlin and Jetpack Compose.

> **One-liner:** Offline grounding button that works in airplane mode. Haptic feedback in under 300ms, no quizzes in the acute path, no backend.

## Why Anchor

Most mental-health apps assume network access and lean on generic biometric or AI claims. Anchor is built the opposite way:

- **Offline-first, deterministic.** No network calls, no LLM/STT/TTS at runtime. Same input always produces the same output.
- **Haptics are the product.** A one-tap "ANCHOR NOW" button triggers tactile grounding patterns (double pulse, breathing-in/out) immediately, before any UI decision-making.
- **Privacy by design.** No trauma-narrative text field, no ambient audio upload, no diagnosis, ever.
- **Safety filter overrides everything.** A dedicated safety layer (`domain/safety`) enforces hard vetoes (e.g. no auto-dial, no auto-SMS) and guarantees the user is never shown an empty screen.
- **Dignity-first support.** Emergency contact is reached through a pre-written, user-confirmed SMS intent, not a silent automated alert (unless the user opts into Companion Mode).

## Core features

- **ANCHOR NOW session** — a state-machine-driven grounding flow (`IDLE → ACTIVATING → GROUNDING → EASING → CHECK_IN → RECOVERY`) that starts haptic feedback immediately on tap.
- **Haptic engine** — device-agnostic vibration patterns (`HapticEngine` interface) with a real hardware implementation and a debug/emulator fallback.
- **Grounding via camera + ML Kit** — a single-frame back-camera capture labeled on-device with Google ML Kit, never uploaded.
- **Safety plan & safety phrases** — a Stanley-Brown 6-step safety plan model and bundled/user-recorded safety phrases for immediate playback.
- **Companion Mode (opt-in)** — background SMS + last-known-location sharing with a trusted contact, clearly separated from the default (manual, dignity-preserving) SOS path.
- **Routines** — user-composable grounding sequences (haptic, breathing, safety phrase, pause steps), validated for length and safety.
- **Content library** — pre-authored grounding exercises, coping statements, and a sleep hygiene / CBT-I tool, all offline and evidence-referenced.
- **Home-screen widget & accessibility trigger** — a Glance widget and an optional volume-button long-press trigger for fast access.
- **Personalization & follow-up** — lightweight Better/Same/Worse feedback loop and an optional post-episode check-in, with no repeated clinical assessments.

## Architecture

The codebase separates plain-Kotlin domain logic from Android/UI concerns so the core behavior can be unit tested without a device or emulator:

```
app/src/main/java/com/anchor/
├── core/        # Hardware & platform boundaries (haptics, audio, vision, location, logging, reminders, companion)
├── domain/      # Pure Kotlin business logic (session, safety, routing, routine, triage, profile, history, ...)
├── ui/          # Jetpack Compose screens
├── data/        # Persistence (DataStore + JSON)
├── widget/      # Home-screen Glance widget
└── trigger/     # Accessibility-service based volume trigger
```

Key design patterns:
- **Interface boundaries** (`HapticEngine`, `EpisodeStore`, etc.) keep Android hardware APIs out of the domain layer, enabling debug/fake implementations for tests and emulators.
- **Safety-first routing** — the intervention router (`domain/routing`) ranks and selects exercises only after the safety filter (`domain/safety`) permits them, with a guaranteed non-empty fallback.

See `docs/vision.md` for the full locked product spec and `docs/technical-features.md` for a detailed, auto-generated feature breakdown.

## Tech stack

| Technology | Purpose |
|---|---|
| Kotlin | Application and domain language |
| Jetpack Compose + Material 3 | UI |
| Android SDK 35 (min 26) | Platform target |
| Kotlin Coroutines | Async camera capture & labeling |
| CameraX + Google ML Kit (Image Labeling) | On-device visual grounding |
| Jetpack Glance | Home-screen widget |
| AndroidX AccessibilityService | Volume-button trigger |
| Play Services Location | Companion Mode location sharing |
| JUnit 4 | Unit tests for domain logic |

## Getting started

### Prerequisites
- Android Studio (Koala or newer recommended)
- JDK 17
- Android SDK 35, min SDK 26

### Build

```bash
git clone https://github.com/NiranjanRSoorej06/Anchor.git
cd Anchor
./gradlew assembleDebug
```

### Run tests

```bash
./gradlew test
```

The domain layer (`app/src/test`) is pure Kotlin and runs on the JVM without an emulator.

### Permissions

Anchor requests permissions only for the features that need them, requested contextually rather than all at launch:

| Permission | Used for |
|---|---|
| `VIBRATE` | Core haptic grounding |
| `RECORD_AUDIO` | Optional custom safety-phrase recording |
| `SEND_SMS` | Dignity SMS to a trusted contact (SOS path) and Companion Mode |
| `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION` | Companion Mode's location-in-alert feature only |
| `POST_NOTIFICATIONS` | Post-SOS check-in reminder |
| `WAKE_LOCK` / `DISABLE_KEYGUARD` | Showing the grounding screen over a locked device |

## Project status

Anchor started as a hackathon project and is under active development. Many domain modules (safety, routing, routines, triage, profile, history, sleep, etc.) are built and unit-tested; UI wiring for several of these (routine playback/recording, onboarding, profile persistence) is still in progress. See `docs/vision.md` for what's locked vs. planned, and `docs/technical-features.md` for what is currently wired into the runtime flow versus foundation-only.

## Documentation

- [`docs/vision.md`](docs/vision.md) — locked product vision and scope decisions
- [`docs/technical-features.md`](docs/technical-features.md) — auto-generated, up-to-date feature inventory
- [`docs/anchor_Symptom_Exercise_Evidence_table.md`](docs/anchor_Symptom_Exercise_Evidence_table.md) — evidence mapping symptoms to interventions
- [`evidence.md`](evidence.md) — clinical evidence backing the app's interventions
- `docs/` — additional research, teardown notes, and design docs

## Disclaimer

Anchor is a self-management and grounding tool. It is **not** a diagnostic tool, a replacement for professional mental health care, or an emergency service. If you or someone you know is in crisis, please contact local emergency services or a crisis helpline.

## License

All rights reserved. See [LICENSE](LICENSE) for details.
