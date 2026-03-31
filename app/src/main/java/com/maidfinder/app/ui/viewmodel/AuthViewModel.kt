package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.domain.model.AuthSession
import com.maidfinder.app.domain.model.UserRole
import com.maidfinder.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val session: AuthSession? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

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

    fun loginDemo(role: UserRole) = authRepository.loginDemo(role)
    fun switchDemoRole() = authRepository.switchDemoRole()
    fun logout() = authRepository.logout()

    fun sendOtp(phone: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.sendOtp(phone).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false); onComplete(true) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message); onComplete(false) }
            )
        }
    }

    fun verifyOtp(phone: String, otp: String, role: UserRole) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.verifyOtp(phone, otp, role).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message) }
            )
        }
    }
}
