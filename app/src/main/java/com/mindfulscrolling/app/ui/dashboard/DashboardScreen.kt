package com.mindfulscrolling.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToAppLimits: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTakeBreak: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Welcome Section
        item {
            WelcomeSection()
        }

        // 2. Permission Section
        item {
            PermissionSection()
        }

        // 3. Analytics Section (Horizontal Scroll)
        item {
            AnalyticsSection(uiState.totalUsageMillis)
        }

        // 4. Take a Break Section
        item {
            TakeABreakSection(
                isBreakActive = uiState.isBreakActive,
                breakEndTime = uiState.breakEndTime,
                onTakeBreakClick = {
                    if (uiState.isBreakActive) viewModel.stopBreak() else onNavigateToTakeBreak()
                }
            )
        }

        // 5. Strictness Level Section
        item {
            StrictnessLevelSection()
        }

        // 6. Quick Action Section
        item {
            QuickActionSection()
        }

        // 7. Profile Section
        item {
            ProfileSection(onNavigateToProfiles)
        }
    }
}

@Composable
fun WelcomeSection() {
    Column {
        Text(
            text = "Hello, User 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Let's make today productive!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PermissionSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "All Systems Go",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Permissions are active",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun AnalyticsSection(totalUsageMillis: Long) {
    Column {
        Text("Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                AnalyticsCard(
                    title = "Screen Time",
                    value = "${totalUsageMillis / 60000 / 60}h ${totalUsageMillis / 60000 % 60}m",
                    icon = Icons.Default.DateRange
                )
            }
            item {
                AnalyticsCard(title = "App Launches", value = "42", icon = Icons.Default.PlayArrow)
            }
            item {
                AnalyticsCard(title = "Browsing", value = "1h 20m", icon = Icons.Default.Place)
            }
            item {
                AnalyticsCard(title = "Unlocks", value = "15", icon = Icons.Default.Lock)
            }
            item {
                AnalyticsCard(title = "Usage Time", value = "3h 10m", icon = Icons.Default.Phone)
            }
        }
    }
}

@Composable
fun AnalyticsCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier.size(width = 140.dp, height = 100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = title, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun TakeABreakSection(
    isBreakActive: Boolean,
    breakEndTime: Long,
    onTakeBreakClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onTakeBreakClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isBreakActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (isBreakActive) "Break Active" else "Take a Break",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isBreakActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = if (isBreakActive) "Until ${java.text.SimpleDateFormat("HH:mm").format(java.util.Date(breakEndTime))}" else "Disconnect instantly",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isBreakActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Icon(
                Icons.Default.Face,
                contentDescription = null,
                tint = if (isBreakActive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun StrictnessLevelSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Strictness Level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Moderate", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Icon(Icons.Default.Lock, contentDescription = null)
        }
    }
}

@Composable
fun QuickActionSection() {
    Column {
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            QuickActionButton("Apps\nBlocked", Icons.Default.List)
            QuickActionButton("Sites\nBlocked", Icons.Default.Place)
            QuickActionButton("Keyword\nBlocked", Icons.Default.Info)
            QuickActionButton("Adult\nContent", Icons.Default.Lock)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            QuickActionButton("Block\nReels", Icons.Default.PlayArrow)
        }
    }
}

@Composable
fun RowScope.QuickActionButton(text: String, icon: ImageVector) {
    Card(
        modifier = Modifier.weight(1f).padding(4.dp).height(80.dp).clickable { },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 10.sp)
        }
    }
}

@Composable
fun ProfileSection(onNavigateToProfiles: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToProfiles),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Focus Profiles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Manage your custom modes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
