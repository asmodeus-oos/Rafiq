package com.rafiq.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class RoomType { OPEN, PRIVATE, DATING, VIP, GAMING, MUSIC, PODCAST }

@Serializable
enum class ParticipantRole { HOST, CO_HOST, MODERATOR, SPEAKER, LISTENER }

@Serializable
data class RoomParticipant(
    val userId: String,
    val role: ParticipantRole = ParticipantRole.LISTENER,
    @SerialName("is_muted") val isMuted: Boolean = true,
    @SerialName("hand_raised") val isHandRaised: Boolean = false,
    @SerialName("joined_at") val joinedAt: Long = System.currentTimeMillis()
)

@Serializable
data class VoiceRoom(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    @SerialName("host_id") val hostId: String,
    @SerialName("room_type") val type: RoomType = RoomType.OPEN,
    @SerialName("cover_url") val coverUrl: String? = null,
    val tags: List<String> = emptyList(),
    
    @SerialName("participant_count") val participantCount: Int = 0,
    @SerialName("max_participants") val maxParticipants: Int = 100,
    
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    
    // Live State (Transient for Real-time)
    @kotlinx.serialization.Transient var participants: List<RoomParticipant> = emptyList(),
    @kotlinx.serialization.Transient var activeSpeakers: Int = 0
)
