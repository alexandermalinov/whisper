package com.example.whisper.di

import android.content.Context
import com.example.whisper.data.repository.user.UserLocalSource
import com.example.whisper.data.repository.user.UserRepository
import com.example.whisper.domain.signing.ValidateEmailUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Singleton
    @Provides
    fun provideValidateEmailUserCase(): ValidateEmailUseCase = ValidateEmailUseCase()
}
