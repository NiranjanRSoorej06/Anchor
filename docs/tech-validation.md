# Anchor Tech Stack Validation — 30h Hackathon

> **Scope**: 7 technical items. Verdict per item: FEASIBLE / FRAGILE / KILL.
> Copy-paste API names. ≤20-line Kotlin samples. Fallbacks. No fluff.

---

## 1. nugon-android Fork + AccessibilityService + MediaSession Background Volume Triple-Tap

### Status
Last release: **v1.4.0 (2026-07-29)** — actively maintained. Architecture: AccessibilityService + MediaSession + silent AudioTrack + ContentObserver + foreground service.

### What works
- **Screen ON**: AccessibilityService `onKeyEvent()` — HIGH reliability.
- **Screen OFF + no music**: MediaSession + silent AudioTrack bypass — works with battery opt disabled.
- **Screen OFF + music playing**: ContentObserver detects volume delta jumps — fallback.

### What breaks (Android 14/15)
- `foregroundServiceType` mandatory (API 34) — must declare `mediaPlayback` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- BOOT_COMPLETED FGS restrictions (API 35) — cannot launch mediaPlayback FGS from BOOT_COMPLETED
- OEM battery kill (Samsung/OneUI/Xiaomi/OnePlus) — user MUST set "Unrestricted" + enable Accessibility
- Doze mode (API 12+) — screen off + unplugged = deep sleep, services may not fire

### Verdict: FRAGILE (Tier2) — stretch, not core

### Kotlin API names
```kotlin
// AccessibilityService
android.accessibilityservice.AccessibilityService
// MediaSession
androidx.media.session.MediaSessionCompat
androidx.media.VolumeProviderCompat
// Foreground Service (Android 14+)
android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
```

### Fallback (Tier0)
Big floating "Panic" button on screen — zero permissions, 100% reliable. Triple volume press = **stretch goal only**.

---

## 2. VibrationEffect.Composition — Purr / Heartbeat / Marble

### Status
`VibrationEffect.Composition` added **API 30** (Android 11). New primitives added **API 31** (Android 12).

### Available primitives (API 31+)
| Constant | Effect | Best for |
|---|---|---|
| `PRIMITIVE_THUD` | Low thud | Heartbeat "thud-thud" |
| `PRIMITIVE_SPIN` | Rotating sensation | Marble rolling sweep |
| `PRIMITIVE_SLOW_RISE` | Gradual intensity increase | Purr rumble build-up |
| `PRIMITIVE_QUICK_FALL` | Sharp intensity drop | Purr release |
| `PRIMITIVE_CLICK` | Short sharp pulse | Tick marker |
| `PRIMITIVE_TICK` | Very short pulse | Timing marker |
| `PRIMITIVE_LOW_TICK` | Low-frequency tick | Subtle pulse |

### CRITICAL requirement
> **Caution**: If a composition contains primitives that aren't supported by the device, the **entire VibrationEffect won't play**. Must check with `vibrator.arePrimitivesSupported()`.

### Minimal Kotlin sample
```kotlin
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager

fun playHeartbeat(vibratorManager: VibratorManager) {
    val vibrator = vibratorManager.defaultVibrator
    if (Build.VERSION.SDK_INT >= 31) {
        val supported = vibrator.areAllPrimitivesSupported(
            VibrationEffect.Composition.PRIMITIVE_THUD,
            VibrationEffect.Composition.PRIMITIVE_SLOW_RISE
        )
        if (supported[0] && supported[1]) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(PRIMITIVE_THUD, 0.8f)
                .addPrimitive(PRIMITIVE_THUD, 0.8f, 200) // second thud
                .compose()
            vibrator.vibrate(effect)
            return
        }
    }
    // Fallback: basic pattern
    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
}
```

### Pre-31 fallback
Use `VibrationEffect.createWaveform()` with `longArrayOf` timings and `intArrayOf` amplitudes. Works API 26+.

### Pre-26 fallback
`vibrator.vibrate(durationMillis)` deprecated but functional.

### Verdict: FEASIBLE ✅ (core feature, never-fail with fallback)

### API names
- `android.os.VibrationEffect.startComposition()`
- `android.os.VibrationEffect.Composition.PRIMITIVE_THUD`
- `android.os.VibrationEffect.Composition.PRIMITIVE_SPIN`
- `android.os.VibrationEffect.Composition.PRIMITIVE_SLOW_RISE`
- `android.os.Vibrator.arePrimitivesSupported()`
- `android.os.VibratorManager` (API 31+)

---

