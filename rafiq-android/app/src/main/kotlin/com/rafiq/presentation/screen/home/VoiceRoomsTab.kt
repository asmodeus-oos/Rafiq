package com.rafiq.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafiq.domain.model.VoiceRoom
import com.rafiq.domain.model.RoomType
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.theme.TextPrimary

@Composable
fun VoiceRoomsTab(
    viewModel: HomeFeedViewModel = hiltViewModel(),
    onJoinRoom: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val rooms = state.trendingRooms

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8FB)),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Live Rooms",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = TextPrimary
            )
        }

        if (rooms.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_mic_off),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active rooms right now", color = Color.Gray, fontSize = 16.sp)
                        Text("Be the first to start one!", color = PrimaryAccent, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(rooms) { room ->
                VoiceRoomCard(room = room, onClick = { onJoinRoom(room.id) })
            }
        }
    }
}

@Composable
fun VoiceRoomCard(room: VoiceRoom, onClick: () -> Unit) {
    val roomColor = when (room.type) {
        RoomType.DATING -> Color(0xFFE91E63)
        RoomType.GAMING -> Color(0xFF9C27B0)
        RoomType.MUSIC -> Color(0xFF3F51B5)
        RoomType.PODCAST -> Color(0xFF009688)
        RoomType.VIP -> Color(0xFFFFD700)
        else -> PrimaryAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Room icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(roomColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_mic),
                    contentDescription = null,
                    tint = roomColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(room.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    room.description.takeIf { it.isNotBlank() } ?: "Public room",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Listeners row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_users),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${room.participantCount} listening", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    // Live indicator
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${room.activeSpeakers} speaking", color = Color(0xFF4CAF50), fontSize = 12.sp)
                }
            }

            // Join button
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                color = roomColor.copy(alpha = 0.12f)
            ) {
                Text(
                    "Join",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = roomColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
