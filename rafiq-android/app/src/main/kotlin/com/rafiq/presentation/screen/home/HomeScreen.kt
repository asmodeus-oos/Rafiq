package com.rafiq.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.items

import com.rafiq.R
import kotlinx.coroutines.launch
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll

import io.github.jan.supabase.auth.auth
import com.rafiq.presentation.theme.BackgroundSecondary
import com.rafiq.presentation.theme.BorderLight
import com.rafiq.presentation.theme.TextPrimary
import com.rafiq.presentation.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDiscovery: () -> Unit,
    onNavigateToRandomCall: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onSignOut: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToRoom: (String) -> Unit,
    onNavigateToPostDetails: (String) -> Unit = {},
    supabaseClient: io.github.jan.supabase.SupabaseClient? = null,
    viewModel: com.rafiq.presentation.screen.profile.ModernProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    homeFeedViewModel: HomeFeedViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    notificationViewModel: com.rafiq.presentation.screen.notification.NotificationViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val homeUiState by homeFeedViewModel.uiState.collectAsState()
    val unreadNotificationsCount by notificationViewModel.unreadCount.collectAsState()
    
    val tabs = listOf(
        stringResource(com.rafiq.R.string.home) to com.composables.icons.lucide.R.drawable.lucide_ic_house,
        stringResource(com.rafiq.R.string.voice) to com.composables.icons.lucide.R.drawable.lucide_ic_mic_vocal,
        stringResource(com.rafiq.R.string.posts) to com.composables.icons.lucide.R.drawable.lucide_ic_newspaper,
        stringResource(com.rafiq.R.string.search) to com.composables.icons.lucide.R.drawable.lucide_ic_search,
        stringResource(com.rafiq.R.string.chats) to com.composables.icons.lucide.R.drawable.lucide_ic_messages_square
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val isUserLoading by viewModel.isLoading.collectAsState()
    var isProfileMenuExpanded by remember { mutableStateOf(false) }
    var showCreatePostSheet by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.activity.compose.BackHandler {
        if (pagerState.currentPage != 0) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(0)
            }
        } else {
            (context as? android.app.Activity)?.moveTaskToBack(true)
        }
    }

    LaunchedEffect(user, isUserLoading) {
        if (!isUserLoading && user == null && supabaseClient?.auth?.currentUserOrNull() == null) {
            onSignOut()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.text_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.height(32.dp)
                        )
                    }
                },
                navigationIcon = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToNotifications
                        ) {
                            Box {
                                Icon(
                                    painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_bell), 
                                    contentDescription = "Notifications", 
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                if (unreadNotificationsCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 2.dp, y = 2.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(androidx.compose.ui.graphics.Color(0xFFff385c))
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    // VIP Star Badge
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_star),
                            contentDescription = "VIP",
                            tint = Color(0xFFFFD700)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { isProfileMenuExpanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.avatar.isNullOrBlank()) {
                            Icon(
                                painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                                contentDescription = "Avatar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            coil3.compose.AsyncImage(
                                model = user!!.avatar,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        
                        DropdownMenu(
                            expanded = isProfileMenuExpanded,
                            onDismissRequest = { isProfileMenuExpanded = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = androidx.compose.ui.graphics.Color.White,
                            tonalElevation = 0.dp,
                            shadowElevation = 8.dp,
                            offset = androidx.compose.ui.unit.DpOffset(x = 0.dp, y = 12.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(com.rafiq.R.string.profile), style = MaterialTheme.typography.titleSmall) },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(12.dp)),
                                onClick = { 
                                    isProfileMenuExpanded = false 
                                    user?.id?.takeIf { it.isNotBlank() }?.let(onNavigateToProfile)
                                },
                                leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user), contentDescription = null) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(com.rafiq.R.string.sign_out), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error) },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(12.dp)),
                                onClick = { 
                                    isProfileMenuExpanded = false
                                    onSignOut()
                                },
                                leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_log_out), contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, pair ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = { 
                            Icon(
                                painter = painterResource(id = pair.second), 
                                contentDescription = pair.first,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        label = { Text(pair.first, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = com.rafiq.presentation.theme.PrimaryAccent.copy(alpha = 0.18f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            val fabIcon = when (pagerState.currentPage) {
                1 -> com.composables.icons.lucide.R.drawable.lucide_ic_phone
                2 -> com.composables.icons.lucide.R.drawable.lucide_ic_pen_line
                else -> com.composables.icons.lucide.R.drawable.lucide_ic_plus
            }
            com.rafiq.presentation.components.common.DualToneFAB(
                iconRes = fabIcon,
                contentDescription = "Action FAB",
                onClick = {
                    if (pagerState.currentPage == 0 || pagerState.currentPage == 2) {
                        showCreatePostSheet = true
                    } else if (pagerState.currentPage == 1) {
                        onNavigateToRandomCall()
                    }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> HomeFeedContent(
                    uiState = homeUiState,
                    onUserClick = onNavigateToProfile,
                    onRoomClick = onNavigateToRoom,
                    onRefresh = { homeFeedViewModel.refreshFeed() },
                    onLike = { homeFeedViewModel.likeUser(it) },
                    onSkip = { homeFeedViewModel.skipUser(it) }
                )
                1 -> VoiceRoomsTab(
                    onJoinRoom = onNavigateToRoom
                )
                2 -> com.rafiq.presentation.screen.post.PostsTabScreen(
                    onNavigateToPostDetails = onNavigateToPostDetails,
                    onNavigateToProfile = onNavigateToProfile
                )
                3 -> {
                    if (supabaseClient != null) {
                        SearchTab(
                            supabaseClient = supabaseClient,
                            currentUserId = viewModel.currentUserId,
                            onNavigateToProfile = { uid -> onNavigateToProfile(uid) },
                            onNavigateToPost = onNavigateToPostDetails
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Search unavailable", color = Color.Gray)
                        }
                    }
                }
                4 -> {
                    val chatListViewModel: com.rafiq.presentation.screen.chat.ChatListViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                    val chats by chatListViewModel.chats.collectAsState()
                    val isChatLoading by chatListViewModel.isLoading.collectAsState()
                    
                    com.rafiq.presentation.screen.chat.ChatListScreen(
                        chats = chats,
                        isLoading = isChatLoading,
                        onRefresh = { chatListViewModel.refresh() },
                        onNavigateToChat = onNavigateToChat,
                        onNavigateBack = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                }
            }
        }
        
        // ... (ModalBottomSheet logic)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeFeedContent(
    uiState: HomeUiState,
    onUserClick: (String) -> Unit,
    onRoomClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onLike: (String) -> Unit,
    onSkip: (String) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isLoading, onRefresh = onRefresh)

    val hasContent = uiState.dailyMatch != null || uiState.trendingRooms.isNotEmpty() || uiState.recommendedPartners.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        if (uiState.isLoading && !hasContent) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = com.rafiq.presentation.theme.PrimaryAccent)
            }
        } else if (!uiState.isLoading && !hasContent) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_sparkles),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = com.rafiq.presentation.theme.PrimaryAccent
                    )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome to RAFIQ!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pull down to refresh or check out the search tab to discover new people.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Daily Discovery Card (Priority Match)
                item {
                    Text(
                        text = "Match of the Day",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    uiState.dailyMatch?.let { (user, score) ->
                        com.rafiq.presentation.components.home.DiscoveryCard(
                            user = user,
                            score = score,
                            onLike = { onLike(user.id) },
                            onSkip = { onSkip(user.id) },
                            onWave = { /* Icebreaker */ }
                        )
                    } ?: Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundSecondary),
                        border = BorderStroke(1.dp, BorderLight),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Your strongest match will appear here.",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Keep swiping and the app will keep this section filled.",
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // 2. Trending Voice Rooms (Horizontal)
                if (uiState.trendingRooms.isNotEmpty()) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Live Rooms",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = {}) {
                                    Text("View All", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(uiState.trendingRooms) { room ->
                                    com.rafiq.presentation.components.home.VoiceRoomItem(
                                        room = room,
                                        onClick = { onRoomClick(room.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Recommended Partners
                item {
                    Text(
                        text = "People for You",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (uiState.recommendedPartners.isNotEmpty()) {
                    items(uiState.recommendedPartners) { user ->
                        SuggestedUserCard(
                            user = user,
                            onUserClick = onUserClick,
                            onLike = { onLike(user.id) },
                            onSkip = { onSkip(user.id) }
                        )
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = BackgroundSecondary),
                            border = BorderStroke(1.dp, BorderLight),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "No suggested users right now.",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "New profiles will appear here as soon as they match your filters.",
                                    color = TextTertiary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuggestedUserCard(
    user: com.rafiq.domain.model.User,
    onUserClick: (String) -> Unit = {},
    onLike: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val isMale = user.gender == com.rafiq.domain.model.Gender.MALE
    Card(
        onClick = { onUserClick(user.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundSecondary),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BorderLight),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.avatar.isNotBlank()) {
                        coil3.compose.AsyncImage(
                            model = user.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        val avatarIcon = if (isMale) com.composables.icons.lucide.R.drawable.lucide_ic_user else com.composables.icons.lucide.R.drawable.lucide_ic_user_round
                        Icon(
                            painter = painterResource(id = avatarIcon),
                            contentDescription = null,
                            tint = TextTertiary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        if (user.isVerified) {
                            Icon(
                                painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_badge_check),
                                contentDescription = null,
                                tint = com.rafiq.presentation.theme.PrimaryAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(
                                    id = if (isMale) com.composables.icons.lucide.R.drawable.lucide_ic_mars else com.composables.icons.lucide.R.drawable.lucide_ic_venus
                                ),
                                contentDescription = null,
                                tint = com.rafiq.presentation.theme.PrimaryAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (user.username.isNotBlank()) "@${user.username}" else "@" + user.name.lowercase().replace(" ", ""),
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        }
                        if (user.country.isNotBlank()) {
                            Text(
                                text = user.country,
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = user.bio.ifBlank { "Open to match, chat, and explore." },
                color = TextPrimary,
                fontSize = 14.sp,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                user.hobbies.take(4).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(BorderLight)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(tag, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

fun getFlagEmoji(country: String): String {
    if (country.isBlank()) return ""
    if (country.length == 2) {
        val firstLetter = Character.codePointAt(country.uppercase(), 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(country.uppercase(), 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }
    return "🌐"
}
