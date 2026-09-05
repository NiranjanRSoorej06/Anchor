# PTSD Care in India — Offline Directory Research

**Purpose**: Power an offline "Get Help near you" directory — user types 6-digit PINCODE, app shows recognized nearby institutions. No GPS, no permission, fully offline.

**Last verified**: 2026-09-05

---

## 1. PINCODE ROUTING — Structure & Offline Table

### 1.1 Indian PIN Structure (confirmed)

The PIN (Postal Index Number) is a **6-digit code** introduced 15 Aug 1972 by India Post. Structure:

| Digit(s) | Meaning | Example (110001) |
|---|---|---|
| **1st digit** | Postal zone (1–8 geographical, 9 = Army) | `1` = Northern region |
| **1st 2 digits** | Sub-region / postal circle / state | `11` = Delhi |
| **1st 3 digits** | Sorting district (main sorting office) | `110` = New Delhi sorting |
| **Last 3 digits** | Delivery post office | `001` = Connaught Place HO |

**Sources**: India Post official (uat.indiapost.gov.in/MBE/Pages/Content/Pincode.aspx), Wikipedia "Postal Index Number", pincodesinfo.in, indiapost.org

### 1.2 Bundled Offline Table — PIN Prefix (first 2 digits) → State/Region

**~28 rows covering all zones.** The app needs only first 2 digits to route to a state/region, then shows the relevant institutions.

| PIN prefix | State / UT | Zone | Trauma-relevant institutions to surface |
|---|---|---|---|
| 11 | Delhi | 1-North | AIIMS Delhi, IHBAS Delhi, Tele MANAS, Vandrevala, iCall |
| 12–13 | Haryana | 1-North | Tele MANAS, PGIMER Chandigarh nearby |
| 14–16 | Punjab + Chandigarh | 1-North | PGIMER Chandigarh, Tele MANAS |
| 17 | Himachal Pradesh | 1-North | Tele MANAS, IGMC Shimla psychiatry |
| 18–19 | Jammu & Kashmir + Ladakh | 1-North | **Kashmir Lifeline** (18001807020), SMHS Srinagar, Tele MANAS |
| 20–22 | Uttar Pradesh (West/Central) | 2-North | Tele MANAS, KGMU Lucknow psychiatry |
| 23–28 | UP (East) + Uttarakhand | 2-North | Tele MANAS, BHU Varanasi psychiatry |
| 30–34 | Rajasthan | 3-West | Tele MANAS, SMS Medical College Jaipur |
| 36–38 | Gujarat | 3-West | Tele MANAS, BJ Medical College Ahmedabad |
| 39 | Gujarat (South) + Dadra Nagar Haveli | 3-West | Tele MANAS |
| 40–42 | Maharashtra (Mumbai/Goa region) | 4-West | Sangath Goa, Tele MANAS, J.J. Hospital Mumbai |
| 43–44 | Maharashtra (Vidarbha) + MP | 4-West | Tele MANAS, Regional Mental Hospital Nagpur |
| 45–48 | Madhya Pradesh | 4-West | Tele MANAS, MGM Medical College Indore |
| 49 | Chhattisgarh | 4-West | Tele MANAS |
| 50 | Telangana | 5-South | Tele MANAS, Osmanabad/Hyderabad psychiatry |
| 51–53 | Andhra Pradesh | 5-South | Tele MANAS, RIMS Ongole |
| 56–59 | Karnataka | 5-South | **NIMHANS Bengaluru** (apex), Tele MANAS, Parivarthan Bengaluru |
| 60–64 | Tamil Nadu | 6-South | IMH Chennai, **Sneha Chennai**, Tele MANAS |
| 67–69 | Kerala | 6-South | Tele MANAS, Govt Medical College Trivandrum |
| 70–74 | West Bengal | 7-East | Pavlov Hospital Kolkata, Tele MANAS |
| 75–77 | Odisha | 7-East | Tele MANAS, SCB Medical College Cuttack |
| 78 | Assam + NE States | 7-East | **LGBRIMH Tezpur** (apex for NE), Tele MANAS |
| 79 | NE States (Arunachal, Manipur, Meghalaya, Mizoram, Nagaland, Tripura, Sikkim) | 7-East | LGBRIMH Tezpur, Tele MANAS |
| 80–83 | Bihar | 8-East | **CIP Ranchi** (apex), Tele MANAS |
| 84–85 | Jharkhand | 8-East | **CIP Ranchi** (apex), Tele MANAS |
| 90–99 | Army Postal Service (APS) | 9-Military | AFMS Tele MANAS Cell (AFMC Pune), 14416 |

