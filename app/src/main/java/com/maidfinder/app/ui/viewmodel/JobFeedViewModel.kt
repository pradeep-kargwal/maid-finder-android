package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.data.model.Job
import com.maidfinder.app.data.model.JobType
import com.maidfinder.app.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the job feed screen (maid perspective).
 */
data class JobFeedUiState(
    val jobs: List<Job> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedJobType: JobType? = null
)

/**
 * ViewModel for the maid's job feed screen.
 */
class JobFeedViewModel(
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobFeedUiState())
    val uiState: StateFlow<JobFeedUiState> = _uiState.asStateFlow()

    init {
        loadJobs()
    }

    fun loadJobs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val jobs = jobRepository.getJobs(
                    jobType = _uiState.value.selectedJobType
                )
                _uiState.value = _uiState.value.copy(
                    jobs = jobs,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load jobs"
                )
            }
        }
    }

    fun setJobTypeFilter(jobType: JobType?) {
        _uiState.value = _uiState.value.copy(selectedJobType = jobType)
        loadJobs()
    }

    class Factory(private val jobRepository: JobRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JobFeedViewModel(jobRepository) as T
        }
    }
}
