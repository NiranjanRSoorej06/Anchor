# PTSD Coach — Competitive Teardown for Hackathon

> **Purpose**: Factual analysis of VA's PTSD Coach to identify exploitable gaps for a superior, inclusive civilian PTSD app.
> **Date**: 2026-09-04 | **Sources**: VA official sites, peer-reviewed RCTs, Play Store/App Store listings, Mozilla privacy review, Congressional testimony.

---

## 1. What Is PTSD Coach

| Attribute | Detail |
|---|---|
| **Developer** | VA National Center for PTSD + DoD DHA Connected Health (formerly National Center for Telehealth & Technology) |
| **First release** | 2011 |
| **Latest version** | 4.0+ (Android last major update ~2022, minor patches 2026; iOS updated more frequently) |
| **Platforms** | iOS (iPhone/iPad), Android, PTSD Coach Online (web/desktop, Chrome recommended) |
| **Cost** | 100% free, no ads, no in-app purchases, no account required |
| **Downloads** | 1,000,000+ across 115 countries (VA flyer, 2026) |
| **Rating** | ~4.5 stars (iOS), ~4.0 stars (Android) |
| **Official links** | [ptsd.va.gov/appvid/mobile/ptsdcoach_app.asp](https://www.ptsd.va.gov/appvid/mobile/ptsdcoach_app.asp) · [mobile.va.gov/app/ptsd-coach](https://mobile.va.gov/app/ptsd-coach) · [Google Play](https://play.google.com/store/apps/details?id=is.vertical.ptsdcoach) · [App Store](https://apps.apple.com/us/app/ptsd-coach/id430646302) |
| **Support contact** | MobileMentalHealth@va.gov / ncptsd@va.gov |

---

## 2. Full Feature List

### Four core modules (from Kuhn et al., 2014; VA flyer 2026)

| Module | Contents |
|---|---|
| **Learn** | Psychoeducation: what PTSD is, how it develops, prevalence, symptom descriptions, treatment options (PE, CPT, EMDR, medication), "What to expect from therapy," "How to find help." Addresses common patient concerns/objections. |
| **Self-Assessment** | PCL-5 (PTSD Checklist for DSM-5, 20 items; formerly PCL-C). Provides severity score, interpretive feedback, change tracking via line graph, scheduled re-assessment with reminders. Not a diagnosis — screening tool. |
| **Manage Symptoms** | User selects problem area → rates distress on 0–10 SUDS → receives CBT-based coping tool. If SUDS doesn't decrease, prompted to try another. Problem categories: trauma reminders, avoidance, disconnection from people/reality, sadness/hopelessness, worry/anxiety, anger, sleep. ~21 evidence-based tools (expanded in v4). Tools include: paced breathing, progressive muscle relaxation, self-coping statements, pleasant events, grounding, mindfulness, positive self-talk, anger management, sleep hygiene, guided relaxation exercises, soothing sounds and images, relationship tips. Users can customize tools with their own audio, photos, text. |
| **Find Support** | Emergency contacts (911), Veterans Crisis Line (988 then press 1), local VA facility locator, personal support contacts (pulls from phone contacts), mental health provider directory. Safety plan capability. |

### Additional features (v4.0 update)
- Personalized home screen with activity recommendations and relaxing visuals with movement
- Expanded emotion management content
- "Hearing from others with PTSD" (peer stories)
- Progress tracking: triggers, medication, mood, and more
- Anonymous community: share inspiring quotes and coping tips
- Customizable home screen theme/images
- Inspirational quotes (customizable)
- Dark mode

### PTSD Coach Online (desktop)
- 17 tools with video coaching
- Problem-solving modules
- Goal-setting tools
- Requires internet, Chrome recommended

---

## 3. Clinical Basis & Evidence

### Therapeutic framework
PTSD Coach is **grounded in Cognitive Behavioral Therapy (CBT)** principles. Specifically:
- **Cognitive restructuring** (self-coping statements, positive self-talk)
- **Relaxation training** (paced breathing, progressive muscle relaxation)
- **Behavioral activation** (pleasant events, activity scheduling)
- **Mindfulness-based techniques** (added in later versions)
- **Psychoeducation** aligned with VA/DoD PTSD treatment guidelines

It does **NOT** deliver Prolonged Exposure (PE) or Cognitive Processing Therapy (CPT) directly — those are separate VA apps (PE Coach, CPT Coach). PTSD Coach is a **self-management adjunct**, not a treatment delivery system.

### Key studies

| Study | Design | N | Key findings |
|---|---|---|---|
| **Kuhn et al. (2014)** Mil Med | Feasibility/acceptability pilot; 3-day use by veterans in residential PTSD treatment | 45 | 90% satisfaction; 68–91% rated "moderately to extremely helpful"; used for in-the-moment distress, scheduled tracking, and sleep |
| **Miner et al. (2016)** | RCT, PTSD Coach vs. waitlist; 3-month use | 120 | Significant improvements in PTSD symptoms (p=.035), depression (p=.005), psychosocial functioning (p=.007) vs. waitlist. 46.8% achieved clinically significant improvement vs. 25.9% waitlist (p=.018). Effects maintained at 3-month follow-up. **No significant between-group mean differences at post-treatment** — effects were modest. |
| **Possemato et al. (2016)** pilot RCT | Self-managed vs. clinician-supported PTSD Coach in primary care | 20 | Both groups improved; clinician-supported group had higher engagement in subsequent PTSD treatment (70% vs. 30% accepted referrals) |
| **Possemato et al. (2023)** JGIM | Multi-site pragmatic RCT, Clinician-Supported PTSD Coach vs. PCMHI-TAU | 234 | Clinician-rated PTSD severity: **no significant difference**. Patient-reported PTSD: Coach superior (d=.28, p=.021). Higher treatment satisfaction (p<.001). 74% more sessions engaged during intervention. **No more engagement in specialty mental health post-study.** |
| **Hensler et al. (2023)** Internet Interventions | Longitudinal follow-up of Swedish RCT | — | Effects maintained long-term |
| **Bröcker et al. (2023)** systematic review & meta-analysis | 14 studies | — | Feasibility and acceptability supported. **Pooled effect on PTSD symptoms: NOT significant at post-treatment.** Evidence on effectiveness "limited." |
| **South Africa RCT (2024)** Cambridge Global Mental Health | Counselor-supported PTSD Coach vs. e-TAU | 62 | Significant CAPS-5 interaction over time (p=0.02). Large between-group effect at 3-month follow-up (g=0.86, p<0.01). First study in resource-constrained setting. |

### Bottom line on evidence
- **Moderate evidence** for feasibility, acceptability, and symptom reduction as self-management tool
- **Strongest evidence** when combined with clinician support (not standalone)
- Effects are **modest** — smaller than evidence-based psychotherapies (PE/CPT)
- **Meta-analysis found non-significant pooled effect** on PTSD symptoms
- Only 5 of 9 VA apps have empirical literature; PTSD Coach has the strongest evidence in the portfolio

---

## 4. Proof of Veteran/US-Centricity

### Language & framing
- **Play Store description (verbatim)**: "PTSD Coach was **designed for Veterans and military Servicemembers** who have, or may have, Posttraumatic Stress Disorder (PTSD)"
- **VA Mobile page**: "The PTSD Coach app was **designed for Veterans** experiencing symptoms of PTSD"
- **Created by**: "VA's National Center for PTSD and DoD's DHA Connected Health"
- The app was built by veterans, for veterans, with veteran focus groups (N=78) and clinical staff (N=5) during participatory design

### Content examples (veteran/combat-centric)
- Crisis routing: **Veterans Crisis Line** (988 then press 1) is the primary crisis resource, not general crisis lines
- "Find Support" links to **VA facilities**, VA mental health care, VA-specific provider directories
- Peer stories section features veteran experiences
- Psychoeducation examples reference combat trauma scenarios
- Resource section assumes VA healthcare system access

### Complaints as evidence (from complaints.md)
- **Complaint #3** (211 helpful): "Combat-veteran specific. Techniques/prompts = ambushes, bombings. CPTSD / civilian trauma not useful."
- **Complaint #14**: "Unless I'm veteran can't download. Have C-PTSD. B.S." (Note: app IS downloadable by anyone, but the framing deters civilians)
- **Complaint #23**: "Got CPTSD, did not help, deleted. Only deals with vets, need child abuse / severe trauma coverage."
- **Complaint #25**: "Targeted to military veterans, civilians won't find useful. / Figured tied to VA, it's not. / Only for VA/military, PTSD affects everyone not just VA."

### Structural US-centrism
- Crisis line numbers are US-only (988, Veterans Crisis Line)
- Provider locator is VA-specific
- PCL-5 is the assessment tool — valid but doesn't capture CPTSD (ICD-11)
- All content in English only — no multilingual support despite "115 countries" download claim
- Treatment referrals assume US healthcare system

---

## 5. Permissions / Privacy / Data Handling

### Android permissions (17 total, from APK analysis)
| Permission | Purpose | Justification |
|---|---|---|
| Camera | User can take photos for app personalization | Optional — for custom backgrounds |
| Record audio | User can record custom audio for coping tools | Optional — for personalization |
| Read contacts | Pulls phone contacts for "Find Support" emergency contacts | Optional — for support network feature |
| Read/Write external storage | Stores user data, photos, audio locally | Required for personalization features |
| Internet | Downloads content, anonymous analytics | Required for initial setup, optional for offline use |
| Access network state | Check connectivity | Standard |
| Wake lock | Keeps device active during exercises | Standard |
| Foreground service | Background audio playback | For relaxation sounds |
| Schedule exact alarm | Medication/reminder scheduling | Optional |

### Privacy policy (official VA position)
> "When you use a VA mobile app, **no data that could be used to identify you is sent to VA or third parties**. Any information that you enter into the app, such as names, phone numbers, addresses, images, or music, **cannot be accessed, stored, or shared by VA**."

> "VA mobile apps do collect **anonymous information** about how people use the app. It is only used to improve how well the app works. We can see what sections of the app people visit, for example."

- Anonymous usage data can be turned off in Settings → "Anonymous Usage Data" → off
- Data encrypted in transit (Google Play listing)
- App stores "Health & Fitness, App activity, Device or other IDs" (Google Play data safety)

### HIPAA stance
> "If the user were to transmit or share data with a health care provider, the provider must then comply with HIPAA rules."

**Key nuance**: PTSD Coach itself does NOT claim HIPAA compliance. It states:
- User data stays on device (not transmitted to VA)
- Data belongs to user; HIPAA doesn't apply while data is stored locally
- If user shares data with a provider, the provider must comply
- VA's privacy agreement: "in no event will VA be liable for any damages, including those for loss of data"

### Congressional concerns (2020 testimony)
- House Veterans Affairs Committee found apps could access "private veteran data, including phone cameras, microphones, photos, locations, contacts, calendars, files and more"
- Lawmakers called this "disturbing"
- VA admitted it does NOT "police" third-party networks
- Privacy agreements are "hundreds, if not thousands of words long" — unrealistic to expect users to read
- Once user taps "agree," **HIPAA protections do not always apply** — third parties could use/share data

### Mozilla Foundation assessment (2022, updated 2023)
- Rated as "one of the best privacy protecting apps"
- "No personal data collected"
- "No known privacy or security incidents in the last 3 years"
- Concern: app wasn't updated in Google Play for ~2 years (security patch cadence unclear)
- **Limitation**: No children's privacy policy

### Offline behavior
- Core app functions offline after initial download
- Data stored locally on device only
- Anonymous usage data requires internet connection
- Crisis line numbers work offline (phone call)
- PTSD Coach Online (web version) requires internet

### Privacy gap analysis
- 17 Android permissions is excessive for a self-help tool
- Camera/microphone access for personalization is optional but requested at install
- No open-source code — can't independently verify data handling claims
- Telemetry collection (even anonymous) without granular consent
- Congress found the gap between "no data sent" claims and actual permission scope concerning

---

## 6. Why PTSD Coach Fails Civilians/CPTSD/Non-US

### DSM-5 PTSD vs. ICD-11 CPTSD — the clinical mismatch

| Feature | DSM-5 PTSD (what PTSD Coach targets) | ICD-11 CPTSD (what many users actually have) |
|---|---|---|
| **Symptom clusters** | 4 clusters, 20 symptoms: intrusion, avoidance, negative cognitions/mood, arousal/reactivity | 6 clusters: PTSD's 3 core (re-experiencing, avoidance, threat) + 3 DSO: **affect dysregulation, negative self-concept, relationship disturbances** |
| **Trauma type** | Any traumatic event | Typically prolonged/repeated interpersonal trauma (child abuse, domestic violence, torture, captivity) |
| **Prevalence** | ~6.1% lifetime (US) | ~3.8% (US); nearly equal to PTSD prevalence |
| **Treatment needs** | Brief manualized trauma-focused therapy (PE, CPT) often sufficient | **Phase-based treatment**: stabilization/skills → processing → reconnection. Brief treatments often insufficient |
| **Key distinguishing symptoms** | Fear-based: re-experiencing, avoidance, hyperarousal | **Plus**: emotional dysregulation, chronic shame/guilt, identity disturbance, relational difficulties, dissociation |

### Specific gaps for civilian/non-veteran PTSD

1. **Trauma type mismatch**: PTSD Coach's coping tools and psychoeducation assume single-incident trauma (combat). CPTSD from childhood abuse, domestic violence, sexual assault, or prolonged captivity requires different approaches — particularly affect regulation skills, self-compassion work, and relational repair.

2. **No affect regulation tools**: CPTSD's hallmark is emotional dysregulation (both hyper- and hypoactivation). PTSD Coach's 10-minute breathing exercises don't address the chronic, pervasive emotional instability of CPTSD.

3. **Missing DSO domains**: No tools for:
   - Negative self-concept (chronic shame, worthlessness, guilt)
   - Relationship disturbances (attachment issues, trust, intimacy)
   - Identity disturbance
   - Dissociation management

4. **Assessment tool limitation**: PCL-5 captures DSM-5 PTSD symptoms only. The International Trauma Questionnaire (ITQ) captures both PTSD and CPTSD (DSO domains). Users with CPTSD may score low on PCL-5 while suffering severely.

5. **Crisis line is US-only**: 988/Veterans Crisis Line is meaningless for UK, EU, Australia, India, etc.

6. **Provider locator is VA-specific**: No help finding civilian therapists, especially trauma-specialized ones outside the US.

7. **Cultural/contextual blindness**: No consideration of:
   - Gender-based violence (complaint #19: "Being FEMALE listed as risk factor — alienating")
   - Childhood abuse (complaint #23: "need child abuse / severe trauma coverage")
   - Intimate partner violence
   - Medical trauma (cancer, chronic illness)
   - Community violence, natural disasters
   - Refugee/asylum seeker experiences
   - Non-Western trauma frameworks

8. **Language barrier**: English-only despite global download claims

9. **Assumes resource access**: Complaint #9: "Prompts assume money to spend: 'go shopping' with what money?!" — tools assume quiet space, time, financial resources, physical ability

---

## 7. Top 5 Competitors/Alternatives for Civilian PTSD

| # | App | Differentiator | Price | Evidence |
|---|---|---|---|---|
| 1 | **Unpanic** (unpanic.app) | Specifically designed for **CPTSD triggers and flashbacks**; 100% free, no paywalls; clinically-validated grounding + sound therapy; no combat framing | Free | Uses crisis-intervention standard breathing protocols |
| 2 | **Tending** (github.com/swallace100/tending) | Open-source **C-PTSD-specific** grounding tool; 5 activities built around CPTSD research; body relaxation, sensory grounding, paced breathing, self-check-in, self-compassion | Free | Built with mental health professional input; research-backed |
| 3 | **PTSDfy** (github.com/KasambaLumwagi/ptsdfy) | AI companion (Gemini 2.5 Flash) with **compassionate, non-judgmental validation**; daily thread organization; privacy-first (local storage); built for hackathon context | Free | Hackathon project with solid grounding features |
| 4 | **MoodStead** | **Stanley-Brown safety plan** built-in; voice journal for sensory processing; CBT thought records; sleep tracking; therapist PDF export; designed for PTSD specifically | Free trial → subscription | Clinically-structured tools |
| 5 | **Anchor** (apps.apple.com) | Combines **DBT + CBT** techniques; AI crisis support; mood/emotion tracking; 100% offline capable; trauma-informed AI assistant; citations to Harvard/APA/VA sources | Free | Uses DBT distress tolerance (TIPP, ACCEPTS) + CBT — broader than PTSD Coach's CBT-only approach |

### Notable mentions
- **Woebot**: AI-guided CBT, RCT evidence for depression/anxiety, but not PTSD-specific
- **Calm Harm**: DBT-based, good for self-harm risk in PTSD, limited PTSD-specific content
- **Sanvello**: DISCONTINUED as consumer product (absorbed into Optum/AbleTo)
- **Mindfulness Coach (VA)**: Best trauma-informed mindfulness; better than Calm/Headspace for PTSD

---

## 8. Five Exploitable Gaps (Mapped to Complaints Clusters)

### Gap 1: Inclusive Trauma Model (maps to Cluster B + complaints #3, #14, #23, #25)

**The gap**: PTSD Coach is DSM-5 PTSD-only, combat-framed, excludes CPTSD and civilian trauma types entirely.

**Our win**: Build with ICD-11 CPTSD as first-class model. Include:
- Affect regulation tools (DBT TIPP, ACCEPTS, Opposite Action)
- Self-compassion work (addresses negative self-concept DSO)
- Trauma-type selection at onboarding (combat, childhood abuse, sexual assault, medical, IPV, community violence, etc.) — no single framing
- International Trauma Questionnaire (ITQ) alongside PCL-5

**30h demo**: Onboarding flow with trauma-type picker → adaptive content that never shows combat language unless selected → ITQ + PCL-5 dual assessment

---

### Gap 2: Transparent Privacy-First Architecture (maps to Cluster A + complaints #7, #8, #11, #22, #24, #26)

**The gap**: PTSD Coach requests 17 Android permissions including camera, microphone, contacts, storage — with vague justifications. No open-source code. Congress found the gap between claims and permissions "disturbing."

**Our win**:
- Zero permissions by default — all features work without camera/mic/contacts
- Open-source codebase (can verify claims)
- Explicit permission justifications in plain language: "We need camera access ONLY if you want to add custom photos to your coping toolkit. You can skip this."
- Local-only storage (no analytics without opt-in)
- Privacy dashboard showing exactly what data exists and one-tap deletion

**30h demo**: Show the permission request screen with plain-language justifications side-by-side with PTSD Coach's vague Android permissions listing

---

### Gap 3: Context-Aware, Non-Nagging UX (maps to Cluster C + D + complaints #1, #2, #9, #10, #12, #15, #16, #18, #20)

**The gap**: PTSD Coach is described as "robotic," "predictable," "kindergarten grounding," "just listening to calming music 10 mins." Forced quizzes and crisis-line nagging. No context awareness (suggests "go shopping" to someone in poverty).

**Our win**:
- **Context-aware tool suggestions**: time of day (work vs. home), location (office-safe tools = no audio), resource level (free tools vs. paid), social setting (discrete vs. private)
- **No forced assessments**: PCL-5 available but never mandatory
- **Work-safe mode**: visual-only coping tools, no audio, 60-second micro-interventions
- **Adaptive complexity**: starts with simple grounding, escalates to deeper work as user progresses — not one-size-fits-all

**30h demo**: Show same user at 2pm at work → gets visual box breathing + 5-4-3-2-1. Same user at 10pm at home → gets full audio-guided PMR + journaling prompt. Show contrast with PTSD Coach's one-size response.

---

### Gap 4: Human Bridge, Not Crisis Line Dump (maps to Cluster F + complaints #1, #17, #20, #27, #28)

**The gap**: PTSD Coach "expected doctor/coach contact, got robotic programs" (complaint #1). "Not replacement for treatment but no bridge to real help" (complaint #17). Only links to Veterans Crisis Line and VA providers — no warm handoff to real humans.

**Our win**:
- **Therapist matching**: Integrate with Open Path Collective, Psychology Today directory, or build simple provider database with filters (specialty, location, insurance, telehealth)
- **Warm handoff flow**: When PCL-5/ITQ indicates severity beyond self-help scope → gentle prompt with pre-composed message to send to a therapist or trusted contact
- **Trusted contact system**: User pre-configures 1-3 contacts; app can send "I'm struggling right now" with one tap (no crisis line required)
- **Session bridge**: "Share this assessment with your therapist" export (PDF/CSV) — so the app becomes a therapy companion, not a replacement

**30h demo**: Show: user takes PCL-5 → score indicates moderate-severe → app suggests "Consider talking to someone. Here are 3 trauma therapists near you" → user taps to compose message → sends via SMS/email. Show the PDF export for therapist.

---

### Gap 5: Reliability + Performance (maps to Cluster E + complaints #4, #5, #6, #13, #21)

**The gap**: PTSD Coach is described as "stuck on logo," "back button breaks," "hasn't worked since last update," "sounds still play after exit," "won't open," "Android layout broken." Android users report significantly more technical problems than iOS (Owen et al., 2015).

**Our win**:
- **Lightweight architecture**: Native or well-optimized cross-platform (not a bloated WebView)
- **Offline-first**: Full functionality without internet after initial load
- **Crash reporting**: Transparent error logging (opt-in)
- **Android-first testing**: Prioritize the platform where PTSD Coach fails most
- **Graceful degradation**: If any feature fails, rest of app continues working

**30h demo**: Side-by-side load time comparison. Show app opening in <1s with full offline functionality. Show PTSD Coach's Android review complaints about crashes.

---

## Summary: Our Positioning in One Sentence

> **"PTSD Coach was built by the VA for veterans with combat PTSD. We're building by trauma survivors for everyone — CPTSD-aware, privacy-transparent, context-smart, human-connected, and clinically inclusive."**

---

## Sources

1. VA National Center for PTSD. "PTSD Coach." https://www.ptsd.va.gov/appvid/mobile/ptsdcoach_app.asp (Updated April 21, 2026)
2. VA Mobile. "PTSD Coach." https://mobile.va.gov/app/ptsd-coach
3. Kuhn, E. et al. (2014). "Preliminary Evaluation of PTSD Coach, a Smartphone App for Post-Traumatic Stress Symptoms." Mil Med. https://doi.org/10.7205/milmed-d-13-00271
4. Miner, A. et al. (2016). "A randomized controlled trial of a smartphone app for posttraumatic stress disorder symptoms." https://www.ptsd.va.gov/professional/articles/article-pdf/id47101.pdf
5. Possemato, K. et al. (2016). "Using PTSD Coach in primary care with and without clinician support: A pilot RCT." Gen Hosp Psychiatry 38:94-98.
6. Possemato, K. et al. (2023). "A Randomized Clinical Trial of Clinician-Supported PTSD Coach in VA Primary Care Patients." JGIM. PMC10022983
7. Bröcker, E. et al. (2023). "Feasibility, acceptability, and effectiveness of web-based and mobile PTSD Coach: a systematic review and meta-analysis." PMC10215014
8. Hensler, I. et al. (2023). "Longitudinal follow-up of the randomized controlled trial of access to the trauma-focused app PTSD Coach." Internet Interventions 32:100618.
9. Yu, R. & Haddock, A. (2026). "Digital Mental Health Care for Veterans." Mil Med. https://doi.org/10.1093/milmed/usag151
10. Bennett, A. (2020). "'Disturbing': VA-recommended apps could access private veteran data, Congress says." Connecting Vets/Audacy.
11. Mozilla Foundation. "PTSD Coach | Privacy & security guide." https://www.mozillafoundation.org/en/privacynotincluded/ptsd-coach/
12. Google Play Store listing: https://play.google.com/store/apps/details?id=is.vertical.ptsdcoach
13. App Store listing: https://apps.apple.com/us/app/ptsd-coach/id430646302
14. VA PTSD Coach Flyer (2026): https://www.ptsd.va.gov/appvid/docs/PTSDCoachFlyer_508.pdf
15. Brewin, C. et al. (2017). "A review of current evidence regarding the ICD-11 proposals for diagnosing PTSD and complex PTSD." Clinical Psychology Review.
16. Cloitre, M. et al. (2013). "PTSD and Complex PTSD: ICD-11 updates on concept and measurement." PMC5774423
17. VA National Center for PTSD. "Complex PTSD." https://www.ptsd.va.gov/professional/treat/essentials/complex_ptsd.asp
18. ILTY (2026). "Best PTSD Apps (2026)." https://ilty.co/best/ptsd-apps
19. Lovon (2026). "AI Therapy for PTSD: Top Apps Ranked 2026." https://lovon.app/blog/ptsd/ai-therapy-for-ptsd-and-trauma-recovery
20. NeuroLaunch (2024). "PTSD Apps: A Comprehensive Recovery Guide." https://neurolaunch.com/ptsd-app/
21. complaints.md — Team's raw Play Store review synthesis (local file)
