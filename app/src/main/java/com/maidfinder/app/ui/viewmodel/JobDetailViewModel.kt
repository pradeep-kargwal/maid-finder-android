package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.data.model.Job
import com.maidfinder.app.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JobDetailUiState(
    val job: Job? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasApplied: Boolean = false,
    val isApplying: Boolean = false,
    val applySuccess: Boolean = false
)

class JobDetailViewModel(
    private val jobRepository: JobRepository,
    private val jobId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()

    init {
        loadJob()
    }

    private fun loadJob() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val job = jobRepository.getJobById(jobId)
                _uiState.value = _uiState.value.copy(job = job, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load job"
                )
            }
        }
    }

    fun applyToJob() {
        if (_uiState.value.hasApplied) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true)
            try {
                // Simulate application submission
                kotlinx.coroutines.delay(500)
                _uiState.value = _uiState.value.copy(
                    isApplying = false,
                    hasApplied = true,
                    applySuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isApplying = false,
                    errorMessage = e.message ?: "Failed to apply"
                )
            }
        }
    }

    fun expressInterest() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            _uiState.value = _uiState.value.copy(applySuccess = true)
        }
    }

    class Factory(
        private val jobRepository: JobRepository,
        private val jobId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JobDetailViewModel(jobRepository, jobId) as T
        }
    }
}
