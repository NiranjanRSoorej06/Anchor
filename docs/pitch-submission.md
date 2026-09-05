# Anchor — Hackathon Submission Q&A (canonical, branch: unified)

> Honesty rules for everything below: no efficacy claims (Bröcker 2023: pooled
> SMD −0.19, n.s.), no medical-mechanism language (no "vagal stimulation"),
> demo-vs-roadmap always labeled. Evidence citations live in
> docs/ptsd-coach-evidence.md and docs/complaints.md.

## Essence paragraph (what Anchor is)

Anchor is an offline-first Android app that treats PTSD support as an
*in-the-moment* problem, not a library problem. Its star is the ANCHOR SOS:
one tap on a big blue button — or a triple-press of the volume keys even with
the phone in a pocket — fires a 30-second guided breathing sprint with
haptics, a looping calming audio track (including a loved one's recorded
MP3), and an "I'm steady" exit that flows into journaling, incident logging,
and exercises ranked for that exact incident type by a clinical routing
engine. If the user is still in distress, an opt-in companion mode alerts
trusted contacts automatically. Around the SOS sit three polished sections:
Manage Symptoms (five research-backed clusters — hyperarousal, intrusion,
dissociation, sleep, avoidance/mood — each with ranked exercises carrying
difficulty ratings and trauma-informed warnings, plus a professional-help
handoff), Tools (persistent trigger log, journal, medicine tracker, goals
with weekly progress, and a Track Progress overview), and Get Support
(India-ready: pincode-routed helplines including Tele MANAS 14416 and KIRAN,
one-tap Maps clinic search, peer communities, NIMHANS RAAH link, and personal
support contacts). Everything runs on-device with zero-permission core —
no account, no backend, no data ever leaving the phone — because the people
it serves often can't afford trust, connectivity, or a therapist.

## 1. Problem (single sentence + detail)

**Single sentence:** Trauma survivors in India face acute distress episodes
with no immediate, private, in-the-moment support between (or instead of)
therapy sessions.

**Who + scale:** PTSD and post-traumatic symptoms cut across accident
survivors, abuse survivors, disaster-affected communities, and families —
and in low-middle-income countries only 22.8% of those who need care seek it
(Koenen 2017, via Bröcker 2023), against an ~80% treatment gap in India.
Smartphones are widespread; therapists are not.

**Without a solution today:** people white-knuckle episodes alone; the
best-known free tool (VA's PTSD Coach) is a passive library whose real-world
retention collapses (87% day-one use → 0.69% at one year, Hallenbeck 2022),
whose pooled symptom effect is non-significant (Bröcker 2023), whose Android
users report 6× more glitches than iOS users (Owen 2015), and which has no
India localization, no active trigger, and no human bridge — while 51% of
users in one RCT reported a negative reaction to using it (Hensler 2022).

## 2. Value proposition

**One line:** The panic button for PTSD — active help that fires during the
episode, not a library you must remember to open.

**How it solves it:** (a) Zero-friction entry — one tap or triple-volume-press
works mid-panic, in airplane mode, no reading required; (b) guided grounding
(breathing + haptics + looping audio) that ends in logging, so every episode
becomes data; (c) ranked exercises matched to incident type instead of a
static list; (d) a human bridge via companion alerts and an India-specific
support directory for everything after the episode.

**Key use cases:** (1) Mid-panic grounding at work/home without speaking or
reading much; (2) discreet SOS when the phone can't be looked at (volume
trigger); (3) nightly 2-minute trigger log + journal building a personal
pattern record; (4) family member's phone alerting them to check in;
(5) finding a nearby clinic via pincode or calling Tele MANAS in one tap.

**Roadmap note:** demo covers all of the above; next is dose reminders,
advanced trend graphs, native Hindi rebuild (not translation — Egypt lesson),
and a guided pilot study.

## 3. Architecture / workflow

