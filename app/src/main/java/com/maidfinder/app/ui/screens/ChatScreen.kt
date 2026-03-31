package com.maidfinder.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maidfinder.app.domain.model.Message
import com.maidfinder.app.domain.model.MessageType
import com.maidfinder.app.ui.components.AvatarInitials
import com.maidfinder.app.ui.components.AnimatedPulsingDot
import com.maidfinder.app.ui.theme.*
import com.maidfinder.app.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    isOnline: Boolean = false,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box { AvatarInitials(name = viewModel.participantName, size = 36.dp) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(viewModel.participantName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(if (isOnline) "Online" else "Offline", fontSize = 12.sp,
                                color = if (isOnline) Success else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.Phone, "Call") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) { Icon(Icons.Default.Mic, "Voice", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    OutlinedTextField(value = uiState.inputText, onValueChange = { viewModel.updateInput(it) },
                        placeholder = { Text("Type a message...") }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp), singleLine = false, maxLines = 4)
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(onClick = { viewModel.sendMessage() }, modifier = Modifier.size(48.dp), containerColor = BluePrimary) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(uiState.messages, key = { it.id }) { MessageBubble(it, it.senderId == viewModel.currentUserId) }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isMine: Boolean) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
        if (message.type == MessageType.SYSTEM) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(message.content, Modifier.padding(16.dp, 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Surface(shape = RoundedCornerShape(16.dp, 16.dp, if (isMine) 16.dp else 4.dp, if (isMine) 4.dp else 16.dp),
                color = if (isMine) BluePrimary else MaterialTheme.colorScheme.surfaceVariant, shadowElevation = 1.dp) {
                Column(Modifier.padding(14.dp, 10.dp)) {
                    Text(message.content, color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMine) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End))
                }
            }
        }
    }
}
