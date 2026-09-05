# Onboarding UX Research Brief (2026-09-05)

Source: web research across SAMHSA/CDC trauma-informed guidelines, mental-health app teardowns (Headspace, Wysa, Noom, PTSD Coach), PCL-5 mobile UX literature, and accessibility guidance. Full URLs in §6.

## 1. Trauma-informed principles (SAMHSA 6, translated)
- **Safety:** no surprise disclosures, calm palette, persistent exit/cancel every screen.
- **Trustworthiness:** explain WHY each datum is asked, one sentence. "Nothing leaves your phone."
- **Collaboration:** user is co-pilot — every screen has Skip / Do-this-later. No dead-end forced flows.
- **Empowerment:** pacing control, no auto-advance timers, no guilt framing ("come back anytime", never "you're missing out").
- **Cultural:** no combat framing, trauma-type agnostic, inclusive language.
- **Exit-anytime:** closing mid-flow saves partial progress.

## 2. What top apps do (steal / avoid)
- **Headspace (steal):** value before ask — grounding sample FIRST, profiling after. For us: ANCHOR NOW works immediately, PCL-5 offered as "optional deeper setup."
- **Wysa (steal):** consent screen BEFORE any data collection, one sentence, every question skippable.
- **Noom (steal):** phase progress ("Part 1 of 3") not raw counts; micro-reveals at chunk boundaries ("shaping your toolkit…").
- **PTSD Coach (avoid):** forced PCL-5 → repeat reminders → crisis-line nagging → zero personalization payoff. Exactly our complaints.md #2.
- Supporting stats: 70–80% of users lost in first 3 days, mostly before first value (Biz4Group); progressive disclosure beats front-loading (Digia); 2–4 inputs per screen chunked thematically.

## 3. PCL-5 on mobile without dropout
- **4 cluster screens × 5 Likert rows**, not 20 screens. Header: "In the past month, how much have you been bothered by…"
- **Phase progress:** "Section 1 of 4: Intrusive thoughts" — never "Item 3 of 20."
- **Break after cluster 1** ("Take a breath. Ready?") + **distress safeguard at halfway** ("You can stop here, progress is saved" + grounding link).
- **Persistent Skip** per screen and overall; partial progress saved with "continue where you left off?"
- **Never display a diagnosis** — score stays internal, shown only as "this personalizes your tools."
- Budget: ~3 min straight-through, ~4–5 with breaks.

## 4. Accessibility for distressed users
- One thing per screen; max 2–4 choices; zero text input until safety phrase (optional).
- 48×48dp targets, 16dp+ spacing (unsteady hands).
- Reduced motion respected; de-saturated palette (no threat reds); 4.5:1 contrast; dark mode default.
- Persistent "Need a moment?" escape → 30s breathing → return to same spot.
- Back always works, same position, no dead-ends. Auto-save everything.

## 5. Recommended S0–S12 sequence (locked as target — see vision.md)
S0 Welcome (privacy-first, 5s) → S1 Consent ("Skip setup" jumps to S7) →
S2 PCL-5 intro ("check-in", 4 sections) → S3–S6 four cluster screens (each
independently skippable, safeguard at halfway) → S7 Situation picker
(multi-select chips) → S8 Sensory prefs (2 toggle groups + defaults) →
S9 Safety phrase (record or bundled fallback) → S10 Trusted contact
(phone + template preview) → S11 Safety plan intro ("now or in Settings";
defer to Settings for demo) → S12 Done reveal + ANCHOR NOW payoff.
Paths: full ~4–5 min, no-PCL-5 ~1 min, minimal ~30s, bare ~15s.

## 6. Sources
- SAMHSA trauma-informed: https://www.samhsa.gov/mental-health/trauma-violence/trauma-informed-approaches-programs
- CDC 6 principles: https://stacks.cdc.gov/view/cdc/56843
- Biz4Group mental-health design: https://www.biz4group.com/blog/best-practices-in-mental-health-design
- Digia onboarding drop-off: https://www.digia.tech/post/mobile-onboarding-ux-reduce-dropoff/
- Smashing distressed-user UX: https://www.smashingmagazine.com/2026/07/designing-distressed-users-mental-health-apps-ui/
- GapsyStudio MH design: https://gapsystudio.com/blog/mental-health-app-design/
- WCAG 2.2: https://www.w3.org/TR/WCAG22/
- PMC accessibility & digital MH: https://pmc.ncbi.nlm.nih.gov/articles/PMC8521906/
- Linardon 2026 dropout: https://onlinelibrary.wiley.com/doi/10.1002/wps.70043
- Blanchard et al. 1996 (PCL psychometrics): Behav Res Ther 34:669-73
