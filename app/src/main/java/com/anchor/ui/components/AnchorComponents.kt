package com.anchor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anchor.ui.theme.AnchorColors
import com.anchor.ui.theme.AnchorTheme

/**
 * Shared building blocks for the module-build-prompts.md screens (Home's
 * tile grid, Manage Symptoms / Get Support's card lists and tabs, the
 * custom-tool wizard, the distress meter). Every color here comes from
 * [MaterialTheme.colorScheme] or [AnchorColors] — no new literals — so
 * these automatically follow whichever of Nord/Sage/Sand + light/dark the
 * user has picked in [com.anchor.devtools.ThemeSwitcher], per
 * `docs/module-build-prompts.md` M0.1's reuse decision.
 *
 * Recreated once already (2026-09-05) after a concurrent HomeScreen
 * rewrite deleted this untracked file as unreferenced dead code — it's
 * back because `ui/tools/ToolsHubScreen.kt` and `ui/tools/TriggerLogScreen.kt`
 * now depend on [SectionCard] and [ThermometerSlider] respectively. If you
 * are about to delete this file again: check for references first
 * (`grep -rn "ui.components" app/src/main/java`), don't assume unused.
 */

/** A tappable row card: leading icon in a rounded tint box + label. Used
 * for Manage Symptoms' symptom list, Get Support's resource list, etc. */
@Composable
fun SectionCard(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AnchorColors.current.tint),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** A square icon+label tile for Home's 3-section grid (Manage Symptoms /
 * Tools / Get Support). */
@Composable
fun GridTile(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

/** An underline-style tab row, e.g. "Symptoms | Exercise Library |
 * Favorites" inside Manage Symptoms. */
@Composable
fun AnchorTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        tabs.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier.clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(if (selected) 28.dp else 0.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

/** One entry in [AnchorBottomBar]. */
data class BottomBarItem(val id: String, val label: String, val icon: ImageVector)

/** Persistent 4-item bottom navigation bar (Home / Manage / Tools /
 * Support) — appears on every screen except Home itself and the SOS
 * session, per module-build-prompts.md's locked information architecture. */
@Composable
fun AnchorBottomBar(
    items: List<BottomBarItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large
    ) {
        Row(modifier = Modifier.padding(vertical = 10.dp)) {
            items.forEach { item ->
                BottomBarEntry(
                    item = item,
                    selected = item.id == selectedId,
                    onClick = { onSelect(item.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomBarEntry(
    item: BottomBarItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(item.icon, contentDescription = item.label, tint = color)
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/** "Step X of Y" header with an optional back arrow and a close (X)
 * button — the custom-tool wizard's top bar. */
@Composable
fun StepperTopBar(
    step: Int,
    totalSteps: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        } else {
            Box(modifier = Modifier.size(48.dp))
        }
        Text(
            text = "Step $step of $totalSteps",
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Close")
        }
    }
}

/**
 * Up/down steppers + numeric readout + a gradient thermometer bar — the
 * Distress Meter's input control. [range] bounds the value (inclusive);
 * taps beyond the bound are no-ops rather than clamped, so a fast tapper
 * never sees the counter silently stop moving without feedback being
 * obviously "at the edge" (button visually disabled).
 */
@Composable
fun ThermometerSlider(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { if (value < range.last) onValueChange(value + 1) }, enabled = value < range.last) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase")
            }
            Text(text = value.toString(), style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = { if (value > range.first) onValueChange(value - 1) }, enabled = value > range.first) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease")
            }
        }

        ThermometerBar(value = value, range = range)
    }
}

@Composable
private fun ThermometerBar(value: Int, range: IntRange) {
    val steps = (range.last - range.first + 1).coerceAtLeast(1)
    val filledSteps = (value - range.first + 1).coerceIn(0, steps)
    // Cool-to-warm gradient bottom-to-top: calm (secondary) rising to
    // distress (error) — matches the reference thermometer's blue-to-orange
    // read, built from existing scheme colors instead of new literals.
    val calm = MaterialTheme.colorScheme.secondary
    val distress = MaterialTheme.colorScheme.error
    val stepColors = (0 until steps).map { indexFromBottom ->
        lerpColor(calm, distress, indexFromBottom.toFloat() / (steps - 1).coerceAtLeast(1))
    }

    Box(
        modifier = Modifier
            .width(28.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom
        ) {
            for (indexFromTop in steps - 1 downTo 0) {
                val filled = indexFromTop < filledSteps
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp / steps)
                        .background(if (filled) stepColors[indexFromTop] else androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
}

private fun lerpColor(
    start: androidx.compose.ui.graphics.Color,
    end: androidx.compose.ui.graphics.Color,
    fraction: Float
): androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(
    red = start.red + (end.red - start.red) * fraction,
    green = start.green + (end.green - start.green) * fraction,
    blue = start.blue + (end.blue - start.blue) * fraction,
    alpha = 1f
)

@Preview(showBackground = true)
@Composable
private fun AnchorComponentsPreview() {
    AnchorTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(label = "Crisis Resources", icon = Icons.Filled.Close, onClick = {})
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridTile(label = "Manage Symptoms", icon = Icons.Filled.Close, modifier = Modifier.weight(1f), onClick = {})
                GridTile(label = "Tools", icon = Icons.Filled.Close, modifier = Modifier.weight(1f), onClick = {})
                GridTile(label = "Get Support", icon = Icons.Filled.Close, modifier = Modifier.weight(1f), onClick = {})
            }
            AnchorTabRow(tabs = listOf("Symptoms", "Exercise Library", "Favorites"), selectedIndex = 0, onSelect = {})
            StepperTopBar(step = 1, totalSteps = 2, onClose = {})
            ThermometerSlider(value = 6, range = 0..10, onValueChange = {})
        }
    }
}
