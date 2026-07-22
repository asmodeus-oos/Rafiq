package com.rafiq.presentation.screen.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.Comment
import com.rafiq.domain.model.Post
import com.rafiq.domain.model.User
import com.rafiq.domain.repository.NotificationRepository
import com.rafiq.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val supabaseClient: SupabaseClient,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post.asStateFlow()

    private val _postUser = MutableStateFlow<User?>(null)
    val postUser: StateFlow<User?> = _postUser.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val currentUserId = supabaseClient.auth.currentUserOrNull()?.id

    // Track the main loading job so we can cancel it before restarting
    private var loadJob: kotlinx.coroutines.Job? = null

    fun loadPostDetails(postId: String) {
        println("RAFIQ_DEBUG: loadPostDetails called for $postId")
        // Cancel previous job to avoid duplicate pollers / flow collectors
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            
            // Poll Post and User every 1 second
            launch {
                while(isActive) {
                    println("RAFIQ_DEBUG: Polling post data for $postId")
                    val postResult = postRepository.getPostById(postId)
                    postResult.getOrNull()?.let { p ->
                        println("RAFIQ_DEBUG: Polled post: $p")
                        _post.value = p
                        try {
                            val u = supabaseClient.postgrest["users"]
                                .select(Columns.ALL) { filter { eq("id", p.userId) } }
                                .decodeSingle<User>()
                            _postUser.value = u
                        } catch (e: Exception) {
                            println("RAFIQ_DEBUG: Polling user error: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                    kotlinx.coroutines.delay(1000)
                }
            }
            
            // Load Comments
            println("RAFIQ_DEBUG: Collecting comments flow for $postId")
            postRepository.getCommentsForPost(postId).collect { commentList ->
                println("RAFIQ_DEBUG: Received ${commentList.size} comments from flow")
                // Fetch users for comments
                val userIds = commentList.mapNotNull { it.userId }.distinct()
                val usersMap = if (userIds.isNotEmpty()) {
                    try {
                        supabaseClient.postgrest["users"]
                            .select(Columns.ALL) { filter { isIn("id", userIds) } }
                            .decodeList<User>()
                            .associateBy { it.id }
                    } catch (e: Exception) {
                        println("RAFIQ_DEBUG: Fetching comment users error: ${e.message}")
                        e.printStackTrace()
                        emptyMap<String, User>()
                    }
                } else {
                    emptyMap()
                }
                
                val commentsWithUsers = commentList.map { it.copy(user = usersMap[it.userId]) }
                
                // Build a true nested tree: each comment only holds its DIRECT replies,
                // and those replies recursively hold their own direct replies.
                fun buildReplies(parentId: String): List<Comment> {
                    return commentsWithUsers
                        .filter { it.parentId == parentId }
                        .map { reply ->
                            // Attach the replying-to username from the parent comment
                            val parentUser = commentsWithUsers.find { it.id == parentId }?.user
                            reply.copy(
                                replyingToUsername = parentUser?.name,
                                replies = buildReplies(reply.id).sortedBy { it.timestamp }
                            )
                        }
                }

                val topLevelComments = commentsWithUsers
                    .filter { it.parentId == null }
                    .map { it.copy(replies = buildReplies(it.id).sortedBy { c -> c.timestamp }) }
                
                println("RAFIQ_DEBUG: Updating _comments with ${topLevelComments.size} top-level comments")
                _comments.value = topLevelComments
                _isLoading.value = false
            }
        }
    }

    fun submitComment(postId: String, text: String, parentId: String?) {
        val userId = currentUserId ?: return
        println("RAFIQ_DEBUG: submitComment called for post $postId with text '$text' and parentId $parentId")
        viewModelScope.launch {
            val generatedId = java.util.UUID.randomUUID().toString()
            val comment = Comment(
                id = generatedId,
                postId = postId,
                userId = userId,
                textContent = text,
                parentId = parentId,
                timestamp = System.currentTimeMillis()
            )
            println("RAFIQ_DEBUG: Calling postRepository.createComment")
            val result = postRepository.createComment(comment)
            if (result.isSuccess) {
                println("RAFIQ_DEBUG: createComment success")
                val postOwnerId = _postUser.value?.id
                
                if (parentId == null) {
                    // New top-level comment on a post → notify post owner
                    if (postOwnerId != null && postOwnerId != userId) {
                        notificationRepository.createNotification(
                            recipientId = postOwnerId,
                            type = "comment",
                            postId = postId,
                            commentId = comment.id
                        )
                    }
                } else {
                    // Reply to an existing comment → notify the comment owner
                    // Also notify post owner if different from the comment owner
                    val commentOwnerId = _comments.value
                        .flatMap { listOf(it) + it.replies }
                        .find { it.id == parentId }?.userId
                    if (commentOwnerId != null && commentOwnerId != userId) {
                        notificationRepository.createNotification(
                            recipientId = commentOwnerId,
                            type = "reply",
                            postId = postId,
                            commentId = comment.id
                        )
                    }
                    if (postOwnerId != null && postOwnerId != userId && postOwnerId != commentOwnerId) {
                        notificationRepository.createNotification(
                            recipientId = postOwnerId,
                            type = "comment",
                            postId = postId,
                            commentId = comment.id
                        )
                    }
                }
            
                // Mentions Logic
                val mentions = Regex("@([a-zA-Z0-9_]+)").findAll(text).map { it.groupValues[1] }.toList()
                if (mentions.isNotEmpty()) {
                    try {
                        val mentionedUsers = supabaseClient.postgrest["users"]
                            .select(io.github.jan.supabase.postgrest.query.Columns.ALL) { filter { isIn("username", mentions) } }
                            .decodeList<User>()
                        for (u in mentionedUsers) {
                            if (u.id != userId) {
                                notificationRepository.createNotification(
                                    recipientId = u.id,
                                    type = "mention",
                                    postId = postId,
                                    commentId = comment.id
                                )
                            }
                        }
                    } catch (e: Exception) {
                        println("RAFIQ_DEBUG: Mentions error: ${e.message}")
                        e.printStackTrace()
                    }
                }
            } else {
                println("RAFIQ_DEBUG: createComment failed: ${result.exceptionOrNull()?.message}")
            }
            // The realtime flow in loadPostDetails will pick up the new comment automatically.
            // No need to call loadPostDetails again here — that would start duplicate pollers.
        }
    }

    fun toggleLike(postId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            // Optimistic update
            val currentPost = _post.value
            val hasLiked = currentPost?.likedBy?.get(userId) == true
            if (currentPost != null) {
                val newLikedBy = currentPost.likedBy.toMutableMap()
                if (hasLiked) newLikedBy.remove(userId) else newLikedBy[userId] = true
                _post.value = currentPost.copy(likesCount = newLikedBy.size, likedBy = newLikedBy)
            }
            
            val result = postRepository.likePost(postId, userId)
            if (result.isSuccess && currentPost != null && !hasLiked) {
                val postOwnerId = currentPost.userId
                if (postOwnerId != userId) {
                    notificationRepository.createNotification(
                        recipientId = postOwnerId,
                        type = "like",
                        postId = postId
                    )
                }
            }
            if (result.isFailure) {
                // Revert if failed by reloading
                loadPostDetails(postId)
            }
        }
    }

    fun editPost(postId: String, newText: String) {
        viewModelScope.launch {
            postRepository.updatePost(postId, newText)
            loadPostDetails(postId)
        }
    }

    fun deletePost(postId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = postRepository.deletePost(postId)
            if (result.isSuccess) {
                onSuccess()
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            postRepository.deleteComment(commentId)
            _post.value?.id?.let { loadPostDetails(it) }
        }
    }

    fun editComment(commentId: String, newText: String) {
        viewModelScope.launch {
            postRepository.updateComment(commentId, newText)
            _post.value?.id?.let { loadPostDetails(it) }
        }
    }

    suspend fun fetchLikers(postId: String): Result<List<User>> {
        return postRepository.getPostLikers(postId)
    }
}
