package com.example.whisper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.whisper.data.local.entity.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: User)

    @Query("SELECT * FROM users WHERE user_id == :userId")
    suspend fun getUserById(userId: String): User

    @Query("SELECT * FROM users WHERE user_id == :userId")
    suspend fun getLoggedUser(userId: String): User
}