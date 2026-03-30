package com.maidfinder.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maidfinder.app.data.model.*
import com.maidfinder.app.data.repository.BookingRepository
import com.maidfinder.app.ui.components.*
import com.maidfinder.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    bookingRepository: BookingRepository,
    userId: String,
    isClient: Boolean,
    onBookingClick: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Active", "Completed", "Cancelled")
    val scope = rememberCoroutineScope()

    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedTab) {
        isLoading = true
        val status = when (selectedTab) {
            0 -> null
            1 -> BookingStatus.COMPLETED
            2 -> BookingStatus.CANCELLED
            else -> null
        }
        bookings = bookingRepository.getBookings(userId, status, isClient)
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = { Text(title) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = if (isClient) BluePrimary else GreenPrimary)
                }
            }
            bookings.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.CalendarMonth,
                    title = "No ${tabs[selectedTab].lowercase()} bookings",
                    subtitle = if (isClient) "Book a maid to see your bookings here"
                    else "Accept jobs to see your bookings here"
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookings, key = { it.id }) { booking ->
                        BookingCard(
                            booking = booking,
                            isClient = isClient,
                            onClick = { onBookingClick(booking.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    isClient: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (booking.status) {
        BookingStatus.PENDING -> Warning
        BookingStatus.CONFIRMED -> Info
        BookingStatus.IN_PROGRESS -> BluePrimary
        BookingStatus.COMPLETED -> Success
        BookingStatus.CANCELLED -> Error
        BookingStatus.DISPUTED -> Error
    }
    val statusText = booking.status.name.replace("_", " ")

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarInitials(
                        name = if (isClient) booking.maidName else booking.clientName,
                        size = 40.dp,
                        backgroundColor = if (isClient) GreenPrimary else BluePrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            if (isClient) booking.maidName else booking.clientName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            booking.dateStart,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(text = statusText, color = statusColor)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    icon = Icons.Default.AttachMoney,
                    text = "\u20B9${booking.agreedRate}/${booking.rateType.name.lowercase()}",
                    color = GreenPrimary
                )
                InfoChip(
                    icon = Icons.Default.Schedule,
                    text = "${booking.dateStart}${if (booking.dateEnd != null) " - ${booking.dateEnd}" else ""}"
                )
            }

            if (booking.totalAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        "Total: \u20B9${booking.totalAmount}",
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
