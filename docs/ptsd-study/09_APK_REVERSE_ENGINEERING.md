# 09 — APK Reverse Engineering (static analysis, 2026-09-05)

Extends 01–08 with ground truth from the actual artifact:
`PTSD+Coach_4.0.1_APKPure.xapk` (71MB). Method: unzip + strings + python
only (no decompilation). All work in /tmp, never in repo. Paraphrase-only
record — zero verbatim text, audio, images, or code taken.

## Package truth
- Flutter app (flutter_assets, 6 dex files ~28MB). Base APK 41MB + 29MB
  arm64 libs + locale splits (EN/FR-CA/ES-419 ship).
- Audio dir: ONLY `silence.mp3` + `ending_tone.mp3`. Exercise audio is
  NOT bundled and NOT freesound — per-tool descriptors point to VA-hosted
  CDN MP3s (+ `.srt` subtitles, background PNG), `has_downloadable_content`
  = download-on-demand + offline cache. Do NOT hotlink.
- Asset packages reveal architecture: va_audio, va_tools_common,
  va_list_tools, va_content_refresher, va_video, webviewtube, record_web,
  wakelock_plus. 306 images/icons.

## Tool catalog: 43 entries (tools.json)
Per-tool fields: id, title, tool_type, image/thumbnail, hook_line,
symptoms[ids 1–8], audio_tool_type (simple_player | countdown_with_settings),
intro/help/content file pointers, chips (time labels), tool_settings
(show_done, show_info, can_add_custom + image/link, max 140 chars,
can_share_on_va_app_connect, enable_timer). 12 audio tools · 1 video
(passengers on the bus) · ~16 content_refreshers · structured tools
(coping cards 4-step wizard, i-messages, RID triggers, my strengths,
soothing images, custom tool, time out, worry time, my feelings, moral
elevation, express with art, mindful meditation, relationship beliefs).
Every tool: intro + help + content JSONs × 3 locales.

## Their "router" and tracker
- `managing_symptoms_routine.json`: STATIC ordered routine
  ({sequence, repeat, activity-id}) — not a learned ranker. Our
  InterventionRouter (vetoes + outcome reranking) is architecturally ahead;
  their repeat-flag shape is worth a Tier1 `repeat` on RoutineStep.
- `tracking_my_triggers/`: 7-step wizard (triggers.json + step_N
  info/content × 3 locales). Informs our 3-tap check-in UI copy.
- Symptom ids 1–8 have no legend file in assets (compiled into Dart) —
  do NOT assume meanings; our CurrentState enum serves this role.

## Corrections to 07 gap analysis (ground truth applied)
- 07 was written WITHOUT opening Anchor's codebase and says so — the
  buckets that matter check out: router/filter/catalog/tests exist but
  are devtools-only (matches our exp-15 audit: ~15% integration).
- PCL-5 model exists with catalog + scorer (07's Module 13 safety-footer
  recommendation stands: add it to results screens).
- "Custom Tool" in their catalog validates our routine-builder USP
  direction; their 140-char custom cap matches our micro-note philosophy.

## Take-list deltas (see docs/apk-inventory.md for the locked list)
TAKE: symptom→tool shape, routine+repeat shape, tracker wizard structure,
coping/strengths/feelings journals (mood-notes only), custom-tool pattern,
time-out/worry-time/art, downloadable-metadata pattern (validates OUR
bundled decision — never network delivery), intro/help/content pattern,
chips/hooks/done-flags, 140-char cap. SKIP: video, relationship/kids,
PTSD facts, ambient sounds, their audio URLs. NEVER: verbatim text,
audio, images, code, VA branding. Gate-reviewed (no lock violations,
no IP breaches).
