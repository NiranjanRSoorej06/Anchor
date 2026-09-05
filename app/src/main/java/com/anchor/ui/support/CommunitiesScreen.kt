package com.anchor.ui.support

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
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
import com.anchor.ui.components.SectionCard

/**
 * "You Are Not Alone" — peer communities, last after professional/crisis
 * resources per `docs/vision.md`'s ordering, each with a caveat line
 * (module-build-prompts.md M5.1, replacing the "Resources for Veterans"
 * card from the PTSD Coach reference — not this app's audience).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitiesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    fun openUri(uri: String) = context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("You Are Not Alone", fontWeight = FontWeight.Bold) },
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
                "Peer-run — not professional advice. Browse when stable.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SectionCard("r/ptsd community", Icons.Filled.Favorite) { openUri("https://reddit.com/r/ptsd") }
            SectionCard("r/CPTSD community", Icons.Filled.Favorite) { openUri("https://reddit.com/r/CPTSD") }
            SectionCard("7 Cups listeners", Icons.Filled.Favorite) { openUri("https://www.7cups.com") }
            SectionCard("TheMindClan (India)", Icons.Filled.Favorite) { openUri("https://themindclan.com") }
            SectionCard("Sangath (India)", Icons.Filled.Favorite) { openUri("https://www.sangath.in") }
        }
    }
}
