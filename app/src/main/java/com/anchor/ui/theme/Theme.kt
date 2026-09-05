package com.anchor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * [lightColorScheme]/[darkColorScheme] only override the roles you pass —
 * every other role (secondaryContainer, tertiary, ...) silently falls back
 * to Material 3's generic default palette, not this one. Anything a real
 * component might pick up (FilterChip's selected state uses
 * secondaryContainer, for example) needs an explicit value here, or a
 * stock Material purple leaks through and the palette work is pointless.
 * `error`/`onError`/`errorContainer`/`onErrorContainer` are deliberately
 * omitted, so they keep Material 3's own built-in defaults — a warning
 * should stay recognizably red regardless of which palette is active.
 */
private fun PaletteColors.toColorScheme(dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = primary, onPrimary = onPrimary,
        primaryContainer = primary, onPrimaryContainer = onPrimary,
        secondary = primary, onSecondary = onPrimary,
        secondaryContainer = primary, onSecondaryContainer = onPrimary,
        tertiary = primary, onTertiary = onPrimary,
        tertiaryContainer = primary, onTertiaryContainer = onPrimary,
        background = background, onBackground = ink,
        surface = surface, onSurface = ink,
        surfaceVariant = surface, onSurfaceVariant = inkMuted,
        outline = border
    )
} else {
    lightColorScheme(
        primary = primary, onPrimary = onPrimary,
        primaryContainer = primary, onPrimaryContainer = onPrimary,
        secondary = primary, onSecondary = onPrimary,
        secondaryContainer = primary, onSecondaryContainer = onPrimary,
        tertiary = primary, onTertiary = onPrimary,
        tertiaryContainer = primary, onTertiaryContainer = onPrimary,
        background = background, onBackground = ink,
        surface = surface, onSurface = ink,
        surfaceVariant = surface, onSurfaceVariant = inkMuted,
        outline = border
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
    val colorScheme = paletteFor(variant, darkTheme).toColorScheme(darkTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AnchorTypography,
        content = content
    )
}
