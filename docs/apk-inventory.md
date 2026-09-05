# APK Inventory: PTSD Coach 4.0.1 (static analysis, 2026-09-05)

Source: `PTSD+Coach_4.0.1_APKPure.xapk` (71MB), analyzed with unzip +
strings + python only (no decompilation). All work in /tmp, never in repo.
Findings are structural (schemas, counts, patterns). ZERO verbatim text,
audio, images, or code copied — paraphrase-only record.

## Package shape
- Flutter app (flutter_assets, 6 dex files ~28MB). Base APK 41MB +
  29MB arm64 libs + locale splits (EN/FR-CA/ES-419 — French-Canadian +
  Latin-American Spanish ship).
- Audio dir: ONLY `silence.mp3` (10KB) + `ending_tone.mp3` (191KB).
  Exercise audio is NOT bundled.
- Asset packages reveal architecture: va_audio, va_tools_common,
  va_list_tools, va_content_refresher, va_video, webviewtube,
  record_web, wakelock_plus. Images/icons: 306 files.

## Audio model (CRACKED — answers the freesound question)
- Per-tool descriptor (`audio/audio_files/<tool>.json`): `{name, audioFile,
  subtitleFile (.srt), backgroundImage, isDefault}` — 39 descriptors.
- URLs point to a VA-hosted CDN (`<VA-domain>/<tool>/<tool>.mp3`)
  (+ `.srt` subtitles, background PNG). `has_downloadable_content: true`
  per tool = download-on-demand + offline cache.
- Verdict: NOT freesound, NOT bundled — VA-hosted streaming with download
  for offline. Do NOT hotlink (bandwidth theft + unclear reuse rights).
  Our path stands: Sarvam-rendered MP3s bundled per language.

## Tool catalog: 43 entries (tools.json schema)
Fields per tool: id, title, tool_type, image/thumbnail, hook_line,
is_hidden_in_list, has_downloadable_content, symptoms[ids 1-8],
audio_tool_type (simple_player | countdown_with_settings),
intro/help/content file pointers, chips (time labels), tool_settings
(show_done, show_info, can_add_custom + image/link, max_content_characters
= 140, can_share_on_va_app_connect, enable_timer).
- 12 audio tools (breathing ×2, body scan, emotion surfing, mindfulness
  ×4, loving kindness, muscle relaxation, observe thoughts, positive
  imagery, ambient sounds, mindful walking).
- 1 video tool (passengers on the bus).
- ~16 content_refreshers (grounding, sleep ×2, cognitive restructuring,
  assertiveness, problem solving, pleasant events, nature, soothe senses,
  thought shifting, AboutFace, communication ×3, PTSD facts, kids).
- Structured tools: coping cards (4-step wizard), i-messages, RID
  triggers, my strengths, soothing images, custom tool, time out,
  worry time, my feelings, moral elevation, express with art, mindful
  meditation, relationship beliefs.
- Every tool ships intro + help + content JSONs × 3 locales.

## Recommendations engine (their "router")
`contents/recommendations/managing_symptoms_routine.json`: ordered list of
{sequence, repeat, activity-id} — a STATIC ordered routine with repeat
flags, not a learned ranker. Plus `already_triggered_activities`,
`track_mood_health`, `learn_about_ptsd`, `find_resources` routines.
Takeaway: our InterventionRouter (safety vetoes + outcome reranking) is
architecturally ahead; their routine shape (ordered + repeatable steps)
validates our Routine model — consider a `repeat` flag Tier1.

## Tracking (their trigger tracker)
`contents/assessments/tracking_my_triggers/`: 7-step wizard
(triggers.json + step_N info/content × 3 locales). Multi-step guided
logging with info screens per step — informs our post-SOS check-in UI
copy structure (but ours stays 3 taps, theirs is 7 steps).

## Symptom ids
Tools carry `symptoms: [1..8]` arrays; no legend file found in assets
(legend likely compiled into Dart). Mapping unverified — do NOT assume
meanings. Our CurrentState enum serves the same role with known semantics.

## Permissions (from dex string constants — APPROXIMATE, not manifest)
Broad SDK surface (location, bluetooth, calendar, contacts, camera,
phone, storage-media). These are largely library-declared constants, NOT
proof the app requests them — manifest needs jadx/aapt to confirm.
Do not cite specific permissions without that step.

## TAKE-LIST (locked decisions)
TAKE (adapt structure, own words): symptom→tool mapping shape (validates
router+triage) · routine sequence+repeat shape (consider `repeat` Tier1; requires RoutineStep model + validator + test update) ·
trigger-tracker wizard structure (informs check-in UI copy) · coping
cards/strengths/feelings journals (Module 06-08 content for journal
section; mood-context notes only, NO narrative fields per lock) · custom tool pattern (validates routine-builder USP) · time
out/worry time/moral elevation/art (lightweight future tools) ·
downloadable-content metadata pattern (validates our bundled-content decision; never network delivery) ·
intro/help/content 3-file pattern (future Learn/tool detail UX) · chips,
hook lines, show_done flags (UX details) · 140-char custom-content cap
(matches our micro-note philosophy).
SKIP: video tools (no video infra) · i-messages/relationship/kids content
(out of scope) · PTSD facts (have evidence base) · ambient sounds
(brown noise CUT) · audio files/URLs (VA CDN, do not hotlink).
NEVER: verbatim text, audio, images, code, VA branding.
