package com.anchor.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anchor.core.haptics.DebugHapticEngine
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPatterns
import com.anchor.domain.session.SessionStateMachine
import com.anchor.ui.theme.AnchorTheme

/**
 * The real Anchor entry point. Per docs/vision.md's Non-negotiable #2:
 * "ANCHOR NOW is hero entry. Big one-tap button."
 *
 * The tap order matters and is deliberate: haptic fires first, session
 * state changes second, navigation third. Someone in acute distress feels
 * something *before* anything else happens — the same principle
 * SessionStateTestScreen already demonstrates, just for a real entry point
 * instead of a dev harness.
 *
 * No onboarding, no settings, no history here — none of that exists yet.
 */
@Composable
fun HomeScreen(
    machine: SessionStateMachine,
    hapticEngine: HapticEngine,
    onEnterSession: () -> Unit
) {
    val context = LocalContext.current
    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Result ignored either way: the grounding feature already has a
        // no-permission fallback script (see GroundingCaptureScreen), so
        // there's nothing more to do here whether the user grants or denies.
    }

    // Requested once, proactively, from Home — a widget/volume-button tap
    // has no UI of its own to show a permission dialog from.
    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!alreadyGranted) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            onEnterSession = {}
        )
    }
}
