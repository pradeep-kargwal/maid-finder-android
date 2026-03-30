package com.maidfinder.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maidfinder.app.data.ServiceLocator
import com.maidfinder.app.ui.viewmodel.JobFeedViewModel
import com.maidfinder.app.ui.viewmodel.MaidListViewModel

data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)

@Composable
fun ClientMainScreen(
    onMaidClick: (String) -> Unit,
    onPostJobClick: () -> Unit
) {
    val maidListViewModel: MaidListViewModel = viewModel(
        factory = MaidListViewModel.Factory(ServiceLocator.maidRepository)
    )

    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home),
        BottomNavItem("Search", Icons.Default.Search),
        BottomNavItem("Jobs", Icons.Default.List),
        BottomNavItem("Messages", Icons.Default.Notifications),
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
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0, 1 -> ClientDashboardScreen(
                viewModel = maidListViewModel,
                onMaidClick = onMaidClick,
                onPostJobClick = onPostJobClick
            )
            2 -> PlaceholderScreen("My Jobs")
            3 -> PlaceholderScreen("Messages")
            4 -> PlaceholderScreen("Profile")
        }
    }
}

@Composable
fun MaidMainScreen(
    onJobClick: (String) -> Unit = {}
) {
    val jobFeedViewModel: JobFeedViewModel = viewModel(
        factory = JobFeedViewModel.Factory(ServiceLocator.jobRepository)
    )

    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home),
        BottomNavItem("Jobs", Icons.Default.DateRange),
        BottomNavItem("Bookings", Icons.Default.List),
        BottomNavItem("Messages", Icons.Default.Notifications),
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
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0, 1 -> MaidDashboardScreen(viewModel = jobFeedViewModel, onJobClick = onJobClick)
            2 -> PlaceholderScreen("My Bookings")
            3 -> PlaceholderScreen("Messages")
            4 -> PlaceholderScreen("Profile")
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.padding(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Coming soon",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
