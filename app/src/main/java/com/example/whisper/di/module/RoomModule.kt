package com.example.whisper.di.module

import android.app.Application
import androidx.room.Room
import com.example.whisper.data.local.RoomDatabase
import com.example.whisper.data.local.dao.ContactDao
import com.example.whisper.data.local.dao.RecentChatDao
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.utils.common.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Singleton
    @Provides
    fun provideRoomDatabase(application: Application): RoomDatabase =
        Room.databaseBuilder(
            application.applicationContext,
            RoomDatabase::class.java,
            DATABASE_NAME
        ).build()

    @Singleton
    @Provides
    fun provideUserDao(database: RoomDatabase): UserDao = database.getUserDao()

    @Singleton
    @Provides
    fun provideContactDao(database: RoomDatabase): ContactDao = database.getContactDao()

    @Singleton
    @Provides
    fun provideRecentChatDao(database: RoomDatabase): RecentChatDao = database.getRecentChatDao()
}