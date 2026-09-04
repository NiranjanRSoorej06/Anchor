# Anchor — Evidence-Backed Module Plan

*A trauma-informed personalization + current-state detection + evidence-informed stabilization framework for PTSD-related distress.*

---

## Core correction to the earlier design

The evidence does not support claiming that a particular trauma type requires a particular quick exercise. The stronger, defensible model is:

> **Trauma-informed personalization + current-state detection + evidence-informed stabilization**

Every claim Anchor makes should be traceable to a specific source. This document maps each claim in the proposal to its evidence base, with direct links.

---

## 1. "PTSD is not one single trauma type"

**Anchor claim:** People with PTSD can have different trauma histories and symptom presentations, so Anchor does not force users into a single "type of PTSD."

**Evidence:** ICD-11 conceptualizes PTSD and Complex PTSD (CPTSD) in terms of symptom domains — re-experiencing, avoidance, sense of current threat (core PTSD), plus affect dysregulation, negative self-concept, and disturbed relationships (CPTSD). The International Trauma Questionnaire (ITQ) was developed specifically to assess these ICD-11 symptom dimensions.

**Sources:**
- Cloitre, M., Shevlin, M., Brewin, C.R., Bisson, J.I., Roberts, N.P., Maercker, A., Karatzias, T., & Hyland, P. (2018). *The International Trauma Questionnaire: Development of a self-report measure of ICD-11 PTSD and Complex PTSD.* Acta Psychiatrica Scandinavica. https://doi.org/10.1111/acps.12956
- VA National Center for PTSD — professional assessment resources: https://www.ptsd.va.gov/professional/assessment/index.asp

**Design implication:**
```
Trauma profile  ≠  PTSD diagnosis / type
```

---

## 2. Don't make the user describe the trauma

**Anchor claim:** Anchor can personalize support without requiring the user to disclose the details of their traumatic experience.

This is a **design and ethical principle**, not a proven therapeutic effect. The ITQ itself asks a person to identify the experience that troubles them most, then assesses symptom and functional-impairment domains — it does not require a detailed trauma narrative.

**Source:**
- Cloitre et al. (2018), ITQ — https://doi.org/10.1111/acps.12956

**Anchor implementation** — instead of "Describe your trauma," ask which situations trigger distress (loud sounds, driving, crowds, being alone, feeling trapped, physical touch, certain smells/places, night/darkness, images/videos, other, prefer not to say), then state plainly: *"You never need to describe what happened to use Anchor."*

---

## 3. Build a trauma profile, not a diagnosis

**Anchor claim:** Trauma history can be collected as contextual information rather than used as a rigid diagnostic category.

**Evidence:** The ITQ is designed to assess ICD-11 PTSD/CPTSD symptom dimensions and functional impairment rather than assign a single trauma "type." The VA also distinguishes self-report screening tools from formal clinician-administered instruments.

**Sources:**
- Cloitre et al. (2018) — https://doi.org/10.1111/acps.12956
- VA National Center for PTSD — list of assessment measures: https://www.ptsd.va.gov/professional/assessment/list_measures.asp

**Important:** call this a **personalized trauma profile**, never a diagnosis.

---

## 4. Identify the current state (the most clinically important module)

**Anchor claim:** The intervention should respond to the person's current symptoms/state rather than simply their trauma category.

**Evidence:** The ICD-11 framework distinguishes core PTSD symptom domains (re-experiencing, avoidance, current threat) from CPTSD's added disturbances in self-organization.

**Source:**
- Cloitre et al. (2018) — https://doi.org/10.1111/acps.12956

**Anchor implementation:** during an episode, don't run a long questionnaire — ask "What feels closest right now?" with a small set of state options (panicky, hyper-alert, re-experiencing, disconnected/unreal, frozen, not sure). This becomes the episode-state classifier that routes to the right stabilization pathway.

---

## 5. Grounding module

**Anchor claim:** Grounding can be incorporated into digital PTSD interventions.

**Evidence:** A randomized controlled trial of internet-based guided self-help for adults with DSM-5 PTSD included eight modules — psychoeducation, grounding, relaxation, behavioural activation, exposure, cognitive therapy, and relapse prevention. 42 adults with mild-to-moderate PTSD were randomized to the intervention or a delayed-treatment control; the intervention group showed significantly lower clinician-assessed PTSD symptoms post-treatment.

