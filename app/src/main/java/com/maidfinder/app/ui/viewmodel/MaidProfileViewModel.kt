package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.data.model.MaidProfile
import com.maidfinder.app.data.repository.MaidRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MaidProfileUiState(
    val maid: MaidProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class MaidProfileViewModel(
    private val maidRepository: MaidRepository,
    private val maidId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaidProfileUiState())
    val uiState: StateFlow<MaidProfileUiState> = _uiState.asStateFlow()

    init {
        loadMaid()
    }

    private fun loadMaid() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val maid = maidRepository.getMaidById(maidId)
                val saved = maidRepository.isMaidSaved(maidId)
                _uiState.value = _uiState.value.copy(
                    maid = maid,
                    isLoading = false,
                    isSaved = saved
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun toggleSave() {
        viewModelScope.launch {
            val currentlySaved = maidRepository.isMaidSaved(maidId)
            if (currentlySaved) {
                maidRepository.removeMaidFromBookmarks(maidId)
            } else {
                maidRepository.saveMaidToBookmarks(maidId)
            }
            _uiState.value = _uiState.value.copy(isSaved = !currentlySaved)
        }
    }

    class Factory(
        private val maidRepository: MaidRepository,
        private val maidId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MaidProfileViewModel(maidRepository, maidId) as T
        }
    }
}
