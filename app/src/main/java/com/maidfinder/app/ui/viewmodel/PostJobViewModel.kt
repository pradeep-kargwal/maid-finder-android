package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.usecase.CreateJobUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostJobUiState(
    val jobType: JobType = JobType.PART_TIME,
    val title: String = "",
    val description: String = "",
    val location: String = "Madhapur, Hyderabad",
    val dateStart: String = "2026-04-01",
    val dateEnd: String = "",
    val shiftType: ShiftType = ShiftType.MORNING,
    val shiftStart: String = "06:00",
    val shiftEnd: String = "10:00",
    val budgetMin: String = "100",
    val budgetMax: String = "200",
    val budgetType: BudgetType = BudgetType.HOURLY,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PostJobViewModel @Inject constructor(
    private val createJobUseCase: CreateJobUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostJobUiState())
    val uiState: StateFlow<PostJobUiState> = _uiState.asStateFlow()

    fun updateJobType(type: JobType) { _uiState.value = _uiState.value.copy(jobType = type) }
    fun updateTitle(title: String) { _uiState.value = _uiState.value.copy(title = title) }
    fun updateDescription(desc: String) { _uiState.value = _uiState.value.copy(description = desc) }
    fun updateDateStart(date: String) { _uiState.value = _uiState.value.copy(dateStart = date) }
    fun updateDateEnd(date: String) { _uiState.value = _uiState.value.copy(dateEnd = date) }
    fun updateBudgetMin(v: String) { _uiState.value = _uiState.value.copy(budgetMin = v) }
    fun updateBudgetMax(v: String) { _uiState.value = _uiState.value.copy(budgetMax = v) }
    fun updateBudgetType(type: BudgetType) { _uiState.value = _uiState.value.copy(budgetType = type) }

    fun updateShiftType(type: ShiftType) {
        val (start, end) = when (type) {
            ShiftType.MORNING -> "06:00" to "10:00"
            ShiftType.AFTERNOON -> "12:00" to "16:00"
            ShiftType.EVENING -> "16:00" to "20:00"
            ShiftType.CUSTOM -> _uiState.value.shiftStart to _uiState.value.shiftEnd
        }
        _uiState.value = _uiState.value.copy(shiftType = type, shiftStart = start, shiftEnd = end)
    }

    fun submitJob() {
        val s = _uiState.value
        if (s.dateStart.isBlank()) { _uiState.value = s.copy(errorMessage = "Enter a start date"); return }
        viewModelScope.launch {
            _uiState.value = s.copy(isSubmitting = true, errorMessage = null)
            val job = Job(
                id = "", clientId = "demo_client_001", clientName = "Current User",
                jobType = s.jobType, title = s.title.ifEmpty { "House cleaning" },
                description = s.description,
                location = Location(17.3850, 78.4867, s.location),
                dateStart = s.dateStart, dateEnd = s.dateEnd.ifEmpty { null },
                shifts = listOf(Shift(s.shiftType, s.shiftStart, s.shiftEnd)),
                budgetMin = s.budgetMin.toDoubleOrNull() ?: 100.0,
                budgetMax = s.budgetMax.toDoubleOrNull() ?: 200.0,
                budgetType = s.budgetType
            )
            createJobUseCase(job).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isSubmitting = false, isSubmitted = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = it.message) }
            )
        }
    }
}
