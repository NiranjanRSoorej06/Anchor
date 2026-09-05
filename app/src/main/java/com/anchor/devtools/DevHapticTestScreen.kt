package com.anchor.devtools

import android.content.Context
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.anchor.core.haptics.DebugHapticEngine
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPattern
import com.anchor.core.haptics.HapticPatterns
import com.anchor.core.haptics.SystemHapticEngine

/**
 * HAPTIC LAB — a developer/testing playground for [HapticEngine] and the
 * patterns in [HapticPatterns]. It is NOT part of the final Anchor recovery
 * experience; it exists so a developer can answer, by hand:
 *
 *   1. Does the pattern execute?
 *   2. Does the pattern stop?
 *   3. Does intensity affect the pattern?
 *   4. Can patterns be switched safely (no overlap)?
 *   5. Does the real (Android) implementation work on a physical device?
 *   6. Does the debug implementation work on the emulator?
 *
 * IMPORTANT: this screen only ever talks to [HapticEngine]. It never
 * imports android.os.Vibrator / VibrationEffect itself — that stays inside
 * [SystemHapticEngine], which is the one place allowed to touch them.
 *
 * Delete this file and its call site in MainActivity once the real Anchor
 * session screen (a later module) replaces it. Nothing else in the app
 * depends on this file.
 */
@Composable
fun DevHapticTestScreen() {
    val context = LocalContext.current
    val engine = remember { buildEngineForThisDevice(context) }
    var intensity by remember { mutableFloatStateOf(1f) }
    var currentPattern by remember { mutableStateOf<HapticPattern?>(null) }

    // Only non-null for DebugHapticEngine; drives the on-screen stand-in pulse.
    // This is a debug-only visualization — it does not mean the device vibrated.
    val debugPulse = (engine as? DebugHapticEngine)?.activePulse?.collectAsState()?.value
    val pulseScale by animateFloatAsState(
        targetValue = if (debugPulse != null) 1.25f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "pulseScale"
    )

    // Switching patterns: stop whatever is currently playing before starting
    // the new one, so playback never overlaps and there is only ever one
    // "current" pattern — on the real Vibrator hardware and in our own
    // currentPattern state alike.
    fun playPattern(pattern: HapticPattern) {
        engine.stop()
        currentPattern = pattern
        engine.play(pattern, intensity)
    }

    fun stopPattern() {
        engine.stop()
        currentPattern = null
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Haptic Lab (dev)", style = MaterialTheme.typography.headlineSmall)

            SectionLabel("Engine")
            Text(engine::class.simpleName ?: "unknown", style = MaterialTheme.typography.bodyMedium)

            SectionLabel("Capabilities")
            val c = engine.capabilities
            Text(
                text = "hasVibrator = ${c.hasVibrator}\n" +
                    "amplitudeControl = ${c.hasAmplitudeControl}\n" +
                    "primitives = ${c.supportsPrimitives}\n" +
                    "API level = ${c.apiLevel}",
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            // This visualization is ONLY for development/debugging. It never
            // claims a physical vibration occurred — see DebugHapticEngine.
            Text("Pattern visualization", style = MaterialTheme.typography.labelMedium)
            Box(
                modifier = Modifier
                    .size(72.dp * pulseScale)
                    .background(
                        color = if (debugPulse != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            )
            Text(
                text = "Active haptic pattern: ${currentPattern?.id ?: "none"}",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            SectionLabel("Patterns")
            PatternButton("Test Pulse", HapticPatterns.TEST_PULSE, ::playPattern)
            PatternButton("Double Pulse", HapticPatterns.DOUBLE_PULSE, ::playPattern)
            PatternButton("Slow Pulse", HapticPatterns.SLOW_PULSE, ::playPattern)
            PatternButton("Breathing In", HapticPatterns.BREATHING_IN, ::playPattern)
            PatternButton("Breathing Out", HapticPatterns.BREATHING_OUT, ::playPattern)

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            SectionLabel("Intensity: ${"%.2f".format(intensity)}")
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                valueRange = 0f..1f
            )
            Text(
                "Applies to the next pattern you play, not one already running.",
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { stopPattern() }
            ) {
                Text("STOP")
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall)
}

@Composable
private fun PatternButton(label: String, pattern: HapticPattern, onPlay: (HapticPattern) -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onPlay(pattern) }
    ) {
        Text(label)
    }
}

/**
 * Heuristic emulator detection so this screen shows a felt vibration on a
 * real Samsung and a visible pulse on the AVD, without needing a manual
 * switch. This heuristic is dev-tooling only — production trigger code
 * (a later module) will not need it, since it already knows its own context.
 */
private fun buildEngineForThisDevice(context: Context): HapticEngine {
    val looksLikeEmulator = Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu") ||
        Build.PRODUCT.contains("sdk")

    return if (looksLikeEmulator) DebugHapticEngine() else SystemHapticEngine(context)
}
