package com.mindfulscrolling.app.ui.analytics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindfulscrolling.app.domain.model.AppUsageItem
import com.mindfulscrolling.app.domain.model.BarData
import com.mindfulscrolling.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    initialTab: String? = null,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialTab) {
        initialTab?.let { viewModel.onTabSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Date Navigator
            item {
                DateNavigator(
                    date = uiState.selectedDate,
                    onPreviousDay = { viewModel.onDateSelected(uiState.selectedDate.minusDays(1)) },
                    onNextDay = {
                        if (uiState.selectedDate < LocalDate.now()) {
                            viewModel.onDateSelected(uiState.selectedDate.plusDays(1))
                        }
                    }
                )
            }

            // Tab selector
            item {
                PillTabSelector(
                    tabs = listOf("Screen Time", "App Launches", "Screen Unlocks"),
                    selectedTab = uiState.selectedTab,
                    onTabSelected = viewModel::onTabSelected
                )
            }

            // Graph
            item {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    AnalyticsGraph(
                        data = uiState.graphData,
                        selectedTab = uiState.selectedTab
                    )
                }
            }

            // Summary header
            if (uiState.overviewList.isNotEmpty()) {
                item {
                    Text(
                        "App Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // App usage list
            items(uiState.overviewList) { item ->
                val appInfo = uiState.appInfoMap[item.packageName]
                AppUsageRow(
                    item = item,
                    icon = appInfo?.icon,
                    selectedTab = uiState.selectedTab
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun DateNavigator(
    date: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    val isToday = date == LocalDate.now()
    val displayText = if (isToday) {
        "Today, ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
    } else {
        "${date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())}, ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
    }
    
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(GradientTealStart.copy(alpha = 0.1f), GradientCyanEnd.copy(alpha = 0.05f))
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .then(
                    Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPreviousDay) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onNextDay, enabled = !isToday) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next",
                        tint = if (isToday) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                              else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun PillTabSelector(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = {
                    Text(
                        text = tab,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AnalyticsGraph(
    data: List<BarData>,
    selectedTab: String
) {
    val maxValue = data.maxOfOrNull { it.value }?.toFloat()?.coerceAtLeast(1f) ?: 1f
    
    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(data) { animationTriggered = true }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Summary
            val totalValue = data.sumOf { it.value }
            val summaryText = when (selectedTab) {
                "Screen Time" -> {
                    val h = totalValue / 3600000
                    val m = (totalValue % 3600000) / 60000
                    "${h}h ${m}m"
                }
                "App Launches" -> "$totalValue launches"
                "Screen Unlocks" -> "$totalValue unlocks"
                else -> ""
            }
            
            Text(
                text = summaryText,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { bar ->
                    val targetHeight = if (maxValue > 0) (bar.value / maxValue).coerceIn(0f, 1f) else 0f
                    val animatedHeight by animateFloatAsState(
                        targetValue = if (animationTriggered) targetHeight else 0f,
                        animationSpec = tween(durationMillis = 800, delayMillis = bar.hour * 20),
                        label = "bar_${bar.hour}"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight(animatedHeight.coerceAtLeast(0.01f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Teal400,
                                            GradientTealEnd.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }
            
            // Hour labels (show every 4 hours)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("12a", "4a", "8a", "12p", "4p", "8p", "").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AppUsageRow(
    item: AppUsageItem,
    icon: Drawable?,
    selectedTab: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            if (icon != null) {
                val bitmap = remember(item.packageName) {
                    val bmp = Bitmap.createBitmap(
                        icon.intrinsicWidth.coerceAtLeast(1),
                        icon.intrinsicHeight.coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bmp)
                    icon.setBounds(0, 0, canvas.width, canvas.height)
                    icon.draw(canvas)
                    bmp
                }
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = item.appName,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.appName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Usage bar
                LinearProgressIndicator(
                    progress = { item.percentage.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Teal400,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Value
            val displayValue = when (selectedTab) {
                "Screen Time" -> {
                    val m = item.usageMillis / 60000
                    val h = m / 60
                    if (h > 0) "${h}h ${m % 60}m" else "${m}m"
                }
                else -> "${item.usageMillis}"
            }
            Text(
                text = displayValue,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
