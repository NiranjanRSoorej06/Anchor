# complaints.md — PTSD Coach Negative Reviews (raw + synthesis base)

> Source: user-pasted Play Store reviews. Saved verbatim for brainstorm testing.
> Convention: Do we solve this? -> will map to features in brainstorm.

## Raw complaints (verbatim batch 2026-09-04)

1. Expected doctor/coach contact, got robotic programs. Offers nothing more than quick google, even less.
2. Pushes crisis line + forces quiz repeatedly = chore. (297 helpful)
3. Combat-veteran specific. Techniques/prompts = ambushes, bombings. CPTSD / civilian PTSD not useful. (211 helpful)
4. App doesn't work: stuck on logo, back button breaks. Try digging ditches.
5. Hasn't worked since last update, won't open. Used to shuffle/mix pictures, option removed.
6. Evaluation questions not fully displayed on Android.
7. Requires media, contacts, storage to function. Refuse -> can't use. (47 helpful)
8. Permissions concern: universal data, mic/vid, no HIPAA transparency, telemetry unclear, not open-source. (145 helpful)
9. Prompts assume money to spend: "go shopping" with what money?!
10. Gave me panic attack, didn't enjoy layout, not ready to delve alone. Why in therapy.
11. Mandatory photos/storage/media -> uninstalled. (23 helpful)
12. Journal not writeable, things wouldn't install, says "go seek help every 2 mins" — give me peace. Had outburst.
13. Sounds still play after exit.
14. Unless I'm veteran can't download. Have C-PTSD. B.S.
15. Coping = just listening to calming music 10 mins, can't do that at work. (7 helpful)
16. Predictable, useless. / Even more angry/anxious after using. Worthless for my PTSD as Army Veteran.
17. Not replacement for treatment but no bridge to real help.
18. Too simple, kindergarten grounding stuff. Who does this work for? Patronizing.
19. Being FEMALE listed as risk factor — alienating.
20. Informational, don't help. Not sure what it's trying to do. Mostly just directs to helplines, not interactive.
21. Crashes, won't download, error midway.
22. No reason to access contacts, location, mic, photos/media. (21 helpful)
23. Got CPTSD, did not help, deleted. Only deals with vets, need child abuse / severe trauma coverage.
24. Sharing specific info required to operate, not necessarily needed.
25. Targeted to military veterans, civilians won't find useful. / Figured tied to VA, it's not. / Only for VA/military, PTSD affects everyone not just VA.
26. Enough in life without helpful app wanting permissions to everything.
27. Blows funds on BS apps to say at press hearings they've done something.
28. Absolute waste, only pissed me off more. Cancelled appointment, jerk behavior.

## Emergent clusters (for feature brainstorm — DRAFT)
- **A. Trust / Privacy:** excessive permissions (contacts, storage, mic, location), no transparency, HIPAA fears, data mining fears.
- **B. One-size combat-centric:** veteran/US combat language, excludes CPTSD, civilian trauma, child abuse, cancer, homelessness, women, non-US.
- **C. Robotic / Generic:** google-able tips, broad strokes, predictable, kindergarten grounding, 10-min music not work-compatible, not interactive.
- **D. Friction / Nagging:** forced quizzes, crisis-line spam every 2 mins, no peace, journal broken, sounds persist after exit.
- **E. Reliability:** won't open, stuck logo, crashes, Android layout broken, update broke it, download errors.
- **F. No human bridge:** expected doctor/coach contact, no real referral, not replacement for therapy but no handoff.
- **G. Cost / Context-blind:** suggestions need money, time, quiet space — ignores work, poverty, real context.
- **H. Safety:** gave panic attack, made angrier/anxious, alone-delving unsafe.

## Test lens for our PTSD app (to fill after user idea drop)
- [ ] Does it work for non-veteran, non-US, CPTSD, civilian trauma?
- [ ] Does it ask minimal permissions + explain why + offline-first?
- [ ] Is it interactive / adaptive, not generic list?
- [ ] Can it de-escalate in <60s at work without audio / without money?
- [ ] Does it avoid quiz/crisis-line spam while staying safe?
- [ ] Does it bridge to human (doctor/coach/trusted contact) when wanted?
- [ ] Is it stable, fast, Android-friendly?

