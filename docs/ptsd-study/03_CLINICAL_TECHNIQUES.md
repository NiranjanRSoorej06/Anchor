# Clinical Technique Database

For every technique found in PTSD Coach, this document separates three distinct things, as required:
1. **PTSD Coach feature** — what the app actually does (observed hands-on).
2. **Underlying clinical technique** — the named psychological technique it is drawing on.
3. **Evidence** — independent summary of what the research literature actually supports, at a general level. This is not a systematic review; treat it as a reasonable clinical orientation, not a citation-checked reference list. Where evidence is strong and specific (e.g., named validated instruments, named evidence-based protocols), that is stated plainly; where an app feature is a simplified/informal version of a technique, that is stated plainly too — a coping tool "inspired by" CBT is not itself a validated CBT intervention.

---

# Diaphragmatic / Paced Breathing

## What it is
Slow, deep, controlled breathing intended to downregulate physiological arousal (activating the parasympathetic nervous system).

## PTSD Coach implementation
"Deep Breathing" tool: ~5-minute audio-guided track with synchronized captions and a minimal static visual pacer. Passive — the user presses play and follows along; no interactive pacing control.

## Target symptoms
Physiological hyperarousal, anxiety, acute distress spikes.

## User interaction
Intro screen (headphones/quiet space recommendation) → play a fixed-length audio track → optional Add-to-Favorite → Distress Meter closes the loop.

## Duration
~5 minutes, fixed.

## Evidence
Slow-paced breathing techniques have a substantial general body of support for reducing acute physiological arousal and self-reported anxiety in the short term (via vagal/parasympathetic mechanisms); they are a standard component of many anxiety and PTSD treatment protocols as an adjunct coping skill rather than a stand-alone cure. Evidence is strongest for acute-state anxiety reduction; evidence for lasting symptom change from breathing exercises alone (without broader trauma-focused treatment) is much weaker.

## Safety considerations
PTSD Coach explicitly warns some breathing/relaxation exercises are unsafe while driving (see Muscle Relaxation). Hyperventilation-style breathing can occasionally increase distress or dissociation in some trauma survivors; a "stop if it feels wrong" affordance is good practice.

## Anchor implementation opportunity
Anchor's existing haptic breathing pacer is architecturally superior to this (interactive, adjustable pace, not a fixed audio track) — extend rather than rebuild.

## Recommended evidence-based improvements
Offer adjustable pacing (e.g., 4-7-8, box breathing, extended-exhale), visible/haptic real-time feedback, and an explicit distress check before/after (already an Anchor pattern to preserve).

---

# Progressive Muscle Relaxation (PMR)

## What it is
Sequentially tensing and releasing each major muscle group, developed by Edmund Jacobson, to reduce somatic tension and teach interoceptive awareness of tension vs. relaxation.

## PTSD Coach implementation
~9-minute guided audio track walking through muscle groups; explicit instruction to sit/lie down and an explicit warning not to do the exercise while driving.

## Target symptoms
Somatic tension, hyperarousal, difficulty sleeping.

## User interaction
Intro (safety framing) → Continue → guided audio player (same pattern as Deep Breathing) → Distress Meter.

## Duration
~9 minutes, fixed.

## Evidence
PMR has a long-standing, fairly robust general evidence base for reducing self-reported anxiety and muscle tension across a range of populations, and is a common component of relaxation-training protocols used alongside trauma-focused therapies. It is considered an adjunct/coping-skill technique, not a primary PTSD treatment on its own.

## Safety considerations
Explicit driving contraindication (correctly implemented in-app) — any eyes-closed/immersive exercise should carry this warning. Not recommended for use in situations requiring alertness (operating machinery, etc.).

## Anchor implementation opportunity
Anchor doesn't appear to have PMR; this is a plausible new module reusing existing guided-audio/haptic infrastructure, with the driving-safety copy carried over.

## Recommended evidence-based improvements
Interactive version (haptic cue per muscle group instead of only audio) could increase engagement versus a passive track.

---

# Mindfulness-Based Practices (Body Scan, Mindful Breathing/Walking, Awareness of the Senses, Loving Kindness, Seated Practice)

## What it is
A family of present-moment-attention practices adapted from Mindfulness-Based Stress Reduction (MBSR) and related traditions.

## PTSD Coach implementation
"Body Scan" (2 narrator options, ~13 min) plus a "Mindfulness" sub-list of 6 further guided-audio practices, all following the same audio-track format as the breathing/relaxation tools.

