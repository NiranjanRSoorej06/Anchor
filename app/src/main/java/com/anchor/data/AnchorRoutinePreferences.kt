package com.anchor.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Which comfort tool plays when the user hits SOS, and what calming audio
 * (if any) loops underneath it — set via Edit Anchor. Backed by
 * [SharedPreferences], same pattern as every other store in this package.
 *
 * Kept as a small dedicated store rather than forcing the full
 * `domain/routine` [com.anchor.domain.routine.Routine] step-sequence model
 * to represent "pick one comfort tool + one looping audio track" — that
 * model's job (an ordered, multi-step, reorderable routine) is a larger,
 * still-unbuilt editor UI (see `docs/demo-plan.md`'s own "routine builder
 * UI — CUT for demo"), while this is exactly what Edit Anchor needs today.
 */
class AnchorRoutinePreferences(context: Context) {

    /**
     * Built-in comfort tool choices, in the order shown by Edit Anchor.
     *
     * [VISUALIZATION] plays [com.anchor.domain.content.GuidedScripts]'
     * `visualization_monsoon` script — internally-guided safe-place
     * imagery, the one form of "visual" grounding with a real (if narrow)
     * evidence base: Korn & Leeds (2002), *Journal of Clinical Psychology*
     * 58(12), 1465-1487 — EMDR resource-installation / safe-place work in
     * complex-PTSD stabilization. See `docs/exercise-evidence.md`'s
     * ADJUNCT_EVIDENCE entry. This is deliberately internally-imagined,
     * not curated photo/video playback — the clinical grounding
     * literature (SAMHSA trauma-informed care; Hammond & Brown 2025)
     * doesn't support watching media as a grounding technique, only
     * attending to the real environment or imagining a safe place.
     */
    enum class ComfortTool(val id: String, val label: String) {
        BREATHING("breathing", "Paced breathing (default)"),
        GROUNDING_54321("grounding_54321", "5-4-3-2-1 grounding"),
        PMR("pmr_full", "Muscle release (PMR)"),
        VISUALIZATION("visualization_monsoon", "Safe-place visualization"),
        CAMERA_GROUNDING("camera_grounding", "Camera grounding");

        companion object {
            fun fromId(id: String): ComfortTool = entries.firstOrNull { it.id == id } ?: BREATHING
        }
    }

    /**
     * Optional generated ambient sound layered under the comfort tool —
     * synthesized on-device ([com.anchor.core.audio.NoiseGenerator]), not
     * a bundled or downloaded file, so there's nothing to source or
     * license. Framed as sound-masking comfort, not noise cancellation
     * (a phone app can't control headset hardware ANC) and not a PTSD-
     * specific clinical claim — evidence for white/pink noise is real but
     * mixed and mostly non-PTSD (see `docs/exercise-evidence.md`).
     * Mutually exclusive with [calmingAudioUri]: a custom file, if set,
     * takes priority over generated noise.
     */
    enum class NoiseType { NONE, WHITE, PINK, RAIN, OCEAN }

    private companion object {
        const val PREF_NAME = "anchor_routine_prefs"
        const val KEY_COMFORT_TOOL_ID = "comfort_tool_id"
        const val KEY_CALMING_AUDIO_URI = "calming_audio_uri"
        const val KEY_CALMING_AUDIO_LABEL = "calming_audio_label"
        const val KEY_SAFE_PLACE_PHOTO_URI = "safe_place_photo_uri"
        const val KEY_SAFE_PLACE_PRESET = "safe_place_preset"
        const val KEY_AMBIENT_NOISE_TYPE = "ambient_noise_type"
    }

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var comfortTool: ComfortTool
        get() = ComfortTool.fromId(prefs.getString(KEY_COMFORT_TOOL_ID, ComfortTool.BREATHING.id)!!)
        set(value) = prefs.edit().putString(KEY_COMFORT_TOOL_ID, value.id).apply()

    /** Persisted content:// URI of a user-picked calming audio file, or `null` for none. */
    var calmingAudioUri: String?
        get() = prefs.getString(KEY_CALMING_AUDIO_URI, null)
        set(value) = prefs.edit().putString(KEY_CALMING_AUDIO_URI, value).apply()

    /** Display name shown in Edit Anchor for the picked file (e.g. its filename). */
    var calmingAudioLabel: String?
        get() = prefs.getString(KEY_CALMING_AUDIO_LABEL, null)
        set(value) = prefs.edit().putString(KEY_CALMING_AUDIO_LABEL, value).apply()

    fun clearCalmingAudio() {
        prefs.edit()
            .remove(KEY_CALMING_AUDIO_URI)
            .remove(KEY_CALMING_AUDIO_LABEL)
            .apply()
    }

    /**
     * Persisted app-private file path (not a `content://` URI — copied on
     * pick, per this app's own convention for anything a user selects
     * from outside the app) of the user's chosen safe-place photo, or
     * `null` for none. Shown behind [ComfortTool.VISUALIZATION]'s
     * narration when set; the technique still works without one.
     */
    var safePlacePhotoPath: String?
        get() = prefs.getString(KEY_SAFE_PLACE_PHOTO_URI, null)
        set(value) {
            // Mutually exclusive with the built-in preset backdrops — a
            // personal photo always replaces a preset choice, so the
            // visualization stage never has to arbitrate between two
            // sources.
            prefs.edit().putString(KEY_SAFE_PLACE_PHOTO_URI, value).remove(KEY_SAFE_PLACE_PRESET).apply()
        }

    fun clearSafePlacePhoto() {
        prefs.edit().remove(KEY_SAFE_PLACE_PHOTO_URI).apply()
    }

    /**
     * Name of a built-in [com.anchor.ui.routine.SafePlacePreset] backdrop
     * (stored as a plain string, not the UI-layer enum, to keep this
     * `data`-package class free of a `ui`-package dependency), or `null`.
     * Mutually exclusive with [safePlacePhotoPath] — picking a preset
     * clears any personal photo.
     */
    var safePlacePresetName: String?
        get() = prefs.getString(KEY_SAFE_PLACE_PRESET, null)
        set(value) {
            prefs.edit().putString(KEY_SAFE_PLACE_PRESET, value).remove(KEY_SAFE_PLACE_PHOTO_URI).apply()
        }

    fun clearSafePlacePreset() {
        prefs.edit().remove(KEY_SAFE_PLACE_PRESET).apply()
    }

    var ambientNoiseType: NoiseType
        get() = NoiseType.entries.firstOrNull { it.name == prefs.getString(KEY_AMBIENT_NOISE_TYPE, null) } ?: NoiseType.NONE
        set(value) = prefs.edit().putString(KEY_AMBIENT_NOISE_TYPE, value.name).apply()
}
