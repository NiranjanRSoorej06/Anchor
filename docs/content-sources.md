# Content Sources: Exercises, Phrases, Communities (2026-09-05)

Rule: techniques are public-domain clinical knowledge — adapt protocols,
write all wording ourselves. Never copy handouts/worksheets verbatim
(Najavits/Guilford, Therapist Aid, Linehan/DBT materials all copyrighted).

## Exercise protocols to adapt from
- **5-4-3-2-1 grounding:** Najavits *Seeking Safety* Topic 4 framework
  (mental/physical/soothing types); VA NCPTSD grounding pages (US gov,
  public domain); PTSD UK variations. Write: simplified sensory-scan +
  3-step acute variant; skip smell/body cues if triggering.
- **Breathing:** box 4-4-4-4, diaphragmatic 4–6/min (PMC5455070, VA Whole
  Health), extended exhale 1:2, coherent 5-5, 4-7-8 for sleep. Ratios are
  public domain. Include caveat: skip holds if distressing.
- **PMR:** Jacobson 1929/1938 = public domain (archive.org); VA Whole Health
  PMR handout = US gov public domain. 14 groups, tense 5s → release.
- **Sleep hygiene:** Irish et al. 2015 checklist items; SF Gov CBT-I handout;
  Sleep Foundation. Concepts only, own checklist. Frame as
  psychoeducation, never treatment.
- **Sleep diary:** field set (bedtime, sleep latency, awakenings, final wake,
  out-of-bed time, quality, nightmare flag) and the two derived metrics
  (total sleep time, sleep efficiency = TST / time-in-bed) are the CBT-I
  standard — SF Gov CBT-I handout, Consensus Sleep Diary (Carney et al.
  2012). Field labels and all copy are ours. Ships as `domain/sleep` +
  `ui/tools/SleepScreen`. No sleep-restriction prescription is computed
  (needs clinician titration) — see `evidence.md` §15.
- **CBT-I psychoeducation:** `domain/content/SleepEducation` — "CBT-I is
  first-line for chronic insomnia, delivered by a therapist" (AASM 2021,
  doi:10.5664/jcsm.8986). Nightmares routed to professional care, IRT named
  but never delivered (multi-session clinical treatment).
- **Coping statements:** Beck/CPT/SIT categories; Mood Juice NHS examples
  ("I can deal with this", "These feelings will pass", "I'm safe now").
  Short phrases aren't copyrightable; write 15–20 across safety /
  temporal / capability categories.
- **DBT TIPP/ACCEPTS/self-soothe:** paraphrase steps only (Guilford
  enforces). Acronyms usable; handout language not.
- **Self-tap/EFT-style:** include ONLY with prominent caveat
  (evidence-adjacent, Church 2018 CC BY 4.0, not first-line).

## Bundled safety phrases (24 defaults + custom)
Present-tense, ≤25 words each, no trauma narrative, no clinical claims.
Anchored to Najavits grounding + Mood Juice NHS + VA guidance:
"You are safe right now." / "This feeling will pass." / "You are here, in
this moment." / "Your feet are on the ground." / "You have survived this
before." / "You are not in danger right now." / "Breathe. You are in
control." / "Name where you are and what day it is." / "This is a feeling,
not a fact." / "You are allowed to take up space." / "You are doing the
best you can." / "It's okay to feel what you're feeling." / "The worst
part is over." / "You are stronger than you think." / "Right now, you are
okay." / "You are not alone in this." / "This moment will not last
forever." / "You deserve kindness, especially from yourself." / "You have
gotten through hard things before." / "You are allowed to rest." /
"Your body is here, and it is safe." / "One breath at a time." /
"You are more than what happened to you." / "It takes courage to ask
for help."
Custom recordings: max ~15s, present tense, no narrative clause
("I was attacked but…" banned → "I am safe right now"), preview +
confirm before enabling.

## "You are not alone" section (order: crisis → action → planning → peers)
1. Crisis helplines (bundled, offline — already spec'd).
2. Maps button: `geo:0,0?q=<encoded>` ACTION_VIEW intent, prefer
   com.google.android.apps.maps with resolveActivity fallback.
   Presets: "mental health clinic near me", "psychiatrist near me",
   "counselling services near me". NO location permission needed (Maps
   handles its own); NEEDS INTERNET for search — badge "online only".
3. Therapist directories (online-badged): TheMindClan, TherapyMantra
   trauma page, CPTSD.in/find-help.
4. Peer communities (online-badged + caveats): r/ptsd (~137K, modded),
   r/CPTSD (~464K, venting-heavy), r/CPTSDNextSteps (recovery-only),
   7 Cups (volunteer listeners, not therapists), ShareWell groups,
   NAMI (US), Discord servers (small, peer-modded, links rot —
   "browse when stable, not mid-crisis"), India: Live Love Laugh
   helplines/directory, Sangath tele-counselling, TheMindClan groups.
   Caveat every peer entry: not professional advice, may trigger.

## Sources
- VA Whole Health PMR (public domain): https://www.va.gov/WHOLEHEALTHLIBRARY/docs/Progressive-Muscle-Relaxation.pdf
- Jacobson 1929 (public domain): https://archive.org/details/progressiverelax0000jaco
- Mood Juice NHS: https://www.moodjuice.scot.nhs.uk/posttrauma.asp
- Maps intents: https://developer.android.com/guide/components/google-maps-intents
- FindAHelpline India: https://findahelpline.com/countries/in
- r/ptsd: https://reddit.com/r/ptsd · r/CPTSD: https://reddit.com/r/CPTSD
- 7 Cups: https://www.7cups.com · TheMindClan: https://themindclan.com
- Live Love Laugh helplines: https://www.thelivelovelaughfoundation.org/find-help/helplines
