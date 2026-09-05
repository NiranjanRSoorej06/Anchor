# Anchor Gap Analysis

## Important caveat on method

This audit was conducted entirely inside an isolated research folder (`/home/ash/ptsd-study`) containing only the PTSD Coach APK, per the task's scope — the Anchor codebase itself was not opened, read, or inspected during this session. The categorization below is therefore based on the **named architecture given as project context** (SessionStateMachine, SafetyFilter, InterventionRouter, InterventionCatalog, haptic breathing, CameraX grounding, ML Kit grounding, PCL-5, SafetyPlan, SupportDirectory, PersonalizationScorer, SessionOutcome, existing tests) rather than direct source inspection. Every row's bucket assignment is a reasoned inference from that context, not a verified fact. **Before acting on this table, run a short verification pass against the actual Anchor repository** (grep for the named classes/modules, check their call sites and test coverage) to confirm or correct each row — the categories and reasoning here are the useful deliverable; the exact bucket per row should be double-checked against ground truth.

---

## ALREADY BUILT + WIRED
*(Per project context, these exist and are presumably reachable in the live app today. Do not rebuild — improve/extend only.)*

| Anchor capability | PTSD Coach equivalent found | Note |
|---|---|---|
| Haptic breathing pacer | Deep Breathing (audio-only, static visual) | Anchor's version is architecturally *ahead* of the reference app (interactive haptic vs. passive audio track) — this is a case where Anchor should feel confident it doesn't need to imitate the reference app; if anything, extend with the reference app's caption-for-accessibility idea. |
| CameraX grounding | Grounding tool (static card deck) | Same pattern — Anchor's version is likely stronger (active/interactive vs. static text). Extend with content variety (more grounding prompt types) rather than rebuild the mechanism. |
| ML Kit grounding | (same as above) | See above. |
| PCL-5 assessment | Track PTSD Symptoms (20-item, 5-point Likert, confirmed structurally identical) | Anchor already has the validated instrument — extend UI (history graph, retake flow) to match the reference app's presentation quality; critically, verify Module 13's "always-on safety footer" is present on Anchor's PCL-5 results screen, since this was not fully confirmed present in the reference app's full-length assessment (only confirmed on its brief screener). |
| SafetyPlan | Safety Plan (Stanley-Brown 6-step model, confirmed structurally) | Anchor already has this; verify step order/names match the canonical 6-step model exactly (see 03_CLINICAL_TECHNIQUES.md and Module 12), and extend with export/share + persistent crisis-button-on-every-step if not already present. |
| SafetyFilter | Crisis Resources (canonical hub screen) | Anchor already has a safety-filtering concept; extend its resource set to be India-relevant (see Module 11) and verify it is reachable in ≤2 taps from everywhere, matching the reference app's multi-entry-point pattern. |
| InterventionRouter | Symptom-first tool routing | Anchor already has a router; extend with the reference app's symptom-taxonomy idea (Module 03) and wire it to PersonalizationScorer output once that exists. |
| InterventionCatalog | Tools catalog (25 tools) | Anchor already has a catalog structure; this audit's 25-tool list (02_COMPLETE_FEATURE_DATABASE.md) is a content/coverage checklist against it, not a reason to rebuild the catalog mechanism. |
| SessionStateMachine | The Distress-Meter-wrapped tool-use flow | Anchor's state machine is the natural host for Module 01's pre/post-rating states — extend its states, don't build a parallel flow controller. |

