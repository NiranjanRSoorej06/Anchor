package com.anchor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.anchor.devtools.DevHapticTestScreen
import com.anchor.devtools.SessionStateTestScreen
import com.anchor.ui.HomeScreen
import com.anchor.ui.theme.AnchorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnchorTheme {
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
