# hack26 Judging Gameplan (branch: unified)

Criteria total: 40 + 2 hardware bonus. NOTE: "Scalability & Technical
Feasibility" header says Max 10 but descriptors run 5/4/3/2 — confirm with
organizers whether it is /10 (doubled?) or a /5 typo. Plan below assumes /10
with the 5-scale doubled; if /5, our story still maxes it.

## Score estimate (now → achievable)

| Criterion | Max | Now | Achievable | Closes the gap |
|---|---|---|---|---|
| Problem Relevance & Impact | 5 | 5 | 5 | Cited LMIC gap (22.8% seeking), retention cliff, HIC skew — done |
| Innovation & Creativity | 5 | 4 | 5 | Differentiation narrative: active-vs-passive, triple-tap, dissociation routing, reranking-as-research-asset. Risk: "resembles PTSD Coach" — narrate explicitly on one slide |
| Prototype / Technical Execution | 10 | 6 | 8–10 | SDK APK + screenshots + 90-sec demo video. Biggest swing on the board |
| Scalability & Feasibility | 10 | 8 | 8–10 | Backendless = ₹0 marginal cost; clarify the /10-vs-/5 ambiguity |
| UX & Design | 5 | 4 | 5 | M3 + 3 themes + civilian-first + warnings exist; needs on-device screenshots + contrast/accessibility note |
| Market Opportunity | 5 | 3 | 3–4 | Honest pre-revenue hypotheses + cost realism; no outreach theater |
| **Total** | **40** | **~30** | **~36–38** | |

## Highest-ROI actions (ranked)

1. **SDK build + screenshots** (≈ +4–5 pts across Prototype/UX).
   `git checkout unified && ./gradlew assembleDebug`, run VERIFY checklists,
   screencap each step of docs/demo-plan.md 90-sec script. Q3 mandates a live
   demo or screenshots — this is the single non-negotiable artifact.
2. **One differentiation slide** (+1 Innovation): side-by-side "PTSD Coach
   (passive library) vs Anchor (active SOS)" + the three literature-cited
   gaps we close (Bröcker SMD −0.19; 87%→0.69% retention; 22.8% LMIC seeking).
3. ~~Partnership email thread~~ — CUT per team. Market stays carried by cost
   realism + institutional path narrative (score ceiling 3–4).

## Arduino UNO Q verdict: CUT (team decision — no hardware available)

Bonus foregone (+2). Volume-trigger USP story carries the hardware-adjacent
narrative at zero cost. No further action.