## Target symptoms
Dissociation, rumination, general emotion dysregulation, hyperarousal.

## User interaction
Sub-list → pick a practice/narrator → intro → guided audio player → Distress Meter.

## Duration
7–13 minutes typically, fixed per track.

## Evidence
Mindfulness-based interventions have a substantial general evidence base for reducing anxiety, depressive symptoms, and rumination, and mindfulness-based approaches are used as adjuncts in some PTSD treatment programs. However, brief single-session audio-guided practices inside a self-help app are a much lighter-weight, less-studied form than structured multi-week MBSR programs — treat this as "may help in the moment," not "clinically equivalent to MBSR."

## Safety considerations
Some trauma survivors experience increased distress or dissociation with eyes-closed, body-focused, or silence-heavy mindfulness practices; offering a clear opt-out/stop path and briefer options is good practice.

## Anchor implementation opportunity
Complements Anchor's existing CameraX/ML Kit grounding (which is action-based/attention-shifting) with a passive-audio option for users who prefer stillness. Should be a distinct, separately-offered modality, not a replacement.

## Recommended evidence-based improvements
Offer very short (60–90 second) mindfulness "micro-practices" for users who can't commit to 10+ minutes during acute distress, and route users toward grounding-first (active/attentional) rather than mindfulness-first (passive/interoceptive) options for dissociation, per typical trauma-informed sequencing guidance.

---

# Cognitive Reframing / Restructuring ("Change Your Perspective," "Thought Shifting")

## What it is
Identifying and replacing unhelpful automatic thoughts with more balanced/accurate alternatives — a core CBT technique.

## PTSD Coach implementation
Browsable card decks of pre-written alternative thoughts (e.g., "This is a hassle, not a horror") or repeatable mantra-style statements, with no personalization of the actual thought content in these particular tools (contrast with Coping Cards, which is personalized).

## Target symptoms
Catastrophizing, anxious/negative automatic thoughts, low mood.

## User interaction
Browse deck via Previous/Next, optionally set a 5-minute repeat timer (Thought Shifting).

## Duration
Seconds per card; Thought Shifting suggests a 5-minute repeat practice.

## Evidence
Cognitive restructuring is one of the most extensively studied and evidence-supported components of CBT for anxiety, depression, and PTSD (e.g., within Cognitive Processing Therapy). However, a static deck of generic pre-written alternative thoughts is a much shallower implementation than the actual technique, which normally involves identifying the user's own specific distorted thought and working through evidence for/against it. This app feature is best understood as a "quick generic reframe prompt," not a full cognitive-restructuring exercise.

## Safety considerations
Generic positive statements can feel dismissive if a user is in acute crisis; should never substitute for actual safety-plan or crisis-resource access when distress is high.

## Anchor implementation opportunity
A genuinely stronger version is achievable: a guided, personalized thought-record flow (situation → automatic thought → evidence for/against → balanced thought) rather than a static card deck — closer to the real CBT technique than PTSD Coach's own implementation.

## Recommended evidence-based improvements
Move from static decks toward a lightweight guided thought-record; keep quick static cards as an optional "in a hurry" fallback.

---

# Cognitive Defusion ("Observe Thoughts": Clouds in the Sky, Leaves on a Stream)

## What it is
An Acceptance and Commitment Therapy (ACT) technique for creating psychological distance from thoughts by visualizing them as passing objects, rather than trying to challenge or suppress them.

## PTSD Coach implementation
Two ~7-minute guided-audio metaphor exercises, same audio-track format as other guided practices.

## Target symptoms
Intrusive thoughts, rumination, thought-suppression struggles.

## User interaction
Intro → guided audio → Distress Meter.

## Duration
~7 minutes.

## Evidence
ACT and cognitive defusion techniques have a reasonably solid general evidence base for reducing distress associated with unwanted thoughts, particularly by reducing struggle/avoidance rather than trying to eliminate the thoughts themselves; ACT-based interventions are used in some PTSD-adjacent treatment contexts. As with mindfulness above, a single guided audio session is a lighter-weight application than a structured multi-session ACT protocol.

## Safety considerations
None specific beyond general guided-audio cautions.

## Anchor implementation opportunity
New module; good complement to grounding/breathing for users whose main struggle is unwanted thoughts rather than physiological arousal.

## Recommended evidence-based improvements
Offer a short defusion exercise (60–90 sec) alongside the full-length one for acute moments.

