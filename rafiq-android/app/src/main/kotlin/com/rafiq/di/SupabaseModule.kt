package com.rafiq.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.auth.Auth
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://ioxikjgdszqtufqgfbqt.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlveGlramdkc3pxdHVmcWdmYnF0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQwNTY1OTIsImV4cCI6MjA5OTYzMjU5Mn0.Hu1gcBXgx-rgbfSyWtbubVXIPG7DVgVKfKdg9n98mkI"
        ) {
            @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
            defaultSerializer = io.github.jan.supabase.serializer.KotlinXSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
                coerceInputValues = true
            })
            install(Postgrest)
            install(Storage)
            install(Realtime)
            install(Auth) {
                scheme = "rafiq"
                host = "login"
            }
        }
    }
}
