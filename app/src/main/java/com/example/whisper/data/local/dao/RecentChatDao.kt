package com.example.whisper.data.local.dao

import androidx.room.*
import com.example.whisper.data.local.entity.Contact
import com.example.whisper.data.local.entity.RecentChat
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentChat(recentChat: RecentChat)

    @Transaction
    @Query("SELECT * FROM recent_chats WHERE contact_url == :recentChatUrl")
    fun getRecentChat(recentChatUrl: String): RecentChat

    @Transaction
    @Query("SELECT * FROM recent_chats")
    fun getRecentChats(): Flow<List<RecentChat>>
}