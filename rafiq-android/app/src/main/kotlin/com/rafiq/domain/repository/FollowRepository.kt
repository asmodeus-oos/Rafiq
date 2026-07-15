package com.rafiq.domain.repository

import kotlinx.coroutines.flow.Flow
import com.rafiq.domain.model.User

interface FollowRepository {
    suspend fun followUser(targetUserId: String)
    suspend fun unfollowUser(targetUserId: String)
    suspend fun isFollowing(targetUserId: String): Boolean
    suspend fun getFollowersCount(userId: String): Int
    suspend fun getFollowingCount(userId: String): Int
    suspend fun getFollowers(userId: String): List<User>
    suspend fun getFollowing(userId: String): List<User>
}
