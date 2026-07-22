package com.rafiq.data.repository

import com.rafiq.domain.model.Story
import com.rafiq.domain.model.StoryGroup
import com.rafiq.domain.model.User
import com.rafiq.domain.repository.StoryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class StoryRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : StoryRepository {

    override fun getActiveStories(): Flow<List<StoryGroup>> = callbackFlow {
        val fetchStories = suspend {
            try {
                // Fetch unexpired stories
                val stories = supabaseClient.postgrest["stories"]
                    .select(Columns.list("*, users!stories_user_id_fkey(*)"))
                    .decodeList<Story>()
                    
                // Group by user
                val grouped = stories.groupBy { it.user?.id ?: it.userId }
                grouped.mapNotNull { (userId, userStories) ->
                    val user = userStories.firstOrNull()?.user ?: return@mapNotNull null
                    StoryGroup(
                        user = user,
                        stories = userStories.sortedByDescending { it.createdAt },
                        hasUnseenStories = userStories.any { !it.isViewed }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

        trySend(fetchStories())

        val channel = supabaseClient.channel("stories_channel")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "stories"
        }

        val job = launch {
            flow.collect { trySend(fetchStories()) }
        }

        launch { channel.subscribe() }
        awaitClose {
            job.cancel()
            launch { channel.unsubscribe() }
        }
    }

    override suspend fun createStory(imageBytes: ByteArray?, audioBytes: ByteArray?, caption: String?): Result<Story> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val storage = supabaseClient.storage.from("media")
            var imageUrl: String? = null
            var audioUrl: String? = null

            if (imageBytes != null) {
                val path = "stories/images/${UUID.randomUUID()}.jpg"
                storage.upload(path, imageBytes)
                imageUrl = storage.publicUrl(path)
            }

            if (audioBytes != null) {
                val path = "stories/audio/${UUID.randomUUID()}.mp4"
                storage.upload(path, audioBytes)
                audioUrl = storage.publicUrl(path)
            }

            val story = Story(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                imageUrl = imageUrl,
                audioUrl = audioUrl,
                caption = caption
            )

            supabaseClient.postgrest["stories"].insert(
                mapOf(
                    "id" to story.id,
                    "user_id" to story.userId,
                    "image_url" to story.imageUrl,
                    "audio_url" to story.audioUrl,
                    "caption" to story.caption
                )
            )

            Result.success(story)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun markStoryAsViewed(storyId: String) {
        // Track locally or in DB view log table
    }

    override suspend fun deleteStory(storyId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["stories"].delete {
                filter { eq("id", storyId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
