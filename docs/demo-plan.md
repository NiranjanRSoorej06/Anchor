# Demo Plan — locked feature list + pitch cards (2026-09-05)

Shareable with the SDK machine (Claude Code). Status marks: ✅ built &
on-device · 🔨 build next · 🌙 stretch only · 🃏 fake card (labeled
Preview, non-functional).

## The 90-second script (reliability story)
1. "Imagine panic hits. One button." → tap ANCHOR NOW (0–10s)
2. Phone vibrates slow heartbeat before the next screen loads (10–30s)
3. "How does it feel now?" → tap Better (30–50s)
4. "Recovery + reach out" → SMS template to your person (50–70s)
5. "Everything stayed on this phone" → privacy close (70–90s)

## REAL — build queue in order
| # | Feature | Status | Effort | Notes |
|---|---|---|---|---|
| R1 | ANCHOR NOW entry (236dp, haptic-first) | ✅ | — | HomeScreen, verified on-device |
| R2 | Haptic grounding (DOUBLE_PULSE + pulsing circle) | ✅ | — | SessionScreen, airplane-mode safe |
| R3 | Check-in Better/Same/Worse + retry loop + SAFETY_STOP | ✅ | — | Full FSM verified incl. WORSE path |
| R4 | SMS template (pre-written, via ACTION_SENDTO, no auto-send) | 🔨 | ~2–3h | Trusted-contact number optional; template preview |
| R5 | Privacy close (end card: on-device, no upload, not medical care) | 🔨 | ~1h | Static screen, zero logic |
| R6 | Find Support directory: 8 India helplines + regional routing | 🔨 | ~8h (lite manual-picker ~2h fallback) | Spec docs/india-resources.md + near-you-research.md; phone-first, offline |
| R7 | Maps button (`geo:0,0?q=`, 3 presets, online-badged) | 🔨 | ~30min | No permission needed |
| R8 | Communities block (r/ptsd, r/CPTSD, 7 Cups, TheMindClan, Sangath) with caveats | 🔨 | ~1h | Links + warning labels, online-badged |
| R9 | Safety plan screen (Stanley-Brown 6 sections, form UI) | 🔨 | ~3–4h | Model + validator built; display-only, no auto-actions |
| R10 | Insight card ("Breathing helped 4 of 5") | 🔨 | ~1–2h | Scorer built; one card on recovery screen |
| R11 | Seeded demo profile (skip onboarding UI) | 🔨 | ~30min | Hardcoded realistic profile; full S0–S12 flow only if R4–R10 done |

## FAKE — pitch cards (labeled "Preview", tap → "Coming soon" toast)
| # | Card | One-line pitch copy |
|---|---|---|
| F1 | Custom routines | "Your panic button, your sequence — reorder, retime, retune." (model built) |
| F2 | Wife's voice | "Record the voice that calms you. Yours, theirs, anyone's." (guardrails spec'd) |
| F3 | Exercise library | "Grounding, breathing, PMR, sleep — 20+ guided exercises, all offline." (protocols sourced) |
| F4 | Volume clutch | "Hold a volume button, or tap a home-screen tile — prototype working, screen-on only." (widget + 600ms long-press built; deep-sleep bypass NOT solved) |
| F5 | Learn module | "Understand PTSD without the textbook. 5-minute reads." |
| F6 | Progress graphs | "Watch what helps, over weeks. Your data never leaves." |
| F7 | Multilingual | "Hindi, Tamil, Telugu, Bengali, Kannada. PTSD speaks every language." |
| F8 | Community inside | "r/CPTSD-scale peer support, moderated, stigma-safe." |

## Do NOT build for demo
Onboarding full flow (unless R4–R10 done), routine builder UI, voice
recording/playback, brown noise, volume clutch, Learn content, graphs,
backend anything, accounts anything, location tracking.

## Honesty rules for the pitch
- Fake cards say Preview and do nothing but toast. Never demo a fake as real.
- Every clinical claim matches `docs/exercise-evidence.md` novelty order.
- Crisis numbers in R6 must be re-verified before the demo build.

---

# SDK-machine verification (Claude Code, 2026-09-05)

Checked every claim against repo HEAD + a real device (Samsung SM-A256E,
Android 16). Plan is broadly feasible. R1–R3 ✅ are true — verified on real
hardware this session, not just the emulator. Below is what doesn't hold.

## Blocking honesty problems (fix before the pitch, not after)

