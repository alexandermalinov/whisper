package com.example.whisper.data.local.dao

import androidx.room.*
import com.example.whisper.data.local.entity.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert
    suspend fun insertContact(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()

    @Transaction
    @Query("SELECT * FROM contacts WHERE contact_url == :contactUrl")
    fun getContact(contactUrl: String): Contact?

    @Transaction
    @Query("SELECT * FROM contacts ORDER BY username")
    fun getContactsFlow(): Flow<List<Contact>>

    @Transaction
    @Query("SELECT * FROM contacts ORDER BY username")
    fun getContacts(): List<Contact>

    @Transaction
    @Query("SELECT * FROM contacts WHERE member_state == 'INVITED' ORDER BY created_at DESC")
    fun getInvitedContacts(): Flow<List<Contact>>

    @Transaction
    @Query("SELECT * FROM contacts WHERE member_state == 'PENDING' ORDER BY created_at DESC")
    fun getPendingContacts(): Flow<List<Contact>>

    @Update
    suspend fun updateContact(contact: Contact)
}