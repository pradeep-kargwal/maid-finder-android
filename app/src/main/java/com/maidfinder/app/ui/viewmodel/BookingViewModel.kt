package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.usecase.CreateBookingUseCase
import com.maidfinder.app.domain.usecase.GetMaidProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingUiState(
    val maid: MaidProfile? = null,
    val dateStart: String = "2026-04-01",
    val dateEnd: String = "2026-04-30",
    val agreedRate: Double = 0.0,
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val isBooking: Boolean = false,
    val isBooked: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getMaidProfileUseCase: GetMaidProfileUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val maidId: String = savedStateHandle["maidId"] ?: ""
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = getMaidProfileUseCase(maidId)) {
                is Resource.Success -> {
                    val rate = result.data.hourlyRate
                    _uiState.value = _uiState.value.copy(maid = result.data, agreedRate = rate, totalAmount = rate * 30, isLoading = false)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun updateDateStart(date: String) { _uiState.value = _uiState.value.copy(dateStart = date) }
    fun updateDateEnd(date: String) { _uiState.value = _uiState.value.copy(dateEnd = date) }

    fun confirmBooking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBooking = true)
            val s = _uiState.value
            val booking = Booking(
                id = "", clientId = "demo_client_001", clientName = "Current User",
                maidId = maidId, maidName = s.maid?.displayName ?: "Maid",
                dateStart = s.dateStart, dateEnd = s.dateEnd,
                agreedRate = s.agreedRate, rateType = BudgetType.HOURLY, totalAmount = s.totalAmount
            )
            createBookingUseCase(booking).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isBooking = false, isBooked = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isBooking = false, errorMessage = it.message) }
            )
        }
    }
}
