# Implementation Roadmap

Ranks every module from 05_ANCHOR_MODULES.md and 06_ANCHOR_PUZZLE_SYSTEM.md. "Existing Anchor code" reuse assumptions carry the same caveat as 07_ANCHOR_GAP_ANALYSIS.md — verify against actual source before committing to the reuse estimate.

---

## P0 — Demo Critical

### Module 11 — Crisis Resources Hub (India-localized)
- **Purpose:** One-tap, zero-friction safety surface reachable from anywhere.
- **Clinical technique:** Crisis-line access.
- **Evidence:** General/strong consensus support for frictionless crisis-line access as a suicide-prevention design requirement.
- **Existing Anchor code:** SafetyFilter (mechanism likely present — needs India-relevant resource data).
- **New UI:** Resource-list screen, global app-bar crisis icon.
- **New logic:** Configurable resource list (not hardcoded), one-tap dial/SMS/chat intents.
- **Tests:** Zero-extra-dialog intent firing; ≤2-tap reachability from every screen (navigation audit test).
- **Complexity:** Low.
- **Expected demo value:** Very high — this is the screen that makes the whole app defensible as a safety-conscious product; demo-able in under a minute and immediately legible to any reviewer.

### Module 13 — Assessment Safety Footer
- **Purpose:** Unconditional crisis-resources block on every assessment result screen.
- **Clinical technique:** Universal safety-net framing.
- **Evidence:** Consistent with general clinical guidance that screening tools should pair with accessible next-step/crisis info regardless of score.
- **Existing Anchor code:** PCL-5 result screen (retrofit).
- **New UI:** Shared footer component.
- **New logic:** Structural enforcement so no result screen can omit it.
- **Tests:** Footer renders at every possible score, including the minimum.
- **Complexity:** Low.
- **Expected demo value:** High relative to effort — a reviewer who checks "does this app handle a concerning assessment result responsibly" will find the answer immediately, for very little build cost.

### Module 12 — Safety Plan Extension
- **Purpose:** Bring existing SafetyPlan to full Stanley-Brown 6-step parity + export/share + persistent crisis button.
- **Clinical technique:** Stanley-Brown Safety Planning Intervention.
- **Evidence:** Solid, specific evidence base including reduced suicidal-behavior associations in clinical-use studies — one of the strongest-evidenced techniques found in the whole audit.
- **Existing Anchor code:** SafetyPlan (extend).
- **New UI:** Prev/Next step navigation, export/share screen.
- **New logic:** Step-order/content verification against canonical model; export generator.
- **Tests:** All 6 steps present in canonical order; no step blocks progression when empty; export produces a complete document.
- **Complexity:** Medium.
- **Expected demo value:** High — safety-plan quality is a natural comparison point against the reference app, and this module lets Anchor claim full parity plus two concrete improvements (export, always-visible crisis button).

### Module 01 — Distress Rating Capture
- **Purpose:** Pre/post 0–10 rating wrapper around tool use.
- **Clinical technique:** Self-monitoring / SUDS rating.
- **Evidence:** General support for routine self-monitoring in behavior change.
- **Existing Anchor code:** SessionStateMachine (extend), SessionOutcome (wire).
- **New UI:** Tap-to-set + stepper rating widget, outcome-delta feedback screen.
- **New logic:** Skip logic for time-critical entry points; delta computation.
- **Tests:** Skip never blocks quick-access; delta correctness for all sign cases.
- **Complexity:** Medium.
- **Expected demo value:** Medium standalone, but it's the prerequisite data layer for Module 02, which has very high demo value — build these two together.

### Module 05 — Trigger Reset Protocol
- **Purpose:** Structured post-trigger stabilization flow, RID-inspired with original branding and safety improvements.
- **Clinical technique:** Brief relaxation + grounding + present-vs-past differentiation + safety-aware decision-making.
- **Evidence:** Component techniques each have general clinical support (see 03_CLINICAL_TECHNIQUES.md); the packaged 3-step protocol itself is a self-help design pattern, not independently validated as a named instrument.
- **Existing Anchor code:** SafetyFilter (escape-hatch target), SessionStateMachine (host flow), existing haptic breathing (reuse for Relax step).
- **New UI:** 4-step flow, persistent safety-escape link.
- **New logic:** Optional (never blocking) field validation — an explicit improvement over the reference app's mandatory-field dialog.
- **Tests:** Every field independently skippable; safety-escape reachable from every step.
- **Complexity:** Medium.
- **Expected demo value:** Very high — this is Anchor's most distinctive, most clearly trauma-specific module, and it directly powers the FLASHBACK quick-access entry point, which is likely to be a headline feature in any demo.

