package com.maidfinder.app.data.repository

import com.maidfinder.app.data.model.Booking
import com.maidfinder.app.data.model.BookingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * In-memory implementation of [BookingRepository].
 */
class InMemoryBookingRepository : BookingRepository {

    private val bookings = mutableListOf<Booking>()

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
        val newBooking = booking.copy(id = "book_${UUID.randomUUID().take(8)}")
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
