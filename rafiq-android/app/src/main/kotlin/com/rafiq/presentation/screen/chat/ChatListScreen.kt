package com.rafiq.presentation.screen.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rafiq.domain.model.Chat
import com.rafiq.presentation.theme.BackgroundPrimary
import com.rafiq.presentation.theme.BackgroundSecondary
import com.rafiq.presentation.theme.BorderLight
import com.rafiq.presentation.theme.TextPrimary
import com.rafiq.presentation.theme.TextTertiary
import com.rafiq.presentation.theme.PrimaryAccent

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatListScreen(
    chats: List<Chat> = emptyList(),
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    onNavigateToChat: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = isLoading, onRefresh = onRefresh)
    var searchQuery by remember { mutableStateOf("") }

    val filteredChats = remember(chats, searchQuery) {
        if (searchQuery.isBlank()) chats
        else chats.filter {
            it.participantName.contains(searchQuery, ignoreCase = true) ||
            it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundPrimary)
    ) {
        // Chat Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("Search messages...", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_search),
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                AnimatedVisibility(visible = searchQuery.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x), contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryAccent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredChats.isEmpty() && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No conversations matching '$searchQuery'" else "No chats yet. Start a conversation!",
                                color = Color.Gray,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                items(filteredChats) { chat ->
                    Card(
                        onClick = { onNavigateToChat(chat.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundSecondary),
                        border = BorderStroke(1.dp, BorderLight),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(58.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryAccent.copy(alpha = 0.08f))
                                ) {
                                    if (!chat.participantAvatar.isNullOrBlank()) {
                                        AsyncImage(
                                            model = chat.participantAvatar,
                                            contentDescription = chat.participantName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                                            contentDescription = null,
                                            tint = PrimaryAccent,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = chat.participantName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 17.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = chat.lastMessage,
                                    color = if (chat.unreadCount > 0) TextPrimary else TextTertiary,
                                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }

                            if (chat.unreadCount > 0) {
                                Box(
                                    modifier = Modifier.size(26.dp).clip(CircleShape).background(PrimaryAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = chat.unreadCount.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = PrimaryAccent
            )
        }
    }
}
