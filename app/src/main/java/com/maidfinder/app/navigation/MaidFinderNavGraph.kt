package com.maidfinder.app.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maidfinder.app.data.ServiceLocator
import com.maidfinder.app.data.model.UserRole
import com.maidfinder.app.ui.screens.*
import com.maidfinder.app.ui.screens.auth.LoginScreen
import com.maidfinder.app.ui.viewmodel.*

sealed class Screen(val route: String) {
    data object Login : Screen("login")
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
    data object Chat : Screen("chat/{conversationId}/{participantName}/{participantId}") {
        fun createRoute(conversationId: String, participantName: String, participantId: String) =
            "chat/$conversationId/$participantName/$participantId"
    }
}

@Composable
fun MaidFinderNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val startDest = if (authState.session?.isLoggedIn == true) {
        if (authState.session?.role == UserRole.CLIENT) Screen.ClientMain.route
        else Screen.MaidMain.route
    } else Screen.Login.route

    NavHost(navController = navController, startDestination = startDest) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    authViewModel.verifyOtp("9876543210", "000000", role)
                    val dest = if (role == UserRole.CLIENT) Screen.ClientMain.route else Screen.MaidMain.route
                    navController.navigate(dest) { popUpTo(Screen.Login.route) { inclusive = true } }
                },
                onDemoLogin = { role ->
                    authViewModel.loginDemo(role)
                    val dest = if (role == UserRole.CLIENT) Screen.ClientMain.route else Screen.MaidMain.route
                    navController.navigate(dest) { popUpTo(Screen.Login.route) { inclusive = true } }
                },
                onBackClick = { }
            )
        }

        composable(Screen.ClientMain.route) {
            val session = authState.session ?: return@composable
            ClientMainScreen(
                authSession = session,
                onMaidClick = { navController.navigate(Screen.MaidProfileDetail.createRoute(it)) },
                onPostJobClick = { navController.navigate(Screen.PostJob.route) },
                onConversationClick = { convId, name ->
                    navController.navigate(Screen.Chat.createRoute(convId, name, ""))
                },
                onSwitchRole = {
                    authViewModel.switchDemoRole()
                    navController.navigate(Screen.MaidMain.route) {
                        popUpTo(Screen.ClientMain.route) { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MaidMain.route) {
            val session = authState.session ?: return@composable
            MaidMainScreen(
                authSession = session,
                onJobClick = { navController.navigate(Screen.JobDetail.createRoute(it)) },
                onConversationClick = { convId, name ->
                    navController.navigate(Screen.Chat.createRoute(convId, name, ""))
                },
                onSwitchRole = {
                    authViewModel.switchDemoRole()
                    navController.navigate(Screen.ClientMain.route) {
                        popUpTo(Screen.MaidMain.route) { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
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
                onBookClick = { navController.navigate(Screen.Booking.createRoute(it)) }
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
            JobDetailScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.Booking.route,
            arguments = listOf(navArgument("maidId") { type = NavType.StringType })
        ) { backStackEntry ->
            val maidId = backStackEntry.arguments?.getString("maidId") ?: return@composable
            val viewModel: BookingViewModel = viewModel(
                factory = BookingViewModel.Factory(ServiceLocator.maidRepository, ServiceLocator.bookingRepository, maidId)
            )
            BookingScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onBookingComplete = {
                    val dest = if (authState.session?.role == UserRole.CLIENT) Screen.ClientMain.route else Screen.MaidMain.route
                    navController.popBackStack(dest, inclusive = false)
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("participantName") { type = NavType.StringType },
                navArgument("participantId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val convId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            val name = backStackEntry.arguments?.getString("participantName") ?: ""
            val partId = backStackEntry.arguments?.getString("participantId") ?: ""
            ChatScreen(
                messageRepository = ServiceLocator.messageRepository,
                conversationId = convId,
                participantName = name,
                participantId = partId,
                currentUserId = authState.session?.userId ?: "",
                isOnline = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
