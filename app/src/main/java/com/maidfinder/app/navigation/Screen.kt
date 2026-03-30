package com.maidfinder.app.navigation

/**
 * Defines all navigation routes in the MaidFinder app.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object RoleSelection : Screen("role_selection")
    data object ClientDashboard : Screen("client_dashboard")
    data object MaidDashboard : Screen("maid_dashboard")
}
