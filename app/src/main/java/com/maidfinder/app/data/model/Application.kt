package com.maidfinder.app.data.model

/**
 * Represents a maid's application to a job posting.
 */
data class Application(
    val id: String,
    val jobId: String,
    val maidId: String,
    val maidName: String,
    val maidPhotoUrl: String? = null,
    val maidRating: Double = 0.0,
    val maidHourlyRate: Double = 0.0,
    val maidExperienceYears: Int = 0,
    val maidIsVerified: Boolean = false,
    val message: String = "",
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null
)

enum class ApplicationStatus {
    PENDING, ACCEPTED, DECLINED, WITHDRAWN
}
