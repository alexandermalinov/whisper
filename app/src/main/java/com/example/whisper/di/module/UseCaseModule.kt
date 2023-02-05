package com.example.whisper.di.module

import com.example.whisper.domain.signup.ValidateEmailUseCase
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
