package com.anchor.ui.grounding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anchor.domain.grounding.GroundingScript
import com.anchor.domain.grounding.GroundingScriptBuilder

/**
 * Rapid sensory grounding screen, reachable from the widget tap or volume-button long-press.
 * Displays a calm, instant 5-sense sensory grounding script to orient someone in acute distress.
 */
@Composable
fun GroundingCaptureScreen(onDone: () -> Unit) {
    val script = remember { GroundingScriptBuilder.build(emptyList()) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScriptStage(script = script, onDone = onDone)
        }
    }
}

@Composable
private fun ScriptStage(script: GroundingScript, onDone: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Notice what's around you.",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        script.sentences.forEach { sentence ->
            Text(
                sentence,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            onClick = onDone
        ) {
            Text("Done")
        }
    }
}
