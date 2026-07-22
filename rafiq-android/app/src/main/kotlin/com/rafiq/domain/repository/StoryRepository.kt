package com.rafiq.domain.repository

import com.rafiq.domain.model.Story
import com.rafiq.domain.model.StoryGroup
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getActiveStories(): Flow<List<StoryGroup>>
    suspend fun createStory(imageBytes: ByteArray?, audioBytes: ByteArray?, caption: String?): Result<Story>
    suspend fun markStoryAsViewed(storyId: String)
    suspend fun deleteStory(storyId: String): Result<Unit>
}
