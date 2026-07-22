package com.rafiq.data.repository

import com.rafiq.domain.repository.StorageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.io.File
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : StorageRepository {

    override suspend fun uploadAvatar(userId: String, imageFile: File): Result<String> {
        return try {
            val bucket = supabaseClient.storage.from("avatars")
            val fileName = "$userId/${System.currentTimeMillis()}_${imageFile.name}"
            val bytes = imageFile.readBytes()
            
            bucket.upload(fileName, bytes) {
                upsert = true
            }
            
            val publicUrl = bucket.publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun uploadPostImage(userId: String, imageFile: File): Result<String> {
        return try {
            val bucket = supabaseClient.storage.from("posts")
            val fileName = "$userId/${System.currentTimeMillis()}_${imageFile.name}"
            val bytes = imageFile.readBytes()
            
            bucket.upload(fileName, bytes)
            
            val publicUrl = bucket.publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
