package com.anchor.core.companion

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.anchor.core.location.LastKnownLocationProvider

class CompanionNotificationEngine(private val context: Context) {

    private companion object {
        const val TAG = "CompanionEngine"
        const val ACTION_SMS_SENT = "com.anchor.ACTION_SMS_SENT"
    }

    /**
     * @return `true` if a send was attempted (pre-checks passed), `false`
     *   if skipped (disabled, no contacts, or missing permission). Because
     *   the location lookup is asynchronous, this does NOT mean delivery
     *   succeeded — check logcat tag [TAG] for the real per-contact result
     *   (`SmsManager`'s actual dispatch outcome, not just "queued").
     */
    fun notifyCompanion(customMessage: String? = null): Boolean {
        val prefs = CompanionPreferences(context)
        if (!prefs.isEnabled) {
            Log.d(TAG, "Companion Mode is disabled; skipping notification")
            return false
        }

        val contacts = prefs.getContacts()
        if (contacts.isEmpty()) {
            Log.w(TAG, "Companion Mode enabled but no contacts configured")
            return false
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "SEND_SMS permission not granted; cannot send direct background SMS")
            return false
        }

        val baseMessage = customMessage ?: prefs.getMessageText()

        fun sendAll(message: String) {
            val smsManager = resolveSmsManager()

            for (contact in contacts) {
                if (contact.phoneNumber.isBlank()) continue
                try {
                    smsManager.sendTextMessage(
                        CompanionMessageComposer.normalizePhoneNumber(contact.phoneNumber),
                        null,
                        message,
                        sentIntent(contact.name, contact.phoneNumber),
                        null
                    )
                    Log.i(TAG, "SMS dispatch requested for ${contact.name} (${contact.phoneNumber}) — see follow-up log for actual radio result")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to queue background SMS to ${contact.phoneNumber}: ${e.message}", e)
                }
            }
        }

        if (prefs.shareLocationOnAlert) {
            LastKnownLocationProvider.getAsync(context) { location ->
                if (location == null) {
                    Log.w(TAG, "No cached location available (permission granted, but nothing cached yet on this device) — sending alert without a location link")
                }
                sendAll(CompanionMessageComposer.withLocation(baseMessage, location))
            }
        } else {
            sendAll(baseMessage)
        }

        return true
    }

    /**
     * Resolves the [SmsManager] tied to the device's actual default SMS
     * subscription, instead of the ambiguous generic "default" instance.
     *
     * On a dual-SIM phone, `SmsManager.getDefault()`/
     * `context.getSystemService(SmsManager::class.java)` can silently
     * dispatch through the wrong slot, or an ambiguous one, and come back
     * `RESULT_ERROR_GENERIC_FAILURE` even though a subscription with real
     * service exists — reproduced on-device (see logcat tag [TAG] for the
     * failure before this fix). Explicitly resolving the subscription id
     * first is the standard fix for that exact symptom.
     */
    private fun resolveSmsManager(): SmsManager {
        try {
            val subId = SubscriptionManager.getDefaultSmsSubscriptionId()
            if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                @Suppress("DEPRECATION")
                return SmsManager.getSmsManagerForSubscriptionId(subId)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not resolve default SMS subscription, falling back: ${e.message}")
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }

    /**
     * A [PendingIntent] that reports the SMS radio's *actual* result —
     * `sendTextMessage` returning without throwing only means the OS
     * accepted the request, never that a carrier delivered it. Registers
     * a short-lived receiver per call rather than one static receiver,
     * since `sendTextMessage` already gives each send its own callback.
     */
    private fun sentIntent(contactName: String, phoneNumber: String): PendingIntent {
        val action = "$ACTION_SMS_SENT.${System.nanoTime()}"
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val outcome = when (resultCode) {
                    android.app.Activity.RESULT_OK -> "SENT to radio successfully"
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "FAILED — generic failure"
                    SmsManager.RESULT_ERROR_NO_SERVICE -> "FAILED — no cell service (check SIM/default SMS subscription)"
                    SmsManager.RESULT_ERROR_NULL_PDU -> "FAILED — null PDU"
                    SmsManager.RESULT_ERROR_RADIO_OFF -> "FAILED — radio off (airplane mode?)"
                    else -> "FAILED — code $resultCode"
                }
                Log.i(TAG, "SMS to $contactName ($phoneNumber): $outcome")
                try {
                    context.unregisterReceiver(this)
                } catch (e: Exception) {
                    // Already unregistered — harmless.
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(action), Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, IntentFilter(action))
        }
        return PendingIntent.getBroadcast(
            context, phoneNumber.hashCode(), Intent(action).setPackage(context.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
