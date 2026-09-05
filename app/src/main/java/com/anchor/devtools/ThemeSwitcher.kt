package com.anchor.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anchor.ui.theme.ThemeVariant

/**
 * TEMPORARY — a dev-only palette switcher, standing in for the real Settings
 * screen (not built yet). Lets anyone testing the app try all three reviewed
 * palette directions (Nord default, Soft Sage, Warm Sand) without rebuilding.
 *
 * The selection lives only in memory (held by whatever composes this) —
 * there is no persistence layer yet (see M0-M4-CODEBASE-REFERENCE.md §17),
 * so it resets to [ThemeVariant.NORD] on every app restart. That's an
 * accepted gap for now, not an oversight.
 *
 * Delete this file once a real Settings screen owns theme selection.
 */
@Composable
fun ThemeSwitcher(selected: ThemeVariant, onSelect: (ThemeVariant) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Theme:",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            for (variant in ThemeVariant.entries) {
                FilterChip(
                    selected = variant == selected,
                    onClick = { onSelect(variant) },
                    label = { Text(variant.label) }
                )
            }
        }
    }
}
