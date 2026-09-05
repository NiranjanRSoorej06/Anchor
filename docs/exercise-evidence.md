# Exercise Evidence + Novelty Brief (2026-09-05)

What app-delivered PTSD exercises can claim, what to borrow, and what is
genuinely unique. Rule throughout: techniques are public domain, wording is
ours, claims stay inside the evidence.

## Technique evidence (honest version)
- **Grounding (5-4-3-2-1 etc.):** evidence-informed component inside evaluated
  multi-module interventions (Lewis et al. 2017, doi:10.1002/da.22645). NO
  RCT of grounding in isolation exists (Hammond & Brown 2025,
  doi:10.1177/15248380251343189). Say "used in evaluated interventions",
  never "stops flashbacks."
- **Paced breathing:** slow breathing (4–6 bpm) acutely raises HRV; NICE NG116
  recommends self-calming. Vagal-tone→PTSD-improvement chain NOT established
  (Fonkoue 2020: no PTSD symptom change). No exhale-specific PTSD RCT.
- **PMR/relaxation:** studied as comparator (Thorp 2019; Vera 2022,
  PMC9035035); VA/DoD 2023: insufficient evidence standalone. Our E005
  caveat string is exactly correct — keep it verbatim.
- **App bilateral stimulation:** first pilot RCT 2025, feasibility only
  (doi:10.34133/jemdr.0003). Do NOT build BLS features.
- **Phone-vibration calming:** zero RCTs for PTSD/anxiety (MacDonald 2024:
  social anxiety, no physiological effect, personalization required).
  Our `DELIVERY_METHOD_UNEVALUATED` tag stays.
- **Sleep hygiene tips alone:** weak. CBT-I proper is strongly evidenced —
  frame any sleep content as psychoeducation, not treatment.
- **Stanley-Brown SPI:** strong — OR 0.47 for suicidal behavior (Stanley
  2018, PubMed 29998307). Our screen is SPI-*informed* (contacts + routing),
  not SPI delivered; frame accordingly.
- **Personalization→outcomes:** weak/fragmented (Linardon 2025 r≈0.15–0.19;
  Hornstein 2023 no consensus). Tailoring improves engagement; claiming
  outcome gains would not survive review. Frame "personalization-informed."
- **Own-voice calming:** real fMRI self-referential effects (Kim 2024) +
  empathic-voice anxiety reduction (d≈0.5, non-PTSD samples). Extrapolation
  to PTSD untested — "informed by", never "proven."
- **Safe-place visualization (internally imagined, not media playback):**
  real technique, narrow evidence — Korn & Leeds 2002 (J. Clinical
  Psychology 58(12), 1465-1487, PubMed 12455016), EMDR resource-
  installation in complex-PTSD stabilization; pilot/case-series tier, not
  RCT. ADJUNCT_EVIDENCE. Ships as `visualization_monsoon`/`visualization_temple`
  guided scripts (Edit Anchor's "Safe-place visualization" comfort tool).
  **Critical distinction, confirmed against SAMHSA trauma-informed care and
  Hammond & Brown 2025 (already cited above): grounding literature never
  recommends watching curated photos/video as a technique** — only
  attending to the real environment (5-4-3-2-1) or imagining a safe place.
  A "soothing video library" would be EVIDENCE_GAP if ever built; we did
  not build one for exactly this reason.
- **User's own safe-place photo (optional layer on the visualization
  above):** more speculative — extrapolated from attachment/safety-cue
  neuroscience, not tested as an app feature. Coan, Schaefer & Davidson
  2006 (Psychological Science 17(12), 1032-1039): hand-holding from a
  partner blunted fMRI threat response. Selcuk et al. 2018 (Soc Cogn
  Affect Neurosci 13(9), 989-998, PubMed 30137625): mentally activating an
  attachment figure blocked fear-conditioning acquisition. Both studies
  manipulate touch or imagined presence, not a viewed photo — a real photo
  is a plausible but untested extension. CLINICAL_CAUTION_DERIVED. Frame
  as "may help," never "proven to calm."
- **Ambient white/pink noise masking:** real but mixed, mostly non-PTSD
  evidence (sleep continuity, pediatric procedural anxiety) — no PTSD-
  specific trial found. Brown noise specifically has the weakest evidence
  of the three (comfort/preference claims, not peer-reviewed PTSD trials)
  — this is why `E009`'s existing brown-noise entry stays EVIDENCE_GAP and
  why the new ambient-noise feature ships white/pink only, generated
  on-device (`core/audio/NoiseGenerator`), never brown. ADJUNCT_EVIDENCE.
  **Framing constraint: masks/reduces noticeability of ambient sound —
  never claim it "cancels" surrounding sound**, which is a hardware ANC
  capability no phone app controls.

## Borrow-safe (PTSD Coach-style, own words + evidence labels)
5-4-3-2-1 grounding, paced breathing scripts, PMR (Jacobson 1938, public
domain), sleep-hygiene education, coping statements (Beck 1979), DBT distress
tolerance skills (Linehan 1993, published manuals), Stanley-Brown 6-step
structure (open protocol, suicidesafetyplan.com), SUDS concept (adapted).
NEVER: verbatim exercise scripts, VA crisis routing, VA branding, forced
assessment flows, reminder nagging, veteran framing.

## Novelty ranking (pitch-safe order)
1. **Offline-first zero-permission PTSD tool** ★★★★★ — architectural claim,
   fully defensible. No competitor does this.
2. **Dissociation-gated routing** ★★★★★ — NICE-grounded, no app does
   differential routing. Novel implementation, not novel clinical claim.
3. **Outcome-tracked explainable reranking** ★★★★ — no PTSD app has it;
   mechanism novel, outcome gains unclaimed.
4. **Custom-voice grounding** ★★★★ — neuroscience-informed, untested in
   PTSD; frame carefully.
5. **Regional helpline routing (India-first)** ★★★★ — pure access win,
   fully defensible.
KILLS: "haptics proven", "vagal regulation", "BLS app", "personalization
improves outcomes", "custom voice reduces anxiety" as clinical claims.

## Pitch line
"Anchor is the first offline-first, zero-permission PTSD grounding tool
with dissociation-safe routing, evidence-traced interventions, and a
personalization loop that learns what helps you — no network, no account,
no trauma story."

## Key sources
- Lewis 2017: doi:10.1002/da.22645 · Hammond 2025: doi:10.1177/15248380251343189
- Thorp 2019: PubMed 30978622 · Vera 2022: PMC9035035
- Stanley 2018: PubMed 29998307 · Stanley 2012: Psycnet 2012-07473-004
- Bröcker 2023: PMC10215014 · NICE NG116: nice.org.uk/guidance/ng116
- VA/DoD 2023: healthquality.va.gov/guidelines/MH/ptsd/
- MacDonald 2024: doi:10.1145/3648615 · Kim 2024: doi:10.3390/brainsci14070637
- Heim 2021 (cultural adaptation): doi:10.1038/s41746-021-00498-1
- Mobile EMDR pilot 2025: doi:10.34133/jemdr.0003