---

# Behavioral Activation ("Connect With Others," "Leisure Activities," "Relationship Tools")

## What it is
Increasing engagement in valued, rewarding, or socially connecting activities to counteract avoidance and low mood — a core component of Behavioral Activation therapy for depression, relevant to PTSD's social-withdrawal and anhedonia symptoms.

## PTSD Coach implementation
Suggestion-card decks (e.g., "Call a family member," "Bake something," "Show Love — tell your loved one one thing you appreciate about them") browsed via Previous/Next; generic, not personalized to the user's actual relationships.

## Target symptoms
Disconnected From People, Sad/Hopeless, social withdrawal, anhedonia.

## User interaction
Browse deck, optionally act on suggestion outside the app.

## Duration
Seconds to browse; the activity itself is external to the app.

## Evidence
Behavioral activation has strong general evidence support for depression and is commonly incorporated into PTSD treatment given the high overlap between PTSD's avoidance/numbing symptoms and depressive withdrawal. The evidence supports the *principle* (do more valued/rewarding activity); a static generic suggestion card is a much smaller-scale nudge than a structured BA program with activity scheduling and monitoring.

## Safety considerations
None specific.

## Anchor implementation opportunity
Wiring this to Anchor's real Support Contacts (tap-to-call/text an actual saved person) rather than a generic "call a family member" card is a clear, low-effort improvement over PTSD Coach's own implementation.

## Recommended evidence-based improvements
Personalize suggestions using the user's own saved contacts/activities/values where available; track follow-through as a light activity log.

---

# Grounding Techniques (sensory-focus prompts; "RID: Identify" step)

## What it is
Directing attention to present-moment sensory input (touch, sight, sound, etc.) to counter dissociation, flashbacks, and panic — widely used in trauma-informed care.

## PTSD Coach implementation
"Grounding" tool: single-sense-focus prompt deck (e.g., feel feet on floor, texture of a pebble). Also embedded inside the RID tool's "Identify" step, which adds explicit present-vs-past differentiation ("how is your current situation different from the traumatic experience?").

## Target symptoms
Disconnected From Reality (dissociation), flashbacks, panic, Reminded of Trauma.

## User interaction
Browse deck (standalone) or guided journaling (within RID).

## Duration
Seconds per prompt; RID's full flow is a few minutes.

## Evidence
Grounding techniques are near-universally recommended in trauma-informed clinical guidance for managing dissociation and flashbacks in the moment, and align with general principles of attention redirection and orienting to the present that show up across multiple trauma-treatment modalities. As a stand-alone technique, controlled-trial evidence specifically isolating "grounding exercises" is thinner than for structured therapies like PE/CPT/EMDR — it is best understood as a well-established clinical-practice tool for acute-moment stabilization, not a treatment for PTSD itself.

## Safety considerations
Appropriate first-line response to acute dissociation; should not delay access to crisis resources if the underlying trigger involves safety risk (RID handles this correctly with its embedded safety-escalation line).

## Anchor implementation opportunity
Anchor's existing CameraX/ML Kit-based grounding is a stronger, more interactive implementation of exactly this technique than PTSD Coach's static card deck — extend, don't replace.

## Recommended evidence-based improvements
Keep grounding fast (single tap to start, no multi-screen setup) since dissociation impairs the ability to navigate complex UI.

---

# Structured Post-Trigger Protocol ("RID: Relax, Identify, Decide")

## What it is
A compressed, self-administered 3-step protocol PTSD Coach built specifically for the moment right after being triggered by a trauma reminder: a brief relaxation step, a present-vs-past differentiation/identification step, and a decision step, closing with a summary recap.

## PTSD Coach implementation
Step R (Relax): 30-second on-screen countdown synced to a two-word breath mantra ("let"/"go"), extendable. Step I (Identify): psychoeducation on internal/external triggers + required 2-question journal (what triggered you / how is now different from the trauma). Step D (Decide): psychoeducation on 3 possible responses (stay and tolerate it / take a time out / seek support if in danger) + 1-question journal. Final: dated summary recap of all 3 answers.

## Target symptoms
Reminded of Trauma, acute triggering/flashback-adjacent distress.

## User interaction
Fully guided, linear, ~3–5 minutes, with mandatory-field validation on the journaling steps.

## Duration
~3–5 minutes.

