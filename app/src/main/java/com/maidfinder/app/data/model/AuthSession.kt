package com.maidfinder.app.data.model

data class AuthSession(
    val userId: String,
    val phone: String,
    val role: UserRole,
    val displayName: String,
    val isLoggedIn: Boolean = false,
    val isDemo: Boolean = false,
    val token: String = ""
)

data class OtpRequest(
    val phone: String,
    val expiresInSeconds: Int = 300
)

data class OtpVerification(
    val phone: String,
    val otp: String,
    val role: UserRole
)
