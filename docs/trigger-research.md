# Anchor Pocket-Trigger Workarounds + Extra Features

> Part A: 5 no-A11y-Service trigger alternatives, ranked. Part B: 7 new high-empathy features.
> Sources: Android Developers docs, Media3/MediaSession APIs, Glance, Wear Tiles, Square Seismic.

---

## Part A — Pocket-Trigger Workarounds (No Background AccessibilityService)

### Ranking: Demo reliability × pocket-usability × permission cost

| Rank | Method | Demo Reliable | Pocket Usable | Perms Needed | 30h Cost |
|---|---|---|---|---|---|
| **1** | Persistent notification 1-tap | ✅ 100% | ✅ Swipe + tap | `POST_NOTIFICATIONS` (API 33+) | S (2h) |
| **2** | Glance lockscreen widget | ✅ 95% | ✅ 1-tap on lockscreen | None | M (3-4h) |
| **3** | Bluetooth earbud button | ✅ 90% | ✅ Pocket blind | `BLUETOOTH_CONNECT` (API 33+) | M (3-4h) |
| **4** | Shake detection | ⚠️ 80% | ✅ Pure gesture | `ACCELEROMETER` (no perm) | S (1-2h) |
| **5** | System A11y Shortcut | ⚠️ 70% | ✅ Hold vol keys 3s | User must enable in Settings | S (1h) |

---

### 1. Persistent Notification Action Button — ⭐ RECOMMENDED

**How**: Show a low-priority ongoing notification with a "Breathe" action button. User swipes down from lockscreen, taps "Breathe" → haptics start. Zero-cognition after initial setup.

**APIs**:
- `NotificationCompat.Builder` (AndroidX)
- `NotificationManagerCompat.IMPORTANCE_LOW`
- `PendingIntent` → `BroadcastReceiver` or `Service` start
- `addAction(R.drawable.ic_breathe, "Breathe", pendingIntent)`

**Kotlin sketch** (~10 lines):
```kotlin
val intent = Intent(context, HapticService::class.java)
val pending = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
val notification = NotificationCompat.Builder(context, "anchor_channel")
    .setSmallIcon(R.drawable.ic_anchor)
    .setContentTitle("Anchor")
    .setContentText("Tap to start breathing")
    .addAction(R.drawable.ic_breathe, "Breathe", pending)
    .setOngoing(true)
    .setPriority(NotificationCompat.PRIORITY_LOW)
    .build()
```

**Pros**: 100% reliable on all Android 8+. Works on lockscreen. No AccessibilityService. User can see it's low-priority (no sound/vibrate spam).
**Cons**: Requires user to swipe down. Slight friction vs pure hardware trigger.
**Fallback**: Big floating overlay button (WindowManager TYPE_APPLICATION_OVERLAY) — 0 perms, even simpler.

### 2. Glance Lockscreen Widget

**How**: A 1×1 Glance widget pinned to lockscreen. Single "Breathe" button. User taps widget on lockscreen → haptics start. No unlock needed.

**APIs**:
- `androidx.glance:glance-appwidget:1.3.0+`
- `GlanceAppWidget` + `GlanceAppWidgetReceiver`
- `AppWidgetProviderInfo` with `widgetCategory="keyguard"`
- `actionStartActivity()` for button click

**Kotlin sketch**:
```kotlin
class BreatheWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Button(text = "Breathe", onClick = actionStartActivity<HapticActivity>())
        }
    }
}
```

**XML metadata**:
```xml
<appwidget-provider
    android:widgetCategory="keyguard"
    android:initialLayout="@layout/widget_loading"
    android:minWidth="50dp" android:minHeight="50dp" />
```

**Pros**: True 1-tap from lockscreen. No perms. Glance is modern Compose-like API.
**Cons**: Widget must be manually placed by user. Slightly more setup than notification.
**Fallback**: If Glance lockscreen fails on specific OEM, fall back to persistent notification.

### 3. Bluetooth Earbud Button (MediaSession)

**How**: Register a `MediaSession`. When user presses earbud button → `onMediaButtonEvent()` fires → detect `KEYCODE_HEADSETHOOK` → trigger haptics. Works even if app is background-killed (system restarts MediaSessionService for last active media session).

**APIs**:
- `androidx.media3.session.MediaSession` + `MediaSessionService`
- `MediaSession.Callback.onMediaButtonEvent()`
- `KeyEvent.KEYCODE_HEADSETHOOK` (single press)
- Double-press detected via timestamp delta (< 300ms between ACTION_DOWN events)

**Kotlin sketch**:
```kotlin
class AnchorMediaService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, FakePlayer())
            .setCallback(object : MediaSession.Callback {
                override fun onMediaButtonEvent(session: MediaSession, info: MediaSession.ControllerInfo, intent: Intent): Boolean {
                    val ke = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return false
                    if (ke.keyCode == KeyEvent.KEYCODE_HEADSETHOOK && ke.action == KeyEvent.ACTION_DOWN) {
                        HapticEngine.playHeartbeat(this@AnchorMediaService)
                        return true
                    }
                    return super.onMediaButtonEvent(session, info, intent)
                }
            }).build()
    }
}
```

