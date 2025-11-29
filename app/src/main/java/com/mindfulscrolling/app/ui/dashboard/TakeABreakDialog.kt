package com.mindfulscrolling.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TakeABreakDialog(
    onDismiss: () -> Unit,
    onStartBreak: (Int) -> Unit
) {
    var selectedDuration by remember { mutableStateOf(0) }
    var customDays by remember { mutableStateOf("") }
    var customHours by remember { mutableStateOf("") }
    var customMinutes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Take a Break") },
        text = {
            Column {
                Text("Select duration to block all distractions:")
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { selectedDuration = 15; customDays = ""; customHours = ""; customMinutes = "" },
                        colors = if (selectedDuration == 15) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("15m")
                    }
                    OutlinedButton(
                        onClick = { selectedDuration = 30; customDays = ""; customHours = ""; customMinutes = "" },
                        colors = if (selectedDuration == 30) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("30m")
                    }
                    OutlinedButton(
                        onClick = { selectedDuration = 60; customDays = ""; customHours = ""; customMinutes = "" },
                        colors = if (selectedDuration == 60) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("1h")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Custom Duration:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customDays,
                        onValueChange = { if (it.all { c -> c.isDigit() }) customDays = it },
                        label = { Text("Days") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customHours,
                        onValueChange = { if (it.all { c -> c.isDigit() }) customHours = it },
                        label = { Text("Hours") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { if (it.all { c -> c.isDigit() }) customMinutes = it },
                        label = { Text("Mins") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedDuration > 0) {
                        onStartBreak(selectedDuration)
                    } else {
                        val d = customDays.toIntOrNull() ?: 0
                        val h = customHours.toIntOrNull() ?: 0
                        val m = customMinutes.toIntOrNull() ?: 0
                        val totalMinutes = (d * 24 * 60) + (h * 60) + m
                        if (totalMinutes > 0) onStartBreak(totalMinutes)
                    }
                },
                enabled = selectedDuration > 0 || (customDays.isNotEmpty() || customHours.isNotEmpty() || customMinutes.isNotEmpty())
            ) {
                Text("Start Break")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
