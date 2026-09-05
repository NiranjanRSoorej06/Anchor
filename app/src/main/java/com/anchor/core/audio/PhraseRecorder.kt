package com.anchor.core.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

/**
 * Thin [MediaRecorder] wrapper for the Safety Phrases recording flow.
 * Records to app-private storage only — never external/shared storage —
 * matching this app's on-device-only privacy stance.
 *
 * Caller owns the ~15s max-duration guard (`SafetyPhraseRecording.MAX_DURATION_MS`)
 * by calling [stop] itself on a timer; this class doesn't cap duration on
 * its own, so it stays a plain record/stop primitive the UI drives.
 */
class PhraseRecorder(private val context: Context) {

    companion object {
        private const val TAG = "PhraseRecorder"

        /** Shared cap for any in-app recording (safety phrases, exercise
         * narration) — matches `docs/vision.md` non-negotiable #6's ~15s limit. */
        const val MAX_DURATION_MS: Long = 15_000L
    }

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    /** @return the file recording started into, or `null` if starting failed. */
    fun start(outputFile: File): File? {
        stop()
        return try {
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            r.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            recorder = r
            this.outputFile = outputFile
            outputFile
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start recording: ${e.message}")
            recorder?.release()
            recorder = null
            null
        }
    }

    /** @return `true` if a recording was in progress and stopped cleanly. */
    fun stop(): Boolean {
        val r = recorder ?: return false
        return try {
            r.stop()
            r.release()
            recorder = null
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop recording cleanly: ${e.message}")
            try {
                r.release()
            } catch (_: Exception) {
                // Already released.
            }
            recorder = null
            false
        }
    }

    /** Stops (if recording) and deletes the in-progress file — used when the user discards a take. */
    fun cancel() {
        stop()
        outputFile?.delete()
        outputFile = null
    }

    fun isRecording(): Boolean = recorder != null
}