**Architecture:** single native Android app, deliberately backendless.
Presentation: Jetpack Compose (Material 3, Nord/Sage/Sand themes).
Domain: pure-Kotlin packages (session state machine, safety filter SF1–SF8,
triage → router → catalog ranking, PCL-5 scorer, safety plan, support
directory, meds/goals/journal/trigger stores). Data: on-device only —
DataStore + JSON and SharedPreferences; zero network calls at runtime.
Device APIs: AccessibilityService (volume triple-tap), MediaPlayer (looping
audio) + TextToSpeech (whisper fallback), Haptics (Vibrator), SmsManager
(companion, opt-in), AlarmManager/WorkManager (reminders, roadmap),
geo:/tel:/smsto: intents (Maps, dialer, dignity SMS), AppWidgetProvider
(home-screen widget), MediaSession (lock-screen audio routing).

**Step-by-step user flow:** (1) Distress hits → tap ANCHOR NOW or triple-press
volume (or widget). (2) 30s breathing sprint: haptic pacer + looping audio +
comfort-tool selector. (3a) "I'm steady" → optional journal → incident-type
picker → ranked exercises → done. (3b) Timeout → "feel better?" → No →
incident picker → ranked exercises; companion contacts auto-alerted if
opted in. (4) Later: Tools log triggers/meds/goals; Trends show trajectory;
Get Support connects to humans.

**Demo evidence:** no screenshots can be produced from the code machine
(no SDK); the SDK team captures per the VERIFY checklists using the
90-second script in docs/demo-plan.md (SOS → steady → journal → incident →
exercise → companion dialog → pincode support lookup).

## 4. Tech stack + justifications

| Choice | Why it won | What lost and why |
|---|---|---|
| Kotlin (single language) | Null-safe, coroutines for audio/timers, Google's Android default; one language across UI+logic halves bug surface in 36h | Java (verbose, slower to write); Dart/Flutter (see below) |
| Jetpack Compose + Material 3 | Declarative UI = screens in hours; M3 theming gives premium feel free | XML Views (2× code, slower iteration) |
| Native Android (not Flutter/RN) | Triple-tap needs AccessibilityService, haptics need Vibrator waveforms, widget needs AppWidgetProvider, SMS needs SmsManager — all native APIs; a bridge layer would have eaten the hackathon | Flutter/RN (great for forms apps; wrong for hardware-adjacent SOS) |
| No backend, DataStore+JSON on-device | ₹0 server cost, works in airplane mode, privacy story is architecture not policy; matches PTSD Coach precedent (no data leaves device) | Firebase/backend (cost, privacy liability, needs connectivity exactly when user has none) |
| MediaPlayer (not ExoPlayer) | `setLooping(true)` is all a single looping track needs; ExoPlayer's power (streaming, playlists) buys nothing here | ExoPlayer/Media3 (overkill; revisit if background playlists come) |
| TTS whisper fallback | Zero-asset audio that works with earbuds; bundled MP3 + SAF picker for loved-one voice | Recorded-only audio (excludes users with nothing recorded on day one) |
| geo:/tel: intents (not Maps SDK) | Zero API keys, zero quota, zero permissions; opens the user's own Maps app | Maps SDK (keys, billing, location permission for a feature that works without it) |
| Android-first, no iOS | Evidence: Android users report more glitches and less benefit from existing apps (Owen 2015); India is an Android market; team and time are finite | iOS-first (serves the already-served HIC/iOS skew the literature complains about) |

## 5 & 6. Scale, challenges, feasibility (duplicate question — one answer)

**Scaling:** there is almost nothing to scale technically — no servers, no
per-user cost, distribution via Play Store. Scaling is geographic and
linguistic: support-directory data per state, native (not translated)
language rebuilds per the Egypt lesson, and sharing anonymized, opt-in
aggregate insights with research partners.

**Challenges at scale:** (a) Android fragmentation — the exact glitch class
Owen 2015 documents; answer is a small matrix of physical test devices +
strict offline determinism; (b) support-directory rot (helplines change) —
answer is versioned bundled data + quarterly re-verification checklist;
(c) crisis-safety review as features grow — answer is the standing oracle
gate + safety-filter veto architecture; (d) translation quality — native
rebuilds with local clinicians, never string translation.

