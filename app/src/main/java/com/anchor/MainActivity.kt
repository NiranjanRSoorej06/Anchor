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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.anchor.core.audio.createAudioEngine
import com.anchor.core.haptics.createHapticEngine
import com.anchor.domain.session.SessionState
import com.anchor.domain.session.SessionStateMachine
import com.anchor.ui.HomeScreen
import com.anchor.ui.grounding.GroundingCaptureScreen
import com.anchor.ui.routine.EditAnchorScreen
import com.anchor.ui.session.SessionScreen
import com.anchor.ui.settings.SettingsScreen
import com.anchor.ui.support.CommunitiesScreen
import com.anchor.ui.support.CrisisResourcesScreen
import com.anchor.ui.support.FindProfessionalCareScreen
import com.anchor.ui.support.SupportHubScreen
import com.anchor.ui.support.TreatmentLocatorScreen
import com.anchor.ui.symptoms.ManageSymptomsScreen
import com.anchor.ui.theme.AnchorTheme
import com.anchor.ui.theme.ThemeVariant
import com.anchor.ui.tools.GoalsScreen
import com.anchor.ui.tools.JournalScreen
import com.anchor.ui.tools.MedTrackerScreen
import com.anchor.ui.tools.ToolsHubScreen
import com.anchor.ui.tools.TrackProgressScreen
import com.anchor.ui.tools.TriggerLogScreen

private enum class Screen {
    HOME, SESSION, GROUNDING,
    MANAGE_SYMPTOMS, TOOLS_HOME, EDIT_ANCHOR,
    TOOLS_TRIGGER_LOG, TOOLS_MEDS, TOOLS_JOURNAL, TOOLS_GOALS, TOOLS_PROGRESS,
    GET_SUPPORT, SUPPORT_CRISIS, SUPPORT_PROFESSIONAL, SUPPORT_LOCATOR, SUPPORT_COMMUNITIES,
    COMPANION, SETTINGS, SAFETY_PHRASES
}

class MainActivity : ComponentActivity() {

