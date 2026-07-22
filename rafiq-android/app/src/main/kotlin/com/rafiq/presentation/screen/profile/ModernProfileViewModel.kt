package com.rafiq.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import com.rafiq.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import com.rafiq.domain.repository.PostRepository
import com.rafiq.domain.repository.FollowRepository
import com.rafiq.domain.repository.NotificationRepository
import com.rafiq.domain.model.Post
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID

@HiltViewModel
class ModernProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
    private val followRepository: FollowRepository,
    private val notificationRepository: NotificationRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _suggestedUsers = MutableStateFlow<List<User>>(emptyList())
    val suggestedUsers: StateFlow<List<User>> = _suggestedUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOwner = MutableStateFlow(false)
    val isOwner: StateFlow<Boolean> = _isOwner.asStateFlow()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isFollowing = MutableStateFlow<Boolean?>(null)
    val isFollowing: StateFlow<Boolean?> = _isFollowing.asStateFlow()
    
    private val _followers = MutableStateFlow<List<User>>(emptyList())
    val followers: StateFlow<List<User>> = _followers.asStateFlow()
    
    private val _following = MutableStateFlow<List<User>>(emptyList())
    val following: StateFlow<List<User>> = _following.asStateFlow()

    private val _followersCount = MutableStateFlow(0)
    val followersCount: StateFlow<Int> = _followersCount.asStateFlow()
    
    private val _followingCount = MutableStateFlow(0)
    val followingCount: StateFlow<Int> = _followingCount.asStateFlow()

    val currentUserId: String?
        get() = supabaseClient.auth.currentUserOrNull()?.id

    init {
        val targetUserId = savedStateHandle.get<String>("userId")
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
        
        _isOwner.value = targetUserId == null || targetUserId == currentUserId
        val uidToFetch = targetUserId ?: currentUserId
        
        if (uidToFetch != null) {
            fetchUser(uidToFetch)
            fetchPosts(uidToFetch)
            fetchFollowStats(uidToFetch)
            
            // Record a profile visit notification (only fires if target is Diamond tier)
            if (targetUserId != null && targetUserId != currentUserId) {
                viewModelScope.launch {
                    notificationRepository.recordProfileVisit(targetUserId)
                }
            }
            
            // Poll for user updates (online status)
            viewModelScope.launch {
                while(isActive) {
                    kotlinx.coroutines.delay(5000) // Reduced from 1s to 5s to reduce load
                    fetchUser(uidToFetch)
                }
            }
        } else {
            _isLoading.value = false
        }
        fetchAllUsers()
        
        viewModelScope.launch {
            delay(5000)
            if (_isLoading.value) {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        _isLoading.value = true
        val targetUserId = savedStateHandle.get<String>("userId")
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
        val uidToFetch = targetUserId ?: currentUserId
        if (uidToFetch != null) {
            fetchUser(uidToFetch)
            fetchPosts(uidToFetch)
            fetchFollowStats(uidToFetch)
        }
        fetchAllUsers()
    }

    suspend fun createPost(textContent: String, imageBytes: ByteArray?, audioBytes: ByteArray?): Boolean {
        val uid = supabaseClient.auth.currentUserOrNull()?.id ?: return false
        return try {
            val postId = UUID.randomUUID().toString()
            var finalImageUrl: String? = null
            var finalAudioUrl: String? = null
            
            val storage = supabaseClient.storage.from("media")

            if (imageBytes != null) {
                val currentUserData = _user.value
                val shouldCompress = currentUserData?.tier != com.rafiq.domain.model.Tier.PLATINUM && 
                                     currentUserData?.tier != com.rafiq.domain.model.Tier.DIAMOND && 
                                     currentUserData?.isOwner != true
                val finalBytes = if (shouldCompress) com.rafiq.util.ImageCompressor.compressImage(imageBytes) else imageBytes
                val path = "posts/images/${UUID.randomUUID()}.jpg"
                storage.upload(path, finalBytes)
                finalImageUrl = storage.publicUrl(path)
            }

            if (audioBytes != null) {
                val path = "posts/audio/${UUID.randomUUID()}.mp4"
                storage.upload(path, audioBytes)
                finalAudioUrl = storage.publicUrl(path)
            }
            
            val newPost = Post(
                id = postId,
                userId = uid,
                textContent = textContent,
                imageUrl = finalImageUrl,
                audioUrl = finalAudioUrl,
                timestamp = System.currentTimeMillis()
            )
            val result = postRepository.createPost(newPost)
            if (result.isSuccess) {
                fetchPosts(uid)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun fetchPosts(uid: String) {
        viewModelScope.launch {
            postRepository.getPostsForUser(uid).collect { fetchedPosts ->
                _posts.value = fetchedPosts
            }
        }
    }

    private fun fetchUser(uid: String) {
        viewModelScope.launch {
            try {
                val fetchedUser = supabaseClient.postgrest["users"]
                    .select(Columns.ALL) { filter { eq("id", uid) } }
                    .decodeSingleOrNull<User>()
                if (fetchedUser != null) {
                    _user.value = fetchedUser
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    private fun fetchAllUsers() {
        viewModelScope.launch {
            try {
                val currentUid = supabaseClient.auth.currentUserOrNull()?.id
                val usersList = supabaseClient.postgrest["users"]
                    .select(Columns.ALL)
                    .decodeList<User>()
                    .filter { it.id != currentUid }
                
                _suggestedUsers.value = usersList.shuffled()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun toggleLike(postId: String) {
        val uid = supabaseClient.auth.currentUserOrNull()?.id ?: return
        
        // Optimistic update
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val hasLiked = post.likedBy[uid] == true
                val newLikedBy = post.likedBy.toMutableMap()
                if (hasLiked) {
                    newLikedBy.remove(uid)
                } else {
                    newLikedBy[uid] = true
                }
                val newLikeCount = if (hasLiked) (post.likesCount - 1).coerceAtLeast(0) else post.likesCount + 1
                post.copy(
                    likesCount = newLikeCount,
                    likedBy = newLikedBy
                )
            } else post
        }

        viewModelScope.launch {
            val result = postRepository.likePost(postId, uid)
            if (result.isFailure) {
                // Revert if failed (simple refetch for reliability)
                fetchPosts(uid)
            }
        }
    }

    fun editPost(postId: String, newText: String) {
        viewModelScope.launch {
            postRepository.updatePost(postId, newText)
            _user.value?.id?.let { fetchPosts(it) }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(postId)
            _user.value?.id?.let { fetchPosts(it) }
        }
    }

    suspend fun updateProfile(updatedUser: User, avatarBytes: ByteArray?, coverBytes: ByteArray?): Boolean {
        return try {
            var avatarUrl = updatedUser.avatar
            var coverUrl = updatedUser.coverPhoto
            val storage = supabaseClient.storage.from("media")
            
            val shouldCompress = updatedUser.tier != com.rafiq.domain.model.Tier.PLATINUM && 
                                 updatedUser.tier != com.rafiq.domain.model.Tier.DIAMOND && 
                                 !updatedUser.isOwner

            if (avatarBytes != null) {
                val finalBytes = if (shouldCompress) com.rafiq.util.ImageCompressor.compressImage(avatarBytes) else avatarBytes
                val avatarPath = "avatars/${UUID.randomUUID()}.jpg"
                storage.upload(avatarPath, finalBytes)
                avatarUrl = storage.publicUrl(avatarPath)
            }
            if (coverBytes != null) {
                val finalBytes = if (shouldCompress) com.rafiq.util.ImageCompressor.compressImage(coverBytes) else coverBytes
                val coverPath = "covers/${UUID.randomUUID()}.jpg"
                storage.upload(coverPath, finalBytes)
                coverUrl = storage.publicUrl(coverPath)
            }
            
            val finalUser = updatedUser.copy(avatar = avatarUrl, coverPhoto = coverUrl)
            
            supabaseClient.postgrest["users"].update(finalUser) {
                filter { eq("id", finalUser.id) }
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun fetchFollowStats(userId: String) {
        viewModelScope.launch {
            _followersCount.value = followRepository.getFollowersCount(userId)
            _followingCount.value = followRepository.getFollowingCount(userId)
            if (!_isOwner.value) {
                _isFollowing.value = followRepository.isFollowing(userId)
            }
            _followers.value = followRepository.getFollowers(userId)
            _following.value = followRepository.getFollowing(userId)
        }
    }

    fun toggleFollow() {
        val targetUser = _user.value ?: return
        viewModelScope.launch {
            if (_isFollowing.value == true) {
                followRepository.unfollowUser(targetUser.id)
                _isFollowing.value = false
                _followersCount.value -= 1
            } else if (_isFollowing.value == false) {
                followRepository.followUser(targetUser.id)
                _isFollowing.value = true
                _followersCount.value += 1
                notificationRepository.createNotification(
                    recipientId = targetUser.id,
                    type = "follow"
                )
            }
        }
    }

    suspend fun fetchLikers(postId: String): Result<List<User>> {
        return postRepository.getPostLikers(postId)
    }
}
