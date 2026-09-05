package com.anchor.ui.support

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.core.net.toUri
import com.anchor.domain.support.PinRegions
import com.anchor.domain.support.SupportDirectory

/**
 * PIN-code → state → regionally-reordered support list (module-build-
 * prompts.md M5.2). No network, no location permission: [PinRegions] is a
 * bundled ~1KB offline table, and re-ordering reuses the already-tested
 * [SupportDirectory.forRegion]. Never stored or sent — looked up and
 * discarded on screen exit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentLocatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var pincode by remember { mutableStateOf("") }
    val state = remember(pincode) { PinRegions.stateForPincode(pincode) }
    val entries = remember(state) { SupportDirectory.forRegion(state) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Treatment Locator", fontWeight = FontWeight.Bold) },
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
                "Enter your 6-digit PIN code to see resources near you first. Nothing is stored or sent anywhere — this stays on your phone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = pincode,
                onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pincode = it },
                label = { Text("PIN code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            if (pincode.length == 6) {
                Text(
                    state?.let { "Showing resources for $it" } ?: "PIN not recognized — showing nationwide resources",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            entries.forEach { entry ->
                Card(
                    onClick = {
                        entry.phoneNumber?.let { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$it".toUri())) }
                            ?: entry.url?.let { context.startActivity(Intent(Intent.ACTION_VIEW, it.toUri())) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(entry.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            entry.phoneNumber ?: "Online directory",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
