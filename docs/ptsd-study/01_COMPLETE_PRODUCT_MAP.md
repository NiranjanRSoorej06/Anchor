# PTSD Coach — Complete Product Map

Reconstructed from hands-on exploration of PTSD Coach v3.5.4 (144), US Dept. of Veterans Affairs / National Center for PTSD, on a live Android emulator. This is a navigation/architecture map, not a copy of any proprietary text or art.

## Top-level architecture

The app is a **4-tab application** (Manage / Track / Learn / Support) with a **Home dashboard** layered on top as a launcher, plus a slide-out **drawer** for settings/secondary screens. Home's 2×2 grid buttons are shortcuts into the same 4 tabs — there is no separate content behind them.

```
APP
├── ONBOARDING (first-run only)
│   ├── Splash
│   ├── License Agreement (VA EULA, must accept)
│   └── Tutorial (5 pages, swipeable, Skip available)
│       ├── P1: Welcome
│       ├── P2: What the app does
│       ├── P3: Privacy by design
│       ├── P4: "Getting what you need" — self-identify as: on my own / working with provider / friend-or-family / just checking it out
│       └── P5: Veteran status (Yes / No / Prefer not to say) → START
│
├── HOME (dashboard, always reachable via drawer "Home" or the Home icon on any tab)
│   ├── Rotating inspirational quote banner (expandable)
│   ├── 2×2 grid → shortcuts into the 4 tabs below
│   │   ├── MANAGE SYMPTOMS  → Manage tab
│   │   ├── TRACK PROGRESS   → Track tab
│   │   ├── LEARN            → Learn tab
│   │   └── GET SUPPORT      → Support tab
│   ├── CRISIS RESOURCES (quick tile → full Crisis Resources screen)
│   ├── ADD A FAVORITE (quick tile → tool picker)
│   ├── FAVORITE TOOLS (horizontal carousel, paged, populated from Manage ▸ Favorites)
│   └── REMINDERS (clock icon, top-right)
│       ├── Assessment Reminder (toggle)
│       ├── Daily Inspiring Quote (toggle)
│       └── Safety Plan Reminder (toggle)
│
├── MANAGE (tab) — "Manage Symptoms"
│   ├── SYMPTOMS (sub-tab, default) — symptom-first browsing, 7 categories
│   │   ├── Unable to Sleep
│   │   ├── Worried/Anxious
│   │   ├── Sad/Hopeless
│   │   ├── Disconnected From People
│   │   ├── Disconnected From Reality
│   │   ├── Avoiding Triggers
│   │   └── Reminded of Trauma
│   │   (each → Distress Meter → a curated/rotating single TOOL from the catalog below, with "NEW TOOL" reroll)
│   ├── TOOLS (sub-tab) — master alphabetical catalog (25 tools) + "Create Tool"
│   │   ├── Ambient Sounds (14 bundled soundscapes)
│   │   ├── Body Scan (2 narrator variants, ~13 min guided audio)
│   │   ├── Change Your Perspective (cognitive-reframe card deck)
│   │   ├── Connect With Others (behavioral-activation suggestion deck)
│   │   ├── Coping Cards (user-authored structured cards: Problem Area / Emotions & Symptoms / Coping Skills)
│   │   ├── Deep Breathing (~5 min guided audio + captions)
│   │   ├── Express with Art (in-app freehand drawing canvas: pencil/pen/marker/spray)
│   │   ├── Grounding (sensory-focus prompt deck)
│   │   ├── Inspiring Quotes (browsable/quote-manager, feeds Home banner)
│   │   ├── Leisure Activities (Time Alone / In Town / In Nature suggestion decks)
│   │   ├── Mindfulness (sub-list: Mindful Breathing, Mindful Walking, Emotional Discomfort, Awareness of the Senses, Loving Kindness, Seated Practice)
│   │   ├── Muscle Relaxation (~9 min guided PMR audio, driving-safety caveat)
│   │   ├── My Feelings (structured emotion journal: primary→nested feeling→0-100 intensity→notes, dated)
│   │   ├── Observe Thoughts (ACT defusion: Clouds in the Sky, Leaves on a Stream)
│   │   ├── Positive Imagery (guided-visualization audio sub-list: Beach, Country Road, Forest, ...)
│   │   ├── Relationship Tools (sub-list: Positive Communication, Reconnect with my Partner, Make Arguments Healthier, +more)
│   │   ├── RID: Coping With Triggers (3-step Relax→Identify→Decide protocol + journaling + summary)
│   │   ├── Schedule Worry Time (worry-postponement tool + reminder toggle)
│   │   ├── Seeing My Strengths (user-authored strengths journal)
│   │   ├── Sleep Tools (sub-list: Help Falling Asleep, Good Sleep Habits [→4 CBT-I activity categories], Changing Sleep Perspectives)
│   │   ├── Soothe the Senses (5-senses self-soothing suggestion deck)
│   │   ├── Soothing Audio (bundled ambient-audio player, sibling of Ambient Sounds)
│   │   ├── Soothing Images (bundled nature-photo viewer, sibling of Personalize's slideshow)
│   │   ├── Thought Shifting (repeat-a-mantra-for-5-min deck + timer)
│   │   └── Create Tool → custom multimedia tool builder (Text/Memo/Music/Photo/Video types; Text has a rich-text editor)
│   └── FAVORITES (sub-tab)
│       ├── Favorited Tools (manually starred, drag-to-reorder)
│       ├── Tools That Have Helped (AUTO-populated from Distress Meter outcome history, e.g. "Ambient Sounds — Helps a lot")
│       └── ADD A FAVORITE (picker)
│
├── TRACK (tab) — "Track Progress"
│   ├── TRACK PTSD SYMPTOMS (full 20-item, 5-point Likert PCL-5-style assessment) → Assessment History (score-over-time graph + list, retake CTA)
│   ├── PTSD SCREEN (brief 1 gate + 5 item PC-PTSD-5-style screener) → Results (score, interpretation, "Understand PTSD"/"Get Help" sections, ALWAYS-ON crisis-line footer) → its own Assessment History
│   └── MY JOURNAL (free-text journal, on-device only, privacy-labeled)
│
├── LEARN (tab)
│   ├── LEARN ABOUT PTSD (article pager: What is PTSD?, PTSD Facts, How does PTSD develop?, How common is PTSD?, Who develops PTSD?, How long does PTSD last?, Problems related to PTSD)
│   ├── GETTING PROFESSIONAL HELP (article pager: I'm in Crisis [safety-critical], Treatment Locator, Finding Treatment for Alcohol or Drugs, Finding a Therapist, What is Counseling (Therapy)?, Tools for PTSD, Do I Need Professional Help?)
│   └── PTSD AND THE FAMILY (article pager, not fully catalogued)
│
├── SUPPORT (tab) — "Get Support"
│   ├── CRISIS RESOURCES → full Crisis Resources screen (see SAFETY below)
│   ├── FIND PROFESSIONAL CARE → branch: Veteran resources / Civilian resources → provider-search links
│   ├── GROW YOUR SUPPORT (article pager: Someone You Trust, Feeling Alone, Growing Your Support)
│   └── PERSONAL SUPPORT CONTACTS (list; Add New Contact → OS-contacts-backed picker/creator)
│
├── SAFETY (cross-cutting, reachable from Home tile, Support tab, Learn article, and its own drawer entry)
│   ├── CRISIS RESOURCES (canonical full screen): 911, Call 988 (+ "press 1 for Veterans"), VCL Text, VCL Chat, National Domestic Violence Hotline, National Sexual Assault Hotline, inline "Safety Plan" CTA, inline "Personal Support Contacts" CTA
│   └── SAFETY PLAN (drawer) — Stanley-Brown 6-step model
│       ├── First-run tutorial (6 pages)
│       ├── Hub: My Safety Plan / Get Support Now / Set Reminders
│       └── My Safety Plan (6 swipeable steps, each a chip-entry list, "Export and share", persistent Call-988 button)
│           1. Signs I Should Use My Plan
│           2. Ways I Can Cope On My Own
│           3. Social Distractions
│           4. Family & Friends I Can Call
│           5. Professionals I Can Call
│           6. Keeping Myself Safe
│
└── SETTINGS / SECONDARY (drawer)
    ├── Home
    ├── How To Use This App (static onboarding recap)
    ├── Personalize
    │   ├── Choose Soothing Pictures (7 bundled photos + user-add; slideshow Settings: interval, transition)
    │   ├── Choose Soothing Songs (user's own on-device music only; empty by default)
    │   ├── Choose Assessments (toggle which assessments are offered: Track PTSD Symptoms / PTSD Screen)
    │   ├── Choose Support Contacts (same OS-contacts-backed flow as Support tab)
    │   ├── Inspiring quotes on home screen (toggle)
    │   ├── Distress Meter (toggle — globally enables/disables the pre/post rating wrapper)
    │   └── Language (picker)
    ├── Manage Data (Export Data, Delete Data, Anonymous Usage Data toggle, Reset Application)
    ├── Privacy Policy
    ├── Safety Plan (see above)
    ├── Inspiring Quotes (same screen as Manage ▸ Tools ▸ Inspiring Quotes)
    ├── About PTSD Coach (About / Citation & Project Team / Partners / Credits / Other Mental Health Apps)
    ├── Send Us Feedback (feedback email CTA + user-testing volunteer CTA)
    └── Share This App (OS share sheet)
```

