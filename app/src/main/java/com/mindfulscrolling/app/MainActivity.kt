package com.mindfulscrolling.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindfulscrolling.app.domain.manager.PermissionManager
import com.mindfulscrolling.app.ui.dashboard.DashboardScreen
import com.mindfulscrolling.app.ui.onboarding.OnboardingScreen
import com.mindfulscrolling.app.ui.theme.MindfulScrollingTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MindfulScrollingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = if (permissionManager.hasUsageStatsPermission() && permissionManager.hasOverlayPermission()) {
                        startService(android.content.Intent(this, com.mindfulscrolling.app.service.AppMonitoringService::class.java))
                        "dashboard"
                    } else {
                        "onboarding"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("onboarding") {
                            OnboardingScreen(
                                permissionManager = permissionManager,
                                onAllPermissionsGranted = {
                                    startService(android.content.Intent(this@MainActivity, com.mindfulscrolling.app.service.AppMonitoringService::class.java))
                                    navController.navigate("dashboard") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("dashboard") {
                            DashboardScreen(
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable("settings") {
                            com.mindfulscrolling.app.ui.settings.SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}
