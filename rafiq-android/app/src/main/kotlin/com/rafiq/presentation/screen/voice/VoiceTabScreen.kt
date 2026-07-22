package com.rafiq.presentation.screen.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafiq.domain.model.RoomType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceTabScreen(
    onNavigateToRoom: (String) -> Unit,
    viewModel: VoiceRoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Quick Actions Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickMatchCard(
                title = "Voice Roulette",
                subtitle = "Random 1v1",
                icon = com.composables.icons.lucide.R.drawable.lucide_ic_shuffle,
                color = Color(0xFFF43F5E),
                onClick = { viewModel.startVoiceMatch() },
                modifier = Modifier.weight(1f)
            )
            QuickMatchCard(
                title = "Blind Date",
                subtitle = "Match by Voice",
                icon = com.composables.icons.lucide.R.drawable.lucide_ic_heart,
                color = Color(0xFF8B5CF6),
                onClick = { viewModel.startVoiceMatch() },
                modifier = Modifier.weight(1f)
            )
        }

        // Room Categories
        val categories = RoomType.values()
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = false,
                    onClick = { /* Filter logic */ },
                    label = { Text(category.name.lowercase().capitalize()) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Rooms
        Text(
            text = "Active Voice Rooms",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.rooms) { room ->
                com.rafiq.presentation.components.home.VoiceRoomItem(
                    room = room,
                    onClick = { onNavigateToRoom(room.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun QuickMatchCard(
    title: String,
    subtitle: String,
    icon: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}
