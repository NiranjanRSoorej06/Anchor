# Anchor Modules

Each module below is independently implementable. "Existing Anchor Components" names what should be reused/extended (per Phase 14 — never rebuild a working Anchor feature). Module numbering is for reference only, not priority order (see 08_IMPLEMENTATION_ROADMAP.md for priority).

---

# MODULE 01 — Distress Rating Capture

## Goal
Capture a lightweight 0–10 self-reported distress rating immediately before and after any coping-tool session, without becoming a barrier to fast access during acute distress.

## User Problem
Users don't have an easy way to notice, in the moment, whether a given technique actually helped them — and neither does the app, without this data.

## Target PTSD State
Any active distress episode.

## Trigger
Opening any intervention from InterventionCatalog; can be explicitly skipped or bypassed for time-critical entry points.

## User Flow
1. User opens a tool.
2. If the entry point is "exploratory" (browsed from a catalog/symptom list), show a skippable 0–10 rating screen before the tool starts.
3. If the entry point is "emergency" (PANIC/FLASHBACK quick-access), skip straight to the tool; rating is offered only afterward, optionally.
4. After the tool session ends, show the same 0–10 widget again (not skippable if a pre-rating was captured, since the delta is the useful signal).
5. Show a brief reinforcement message reflecting the delta (decreased/same/increased) with quick actions: Add to Favorites / Use Again / Try Another.

