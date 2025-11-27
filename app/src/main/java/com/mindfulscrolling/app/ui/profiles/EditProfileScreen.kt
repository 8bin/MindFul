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
                modifier = Modifier.weight(1f)
            )
        }
        
        if (appState.isSelected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Limit: ", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(8.dp))
                // Simple limit selector for now (0 = Block, -1 = Allow, >0 = Limit)
                // Using a simple segmented button or radio row for MVP
                FilterChip(
                    selected = appState.limitMinutes == 0L,
                    onClick = { onLimitChange(0) },
                    label = { Text("Block") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = appState.limitMinutes == -1L,
                    onClick = { onLimitChange(-1) },
                    label = { Text("Allow") }
                )
                // Custom limit can be added later
            }
            HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
        }
    }
}