## 3. MIT AST Self-Host vs ML Kit SoundClassifier vs dB Threshold

### MIT AST (Audio Spectrogram Transformer)
- Model: `MIT/ast-finetuned-audioset-10-10-0.4593` (86.6M params, PyTorch)
- Size: ~340MB float32, ~86MB quantized INT8
- Mobile latency: 200-500ms per sample, RAM: 200-500MB
- **Problems**: 340MB APK, requires PyTorch→ONNX→TFLite pipeline (hours), sluggish on mid-range, misclassification liability (527 AudioSet classes, not PTSD-specific)

### ML Kit / YAMNet (Google AI Edge)
- Model: YAMNet TFLite, **~3-5MB**, ~50ms inference, ~20-50MB RAM
- API: `com.google.mediapipe:tasks-audio` or `org.tensorflow:tensorflow-lite-task-audio`
- 521 AudioSet classes, streaming mode with `resultListener`

### dB Threshold (simplest)
- `AudioRecord` + `AudioManager`. 0 bytes model, real-time, negligible RAM.
- Louder than X dB → trigger response. No ML needed.

### Comparison
- **MIT AST**: 86-340MB, 200-500ms, high accuracy, 4-8h build. KILL for 30h.
- **YAMNet/ML Kit**: 3-5MB, ~50ms, good accuracy, 1-2h build. **RECOMMENDED**.
- **dB Threshold**: 0 bytes, real-time, low accuracy, 30min build. **Tier0 fallback**.

All three are on-device. **Never upload ambient audio to server** in a PTSD app — privacy killer.

### Fallback chain
1. dB threshold (always works, 0 deps)
2. YAMNet/ML Kit (3MB, 1h integration)
3. MIT AST (v2 only, if time allows)

---

## 4. Deepgram / Groq / ElevenLabs Free-Tier Quotas

### Deepgram (STT)
- $200 one-time credit (no CC). Nova-3 streaming: $0.0048/min → ~41,667 min (~694 hours). **Generous for demo**.

### Groq (LLM)
- Free: 30 RPM, 1,000 RPD (most models). Use `llama-3.1-8b-instant` (14,400 RPD!).
- Whisper STT: 20 RPM, 2,000 RPD. **Tight but workable** — 30 RPM saturates at 5 concurrent users.

### ElevenLabs (TTS)
- 10,000 credits/month (~10k chars standard, ~20k with Flash/Turbo). 2 concurrent requests. **Tight** — enough for demo whisper script, not ongoing use.

### Fallback chain
1. **ElevenLabs** → quota exhausted → **Android TTS** (`android.speech.tts.TextToSpeech`)
2. **Deepgram** → quota exhausted → **Groq Whisper** (backup STT)
3. **Groq LLM** → quota exhausted → **hardcoded response templates**

### Kotlin fallback API
```kotlin
// Android TTS fallback
val tts = TextToSpeech(context) { status ->
    if (status == TextToSpeech.SUCCESS) {
        tts.language = Locale.US
        tts.speak("Just a train door. You're safe.", TextToSpeech.QUEUE_FLUSH, null, "anchor")
    }
}
```

### Verdict: FRAGILE as a chain, FEASIBLE with fallbacks ✅

---

## 5. Django + Channels vs Simpler Backend

### Complexity for 30h
Requires: Django 4.2+ / channels / channels-redis / Redis / ASGI server (Uvicorn) / ASGI routing / WebSocket consumers / Nginx proxy. **Total: 14-20h** — eats entire hackathon.

### Simpler alternatives
- **No backend (Tier0-1)**: 0h. All on-device. Offline-first. The selling point.
- **Firebase Realtime DB**: 2-3h. JSON push, no WebSocket complexity.
- **Supabase Realtime**: 2-3h. PostgreSQL + WebSocket, hosted.
- **PocketBase**: 1-2h. Single-binary Go backend, SQLite, built-in auth + realtime.

### Verdict: KILL for 30h ✅ — use no-backend or Firebase if needed

---

## 6. Ed25519 via Android Keystore + AES-256 Log

### Ed25519 (digital signatures)
- Android Keystore support: **API 33+** via `ECGenParameterSpec("ed25519")`
- CTS tested: Yes — `Curve25519Test.ed25519KeyGenerationAndSigningTest()`
- Pre-33 fallback: Software-only `KeyPairGenerator.getInstance("Ed25519")` or EC P-256

### AES-256 (log encryption)
- API 23+ (Android 6.0). `AES/GCM/NoPadding`. Key via Android KeyStore.
- Build time: ~1h

