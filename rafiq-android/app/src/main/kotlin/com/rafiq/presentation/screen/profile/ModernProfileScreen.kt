package com.rafiq.presentation.screen.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.rafiq.domain.model.Gender
import com.rafiq.domain.model.Post
import com.rafiq.domain.model.Tier
import com.rafiq.domain.model.User

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.combinedClickable
import com.rafiq.presentation.components.post.LikersBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)
@Composable
fun ModernProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPostDetails: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    viewModel: ModernProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isOwner by viewModel.isOwner.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()
    val followersList by viewModel.followers.collectAsState()
    val followingList by viewModel.following.collectAsState()
    val androidContext = LocalContext.current
    var postToEdit by remember { mutableStateOf<Post?>(null) }
    var showLikersForPost by remember { mutableStateOf<String?>(null) }

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_CONTACTS
        )
    )

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    val postsTab = stringResource(com.rafiq.R.string.posts)
    val followersTab = stringResource(com.rafiq.R.string.followers)
    val followingTab = stringResource(com.rafiq.R.string.following)
    val visitorsTab = stringResource(com.rafiq.R.string.visitors)
    val roomTab = stringResource(com.rafiq.R.string.room)
    val shopTab = "Shop"

    var selectedTab by remember(postsTab) { mutableStateOf(postsTab) }
    var showCreatePostModal by remember { mutableStateOf(false) }
    var showAvatarPreview by remember { mutableStateOf(false) }
    var isPosting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showAvatarPreview && user != null) {
        val u = user!!
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showAvatarPreview = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
            ) {
                // Close Button
                IconButton(
                    onClick = { showAvatarPreview = false },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x),
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Avatar Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f)
                        .align(Alignment.Center)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (u.avatar.isNotBlank()) {
                        AsyncImage(
                            model = u.avatar,
                            contentDescription = "Full Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                            contentDescription = "Default Avatar",
                            tint = Color.LightGray,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                // Bottom user info bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(u.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    if (u.username.isNotBlank()) {
                        Text("@${u.username}", color = Color.Gray, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isOwner) {
                        Button(
                            onClick = {
                                showAvatarPreview = false
                                onNavigateToEditProfile()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_camera), contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Profile Picture")
                        }
                    }
                }
            }
        }
    }

    if (showCreatePostModal) {
        CreatePostModal(
            onDismiss = { showCreatePostModal = false },
            isPosting = isPosting,
            onPostCreate = { text, imageBytes, audioBytes ->
                if (isPosting) return@CreatePostModal
                isPosting = true
                coroutineScope.launch {
                    val skipCompression = user?.isOwner == true || user?.tier == com.rafiq.domain.model.Tier.PLATINUM || user?.tier == com.rafiq.domain.model.Tier.DIAMOND || user?.isVerified == true
                    val compressedImageBytes = if (imageBytes != null && !skipCompression) {
                        com.rafiq.util.ImageUtils.compressImage(imageBytes)
                    } else imageBytes
                    val success = viewModel.createPost(text, compressedImageBytes, audioBytes)
                    isPosting = false
                    if (success) {
                        showCreatePostModal = false
                    } else {
                        android.widget.Toast.makeText(androidContext, "Failed to create post. Check connection.", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
    
    val currentPostToEdit = postToEdit
    if (currentPostToEdit != null) {
        var editText by remember(currentPostToEdit.id) { mutableStateOf(currentPostToEdit.textContent) }
        AlertDialog(
            onDismissRequest = { postToEdit = null },
            title = { Text("Edit Post") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Post content") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.editPost(currentPostToEdit.id, editText)
                    postToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { postToEdit = null }) { Text("Cancel") }
            }
        )
    }

    val tabs = remember(user?.tier, isOwner, postsTab, followersTab, followingTab, visitorsTab, roomTab, shopTab) {
        buildList {
            add(postsTab)
            add(followersTab)
            add(followingTab)
            val tier = user?.tier
            if (tier == Tier.PLATINUM || tier == Tier.DIAMOND) add(visitorsTab)
            if (tier == Tier.GOLD || tier == Tier.PLATINUM || tier == Tier.DIAMOND) add(roomTab)
            if (isOwner) add(shopTab)
        }
    }
    
    // Ensure selected tab is valid
    LaunchedEffect(tabs, postsTab) {
        if (!tabs.contains(selectedTab)) {
            selectedTab = tabs.firstOrNull() ?: postsTab
        }
    }

    Scaffold(
        containerColor = Color(0xFFF3F4F6),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button Card
                Surface(
                    onClick = { onNavigateBack() },
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_arrow_left), contentDescription = stringResource(com.rafiq.R.string.back), modifier = Modifier.padding(12.dp), tint = MaterialTheme.colorScheme.onBackground)
                }

                // Center Title Card
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(48.dp).padding(horizontal = 8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp)) {
                        Text(stringResource(com.rafiq.R.string.profile), fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                }

                // Right Section: Share Profile & Tier Icon
                if (user != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Share Profile Button
                        Surface(
                            onClick = {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Check out my profile on Rafiq!\nhttps://rafiq-roan.vercel.app/profile/${user?.id}")
                                }
                                androidContext.startActivity(android.content.Intent.createChooser(shareIntent, "Share Profile"))
                            },
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_share_2),
                                contentDescription = "Share Profile",
                                modifier = Modifier.padding(12.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        val tierIcon = when (user?.tier) {
                            com.rafiq.domain.model.Tier.FREE -> com.composables.icons.lucide.R.drawable.lucide_ic_user
                            com.rafiq.domain.model.Tier.GOLD -> com.composables.icons.lucide.R.drawable.lucide_ic_award
                            com.rafiq.domain.model.Tier.PLATINUM -> com.composables.icons.lucide.R.drawable.lucide_ic_crown
                            com.rafiq.domain.model.Tier.DIAMOND -> com.composables.icons.lucide.R.drawable.lucide_ic_gem
                            else -> com.composables.icons.lucide.R.drawable.lucide_ic_user
                        }
                        val tierColor = when (user?.tier) {
                            com.rafiq.domain.model.Tier.FREE -> Color.Gray
                            com.rafiq.domain.model.Tier.GOLD -> Color(0xFFFFD700)
                            com.rafiq.domain.model.Tier.PLATINUM -> Color(0xFFE5E4E2)
                            com.rafiq.domain.model.Tier.DIAMOND -> Color(0xFF00FFFF)
                            else -> Color.Gray
                        }
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(painter = painterResource(id = tierIcon), contentDescription = "Tier", modifier = Modifier.padding(12.dp), tint = tierColor)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == stringResource(com.rafiq.R.string.posts) && !isLoading && user != null && isOwner) {
                com.rafiq.presentation.components.common.DualToneFAB(
                    iconRes = com.composables.icons.lucide.R.drawable.lucide_ic_feather,
                    contentDescription = stringResource(com.rafiq.R.string.new_post),
                    onClick = { showCreatePostModal = true }
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (user == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(com.rafiq.R.string.failed_to_load_profile_please_check_your_internet_or_log_out_and_log_back_in), color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            val u = user!!
            val pullRefreshState = rememberPullRefreshState(refreshing = isLoading, onRefresh = { viewModel.refresh() })

            Box(
                modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(), 
                        bottom = 100.dp
                    )
                ) {
                    item {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp),
                            shadowElevation = 0.dp
                        ) {
                            Column {
                                // Banner + Avatar Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp) // 140 for banner, 40 for avatar overhang space
                                ) {
                                    // Cover Photo
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(32.dp))
                                    ) {
                                        if (u.coverPhoto.isNotBlank()) {
                                            AsyncImage(
                                                model = u.coverPhoto,
                                                contentDescription = "Cover Photo",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFFE0E7FF), Color(0xFFC7D2FE)))))
                                        }
                                        
                                        // Diamond Pill inside cover photo
                                        Surface(
                                            color = Color.White.copy(alpha = 0.9f),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(end = 25.dp, bottom = 10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                                                Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_gem), contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${u.diamonds}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }
                                    
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 24.dp)
                                            .align(Alignment.BottomStart)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .background(Color.White, CircleShape)
                                                .padding(4.dp)
                                                .clip(CircleShape)
                                                .clickable { showAvatarPreview = true }
                                        ) {
                                            if (u.avatar.isNotBlank()) {
                                                AsyncImage(
                                                    model = u.avatar,
                                                    contentDescription = "Avatar",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Image(
                                                    painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                                                    contentDescription = "Avatar",
                                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                        }
                                        // Online indicator dot like WhatsApp
                                        val statusColor = when (u.onlineStatus) {
                                            com.rafiq.domain.model.OnlineStatus.ONLINE -> Color(0xFF22C55E)
                                            com.rafiq.domain.model.OnlineStatus.IN_CALL -> Color(0xFFFF9800)
                                            else -> Color.Gray
                                        }
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .offset(x = (-4).dp, y = (-4).dp)
                                                .size(16.dp)
                                                .background(Color.White, CircleShape)
                                                .padding(2.dp)
                                                .background(statusColor, CircleShape)
                                        )
                                        }
                                    }
                                
                                // Info Section
                                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                    // Name Row
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(u.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                        
                                        Spacer(modifier = Modifier.weight(1f))
                                        
                                        // Gender, Age, Location
                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val genderColor = if (u.gender == com.rafiq.domain.model.Gender.MALE) Color(0xFF2196F3) else Color(0xFFE91E63)
                                                val genderIcon = if (u.gender == com.rafiq.domain.model.Gender.MALE) com.composables.icons.lucide.R.drawable.lucide_ic_mars else com.composables.icons.lucide.R.drawable.lucide_ic_venus
                                                Icon(painterResource(id = genderIcon), contentDescription = null, tint = genderColor, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("${u.age}", fontSize = 14.sp, color = genderColor, fontWeight = FontWeight.Bold)
                                            }
                                            if (u.showLocation && u.country.isNotBlank()) {
                                                Text("${u.governorate}, ${u.country}", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    // Subtitle
                                    Text("@${u.username}", color = Color.Gray, fontSize = 14.sp)
                                    if (u.bio.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(u.bio, color = Color.Gray, fontSize = 14.sp)
                                    }
                                    
                                    if (u.links["showHobbies"]?.toBoolean() ?: true && u.hobbies.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            color = Color(0xFFF8F9FA),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                                            androidx.compose.foundation.layout.FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                u.hobbies.forEach { hobby ->
                                                    val iconRes = when(hobby) {
                                                        "Football" -> com.composables.icons.lucide.R.drawable.lucide_ic_target
                                                        "Basketball" -> com.composables.icons.lucide.R.drawable.lucide_ic_circle
                                                        "Gym", "Fitness" -> com.composables.icons.lucide.R.drawable.lucide_ic_activity
                                                        "Running" -> com.composables.icons.lucide.R.drawable.lucide_ic_footprints
                                                        "Gaming" -> com.composables.icons.lucide.R.drawable.lucide_ic_gamepad_2
                                                        "Coding", "Technology" -> com.composables.icons.lucide.R.drawable.lucide_ic_code
                                                        "Reading" -> com.composables.icons.lucide.R.drawable.lucide_ic_book
                                                        "Photography" -> com.composables.icons.lucide.R.drawable.lucide_ic_camera
                                                        "Movies", "TV Shows", "Anime" -> com.composables.icons.lucide.R.drawable.lucide_ic_film
                                                        "Music", "Singing", "Concerts" -> com.composables.icons.lucide.R.drawable.lucide_ic_music
                                                        "Dancing" -> com.composables.icons.lucide.R.drawable.lucide_ic_star
                                                        "Travel" -> com.composables.icons.lucide.R.drawable.lucide_ic_plane
                                                        "Cooking" -> com.composables.icons.lucide.R.drawable.lucide_ic_flame
                                                        "Coffee" -> com.composables.icons.lucide.R.drawable.lucide_ic_coffee
                                                        "Fashion" -> com.composables.icons.lucide.R.drawable.lucide_ic_shopping_bag
                                                        "Pets" -> com.composables.icons.lucide.R.drawable.lucide_ic_heart
                                                        "Meditation" -> com.composables.icons.lucide.R.drawable.lucide_ic_moon
                                                        "Volunteering" -> com.composables.icons.lucide.R.drawable.lucide_ic_hand
                                                        "Entrepreneurship" -> com.composables.icons.lucide.R.drawable.lucide_ic_briefcase
                                                        else -> null
                                                    }
                                                    Surface(
                                                        color = Color.White,
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                                            if (iconRes != null) {
                                                                Icon(painterResource(id = iconRes), contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                            }
                                                            Text(hobby, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // Stats Row
                                    Surface(
                                        color = Color(0xFFF8F9FA),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Item 1
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(followersCount.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("followers", fontSize = 12.sp, color = Color.Gray)
                                            }
                                            
                                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE5E7EB)))
                                            
                                            // Item 2
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(followingCount.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("following", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // Action Button
                                    if (isOwner) {
                                        com.rafiq.presentation.components.common.DualToneButton(
                                            text = "Edit Profile",
                                            onClick = { onNavigateToEditProfile() },
                                            modifier = Modifier.fillMaxWidth(),
                                            height = 48.dp
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            if (isFollowing == false) {
                                                com.rafiq.presentation.components.common.DualToneButton(
                                                    text = "Follow",
                                                    onClick = { viewModel.toggleFollow() },
                                                    modifier = Modifier.weight(1f),
                                                    height = 48.dp
                                                )
                                            } else {
                                                Surface(
                                                    onClick = { if (isFollowing != null) viewModel.toggleFollow() },
                                                    color = Color.Transparent,
                                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, com.rafiq.presentation.theme.PrimaryAccent),
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier.weight(1f).height(48.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(if (isFollowing == true) "Following" else "...", color = com.rafiq.presentation.theme.PrimaryAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    }
                                                }
                                            }
                                            Surface(
                                                onClick = { onNavigateToChat(u.id) },
                                                color = Color(0xFFF8F9FA),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier.weight(1f).height(48.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("Get in Touch", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }

                    item {
                        // Futuristic Pill Tabs
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(tabs) { tabName ->
                                val isSelected = selectedTab == tabName
                                val bgColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                val textColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                
                                Surface(
                                    color = bgColor,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedTab = tabName }
                                ) {
                                    Text(
                                        text = tabName,
                                        color = textColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { tabs.size })
                        LaunchedEffect(pagerState.currentPage) {
                            selectedTab = tabs[pagerState.currentPage]
                        }
                        LaunchedEffect(selectedTab) {
                            val index = tabs.indexOf(selectedTab)
                            if (index != -1 && pagerState.currentPage != index) {
                                pagerState.animateScrollToPage(index)
                            }
                        }

                        androidx.compose.foundation.pager.HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        ) { page ->
                            val currentTab = tabs[page]
                            if (currentTab == stringResource(com.rafiq.R.string.posts)) {
                                if (posts.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text(stringResource(com.rafiq.R.string.no_posts_yet), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        posts.forEach { post ->
                                            androidx.compose.runtime.key(post.id) {
                                                ModernPostCard(
                                                    user = u,
                                                    post = post,
                                                    currentUserId = viewModel.currentUserId,
                                                    onLikeClick = { viewModel.toggleLike(post.id) },
                                                    onLikeLongClick = { showLikersForPost = post.id },
                                                    onCommentClick = { onNavigateToPostDetails(post.id) },
                                                    onEditClick = { postToEdit = it },
                                                    onDeleteClick = { viewModel.deletePost(it.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (currentTab == shopTab) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_gem),
                                        contentDescription = "Diamonds",
                                        modifier = Modifier.size(64.dp),
                                        tint = Color(0xFF00FFFF)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Buy Diamonds",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Coming soon: purchase diamonds to unlock premium features and VIP tiers!",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            } else if (currentTab == stringResource(com.rafiq.R.string.room)) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("No signals found for $currentTab", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(android.content.Intent.EXTRA_TEXT, "Join my Voice Room on Rafiq!\nhttps://rafiq-roan.vercel.app/room/${u.id}")
                                            }
                                            androidContext.startActivity(android.content.Intent.createChooser(shareIntent, "Share Voice Room"))
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_share_2), contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Share Voice Room Link")
                                    }
                                }
                            } else if (currentTab == followersTab || currentTab == followingTab) {
                                val userList = if (currentTab == followersTab) followersList else followingList
                                if (userList.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (currentTab == followersTab) "No followers yet" else "Not following anyone yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                        userList.forEach { uItem ->
                                            androidx.compose.runtime.key(uItem.id) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE5E7EB))) {
                                                        if (uItem.avatar.isNullOrBlank()) {
                                                            Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user), contentDescription = null, modifier = Modifier.align(Alignment.Center), tint = Color.Gray)
                                                        } else {
                                                            AsyncImage(model = uItem.avatar, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(uItem.name.ifBlank { "Unknown" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                                        if (!uItem.bio.isNullOrBlank()) {
                                                            Text(uItem.bio, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No signals found for $currentTab", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
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

    if (showLikersForPost != null) {
        LikersBottomSheet(
            postId = showLikersForPost!!,
            fetchLikers = { viewModel.fetchLikers(it) },
            onDismiss = { showLikersForPost = null },
            onUserClick = {
                showLikersForPost = null
                // Wait for navigation logic if applicable
            }
        )
    }
}


@Composable
fun GlassPill(icon: Int, text: String, iconTint: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(painter = painterResource(id = icon), contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DualToneTag(icon: Int, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ModernPostCard(
    user: User,
    post: Post,
    currentUserId: String?,
    onLikeClick: () -> Unit,
    onLikeLongClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onEditClick: (Post) -> Unit = {},
    onDeleteClick: (Post) -> Unit = {}
) {
    val context = LocalContext.current
    val isLiked = currentUserId != null && post.likedBy[currentUserId] == true
    val timestampText = android.text.format.DateUtils.getRelativeTimeSpanString(post.timestamp).toString()
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (user.avatar.isNotBlank()) {
                    AsyncImage(
                        model = user.avatar,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(user.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                    Text(timestampText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                if (post.userId == currentUserId) {
                    Box {
                        Surface(
                            onClick = { showMenu = true },
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_ellipsis_vertical), contentDescription = "More Options", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = Color.White,
                            tonalElevation = 0.dp,
                            shadowElevation = 8.dp
                        ) {
                            if (post.imageUrl.isNullOrBlank() && post.audioUrl.isNullOrBlank()) {
                                DropdownMenuItem(
                                    text = { Text("Edit", style = MaterialTheme.typography.titleSmall) },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(12.dp)),
                                    onClick = {
                                        showMenu = false
                                        onEditClick(post)
                                    },
                                    leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_pencil), contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error) },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(12.dp)),
                                onClick = {
                                    showMenu = false
                                    onDeleteClick(post)
                                },
                                leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_trash_2), contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Text Content
            if (post.textContent.isNotBlank()) {
                Text(post.textContent, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f), fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Optional Image
            if (!post.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Audio Player Component
            if (!post.audioUrl.isNullOrBlank()) {
                AudioPlayerComponent(audioUrl = post.audioUrl)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Interaction Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                // Like Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isLiked) Color(0xFFFF007F).copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                        .combinedClickable(
                            onClick = onLikeClick,
                            onLongClick = onLikeLongClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_heart), contentDescription = "Like", tint = if (isLiked) Color(0xFFFF007F) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${post.likesCount}", color = if (isLiked) Color(0xFFFF007F) else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Comment Button
                Surface(
                    onClick = { onCommentClick() },
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_message_circle), contentDescription = "Comment", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${post.commentsCount}", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Share Button
                Surface(
                    onClick = { 
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            val shareText = "${context.getString(com.rafiq.R.string.check_out_this_post_on_rafiq)}\nhttps://rafiq-roan.vercel.app/post/${post.id}"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Post"))
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_share_2), contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreatePostModal(onDismiss: () -> Unit, isPosting: Boolean, onPostCreate: (String, ByteArray?, ByteArray?) -> Unit) {
    var textContent by remember { mutableStateOf("") }
    var attachedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    
    // Audio recording state
    var isRecording by remember { mutableStateOf(false) }
    var hasAudio by remember { mutableStateOf(false) }
    var audioFile by remember { mutableStateOf<java.io.File?>(null) }
    var mediaRecorder by remember { mutableStateOf<android.media.MediaRecorder?>(null) }
    var showRecordUI by remember { mutableStateOf(false) }
    var recordingTimer by remember { mutableStateOf(0) }
    var maxAmplitude by remember { mutableStateOf(0) }
    var amplitudes by remember { mutableStateOf(List(20) { 4 }) }
    
    val context = androidx.compose.ui.platform.LocalContext.current


    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachedImageUri = uri
        }
    }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(stringResource(com.rafiq.R.string.create_post), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            
            androidx.compose.material3.OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                placeholder = { Text(stringResource(com.rafiq.R.string.what_s_on_your_mind), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            if (attachedImageUri != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))) {
                    AsyncImage(
                        model = attachedImageUri,
                        contentDescription = stringResource(com.rafiq.R.string.attached_image),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { attachedImageUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x), contentDescription = stringResource(com.rafiq.R.string.remove), tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        modifier = Modifier.size(48.dp).clickable {
                            photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                        shape = CircleShape
                    ) {
                        Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_image), contentDescription = stringResource(com.rafiq.R.string.image), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                    }

                    Surface(
                        modifier = Modifier.size(48.dp).clickable {
                            if (context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                showRecordUI = true
                                recordingTimer = 0
                            } else {
                                android.widget.Toast.makeText(context, "Microphone permission required", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        color = if (isRecording) Color.Red.copy(alpha = 0.1f) else (if (hasAudio) Color.Green.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), 
                        shape = CircleShape
                    ) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_mic),
                            contentDescription = "Voice Record",
                            tint = if (isRecording) Color.Red else (if (hasAudio) Color.Green else MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                

                
                Surface(
                        modifier = Modifier.height(48.dp).clickable { 
                        if (isPosting) return@clickable
                        if (textContent.isNotBlank() || attachedImageUri != null || hasAudio) {
                            val imgBytes = attachedImageUri?.let { uri -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }
                            val audBytes = if (hasAudio && audioFile != null) audioFile!!.readBytes() else null
                            onPostCreate(textContent, imgBytes, audBytes)
                        }
                    },
                    color = Color(0xFF111111),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp)) {
                        if (isPosting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(com.rafiq.R.string.post), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
            if (hasAudio && !showRecordUI) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 4.dp
                ) {
                    Surface(
                        color = Color(0xFFFCEAE9),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().height(70.dp).padding(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).height(40.dp)) {
                                amplitudes.forEach { amp ->
                                    Box(modifier = Modifier.width(4.dp).height(amp.dp).background(Color(0xFFF35B69), CircleShape))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(String.format("0:%02d", recordingTimer), color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { hasAudio = false; audioFile = null }, modifier = Modifier.size(24.dp)) {
                                Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x), contentDescription = "Remove", tint = Color.Black)
                            }
                        }
                    }
                }
            }
            if (showRecordUI) {
                LaunchedEffect(Unit) {
                    val file = java.io.File(context.cacheDir, "audio_post_${System.currentTimeMillis()}.mp4")
                    audioFile = file
                    try {
                        mediaRecorder = @Suppress("DEPRECATION") android.media.MediaRecorder().apply {
                            setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                            setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                            setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                            setOutputFile(file.absolutePath)
                            prepare()
                            start()
                        }
                        isRecording = true
                        val startTime = System.currentTimeMillis()
                        while (isRecording) {
                            kotlinx.coroutines.delay(100)
                            val elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                            if (elapsedSeconds >= 30) break
                            recordingTimer = elapsedSeconds
                            maxAmplitude = mediaRecorder?.maxAmplitude ?: 0
                            val scaledAmp = (maxAmplitude / 500).coerceIn(4, 40)
                            amplitudes = amplitudes.drop(1) + scaledAmp
                        }
                    } catch(e: Exception) { e.printStackTrace() }
                    finally {
                        isRecording = false
                        try { mediaRecorder?.stop() } catch (e: Exception) {}
                        mediaRecorder?.release()
                        mediaRecorder = null
                        if (recordingTimer >= 30) {
                            hasAudio = true
                            showRecordUI = false
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(
                            color = Color(0xFFFCEAE9),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).height(40.dp)) {
                                    amplitudes.forEach { amp ->
                                        Box(modifier = Modifier.width(4.dp).height(amp.dp).background(Color(0xFFF35B69), CircleShape))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(String.format("0:%02d", recordingTimer), color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            var isVisible by remember { mutableStateOf(true) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    isVisible = !isVisible
                                    kotlinx.coroutines.delay(500)
                                }
                            }
                            val alpha by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isVisible) 1f else 0f)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFF35B69).copy(alpha = alpha)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Recording", color = Color(0xFFF35B69), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFCEAE9),
                                modifier = Modifier.clickable {
                                    isRecording = false
                                    try { mediaRecorder?.stop() } catch (e: Exception) {}
                                    mediaRecorder?.release()
                                    mediaRecorder = null
                                    showRecordUI = false
                                    audioFile = null
                                    hasAudio = false
                                    recordingTimer = 0
                                }
                            ) {
                                Text("Cancel", color = Color(0xFFF35B69), modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black,
                                modifier = Modifier.clickable {
                                    isRecording = false
                                    try { mediaRecorder?.stop() } catch (e: Exception) {}
                                    mediaRecorder?.release()
                                    mediaRecorder = null
                                    showRecordUI = false
                                    hasAudio = true
                                }
                            ) {
                                Text("Save", color = Color.White, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}







@Composable
fun AudioPlayerComponent(
    audioUrl: String,
    containerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
    contentColor: Color = MaterialTheme.colorScheme.primary,
    onSurfaceColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    var isPlaying by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var progress by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0f) }
    var duration by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    var currentPosition by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    var playbackSpeed by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(1.0f) }
    
    var mediaPlayer by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<android.media.MediaPlayer?>(null) }
    
    androidx.compose.runtime.DisposableEffect(audioUrl) {
        val player = android.media.MediaPlayer()
        mediaPlayer = player
        
        try {
            val finalUrl = if (audioUrl.startsWith("file://")) audioUrl.replace("file://", "") else audioUrl
            player.setDataSource(finalUrl)
            player.prepareAsync()
            player.setOnPreparedListener { mp ->
                duration = mp.duration
            }
            player.setOnCompletionListener {
                isPlaying = false
                currentPosition = 0
                progress = 0f
                it.seekTo(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        onDispose {
            player.release()
            mediaPlayer = null
        }
    }
    
    androidx.compose.runtime.LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val player = mediaPlayer
            if (player != null && player.isPlaying) {
                currentPosition = player.currentPosition
                if (duration > 0) {
                    progress = currentPosition.toFloat() / duration.toFloat()
                }
            }

            kotlinx.coroutines.delay(100)
        }
    }
    
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = {
                    val player = mediaPlayer ?: return@Surface
                    if (isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.start()
                        isPlaying = true
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                player.playbackParams = player.playbackParams.setSpeed(playbackSpeed)
                            }
                        } catch (e: Exception) {}
                    }
                },
                color = contentColor,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                val icon = if (isPlaying) com.composables.icons.lucide.R.drawable.lucide_ic_pause else com.composables.icons.lucide.R.drawable.lucide_ic_play
                Icon(painter = painterResource(id = icon), contentDescription = "Play/Pause", tint = Color.White, modifier = Modifier.padding(12.dp))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                onClick = {
                    playbackSpeed = when (playbackSpeed) {
                        1.0f -> 1.5f
                        1.5f -> 2.0f
                        else -> 1.0f
                    }
                    if (isPlaying) {
                        try {
                            val player = mediaPlayer
                            if (player != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                player.playbackParams = player.playbackParams.setSpeed(playbackSpeed)
                            }
                        } catch (e: Exception) {}
                    }
                },
                color = contentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Text(text = "${playbackSpeed}x", color = contentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            WaveformSlider(
                progress = progress,
                onProgressChange = { newProgress ->
                    progress = newProgress
                    val seekToPos = (progress * duration).toInt()
                    mediaPlayer?.seekTo(seekToPos)
                    currentPosition = seekToPos
                },
                audioUrl = audioUrl,
                primaryColor = contentColor,
                modifier = Modifier.weight(1f).height(32.dp).padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            val currStr = String.format("%02d:%02d", (currentPosition / 1000) / 60, (currentPosition / 1000) % 60)
            val durStr = String.format("%02d:%02d", (duration / 1000) / 60, (duration / 1000) % 60)
            Text(text = "$currStr / $durStr", fontSize = 11.sp, color = onSurfaceColor)
        }
    }
}



@Composable
fun WaveformSlider(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    audioUrl: String,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val seed = audioUrl.hashCode().toLong()
    val random = androidx.compose.runtime.remember(audioUrl) { java.util.Random(seed) }
    
    val numBars = 35
    val barHeights = androidx.compose.runtime.remember(audioUrl) {
        FloatArray(numBars) {
            0.2f + random.nextFloat() * 0.8f
        }
    }
    
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onProgressChange(newProgress)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    onProgressChange(newProgress)
                }
            }
    ) {
        val barWidth = size.width / numBars
        val barSpacing = 4.dp.toPx()
        val actualBarWidth = (barWidth - barSpacing).coerceAtLeast(1f)
        
        for (i in 0 until numBars) {
            val barProgress = i.toFloat() / numBars
            val color = if (barProgress <= progress) {
                primaryColor
            } else {
                primaryColor.copy(alpha = 0.3f)
            }
            
            val barHeight = size.height * barHeights[i]
            val x = i * barWidth + (barSpacing / 2)
            val y = (size.height - barHeight) / 2
            
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(actualBarWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(actualBarWidth / 2, actualBarWidth / 2)
            )
        }
        
        // Draw the black handle
        val handleX = progress * size.width
        val handleRadius = 6.dp.toPx()
        drawCircle(
            color = androidx.compose.ui.graphics.Color.Black,
            radius = handleRadius,
            center = androidx.compose.ui.geometry.Offset(handleX, size.height / 2)
        )
    }
}

