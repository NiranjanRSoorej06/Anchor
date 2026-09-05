package com.anchor.ui.support

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.anchor.core.companion.CompanionPreferences
import com.anchor.domain.support.MapsQueries
import com.anchor.domain.support.PinRegions
import com.anchor.domain.support.SupportCategory
import com.anchor.domain.support.SupportDirectory
import com.anchor.domain.support.SupportEntry
import com.anchor.ui.theme.AnchorQuoteStyle
import com.anchor.ui.theme.DmMonoFamily

/**
 * GET SUPPORT — production screen (replaces devtools/FindSupportScreen).
 *
 * Sections, top-to-bottom:
 * 1. "You are not alone" quote
 * 2. Pincode field → regional re-sort of helplines
 * 3. Crisis helplines — tap to call (ACTION_DIAL)
 * 4. Clinics near you — geo: map intents
 * 5. Personal contacts — opt in/out inline (read-only to companion prefs)
 * 6. You are not alone — NIMHANS RAAH link
 * 7. Ongoing / Peer support links
 *
 * Material 3 · civilian-first · discreet wording.
 * Data is bundled offline; nothing is fetched, tracked, or uploaded.
 */
@Composable
fun FindSupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val companionPrefs = remember { CompanionPreferences(context) }

    var pincode by remember { mutableStateOf("") }
    var pincodeError by remember { mutableStateOf(false) }
    var pincodeRegion by remember { mutableStateOf<String?>(null) }
    var companionEnabled by remember { mutableStateOf(companionPrefs.isEnabled) }

    // Helpline list: re-sorted when a valid pincode is entered
    val helplines: List<SupportEntry> = remember(pincodeRegion) {
        val region = pincodeRegion
        SupportDirectory.forRegion(region).filter { it.category != SupportCategory.THERAPIST_DIRECTORY }
    }

    fun openUri(uri: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
    }

    fun dial(number: String) {
        // Strip non-dial characters (+, -, spaces) for ACTION_DIAL
        val cleaned = number.replace(Regex("[+\\-\\s]"), "")
        context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$cleaned".toUri()))
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
                Text(
                    text = "Get Support",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── "You are not alone" — anchor quote ──────────────────────
            Text(
                text = "You are not alone.",
                style = AnchorQuoteStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            )

            Text(
                text = "Free, confidential help is one tap away.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // ── Pincode field ───────────────────────────────────────────
            SectionLabel("Find help in your area")

            OutlinedTextField(
                value = pincode,
                onValueChange = { input ->
                    // Accept only digits, max 6
                    val filtered = input.filter { it.isDigit() }.take(6)
                    pincode = filtered

                    when {
                        filtered.length == 6 -> {
                            val region = PinRegions.stateForPincode(filtered)
                            if (region != null) {
                                pincodeRegion = region
                                pincodeError = false
                            } else {
                                pincodeRegion = null
                                pincodeError = true
                            }
                        }
                        else -> {
                            pincodeRegion = null
                            pincodeError = false
                        }
                    }
                },
                label = {
                    Text(if (pincodeRegion != null) "$pincodeRegion" else "Area PIN code")
                },
                placeholder = {
                    Text("e.g. 560001")
                },
                supportingText = {
                    when {
                        pincodeRegion != null -> Text("Showing help available in $pincodeRegion")
                        pincodeError -> Text("PIN not recognised — showing all of India")
                        pincode.length in 1..5 -> Text("Enter 6 digits for local results")
                        else -> Text("Optional — helps show nearby helplines first")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = pincodeError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Crisis helplines ────────────────────────────────────────
            SectionLabel("Crisis helplines \u00B7 tap to call")

            helplines.forEach { entry ->
                HelplineButton(
                    entry = entry,
                    onClick = { entry.phoneNumber?.let { dial(it) } }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            // ── Clinics near you ────────────────────────────────────────
            SectionLabel("Clinics near you \u00B7 opens maps")

            MapButton(label = "Mental health clinics") {
                openUri(MapsQueries.uriFor(MapsQueries.CLINIC))
            }
            MapButton(label = "Psychiatrists") {
                openUri(MapsQueries.uriFor(MapsQueries.PSYCHIATRIST))
            }
            MapButton(label = "Counselling services") {
                openUri(MapsQueries.uriFor(MapsQueries.COUNSELLING))
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            // ── Personal contacts — inline opt in/out ───────────────────
            SectionLabel("People around you")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notify trusted contacts",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (companionEnabled) {
                                "On \u00B7 A quiet message goes out if you need it"
                            } else {
                                "Off \u00B7 No one will be notified"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = companionEnabled,
                        onCheckedChange = { checked ->
                            companionEnabled = checked
                            companionPrefs.isEnabled = checked
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── "You are not alone" — NIMHANS RAAH link ────────────────
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            SectionLabel("You are not alone")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Recovery is a journey, not a destination.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = { openUri("https://raah.nimhans.ac.in") },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "NIMHANS RAAH \u2014 Recovery Aspirations and Hope",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )

            // ── Ongoing support (online) ────────────────────────────────
            SectionLabel("Ongoing support \u00B7 online")

            SupportDirectory.ALL
                .filter { it.category == SupportCategory.THERAPIST_DIRECTORY }
                .forEach { entry ->
                    LinkButton(label = entry.name) {
                        entry.url?.let { openUri(it) }
                    }
                }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Peer communities (online) ───────────────────────────────
            SectionLabel("Peer communities \u00B7 online")

            Text(
                "Peer-run \u2014 not professional advice. Browse when you feel steady.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinkButton("r/ptsd community") { openUri("https://reddit.com/r/ptsd") }
            LinkButton("r/CPTSD community") { openUri("https://reddit.com/r/CPTSD") }
            LinkButton("7 Cups listeners") { openUri("https://www.7cups.com") }
            LinkButton("TheMindClan (India)") { openUri("https://themindclan.com") }
            LinkButton("Sangath (India)") { openUri("https://www.sangath.in") }

            // ── Footer ──────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Not a replacement for professional care \u00B7 works offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Reusable composables
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun HelplineButton(entry: SupportEntry, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Row {
                    entry.phoneNumber?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.hours.isNotBlank()) {
                            Text(
                                text = " \u00B7 ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (entry.hours.isNotBlank()) {
                        Text(
                            text = entry.hours,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (entry.languages.isNotEmpty()) {
                    Text(
                        text = entry.languages.joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapButton(label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Directions,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LinkButton(label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(14.dp)
        )
    }
}

/** Uppercase-style mono meta label matching the design system's section headers. */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = DmMonoFamily,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}