**Source:**
- Lewis, C., Farewell, D., Groves, V., Kitchiner, N.J., Roberts, N., Vick, T., & Bisson, J. (2017). *Internet-based guided self-help for post-traumatic stress disorder (PTSD): Randomised controlled trial.* Depression and Anxiety, 34(6), 555–565. https://doi.org/10.1002/da.22645
  (open-access accepted manuscript: https://orca.cardiff.ac.uk/id/eprint/99409/)

**What you can claim:**
✅ "Grounding is an evidence-informed component used in evaluated PTSD interventions."

**What you cannot claim:**
❌ "Our grounding exercise is clinically proven to stop a flashback in 90 seconds." — the Lewis et al. trial evaluated a multi-module guided intervention, not a specific 90-second protocol.

---

## 6. Self-calming / arousal module

**Anchor claim:** Brief self-calming strategies are consistent with clinical PTSD guidance.

**Evidence:** NICE's guideline on PTSD recommends that trauma-focused CBT include strategies for managing arousal and flashbacks, including self-calming techniques for use within and between sessions.

**Source:**
- National Institute for Health and Care Excellence (NICE). *Post-traumatic stress disorder (NG116).* Published 5 December 2018, last reviewed 2025. https://www.nice.org.uk/guidance/ng116

This is one of the more directly supported claims in the proposal, since it maps to an explicit guideline recommendation rather than a single trial.

---

## 7. Relaxation module

**Anchor claim:** Relaxation training has been studied directly in people with PTSD.

**Evidence:**
- An RCT of 87 older male combat veterans with military-related PTSD compared prolonged exposure (PE) to relaxation training (RT). Both groups showed significant reductions in clinician-rated PTSD symptoms, though the two treatments differed on some self-report outcomes and gains were not fully maintained at follow-up.
- A separate RCT of 98 Spanish-speaking Latino adults with PTSD in Puerto Rico compared culturally adapted prolonged exposure with applied relaxation; both arms showed significant reductions in PTSD symptoms.

**Sources:**
- Thorp, S.R., Glassman, L.H., Wells, S.Y., Walter, K.H., Gebhardt, H., Twamley, E., Golshan, S., Pittman, J., Penski, K., Allard, C., Morland, L.A., & Wetherell, J. (2019). *A randomized controlled trial of prolonged exposure therapy versus relaxation training for older veterans with military-related PTSD.* Journal of Anxiety Disorders, 64, 45–54. https://pubmed.ncbi.nlm.nih.gov/30978622/
  (trial registration: https://clinicaltrials.gov/study/NCT00539279)
- Vera, M., et al. (2022). Randomized controlled trial comparing culturally adapted prolonged exposure with applied relaxation among Spanish-speaking Latinos with PTSD in Puerto Rico (N = 98). https://pmc.ncbi.nlm.nih.gov/articles/PMC9035035
  (trial registration: https://clinicaltrials.gov/study/NCT02134691)

**Be precise:** say "Relaxation techniques have been studied as part of PTSD treatment," not "Relaxation is a first-line treatment for PTSD." The 2023 VA/DoD guideline states evidence is insufficient to recommend for or against relaxation training as a standalone PTSD treatment (see source in section 13). Stating this limitation explicitly in the pitch will make Anchor's evidence framing look more credible, not less.

---

## 8. Emotion-regulation module

**Anchor claim:** Emotion-regulation skills have been experimentally studied as an adjunct to PTSD treatment.

**Evidence:** An RCT of 70 adult civilian patients with PTSD compared supportive counselling followed by CBT against emotion-regulation training followed by CBT. The emotion-regulation arm showed fewer treatment drop-outs and lower PTSD/anxiety symptoms at 6-month follow-up (moderate effect size).

**Source:**
- Bryant, R.A., Mastrodomenico, J., Hopwood, S., Kenny, L., Cahill, C., Kandris, E., & Taylor, K. (2013). *Augmenting cognitive behaviour therapy for post-traumatic stress disorder with emotion tolerance training: A randomized controlled trial.* Psychological Medicine. https://pubmed.ncbi.nlm.nih.gov/23406821/

**Framing:** this supports emotion regulation as *preparation for*, or an adjunct to, formal trauma-focused treatment — not a standalone cure. Present it as one stabilization option among several.

---

## 9. Flashback-management module

**Anchor claim:** Clinical PTSD guidance specifically recognizes management of flashbacks as a treatment component.

**Evidence:** NICE's guideline states that trauma-focused CBT should include strategies for managing arousal and flashbacks, including self-calming and flashback-management techniques.

**Source:**
- NICE, *Post-traumatic stress disorder (NG116)* — https://www.nice.org.uk/guidance/ng116

**Design pattern:** "I'm back there" → flashback mode → present-time orientation → external sensory grounding → present-time statement → reassess. The goal is stabilization, not trauma analysis, during an acute episode.

---

## 10. Dissociation needs a separate pathway

This is one of the most important safety distinctions in the whole design.

**Evidence:** NICE recommends that supported trauma-focused computerized CBT should *not* be offered to people with severe PTSD symptoms, particularly dissociative symptoms. VA assessment resources also include measures specifically covering dissociative symptoms and the dissociative subtype of PTSD.

**Sources:**
- NICE, NG116 — https://www.nice.org.uk/guidance/ng116
- VA National Center for PTSD, assessment measures — https://www.ptsd.va.gov/professional/assessment/list_measures.asp

**Design rule:** if a user reports feeling disconnected/unreal, do not run the same algorithm used for hyperarousal ("my heart is racing"). Route instead to: external orientation → simple present-focused prompts → reassess → **if worsening, stop and route to a safety pathway.** Do not use trauma imagery or exposure-style content as a quick-relief feature.

---

## 11. The complete evidence-backed algorithm

```
                   ANCHOR
                     │
                     ▼
             ┌──────────────┐
             │ ONBOARDING   │
             └──────┬───────┘
                    │
                    ▼
          ┌───────────────────┐
          │ PERSONAL PROFILE  │
          │  Trauma context   │
          │  Triggers         │
          │  Typical symptoms │
          │  Preferences      │
          └─────────┬─────────┘
                    │
                    ▼
             🔴 I'M OVERWHELMED
                    │
                    ▼
          ┌───────────────────┐
          │ CURRENT STATE     │
          └─────────┬─────────┘
                    │
       ┌────────────┼─────────────┐
       ▼            ▼             ▼
  Hyperarousal   Flashback    Dissociation
       │            │             │
       ▼            ▼             ▼
 Self-calming / Present-time    External
 Relaxation     grounding       orientation
       │            │             │
       └────────────┼─────────────┘
                    ▼
             1–3 MINUTE SKILL
                    │
                    ▼
             REASSESS DISTRESS
                    │
          ┌─────────┼─────────┐
          ▼         ▼         ▼
        Better     Same      Worse
          │         │         │
       Finish    Another   Stop + safety
                  option      pathway
```

---

## 12. Claim-to-source map

| Anchor claim | What you can say | Source |
|---|---|---|
| PTSD shouldn't be reduced to one trauma type | Symptom-based assessment is more appropriate | Cloitre et al. 2018 (ITQ) — https://doi.org/10.1111/acps.12956 |
| Trauma profile can be collected without a detailed narrative | ITQ focuses on trauma + symptom/functioning domains | Cloitre et al. 2018 — https://doi.org/10.1111/acps.12956 |
| Assess current symptom state | ICD-11 PTSD domains include re-experiencing, avoidance, and current threat | Cloitre et al. 2018 — https://doi.org/10.1111/acps.12956 |
| Flashback management is clinically relevant | NICE recommends strategies for flashbacks | NICE NG116 — https://www.nice.org.uk/guidance/ng116 |
| Self-calming is clinically recognized | NICE recommends self-calming techniques | NICE NG116 — https://www.nice.org.uk/guidance/ng116 |
| Grounding can be part of a PTSD intervention | Included in an RCT of internet-guided PTSD self-help | Lewis et al. 2017 — https://doi.org/10.1002/da.22645 |
| Relaxation has PTSD research support | RCT in 87 older combat veterans | Thorp et al. 2019 — https://pubmed.ncbi.nlm.nih.gov/30978622/ |
| Applied relaxation has PTSD research support | RCT in 98 Spanish-speaking adults with PTSD | Vera et al. 2022 — https://pmc.ncbi.nlm.nih.gov/articles/PMC9035035 |
| Emotion-regulation skills can be incorporated into PTSD treatment prep | RCT in 70 adults | Bryant et al. 2013 — https://pubmed.ncbi.nlm.nih.gov/23406821/ |
| Dissociation deserves a separate pathway | VA assessment resources + NICE caution on computerized treatment | NICE NG116; VA National Center for PTSD |
| Digital PTSD interventions are feasible | Internet-based guided self-help RCT | Lewis et al. 2017 — https://doi.org/10.1002/da.22645 |
| Anchor shouldn't claim to replace PTSD treatment | Guidelines emphasize formal evidence-based treatment and clinical judgment | NICE NG116; VA/DoD CPG 2023 — https://www.healthquality.va.gov/guidelines/MH/ptsd/ |

---

## 13. The clinical boundary to state explicitly in the pitch

The 2023 VA/DoD Clinical Practice Guideline for Management of PTSD and Acute Stress Disorder explicitly states it is intended to assist clinicians and support shared decision-making — not to replace clinical judgment. NICE similarly recommends established trauma-focused psychological treatments (e.g., trauma-focused CBT, EMDR) as first-line care for PTSD.

**Source:**
- Management of Posttraumatic Stress Disorder and Acute Stress Disorder Work Group (2023). *VA/DoD Clinical Practice Guideline for the Management of Posttraumatic Stress Disorder and Acute Stress Disorder.* Department of Veterans Affairs & Department of Defense. https://www.healthquality.va.gov/guidelines/MH/ptsd/
- NICE, NG116 — https://www.nice.org.uk/guidance/ng116

**Describe Anchor as:**
> "An evidence-informed acute stabilization and grounding tool for people experiencing PTSD-related distress."

**Never describe it as:**
- ❌ "An AI treatment for PTSD."
- ❌ "A cure for PTSD."
- ❌ "An app that diagnoses the patient's PTSD type."

---

## 14. The strongest version of the innovation

Weak framing: *"We created breathing exercises for PTSD."*

Stronger framing:

> "Anchor personalizes the delivery of evidence-informed stabilization based on the user's trauma context, known triggers, current distress state, and preferences — without requiring the survivor to repeatedly disclose their trauma."

Technically, this is:

```
          PERSONALIZATION
                +
       CURRENT-STATE DETECTION
                +
        CLINICAL EVIDENCE
                +
          SAFETY FILTER
                ↓
       PERSONALIZED 1–3 MIN
       STABILIZATION SEQUENCE
```

Every intervention in the sequence should carry an evidence record, e.g.:

```
Exercise E001
      │
      ├── Target: flashback
      ├── Technique: grounding
      ├── Evidence: Lewis et al. 2017 (doi.org/10.1002/da.22645)
      ├── Clinical guidance: NICE NG116
      ├── Duration: 90 sec
      ├── Delivery: text/audio
      └── Safety notes: clinician reviewed
```

That last piece is a strong technical differentiator for a hackathon pitch: not an AI generating mental-health advice on the fly, but an **evidence-constrained intervention engine** — the AI personalizes *how* an established technique is presented, but does not invent the clinical intervention itself.

This gives a direct, defensible answer to the inevitable judge question — *"Where did this exercise come from?"* — traceable as:

**Technique → clinical guideline/paper → evidence level → personalization rules.**

---

## Reference list (all sources, in one place)

1. Cloitre, M., Shevlin, M., Brewin, C.R., Bisson, J.I., Roberts, N.P., Maercker, A., Karatzias, T., & Hyland, P. (2018). The International Trauma Questionnaire: Development of a self-report measure of ICD-11 PTSD and Complex PTSD. *Acta Psychiatrica Scandinavica.* https://doi.org/10.1111/acps.12956
2. VA National Center for PTSD — Professional assessment overview: https://www.ptsd.va.gov/professional/assessment/index.asp
3. VA National Center for PTSD — List of assessment measures: https://www.ptsd.va.gov/professional/assessment/list_measures.asp
4. National Institute for Health and Care Excellence (NICE). Post-traumatic stress disorder (NG116), 2018 (reviewed 2025). https://www.nice.org.uk/guidance/ng116
5. Lewis, C., Farewell, D., Groves, V., Kitchiner, N.J., Roberts, N., Vick, T., & Bisson, J. (2017). Internet-based guided self-help for post-traumatic stress disorder (PTSD): Randomised controlled trial. *Depression and Anxiety, 34*(6), 555–565. https://doi.org/10.1002/da.22645 (open access version: https://orca.cardiff.ac.uk/id/eprint/99409/)
6. Thorp, S.R., et al. (2019). A randomized controlled trial of prolonged exposure therapy versus relaxation training for older veterans with military-related PTSD. *Journal of Anxiety Disorders, 64*, 45–54. https://pubmed.ncbi.nlm.nih.gov/30978622/ (trial: https://clinicaltrials.gov/study/NCT00539279)
7. Vera, M., et al. (2022). RCT of culturally adapted prolonged exposure vs. applied relaxation among Spanish-speaking Latinos with PTSD. https://pmc.ncbi.nlm.nih.gov/articles/PMC9035035 (trial: https://clinicaltrials.gov/study/NCT02134691)
8. Bryant, R.A., Mastrodomenico, J., Hopwood, S., Kenny, L., Cahill, C., Kandris, E., & Taylor, K. (2013). Augmenting cognitive behaviour therapy for post-traumatic stress disorder with emotion tolerance training: A randomized controlled trial. *Psychological Medicine.* https://pubmed.ncbi.nlm.nih.gov/23406821/
9. Management of Posttraumatic Stress Disorder and Acute Stress Disorder Work Group (2023). VA/DoD Clinical Practice Guideline for the Management of Posttraumatic Stress Disorder and Acute Stress Disorder. https://www.healthquality.va.gov/guidelines/MH/ptsd/

---

*Note: this document is intended to support building an evidence-informed stabilization tool, not to diagnose or treat PTSD. It is not a substitute for a licensed clinician's judgment.*