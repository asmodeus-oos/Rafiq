package com.rafiq.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rafiq.domain.model.Post
import com.rafiq.domain.model.User
import com.rafiq.presentation.screen.search.SearchFilterBottomSheet
import com.rafiq.presentation.screen.search.SearchFilterState
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.theme.TextPrimary
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchTab(
    supabaseClient: SupabaseClient,
    currentUserId: String?,
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToPost: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var filterState by remember { mutableStateOf(SearchFilterState()) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val trendingTags = listOf("#CairoDating", "#Music", "#TechTalk", "#Gaming", "#Confessions", "#CoffeeLovers")

    // Load suggested users initially when query is blank
    LaunchedEffect(Unit) {
        try {
            val suggested = supabaseClient.postgrest["users"]
                .select(Columns.ALL)
                .decodeList<User>()
                .filter { it.id != currentUserId }
            if (users.isEmpty()) {
                users = suggested.shuffled().take(25)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Debounced search
    LaunchedEffect(query, filterState) {
        searchJob?.cancel()
        if (query.isBlank()) {
            posts = emptyList()
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        delay(350) // Debounce 350ms
        try {
            val pattern = "%${query.trim()}%"
            var fetchedUsers = try {
                supabaseClient.postgrest["users"]
                    .select(Columns.ALL) {
                        filter {
                            or {
                                ilike("name", pattern)
                                ilike("username", pattern)
                                ilike("bio", pattern)
                                ilike("country", pattern)
                            }
                        }
                    }
                    .decodeList<User>()
                    .filter { it.id != currentUserId }
            } catch (e: Exception) {
                emptyList()
            }

            if (filterState.verifiedOnly) {
                fetchedUsers = fetchedUsers.filter { it.isVerified }
            }
            if (filterState.onlineOnly) {
                fetchedUsers = fetchedUsers.filter { it.onlineStatus == com.rafiq.domain.model.OnlineStatus.ONLINE }
            }

            users = fetchedUsers.take(30)

            val fetchedPosts = try {
                supabaseClient.postgrest["posts"]
                    .select(Columns.ALL) {
                        filter { ilike("text_content", pattern) }
                    }
                    .decodeList<Post>()
                    .sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                emptyList()
            }

            posts = fetchedPosts.take(30)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoading = false
    }

    if (showFilterSheet) {
        SearchFilterBottomSheet(
            filterState = filterState,
            onDismiss = { showFilterSheet = false },
            onApplyFilters = { filterState = it }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8FB))
    ) {
        // Search bar with filter icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_search),
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(visible = query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                        IconButton(onClick = { query = "" }) {
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

            Spacer(modifier = Modifier.width(8.dp))

            // Filter Trigger Button
            IconButton(
                onClick = { showFilterSheet = true },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (filterState.verifiedOnly || filterState.onlineOnly) PrimaryAccent else Color.White)
            ) {
                Icon(
                    painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_sliders_horizontal),
                    contentDescription = "Filters",
                    tint = if (filterState.verifiedOnly || filterState.onlineOnly) Color.White else TextPrimary
                )
            }
        }

        // Results tabs
        if (query.isNotBlank()) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryAccent,
                indicator = {},
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text(
                        "All (${users.size + posts.size})",
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) PrimaryAccent else Color.Gray
                    )
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text(
                        "People (${users.size})",
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) PrimaryAccent else Color.Gray
                    )
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text(
                        "Posts (${posts.size})",
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 2) PrimaryAccent else Color.Gray
                    )
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryAccent, modifier = Modifier.size(32.dp))
            }
        } else if (query.isBlank()) {
            // Trending Tags & Suggestions View
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Trending Topics 🔥", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trendingTags) { tag ->
                        Surface(
                            onClick = { query = tag.replace("#", "") },
                            shape = RoundedCornerShape(14.dp),
                            color = PrimaryAccent.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.15f))
                        ) {
                            Text(
                                tag,
                                color = PrimaryAccent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Suggested People", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        SearchUserRow(user = user, onClick = { onNavigateToProfile(user.id) })
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTab == 0) {
                    if (users.isEmpty() && posts.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No matches found for '$query'", color = Color.Gray)
                            }
                        }
                    } else {
                        if (users.isNotEmpty()) {
                            item {
                                Text("People", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = TextPrimary)
                            }
                            items(users.take(5)) { user ->
                                SearchUserRow(user = user, onClick = { onNavigateToProfile(user.id) })
                            }
                        }
                        if (posts.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Posts", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = TextPrimary)
                            }
                            items(posts.take(10)) { post ->
                                SearchPostRow(post = post, onClick = { onNavigateToPost(post.id) })
                            }
                        }
                    }
                } else if (selectedTab == 1) {
                    if (users.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No users found", color = Color.Gray)
                            }
                        }
                    } else {
                        items(users) { user ->
                            SearchUserRow(user = user, onClick = { onNavigateToProfile(user.id) })
                        }
                    }
                } else {
                    if (posts.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No posts found", color = Color.Gray)
                            }
                        }
                    } else {
                        items(posts) { post ->
                            SearchPostRow(post = post, onClick = { onNavigateToPost(post.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchUserRow(user: User, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray)
            ) {
                if (user.avatar.isNotBlank()) {
                    AsyncImage(
                        model = user.avatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.name.ifBlank { "Anonymous" }, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_badge_check),
                            contentDescription = "Verified",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (user.username.isNotBlank()) {
                    Text("@${user.username}", color = Color.Gray, fontSize = 13.sp)
                } else if (user.bio.isNotBlank()) {
                    Text(user.bio, color = Color.Gray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PrimaryAccent.copy(alpha = 0.1f)
            ) {
                Text(
                    "View",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    color = PrimaryAccent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun SearchPostRow(post: Post, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryAccent.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (!post.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!post.audioUrl.isNullOrBlank()) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_mic),
                        contentDescription = null,
                        tint = PrimaryAccent
                    )
                } else {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_file_text),
                        contentDescription = null,
                        tint = PrimaryAccent
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    post.textContent.ifBlank { "Media Post" },
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_heart),
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("${post.likesCount}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_message_square),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("${post.commentsCount}", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}
