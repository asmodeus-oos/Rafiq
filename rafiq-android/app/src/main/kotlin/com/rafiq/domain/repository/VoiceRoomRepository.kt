package com.rafiq.domain.repository

import com.rafiq.domain.model.VoiceRoom
import com.rafiq.domain.model.RoomParticipant
import kotlinx.coroutines.flow.Flow

interface VoiceRoomRepository {
    fun getActiveVoiceRooms(): Flow<List<VoiceRoom>>
    suspend fun createVoiceRoom(title: String, description: String, category: String, maxParticipants: Int): Result<VoiceRoom>
    suspend fun joinVoiceRoom(roomId: String): Result<Unit>
    suspend fun leaveVoiceRoom(roomId: String): Result<Unit>
    suspend fun toggleMic(roomId: String, isMuted: Boolean): Result<Unit>
    suspend fun raiseHand(roomId: String, raise: Boolean): Result<Unit>
    suspend fun endVoiceRoom(roomId: String): Result<Unit>
}
