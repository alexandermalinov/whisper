package com.example.whisper.data.local.dao

import androidx.room.*
import com.example.whisper.data.local.entity.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Transaction
    @Query("SELECT * FROM contacts WHERE contact_url == :contactUrl")
    fun getContact(contactUrl: String): Contact

    @Transaction
    @Query("SELECT * FROM contacts WHERE member_state == 'JOINED' ORDER BY username")
    fun getContacts(): Flow<List<Contact>>

    @Transaction
    @Query("SELECT * FROM contacts WHERE member_state == 'INVITED' ORDER BY created_at DESC")
    fun getInvitedContacts(): Flow<List<Contact>>

    @Transaction
    @Query("SELECT * FROM contacts WHERE member_state == 'PENDING' ORDER BY created_at DESC")
    fun getPendingContacts(): Flow<List<Contact>>

    @Update
    suspend fun updateContact(contact: Contact)
}