package com.example.whisper.di.module

import android.content.Context
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.repository.contacts.ContactsRemoteSource
import com.example.whisper.data.repository.contacts.ContactsRepository
import com.example.whisper.data.repository.user.UserLocalSource
import com.example.whisper.data.repository.user.UserRemoteSource
import com.example.whisper.data.repository.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideUserLocalSource(context: Context, userDao: UserDao): UserRepository.LocalSource =
        UserLocalSource(context, userDao)

    @Singleton
    @Provides
    fun provideUserRemoteSource(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserRepository.RemoteSource = UserRemoteSource(firebaseAuth, firestore)

    @Singleton
    @Provides
    fun provideContactsRemoteSource(): ContactsRepository.RemoteSource = ContactsRemoteSource()
}