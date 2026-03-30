package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.data.model.MaidProfile
import com.maidfinder.app.data.model.Skill
import com.maidfinder.app.data.model.WorkType
import com.maidfinder.app.data.repository.MaidRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the maid list screen.
 */
data class MaidListUiState(
    val maids: List<MaidProfile> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedWorkType: WorkType? = null,
    val verifiedOnly: Boolean = false,
    val searchRadius: Double = 5.0
)

/**
 * ViewModel for the maid list/browse screen.
 */
class MaidListViewModel(
    private val maidRepository: MaidRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaidListUiState())
    val uiState: StateFlow<MaidListUiState> = _uiState.asStateFlow()

    init {
        loadMaids()
    }

    fun loadMaids() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val maids = maidRepository.getNearbyMaids(
                    latitude = 17.3850,
                    longitude = 78.4867,
                    radiusKm = _uiState.value.searchRadius,
                    workType = _uiState.value.selectedWorkType,
                    verifiedOnly = _uiState.value.verifiedOnly
                )
                _uiState.value = _uiState.value.copy(
                    maids = maids,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load maids"
                )
            }
        }
    }

    fun setWorkTypeFilter(workType: WorkType?) {
        _uiState.value = _uiState.value.copy(selectedWorkType = workType)
        loadMaids()
    }

    fun setVerifiedOnly(verified: Boolean) {
        _uiState.value = _uiState.value.copy(verifiedOnly = verified)
        loadMaids()
    }

    fun setRadius(radiusKm: Double) {
        _uiState.value = _uiState.value.copy(searchRadius = radiusKm)
        loadMaids()
    }

    fun toggleSaveMaid(maidId: String) {
        viewModelScope.launch {
            val isSaved = maidRepository.isMaidSaved(maidId)
            if (isSaved) {
                maidRepository.removeMaidFromBookmarks(maidId)
            } else {
                maidRepository.saveMaidToBookmarks(maidId)
            }
        }
    }

    class Factory(private val maidRepository: MaidRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MaidListViewModel(maidRepository) as T
        }
    }
}
