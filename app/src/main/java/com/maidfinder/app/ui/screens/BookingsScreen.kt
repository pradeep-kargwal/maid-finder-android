package com.maidfinder.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maidfinder.app.domain.model.*
import com.maidfinder.app.ui.components.*
import com.maidfinder.app.ui.theme.*
import com.maidfinder.app.ui.viewmodel.BookingsListViewModel

@Composable
fun BookingsScreen(
    viewModel: BookingsListViewModel = hiltViewModel(),
    onBookingClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Active", "Completed", "Cancelled").forEachIndexed { index, title ->
                FilterChip(selected = uiState.selectedTab == index, onClick = { viewModel.loadBookings(index) },
                    label = { Text(title) }, modifier = Modifier.weight(1f))
            }
        }
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            uiState.bookings.isEmpty() -> EmptyState(Icons.Default.CalendarMonth, "No bookings", "Book a maid to see your bookings here")
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.bookings, key = { it.id }) { BookingCard(it, viewModel.isClient) { onBookingClick(it.id) } }
            }
        }
    }
}

@Composable
private fun BookingCard(booking: Booking, isClient: Boolean, onClick: () -> Unit) {
    val statusColor = when (booking.status) {
        BookingStatus.PENDING -> Warning; BookingStatus.CONFIRMED -> Info; BookingStatus.IN_PROGRESS -> BluePrimary
        BookingStatus.COMPLETED -> Success; BookingStatus.CANCELLED, BookingStatus.DISPUTED -> Error
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarInitials(name = if (isClient) booking.maidName else booking.clientName, size = 40.dp,
                        backgroundColor = if (isClient) GreenPrimary else BluePrimary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(if (isClient) booking.maidName else booking.clientName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(booking.dateStart, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                StatusBadge(text = booking.status.name.replace("_", " "), color = statusColor)
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoChip(Icons.Default.AttachMoney, "\u20B9${booking.agreedRate}/${booking.rateType.name.lowercase()}", color = GreenPrimary)
                InfoChip(Icons.Default.Schedule, "${booking.dateStart}${if (booking.dateEnd != null) " - ${booking.dateEnd}" else ""}")
            }
            if (booking.totalAmount > 0) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("Total: \u20B9${booking.totalAmount}", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 15.sp)
                }
            }
        }
    }
}
