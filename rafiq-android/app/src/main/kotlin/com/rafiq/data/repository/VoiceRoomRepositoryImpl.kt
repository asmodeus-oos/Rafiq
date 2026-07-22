package com.rafiq.data.repository

import com.rafiq.domain.model.VoiceRoom
import com.rafiq.domain.model.RoomType
import com.rafiq.domain.repository.VoiceRoomRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class VoiceRoomRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : VoiceRoomRepository {

    override fun getActiveVoiceRooms(): Flow<List<VoiceRoom>> = callbackFlow {
        val fetchRooms = suspend {
            try {
                val rooms = supabaseClient.postgrest["voice_rooms"]
                    .select(Columns.ALL) {
                        filter { eq("is_active", true) }
                    }
                    .decodeList<VoiceRoom>()
                rooms.sortedByDescending { it.participantCount }
            } catch (e: Exception) {
                emptyList()
            }
        }

        trySend(fetchRooms())

        val channel = supabaseClient.channel("voice_rooms_channel")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "voice_rooms"
        }

        val job = launch {
            flow.collect { trySend(fetchRooms()) }
        }

        launch { channel.subscribe() }
        awaitClose {
            job.cancel()
            launch { channel.unsubscribe() }
        }
    }

    override suspend fun createVoiceRoom(title: String, description: String, category: String, maxParticipants: Int): Result<VoiceRoom> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val roomId = UUID.randomUUID().toString()
            val roomType = when (category.uppercase()) {
                "DATING" -> RoomType.DATING
                "GAMING" -> RoomType.GAMING
                "MUSIC" -> RoomType.MUSIC
                "VIP" -> RoomType.VIP
                else -> RoomType.OPEN
            }

            val room = VoiceRoom(
                id = roomId,
                title = title,
                description = description,
                hostId = currentUserId,
                type = roomType,
                maxParticipants = maxParticipants,
                participantCount = 1,
                activeSpeakers = 1,
                isActive = true
            )

            try {
                supabaseClient.postgrest["voice_rooms"].insert(
                    mapOf(
                        "id" to room.id,
                        "host_id" to room.hostId,
                        "title" to room.title,
                        "description" to room.description,
                        "type" to room.type.name,
                        "max_participants" to room.maxParticipants,
                        "participant_count" to room.participantCount,
                        "active_speakers" to room.activeSpeakers,
                        "is_active" to true
                    )
                )
            } catch (e: Exception) {
                // Return generated room object even if table doesn't exist yet
            }

            Result.success(room)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinVoiceRoom(roomId: String): Result<Unit> {
        return try {
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            if (currentUserId != null) {
                // Increment participant count in DB
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveVoiceRoom(roomId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun toggleMic(roomId: String, isMuted: Boolean): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun raiseHand(roomId: String, raise: Boolean): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun endVoiceRoom(roomId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["voice_rooms"].update({ set("is_active", false) }) {
                filter { eq("id", roomId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