## BUILT + TESTED + NOT WIRED
*(Per project context, these are named as existing but their reachability in the live product wasn't independently verifiable from this research folder. These should usually be prioritized for wiring, per task instructions.)*

| Anchor capability | Why it's flagged here | Recommended action |
|---|---|---|
| SessionOutcome | Named in project context as existing, and its use case (Module 01's pre/post distress capture, Module 02's effectiveness scoring) is exactly what the reference app's Distress Meter + "Tools That Have Helped" pattern demonstrates is valuable — but whether it is currently invoked from every tool-open flow, and whether its output currently drives any UI, could not be confirmed from this research folder. | Verify SessionOutcome is actually called at tool-open and tool-close for every InterventionCatalog entry; if it collects data but nothing reads it back, wiring a "Tools That Have Helped"-equivalent UI (Module 02) is the single highest-leverage gap-closing action from this whole audit. |
| PersonalizationScorer | Same reasoning — named as existing, and the reference app independently validates the entire concept (auto-tracked tool effectiveness, separate from manual favorites) works and is discoverable. | Verify it consumes SessionOutcome data and that its output actually influences InterventionRouter's suggestion order; if not wired end-to-end, this is the second-highest-leverage gap-closing action. |
| SupportDirectory | Named as existing; the reference app's "Find Professional Care" (veteran/civilian branch, provider-search links) is a plausible analog. | Verify it's populated with India-relevant provider-search resources (not placeholder/US-only data) and reachable from both a dedicated Support section and inline from the Crisis Resources hub, matching the reference app's multi-entry-point pattern. |
| Existing tests (unspecified scope) | Named as existing, but coverage against the newly-identified modules in this audit (Trigger Reset, Structured Emotion Journal, Coping Cards, Quick Access routing, Puzzle System) is obviously not yet possible since those modules don't exist yet. | Not a "wire it" gap so much as a reminder: extend the existing test suite alongside each new module in 05_ANCHOR_MODULES.md and 06_ANCHOR_PUZZLE_SYSTEM.md rather than treating tests as an afterthought — each module's Tests section above should become real test files as it's implemented. |

## PARTIALLY BUILT

| Anchor capability | What's missing, specifically |
|---|---|
| InterventionRouter (symptom-first routing) | Per project context it exists, but the reference app's specific symptom taxonomy (7 named categories: sleep, anxiety, low mood, social disconnection, dissociation, avoidance, trauma-reminders) and its "single suggestion + reroll" interaction pattern (Module 03) may not be present yet — this is a content/UX gap on top of an existing routing mechanism, not a missing mechanism. |
| SafetyFilter's resource set | The *mechanism* likely exists (per ALREADY BUILT above), but whether its actual hotline/resource content is India-localized (KIRAN, iCall, Vandrevala Foundation, Snehi, 112) versus generic/placeholder could not be verified from this research folder. Treat the underlying filter logic as built, the resource content as a data gap to fill (Module 11). |
| SafetyPlan step structure | Likely built with *some* 6-ish-step structure per project context, but exact parity with the canonical Stanley-Brown order/names (Module 12), plus export/share and persistent-crisis-button-per-step, needs verification and likely extension. |

## COMPLETELY MISSING
*(No named Anchor equivalent in the provided project context; genuinely new modules.)*

| Missing capability | Reference module | Priority pointer |
|---|---|---|
| Guided-session content library beyond breathing (Body Scan, PMR, Mindfulness family, Guided Imagery, ACT Defusion) | Module 04 | P1 — reuses existing breathing infra as the delivery mechanism, "just" needs content + a shared player |
| Trigger Reset Protocol (Relax→Ground/Differentiate→Decide) | Module 05 | P0 — no named Anchor equivalent; directly powers FLASHBACK quick-access |
| Structured Emotion Journal (My Feelings equivalent) | Module 06 | P1 — no named Anchor equivalent |
| Structured Coping Plan (Coping Cards equivalent) | Module 07 | P1 — no named Anchor equivalent |
| Strengths Journal | Module 08 | P2 |
| Free Journal | Module 09 | P2 |
| Custom Coping Tool Builder (user-authored multimedia tools) | Module 10 | P2 — high differentiation, no named equivalent |
| Assessment Safety Footer as a structurally-enforced shared component | Module 13 | P0 — cheap, easy to retrofit onto the existing PCL-5 result screen, high safety value |
| In-app Support Contacts (independent of OS Contacts) | Module 14 | P1 — SupportDirectory (provider search) is a different thing from a personal trusted-contacts list; no named equivalent for the latter |
| Sleep Toolkit | Module 15 | P2 |
| Worry Time | Module 16 | P2 |
| Bundled Ambient Sounds / Soothing Media library | Module 17 | P2 — avoids the reference app's own cold-start problem (its music feature is empty by default) |
| Onboarding & light personalization intake | Module 18 | P1 |
| Global Quick Access System (PANIC/FLASHBACK/etc. routing sheet) | Quick Access System (in 05_ANCHOR_MODULES.md) | P0 — the highest-leverage UX addition in this whole audit; a thin routing layer over existing/planned components, not a new architecture |
| Puzzle / attention-shifting activities | 06_ANCHOR_PUZZLE_SYSTEM.md | P2/P3 — genuinely absent from the reference app too; original Anchor addition |

## Cross-cutting note on "preserve existing functionality"

Nothing in this audit suggests replacing any of the ALREADY BUILT + WIRED items. Every recommendation above is additive (new modules, new content, new resource data) or extends an existing mechanism's reach (wiring SessionOutcome/PersonalizationScorer output into visible UI, extending InterventionRouter's taxonomy, extending SafetyPlan's step content) rather than swapping out working Anchor architecture for something PTSD-Coach-shaped. Where the reference app and Anchor implement conceptually the same idea (breathing, grounding), the audit's conclusion in every case was that Anchor's existing implementation is architecturally equal or ahead, and PTSD Coach's version should inform *content/copy/safety-framing* additions at most, never a mechanism swap.
