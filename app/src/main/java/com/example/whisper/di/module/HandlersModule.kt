package com.example.whisper.di.module

import com.example.whisper.data.handlers.ConnectionHandler
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HandlersModule {

    @Singleton
    @Provides
    fun provideConnectionHandler(
        contactsRepository: ContactsRepository,
        recentChatsRepository: RecentChatsRepository,
        userRepository: UserRepository
    ): ConnectionHandler = ConnectionHandler(
        contactsRepository,
        recentChatsRepository,
        userRepository
    )
}