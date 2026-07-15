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

    fun loadPostDetails(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Load Post
            val postResult = postRepository.getPostById(postId)
            postResult.getOrNull()?.let { p ->
                _post.value = p
                // Load Post User
                try {
                    val u = supabaseClient.postgrest["users"]
                        .select(Columns.ALL) { filter { eq("id", p.userId) } }
                        .decodeSingle<User>()
                    _postUser.value = u
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Load Comments
            postRepository.getCommentsForPost(postId).collect { commentList ->
                // Fetch users for comments
                val userIds = commentList.map { it.userId }.distinct()
                val usersMap = if (userIds.isNotEmpty()) {
                    try {
                        supabaseClient.postgrest["users"]
                            .select(Columns.ALL) { filter { isIn("id", userIds) } }
                            .decodeList<User>()
                            .associateBy { it.id }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyMap<String, User>()
                    }
                } else {
                    emptyMap()
                }
                
                val commentsWithUsers = commentList.map { it.copy(user = usersMap[it.userId]) }
                
                // Flatten replies to prevent excessive nesting
                fun flattenReplies(parentId: String, replyingToUser: String?, depth: Int): List<Comment> {
                    val directReplies = commentsWithUsers.filter { it.parentId == parentId }
                    return directReplies.map { 
                        it.copy(replyingToUsername = replyingToUser) 
                    } + directReplies.flatMap { flattenReplies(it.id, it.user?.name, depth + 1) }
                }
                
                val topLevelComments = commentsWithUsers
                    .filter { it.parentId == null }
                    .map { it.copy(replies = flattenReplies(it.id, it.user?.name, 1).sortedBy { c -> c.timestamp }) }
                
                _comments.value = topLevelComments
                _isLoading.value = false
            }
        }
    }

    fun submitComment(postId: String, text: String, parentId: String?) {
        val userId = currentUserId ?: return
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
            postRepository.createComment(comment)
            val postOwnerId = _postUser.value?.id
            if (postOwnerId != null && postOwnerId != userId) {
                notificationRepository.createNotification(
                    recipientId = postOwnerId,
                    type = "reply",
                    postId = postId,
                    commentId = comment.id
                )
            }
            
            if (parentId != null) {
                val commentOwnerId = _comments.value.flatMap { it.replies + it }.find { it.id == parentId }?.userId
                if (commentOwnerId != null && commentOwnerId != userId && commentOwnerId != postOwnerId) {
                    notificationRepository.createNotification(
                        recipientId = commentOwnerId,
                        type = "reply",
                        postId = postId,
                        commentId = comment.id
                    )
                }
            }
            
            // The getCommentsForPost flow might automatically emit if we set up realtime, 
            // but we are using select which doesn't auto-update. So we manually reload.
            loadPostDetails(postId)
        }
    }

    fun toggleLike(postId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            // Optimistic update
            val currentPost = _post.value
            if (currentPost != null) {
                val hasLiked = currentPost.likedBy[userId] == true
                val newLikedBy = currentPost.likedBy.toMutableMap()
                if (hasLiked) newLikedBy.remove(userId) else newLikedBy[userId] = true
                _post.value = currentPost.copy(likesCount = newLikedBy.size, likedBy = newLikedBy)
            }
            
            val result = postRepository.likePost(postId, userId)
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
}
