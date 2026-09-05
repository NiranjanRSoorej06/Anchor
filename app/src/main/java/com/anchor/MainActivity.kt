package com.anchor

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
import com.anchor.devtools.DevHapticTestScreen
import com.anchor.devtools.SessionStateTestScreen
import com.anchor.devtools.ThemeSwitcher
import com.anchor.ui.HomeScreen
import com.anchor.ui.theme.AnchorTheme
import com.anchor.ui.theme.ThemeVariant

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themeVariant by remember { mutableStateOf(ThemeVariant.NORD) }

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
                        // TEMPORARY (Module 4 manual verification) — swap back to
                        // DevHapticTestScreen()/HomeScreen() and delete the devtools
                        // package once the real Anchor session screen lands.
                        SessionStateTestScreen()
                        // DevHapticTestScreen()
                        // HomeScreen()
                    }
                }
            }
        }
    }
}
