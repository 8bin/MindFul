package com.mindfulscrolling.app.ui.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val appStates by viewModel.appListState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(profile?.name ?: "Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Profile Name Edit
            OutlinedTextField(
                value = profile?.name ?: "",
                onValueChange = { viewModel.updateProfileName(it) },
                label = { Text("Profile Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            HorizontalDivider()

            Text(
                text = "Select Apps to Block/Limit",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(appStates) { appState ->
                    AppSelectionItem(
                        appState = appState,
                        onToggle = { viewModel.toggleAppSelection(appState.appInfo) },
                        onLimitChange = { limit ->
                            viewModel.updateAppLimit(appState.appInfo.packageName, limit)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppSelectionItem(
    appState: AppProfileState,
    onToggle: () -> Unit,
    onLimitChange: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = appState.isSelected,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = appState.appInfo.name,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            if (appState.isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                // Options Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = appState.limitMinutes == 0L,
                        onClick = { onLimitChange(0) },
                        label = { Text("Block") },
                        modifier = Modifier.height(32.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = appState.limitMinutes == -1L,
                        onClick = { onLimitChange(-1) },
                        label = { Text("Allow") },
                        modifier = Modifier.height(32.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    var showDialog by remember { mutableStateOf(false) }
                    if (showDialog) {
                        TimeLimitDialog(
                            initialMinutes = if (appState.limitMinutes > 0) appState.limitMinutes else 30,
                            onDismiss = { showDialog = false },
                            onConfirm = { minutes ->
                                onLimitChange(minutes)
                                showDialog = false
                            }
                        )
                    }

                    FilterChip(
                        selected = appState.limitMinutes > 0,
                        onClick = { showDialog = true },
                        label = { 
                            Text(if (appState.limitMinutes > 0) "${appState.limitMinutes}m" else "Custom") 
                        },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
    }
}

@Composable
fun TimeLimitDialog(
    initialMinutes: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var minutes by remember { mutableStateOf(initialMinutes.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Time Limit") },
        text = {
            OutlinedTextField(
                value = minutes,
                onValueChange = { if (it.all { char -> char.isDigit() }) minutes = it },
                label = { Text("Minutes") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val m = minutes.toLongOrNull()
                    if (m != null && m > 0) {
                        onConfirm(m)
                    }
                }
            ) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
