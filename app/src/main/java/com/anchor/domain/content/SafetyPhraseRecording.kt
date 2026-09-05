package com.anchor.domain.content

/**
 * A saved recording tied to one safety phrase — either one of
 * [SafetyPhraseBank.DEFAULTS] or a user-authored custom phrase that
 * already passed [SafetyPhraseBank.isAcceptable].
 *
 * This is plain Kotlin: no Android, no Compose. [filePath] is an
 * app-private absolute path (never a raw external `content://` URI) —
 * recordings are made directly to app storage, and imported files are
 * copied in on pick, matching this app's "copy on pick" convention for
 * every other piece of user-supplied media (the safe-place photo,
 * custom comfort-tool audio).
 *
 * Per `docs/vision.md` non-negotiable #6: recordings are capped at
 * [MAX_DURATION_MS] (~15s) — enforced by the recording UI, not this
 * model — and playback should start in well under 300ms, which is why
 * this stays a plain local file path rather than anything requiring a
 * network round-trip or on-the-fly transcoding.
 *
 * @property id Stable identifier — `"bundled_<index>"` for a recording
 *   against one of [SafetyPhraseBank.DEFAULTS], or `"custom_<uuid>"` for
 *   a user-authored phrase.
 * @property phraseText The phrase this recording is of.
 * @property filePath App-private absolute path to the audio file.
 * @property createdAtMillis Unix epoch millis when this recording was saved.
 */
data class SafetyPhraseRecording(
    val id: String,
    val phraseText: String,
    val filePath: String,
    val createdAtMillis: Long,
) {
    companion object {
        /** Recording UI must stop/reject anything longer than this. */
        const val MAX_DURATION_MS: Long = 15_000L
    }
}

/**
 * Pure-Kotlin validator for [SafetyPhraseRecording]. Collects every
 * failure without short-circuiting so the caller can present the full
 * list at once — same convention as every other validator in `domain/`.
 */
object SafetyPhraseRecordingValidator {

    fun validate(recording: SafetyPhraseRecording): ValidationResult {
        val reasons = mutableListOf<String>()

        if (recording.id.isBlank()) reasons += "id must not be blank"
        if (recording.filePath.isBlank()) reasons += "filePath must not be blank"
        if (!SafetyPhraseBank.isAcceptable(recording.phraseText)) {
            reasons += "phraseText must be non-blank and at most ${SafetyPhraseBank.MAX_WORDS} words"
        }
        if (recording.createdAtMillis < 0) reasons += "createdAtMillis must not be negative"

        return if (reasons.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(reasons)
    }

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val reasons: List<String>) : ValidationResult
    }
}
