package com.rafiq.di

import com.rafiq.data.repository.PostRepositoryImpl
import com.rafiq.domain.repository.PostRepository
import com.rafiq.data.repository.ChatRepositoryImpl
import com.rafiq.domain.repository.ChatRepository
import com.rafiq.data.repository.NotificationRepositoryImpl
import com.rafiq.domain.repository.NotificationRepository
import com.rafiq.data.repository.FollowRepositoryImpl
import com.rafiq.domain.repository.FollowRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindFollowRepository(
        followRepositoryImpl: FollowRepositoryImpl
    ): FollowRepository
}
