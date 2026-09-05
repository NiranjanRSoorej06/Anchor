# ORACLE_REREVIEW.md — Full-spec cut (2026-09-05, ora-1 reconciled)

Full result in task ses_f925929ffffe1qxz8HIP12Ajba. Key deltas vs ora-1:

## Verdict
Team went 3-feature MVP -> 7-step pipeline + Django + 4 AI services + nugon fork + Ed25519. Right for "what's next", wrong for 30h.

Kill for demo: nugon fork + A11y background (abandoned, Android14/15 breaks, OEM kills), MIT AST self-host (~340MB, slow, misclassify liability), Django+Channels (contradicts offline-first, WiFi fail = demo fail).
Fragile: Deepgram+Groq+ElevenLabs chain (3 deps, quotas: Groq 30RPM, ElevenLabs 10k chars/mo).
Buildable low-value: Ed25519 (invisible), brown-noise (kills work-safe).
Survives: haptics VibrationEffect.Composition (API31+, fallback basic), anchor phrase (mic one-time local), trusted SMS via Intent (no backend).

## Privacy contradiction fix
5s ambient upload vs offline-first promise = trust killer. Fix: A) on-device SoundClassifier/ML Kit (~5MB, no upload) LOW, B) dB threshold -> brown noise, "we never listen" ZERO, C) keep AST on-device only + explicit consent + manual picker fallback. Never upload ambient audio to server in PTSD app.

## Volume verdict
Foreground (screen on, onKeyDown) HIGH reliable YES. Background (screen off pocket, A11y/MediaSession/foreground service) LOW (OEM kills, Android14 specialUse, Doze). nugon last ~2023 targets 10-12. Demo-safe: Tier0 big floating button (no perms, 100%), Tier1 foreground triple-press, Tier2 background CUT to slide (Pixel + disable battery opt + pray if must).

## Locked MVP
Tier0 never-fail (~6-8h): HapticEngine.kt + big PanicButton + SUDS pre/post, fallback visual pulse / full-screen tap.
Tier1 wow pick 2 (~8-12h): anchor phrase (fallback presets), trusted SMS (fallback share-sheet), visual breathing circle (fallback text).
Tier2 slide: background vol, brown-noise, AST, Django WS, Deepgram/Groq/ElevenLabs chain, Ed25519, hangover, EMA.

60s offline version MORE impressive than backend version. Judge holding phone feeling haptics + "no server needed" wins.

## Team split 3-4 devs, merge hr28, freeze 26-28
A: Haptic Core (HapticEngine, SudsSlider, PanicButton), B: Anchor+SMS (record/play, Intent, PermissionExplainer), C: Breathing+Onboarding (BreathingView, trauma picker, dark theme), D bonus: backend skeleton separate folder NOT required. Rules: no shared ViewModel, no shared Composable (C owns theme), single module/Activity+Nav, feature flags, no backend in Tier0-1.
