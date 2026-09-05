# UI/UX Replication Specification

Design pattern documentation for Anchor's original implementation — no proprietary text, art, or exact copy is reproduced. Colors described qualitatively (PTSD Coach uses a deep-to-medium blue primary palette, white/off-white content backgrounds, sparing use of a lighter blue accent, and category-colored circular icons in list rows).

---

## Screen: Home Dashboard

### Purpose
Central launcher/dashboard; entry point to everything.

### Entry point
App open (after onboarding); drawer "Home"; Home icon on any tab.

### Layout
Top app bar (hamburger left, title center, one contextual icon right — Reminders clock on Home only). Below: a curved-bottom blue banner containing a rotating quote and a downward "expand" affinity chevron. Below that: a full-bleed 2×2 grid, each cell = a large flat icon + label, split by thin divider lines. Below the grid: a light-blue curved panel containing 2 quick-tiles (Crisis Resources, Add a Favorite) with a "Next" chevron, and a horizontally-paged "Favorite Tools" carousel underneath.

### Controls
Hamburger (opens drawer), Reminders icon, quote expand chevron, 4 grid buttons, 2 quick-tiles, carousel paging, favorite-tool cards (once populated).

### Interaction
Single tap per control; no multi-step gestures required to reach any of the 4 main sections.

### State changes
None on Home itself besides quote rotation and (once favorites exist) carousel population.

### Navigation
Grid buttons → the 4 tabs (Manage/Track/Learn/Support). Quick-tiles → Crisis Resources screen / Add-a-Favorite picker.

### UX strengths
Extremely low cognitive load — 4 big unambiguous choices, nothing buried. Crisis Resources is always one tap away without needing to know which tab it lives under. Favorites carousel rewards personalization with visible payoff.

### UX weaknesses
The 2×2 grid gives visual parity to "Learn" (low-urgency, browsing) and "Get Support"/"Manage Symptoms" (potentially high-urgency) — a distressed user has no visual cue about which quadrant is fastest for them right now. No single "I need help right now" affordance bigger than the other three; Crisis Resources is same-size as "Add a Favorite." No indication of current distress state or a smart-default suggestion.

