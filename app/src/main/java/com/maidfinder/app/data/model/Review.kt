package com.maidfinder.app.data.model

/**
 * Represents a review left after a completed booking.
 */
data class Review(
    val id: String,
    val bookingId: String,
    val reviewerId: String,
    val reviewerName: String,
    val revieweeId: String,
    val rating: Int,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