## Literature backing (peer-reviewed, 2026-09-06 research pass)

Maps verbatim Play complaints above to published evidence. Full per-paper
brief held in session; key citations here.

- **Harms are measured, not anecdotal (→ H):** Hensler 2022 RCT (n=71,
  doi:10.2196/31419): 51% reported ≥1 negative reaction — unfulfilled
  expectations 20%, unmotivating 18%, confusing 11%, increased stress 10%,
  distressing memories 8%, anxiety 7%, symptom deterioration 7%. No increased
  suicidality. Follow-up Hensler 2023 (doi:10.1016/j.invent.2023.100618): 43%
  any negative effect at 9 months. Cernvall 2018 pilot: users questioned
  whether unsupervised use could be harmful.
- **Freezing mid-crisis (→ E):** Strodl 2020 clinicians (n=63,
  doi:10.1016/j.invent.2020.100333): *"the app actually froze, and if I was
  someone in the process of being triggered, I probably would have smashed
  the phone"*; *"once I added three photos and the whole app crashed."*
  Low frustration tolerance warning: users *"could well pelt a phone across
  the room, just trying to find things."*
- **Militarized tone (→ B):** Shakespeare-Finch 2020 ADF study (n=53,
  doi:10.2196/18447): *"very militarized"* tone; *"as soon as you get out you
  will not be told what to do"* (authoritarian); icon stigma in public;
  *"I don't know where it went. Just disappeared"* (histories lost on
  Android). Strodl 2020: *"when you are serving you follow orders, however
  when you are not, you baulk at orders."*
- **History loss confirmed (→ E):** workshop-observed assessment histories
  vanishing on Android (Shakespeare-Finch 2020); matches verbatim review #5.
- **Human > app (→ F):** Bröcker 2024 SA qualitative (n=25,
  doi:10.1080/20008066.2024.2298612): *"the app did not help (instead) the
  people encouraged me to talk"*; *"exhausting with the exercises"*; wanted
  more counselling. Possemato 2023 RCT (N=234,
  doi:10.1007/s11606-023-08130-6): clinician-supported arm superior on
  self-reported PTSD (d=0.28) + 74% more sessions + higher satisfaction.
- **iOS/Android split (→ E):** Owen 2015 (doi:10.2196/mental.3935, 156
  reviews): iOS 89% positive / 4.6★ vs Android 42.7% / 3.1★; Android 46.6%
  reported tech problems vs iOS 7.5%. One user's difficulties caused
  increased distress.
- **Retention cliff (→ D):** Hallenbeck 2022 v3.1 (doi:10.2196/34744,
  ~150k users): mean 3 visits / 3 days / 18 min total; 87% day 1 → 0.69%
  at 12 months; only 2.02% high-engagement. Post-tool distress drop only
  −1.38/10 (smaller than v1's ~2).
- **Satisfaction ceiling (→ C/D):** Reyes 2025 (n=164,
  doi:10.1037/pro0000633): helpful 90.2%, easy 69.5%, recommend 90.9% —
  yet usage still collapses; liking ≠ returning. Rodriguez-Paras 2017:
  SUS 66.25 (below "good" 68); color scheme + personalization deficits.
- **Newest signal (2025–26):** Senti 2025 UX interviews drove v4.0 redesign
  (doi:10.5281/zenodo.15851443); Pacella-LaBarbara 2026 ED-injury
  qualitative (PMCID:PMC12958468): users want voice-overs, onboarding
  personalization, text reminders, in-person linkages; 1/3 barely used it.
  Possemato 2026 VHA implementation: 348 patients, d=−0.37, 90% clinician
  adoption — supported model scales in-system.
- **Privacy split (→ A):** Strodl 2020: distrust of gov data-sharing vs
  Mozilla 2022: no identifiable data leaves device. Lesson: architecture
  isn't enough — the *perception* needs designing (our offline story must
  be visible, not just true).
