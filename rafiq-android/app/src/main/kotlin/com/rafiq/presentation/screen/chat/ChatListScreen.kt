package com.rafiq.presentation.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.domain.model.Chat
import com.rafiq.presentation.theme.BackgroundPrimary
import com.rafiq.presentation.theme.TextPrimary
import com.rafiq.presentation.theme.PrimaryAccent

@Composable
fun ChatListScreen(
    chats: List<Chat>,
    onChatClicked: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundPrimary)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(text = "Messages", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(chats) { chat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChatClicked(chat.id) }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(PrimaryAccent)) // Placeholder avatar
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = chat.participantName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                        Text(text = chat.lastMessage, color = TextPrimary, fontSize = 14.sp, maxLines = 1)
                    }
                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier.size(24.dp).clip(CircleShape).background(PrimaryAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = chat.unreadCount.toString(), color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
