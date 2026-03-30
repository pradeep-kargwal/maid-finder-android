package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.data.model.BudgetType
import com.maidfinder.app.data.model.Job
import com.maidfinder.app.data.model.JobType
import com.maidfinder.app.data.model.Location
import com.maidfinder.app.data.model.Shift
import com.maidfinder.app.data.model.ShiftType
import com.maidfinder.app.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PostJobUiState(
    val jobType: JobType = JobType.PART_TIME,
    val title: String = "",
    val description: String = "",
    val location: String = "Madhapur, Hyderabad",
    val dateStart: String = "2026-04-01",
    val dateEnd: String = "",
    val shiftType: ShiftType = ShiftType.MORNING,
    val shiftStart: String = "08:00",
    val shiftEnd: String = "10:00",
    val budgetMin: String = "100",
    val budgetMax: String = "200",
    val budgetType: BudgetType = BudgetType.HOURLY,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

class PostJobViewModel(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostJobUiState())
    val uiState: StateFlow<PostJobUiState> = _uiState.asStateFlow()

    fun updateJobType(type: JobType) {
        _uiState.value = _uiState.value.copy(jobType = type)
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun updateDateStart(date: String) {
        _uiState.value = _uiState.value.copy(dateStart = date)
    }

    fun updateDateEnd(date: String) {
        _uiState.value = _uiState.value.copy(dateEnd = date)
    }

    fun updateShiftType(type: ShiftType) {
        val (start, end) = when (type) {
            ShiftType.MORNING -> "06:00" to "10:00"
            ShiftType.AFTERNOON -> "12:00" to "16:00"
            ShiftType.EVENING -> "16:00" to "20:00"
            ShiftType.CUSTOM -> _uiState.value.shiftStart to _uiState.value.shiftEnd
        }
        _uiState.value = _uiState.value.copy(
            shiftType = type,
            shiftStart = start,
            shiftEnd = end
        )
    }

    fun updateBudgetMin(value: String) {
        _uiState.value = _uiState.value.copy(budgetMin = value)
    }

    fun updateBudgetMax(value: String) {
        _uiState.value = _uiState.value.copy(budgetMax = value)
    }

    fun updateBudgetType(type: BudgetType) {
        _uiState.value = _uiState.value.copy(budgetType = type)
    }

    fun submitJob() {
        val state = _uiState.value
        if (state.dateStart.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter a start date")
            return
        }
        if (state.budgetMin.isBlank() || state.budgetMax.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter budget range")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            try {
                val job = Job(
                    id = "",
                    clientId = "client_001",
                    clientName = "Current User",
                    jobType = state.jobType,
                    title = state.title.ifEmpty { "House ${state.jobType.name.lowercase().replace("_", "-")} help" },
                    description = state.description,
                    location = Location(17.3850, 78.4867, state.location, "Hyderabad"),
                    dateStart = state.dateStart,
                    dateEnd = state.dateEnd.ifEmpty { null },
                    shifts = listOf(Shift(state.shiftType, state.shiftStart, state.shiftEnd)),
                    budgetMin = state.budgetMin.toDoubleOrNull() ?: 100.0,
                    budgetMax = state.budgetMax.toDoubleOrNull() ?: 200.0,
                    budgetType = state.budgetType
                )
                jobRepository.createJob(job)
                _uiState.value = _uiState.value.copy(isSubmitting = false, isSubmitted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = e.message ?: "Failed to post job"
                )
            }
        }
    }

    class Factory(private val jobRepository: JobRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PostJobViewModel(jobRepository) as T
        }
    }
}