**Key insight for offline routing**: First 2 digits → state lookup (28 rows) → show state-relevant institutions + national defaults (Tele MANAS always shown).

### 1.3 How PIN-to-State Actually Works

The first 2 digits are NOT a perfect 1:1 state mapping — some states share prefixes (e.g., 50 = both Telangana and AP; 79 = multiple NE states). For the app:

- **Use first 2 digits as primary key** (28 rows, <1KB)
- **Fall back to first 1 digit** (9 zones) if ambiguous
- **Never need more than first 2 digits** for the "near you" routing — we just need state-level, not street-level
- **PIN prefix table is deterministic** — same input always gives same state/region

**Source**: India Post official structure (uat.indiapost.gov.in), Wikipedia, odialive.com/first-2-digits-of-pincode-state-wise/

---

## 2. INSTITUTIONS BY CATEGORY — PTSD/Trauma Care in India

### 2a. Apex Institutes (3 Central + NIMHANS-2 announced)

| Institution | City | State | PIN zone | Phone | What they do for PTSD | Source |
|---|---|---|---|---|---|---|
| **NIMHANS** | Bengaluru | Karnataka | 56–59 | 080-46110007 | Apex national institute. Dept of Psychosocial Support in Disaster Management (DPSSDM). CPT for PTSD research. Helpline for disasters. | nimhans.ac.in |
| **CIP** (Central Institute of Psychiatry) | Ranchi | Jharkhand | 80–85 | — | Upgraded to Regional Apex Institution (Budget 2026). 500-bed expansion planned. Serves Bihar/Jharkhand/east India. | pib.gov.in/PRID=2221458 |
| **LGBRIMH** (Lokopriya Gopinath Bordoloi Regional Institute of Mental Health) | Tezpur | Assam | 78 | — | Upgraded to Regional Apex Institution (Budget 2026). Serves all 8 NE states. Key for addiction + trauma from geographic isolation. | pib.gov.in/PRID=2221458 |
| **NIMHANS-2** (announced) | Northern India (TBD) | TBD | 1–2 (likely) | — | Announced Budget 2026-27. No national mental health institute in North India. Location not yet announced. Modeled on NIMHANS Bengaluru. | thehindu.com, livemint.com (Feb 2026) |

**Tripartite MoU (Aug 2026)**: NIMHANS, CIP, and LGBRIMH signed a tripartite MoU for capacity building, training of trainers, joint research, tele-mentoring. (sentinelassam.com, Aug 2026)

### 2b. Armed Forces PTSD Care

| Resource | City | Access | Details | Source |
|---|---|---|---|---|
| **AFMC Dept of Psychiatry** | Pune | AFMS beneficiaries (serving + veterans + dependents) | Tertiary mental health care. Developed PROJECT MANAS digital wellbeing platform. Two-week capsule courses on military psychiatry. | afmc.nic.in/Psychiatry |
| **AFMS Tele MANAS Cell** | AFMC Pune | **All armed forces beneficiaries nationwide** — call **14416** | Dedicated cell for defence personnel. Operational 24x7. Inaugurated Dec 2023 by CDS Gen Anil Chauhan. MoU between MoHFW and MoD. | pib.gov.in/PRID=1981584 |
| **NIMHANS-AFMS MoU** | Bengaluru + all AFMS | Collaborative | MoU signed March 2025 for PTSD, anxiety, depression research + training for military. Faculty exchange, specialized psychiatric care. | pib.gov.in/PRID=2113122 |
| **Military Hospitals** | Across India | AFMS beneficiaries | All Command Hospitals, base hospitals have psychiatry departments. PTSD screening integrated post-2025. | afmc.nic.in |

**Key for directory**: Armed forces users → show AFMS Tele MANAS Cell (14416, same number as civilian Tele MANAS but routed to military-specific counsellors).

### 2c. Govt Medical College Psychiatry + District Hospital (DMHP)

**DMHP scale**: Sanctioned in **767 districts** across India. Each district hospital has a psychiatry OPD.

**What a user gets at DMHP**:
- Free psychiatry consultation (₹0–10 registration)
- Free medications from hospital pharmacy
- Counselling (clinical psychologist on team)
- 10-bed inpatient facility at district level
- Satellite clinics at CHCs/PHCs (4/month)
- Walk-in, no referral needed

