package com.mindfulscrolling.app.ui.onboarding

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.mindfulscrolling.app.domain.manager.PermissionManager

@Composable
fun OnboardingScreen(
    permissionManager: PermissionManager,
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    
    var hasUsageStats by remember { mutableStateOf(false) }
    var hasOverlay by remember { mutableStateOf(false) }
    var hasAccessibility by remember { mutableStateOf(false) }

    // Check permissions on resume
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            hasUsageStats = permissionManager.hasUsageStatsPermission()
            hasOverlay = permissionManager.hasOverlayPermission()
            hasAccessibility = permissionManager.isAccessibilityServiceEnabled()
            
            if (hasUsageStats && hasOverlay && hasAccessibility) {
                onAllPermissionsGranted()
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Mindful",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "To help you manage your screen time, we need a few permissions.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            PermissionItem(
                title = "Usage Access",
                description = "Required to track how much time you spend on apps.",
                isGranted = hasUsageStats,
                onClick = { context.startActivity(permissionManager.getUsageStatsIntent()) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                title = "Display Over Other Apps",
                description = "Required to show reminders when you exceed limits.",
                isGranted = hasOverlay,
                onClick = { context.startActivity(permissionManager.getOverlayPermissionIntent()) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                title = "Accessibility Service",
                description = "Required to detect when you open apps and scroll.",
                isGranted = hasAccessibility,
                onClick = { context.startActivity(permissionManager.getAccessibilitySettingsIntent()) }
            )
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onClick,
            enabled = !isGranted
        ) {
            Text(if (isGranted) "Granted" else "Grant Permission")
        }
    }
}
