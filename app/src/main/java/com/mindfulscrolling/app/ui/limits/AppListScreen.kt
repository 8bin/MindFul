package com.mindfulscrolling.app.ui.limits

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindfulscrolling.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: AppListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    var showLimitDialog by remember { mutableStateOf<AppItemUiState?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Text(
            text = "Apps",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search apps...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppFilterMode.entries.forEach { mode ->
                FilterChip(
                    selected = filterMode == mode,
                    onClick = { viewModel.onFilterModeChanged(mode) },
                    label = { Text(mode.label, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // App List
        when (val state = uiState) {
            is AppListUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is AppListUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is AppListUiState.Success -> {
                if (state.apps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📱", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No apps found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = state.apps,
                            key = { it.appInfo.packageName }
                        ) { appItem ->
                            AppListItem(
                                appItem = appItem,
                                onClick = { showLimitDialog = appItem },
                                onRemoveLimit = { viewModel.removeLimit(appItem.appInfo.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Limit + Notification Dialog
    showLimitDialog?.let { appItem ->
        SetLimitDialog(
            appItem = appItem,
            onDismiss = { showLimitDialog = null },
            onSetLimit = { minutes, notifInterval ->
                viewModel.setLimit(appItem.appInfo.packageName, minutes, notifInterval)
                showLimitDialog = null
            }
        )
    }
}

@Composable
private fun AppListItem(
    appItem: AppItemUiState,
    onClick: () -> Unit,
    onRemoveLimit: () -> Unit
) {
    val usageMinutes = appItem.usageMillis / 60000
    val usageHours = usageMinutes / 60
    val usageMins = usageMinutes % 60
    val usageText = if (usageHours > 0) "${usageHours}h ${usageMins}m" else "${usageMins}m"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                val icon = appItem.appInfo.icon
                if (icon != null) {
                    Image(
                        bitmap = drawableToBitmap(icon).asImageBitmap(),
                        contentDescription = appItem.appInfo.name,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text("📱", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appItem.appInfo.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (appItem.appInfo.isSystemApp) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "SYS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Today: $usageText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Show limit badge
                    appItem.limitMinutes?.let { limit ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            val limitText = if (limit >= 60) "${limit / 60}h ${limit % 60}m" else "${limit}m"
                            Text(
                                "Limit: $limitText",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Show notification badge
                    appItem.notificationIntervalMinutes?.let { interval ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFF9800).copy(alpha = 0.15f)
                        ) {
                            Text(
                                "🔔 ${interval}m",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFF9800),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Usage progress bar (if limit is set)
                appItem.limitMinutes?.let { limit ->
                    val progress = (usageMinutes.toFloat() / limit).coerceIn(0f, 1f)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (progress >= 1f) MaterialTheme.colorScheme.error 
                               else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Remove button (if limit exists)
            if (appItem.limitMinutes != null) {
                IconButton(
                    onClick = onRemoveLimit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove Limit",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SetLimitDialog(
    appItem: AppItemUiState,
    onDismiss: () -> Unit,
    onSetLimit: (minutes: Int, notificationInterval: Int?) -> Unit
) {
    var customMinutes by remember { mutableStateOf(appItem.limitMinutes?.toString() ?: "") }
    var selectedPreset by remember { mutableStateOf<Int?>(null) }
    var notificationEnabled by remember { mutableStateOf(appItem.notificationIntervalMinutes != null) }
    var selectedNotifInterval by remember { mutableStateOf(appItem.notificationIntervalMinutes ?: 30) }

    val presets = listOf(15, 30, 60, 120)
    val notifPresets = listOf(15, 30, 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = appItem.appInfo.icon
                if (icon != null) {
                    Image(
                        bitmap = drawableToBitmap(icon).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    appItem.appInfo.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        },
        text = {
            Column {
                // Time Limit Section
                Text(
                    "⏱️  Daily Time Limit",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Preset Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { minutes ->
                        val label = if (minutes >= 60) "${minutes / 60}h" else "${minutes}m"
                        FilterChip(
                            selected = selectedPreset == minutes,
                            onClick = {
                                selectedPreset = minutes
                                customMinutes = minutes.toString()
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Input
                OutlinedTextField(
                    value = customMinutes,
                    onValueChange = { 
                        if (it.all { c -> c.isDigit() }) {
                            customMinutes = it
                            selectedPreset = it.toIntOrNull()?.let { v -> if (v in presets) v else null }
                        }
                    },
                    label = { Text("Custom (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Notification Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Usage Popup",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFFFF9800)
                        )
                    )
                }

                if (notificationEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Notify every:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        notifPresets.forEach { minutes ->
                            FilterChip(
                                selected = selectedNotifInterval == minutes,
                                onClick = { selectedNotifInterval = minutes },
                                label = { Text("${minutes}m", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF9800),
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minutes = customMinutes.toIntOrNull()
                    if (minutes != null && minutes > 0) {
                        val notifInterval = if (notificationEnabled) selectedNotifInterval else null
                        onSetLimit(minutes, notifInterval)
                    }
                },
                enabled = (customMinutes.toIntOrNull() ?: 0) > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.coerceAtLeast(1),
        drawable.intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
