package com.anchor.ui.symptoms

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anchor.core.audio.AudioDeliveryEngine
import com.anchor.core.haptics.HapticEngine
import com.anchor.core.haptics.HapticPatterns
import com.anchor.core.logging.SprintExperienceLog
import com.anchor.core.logging.SprintLogStore
import kotlinx.coroutines.delay
import java.util.Locale

enum class ExerciseCategoryType {
    BREATHING,
    SENSORY_54321,
    PROGRESSIVE_MUSCLE,
    COLD_WATER,
    GUIDED_REFLECTION
}

@Composable
fun InteractiveExerciseRunner(
    exerciseName: String,
    onClose: () -> Unit,
    hapticEngine: HapticEngine? = null,
    audioEngine: AudioDeliveryEngine? = null
) {
    val context = LocalContext.current
    val logStore = remember { SprintLogStore(context) }
    
    // TTS Initialization
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var ttsObj: TextToSpeech? = null
        ttsObj = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsObj?.language = Locale.US
                isTtsReady = true
            }
        }
        tts = ttsObj

        onDispose {
            ttsObj.stop()
            ttsObj.shutdown()
        }
    }

    val speak: (String) -> Unit = { text ->
        if (isTtsReady && tts != null) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "exercise_tts")
        }
    }

    var isCompleted by remember { mutableStateOf(false) }
    var userRating by remember { mutableFloatStateOf(4f) }
    var userNote by remember { mutableStateOf("") }

    // Categorize Exercise
    val type = remember(exerciseName) {
        when {
            exerciseName.contains("Breathing", ignoreCase = true) || exerciseName.contains("Paced", ignoreCase = true) -> ExerciseCategoryType.BREATHING
            exerciseName.contains("5-4-3-2-1", ignoreCase = true) -> ExerciseCategoryType.SENSORY_54321
            exerciseName.contains("Muscle", ignoreCase = true) || exerciseName.contains("PMR", ignoreCase = true) -> ExerciseCategoryType.PROGRESSIVE_MUSCLE
            exerciseName.contains("Cold Water", ignoreCase = true) || exerciseName.contains("Feet Press", ignoreCase = true) -> ExerciseCategoryType.COLD_WATER
            else -> ExerciseCategoryType.GUIDED_REFLECTION
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClose) {
                    Text("Exit", color = MaterialTheme.colorScheme.error)
                }
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isCompleted) {
                when (type) {
                    ExerciseCategoryType.BREATHING -> BreathingExerciseContent(
                        exerciseName = exerciseName,
                        onFinished = { isCompleted = true },
                        speak = speak,
                        hapticEngine = hapticEngine
                    )
                    ExerciseCategoryType.SENSORY_54321 -> Sensory54321Content(
                        onFinished = { isCompleted = true },
                        speak = speak,
                        hapticEngine = hapticEngine
                    )
                    ExerciseCategoryType.PROGRESSIVE_MUSCLE -> ProgressiveMuscleContent(
                        onFinished = { isCompleted = true },
                        speak = speak,
                        hapticEngine = hapticEngine
                    )
                    ExerciseCategoryType.COLD_WATER -> ColdWaterResetContent(
                        exerciseName = exerciseName,
                        onFinished = { isCompleted = true },
                        speak = speak,
                        hapticEngine = hapticEngine
                    )
                    ExerciseCategoryType.GUIDED_REFLECTION -> GuidedReflectionContent(
                        exerciseName = exerciseName,
                        onFinished = { isCompleted = true },
                        speak = speak
                    )
                }
            } else {
                // Post-Exercise Completion & Calm Rating Screen
                CompletionRatingScreen(
                    exerciseName = exerciseName,
                    rating = userRating,
                    onRatingChanged = { userRating = it },
                    note = userNote,
                    onNoteChanged = { userNote = it },
                    onSave = {
                        logStore.saveLog(
                            SprintExperienceLog(
                                comfortableToTalk = true,
                                distressLevel = "Calm Rating: ${userRating.toInt()}/5",
                                primaryTrigger = exerciseName,
                                finalState = if (userNote.isNotBlank()) userNote else "Exercise Completed"
                            )
                        )
                        onClose()
                    }
                )
            }
        }
    }
}

