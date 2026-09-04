package com.anchor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = AnchorPrimary,
    onPrimary = AnchorOnPrimary,
    secondary = AnchorSecondary,
    background = AnchorBackground,
    surface = AnchorSurface,
    onBackground = AnchorOnBackground
)

private val DarkColors = darkColorScheme(
    primary = AnchorPrimaryDark,
    onPrimary = AnchorOnPrimaryDark,
    secondary = AnchorSecondaryDark,
    background = AnchorBackgroundDark,
    surface = AnchorSurfaceDark,
    onBackground = AnchorOnBackgroundDark
)

@Composable
fun AnchorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AnchorTypography,
        content = content
    )
}
