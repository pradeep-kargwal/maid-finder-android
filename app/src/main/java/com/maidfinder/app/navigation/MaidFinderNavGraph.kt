package com.maidfinder.app.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maidfinder.app.domain.model.UserRole
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
        if (authState.session?.role == UserRole.CLIENT) Screen.ClientMain.route else Screen.MaidMain.route
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
                onConversationClick = { convId, name -> navController.navigate(Screen.Chat.createRoute(convId, name, "")) },
                onSwitchRole = {
                    authViewModel.switchDemoRole()
                    navController.navigate(Screen.MaidMain.route) { popUpTo(Screen.ClientMain.route) { inclusive = true } }
                },
                onLogout = { authViewModel.logout(); navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(Screen.MaidMain.route) {
            val session = authState.session ?: return@composable
            MaidMainScreen(
                authSession = session,
                onJobClick = { navController.navigate(Screen.JobDetail.createRoute(it)) },
                onConversationClick = { convId, name -> navController.navigate(Screen.Chat.createRoute(convId, name, "")) },
                onSwitchRole = {
                    authViewModel.switchDemoRole()
                    navController.navigate(Screen.ClientMain.route) { popUpTo(Screen.MaidMain.route) { inclusive = true } }
                },
                onLogout = { authViewModel.logout(); navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }
            )
        }

        composable(Screen.MaidProfileDetail.route, arguments = listOf(navArgument("maidId") { type = NavType.StringType })) {
            val vm: MaidProfileViewModel = hiltViewModel()
            MaidProfileDetailScreen(viewModel = vm, onBackClick = { navController.popBackStack() },
                onBookClick = { navController.navigate(Screen.Booking.createRoute(it)) })
        }

        composable(Screen.PostJob.route) {
            val vm: PostJobViewModel = hiltViewModel()
            PostJobScreen(viewModel = vm, onBackClick = { navController.popBackStack() }, onJobPosted = { navController.popBackStack() })
        }

        composable(Screen.JobDetail.route, arguments = listOf(navArgument("jobId") { type = NavType.StringType })) {
            val vm: JobDetailViewModel = hiltViewModel()
            JobDetailScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Booking.route, arguments = listOf(navArgument("maidId") { type = NavType.StringType })) {
            val vm: BookingViewModel = hiltViewModel()
            BookingScreen(viewModel = vm, onBackClick = { navController.popBackStack() },
                onBookingComplete = {
                    val dest = if (authState.session?.role == UserRole.CLIENT) Screen.ClientMain.route else Screen.MaidMain.route
                    navController.popBackStack(dest, inclusive = false)
                })
        }

        composable(Screen.Chat.route, arguments = listOf(
            navArgument("conversationId") { type = NavType.StringType },
            navArgument("participantName") { type = NavType.StringType },
            navArgument("participantId") { type = NavType.StringType }
        )) { backStackEntry ->
            val convId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            val name = backStackEntry.arguments?.getString("participantName") ?: ""
            ChatScreen(isOnline = true, onBackClick = { navController.popBackStack() })
        }
    }
}