@Composable
fun BreathingExerciseContent(
    exerciseName: String,
    onFinished: () -> Unit,
    speak: (String) -> Unit,
    hapticEngine: HapticEngine?
) {
    val isBoxBreathing = exerciseName.contains("Box", ignoreCase = true)
    
    // Cycle parameters in seconds
    val inhaleTime = 4
    val hold1Time = if (isBoxBreathing) 4 else 0
    val exhaleTime = if (isBoxBreathing) 4 else 6
    val hold2Time = if (isBoxBreathing) 4 else 0

    var currentCycle by remember { mutableIntStateOf(1) }
    val totalCycles = 4
    var phase by remember { mutableStateOf("Inhale") }
    var phaseTimeRemaining by remember { mutableIntStateOf(inhaleTime) }
    val circleScale = remember { Animatable(0.5f) }

    LaunchedEffect(currentCycle, phase) {
        speak(phase)
        hapticEngine?.play(HapticPatterns.DOUBLE_PULSE)

        when (phase) {
            "Inhale" -> {
                circleScale.animateTo(1.0f, animationSpec = tween(durationMillis = inhaleTime * 1000, easing = LinearEasing))
                if (hold1Time > 0) {
                    phase = "Hold"
                    phaseTimeRemaining = hold1Time
                } else {
                    phase = "Exhale"
                    phaseTimeRemaining = exhaleTime
                }
            }
            "Hold" -> {
                delay((hold1Time * 1000).toLong())
                phase = "Exhale"
                phaseTimeRemaining = exhaleTime
            }
            "Exhale" -> {
                circleScale.animateTo(0.5f, animationSpec = tween(durationMillis = exhaleTime * 1000, easing = LinearEasing))
                if (hold2Time > 0) {
                    phase = "Rest Hold"
                    phaseTimeRemaining = hold2Time
                } else {
                    if (currentCycle < totalCycles) {
                        currentCycle++
                        phase = "Inhale"
                        phaseTimeRemaining = inhaleTime
                    } else {
                        speak("Breathing complete. Great job.")
                        onFinished()
                    }
                }
            }
            "Rest Hold" -> {
                delay((hold2Time * 1000).toLong())
                if (currentCycle < totalCycles) {
                    currentCycle++
                    phase = "Inhale"
                    phaseTimeRemaining = inhaleTime
                } else {
                    speak("Breathing complete. Great job.")
                    onFinished()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Cycle $currentCycle of $totalCycles",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = phase,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Pulse Animation Circle
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(circleScale.value)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = phase,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Follow the expanding circle. Focus purely on your breath.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onFinished,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Complete Early")
            }
        }
    }
}

@Composable
fun Sensory54321Content(
    onFinished: () -> Unit,
    speak: (String) -> Unit,
    hapticEngine: HapticEngine?
) {
    val steps = listOf(
        "5 Things You Can SEE" to "Look around and spot 5 distinct items (e.g., lamp, wall clock, shoes).",
        "4 Things You Can TOUCH" to "Feel 4 physical textures (e.g., jeans fabric, cool table surface, chair cushion).",
        "3 Things You Can HEAR" to "Listen closely for 3 background sounds (e.g., fan hum, traffic, birds).",
        "2 Things You Can SMELL" to "Notice 2 scents around you or inhale deeply.",
        "1 Thing You Can TASTE" to "Notice 1 lingering taste in your mouth or take a sip of water."
    )

    var currentStep by remember { mutableIntStateOf(0) }
    val checkedItems = remember { mutableStateListOf(false, false, false, false, false) }

    LaunchedEffect(currentStep) {
        speak(steps[currentStep].first)
        hapticEngine?.play(HapticPatterns.DOUBLE_PULSE)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Step ${currentStep + 1} of 5",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = steps[currentStep].first,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = steps[currentStep].second,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkedItems[currentStep],
                        onCheckedChange = { checkedItems[currentStep] = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I have identified these items in my surroundings.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(top = 24.dp)) {
            Button(
                onClick = {
                    if (currentStep < 4) {
                        currentStep++
                    } else {
                        onFinished()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = checkedItems[currentStep]
            ) {
                Text(if (currentStep < 4) "Next Sense ->" else "Finish Grounding")
            }
        }
    }
}

@Composable
fun ProgressiveMuscleContent(
    onFinished: () -> Unit,
    speak: (String) -> Unit,
    hapticEngine: HapticEngine?
) {
    val stages = listOf(
        "Shoulders & Neck" to "Hunch your shoulders tight up toward your ears. Hold tension...",
        "Hands & Arms" to "Clench both fists firmly and tighten your arms. Hold tension...",
        "Abdomen & Core" to "Tighten your stomach muscles firmly. Hold tension...",
        "Legs & Feet" to "Press your feet into the ground and tense your thigh muscles. Hold tension...",
        "Full Body Release" to "Release all muscles completely. Let your body melt into relaxation."
    )

    var stageIndex by remember { mutableIntStateOf(0) }
    var secondsRemaining by remember { mutableIntStateOf(7) }

    LaunchedEffect(stageIndex) {
        speak(stages[stageIndex].first + ". " + stages[stageIndex].second)
        hapticEngine?.play(HapticPatterns.DOUBLE_PULSE)
        secondsRemaining = 7
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
        if (stageIndex < stages.size - 1) {
            stageIndex++
        } else {
            onFinished()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "PMR Stage ${stageIndex + 1} of ${stages.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stages[stageIndex].first,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stages[stageIndex].second,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$secondsRemaining s",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Button(
            onClick = onFinished,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done / Next")
        }
    }
}

@Composable
fun ColdWaterResetContent(
    exerciseName: String,
    onFinished: () -> Unit,
    speak: (String) -> Unit,
    hapticEngine: HapticEngine?
) {
    var timer by remember { mutableIntStateOf(30) }

    LaunchedEffect(Unit) {
        speak("Apply cool water to your face or hands. Press your feet into the floor.")
        hapticEngine?.play(HapticPatterns.DOUBLE_PULSE)
        while (timer > 0) {
            delay(1000L)
            timer--
        }
        onFinished()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sensory Shock & Nervous System Reset",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${timer}s",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Text(
            text = "Cold temperature stimulates the vagus nerve, immediately slowing down a racing heart and breaking dissociation.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onFinished,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I'm Steady Now")
        }
    }
}

@Composable
fun GuidedReflectionContent(
    exerciseName: String,
    onFinished: () -> Unit,
    speak: (String) -> Unit
) {
    var userReflection by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        speak("Take a moment to reflect and ground your mind.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Focus your attention on the present moment. Acknowledge distressing thoughts without judgment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = userReflection,
                onValueChange = { userReflection = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                label = { Text("Write a quick affirmation or grounding note...") },
                placeholder = { Text("e.g., 'I am safe in this room right now.'") }
            )
        }

        Column(modifier = Modifier.padding(top = 24.dp)) {
            Button(
                onClick = onFinished,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Complete Exercise")
            }
        }
    }
}

@Composable
fun CompletionRatingScreen(
    exerciseName: String,
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    note: String,
    onNoteChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✓", fontSize = 36.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Exercise Complete!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "You completed $exerciseName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "How calm do you feel right now?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = rating,
                        onValueChange = onRatingChanged,
                        valueRange = 1f..5f,
                        steps = 3
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 - Still Anxious", style = MaterialTheme.typography.labelSmall)
                        Text("3 - Steady", style = MaterialTheme.typography.labelSmall)
                        Text("5 - Very Calm", style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = onNoteChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Reflection Note (Optional)") },
                        placeholder = { Text("What helped most during this exercise?") }
                    )
                }
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Save & Return Home")
        }
    }
}
