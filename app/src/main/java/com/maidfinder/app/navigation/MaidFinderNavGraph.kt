package com.maidfinder.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maidfinder.app.data.ServiceLocator
import com.maidfinder.app.ui.screens.BookingScreen
import com.maidfinder.app.ui.screens.ClientMainScreen
import com.maidfinder.app.ui.screens.JobDetailScreen
import com.maidfinder.app.ui.screens.MaidMainScreen
import com.maidfinder.app.ui.screens.MaidProfileDetailScreen
import com.maidfinder.app.ui.screens.PostJobScreen
import com.maidfinder.app.ui.screens.RoleSelectionScreen
import com.maidfinder.app.ui.viewmodel.BookingViewModel
import com.maidfinder.app.ui.viewmodel.JobDetailViewModel
import com.maidfinder.app.ui.viewmodel.MaidProfileViewModel
import com.maidfinder.app.ui.viewmodel.PostJobViewModel

sealed class Screen(val route: String) {
    data object RoleSelection : Screen("role_selection")
    data object ClientMain : Screen("client_main")
    data object MaidMain : Screen("maid_main")
    data object MaidProfileDetail : Screen("maid_profile/{maidId}") {
        fun createRoute(maidId: String) = "maid_profile/$maidId"
    }
    data object PostJob : Screen("post_job")
    data object JobDetail : Screen("job_detail/{jobId}") {
        fun createRoute(jobId: String) = "job_detail/$jobId"
    }
    data object Booking : Screen("booking/{maidId}") {
        fun createRoute(maidId: String) = "booking/$maidId"
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
                    navController.navigate(Screen.ClientMain.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
                onMaidSelected = {
                    navController.navigate(Screen.MaidMain.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ClientMain.route) {
            ClientMainScreen(
                onMaidClick = { maidId ->
                    navController.navigate(Screen.MaidProfileDetail.createRoute(maidId))
                },
                onPostJobClick = {
                    navController.navigate(Screen.PostJob.route)
                }
            )
        }

        composable(Screen.MaidMain.route) {
            MaidMainScreen(
                onJobClick = { jobId ->
                    navController.navigate(Screen.JobDetail.createRoute(jobId))
                }
            )
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
                onBackClick = { navController.popBackStack() },
                onBookClick = { id ->
                    navController.navigate(Screen.Booking.createRoute(id))
                }
            )
        }

        composable(Screen.PostJob.route) {
            val viewModel: PostJobViewModel = viewModel(
                factory = PostJobViewModel.Factory(ServiceLocator.jobRepository)
            )
            PostJobScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onJobPosted = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.JobDetail.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: return@composable
            val viewModel: JobDetailViewModel = viewModel(
                factory = JobDetailViewModel.Factory(ServiceLocator.jobRepository, jobId)
            )
            JobDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Booking.route,
            arguments = listOf(navArgument("maidId") { type = NavType.StringType })
        ) { backStackEntry ->
            val maidId = backStackEntry.arguments?.getString("maidId") ?: return@composable
            val viewModel: BookingViewModel = viewModel(
                factory = BookingViewModel.Factory(
                    ServiceLocator.maidRepository,
                    ServiceLocator.bookingRepository,
                    maidId
                )
            )
            BookingScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onBookingComplete = {
                    navController.popBackStack(Screen.ClientMain.route, inclusive = false)
                }
            )
        }
    }
}
