package com.mindfulscrolling.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindfulscrolling.app.data.local.entity.FocusProfileEntity
import com.mindfulscrolling.app.domain.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeABreakScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val profiles = uiState.profiles
    val installedApps = uiState.installedApps
    
    var selectedDuration by remember { mutableStateOf(15) }
    var selectedProfileId by remember { mutableStateOf<Long?>(null) }
    var customDurationText by remember { mutableStateOf("") }
    
    // Initialize selected profile if available
    LaunchedEffect(profiles) {
        if (selectedProfileId == null && profiles.isNotEmpty()) {
            selectedProfileId = profiles.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Take a Break") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val duration = if (selectedDuration == -1) customDurationText.toIntOrNull() ?: 0 else selectedDuration
                    if (duration > 0 && selectedProfileId != null) {
                        viewModel.startBreakWithProfile(duration, selectedProfileId!!)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = (selectedDuration > 0 || (selectedDuration == -1 && customDurationText.toIntOrNull() ?: 0 > 0)) && selectedProfileId != null
            ) {
                Text("Start Break")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Duration", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DurationChip(15, selectedDuration) { selectedDuration = it }
                DurationChip(30, selectedDuration) { selectedDuration = it }
                DurationChip(60, selectedDuration) { selectedDuration = it }
                DurationChip(-1, selectedDuration, "Custom") { selectedDuration = it }
            }
            
            if (selectedDuration == -1) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customDurationText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) customDurationText = it },
                    label = { Text("Minutes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Focus Profile", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            var expanded by remember { mutableStateOf(false) }
            val selectedProfile = profiles.find { it.id == selectedProfileId }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedProfile?.name ?: "Select Profile",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    profiles.forEach { profile ->
                        DropdownMenuItem(
                            text = { Text(profile.name) },
                            onClick = {
                                selectedProfileId = profile.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Allowed Apps", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            // We need to fetch apps for the selected profile. 
            // Since we don't have them in UI state yet, we might need to fetch them or just show a placeholder.
            // Ideally ViewModel should expose `selectedProfileApps`.
            // For now, let's just list "Essential Apps" if it's the default profile.
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = "Apps allowed in ${selectedProfile?.name ?: "this profile"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Phone, Contacts, UPI, etc.", // Placeholder until we fetch real apps
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DurationChip(
    duration: Int,
    selectedDuration: Int,
    label: String? = null,
    onClick: (Int) -> Unit
) {
    FilterChip(
        selected = duration == selectedDuration,
        onClick = { onClick(duration) },
        label = { Text(label ?: "${duration}m") }
    )
}
