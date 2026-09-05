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
| Market Opportunity | 5 | 3 | 4 | Model is honest pre-revenue hypotheses; +1 with any partnership outreach proof (even an email thread with Tele MANAS/NIMHANS/Vandrevala/iCall) |
| **Total** | **40** | **~30** | **~37–39** | |

## Highest-ROI actions (ranked)

1. **SDK build + screenshots + demo video** (≈ +5–6 pts across Prototype/UX).
   `git checkout unified && ./gradlew assembleDebug`, run VERIFY checklists,
   screencap each step of docs/demo-plan.md 90-sec script.
2. **One differentiation slide** (+1 Innovation): side-by-side "PTSD Coach
   (passive library) vs Anchor (active SOS)" + the three literature-cited
   gaps we close (Bröcker SMD −0.19; 87%→0.69% retention; 22.8% LMIC seeking).
3. **One partnership email thread** (+1 Market): write to Tele MANAS/NIMHANS/
   Vandrevala/iCall about directory listing + pilot; the thread itself is
   the artifact, even pre-reply.
4. **Arduino UNO Q (+2 bonus) — conditional**, see below.

## Arduino UNO Q verdict: chase ONLY with a hardware owner

- Facts: Qualcomm QRB2210 + STM32U585, Debian Linux, BLE 5.1 + WiFi 5,
  $59 (2GB, hiked from $44 July 2026) / $79 4GB; stocked in India via
  Robu.in (~₹5k). Datasheets + Arduino App Lab exist.
- Credible concept: BLE panic-button pendant/fob → phone app fires SOS
  (BLE GATT via CompanionDeviceManager). Bonus points for the on-board
  RGB "panic" LED channel mirroring alert state on demo table.
- Cost: ~₹5–6k + half a day of a hardware-capable teammate (Arduino sketch
  + Android BLE client + pairing debugging).
- Risk: BLE pairing failures during live demo directly threaten the
  10-point Prototype score to gain 2 bonus points (~5%). Bad trade without
  a hardware owner; good trade with one (fallback: pre-pair on video, live
  triple-tap as backup trigger).
- Decision rule: hardware teammate owns it end-to-end by T-6h or we cut it
  and keep the volume-trigger USP story (already hardware-adjacent, zero cost).