**DMHP team per district**: Psychiatrist, Clinical Psychologist, Psychiatric Social Worker, Psychiatric Nurse, M&E Officer, Case Registry Assistant, Ward Assistant.

**How does a user find their district's DMHP clinic?**
1. **Call Tele MANAS 14416** → ask for nearest DMHP facility in your district
2. **Go to district hospital** → ask for "Psychiatry OPD" or "DMHP centre"
3. **Call 104** (National Health Helpline) → ask for district hospital psychiatry
4. **No online directory exists** that's user-friendly — this is the gap Anchor fills offline

**Sources**: MoHFW (mohfw.gov.in/?q=en/pressrelease-287), PIB PRID=2247567, citizennest.com/guide/national-mental-health-programme-nmhp, fittour.in/guides/free-depression-treatment-india-government-options

### 2d. Disaster/Conflict Specialists

| Resource | City | Focus | Contact | Source |
|---|---|---|---|---|
| **NIMHANS DPSSDM** (Dept of Psychosocial Support in Disaster Management) | Bengaluru | Nodal centre for disaster psychosocial support since 2005. Centre of Excellence (NDMA 2010). COVID-19 nodal centre 2020. | 080-46110007 (24x7 toll-free) | nimhans.ac.in/departments/psychosocial-support-in-disaster-management |
| **Kashmir Lifeline** | Srinagar, J&K | Conflict/disaster trauma. Trauma-informed counsellors. Kashmiri-speaking. | **18001807020** (toll-free) | kashmirlifeline.org |
| **NIMHANS PSSMHS Helpline** | Bengaluru (national) | 24x7 toll-free for disaster survivors nationwide. First India institute to start disaster-specific helpline. | **080-46110007** | nimhans.ac.in/pssmhs-helpline/ |

### 2e. NGOs with Walk-in/Clinic Services

| NGO | City | What they offer | Cost | Walk-in? | Source |
|---|---|---|---|---|---|
| **Sangath** | Goa (+ Delhi hub) | Evidence-based therapies for depression, anxiety, trauma. Task-sharing model. COVID Wellbeing Centre (free tele-counselling). Clinical services (paid). | Paid clinical services (amount not public) | By appointment | sangath.in, sangath.in/clinical-services/ |
| **TheMindClan** | Mumbai (primary) + online nationwide | Curated therapist directory. In-person support groups in Mumbai (Bandra). LGBTQIA+ inclusive. Trauma-specialized therapists listed. | Varies (therapists set own rates) | Support groups: walk-in (Mumbai). Therapists: appointment. | themindclan.com |
| **Parivarthan** | Bengaluru | Counselling helpline + in-person. Trauma training workshops. Institutional counselling (schools, corporates, NGOs). | Sliding scale | Counselling helpline (PCH) + in-person by appointment | parivarthan.org, Phone: 080-25298686 |
| **Richmond Fellowship Society (India)** | Bengaluru (HQ) + branches | Psychosocial rehabilitation for chronic mental illness. Therapeutic community model. Not PTSD-specific but serves severe mental illness. | Subsidized | Residential + outpatient | rfsindiafoundation.org |
| **Sneha** | Chennai | 24/7 suicide prevention. Not PTSD-specific but trauma-informed. Tamil-speaking. | Free | Helpline (not walk-in clinic) | snehaindia.org |

### 2f. Private Trauma-Specialized Centers

| Platform | City/Online | Trauma specialization | Cost per session | Walk-in? | Source |
|---|---|---|---|---|---|
| **Amaha** | Online + Mumbai | PTSD specialist clinicians, EMDR. Integrated psychiatry. Proprietary clinical protocols. | ₹1,000–₹3,500+ | No (appointment only, online) | amahahealth.com |
| **LeapHope** | Online (India + international) | Trauma-informed care, EMDR, CBT, DBT. RCI-registered psychologists. | From $19/session (~₹1,600) to $39/session (~₹3,250) | No (online appointment) | leaphope.com |
| **Wysa** | Online (app-based) | AI chatbot + human therapy. Mood/anxiety focus (not PTSD-specific). | Free tier + premium | No (app) | wysa.com |
| **Your Emotional Wellbeing (YEW)** | Online | First-time therapy users. ₹99 intro session. | ₹99 intro → ₹1,500–2,500 ongoing | No (online) | youremotionalwellbeing.org |

