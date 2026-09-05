# Anchor Pitch Script — talking points per slide (branch: unified)

> Deck: `/home/prajwal-k/Projects/hack26/Anchor-Hogrithm-Deck.pptx` (20 slides,
> built by `/home/prajwal-k/Projects/hack26/build_deck.py` from the prescribed
> template — cover + closer untouched, content order preserved).
> Timing: 5-minute version below (~17s/slide). Lines marked ▼ can be dropped
> for a 3-minute version; lines marked ★ are the must-land moments.

## S1 Cover — (no talk, title card)

## S2 Anchor / Hogrithm (~15s)
- "We're Hogrithm. We built Anchor — the panic button for PTSD."
- ★ "Active help that fires during the episode, not a library you open after."

## S3 Problem Track (~10s)
- "Track 5, Inclusive Innovation. We chose PTSD: timely support with empathy,
  dignity, privacy — help people feel safe, not judged."

## S4 Problem statement (~25s)
- Read the sentence verbatim — slowly.
- ★ "Only 22.8% of those who need care in LMICs seek it. India's treatment gap
  is ~80%. Phones are everywhere; therapists are not."

## S5 Problem, cont. (~25s)
- "The best free tool, VA's PTSD Coach: retention 87% → 0.69% in a year."
- "Pooled symptom effect: −0.19, not significant. Android users get 6× the
  glitches. Half of one trial's users reported a negative reaction."
- ★ "No India version. No trigger. No human bridge. That's the gap we fill."

## S6 Solution (~20s)
- Read the one-liner.
- Walk a–d in one breath each: entry, grounding-to-data, ranked exercises,
  human bridge.

## S7 Solution, cont. — use cases (~25s)
- ▼ Pick TWO that fit the room: live demo the volume trigger if allowed,
  else narrate mid-panic at work + family check-in.
- ★ End on: "finding a clinic or calling Tele MANAS in one tap."

## S8 Architecture (~20s)
- "One native app, deliberately backendless. Compose UI, pure-Kotlin domain,
  on-device data, zero network calls."
- ★ "Privacy isn't our policy — it's our architecture."

## S9 Architecture, cont. — flow (~25s)
- Walk the 4 steps with fingers: SOS → sprint → steady/journal/ranked →
  timeout branch + companion alert.
- "Then Tools, Trends, and humans. If time: run the 90-second live demo here."

## S10 Tech stack (~20s)
- "Kotlin + Compose + native Android + on-device DataStore + intents."
- One line each; land on: "each choice removes a server, a permission, or a week."

## S11 Stack, cont. — why-nots (~15s)
- ▼ Drop if short on time. Else: "No Flutter — hardware APIs. No backend —
  connectivity fails exactly when users need us. Android-first because the
  evidence says Android users are the underserved ones."

## S12 Scale in India (~20s)
- "₹0 per user. Scale is states and languages, not servers."
- "Native rebuilds, never translations — a published Egypt adaptation failed
  on literal translation, so Hindi gets rebuilt with clinicians."

## S13 Scale, cont. (~15s)
- Challenges → one-line answers each. Land: "demo built and pushed; risk is
  validation, and we say so."

## S14 USP (~25s)
- ★ Count all five on fingers: active SOS, dissociation routing, reranking
  data, offline zero-permission, India-ready.
- "Market: survivors, students, professionals — plus families, who buy
  companion mode, then institutions."

## S15 GTM (~20s)
- "The VA playbook, Indian edition: directory, then pilot, then data."
- "Colleges, EAPs, family doctors. Exam season, disaster windows."

## S16 Economics (~20s)
- "₹0 to acquire, ₹0 to serve. ~₹3L institutional tickets; 8–10 cover a
  lean team. Social ROI first, financial second."

## S17 SWOT (~20s)
- Read the grid fast; land on: "threats each carry a mitigation — no medical
  claims, safety filter, confirm-gates, warnings."

## S18 Roadmap (~20s)
- "Next: reminders, graphs, Hindi, pilot. 1 year: published LMIC feasibility
  study — the paper the literature keeps requesting."

## S19 Learnings (~20s)
- ★ "Consent is a feature. Never cheap-localize. Sixty seconds to first
  value. And if pilots stall, sell to families first."
- Close: "Anchor — help that fires during the episode."

## S20 Closer — (thank-you card, Q&A)

## Q&A prep (likely questions)
- "Does it work?" → Honest: no RCT yet; Bröcker pooled −0.19 n.s. is why we
  claim engagement/routing/safety, never symptom reduction.
- "Why not iOS?" → Owen 2015: Android underserved; India is Android.
- "Auto-SMS privacy?" → Opt-in Companion Mode = explicit consent; silent path
  banned; pitch names SEND_SMS + location (see vision.md lock 7).
- "Business model?" → Free core; institutional licenses; 8–10 tickets break even.
- "Clinical risk?" → Safety filter vetoes, trauma warnings, difficulty ratings,
  professional-help handoffs; Hensler 51% negative-reaction stat is why.
