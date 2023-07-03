package com.example.whisper.di.module

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import com.example.whisper.utils.common.USER_SHARED_PREFS_KEY
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideContext(app: Application): Context =
        app.applicationContext

    @Singleton
    fun provideApplication(app: Application): Application = app

    @Singleton
    @Provides
    fun provideSharedPreferences(application: Application): SharedPreferences =
        application.getSharedPreferences(
            USER_SHARED_PREFS_KEY,
            Context.MODE_PRIVATE
        )

    @Singleton
    @Provides
    fun provideFirebaseAuth() = Firebase.auth

    @Singleton
    @Provides
    fun provideContentResolver(app: Application): ContentResolver = app.contentResolver
}