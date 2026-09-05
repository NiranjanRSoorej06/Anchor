package com.anchor.ui.support

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.anchor.domain.support.MapsQueries
import com.anchor.domain.support.SupportCategory
import com.anchor.domain.support.SupportDirectory
import com.anchor.ui.components.SectionCard

/** Maps shortcuts + therapist directory (module-build-prompts.md M5.1) —
 * online-badged, no location permission needed (`geo:0,0?q=`). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindProfessionalCareScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    fun openUri(uri: String) = context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
    val directories = SupportDirectory.ALL.filter { it.category == SupportCategory.THERAPIST_DIRECTORY }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Professional Care", fontWeight = FontWeight.Bold) },
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
                "Needs internet — these open in Maps or your browser.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SectionCard("Mental health clinics near me", Icons.Filled.LocationOn) {
                openUri(MapsQueries.uriFor(MapsQueries.CLINIC))
            }
            SectionCard("Psychiatrists near me", Icons.Filled.LocationOn) {
                openUri(MapsQueries.uriFor(MapsQueries.PSYCHIATRIST))
            }
            SectionCard("Counselling services near me", Icons.Filled.LocationOn) {
                openUri(MapsQueries.uriFor(MapsQueries.COUNSELLING))
            }
            directories.forEach { entry ->
                SectionCard(entry.name, Icons.Filled.Info) {
                    entry.url?.let { openUri(it) }
                }
            }
        }
    }
}
