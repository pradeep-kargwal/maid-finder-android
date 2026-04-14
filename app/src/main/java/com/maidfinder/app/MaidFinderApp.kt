package com.maidfinder.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.maidfinder.app.data.ServiceLocator
import com.maidfinder.app.navigation.MaidFinderNavGraph
import com.maidfinder.app.ui.viewmodel.AuthViewModel

@Composable
fun MaidFinderApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(ServiceLocator.authRepository)
    )
    MaidFinderNavGraph(navController = navController, authViewModel = authViewModel)
}
