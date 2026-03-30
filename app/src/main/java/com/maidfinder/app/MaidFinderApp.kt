package com.maidfinder.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.maidfinder.app.navigation.MaidFinderNavGraph

@Composable
fun MaidFinderApp() {
    val navController = rememberNavController()
    MaidFinderNavGraph(navController = navController)
}
