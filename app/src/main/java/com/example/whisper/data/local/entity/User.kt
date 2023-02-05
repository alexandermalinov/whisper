package com.example.whisper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.whisper.utils.common.EMPTY
import java.io.File

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val id: String,
    @ColumnInfo(name = "user_email")
    val userEmail: String,
    @ColumnInfo(name = "user_password")
    val userPassword: String,
    @ColumnInfo(name = "user_username")
    val username: String,
    @ColumnInfo(name = "user_picture")
    val userPicture: String
)