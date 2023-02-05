package com.example.whisper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.local.entity.User


@Database(
    entities = [User::class],
    version = 1,
    exportSchema = true
)
abstract class RoomDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDao
}