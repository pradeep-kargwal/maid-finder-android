package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.data.model.AuthSession
import com.maidfinder.app.data.model.UserRole
import com.maidfinder.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val session: AuthSession? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.session.collect { session ->
                _uiState.value = _uiState.value.copy(session = session)
            }
        }
    }

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn

    fun loginDemo(role: UserRole) {
        authRepository.loginDemo(role)
    }

    fun switchDemoRole() {
        authRepository.switchDemoRole()
    }

    fun logout() {
        authRepository.logout()
    }

    fun sendOtp(phone: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val success = authRepository.sendOtp(phone)
            _uiState.value = _uiState.value.copy(isLoading = false)
            onComplete(success)
        }
    }

    fun verifyOtp(phone: String, otp: String, role: UserRole) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                authRepository.verifyOtp(phone, otp, role)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
