package com.anchor.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.anchor.MainActivity

/**
 * Home-screen widget trigger for the grounding feature (see plan.md). A
 * single tap target: opens [MainActivity] straight into
 * [com.anchor.ui.grounding.GroundingCaptureScreen] via the same launch
 * extra the volume-button trigger in [com.anchor.trigger.AnchorAccessibilityService]
 * uses, so both triggers converge on the identical flow.
 */
class GroundingWidget : GlanceAppWidget() {

    private companion object {
        val LAUNCH_GROUNDING_KEY = ActionParameters.Key<Boolean>(MainActivity.EXTRA_LAUNCH_GROUNDING)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF5E81AC))
                    .clickable(
                        actionStartActivity(
                            intent = Intent(context, MainActivity::class.java),
                            parameters = actionParametersOf(LAUNCH_GROUNDING_KEY to true)
                        )
                    )
            ) {
                Text(
                    text = "Ground Me",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}
