package com.rafiq.presentation.screen.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import com.rafiq.presentation.theme.TextPrimary
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_arrow_left), contentDescription = "Back", tint = TextPrimary)
            }
            Text(text = "Messages", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(start = 8.dp))
        }

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
                contentPadding = PaddingValues(bottom = 24.dp)
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToChat(chat.id) }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar with Presence Dot
                        Box(modifier = Modifier.size(56.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
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
                                        tint = Color.White,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }

                            // Presence Indicator Dot
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(14.dp)
                                    .background(Color.White, CircleShape)
                                    .padding(2.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = chat.participantName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 17.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = chat.lastMessage,
                                color = if (chat.unreadCount > 0) TextPrimary else Color.Gray,
                                fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        if (chat.unreadCount > 0) {
                            Box(
                                modifier = Modifier.size(24.dp).clip(CircleShape).background(PrimaryAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = chat.unreadCount.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
