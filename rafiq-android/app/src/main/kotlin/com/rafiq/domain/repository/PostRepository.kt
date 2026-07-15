package com.rafiq.domain.repository

import com.rafiq.domain.model.Post
import com.rafiq.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPostsForUser(userId: String): Flow<List<Post>>
    suspend fun getPostById(postId: String): Result<Post>
    suspend fun createPost(post: Post): Result<Unit>
    suspend fun updatePost(postId: String, newTextContent: String): Result<Unit>
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun likePost(postId: String, userId: String): Result<Unit>
    
    fun getCommentsForPost(postId: String): Flow<List<Comment>>
    suspend fun createComment(comment: Comment): Result<Unit>
    suspend fun updateComment(commentId: String, newText: String): Result<Unit>
    suspend fun deleteComment(commentId: String): Result<Unit>
}
