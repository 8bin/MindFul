package com.mindfulscrolling.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val limits by viewModel.limits.collectAsState()
    var packageName by remember { mutableStateOf("") }
    var limitMinutes by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Set App Limits")
            
            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("Package Name (e.g. com.android.chrome)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = limitMinutes,
                onValueChange = { limitMinutes = it },
                label = { Text("Limit (minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = {
                    val mins = limitMinutes.toIntOrNull()
                    if (packageName.isNotBlank() && mins != null) {
                        viewModel.setLimit(packageName, mins)
                        packageName = ""
                        limitMinutes = ""
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Add Limit")
            }

            LazyColumn {
                items(limits) { limit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${limit.packageName}: ${limit.limitDurationMinutes}m",
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { viewModel.removeLimit(limit.packageName) }) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}
