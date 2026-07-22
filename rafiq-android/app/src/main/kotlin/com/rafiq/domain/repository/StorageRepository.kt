package com.rafiq.domain.repository

import java.io.File

interface StorageRepository {
    suspend fun uploadAvatar(userId: String, imageFile: File): Result<String>
    suspend fun uploadPostImage(userId: String, imageFile: File): Result<String>
}
