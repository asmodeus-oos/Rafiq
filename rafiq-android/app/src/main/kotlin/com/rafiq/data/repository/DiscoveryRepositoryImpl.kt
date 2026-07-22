package com.rafiq.data.repository

import com.rafiq.domain.model.User
import com.rafiq.domain.model.VoiceRoom
import com.rafiq.domain.repository.DiscoveryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import io.github.jan.supabase.auth.auth

class DiscoveryRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : DiscoveryRepository {

    override suspend fun getDailyMatch(): Result<Pair<User, Int>> {
        return try {
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            val users = supabaseClient.postgrest["users"]
                .select(Columns.ALL)
                .decodeList<User>()
                .filter { currentUserId == null || it.id != currentUserId }
            
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
            // Fetching active voice rooms
            // Mocking for now as table might not exist
            val rooms = listOf(
                VoiceRoom(id = "1", title = "Late Night Chill", description = "Talking about tech and music", hostId = "h1", participantCount = 12, activeSpeakers = 3),
                VoiceRoom(id = "2", title = "Blind Dating Cairo", description = "Finding your companion", hostId = "h2", participantCount = 45, activeSpeakers = 2),
                VoiceRoom(id = "3", title = "Gaming Lounge", description = "Matchmaking for Valorant", hostId = "h3", participantCount = 8, activeSpeakers = 5)
            )
            emit(rooms)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getRecommendedPartners(): Result<List<User>> {
        return try {
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            val users = supabaseClient.postgrest["users"]
                .select(Columns.ALL)
                .decodeList<User>()
                .filter { currentUserId == null || it.id != currentUserId }
            Result.success(users.shuffled().take(10))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
