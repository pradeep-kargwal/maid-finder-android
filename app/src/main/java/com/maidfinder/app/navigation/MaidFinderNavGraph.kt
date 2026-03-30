package com.maidfinder.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maidfinder.app.data.ServiceLocator
import com.maidfinder.app.ui.screens.ClientDashboardScreen
import com.maidfinder.app.ui.screens.MaidDashboardScreen
import com.maidfinder.app.ui.screens.MaidProfileDetailScreen
import com.maidfinder.app.ui.screens.RoleSelectionScreen
import com.maidfinder.app.ui.viewmodel.JobFeedViewModel
import com.maidfinder.app.ui.viewmodel.MaidListViewModel
import com.maidfinder.app.ui.viewmodel.MaidProfileViewModel

sealed class Screen(val route: String) {
    data object RoleSelection : Screen("role_selection")
    data object ClientDashboard : Screen("client_dashboard")
    data object MaidDashboard : Screen("maid_dashboard")
    data object MaidProfileDetail : Screen("maid_profile/{maidId}") {
        fun createRoute(maidId: String) = "maid_profile/$maidId"
    }
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
            ClientDashboardScreen(
                viewModel = viewModel,
                onMaidClick = { maidId ->
                    navController.navigate(Screen.MaidProfileDetail.createRoute(maidId))
                }
            )
        }

        composable(Screen.MaidDashboard.route) {
            val viewModel: JobFeedViewModel = viewModel(
                factory = JobFeedViewModel.Factory(ServiceLocator.jobRepository)
            )
            MaidDashboardScreen(viewModel = viewModel)
        }

        composable(
            route = Screen.MaidProfileDetail.route,
            arguments = listOf(navArgument("maidId") { type = NavType.StringType })
        ) { backStackEntry ->
            val maidId = backStackEntry.arguments?.getString("maidId") ?: return@composable
            val viewModel: MaidProfileViewModel = viewModel(
                factory = MaidProfileViewModel.Factory(ServiceLocator.maidRepository, maidId)
            )
            MaidProfileDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
