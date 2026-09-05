package com.anchor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii mirrored from the design canvas (`Anchor.dc.html`): chips
 * ~18dp, cards ~22dp, buttons/pills ~26dp. Wiring these into
 * [androidx.compose.material3.MaterialTheme]'s `shapes` lets stock
 * components (Button, Card, FilterChip, OutlinedButton) pick up the
 * rounding automatically, without every call site needing an explicit
 * `.clip(RoundedCornerShape(...))`.
 */
internal val AnchorShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
