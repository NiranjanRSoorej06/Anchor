package com.anchor.ui.symptoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SymptomCategory(
    val title: String,
    val description: String,
    val exercises: List<Pair<String, String>>
) {
    HYPERAROUSAL(
        title = "Panic, Anxiety & Hyperarousal",
        description = "Calm physical agitation, racing heart, and intense panic spikes.",
        exercises = listOf(
            "Resonant Paced Breathing (4s In / 6s Out)" to "Clinical slow-exhale breathing to activate parasympathetic nervous system calm.",
            "Box Breathing (4-4-4-4)" to "Equal duration inhale, hold, exhale, and hold cycle to restore focus.",
            "Progressive Muscle Relaxation (PMR-Lite)" to "Systematic muscle tension and release wave across shoulders and hands."
        )
    ),
    INTRUSION(
        title = "Flashbacks & Intrusive Memories",
        description = "Re-orient yourself immediately to the physical present moment.",
        exercises = listOf(
            "5-4-3-2-1 Sensory Grounding" to "Identify 5 things seen, 4 felt, 3 heard, 2 smelled, and 1 tasted.",
            "Safe-Place Visualization" to "Guided mental anchor to a secure, peaceful memory or environment.",
            "Present Statement Orientation" to "Affirm present time, location, and safety out loud."
        )
    ),
    DISSOCIATION(
        title = "Dissociation, Numbness & Freeze",
        description = "Re-engage sensory awareness when feeling detached or frozen.",
        exercises = listOf(
            "Physical Feet Press Grounding" to "Press feet firmly into floor to register physical stability.",
            "Temperature Reset (Cold Water)" to "Cool water on hands or face to trigger physiological dive reflex reset.",
            "Object Texture Description" to "Describe texture, color, and weight of a nearby physical item."
        )
    ),
    AVOIDANCE(
        title = "Avoidance, Sadness & Low Mood",
        description = "Gentle cognitive reframing and micro-action steps for low energy.",
        exercises = listOf(
            "Thought Defusion & Labeling" to "Acknowledge distressing thoughts as passing events without judgment.",
            "Micro-Step Action Activation" to "Choose one tiny 30-second manageable physical action.",
            "Gratitude Anchor" to "Name 3 small things that provided comfort today."
        )
    ),
    SLEEP(
        title = "Sleep Distress & Nightmares",
        description = "Prepare body and mind for rest or recover from distressing dreams.",
        exercises = listOf(
            "Bedtime Wind-Down Breathing" to "Gentle slow rhythmic breathing tailored for rest.",
            "Post-Nightmare Grounding" to "Re-orient to your safe bedroom environment after waking up."
        )
    )
}

@Composable
fun ManageSymptomsScreen(
    onBack: () -> Unit,
    onNeedHelp: () -> Unit,
    onRunExercise: (String) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf<SymptomCategory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("Back to Home", color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "Manage Symptoms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(1.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "What symptom do you want to manage right now?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SymptomCategory.values().forEach { cat ->
                val isExpanded = selectedCategory == cat
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = {
                        selectedCategory = if (isExpanded) null else cat
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = cat.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            cat.exercises.forEach { (name, desc) ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = { onRunExercise(name) },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text("Start Exercise", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Professional Handoff Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Need Immediate Professional Support?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Access 24/7 verified Indian crisis helplines, local clinics, and emergency support.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onNeedHelp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Text("Get Professional Support Now")
                    }
                }
            }
        }
    }
}
