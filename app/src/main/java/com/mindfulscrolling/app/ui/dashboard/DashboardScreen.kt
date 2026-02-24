package com.mindfulscrolling.app.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindfulscrolling.app.ui.theme.*
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTakeBreak: () -> Unit,
    onAnalyticsClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Welcome Section
        item {
            WelcomeSection(insightText = uiState.insightText)
        }

        // 2. Permission Section
        if (!uiState.allPermissionsGranted) {
            item {
                PermissionSection(onClick = onNavigateToSettings)
            }
        }

        // 3. Screen Time Ring Card
        item {
            ScreenTimeCard(
                totalUsageMillis = uiState.totalUsageMillis,
                weeklyHistory = uiState.weeklyUsageHistory,
                onAnalyticsClick = onAnalyticsClick
            )
        }

        // 4. Analytics Section (Horizontal Scroll)
        item {
            AnalyticsSection(
                totalUsageMillis = uiState.totalUsageMillis,
                appLaunchCount = uiState.appLaunchCount,
                unlockCount = uiState.unlockCount,
                sessionCount = uiState.sessionCount,
                onAnalyticsClick = onAnalyticsClick
            )
        }

        // 5. Take a Break Section
        item {
            TakeABreakSection(
                isBreakActive = uiState.isBreakActive,
                breakEndTime = uiState.breakEndTime,
                onTakeBreakClick = {
                    if (uiState.isBreakActive) viewModel.stopBreak() else onNavigateToTakeBreak()
                }
            )
        }


        // 7. Quick Action Section
        item {
            QuickActionSection()
        }

        // 8. Profile Section
        item {
            ProfileSection(onNavigateToProfiles)
        }
    }
}

