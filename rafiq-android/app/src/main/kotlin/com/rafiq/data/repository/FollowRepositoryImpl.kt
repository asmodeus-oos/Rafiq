package com.rafiq.data.repository

import com.rafiq.domain.repository.FollowRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import javax.inject.Inject

class FollowRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : FollowRepository {

    override suspend fun followUser(targetUserId: String) {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        if (currentUserId == targetUserId) return
        
        try {
            val followData = mapOf(
                "follower_id" to currentUserId,
                "following_id" to targetUserId
            )
            supabaseClient.postgrest["followers"].insert(followData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun unfollowUser(targetUserId: String) {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        try {
            supabaseClient.postgrest["followers"].delete {
                filter {
                    eq("follower_id", currentUserId)
                    eq("following_id", targetUserId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun isFollowing(targetUserId: String): Boolean {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return false
        return try {
            val count = supabaseClient.postgrest["followers"].select {
                filter {
                    eq("follower_id", currentUserId)
                    eq("following_id", targetUserId)
                }
                count(Count.EXACT)
            }.countOrNull() ?: 0
            count > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getFollowersCount(userId: String): Int {
        return try {
            val count = supabaseClient.postgrest["followers"].select {
                filter { eq("following_id", userId) }
                count(Count.EXACT)
            }.countOrNull() ?: 0
            count.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override suspend fun getFollowingCount(userId: String): Int {
        return try {
            val count = supabaseClient.postgrest["followers"].select {
                filter { eq("follower_id", userId) }
                count(Count.EXACT)
            }.countOrNull() ?: 0
            count.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    override suspend fun getFollowers(userId: String): List<com.rafiq.domain.model.User> {
        return try {
            val followersData = supabaseClient.postgrest["followers"].select(io.github.jan.supabase.postgrest.query.Columns.list("follower_id")) {
                filter { eq("following_id", userId) }
            }.decodeList<Map<String, String>>()
            
            val followerIds = followersData.mapNotNull { it["follower_id"] }
            if (followerIds.isEmpty()) return emptyList()
            
            supabaseClient.postgrest["users"].select {
                filter { isIn("id", followerIds) }
            }.decodeList<com.rafiq.domain.model.User>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getFollowing(userId: String): List<com.rafiq.domain.model.User> {
        return try {
            val followingData = supabaseClient.postgrest["followers"].select(io.github.jan.supabase.postgrest.query.Columns.list("following_id")) {
                filter { eq("follower_id", userId) }
            }.decodeList<Map<String, String>>()
            
            val followingIds = followingData.mapNotNull { it["following_id"] }
            if (followingIds.isEmpty()) return emptyList()
            
            supabaseClient.postgrest["users"].select {
                filter { isIn("id", followingIds) }
            }.decodeList<com.rafiq.domain.model.User>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
