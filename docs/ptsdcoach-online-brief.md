# PTSD Coach Online Brief (2026-09-05)

Deep study of ptsd.va.gov/apps/ptsdcoachonline (17 tools, videos,
worksheets, PDF transcripts for every audio exercise). Rule: VA employee
works = public domain → adapt structure freely, own wording always.
Videos/branding/stock/audio = hands off. PCL-5: already built; keep with
Weathers 2013 citation.

## Site shape (what to steal structurally)
- 17 tools across 10 problem categories (worry, anger, sadness, sleep,
  reminders, avoidance, disconnection ×2, problem-solving, direction).
- Every audio tool = coach pattern: normalize → guide with pacing cues
  ("Ready? Okay, let's begin… Very good") → encourage practice.
- Worksheets (Values, Problem-Solve, Assertiveness) = interactive
  structured reflection → our future Learn module, not demo.
- SUDS before/after every tool (we use Better/Same/Worse — same instinct).

## Transcript URLs (adapt structure, never wording)
- Breathing: .../relax-through-breathing/pages/files/breathing-retraining-transcript.pdf
- Grounding: .../be-in-the-moment/pages/files/be-where-you-are-transcript.pdf
- PMR: .../relax-your-body/pages/files/progressive-muscle-relaxation-transcript.pdf
- Visualization ×3 (beach/country-road/forest, 10–13 min): scene → sensory
  deepening → personal choice → safe return. Ours should be Indian
  landscapes (monsoon garden, temple courtyard, mountain stream).
- Mindfulness: observe-feelings + observe-thoughts (sensation before
  naming; "thoughts are like clouds").
- Sleep: change-how-you-think-about-sleep.pdf (cognitive) + tips-for-sleeping-better.pdf.
- Base for all: https://www.ptsd.va.gov/apps/ptsdcoachonline/

## TTS verdict (for SDK machine)
- Android built-in TextToSpeech: offline after one-time language-pack
  download, ZERO permissions, Hindi/Tamil/Telugu supported,
  setSpeechRate/setPitch for calming pace. Robotic but adequate.
  No Gradle dep (framework API). ~2–3h. RECOMMENDED as stretch.
- Bundled MP3: best quality, needs voice actors + ~24MB/language.
  Not hackathon. Post-demo: pre-render 2–3 key scripts (EN + HI).
- Neural offline (Piper/Sherpa-ONNX): good quality, 10–50MB/language,
  ARM64 caveats, new dependency. Future only.
- MVP stays text-only. TTS reads our GuidedScripts verbatim — no new
  content needed.

## Top 5 takeaways (value/effort)
1. Coach pattern (normalize→guide→encourage): add preamble + closing
   steps to our scripts. LOW.
2. Problem→tool routing: our router already does this; add an "I'm
   feeling ___" entry layer later. MEDIUM.
3. Indian visualizations: 2 new scripts in our ScriptStep model. MEDIUM.
4. Worksheets → future Learn module (Thought Record, Values). HIGH, post-MVP.
5. Normalize framing: add NORMALIZE statement category; psychoeducation
   as normalize→explain→act. LOW.
