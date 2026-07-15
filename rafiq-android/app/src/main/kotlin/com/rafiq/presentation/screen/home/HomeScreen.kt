package com.rafiq.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items

import com.rafiq.R
import kotlinx.coroutines.launch
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDiscovery: () -> Unit,
    onNavigateToRandomCall: () -> Unit,
    onNavigateToProfile: (String?) -> Unit,
    onSignOut: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: com.rafiq.presentation.screen.profile.ModernProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    notificationViewModel: com.rafiq.presentation.screen.notification.NotificationViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
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
    var isProfileMenuExpanded by remember { mutableStateOf(false) }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                        IconButton(
                            onClick = {
                                android.widget.Toast.makeText(context, context.getString(com.rafiq.R.string.vip_only_filter_users_by_gender), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_funnel), contentDescription = stringResource(com.rafiq.R.string.filter_vip_only), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
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
                                modifier = Modifier.size(22.dp)
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
                                    onNavigateToProfile(null)
                                },
                                leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user), contentDescription = null) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(com.rafiq.R.string.vip_membership), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.tertiary) },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(12.dp)),
                                onClick = { isProfileMenuExpanded = false },
                                leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_star), contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(com.rafiq.R.string.settings), style = MaterialTheme.typography.titleSmall) },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(12.dp)),
                                onClick = { isProfileMenuExpanded = false },
                                leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_settings), contentDescription = null) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
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
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    scrolledContainerColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.ui.graphics.Color.White,
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
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            val fabIcon = when(pagerState.currentPage) {
                0 -> com.composables.icons.lucide.R.drawable.lucide_ic_plus
                1 -> com.composables.icons.lucide.R.drawable.lucide_ic_phone
                2 -> com.composables.icons.lucide.R.drawable.lucide_ic_pen_line
                3 -> com.composables.icons.lucide.R.drawable.lucide_ic_scan
                4 -> com.composables.icons.lucide.R.drawable.lucide_ic_pen
                else -> com.composables.icons.lucide.R.drawable.lucide_ic_plus
            }
            
            FloatingActionButton(
                onClick = {},
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(painter = painterResource(id = fabIcon), contentDescription = "FAB")
            }
        }
    ) { innerPadding ->
        val suggestedUsers by viewModel.suggestedUsers.collectAsState()
        
        val isLoading by viewModel.isLoading.collectAsState()
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            if (page == 0) {
                HomeFeedContent(
                    onRandomCall = onNavigateToRandomCall,
                    onDiscovery = onNavigateToDiscovery,
                    onUserClick = { userId -> onNavigateToProfile(userId) },
                    currentUserTier = user?.tier ?: com.rafiq.domain.model.Tier.FREE, 
                    suggestedUsers = suggestedUsers,
                    isLoading = isLoading,
                    onRefresh = { viewModel.refresh() }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Content for Tab $page", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeFeedContent(
    onRandomCall: () -> Unit,
    onDiscovery: () -> Unit,
    onUserClick: (String) -> Unit,
    currentUserTier: com.rafiq.domain.model.Tier = com.rafiq.domain.model.Tier.FREE,
    suggestedUsers: List<com.rafiq.domain.model.User> = emptyList(),
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    BoxWithConstraints {
        val isSmallScreen = maxWidth < 360.dp
        val columns = if (isSmallScreen) 1 else 2
        
        val pullRefreshState = rememberPullRefreshState(refreshing = isLoading, onRefresh = onRefresh)

        Box(
            modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            item {
                // 4 Games Cards
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val voiceGameStr = stringResource(com.rafiq.R.string.voice_game)
                    val soulGameStr = stringResource(com.rafiq.R.string.soul_game)
                    val games = listOf(voiceGameStr, soulGameStr, stringResource(com.rafiq.R.string.party_game), stringResource(com.rafiq.R.string.group_video_game))
                    val rows = games.chunked(columns)
                    for (row in rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (game in row) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable {
                                            if (game == voiceGameStr) onRandomCall()
                                            else if (game == soulGameStr) onDiscovery()
                                        },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                androidx.compose.ui.graphics.Brush.linearGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                                                    )
                                                )
                                            ), 
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(game, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                            if (row.size < columns) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            val maxSuggestions = when(currentUserTier) {
                com.rafiq.domain.model.Tier.FREE -> 10
                com.rafiq.domain.model.Tier.GOLD -> 100
                com.rafiq.domain.model.Tier.PLATINUM -> 250
                com.rafiq.domain.model.Tier.DIAMOND -> 500
            }

            val usersList = suggestedUsers.take(maxSuggestions)
            items(items = usersList) { user ->
                SuggestedUserCard(user = user, onUserClick = onUserClick)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun SuggestedUserCard(user: com.rafiq.domain.model.User, onUserClick: (String) -> Unit = {}) {
    val isMale = user.gender == com.rafiq.domain.model.Gender.MALE
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onUserClick(user.id) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
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
                    val avatarTint = if (isMale) androidx.compose.ui.graphics.Color(0xFF2196F3) else androidx.compose.ui.graphics.Color(0xFFE91E63)
                    Icon(
                        painter = painterResource(id = avatarIcon),
                        contentDescription = null,
                        tint = avatarTint
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val genderIconRes = if (isMale) com.composables.icons.lucide.R.drawable.lucide_ic_mars else com.composables.icons.lucide.R.drawable.lucide_ic_venus
                        val genderColor = if (isMale) androidx.compose.ui.graphics.Color(0xFF2196F3) else androidx.compose.ui.graphics.Color(0xFFE91E63)
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(genderColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = genderIconRes),
                                contentDescription = "Gender",
                                tint = genderColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    if (user.showAge && user.age > 0) {
                        Text(
                            text = "${user.age} y/o",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    if (user.username.isNotBlank()) {
                        Text("@${user.username}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    if (user.showLocation && user.country.isNotBlank()) {
                        val flag = getFlagEmoji(user.country)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            if (flag.isNotEmpty()) Text(flag, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(user.country, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tags = user.hobbies.take(4)
                    tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(tag, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
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
