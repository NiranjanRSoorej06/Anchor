package com.anchor.core.companion

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Companion notification engine: the ONLY place in the codebase that sends
 * an SMS via [SmsManager].
 *
 * ## SF5 lock (confirm-gate, NEVER silent background)
 *
 * The old `notifyCompanion()` path sent SMS silently in the background without
 * any user confirmation step. That path has been **deleted** — silent auto-send
 * of emergency/companion messages is banned (vision.md SF5).
 *
 * The sole public entry point is now [sendAfterUserConfirm], which:
 * 1. Receives the explicit contact phone numbers and message text from the
 *    caller — the UI layer is responsible for obtaining user confirmation
 *    before invoking this method.
 * 2. Sends via SmsManager **here and only here** — no other code path may
 *    import or call SmsManager.sendTextMessage.
 * 3. Returns true if at least one SMS was dispatched.
 *
 * The caller must have already checked/ensured SEND_SMS permission.
 */
class CompanionNotificationEngine(private val context: Context) {

    private companion object {
        const val TAG = "CompanionEngine"
    }

    /**
     * Send [message] to each phone number in [contactPhones] via SMS.
     *
     * **This is the only SmsManager send path in the codebase.** All callers
     * must go through this method — no background send, no silent trigger.
     *
     * @param contactPhones non-blank phone numbers to message.
     * @param message       the SMS body text.
     * @return `true` if at least one SMS was successfully dispatched.
     */
    fun sendAfterUserConfirm(contactPhones: List<String>, message: String): Boolean {
        if (contactPhones.isEmpty()) {
            Log.w(TAG, "sendAfterUserConfirm: no contact phones provided")
            return false
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "SEND_SMS permission not granted; cannot send SMS")
            return false
        }

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        var successCount = 0

        for (phone in contactPhones) {
            if (phone.isBlank()) continue
            try {
                smsManager.sendTextMessage(
                    phone.trim(),
                    null,
                    message,
                    null,
                    null
                )
                successCount++
                Log.i(TAG, "SMS sent to $phone (confirm-gate)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send SMS to $phone: ${e.message}", e)
            }
        }

        return successCount > 0
    }
}
