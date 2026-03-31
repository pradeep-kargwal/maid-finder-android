package com.maidfinder.app.domain.repository

import com.maidfinder.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val session: Flow<AuthSession?>
    val isLoggedIn: Boolean
    val currentUserId: String
    val currentRole: UserRole
    suspend fun sendOtp(phone: String): Result<Boolean>
    suspend fun verifyOtp(phone: String, otp: String, role: UserRole): Result<AuthSession>
    fun loginDemo(role: UserRole)
    fun switchDemoRole()
    fun logout()
}

interface MaidRepository {
    fun getNearbyMaids(lat: Double, lng: Double, radiusKm: Double, filters: MaidFilters = MaidFilters()): Flow<Resource<List<MaidProfile>>>
    suspend fun getMaidById(id: String): Resource<MaidProfile>
    suspend fun saveMaid(maidId: String)
    suspend fun removeMaid(maidId: String)
    suspend fun isMaidSaved(maidId: String): Boolean
    fun getSavedMaids(): Flow<List<MaidProfile>>
}

data class MaidFilters(
    val workType: WorkType? = null,
    val minRate: Double? = null,
    val maxRate: Double? = null,
    val minExperience: Int? = null,
    val languages: List<String>? = null,
    val minRating: Double? = null,
    val skills: List<Skill>? = null,
    val verifiedOnly: Boolean = false
)

interface JobRepository {
    fun getJobs(lat: Double? = null, lng: Double? = null, filters: JobFilters = JobFilters()): Flow<Resource<List<Job>>>
    suspend fun getJobById(id: String): Resource<Job>
    suspend fun createJob(job: Job): Result<Job>
    suspend fun applyToJob(jobId: String, message: String?): Result<Unit>
    fun getMyJobs(clientId: String): Flow<List<Job>>
}

data class JobFilters(
    val jobType: JobType? = null,
    val minBudget: Double? = null,
    val maxBudget: Double? = null,
    val radiusKm: Double = 5.0
)

interface BookingRepository {
    fun getBookings(userId: String, isClient: Boolean, status: BookingStatus? = null): Flow<Resource<List<Booking>>>
    suspend fun getBookingById(id: String): Resource<Booking>
    suspend fun createBooking(booking: Booking): Result<Booking>
    suspend fun updateStatus(bookingId: String, status: BookingStatus): Result<Unit>
    suspend fun cancelBooking(bookingId: String, reason: String): Result<Unit>
}

interface MessageRepository {
    fun getConversations(): Flow<List<Conversation>>
    suspend fun getMessages(conversationId: String): List<Message>
    suspend fun sendMessage(conversationId: String, senderId: String, receiverId: String, content: String): Result<Message>
    suspend fun markAsRead(conversationId: String)
}
