package com.example.whisper.vo.profile

import com.example.whisper.utils.common.EMPTY

data class ProfileUiState(
    val profilePictureUrl: String = EMPTY,
    val username: String = EMPTY,
    val userEmail: String = EMPTY
)
