package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.data.model.Booking
import com.maidfinder.app.data.model.BudgetType
import com.maidfinder.app.data.model.MaidProfile
import com.maidfinder.app.data.repository.BookingRepository
import com.maidfinder.app.data.repository.MaidRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class BookingViewModel(
    private val maidRepository: MaidRepository,
    private val bookingRepository: BookingRepository,
    private val maidId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        loadMaid()
    }

    private fun loadMaid() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val maid = maidRepository.getMaidById(maidId)
                val rate = maid?.hourlyRate ?: 150.0
                _uiState.value = _uiState.value.copy(
                    maid = maid,
                    agreedRate = rate,
                    totalAmount = rate * 30, // estimate 30 hours/month
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load"
                )
            }
        }
    }

    fun updateDateStart(date: String) {
        _uiState.value = _uiState.value.copy(dateStart = date)
    }

    fun updateDateEnd(date: String) {
        _uiState.value = _uiState.value.copy(dateEnd = date)
    }

    fun confirmBooking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBooking = true)
            try {
                val state = _uiState.value
                val booking = Booking(
                    id = "",
                    clientId = "client_001",
                    clientName = "Current User",
                    maidId = maidId,
                    maidName = state.maid?.displayName ?: "Maid",
                    dateStart = state.dateStart,
                    dateEnd = state.dateEnd,
                    agreedRate = state.agreedRate,
                    rateType = BudgetType.HOURLY,
                    totalAmount = state.totalAmount
                )
                bookingRepository.createBooking(booking)
                _uiState.value = _uiState.value.copy(isBooking = false, isBooked = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBooking = false,
                    errorMessage = e.message ?: "Failed to create booking"
                )
            }
        }
    }

    class Factory(
        private val maidRepository: MaidRepository,
        private val bookingRepository: BookingRepository,
        private val maidId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BookingViewModel(maidRepository, bookingRepository, maidId) as T
        }
    }
}
