package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.repository.JobFilters
import com.maidfinder.app.domain.usecase.GetJobsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobFeedUiState(
    val jobs: List<Job> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedJobType: JobType? = null
)

@HiltViewModel
class JobFeedViewModel @Inject constructor(
    private val getJobsUseCase: GetJobsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobFeedUiState())
    val uiState: StateFlow<JobFeedUiState> = _uiState.asStateFlow()

    init { loadJobs() }

    fun loadJobs() {
        viewModelScope.launch {
            getJobsUseCase(filters = JobFilters(jobType = _uiState.value.selectedJobType)).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value = _uiState.value.copy(jobs = resource.data, isLoading = false)
                    is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    fun setJobTypeFilter(jobType: JobType?) {
        _uiState.value = _uiState.value.copy(selectedJobType = jobType); loadJobs()
    }
}
