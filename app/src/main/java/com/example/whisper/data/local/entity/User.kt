package com.example.whisper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.whisper.data.local.model.UserModel
import com.example.whisper.utils.common.EMPTY

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "user_id") val userId: String = EMPTY,
    @ColumnInfo(name = "email") val email: String = EMPTY,
    @ColumnInfo(name = "password") val password: String = EMPTY,
    @ColumnInfo(name = "username") val username: String = EMPTY,
    @ColumnInfo(name = "picture") val picture: String = EMPTY,
    @ColumnInfo(name = "notifications_enabled") val isNotificationsEnabled: Boolean = true,
    @ColumnInfo(name = "finger_print_enabled") val isFingerPrintEnabled: Boolean = false,
    @ColumnInfo(name = "pin_password_enabled") val isPinEnabled: Boolean = false,
    @ColumnInfo(name = "pin_password") val pin: String = EMPTY,
)

fun User.toUserModel() = UserModel(
    userId = userId,
    email = email,
    password = password,
    username = username,
    profilePicture = picture,
    isNotificationsEnabled = isNotificationsEnabled,
    isFingerPrintEnabled = isFingerPrintEnabled,
    isPinEnabled = isPinEnabled,
    pin = pin,
)