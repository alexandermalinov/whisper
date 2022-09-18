package com.example.whisper.di

import android.content.Context
import com.example.whisper.data.repository.user.UserLocalSource
import com.example.whisper.data.repository.user.UserRepository
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
    fun provideUserLocalSource(context: Context): UserRepository.LocalSource =
        UserLocalSource(context)
}