### Quick Access System
- **Purpose:** Single-tap routing sheet (PANIC/FLASHBACK/HYPERVIGILANCE/DISSOCIATION/INTRUSIVE THOUGHTS/SLEEP/GROUND ME/BREATHE/FOCUS/SAFETY PLAN/GET SUPPORT).
- **Clinical technique:** N/A — a routing/UX layer over existing techniques.
- **Evidence:** N/A directly; reduces friction to evidence-supported techniques, which is itself a recognized design requirement for crisis-adjacent tools.
- **Existing Anchor code:** InterventionRouter, SafetyFilter (both as routing targets, not rebuilt).
- **New UI:** Persistent global entry icon + routing bottom sheet.
- **New logic:** Thin routing table (see 05_ANCHOR_MODULES.md).
- **Tests:** ≤2-tap reachability from every screen; correct routing per entry point.
- **Complexity:** Low–Medium (mostly UI + routing table; depends on how many destination modules already exist by the time this is built).
- **Expected demo value:** Extremely high — this single screen is the clearest, fastest way to demonstrate Anchor's "distress-first" design philosophy versus the reference app's flatter 2×2-grid Home screen, and it's cheap to build once its destination modules exist.

---

## P1 — High Value

### Module 02 — Personalized Tool Effectiveness Tracker
- **Purpose:** Auto-surface tools that have actually helped this user.
- **Clinical technique:** Measurement-based care applied to self-help tool selection.
- **Evidence:** General support for measurement-based care improving outcomes versus intuition-only selection.
- **Existing Anchor code:** PersonalizationScorer (build/extend), InterventionRouter (consumer).
- **New UI:** "Tools That Have Helped"-equivalent list.
- **New logic:** Effectiveness aggregation job, explicit user-override/opt-out.
- **Tests:** Score correctness over N sessions; safety-critical tools never gated by ranking.
- **Complexity:** Medium.
- **Expected demo value:** High — pairs with Module 01 to make a compelling "the app learns what works for you" demo narrative, directly validated by the reference app's own (independently discovered) implementation of the same idea.

### Module 03 — Symptom-First Intervention Router
- **Purpose:** Route by felt-state, not technique name.
- **Clinical technique:** Menu-based/individualized coping-skill matching.
- **Evidence:** General consensus support for the matching principle; the specific algorithm has no independent evidence base.
- **Existing Anchor code:** InterventionRouter (extend), InterventionCatalog (tag with symptomTags), SafetyFilter (escape-hatch target).
- **New UI:** Symptom-state picker, reroll UX.
- **New logic:** Symptom-to-tool mapping, PersonalizationScorer-aware ordering.
- **Tests:** Every symptom resolves to ≥1 valid tool with zero history; reroll doesn't immediately repeat.
- **Complexity:** Low–Medium.
- **Expected demo value:** Medium-high — a natural, relatable navigation model reviewers will recognize as an improvement over generic technique-name lists.

### Module 04 — Generic Guided Session Player
- **Purpose:** Shared player for all guided-practice tools.
- **Clinical technique:** Delivery mechanism for PMR, Mindfulness family, Guided Imagery, ACT Defusion (see 03_CLINICAL_TECHNIQUES.md).
- **Evidence:** Per-technique, generally solid for the underlying practices as adjunct coping skills.
- **Existing Anchor code:** Existing haptic breathing pacer (reuse as the breathing-family visual).
- **New UI:** Shared chrome + pluggable per-family visual slots.
- **New logic:** Resume-position persistence, safety-caveat rendering.
- **Tests:** Resume works; caveat renders when flagged.
- **Complexity:** Medium.
- **Expected demo value:** Medium standalone; high once populated with several techniques, since it demonstrates content breadth cheaply per additional technique.

### Module 06 — Structured Emotion Journal
- **Purpose:** Hierarchical feeling picker + intensity + notes, dated.
- **Clinical technique:** Affect labeling / emotional granularity.
- **Evidence:** General support for affect labeling reducing distress and for emotional granularity correlating with better regulation.
- **Existing Anchor code:** SessionOutcome (shared history surface).
- **New UI:** Step-indicated multi-step flow, mood history view.
- **New logic:** Safety-threshold nudge (Anchor improvement over reference app).
- **Tests:** Threshold nudge fires correctly; entry persistence/order.
- **Complexity:** Low–Medium.
- **Expected demo value:** High — visually engaging, clinically defensible, and the safety-threshold nudge is a concrete "we did this better than the reference app" talking point.

