package com.rafiq.data.repository

import com.rafiq.domain.model.Post
import com.rafiq.domain.model.Comment
import com.rafiq.domain.repository.PostRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : PostRepository {

    override fun getPostsForUser(userId: String): Flow<List<Post>> = callbackFlow {
        val fetchPosts = suspend {
            try {
                val posts = supabaseClient.postgrest["posts"]
                    .select(Columns.ALL) { filter { eq("user_id", userId) } }
                    .decodeList<Post>()
                
                val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
                if (currentUserId != null && posts.isNotEmpty()) {
                    val postIds = posts.map { it.id }
                    val likes = supabaseClient.postgrest["likes"]
                        .select { filter { isIn("post_id", postIds); eq("user_id", currentUserId) } }
                        .decodeList<com.rafiq.domain.model.Like>()
                
                    val likedPostIds = likes.map { it.postId }.toSet()
                    posts.forEach { 
                        if (it.id in likedPostIds) {
                            it.likedBy = mapOf(currentUserId to true)
                        }
                    }
                }
                posts.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList<Post>()
            }
        }

        trySend(fetchPosts())

        val channel = supabaseClient.channel("posts_$userId")
        
        val postsFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "posts"
            filter("user_id", FilterOperator.EQ, userId)
        }
        
        val likesFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "likes"
        }

        val postsJob = launch {
            postsFlow.collect { trySend(fetchPosts()) }
        }
        
        val likesJob = launch {
            likesFlow.collect { action ->
                // Check if this like action is relevant to the posts we are watching
                // For simplicity, we just re-fetch everything on any like change
                trySend(fetchPosts())
            }
        }

        launch { channel.subscribe() }

        awaitClose { 
            postsJob.cancel()
            likesJob.cancel()
            launch { channel.unsubscribe() }
        }
    }

    override suspend fun getPostById(postId: String): Result<Post> {
        return try {
            val post = supabaseClient.postgrest["posts"]
                .select(Columns.ALL) { filter { eq("id", postId) } }
                .decodeSingle<Post>()
                
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            if (currentUserId != null) {
                val likeCount = supabaseClient.postgrest["likes"]
                    .select { filter { eq("post_id", postId); eq("user_id", currentUserId) } }
                    .countOrNull() ?: 0
                if (likeCount > 0) {
                    post.likedBy = mapOf(currentUserId to true)
                }
            }
            Result.success(post)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun createPost(post: Post): Result<Unit> {
        return try {
            supabaseClient.postgrest["posts"].insert(post)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updatePost(postId: String, newTextContent: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["posts"].update(
                { set("text_content", newTextContent) }
            ) { filter { eq("id", postId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["posts"].delete { filter { eq("id", postId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun likePost(postId: String, userId: String): Result<Unit> {
        return try {
            // FIX: wrap (countOrNull() ?: 0) to avoid operator precedence bug
            // (previously parsed as countOrNull() ?: (0 > 0) = always false)
            val alreadyLiked = (supabaseClient.postgrest["likes"]
                .select { filter { eq("post_id", postId); eq("user_id", userId) } }
                .countOrNull() ?: 0) > 0
            
            if (alreadyLiked) {
                supabaseClient.postgrest["likes"].delete {
                    filter { eq("post_id", postId); eq("user_id", userId) }
                }
            } else {
                supabaseClient.postgrest["likes"].insert(
                    mapOf("post_id" to postId, "user_id" to userId)
                )
            }
            
            // Update post likes_count to reflect the actual count in the DB
            val newLikesCount = (supabaseClient.postgrest["likes"]
                .select { filter { eq("post_id", postId) } }
                .countOrNull() ?: 0).toInt()
            supabaseClient.postgrest["posts"].update(
                { set("likes_count", newLikesCount) }
            ) { filter { eq("id", postId) } }
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getPostLikers(postId: String): Result<List<com.rafiq.domain.model.User>> {
        return try {
            val likes = supabaseClient.postgrest["likes"]
                .select(Columns.ALL) { filter { eq("post_id", postId) } }
                .decodeList<com.rafiq.domain.model.Like>()
                
            val userIds = likes.map { it.userId }
            val users = if (userIds.isNotEmpty()) {
                supabaseClient.postgrest["users"]
                    .select(Columns.ALL) { filter { isIn("id", userIds) } }
                    .decodeList<com.rafiq.domain.model.User>()
            } else {
                emptyList()
            }
            
            Result.success(users)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    override fun getCommentsForPost(postId: String): Flow<List<Comment>> = callbackFlow {
        val fetchComments = suspend {
            try {
                println("RAFIQ_DEBUG: getCommentsForPost fetching for post $postId")
                val comments = supabaseClient.postgrest["comments"]
                    .select(Columns.ALL) { 
                        filter { 
                            // Safety: if postId is not a valid UUID and post_id column is UUID, this may fail.
                            eq("post_id", postId) 
                        } 
                    }
                    .decodeList<Comment>()
                println("RAFIQ_DEBUG: getCommentsForPost fetched ${comments.size} comments for post $postId")
                comments.sortedBy { it.timestamp }
            } catch (e: Exception) {
                println("RAFIQ_DEBUG: getCommentsForPost fetch error: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }

        trySend(fetchComments())

        val channel = supabaseClient.channel("comments_$postId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "comments"
            filter("post_id", FilterOperator.EQ, postId)
        }

        val job = launch {
            changeFlow.collect { action ->
                println("RAFIQ_DEBUG: Received real-time action: $action")
                trySend(fetchComments())
            }
        }

        launch { channel.subscribe() }

        awaitClose { 
            println("RAFIQ_DEBUG: Closing comments real-time channel for $postId")
            job.cancel() 
            launch { channel.unsubscribe() }
        }
    }

    override suspend fun createComment(comment: Comment): Result<Unit> {
        android.util.Log.d("RAFIQ_DEBUG", "createComment: START - PostId: ${comment.postId}, UserId: ${comment.userId}")
        return try {
            val generatedId = if (!comment.id.isNullOrBlank()) comment.id else java.util.UUID.randomUUID().toString()
            val timestamp = if (comment.timestamp > 0) comment.timestamp else System.currentTimeMillis()
            
            val commentJson = buildJsonObject {
                put("id", generatedId)
                put("post_id", comment.postId)
                if (!comment.userId.isNullOrBlank()) {
                    put("user_id", comment.userId)
                }
                put("text_content", comment.textContent)
                put("timestamp", timestamp)
                if (!comment.parentId.isNullOrBlank()) {
                    put("parent_id", comment.parentId)
                }
            }
            
            android.util.Log.d("RAFIQ_DEBUG", "createComment: Inserting JSON: $commentJson")
            
            // Supabase 3.0.x insertion
            supabaseClient.postgrest["comments"].insert(commentJson)
            
            android.util.Log.d("RAFIQ_DEBUG", "createComment: INSERT CALL FINISHED (No Exception)")
            
            // Recalculate actual visible count - Wrap in try/catch to prevent failure if schema mismatch exists
            try {
                val postResult = getPostById(comment.postId)
                val post = postResult.getOrNull()
                if (post != null) {
                    val allComments = supabaseClient.postgrest["comments"]
                        .select(Columns.ALL) { filter { eq("post_id", post.id) } }
                        .decodeList<Comment>()
                    
                    // Exclude orphans
                    val allIds = allComments.map { it.id }.toSet()
                    val validCount = allComments.count { it.parentId == null || it.parentId in allIds }
                    
                    android.util.Log.d("RAFIQ_DEBUG", "createComment: Updating post ${post.id} comments_count to $validCount")
                    supabaseClient.postgrest["posts"].update(
                        { set("comments_count", validCount) }
                    ) { filter { eq("id", post.id) } }
                }
            } catch (e: Exception) {
                android.util.Log.e("RAFIQ_DEBUG", "createComment: Secondary update error (ignored) - ${e.message}")
            }
            
            android.util.Log.d("RAFIQ_DEBUG", "createComment: SUCCESS")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("RAFIQ_DEBUG", "createComment: FATAL ERROR - ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateComment(commentId: String, newText: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["comments"].update(
                { set("text_content", newText) }
            ) {
                filter { eq("id", commentId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
            // We might want to decrement comments count on the post, but for simplicity
            // or if we have a cascade, we just delete the comment first.
            // Getting the post ID from the comment to decrement count.
            val comment = supabaseClient.postgrest["comments"]
                .select(Columns.ALL) { filter { eq("id", commentId) } }
                .decodeSingleOrNull<Comment>()
            
            comment?.let {
                val allPostComments = supabaseClient.postgrest["comments"]
                    .select(Columns.ALL) { filter { eq("post_id", it.postId) } }
                    .decodeList<Comment>()
                
                val idsToDelete = mutableSetOf(commentId)
                
                // Find existing orphans
                val allIds = allPostComments.map { it.id }.toSet()
                allPostComments.forEach { c ->
                    if (!c.parentId.isNullOrBlank() && c.parentId !in allIds) {
                        idsToDelete.add(c.id)
                    }
                }
                
                supabaseClient.postgrest["comments"].delete {
                    filter { isIn("id", idsToDelete.toList()) }
                }
                
                // Update post count
                val remainingCount = allPostComments.size - idsToDelete.size
                supabaseClient.postgrest["posts"].update(
                    { set("comments_count", if (remainingCount > 0) remainingCount else 0) }
                ) { filter { eq("id", it.postId) } }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
