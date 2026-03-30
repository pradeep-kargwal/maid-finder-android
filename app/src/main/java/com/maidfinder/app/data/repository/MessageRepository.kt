package com.maidfinder.app.data.repository

import com.maidfinder.app.data.model.Conversation
import com.maidfinder.app.data.model.Message
import com.maidfinder.app.data.model.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class MessageRepository {
    private val conversations = MutableStateFlow(sampleConversations())
    private val messagesMap = mutableMapOf<String, MutableList<Message>>()

    init {
        sampleConversations().forEach { conv ->
            messagesMap[conv.id] = sampleMessages(conv.id).toMutableList()
        }
    }

    fun getConversations(): StateFlow<List<Conversation>> = conversations.asStateFlow()

    suspend fun getMessages(conversationId: String): List<Message> = withContext(Dispatchers.IO) {
        delay(300)
        messagesMap[conversationId]?.toList() ?: emptyList()
    }

    suspend fun sendMessage(conversationId: String, senderId: String, receiverId: String, content: String, type: MessageType = MessageType.TEXT): Message = withContext(Dispatchers.IO) {
        delay(200)
        val message = Message(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            conversationId = conversationId,
            senderId = senderId,
            receiverId = receiverId,
            type = type,
            content = content
        )
        messagesMap.getOrPut(conversationId) { mutableListOf() }.add(message)
        val updated = conversations.value.map {
            if (it.id == conversationId) it.copy(lastMessage = message, updatedAt = System.currentTimeMillis()) else it
        }
        conversations.value = updated.sortedByDescending { it.updatedAt }
        message
    }

    fun markAsRead(conversationId: String) {
        messagesMap[conversationId]?.replaceAll { if (!it.isRead) it.copy(isRead = true) else it }
        val updated = conversations.value.map {
            if (it.id == conversationId) it.copy(unreadCount = 0) else it
        }
        conversations.value = updated
    }

    private fun sampleConversations() = listOf(
        Conversation("conv_001", "maid_001", "Lakshmi R.", isParticipantOnline = true,
            lastMessage = Message("m1", "conv_001", "maid_001", "client_001", MessageType.TEXT, "I'll be there at 8 AM tomorrow!"),
            unreadCount = 2, updatedAt = System.currentTimeMillis() - 300000),
        Conversation("conv_002", "maid_002", "Sunita K.", isParticipantOnline = false,
            lastMessage = Message("m2", "conv_002", "client_001", "maid_002", MessageType.TEXT, "Thank you, see you then"),
            unreadCount = 0, updatedAt = System.currentTimeMillis() - 3600000),
        Conversation("conv_003", "maid_004", "Anita D.", isParticipantOnline = true,
            lastMessage = Message("m3", "conv_003", "maid_004", "client_001", MessageType.TEXT, "Is the job still available?"),
            unreadCount = 1, updatedAt = System.currentTimeMillis() - 7200000)
    )

    private fun sampleMessages(conversationId: String) = listOf(
        Message("msg_001", conversationId, "client_001", "maid_001", MessageType.TEXT, "Hi! Are you available for cleaning tomorrow?"),
        Message("msg_002", conversationId, "maid_001", "client_001", MessageType.TEXT, "Yes, I'm available. What time works for you?"),
        Message("msg_003", conversationId, "client_001", "maid_001", MessageType.TEXT, "How about 8 AM?"),
        Message("msg_004", conversationId, "maid_001", "client_001", MessageType.TEXT, "I'll be there at 8 AM tomorrow!"),
        Message("msg_005", conversationId, "client_001", "maid_001", MessageType.SYSTEM, "Booking confirmed for April 1, 8:00 AM")
    )
}
