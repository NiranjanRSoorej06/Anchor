# Anchor Puzzle / Attention-Shifting System

## Reference-app finding: no true puzzle/game feature exists

PTSD Coach itself contains **no attention-shifting puzzle, game, or cognitive-activity feature**. Its closest relatives are passive/receptive: guided audio tracks (Module 04 family) and static content decks (Grounding, Change Your Perspective). Nothing in the reference app asks the user to actively perform a visual-search, counting, pattern, or memory task. This is a genuine gap in the reference product, not something to "replicate" — it is a deliberately original addition for Anchor, and it must be positioned carefully per the task's clinical-honesty requirement.

## Underlying technique and evidence framing

Brief attention-shifting/cognitive tasks are a recognized *grounding* mechanism — the general clinical logic is that engaging working memory and active visual/verbal attention on a neutral, effortful task competes with rumination, dissociation, and panic-spiral attention, similar in spirit to the "counting objects of a color," "name 5 things you can see" style grounding exercises already common in trauma-informed care (and present in the reference app as static Grounding prompts). This is a well-established *category* of grounding technique. It is **not** a treatment for PTSD, and no specific claim should be made that any individual puzzle type is validated for symptom reduction — these are optional, low-stakes attention-shifting activities, framed exactly that way throughout the UI and copy.

**Required framing language pattern (to reuse verbatim in-app, adapted to house style):** "This is a quick attention-shifting activity, not a treatment. Some people find it helps interrupt racing thoughts or panic in the moment. If it's not helping, try Breathe or Ground Me instead, or reach Get Support."

## Puzzle types

All are original implementations — no assets or exact mechanics copied from any third party.

### 1. Pattern Recognition
Find the one tile among a grid that breaks a simple visual pattern (color/shape/rotation sequence). Low verbal load, works well one-handed.

### 2. Visual Search
"Find the [N] odd-ones-out" among a field of similar shapes/icons (original iconography), timed-optional. Classic, well-understood attention-capture mechanic.

### 3. Counting
Count occurrences of a specific simple shape/color within a cluttered field, enter the number. Very low cognitive demand — good for higher-distress states.

### 4. Sequencing
Tap a small set of items (numbers, or simple original icons) in ascending/given order. Slightly higher working-memory demand — better suited to lower-distress, more "I want to occupy my mind for a bit" moments.

### 5. Categorization
Sort a small set of original icons into 2–3 labeled buckets by an obvious shared attribute (color, shape, size). Light executive-function engagement.

### 6. Object Identification
"Which of these doesn't belong" — one item among a set of otherwise-similar original icons is subtly different (a grounding-adjacent variant of visual search, slightly more analytical).

### 7. Simple Memory
Brief "watch then recall" — a short sequence of positions/colors flashes, then the user reproduces it. Kept intentionally short (3–5 items) so it stays a light distraction, not a frustrating memory test.

## Difficulty

Each puzzle type has 3 tiers (Easy/Standard/Focused), controlling grid size, item count, and time pressure (time pressure always optional — never a hard fail state). Default difficulty is Easy on first use and for any FOCUS quick-access entry; a user can opt up. Difficulty never auto-escalates based on performance — this is not a game with a score-chasing loop, it's a coping tool, and pressure/failure states run counter to its purpose.

## Duration

Each puzzle instance targets 30–90 seconds. No puzzle should require more than 2 minutes even at Focused difficulty. A visible, unobtrusive "I'm done" exit is always present — never a forced completion requirement.

## UI

- Full-screen, minimal chrome, large tap targets (one-handed friendly).
- No score, no leaderboard, no streaks, no achievement badges — this explicitly is not gamified in a way that could create pressure or compulsive-use dynamics; the goal is a calm few minutes, not engagement-maximization.
- A single, calm "Done" / "Another one" choice at the end of each round.
- Persistent small "Get Support" link, consistent with every other quick-access destination.
- Muted, low-saturation original color palette and simple shapes (avoid high-arousal bright/flashing visuals, which can be counter-therapeutic for an anxious or hyperaroused user).

## Accessibility
- All puzzles playable with large touch targets (min 48dp) and without fine motor precision (generous hit-boxes).
- Color is never the sole differentiator (pair with shape/pattern) for colorblind accessibility.
- No flashing/strobing effects (photosensitivity safety).
- Full screen-reader labeling for anyone using assistive tech, even though the visual-search/pattern puzzles are inherently visual (an audio-based alternative, e.g. Counting via sound cues, can be offered as an accessible variant for visual-search-family puzzles).
- No mandatory time pressure at any accessibility setting.

## Randomization
Each puzzle instance is procedurally generated from simple parameter ranges (grid size, item count, target count) rather than a fixed content bank, so repeated use doesn't become memorized/stale. A seedable generator supports deterministic testing (Tests section below) while remaining random in production.

## State

No persistence required beyond an optional lightweight "used a Focus activity" event for Module 02-style outcome tracking (did this help — an optional post-activity 0-10 rating, exactly like Module 01, fully skippable, feeding the same PersonalizationScorer if the user opts to rate).

```
FocusActivityEvent { id, puzzleType, difficulty, startedAt, completedAt, skipped: bool, postRating?: 0-10 }
```

## Safety

- Never the *only* option offered for a genuinely high-distress or crisis state — FOCUS activities sit alongside, not instead of, Breathe/Ground Me/Get Support in the quick-access system (see 05_ANCHOR_MODULES.md's Quick Access System section).
- No failure state, no penalty, no time-out consequence — removing any performance pressure is itself a safety design choice for this population.
- Framing copy (above) is shown once per puzzle type on first use and always available via a small "what is this for?" info affordance, never forced reading before starting.

## Tests

- Each puzzle type's generator produces a valid, solvable instance for every difficulty tier (property-based/seeded tests).
- Exit ("I'm done") is reachable from every puzzle screen at every point, including mid-round.
- No puzzle instance can be generated with zero valid solutions or with ambiguous multiple "correct" answers where only one is scored correct (avoid false-failure frustration).
- Difficulty tier correctly bounds generation parameters (Easy is never harder than Standard, etc.).
- Post-activity rating is fully optional and never blocks exit.
- Accessibility: all interactive elements meet minimum touch-target size in automated UI tests; no flashing-effect assets pass a basic flicker-rate check.