**H1 — F4's card copy contradicts our own research.** "Triple-press in your
pocket, even locked. Solved in open source." fails three ways:
`docs/clutch-research.md` says the locked/deep-sleep half is NOT solved
(Nugon ships that hack commented out) and that the Dictate claim is
debunked; the actual mechanism is a **1.5s long-press, not a triple-press**
(vision.md non-negotiable #2 also says triple-press is deferred); and
"solved in open source" is the exact overclaim clutch-research tells the
pitch not to make. Also outdated: the clutch is **partly real now** — the
home-screen widget trigger is built and verified end-to-end, the volume
long-press is built but unverified on hardware. Rewrite as e.g. "Hold a
volume button, or tap a home-screen tile — prototype working, screen-on
only." Or demo the widget for real and drop the card.

**H2 — "offline zero-permission ★5" is no longer literally true.** The
manifest now declares `android.permission.CAMERA` (from the grounding
prototype I built), and R6 would add `ACCESS_COARSE_LOCATION`. A judge who
opens App Info sees permissions. Pick one: rephrase to "no permissions for
the core flow, nothing leaves the device", or ship the demo build with the
grounding feature stripped (it's off-path anyway — also drops the APK from
59MB to ~5MB, since ML Kit's bundled model is 54MB of that).

**H3 — R10's insight card would be a fake shown as real.** Verified:
`SessionScreen` records **no** `SessionOutcome`, and nothing in the app
persists anything (`InMemoryEpisodeStore` only; no DataStore, no
SharedPreferences, no file writes). So "Breathing helped 4 of 5" can only
come from hardcoded numbers today — which breaks this doc's own "never demo
a fake as real" rule. Either label it Preview like F1–F8, or budget the real
path: record outcomes in `SessionScreen` (~1h) + DataStore persistence
(~2–3h) on top of the listed ~1–2h.

**H4 — the script's "slow heartbeat" wording violates an in-code rule.**
`HapticPatterns.kt` states explicitly: "Do not name or describe a pattern in
a way that implies" a physiological signal — and vision.md #1 says no
biometric claims. Say "a slow pulse". (`SLOW_PULSE` also already exists and
fits that description better than `DOUBLE_PULSE`, if you want the feel to
match the words.)

## Build-state corrections

**C1 — dissociation routing (★5, our #2 novelty) has zero user-facing UI.**
`InterventionCatalog`/`InterventionRouter`/`SafetyFilter` are built and
tested but referenced only from `devtools/RoutingLabScreen`. If a judge asks
to see it, there is nothing in the real app to show. Cheapest fix: wire the
catalog into the `INTERVENTION` state so it renders a *named* exercise
(~2–3h). Not in the current queue — recommend adding it above R6.

**C2 — "About the same" is a demo landmine.** R3's FSM is genuinely ✅, but
because of C1 the SAME → ROUTING → INTERVENTION path re-renders an identical
generic pulse. A judge who taps "About the same" sees the same screen twice
and concludes nothing happened. Either fix C1, or keep the walkthrough on
the Better path and be ready to explain.

**C3 — R9 without persistence is a prop.** The Stanley-Brown form UI (~3–4h)
is accurate as an estimate, but anything typed vanishes on restart until
DataStore exists. Fine if seeded and framed as display-only; not fine if
pitched as "your plan is saved."

## Effort check
R4 ~2–3h ✔ · R5 ~1h ✔ (likely 45min) · R7 ~30min ✔ · R8 ~1h ✔ ·
R11 ~30min ✔ · R9 ~3–4h ✔ *for UI only* (see C3) · R10 see H3 ·
**R6 ~8h is the schedule risk** — it is PLUS-tier but larger than the entire
MUST tier (R4+R5 ≈ 3–4h). Recommend the ~2h manual-picker lite by default;
the India directory is the differentiator, GPS auto-detection is not.

Suggested order: R4 → R5 → R7 → R8 → **C1** → R11 → R6-lite → R9 → R10.

## Demo-day ops (zero dev hours, all learned the hard way today)
- **Install on the demo phone via USB days early and leave it installed.**
  Sending the APK over WhatsApp failed today with "App not installed";
  `adb install` over USB worked first try. Do not transfer the APK on the day.
- **Put the phone in airplane mode as a visible step 0.** Our #1 novelty is
  offline and the script never proves it. Free, and it's the strongest
  moment in the 90 seconds. R7/R8 are online-badged by design — that
  contrast ("everything else works with the radio off") is a feature.
- **Pre-warm the app** right before presenting; cold start on a 59MB debug
  build is slower than the <300ms haptic claim will look.
- **Clear or dummy the Messages app** before R4 — `ACTION_SENDTO` leaves
  Anchor and shows the real SMS app, including whatever threads are in it.
- Samsung/One UI is on dontkillmyapp's aggressive list — if the volume
  trigger is ever demoed, set Anchor's battery to Unrestricted first.
