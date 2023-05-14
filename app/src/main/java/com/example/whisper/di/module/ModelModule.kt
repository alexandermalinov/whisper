package com.example.whisper.di.module

import com.example.whisper.data.local.model.UserModel
import com.example.whisper.utils.common.EMPTY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ModelModule {

    @Singleton
    @Provides
    fun provideUserModel(): UserModel = UserModel(EMPTY, EMPTY, EMPTY, EMPTY, EMPTY)
}
