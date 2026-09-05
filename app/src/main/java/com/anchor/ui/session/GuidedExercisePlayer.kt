package com.anchor.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.domain.content.ScriptStep
import kotlinx.coroutines.delay

/**
 * Shared scripted-exercise player: cycles through [steps], speaking each
 * one via [audioEngine] and displaying it, then calls [onComplete].
 *
 * Extracted from the old per-condition `when(condition.id)` block that
 * used to live inline in `SessionScreen`, generalized to any
 * [ScriptStep] list — reused by both the Anchor SOS flow's
 * router-recommended exercise and Manage Symptoms (Phase 2), so this
 * cadence mechanism exists exactly once.
 */
@Composable
fun GuidedExercisePlayer(
    title: String,
    steps: List<ScriptStep>,
    audioEngine: AudioDeliveryEngine,
    onComplete: () -> Unit,
    showSteadyButton: Boolean = false,
    onSteady: (() -> Unit)? = null
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(steps) {
        steps.forEachIndexed { index, step ->
            currentIndex = index
            audioEngine.speakWhisper(step.text)
            delay(step.durationSec * 1000L)
        }
        finished = true
    }

    val step = steps.getOrNull(currentIndex) ?: steps.first()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Step ${currentIndex + 1} of ${steps.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = step.text,
                    style = MaterialTheme.typography.titleLarge.copy(lineHeight = 30.sp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (finished) {
            Button(modifier = Modifier.fillMaxWidth(), onClick = onComplete) {
                Text("Continue")
            }
        } else {
            Text(
                text = "Focus on the exercise while it plays through…",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showSteadyButton) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { (onSteady ?: onComplete).invoke() }
                ) {
                    Text("I'm steady")
                }
            }
        }
    }
}
