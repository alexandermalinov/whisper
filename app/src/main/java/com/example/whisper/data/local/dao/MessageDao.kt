package com.example.whisper.data.local.dao

import androidx.room.*
import com.example.whisper.data.local.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Transaction
    @Query("SELECT * FROM messages WHERE id == :id")
    fun getMessage(id: String): Message?

    @Transaction
    @Query("SELECT * FROM messages ORDER BY created_at DESC")
    fun getMessages(): List<Message>

    @Transaction
    @Query("SELECT * FROM messages ORDER BY created_at DESC")
    fun getMessagesFlow(): Flow<List<Message>>

    @Transaction
    @Query("" +
            " SELECT * FROM messages" +
            " WHERE (sender_id = :currentUserId AND receiver_id = :contactId) " +
            " OR (sender_id = :contactId AND receiver_id = :currentUserId)" +
            " ORDER BY created_at DESC"
    )
    fun getMessagesWithContactFlow(currentUserId: String, contactId: String): Flow<List<Message>>

    @Transaction
    @Query("" +
            " SELECT * FROM messages" +
            " WHERE (sender_id = :currentUserId AND receiver_id = :contactId) " +
            " OR (sender_id = :contactId AND receiver_id = :currentUserId)" +
            " ORDER BY created_at DESC"
    )
    fun getContactMessages(currentUserId: String, contactId: String): List<Message>

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Update
    suspend fun updateMessage(message: Message)
}