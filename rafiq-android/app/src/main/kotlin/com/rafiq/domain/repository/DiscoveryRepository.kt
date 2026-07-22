package com.rafiq.domain.repository

import com.rafiq.domain.model.User
import com.rafiq.domain.model.VoiceRoom
import kotlinx.coroutines.flow.Flow

interface DiscoveryRepository {
    suspend fun getDailyMatch(): Result<Pair<User, Int>> // User and Match Score
    suspend fun getNearbyPeople(latitude: Double, longitude: Double, radiusKm: Int): Result<List<User>>
    suspend fun getTrendingRooms(): Flow<List<VoiceRoom>>
    suspend fun getRecommendedPartners(): Result<List<User>>
}
