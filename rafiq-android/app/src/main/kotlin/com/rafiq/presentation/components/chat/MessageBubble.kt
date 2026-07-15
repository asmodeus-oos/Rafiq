package com.rafiq.presentation.components.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.domain.model.Message
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.theme.GlassBackground
import com.rafiq.presentation.theme.TextPrimary

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier,
    currentUserId: String? = null
) {
    val isMe = currentUserId != null && message.senderId == currentUserId
    val backgroundColor = if (isMe) PrimaryAccent else GlassBackground
    val textColor = if (isMe) androidx.compose.ui.graphics.Color.White else TextPrimary
    
    // Mix of ME Gallery massive radius + Apple HIG chat bubble logic
    val shape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = if (isMe) 24.dp else 4.dp,
        bottomEnd = if (isMe) 4.dp else 24.dp
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(backgroundColor)
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Text(
                text = message.textContent ?: "",
                color = textColor,
                fontSize = 16.sp
            )
        }
    }
}
