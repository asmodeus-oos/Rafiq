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

data class PostFeedUiState(
    val posts: List<Post> = emptyList(),
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

                // Annotate which ones the current user has liked
                val annotatedPosts = if (currentUserId != null && posts.isNotEmpty()) {
                    val postIds = posts.map { it.id }
                    val likedIds = try {
                        supabaseClient.postgrest["likes"]
                            .select { filter { isIn("post_id", postIds); eq("user_id", currentUserId) } }
                            .decodeList<com.rafiq.domain.model.Like>()
                            .map { it.postId }.toSet()
                    } catch (e: Exception) { emptySet() }

                    posts.map { post ->
                        if (post.id in likedIds) {
                            post.apply { likedBy = mapOf(currentUserId to true) }
                        } else post
                    }
                } else posts

                _uiState.value = _uiState.value.copy(posts = annotatedPosts, isLoading = false)
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
