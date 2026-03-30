package com.maidfinder.app.data.repository

import com.maidfinder.app.data.model.Booking
import com.maidfinder.app.data.model.BookingStatus

/**
 * Repository interface for booking-related operations.
 */
interface BookingRepository {
    suspend fun getBookings(
        userId: String,
        status: BookingStatus? = null,
        isClient: Boolean = true
    ): List<Booking>

    suspend fun getBookingById(bookingId: String): Booking?

    suspend fun createBooking(booking: Booking): Booking

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus)

    suspend fun cancelBooking(bookingId: String, reason: String)
}
