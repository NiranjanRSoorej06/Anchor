package com.anchor.core.vision

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Real, on-device [ObjectLabeler] backed by ML Kit's **bundled** Image
 * Labeling model (statically linked into the APK — see the `image-labeling`
 * dependency in app/build.gradle.kts, chosen over the Play-Services/
 * unbundled variant specifically so this works immediately offline with no
 * first-run model download). Runs entirely on-device, no network call.
 */
class MlKitObjectLabeler : ObjectLabeler {
    private val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.55f)
        .build()
    private val labeler = ImageLabeling.getClient(options)

    override suspend fun label(bitmap: Bitmap): List<String> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return suspendCancellableCoroutine { continuation ->
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    continuation.resume(labels.map { it.text })
                }
                .addOnFailureListener {
                    // Labeling failure degrades to "no labels" — the caller
                    // (GroundingScriptBuilder) already treats that as a
                    // reason to show the fixed fallback script, never a
                    // reason to crash the capture flow.
                    continuation.resume(emptyList())
                }
        }
    }
}
