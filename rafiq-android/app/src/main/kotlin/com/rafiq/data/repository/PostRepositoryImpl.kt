package com.rafiq.data.repository

import com.rafiq.domain.model.Post
import com.rafiq.domain.model.Comment
import com.rafiq.domain.repository.PostRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import java.util.UUID
import java.io.File
import io.github.jan.supabase.postgrest.query.Columns

class PostRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : PostRepository {

    override fun getPostsForUser(userId: String): Flow<List<Post>> = flow {
        try {
            val posts = supabaseClient.postgrest["posts"]
                .select(Columns.ALL) { filter { eq("user_id", userId) } }
                .decodeList<Post>()
            emit(posts.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override suspend fun getPostById(postId: String): Result<Post> {
        return try {
            val post = supabaseClient.postgrest["posts"]
                .select(Columns.ALL) { filter { eq("id", postId) } }
                .decodeSingle<Post>()
            Result.success(post)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun createPost(post: Post): Result<Unit> {
        return try {
            var finalImageUrl = post.imageUrl
            var finalAudioUrl = post.audioUrl

            val storage = supabaseClient.storage.from("media")

            if (post.imageUrl != null && !post.imageUrl.startsWith("http")) {
                val path = "posts/images/${UUID.randomUUID()}.jpg"
                if (post.imageUrl.startsWith("data:image")) {
                    val base64 = post.imageUrl.substringAfter("base64,")
                    val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                    storage.upload(path, bytes)
                    finalImageUrl = storage.publicUrl(path)
                } else {
                    val file = File(post.imageUrl.replace("file://", ""))
                    if (file.exists()) {
                        storage.upload(path, file.readBytes())
                        finalImageUrl = storage.publicUrl(path)
                    }
                }
            }

            if (post.audioUrl != null && !post.audioUrl.startsWith("http")) {
                val path = "posts/audio/${UUID.randomUUID()}.mp4"
                if (post.audioUrl.startsWith("data:audio")) {
                    val base64 = post.audioUrl.substringAfter("base64,")
                    val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                    storage.upload(path, bytes)
                    finalAudioUrl = storage.publicUrl(path)
                } else {
                    val file = File(post.audioUrl.replace("file://", ""))
                    if (file.exists()) {
                        storage.upload(path, file.readBytes())
                        finalAudioUrl = storage.publicUrl(path)
                    }
                }
            }

            val newPost = post.copy(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                imageUrl = finalImageUrl,
                audioUrl = finalAudioUrl
            )
            supabaseClient.postgrest["posts"].insert(newPost)
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
            ) {
                filter { eq("id", postId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["posts"].delete {
                filter { eq("id", postId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun likePost(postId: String, userId: String): Result<Unit> {
        return try {
            val post = supabaseClient.postgrest["posts"]
                .select(Columns.ALL) { filter { eq("id", postId) } }
                .decodeSingle<Post>()
            
            val likesMap = post.likedBy.toMutableMap()
            val hasLiked = likesMap[userId] == true
            
            if (hasLiked) {
                likesMap.remove(userId)
            } else {
                likesMap[userId] = true
            }
            val newLikesCount = likesMap.size
            
            supabaseClient.postgrest["posts"].update(
                {
                    set("liked_by", likesMap)
                    set("likes_count", newLikesCount)
                }
            ) {
                filter { eq("id", postId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    override fun getCommentsForPost(postId: String): Flow<List<Comment>> = flow {
        try {
            val comments = supabaseClient.postgrest["comments"]
                .select(Columns.ALL) { filter { eq("post_id", postId) } }
                .decodeList<Comment>()
            emit(comments.sortedBy { it.timestamp })
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override suspend fun createComment(comment: Comment): Result<Unit> {
        return try {
            val newComment = comment.copy(
                id = if (comment.id.isNotBlank()) comment.id else java.util.UUID.randomUUID().toString(),
                timestamp = if (comment.timestamp > 0) comment.timestamp else System.currentTimeMillis()
            )
            supabaseClient.postgrest["comments"].insert(newComment)
            
            // Recalculate actual visible count
            val postResult = getPostById(comment.postId)
            postResult.getOrNull()?.let { post ->
                val allComments = supabaseClient.postgrest["comments"]
                    .select(Columns.ALL) { filter { eq("post_id", post.id) } }
                    .decodeList<Comment>()
                
                // Exclude orphans
                val allIds = allComments.map { it.id }.toSet()
                val validCount = allComments.count { it.parentId == null || it.parentId in allIds }
                
                supabaseClient.postgrest["posts"].update(
                    { set("comments_count", validCount) }
                ) { filter { eq("id", post.id) } }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
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
                    if (c.parentId != null && c.parentId !in allIds) {
                        idsToDelete.add(c.id)
                    }
                }
                
                var addedNew = true
                while (addedNew) {
                    val currentSize = idsToDelete.size
                    allPostComments.forEach { c ->
                        if (c.parentId in idsToDelete) idsToDelete.add(c.id)
                    }
                    addedNew = idsToDelete.size > currentSize
                }
                
                supabaseClient.postgrest["comments"].delete {
                    filter { isIn("id", idsToDelete.toList()) }
                }

                val postResult = getPostById(it.postId)
                postResult.getOrNull()?.let { post ->
                    val remainingCount = allPostComments.size - idsToDelete.size
                    supabaseClient.postgrest["posts"].update(
                        { set("comments_count", remainingCount) }
                    ) { filter { eq("id", post.id) } }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
