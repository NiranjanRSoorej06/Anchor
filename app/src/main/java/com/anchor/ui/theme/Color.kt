package com.anchor.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The sixteen roles the design canvas (`Anchor.dc.html`) defines, matching
 * its CSS custom-property names 1:1 (canvas/bg/surf/surf2/line, ink/ink2/
 * ink3, acc/acc2/onAcc/tint, ok/warn/amber/lilac). Colors are the canvas's
 * own sRGB hex literals — Compose's [Color] constructor takes sRGB.
 */
internal data class PaletteColors(
    val canvas: Color,
    val bg: Color,
    val surf: Color,
    val surf2: Color,
    val line: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val acc: Color,
    val acc2: Color,
    val onAcc: Color,
    val tint: Color,
    val ok: Color,
    val warn: Color,
    val amber: Color,
    val lilac: Color
)

/**
 * The design canvas only varies [acc]/[acc2]/[tint] between its three
 * palettes (`data-palette="nord|sage|sand"`) — every other role is a
 * function of light/dark alone. Splitting the model this way (instead of
 * six fully-duplicated [PaletteColors] literals) mirrors that structure
 * and keeps Sage/Sand from drifting out of sync with Nord's neutrals.
 */
private data class Neutrals(
    val canvas: Color,
    val bg: Color,
    val surf: Color,
    val surf2: Color,
    val line: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val onAcc: Color,
    val ok: Color,
    val warn: Color,
    val amber: Color,
    val lilac: Color
)

private data class Accent(val acc: Color, val acc2: Color, val tint: Color)

// ---- Neutrals — canonical Nord Polar Night / Snow Storm / Aurora hex ----

private val NeutralsLight = Neutrals(
    canvas = Color(0xFFE5E9F0),
    bg = Color(0xFFECEFF4),
    surf = Color(0xFFFFFFFF),
    surf2 = Color(0xFFE5E9F0),
    line = Color(0xFFD8DEE9),
    ink = Color(0xFF2E3440),
    ink2 = Color(0xFF4C566A),
    ink3 = Color(0xFF79839A),
    onAcc = Color(0xFFECEFF4),
    ok = Color(0xFF7D9B6A),
    warn = Color(0xFFBF616A),
    amber = Color(0xFFB9902F),
    lilac = Color(0xFF9C7898)
)

private val NeutralsDark = Neutrals(
    canvas = Color(0xFF1C2027),
    bg = Color(0xFF242933),
    surf = Color(0xFF2E3440),
    surf2 = Color(0xFF3B4252),
    line = Color(0xFF434C5E),
    ink = Color(0xFFECEFF4),
    ink2 = Color(0xFFD8DEE9),
    ink3 = Color(0xFF8F9AB0),
    onAcc = Color(0xFF232831),
    ok = Color(0xFFA3BE8C),
    warn = Color(0xFFD08770),
    amber = Color(0xFFEBCB8B),
    lilac = Color(0xFFB48EAD)
)

// ---- Accents — one per palette per mode ----

private val NordAccentLight = Accent(acc = Color(0xFF5E81AC), acc2 = Color(0xFF81A1C1), tint = Color(0xFFE3EAF3))
private val NordAccentDark = Accent(acc = Color(0xFF88C0D0), acc2 = Color(0xFF81A1C1), tint = Color(0xFF333B48))

private val SageAccentLight = Accent(acc = Color(0xFF6F8F74), acc2 = Color(0xFF8AA88E), tint = Color(0xFFE4EBE4))
private val SageAccentDark = Accent(acc = Color(0xFFA9C4A2), acc2 = Color(0xFF8AA88E), tint = Color(0xFF343D38))

private val SandAccentLight = Accent(acc = Color(0xFFA3784F), acc2 = Color(0xFFC09A6B), tint = Color(0xFFF0E7DC))
private val SandAccentDark = Accent(acc = Color(0xFFD8AE7E), acc2 = Color(0xFFC09A6B), tint = Color(0xFF413830))

private fun compose(neutrals: Neutrals, accent: Accent) = PaletteColors(
    canvas = neutrals.canvas,
    bg = neutrals.bg,
    surf = neutrals.surf,
    surf2 = neutrals.surf2,
    line = neutrals.line,
    ink = neutrals.ink,
    ink2 = neutrals.ink2,
    ink3 = neutrals.ink3,
    acc = accent.acc,
    acc2 = accent.acc2,
    onAcc = neutrals.onAcc,
    tint = accent.tint,
    ok = neutrals.ok,
    warn = neutrals.warn,
    amber = neutrals.amber,
    lilac = neutrals.lilac
)

internal fun paletteFor(variant: ThemeVariant, dark: Boolean): PaletteColors {
    val neutrals = if (dark) NeutralsDark else NeutralsLight
    val accent = when (variant) {
        ThemeVariant.NORD -> if (dark) NordAccentDark else NordAccentLight
        ThemeVariant.SAGE -> if (dark) SageAccentDark else SageAccentLight
        ThemeVariant.SAND -> if (dark) SandAccentDark else SandAccentLight
    }
    return compose(neutrals, accent)
}
