package com.anchor.ui.grounding

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.anchor.core.vision.createObjectLabeler
import com.anchor.domain.grounding.GroundingScript
import com.anchor.domain.grounding.GroundingScriptBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

private sealed interface CaptureState {
    data object Loading : CaptureState
    data class Result(val script: GroundingScript) : CaptureState
}

/**
 * Reachable only from the widget tap ([com.anchor.widget.GroundingWidget])
 * or the volume-button long-press ([com.anchor.trigger.AnchorAccessibilityService]).
 * Takes exactly one photo, labels it on-device, shows a calm "things you
 * can see" script, and returns to Home. Does not touch
 * [com.anchor.domain.session.SessionStateMachine] — this stands alone.
 *
 * The photo never leaves this function: it's decoded into memory, handed
 * to the labeler, and dropped — never written to disk, never added to the
 * MediaStore, never shown on screen. See plan.md for why.
 */
@Composable
fun GroundingCaptureScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val objectLabeler = remember { createObjectLabeler() }
    val previewView = remember { PreviewView(context) }

    var captureState by remember { mutableStateOf<CaptureState>(CaptureState.Loading) }

    LaunchedEffect(Unit) {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            // No camera permission -> the fixed fallback script, not a crash
            // or a permission prompt (a widget/trigger tap has no UI to show one).
            captureState = CaptureState.Result(GroundingScriptBuilder.build(emptyList()))
            return@LaunchedEffect
        }

        val labels = try {
            val provider = withContext(Dispatchers.IO) {
                ProcessCameraProvider.getInstance(context).get()
            }
            val preview = CameraPreview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val imageCapture = ImageCapture.Builder().build()

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )

            kotlinx.coroutines.delay(500L)

            val bitmap = captureOneFrame(imageCapture, context)
            provider.unbindAll()

            bitmap?.let { objectLabeler.label(it) } ?: emptyList()
        } catch (e: Exception) {
            // Any camera/labeling failure degrades to the fallback script —
            // never blocks or crashes this screen.
            emptyList()
        }

        captureState = CaptureState.Result(GroundingScriptBuilder.build(labels))
    }

    Scaffold { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Full size preview view so CameraX initializes HD stream resolution with hardware auto-focus
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // Calm overlay card
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.88f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (val state = captureState) {
                    is CaptureState.Loading -> LoadingStage()
                    is CaptureState.Result -> ScriptStage(script = state.script, onDone = onDone)
                }
            }
        }
    }
}

@Composable
private fun LoadingStage() {
    Text(
        "Looking around…",
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ScriptStage(script: GroundingScript, onDone: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Notice what's around you.",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        script.sentences.forEach { sentence ->
            Text(
                sentence,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = onDone) { Text("Done") }
    }
}

/** Captures one in-memory frame, or null on failure — never throws. */
private suspend fun captureOneFrame(
    imageCapture: ImageCapture,
    context: android.content.Context
): Bitmap? = suspendCancellableCoroutine { continuation ->
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toJpegBitmapOrNull()
                image.close()
                continuation.resume(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                continuation.resume(null)
            }
        }
    )
}

private fun ImageProxy.toJpegBitmapOrNull(): Bitmap? = try {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} catch (e: Exception) {
    null
}
