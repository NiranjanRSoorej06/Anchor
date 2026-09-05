package com.anchor.ui.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anchor.ui.components.SectionCard

/**
 * "My Support" hub (module-build-prompts.md M5.1) — restyles the old
 * devtools `FindSupportScreen` flat list into the reference's card-list
 * hub, and folds Companion Mode's contact management in here as "My
 * Personal Support Contacts" instead of a separate top-level button.
 *
 * Per the doc's explicit calls: no "Resources for Veterans" card (not
 * this app's audience — replaced by "You Are Not Alone"), no "Family and
 * Caregiver Resources" or "Mobile Apps" placeholder cards (out of scope,
 * not built just to fill a slot).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportHubScreen(
    onBack: () -> Unit,
    onCrisisResources: () -> Unit,
    onFindProfessionalCare: () -> Unit,
    onTreatmentLocator: () -> Unit,
    onPersonalContacts: () -> Unit,
    onCommunities: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Support", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "You are not alone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionCard(
                label = "Crisis Resources",
                subtitle = "Tele MANAS, Emergency 112, and other helplines — offline, tap to call",
                icon = Icons.Filled.Call,
                onClick = onCrisisResources
            )
            SectionCard(
                label = "Find Professional Care",
                subtitle = "Clinics, psychiatrists, and a therapist directory",
                icon = Icons.Filled.Person,
                onClick = onFindProfessionalCare
            )
            SectionCard(
                label = "Treatment Locator",
                subtitle = "Enter your PIN code to see resources near you",
                icon = Icons.Filled.LocationOn,
                onClick = onTreatmentLocator
            )
            SectionCard(
                label = "My Personal Support Contacts",
                subtitle = "Manage who gets notified, and when",
                icon = Icons.Filled.AccountCircle,
                onClick = onPersonalContacts
            )
            SectionCard(
                label = "You Are Not Alone",
                subtitle = "Peer communities — browse when stable",
                icon = Icons.Filled.Favorite,
                onClick = onCommunities
            )
        }
    }
}
