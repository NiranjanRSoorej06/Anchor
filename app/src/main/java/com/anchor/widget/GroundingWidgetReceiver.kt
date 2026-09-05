package com.anchor.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Boilerplate glue Glance requires to register [GroundingWidget] as a real AppWidgetProvider. */
class GroundingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GroundingWidget()
}
