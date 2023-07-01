package com.example.whisper.di.module

import android.app.Application
import com.example.whisper.utils.NetworkHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun provideNetworkHandler(application: Application) = NetworkHandler(application)
}
