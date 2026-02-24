package com.mindfulscrolling.app.ui.dashboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindfulscrolling.app.domain.model.AppInfo
import com.mindfulscrolling.app.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeABreakScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDuration by remember { mutableStateOf(15) }
    var customDurationText by remember { mutableStateOf("") }
    val selectedApps = remember { mutableStateListOf<String>() }
    val selectedProfileIds = remember { mutableStateListOf<Long>() }
    // Track which apps came from each profile so we can remove them independently
    val profileAppsMap = remember { mutableStateMapOf<Long, List<String>>() }

    // Pre-select common essential apps
    LaunchedEffect(uiState.installedApps) {
        if (selectedApps.isEmpty() && uiState.installedApps.isNotEmpty()) {
            val essentials = listOf(
                "com.android.phone", "com.google.android.dialer",
                "com.android.contacts", "com.google.android.contacts",
                "com.whatsapp", "com.google.android.apps.nbu.paisa.user",
                "com.android.mms", "com.google.android.apps.messaging"
            )
            uiState.installedApps.forEach { app ->
                if (app.packageName in essentials) {
                    selectedApps.add(app.packageName)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Take a Break", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        GradientTealStart.copy(alpha = 0.15f),
                                        GradientCyanEnd.copy(alpha = 0.08f)
                                    )
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Text("🧘", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Time to unplug",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "During a break, all apps will be blocked except the ones you whitelist below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Duration Section
            item {
                Text(
                    "Choose Duration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DurationChip(15, selectedDuration) { selectedDuration = it; customDurationText = "" }
                    DurationChip(30, selectedDuration) { selectedDuration = it; customDurationText = "" }
                    DurationChip(60, selectedDuration) { selectedDuration = it; customDurationText = "" }
                    DurationChip(-1, selectedDuration, "Custom") { selectedDuration = it }
                }
            }

            if (selectedDuration == -1) {
                item {
                    OutlinedTextField(
                        value = customDurationText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) customDurationText = it },
                        label = { Text("Duration in minutes") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }
            }

            // Start Button (placed near duration for easy access)
            item {
                val duration = if (selectedDuration == -1) customDurationText.toIntOrNull() ?: 0 else selectedDuration
                Button(
                    onClick = {
                        if (duration > 0) {
                            viewModel.startBreak(duration, selectedApps.toSet())
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = duration > 0 && !uiState.isBreakActive,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Start Break${if (duration > 0) " • ${duration}m" else ""}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Break active indicator
            if (uiState.isBreakActive) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val remainingMs = uiState.breakEndTime - System.currentTimeMillis()
                            val remainingMin = if (remainingMs > 0) (remainingMs / 60000).toInt() else 0
                            Text(
                                "⏸️ Break Active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "~${remainingMin} minutes remaining",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Focus Profiles Section
            if (uiState.profiles.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "📋 Focus Profiles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Select profiles to whitelist their apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    val coroutineScope = rememberCoroutineScope()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.profiles.forEach { profile ->
                            val isSelected = profile.id in selectedProfileIds
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    coroutineScope.launch {
                                        if (isSelected) {
                                            // Deselect: remove this profile's apps
                                            profileAppsMap[profile.id]?.forEach { pkg ->
                                                // Only remove if no other selected profile also has it
                                                val otherProfilesHaveIt = profileAppsMap
                                                    .filter { it.key != profile.id && it.key in selectedProfileIds }
                                                    .values.any { pkg in it }
                                                if (!otherProfilesHaveIt) {
                                                    selectedApps.remove(pkg)
                                                }
                                            }
                                            profileAppsMap.remove(profile.id)
                                            selectedProfileIds.remove(profile.id)
                                        } else {
                                            // Select: fetch and add profile apps
                                            selectedProfileIds.add(profile.id)
                                            val apps = viewModel
                                                .getProfileApps(profile.id)
                                                .first()
                                            val pkgNames = apps.map { it.packageName }
                                            profileAppsMap[profile.id] = pkgNames
                                            pkgNames.forEach { pkg ->
                                                if (pkg !in selectedApps) {
                                                    selectedApps.add(pkg)
                                                }
                                            }
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = profile.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Essential Apps Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "🔓 Essential Apps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "These apps won't be blocked during your break",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "${selectedApps.size} selected",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // App List
            items(
                items = uiState.installedApps,
                key = { it.packageName }
            ) { app ->
                val isSelected = app.packageName in selectedApps
                WhitelistAppItem(
                    app = app,
                    isSelected = isSelected,
                    onToggle = {
                        if (isSelected) selectedApps.remove(app.packageName)
                        else selectedApps.add(app.packageName)
                    }
                )
            }

            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun WhitelistAppItem(
    app: AppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                val icon = app.icon
                if (icon != null) {
                    Image(
                        bitmap = drawableToBitmap(icon).asImageBitmap(),
                        contentDescription = app.name,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Text("📱", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            // Checkmark
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
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
        label = { Text(label ?: "${duration}m") },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(12.dp)
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