## Key architectural observations

1. **Distress Meter is a universal wrapper**, not a per-tool feature. Any tool opened via a Symptom category, and most tools opened directly from the Tools catalog, are bookended by a pre-rating → tool → post-rating → reinforcement-feedback loop. It can be globally disabled in Personalize.
2. **Two parallel personalization tracks** exist and are easy to conflate: manually "Favorited Tools" (user hearts a tool) vs. automatically-tracked "Tools That Have Helped" (derived from Distress Meter deltas). Anchor should design both explicitly.
3. **User-generated-content tools** (Coping Cards, My Feelings, Seeing My Strengths, Express with Art, My Journal, Create Tool) all share one interaction pattern: intro copy → "+ Add" → structured or freeform entry → save → persisted, re-visitable list. This is a reusable component, not five different features.
4. **Crisis Resources is a single canonical screen** reused from Home, the Support tab, and (in a lighter subset) a Learn article — not five different implementations.
5. **Support Contacts has no independent data model** — it is a thin wrapper around the Android OS Contacts app (permission request → "Pick from contact list" / "Create new contact" bottom sheet → native contact editor). Anchor should NOT copy this; an isolated in-app contact list is better for privacy and portability.
6. **The Safety Plan follows the published Stanley-Brown Safety Planning Intervention** exactly (6 steps in the standard order), which is worth knowing precisely because Anchor already has its own SafetyPlan feature that should be checked against this canonical structure rather than redesigned from scratch.
