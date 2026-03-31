package com.maidfinder.app.domain.model

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
    val distanceKm: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class JobType { PART_TIME, FULL_TIME, ONE_TIME }
enum class BudgetType { HOURLY, DAILY, MONTHLY, FIXED }
enum class JobStatus { ACTIVE, FILLED, EXPIRED, CANCELLED, COMPLETED }

data class Shift(
    val type: ShiftType,
    val startTime: String,
    val endTime: String
)

enum class ShiftType { MORNING, AFTERNOON, EVENING, CUSTOM }

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
    val agreedRate: Double,
    val rateType: BudgetType = BudgetType.HOURLY,
    val totalAmount: Double,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BookingStatus { PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, DISPUTED }

data class Conversation(
    val id: String,
    val participantId: String,
    val participantName: String,
    val isParticipantOnline: Boolean = false,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val type: MessageType = MessageType.TEXT,
    val content: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MessageType { TEXT, VOICE, IMAGE, SYSTEM, BOOKING_CARD }
