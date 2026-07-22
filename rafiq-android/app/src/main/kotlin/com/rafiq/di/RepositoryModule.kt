package com.rafiq.di

import com.rafiq.data.repository.ChatRepositoryImpl
import com.rafiq.data.repository.DiscoveryRepositoryImpl
import com.rafiq.data.repository.FollowRepositoryImpl
import com.rafiq.data.repository.NotificationRepositoryImpl
import com.rafiq.data.repository.PostRepositoryImpl
import com.rafiq.data.repository.StoryRepositoryImpl
import com.rafiq.data.repository.VoiceMatchRepositoryImpl
import com.rafiq.data.repository.VoiceRoomRepositoryImpl
import com.rafiq.domain.repository.ChatRepository
import com.rafiq.domain.repository.DiscoveryRepository
import com.rafiq.domain.repository.FollowRepository
import com.rafiq.domain.repository.NotificationRepository
import com.rafiq.domain.repository.PostRepository
import com.rafiq.domain.repository.StoryRepository
import com.rafiq.domain.repository.VoiceMatchRepository
import com.rafiq.domain.repository.VoiceRoomRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds @Singleton
    abstract fun bindFollowRepository(impl: FollowRepositoryImpl): FollowRepository

    @Binds @Singleton
    abstract fun bindDiscoveryRepository(impl: DiscoveryRepositoryImpl): DiscoveryRepository

    @Binds @Singleton
    abstract fun bindStoryRepository(impl: StoryRepositoryImpl): StoryRepository

    @Binds @Singleton
    abstract fun bindVoiceRoomRepository(impl: VoiceRoomRepositoryImpl): VoiceRoomRepository

    @Binds @Singleton
    abstract fun bindVoiceMatchRepository(impl: VoiceMatchRepositoryImpl): VoiceMatchRepository
}