### Module 07 — Structured Coping Plan (Coping Cards)
- **Purpose:** Personal problem-area + feelings + coping-skills reference cards.
- **Clinical technique:** Coping-card / personal coping-plan authoring.
- **Evidence:** General clinical-practice support for written coping plans under reduced executive function.
- **Existing Anchor code:** InterventionCatalog (for optional tool-linking).
- **New UI:** Fixed-input chip-adder (reusable component).
- **New logic:** Tool-linking deep-links (improvement over reference app's plain-text tags).
- **Tests:** No layout shift on chip-add; tool-link opens correct tool.
- **Complexity:** Low.
- **Expected demo value:** Medium-high — quick to build, clearly useful, and the reusable chip-adder component pays for itself across Modules 06–08.

### Module 14 — In-App Support Contacts
- **Purpose:** Private, in-app trusted-contact list, no OS Contacts binding.
- **Clinical technique:** Social-support-network activation (access mechanism).
- **Evidence:** General support for social connectedness as a protective/coping factor; this module is infrastructure, not a technique itself.
- **Existing Anchor code:** None specific — becomes the canonical reference for Modules 07/11/12.
- **New UI:** Contact CRUD screen.
- **New logic:** Tap-to-call/text intents.
- **Tests:** No OS permission ever requested; consistent reference across modules.
- **Complexity:** Low.
- **Expected demo value:** Medium — a clear, easy-to-explain privacy win versus the reference app's OS-Contacts-permission approach.

### Module 18 — Onboarding & Personalization Intake
- **Purpose:** Fast, skippable first-run flow.
- **Clinical technique:** N/A (product onboarding).
- **Evidence:** N/A.
- **Existing Anchor code:** None specific.
- **New UI:** Short swipeable pager.
- **New logic:** Skip-from-anywhere.
- **Tests:** Skip lands on dashboard from any screen; crisis resources reachable mid-onboarding.
- **Complexity:** Low.
- **Expected demo value:** Medium — mostly a polish/trust-building item, necessary for a credible first impression but not a differentiator.

---

## P2 — Useful

### Module 08 — Strengths Journal
- **Complexity:** Low. **Demo value:** Low-medium (nice, not central).
- Reuses Module 06/07's chip/entry patterns; cheap add-on.

### Module 09 — Free Journal
- **Complexity:** Low. **Demo value:** Low (expected/table-stakes feature, not a differentiator).

### Module 10 — Custom Coping Tool Builder
- **Complexity:** Medium–High (media handling, rich-text editor).
- **Demo value:** High *if* time allows — genuinely distinctive personalization feature, but its build cost (5 content types, rich-text editor) makes it a poor fit for a tight timeline; a strong post-MVP feature.

### Module 15 — Sleep Toolkit
- **Complexity:** Low–Medium. **Demo value:** Medium — sleep is a very commonly cited PTSD symptom, so this resonates, but static content alone is a modest technical lift with modest differentiation.

### Module 16 — Worry Time
- **Complexity:** Low. **Demo value:** Low-medium.

### Module 17 — Ambient Sounds & Soothing Media Library
- **Complexity:** Low engineering / content-sourcing dependent. **Demo value:** Medium — pleasant, easy to show, but not clinically central; the real work is sourcing or producing original/licensed audio and imagery assets, which is a content-production task more than an engineering one.

### Puzzle System (06_ANCHOR_PUZZLE_SYSTEM.md)
- **Complexity:** Medium (several puzzle-type generators + accessibility work).
- **Demo value:** Medium — genuinely differentiating since the reference app has nothing like it, but must be framed carefully (optional grounding/attention-shifting, not treatment) to avoid overclaiming; good "innovation" talking point in a demo if framed correctly.

---

## P3 — Stretch

- **Additional guided-audio content breadth** (more Mindfulness-family tracks, more Positive-Imagery themes, more Relationship Tools) — extends Module 04's content library indefinitely; pure content production once the player exists, no new engineering.
- **Puzzle System difficulty/variety expansion** beyond the initial set (e.g., adding all 7 puzzle types at launch is P2; expanding each type's parameter space and adding an audio-based accessible variant for every visual type is P3 polish).
- **Export/share for Coping Cards / My Feelings history / Strengths Journal** (mirroring Module 12's Safety Plan export) — nice-to-have data portability, not required for MVP safety or clinical value.
- **Multi-language content** beyond whatever Anchor already targets — valuable long-term (the reference app itself only exposed a language *setting*, not verified multi-language content depth, in this audit) but a large content-production undertaking, not core to the P0–P1 clinical/safety feature set.

---

## Sequencing note

Build order should roughly follow: **Module 01 → Module 11 → Module 13 → Module 12 → Module 05 → Quick Access System → Module 02 → Module 03 → Module 04 (with Module 06/07 in parallel, since they share the chip-adder component) → Module 14 → Module 18 → everything in P2/P3 as capacity allows.** This ordering front-loads every safety-critical item (11, 13, 12, 05's embedded safety escalation) before any pure-personalization or pure-content module, consistent with treating safety as non-negotiable rather than a feature to schedule alongside others.
