package com.mindfulscrolling.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable

@Composable
fun TakeABreakDialog(
    installedApps: List<com.mindfulscrolling.app.domain.model.AppInfo>,
    onDismiss: () -> Unit,
    onStartBreak: (Int, Set<String>) -> Unit
) {
    var selectedDuration by remember { mutableStateOf(0) }
    var customDays by remember { mutableStateOf("") }
    var customHours by remember { mutableStateOf("") }
    var customMinutes by remember { mutableStateOf("") }
    
    // Whitelist state
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var showAppList by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Take a Break") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Whitelist Selection
                Text("Allowed Apps:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { showAppList = !showAppList },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedApps.isEmpty()) "Select Apps to Allow" else "${selectedApps.size} Apps Selected")
                }
                
                if (showAppList) {
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(installedApps) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newSelection = selectedApps.toMutableSet()
                                        if (newSelection.contains(app.packageName)) {
                                            newSelection.remove(app.packageName)
                                        } else {
                                            newSelection.add(app.packageName)
                                        }
                                        selectedApps = newSelection
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Checkbox(
                                    checked = selectedApps.contains(app.packageName),
                                    onCheckedChange = null // Handled by Row click
                                )
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedDuration > 0) {
                        onStartBreak(selectedDuration, selectedApps)
                    } else {
                        val d = customDays.toIntOrNull() ?: 0
                        val h = customHours.toIntOrNull() ?: 0
                        val m = customMinutes.toIntOrNull() ?: 0
                        val totalMinutes = (d * 24 * 60) + (h * 60) + m
                        if (totalMinutes > 0) onStartBreak(totalMinutes, selectedApps)
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