**Cost reality for offline display**:
- Govt OPD: ₹10–50
- Private therapy: ₹800–2,500/session
- Budget online: ₹499–800/session
- Trauma-specialized: ₹1,799+/session
- Almost no cashless insurance for therapy (2026)

---

## 3. DIRECTORY MODEL — Fields & Seed Entries

### 3.1 Proposed Fields per Entry

```json
{
  "id": "nimhans_bengaluru",
  "name": "NIMHANS",
  "fullName": "National Institute of Mental Health and Neuro Sciences",
  "city": "Bengaluru",
  "state": "Karnataka",
  "pinPrefix": ["56", "57", "58", "59"],
  "zone": 5,
  "phone": ["080-46110007"],
  "tollFree": true,
  "category": "apex_institute",
  "traumaSpecialist": true,
  "costBand": "free",
  "walkIn": true,
  "hours": "24/7 (helpline), OPD: Mon-Sat 9AM-1PM",
  "languages": ["English", "Kannada", "Hindi"],
  "url": "https://nimhans.ac.in",
  "offlineAvailable": true,
  "description": "India's apex mental health institute. PTSD research + treatment. Disaster psychosocial support."
}
```

**Key design decisions**:
- `pinPrefix`: Array of first-2-digit prefixes that route to this entry (for offline lookup)
- `costBand`: `"free"` | `"subsidized"` | `"paid"` | `"variable"` — NOT exact prices (those change)
- `walkIn`: boolean — critical for "I need help NOW" users
- `offlineAvailable`: boolean — phone numbers work offline, URLs don't
- `category`: for UI filtering (see Section 4)

### 3.2 Seed Entries (~20 entries covering all zones)

| # | Name | City | State | PIN prefix | Category | Cost | Walk-in | Phone |
|---|---|---|---|---|---|---|---|---|
| 1 | **Tele MANAS** | National | All | ALL | helpline | Free | N/A (call) | 14416, 1800-891-4416 |
| 2 | **Emergency** | National | All | ALL | emergency | Free | N/A (call) | 112 |
| 3 | **Vandrevala Foundation** | National | All | ALL | helpline | Free | N/A (call/WhatsApp) | 9999666555 |
| 4 | **NIMHANS** | Bengaluru | Karnataka | 56–59 | apex_institute | Free (govt OPD) | Yes | 080-46110007 |
| 5 | **CIP Ranchi** | Ranchi | Jharkhand | 80–85 | apex_institute | Free (govt OPD) | Yes | — |
| 6 | **LGBRIMH Tezpur** | Tezpur | Assam | 78 | apex_institute | Free (govt OPD) | Yes | — |
| 7 | **AFMS Tele MANAS Cell** | Pune | Maharashtra | 40–44 | armed_forces | Free | N/A (call) | 14416 |
| 8 | **AIIMS Delhi** | New Delhi | Delhi | 11 | govt_medical | Free (₹10 reg) | Yes | 011-26588500 |
| 9 | **IHBAS Delhi** | Delhi | Delhi | 11 | govt_hospital | Free | Yes | 011-22582918 |
| 10 | **Kashmir Lifeline** | Srinagar | J&K | 18–19 | helpline | Free | N/A (call) | 18001807020 |
| 11 | **Sneha** | Chennai | Tamil Nadu | 60–64 | helpline | Free | N/A (call) | 044-24640050 |
| 12 | **iCall (TISS)** | Mumbai | Maharashtra | 40–44 | helpline | Free | N/A (call) | 9152987821 |
| 13 | **Sangath** | Goa | Goa | 40 | ngo_clinic | Paid (varies) | Appt | sangath.in |
| 14 | **Parivarthan** | Bengaluru | Karnataka | 56–59 | ngo_clinic | Sliding scale | Appt | 080-25298686 |
| 15 | **TheMindClan** | Mumbai | Maharashtra | 40–44 | ngo_directory | Varies | Groups: walk-in | themindclan.com |
| 16 | **Amaha** | Mumbai (online) | Maharashtra | 40–44 | private_clinic | ₹1,000–3,500 | Appt only | amahahealth.com |
| 17 | **LeapHope** | Online (India) | National | ALL | private_clinic | ₹1,600–3,250 | Appt only | leaphope.com |
| 18 | **IMH Chennai** | Chennai | Tamil Nadu | 60–64 | govt_hospital | Free | Yes | — |
| 19 | **Pavlov Hospital** | Kolkata | West Bengal | 70–74 | govt_hospital | Free | Yes | — |
| 20 | **NIMHANS PSSMHS Helpline** | Bengaluru (national) | Karnataka | 56–59 | disaster_helpline | Free | N/A (call) | 080-46110007 |

