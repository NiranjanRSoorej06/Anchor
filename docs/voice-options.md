# Natural Voice Options (2026-09-05)

Robotic built-in TTS is the complaint. Verdict: **cloud-generate once
(Sarvam), bundle MP3s, play offline** — natural voices, zero runtime
internet/permissions/cost. Full comparison below; all URLs verified Sep 2026.

## Options
- **ElevenLabs:** best global quality; Hindi+Tamil+Telugu+8 more Indic in
  Eleven v3. Cloud-only (INTERNET + API key). ~$0.10/1K chars (~$1.80 for
  15 min × 3 langs). ToS §4(a) permits redistributing generated audio.
  Starter $6 = commercial rights, no attribution.
- **Sarvam Bulbul v3 (RECOMMENDED generator):** best Indic quality
  (60.1% telephony win vs ElevenLabs; Hinglish + name pronunciation).
  11 languages. Cloud-only API. **~₹54 (~$0.65) total** for 15 min × 3
  langs. EULA §17.4 assigns Output rights, commercial use incl. shipped
  apps, perpetual. REST 2,500 chars/req.
- **Google on-device TTS:** only eSpeak works fully offline (robotic,
  unacceptable). Neural voices are cloud-cached, expire. Not warm.
- **Piper/Sherpa-ONNX:** mid-tier neural, Hindi/Tamil/Telugu available,
  15–30MB/voice, ~2–4h integration — but **GPL-3.0** (espeak-ng link).
  Kokoro-82M nicer (~115MB, slower on budget phones, also GPL wrapper).
- **Coqui XTTS:** 2GB + non-commercial license. No. **Meta MMS:** not
  Android-ready. **Bhashini:** cloud, not self-serve. **AI4Bharat
  IndicTTS:** best OPEN option (Apache-2.0), convertible to Piper format.

## The killer combo (demo + release)
1. Render all GuidedScripts via **Sarvam Bulbul v3** (~₹54 one-time).
2. Bundle MP3s per language (~7–14MB/lang at 64–128kbps; AAB splits serve
   one language per device; 200MB Play limit not threatened).
3. Play offline with MediaPlayer/ExoPlayer (gapless via
   ConcatenatingMediaSource). Zero internet, zero API calls, zero
   permissions at runtime.
Fallback: Android built-in TTS (~2–3h, robotic, zero permissions).
Never: runtime cloud TTS in the acute path (latency + offline kill).

## Sources
- ElevenLabs pricing/terms: elevenlabs.io/pricing, /terms-of-use, /oem-terms
- Sarvam pricing/EULA/commercial: sarvam.ai/api-pricing, /eula, /api/getting-started/commercial-licensing
- Sherpa-ONNX: github.com/k2-fsa/sherpa-onnx · Piper voices: huggingface.co/rhasspy/piper-voices
- AAB limits/splits: developer.android.com/guide/app-bundle
