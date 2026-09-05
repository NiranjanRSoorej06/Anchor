package com.anchor.trigger

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import com.anchor.MainActivity

/**
 * Volume-button long-press trigger for the grounding feature (see plan.md).
 *
 * Ported directly from the mechanism verified in `jzsalinas/nugon-android`'s
 * `NugonAccessibilityService.java` (long-press timer + partial wake lock),
 * adapted to Kotlin and to Anchor's target (open the grounding screen,
 * not send an SMS).
 *
 * Hackathon-demo scope only, deliberately: this works while the screen is
 * on, or locked-but-awake — the same limitation the Nugon reference itself
 * has (its own deep-sleep bypass ships commented out). Screen-off/deep-sleep
 * reliability is out of scope; see plan.md's Technical Risks section.
 */
class AnchorAccessibilityService : AccessibilityService() {

    private companion object {
        const val TAG = "AnchorA11yService"
        const val LONG_PRESS_TIMEOUT_MS = 380L
        const val WAKE_LOCK_TIMEOUT_MS = 3000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isButtonPressed = false
    private var isLongPressHandled = false
    private var pressedKeyCode = -1
    private var wakeLock: PowerManager.WakeLock? = null

    private val longPressRunnable = Runnable {
        if (isButtonPressed && !isLongPressHandled) {
            isLongPressHandled = true
            Log.i(TAG, "Grounding long-press detected")
            triggerGrounding()
            releaseWakeLock()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        @Suppress("DEPRECATION")
        wakeLock = powerManager?.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "Anchor:TriggerWakeLock"
        )
        // Pre-warm TTS engine so speech playback is instantaneous when triggered
        try {
            com.anchor.core.audio.createAudioEngine(this)
        } catch (e: Exception) {
            Log.w(TAG, "Error pre-warming AudioEngine: ${e.message}")
        }
        Log.i(TAG, "Anchor accessibility service connected")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (event.repeatCount == 0) {
                        acquireWakeLock()
                        isButtonPressed = true
                        isLongPressHandled = false
                        pressedKeyCode = keyCode
                        handler.removeCallbacks(longPressRunnable)
                        handler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT_MS)
                    }
                }
                KeyEvent.ACTION_UP -> {
                    if (keyCode == pressedKeyCode) {
                        handler.removeCallbacks(longPressRunnable)
                        releaseWakeLock()
                        isButtonPressed = false
                        val handled = isLongPressHandled
                        isLongPressHandled = false
                        if (handled) {
                            return true
                        }
                    }
                }
            }
            return false
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // No window content is needed — only onKeyEvent() matters here.
    }

    override fun onInterrupt() {
        releaseWakeLock()
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    private fun acquireWakeLock() {
        wakeLock?.let { lock ->
            if (!lock.isHeld) lock.acquire(WAKE_LOCK_TIMEOUT_MS)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { lock ->
            if (lock.isHeld) lock.release()
        }
    }

    private fun triggerGrounding() {
        try {
            val audioEngine = com.anchor.core.audio.createAudioEngine(this)
            if (audioEngine.isWhisperModeActive()) {
                audioEngine.speakWhisper("You are in a safe place.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error playing instant trigger whisper: ${e.message}")
        }

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        @Suppress("DEPRECATION")
        val screenLock = powerManager?.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "Anchor:ScreenOnLock"
        )
        screenLock?.acquire(3000L)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            putExtra(MainActivity.EXTRA_LAUNCH_GROUNDING, true)
        }

        try {
            val pendingIntent = android.app.PendingIntent.getActivity(
                this,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent.send()
        } catch (e: Exception) {
            Log.w(TAG, "PendingIntent launch failed, falling back to startActivity: ${e.message}")
            startActivity(intent)
        }
    }
}
