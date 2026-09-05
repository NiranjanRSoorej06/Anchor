package com.anchor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.anchor.core.haptics.createHapticEngine
import com.anchor.devtools.DevHapticTestScreen
import com.anchor.devtools.RoutingLabScreen
import com.anchor.devtools.SessionStateTestScreen
import com.anchor.devtools.ThemeSwitcher
import com.anchor.domain.session.SessionStateMachine
import com.anchor.ui.HomeScreen
import com.anchor.ui.grounding.GroundingCaptureScreen
import com.anchor.ui.session.SessionScreen
import com.anchor.ui.theme.AnchorTheme
import com.anchor.ui.theme.ThemeVariant

private enum class Screen { HOME, SESSION, GROUNDING }

class MainActivity : ComponentActivity() {

    companion object {
        /**
         * Set by the widget tap and the volume-button long-press trigger
         * (see widget/GroundingWidget.kt, trigger/AnchorAccessibilityService.kt)
         * to open straight into [GroundingCaptureScreen] instead of Home.
         */
        const val EXTRA_LAUNCH_GROUNDING = "com.anchor.EXTRA_LAUNCH_GROUNDING"
    }

    private val launchScreen = mutableStateOf(Screen.HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyLaunchIntent(intent)
        setContent {
            var themeVariant by remember { mutableStateOf(ThemeVariant.NORD) }
            var screen by launchScreen

            val context = LocalContext.current
            val machine = remember { SessionStateMachine() }
            val hapticEngine = remember { createHapticEngine(context) }

            AnchorTheme(variant = themeVariant) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                ) {
                    // TEMPORARY — dev-only palette switcher; see ThemeSwitcher.kt.
                    ThemeSwitcher(selected = themeVariant, onSelect = { themeVariant = it })

                    Box(modifier = Modifier.weight(1f)) {
                        // The real app: ANCHOR NOW -> the actual session flow.
                        when (screen) {
                            Screen.HOME -> HomeScreen(
                                machine = machine,
                                hapticEngine = hapticEngine,
                                onEnterSession = { screen = Screen.SESSION }
                            )
                            Screen.SESSION -> SessionScreen(
                                machine = machine,
                                hapticEngine = hapticEngine,
                                onExitToHome = { screen = Screen.HOME }
                            )
                            Screen.GROUNDING -> GroundingCaptureScreen(
                                onDone = { screen = Screen.HOME }
                            )
                        }

                        // Dev tooling — still reachable by swapping the line above
                        // for manual verification. Not part of the real app flow.
                        // SessionStateTestScreen()
                        // RoutingLabScreen()
                        // DevHapticTestScreen()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        applyLaunchIntent(intent)
    }

    private fun applyLaunchIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_LAUNCH_GROUNDING, false) == true) {
            launchScreen.value = Screen.GROUNDING
        }
    }
}