## UI
Vertical 0–10 scale, both tap-to-set-directly and up/down stepper affordances (fixing the reference app's arrow-only, jump-to-max-on-first-tap issue). Primary continue button enabled only once a value is set (pre); Skip always available (pre only, never on time-critical entry points).

## Clinical Technique
Self-monitoring / SUDS-style (Subjective Units of Distress) rating, standard in CBT practice for tracking within-session change.

## Evidence
General support for the value of routine self-monitoring in behavior change and symptom tracking; SUDS-style ratings are a long-standing, low-cost clinical practice tool.

## Safety Rules
Never block access to a PANIC/FLASHBACK/crisis entry point behind a rating screen. Never treat a single high rating as an automatic crisis escalation on its own — pair with SafetyFilter thresholds defined in Module 11.

## Data Model
```
DistressRating {
  id, sessionId, timing: "pre"|"post", value: 0-10, capturedAt: timestamp
}
InterventionSession {
  id, toolId, startedAt, endedAt, entryPoint: "catalog"|"symptom"|"quick_access"|"favorite",
  preRatingId?, postRatingId?
}
```

## State Machine
`NotStarted → PreRatingShown (skippable) → ToolActive → PostRatingShown (skippable if no pre) → OutcomeFeedbackShown → Closed`

## Offline Behavior
Fully offline; all data local.

## Existing Anchor Components
SessionStateMachine (extend with rating states), SessionOutcome (this module *is* largely SessionOutcome's capture mechanism — wire, don't rebuild).

## New Components
Distress rating widget (tap-to-set + stepper), outcome-delta reinforcement screen.

## Tests
- Pre-rating skip does not block tool start.
- Post-rating with no pre-rating does not force a value (or is itself skippable).
- PANIC/FLASHBACK entry points never show pre-rating.
- Delta computation correct for all cases (decrease/same/increase, including missing pre or post).

## Acceptance Criteria
User can complete a full tool session with zero taps beyond Skip/Skip if they choose; distress delta is correctly recorded when both ratings are given; reinforcement message matches the actual delta sign.

## Complexity
Medium (state machine + UI widget + wiring across every tool entry point).

## Priority
P0 — this is the data-capture backbone for Module 02 and for Anchor's own SessionOutcome ambitions.

---

# MODULE 02 — Personalized Tool Effectiveness Tracker

## Goal
Automatically surface which specific tools have actually reduced this user's distress in the past, separate from manually "favorited" tools.

## User Problem
Users forget what worked for them last time, especially under stress when recall is impaired.

## Target PTSD State
Any recurring distress pattern.

## Trigger
Passive — computed from accumulated Module 01 data; surfaced whenever the user views Favorites/Quick Access.

## User Flow
1. Every completed session with both a pre- and post-rating contributes a signed delta to that tool's running effectiveness score.
2. A "Tools That Have Helped" list (separate from manually-favorited tools) ranks tools by aggregated effectiveness (e.g., average delta, weighted toward recent sessions), each shown with a qualitative label ("Helps a lot" / "Helps somewhat" / etc.) or (Anchor improvement) a more precise numeric indicator.
3. This list can feed directly into InterventionRouter's default suggestion order for a given symptom.

## UI
A distinct section (not merged with manual Favorites) inside the tools/favorites screen; each row shows the tool name + effectiveness label; tapping opens the tool directly.

## Clinical Technique
Personalized/idiographic outcome tracking — not a named clinical technique itself, but a direct application of the self-monitoring data from Module 01 to individualize future tool selection (a form of measurement-based care).

## Evidence
Measurement-based care (using session-level self-report data to guide subsequent intervention choice) has general support for improving outcomes versus intuition-only clinical decision-making; applying the same logic to self-help tool selection is a reasonable, low-risk extrapolation, not itself a separately validated technique.

## Safety Rules
Never let this list surface a tool the user has explicitly marked as unhelpful/negative even if an early data point was positive (respect an explicit "don't suggest this again" override). Never use this ranking to suppress or de-prioritize genuinely safety-critical tools (Safety Plan, Crisis Resources) — those must always remain independently and fully accessible regardless of their "effectiveness" score.

## Data Model
```
ToolEffectivenessScore {
  toolId, userId(local), sampleCount, averageDelta, lastUpdatedAt, label: computed band
}
```
Derived entirely from `InterventionSession` + `DistressRating` (Module 01); no separate write path needed beyond a recompute-on-session-close job.

## State Machine
Not stateful itself — a derived/materialized view recomputed after each session close.

## Offline Behavior
Fully offline; pure local computation.

## Existing Anchor Components
PersonalizationScorer (this module *is* PersonalizationScorer's core use case — build/extend here, not from scratch), InterventionRouter (consumes this ranking).

## New Components
Effectiveness aggregation job; "Tools That Have Helped" list UI; explicit override/opt-out affordance (improvement over reference app).

## Tests
- Score updates correctly after N sessions with known deltas.
- Explicit user override correctly suppresses a tool from future suggestions.
- Safety-critical tools are never gated behind this ranking.

## Acceptance Criteria
After 3+ completed sessions for a given tool, an effectiveness label appears and is directionally correct; InterventionRouter's default suggestion order reflects this data when available, falling back sensibly when it isn't (new user, no data yet).

## Complexity
Medium.

## Priority
P0 — highest-leverage personalization discovery from the whole audit.

---

# MODULE 03 — Symptom-First Intervention Router

## Goal
Let a user find help by naming what they feel rather than by knowing a technique's name.

## User Problem
A distressed person often doesn't know (or can't recall under stress) which named technique to use, but can usually name what they're feeling.

## Target PTSD State
All 7 reference-app symptom categories (adaptable/expandable): sleep difficulty, anxiety, low mood, social disconnection, dissociation, trigger-avoidance, trauma reminders.

## Trigger
User taps a symptom/feeling-state entry point.

## User Flow
1. User picks a felt-state (single or, as an Anchor improvement, multi-select for blended states).
2. Router selects a suggested tool using: PersonalizationScorer ranking (Module 02) if available → curated symptom-to-technique mapping → randomized fallback.
3. Tool opens (wrapped by Module 01 as appropriate for entry context).
4. A visible "different tool" action re-queries the router for an alternative.
5. A persistently visible "not safe right now?" link routes directly to Module 11 (Crisis/SafetyFilter).

## UI
Simple large-row list of felt-states with icons; single suggested tool shown at a time with a clear "try something else" action.

## Clinical Technique
Menu-based/individualized coping-skill matching — a common self-help-app design pattern rather than a specific named clinical protocol.

## Evidence
General support for the principle that offering a menu of coping strategies matched to presenting symptoms increases the odds of finding something that helps a given individual (consistent with broader "no single technique works for everyone" clinical consensus); the specific routing algorithm itself has no independent evidence base and should be understood as a UX/product design choice, refined over time by Module 02's real outcome data.

## Safety Rules
Always show the safety-escape-hatch link regardless of which symptom is selected. Never let randomization alone govern selection when good personalization data exists.

## Data Model
Reuses InterventionCatalog's existing tool metadata; add a `symptomTags: string[]` field per tool if not already present.

## State Machine
`SymptomSelected → ToolSuggested → (ToolOpened | RerollRequested | SafetyEscapeRequested)`

## Offline Behavior
Fully offline.

## Existing Anchor Components
InterventionRouter (this module is a direct extension of it), InterventionCatalog (tag with symptomTags), SafetyFilter (escape-hatch link target).

## New Components
Symptom picker UI; reroll UX.

## Tests
- Every symptom category resolves to at least one valid tool even with zero personalization data.
- Reroll never repeats the immediately-previous suggestion twice in a row for the same symptom.
- Safety escape-hatch is reachable from every symptom screen.

## Acceptance Criteria
A brand-new user with no history can pick any symptom and reach a working tool in ≤2 taps; a returning user with effectiveness history sees their historically-best tool suggested first for a matching symptom.

## Complexity
Low–Medium.

## Priority
P1.

---

# MODULE 04 — Generic Guided Session Player

## Goal
One reusable, well-designed audio/visual/haptic-guided session component for every "guided practice" style tool (breathing, body scan, PMR, mindfulness family, guided imagery, ACT defusion).

## User Problem
Ten+ different guided techniques shouldn't each need a bespoke player; users benefit from one consistent, well-understood control set.

## Target PTSD State
Hyperarousal, dissociation, rumination, intrusive thoughts (technique-dependent).

## Trigger
Opening any guided-practice tool.

## User Flow
1. Intro screen: technique description, expected duration, any safety caveat (e.g., don't do this while driving), Continue.
2. Player screen: play/pause, seek, elapsed/remaining time, captions (if audio-based), a technique-appropriate visual (not one generic static region — see UI notes), Add to Favorites, Done.
3. Done → Module 01's post-rating flow if a pre-rating was captured.

## UI
Shared chrome (seek bar, play/pause, captions, Done) with a pluggable "visual" slot per technique family: an interactive haptic-synced breathing shape for breathing exercises (Anchor already has this — reuse), a simple body-outline highlight for Body Scan, a plain minimal scene for imagery/defusion tracks. Resume-from-last-position on reopen (improvement over reference app).

## Clinical Technique
Varies per instance — see 03_CLINICAL_TECHNIQUES.md entries for Diaphragmatic Breathing, PMR, Mindfulness family, ACT Defusion, Guided Imagery.

## Evidence
See individual technique entries in 03_CLINICAL_TECHNIQUES.md; this module is a delivery mechanism, not a technique itself.

## Safety Rules
Any technique requiring stillness/eyes-closed/reduced alertness must carry an explicit "don't use while driving/operating machinery" caveat on its intro screen, matching the reference app's Muscle Relaxation warning.

## Data Model
```
GuidedSession { toolId, mediaRef, durationSec, captionsRef, visualType, safetyCaveat? }
```

## State Machine
`Intro → Playing ⇄ Paused → Completed → (Module01 post-rating)`

## Offline Behavior
All media bundled with the app; fully offline.

## Existing Anchor Components
Existing haptic breathing pacer (reuse directly as the breathing-family visual/interaction, do not replace with a passive audio track).

## New Components
Shared player chrome; caption renderer; per-family visual slots (body-outline, minimal-scene); resume-position persistence.

## Tests
- Playback resumes from last position on reopen.
- Safety caveat renders whenever the tool metadata flags one.
- Captions stay in sync across pause/resume/seek.

## Acceptance Criteria
Adding a new guided-practice tool requires only new media + metadata, no new player code.

## Complexity
Medium.

## Priority
P1.

---

# MODULE 05 — Trigger Reset Protocol (RID-inspired, original branding)

## Goal
A fast, structured, safety-aware protocol for the moment right after being triggered by a trauma reminder.

## User Problem
In the seconds after a trigger, a person needs something more structured than "just breathe" but faster and less demanding than a full journaling session.

## Target PTSD State
Trauma-reminder triggering, flashback-adjacent distress.

## Trigger
FLASHBACK quick-access entry point (Phase 11), or Symptom picker ("Reminded of Trauma"/"Avoiding Triggers").

## User Flow
1. **Relax** (~30 sec): on-screen countdown synced to a simple two-part breath cue (e.g., inhale-word / exhale-word), extendable in 30-second increments, always skippable forward.
2. **Ground & Differentiate**: brief psychoeducation (triggers can be internal or external; the goal is noticing "this is a reminder, not a recurrence"), then two OPTIONAL prompts — "What just happened?" and "How is right now different from the trauma?" — each individually skippable with a one-tap "I can't put it into words right now" (fixing the reference app's blocking-validation problem).
3. **Decide**: brief guidance on 3 response options (stay and tolerate / take a short break / get support now), with "get support now" as an actual button routing directly into Module 11 (SafetyFilter/Crisis Resources), not just descriptive text.
4. **Summary**: recap of whatever was entered (blank fields shown as "skipped"), saved with a timestamp.

## UI
Large single-focus screens, big tap targets, minimal choices per screen, a persistent small "get support now" link visible on every step (not only in the Decide step).

## Clinical Technique
Combines brief relaxation/paced breathing + grounding + present-vs-past differentiation + safety-aware decision-making — see the RID entry in 03_CLINICAL_TECHNIQUES.md for the evidence caveat: this is a packaged self-help protocol built from separately-evidenced components, not itself an independently validated instrument.

## Evidence
See above; individual components (breathing, grounding, present-orientation) each have general clinical support as described in 03_CLINICAL_TECHNIQUES.md.

## Safety Rules
The "get support now" path must be a real, always-visible, one-tap action, not just descriptive text the user has to remember to act on (this is an explicit improvement over the reference app's static safety sentence). Never block progression behind required text entry.

## Data Model
```
TriggerResetSession {
  id, startedAt, relaxExtendedSeconds, whatHappened?: string, howDifferent?: string,
  decision?: "stayed"|"tookBreak"|"gotSupport", completedAt
}
```

## State Machine
`Relax → GroundAndDifferentiate (each field optional) → Decide → Summary` with a side-channel `SafetyEscapeRequested` reachable from any state.

## Offline Behavior
Fully offline.

## Existing Anchor Components
SafetyFilter (the "get support now" target), SessionStateMachine (host this as one flow type), existing haptic breathing (reuse for the Relax step instead of a plain countdown, if feasible).

## New Components
The 4-step flow UI; optional-field journaling with a graceful skip affordance; persistent safety-escape link component (reusable elsewhere).

## Tests
- Every field is independently skippable without blocking a dialog.
- "Get support now" is reachable and functional from every step.
- Summary correctly displays "skipped" for any omitted field.

## Acceptance Criteria
A user can complete the entire flow in under 60 seconds using only the Relax/Decide steps and skipping both journaling prompts; a user who does journal has their answers correctly recapped.

## Complexity
Medium.

## Priority
P0 — directly powers the FLASHBACK quick-access entry point.

---

# MODULE 06 — Structured Emotion Journal ("My Feelings" equivalent)

## Goal
Let a user log a precise, granular emotional state with intensity and optional notes in under 90 seconds.

## User Problem
"I feel bad" is too vague to act on or track over time; naming the specific feeling and its intensity is itself regulating and produces useful longitudinal data.

## Target PTSD State
General mood/emotion tracking; useful across nearly every symptom category.

## Trigger
Manual open from Manage/Track equivalent; optionally auto-suggested after completing any coping session (Anchor improvement — capture the "how do you feel now" moment organically).

## User Flow
1. Select one or more primary feelings from a small fixed set (e.g., Angry/Joyful/Powerful/Sad/Safe/Scared, expandable).
2. For each selected primary, optionally drill into 8–12 more granular nested feelings.
3. Set an intensity value (default a coarse 5-point scale with an optional "more precision" 0–100 expand, per the UI-redesign recommendation).
4. Optional free-text notes.
5. Save → dated recap screen; entry appended to a viewable, chronological mood history.

## UI
Step-indicator visible throughout (fixing the reference app's missing-progress-indicator gap); tap-to-drill-down chip interaction preferred over full-screen checkbox lists where feasible.

## Clinical Technique
Affect labeling / emotional granularity.

## Evidence
General research support for affect labeling reducing amygdala reactivity/subjective distress, and for emotional granularity being associated with better regulation outcomes in the broader literature (see 03_CLINICAL_TECHNIQUES.md for full framing).

## Safety Rules
If a very high intensity is paired with certain feeling categories (e.g., high "Scared"/despair-adjacent nested feelings at high intensity), surface a gentle, non-alarming nudge toward SafetyFilter — this is an explicit improvement over the reference app, which does not appear to do this.

## Data Model
```
FeelingEntry {
  id, capturedAt, primaryFeelings: string[], nestedFeelings: string[],
  intensity: 0-100, intensityBand: string, notes?: string
}
```

## State Machine
`SelectPrimary → SelectNested → SetIntensity → Notes(optional) → Saved`

## Offline Behavior
Fully offline.

## Existing Anchor Components
SessionOutcome (this data feeds the same personalization/history surface).

## New Components
Feeling taxonomy (original wording, not copied); step-indicator UI; safety-threshold nudge; mood-history view.

## Tests
- Threshold nudge fires only above the defined intensity+category combination.
- Entry persists correctly and appears in history in chronological order.
- Flow completes with zero required fields beyond at least one primary feeling.

## Acceptance Criteria
A user can log a feeling entry in under 90 seconds; history view correctly shows entries in reverse-chronological order with intensity trend visible.

## Complexity
Low–Medium.

## Priority
P1 — high value, low clinical risk, highly portable.

---

# MODULE 07 — Structured Coping Plan ("Coping Cards" equivalent)

## Goal
Let a user author, when calm, a short personal reference card per problem area (feelings/symptoms + coping skills) to consult later when distressed.

## User Problem
Under stress, people forget strategies they know work — a pre-written personal reference removes the need to think of one from scratch.

## Target PTSD State
General; authored proactively, consulted reactively across any symptom.

## Trigger
Manual creation any time; consultation surfaced via InterventionRouter when relevant.

## User Flow
1. Pick/author a Problem Area (small preset set + custom).
2. Add Emotions/Symptoms as tags (type + add, chip rendered below a **fixed** input row — fixing the reference app's layout-shift hazard).
3. Add Coping Skills as tags, same pattern; optionally deep-link a tag to a matching tool in the catalog (Anchor improvement over the reference app's plain-text-only tags).
4. Save; card appears in a persisted, editable list.

## UI
Chip-adder pattern (input row stays fixed; new chips append below, not causing the input to jump).

## Clinical Technique
Coping-card / personal coping-plan authoring, related in spirit to relapse-prevention and safety planning but for general (non-suicide-specific) distress.

## Evidence
General clinical-practice support for written coping plans helping people access previously-identified strategies under reduced executive function during stress; not a single named RCT-tested intervention.

## Safety Rules
Keep conceptually and visually distinct from the Safety Plan (Module 12) — this is general-distress support, not a suicide-risk tool; never let it substitute for the Safety Plan.

## Data Model
```
CopingCard {
  id, problemArea: string, emotionsSymptoms: string[], copingSkills: string[],
  linkedToolIds?: string[], createdAt, updatedAt
}
```

## State Machine
`Draft → Saved (editable)`

## Offline Behavior
Fully offline.

## Existing Anchor Components
InterventionCatalog (for the optional tool-linking improvement).

## New Components
Fixed-input chip-adder component (reusable across Modules 06–08); card list/detail UI.

## Tests
- Adding a chip never shifts the input field's position.
- Linked-tool deep-link opens the correct tool.
- Multiple cards per problem area are supported.

## Acceptance Criteria
A user can create a complete card (area + at least one emotion tag + at least one skill tag) in under 2 minutes.

## Complexity
Low.

## Priority
P1.

---

# MODULE 08 — Strengths Journal

## Goal
Low-effort, positive-psychology self-affirmation logging.

## User Problem
Negative self-view is common and self-sustaining in PTSD; a running list of one's own strengths/successes is a lightweight counterweight.

## Target PTSD State
Sad/Hopeless, low self-worth.

## Trigger
Manual open; auto-prompted after a successfully completed coping session (Anchor improvement — "you just did something hard, want to note a strength?").

## User Flow
1. Tap "+Add Strength."
2. Free-text entry, save.
3. Entry appended to a persisted, viewable list.

## UI
Minimal — a single free-text add flow and a simple list.

## Clinical Technique
Positive psychology / strengths-based journaling.

## Evidence
General support for modest wellbeing/mood improvements from positive-psychology journaling exercises with repeated practice; effects typically small-to-moderate.

## Safety Rules
None specific.

## Data Model
```
StrengthEntry { id, text: string, createdAt, sourceSessionId? }
```

## State Machine
Trivial — `Add → Saved`.

## Offline Behavior
Fully offline.

## Existing Anchor Components
SessionOutcome (for the post-session auto-prompt trigger).

## New Components
Add-and-list UI; post-session prompt hook.

## Tests
- Entry saves and persists correctly.
- Post-session prompt fires only after a genuinely completed (not abandoned) session.

## Acceptance Criteria
Entry can be added in under 15 seconds.

## Complexity
Low.

## Priority
P2.

---

# MODULE 09 — Free Journal

## Goal
Unstructured text venting/processing space, explicitly private.

## User Problem
Sometimes structure gets in the way; a plain journal is still valuable.

## Target PTSD State
General.

## Trigger
Manual open.

## User Flow
Open → "+Add" → free text → save → persisted, dated, chronological list.

## UI
Minimal text editor; explicit on-screen privacy statement ("stays on your device, never leaves").

## Clinical Technique
Expressive writing.

## Evidence
General support for expressive-writing exercises providing modest benefits for processing distressing experiences; evidence is more consistent for structured expressive-writing protocols (e.g., writing about a trauma for a fixed number of sessions) than for unstructured ad-hoc journaling, but the latter remains a reasonable low-risk feature.

## Safety Rules
None specific beyond the standard privacy assurance.

## Data Model
```
JournalEntry { id, text: string, createdAt, updatedAt }
```

## State Machine
Trivial.

## Offline Behavior
Fully offline, on-device only, no sync by default.

## Existing Anchor Components
None specific — standalone.

## New Components
Text editor UI; entry list.

## Tests
Entry CRUD correctness.

## Acceptance Criteria
Entry created, edited, and deleted correctly; explicit privacy copy visible.

## Complexity
Low.

## Priority
P2.

---

# MODULE 10 — Custom Coping Tool Builder ("Create Tool" equivalent)

## Goal
Let a user turn any personally meaningful content (their own affirmation text, a voice memo, a photo, a video, a specific song) into a first-class coping tool alongside the built-in catalog.

## User Problem
No pre-built library can contain the one specific thing that works for a given individual (a message from a specific loved one, a specific song, a specific photo).

## Target PTSD State
General — maximum personalization.

## Trigger
Manual creation from the tool catalog.

## User Flow
1. Name the tool (short character limit).
2. Pick a content type: Text, Voice Memo, Music (device-local pick), Photo, Video.
3. Author/attach content (Text gets a rich-text editor: bold/italic/underline/strikethrough/heading styles/color/alignment; other types use OS pickers/recorders).
4. Save; tool appears in a clearly-labeled "My Tools" section (not silently reordering the shared catalog — improvement over reference app).

## UI
Simple stepper form; type-specific content editor/picker; distinct "My Tools" section with a filter toggle on the main catalog.

## Clinical Technique
Personalized/idiographic coping-tool construction — not itself a named clinical technique, but a strong personalization mechanism consistent with the broader principle that individualized coping strategies outperform generic ones.

## Evidence
No specific instrument evidence applies (this is a construction mechanism, not a technique); the underlying principle — personalized, self-selected coping content — is consistent with general clinical experience that self-relevant material is more engaging and effective than generic material.

## Safety Rules
Scan/validate that media attachments don't silently fail (e.g., a corrupted memo) before allowing save; no content-moderation concerns since this is fully private/local.

## Data Model
```
CustomTool {
  id, name: string, type: "text"|"memo"|"music"|"photo"|"video",
  contentRef: string (rich-text blob | file URI), createdAt
}
```

## State Machine
`NameEntry → TypeSelection → ContentAuthoring → Saved`

## Offline Behavior
Fully offline; all content stored locally.

## Existing Anchor Components
InterventionCatalog (custom tools become first-class catalog entries, tagged as user-created).

## New Components
Type selector; rich-text editor; media pickers/recorders; "My Tools" filtered view.

## Tests
- Each content type saves and reopens correctly.
- "My Tools" filter shows exactly the user's custom tools, no more/fewer.
- Deleting a custom tool doesn't affect the built-in catalog.

## Acceptance Criteria
A user can create and later reopen a Text-type tool with formatting intact; a Photo/Video/Music tool correctly references the picked media.

## Complexity
Medium–High (media handling + rich-text editor).

## Priority
P2 — high differentiation value but not required for MVP safety/clinical coverage.

---

# MODULE 11 — Crisis Resources Hub (India-localized)

## Goal
One consolidated, zero-friction, one-tap-to-contact safety screen reachable from anywhere in the app.

## User Problem
In a genuine crisis, every extra tap, every confirmation dialog, and every wrong turn in navigation is a real cost.

## Target PTSD State
Suicidal crisis, acute panic, any moment requiring immediate outside help.

## Trigger
A persistent, always-visible entry point (not just a Home tile — also a small icon in the global app bar), plus automatic surfacing after any concerning assessment result or Module 05/06 safety threshold.

## User Flow
1. Tap the crisis entry point from anywhere.
2. Screen shows, top to bottom, ordered by urgency: Emergency (112), a primary mental-health crisis line (e.g., KIRAN 1800-599-0019), other relevant lines (iCall, Vandrevala Foundation, Snehi, domestic-violence/sexual-assault-specific lines as regionally appropriate), each as one large one-tap pill button (direct dial/text/chat intent, no confirmation step).
3. Inline "Open my Safety Plan" and "Contact someone from my Support Contacts" CTAs below the hotline list.

## UI
Exactly matches the reference app's pattern: stacked labeled sections, one large pill button per resource, no unnecessary confirmation dialogs.

## Clinical Technique
Crisis-line access / means of immediate professional contact — a core, non-negotiable component of any suicide-safety-oriented design.

## Evidence
Crisis lines have general evidence and broad clinical consensus support as an effective, low-barrier point of contact during acute suicidal crisis; ensuring frictionless access is itself an evidence-informed design requirement (delay/friction is a known barrier to help-seeking during crisis).

## Safety Rules
Every hotline button must be a single tap with no intermediate confirmation dialog. This screen must be reachable from every screen in the app within 2 taps maximum, and from any safety-threshold trigger (Module 05, Module 06, Module 13) automatically or via one obvious tap.

## Data Model
```
CrisisResource { id, label, type: "call"|"sms"|"chat"|"emergency", contact: string, order: int, region: "IN"|"global" }
```
Configurable/updatable list, not hardcoded, so numbers can be corrected without an app update.

## State Machine
Stateless — a direct-action screen.

## Offline Behavior
Screen itself renders offline; actual call/SMS/chat requires network/carrier as normal for those OS actions.

## Existing Anchor Components
SafetyFilter (this screen is SafetyFilter's primary UI surface — build/extend here).

## New Components
Resource-list config + renderer; global-app-bar crisis icon.

## Tests
- Every resource button fires the correct intent with zero extra dialogs.
- Screen is reachable within 2 taps from every other screen in a navigation audit.
- Resource list updates correctly if config changes without requiring a rebuild.

## Acceptance Criteria
From a cold app start, a user can reach this screen and initiate a call to the primary crisis line in ≤3 total taps.

## Complexity
Low (mostly configuration + intents), but P0 priority because of its role.

## Priority
P0.

---

# MODULE 12 — Safety Plan Extension

## Goal
Bring Anchor's existing SafetyPlan feature to full parity with the Stanley-Brown 6-step canonical model, plus export/share and persistent crisis access.

## User Problem
A safety plan is only as useful as it is complete, reviewable, and shareable with real support people/providers.

## Target PTSD State
Suicidal crisis risk.

## Trigger
Drawer/settings entry; Crisis Resources inline CTA; scheduled reminder.

## User Flow
1. First-run explainer (who/why/when to create one; explicit "it's ok to skip steps"; ~30-minute expectation set correctly up front).
2. Hub: View/Edit Plan, Get Support Now (→ Module 11), Set Reminders.
3. Plan editor: exactly 6 steps in this order — Warning Signs, Internal Coping Strategies, Social Distractions, People I Can Call, Professionals I Can Call, Making My Environment Safe — each a chip-adder list (reuse Module 07's fixed-input pattern), swipeable between steps with visible Prev/Next arrows (not swipe-only).
4. Persistent one-tap crisis-line button visible on the plan-overview screen and, ideally, every step.
5. Export/Share the completed plan as text/PDF.
6. Optional reminder scheduling to revisit the plan.

## UI
Matches the reference app's structure closely (it is the correctly-implemented published clinical model) with the two identified fixes: visible Prev/Next arrows in addition to swipe, and export/share included.

## Clinical Technique
Stanley-Brown Safety Planning Intervention.

## Evidence
Solid, specific general evidence base including associations with reduced suicidal behavior and improved follow-up care engagement when used in clinical settings — one of the strongest-evidenced techniques in this entire audit precisely because it is a real, named, published protocol.

## Safety Rules
Never let plan steps be mandatory to "complete" the plan — every step must be independently skippable and revisitable. Crisis-line button must be reachable without leaving the Safety Plan context at any point.

## Data Model
```
SafetyPlan {
  id, warningSigns: string[], internalCoping: string[], socialDistractions: string[],
  peopleToCall: ContactRef[], professionalsToCall: ContactRef[], environmentSafety: string[],
  updatedAt
}
```

## State Machine
`Step1 ⇄ Step2 ⇄ ... ⇄ Step6` (free bidirectional navigation, nothing gated).

## Offline Behavior
Fully offline; export/share may use OS share sheet (offline-capable for local file share, online for e.g. emailing to a provider).

## Existing Anchor Components
Anchor's existing SafetyPlan (verify/extend structure to match the 6-step canon exactly), SafetyFilter/Module 11 (crisis-button target).

## New Components
Export/share generator; Prev/Next step navigation (in addition to swipe); reminder scheduling hook.

## Tests
- All 6 steps present in the canonical order.
- No step blocks progression when empty.
- Export produces a complete, correctly-formatted document.
- Crisis-line button present and functional on the overview screen.

## Acceptance Criteria
A user can create a minimally-viable plan (at least one entry in Warning Signs and one in People I Can Call) in under 5 minutes and export it.

## Complexity
Medium (mostly extension of existing SafetyPlan).

## Priority
P0 — safety-critical, and Anchor already has the foundation.

---

# MODULE 13 — Assessment Safety Footer (cross-cutting)

## Goal
Ensure every self-assessment result screen (PCL-5 and any brief screener) unconditionally shows crisis resources, regardless of score.

## User Problem
A brief screener has no dedicated suicidal-ideation item to gate on, and even a full assessment shouldn't rely solely on score thresholds to decide whether someone needs to see crisis resources.

## Target PTSD State
Any user who has just completed a self-assessment.

## Trigger
Automatic — renders on every assessment result screen.

## User Flow
Assessment completes → score shown → plain-language interpretation → non-diagnostic disclaimer → **unconditional** "You're not alone" block with 1-tap access to Module 11's primary resources → Done.

## UI
A fixed, always-rendered block at the bottom of every result screen; visually distinct but not alarmist.

## Clinical Technique
Universal safety-net framing on self-report results — a design pattern, not a named clinical instrument.

## Evidence
Consistent with general clinical guidance that self-report screening tools should be paired with accessible next-step/crisis-resource information regardless of score, since screening tools are not risk-assessment instruments on their own.

## Safety Rules
This block must never be conditionally hidden based on score — it is universal by design, exactly matching the one pattern in the reference app worth copying without modification.

## Data Model
No new persisted data; a shared UI component consuming Module 11's resource config.

## State Machine
N/A — rendered unconditionally.

## Offline Behavior
Fully offline.

## Existing Anchor Components
Anchor's existing PCL-5 result screen (retrofit this component in); Module 11 (resource source).

## New Components
Shared "AssessmentSafetyFooter" component.

## Tests
- Footer renders on every assessment result screen regardless of score value (including the lowest possible score).
- Footer's resource buttons match Module 11's live config.

## Acceptance Criteria
100% of assessment result screens in the app include this footer; no code path can render a result screen without it (enforced structurally, e.g. via a shared result-screen template).

## Complexity
Low.

## Priority
P0 — cheap, safety-critical, easy to get wrong by omission.

---

# MODULE 14 — In-App Support Contacts (NOT OS-contacts-bound)

## Goal
Let a user maintain a short list of trusted people to reach out to, without requiring OS Contacts permission or writing into the user's real address book.

## User Problem
The reference app's OS-Contacts-backed approach requires an intrusive permission and pollutes/depends on the user's real contact list; a fully in-app model is more private and more portable.

## Target PTSD State
Disconnected From People, general support-seeking.

## Trigger
Manual add from Settings/Support section, or inline from Module 11's Crisis Resources screen.

## User Flow
1. "+Add Contact."
2. Fields: Name, Relationship (free text or small preset list), Phone/contact method, optional note (e.g., "best time to call").
3. Save; appears in a simple list with tap-to-call/text/message actions (using OS intents, but the underlying data model stays independent of OS Contacts).

## UI
Simple form + list, no permission prompt required.

## Clinical Technique
Social-support-network activation — supports Module 07's Connect-With-Others-style suggestions by making them concretely actionable (deep-link straight to a real saved person instead of a generic "call a family member" card).

## Evidence
General support for social connectedness as a protective factor and coping resource in PTSD recovery; this module is an access mechanism, not a technique with its own evidence base.

## Safety Rules
None specific; ensure a user can quickly remove a contact if a relationship changes (no confirmation friction beyond a standard undo).

## Data Model
```
SupportContact { id, name: string, relationship?: string, phone?: string, note?: string, createdAt }
```

## State Machine
Trivial CRUD.

## Offline Behavior
Fully offline, fully local, no OS Contacts permission required.

## Existing Anchor Components
None specific — but should be the single canonical contact list referenced by Module 07 (Connect suggestions), Module 11 (Crisis Resources inline CTA), and Module 12 (Safety Plan's People I Can Call step) rather than four separate implementations.

## New Components
Contact CRUD UI; tap-to-call/text intent wiring.

## Tests
- No OS Contacts permission is ever requested.
- The same contact list is referenced consistently across Modules 07/11/12.
- Delete removes a contact everywhere it's referenced (or handles dangling references gracefully).

## Acceptance Criteria
A user can add, edit, call, and remove a contact without ever seeing an OS permission dialog.

## Complexity
Low.

## Priority
P1.

---

# MODULE 15 — Sleep Toolkit

## Goal
Sleep-hygiene and stimulus-control content and light tracking for PTSD-related sleep disruption.

## User Problem
Sleep difficulty is one of the most common and distressing PTSD symptoms and often the presenting complaint.

## Target PTSD State
Unable to Sleep.

## Trigger
Symptom picker ("Unable to Sleep") or a dedicated SLEEP quick-access entry point.

## User Flow
1. Static psychoeducation content: winding-down activities, activities-to-avoid-before-bed, what-to-do-if-you-can't-sleep, morning wake-up activities (matching the reference app's 4-category structure).
2. (Anchor improvement) an optional simple sleep diary (bed time, wake time, awakenings) and a stimulus-control reminder ("if not asleep in ~20 min, get up and do a calm activity, come back when sleepy").
3. Deep-link to Module 04's breathing/relaxation guided sessions for pre-sleep use.

## UI
Simple content pager + optional diary entry form.

## Clinical Technique
CBT for Insomnia (CBT-I) sleep hygiene and stimulus control.

## Evidence
Strong, well-established evidence for CBT-I as first-line insomnia treatment, including insomnia comorbid with PTSD; static tips alone are a much lighter-weight application than full structured CBT-I and should be framed as a starting point, not a substitute for it when insomnia is severe/persistent.

## Safety Rules
None specific.

## Data Model
```
SleepDiaryEntry { id, date, bedTime, wakeTime, awakenings: int, notes? }
```

## State Machine
Trivial.

## Offline Behavior
Fully offline.

## Existing Anchor Components
Module 04 (deep-link to breathing/relaxation sessions).

## New Components
Content pager; sleep diary form; stimulus-control reminder logic.

## Tests
Diary entries persist and display correctly in chronological order.

## Acceptance Criteria
Content accessible in ≤2 taps from the Unable-to-Sleep symptom entry.

## Complexity
Low–Medium.

## Priority
P2.

---

# MODULE 16 — Worry Time

## Goal
Let a user postpone a specific worry to a scheduled time rather than suppress or ruminate on it immediately.

## User Problem
Worry/rumination, especially at night, is common and disruptive; deferring it with intent (not avoidance) is a known coping technique.

## Target PTSD State
Worried/Anxious, Unable to Sleep.

## Trigger
Symptom picker or Tools catalog.

## User Flow
1. Enter the worry topic (free text).
2. Optionally set a reminder for a specific future time.
3. (Anchor improvement) at the scheduled time, present a short structured prompt: "What's the worry? Is there a concrete next step you can take? If not, would a defusion or acceptance exercise help right now?" with deep-links into Module 04/05 as appropriate — rather than only firing a bare reminder.

## UI
Simple text entry + time picker; scheduled-prompt notification.

## Clinical Technique
CBT worry-time / stimulus control for worry.

## Evidence
General support within CBT for generalized anxiety for reducing worry intrusion via scheduled worry time; evidence for a bare app reminder alone (vs. the full worry-time protocol including active engagement at the scheduled time) is thinner — the Anchor improvement above narrows that gap.

## Safety Rules
None specific.

## Data Model
```
WorryEntry { id, topic: string, scheduledAt?: timestamp, resolvedAt? }
```

## State Machine
`Logged → (ReminderFired) → Engaged|Dismissed`

## Offline Behavior
Fully offline; local notification.

## Existing Anchor Components
Local notification infrastructure (if present); Modules 04/05 (deep-link targets from the scheduled prompt).

## New Components
Entry form; scheduled local notification; structured at-time prompt.

## Tests
Reminder fires at the scheduled time; structured prompt deep-links correctly.

## Acceptance Criteria
Entry + reminder set in under 30 seconds.

## Complexity
Low.

## Priority
P2.

---

# MODULE 17 — Ambient Sounds & Soothing Media Library

## Goal
Bundled, fully-offline sensory self-soothing content (nature soundscapes, calming imagery) that works out of the box with zero user setup — unlike a "bring your own music" approach, which has a cold-start problem.

## User Problem
A brand-new install with no personal media saved (the reference app's "Soothing Songs" feature is empty by default and stays empty until the user manually adds their own) offers nothing useful on day one.

## Target PTSD State
General anxiety/arousal reduction, sleep onset, sensory grounding.

## Trigger
Tools catalog or symptom routing (sleep, anxiety).

## User Flow
1. Pick a bundled track/image (originally-produced or appropriately-licensed, not copied assets) from a themed list (e.g., rain, forest, waves — original naming/content).
2. Full-screen player: play/pause, last-played remembered as default next time.
3. Optionally allow the user to add their own local media as a supplement, but never make bundled content depend on user-supplied files.

## UI
Full-bleed themed background + minimal playback controls, matching the calm, low-chrome feel of the reference app's Ambient Sounds player.

## Clinical Technique
Sensory self-soothing.

## Evidence
General support for calming sensory input (nature sounds/imagery) reducing subjective arousal/anxiety in the short term; a low-risk, low-specificity adjunct technique rather than a primary treatment component.

## Safety Rules
None specific.

## Data Model
```
SoothingMedia { id, type: "audio"|"image", title, assetRef, tags: string[] }
```

## State Machine
Trivial playback state.

## Offline Behavior
Fully offline — bundled assets, no cold-start problem.

## Existing Anchor Components
Existing audio-playback infrastructure (extend with bundled content rather than build new).

## New Components
Original/licensed soundscape and imagery asset set; player UI (can share chrome with Module 04).

## Tests
Playback works fully offline immediately after install with zero user configuration.

## Acceptance Criteria
A fresh install has at least 5 working bundled audio tracks and 5 working bundled images with no setup required.

## Complexity
Low (engineering) / requires original or properly licensed content sourcing.

## Priority
P2.

---

# MODULE 18 — Onboarding & Personalization Intake

## Goal
Lightweight first-run flow that sets expectations and captures a few personalization signals without gatekeeping access.

## User Problem
A new user needs to understand what the app does and trust its privacy stance quickly, without a long form standing between them and help.

## Target PTSD State
N/A — pre-symptom-specific, applies to all new users.

## Trigger
First app launch.

## User Flow
1. 3–4 short screens: what the app is for, privacy stance (plain language, matching the reference app's strong "no account, nothing leaves your device" framing), and how to get help fast.
2. One light personalization question (e.g., "what would be most helpful to set up first?") — optional, skippable, and NOT gating any feature behind an answer.
3. Land directly on the main dashboard.

## UI
Swipeable short pager, Skip always visible, no forced multi-question intake.

## Clinical Technique
N/A (product onboarding, not a clinical technique).

## Evidence
N/A.

## Safety Rules
Never let onboarding delay access to Module 11 (Crisis Resources) — it should be reachable even before onboarding completes, if a user needs it immediately.

## Data Model
```
OnboardingState { completed: bool, skippedAt?: timestamp, lightPreferences?: map }
```

## State Machine
`Screen1 → Screen2 → Screen3 → (optional pref) → Done` (Skip jumps straight to Done from any screen).

## Offline Behavior
Fully offline.

## Existing Anchor Components
None specific.

## New Components
Onboarding pager; Skip-always-available pattern.

## Tests
Skip from any screen lands on the main dashboard; Crisis Resources reachable during onboarding.

## Acceptance Criteria
Onboarding completable (or skippable) in under 30 seconds.

## Complexity
Low.

## Priority
P1.

---

# QUICK ACCESS SYSTEM (Phase 11)

Designed for someone actively distressed, optimizing for one-handed use, minimal reading, and minimal decisions. This reuses Anchor's existing SafetyFilter and InterventionRouter — it is a routing/entry-point layer, not a new parallel architecture.

## Entry points and what each routes to

| Entry point | Routes to | Rationale |
|---|---|---|
| **PANIC** | Module 04 breathing session (auto-started, no pre-rating gate) with a persistent one-tap link to Module 11 Crisis Resources | Acute physiological arousal needs the fastest possible calming action; skip all friction. |
| **FLASHBACK** | Module 05 Trigger Reset Protocol | Purpose-built for exactly this state. |
| **HYPERVIGILANCE** | InterventionRouter with symptomTag="hypervigilance" → prioritizes Module 04 breathing/PMR and grounding tools | Physiological down-regulation first. |
| **DISSOCIATION** | Anchor's existing CameraX/ML Kit grounding tool (already stronger than a static card deck) | Active, attention-demanding grounding is the right first response to dissociation. |
| **INTRUSIVE THOUGHTS** | Module 04 ACT-defusion-style guided session, with Module 07 Coping Cards as a secondary option if the user has one for this | Defusion technique matches this specific symptom best. |
| **SLEEP** | Module 15 Sleep Toolkit → deep-links to Module 04 breathing/relaxation for pre-sleep use | Matches reference app's sleep-symptom routing. |
| **GROUND ME** | Anchor's existing CameraX/ML Kit grounding directly (bypasses router — this label IS the technique, not a symptom) | Explicit technique request, no need to route further. |
| **BREATHE** | Module 04 breathing session directly | Same as above — direct technique request. |
| **FOCUS** | A short attention-shifting activity from the Puzzle System (06_ANCHOR_PUZZLE_SYSTEM.md) | Distinct from grounding/breathing — cognitive attention-shift rather than sensory/physiological. |
| **SAFETY PLAN** | Module 12 directly (skips hub if the plan already exists and jumps to overview; shows hub/tutorial only on first use) | Direct technique/tool request. |
| **GET SUPPORT** | Module 11 Crisis Resources Hub directly | Direct safety request — zero friction. |

## Navigation shape

A single always-visible quick-access affordance (larger and visually distinct from Home's other elements, plus mirrored as a persistent small icon in the global app bar on every screen) opens a **single screen** listing all entry points above as large, one-tap rows — no nested menus, no sub-categories to browse through. Tapping any row immediately performs the routing above; there is no intermediate confirmation screen for any of them. The screen itself has zero required reading beyond the row labels themselves (original wording, short — 1-3 words each, verb-or-noun-led).

## Interaction flow (example: PANIC)

1. User taps the persistent quick-access icon (available from literally any screen, including mid-session in another tool).
2. Quick-access sheet opens (a bottom sheet preferred over a full screen transition, since it requires no navigation animation and can be dismissed with a single swipe if tapped by mistake).
3. User taps "PANIC."
4. Module 04's breathing session begins immediately — no pre-rating screen, no intro-copy Continue button, straight into the interactive haptic pacer.
5. A small, always-visible "Get Support" link remains present throughout the session (matching Module 05's persistent safety-escape pattern) in case breathing alone isn't enough.
6. On completion, an optional (fully skippable) post-rating per Module 01, since the outcome data has real value for Module 02, but is never mandatory.

## Design constraints carried through every entry point
- No entry point requires more than 2 taps from anywhere in the app to reach its destination.
- No entry point blocks on text entry, a distress rating, or a confirmation dialog before showing help.
- Every destination screen carries a persistent, one-tap escalation path to Module 11 (Crisis Resources), even the non-crisis ones like SLEEP or FOCUS, per the "always show a safety net" principle observed as a strength in the reference app's assessment results (Module 13) and generalized here.
- This system does not duplicate InterventionRouter or SafetyFilter — every route above is a thin routing rule into those existing/planned Anchor systems, not a new decision engine.
