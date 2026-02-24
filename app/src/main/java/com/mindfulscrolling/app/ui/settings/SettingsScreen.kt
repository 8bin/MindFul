package com.mindfulscrolling.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindfulscrolling.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isStrictMode by viewModel.isStrictModeEnabled.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationInterval by viewModel.notificationInterval.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var pinDialogType by remember { mutableStateOf("enable") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showIntervalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Appearance Section
            item {
                SettingsSectionHeader("Appearance")
            }
            item {
                SettingsCard(
                    icon = Icons.Default.AccountCircle,
                    iconBackground = Teal400.copy(alpha = 0.15f),
                    iconTint = Teal400,
                    title = "Theme",
                    subtitle = when (themeMode) {
                        "LIGHT" -> "Light"
                        "DARK" -> "Dark"
                        else -> "System Default"
                    },
                    onClick = { showThemeDialog = true }
                )
            }

            // Notifications Section
            item {
                SettingsSectionHeader("Notifications")
            }
            item {
                SettingsToggleCard(
                    icon = Icons.Default.Notifications,
                    iconBackground = Amber400.copy(alpha = 0.15f),
                    iconTint = Amber400,
                    title = "Usage Alerts",
                    subtitle = if (notificationsEnabled) "Every ${notificationInterval} minutes" else "Disabled",
                    isChecked = notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
            }
            if (notificationsEnabled) {
                item {
                    SettingsCard(
                        icon = Icons.Default.DateRange,
                        iconBackground = Cyan400.copy(alpha = 0.15f),
                        iconTint = Cyan400,
                        title = "Alert Interval",
                        subtitle = "${notificationInterval} minutes",
                        onClick = { showIntervalDialog = true }
                    )
                }
            }

            // Security Section
            item {
                SettingsSectionHeader("Security")
            }
            item {
                SettingsToggleCard(
                    icon = Icons.Default.Lock,
                    iconBackground = CoralRed.copy(alpha = 0.15f),
                    iconTint = CoralRed,
                    title = "Strict Mode",
                    subtitle = if (isStrictMode) "Protected with PIN" else "Off",
                    isChecked = isStrictMode,
                    onCheckedChange = {
                        pinDialogType = if (it) "enable" else "disable"
                        pinError = null
                        showPinDialog = true
                    }
                )
            }
            if (isStrictMode) {
                item {
                    SettingsCard(
                        icon = Icons.Default.Edit,
                        iconBackground = Color(0xFF7C4DFF).copy(alpha = 0.15f),
                        iconTint = Color(0xFF7C4DFF),
                        title = "Change PIN",
                        subtitle = "Update your security PIN",
                        onClick = {
                            pinError = null
                            showChangePinDialog = true
                        }
                    )
                }
            }

            // Permissions Section
            item {
                SettingsSectionHeader("Permissions")
            }
            item {
                SettingsCard(
                    icon = Icons.Default.Settings,
                    iconBackground = SuccessGreen.copy(alpha = 0.15f),
                    iconTint = SuccessGreen,
                    title = "Manage Permissions",
                    subtitle = "Usage access, overlay, accessibility",
                    onClick = onNavigateToOnboarding
                )
            }

            // About Section
            item {
                SettingsSectionHeader("About")
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Teal400.copy(alpha = 0.08f), Cyan400.copy(alpha = 0.04f))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .then(
                                Modifier.background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "MindFul Scrolling",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Version 1.0.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your digital wellbeing companion",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Dialogs
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
            title = if (pinDialogType == "enable") "Set PIN" else "Enter PIN to Disable",
            isSettingPin = pinDialogType == "enable",
            onDismiss = { showPinDialog = false },
            onConfirm = { pin ->
                if (pinDialogType == "enable") {
                    viewModel.enableStrictMode(pin)
                    showPinDialog = false
                } else {
                    viewModel.disableStrictMode(pin,
                        onSuccess = { showPinDialog = false },
                        onError = { pinError = "Incorrect PIN" }
                    )
                }
            },
            errorMessage = pinError
        )
    }

    if (showChangePinDialog) {
        ChangePinDialog(
            onDismiss = { showChangePinDialog = false },
            onConfirm = { oldPin, newPin ->
                viewModel.changePin(oldPin, newPin,
                    onSuccess = { showChangePinDialog = false },
                    onError = { pinError = "Incorrect current PIN" }
                )
            },
            errorMessage = pinError
        )
    }

    if (showIntervalDialog) {
        IntervalSelectionDialog(
            currentInterval = notificationInterval,
            onIntervalSelected = {
                viewModel.setNotificationInterval(it)
                showIntervalDialog = false
            },
            onDismiss = { showIntervalDialog = false }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsCard(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SettingsToggleCard(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = Color.White
                )
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
        title = {
            Text(
                "Choose Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ThemeOption("System Default", currentTheme == "SYSTEM") { onThemeSelected("SYSTEM") }
                ThemeOption("Light", currentTheme == "LIGHT") { onThemeSelected("LIGHT") }
                ThemeOption("Dark", currentTheme == "DARK") { onThemeSelected("DARK") }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ThemeOption(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun IntervalSelectionDialog(
    currentInterval: Int,
    onIntervalSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val intervals = listOf(15, 30, 60)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Alert Interval",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                intervals.forEach { minutes ->
                    val label = if (minutes >= 60) "${minutes / 60} hour" else "$minutes minutes"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onIntervalSelected(minutes) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentInterval == minutes,
                            onClick = { onIntervalSelected(minutes) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (currentInterval == minutes) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun PinDialog(
    title: String,
    isSettingPin: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    errorMessage: String?
) {
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) input = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter 4-6 digit PIN") },
                    shape = RoundedCornerShape(12.dp)
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (input.length >= 4) onConfirm(input) },
                enabled = input.length >= 4,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ChangePinDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    errorMessage: String?
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Change PIN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) oldPin = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Current PIN") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPin = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New PIN") },
                    shape = RoundedCornerShape(12.dp)
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (oldPin.length >= 4 && newPin.length >= 4) onConfirm(oldPin, newPin) },
                enabled = oldPin.length >= 4 && newPin.length >= 4,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Change", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
