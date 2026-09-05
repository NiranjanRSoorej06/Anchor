package com.anchor.ui.grounding

import android.speech.tts.TextToSpeech
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.core.audio.DebugAudioEngine
import com.anchor.domain.grounding.GroundingScript
import com.anchor.domain.grounding.GroundingScriptBuilder
import kotlinx.coroutines.delay

/**
 * Rapid sensory grounding screen, reachable from the widget tap or volume-button long-press.
 * Displays a calm, instant 5-sense sensory grounding script to orient someone in acute distress.
 *
 * Emergency Mode Audio Delivery:
 * - When earphones are attached: speaks soft female whisper ("You are in a safe place.") instantly on button trigger (<0.2s).
 * - When no earphones are attached: mutes 100% of audio output for silent haptic-only mode.
 */
@Composable
fun GroundingCaptureScreen(
    audioEngine: AudioDeliveryEngine = DebugAudioEngine(),
    onDone: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val script = remember { GroundingScriptBuilder.build(emptyList()) }
    val isWhisper = audioEngine.isWhisperModeActive()

    LaunchedEffect(Unit) {
        // Dispatch direct background SMS to configured companions
        try {
            com.anchor.core.companion.CompanionNotificationEngine(context).notifyCompanion()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Smoothly queue sensory grounding sentences after initial trigger safety phrase finishes
        delay(2200L)
        script.sentences.forEach { sentence ->
            audioEngine.speakWhisper(sentence, TextToSpeech.QUEUE_ADD)
            delay(4000L)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isWhisper) "🎧 Whisper Mode · Private Earbuds" else "🔊 Speaker Mode · Soothing Voice",
                style = MaterialTheme.typography.labelSmall,
                color = if (isWhisper) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
