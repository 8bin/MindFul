package com.mindfulscrolling.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindfulscrolling.app.ui.common.PinDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isStrictModeEnabled by viewModel.isStrictModeEnabled.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var isEnablingStrictMode by remember { mutableStateOf(true) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Settings
            SettingsSection(title = "General") {
                SettingsItem(
                    icon = Icons.Default.Settings,
                    title = "Theme",
                    subtitle = themeMode.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { showThemeDialog = true }
                )
            }

            // Strict Mode
            SettingsSection(title = "Security") {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = "Strict Mode",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (isStrictModeEnabled) "Enabled (PIN Protected)" else "Disabled",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isStrictModeEnabled,
                            onCheckedChange = { 
                                isEnablingStrictMode = it
                                showPinDialog = true 
                            }
                        )
                    }
                    
                    if (isStrictModeEnabled) {
                        SettingsItem(
                            icon = Icons.Default.Lock, // Reusing lock icon or could use Edit
                            title = "Change PIN",
                            subtitle = "Update your Strict Mode PIN",
                            onClick = { 
                                isEnablingStrictMode = false // Reusing flag logic slightly differently or need new state
                                showChangePinDialog = true
                            }
                        )
                    }
                }
            }

            // About
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onThemeSelected = { 
                viewModel.setThemeMode(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showPinDialog) {
        PinDialog(
            title = if (isEnablingStrictMode) "Set Strict Mode PIN" else "Enter PIN to Disable",
            isSettingPin = isEnablingStrictMode,
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                if (isEnablingStrictMode) {
                    viewModel.enableStrictMode(pin)
                    showPinDialog = false
                } else {
                    viewModel.disableStrictMode(
                        pin = pin,
                        onSuccess = { showPinDialog = false },
                        onError = { pinError = "Incorrect PIN" } // We need to pass this error to PinDialog
                    )
                }
            },
            errorMessage = pinError // Need to update PinDialog to accept this
        )
    }
    
    if (showChangePinDialog) {
        var step by remember { mutableStateOf(0) } // 0: Old PIN, 1: New PIN
        var oldPin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        
        PinDialog(
            title = if (step == 0) "Enter Old PIN" else "Enter New PIN",
            isSettingPin = step == 1,
            onDismiss = { showChangePinDialog = false },
            onConfirm = { pin ->
                if (step == 0) {
                    viewModel.verifyPin(
                        pin = pin,
                        onSuccess = {
                            oldPin = pin
                            step = 1
                            error = null
                        },
                        onError = { error = "Incorrect Old PIN" }
                    )
                } else {
                    viewModel.changePin(
                        oldPin = oldPin,
                        newPin = pin,
                        onSuccess = { showChangePinDialog = false },
                        onError = { error = "Failed to change PIN" }
                    )
                }
            },
            errorMessage = error
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                ThemeOption(
                    text = "System Default",
                    selected = currentTheme == "SYSTEM",
                    onClick = { onThemeSelected("SYSTEM") }
                )
                ThemeOption(
                    text = "Light",
                    selected = currentTheme == "LIGHT",
                    onClick = { onThemeSelected("LIGHT") }
                )
                ThemeOption(
                    text = "Dark",
                    selected = currentTheme == "DARK",
                    onClick = { onThemeSelected("DARK") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
