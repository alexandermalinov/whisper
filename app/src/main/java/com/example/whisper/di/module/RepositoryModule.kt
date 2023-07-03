package com.example.whisper.di.module

import android.content.ContentResolver
import android.content.Context
import com.example.whisper.data.local.dao.ContactDao
import com.example.whisper.data.local.dao.MessageDao
import com.example.whisper.data.local.dao.RecentChatDao
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.local.model.ContactModel
import com.example.whisper.data.local.model.UserModel
import com.example.whisper.data.repository.contacts.ContactsLocalSource
import com.example.whisper.data.repository.contacts.ContactsRemoteSource
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.images.ImagesLocalSource
import com.example.whisper.data.repository.images.ImagesRepository
import com.example.whisper.data.repository.messages.MessagesLocalSource
import com.example.whisper.data.repository.messages.MessagesRemoteSource
import com.example.whisper.data.repository.messages.MessagesRepository
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
    ): UserRepository.LocalSource = UserLocalSource(context, userDao)

    @Singleton
    @Provides
    fun provideUserRemoteSource(
        firebaseAuth: FirebaseAuth
    ): UserRepository.RemoteSource = UserRemoteSource(firebaseAuth)

    @Singleton
    @Provides
    fun provideUserRepository(
        remote: UserRemoteSource,
        local: UserLocalSource,
        cachedUserModel: UserModel
    ): UserRepository = UserRepository(remote, local, cachedUserModel)

    @Singleton
    @Provides
    fun provideContactsLocalSource(
        contactDao: ContactDao
    ): ContactsRepository.LocalSource = ContactsLocalSource(contactDao)

    @Singleton
    @Provides
    fun provideContactsRemoteSource(): ContactsRepository.RemoteSource = ContactsRemoteSource()

    @Singleton
    @Provides
    fun provideContactsRepository(
        remote: ContactsRemoteSource,
        local: ContactsLocalSource,
        cachedContacts: List<ContactModel>
    ): ContactsRepository = ContactsRepository(remote, local, cachedContacts)

    @Singleton
    @Provides
    fun provideRecentChatsLocalSource(
        recentChatDao: RecentChatDao
    ): RecentChatsRepository.LocalSource = RecentChatsLocalSource(recentChatDao)

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


    @Singleton
    @Provides
    fun provideMessagesLocalSource(
        messageDao: MessageDao
    ): MessagesRepository.LocalSource = MessagesLocalSource(messageDao)

    @Singleton
    @Provides
    fun provideMessagesRemoteSource(): MessagesRepository.RemoteSource = MessagesRemoteSource()

    @Singleton
    @Provides
    fun provideMessagesRepository(
        remote: MessagesRemoteSource,
        local: MessagesLocalSource,
        userRepository: UserRepository,
        contactsRepository: ContactsRepository,
        recentChatsRepository: RecentChatsRepository
    ): MessagesRepository =
        MessagesRepository(remote, local, userRepository, contactsRepository, recentChatsRepository)

    @Singleton
    @Provides
    fun provideImagesLocalSource(contentResolver: ContentResolver) =
        ImagesLocalSource(contentResolver)

    @Singleton
    @Provides
    fun provideImagesRepository(localSource: ImagesLocalSource) = ImagesRepository(localSource)
}