**Feasibility:** yes with current resources — the demo is built and pushed
(branch `unified`); marginal cost per user is ₹0; maintenance is app updates,
not ops. Timeline risk is validation (no RCT yet), stated openly.

## 7. USP, market, go-to-market

**USP (5, each citable):** (1) Active not passive — the app fires during the
episode (triple-tap hardware trigger nobody else has); (2) dissociation-gated
routing (no app does this); (3) outcome-tracked reranking that generates the
component-level dose data Possemato 2023 says the literature lacks;
(4) offline zero-permission core; (5) India-ready from day one (pincode
routing, local helplines, communities).

**Target market:** Indian trauma survivors poorly served today — accident and
abuse survivors, students, young professionals — plus their families (the
companion-mode buyer), then institutions.

**Go-to-market (VA playbook, Indian edition):** PTSD Coach spread through the
Veterans Association; we mirror that via Tele MANAS / NIMHANS / Vandrevala /
iCall partnerships — directory integration first, co-branded pilot second,
outcome-data collaboration third (our logging is built to produce exactly
the component data researchers ask for). Channels: college counselling cells,
corporate EAPs, family-doctor referrals; campaigns around exam season and
disaster response windows.

## 8. Cost, revenue, break-even, SWOT

**Unit economics (honest, pre-revenue):** acquiring one user costs ~₹0
(organic + institutional channels); serving one user costs ~₹0 marginal
(no backend, no per-seat infra). Costs are fixed: development time now,
~₹15–25L/yr to sustain a 3-person team later. Revenue hypothesis (not yet
tested): free core forever; institutional licenses (colleges, EAPs,
hospital networks) + paid clinician-dashboard module. Margins on licenses
approach software norms (~80%+) because delivery cost is ~zero.

**Break-even:** at, say, ₹3L average institutional ticket, ~8–10 institutions
cover a lean team. ROI framing for partners: early grounded episodes and
logged patterns vs. crisis escalation costs; for the pitch, social ROI first
(22.8% treatment-seeking → measurable lift), financial second.

**SWOT:**
- Strengths: offline zero-permission core; hardware SOS nobody has;
  evidence-mapped design (every feature traces to a cited gap); working demo.
- Weaknesses: no RCT/clinician-rated outcomes yet; Android-only; tiny team;
  support-directory upkeep burden.
- Opportunities: LMIC research vacuum (funders + papers want exactly this);
  Tele MANAS-scale partnership; outcome data as a research asset; iOS port later.
- Threats: wellness-app competition with bigger budgets; Play health-claims
  policy (mitigated: no medical claims, warnings everywhere); clinical adverse
  events (mitigated: safety filter, confirm-gates, trauma warnings); Android
  fragmentation bugs (mitigated: test matrix, determinism).

## 9. What's next

**Planned features:** dose reminders (WorkManager), advanced trend graphs,
native Hindi rebuild with local clinicians, PCL-5 onboarding UI, safety-plan
UI completion, widget polish, clinician/counsellor dashboard pilot.

**Long-term vision:** 6 months — English + Hindi builds, one institutional
pilot (college or EAP) producing the first engagement dataset; 1 year —
multi-language, published feasibility study (the LMIC paper the literature
keeps requesting), clinician-supported tier mirroring the VHA rollout model;
beyond — the outcome dataset becomes the product's second half: a routing
engine that knows what works for whom, Prospectively validated.

**Hackathon learnings → pivots:** (1) confirm-gate vs auto-send taught us
consent UX is a feature, not a checkbox — keep investing there;
(2) the Egypt translation failure rules out cheap localization forever;
(3) retention data says onboarding-to-first-value must be under 60 seconds —
measure it; (4) if institutional pilots stall, pivot wedge is D2C via
family buyers (companion mode) rather than survivor acquisition.