@Composable
fun WelcomeSection(insightText: String) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 6 -> "Good Night 🌙"
        hour < 12 -> "Good Morning ☀️"
        hour < 17 -> "Good Afternoon 🌤️"
        hour < 21 -> "Good Evening 🌅"
        else -> "Good Night 🌙"
    }
    
    Column {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (insightText.isNotEmpty()) {
            Text(
                text = insightText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        } else {
            Text(
                text = "Loading your insights...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ScreenTimeCard(totalUsageMillis: Long, weeklyHistory: List<Long>, onAnalyticsClick: (String) -> Unit) {
    val totalHours = totalUsageMillis / 3600000f
    val dailyGoalHours = 4f // 4-hour daily goal
    val progress = (totalHours / dailyGoalHours).coerceIn(0f, 1f)
    
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1200),
        label = "progress"
    )
    
    LaunchedEffect(progress) {
        animatedProgress = progress
    }
    
    val hours = totalUsageMillis / 3600000
    val minutes = (totalUsageMillis % 3600000) / 60000
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAnalyticsClick("Screen Time") },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientTealStart.copy(alpha = 0.15f), GradientCyanEnd.copy(alpha = 0.08f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Today's Screen Time",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${hours}h ${minutes}m",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "of ${dailyGoalHours.toInt()}h daily goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Animated ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(100.dp)
                    ) {
                        val ringColor = if (progress >= 1f) CoralRed else Teal400
                        val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        
                        Canvas(modifier = Modifier.size(90.dp)) {
                            // Track
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 10f, cap = StrokeCap.Round),
                                size = Size(size.width, size.height)
                            )
                            // Progress
                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = animatedValue * 360f,
                                useCenter = false,
                                style = Stroke(width = 10f, cap = StrokeCap.Round),
                                size = Size(size.width, size.height)
                            )
                        }
                        
                        Text(
                            text = "${(animatedValue * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ringColor
                        )
                    }
                }
                
                // 7-day mini sparkline
                // 7-day mini sparkline — only when we have real data
                if (weeklyHistory.size == 7 && weeklyHistory.any { it > 0 }) {
                    Spacer(modifier = Modifier.height(16.dp))
                    WeeklySparkline(
                        data = weeklyHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklySparkline(data: List<Long>, modifier: Modifier = Modifier) {
    val lineColor = Teal400
    val fillColor = Teal400.copy(alpha = 0.15f)
    val dotColor = Teal400
    val todayDotColor = Cyan400
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val goalLineColor = CoralRed.copy(alpha = 0.3f)
    
    val dayLabels = remember {
        val cal = Calendar.getInstance()
        val labels = mutableListOf<String>()
        for (daysAgo in 6 downTo 0) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            labels.add(
                when (dayCal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "M"
                    Calendar.TUESDAY -> "T"
                    Calendar.WEDNESDAY -> "W"
                    Calendar.THURSDAY -> "T"
                    Calendar.FRIDAY -> "F"
                    Calendar.SATURDAY -> "S"
                    Calendar.SUNDAY -> "S"
                    else -> ""
                }
            )
        }
        labels
    }
    
    Column(modifier = modifier) {
        val maxVal = (data.maxOrNull() ?: 1L).coerceAtLeast(DashboardViewModel.DAILY_GOAL_MS)
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val w = size.width
            val h = size.height
            val padding = 16f
            val chartW = w - padding * 2
            val chartH = h - 8f
            
            val points = data.mapIndexed { i, value ->
                val x = padding + (chartW * i / (data.size - 1).coerceAtLeast(1))
                val y = chartH - (value.toFloat() / maxVal * chartH).coerceIn(0f, chartH) + 4f
                Offset(x, y)
            }
            
            // Goal line
            val goalY = chartH - (DashboardViewModel.DAILY_GOAL_MS.toFloat() / maxVal * chartH) + 4f
            drawLine(
                color = goalLineColor,
                start = Offset(padding, goalY),
                end = Offset(w - padding, goalY),
                strokeWidth = 2f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
            )
            
            // Fill area under the line
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, chartH + 4f)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, chartH + 4f)
                close()
            }
            drawPath(fillPath, fillColor)
            
            // Line connecting points
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            
            // Dots
            points.forEachIndexed { i, pt ->
                val isToday = i == points.size - 1
                drawCircle(
                    color = if (isToday) todayDotColor else dotColor,
                    radius = if (isToday) 6f else 4f,
                    center = pt
                )
                if (isToday) {
                    drawCircle(
                        color = todayDotColor.copy(alpha = 0.3f),
                        radius = 10f,
                        center = pt
                    )
                }
            }
        }
        
        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayLabels.forEachIndexed { i, label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == dayLabels.size - 1) Cyan400 else textColor,
                    fontWeight = if (i == dayLabels.size - 1) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PermissionSection(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Tap to enable all features",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun AnalyticsSection(
    totalUsageMillis: Long,
    appLaunchCount: Int,
    unlockCount: Int,
    sessionCount: Int,
    onAnalyticsClick: (String) -> Unit
) {
    Column {
        Text(
            "Analytics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                GradientAnalyticsCard(
                    title = "App Launches",
                    value = "$appLaunchCount",
                    icon = Icons.Default.PlayArrow,
                    gradientColors = listOf(Teal400.copy(alpha = 0.15f), Cyan400.copy(alpha = 0.08f)),
                    onClick = { onAnalyticsClick("App Launches") }
                )
            }
            item {
                GradientAnalyticsCard(
                    title = "Unlocks",
                    value = "$unlockCount",
                    icon = Icons.Default.Lock,
                    gradientColors = listOf(Amber400.copy(alpha = 0.15f), Color(0xFFFF8F00).copy(alpha = 0.08f)),
                    onClick = { onAnalyticsClick("Screen Unlocks") }
                )
            }
            item {
                GradientAnalyticsCard(
                    title = "Sessions",
                    value = "$sessionCount",
                    icon = Icons.Default.DateRange,
                    gradientColors = listOf(Cyan400.copy(alpha = 0.15f), Teal700.copy(alpha = 0.08f)),
                    onClick = { onAnalyticsClick("Usage Timeline") }
                )
            }
        }
    }
}

@Composable
fun GradientAnalyticsCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 140.dp, height = 110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .then(
                    Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
    val cardColors = if (isBreakActive) {
        listOf(CoralRed.copy(alpha = 0.15f), SoftRed.copy(alpha = 0.08f))
    } else {
        listOf(Teal400.copy(alpha = 0.12f), GradientTealEnd.copy(alpha = 0.06f))
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTakeBreakClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(cardColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .then(
                    Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isBreakActive)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isBreakActive) Icons.Default.Close else Icons.Default.Face,
                            contentDescription = null,
                            tint = if (isBreakActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isBreakActive) "Break Active" else "Take a Break",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBreakActive) "Until ${java.text.SimpleDateFormat("HH:mm").format(java.util.Date(breakEndTime))}" else "Disconnect and recharge",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



@Composable
fun QuickActionSection() {
    Column {
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionButton("Apps\nBlocked", Icons.Default.List, Modifier.weight(1f))
            QuickActionButton("Sites\nBlocked", Icons.Default.Place, Modifier.weight(1f))
            QuickActionButton("Keyword\nBlocked", Icons.Default.Info, Modifier.weight(1f))
            QuickActionButton("Adult\nContent", Icons.Default.Lock, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            QuickActionButton("Block\nReels", Icons.Default.PlayArrow, Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(3f))
        }
    }
}

@Composable
fun QuickActionButton(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileSection(onNavigateToProfiles: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToProfiles),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Profiles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Manage your custom modes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}
