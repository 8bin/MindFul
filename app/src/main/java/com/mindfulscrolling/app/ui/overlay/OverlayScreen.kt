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
import androidx.compose.ui.unit.dp

@Composable
fun OverlayScreen(
    packageName: String,
    onDismiss: () -> Unit,
    onOverride: (String) -> Unit
) {
    var showChallenge by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
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
            if (showChallenge) {
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
