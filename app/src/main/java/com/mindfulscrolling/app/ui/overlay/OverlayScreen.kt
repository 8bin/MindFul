package com.mindfulscrolling.app.ui.overlay

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.mutableFloatStateOf

@Composable
fun OverlayScreen(
    packageName: String,
    isBreakMode: Boolean = false,
    remainingTime: Long = 0,
    onDismiss: () -> Unit,
    onOverride: (String) -> Unit
) {
    var showChallenge by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f)), // Darker for break
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            if (isBreakMode) {
                BreakContent(
                    remainingTime = remainingTime,
                    onDismiss = onDismiss
                )
            } else if (showChallenge) {
                ChallengeContent(
                    onDismiss = { showChallenge = false },
                    onSuccess = { onOverride("Math Challenge Passed") }
                )
            } else {
                BlockedContent(
                    onDismiss = onDismiss,
                    onChallengeRequest = { showChallenge = true }
                )
            }
        }
    }
}

@Composable
fun BreakContent(
    remainingTime: Long,
    onDismiss: () -> Unit
) {
    var showBreathing by remember { mutableStateOf(false) }
    var currentRemainingTime by remember { mutableStateOf(remainingTime) }

    LaunchedEffect(remainingTime) {
        currentRemainingTime = remainingTime
        while (currentRemainingTime > 0) {
            kotlinx.coroutines.delay(1000)
            currentRemainingTime -= 1000
        }
    }

    if (showBreathing) {
        BreathingExercise(
            onFinish = { showBreathing = false }
        )
    } else {
        val days = currentRemainingTime / (24 * 60 * 60 * 1000)
        val hours = (currentRemainingTime % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        val minutes = (currentRemainingTime % (60 * 60 * 1000)) / (60 * 1000)
        val seconds = (currentRemainingTime % (60 * 1000)) / 1000
        
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Take a Break",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val timeString = if (days > 0) {
                String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds)
            } else {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }

            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Disconnect and Recharge",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showBreathing = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Start Breathing Exercise")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Close App")
            }
        }
    }
}

@Composable
fun BreathingExercise(
    onFinish: () -> Unit
) {
    var phase by remember { mutableStateOf("Breathe In") }
    var targetScale by remember { mutableFloatStateOf(1f) }
    
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetScale,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 4000)
    )
    
    LaunchedEffect(Unit) {
        while(true) {
            phase = "Breathe In"
            targetScale = 1.5f
            kotlinx.coroutines.delay(4000)
            
            phase = "Hold"
            kotlinx.coroutines.delay(2000)
            
            phase = "Breathe Out"
            targetScale = 1f
            kotlinx.coroutines.delay(4000)
            
            phase = "Hold"
            kotlinx.coroutines.delay(2000)
        }
    }

    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Breathing Exercise",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.Crossfade(
                targetState = phase,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
            ) { currentPhase ->
                Text(
                    text = currentPhase,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finish Exercise")
        }
    }
}

@Composable
fun BlockedContent(
    onDismiss: () -> Unit,
    onChallengeRequest: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Time's Up!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You've reached your limit for this app.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Close App")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        androidx.compose.material3.OutlinedButton(
            onClick = onChallengeRequest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("5 More Minutes")
        }
    }
}

@Composable
fun ChallengeContent(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    // Simple Math Challenge (Single/Small digits)
    val num1 = remember { (1..9).random() }
    val num2 = remember { (1..9).random() }
    val answer = num1 + num2
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Emergency Override",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Solve this to unlock for 5 minutes:")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$num1 + $num2 = ?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        androidx.compose.material3.OutlinedTextField(
            value = input,
            onValueChange = { 
                if (it.all { char -> char.isDigit() }) {
                    input = it
                    error = false
                }
            },
            label = { Text("Answer") },
            isError = error,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (error) {
            Text(
                text = "Incorrect answer",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    if (input == answer.toString()) {
                        onSuccess()
                    } else {
                        error = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Unlock")
            }
        }
    }
}
