package com.rafiq.data.repository

import com.rafiq.domain.model.User
import com.rafiq.domain.model.VoiceRoom
import com.rafiq.domain.model.SwipeAction
import com.rafiq.domain.model.UserSwipe
import com.rafiq.domain.repository.DiscoveryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import io.github.jan.supabase.auth.auth
import java.util.UUID

class DiscoveryRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : DiscoveryRepository {

    private suspend fun getSwipedTargetIds(currentUserId: String): Set<String> {
        return try {
            supabaseClient.postgrest["user_swipes"]
                .select(Columns.ALL) { filter { eq("swiper_id", currentUserId) } }
                .decodeList<UserSwipe>()
                .map { it.targetId }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private suspend fun getEligibleUsers(): List<User> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
        val users = supabaseClient.postgrest["users"]
            .select(Columns.ALL)
            .decodeList<User>()
            .filter { currentUserId == null || it.id != currentUserId }

        return if (currentUserId == null) {
            users
        } else {
            val swipedIds = getSwipedTargetIds(currentUserId)
            users.filterNot { it.id in swipedIds }
        }
    }

    override suspend fun getDailyMatch(): Result<Pair<User, Int>> {
        return try {
            val users = getEligibleUsers()
            
            if (users.isNotEmpty()) {
                Result.success(users.random() to (85..99).random())
            } else {
                Result.failure(Exception("No matches found"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getNearbyPeople(latitude: Double, longitude: Double, radiusKm: Int): Result<List<User>> {
        // Implementation using PostGIS or simple filtering
        return Result.success(emptyList())
    }

    override suspend fun getTrendingRooms(): Flow<List<VoiceRoom>> = flow {
        try {
            val rooms = supabaseClient.postgrest["voice_rooms"]
                .select(Columns.ALL) {
                    filter { eq("is_active", true) }
                }
                .decodeList<VoiceRoom>()
                .sortedByDescending { it.participantCount }

            emit(rooms)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getRecommendedPartners(): Result<List<User>> {
        return try {
            Result.success(getEligibleUsers().shuffled().take(10))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun recordSwipe(targetUserId: String, action: SwipeAction): Result<Unit> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return Result.failure(Exception("User not authenticated"))
        return try {
            if (targetUserId.isBlank() || targetUserId == currentUserId) {
                return Result.success(Unit)
            }
            supabaseClient.postgrest["user_swipes"].insert(
                mapOf(
                    "id" to UUID.randomUUID().toString(),
                    "swiper_id" to currentUserId,
                    "target_id" to targetUserId,
                    "action" to action.name
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
