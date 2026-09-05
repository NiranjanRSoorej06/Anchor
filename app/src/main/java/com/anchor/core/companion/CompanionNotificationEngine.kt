package com.anchor.core.companion

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat

class CompanionNotificationEngine(private val context: Context) {

    private companion object {
        const val TAG = "CompanionEngine"
    }

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

        val message = customMessage ?: prefs.getMessageText()
        var successCount = 0

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        for (contact in contacts) {
            if (contact.phoneNumber.isBlank()) continue
            try {
                smsManager.sendTextMessage(
                    contact.phoneNumber.trim(),
                    null,
                    message,
                    null,
                    null
                )
                successCount++
                Log.i(TAG, "Direct background SMS notification sent to ${contact.name} (${contact.phoneNumber})")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send background SMS to ${contact.phoneNumber}: ${e.message}", e)
            }
        }

        return successCount > 0
    }
}
