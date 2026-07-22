package com.rafiq.presentation.screen.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.Post
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

import com.rafiq.domain.model.User

data class PostFeedUiState(
    val posts: List<Post> = emptyList(),
    val users: Map<String, User> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PostFeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostFeedUiState())
    val uiState: StateFlow<PostFeedUiState> = _uiState.asStateFlow()

    private val _likedPostViewModel = MutableStateFlow<com.rafiq.presentation.screen.profile.ModernProfileViewModel?>(null)

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val currentUserId = supabaseClient.auth.currentUserOrNull()?.id

                // Fetch all recent posts sorted by newest first
                val posts = supabaseClient.postgrest["posts"]
                    .select(Columns.ALL)
                    .decodeList<Post>()
                    .sortedByDescending { it.timestamp }

                val userIds = posts.map { it.userId }.filter { it.isNotBlank() }.distinct()
                val usersMap = if (userIds.isNotEmpty()) {
                    try {
                        supabaseClient.postgrest["users"]
                            .select(Columns.ALL) { filter { isIn("id", userIds) } }
                            .decodeList<User>()
                            .associateBy { it.id }
                    } catch (e: Exception) {
                        emptyMap()
                    }
                } else emptyMap()

                val postIds = posts.map { it.id }
                val likeRecords = if (postIds.isNotEmpty()) {
                    try {
                        supabaseClient.postgrest["likes"]
                            .select(Columns.ALL) { filter { isIn("post_id", postIds) } }
                            .decodeList<com.rafiq.domain.model.Like>()
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else emptyList()

                val likeCounts = likeRecords.groupingBy { it.postId }.eachCount()
                val likedIds = if (currentUserId != null) {
                    likeRecords.filter { it.userId == currentUserId }.map { it.postId }.toSet()
                } else emptySet()

                val annotatedPosts = posts.map { post ->
                    post.copy(
                        likesCount = likeCounts[post.id] ?: post.likesCount,
                        likedBy = if (post.id in likedIds && currentUserId != null) mapOf(currentUserId to true) else post.likedBy
                    )
                }

                _uiState.value = _uiState.value.copy(
                    posts = annotatedPosts,
                    users = usersMap,
                    isLoading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun likePost(postId: String) {
        val uid = supabaseClient.auth.currentUserOrNull()?.id ?: return

        // Optimistic update
        _uiState.value = _uiState.value.copy(
            posts = _uiState.value.posts.map { post ->
                if (post.id == postId) {
                    val hasLiked = post.likedBy[uid] == true
                    val newLikedBy = post.likedBy.toMutableMap()
                    if (hasLiked) newLikedBy.remove(uid) else newLikedBy[uid] = true
                    post.copy(likesCount = post.likesCount + if (hasLiked) -1 else 1, likedBy = newLikedBy)
                } else post
            }
        )

        viewModelScope.launch {
            postRepository.likePost(postId, uid)
        }
    }
}
