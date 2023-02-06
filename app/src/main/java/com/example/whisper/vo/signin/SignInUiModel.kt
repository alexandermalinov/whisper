package com.example.whisper.vo.signin

import com.example.whisper.utils.common.EMPTY
import com.example.whisper.utils.common.INVALID_RES

data class SignInUiModel(
    val email: String = EMPTY,
    val password: String = EMPTY,
    val emailErrorEnabled: Boolean = false,
    val emailError: Int = INVALID_RES,
    val emailEndIcon: Int? = INVALID_RES,
    val passwordError: Int = INVALID_RES,
    val passwordErrorEnabled: Boolean = false,
    val isContinueEnabled: Boolean = false,
    val isLoading: Boolean = false
)
