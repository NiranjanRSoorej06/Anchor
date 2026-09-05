package com.anchor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.anchor.core.audio.createAudioEngine
import com.anchor.core.haptics.createHapticEngine
import com.anchor.devtools.ThemeSwitcher
import com.anchor.domain.session.SessionStateMachine
import com.anchor.ui.HomeScreen
import com.anchor.ui.anchor.EditAnchorScreen
import com.anchor.ui.grounding.GroundingCaptureScreen
import com.anchor.ui.session.SessionScreen
import com.anchor.ui.support.GetSupportScreen
import com.anchor.ui.symptoms.ManageSymptomsScreen
import com.anchor.ui.theme.AnchorTheme
import com.anchor.ui.theme.ThemeVariant
import com.anchor.ui.tools.ToolsSuiteScreen

private enum class Screen { HOME, SESSION, GROUNDING, MANAGE_SYMPTOMS, TOOLS, SUPPORT, EDIT_ANCHOR, COMPANION }

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_LAUNCH_GROUNDING = "com.anchor.EXTRA_LAUNCH_GROUNDING"
    }

    private val launchScreen = mutableStateOf(Screen.HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureLockscreenDisplay()
        applyLaunchIntent(intent)
        // Pre-warm AudioEngineProvider for zero-latency instant TTS output
        com.anchor.core.audio.AudioEngineProvider.get(this)
        setContent {
            var themeVariant by remember { mutableStateOf(ThemeVariant.NORD) }
            val screen by launchScreen

            // System back from any non-HOME destination returns HOME;
            // HOME itself keeps the default behavior (exits the app).
            BackHandler(enabled = screen != Screen.HOME) {
                launchScreen.value = Screen.HOME
            }

            val context = LocalContext.current
            val machine = remember { SessionStateMachine() }
            val hapticEngine = remember { createHapticEngine(context) }
            val audioEngine = remember { createAudioEngine(context) }

            AnchorTheme(variant = themeVariant) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                ) {
                    ThemeSwitcher(selected = themeVariant, onSelect = { themeVariant = it })

                    Box(modifier = Modifier.weight(1f)) {
                        when (screen) {
                            Screen.HOME -> HomeScreen(
                                machine = machine,
                                hapticEngine = hapticEngine,
                                audioEngine = audioEngine,
                                onEnterSession = { launchScreen.value = Screen.SESSION },
                                onManageSymptoms = { launchScreen.value = Screen.MANAGE_SYMPTOMS },
                                onTools = { launchScreen.value = Screen.TOOLS },
                                onFindSupport = { launchScreen.value = Screen.SUPPORT },
                                onEditAnchor = { launchScreen.value = Screen.EDIT_ANCHOR },
                                onCompanionMode = { launchScreen.value = Screen.COMPANION }
                            )
                            Screen.SESSION -> SessionScreen(
                                machine = machine,
                                hapticEngine = hapticEngine,
                                audioEngine = audioEngine,
                                onExitToHome = { launchScreen.value = Screen.HOME }
                            )
                            Screen.GROUNDING -> GroundingCaptureScreen(
                                audioEngine = audioEngine,
                                onDone = { launchScreen.value = Screen.HOME }
                            )
                            Screen.MANAGE_SYMPTOMS -> ManageSymptomsScreen(
                                onBack = { launchScreen.value = Screen.HOME },
                                onNeedHelp = { launchScreen.value = Screen.SUPPORT }
                            )
                            Screen.TOOLS -> ToolsSuiteScreen(
                                onBack = { launchScreen.value = Screen.HOME }
                            )
                            Screen.SUPPORT -> GetSupportScreen(
                                onBack = { launchScreen.value = Screen.HOME }
                            )
                            Screen.EDIT_ANCHOR -> EditAnchorScreen(
                                onBack = { launchScreen.value = Screen.HOME },
                                onSave = { tool, audio ->
                                    // Settings saved
                                }
                            )
                            Screen.COMPANION -> com.anchor.ui.companion.CompanionModeScreen(
                                onBack = { launchScreen.value = Screen.HOME }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureLockscreenDisplay()
        applyLaunchIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        configureLockscreenDisplay()
    }

    private fun configureLockscreenDisplay() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    private fun applyLaunchIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_LAUNCH_GROUNDING, false) == true) {
            launchScreen.value = Screen.GROUNDING
            intent.removeExtra(EXTRA_LAUNCH_GROUNDING)
        }
    }
}
