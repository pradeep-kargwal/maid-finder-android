package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.repository.JobRepository
import com.maidfinder.app.domain.usecase.ApplyToJobUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobDetailUiState(
    val job: Job? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasApplied: Boolean = false,
    val isApplying: Boolean = false,
    val applySuccess: Boolean = false
)

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val applyToJobUseCase: ApplyToJobUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val jobId: String = savedStateHandle["jobId"] ?: ""
    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = jobRepository.getJobById(jobId)) {
                is Resource.Success -> _uiState.value = _uiState.value.copy(job = result.data, isLoading = false)
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun applyToJob() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true)
            applyToJobUseCase(jobId).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isApplying = false, hasApplied = true, applySuccess = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isApplying = false, errorMessage = it.message) }
            )
        }
    }

    fun expressInterest() {
        _uiState.value = _uiState.value.copy(applySuccess = true)
    }
}
