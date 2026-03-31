package com.maidfinder.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.maidfinder.app.domain.model.AuthSession
import com.maidfinder.app.ui.viewmodel.*

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun ClientMainScreen(
    authSession: AuthSession,
    onMaidClick: (String) -> Unit,
    onPostJobClick: () -> Unit,
    onConversationClick: (String, String) -> Unit,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    val maidListViewModel: MaidListViewModel = hiltViewModel()

    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home),
        BottomNavItem("Bookings", Icons.Default.CalendarMonth),
        BottomNavItem("Messages", Icons.AutoMirrored.Filled.Chat),
        BottomNavItem("Profile", Icons.Default.Person)
    )

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (index == 2) {
                                        Badge { Text("3") }
                                    }
                                }
                            ) {
                                Icon(
                                    if (selectedTab == index) item.selectedIcon else item.icon,
                                    contentDescription = item.label
                                )
                            }
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier.padding(padding),
            transitionSpec = {
                fadeIn() + slideInHorizontally { if (targetState > initialState) 30 else -30 } togetherWith
                    fadeOut() + slideOutHorizontally { if (targetState > initialState) -30 else 30 }
            },
            label = "client_tab"
        ) { tab ->
            when (tab) {
                0 -> ClientDashboardScreen(
                    viewModel = maidListViewModel,
                    onMaidClick = onMaidClick,
                    onPostJobClick = onPostJobClick
                )
                1 -> BookingsScreen(onBookingClick = {})
                2 -> MessagesScreen(onConversationClick = onConversationClick)
                3 -> ProfileScreen(
                    session = authSession,
                    onSwitchRole = onSwitchRole,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun MaidMainScreen(
    authSession: AuthSession,
    onJobClick: (String) -> Unit,
    onConversationClick: (String, String) -> Unit,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    val jobFeedViewModel: JobFeedViewModel = hiltViewModel()

    val items = listOf(
        BottomNavItem("Jobs", Icons.Default.Work),
        BottomNavItem("Bookings", Icons.Default.CalendarMonth),
        BottomNavItem("Messages", Icons.AutoMirrored.Filled.Chat),
        BottomNavItem("Profile", Icons.Default.Person)
    )

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (index == 2) {
                                        Badge { Text("1") }
                                    }
                                }
                            ) {
                                Icon(
                                    if (selectedTab == index) item.selectedIcon else item.icon,
                                    contentDescription = item.label
                                )
                            }
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier.padding(padding),
            transitionSpec = {
                fadeIn() + slideInHorizontally { if (targetState > initialState) 30 else -30 } togetherWith
                    fadeOut() + slideOutHorizontally { if (targetState > initialState) -30 else 30 }
            },
            label = "maid_tab"
        ) { tab ->
            when (tab) {
                0 -> MaidDashboardScreen(
                    viewModel = jobFeedViewModel,
                    onJobClick = onJobClick
                )
                1 -> BookingsScreen(onBookingClick = {})
                2 -> MessagesScreen(onConversationClick = onConversationClick)
                3 -> ProfileScreen(
                    session = authSession,
                    onSwitchRole = onSwitchRole,
                    onLogout = onLogout
                )
            }
        }
    }
}
