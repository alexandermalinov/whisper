package com.example.whisper.di.module

import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.contacts.ContactsUpdateLister
import com.example.whisper.data.repository.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ListenersModule {

    @Singleton
    @Provides
    fun provideContactsRepository(
        contactsRepository: ContactsRepository,
        userRepository: UserRepository
    ): ContactsUpdateLister =
        ContactsUpdateLister(contactsRepository, userRepository)
}