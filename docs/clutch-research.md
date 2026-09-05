# Clutch Verification: Volume-Button Background Trigger (2026-09-05)

Claim: volume-button pattern while locked via AccessibilityService + silent
MediaSession audio loop (Doze bypass), validated by OSS prior art (Nugon SOS
+ "Dictate"). Verdict: HALF TRUE — and the true half still loses to our
constraints. Clutch stays DEFERRED; first stretch item if demo gaps close early.

## Nugon SOS: VERIFIED (with caveats)
- Repo: https://github.com/jzsalinas/nugon-android — MIT, Java, 4 stars,
  15 commits, v1.4.0 (Jul 2026). Spanish-language niche project.
- Real claims: long-press volume (1.5s) works screen-off; screen-ON uses
  AccessibilityService; screen-OFF uses MediaSession + silent AudioLoop;
  ContentObserver volume-jump fallback when music plays; FGS with
  IMPORTANCE_LOW notification; SMS + location alert.
- Caveats the pitch must NOT omit: the silent-audio hack ships COMMENTED
  OUT by default for Play policy compliance; persistent notification
  required (kills "discreet"); 10-min FGS timeout risk; media-session
  conflicts (Spotify/YouTube steal volume routing); Android 15 blocks media
  FGS from BOOT_COMPLETED; Android 17 APM revokes non-tool accessibility
  services; silent loops may fail Play review (not user-perceptible).

## "Dictate": DOES NOT EXIST as claimed
- The real Dictate (DevEmperor/DictateKeyboard) is a Whisper voice-dictation
  KEYBOARD. No volume trigger, no lock overlay, no emergency audio posting.
- No single OSS project validates volume→overlay→backend end-to-end.
  NEVER cite Dictate to judges. The audio-posting half is irrelevant anyway:
  we forbid ambient upload and have no backend.

## Costs that kill it for the demo
- Accessibility enablement shows the system warning "observe your actions,
  retrieve window content" — the exact trust killer our complaints research
  flagged, and it detonates pitch pillar 4 (zero-permission default).
- Play policy (Oct 2025, enforced Jan 2026): non-tool apps need declaration
  form + demo video + in-app disclosure + consent. Sideload-only for demo;
  Android 13+ restricted-settings adds "Allow restricted settings" friction.
- OEM killers (Xiaomi/Oppo/Vivo = India majority): accessibility + FGS die
  by default; 4–6 manual settings across hidden menus. Nugon itself admits:
  on aggressive devices, wake the screen first. A judge's unknown Xiaomi
  fails silently. Demo-safe only on a pre-configured Pixel/Samsung.
- Effort: 5.5–9.5 days realistic. Nothing about it fits 36h alongside SMS,
  privacy close, near-you, and onboarding.

## If it ever gets built (Tier1 spike, not demo)
Port Nugon's EmergencyService detection to Kotlin; trigger = SMS template
(not audio upload); overlay via showWhenLocked (no SYSTEM_ALERT_WINDOW
needed for full-screen); keep on-screen ANCHOR NOW as the fallback the
demo already has. Re-verify Play policy at build time.

## Sources
- Nugon SOS: https://github.com/jzsalinas/nugon-android
- Volume Power App (documented silent-loop): https://github.com/depaolaluigi123/Volume-Power-App
- KeepLiveService: https://github.com/Pangu-Immortal/KeepLiveService
- Play a11y policy: https://support.google.com/googleplay/android-developer/answer/10964491 (+ Oct 2025 update 16550159)
- Media3 background: https://developer.android.com/media/media3/session/background-playback
- Android 15 FGS changes: https://developer.android.com/about/versions/15/changes/foreground-service-types
- Restricted settings: https://support.google.com/android/answer/12623953
- dontkillmyapp (Xiaomi/Oppo/Vivo): https://dontkillmyapp.com/
