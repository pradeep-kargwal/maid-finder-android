package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.repository.AuthRepository
import com.maidfinder.app.domain.repository.BookingRepository
import com.maidfinder.app.domain.usecase.GetBookingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingsListUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0
)

@HiltViewModel
class BookingsListViewModel @Inject constructor(
    private val getBookingsUseCase: GetBookingsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingsListUiState())
    val uiState: StateFlow<BookingsListUiState> = _uiState.asStateFlow()

    init { loadBookings(0) }

    fun loadBookings(tab: Int) {
        val status = when (tab) { 1 -> BookingStatus.COMPLETED; 2 -> BookingStatus.CANCELLED; else -> null }
        val isClient = authRepository.currentRole == UserRole.CLIENT
        viewModelScope.launch {
            getBookingsUseCase(authRepository.currentUserId, isClient, status).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value = _uiState.value.copy(bookings = resource.data, isLoading = false, selectedTab = tab)
                    is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    val isClient: Boolean get() = authRepository.currentRole == UserRole.CLIENT
}