**Coverage**: All 9 zones represented. Free options first in every zone. Mix of helplines (offline), walk-in clinics (offline), and online-only (badgeged).

---

## 4. UI CATEGORIZATION — Get Help Screen Taxonomy

### Proposed Screen Structure

```
┌─────────────────────────────────────────┐
│  GET HELP                               │
├─────────────────────────────────────────┤
│                                         │
│  ── TOOLS (self-guided, always offline) ──
│                                         │
│  ┌──────────┐  ┌──────────┐            │
│  │ Grounding│  │ Breathing│            │
│  │ (5-4-3-2-1)│ │ (Box)   │            │
│  │ 🟢 Offline│  │ 🟢 Offline│           │
│  └──────────┘  └──────────┘            │
│  ┌──────────┐  ┌──────────┐            │
│  │ Body Scan│  │ Safe Place│           │
│  │ 🟢 Offline│  │ 🟢 Offline│           │
│  └──────────┘  └──────────┘            │
│                                         │
│  ── GET HELP (people & places) ─────────
│                                         │
│  ┌──────────┐  ┌──────────┐            │
│  │ Crisis   │  │ Nearby   │            │
│  │ Helplines│  │ Support  │            │
│  │ 🟢 Offline│  │ 🟢 Offline│           │
│  │ 14416... │  │ PINCODE→ │            │
│  └──────────┘  └──────────┘            │
│  ┌──────────┐  ┌──────────┐            │
│  │Find a    │  │Community │            │
│  │Therapist │  │ Support  │            │
│  │🔴 Online │  │ 🔴 Online│            │
│  └──────────┘  └──────────┘            │
│                                         │
│  ── JOURNAL (private, offline) ─────────
│                                         │
│  ┌──────────────────────────────────┐  │
│  │ Private Pattern Log              │  │
│  │ 🟢 Offline · Never leaves device │  │
│  └──────────────────────────────────┘  │
│                                         │
└─────────────────────────────────────────┘
```

### Square-Card Structure (per card)

```
┌─────────────────────┐
│ [ICON]              │
│ Title (2-3 words)   │
│ One-liner (≤15 words)│
│ 🟢 Offline / 🔴 Online │
└─────────────────────┘
```

### Card Definitions

| Section | Card Title | One-liner | Icon | Badge | Destination |
|---|---|---|---|---|---|
| **TOOLS** | Grounding | 5-4-3-2-1 senses exercise | 🫁 | 🟢 Offline | Grounding screen |
| **TOOLS** | Breathing | Box breathing with haptics | 💨 | 🟢 Offline | Breathing screen |
| **TOOLS** | Body Scan | Guided body awareness | 🧘 | 🟢 Offline | Body scan screen |
| **TOOLS** | Safe Place | Guided visualization | 🏠 | 🟢 Offline | Safe place screen |
| **GET HELP** | Crisis Helplines | Call now — free, 24/7, 20 languages | 📞 | 🟢 Offline | Helpline list |
| **GET HELP** | Nearby Support | Type your PINCODE for local help | 📍 | 🟢 Offline | PINCODE directory |
| **GET HELP** | Find a Therapist | Browse trauma-specialized therapists | 👩‍⚕️ | 🔴 Online | External link |
| **GET HELP** | Community | Support groups & shared experiences | 🤝 | 🔴 Online | External link |
| **JOURNAL** | Private Log | Track patterns, never leaves device | 📓 | 🟢 Offline | Journal screen |

### "Nearby Support" PINCODE Flow

1. User taps "Nearby Support" card
2. PINCODE input screen (6-digit, numeric keyboard)
3. Lookup: first 2 digits → state → institutions for that state
4. Results screen: state-level institutions + national defaults (Tele MANAS always first)
5. Each result: name, phone (tap to call), cost badge, walk-in badge

---

## 5. What CANNOT Be Done Offline — Honest Limitations