**Manifest**:
```xml
<service android:name=".AnchorMediaService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="true">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
        <action android:name="android.intent.action.MEDIA_BUTTON" />
    </intent-filter>
</service>
```

**Pros**: True blind-pocket trigger. System manages service lifecycle. No AccessibilityService.
**Cons**: Requires `BLUETOOTH_CONNECT` (API 33+). Media3 1.9.2+ sends ACTION_DOWN + ACTION_UP — must filter. Doesn't work without earbuds connected.
**Known bug**: `KEYCODE_HEADSETHOOK` sent twice in Media3 1.9.2+ — filter by `ke.action == ACTION_DOWN` only.
**Fallback**: If no Bluetooth connected, notification button still works.

### 4. Shake Detection (Accelerometer)

**How**: Register `SensorManager` with `TYPE_ACCELEROMETER`. When magnitude > threshold for > 0.5s window → trigger haptics. Pocket shaking = panic = trigger.

**APIs**:
- `SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)`
- `SensorEventListener.onSensorChanged()`
- Square Seismic `ShakeDetector` (copy-paste, no library needed) or `callbackFlow`

**Kotlin sketch** (~8 lines with Seismic):
```kotlin
val shakeDetector = ShakeDetector { HapticEngine.playHeartbeat(context) }
val sm = getSystemService(SENSOR_SERVICE) as SensorManager
shakeDetector.start(sm, SensorManager.SENSOR_DELAY_UI)
```

**Pros**: Zero permissions. Pure gesture. Natural panic behavior = shake phone.
**Cons**: False positives (walking, car, gym). Doesn't work if phone is still (desk). Battery drain from always-on accelerometer.
**Fallback**: Only register listener when app is in foreground or after SUDS spike.

### 5. System Accessibility Shortcut (NOT custom A11y Service)

**How**: User enables system A11y Shortcut → assigns "hold both volume keys for 3s" → system shows a dialog → user picks our app's activity → haptics start. This is the **system shortcut**, not a custom AccessibilityService.

**APIs**:
- `Settings.ACTION_ACCESSIBILITY_SETTINGS` (deep link to setup)
- `android:name="android.accessibilityservice.AccessibilityService"` in manifest (for shortcut target)
- `GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT` constant

**Setup flow** (in-app guide):
```
Settings → Accessibility → [Your App] → Shortcut → "Hold volume keys"
```

**Pros**: Works from lockscreen. No ongoing service. System-managed.
**Cons**: 3-second hold required (not instant). User must navigate Settings to enable. Only ONE A11y shortcut target per device — conflicts with TalkBack, Magnification, etc.
**Fallback**: Don't rely on this. Use as supplementary option in onboarding.

---

## Part B — Extra Features (Not in anchor_full.md)

### 1. Work-Safe Silent Mode

**Pitch**: Auto-detect work hours (calendar or manual schedule) → all tools switch to visual-only, zero audio, zero vibrate-beyond-subtle, no notification sounds. "De-escalate at your desk without anyone knowing."

**Why judges win**: Directly solves complaint #15 ("coping = just listening to calming music 10 mins, can't do that at work") and complaint #G (context-blind). Shows empathy for real daily life.

**30h cost**: S (2-3h). `WorkManager` time check + Compose conditional rendering (text-only breathing guide, subtle screen pulse).

**Fallback**: Manual toggle in settings: "Work-safe mode: ON/OFF".

### 2. Trauma-Type Onboarding Picker

**Pitch**: First launch: "What brings you here?" → Cards: Combat, Childhood Abuse, Sexual Assault, Medical Trauma, Accident/Disaster, Loss/Grief, Chronic Illness, Other. Selection tailors language, tool suggestions, and preset anchor phrases. No combat language unless selected.

**Why judges win**: Solves cluster #B ("combat-veteran specific") and complaint #3/14/23/25. "Built for everyone, not just veterans." The #1 differentiator vs PTSD Coach.

**30h cost**: S (2-3h). Compose `LazyVerticalStaggeredGrid` of cards → store selection in `DataStore` → conditional content branches.

**Fallback**: If user skips, default to "All trauma types" (generic, no combat language).

### 3. ITQ-Lite (International Trauma Questionnaire) vs PCL-5

**Pitch**: Offer ITQ-lite (6 items: 3 PTSD + 3 CPTSD DSO items) alongside or instead of PCL-5. Captures affect dysregulation, negative self-concept, relationship difficulties — the 3 CPTSD domains PTSD Coach completely ignores.

**Why judges win**: Clinically rigorous. Shows team knows DSM-5 vs ICD-11 distinction. "We screen for complex PTSD, not just PTSD." Judges with clinical background will notice.

