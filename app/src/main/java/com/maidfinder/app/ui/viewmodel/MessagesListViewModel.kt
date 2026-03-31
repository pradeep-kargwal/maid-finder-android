package com.maidfinder.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maidfinder.app.domain.model.Conversation
import com.maidfinder.app.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagesListUiState(
    val conversations: List<Conversation> = emptyList(),
    val searchQuery: String = ""
)

@HiltViewModel
class MessagesListViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesListUiState())
    val uiState: StateFlow<MessagesListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            messageRepository.getConversations().collect { convs ->
                _uiState.value = _uiState.value.copy(conversations = convs)
            }
        }
    }

    fun updateSearch(query: String) { _uiState.value = _uiState.value.copy(searchQuery = query) }

    fun markAsRead(conversationId: String) {
        viewModelScope.launch { messageRepository.markAsRead(conversationId) }
    }

    val filteredConversations: List<Conversation>
        get() {
            val q = _uiState.value.searchQuery
            return if (q.isBlank()) _uiState.value.conversations
            else _uiState.value.conversations.filter { it.participantName.contains(q, ignoreCase = true) }
        }
}
