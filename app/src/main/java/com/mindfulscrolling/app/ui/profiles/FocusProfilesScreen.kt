package com.mindfulscrolling.app.ui.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindfulscrolling.app.data.local.entity.FocusProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusProfilesScreen(
    onNavigateToEditProfile: (Long) -> Unit,
    viewModel: FocusProfilesViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<FocusProfileEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Focus Profiles") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Create Profile")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(profiles) { profile ->
                ProfileItem(
                    profile = profile,
                    isActive = profile.isActive,
                    onActivate = { viewModel.toggleProfileActivation(profile) },
                    onEdit = { onNavigateToEditProfile(profile.id) },
                    onDelete = { showDeleteDialog = profile }
                )
            }
        }

        if (showCreateDialog) {
            CreateProfileDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name ->
                    viewModel.createProfile(name, "default_icon")
                    showCreateDialog = false
                }
            )
        }
        
        if (showDeleteDialog != null) {
             AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Profile") },
                text = { Text("Are you sure you want to delete '${showDeleteDialog?.name}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog?.let { viewModel.deleteProfile(it) }
                            showDeleteDialog = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileItem(
    profile: FocusProfileEntity,
    isActive: Boolean,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (isActive) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (profile.scheduleEnabled) {
                    val days = profile.daysOfWeek?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                    val daysText = days.joinToString(" ") { dayLabels.getOrElse(it - 1) { "?" } }
                    
                    fun formatTime(minutes: Int?): String {
                        if (minutes == null) return "??"
                        val h = minutes / 60
                        val m = minutes % 60
                        return String.format("%02d:%02d", h, m)
                    }
                    
                    Text(
                        text = "Scheduled: $daysText (${formatTime(profile.startTime)} - ${formatTime(profile.endTime)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = isActive,
                    onCheckedChange = { onActivate() }
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Profile") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Profile Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
