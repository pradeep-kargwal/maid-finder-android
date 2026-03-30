package com.maidfinder.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.maidfinder.app.ui.screens.ClientDashboardScreen
import com.maidfinder.app.ui.screens.MaidDashboardScreen
import com.maidfinder.app.ui.screens.RoleSelectionScreen

@Composable
fun MaidFinderNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.RoleSelection.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onClientSelected = {
                    navController.navigate(Screen.ClientDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
                onMaidSelected = {
                    navController.navigate(Screen.MaidDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ClientDashboard.route) {
            ClientDashboardScreen()
        }

        composable(Screen.MaidDashboard.route) {
            MaidDashboardScreen()
        }
    }
}
