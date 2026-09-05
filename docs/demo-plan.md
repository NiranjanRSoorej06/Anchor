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
| F4 | Volume clutch | "Triple-press in your pocket, even locked. Solved in open source." (spike spec'd, Tier1) |
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
