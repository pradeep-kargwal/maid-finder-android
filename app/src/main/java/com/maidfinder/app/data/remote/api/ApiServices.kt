package com.maidfinder.app.data.remote.api

import com.maidfinder.app.data.remote.dto.*
import retrofit2.http.*

interface AuthApiService {
    @POST("api/v1/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): ApiResponse<OtpResponse>

    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): ApiResponse<TokenResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResponse<TokenResponse>
}

interface UserApiService {
    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): ApiResponse<UserDto>

    @PUT("api/v1/users/me")
    suspend fun updateProfile(@Body request: Map<String, @JvmSuppressWildcards Any>): ApiResponse<UserDto>

    @GET("api/v1/maids/search")
    suspend fun searchMaids(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radiusKm: Int,
        @Query("workType") workType: String? = null,
        @Query("minRate") minRate: Double? = null,
        @Query("maxRate") maxRate: Double? = null,
        @Query("minExperience") minExperience: Int? = null,
        @Query("languages") languages: String? = null,
        @Query("minRating") minRating: Double? = null,
        @Query("verifiedOnly") verifiedOnly: Boolean? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<PaginatedResponse<MaidProfileDto>>

    @GET("api/v1/maids/{id}")
    suspend fun getMaidById(@Path("id") maidId: String): ApiResponse<MaidProfileDto>

    @POST("api/v1/maids/{id}/save")
    suspend fun saveMaid(@Path("id") maidId: String): ApiResponse<Unit>

    @DELETE("api/v1/maids/{id}/save")
    suspend fun removeMaid(@Path("id") maidId: String): ApiResponse<Unit>
}

interface JobApiService {
    @GET("api/v1/jobs")
    suspend fun getJobs(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("type") type: String? = null,
        @Query("minBudget") minBudget: Double? = null,
        @Query("maxBudget") maxBudget: Double? = null,
        @Query("radius") radius: Int = 5,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<PaginatedResponse<JobDto>>

    @GET("api/v1/jobs/{id}")
    suspend fun getJobById(@Path("id") jobId: String): ApiResponse<JobDto>

    @POST("api/v1/jobs")
    suspend fun createJob(@Body request: CreateJobRequest): ApiResponse<JobDto>

    @POST("api/v1/jobs/{id}/apply")
    suspend fun applyToJob(@Path("id") jobId: String, @Body request: ApplyRequest): ApiResponse<Unit>

    @GET("api/v1/jobs/my")
    suspend fun getMyJobs(): ApiResponse<List<JobDto>>
}

interface BookingApiService {
    @GET("api/v1/bookings")
    suspend fun getBookings(@Query("status") status: String? = null): ApiResponse<List<BookingDto>>

    @POST("api/v1/bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): ApiResponse<BookingDto>

    @PUT("api/v1/bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") bookingId: String, @Body request: CancelRequest): ApiResponse<Unit>

    @PUT("api/v1/bookings/{id}/complete")
    suspend fun completeBooking(@Path("id") bookingId: String): ApiResponse<Unit>
}

interface MessageApiService {
    @GET("api/v1/chats")
    suspend fun getConversations(): ApiResponse<List<ConversationDto>>

    @GET("api/v1/chats/{id}/messages")
    suspend fun getMessages(@Path("id") conversationId: String, @Query("before") before: String? = null): ApiResponse<List<MessageDto>>

    @POST("api/v1/chats/{id}/messages")
    suspend fun sendMessage(@Path("id") conversationId: String, @Body request: SendMessageRequest): ApiResponse<MessageDto>
}
