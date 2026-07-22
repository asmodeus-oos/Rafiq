package com.rafiq.presentation.screen.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.rafiq.domain.model.Post
import com.rafiq.domain.model.User
import com.rafiq.presentation.components.common.AudioPlayerComponent
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.theme.TextPrimary
import com.rafiq.presentation.theme.TextTertiary

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PostsTabScreen(
    onNavigateToPostDetails: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit = {},
    viewModel: PostFeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFeedType by remember { mutableStateOf(0) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = { viewModel.loadFeed() }
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6))) {
        // Feed Type Switcher
        val feedTypes = listOf("For You", "Following", "Nearby", "Confessions", "Reels")
        ScrollableTabRow(
            selectedTabIndex = selectedFeedType,
            containerColor = Color.White,
            edgePadding = 16.dp,
            divider = {}
        ) {
            feedTypes.forEachIndexed { index, title ->
                Tab(
                    selected = selectedFeedType == index,
                    onClick = { selectedFeedType = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedFeedType == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedFeedType == index) PrimaryAccent else Color.Gray
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryAccent)
                }
            } else if (!uiState.isLoading && uiState.posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_newspaper),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No posts yet. Be the first to share!", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.posts) { post ->
                        val author = uiState.users[post.userId]
                        FeedPostCard(
                            post = post,
                            author = author,
                            onPostClick = { onNavigateToPostDetails(post.id) },
                            onProfileClick = { if (post.userId.isNotBlank()) onNavigateToProfile(post.userId) },
                            onLikeClick = { viewModel.likePost(post.id) }
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = PrimaryAccent
            )
        }
    }
}

@Composable
fun FeedPostCard(
    post: Post,
    author: User?,
    onPostClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    Card(
        onClick = onPostClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    if (author?.avatar?.isNotBlank() == true) {
                        AsyncImage(
                            model = author.avatar,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = author?.name?.ifBlank { "User" } ?: "User",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = if (author?.username?.isNotBlank() == true) "@${author.username}" else "Rafiq Member",
                        fontSize = 12.sp,
                        color = TextTertiary
                    )
                }

                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Post",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Text Content
            if (!post.textContent.isNullOrBlank()) {
                Text(
                    text = post.textContent,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Media Image
            if (!post.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                ) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Post image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Voice Note Player
            if (!post.audioUrl.isNullOrBlank()) {
                AudioPlayerComponent(
                    audioUrl = post.audioUrl
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLikeClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_heart),
                        contentDescription = "Like",
                        tint = if (post.likedBy.isNotEmpty()) PrimaryAccent else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.likesCount}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (post.likedBy.isNotEmpty()) PrimaryAccent else Color.Gray
                    )
                }

                // Comment Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPostClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_message_circle),
                        contentDescription = "Comment",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.commentsCount}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }

                // Share Button
                IconButton(onClick = { /* Share */ }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_share_2),
                        contentDescription = "Share",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
