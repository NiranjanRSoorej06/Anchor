package com.anchor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.core.audio.DebugAudioEngine
import com.anchor.core.haptics.DebugHapticEngine
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPatterns
import com.anchor.domain.session.SessionStateMachine
import com.anchor.ui.theme.AnchorTheme

/**
 * The real Anchor entry point. Per docs/vision.md's Non-negotiable #2:
 * "ANCHOR NOW is hero entry. Big one-tap button."
 *
 * Supports Dual-Mode Audio & Haptic Delivery:
 * - When private earphones/earbuds are connected: whispers "You are in a safe place." in a soft female voice.
 * - When no earphones are connected: mutes 100% of audio output for silent haptic mode.
 */
@Composable
fun HomeScreen(
    machine: SessionStateMachine,
    hapticEngine: HapticEngine,
    audioEngine: AudioDeliveryEngine = DebugAudioEngine(),
    onEnterSession: () -> Unit
) {
    val isWhisper = audioEngine.isWhisperModeActive()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isWhisper) "🎧 Whisper Mode · Private Earbuds" else "🔊 Speaker Mode · Soothing Voice",
                style = MaterialTheme.typography.labelSmall,
                color = if (isWhisper) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnchorNowButton {
                    // Haptic first, then state, then navigation — in that order.
                    hapticEngine.play(HapticPatterns.DOUBLE_PULSE)
                    machine.start()
                    onEnterSession()
                }
            }

            Text(
                text = "Not a replacement for professional care · works offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun AnchorNowButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(236.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ANCHOR\nNOW",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AnchorTheme {
        HomeScreen(
            machine = SessionStateMachine(),
            hapticEngine = DebugHapticEngine(),
            audioEngine = DebugAudioEngine(),
            onEnterSession = {}
        )
    }
}
