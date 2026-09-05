package com.anchor.ui.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A small set of built-in "safe space" backdrops for anyone who doesn't
 * have (or doesn't want to pick) their own photo — the "plethora of
 * inbuilt images" the team asked for, without sourcing or licensing a
 * single external asset. Each is a procedurally-drawn gradient, not a
 * photo — deliberately: real stock/nature photography needs a license
 * this project has no way to clear, and per `docs/exercise-evidence.md`,
 * these are a comfort/preference feature, not a claimed grounding
 * technique either way (same EVIDENCE_GAP framing as a curated photo
 * library would carry).
 *
 * Mutually exclusive with a personal photo in [com.anchor.data.AnchorRoutinePreferences] —
 * picking one clears the other, so the visualization stage always has a
 * single, unambiguous backdrop to show.
 */
enum class SafePlacePreset(val label: String, val colors: List<Color>) {
    OCEAN("Ocean", listOf(Color(0xFF0B2545), Color(0xFF1B5299), Color(0xFF6FA8DC))),
    FOREST("Forest", listOf(Color(0xFF0D2818), Color(0xFF1F4D2E), Color(0xFF5B8A55))),
    SUNSET("Sunset", listOf(Color(0xFF2B1055), Color(0xFFB33A5B), Color(0xFFF2A65A))),
    NIGHT_SKY("Night sky", listOf(Color(0xFF03050C), Color(0xFF10182B), Color(0xFF2E3A66))),
    MEADOW("Meadow", listOf(Color(0xFF244A3D), Color(0xFF6B9E5D), Color(0xFFD9E8A4)));

    companion object {
        fun fromName(name: String?): SafePlacePreset? = entries.firstOrNull { it.name == name }
    }
}

/** Renders [preset] as a smooth vertical gradient filling its bounds. */
@Composable
fun SafePlacePresetBackdrop(preset: SafePlacePreset, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(preset.colors))
    )
}
