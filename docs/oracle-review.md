# Oracle Review — Anchor (2026-09-04)

Source: ora-1 / ses_f925929ffffe1qxz8HIP12Ajba (reconciled 2026-09-04)

## Matrix (9 candidates vs H1-H7)
- Strong: #4 vibrotactile breathing, #7 trusted-contact, #9 anchor phrase (evidence-based, privacy-respecting, demoable, hit C,F,G)
- Weak: #5 brown-noise (kills work-safe), #8 EMA (invisible in 90s demo)
- Risky: #6 env-sound reality check (ML fantasy in 30h), #2 hangover mode (weeks of UX), #1/#3 haptic fragmentation
- Out-of-scope: #6 impossible (mic + battery + misclassification liability), #2 invisible

## Kill list
- Kill #6 env-sound, #2 hangover, #5 brown-noise, #8 EMA for MVP. Keep for "what's next" slide.

## MVP slice (recommended)
Hero: #4 visual breathing + optional haptic + pre/post SUDS (works <60s, zero perms, work-safe, fallback visual-only)
Support1: #9 self-recorded anchor phrase (10s record, local only, fallback pre-loaded phrases if mic denied)
Support2: #7 trusted-contact discreet SMS (1-3 contacts, pre-composed editable, Intent/SMS, fallback copy-to-clipboard/share)

Journey: breathe -> self-talk -> reach out. Each has permission-free fallback.

## Risks
- Haptic fragmentation HIGH -> visual is core, haptic polish
- Mic trust MEDIUM -> plain-language, default no-mic
- Reliability HIGH -> offline-first, test open->breathe->SUDS->close 20x, no login/network in core
- Safety CRITICAL -> exit always visible, SUDS + try-something-else, no forced trauma dive, crisis footer not popup, no mandatory assessments
- Privacy MEDIUM -> zero network in demo, show 0-perms screen
- Stack: Android native Kotlin preferred (haptic control), ~3 screens + privacy screen, Room/SQLite, no backend

## 90s demo script
0-10s problem (VA for veterans, 17 perms, no CPTSD, generic, no human)
10-30s breathing (no login, <1s open, silent desk-safe)
30-50s anchor phrase (your voice not 2011 script)
50-70s trusted contact (1 tap discreet SMS, you choose who/words)
70-85s privacy + inclusivity (0 perms, open-source, local-only, trauma-type picker)
85-90s ask (zero perms + 60s works vs 17 perms + n.s. effect)

Full text in task result ora-1.
