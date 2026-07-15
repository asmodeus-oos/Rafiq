package com.rafiq.presentation.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafiq.domain.model.Message
import com.rafiq.presentation.components.chat.MessageBubble
import com.rafiq.presentation.theme.BackgroundPrimary
import com.rafiq.presentation.theme.TextPrimary
import com.rafiq.presentation.theme.GlassBackground
import com.rafiq.presentation.theme.RafiqShapes
import com.rafiq.presentation.components.common.PillButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showGiftDialog by remember { mutableStateOf(false) }
    
    if (showGiftDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showGiftDialog = false },
            title = { Text("Send a Gift") },
            text = {
                Column {
                    val gifts = listOf("Rose" to 10, "Car" to 500, "Yacht" to 5000)
                    gifts.forEach { gift ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(gift.first)
                            androidx.compose.material3.Button(onClick = { 
                                showGiftDialog = false
                                // onSendMessage("Sent a ${gift.first} gift!") 
                            }) {
                                Text("${gift.second} Coins")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showGiftDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundPrimary)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(GlassBackground).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PillButton(text = "<", onClick = onBack, isPrimary = false)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Chat Name", color = TextPrimary)
        }
        
        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages.reversed()) { msg ->
                MessageBubble(message = msg)
            }
        }
        
        // Input
        Row(
            modifier = Modifier.fillMaxWidth().background(GlassBackground).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.IconButton(onClick = { showGiftDialog = true }) {
                androidx.compose.material3.Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_gift),
                    contentDescription = "Send Gift",
                    tint = com.rafiq.presentation.theme.PrimaryAccent
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                shape = RafiqShapes.extraLarge,
                placeholder = { Text("Message...") },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = com.rafiq.presentation.theme.PrimaryAccent,
                    unfocusedBorderColor = com.rafiq.presentation.theme.BorderLight
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            PillButton(
                text = "Send",
                onClick = { 
                    if(text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                }
            )
        }
    }
}
