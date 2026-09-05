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
        const val TRIPLE_TAP_WINDOW_MS = 1200L
        const val WAKE_LOCK_TIMEOUT_MS = 3000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var tapCount = 0
    private var lastTapTimeMs = 0L
    private var wakeLock: PowerManager.WakeLock? = null
    private var screenOnWakeLock: PowerManager.WakeLock? = null
    private var partialWakeLock: PowerManager.WakeLock? = null
    private var mediaSession: android.media.session.MediaSession? = null

    private var silentAudioTrack: android.media.AudioTrack? = null
    @Volatile
    private var isSilentLoopRunning = false
    private var silentLoopThread: Thread? = null



    override fun onServiceConnected() {
        super.onServiceConnected()
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        @Suppress("DEPRECATION")
        wakeLock = powerManager?.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "Anchor:TriggerWakeLock"
        )
        partialWakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Anchor:PartialWakeLock"
        )?.apply {
            setReferenceCounted(false)
            try {
                acquire()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to acquire partial wake lock: ${e.message}")
            }
        }

        try {
            mediaSession = android.media.session.MediaSession(this, "AnchorMediaSession").apply {
                @Suppress("DEPRECATION")
                setFlags(
                    android.media.session.MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
                    android.media.session.MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
                )
                val state = android.media.session.PlaybackState.Builder()
                    .setActions(
                        android.media.session.PlaybackState.ACTION_PLAY or
                        android.media.session.PlaybackState.ACTION_PLAY_PAUSE
                    )
                    .setState(android.media.session.PlaybackState.STATE_PLAYING, 0, 1.0f)
                    .build()
                setPlaybackState(state)
                isActive = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error initializing MediaSession for screen-off key intercept: ${e.message}")
        }

        // Start silent audio loop so Android OS & Samsung One UI route volume keys while screen is OFF
        startSilentAudioLoop()

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
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                val currentTime = android.os.SystemClock.uptimeMillis()
                acquireWakeLock()

                if (currentTime - lastTapTimeMs < TRIPLE_TAP_WINDOW_MS) {
                    tapCount++
                } else {
                    tapCount = 1
                }
                lastTapTimeMs = currentTime

                Log.d(TAG, "Volume key tap count: $tapCount")

                if (tapCount >= 3) {
                    tapCount = 0
                    Log.i(TAG, "Rapid triple-tap volume trigger detected!")
                    triggerGrounding()
                    return true
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
        stopSilentAudioLoop()
        try {
            partialWakeLock?.let { if (it.isHeld) it.release() }
            mediaSession?.isActive = false
            mediaSession?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning up screen-off locks: ${e.message}")
        }
        super.onDestroy()
    }

    private fun startSilentAudioLoop() {
        if (isSilentLoopRunning) return
        isSilentLoopRunning = true
        silentLoopThread = Thread {
            try {
                val sampleRate = 44100
                val bufferSize = android.media.AudioTrack.getMinBufferSize(
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT
                )
                silentAudioTrack = android.media.AudioTrack.Builder()
                    .setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        android.media.AudioFormat.Builder()
                            .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(android.media.AudioTrack.MODE_STREAM)
                    .build()

                val silentBuffer = ByteArray(bufferSize)
                silentAudioTrack?.play()
                while (isSilentLoopRunning) {
                    silentAudioTrack?.write(silentBuffer, 0, silentBuffer.size)
                    Thread.sleep(200)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Silent audio loop info: ${e.message}")
            }
        }.apply {
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    private fun stopSilentAudioLoop() {
        isSilentLoopRunning = false
        try {
            silentAudioTrack?.stop()
            silentAudioTrack?.release()
            silentAudioTrack = null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping silent audio track: ${e.message}")
        }
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
        screenLock?.acquire(5000L)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            putExtra(MainActivity.EXTRA_LAUNCH_GROUNDING, true)
        }

        val options = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            android.app.ActivityOptions.makeBasic().apply {
                pendingIntentBackgroundActivityStartMode = android.app.ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
            }
        } else null

        try {
            val pendingIntent = android.app.PendingIntent.getActivity(
                this,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            if (options != null) {
                pendingIntent.send(this, 0, null, null, null, null, options.toBundle())
            } else {
                pendingIntent.send()
            }
            Log.i(TAG, "Successfully sent PendingIntent to launch Grounding screen")
        } catch (e: Exception) {
            Log.w(TAG, "PendingIntent launch failed, falling back to startActivity: ${e.message}")
            try {
                if (options != null) {
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Fallback startActivity also failed: ${fallbackEx.message}", fallbackEx)
            }
        }
    }
}
