package com.maidfinder.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maidfinder.app.domain.model.Conversation
import com.maidfinder.app.domain.model.MessageType
import com.maidfinder.app.ui.components.*
import com.maidfinder.app.ui.theme.*
import com.maidfinder.app.ui.viewmodel.MessagesListViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessagesScreen(
    viewModel: MessagesListViewModel = hiltViewModel(),
    onConversationClick: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val filtered = viewModel.filteredConversations

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(value = uiState.searchQuery, onValueChange = { viewModel.updateSearch(it) },
            placeholder = { Text("Search conversations...") }, leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant))

        when {
            filtered.isEmpty() -> EmptyState(Icons.Default.ChatBubbleOutline, "No conversations yet", "Start chatting by booking or contacting a maid")
            else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(filtered, key = { it.id }) { conv ->
                    ConversationItem(conv) { viewModel.markAsRead(conv.id); onConversationClick(conv.id, conv.participantId) }
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (conversation.unreadCount > 0) BluePrimary.copy(0.04f) else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                AvatarInitials(name = conversation.participantName, size = 52.dp, backgroundColor = MaterialTheme.colorScheme.primaryContainer)
                if (conversation.isParticipantOnline) {
                    Box(Modifier.size(14.dp).align(Alignment.BottomEnd).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface).padding(2.dp)) {
                        AnimatedPulsingDot(color = Success, size = 10.dp)
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(conversation.participantName, fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Text(formatTimestamp(conversation.updatedAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (conversation.lastMessage?.type) {
                            MessageType.VOICE -> "\uD83C\uDFA4 Voice message"
                            MessageType.IMAGE -> "\uD83D\uDDBC\uFE0F Photo"
                            MessageType.SYSTEM -> "\u2139\uFE0F ${conversation.lastMessage?.content ?: ""}"
                            else -> conversation.lastMessage?.content ?: ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    if (conversation.unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(22.dp).clip(CircleShape).background(BluePrimary), contentAlignment = Alignment.Center) {
                            Text(conversation.unreadCount.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "now"; diff < 3600000 -> "${diff / 60000}m"; diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> "${diff / 86400000}d"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
