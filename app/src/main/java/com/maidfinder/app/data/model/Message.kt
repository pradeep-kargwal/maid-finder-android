package com.maidfinder.app.data.model

data class Conversation(
    val id: String,
    val participantId: String,
    val participantName: String,
    val participantPhotoUrl: String? = null,
    val isParticipantOnline: Boolean = false,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val receiverId: String,
    val type: MessageType = MessageType.TEXT,
    val content: String = "",
    val voiceDurationSeconds: Int = 0,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MessageType {
    TEXT, VOICE, IMAGE, SYSTEM, BOOKING_CARD
}
