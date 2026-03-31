package com.maidfinder.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Generic API response wrapper
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: ErrorDetail? = null,
    @SerializedName("meta") val meta: Meta? = null
)

data class ErrorDetail(
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("details") val details: List<FieldError>? = null
)

data class FieldError(
    @SerializedName("field") val field: String,
    @SerializedName("message") val message: String
)

data class Meta(
    @SerializedName("requestId") val requestId: String,
    @SerializedName("timestamp") val timestamp: String
)

data class PaginatedResponse<T>(
    @SerializedName("items") val items: List<T>,
    @SerializedName("pagination") val pagination: Pagination
)

data class Pagination(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("hasMore") val hasMore: Boolean
)

// Auth DTOs
data class SendOtpRequest(val phone: String, val channel: String = "sms")
data class VerifyOtpRequest(val phone: String, val otp: String, val role: String)
data class RefreshTokenRequest(val refreshToken: String)
data class OtpResponse(val expiresInSeconds: Int)
data class TokenResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("user") val user: UserDto
)

// User DTOs
data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("role") val role: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("photoUrl") val photoUrl: String?,
    @SerializedName("isVerified") val isVerified: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

data class MaidProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("photoUrl") val photoUrl: String?,
    @SerializedName("skills") val skills: List<String>,
    @SerializedName("experienceYears") val experienceYears: Int,
    @SerializedName("hourlyRate") val hourlyRate: Double,
    @SerializedName("languages") val languages: List<String>,
    @SerializedName("workType") val workType: String,
    @SerializedName("ratingAvg") val ratingAvg: Double,
    @SerializedName("ratingCount") val ratingCount: Int,
    @SerializedName("isVerifiedPhoto") val isVerifiedPhoto: Boolean,
    @SerializedName("isVerifiedId") val isVerifiedId: Boolean,
    @SerializedName("distanceKm") val distanceKm: Double?,
    @SerializedName("isOnline") val isOnline: Boolean
)

// Job DTOs
data class JobDto(
    @SerializedName("id") val id: String,
    @SerializedName("clientId") val clientId: String,
    @SerializedName("clientName") val clientName: String,
    @SerializedName("clientRating") val clientRating: Double,
    @SerializedName("jobType") val jobType: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("locationLat") val locationLat: Double,
    @SerializedName("locationLng") val locationLng: Double,
    @SerializedName("locationAddress") val locationAddress: String,
    @SerializedName("dateStart") val dateStart: String,
    @SerializedName("dateEnd") val dateEnd: String?,
    @SerializedName("budgetMin") val budgetMin: Double,
    @SerializedName("budgetMax") val budgetMax: Double,
    @SerializedName("budgetType") val budgetType: String,
    @SerializedName("status") val status: String,
    @SerializedName("applicantCount") val applicantCount: Int,
    @SerializedName("distanceKm") val distanceKm: Double?,
    @SerializedName("createdAt") val createdAt: String
)

data class CreateJobRequest(
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
    val budgetType: String
)

data class ApplyRequest(val message: String?)

// Booking DTOs
data class BookingDto(
    @SerializedName("id") val id: String,
    @SerializedName("clientId") val clientId: String,
    @SerializedName("clientName") val clientName: String,
    @SerializedName("maidId") val maidId: String,
    @SerializedName("maidName") val maidName: String,
    @SerializedName("jobId") val jobId: String?,
    @SerializedName("status") val status: String,
    @SerializedName("dateStart") val dateStart: String,
    @SerializedName("dateEnd") val dateEnd: String?,
    @SerializedName("agreedRate") val agreedRate: Double,
    @SerializedName("rateType") val rateType: String,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("createdAt") val createdAt: String
)

data class CreateBookingRequest(
    val maidId: String,
    val jobId: String?,
    val dateStart: String,
    val dateEnd: String?,
    val agreedRate: Double
)

data class CancelRequest(val reason: String)

// Message DTOs
data class ConversationDto(
    @SerializedName("id") val id: String,
    @SerializedName("participantId") val participantId: String,
    @SerializedName("participantName") val participantName: String,
    @SerializedName("isOnline") val isOnline: Boolean,
    @SerializedName("lastMessage") val lastMessage: MessageDto?,
    @SerializedName("unreadCount") val unreadCount: Int,
    @SerializedName("updatedAt") val updatedAt: String
)

data class MessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("conversationId") val conversationId: String,
    @SerializedName("senderId") val senderId: String,
    @SerializedName("receiverId") val receiverId: String,
    @SerializedName("type") val type: String,
    @SerializedName("content") val content: String,
    @SerializedName("isRead") val isRead: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

data class SendMessageRequest(val content: String, val type: String = "TEXT")