    companion object {
        /** Fired by the volume-button trigger and the home-screen widget — both launch the real Anchor SOS flow. */
        const val EXTRA_LAUNCH_ANCHOR = "com.anchor.EXTRA_LAUNCH_ANCHOR"
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
            val hapticEngine = remember { com.anchor.core.haptics.createHapticEngine(context) }
            val audioEngine = remember { createAudioEngine(context) }

            // The volume-button trigger and the widget both jump straight to
            // Screen.SESSION via the launch intent, bypassing HomeScreen's
            // own `machine.start()` call — start the machine here instead
            // whenever we land on SESSION from IDLE. Safe to call from the
            // Home button's path too: start() on an already-ACTIVATING
            // machine is a harmless no-op rejection.
            LaunchedEffect(screen) {
                if (screen == Screen.SESSION && machine.currentState == SessionState.IDLE) {
                    machine.start()
                }
            }

            AnchorTheme(variant = themeVariant) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (screen) {
                            Screen.HOME -> HomeScreen(
                                machine = machine,
                                hapticEngine = hapticEngine,
                                audioEngine = audioEngine,
                                onEnterSession = { launchScreen.value = Screen.SESSION },
                                onEditAnchor = { launchScreen.value = Screen.EDIT_ANCHOR },
                                onManageSymptoms = { launchScreen.value = Screen.MANAGE_SYMPTOMS },
                                onTools = { launchScreen.value = Screen.TOOLS_HOME },
                                onFindSupport = { launchScreen.value = Screen.GET_SUPPORT },
                                onSettings = { launchScreen.value = Screen.SETTINGS }
                            )
                            Screen.SESSION -> SessionScreen(
                                machine = machine,
                                hapticEngine = hapticEngine,
                                audioEngine = audioEngine,
                                onExitToHome = { launchScreen.value = Screen.HOME },
                                onGoToGetSupport = { launchScreen.value = Screen.GET_SUPPORT },
                                onGoToTrackProgress = { launchScreen.value = Screen.TOOLS_HOME }
                            )
                            Screen.GROUNDING -> GroundingCaptureScreen(
                                audioEngine = audioEngine,
                                onDone = { launchScreen.value = Screen.HOME }
                            )
                            Screen.MANAGE_SYMPTOMS -> ManageSymptomsScreen(
                                audioEngine = audioEngine,
                                onBack = { launchScreen.value = Screen.HOME },
                                onNeedProfessionalHelp = { launchScreen.value = Screen.GET_SUPPORT }
                            )
                            Screen.TOOLS_HOME -> ToolsHubScreen(
                                onBack = { launchScreen.value = Screen.HOME },
                                onTriggerLog = { launchScreen.value = Screen.TOOLS_TRIGGER_LOG },
                                onMedTracker = { launchScreen.value = Screen.TOOLS_MEDS },
                                onJournal = { launchScreen.value = Screen.TOOLS_JOURNAL },
                                onGoals = { launchScreen.value = Screen.TOOLS_GOALS },
                                onTrackProgress = { launchScreen.value = Screen.TOOLS_PROGRESS }
                            )
                            Screen.TOOLS_TRIGGER_LOG -> TriggerLogScreen(onBack = { launchScreen.value = Screen.TOOLS_HOME })
                            Screen.TOOLS_MEDS -> MedTrackerScreen(onBack = { launchScreen.value = Screen.TOOLS_HOME })
                            Screen.TOOLS_JOURNAL -> JournalScreen(onBack = { launchScreen.value = Screen.TOOLS_HOME })
                            Screen.TOOLS_GOALS -> GoalsScreen(onBack = { launchScreen.value = Screen.TOOLS_HOME })
                            Screen.TOOLS_PROGRESS -> TrackProgressScreen(onBack = { launchScreen.value = Screen.TOOLS_HOME })
                            Screen.EDIT_ANCHOR -> EditAnchorScreen(
                                onBack = { launchScreen.value = Screen.HOME }
                            )
                            Screen.GET_SUPPORT -> SupportHubScreen(
                                onBack = { launchScreen.value = Screen.HOME },
                                onCrisisResources = { launchScreen.value = Screen.SUPPORT_CRISIS },
                                onFindProfessionalCare = { launchScreen.value = Screen.SUPPORT_PROFESSIONAL },
                                onTreatmentLocator = { launchScreen.value = Screen.SUPPORT_LOCATOR },
                                onPersonalContacts = { launchScreen.value = Screen.COMPANION },
                                onCommunities = { launchScreen.value = Screen.SUPPORT_COMMUNITIES }
                            )
                            Screen.SUPPORT_CRISIS -> CrisisResourcesScreen(onBack = { launchScreen.value = Screen.GET_SUPPORT })
                            Screen.SUPPORT_PROFESSIONAL -> FindProfessionalCareScreen(onBack = { launchScreen.value = Screen.GET_SUPPORT })
                            Screen.SUPPORT_LOCATOR -> TreatmentLocatorScreen(onBack = { launchScreen.value = Screen.GET_SUPPORT })
                            Screen.SUPPORT_COMMUNITIES -> CommunitiesScreen(onBack = { launchScreen.value = Screen.GET_SUPPORT })
                            Screen.COMPANION -> com.anchor.ui.companion.CompanionModeScreen(
                                onBack = { launchScreen.value = Screen.HOME }
                            )
                            Screen.SETTINGS -> SettingsScreen(
                                themeVariant = themeVariant,
                                onThemeSelect = { themeVariant = it },
                                onCompanionMode = { launchScreen.value = Screen.COMPANION },
                                onSafetyPhrases = { launchScreen.value = Screen.SAFETY_PHRASES },
                                onBack = { launchScreen.value = Screen.HOME }
                            )
                            Screen.SAFETY_PHRASES -> com.anchor.ui.settings.SafetyPhrasesScreen(
                                onBack = { launchScreen.value = Screen.SETTINGS }
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
        if (intent?.getBooleanExtra(EXTRA_LAUNCH_ANCHOR, false) == true) {
            launchScreen.value = Screen.SESSION
            intent.removeExtra(EXTRA_LAUNCH_ANCHOR)
        }
    }
}
