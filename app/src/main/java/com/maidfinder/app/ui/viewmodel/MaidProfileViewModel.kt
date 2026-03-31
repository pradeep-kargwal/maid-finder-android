package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.domain.model.MaidProfile
import com.maidfinder.app.domain.model.Resource
import com.maidfinder.app.domain.repository.MaidRepository
import com.maidfinder.app.domain.usecase.GetMaidProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaidProfileUiState(
    val maid: MaidProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class MaidProfileViewModel @Inject constructor(
    private val getMaidProfileUseCase: GetMaidProfileUseCase,
    private val maidRepository: MaidRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val maidId: String = savedStateHandle["maidId"] ?: ""
    private val _uiState = MutableStateFlow(MaidProfileUiState())
    val uiState: StateFlow<MaidProfileUiState> = _uiState.asStateFlow()

    init { loadMaid() }

    private fun loadMaid() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = getMaidProfileUseCase(maidId)) {
                is Resource.Success -> {
                    val saved = maidRepository.isMaidSaved(maidId)
                    _uiState.value = _uiState.value.copy(maid = result.data, isLoading = false, isSaved = saved)
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleSave() {
        viewModelScope.launch {
            val currentlySaved = maidRepository.isMaidSaved(maidId)
            if (currentlySaved) maidRepository.removeMaid(maidId) else maidRepository.saveMaid(maidId)
            _uiState.value = _uiState.value.copy(isSaved = !currentlySaved)
        }
    }
}
