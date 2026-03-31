package com.maidfinder.app

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.maidfinder.app.navigation.MaidFinderNavGraph
import com.maidfinder.app.ui.viewmodel.AuthViewModel

@Composable
fun MaidFinderApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    MaidFinderNavGraph(navController = navController, authViewModel = authViewModel)
}