### Minimal Kotlin: AES-256-GCM
```kotlin
val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
keyGen.init(
    KeyGenParameterSpec.Builder("log_key",
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build()
)
val key = keyGen.generateKey()
val cipher = Cipher.getInstance("AES/GCM/NoPadding")
cipher.init(Cipher.ENCRYPT_MODE, key)
val encrypted = cipher.doFinal(plaintext)
// IV = cipher.iv (store alongside ciphertext)
```

### Demo-necessary?
- **Ed25519 signing**: Invisible to judge. Demo shows "cryptographically signed" but nobody verifies in real-time. **v2 feature**.
- **AES-256 log encryption**: Simple 1h implementation. Worth doing if log feature ships. **build if time**.

### Verdict: Ed25519 = KILL for demo (v2). AES-256 = FEASIBLE (1h, build if time).

---

## 7. Privacy: 5s Ambient Audio Upload Contradiction

### The contradiction
- Team promises "offline-first, privacy-first"
- Spec says: "5s ambient audio → Groq/Deepgram → whispered reality check"
- **Uploading ambient audio to a cloud server in a PTSD app is a trust killer**

### Why it's worse for PTSD
- PTSD users have **hypervigilance and paranoia** as core symptoms
- Knowing the app "listens" and uploads to a server = potential panic trigger
- Privacy complaint #8 from PTSD Coach: "Permissions concern: universal data, mic/vid, no HIPAA transparency" — our app can't repeat this mistake

### Recommended fix (on-device only)

| Tier | Approach | Privacy | Latency |
|---|---|---|---|
| **Tier0** | dB threshold → brown noise (no mic permission beyond `RECORD_AUDIO`) | ZERO upload | Real-time |
| **Tier1** | YAMNet/ML Kit on-device → classify → hardcoded whispered response | ZERO upload | ~50ms |
| **Tier2** | AST on-device (if time allows) | ZERO upload | 200-500ms |

### Key principle
> **"We never listen"** is a 1-sentence privacy pitch. "We analyze sound patterns on-device and never send audio anywhere" is the fix. If user wants AI analysis, they must explicitly opt-in with clear consent flow.

### If AI whisper IS needed (stretch)
- Upload **classification result only** ("siren detected") — NOT raw audio
- Use server-side TTS to generate whisper → return audio file
- Consent screen: "Allow AI analysis? Audio processed, not stored."
- But: adds complexity, network dependency, and privacy surface — **recommend against for 30h demo**

### Verdict: KILL ambient upload ✅ — use on-device-only approach

---

## Summary Matrix

| # | Item | Verdict | Priority | Fallback |
|---|---|---|---|---|
| 1 | nugon fork / A11y background | FRAGILE | Tier2 (stretch) | Big on-screen Panic button |
| 2 | VibrationEffect.Composition | FEASIBLE ✅ | **Tier0 (core)** | createWaveform() pre-31 |
| 3 | MIT AST self-host | KILL | — | YAMNet (3MB) or dB threshold |
| 4 | Deepgram/Groq/ElevenLabs | FRAGILE chain | Tier1 (w/ fallbacks) | Android TTS + hardcoded |
| 5 | Django + Channels | KILL | — | No backend or Firebase |
| 6 | Ed25519 + AES-256 | Ed25519=KILL, AES=FEASIBLE | v2 / if-time | Skip Ed25519; AES optional |
| 7 | 5s ambient upload | KILL ✅ | — | On-device dB/YAMNet only |

### Locked MVP (never-fail core)
1. **HapticEngine** — VibrationEffect.Composition + waveform fallback (3-4h)
2. **PanicButton** — Big floating button, zero permissions (1-2h)
3. **SUDS slider** — Pre/post distress rating (1-2h)
4. **Anchor phrase** — Record/play own voice, local only (2h)
5. **Trusted SMS** — `Intent.ACTION_SENDTO`, no backend (1h)

### Tier1 (wow picks, if time)
- Visual breathing circle (2h) · Onboarding trauma picker (2h) · YAMNet sound classification (2h)

### Sources
- Android Developers: VibrationEffect.Composition, Foreground service types (14/15), KeyGenParameterSpec/Ed25519 CTS
- nugon-android v1.4.0 (2026-07-29)
- Groq / Deepgram / ElevenLabs pricing docs (2026)
- HuggingFace: MIT/ast-finetuned-audioset-10-10-0.4593
- Google AI Edge: Audio Classifier (YAMNet)
- Django Channels docs / community guides
