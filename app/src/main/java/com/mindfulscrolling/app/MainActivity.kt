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
import com.mindfulscrolling.app.ui.onboarding.OnboardingScreen
import com.mindfulscrolling.app.ui.theme.MindfulScrollingTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mindfulscrolling.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Schedule Usage Sync
        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<com.mindfulscrolling.app.worker.SyncUsageWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        ).build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SyncUsageWorker",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = "SYSTEM")
            
            MindfulScrollingTheme(
                darkTheme = when (themeMode) {
                    "LIGHT" -> false
                    "DARK" -> true
                    else -> isSystemInDarkTheme()
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = if (permissionManager.hasUsageStatsPermission() && 
                        permissionManager.hasOverlayPermission() && 
                        permissionManager.isAccessibilityServiceEnabled()) {
                        startService(android.content.Intent(this, com.mindfulscrolling.app.service.AppMonitoringService::class.java))
                        "main"
                    } else {
                        "onboarding"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("onboarding") {
                            OnboardingScreen(
                                permissionManager = permissionManager,
                                onAllPermissionsGranted = {
                                    startService(android.content.Intent(this@MainActivity, com.mindfulscrolling.app.service.AppMonitoringService::class.java))
                                    navController.navigate("main") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("main") {
                            com.mindfulscrolling.app.ui.main.MainScreen(
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onNavigateToAppLimits = {
                                    navController.navigate("app_limits")
                                },
                                onNavigateToProfiles = {
                                    navController.navigate("profiles")
                                },
                                onNavigateToHistory = {
                                    navController.navigate("history")
                                },
                                onNavigateToTakeBreak = {
                                    navController.navigate("take_break")
                                }
                            )
                        }
                        composable("settings") {
                            com.mindfulscrolling.app.ui.settings.SettingsScreen()
                        }
                        composable("app_limits") {
                            com.mindfulscrolling.app.ui.limits.AppListScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("profiles") {
                            com.mindfulscrolling.app.ui.profiles.FocusProfilesScreen(
                                onNavigateToEditProfile = { profileId ->
                                    navController.navigate("edit_profile/$profileId")
                                }
                            )
                        }
                        composable(
                            route = "edit_profile/{profileId}",
                            arguments = listOf(androidx.navigation.navArgument("profileId") { type = androidx.navigation.NavType.LongType })
                        ) {
                            com.mindfulscrolling.app.ui.profiles.EditProfileScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("history") {
                            com.mindfulscrolling.app.ui.dashboard.UsageHistoryScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("take_break") {
                            com.mindfulscrolling.app.ui.dashboard.TakeABreakScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
