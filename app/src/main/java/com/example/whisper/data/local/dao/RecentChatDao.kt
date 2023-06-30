package com.example.whisper.data.local.dao

import androidx.room.*
import com.example.whisper.data.local.entity.RecentChat
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentChat(recentChat: RecentChat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentChats(contacts: List<RecentChat>)

    @Transaction
    @Query("SELECT * FROM recent_chats WHERE contact_url == :recentChatUrl")
    fun getRecentChat(recentChatUrl: String): RecentChat?

    @Transaction
    @Query("SELECT * FROM recent_chats ORDER BY last_message_timestamp DESC")
    fun getRecentChats(): List<RecentChat>

    @Transaction
    @Query("SELECT * FROM recent_chats ORDER BY last_message_timestamp DESC")
    fun getRecentChatsFlow(): Flow<List<RecentChat>>

    @Delete
    suspend fun deleteRecentChat(contact: RecentChat)

    @Query("DELETE FROM recent_chats")
    suspend fun deleteAllRecentChats()

    @Update
    suspend fun updateRecentChat(recentChat: RecentChat)
}