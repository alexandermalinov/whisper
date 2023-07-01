package com.example.whisper.data.local.model

import com.example.whisper.data.local.entity.User
import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.USER_EMAIL
import com.example.whisper.utils.common.USER_ID
import com.example.whisper.utils.common.USER_PASSWORD

data class UserModel(
    val userId: String,
    val email: String,
    val password: String,
    val username: String,
    val profilePicture: String,
    val isNotificationsEnabled: Boolean = false,
    val isFingerPrintEnabled: Boolean = false,
    val isPinEnabled: Boolean = false,
    val pin: String = EMPTY,
)

fun UserModel.toUser() = User(
    userId = userId,
    email = email,
    password = password,
    username = username,
    picture = profilePicture,
    isNotificationsEnabled = isNotificationsEnabled,
    isFingerPrintEnabled = isFingerPrintEnabled,
    isPinEnabled = isPinEnabled,
    pin = pin,
)

fun com.sendbird.android.User.toUserModel() = UserModel(
    userId = userId,
    email = metaData[USER_EMAIL].toString(),
    password = metaData[USER_PASSWORD].toString(),
    username = nickname,
    profilePicture = profileUrl
)

fun UserModel.toMap() = mapOf(
    USER_ID to userId,
    USER_EMAIL to email,
    USER_PASSWORD to password,
)
