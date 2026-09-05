package com.anchor.core.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Schedules a single, local, inexact reminder notification — used for
 * "remind me to log this later" (Anchor flow) and medicine reminders
 * (Tools). Deliberately uses [AlarmManager.setAndAllowWhileIdle] rather
 * than an exact alarm, so no `SCHEDULE_EXACT_ALARM` permission is needed.
 *
 * This is a thin Android wrapper with no pure-Kotlin logic worth unit
 * testing in isolation — verified on-device, matching every other
 * Android-API-surface class in this codebase (`CompanionNotificationEngine`,
 * `AudioOutputDetector`).
 */
object ReminderScheduler {

    const val CHANNEL_ID = "anchor_reminders"
    private const val REQUEST_CODE_LOG_REMINDER = 9001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Anchor reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle reminders to log an event or take a scheduled medicine"
            }
            manager?.createNotificationChannel(channel)
        }
    }

    /** Schedules a one-time "log the event" reminder [delayMillis] from now. */
    fun scheduleLogReminder(context: Context, delayMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, LogReminderReceiver::class.java).apply {
            putExtra(LogReminderReceiver.EXTRA_TITLE, "Anchor: a moment to log")
            putExtra(
                LogReminderReceiver.EXTRA_TEXT,
                "When you're ready, log what happened — it helps you and Anchor understand your triggers over time."
            )
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_LOG_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + delayMillis
        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } catch (_: SecurityException) {
            // No exact-alarm permission on this OS version — silently skip;
            // this is a gentle nice-to-have reminder, never a safety path.
        }
    }
}

/** Fires the local notification scheduled by [ReminderScheduler.scheduleLogReminder]. */
class LogReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TEXT = "extra_text"
        private const val NOTIFICATION_ID = 9101
    }

    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.ensureChannel(context)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Anchor"
        val text = intent.getStringExtra(EXTRA_TEXT) ?: ""

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted — nothing safety-critical is lost.
        }
    }
}
