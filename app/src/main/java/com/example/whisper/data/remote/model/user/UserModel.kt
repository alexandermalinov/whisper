package com.example.whisper.data.remote.model.user

import com.example.whisper.utils.common.*
import java.io.File

data class UserModel(
    val id: String = EMPTY,
    val email: String = EMPTY,
    val password: String = EMPTY,
    val username: String = EMPTY,
    val profilePictureUrl: File,
)

fun UserModel.toMap() = mapOf(
    USER_ID to id,
    USER_EMAIL to email,
    USER_PASSWORD to password,
)