### Anchor redesign
Keep the low-tap-count philosophy but visually and hierarchically differentiate a distress-state entry point (e.g., a single prominent "I'm struggling right now" action above the grid, sized and colored distinctly, that opens Anchor's InterventionRouter directly) from the calmer, browsing-oriented sections. Preserve one-tap crisis access, but make it reachable even faster (e.g., a persistent small icon in the app bar on every screen, not just a Home tile) for true one-handed, high-distress use.

---

## Screen: Slide-out Drawer

### Purpose
Secondary navigation for settings and less-frequent destinations.

### Entry point
Hamburger icon (top-left) from any screen.

### Layout
Overlay panel sliding from the left, covering ~73% of screen width, dimmed scrim over the remaining content; vertical list of text-only rows; footer shows app version.

### Controls
Close (X), 10 destination rows, scrim-tap-to-dismiss.

### Interaction
Tap a row → navigates and closes drawer.

### State changes
None.

### Navigation
Flat list, no nesting; every drawer destination is a direct sibling, one tap away.

### UX strengths
Simple, predictable, standard Android pattern; version number visible for support purposes.

### UX weaknesses
Mixes genuinely different priority levels in one flat list — "Safety Plan" (safety-critical) sits at the same visual weight as "Share This App" (marketing). No icons, just text, which slows scanning under stress. Sub-screens reached from here navigate "up" back to Home rather than back to the drawer/list, which is disorienting if a user wants to visit two drawer items in a row (must reopen the drawer each time).

### Anchor redesign
Group drawer items by priority tier (Safety-related items visually distinct/pinned at top; Settings/About grouped and visually de-emphasized at bottom). Add small icons for faster recognition. Preserve "return to the calling context" navigation rather than always bouncing to Home, so chaining settings changes doesn't require repeated drawer reopens.

---

## Screen: Distress Meter (pre/post rating)

### Purpose
Capture a 0–10 self-reported distress level immediately before and after using a coping tool.

### Entry point
Automatically shown when opening most tools (from a Symptom category always; from the Tools catalog for most, but not all, individual tools).

### Layout
Question text at top, a vertical "thermometer" bar with tick marks on the right, a large circular up/down arrow pair on the left with a numeric label between them ("--" until set), primary "NEXT" button (disabled until a value is chosen) and a secondary "SKIP" button below it (only present on the pre-rating; the post-rating shows only "NEXT").

### Controls
Up arrow, down arrow, Next, Skip (pre only), help (?) icon.

### Interaction
Tap up/down repeatedly to set a 0–10 value (first tap observed jumping straight to 10 in testing — worth avoiding this jump-to-max quirk in Anchor's version); thermometer fill updates live.

### State changes
Sets the session's baseline (pre) or outcome (post) distress value; on post-completion this triggers the Distress-Change feedback screen.

### Navigation
Pre-rating leads into the chosen/suggested tool; post-rating leads to the feedback/reinforcement screen.

### UX strengths
Very low-friction 0–10 capture with a visual metaphor (thermometer) that's faster to read than a plain number picker. Skip option respects that some users don't want to rate right now. Reusing the same widget for pre and post makes the "did this help?" comparison intuitive.

### UX weaknesses
Requiring this gate before *every* tool adds friction that may deter quick use during high distress, exactly when speed matters most. The up/down-arrow-only input (no direct tap-to-set-value on the thermometer itself) is slower than it needs to be. Disabled Next until a value is set can strand a user who taps Skip's neighbor by mistake.

### Anchor redesign
Make the pre-rating tappable directly on the scale (tap a tick mark to set that value in one gesture) in addition to arrows. Keep it fully skippable, and make it non-blocking for genuinely time-critical entry points (e.g., PANIC/FLASHBACK quick-access should not force a rating before showing help). Reserve the mandatory-feeling version for calmer, exploratory tool use where the outcome data is genuinely useful for PersonalizationScorer.

---

## Screen: Symptom Picker (Manage ▸ Symptoms)

### Purpose
Let a user find a coping tool by naming what they feel, rather than by technique name.

### Entry point
Manage tab, default sub-tab.

### Layout
Simple vertical list of large rows, each an icon + label (7 rows: Unable to Sleep, Worried/Anxious, Sad/Hopeless, Disconnected From People, Disconnected From Reality, Avoiding Triggers, Reminded of Trauma).

### Controls
7 tappable rows; 3 sub-tab pills (Symptoms/Tools/Favorites) above.

### Interaction
Tap a symptom → Distress Meter → a single suggested tool opens (not a filtered list) → "NEW TOOL" button on that tool re-rolls to a different suggestion for the same symptom.

### State changes
None persisted by the picker itself; downstream Distress Meter/tool-use state changes apply.

### Navigation
Leads into the Distress-Meter-wrapped tool flow.

### UX strengths
Symptom-first framing matches how a distressed person actually thinks ("I feel X" rather than "I want technique Y"). One-suggestion-at-a-time avoids decision paralysis; reroll gives an escape hatch without overwhelming with a full list.

### UX weaknesses
7 discrete categories can't capture blended states (e.g., "avoiding + reminded of trauma at once"); no multi-select. No visible indication of how many tools exist behind a category, so the reroll can feel like a slot machine rather than a considered recommendation. No obvious link from this list to the safety-critical flows (Crisis Resources, Safety Plan) even though "Reminded of Trauma" or "Avoiding Triggers" could plausibly be a moment of real risk.

### Anchor redesign
Preserve the symptom-first mental model but let InterventionRouter drive selection using richer signals (time of day, past effective tools for this symptom from PersonalizationScorer, current distress rating) instead of pure randomization, and surface an always-visible small "not safe right now?" link to SafetyFilter directly from this screen.

---

## Screen: Tools Catalog (Manage ▸ Tools)

### Purpose
Browsable master list of every coping tool.

### Entry point
Manage tab, "Tools" sub-tab.

### Layout
Long vertical alphabetical list, one row per tool (icon + label), a floating "Create Tool" action fixed near the bottom-right.

### Controls
Scrollable list, Create Tool FAB.

### Interaction
Tap a row → Distress Meter (usually) → tool.

### State changes
None from browsing itself.

### Navigation
Straight into the tool.

### UX strengths
Complete transparency — nothing is hidden; power users who know what they want can go straight there.

### UX weaknesses
25 items in one flat alphabetical list is a lot to scan, especially under distress, with no grouping by technique family (breathing vs. cognitive vs. journaling vs. sensory) or by typical duration. Custom user-created tools get pinned at the very top, which is good for the tool's owner but means the catalog's alphabetical promise is broken without any visual explanation why.

### Anchor redesign
Group the catalog by technique family and/or by duration ("under 2 minutes" / "5–10 minutes" / "journal/write") with the alphabetical list available as a secondary "see all" view; visually separate the user's own custom tools into their own labeled section rather than silently reordering the list.

---

## Screen: Generic Guided-Audio Tool Player (Deep Breathing, Body Scan, Muscle Relaxation, Mindfulness family, Positive Imagery, Observe Thoughts)

### Purpose
Deliver a fixed-length audio-guided relaxation/mindfulness exercise.

### Entry point
Tapped from Symptoms, Tools catalog, or Favorites.

### Layout
Toolbar (back, title, Add-to-Favorite heart, Done); below, an intro screen (technique description, duration, any safety caveat, "Continue"); then the player screen: a small mostly-static visual region, a seek bar with elapsed/remaining time, a single play/pause circular button, and synchronized caption text beneath.

### Controls
Continue, play/pause, seek bar, Add-to-Favorite, Done.

### Interaction
Linear playback; user can scrub the seek bar; captions update as narration progresses.

### State changes
Marks the tool "used" for the Distress Meter loop; Done exits (post-rating occurs only if a pre-rating was set).

### Navigation
Done → back to wherever the tool was launched from (or the post-rating flow).

### UX strengths
Consistent, predictable pattern reused across ~10 different tools reduces relearning cost. Captions are a genuinely good accessibility/discretion feature (usable without audio in a public place).

### UX weaknesses
The visual is minimal/static across every one of these tools — no differentiation in feel between, say, Body Scan and Loving Kindness beyond the caption text and duration. No pause-resume-position memory observed across sessions in this pass. No adjustable playback speed.

### Anchor redesign
Reuse a single shared "guided session" component (matching the reusability PTSD Coach itself demonstrates) but give each technique family a distinct, purposeful visual (e.g., a body outline for Body Scan, a breathing shape for breathing exercises) rather than one generic static region for everything, and add resume-where-you-left-off plus adjustable pace/speed where the technique allows it.

---

## Screen: RID (Relax → Identify → Decide)

### Purpose
Guided post-trigger stabilization protocol.

### Entry point
Manage ▸ Tools ▸ RID: Coping With Triggers (or via Symptoms ▸ Reminded of Trauma / Avoiding Triggers routing).

### Layout
Step 1 (Relax): large "RID" wordmark with the active letter highlighted in red, step name, instructional copy, a large centered countdown number, and an "extend 30 seconds" pill button. Step 2 (Identify): same wordmark treatment, a psychoeducation paragraph, then (on continuing) two labeled multi-line text fields with placeholder examples. Step 3 (Decide): psychoeducation paragraph including the safety-escalation sentence, then one labeled text field. Final: a plain recap screen listing all three answers under their original questions, dated implicitly by "as of [today]."

### Controls
"GO ON" (advances a step, doubles as validation trigger), "FINISH" (last step), Add-to-Favorite heart, back arrow (which, mid-flow, reopens the previous sub-step rather than exiting).

### Interaction
Countdown auto-runs on Step 1; Steps 2–3 require the text fields to be non-empty before advancing (blocking alert dialog otherwise).

### State changes
Persists a dated 3-answer record, viewable on the summary screen (not confirmed whether past RID sessions are individually re-viewable afterward, versus only the most recent).

### Navigation
Linear, forced order, no skipping steps (contrast with Safety Plan, which explicitly allows skipping).

### UX strengths
The wordmark-with-highlighted-letter is a nice at-a-glance progress indicator that also reinforces what the acronym means at each stage. Forcing the 2 identify-step answers is a deliberate design choice to make sure the present-vs-past differentiation actually happens rather than being skippable fluff. The final recap gives closure and validates the effort just spent.

### UX weaknesses
Mandatory text-field validation is exactly the kind of friction that's risky during acute distress — a user who can't articulate an answer right now (a very plausible state during a flashback) gets stuck behind a blocking dialog instead of being allowed to move on. No visible safety-resources button embedded directly in the Decide step even though it discusses "if you are in danger" — the user has to remember to act on that sentence themselves.

### Anchor redesign
Keep the countdown-relax step and the present-vs-past differentiation idea, but make the journaling optional (skippable with a single tap, e.g. "I can't put it into words right now") rather than blocking, and make the safety-escalation sentence in the Decide step an actual tappable button that routes straight into SafetyFilter/crisis resources, not just static text.

---

## Screen: My Feelings

### Purpose
Structured, hierarchical emotion-logging with intensity and notes.

### Entry point
Manage ▸ Tools ▸ My Feelings.

### Layout
Step 1: checkbox list of 6 primary emotions. Step 2: checkbox list of nested feelings under each selected primary. Step 3: full-width 0–100 slider with plain-language endpoint labels. Step 4 (Save/recap): dated summary with bulleted feelings, a labeled percentage+band ("68% Moderate"), and a notes field.

### Controls
Checkboxes, NEXT (top-right text button throughout), slider, notes field, DONE.

### Interaction
Multi-select at each level; single continuous slider drag; free text at the end.

### State changes
Persists a dated structured mood-log entry.

### Navigation
Linear 4-step wizard, "NEXT" always top-right, no visible step counter (unlike RID/PCL-5's "Question X of Y").

### UX strengths
The hierarchical primary→granular structure teaches emotional vocabulary passively while logging. Ending on a clean recap (matching RID's pattern) gives the entry a satisfying sense of completion.

### UX weaknesses
No step-count indicator, so a first-time user doesn't know how many steps remain. Checkbox-heavy primary/secondary screens can be slow to scan for a long emotion list under distress. The intensity slider's qualitative band ("Moderate") is a nice touch but the underlying 0–100 granularity is arguably more precision than most users need in the moment.

### Anchor redesign
Add a lightweight step indicator; consider replacing the two full checkbox screens with a single tap-to-drill-down flow (tap a primary "chip," it expands in place to show its children) to reduce screen count and scrolling; keep the intensity slider but consider a coarser default (e.g., 5-point) with a "more precision" expand option.

---

## Screen: Coping Cards (create flow)

### Purpose
Author a structured, reusable personal coping reference.

### Entry point
Manage ▸ Tools ▸ Coping Cards ▸ "+".

### Layout
Problem Area selector button (opens a bottom sheet of 6 presets + "Add your own"); "Emotions & Symptoms" text field + inline "Add" button, appended entries render as removable chip rows; identical pattern repeated for "Coping Skills"; SAVE button pinned at bottom.

### Controls
Selector, 2× (text field + Add button), chip rows, Save.

### Interaction
Type → tap Add → chip appears and field clears, ready for the next entry; repeatable.

### State changes
Persists a new card, viewable in a read-only summary layout matching the same field labels.

### Navigation
Save returns to the tool's list/intro screen, where the new card now appears alongside intro copy and the "+Add" affordance for further cards.

### UX strengths
The tag-chip-adder pattern (reused across several personalization tools in this app) is genuinely good — fast, forgiving, and visually clear about what's been captured so far.

### UX weaknesses
Layout shifts as chips are added (later fields move down the screen), which caused a real navigation mistake during this very audit (text intended for one field landed in another after a layout shift) — a UX hazard for anyone typing without watching carefully. No edit/delete affordance for individual chips was surfaced in this pass (tap behavior on an existing chip wasn't confirmed).

### Anchor redesign
Keep the chip-adder interaction but pin each field's "current input row" so it doesn't visually shift as chips accumulate above it (e.g., always add new chips below a fixed input row, not above it); make it obvious (and confirmed) that tapping an existing chip lets you edit or remove it.

---

## Screen: PTSD Screen (brief assessment) — intro, questions, results

### Purpose
Administer a brief validated self-screen and interpret the result safely.

### Entry point
Track tab ▸ PTSD Screen.

### Layout — intro
Title, 2-paragraph plain-language explanation with an explicit non-diagnostic disclaimer and a proxy-use warning, single "GET STARTED" button.
### Layout — question
"Question X of N" in the toolbar title, question stem, question text, an expandable "Examples" button (gate question only), two large full-width Yes/No buttons stacked vertically.
### Layout — results
"Total Score:" heading with a large numeral, a one-line plain-language interpretation, a repeated non-diagnostic disclaimer, an "Understand PTSD" section linking to psychoeducation, a "Get Help" section explaining what a provider visit involves plus a "Find Help" button, and — unconditionally — a "You're not alone" safety block with Call/Text crisis-line buttons.

### Controls
Get Started, Yes/No pairs, Examples expander, About PTSD / Find Help buttons, Call/Text Crisis Line buttons, Done.

### Interaction
Strictly linear question flow; answering auto-advances (no separate "submit answer" step).

### State changes
Persists a dated score to Assessment History.

### Navigation
Done → Track tab / Assessment History.

### UX strengths
Auto-advance on answer removes an unnecessary tap per question. The universal, score-independent safety footer is the single best safety-UX pattern found in the whole app — it costs nothing when unneeded and could matter enormously when needed. Repeating the non-diagnostic disclaimer at both intro and results reduces the chance a user misreads the score as a diagnosis.

### UX weaknesses
Exiting mid-assessment triggers a generic "you'll lose unsaved data" dialog with no partial-save option — a user who gets interrupted (very plausible for this audience) loses all progress. No visible progress bar beyond the "X of N" text (a bar is easier to parse at a glance than reading a fraction).

### Anchor redesign
Auto-save partial progress so an interrupted assessment can be resumed; add a lightweight visual progress bar in addition to the text counter; keep — and generalize — the unconditional safety-footer pattern across every assessment result screen Anchor has (including its existing PCL-5), regardless of score.

---

## Screen: Safety Plan hub + step editor

### Purpose
Build and maintain a structured suicide safety plan.

### Entry point
Drawer ▸ Safety Plan; Home ▸ Crisis Resources ▸ "Safety Plan"; Support tab ▸ Crisis Resources.

### Layout — hub
Three large stacked cards: My Safety Plan, Get Support Now, Set Reminders; below, a "thumb up" icon with inline instructional caption for favoriting the whole feature.
### Layout — plan overview
6 stacked rows, each "STEP N:" + step name, a top-right "Export and share" icon, and a persistent "Call 988" button pinned near the bottom.
### Layout — step editor
Step name as a heading, explanatory copy on what belongs in this step, an info-icon for more guidance, and an "ADD [X]" button leading into the same chip-adder pattern used elsewhere.

### Controls
3 hub cards, 6 step rows, Export/share, Call 988, Add-entry button, chip adder, swipe left/right between steps (first-time tooltip explains this).

### Interaction
Steps form a swipeable pager once inside the editor, not just a tappable list — a first-time tooltip explicitly teaches this gesture.

### State changes
Persists the 6-step plan; presumably export/share generates a shareable document (not confirmed in detail).

### Navigation
Hub → plan overview → individual step editor (swipeable) → back to overview.

### UX strengths
The persistent Call-988 button on the plan-overview screen means a user never has to leave the Safety Plan context to reach a crisis line. The explicit "it's ok to skip steps" framing (seen in the tutorial) reduces the pressure to complete every field before the plan is "usable." Export/share supports genuinely useful real-world behavior (showing the plan to a provider or trusted person).

### UX weaknesses
The swipe-between-steps gesture is not discoverable without the one-time tooltip — a returning user who dismissed the tooltip and forgets it exists may not realize steps 2–6 are reachable by swiping rather than only via the overview list (though the overview list does also work as a way in).

### Anchor redesign
Preserve persistent one-tap crisis access on every step (adapted to India-relevant hotlines) and the explicit skip-is-okay framing; make the swipe gesture optional but always also expose clear Prev/Next arrows so the interaction isn't reliant on a one-time tooltip being remembered.

---

## Screen: Crisis Resources (canonical)

### Purpose
Single consolidated safety surface.

### Entry point
Home quick-tile; Support tab; Learn ▸ Getting Professional Help ▸ I'm in Crisis (lighter subset); Safety Plan's "Get Support Now."

### Layout
Vertically stacked labeled sections, each pairing a short context line with one or more large full-width pill buttons: emergency (911), Suicide & Crisis Lifeline (988, with a Veteran-routing note), Text/Chat crisis-line variants, Domestic Violence Hotline, Sexual Assault Hotline, then an inline "Create a Safety Plan" CTA and an inline "Personal Support Contacts" section (with its own empty-state and Add-Contact CTA).

### Controls
Every hotline is a single large tappable pill (presumed direct dial/text/chat intent, no extra confirmation step).

### Interaction
One tap → OS dialer/messaging app opens pre-filled.

### State changes
None besides the OS-level call/text/chat being initiated.

### Navigation
Terminal screen (initiates external action) or deep-links into Safety Plan / Contacts.

### UX strengths
Zero-friction, one-tap-to-dial design is exactly right for a crisis moment — no confirmation dialogs, no extra taps. Bundling adjacent-but-distinct crisis types (suicide, domestic violence, sexual assault) on one screen means a user doesn't need to guess which app section covers their specific situation. Inline escalation into Safety Plan and Contacts from the same screen avoids forcing extra navigation.

### UX weaknesses
All hotlines are US-specific (911, 988, US toll-free numbers) with no observed localization — not usable as-is outside the US. Visually, every pill button has equal weight; there's no ordering cue for "call this first if you're not sure," though the vertical order does put 911/988 first.

### Anchor redesign
Rebuild this exact "one screen, many hotlines, zero-friction one-tap dial, inline escalation to Safety Plan and Contacts" pattern with India-relevant numbers (e.g., 112 emergency, KIRAN 1800-599-0019, iCall, Vandrevala Foundation, Snehi, and any other regionally appropriate lines), and keep the equal-weight-but-ordered-by-urgency button layout. This screen should be reachable via SafetyFilter from literally anywhere in Anchor, not just a Home tile.

---

## Screen: Create Tool (custom tool builder)

### Purpose
Let a user build their own personal coping tool from any content type.

### Entry point
Manage ▸ Tools ▸ "Create Tool" FAB.

### Layout
Name field (with visible character-limit hint), a "Tool Type" selector button opening a 5-option sheet (Text/Memo/Music/Photo/Video), a content section whose control changes based on chosen type (e.g., for Text: a "TYPE TEXT" button opening a full rich-text editor with formatting controls), and a pinned "SAVE TOOL" button.

### Controls
Name field, type selector, content editor (varies), Save.

### Interaction
Fill name → pick type → author content in a type-specific sub-screen → return → Save.

### State changes
Persists a new custom tool, inserted at the top of the Tools catalog (pinned, not alphabetically merged).

### Navigation
Save → Tools catalog, now showing the new tool first.

### UX strengths
Genuinely impressive flexibility for a "coping tool" feature — letting a user attach their own voice memo, photo, video, or richly-formatted text as a first-class tool is a strong personalization idea, not a token gesture. The rich-text editor (bold/italic/underline/strikethrough/title/subtitle/color/alignment) is more capable than most users will need, which is a safe direction to err in.

### UX weaknesses
Pinning new custom tools at the top of an otherwise-alphabetical list is inconsistent and undocumented — the sudden appearance of an out-of-order item may confuse a user who expects alphabetical ordering. No visible way (in this pass) to browse "my custom tools" as their own filtered group.

### Anchor redesign
Keep the 5-content-type flexibility (it's a genuine differentiator) but give custom tools their own clearly-labeled section ("My Tools") rather than silently reordering the shared catalog, and let a user filter the catalog to just their own creations.
