package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.repository.MaidFilters
import com.maidfinder.app.domain.repository.MaidRepository
import com.maidfinder.app.domain.usecase.SearchMaidsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaidListUiState(
    val maids: List<MaidProfile> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedWorkType: WorkType? = null,
    val verifiedOnly: Boolean = false
)

@HiltViewModel
class MaidListViewModel @Inject constructor(
    private val searchMaidsUseCase: SearchMaidsUseCase,
    private val maidRepository: MaidRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaidListUiState())
    val uiState: StateFlow<MaidListUiState> = _uiState.asStateFlow()

    init { loadMaids() }

    fun loadMaids() {
        viewModelScope.launch {
            searchMaidsUseCase(17.3850, 78.4867, 5.0, MaidFilters(
                workType = _uiState.value.selectedWorkType,
                verifiedOnly = _uiState.value.verifiedOnly
            )).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Success -> _uiState.value = _uiState.value.copy(maids = resource.data, isLoading = false)
                    is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = resource.message)
                }
            }
        }
    }

    fun setWorkTypeFilter(workType: WorkType?) {
        _uiState.value = _uiState.value.copy(selectedWorkType = workType); loadMaids()
    }

    fun setVerifiedOnly(verified: Boolean) {
        _uiState.value = _uiState.value.copy(verifiedOnly = verified); loadMaids()
    }

    fun toggleSaveMaid(maidId: String) {
        viewModelScope.launch {
            if (maidRepository.isMaidSaved(maidId)) maidRepository.removeMaid(maidId) else maidRepository.saveMaid(maidId)
        }
    }
}
