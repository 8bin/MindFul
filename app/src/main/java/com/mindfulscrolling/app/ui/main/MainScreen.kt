package com.mindfulscrolling.app.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mindfulscrolling.app.ui.dashboard.DashboardScreen
import com.mindfulscrolling.app.ui.modes.ModesScreen
import com.mindfulscrolling.app.ui.analytics.AnalyticsScreen

@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToAppLimits: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTakeBreak: () -> Unit
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentDestination?.route == "dashboard_tab",
                    onClick = {
                        navController.navigate("dashboard_tab") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Modes") },
                    label = { Text("Modes") },
                    selected = currentDestination?.route == "modes_tab",
                    onClick = {
                        navController.navigate("modes_tab") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    selected = currentDestination?.route == "analytics_tab",
                    onClick = {
                        navController.navigate("analytics_tab") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard_tab",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard_tab") {
                DashboardScreen(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToAppLimits = onNavigateToAppLimits,
                    onNavigateToProfiles = onNavigateToProfiles,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToTakeBreak = onNavigateToTakeBreak
                )
            }
            composable("modes_tab") {
                ModesScreen()
            }
            composable("analytics_tab") {
                AnalyticsScreen()
            }
        }
    }
}