## Evidence
This is best understood as a **branded self-help protocol combining well-supported component techniques** (brief relaxation/breathing + grounding + present-orientation/differentiation + safety-aware decision-making) rather than itself being a named, independently validated clinical instrument. The individual components (paced breathing, grounding, distinguishing "then" from "now") each have general support as described above; the specific 3-step packaging and its acronym are a self-help-app design choice, not a peer-reviewed protocol with its own outcome literature that we independently verified.

## Safety considerations
Explicitly and correctly built in: "If you are in danger of hurting yourself or others, seek support and don't take any chances." This is exactly the kind of embedded safety gate a trigger-response tool should have.

## Anchor implementation opportunity
High-value, buildable-with-original-branding module for Anchor's FLASHBACK quick-access entry point — reuse the *structure* (brief relax → present-vs-past differentiate → safety-aware decide → summarize), not the name "RID" or exact wording.

## Recommended evidence-based improvements
Make the safety-escalation branch a real interactive choice (tap a button, not just read a sentence) that routes directly into SafetyFilter/crisis resources if selected.

---

# Structured Emotion Journal ("My Feelings")

## What it is
A hierarchical emotion-vocabulary picker (primary emotion → nested granular emotion) plus an intensity rating and free notes, saved with a timestamp.

## PTSD Coach implementation
6 primary emotions (Angry, Joyful, Powerful, Sad, Safe, Scared) each with ~10-12 nested granular feelings; 0–100 intensity slider labeled with a qualitative band (e.g., "68% Moderate"); free-text notes; dated save.

## Target symptoms
Emotional overwhelm, alexithymia-adjacent difficulty naming feelings, general mood tracking.

## User interaction
Multi-step guided flow, ends in a recap/summary.

## Duration
~1–2 minutes.

## Evidence
"Affect labeling" (putting feelings into words) has general research support for reducing amygdala reactivity and subjective distress in the moment; more granular, precise emotion vocabulary ("emotional granularity") is associated in the broader literature with better emotion-regulation outcomes. A structured, hierarchical feelings picker is a reasonable, low-risk operationalization of both ideas.

## Safety considerations
None specific; a good candidate for routing very high intensity + certain feelings (e.g., high "Scared"/"Hopeless" + high intensity) toward a gentle safety-resources prompt, though PTSD Coach does not appear to do this automatically based on this exploration.

## Anchor implementation opportunity
One of the strongest, most portable, most clinically-defensible structures found in the whole audit — recommend building this early and considering adding a threshold-based nudge toward SafetyFilter for extreme entries (an improvement over the reference app).

## Recommended evidence-based improvements
Add the safety-threshold nudge described above; surface the resulting mood history as a simple trend alongside assessment scores (PCL-5/PC-PTSD-5) for a fuller picture over time.

---

# Structured Coping Plan ("Coping Cards")

## What it is
A simplified coping-card technique: for a chosen problem area, list the associated feelings/symptoms and the coping skills that help, so it can be consulted later when distressed.

## PTSD Coach implementation
Pick/author a Problem Area, add Emotions & Symptoms as tags, add Coping Skills as tags, save as a persisted card; multiple cards supported.

## Target symptoms
General — designed to be filled out when calm, for use across many symptom categories.

## User interaction
Structured 3-field form, chip-based tag entry.

## Duration
A few minutes to author; seconds to consult later.

## Evidence
Coping cards / written coping plans are a common, practically-oriented clinical tool (related in spirit to safety-planning and relapse-prevention planning) with general support for helping people access previously-identified strategies under stress, when executive function is reduced. This is a well-established clinical-practice pattern rather than a single named RCT-tested intervention.

## Safety considerations
None specific, though a coping card explicitly built for suicidal-crisis moments would properly be the Safety Plan (F44), not this general-purpose tool — the two should stay conceptually distinct.

## Anchor implementation opportunity
Directly buildable, low clinical risk, high everyday utility — good early module.

