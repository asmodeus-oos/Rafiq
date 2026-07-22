package com.rafiq.presentation.screen.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoiceRoomBottomSheet(
    onDismiss: () -> Unit,
    onCreateRoom: (title: String, description: String, category: String, maxParticipants: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("OPEN") }
    var maxParticipants by remember { mutableStateOf(20f) }

    val categories = listOf("OPEN", "DATING", "GAMING", "MUSIC", "VIP")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Start a Voice Room", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x),
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Room Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Room Title") },
                placeholder = { Text("e.g. Late Night Tech & Chill") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryAccent)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("What are you talking about?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryAccent)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.take(3).forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryAccent else Color(0xFFF1F3F4),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                cat,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.drop(3).forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryAccent else Color(0xFFF1F3F4),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(
                                cat,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Max Participants Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Max Participants", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                Text("${maxParticipants.toInt()} users", fontWeight = FontWeight.Bold, color = PrimaryAccent)
            }
            Slider(
                value = maxParticipants,
                onValueChange = { maxParticipants = it },
                valueRange = 5f..100f,
                colors = SliderDefaults.colors(thumbColor = PrimaryAccent, activeTrackColor = PrimaryAccent)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Start Room Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateRoom(title.trim(), description.trim(), selectedCategory, maxParticipants.toInt())
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
            ) {
                Icon(
                    painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_mic),
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Go Live Now", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
