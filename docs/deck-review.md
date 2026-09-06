# Deck review — full pass (2026-09-06)

Source: team PDF export, 15 slides. Severity: BLOCKER > FIX > NIT.

## BLOCKER

1. **Team name inconsistent.** Title slide says "Team Hogorithm"; earlier
   team messages said "Hogrithm". One of them disagrees with the official
   registration. Confirm the registered spelling and use it everywhere
   (deck + demo + repo).

## FIX (wrong or broken copy)

2. **Typo, problem slide:** header line reads "peole with PTSD" → "people".
3. **Broken sentence, Solution 02:** "looping a loved one's recorded then
   flows into journaling" — missing noun. Fix: "looping a loved one's
   recorded voice note, then flows into journaling and incident logging."
4. **Text artifact, Solution 02:** "AANNCCHHOORR NNOOWW" (letter-spacing
   blowup). Fix: "ANCHOR NOW".
5. **Grammar, 01b right column:** "The right helpline exist reaching them
   needs to be made easy." Fix: "The right helplines exist — reaching them
   needs to be easy."
6. **Spacing typo, 01b:** "weekly at best , episodes" — stray space before comma.
7. **Unsourced stat, problem slide:** "~80%" still has no citation. Replace
   both stat cards: card 1 → "3.9% lifetime PTSD worldwide — 7 in 10 face
   serious trauma (Koenen et al., 2017)"; card 2 → "70–92% treatment gap for
   mental disorders in India (NMHS 2015–16)". Drop the "24×7" rhetoric card
   (not a statistic) and the "therapy is an hour a week" line (insults
   future partners).
8. **Redundant numbering, Future 08:** columns read "01 6 MONTHS … 01 /
   02 1 YEAR … 02 / 03 BEYOND … 03". Keep one number set.

## NIT (polish if time)

9. 01b footnote ("Even the benchmark free app…") is the right call — keep
   it small exactly as is; do not promote it back to cards.
10. Footers claiming "shown live on the device today" (02b) and "LIVE ON
    DEVICE — 90-SECOND DEMO" (03b): true only if the SDK build runs them.
    If unverified at pitch time, soften to "built for live demo".
11. Tech 04 card 08 ("the team is finite") reads self-deprecating; consider
    "a focused team ships faster" energy instead. Optional.
12. Header numbering mixes styles ("01 ·", "01b", "02 ·", "02b", bare
    "04"–"08"). Harmless, but unifying to one pattern is free polish.

## Z.ai fix prompt (paste as-is)

---
STYLE LOCK (do not redesign): dark navy background (#0A1633-ish), cream
serif display headlines, teal (#7FB5A8-ish) eyebrow labels and accents,
coral/orange (#E86A4A-ish) sparingly for hero numbers and the ANCHOR NOW
button, rounded cards with thin borders, generous whitespace. Keep all 15
slides in the same order with the same headlines. Change ONLY the items
below; touch nothing else.

1. Title slide: confirm team name spelling against our registration
   ("Hogorithm" vs "Hogrithm") and use the registered spelling.
2. Problem slide: fix "peole" → "people". Replace the two stat cards with:
   card 1 — big "3.9%", sub "lifetime PTSD worldwide — 7 in 10 face serious
   trauma", source "Koenen et al., 2017"; card 2 — big "70–92%", sub
   "treatment gap for mental disorders in India", source "NMHS 2015–16".
   Delete the "24×7 / therapy is an hour a week" card and line.
3. Solution 02: fix the grounding card to "…looping a loved one's recorded
   voice note, then flows into journaling and incident logging." Fix the
   stretched "AANNCCHHOORR NNOOWW" text to "ANCHOR NOW".
4. Slide 01b: fix "The right helpline exist reaching them needs to be made
   easy." → "The right helplines exist — reaching them needs to be easy."
   Fix "weekly at best , episodes" spacing. Keep the PTSD Coach footnote
   exactly as is (small, bottom, no promotion).
5. Future 08: remove the duplicated trailing numbers so columns read once:
   "01 · 6 MONTHS", "02 · 1 YEAR", "03 · BEYOND".
6. If time permits: unify header numbering style; soften "the team is
   finite" to something confident.
Return the full corrected deck, same 15-slide order, same design system.
---
