package com.example.whisper.di.module

import android.app.Application
import android.content.Context
import com.example.whisper.utils.FileUtils
import com.example.whisper.utils.permissions.PermissionChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UtilsModule {

    @Singleton
    @Provides
    fun providePermissionChecker(application: Application) = PermissionChecker(application)

    @Singleton
    @Provides
    fun provideFileUtils(context: Context) = FileUtils(context)
}
