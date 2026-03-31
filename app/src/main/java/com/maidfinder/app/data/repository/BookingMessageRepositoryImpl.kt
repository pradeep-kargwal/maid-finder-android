package com.maidfinder.app.data.repository

import com.maidfinder.app.data.local.dao.*
import com.maidfinder.app.data.local.entity.*
import com.maidfinder.app.data.remote.api.*
import com.maidfinder.app.data.remote.dto.*
import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.repository.*
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val bookingDao: BookingDao,
    private val bookingApi: BookingApiService
) : BookingRepository {

    override fun getBookings(userId: String, isClient: Boolean, status: BookingStatus?): Flow<Resource<List<Booking>>> = flow {
        emit(Resource.Loading)
        val cachedFlow = if (status != null) bookingDao.getBookingsByStatus(userId, status.name) else bookingDao.getBookings(userId)
        cachedFlow.first().let { cached ->
            if (cached.isNotEmpty()) emit(Resource.Success(cached.map { it.toDomain() }, DataSource.LOCAL))
        }
        try {
            val response = bookingApi.getBookings(status?.name)
            if (response.success && response.data != null) {
                val bookings = response.data.map { it.toEntity() }
                bookingDao.insertAll(bookings)
                emit(Resource.Success(bookings.map { it.toDomain() }, DataSource.REMOTE))
            }
        } catch (e: Exception) { emit(Resource.Error(e.message ?: "Network error")) }
    }

    override suspend fun getBookingById(id: String): Resource<Booking> =
        bookingDao.getBookingById(id)?.let { Resource.Success(it.toDomain(), DataSource.LOCAL) } ?: Resource.Error("Not found")

    override suspend fun createBooking(booking: Booking): Result<Booking> = try {
        val request = CreateBookingRequest(booking.maidId, booking.jobId, booking.dateStart, booking.dateEnd, booking.agreedRate)
        val response = bookingApi.createBooking(request)
        if (response.success && response.data != null) {
            val entity = response.data.toEntity()
            bookingDao.insert(entity)
            Result.success(entity.toDomain())
        } else Result.failure(Exception(response.error?.message ?: "Failed"))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateStatus(bookingId: String, status: BookingStatus): Result<Unit> = try {
        bookingDao.updateStatus(bookingId, status.name)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun cancelBooking(bookingId: String, reason: String): Result<Unit> = try {
        val response = bookingApi.cancelBooking(bookingId, CancelRequest(reason))
        if (response.success) { bookingDao.updateStatus(bookingId, "CANCELLED"); Result.success(Unit) }
        else Result.failure(Exception(response.error?.message))
    } catch (e: Exception) { bookingDao.updateStatus(bookingId, "CANCELLED"); Result.success(Unit) }
}

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {

    private val sampleConversations = listOf(
        Conversation("conv_001", "maid_001", "Lakshmi R.", true,
            Message("m1", "conv_001", "maid_001", "client_001", MessageType.TEXT, "I'll be there at 8 AM tomorrow!"), 2, System.currentTimeMillis() - 300000),
        Conversation("conv_002", "maid_002", "Sunita K.", false,
            Message("m2", "conv_002", "client_001", "maid_002", MessageType.TEXT, "Thank you, see you then"), 0, System.currentTimeMillis() - 3600000)
    )
    private val messagesMap = mutableMapOf("conv_001" to mutableListOf(
        Message("msg_001", "conv_001", "client_001", "maid_001", MessageType.TEXT, "Hi! Are you available for cleaning tomorrow?"),
        Message("msg_002", "conv_001", "maid_001", "client_001", MessageType.TEXT, "Yes, I'm available. What time works for you?"),
        Message("msg_003", "conv_001", "client_001", "maid_001", MessageType.TEXT, "How about 8 AM?"),
        Message("msg_004", "conv_001", "maid_001", "client_001", MessageType.TEXT, "I'll be there at 8 AM tomorrow!")
    ))

    private val _conversations = MutableStateFlow(sampleConversations)
    override fun getConversations(): Flow<List<Conversation>> = _conversations.asStateFlow()

    override suspend fun getMessages(conversationId: String): List<Message> = messagesMap[conversationId] ?: emptyList()

    override suspend fun sendMessage(conversationId: String, senderId: String, receiverId: String, content: String): Result<Message> {
        val msg = Message("msg_${UUID.randomUUID().toString().take(8)}", conversationId, senderId, receiverId, MessageType.TEXT, content)
        messagesMap.getOrPut(conversationId) { mutableListOf() }.add(msg)
        _conversations.value = _conversations.value.map { if (it.id == conversationId) it.copy(lastMessage = msg, updatedAt = System.currentTimeMillis()) else it }.sortedByDescending { it.updatedAt }
        return Result.success(msg)
    }

    override fun markAsRead(conversationId: String) {
        _conversations.value = _conversations.value.map { if (it.id == conversationId) it.copy(unreadCount = 0) else it }
    }
}

private fun BookingDto.toEntity() = BookingEntity(id, clientId, clientName, maidId, maidName, jobId, status, dateStart, dateEnd, agreedRate, rateType, totalAmount, System.currentTimeMillis())
private fun BookingEntity.toDomain() = Booking(id, clientId, clientName, maidId, maidName, jobId,
    runCatching { BookingStatus.valueOf(status) }.getOrDefault(BookingStatus.PENDING), dateStart, dateEnd, agreedRate,
    runCatching { BudgetType.valueOf(rateType) }.getOrDefault(BudgetType.HOURLY), totalAmount, createdAt)
