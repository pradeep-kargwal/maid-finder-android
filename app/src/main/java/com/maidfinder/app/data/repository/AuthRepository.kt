package com.maidfinder.app.data.repository

import com.maidfinder.app.data.model.AuthSession
import com.maidfinder.app.data.model.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository {
    private val _session = MutableStateFlow<AuthSession?>(null)
    val session: StateFlow<AuthSession?> = _session.asStateFlow()

    private val demoClient = AuthSession(
        userId = "demo_client_001",
        phone = "+919876543210",
        role = UserRole.CLIENT,
        displayName = "Priya M.",
        isLoggedIn = true,
        isDemo = true,
        token = "demo_token_client"
    )

    private val demoMaid = AuthSession(
        userId = "maid_001",
        phone = "+919876543211",
        role = UserRole.MAID,
        displayName = "Lakshmi R.",
        isLoggedIn = true,
        isDemo = true,
        token = "demo_token_maid"
    )

    val isLoggedIn: Boolean get() = _session.value?.isLoggedIn == true
    val currentUserId: String get() = _session.value?.userId ?: ""
    val currentRole: UserRole get() = _session.value?.role ?: UserRole.CLIENT

    suspend fun sendOtp(phone: String): Boolean {
        delay(1500)
        return phone.length >= 10
    }

    suspend fun verifyOtp(phone: String, otp: String, role: UserRole): AuthSession {
        delay(1000)
        val session = AuthSession(
            userId = "user_${phone.takeLast(4)}",
            phone = phone,
            role = role,
            displayName = if (role == UserRole.CLIENT) "Client User" else "Maid User",
            isLoggedIn = true,
            isDemo = false,
            token = "token_${System.currentTimeMillis()}"
        )
        _session.value = session
        return session
    }

    fun loginDemo(role: UserRole) {
        _session.value = if (role == UserRole.CLIENT) demoClient else demoMaid
    }

    fun switchDemoRole() {
        val current = _session.value
        if (current?.isDemo == true) {
            _session.value = if (current.role == UserRole.CLIENT) demoMaid else demoClient
        }
    }

    fun logout() {
        _session.value = null
    }
}
