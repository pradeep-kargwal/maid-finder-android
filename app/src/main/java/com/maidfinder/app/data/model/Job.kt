package com.maidfinder.app.data.model

/**
 * Represents a job posted by a client.
 */
data class Job(
    val id: String,
    val clientId: String,
    val clientName: String,
    val clientRating: Double = 0.0,
    val clientRatingCount: Int = 0,
    val jobType: JobType,
    val title: String = "",
    val description: String = "",
    val location: Location,
    val dateStart: String,
    val dateEnd: String? = null,
    val shifts: List<Shift> = emptyList(),
    val budgetMin: Double,
    val budgetMax: Double,
    val budgetType: BudgetType = BudgetType.HOURLY,
    val status: JobStatus = JobStatus.ACTIVE,
    val applicantCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val distanceKm: Double? = null
)

enum class JobType {
    PART_TIME, FULL_TIME, ONE_TIME
}

enum class BudgetType {
    HOURLY, DAILY, MONTHLY, FIXED
}

enum class JobStatus {
    ACTIVE, FILLED, EXPIRED, CANCELLED, COMPLETED
}

data class Shift(
    val type: ShiftType,
    val startTime: String,
    val endTime: String
)

enum class ShiftType {
    MORNING, AFTERNOON, EVENING, CUSTOM
}
