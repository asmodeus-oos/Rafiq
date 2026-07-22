package com.rafiq.presentation.screen.post

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.rafiq.domain.manager.ShareManager
import com.rafiq.domain.model.Comment
import com.rafiq.domain.model.Post
import com.rafiq.presentation.screen.profile.ModernPostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(
    postId: String,
    highlightCommentId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: PostDetailsViewModel = hiltViewModel()
) {
    val post by viewModel.post.collectAsState()
    val postUser by viewModel.postUser.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var commentText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    var postToEdit by remember { mutableStateOf<Post?>(null) }
    var commentToEdit by remember { mutableStateOf<Comment?>(null) }
    var showLikersForPost by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val listState = rememberLazyListState()

    LaunchedEffect(postId, highlightCommentId) {
        viewModel.loadPostDetails(postId, highlightCommentId)
    }

    // Auto-scroll to target comment when deep link opens
    LaunchedEffect(highlightCommentId, comments) {
        if (!highlightCommentId.isNullOrBlank() && comments.isNotEmpty()) {
            val targetIdx = comments.indexOfFirst { it.id == highlightCommentId }
            if (targetIdx >= 0) {
                // Account for post header item (+1)
                listState.animateScrollToItem(targetIdx + 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_chevron_left), contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val currentPost = post
                        if (currentPost != null) {
                            val author = postUser?.name ?: "User"
                            ShareManager.sharePost(context, author, currentPost.textContent, currentPost.id)
                        }
                    }) {
                        Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_share_2), contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(8.dp)
                ) {
                    if (replyingTo != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Replying to ${replyingTo?.user?.name ?: "User"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { replyingTo = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x), contentDescription = "Cancel Reply", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Write a comment...") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )
                        
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.submitComment(postId, commentText, replyingTo?.id)
                                    commentText = ""
                                    replyingTo = null
                                }
                            },
                            enabled = commentText.isNotBlank()
                        ) {
                            Icon(
                                painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_send),
                                contentDescription = "Send",
                                tint = if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading && post == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Main Post
                if (post != null && postUser != null) {
                    item {
                        ModernPostCard(
                            user = postUser!!,
                            post = post!!,
                            currentUserId = viewModel.currentUserId,
                            onLikeClick = { viewModel.toggleLike(postId) },
                            onLikeLongClick = { showLikersForPost = postId },
                            onCommentClick = { },
                            onEditClick = { postToEdit = it },
                            onDeleteClick = { viewModel.deletePost(it.id) { onNavigateBack() } }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                }

                // Comments List
                items(comments.size) { index ->
                    val comment = comments[index]
                    val isHighlighted = comment.id == highlightCommentId
                    CommentItem(
                        comment = comment,
                        currentUserId = viewModel.currentUserId,
                        isHighlighted = isHighlighted,
                        onReplyClick = { replyingTo = it },
                        onProfileClick = onNavigateToProfile,
                        onEditClick = { commentToEdit = it },
                        onDeleteClick = { viewModel.deleteComment(it.id) }
                    )
                    if (index < comments.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                    }
                }
            }
        }
    }

    val currentCommentToEdit = commentToEdit
    if (currentCommentToEdit != null) {
        var editCommentText by remember(currentCommentToEdit.id) { mutableStateOf(currentCommentToEdit.textContent) }
        AlertDialog(
            onDismissRequest = { commentToEdit = null },
            title = { Text("Edit Comment") },
            text = {
                OutlinedTextField(
                    value = editCommentText,
                    onValueChange = { editCommentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Comment") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.editComment(currentCommentToEdit.id, editCommentText)
                    commentToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { commentToEdit = null }) { Text("Cancel") }
            }
        )
    }

    if (showLikersForPost != null) {
        com.rafiq.presentation.components.post.LikersBottomSheet(
            postId = showLikersForPost!!,
            fetchLikers = { viewModel.fetchLikers(it) },
            onDismiss = { showLikersForPost = null },
            onUserClick = {
                showLikersForPost = null
                onNavigateToProfile(it)
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
}

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String?,
    isHighlighted: Boolean = false,
    highlightCommentId: String? = null,
    onReplyClick: (Comment) -> Unit,
    onProfileClick: (String) -> Unit,
    onEditClick: (Comment) -> Unit,
    onDeleteClick: (Comment) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val borderColor by animateColorAsState(
        targetValue = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(600),
        label = "commentHighlight"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (comment.parentId == null) 24.dp else 0.dp,
                end = if (comment.parentId == null) 24.dp else 0.dp,
                bottom = if (comment.parentId == null) 16.dp else 0.dp
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = if (isHighlighted) 2.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.White
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarUrl = comment.user?.avatar
                    if (avatarUrl != null && avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onProfileClick(comment.userId ?: "") }
                        )
                    } else {
                        Image(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6))
                                .clickable { onProfileClick(comment.userId ?: "") },
                            contentScale = ContentScale.Inside
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = comment.user?.name ?: "Unknown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.clickable { onProfileClick(comment.userId ?: "") }
                        )
                        val timeStr = android.text.format.DateUtils.getRelativeTimeSpanString(comment.timestamp).toString()
                        Text(timeStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        Surface(
                            onClick = { showMenu = true },
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_ellipsis_vertical), contentDescription = "More Options", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
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
                            DropdownMenuItem(
                                text = { Text("Share Comment", style = MaterialTheme.typography.titleSmall) },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(12.dp)),
                                onClick = {
                                    showMenu = false
                                    ShareManager.shareComment(
                                        context = context,
                                        postId = comment.postId,
                                        commentId = comment.id,
                                        authorName = comment.user?.name ?: "User",
                                        commentText = comment.textContent
                                    )
                                },
                                leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_share_2), contentDescription = null, modifier = Modifier.size(18.dp)) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            )
                            DropdownMenuItem(
                                text = { Text("Copy Link", style = MaterialTheme.typography.titleSmall) },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(12.dp)),
                                onClick = {
                                    showMenu = false
                                    val url = ShareManager.getCommentUrl(comment.postId, comment.id)
                                    ShareManager.copyToClipboard(context, url, "Comment Link")
                                },
                                leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_copy), contentDescription = null, modifier = Modifier.size(18.dp)) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            )
                            if (comment.userId == currentUserId) {
                                DropdownMenuItem(
                                    text = { Text("Edit", style = MaterialTheme.typography.titleSmall) },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(12.dp)),
                                    onClick = {
                                        showMenu = false
                                        onEditClick(comment)
                                    },
                                    leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_pencil), contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error) },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).clip(RoundedCornerShape(12.dp)),
                                    onClick = {
                                        showMenu = false
                                        onDeleteClick(comment)
                                    },
                                    leadingIcon = { Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_trash_2), contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (comment.replyingToUsername != null) {
                    Text(
                        text = "Replying to @${comment.replyingToUsername}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(text = comment.textContent, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF111111))
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Reply",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onReplyClick(comment) }
                    )
                }
            }
        }
        
        if (comment.replies.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }
            val repliesToShow = if (expanded) comment.replies else comment.replies.take(1)

            Column(modifier = Modifier.padding(start = 32.dp, top = 12.dp)) {
                repliesToShow.forEach { reply ->
                    CommentItem(
                        comment = reply,
                        currentUserId = currentUserId,
                        isHighlighted = (reply.id == highlightCommentId),
                        onReplyClick = onReplyClick,
                        onProfileClick = onProfileClick,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick
                    )
                    if (reply != repliesToShow.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                if (comment.replies.size > 1) {
                    Text(
                        text = if (expanded) "Hide replies" else "Show ${comment.replies.size - 1} more replies",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 8.dp)
                            .clickable { expanded = !expanded }
                    )
                }
            }
        }
    }
}
