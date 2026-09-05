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
        const val LONG_PRESS_TIMEOUT_MS = 1500L
        const val WAKE_LOCK_TIMEOUT_MS = 3000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isButtonPressed = false
    private var pressedKeyCode = -1
    private var wakeLock: PowerManager.WakeLock? = null

    private val longPressRunnable = Runnable {
        if (isButtonPressed) {
            Log.i(TAG, "Grounding long-press detected")
            triggerGrounding()
            releaseWakeLock()
            isButtonPressed = false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Anchor:TriggerWakeLock")
        Log.i(TAG, "Anchor accessibility service connected")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (!isButtonPressed) {
                        acquireWakeLock()
                        isButtonPressed = true
                        pressedKeyCode = keyCode
                        handler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT_MS)
                    }
                }
                KeyEvent.ACTION_UP -> {
                    if (isButtonPressed && keyCode == pressedKeyCode) {
                        handler.removeCallbacks(longPressRunnable)
                        releaseWakeLock()
                        isButtonPressed = false
                    }
                }
            }
            // Never swallow the keypress — the system still handles the
            // ordinary volume change, exactly like the Nugon reference.
            return false
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // No window content is needed — only onKeyEvent() matters here.
    }

    override fun onInterrupt() {
        // No-op: nothing to clean up beyond the wake lock, already handled above.
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
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.EXTRA_LAUNCH_GROUNDING, true)
        }
        startActivity(intent)
    }
}
