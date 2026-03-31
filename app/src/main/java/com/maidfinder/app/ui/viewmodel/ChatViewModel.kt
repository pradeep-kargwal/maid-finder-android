package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.domain.model.*
import com.maidfinder.app.domain.repository.AuthRepository
import com.maidfinder.app.domain.repository.MessageRepository
import com.maidfinder.app.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val conversationId: String = savedStateHandle["conversationId"] ?: ""
    val participantName: String = savedStateHandle["participantName"] ?: ""
    val participantId: String = savedStateHandle["participantId"] ?: ""

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val currentUserId: String get() = authRepository.currentUserId

    init { loadMessages() }

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val messages = messageRepository.getMessages(conversationId)
            _uiState.value = _uiState.value.copy(messages = messages, isLoading = false)
            messageRepository.markAsRead(conversationId)
        }
    }

    fun updateInput(text: String) { _uiState.value = _uiState.value.copy(inputText = text) }

    fun sendMessage() {
        val text = _uiState.value.inputText
        if (text.isBlank()) return
        viewModelScope.launch {
            sendMessageUseCase(conversationId, currentUserId, participantId, text).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + it,
                        inputText = ""
                    )
                },
                onFailure = {}
            )
        }
    }
}
