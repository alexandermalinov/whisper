package com.example.whisper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.whisper.data.local.entity.Contact
import com.example.whisper.data.local.entity.User
import com.example.whisper.data.local.model.ContactModel
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: User)

    @Query("SELECT * FROM users WHERE user_id == :userId")
    suspend fun getUserById(userId: String): User

    @Query("SELECT * FROM users WHERE email == :userEmail")
    suspend fun getLoggedUser(userEmail: String): User

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE email == :userEmail")
    fun getUserFlow(userEmail: String): Flow<User>
}