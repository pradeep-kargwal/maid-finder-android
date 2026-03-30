package com.maidfinder.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.maidfinder.app.data.ServiceLocator
import com.maidfinder.app.ui.screens.ClientDashboardScreen
import com.maidfinder.app.ui.screens.MaidDashboardScreen
import com.maidfinder.app.ui.screens.RoleSelectionScreen
import com.maidfinder.app.ui.viewmodel.JobFeedViewModel
import com.maidfinder.app.ui.viewmodel.MaidListViewModel

/**
 * Defines all navigation routes in the MaidFinder app.
 */
sealed class Screen(val route: String) {
    data object RoleSelection : Screen("role_selection")
    data object ClientDashboard : Screen("client_dashboard")
    data object MaidDashboard : Screen("maid_dashboard")
}

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
            val viewModel: MaidListViewModel = viewModel(
                factory = MaidListViewModel.Factory(ServiceLocator.maidRepository)
            )
            ClientDashboardScreen(viewModel = viewModel)
        }

        composable(Screen.MaidDashboard.route) {
            val viewModel: JobFeedViewModel = viewModel(
                factory = JobFeedViewModel.Factory(ServiceLocator.jobRepository)
            )
            MaidDashboardScreen(viewModel = viewModel)
        }
    }
}
