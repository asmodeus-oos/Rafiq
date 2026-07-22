package com.rafiq.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rafiq.domain.model.VoiceRoom
import com.rafiq.presentation.theme.AccentBadge
import com.rafiq.presentation.theme.AccentSurface
import com.rafiq.presentation.theme.PrimaryAccent

@Composable
fun VoiceRoomItem(
    room: VoiceRoom,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .height(160.dp),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AccentSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Tag
                Surface(
                    color = AccentBadge,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = room.type.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryAccent
                    )
                }

                // Participant Count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_users),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = PrimaryAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${room.participantCount}",
                        fontSize = 12.sp,
                        color = PrimaryAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = room.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            
            Text(
                text = room.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Host Avatars (Mock for now)
                Box(contentAlignment = Alignment.CenterStart) {
                    repeat(3) { index ->
                    Box(
                            modifier = Modifier
                                .padding(start = (index * 16).dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PrimaryAccent.copy(alpha = 0.08f), CircleShape)
                                .padding(1.dp)
                                .clip(CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                if (room.activeSpeakers > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryAccent.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(PrimaryAccent)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Live",
                            color = PrimaryAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
