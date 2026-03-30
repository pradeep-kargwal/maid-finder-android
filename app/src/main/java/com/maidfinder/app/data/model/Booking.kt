package com.maidfinder.app.data.model

/**
 * Represents a confirmed booking between a client and a maid.
 */
data class Booking(
    val id: String,
    val clientId: String,
    val clientName: String,
    val maidId: String,
    val maidName: String,
    val jobId: String? = null,
    val status: BookingStatus = BookingStatus.PENDING,
    val dateStart: String,
    val dateEnd: String? = null,
    val shifts: List<Shift> = emptyList(),
    val agreedRate: Double,
    val rateType: BudgetType = BudgetType.HOURLY,
    val totalAmount: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null,
    val completedAt: Long? = null
)

enum class BookingStatus {
    PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, DISPUTED
}
