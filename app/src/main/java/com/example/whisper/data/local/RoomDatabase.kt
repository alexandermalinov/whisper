package com.example.whisper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.whisper.data.local.converters.ContactTypeConverter
import com.example.whisper.data.local.dao.ContactDao
import com.example.whisper.data.local.dao.RecentChatDao
import com.example.whisper.data.local.dao.UserDao
import com.example.whisper.data.local.entity.Contact
import com.example.whisper.data.local.entity.RecentChat
import com.example.whisper.data.local.entity.User
import com.example.whisper.data.local.entity.UserAndContactCrossRef


@Database(
    entities = [User::class, Contact::class, RecentChat::class, UserAndContactCrossRef::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(ContactTypeConverter::class)
abstract class RoomDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDao

    abstract fun getContactDao(): ContactDao

    abstract fun getRecentChatDao(): RecentChatDao
}