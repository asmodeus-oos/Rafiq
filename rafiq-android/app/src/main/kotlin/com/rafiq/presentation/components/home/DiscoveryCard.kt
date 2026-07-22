package com.rafiq.presentation.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.rafiq.domain.model.User
import com.rafiq.presentation.theme.PrimaryAccent

@Composable
fun DiscoveryCard(
    user: User,
    score: Int,
    onLike: () -> Unit,
    onSkip: () -> Unit,
    onWave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Photo & Match Badge Stack
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF3F4F6))
            ) {
                if (user.avatar.isNotBlank()) {
                    AsyncImage(
                        model = user.avatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Match Percentage Badge
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$score% Match",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${user.name.ifBlank { "User" }}${if (user.age > 0) ", ${user.age}" else ""}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_badge_check),
                                contentDescription = "Verified",
                                tint = PrimaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (user.username.isNotBlank()) {
                        Text(
                            text = "@${user.username}",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (user.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user.bio,
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dual-Tone Rounded Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Button
                Surface(
                    onClick = onSkip,
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(width = 72.dp, height = 56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x),
                            contentDescription = "Skip",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Wave / Icebreaker Button
                Surface(
                    onClick = onWave,
                    color = Color(0xFFE0E7FF),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(width = 72.dp, height = 56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_sparkles),
                            contentDescription = "Wave",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Like Button
                Surface(
                    onClick = onLike,
                    color = PrimaryAccent,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(width = 110.dp, height = 56.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_heart),
                            contentDescription = "Like",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Like", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
