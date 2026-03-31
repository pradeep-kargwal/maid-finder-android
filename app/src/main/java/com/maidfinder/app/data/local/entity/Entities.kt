package com.maidfinder.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phone: String,
    val role: String,
    val displayName: String,
    val photoUrl: String?,
    val isVerified: Boolean,
    val accessToken: String?,
    val refreshToken: String?,
    val isDemo: Boolean,
    val createdAt: Long,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "maid_profiles")
data class MaidProfileEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val photoUrl: String?,
    val skills: List<String>,
    val experienceYears: Int,
    val hourlyRate: Double,
    val languages: List<String>,
    val workType: String,
    val ratingAvg: Double,
    val ratingCount: Int,
    val isVerifiedPhoto: Boolean,
    val isVerifiedId: Boolean,
    val distanceKm: Double?,
    val isOnline: Boolean,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val clientName: String,
    val clientRating: Double,
    val jobType: String,
    val title: String,
    val description: String,
    val locationLat: Double,
    val locationLng: Double,
    val locationAddress: String,
    val dateStart: String,
    val dateEnd: String?,
    val budgetMin: Double,
    val budgetMax: Double,
    val budgetType: String,
    val status: String,
    val applicantCount: Int,
    val distanceKm: Double?,
    val createdAt: Long,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val clientName: String,
    val maidId: String,
    val maidName: String,
    val jobId: String?,
    val status: String,
    val dateStart: String,
    val dateEnd: String?,
    val agreedRate: Double,
    val rateType: String,
    val totalAmount: Double,
    val createdAt: Long,
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val type: String,
    val content: String,
    val isRead: Boolean,
    val isSent: Boolean = true,
    val createdAt: Long
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val participantId: String,
    val participantName: String,
    val isParticipantOnline: Boolean,
    val lastMessageContent: String?,
    val lastMessageType: String?,
    val unreadCount: Int,
    val updatedAt: Long
)

@Entity(tableName = "saved_maids")
data class SavedMaidEntity(
    @PrimaryKey val maidId: String,
    val savedAt: Long = System.currentTimeMillis()
)

class ListStringConverter {
    @TypeConverter
    fun fromList(value: List<String>): String = Gson().toJson(value)
    @TypeConverter
    fun toList(value: String): List<String> = Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
}
