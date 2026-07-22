package com.rafiq.presentation.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.presentation.theme.PrimaryAccent

data class GiftItem(
    val id: String,
    val name: String,
    val emoji: String,
    val diamonds: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftPickerSheet(
    userDiamonds: Int = 500,
    onDismiss: () -> Unit,
    onSendGift: (GiftItem) -> Unit
) {
    val gifts = listOf(
        GiftItem("rose", "Rose", "🌹", 10),
        GiftItem("chocolate", "Chocolate", "🍫", 20),
        GiftItem("teddy", "Teddy Bear", "🧸", 50),
        GiftItem("sparkler", "Sparkler", "🎆", 100),
        GiftItem("crown", "Crown", "👑", 200),
        GiftItem("car", "Sports Car", "🏎️", 500),
        GiftItem("jet", "Private Jet", "✈️", 1000),
        GiftItem("yacht", "Yacht", "🚤", 2000),
        GiftItem("castle", "Castle", "🏰", 4000),
        GiftItem("rocket", "Rocket", "🚀", 7000),
        GiftItem("galaxy", "Galaxy", "🌌", 10000)
    )

    var selectedGift by remember { mutableStateOf<GiftItem?>(gifts[0]) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Send Virtual Gift",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )

                Surface(
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💎", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$userDiamonds",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                items(gifts) { gift ->
                    val isSelected = selectedGift?.id == gift.id
                    Card(
                        onClick = { selectedGift = gift },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PrimaryAccent.copy(alpha = 0.08f) else Color(0xFFF9FAFB)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) PrimaryAccent else Color(0xFFE5E7EB)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(gift.emoji, fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                gift.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💎", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    "${gift.diamonds}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = PrimaryAccent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    selectedGift?.let { onSendGift(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                enabled = selectedGift != null
            ) {
                Text(
                    text = if (selectedGift != null) "Send ${selectedGift!!.name} (${selectedGift!!.diamonds} 💎)" else "Select a Gift",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