## Recommended evidence-based improvements
Let a Coping Card link directly to the matching Tool(s) in the catalog (e.g., a "slow breathing" coping skill tag could deep-link to Anchor's breathing module) rather than staying purely textual.

---

# Sleep Hygiene / Stimulus Control (Sleep Tools)

## What it is
Behavioral and environmental recommendations for improving sleep onset and quality — core components of Cognitive Behavioral Therapy for Insomnia (CBT-I).

## PTSD Coach implementation
3 sub-tools: Help Falling Asleep, Good Sleep Habits (branching into 4 activity categories — winding down, staying awake avoidance, what-to-do-if-can't-sleep, waking-up activities), Changing Sleep Perspectives (cognitive component for sleep-related worry).

## Target symptoms
Unable to Sleep, nightmares/sleep disruption common in PTSD.

## User interaction
Sub-list browsing, mostly static psychoeducation/suggestion content.

## Duration
A few minutes to read.

## Evidence
CBT-I has strong, well-established evidence as a first-line treatment for insomnia, including insomnia comorbid with PTSD; sleep-hygiene education is a component of CBT-I but on its own (without stimulus control, sleep restriction, and cognitive components delivered as a structured multi-week program) has much weaker evidence for meaningfully improving chronic insomnia. Static tips are a reasonable low-risk starting point, not a substitute for structured CBT-I.

## Safety considerations
None specific.

## Anchor implementation opportunity
Good candidate for a dedicated SLEEP quick-access entry point (Phase 11); could eventually incorporate real stimulus-control tracking (bed only for sleep, get up if not asleep in X minutes) rather than static tips only.

## Recommended evidence-based improvements
Layer in a simple sleep diary and stimulus-control reminders for users who want more than static tips — closer to real CBT-I.

---

# Worry Postponement ("Schedule Worry Time")

## What it is
Deliberately scheduling a specific time to engage with worries, rather than suppressing them, to reduce their intrusion at other times (including bedtime) — a technique from CBT for generalized anxiety.

## PTSD Coach implementation
Free-text "topic to think about" + a reminder toggle for the scheduled time.

## Target symptoms
Worried/Anxious, nighttime rumination, Unable to Sleep.

## User interaction
Single-field entry + optional reminder.

## Duration
Seconds to set up; the "worry time" itself happens later, outside the app.

## Evidence
Worry-time/stimulus-control-for-worry techniques have general support within CBT for generalized anxiety disorder for reducing daytime and nighttime worry intrusion, though evidence specifically for a stand-alone app reminder (vs. a full worry-time protocol including active problem-solving during the scheduled time) is thinner.

## Safety considerations
None specific.

## Anchor implementation opportunity
Simple, low-effort module; could pair with a lightweight structured worry-processing template at the scheduled time rather than just a reminder.

## Recommended evidence-based improvements
Offer a short structured prompt during the scheduled worry time itself (What's the worry? Is it solvable? If yes, one small next step; if no, a defusion/acceptance prompt) rather than only setting a reminder.

---

# Strengths-Based Journaling ("Seeing My Strengths")

## What it is
Deliberately recalling and recording one's own positive qualities and past successes, drawn from positive psychology.

## PTSD Coach implementation
Free-text "+Add Strength" persisted list, framed around boosting mood/outlook.

## Target symptoms
Sad/Hopeless, low self-worth common in PTSD.

## User interaction
Simple add-and-persist list.

## Duration
Seconds per entry.

## Evidence
Strengths-based/positive-psychology interventions (e.g., recording positive experiences, identifying character strengths) have general support for modest improvements in wellbeing and mood in the broader literature; effects are typically small-to-moderate and most robust with repeated practice over time rather than one-off use.

## Safety considerations
None specific.

## Anchor implementation opportunity
Simple, low-risk, good for a "quiet" non-crisis engagement loop; pairs well with the quote/priming feature.

## Recommended evidence-based improvements
Prompt for a strength shortly after a successfully-completed coping session (capturing "I just did something hard") to build a strengths list organically from real moments rather than requiring separate motivation to open the feature.

---

# Expressive Arts ("Express with Art")

## What it is
Using drawing/creative expression as an outlet for feelings that are difficult to verbalize.

## PTSD Coach implementation
In-app freehand drawing canvas (pencil/pen/marker/spray, undo/redo, save) with prompt ideas.

## Target symptoms
Sad/Hopeless, Reminded of Trauma, generally for feelings hard to put into words.

## User interaction
Open canvas, draw, save.

## Duration
Open-ended.

## Evidence
Expressive-arts/art-therapy approaches have general support for reducing distress and aiding emotional processing, particularly as an adjunct to verbal therapies; evidence quality varies widely across study designs and the mechanism is less mechanistically specific than techniques like PMR or cognitive restructuring. Best framed as a legitimate but supplementary self-expression outlet, not a primary treatment technique.

## Safety considerations
None specific.

## Anchor implementation opportunity
Good optional/creative module; low priority relative to the structured clinical tools above.

## Recommended evidence-based improvements
None critical; could add optional saved-gallery review over time as a light visual journal.

---

# Self-Report Assessments

## PC-PTSD-5-style brief screener ("PTSD Screen")
**What it is:** A short (1 gate + 5 item) validated primary-care screening tool for probable PTSD, developed by the VA National Center for PTSD (the real-world PC-PTSD-5 is a public-domain, validated instrument).
**PTSD Coach implementation:** Faithful digitization — trauma-exposure gate question with an "Examples" expandable, then 5 Yes/No items ("In the past month, have you..."), summed score, plain-language interpretation, explicit non-diagnostic disclaimer, and a universal crisis-resources footer regardless of score.
**Evidence:** The PC-PTSD-5 is a validated, published screening instrument with established sensitivity/specificity for identifying probable PTSD in primary-care-style settings; it is a screener, not a diagnostic tool, and the app's own copy correctly says so.
**Safety consideration:** Always showing crisis resources on the results screen (not just for high scores) is a strong, worth-replicating pattern, since this brief instrument has no dedicated suicidal-ideation item to gate on.
**Anchor implementation opportunity:** Anchor already has PCL-5 infrastructure per its architecture — extend with a brief-screener variant using this same universal-safety-footer pattern.

## PCL-5-style full assessment ("Track PTSD Symptoms")
**What it is:** A 20-item, 5-point Likert-scale instrument matching the structure of the real-world PCL-5 (PTSD Checklist for DSM-5), a validated, widely-used self-report measure of PTSD symptom severity.
**PTSD Coach implementation:** Faithful digitization — "In the past month, how much were you bothered by: [item]" with Not at all / A little bit / Moderately / Quite a bit / Extremely, 20 items, scored and graphed over time in Assessment History.
**Evidence:** The PCL-5 is one of the most widely validated and used PTSD symptom-severity measures in both research and clinical practice; repeated administration over time is a standard, evidence-supported way to monitor symptom trajectory.
**Safety consideration:** No explicit item-level safety gate was observed during this pass on the 20-item version itself (only confirmed on the brief screener's results screen); Anchor should make sure ITS PCL-5 implementation surfaces crisis resources on every results screen regardless of instrument, not only on a brief-screener variant.
**Anchor implementation opportunity:** Anchor already has PCL-5 per its architecture — this confirms the reference implementation pattern (graphed history, retake CTA, plain-language, non-diagnostic framing) to extend the existing UI toward, not evidence that a new instrument needs to be built.

---

# Safety Planning ("Safety Plan")

## What it is
The Stanley-Brown Safety Planning Intervention: a widely-adopted, structured 6-step brief intervention for suicide-risk management, developed by Barbara Stanley and Gregory Brown.

## PTSD Coach implementation
Faithful 6-step digitization: (1) Signs I Should Use My Plan (warning signs), (2) Ways I Can Cope On My Own, (3) Social Distractions, (4) Family & Friends I Can Call, (5) Professionals I Can Call, (6) Keeping Myself Safe (means restriction). Plus export/share, a persistent Call-988 button on every step, an explicit "it's ok to skip steps" reassurance, and reminder scheduling to revisit the plan.

## Target symptoms
Suicidal ideation/crisis risk (broader than any single PTSD symptom category).

## User interaction
6-page tutorial → hub → swipeable 6-step editable plan.

## Duration
App's own copy: "about 30 minutes to complete," revisited over time.

## Evidence
The Safety Planning Intervention has a solid and specific general evidence base — including studies showing associations with reduced suicidal behavior and increased engagement with follow-up care when used in clinical settings (e.g., emergency departments). This is one of the strongest, most specifically-evidenced techniques in the entire app, precisely because PTSD Coach implemented the real, named, published protocol rather than an informal variant.

## Safety considerations
This *is* the core safety feature; the "it's ok to skip steps," persistent crisis-line access, and export/share-with-a-provider affordances are all good, worth-replicating design choices.

## Anchor implementation opportunity
Anchor already has SafetyPlan — verify it matches this exact 6-step canonical order and extend it with export/share and the persistent-988-equivalent-button pattern, rather than redesigning the underlying model.

## Recommended evidence-based improvements
Ensure Anchor's equivalent hotline is India-appropriate (e.g., KIRAN 1800-599-0019, iCall, Vandrevala Foundation, or 112 for emergencies) and reachable with one tap from every step, matching PTSD Coach's persistent-988-button pattern.
