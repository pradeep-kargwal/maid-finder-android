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
            emit(Resource.Success(cached.map { it.toDomain() }, DataSource.LOCAL))
        }
        try {
            val response = bookingApi.getBookings(status?.name)
            if (response.success && response.data != null) {
                val bookings = response.data.map { it.toEntity() }
                bookingDao.insertAll(bookings)
                emit(Resource.Success(bookings.map { it.toDomain() }, DataSource.REMOTE))
            }
        } catch (_: Exception) { }
    }

    override suspend fun getBookingById(id: String): Resource<Booking> =
        bookingDao.getBookingById(id)?.let { Resource.Success(it.toDomain(), DataSource.LOCAL) } ?: Resource.Error("Not found")

    override suspend fun createBooking(booking: Booking): Result<Booking> = try {
        val entity = BookingEntity(
            id = "book_${UUID.randomUUID().toString().take(8)}",
            clientId = booking.clientId, clientName = booking.clientName,
            maidId = booking.maidId, maidName = booking.maidName,
            jobId = booking.jobId, status = "PENDING",
            dateStart = booking.dateStart, dateEnd = booking.dateEnd,
            agreedRate = booking.agreedRate, rateType = booking.rateType.name,
            totalAmount = booking.totalAmount, createdAt = System.currentTimeMillis()
        )
        bookingDao.insert(entity)
        Result.success(entity.toDomain())
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateStatus(bookingId: String, status: BookingStatus): Result<Unit> = try {
        bookingDao.updateStatus(bookingId, status.name)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun cancelBooking(bookingId: String, reason: String): Result<Unit> = try {
        bookingDao.updateStatus(bookingId, "CANCELLED")
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {

    override fun getConversations(): Flow<List<Conversation>> =
        messageDao.getConversations().map { list -> list.map { it.toDomain() } }

    override suspend fun getMessages(conversationId: String): List<Message> =
        messageDao.getMessages(conversationId).map { it.toDomain() }

    override suspend fun sendMessage(conversationId: String, senderId: String, receiverId: String, content: String): Result<Message> {
        val msg = MessageEntity(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            conversationId = conversationId, senderId = senderId, receiverId = receiverId,
            type = "TEXT", content = content, isRead = false, isSent = true,
            createdAt = System.currentTimeMillis()
        )
        messageDao.insertMessage(msg)
        return Result.success(msg.toDomain())
    }

    override suspend fun markAsRead(conversationId: String) {
        messageDao.markAsRead(conversationId)
        messageDao.clearUnread(conversationId)
    }
}

private fun BookingDto.toEntity() = BookingEntity(id, clientId, clientName, maidId, maidName, jobId, status, dateStart, dateEnd, agreedRate, rateType, totalAmount, System.currentTimeMillis())
private fun BookingEntity.toDomain() = Booking(id, clientId, clientName, maidId, maidName, jobId,
    runCatching { BookingStatus.valueOf(status) }.getOrDefault(BookingStatus.PENDING), dateStart, dateEnd, agreedRate,
    runCatching { BudgetType.valueOf(rateType) }.getOrDefault(BudgetType.HOURLY), totalAmount, createdAt)

private fun ConversationEntity.toDomain() = Conversation(
    id = id, participantId = participantId, participantName = participantName,
    isParticipantOnline = isParticipantOnline,
    lastMessage = if (lastMessageContent != null) Message("", id, "", "", MessageType.TEXT, lastMessageContent) else null,
    unreadCount = unreadCount, updatedAt = updatedAt
)

private fun MessageEntity.toDomain() = Message(
    id = id, conversationId = conversationId, senderId = senderId, receiverId = receiverId,
    type = runCatching { MessageType.valueOf(type) }.getOrDefault(MessageType.TEXT),
    content = content, isRead = isRead, createdAt = createdAt
)
