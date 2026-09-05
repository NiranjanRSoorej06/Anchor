package com.anchor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The design canvas's tokens that don't map onto any stock Material 3
 * [androidx.compose.material3.ColorScheme] slot: `canvas` (screen ground
 * behind cards), `tint` (light accent fill), the status colors (`ok`/
 * `amber`/`lilac`) used by Better/Same/Worse and similar cues, and the
 * `ink2`/`ink3` text-hierarchy tiers (`surfaceVariant`/`onSurfaceVariant`
 * only cover one muted tier). Access via [AnchorColors.current], mirroring
 * `MaterialTheme.colorScheme`.
 */
data class AnchorExtraColors(
    val canvas: Color,
    val tint: Color,
    val ok: Color,
    val amber: Color,
    val lilac: Color,
    val ink2: Color,
    val ink3: Color
)

private val LocalAnchorExtraColors = staticCompositionLocalOf {
    AnchorExtraColors(
        canvas = Color.Unspecified,
        tint = Color.Unspecified,
        ok = Color.Unspecified,
        amber = Color.Unspecified,
        lilac = Color.Unspecified,
        ink2 = Color.Unspecified,
        ink3 = Color.Unspecified
    )
}

object AnchorColors {
    val current: AnchorExtraColors
        @Composable get() = LocalAnchorExtraColors.current
}

/**
 * [lightColorScheme]/[darkColorScheme] only override the roles you pass —
 * every other role (secondaryContainer, tertiary, ...) silently falls back
 * to Material 3's generic default palette, not this one. Anything a real
 * component might pick up (FilterChip's selected state uses
 * secondaryContainer, for example) needs an explicit value here, or a
 * stock Material purple leaks through and the palette work is pointless.
 * `error`/`onError`/`errorContainer`/`onErrorContainer` deliberately map to
 * `warn` — Worse/danger states share Material's error slot on purpose, per
 * the design canvas's `CheckInStage`/safety-stop treatment.
 */
private fun PaletteColors.toColorScheme(dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = acc, onPrimary = onAcc,
        primaryContainer = acc, onPrimaryContainer = onAcc,
        secondary = acc2, onSecondary = onAcc,
        secondaryContainer = tint, onSecondaryContainer = ink,
        tertiary = lilac, onTertiary = onAcc,
        tertiaryContainer = tint, onTertiaryContainer = ink,
        background = bg, onBackground = ink,
        surface = surf, onSurface = ink,
        surfaceVariant = surf2, onSurfaceVariant = ink2,
        outline = line,
        error = warn, onError = onAcc, errorContainer = warn, onErrorContainer = onAcc
    )
} else {
    lightColorScheme(
        primary = acc, onPrimary = onAcc,
        primaryContainer = acc, onPrimaryContainer = onAcc,
        secondary = acc2, onSecondary = onAcc,
        secondaryContainer = tint, onSecondaryContainer = ink,
        tertiary = lilac, onTertiary = onAcc,
        tertiaryContainer = tint, onTertiaryContainer = ink,
        background = bg, onBackground = ink,
        surface = surf, onSurface = ink,
        surfaceVariant = surf2, onSurfaceVariant = ink2,
        outline = line,
        error = warn, onError = onAcc, errorContainer = warn, onErrorContainer = onAcc
    )
}

/**
 * @param variant Which palette family to use (Nord / Soft Sage / Warm Sand).
 *   Defaults to [ThemeVariant.NORD] per team decision — see [ThemeVariant].
 * @param darkTheme Whether to use the variant's dark or light colors.
 *   Independent of [variant]: every palette has both.
 */
@Composable
fun AnchorTheme(
    variant: ThemeVariant = ThemeVariant.NORD,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = paletteFor(variant, darkTheme)
    val extraColors = AnchorExtraColors(
        canvas = palette.canvas,
        tint = palette.tint,
        ok = palette.ok,
        amber = palette.amber,
        lilac = palette.lilac,
        ink2 = palette.ink2,
        ink3 = palette.ink3
    )
    CompositionLocalProvider(LocalAnchorExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = palette.toColorScheme(darkTheme),
            typography = AnchorTypography,
            shapes = AnchorShapes,
            content = content
        )
    }
}
