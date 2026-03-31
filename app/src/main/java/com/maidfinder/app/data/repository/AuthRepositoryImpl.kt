package com.maidfinder.app.data.repository

import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val _session = MutableStateFlow<AuthSession?>(null)
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()

    private val demoClient = AuthSession("demo_client_001", "+919876543210", UserRole.CLIENT, "Priya M.", isDemo = true)
    private val demoMaid = AuthSession("maid_001", "+919876543211", UserRole.MAID, "Lakshmi R.", isDemo = true)

    override val isLoggedIn: Boolean get() = _session.value?.isLoggedIn == true
    override val currentUserId: String get() = _session.value?.userId ?: ""
    override val currentRole: UserRole get() = _session.value?.role ?: UserRole.CLIENT

    override suspend fun sendOtp(phone: String): Result<Boolean> {
        delay(1500)
        return if (phone.length >= 10) Result.success(true) else Result.failure(Exception("Invalid phone"))
    }

    override suspend fun verifyOtp(phone: String, otp: String, role: UserRole): Result<AuthSession> {
        delay(1000)
        val session = AuthSession("user_${phone.takeLast(4)}", phone, role, if (role == UserRole.CLIENT) "Client" else "Maid")
        _session.value = session
        return Result.success(session)
    }

    override fun loginDemo(role: UserRole) { _session.value = if (role == UserRole.CLIENT) demoClient else demoMaid }
    override fun switchDemoRole() {
        val current = _session.value
        if (current?.isDemo == true) _session.value = if (current.role == UserRole.CLIENT) demoMaid else demoClient
    }
    override fun logout() { _session.value = null }
}
