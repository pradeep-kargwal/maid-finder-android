package com.maidfinder.app.data.repository

import com.maidfinder.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class InMemoryBookingRepository : BookingRepository {

    private val bookings = mutableListOf<Booking>(
        Booking(
            id = "book_001",
            clientId = "demo_client_001",
            clientName = "Priya M.",
            maidId = "maid_001",
            maidName = "Lakshmi R.",
            status = BookingStatus.CONFIRMED,
            dateStart = "2026-04-01",
            dateEnd = "2026-04-30",
            agreedRate = 150.0,
            rateType = BudgetType.HOURLY,
            totalAmount = 6000.0,
            createdAt = System.currentTimeMillis() - 86400000
        ),
        Booking(
            id = "book_002",
            clientId = "demo_client_001",
            clientName = "Priya M.",
            maidId = "maid_002",
            maidName = "Sunita K.",
            status = BookingStatus.COMPLETED,
            dateStart = "2026-03-15",
            dateEnd = "2026-03-20",
            agreedRate = 120.0,
            rateType = BudgetType.HOURLY,
            totalAmount = 1200.0,
            createdAt = System.currentTimeMillis() - 604800000
        ),
        Booking(
            id = "book_003",
            clientId = "demo_client_001",
            clientName = "Priya M.",
            maidId = "maid_004",
            maidName = "Anita D.",
            status = BookingStatus.PENDING,
            dateStart = "2026-04-05",
            dateEnd = "2026-04-10",
            agreedRate = 200.0,
            rateType = BudgetType.HOURLY,
            totalAmount = 2000.0,
            createdAt = System.currentTimeMillis() - 3600000
        ),
        Booking(
            id = "book_004",
            clientId = "client_002",
            clientName = "Meera P.",
            maidId = "maid_001",
            maidName = "Lakshmi R.",
            status = BookingStatus.IN_PROGRESS,
            dateStart = "2026-03-28",
            dateEnd = "2026-04-28",
            agreedRate = 150.0,
            rateType = BudgetType.HOURLY,
            totalAmount = 8000.0,
            createdAt = System.currentTimeMillis() - 172800000
        ),
        Booking(
            id = "book_005",
            clientId = "client_003",
            clientName = "Arun K.",
            maidId = "maid_001",
            maidName = "Lakshmi R.",
            status = BookingStatus.COMPLETED,
            dateStart = "2026-03-01",
            dateEnd = "2026-03-15",
            agreedRate = 150.0,
            rateType = BudgetType.HOURLY,
            totalAmount = 3000.0,
            createdAt = System.currentTimeMillis() - 2592000000
        ),
        Booking(
            id = "book_006",
            clientId = "demo_client_001",
            clientName = "Priya M.",
            maidId = "maid_003",
            maidName = "Priya M.",
            status = BookingStatus.CANCELLED,
            dateStart = "2026-03-25",
            agreedRate = 180.0,
            rateType = BudgetType.HOURLY,
            totalAmount = 0.0,
            createdAt = System.currentTimeMillis() - 432000000
        )
    )

    override suspend fun getBookings(
        userId: String,
        status: BookingStatus?,
        isClient: Boolean
    ): List<Booking> = withContext(Dispatchers.IO) {
        bookings
            .filter { booking ->
                val matchesUser = if (isClient) booking.clientId == userId else booking.maidId == userId
                val matchesStatus = status == null || booking.status == status
                matchesUser && matchesStatus
            }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun getBookingById(bookingId: String): Booking? =
        withContext(Dispatchers.IO) {
            bookings.find { it.id == bookingId }
        }

    override suspend fun createBooking(booking: Booking): Booking = withContext(Dispatchers.IO) {
        val newBooking = booking.copy(id = "book_${UUID.randomUUID().toString().take(8)}")
        bookings.add(0, newBooking)
        newBooking
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) =
        withContext(Dispatchers.IO) {
            val index = bookings.indexOfFirst { it.id == bookingId }
            if (index >= 0) {
                bookings[index] = bookings[index].copy(
                    status = status,
                    confirmedAt = if (status == BookingStatus.CONFIRMED) System.currentTimeMillis() else bookings[index].confirmedAt,
                    completedAt = if (status == BookingStatus.COMPLETED) System.currentTimeMillis() else bookings[index].completedAt
                )
            }
        }

    override suspend fun cancelBooking(bookingId: String, reason: String) =
        withContext(Dispatchers.IO) {
            val index = bookings.indexOfFirst { it.id == bookingId }
            if (index >= 0) {
                bookings[index] = bookings[index].copy(status = BookingStatus.CANCELLED)
            }
        }
}
