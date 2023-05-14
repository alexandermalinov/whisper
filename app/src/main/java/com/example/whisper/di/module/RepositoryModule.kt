package com.example.whisper.di.module

import android.content.Context
import com.example.whisper.data.local.dao.ContactDao
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.local.model.UserModel
import com.example.whisper.data.repository.contacts.ContactsLocalSource
import com.example.whisper.data.repository.contacts.ContactsRemoteSource
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.recentchats.RecentChatsLocalSource
import com.example.whisper.data.repository.recentchats.RecentChatsRemoteSource
import com.example.whisper.data.repository.recentchats.RecentChatsRepository
import com.example.whisper.data.repository.user.UserLocalSource
import com.example.whisper.data.repository.user.UserRemoteSource
import com.example.whisper.data.repository.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun provideUserLocalSource(
        context: Context,
        userDao: UserDao,
        user: UserModel
    ): UserRepository.LocalSource = UserLocalSource(context, userDao, user)

    @Singleton
    @Provides
    fun provideUserRemoteSource(
        firebaseAuth: FirebaseAuth
    ): UserRepository.RemoteSource = UserRemoteSource(firebaseAuth)

    @Singleton
    @Provides
    fun provideUserRepository(
        remote: UserRemoteSource,
        local: UserLocalSource
    ): UserRepository = UserRepository(remote, local)

    @Singleton
    @Provides
    fun provideContactsLocalSource(
        contactDao: ContactDao,
        userDao: UserDao,
        userRepository: UserRepository
    ): ContactsRepository.LocalSource = ContactsLocalSource(contactDao, userDao, userRepository)

    @Singleton
    @Provides
    fun provideContactsRemoteSource(): ContactsRepository.RemoteSource = ContactsRemoteSource()

    @Singleton
    @Provides
    fun provideContactsRepository(
        remote: ContactsRemoteSource,
        local: ContactsLocalSource
    ): ContactsRepository = ContactsRepository(remote, local)

    @Singleton
    @Provides
    fun provideRecentChatsLocalSource(
        contactDao: ContactDao,
        userDao: UserDao,
        currentUser: UserModel
    ): RecentChatsRepository.LocalSource = RecentChatsLocalSource(contactDao, userDao, currentUser)

    @Singleton
    @Provides
    fun provideRecentChatsRemoteSource(): RecentChatsRepository.RemoteSource =
        RecentChatsRemoteSource()

    @Singleton
    @Provides
    fun provideRecentChatsRepository(
        remote: RecentChatsRemoteSource,
        local: RecentChatsLocalSource
    ): RecentChatsRepository = RecentChatsRepository(remote, local)
}