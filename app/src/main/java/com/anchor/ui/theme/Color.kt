package com.anchor.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The seven roles every palette below fills, matching Material 3's core
 * color-scheme slots. Colors were authored in OKLCH (see the design canvas
 * this shipped alongside) and converted to sRGB hex here — Compose's
 * [Color] constructor takes sRGB, it has no OKLCH constructor.
 */
internal data class PaletteColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val onPrimary: Color,
    val ink: Color,
    val inkMuted: Color,
    val border: Color
)

// ---- NORD / Arctic — the default ----

internal val NordLight = PaletteColors(
    background = Color(0xFFF1F6FA),
    surface = Color(0xFFE5ECF3),
    primary = Color(0xFF577594),
    onPrimary = Color(0xFFF6F9FC),
    ink = Color(0xFF1A222B),
    inkMuted = Color(0xFF5B646F),
    border = Color(0xFFD3D8DE)
)

internal val NordDark = PaletteColors(
    background = Color(0xFF080C10),
    surface = Color(0xFF11171C),
    primary = Color(0xFF6D9DCE),
    onPrimary = Color(0xFF080C0F),
    ink = Color(0xFFE2E5E8),
    inkMuted = Color(0xFF9299A1),
    border = Color(0xFF292E35)
)

// ---- Soft Sage ----

internal val SageLight = PaletteColors(
    background = Color(0xFFEFF8F2),
    surface = Color(0xFFE2EFE6),
    primary = Color(0xFF528162),
    onPrimary = Color(0xFFF6F9F7),
    ink = Color(0xFF1C2720),
    inkMuted = Color(0xFF5B675E),
    border = Color(0xFFD2DAD4)
)

internal val SageDark = PaletteColors(
    background = Color(0xFF060D08),
    surface = Color(0xFF0F1912),
    primary = Color(0xFF5CA477),
    onPrimary = Color(0xFF080C09),
    ink = Color(0xFFE1E6E2),
    inkMuted = Color(0xFF909C93),
    border = Color(0xFF27312A)
)

// ---- Warm Sand ----

internal val SandLight = PaletteColors(
    background = Color(0xFFFBF4EA),
    surface = Color(0xFFF3EADD),
    primary = Color(0xFFA36A56),
    onPrimary = Color(0xFFFCF7F6),
    ink = Color(0xFF30271F),
    inkMuted = Color(0xFF72665E),
    border = Color(0xFFE1D9D0)
)

internal val SandDark = PaletteColors(
    background = Color(0xFF100A04),
    surface = Color(0xFF1D160C),
    primary = Color(0xFFD28063),
    onPrimary = Color(0xFF110C0A),
    ink = Color(0xFFECE7E3),
    inkMuted = Color(0xFFA89C92),
    border = Color(0xFF372F25)
)

internal fun paletteFor(variant: ThemeVariant, dark: Boolean): PaletteColors = when (variant) {
    ThemeVariant.NORD -> if (dark) NordDark else NordLight
    ThemeVariant.SAGE -> if (dark) SageDark else SageLight
    ThemeVariant.SAND -> if (dark) SandDark else SandLight
}