**30h cost**: M (3-4h). 6-item questionnaire UI + scoring logic + result display. Reference: Cloitre et al. ITQ validation papers.

**Fallback**: PCL-5 if ITQ scoring is too complex for demo. Or show both side-by-side.

### 4. SUDS-Gated Safety Rail

**Pitch**: Every tool starts with SUDS (0-10 distress). After tool, ask SUDS again. If SUDS went UP or stayed same → gently suggest switching tools, exiting, or contacting trusted person. "We don't leave you worse than we found you."

**Why judges win**: Solves cluster #H ("gave panic attack, made angrier/anxious") and complaint #10/16/28. Shows safety-first design. Directly addresses PTSD Coach's failure to monitor outcome.

**30h cost**: S (1-2h). Pre-tool slider + post-tool slider + conditional UI branch.

**Fallback**: If user skips SUDS, don't block — just don't show safety rail.

### 5. Pre-Loaded Anchor Presets

**Pitch**: 5 pre-recorded anchor phrases user can one-tap to hear: "You are safe right now", "This feeling will pass", "You have survived before", "Name 3 things you see", "Feel your feet on the ground". Plus record-your-own. "Don't know what to say to yourself? We've got you."

**Why judges win**: Solves cold-start problem (user doesn't know what to record). Complaint #18 ("too simple, kindergarten") → these are clinically-framed, not patronizing. Also helps dissociation (complaint about "not ready to delve alone").

**30h cost**: S (2h). Pre-record 5 audio files (team member voices) + Compose list + playback. Room DB for custom recordings.

**Fallback**: Text-only display of phrases if audio fails.

### 6. Trusted-Contact Message Templates

**Pitch**: Pre-written SMS templates user customizes once: "Hey, having a rough moment. Don't need you to do anything, just knowing you saw this helps." / "Can you call me in 10 min?" / "I'm okay, just checking in." One-tap send via `Intent.ACTION_SENDTO`. "Reach out without explaining."

**Why judges win**: Solves cluster #F ("no human bridge") and complaint #1/17/20. Dignity-preserving (complaint #7 — SOS causes shame). Templates lower the activation barrier to contacting someone.

**30h cost**: S (1-2h). 3 editable `TextField`s in settings + `Intent.ACTION_SENDTO` with pre-filled body.

**Fallback**: If user hasn't set up contacts, show "Copy to clipboard" instead of direct SMS.

### 7. Privacy Dashboard (1-Screen Transparency)

**Pitch**: Single screen: "What we know about you: ✓ Everything stays on this phone. ✓ We never record audio. ✓ We never track your location. ✓ You can delete everything with one tap." With a big red "Delete All Data" button. "Your trauma data is yours."

**Why judges win**: Solves cluster #A ("permissions concern, no HIPAA transparency") and complaint #8/22/24/26. "The PTSD app that respects your privacy" — direct counter to PTSD Coach's 17-permission model. Open-source badge if code is public.

**30h cost**: S (1-2h). Compose screen + `RoomDatabase.clearAllTables()` + `SharedPreferences.clear()`.

**Fallback**: Just a static text screen if delete functionality is too complex.

---

## Summary: Feature Priority for 30h

| # | Feature | Maps to | Cost | Priority |
|---|---|---|---|---|
| — | **Part A #1: Notification trigger** | H3 context-smart | S (2h) | **Build** |
| 1 | Work-Safe Silent Mode | G context-blind, C generic | S (2-3h) | **Build** |
| 2 | Trauma-Type Onboarding | B one-size, H1 inclusive | S (2-3h) | **Build** |
| 6 | Trusted-Contact Templates | F no-bridge, H5 human | S (1-2h) | **Build** |
| 7 | Privacy Dashboard | A trust/privacy | S (1-2h) | **Build** |
| 4 | SUDS-Gated Safety Rail | H safety, H7 safe | S (1-2h) | **Build if time** |
| 5 | Pre-Loaded Anchor Presets | C generic, cold-start | S (2h) | **Build if time** |
| 3 | ITQ-Lite Assessment | B one-size CPTSD | M (3-4h) | v2 (stretch) |

**Total new features if all S built**: ~10-14h addition to existing MVP. Fits in 30h with 3-4 devs.

### Sources
- Android Developers: Glance AppWidget (lockscreen `keyguard` category)
- Android Developers: Media3 `MediaSession.Callback.onMediaButtonEvent()`
- Android Developers: `SensorManager` / `TYPE_ACCELEROMETER`
- Android Developers: Accessibility Shortcut (system, not custom service)
- Android Developers: Wear OS Tiles / `TileService`
- Square Seismic ShakeDetector (Apache 2.0)
- Cloitre et al. (2018): International Trauma Questionnaire (ITQ)
- complaints.md clusters A-H / synthesis.md H1-H7
