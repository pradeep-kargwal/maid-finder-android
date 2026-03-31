package com.maidfinder.app.domain.usecase

import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.repository.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMaidsUseCase @Inject constructor(
    private val maidRepository: MaidRepository
) {
    operator fun invoke(lat: Double, lng: Double, radiusKm: Double, filters: MaidFilters = MaidFilters()): Flow<Resource<List<MaidProfile>>> =
        maidRepository.getNearbyMaids(lat, lng, radiusKm, filters)
}

class GetMaidProfileUseCase @Inject constructor(
    private val maidRepository: MaidRepository
) {
    suspend operator fun invoke(maidId: String): Resource<MaidProfile> =
        maidRepository.getMaidById(maidId)
}

class GetJobsUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    operator fun invoke(lat: Double? = null, lng: Double? = null, filters: JobFilters = JobFilters()): Flow<Resource<List<Job>>> =
        jobRepository.getJobs(lat, lng, filters)
}

class CreateJobUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    suspend operator fun invoke(job: Job): Result<Job> =
        jobRepository.createJob(job)
}

class ApplyToJobUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    suspend operator fun invoke(jobId: String, message: String? = null): Result<Unit> =
        jobRepository.applyToJob(jobId, message)
}

class GetBookingsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    operator fun invoke(userId: String, isClient: Boolean, status: BookingStatus? = null): Flow<Resource<List<Booking>>> =
        bookingRepository.getBookings(userId, isClient, status)
}

class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(booking: Booking): Result<Booking> =
        bookingRepository.createBooking(booking)
}

class CancelBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String, reason: String): Result<Unit> =
        bookingRepository.cancelBooking(bookingId, reason)
}

class GetConversationsUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(): Flow<List<Conversation>> =
        messageRepository.getConversations()
}

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(conversationId: String, senderId: String, receiverId: String, content: String): Result<Message> =
        messageRepository.sendMessage(conversationId, senderId, receiverId, content)
}
