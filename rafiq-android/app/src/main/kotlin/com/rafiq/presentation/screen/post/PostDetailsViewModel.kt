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

    val currentUserId: String?
        get() = supabaseClient.auth.currentUserOrNull()?.id ?: _loggedInUser.value?.id

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
            
            // Load Comments (Polling + Realtime)
            launch {
                while(isActive) {
                    try {
                        val rawComments = supabaseClient.postgrest["comments"]
                            .select(Columns.ALL) { filter { eq("post_id", postId) } }
                            .decodeList<Comment>()
                        
                        val userIds = rawComments.mapNotNull { it.userId }.distinct()
                        val usersMap = if (userIds.isNotEmpty()) {
                            try {
                                supabaseClient.postgrest["users"]
                                    .select(Columns.ALL) { filter { isIn("id", userIds) } }
                                    .decodeList<User>()
                                    .associateBy { it.id }
                            } catch (e: Exception) {
                                emptyMap<String, User>()
                            }
                        } else {
                            emptyMap()
                        }
                        
                        val commentsWithUsers = rawComments.map { it.copy(user = usersMap[it.userId]) }
                        fun buildReplies(parentId: String): List<Comment> {
                            return commentsWithUsers
                                .filter { it.parentId == parentId }
                                .map { reply ->
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

                        val fetchedIds = rawComments.map { it.id }.toSet()
                        val pendingLocal = _comments.value.filter { it.id !in fetchedIds }
                        _comments.value = (topLevelComments + pendingLocal).distinctBy { it.id }
                        _isLoading.value = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    kotlinx.coroutines.delay(1500)
                }
            }
        }
    }

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> = _loggedInUser.asStateFlow()

    init {
        loadLoggedInUser()
    }

    private fun loadLoggedInUser() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            try {
                val u = supabaseClient.postgrest["users"]
                    .select(Columns.ALL) { filter { eq("id", uid) } }
                    .decodeSingleOrNull<User>()
                _loggedInUser.value = u
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitComment(postId: String, text: String, parentId: String?) {
        val userId = currentUserId
        println("RAFIQ_DEBUG: submitComment called for post $postId with text '$text', parentId $parentId, userId $userId")
        if (userId.isNullOrBlank()) {
            println("RAFIQ_DEBUG: submitComment CANCELLED - userId is null or blank!")
            return
        }
        viewModelScope.launch {
            val generatedId = java.util.UUID.randomUUID().toString()
            val currentUserObj = _loggedInUser.value ?: _comments.value.flatMap { listOf(it) + it.replies }.find { it.userId == userId }?.user
            val comment = Comment(
                id = generatedId,
                postId = postId,
                userId = userId,
                textContent = text,
                parentId = parentId,
                timestamp = System.currentTimeMillis(),
                user = currentUserObj
            )

            // 1. INSTANT OPTIMISTIC UI UPDATE
            if (parentId == null) {
                _comments.value = _comments.value + comment
            } else {
                _comments.value = _comments.value.map { topLevel ->
                    if (topLevel.id == parentId) {
                        topLevel.copy(replies = topLevel.replies + comment)
                    } else {
                        topLevel
                    }
                }
            }

            // 2. BACKEND CREATION
            val result = postRepository.createComment(comment)
            if (result.isSuccess) {
                println("RAFIQ_DEBUG: createComment success")
                val postOwnerId = _postUser.value?.id
                
                try {
                    if (parentId == null) {
                        if (postOwnerId != null && postOwnerId != userId) {
                            notificationRepository.createNotification(
                                recipientId = postOwnerId,
                                type = "comment",
                                postId = postId,
                                commentId = comment.id
                            )
                        }
                    } else {
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
                    }
                } catch (e: Exception) {
                    println("RAFIQ_DEBUG: Notification error: ${e.message}")
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
                    }
                }
            } else {
                println("RAFIQ_DEBUG: createComment failed: ${result.exceptionOrNull()?.message}")
            }
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
                val newLikeCount = if (hasLiked) (currentPost.likesCount - 1).coerceAtLeast(0) else currentPost.likesCount + 1
                _post.value = currentPost.copy(likesCount = newLikeCount, likedBy = newLikedBy)
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
