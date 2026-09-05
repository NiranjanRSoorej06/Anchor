package com.anchor.devtools

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.anchor.domain.support.MapsQueries
import com.anchor.domain.support.SupportCategory
import com.anchor.domain.support.SupportDirectory
import com.anchor.ui.theme.DmMonoFamily

/**
 * FIND SUPPORT (real product screen, not a dev harness) — the R6/R7/R8
 * slice of the demo plan: crisis helplines, map shortcuts, therapist
 * directory, and peer communities in one place.
 *
 * - Phone buttons use [Intent.ACTION_DIAL] (opens the dialer with the
 *   number filled in). This needs NO permission — the user presses call.
 * - Map buttons fire `geo:0,0?q=` intents ([MapsQueries]); the map app
 *   handles its own location — we request none. Needs internet for
 *   search, badged as online-only.
 * - Community links open in the browser, each with a caveat line.
 *   Peer-run spaces are listed last, after crisis help.
 * - Data comes from [SupportDirectory] (bundled, offline). Nothing here
 *   is fetched, tracked, or uploaded.
 */
@Composable
fun FindSupportScreen() {
    val context = LocalContext.current

    fun openUri(uri: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
    }

    fun dial(number: String) {
        context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$number".toUri()))
    }

    val helplines = SupportDirectory.ALL.filter { it.category != SupportCategory.THERAPIST_DIRECTORY }
    val directories = SupportDirectory.ALL.filter { it.category == SupportCategory.THERAPIST_DIRECTORY }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Find Support", style = MaterialTheme.typography.headlineSmall)
            Text(
                "You are not alone.",
                style = MaterialTheme.typography.bodyMedium
            )

            SectionLabel("Crisis helplines — tap to call")
            helplines.forEach { entry ->
                SupportButton(
                    label = "${entry.name}\n${entry.phoneNumber ?: ""} · ${entry.hours}",
                    onClick = { entry.phoneNumber?.let { dial(it) } }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            SectionLabel("Clinics near you (online)")
            SupportButton("Mental health clinics near me") {
                openUri(MapsQueries.uriFor(MapsQueries.CLINIC))
            }
            SupportButton("Psychiatrists near me") {
                openUri(MapsQueries.uriFor(MapsQueries.PSYCHIATRIST))
            }
            SupportButton("Counselling services near me") {
                openUri(MapsQueries.uriFor(MapsQueries.COUNSELLING))
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            SectionLabel("Ongoing support (online)")
            directories.forEach { entry ->
                SupportButton(entry.name) { entry.url?.let { openUri(it) } }
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            SectionLabel("Peer communities (online)")
            Text(
                "Peer-run — not professional advice. Browse when stable.",
                style = MaterialTheme.typography.bodySmall
            )
            SupportButton("r/ptsd community") { openUri("https://reddit.com/r/ptsd") }
            SupportButton("r/CPTSD community") { openUri("https://reddit.com/r/CPTSD") }
            SupportButton("7 Cups listeners") { openUri("https://www.7cups.com") }
            SupportButton("TheMindClan (India)") { openUri("https://themindclan.com") }
            SupportButton("Sangath (India)") { openUri("https://www.sangath.in") }
        }
    }
}

@Composable
private fun SupportButton(label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/** Uppercase-style mono meta label, matching the design canvas's section headers. */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = DmMonoFamily,
            fontSize = 12.sp,
            letterSpacing = 1.2.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