| Feature | Why it can't be offline | What to show instead |
|---|---|---|
| **Live availability** | Can't know if a helpline is currently busy, if a hospital has beds, or if a psychiatrist is in today | "Call to check availability" label on every entry |
| **Appointment booking** | Requires real-time calendar sync with provider systems | "Call to book" or "Walk in during OPD hours" |
| **Ratings/reviews** | Requires live data from Google/Practo/etc. | Fixed curated list (Anchor's editorial choice, not crowd-sourced) |
| **Real-time wait times** | No government API exists for OPD queues | "Arrive before 9 AM" advisory text |
| **Insurance coverage check** | Requires insurer API integration | "Most therapy not covered by insurance (2026)" disclaimer |
| **Telehealth video calls** | Requires internet | "Works offline: call only" badge; "Online: video available" badge |
| **Medication availability** | Can't verify which drugs are in stock at which pharmacy | "Medicines free at govt hospital pharmacy" general guidance |
| **Language availability in real-time** | Can't confirm a Kashmir Lifeline counsellor speaking Kashmiri is on shift | Language tags are approximate ("Kashmiri-speaking counsellors available") |
| **NIMHANS-2 location** | Not yet announced (Budget 2026, location TBD) | "Coming soon — Northern India (location TBD)" |

### Design Principle: Under-promise, over-deliver

- **Always show**: Phone numbers, names, cities, cost bands, walk-in status
- **Never promise**: Real-time data, exact availability, guaranteed languages
- **Badge every online-only feature**: "🔴 Requires internet" — never pretend it works offline
- **Cost bands are ranges**: "Free (govt)" or "₹1,000–3,500" — never exact current prices

---

## 6. Sources Summary

| Claim | Source | URL |
|---|---|---|
| PIN structure (6 digits, zone/sub-zone/sorting/post office) | India Post official | uat.indiapost.gov.in/MBE/Pages/Content/Pincode.aspx |
| PIN prefix → state mapping | Wikipedia, pincodesinfo.in, odialive.com | en.wikipedia.org/wiki/Postal_Index_Number |
| DMHP sanctioned in 767 districts | MoHFW press release | mohfw.gov.in/?q=en/pressrelease-287 |
| NIMHANS-2 announced Budget 2026 | PIB, The Hindu, LiveMint | pib.gov.in/PRID=2221458, thehindu.com, livemint.com |
| CIP/LGBRIMH upgraded to Regional Apex | PIB Budget 2026 | pib.gov.in/PRID=2221458 |
| AFMS Tele MANAS Cell at AFMC Pune | PIB Dec 2023 | pib.gov.in/PRID=1981584 |
| NIMHANS-AFMS PTSD MoU 2025 | PIB March 2025 | pib.gov.in/PRID=2113122 |
| NIMHANS DPSSDM (disaster dept) | NIMHANS official | nimhans.ac.in/departments/psychosocial-support-in-disaster-management |
| Kashmir Lifeline | Kashmir Lifeline | kashmirlifeline.org |
| Sangath clinical services | Sangath | sangath.in/clinical-services/ |
| Parivarthan | Parivarthan | parivarthan.org |
| Richmond Fellowship India | RFS India | rfsindiafoundation.org |
| Amaha PTSD therapy + pricing | Amaha, YourEmotionalWellbeing comparison | amahahealth.com, youremotionalwellbeing.org |
| LeapHope pricing | LeapHope | leaphope.com |
| Tele MANAS scale (33L+ calls, 20 languages) | PIB, MoHFW | pib.gov.in/PRID=2226319 |
| Govt mental hospitals: 47 total (3 central + 44 state) | PIB Budget 2026 | pib.gov.in/PRID=2226319 |
| Treatment gap 70-92% | NMHS 2015-16 via PIB | pib.gov.in/PRID=2226319 |
| Cost bands (govt ₹10-50, private ₹800-2500) | Existing research doc | Anchor/docs/india-resources.md |

---

## 7. Rejected alternative: GPS region lookup (folded from near-you-research.md, file deleted)

Region-mapped static directory won over live GPS: Google Places needs
internet + billing (no free tier since Mar 2025); Android Geocoder is
backend-dependent; OSM extracts are GBs. The GPS fix itself works offline
but costs a COARSE_LOCATION permission + ~1.5h permission flow for zero
extra routing value over the PIN table. Kept as fallback only if pincode
entry ever tests poorly. Sources: developer.android.com location +
Geocoder docs, Places billing page.
